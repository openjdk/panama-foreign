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

import java.nio.ByteOrder;
import java.util.Objects;
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
 * <p>Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more
 * {@code null} elements to a method in this class causes a {@link NullPointerException NullPointerException}
 * to be thrown.</p>
 */
public final class MemoryCopy {
    private static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();
    private static final ByteOrder NON_NATIVE_ORDER = NATIVE_ORDER == ByteOrder.LITTLE_ENDIAN
            ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    private static final ValueLayout ARR_LAYOUT_16_N = MemoryLayout.valueLayout(16, NATIVE_ORDER).withBitAlignment(8);
    private static final ValueLayout ARR_LAYOUT_16_NN = MemoryLayout.valueLayout(16, NON_NATIVE_ORDER).withBitAlignment(8);
    private static final ValueLayout ARR_LAYOUT_32_N = MemoryLayout.valueLayout(32, NATIVE_ORDER).withBitAlignment(8);
    private static final ValueLayout ARR_LAYOUT_32_NN = MemoryLayout.valueLayout(32, NON_NATIVE_ORDER).withBitAlignment(8);
    private static final ValueLayout ARR_LAYOUT_64_N = MemoryLayout.valueLayout(64, NATIVE_ORDER).withBitAlignment(8);
    private static final ValueLayout ARR_LAYOUT_64_NN = MemoryLayout.valueLayout(64, NON_NATIVE_ORDER).withBitAlignment(8);

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
        Objects.requireNonNull(dstSegment);
        MemorySegment srcSegmentSlice = MemorySegment.ofArray(srcArray).asSlice(srcIndexBytes, srcCopyLengthBytes);
        MemorySegment dstSegmentSlice = dstSegment.asSlice(dstOffsetBytes, srcCopyLengthBytes);
        dstSegmentSlice.copyFrom(srcSegmentSlice);
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
        MemorySegment srcSegmentSlice = srcSegment.asSlice(srcOffsetBytes, dstCopyLengthBytes);
        MemorySegment dstSegmentSlice = MemorySegment.ofArray(dstArray).asSlice(dstIndexBytes, dstCopyLengthBytes);
        dstSegmentSlice.copyFrom(srcSegmentSlice);
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
        Objects.requireNonNull(dstSegment);
        MemorySegment srcSegmentSlice =
                MemorySegment.ofArray(srcArray).asSlice(srcIndexChars * 2L, srcCopyLengthChars * 2L);
        MemorySegment dstSegmentSlice = dstSegment.asSlice(dstOffsetBytes, srcCopyLengthChars * 2L);
        dstSegmentSlice.copyFrom(order == NATIVE_ORDER ? ARR_LAYOUT_16_N : ARR_LAYOUT_16_NN, srcSegmentSlice, ARR_LAYOUT_16_N);
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
        MemorySegment srcSegmentSlice = srcSegment.asSlice(srcOffsetBytes, dstCopyLengthChars * 2L);
        MemorySegment dstSegmentSlice =
                MemorySegment.ofArray(dstArray).asSlice(dstIndexChars * 2L, dstCopyLengthChars * 2L);
        dstSegmentSlice.copyFrom(ARR_LAYOUT_16_N, srcSegmentSlice, order == NATIVE_ORDER ? ARR_LAYOUT_16_N : ARR_LAYOUT_16_NN);
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
        Objects.requireNonNull(dstSegment);
        MemorySegment srcSegmentSlice =
                MemorySegment.ofArray(srcArray).asSlice(srcIndexShorts * 2L, srcCopyLengthShorts * 2L);
        MemorySegment dstSegmentSlice = dstSegment.asSlice(dstOffsetBytes, srcCopyLengthShorts * 2L);
        dstSegmentSlice.copyFrom(order == NATIVE_ORDER ? ARR_LAYOUT_16_N : ARR_LAYOUT_16_NN, srcSegmentSlice, ARR_LAYOUT_16_N);
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
        MemorySegment srcSegmentSlice = srcSegment.asSlice(srcOffsetBytes, dstCopyLengthShorts * 2L);
        MemorySegment dstSegmentSlice =
                MemorySegment.ofArray(dstArray).asSlice(dstIndexShorts * 2L, dstCopyLengthShorts * 2L);
        dstSegmentSlice.copyFrom(ARR_LAYOUT_16_N, srcSegmentSlice, order == NATIVE_ORDER ? ARR_LAYOUT_16_N : ARR_LAYOUT_16_NN);
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
        Objects.requireNonNull(dstSegment);
        MemorySegment srcSegmentSlice =
                MemorySegment.ofArray(srcArray).asSlice(srcIndexInts * 4L, srcCopyLengthInts * 4L);
        MemorySegment dstSegmentSlice = dstSegment.asSlice(dstOffsetBytes, srcCopyLengthInts * 4L);
        dstSegmentSlice.copyFrom(order == NATIVE_ORDER ? ARR_LAYOUT_32_N : ARR_LAYOUT_32_NN, srcSegmentSlice, ARR_LAYOUT_32_N);
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
        MemorySegment srcSegmentSlice = srcSegment.asSlice(srcOffsetBytes, dstCopyLengthInts * 4L);
        MemorySegment dstSegmentSlice =
                MemorySegment.ofArray(dstArray).asSlice(dstIndexInts * 4L, dstCopyLengthInts * 4L);
        dstSegmentSlice.copyFrom(ARR_LAYOUT_32_N, srcSegmentSlice, order == NATIVE_ORDER ? ARR_LAYOUT_32_N : ARR_LAYOUT_32_NN);
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
        Objects.requireNonNull(dstSegment);
        MemorySegment srcSegmentSlice =
                MemorySegment.ofArray(srcArray).asSlice(srcIndexFloats * 4L, srcCopyLengthFloats * 4L);
        MemorySegment dstSegmentSlice = dstSegment.asSlice(dstOffsetBytes, srcCopyLengthFloats * 4L);
        dstSegmentSlice.copyFrom(order == NATIVE_ORDER ? ARR_LAYOUT_32_N : ARR_LAYOUT_32_NN, srcSegmentSlice, ARR_LAYOUT_32_N);
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
        MemorySegment srcSegmentSlice = srcSegment.asSlice(srcOffsetBytes, dstCopyLengthFloats * 4L);
        MemorySegment dstSegmentSlice =
                MemorySegment.ofArray(dstArray).asSlice(dstIndexFloats * 4L, dstCopyLengthFloats * 4L);
        dstSegmentSlice.copyFrom(ARR_LAYOUT_32_N, srcSegmentSlice, order == NATIVE_ORDER ? ARR_LAYOUT_32_N : ARR_LAYOUT_32_NN);
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
        Objects.requireNonNull(dstSegment);
        MemorySegment srcSegmentSlice =
                MemorySegment.ofArray(srcArray).asSlice(srcIndexLongs * 8L, srcCopyLengthLongs * 8L);
        MemorySegment dstSegmentSlice = dstSegment.asSlice(dstOffsetBytes, srcCopyLengthLongs * 8L);
        dstSegmentSlice.copyFrom(order == NATIVE_ORDER ? ARR_LAYOUT_64_N : ARR_LAYOUT_64_NN, srcSegmentSlice, ARR_LAYOUT_64_N);
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
        MemorySegment srcSegmentSlice = srcSegment.asSlice(srcOffsetBytes, dstCopyLengthLongs * 8L);
        MemorySegment dstSegmentSlice =
                MemorySegment.ofArray(dstArray).asSlice(dstIndexLongs * 8L, dstCopyLengthLongs * 8L);
        dstSegmentSlice.copyFrom(ARR_LAYOUT_64_N, srcSegmentSlice, order == NATIVE_ORDER ? ARR_LAYOUT_64_N : ARR_LAYOUT_64_NN);
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
        Objects.requireNonNull(dstSegment);
        MemorySegment srcSegmentSlice =
                MemorySegment.ofArray(srcArray).asSlice(srcIndexDoubles * 8L, srcCopyLengthDoubles * 8L);
        MemorySegment dstSegmentSlice = dstSegment.asSlice(dstOffsetBytes, srcCopyLengthDoubles * 8L);
        dstSegmentSlice.copyFrom(order == NATIVE_ORDER ? ARR_LAYOUT_64_N : ARR_LAYOUT_64_NN, srcSegmentSlice, ARR_LAYOUT_64_N);
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
        MemorySegment srcSegmentSlice = srcSegment.asSlice(srcOffsetBytes, dstCopyLengthDoubles * 8L);
        MemorySegment dstSegmentSlice =
                MemorySegment.ofArray(dstArray).asSlice(dstIndexDoubles * 8L, dstCopyLengthDoubles * 8L);
        dstSegmentSlice.copyFrom(ARR_LAYOUT_64_N, srcSegmentSlice, order == NATIVE_ORDER ? ARR_LAYOUT_64_N : ARR_LAYOUT_64_NN);
    }
}
