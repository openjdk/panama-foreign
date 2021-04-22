package org.openjdk.bench.jdk.incubator.foreign;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

  private static final int[] POOL_MAX_SIZE;

  static {
    final var rand = new Random(0L);
    final var passes = 1024;
    final var sizeClasses = new int[] {
        24,
        128,
        4096,
        1024*16,
        1024 * 1024 * 2
    };
    // Generate pseudo random sizes
    sizes = new long[passes * sizeClasses.length];

    for (int i = 0; i < passes; i++) {
      for (int j=0; j < sizeClasses.length; j++) {
        sizes[sizeClasses.length * i + j] = Math.max(1, (long) ((rand.nextGaussian() + 1.0) * sizeClasses[j]));
      }
    }

    POOL_MAX_SIZE = new int[Long.SIZE + 1];
    // Use larger sizes for pool, so it will not get exhausted
    Arrays.fill(POOL_MAX_SIZE, 256);
  }

  @Param({"1", "16", "200"})
  public int allocations;

  private MemorySegmentPool pool = new MemorySegmentPool(POOL_MAX_SIZE, ResourceScope.globalScope());
  private MemorySegmentPool poolEmpty = new MemorySegmentPool(new int[Long.SIZE], ResourceScope.globalScope());

  private int i;
  @Setup
  public void setup() {
    i = 0;
    // Preallocate pool
    for (int j = 0; j <= 24; j++) {
      pool.getSegmentEntryBySize(1L << j, 1).close();
    }
  }

  @Benchmark
  public void arena() {
    try (var scope = ResourceScope.newConfinedScope()) {
      final var allocator = SegmentAllocator.arenaAllocator(scope);
      for (int j = 0; j < allocations; j++) {
        final var segment = allocator.allocate(sizes[i]);
        readSegment(segment);
        next();
      }
    }
  }

  @Benchmark
  public void pool_allocator() {
    try (var scope = ResourceScope.newConfinedScope()) {
      final var allocator = pool.allocatorForScope(scope);
      for (int j = 0; j < allocations; j++) {
        final var segment = allocator.allocate(sizes[i]);
        readSegment(segment);
        next();
      }
    }
  }

  @Benchmark
  public void pool_allocator_exhausted() {
    try (var scope = ResourceScope.newConfinedScope()) {
      final var allocator = poolEmpty.allocatorForScope(scope);
      for (int j = 0; j < allocations; j++) {
        final var segment = allocator.allocate(sizes[i]);
        readSegment(segment);
        next();
      }
    }
  }

  @Benchmark
//  @CompilerControl(CompilerControl.Mode.PRINT)
  public void pool_direct() {
    List<MemoryPoolSegment> pooledSegments = new ArrayList<>(allocations);
    for (int j = 0; j < allocations; j++) {
      var s = pool.getSegmentEntryBySize(sizes[i], 1);
      pooledSegments.add(s);
      readSegment(s.memorySegment());
      next();
    }
    pooledSegments.forEach(MemoryPoolSegment::close);
  }
  @Benchmark
//  @CompilerControl(CompilerControl.Mode.PRINT)
  public void malloc_free() {
    List<MemoryAddress> allocatedAddresses = new ArrayList<>(allocations);
    for (int j = 0; j < allocations; j++) {
      var size = sizes[i];
      var a = CLinker.allocateMemory(size);
      var s = a.asSegment(size, ResourceScope.globalScope());
      allocatedAddresses.add(a);
      readSegment(s);
      next();
    }
    allocatedAddresses.forEach(CLinker::freeMemory);
  }

  /**
   * Do read to avoid situation allocator will allocate not mapped memory.
   */
  private void readSegment(MemorySegment s) {
    final var size = (int) s.byteSize();
    for (int idx = 0; idx <  size; idx += 1024) {
//      MemoryAccess.setByteAtOffset(s, l, (byte)0);
      BYTE.set(s, 0, (byte) 1);
    }
  }

  private void next() {
    i = (++i == sizes.length ? 0 : i);
  }
}
