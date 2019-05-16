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
 * @run testng/othervm -ea -esa ShortMaxVectorTests
 */

import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.ShortVector;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.Integer;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class ShortMaxVectorTests extends AbstractVectorTest {

    static final VectorSpecies<Short> SPECIES =
                ShortVector.SPECIES_MAX;

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);

    static VectorShape getMaxBit() {
        return VectorShape.S_Max_BIT;
    }

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

    interface FReductionAllOp {
        short apply(short[] a);
    }

    static void assertReductionArraysEquals(short[] a, short[] b, short c,
                                            FReductionOp f, FReductionAllOp fa) {
        int i = 0;
        try {
            Assert.assertEquals(c, fa.apply(a));
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(b[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(c, fa.apply(a), "Final result is incorrect!");
            Assert.assertEquals(b[i], f.apply(a, i), "at index #" + i);
        }
    }

    interface FReductionMaskedOp {
        short apply(short[] a, int idx, boolean[] mask);
    }

    interface FReductionAllMaskedOp {
        short apply(short[] a, boolean[] mask);
    }

    static void assertReductionArraysEqualsMasked(short[] a, short[] b, short c, boolean[] mask,
                                            FReductionMaskedOp f, FReductionAllMaskedOp fa) {
        int i = 0;
        try {
            Assert.assertEquals(c, fa.apply(a, mask));
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(b[i], f.apply(a, i, mask));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(c, fa.apply(a, mask), "Final result is incorrect!");
            Assert.assertEquals(b[i], f.apply(a, i, mask), "at index #" + i);
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

    static void assertInsertArraysEquals(short[] a, short[] b, short element, int index) {
        int i = 0;
        try {
            for (; i < a.length; i += 1) {
                if(i%SPECIES.length() == index) {
                    Assert.assertEquals(b[i], element);
                } else {
                    Assert.assertEquals(b[i], a[i]);
                }
            }
        } catch (AssertionError e) {
            if (i%SPECIES.length() == index) {
                Assert.assertEquals(b[i], element, "at index #" + i);
            } else {
                Assert.assertEquals(b[i], a[i], "at index #" + i);
            }
        }
    }

    static void assertRearrangeArraysEquals(short[] a, short[] r, int[] order, int vector_len) {
        int i = 0, j = 0;
        try {
            for (; i < a.length; i += vector_len) {
                for (j = 0; j < vector_len; j++) {
                    Assert.assertEquals(r[i+j], a[i+order[i+j]]);
                }
            }
        } catch (AssertionError e) {
            int idx = i + j;
            Assert.assertEquals(r[i+j], a[i+order[i+j]], "at index #" + idx + ", input = " + a[i+order[i+j]]);
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
            Assert.assertEquals(f.apply(a[i], b[i]), r[i], "(" + a[i] + ", " + b[i] + ") at index #" + i);
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

    static void assertShiftArraysEquals(short[] a, short[] b, short[] r, FBinOp f) {
        int i = 0;
        int j = 0;
        try {
            for (; j < a.length; j += SPECIES.length()) {
                for (i = 0; i < SPECIES.length(); i++) {
                    Assert.assertEquals(f.apply(a[i+j], b[j]), r[i+j]);
                }
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i+j], b[j]), r[i+j], "at index #" + i + ", " + j);
        }
    }

    static void assertShiftArraysEquals(short[] a, short[] b, short[] r, boolean[] mask, FBinOp f) {
        assertShiftArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertShiftArraysEquals(short[] a, short[] b, short[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        int j = 0;
        try {
            for (; j < a.length; j += SPECIES.length()) {
                for (i = 0; i < SPECIES.length(); i++) {
                    Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j], mask[i]));
                }
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j], mask[i]), "at index #" + i + ", input1 = " + a[i+j] + ", input2 = " + b[j] + ", mask = " + mask[i]);
        }
    }


    interface FBinArrayOp {
        short apply(short[] a, int b);
    }

    static void assertArraysEquals(short[] a, short[] r, FBinArrayOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a, i), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a,i), r[i], "at index #" + i);
        }
    }

    static final List<IntFunction<short[]>> SHORT_GENERATORS = List.of(
            withToString("short[-i * 5]", (int s) -> {
                return fill(s * 1000,
                            i -> (short)(-i * 5));
            }),
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
    public Object[][] shortIndexedOpProvider() {
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

    @DataProvider
    public Object[][] shortUnaryOpShuffleProvider() {
        return INT_SHUFFLE_GENERATORS.stream().
                flatMap(fs -> SHORT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] shortUnaryOpIndexProvider() {
        return INT_INDEX_GENERATORS.stream().
                flatMap(fs -> SHORT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
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
   static short get(short[] a, int i) {
       return (short) a[i];
   }

   static final IntFunction<short[]> fr = (vl) -> {
        int length = 1000 * vl;
        return new short[length];
    };

    static final IntFunction<boolean[]> fmr = (vl) -> {
        int length = 1000 * vl;
        return new boolean[length];
    };
    static short add(short a, short b) {
        return (short)(a + b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void addShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.add(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::add);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void addShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.add(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::add);
    }
    static short sub(short a, short b) {
        return (short)(a - b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void subShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.sub(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::sub);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void subShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.sub(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::sub);
    }


    static short mul(short a, short b) {
        return (short)(a * b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void mulShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.mul(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::mul);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void mulShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.mul(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::mul);
    }

    static short and(short a, short b) {
        return (short)(a & b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void andShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.and(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::and);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void andShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.and(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::and);
    }


    static short or(short a, short b) {
        return (short)(a | b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void orShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.or(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::or);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void orShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.or(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::or);
    }


    static short xor(short a, short b) {
        return (short)(a ^ b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void xorShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.xor(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::xor);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void xorShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.xor(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::xor);
    }






    static short shiftLeft(short a, short b) {
        return (short)((a << (b & 0xF)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void shiftLeftShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.shiftLeft(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::shiftLeft);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void shiftLeftShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.shiftLeft(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::shiftLeft);
    }






    static short shiftRight(short a, short b) {
        return (short)((a >>> (b & 0xF)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void shiftRightShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.shiftRight(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::shiftRight);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void shiftRightShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.shiftRight(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::shiftRight);
    }






    static short shiftArithmeticRight(short a, short b) {
        return (short)((a >> (b & 0xF)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void shiftArithmeticRightShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.shiftArithmeticRight(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::shiftArithmeticRight);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void shiftArithmeticRightShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.shiftArithmeticRight(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::shiftArithmeticRight);
    }






    static short shiftLeft_unary(short a, short b) {
        return (short)((a << (b & 15)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void shiftLeftShortMaxVectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.shiftLeft((int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, ShortMaxVectorTests::shiftLeft_unary);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void shiftLeftShortMaxVectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.shiftLeft((int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, ShortMaxVectorTests::shiftLeft_unary);
    }






    static short shiftRight_unary(short a, short b) {
        return (short)(((a & 0xFFFF) >>> (b & 15)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void shiftRightShortMaxVectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.shiftRight((int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, ShortMaxVectorTests::shiftRight_unary);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void shiftRightShortMaxVectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.shiftRight((int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, ShortMaxVectorTests::shiftRight_unary);
    }






    static short shiftArithmeticRight_unary(short a, short b) {
        return (short)((a >> (b & 15)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void shiftArithmeticRightShortMaxVectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.shiftArithmeticRight((int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, ShortMaxVectorTests::shiftArithmeticRight_unary);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void shiftArithmeticRightShortMaxVectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.shiftArithmeticRight((int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, ShortMaxVectorTests::shiftArithmeticRight_unary);
    }

    static short max(short a, short b) {
        return (short)(Math.max(a, b));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void maxShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.max(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::max);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void maxShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.max(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::max);
    }
    static short min(short a, short b) {
        return (short)(Math.min(a, b));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void minShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.min(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, ShortMaxVectorTests::min);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void minShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.min(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::min);
    }

    static short andLanes(short[] a, int idx) {
        short res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }

    static short andLanes(short[] a) {
        short res = -1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            short tmp = -1;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp &= a[i + j];
            }
            res &= tmp;
        }

        return res;
    }


    @Test(dataProvider = "shortUnaryOpProvider")
    static void andLanesShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.andLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra &= av.andLanes();
            }
        }

        assertReductionArraysEquals(a, r, ra, ShortMaxVectorTests::andLanes, ShortMaxVectorTests::andLanes);
    }


    static short andLanesMasked(short[] a, int idx, boolean[] mask) {
        short res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res &= a[i];
        }

        return res;
    }

    static short andLanesMasked(short[] a, boolean[] mask) {
        short res = -1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            short tmp = -1;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp &= a[i + j];
            }
            res &= tmp;
        }

        return res;
    }


    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void andLanesShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);
        short ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.andLanes(vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra &= av.andLanes(vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, ShortMaxVectorTests::andLanesMasked, ShortMaxVectorTests::andLanesMasked);
    }


    static short orLanes(short[] a, int idx) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }

    static short orLanes(short[] a) {
        short res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            short tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp |= a[i + j];
            }
            res |= tmp;
        }

        return res;
    }


    @Test(dataProvider = "shortUnaryOpProvider")
    static void orLanesShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.orLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra |= av.orLanes();
            }
        }

        assertReductionArraysEquals(a, r, ra, ShortMaxVectorTests::orLanes, ShortMaxVectorTests::orLanes);
    }


    static short orLanesMasked(short[] a, int idx, boolean[] mask) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res |= a[i];
        }

        return res;
    }

    static short orLanesMasked(short[] a, boolean[] mask) {
        short res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            short tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp |= a[i + j];
            }
            res |= tmp;
        }

        return res;
    }


    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void orLanesShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.orLanes(vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra |= av.orLanes(vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, ShortMaxVectorTests::orLanesMasked, ShortMaxVectorTests::orLanesMasked);
    }


    static short xorLanes(short[] a, int idx) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res ^= a[i];
        }

        return res;
    }

    static short xorLanes(short[] a) {
        short res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            short tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp ^= a[i + j];
            }
            res ^= tmp;
        }

        return res;
    }


    @Test(dataProvider = "shortUnaryOpProvider")
    static void xorLanesShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.xorLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra ^= av.xorLanes();
            }
        }

        assertReductionArraysEquals(a, r, ra, ShortMaxVectorTests::xorLanes, ShortMaxVectorTests::xorLanes);
    }


    static short xorLanesMasked(short[] a, int idx, boolean[] mask) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res ^= a[i];
        }

        return res;
    }

    static short xorLanesMasked(short[] a, boolean[] mask) {
        short res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            short tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp ^= a[i + j];
            }
            res ^= tmp;
        }

        return res;
    }


    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void xorLanesShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.xorLanes(vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra ^= av.xorLanes(vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, ShortMaxVectorTests::xorLanesMasked, ShortMaxVectorTests::xorLanesMasked);
    }

    static short addLanes(short[] a, int idx) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res += a[i];
        }

        return res;
    }

    static short addLanes(short[] a) {
        short res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            short tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp += a[i + j];
            }
            res += tmp;
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void addLanesShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.addLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra += av.addLanes();
            }
        }

        assertReductionArraysEquals(a, r, ra, ShortMaxVectorTests::addLanes, ShortMaxVectorTests::addLanes);
    }
    static short addLanesMasked(short[] a, int idx, boolean[] mask) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res += a[i];
        }

        return res;
    }

    static short addLanesMasked(short[] a, boolean[] mask) {
        short res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            short tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp += a[i + j];
            }
            res += tmp;
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void addLanesShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.addLanes(vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra += av.addLanes(vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, ShortMaxVectorTests::addLanesMasked, ShortMaxVectorTests::addLanesMasked);
    }
    static short mulLanes(short[] a, int idx) {
        short res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res *= a[i];
        }

        return res;
    }

    static short mulLanes(short[] a) {
        short res = 1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            short tmp = 1;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp *= a[i + j];
            }
            res *= tmp;
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void mulLanesShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.mulLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra *= av.mulLanes();
            }
        }

        assertReductionArraysEquals(a, r, ra, ShortMaxVectorTests::mulLanes, ShortMaxVectorTests::mulLanes);
    }
    static short mulLanesMasked(short[] a, int idx, boolean[] mask) {
        short res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res *= a[i];
        }

        return res;
    }

    static short mulLanesMasked(short[] a, boolean[] mask) {
        short res = 1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            short tmp = 1;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp *= a[i + j];
            }
            res *= tmp;
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void mulLanesShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);
        short ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.mulLanes(vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra *= av.mulLanes(vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, ShortMaxVectorTests::mulLanesMasked, ShortMaxVectorTests::mulLanesMasked);
    }
    static short minLanes(short[] a, int idx) {
        short res = Short.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (short)Math.min(res, a[i]);
        }

        return res;
    }

    static short minLanes(short[] a) {
        short res = Short.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (short)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void minLanesShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = Short.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.minLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Short.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra = (short)Math.min(ra, av.minLanes());
            }
        }

        assertReductionArraysEquals(a, r, ra, ShortMaxVectorTests::minLanes, ShortMaxVectorTests::minLanes);
    }
    static short minLanesMasked(short[] a, int idx, boolean[] mask) {
        short res = Short.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res = (short)Math.min(res, a[i]);
        }

        return res;
    }

    static short minLanesMasked(short[] a, boolean[] mask) {
        short res = Short.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            if(mask[i % SPECIES.length()])
                res = (short)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void minLanesShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);
        short ra = Short.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.minLanes(vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Short.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra = (short)Math.min(ra, av.minLanes(vmask));
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, ShortMaxVectorTests::minLanesMasked, ShortMaxVectorTests::minLanesMasked);
    }
    static short maxLanes(short[] a, int idx) {
        short res = Short.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (short)Math.max(res, a[i]);
        }

        return res;
    }

    static short maxLanes(short[] a) {
        short res = Short.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (short)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void maxLanesShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = Short.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.maxLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Short.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra = (short)Math.max(ra, av.maxLanes());
            }
        }

        assertReductionArraysEquals(a, r, ra, ShortMaxVectorTests::maxLanes, ShortMaxVectorTests::maxLanes);
    }
    static short maxLanesMasked(short[] a, int idx, boolean[] mask) {
        short res = Short.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res = (short)Math.max(res, a[i]);
        }

        return res;
    }

    static short maxLanesMasked(short[] a, boolean[] mask) {
        short res = Short.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            if(mask[i % SPECIES.length()])
                res = (short)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void maxLanesShortMaxVectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);
        short ra = Short.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.maxLanes(vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Short.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra = (short)Math.max(ra, av.maxLanes(vmask));
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, ShortMaxVectorTests::maxLanesMasked, ShortMaxVectorTests::maxLanesMasked);
    }

    static boolean anyTrue(boolean[] a, int idx) {
        boolean res = false;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void anyTrueShortMaxVectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.anyTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, ShortMaxVectorTests::anyTrue);
    }


    static boolean allTrue(boolean[] a, int idx) {
        boolean res = true;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void allTrueShortMaxVectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.allTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, ShortMaxVectorTests::allTrue);
    }


    @Test(dataProvider = "shortUnaryOpProvider")
    static void withShortMaxVectorTests(IntFunction<short []> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.with(0, (short)4).intoArray(r, i);
            }
        }

        assertInsertArraysEquals(a, r, (short)4, 0);
    }

    @Test(dataProvider = "shortCompareOpProvider")
    static void lessThanShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.lessThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void greaterThanShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.greaterThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] > b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void equalShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.equal(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void notEqualShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.notEqual(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] != b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void lessThanEqShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.lessThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] <= b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void greaterThanEqShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.greaterThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] >= b[i + j]);
                }
            }
        }
    }


    static short blend(short a, short b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void blendShortMaxVectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, ShortMaxVectorTests::blend);
    }

    @Test(dataProvider = "shortUnaryOpShuffleProvider")
    static void RearrangeShortMaxVectorTests(IntFunction<short[]> fa,
                                           BiFunction<Integer,Integer,int[]> fs) {
        short[] a = fa.apply(SPECIES.length());
        int[] order = fs.apply(a.length, SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.rearrange(VectorShuffle.fromArray(SPECIES, order, i)).intoArray(r, i);
            }
        }

        assertRearrangeArraysEquals(a, r, order, SPECIES.length());
    }




    @Test(dataProvider = "shortUnaryOpProvider")
    static void getShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                int num_lanes = SPECIES.length();
                // Manually unroll because full unroll happens after intrinsification.
                // Unroll is needed because get intrinsic requires for index to be a known constant.
                if (num_lanes == 1) {
                    r[i]=av.lane(0);
                } else if (num_lanes == 2) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                } else if (num_lanes == 4) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                } else if (num_lanes == 8) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                } else if (num_lanes == 16) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                } else if (num_lanes == 32) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                    r[i+16]=av.lane(16);
                    r[i+17]=av.lane(17);
                    r[i+18]=av.lane(18);
                    r[i+19]=av.lane(19);
                    r[i+20]=av.lane(20);
                    r[i+21]=av.lane(21);
                    r[i+22]=av.lane(22);
                    r[i+23]=av.lane(23);
                    r[i+24]=av.lane(24);
                    r[i+25]=av.lane(25);
                    r[i+26]=av.lane(26);
                    r[i+27]=av.lane(27);
                    r[i+28]=av.lane(28);
                    r[i+29]=av.lane(29);
                    r[i+30]=av.lane(30);
                    r[i+31]=av.lane(31);
                } else if (num_lanes == 64) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                    r[i+16]=av.lane(16);
                    r[i+17]=av.lane(17);
                    r[i+18]=av.lane(18);
                    r[i+19]=av.lane(19);
                    r[i+20]=av.lane(20);
                    r[i+21]=av.lane(21);
                    r[i+22]=av.lane(22);
                    r[i+23]=av.lane(23);
                    r[i+24]=av.lane(24);
                    r[i+25]=av.lane(25);
                    r[i+26]=av.lane(26);
                    r[i+27]=av.lane(27);
                    r[i+28]=av.lane(28);
                    r[i+29]=av.lane(29);
                    r[i+30]=av.lane(30);
                    r[i+31]=av.lane(31);
                    r[i+32]=av.lane(32);
                    r[i+33]=av.lane(33);
                    r[i+34]=av.lane(34);
                    r[i+35]=av.lane(35);
                    r[i+36]=av.lane(36);
                    r[i+37]=av.lane(37);
                    r[i+38]=av.lane(38);
                    r[i+39]=av.lane(39);
                    r[i+40]=av.lane(40);
                    r[i+41]=av.lane(41);
                    r[i+42]=av.lane(42);
                    r[i+43]=av.lane(43);
                    r[i+44]=av.lane(44);
                    r[i+45]=av.lane(45);
                    r[i+46]=av.lane(46);
                    r[i+47]=av.lane(47);
                    r[i+48]=av.lane(48);
                    r[i+49]=av.lane(49);
                    r[i+50]=av.lane(50);
                    r[i+51]=av.lane(51);
                    r[i+52]=av.lane(52);
                    r[i+53]=av.lane(53);
                    r[i+54]=av.lane(54);
                    r[i+55]=av.lane(55);
                    r[i+56]=av.lane(56);
                    r[i+57]=av.lane(57);
                    r[i+58]=av.lane(58);
                    r[i+59]=av.lane(59);
                    r[i+60]=av.lane(60);
                    r[i+61]=av.lane(61);
                    r[i+62]=av.lane(62);
                    r[i+63]=av.lane(63);
                } else {
                    for (int j = 0; j < SPECIES.length(); j++) {
                        r[i+j]=av.lane(j);
                    }
                }
            }
        }

        assertArraysEquals(a, r, ShortMaxVectorTests::get);
    }






















    static short neg(short a) {
        return (short)(-((short)a));
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void negShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.neg().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, ShortMaxVectorTests::neg);
    }

    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void negMaskedShortMaxVectorTests(IntFunction<short[]> fa,
                                                IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.neg(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, ShortMaxVectorTests::neg);
    }

    static short abs(short a) {
        return (short)(Math.abs((short)a));
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void absShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, ShortMaxVectorTests::abs);
    }

    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void absMaskedShortMaxVectorTests(IntFunction<short[]> fa,
                                                IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.abs(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, ShortMaxVectorTests::abs);
    }


    static short not(short a) {
        return (short)(~((short)a));
    }



    @Test(dataProvider = "shortUnaryOpProvider")
    static void notShortMaxVectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.not().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, ShortMaxVectorTests::not);
    }



    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void notMaskedShortMaxVectorTests(IntFunction<short[]> fa,
                                                IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.not(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, ShortMaxVectorTests::not);
    }






}

