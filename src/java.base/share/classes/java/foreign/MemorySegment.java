package java.foreign;

import jdk.internal.foreign.GlobalMemoryScopeImpl;
import jdk.internal.foreign.MemorySegmentImpl;

import java.nio.ByteBuffer;

/**
 * A memory segment models a contiguous region of memory. A memory segment is associated with both spatial
 * and temporal bounds. Spatial bounds make sure that it is not possible for an address to refer to an area
 * outside of its owning memory segment. Temporal checks make sure that an address cannot perform operation on
 * a memory segment which is no longer available.
 */
public interface MemorySegment {

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
     * The scope associated with this address.
     * @return The scope associated with this address.
     */
    MemoryScope scope();

    /**
     * The size (in bytes) of this memory segment.
     * @return The size (in bytes) of this memory segment.
     */
    long bytesSize();

    /**
     * Returns a memory segment that models the memory associated with the given byte
     * buffer. The segment starts relative to the buffer's position (inclusive)
     * and ends relative to the buffer's limit (exclusive).
     * <p>
     * The resulting segment keeps a reference to the buffer to ensure the buffer is kept
     * live for the life-time of the address.
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
        return MemorySegmentImpl.ofByteBuffer(GlobalMemoryScopeImpl.UNCHECKED, bb);
    }
}
