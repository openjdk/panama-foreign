/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * @test
 * @modules jdk.incubator.vector
 */

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.Shapes;
import jdk.incubator.vector.Vector;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

public class VectorHash {

    public static void main(String[] args) {
        Properties p = System.getProperties();
        p.forEach((k, v) -> {
            byte[] bk = k.toString().getBytes(StandardCharsets.UTF_8);
            byte[] bv = v.toString().getBytes(StandardCharsets.UTF_8);
            assertHashCode(bk);
            assertHashCode(bv);
        });
    }

    static void assertHashCode(byte[] b) {
        int expected = Arrays.hashCode(b);
        assertEquals(hashCodeUnrollExplicit(b), expected);
        assertEquals(hashCodeUnrollConstants(b), expected);
        assertEquals(hashCodeVector64(b), expected);
        assertEquals(hashCodeVector128(b), expected);
    }

    static void assertEquals(Object a, Object b) {
        if (!a.equals(b)) throw new AssertionError();
    }

    static int hashCodeUnrollExplicit(byte[] a) {
        if (a == null)
            return 0;

        int h = 1;
        int i = 0;
        for (; i < (a.length & ~(8 - 1)); i += 8) {
            h = h * 31 * 31 * 31 * 31 * 31 * 31 * 31 * 31 +
                a[i + 0] * 31 * 31 * 31 * 31 * 31 * 31 * 31 +
                a[i + 1] * 31 * 31 * 31 * 31 * 31 * 31 +
                a[i + 2] * 31 * 31 * 31 * 31 * 31 +
                a[i + 3] * 31 * 31 * 31 * 31 +
                a[i + 4] * 31 * 31 * 31 +
                a[i + 5] * 31 * 31 +
                a[i + 6] * 31 +
                a[i + 7];
        }

        for (; i < a.length; i++) {
            h = 31 * h + a[i];
        }
        return h;
    }

    static int hashCodeUnrollConstants(byte[] a) {
        if (a == null)
            return 0;

        int h = 1;
        int i = 0;
        for (; i < (a.length & ~(8 - 1)); i += 8) {
            h = h * COEFF_31_TO_8 +
                a[i + 0] * H_COEFF_8.get(0) +
                a[i + 1] * H_COEFF_8.get(1) +
                a[i + 2] * H_COEFF_8.get(2) +
                a[i + 3] * H_COEFF_8.get(3) +
                a[i + 4] * H_COEFF_8.get(4) +
                a[i + 5] * H_COEFF_8.get(5) +
                a[i + 6] * H_COEFF_8.get(6) +
                a[i + 7] * H_COEFF_8.get(7);
        }

        for (; i < a.length; i++) {
            h = 31 * h + a[i];
        }
        return h;
    }

    static int hashCodeVector64(byte[] a) {
        int h = 1;
        int i = 0;
        for (; i < (a.length & ~(BYTE_64_SPECIES.length() - 1)); i += BYTE_64_SPECIES.length()) {
            ByteVector<Shapes.S64Bit> b = BYTE_64_SPECIES.fromArray(a, i);
            IntVector<Shapes.S256Bit> x = (IntVector<Shapes.S256Bit>) b.cast(Integer.class, Shapes.S_256_BIT);
            h = h * COEFF_31_TO_8 + x.mul(H_COEFF_8).addAll();
        }

        for (; i < a.length; i++) {
            h = 31 * h + a[i];
        }
        return h;
    }

    static int hashCodeVector128(byte[] a) {
        int h = 1;
        int i = 0;
        for (; i < (a.length & ~(BYTE_128_SPECIES.length() - 1)); i += BYTE_128_SPECIES.length()) {
            ByteVector<Shapes.S128Bit> b = BYTE_128_SPECIES.fromArray(a, i);
            IntVector<Shapes.S512Bit> x = (IntVector<Shapes.S512Bit>) b.cast(Integer.class, Shapes.S_512_BIT);
            h = h * COEFF_31_TO_16 + x.mul(H_COEFF_16).addAll();
        }

        for (; i < a.length; i++) {
            h = 31 * h + a[i];
        }
        return h;
    }

    static final ByteVector.ByteSpecies<Shapes.S128Bit> BYTE_128_SPECIES = (ByteVector.ByteSpecies<Shapes.S128Bit>)
            Vector.speciesInstance(Byte.class, Shapes.S_128_BIT);
    static final int COEFF_31_TO_16;
    static final IntVector<Shapes.S512Bit> H_COEFF_16;

    static final ByteVector.ByteSpecies<Shapes.S64Bit> BYTE_64_SPECIES = (ByteVector.ByteSpecies<Shapes.S64Bit>)
            Vector.speciesInstance(Byte.class, Shapes.S_64_BIT);
    static final int COEFF_31_TO_8;
    static final IntVector<Shapes.S256Bit> H_COEFF_8;

    static {
        IntVector.IntSpecies<Shapes.S256Bit> int256Species = (IntVector.IntSpecies<Shapes.S256Bit>)
                Vector.speciesInstance(Integer.class, Shapes.S_256_BIT);

        int[] a = new int[int256Species.length()];
        a[a.length - 1] = 1;
        for (int i = 1; i < a.length; i++) {
            a[a.length - 1 - i] = a[a.length - 1 - i + 1] * 31;
        }

        COEFF_31_TO_8 = a[0] * 31;
        H_COEFF_8 = int256Species.fromArray(a, 0);


        IntVector.IntSpecies<Shapes.S512Bit> int512Species = (IntVector.IntSpecies<Shapes.S512Bit>)
                Vector.speciesInstance(Integer.class, Shapes.S_512_BIT);

        a = new int[int512Species.length()];
        a[a.length - 1] = 1;
        for (int i = 1; i < a.length; i++) {
            a[a.length - 1 - i] = a[a.length - 1 - i + 1] * 31;
        }

        COEFF_31_TO_16 = a[0] * 31;
        H_COEFF_16 = int512Species.fromArray(a, 0);
    }
}
