package java.foreign;

import jdk.internal.foreign.BufferScope;
import jdk.internal.foreign.HeapScope;
import jdk.internal.foreign.NativeScope;

import java.nio.ByteBuffer;

/**
 * A memory segment models a contiguous region of memory. A memory segment is associated with both spatial
 * and temporal bounds. Spatial bounds make sure that it is not possible for an address to refer to an area
 * outside of its owning memory segment. Temporal checks make sure that an address cannot perform operation on
 * a memory segment which is no longer available.
 * <p>
 * Memory segments support <em>views</em>, that is, it is possible to create immutable view of a memory segment
 * (see {@link MemorySegment#asReadOnly()}) which does not support write operations. It is also possible
 * to create a <em>pinned</em> view of a memory segment (see {@link MemorySegment#asPinned()}), which cannot be
 * closed (see {@link MemorySegment#close()}). Finally, it is possible to create views whose spatial bounds
 * are stricter (see {@link MemorySegment#resize(long, long)}).
 * <p>
 * Temporal bounds of the original segment are inherited by the view; that is, closing a resized segment view
 * will cause the whole segment to be closed; as such special care must be taken when sharing views
 * between multiple clients. If a client want to protect itself against early closure of a segment by
 * another actor, it is the responsibility of that client to take protective measures, such as calling
 * {@link MemorySegment#asPinned()} before sharing the view with another client.
 */
public interface MemorySegment extends AutoCloseable {

    /**
     * The base memory address associated with this memory segment.
     * @return The base memory address.
     */
    MemoryAddress baseAddress();

    /**
     * Creates a new memory address whose base address is the same as this address plus a given offset,
     * and whose new size is specified by the given argument.
     * @param offset The new segment base offset (relative to the current segment base), specified in bytes.
     * @param newSize The new segment size, specified in bytes.
     * @return a new address with updated base/limit addresses.
     * @throws IllegalArgumentException if the new segment bounds are illegal; this can happen because:
     * <ul>
     * <li>either {@code offset} or {@code newSize} are &lt; 0</li>
     * <li>{@code offset} is bigger than the current segment size (see {@link #bytesSize()}.
     * <li>{@code newSize} is bigger than the current segment size (see {@link #bytesSize()}
     * </ul>
     */
    MemorySegment resize(long offset, long newSize) throws IllegalArgumentException;

    /**
     * The size (in bytes) of this memory segment.
     * @return The size (in bytes) of this memory segment.
     */
    long bytesSize();

    /**
     * Obtains a read-only view of this segment.
     * @return a read-only view of this segment.
     */
    MemorySegment asReadOnly();

    /**
     * Obtains a pinned view of this segment - that is a view that does not support calls to the
     * the {@link MemorySegment#close()} method.
     * @return a pinned view of this segment.
     */
    MemorySegment asPinned();

    /**
     * Is this segment alive?
     * @return true, if the segment is alive?
     * @see MemorySegment#close()
     */
    boolean isAlive();

    /**
     * Is this segment pinned - that is, does it allow for the segment to be closed using
     * the {@link MemorySegment#close()} method?
     * @return true, if the segment is pinned?
     * @see MemorySegment#asReadOnly()
     */
    boolean isPinned();

    /**
     * Is this segment read-only?
     * @return true, if the segment does not support write operations.
     */
    boolean isReadOnly();

    /**
     * Closes this memory segment, and releases any resources allocated with it.
     * @throws UnsupportedOperationException if the segment cannot be closed (e.g. because the segment is pinned)
     * @see MemorySegment#isPinned()
     */
    void close() throws UnsupportedOperationException;

    /**
     * Returns a memory segment that models the memory associated with the given byte
     * buffer. The segment starts relative to the buffer's position (inclusive)
     * and ends relative to the buffer's limit (exclusive).
     * <p>
     * The resulting segment keeps a reference to the buffer to ensure the buffer is kept
     * live for the life-time of the segment; also, the resulting segment is allocated on a <em>pinned</em> scope
     * which cannot be closed.
     * <p>
     *
     * @param bb the byte buffer
     * @return the created segment
     */
    static MemorySegment ofByteBuffer(ByteBuffer bb) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.foreign.Pointer.fromByteBuffer"));
        }
        return BufferScope.of(bb);
    }

    /**
     * Returns a memory segment that models the memory associated with a given heap-allocated primitive array.
     * <p>
     * The resulting segment keeps a reference to the array to ensure the underlying array is kept
     * live for the life-time of the segment; also, the resulting segment is allocated on a <em>pinned</em> scope
     * which cannot be closed.
     *
     * @param arr the primitive array
     * @return the created segment
     * @throws IllegalArgumentException if the array is not a primitive type array, or if the number of dimension in
     * the array is greater than 1.
     */
    static MemorySegment ofArray(Object arr) throws IllegalArgumentException {
        if (!arr.getClass().isArray() ||
                !arr.getClass().componentType().isPrimitive()) {
            throw new IllegalArgumentException("Not a primitive array");
        }
        return HeapScope.ofArray(arr);
    }

    /**
     * Allocate region of memory with given {@code LayoutType}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
ofNative(layout.bitsSize() / 8, layout.alignmentInBits() / 8);
     * }</pre></blockquote>
     *
     * @param layout the memory layout to be allocated.
     * @return the newly allocated memory segment.
     * @throws IllegalArgumentException if the specified layout has illegal size or alignment constraints.
     * @throws RuntimeException if the specified size is too large for the system runtime.
     * @throws OutOfMemoryError if the allocation is refused by the system runtime.
     */
    static MemorySegment ofNative(Layout layout) throws IllegalArgumentException {
        if (layout.bitsSize() % 8 != 0) {
            throw new IllegalArgumentException("Layout bits size must be a multiple of 8");
        } else if (layout.alignmentBits() % 8 != 0) {
            throw new IllegalArgumentException("Layout alignment bits must be a multiple of 8");
        }
        return ofNative(layout.bitsSize() / 8, layout.alignmentBits() / 8);
    }

    /**
     * Allocate an unaligned memory segment with given size (expressed in bits).
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
ofNative(bitsSize, 1);
     * }</pre></blockquote>
     *
     * @param bytesSize the size (expressed in bytes) of the memory segment to be allocated.
     * @return the newly allocated memory segment.
     * @throws IllegalArgumentException if specified size is &lt; 0.
     * @throws RuntimeException if the specified size is too large for the system runtime.
     * @throws OutOfMemoryError if the allocation is refused by the system runtime.
     */
    static MemorySegment ofNative(long bytesSize) throws IllegalArgumentException {
        return ofNative(bytesSize, 1);
    }

    /**
     * Allocate a memory segment with given size (expressed in bits) and alignment constraints (also expressed in bits).
     * @param bytesSize the size (expressed in bits) of the memory segment to be allocated.
     * @param alignmentBytes the alignment constraints (expressed in bits) of the memory segment to be allocated.
     * @return the newly allocated memory segment.
     * @throws IllegalArgumentException if either specified size or alignment are &lt; 0, or if the alignment constraint
     * is not a power of 2.
     * @throws RuntimeException if the specified size is too large for the system runtime.
     * @throws OutOfMemoryError if the allocation is refused by the system runtime.
     */
    static MemorySegment ofNative(long bytesSize, long alignmentBytes) throws IllegalArgumentException {
        if (bytesSize <= 0) {
            throw new IllegalArgumentException("Invalid allocation size : " + bytesSize);
        }

        if (alignmentBytes < 0 ||
            ((alignmentBytes & (alignmentBytes - 1)) != 0L)) {
            throw new IllegalArgumentException("Invalid alignment constraint : " + alignmentBytes);
        }

        return NativeScope.of(bytesSize, alignmentBytes);
    }
}
