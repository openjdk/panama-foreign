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

/*
 * @test
 * @run testng BulkArrayTest
 */

import java.foreign.Scope;
import java.foreign.layout.Value;
import java.foreign.memory.Array;
import java.foreign.memory.LayoutType;
import java.util.Objects;

import org.testng.annotations.*;

import static org.testng.Assert.*;

public class BulkArrayTest {

    @Test(dataProvider = "arrays")
    public void testBulk(LayoutType<?> type, Object arr) {
        try (Scope sc = Scope.globalScope().fork()) {
            Array<?> nativeArr = sc.allocateArray(type, 10);
            Array.assign(arr, nativeArr);
            Object temp = java.lang.reflect.Array.newInstance(arr.getClass().componentType(), 10);
            Array.assign(nativeArr, temp);
            assertTrue(Objects.deepEquals(arr, temp));
        }
    }

    @DataProvider
    public static Object[][] arrays() {
        return new Object[][] {
                new Object[] {
                        LayoutType.ofBoolean(Value.ofUnsignedInt(8)),
                        new boolean[] { true, false, true, false, true, false, true, false, true, false }
                },
                new Object[] {
                        LayoutType.ofByte(Value.ofUnsignedInt(8)),
                        new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }
                },
                new Object[] {
                        LayoutType.ofShort(Value.ofUnsignedInt(16)),
                        new short[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }
                },
                new Object[] {
                        LayoutType.ofInt(Value.ofUnsignedInt(32)),
                        new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }
                },
                new Object[] {
                        LayoutType.ofFloat(Value.ofFloatingPoint(32)),
                        new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }
                },
                new Object[] {
                        LayoutType.ofLong(Value.ofUnsignedInt(64)),
                        new long[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }
                },
                new Object[] {
                        LayoutType.ofDouble(Value.ofFloatingPoint(64)),
                        new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }
                }
        };
    }
}
