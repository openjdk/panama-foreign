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
 * @run testng Byte128VectorTests
 *
 */

import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.ByteVector;

import org.testng.Assert;

@org.testng.annotations.Test
public class Byte128VectorTests {
    static final int SIZE = 128 * 1000;
    static final ByteVector.ByteSpecies<Shapes.S128Bit> species = (ByteVector.ByteSpecies<Shapes.S128Bit>)
                Vector.speciesInstance(Byte.class, Shapes.S_128_BIT);

    static void init_mask(boolean[] t, int size) {
        for (int i = 0; i < size; i++) {
          t[i] = (i%2 == 0);
        }
    }

    static void init_arr1(byte[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (byte)(i*5);
        }
    }

    static void init_arr2(byte[] t) {
        for (int i = 0; i < t.length; i++) {
            t[i] = (((byte)(i+1) == 0) ? 1 : (byte)(i+1));
        }
    }

    static byte add(byte a, byte b) {
        return (byte)(a + b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void addByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.add(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)add(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void addMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];
        byte[] d = new byte[SIZE];
        byte[] e = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.add(bv, tMask).intoArray(c, i);
            av.add(bv, fMask).intoArray(d, i);
            av.add(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            byte c_res = (byte)(add(a[i], b[i]));
            byte d_res = (byte) a[i];
            byte e_res;
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

    static byte sub(byte a, byte b) {
        return (byte)(a - b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void subByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.sub(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)sub(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void subMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];
        byte[] d = new byte[SIZE];
        byte[] e = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.sub(bv, tMask).intoArray(c, i);
            av.sub(bv, fMask).intoArray(d, i);
            av.sub(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            byte c_res = (byte)(sub(a[i], b[i]));
            byte d_res = (byte) a[i];
            byte e_res;
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

    static byte div(byte a, byte b) {
        return (byte)(a / b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void divByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.div(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)div(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void divMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];
        byte[] d = new byte[SIZE];
        byte[] e = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.div(bv, tMask).intoArray(c, i);
            av.div(bv, fMask).intoArray(d, i);
            av.div(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            byte c_res = (byte)(div(a[i], b[i]));
            byte d_res = (byte) a[i];
            byte e_res;
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

    static byte mul(byte a, byte b) {
        return (byte)(a * b);
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void mulByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.mul(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)mul(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void mulMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];
        byte[] d = new byte[SIZE];
        byte[] e = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.mul(bv, tMask).intoArray(c, i);
            av.mul(bv, fMask).intoArray(d, i);
            av.mul(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            byte c_res = (byte)(mul(a[i], b[i]));
            byte d_res = (byte) a[i];
            byte e_res;
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


    static byte and(byte a, byte b) {
        return (byte)(a & b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void andByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.and(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)and(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void andMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];
        byte[] d = new byte[SIZE];
        byte[] e = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.and(bv, tMask).intoArray(c, i);
            av.and(bv, fMask).intoArray(d, i);
            av.and(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            byte c_res = (byte)(and(a[i], b[i]));
            byte d_res = (byte) a[i];
            byte e_res;
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



    static byte or(byte a, byte b) {
        return (byte)(a | b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void orByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.or(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)or(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void orMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];
        byte[] d = new byte[SIZE];
        byte[] e = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.or(bv, tMask).intoArray(c, i);
            av.or(bv, fMask).intoArray(d, i);
            av.or(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            byte c_res = (byte)(or(a[i], b[i]));
            byte d_res = (byte) a[i];
            byte e_res;
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



    static byte xor(byte a, byte b) {
        return (byte)(a ^ b);
    }



   @org.testng.annotations.Test(invocationCount = 10)
    static void xorByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.xor(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)xor(a[i], b[i]), c[i], "at index #" + i);
        }
    }



    @org.testng.annotations.Test(invocationCount = 10)
    static void xorMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];
        byte[] d = new byte[SIZE];
        byte[] e = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.xor(bv, tMask).intoArray(c, i);
            av.xor(bv, fMask).intoArray(d, i);
            av.xor(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            byte c_res = (byte)(xor(a[i], b[i]));
            byte d_res = (byte) a[i];
            byte e_res;
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


    static byte max(byte a, byte b) {
        return (byte)(Math.max(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void maxByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.max(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)max(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    static byte min(byte a, byte b) {
        return (byte)(Math.min(a, b));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void minByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);
        init_arr2(b);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.min(bv).intoArray(c, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)min(a[i], b[i]), c[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void blendByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];
        byte[] c = new byte[SIZE];
        byte[] d = new byte[SIZE];
        byte[] e = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);
        init_arr2(b);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            ByteVector<Shapes.S128Bit> bv = species.fromArray(b, i);
            av.blend(bv, tMask).intoArray(c, i);
            av.blend(bv, fMask).intoArray(d, i);
            av.blend(bv, pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            byte c_sum = (byte) b[i];
            byte d_sum = (byte) a[i];
            byte e_sum;
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


    static byte neg(byte a) {
        return (byte)(-((byte)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void negByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.neg().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)neg((byte)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void negMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] c = new byte[SIZE];
        byte[] d = new byte[SIZE];
        byte[] e = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.neg(tMask).intoArray(c, i);
            av.neg(fMask).intoArray(d, i);
            av.neg(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            byte c_res = (byte)neg((byte)a[i]);
            byte d_res = (byte) a[i];
            byte e_res;
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

    static byte abs(byte a) {
        return (byte)(Math.abs((byte)a));
    }

   @org.testng.annotations.Test(invocationCount = 10)
    static void absByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] b = new byte[SIZE];

        // Data Initialization.
        init_arr1(a);

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.abs().intoArray(b, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            Assert.assertEquals((byte)abs((byte)a[i]), b[i], "at index #" + i);
        }
    }

    @org.testng.annotations.Test(invocationCount = 10)
    static void absMaskedByte128VectorTests() {
        byte[] a = new byte[SIZE];
        byte[] c = new byte[SIZE];
        byte[] d = new byte[SIZE];
        byte[] e = new byte[SIZE];
        boolean[] mask = new boolean[species.length()];
        
        // Data Initialization.
        init_mask(mask, species.length());
        init_arr1(a);

        Vector.Mask<Byte, Shapes.S128Bit> pMask = species.constantMask(mask);
        Vector.Mask<Byte, Shapes.S128Bit> tMask = species.trueMask();
        Vector.Mask<Byte, Shapes.S128Bit> fMask = species.falseMask();

        // Computation.
        for (int i = 0; i < a.length; i += species.length()) {
            ByteVector<Shapes.S128Bit> av = species.fromArray(a, i);
            av.abs(tMask).intoArray(c, i);
            av.abs(fMask).intoArray(d, i);
            av.abs(pMask).intoArray(e, i);
        }

        // Checking.
        for (int i = 0; i < a.length; i++) {
            byte c_res = (byte)abs((byte)a[i]);
            byte d_res = (byte) a[i];
            byte e_res;
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

