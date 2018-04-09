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
 * @run testng Float64VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.FloatVector;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class Float64VectorTests extends AbstractVectorTest {

    static final FloatVector.FloatSpecies<Shapes.S64Bit> SPECIES =
                FloatVector.speciesInstance(Shapes.S_64_BIT);

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 10);

    interface FUnOp {
        float apply(float a);
    }

    static void assertArraysEquals(float[] a, float[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(float[] a, float[] r, boolean[] mask, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(mask[i % SPECIES.length()] ? f.apply(a[i]) : a[i], r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(mask[i % SPECIES.length()] ? f.apply(a[i]) : a[i], r[i], "at index #" + i);
        }
    }

    interface FReductionOp {
      float apply(float[] a, int idx);
    }

    static void assertReductionArraysEquals(float[] a, float[] b, FReductionOp f) {
      int i = 0;
      try {
        for (; i < a.length; i += SPECIES.length()) {
          Assert.assertEquals(f.apply(a, i), b[i]);
        }
      } catch (AssertionError e) {
        Assert.assertEquals(f.apply(a, i), b[i], "at index #" + i);
      }
    }

    interface FBinOp {
        float apply(float a, float b);
    }

    interface FBinMaskOp {
        float apply(float a, float b, boolean m);

        static FBinMaskOp lift(FBinOp f) {
            return (a, b, m) -> m ? f.apply(a, b) : a;
        }
    }

    static void assertArraysEquals(float[] a, float[] b, float[] r, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i], b[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(float[] a, float[] b, float[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(float[] a, float[] b, float[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i], mask[i % SPECIES.length()]), r[i]);
            }
        } catch (AssertionError err) {
            Assert.assertEquals(f.apply(a[i], b[i], mask[i % SPECIES.length()]), r[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }
    
    static final List<IntFunction<float[]>> FLOAT_GENERATORS = List.of(
            withToString("float[i * 5]", (int s) -> {
                return fill(s * 1000,
                            i -> (float)(i * 5));
            }),
            withToString("float[i + 1]", (int s) -> {
                return fill(s * 1000,
                            i -> (((float)(i + 1) == 0) ? 1 : (float)(i + 1)));
            })
    );

    // Create combinations of pairs
    // @@@ Might be sensitive to order e.g. div by 0
    static final List<List<IntFunction<float[]>>> FLOAT_GENERATOR_PAIRS =
        Stream.of(FLOAT_GENERATORS.get(0)).
                flatMap(fa -> FLOAT_GENERATORS.stream().skip(1).map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] floatBinaryOpProvider() {
        return FLOAT_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] floatBinaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> FLOAT_GENERATOR_PAIRS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] floatUnaryOpProvider() {
        return FLOAT_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] floatUnaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> FLOAT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm};
                })).
                toArray(Object[][]::new);
    }

    static final List<IntFunction<float[]>> FLOAT_COMPARE_GENERATORS = List.of(
            withToString("float[i]", (int s) -> {
                return fill(s * 1000,
                            i -> (float)i);
            }),
            withToString("float[i + 1]", (int s) -> {
                return fill(s * 1000,
                            i -> (float)(i + 1));
            }),
            withToString("float[i - 2]", (int s) -> {
                return fill(s * 1000,
                            i -> (float)(i - 2));
            }),
            withToString("float[zigZag(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> i%3 == 0 ? (float)i : (i%3 == 1 ? (float)(i + 1) : (float)(i - 2)));
            }),
            withToString("float[cornerComparisonCase(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> cornerComparisonCase(i));
            })
    );

    static final List<List<IntFunction<float[]>>> FLOAT_COMPARE_GENERATOR_PAIRS =
        FLOAT_COMPARE_GENERATORS.stream().
                flatMap(fa -> FLOAT_COMPARE_GENERATORS.stream().map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] floatCompareOpProvider() {
        return FLOAT_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    interface ToFloatF {
        float apply(int i);
    }

    static float[] fill(int s , ToFloatF f) {
        return fill(new float[s], f);
    }

    static float[] fill(float[] a, ToFloatF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }
    
    static float cornerComparisonCase(int i) {
        switch(i % 7) {
            case 0:
                return Float.MAX_VALUE;
            case 1:
                return Float.MIN_VALUE;
            case 2:
                return Float.NEGATIVE_INFINITY;
            case 3:
                return Float.POSITIVE_INFINITY;
            case 4:
                return Float.NaN;
            case 5:
                return (float)0.0;
            default:
                return (float)-0.0;
        }
    }


    static float add(float a, float b) {
        return (float)(a + b);
    }

    @Test(dataProvider = "floatBinaryOpProvider")
    static void addFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length()); 
        float[] b = fb.apply(SPECIES.length()); 
        float[] r = new float[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.add(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Float64VectorTests::add);
    }

    @Test(dataProvider = "floatBinaryOpMaskProvider")
    static void addFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb,
                                          IntFunction<boolean[]> fm) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = new float[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Float, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.add(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Float64VectorTests::add);
    }

    static float sub(float a, float b) {
        return (float)(a - b);
    }

    @Test(dataProvider = "floatBinaryOpProvider")
    static void subFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length()); 
        float[] b = fb.apply(SPECIES.length()); 
        float[] r = new float[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.sub(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Float64VectorTests::sub);
    }

    @Test(dataProvider = "floatBinaryOpMaskProvider")
    static void subFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb,
                                          IntFunction<boolean[]> fm) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = new float[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Float, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.sub(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Float64VectorTests::sub);
    }


    static float div(float a, float b) {
        return (float)(a / b);
    }



    @Test(dataProvider = "floatBinaryOpProvider")
    static void divFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length()); 
        float[] b = fb.apply(SPECIES.length()); 
        float[] r = new float[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.div(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Float64VectorTests::div);
    }



    @Test(dataProvider = "floatBinaryOpMaskProvider")
    static void divFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb,
                                          IntFunction<boolean[]> fm) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = new float[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Float, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.div(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Float64VectorTests::div);
    }


    static float mul(float a, float b) {
        return (float)(a * b);
    }

    @Test(dataProvider = "floatBinaryOpProvider")
    static void mulFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length()); 
        float[] b = fb.apply(SPECIES.length()); 
        float[] r = new float[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.mul(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Float64VectorTests::mul);
    }

    @Test(dataProvider = "floatBinaryOpMaskProvider")
    static void mulFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb,
                                          IntFunction<boolean[]> fm) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = new float[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Float, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.mul(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Float64VectorTests::mul);
    }










    static float max(float a, float b) {
        return (float)(Math.max(a, b));
    }

    @Test(dataProvider = "floatBinaryOpProvider")
    static void maxFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length()); 
        float[] b = fb.apply(SPECIES.length()); 
        float[] r = new float[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.max(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Float64VectorTests::max);
    }

    static float min(float a, float b) {
        return (float)(Math.min(a, b));
    }

    @Test(dataProvider = "floatBinaryOpProvider")
    static void minFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length()); 
        float[] b = fb.apply(SPECIES.length()); 
        float[] r = new float[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.min(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Float64VectorTests::min);
    }






    static float addAll(float[] a, int idx) {
        float res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res += a[i];
        }

        return res;
    }
    @Test(dataProvider = "floatUnaryOpProvider")
    static void addAllFloat64VectorTests(IntFunction<float[]> fa) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = new float[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.addAll();
            }
        }

        assertReductionArraysEquals(a, r, Float64VectorTests::addAll);
    }
    static float subAll(float[] a, int idx) {
        float res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res -= a[i];
        }

        return res;
    }
    @Test(dataProvider = "floatUnaryOpProvider")
    static void subAllFloat64VectorTests(IntFunction<float[]> fa) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = new float[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.subAll();
            }
        }

        assertReductionArraysEquals(a, r, Float64VectorTests::subAll);
    }

    @Test(dataProvider = "floatCompareOpProvider")
    static void lessThanFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Float, Shapes.S64Bit> mv = av.lessThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "floatCompareOpProvider")
    static void greaterThanFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Float, Shapes.S64Bit> mv = av.greaterThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] > b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "floatCompareOpProvider")
    static void equalFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Float, Shapes.S64Bit> mv = av.equal(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "floatCompareOpProvider")
    static void notEqualFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Float, Shapes.S64Bit> mv = av.notEqual(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] != b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "floatCompareOpProvider")
    static void lessThanEqFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Float, Shapes.S64Bit> mv = av.lessThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] <= b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "floatCompareOpProvider")
    static void greaterThanEqFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Float, Shapes.S64Bit> mv = av.greaterThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] >= b[i + j]);
                }
            }
        }
    }


    static float blend(float a, float b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "floatBinaryOpMaskProvider")
    static void blendFloat64VectorTests(IntFunction<float[]> fa, IntFunction<float[]> fb,
                                          IntFunction<boolean[]> fm) {
        float[] a = fa.apply(SPECIES.length());
        float[] b = fb.apply(SPECIES.length());
        float[] r = new float[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Float, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                FloatVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Float64VectorTests::blend);
    }

    static float neg(float a) {
        return (float)(-((float)a));
    }

    @Test(dataProvider = "floatUnaryOpProvider")
    static void negFloat64VectorTests(IntFunction<float[]> fa) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = new float[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.neg().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Float64VectorTests::neg);
    }

    @Test(dataProvider = "floatUnaryOpMaskProvider")
    static void negMaskedFloat64VectorTests(IntFunction<float[]> fa,
                                                IntFunction<boolean[]> fm) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = new float[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Float, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.neg(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Float64VectorTests::neg);
    }

    static float abs(float a) {
        return (float)(Math.abs((float)a));
    }

    @Test(dataProvider = "floatUnaryOpProvider")
    static void absFloat64VectorTests(IntFunction<float[]> fa) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = new float[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Float64VectorTests::abs);
    }

    @Test(dataProvider = "floatUnaryOpMaskProvider")
    static void absMaskedFloat64VectorTests(IntFunction<float[]> fa,
                                                IntFunction<boolean[]> fm) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = new float[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Float, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.abs(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Float64VectorTests::abs);
    }





    static float sqrt(float a) {
        return (float)(Math.sqrt((double)a));
    }



    @Test(dataProvider = "floatUnaryOpProvider")
    static void sqrtFloat64VectorTests(IntFunction<float[]> fa) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = new float[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.sqrt().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Float64VectorTests::sqrt);
    }



    @Test(dataProvider = "floatUnaryOpMaskProvider")
    static void sqrtMaskedFloat64VectorTests(IntFunction<float[]> fa,
                                                IntFunction<boolean[]> fm) {
        float[] a = fa.apply(SPECIES.length());
        float[] r = new float[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Float, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                FloatVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.sqrt(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Float64VectorTests::sqrt);
    }

}

