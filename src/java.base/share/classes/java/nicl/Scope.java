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

import jdk.internal.nicl.HeapScope;
import jdk.internal.nicl.NativeScope;
import jdk.internal.nicl.Util;

import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.nicl.types.Reference;
import java.nicl.types.Resource;
import java.util.List;
import java.util.function.Function;

public interface Scope extends AutoCloseable {
    void checkAlive();

    @Deprecated
    void startAllocation();

    @Deprecated
    void endAllocation();

    <T> Pointer<T> allocate(LayoutType<T> type);

    /**
     * Allocate storage for a number of element of specified type.
     * @param type The LayoutType of the element
     * @param count The numbder of elements to be allocated
     * @return A pointer to the allocated storage points to the first element.
     */
    <T> Pointer<T> allocateArray(LayoutType<T> type, long count);

    @Deprecated
    default <T> Pointer<T> allocate(LayoutType<T> type, long count) {
        return allocateArray(type, count);
    }

    // FIXME: When .ptr().deref works as expected for structs (returns
    // an actual struct instance instead of a ReferenceImpl instance)
    // this can be removed
    <T extends Reference<T>> T allocateStruct(LayoutType<T> type);

    void free(Resource ptr);

    void handoff(Resource ptr);

    @Override
    void close();

    private Pointer<Byte> toNativeBuffer(byte[] ar, boolean appendNull) {
        try {
            if (ar == null || ar.length == 0) {
                if (appendNull) {
                    Pointer<Byte> buf = allocate(Util.BYTE_TYPE);
                    buf.lvalue().set((byte) 0);
                    return buf;
                } else {
                    return Pointer.nullPointer();
                }
            }

            int len = ar.length;
            if (appendNull) {
                len += 1;
            }

            Pointer<Byte> buf = allocateArray(Util.BYTE_TYPE, len);
            Pointer<Byte> src = Util.createArrayElementsPointer(ar);
            Util.copy(src, buf, ar.length);
            if (appendNull) {
                buf.offset(ar.length).lvalue().set((byte) 0);
            }
            return buf;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public default Pointer<Byte> toNativeBuffer(byte[] ar) {
        if (null == ar || ar.length == 0) {
            return Pointer.nullPointer();
        }
        return toNativeBuffer(ar, false);
    }

    public default Pointer<Byte> toCString(String str) {
        if (null == str) {
            return Pointer.nullPointer();
        }
        return toNativeBuffer(str.getBytes(), true);
    }

    public default Pointer<Pointer<Byte>> toCStrArray(String[] ar) {
        if (ar.length == 0) {
            return null;
        }

        Pointer<Pointer<Byte>> ptr = allocateArray(Util.BYTE_PTR_TYPE, ar.length);
        for (int i = 0; i < ar.length; i++) {
            Pointer<Byte> s = toCString(ar[i]);
            ptr.offset(i).lvalue().set(s);
        }

        return ptr;
    }

    public default <T, E> Pointer<T> toNativeArray(LayoutType<T> type, E[] values, Function<E,T> fn) {
        if (values == null || values.length == 0) {
            return Pointer.nullPointer();
        }
        final int size = values.length;
        Pointer<T> ar = allocateArray(type, size);
        for (int i = 0; i < size; i++) {
            ar.offset(i).lvalue().set(fn.apply(values[i]));
        }
        return ar;
    }

    public default <T, E> Pointer<T> toNativeArray(LayoutType<T> type, List<E> values, Function<E,T> fn) {
        if (values == null || values.size() == 0) {
            return Pointer.nullPointer();
        }
        final int size = values.size();
        Pointer<T> ar = allocateArray(type, size);
        for (int i = 0; i < size; i++) {
            ar.offset(i).lvalue().set(fn.apply(values.get(i)));
        }
        return ar;
    }

    static Scope newNativeScope() {
        return new NativeScope();
    }

    static Scope newHeapScope() {
        return new HeapScope();
    }
}
