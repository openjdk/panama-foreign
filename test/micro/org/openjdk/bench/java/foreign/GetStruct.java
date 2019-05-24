/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.bench.java.foreign;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.foreign.Scope;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(org.openjdk.jmh.annotations.Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class GetStruct {

    @NativeStruct("[u16u16u16u16u16u16u16(wSecond)u16](MyStruct)")
    public static interface MyStruct extends Struct<MyStruct> {
        @NativeGetter("wSecond")
        short wSecond$get();
    }

    private Scope scope;
    private static final LayoutType<MyStruct> layout = LayoutType.ofStruct(MyStruct.class); // constant
    private static final MethodHandle getter = layout.getter();
    private Pointer<MyStruct> ptrStruct;
    private MyStruct struct;

    @Setup(Level.Trial)
    public void setup() {
        scope = Scope.globalScope().fork();
        ptrStruct = scope.allocate(layout);
        struct = ptrStruct.get();
    }

    private static native short get_field(long struct_addr);

    static {
        System.loadLibrary("GetStruct");
    }

    @Benchmark
    public short jni_baseline() throws ReflectiveOperationException {
        return get_field(ptrStruct.addr());
    }

    @Benchmark
    public short panama_get_both() {
        return ptrStruct.get().wSecond$get();
    }
    
    @Benchmark
    public short panama_get_both_unwrapped() throws Throwable {
        return ((MyStruct) ((Struct<?>) getter.invokeExact(ptrStruct))).wSecond$get();
    }

    @Benchmark
    public short panama_get_fieldonly() {
        return struct.wSecond$get();
    }

    @Benchmark
    public Struct<?> panama_get_structonly() {
        // if inlining of getter occurs, we should see the same perf as '_unwrapped'
        return ptrStruct.get();
    }

    @Benchmark
    public Struct<?> panama_get_structonly_unwrapped() throws Throwable {
        return (Struct<?>) getter.invokeExact(ptrStruct);
    }
}