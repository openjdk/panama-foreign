/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 * This class provides convenient methods for copying data between primitive arrays and MemorySegments.
 *
 * <p>If the source (destination) segment is actually a view of the destination (source) array,
 * and if the copy region of the source overlaps with the copy region of the destination,
 * the copy of the overlapping region is performed as if the data in the overlapping region
 * were first copied into a temporary segment before being copied to the destination.</p>
 *
 * <p>To improve clarity the starting offset into a MemorySegment always has a variable name that includes the word
 * "offset" and the relevant units.  For example, "srcOffsetBytes".  The starting offset into a primitive array has a
 * variable name that includes the word "index" and the relevant units. For example, "srcIndexChars.</p>
 *
 * <p>In cases where native byte order is preferred, overloads are provided
 * (For example, see {@link #copyFromArray(double[], int, int, MemorySegment, long)}),
 * so that clients can omit the byte order parameter.</p>
 *
 * <p>Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more
 * {@code null} elements to a method in this class causes a {@link NullPointerException NullPointerException}
 * to be thrown.</p>
 */
@SuppressWarnings({"checkstyle:FinalLocalVariable", "CheckStyle"})
public final class MemoryCopy {
  private static final ByteOrder nativeByteOrder = ByteOrder.nativeOrder();
  private static final ByteOrder nonNativeByteOrder = nativeByteOrder == ByteOrder.LITTLE_ENDIAN
          ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
  private static final ValueLayout arrLayout16N = MemoryLayout.valueLayout(16, nativeByteOrder);
  private static final ValueLayout segLayout16NN = MemoryLayout.valueLayout(16, nonNativeByteOrder);
  private static final ValueLayout arrLayout32N = MemoryLayout.valueLayout(32, nativeByteOrder);
  private static final ValueLayout segLayout32NN = MemoryLayout.valueLayout(32, nonNativeByteOrder);
  private static final ValueLayout arrLayout64N = MemoryLayout.valueLayout(64, nativeByteOrder);
  private static final ValueLayout segLayout64NN = MemoryLayout.valueLayout(64, nonNativeByteOrder);

  private MemoryCopy() { /* singleton */ }

  //BYTE
  /**
   * Copies from a source byte array, starting at a source index in bytes and extending for a given length in bytes,
   * to a destination segment starting at a destination offset in bytes.
   * @param srcArray the source byte array.
   * @param srcIndexBytes the starting index of the source byte array.
   * @param srcCopyLengthBytes the length of the copy operation in bytes.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
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
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination byte array, starting at a destination index in bytes and extending for a given length in bytes.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination byte array.
   * @param dstIndexBytes the starting index of the destination byte array.
   * @param dstCopyLengthBytes the length in bytes of the copy operation.
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
   * Copies from a source char array, starting at a source index in chars and extending for a given length in chars,
   * to a destination segment starting at a destination offset in bytes,
   * using the native byte order.
   * @param srcArray the source char array.
   * @param srcIndexChars the starting index in chars of the source char array.
   * @param srcCopyLengthChars the length of the copy operation in chars.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   */
  @ForceInline
  public static void copyFromArray(
          char[] srcArray, int srcIndexChars, int srcCopyLengthChars,
          MemorySegment dstSegment, long dstOffsetBytes) {
    copyFromArray(srcArray, srcIndexChars, srcCopyLengthChars, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source char array, starting at a source index in chars and extending for a given length in chars,
   * to a destination segment starting at a destination offset in bytes,
   * using the given byte order.
   * @param srcArray the source char array.
   * @param srcIndexChars the starting index in chars of the source char array.
   * @param srcCopyLengthChars the length of the copy operation in chars.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, arrLayout16N, segLayout16NN);
    }
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination char array, starting at a destination index in chars and extending for a given length in chars,
   * using the native byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination char array.
   * @param dstIndexChars the starting index of the destination char array.
   * @param dstCopyLengthChars the length in chars of the copy operation.
   */
  @ForceInline
  public static void copyToArray(
          MemorySegment srcSegment, long srcOffsetBytes,
          char[] dstArray, int dstIndexChars, int dstCopyLengthChars) {
    copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexChars, dstCopyLengthChars, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination char array, starting at a destination index in chars and extending for a given length in chars,
   * using the given byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination char array.
   * @param dstIndexChars the starting index of the destination char array.
   * @param dstCopyLengthChars the length in chars of the copy operation.
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, segLayout16NN, arrLayout16N);
    }
  }

  //DOUBLE
  /**
   * Copies from a source double array, starting at a source index in doubles and extending for a given length
   * in doubles, to a destination segment starting at a destination offset in bytes,
   * using the native byte order.
   * @param srcArray the source double array.
   * @param srcIndexDoubles the starting index of the source double array.
   * @param srcCopyLengthDoubles the length of the copy operation in doubles.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   */
  @ForceInline
  public static void copyFromArray(
          double[] srcArray, int srcIndexDoubles, int srcCopyLengthDoubles,
          MemorySegment dstSegment, long dstOffsetBytes) {
    copyFromArray(srcArray, srcIndexDoubles, srcCopyLengthDoubles, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source double array, starting at a source index in doubles and extending for a given length
   * in doubles, to a destination segment starting at a destination offset in bytes,
   * using the given byte order.
   * @param srcArray the source double array.
   * @param srcIndexDoubles the starting index of the source double array.
   * @param srcCopyLengthDoubles the length of the copy operation in doubles.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, arrLayout64N, segLayout64NN);
    }
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination double array, starting at a destination index in doubles and extending for a given length
   * in doubles, using the native byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination double array.
   * @param dstIndexDoubles the starting index of the destination double array.
   * @param dstCopyLengthDoubles the length in doubles of the copy operation.
   */
  @ForceInline
  public static void copyToArray(
          MemorySegment srcSegment, long srcOffsetBytes,
          double[] dstArray, int dstIndexDoubles, int dstCopyLengthDoubles) {
    copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexDoubles, dstCopyLengthDoubles, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination double array, starting at a destination index in doubles and extending for a given length
   * in doubles, using the given byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination double array.
   * @param dstIndexDoubles the starting index of the destination double array.
   * @param dstCopyLengthDoubles the length in doubles of the copy operation.
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, segLayout64NN, arrLayout64N);
    }
  }

  //FLOAT
  /**
   * Copies from a source float array, starting at a source index in floats and extending for a given length in floats,
   * to a destination segment starting at a destination offset in bytes,
   * using the native byte order.
   * @param srcArray the source float array.
   * @param srcIndexFloats the starting index of the source float array.
   * @param srcCopyLengthFloats the length of the copy operation in floats.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   */
  @ForceInline
  public static void copyFromArray(
          float[] srcArray, int srcIndexFloats, int srcCopyLengthFloats,
          MemorySegment dstSegment, long dstOffsetBytes) {
    copyFromArray(srcArray, srcIndexFloats, srcCopyLengthFloats, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source float array, starting at a source index in floats and extending for a given length in floats,
   * to a destination segment starting at a destination offset in bytes,
   * using the given byte order.
   * @param srcArray the source float array.
   * @param srcIndexFloats the starting index of the source float array.
   * @param srcCopyLengthFloats the length of the copy operation in floats.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, arrLayout32N, segLayout32NN);
    }
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination float array, starting at a destination index in floats and extending for a given length in floats,
   * using the native byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination float array.
   * @param dstIndexFloats the starting index of the destination float array.
   * @param dstCopyLengthFloats the length in floats of the copy operation.
   */
  @ForceInline
  public static void copyToArray(
          MemorySegment srcSegment, long srcOffsetBytes,
          float[] dstArray, int dstIndexFloats, int dstCopyLengthFloats) {
    copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexFloats, dstCopyLengthFloats, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination float array, starting at a destination index in floats and extending for a given length in floats,
   * using the given byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination float array.
   * @param dstIndexFloats the starting index of the destination float array.
   * @param dstCopyLengthFloats the length in floats of the copy operation.
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, segLayout32NN, arrLayout32N);
    }
  }

  //INT
  /**
   * Copies from a source int array, starting at a source index in ints and extending for a given length in ints,
   * to a destination segment starting at a destination offset in bytes,
   * using the native byte order.
   * @param srcArray the source int array.
   * @param srcIndexInts the starting index of the source int array.
   * @param srcCopyLengthInts the length of the copy operation in ints.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   */
  @ForceInline
  public static void copyFromArray(
          int[] srcArray, int srcIndexInts, int srcCopyLengthInts,
          MemorySegment dstSegment, long dstOffsetBytes) {
    copyFromArray(srcArray, srcIndexInts, srcCopyLengthInts, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source int array, starting at a source index in ints and extending for a given length in ints,
   * to a destination segment starting at a destination offset in bytes,
   * using the given byte order.
   * @param srcArray the source int array.
   * @param srcIndexInts the starting index of the source int array.
   * @param srcCopyLengthInts the length of the copy operation in ints.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, arrLayout32N, segLayout32NN);
    }
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination int array, starting at a destination index in ints and extending for a given length in ints,
   * using the native byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination int array.
   * @param dstIndexInts the starting index of the destination int array.
   * @param dstCopyLengthInts the length in ints of the copy operation.
   */
  @ForceInline
  public static void copyToArray(
          MemorySegment srcSegment, long srcOffsetBytes,
          int[] dstArray, int dstIndexInts, int dstCopyLengthInts) {
    copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexInts, dstCopyLengthInts, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination int array, starting at a destination index in ints and extending for a given length in ints,
   * using the given byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination int array.
   * @param dstIndexInts the starting index of the destination int array.
   * @param dstCopyLengthInts the length in ints of the copy operation.
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, segLayout32NN, arrLayout32N);
    }
  }

  //LONG
  /**
   * Copies from a source long array, starting at a source index in longs and extending for a given length in longs,
   * to a destination segment starting at a destination offset in bytes,
   * using the native byte order.
   * @param srcArray the source long array.
   * @param srcIndexLongs the starting index of the source long array.
   * @param srcCopyLengthLongs the length of the copy operation in longs.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   */
  @ForceInline
  public static void copyFromArray(
          long[] srcArray, int srcIndexLongs, int srcCopyLengthLongs,
          MemorySegment dstSegment, long dstOffsetBytes) {
    copyFromArray(srcArray, srcIndexLongs, srcCopyLengthLongs, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source long array, starting at a source index in longs and extending for a given length in longs,
   * to a destination segment starting at a destination offset in bytes,
   * using the given byte order.
   * @param srcArray the source long array.
   * @param srcIndexLongs the starting index of the source long array.
   * @param srcCopyLengthLongs the length of the copy operation in longs.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, arrLayout64N, segLayout64NN);
    }
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination long array, starting at a destination index in longs and extending for a given length in longs,
   * using the native byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination long array.
   * @param dstIndexLongs the starting index of the destination long array.
   * @param dstCopyLengthLongs the length in longs of the copy operation.
   */
  @ForceInline
  public static void copyToArray(
          MemorySegment srcSegment, long srcOffsetBytes,
          long[] dstArray, int dstIndexLongs, int dstCopyLengthLongs) {
    copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexLongs, dstCopyLengthLongs, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination long array, starting at a destination index in longs and extending for a given length in longs,
   * using the given byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination long array.
   * @param dstIndexLongs the starting index of the destination long array.
   * @param dstCopyLengthLongs the length in longs of the copy operation.
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, segLayout64NN, arrLayout64N);
    }
  }

  //SHORT
  /**
   * Copies from a source short array, starting at a source index in shorts and extending for a given length in shorts,
   * to a destination segment starting at a destination offset in bytes,
   * using the native byte order.
   * @param srcArray the source short array.
   * @param srcIndexShorts the starting index of the source short array.
   * @param srcCopyLengthShorts the length of the copy operation in shorts.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   */
  @ForceInline
  public static void copyFromArray(
          short[] srcArray, int srcIndexShorts, int srcCopyLengthShorts,
          MemorySegment dstSegment, long dstOffsetBytes) {
    copyFromArray(srcArray, srcIndexShorts, srcCopyLengthShorts, dstSegment,dstOffsetBytes, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source short array, starting at a source index in shorts and extending for a given length in shorts,
   * to a destination segment starting at a destination offset in bytes,
   * using the given byte order.
   * @param srcArray the source short array.
   * @param srcIndexShorts the starting index of the source short array.
   * @param srcCopyLengthShorts the length of the copy operation in shorts.
   * @param dstSegment the destination segment
   * @param dstOffsetBytes the starting offset in bytes of the destination segment
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, arrLayout16N, segLayout16NN);
    }
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination short array, starting at a destination index in shorts and extending for a given length in shorts,
   * using the native byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination short array.
   * @param dstIndexShorts the starting index of the destination short array.
   * @param dstCopyLengthShorts the length in shorts of the copy operation.
   */
  @ForceInline
  public static void copyToArray(
          MemorySegment srcSegment, long srcOffsetBytes,
          short[] dstArray, int dstIndexShorts, int dstCopyLengthShorts) {
    copyToArray(srcSegment, srcOffsetBytes, dstArray, dstIndexShorts, dstCopyLengthShorts, ByteOrder.nativeOrder());
  }

  /**
   * Copies from a source segment, starting at a source offset in bytes,
   * to a destination short array, starting at a destination index shorts and extending for a given length in shorts,
   * using the given byte order.
   * @param srcSegment the source segment.
   * @param srcOffsetBytes the starting offset in bytes of the source segment.
   * @param dstArray the destination short array.
   * @param dstIndexShorts the starting index of the destination short array.
   * @param dstCopyLengthShorts the length in shorts of the copy operation.
   * @param order the byte order to be used for the copy operation. If the specified byte order is
   * different from the native order, a byte swap operation will be performed.
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
    if (order == nativeByteOrder) {
      dstSegmentSlice.copyFrom(srcSegmentSlice);
    } else {
      dstSegmentSlice.copyFrom(srcSegmentSlice, segLayout16NN, arrLayout16N);
    }
  }

}
