package jdk.incubator.foreign;

import java.util.Arrays;
import jdk.internal.foreign.ResourceScopeImpl;
import jdk.internal.foreign.ResourceScopeImpl.ResourceList.ResourceCleanup;
import jdk.internal.vm.annotation.DontInline;
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
  private final SpinLockQueue<MemoryPoolItem> segmentsDequeue[] = new SpinLockQueue[Long.SIZE + 1];

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

    scope.addCloseAction(new CleanRunnable(this.segmentsDequeue));
  }

  /**
   * Creates new allocator backed by this pool, and bound to given scope.
   *
   * <br />
   *
   * The returned allocator will allocate segment of requested size, firstly by
   * searching for segment in the pool.
   *
   * <br />
   *
   * If the segment can't be found in the pool, a new one is allocated (so, this is unbounded
   * allocator).
   *
   * <br />
   *
   * When associated scope is closed, all allocated entries are put back into pool. If
   * during allocations more entries have been created than configured maximum number of
   * elements of given size class, the allocated segment is freed.
   *
   * <br />
   *
   * There's no guarantee which allocated entries will be put back to pool, and which one
   * freed.
   *
   * @param resourceScope the scope to which allocator should be bounded
   *
   * @return memory allocator backed by this pool and bound to given scope
   */
  @ForceInline
  public SegmentAllocator allocatorForScope(ResourceScope resourceScope) {
    // Prevent scope managing this pool to go away, when dependant allocator is alive
    final var handle = scope.acquire();
//    resourceScope.addCloseAction(() -> scope.release(handle));
    ((ResourceScopeImpl) resourceScope).addOrCleanupIfFail(new ResourceCleanup() {
      @Override
      public void cleanup() {
        scope.release(handle);
      }
    });
    return ((bytesSize, bytesAlignment) -> {
      final var alignedSize = alignSize(bytesSize, bytesAlignment);
      final var segmentEntry = findOrAllocateItemAndPrepareForAllocator(resourceScope, alignedSize);
      // Slicing source segment can be faster than source Address as segment
      return segmentEntry.sourceAddress.asSegment(alignedSize, null, resourceScope);
    });
  }

  ////////////////////////////
  //// Internal methods
  ////////////////////////////

  /**
   * Search and maybe allocate segment bounded by given bucket
   */
  @ForceInline
  private MemoryPoolItem findOrAllocateItem(int bucket) {
    final var segmentDequeue = segmentsDequeue[bucket];

    var segment = segmentDequeue.pollEntry();
    if (segment == null) {
      final var bitBoundedSize = 1L << bucket;
      segment = allocateNewPoolItem(segmentDequeue, bitBoundedSize);
    }
    return segment;
  }

  /**
   * Prepares pooled segment to be returned by allocator from allocatorForScope
   */
  private MemoryPoolItem findOrAllocateItemAndPrepareForAllocator(ResourceScope resourceScope, long alignedSize) {
    int bound = calculateBucket(alignedSize);
    final var segmentEntry = findOrAllocateItem(bound);

    ((ResourceScopeImpl) resourceScope).addOrCleanupIfFail(new ResourceCleanup() {
      @Override
      @ForceInline
      public void cleanup() {
        segmentEntry.close();
      }
    });

    return segmentEntry;
  }

  /**
   * Calculates bit bound, of size - in other words the bucket which should be used for item.
   * @param alignedSize
   */
  @ForceInline
  protected int calculateBucket(long alignedSize) {
    // If 100.., than 100... - 1 -> 01111
    // If 101 -> than 101 - 1 -> 1....

    // 0 -> 64
    // This equation does not allow to allocate more than 2^63, however such memory may require
    // 5 level page cache, so skippable for now
    return 64 - Long.numberOfLeadingZeros(alignedSize - 1);
  }

  /**
   * Allocates and prepares a new item.
   */
  @ForceInline
  private MemoryPoolItem allocateNewPoolItem(SpinLockQueue<MemoryPoolItem> queue, long allocationSize) {
    final var memoryAddress = CLinker.allocateMemory(allocationSize);
    return new MemoryPoolItem(queue, memoryAddress, allocationSize, scope);
  }

  private static void validateMaxSizes(int maxSizes[]) {
    Arrays.stream(maxSizes).filter(i -> i < 0).findAny()
        .ifPresent(i -> {
          throw new IllegalStateException("Invalid max size " + i);
        });
  }

  private static long alignSize(long bytesSize, long bytesAlignment) {
    return (bytesSize + bytesAlignment - 1) & -bytesAlignment;
  }

  /**
   * Represent single item in the pool with related data.
   * <br />
   * In order to return item back to pool close should be called (depending on context either
   * implicite or explicite).
   */
  public static final class MemoryPoolItem extends SpinLockQueue.Entry<MemoryPoolItem> implements AutoCloseable {
    /** The owning queue (bucket) to which this pool item belongs. */
    private final SpinLockQueue<MemoryPoolItem> owner;

    /** Source memory address, it's the start of sourceSegment. */
    private final MemoryAddress sourceAddress;

    @ForceInline
    private MemoryPoolItem(SpinLockQueue<MemoryPoolItem> queue, MemoryAddress sourceAddress, long size, ResourceScope scope) {
      super();
      this.owner = queue;
      this.sourceAddress = sourceAddress;
    }

    /**
     * The memory address representing beginning of this memory item.
     *
     * @return memory address representing beginning of this memory item.
     */
    @ForceInline
    public MemoryAddress memoryAddress() {
      return sourceAddress;
    }

    @Override
    @DontInline
    public void close() {
      if (!this.owner.putEntry(this)) {
        this.release();
      }
    }

    /**
     * Physically releases entry - free underlying memory.
     */
    private void release() {
      // Can be called only once, from close
//      if (RELEASED.compareAndSet(this, false, true)) {
        // Don't use segment here, if scope closed will produce exception
        CLinker.freeMemory(this.sourceAddress);
//      }
    }
  }

  /**
   * Cleaner class has to separate pool class, from objects having strong reference
   * to pool's scope, as in case of implicite scope, such scope may not be closed, due
   * to strong reference trough cleaner method
   */
  private static class CleanRunnable implements Runnable {
    private SpinLockQueue<?>[] queuesToClean;

    CleanRunnable(SpinLockQueue<?>[] queuesToClean) {
      this.queuesToClean = queuesToClean;
    }
    @Override
    public void run() {
      // This method is called from pool's scope close method
      for (int i = 0; i < queuesToClean.length; i++) {
        // After calling this method maxSize is zero, and no new entries can be put back
        @SuppressWarnings("unchecked")
        final var iterator = ((SpinLockQueue<MemoryPoolItem>) queuesToClean[i]).retrieveAndLock();
        while (iterator.hasNext()) {
          iterator.next().close();
        }
      }
      queuesToClean = null;
    }
  }
}
