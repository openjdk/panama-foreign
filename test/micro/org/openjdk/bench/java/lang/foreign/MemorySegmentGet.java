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

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.annotations.CompilerControl;

import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import static java.lang.foreign.ValueLayout.*;
import static org.openjdk.jmh.annotations.CompilerControl.Mode.*;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(org.openjdk.jmh.annotations.Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = { "--enable-preview", "--enable-native-access=ALL-UNNAMED" })
public class MemorySegmentGet extends JavaLayouts {

    static final Unsafe unsafe = Utils.unsafe;
    static final int ALLOC_SIZE = (int)JAVA_INT.byteSize();
    static final MemorySegment ALL = MemorySegment.NULL.reinterpret(Long.MAX_VALUE);

    Arena arena;
    MemorySegment segment;
    long unsafe_addr;
    ByteBuffer byteBuffer;

    @Setup
    public void setup() {
        unsafe_addr = unsafe.allocateMemory(ALLOC_SIZE);
        unsafe.putInt(unsafe_addr, 42);
        arena = Arena.ofConfined();
        segment = arena.allocate(ALLOC_SIZE, 1);
        segment.set(JAVA_INT, 0L, 42);
        byteBuffer = ByteBuffer.allocateDirect(ALLOC_SIZE).order(ByteOrder.nativeOrder());
        byteBuffer.putInt(0, 42);
    }

    @TearDown
    public void tearDown() {
        arena.close();
        unsafe.invokeCleaner(byteBuffer);
        unsafe.freeMemory(unsafe_addr);
    }

    @Benchmark
    public int unsafe() {
        return unsafe.getInt(unsafe_addr);
    }

    @Benchmark
    public int segment_VH() {
        return (int) VH_INT.get(segment, 0L);
    }

    @Benchmark
    public int segment_VH_unaligned() {
        return (int) VH_INT_UNALIGNED.get(segment, 0L);
    }

    @Benchmark
    public int segment_get() {
        return segment.get(JAVA_INT, 0L);
    }

    @Benchmark
    public int segment_get_unaligned() {
        return segment.get(JAVA_INT_UNALIGNED, 0L);
    }

    @Benchmark
    public int segment_ALL() {
        return ALL.get(JAVA_INT, unsafe_addr);
    }

    @Benchmark
    public int segment_ALL_unaligned() {
        return ALL.get(JAVA_INT_UNALIGNED, unsafe_addr);
    }

    @Benchmark
    public int BB() {
        return byteBuffer.getInt(0);
    }

}
