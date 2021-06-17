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

import static org.testng.Assert.assertEquals;

import java.nio.ByteOrder;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryCopy;
import jdk.incubator.foreign.MemorySegment;

import org.testng.annotations.Test;

/**
 * These tests exercise the MemoryCopy copyFromArray(...) and copyToArray(...).
 * To make these tests more challenging the segment is a view of the given array,
 * which makes the copy operations overlapping self-copies.  Thus, this checks the claim:
 *
 * <p>If the source (destination) segment is actually a view of the destination (source) array,
 * and if the copy region of the source overlaps with the copy region of the destination,
 * the copy of the overlapping region is performed as if the data in the overlapping region
 * were first copied into a temporary segment before being copied to the destination.</p>
 */
public class TestMemoryCopy {
    private static final ByteOrder nativeByteOrder = ByteOrder.nativeOrder();
    private static final ByteOrder nonNativeByteOrder = nativeByteOrder == ByteOrder.LITTLE_ENDIAN
            ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    private static final boolean up = true;
    private static final boolean down = false;

    //BYTE ARRAY
    @Test
    public void checkByteArrSelfCopy() {
        checkByteArr(up);
        checkByteArr(down);
    }

    private void checkByteArr(boolean direction) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 1; // f(array type)
        int arrIndex = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcByteSegment(0, segLengthBytes);
        MemorySegment truth = truthSegment1Byte(base, 8, direction);
        //CopyFrom
        byte[] srcArr = base.toByteArray(); // f(array type)
        int srcIndex = direction ? 0 : arrIndex;
        int srcCopyLen = srcArr.length - arrIndex;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = direction ? 0 : segOffsetBytes;
        byte[] dstArr = base.toByteArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = direction ? arrIndex : 0;
        int dstCopyLen = dstArr.length - arrIndex;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //CHAR ARRAY
    @Test
    public void checkCharArrSelfCopy() {
        checkCharArr(up, true);
        checkCharArr(down, true);
        checkCharArr(up, false);
        checkCharArr(down, false);
    }

    private void checkCharArr(boolean direction, boolean swap) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 2;  // f(array type)
        int indexShifts = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcByteSegment(0, segLengthBytes);
        MemorySegment truth = truthSegment2Bytes(base, indexShifts, direction, swap);
        ByteOrder bo = swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        char[] srcArr = base.toCharArray(); // f(array type)
        int srcIndex = direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = direction ? 0 : segOffsetBytes;
        char[] dstArr = base.toCharArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //DOUBLE ARRAY
    @Test
    public void checkDoubleArrSelfCopy() {
        checkDoubleArr(up, true);
        checkDoubleArr(down, true);
        checkDoubleArr(up, false);
        checkDoubleArr(down, false);
    }

    private void checkDoubleArr(boolean direction, boolean swap) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 8; // f(array type)
        int indexShifts = segOffsetBytes / bytesPerElement;
        int arrLength = segLengthBytes / bytesPerElement; //this is done differently
        MemorySegment base = srcDoubleSegment(Math.PI, arrLength); // f(array type)
        MemorySegment truth = truthSegment8Bytes(base, indexShifts, direction, swap); // f(array type)
        ByteOrder bo = swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        double[] srcArr = base.toDoubleArray(); // f(array type)
        int srcIndex = direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = direction ? 0 : segOffsetBytes;
        double[] dstArr = base.toDoubleArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //FlOAT ARRAY
    @Test
    public void checkFloatArrSelfCopy() {
        checkFloatArr(up, true);
        checkFloatArr(down, true);
        checkFloatArr(up, false);
        checkFloatArr(down, false);
    }

    private void checkFloatArr(boolean direction, boolean swap) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 4;
        int indexShifts = segOffsetBytes / bytesPerElement;
        int arrLength = segLengthBytes / bytesPerElement; //this is done differently
        MemorySegment base = srcFloatSegment((float) Math.PI, arrLength); // f(array type)
        MemorySegment truth = truthSegment4Bytes(base, indexShifts, direction, swap); // f(array type)
        ByteOrder bo = swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        float[] srcArr = base.toFloatArray(); // f(array type)
        int srcIndex = direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = direction ? 0 : segOffsetBytes;
        float[] dstArr = base.toFloatArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //INT ARRAY
    @Test
    public void checkIntArrSelfCopy() {
        checkIntArr(up, true);
        checkIntArr(down, true);
        checkIntArr(up, false);
        checkIntArr(down, false);
    }

    private void checkIntArr(boolean direction, boolean swap) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 4;  // f(array type)
        int indexShifts = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcByteSegment(0, segLengthBytes);
        MemorySegment truth = truthSegment4Bytes(base, indexShifts, direction, swap);
        ByteOrder bo = swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        int[] srcArr = base.toIntArray(); // f(array type)
        int srcIndex = direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = direction ? 0 : segOffsetBytes;
        int[] dstArr = base.toIntArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //LONG ARRAY
    @Test
    public void checkLongArrSelfCopy() {
        checkLongArr(up, true);
        checkLongArr(down, true);
        checkLongArr(up, false);
        checkLongArr(down, false);
    }

    private void checkLongArr(boolean direction, boolean swap) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 8;  // f(array type)
        int indexShifts = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcByteSegment(0, segLengthBytes);
        MemorySegment truth = truthSegment8Bytes(base, indexShifts, direction, swap);
        ByteOrder bo = swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        long[] srcArr = base.toLongArray(); // f(array type)
        int srcIndex = direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = direction ? 0 : segOffsetBytes;
        long[] dstArr = base.toLongArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //SHORT ARRAY
    @Test
    public void checkShortArrSelfCopy() {
        checkShortArr(up, true);
        checkShortArr(down, true);
        checkShortArr(up, false);
        checkShortArr(down, false);
    }

    private void checkShortArr(boolean direction, boolean swap) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 2;  // f(array type)
        int indexShifts = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcByteSegment(0, segLengthBytes);
        MemorySegment truth = truthSegment2Bytes(base, indexShifts, direction, swap);
        ByteOrder bo = swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        short[] srcArr = base.toShortArray(); // f(array type)
        int srcIndex = direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = direction ? 0 : segOffsetBytes;
        short[] dstArr = base.toShortArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    /* ************** */

    public static MemorySegment srcByteSegment(int startValue, int length) {
        byte[] arr = new byte[length];
        for (int i = 0; i < length; i++) {
            arr[i] = (byte) (startValue + i);
        }
        return MemorySegment.ofArray(arr);
    }

    public static MemorySegment srcFloatSegment(float startValue, int length) {
        float[] arr = new float[length];
        for (int i = 0; i < length; i++) {
            arr[i] = startValue + i;
        }
        return MemorySegment.ofArray(arr);
    }

    public static MemorySegment srcDoubleSegment(double startValue, int length) {
        double[] arr = new double[length];
        for (int i = 0; i < length; i++) {
            arr[i] = startValue + i;
        }
        return MemorySegment.ofArray(arr);
    }

    /* ************** */

    /**
     * Builds a truth segment of multibyte primitives with a byte-length of 1.
     * @param srcSeg The source segment prior to any copy-shifts
     * @param indexShifts the number of copy-shifts either up or down in index units
     * @param up if true copy up, else down.
     * @return the truth segment
     */
    public static MemorySegment truthSegment1Byte(MemorySegment srcSeg, int indexShifts, boolean up) {
        MemorySegment dstSeg = MemorySegment.ofArray(srcSeg.toByteArray());
        int indexLength = (int) dstSeg.byteSize();
        if (up) {
            for (int i = indexLength - 1; i >= indexShifts; i--) {
                byte v = MemoryAccess.getByteAtOffset(dstSeg, i - indexShifts);
                MemoryAccess.setByteAtOffset(dstSeg, i, v);
            }
        } else { //down
            for (int i = indexShifts; i < indexLength; i++) {
                byte v = MemoryAccess.getByteAtOffset(dstSeg, i);
                MemoryAccess.setByteAtOffset(dstSeg, i - indexShifts, v);
            }
        }
        return dstSeg;
    }

    /**
     * Builds a truth segment of multibyte primitives with a byte-length of 2.
     * @param srcSeg The source segment prior to any copy-shifts
     * @param indexShifts the number of copy-shifts either up or down in index units
     * @param up if true, copy up, else down.
     * @param swap if true, swap the bytes
     * @return the truth segment
     */
    public static MemorySegment truthSegment2Bytes(MemorySegment srcSeg, int indexShifts, boolean up, boolean swap) {
        MemorySegment dstSeg = MemorySegment.ofArray(srcSeg.toByteArray());
        int indexLength = (int) dstSeg.byteSize() / 2;
        if (up) {
            if (swap) {
                for (int i = indexLength - 1; i >= indexShifts; i--) {
                    char v = MemoryAccess.getCharAtIndex(dstSeg, i - indexShifts, nonNativeByteOrder);
                    MemoryAccess.setCharAtIndex(dstSeg, i, v);
                }
            } else {
                for (int i = indexLength - 1; i >= indexShifts; i--) {
                    char v = MemoryAccess.getCharAtIndex(dstSeg, i - indexShifts);
                    MemoryAccess.setCharAtIndex(dstSeg, i, v);
                }
            }
        } else { //down
            if (swap) {
                for (int i = indexShifts; i < indexLength; i++) {
                    char v = MemoryAccess.getCharAtIndex(dstSeg, i, nonNativeByteOrder);
                    MemoryAccess.setCharAtIndex(dstSeg, i - indexShifts, v);
                }
            } else {
                for (int i = indexShifts; i < indexLength; i++) {
                    char v = MemoryAccess.getCharAtIndex(dstSeg, i);
                    MemoryAccess.setCharAtIndex(dstSeg, i - indexShifts, v);
                }
            }
        }
        return dstSeg;
    }

    /**
     * Builds a truth segment of multibyte primitives with a byte-length of 4.
     * @param srcSeg The source segment prior to any copy-shifts
     * @param indexShifts the number of copy-shifts either up or down in index units
     * @param up if true copy up, else down.
     * @param swap if true, swap the bytes
     * @return the truth segment
     */
    public static MemorySegment truthSegment4Bytes(MemorySegment srcSeg, int indexShifts, boolean up, boolean swap) {
        MemorySegment dstSeg = MemorySegment.ofArray(srcSeg.toByteArray());
        int indexLength = (int) dstSeg.byteSize() / 4;
        if (up) {
            if (swap) {
                for (int i = indexLength - 1; i >= indexShifts; i--) {
                    int v = MemoryAccess.getIntAtIndex(dstSeg, i - indexShifts, nonNativeByteOrder);
                    MemoryAccess.setIntAtIndex(dstSeg, i, v);
                }
            } else {
                for (int i = indexLength - 1; i >= indexShifts; i--) {
                    int v = MemoryAccess.getIntAtIndex(dstSeg, i - indexShifts);
                    MemoryAccess.setIntAtIndex(dstSeg, i, v);
                }
            }
        } else { //down
            if (swap) {
                for (int i = indexShifts; i < indexLength; i++) {
                    int v = MemoryAccess.getIntAtIndex(dstSeg, i, nonNativeByteOrder);
                    MemoryAccess.setIntAtIndex(dstSeg, i - indexShifts, v);
                }
            } else {
                for (int i = indexShifts; i < indexLength; i++) {
                    int v = MemoryAccess.getIntAtIndex(dstSeg, i);
                    MemoryAccess.setIntAtIndex(dstSeg, i - indexShifts, v);
                }
            }
        }
        return dstSeg;
    }

    /**
     * Builds a truth segment of multibyte primitives with a byte-length of 8.
     * @param srcSeg The source segment prior to any copy-shifts
     * @param indexShifts the number of copy-shifts either up or down in index units
     * @param up if true copy up, else down.
     * @param swap if true, swap the bytes
     * @return the truth segment
     */
    public static MemorySegment truthSegment8Bytes(MemorySegment srcSeg, int indexShifts, boolean up, boolean swap) {
        MemorySegment dstSeg = MemorySegment.ofArray(srcSeg.toByteArray());
        int indexLength = (int) dstSeg.byteSize() / 8;
        if (up) {
            if (swap) {
                for (int i = indexLength - 1; i >= indexShifts; i--) {
                    long v = MemoryAccess.getLongAtIndex(dstSeg, i - indexShifts, nonNativeByteOrder);
                    MemoryAccess.setLongAtIndex(dstSeg, i, v);
                }
            } else {
                for (int i = indexLength - 1; i >= indexShifts; i--) {
                    long v = MemoryAccess.getLongAtIndex(dstSeg, i - indexShifts);
                    MemoryAccess.setLongAtIndex(dstSeg, i, v);
                }
            }
        } else { //down
            if (swap) {
                for (int i = indexShifts; i < indexLength; i++) {
                    long v = MemoryAccess.getLongAtIndex(dstSeg, i, nonNativeByteOrder);
                    MemoryAccess.setLongAtIndex(dstSeg, i - indexShifts, v);
                }
            } else {
                for (int i = indexShifts; i < indexLength; i++) {
                    long v = MemoryAccess.getLongAtIndex(dstSeg, i);
                    MemoryAccess.setLongAtIndex(dstSeg, i - indexShifts, v);
                }
            }
        }
        return dstSeg;
    }
}
