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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */

/*
 * @test
 * @modules jdk.incubator.vector
 * @run testng Double128VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.DoubleVector;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class Double128VectorTests extends AbstractVectorTest {

    static final DoubleVector.DoubleSpecies<Shapes.S128Bit> SPECIES = (DoubleVector.DoubleSpecies<Shapes.S128Bit>)
                Vector.speciesInstance(Double.class, Shapes.S_128_BIT);


    interface FUnOp {
        double apply(double a);
    }

    static void assertArraysEquals(double[] a, double[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(double[] a, double[] r, boolean[] mask, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(mask[i % SPECIES.length()] ? f.apply(a[i]) : a[i], r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(mask[i % SPECIES.length()] ? f.apply(a[i]) : a[i], r[i], "at index #" + i);
        }
    }

    interface FBinOp {
        double apply(double a, double b);
    }

    interface FBinMaskOp {
        double apply(double a, double b, boolean m);

        static FBinMaskOp lift(FBinOp f) {
            return (a, b, m) -> m ? f.apply(a, b) : a;
        }
    }

    static void assertArraysEquals(double[] a, double[] b, double[] r, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i], b[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(double[] a, double[] b, double[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(double[] a, double[] b, double[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i], mask[i % SPECIES.length()]), r[i]);
            }
        } catch (AssertionError err) {
            Assert.assertEquals(f.apply(a[i], b[i], mask[i % SPECIES.length()]), r[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }
    
    static final List<IntFunction<double[]>> DOUBLE_GENERATORS = List.of(
            withToString("double[i * 5]", (int s) -> {
                return fill(s * 1000,
                            i -> (double)(i * 5));
            }),
            withToString("double[i + 1]", (int s) -> {
                return fill(s * 1000,
                            i -> (((double)(i + 1) == 0) ? 1 : (double)(i + 1)));
            })
    );

    // Create combinations of pairs
    // @@@ Might be sensitive to order e.g. div by 0
    static final List<List<IntFunction<double[]>>> DOUBLE_GENERATOR_PAIRS =
        Stream.of(DOUBLE_GENERATORS.get(0)).
                flatMap(fa -> DOUBLE_GENERATORS.stream().skip(1).map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] doubleBinaryOpProvider() {
        return DOUBLE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] doubleBinaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> DOUBLE_GENERATOR_PAIRS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] doubleUnaryOpProvider() {
        return DOUBLE_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] doubleUnaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> DOUBLE_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm};
                })).
                toArray(Object[][]::new);
    }

    static final List<IntFunction<double[]>> DOUBLE_COMPARE_GENERATORS = List.of(
            withToString("double[i]", (int s) -> {
                return fill(s * 1000,
                            i -> (double)i);
            }),
            withToString("double[i + 1]", (int s) -> {
                return fill(s * 1000,
                            i -> (double)(i + 1));
            }),
            withToString("double[i - 2]", (int s) -> {
                return fill(s * 1000,
                            i -> (double)(i - 2));
            }),
            withToString("double[zigZag(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> i%3 == 0 ? (double)i : (i%3 == 1 ? (double)(i + 1) : (double)(i - 2)));
            }),
            withToString("double[cornerComparisonCase(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> cornerComparisonCase(i));
            })
    );

    static final List<List<IntFunction<double[]>>> DOUBLE_COMPARE_GENERATOR_PAIRS =
        DOUBLE_COMPARE_GENERATORS.stream().
                flatMap(fa -> DOUBLE_COMPARE_GENERATORS.stream().map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] doubleCompareOpProvider() {
        return DOUBLE_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    interface ToDoubleF {
        double apply(int i);
    }

    static double[] fill(int s , ToDoubleF f) {
        return fill(new double[s], f);
    }

    static double[] fill(double[] a, ToDoubleF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }
    
    static double cornerComparisonCase(int i) {
        switch(i % 7) {
            case 0:
                return Double.MAX_VALUE;
            case 1:
                return Double.MIN_VALUE;
            case 2:
                return Double.NEGATIVE_INFINITY;
            case 3:
                return Double.POSITIVE_INFINITY;
            case 4:
                return Double.NaN;
            case 5:
                return (double)0.0;
            default:
                return (double)-0.0;
        }
    }


    static double add(double a, double b) {
        return (double)(a + b);
    }

    @Test(dataProvider = "doubleBinaryOpProvider", invocationCount = 10)
    static void addDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.add(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double128VectorTests::add);
    }

    @Test(dataProvider = "doubleBinaryOpMaskProvider", invocationCount = 10)
    static void addDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb,
                                          IntFunction<boolean[]> fm) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Double, Shapes.S128Bit> vmask = SPECIES.constantMask(mask);

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.add(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, mask, Double128VectorTests::add);
    }

    static double sub(double a, double b) {
        return (double)(a - b);
    }

    @Test(dataProvider = "doubleBinaryOpProvider", invocationCount = 10)
    static void subDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.sub(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double128VectorTests::sub);
    }

    @Test(dataProvider = "doubleBinaryOpMaskProvider", invocationCount = 10)
    static void subDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb,
                                          IntFunction<boolean[]> fm) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Double, Shapes.S128Bit> vmask = SPECIES.constantMask(mask);

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.sub(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, mask, Double128VectorTests::sub);
    }


    static double div(double a, double b) {
        return (double)(a / b);
    }



    @Test(dataProvider = "doubleBinaryOpProvider", invocationCount = 10)
    static void divDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.div(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double128VectorTests::div);
    }



    @Test(dataProvider = "doubleBinaryOpMaskProvider", invocationCount = 10)
    static void divDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb,
                                          IntFunction<boolean[]> fm) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Double, Shapes.S128Bit> vmask = SPECIES.constantMask(mask);

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.div(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, mask, Double128VectorTests::div);
    }


    static double mul(double a, double b) {
        return (double)(a * b);
    }

    @Test(dataProvider = "doubleBinaryOpProvider", invocationCount = 10)
    static void mulDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.mul(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double128VectorTests::mul);
    }

    @Test(dataProvider = "doubleBinaryOpMaskProvider", invocationCount = 10)
    static void mulDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb,
                                          IntFunction<boolean[]> fm) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Double, Shapes.S128Bit> vmask = SPECIES.constantMask(mask);

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.mul(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, mask, Double128VectorTests::mul);
    }










    static double max(double a, double b) {
        return (double)(Math.max(a, b));
    }

    @Test(dataProvider = "doubleBinaryOpProvider", invocationCount = 10)
    static void maxDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.max(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double128VectorTests::max);
    }

    static double min(double a, double b) {
        return (double)(Math.min(a, b));
    }

    @Test(dataProvider = "doubleBinaryOpProvider", invocationCount = 10)
    static void minDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.min(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double128VectorTests::min);
    }

    @Test(dataProvider = "doubleCompareOpProvider", invocationCount = 10)
    static void lessThanDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            Vector.Mask<Double, Shapes.S128Bit> mv = av.lessThan(bv);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.getElement(j), a[i + j] < b[i + j]);
            }
        }
    }

    @Test(dataProvider = "doubleCompareOpProvider", invocationCount = 10)
    static void greaterThanDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            Vector.Mask<Double, Shapes.S128Bit> mv = av.greaterThan(bv);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.getElement(j), a[i + j] > b[i + j]);
            }
        }
    }

    @Test(dataProvider = "doubleCompareOpProvider", invocationCount = 10)
    static void equalDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            Vector.Mask<Double, Shapes.S128Bit> mv = av.equal(bv);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.getElement(j), a[i + j] == b[i + j]);
            }
        }
    }

    @Test(dataProvider = "doubleCompareOpProvider", invocationCount = 10)
    static void notEqualDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            Vector.Mask<Double, Shapes.S128Bit> mv = av.notEqual(bv);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.getElement(j), a[i + j] != b[i + j]);
            }
        }
    }

    @Test(dataProvider = "doubleCompareOpProvider", invocationCount = 10)
    static void lessThanEqDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            Vector.Mask<Double, Shapes.S128Bit> mv = av.lessThanEq(bv);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.getElement(j), a[i + j] <= b[i + j]);
            }
        }
    }

    @Test(dataProvider = "doubleCompareOpProvider", invocationCount = 10)
    static void greaterThanEqDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            Vector.Mask<Double, Shapes.S128Bit> mv = av.greaterThanEq(bv);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.getElement(j), a[i + j] >= b[i + j]);
            }
        }
    }

    static double blend(double a, double b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "doubleBinaryOpMaskProvider", invocationCount = 10)
    static void blendDouble128VectorTests(IntFunction<double[]> fa, IntFunction<double[]> fb,
                                          IntFunction<boolean[]> fm) {
        double[] a = fa.apply(SPECIES.length());
        double[] b = fb.apply(SPECIES.length());
        double[] r = new double[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Double, Shapes.S128Bit> vmask = SPECIES.constantMask(mask);

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            DoubleVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
            av.blend(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, mask, Double128VectorTests::blend);
    }

    static double neg(double a) {
        return (double)(-((double)a));
    }

    @Test(dataProvider = "doubleUnaryOpProvider", invocationCount = 10)
    static void negDouble128VectorTests(IntFunction<double[]> fa) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = new double[a.length];

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            av.neg().intoArray(r, i);
        }

        assertArraysEquals(a, r, Double128VectorTests::neg);
    }

    @Test(dataProvider = "doubleUnaryOpMaskProvider")
    static void negMaskedDouble128VectorTests(IntFunction<double[]> fa,
                                                IntFunction<boolean[]> fm) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = new double[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Double, Shapes.S128Bit> vmask = SPECIES.constantMask(mask);

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            av.neg(vmask).intoArray(r, i);
        }

        assertArraysEquals(a, r, mask, Double128VectorTests::neg);
    }

    static double abs(double a) {
        return (double)(Math.abs((double)a));
    }

    @Test(dataProvider = "doubleUnaryOpProvider", invocationCount = 10)
    static void absDouble128VectorTests(IntFunction<double[]> fa) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = new double[a.length];

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            av.abs().intoArray(r, i);
        }

        assertArraysEquals(a, r, Double128VectorTests::abs);
    }

    @Test(dataProvider = "doubleUnaryOpMaskProvider")
    static void absMaskedDouble128VectorTests(IntFunction<double[]> fa,
                                                IntFunction<boolean[]> fm) {
        double[] a = fa.apply(SPECIES.length());
        double[] r = new double[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Double, Shapes.S128Bit> vmask = SPECIES.constantMask(mask);

        // Computation.
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            DoubleVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
            av.abs(vmask).intoArray(r, i);
        }

        assertArraysEquals(a, r, mask, Double128VectorTests::abs);
    }
}

