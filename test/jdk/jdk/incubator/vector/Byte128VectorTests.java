/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

@org.testng.annotations.Test
public class Byte128VectorTests {
    static final int SIZE = 128 * 1000;
    static final ByteVector.ByteSpecies<Shapes.S128Bit> species = (ByteVector.ByteSpecies<Shapes.S128Bit>)
                Vector.speciesInstance(Byte.class, Shapes.S_128_BIT);

    static void init_mask(boolean[] t, int size) {
        for (int i = 0; i < size; i++) {
          t[i] = (i%2 == 0);
        }
    }

    static void init_arr1(byte[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (byte)(i*5);
        }
    }

    static void init_arr2(byte[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (((byte)(i+1) == 0) ? 1 : (byte)(i+1));
        }
    }

    interface FUnOp {
        byte apply(byte a);
    }

    static void assertArraysEquals(byte[] a, byte[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(byte[] a, byte[] r, boolean[] mask, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(mask[i % species.length()] ? f.apply(a[i]) : a[i], r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(mask[i % species.length()] ? f.apply(a[i]) : a[i], r[i], "at index #" + i);
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
                Assert.assertEquals(f.apply(a[i], b[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i], b[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(byte[] a, byte[] b, byte[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(byte[] a, byte[] b, byte[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i]);
            }
        } catch (AssertionError err) {
            Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i % species.length()]);
        }
    }

    static byte add(byte a, byte b) {
        return (byte)(a + b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Byte128VectorTests::add);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] rT = new byte[SIZE];
        byte[] rF = new byte[SIZE];
        byte[] rM = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.add(bv, tMask).intoArray(rT, i);
            av.add(bv, fMask).intoArray(rF, i);
            av.add(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Byte128VectorTests::add);
        assertArraysEquals(a, b, rF, fMask.toArray(), Byte128VectorTests::add);
        assertArraysEquals(a, b, rM, mask, Byte128VectorTests::add);
    }

    static byte sub(byte a, byte b) {
        return (byte)(a - b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Byte128VectorTests::sub);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] rT = new byte[SIZE];
        byte[] rF = new byte[SIZE];
        byte[] rM = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.sub(bv, tMask).intoArray(rT, i);
            av.sub(bv, fMask).intoArray(rF, i);
            av.sub(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Byte128VectorTests::sub);
        assertArraysEquals(a, b, rF, fMask.toArray(), Byte128VectorTests::sub);
        assertArraysEquals(a, b, rM, mask, Byte128VectorTests::sub);
    }

    static byte div(byte a, byte b) {
        return (byte)(a / b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Byte128VectorTests::div);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] rT = new byte[SIZE];
        byte[] rF = new byte[SIZE];
        byte[] rM = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.div(bv, tMask).intoArray(rT, i);
            av.div(bv, fMask).intoArray(rF, i);
            av.div(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Byte128VectorTests::div);
        assertArraysEquals(a, b, rF, fMask.toArray(), Byte128VectorTests::div);
        assertArraysEquals(a, b, rM, mask, Byte128VectorTests::div);
    }

    static byte mul(byte a, byte b) {
        return (byte)(a * b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Byte128VectorTests::mul);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] rT = new byte[SIZE];
        byte[] rF = new byte[SIZE];
        byte[] rM = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.mul(bv, tMask).intoArray(rT, i);
            av.mul(bv, fMask).intoArray(rF, i);
            av.mul(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Byte128VectorTests::mul);
        assertArraysEquals(a, b, rF, fMask.toArray(), Byte128VectorTests::mul);
        assertArraysEquals(a, b, rM, mask, Byte128VectorTests::mul);
    }


    static byte and(byte a, byte b) {
        return (byte)(a & b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.and(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Byte128VectorTests::and);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] rT = new byte[SIZE];
        byte[] rF = new byte[SIZE];
        byte[] rM = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.and(bv, tMask).intoArray(rT, i);
            av.and(bv, fMask).intoArray(rF, i);
            av.and(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Byte128VectorTests::and);
        assertArraysEquals(a, b, rF, fMask.toArray(), Byte128VectorTests::and);
        assertArraysEquals(a, b, rM, mask, Byte128VectorTests::and);
    }



    static byte or(byte a, byte b) {
        return (byte)(a | b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.or(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Byte128VectorTests::or);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] rT = new byte[SIZE];
        byte[] rF = new byte[SIZE];
        byte[] rM = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.or(bv, tMask).intoArray(rT, i);
            av.or(bv, fMask).intoArray(rF, i);
            av.or(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Byte128VectorTests::or);
        assertArraysEquals(a, b, rF, fMask.toArray(), Byte128VectorTests::or);
        assertArraysEquals(a, b, rM, mask, Byte128VectorTests::or);
    }



    static byte xor(byte a, byte b) {
        return (byte)(a ^ b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.xor(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Byte128VectorTests::xor);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] rT = new byte[SIZE];
        byte[] rF = new byte[SIZE];
        byte[] rM = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.xor(bv, tMask).intoArray(rT, i);
            av.xor(bv, fMask).intoArray(rF, i);
            av.xor(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Byte128VectorTests::xor);
        assertArraysEquals(a, b, rF, fMask.toArray(), Byte128VectorTests::xor);
        assertArraysEquals(a, b, rM, mask, Byte128VectorTests::xor);
    }


    static byte max(byte a, byte b) {
        return (byte)(Math.max(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void maxByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Byte128VectorTests::max);
    }

    static byte min(byte a, byte b) {
        return (byte)(Math.min(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void minByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Byte128VectorTests::min);
    }


    static byte blend(byte a, byte b, boolean mask) {
        return mask ? b : a;
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] rT = new byte[SIZE];
        byte[] rF = new byte[SIZE];
        byte[] rM = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.blend(bv, tMask).intoArray(rT, i);
            av.blend(bv, fMask).intoArray(rF, i);
            av.blend(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Byte128VectorTests::blend);
        assertArraysEquals(a, b, rF, fMask.toArray(), Byte128VectorTests::blend);
        assertArraysEquals(a, b, rM, mask, Byte128VectorTests::blend);
    }
    static byte neg(byte a) {
        return (byte)(-((byte)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.neg().intoArray(r, i);
        }

        assertArraysEquals(a, r, Byte128VectorTests::neg);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] rT = new byte[SIZE];
        byte[] rF = new byte[SIZE];
        byte[] rM = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.neg(tMask).intoArray(rT, i);
            av.neg(fMask).intoArray(rF, i);
            av.neg(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Byte128VectorTests::neg);
        assertArraysEquals(a, rF, fMask.toArray(), Byte128VectorTests::neg);
        assertArraysEquals(a, rM, mask, Byte128VectorTests::neg);
    }

    static byte abs(byte a) {
        return (byte)(Math.abs((byte)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] r = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.abs().intoArray(r, i);
        }

        assertArraysEquals(a, r, Byte128VectorTests::abs);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] rT = new byte[SIZE];
        byte[] rF = new byte[SIZE];
        byte[] rM = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.abs(tMask).intoArray(rT, i);
            av.abs(fMask).intoArray(rF, i);
            av.abs(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Byte128VectorTests::abs);
        assertArraysEquals(a, rF, fMask.toArray(), Byte128VectorTests::abs);
        assertArraysEquals(a, rM, mask, Byte128VectorTests::abs);
    }

}

