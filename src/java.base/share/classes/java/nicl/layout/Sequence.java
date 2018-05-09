package java.nicl.layout;

import java.util.Collections;
import java.util.Map;

/**
 * A sequence layout. A sequence layout is a special case of a group layout, made up of an element layout and a repetition count.
 * The repetition count can be zero if the sequence contains no elements. A sequence layout of the kind e.g. {@code [ 4i32 ]},
 * always induces a group layout e.g. {@code [i32 i32 i32 i32]} - that is, the group associated with a sequence is
 * a 'struct' group (see {@link Group.Kind#STRUCT}), where a given layout element is repeated a number of times that
 * is equal to the sequence size.
 */
public final class Sequence extends Group {

    private final int size;
    private final Layout elementLayout;

    private Sequence(int size, Layout elementLayout, Map<String, String> annotations) {
        super(Kind.STRUCT, Collections.nCopies(size, elementLayout), annotations);
        this.size = size;
        this.elementLayout = elementLayout;
    }

    /**
     * The element layout associated with this sequence layout.
     * @return element layout.
     */
    public Layout element() {
        return elementLayout;
    }

    /**
     * Create a new sequence layout with given element layout and size.
     * @param elementLayout the element layout.
     * @param size the array repetition count.
     * @return the new sequence layout.
     */
    public static Sequence of(int size, Layout elementLayout) {
        return new Sequence(size, elementLayout, NO_ANNOS);
    }

    /**
     * Returns the repetition count associated with this sequence layout.
     * @return the repetition count (can be zero if array size is unspecified).
     */
    public int elementsSize() {
        return size;
    }

    @Override
    public String toString() {
        return wrapWithAnnotations(String.format("[%d%s]",
                size, elementLayout));
    }

    @Override
    Sequence dup(Map<String, String> annotations) {
        return new Sequence(elementsSize(), elementLayout, annotations);
    }

    @Override
    public Sequence stripAnnotations() {
        return (Sequence)super.stripAnnotations();
    }

    @Override
    public Sequence withAnnotation(String name, String value) {
        return (Sequence) super.withAnnotation(name, value);
    }
}
