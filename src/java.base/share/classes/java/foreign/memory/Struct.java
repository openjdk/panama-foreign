/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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
package java.foreign.memory;

import jdk.internal.foreign.Util;

import java.foreign.Scope;
import java.foreign.annotations.NativeStruct;
import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.lang.reflect.Method;
import java.util.StringJoiner;

/**
 * This interface acts as the root type for all native types handled by the binder. A native type must be annotated
 * with the {@link java.foreign.annotations.NativeStruct} annotation, which contains information about the native type's
 * layout.
 * @param <T> the Java type modelling this native type.
 */
public interface Struct<T extends Struct<T>> extends Resource {

    /**
     * Return a pointer to the managed struct.
     * @return a pointer.
     */
    Pointer<T> ptr();

    @Override
    default Scope scope() {
        return ptr().scope();
    }

    /**
     * Return the size of the struct represented by the given class.
     *
     * @param <X> the struct type.
     * @param clazz Class object representing the struct.
     * @return the size of the struct in bytes.
     */
    static <X extends Struct<X>> long sizeof(Class<X> clazz) {
        return LayoutType.ofStruct(clazz).bytesSize();
    }

    /**
     * Copy contents of source struct into destination struct.
     * @param src source struct.
     * @param dst destination struct.
     * @param <Z> the struct carrier type.
     * @throws IllegalArgumentException if the two structs have different layouts.
     */
    static <Z extends Struct<Z>> void assign(Struct<Z> src, Struct<Z> dst) throws IllegalArgumentException {
        if (!src.ptr().type().layout().equals(dst.ptr().type().layout())) {
            throw new IllegalArgumentException("Structs have different layouts!");
        }
        try {
            Pointer.copy(src.ptr(), dst.ptr(), src.ptr().type().bytesSize());
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Returns a string representation of a struct object.
     *
     * Since accessing inactive fields of unions is undefined behaviour,
     * this method will throw an {@link IllegalArgumentException} if a union is passed as an argument.
     *
     * @param struct The struct object
     * @return a string representation of the passed struct
     * @throws IllegalArgumentException if the struct is a union
     * @throws ReflectiveOperationException if an exception occurs while calling the struct's getters
     */
    static String toString(Struct<?> struct) throws IllegalArgumentException, ReflectiveOperationException {
        Class<?> structClass = Util.findStructInterface(struct);
        NativeStruct ns = structClass.getAnnotation(NativeStruct.class);

        Group layout = (Group) Layout.of(ns.value());
        if(layout.kind() == Group.Kind.UNION) {
            throw new IllegalArgumentException("Can not safely toString unions.");
        }

        String name = layout.name().orElse(structClass.getName());
        StringJoiner sj = new StringJoiner(", ", name + "{ ", " }");
        for(Layout e : layout.elements()) {
            String getter = e.annotations().get("get");
            if(getter == null) continue;

            Method mGetter = Util.getterByName(structClass, getter);
            if(mGetter == null) continue;

            sj.add(getter + "=" + mGetter.invoke(struct).toString());
        }

        return sj.toString();
    }
}
