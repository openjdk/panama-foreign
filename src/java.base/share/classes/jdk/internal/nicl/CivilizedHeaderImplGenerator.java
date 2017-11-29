/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nicl.NativeScope;
import java.nicl.Scope;
import java.util.stream.Stream;
import static sun.security.action.GetPropertyAction.privilegedGetProperty;

class CivilizedHeaderImplGenerator<T> extends ClassGenerator {
    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.nicl.CivilizedHeaderImplGenerator.DEBUG"));

    // the interface/type to implement
    private final Class<T> c;

    // the raw instance to delegate to
    private final T rawInstance;

    public CivilizedHeaderImplGenerator(Class<?> hostClass, String implClassName, Class<T> c, T rawInstance) {
        super(hostClass, implClassName, null);

        this.c = c;
        this.rawInstance = rawInstance;
    }

    private CivilizerAutoConversions.Projection<?, ?>[] getArgumentProjections(Method m) {
        return Stream.of(m.getGenericParameterTypes()).map(t -> CivilizerAutoConversions.getProjection(t, true)).toArray(CivilizerAutoConversions.Projection[]::new);
    }

    private CivilizerAutoConversions.Projection<?, ?> getReturnProjection(Method m) {
        return CivilizerAutoConversions.getProjection(m.getGenericReturnType(), false);
    }

    static class CivilizingInvoker {
        private final MethodHandle target;
        private final CivilizerAutoConversions.Projection<?, ?> returnProjection;
        private final CivilizerAutoConversions.Projection<?, ?>[] argumentProjections;

        CivilizingInvoker(MethodHandle target, CivilizerAutoConversions.Projection<?, ?> returnProjection, CivilizerAutoConversions.Projection<?, ?>[] argumentProjections) {
            this.target = target;
            this.returnProjection = returnProjection;
            this.argumentProjections = argumentProjections;
        }

        private MethodHandle generateMethodHandle() {
            MethodType methodType =
                    MethodType.methodType(returnProjection.targetClass(),
                            Stream.of(argumentProjections).map(CivilizerAutoConversions.Projection::sourceClass).toArray(Class[]::new));

            try {
                return MethodHandles
                        .lookup()
                        .findVirtual(CivilizingInvoker.class, "invoke", MethodType.methodType(Object.class, Object[].class))
                        .bindTo(this)
                        .asCollector(Object[].class, target.type().parameterCount())
                        .asType(methodType);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Object invoke(Object[] args) {
            if (DEBUG) {
                for (Object o : args) {
                    System.out.println(" arg: " + o);
                }
            }

            Object[] newArgs = new Object[args.length];

            if (args.length != argumentProjections.length) {
                throw new IllegalArgumentException("Invalid number of arguments: " + args.length + ", expected " + argumentProjections.length);
            }

            try (Scope scope = new NativeScope()) {
                for (int i = 0; i < args.length; i++) {
                    @SuppressWarnings("unchecked")
                    CivilizerAutoConversions.Projection<Object, Object> p = (CivilizerAutoConversions.Projection<Object, Object>)argumentProjections[i];
                    newArgs[i] = p.project(args[i], scope);
                }

                try {
                    Object ret = target.asSpreader(Object[].class, target.type().parameterCount()).invoke(newArgs);

                    @SuppressWarnings("unchecked")
                    CivilizerAutoConversions.Projection<Object, Object> p = (CivilizerAutoConversions.Projection<Object, Object>)returnProjection;
                    return p.project(ret, scope);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        }
    }

    private void generateMethod(ClassGeneratorContext ctxt, Method method) {
        MethodHandle rawTarget;
        try {
            rawTarget = MethodHandles
                    .publicLookup()
                    .findVirtual(c, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()))
                    .bindTo(rawInstance);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        MethodHandle civilizedTarget = new CivilizingInvoker(rawTarget, getReturnProjection(method), getArgumentProjections(method)).generateMethodHandle();

        MethodGenerator generator = new GenericMethodImplGenerator(ctxt, method.getName(), civilizedTarget.type(), method.isVarArgs(), civilizedTarget);
        generator.generate();
    }

    private void generateMethods(ClassGeneratorContext ctxt) {
        Stream.of(c.getMethods())
            .forEach(method -> generateMethod(ctxt, method));
    }

    @Override
    void generate(ClassGeneratorContext ctxt) {
        generateMethods(ctxt);
    }
}
