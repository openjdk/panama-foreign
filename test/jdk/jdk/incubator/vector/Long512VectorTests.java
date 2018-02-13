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

    static long add(long a, long b) {
        return (long)(a + b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void addLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)add(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];
        long[] d = new long[SIZE];
        long[] e = new long[SIZE];
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
            av.add(bv, tMask).intoArray(c, i);
            av.add(bv, fMask).intoArray(d, i);
            av.add(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            long c_res = (long)(add(a[i], b[i]));
            long d_res = (long) a[i];
            long e_res;
            if (mask[i%species.length()] == false) {
              e_res = d_res;
            } else {
              e_res = c_res;
            }

            Assert.assertEquals(c_res, c[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(d_res, d[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(e_res, e[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
        }
    }

    static long sub(long a, long b) {
        return (long)(a - b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void subLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)sub(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];
        long[] d = new long[SIZE];
        long[] e = new long[SIZE];
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
            av.sub(bv, tMask).intoArray(c, i);
            av.sub(bv, fMask).intoArray(d, i);
            av.sub(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            long c_res = (long)(sub(a[i], b[i]));
            long d_res = (long) a[i];
            long e_res;
            if (mask[i%species.length()] == false) {
              e_res = d_res;
            } else {
              e_res = c_res;
            }

            Assert.assertEquals(c_res, c[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(d_res, d[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(e_res, e[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
        }
    }

    static long div(long a, long b) {
        return (long)(a / b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void divLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)div(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];
        long[] d = new long[SIZE];
        long[] e = new long[SIZE];
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
            av.div(bv, tMask).intoArray(c, i);
            av.div(bv, fMask).intoArray(d, i);
            av.div(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            long c_res = (long)(div(a[i], b[i]));
            long d_res = (long) a[i];
            long e_res;
            if (mask[i%species.length()] == false) {
              e_res = d_res;
            } else {
              e_res = c_res;
            }

            Assert.assertEquals(c_res, c[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(d_res, d[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(e_res, e[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
        }
    }

    static long mul(long a, long b) {
        return (long)(a * b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void mulLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)mul(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];
        long[] d = new long[SIZE];
        long[] e = new long[SIZE];
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
            av.mul(bv, tMask).intoArray(c, i);
            av.mul(bv, fMask).intoArray(d, i);
            av.mul(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            long c_res = (long)(mul(a[i], b[i]));
            long d_res = (long) a[i];
            long e_res;
            if (mask[i%species.length()] == false) {
              e_res = d_res;
            } else {
              e_res = c_res;
            }

            Assert.assertEquals(c_res, c[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(d_res, d[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(e_res, e[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
        }
    }


    static long and(long a, long b) {
        return (long)(a & b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void andLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.and(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)and(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];
        long[] d = new long[SIZE];
        long[] e = new long[SIZE];
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
            av.and(bv, tMask).intoArray(c, i);
            av.and(bv, fMask).intoArray(d, i);
            av.and(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            long c_res = (long)(and(a[i], b[i]));
            long d_res = (long) a[i];
            long e_res;
            if (mask[i%species.length()] == false) {
              e_res = d_res;
            } else {
              e_res = c_res;
            }

            Assert.assertEquals(c_res, c[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(d_res, d[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(e_res, e[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
        }
    }



    static long or(long a, long b) {
        return (long)(a | b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void orLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.or(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)or(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];
        long[] d = new long[SIZE];
        long[] e = new long[SIZE];
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
            av.or(bv, tMask).intoArray(c, i);
            av.or(bv, fMask).intoArray(d, i);
            av.or(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            long c_res = (long)(or(a[i], b[i]));
            long d_res = (long) a[i];
            long e_res;
            if (mask[i%species.length()] == false) {
              e_res = d_res;
            } else {
              e_res = c_res;
            }

            Assert.assertEquals(c_res, c[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(d_res, d[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(e_res, e[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
        }
    }



    static long xor(long a, long b) {
        return (long)(a ^ b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void xorLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.xor(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)xor(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];
        long[] d = new long[SIZE];
        long[] e = new long[SIZE];
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
            av.xor(bv, tMask).intoArray(c, i);
            av.xor(bv, fMask).intoArray(d, i);
            av.xor(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            long c_res = (long)(xor(a[i], b[i]));
            long d_res = (long) a[i];
            long e_res;
            if (mask[i%species.length()] == false) {
              e_res = d_res;
            } else {
              e_res = c_res;
            }

            Assert.assertEquals(c_res, c[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(d_res, d[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(e_res, e[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
        }
    }


    static long max(long a, long b) {
        return (long)(Math.max(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void maxLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)max(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    static long min(long a, long b) {
        return (long)(Math.min(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void minLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            LongVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)min(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];
        long[] c = new long[SIZE];
        long[] d = new long[SIZE];
        long[] e = new long[SIZE];
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
            av.blend(bv, tMask).intoArray(c, i);
            av.blend(bv, fMask).intoArray(d, i);
            av.blend(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            long c_sum = (long) b[i];
            long d_sum = (long) a[i];
            long e_sum;
            if (mask[i%species.length()] == false) {
              e_sum = d_sum;
            } else {
              e_sum = c_sum;
            }

            Assert.assertEquals(c_sum, c[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(d_sum, d[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(e_sum, e[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i%species.length()]);
        }
    }


    static long neg(long a) {
        return (long)(-((long)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.neg().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)neg((long)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] c = new long[SIZE];
        long[] d = new long[SIZE];
        long[] e = new long[SIZE];
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
            av.neg(tMask).intoArray(c, i);
            av.neg(fMask).intoArray(d, i);
            av.neg(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            long c_res = (long)neg((long)a[i]);
            long d_res = (long) a[i];
            long e_res;
            if (mask[i%species.length()] == false) {
              e_res = d_res;
            } else {
              e_res = c_res;
            }

            Assert.assertEquals(c_res, c[i], "at index #" + i + ", a[i] = " + a[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(d_res, d[i], "at index #" + i + ", a[i] = " + a[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(e_res, e[i], "at index #" + i + ", a[i] = " + a[i] + ", mask = " + mask[i%species.length()]);
        }
    }

    static long abs(long a) {
        return (long)(Math.abs((long)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] b = new long[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            LongVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.abs().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((long)abs((long)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedLong512VectorTests() {
        long[] a = new long[SIZE];
        long[] c = new long[SIZE];
        long[] d = new long[SIZE];
        long[] e = new long[SIZE];
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
            av.abs(tMask).intoArray(c, i);
            av.abs(fMask).intoArray(d, i);
            av.abs(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            long c_res = (long)abs((long)a[i]);
            long d_res = (long) a[i];
            long e_res;
            if (mask[i%species.length()] == false) {
              e_res = d_res;
            } else {
              e_res = c_res;
            }

            Assert.assertEquals(c_res, c[i], "at index #" + i + ", a[i] = " + a[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(d_res, d[i], "at index #" + i + ", a[i] = " + a[i] + ", mask = " + mask[i%species.length()]);
            Assert.assertEquals(e_res, e[i], "at index #" + i + ", a[i] = " + a[i] + ", mask = " + mask[i%species.length()]);
        }
    }

}

