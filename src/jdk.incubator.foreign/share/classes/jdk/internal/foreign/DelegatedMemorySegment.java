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

public class DelegatedMemorySegment extends AbstractMemorySegment {
    final AbstractMemorySegment memorySegment;
    final long offset;
    final long size;
    final int mask;

    DelegatedMemorySegment(AbstractMemorySegment memorySegment, long offset, long size, int mask) {
        this.memorySegment = memorySegment;
        this.offset = offset;
        this.size = size;
        this.mask = size > Integer.MAX_VALUE ? mask : (mask | SMALL);
    }

    @Override
    AbstractMemorySegment root() {
        return memorySegment;
    }

    @Override
    public Thread ownerThread() {
        return memorySegment.ownerThread();
    }

    @Override
    public long byteSize() {
        return size;
    }

    @Override
    public int accessModesInternal() {
        return mask;
    }

    @Override
    public final boolean isAlive() {
        return memorySegment.isAlive();
    }

    @Override
    public final void checkValidState() {
        memorySegment.checkValidState();
    }

    @Override
    AbstractMemorySegment asUnconfined() {
        return new DelegatedMemorySegment(memorySegment.asUnconfined(), offset, size, mask);
    }

    @Override
    void closeNoCheck() {
        memorySegment.closeNoCheck();
    }

    @Override
    long min() {
        return memorySegment.min() + offset;
    }

    @Override
    long offset() {
        return offset;
    }

    @Override
    Object base() {
        return memorySegment.base();
    }

    @Override
    AbstractMemorySegment acquireNoCheck() {
        return new DelegatedMemorySegment(memorySegment.acquireNoCheck(), offset, size, mask);
    }
}
