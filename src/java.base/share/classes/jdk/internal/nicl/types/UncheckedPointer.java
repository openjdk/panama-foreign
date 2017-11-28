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
package jdk.internal.nicl.types;

import java.nicl.types.LayoutType;
import java.nicl.types.MemoryRegion;
import java.nicl.types.Pointer;

public class UncheckedPointer<T> extends BoundedPointer<T> {
    public UncheckedPointer(LayoutType<T> type) {
        this(type, 0);
    }

    public UncheckedPointer(LayoutType<T> type, long offset) {
        this(type, offset, MemoryRegion.MODE_RW);
    }

    public UncheckedPointer(LayoutType<T> type, long offset, int mode) {
        this(type, BoundedMemoryRegion.EVERYTHING, offset, mode);
    }

    public UncheckedPointer(LayoutType<T> type, MemoryRegion region, long offset, int mode) {
        super(type, region, offset, mode);
    }

    @Override
    public <S> Pointer<S> cast(LayoutType<S> type) {
        return new UncheckedPointer<>(type, offset);
    }
}
