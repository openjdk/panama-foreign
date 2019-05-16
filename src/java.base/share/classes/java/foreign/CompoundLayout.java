package java.foreign;

import java.util.stream.Stream;

/**
 * A compound layout. A compound layout is made of one or more layout sub-elements. Heterogeneous compound layouts
 * are modelled by the {@link GroupLayout} class, and are always finite in size. Homogeneous compound layouts are modelled
 * by the {@link SequenceLayout} class, and can be either finite or infinite in size.
 */
public interface CompoundLayout extends Layout {
    /**
     * Returns a stream of the layout sub-elements which forms this compound layout.
     * @return a stream of layout sub-elements.
     *
     * @apiNote in case the compound layout is a {@link SequenceLayout} whose size is unbounded, the resulting stream is infinite
     * and methods such as {@link Stream#limit} should be used.
     */
    Stream<Layout> elements();
}
