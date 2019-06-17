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
 * @run testng/othervm -ea -esa Byte512VectorTests
 */

// -- This file was mechanically generated: Do not edit! -- //

import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.ByteVector;

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
public class Byte512VectorTests extends AbstractVectorTest {

    static final VectorSpecies<Byte> SPECIES =
                ByteVector.SPECIES_512;

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);


    static final int BUFFER_REPS = Integer.getInteger("jdk.incubator.vector.test.buffer-vectors", 25000 / 512);

    static final int BUFFER_SIZE = Integer.getInteger("jdk.incubator.vector.test.buffer-size", BUFFER_REPS * (512 / 8));

    interface FUnOp {
        byte apply(byte a);
    }

    static void assertArraysEquals(byte[] a, byte[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i]), "at index #" + i + ", input = " + a[i]);
        }
    }

    static void assertArraysEquals(byte[] a, byte[] r, boolean[] mask, FUnOp f) {
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
        byte apply(byte[] a, int idx);
    }

    interface FReductionAllOp {
        byte apply(byte[] a);
    }

    static void assertReductionArraysEquals(byte[] a, byte[] b, byte c,
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

    static void assertInsertArraysEquals(byte[] a, byte[] b, byte element, int index) {
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

    static void assertRearrangeArraysEquals(byte[] a, byte[] r, int[] order, int vector_len) {
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
        byte apply(byte a, byte b);
    }

    interface FBinMaskOp {
        byte apply(byte a, byte b, boolean m);

        static FBinMaskOp lift(FBinOp f) {
            return (a, b, m) -> m ? f.apply(a, b) : a;
        }
    }

    static void assertArraysEquals(byte[] a, byte[] b, byte[] r, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i]), "(" + a[i] + ", " + b[i] + ") at index #" + i);
        }
    }

    static void assertArraysEquals(byte[] a, byte[] b, byte[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(byte[] a, byte[] b, byte[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    static void assertShiftArraysEquals(byte[] a, byte[] b, byte[] r, FBinOp f) {
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

    static void assertShiftArraysEquals(byte[] a, byte[] b, byte[] r, boolean[] mask, FBinOp f) {
        assertShiftArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertShiftArraysEquals(byte[] a, byte[] b, byte[] r, boolean[] mask, FBinMaskOp f) {
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
        byte apply(byte a, byte b, byte c);
    }

    interface FTernMaskOp {
        byte apply(byte a, byte b, byte c, boolean m);

        static FTernMaskOp lift(FTernOp f) {
            return (a, b, c, m) -> m ? f.apply(a, b, c) : a;
        }
    }

    static void assertArraysEquals(byte[] a, byte[] b, byte[] c, byte[] r, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " + c[i]);
        }
    }

    static void assertArraysEquals(byte[] a, byte[] b, byte[] c, byte[] r, boolean[] mask, FTernOp f) {
        assertArraysEquals(a, b, c, r, mask, FTernMaskOp.lift(f));
    }

    static void assertArraysEquals(byte[] a, byte[] b, byte[] c, byte[] r, boolean[] mask, FTernMaskOp f) {
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
        byte apply(byte[] a, int b);
    }

    static void assertArraysEquals(byte[] a, byte[] r, FBinArrayOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a,i), "at index #" + i);
        }
    }

    static final List<IntFunction<byte[]>> BYTE_GENERATORS = List.of(
            withToString("byte[-i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (byte)(-i * 5));
            }),
            withToString("byte[i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (byte)(i * 5));
            }),
            withToString("byte[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (((byte)(i + 1) == 0) ? 1 : (byte)(i + 1)));
            }),
            withToString("byte[cornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> cornerCaseValue(i));
            })
    );

    // Create combinations of pairs
    // @@@ Might be sensitive to order e.g. div by 0
    static final List<List<IntFunction<byte[]>>> BYTE_GENERATOR_PAIRS =
        Stream.of(BYTE_GENERATORS.get(0)).
                flatMap(fa -> BYTE_GENERATORS.stream().skip(1).map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] boolUnaryOpProvider() {
        return BOOL_ARRAY_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    static final List<List<IntFunction<byte[]>>> BYTE_GENERATOR_TRIPLES =
        BYTE_GENERATOR_PAIRS.stream().
                flatMap(pair -> BYTE_GENERATORS.stream().map(f -> List.of(pair.get(0), pair.get(1), f))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] byteBinaryOpProvider() {
        return BYTE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] byteIndexedOpProvider() {
        return BYTE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] byteBinaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> BYTE_GENERATOR_PAIRS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] byteTernaryOpProvider() {
        return BYTE_GENERATOR_TRIPLES.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] byteTernaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> BYTE_GENERATOR_TRIPLES.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] byteUnaryOpProvider() {
        return BYTE_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] byteUnaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> BYTE_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] byteUnaryOpShuffleProvider() {
        return INT_SHUFFLE_GENERATORS.stream().
                flatMap(fs -> BYTE_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] byteUnaryOpIndexProvider() {
        return INT_INDEX_GENERATORS.stream().
                flatMap(fs -> BYTE_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
                })).
                toArray(Object[][]::new);
    }


    static final List<IntFunction<byte[]>> BYTE_COMPARE_GENERATORS = List.of(
            withToString("byte[i]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (byte)i);
            }),
            withToString("byte[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (byte)(i + 1));
            }),
            withToString("byte[i - 2]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (byte)(i - 2));
            }),
            withToString("byte[zigZag(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> i%3 == 0 ? (byte)i : (i%3 == 1 ? (byte)(i + 1) : (byte)(i - 2)));
            }),
            withToString("byte[cornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> cornerCaseValue(i));
            })
    );

    static final List<List<IntFunction<byte[]>>> BYTE_COMPARE_GENERATOR_PAIRS =
        BYTE_COMPARE_GENERATORS.stream().
                flatMap(fa -> BYTE_COMPARE_GENERATORS.stream().map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] byteCompareOpProvider() {
        return BYTE_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    interface ToByteF {
        byte apply(int i);
    }

    static byte[] fill(int s , ToByteF f) {
        return fill(new byte[s], f);
    }

    static byte[] fill(byte[] a, ToByteF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    static byte cornerCaseValue(int i) {
        switch(i % 5) {
            case 0:
                return Byte.MAX_VALUE;
            case 1:
                return Byte.MIN_VALUE;
            case 2:
                return Byte.MIN_VALUE;
            case 3:
                return Byte.MAX_VALUE;
            default:
                return (byte)0;
        }
    }
   static byte get(byte[] a, int i) {
       return (byte) a[i];
   }

   static final IntFunction<byte[]> fr = (vl) -> {
        int length = BUFFER_REPS * vl;
        return new byte[length];
    };

    static final IntFunction<boolean[]> fmr = (vl) -> {
        int length = BUFFER_REPS * vl;
        return new boolean[length];
    };

    @Test
    static void smokeTest1() {
        ByteVector three = ByteVector.broadcast(SPECIES, (byte)-3);
        ByteVector three2 = (ByteVector) SPECIES.broadcast(-3);
        assert(three.equal(three2).allTrue());
        ByteVector three3 = three2.broadcast(1).broadcast(-3);
        assert(three.equal(three3).allTrue());
        int scale = 2;
        Class<?> ETYPE = byte.class;
        if (ETYPE == double.class || ETYPE == long.class)
            scale = 1000000;
        else if (ETYPE == byte.class && SPECIES.length() >= 64)
            scale = 1;
        ByteVector higher = three.addIndex(scale);
        VectorMask<Byte> m = three.compare(VectorOperators.LE, higher);
        assert(m.allTrue());
        byte max = higher.maxLanes();
        assert(max == -3 + scale * (SPECIES.length()-1));
    }
    static byte add(byte a, byte b) {
        return (byte)(a + b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void addByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.add(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::add);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void addByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.add(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::add);
    }
    static byte sub(byte a, byte b) {
        return (byte)(a - b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void subByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.sub(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::sub);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void subByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.sub(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::sub);
    }


    static byte mul(byte a, byte b) {
        return (byte)(a * b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void mulByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.mul(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::mul);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void mulByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.mul(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::mul);
    }

    static byte and(byte a, byte b) {
        return (byte)(a & b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void andByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.and(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::and);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void andByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.and(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::and);
    }


    static byte lanewise_ANDC2(byte a, byte b) {
        return (byte)(a & ~b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void lanewise_ANDC2Byte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ANDC2, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::lanewise_ANDC2);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void lanewise_ANDC2Byte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ANDC2, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::lanewise_ANDC2);
    }


    static byte or(byte a, byte b) {
        return (byte)(a | b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void orByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.or(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::or);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void orByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.or(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::or);
    }


    static byte xor(byte a, byte b) {
        return (byte)(a ^ b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void xorByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.xor(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::xor);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void xorByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.xor(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::xor);
    }

    static byte lanewise_FIRST_NONZERO(byte a, byte b) {
        return (byte)((a)!=0?a:b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void lanewise_FIRST_NONZEROByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::lanewise_FIRST_NONZERO);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void lanewise_FIRST_NONZEROByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::lanewise_FIRST_NONZERO);
    }



    static byte shiftLeft(byte a, byte b) {
        return (byte)((a << (b & 0x7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void shiftLeftByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.shiftLeft(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::shiftLeft);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void shiftLeftByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.shiftLeft(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::shiftLeft);
    }






    static byte shiftRight(byte a, byte b) {
        return (byte)(((a & 0xFF) >>> (b & 0x7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void shiftRightByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.shiftRight(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::shiftRight);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void shiftRightByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.shiftRight(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::shiftRight);
    }






    static byte shiftArithmeticRight(byte a, byte b) {
        return (byte)((a >> (b & 0x7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void shiftArithmeticRightByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.shiftArithmeticRight(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::shiftArithmeticRight);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void shiftArithmeticRightByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.shiftArithmeticRight(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::shiftArithmeticRight);
    }






    static byte shiftLeft_unary(byte a, byte b) {
        return (byte)((a << (b & 7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void shiftLeftByte512VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.shiftLeft((int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Byte512VectorTests::shiftLeft_unary);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void shiftLeftByte512VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.shiftLeft((int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Byte512VectorTests::shiftLeft_unary);
    }






    static byte shiftRight_unary(byte a, byte b) {
        return (byte)(((a & 0xFF) >>> (b & 7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void shiftRightByte512VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.shiftRight((int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Byte512VectorTests::shiftRight_unary);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void shiftRightByte512VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.shiftRight((int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Byte512VectorTests::shiftRight_unary);
    }






    static byte shiftArithmeticRight_unary(byte a, byte b) {
        return (byte)((a >> (b & 7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void shiftArithmeticRightByte512VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.shiftArithmeticRight((int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Byte512VectorTests::shiftArithmeticRight_unary);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void shiftArithmeticRightByte512VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.shiftArithmeticRight((int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Byte512VectorTests::shiftArithmeticRight_unary);
    }



    static byte max(byte a, byte b) {
        return (byte)(Math.max(a, b));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void maxByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.max(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::max);
    }
    static byte min(byte a, byte b) {
        return (byte)(Math.min(a, b));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void minByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.min(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte512VectorTests::min);
    }

    static byte andLanes(byte[] a, int idx) {
        byte res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }

    static byte andLanes(byte[] a) {
        byte res = -1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            byte tmp = -1;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp &= a[i + j];
            }
            res &= tmp;
        }

        return res;
    }


    @Test(dataProvider = "byteUnaryOpProvider")
    static void andLanesByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.andLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra &= av.andLanes();
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte512VectorTests::andLanes, Byte512VectorTests::andLanes);
    }


    static byte orLanes(byte[] a, int idx) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }

    static byte orLanes(byte[] a) {
        byte res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            byte tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp |= a[i + j];
            }
            res |= tmp;
        }

        return res;
    }


    @Test(dataProvider = "byteUnaryOpProvider")
    static void orLanesByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.orLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra |= av.orLanes();
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte512VectorTests::orLanes, Byte512VectorTests::orLanes);
    }


    static byte xorLanes(byte[] a, int idx) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res ^= a[i];
        }

        return res;
    }

    static byte xorLanes(byte[] a) {
        byte res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            byte tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp ^= a[i + j];
            }
            res ^= tmp;
        }

        return res;
    }


    @Test(dataProvider = "byteUnaryOpProvider")
    static void xorLanesByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.xorLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra ^= av.xorLanes();
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte512VectorTests::xorLanes, Byte512VectorTests::xorLanes);
    }

    static byte addLanes(byte[] a, int idx) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res += a[i];
        }

        return res;
    }

    static byte addLanes(byte[] a) {
        byte res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            byte tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp += a[i + j];
            }
            res += tmp;
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void addLanesByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.addLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra += av.addLanes();
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte512VectorTests::addLanes, Byte512VectorTests::addLanes);
    }
    static byte mulLanes(byte[] a, int idx) {
        byte res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res *= a[i];
        }

        return res;
    }

    static byte mulLanes(byte[] a) {
        byte res = 1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            byte tmp = 1;
            for (int j = 0; j < SPECIES.length(); j++) {
                tmp *= a[i + j];
            }
            res *= tmp;
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void mulLanesByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.mulLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra *= av.mulLanes();
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte512VectorTests::mulLanes, Byte512VectorTests::mulLanes);
    }
    static byte minLanes(byte[] a, int idx) {
        byte res = Byte.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (byte)Math.min(res, a[i]);
        }

        return res;
    }

    static byte minLanes(byte[] a) {
        byte res = Byte.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (byte)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void minLanesByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = Byte.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.minLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Byte.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra = (byte)Math.min(ra, av.minLanes());
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte512VectorTests::minLanes, Byte512VectorTests::minLanes);
    }
    static byte maxLanes(byte[] a, int idx) {
        byte res = Byte.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (byte)Math.max(res, a[i]);
        }

        return res;
    }

    static byte maxLanes(byte[] a) {
        byte res = Byte.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (byte)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void maxLanesByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = Byte.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.maxLanes();
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Byte.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra = (byte)Math.max(ra, av.maxLanes());
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte512VectorTests::maxLanes, Byte512VectorTests::maxLanes);
    }

    static boolean anyTrue(boolean[] a, int idx) {
        boolean res = false;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void anyTrueByte512VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.anyTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Byte512VectorTests::anyTrue);
    }


    static boolean allTrue(boolean[] a, int idx) {
        boolean res = true;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void allTrueByte512VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.allTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Byte512VectorTests::allTrue);
    }


    @Test(dataProvider = "byteUnaryOpProvider")
    static void withByte512VectorTests(IntFunction<byte []> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.with(0, (byte)4).intoArray(r, i);
            }
        }

        assertInsertArraysEquals(a, r, (byte)4, 0);
    }

    @Test(dataProvider = "byteCompareOpProvider")
    static void lessThanByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.lessThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void greaterThanByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.greaterThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] > b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void equalByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.equal(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void notEqualByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.notEqual(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] != b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void lessThanEqByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.lessThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] <= b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void greaterThanEqByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.greaterThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.lane(j), a[i + j] >= b[i + j]);
                }
            }
        }
    }


    static byte blend(byte a, byte b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void blendByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte512VectorTests::blend);
    }

    @Test(dataProvider = "byteUnaryOpShuffleProvider")
    static void RearrangeByte512VectorTests(IntFunction<byte[]> fa,
                                           BiFunction<Integer,Integer,int[]> fs) {
        byte[] a = fa.apply(SPECIES.length());
        int[] order = fs.apply(a.length, SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.rearrange(VectorShuffle.fromArray(SPECIES, order, i)).intoArray(r, i);
            }
        }

        assertRearrangeArraysEquals(a, r, order, SPECIES.length());
    }




    @Test(dataProvider = "byteUnaryOpProvider")
    static void getByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
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

        assertArraysEquals(a, r, Byte512VectorTests::get);
    }






















    static byte lanewise_BITWISE_BLEND(byte a, byte b, byte c) {
        return (byte)((a&~(c))|(b&c));
    }


    @Test(dataProvider = "byteTernaryOpProvider")
    static void lanewise_BITWISE_BLENDByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb, IntFunction<byte[]> fc) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] c = fc.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                ByteVector cv = ByteVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, c, r, Byte512VectorTests::lanewise_BITWISE_BLEND);
    }


    @Test(dataProvider = "byteTernaryOpMaskProvider")
    static void lanewise_BITWISE_BLENDByte512VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<byte[]> fc, IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] c = fc.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                ByteVector cv = ByteVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, c, r, mask, Byte512VectorTests::lanewise_BITWISE_BLEND);
    }


    static byte neg(byte a) {
        return (byte)(-((byte)a));
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void negByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.neg().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte512VectorTests::neg);
    }

    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void negMaskedByte512VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.neg(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte512VectorTests::neg);
    }

    static byte abs(byte a) {
        return (byte)(Math.abs((byte)a));
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void absByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte512VectorTests::abs);
    }

    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void absMaskedByte512VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.abs(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte512VectorTests::abs);
    }


    static byte not(byte a) {
        return (byte)(~((byte)a));
    }



    @Test(dataProvider = "byteUnaryOpProvider")
    static void notByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.not().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte512VectorTests::not);
    }



    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void notMaskedByte512VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.not(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte512VectorTests::not);
    }



    static byte lanewise_ZOMO(byte a) {
        return (byte)((a==0?0:-1));
    }



    @Test(dataProvider = "byteUnaryOpProvider")
    static void lanewise_ZOMOByte512VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte512VectorTests::lanewise_ZOMO);
    }



    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void lanewise_ZOMOMaskedByte512VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte512VectorTests::lanewise_ZOMO);
    }






}

