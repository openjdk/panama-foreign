package java.nicl.layout;

import java.util.Map;

/**
 * An unresolved layout acts as a placeholder for another layout. Unresolved layouts can be resolved, which yields
 * a new layout whose size is known. The resolution process is typically driven by the annotations attached to the
 * unresolved layout.
 */
public class Unresolved extends AbstractLayout<Unresolved> implements Layout {
    protected Unresolved(Map<String, String> annotations) {
        super(annotations);
    }

    /**
     * Resolve this layout to a new layout instance.
     * @throws IllegalArgumentException if the resolution process fails.
     * @return the resolved layout. The resulting layout is such that its size is known. There are no constraints
     * on whether the resolved layout could contain extra unresolved layout in size-independent positions (e.g. inside pointers).
     */
    public Layout resolve() throws IllegalArgumentException {
        //Todo: implementation missing
        throw new IllegalArgumentException("Cannot resolve path expression; " + this);
    }

    /**
     * Create a new selector layout from given path expression.
     * @return the new selector layout.
     */
    public static Unresolved of() {
        return new Unresolved(NO_ANNOS);
    }

    @Override
    public long bitsSize() {
        return resolve().bitsSize();
    }

    @Override
    public boolean isPartial() {
        return true;
    }

    @Override
    public String toString() {
        return wrapWithAnnotations("$");
    }

    @Override
    Unresolved dup(Map<String, String> annotations) {
        return new Unresolved(annotations);
    }
}
