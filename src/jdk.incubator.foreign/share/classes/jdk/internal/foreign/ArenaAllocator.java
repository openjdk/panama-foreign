package jdk.internal.foreign;

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SegmentAllocator;
import jdk.incubator.foreign.ResourceScope;

import java.util.OptionalLong;

public class ArenaAllocator implements SegmentAllocator {

    private final SegmentAllocator allocator;
    private MemorySegment segment;

    private static final long BLOCK_SIZE = 4 * 1024;
    private static final long MAX_ALLOC_SIZE = BLOCK_SIZE / 2;

    private long sp = 0L;

    public ArenaAllocator(ResourceScope scope) {
        this(BLOCK_SIZE, scope);
    }

    ArenaAllocator(long initialSize, ResourceScope scope) {
        this.allocator = SegmentAllocator.malloc(() -> scope);
        this.segment = allocator.allocate(initialSize, 1);
    }

    MemorySegment newSegment(long size, long align) {
        MemorySegment segment = allocator.allocate(size, align);
        return segment;
    }

    @Override
    public synchronized MemorySegment allocate(long bytesSize, long bytesAlignment) {
        if (Utils.alignUp(bytesSize, bytesAlignment) > MAX_ALLOC_SIZE) {
            return newSegment(bytesSize, bytesAlignment);
        }
        // try to slice from current segment first...
        MemorySegment slice = trySlice(bytesSize, bytesAlignment);
        if (slice == null) {
            // ... if that fails, allocate a new segment and slice from there
            sp = 0L;
            segment = newSegment(BLOCK_SIZE, 1L);
            slice = trySlice(bytesSize, bytesAlignment);
            if (slice == null) {
                // this should not be possible - allocations that do not fit in BLOCK_SIZE should get their own
                // standalone segment (see above).
                throw new AssertionError("Cannot get here!");
            }
        }
        return slice;
    }

    private MemorySegment trySlice(long bytesSize, long bytesAlignment) {
        long min = segment.address().toRawLongValue();
        long start = Utils.alignUp(min + sp, bytesAlignment) - min;
        if (segment.byteSize() - start < bytesSize) {
            return null;
        } else {
            MemorySegment slice = segment.asSlice(start, bytesSize);
            sp = start + bytesSize;
            return slice;
        }
    }

    public static class BoundedArenaAllocator extends ArenaAllocator {

        public BoundedArenaAllocator(ResourceScope scope, long size) {
            super(size, scope);
        }

        @Override
        MemorySegment newSegment(long size, long align) {
            throw new OutOfMemoryError("Not enough space left to allocate");
        }
    }
}
