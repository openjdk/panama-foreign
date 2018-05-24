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

package jdk.internal.nicl;

import jdk.internal.nicl.types.DescriptorParser;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nicl.layout.Group;
import java.nicl.layout.Layout;
import java.nicl.layout.Unresolved;
import java.nicl.metadata.NativeStruct;
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

    private LayoutResolver() { }

    private Map<String, Group> roots = new HashMap<>();

    public Optional<Layout> resolve(Unresolved selector) {
        return resolveRoot(selector.name().get());
    }

    private Optional<Layout> resolveRoot(String name) {
        return Optional.ofNullable(roots.get(name));
    }

    private static LayoutResolver resolver = new LayoutResolver();

    public static LayoutResolver instance() {
        return resolver;
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
            } else if (cl.isAnnotationPresent(NativeStruct.class)) {
                Group g = (Group) DescriptorParser.parseLayout(cl.getAnnotation(NativeStruct.class).value());
                addRoot(g);
            }
        }
    }
    //where
    private void addRoot(Group group) {
        group.name().ifPresent(name -> roots.put(name, group));
    }

    void scanMethod(Method m) {
        Stream.of(m.getGenericParameterTypes()).forEach(this::scanType);
        scanType(m.getGenericReturnType());
    }
}
