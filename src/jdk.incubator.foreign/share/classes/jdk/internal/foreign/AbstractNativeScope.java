package jdk.internal.foreign;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

public abstract class AbstractNativeScope extends NativeScope {

    private final List<MemorySegment> segments = new ArrayList<>();
    private final Thread ownerThread;

    private static final int SCOPE_MASK = MemorySegment.READ | MemorySegment.WRITE; // no terminal operations allowed

    AbstractNativeScope(Thread ownerThread) {
        this.ownerThread = ownerThread;
    }

    @Override
    public Thread ownerThread() {
        return ownerThread;
    }

    @Override
    public void close() {
        for (MemorySegment segment : segments) {
            try {
                segment.close();
            } catch (IllegalStateException ex) {
                //already closed - skip
            }
        }
    }

    void checkOwnerThread() {
        if (Thread.currentThread() != ownerThread()) {
            throw new IllegalStateException("Attempt to access scope from different thread");
        }
    }

    MemorySegment newSegment(long size, long align) {
        MemorySegment segment = MemorySegment.allocateNative(size, align);
        segments.add(segment);
        return segment;
    }

    MemorySegment newSegment(long size) {
        return newSegment(size, size);
    }

    @Override
    public MemorySegment register(MemorySegment segment) {
        Objects.requireNonNull(segment);
        if (segment.ownerThread() != null && (segment.ownerThread() != ownerThread())) {
            throw new IllegalArgumentException("Cannot register segment owned by a different thread");
        } else if (!segment.hasAccessModes(MemorySegment.CLOSE)) {
            throw new IllegalArgumentException("Cannot register a non-closeable segment");
        }
        MemorySegment attachedSegment = ((AbstractMemorySegmentImpl)segment)
                .dupAndClose(ownerThread());
        segments.add(attachedSegment);
        return attachedSegment
                .withAccessModes(segment.accessModes() & SCOPE_MASK);
    }

    public static class UnboundedNativeScope extends AbstractNativeScope {

        private static final long BLOCK_SIZE = 4 * 1024;
        private static final long MAX_ALLOC_SIZE = BLOCK_SIZE / 2;

        private MemorySegment segment;
        private long sp = 0L;
        private long size = 0L;

        @Override
        public OptionalLong byteSize() {
            return OptionalLong.empty();
        }

        @Override
        public long allocatedBytes() {
            return size;
        }

        public UnboundedNativeScope() {
            super(Thread.currentThread());
            this.segment = newSegment(BLOCK_SIZE);
        }

        @Override
        public MemoryAddress allocate(long bytesSize, long bytesAlignment) {
            checkOwnerThread();
            if (bytesSize > MAX_ALLOC_SIZE) {
                MemorySegment segment = newSegment(bytesSize, bytesAlignment);
                return segment.withAccessModes(SCOPE_MASK)
                        .address();
            }
            for (int i = 0; i < 2; i++) {
                long min = ((MemoryAddressImpl) segment.address()).unsafeGetOffset();
                long start = Utils.alignUp(min + sp, bytesAlignment) - min;
                try {
                    MemorySegment slice = segment.asSlice(start, bytesSize)
                            .withAccessModes(SCOPE_MASK);
                    sp = start + bytesSize;
                    size += Utils.alignUp(bytesSize, bytesAlignment);
                    return slice.address();
                } catch (IndexOutOfBoundsException ex) {
                    sp = 0L;
                    segment = newSegment(BLOCK_SIZE, 1L);
                }
            }
            throw new AssertionError("Cannot get here!");
        }
    }

    public static class BoundedNativeScope extends AbstractNativeScope {
        private final MemorySegment segment;
        private long sp = 0L;

        @Override
        public OptionalLong byteSize() {
            return OptionalLong.of(segment.byteSize());
        }

        @Override
        public long allocatedBytes() {
            return sp;
        }

        public BoundedNativeScope(long size) {
            super(Thread.currentThread());
            this.segment = newSegment(size, 1);
        }

        @Override
        public MemoryAddress allocate(long bytesSize, long bytesAlignment) {
            checkOwnerThread();
            long min = ((MemoryAddressImpl)segment.address()).unsafeGetOffset();
            long start = Utils.alignUp(min + sp, bytesAlignment) - min;
            try {
                MemorySegment slice = segment.asSlice(start, bytesSize)
                        .withAccessModes(SCOPE_MASK);
                sp = start + bytesSize;
                return slice.address();
            } catch (IndexOutOfBoundsException ex) {
                throw new OutOfMemoryError("Not enough space left to allocate");
            }
        }
    }
}
