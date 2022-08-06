package org.openjdk.bench.jdk.incubator.vector;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import static org.openjdk.jmh.annotations.CompilerControl.Mode.DONT_INLINE;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

@BenchmarkMode(AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsPrepend = {"--add-modules=jdk.incubator.vector", "--enable-preview"})
public class MixedAccessBenchmarks {
    private static final VectorSpecies<Byte> SPECIES_BYTE = VectorSpecies.ofLargestShape(byte.class);

    private MemorySegment heapIn, heapOu, directIn, directOu;
    @Param("1048576")
    private int size;

    @Setup
    public void setup() {
        var session = MemorySession.openConfined();
        heapIn = MemorySegment.ofArray(new byte[size]);
        heapOu = MemorySegment.ofArray(new byte[size]);

        directIn = MemorySegment.allocateNative(size, session);
        directOu = MemorySegment.allocateNative(size, session);
    }

    @Benchmark
    public void directCopy() {
        copyMemorySegments1(directIn, directOu);
    }

    @Benchmark
    public void pollutedAccessCopy() {
        copyMemorySegments1(heapIn, heapOu);
        copyMemorySegments1(directIn, directOu);
    }

    @CompilerControl(DONT_INLINE)
    private static void copyMemorySegments1(MemorySegment in, MemorySegment out) {
        long sz = in.byteSize();
        for (long i = 0; i < SPECIES_BYTE.loopBound(sz); i += SPECIES_BYTE.vectorByteSize()) {
            var v1 = ByteVector.fromMemorySegment(SPECIES_BYTE, in, i, ByteOrder.nativeOrder());
            v1.intoMemorySegment(out, i, ByteOrder.nativeOrder());
        }
    }
}
