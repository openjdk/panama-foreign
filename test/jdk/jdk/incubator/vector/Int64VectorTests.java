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
 * @run testng Int64VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.IntVector;

import org.testng.Assert;

@org.testng.annotations.Test
public class Int64VectorTests {
    static final int SIZE = 64 * 1000;
    static final IntVector.IntSpecies<Shapes.S64Bit> species = (IntVector.IntSpecies<Shapes.S64Bit>)
                Vector.speciesInstance(Integer.class, Shapes.S_64_BIT);

    static void init_mask(boolean[] t, int size) {
        for (int i = 0; i < size; i++) {
          t[i] = (i%2 == 0);
        }
    }

    static void init_arr1(int[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (int)(i*5);
        }
    }

    static void init_arr2(int[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (((int)(i+1) == 0) ? 1 : (int)(i+1));
        }
    }

    static void init_5arr(int[] t1, int[] t2, int[] t3, int[] t4, int[] t5) {
        assert(t1.length == t2.length && t2.length == t3.length && t3.length == t4.length && t4.length == t5.length);
        int corner1 = Integer.MAX_VALUE;
        int corner2 = Integer.MIN_VALUE;
        int corner3 = corner2;
        int corner4 = corner1;
        int corner5 = 0;
        int corner6 = 0;
        int corner7 = 0;


        for (int i = 0; i < t1.length; i++) {
            t1[i] = (int)(i);
            t2[i] = (int)(i+1);
            t3[i] = (int)(i-2);
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
        int apply(int a);
    }

    static void assertArraysEquals(int[] a, int[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(int[] a, int[] r, boolean[] mask, FUnOp f) {
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
                Assert.assertEquals(f.apply(a[i], b[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i], b[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(int[] a, int[] b, int[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(int[] a, int[] b, int[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i]);
            }
        } catch (AssertionError err) {
            Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i % species.length()]);
        }
    }

    static int add(int a, int b) {
        return (int)(a + b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Int64VectorTests::add);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] rT = new int[SIZE];
        int[] rF = new int[SIZE];
        int[] rM = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S64Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S64Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S64Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.add(bv, tMask).intoArray(rT, i);
            av.add(bv, fMask).intoArray(rF, i);
            av.add(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Int64VectorTests::add);
        assertArraysEquals(a, b, rF, fMask.toArray(), Int64VectorTests::add);
        assertArraysEquals(a, b, rM, mask, Int64VectorTests::add);
    }

    static int sub(int a, int b) {
        return (int)(a - b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Int64VectorTests::sub);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] rT = new int[SIZE];
        int[] rF = new int[SIZE];
        int[] rM = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S64Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S64Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S64Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.sub(bv, tMask).intoArray(rT, i);
            av.sub(bv, fMask).intoArray(rF, i);
            av.sub(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Int64VectorTests::sub);
        assertArraysEquals(a, b, rF, fMask.toArray(), Int64VectorTests::sub);
        assertArraysEquals(a, b, rM, mask, Int64VectorTests::sub);
    }

    static int div(int a, int b) {
        return (int)(a / b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Int64VectorTests::div);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] rT = new int[SIZE];
        int[] rF = new int[SIZE];
        int[] rM = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S64Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S64Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S64Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.div(bv, tMask).intoArray(rT, i);
            av.div(bv, fMask).intoArray(rF, i);
            av.div(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Int64VectorTests::div);
        assertArraysEquals(a, b, rF, fMask.toArray(), Int64VectorTests::div);
        assertArraysEquals(a, b, rM, mask, Int64VectorTests::div);
    }

    static int mul(int a, int b) {
        return (int)(a * b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Int64VectorTests::mul);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] rT = new int[SIZE];
        int[] rF = new int[SIZE];
        int[] rM = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S64Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S64Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S64Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.mul(bv, tMask).intoArray(rT, i);
            av.mul(bv, fMask).intoArray(rF, i);
            av.mul(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Int64VectorTests::mul);
        assertArraysEquals(a, b, rF, fMask.toArray(), Int64VectorTests::mul);
        assertArraysEquals(a, b, rM, mask, Int64VectorTests::mul);
    }


    static int and(int a, int b) {
        return (int)(a & b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.and(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Int64VectorTests::and);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andMaskedInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] rT = new int[SIZE];
        int[] rF = new int[SIZE];
        int[] rM = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S64Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S64Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S64Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.and(bv, tMask).intoArray(rT, i);
            av.and(bv, fMask).intoArray(rF, i);
            av.and(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Int64VectorTests::and);
        assertArraysEquals(a, b, rF, fMask.toArray(), Int64VectorTests::and);
        assertArraysEquals(a, b, rM, mask, Int64VectorTests::and);
    }



    static int or(int a, int b) {
        return (int)(a | b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.or(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Int64VectorTests::or);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orMaskedInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] rT = new int[SIZE];
        int[] rF = new int[SIZE];
        int[] rM = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S64Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S64Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S64Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.or(bv, tMask).intoArray(rT, i);
            av.or(bv, fMask).intoArray(rF, i);
            av.or(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Int64VectorTests::or);
        assertArraysEquals(a, b, rF, fMask.toArray(), Int64VectorTests::or);
        assertArraysEquals(a, b, rM, mask, Int64VectorTests::or);
    }



    static int xor(int a, int b) {
        return (int)(a ^ b);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.xor(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Int64VectorTests::xor);
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorMaskedInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] rT = new int[SIZE];
        int[] rF = new int[SIZE];
        int[] rM = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S64Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S64Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S64Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.xor(bv, tMask).intoArray(rT, i);
            av.xor(bv, fMask).intoArray(rF, i);
            av.xor(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Int64VectorTests::xor);
        assertArraysEquals(a, b, rF, fMask.toArray(), Int64VectorTests::xor);
        assertArraysEquals(a, b, rM, mask, Int64VectorTests::xor);
    }


    static int max(int a, int b) {
        return (int)(Math.max(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void maxInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Int64VectorTests::max);
    }

    static int min(int a, int b) {
        return (int)(Math.min(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void minInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Int64VectorTests::min);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void lessThanInt64VectorTests1() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = av.lessThan(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = av.lessThan(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = av.lessThan(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = av.lessThan(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = av.lessThan(ev);

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
    static void lessThanInt64VectorTests2() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = bv.lessThan(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = bv.lessThan(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = bv.lessThan(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = bv.lessThan(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = bv.lessThan(ev);

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
    static void lessThanInt64VectorTests3() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = cv.lessThan(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = cv.lessThan(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = cv.lessThan(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = cv.lessThan(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = cv.lessThan(ev);

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
    static void lessThanInt64VectorTests4() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = dv.lessThan(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = dv.lessThan(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = dv.lessThan(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = dv.lessThan(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = dv.lessThan(ev);

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
    static void lessThanInt64VectorTests5() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = ev.lessThan(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = ev.lessThan(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = ev.lessThan(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = ev.lessThan(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = ev.lessThan(ev);

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
    static void greaterThanInt64VectorTests1() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = av.greaterThan(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = av.greaterThan(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = av.greaterThan(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = av.greaterThan(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = av.greaterThan(ev);

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
    static void greaterThanInt64VectorTests2() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = bv.greaterThan(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = bv.greaterThan(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = bv.greaterThan(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = bv.greaterThan(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = bv.greaterThan(ev);

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
    static void greaterThanInt64VectorTests3() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = cv.greaterThan(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = cv.greaterThan(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = cv.greaterThan(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = cv.greaterThan(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = cv.greaterThan(ev);

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
    static void greaterThanInt64VectorTests4() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = dv.greaterThan(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = dv.greaterThan(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = dv.greaterThan(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = dv.greaterThan(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = dv.greaterThan(ev);

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
    static void greaterThanInt64VectorTests5() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = ev.greaterThan(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = ev.greaterThan(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = ev.greaterThan(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = ev.greaterThan(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = ev.greaterThan(ev);

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
    static void equalInt64VectorTests1() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = av.equal(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = av.equal(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = av.equal(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = av.equal(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = av.equal(ev);

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
    static void equalInt64VectorTests2() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = bv.equal(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = bv.equal(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = bv.equal(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = bv.equal(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = bv.equal(ev);

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
    static void equalInt64VectorTests3() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = cv.equal(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = cv.equal(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = cv.equal(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = cv.equal(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = cv.equal(ev);

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
    static void equalInt64VectorTests4() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = dv.equal(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = dv.equal(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = dv.equal(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = dv.equal(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = dv.equal(ev);

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
    static void equalInt64VectorTests5() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = ev.equal(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = ev.equal(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = ev.equal(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = ev.equal(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = ev.equal(ev);

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
    static void notEqualInt64VectorTests1() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = av.notEqual(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = av.notEqual(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = av.notEqual(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = av.notEqual(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = av.notEqual(ev);

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
    static void notEqualInt64VectorTests2() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = bv.notEqual(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = bv.notEqual(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = bv.notEqual(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = bv.notEqual(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = bv.notEqual(ev);

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
    static void notEqualInt64VectorTests3() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = cv.notEqual(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = cv.notEqual(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = cv.notEqual(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = cv.notEqual(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = cv.notEqual(ev);

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
    static void notEqualInt64VectorTests4() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = dv.notEqual(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = dv.notEqual(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = dv.notEqual(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = dv.notEqual(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = dv.notEqual(ev);

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
    static void notEqualInt64VectorTests5() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = ev.notEqual(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = ev.notEqual(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = ev.notEqual(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = ev.notEqual(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = ev.notEqual(ev);

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
    static void lessThanEqInt64VectorTests1() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = av.lessThanEq(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = av.lessThanEq(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = av.lessThanEq(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = av.lessThanEq(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = av.lessThanEq(ev);

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
    static void lessThanEqInt64VectorTests2() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = bv.lessThanEq(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = bv.lessThanEq(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = bv.lessThanEq(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = bv.lessThanEq(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = bv.lessThanEq(ev);

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
    static void lessThanEqInt64VectorTests3() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = cv.lessThanEq(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = cv.lessThanEq(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = cv.lessThanEq(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = cv.lessThanEq(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = cv.lessThanEq(ev);

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
    static void lessThanEqInt64VectorTests4() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = dv.lessThanEq(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = dv.lessThanEq(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = dv.lessThanEq(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = dv.lessThanEq(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = dv.lessThanEq(ev);

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
    static void lessThanEqInt64VectorTests5() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = ev.lessThanEq(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = ev.lessThanEq(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = ev.lessThanEq(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = ev.lessThanEq(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = ev.lessThanEq(ev);

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
    static void greaterThanEqInt64VectorTests1() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = av.greaterThanEq(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = av.greaterThanEq(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = av.greaterThanEq(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = av.greaterThanEq(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = av.greaterThanEq(ev);

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
    static void greaterThanEqInt64VectorTests2() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = bv.greaterThanEq(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = bv.greaterThanEq(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = bv.greaterThanEq(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = bv.greaterThanEq(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = bv.greaterThanEq(ev);

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
    static void greaterThanEqInt64VectorTests3() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = cv.greaterThanEq(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = cv.greaterThanEq(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = cv.greaterThanEq(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = cv.greaterThanEq(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = cv.greaterThanEq(ev);

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
    static void greaterThanEqInt64VectorTests4() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = dv.greaterThanEq(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = dv.greaterThanEq(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = dv.greaterThanEq(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = dv.greaterThanEq(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = dv.greaterThanEq(ev);

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
    static void greaterThanEqInt64VectorTests5() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];

        // Data Initialization.
        init_5arr(a, b, c, d, e);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            IntVector<Shapes.S64Bit> cv = species.fromArray(c, i);
            IntVector<Shapes.S64Bit> dv = species.fromArray(d, i);
            IntVector<Shapes.S64Bit> ev = species.fromArray(e, i);
            Vector.Mask<Integer, Shapes.S64Bit> mask1 = ev.greaterThanEq(av);
            Vector.Mask<Integer, Shapes.S64Bit> mask2 = ev.greaterThanEq(bv);
            Vector.Mask<Integer, Shapes.S64Bit> mask3 = ev.greaterThanEq(cv);
            Vector.Mask<Integer, Shapes.S64Bit> mask4 = ev.greaterThanEq(dv);
            Vector.Mask<Integer, Shapes.S64Bit> mask5 = ev.greaterThanEq(ev);

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

    static int blend(int a, int b, boolean mask) {
        return mask ? b : a;
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] rT = new int[SIZE];
        int[] rF = new int[SIZE];
        int[] rM = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S64Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S64Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S64Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S64Bit> bv = species.fromArray(b, i);
            av.blend(bv, tMask).intoArray(rT, i);
            av.blend(bv, fMask).intoArray(rF, i);
            av.blend(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Int64VectorTests::blend);
        assertArraysEquals(a, b, rF, fMask.toArray(), Int64VectorTests::blend);
        assertArraysEquals(a, b, rM, mask, Int64VectorTests::blend);
    }
    static int neg(int a) {
        return (int)(-((int)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            av.neg().intoArray(r, i);
        }

        assertArraysEquals(a, r, Int64VectorTests::neg);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] rT = new int[SIZE];
        int[] rF = new int[SIZE];
        int[] rM = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Integer, Shapes.S64Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S64Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S64Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            av.neg(tMask).intoArray(rT, i);
            av.neg(fMask).intoArray(rF, i);
            av.neg(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Int64VectorTests::neg);
        assertArraysEquals(a, rF, fMask.toArray(), Int64VectorTests::neg);
        assertArraysEquals(a, rM, mask, Int64VectorTests::neg);
    }

    static int abs(int a) {
        return (int)(Math.abs((int)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] r = new int[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            av.abs().intoArray(r, i);
        }

        assertArraysEquals(a, r, Int64VectorTests::abs);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedInt64VectorTests() {
        int[] a = new int[SIZE];
        int[] rT = new int[SIZE];
        int[] rF = new int[SIZE];
        int[] rM = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Integer, Shapes.S64Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S64Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S64Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S64Bit> av = species.fromArray(a, i);
            av.abs(tMask).intoArray(rT, i);
            av.abs(fMask).intoArray(rF, i);
            av.abs(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Int64VectorTests::abs);
        assertArraysEquals(a, rF, fMask.toArray(), Int64VectorTests::abs);
        assertArraysEquals(a, rM, mask, Int64VectorTests::abs);
    }

}

