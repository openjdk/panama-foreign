/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.openjdk.bench.java.lang.foreign;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.foreign.ValueLayout.*;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * This benchmark creates an array of longs with random contents. The array
 * is then copied into a byte array (using little endian) using different
 * methods.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(org.openjdk.jmh.annotations.Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = {"--enable-native-access=ALL-UNNAMED", "--enable-preview"})
public class MemorySegmentVsBits {

    Arena arena = Arena.openConfined();

    @Param({"16", "64", "256"})
    public int size;
    private long[] longs;
    private byte[] bytes;
    private MemorySegment segment;
    private MemorySegment nativeSegment;

    private static final ValueLayout.OfLong OF_LONG = (JAVA_LONG.order() == BIG_ENDIAN)
            ? JAVA_LONG.withOrder(LITTLE_ENDIAN)
            : JAVA_LONG;

    @Setup
    public void setup() {
        longs = ThreadLocalRandom.current().longs(size).toArray();
        bytes = new byte[size * Long.BYTES];
        segment = MemorySegment.ofArray(bytes);
        nativeSegment = arena.allocate(size * Long.BYTES);
    }

    @TearDown
    public void tearDown() {
        arena.close();
    }

    @Benchmark
    public void bitsEquivalent() {
        for (int i = 0; i < size; i++) {
            putLong(bytes, i * Long.BYTES, longs[i]);
        }
    }

    @Benchmark
    public void panamaHeap() {
        //         for (int i = 0, o = 0; i < size; i++, o += 8) {
        for (int i = 0; i < size; i++) {
            segment.set(JAVA_LONG_UNALIGNED, i * Long.BYTES, longs[i]);
        }
    }

    @Benchmark
    public void panamaNative() {
        for (int i = 0; i < size; i++) {
            nativeSegment.set(OF_LONG, i * Long.BYTES, longs[i]);
        }
    }

    @Benchmark
    public void panamaNativeUnaligned() {
        for (int i = 0; i < size; i++) {
            nativeSegment.set(JAVA_LONG_UNALIGNED, i * Long.BYTES, longs[i]);
        }
    }

    // java.nio.Bits is package private
    static void putLong(byte[] b, int off, long val) {
        b[off + 7] = (byte) (val);
        b[off + 6] = (byte) (val >>> 8);
        b[off + 5] = (byte) (val >>> 16);
        b[off + 4] = (byte) (val >>> 24);
        b[off + 3] = (byte) (val >>> 32);
        b[off + 2] = (byte) (val >>> 40);
        b[off + 1] = (byte) (val >>> 48);
        b[off] = (byte) (val >>> 56);
    }

    static long getLong(byte[] b, int off) {
        return ((b[off + 7] & 0xFFL)) +
                ((b[off + 6] & 0xFFL) << 8) +
                ((b[off + 5] & 0xFFL) << 16) +
                ((b[off + 4] & 0xFFL) << 24) +
                ((b[off + 3] & 0xFFL) << 32) +
                ((b[off + 2] & 0xFFL) << 40) +
                ((b[off + 1] & 0xFFL) << 48) +
                (((long) b[off]) << 56);
    }

}
