package java.nicl.layout;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * A group layout is used to combine together multiple layouts. There are two ways in which layouts can be combined,
 * e.g. a 'struct' (see {@link Kind#STRUCT}), where contained elements are laid out one after the other, and a 'union'
 * (see {@link Kind#UNION}, where contained elements are laid out 'on top' of each other.
 */
public class Group extends AbstractLayout<Group> implements Layout {

    /**
     * The group kind.
     */
    public enum Kind {
        /**
         * A 'struct' kind.
         */
        STRUCT(LongStream::sum, ""),
        /**
         * A 'union' kind.
         */
        UNION(ls -> ls.max().getAsLong(), "|");

        final ToLongFunction<LongStream> sizeFunc;
        final String delimTag;

        Kind(ToLongFunction<LongStream> sizeFunc, String delimTag) {
            this.sizeFunc = sizeFunc;
            this.delimTag = delimTag;
        }
    }

    private final Kind kind;
    private final List<Layout> elements;

    protected Group(Kind kind, List<Layout> elements, Map<String, String> annotations) {
        super(annotations);
        this.kind = kind;
        this.elements = elements;
    }

    /**
     * Returns the kind associated to this group.
     * @return the group kind.
     */
    public Kind kind() {
        return kind;
    }

    /**
     * Returns the sub-elements associated with this group.
     * @return the element layouts.
     */
    public List<Layout> elements() {
        return elements;
    }

    @Override
    public String toString() {
        return wrapWithAnnotations(elements.stream()
                .map(Object::toString)
                .collect(Collectors.joining(kind.delimTag, "[", "]")));
    }

    @Override
    public long bitsSize() {
        return kind.sizeFunc.applyAsLong(elements.stream().mapToLong(Layout::bitsSize));
    }

    @Override
    public boolean isPartial() {
        return elements.stream().anyMatch(Layout::isPartial);
    }

    /**
     * Create a new product group layout with given elements.
     * @param elements The components of the product layout.
     * @return the new product group layout.
     */
    public static Group struct(Layout... elements) {
        return new Group(Kind.STRUCT, Arrays.asList(elements), NO_ANNOS);
    }

    /**
     * Create a new sum group layout with given elements.
     * @param elements The components of the sum layout.
     * @return the new sum group layout.
     */
    public static Group union(Layout... elements) {
        return new Group(Kind.UNION, Arrays.asList(elements), NO_ANNOS);
    }

    @Override
    Group dup(Map<String, String> annotations) {
        return new Group(kind, elements, annotations);
    }
}
