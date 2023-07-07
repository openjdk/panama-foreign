/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.bench.java.lang.foreign;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import sun.misc.Unsafe;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.TimeUnit;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED;
import static org.openjdk.jmh.annotations.CompilerControl.Mode.*;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(org.openjdk.jmh.annotations.Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = {"--enable-preview", "--enable-native-access=ALL-UNNAMED"})
public class MemorySegmentCopyALL {

    static final Unsafe UNSAFE = Utils.unsafe;

    static final int ELEM_SIZE = 10;
    static final int CARRIER_SIZE = (int)JAVA_INT.byteSize();
    static final int BYTE_SIZE = ELEM_SIZE * CARRIER_SIZE;
    static final MemorySegment ALL = MemorySegment.NULL.reinterpret(Long.MAX_VALUE);
    static final int UNSAFE_INT_OFFSET = UNSAFE.arrayBaseOffset(int[].class);

    long unsafe_src;
    long unsafe_dst;
    int[] ints;

    @Setup
    public void setup() {
        unsafe_src = UNSAFE.allocateMemory(BYTE_SIZE);
        unsafe_dst = UNSAFE.allocateMemory(BYTE_SIZE);
        ints = new int[ELEM_SIZE];

        for (int i = 0; i < ints.length ; i++) {
            ints[i] = i;
            ALL.set(JAVA_INT, unsafe_src, i);
        }
    }

    @TearDown
    public void TearDown() {
        UNSAFE.freeMemory(unsafe_src);
        UNSAFE.freeMemory(unsafe_dst);
    }

    @Benchmark
    public void panamam_array_to_ALL() {
        MemorySegment.copy(ints, 0, ALL, JAVA_INT_UNALIGNED, unsafe_dst, ELEM_SIZE);
    }

    @Benchmark
    public void panamam_ALL_to_array() {
        MemorySegment.copy(ALL, JAVA_INT_UNALIGNED, unsafe_src, ints, 0, ELEM_SIZE);
    }

    @Benchmark
    public void panamam_ALL_to_ALL() {
        MemorySegment.copy(ALL, JAVA_INT_UNALIGNED, unsafe_src, ALL, JAVA_INT_UNALIGNED, unsafe_dst, ELEM_SIZE);
    }

    @Benchmark
    public void unsafe_array_to_addr() {
        UNSAFE.copyMemory(ints, UNSAFE_INT_OFFSET, null, unsafe_dst, BYTE_SIZE);
    }

    @Benchmark
    public void unsafe_addr_to_array() {
        UNSAFE.copyMemory(null, unsafe_src, ints, UNSAFE_INT_OFFSET, BYTE_SIZE);
    }

    @Benchmark
    public void unsafe_addr_to_addr() {
        UNSAFE.copyMemory(null, unsafe_src, null, unsafe_dst, BYTE_SIZE);
    }
}
