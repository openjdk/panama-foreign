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

    final long length;
    final int mask;
    final long min;
    final Object base;
    final Thread owner;
    final Object ref; //reference to keep hold onto
    final Runnable cleanupAction;

    int activeCount = UNACQUIRED;

    final static VarHandle COUNT_HANDLE;

    static {
        try {
            COUNT_HANDLE = MethodHandles.lookup().findVarHandle(MemorySegmentImpl.class, "activeCount", int.class);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    final static int UNACQUIRED = 0;
    final static int CLOSED = -1;
    final static int MAX_ACQUIRE = Integer.MAX_VALUE;

    public MemorySegmentImpl(long min, Object base, long length, Thread owner, Object ref, Runnable cleanupAction) {
        this(min, base, length, DEFAULT_MASK, owner, ref, cleanupAction);
    }

    @ForceInline
    MemorySegmentImpl(long min, Object base, long length, int mask, Thread owner, Object ref, Runnable cleanupAction) {
        this.length = length;
        this.mask = length > Integer.MAX_VALUE ? mask : (mask | SMALL);
        this.min = min;
        this.base = base;
        this.owner = owner;
        this.ref = ref;
        this.cleanupAction = cleanupAction;
    }

    // MemorySegment methods

    @Override
    public final long byteSize() {
        return length;
    }

    @Override
    public final boolean isAlive() {
        return isAliveThreadSafe();
    }

    @Override
    public Thread ownerThread() {
        return owner;
    }

    @Override
    int accessModesInternal() {
        return mask;
    }

    @Override
    long min() {
        return min;
    }

    @Override
    Object base() {
        return base;
    }

    // MemorySegmentProxy methods

    @Override
    public final void checkValidState() {
        if (owner != null && owner != Thread.currentThread()) {
            throw new IllegalStateException("Attempt to access segment outside owning thread");
        }
        checkAliveConfined();
    }

    // Helper methods

    @Override
    AbstractMemorySegment asUnconfined() {
        checkValidState();
        return new MemorySegmentImpl(min, base, length, mask, null, ref, cleanupAction);
    }

    /**
     * This method performs a full, thread-safe liveness check; can be used outside confinement thread.
     */
    final boolean isAliveThreadSafe() {
        return ((int)COUNT_HANDLE.getVolatile(this)) != CLOSED;
    }

    /**
     * This method performs a quick liveness check; must be called from the confinement thread.
     */
    final void checkAliveConfined() {
        if (activeCount == CLOSED) {
            throw new IllegalStateException("Segment is not alive");
        }
    }

    AbstractMemorySegment acquireNoCheck() {
        int value;
        do {
            value = (int)COUNT_HANDLE.getVolatile(this);
            if (value == CLOSED) {
                //segment is not alive!
                throw new IllegalStateException("Segment is not alive");
            } else if (value == MAX_ACQUIRE) {
                //overflow
                throw new IllegalStateException("Segment acquire limit exceeded");
            }
        } while (!COUNT_HANDLE.compareAndSet(this, value, value + 1));
        return new MemorySegmentImpl(min, base, length, mask, Thread.currentThread(), ref, this::release);
    }

    private void release() {
        int value;
        do {
            value = (int)COUNT_HANDLE.getVolatile(this);
            if (value <= UNACQUIRED) {
                //cannot get here - we can't close segment twice
                throw new IllegalStateException();
            }
        } while (!COUNT_HANDLE.compareAndSet(this, value, value - 1));
    }

    @Override
    void closeNoCheck() {
        if (!COUNT_HANDLE.compareAndSet(this, UNACQUIRED, CLOSED)) {
            //first check if already closed...
            checkAliveConfined();
            //...if not, then we have acquired views that are still active
            throw new IllegalStateException("Cannot close a segment that has active acquired views");
        }
        if (cleanupAction != null) {
            cleanupAction.run();
        }
    }
}
