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
 * Additional overloads are provided (see {@link #copy(double[], int, MemorySegment, long, int)}),
 * so that clients can omit the byte order parameter.
 *
 * <p> Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more {@code null}
 * elements to a method in this class causes a {@link NullPointerException} to be thrown. Moreover,
 * attempting to copy to/from a segment whose {@linkplain MemorySegment#scope() scope} has already been closed,
 * or from a thread other than the thread owning the scope causes an {@link IllegalStateException} to be thrown.
 * Moreover, attempting to copy to/from a segment (of {@linkplain MemorySegment#address() base address} {@code B} and
 * {@linkplain MemorySegment#byteSize() size} {@code S}) at addresses that are {@code < B}, or {@code >= B + S},
 * causes an {@link IndexOutOfBoundsException} to be thrown; similarly, attempting to copy to/from an array
 * (of length {@code L}) at indices that are {@code < 0}, or {@code >= L} causes an {@link IndexOutOfBoundsException} to be thrown.
 * Finally, attempting to copy data into a see {@linkplain MemorySegment#isReadOnly() read-only} always causes an
 * {@link UnsupportedOperationException} to be thrown.
 */
public final class MemoryCopy {

    private static final ScopedMemoryAccess scopedMemoryAccess = ScopedMemoryAccess.getScopedMemoryAccess();
    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private static final int BYTE_BASE = unsafe.arrayBaseOffset(byte[].class);
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
     * @param srcIndex the starting index of the source byte array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of byte elements to be copied.
     */
    @ForceInline
    public static void copy(
            byte[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.checkFromIndexSize(srcIndex, elementCount, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffset, elementCount, false);
        scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                srcArray, BYTE_BASE + srcIndex,
                destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount);
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
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffset, elementCount, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, dstArray.length);
        scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                dstArray, BYTE_BASE + dstIndex, elementCount);
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
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndex, elementCount, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffset, elementCount << 1, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, CHAR_BASE + (srcIndex << 1),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 1);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, CHAR_BASE + (srcIndex << 1),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 1, 2);
        }
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
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffset, elementCount << 1, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, CHAR_BASE + (dstIndex << 1), elementCount << 1);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, CHAR_BASE + (dstIndex << 1), elementCount << 1, 2);
        }
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
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndex, elementCount, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffset, elementCount << 1, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, SHORT_BASE + (srcIndex << 1),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 1);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, SHORT_BASE + (srcIndex << 1),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 1, 2);
        }
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
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffset, elementCount << 1, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, SHORT_BASE + (dstIndex << 1), elementCount << 1);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, SHORT_BASE + (dstIndex << 1), elementCount << 1, 2);
        }
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
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndex, elementCount, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffset, elementCount << 2, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, INT_BASE + (srcIndex << 2),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 2);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, INT_BASE + (srcIndex << 2),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 2, 4);
        }
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
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffset, elementCount << 2, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, INT_BASE + (dstIndex << 2), elementCount << 2);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, INT_BASE + (dstIndex << 2), elementCount << 2, 4);
        }
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
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndex, elementCount, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffset, elementCount << 2, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, FLOAT_BASE + (srcIndex << 2),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 2);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, FLOAT_BASE + (srcIndex << 2),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 2, 4);
        }
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
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffset, elementCount << 2, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, FLOAT_BASE + (dstIndex << 2), elementCount << 2);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, FLOAT_BASE + (dstIndex << 2), elementCount << 2, 4);
        }
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
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndex, elementCount, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffset, elementCount << 3, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, LONG_BASE + (srcIndex << 3),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 3);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, LONG_BASE + (srcIndex << 3),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 3, 8);
        }
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
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffset, elementCount << 3, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, LONG_BASE + (dstIndex << 3), elementCount << 3);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, LONG_BASE + (dstIndex << 3), elementCount << 3, 8);
        }
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
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        Objects.checkFromIndexSize(srcIndex, elementCount, srcArray.length);
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffset, elementCount << 3, false);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, DOUBLE_BASE + (srcIndex << 3),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 3);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, DOUBLE_BASE + (srcIndex << 3),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount << 3, 8);
        }
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
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffset, elementCount << 3, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, dstArray.length);
        if (order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, DOUBLE_BASE + (dstIndex << 3), elementCount << 3);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, DOUBLE_BASE + (dstIndex << 3), elementCount << 3, 8);
        }
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
}
