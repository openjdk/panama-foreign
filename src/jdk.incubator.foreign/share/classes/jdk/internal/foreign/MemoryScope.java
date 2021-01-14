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

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.internal.misc.ScopedMemoryAccess;
import jdk.internal.ref.PhantomCleanable;
import jdk.internal.vm.annotation.ForceInline;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.util.Objects;

/**
 * This class manages the temporal bounds associated with a memory segment as well
 * as thread confinement. A scope has a liveness bit, which is updated when the scope is closed
 * (this operation is triggered by {@link AbstractMemorySegmentImpl#close()}). This bit is consulted prior
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

    static final Cleaner.Cleanable DUMMY_CLEANUP_ACTION = () -> { };

    Resource fst;
    final Cleaner cleaner;
    public abstract void add(Resource resource);

    static final VarHandle FST;

    static {
        try {
            FST = MethodHandles.lookup().findVarHandle(MemoryScope.class, "fst", Resource.class);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError();
        }
    }

    private MemoryScope(Object ref, Cleaner cleaner) {
        this.ref = ref;
        this.cleaner = cleaner;
    }

    public static MemoryScope createConfined(Thread thread, Object ref, Cleaner cleaner) {
        return new ConfinedScope(thread, ref, cleaner);
    }

    /**
     * Creates a confined memory scope with given attachment and cleanup action. The returned scope
     * is assumed to be confined on the current thread.
     * @param ref           an optional reference to an instance that needs to be kept reachable
     * @param cleanupAction a cleanup action to be executed when returned scope is closed
     * @return a confined memory scope
     */
    public static MemoryScope createConfined(Object ref, Cleaner cleaner) {
        return new ConfinedScope(Thread.currentThread(), ref, cleaner);
    }

    /**
     * Creates a shared memory scope with given attachment and cleanup action.
     * @param ref           an optional reference to an instance that needs to be kept reachable
     * @param cleanupAction a cleanup action to be executed when returned scope is closed
     * @return a shared memory scope
     */
    public static MemoryScope createShared(Object ref, Cleaner cleaner) {
        return new SharedScope(ref, cleaner);
    }

    protected final Object ref;

    /**
     * Closes this scope, executing any cleanup action (where provided).
     * @throws IllegalStateException if this scope is already closed or if this is
     * a confined scope and this method is called outside of the owner thread.
     */
    public final void close() {
        try {
            Resource current = justClose();
            //ok from now on, no more adds will be valid, so let's walk the chain
            while (current != null) {
                current.cleanup();
                current = current.next();
            }
        } finally {
            Reference.reachabilityFence(this);
        }
    }

    abstract Resource justClose();

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
     * Checks that this scope is still alive (see {@link #isAlive()}).
     * @throws IllegalStateException if this scope is already closed or if this is
     * a confined scope and this method is called outside of the owner thread.
     */
    public abstract void checkValidState();

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * A confined scope, which features an owner thread. The liveness check features an additional
     * confinement check - that is, calling any operation on this scope from a thread other than the
     * owner thread will result in an exception. Because of this restriction, checking the liveness bit
     * can be performed in plain mode (see {@link #checkAliveRaw(MemoryScope)}).
     */
    static class ConfinedScope extends MemoryScope {

        private boolean closed; // = false
        final Thread owner;

        public ConfinedScope(Thread owner, Object ref, Cleaner cleaner) {
            super(ref, cleaner);
            this.owner = owner;
        }

        @ForceInline
        public final void checkValidState() {
            if (owner != Thread.currentThread()) {
                throw new IllegalStateException("Attempted access outside owning thread");
            }
            if (closed) {
                throw ScopedAccessError.INSTANCE;
            }
        }

        @Override
        public boolean isAlive() {
            return !closed;
        }

        @Override
        public void add(Resource resource) {
            checkValidState();
            resource.setNext(fst);
            fst = resource;
        }

        Resource justClose() {
            checkValidState();
            closed = true;
            return fst;
        }

        @Override
        public Thread ownerThread() {
            return owner;
        }
    }

    /**
     * A shared scope, which can be shared across multiple threads. Closing a shared scope has to ensure that
     * (i) only one thread can successfully close a scope (e.g. in a close vs. close race) and that
     * (ii) no other thread is accessing the memory associated with this scope while the segment is being
     * closed. To ensure the former condition, a CAS is performed on the liveness bit. Ensuring the latter
     * is trickier, and require a complex synchronization protocol (see {@link jdk.internal.misc.ScopedMemoryAccess}).
     * Since it is the responsibility of the closing thread to make sure that no concurrent access is possible,
     * checking the liveness bit upon access can be performed in plain mode (see {@link #checkAliveRaw(MemoryScope)}),
     * as in the confined case.
     */
    static class SharedScope extends MemoryScope {

        static ScopedMemoryAccess SCOPED_MEMORY_ACCESS = ScopedMemoryAccess.getScopedMemoryAccess();

        final static int ALIVE = 0;
        final static int CLOSING = 1;
        final static int CLOSED = 2;

        int state = ALIVE;

        private static final VarHandle STATE;

        static {
            try {
                STATE = MethodHandles.lookup().findVarHandle(SharedScope.class, "state", int.class);
            } catch (Throwable ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }

        SharedScope(Object ref, Cleaner cleaner) {
            super(ref, cleaner);
        }

        @Override
        public Thread ownerThread() {
            return null;
        }

        @Override
        public void checkValidState() {
            if (state != ALIVE) {
                throw ScopedAccessError.INSTANCE;
            }
        }

        @Override
        public void add(Resource segment) {
            while (true) {
                Resource prev = (Resource)FST.getAcquire(this);
                segment.setNext(prev);
                Resource newSegment = (Resource)FST.compareAndExchangeRelease(this, prev, segment);
                if (newSegment == DUMMY) {
                    // too late
                    segment.cleanup();
                    throw new IllegalStateException("Already closed");
                } else if (newSegment == prev) {
                    return; //victory
                }
                // keep trying
            }
        }

        Resource justClose() {
            if (!STATE.compareAndSet(this, ALIVE, CLOSING)) {
                throw new IllegalStateException("Already closed");
            }
            boolean success = SCOPED_MEMORY_ACCESS.closeScope(this);
            STATE.setVolatile(this, success ? CLOSED : ALIVE);
            if (!success) {
                throw new IllegalStateException("Cannot close while another thread is accessing the segment");
            }
            //ok now we're really closing down
            Resource prev = null;
            while (true) {
                prev = (Resource)FST.getAcquire(this);
                // no need to check for DUMMY, since only one thread can get here!
                if (FST.weakCompareAndSetRelease(this, prev, DUMMY)) {
                    break;
                }
            }
            return prev;
        }

        @Override
        public boolean isAlive() {
            return (int)STATE.getVolatile(this) != CLOSED;
        }
    }

    static class ScopeCleanable extends PhantomCleanable<MemoryScope> {
        final Cleaner.Cleanable cleanupAction;

        public ScopeCleanable(MemoryScope referent, Cleaner.Cleanable cleanupAction) {
            super(referent, referent.cleaner);
            this.cleanupAction = cleanupAction;
        }

        @Override
        protected void performCleanup() {
            cleanupAction.clean();
        }
    }

    static final Resource DUMMY = new Resource() {
        @Override
        public ResourceScope scope() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Resource next() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setNext(Resource next) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cleanup() {
            throw new UnsupportedOperationException();
        }
    };

    public static MemoryScope PRIMORDIAL = new MemoryScope(null, null) {
        @Override
        public void add(Resource resource) {
            // do nothing
        }

        @Override
        Resource justClose() {
            throw new UnsupportedOperationException("Cannot close primordial scope");
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
    };
}
