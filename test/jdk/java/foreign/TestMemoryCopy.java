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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        MemorySegment dstSeg = helper.fromArray(srcArr);
        long dstOffsetBytes = mode.direction ? SEG_OFFSET_BYTES : 0;
        helper.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        assertEquals(truth.mismatch(dstSeg), -1);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : SEG_OFFSET_BYTES;
        Object dstArr = helper.toArray(base);
        MemorySegment srcSeg = helper.fromArray(dstArr);
        int dstIndex = mode.direction ? indexShifts : 0;
        int dstCopyLen = helper.length(dstArr) - indexShifts;
        helper.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
        MemorySegment result = helper.fromArray(dstArr);
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
        MemorySegment dstSeg = helper.fromArray(srcArr);
        long dstOffsetBytes = mode.direction ? (SEG_OFFSET_BYTES - 1) : 0;
        helper.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
        //CopyTo
        long srcOffsetBytes = mode.direction ? 0 : (SEG_OFFSET_BYTES - 1);
        Object dstArr = helper.toArray(base);
        MemorySegment srcSeg = helper.fromArray(dstArr);
        int dstIndex = mode.direction ? indexShifts : 0;
        int dstCopyLen = helper.length(dstArr) - indexShifts;
        helper.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
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
        MemorySegment dstSeg = MemorySegment.ofArray(srcSeg.toByteArray());
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

    abstract static class CopyHelper<X> {

        final ValueLayout elementLayout;
        final Class<?> carrier;

        public CopyHelper(ValueLayout elementLayout, Class<X> carrier) {
            this.elementLayout = elementLayout;
            this.carrier = carrier;
        }

        abstract void copyFromArray(X srcArr, int srcIndex, int srcCopyLen, MemorySegment dstSeg, long dstOffsetBytes, ByteOrder bo);
        abstract void copyToArray(MemorySegment srcSeg, long srcOffsetBytes, X dstArr, int dstIndex, int dstCopyLen, ByteOrder bo);
        abstract X toArray(MemorySegment segment);
        abstract MemorySegment fromArray(X array);
        abstract int length(X arr);

        @Override
        public String toString() {
            return "CopyHelper{" +
                    "elementLayout=" + elementLayout +
                    ", carrier=" + carrier.getName() +
                    '}';
        }

        static final CopyHelper<byte[]> BYTE = new CopyHelper<>(MemoryLayouts.JAVA_BYTE, byte[].class) {
            @Override
            void copyFromArray(byte[] srcArr, int srcIndex, int srcCopyLen, MemorySegment dstSeg, long dstOffsetBytes, ByteOrder bo) {
                MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes);
            }

            @Override
            void copyToArray(MemorySegment srcSeg, long srcOffsetBytes, byte[] dstArr, int dstIndex, int dstCopyLen, ByteOrder bo) {
                MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen);
            }

            @Override
            byte[] toArray(MemorySegment segment) {
                return segment.toByteArray();
            }

            @Override
            MemorySegment fromArray(byte[] array) {
                return MemorySegment.ofArray(array);
            }

            @Override
            int length(byte[] arr) {
                return arr.length;
            }
        };

        static final CopyHelper<char[]> CHAR = new CopyHelper<>(MemoryLayouts.JAVA_CHAR, char[].class) {
            @Override
            void copyFromArray(char[] srcArr, int srcIndex, int srcCopyLen, MemorySegment dstSeg, long dstOffsetBytes, ByteOrder bo) {
                MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
            }

            @Override
            void copyToArray(MemorySegment srcSeg, long srcOffsetBytes, char[] dstArr, int dstIndex, int dstCopyLen, ByteOrder bo) {
                MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
            }

            @Override
            char[] toArray(MemorySegment segment) {
                return segment.toCharArray();
            }

            @Override
            MemorySegment fromArray(char[] array) {
                return MemorySegment.ofArray(array);
            }

            @Override
            int length(char[] arr) {
                return arr.length;
            }
        };

        static final CopyHelper<short[]> SHORT = new CopyHelper<>(MemoryLayouts.JAVA_SHORT, short[].class) {
            @Override
            void copyFromArray(short[] srcArr, int srcIndex, int srcCopyLen, MemorySegment dstSeg, long dstOffsetBytes, ByteOrder bo) {
                MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
            }

            @Override
            void copyToArray(MemorySegment srcSeg, long srcOffsetBytes, short[] dstArr, int dstIndex, int dstCopyLen, ByteOrder bo) {
                MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
            }

            @Override
            short[] toArray(MemorySegment segment) {
                return segment.toShortArray();
            }

            @Override
            MemorySegment fromArray(short[] array) {
                return MemorySegment.ofArray(array);
            }

            @Override
            int length(short[] arr) {
                return arr.length;
            }
        };

        static final CopyHelper<int[]> INT = new CopyHelper<>(MemoryLayouts.JAVA_INT, int[].class) {
            @Override
            void copyFromArray(int[] srcArr, int srcIndex, int srcCopyLen, MemorySegment dstSeg, long dstOffsetBytes, ByteOrder bo) {
                MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
            }

            @Override
            void copyToArray(MemorySegment srcSeg, long srcOffsetBytes, int[] dstArr, int dstIndex, int dstCopyLen, ByteOrder bo) {
                MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
            }

            @Override
            int[] toArray(MemorySegment segment) {
                return segment.toIntArray();
            }

            @Override
            MemorySegment fromArray(int[] array) {
                return MemorySegment.ofArray(array);
            }

            @Override
            int length(int[] arr) {
                return arr.length;
            }
        };

        static final CopyHelper<float[]> FLOAT = new CopyHelper<>(MemoryLayouts.JAVA_FLOAT, float[].class) {
            @Override
            void copyFromArray(float[] srcArr, int srcIndex, int srcCopyLen, MemorySegment dstSeg, long dstOffsetBytes, ByteOrder bo) {
                MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
            }

            @Override
            void copyToArray(MemorySegment srcSeg, long srcOffsetBytes, float[] dstArr, int dstIndex, int dstCopyLen, ByteOrder bo) {
                MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
            }

            @Override
            float[] toArray(MemorySegment segment) {
                return segment.toFloatArray();
            }

            @Override
            MemorySegment fromArray(float[] array) {
                return MemorySegment.ofArray(array);
            }

            @Override
            int length(float[] arr) {
                return arr.length;
            }
        };

        static final CopyHelper<long[]> LONG = new CopyHelper<>(MemoryLayouts.JAVA_LONG, long[].class) {
            @Override
            void copyFromArray(long[] srcArr, int srcIndex, int srcCopyLen, MemorySegment dstSeg, long dstOffsetBytes, ByteOrder bo) {
                MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
            }

            @Override
            void copyToArray(MemorySegment srcSeg, long srcOffsetBytes, long[] dstArr, int dstIndex, int dstCopyLen, ByteOrder bo) {
                MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
            }

            @Override
            long[] toArray(MemorySegment segment) {
                return segment.toLongArray();
            }

            @Override
            MemorySegment fromArray(long[] array) {
                return MemorySegment.ofArray(array);
            }

            @Override
            int length(long[] arr) {
                return arr.length;
            }
        };

        static final CopyHelper<double[]> DOUBLE = new CopyHelper<>(MemoryLayouts.JAVA_DOUBLE, double[].class) {
            @Override
            void copyFromArray(double[] srcArr, int srcIndex, int srcCopyLen, MemorySegment dstSeg, long dstOffsetBytes, ByteOrder bo) {
                MemoryCopy.copyFromArray(srcArr, srcIndex, srcCopyLen, dstSeg, dstOffsetBytes, bo);
            }

            @Override
            void copyToArray(MemorySegment srcSeg, long srcOffsetBytes, double[] dstArr, int dstIndex, int dstCopyLen, ByteOrder bo) {
                MemoryCopy.copyToArray(srcSeg, srcOffsetBytes, dstArr, dstIndex, dstCopyLen, bo);
            }

            @Override
            double[] toArray(MemorySegment segment) {
                return segment.toDoubleArray();
            }

            @Override
            MemorySegment fromArray(double[] array) {
                return MemorySegment.ofArray(array);
            }

            @Override
            int length(double[] arr) {
                return arr.length;
            }
        };
    }

    @DataProvider
    Object[][] copyModes() {
        return Arrays.stream(CopyMode.values())
                .map(mode -> new Object[] { mode })
                .toArray(Object[][]::new);
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
