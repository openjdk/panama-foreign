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

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.LongFunction;

public class UnboundedAllocationScope extends NativeAllocationScope {

    private static final long BLOCK_SIZE = 4 * 1024;
    private static final long MAX_ALLOC_SIZE = BLOCK_SIZE / 2;

    private final List<MemorySegment> usedSegments = new ArrayList<>();
    private MemorySegment segment;
    private long sp = 0L;
    private long size = 0L;

    @Override
    public OptionalLong byteSize() {
        return OptionalLong.empty();
    }

    @Override
    public long allocatedBytes() {
        return size;
    }

    public UnboundedAllocationScope() {
        this.segment = MemorySegment.allocateNative(BLOCK_SIZE);
    }

    @Override
    public MemoryAddress allocate(long bytesSize, long bytesAlignment) {
        if (bytesSize > MAX_ALLOC_SIZE) {
            MemorySegment segment = MemorySegment.allocateNative(bytesSize, bytesAlignment);
            usedSegments.add(segment);
            return segment.withAccessModes(MemorySegment.READ | MemorySegment.WRITE | MemorySegment.ACQUIRE)
                    .baseAddress();
        }
        for (int i = 0; i < 2; i++) {
            long min = ((MemoryAddressImpl) segment.baseAddress()).unsafeGetOffset();
            long start = Utils.alignUp(min + sp, bytesAlignment) - min;
            try {
                MemorySegment slice = segment.asSlice(start, bytesSize)
                        .withAccessModes(MemorySegment.READ | MemorySegment.WRITE | MemorySegment.ACQUIRE);
                sp = start + bytesSize;
                size += Utils.alignUp(bytesSize, bytesAlignment);
                return slice.baseAddress();
            } catch (IndexOutOfBoundsException ex) {
                sp = 0L;
                usedSegments.add(segment);
                segment = MemorySegment.allocateNative(BLOCK_SIZE);
            }
        }
        throw new AssertionError("Cannot get here!");
    }

    @Override
    public void close() {
        segment.close();
        usedSegments.forEach(MemorySegment::close);
    }
}
