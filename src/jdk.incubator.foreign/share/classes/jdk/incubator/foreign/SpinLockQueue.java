package jdk.incubator.foreign;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Iterator;
import jdk.incubator.foreign.SpinLockQueue.Entry;
import jdk.internal.vm.annotation.ForceInline;

/**
 * Fast, concurrent LIFO queue (stack).
 *
 * This queue is designed for fast push / pop operations. Synchronization is
 * provided by classic spin lock.
 *
 * @param <T> the item type
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

  /**
   * Creates new instace with specified maximum capacity.
   *
   * @param maxSize maximum number of elements in queue.
   */
  public SpinLockQueue(int maxSize) {
    this.maxSize = maxSize;
  }

  /**
   * Retrieve element form queue.
   * <br />
   * This operation is atomic.
   *
   * @return item or {@code null} if queue is empty.
   */
  @ForceInline
  final public T pollEntry() {
    while (!LOCK.compareAndSet(this, 0, 1)) {}
    // After volatile spin lock
    try {
      final var current = (T) HEAD.get(this);
      if (current != null) {
        HEAD.set(this, ENTRY_NEXT.get(current));
        SIZE.set(this, (int) SIZE.get(this) - 1);
        ENTRY_IN_POOL.set(current, false);
      }
      return current;
    } finally {
      LOCK.setRelease(this, 0);
    }
  }

  /**
   * Puts entry into the queue, but only if queue has a capacity.
   * <br />
   * This operation is atomic.
   *
   * @param entry entry to put
   *
   * @return {@code true} if elements has been successfully put.
   */
  @ForceInline
  final public boolean putEntry(T entry) {
    while (!LOCK.compareAndSet(this, 0, 1)) { }
    // After volatile spin lock
    try {
      final var size = (int) SIZE.get(this);

      if ((boolean) ENTRY_IN_POOL.get(entry)) {
        throw new IllegalStateException("Entry already in pool, can't be added twice");
      }

      if (size < this.maxSize) {
        ENTRY_IN_POOL.set(entry, true);

        ENTRY_NEXT.set(entry, HEAD.get(this));
        HEAD.set(this, entry);
        SIZE.set(this, size + 1);
        return true;
      } else {
        return false;
      }
    } finally {
      LOCK.setRelease(this, 0);
    }
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
   * Polls all entries and sets max size to 0, so no new entries can be added.
   * <br />
   * This operation is atomic.
   *
   * @return iterator with all entries, iterator is not synchronized, nor thread-safe
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
   * Represent a single item which can be added to the queue.
   * <br />
   * As the queue is intended for fast puts and gets, users should
   * subclass this calls in order to add custom attributes.
   *
   * @param <T> the final exact type.
   */
  public abstract static class Entry<T extends Entry<T>> {
    private volatile T next;
    private volatile boolean inPool;

    protected Entry() {
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
