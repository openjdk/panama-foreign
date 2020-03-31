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

package jdk.internal.foreign;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SequenceLayout;
import jdk.internal.access.JavaNioAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.access.foreign.MemorySegmentProxy;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * This class provides an immutable implementation for the {@code MemorySegment} interface. This class contains information
 * about the segment's spatial and temporal bounds, as well as the addressing coordinates (base + offset) which allows
 * unsafe access; each memory segment implementation is associated with an owner thread which is set at creation time.
 * Access to certain sensitive operations on the memory segment will fail with {@code IllegalStateException} if the
 * segment is either in an invalid state (e.g. it has already been closed) or if access occurs from a thread other
 * than the owner thread. See {@link MemoryScope} for more details on management of temporal bounds.
 */
public final class MemorySegmentImpl implements MemorySegment, MemorySegmentProxy {

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    private static final int BYTE_ARR_BASE = UNSAFE.arrayBaseOffset(byte[].class);

    final long length;
    final int mask;
    final long min;
    final Object base;
    final Thread owner;
    final MemoryScope scope;

    final static int ACCESS_MASK = READ | WRITE | CLOSE | ACQUIRE;
    final static int FIRST_RESERVED_FLAG = 1 << 16; // upper 16 bits are reserved
    final static int SMALL = FIRST_RESERVED_FLAG;

    final static long NONCE = new Random().nextLong();

    final static int DEFAULT_MASK = READ | WRITE | CLOSE | ACQUIRE;
    public static final MemorySegmentImpl NOTHING = new MemorySegmentImpl();

    private MemorySegmentImpl() {
        this.length = 0L;
        this.mask = 0;
        this.min = 0L;
        this.base = null;
        this.owner = null;
        this.scope = MemoryScope.GLOBAL;
    }

    public MemorySegmentImpl(long min, Object base, long length, Thread owner, MemoryScope scope) {
        this(min, base, length, DEFAULT_MASK, owner, scope);
    }

    @ForceInline
    MemorySegmentImpl(long min, Object base, long length, int mask, Thread owner, MemoryScope scope) {
        this.length = length;
        this.mask = length > Integer.MAX_VALUE ? mask : (mask | SMALL);
        this.min = min;
        this.base = base;
        this.owner = owner;
        this.scope = scope;
    }

    // MemorySegment methods

    @Override
    public final MemorySegment asSlice(long offset, long newSize) {
        checkBounds(offset, newSize);
        return asSliceNoCheck(offset, newSize);
    }

    @ForceInline
    private MemorySegmentImpl asSliceNoCheck(long offset, long newSize) {
        return new MemorySegmentImpl(min + offset, base, newSize, mask, owner, scope);
    }

    @Override
    public Spliterator<MemorySegment> spliterator(SequenceLayout sequenceLayout) {
        checkValidState();
        if (sequenceLayout.byteSize() != byteSize()) {
            throw new IllegalArgumentException();
        }
        return new SegmentSplitter(sequenceLayout.elementLayout().byteSize(), sequenceLayout.elementCount().getAsLong(),
                this.withAccessModes(accessModes() & ~CLOSE));
    }

    @Override
    @ForceInline
    public final MemoryAddress baseAddress() {
        return new MemoryAddressImpl(this, 0);
    }

    @Override
    public final long byteSize() {
        return length;
    }

    @Override
    public final boolean isAlive() {
        return scope.isAliveThreadSafe();
    }

    @Override
    public Thread ownerThread() {
        return owner;
    }

    @Override
    public final void close() {
        if (!isSet(CLOSE)) {
            throw unsupportedAccessMode(CLOSE);
        }
        checkValidState();
        closeNoCheck();
    }

    private void closeNoCheck() {
        scope.close();
    }

    @Override
    public ByteBuffer asByteBuffer() {
        if (!isSet(READ)) {
            throw unsupportedAccessMode(READ);
        }
        checkIntSize("ByteBuffer");
        JavaNioAccess nioAccess = SharedSecrets.getJavaNioAccess();
        ByteBuffer _bb;
        if (base() != null) {
            if (!(base() instanceof byte[])) {
                throw new UnsupportedOperationException("Not an address to an heap-allocated byte array");
            }
            _bb = nioAccess.newHeapByteBuffer((byte[]) base(), (int)min - BYTE_ARR_BASE, (int) length, this);
        } else {
            _bb = nioAccess.newDirectByteBuffer(min, (int) length, null, this);
        }
        if (!isSet(WRITE)) {
            //scope is IMMUTABLE - obtain a RO byte buffer
            _bb = _bb.asReadOnlyBuffer();
        }
        return _bb;
    }

    @Override
    public MemorySegmentImpl withAccessModes(int accessModes) {
        checkAccessModes(accessModes);
        if ((~accessModes() & accessModes) != 0) {
            throw new UnsupportedOperationException("Cannot acquire more access modes");
        }
        return new MemorySegmentImpl(min, base, length, accessModes, owner, scope);
    }

    @Override
    public boolean hasAccessModes(int accessModes) {
        checkAccessModes(accessModes);
        return (accessModes() & accessModes) == accessModes;
    }

    private void checkAccessModes(int accessModes) {
        if ((accessModes & ~ACCESS_MASK) != 0) {
            throw new IllegalArgumentException("Invalid access modes");
        }
    }

    @Override
    public int accessModes() {
        return mask & ACCESS_MASK;
    }

    @Override
    public byte[] toByteArray() {
        checkIntSize("byte[]");
        byte[] arr = new byte[(int)length];
        MemorySegment arrSegment = MemorySegment.ofArray(arr);
        MemoryAddress.copy(this.baseAddress(), arrSegment.baseAddress(), length);
        return arr;
    }

    // MemorySegmentProxy methods

    @Override
    public final void checkValidState() {
        if (owner != null && owner != Thread.currentThread()) {
            throw new IllegalStateException("Attempt to access segment outside owning thread");
        }
        scope.checkAliveConfined();
    }

    boolean isSmall() {
        return isSet(SMALL);
    }

    // Object methods

    @Override
    public String toString() {
        return "MemorySegment{ id=0x" + Long.toHexString(id()) + " limit: " + byteSize() + " }";
    }

    // Helper methods

    private MemorySegmentImpl acquire() {
        if (Thread.currentThread() != owner && !isSet(ACQUIRE)) {
            throw unsupportedAccessMode(ACQUIRE);
        }
        return new MemorySegmentImpl(min, base, length, mask, Thread.currentThread(), scope.acquire());
    }

    public MemorySegment asUnconfined() {
        checkValidState();
        return new MemorySegmentImpl(min, base, length, mask, null, scope);
    }

    void checkRange(long offset, long length, boolean writeAccess) {
        checkValidState();
        if (writeAccess && !isSet(WRITE)) {
            throw unsupportedAccessMode(WRITE);
        } else if (!writeAccess && !isSet(READ)) {
            throw unsupportedAccessMode(READ);
        }
        checkBounds(offset, length);
    }

    Object base() {
        return base;
    }

    private boolean isSet(int mask) {
        return (this.mask & mask) != 0;
    }

    private void checkIntSize(String typeName) {
        if (length > (Integer.MAX_VALUE - 8)) { //conservative check
            throw new UnsupportedOperationException(String.format("Segment is too large to wrap as %s. Size: %d", typeName, length));
        }
    }

    private void checkBounds(long offset, long length) {
        if (isSmall()) {
            checkBoundsSmall((int)offset, (int)length);
        } else {
            if (length < 0 ||
                    offset < 0 ||
                    offset > this.length - length) { // careful of overflow
                throw outOfBoundException(offset, length);
            }
        }
    }

    @ForceInline
    private void checkBoundsSmall(int offset, int length) {
        if (length < 0 ||
                offset < 0 ||
                offset > (int)this.length - length) { // careful of overflow
            throw outOfBoundException(offset, length);
        }
    }

    UnsupportedOperationException unsupportedAccessMode(int expected) {
        return new UnsupportedOperationException((String.format("Required access mode %s ; current access modes: %s",
                modeStrings(expected).get(0), modeStrings(mask))));
    }

    private List<String> modeStrings(int mode) {
        List<String> modes = new ArrayList<>();
        if ((mode & READ) != 0) {
            modes.add("READ");
        }
        if ((mode & WRITE) != 0) {
            modes.add("WRITE");
        }
        if ((mode & CLOSE) != 0) {
            modes.add("CLOSE");
        }
        if ((mode & ACQUIRE) != 0) {
            modes.add("ACQUIRE");
        }
        return modes;
    }

    private IndexOutOfBoundsException outOfBoundException(long offset, long length) {
        return new IndexOutOfBoundsException(String.format("Out of bound access on segment %s; new offset = %d; new length = %d",
                        this, offset, length));
    }

    private int id() {
        //compute a stable and random id for this memory segment
        return Math.abs(Objects.hash(base, min, NONCE));
    }

    static class SegmentSplitter implements Spliterator<MemorySegment> {
        MemorySegmentImpl segment;
        long elemCount;
        final long elementSize;
        long currentIndex;

        SegmentSplitter(long elementSize, long elemCount, MemorySegmentImpl segment) {
            this.segment = segment;
            this.elementSize = elementSize;
            this.elemCount = elemCount;
        }

        @Override
        public SegmentSplitter trySplit() {
            if (currentIndex == 0 && elemCount > 1) {
                MemorySegmentImpl parent = segment;
                long rem = elemCount % 2;
                long split = elemCount / 2;
                long lobound = split * elementSize;
                long hibound = lobound + (rem * elementSize);
                elemCount  = split + rem;
                segment = parent.asSliceNoCheck(lobound, hibound);
                return new SegmentSplitter(elementSize, split, parent.asSliceNoCheck(0, lobound));
            } else {
                return null;
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super MemorySegment> action) {
            Objects.requireNonNull(action);
            if (currentIndex < elemCount) {
                MemorySegmentImpl acquired = segment.acquire();
                try {
                    action.accept(acquired.asSliceNoCheck(currentIndex * elementSize, elementSize));
                } finally {
                    acquired.closeNoCheck();
                    currentIndex++;
                    if (currentIndex == elemCount) {
                        segment = null;
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void forEachRemaining(Consumer<? super MemorySegment> action) {
            Objects.requireNonNull(action);
            if (currentIndex < elemCount) {
                MemorySegmentImpl acquired = segment.acquire();
                try {
                    if (acquired.isSmall()) {
                        int index = (int) currentIndex;
                        int limit = (int) elemCount;
                        int elemSize = (int) elementSize;
                        for (; index < limit; index++) {
                            action.accept(acquired.asSliceNoCheck(index * elemSize, elemSize));
                        }
                    } else {
                        for (long i = currentIndex ; i < elemCount ; i++) {
                            action.accept(acquired.asSliceNoCheck(i * elementSize, elementSize));
                        }
                    }
                } finally {
                    acquired.closeNoCheck();
                    currentIndex = elemCount;
                    segment = null;
                }
            }
        }

        @Override
        public long estimateSize() {
            return elemCount;
        }

        @Override
        public int characteristics() {
            return NONNULL | SUBSIZED | SIZED | IMMUTABLE | ORDERED;
        }
    }
}
