package java.nicl.layout;

import java.util.Map;
import java.util.Optional;

/**
 * This interface models the layout of a group of bits in a memory region.
 * Layouts can be annotated in order to embed domain specific knowledge, and they can be referenced by name
 * (see {@link Unresolved}). A layout is always associated with a size (in bits).
 */
public interface Layout {
    /**
     * Computes the layout size, in bits
     * @return the layout size.
     */
    long bitsSize();

    /**
     * Does this layout contain unresolved layouts?
     * @return true if this layout contains (possibly nested) unresolved layouts.
     */
    boolean isPartial();

    /**
     * The key of the predefined 'name' annotation.
     */
    String NAME = "name";

    /**
     * Return the key-value annotations map associated with this object.
     * @return the key-value annotations map.
     */
    Map<String, String> annotations();

    /**
     * Return the value of the 'name' annotation (if any) associated with this object.
     * @return the layout name (if any).
     */
    default Optional<String> name() {
        return Optional.ofNullable(annotations().get(NAME));
    }

    /**
     * Add annotation to layout.
     * @param name the annotation name.
     * @param value the annotation value.
     * @return the annotated layout.
     */
    Layout withAnnotation(String name, String value);

    /**
     * Strip all annotations from this (possibly annotated) layout.
     * @return the unannotated layout.
     */
    Layout stripAnnotations();

    @Override
    String toString();
}
