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
 * @run testng Short64VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.ShortVector;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class Short64VectorTests extends AbstractVectorTest {

    static final ShortVector.ShortSpecies<Shapes.S64Bit> SPECIES =
                ShortVector.species(Shapes.S_64_BIT);

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);

    interface FUnOp {
        short apply(short a);
    }

    static void assertArraysEquals(short[] a, short[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i]), "at index #" + i + ", input = " + a[i]);
        }
    }

    static void assertArraysEquals(short[] a, short[] r, boolean[] mask, FUnOp f) {
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
      short apply(short[] a, int idx);
    }

    static void assertReductionArraysEquals(short[] a, short[] b, FReductionOp f) {
      int i = 0;
      try {
        for (; i < a.length; i += SPECIES.length()) {
          Assert.assertEquals(b[i], f.apply(a, i));
        }
      } catch (AssertionError e) {
        Assert.assertEquals(b[i], f.apply(a, i), "at index #" + i);
      }
    }
 
    interface FBoolReductionOp {
      boolean apply(boolean[] a, int idx);
    }

    static void assertReductionBoolArraysEquals(boolean[] a, boolean[] b, FBoolReductionOp f) {
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
        short apply(short a, short b);
    }

    interface FBinMaskOp {
        short apply(short a, short b, boolean m);

        static FBinMaskOp lift(FBinOp f) {
            return (a, b, m) -> m ? f.apply(a, b) : a;
        }
    }

    static void assertArraysEquals(short[] a, short[] b, short[] r, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i]);
        }
    }

    static void assertArraysEquals(short[] a, short[] b, short[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(short[] a, short[] b, short[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }
    static final List<IntFunction<short[]>> SHORT_GENERATORS = List.of(
            withToString("short[i * 5]", (int s) -> {
                return fill(s * 1000,
                            i -> (short)(i * 5));
            }),
            withToString("short[i + 1]", (int s) -> {
                return fill(s * 1000,
                            i -> (((short)(i + 1) == 0) ? 1 : (short)(i + 1)));
            }),
            withToString("short[cornerCaseValue(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> cornerCaseValue(i));
            })
    );

    // Create combinations of pairs
    // @@@ Might be sensitive to order e.g. div by 0
    static final List<List<IntFunction<short[]>>> SHORT_GENERATOR_PAIRS =
        Stream.of(SHORT_GENERATORS.get(0)).
                flatMap(fa -> SHORT_GENERATORS.stream().skip(1).map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] boolUnaryOpProvider() {
        return BOOL_ARRAY_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] shortBinaryOpProvider() {
        return SHORT_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] shortBinaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> SHORT_GENERATOR_PAIRS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] shortUnaryOpProvider() {
        return SHORT_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] shortUnaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> SHORT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm};
                })).
                toArray(Object[][]::new);
    }

    static final List<IntFunction<short[]>> SHORT_COMPARE_GENERATORS = List.of(
            withToString("short[i]", (int s) -> {
                return fill(s * 1000,
                            i -> (short)i);
            }),
            withToString("short[i + 1]", (int s) -> {
                return fill(s * 1000,
                            i -> (short)(i + 1));
            }),
            withToString("short[i - 2]", (int s) -> {
                return fill(s * 1000,
                            i -> (short)(i - 2));
            }),
            withToString("short[zigZag(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> i%3 == 0 ? (short)i : (i%3 == 1 ? (short)(i + 1) : (short)(i - 2)));
            }),
            withToString("short[cornerCaseValue(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> cornerCaseValue(i));
            })
    );

    static final List<List<IntFunction<short[]>>> SHORT_COMPARE_GENERATOR_PAIRS =
        SHORT_COMPARE_GENERATORS.stream().
                flatMap(fa -> SHORT_COMPARE_GENERATORS.stream().map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] shortCompareOpProvider() {
        return SHORT_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    interface ToShortF {
        short apply(int i);
    }

    static short[] fill(int s , ToShortF f) {
        return fill(new short[s], f);
    }

    static short[] fill(short[] a, ToShortF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }
    
    static short cornerCaseValue(int i) {
        switch(i % 5) {
            case 0:
                return Short.MAX_VALUE;
            case 1:
                return Short.MIN_VALUE;
            case 2:
                return Short.MIN_VALUE;
            case 3:
                return Short.MAX_VALUE;
            default:
                return (short)0;
        }
    }


    static short add(short a, short b) {
        return (short)(a + b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void addShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length()); 
        short[] b = fb.apply(SPECIES.length()); 
        short[] r = new short[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.add(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Short64VectorTests::add);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void addShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = new short[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.add(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Short64VectorTests::add);
    }

    static short sub(short a, short b) {
        return (short)(a - b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void subShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length()); 
        short[] b = fb.apply(SPECIES.length()); 
        short[] r = new short[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.sub(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Short64VectorTests::sub);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void subShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = new short[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.sub(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Short64VectorTests::sub);
    }




    static short mul(short a, short b) {
        return (short)(a * b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void mulShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length()); 
        short[] b = fb.apply(SPECIES.length()); 
        short[] r = new short[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.mul(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Short64VectorTests::mul);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void mulShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = new short[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.mul(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Short64VectorTests::mul);
    }


    static short and(short a, short b) {
        return (short)(a & b);
    }



    @Test(dataProvider = "shortBinaryOpProvider")
    static void andShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length()); 
        short[] b = fb.apply(SPECIES.length()); 
        short[] r = new short[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.and(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Short64VectorTests::and);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void andShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = new short[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.and(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Short64VectorTests::and);
    }



    static short or(short a, short b) {
        return (short)(a | b);
    }



    @Test(dataProvider = "shortBinaryOpProvider")
    static void orShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length()); 
        short[] b = fb.apply(SPECIES.length()); 
        short[] r = new short[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.or(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Short64VectorTests::or);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void orShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = new short[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.or(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Short64VectorTests::or);
    }



    static short xor(short a, short b) {
        return (short)(a ^ b);
    }



    @Test(dataProvider = "shortBinaryOpProvider")
    static void xorShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length()); 
        short[] b = fb.apply(SPECIES.length()); 
        short[] r = new short[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.xor(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Short64VectorTests::xor);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void xorShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = new short[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.xor(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Short64VectorTests::xor);
    }


    static short max(short a, short b) {
        return (short)((a > b) ? a : b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void maxShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length()); 
        short[] b = fb.apply(SPECIES.length()); 
        short[] r = new short[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.max(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Short64VectorTests::max);
    }

    static short min(short a, short b) {
        return (short)((a < b) ? a : b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void minShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length()); 
        short[] b = fb.apply(SPECIES.length()); 
        short[] r = new short[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.min(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Short64VectorTests::min);
    }

    static short andAll(short[] a, int idx) {
        short res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res &= a[i];
        }

        return res;
    }


    @Test(dataProvider = "shortUnaryOpProvider")
    static void andAllShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.andAll();
            }
        }

        assertReductionArraysEquals(a, r, Short64VectorTests::andAll);
    }


    static short orAll(short[] a, int idx) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res |= a[i];
        }

        return res;
    }


    @Test(dataProvider = "shortUnaryOpProvider")
    static void orAllShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.orAll();
            }
        }

        assertReductionArraysEquals(a, r, Short64VectorTests::orAll);
    }


    static short xorAll(short[] a, int idx) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res ^= a[i];
        }

        return res;
    }


    @Test(dataProvider = "shortUnaryOpProvider")
    static void xorAllShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.xorAll();
            }
        }

        assertReductionArraysEquals(a, r, Short64VectorTests::xorAll);
    }

    static short addAll(short[] a, int idx) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res += a[i];
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void addAllShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.addAll();
            }
        }

        assertReductionArraysEquals(a, r, Short64VectorTests::addAll);
    }
    static short subAll(short[] a, int idx) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res -= a[i];
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void subAllShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.subAll();
            }
        }

        assertReductionArraysEquals(a, r, Short64VectorTests::subAll);
    }
    static short mulAll(short[] a, int idx) {
        short res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res *= a[i];
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void mulAllShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.mulAll();
            }
        }

        assertReductionArraysEquals(a, r, Short64VectorTests::mulAll);
    }
    static short minAll(short[] a, int idx) {
        short res = Short.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res = (res < a[i])?res:a[i];
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void minAllShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.minAll();
            }
        }

        assertReductionArraysEquals(a, r, Short64VectorTests::minAll);
    }
    static short maxAll(short[] a, int idx) {
        short res = Short.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res = (res > a[i])?res:a[i];
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void maxAllShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.maxAll();
            }
        }

        assertReductionArraysEquals(a, r, Short64VectorTests::maxAll);
    }

    static boolean anyTrue(boolean[] a, int idx) {
        boolean res = false;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res |= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void anyTrueShort64VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = new boolean[mask.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
              Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromArray(mask, i);
              r[i] = vmask.anyTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Short64VectorTests::anyTrue);
    }


    static boolean allTrue(boolean[] a, int idx) {
        boolean res = true;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res &= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void allTrueShort64VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = new boolean[mask.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
              Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromArray(mask, i);
              r[i] = vmask.allTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Short64VectorTests::allTrue);
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void lessThanShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Short, Shapes.S64Bit> mv = av.lessThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void greaterThanShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Short, Shapes.S64Bit> mv = av.greaterThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] > b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void equalShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Short, Shapes.S64Bit> mv = av.equal(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void notEqualShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Short, Shapes.S64Bit> mv = av.notEqual(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] != b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void lessThanEqShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Short, Shapes.S64Bit> mv = av.lessThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] <= b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void greaterThanEqShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Short, Shapes.S64Bit> mv = av.greaterThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] >= b[i + j]);
                }
            }
        }
    }


    static short blend(short a, short b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void blendShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = new short[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                ShortVector<Shapes.S64Bit> bv = SPECIES.fromArray(b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Short64VectorTests::blend);
    }



















    static short neg(short a) {
        return (short)(-((short)a));
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void negShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.neg().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Short64VectorTests::neg);
    }

    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void negMaskedShort64VectorTests(IntFunction<short[]> fa,
                                                IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.neg(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Short64VectorTests::neg);
    }

    static short abs(short a) {
        return (short)(Math.abs((short)a));
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void absShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Short64VectorTests::abs);
    }

    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void absMaskedShort64VectorTests(IntFunction<short[]> fa,
                                                IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.abs(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Short64VectorTests::abs);
    }


    static short not(short a) {
        return (short)(~((short)a));
    }



    @Test(dataProvider = "shortUnaryOpProvider")
    static void notShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.not().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Short64VectorTests::not);
    }



    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void notMaskedShort64VectorTests(IntFunction<short[]> fa,
                                                IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Short, Shapes.S64Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector<Shapes.S64Bit> av = SPECIES.fromArray(a, i);
                av.not(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Short64VectorTests::not);
    }




}

