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

import java.foreign.Scope;

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
     * @param clazz Class object representing the struct.
     * @return the size of the struct in bytes.
     */
    static <X extends Struct<X>> long sizeof(Class<X> clazz) {
        return LayoutType.ofStruct(clazz).bytesSize();
    }
}
