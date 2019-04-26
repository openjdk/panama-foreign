/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */

package benchmark.jdk.incubator.vector;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.LongVector;
import jdk.incubator.vector.Vector;
import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorMask;

import java.util.Random;
import java.util.function.IntFunction;

public class AbstractVectorBenchmark {
    static final Random RANDOM = new Random(Integer.getInteger("jdk.incubator.vector.random-seed", 1337));

    static final VectorSpecies<Byte> B64  = ByteVector.SPECIES_64;
    static final VectorSpecies<Byte> B128 = ByteVector.SPECIES_128;
    static final VectorSpecies<Byte> B256 = ByteVector.SPECIES_256;
    static final VectorSpecies<Byte> B512 = ByteVector.SPECIES_512;

    static final VectorSpecies<Short> S64  = ShortVector.SPECIES_64;
    static final VectorSpecies<Short> S128 = ShortVector.SPECIES_128;
    static final VectorSpecies<Short> S256 = ShortVector.SPECIES_256;
    static final VectorSpecies<Short> S512 = ShortVector.SPECIES_512;

    static final VectorSpecies<Integer> I64  = IntVector.SPECIES_64;
    static final VectorSpecies<Integer> I128 = IntVector.SPECIES_128;
    static final VectorSpecies<Integer> I256 = IntVector.SPECIES_256;
    static final VectorSpecies<Integer> I512 = IntVector.SPECIES_512;

    static final VectorSpecies<Long> L64  = LongVector.SPECIES_64;
    static final VectorSpecies<Long> L128 = LongVector.SPECIES_128;
    static final VectorSpecies<Long> L256 = LongVector.SPECIES_256;
    static final VectorSpecies<Long> L512 = LongVector.SPECIES_512;

    static VectorShape widen(VectorShape s) {
        switch (s) {
            case S_64_BIT:  return VectorShape.S_128_BIT;
            case S_128_BIT: return VectorShape.S_256_BIT;
            case S_256_BIT: return VectorShape.S_512_BIT;
            default: throw new IllegalArgumentException("" + s);
        }
    }

    static VectorShape narrow(VectorShape s) {
        switch (s) {
            case S_512_BIT: return VectorShape.S_256_BIT;
            case S_256_BIT: return VectorShape.S_128_BIT;
            case S_128_BIT: return VectorShape.S_64_BIT;
            default: throw new IllegalArgumentException("" + s);
        }
    }

    static <E> VectorSpecies<E> widen(VectorSpecies<E> s) {
        return VectorSpecies.of(s.elementType(), widen(s.shape()));
    }

    static <E> VectorSpecies<E> narrow(VectorSpecies<E> s) {
        return VectorSpecies.of(s.elementType(), narrow(s.shape()));
    }

    static IntVector join(VectorSpecies<Integer> from, VectorSpecies<Integer> to, IntVector lo, IntVector hi) {
        assert 2 * from.length() == to.length();

        int vlen = from.length();
        var lo_mask = mask(from, to, 0);

        var v1 = lo.reshape(to);
        var v2 = hi.reshape(to).shiftLanesRight(vlen);
        var r = v2.blend(v1, lo_mask);
        return r;
    }

    static VectorMask<Integer> mask(VectorSpecies<Integer> from, VectorSpecies<Integer> to, int i) {
        int vlen = from.length();
        var v1 = IntVector.broadcast(from, 1);    //                         [1 1 ... 1]
        var v2 = v1.reshape(to);                  // [0 0 ... 0 |   ...     | 1 1 ... 1]
        var v3 = v2.shiftLanesRight(i * vlen);            // [0 0 ... 0 | 1 1 ... 1 | 0 0 ... 0]
        return v3.notEqual(0);                    // [F F ... F | T T ... T | F F ... F]
    }

    static <E> IntVector sum(ByteVector va) {
        VectorSpecies<Integer> species = VectorSpecies.of(Integer.class, va.shape());
        var acc = IntVector.zero(species);
        int limit = va.length() / species.length();
        for (int k = 0; k < limit; k++) {
            var vb = ((IntVector)(va.shiftLanesLeft(k * B64.length()).reshape(B64).cast(species))).and(0xFF);
            acc = acc.add(vb);
        }
        return acc;
    }

    /* ============================================================================================================== */

    boolean[] fillMask(int size, IntFunction<Boolean> f) {
        boolean[] array = new boolean[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = f.apply(i);
        }
        return array;
    }

    byte[] fillByte(int size, IntFunction<Byte> f) {
        byte[] array = new byte[size];
        for (int i = 0; i < size; i++) {
            array[i] = f.apply(i);
        }
        return array;
    }

    int[] fillInt(int size, IntFunction<Integer> f) {
        int[] array = new int[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = f.apply(i);
        }
        return array;
    }

    long[] fillLong(int size, IntFunction<Long> f) {
        long[] array = new long[size];
        for (int i = 0; i < array.length; i++) {
            array[i] = f.apply(i);
        }
        return array;
    }
}
