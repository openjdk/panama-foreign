/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
package java.nicl.types;

import jdk.internal.misc.SharedSecrets;
import jdk.internal.nicl.Util;
import jdk.internal.nicl.types.BoundedPointer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * @param <T> The type of the pointee
 */
public interface Pointer<T> extends Resource {
    Pointer<T> offset(long nElements);
    <S> Pointer<S> cast(LayoutType<S> type);

    boolean isNull();

    long addr() throws IllegalAccessException;

    Reference<T> lvalue();
    T deref();

    // These static utility methods are invoked from generated code

    // Pointer utilities
    public static <T> Reference<T> buildRef(Pointer<?> p, long offset, LayoutType<T> type) {
        return buildPtr(p, offset, type).lvalue();
    }

    private static <T> Pointer<T> buildPtr(Pointer<?> p, long offset, LayoutType<T> type) {
        return p
                .cast(LayoutType.create(byte.class))
                .offset(offset)
                .cast(type);
    }

    public static void copyFromArray(int[] src, Pointer<?> p, long offset, int nElems) {
        Pointer<Integer> dst = buildPtr(p, offset, LayoutType.create(int.class));
        for (int i = 0; i < nElems; i++) {
            dst.offset(i).lvalue().set(src[i]);
        }
    }

    public static void copyToArray(Pointer<?> p, long offset, int[] dst, int nElems) {
        Pointer<Integer> src = buildPtr(p, offset, LayoutType.create(int.class));
        for (int i = 0; i < nElems; i++) {
            dst[i] = src.offset(i).lvalue().get();
        }
    }

    public static <T> void copyFromArray(T[] src, Pointer<?> p, long offset, int nElems, Class<T> elementType) {
        Pointer<T> dst = buildPtr(p, offset, LayoutType.create(elementType));
        for (int i = 0; i < nElems; i++) {
            dst.offset(i).lvalue().set(src[i]);
        }
    }

    public static <T> void copyToArray(Pointer<?> p, long offset, T[] dst, int nElems, Class<T> elementType) {
        Pointer<T> src = buildPtr(p, offset, LayoutType.create(elementType));
        for (int i = 0; i < nElems; i++) {
            dst[i] = src.offset(i).lvalue().get();
        }
    }

    public static void copy(Pointer<?> src, Pointer<?> dst, long bytes) throws IllegalAccessException {
        Util.copy(src, dst, bytes);
    }

    public static String toString(Pointer<Byte> cstr) {
        if (cstr == null || cstr.isNull()) {
            return null;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte b;
        for (int i = 0; (b = cstr.offset(i).lvalue().get()) != 0; i++) {
            os.write(b);
        }
        return os.toString();
    }

    public static ByteBuffer asDirectByteBuffer(Pointer<?> buf, int bytes) throws IllegalAccessException {
        return SharedSecrets.getJavaNioAccess().newDirectByteBuffer(
                buf.addr(), bytes, null);
    }

    /**
     * Obtains the {@code NULL} pointer.
     * @param <Z> the null pointer type.
     * @return the {@code NULL} pointer.
     */
    @SuppressWarnings("unchecked")
    static <Z extends Pointer<?>> Z nullPointer() {
        return (Z) BoundedPointer.NULL;
    }
}
