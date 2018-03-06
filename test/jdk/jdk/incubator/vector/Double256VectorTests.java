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

    interface FUnOp {
        double apply(double a);
    }

    static void assertArraysEquals(double[] a, double[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(double[] a, double[] r, boolean[] mask, FUnOp f) {
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
        double apply(double a, double b);
    }

    interface FBinMaskOp {
        double apply(double a, double b, boolean m);

        static FBinMaskOp lift(FBinOp f) {
            return (a, b, m) -> m ? f.apply(a, b) : a;
        }
    }

    static void assertArraysEquals(double[] a, double[] b, double[] r, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i], b[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(double[] a, double[] b, double[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(double[] a, double[] b, double[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i]);
            }
        } catch (AssertionError err) {
            Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i % species.length()]);
        }
    }

    static double add(double a, double b) {
        return (double)(a + b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] r = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double256VectorTests::add);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] rT = new double[SIZE];
        double[] rF = new double[SIZE];
        double[] rM = new double[SIZE];
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
            av.add(bv, tMask).intoArray(rT, i);
            av.add(bv, fMask).intoArray(rF, i);
            av.add(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Double256VectorTests::add);
        assertArraysEquals(a, b, rF, fMask.toArray(), Double256VectorTests::add);
        assertArraysEquals(a, b, rM, mask, Double256VectorTests::add);
    }

    static double sub(double a, double b) {
        return (double)(a - b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] r = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double256VectorTests::sub);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] rT = new double[SIZE];
        double[] rF = new double[SIZE];
        double[] rM = new double[SIZE];
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
            av.sub(bv, tMask).intoArray(rT, i);
            av.sub(bv, fMask).intoArray(rF, i);
            av.sub(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Double256VectorTests::sub);
        assertArraysEquals(a, b, rF, fMask.toArray(), Double256VectorTests::sub);
        assertArraysEquals(a, b, rM, mask, Double256VectorTests::sub);
    }

    static double div(double a, double b) {
        return (double)(a / b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] r = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double256VectorTests::div);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] rT = new double[SIZE];
        double[] rF = new double[SIZE];
        double[] rM = new double[SIZE];
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
            av.div(bv, tMask).intoArray(rT, i);
            av.div(bv, fMask).intoArray(rF, i);
            av.div(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Double256VectorTests::div);
        assertArraysEquals(a, b, rF, fMask.toArray(), Double256VectorTests::div);
        assertArraysEquals(a, b, rM, mask, Double256VectorTests::div);
    }

    static double mul(double a, double b) {
        return (double)(a * b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] r = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double256VectorTests::mul);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] rT = new double[SIZE];
        double[] rF = new double[SIZE];
        double[] rM = new double[SIZE];
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
            av.mul(bv, tMask).intoArray(rT, i);
            av.mul(bv, fMask).intoArray(rF, i);
            av.mul(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Double256VectorTests::mul);
        assertArraysEquals(a, b, rF, fMask.toArray(), Double256VectorTests::mul);
        assertArraysEquals(a, b, rM, mask, Double256VectorTests::mul);
    }










    static double max(double a, double b) {
        return (double)(Math.max(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void maxDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] r = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double256VectorTests::max);
    }

    static double min(double a, double b) {
        return (double)(Math.min(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void minDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] r = new double[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            DoubleVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Double256VectorTests::min);
    }


    static double blend(double a, double b, boolean mask) {
        return mask ? b : a;
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] b = new double[SIZE];
        double[] rT = new double[SIZE];
        double[] rF = new double[SIZE];
        double[] rM = new double[SIZE];
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
            av.blend(bv, tMask).intoArray(rT, i);
            av.blend(bv, fMask).intoArray(rF, i);
            av.blend(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Double256VectorTests::blend);
        assertArraysEquals(a, b, rF, fMask.toArray(), Double256VectorTests::blend);
        assertArraysEquals(a, b, rM, mask, Double256VectorTests::blend);
    }
    static double neg(double a) {
        return (double)(-((double)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] r = new double[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.neg().intoArray(r, i);
        }

        assertArraysEquals(a, r, Double256VectorTests::neg);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] rT = new double[SIZE];
        double[] rF = new double[SIZE];
        double[] rM = new double[SIZE];
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
            av.neg(tMask).intoArray(rT, i);
            av.neg(fMask).intoArray(rF, i);
            av.neg(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Double256VectorTests::neg);
        assertArraysEquals(a, rF, fMask.toArray(), Double256VectorTests::neg);
        assertArraysEquals(a, rM, mask, Double256VectorTests::neg);
    }

    static double abs(double a) {
        return (double)(Math.abs((double)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] r = new double[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            DoubleVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.abs().intoArray(r, i);
        }

        assertArraysEquals(a, r, Double256VectorTests::abs);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedDouble256VectorTests() {
        double[] a = new double[SIZE];
        double[] rT = new double[SIZE];
        double[] rF = new double[SIZE];
        double[] rM = new double[SIZE];
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
            av.abs(tMask).intoArray(rT, i);
            av.abs(fMask).intoArray(rF, i);
            av.abs(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Double256VectorTests::abs);
        assertArraysEquals(a, rF, fMask.toArray(), Double256VectorTests::abs);
        assertArraysEquals(a, rM, mask, Double256VectorTests::abs);
    }

}

