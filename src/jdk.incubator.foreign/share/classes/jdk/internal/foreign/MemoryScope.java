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
import jdk.incubator.foreign.SegmentAllocator;
import jdk.internal.misc.ScopedMemoryAccess;
import jdk.internal.ref.CleanerFactory;
import jdk.internal.vm.annotation.Stable;

import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.util.Objects;

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
public abstract class MemoryScope implements ResourceScope, ScopedMemoryAccess.Scope, SegmentAllocator {

    final ResourceList resourceList;
    protected final Object ref;

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

    void addInternal(ResourceList.ResourceCleanup resource) {
        try {
            checkValidStateSlow();
            resourceList.add(resource);
        } catch (ScopedMemoryAccess.Scope.ScopedAccessError err) {
            throw new IllegalStateException("Already closed");
        }
    }

    protected MemoryScope(Object ref, Cleaner cleaner, ResourceList resourceList) {
        this.ref = ref;
        this.resourceList = resourceList;
        if (cleaner != null) {
            cleaner.register(this, resourceList);
        }
    }

    public static MemoryScope createDefault() {
        return new NonCloseableSharedScope(null, CleanerFactory.cleaner());
    }

    public static MemoryScope createConfined(Thread thread, Object ref, Cleaner cleaner) {
        return new ConfinedScope(thread, ref, cleaner);
    }

    /**
     * Creates a confined memory scope with given attachment and cleanup action. The returned scope
     * is assumed to be confined on the current thread.
     * @param ref           an optional reference to an instance that needs to be kept reachable
     * @return a confined memory scope
     */
    public static MemoryScope createConfined(Object ref, Cleaner cleaner) {
        return new ConfinedScope(Thread.currentThread(), ref, cleaner);
    }

    /**
     * Creates a shared memory scope with given attachment and cleanup action.
     * @param ref           an optional reference to an instance that needs to be kept reachable
     * @return a shared memory scope
     */
    public static MemoryScope createShared(Object ref, Cleaner cleaner) {
        return new SharedScope(ref, cleaner);
    }

    /**
     * Closes this scope, executing any cleanup action (where provided).
     * @throws IllegalStateException if this scope is already closed or if this is
     * a confined scope and this method is called outside of the owner thread.
     */
    public void close() {
        try {
            justClose();
            resourceList.cleanup();
        } finally {
            Reference.reachabilityFence(this);
        }
    }

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
     * Allocates a segment using this scope. Used by {@link SegmentAllocator#scoped(ResourceScope)}.
     */
    @Override
    public MemorySegment allocate(long bytesSize, long bytesAlignment) {
        return MemorySegment.allocateNative(bytesSize, bytesAlignment, this);
    }

    /**
     * A non-closeable, shared scope. Similar to a shared scope, but its {@link #close()} method throws unconditionally.
     * In addition, non-closeable scopes feature a much simpler scheme for generating resource scope handles, where
     * the same per-scope handle is shared across multiple calls to {@link #acquire()}. In fact, for non-closeable
     * scopes, it is sufficient for resource scope handles to keep a strong reference to their scopes, to prevent closure.
     */
    static class NonCloseableSharedScope extends SharedScope {

        @Stable
        private Handle handle;

        public NonCloseableSharedScope(Object ref, Cleaner cleaner) {
            super(ref, cleaner);
        }

        @Override
        public Handle acquire() {
            if (handle != null) {
                // capture 'this'
                handle = () -> Reference.reachabilityFence(NonCloseableSharedScope.this);
            }
            return handle;
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("Scope cannot be closed");
        }
    }

    /**
     * The global, always alive, non-closeable, shared scope. This is like a {@link NonCloseableSharedScope non-closeable scope},
     * except that the operation which adds new resources to the global scope does nothing: as the scope can never
     * become not-alive, there is nothing to track.
     */
    public static MemoryScope GLOBAL = new NonCloseableSharedScope( null, null) {
        @Override
        void addInternal(ResourceList.ResourceCleanup resource) {
            // do nothing
        }
    };

    /**
     * A list of all cleanup actions associated with a resource scope. Cleanup actions are modelled as instances
     * of the {@link ResourceCleanup} class, and, together, form a linked list. Depending on whether a scope
     * is shared or confined, different implementations of this class will be used, see {@link ConfinedScope.ConfinedResourceList}
     * and {@link SharedScope.SharedResourceList}.
     */
    public abstract static class ResourceList implements Runnable {
        ResourceCleanup fst;

        abstract void add(ResourceCleanup cleanup);

        abstract void cleanup();

        public final void run() {
            cleanup(); // cleaner interop
        }

        static void cleanup(ResourceCleanup first) {
            ResourceCleanup current = first;
            while (current != null) {
                current.cleanup();
                current = current.next;
            }
        }

        public static abstract class ResourceCleanup {
            ResourceCleanup next;

            public abstract void cleanup();

            final static ResourceCleanup CLOSED_LIST = new ResourceCleanup() {
                @Override
                public void cleanup() {
                    throw new IllegalStateException("This resource list has already been closed!");
                }
            };

            static ResourceCleanup ofRunnable(Runnable cleanupAction) {
                return new ResourceCleanup() {
                    @Override
                    public void cleanup() {
                        cleanupAction.run();
                    }
                };
            }
        }

    }
}
