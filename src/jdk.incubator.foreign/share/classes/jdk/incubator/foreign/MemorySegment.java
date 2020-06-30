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

package jdk.incubator.foreign;

import java.nio.ByteBuffer;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Spliterator;

/**
 * A memory segment models a contiguous region of memory. A memory segment is associated with both spatial
 * and temporal bounds. Spatial bounds ensure that memory access operations on a memory segment cannot affect a memory location
 * which falls <em>outside</em> the boundaries of the memory segment being accessed. Temporal bounds ensure that memory access
 * operations on a segment cannot occur after a memory segment has been closed (see {@link MemorySegment#close()}).
 * <p>
 * All implementations of this interface must be <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>;
 * use of identity-sensitive operations (including reference equality ({@code ==}), identity hash code, or synchronization) on
 * instances of {@code MemorySegment} may have unpredictable results and should be avoided. The {@code equals} method should
 * be used for comparisons.
 * <p>
 * Non-platform classes should not implement {@linkplain MemorySegment} directly.
 *
 * <h2>Constructing memory segments from different sources</h2>
 *
 * There are multiple ways to obtain a memory segment. First, memory segments backed by off-heap memory can
 * be allocated using one of the many factory methods provided (see {@link MemorySegments#allocateNative(MemoryLayout)},
 * {@link MemorySegments#allocateNative(long)} and {@link MemorySegments#allocateNative(long, long)}). Memory segments obtained
 * in this way are called <em>native memory segments</em>.
 * <p>
 * It is also possible to obtain a memory segment backed by an existing heap-allocated Java array,
 * using one of the provided factory methods (e.g. {@link MemorySegments#ofArray(int[])}). Memory segments obtained
 * in this way are called <em>array memory segments</em>.
 * <p>
 * It is possible to obtain a memory segment backed by an existing Java byte buffer (see {@link ByteBuffer}),
 * using the factory method {@link MemorySegments#ofByteBuffer(ByteBuffer)}.
 * Memory segments obtained in this way are called <em>buffer memory segments</em>. Note that buffer memory segments might
 * be backed by native memory (as in the case of native memory segments) or heap memory (as in the case of array memory segments),
 * depending on the characteristics of the byte buffer instance the segment is associated with. For instance, a buffer memory
 * segment obtained from a byte buffer created with the {@link ByteBuffer#allocateDirect(int)} method will be backed
 * by native memory.
 * <p>
 * Finally, it is also possible to obtain a memory segment backed by a memory-mapped file using the factory method
 * {@link MemorySegments#mapFromPath(Path, long, long, FileChannel.MapMode)}. Such memory segments are called <em>mapped memory segments</em>
 * (see {@link MappedMemorySegment}).
 * <p>
 * Array and buffer segments are effectively <em>views</em> over existing memory regions which might outlive the
 * lifecycle of the segments derived from them, and can even be manipulated directly (e.g. via array access, or direct use
 * of the {@link ByteBuffer} API) by other clients. As a result, while sharing array or buffer segments is possible,
 * it is strongly advised that clients wishing to do so take extra precautions to make sure that the underlying memory sources
 * associated with such segments remain inaccessible, and that said memory sources are never aliased by more than one segment
 * at a time - e.g. so as to prevent concurrent modifications of the contents of an array, or buffer segment.
 *
 * <h2>Closing a memory segment</h2>
 *
 * Memory segments are closed explicitly (see {@link MemorySegment#close()}). When a segment is closed, it is no longer
 * <em>alive</em> (see {@link #isAlive()}, and subsequent operation on the segment (or on any {@link MemoryAddress} instance
 * derived from it) will fail with {@link IllegalStateException}.
 * <p>
 * Closing a segment might trigger the releasing of the underlying memory resources associated with said segment, depending on
 * the kind of memory segment being considered:
 * <ul>
 *     <li>closing a native memory segment results in <em>freeing</em> the native memory associated with it</li>
 *     <li>closing a mapped memory segment results in the backing memory-mapped file to be unmapped</li>
 *     <li>closing a buffer, or a heap segment does not have any side-effect, other than marking the segment
 *     as <em>not alive</em> (see {@link MemorySegment#isAlive()}). Also, since the buffer and heap segments might keep
 *     strong references to the original buffer or array instance, it is the responsibility of clients to ensure that
 *     these segments are discarded in a timely manner, so as not to prevent garbage collection to reclaim the underlying
 *     objects.</li>
 * </ul>
 *
 * <h2><a id = "access-modes">Access modes</a></h2>
 *
 * Memory segments supports zero or more <em>access modes</em>. Supported access modes are {@link #READ},
 * {@link #WRITE}, {@link #CLOSE}, {@link #ACQUIRE} and {@link #HANDOFF}. The set of access modes supported by a segment alters the
 * set of operations that are supported by that segment. For instance, attempting to call {@link #close()} on
 * a segment which does not support the {@link #CLOSE} access mode will result in an exception.
 * <p>
 * The set of supported access modes can only be made stricter (by supporting <em>fewer</em> access modes). This means
 * that restricting the set of access modes supported by a segment before sharing it with other clients
 * is generally a good practice if the creator of the segment wants to retain some control over how the segment
 * is going to be accessed.
 *
 * <h2>Memory segment views</h2>
 *
 * Memory segments support <em>views</em>. For instance, it is possible to alter the set of supported access modes,
 * by creating an <em>immutable</em> view of a memory segment, as follows:
 * <blockquote><pre>{@code
MemorySegment segment = ...
MemorySegment roSegment = segment.withAccessModes(segment.accessModes() & ~WRITE);
 * }</pre></blockquote>
 * It is also possible to create views whose spatial bounds are stricter than the ones of the original segment
 * (see {@link MemorySegment#asSlice(long, long)}).
 * <p>
 * Temporal bounds of the original segment are inherited by the view; that is, closing a segment view, such as a sliced
 * view, will cause the original segment to be closed; as such special care must be taken when sharing views
 * between multiple clients. If a client want to protect itself against early closure of a segment by
 * another actor, it is the responsibility of that client to take protective measures, such as removing {@link #CLOSE}
 * from the set of supported access modes, before sharing the view with another client.
 * <p>
 * To allow for interoperability with existing code, a byte buffer view can be obtained from a memory segment
 * (see {@link MemorySegments#asByteBuffer(MemorySegment)}). This can be useful, for instance, for those clients that want to keep using the
 * {@link ByteBuffer} API, but need to operate on large memory segments. Byte buffers obtained in such a way support
 * the same spatial and temporal access restrictions associated to the memory segment from which they originated.
 *
 * <h2><a id = "thread-confinement">Thread confinement</a></h2>
 *
 * Memory segments support strong thread-confinement guarantees. Upon creation, they are assigned an <em>owner thread</em>,
 * typically the thread which initiated the creation operation. After creation, only the owner thread will be allowed
 * to directly manipulate the memory segment (e.g. close the memory segment) or access the underlying memory associated with
 * the segment using a memory access var handle. Any attempt to perform such operations from a thread other than the
 * owner thread will result in a runtime failure.
 * <p>
 * Memory segments support <em>serial thread confinement</em>; that is, ownership of a memory segment can change (see
 * {@link #withOwnerThread(Thread)}). This allows, for instance, for two threads {@code A} and {@code B} to share
 * a segment in a controlled, cooperative and race-free fashion.
 * <p>
 * In some cases, it might be useful for multiple threads to process the contents of the same memory segment concurrently
 * (e.g. in the case of parallel processing); while memory segments provide strong confinement guarantees, it is possible
 * to obtain a {@link Spliterator} from a segment, which can be used to slice the segment and allow multiple thread to
 * work in parallel on disjoint segment slices (this assumes that the access mode {@link #ACQUIRE} is set).
 * For instance, the following code can be used to sum all int values in a memory segment in parallel:
 * <blockquote><pre>{@code
MemorySegment segment = ...
SequenceLayout SEQUENCE_LAYOUT = MemoryLayout.ofSequence(1024, MemoryLayouts.JAVA_INT);
VarHandle VH_int = SEQUENCE_LAYOUT.elementLayout().varHandle(int.class);
int sum = StreamSupport.stream(MemorySegment.spliterator(segment, SEQUENCE_LAYOUT), true)
                       .mapToInt(s -> (int)VH_int.get(s.baseAddress()))
                       .sum();
 * }</pre></blockquote>
 *
 * @apiNote In the future, if the Java language permits, {@link MemorySegment}
 * may become a {@code sealed} interface, which would prohibit subclassing except by
 * {@link MappedMemorySegment} and other explicitly permitted subtypes.
 *
 * @implSpec
 * Implementations of this interface are immutable, thread-safe and <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
 */
public interface MemorySegment extends AutoCloseable {

    /**
     * The base memory address associated with this memory segment. The returned address is
     * a <em>checked</em> memory address and can therefore be used in derefrence operations
     * (see {@link MemoryAddress}).
     * @return The base memory address.
     */
    MemoryAddress baseAddress();

    /**
     * The thread owning this segment.
     * @return the thread owning this segment.
     */
    Thread ownerThread();

    /**
     * Obtains a new memory segment backed by the same underlying memory region as this segment,
     * but with different owner thread. As a side-effect, this segment will be marked as <em>not alive</em>,
     * and subsequent operations on this segment will result in runtime errors.
     * <p>
     * Write accesses to the segment's content <a href="../../../java/util/concurrent/package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * hand-over from the current owner thread to the new owner thread, which in turn <i>happens before</i> read accesses to the segment's contents on
     * the new owner thread.
     *
     * @param newOwner the new owner thread.
     * @return a new memory segment backed by the same underlying memory region as this segment,
     *      owned by {@code newOwner}.
     * @throws IllegalStateException if this segment is not <em>alive</em>, or if access occurs from a thread other than the
     * thread owning this segment, or if the segment cannot be closed because it is being operated upon by a different
     * thread (see {@link MemorySegments#spliterator(MemorySegment, SequenceLayout)}).
     * @throws NullPointerException if {@code newOwner == null}
     * @throws IllegalArgumentException if the segment is already a confined segment owner by {@code newOnwer}.
     * @throws UnsupportedOperationException if this segment does not support the {@link #HANDOFF} access mode.
     */
    MemorySegment withOwnerThread(Thread newOwner);

    /**
     * The size (in bytes) of this memory segment.
     * @return The size (in bytes) of this memory segment.
     */
    long byteSize();

    /**
     * Obtains a segment view with specific <a href="#access-modes">access modes</a>. Supported access modes are {@link #READ}, {@link #WRITE},
     * {@link #CLOSE}, {@link #ACQUIRE} and {@link #HANDOFF}. It is generally not possible to go from a segment with stricter access modes
     * to one with less strict access modes. For instance, attempting to add {@link #WRITE} access mode to a read-only segment
     * will be met with an exception.
     * @param accessModes an ORed mask of zero or more access modes.
     * @return a segment view with specific access modes.
     * @throws IllegalArgumentException when {@code mask} is an access mask which is less strict than the one supported by this
     * segment, or when {@code mask} contains bits not associated with any of the supported access modes.
     */
    MemorySegment withAccessModes(int accessModes);

    /**
     * Does this segment support a given set of access modes?
     * @param accessModes an ORed mask of zero or more access modes.
     * @return true, if the access modes in {@code accessModes} are stricter than the ones supported by this segment.
     * @throws IllegalArgumentException when {@code mask} contains bits not associated with any of the supported access modes.
     */
    boolean hasAccessModes(int accessModes);

    /**
     * Returns the <a href="#access-modes">access modes</a> associated with this segment; the result is represented as ORed values from
     * {@link #READ}, {@link #WRITE}, {@link #CLOSE}, {@link #ACQUIRE} and {@link #HANDOFF}.
     * @return the access modes associated with this segment.
     */
    int accessModes();

    /**
     * Obtains a new memory segment view whose base address is the same as the base address of this segment plus a given offset,
     * and whose new size is specified by the given argument.
     * @param offset The new segment base offset (relative to the current segment base address), specified in bytes.
     * @param newSize The new segment size, specified in bytes.
     * @return a new memory segment view with updated base/limit addresses.
     * @throws IndexOutOfBoundsException if {@code offset < 0}, {@code offset > byteSize()}, {@code newSize < 0}, or {@code newSize > byteSize() - offset}
     */
    MemorySegment asSlice(long offset, long newSize);

    /**
     * Is this segment alive?
     * @return true, if the segment is alive.
     * @see MemorySegment#close()
     */
    boolean isAlive();

    /**
     * Closes this memory segment. Once a memory segment has been closed, any attempt to use the memory segment,
     * or to access any {@link MemoryAddress} instance associated with it will fail with {@link IllegalStateException}.
     * Depending on the kind of memory segment being closed, calling this method further triggers deallocation of all the resources
     * associated with the memory segment.
     * @throws IllegalStateException if this segment is not <em>alive</em>, or if access occurs from a thread other than the
     * thread owning this segment, or if the segment cannot be closed because it is being operated upon by a different
     * thread (see {@link MemorySegments#spliterator(MemorySegment, SequenceLayout)}).
     * @throws UnsupportedOperationException if this segment does not support the {@link #CLOSE} access mode.
     */
    void close();

    // access mode masks

    /**
     * Read access mode; read operations are supported by a segment which supports this access mode.
     * @see MemorySegment#accessModes()
     * @see MemorySegment#withAccessModes(int)
     */
    int READ = 1;

    /**
     * Write access mode; write operations are supported by a segment which supports this access mode.
     * @see MemorySegment#accessModes()
     * @see MemorySegment#withAccessModes(int)
     */
    int WRITE = READ << 1;

    /**
     * Close access mode; calling {@link #close()} is supported by a segment which supports this access mode.
     * @see MemorySegment#accessModes()
     * @see MemorySegment#withAccessModes(int)
     */
    int CLOSE = WRITE << 1;

    /**
     * Acquire access mode; this segment support sharing with threads other than the owner thread, via spliterator
     * (see {@link MemorySegments#spliterator(MemorySegment, SequenceLayout)}).
     * @see MemorySegment#accessModes()
     * @see MemorySegment#withAccessModes(int)
     */
    int ACQUIRE = CLOSE << 1;

    /**
     * Handoff access mode; this segment support serial thread-confinement via thread ownership changes
     * (see {@link #withOwnerThread(Thread)}).
     * @see MemorySegment#accessModes()
     * @see MemorySegment#withAccessModes(int)
     */
    int HANDOFF = ACQUIRE << 1;

    /**
     * Default access mode; this is a union of all the access modes supported by memory segments.
     * @see MemorySegment#accessModes()
     * @see MemorySegment#withAccessModes(int)
     */
    int ALL_ACCESS = READ | WRITE | CLOSE | ACQUIRE | HANDOFF;
}
