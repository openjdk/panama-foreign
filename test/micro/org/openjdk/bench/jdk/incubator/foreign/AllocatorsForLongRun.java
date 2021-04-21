package org.openjdk.bench.jdk.incubator.foreign;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.MemorySegmentPool;
import jdk.incubator.foreign.MemorySegmentPool.MemoryPoolSegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Allocators performance tests for long running applications.
 *
 * Tries to simulate library which has to allocate number of different size elements.
 *
 * Ofc... thre are some cavets
 * - if pool of segments will be exhausted, pooled allocator will slow down
 * - arena allocator - for long running has to be freed at some point of time...
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(org.openjdk.jmh.annotations.Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = { "--add-modules=jdk.incubator.foreign", "-Dforeign.restricted=permit",
    "--enable-native-access", "ALL-UNNAMED"})
public class AllocatorsForLongRun {
  private static final long[] sizes;

  private static final VarHandle BYTE = MemoryHandles.varHandle(byte.class, 1, ByteOrder.nativeOrder());

  static {
    final var rand = new Random(0L);
    final var passes = 1024;
    final var sizeClasses = 3;
    // Generate pseudo random sizes
    sizes = new long[passes * sizeClasses];

    for (int i = 0; i < passes; i++) {
      sizes[sizeClasses * i + 0] = Math.max(1, (long) (rand.nextGaussian() * 24));
      sizes[sizeClasses * i + 1] = Math.max(1, (long) (rand.nextGaussian() * 128));
      sizes[sizeClasses * i + 2] = Math.max(1, (long) (rand.nextGaussian() * 4096));
//      sizes[sizeClasses * i + 3] = Math.max(1, (long) (rand.nextGaussian() * 1024 * 16));
//      sizes[sizeClasses * i + 4] = Math.max(1, (long) (rand.nextGaussian() * 1024 * 1024 * 2));
    }
  }

  @Param({"1", "16", "200"})
  public int allocations;

  private MemorySegmentPool pool = new MemorySegmentPool(ResourceScope.globalScope());
  private MemorySegmentPool poolEmpty = new MemorySegmentPool(new int[Long.SIZE], ResourceScope.globalScope());

  @Setup
  public void setup() {

  }

  @Benchmark
  public void arena() {
    int i = 0;
    try (var scope = ResourceScope.newConfinedScope()) {
      final var allocator = SegmentAllocator.arenaAllocator(scope);
      for (int j = 0; j < allocations; j++) {
        final var segment = allocator.allocate(sizes[i]);
        readSegment(segment);
        i = next(i);
      }
    }
  }

  @Benchmark
  public void pool_allocator() {
    int i = 0;
    try (var scope = ResourceScope.newConfinedScope()) {
      final var allocator = pool.allocatorForScope(scope);
      for (int j = 0; j < allocations; j++) {
        final var segment = allocator.allocate(sizes[i]);
        readSegment(segment);
        i = next(i);
      }
    }
  }

  @Benchmark
  public void pool_allocator_exhausted() {
    int i = 0;
    try (var scope = ResourceScope.newConfinedScope()) {
      final var allocator = poolEmpty.allocatorForScope(scope);
      for (int j = 0; j < allocations; j++) {
        final var segment = allocator.allocate(sizes[i]);
        readSegment(segment);
        i = next(i);
      }
    }
  }

  @Benchmark
  public void pool_direct() {
    int i = 0;
    // If I would develop with direct segments I would do something like this
    LinkedList<MemoryPoolSegment> pooledSegments = new LinkedList<>();
    for (int j = 0; j < allocations; j++) {
      var s = pool.getSegmentEntryBySize(sizes[i], 1);
      pooledSegments.add(s);
      readSegment(s.memorySegment());
      i = next(i);
    }
    pooledSegments.forEach(MemoryPoolSegment::close);
  }

  @Benchmark
  public void malloc_free() {
    int i = 0;
    // If I would develop with direct segments I would do something like this
    LinkedList<MemoryAddress> allocatedAddresses = new LinkedList<>();
    for (int j = 0; j < allocations; j++) {
      var size = sizes[i];
      var a = CLinker.allocateMemory(size);
      var s = a.asSegment(size, ResourceScope.globalScope());
      allocatedAddresses.add(a);
      readSegment(s);
      i = next(i);
    }
    allocatedAddresses.forEach(CLinker::freeMemory);
  }

  /**
   * Do read to avoid situation allocator will allocate not mapped memory.
   */
  private void readSegment(MemorySegment s) {
    final var size = s.byteSize();
    for (long l = 0; l <  size; l += 256) {
      BYTE.set(s, l, (byte) 1);
    }
  }
  private static int next(int i) {
    return ++i == sizes.length ? 0 : i;
  }
}
