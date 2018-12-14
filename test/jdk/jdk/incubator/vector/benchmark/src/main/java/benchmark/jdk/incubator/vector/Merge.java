package benchmark.jdk.incubator.vector;

import jdk.incubator.vector.*;
import jdk.incubator.vector.IntVector.IntSpecies;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgsPrepend = {"--add-modules=jdk.incubator.vector"})
public class Merge extends AbstractVectorBenchmark {

    @Param({"64", "1024", "65536"})
    int size;

    int[] in, out;

    @Setup
    public void setup() {
        size = size + (size % 64); // FIXME: process tails
        in  = new int[size];
        out = new int[size];
        for (int i = 0; i < size; i++) {
            in[i] = i;
        }
    }

    @Benchmark
    public void merge64_128() {
        merge(I64, I128);
    }

    @Benchmark
    public void merge128_256() {
        merge(I128, I256);
    }

    @Benchmark
    public void merge256_512() {
        merge(I256, I512);
    }

    @Benchmark
    public void merge64_256() {
        merge(I64, I256);
    }

    @Benchmark
    public void merge128_512() {
        merge(I128, I512);
    }

    @Benchmark
    public void merge64_512() {
        merge(I64, I256);
    }

    IntVector merge(IntSpecies from, IntSpecies to, int idx) {
        assert from.length() <= to.length();

        int vlenFrom = from.length();
        int vlenTo   =   to.length();

        if (vlenFrom == vlenTo) {
            return from.fromArray(in, idx);
        } else {
            var stepDown = (IntSpecies) narrow(to);
            int mid = stepDown.length();
            var lo = merge(from, stepDown, idx);
            var hi = merge(from, stepDown, idx + mid);
            return join(stepDown, to, lo, hi);
        }
    }


    void merge(IntSpecies from, IntSpecies to) {
        int vlenTo = to.length();
        for (int i = 0; i < in.length; i += vlenTo) {
            var r = merge(from, to, i);
            r.intoArray(out, i);
        }
    }

    @TearDown
    public void tearDown() {
        assertArrayEquals(in, out);
    }
}
