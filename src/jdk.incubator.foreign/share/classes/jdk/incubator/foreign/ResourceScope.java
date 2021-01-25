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

import jdk.internal.foreign.MemoryScope;

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
 * <h2>Explicit deallocation</h2>
 *
 * Certain resource scopes can be closed explicitly (see {@link ResourceScope#close()}). To check if a resource scope supports
 * explicit closure, the {@link #isCloseable()} predicate can be used. When a resource scope is closed, it is no longer
 * <em>alive</em> (see {@link #isAlive()}, and subsequent operation on resources derived from that scope (e.g. attempting to
 * access a {@link MemorySegment} instance) will fail with {@link IllegalStateException}.
 * <p>
 * Closing a resource scope will cause all the cleanup actions associated with that scope (see {@link #addOnClose(Runnable)}) to be called.
 * Moreover, closing a resource scope might trigger the releasing of the underlying memory resources associated with said scope; for instance:
 * <ul>
 *     <li>closing the scope associated with a native memory segment results in <em>freeing</em> the native memory associated with it
 *     (see {@link MemorySegment#allocateNative(long, ResourceScope)}, or {@link NativeAllocator#arenaUnbounded(ResourceScope)})</li>
 *     <li>closing the scope associated with a mapped memory segment results in the backing memory-mapped file to be unmapped
 *     (see {@link MemorySegment#mapFile(Path, long, long, FileChannel.MapMode, ResourceScope)})</li>
 *     <li>closing the scope associated with an upcall stub results in releasing the stub
 *     (see {@link CLinker#upcallStub(MethodHandle, FunctionDescriptor, ResourceScope)}</li>
 * </ul>
 *
 * <h2>Implicit deallocation</h2>
 *
 * Resource scopes can be associated with a {@link Cleaner} instance (see {@link #ofConfined(Cleaner)}) - we call these
 * resource scopes <em>managed</em> resource scopes. A managed resource scope is closed automatically once the scope instance
 * becomes <em>unreachable</em>.
 * <p>
 * Managed resource scopes can also be made closeable (see {@link #isCloseable()}), in which
 * case a scope will feature both explicit and implicit deallocation modes. This can be useful to allow for predictable,
 * deterministic resource deallocation, while still prevent accidental native memory leaks.
 * <p>
 * Even when both explicit and implicit deallocation is enabled, cleanup actions associated with a given scope
 * (see {@link #addOnClose(Runnable)}) must be called <em>exactly once</em>.
 *
 * <h2><a id = "thread-confinement">Thread confinement</a></h2>
 *
 * Resource scopes can be further divided into two categories: <em>thread-confined</em> resource scopes, and <em>shared</em>
 * resource scopes.
 * <p>
 * Confined resource scopes (see {@link #ofConfined()}), support strong thread-confinement guarantees. Upon creation,
 * they are assigned an <em>owner thread</em>, typically the thread which initiated the creation operation (see {@link #ownerThread()}).
 * After creating a confined resource scopeon, only the owner thread will be allowed to directly manipulate the resources
 * associated with this resource scope. Any attempt to perform resource access from a thread other than the
 * owner thread will result in a runtime failure.
 * <p>
 * Shared resource scopes (see {@link #ofShared()}), support strong thread-confinement guarantees. A shared resource scope
 * has no owner thread; as such resources associated with this scope can be accessed by multiple threads. This might be useful
 * when multiple threads need to access the same resource concurrently (e.g. in the case of parallel processing). For instance, a client
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
 * <h2>Scope locks</h2>
 *
 * Resource scopes can be <em>locked</em>. When a resource scope is locked, a new instance of type {@link ResourceScope.Lock}
 * is created; a resource scope lock can be used to make sure that its corresponding scope cannot be closed (either explicitly, or implicitly)
 * for a certain period of time - e.g. when one or more resources associated with the parent scope need to be accessed.
 * A resource scope can be acquired multiple times; the resource scope can only be closed <em>after</em> all
 * the locks held against that scope have been closed. This can be useful when clients need to perform a critical
 * operation on a memory segment, during which they have to ensure that the segment will not be released; this
 * can be done as follows:
 *
 * <blockquote><pre>{@code
MemorySegment segment = ...
try (ResourceScope.Lock segmentLock = segment.scope().lock()) {
   <critical operation on segment>
} // release scope lock
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
     * Is this resource scope closeable?
     * @return true, if this resource scope is closeable.
     * @see ResourceScope#close()
     */
    boolean isCloseable();

    /**
     * The thread owning this resource scope.
     * @return the thread owning this resource scope, or {@code null} if this resource scope is shared.
     */
    Thread ownerThread();

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
     *     <li>this resource scope has one or more forked scopes that have not been closed</li>
     * </ul>
     */
    void close();

    /**
     * Add a custom cleanup action which will be executed when the resource scope is closed.
     * @param runnable the custom cleanup action to be associated with this scope.
     * @throws IllegalStateException if this scope has already been closed.
     */
    void addOnClose(Runnable runnable);

    /**
     * Locks this resource scope by acquiring a new resource scope lock. This scope cannot be closed unless all its
     * locks have been released first.
     * @return a resource scope lock.
     */
    Lock lock();

    /**
     * An abstraction modelling a lock on a resource scope. Features a method (see {@link #close()}) which
     * can be used by clients to release the lock.
     */
    interface Lock extends AutoCloseable {

        /**
         * The scope being locked by this instance.
         * @return The scope being locked by this instance.
         */
        ResourceScope scope();

        /**
         * Release the lock on the resource scope associated with this instance.
         */
        @Override
        void close();
    }

    /**
     * Create a new confined scope. The resulting scope is closeable, and is not managed by a {@link Cleaner}.
     * @return a new confined scope.
     */
    static ResourceScope ofConfined() {
        return ofConfined(null, null, true);
    }

    /**
     * Create a new confined scope. The resulting scope is not closeable, and is managed by a {@link Cleaner}.
     * @param cleaner the cleaner to be associated with the returned scope.
     * @return a new confined scope, managed by {@code cleaner}.
     * @throws NullPointerException if {@code cleaner == null}.
     */
    static ResourceScope ofConfined(Cleaner cleaner) {
        Objects.requireNonNull(cleaner);
        return ofConfined(null, cleaner, false);
    }

    /**
     * Create a new confined scope. The resulting scope might be managed by a {@link Cleaner} (where provided) and might be closeable,
     * as specified by the {@code closeable} parameter. An optional attachment can be associated with the resulting
     * scope.
     * @param attachment an attachment object which is kept alive by the returned resource scope (can be {@code null}).
     * @param cleaner the cleaner to be associated with the returned scope. Can be {@code null}.
     * @param closeable whether the returned resource scope can be closed directly, with {@link #close()}).
     * @return a new confined scope, managed by {@code cleaner}; the resulting scope is closeable if {@code closeable == true}.
     */
    static ResourceScope ofConfined(Object attachment, Cleaner cleaner, boolean closeable) {
        return MemoryScope.createConfined(attachment, cleaner, closeable);
    }

    /**
     * Create a new shared scope. The resulting scope is closeable, and is also managed by a {@link Cleaner}.
     * @return a new shared scope, managed by {@code cleaner}.
     */
    static ResourceScope ofShared() {
        return ofShared(null, null, true);
    }

    /**
     * Create a new shared scope. The resulting scope is not closeable, and is managed by a {@link Cleaner}.
     * @param cleaner the cleaner to be associated with the returned scope.
     * @return a new shared scope, managed by {@code cleaner}.
     * @throws NullPointerException if {@code cleaner == null}.
     */
    static ResourceScope ofShared(Cleaner cleaner) {
        Objects.requireNonNull(cleaner);
        return ofShared(null, cleaner, false);
    }

    /**
     * Create a new shared scope. The resulting scope might be managed by a {@link Cleaner} (where provided) and might be closeable,
     * as specified by the {@code closeable} parameter. An optional attachment can be associated with the resulting
     * scope.
     * @param attachment an attachment object which is kept alive by the returned resource scope (can be {@code null}).
     * @param cleaner the cleaner to be associated with the returned scope. Can be {@code null}.
     * @param closeable whether the returned resource scope can be closed directly, with {@link #close()}).
     * @return a new shared scope, managed by {@code cleaner}; the resulting scope is closeable if {@code closeable == true}.
     */
    static ResourceScope ofShared(Object attachment, Cleaner cleaner, boolean closeable) {
        return MemoryScope.createShared(attachment, cleaner, closeable);
    }

    /**
     * A non-closeable, shared, global scope which is assumed to be always alive.
     * @return the global scope.
     */
    static ResourceScope globalScope() {
        return MemoryScope.GLOBAL;
    }
}

