package jdk.incubator.foreign;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Iterator;
import jdk.incubator.foreign.SpinLockQueue.Entry;
import jdk.internal.vm.annotation.ForceInline;

/**
 * Fast, concurrent LIFO queue (stack), based on operating on entries.
 *
 * This queue is designed for fast push / pop operations. Synchronization is
 * provided by classic spin lock.
 *
 * @param <T> the type of value used in queue
 */
public final class SpinLockQueue<T extends Entry<T>> {

  private int lock = 0;
  private int maxSize;

  private volatile int size;

  private volatile T head;

  private static final VarHandle HEAD;
  private static final VarHandle SIZE;
  private static final VarHandle LOCK;
  private static final VarHandle ENTRY_NEXT;
  private static final VarHandle ENTRY_IN_POOL;

  static {
    try {
      HEAD = MethodHandles.lookup().findVarHandle(SpinLockQueue.class, "head", Entry.class);
      SIZE = MethodHandles.lookup().findVarHandle(SpinLockQueue.class, "size", int.class);
      LOCK = MethodHandles.lookup().findVarHandle(SpinLockQueue.class, "lock", int.class);

      ENTRY_NEXT = MethodHandles.lookup().findVarHandle(Entry.class, "next", Entry.class);
      ENTRY_IN_POOL = MethodHandles.lookup().findVarHandle(Entry.class, "inPool", boolean.class);

    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public SpinLockQueue(int maxSize) {
    this.maxSize = maxSize;
  }

  @ForceInline
  final public T pollEntry() {
//    while ((int) LOCK.compareAndExchange(this, 0, 1) != 1) {};
    while (!LOCK.compareAndSet(this, 0, 1)) {}
    try {
      final var current = (T) HEAD.getAcquire(this);
      if (current != null) {
        HEAD.setRelease(this, ENTRY_NEXT.getAcquire(current));
        SIZE.setRelease(this, (int) SIZE.getAcquire(this) - 1);
        ENTRY_IN_POOL.set(current, false);
      }
      return current;
    } finally {
      LOCK.setRelease(this, 0);
    }
  }

//  final public void putEntryNoSizeCheck(T entry) {
//    while (!lock.compareAndSet(0, 1)) { }
//    try {
//      entry.next = head;
//      head = entry;
//      size++;
//    } finally {
//      lock.set(0);
//    }
//  }

  /**
   * Puts entry only if queue size is less then given size.
   *
   * @param entry - entry to put
   *
   * @return {@code true} if elements has been put.
   */
  @ForceInline
  final public boolean putEntry(T entry) {
    while (!LOCK.compareAndSet(this, 0, 1)) { }
    try {
      final var size = (int) SIZE.getAcquire(this);

      if ((boolean) ENTRY_IN_POOL.get(entry)) {
        throw new IllegalStateException("Entry already in pool, can't be added twice");
      }

      if (size < this.maxSize) {
        ENTRY_IN_POOL.set(entry, true);

        ENTRY_NEXT.setRelease(entry, HEAD.getAcquire(this));
        HEAD.setRelease(this, entry);
        SIZE.setRelease(this, size + 1);
        return true;
      } else {
        return false;
      }
    } finally {
      LOCK.setRelease(this, 0);
    }
  }

  /**
   * Checks if entry is associated with this queue.
   *
   * @param entry entry to check
   * @return {@code true} if this entry is associated with this queue if it's not in it
   */
  public boolean isAssociated(Entry<T> entry) {
    return entry.owner == this;
  }

  /**
   * Returns number of elements in the queue. This method is not atomic.
   *
   * @return number of elements in queue
   */
  public long size() {
    return this.size;
  }

  /**
   * Polls all entries and sets max size to 0 so no new entries can be added.
   * This operation is atomic.
   *
   * @return iterator will all entries, iterator is not synchronized, nor thread-safe
   */
  public Iterator<T> retrieveAndLock() {
    while (!LOCK.compareAndSet(this, 0, 1)) { }
    try {
      final var currentHead = (T) HEAD.getAcquire(this);
      final var result = new FastEntryIterator<T>(currentHead);
      SIZE.set(this, 0);
      HEAD.set(this, null);
      maxSize = 0;
      return result;
    } finally {
      LOCK.setRelease(this, 0);
    }
  }

  /**
   * Checks if entry is not in pool (or throw exception) and change flags inPool.
   * Prevent double addition.
   * To be called after lock
   */
  @ForceInline
  private static void checkMarkEntryInPool(Entry<?> entry) {
    if (!ENTRY_IN_POOL.weakCompareAndSet(entry, false, true)) {
      throw new IllegalStateException("Entry " + entry + " already in pool, can't be added twice");
    }
  }

  public static class Entry<T extends Entry<T>> {
    // Should we keep generic
    // If exposing spinlock queue, the entry should be in module internal package, to prevent
    // tampering owner and next with reflect
    final SpinLockQueue<T> owner;
    private volatile T next;
    private volatile boolean inPool;

    protected Entry(SpinLockQueue<T> owner) {
      this.owner = owner;
    }
  }

  /**
   * Goes through entries chain but don't use spinlock.
   */
  private static class FastEntryIterator<T extends Entry<T>> implements Iterator<T> {
    private T next;

    public FastEntryIterator(T next) {
      this.next = next;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public T next() {
      var result = next;
      ENTRY_IN_POOL.setVolatile(result, false);
      next = (T) ENTRY_NEXT.get(next);
      return result;
    }
  }
}
