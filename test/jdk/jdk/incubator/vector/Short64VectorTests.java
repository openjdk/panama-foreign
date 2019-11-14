/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
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
 * @run testng/othervm -ea -esa -Xbatch Short64VectorTests
 */

// -- This file was mechanically generated: Do not edit! -- //

import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.ShortVector;

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
public class Short64VectorTests extends AbstractVectorTest {

    static final VectorSpecies<Short> SPECIES =
                ShortVector.SPECIES_64;

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);


    static final int BUFFER_REPS = Integer.getInteger("jdk.incubator.vector.test.buffer-vectors", 25000 / 64);

    static final int BUFFER_SIZE = Integer.getInteger("jdk.incubator.vector.test.buffer-size", BUFFER_REPS * (64 / 8));

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

    interface FUnArrayOp {
        short[] apply(short a);
    }

    static void assertArraysEquals(short[] a, short[] r, FUnArrayOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a[i]));
            }
        } catch (AssertionError e) {
            short[] ref = f.apply(a[i]);
            short[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(ref, res, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i);
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
                Assert.assertEquals(b[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(b[i], f.apply(a, i), "at index #" + i);
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

    static void assertBroadcastArraysEquals(short[]a, short[]r) {
        int i = 0;
        for (; i < a.length; i += SPECIES.length()) {
            int idx = i;
            for (int j = idx; j < (idx + SPECIES.length()); j++)
                a[j]=a[idx];
        }

        try {
            for (i = 0; i < a.length; i++) {
                Assert.assertEquals(r[i], a[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], a[i], "at index #" + i + ", input = " + a[i]);
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
            Assert.assertEquals(r[i], f.apply(a[i], b[i]), "(" + a[i] + ", " + b[i] + ") at index #" + i);
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
                    Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j]));
                }
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j]), "at index #" + i + ", " + j);
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

    interface FTernOp {
        short apply(short a, short b, short c);
    }

    interface FTernMaskOp {
        short apply(short a, short b, short c, boolean m);

        static FTernMaskOp lift(FTernOp f) {
            return (a, b, c, m) -> m ? f.apply(a, b, c) : a;
        }
    }

    static void assertArraysEquals(short[] a, short[] b, short[] c, short[] r, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " + c[i]);
        }
    }

    static void assertArraysEquals(short[] a, short[] b, short[] c, short[] r, boolean[] mask, FTernOp f) {
        assertArraysEquals(a, b, c, r, mask, FTernMaskOp.lift(f));
    }

    static void assertArraysEquals(short[] a, short[] b, short[] c, short[] r, boolean[] mask, FTernMaskOp f) {
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
        short apply(short[] a, int b);
    }

    static void assertArraysEquals(short[] a, short[] r, FBinArrayOp f) {
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
        short[] apply(short[] a, int ix, int[] b, int iy);
    }

    static void assertArraysEquals(short[] a, int[] b, short[] r, FGatherScatterOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, i, b, i));
            }
        } catch (AssertionError e) {
            short[] ref = f.apply(a, i, b, i);
            short[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + " at index #" + i);
        }
    }

    interface FGatherMaskedOp {
        short[] apply(short[] a, int ix, boolean[] mask, int[] b, int iy);
    }

    interface FScatterMaskedOp {
        short[] apply(short[] r, short[] a, int ix, boolean[] mask, int[] b, int iy);
    }

    static void assertArraysEquals(short[] a, int[] b, short[] r, boolean[] mask, FGatherMaskedOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, i, mask, b, i));
            }
        } catch (AssertionError e) {
            short[] ref = f.apply(a, i, mask, b, i);
            short[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
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

    static void assertArraysEquals(short[] a, int[] b, short[] r, boolean[] mask, FScatterMaskedOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(r, a, i, mask, b, i));
            }
        } catch (AssertionError e) {
            short[] ref = f.apply(r, a, i, mask, b, i);
            short[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
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

    interface FLaneOp {
        short[] apply(short[] a, int origin, int idx);
    }

    static void assertArraysEquals(short[] a, short[] r, int origin, FLaneOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, origin, i));
            }
        } catch (AssertionError e) {
            short[] ref = f.apply(a, origin, i);
            short[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(ref, res, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i);
        }
    }

    interface FLaneBop {
        short[] apply(short[] a, short[] b, int origin, int idx);
    }

    static void assertArraysEquals(short[] a, short[] b, short[] r, int origin, FLaneBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, i));
            }
        } catch (AssertionError e) {
            short[] ref = f.apply(a, b, origin, i);
            short[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(ref, res, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin);
        }
    }

    interface FLaneMaskedBop {
        short[] apply(short[] a, short[] b, int origin, boolean[] mask, int idx);
    }

    static void assertArraysEquals(short[] a, short[] b, short[] r, int origin, boolean[] mask, FLaneMaskedBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, mask, i));
            }
        } catch (AssertionError e) {
            short[] ref = f.apply(a, b, origin, mask, i);
            short[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(ref, res, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin);
        }
    }

    interface FLanePartBop {
        short[] apply(short[] a, short[] b, int origin, int part, int idx);
    }

    static void assertArraysEquals(short[] a, short[] b, short[] r, int origin, int part, FLanePartBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, part, i));
            }
        } catch (AssertionError e) {
            short[] ref = f.apply(a, b, origin, part, i);
            short[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(ref, res, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin
              + ", with part #" + part);
        }
    }

    interface FLanePartMaskedBop {
        short[] apply(short[] a, short[] b, int origin, int part, boolean[] mask, int idx);
    }

    static void assertArraysEquals(short[] a, short[] b, short[] r, int origin, int part, boolean[] mask, FLanePartMaskedBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, part, mask, i));
            }
        } catch (AssertionError e) {
            short[] ref = f.apply(a, b, origin, part, mask, i);
            short[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(ref, res, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin
              + ", with part #" + part);
        }
    }

    static short bits(short e) {
        return  e;
    }

    static final List<IntFunction<short[]>> SHORT_GENERATORS = List.of(
            withToString("short[-i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (short)(-i * 5));
            }),
            withToString("short[i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (short)(i * 5));
            }),
            withToString("short[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (((short)(i + 1) == 0) ? 1 : (short)(i + 1)));
            }),
            withToString("short[cornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
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

    static final List<List<IntFunction<short[]>>> SHORT_GENERATOR_TRIPLES =
        SHORT_GENERATOR_PAIRS.stream().
                flatMap(pair -> SHORT_GENERATORS.stream().map(f -> List.of(pair.get(0), pair.get(1), f))).
                collect(Collectors.toList());

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
    public Object[][] shortTernaryOpProvider() {
        return SHORT_GENERATOR_TRIPLES.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] shortTernaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> SHORT_GENERATOR_TRIPLES.stream().map(lfa -> {
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

    @DataProvider
    public Object[][] shortUnaryMaskedOpIndexProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
          flatMap(fs -> INT_INDEX_GENERATORS.stream().flatMap(fm ->
            SHORT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm, fs};
            }))).
            toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] scatterMaskedOpIndexProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
          flatMap(fs -> INT_INDEX_GENERATORS.stream().flatMap(fm ->
            SHORT_GENERATORS.stream().flatMap(fn ->
              SHORT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fn, fm, fs};
            })))).
            toArray(Object[][]::new);
    }

    static final List<IntFunction<short[]>> SHORT_COMPARE_GENERATORS = List.of(
            withToString("short[i]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (short)i);
            }),
            withToString("short[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (short)(i + 1));
            }),
            withToString("short[i - 2]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (short)(i - 2));
            }),
            withToString("short[zigZag(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> i%3 == 0 ? (short)i : (i%3 == 1 ? (short)(i + 1) : (short)(i - 2)));
            }),
            withToString("short[cornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> cornerCaseValue(i));
            })
    );

    static final List<List<IntFunction<short[]>>> SHORT_TEST_GENERATOR_ARGS =
        SHORT_COMPARE_GENERATORS.stream().
                map(fa -> List.of(fa)).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] shortTestOpProvider() {
        return SHORT_TEST_GENERATOR_ARGS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

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
        int length = BUFFER_REPS * vl;
        return new short[length];
    };

    static final IntFunction<boolean[]> fmr = (vl) -> {
        int length = BUFFER_REPS * vl;
        return new boolean[length];
    };

    @Test
    static void smokeTest1() {
        ShortVector three = ShortVector.broadcast(SPECIES, (byte)-3);
        ShortVector three2 = (ShortVector) SPECIES.broadcast(-3);
        assert(three.eq(three2).allTrue());
        ShortVector three3 = three2.broadcast(1).broadcast(-3);
        assert(three.eq(three3).allTrue());
        int scale = 2;
        Class<?> ETYPE = short.class;
        if (ETYPE == double.class || ETYPE == long.class)
            scale = 1000000;
        else if (ETYPE == byte.class && SPECIES.length() >= 64)
            scale = 1;
        ShortVector higher = three.addIndex(scale);
        VectorMask<Short> m = three.compare(VectorOperators.LE, higher);
        assert(m.allTrue());
        m = higher.min((short)-1).test(VectorOperators.IS_NEGATIVE);
        assert(m.allTrue());
        short max = higher.reduceLanes(VectorOperators.MAX);
        assert(max == -3 + scale * (SPECIES.length()-1));
    }

    private static short[]
    bothToArray(ShortVector a, ShortVector b) {
        short[] r = new short[a.length() + b.length()];
        a.intoArray(r, 0);
        b.intoArray(r, a.length());
        return r;
    }

    @Test
    static void smokeTest2() {
        // Do some zipping and shuffling.
        ShortVector io = (ShortVector) SPECIES.broadcast(0).addIndex(1);
        ShortVector io2 = (ShortVector) VectorShuffle.iota(SPECIES,0,1,false).toVector();
        Assert.assertEquals(io, io2);
        ShortVector a = io.add((short)1); //[1,2]
        ShortVector b = a.neg();  //[-1,-2]
        short[] abValues = bothToArray(a,b); //[1,2,-1,-2]
        VectorShuffle<Short> zip0 = VectorShuffle.makeZip(SPECIES, 0);
        VectorShuffle<Short> zip1 = VectorShuffle.makeZip(SPECIES, 1);
        ShortVector zab0 = a.rearrange(zip0,b); //[1,-1]
        ShortVector zab1 = a.rearrange(zip1,b); //[2,-2]
        short[] zabValues = bothToArray(zab0, zab1); //[1,-1,2,-2]
        // manually zip
        short[] manual = new short[zabValues.length];
        for (int i = 0; i < manual.length; i += 2) {
            manual[i+0] = abValues[i/2];
            manual[i+1] = abValues[a.length() + i/2];
        }
        Assert.assertEquals(Arrays.toString(zabValues), Arrays.toString(manual));
        VectorShuffle<Short> unz0 = VectorShuffle.makeUnzip(SPECIES, 0);
        VectorShuffle<Short> unz1 = VectorShuffle.makeUnzip(SPECIES, 1);
        ShortVector uab0 = zab0.rearrange(unz0,zab1);
        ShortVector uab1 = zab0.rearrange(unz1,zab1);
        short[] abValues1 = bothToArray(uab0, uab1);
        Assert.assertEquals(Arrays.toString(abValues), Arrays.toString(abValues1));
    }

    static void iotaShuffle() {
        ShortVector io = (ShortVector) SPECIES.broadcast(0).addIndex(1);
        ShortVector io2 = (ShortVector) VectorShuffle.iota(SPECIES, 0 , 1, false).toVector();
        Assert.assertEquals(io, io2);
    }

    @Test
    // Test all shuffle related operations.
    static void shuffleTest() {
        // To test backend instructions, make sure that C2 is used.
        for (int loop = 0; loop < INVOC_COUNT * INVOC_COUNT; loop++) {
            iotaShuffle();
        }
    }

    static short ADD(short a, short b) {
        return (short)(a + b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void ADDShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::ADD);
    }
    static short add(short a, short b) {
        return (short)(a + b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void addShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
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

        assertArraysEquals(a, b, r, Short64VectorTests::add);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void ADDShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::ADD);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void addShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.add(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::add);
    }
    static short SUB(short a, short b) {
        return (short)(a - b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void SUBShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::SUB);
    }
    static short sub(short a, short b) {
        return (short)(a - b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void subShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
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

        assertArraysEquals(a, b, r, Short64VectorTests::sub);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void SUBShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::SUB);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void subShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.sub(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::sub);
    }
    static short MUL(short a, short b) {
        return (short)(a * b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void MULShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::MUL);
    }
    static short mul(short a, short b) {
        return (short)(a * b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void mulShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
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

        assertArraysEquals(a, b, r, Short64VectorTests::mul);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void MULShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::MUL);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void mulShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.mul(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::mul);
    }


    static short FIRST_NONZERO(short a, short b) {
        return (short)((a)!=0?a:b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void FIRST_NONZEROShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::FIRST_NONZERO);
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void FIRST_NONZEROShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::FIRST_NONZERO);
    }

    static short AND(short a, short b) {
        return (short)(a & b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void ANDShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::AND);
    }
    static short and(short a, short b) {
        return (short)(a & b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void andShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
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

        assertArraysEquals(a, b, r, Short64VectorTests::and);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void ANDShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::AND);
    }


    static short AND_NOT(short a, short b) {
        return (short)(a & ~b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void AND_NOTShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND_NOT, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::AND_NOT);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void AND_NOTShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND_NOT, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::AND_NOT);
    }


    static short OR(short a, short b) {
        return (short)(a | b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void ORShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.OR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::OR);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void ORShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.OR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::OR);
    }


    static short XOR(short a, short b) {
        return (short)(a ^ b);
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void XORShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.XOR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::XOR);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void XORShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.XOR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::XOR);
    }






    static short LSHL(short a, short b) {
        return (short)((a << (b & 0xF)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void LSHLShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::LSHL);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void LSHLShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::LSHL);
    }






    static short ASHR(short a, short b) {
        return (short)((a >> (b & 0xF)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void ASHRShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ASHR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::ASHR);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void ASHRShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ASHR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::ASHR);
    }






    static short LSHR(short a, short b) {
        return (short)(((a & 0xFFFF) >>> (b & 0xF)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void LSHRShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::LSHR);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void LSHRShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::LSHR);
    }






    static short LSHL_unary(short a, short b) {
        return (short)((a << (b & 15)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void LSHLShort64VectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Short64VectorTests::LSHL_unary);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void LSHLShort64VectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Short64VectorTests::LSHL_unary);
    }






    static short LSHR_unary(short a, short b) {
        return (short)(((a & 0xFFFF) >>> (b & 15)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void LSHRShort64VectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Short64VectorTests::LSHR_unary);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void LSHRShort64VectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Short64VectorTests::LSHR_unary);
    }






    static short ASHR_unary(short a, short b) {
        return (short)((a >> (b & 15)));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void ASHRShort64VectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Short64VectorTests::ASHR_unary);
    }



    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void ASHRShort64VectorTestsShift(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Short64VectorTests::ASHR_unary);
    }

    static short MIN(short a, short b) {
        return (short)(Math.min(a, b));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void MINShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MIN, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::MIN);
    }
    static short min(short a, short b) {
        return (short)(Math.min(a, b));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void minShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
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

        assertArraysEquals(a, b, r, Short64VectorTests::min);
    }
    static short MAX(short a, short b) {
        return (short)(Math.max(a, b));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void MAXShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MAX, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::MAX);
    }
    static short max(short a, short b) {
        return (short)(Math.max(a, b));
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void maxShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
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

        assertArraysEquals(a, b, r, Short64VectorTests::max);
    }

    static short AND(short[] a, int idx) {
        short res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }

    static short AND(short[] a) {
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
    static void ANDShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.AND);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra &= av.reduceLanes(VectorOperators.AND);
            }
        }

        assertReductionArraysEquals(a, r, ra, Short64VectorTests::AND, Short64VectorTests::AND);
    }


    static short ANDMasked(short[] a, int idx, boolean[] mask) {
        short res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res &= a[i];
        }

        return res;
    }

    static short ANDMasked(short[] a, boolean[] mask) {
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
    static void ANDShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        short ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.AND, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra &= av.reduceLanes(VectorOperators.AND, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Short64VectorTests::ANDMasked, Short64VectorTests::ANDMasked);
    }


    static short OR(short[] a, int idx) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }

    static short OR(short[] a) {
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
    static void ORShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.OR);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra |= av.reduceLanes(VectorOperators.OR);
            }
        }

        assertReductionArraysEquals(a, r, ra, Short64VectorTests::OR, Short64VectorTests::OR);
    }


    static short ORMasked(short[] a, int idx, boolean[] mask) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res |= a[i];
        }

        return res;
    }

    static short ORMasked(short[] a, boolean[] mask) {
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
    static void ORShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.OR, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra |= av.reduceLanes(VectorOperators.OR, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Short64VectorTests::ORMasked, Short64VectorTests::ORMasked);
    }


    static short XOR(short[] a, int idx) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res ^= a[i];
        }

        return res;
    }

    static short XOR(short[] a) {
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
    static void XORShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.XOR);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra ^= av.reduceLanes(VectorOperators.XOR);
            }
        }

        assertReductionArraysEquals(a, r, ra, Short64VectorTests::XOR, Short64VectorTests::XOR);
    }


    static short XORMasked(short[] a, int idx, boolean[] mask) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res ^= a[i];
        }

        return res;
    }

    static short XORMasked(short[] a, boolean[] mask) {
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
    static void XORShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.XOR, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra ^= av.reduceLanes(VectorOperators.XOR, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Short64VectorTests::XORMasked, Short64VectorTests::XORMasked);
    }

    static short ADD(short[] a, int idx) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res += a[i];
        }

        return res;
    }

    static short ADD(short[] a) {
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
    static void ADDShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.ADD);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD);
            }
        }

        assertReductionArraysEquals(a, r, ra, Short64VectorTests::ADD, Short64VectorTests::ADD);
    }
    static short ADDMasked(short[] a, int idx, boolean[] mask) {
        short res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res += a[i];
        }

        return res;
    }

    static short ADDMasked(short[] a, boolean[] mask) {
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
    static void ADDShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        short ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Short64VectorTests::ADDMasked, Short64VectorTests::ADDMasked);
    }
    static short MUL(short[] a, int idx) {
        short res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res *= a[i];
        }

        return res;
    }

    static short MUL(short[] a) {
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
    static void MULShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MUL);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL);
            }
        }

        assertReductionArraysEquals(a, r, ra, Short64VectorTests::MUL, Short64VectorTests::MUL);
    }
    static short MULMasked(short[] a, int idx, boolean[] mask) {
        short res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res *= a[i];
        }

        return res;
    }

    static short MULMasked(short[] a, boolean[] mask) {
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
    static void MULShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        short ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Short64VectorTests::MULMasked, Short64VectorTests::MULMasked);
    }
    static short MIN(short[] a, int idx) {
        short res = Short.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (short)Math.min(res, a[i]);
        }

        return res;
    }

    static short MIN(short[] a) {
        short res = Short.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (short)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void MINShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = Short.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MIN);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Short.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra = (short)Math.min(ra, av.reduceLanes(VectorOperators.MIN));
            }
        }

        assertReductionArraysEquals(a, r, ra, Short64VectorTests::MIN, Short64VectorTests::MIN);
    }
    static short MINMasked(short[] a, int idx, boolean[] mask) {
        short res = Short.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res = (short)Math.min(res, a[i]);
        }

        return res;
    }

    static short MINMasked(short[] a, boolean[] mask) {
        short res = Short.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            if(mask[i % SPECIES.length()])
                res = (short)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void MINShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        short ra = Short.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MIN, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Short.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra = (short)Math.min(ra, av.reduceLanes(VectorOperators.MIN, vmask));
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Short64VectorTests::MINMasked, Short64VectorTests::MINMasked);
    }
    static short MAX(short[] a, int idx) {
        short res = Short.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (short)Math.max(res, a[i]);
        }

        return res;
    }

    static short MAX(short[] a) {
        short res = Short.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (short)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpProvider")
    static void MAXShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        short ra = Short.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MAX);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Short.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra = (short)Math.max(ra, av.reduceLanes(VectorOperators.MAX));
            }
        }

        assertReductionArraysEquals(a, r, ra, Short64VectorTests::MAX, Short64VectorTests::MAX);
    }
    static short MAXMasked(short[] a, int idx, boolean[] mask) {
        short res = Short.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res = (short)Math.max(res, a[i]);
        }

        return res;
    }

    static short MAXMasked(short[] a, boolean[] mask) {
        short res = Short.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            if(mask[i % SPECIES.length()])
                res = (short)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void MAXShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        short ra = Short.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MAX, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Short.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ra = (short)Math.max(ra, av.reduceLanes(VectorOperators.MAX, vmask));
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Short64VectorTests::MAXMasked, Short64VectorTests::MAXMasked);
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
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, i);
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
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.allTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Short64VectorTests::allTrue);
    }


    @Test(dataProvider = "shortUnaryOpProvider")
    static void withShort64VectorTests(IntFunction<short []> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.withLane(0, (short)4).intoArray(r, i);
            }
        }

        assertInsertArraysEquals(a, r, (short)4, 0);
    }
    static boolean testIS_DEFAULT(short a) {
        return bits(a)==0;
    }

    @Test(dataProvider = "shortTestOpProvider")
    static void IS_DEFAULTShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                VectorMask<Short> mv = av.test(VectorOperators.IS_DEFAULT);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
   
                 Assert.assertEquals(mv.laneIsSet(j), testIS_DEFAULT(a[i + j]));
                }
            }
        }
    }

    static boolean testIS_NEGATIVE(short a) {
        return bits(a)<0;
    }

    @Test(dataProvider = "shortTestOpProvider")
    static void IS_NEGATIVEShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                VectorMask<Short> mv = av.test(VectorOperators.IS_NEGATIVE);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
   
                 Assert.assertEquals(mv.laneIsSet(j), testIS_NEGATIVE(a[i + j]));
                }
            }
        }
    }





    @Test(dataProvider = "shortCompareOpProvider")
    static void LTShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.compare(VectorOperators.LT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void ltShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.lt(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void GTShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.compare(VectorOperators.GT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] > b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void EQShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.compare(VectorOperators.EQ, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void eqShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.eq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void NEShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.compare(VectorOperators.NE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] != b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void LEShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.compare(VectorOperators.LE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] <= b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "shortCompareOpProvider")
    static void GEShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                VectorMask<Short> mv = av.compare(VectorOperators.GE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] >= b[i + j]);
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
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Short64VectorTests::blend);
    }

    @Test(dataProvider = "shortUnaryOpShuffleProvider")
    static void RearrangeShort64VectorTests(IntFunction<short[]> fa,
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
    static void getShort64VectorTests(IntFunction<short[]> fa) {
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

        assertArraysEquals(a, r, Short64VectorTests::get);
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void BroadcastShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector.broadcast(SPECIES, a[i]).intoArray(r, i);
            }
        }

        assertBroadcastArraysEquals(a, r);
    }





    @Test(dataProvider = "shortUnaryOpProvider")
    static void ZeroShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector.zero(SPECIES).intoArray(a, i);
            }
        }

        Assert.assertEquals(a, r);
    }




    static short[] sliceUnary(short[] a, int origin, int idx) {
        short[] res = new short[SPECIES.length()];
        for (int i = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = a[idx+i+origin];
            else
                res[i] = (short)0;
        }
        return res;
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void sliceUnaryShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.slice(origin).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, origin, Short64VectorTests::sliceUnary);
    }
    static short[] sliceBinary(short[] a, short[] b, int origin, int idx) {
        short[] res = new short[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = a[idx+i+origin];
            else {
                res[i] = b[idx+j];
                j++;
            }
        }
        return res;
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void sliceBinaryShort64VectorTestsBinary(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = new short[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.slice(origin, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, origin, Short64VectorTests::sliceBinary);
    }
    static short[] slice(short[] a, short[] b, int origin, boolean[] mask, int idx) {
        short[] res = new short[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = mask[i] ? a[idx+i+origin] : (short)0;
            else {
                res[i] = mask[i] ? b[idx+j] : (short)0;
                j++;
            }
        }
        return res;
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void sliceShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
    IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        short[] r = new short[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.slice(origin, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, origin, mask, Short64VectorTests::slice);
    }
    static short[] unsliceUnary(short[] a, int origin, int idx) {
        short[] res = new short[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i < origin)
                res[i] = (short)0;
            else {
                res[i] = a[idx+j];
                j++;
            }
        }
        return res;
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void unsliceUnaryShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = new short[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.unslice(origin).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, origin, Short64VectorTests::unsliceUnary);
    }
    static short[] unsliceBinary(short[] a, short[] b, int origin, int part, int idx) {
        short[] res = new short[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if (part == 0) {
                if (i < origin)
                    res[i] = b[idx+i];
                else {
                    res[i] = a[idx+j];
                    j++;
                }
            } else if (part == 1) {
                if (i < origin)
                    res[i] = a[idx+SPECIES.length()-origin+i];
                else {
                    res[i] = b[idx+origin+j];
                    j++;
                }
            }
        }
        return res;
    }

    @Test(dataProvider = "shortBinaryOpProvider")
    static void unsliceBinaryShort64VectorTestsBinary(IntFunction<short[]> fa, IntFunction<short[]> fb) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] r = new short[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        int part = (new java.util.Random()).nextInt(2);
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.unslice(origin, bv, part).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, origin, part, Short64VectorTests::unsliceBinary);
    }
    static short[] unslice(short[] a, short[] b, int origin, int part, boolean[] mask, int idx) {
        short[] res = new short[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = b[idx+i+origin];
            else {
                res[i] = b[idx+j];
                j++;
            }
        }
        for (int i = 0; i < SPECIES.length(); i++){
            res[i] = mask[i] ? a[idx+i] : res[i];
        }
        short[] res1 = new short[SPECIES.length()];
        if (part == 0) {
            for (int i = 0, j = 0; i < SPECIES.length(); i++){
                if (i < origin)
                    res1[i] = b[idx+i];
                else {
                   res1[i] = res[j];
                   j++;
                }
            }
        } else if (part == 1) {
            for (int i = 0, j = 0; i < SPECIES.length(); i++){
                if (i < origin)
                    res1[i] = res[SPECIES.length()-origin+i];
                else {
                    res1[i] = b[idx+origin+j];
                    j++;
                }
            }
        }
        return res1;
    }

    @Test(dataProvider = "shortBinaryOpMaskProvider")
    static void unsliceShort64VectorTestsMasked(IntFunction<short[]> fa, IntFunction<short[]> fb,
    IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        short[] r = new short[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        int part = (new java.util.Random()).nextInt(2);
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                av.unslice(origin, bv, part, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, origin, part, mask, Short64VectorTests::unslice);
    }






















    static short BITWISE_BLEND(short a, short b, short c) {
        return (short)((a&~(c))|(b&c));
    }


    @Test(dataProvider = "shortTernaryOpProvider")
    static void BITWISE_BLENDShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb, IntFunction<short[]> fc) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] c = fc.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                ShortVector cv = ShortVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, c, r, Short64VectorTests::BITWISE_BLEND);
    }


    @Test(dataProvider = "shortTernaryOpMaskProvider")
    static void BITWISE_BLENDShort64VectorTests(IntFunction<short[]> fa, IntFunction<short[]> fb,
                                          IntFunction<short[]> fc, IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] b = fb.apply(SPECIES.length());
        short[] c = fc.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                ShortVector bv = ShortVector.fromArray(SPECIES, b, i);
                ShortVector cv = ShortVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, c, r, mask, Short64VectorTests::BITWISE_BLEND);
    }


    static short NEG(short a) {
        return (short)(-((short)a));
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void NEGShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Short64VectorTests::NEG);
    }

    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void NEGMaskedShort64VectorTests(IntFunction<short[]> fa,
                                                IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Short64VectorTests::NEG);
    }

    static short ABS(short a) {
        return (short)(Math.abs((short)a));
    }

    static short abs(short a) {
        return (short)(Math.abs((short)a));
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void ABSShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Short64VectorTests::ABS);
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void absShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Short64VectorTests::abs);
    }

    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void ABSMaskedShort64VectorTests(IntFunction<short[]> fa,
                                                IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Short64VectorTests::ABS);
    }


    static short NOT(short a) {
        return (short)(~((short)a));
    }



    @Test(dataProvider = "shortUnaryOpProvider")
    static void NOTShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NOT).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Short64VectorTests::NOT);
    }



    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void NOTMaskedShort64VectorTests(IntFunction<short[]> fa,
                                                IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NOT, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Short64VectorTests::NOT);
    }



    static short ZOMO(short a) {
        return (short)((a==0?0:-1));
    }



    @Test(dataProvider = "shortUnaryOpProvider")
    static void ZOMOShort64VectorTests(IntFunction<short[]> fa) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Short64VectorTests::ZOMO);
    }



    @Test(dataProvider = "shortUnaryOpMaskProvider")
    static void ZOMOMaskedShort64VectorTests(IntFunction<short[]> fa,
                                                IntFunction<boolean[]> fm) {
        short[] a = fa.apply(SPECIES.length());
        short[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Short> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Short64VectorTests::ZOMO);
    }




    static short[] gather(short a[], int ix, int[] b, int iy) {
        short[] res = new short[SPECIES.length()];
        for (int i = 0; i < SPECIES.length(); i++) {
            int bi = iy + i;
            res[i] = a[b[bi] + ix];
        }
        return res;
    }

    @Test(dataProvider = "shortUnaryOpIndexProvider")
    static void gatherShort64VectorTests(IntFunction<short[]> fa, BiFunction<Integer,Integer,int[]> fs) {
        short[] a = fa.apply(SPECIES.length());
        int[] b    = fs.apply(a.length, SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i, b, i);
                av.intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::gather);
    }
    static short[] scatter(short a[], int ix, int[] b, int iy) {
      short[] res = new short[SPECIES.length()];
      for (int i = 0; i < SPECIES.length(); i++) {
        int bi = iy + i;
        res[b[bi]] = a[i + ix];
      }
      return res;
    }

    @Test(dataProvider = "shortUnaryOpIndexProvider")
    static void scatterShort64VectorTests(IntFunction<short[]> fa, BiFunction<Integer,Integer,int[]> fs) {
        short[] a = fa.apply(SPECIES.length());
        int[] b = fs.apply(a.length, SPECIES.length());
        short[] r = new short[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ShortVector av = ShortVector.fromArray(SPECIES, a, i);
                av.intoArray(r, i, b, i);
            }
        }

        assertArraysEquals(a, b, r, Short64VectorTests::scatter);
    }

}

