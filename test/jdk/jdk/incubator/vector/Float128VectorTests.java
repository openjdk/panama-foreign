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
 * @run testng Float128VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.FloatVector;

import org.testng.Assert;

@org.testng.annotations.Test
public class Float128VectorTests {
    static final int SIZE = 128 * 1000;
    static final FloatVector.FloatSpecies<Shapes.S128Bit> species = (FloatVector.FloatSpecies<Shapes.S128Bit>)
                Vector.speciesInstance(Float.class, Shapes.S_128_BIT);

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

    interface FUnOp {
        float apply(float a);
    }

    static void assertArraysEquals(float[] a, float[] r, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(float[] a, float[] r, boolean[] mask, FUnOp f) {
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
        float apply(float a, float b);
    }

    interface FBinMaskOp {
        float apply(float a, float b, boolean m);

        static FBinMaskOp lift(FBinOp f) {
            return (a, b, m) -> m ? f.apply(a, b) : a;
        }
    }

    static void assertArraysEquals(float[] a, float[] b, float[] r, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i]), r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(f.apply(a[i], b[i]), r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(float[] a, float[] b, float[] r, boolean[] mask, FBinOp f) {
        assertArraysEquals(a, b, r, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(float[] a, float[] b, float[] r, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i]);
            }
        } catch (AssertionError err) {
            Assert.assertEquals(f.apply(a[i], b[i], mask[i % species.length()]), r[i], "at index #" + i + ", a[i] = " + a[i] + ", b[i] = " + b[i] + ", mask = " + mask[i % species.length()]);
        }
    }

    static float add(float a, float b) {
        return (float)(a + b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] r = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Float128VectorTests::add);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] rT = new float[SIZE];
        float[] rF = new float[SIZE];
        float[] rM = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Float, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.add(bv, tMask).intoArray(rT, i);
            av.add(bv, fMask).intoArray(rF, i);
            av.add(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Float128VectorTests::add);
        assertArraysEquals(a, b, rF, fMask.toArray(), Float128VectorTests::add);
        assertArraysEquals(a, b, rM, mask, Float128VectorTests::add);
    }

    static float sub(float a, float b) {
        return (float)(a - b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] r = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Float128VectorTests::sub);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] rT = new float[SIZE];
        float[] rF = new float[SIZE];
        float[] rM = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Float, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.sub(bv, tMask).intoArray(rT, i);
            av.sub(bv, fMask).intoArray(rF, i);
            av.sub(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Float128VectorTests::sub);
        assertArraysEquals(a, b, rF, fMask.toArray(), Float128VectorTests::sub);
        assertArraysEquals(a, b, rM, mask, Float128VectorTests::sub);
    }

    static float div(float a, float b) {
        return (float)(a / b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] r = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Float128VectorTests::div);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] rT = new float[SIZE];
        float[] rF = new float[SIZE];
        float[] rM = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Float, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.div(bv, tMask).intoArray(rT, i);
            av.div(bv, fMask).intoArray(rF, i);
            av.div(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Float128VectorTests::div);
        assertArraysEquals(a, b, rF, fMask.toArray(), Float128VectorTests::div);
        assertArraysEquals(a, b, rM, mask, Float128VectorTests::div);
    }

    static float mul(float a, float b) {
        return (float)(a * b);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] r = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Float128VectorTests::mul);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] rT = new float[SIZE];
        float[] rF = new float[SIZE];
        float[] rM = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Float, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.mul(bv, tMask).intoArray(rT, i);
            av.mul(bv, fMask).intoArray(rF, i);
            av.mul(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Float128VectorTests::mul);
        assertArraysEquals(a, b, rF, fMask.toArray(), Float128VectorTests::mul);
        assertArraysEquals(a, b, rM, mask, Float128VectorTests::mul);
    }










    static float max(float a, float b) {
        return (float)(Math.max(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void maxFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] r = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Float128VectorTests::max);
    }

    static float min(float a, float b) {
        return (float)(Math.min(a, b));
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void minFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] r = new float[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(r, i);
        }

        assertArraysEquals(a, b, r, Float128VectorTests::min);
    }


    static float blend(float a, float b, boolean mask) {
        return mask ? b : a;
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] b = new float[SIZE];
        float[] rT = new float[SIZE];
        float[] rF = new float[SIZE];
        float[] rM = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Float, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            FloatVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.blend(bv, tMask).intoArray(rT, i);
            av.blend(bv, fMask).intoArray(rF, i);
            av.blend(bv, pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, b, rT, tMask.toArray(), Float128VectorTests::blend);
        assertArraysEquals(a, b, rF, fMask.toArray(), Float128VectorTests::blend);
        assertArraysEquals(a, b, rM, mask, Float128VectorTests::blend);
    }
    static float neg(float a) {
        return (float)(-((float)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] r = new float[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.neg().intoArray(r, i);
        }

        assertArraysEquals(a, r, Float128VectorTests::neg);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] rT = new float[SIZE];
        float[] rF = new float[SIZE];
        float[] rM = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Float, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.neg(tMask).intoArray(rT, i);
            av.neg(fMask).intoArray(rF, i);
            av.neg(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Float128VectorTests::neg);
        assertArraysEquals(a, rF, fMask.toArray(), Float128VectorTests::neg);
        assertArraysEquals(a, rM, mask, Float128VectorTests::neg);
    }

    static float abs(float a) {
        return (float)(Math.abs((float)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] r = new float[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.abs().intoArray(r, i);
        }

        assertArraysEquals(a, r, Float128VectorTests::abs);
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedFloat128VectorTests() {
        float[] a = new float[SIZE];
        float[] rT = new float[SIZE];
        float[] rF = new float[SIZE];
        float[] rM = new float[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Float, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Float, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Float, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            FloatVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.abs(tMask).intoArray(rT, i);
            av.abs(fMask).intoArray(rF, i);
            av.abs(pMask).intoArray(rM, i);
        }

        assertArraysEquals(a, rT, tMask.toArray(), Float128VectorTests::abs);
        assertArraysEquals(a, rF, fMask.toArray(), Float128VectorTests::abs);
        assertArraysEquals(a, rM, mask, Float128VectorTests::abs);
    }

}

