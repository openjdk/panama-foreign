/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.nicl.LibrariesHelper;
import jdk.internal.nicl.Util;
import java.nicl.types.*;
import java.nicl.types.Pointer;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

public class BoundedPointer<T> implements Pointer<T> {
    private static final boolean DEBUG_WARN_ON_OOBPTR = Boolean.valueOf(
        privilegedGetProperty("jdk.internal.nicl.types.DEBUG_WARN_ON_OOBPTR", "true"));

    public static final Pointer<Void> NULL = new BoundedPointer<>(LayoutType.create(void.class), BoundedMemoryRegion.NOTHING, 0, 0);

    public static boolean isNull(long addr) {
        // FIMXE: Include the 64k?
        return addr == 0;
    }

    protected final BoundedMemoryRegion region;
    protected final long offset;

    protected final LayoutType<T> type;
    protected final int mode;

    public BoundedPointer(LayoutType<T> type, BoundedMemoryRegion region) {
        this(type, region, 0);
    }

    public BoundedPointer(LayoutType<T> type, BoundedMemoryRegion region, long offset) {
        this(type, region, offset, BoundedMemoryRegion.MODE_RW);
    }

    public BoundedPointer(LayoutType<T> type, BoundedMemoryRegion region, long offset, int mode) {
        this.region = region;
        this.offset = offset;
        this.type = type;
        this.mode = mode;
    }

    public static BoundedPointer<Void> createNativeVoidPointer(long offset) {
        return new BoundedPointer<>(LayoutType.create(void.class), BoundedMemoryRegion.EVERYTHING, offset);
    }

    @Override
    public T deref() {
        if (Util.isCStruct(type.getCarrierType())) {
            Class<? extends T> c = LibrariesHelper.getStructImplClass(type.getCarrierType());

            try {
                return c.getConstructor(Pointer.class).newInstance(this);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            return lvalue().get();
        }
    }

    @Override
    public Reference<T> lvalue() {
        // FIXME: check bounds & liveness here
        // FIXME: check for and potentially create an instance of the struct here if Util.isCStruct(type.getCarrierType()) ?
        return new ReferenceImpl<>(type, region, offset, mode);
    }

    private long effectiveAddress() {
        return region.addr() + offset;
    }

    @Override
    public long addr() throws UnsupportedOperationException, IllegalAccessException {
        return effectiveAddress();
    }

    @Override
    public <S> Pointer<S> cast(LayoutType<S> type) {
        return new BoundedPointer<>(type, region, offset);
    }

    @Override
    public boolean isNull() {
        return isNull(effectiveAddress());
    }

    @Override
    public BoundedPointer<T> offset(long nElements) {
        long elemSize = type.getNativeTypeSize();
        if (elemSize == 0) {
            throw new IllegalArgumentException();
        }

        long newOffset = this.offset + nElements * elemSize;

        if (DEBUG_WARN_ON_OOBPTR) { // FIXME: eager warning for sanity, just for now
            try {
                region.checkBounds(newOffset);
            } catch (IndexOutOfBoundsException e) {
                System.err.println("WARNING: Producing out of bounds pointer (could be an error)");
                e.printStackTrace();
            }
        }
        // Note: the pointer may point outside of the memory region bounds.
        // This is allowed, as long as the pointer/data is not dereferenced
        return new BoundedPointer<>(type, region, newOffset);
    }

    public void copyTo(BoundedPointer<?> dst, long bytes) throws IllegalAccessException {
        BoundedMemoryRegion srcRegion = this.region;
        BoundedMemoryRegion dstRegion = dst.region;
        srcRegion.copyTo(this.offset, dstRegion, dst.offset, bytes);
    }

    @Override
    public String toString() {
        return "{ BoundedPointer<" + type.getCarrierType() + "> type: " + type + " region: " + region + " offset=0x" + Long.toHexString(offset) + " }";
    }
}
