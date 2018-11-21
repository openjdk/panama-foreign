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
package jdk.internal.foreign.memory;

import jdk.internal.foreign.Util;

import java.nio.ByteBuffer;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.layout.Layout;
import java.foreign.layout.Unresolved;
import java.foreign.memory.Array;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.util.Objects;
import jdk.internal.access.SharedSecrets;

public class BoundedPointer<X> implements Pointer<X> {

    private static final BoundedPointer<?> theNullPointer = new BoundedPointer<>(
        LayoutTypeImpl.nullType, BoundedMemoryRegion.NOTHING, 0, 0);

    public static boolean isNull(long addr) {
        // FIMXE: Include the 64k?
        return addr == 0;
    }

    @SuppressWarnings("unchecked")
    public static <Z> BoundedPointer<Z> nullPointer() {
        return (BoundedPointer<Z>) theNullPointer;
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
        if (! (type.layout() instanceof Unresolved)) {
            region.checkRange(offset, type.bytesSize());
        }
    }

    @Override
    public LayoutType<X> type() {
        return type;
    }

    private long effectiveAddress() {
        checkAlive();
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
    public Array<X> withSize(long size) {
        return new BoundedArray<>(this, (int)size);
    }

    public BoundedPointer<X> limit(long nelems) {
        return new BoundedPointer<>(type, region.limit(offset + ((type.bytesSize()) * nelems)), offset);
    }

    @Override
    public boolean isNull() {
        return isNull(effectiveAddress());
    }

    @Override
    public BoundedPointer<X> offset(long nElements) throws IllegalArgumentException, IndexOutOfBoundsException {
        long elemSize = type.bytesSize();
        if (elemSize == 0) {
            throw new IllegalArgumentException();
        }

        long newOffset = this.offset + nElements * elemSize;

        // Note: the pointer may point outside of the memory region bounds.
        // This is allowed, as long as the pointer/data is not dereferenced
        return new BoundedPointer<>(type, region, newOffset);
    }

    @Override
    public ByteBuffer asDirectByteBuffer(int bytes) throws IllegalAccessException {
        region.checkRange(offset, bytes);
        return SharedSecrets.getJavaNioAccess()
                .newDirectByteBuffer(addr(), bytes, null);
    }

    public void copyTo(BoundedPointer<?> dst, long bytes) throws IllegalAccessException {
        BoundedMemoryRegion srcRegion = this.region;
        BoundedMemoryRegion dstRegion = dst.region;
        srcRegion.copyTo(this.offset, dstRegion, dst.offset, bytes);
    }

    public <Z> BoundedPointer<Z> cast(LayoutType<Z> layoutType) {
        if (isCompatible(type.layout(), layoutType.layout())) {
            return new BoundedPointer<>(layoutType, region, offset, mode);
        } else {
            throw new ClassCastException("Pointer to " + type.layout() +
                " cannot be cast to pointer to " + layoutType.layout());
        }
    }
    // where
    private static boolean isCompatible(Layout src, Layout dest) {
        /*
         * 1. Any pointer can be converted to void* and vice-versa.
         * 2. Any pointer can be converted to a pointer with same layout
         */
        return src.equals(NativeTypes.VOID.layout()) ||
            dest.equals(NativeTypes.VOID.layout()) ||
            src.equals(dest);
    }

    public void checkAlive() {
        region.checkAlive();
    }

    public static <Z> BoundedPointer<Z> createNativePointer(LayoutType<Z> type, long offset) {
        return new BoundedPointer<>(type, BoundedMemoryRegion.EVERYTHING, offset);
    }

    public static BoundedPointer<?> createNativeVoidPointer(long offset) {
        return createNativePointer(NativeTypes.VOID, offset);
    }

    public static BoundedPointer<?> createNativeVoidPointer(Scope scope, long offset) {
        return createNativeVoidPointer(scope, offset, BoundedMemoryRegion.MODE_R);
    }

    public static BoundedPointer<?> createNativeVoidPointer(Scope scope, long offset, int mode) {
        return new BoundedPointer<>(NativeTypes.VOID, BoundedMemoryRegion.ofEverything(mode, scope), offset);
    }

    public static <Z> BoundedPointer<Z> fromLongArray(LayoutType<Z> type, long[] values) {
        return new BoundedPointer<>(type,
                        BoundedMemoryRegion.of(values, Util.LONG_ARRAY_BASE, values.length * Util.LONG_ARRAY_SCALE));
    }

    public Scope scope() {
        return region.scope();
    }

    @Override
    public String toString() {
        return "{ BoundedPointer type: " + type + " region: " + region + " offset=0x" + Long.toHexString(offset) + " }";
    }

    public String dump(int nbytes) {
        return region.dump(offset, nbytes);
    }

    @Override
    public int hashCode() {
        return Long.valueOf(effectiveAddress()).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof BoundedPointer) {
            return ((BoundedPointer) o).effectiveAddress() == effectiveAddress();
        } else if (o instanceof Pointer) {
            try {
                return ((Pointer) o).addr() == effectiveAddress();
            } catch (IllegalAccessException iae) {
                throw new IllegalStateException();
            }
        } else {
            return false;
        }
    }
}
