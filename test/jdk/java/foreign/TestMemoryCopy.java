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

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * These tests exercise the MemoryCopy copyFromArray(...) and copyToArray(...).
 * To make these tests more challenging the segment is a view of the given array,
 * which makes the copy operations overlapping self-copies.  Thus, this checks the claim:
 *
 * <p>If the source (destination) segment is actually a view of the destination (source) array,
 * and if the copy region of the source overlaps with the copy region of the destination,
 * the copy of the overlapping region is performed as if the data in the overlapping region
 * were first copied into a temporary segment before being copied to the destination.</p>
 *
 * <p>Note: non-native byte order tests are not included here.</p>
 */
@SuppressWarnings({"ConstantConditions"})
public class TestMemoryCopy {

  //BYTE ARRAY
  @Test
  public void checkByteArrSelfCopy() {
    checkByteArr(true);
    checkByteArr(false);
  }

  private void checkByteArr(boolean up) {
    int segLengthBytes = 32;
    int segOffsetBytes = 8;
    int bytesPerElement = 1; // f(array type)
    int arrIndex = segOffsetBytes / bytesPerElement;
    MemorySegment base = buildSegment(0, segLengthBytes, 0, up);
    MemorySegment truth = buildSegment(0, segLengthBytes, segOffsetBytes, up);
    //CopyFrom
    byte[] srcArr = base.toByteArray(); // f(array type)
    int srcIndex = up ? 0 : arrIndex;
    int srcCopyLen = srcArr.length - arrIndex;
    MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
    long dstOffsetBytes = up ? segOffsetBytes : 0;
    MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
    assertEquals(truth.mismatch(dstSeg), -1);
    //CopyTo
    long srcOffsetBytes = up ? 0 : segOffsetBytes;
    byte[] dstArr = base.toByteArray(); // f(array type)
    MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
    int dstIndex = up ? arrIndex : 0;
    int dstCopyLen = dstArr.length - arrIndex;
    MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
    MemorySegment result = MemorySegment.ofArray(dstArr);
    assertEquals(truth.mismatch(result), -1);
  }

  //INT ARRAY
  @Test
  public void checkIntArrSelfCopy() {
    checkIntArr(true);
    checkIntArr(false);
  }

  private void checkIntArr(boolean up) {
    int segLengthBytes = 32;
    int segOffsetBytes = 8;
    int bytesPerElement = 4;  // f(array type)
    int arrIndex = segOffsetBytes / bytesPerElement;
    MemorySegment base = buildSegment(0, segLengthBytes, 0, up);
    MemorySegment truth = buildSegment(0, segLengthBytes, segOffsetBytes, up);
    //CopyFrom
    int[] srcArr = base.toIntArray(); // f(array type)
    int srcIndex = up ? 0 : arrIndex;
    int srcCopyLen = srcArr.length - arrIndex;
    MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
    long dstOffsetBytes = up ? segOffsetBytes : 0;
    MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
    assertEquals(truth.mismatch(dstSeg), -1);
    //CopyTo
    long srcOffsetBytes = up ? 0 : segOffsetBytes;
    int[] dstArr = base.toIntArray(); // f(array type)
    MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
    int dstIndex = up ? arrIndex : 0;
    int dstCopyLen = dstArr.length - arrIndex;
    MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
    MemorySegment result = MemorySegment.ofArray(dstArr);
    assertEquals(truth.mismatch(result), -1);
  }

  //CHAR ARRAY
  @Test
  public void checkCharArrSelfCopy() {
    checkCharArr(true);
    checkCharArr(false);
  }

  private void checkCharArr(boolean up) {
    int segLengthBytes = 32;
    int segOffsetBytes = 8;
    int bytesPerElement = 2;  // f(array type)
    int arrIndex = segOffsetBytes / bytesPerElement;
    MemorySegment base = buildSegment(0, segLengthBytes, 0, up);
    MemorySegment truth = buildSegment(0, segLengthBytes, segOffsetBytes, up);
    //CopyFrom
    char[] srcArr = base.toCharArray(); // f(array type)
    int srcIndex = up ? 0 : arrIndex;
    int srcCopyLen = srcArr.length - arrIndex;
    MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
    long dstOffsetBytes = up ? segOffsetBytes : 0;
    MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
    assertEquals(truth.mismatch(dstSeg), -1);
    //CopyTo
    long srcOffsetBytes = up ? 0 : segOffsetBytes;
    char[] dstArr = base.toCharArray(); // f(array type)
    MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
    int dstIndex = up ? arrIndex : 0;
    int dstCopyLen = dstArr.length - arrIndex;
    MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
    MemorySegment result = MemorySegment.ofArray(dstArr);
    assertEquals(truth.mismatch(result), -1);
  }

  //SHORT ARRAY
  @Test
  public void checkShortArrSelfCopy() {
    checkShortArr(true);
    checkShortArr(false);
  }

  private void checkShortArr(boolean up) {
    int segLengthBytes = 32;
    int segOffsetBytes = 8;
    int bytesPerElement = 2;  // f(array type)
    int arrIndex = segOffsetBytes / bytesPerElement;
    MemorySegment base = buildSegment(0, segLengthBytes, 0, up);
    MemorySegment truth = buildSegment(0, segLengthBytes, segOffsetBytes, up);
    //CopyFrom
    short[] srcArr = base.toShortArray(); // f(array type)
    int srcIndex = up ? 0 : arrIndex;
    int srcCopyLen = srcArr.length - arrIndex;
    MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
    long dstOffsetBytes = up ? segOffsetBytes : 0;
    MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
    assertEquals(truth.mismatch(dstSeg), -1);
    //CopyTo
    long srcOffsetBytes = up ? 0 : segOffsetBytes;
    short[] dstArr = base.toShortArray(); // f(array type)
    MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
    int dstIndex = up ? arrIndex : 0;
    int dstCopyLen = dstArr.length - arrIndex;
    MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
    MemorySegment result = MemorySegment.ofArray(dstArr);
    assertEquals(truth.mismatch(result), -1);
  }

  //LONG ARRAY
  @Test
  public void checkLongArrSelfCopy() {
    checkLongArr(true);
    checkLongArr(false);
  }

  private void checkLongArr(boolean up) {
    int segLengthBytes = 32;
    int segOffsetBytes = 8;
    int bytesPerElement = 8;  // f(array type)
    int arrIndex = segOffsetBytes / bytesPerElement;
    MemorySegment base = buildSegment(0, segLengthBytes, 0, up);
    MemorySegment truth = buildSegment(0, segLengthBytes, segOffsetBytes, up);
    //CopyFrom
    long[] srcArr = base.toLongArray(); // f(array type)
    int srcIndex = up ? 0 : arrIndex;
    int srcCopyLen = srcArr.length - arrIndex;
    MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
    long dstOffsetBytes = up ? segOffsetBytes : 0;
    MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
    assertEquals(truth.mismatch(dstSeg), -1);
    //CopyTo
    long srcOffsetBytes = up ? 0 : segOffsetBytes;
    long[] dstArr = base.toLongArray(); // f(array type)
    MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
    int dstIndex = up ? arrIndex : 0;
    int dstCopyLen = dstArr.length - arrIndex;
    MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
    MemorySegment result = MemorySegment.ofArray(dstArr);
    assertEquals(truth.mismatch(result), -1);
  }

  //FlOAT ARRAY
  @Test
  public void checkFloatArrSelfCopy() {
    checkFloatArr(true);
    checkFloatArr(false);
  }

  private void checkFloatArr(boolean up) {
    int segLengthBytes = 32;
    int segOffsetBytes = 8;
    int bytesPerElement = 4;
    int arrIndex = segOffsetBytes / bytesPerElement;
    int arrLength = segLengthBytes / segOffsetBytes; //this is done differently
    MemorySegment base = buildFloatSegment((float)Math.PI, arrLength, 0, up);
    MemorySegment truth = buildFloatSegment((float)Math.PI, arrLength, arrIndex, up);
    //CopyFrom
    float[] srcArr = base.toFloatArray(); // f(array type)
    int srcIndex = up ? 0 : arrIndex;
    int srcCopyLen = srcArr.length - arrIndex;
    MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
    long dstOffsetBytes = up ? segOffsetBytes : 0;
    MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
    assertEquals(truth.mismatch(dstSeg), -1);
    //CopyTo
    long srcOffsetBytes = up ? 0 : segOffsetBytes;
    float[] dstArr = base.toFloatArray(); // f(array type)
    MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
    int dstIndex = up ? arrIndex : 0;
    int dstCopyLen = dstArr.length - arrIndex;
    MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
    MemorySegment result = MemorySegment.ofArray(dstArr);
    assertEquals(truth.mismatch(result), -1);
  }

  //DOUBLE ARRAY
  @Test
  public void checkDoubleArrSelfCopy() {
    checkDoubleArr(true);
    checkDoubleArr(false);
  }

  private void checkDoubleArr(boolean up) {
    int segLengthBytes = 32;
    int segOffsetBytes = 8;
    int bytesPerElement = 8; // f(array type)
    int arrIndex = segOffsetBytes / bytesPerElement;
    int arrLength = segLengthBytes / segOffsetBytes; //this is done differently
    MemorySegment base = buildDoubleSegment(Math.PI, arrLength, 0, up); // f(array type)
    MemorySegment truth = buildDoubleSegment(Math.PI, arrLength, arrIndex, up); // f(array type)
    //CopyFrom
    double[] srcArr = base.toDoubleArray(); // f(array type)
    int srcIndex = up ? 0 : arrIndex;
    int srcCopyLen = srcArr.length - arrIndex;
    MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
    long dstOffsetBytes = up ? segOffsetBytes : 0;
    MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
    assertEquals(truth.mismatch(dstSeg), -1);
    //CopyTo
    long srcOffsetBytes = up ? 0 : segOffsetBytes;
    double[] dstArr = base.toDoubleArray(); // f(array type)
    MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
    int dstIndex = up ? arrIndex : 0;
    int dstCopyLen = dstArr.length - arrIndex;
    MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
    MemorySegment result = MemorySegment.ofArray(dstArr);
    assertEquals(truth.mismatch(result), -1);
  }

  /**
   * Builds base segment for integral tests, a sequence of positive byte values.
   * @param startValue a value
   * @param segLengthBytes less than 128
   * @param segOffsetBytes less than segLengthBytes
   * @param up if true shift up, else down
   * @return a MemorySegment
   */
  @SuppressWarnings({"SameParameterValue", "ManualArrayCopy"})
  private MemorySegment buildSegment(int startValue, int segLengthBytes, int segOffsetBytes, boolean up) {
    byte[] arr = new byte[segLengthBytes];
    for (int i = 0; i < segLengthBytes; i++) { arr[i] = (byte)(i + startValue); }
    if (segOffsetBytes == 0) { return MemorySegment.ofArray(arr); }
    if (up) {
      for (int i = segLengthBytes - 1; i >= segOffsetBytes; i--) { arr[i] = arr[i - segOffsetBytes]; }
    } else { //down
      for (int i = segOffsetBytes; i < segLengthBytes; i++) { arr[i - segOffsetBytes] = arr[i]; }
    }
    return MemorySegment.ofArray(arr);
  }

  /**
   * Builds base segment for float tests, a sequence of values.
   * @param startValue a value
   * @param arrLengthFloats less than 128
   * @param arrIndexFloats less than arrLengthFloats
   * @param up if true shift up, else down
   * @return a MemorySegment
   */
  @SuppressWarnings({"SameParameterValue", "ManualArrayCopy"})
  private MemorySegment buildFloatSegment(float startValue, int arrLengthFloats, int arrIndexFloats, boolean up) {
    float[] arr = new float[arrLengthFloats];
    for (int i = 0; i < arrLengthFloats; i++) { arr[i] = i + startValue; }
    if (up) {
      for (int i = arrLengthFloats - 1; i >= arrIndexFloats; i--) { arr[i] = arr[i - arrIndexFloats]; }
    } else { //down
      for (int i = arrIndexFloats; i < arrLengthFloats; i++) { arr[i - arrIndexFloats] = arr[i]; }
    }
    return MemorySegment.ofArray(arr);
  }

  /**
   * Builds base segment for double tests, a sequence of values.
   * @param startValue a value
   * @param arrLengthDoubles less than 128
   * @param arrIndexDoubles less than arrLengthDoubles
   * @param up if true shift up, else down
   * @return a MemorySegment
   */
  @SuppressWarnings({"SameParameterValue", "ManualArrayCopy"})
  private MemorySegment buildDoubleSegment(double startValue, int arrLengthDoubles, int arrIndexDoubles, boolean up) {
    double[] arr = new double[arrLengthDoubles];
    for (int i = 0; i < arrLengthDoubles; i++) { arr[i] = i + startValue; }
    if (up) {
      for (int i = arrLengthDoubles - 1; i >= arrIndexDoubles; i--) { arr[i] = arr[i - arrIndexDoubles]; }
    } else { //down
      for (int i = arrIndexDoubles; i < arrLengthDoubles; i++) { arr[i - arrIndexDoubles] = arr[i]; }
    }
    return MemorySegment.ofArray(arr);
  }

  //@Test //visual debug check only
  @SuppressWarnings("unused")
  private void checkBuildFloatSegment() {
    MemorySegment seg = buildFloatSegment((float)Math.PI, 8, 2, false);
    float[] arr = seg.toFloatArray();
    for (float v : arr) {
      println(v);
    }
  }

  //@Test //visual debug check only
  @SuppressWarnings("unused")
  private void checkBuildDoubleSegment() {
    MemorySegment seg = buildDoubleSegment(Math.PI, 4, 1, false);
    double[] arr = seg.toDoubleArray();
    for (double v : arr) {
      println(v);
    }
  }

  //@Test //visual debug check only
  @SuppressWarnings("unused")
  private void printSegment(MemorySegment seg) {
    int len = (int) seg.byteSize();
    for (int i = 0; i < len; i++) {
      println(i + "\t" + MemoryAccess.getByteAtOffset(seg, i));
    }
  }

  @SuppressWarnings("unused") //used for debug
  private static void println(Object o) { System.out.println(o.toString()); }
}
