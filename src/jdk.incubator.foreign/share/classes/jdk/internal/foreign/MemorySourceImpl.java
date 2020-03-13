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

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.MemorySource;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Optional;

public abstract class MemorySourceImpl implements MemorySource {

    //reference to keep hold onto
    final Object ref;
    final Kind kind;
    final long size;

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
    final static int CLOSED = -1;
    final static int MAX_ACQUIRE = Integer.MAX_VALUE;

    final Runnable cleanupAction;

    public MemorySourceImpl(Kind kind, long size, Object ref, Runnable cleanupAction) {
        this.kind = kind;
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

    @Override
    public Kind kind() {
        return kind;
    }

    @Override
    public long byteSize() {
        return size;
    }

    @Override
    public void registerCleaner() {
        throw new UnsupportedOperationException();
    }

    /**
     * This method performs a full, thread-safe liveness check; can be used outside confinement thread.
     */

    @Override
    public boolean isReleased() {
        return ((int)COUNT_HANDLE.getVolatile(this)) != CLOSED;
    }

    MemoryScope acquire() {
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
        return new MemoryScope(this) {
            @Override
            void close() {
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
        if ((boolean)COUNT_HANDLE.compareAndSet(this, UNACQUIRED, CLOSED)) {
            if (cleanupAction != null) {
                cleanupAction.run();
            }
        }
    }

    public static class OfHeap<X> extends MemorySourceImpl implements MemorySource.OfHeap<X> {

        final X base;

        public OfHeap(long size, X base, Object ref, Runnable cleanupAction) {
            super(Kind.HEAP, size, ref, cleanupAction);
            this.base = base;
        }

        @Override
        @SuppressWarnings("unchecked")
        public X getObject() {
            return base;
        }

        @Override
        Object unsafeBase() {
            return base;
        }
    }

    public static class OfNative extends MemorySourceImpl implements MemorySource.OfNative {

        final long addr;

        public OfNative(long addr, long size, Object ref, Runnable cleanupAction) {
            super(Kind.NATIVE, size, ref, cleanupAction);
            this.addr = addr;
        }

        @Override
        public long address() {
            return addr;
        }

        @Override
        long unsafeAddress() {
            return addr;
        }
    }

    public static class OfMapped extends MemorySourceImpl implements MemorySource.OfMapped {

        final Path path;
        final long address;

        public OfMapped(long address, Path path, long size, Object ref, Runnable cleanupAction) {
            super(Kind.MAPPED, size, ref, cleanupAction);
            this.path = path;
            this.address = address;
        }

        @Override
        public Optional<Path> path() {
            return Optional.ofNullable(path);
        }

        @Override
        public long address() {
            return address;
        }

        @Override
        long unsafeAddress() {
            return address;
        }

        @Override
        public void force() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void force(MemorySegment segment) {
            throw new UnsupportedOperationException();
        }
    }
}
