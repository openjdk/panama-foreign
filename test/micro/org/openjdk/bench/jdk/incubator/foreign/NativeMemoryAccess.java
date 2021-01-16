/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

package org.openjdk.bench.jdk.incubator.foreign;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Simulates & benchmarks random access from users (i.e. to off-heap array).
 * Baselined to plain Java arrays.
 */
@Fork(
    value = 3,
    jvmArgsAppend = { "--add-modules", "jdk.incubator.foreign", "-Dforeign.restricted=permit"}
)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class NativeMemoryAccess {
    static final MemorySegment ms = MemorySegment.ofNativeRestricted();
    static final VarHandle intHandle = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());

    MemorySegment allocatedSegment;
    int[] intData = new int[12];
    volatile int intDataOffset = 0;

    volatile MemoryAddress address;
    volatile long addressRaw;

    @Setup
    public void setup() {
        var ms = MemorySegment.allocateNative(256);
        address = ms.address();
        addressRaw = address.toRawLongValue();
        allocatedSegment = ms;
    }

    @TearDown
    public void tearDown() {
        allocatedSegment.close();
        allocatedSegment = null;
    }

    @Benchmark
    public void target(Blackhole bh) {
        int[] local = intData;
        int localOffset = intDataOffset;
        bh.consume(local[localOffset]);
        bh.consume(local[localOffset + 1]);
    }

    @Benchmark
    public void foreignAddress(Blackhole bh) {
        var a = address;
        bh.consume((int) intHandle.get(ms, a.addOffset(0).toRawLongValue()));
        bh.consume((int) intHandle.get(ms, a.addOffset(4).toRawLongValue()));
    }

    @Benchmark
    public void foreignAddressRaw(Blackhole bh) {
        var a = addressRaw;
        bh.consume((int) intHandle.get(ms, a));
        bh.consume((int) intHandle.get(ms, a + 4));
    }
}
