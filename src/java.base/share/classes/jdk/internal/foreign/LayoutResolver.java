/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.internal.foreign;

import java.foreign.layout.Sequence;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.foreign.layout.Function;
import java.foreign.layout.Group;
import java.foreign.layout.Group.Kind;
import java.foreign.layout.Layout;
import java.foreign.layout.Unresolved;
import java.foreign.annotations.NativeStruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This class acts as some kind of shared type dictionary. Note that it is necessary for the framework
 * to scan reflective signatures to look for forward references to other structs (otherwise layout resolution
 * will fail).
 */
public final class LayoutResolver {
    private LayoutResolver(Class<?> root) {
        addClass(root);
    }

    private final Map<String, Group> descriptorToGroup = new HashMap<>();
    private final Map<String, Group> nameToGroup = new HashMap<>();

    private Optional<Layout> resolveRoot(String name) {
        return Optional.ofNullable(nameToGroup.get(name));
    }

    private static final ClassValue<LayoutResolver> resolvers = new ClassValue<>() {
        @Override
        protected LayoutResolver computeValue(Class<?> c) {
            return new LayoutResolver(c);
        }
    };

    public static LayoutResolver get(Class<?> c) {
        while (c.getEnclosingClass() != null) {
            c = c.getEnclosingClass();
        }
        return resolvers.get(c);
    }

    private void addClass(Class<?> enclosing) {
        Class<?>[] inner = enclosing.getClasses();

        for (Class<?> c : inner) {
            if (Util.isCStruct(c)) {
                addCStruct(c);
            } else {
                addClass(c);
            }
        }
    }

    void scanType(Type t) {
        if (t instanceof ParameterizedType) {
            Stream.of(((ParameterizedType)t).getActualTypeArguments())
                    .forEach(this::scanType);
        } else if (t instanceof GenericArrayType) {
            scanType(((GenericArrayType)t).getGenericComponentType());
        } else if (t instanceof Class<?>) {
            Class<?> cl = (Class<?>)t;
            if (cl.isArray()) {
                scanType(cl.getComponentType());
            } else if (Util.isCStruct(cl)) {
                addCStruct(cl);
            }
        }
    }
    //where
    private void addCStruct(Class<?> cl) {
        String layout = cl.getAnnotation(NativeStruct.class).value();
        Layout l = Layout.of(layout);
        if (l instanceof Unresolved) {
            // ignore undefined struct
            return;
        }
        Group group = descriptorToGroup.computeIfAbsent(layout, ignored -> (Group) l);
        group.name().ifPresent(name -> {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("name cannot be empty");
            }
            if (nameToGroup.containsKey(name) && nameToGroup.get(name) != group) {
                throw new IllegalArgumentException(name + " cannot be redefined");
            }
            nameToGroup.put(name, group);
        });
    }

    public void scanMethod(Method m) {
        Stream.of(m.getGenericParameterTypes()).forEach(this::scanType);
        scanType(m.getGenericReturnType());
    }

    public Function resolve(Function f) {
        if (!f.isPartial()) {
            return f;
        } else {
            Layout[] newArgs = f.argumentLayouts().stream()
                    .map(this::resolve)
                    .toArray(Layout[]::new);
            return (f.returnLayout().isEmpty()) ?
                    Function.ofVoid(f.isVariadic(), newArgs) :
                    Function.of(resolve(f.returnLayout().get()), f.isVariadic(), newArgs);
        }
    }

    public Layout resolve(Layout l) {
        if (!l.isPartial()) {
            return l;
        } else {
            Map<String, String> annotations = l.annotations();
            Layout rv;

            if (l instanceof Unresolved) {
                rv = resolveRoot(l.name().get()).orElseThrow(IllegalStateException::new);
                if (rv.isPartial()) {
                    return resolve(rv);
                }
            } else if (l instanceof Sequence) {
                Sequence s = (Sequence)l;
                Layout elem = resolve(s.element());
                rv = Sequence.of(s.elementsSize(), elem);
            } else if (l instanceof Group) {
                Group g = (Group)l;
                Layout[] newElems = g.elements().stream()
                        .map(this::resolve)
                        .toArray(Layout[]::new);
                rv = g.kind() == Kind.STRUCT ?
                        Group.struct(newElems) :
                        Group.union(newElems);
            } else {
                return l;
            }

            // Put back original annotations
            for (Map.Entry<String, String> e : annotations.entrySet()) {
                rv = rv.withAnnotation(e.getKey(), e.getValue());
            }
            return rv;
        }
    }
}
