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
 * @run testng Short512VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.ShortVector;

import org.testng.Assert;

@org.testng.annotations.Test
public class Short512VectorTests {
    static final int SIZE = 512 * 1000;
    static final ShortVector.ShortSpecies<Shapes.S512Bit> species = (ShortVector.ShortSpecies<Shapes.S512Bit>)
                Vector.speciesInstance(Short.class, Shapes.S_512_BIT);

    static void init_mask(boolean[] t, int size) {
        for (int i = 0; i < size; i++) {
          t[i] = (i%2 == 0);
        }
    }

    static void init_arr1(short[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (short)(i*5);
        }
    }

    static void init_arr2(short[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (((short)(i+1) == 0) ? 1 : (short)(i+1));
        }
    }

    interface FUnOp {
        short apply(short a);
    }

    static void assertArraysEquals(short[] a, short[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(short[] a, short[] r, boolean[] mask, FUnOp f) {
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
                Assert.assertEquals(f.apply(a[i], b[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i], b[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(short[] a, short[] b, short[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(short[] a, short[] b, short[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i]);
            }
        } catch (AssertionError err) {
            Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i % species.length()]);
        }
    }

    static short add(short a, short b) {
        return (short)(a + b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short512VectorTests::add);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Short, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.add(bv, tMask).intoArray(rT, i);
            av.add(bv, fMask).intoArray(rF, i);
            av.add(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short512VectorTests::add);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short512VectorTests::add);
        assertArraysEquals(a, b, rM, mask, Short512VectorTests::add);
    }

    static short sub(short a, short b) {
        return (short)(a - b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short512VectorTests::sub);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Short, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.sub(bv, tMask).intoArray(rT, i);
            av.sub(bv, fMask).intoArray(rF, i);
            av.sub(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short512VectorTests::sub);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short512VectorTests::sub);
        assertArraysEquals(a, b, rM, mask, Short512VectorTests::sub);
    }

    static short div(short a, short b) {
        return (short)(a / b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short512VectorTests::div);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Short, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.div(bv, tMask).intoArray(rT, i);
            av.div(bv, fMask).intoArray(rF, i);
            av.div(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short512VectorTests::div);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short512VectorTests::div);
        assertArraysEquals(a, b, rM, mask, Short512VectorTests::div);
    }

    static short mul(short a, short b) {
        return (short)(a * b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short512VectorTests::mul);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Short, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.mul(bv, tMask).intoArray(rT, i);
            av.mul(bv, fMask).intoArray(rF, i);
            av.mul(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short512VectorTests::mul);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short512VectorTests::mul);
        assertArraysEquals(a, b, rM, mask, Short512VectorTests::mul);
    }


    static short and(short a, short b) {
        return (short)(a & b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.and(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short512VectorTests::and);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andMaskedShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Short, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.and(bv, tMask).intoArray(rT, i);
            av.and(bv, fMask).intoArray(rF, i);
            av.and(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short512VectorTests::and);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short512VectorTests::and);
        assertArraysEquals(a, b, rM, mask, Short512VectorTests::and);
    }



    static short or(short a, short b) {
        return (short)(a | b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.or(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short512VectorTests::or);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orMaskedShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Short, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.or(bv, tMask).intoArray(rT, i);
            av.or(bv, fMask).intoArray(rF, i);
            av.or(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short512VectorTests::or);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short512VectorTests::or);
        assertArraysEquals(a, b, rM, mask, Short512VectorTests::or);
    }



    static short xor(short a, short b) {
        return (short)(a ^ b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.xor(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short512VectorTests::xor);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorMaskedShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Short, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.xor(bv, tMask).intoArray(rT, i);
            av.xor(bv, fMask).intoArray(rF, i);
            av.xor(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short512VectorTests::xor);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short512VectorTests::xor);
        assertArraysEquals(a, b, rM, mask, Short512VectorTests::xor);
    }


    static short max(short a, short b) {
        return (short)(Math.max(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void maxShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short512VectorTests::max);
    }

    static short min(short a, short b) {
        return (short)(Math.min(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void minShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short512VectorTests::min);
    }


    static short blend(short a, short b, boolean mask) {
        return mask ? b : a;
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Short, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.blend(bv, tMask).intoArray(rT, i);
            av.blend(bv, fMask).intoArray(rF, i);
            av.blend(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short512VectorTests::blend);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short512VectorTests::blend);
        assertArraysEquals(a, b, rM, mask, Short512VectorTests::blend);
    }
    static short neg(short a) {
        return (short)(-((short)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.neg().intoArray(r, i);
        }

        assertArraysEquals(a, r, Short512VectorTests::neg);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Short, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.neg(tMask).intoArray(rT, i);
            av.neg(fMask).intoArray(rF, i);
            av.neg(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Short512VectorTests::neg);
        assertArraysEquals(a, rF, fMask.toArray(), Short512VectorTests::neg);
        assertArraysEquals(a, rM, mask, Short512VectorTests::neg);
    }

    static short abs(short a) {
        return (short)(Math.abs((short)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.abs().intoArray(r, i);
        }

        assertArraysEquals(a, r, Short512VectorTests::abs);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedShort512VectorTests() {
        short[] a = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Short, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.abs(tMask).intoArray(rT, i);
            av.abs(fMask).intoArray(rF, i);
            av.abs(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Short512VectorTests::abs);
        assertArraysEquals(a, rF, fMask.toArray(), Short512VectorTests::abs);
        assertArraysEquals(a, rM, mask, Short512VectorTests::abs);
    }

}

