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

import jdk.internal.nicl.types.LayoutTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.nicl.Scope;
import java.nicl.types.Pointer;
import java.nicl.types.Transformer;
import java.util.HashMap;

class CivilizerAutoConversions {
    interface Projection<T, U> {
        java.lang.reflect.Type sourceType();
        java.lang.reflect.Type targetType();

        U project(T arg, Scope scope);

        @SuppressWarnings("unchecked")
        private <V> Class<V> getTypeClass(java.lang.reflect.Type type) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)type;

                return (Class<V>)pt.getRawType();
            } else {
                return (Class<V>)type;
            }
        }

        default Class<T> sourceClass() {
            return getTypeClass(sourceType());
        }

        default Class<U> targetClass() {
            return getTypeClass(targetType());
        }
    }

    private static final HashMap<java.lang.reflect.Type,Projection<?, ?>> projectionsFrom = new HashMap<>();
    private static final HashMap<java.lang.reflect.Type,Projection<?, ?>> projectionsTo = new HashMap<>();

    static {
        java.lang.reflect.Type p2bType = new LayoutTypeImpl.PointerType(Byte.class);

        // Pointer<Byte> -> String projection
        projectionsFrom.put(p2bType, new Projection<Pointer<Byte>, String>() {
            @Override
            @SuppressWarnings("unchecked")
            public String project(Pointer<Byte> o, Scope scope) {
                return Transformer.toString(o);
            }

            @Override
            public java.lang.reflect.Type sourceType() {
                return p2bType;
            }

            @Override
            public java.lang.reflect.Type targetType() {
                return String.class;
            }
        });

        // String -> Pointer<Byte> projection
        projectionsTo.put(p2bType, new Projection<String, Pointer<Byte>>() {
            @Override
            public Pointer<Byte> project(String o, Scope scope) {
                return Transformer.toCString(o, scope);
            }

            @Override
            @SuppressWarnings("unchecked")
            public java.lang.reflect.Type sourceType() {
                return String.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public java.lang.reflect.Type targetType() {
                return p2bType;
            }
        });
    }

    static class IdentityProjection<T> implements Projection<T, T> {
        private final java.lang.reflect.Type type;

        IdentityProjection(java.lang.reflect.Type type) {
            this.type = type;
        }

        @Override
        public java.lang.reflect.Type sourceType() {
            return type;
        }

        @Override
        public java.lang.reflect.Type targetType() {
            return type;
        }

        @Override
        public T project(T arg, Scope scope) {
            return arg;
        }
    }

    @SuppressWarnings("rawtypes")
    public static Projection<?, ?> getProjection(java.lang.reflect.Type type, boolean projectTo) {
        HashMap<java.lang.reflect.Type,Projection<?, ?>> map = projectTo ? projectionsTo : projectionsFrom;
        Projection<?, ?> p = map.get(type);
        if (p == null) {
            return new IdentityProjection(type);
        } else {
            return p;
        }
    }
}
