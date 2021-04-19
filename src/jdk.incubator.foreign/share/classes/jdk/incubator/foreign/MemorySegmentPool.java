package jdk.incubator.foreign;

import java.util.Arrays;
import jdk.incubator.foreign.SpinLockQueue.Entry;

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
 * In order to mitigate overhead related to managing segments by the {@link @ResourceScope} high
 * performant applications can use fast entry methods.
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
 * Segments are kept in buckets of size log(2^n). For each bucket the maximum number of elements
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
 * In such a case segment entry is put back, otherwise it is deallocated. This check is not atomic
 * with put (due to performance reasons), however low probable queue finally can contain
 * more elements than max.
 *
 * <br />
 * Please note: that this pool can allocate segments of larger size than requested
 * (almost 2x as requested), if for small it can be ok, than for larger segments it can be
 * risky. I. e. request for segment for 1MB (2^20), will result in segment of 1MB, however requesting for
 * segment of size 1MB + 1(2^20 + 1), will result in segment of size 2^21). However in case of
 * 1GB + 1, pool will allocate 2GB of memory.
 */
public class MemorySegmentPool {
  private static final int[] DEFAULT_MAX_SIZES = new int[Long.SIZE];

  @SuppressWarnings({"rawtypes", "unchecked"})
  private final SpinLockQueue<MemorySegment> segmentsDequeue[] = new SpinLockQueue[Long.SIZE];
  private final int maxSizes[] = new int[Long.SIZE];

  private final ResourceScope scope;

  static {
    int idx = 0;
    int cores = Runtime.getRuntime().availableProcessors();

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
  public MemorySegmentPool(int maxSizes[], ResourceScope scope) {
    this.scope = scope;

    for (int i=0; i < segmentsDequeue.length; i++) {
      segmentsDequeue[i] = new SpinLockQueue<>();
    }
    System.arraycopy(maxSizes, 0, this.maxSizes, 0, Math.min(maxSizes.length, this.maxSizes.length));
    validateMaxSizes();
  }

  public SegmentAllocator allocatorForScope(ResourceScope resourceScope) {
    // Prevent scope managing this pool to go away, when dependant allocator is alive
    final var handle = scope.acquire();
    resourceScope.addOnClose(handle::close);
    return (bytesSize, bytesAlignment) -> getSegmentForScope(resourceScope, bytesSize, bytesAlignment);
  }

  public Entry<MemorySegment> getSegmentEntryByLayout(MemoryLayout layout) {
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
  public Entry<MemorySegment> getSegmentEntryBySize(long size, long alignment) {
    if (!scope.isAlive()) {
      throw new IllegalStateException("Associated resource scope is closed");
    }

    final var alignedSize = (size + alignment - 1) & -alignment;
    final var bitBound = bitBound(alignedSize);

    final var segmentDequeue = segmentsDequeue[bitBound];
    var segment = segmentDequeue.pollEntry();
    if (segment == null) {
      segment = allocateNewEntry(segmentDequeue, bitBound);
    }

    return segment;
  }

  private MemorySegment getSegmentForScope(ResourceScope resourceScope, long size, long alignment) {
    final var segmentEntry = getSegmentEntryBySize(size, alignment);
    resourceScope.addOnClose(() -> putSegmentEntry(segmentEntry));
    return segmentEntry.value;
  }

  /**
   * Returns entry back to pool. After this operation entry should not be used.
   *
   * @param entry - the entry to put back
   */
  public void putSegmentEntry(Entry<MemorySegment> entry) {
    // The size already should be aligned, in case of putting wrong entry queue will
    // throw exception
    final var bitBound = 64 - Long.numberOfLeadingZeros(entry.value.byteSize()) - 1;

    final var segmentsQueue = segmentsDequeue[bitBound];
    if (!segmentsQueue.putEntryIfSize(entry, maxSizes[bitBound])) {
      if (segmentsQueue.isAssociated(entry)) {
        CLinker.freeMemory(entry.value.address());
      } else {
        // Queue checks for ownership only during add, someone can accidentally release not intended
        // segment.
        throw new IllegalStateException("This entry is not associated with given queue and this pool");
      }
    }
  }

  private static int bitBound(long alignedSize) {
    // If 100.., than 100... - 1 -> 01111
    // If 101 -> than 101 - 1 -> 1....
    return 64- Long.numberOfLeadingZeros(alignedSize - 1);
  }

  private Entry<MemorySegment> allocateNewEntry(SpinLockQueue<MemorySegment> queue, int bitBound) {
    final var allocationSize = 1 << bitBound;
    final var memoryAddress = CLinker.allocateMemory(allocationSize);
    return queue.allocateEntry(memoryAddress.asSegment(allocationSize, scope));
  }

  private void validateMaxSizes() {
    Arrays.stream(this.maxSizes).filter(i -> i < 0).findAny()
        .ifPresent(i -> {
          throw new IllegalStateException("Invalid max size " + i);
        });
  }
}
