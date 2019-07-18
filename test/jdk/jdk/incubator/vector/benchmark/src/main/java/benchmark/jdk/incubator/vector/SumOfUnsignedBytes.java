/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
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
package benchmark.jdk.incubator.vector;

import jdk.incubator.vector.*;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// Inspired by "SIMDized sum of all bytes in the array"
//   http://0x80.pl/notesen/2018-10-24-sse-sumbytes.html
//
// C/C++ equivalent: https://github.com/WojciechMula/toys/tree/master/sse-sumbytes
//
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgsPrepend = {"--add-modules=jdk.incubator.vector"})
public class SumOfUnsignedBytes extends AbstractVectorBenchmark {

    @Param({"64", "1024", "4096"})
    int size;

    private byte[] data;

    @Setup
    public void init() {
        size = size + size % 32; // FIXME: process tails
        data = fillByte(size, i -> (byte)(int)i);

        int sum = scalar();
        assertEquals(vectorInt(),   sum);
        assertEquals(vectorShort(), sum);
        //assertEquals(vectorByte(),  sum);
        //assertEquals(vectorSAD(),   sum);
    }

    @Benchmark
    public int scalar() {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i] & 0xFF;
        }
        return sum;
    }

    // 1. 32-bit accumulators
    @Benchmark
    public int vectorInt() {
        final var lobyte_mask = IntVector.broadcast(I256, 0x000000FF);

        var acc = IntVector.zero(I256);
        for (int i = 0; i < data.length; i += B256.length()) {
            var vb = ByteVector.fromArray(B256, data, i);
            var vi = (IntVector)vb.reinterpretAsInts();
            for (int j = 0; j < 4; j++) {
                var tj = vi.lanewise(VectorOperators.LSHR, j * 8).and(lobyte_mask);
                acc = acc.add(tj);
            }
        }
        return (int)Integer.toUnsignedLong(acc.reduceLanes(VectorOperators.ADD));
    }

    // 2. 16-bit accumulators
    @Benchmark
    public int vectorShort() {
        final var lobyte_mask = ShortVector.broadcast(S256, (short) 0x00FF);

        // FIXME: overflow
        var acc = ShortVector.zero(S256);
        for (int i = 0; i < data.length; i += B256.length()) {
            var vb = ByteVector.fromArray(B256, data, i);
            var vs = (ShortVector)vb.reinterpretAsShorts();
            for (int j = 0; j < 2; j++) {
                var tj = vs.lanewise(VectorOperators.LSHR, j * 8).and(lobyte_mask);
                acc = acc.add(tj);
            }
        }

        int mid = S128.length();
        var accLo = ((IntVector)(acc.reinterpretShape(S128, 0).castShape(I256, 0))).and(0xFFFF); // low half as ints
        var accHi = ((IntVector)(acc.reinterpretShape(S128, 1).castShape(I256, 0))).and(0xFFFF); // high half as ints
        return accLo.reduceLanes(VectorOperators.ADD) + accHi.reduceLanes(VectorOperators.ADD);
    }

    /*
    // 3. 8-bit halves (MISSING: _mm_adds_epu8)
    @Benchmark
    public int vectorByte() {
        int window = 256;
        var acc_hi  = IntVector.zero(I256);
        var acc8_lo = ByteVector.zero(B256);
        for (int i = 0; i < data.length; i += window) {
            var acc8_hi = ByteVector.zero(B256);
            int limit = Math.min(window, data.length - i);
            for (int j = 0; j < limit; j += B256.length()) {
                var vb = ByteVector.fromArray(B256, data, i + j);

                var t0 = acc8_lo.add(vb);
                var t1 = addSaturated(acc8_lo, vb); // MISSING
                var overflow = t0.notEqual(t1);

                acc8_lo = t0;
                acc8_hi = acc8_hi.add((byte) 1, overflow);
            }
            acc_hi = acc_hi.add(sum(acc8_hi));
        }
        return sum(acc8_lo)
                .add(acc_hi.mul(256)) // overflow
                .addAll();
    }

    // 4. Sum Of Absolute Differences (SAD) (MISSING: VPSADBW, _mm256_sad_epu8)
    public int vectorSAD() {
        var acc = IntVector.zero(I256);
        for (int i = 0; i < data.length; i += B256.length()) {
            var v = ByteVector.fromArray(B256, data, i);
            var sad = sumOfAbsoluteDifferences(v, ByteVector.zero(B256)); // MISSING
            acc = acc.add(sad);
        }
        return acc.addAll();
    } */

    // Helpers
    /*
    static ByteVector addSaturated(ByteVector va, ByteVector vb) {
        var vc = ByteVector.zero(B256);
        for (int i = 0; i < B256.length(); i++) {
            if ((va.get(i) & 0xFF) + (vb.get(i) & 0xFF) < 0xFF) {
                vc = vc.withLane(i, (byte)(va.get(i) + vb.get(i)));
            } else {
                vc = vc.withLane(i, (byte)0xFF);
            }
        }
        return vc;
    }
    IntVector sumOfAbsoluteDifferences(ByteVector va, ByteVector vb) {
        var vc = ByteVector.zero(B256);
        for (int i = 0; i < B256.length(); i++) {
            if ((va.get(i) & 0xFF) > (vb.get(i) & 0xFF)) {
                vc = vc.withLane(i, (byte)(va.get(i) - vb.get(i)));
            } else {
                vc = vc.withLane(i, (byte)(vb.get(i) - va.get(i)));
            }
        }
        return sum(vc);
    } */
}
