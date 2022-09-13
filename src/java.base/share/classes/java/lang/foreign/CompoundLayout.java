package java.lang.foreign;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * A compound layout aggregates multiple <em>memory layout</em> elements. There are two ways in which
 * element layouts can be combined: if element layouts can be heterogeneous, the resulting compound
 * layout is said to be a <em>group layout</em> (see {@link GroupLayout});
 * conversely, if all element layouts are homogeneous, the resulting compound layout is said to be a
 * <em>sequence layout</em> (see {@link SequenceLayout}).
 *
 * @implSpec
 * Implementing classes are immutable, thread-safe and <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
 *
 * @since 20
 */
public sealed interface CompoundLayout extends MemoryLayout, Iterable<MemoryLayout> permits GroupLayout, SequenceLayout {

    /**
     * {@return the size of this group layout (i.e. the number of elements in this group).}
     */
    long elementCount();

    /**
     * {@return the element at the provided {@code index}.}
     *
     * @apiNote the order in which member layouts are produced is the same order in which member layouts have
     * been passed to one of the group layout factory methods (see {@link MemoryLayout#structLayout(MemoryLayout...)},
     * {@link MemoryLayout#unionLayout(MemoryLayout...)}, {@link MemoryLayout#sequenceLayout(long, MemoryLayout)}).
     *
     * @param index the index for the MemoryLayout to retrieve
     * @throws NoSuchElementException if the provided {@code index} is negative or if greater or equal to {@link #elementCount()}
     */
    MemoryLayout elementAt(long index);

    /**
     * {@return a stream of all elements in this group.}
     *
     * @apiNote the order in which element layouts are produced is the same order in which member layouts have
     * been passed to one of the group layout factory methods (see {@link MemoryLayout#structLayout(MemoryLayout...)},
     * {@link MemoryLayout#unionLayout(MemoryLayout...)}, {@link MemoryLayout#sequenceLayout(long, MemoryLayout)}).
     */
    Stream<MemoryLayout> stream();

}
