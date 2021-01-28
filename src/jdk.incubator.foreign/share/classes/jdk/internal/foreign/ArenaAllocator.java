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
    private long size = 0L;

    public ArenaAllocator(SegmentAllocator allocator) {
        this.allocator = allocator;
        this.segment = newSegment(BLOCK_SIZE);
    }

    MemorySegment newSegment(long size, long align) {
        MemorySegment segment = allocator.allocate(size, align);
        return segment;
    }

    MemorySegment newSegment(long size) {
        return newSegment(size, size);
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
            size += Utils.alignUp(bytesSize, bytesAlignment);
            return slice;
        }
    }

    public synchronized long allocatedBytes() {
        return size;
    }

    public OptionalLong byteSize() {
        return (allocator instanceof OneOffBlockAllocator) ?
                OptionalLong.of(((OneOffBlockAllocator) allocator).size) :
                OptionalLong.empty();
    }

    public static class OneOffBlockAllocator implements SegmentAllocator {
        boolean first = true;
        final ResourceScope scope;
        final long size;

        public OneOffBlockAllocator(ResourceScope scope, long size) {
            this.scope = scope;
            this.size = size;
        }

        @Override
        public MemorySegment allocate(long bytesSize, long bytesAlignment) {
            if (first) {
                first = false;
                return MemorySegment.allocateNative(size, scope);
            } else {
                throw new OutOfMemoryError("Not enough space left to allocate");
            }
        }
    }
}
