/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.foreign;

import jdk.incubator.foreign.NativeAllocationScope;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

import java.util.OptionalLong;

public class BoundedAllocationScope extends NativeAllocationScope {
    private final MemorySegment segment;
    private long sp = 0L;

    @Override
    public OptionalLong byteSize() {
        return OptionalLong.of(segment.byteSize());
    }

    @Override
    public long allocatedBytes() {
        return sp;
    }

    public BoundedAllocationScope(long size) {
        this.segment = MemorySegment.allocateNative(size);
    }

    @Override
    public MemoryAddress allocate(long bytesSize, long bytesAlignment) {
        long min = ((MemoryAddressImpl)segment.baseAddress()).unsafeGetOffset();
        long start = Utils.alignUp(min + sp, bytesAlignment) - min;
        try {
            MemorySegment slice = segment.asSlice(start, bytesSize)
                    .withAccessModes(MemorySegment.READ | MemorySegment.WRITE | MemorySegment.ACQUIRE);
            sp = start + bytesSize;
            return slice.baseAddress();
        } catch (IndexOutOfBoundsException ex) {
            throw new OutOfMemoryError("Not enough space left to allocate");
        }
    }

    @Override
    public void close() {
        segment.close();
    }
}
