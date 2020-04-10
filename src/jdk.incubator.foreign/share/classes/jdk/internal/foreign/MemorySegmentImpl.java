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

import jdk.internal.vm.annotation.ForceInline;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * This class provides an immutable implementation for the {@code MemorySegment} interface. This class contains information
 * about the segment's spatial and temporal bounds, as well as the addressing coordinates (base + offset) which allows
 * unsafe access; each memory segment implementation is associated with an owner thread which is set at creation time.
 * Access to certain sensitive operations on the memory segment will fail with {@code IllegalStateException} if the
 * segment is either in an invalid state (e.g. it has already been closed) or if access occurs from a thread other
 * than the owner thread. See {@link MemoryScope} for more details on management of temporal bounds.
 */
public final class MemorySegmentImpl extends AbstractMemorySegment {

    final long min;
    final Object base;

    public MemorySegmentImpl(long min, Object base, long length, Thread owner, MemoryScope scope) {
        this(min, base, length,
                length > Integer.MAX_VALUE ? DEFAULT_MASK : DEFAULT_MASK | SMALL, owner, scope);
    }

    @ForceInline
    MemorySegmentImpl(long min, Object base, long length, int mask, Thread owner, MemoryScope scope) {
        super(length, mask, owner, scope);
        this.min = min;
        this.base = base;
    }

    @Override
    AbstractMemorySegment dup(long offset, long size, int mask, Thread owner, MemoryScope scope) {
        return new MemorySegmentImpl(min + offset, base, size, mask, owner, scope);
    }

    @Override
    long min() {
        return min;
    }

    @Override
    Object base() {
        return base;
    }
}
