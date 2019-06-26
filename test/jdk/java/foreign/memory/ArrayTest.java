/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.foreign.Scope;
import java.foreign.annotations.NativeAddressof;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.layout.Sequence;
import java.foreign.memory.Array;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.util.function.IntUnaryOperator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.foreign.NativeTypes.DOUBLE;
import static java.foreign.NativeTypes.FLOAT;
import static java.foreign.NativeTypes.INT16;
import static java.foreign.NativeTypes.INT32;
import static java.foreign.NativeTypes.INT64;
import static java.foreign.NativeTypes.INT8;
import static java.foreign.NativeTypes.UINT16;
import static java.foreign.NativeTypes.UINT32;
import static java.foreign.NativeTypes.UINT8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

/**
 * @test
 * @run testng ArrayTest
 * @summary Tests for java.foreign.memory.Array
 */
public class ArrayTest {
    @NativeStruct("[" +
            "i32(x)" +
            "i32(y)" +
            "](Point)")
    public static interface Point extends Struct<Point> {
        @NativeGetter("x")
        public int x$get();
        @NativeSetter("x")
        public void x$set(int arg);
        @NativeAddressof("x")
        public Pointer<Integer> x$ptr();
        @NativeGetter("y")
        public int y$get();
        @NativeSetter("y")
        public void y$set(int arg);
        @NativeAddressof("y")
        public Pointer<Integer> y$ptr();
    }


    final private static int[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    private Array<Integer> arInt = Array.ofPrimitiveArray(INT32, data);

    private IntUnaryOperator at(int[] ar) {
        return i -> ar[i];
    }

    private void verifyIntArray(IntUnaryOperator fa, IntUnaryOperator fb, int aPos, int bPos, int size) {
        for (int i = 0; i < size; i++) {
            assertEquals(fa.applyAsInt(aPos + i), fb.applyAsInt(bPos + i));
        }
    }

    @Test(dataProvider = "PrimitiveArrays")
    public <Z> void testJavaArray(LayoutType<Z> layout, Object javaArray) {
        Array<Z> ar = Array.ofPrimitiveArray(layout, javaArray);

        // Check length
        int length = java.lang.reflect.Array.getLength(javaArray);
        assertEquals(ar.length(), length);

        // Verify layout of ptr() is as expected
        Sequence s = (Sequence) ar.ptr().type().layout();
        assertEquals(s.elementsSize(), length);

        // Verify contents
        for (int i = 0; i < length; i++) {
            assertEquals(ar.get(i), java.lang.reflect.Array.get(javaArray, i));
        }
    }

    @DataProvider(name = "PrimitiveArrays")
    Object[][] primitiveArrays() {
        return new Object[][]{
                { INT8, new byte[] { (byte) 0xC0, 0x01, (byte) 0xBA, (byte) 0xBE } },
                { LayoutType.ofBoolean(UINT8.layout()), new boolean[] { true, false, false, true } },
                { LayoutType.ofChar(UINT16.layout()), new char[] { 'a', 'b' } },
                { INT16, new short[] { 'c', 'd' } },
                { INT32, new int[] { 1024, 0xDEADBEEF, 0x00FFFFFF } },
                { INT64, new long[] { Long.MIN_VALUE, Long.MAX_VALUE, -1L } },
                { FLOAT, new float[] { 3.1416f, 621.1649f } },
                { DOUBLE, new double[] { Double.MIN_EXPONENT, Double.MAX_VALUE, Double.MIN_NORMAL } },
                { UINT32, new int[0] }
        };
    }

    @Test(dataProvider = "IllegalJavaArrays", expectedExceptions = IllegalArgumentException.class)
    public <Z> void testNegativeJavaArray(LayoutType<Z> layout, Object javaArray) {
        Array<Z> ar = Array.ofPrimitiveArray(layout, javaArray);
    }

    @DataProvider(name = "IllegalJavaArrays")
    Object[][] illegalJavaArrays() {
        return new Object[][]{
                { UINT8, new boolean[] {} },
                { LayoutType.ofStruct(Point.class), new Point[0] },
                { INT16, new Integer[] { 1, 2, 3 } },
                { INT64, new short[] { 1, 2, 3 } },
        };
    }

    @Test
    public void testSlice() {
        // Head
        Array<Integer> view = arInt.slice(0, 3);
        assertEquals(view.length(), 3);
        assertEquals(view.get(0), arInt.get(0));
        assertEquals(view.get(2), arInt.get(2));

        // Middle
        view = arInt.slice(5, 2);
        assertEquals(view.length(), 2);
        assertEquals(view.get(0), arInt.get(5));
        assertEquals(view.get(1), arInt.get(6));

        // Tail
        view = arInt.slice(6, 4);
        assertEquals(view.length(), 4);
        assertEquals(view.get(0), arInt.get(6));
        assertEquals(view.get(3), arInt.get(9));

        // Negative start
        assertThrows(IndexOutOfBoundsException.class, () ->
                arInt.slice(-1, 4));
        // Negative count
        assertThrows(IndexOutOfBoundsException.class, () ->
                arInt.slice(7, -4));
        // Overflow
        assertThrows(IndexOutOfBoundsException.class, () ->
                arInt.slice(7, 5));
    }

    @Test
    public void testAssign() {
        int size, copied;
        size = copied = data.length;

        try (Scope s = Scope.globalScope().fork()) {
            // Native -> Java
            int[] jaInt = new int[size];
            Array.assign(arInt, Array.ofPrimitiveArray(INT32, jaInt));
            verifyIntArray(at(jaInt), arInt::get, 0, 0, copied);

            // Java -> Native
            Array<Integer> naInt = s.allocateArray(UINT32, size);
            Array.assign(Array.ofPrimitiveArray(UINT32, data), naInt);
            verifyIntArray(naInt::get, at(data), 0, 0, copied);

            // Native -> Native
            Array<Integer> naInt2 = s.allocateArray(INT32, size);
            Array.assign(arInt, naInt2);
            verifyIntArray(naInt2::get, arInt::get, 0, 0, copied);
        }
    }

    @Test
    public void testAssignFail() {
        try (Scope s = Scope.globalScope().fork()) {
            Array<Integer> buf = s.allocateArray(INT32, data.length + 1);

            // Larger array
            assertThrows(IllegalArgumentException.class, () ->
                    Array.assign(arInt, buf));

            // Smaller array
            assertThrows(IllegalArgumentException.class, () ->
                    Array.assign(arInt, buf.slice(0, data.length - 1)));

            // Type mismatch, INT32 vs. UINT32
            assertThrows(IllegalArgumentException.class, () ->
                    Array.assign(arInt, s.allocateArray(UINT32, data.length)));

            // Type mismatch, INT32 vs. INT16
            assertThrows(IllegalArgumentException.class, () ->
                    Array.assign(arInt, s.allocateArray(LayoutType.ofInt(INT16.layout()), data.length)));
        }
    }

    @Test(dataProvider = "CopyCases")
    public void testCopySuccess(String caseName, int size, int srcPos, int dstPos, int copied, int firstValue) {
        try (Scope s = Scope.globalScope().fork()) {
            // Native -> Java
            int[] jaInt = new int[size];
            Array.copy(arInt, srcPos, Array.ofPrimitiveArray(INT32, jaInt), dstPos, copied);
            verifyIntArray(at(jaInt), arInt::get, dstPos, srcPos, copied);
            assertEquals(jaInt[dstPos], firstValue);

            // Java -> Native
            Array<Integer> naInt = s.allocateArray(UINT32, size);
            Array.copy(Array.ofPrimitiveArray(UINT32, data), srcPos, naInt, dstPos, copied);
            verifyIntArray(naInt::get, at(data), dstPos, srcPos, copied);
            assertEquals((int) naInt.get(dstPos), firstValue);

            // Native -> Native
            Array<Integer> naInt2 = s.allocateArray(INT32, size);
            Array.copy(arInt, (long) srcPos, naInt2, (long) dstPos, (long) copied);
            verifyIntArray(naInt2::get, arInt::get, dstPos, srcPos, copied);
            assertEquals((int) naInt2.get(dstPos), firstValue);
        }
    }

    @Test(dataProvider = "OutOfBound")
    public void testCopyExceptions(String caseName, int size, int srcPos, int dstPos, int copied,
                                Class<? extends Throwable> expected) {
        try (Scope s = Scope.globalScope().fork()) {
            // Native -> Native
            assertThrows(expected, () -> {
                        Array<Integer> naInt2 = s.allocateArray(INT32, size);
                        Array.copy(arInt, (long) srcPos, naInt2, (long) dstPos, (long) copied);
                    });
        }
    }

    @Test
    public void testCopyMismatchType() {
        try (Scope s = Scope.globalScope().fork()) {

            // Type mismatch, INT32 vs. UINT32
            assertThrows(IllegalArgumentException.class, () ->
                    Array.copy(arInt, 3,
                            s.allocateArray(UINT32, data.length), 2,
                            3));

            // Type mismatch, INT32 vs. INT16
            assertThrows(IllegalArgumentException.class, () ->
                    Array.copy(arInt, 3,
                            s.allocateArray(LayoutType.ofInt(INT16.layout()), data.length),
                            4,2));
        }
    }

    final private static Array<Integer> EMPTY_INT_ARRAY = Pointer.<Integer>ofNull().withSize(0);

    @Test
    public void testCopyZeroElements() {
        try (Scope s = Scope.globalScope().fork()) {
            // Frozen target
            Array<Integer> naInt = s.allocateArray(INT32, 1);
            naInt.set(0, 11);

            // Native -> Native
            Array.copy(arInt, 0L, naInt, 0L, 0L);
            assertEquals((int) naInt.get(0), 11);

            Array.copy(EMPTY_INT_ARRAY, 0, naInt, 0, 0L);
            assertEquals((int) naInt.get(0), 11);

            Array.copy(arInt, 0, EMPTY_INT_ARRAY, 0, 0L);
            assertEquals((int) naInt.get(0), 11);

            assertThrows(NullPointerException.class, () ->
                    Array.copy(null, 0L, naInt, 0L, 0L));

            assertThrows(NullPointerException.class, () ->
                    Array.copy(arInt, 0, null, 0, 0L));
        }
    }

    @DataProvider(name = "CopyCases")
    public static Object[][] cases() {
        return new Object[][] {
                { "Head to Head", 5, 0, 0, 3, 1 },
                { "Head to Middle", 5, 0, 2, 2, 1 },
                { "Head to Tail", 5, 0, 2, 3, 1 },
                { "Middle to Head", 5, 2, 0, 3, 3 },
                { "Middle to Middle", 5, 3, 2, 2, 4 },
                { "Middle to Tail", 5, 4, 2, 3, 5 },
                { "Tail to Head", 5, 7, 0, 3, 8 },
                { "Tail to Middle", 5, 8, 2, 2, 9 },
                { "Tail to Tail", 5, 7, 2, 3, 8 }
        };
    }

    @DataProvider(name = "OutOfBound")
    public static Object[][] errors() {
        return new Object[][] {
                { "Negative SrcPos", 5, -1, 0, 3, IndexOutOfBoundsException.class },
                { "Negative DstPos", 5, 0, -1, 3, IndexOutOfBoundsException.class },
                { "Negative Count", 5, 0, 0, -2, IndexOutOfBoundsException.class },
                { "BeyondSrc", 15, 12, 0, 3, IndexOutOfBoundsException.class },
                { "BeyondDst", 5, 5, 8, 1, IndexOutOfBoundsException.class },
                { "OverflowDst", 5, 0, 3, 3, IndexOutOfBoundsException.class },
                { "OverflowSrc", 5, 7, 0, 4, IndexOutOfBoundsException.class }
        };
    }
}
