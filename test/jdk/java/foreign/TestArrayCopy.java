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
 * @run testng TestArrayCopy
 */

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.ValueLayout;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * These tests exercise the bulk accessors in the MemoryAccess class.
 * To make these tests more challenging the segment is a view of the given array,
 * which makes the copy operations overlapping self-copies.  Thus, this checks the claim:
 *
 * <p>If the source (destination) segment is actually a view of the destination (source) array,
 * and if the copy region of the source overlaps with the copy region of the destination,
 * the copy of the overlapping region is performed as if the data in the overlapping region
 * were first copied into a temporary segment before being copied to the destination.</p>
 */
public class TestArrayCopy {
    private static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();
    private static final ByteOrder NON_NATIVE_ORDER = NATIVE_ORDER == ByteOrder.LITTLE_ENDIAN
            ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

    private static final int SEG_LENGTH_BYTES = 32;
    private static final int SEG_OFFSET_BYTES = 8;

    @Test(dataProvider = "copyModesAndHelpers")
    public void testSelfCopy(CopyMode mode, CopyHelper<Object> helper, String helperDebugString) {
        int bytesPerElement = (int)helper.elementLayout.byteSize();
        int indexShifts = SEG_OFFSET_BYTES / bytesPerElement;
        MemorySegment base = srcSegment(SEG_LENGTH_BYTES);
        MemorySegment truth = truthSegment(base, helper, indexShifts, mode);
        ByteOrder bo = mode.swap ? NON_NATIVE_ORDER : NATIVE_ORDER;
        //CopyFrom
        Object srcArr = helper.toArray(base);
        int srcIndex = mode.direction ? 0 : indexShifts;
        int srcCopyLen = helper.length(srcArr) - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = mode.direction ? SEG_OFFSET_BYTES : 0;
        MemoryAccess.copy(srcArr, srcIndex, dstSeg, dstOffsetBytes, srcCopyLen, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : SEG_OFFSET_BYTES;
        Object dstArr = helper.toArray(base);
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr).asReadOnly();
        int dstIndex = mode.direction ? indexShifts : 0;
        int dstCopyLen = helper.length(dstArr) - indexShifts;
        MemoryAccess.copy(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = MemorySegment.ofArray(dstArr);
        assertEquals(truth.mismatch(result), -1);
    }

    @Test(dataProvider = "copyModesAndHelpers")
    public void testUnalignedCopy(CopyMode mode, CopyHelper<Object> helper, String helperDebugString) {
        int bytesPerElement = (int)helper.elementLayout.byteSize();
        int indexShifts = SEG_OFFSET_BYTES / bytesPerElement;
        MemorySegment base = srcSegment(SEG_LENGTH_BYTES);
        ByteOrder bo = mode.swap ? NON_NATIVE_ORDER : NATIVE_ORDER;
        //CopyFrom
        Object srcArr = helper.toArray(base);
        int srcIndex = mode.direction ? 0 : indexShifts;
        int srcCopyLen = helper.length(srcArr) - indexShifts;
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        long dstOffsetBytes = mode.direction ? (SEG_OFFSET_BYTES - 1) : 0;
        MemoryAccess.copy(srcArr, srcIndex, dstSeg, dstOffsetBytes, srcCopyLen, bo);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : (SEG_OFFSET_BYTES - 1);
        Object dstArr = helper.toArray(base);
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr).asReadOnly();
        int dstIndex = mode.direction ? indexShifts : 0;
        int dstCopyLen = helper.length(dstArr) - indexShifts;
        MemoryAccess.copy(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
    }

    @Test(dataProvider = "copyModesAndHelpers")
    public void testCopyOobLength(CopyMode mode, CopyHelper<Object> helper, String helperDebugString) {
        int bytesPerElement = (int)helper.elementLayout.byteSize();
        MemorySegment base = srcSegment(SEG_LENGTH_BYTES);
        //CopyFrom
        Object srcArr = helper.toArray(base);
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        try {
            MemoryAccess.copy(srcArr, 0, dstSeg, 0, (SEG_LENGTH_BYTES / bytesPerElement) * 2, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
        //CopyTo
        Object dstArr = helper.toArray(base);
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr).asReadOnly();
        try {
            MemoryAccess.copy(srcSeg, 0, dstArr, 0, (SEG_LENGTH_BYTES / bytesPerElement) * 2, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
    }

    @Test(dataProvider = "copyModesAndHelpers")
    public void testCopyNegativeIndices(CopyMode mode, CopyHelper<Object> helper, String helperDebugString) {
        int bytesPerElement = (int)helper.elementLayout.byteSize();
        MemorySegment base = srcSegment(SEG_LENGTH_BYTES);
        //CopyFrom
        Object srcArr = helper.toArray(base);
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        try {
            MemoryAccess.copy(srcArr, -1, dstSeg, 0, SEG_LENGTH_BYTES / bytesPerElement, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
        //CopyTo
        Object dstArr = helper.toArray(base);
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr).asReadOnly();
        try {
            MemoryAccess.copy(srcSeg, 0, dstArr, -1, SEG_LENGTH_BYTES / bytesPerElement, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
    }

    @Test(dataProvider = "copyModesAndHelpers")
    public void testCopyNegativeOffsets(CopyMode mode, CopyHelper<Object> helper, String helperDebugString) {
        int bytesPerElement = (int)helper.elementLayout.byteSize();
        MemorySegment base = srcSegment(SEG_LENGTH_BYTES);
        //CopyFrom
        Object srcArr = helper.toArray(base);
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        try {
            MemoryAccess.copy(srcArr, 0, dstSeg, -1, SEG_LENGTH_BYTES / bytesPerElement, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
        //CopyTo
        Object dstArr = helper.toArray(base);
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr).asReadOnly();
        try {
            MemoryAccess.copy(srcSeg, -1, dstArr, 0, SEG_LENGTH_BYTES / bytesPerElement, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
    }

    @Test(dataProvider = "copyModesAndHelpers")
    public void testCopyNegativeLengths(CopyMode mode, CopyHelper<Object> helper, String helperDebugString) {
        int bytesPerElement = (int)helper.elementLayout.byteSize();
        MemorySegment base = srcSegment(SEG_LENGTH_BYTES);
        //CopyFrom
        Object srcArr = helper.toArray(base);
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        try {
            MemoryAccess.copy(srcArr, 0, dstSeg, -1, -SEG_LENGTH_BYTES / bytesPerElement, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
        //CopyTo
        Object dstArr = helper.toArray(base);
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr).asReadOnly();
        try {
            MemoryAccess.copy(srcSeg, -1, dstArr, 0, -SEG_LENGTH_BYTES / bytesPerElement, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
    }

    @Test(dataProvider = "copyModesAndHelpers")
    public void testCopyOobIndices(CopyMode mode, CopyHelper<Object> helper, String helperDebugString) {
        int bytesPerElement = (int)helper.elementLayout.byteSize();
        MemorySegment base = srcSegment(SEG_LENGTH_BYTES);
        //CopyFrom
        Object srcArr = helper.toArray(base);
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        try {
            MemoryAccess.copy(srcArr, helper.length(srcArr) + 1, dstSeg, 0, SEG_LENGTH_BYTES / bytesPerElement, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
        //CopyTo
        Object dstArr = helper.toArray(base);
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr).asReadOnly();
        try {
            MemoryAccess.copy(srcSeg, 0, dstArr, helper.length(dstArr) + 1, SEG_LENGTH_BYTES / bytesPerElement, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
    }

    @Test(dataProvider = "copyModesAndHelpers")
    public void testCopyOobOffsets(CopyMode mode, CopyHelper<Object> helper, String helperDebugString) {
        int bytesPerElement = (int)helper.elementLayout.byteSize();
        MemorySegment base = srcSegment(SEG_LENGTH_BYTES);
        //CopyFrom
        Object srcArr = helper.toArray(base);
        MemorySegment dstSeg = MemorySegment.ofArray(srcArr);
        try {
            MemoryAccess.copy(srcArr, 0, dstSeg, SEG_LENGTH_BYTES + 1, SEG_LENGTH_BYTES / bytesPerElement, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
        //CopyTo
        Object dstArr = helper.toArray(base);
        MemorySegment srcSeg = MemorySegment.ofArray(dstArr).asReadOnly();
        try {
            MemoryAccess.copy(srcSeg, SEG_OFFSET_BYTES + 1, dstArr, 0, SEG_LENGTH_BYTES / bytesPerElement, ByteOrder.nativeOrder());
            fail();
        } catch (IndexOutOfBoundsException ex) {
            //ok
        }
    }

    /***** Utilities *****/

    public static MemorySegment srcSegment(int bytesLength) {
        byte[] arr = new byte[bytesLength];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte)i;
        }
        return MemorySegment.ofArray(arr);
    }

    public static MemorySegment truthSegment(MemorySegment srcSeg, CopyHelper<?> helper, int indexShifts, CopyMode mode) {
        VarHandle indexedHandleNO = MemoryLayout.sequenceLayout(helper.elementLayout.withOrder(NATIVE_ORDER))
                                                .varHandle(helper.carrier.componentType(), MemoryLayout.PathElement.sequenceElement());
        VarHandle indexedHandleNNO = MemoryLayout.sequenceLayout(helper.elementLayout.withOrder(NON_NATIVE_ORDER))
                                                 .varHandle(helper.carrier.componentType(), MemoryLayout.PathElement.sequenceElement());
        byte[] bytes = new byte[(int)srcSeg.byteSize()];
        MemoryAccess.copy(srcSeg, 0, bytes, 0, bytes.length);
        MemorySegment dstSeg = MemorySegment.ofArray(bytes);
        int indexLength = (int) dstSeg.byteSize() / (int)helper.elementLayout.byteSize();
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

    static class CopyHelper<X> {

        final ValueLayout elementLayout;
        final Class<?> carrier;
        final IntFunction<X> arrayFactory;

        public CopyHelper(ValueLayout elementLayout, Class<X> carrier, IntFunction<X> arrayFactory) {
            this.elementLayout = elementLayout;
            this.carrier = carrier;
            this.arrayFactory = arrayFactory;
        }

        final int length(X arr) {
            return Array.getLength(arr);
        }

        final X toArray(MemorySegment segment) {
            int size = (int)(segment.byteSize() / elementLayout.byteSize());
            X arr = arrayFactory.apply(size);
            MemoryAccess.copy(segment, 0, arr, 0, size, ByteOrder.nativeOrder());
            return arr;
        }

        @Override
        public String toString() {
            return "CopyHelper{" +
                    "elementLayout=" + elementLayout +
                    ", carrier=" + carrier.getName() +
                    '}';
        }

        static final CopyHelper<byte[]> BYTE = new CopyHelper<>(MemoryLayouts.JAVA_BYTE, byte[].class, byte[]::new);
        static final CopyHelper<char[]> CHAR = new CopyHelper<>(MemoryLayouts.JAVA_CHAR, char[].class, char[]::new);
        static final CopyHelper<short[]> SHORT = new CopyHelper<>(MemoryLayouts.JAVA_SHORT, short[].class, short[]::new);
        static final CopyHelper<int[]> INT = new CopyHelper<>(MemoryLayouts.JAVA_INT, int[].class, int[]::new);
        static final CopyHelper<float[]> FLOAT = new CopyHelper<>(MemoryLayouts.JAVA_FLOAT, float[].class, float[]::new);
        static final CopyHelper<long[]> LONG = new CopyHelper<>(MemoryLayouts.JAVA_LONG, long[].class, long[]::new);
        static final CopyHelper<double[]> DOUBLE = new CopyHelper<>(MemoryLayouts.JAVA_DOUBLE, double[].class, double[]::new);
    }

    @DataProvider
    Object[][] copyModesAndHelpers() {
        CopyHelper<?>[] helpers = { CopyHelper.BYTE, CopyHelper.CHAR, CopyHelper.SHORT, CopyHelper.INT,
                                    CopyHelper.FLOAT, CopyHelper.LONG, CopyHelper.DOUBLE };
        List<Object[]> results = new ArrayList<>();
        for (CopyHelper<?> helper : helpers) {
            for (CopyMode mode : CopyMode.values()) {
                results.add(new Object[] { mode, helper, helper.toString() });
            }
        }
        return results.stream().toArray(Object[][]::new);
    }
}
