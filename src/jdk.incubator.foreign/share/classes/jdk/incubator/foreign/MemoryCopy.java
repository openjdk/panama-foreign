/*
 *  Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package jdk.incubator.foreign;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import jdk.internal.foreign.AbstractMemorySegmentImpl;
import jdk.internal.foreign.HeapMemorySegmentImpl;
import jdk.internal.misc.ScopedMemoryAccess;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

/**
 * This class provides convenient methods for copying data between primitive arrays and memory segments.
 *
 * <p>If the source (destination) segment is actually a view of the destination (source) array,
 * and if the copy region of the source overlaps with the copy region of the destination,
 * the copy of the overlapping region is performed as if the data in the overlapping region
 * were first copied into a temporary segment before being copied to the destination.</p>
 *
 * <p>
 * Copy operations defined in this class accept a <em>byte order</em> parameter. If the specified byte order is different
 * from the <em>native</em> byte order, a byte swap operation is performed on each array elements
 * as they are copied from the source (destination) segment to the destination (source) array.
 * Additional overloads are provided (see {@link #copyFromArray(double[], int, int, MemorySegment, long)}),
 * so that clients can omit the byte order parameter.
 *
 * <p> Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more {@code null}
 * elements to a method in this class causes a {@link NullPointerException} to be thrown. Moreover,
 * attempting to copy to/from a segment whose {@linkplain MemorySegment#scope() scope} has already been closed,
 * or from a thread other than the thread owning the scope causes an {@link IllegalStateException} to be thrown.
 * Finally, attempting to copy to/from a segment (of {@linkplain MemorySegment#address() base address} {@code B} and
 * {@linkplain MemorySegment#byteSize() size} {@code S}) at addresses that are {@code < B}, or {@code >= B + S},
 * causes an {@link IndexOutOfBoundsException} to be thrown; similarly, attempting to copy to/from an array
 * (of length {@code L}) at indices that are {@code < 0}, or {@code >= L} causes an {@link IndexOutOfBoundsException} to be thrown.</p>
 */
public final class MemoryCopy {

    private static final ScopedMemoryAccess scopedMemoryAccess = ScopedMemoryAccess.getScopedMemoryAccess();
    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private static final int BYTE_BASE = unsafe.arrayBaseOffset(byte[].class);
    private static final int BOOL_BASE = unsafe.arrayBaseOffset(boolean[].class);
    private static final int CHAR_BASE = unsafe.arrayBaseOffset(char[].class);
    private static final int SHORT_BASE = unsafe.arrayBaseOffset(short[].class);
    private static final int INT_BASE = unsafe.arrayBaseOffset(int[].class);
    private static final int FLOAT_BASE = unsafe.arrayBaseOffset(float[].class);
    private static final int LONG_BASE = unsafe.arrayBaseOffset(long[].class);
    private static final int DOUBLE_BASE = unsafe.arrayBaseOffset(double[].class);

    private MemoryCopy() { /* singleton */ }

    //BYTE
    /**
     * Copies a number of byte elements from a source byte array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source byte array.
     * @param srcIndexBytes the starting index of the source byte array.
     * @param srcCopyLengthBytes the number of byte elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     */
    @ForceInline
    public static void copyFromArray(
            byte[] srcArray, int srcIndexBytes, int srcCopyLengthBytes,
            MemorySegment dstSegment, long dstOffsetBytes) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.checkFromIndexSize(srcIndexBytes, srcCopyLengthBytes, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffsetBytes, srcCopyLengthBytes, false);
        scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                srcArray, BYTE_BASE + srcIndexBytes,
                destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthBytes);
    }

    /**
     * Copies a number of byte elements from a source segment to a destination byte array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination byte array.
     * @param dstIndexBytes the starting index of the destination byte array.
     * @param dstCopyLengthBytes the number of byte elements to be copied.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            byte[] dstArray, int dstIndexBytes, int dstCopyLengthBytes) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffsetBytes, dstCopyLengthBytes, true);
        Objects.checkFromIndexSize(dstIndexBytes, dstCopyLengthBytes, dstArray.length);
        scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                dstArray, BYTE_BASE + dstIndexBytes, dstCopyLengthBytes);
    }

    //BOOL
    /**
     * Copies a number of boolean elements from a source byte array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source boolean array.
     * @param srcIndexBooleans the starting index of the source boolean array.
     * @param srcCopyLengthBooleans the number of boolean elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     */
    @ForceInline
    public static void copyFromArray(
            boolean[] srcArray, int srcIndexBooleans, int srcCopyLengthBooleans,
            MemorySegment dstSegment, long dstOffsetBytes) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.checkFromIndexSize(srcIndexBooleans, srcCopyLengthBooleans, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffsetBytes, srcCopyLengthBooleans, false);
        scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                srcArray, BOOL_BASE + srcIndexBooleans,
                destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthBooleans);
    }

    /**
     * Copies a number of boolean elements from a source segment to a destination boolean array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination boolean array.
     * @param dstIndexBooleans the starting index of the destination boolean array.
     * @param dstCopyLengthBooleans the number of boolean elements to be copied.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            boolean[] dstArray, int dstIndexBooleans, int dstCopyLengthBooleans) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        for (int i = 0 ; i < dstCopyLengthBooleans ; i++) {
            dstArray[dstIndexBooleans + i] =
                    (MemoryAccess.getByteAtOffset(srcSegment, srcOffsetBytes + i) & 1) == 1;
        }
    }

    //CHAR
    /**
     * Copies a number of char elements from a source char array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source char array.
     * @param srcIndexChars the starting index of the source char array.
     * @param srcCopyLengthChars the number of char elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     */
    @ForceInline
    public static void copyFromArray(
            char[] srcArray, int srcIndexChars, int srcCopyLengthChars,
            MemorySegment dstSegment, long dstOffsetBytes) {
        copyFromArray(srcArray, srcIndexChars, srcCopyLengthChars, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of char elements from a source char array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source char array.
     * @param srcIndexChars the starting index of the source char array.
     * @param srcCopyLengthChars the number of char elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyFromArray(
            char[] srcArray, int srcIndexChars, int srcCopyLengthChars,
            MemorySegment dstSegment, long dstOffsetBytes,
            ByteOrder order) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndexChars, srcCopyLengthChars, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffsetBytes, srcCopyLengthChars << 1, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, CHAR_BASE + (srcIndexChars << 1),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthChars << 1);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, CHAR_BASE + (srcIndexChars << 1),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthChars << 1, 2);
        }
    }

    /**
     * Copies a number of char elements from a source segment to a destination char array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination char array.
     * @param dstIndexChars the starting index of the destination char array.
     * @param dstCopyLengthChars the number of char elements to be copied.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            char[] dstArray, int dstIndexChars, int dstCopyLengthChars) {
        copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexChars, dstCopyLengthChars, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of char elements from a source segment to a destination char array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination char array.
     * @param dstIndexChars the starting index of the destination char array.
     * @param dstCopyLengthChars the number of char elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            char[] dstArray, int dstIndexChars, int dstCopyLengthChars,
            ByteOrder order) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffsetBytes, dstCopyLengthChars << 1, true);
        Objects.checkFromIndexSize(dstIndexChars, dstCopyLengthChars, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, CHAR_BASE + (dstIndexChars << 1), dstCopyLengthChars << 1);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, CHAR_BASE + (dstIndexChars << 1), dstCopyLengthChars << 1, 2);
        }
    }

    //SHORT
    /**
     * Copies a number of short elements from a source short array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source short array.
     * @param srcIndexShorts the starting index of the source short array.
     * @param srcCopyLengthShorts the number of short elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     */
    @ForceInline
    public static void copyFromArray(
            short[] srcArray, int srcIndexShorts, int srcCopyLengthShorts,
            MemorySegment dstSegment, long dstOffsetBytes) {
        copyFromArray(srcArray, srcIndexShorts, srcCopyLengthShorts, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of short elements from a source short array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source short array.
     * @param srcIndexShorts the starting index of the source short array.
     * @param srcCopyLengthShorts the number of short elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyFromArray(
            short[] srcArray, int srcIndexShorts, int srcCopyLengthShorts,
            MemorySegment dstSegment, long dstOffsetBytes,
            ByteOrder order) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndexShorts, srcCopyLengthShorts, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffsetBytes, srcCopyLengthShorts << 1, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, SHORT_BASE + (srcIndexShorts << 1),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthShorts << 1);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, SHORT_BASE + (srcIndexShorts << 1),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthShorts << 1, 2);
        }
    }

    /**
     * Copies a number of short elements from a source segment to a destination short array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination short array.
     * @param dstIndexShorts the starting index of the destination short array.
     * @param dstCopyLengthShorts the number of short elements to be copied.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            short[] dstArray, int dstIndexShorts, int dstCopyLengthShorts) {
        copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexShorts, dstCopyLengthShorts, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of short elements from a source segment to a destination short array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination short array.
     * @param dstIndexShorts the starting index of the destination short array.
     * @param dstCopyLengthShorts the number of short elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            short[] dstArray, int dstIndexShorts, int dstCopyLengthShorts,
            ByteOrder order) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffsetBytes, dstCopyLengthShorts << 1, true);
        Objects.checkFromIndexSize(dstIndexShorts, dstCopyLengthShorts, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, SHORT_BASE + (dstIndexShorts << 1), dstCopyLengthShorts << 1);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, SHORT_BASE + (dstIndexShorts << 1), dstCopyLengthShorts << 1, 2);
        }
    }

    //INT
    /**
     * Copies a number of int elements from a source int array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source int array.
     * @param srcIndexInts the starting index of the source int array.
     * @param srcCopyLengthInts the number of int elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     */
    @ForceInline
    public static void copyFromArray(
            int[] srcArray, int srcIndexInts, int srcCopyLengthInts,
            MemorySegment dstSegment, long dstOffsetBytes) {
        copyFromArray(srcArray, srcIndexInts, srcCopyLengthInts, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of int elements from a source int array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source int array.
     * @param srcIndexInts the starting index of the source int array.
     * @param srcCopyLengthInts the number of int elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyFromArray(
            int[] srcArray, int srcIndexInts, int srcCopyLengthInts,
            MemorySegment dstSegment, long dstOffsetBytes,
            ByteOrder order) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndexInts, srcCopyLengthInts, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffsetBytes, srcCopyLengthInts << 2, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, INT_BASE + (srcIndexInts << 2),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthInts << 2);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, INT_BASE + (srcIndexInts << 2),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthInts << 2, 4);
        }
    }

    /**
     * Copies a number of int elements from a source segment to a destination int array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination int array.
     * @param dstIndexInts the starting index of the destination int array.
     * @param dstCopyLengthInts the number of int elements to be copied.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            int[] dstArray, int dstIndexInts, int dstCopyLengthInts) {
        copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexInts, dstCopyLengthInts, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of int elements from a source segment to a destination int array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination int array.
     * @param dstIndexInts the starting index of the destination int array.
     * @param dstCopyLengthInts the number of int elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            int[] dstArray, int dstIndexInts, int dstCopyLengthInts,
            ByteOrder order) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffsetBytes, dstCopyLengthInts << 2, true);
        Objects.checkFromIndexSize(dstIndexInts, dstCopyLengthInts, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, INT_BASE + (dstIndexInts << 2), dstCopyLengthInts << 2);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, INT_BASE + (dstIndexInts << 2), dstCopyLengthInts << 2, 4);
        }
    }

    //FLOAT
    /**
     * Copies a number of float elements from a source float array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source float array.
     * @param srcIndexFloats the starting index of the source float array.
     * @param srcCopyLengthFloats the number of float elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     */
    @ForceInline
    public static void copyFromArray(
            float[] srcArray, int srcIndexFloats, int srcCopyLengthFloats,
            MemorySegment dstSegment, long dstOffsetBytes) {
        copyFromArray(srcArray, srcIndexFloats, srcCopyLengthFloats, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of float elements from a source float array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source float array.
     * @param srcIndexFloats the starting index of the source float array.
     * @param srcCopyLengthFloats the number of float elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyFromArray(
            float[] srcArray, int srcIndexFloats, int srcCopyLengthFloats,
            MemorySegment dstSegment, long dstOffsetBytes,
            ByteOrder order) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndexFloats, srcCopyLengthFloats, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffsetBytes, srcCopyLengthFloats << 2, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, FLOAT_BASE + (srcIndexFloats << 2),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthFloats << 2);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, FLOAT_BASE + (srcIndexFloats << 2),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthFloats << 2, 4);
        }
    }

    /**
     * Copies a number of float elements from a source segment to a destination float array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination float array.
     * @param dstIndexFloats the starting index of the destination float array.
     * @param dstCopyLengthFloats the number of float elements to be copied.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            float[] dstArray, int dstIndexFloats, int dstCopyLengthFloats) {
        copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexFloats, dstCopyLengthFloats, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of float elements from a source segment to a destination float array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination float array.
     * @param dstIndexFloats the starting index of the destination float array.
     * @param dstCopyLengthFloats the number of float elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a float swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            float[] dstArray, int dstIndexFloats, int dstCopyLengthFloats,
            ByteOrder order) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffsetBytes, dstCopyLengthFloats << 2, true);
        Objects.checkFromIndexSize(dstIndexFloats, dstCopyLengthFloats, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, FLOAT_BASE + (dstIndexFloats << 2), dstCopyLengthFloats << 2);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, FLOAT_BASE + (dstIndexFloats << 2), dstCopyLengthFloats << 2, 4);
        }
    }

    //LONG
    /**
     * Copies a number of long elements from a source long array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source long array.
     * @param srcIndexLongs the starting index of the source long array.
     * @param srcCopyLengthLongs the number of long elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     */
    @ForceInline
    public static void copyFromArray(
            long[] srcArray, int srcIndexLongs, int srcCopyLengthLongs,
            MemorySegment dstSegment, long dstOffsetBytes) {
        copyFromArray(srcArray, srcIndexLongs, srcCopyLengthLongs, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of long elements from a source long array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source long array.
     * @param srcIndexLongs the starting index of the source long array.
     * @param srcCopyLengthLongs the number of long elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyFromArray(
            long[] srcArray, int srcIndexLongs, int srcCopyLengthLongs,
            MemorySegment dstSegment, long dstOffsetBytes,
            ByteOrder order) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndexLongs, srcCopyLengthLongs, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffsetBytes, srcCopyLengthLongs << 3, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, LONG_BASE + (srcIndexLongs << 3),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthLongs << 3);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, LONG_BASE + (srcIndexLongs << 3),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthLongs << 3, 8);
        }
    }

    /**
     * Copies a number of long elements from a source segment to a destination long array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination long array.
     * @param dstIndexLongs the starting index of the destination long array.
     * @param dstCopyLengthLongs the number of long elements to be copied.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            long[] dstArray, int dstIndexLongs, int dstCopyLengthLongs) {
        copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexLongs, dstCopyLengthLongs, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of long elements from a source segment to a destination long array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination long array.
     * @param dstIndexLongs the starting index of the destination long array.
     * @param dstCopyLengthLongs the number of long elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            long[] dstArray, int dstIndexLongs, int dstCopyLengthLongs,
            ByteOrder order) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffsetBytes, dstCopyLengthLongs << 3, true);
        Objects.checkFromIndexSize(dstIndexLongs, dstCopyLengthLongs, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, LONG_BASE + (dstIndexLongs << 3), dstCopyLengthLongs << 3);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, LONG_BASE + (dstIndexLongs << 3), dstCopyLengthLongs << 3, 8);
        }
    }

    //DOUBLE
    /**
     * Copies a number of double elements from a source double array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source double array.
     * @param srcIndexDoubles the starting index of the source double array.
     * @param srcCopyLengthDoubles the number of double elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     */
    @ForceInline
    public static void copyFromArray(
            double[] srcArray, int srcIndexDoubles, int srcCopyLengthDoubles,
            MemorySegment dstSegment, long dstOffsetBytes) {
        copyFromArray(srcArray, srcIndexDoubles, srcCopyLengthDoubles, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of double elements from a source double array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source double array.
     * @param srcIndexDoubles the starting index of the source double array.
     * @param srcCopyLengthDoubles the number of double elements to be copied.
     * @param dstSegment the destination segment.
     * @param dstOffsetBytes the starting offset, in bytes, of the destination segment.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyFromArray(
            double[] srcArray, int srcIndexDoubles, int srcCopyLengthDoubles,
            MemorySegment dstSegment, long dstOffsetBytes,
            ByteOrder order) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndexDoubles, srcCopyLengthDoubles, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffsetBytes, srcCopyLengthDoubles << 3, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, DOUBLE_BASE + (srcIndexDoubles << 3),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthDoubles << 3);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, DOUBLE_BASE + (srcIndexDoubles << 3),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffsetBytes, srcCopyLengthDoubles << 3, 8);
        }
    }

    /**
     * Copies a number of double elements from a source segment to a destination double array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination double array.
     * @param dstIndexDoubles the starting index of the destination double array.
     * @param dstCopyLengthDoubles the number of double elements to be copied.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            double[] dstArray, int dstIndexDoubles, int dstCopyLengthDoubles) {
        copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexDoubles, dstCopyLengthDoubles, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of double elements from a source segment to a destination double array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffsetBytes the starting offset, in bytes, of the source segment.
     * @param dstArray the destination double array.
     * @param dstIndexDoubles the starting index of the destination double array.
     * @param dstCopyLengthDoubles the number of double elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copyToArray(
            MemorySegment srcSegment, long srcOffsetBytes,
            double[] dstArray, int dstIndexDoubles, int dstCopyLengthDoubles,
            ByteOrder order) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffsetBytes, dstCopyLengthDoubles << 3, true);
        Objects.checkFromIndexSize(dstIndexDoubles, dstCopyLengthDoubles, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, DOUBLE_BASE + (dstIndexDoubles << 3), dstCopyLengthDoubles << 3);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffsetBytes,
                    dstArray, DOUBLE_BASE + (dstIndexDoubles << 3), dstCopyLengthDoubles << 3, 8);
        }
    }
}
