/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

package jdk.internal.foreign;

import jdk.internal.misc.Unsafe;

import java.foreign.MemorySegment;

public class NativeScope extends MemorySegmentImpl.Scope {

    private static Unsafe unsafe = Unsafe.getUnsafe();

    // The maximum alignment supported by malloc - typically 16 on 64-bit platforms.
    private final static long MAX_ALIGN = 16;

    private final long addr;

    private NativeScope(long addr) {
        this.addr = addr;
    }

    @Override
    public Object base() {
        return null;
    }

    @Override
    public void close() {
        super.close();
        unsafe.freeMemory(addr);
    }

    public static MemorySegment of(long bytesSize, long alignmentBytes) {
        long alignedSize = bytesSize;

        if (alignmentBytes > MAX_ALIGN) {
            alignedSize = bytesSize + (alignmentBytes - 1);
        }

        long buf = unsafe.allocateMemory(alignedSize);
        long alignedBuf = alignUp(buf, alignmentBytes);
        MemorySegment segment = new MemorySegmentImpl(buf, alignedSize, 0, new NativeScope(buf));
        if (alignedBuf != buf) {
            long delta = alignedBuf - buf;
            segment = segment.resize(delta, bytesSize);
        }
        return segment;
    }

    public static long alignUp(long n, long alignment) {
        return (n + alignment - 1) & -alignment;
    }
}
