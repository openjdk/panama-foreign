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

    static short add(short a, short b) {
        return (short)(a + b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void addShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)add(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];
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
            av.add(bv, tMask).intoArray(c, i);
            av.add(bv, fMask).intoArray(d, i);
            av.add(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            short c_res = (short)(add(a[i], b[i]));
            short d_res = (short) a[i];
            short e_res;
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

    static short sub(short a, short b) {
        return (short)(a - b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void subShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)sub(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];
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
            av.sub(bv, tMask).intoArray(c, i);
            av.sub(bv, fMask).intoArray(d, i);
            av.sub(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            short c_res = (short)(sub(a[i], b[i]));
            short d_res = (short) a[i];
            short e_res;
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

    static short div(short a, short b) {
        return (short)(a / b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void divShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)div(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];
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
            av.div(bv, tMask).intoArray(c, i);
            av.div(bv, fMask).intoArray(d, i);
            av.div(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            short c_res = (short)(div(a[i], b[i]));
            short d_res = (short) a[i];
            short e_res;
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

    static short mul(short a, short b) {
        return (short)(a * b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void mulShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)mul(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];
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
            av.mul(bv, tMask).intoArray(c, i);
            av.mul(bv, fMask).intoArray(d, i);
            av.mul(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            short c_res = (short)(mul(a[i], b[i]));
            short d_res = (short) a[i];
            short e_res;
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


    static short and(short a, short b) {
        return (short)(a & b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void andShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.and(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)and(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];
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
            av.and(bv, tMask).intoArray(c, i);
            av.and(bv, fMask).intoArray(d, i);
            av.and(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            short c_res = (short)(and(a[i], b[i]));
            short d_res = (short) a[i];
            short e_res;
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



    static short or(short a, short b) {
        return (short)(a | b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void orShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.or(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)or(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];
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
            av.or(bv, tMask).intoArray(c, i);
            av.or(bv, fMask).intoArray(d, i);
            av.or(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            short c_res = (short)(or(a[i], b[i]));
            short d_res = (short) a[i];
            short e_res;
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



    static short xor(short a, short b) {
        return (short)(a ^ b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void xorShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.xor(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)xor(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];
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
            av.xor(bv, tMask).intoArray(c, i);
            av.xor(bv, fMask).intoArray(d, i);
            av.xor(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            short c_res = (short)(xor(a[i], b[i]));
            short d_res = (short) a[i];
            short e_res;
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


    static short max(short a, short b) {
        return (short)(Math.max(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void maxShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)max(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    static short min(short a, short b) {
        return (short)(Math.min(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void minShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            ShortVector<Shapes.S256Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)min(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];
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
            av.blend(bv, tMask).intoArray(c, i);
            av.blend(bv, fMask).intoArray(d, i);
            av.blend(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            short c_sum = (short) b[i];
            short d_sum = (short) a[i];
            short e_sum;
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


    static short neg(short a) {
        return (short)(-((short)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.neg().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)neg((short)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];
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
            av.neg(tMask).intoArray(c, i);
            av.neg(fMask).intoArray(d, i);
            av.neg(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            short c_res = (short)neg((short)a[i]);
            short d_res = (short) a[i];
            short e_res;
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

    static short abs(short a) {
        return (short)(Math.abs((short)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] b = new short[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ShortVector<Shapes.S256Bit> av = species.fromArray(a, i);
            av.abs().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((short)abs((short)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedShort256VectorTests() {
        short[] a = new short[SIZE];
        short[] c = new short[SIZE];
        short[] d = new short[SIZE];
        short[] e = new short[SIZE];
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
            av.abs(tMask).intoArray(c, i);
            av.abs(fMask).intoArray(d, i);
            av.abs(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            short c_res = (short)abs((short)a[i]);
            short d_res = (short) a[i];
            short e_res;
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

