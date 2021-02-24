/*
 *  Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.ResourceScope;
import jdk.internal.misc.ScopedMemoryAccess;
import jdk.internal.vm.annotation.ForceInline;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class manages the temporal bounds associated with a memory segment as well
 * as thread confinement. A scope has a liveness bit, which is updated when the scope is closed
 * (this operation is triggered by {@link ResourceScope#close()}). This bit is consulted prior
 * to memory access (see {@link #checkValidState()}).
 * There are two kinds of memory scope: confined memory scope and shared memory scope.
 * A confined memory scope has an associated owner thread that confines some operations to
 * associated owner thread such as {@link #close()} or {@link #checkValidState()}.
 * Shared scopes do not feature an owner thread - meaning their operations can be called, in a racy
 * manner, by multiple threads. To guarantee temporal safety in the presence of concurrent thread,
 * shared scopes use a more sophisticated synchronization mechanism, which guarantees that no concurrent
 * access is possible when a scope is being closed (see {@link jdk.internal.misc.ScopedMemoryAccess}).
 */
public abstract class MemoryScope implements ResourceScope, ScopedMemoryAccess.Scope {

    final ResourceList resourceList;
    final boolean closeable;

    @Override
    public void addOnClose(Runnable runnable) {
        Objects.requireNonNull(runnable);
        addInternal(ResourceList.ResourceCleanup.ofRunnable(runnable));
    }

    /**
     * Add a cleanup action. If a failure occurred (because of a add vs. close race), call the cleanup action.
     * This semantics is useful when allocating new memory segments, since we first do a malloc/mmap and _then_
     * we register the cleanup (free/munmap) against the scope; so, if registration fails, we still have to
     * cleanup memory. From the perspective of the client, such a failure would manifest as a factory
     * returning a segment that is already "closed" - which is always possible anyway (e.g. if the scope
     * is closed _after_ the cleanup for the segment is registered but _before_ the factory returns the
     * new segment to the client). For this reason, it's not worth adding extra complexity to the segment
     * initialization logic here - and using an optimistic logic works well in practice.
     */
    public void addOrCleanupIfFail(ResourceList.ResourceCleanup resource) {
        try {
            addInternal(resource);
        } catch (Throwable ex) {
            resource.cleanup();
        }
    }

    final void addInternal(ResourceList.ResourceCleanup resource) {
        try {
            checkValidStateSlow();
            resourceList.add(resource);
        } catch (ScopedMemoryAccess.Scope.ScopedAccessError err) {
            throw new IllegalStateException("Already closed");
        }
    }

    protected MemoryScope(Object ref, Cleaner cleaner, boolean closeable, ResourceList resourceList) {
        this.ref = ref;
        this.resourceList = resourceList;
        this.closeable = closeable;
        if (cleaner != null) {
            Runnable r = resourceList::cleanup;
            cleaner.register(this, r);
        }
    }

    public static MemoryScope createConfined(Thread thread, Object ref, Cleaner cleaner, boolean closeable) {
        return new ConfinedScope(thread, ref, cleaner, closeable);
    }

    /**
     * Creates a confined memory scope with given attachment and cleanup action. The returned scope
     * is assumed to be confined on the current thread.
     * @param ref           an optional reference to an instance that needs to be kept reachable
     * @return a confined memory scope
     */
    public static MemoryScope createConfined(Object ref, Cleaner cleaner, boolean closeable) {
        return new ConfinedScope(Thread.currentThread(), ref, cleaner, closeable);
    }

    /**
     * Creates a shared memory scope with given attachment and cleanup action.
     * @param ref           an optional reference to an instance that needs to be kept reachable
     * @return a shared memory scope
     */
    public static MemoryScope createShared(Object ref, Cleaner cleaner, boolean closeable) {
        return new SharedScope(ref, cleaner, closeable);
    }

    protected final Object ref;

    /**
     * Closes this scope, executing any cleanup action (where provided).
     * @throws IllegalStateException if this scope is already closed or if this is
     * a confined scope and this method is called outside of the owner thread.
     */
    public final void close() {
        if (!closeable) {
            throw new IllegalStateException("Scope is not closeable");
        }
        try {
            justClose();
            resourceList.cleanup();
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    @Override
    public boolean isCloseable() {
        return closeable;
    }

    abstract void release();

    abstract void justClose();

    /**
     * Returns "owner" thread of this scope.
     * @return owner thread (or null for a shared scope)
     */
    public abstract Thread ownerThread();

    /**
     * Returns true, if this scope is still alive. This method may be called in any thread.
     * @return {@code true} if this scope is not closed yet.
     */
    public abstract boolean isAlive();


    /**
     * This is a faster version of {@link #checkValidStateSlow()}, which is called upon memory access, and which
     * relies on invariants associated with the memory scope implementations (typically, volatile access
     * to the closed state bit is replaced with plain access, and ownership check is removed where not needed.
     * Should be used with care.
     */
    public abstract void checkValidState();

    /**
     * Checks that this scope is still alive (see {@link #isAlive()}).
     * @throws IllegalStateException if this scope is already closed or if this is
     * a confined scope and this method is called outside of the owner thread.
     */
    public final void checkValidStateSlow() {
        if (ownerThread() != null && Thread.currentThread() != ownerThread()) {
            throw new IllegalStateException("Attempted access outside owning thread");
        } else if (!isAlive()) {
            throw new IllegalStateException("Already closed");
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * A confined scope, which features an owner thread. The liveness check features an additional
     * confinement check - that is, calling any operation on this scope from a thread other than the
     * owner thread will result in an exception. Because of this restriction, checking the liveness bit
     * can be performed in plain mode.
     */
    static class ConfinedScope extends MemoryScope {

        private boolean closed; // = false
        int forkedCount = 0;
        final Thread owner;

        public ConfinedScope(Thread owner, Object ref, Cleaner cleaner, boolean closeable) {
            super(ref, cleaner, closeable, new ResourceList.ConfinedResourceList());
            this.owner = owner;
        }

        @ForceInline
        public final void checkValidState() {
            if (owner != Thread.currentThread()) {
                throw new IllegalStateException("Attempted access outside owning thread");
            }
            if (closed) {
                throw new IllegalStateException("Already closed");
            }
        }

        @Override
        public boolean isAlive() {
            return !closed;
        }

        @Override
        public Lock lock() {
            checkValidState();
            forkedCount++;
            return new ConfinedLock(this);
        }

        @Override
        void release() {
            forkedCount--;
        }

        void justClose() {
            this.checkValidState();
            if (forkedCount == 0) {
                closed = true;
            } else {
                throw new IllegalStateException("Cannot close a scope which has active forks");
            }
        }

        @Override
        public Thread ownerThread() {
            return owner;
        }

        static class ConfinedLock extends LockImpl {
            boolean released = false;

            ConfinedLock(MemoryScope scope) {
                super(scope);
            }

            @Override
            public void close() {
                scope.checkValidState(); // thread check
                if (!released) {
                    scope.release();
                } else {
                    throw new IllegalStateException("Already released");
                }
            }
        }
    }

    /**
     * A shared scope, which can be shared across multiple threads. Closing a shared scope has to ensure that
     * (i) only one thread can successfully close a scope (e.g. in a close vs. close race) and that
     * (ii) no other thread is accessing the memory associated with this scope while the segment is being
     * closed. To ensure the former condition, a CAS is performed on the liveness bit. Ensuring the latter
     * is trickier, and require a complex synchronization protocol (see {@link jdk.internal.misc.ScopedMemoryAccess}).
     * Since it is the responsibility of the closing thread to make sure that no concurrent access is possible,
     * checking the liveness bit upon access can be performed in plain mode, as in the confined case.
     */
    static class SharedScope extends MemoryScope {

        static ScopedMemoryAccess SCOPED_MEMORY_ACCESS = ScopedMemoryAccess.getScopedMemoryAccess();

        final static int ALIVE = 0;
        final static int CLOSING = -1;
        final static int CLOSED = -2;
        final static int MAX_FORKS = Integer.MAX_VALUE;

        int state = ALIVE;

        private static final VarHandle STATE;

        static {
            try {
                STATE = MethodHandles.lookup().findVarHandle(SharedScope.class, "state", int.class);
            } catch (Throwable ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }

        SharedScope(Object ref, Cleaner cleaner, boolean closeable) {
            super(ref, cleaner, closeable, new ResourceList.SharedResourceList());
        }

        @Override
        public Thread ownerThread() {
            return null;
        }

        @Override
        public void checkValidState() {
            if (state < ALIVE) {
                throw ScopedAccessError.INSTANCE;
            }
        }

        @Override
        public Lock lock() {
            int value;
            do {
                value = (int)STATE.getVolatile(this);
                if (value < ALIVE) {
                    //segment is not alive!
                    throw new IllegalStateException("Already closed");
                } else if (value == MAX_FORKS) {
                    //overflow
                    throw new IllegalStateException("Segment acquire limit exceeded");
                }
            } while (!STATE.compareAndSet(this, value, value + 1));
            return new SharedLock(this);
        }

        void release() {
            int value;
            do {
                value = (int)STATE.getVolatile(this);
                if (value <= ALIVE) {
                    //cannot get here - we can't close segment twice
                    throw new IllegalStateException("Already closed");
                }
            } while (!STATE.compareAndSet(this, value, value - 1));
        }

        void justClose() {
            int prevState = (int)STATE.compareAndExchange(this, ALIVE, CLOSING);
            if (prevState < 0) {
                throw new IllegalStateException("Already closed");
            } else if (prevState != ALIVE) {
                throw new IllegalStateException("Cannot close a scope which has active forks");
            }
            boolean success = SCOPED_MEMORY_ACCESS.closeScope(this);
            STATE.setVolatile(this, success ? CLOSED : ALIVE);
            if (!success) {
                throw new IllegalStateException("Cannot close while another thread is accessing the segment");
            }
        }

        @Override
        public boolean isAlive() {
            return (int)STATE.getVolatile(this) != CLOSED;
        }

        static class SharedLock extends LockImpl {
            AtomicBoolean released = new AtomicBoolean(false);

            SharedLock(MemoryScope scope) {
                super(scope);
            }

            @Override
            public void close() {
                if (released.compareAndSet(false, true)) {
                    scope.release();
                } else {
                    throw new IllegalStateException("Already released");
                }
            }
        }
    }

    static abstract class LockImpl implements Lock {
        final MemoryScope scope;

        LockImpl(MemoryScope scope) {
            this.scope = scope;
        }

        @Override
        public ResourceScope scope() {
            return scope;
        }
    }

    public static MemoryScope GLOBAL = new MemoryScope( null, null, false, null) {

        @Override
        public void addOnClose(Runnable runnable) {
            // do nothing
        }

        @Override
        public void addOrCleanupIfFail(ResourceList.ResourceCleanup resource) {
            // do nothing
        }

        @Override
        void justClose() {
            // do nothing
        }

        @Override
        public Thread ownerThread() {
            return null;
        }

        @Override
        public boolean isAlive() {
            return true;
        }

        @Override
        public void checkValidState() {
            // do nothing
        }

        @Override
        public Lock lock() {
            return GLOBAL_LOCK;
        }

        @Override
        void release() {
            // do nothing
        }

        final Lock GLOBAL_LOCK = new Lock() {
            @Override
            public ResourceScope scope() {
                return GLOBAL;
            }

            @Override
            public void close() {
                // do nothing
            }
        };
    };

}
