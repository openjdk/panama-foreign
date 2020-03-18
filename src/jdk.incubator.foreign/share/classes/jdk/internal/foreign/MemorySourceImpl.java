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

import jdk.incubator.foreign.MappedMemorySource;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.MemorySource;
import jdk.internal.access.foreign.UnmapperProxy;
import jdk.internal.ref.CleanerFactory;
import jdk.internal.ref.PhantomCleanable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Implementation of a memory source. A memory source acts as a shared, <am>atomic</am> reference counter for all the memory segments
 * which are derived from it. The counter can be incremented (upon calling the {@link #acquire()} method),
 * and is decremented (when a previously acquired memory scope is later closed).
 */
public abstract class MemorySourceImpl implements MemorySource {

    //reference to keep hold onto
    final Object ref;
    final long size;
    volatile PhantomCleanable<?> cleaneable;

    int activeCount = UNACQUIRED;

    final static VarHandle COUNT_HANDLE;

    static {
        try {
            COUNT_HANDLE = MethodHandles.lookup().findVarHandle(MemorySourceImpl.class, "activeCount", int.class);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    final static int UNACQUIRED = 0;
    final static int RELEASED = -1;
    final static int MAX_ACQUIRE = Integer.MAX_VALUE;

    final Runnable cleanupAction;

    public MemorySourceImpl(long size, Object ref, Runnable cleanupAction) {
        this.size = size;
        this.ref = ref;
        this.cleanupAction = cleanupAction;
    }

    Object unsafeBase() {
        return null;
    }

    long unsafeAddress() {
        return 0L;
    }

    UnmapperProxy unmapper() {
        return null;
    }

    @Override
    public long byteSize() {
        return size;
    }

    @Override
    public void registerCleaner() {
        if (cleanupAction != null) {
            MemoryScope scope = acquire();
            try {
                //Note: if we are here nobody else could be attempting to call the cleanupAction in release()
                synchronized (this) {
                    if (cleaneable == null) {
                        cleaneable = (PhantomCleanable<?>) CleanerFactory.cleaner().register(this, cleanupAction);
                    }
                }
            } finally {
                scope.close();
            }
        }
    }

    /**
     * This method performs a full, thread-safe liveness check; can be used outside confinement thread.
     */

    @Override
    public boolean isReleased() {
        return ((int)COUNT_HANDLE.getVolatile(this)) == RELEASED;
    }

    public MemoryScope acquire() {
        int value;
        do {
            value = (int)COUNT_HANDLE.getVolatile(this);
            if (value == RELEASED) {
                //segment is not alive!
                throw new IllegalStateException("Segment is not alive");
            } else if (value == MAX_ACQUIRE) {
                //overflow
                throw new IllegalStateException("Segment acquire limit exceeded");
            }
        } while (!COUNT_HANDLE.compareAndSet(this, value, value + 1));
        return new MemoryScope(this) {
            @Override
            public void close() {
                super.close();
                source.release();
            }
        };
    }

    void release() {
        int value;
        do {
            value = (int)COUNT_HANDLE.getVolatile(this);
            if (value <= UNACQUIRED) {
                //cannot get here - we can't close segment twice
                throw new IllegalStateException();
            }
        } while (!COUNT_HANDLE.compareAndSet(this, value, value - 1));
        //auto-close
        if ((boolean)COUNT_HANDLE.compareAndSet(this, UNACQUIRED, RELEASED)) {
            // Note: if we are here it means that nobody else could be in the middle of a registerCleaner
            if (cleanupAction != null) {
                cleanupAction.run();
                if (cleaneable != null) {
                    // if we are here, we are explicitly releasing (e.g. close()), so we need to unregister the cleaneable
                    cleaneable.clear();
                }
            }
        }
    }

    public static class OfHeap extends MemorySourceImpl {

        final Object base;

        public OfHeap(long size, Object base, Object ref, Runnable cleanupAction) {
            super(size, ref, cleanupAction);
            this.base = base;
        }

        @Override
        Object unsafeBase() {
            return base;
        }
    }

    public static class OfNative extends MemorySourceImpl {

        final long addr;

        public OfNative(long addr, long size, Object ref, Runnable cleanupAction) {
            super(size, ref, cleanupAction);
            this.addr = addr;
        }

        @Override
        long unsafeAddress() {
            return addr;
        }
    }

    public static class OfMapped extends MemorySourceImpl implements MappedMemorySource {

        final UnmapperProxy unmapperProxy;

        public OfMapped(UnmapperProxy unmapperProxy, long size, Object ref, Runnable cleanupAction) {
            super(size, ref, cleanupAction);
            this.unmapperProxy = unmapperProxy;
        }

        @Override
        long unsafeAddress() {
            return unmapperProxy.address();
        }

        @Override
        public void force() {
            try (MemorySegment segment = new MemorySegmentImpl(0L, size, Thread.currentThread(), acquire())) {
                force(segment);
            }
        }

        @Override
        public void force(MemorySegment segment) {
            if (segment.source() instanceof MappedMemorySource) {
                ((MappedByteBuffer)segment.asByteBuffer()).force();
            } else {
                throw new IllegalArgumentException("Not a mapped memory segment");
            }
        }
    }
}
