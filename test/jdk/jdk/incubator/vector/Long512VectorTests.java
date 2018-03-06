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
 * @run testng Long512VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.LongVector;

import org.testng.Assert;

@org.testng.annotations.Test
public class Long512VectorTests {
    static final int SIZE = 512 * 1000;
    static final LongVector.LongSpecies<Shapes.S512Bit> species = (LongVector.LongSpecies<Shapes.S512Bit>)
                Vector.speciesInstance(Long.class, Shapes.S_512_BIT);

    static void init_mask(boolean[] t, int size) {
        for (int i = 0; i < size; i++) {
          t[i] = (i%2 == 0);
        }
    }

    static void init_arr1(long[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (long)(i*5);
        }
    }

    static void init_arr2(long[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (((long)(i+1) == 0) ? 1 : (long)(i+1));
        }
    }

    interface FUnOp {
        long apply(long a);
    }

    static void assertArraysEquals(long[] a, long[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(long[] a, long[] r, boolean[] mask, FUnOp f) {
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
        long apply(long a, long b);
    }

    interface FBinMaskOp {
        long apply(long a, long b, boolean m);

        static FBinMaskOp lift(FBinOp f) {
            return (a, b, m) -> m ? f.apply(a, b) : a;
        }
    }

    static void assertArraysEquals(long[] a, long[] b, long[] r, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i], b[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(long[] a, long[] b, long[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(long[] a, long[] b, long[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i]);
            }
        } catch (AssertionError err) {
            Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i % species.length()]);
        }
    }

    static long add(long a, long b) {
        return (long)(a + b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Long512VectorTests::add);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] rT = new long[SIZE];
        long[] rF = new long[SIZE];
        long[] rM = new long[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Long, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Long, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Long, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.add(bv, tMask).intoArray(rT, i);
            av.add(bv, fMask).intoArray(rF, i);
            av.add(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Long512VectorTests::add);
        assertArraysEquals(a, b, rF, fMask.toArray(), Long512VectorTests::add);
        assertArraysEquals(a, b, rM, mask, Long512VectorTests::add);
    }

    static long sub(long a, long b) {
        return (long)(a - b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Long512VectorTests::sub);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] rT = new long[SIZE];
        long[] rF = new long[SIZE];
        long[] rM = new long[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Long, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Long, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Long, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.sub(bv, tMask).intoArray(rT, i);
            av.sub(bv, fMask).intoArray(rF, i);
            av.sub(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Long512VectorTests::sub);
        assertArraysEquals(a, b, rF, fMask.toArray(), Long512VectorTests::sub);
        assertArraysEquals(a, b, rM, mask, Long512VectorTests::sub);
    }

    static long div(long a, long b) {
        return (long)(a / b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Long512VectorTests::div);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] rT = new long[SIZE];
        long[] rF = new long[SIZE];
        long[] rM = new long[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Long, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Long, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Long, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.div(bv, tMask).intoArray(rT, i);
            av.div(bv, fMask).intoArray(rF, i);
            av.div(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Long512VectorTests::div);
        assertArraysEquals(a, b, rF, fMask.toArray(), Long512VectorTests::div);
        assertArraysEquals(a, b, rM, mask, Long512VectorTests::div);
    }

    static long mul(long a, long b) {
        return (long)(a * b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Long512VectorTests::mul);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] rT = new long[SIZE];
        long[] rF = new long[SIZE];
        long[] rM = new long[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Long, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Long, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Long, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.mul(bv, tMask).intoArray(rT, i);
            av.mul(bv, fMask).intoArray(rF, i);
            av.mul(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Long512VectorTests::mul);
        assertArraysEquals(a, b, rF, fMask.toArray(), Long512VectorTests::mul);
        assertArraysEquals(a, b, rM, mask, Long512VectorTests::mul);
    }


    static long and(long a, long b) {
        return (long)(a & b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.and(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Long512VectorTests::and);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] rT = new long[SIZE];
        long[] rF = new long[SIZE];
        long[] rM = new long[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Long, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Long, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Long, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.and(bv, tMask).intoArray(rT, i);
            av.and(bv, fMask).intoArray(rF, i);
            av.and(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Long512VectorTests::and);
        assertArraysEquals(a, b, rF, fMask.toArray(), Long512VectorTests::and);
        assertArraysEquals(a, b, rM, mask, Long512VectorTests::and);
    }



    static long or(long a, long b) {
        return (long)(a | b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.or(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Long512VectorTests::or);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] rT = new long[SIZE];
        long[] rF = new long[SIZE];
        long[] rM = new long[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Long, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Long, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Long, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.or(bv, tMask).intoArray(rT, i);
            av.or(bv, fMask).intoArray(rF, i);
            av.or(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Long512VectorTests::or);
        assertArraysEquals(a, b, rF, fMask.toArray(), Long512VectorTests::or);
        assertArraysEquals(a, b, rM, mask, Long512VectorTests::or);
    }



    static long xor(long a, long b) {
        return (long)(a ^ b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.xor(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Long512VectorTests::xor);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] rT = new long[SIZE];
        long[] rF = new long[SIZE];
        long[] rM = new long[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Long, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Long, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Long, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.xor(bv, tMask).intoArray(rT, i);
            av.xor(bv, fMask).intoArray(rF, i);
            av.xor(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Long512VectorTests::xor);
        assertArraysEquals(a, b, rF, fMask.toArray(), Long512VectorTests::xor);
        assertArraysEquals(a, b, rM, mask, Long512VectorTests::xor);
    }


    static long max(long a, long b) {
        return (long)(Math.max(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void maxLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Long512VectorTests::max);
    }

    static long min(long a, long b) {
        return (long)(Math.min(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void minLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Long512VectorTests::min);
    }


    static long blend(long a, long b, boolean mask) {
        return mask ? b : a;
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] rT = new long[SIZE];
        long[] rF = new long[SIZE];
        long[] rM = new long[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Long, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Long, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Long, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.blend(bv, tMask).intoArray(rT, i);
            av.blend(bv, fMask).intoArray(rF, i);
            av.blend(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Long512VectorTests::blend);
        assertArraysEquals(a, b, rF, fMask.toArray(), Long512VectorTests::blend);
        assertArraysEquals(a, b, rM, mask, Long512VectorTests::blend);
    }
    static long neg(long a) {
        return (long)(-((long)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.neg().intoArray(r, i);
        }

        assertArraysEquals(a, r, Long512VectorTests::neg);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] rT = new long[SIZE];
        long[] rF = new long[SIZE];
        long[] rM = new long[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Long, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Long, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Long, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.neg(tMask).intoArray(rT, i);
            av.neg(fMask).intoArray(rF, i);
            av.neg(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Long512VectorTests::neg);
        assertArraysEquals(a, rF, fMask.toArray(), Long512VectorTests::neg);
        assertArraysEquals(a, rM, mask, Long512VectorTests::neg);
    }

    static long abs(long a) {
        return (long)(Math.abs((long)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] r = new long[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.abs().intoArray(r, i);
        }

        assertArraysEquals(a, r, Long512VectorTests::abs);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] rT = new long[SIZE];
        long[] rF = new long[SIZE];
        long[] rM = new long[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Long, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Long, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Long, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.abs(tMask).intoArray(rT, i);
            av.abs(fMask).intoArray(rF, i);
            av.abs(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Long512VectorTests::abs);
        assertArraysEquals(a, rF, fMask.toArray(), Long512VectorTests::abs);
        assertArraysEquals(a, rM, mask, Long512VectorTests::abs);
    }

}

