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
 * @run testng Int128VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.IntVector;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class Int128VectorTests extends AbstractVectorTest {

    static final IntVector.IntSpecies<Shapes.S128Bit> SPECIES =
                IntVector.species(Shapes.S_128_BIT);

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);

    interface FUnOp {
        int apply(int a);
    }

    static void assertArraysEquals(int[] a, int[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i]), "at index #" + i + ", input = " + a[i]);
        }
    }

    static void assertArraysEquals(int[] a, int[] r, boolean[] mask, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], mask[i % SPECIES.length()] ? f.apply(a[i]) : a[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], mask[i % SPECIES.length()] ? f.apply(a[i]) : a[i], "at index #" + i + ", input = " + a[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    interface FReductionOp {
      int apply(int[] a, int idx);
    }

    static void assertReductionArraysEquals(int[] a, int[] b, FReductionOp f) {
      int i = 0;
      try {
        for (; i < a.length; i += SPECIES.length()) {
          Assert.assertEquals(b[i], f.apply(a, i));
        }
      } catch (AssertionError e) {
        Assert.assertEquals(b[i], f.apply(a, i), "at index #" + i);
      }
    }

    interface FBinOp {
        int apply(int a, int b);
    }

    interface FBinMaskOp {
        int apply(int a, int b, boolean m);

        static FBinMaskOp lift(FBinOp f) {
            return (a, b, m) -> m ? f.apply(a, b) : a;
        }
    }

    static void assertArraysEquals(int[] a, int[] b, int[] r, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i]);
        }
    }

    static void assertArraysEquals(int[] a, int[] b, int[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(int[] a, int[] b, int[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }
    static final List<IntFunction<int[]>> INT_GENERATORS = List.of(
            withToString("int[i * 5]", (int s) -> {
                return fill(s * 1000,
                            i -> (int)(i * 5));
            }),
            withToString("int[i + 1]", (int s) -> {
                return fill(s * 1000,
                            i -> (((int)(i + 1) == 0) ? 1 : (int)(i + 1)));
            }),
            withToString("int[cornerCaseValue(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> cornerCaseValue(i));
            })
    );

    // Create combinations of pairs
    // @@@ Might be sensitive to order e.g. div by 0
    static final List<List<IntFunction<int[]>>> INT_GENERATOR_PAIRS =
        Stream.of(INT_GENERATORS.get(0)).
                flatMap(fa -> INT_GENERATORS.stream().skip(1).map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] intBinaryOpProvider() {
        return INT_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intBinaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> INT_GENERATOR_PAIRS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intUnaryOpProvider() {
        return INT_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intUnaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> INT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm};
                })).
                toArray(Object[][]::new);
    }

    static final List<IntFunction<int[]>> INT_COMPARE_GENERATORS = List.of(
            withToString("int[i]", (int s) -> {
                return fill(s * 1000,
                            i -> (int)i);
            }),
            withToString("int[i + 1]", (int s) -> {
                return fill(s * 1000,
                            i -> (int)(i + 1));
            }),
            withToString("int[i - 2]", (int s) -> {
                return fill(s * 1000,
                            i -> (int)(i - 2));
            }),
            withToString("int[zigZag(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> i%3 == 0 ? (int)i : (i%3 == 1 ? (int)(i + 1) : (int)(i - 2)));
            }),
            withToString("int[cornerCaseValue(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> cornerCaseValue(i));
            })
    );

    static final List<List<IntFunction<int[]>>> INT_COMPARE_GENERATOR_PAIRS =
        INT_COMPARE_GENERATORS.stream().
                flatMap(fa -> INT_COMPARE_GENERATORS.stream().map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] intCompareOpProvider() {
        return INT_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    interface ToIntF {
        int apply(int i);
    }

    static int[] fill(int s , ToIntF f) {
        return fill(new int[s], f);
    }

    static int[] fill(int[] a, ToIntF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }
    
    static int cornerCaseValue(int i) {
        switch(i % 5) {
            case 0:
                return Integer.MAX_VALUE;
            case 1:
                return Integer.MIN_VALUE;
            case 2:
                return Integer.MIN_VALUE;
            case 3:
                return Integer.MAX_VALUE;
            default:
                return (int)0;
        }
    }


    static int add(int a, int b) {
        return (int)(a + b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void addInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length()); 
        int[] b = fb.apply(SPECIES.length()); 
        int[] r = new int[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.add(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Int128VectorTests::add);
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void addInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Integer, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.add(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Int128VectorTests::add);
    }

    static int sub(int a, int b) {
        return (int)(a - b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void subInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length()); 
        int[] b = fb.apply(SPECIES.length()); 
        int[] r = new int[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.sub(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Int128VectorTests::sub);
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void subInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Integer, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.sub(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Int128VectorTests::sub);
    }




    static int mul(int a, int b) {
        return (int)(a * b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void mulInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length()); 
        int[] b = fb.apply(SPECIES.length()); 
        int[] r = new int[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.mul(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Int128VectorTests::mul);
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void mulInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Integer, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.mul(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Int128VectorTests::mul);
    }


    static int and(int a, int b) {
        return (int)(a & b);
    }



    @Test(dataProvider = "intBinaryOpProvider")
    static void andInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length()); 
        int[] b = fb.apply(SPECIES.length()); 
        int[] r = new int[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.and(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Int128VectorTests::and);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void andInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Integer, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.and(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Int128VectorTests::and);
    }



    static int or(int a, int b) {
        return (int)(a | b);
    }



    @Test(dataProvider = "intBinaryOpProvider")
    static void orInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length()); 
        int[] b = fb.apply(SPECIES.length()); 
        int[] r = new int[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.or(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Int128VectorTests::or);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void orInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Integer, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.or(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Int128VectorTests::or);
    }



    static int xor(int a, int b) {
        return (int)(a ^ b);
    }



    @Test(dataProvider = "intBinaryOpProvider")
    static void xorInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length()); 
        int[] b = fb.apply(SPECIES.length()); 
        int[] r = new int[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.xor(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Int128VectorTests::xor);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void xorInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Integer, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.xor(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Int128VectorTests::xor);
    }


    static int max(int a, int b) {
        return (int)((a > b) ? a : b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void maxInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length()); 
        int[] b = fb.apply(SPECIES.length()); 
        int[] r = new int[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.max(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Int128VectorTests::max);
    }

    static int min(int a, int b) {
        return (int)((a < b) ? a : b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void minInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length()); 
        int[] b = fb.apply(SPECIES.length()); 
        int[] r = new int[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.min(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Int128VectorTests::min);
    }

    static int andAll(int[] a, int idx) {
        int res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res &= a[i];
        }

        return res;
    }


    @Test(dataProvider = "intUnaryOpProvider")
    static void andAllInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.andAll();
            }
        }

        assertReductionArraysEquals(a, r, Int128VectorTests::andAll);
    }


    static int orAll(int[] a, int idx) {
        int res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res |= a[i];
        }

        return res;
    }


    @Test(dataProvider = "intUnaryOpProvider")
    static void orAllInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.orAll();
            }
        }

        assertReductionArraysEquals(a, r, Int128VectorTests::orAll);
    }


    static int xorAll(int[] a, int idx) {
        int res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res ^= a[i];
        }

        return res;
    }


    @Test(dataProvider = "intUnaryOpProvider")
    static void xorAllInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.xorAll();
            }
        }

        assertReductionArraysEquals(a, r, Int128VectorTests::xorAll);
    }

    static int addAll(int[] a, int idx) {
        int res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res += a[i];
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpProvider")
    static void addAllInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.addAll();
            }
        }

        assertReductionArraysEquals(a, r, Int128VectorTests::addAll);
    }
    static int subAll(int[] a, int idx) {
        int res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res -= a[i];
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpProvider")
    static void subAllInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.subAll();
            }
        }

        assertReductionArraysEquals(a, r, Int128VectorTests::subAll);
    }
    static int mulAll(int[] a, int idx) {
        int res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res *= a[i];
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpProvider")
    static void mulAllInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.mulAll();
            }
        }

        assertReductionArraysEquals(a, r, Int128VectorTests::mulAll);
    }
    static int minAll(int[] a, int idx) {
        int res = Integer.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res = (res < a[i])?res:a[i];
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpProvider")
    static void minAllInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.minAll();
            }
        }

        assertReductionArraysEquals(a, r, Int128VectorTests::minAll);
    }
    static int maxAll(int[] a, int idx) {
        int res = Integer.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res = (res > a[i])?res:a[i];
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpProvider")
    static void maxAllInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.maxAll();
            }
        }

        assertReductionArraysEquals(a, r, Int128VectorTests::maxAll);
    }

    @Test(dataProvider = "intCompareOpProvider")
    static void lessThanInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Integer, Shapes.S128Bit> mv = av.lessThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void greaterThanInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Integer, Shapes.S128Bit> mv = av.greaterThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] > b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void equalInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Integer, Shapes.S128Bit> mv = av.equal(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void notEqualInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Integer, Shapes.S128Bit> mv = av.notEqual(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] != b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void lessThanEqInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Integer, Shapes.S128Bit> mv = av.lessThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] <= b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void greaterThanEqInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Integer, Shapes.S128Bit> mv = av.greaterThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] >= b[i + j]);
                }
            }
        }
    }


    static int blend(int a, int b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void blendInt128VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Integer, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                IntVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Int128VectorTests::blend);
    }



















    static int neg(int a) {
        return (int)(-((int)a));
    }

    @Test(dataProvider = "intUnaryOpProvider")
    static void negInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.neg().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Int128VectorTests::neg);
    }

    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void negMaskedInt128VectorTests(IntFunction<int[]> fa,
                                                IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Integer, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.neg(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Int128VectorTests::neg);
    }

    static int abs(int a) {
        return (int)(Math.abs((int)a));
    }

    @Test(dataProvider = "intUnaryOpProvider")
    static void absInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Int128VectorTests::abs);
    }

    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void absMaskedInt128VectorTests(IntFunction<int[]> fa,
                                                IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Integer, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.abs(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Int128VectorTests::abs);
    }


    static int not(int a) {
        return (int)(~((int)a));
    }



    @Test(dataProvider = "intUnaryOpProvider")
    static void notInt128VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.not().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Int128VectorTests::not);
    }



    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void notMaskedInt128VectorTests(IntFunction<int[]> fa,
                                                IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Integer, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.not(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Int128VectorTests::not);
    }




}

