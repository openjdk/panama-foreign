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

package jdk.internal.foreign.memory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.foreign.memory.Array;
import java.foreign.memory.LayoutType;
import java.util.Objects;

public class BoundedArray<X> implements Array<X> {

    BoundedPointer<X> pointer;
    long size;

    public BoundedArray(BoundedPointer<X> pointer, long size) {
        this.pointer = Objects.requireNonNull(pointer);
        this.size = size;
    }

    @Override
    public long length() {
        return size;
    }

    @Override
    public BoundedPointer<X> elementPointer() {
        return pointer.limit(size);
    }

    @Override
    public LayoutType<X> type() {
        return pointer.type();
    }

    @Override
    public <Z> Array<Z> cast(LayoutType<Z> type, long size) {
        BoundedPointer<Z> np = new BoundedPointer<>(type, pointer.region, pointer.offset, pointer.mode);
        return new BoundedArray<>(np, size);
    }

    public static void copyFrom(Array<?> nativeArray, Object javaArray, int size) {
        MethodHandle getter = MethodHandles.arrayElementGetter(javaArray.getClass());
        MethodHandle copier = MethodHandles.collectArguments(nativeArray.type().setter(), 1, getter);
        for (int i = 0 ; i < size ; i++) {
            try {
                copier.invoke(nativeArray.elementPointer().offset(i), javaArray, i);
            } catch (Throwable ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    public static void copyTo(Array<?> nativeArray, Object javaArray, int size) {
        MethodHandle setter = MethodHandles.arrayElementSetter(javaArray.getClass());
        MethodHandle copier = MethodHandles.filterArguments(setter, 2, nativeArray.type().getter());
        for (int i = 0 ; i < size ; i++) {
            try {
                copier.invoke(javaArray, i, nativeArray.elementPointer().offset(i));
            } catch (Throwable ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

}
