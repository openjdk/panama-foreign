package jdk.internal.foreign;

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeAllocator;
import jdk.incubator.foreign.ResourceScope;

import java.util.OptionalLong;

public abstract class AbstractArenaAllocator implements NativeAllocator {

    private final ResourceScope scope;

    AbstractArenaAllocator(ResourceScope scope) {
        this.scope = scope;
    }

    MemorySegment newSegment(long size, long align) {
        MemorySegment segment = NativeMemorySegmentImpl.makeNativeSegment(size, align, Utils.asScope(scope));
        return segment;
    }

    MemorySegment newSegment(long size) {
        return newSegment(size, size);
    }

    abstract long allocatedBytes();

    abstract OptionalLong size();

    public static class UnboundedArenaAllocator extends AbstractArenaAllocator {

        private static final long BLOCK_SIZE = 4 * 1024;
        private static final long MAX_ALLOC_SIZE = BLOCK_SIZE / 2;

        private MemorySegment segment;
        private long sp = 0L;
        private long size = 0L;

        public UnboundedArenaAllocator(ResourceScope scope) {
            super(scope);
            this.segment = newSegment(BLOCK_SIZE);
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

        @Override
        synchronized long allocatedBytes() {
            return size;
        }

        @Override
        OptionalLong size() {
            return OptionalLong.empty();
        }
    }

    public static class BoundedArenaAllocator extends AbstractArenaAllocator {
        private final MemorySegment segment;
        private long sp = 0L;

        public BoundedArenaAllocator(long size, ResourceScope scope) {
            super(scope);
            this.segment = newSegment(size, 1);
        }

        @Override
        public synchronized MemorySegment allocate(long bytesSize, long bytesAlignment) {
            long min = segment.address().toRawLongValue();
            long start = Utils.alignUp(min + sp, bytesAlignment) - min;
            try {
                MemorySegment slice = segment.asSlice(start, bytesSize);
                sp = start + bytesSize;
                return slice;
            } catch (IndexOutOfBoundsException ex) {
                throw new OutOfMemoryError("Not enough space left to allocate");
            }
        }

        @Override
        synchronized long allocatedBytes() {
            return sp;
        }

        @Override
        OptionalLong size() {
            return OptionalLong.of(segment.byteSize());
        }
    }
}
