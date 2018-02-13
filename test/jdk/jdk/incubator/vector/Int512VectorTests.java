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
 * @run testng Int512VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.IntVector;

import org.testng.Assert;

@org.testng.annotations.Test
public class Int512VectorTests {
    static final int SIZE = 512 * 1000;
    static final IntVector.IntSpecies<Shapes.S512Bit> species = (IntVector.IntSpecies<Shapes.S512Bit>)
                Vector.speciesInstance(Integer.class, Shapes.S_512_BIT);

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

    static int add(int a, int b) {
        return (int)(a + b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void addInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)add(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.add(bv, tMask).intoArray(c, i);
            av.add(bv, fMask).intoArray(d, i);
            av.add(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            int c_res = (int)(add(a[i], b[i]));
            int d_res = (int) a[i];
            int e_res;
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

    static int sub(int a, int b) {
        return (int)(a - b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void subInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)sub(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.sub(bv, tMask).intoArray(c, i);
            av.sub(bv, fMask).intoArray(d, i);
            av.sub(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            int c_res = (int)(sub(a[i], b[i]));
            int d_res = (int) a[i];
            int e_res;
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

    static int div(int a, int b) {
        return (int)(a / b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void divInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)div(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.div(bv, tMask).intoArray(c, i);
            av.div(bv, fMask).intoArray(d, i);
            av.div(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            int c_res = (int)(div(a[i], b[i]));
            int d_res = (int) a[i];
            int e_res;
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

    static int mul(int a, int b) {
        return (int)(a * b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void mulInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)mul(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.mul(bv, tMask).intoArray(c, i);
            av.mul(bv, fMask).intoArray(d, i);
            av.mul(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            int c_res = (int)(mul(a[i], b[i]));
            int d_res = (int) a[i];
            int e_res;
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


    static int and(int a, int b) {
        return (int)(a & b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void andInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.and(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)and(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andMaskedInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.and(bv, tMask).intoArray(c, i);
            av.and(bv, fMask).intoArray(d, i);
            av.and(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            int c_res = (int)(and(a[i], b[i]));
            int d_res = (int) a[i];
            int e_res;
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



    static int or(int a, int b) {
        return (int)(a | b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void orInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.or(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)or(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orMaskedInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.or(bv, tMask).intoArray(c, i);
            av.or(bv, fMask).intoArray(d, i);
            av.or(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            int c_res = (int)(or(a[i], b[i]));
            int d_res = (int) a[i];
            int e_res;
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



    static int xor(int a, int b) {
        return (int)(a ^ b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void xorInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.xor(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)xor(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorMaskedInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.xor(bv, tMask).intoArray(c, i);
            av.xor(bv, fMask).intoArray(d, i);
            av.xor(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            int c_res = (int)(xor(a[i], b[i]));
            int d_res = (int) a[i];
            int e_res;
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


    static int max(int a, int b) {
        return (int)(Math.max(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void maxInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)max(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    static int min(int a, int b) {
        return (int)(Math.min(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void minInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)min(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Integer, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            IntVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.blend(bv, tMask).intoArray(c, i);
            av.blend(bv, fMask).intoArray(d, i);
            av.blend(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            int c_sum = (int) b[i];
            int d_sum = (int) a[i];
            int e_sum;
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


    static int neg(int a) {
        return (int)(-((int)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.neg().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)neg((int)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Integer, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.neg(tMask).intoArray(c, i);
            av.neg(fMask).intoArray(d, i);
            av.neg(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            int c_res = (int)neg((int)a[i]);
            int d_res = (int) a[i];
            int e_res;
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

    static int abs(int a) {
        return (int)(Math.abs((int)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] b = new int[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.abs().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((int)abs((int)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedInt512VectorTests() {
        int[] a = new int[SIZE];
        int[] c = new int[SIZE];
        int[] d = new int[SIZE];
        int[] e = new int[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Integer, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Integer, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Integer, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            IntVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.abs(tMask).intoArray(c, i);
            av.abs(fMask).intoArray(d, i);
            av.abs(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            int c_res = (int)abs((int)a[i]);
            int d_res = (int) a[i];
            int e_res;
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

