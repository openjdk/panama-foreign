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
 * @run testng Double256VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.DoubleVector;

import org.testng.Assert;

@org.testng.annotations.Test
public class Double256VectorTests {
    static final int SIZE = 256 * 1000;
    static final DoubleVector.DoubleSpecies<Shapes.S256Bit> species = (DoubleVector.DoubleSpecies<Shapes.S256Bit>)
                Vector.speciesInstance(Double.class, Shapes.S_256_BIT);

    static void init_mask(boolean[] t, int size) {
        for (int i = 0; i < size; i++) {
          t[i] = (i%2 == 0);
        }
    }

    static void init_arr1(double[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (double)(i*5);
        }
    }

    static void init_arr2(double[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (((double)(i+1) == 0) ? 1 : (double)(i+1));
        }
    }

    static double add(double a, double b) {
        return (double)(a + b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void addDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((double)add(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];
        double[] d = new double[SIZE];
        double[] e = new double[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Double, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Double, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Double, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.add(bv, tMask).intoArray(c, i);
            av.add(bv, fMask).intoArray(d, i);
            av.add(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            double c_res = (double)(add(a[i], b[i]));
            double d_res = (double) a[i];
            double e_res;
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

    static double sub(double a, double b) {
        return (double)(a - b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void subDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((double)sub(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];
        double[] d = new double[SIZE];
        double[] e = new double[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Double, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Double, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Double, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.sub(bv, tMask).intoArray(c, i);
            av.sub(bv, fMask).intoArray(d, i);
            av.sub(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            double c_res = (double)(sub(a[i], b[i]));
            double d_res = (double) a[i];
            double e_res;
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

    static double div(double a, double b) {
        return (double)(a / b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void divDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((double)div(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];
        double[] d = new double[SIZE];
        double[] e = new double[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Double, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Double, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Double, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.div(bv, tMask).intoArray(c, i);
            av.div(bv, fMask).intoArray(d, i);
            av.div(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            double c_res = (double)(div(a[i], b[i]));
            double d_res = (double) a[i];
            double e_res;
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

    static double mul(double a, double b) {
        return (double)(a * b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void mulDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((double)mul(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];
        double[] d = new double[SIZE];
        double[] e = new double[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Double, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Double, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Double, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.mul(bv, tMask).intoArray(c, i);
            av.mul(bv, fMask).intoArray(d, i);
            av.mul(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            double c_res = (double)(mul(a[i], b[i]));
            double d_res = (double) a[i];
            double e_res;
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










    static double max(double a, double b) {
        return (double)(Math.max(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void maxDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((double)max(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    static double min(double a, double b) {
        return (double)(Math.min(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void minDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((double)min(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] c = new double[SIZE];
        double[] d = new double[SIZE];
        double[] e = new double[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Double, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Double, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Double, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.blend(bv, tMask).intoArray(c, i);
            av.blend(bv, fMask).intoArray(d, i);
            av.blend(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            double c_sum = (double) b[i];
            double d_sum = (double) a[i];
            double e_sum;
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


    static double neg(double a) {
        return (double)(-((double)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.neg().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((double)neg((double)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] c = new double[SIZE];
        double[] d = new double[SIZE];
        double[] e = new double[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Double, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Double, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Double, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.neg(tMask).intoArray(c, i);
            av.neg(fMask).intoArray(d, i);
            av.neg(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            double c_res = (double)neg((double)a[i]);
            double d_res = (double) a[i];
            double e_res;
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

    static double abs(double a) {
        return (double)(Math.abs((double)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.abs().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((double)abs((double)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] c = new double[SIZE];
        double[] d = new double[SIZE];
        double[] e = new double[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Double, Shapes.S256Bit> pMask = species.constantMask(mask);
        Vector.Mask<Double, Shapes.S256Bit> tMask = species.trueMask();
        Vector.Mask<Double, Shapes.S256Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.abs(tMask).intoArray(c, i);
            av.abs(fMask).intoArray(d, i);
            av.abs(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            double c_res = (double)abs((double)a[i]);
            double d_res = (double) a[i];
            double e_res;
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

