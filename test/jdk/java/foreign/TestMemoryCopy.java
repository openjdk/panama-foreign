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

/*
 * @test
 * @run testng TestMemoryCopy
 */

import static org.testng.Assert.assertEquals;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Arrays;

import jdk.incubator.foreign.MemoryCopy;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;

import jdk.incubator.foreign.ValueLayout;
import org.testng.annotations.DataProvider;
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

    enum CopyMode {
        UP_NO_SWAP(true, false),
        UP_SWAP(true, true),
        DOWN_NO_SWAP(false, false),
        DOWN_SWAP(false, true);

        final boolean direction;
        final boolean swap;

        CopyMode(boolean direction, boolean swap) {
            this.direction = direction;
            this.swap = swap;
        }
    }

    //BYTE ARRAY
    @Test(dataProvider = "copyModes")
    public void checkByteArr(CopyMode mode) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 1; // f(array type)
        int arrIndex = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcSegment(segLengthBytes);
        MemorySegment truth = truthSegment(base, byte.class, MemoryLayouts.JAVA_BYTE, 8, mode);
        //CopyFrom
        byte[] srcArr = base.toByteArray(); // f(array type)
        int srcIndex = mode.direction ? 0 : arrIndex;
        int srcCopyLen = srcArr.length - arrIndex;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = mode.direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : segOffsetBytes;
        byte[] dstArr = base.toByteArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = mode.direction ? arrIndex : 0;
        int dstCopyLen = dstArr.length - arrIndex;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    @Test(dataProvider = "copyModes")
    public void checkCharArr(CopyMode mode) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 2;  // f(array type)
        int indexShifts = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcSegment(segLengthBytes);
        MemorySegment truth = truthSegment(base, char.class, MemoryLayouts.JAVA_CHAR, indexShifts, mode);
        ByteOrder bo = mode.swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        char[] srcArr = base.toCharArray(); // f(array type)
        int srcIndex = mode.direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = mode.direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : segOffsetBytes;
        char[] dstArr = base.toCharArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = mode.direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //DOUBLE ARRAY
    @Test(dataProvider = "copyModes")
    public void checkDoubleArr(CopyMode mode) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 8; // f(array type)
        int indexShifts = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcSegment(segLengthBytes); // f(array type)
        MemorySegment truth = truthSegment(base, double.class, MemoryLayouts.JAVA_DOUBLE, indexShifts, mode); // f(array type)
        ByteOrder bo = mode.swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        double[] srcArr = base.toDoubleArray(); // f(array type)
        int srcIndex = mode.direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = mode.direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : segOffsetBytes;
        double[] dstArr = base.toDoubleArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = mode.direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //FlOAT ARRAY
    @Test(dataProvider = "copyModes")
    public void checkFloatArr(CopyMode mode) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 4;
        int indexShifts = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcSegment(segLengthBytes); // f(array type)
        MemorySegment truth = truthSegment(base, float.class, MemoryLayouts.JAVA_FLOAT, indexShifts, mode); // f(array type)
        ByteOrder bo = mode.swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        float[] srcArr = base.toFloatArray(); // f(array type)
        int srcIndex = mode.direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = mode.direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : segOffsetBytes;
        float[] dstArr = base.toFloatArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = mode.direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //INT ARRAY
    @Test(dataProvider = "copyModes")
    public void checkIntArr(CopyMode mode) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 4;  // f(array type)
        int indexShifts = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcSegment(segLengthBytes);
        MemorySegment truth = truthSegment(base, int.class, MemoryLayouts.JAVA_INT, indexShifts, mode);
        ByteOrder bo = mode.swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        int[] srcArr = base.toIntArray(); // f(array type)
        int srcIndex = mode.direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = mode.direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : segOffsetBytes;
        int[] dstArr = base.toIntArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = mode.direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //LONG ARRAY
    @Test(dataProvider = "copyModes")
    public void checkLongArr(CopyMode mode) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 8;  // f(array type)
        int indexShifts = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcSegment(segLengthBytes);
        MemorySegment truth = truthSegment(base, long.class, MemoryLayouts.JAVA_LONG, indexShifts, mode);
        ByteOrder bo = mode.swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        long[] srcArr = base.toLongArray(); // f(array type)
        int srcIndex = mode.direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = mode.direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : segOffsetBytes;
        long[] dstArr = base.toLongArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = mode.direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    //SHORT ARRAY
    @Test(dataProvider = "copyModes")
    public void checkShortArr(CopyMode mode) {
        int segLengthBytes = 32;
        int segOffsetBytes = 8;
        int bytesPerElement = 2;  // f(array type)
        int indexShifts = segOffsetBytes / bytesPerElement;
        MemorySegment base = srcSegment(segLengthBytes);
        MemorySegment truth = truthSegment(base, short.class, MemoryLayouts.JAVA_SHORT, indexShifts, mode);
        ByteOrder bo = mode.swap ? nonNativeByteOrder : nativeByteOrder;
        //CopyFrom
        short[] srcArr = base.toShortArray(); // f(array type)
        int srcIndex = mode.direction ? 0 : indexShifts;
        int srcCopyLen = srcArr.length - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = mode.direction ? segOffsetBytes : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : segOffsetBytes;
        short[] dstArr = base.toShortArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = mode.direction ? indexShifts : 0;
        int dstCopyLen = dstArr.length - indexShifts;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    /***** Utilities *****/

    public static MemorySegment srcSegment(int bytesLength) {
        byte[] arr = new byte[bytesLength];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte)i;
        }
        return MemorySegment.ofArray(arr);
    }

    public static MemorySegment truthSegment(MemorySegment srcSeg, Class<?> carrier, ValueLayout elementLayout, int indexShifts, CopyMode mode) {
        VarHandle indexedHandleNO = MemoryLayout.sequenceLayout(elementLayout.withOrder(nativeByteOrder))
                                                .varHandle(carrier, MemoryLayout.PathElement.sequenceElement());
        VarHandle indexedHandleNNO = MemoryLayout.sequenceLayout(elementLayout.withOrder(nonNativeByteOrder))
                                                 .varHandle(carrier, MemoryLayout.PathElement.sequenceElement());
        MemorySegment dstSeg = MemorySegment.ofArray(srcSeg.toByteArray());
        int indexLength = (int) dstSeg.byteSize() / (int)elementLayout.byteSize();
        if (mode.direction) {
            if (mode.swap) {
                for (int i = indexLength - 1; i >= indexShifts; i--) {
                    Object v = indexedHandleNNO.get(dstSeg, i - indexShifts);
                    indexedHandleNO.set(dstSeg, i, v);
                }
            } else {
                for (int i = indexLength - 1; i >= indexShifts; i--) {
                    Object v = indexedHandleNO.get(dstSeg, i - indexShifts);
                    indexedHandleNO.set(dstSeg, i, v);
                }
            }
        } else { //down
            if (mode.swap) {
                for (int i = indexShifts; i < indexLength; i++) {
                    Object v = indexedHandleNNO.get(dstSeg, i);
                    indexedHandleNO.set(dstSeg, i - indexShifts, v);
                }
            } else {
                for (int i = indexShifts; i < indexLength; i++) {
                    Object v = indexedHandleNO.get(dstSeg, i);
                    indexedHandleNO.set(dstSeg, i - indexShifts, v);
                }
            }
        }
        return dstSeg;
    }

    @DataProvider
    Object[][] copyModes() {
        return Arrays.stream(CopyMode.values())
                .map(mode -> new Object[] { mode })
                .toArray(Object[][]::new);
    }
}
