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
 * A memory segment is a view over a given memory source (see {@link MemorySource}), which is associated with both spatial
 * and temporal <em>bounds</em>. Spatial bounds ensure that memory access operations on a memory segment cannot affect a memory location
 * which falls <em>outside</em> the boundaries of the memory segment being accessed. Temporal checks ensure that memory access
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
 * be allocated using one of the many factory methods provided (see {@link MemorySegment#allocateNative(MemoryLayout)},
 * {@link MemorySegment#allocateNative(long)} and {@link MemorySegment#allocateNative(long, long)}). Memory segments obtained
 * in this way are backed by a <em>native</em> memory source (see {@link MemorySource}).
 * <p>
 * It is also possible to obtain a memory segment backed by an existing heap-allocated Java array,
 * using one of the provided factory methods (e.g. {@link MemorySegment#ofArray(int[])}). Memory segments obtained
 * in this way are backed by a <em>heap</em> memory source (see {@link MemorySource}).
 * <p>
 * It is possible to obtain a memory segment backed by a memory-mapped file using the factory method
 * {@link MemorySegment#mapFromPath(Path, long, FileChannel.MapMode)}. Such memory segments are backed by a
 * <em>mapped</em> memory source (see {@link MappedMemorySource}).
 * <p>
 * Finally, it is also possible to obtain a memory segment backed by an existing Java byte buffer (see {@link ByteBuffer}),
 * using the factory method {@link MemorySegment#ofByteBuffer(ByteBuffer)}. Memory segments obtained in this can be
 * backed by a native memory source, an heap memory source, or a mapped memory source, depending on the characteristics
 * of the byte buffer instance the segment is associated with. For instance, a memory segment obtained from a byte buffer
 * created with the {@link ByteBuffer#allocateDirect(int)} method will be backed by a native memory source; a memory segment
 * obtained from a byte buffer created with the {@link ByteBuffer#allocate(int)} will be backed by a heap memory source;
 * finally, a memory segment obtained from a byte buffer created with the {@link FileChannel#map(FileChannel.MapMode, long, long)} method
 * will be backed by a mapped memory source.
 *
 * <h2>Closing a memory segment</h2>
 *
 * Memory segments are closed explicitly (see {@link MemorySegment#close()}). When a memory segment is closed,
 * the segment, and all memory addresses (see {@link MemoryAddress}) derived from it are rendered invalid. This means
 * that e.g. dereferencing a memory address derived from a segment that has been closed will result in a runtime
 * exception. Closing a memory segment <em>might</em> additionally result in <a href="MemorySource.html#releasing-sources">
 * releasing</a> the memory source backing that memory segment.
 *
 * <h2><a id = "thread-confinement">Thread confinement</a></h2>
 *
 * Memory segments support strong thread-confinement guarantees. Upon creation, they are assigned an <em>owner thread</em>,
 * typically the thread which initiated the creation operation. After creation, only the owner thread will be allowed
 * to directly manipulate the memory segment (e.g. close the memory segment) or access the underlying memory associated with
 * the segment using a memory access var handle. Any attempt to perform such operations from a thread other than the
 * owner thread will result in a runtime failure.
 * <p>
 * If a memory segment S owned by thread A needs to be used by thread B, B needs to explicitly <em>acquire</em> S,
 * which will create a new memory segment owned by B (see {@link #acquire()}) backed by the same memory
 * source as S. A memory segment can be acquired multiple times by one or more threads; in that case, the underlying memory
 * source will not be released until <em>all</em> the memory segments derived from it have been closed.
 *
 * <h2><a id = "access-modes">Access modes</a></h2>
 *
 * Memory segments supports zero or more <em>access modes</em>. Supported access modes are {@link #READ},
 * {@link #WRITE}, {@link #CLOSE} and {@link #ACQUIRE}. The set of access modes supported by a segment alters the
 * set of operations that are supported by that segment. For instance, attempting to call {@link #close()} on
 * a segment which does not support the {@link #CLOSE} access mode will result in an exception.
 * <p>
 * The set of supported access modes can only be made stricter (by supporting <em>less</em> access modes). This means
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
     * Obtains a new memory segment backed by the same memory source as this segment which can be used to access memory associated
     * with this segment from the current thread.
     * @return a new memory segment backed by the same memory source as this segment which can be used to access memory associated
     * with this segment from the current thread.
     * @throws IllegalStateException if the memory source associated with this segment has been released (see {@link MemorySource#isReleased()}).
     */
    MemorySegment acquire();

    /**
     * The thread owning this segment.
     * @return the thread owning this segment.
     */
    Thread ownerThread();

    /**
     * The size (in bytes) of this memory segment.
     * @return The size (in bytes) of this memory segment.
     */
    long byteSize();

    /**
     * Obtains a segment view with specific <a href="#access-modes">access modes</a>. Supported access modes are {@link #READ}, {@link #WRITE},
     * {@link #CLOSE} and {@link #ACQUIRE}. It is generally not possible to go from a segment with stricter access modes
     * to one with less strict access modes. For instance, attempting to add {@link #WRITE} access mode to a read-only segment
     * will be met with an exception.
     * @param accessModes an ORed mask of zero or more access modes.
     * @return a segment view with specific access modes.
     * @throws UnsupportedOperationException when {@code mask} is an access mask which is less strict than the one supported by this
     * segment.
     */
    MemorySegment withAccessModes(int accessModes);

    /**
     * Does this segment support a given set of access modes?
     * @param accessModes an ORed mask of zero or more access modes.
     * @return true, if the access modes in {@code accessModes} are stricter than the ones supported by this segment.
     */
    boolean hasAccessModes(int accessModes);

    /**
     * Returns the <a href="#access-modes">access modes</a> associated with this segment; the result is represented as ORed values from
     * {@link #READ}, {@link #WRITE}, {@link #CLOSE} and {@link #ACQUIRE}.
     * @return the access modes associated with this segment.
     */
    int accessModes();

    /**
     * Returns the memory source associated with this segment.
     * @return the memory source associated with this segment.
     */
    MemorySource source();

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
     * or to access the memory associated with the segment will fail with {@link IllegalStateException}. Depending on
     * the kind of memory segment being closed, calling this method further trigger deallocation of all the resources
     * associated with the memory segment.
     * @throws IllegalStateException if this segment is not <em>alive</em>, or if access occurs from a thread other than the
     * thread owning this segment.
     * @throws UnsupportedOperationException if this segment does not support the {@link #CLOSE} access mode.
     */
    void close();

    /**
     * Wraps this segment in a {@link ByteBuffer}. Some of the properties of the returned buffer are linked to
     * the properties of this segment. For instance, if this segment is <em>immutable</em>
     * (e.g. the segment has access mode {@link #READ} but not {@link #WRITE}), then the resulting buffer is <em>read-only</em>
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
     * than {@link Integer#MAX_VALUE}, or if the segment does not support the {@link #READ} access mode.
     */
    ByteBuffer asByteBuffer();

    /**
     * Copy the contents of this memory segment into a fresh byte array.
     * @return a fresh byte array copy of this memory segment.
     * @throws UnsupportedOperationException if this segment's contents cannot be copied into a {@link byte[]} instance,
     * e.g. its size is greater than {@link Integer#MAX_VALUE}.
     * @throws IllegalStateException if this segment has been closed, or if access occurs from a thread other than the
     * thread owning this segment.
     */
    byte[] toByteArray();

    /**
     * Creates a new memory segment view of the memory associated with a given byte buffer.
     * The segment starts relative to the buffer's position (inclusive) and ends relative to the buffer's limit (exclusive).
     * <p>
     * The resulting memory segment keeps a reference to the backing buffer, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param bb the byte buffer backing the buffer memory segment.
     * @return a new memory segment. The memory source of the returned segment depends on the characteristics of {@code bb}. More specifically,
     * if {@code bb} has been created with the {@link ByteBuffer#allocateDirect(int)} method, the resulting segment will be
     * backed by a native memory source; if {@code bb} has been created with the {@link ByteBuffer#allocate(int)} method,
     * the resulting sement will be backed by a heap memory source; finally, if {@code bb} has been created with the
     * {@link FileChannel#map(FileChannel.MapMode, long, long)} method, the resulting segment  will be backed by a mapped memory source.
     */
    static MemorySegment ofByteBuffer(ByteBuffer bb) {
        return Utils.makeBufferSegment(bb);
    }

    /**
     * Creates a new memory segment view of the memory associated with a given heap-allocated byte array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new memory segment backed by a heap memory source.
     */
    static MemorySegment ofArray(byte[] arr) {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new memory segment view of the memory associated with a given heap-allocated char array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new memory segment backed by a heap memory source.
     */
    static MemorySegment ofArray(char[] arr) {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new memory segment view of the memory associated with a given heap-allocated short array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new memory segment backed by a heap memory source.
     */
    static MemorySegment ofArray(short[] arr) {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new memory segment view of the memory associated with a given heap-allocated int array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new memory segment backed by a heap memory source.
     */
    static MemorySegment ofArray(int[] arr) {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new memory segment view of the memory associated with a given heap-allocated float array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new memory segment backed by a heap memory source.
     */
    static MemorySegment ofArray(float[] arr) {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new memory segment view of the memory associated with a given heap-allocated long array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new memory segment backed by a heap memory source.
     */
    static MemorySegment ofArray(long[] arr) {
        return Utils.makeArraySegment(arr);
    }

    /**
     * Creates a new memory segment view of the memory associated with a given heap-allocated double array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new memory segment backed by a heap memory source.
     */
    static MemorySegment ofArray(double[] arr) {
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
     * @implNote The block of off-heap memory associated with the returned native memory segment is initialized to zero.
     * Moreover, a client is responsible to call the {@link MemorySegment#close()} on a native memory segment,
     * to make sure the backing off-heap memory block is deallocated accordingly. Failure to do so will result in off-heap memory leaks.
     *
     * @param layout the layout of the off-heap memory block backing the native memory segment.
     * @return a new memory segment backed by a native memory source.
     * @throws IllegalArgumentException if the specified layout has illegal size or alignment constraint.
     */
    static MemorySegment allocateNative(MemoryLayout layout) {
        return allocateNative(layout.byteSize(), layout.byteAlignment());
    }

    /**
     * Creates a new native memory segment that models a newly allocated block of off-heap memory with given size (in bytes).
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
allocateNative(bytesSize, 1);
     * }</pre></blockquote>
     *
     * @implNote The block of off-heap memory associated with the returned native memory segment is initialized to zero.
     * Moreover, a client is responsible to call the {@link MemorySegment#close()} on a native memory segment,
     * to make sure the backing off-heap memory block is deallocated accordingly. Failure to do so will result in off-heap memory leaks.
     *
     * @param bytesSize the size (in bytes) of the off-heap memory block backing the native memory segment.
     * @return a new memory segment backed by a native memory source.
     * @throws IllegalArgumentException if {@code bytesSize < 0}.
     */
    static MemorySegment allocateNative(long bytesSize) {
        return allocateNative(bytesSize, 1);
    }

    /**
     * Creates a new mapped memory segment that models a memory-mapped region of a file from a given path.
     *
     * @implNote When obtaining a mapped segment from a newly created file, the initialization state of the contents of the block
     * of mapped memory associated with the returned mapped memory segment is unspecified and should not be relied upon.
     *
     * @param path the path to the file to memory map.
     * @param bytesSize the size (in bytes) of the mapped memory backing the memory segment.
     * @param mapMode a file mapping mode, see {@link FileChannel#map(FileChannel.MapMode, long, long)}.
     * @return a new memory segment backed by a mapped memory source.
     * @throws IllegalArgumentException if {@code bytesSize < 0}.
     * @throws UnsupportedOperationException if an unsupported map mode is specified.
     * @throws IOException if the specified path does not point to an existing file, or if some other I/O error occurs.
     */
    static MemorySegment mapFromPath(Path path, long bytesSize, FileChannel.MapMode mapMode) throws IOException {
        return Utils.makeMappedSegment(path, bytesSize, mapMode);
    }

    /**
     * Creates a new native memory segment that models a newly allocated block of off-heap memory with given size and
     * alignment constraint (in bytes).
     *
     * @implNote The block of off-heap memory associated with the returned native memory segment is initialized to zero.
     * Moreover, a client is responsible to call the {@link MemorySegment#close()} on a native memory segment,
     * to make sure the backing off-heap memory block is deallocated accordingly. Failure to do so will result in off-heap memory leaks.
     *
     * @param bytesSize the size (in bytes) of the off-heap memory block backing the native memory segment.
     * @param alignmentBytes the alignment constraint (in bytes) of the off-heap memory block backing the native memory segment.
     * @return a new memory segment backed by a native memory source.
     * @throws IllegalArgumentException if {@code bytesSize < 0}, {@code alignmentBytes < 0}, or if {@code alignmentBytes}
     * is not a power of 2.
     */
    static MemorySegment allocateNative(long bytesSize, long alignmentBytes) {
        if (bytesSize <= 0) {
            throw new IllegalArgumentException("Invalid allocation size : " + bytesSize);
        }

        if (alignmentBytes < 0 ||
                ((alignmentBytes & (alignmentBytes - 1)) != 0L)) {
            throw new IllegalArgumentException("Invalid alignment constraint : " + alignmentBytes);
        }

        return Utils.makeNativeSegment(bytesSize, alignmentBytes);
    }

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
     * Acquire access mode; calling {@link #acquire()} is supported by a segment which supports this access mode.
     * @see MemorySegment#accessModes()
     * @see MemorySegment#withAccessModes(int)
     */
    int ACQUIRE = CLOSE << 1;
}
