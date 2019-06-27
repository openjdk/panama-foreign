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
 * @run testng/othervm -ea -esa Int64VectorTests
 */

// -- This file was mechanically generated: Do not edit! -- //

import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.IntVector;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.Integer;
import java.util.List;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class Int64VectorTests extends AbstractVectorTest {

    static final VectorSpecies<Integer> SPECIES =
                IntVector.SPECIES_64;

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);


    static final int BUFFER_REPS = Integer.getInteger("jdk.incubator.vector.test.buffer-vectors", 25000 / 64);

    static final int BUFFER_SIZE = Integer.getInteger("jdk.incubator.vector.test.buffer-size", BUFFER_REPS * (64 / 8));

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

    interface FUnArrayOp {
        int[] apply(int a);
    }

    static void assertArraysEquals(int[] a, int[] r, FUnArrayOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a[i]));
            }
        } catch (AssertionError e) {
            int[] ref = f.apply(a[i]);
            int[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(ref, res, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i);
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

    interface FReductionAllOp {
        int apply(int[] a);
    }

    static void assertReductionArraysEquals(int[] a, int[] b, int c,
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
        int apply(int[] a, int idx, boolean[] mask);
    }

    interface FReductionAllMaskedOp {
        int apply(int[] a, boolean[] mask);
    }

    static void assertReductionArraysEqualsMasked(int[] a, int[] b, int c, boolean[] mask,
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
                Assert.assertEquals(b[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(b[i], f.apply(a, i), "at index #" + i);
        }
    }

    static void assertInsertArraysEquals(int[] a, int[] b, int element, int index) {
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

    static void assertRearrangeArraysEquals(int[] a, int[] r, int[] order, int vector_len) {
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
            Assert.assertEquals(r[i], f.apply(a[i], b[i]), "(" + a[i] + ", " + b[i] + ") at index #" + i);
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

    static void assertShiftArraysEquals(int[] a, int[] b, int[] r, FBinOp f) {
        int i = 0;
        int j = 0;
        try {
            for (; j < a.length; j += SPECIES.length()) {
                for (i = 0; i < SPECIES.length(); i++) {
                    Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j]));
                }
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j]), "at index #" + i + ", " + j);
        }
    }

    static void assertShiftArraysEquals(int[] a, int[] b, int[] r, boolean[] mask, FBinOp f) {
        assertShiftArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertShiftArraysEquals(int[] a, int[] b, int[] r, boolean[] mask, FBinMaskOp f) {
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

    interface FTernOp {
        int apply(int a, int b, int c);
    }

    interface FTernMaskOp {
        int apply(int a, int b, int c, boolean m);

        static FTernMaskOp lift(FTernOp f) {
            return (a, b, c, m) -> m ? f.apply(a, b, c) : a;
        }
    }

    static void assertArraysEquals(int[] a, int[] b, int[] c, int[] r, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " + c[i]);
        }
    }

    static void assertArraysEquals(int[] a, int[] b, int[] c, int[] r, boolean[] mask, FTernOp f) {
        assertArraysEquals(a, b, c, r, mask, FTernMaskOp.lift(f));
    }

    static void assertArraysEquals(int[] a, int[] b, int[] c, int[] r, boolean[] mask, FTernMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i], mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = "
              + b[i] + ", input3 = " + c[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }


    interface FBinArrayOp {
        int apply(int[] a, int b);
    }

    static void assertArraysEquals(int[] a, int[] r, FBinArrayOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a,i), "at index #" + i);
        }
    }

    interface FGatherScatterOp {
        int[] apply(int[] a, int ix, int[] b, int iy);
    }

    static void assertArraysEquals(int[] a, int[] b, int[] r, FGatherScatterOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, i, b, i));
            }
        } catch (AssertionError e) {
            int[] ref = f.apply(a, i, b, i);
            int[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + " at index #" + i);
        }
    }

    interface FGatherMaskedOp {
        int[] apply(int[] a, int ix, boolean[] mask, int[] b, int iy);
    }

    interface FScatterMaskedOp {
        int[] apply(int[] r, int[] a, int ix, boolean[] mask, int[] b, int iy);
    }

    static void assertArraysEquals(int[] a, int[] b, int[] r, boolean[] mask, FGatherMaskedOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, i, mask, b, i));
            }
        } catch (AssertionError e) {
            int[] ref = f.apply(a, i, mask, b, i);
            int[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(ref, res,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + ", mask: "
              + Arrays.toString(mask)
              + " at index #" + i);
        }
    }

    static void assertArraysEquals(int[] a, int[] b, int[] r, boolean[] mask, FScatterMaskedOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(r, a, i, mask, b, i));
            }
        } catch (AssertionError e) {
            int[] ref = f.apply(r, a, i, mask, b, i);
            int[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(ref, res,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + ", r: "
              + Arrays.toString(Arrays.copyOfRange(r, i, i+SPECIES.length()))
              + ", mask: "
              + Arrays.toString(mask)
              + " at index #" + i);
        }
    }

    static final List<IntFunction<int[]>> INT_GENERATORS = List.of(
            withToString("int[-i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (int)(-i * 5));
            }),
            withToString("int[i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (int)(i * 5));
            }),
            withToString("int[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (((int)(i + 1) == 0) ? 1 : (int)(i + 1)));
            }),
            withToString("int[cornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
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
    public Object[][] boolUnaryOpProvider() {
        return BOOL_ARRAY_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    static final List<List<IntFunction<int[]>>> INT_GENERATOR_TRIPLES =
        INT_GENERATOR_PAIRS.stream().
                flatMap(pair -> INT_GENERATORS.stream().map(f -> List.of(pair.get(0), pair.get(1), f))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] intBinaryOpProvider() {
        return INT_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intIndexedOpProvider() {
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
    public Object[][] intTernaryOpProvider() {
        return INT_GENERATOR_TRIPLES.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intTernaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> INT_GENERATOR_TRIPLES.stream().map(lfa -> {
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

    @DataProvider
    public Object[][] intUnaryOpShuffleProvider() {
        return INT_SHUFFLE_GENERATORS.stream().
                flatMap(fs -> INT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intUnaryOpIndexProvider() {
        return INT_INDEX_GENERATORS.stream().
                flatMap(fs -> INT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intUnaryMaskedOpIndexProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
          flatMap(fs -> INT_INDEX_GENERATORS.stream().flatMap(fm ->
            INT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm, fs};
            }))).
            toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] scatterMaskedOpIndexProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
          flatMap(fs -> INT_INDEX_GENERATORS.stream().flatMap(fm ->
            INT_GENERATORS.stream().flatMap(fn ->
              INT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fn, fm, fs};
            })))).
            toArray(Object[][]::new);
    }

    static final List<IntFunction<int[]>> INT_COMPARE_GENERATORS = List.of(
            withToString("int[i]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (int)i);
            }),
            withToString("int[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (int)(i + 1));
            }),
            withToString("int[i - 2]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (int)(i - 2));
            }),
            withToString("int[zigZag(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> i%3 == 0 ? (int)i : (i%3 == 1 ? (int)(i + 1) : (int)(i - 2)));
            }),
            withToString("int[cornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
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
   static int get(int[] a, int i) {
       return (int) a[i];
   }

   static final IntFunction<int[]> fr = (vl) -> {
        int length = BUFFER_REPS * vl;
        return new int[length];
    };

    static final IntFunction<boolean[]> fmr = (vl) -> {
        int length = BUFFER_REPS * vl;
        return new boolean[length];
    };

    @Test
    static void smokeTest1() {
        IntVector three = IntVector.broadcast(SPECIES, (byte)-3);
        IntVector three2 = (IntVector) SPECIES.broadcast(-3);
        assert(three.equal(three2).allTrue());
        IntVector three3 = three2.broadcast(1).broadcast(-3);
        assert(three.equal(three3).allTrue());
        int scale = 2;
        Class<?> ETYPE = int.class;
        if (ETYPE == double.class || ETYPE == long.class)
            scale = 1000000;
        else if (ETYPE == byte.class && SPECIES.length() >= 64)
            scale = 1;
        IntVector higher = three.addIndex(scale);
        VectorMask<Integer> m = three.compare(VectorOperators.LE, higher);
        assert(m.allTrue());
        int max = higher.maxLanes();
        assert(max == -3 + scale * (SPECIES.length()-1));
    }
    static int ADD(int a, int b) {
        return (int)(a + b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void ADDInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::ADD);
    }
    static int add(int a, int b) {
        return (int)(a + b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void addInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.add(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::add);
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void ADDInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::ADD);
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void addInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.add(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::add);
    }
    static int SUB(int a, int b) {
        return (int)(a - b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void SUBInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::SUB);
    }
    static int sub(int a, int b) {
        return (int)(a - b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void subInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.sub(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::sub);
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void SUBInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::SUB);
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void subInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.sub(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::sub);
    }
    static int MUL(int a, int b) {
        return (int)(a * b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void MULInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::MUL);
    }
    static int mul(int a, int b) {
        return (int)(a * b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void mulInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.mul(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::mul);
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void MULInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::MUL);
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void mulInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.mul(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::mul);
    }


    static int FIRST_NONZERO(int a, int b) {
        return (int)((a)!=0?a:b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void FIRST_NONZEROInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::FIRST_NONZERO);
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void FIRST_NONZEROInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::FIRST_NONZERO);
    }

    static int AND(int a, int b) {
        return (int)(a & b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void ANDInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::AND);
    }
    static int and(int a, int b) {
        return (int)(a & b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void andInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.and(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::and);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void ANDInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::AND);
    }


    static int ANDC2(int a, int b) {
        return (int)(a & ~b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void ANDC2Int64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ANDC2, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::ANDC2);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void ANDC2Int64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ANDC2, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::ANDC2);
    }


    static int OR(int a, int b) {
        return (int)(a | b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void ORInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.OR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::OR);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void ORInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.OR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::OR);
    }


    static int XOR(int a, int b) {
        return (int)(a ^ b);
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void XORInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.XOR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::XOR);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void XORInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.XOR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::XOR);
    }


    static int LSHL(int a, int b) {
        return (int)((a << b));
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void LSHLInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::LSHL);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void LSHLInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::LSHL);
    }


    static int ASHR(int a, int b) {
        return (int)((a >> b));
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void ASHRInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ASHR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::ASHR);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void ASHRInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ASHR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::ASHR);
    }


    static int LSHR(int a, int b) {
        return (int)((a >>> b));
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void LSHRInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::LSHR);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void LSHRInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::LSHR);
    }


    static int LSHL_unary(int a, int b) {
        return (int)((a << b));
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void LSHLInt64VectorTestsShift(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Int64VectorTests::LSHL_unary);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void LSHLInt64VectorTestsShift(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Int64VectorTests::LSHL_unary);
    }






    static int LSHR_unary(int a, int b) {
        return (int)((a >>> b));
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void LSHRInt64VectorTestsShift(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Int64VectorTests::LSHR_unary);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void LSHRInt64VectorTestsShift(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Int64VectorTests::LSHR_unary);
    }






    static int ASHR_unary(int a, int b) {
        return (int)((a >> b));
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void ASHRInt64VectorTestsShift(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Int64VectorTests::ASHR_unary);
    }



    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void ASHRInt64VectorTestsShift(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Int64VectorTests::ASHR_unary);
    }





    static int MIN(int a, int b) {
        return (int)(Math.min(a, b));
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void MINInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MIN, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::MIN);
    }
    static int min(int a, int b) {
        return (int)(Math.min(a, b));
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void minInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.min(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::min);
    }
    static int MAX(int a, int b) {
        return (int)(Math.max(a, b));
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void MAXInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MAX, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::MAX);
    }
    static int max(int a, int b) {
        return (int)(Math.max(a, b));
    }

    @Test(dataProvider = "intBinaryOpProvider")
    static void maxInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.max(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::max);
    }

    static int AND(int[] a, int idx) {
        int res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }

    static int AND(int[] a) {
        int res = -1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            int tmp = -1;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp &= a[i + j];
            }
            res &= tmp;
        }

        return res;
    }


    @Test(dataProvider = "intUnaryOpProvider")
    static void ANDInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        int ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.AND);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra &= av.reduceLanes(VectorOperators.AND);
            }
        }

        assertReductionArraysEquals(a, r, ra, Int64VectorTests::AND, Int64VectorTests::AND);
    }


    static int ANDMasked(int[] a, int idx, boolean[] mask) {
        int res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res &= a[i];
        }

        return res;
    }

    static int ANDMasked(int[] a, boolean[] mask) {
        int res = -1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            int tmp = -1;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp &= a[i + j];
            }
            res &= tmp;
        }

        return res;
    }


    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void ANDInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);
        int ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.AND, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra &= av.reduceLanes(VectorOperators.AND, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Int64VectorTests::ANDMasked, Int64VectorTests::ANDMasked);
    }


    static int OR(int[] a, int idx) {
        int res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }

    static int OR(int[] a) {
        int res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            int tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp |= a[i + j];
            }
            res |= tmp;
        }

        return res;
    }


    @Test(dataProvider = "intUnaryOpProvider")
    static void ORInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        int ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.OR);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra |= av.reduceLanes(VectorOperators.OR);
            }
        }

        assertReductionArraysEquals(a, r, ra, Int64VectorTests::OR, Int64VectorTests::OR);
    }


    static int ORMasked(int[] a, int idx, boolean[] mask) {
        int res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res |= a[i];
        }

        return res;
    }

    static int ORMasked(int[] a, boolean[] mask) {
        int res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            int tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp |= a[i + j];
            }
            res |= tmp;
        }

        return res;
    }


    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void ORInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);
        int ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.OR, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra |= av.reduceLanes(VectorOperators.OR, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Int64VectorTests::ORMasked, Int64VectorTests::ORMasked);
    }


    static int XOR(int[] a, int idx) {
        int res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res ^= a[i];
        }

        return res;
    }

    static int XOR(int[] a) {
        int res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            int tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp ^= a[i + j];
            }
            res ^= tmp;
        }

        return res;
    }


    @Test(dataProvider = "intUnaryOpProvider")
    static void XORInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        int ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.XOR);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra ^= av.reduceLanes(VectorOperators.XOR);
            }
        }

        assertReductionArraysEquals(a, r, ra, Int64VectorTests::XOR, Int64VectorTests::XOR);
    }


    static int XORMasked(int[] a, int idx, boolean[] mask) {
        int res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res ^= a[i];
        }

        return res;
    }

    static int XORMasked(int[] a, boolean[] mask) {
        int res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            int tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp ^= a[i + j];
            }
            res ^= tmp;
        }

        return res;
    }


    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void XORInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);
        int ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.XOR, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra ^= av.reduceLanes(VectorOperators.XOR, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Int64VectorTests::XORMasked, Int64VectorTests::XORMasked);
    }

    static int ADD(int[] a, int idx) {
        int res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res += a[i];
        }

        return res;
    }

    static int ADD(int[] a) {
        int res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            int tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp += a[i + j];
            }
            res += tmp;
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpProvider")
    static void ADDInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        int ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.ADD);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD);
            }
        }

        assertReductionArraysEquals(a, r, ra, Int64VectorTests::ADD, Int64VectorTests::ADD);
    }
    static int ADDMasked(int[] a, int idx, boolean[] mask) {
        int res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res += a[i];
        }

        return res;
    }

    static int ADDMasked(int[] a, boolean[] mask) {
        int res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            int tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp += a[i + j];
            }
            res += tmp;
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void ADDInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);
        int ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Int64VectorTests::ADDMasked, Int64VectorTests::ADDMasked);
    }
    static int MUL(int[] a, int idx) {
        int res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res *= a[i];
        }

        return res;
    }

    static int MUL(int[] a) {
        int res = 1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            int tmp = 1;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp *= a[i + j];
            }
            res *= tmp;
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpProvider")
    static void MULInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        int ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MUL);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL);
            }
        }

        assertReductionArraysEquals(a, r, ra, Int64VectorTests::MUL, Int64VectorTests::MUL);
    }
    static int MULMasked(int[] a, int idx, boolean[] mask) {
        int res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res *= a[i];
        }

        return res;
    }

    static int MULMasked(int[] a, boolean[] mask) {
        int res = 1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            int tmp = 1;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp *= a[i + j];
            }
            res *= tmp;
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void MULInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);
        int ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Int64VectorTests::MULMasked, Int64VectorTests::MULMasked);
    }
    static int MIN(int[] a, int idx) {
        int res = Integer.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (int)Math.min(res, a[i]);
        }

        return res;
    }

    static int MIN(int[] a) {
        int res = Integer.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (int)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpProvider")
    static void MINInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        int ra = Integer.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MIN);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Integer.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra = (int)Math.min(ra, av.reduceLanes(VectorOperators.MIN));
            }
        }

        assertReductionArraysEquals(a, r, ra, Int64VectorTests::MIN, Int64VectorTests::MIN);
    }
    static int MINMasked(int[] a, int idx, boolean[] mask) {
        int res = Integer.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res = (int)Math.min(res, a[i]);
        }

        return res;
    }

    static int MINMasked(int[] a, boolean[] mask) {
        int res = Integer.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            if(mask[i % SPECIES.length()])
                res = (int)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void MINInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);
        int ra = Integer.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MIN, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Integer.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra = (int)Math.min(ra, av.reduceLanes(VectorOperators.MIN, vmask));
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Int64VectorTests::MINMasked, Int64VectorTests::MINMasked);
    }
    static int MAX(int[] a, int idx) {
        int res = Integer.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (int)Math.max(res, a[i]);
        }

        return res;
    }

    static int MAX(int[] a) {
        int res = Integer.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (int)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpProvider")
    static void MAXInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        int ra = Integer.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MAX);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Integer.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra = (int)Math.max(ra, av.reduceLanes(VectorOperators.MAX));
            }
        }

        assertReductionArraysEquals(a, r, ra, Int64VectorTests::MAX, Int64VectorTests::MAX);
    }
    static int MAXMasked(int[] a, int idx, boolean[] mask) {
        int res = Integer.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res = (int)Math.max(res, a[i]);
        }

        return res;
    }

    static int MAXMasked(int[] a, boolean[] mask) {
        int res = Integer.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            if(mask[i % SPECIES.length()])
                res = (int)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void MAXInt64VectorTestsMasked(IntFunction<int[]> fa, IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);
        int ra = Integer.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MAX, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Integer.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                ra = (int)Math.max(ra, av.reduceLanes(VectorOperators.MAX, vmask));
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Int64VectorTests::MAXMasked, Int64VectorTests::MAXMasked);
    }

    static boolean anyTrue(boolean[] a, int idx) {
        boolean res = false;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void anyTrueInt64VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Integer> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.anyTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Int64VectorTests::anyTrue);
    }


    static boolean allTrue(boolean[] a, int idx) {
        boolean res = true;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void allTrueInt64VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Integer> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.allTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Int64VectorTests::allTrue);
    }


    @Test(dataProvider = "intUnaryOpProvider")
    static void withInt64VectorTests(IntFunction<int []> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.withLane(0, (int)4).intoArray(r, i);
            }
        }

        assertInsertArraysEquals(a, r, (int)4, 0);
    }

    @Test(dataProvider = "intCompareOpProvider")
    static void LTInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                VectorMask<Integer> mv = av.compare(VectorOperators.LT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void ltInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                VectorMask<Integer> mv = av.lt(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void GTInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                VectorMask<Integer> mv = av.compare(VectorOperators.GT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] > b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void EQInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                VectorMask<Integer> mv = av.compare(VectorOperators.EQ, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void eqInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                VectorMask<Integer> mv = av.eq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void NEInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                VectorMask<Integer> mv = av.compare(VectorOperators.NE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] != b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void LEInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                VectorMask<Integer> mv = av.compare(VectorOperators.LE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] <= b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "intCompareOpProvider")
    static void GEInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                VectorMask<Integer> mv = av.compare(VectorOperators.GE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] >= b[i + j]);
                }
            }
        }
    }


    static int blend(int a, int b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "intBinaryOpMaskProvider")
    static void blendInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Int64VectorTests::blend);
    }

    @Test(dataProvider = "intUnaryOpShuffleProvider")
    static void RearrangeInt64VectorTests(IntFunction<int[]> fa,
                                           BiFunction<Integer,Integer,int[]> fs) {
        int[] a = fa.apply(SPECIES.length());
        int[] order = fs.apply(a.length, SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.rearrange(VectorShuffle.fromArray(SPECIES, order, i)).intoArray(r, i);
            }
        }

        assertRearrangeArraysEquals(a, r, order, SPECIES.length());
    }




    @Test(dataProvider = "intUnaryOpProvider")
    static void getInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
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

        assertArraysEquals(a, r, Int64VectorTests::get);
    }
    static int[] single(int val) {
        int[] res = new int[SPECIES.length()];
        res[0] = val;

        return res;
    }

    @Test(dataProvider = "intUnaryOpProvider")
    static void singleInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.single(SPECIES, a[i]);
                av.intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Int64VectorTests::single);
    }






















    static int BITWISE_BLEND(int a, int b, int c) {
        return (int)((a&~(c))|(b&c));
    }


    @Test(dataProvider = "intTernaryOpProvider")
    static void BITWISE_BLENDInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb, IntFunction<int[]> fc) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] c = fc.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                IntVector cv = IntVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, c, r, Int64VectorTests::BITWISE_BLEND);
    }


    @Test(dataProvider = "intTernaryOpMaskProvider")
    static void BITWISE_BLENDInt64VectorTests(IntFunction<int[]> fa, IntFunction<int[]> fb,
                                          IntFunction<int[]> fc, IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fb.apply(SPECIES.length());
        int[] c = fc.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                IntVector bv = IntVector.fromArray(SPECIES, b, i);
                IntVector cv = IntVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, c, r, mask, Int64VectorTests::BITWISE_BLEND);
    }


    static int NEG(int a) {
        return (int)(-((int)a));
    }

    @Test(dataProvider = "intUnaryOpProvider")
    static void NEGInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Int64VectorTests::NEG);
    }

    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void NEGMaskedInt64VectorTests(IntFunction<int[]> fa,
                                                IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Int64VectorTests::NEG);
    }

    static int ABS(int a) {
        return (int)(Math.abs((int)a));
    }

    static int abs(int a) {
        return (int)(Math.abs((int)a));
    }

    @Test(dataProvider = "intUnaryOpProvider")
    static void ABSInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Int64VectorTests::ABS);
    }

    @Test(dataProvider = "intUnaryOpProvider")
    static void absInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Int64VectorTests::abs);
    }

    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void ABSMaskedInt64VectorTests(IntFunction<int[]> fa,
                                                IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Int64VectorTests::ABS);
    }


    static int NOT(int a) {
        return (int)(~((int)a));
    }



    @Test(dataProvider = "intUnaryOpProvider")
    static void NOTInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NOT).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Int64VectorTests::NOT);
    }



    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void NOTMaskedInt64VectorTests(IntFunction<int[]> fa,
                                                IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NOT, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Int64VectorTests::NOT);
    }



    static int ZOMO(int a) {
        return (int)((a==0?0:-1));
    }



    @Test(dataProvider = "intUnaryOpProvider")
    static void ZOMOInt64VectorTests(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Int64VectorTests::ZOMO);
    }



    @Test(dataProvider = "intUnaryOpMaskProvider")
    static void ZOMOMaskedInt64VectorTests(IntFunction<int[]> fa,
                                                IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Int64VectorTests::ZOMO);
    }




    static int[] gather(int a[], int ix, int[] b, int iy) {
        int[] res = new int[SPECIES.length()];
        for (int i = 0; i < SPECIES.length(); i++) {
            int bi = iy + i;
            res[i] = a[b[bi] + ix];
        }
        return res;
    }

    @Test(dataProvider = "intUnaryOpIndexProvider")
    static void gatherInt64VectorTests(IntFunction<int[]> fa, BiFunction<Integer,Integer,int[]> fs) {
        int[] a = fa.apply(SPECIES.length());
        int[] b    = fs.apply(a.length, SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i, b, i);
                av.intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::gather);
    }
    static int[] scatter(int a[], int ix, int[] b, int iy) {
      int[] res = new int[SPECIES.length()];
      for (int i = 0; i < SPECIES.length(); i++) {
        int bi = iy + i;
        res[b[bi]] = a[i + ix];
      }
      return res;
    }

    @Test(dataProvider = "intUnaryOpIndexProvider")
    static void scatterInt64VectorTests(IntFunction<int[]> fa, BiFunction<Integer,Integer,int[]> fs) {
        int[] a = fa.apply(SPECIES.length());
        int[] b = fs.apply(a.length, SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.intoArray(r, i, b, i);
            }
        }

        assertArraysEquals(a, b, r, Int64VectorTests::scatter);
    }

}

