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

    public record Point(int x, int y){}

    private static final Function<MemorySegment, Point> MAPPER = POINT_LAYOUT.recordMapper(Point.class);
    private static final Function<MemorySegment, Point> EXPLICIT_MAPPER = ms ->
            new Point(ms.get(JAVA_INT, 0L), ms.get(JAVA_INT, 4));

    Arena arena;
    MemorySegment segment;

    @Setup
    public void setup() {
        arena = Arena.ofConfined();
        segment = arena.allocate(POINT_LAYOUT);
        var rnd = new Random();
        segment.set(JAVA_INT, 0, rnd.nextInt(10));
        segment.set(JAVA_INT, 4, rnd.nextInt(10));
    }

    @TearDown
    public void tearDown() {
        arena.close();
    }

    @Benchmark
    public void mapper(Blackhole bh) {
        bh.consume(MAPPER.apply(segment));
    }

    @Benchmark
    public void explicitMapper(Blackhole bh) {
        bh.consume(EXPLICIT_MAPPER.apply(segment));
    }

}
