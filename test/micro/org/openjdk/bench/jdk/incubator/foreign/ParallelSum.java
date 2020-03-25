/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

package org.openjdk.bench.jdk.incubator.foreign;

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.SequenceLayout;
import sun.misc.Unsafe;
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

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import java.lang.invoke.VarHandle;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static jdk.incubator.foreign.MemoryLayout.PathElement.sequenceElement;
import static jdk.incubator.foreign.MemoryLayouts.JAVA_INT;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(org.openjdk.jmh.annotations.Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
public class ParallelSum {

    final static int CARRIER_SIZE = 4;
    final static int ALLOC_SIZE = CARRIER_SIZE * 1024 * 1024 * 256;
    final static int ELEM_SIZE = ALLOC_SIZE / CARRIER_SIZE;
    static final VarHandle VH_int = MemoryLayout.ofSequence(JAVA_INT).varHandle(int.class, sequenceElement());

    final static SequenceLayout SEQUENCE_LAYOUT = MemoryLayout.ofSequence(ELEM_SIZE, MemoryLayouts.JAVA_INT);

    static final Unsafe unsafe = Utils.unsafe;

    MemorySegment segment;
    long address;

    ForkJoinPool pool = (ForkJoinPool) Executors.newWorkStealingPool();


    @Setup
    public void setup() {
        address = unsafe.allocateMemory(ALLOC_SIZE);
        for (int i = 0; i < ELEM_SIZE; i++) {
            unsafe.putInt(address + (i * CARRIER_SIZE), i);
        }
        segment = MemorySegment.allocateNative(ALLOC_SIZE);
        for (int i = 0; i < ELEM_SIZE; i++) {
            VH_int.set(segment.baseAddress(), (long) i, i);
        }
    }

    @TearDown
    public void tearDown() throws Throwable {
        unsafe.freeMemory(address);
        segment.close();
        pool.shutdown();
        pool.awaitTermination(60, TimeUnit.SECONDS);
    }

    @Benchmark
    public int segment_serial() {
        int res = 0;
        MemoryAddress base = segment.baseAddress();
        for (int i = 0; i < ELEM_SIZE; i++) {
            res += (int)VH_int.get(base, (long) i);
        }
        return res;
    }

    @Benchmark
    public int unsafe_serial() {
        int res = 0;
        for (int i = 0; i < ELEM_SIZE; i++) {
            res += unsafe.getInt(address + (i * CARRIER_SIZE));
        }
        return res;
    }

    @Benchmark
    public int segment_parallel() {
        return pool.invoke(new SumSegment(segment.spliterator(SEQUENCE_LAYOUT)));
    }

    @Benchmark
    public int segment_stream_parallel() {
        return StreamSupport.stream(segment.spliterator(SEQUENCE_LAYOUT), true)
                .reduce(0, ParallelSum::doWork, Integer::sum);
    }

    static int doWork(int start, MemorySegment segment) {
        int sum = start;
        MemoryAddress base = segment.baseAddress();
        int length = (int)segment.byteSize();
        for (int i = 0 ; i < length / CARRIER_SIZE ; i++) {
            sum += (int)VH_int.get(base, (long)i);
        }
        return sum;
    }

    @Benchmark
    public int unsafe_parallel() {
        return pool.invoke(new SumUnsafe(address, 0, ALLOC_SIZE));
    }

    static class SumUnsafe extends RecursiveTask<Integer> {

        final static int SPLIT_THRESHOLD = 4 * 1024 * 8;

        private final long address;
        private final int start, length;

        SumUnsafe(long address, int start, int length) {
            this.address = address;
            this.start = start;
            this.length = length;
        }

        @Override
        protected Integer compute() {
            if (length > SPLIT_THRESHOLD) {
                SumUnsafe s1 = new SumUnsafe(address, start, length / 2);
                SumUnsafe s2 = new SumUnsafe(address, length / 2, length / 2);
                s1.fork();
                s2.fork();
                return s1.join() + s2.join();
            } else {
                int res = 0;
                for (int i = 0; i < length; i += CARRIER_SIZE) {
                    res += unsafe.getInt(start + address + i);
                }
                return res;
            }
        }
    }

    static class SumSegment extends RecursiveTask<Integer> {

        final static int SPLIT_THRESHOLD = 1024 * 8;

        private final Spliterator<MemorySegment> splitter;
        private int result;

        SumSegment(Spliterator<MemorySegment> splitter) {
            this.splitter = splitter;
        }

        @Override
        protected Integer compute() {
            if (splitter.estimateSize() > SPLIT_THRESHOLD) {
                SumSegment sub = new SumSegment(splitter.trySplit());
                sub.fork();
                return compute() + sub.join();
            } else {
                splitter.tryAdvance(this::doWork);
                return result;
            }
        }

        void doWork(MemorySegment segment) {
            int sum = 0;
            MemoryAddress base = segment.baseAddress();
            int length = (int)segment.byteSize();
            for (int i = 0; i < length / CARRIER_SIZE; i++) {
                sum += (int) VH_int.get(base, (long) i);
            }
            result = sum;
        }
    }
}
