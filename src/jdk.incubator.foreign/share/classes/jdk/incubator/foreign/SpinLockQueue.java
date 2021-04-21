package jdk.incubator.foreign;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
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
  private final int maxSize;

  private volatile int size;

  private volatile T head;

  private static final VarHandle HEAD;
  private static final VarHandle SIZE;
  private static final VarHandle LOCK;
  private static final VarHandle ENTRY_NEXT;
  static {
    try {
      HEAD = MethodHandles.lookup().findVarHandle(SpinLockQueue.class, "head", Entry.class);
      SIZE = MethodHandles.lookup().findVarHandle(SpinLockQueue.class, "size", int.class);
      LOCK = MethodHandles.lookup().findVarHandle(SpinLockQueue.class, "lock", int.class);

      ENTRY_NEXT = MethodHandles.lookup().findVarHandle(Entry.class, "next", Entry.class);
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
      if (size < this.maxSize) {
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

  public static class Entry<T extends Entry<T>> {
    // Should we keep generic
    // If exposing spinlock queue, the entry should be in module internal package, to prevent
    // tampering owner and next with reflect
    final SpinLockQueue<T> owner;
    volatile T next;

    protected Entry(SpinLockQueue<T> owner) {
      this.owner = owner;
    }
  }
}
