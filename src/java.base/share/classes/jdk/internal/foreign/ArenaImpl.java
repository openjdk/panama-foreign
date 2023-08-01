package jdk.internal.foreign;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySegment.Scope;
import java.util.Objects;

public class ArenaImpl implements Arena {

    final MemorySessionImpl session;
    ArenaImpl(MemorySessionImpl session) {
        this.session = session;
    }

    @Override
    public Scope scope() {
        return session;
    }

    @Override
    public void close() {
        session.close();
    }

    public MemorySegment allocateNoInit(long byteSize, long byteAlignment) {
        Utils.checkAllocationSizeAndAlign(byteSize, byteAlignment);
        return NativeMemorySegmentImpl.makeNativeSegment(byteSize, byteAlignment, session, false);
    }

    public MemorySegment allocateNoInit(long byteSize) {
        return allocateNoInit(byteSize, 1);
    }

    public MemorySegment allocateNoInit(MemoryLayout layout) {
        Objects.requireNonNull(layout);
        return allocateNoInit(layout.byteSize(), layout.byteAlignment());
    }

    public MemorySegment allocateNoInit(MemoryLayout layout, long size) {
        Objects.requireNonNull(layout);
        if (size < 0) {
            throw new IllegalArgumentException("Negative array size");
        }
        return allocateNoInit(layout.byteSize() * size, layout.byteAlignment());
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        Utils.checkAllocationSizeAndAlign(byteSize, byteAlignment);
        return NativeMemorySegmentImpl.makeNativeSegment(byteSize, byteAlignment, session, true);
    }
}
