/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.foreign;

import java.lang.invoke.MethodHandle;
import java.lang.ref.Cleaner;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Objects;

import jdk.internal.foreign.MemorySessionImpl;
import jdk.internal.javac.PreviewFeature;

/**
 * A memory session manages the lifecycle of one or more resources. Resources (e.g. {@link MemorySegment}) associated
 * with a memory session can only be accessed while the memory session is {@linkplain #isAlive() alive},
 * and by the {@linkplain #ownerThread() thread} associated with the memory session (if any).
 * <p>
 * Memory sessions can be closed, either implicitly (e.g. when a session is no longer reachable), or explicitly.
 * When a memory session is closed, it is no longer {@link #isAlive() alive}, and subsequent operations on resources
 * associated with that session (e.g. attempting to access a {@link MemorySegment} instance) will fail with {@link IllegalStateException}.
 * <p>
 * When a memory session is closed (either explicitly, or implicitly), all the {@linkplain #addCloseAction(Runnable) close actions} associated with that session will be called.
 * and underlying memory resources associated with said session might be released; for instance:
 * <ul>
 *     <li>closing the memory session associated with a {@linkplain MemorySegment#allocateNative(long, long, MemorySession) native memory segment}
 *     results in <em>freeing</em> the native memory associated with it;</li>
 *     <li>closing the memory session associated with a {@linkplain MemorySegment#mapFile(Path, long, long, FileChannel.MapMode, MemorySession) mapped memory segment}
 *     results in the backing memory-mapped file to be unmapped;</li>
 *     <li>closing the memory session associated with an {@linkplain CLinker#upcallStub(MethodHandle, FunctionDescriptor, MemorySession) upcall stub}
 *     results in releasing the stub;</li>
 *     <li>closing the memory session associated with a {@linkplain VaList variable arity list} results in releasing the memory
 *     associated with that variable arity list instance.</li>
 * </ul>
 *
 * <h2><a id = "thread-confinement">Thread confinement</a></h2>
 *
 * Memory sessions can be divided into two categories: <em>thread-confined</em> memory sessions, and <em>shared</em>
 * memory sessions.
 * <p>
 * Confined memory sessions, support strong thread-confinement guarantees. Upon creation,
 * they are assigned an {@linkplain #ownerThread() owner thread}, typically the thread which initiated the creation operation.
 * After creating a confined memory session, only the owner thread will be allowed to directly manipulate the resources
 * associated with this memory session. Any attempt to perform resource access from a thread other than the
 * owner thread will result in a runtime failure.
 * <p>
 * Shared memory sessions, on the other hand, have no owner thread; as such, resources associated with shared memory sessions
 * can be accessed by multiple threads. This might be useful when multiple threads need to access the same resource concurrently
 * (e.g. in the case of parallel processing).
 *
 * <h2>Deterministic deallocation</h2>
 *
 * When a session is associated with off-heap resources, it is often desirable for said resources to be released in a timely fashion,
 * rather than waiting for the session to be deemed <a href="../../../java/lang/ref/package.html#reachability">unreachable</a>
 * by the garbage collector. In this scenario, a client might consider using a {@linkplain #isCloseable() <em>closeable</em>} memory session.
 * Closeable memory sessions are memory sessions that can be {@link MemorySession#close() closed} explicitly, as demonstrated
 * in the following example:
 *
 * {@snippet lang=java :
 * try (MemorySession session = MemorySession.openConfined()) {
 *    MemorySegment segment1 = MemorySegment.allocateNative(100);
 *    MemorySegment segment1 = MemorySegment.allocateNative(200);
 *    ...
 * } // all memory released here
 * }
 *
 * The above code creates a confined, closeable session. Then it allocates two segments associated with that session.
 * When the session is {@linkplain #close() closed} (above, this is done implicitly, using the <em>try-with-resources construct</em>),
 * all memory allocated within the session will be released
 * <p>
 * Closeable memory sessions, while powerful, must be used with caution. Clients must remember to
 * {@linkplain MemorySession#close() close} a closeable memory session explicitly, when the session
 * is no longer in use. A failure to do so might result in memory leaks. To mitigate this problem,
 * closeable memory sessions can be associated with a {@link Cleaner} instance, so that they are also closed automatically,
 * once the session instance becomes <a href="../../../java/lang/ref/package.html#reachability">unreachable</a>.
 * This can be useful to allow for predictable, deterministic resource deallocation, while still preventing accidental
 * native memory leaks. In case a managed memory session is closed explicitly, no further action will be taken when
 * the session becomes unreachable; that is, {@linkplain #addCloseAction(Runnable) close actions} associated with a
 * memory session, whether managed or not, are called <em>exactly once</em>.
 *
 * <h2><a id = "global-session">Global session</a></h2>
 *
 * An important memory session is the so called {@linkplain #global() global session}; the global session is
 * a memory session that cannot be closed, either explicitly or implicitly. As a result, the global session will never
 * attempt to release resources associated with it. Examples of resources associated with the global memory session are
 * {@linkplain MemorySegment#ofArray(int[]) heap segments}.
 *
 * @implSpec
 * Implementations of this interface are immutable, thread-safe and <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
 *
 * @since 19
 */
@PreviewFeature(feature=PreviewFeature.Feature.FOREIGN)
public sealed interface MemorySession extends AutoCloseable, SegmentAllocator permits MemorySessionImpl, MemorySessionImpl.NonCloseableView {

    /**
     * {@return {@code true}, if this memory session is alive}
     */
    boolean isAlive();

    /**
     * {@return {@code true}, if this session can be {@linkplain #close() closed} explicitly}
     */
    boolean isCloseable();

    /**
     * {@return the owner thread associated with this memory session (if any)}
     */
    Thread ownerThread();

    /**
     * Run a critical action while this memory session is kept alive.
     * @param action the action to be run.
     */
    void whileAlive(Runnable action);

    /**
     * Add a custom cleanup action which will be executed when the memory session is closed.
     * The order in which custom cleanup actions are invoked once the memory session is closed is unspecified.
     * @apiNote The provided action should not keep a strong reference to this memory session, so that implicitly
     * closed sessions can be handled correctly by a {@link Cleaner} instance.
     * @param runnable the custom cleanup action to be associated with this memory session.
     * @throws IllegalStateException if this memory session is not {@linkplain #isAlive() alive}, or if access occurs from
     * a thread other than the thread {@linkplain #ownerThread() owning} this memory session.
     */
    void addCloseAction(Runnable runnable);

    /**
     * Closes this memory session. As a side effect, if this operation completes without exceptions, this session
     * will be marked as <em>not alive</em>, the {@linkplain #addCloseAction(Runnable) close actions} associated
     * with this session will be executed, and all the resources associated with this session will be released.
     *
     * @apiNote This operation is not idempotent; that is, closing an already closed memory session <em>always</em> results in an
     * exception being thrown. This reflects a deliberate design choice: memory session state transitions should be
     * manifest in the client code; a failure in any of these transitions reveals a bug in the underlying application
     * logic.
     *
     * @see MemorySession#isAlive()
     *
     * @throws IllegalStateException if this memory session is not {@linkplain #isAlive() alive}, or if access occurs from
     * a thread other than the thread {@linkplain #ownerThread() owning} this memory session.
     * @throws IllegalStateException this memory session is shared and a resource associated with this session is accessed while this method is called.
     * @throws UnsupportedOperationException if this memory session is not {@linkplain #isCloseable() closeable}.
     */
    void close();

    /**
     * Compares the specified object with this memory session for equality. Returns {@code true} if and only if the specified
     * object is also a memory session, and it refers to the same memory session as this memory session. This method
     * is especially useful when operating on non-closeable views of memory sessions, such as the ones returned
     * by {@link MemorySegment#session()}.
     *
     * @param that the object to be compared for equality with this memory session.
     * @return {@code true} if the specified object is equal to this memory session.
     */
    @Override
    boolean equals(Object that);

    /**
     * {@return the hash code value for this memory session}
     */
    @Override
    int hashCode();

    /**
     * Allocates a new native segment, using this session. Equivalent to the following code:
     * {@snippet lang=java :
     * MemorySegment.allocateNative(size, align, this);
     * }
     *
     * @throws IllegalStateException if this memory session is not {@linkplain #isAlive() alive}, or if access occurs from
     * a thread other than the thread {@linkplain #ownerThread() owning} this memory session.
     * @return a new native segment, associated with this session.
     */
    @Override
    default MemorySegment allocate(long bytesSize, long bytesAlignment) {
        return MemorySegment.allocateNative(bytesSize, bytesAlignment, this);
    }

    /**
     * Creates a new closeable confined memory session.
     * @return a new closeable confined memory session.
     */
    static MemorySession openConfined() {
        return MemorySessionImpl.createConfined(Thread.currentThread(), null);
    }

    /**
     * Creates a new closeable confined memory session, managed by the provided cleaner instance.
     * @param cleaner the cleaner to be associated with the returned memory session.
     * @return a new closeable confined memory session, managed by {@code cleaner}.
     */
    static MemorySession openConfined(Cleaner cleaner) {
        Objects.requireNonNull(cleaner);
        return MemorySessionImpl.createConfined(Thread.currentThread(), cleaner);
    }

    /**
     * Creates a new closeable shared memory session.
     * @return a new closeable shared memory session.
     */
    static MemorySession openShared() {
        return MemorySessionImpl.createShared(null);
    }

    /**
     * Creates a new closeable shared memory session, managed by the provided cleaner instance.
     * @param cleaner the cleaner to be associated with the returned memory session.
     * @return a new closeable shared memory session, managed by {@code cleaner}.
     */
    static MemorySession openShared(Cleaner cleaner) {
        Objects.requireNonNull(cleaner);
        return MemorySessionImpl.createShared(cleaner);
    }

    /**
     * Creates a new non-closeable shared memory session, managed by a private {@link Cleaner} instance.
     * Equivalent to (but likely more efficient than) the following code:
     * {@snippet lang=java :
     * openShared(Cleaner.create());
     * }
     * @return a non-closeable shared memory session, managed by a private {@link Cleaner} instance.
     */
    static MemorySession openImplicit() {
        return MemorySessionImpl.createImplicit();
    }

    /**
     * Returns the <a href="MemorySession.html#global-session"><em>global memory session</em></a>.
     * @return the <a href="MemorySession.html#global-session"><em>global memory session</em></a>.
     */
    static MemorySession global() {
        return MemorySessionImpl.GLOBAL;
    }
}
