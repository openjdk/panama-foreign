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

import jdk.internal.foreign.Utils;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 * A memory segment models a contiguous region of memory. A memory segment is associated with both spatial
 * and temporal bounds. Spatial bounds make sure that memory access operatons on a memory segment cannot affect a memory location
 * which falls <em>outside</em> the boundaries of the memory segment being accessed. Temporal checks make sure that memory access
 * operations on a segment cannot occur after a memory segment has already been closed (see {@link MemorySegment#close()}).
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
 * be allocated using one of the many factory methods provided (see {@link MemorySegment#allocateNative(MemoryLayout)},
 * {@link MemorySegment#allocateNative(long)} and {@link MemorySegment#allocateNative(long, long)}). Memory segments obtained
 * in this way are called <em>native memory segments</em>.
 * <p>
 * It is also possible to obtain a memory segment backed by an existing heap-allocated Java array,
 * using one of the provided factory methods (e.g. {@link MemorySegment#ofArray(int[])}). Memory segments obtained
 * in this way are called <em>array memory segments</em>.
 * <p>
 * It is possible to obtain a memory segment backed by an existing Java byte buffer (see {@link ByteBuffer}),
 * using the factory method {@link MemorySegment#ofByteBuffer(ByteBuffer)}.
 * Memory segments obtained in this way are called <em>buffer memory segments</em>. Note that buffer memory segments might
 * be backed by native memory (as in the case of native memory segments), heap memory (as in the case of array memory segments)
 * depending on the characteristics of the byte buffer instance the segment is associated with. For instance, a buffer memory
 * segment obtained from a byte buffer created with the {@link ByteBuffer#allocateDirect(int)} method will be backed
 * by native memory.
 * <p>
 * Finally, it is also possible to obtain a memory segment backed by a memory-mapped file using the factory method
 * {@link MemorySegment#mapFromPath(Path, long, FileChannel.MapMode)}. Such memory segments are called <em>mapped memory segments</em>.
 *
 * <h2>Closing a memory segment</h2>
 *
 * <a href="./package-summary.html#concurrency">Confined memory segments</a> can be closed explicitly (see {@link MemorySegment#close()}).
 * When a confined segment is closed, all off-heap resources associated with it are released; this has different meanings depending on the kind of
 * memory segment being considered:
 * <ul>
 *     <li>closing a native memory segment results in <em>freeing</em> the native memory associated with it</li>
 *     <li>closing a mapped memory segment results in the backing memory-mapped file to be unmapped</li>
 * </ul>
 *
 * Closing a buffer, or a heap segment does not have any side-effect, other than making the marking the segment as
 * <em>not alive</em> (see {@link MemorySegment#isAlive()}). Also, since the buffer and heap segments might keep strong
 * references to the original buffer or array instance, it is the responsibility of clients to ensure that these
 * segments are discarded in a timely manner, so as not to prevent garbage collection to reclaim the underlying
 * objects.
 *
 * Conversely, <a href="./package-summary.html#concurrency">shared memory segments</a> cannot be closed explicitly;
 * resources associated with a given shared segment will be released when such a segment is deemed <em>unreacheable</em>
 * by the garbage collector.
 *
 * <h2><a id = "thread-confinement">Thread confinement</a></h2>
 *
 * Memory segments support strong thread-confinement guarantees. Upon creation, they are assigned an <em>owner thread</em>,
 * typically the thread which initiated the creation operation. After creation, only the owner thread will be allowed
 * to directly manipulate the memory segment (e.g. close the memory segment) or access the underlying memory associated with
 * the segment using a memory access var handle. Any attempt to perform such operations from a thread other than the
 * owner thread will result in a runtime failure.
 *
 * If a memory segment S owned by thread A needs to be used by thread B, B needs to explicitly <em>acquire</em> S,
 * which will create a so called <em>acquired</em> memory segment owned by B (see {@link #acquire()}) backed by the same resources
 * as S. A memory segment can be acquired multiple times by one or more threads; in that case, a memory segment S cannot
 * be closed until all the acquired memory segments derived from S have been closed. Furthermore, closing an acquired
 * memory segment does <em>not</em> trigger any deallocation action. It is therefore important that clients remember to
 * explicitly close the original segment from which the acquired memory segments have been obtained in order to truly
 * ensure that off-heap resources associated with the memory segment are released.
 *
 * <h2>Memory segment views</h2>
 *
 * Memory segments support <em>views</em>. It is possible to create an <em>immutable</em> view of a memory segment
 * (see {@link MemorySegment#asReadOnly()}) which does not support write operations. It is also possible to create views
 * whose spatial bounds are stricter than the ones of the original segment (see {@link MemorySegment#asSlice(long, long)}).
 * <p>
 * Temporal bounds of the original segment are inherited by the view; that is, closing a resized segment view
 * will cause the whole segment to be closed; as such special care must be taken when sharing views
 * between multiple clients. If a client want to protect itself against early closure of a segment by
 * another actor, it is the responsibility of that client to take protective measures, such as calling
 * {@link MemorySegment#acquire()} before sharing the view with another client.
 * <p>
 * To allow for interoperability with existing code, a byte buffer view can be obtained from a memory segment
 * (see {@link #asByteBuffer()}). This can be useful, for instance, for those clients that want to keep using the
 * {@link ByteBuffer} API, but need to operate on large memory segments. Byte buffers obtained in such a way support
 * the same spatial and temporal access restrictions associated to the memory address from which they originated.
 *
 * @apiNote In the future, if the Java language permits, {@link MemorySegment}
 * may become a {@code sealed} interface, which would prohibit subclassing except by
 * explicitly permitted types.
 *
 * @implSpec
 * Implementations of this interface are immutable and thread-safe.
 */
public interface MemorySegment extends AutoCloseable {

    /**
     * The base memory address associated with this memory segment.
     * @return The base memory address.
     */
    MemoryAddress baseAddress();

    /**
     * Obtains an <a href="#thread-confinement">acquired</a> memory segment which can be used to access memory associated
     * to this segment from the current thread. As a side-effect, the current segment cannot be closed until the acquired
     * view has been closed too (see {@link #close()}).
     * @return an <a href="#thread-confinement">acquired</a> memory segment which can be used to access memory associated
     * to this segment from the current thread.
     * @throws IllegalStateException if this segment has been closed.
     */
    MemorySegment acquire() throws IllegalStateException;

    /**
     * Is this segment accessible from the current thread?
     * @return true, if this segment is accessible from the current thread.
     */
    boolean isAccessible();

    /**
     * The size (in bytes) of this memory segment.
     * @return The size (in bytes) of this memory segment.
     */
    long byteSize();

    /**
     * Obtains a read-only view of this segment. An attempt to write memory associated with a read-only memory segment
     * will fail with {@link UnsupportedOperationException}.
     * @return a read-only view of this segment.
     * @throws IllegalStateException if this segment has been closed, or if access occurs from a thread other than the
     * thread owning this segment.
     */
    MemorySegment asReadOnly() throws IllegalStateException;

    /**
     * Obtains a new memory segment view whose base address is the same as the base address in this segment plus a given offset,
     * and whose new size is specified by the given argument.
     * @param offset The new segment base offset (relative to the current segment base address), specified in bytes.
     * @param newSize The new segment size, specified in bytes.
     * @return a new address with updated base/limit addresses.
     * @throws IllegalArgumentException if the new segment bounds are illegal; this can happen because:
     * <ul>
     * <li>either {@code offset} or {@code newSize} are &lt; 0</li>
     * <li>{@code offset} is bigger than the current segment size (see {@link #byteSize()}
     * <li>{@code newSize} is bigger than the current segment size (see {@link #byteSize()}
     * </ul>
     * @throws IllegalStateException if this segment has been closed, or if access occurs from a thread other than the
     * thread owning this segment.
     */
    MemorySegment asSlice(long offset, long newSize) throws IllegalArgumentException, IllegalStateException;

    /**
     * Is this segment alive?
     * @return true, if the segment is alive.
     * @see MemorySegment#close()
     */
    boolean isAlive();

    /**
     * Is this segment read-only?
     * @return true, if the segment is read-only.
     * @see MemorySegment#asReadOnly()
     */
    boolean isReadOnly();

    /**
     * Closes this memory segment. Once a memory segment has been closed, any attempt to use the memory segment,
     * or to access the memory associated with the segment will fail with {@link IllegalStateException}. Depending on
     * the memory segment being closed, calling this method further trigger deallocation of all the resources associated
     * with the memory segment.
     * @throws UnsupportedOperationException if the segment cannot be closed e.g. because existing acquired views of this
     * segment are still in use (see {@link MemorySegment#acquire()}).
     * @throws IllegalStateException if this segment has been closed, or if access occurs from a thread other than the
     * thread owning this segment.
     */
    void close() throws UnsupportedOperationException, IllegalStateException;

    /**
     * Wraps this segment in a {@link ByteBuffer}. Some of the properties of the returned buffer are linked to
     * the properties of this segment. For instance, if this segment is <em>immutable</em>
     * (see {@link MemorySegment#asReadOnly()}, then the resulting buffer is <em>read-only</em>
     * (see {@link ByteBuffer#isReadOnly()}. Additionally, if this is a native memory segment, the resulting buffer is
     * <em>direct</em> (see {@link ByteBuffer#isDirect()}).
     * <p>
     * The life-cycle of the returned buffer will be tied to that of this segment. That means that if the this segment
     * is closed (see {@link MemorySegment#close()}, accessing the returned
     * buffer will throw an {@link IllegalStateException}.
     * <p>
     * The resulting buffer's byte order is {@link java.nio.ByteOrder#BIG_ENDIAN}; this can be changed using
     * {@link ByteBuffer#order(java.nio.ByteOrder)}.
     *
     * @return a {@link ByteBuffer} view of this memory segment.
     * @throws UnsupportedOperationException if this segment cannot be mapped onto a {@link ByteBuffer} instance,
     * e.g. because it models an heap-based segment that is not based on a {@code byte[]}), or if its size is greater
     * than {@link Integer#MAX_VALUE}.
     * @throws IllegalStateException if this segment has been closed, or if access occurs from a thread other than the
     * thread owning this segment.
     */
    ByteBuffer asByteBuffer() throws UnsupportedOperationException, IllegalStateException;

    /**
     * Copy the contents of this memory segment into a fresh byte array.
     * @return a fresh byte array copy of this memory segment.
     * @throws UnsupportedOperationException if this segment cannot be mapped onto a {@link byte[]} instance,
     * e.g. its size is greater than {@link Integer#MAX_VALUE}.
     * @throws IllegalStateException if this segment has been closed, or if access occurs from a thread other than the
     * thread owning this segment.
     */
    byte[] toByteArray() throws UnsupportedOperationException, IllegalStateException;

    /**
     * Creates a new buffer memory segment that models the memory associated with the given byte
     * buffer. The segment starts relative to the buffer's position (inclusive)
     * and ends relative to the buffer's limit (exclusive).
     * <p>
     * The resulting memory segment keeps a reference to the backing buffer, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param bb the byte buffer backing the buffer memory segment.
     * @return a new buffer memory segment.
     */
    static MemorySegment ofByteBuffer(ByteBuffer bb) {
        return Utils.makeBufferSegment(bb);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated byte array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    static MemorySegment ofArray(byte[] arr) throws IllegalArgumentException {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated char array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    static MemorySegment ofArray(char[] arr) throws IllegalArgumentException {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated short array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    static MemorySegment ofArray(short[] arr) throws IllegalArgumentException {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated int array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    static MemorySegment ofArray(int[] arr) throws IllegalArgumentException {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated float array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    static MemorySegment ofArray(float[] arr) throws IllegalArgumentException {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated long array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    static MemorySegment ofArray(long[] arr) throws IllegalArgumentException {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated double array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    static MemorySegment ofArray(double[] arr) throws IllegalArgumentException {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new native memory segment that models a newly allocated block of off-heap memory with given layout.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    allocateNative(layout.bytesSize(), layout.bytesAlignment());
     * }</pre></blockquote>
     *
     * @implNote The initialization state of the contents of the block of off-heap memory associated with the returned native memory
     * segment is unspecified and should not be relied upon. Moreover, a client is responsible to call the {@link MemorySegment#close()}
     * on a native memory segment, to make sure the backing off-heap memory block is deallocated accordingly. Failure to do so
     * will result in off-heap memory leaks.
     *
     * @param layout the layout of the off-heap memory block backing the native memory segment.
     * @return a new native memory segment.
     * @throws UnsupportedOperationException if the specified layout has illegal size or alignment constraints.
     * @throws RuntimeException if the specified size is too large for the system runtime.
     * @throws OutOfMemoryError if the allocation of the off-heap memory block is refused by the system runtime.
     */
    static MemorySegment allocateNative(MemoryLayout layout) throws IllegalArgumentException {
        return allocateNative(layout.byteSize(), layout.byteAlignment());
    }

    /**
     * Creates a new native memory segment that models a newly allocated block of off-heap memory with given size (in bytes).
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    allocateNative(bitsSize, 1);
     * }</pre></blockquote>
     *
     * @param bytesSize the size (in bytes) of the off-heap memory block backing the native memory segment.
     * @return a new native memory segment.
     * @throws IllegalArgumentException if specified size is &lt; 0.
     * @throws RuntimeException if the specified size is too large for the system runtime.
     * @throws OutOfMemoryError if the allocation of the off-heap memory block is refused by the system runtime.
     *
     * @implNote The initialization state of the contents of the block of off-heap memory associated with the returned native memory
     * segment is unspecified and should not be relied upon. Moreover, a client is responsible to call the {@link MemorySegment#close()}
     * on a native memory segment, to make sure the backing off-heap memory block is deallocated accordingly. Failure to do so
     * will result in off-heap memory leaks.
     */
    static MemorySegment allocateNative(long bytesSize) throws IllegalArgumentException {
        return allocateNative(bytesSize, 1);
    }

    /**
     * Creates a new mapped memory segment that models a newly allocated block of memory of the given size, mapped from the given path.
     *
     * @param path the path to the resource to memory map.
     * @param bytesSize the size (in bytes) of the mapped memory backing the memory segment.
     * @param mapMode a file mapping mode, see {@link FileChannel#map(FileChannel.MapMode, long, long)}.
     * @return a new mapped memory segment.
     * @throws IllegalArgumentException if specified size is &lt; 0.
     * @throws UnsupportedOperationException if an unsupported map mode is specified.
     * @throws IOException if the specified path does not point to an existing file, or if some other I/O error occurs.
     *
     * @implNote When obtaining a mapped segment from a newly created file, the initialization state of the contents of the block
     * of mapped memory associated with the returned mapped memory segment is unspecified and should not be relied upon.
     */
    static MemorySegment mapFromPath(Path path, long bytesSize, FileChannel.MapMode mapMode) throws IllegalArgumentException, IOException {
        return Utils.makeMappedSegment(path, bytesSize, mapMode);
    }

    /**
     * Creates a new native memory segment that models a newly allocated block of off-heap memory with given size and alignment (in bytes).
     * @param bytesSize the size (in bytes) of the off-heap memory block backing the native memory segment.
     * @param alignmentBytes the alignment (in bytes) of the off-heap memory block backing the native memory segment.
     * @return a new native memory segment.
     * @throws IllegalArgumentException if either specified size or alignment are &lt; 0, or if the alignment constraint
     * is not a power of 2.
     * @throws RuntimeException if the specified size is too large for the system runtime.
     * @throws OutOfMemoryError if the allocation of the off-heap memory block is refused by the system runtime.
     *
     * @implNote The initialization state of the contents of the block of off-heap memory associated with the returned native memory
     * segment is unspecified and should not be relied upon. Moreover, a client is responsible to call the {@link MemorySegment#close()}
     * on a native memory segment, to make sure the backing off-heap memory block is deallocated accordingly. Failure to do so
     * will result in off-heap memory leaks.
     */
    static MemorySegment allocateNative(long bytesSize, long alignmentBytes) throws IllegalArgumentException {
        if (bytesSize <= 0) {
            throw new IllegalArgumentException("Invalid allocation size : " + bytesSize);
        }

        if (alignmentBytes < 0 ||
                ((alignmentBytes & (alignmentBytes - 1)) != 0L)) {
            throw new IllegalArgumentException("Invalid alignment constraint : " + alignmentBytes);
        }

        return Utils.makeNativeSegment(bytesSize, alignmentBytes);
    }
}
