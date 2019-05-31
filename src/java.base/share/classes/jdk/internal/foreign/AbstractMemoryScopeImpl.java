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

import java.foreign.MemoryScope;
import java.foreign.MemorySegment;

public abstract class AbstractMemoryScopeImpl implements MemoryScope {

    final static Unsafe U = Unsafe.getUnsafe();

    // The maximum alignment supported by malloc - typically 16 on 64-bit platforms.
    final static long MAX_ALIGN = 16;

    final static long CHARATERISTICS_MASK =
                MemoryScope.CONFINED |
                MemoryScope.IMMUTABLE |
                MemoryScope.UNALIGNED |
                MemoryScope.UNCHECKED;

    final long characteristics;

    AbstractMemoryScopeImpl(long characteristics) {
        this.characteristics = characteristics;
    }

    @Override
    public final long characteristics() {
        return characteristics;
    }

    public final void checkAlive() {
        if ((characteristics() & MemoryScope.CONFINED) != 0) {
            checkThread();
        }
        if ((characteristics() & MemoryScope.PINNED) != 0) {
            return;
        }
        if (!isAlive()) {
            throw new IllegalStateException("Scope is not alive");
        }
    }

    public final void checkTerminal() {
        checkThread();
        if ((characteristics() & PINNED) != 0) {
            throw new IllegalStateException("Terminal operations close() or merge() not supported by this scope!");
        }
    }

    @Override
    public final MemorySegment allocate(long bytesSize, long alignmentBytes) {
        checkThread();
        if (bytesSize < 0) {
            throw new IllegalArgumentException("Invalid allocation size : " + bytesSize);
        }

        if (alignmentBytes < 0 ||
            ((alignmentBytes & (alignmentBytes - 1)) != 0L)) {
            throw new IllegalArgumentException("Invalid alignment constraint : " + alignmentBytes);
        }

        return bytesSize > 0 ?
                allocateRegion(bytesSize, alignmentBytes) :
                MemorySegmentImpl.ofNothing(this);
    }

    @Override
    public MemoryScope fork(long characteristics) {
        checkThread();
        if ((characteristics & ~CHARATERISTICS_MASK) != 0) {
            throw new IllegalArgumentException("Invalid charateristics mask");
        }
        return new ConfinedMemoryScopeImpl(this, characteristics);
    }

    abstract void checkThread();

    abstract MemorySegment allocateRegion(long bytesSize, long alignmentBytes);

    public static long alignUp(long n, long alignment) {
        return (n + alignment - 1) & ~(alignment - 1);
    }
}
