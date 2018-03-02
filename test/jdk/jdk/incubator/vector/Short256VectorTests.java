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
 * @run testng Short256VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.ShortVector;

import org.testng.Assert;

@org.testng.annotations.Test
public class Short256VectorTests {
    static final int SIZE = 256 * 1000;
    static final ShortVector.ShortSpecies<Shapes.S256Bit> species = (ShortVector.ShortSpecies<Shapes.S256Bit>)
                Vector.speciesInstance(Short.class, Shapes.S_256_BIT);

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

    static void init_5arr(short[] t1, short[] t2, short[] t3, short[] t4, short[] t5) {
        assert(t1.length == t2.length && t2.length == t3.length && t3.length == t4.length && t4.length == t5.length);
        short corner1 = Short.MAX_VALUE;
        short corner2 = Short.MIN_VALUE;
        short corner3 = corner2;
        short corner4 = corner1;
        short corner5 = 0;
        short corner6 = 0;
        short corner7 = 0;


        for (int i = 0; i < t1.length; i++) {
            t1[i] = (short)(i);
            t2[i] = (short)(i+1);
            t3[i] = (short)(i-2);
            t4[i] = i%3 == 0 ? t1[i] : (i%3 == 1 ? t2[i] : t3[i]);
            switch(i%7) {
                case 0:
                    t5[i] = corner1;
                    break;
                case 1:
                    t5[i] = corner2;
                    break;
                case 2:
                    t5[i] = corner3;
                    break;
                case 3:
                    t5[i] = corner4;
                    break;
                case 4:
                    t5[i] = corner5;
                    break;
                case 5:
                    t5[i] = corner6;
                    break;
                default:
                    t5[i] = corner7;
                    break;
            }
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
    static void addShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short256VectorTests::add);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedShort256VectorTests() {
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

        Vector.Mask<Short, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.add(bv, tMask).intoArray(rT, i);
            av.add(bv, fMask).intoArray(rF, i);
            av.add(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short256VectorTests::add);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short256VectorTests::add);
        assertArraysEquals(a, b, rM, mask, Short256VectorTests::add);
    }

    static short sub(short a, short b) {
        return (short)(a - b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short256VectorTests::sub);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedShort256VectorTests() {
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

        Vector.Mask<Short, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.sub(bv, tMask).intoArray(rT, i);
            av.sub(bv, fMask).intoArray(rF, i);
            av.sub(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short256VectorTests::sub);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short256VectorTests::sub);
        assertArraysEquals(a, b, rM, mask, Short256VectorTests::sub);
    }

    static short div(short a, short b) {
        return (short)(a / b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short256VectorTests::div);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedShort256VectorTests() {
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

        Vector.Mask<Short, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.div(bv, tMask).intoArray(rT, i);
            av.div(bv, fMask).intoArray(rF, i);
            av.div(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short256VectorTests::div);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short256VectorTests::div);
        assertArraysEquals(a, b, rM, mask, Short256VectorTests::div);
    }

    static short mul(short a, short b) {
        return (short)(a * b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short256VectorTests::mul);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedShort256VectorTests() {
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

        Vector.Mask<Short, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.mul(bv, tMask).intoArray(rT, i);
            av.mul(bv, fMask).intoArray(rF, i);
            av.mul(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short256VectorTests::mul);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short256VectorTests::mul);
        assertArraysEquals(a, b, rM, mask, Short256VectorTests::mul);
    }


    static short and(short a, short b) {
        return (short)(a & b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.and(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short256VectorTests::and);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andMaskedShort256VectorTests() {
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

        Vector.Mask<Short, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.and(bv, tMask).intoArray(rT, i);
            av.and(bv, fMask).intoArray(rF, i);
            av.and(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short256VectorTests::and);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short256VectorTests::and);
        assertArraysEquals(a, b, rM, mask, Short256VectorTests::and);
    }



    static short or(short a, short b) {
        return (short)(a | b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.or(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short256VectorTests::or);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orMaskedShort256VectorTests() {
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

        Vector.Mask<Short, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.or(bv, tMask).intoArray(rT, i);
            av.or(bv, fMask).intoArray(rF, i);
            av.or(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short256VectorTests::or);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short256VectorTests::or);
        assertArraysEquals(a, b, rM, mask, Short256VectorTests::or);
    }



    static short xor(short a, short b) {
        return (short)(a ^ b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.xor(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short256VectorTests::xor);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorMaskedShort256VectorTests() {
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

        Vector.Mask<Short, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.xor(bv, tMask).intoArray(rT, i);
            av.xor(bv, fMask).intoArray(rF, i);
            av.xor(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short256VectorTests::xor);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short256VectorTests::xor);
        assertArraysEquals(a, b, rM, mask, Short256VectorTests::xor);
    }


    static short max(short a, short b) {
        return (short)(Math.max(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void maxShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short256VectorTests::max);
    }

    static short min(short a, short b) {
        return (short)(Math.min(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void minShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Short256VectorTests::min);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanShort256VectorTests1() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = av.lessThan(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = av.lessThan(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = av.lessThan(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = av.lessThan(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = av.lessThan(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), a[i+j] < a[i+j]);
                Assert.assertEquals(mask2.getElement(j), a[i+j] < b[i+j]);
                Assert.assertEquals(mask3.getElement(j), a[i+j] < c[i+j]);
                Assert.assertEquals(mask4.getElement(j), a[i+j] < d[i+j]);
                Assert.assertEquals(mask5.getElement(j), a[i+j] < e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanShort256VectorTests2() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = bv.lessThan(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = bv.lessThan(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = bv.lessThan(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = bv.lessThan(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = bv.lessThan(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), b[i+j] < a[i+j]);
                Assert.assertEquals(mask2.getElement(j), b[i+j] < b[i+j]);
                Assert.assertEquals(mask3.getElement(j), b[i+j] < c[i+j]);
                Assert.assertEquals(mask4.getElement(j), b[i+j] < d[i+j]);
                Assert.assertEquals(mask5.getElement(j), b[i+j] < e[i+j]);
            }
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanShort256VectorTests3() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = cv.lessThan(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = cv.lessThan(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = cv.lessThan(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = cv.lessThan(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = cv.lessThan(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), c[i+j] < a[i+j]);
                Assert.assertEquals(mask2.getElement(j), c[i+j] < b[i+j]);
                Assert.assertEquals(mask3.getElement(j), c[i+j] < c[i+j]);
                Assert.assertEquals(mask4.getElement(j), c[i+j] < d[i+j]);
                Assert.assertEquals(mask5.getElement(j), c[i+j] < e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanShort256VectorTests4() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = dv.lessThan(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = dv.lessThan(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = dv.lessThan(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = dv.lessThan(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = dv.lessThan(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), d[i+j] < a[i+j]);
                Assert.assertEquals(mask2.getElement(j), d[i+j] < b[i+j]);
                Assert.assertEquals(mask3.getElement(j), d[i+j] < c[i+j]);
                Assert.assertEquals(mask4.getElement(j), d[i+j] < d[i+j]);
                Assert.assertEquals(mask5.getElement(j), d[i+j] < e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanShort256VectorTests5() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = ev.lessThan(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = ev.lessThan(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = ev.lessThan(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = ev.lessThan(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = ev.lessThan(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), e[i+j] < a[i+j]);
                Assert.assertEquals(mask2.getElement(j), e[i+j] < b[i+j]);
                Assert.assertEquals(mask3.getElement(j), e[i+j] < c[i+j]);
                Assert.assertEquals(mask4.getElement(j), e[i+j] < d[i+j]);
                Assert.assertEquals(mask5.getElement(j), e[i+j] < e[i+j]);
            }
        }
    }
   @org.testng.annotations.Test(invocationCount = 10)
    static void greaterThanShort256VectorTests1() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = av.greaterThan(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = av.greaterThan(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = av.greaterThan(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = av.greaterThan(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = av.greaterThan(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), a[i+j] > a[i+j]);
                Assert.assertEquals(mask2.getElement(j), a[i+j] > b[i+j]);
                Assert.assertEquals(mask3.getElement(j), a[i+j] > c[i+j]);
                Assert.assertEquals(mask4.getElement(j), a[i+j] > d[i+j]);
                Assert.assertEquals(mask5.getElement(j), a[i+j] > e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void greaterThanShort256VectorTests2() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = bv.greaterThan(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = bv.greaterThan(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = bv.greaterThan(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = bv.greaterThan(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = bv.greaterThan(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), b[i+j] > a[i+j]);
                Assert.assertEquals(mask2.getElement(j), b[i+j] > b[i+j]);
                Assert.assertEquals(mask3.getElement(j), b[i+j] > c[i+j]);
                Assert.assertEquals(mask4.getElement(j), b[i+j] > d[i+j]);
                Assert.assertEquals(mask5.getElement(j), b[i+j] > e[i+j]);
            }
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void greaterThanShort256VectorTests3() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = cv.greaterThan(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = cv.greaterThan(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = cv.greaterThan(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = cv.greaterThan(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = cv.greaterThan(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), c[i+j] > a[i+j]);
                Assert.assertEquals(mask2.getElement(j), c[i+j] > b[i+j]);
                Assert.assertEquals(mask3.getElement(j), c[i+j] > c[i+j]);
                Assert.assertEquals(mask4.getElement(j), c[i+j] > d[i+j]);
                Assert.assertEquals(mask5.getElement(j), c[i+j] > e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void greaterThanShort256VectorTests4() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = dv.greaterThan(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = dv.greaterThan(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = dv.greaterThan(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = dv.greaterThan(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = dv.greaterThan(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), d[i+j] > a[i+j]);
                Assert.assertEquals(mask2.getElement(j), d[i+j] > b[i+j]);
                Assert.assertEquals(mask3.getElement(j), d[i+j] > c[i+j]);
                Assert.assertEquals(mask4.getElement(j), d[i+j] > d[i+j]);
                Assert.assertEquals(mask5.getElement(j), d[i+j] > e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void greaterThanShort256VectorTests5() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = ev.greaterThan(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = ev.greaterThan(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = ev.greaterThan(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = ev.greaterThan(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = ev.greaterThan(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), e[i+j] > a[i+j]);
                Assert.assertEquals(mask2.getElement(j), e[i+j] > b[i+j]);
                Assert.assertEquals(mask3.getElement(j), e[i+j] > c[i+j]);
                Assert.assertEquals(mask4.getElement(j), e[i+j] > d[i+j]);
                Assert.assertEquals(mask5.getElement(j), e[i+j] > e[i+j]);
            }
        }
    }
   @org.testng.annotations.Test(invocationCount = 10)
    static void equalShort256VectorTests1() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = av.equal(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = av.equal(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = av.equal(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = av.equal(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = av.equal(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), a[i+j] == a[i+j]);
                Assert.assertEquals(mask2.getElement(j), a[i+j] == b[i+j]);
                Assert.assertEquals(mask3.getElement(j), a[i+j] == c[i+j]);
                Assert.assertEquals(mask4.getElement(j), a[i+j] == d[i+j]);
                Assert.assertEquals(mask5.getElement(j), a[i+j] == e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void equalShort256VectorTests2() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = bv.equal(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = bv.equal(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = bv.equal(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = bv.equal(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = bv.equal(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), b[i+j] == a[i+j]);
                Assert.assertEquals(mask2.getElement(j), b[i+j] == b[i+j]);
                Assert.assertEquals(mask3.getElement(j), b[i+j] == c[i+j]);
                Assert.assertEquals(mask4.getElement(j), b[i+j] == d[i+j]);
                Assert.assertEquals(mask5.getElement(j), b[i+j] == e[i+j]);
            }
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void equalShort256VectorTests3() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = cv.equal(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = cv.equal(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = cv.equal(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = cv.equal(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = cv.equal(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), c[i+j] == a[i+j]);
                Assert.assertEquals(mask2.getElement(j), c[i+j] == b[i+j]);
                Assert.assertEquals(mask3.getElement(j), c[i+j] == c[i+j]);
                Assert.assertEquals(mask4.getElement(j), c[i+j] == d[i+j]);
                Assert.assertEquals(mask5.getElement(j), c[i+j] == e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void equalShort256VectorTests4() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = dv.equal(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = dv.equal(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = dv.equal(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = dv.equal(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = dv.equal(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), d[i+j] == a[i+j]);
                Assert.assertEquals(mask2.getElement(j), d[i+j] == b[i+j]);
                Assert.assertEquals(mask3.getElement(j), d[i+j] == c[i+j]);
                Assert.assertEquals(mask4.getElement(j), d[i+j] == d[i+j]);
                Assert.assertEquals(mask5.getElement(j), d[i+j] == e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void equalShort256VectorTests5() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = ev.equal(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = ev.equal(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = ev.equal(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = ev.equal(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = ev.equal(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), e[i+j] == a[i+j]);
                Assert.assertEquals(mask2.getElement(j), e[i+j] == b[i+j]);
                Assert.assertEquals(mask3.getElement(j), e[i+j] == c[i+j]);
                Assert.assertEquals(mask4.getElement(j), e[i+j] == d[i+j]);
                Assert.assertEquals(mask5.getElement(j), e[i+j] == e[i+j]);
            }
        }
    }
   @org.testng.annotations.Test(invocationCount = 10)
    static void notEqualShort256VectorTests1() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = av.notEqual(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = av.notEqual(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = av.notEqual(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = av.notEqual(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = av.notEqual(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), a[i+j] != a[i+j]);
                Assert.assertEquals(mask2.getElement(j), a[i+j] != b[i+j]);
                Assert.assertEquals(mask3.getElement(j), a[i+j] != c[i+j]);
                Assert.assertEquals(mask4.getElement(j), a[i+j] != d[i+j]);
                Assert.assertEquals(mask5.getElement(j), a[i+j] != e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void notEqualShort256VectorTests2() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = bv.notEqual(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = bv.notEqual(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = bv.notEqual(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = bv.notEqual(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = bv.notEqual(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), b[i+j] != a[i+j]);
                Assert.assertEquals(mask2.getElement(j), b[i+j] != b[i+j]);
                Assert.assertEquals(mask3.getElement(j), b[i+j] != c[i+j]);
                Assert.assertEquals(mask4.getElement(j), b[i+j] != d[i+j]);
                Assert.assertEquals(mask5.getElement(j), b[i+j] != e[i+j]);
            }
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void notEqualShort256VectorTests3() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = cv.notEqual(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = cv.notEqual(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = cv.notEqual(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = cv.notEqual(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = cv.notEqual(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), c[i+j] != a[i+j]);
                Assert.assertEquals(mask2.getElement(j), c[i+j] != b[i+j]);
                Assert.assertEquals(mask3.getElement(j), c[i+j] != c[i+j]);
                Assert.assertEquals(mask4.getElement(j), c[i+j] != d[i+j]);
                Assert.assertEquals(mask5.getElement(j), c[i+j] != e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void notEqualShort256VectorTests4() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = dv.notEqual(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = dv.notEqual(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = dv.notEqual(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = dv.notEqual(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = dv.notEqual(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), d[i+j] != a[i+j]);
                Assert.assertEquals(mask2.getElement(j), d[i+j] != b[i+j]);
                Assert.assertEquals(mask3.getElement(j), d[i+j] != c[i+j]);
                Assert.assertEquals(mask4.getElement(j), d[i+j] != d[i+j]);
                Assert.assertEquals(mask5.getElement(j), d[i+j] != e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void notEqualShort256VectorTests5() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = ev.notEqual(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = ev.notEqual(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = ev.notEqual(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = ev.notEqual(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = ev.notEqual(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), e[i+j] != a[i+j]);
                Assert.assertEquals(mask2.getElement(j), e[i+j] != b[i+j]);
                Assert.assertEquals(mask3.getElement(j), e[i+j] != c[i+j]);
                Assert.assertEquals(mask4.getElement(j), e[i+j] != d[i+j]);
                Assert.assertEquals(mask5.getElement(j), e[i+j] != e[i+j]);
            }
        }
    }
   @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanEqShort256VectorTests1() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = av.lessThanEq(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = av.lessThanEq(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = av.lessThanEq(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = av.lessThanEq(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = av.lessThanEq(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), a[i+j] <= a[i+j]);
                Assert.assertEquals(mask2.getElement(j), a[i+j] <= b[i+j]);
                Assert.assertEquals(mask3.getElement(j), a[i+j] <= c[i+j]);
                Assert.assertEquals(mask4.getElement(j), a[i+j] <= d[i+j]);
                Assert.assertEquals(mask5.getElement(j), a[i+j] <= e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanEqShort256VectorTests2() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = bv.lessThanEq(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = bv.lessThanEq(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = bv.lessThanEq(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = bv.lessThanEq(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = bv.lessThanEq(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), b[i+j] <= a[i+j]);
                Assert.assertEquals(mask2.getElement(j), b[i+j] <= b[i+j]);
                Assert.assertEquals(mask3.getElement(j), b[i+j] <= c[i+j]);
                Assert.assertEquals(mask4.getElement(j), b[i+j] <= d[i+j]);
                Assert.assertEquals(mask5.getElement(j), b[i+j] <= e[i+j]);
            }
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanEqShort256VectorTests3() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = cv.lessThanEq(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = cv.lessThanEq(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = cv.lessThanEq(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = cv.lessThanEq(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = cv.lessThanEq(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), c[i+j] <= a[i+j]);
                Assert.assertEquals(mask2.getElement(j), c[i+j] <= b[i+j]);
                Assert.assertEquals(mask3.getElement(j), c[i+j] <= c[i+j]);
                Assert.assertEquals(mask4.getElement(j), c[i+j] <= d[i+j]);
                Assert.assertEquals(mask5.getElement(j), c[i+j] <= e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanEqShort256VectorTests4() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = dv.lessThanEq(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = dv.lessThanEq(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = dv.lessThanEq(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = dv.lessThanEq(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = dv.lessThanEq(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), d[i+j] <= a[i+j]);
                Assert.assertEquals(mask2.getElement(j), d[i+j] <= b[i+j]);
                Assert.assertEquals(mask3.getElement(j), d[i+j] <= c[i+j]);
                Assert.assertEquals(mask4.getElement(j), d[i+j] <= d[i+j]);
                Assert.assertEquals(mask5.getElement(j), d[i+j] <= e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanEqShort256VectorTests5() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = ev.lessThanEq(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = ev.lessThanEq(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = ev.lessThanEq(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = ev.lessThanEq(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = ev.lessThanEq(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), e[i+j] <= a[i+j]);
                Assert.assertEquals(mask2.getElement(j), e[i+j] <= b[i+j]);
                Assert.assertEquals(mask3.getElement(j), e[i+j] <= c[i+j]);
                Assert.assertEquals(mask4.getElement(j), e[i+j] <= d[i+j]);
                Assert.assertEquals(mask5.getElement(j), e[i+j] <= e[i+j]);
            }
        }
    }
   @org.testng.annotations.Test(invocationCount = 10)
    static void greaterThanEqShort256VectorTests1() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = av.greaterThanEq(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = av.greaterThanEq(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = av.greaterThanEq(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = av.greaterThanEq(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = av.greaterThanEq(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), a[i+j] >= a[i+j]);
                Assert.assertEquals(mask2.getElement(j), a[i+j] >= b[i+j]);
                Assert.assertEquals(mask3.getElement(j), a[i+j] >= c[i+j]);
                Assert.assertEquals(mask4.getElement(j), a[i+j] >= d[i+j]);
                Assert.assertEquals(mask5.getElement(j), a[i+j] >= e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void greaterThanEqShort256VectorTests2() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = bv.greaterThanEq(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = bv.greaterThanEq(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = bv.greaterThanEq(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = bv.greaterThanEq(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = bv.greaterThanEq(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), b[i+j] >= a[i+j]);
                Assert.assertEquals(mask2.getElement(j), b[i+j] >= b[i+j]);
                Assert.assertEquals(mask3.getElement(j), b[i+j] >= c[i+j]);
                Assert.assertEquals(mask4.getElement(j), b[i+j] >= d[i+j]);
                Assert.assertEquals(mask5.getElement(j), b[i+j] >= e[i+j]);
            }
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void greaterThanEqShort256VectorTests3() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = cv.greaterThanEq(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = cv.greaterThanEq(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = cv.greaterThanEq(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = cv.greaterThanEq(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = cv.greaterThanEq(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), c[i+j] >= a[i+j]);
                Assert.assertEquals(mask2.getElement(j), c[i+j] >= b[i+j]);
                Assert.assertEquals(mask3.getElement(j), c[i+j] >= c[i+j]);
                Assert.assertEquals(mask4.getElement(j), c[i+j] >= d[i+j]);
                Assert.assertEquals(mask5.getElement(j), c[i+j] >= e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void greaterThanEqShort256VectorTests4() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = dv.greaterThanEq(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = dv.greaterThanEq(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = dv.greaterThanEq(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = dv.greaterThanEq(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = dv.greaterThanEq(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), d[i+j] >= a[i+j]);
                Assert.assertEquals(mask2.getElement(j), d[i+j] >= b[i+j]);
                Assert.assertEquals(mask3.getElement(j), d[i+j] >= c[i+j]);
                Assert.assertEquals(mask4.getElement(j), d[i+j] >= d[i+j]);
                Assert.assertEquals(mask5.getElement(j), d[i+j] >= e[i+j]);
            }
        }
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void greaterThanEqShort256VectorTests5() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            ShortVector<Shapes.S256Bit> cv = species.fromArray(c, i);
            ShortVector<Shapes.S256Bit> dv = species.fromArray(d, i);
            ShortVector<Shapes.S256Bit> ev = species.fromArray(e, i);
            Vector.Mask<Short, Shapes.S256Bit> mask1 = ev.greaterThanEq(av);
            Vector.Mask<Short, Shapes.S256Bit> mask2 = ev.greaterThanEq(bv);
            Vector.Mask<Short, Shapes.S256Bit> mask3 = ev.greaterThanEq(cv);
            Vector.Mask<Short, Shapes.S256Bit> mask4 = ev.greaterThanEq(dv);
            Vector.Mask<Short, Shapes.S256Bit> mask5 = ev.greaterThanEq(ev);

            // Check results as part of computation.
            for (int j = 0; j < species.length(); j++) {
                Assert.assertEquals(mask1.getElement(j), e[i+j] >= a[i+j]);
                Assert.assertEquals(mask2.getElement(j), e[i+j] >= b[i+j]);
                Assert.assertEquals(mask3.getElement(j), e[i+j] >= c[i+j]);
                Assert.assertEquals(mask4.getElement(j), e[i+j] >= d[i+j]);
                Assert.assertEquals(mask5.getElement(j), e[i+j] >= e[i+j]);
            }
        }
    }

    static short blend(short a, short b, boolean mask) {
        return mask ? b : a;
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendShort256VectorTests() {
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

        Vector.Mask<Short, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.blend(bv, tMask).intoArray(rT, i);
            av.blend(bv, fMask).intoArray(rF, i);
            av.blend(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Short256VectorTests::blend);
        assertArraysEquals(a, b, rF, fMask.toArray(), Short256VectorTests::blend);
        assertArraysEquals(a, b, rM, mask, Short256VectorTests::blend);
    }
    static short neg(short a) {
        return (short)(-((short)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.neg().intoArray(r, i);
        }

        assertArraysEquals(a, r, Short256VectorTests::neg);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Short, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.neg(tMask).intoArray(rT, i);
            av.neg(fMask).intoArray(rF, i);
            av.neg(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Short256VectorTests::neg);
        assertArraysEquals(a, rF, fMask.toArray(), Short256VectorTests::neg);
        assertArraysEquals(a, rM, mask, Short256VectorTests::neg);
    }

    static short abs(short a) {
        return (short)(Math.abs((short)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] r = new short[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.abs().intoArray(r, i);
        }

        assertArraysEquals(a, r, Short256VectorTests::abs);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] rT = new short[SIZE];
        short[] rF = new short[SIZE];
        short[] rM = new short[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Short, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Short, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Short, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.abs(tMask).intoArray(rT, i);
            av.abs(fMask).intoArray(rF, i);
            av.abs(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Short256VectorTests::abs);
        assertArraysEquals(a, rF, fMask.toArray(), Short256VectorTests::abs);
        assertArraysEquals(a, rM, mask, Short256VectorTests::abs);
    }

}

