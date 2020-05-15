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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.StampedLock;

/**
 * This class manages the temporal bounds associated with a memory segment. A scope has a liveness bit, which is updated
 * when the scope is closed (this operation is triggered by {@link AbstractMemorySegmentImpl#close()}). Furthermore,
 * a scope is either root scope ({@link #create(Object, Runnable) created} when memory segment is allocated) or child scope
 * ({@link #acquire() acquired} from root scope). When a child scope is acquired from another child scope, it is actually
 * acquired from the root scope. There is only a single level of children. All child scopes are peers.
 * A child scope can be {@link #close() closed} at any time, but root scope can only be closed after all its children
 * have been closed, at which time any associated cleanup action is executed (the associated memory segment is freed).
 */
abstract class MemoryScope {
    public static final MemoryScope GLOBAL = new Root(null, null);

    /**
     * Creates a root MemoryScope with given ref and cleanupAction.
     * The returned instance may be published unsafely to and used in any thread, but methods that explicitly state that
     * they may only be called in "owner" thread, must strictly be called in single thread that has been selected to be the
     * "owner" thread.
     *
     * @param ref           an optional reference to an instance that needs to be kept reachable
     * @param cleanupAction an optional cleanup action to be executed when returned scope is closed
     * @return a root MemoryScope
     */
    static MemoryScope create(Object ref, Runnable cleanupAction) {
        return new Root(ref, cleanupAction);
    }

    boolean closed = false;
    private static final VarHandle CLOSED;

    static {
        try {
            CLOSED = MethodHandles.lookup().findVarHandle(MemoryScope.class, "closed", boolean.class);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private MemoryScope() {
    }

    /**
     * Acquires a child scope (or peer scope if this is a child).
     * This method may be called in any thread.
     * The returned instance may be published unsafely to and used in any thread, but methods that explicitly state that
     * they may only be called in "owner" thread, must strictly be called in single thread that has been selected to be the
     * "owner" thread.
     *
     * @return a child (or peer) scope
     * @throws IllegalStateException if root scope is already closed
     */
    abstract MemoryScope acquire();

    /**
     * Closes this scope, executing any cleanup action if this is the root scope.
     * This method may only be called in "owner" thread.
     *
     * @throws IllegalStateException if this scope is already closed or if this is
     *                               a root scope and there is/are still active child
     *                               scope(s).
     */
    abstract void close();

    /**
     * Duplicates this scope and {@link #close() closes} it. If this is a root scope,
     * new root scope is returned. If this is a child scope, new child scope is returned.
     * This method may only be called in "owner" thread.
     * The returned instance may be published unsafely to and used in any thread, but methods that explicitly state that
     * they may only be called in "owner" thread, must strictly be called in single thread that has been selected to be the
     * "owner" thread.
     *
     * @return a duplicate of this scope
     * @throws IllegalStateException if this scope is already closed or if this is
     *                               a root scope and there is/are still active child
     *                               scope(s).
     */
    abstract MemoryScope dup();

    /**
     * This method may be called in any thread.
     *
     * @return {@code true} if this scope is not closed yet.
     */
    final boolean isAliveThreadSafe() {
        return !((boolean)CLOSED.getVolatile(this));
    }

    /**
     * Checks that this scope is still alive.
     * This method may only be called in "owner" thread.
     *
     * @throws IllegalStateException if this scope is already closed
     */
    final void checkAliveConfined() {
        if (closed) {
            throw new IllegalStateException("This scope is already closed");
        }
    }

    private static final class Root extends MemoryScope {
        private final LongAdder acquired = new LongAdder();
        private final Object ref;
        private final Runnable cleanupAction;

        private final StampedLock lock = new StampedLock();



        private Root(Object ref, Runnable cleanupAction) {
            this.ref = ref;
            this.cleanupAction = cleanupAction;
        }

        @Override
        MemoryScope acquire() {
            //try to optimistically acquire the lock
            long stamp = lock.tryOptimisticRead();
            try {
                for (; ; stamp = lock.readLock()) {
                    if (stamp == 0L)
                        continue;
                    checkAliveConfined(); // plain read is enough here (either successful optimistic read, or full read lock)

                    // increment acquires
                    acquired.increment();
                    // did a call to close() occur since we acquired the lock?
                    if (lock.validate(stamp)) {
                        // no, just return the acquired scope
                        return new Child();
                    } else {
                        // yes, just back off and retry (close might have failed, after all)
                        acquired.decrement();
                    }
                }
            } finally {
                if (StampedLock.isReadLockStamp(stamp))
                    lock.unlockRead(stamp);
            }
        }

        @Override
        MemoryScope dup() { // always called in owner thread
            return closeOrDup(false);
        }

        @Override
        void close() { // always called in owner thread
            closeOrDup(true);
        }

        private MemoryScope closeOrDup(boolean close) {
            // pre-allocate duped scope so we don't get OOME later and be left with this scope closed
            var duped = close ? null : new Root(ref, cleanupAction);
            // enter critical section - no acquires are possible past this point
            long stamp = lock.writeLock();
            try {
                checkAliveConfined(); // plain read is enough here (full write lock)
                // check for absence of active acquired children
                if (acquired.sum() > 0) {
                    throw new IllegalStateException("Cannot close this scope as it has active acquired children");
                }
                // now that we made sure there's no active acquired children, we can mark scope as closed
                closed = true; // plain write is enough here (full write lock)
            } finally {
                // leave critical section
                lock.unlockWrite(stamp);
            }

            // do close or dup
            if (close) {
                if (cleanupAction != null) {
                    cleanupAction.run();
                }
                return null;
            } else {
                return duped;
            }
        }

        private final class Child extends MemoryScope {

            private Child() {
            }

            @Override
            MemoryScope acquire() {
                return Root.this.acquire();
            }

            @Override
            MemoryScope dup() { // always called in owner thread
                checkAliveConfined();
                // pre-allocate duped scope so we don't get OOME later and be left with this scope closed
                var duped = new Child();
                CLOSED.setVolatile(this, true);
                return duped;
            }

            @Override
            void close() { // always called in owner thread
                checkAliveConfined();
                closed = true;
                // following acts as a volatile write after plain write above so
                // plain write gets flushed too (which is important for isAliveThreadSafe())
                Root.this.acquired.decrement();
            }
        }
    }
}