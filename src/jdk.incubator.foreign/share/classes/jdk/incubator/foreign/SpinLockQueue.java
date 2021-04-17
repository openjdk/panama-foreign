package jdk.incubator.foreign;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fast, concurrent LIFO queue (stack), based on operating on entries.
 *
 * This queue is designed for fast push / pop operations. Synchronization is
 * provided by spin lock.
 *
 * @param <T> the type of value used in queue
 */
public final class SpinLockQueue<T> {

  private final AtomicInteger lock = new AtomicInteger();
  private volatile int size;

  private volatile Entry<T> head;

  public SpinLockQueue() {

  }

  public Entry<T> pollEntry() {
    while (!lock.compareAndSet(0, 1)) { }
    try {
      final var current = head;
      if (current != null) {
        head = current.next;
        size--;
      }
      return current;
    } finally {
      lock.set(0);
    }
  }

  public void putEntry(Entry<T> entry) {
    if (entry.owner != this) {
      throw new IllegalStateException("This entry does not belong to this queue");
    }
    while (!lock.compareAndSet(0, 1)) { }
    try {
      entry.next = head;
      head = entry;
      size++;
    } finally {
      lock.set(0);
    }
  }

  /**
   * Puts entry only if queue size is less then given size.
   *
   * @param entry - entry to put
   * @param size - the maximum expected queue size
   *
   * @return {@code true} if elements has been put.
   */
  public boolean putEntryIfSize(Entry<T> entry, long size) {
    if (entry.owner != this) {
      throw new IllegalStateException("This entry does not belong to this queue");
    }
    while (!lock.compareAndSet(0, 1)) { }
    try {
      if (this.size <= size) {
        entry.next = head;
        head = entry;
        size++;
        return true;
      } else {
        return false;
      }
    } finally {
      lock.set(0);
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
   * Allocates, but not adds entry to the queue.
   */
  public Entry<T> allocateEntry(T value) {
    return new Entry<T>(this, value);
  }

  public static class Entry<T> {
    public final T value;

    private final SpinLockQueue<T> owner;
    private volatile Entry<T> next;

    private Entry(SpinLockQueue<T> owner, T value) {
      this.owner = owner;
      this.value = value;
    }
  }
}
