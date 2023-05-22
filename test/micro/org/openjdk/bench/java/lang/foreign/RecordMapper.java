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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.*;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.lang.foreign.ValueLayout.JAVA_INT;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(org.openjdk.jmh.annotations.Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = { "--enable-preview", "--enable-native-access=ALL-UNNAMED" })
public class RecordMapper {

    private static final GroupLayout POINT_LAYOUT =
            MemoryLayout.structLayout(
                    JAVA_INT.withName("x"),
                    JAVA_INT.withName("y"));

    private static final int ARRAY_SIZE = 8;

    private static final GroupLayout ARRAY_LAYOUT =
            MemoryLayout.structLayout(
                    MemoryLayout.sequenceLayout(ARRAY_SIZE, JAVA_INT)
                            .withName("ints"));

    public record Point(int x, int y){}

    public record Array(int[] ints) {}

    private static final Function<MemorySegment, Point> POINT_MAPPER = POINT_LAYOUT.recordMapper(Point.class);
    private static final Function<MemorySegment, Point> POINT_EXPLICIT_MAPPER = ms ->
            new Point(ms.get(JAVA_INT, 0L), ms.get(JAVA_INT, 4));
    private static final MethodHandle POINT_MH = methodHandle();

    private static final Function<MemorySegment, Array> ARRAY_MAPPER = ARRAY_LAYOUT.recordMapper(Array.class);
    private static final Function<MemorySegment, Array> ARRAY_EXPLICIT_MAPPER = ms -> new Array(ms.toArray(JAVA_INT));

    Arena arena;
    MemorySegment pointSegment;
    MemorySegment arraySegment;

    @Setup
    public void setup() {
        arena = Arena.ofConfined();
        pointSegment = arena.allocate(POINT_LAYOUT);
        var rnd = new Random();
        pointSegment.set(JAVA_INT, 0, rnd.nextInt(10));
        pointSegment.set(JAVA_INT, 4, rnd.nextInt(10));

        int[] ints = rnd.ints(ARRAY_SIZE).toArray();
        // Use native memory
        arraySegment = arena.allocate(ARRAY_LAYOUT);
        for (int i = 0; i < ARRAY_SIZE; i++) {
            arraySegment.setAtIndex(JAVA_INT, i, ints[i]);
        }
    }

    @TearDown
    public void tearDown() {
        arena.close();
    }

    @Benchmark
    public void pointMapper(Blackhole bh) {
        bh.consume(POINT_MAPPER.apply(pointSegment));
    }

    @Benchmark
    public void pointExplicitMapper(Blackhole bh) {
        bh.consume(POINT_EXPLICIT_MAPPER.apply(pointSegment));
    }

    @Benchmark
    public void pointMhMapper(Blackhole bh) {
        try {
            bh.consume((Point) POINT_MH.invokeExact(pointSegment));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void arrayMapper(Blackhole bh) {
        bh.consume(ARRAY_MAPPER.apply(arraySegment));
    }

    @Benchmark
    public void arrayExplicitMapper(Blackhole bh) {
        bh.consume(ARRAY_EXPLICIT_MAPPER.apply(arraySegment));
    }

    static MethodHandle methodHandle() {
        try {
            var lookup = MethodHandles.lookup();
            var ctor = lookup.findConstructor(Point.class, MethodType.methodType(void.class, int.class, int.class));

            var extractorType = MethodType.methodType(int.class, ValueLayout.OfInt.class, long.class);

            var xVh = lookup.findVirtual(MemorySegment.class, "get", extractorType);
            // (MemorySegment, OfInt, long) -> (MemorySegment, long)
            var xVh2 = MethodHandles.insertArguments(xVh, 1, JAVA_INT);
            // (MemorySegment, long) -> (MemorySegment)
            var xVh3 = MethodHandles.insertArguments(xVh2, 1, 0L);

            var yVh = lookup.findVirtual(MemorySegment.class, "get", extractorType);
            // (MemorySegment, OfInt, long) -> (MemorySegment, long)
            var yVh2 = MethodHandles.insertArguments(yVh, 1, JAVA_INT);
            // (MemorySegment, long) -> (MemorySegment)
            var yVh3 = MethodHandles.insertArguments(yVh2, 1, 4L);

            var ctorFilter = MethodHandles.filterArguments(ctor, 0, xVh3);
            var ctorFilter2 = MethodHandles.filterArguments(ctorFilter, 1, yVh3);

            var mt = MethodType.methodType(Point.class, MemorySegment.class);

            return MethodHandles.permuteArguments(ctorFilter2, mt, 0, 0);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
