/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.nicl.NativeTypes;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.nicl.types.Struct;

final class RuntimeSupport {
    private RuntimeSupport() {}

    // These static utility methods are invoked from generated code

    static <T> Pointer<T> buildPtr(Pointer<?> p, long offset, LayoutType<T> type) {
        return p
                .cast(NativeTypes.UINT8)
                .offset(offset)
                .cast(type);
    }

    static void copyFromArray(int[] src, Pointer<?> p, long offset, int nElems) {
        Pointer<Integer> dst = buildPtr(p, offset, NativeTypes.INT32);
        for (int i = 0; i < nElems; i++) {
            dst.offset(i).set(src[i]);
        }
    }

    static void copyToArray(Pointer<?> p, long offset, int[] dst, int nElems) {
        Pointer<Integer> src = buildPtr(p, offset, NativeTypes.INT32);
        for (int i = 0; i < nElems; i++) {
            dst[i] = src.offset(i).get();
        }
    }

    static <T> void copyFromArray(T[] src, Pointer<?> p, long offset, int nElems, LayoutType<T> type) {
        Pointer<T> dst = buildPtr(p, offset, type);
        for (int i = 0; i < nElems; i++) {
            dst.offset(i).set(src[i]);
        }
    }

    static <T> void copyToArray(Pointer<?> p, long offset, T[] dst, int nElems, LayoutType<T> type) {
        Pointer<T> src = buildPtr(p, offset, type);
        for (int i = 0; i < nElems; i++) {
            dst[i] = src.offset(i).get();
        }
    }
}
