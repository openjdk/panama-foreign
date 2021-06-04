/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @run testng TestMemoryCopy
 */

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryCopy;
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
public class TestMemoryCopy {
    //BYTE ARRAY
    @Test
    public void checkByteArrSelfCopy() {
        checkByteArr(true);
        checkByteArr(false);
    }

    private void checkByteArr(boolean up) {
        int segLength = 32;
        int segOffset = 8;
        int bytesPerElement = 1; // f(array type)
        int arrOffset = segOffset / bytesPerElement;
        MemorySegment base = buildSegment(0, segLength, 0, up);
        MemorySegment truth = buildSegment(0, segLength, segOffset, up);
        //CopyFrom
        byte[] srcArr = base.toByteArray(); // f(array type)
        int srcIndex = up ? 0 : arrOffset;
        int srcCopyLen = srcArr.length - arrOffset;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = up ? segOffset : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = up ? 0 : segOffset;
        byte[] dstArr = base.toByteArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = up ? arrOffset : 0;
        int dstCopyLen = dstArr.length - arrOffset;
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
        int segLength = 32;
        int segOffset = 8;
        int bytesPerElement = 4;  // f(array type)
        int arrOffset = segOffset / bytesPerElement;
        MemorySegment base = buildSegment(0, segLength, 0, up);
        MemorySegment truth = buildSegment(0, segLength, segOffset, up);
        //CopyFrom
        int[] srcArr = base.toIntArray(); // f(array type)
        int srcIndex = up ? 0 : arrOffset;
        int srcCopyLen = srcArr.length - arrOffset;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = up ? segOffset : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = up ? 0 : segOffset;
        int[] dstArr = base.toIntArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = up ? arrOffset : 0;
        int dstCopyLen = dstArr.length - arrOffset;
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
        int segLength = 32;
        int segOffset = 8;
        int bytesPerElement = 2;  // f(array type)
        int arrOffset = segOffset / bytesPerElement;
        MemorySegment base = buildSegment(0, segLength, 0, up);
        MemorySegment truth = buildSegment(0, segLength, segOffset, up);
        //CopyFrom
        char[] srcArr = base.toCharArray(); // f(array type)
        int srcIndex = up ? 0 : arrOffset;
        int srcCopyLen = srcArr.length - arrOffset;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = up ? segOffset : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = up ? 0 : segOffset;
        char[] dstArr = base.toCharArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = up ? arrOffset : 0;
        int dstCopyLen = dstArr.length - arrOffset;
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
        int segLength = 32;
        int segOffset = 8;
        int bytesPerElement = 2;  // f(array type)
        int arrOffset = segOffset / bytesPerElement;
        MemorySegment base = buildSegment(0, segLength, 0, up);
        MemorySegment truth = buildSegment(0, segLength, segOffset, up);
        //CopyFrom
        short[] srcArr = base.toShortArray(); // f(array type)
        int srcIndex = up ? 0 : arrOffset;
        int srcCopyLen = srcArr.length - arrOffset;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = up ? segOffset : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = up ? 0 : segOffset;
        short[] dstArr = base.toShortArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = up ? arrOffset : 0;
        int dstCopyLen = dstArr.length - arrOffset;
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
        int segLength = 32;
        int segOffset = 8;
        int bytesPerElement = 8;  // f(array type)
        int arrOffset = segOffset / bytesPerElement;
        MemorySegment base = buildSegment(0, segLength, 0, up);
        MemorySegment truth = buildSegment(0, segLength, segOffset, up);
        //CopyFrom
        long[] srcArr = base.toLongArray(); // f(array type)
        int srcIndex = up ? 0 : arrOffset;
        int srcCopyLen = srcArr.length - arrOffset;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = up ? segOffset : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = up ? 0 : segOffset;
        long[] dstArr = base.toLongArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = up ? arrOffset : 0;
        int dstCopyLen = dstArr.length - arrOffset;
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
        int segLength = 32;
        int segOffset = 8;
        int bytesPerElement = 4;
        int arrOffset = segOffset / bytesPerElement;
        int arrLength = segLength / segOffset; //this is done differently
        MemorySegment base = buildFloatSegment((float)Math.PI, arrLength, 0, up);
        MemorySegment truth = buildFloatSegment((float)Math.PI, arrLength, arrOffset, up);
        //CopyFrom
        float[] srcArr = base.toFloatArray(); // f(array type)
        int srcIndex = up ? 0 : arrOffset;
        int srcCopyLen = srcArr.length - arrOffset;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = up ? segOffset : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = up ? 0 : segOffset;
        float[] dstArr = base.toFloatArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = up ? arrOffset : 0;
        int dstCopyLen = dstArr.length - arrOffset;
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
        int segLength = 32;
        int segOffset = 8;
        int bytesPerElement = 8; // f(array type)
        int arrOffset = segOffset / bytesPerElement;
        int arrLength = segLength / segOffset; //this is done differently
        MemorySegment base = buildDoubleSegment(Math.PI, arrLength, 0, up); // f(array type)
        MemorySegment truth = buildDoubleSegment(Math.PI, arrLength, arrOffset, up); // f(array type)
        //CopyFrom
        double[] srcArr = base.toDoubleArray(); // f(array type)
        int srcIndex = up ? 0 : arrOffset;
        int srcCopyLen = srcArr.length - arrOffset;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = up ? segOffset : 0;
        MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = up ? 0 : segOffset;
        double[] dstArr = base.toDoubleArray(); // f(array type)
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr);
        int dstIndex = up ? arrOffset : 0;
        int dstCopyLen = dstArr.length - arrOffset;
        MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    /**
     * Builds base segment for integral tests, a sequence of positive byte values.
     * @param startValue a value
     * @param segLength less than 128
     * @param segOffset less than segLength
     * @param up if true shift up, else down
     * @return a MemorySegment
     */
    @SuppressWarnings({"SameParameterValue", "ManualArrayCopy"})
    private MemorySegment buildSegment(int startValue, int segLength, int segOffset, boolean up) {
        byte[] arr = new byte[segLength];
        for (int i = 0; i < segLength; i++) { arr[i] = (byte)(i + startValue); }
        if (segOffset == 0) { return MemorySegment.ofArray(arr); }
        if (up) {
            for (int i = segLength - 1; i >= segOffset; i--) { arr[i] = arr[i - segOffset]; }
        } else { //down
            for (int i = segOffset; i < segLength; i++) { arr[i - segOffset] = arr[i]; }
        }
        return MemorySegment.ofArray(arr);
    }

    /**
     * Builds base segment for float tests, a sequence of values.
     * @param startValue a value
     * @param arrLength less than 128
     * @param arrOffset less than arrLength
     * @param up if true shift up, else down
     * @return a MemorySegment
     */
    @SuppressWarnings({"SameParameterValue", "ManualArrayCopy"})
    private MemorySegment buildFloatSegment(float startValue, int arrLength, int arrOffset, boolean up) {
        float[] arr = new float[arrLength];
        for (int i = 0; i < arrLength; i++) { arr[i] = i + startValue; }
        if (up) {
            for (int i = arrLength - 1; i >= arrOffset; i--) { arr[i] = arr[i - arrOffset]; }
        } else { //down
            for (int i = arrOffset; i < arrLength; i++) { arr[i - arrOffset] = arr[i]; }
        }
        return MemorySegment.ofArray(arr);
    }

    /**
     * Builds base segment for double tests, a sequence of values.
     * @param startValue a value
     * @param arrLength less than 128
     * @param arrOffset less than arrLength
     * @param up if true shift up, else down
     * @return a MemorySegment
     */
    @SuppressWarnings({"SameParameterValue", "ManualArrayCopy"})
    private MemorySegment buildDoubleSegment(double startValue, int arrLength, int arrOffset, boolean up) {
        double[] arr = new double[arrLength];
        for (int i = 0; i < arrLength; i++) { arr[i] = i + startValue; }
        if (up) {
            for (int i = arrLength - 1; i >= arrOffset; i--) { arr[i] = arr[i - arrOffset]; }
        } else { //down
            for (int i = arrOffset; i < arrLength; i++) { arr[i - arrOffset] = arr[i]; }
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
