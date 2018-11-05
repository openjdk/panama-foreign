/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 * @run testng LongDoubleTest
 */

import org.testng.annotations.*;

import java.foreign.Libraries;
import java.foreign.Scope;
import java.foreign.annotations.NativeCallback;
import java.foreign.annotations.NativeHeader;
import java.foreign.memory.Callback;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.testng.Assert.*;

@Test
public class LongDoubleTest {

    private static final double TOLERANCE = 0.0001;

    @NativeHeader(declarations =
            "id=(f128)f128" +
            "plus=(f128 f128)f128" +
            "minus=(f128 f128)f128" +
            "mul=(f128 f128)f128" +
            "div=(f128 f128)f128" +
            "f=(f128 u64:(f128)f128)f128"
    )
    interface longdouble {
        double id(double arg);
        double plus(double arg1, double arg2);
        double minus(double arg1, double arg2);
        double mul(double arg1, double arg2);
        double div(double arg1, double arg2);
        double f(double arg, Callback<cb> cb);

        @NativeCallback("(f128)f128")
        interface cb {
            double apply(double arg);
        }
    }

    static longdouble lib =
            Libraries.bind(longdouble.class,
                    Libraries.loadLibrary(MethodHandles.lookup(), "LongDouble"));

    @Test(dataProvider = "numbers")
    public void testId(double d) {
        assertEquals(lib.id(d), d, TOLERANCE);
    }

    @Test(dataProvider = "pairs")
    public void testPlus(double d1, double d2) {
        assertEquals(lib.plus(d1, d2), d1 + d2, TOLERANCE);
    }

    @Test(dataProvider = "pairs")
    public void testMinus(double d1, double d2) {
        assertEquals(lib.minus(d1, d2), d1 - d2, TOLERANCE);
    }

    @Test(dataProvider = "pairs")
    public void testMul(double d1, double d2) {
        assertEquals(lib.mul(d1, d2), d1 * d2, TOLERANCE);
    }

    @Test(dataProvider = "pairs")
    public void testDiv(double d1, double d2) {
        assertEquals(lib.div(d1, d2), d1 / d2, TOLERANCE);
    }

    @Test(dataProvider = "numbers")
    public void testCallback(double d) {
        try (Scope sc = Scope.newNativeScope()) {
            assertEquals(lib.f(d, sc.allocateCallback(a -> a)), d, TOLERANCE);
        }
    }

    static DoubleStream doubleStream() {
        return new Random().doubles(Double.MIN_VALUE, Double.MAX_VALUE)
                .limit(100);
    }

    @DataProvider
    public static Object[][] numbers() {
        return doubleStream().mapToObj(d -> new Object[] { d }).toArray(Object[][]::new);
    }

    @DataProvider
    public static Object[][] pairs() {
        double[] d1Arr = doubleStream().toArray();
        double[] d2Arr = doubleStream().toArray();
        Object[][] res = new Object[d1Arr.length][];
        for (int i = 0 ; i < d1Arr.length ; i++) {
            res[i] = new Object[] { d1Arr[i], d2Arr[i]};
        }
        return res;
    }
}
