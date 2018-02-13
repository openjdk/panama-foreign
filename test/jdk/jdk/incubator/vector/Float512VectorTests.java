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
 * @run testng Float512VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.FloatVector;

import org.testng.Assert;

@org.testng.annotations.Test
public class Float512VectorTests {
    static final int SIZE = 512 * 1000;
    static final FloatVector.FloatSpecies<Shapes.S512Bit> species = (FloatVector.FloatSpecies<Shapes.S512Bit>)
                Vector.speciesInstance(Float.class, Shapes.S_512_BIT);

    static void init_mask(boolean[] t, int size) {
        for (int i = 0; i < size; i++) {
          t[i] = (i%2 == 0);
        }
    }

    static void init_arr1(float[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (float)(i*5);
        }
    }

    static void init_arr2(float[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (((float)(i+1) == 0) ? 1 : (float)(i+1));
        }
    }

    static float add(float a, float b) {
        return (float)(a + b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void addFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((float)add(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];
        float[] d = new float[SIZE];
        float[] e = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Float, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.add(bv, tMask).intoArray(c, i);
            av.add(bv, fMask).intoArray(d, i);
            av.add(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            float c_res = (float)(add(a[i], b[i]));
            float d_res = (float) a[i];
            float e_res;
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

    static float sub(float a, float b) {
        return (float)(a - b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void subFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((float)sub(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];
        float[] d = new float[SIZE];
        float[] e = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Float, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.sub(bv, tMask).intoArray(c, i);
            av.sub(bv, fMask).intoArray(d, i);
            av.sub(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            float c_res = (float)(sub(a[i], b[i]));
            float d_res = (float) a[i];
            float e_res;
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

    static float div(float a, float b) {
        return (float)(a / b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void divFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((float)div(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];
        float[] d = new float[SIZE];
        float[] e = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Float, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.div(bv, tMask).intoArray(c, i);
            av.div(bv, fMask).intoArray(d, i);
            av.div(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            float c_res = (float)(div(a[i], b[i]));
            float d_res = (float) a[i];
            float e_res;
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

    static float mul(float a, float b) {
        return (float)(a * b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void mulFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((float)mul(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];
        float[] d = new float[SIZE];
        float[] e = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Float, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.mul(bv, tMask).intoArray(c, i);
            av.mul(bv, fMask).intoArray(d, i);
            av.mul(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            float c_res = (float)(mul(a[i], b[i]));
            float d_res = (float) a[i];
            float e_res;
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










    static float max(float a, float b) {
        return (float)(Math.max(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void maxFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((float)max(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    static float min(float a, float b) {
        return (float)(Math.min(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void minFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((float)min(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] c = new float[SIZE];
        float[] d = new float[SIZE];
        float[] e = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Float, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S512Bit> bv = species.fromArray(b, i);
            av.blend(bv, tMask).intoArray(c, i);
            av.blend(bv, fMask).intoArray(d, i);
            av.blend(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            float c_sum = (float) b[i];
            float d_sum = (float) a[i];
            float e_sum;
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


    static float neg(float a) {
        return (float)(-((float)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.neg().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((float)neg((float)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] c = new float[SIZE];
        float[] d = new float[SIZE];
        float[] e = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Float, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.neg(tMask).intoArray(c, i);
            av.neg(fMask).intoArray(d, i);
            av.neg(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            float c_res = (float)neg((float)a[i]);
            float d_res = (float) a[i];
            float e_res;
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

    static float abs(float a) {
        return (float)(Math.abs((float)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.abs().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((float)abs((float)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedFloat512VectorTests() {
        float[] a = new float[SIZE];
        float[] c = new float[SIZE];
        float[] d = new float[SIZE];
        float[] e = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Float, Shapes.S512Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S512Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S512Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S512Bit> av = species.fromArray(a, i);
            av.abs(tMask).intoArray(c, i);
            av.abs(fMask).intoArray(d, i);
            av.abs(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            float c_res = (float)abs((float)a[i]);
            float d_res = (float) a[i];
            float e_res;
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

