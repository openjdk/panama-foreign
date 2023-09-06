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
import java.lang.foreign.MemorySegment;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(org.openjdk.jmh.annotations.Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = { "--enable-native-access=ALL-UNNAMED" })
public class AllocFromSliceTest extends CLayouts {

    @Param({"5", "20", "100", "500", "1000"})
    public int size;
    public int start;
    public byte[] arr;

    @Setup
    public void setup() {
        arr = new byte[1024];
        Random random = new Random(0);
        random.nextBytes(arr);
        start = random.nextInt(1024 - size);
    }

    @Benchmark
    public MemorySegment alloc_confined() {
        Arena arena = Arena.ofConfined();
        MemorySegment segment = arena.allocate(size);
        MemorySegment.copy(arr, start, segment, C_CHAR, 0, size);
        arena.close();
        return segment;
    }

    @Benchmark
    public MemorySegment alloc_confined_slice() {
        Arena arena = Arena.ofConfined();
        MemorySegment segment = arena.allocateFrom(C_CHAR, MemorySegment.ofArray(arr), C_CHAR, start, size);
        arena.close();
        return segment;
    }
}
