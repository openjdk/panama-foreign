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
package java.nicl;

import jdk.internal.misc.Unsafe;
import jdk.internal.nicl.types.BoundedMemoryRegion;
import jdk.internal.nicl.types.BoundedPointer;
import jdk.internal.nicl.types.PointerTokenImpl;

import java.nicl.types.*;

public class RuntimeSupport {
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    // FIXME: Only here for debugging/until all uses of pointer have been updated
    @Deprecated
    public static PointerToken debugCreatePointerToken() {
        return new PointerTokenImpl();
    }

    public static long unpack(Pointer<?> ptr, PointerToken token) throws IllegalAccessException {
        if (ptr == null) {
            return 0;
        }
        return ptr.addr(token);
    }

    public static <T> Pointer<T> createPtr(long addr, LayoutType<T> type) {
        return createPtr(null, addr, type);
    }

    public static <T> Pointer<T> createPtr(Object base, long addr, LayoutType<T> type) {
        // FIXME: Long.MAX_VALUE is not correct
        return new BoundedPointer<>(type, new BoundedMemoryRegion(base, addr, Long.MAX_VALUE), 0);
    }

    public static long strlen(long addr) {
        long i = 0;

        while (UNSAFE.getByte(addr + i) != 0) {
            i++;
        }

        return i;
    }

    // FIXME: Helper methods useful for playing with pointers into the Java heap and data copying

    public static MemoryRegion createRegionForArrayElements(long[] arr) {
        return new BoundedMemoryRegion(arr, jdk.internal.misc.Unsafe.getUnsafe().arrayBaseOffset(long[].class), arr.length * 8, MemoryRegion.MODE_RW);
    }

    public static MemoryRegion createRegionForArrayElements(byte[] arr) {
        return new BoundedMemoryRegion(arr, jdk.internal.misc.Unsafe.getUnsafe().arrayBaseOffset(byte[].class), arr.length, MemoryRegion.MODE_RW);
    }

    public static MemoryRegion createRegionForArrayElements(long[] arr, Scope scope) {
        return new BoundedMemoryRegion(arr, jdk.internal.misc.Unsafe.getUnsafe().arrayBaseOffset(long[].class), arr.length * 8, MemoryRegion.MODE_RW, scope);
    }

    public static Pointer<Long> createArrayElementsPointer(long[] arr) {
        return new BoundedPointer<>(NativeLibrary.createLayout(long.class), createRegionForArrayElements(arr), 0);
    }

    public static Pointer<Byte> createArrayElementsPointer(byte[] arr) {
        return new BoundedPointer<>(NativeLibrary.createLayout(byte.class), createRegionForArrayElements(arr), 0);
    }

    public static Pointer<Long> createArrayElementsPointer(long[] arr, Scope scope) {
        return new BoundedPointer<>(NativeLibrary.createLayout(long.class), createRegionForArrayElements(arr, scope), 0);
    }

    public static void copy(Pointer<?> src, Pointer<?> dst, long bytes) throws IllegalAccessException {
        BoundedPointer<?> bsrc = (BoundedPointer<?>)src;
        BoundedPointer<?> bdst = (BoundedPointer<?>)dst;

        bsrc.copyTo(bdst, bytes);
    }

    /*** FIXME: Temporary exports ***/
    @Deprecated
    public static <T> Reference<T> buildRef(Pointer<?> p, long offset, LayoutType<T> type) {
        return buildPtr(p, offset, type).lvalue();
    }

    private static <T> Pointer<T> buildPtr(Pointer<?> p, long offset, LayoutType<T> type) {
        return p
                .cast(NativeLibrary.createLayout(byte.class))
                .offset(offset)
                .cast(type);
    }

    @Deprecated
    public static void copyFromArray(int[] src, Pointer<?> p, long offset, int nElems) {
        Pointer<Integer> dst = buildPtr(p, offset, NativeLibrary.createLayout(int.class));
        for (int i = 0; i < nElems; i++) {
            dst.offset(i).lvalue().set(src[i]);
        }
    }

    @Deprecated
    public static void copyToArray(Pointer<?> p, long offset, int[] dst, int nElems) {
        Pointer<Integer> src = buildPtr(p, offset, NativeLibrary.createLayout(int.class));
        for (int i = 0; i < nElems; i++) {
            dst[i] = src.offset(i).lvalue().get();
        }
    }

    @Deprecated
    public static <T> void copyFromArray(T[] src, Pointer<?> p, long offset, int nElems, Class<T> elementType) {
        Pointer<T> dst = buildPtr(p, offset, NativeLibrary.createLayout(elementType));
        for (int i = 0; i < nElems; i++) {
            dst.offset(i).lvalue().set(src[i]);
        }
    }

    @Deprecated
    public static <T> void copyToArray(Pointer<?> p, long offset, T[] dst, int nElems, Class<T> elementType) {
        Pointer<T> src = buildPtr(p, offset, NativeLibrary.createLayout(elementType));
        for (int i = 0; i < nElems; i++) {
            dst[i] = src.offset(i).lvalue().get();
        }
    }
}
