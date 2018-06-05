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
 * @run testng Byte128VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.ByteVector;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class Byte128VectorTests extends AbstractVectorTest {

    static final ByteVector.ByteSpecies<Shapes.S128Bit> SPECIES =
                ByteVector.species(Shapes.S_128_BIT);

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);

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

    static void assertReductionArraysEquals(byte[] a, byte[] b, FReductionOp f) {
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
            Assert.assertEquals(r[i], f.apply(a[i], b[i]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i]);
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
    static final List<IntFunction<byte[]>> BYTE_GENERATORS = List.of(
            withToString("byte[i * 5]", (int s) -> {
                return fill(s * 1000,
                            i -> (byte)(i * 5));
            }),
            withToString("byte[i + 1]", (int s) -> {
                return fill(s * 1000,
                            i -> (((byte)(i + 1) == 0) ? 1 : (byte)(i + 1)));
            }),
            withToString("byte[cornerCaseValue(i)]", (int s) -> {
                return fill(s * 1000,
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

    @DataProvider
    public Object[][] byteBinaryOpProvider() {
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

    static final List<IntFunction<byte[]>> BYTE_COMPARE_GENERATORS = List.of(
            withToString("byte[i]", (int s) -> {
                return fill(s * 1000,
                            i -> (byte)i);
            }),
            withToString("byte[i + 1]", (int s) -> {
                return fill(s * 1000,
                            i -> (byte)(i + 1));
            }),
            withToString("byte[i - 2]", (int s) -> {
                return fill(s * 1000,
                            i -> (byte)(i - 2));
            }),
            withToString("byte[zigZag(i)]", (int s) -> {
                return fill(s * 1000,
                            i -> i%3 == 0 ? (byte)i : (i%3 == 1 ? (byte)(i + 1) : (byte)(i - 2)));
            }),
            withToString("byte[cornerCaseValue(i)]", (int s) -> {
                return fill(s * 1000,
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


    static byte add(byte a, byte b) {
        return (byte)(a + b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void addByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length()); 
        byte[] b = fb.apply(SPECIES.length()); 
        byte[] r = new byte[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.add(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Byte128VectorTests::add);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void addByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.add(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Byte128VectorTests::add);
    }

    static byte sub(byte a, byte b) {
        return (byte)(a - b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void subByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length()); 
        byte[] b = fb.apply(SPECIES.length()); 
        byte[] r = new byte[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.sub(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Byte128VectorTests::sub);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void subByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.sub(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Byte128VectorTests::sub);
    }




    static byte mul(byte a, byte b) {
        return (byte)(a * b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void mulByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length()); 
        byte[] b = fb.apply(SPECIES.length()); 
        byte[] r = new byte[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.mul(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Byte128VectorTests::mul);
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void mulByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.mul(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Byte128VectorTests::mul);
    }


    static byte and(byte a, byte b) {
        return (byte)(a & b);
    }



    @Test(dataProvider = "byteBinaryOpProvider")
    static void andByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length()); 
        byte[] b = fb.apply(SPECIES.length()); 
        byte[] r = new byte[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.and(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Byte128VectorTests::and);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void andByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.and(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Byte128VectorTests::and);
    }



    static byte or(byte a, byte b) {
        return (byte)(a | b);
    }



    @Test(dataProvider = "byteBinaryOpProvider")
    static void orByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length()); 
        byte[] b = fb.apply(SPECIES.length()); 
        byte[] r = new byte[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.or(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Byte128VectorTests::or);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void orByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.or(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Byte128VectorTests::or);
    }



    static byte xor(byte a, byte b) {
        return (byte)(a ^ b);
    }



    @Test(dataProvider = "byteBinaryOpProvider")
    static void xorByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length()); 
        byte[] b = fb.apply(SPECIES.length()); 
        byte[] r = new byte[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.xor(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Byte128VectorTests::xor);
    }



    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void xorByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.xor(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Byte128VectorTests::xor);
    }


    static byte max(byte a, byte b) {
        return (byte)((a > b) ? a : b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void maxByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length()); 
        byte[] b = fb.apply(SPECIES.length()); 
        byte[] r = new byte[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.max(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Byte128VectorTests::max);
    }

    static byte min(byte a, byte b) {
        return (byte)((a < b) ? a : b);
    }

    @Test(dataProvider = "byteBinaryOpProvider")
    static void minByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length()); 
        byte[] b = fb.apply(SPECIES.length()); 
        byte[] r = new byte[a.length];       

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.min(bv).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, Byte128VectorTests::min);
    }

    static byte andAll(byte[] a, int idx) {
        byte res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res &= a[i];
        }

        return res;
    }


    @Test(dataProvider = "byteUnaryOpProvider")
    static void andAllByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.andAll();
            }
        }

        assertReductionArraysEquals(a, r, Byte128VectorTests::andAll);
    }


    static byte orAll(byte[] a, int idx) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res |= a[i];
        }

        return res;
    }


    @Test(dataProvider = "byteUnaryOpProvider")
    static void orAllByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.orAll();
            }
        }

        assertReductionArraysEquals(a, r, Byte128VectorTests::orAll);
    }


    static byte xorAll(byte[] a, int idx) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res ^= a[i];
        }

        return res;
    }


    @Test(dataProvider = "byteUnaryOpProvider")
    static void xorAllByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.xorAll();
            }
        }

        assertReductionArraysEquals(a, r, Byte128VectorTests::xorAll);
    }

    static byte addAll(byte[] a, int idx) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res += a[i];
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void addAllByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.addAll();
            }
        }

        assertReductionArraysEquals(a, r, Byte128VectorTests::addAll);
    }
    static byte subAll(byte[] a, int idx) {
        byte res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res -= a[i];
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void subAllByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.subAll();
            }
        }

        assertReductionArraysEquals(a, r, Byte128VectorTests::subAll);
    }
    static byte mulAll(byte[] a, int idx) {
        byte res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res *= a[i];
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void mulAllByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.mulAll();
            }
        }

        assertReductionArraysEquals(a, r, Byte128VectorTests::mulAll);
    }
    static byte minAll(byte[] a, int idx) {
        byte res = Byte.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res = (res < a[i])?res:a[i];
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void minAllByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.minAll();
            }
        }

        assertReductionArraysEquals(a, r, Byte128VectorTests::minAll);
    }
    static byte maxAll(byte[] a, int idx) {
        byte res = Byte.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res = (res > a[i])?res:a[i];
        }

        return res;
    }
    @Test(dataProvider = "byteUnaryOpProvider")
    static void maxAllByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
              ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
              r[i] = av.maxAll();
            }
        }

        assertReductionArraysEquals(a, r, Byte128VectorTests::maxAll);
    }

    static boolean anyTrue(boolean[] a, int idx) {
        boolean res = false;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res |= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void anyTrueByte128VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = new boolean[mask.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
              Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromArray(mask, i);
              r[i] = vmask.anyTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Byte128VectorTests::anyTrue);
    }


    static boolean allTrue(boolean[] a, int idx) {
        boolean res = true;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
          res &= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void allTrueByte128VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = new boolean[mask.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
              Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromArray(mask, i);
              r[i] = vmask.allTrue();
            }
        }

        assertReductionBoolArraysEquals(mask, r, Byte128VectorTests::allTrue);
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void lessThanByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Byte, Shapes.S128Bit> mv = av.lessThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] < b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void greaterThanByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Byte, Shapes.S128Bit> mv = av.greaterThan(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] > b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void equalByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Byte, Shapes.S128Bit> mv = av.equal(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] == b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void notEqualByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Byte, Shapes.S128Bit> mv = av.notEqual(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] != b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void lessThanEqByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Byte, Shapes.S128Bit> mv = av.lessThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] <= b[i + j]);
                }
            }
        }
    }


    @Test(dataProvider = "byteCompareOpProvider")
    static void greaterThanEqByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                Vector.Mask<Byte, Shapes.S128Bit> mv = av.greaterThanEq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.getElement(j), a[i + j] >= b[i + j]);
                }
            }
        }
    }


    static byte blend(byte a, byte b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "byteBinaryOpMaskProvider")
    static void blendByte128VectorTests(IntFunction<byte[]> fa, IntFunction<byte[]> fb,
                                          IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] b = fb.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                ByteVector<Shapes.S128Bit> bv = SPECIES.fromArray(b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }
        assertArraysEquals(a, b, r, mask, Byte128VectorTests::blend);
    }



















    static byte neg(byte a) {
        return (byte)(-((byte)a));
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void negByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.neg().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte128VectorTests::neg);
    }

    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void negMaskedByte128VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.neg(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte128VectorTests::neg);
    }

    static byte abs(byte a) {
        return (byte)(Math.abs((byte)a));
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void absByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte128VectorTests::abs);
    }

    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void absMaskedByte128VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.abs(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte128VectorTests::abs);
    }


    static byte not(byte a) {
        return (byte)(~((byte)a));
    }



    @Test(dataProvider = "byteUnaryOpProvider")
    static void notByte128VectorTests(IntFunction<byte[]> fa) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.not().intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, Byte128VectorTests::not);
    }



    @Test(dataProvider = "byteUnaryOpMaskProvider")
    static void notMaskedByte128VectorTests(IntFunction<byte[]> fa,
                                                IntFunction<boolean[]> fm) {
        byte[] a = fa.apply(SPECIES.length());
        byte[] r = new byte[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        Vector.Mask<Byte, Shapes.S128Bit> vmask = SPECIES.maskFromValues(mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                ByteVector<Shapes.S128Bit> av = SPECIES.fromArray(a, i);
                av.not(vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(a, r, mask, Byte128VectorTests::not);
    }




}

