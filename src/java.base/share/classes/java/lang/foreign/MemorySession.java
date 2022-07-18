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

import java.lang.ref.Cleaner;
import java.util.Objects;

import jdk.internal.foreign.MemorySessionImpl;
import jdk.internal.foreign.NativeMemorySegmentImpl;
import jdk.internal.foreign.Utils;
import jdk.internal.javac.PreviewFeature;
import jdk.internal.ref.CleanerFactory;

/**
 * A memory session manages a group of {@linkplain MemorySegment memory segments} which share the same lifecycle.
 * Memory segments are associated with a memory session and can only be accessed while the session is {@linkplain #isAlive() alive},
 * and by the {@linkplain #ownerThread() thread} associated with the memory session (if any).
 * <p>
 * A memory session acts as an {@linkplain SegmentAllocator allocator}, so it can be used to allocate new segments directly,
 * via its {@link #allocate(long, long)} method. Furthermore, a memory session implements {@link AutoCloseable},
 * meaning that all the segments associated with the session can be released deterministically, by calling its
 * {@link #close()} method, as demonstrated in the following example:
 *
 * {@snippet lang = java:
 * try (MemorySession session = MemorySession.openConfined()) {
 *    MemorySegment segment1 = session.allocate(100);
 *    MemorySegment segment2 = session.allocate(200);
 *    ...
 * } // all memory released here
 *}
 *
 * The above code creates a confined session. Then it allocates two segments associated with that session.
 * When the session is {@linkplain #close() closed} (above, this is done implicitly, using the <em>try-with-resources construct</em>),
 * all memory allocated within the session will be released.
 *
 * <h2><a id = "thread-confinement">Thread confinement</a></h2>
 *
 * Memory sessions can be divided in two groups: <em>thread-confined</em> sessions, and <em>shared</em> sessions.
 * <p>
 * Confined sessions, support strong thread-confinement guarantees. Upon creation,
 * they are assigned an {@linkplain #ownerThread() owner thread}, typically the thread which initiated the creation operation.
 * After creating a confined session, only the owner thread will be allowed to directly manipulate the segments
 * associated with this session. Any attempt to access segments from a thread other than the
 * owner thread will fail with {@link WrongThreadException}.
 * <p>
 * Shared sessions, on the other hand, have no owner thread; as such, segments associated with shared sessions
 * can be accessed by multiple threads. This might be useful when multiple threads need to access the same segment concurrently
 * (e.g. in the case of parallel processing).
 *
 * <h2>Closing sessions</h2>
 *
 * Memory sessions can be {@linkplain #close() closed}. When a session is closed, it is no longer {@linkplain #isAlive() alive},
 * and subsequent operations on segments associated with that session (e.g. attempting to access a {@link MemorySegment} instance)
 * will fail with {@link IllegalStateException}.
 * <p>
 * When a session is closed, the {@linkplain #addCloseAction(Runnable) close actions} associated with that session are executed.
 * For instance, closing a memory session which has been used to {@linkplain #allocate(long, long) allocate} one or more
 * native memory segments results in releasing the off-heap memory associated with said segments.
 * <p>
 * Depending on the session kind, closing a session could be subject to thread-confinement restrictions. For instance,
 * thread-confined sessions can only be closed by the session's {@linkplain #ownerThread() owner thread}, whereas
 * shared sessions can be closed by any thread.
 * <p>
 * Regardless of the session kind, closing a memory session is always a thread-safe and atomic operation: closing a session either
 * succeeds (resulting in subsequent accesses to segments managed by the session to fail), or fails with
 * {@linkplain IllegalStateException}, if the session cannot be closed (e.g. because a segment associated with the session
 * is being concurrently accessed by another thread). Closing a memory session should never result in a JVM crash,
 * if all the segments associated with said session have been obtained <em>safely</em>, e.g. without invoking
 * any <a href="package-summary.html#restricted">restricted methods</a>.
 *
 * <h2 id=implicit-sessions>Implicit sessions</h2>
 *
 * Some memory sessions can be {@linkplain #openImplicit(Cleaner) configured} to be closed automatically by a {@link Cleaner}
 * instance, when they become <a href="../../../java/lang/ref/package.html#reachability">unreachable</a>.
 * Memory sessions associated with cleaners are called <em>implicit sessions</em>.
 * Implicit sessions are always shared and cannot be closed explicitly: calling {@link #close()} will fail with {@link UnsupportedOperationException}.
 * <p>
 * Using implicit sessions can be helpful when clients need to manage one or more long-lived memory segments, where
 * releasing the memory associated with said segments in a deterministic fashion is either not required, or not
 * desirable.
 * <p>
 * A special implicit session is the {@linkplain #global() global session}, an implicit session that is always reachable and,
 * therefore, always {@linkplain #isAlive() alive}. As a result, accessing a segment managed by the
 * global session can never result in a {@link IllegalStateException}. Examples of segments associated with the global
 * session are {@linkplain MemorySegment#ofArray(int[]) heap segments}, as well as segments obtained from
 * {@linkplain Linker#downcallHandle(FunctionDescriptor) interacting} with foreign functions.
 *
 * <h2 id="non-closeable">Non-closeable views</h2>
 *
 * There are situations in which it might not be desirable for a memory session to be reachable from one or
 * more resources associated with it. For instance, an API might create a private memory session, and allocate
 * a memory segment, and then expose one or more slices of this segment to its clients. Since the API's memory session
 * would be reachable from the slices (using the {@link MemorySegment#session()} accessor), it might be possible for
 * clients to compromise the API (e.g. by closing the session prematurely). To avoid leaking private memory sessions
 * to untrusted clients, an API can instead return segments based on a non-closeable view of the session it created, as follows:
 *
 * {@snippet lang = java:
 * MemorySession session = MemorySession.openConfined();
 * MemorySession nonCloseableSession = session.asNonCloseable();
 * MemorySegment segment = nonCloseableSession.allocate(100);
 * segment.session().close(); // throws
 * session.close(); //ok
 *}
 *
 * In other words, only the owner of the original {@code session} object can close the session. External clients can only
 * access the non-closeable session, and have no access to the underlying API session.
 *
 * @implSpec
 * Implementations of this interface are thread-safe.
 *
 * @see MemorySegment
 * @see SymbolLookup
 * @see Linker
 * @see VaList
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
     * {@return {@code true}, if this session is a closeable memory session}.
     */
    boolean isCloseable();

    /**
     * {@return the owner thread associated with this memory session, or {@code null} if this session is shared
     * across multiple threads}
     */
    Thread ownerThread();

    /**
     * Adds a custom cleanup action which will be executed when the memory session is closed.
     * Cleanup actions added from the same thread are executed <em>in reverse</em>; that is the cleanup action
     * that has been added last will be the one running first. The provided cleanup action is executed
     * immediately, if this method fails with an exception.
     * @apiNote The provided action should not keep a strong reference to this memory session, so that implicitly
     * closed sessions can be handled correctly by a {@link Cleaner} instance.
     * @param runnable the custom cleanup action to be associated with this memory session.
     * @throws IllegalStateException if this memory session is not {@linkplain #isAlive() alive}.
     * @throws WrongThreadException if this method is called from a thread other than the thread
     * {@linkplain #ownerThread() owning} this memory session.
     */
    void addCloseAction(Runnable runnable);

    /**
     * Closes this memory session. If this operation completes without exceptions, this session
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
     * @throws IllegalStateException if this memory session is not {@linkplain #isAlive() alive}.
     * @throws IllegalStateException if this session is kept alive by another client.
     * @throws WrongThreadException if this method is called from a thread other than the thread
     * {@linkplain #ownerThread() owning} this memory session.
     * @throws UnsupportedOperationException if this memory session is not {@linkplain #isCloseable() closeable}.
     */
    void close();

    /**
     * Returns a non-closeable view of this memory session. If this session is {@linkplain #isCloseable() non-closeable},
     * this session is returned. Otherwise, this method returns a non-closeable view of this memory session.
     * @apiNote a non-closeable view of a memory session {@code S} keeps {@code S} reachable. As such, {@code S}
     * cannot be closed implicitly (e.g. by a {@link Cleaner}) as long as one or more non-closeable views of {@code S}
     * are reachable.
     * @return a non-closeable view of this memory session.
     */
    MemorySession asNonCloseable();

    /**
     * Compares the specified object with this memory session for equality. Returns {@code true} if and only if the specified
     * object is also a memory session, and it refers to the same memory session as this memory session.
     * {@linkplain #asNonCloseable() A non-closeable view} {@code V} of a memory session {@code S} is considered
     * equal to {@code S}.
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
     * Allocates a native memory segment with the given size (in bytes), alignment constraint (in bytes) in this memory session.
     * The block of off-heap memory associated with the returned native memory segment is initialized to zero.
     *
     * @param bytesSize the size (in bytes) of the off-heap memory block backing the native memory segment.
     * @param alignmentBytes the alignment constraint (in bytes) of the off-heap memory block backing the native memory segment.
     * @return a new native memory segment.
     * @throws IllegalArgumentException if {@code bytesSize < 0}, {@code alignmentBytes <= 0}, or if {@code alignmentBytes}
     * is not a power of 2.
     * @throws IllegalStateException if this session is not {@linkplain MemorySession#isAlive() alive}.
     * @throws WrongThreadException if this method is called from a thread other than the thread
     * {@linkplain MemorySession#ownerThread() owning} this session.
     */
    @Override
    default MemorySegment allocate(long bytesSize, long alignmentBytes) {
        Utils.checkAllocationSizeAndAlign(bytesSize, alignmentBytes);
        return NativeMemorySegmentImpl.makeNativeSegment(bytesSize, alignmentBytes, this);
    }

    /**
     * Creates a confined memory session.
     * @return a new confined memory session.
     */
    static MemorySession openConfined() {
        return MemorySessionImpl.createConfined(Thread.currentThread());
    }

    /**
     * Creates a shared memory session.
     * @return a new shared memory session.
     */
    static MemorySession openShared() {
        return MemorySessionImpl.createShared();
    }

    /**
     * Creates an implicit memory session, managed by a private {@link Cleaner} instance.
     * Equivalent to (but likely more efficient than) the following code:
     * {@snippet lang=java :
     * openImplicit(Cleaner.create())
     * }
     * @return a new implicit memory session, managed by a private {@link Cleaner} instance.
     */
    static MemorySession openImplicit() {
        return MemorySessionImpl.createImplicit(null, CleanerFactory.cleaner());
    }

    /**
     * Creates a new implicit memory session, managed by the given {@link Cleaner} instance.
     * @param cleaner the cleaner to be associated with the returned implicit memory session.
     * @return a new implicit memory session, managed by the given {@link Cleaner} instance.
     */
    static MemorySession openImplicit(Cleaner cleaner) {
        Objects.requireNonNull(cleaner);
        return MemorySessionImpl.createImplicit(null, cleaner);
    }

    /**
     * Returns the global memory session.
     * @return the global memory session.
     */
    static MemorySession global() {
        return MemorySessionImpl.GLOBAL;
    }
}
