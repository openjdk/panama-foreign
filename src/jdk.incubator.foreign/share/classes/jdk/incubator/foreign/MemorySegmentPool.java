package jdk.incubator.foreign;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import jdk.internal.foreign.ResourceScopeImpl;
import jdk.internal.foreign.ResourceScopeImpl.ResourceList.ResourceCleanup;
import jdk.internal.vm.annotation.ForceInline;

/**
 * Memory segments pool, maintaining pools of segments of size equal to power of 2 with ability to
 * expose {@link SegmentAllocator}.
 *
 * <h1>Overview</h1>
 * <br />
 * This pool can be used by long running & highly performant code which requires frequent allocations
 * of memory segments (typically having small size) to communicate with native API.
 * <br />
 * An example, can be I/O layer which use temporary buffers for read and write.
 *
 * <h1>Segment allocators</h1>
 * This pool can provide segment allocator which can be backed by pool. All allocations
 * will be directed to the pool, and on {@link ResourceScope} close, the allocated segments
 * will be put back to pool.
 * <br />
 * Please note: depending on resource scope close strategy there can be a daley in putting
 * segments back to the pool.
 *
 * <h1>Fast entry API</h1>
 * <b>
 *   Note: this API is for advanced applications, as incorrect usage can lead to memory leaks,
 *   or memory corruption.
 * </b>
 *
 * <br />
 *
 * In order to mitigate overhead related to managing segments by the {@link @ResourceScope} and
 * {@link SegmentAllocator} high performant applications can use fast entry methods. The
 * caller can get, directly, entry which contains memory segment bound to pool's scope. Entry should
 * be returned to pool witch
 * <pre>
 *   try (final var entry = memoryPool.getSegmentEntryBySize(len, 8)) {
 *     final var segment = entry.memorySegment();
 *     // do something with segment
 *   }
 * </pre>
 *
 * <h1>Memory allocation strategy</h1>
 * This pool allocates and manages set of segments of size being power of 2. If the request
 * is made for a segment of size S (size calculated with alignment bytes), than smallest segment
 * meeting both constraints is returned.
 *
 * <br />
 *
 * The pool manages segments in LIFO order, to increase page table, cache hits.
 *
 * <br />
 *
 * Segments are kept in buckets of size 2^n. For each bucket the maximum number of elements
 * can be set.
 *
 * <br />
 *
 * When the pool can't provide segment from bucket the new segment is created, there's no upper
 * bound.
 *
 * <br />
 *
 * When the segment is returned back to pool, pool checks if the bucket size is less than maximum.
 * In such a case segment entry is put back, otherwise it is deallocated. This check is may not
 * be atomic with put.
 *
 * <br />
 * Please note: that this pool can allocate segments of larger size than requested
 * (almost 2x as requested), if for small it can be ok, than for larger segments it can be
 * risky. I. e. request for segment for 1MB (2^20), will result in segment of 1MB, however requesting for
 * segment of size 1MB + 1(2^20 + 1), will result in segment of size 2^21). However in case of
 * 1GB + 1, pool will allocate 2GB of memory.
 */
public class MemorySegmentPool {
  private static final int[] DEFAULT_MAX_SIZES = new int[Long.SIZE + 1];
  private static final ResourceScope GLOBAL = ResourceScope.globalScope();

  /**
   * Last element to hold 0 size and negative sizes (fallback)
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private final SpinLockQueue<MemoryPoolSegment> segmentsDequeue[] = new SpinLockQueue[Long.SIZE + 1];

  private final ResourceScope scope;

  static {
    int idx = 0;
    int cores = Runtime.getRuntime().availableProcessors();

    // Up to 1kb
    for (; idx <= 10; idx++) {
      DEFAULT_MAX_SIZES[idx] = 256;
    }

    // Sizes up to 64kb
    for (; idx <= 16; idx++) {
      DEFAULT_MAX_SIZES[idx] = cores * 4;
    }

    // Sizes up to 1MB {
    for (; idx <= 20; idx++) {
      DEFAULT_MAX_SIZES[idx] = cores;
    }

    DEFAULT_MAX_SIZES[idx++] = Math.min(cores, 2); //2mb
    DEFAULT_MAX_SIZES[idx++] = Math.min(cores, 1); //4mb
    // Rest 0
  }

  public MemorySegmentPool(ResourceScope scope) {
    this(DEFAULT_MAX_SIZES, scope);
  }

  /**
   * Constructs new pool with specified maximum number of elements per bucket.
   *
   * There's {@link Long#SIZE} + 1 buckets, while last bucket is not used,
   * as allocations with size 0 or with highest bit set to 1 goes there.
   *
   * @param maxSizes the array of maximum sizes per segment bucket
   * @param scope the scope to which this allocator should be bound
   */
  public MemorySegmentPool(int maxSizes[], ResourceScope scope) {
    this.scope = scope;

    validateMaxSizes(maxSizes);
    for (int i=0; i < segmentsDequeue.length; i++) {
      var segmentsBucketMaxSize = i < maxSizes.length ? maxSizes[i] : 0;
      segmentsDequeue[i] = new SpinLockQueue<>(segmentsBucketMaxSize);
    }

    scope.addOnClose(this::freePool);
  }

  public SegmentAllocator allocatorForScope(ResourceScope resourceScope) {
    // Prevent scope managing this pool to go away, when dependant allocator is alive
    final var handle = scope.acquire();
    resourceScope.addOnClose(handle::close);
    return (bytesSize, bytesAlignment) -> getAsNewSegmentWithScope(resourceScope, bytesSize, bytesAlignment);
  }

  @ForceInline
  public MemoryPoolSegment getSegmentEntryByLayout(MemoryLayout layout) {
    return getSegmentEntryBySize(layout.byteSize(), layout.byteAlignment());
  }

  /**
   * Gets segment from pool or allocates new one. Internally segments are cached.
   * The size of segment can be larger than requested.
   *
   * @param size the size of segment.
   *
   * @return segment of size at least `size`
   */
//  @ForceInline
  public MemoryPoolSegment getSegmentEntryBySize(long size, long alignment) {
//    if (!scope.isAlive()) {
//      throw new IllegalStateException("Associated resource scope is closed");
//    }

    final var bitBound = bitBound(size, alignment);
    MemoryPoolSegment segment = getMemoryPoolSegment(bitBound);

    return segment;
  }

  @ForceInline
  private MemoryPoolSegment getMemoryPoolSegment(int bitBound) {
    final var segmentDequeue = segmentsDequeue[bitBound];

    var segment = segmentDequeue.pollEntry();
    if (segment == null) {
      final var bitBoundedSize = 1L << bitBound;
      segment = allocateNewEntry(segmentDequeue, bitBoundedSize);
//      segment.memorySegment = (NativeMemorySegmentImpl) segment.memoryAddress.asSegment(bitBoundedSize, scope);
//      segment.size = bitBoundedSize;
    }
    return segment;
  }

  @ForceInline
  private MemorySegment getAsNewSegmentWithScope(ResourceScope resourceScope, long size, long alignment) {
    final var bitBound = bitBound(size, alignment);
    final var segmentEntry = getMemoryPoolSegment(bitBound);

    ((ResourceScopeImpl) resourceScope).addOrCleanupIfFail(new ResourceCleanup() {
      @Override
      public void cleanup() {
        segmentEntry.close();
      }
    });

    return segmentEntry.memoryAddress.asSegment(1L << bitBound, resourceScope);
  }



  @ForceInline
  private static int bitBound(long alignedSize) {
    // If 100.., than 100... - 1 -> 01111
    // If 101 -> than 101 - 1 -> 1....

    // 0 -> 64
    // This equation does not allow to allocate more than 2^63, however such memory may require
    // 5 level page cache, so skippable for now
    return 64 - Long.numberOfLeadingZeros(alignedSize - 1);
  }

  @ForceInline
  private static int bitBound(long size, long alignment) {
    final var alignedSize = (size + alignment - 1) & -alignment;

    return bitBound(alignedSize);
  }

  @ForceInline
  private MemoryPoolSegment allocateNewEntry(SpinLockQueue<MemoryPoolSegment> queue, long allocationSize) {
    final var memoryAddress = CLinker.allocateMemory(allocationSize);
    return new MemoryPoolSegment(queue, memoryAddress, allocationSize, scope);
  }

  /**
   * Free all elements associated with pool
   */
  private void freePool() {
    // This method is called from pool's scope close method
    for (int i = 0; i < segmentsDequeue.length; i++) {
      // After calling this method maxSize is zero, and no new entries can be put back
      // Entries are released using cleaner attached to pool's scope
      segmentsDequeue[i].retrieveAndLock();
    }
  }

  private static void validateMaxSizes(int maxSizes[]) {
    Arrays.stream(maxSizes).filter(i -> i < 0).findAny()
        .ifPresent(i -> {
          throw new IllegalStateException("Invalid max size " + i);
        });
  }

  public static class MemoryPoolSegment extends SpinLockQueue.Entry<MemoryPoolSegment> implements AutoCloseable {
    private final MemoryAddress memoryAddress;
    private final MemorySegment memorySegment;
    private volatile boolean released;
    long size;

    private final static VarHandle RELEASED;

    static {
      try {
        RELEASED = MethodHandles.lookup().findVarHandle(MemoryPoolSegment.class, "released", boolean.class);
      } catch (Exception e) {
        throw new ExceptionInInitializerError(e);
      }
    }
    @ForceInline
    private MemoryPoolSegment(SpinLockQueue<MemoryPoolSegment> queue, MemoryAddress memoryAddress, long size, ResourceScope scope) {
      super(queue);
      this.memoryAddress = memoryAddress;
      this.memorySegment = memoryAddress.asSegment(size, scope);

      ((ResourceScopeImpl) scope).addOrCleanupIfFail(new ResourceCleanup() {
        @Override
        public void cleanup() {
          release();
        }
      });
    }

    @ForceInline
    public MemorySegment memorySegment() {
      return memorySegment;
    }

    @Override
//    @ForceInline
    public void close() {
      if (!this.owner.putEntry(this)) {
        this.release();
      }
    }

    @ForceInline
    private void release() {
      if (RELEASED.compareAndSet(this, false, true)) {
        // Don't use segment here, if scope closed will produce exception
        CLinker.freeMemory(this.memoryAddress);
      }
    }
  }

}
