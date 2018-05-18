/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.internal.nicl.types;

import java.nicl.NativeTypes;
import java.nicl.types.Array;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BoundedPointer<X> implements Pointer<X> {

    public static boolean isNull(long addr) {
        // FIMXE: Include the 64k?
        return addr == 0;
    }

    public final BoundedMemoryRegion region;
    public final long offset;

    public final LayoutType<X> type;
    // @@@ Unused
    public final int mode;

    public BoundedPointer(LayoutType<X> type, BoundedMemoryRegion region) {
        this(type, region, 0);
    }

    public BoundedPointer(LayoutType<X> type, BoundedMemoryRegion region, long offset) {
        this(type, region, offset, BoundedMemoryRegion.MODE_RW);
    }

    public BoundedPointer(LayoutType<X> type, BoundedMemoryRegion region, long offset, int mode) {
        this.region = Objects.requireNonNull(region);
        this.offset = offset;
        this.type = Objects.requireNonNull(type);
        this.mode = mode;
    }

    @Override
    public LayoutType<X> type() {
        return type;
    }

    private long effectiveAddress() {
        return region.addr() + offset;
    }

    @Override
    public boolean isAccessibleFor(int mode) {
        return region.isAccessibleFor(mode);
    }

    @Override
    public long addr() throws UnsupportedOperationException, IllegalAccessException {
        return effectiveAddress();
    }

    @Override
    public long bytesSize() {
        return region.length - offset;
    }

    @Override
    public Array<X> withSize(long size) {
        return new BoundedArray<>(this, (int)size);
    }

    @Override
    public Stream<Pointer<X>> elements() {
        return StreamSupport.stream(Spliterators.spliterator(new PointerIterator<>(this),
                region.length / (type.layout().bitsSize() / 8),
                Spliterator.NONNULL | Spliterator.DISTINCT | Spliterator.ORDERED), false);
    }

    public BoundedPointer<X> limit(long nelems) {
        return new BoundedPointer<>(type, region.limit(offset + ((type.layout().bitsSize() / 8) * nelems)), offset);
    }

    @Override
    public boolean isNull() {
        return isNull(effectiveAddress());
    }

    @Override
    public BoundedPointer<X> offset(long nElements) throws IllegalArgumentException, IndexOutOfBoundsException {
        long elemSize = type.layout().bitsSize() / 8;
        if (elemSize == 0) {
            throw new IllegalArgumentException();
        }

        long newOffset = this.offset + nElements * elemSize;

        region.checkBounds(newOffset);

        // Note: the pointer may point outside of the memory region bounds.
        // This is allowed, as long as the pointer/data is not dereferenced
        return new BoundedPointer<>(type, region, newOffset);
    }

    public void copyTo(BoundedPointer<?> dst, long bytes) throws IllegalAccessException {
        BoundedMemoryRegion srcRegion = this.region;
        BoundedMemoryRegion dstRegion = dst.region;
        srcRegion.copyTo(this.offset, dstRegion, dst.offset, bytes);
    }

    public <Z> BoundedPointer<Z> cast(LayoutType<Z> layoutType) {
        return new BoundedPointer<>(layoutType, region, offset, mode);
    }

    public static BoundedPointer<?> createNativeVoidPointer(long offset) {
        return new BoundedPointer<>(NativeTypes.VOID, BoundedMemoryRegion.EVERYTHING, offset);
    }

    @Override
    public String toString() {
        return "{ BoundedPointer type: " + type + " region: " + region + " offset=0x" + Long.toHexString(offset) + " }";
    }

    public void dump(int nbytes) {
        region.dump(nbytes);
    }

    static class PointerIterator<X> implements Iterator<BoundedPointer<X>> {

        BoundedPointer<X> next;

        PointerIterator(BoundedPointer<X> pointer) {
            this.next = pointer;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public BoundedPointer<X> next() {
            if (next == null) {
                throw new IllegalStateException("Pointer iterator doesn't have next()");
            } else {
                BoundedPointer<X> prev = next;
                update();
                return prev;
            }
        }

        private void update() {
            try {
                next = next.offset(1);
            } catch (IndexOutOfBoundsException ex) {
                next = null;
            }
        }
    }
}
