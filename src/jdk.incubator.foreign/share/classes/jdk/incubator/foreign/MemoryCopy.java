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

import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.util.Objects;

import jdk.internal.foreign.AbstractMemorySegmentImpl;
import jdk.internal.misc.ScopedMemoryAccess;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

/**
 * This class provides convenient methods for copying data between to and from memory segments.
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
 * <p>
 * Generally, the arguments of the copy methods roughly follow the convention for {@link System#arraycopy}.
 * The optional trailing argument controls byte order, which by default is the current {@linkplain ByteOrder#nativeOrder() native byte order}.
 * <p>
 * Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more {@code null}
 * elements to a method in this class causes a {@link NullPointerException} to be thrown. Moreover,
 * attempting to copy to/from a segment whose {@linkplain MemorySegment#scope() scope} has already been closed,
 * or from a thread other than the thread owning the scope causes an {@link IllegalStateException} to be thrown.
 * Moreover, attempting to copy to/from a segment (of {@linkplain MemorySegment#address() base address} {@code B} and
 * {@linkplain MemorySegment#byteSize() size} {@code S}) at addresses that are {@code < B}, or {@code >= B + S},
 * causes an {@link IndexOutOfBoundsException} to be thrown; similarly, attempting to copy to/from an array
 * (of length {@code L}) at indices that are {@code < 0}, or {@code >= L} causes an {@link IndexOutOfBoundsException} to be thrown.
 * Finally, attempting to copy data into a {@linkplain MemorySegment#isReadOnly() read-only} segment always causes an
 * {@link UnsupportedOperationException} to be thrown.
 */
public final class MemoryCopy {

    private static final ScopedMemoryAccess scopedMemoryAccess = ScopedMemoryAccess.getScopedMemoryAccess();
    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private final static ByteOrder NON_NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ?
            ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

    private final static ValueLayout JAVA_SHORT_NO = MemoryLayouts.JAVA_SHORT.withBitAlignment(8);
    private final static ValueLayout JAVA_CHAR_NO = MemoryLayouts.JAVA_CHAR.withBitAlignment(8);
    private final static ValueLayout JAVA_INT_NO = MemoryLayouts.JAVA_INT.withBitAlignment(8);
    private final static ValueLayout JAVA_FLOAT_NO = MemoryLayouts.JAVA_FLOAT.withBitAlignment(8);
    private final static ValueLayout JAVA_LONG_NO = MemoryLayouts.JAVA_LONG.withBitAlignment(8);
    private final static ValueLayout JAVA_DOUBLE_NO = MemoryLayouts.JAVA_DOUBLE.withBitAlignment(8);

    private final static ValueLayout JAVA_SHORT_NNO = JAVA_SHORT_NO.withOrder(NON_NATIVE_ORDER);
    private final static ValueLayout JAVA_CHAR_NNO = JAVA_CHAR_NO.withOrder(NON_NATIVE_ORDER);
    private final static ValueLayout JAVA_INT_NNO = JAVA_INT_NO.withOrder(NON_NATIVE_ORDER);
    private final static ValueLayout JAVA_FLOAT_NNO = JAVA_FLOAT_NO.withOrder(NON_NATIVE_ORDER);
    private final static ValueLayout JAVA_LONG_NNO = JAVA_LONG_NO.withOrder(NON_NATIVE_ORDER);
    private final static ValueLayout JAVA_DOUBLE_NNO = JAVA_DOUBLE_NO.withOrder(NON_NATIVE_ORDER);

    private static ValueLayout pick(ByteOrder order, ValueLayout nativeLayout, ValueLayout nonNativeLayout) {
        Objects.requireNonNull(order);
        return order == ByteOrder.nativeOrder() ?
                nativeLayout : nonNativeLayout;
    }

    private MemoryCopy() { /* singleton */ }

    //BYTE
    /**
     * Copies a number of byte elements from a source byte array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source byte array.
     * @param srcIndex the starting index of the source byte array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of byte elements to be copied.
     */
    @ForceInline
    public static void copy(
            byte[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, MemoryLayouts.JAVA_BYTE, dstOffset, elementCount,
                Unsafe.ARRAY_BYTE_BASE_OFFSET, Unsafe.ARRAY_BYTE_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of byte elements from a source segment to a destination byte array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination byte array.
     * @param dstIndex the starting index of the destination byte array.
     * @param elementCount the number of byte elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            byte[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, MemoryLayouts.JAVA_BYTE, srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_BYTE_BASE_OFFSET, Unsafe.ARRAY_BYTE_INDEX_SCALE, dstArray.length);
    }

    //CHAR
    /**
     * Copies a number of char elements from a source char array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source char array.
     * @param srcIndex the starting index of the source char array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of char elements to be copied.
     */
    @ForceInline
    public static void copy(
            char[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of char elements from a source char array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source char array.
     * @param srcIndex the starting index of the source char array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of char elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            char[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount, ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_CHAR_NO, JAVA_CHAR_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_CHAR_BASE_OFFSET, Unsafe.ARRAY_CHAR_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of char elements from a source segment to a destination char array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination char array.
     * @param dstIndex the starting index of the destination char array.
     * @param elementCount the number of char elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            char[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of char elements from a source segment to a destination char array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination char array.
     * @param dstIndex the starting index of the destination char array.
     * @param elementCount the number of char elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            char[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_CHAR_NO, JAVA_CHAR_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_CHAR_BASE_OFFSET, Unsafe.ARRAY_CHAR_INDEX_SCALE, dstArray.length);
    }

    //SHORT
    /**
     * Copies a number of short elements from a source short array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source short array.
     * @param srcIndex the starting index of the source short array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of short elements to be copied.
     */
    @ForceInline
    public static void copy(
            short[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of short elements from a source short array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source short array.
     * @param srcIndex the starting index of the source short array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of short elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            short[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount,
            ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_SHORT_NO, JAVA_SHORT_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_SHORT_BASE_OFFSET, Unsafe.ARRAY_SHORT_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of short elements from a source segment to a destination short array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination short array.
     * @param dstIndex the starting index of the destination short array.
     * @param elementCount the number of short elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            short[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of short elements from a source segment to a destination short array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination short array.
     * @param dstIndex the starting index of the destination short array.
     * @param elementCount the number of short elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            short[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_SHORT_NO, JAVA_SHORT_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_SHORT_BASE_OFFSET, Unsafe.ARRAY_SHORT_INDEX_SCALE, dstArray.length);
    }

    //INT
    /**
     * Copies a number of int elements from a source int array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source int array.
     * @param srcIndex the starting index of the source int array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of int elements to be copied.
     */
    @ForceInline
    public static void copy(
            int[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of int elements from a source int array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source int array.
     * @param srcIndex the starting index of the source int array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of int elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            int[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount,
            ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_INT_NO, JAVA_INT_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_INT_BASE_OFFSET, Unsafe.ARRAY_INT_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of int elements from a source segment to a destination int array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination int array.
     * @param dstIndex the starting index of the destination int array.
     * @param elementCount the number of int elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            int[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of int elements from a source segment to a destination int array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination int array.
     * @param dstIndex the starting index of the destination int array.
     * @param elementCount the number of int elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            int[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_INT_NO, JAVA_INT_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_INT_BASE_OFFSET, Unsafe.ARRAY_INT_INDEX_SCALE, dstArray.length);
    }

    //FLOAT
    /**
     * Copies a number of float elements from a source float array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source float array.
     * @param srcIndex the starting index of the source float array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of float elements to be copied.
     */
    @ForceInline
    public static void copy(
            float[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of float elements from a source float array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source float array.
     * @param srcIndex the starting index of the source float array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of float elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            float[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount,
            ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_FLOAT_NO, JAVA_FLOAT_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_FLOAT_BASE_OFFSET, Unsafe.ARRAY_FLOAT_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of float elements from a source segment to a destination float array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination float array.
     * @param dstIndex the starting index of the destination float array.
     * @param elementCount the number of float elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            float[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of float elements from a source segment to a destination float array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination float array.
     * @param dstIndex the starting index of the destination float array.
     * @param elementCount the number of float elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a float swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            float[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_FLOAT_NO, JAVA_FLOAT_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_FLOAT_BASE_OFFSET, Unsafe.ARRAY_FLOAT_INDEX_SCALE, dstArray.length);
    }

    //LONG
    /**
     * Copies a number of long elements from a source long array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source long array.
     * @param srcIndex the starting index of the source long array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of long elements to be copied.
     */
    @ForceInline
    public static void copy(
            long[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of long elements from a source long array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source long array.
     * @param srcIndex the starting index of the source long array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of long elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            long[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount,
            ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_LONG_NO, JAVA_LONG_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_LONG_BASE_OFFSET, Unsafe.ARRAY_LONG_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of long elements from a source segment to a destination long array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination long array.
     * @param dstIndex the starting index of the destination long array.
     * @param elementCount the number of long elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            long[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of long elements from a source segment to a destination long array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination long array.
     * @param dstIndex the starting index of the destination long array.
     * @param elementCount the number of long elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            long[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_LONG_NO, JAVA_LONG_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_LONG_BASE_OFFSET, Unsafe.ARRAY_LONG_INDEX_SCALE, dstArray.length);
    }

    //DOUBLE
    /**
     * Copies a number of double elements from a source double array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source double array.
     * @param srcIndex the starting index of the source double array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of double elements to be copied.
     */
    @ForceInline
    public static void copy(
            double[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of double elements from a source double array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source double array.
     * @param srcIndex the starting index of the source double array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of double elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            double[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount,
            ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_DOUBLE_NO, JAVA_DOUBLE_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_DOUBLE_BASE_OFFSET, Unsafe.ARRAY_DOUBLE_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of double elements from a source segment to a destination double array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination double array.
     * @param dstIndex the starting index of the destination double array.
     * @param elementCount the number of double elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            double[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of double elements from a source segment to a destination double array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination double array.
     * @param dstIndex the starting index of the destination double array.
     * @param elementCount the number of double elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            double[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_DOUBLE_NO, JAVA_DOUBLE_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_DOUBLE_BASE_OFFSET, Unsafe.ARRAY_DOUBLE_INDEX_SCALE, dstArray.length);
    }

    /**
     * Copies a number of bytes from a source segment to a destination segment.
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * <p>
     * The result of a bulk copy is unspecified if, in the uncommon case, the source segment and this segment
     * do not overlap, but refer to overlapping regions of the same backing storage using different addresses.
     * For example, this may occur if the same file is {@linkplain MemorySegment#mapFile mapped} to two segments.
     * @param srcSegment the source segment.
     * @param dstSegment  the destination segment.
     * @param bytes the number of bytes to be copied.
     */
    @ForceInline
    public static void copy(MemorySegment srcSegment, MemorySegment dstSegment, long bytes) {
        copy(srcSegment, 0, dstSegment, 0, bytes);
    }

    /**
     * Copies a number of bytes from a source segment to a destination segment.
     * starting at a given source and destination offsets (expressed in bytes).
     * <p>
     * The result of a bulk copy is unspecified if, in the uncommon case, the source segment and this segment
     * do not overlap, but refer to overlapping regions of the same backing storage using different addresses.
     * For example, this may occur if the same file is {@linkplain MemorySegment#mapFile mapped} to two segments.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param bytes the number of bytes to be copied.
     */
    @ForceInline
    public static void copy(MemorySegment srcSegment, long srcOffset, MemorySegment dstSegment, long dstOffset, long bytes) {
        copy(srcSegment, MemoryLayouts.JAVA_BYTE, srcOffset, dstSegment, MemoryLayouts.JAVA_BYTE, dstOffset, bytes);
    }

    /**
     * Copies a number of elements (whose size is specified by the corresponding layout parameters) from a source segment
     * to a destination segment, starting at a given source and destination offsets (expressed in bytes).
     * <p>
     * The result of a bulk copy is unspecified if, in the uncommon case, the source segment and this segment
     * do not overlap, but refer to overlapping regions of the same backing storage using different addresses.
     * For example, this may occur if the same file is {@linkplain MemorySegment#mapFile mapped} to two segments.
     * @param srcSegment the source segment.
     * @param srcElementLayout the element layout associated with the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstSegment the destination segment.
     * @param dstElementLayout the element layout associated with the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of elements to be copied.
     * @throws IllegalArgumentException if the element layouts have different sizes, if the source offset is incompatible
     * with the alignment constraints in the source element layout, or if the destination offset is incompatible with the
     * alignment constraints in the destination element layout.
     */
    @ForceInline
    public static void copy(MemorySegment srcSegment, ValueLayout srcElementLayout, long srcOffset, MemorySegment dstSegment,
                                        ValueLayout dstElementLayout, long dstOffset, long elementCount) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(srcElementLayout);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(dstElementLayout);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        AbstractMemorySegmentImpl dstImpl = (AbstractMemorySegmentImpl)dstSegment;
        if (srcElementLayout.byteSize() != dstElementLayout.byteSize()) {
            throw new IllegalArgumentException("Source and destination layouts must have same sizes");
        }
        if (srcOffset % srcElementLayout.byteAlignment() != 0) {
            throw new IllegalArgumentException("Source segment incompatible with alignment constraints");
        }
        if (dstOffset % dstElementLayout.byteAlignment() != 0) {
            throw new IllegalArgumentException("Target segment incompatible with alignment constraints");
        }
        long size = elementCount * srcElementLayout.byteSize();
        if (size % srcElementLayout.byteSize() != 0) {
            throw new IllegalArgumentException("Segment size is not a multiple of layout size");
        }
        srcImpl.checkAccess(srcOffset, size, true);
        dstImpl.checkAccess(dstOffset, size, false);
        if (srcElementLayout.byteSize() == 1 || srcElementLayout.order() == dstElementLayout.order()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), dstImpl.scope(),
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstImpl.unsafeGetBase(), dstImpl.unsafeGetOffset() + dstOffset, size);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), dstImpl.scope(),
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstImpl.unsafeGetBase(), dstImpl.unsafeGetOffset() + dstOffset, size, srcElementLayout.byteSize());
        }
    }

    /**
     * Copies a number of elements from a source segment to a destination array, starting at a given segment offset
     * (expressed in bytes), and a given array index, and using the given source element layout.
     * @param srcSegment the source segment.
     * @param srcElementLayout the element layout associated with the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination array.
     * @param dstIndex the starting index of the destination array.
     * @param elementCount the number of array elements to be copied.
     * @throws IllegalArgumentException if {@code dstArray} is not an array, or if the source
     * element layout has a size that does not match that of the array elements, or if its alignment constraints
     * are incompatible with the source offset.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, ValueLayout srcElementLayout, long srcOffset,
            Object dstArray, int dstIndex, int elementCount) {
        Objects.requireNonNull(dstArray);
        int length = Array.getLength(dstArray); // throws if not an array
        copy(srcSegment, srcElementLayout, srcOffset, dstArray, dstIndex, elementCount,
                unsafe.arrayBaseOffset(dstArray.getClass()), unsafe.arrayIndexScale(dstArray.getClass()), length);
    }

    @ForceInline
    private static void copy(
            MemorySegment srcSegment, ValueLayout srcElementLayout, long srcOffset,
            Object dstArray, int dstIndex, int elementCount,
            int dstBase, int dstWidth, int dstLength) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(srcElementLayout);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffset, elementCount * dstWidth, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, dstLength);
        if (srcOffset % srcElementLayout.byteAlignment() != 0) {
            throw new IllegalArgumentException("Source offset incompatible with alignment constraints");
        }
        if (srcElementLayout.byteSize() != dstWidth) {
            throw new IllegalArgumentException("Array element size incompatible with segment element layout size");
        }
        if (srcElementLayout.order() == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, dstBase + (dstIndex * dstWidth), elementCount * dstWidth);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, dstBase + (dstIndex * dstWidth), elementCount * dstWidth, dstWidth);
        }
    }

    /**
     * Copies a number of elements from a source array to a destination segment, starting at a given array index, and a
     * given segment offset (expressed in bytes), using the given destination element layout.
     * @param srcArray the source array.
     * @param srcIndex the starting index of the source array.
     * @param dstSegment the destination segment.
     * @param dstElementLayout the element layout associated with the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of array elements to be copied.
     * @throws IllegalArgumentException if {@code srcArray} is not an array, or if the destination
     * element layout has a size that does not match that of the array elements, or if its alignment constraints
     * are incompatible with the destination offset.
     */
    @ForceInline
    public static void copy(
            Object srcArray, int srcIndex,
            MemorySegment dstSegment, ValueLayout dstElementLayout, long dstOffset, int elementCount) {
        Objects.requireNonNull(srcArray);
        int length = Array.getLength(srcArray); // throws if not an array
        copy(srcArray, srcIndex, dstSegment, dstElementLayout, dstOffset, elementCount,
                unsafe.arrayBaseOffset(srcArray.getClass()), unsafe.arrayIndexScale(srcArray.getClass()), length);
    }

    @ForceInline
    private static void copy(
            Object srcArray, int srcIndex,
            MemorySegment dstSegment, ValueLayout dstElementLayout, long dstOffset, int elementCount,
            int srcBase, int srcWidth, int srcLength) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(dstElementLayout);
        Objects.checkFromIndexSize(srcIndex, elementCount, srcLength);
        if (dstOffset % dstElementLayout.byteAlignment() != 0) {
            throw new IllegalArgumentException("Destination offset incompatible with alignment constraints");
        }
        if (dstElementLayout.byteSize() != srcWidth) {
            throw new IllegalArgumentException("Array element size incompatible with segment element layout size");
        }
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffset, elementCount * srcWidth, false);
        if (dstElementLayout.order() == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, srcBase + (srcIndex * srcWidth),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount * srcWidth);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, srcBase + (srcIndex * srcWidth),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount * srcWidth, srcWidth);
        }
    }
}
