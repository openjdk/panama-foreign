/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

package jdk.incubator.foreign;

import jdk.internal.foreign.ResourceScopeImpl;

import java.lang.invoke.MethodHandle;
import java.lang.ref.Cleaner;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Spliterator;

/**
 * A resource scope manages the lifecycle of one or more resources. Resources (e.g. {@link MemorySegment}) associated
 * with a resource scope can only be accessed while the resource scope is <em>alive</em> (see {@link #isAlive()}),
 * and by the thread associated with the resource scope (if any).
 *
 * <h2>Explicit closure</h2>
 *
 * Resource scopes created using one of the factories in this class can be closed explicitly (see {@link ResourceScope#close()}).
 * When a resource scope is closed, it is no longer <em>alive</em> (see {@link #isAlive()}, and subsequent operation on
 * resources derived from that scope (e.g. attempting to access a {@link MemorySegment} instance) will fail with {@link IllegalStateException}.
 * <p>
 * Closing a resource scope will cause all the cleanup actions associated with that scope (see {@link #addOnClose(Runnable)}) to be called.
 * Moreover, closing a resource scope might trigger the releasing of the underlying memory resources associated with said scope; for instance:
 * <ul>
 *     <li>closing the scope associated with a native memory segment results in <em>freeing</em> the native memory associated with it
 *     (see {@link MemorySegment#allocateNative(long, ResourceScope)}, or {@link SegmentAllocator#arenaAllocator(ResourceScope)})</li>
 *     <li>closing the scope associated with a mapped memory segment results in the backing memory-mapped file to be unmapped
 *     (see {@link MemorySegment#mapFile(Path, long, long, FileChannel.MapMode, ResourceScope)})</li>
 *     <li>closing the scope associated with an upcall stub results in releasing the stub
 *     (see {@link CLinker#upcallStub(MethodHandle, FunctionDescriptor, ResourceScope)}</li>
 * </ul>
 *
 * <h2><a id = "implicit-closure">Implicit closure</a></h2>
 *
 * Resource scopes can be associated with a {@link Cleaner} instance (see {@link #newConfinedScope(Cleaner)}) - we call these
 * resource scopes <em>managed</em> resource scopes. A managed resource scope is closed automatically once the scope instance
 * becomes <a href="../../../java/lang/ref/package.html#reachability">unreachable</a>.
 * <p>
 * Managed resource scopes can still be closed explicitly (see {@link #close()}); this can be useful to allow for predictable,
 * deterministic resource deallocation, while still prevent accidental native memory leaks. In case a managed resource
 * scope is closed explicitly, no further action will be taken when the scope becomes unreachable; that is, cleanup actions
 * (see {@link #addOnClose(Runnable)}) associated with a resource scope, whether managed or not, are called <em>exactly once</em>.
 * <p>
 * Some managed resource scopes are implicitly managed (see {@link #newImplicitScope()}, {@link #globalScope()}, and are said to be <em>implicit scopes</em>.
 * An implicit resource scope only features implicit closure, and always throws an {@link UnsupportedOperationException}
 * when the {@link #close()} method is called directly.
 *
 * <h2><a id = "thread-confinement">Thread confinement</a></h2>
 *
 * Resource scopes can be further divided into two categories: <em>thread-confined</em> resource scopes, and <em>shared</em>
 * resource scopes.
 * <p>
 * Confined resource scopes (see {@link #newConfinedScope()}), support strong thread-confinement guarantees. Upon creation,
 * they are assigned an <em>owner thread</em>, typically the thread which initiated the creation operation (see {@link #ownerThread()}).
 * After creating a confined resource scope, only the owner thread will be allowed to directly manipulate the resources
 * associated with this resource scope. Any attempt to perform resource access from a thread other than the
 * owner thread will result in a runtime failure.
 * <p>
 * Shared resource scopes (see {@link #newSharedScope()}), on the other hand, have no owner thread; as such resources associated
 * with this shared resource scopes can be accessed by multiple threads. This might be useful when multiple threads need
 * to access the same resource concurrently (e.g. in the case of parallel processing). For instance, a client
 * might obtain a {@link Spliterator} from a shared segment, which can then be used to slice the segment and allow multiple
 * threads to work in parallel on disjoint segment slices. The following code can be used to sum all int values in a memory segment in parallel:
 *
 * <blockquote><pre>{@code
SequenceLayout SEQUENCE_LAYOUT = MemoryLayout.ofSequence(1024, MemoryLayouts.JAVA_INT);
try (ResourceScope scope = ResourceScope.ofShared()) {
    MemorySegment segment = MemorySegment.allocateNative(SEQUENCE_LAYOUT, scope);
    VarHandle VH_int = SEQUENCE_LAYOUT.elementLayout().varHandle(int.class);
    int sum = StreamSupport.stream(segment.spliterator(SEQUENCE_LAYOUT), true)
        .mapToInt(s -> (int)VH_int.get(s.address()))
        .sum();
}
 * }</pre></blockquote>
 *
 * <p>
 * When using shared resource scopes, clients should make sure that no other thread is accessing the segment while
 * the segment is being closed. If one or more threads attempts to access a segment concurrently while the
 * segment is being closed, an exception might occur on both the accessing and the closing threads. Clients should
 * refrain from attempting to close a shared resource scope repeatedly (e.g. keep calling {@link #close()} until no exception is thrown);
 * such exceptions should instead be seen as an indication that the client code is lacking appropriate synchronization between the threads
 * accessing/closing the resources associated with the shared resource scope.
 *
 * <h2>Scope handles</h2>
 *
 * Resource scopes can be made <em>non-closeable</em> by acquiring one or more resource scope <em>handles</em> (see
 * {@link #acquire()}. A resource scope handle can be used to make sure that its corresponding scope cannot be closed
 * (either explicitly, or implicitly) for a certain period of time - e.g. when one or more resources associated with
 * the parent scope need to be accessed. A resource scope can be acquired multiple times; the resource scope can only be
 * closed <em>after</em> all the handles acquired against that scope have been closed (see {@link Handle#close()}).
 * This can be useful when clients need to perform a critical operation on a memory segment, during which they have
 * to ensure that the segment will not be released; this can be done as follows:
 *
 * <blockquote><pre>{@code
MemorySegment segment = ...
try (ResourceScope.Handle segmentHandle = segment.scope().acquire()) {
   <critical operation on segment>
} // release scope handle
 * }</pre></blockquote>
 *
 * @apiNote In the future, if the Java language permits, {@link ResourceScope}
 * may become a {@code sealed} interface, which would prohibit subclassing except by other explicitly permitted subtypes.
 *
 * @implSpec
 * Implementations of this interface are immutable, thread-safe and <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
 */
public interface ResourceScope extends AutoCloseable {
    /**
     * Is this resource scope alive?
     * @return true, if this resource scope is alive.
     * @see ResourceScope#close()
     */
    boolean isAlive();

    /**
     * The thread owning this resource scope.
     * @return the thread owning this resource scope, or {@code null} if this resource scope is shared.
     */
    Thread ownerThread();

    /**
     * Is this resource scope an <em>implicit scope</em>?
     * @return true if this scope is an <em>implicit scope</em>.
     * @see #newImplicitScope()
     * @see #globalScope()
     */
    boolean isImplicit();

    /**
     * Closes this resource scope. As a side-effect, if this operation completes without exceptions, this scope will be marked
     * as <em>not alive</em>, and subsequent operations on resources associated with this scope will fail with {@link IllegalStateException}.
     * Additionally, upon successful closure, all native resources associated with this resource scope will be released.
     *
     * @apiNote This operation is not idempotent; that is, closing an already closed resource scope <em>always</em> results in an
     * exception being thrown. This reflects a deliberate design choice: resource scope state transitions should be
     * manifest in the client code; a failure in any of these transitions reveals a bug in the underlying application
     * logic.
     *
     * @throws IllegalStateException if one of the following condition is met:
     * <ul>
     *     <li>this resource scope is not <em>alive</em>
     *     <li>this resource scope is confined, and this method is called from a thread other than the thread owning this resource scope</li>
     *     <li>this resource scope is shared and a resource associated with this scope is accessed while this method is called</li>
     *     <li>one or more handles (see {@link #acquire()}) associated with this resource scope have not been closed</li>
     * </ul>
     * @throws UnsupportedOperationException if this resource scope is {@link #isImplicit() implicit}.
     */
    void close();

    /**
     * Add a custom cleanup action which will be executed when the resource scope is closed.
     * @param runnable the custom cleanup action to be associated with this scope.
     * @throws IllegalStateException if this scope has already been closed.
     */
    void addOnClose(Runnable runnable);

    /**
     * Make this resource scope non-closeable by acquiring a new resource scope handle. This scope cannot be closed unless all its
     * acquired handles have been closed first. Additionally, a resource scope handle maintains a strong reference
     * to its resource scope; this means that if a resource scope features
     * <a href="ResourceScope.html#implicit-closure"><em>implicit closure</em></a>, the scope cannot be implicitly closed
     * until all its acquired handles becomes <a href="../../../java/lang/ref/package.html#reachability">unreachable</a>.
     * @return a resource scope handle.
     */
    Handle acquire();

    /**
     * An abstraction modelling resource scope handle. A resource scope handle is typically acquired by clients (see
     * {@link #acquire()} in order to prevent the resource scope from being closed while executing a certain operation.
     * A resource scope handle features a method (see {@link #close()}) which can be used by clients to release the handle.
     */
    interface Handle extends AutoCloseable {

        /**
         * Release this handle on the resource scope associated with this instance. This method is idempotent,
         * that is, closing an already closed handle has no effect.
         */
        @Override
        void close();
    }

    /**
     * Create a new confined scope. The resulting scope is closeable, and is not managed by a {@link Cleaner}.
     * @return a new confined scope.
     */
    static ResourceScope newConfinedScope() {
        return ResourceScopeImpl.createConfined( null);
    }

    /**
     * Create a new confined scope managed by a {@link Cleaner}.
     * @param cleaner the cleaner to be associated with the returned scope.
     * @return a new confined scope, managed by {@code cleaner}.
     * @throws NullPointerException if {@code cleaner == null}.
     */
    static ResourceScope newConfinedScope(Cleaner cleaner) {
        Objects.requireNonNull(cleaner);
        return ResourceScopeImpl.createConfined( cleaner);
    }

    /**
     * Create a new shared scope. The resulting scope is closeable, and is not managed by a {@link Cleaner}.
     * @return a new shared scope.
     */
    static ResourceScope newSharedScope() {
        return ResourceScopeImpl.createShared(null);
    }

    /**
     * Create a new shared scope managed by a {@link Cleaner}.
     * @param cleaner the cleaner to be associated with the returned scope.
     * @return a new shared scope, managed by {@code cleaner}.
     * @throws NullPointerException if {@code cleaner == null}.
     */
    static ResourceScope newSharedScope(Cleaner cleaner) {
        Objects.requireNonNull(cleaner);
        return ResourceScopeImpl.createShared(cleaner);
    }

    /**
     * Create a new <em>implicit scope</em>. The implicit scope is a managed, shared, and non-closeable scope which only features
     * <a href="ResourceScope.html#implicit-closure"><em>implicit closure</em></a>.
     * Since implicit scopes can only be closed implicitly by the garbage collector, it is recommended that implicit
     * scopes are only used in cases where deallocation performance is not a critical concern, to avoid unnecessary
     * memory pressure.
     *
     * @return a new implicit scope.
     */
    static ResourceScope newImplicitScope() {
        return ResourceScopeImpl.createImplicitScope();
    }

    /**
     * Returns an implicit scope which is assumed to be always alive.
     * @return the global scope.
     */
    static ResourceScope globalScope() {
        return ResourceScopeImpl.GLOBAL;
    }
}
