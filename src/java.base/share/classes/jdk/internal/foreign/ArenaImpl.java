package jdk.internal.foreign;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;

public final class ArenaImpl implements Arena {

    final MemorySessionImpl sessionImpl;

    public ArenaImpl(MemorySessionImpl sessionImpl) {
        this.sessionImpl = sessionImpl;
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        return MemorySegment.allocateNative(byteSize, byteAlignment, session());
    }

    @Override
    public MemorySession session() {
        return sessionImpl;
    }

    @Override
    public void close() {
        sessionImpl.close();
    }
}
