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
 * @run testng/othervm -ea -esa Byte256VectorTests
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
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class Byte256VectorTests extends AbstractVectorTest {

    static final VectorSpecies<Byte> SPECIES =
                ByteVector.SPECIES_256;

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);


    static final int BUFFER_REPS = Integer.getInteger("jdk.incubator.vector.test.buffer-vectors", 25000 / 256);

    static final int BUFFER_SIZE = Integer.getInteger("jdk.incubator.vector.test.buffer-size", BUFFER_REPS * (256 / 8));

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

    interface FUnArrayOp {
        byte[] apply(byte a);
    }

    static void assertArraysEquals(byte[] a, byte[] r, FUnArrayOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a[i]));
            }
        } catch (AssertionError e) {
            byte[] ref = f.apply(a[i]);
            byte[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(ref, res, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i);
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

    interface FReductionMaskedOp {
        byte apply(byte[] a, int idx, boolean[] mask);
    }

    interface FReductionAllMaskedOp {
        byte apply(byte[] a, boolean[] mask);
    }

    static void assertReductionArraysEqualsMasked(byte[] a, byte[] b, byte c, boolean[] mask,
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

    static void assertBroadcastArraysEquals(byte[]a, byte[]r) {
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

    interface FGatherScatterOp {
        byte[] apply(byte[] a, int ix, int[] b, int iy);
    }

    static void assertArraysEquals(byte[] a, int[] b, byte[] r, FGatherScatterOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, i, b, i));
            }
        } catch (AssertionError e) {
            byte[] ref = f.apply(a, i, b, i);
            byte[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + " at index #" + i);
        }
    }

    interface FGatherMaskedOp {
        byte[] apply(byte[] a, int ix, boolean[] mask, int[] b, int iy);
    }

    interface FScatterMaskedOp {
        byte[] apply(byte[] r, byte[] a, int ix, boolean[] mask, int[] b, int iy);
    }

    static void assertArraysEquals(byte[] a, int[] b, byte[] r, boolean[] mask, FGatherMaskedOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, i, mask, b, i));
            }
        } catch (AssertionError e) {
            byte[] ref = f.apply(a, i, mask, b, i);
            byte[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
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

    static void assertArraysEquals(byte[] a, int[] b, byte[] r, boolean[] mask, FScatterMaskedOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(r, a, i, mask, b, i));
            }
        } catch (AssertionError e) {
            byte[] ref = f.apply(r, a, i, mask, b, i);
            byte[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
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

    static byte bits(byte e) {
        return  e;
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

    @DataProvider
    public Object[][] byteUnaryMaskedOpIndexProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
          flatMap(fs -> INT_INDEX_GENERATORS.stream().flatMap(fm ->
            BYTE_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm, fs};
            }))).
            toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] scatterMaskedOpIndexProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
          flatMap(fs -> INT_INDEX_GENERATORS.stream().flatMap(fm ->
            BYTE_GENERATORS.stream().flatMap(fn ->
              BYTE_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fn, fm, fs};
            })))).
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

    static final List<List<IntFunction<byte[]>>> BYTE_TEST_GENERATOR_ARGS =
        BYTE_COMPARE_GENERATORS.stream().
                map(fa -> List.of(fa)).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] byteTestOpProvider() {
        return BYTE_TEST_GENERATOR_ARGS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

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
        assert(three.eq(three2).allTrue());
        ByteVector three3 = three2.broadcast(1).broadcast(-3);
        assert(three.eq(three3).allTrue());
        int scale = 2;
        Class<?> ETYPE = byte.class;
        if (ETYPE == double.class || ETYPE == long.class)
            scale = 1000000;
        else if (ETYPE == byte.class && SPECIES.length() >= 64)
            scale = 1;
        ByteVector higher = three.addIndex(scale);
        VectorMask<Byte> m = three.compare(VectorOperators.LE, higher);
        assert(m.allTrue());
        m = higher.min((byte)-1).test(VectorOperators.IS_NEGATIVE);
        assert(m.allTrue());
        byte max = higher.reduceLanes(VectorOperators.MAX);
        assert(max == -3 + scale * (SPECIES.length()-1));
    }

    private static byte[]
    bothToArray(ByteVector a, ByteVector b) {
        byte[] r = new byte[a.length() + b.length()];
        a.intoArray(r, 0);
        b.intoArray(r, a.length());
        return r;
    }   

    @Test
    static void smokeTest2() {
        // Do some zipping and shuffling.
        ByteVector io = (ByteVector) SPECIES.broadcast(0).addIndex(1);
        ByteVector io2 = (ByteVector) VectorShuffle.iota(SPECIES,0,1,false).toVector();
        Assert.assertEquals(io, io2);
        ByteVector a = io.add((byte)1); //[1,2]
        ByteVector b = a.neg();  //[-1,-2]
        byte[] abValues = bothToArray(a,b); //[1,2,-1,-2]
        VectorShuffle<Byte> zip0 = VectorShuffle.makeZip(SPECIES, 0);
        VectorShuffle<Byte> zip1 = VectorShuffle.makeZip(SPECIES, 1);
        ByteVector zab0 = a.rearrange(zip0,b); //[1,-1]
        ByteVector zab1 = a.rearrange(zip1,b); //[2,-2]
        byte[] zabValues = bothToArray(zab0, zab1); //[1,-1,2,-2]
        // manually zip
        byte[] manual = new byte[zabValues.length];
        for (int i = 0; i < manual.length; i += 2) {
            manual[i+0] = abValues[i/2];
            manual[i+1] = abValues[a.length() + i/2];
        }
        Assert.assertEquals(Arrays.toString(zabValues), Arrays.toString(manual));
        VectorShuffle<Byte> unz0 = VectorShuffle.makeUnzip(SPECIES, 0);
        VectorShuffle<Byte> unz1 = VectorShuffle.makeUnzip(SPECIES, 1);
        ByteVector uab0 = zab0.rearrange(unz0,zab1);
        ByteVector uab1 = zab0.rearrange(unz1,zab1);
        byte[] abValues1 = bothToArray(uab0, uab1);
        Assert.assertEquals(Arrays.toString(abValues), Arrays.toString(abValues1));
    }
    static byte ADD(byte a, byte b) {
        return (byte)(a + b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void ADDByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::ADD);
    }
    static byte add(byte a, byte b) {
        return (byte)(a + b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void addByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
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

        assertArraysEquals(a, b, r, Byte256VectorTests::add);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void ADDByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::ADD);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void addByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.add(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::add);
    }
    static byte SUB(byte a, byte b) {
        return (byte)(a - b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void SUBByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::SUB);
    }
    static byte sub(byte a, byte b) {
        return (byte)(a - b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void subByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
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

        assertArraysEquals(a, b, r, Byte256VectorTests::sub);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void SUBByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::SUB);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void subByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.sub(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::sub);
    }
    static byte MUL(byte a, byte b) {
        return (byte)(a * b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void MULByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::MUL);
    }
    static byte mul(byte a, byte b) {
        return (byte)(a * b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void mulByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
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

        assertArraysEquals(a, b, r, Byte256VectorTests::mul);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void MULByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::MUL);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void mulByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.mul(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::mul);
    }


    static byte FIRST_NONZERO(byte a, byte b) {
        return (byte)((a)!=0?a:b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void FIRST_NONZEROByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
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

        assertArraysEquals(a, b, r, Byte256VectorTests::FIRST_NONZERO);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void FIRST_NONZEROByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::FIRST_NONZERO);
    }

    static byte AND(byte a, byte b) {
        return (byte)(a & b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void ANDByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::AND);
    }
    static byte and(byte a, byte b) {
        return (byte)(a & b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void andByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
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

        assertArraysEquals(a, b, r, Byte256VectorTests::and);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void ANDByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::AND);
    }


    static byte AND_NOT(byte a, byte b) {
        return (byte)(a & ~b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void AND_NOTByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND_NOT, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::AND_NOT);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void AND_NOTByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND_NOT, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::AND_NOT);
    }


    static byte OR(byte a, byte b) {
        return (byte)(a | b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void ORByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.OR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::OR);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void ORByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.OR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::OR);
    }


    static byte XOR(byte a, byte b) {
        return (byte)(a ^ b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void XORByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.XOR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::XOR);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void XORByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.XOR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::XOR);
    }




    static byte LSHL(byte a, byte b) {
        return (byte)((a << (b & 0x7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void LSHLByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::LSHL);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void LSHLByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::LSHL);
    }






    static byte ASHR(byte a, byte b) {
        return (byte)((a >> (b & 0x7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void ASHRByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ASHR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::ASHR);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void ASHRByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ASHR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::ASHR);
    }






    static byte LSHR(byte a, byte b) {
        return (byte)(((a & 0xFF) >>> (b & 0x7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void LSHRByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::LSHR);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void LSHRByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::LSHR);
    }






    static byte LSHL_unary(byte a, byte b) {
        return (byte)((a << (b & 7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void LSHLByte256VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Byte256VectorTests::LSHL_unary);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void LSHLByte256VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Byte256VectorTests::LSHL_unary);
    }






    static byte LSHR_unary(byte a, byte b) {
        return (byte)(((a & 0xFF) >>> (b & 7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void LSHRByte256VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Byte256VectorTests::LSHR_unary);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void LSHRByte256VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Byte256VectorTests::LSHR_unary);
    }






    static byte ASHR_unary(byte a, byte b) {
        return (byte)((a >> (b & 7)));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void ASHRByte256VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, Byte256VectorTests::ASHR_unary);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void ASHRByte256VectorTestsShift(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(a, b, r, mask, Byte256VectorTests::ASHR_unary);
    }



    static byte MIN(byte a, byte b) {
        return (byte)(Math.min(a, b));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void MINByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MIN, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::MIN);
    }
    static byte min(byte a, byte b) {
        return (byte)(Math.min(a, b));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void minByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
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

        assertArraysEquals(a, b, r, Byte256VectorTests::min);
    }
    static byte MAX(byte a, byte b) {
        return (byte)(Math.max(a, b));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void MAXByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MAX, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::MAX);
    }
    static byte max(byte a, byte b) {
        return (byte)(Math.max(a, b));
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void maxByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
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

        assertArraysEquals(a, b, r, Byte256VectorTests::max);
    }

    static byte AND(byte[] a, int idx) {
        byte res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }

    static byte AND(byte[] a) {
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
    static void ANDByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.AND);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra &= av.reduceLanes(VectorOperators.AND);
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte256VectorTests::AND, Byte256VectorTests::AND);
    }


    static byte ANDMasked(byte[] a, int idx, boolean[] mask) {
        byte res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res &= a[i];
        }

        return res;
    }

    static byte ANDMasked(byte[] a, boolean[] mask) {
        byte res = -1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            byte tmp = -1;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp &= a[i + j];
            }
            res &= tmp;
        }

        return res;
    }


    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void ANDByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        byte ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.AND, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra &= av.reduceLanes(VectorOperators.AND, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Byte256VectorTests::ANDMasked, Byte256VectorTests::ANDMasked);
    }


    static byte OR(byte[] a, int idx) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }

    static byte OR(byte[] a) {
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
    static void ORByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.OR);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra |= av.reduceLanes(VectorOperators.OR);
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte256VectorTests::OR, Byte256VectorTests::OR);
    }


    static byte ORMasked(byte[] a, int idx, boolean[] mask) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res |= a[i];
        }

        return res;
    }

    static byte ORMasked(byte[] a, boolean[] mask) {
        byte res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            byte tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp |= a[i + j];
            }
            res |= tmp;
        }

        return res;
    }


    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void ORByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        byte ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.OR, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra |= av.reduceLanes(VectorOperators.OR, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Byte256VectorTests::ORMasked, Byte256VectorTests::ORMasked);
    }


    static byte XOR(byte[] a, int idx) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res ^= a[i];
        }

        return res;
    }

    static byte XOR(byte[] a) {
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
    static void XORByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.XOR);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra ^= av.reduceLanes(VectorOperators.XOR);
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte256VectorTests::XOR, Byte256VectorTests::XOR);
    }


    static byte XORMasked(byte[] a, int idx, boolean[] mask) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res ^= a[i];
        }

        return res;
    }

    static byte XORMasked(byte[] a, boolean[] mask) {
        byte res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            byte tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp ^= a[i + j];
            }
            res ^= tmp;
        }

        return res;
    }


    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void XORByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        byte ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.XOR, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra ^= av.reduceLanes(VectorOperators.XOR, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Byte256VectorTests::XORMasked, Byte256VectorTests::XORMasked);
    }

    static byte ADD(byte[] a, int idx) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res += a[i];
        }

        return res;
    }

    static byte ADD(byte[] a) {
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
    static void ADDByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.ADD);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD);
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte256VectorTests::ADD, Byte256VectorTests::ADD);
    }
    static byte ADDMasked(byte[] a, int idx, boolean[] mask) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res += a[i];
        }

        return res;
    }

    static byte ADDMasked(byte[] a, boolean[] mask) {
        byte res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            byte tmp = 0;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp += a[i + j];
            }
            res += tmp;
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void ADDByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        byte ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Byte256VectorTests::ADDMasked, Byte256VectorTests::ADDMasked);
    }
    static byte MUL(byte[] a, int idx) {
        byte res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res *= a[i];
        }

        return res;
    }

    static byte MUL(byte[] a) {
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
    static void MULByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MUL);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL);
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte256VectorTests::MUL, Byte256VectorTests::MUL);
    }
    static byte MULMasked(byte[] a, int idx, boolean[] mask) {
        byte res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res *= a[i];
        }

        return res;
    }

    static byte MULMasked(byte[] a, boolean[] mask) {
        byte res = 1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            byte tmp = 1;
            for (int j = 0; j < SPECIES.length(); j++) {
                if(mask[(i + j) % SPECIES.length()])
                    tmp *= a[i + j];
            }
            res *= tmp;
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void MULByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        byte ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Byte256VectorTests::MULMasked, Byte256VectorTests::MULMasked);
    }
    static byte MIN(byte[] a, int idx) {
        byte res = Byte.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (byte)Math.min(res, a[i]);
        }

        return res;
    }

    static byte MIN(byte[] a) {
        byte res = Byte.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (byte)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void MINByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = Byte.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MIN);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Byte.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra = (byte)Math.min(ra, av.reduceLanes(VectorOperators.MIN));
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte256VectorTests::MIN, Byte256VectorTests::MIN);
    }
    static byte MINMasked(byte[] a, int idx, boolean[] mask) {
        byte res = Byte.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res = (byte)Math.min(res, a[i]);
        }

        return res;
    }

    static byte MINMasked(byte[] a, boolean[] mask) {
        byte res = Byte.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            if(mask[i % SPECIES.length()])
                res = (byte)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void MINByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        byte ra = Byte.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MIN, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Byte.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra = (byte)Math.min(ra, av.reduceLanes(VectorOperators.MIN, vmask));
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Byte256VectorTests::MINMasked, Byte256VectorTests::MINMasked);
    }
    static byte MAX(byte[] a, int idx) {
        byte res = Byte.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (byte)Math.max(res, a[i]);
        }

        return res;
    }

    static byte MAX(byte[] a) {
        byte res = Byte.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (byte)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void MAXByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        byte ra = Byte.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MAX);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Byte.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra = (byte)Math.max(ra, av.reduceLanes(VectorOperators.MAX));
            }
        }

        assertReductionArraysEquals(a, r, ra, Byte256VectorTests::MAX, Byte256VectorTests::MAX);
    }
    static byte MAXMasked(byte[] a, int idx, boolean[] mask) {
        byte res = Byte.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res = (byte)Math.max(res, a[i]);
        }

        return res;
    }

    static byte MAXMasked(byte[] a, boolean[] mask) {
        byte res = Byte.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            if(mask[i % SPECIES.length()])
                res = (byte)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void MAXByte256VectorTestsMasked(IntFunction<byte[]> fa, IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        byte ra = Byte.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MAX, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Byte.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ra = (byte)Math.max(ra, av.reduceLanes(VectorOperators.MAX, vmask));
            }
        }

        assertReductionArraysEqualsMasked(a, r, ra, mask, Byte256VectorTests::MAXMasked, Byte256VectorTests::MAXMasked);
    }

    static boolean anyTrue(boolean[] a, int idx) {
        boolean res = false;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void anyTrueByte256VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.anyTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Byte256VectorTests::anyTrue);
    }


    static boolean allTrue(boolean[] a, int idx) {
        boolean res = true;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void allTrueByte256VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.allTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Byte256VectorTests::allTrue);
    }


    @Test(dataProvider = "byteUnaryOpProvider")
    static void withByte256VectorTests(IntFunction<byte []> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.withLane(0, (byte)4).intoArray(r, i);
            }
        }

        assertInsertArraysEquals(a, r, (byte)4, 0);
    }
    static boolean testIS_DEFAULT(byte a) {
        return bits(a)==0;
    }

    @Test(dataProvider = "byteTestOpProvider")
    static void IS_DEFAULTByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                VectorMask<Byte> mv = av.test(VectorOperators.IS_DEFAULT);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
   
                 Assert.assertEquals(mv.laneIsSet(j), testIS_DEFAULT(a[i + j]));
                }
            }
        }
    }

    static boolean testIS_NEGATIVE(byte a) {
        return bits(a)<0;
    }

    @Test(dataProvider = "byteTestOpProvider")
    static void IS_NEGATIVEByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                VectorMask<Byte> mv = av.test(VectorOperators.IS_NEGATIVE);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
   
                 Assert.assertEquals(mv.laneIsSet(j), testIS_NEGATIVE(a[i + j]));
                }
            }
        }
    }





    @Test(dataProvider = "byteCompareOpProvider")
    static void LTByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.compare(VectorOperators.LT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void ltByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.lt(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void GTByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.compare(VectorOperators.GT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] > b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void EQByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.compare(VectorOperators.EQ, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void eqByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.eq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void NEByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.compare(VectorOperators.NE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] != b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void LEByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.compare(VectorOperators.LE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] <= b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void GEByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                VectorMask<Byte> mv = av.compare(VectorOperators.GE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), a[i + j] >= b[i + j]);
                }
            }
        }
    }


    static byte blend(byte a, byte b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void blendByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, mask, Byte256VectorTests::blend);
    }

    @Test(dataProvider = "byteUnaryOpShuffleProvider")
    static void RearrangeByte256VectorTests(IntFunction<byte[]> fa,
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
    static void getByte256VectorTests(IntFunction<byte[]> fa) {
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

        assertArraysEquals(a, r, Byte256VectorTests::get);
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void BroadcastByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector.broadcast(SPECIES, a[i]).intoArray(r, i);
            }
        }

        assertBroadcastArraysEquals(a, r);
    }





    @Test(dataProvider = "byteUnaryOpProvider")
    static void ZeroByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector.zero(SPECIES).intoArray(a, i);
            }
        }

        Assert.assertEquals(a, r);
    }




    static byte[] single(byte val) {
        byte[] res = new byte[SPECIES.length()];
        res[0] = val;

        return res;
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void singleByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.single(SPECIES, a[i]);
                av.intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte256VectorTests::single);
    }






















    static byte BITWISE_BLEND(byte a, byte b, byte c) {
        return (byte)((a&~(c))|(b&c));
    }


    @Test(dataProvider = "byteTernaryOpProvider")
    static void BITWISE_BLENDByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb, IntFunction<byte[]> fc) {
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

        assertArraysEquals(a, b, c, r, Byte256VectorTests::BITWISE_BLEND);
    }


    @Test(dataProvider = "byteTernaryOpMaskProvider")
    static void BITWISE_BLENDByte256VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<byte[]> fc, IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] c = fc.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                ByteVector bv = ByteVector.fromArray(SPECIES, b, i);
                ByteVector cv = ByteVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, c, r, mask, Byte256VectorTests::BITWISE_BLEND);
    }


    static byte NEG(byte a) {
        return (byte)(-((byte)a));
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void NEGByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte256VectorTests::NEG);
    }

    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void NEGMaskedByte256VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte256VectorTests::NEG);
    }

    static byte ABS(byte a) {
        return (byte)(Math.abs((byte)a));
    }

    static byte abs(byte a) {
        return (byte)(Math.abs((byte)a));
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void ABSByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte256VectorTests::ABS);
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void absByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte256VectorTests::abs);
    }

    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void ABSMaskedByte256VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte256VectorTests::ABS);
    }


    static byte NOT(byte a) {
        return (byte)(~((byte)a));
    }



    @Test(dataProvider = "byteUnaryOpProvider")
    static void NOTByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NOT).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte256VectorTests::NOT);
    }



    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void NOTMaskedByte256VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NOT, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte256VectorTests::NOT);
    }



    static byte ZOMO(byte a) {
        return (byte)((a==0?0:-1));
    }



    @Test(dataProvider = "byteUnaryOpProvider")
    static void ZOMOByte256VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte256VectorTests::ZOMO);
    }



    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void ZOMOMaskedByte256VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Byte> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte256VectorTests::ZOMO);
    }




    static byte[] gather(byte a[], int ix, int[] b, int iy) {
        byte[] res = new byte[SPECIES.length()];
        for (int i = 0; i < SPECIES.length(); i++) {
            int bi = iy + i;
            res[i] = a[b[bi] + ix];
        }
        return res;
    }

    @Test(dataProvider = "byteUnaryOpIndexProvider")
    static void gatherByte256VectorTests(IntFunction<byte[]> fa, BiFunction<Integer,Integer,int[]> fs) {
        byte[] a = fa.apply(SPECIES.length());
        int[] b    = fs.apply(a.length, SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i, b, i);
                av.intoArray(r, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::gather);
    }
    static byte[] scatter(byte a[], int ix, int[] b, int iy) {
      byte[] res = new byte[SPECIES.length()];
      for (int i = 0; i < SPECIES.length(); i++) {
        int bi = iy + i;
        res[b[bi]] = a[i + ix];
      }
      return res;
    }

    @Test(dataProvider = "byteUnaryOpIndexProvider")
    static void scatterByte256VectorTests(IntFunction<byte[]> fa, BiFunction<Integer,Integer,int[]> fs) {
        byte[] a = fa.apply(SPECIES.length());
        int[] b = fs.apply(a.length, SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector av = ByteVector.fromArray(SPECIES, a, i);
                av.intoArray(r, i, b, i);
            }
        }

        assertArraysEquals(a, b, r, Byte256VectorTests::scatter);
    }

}

