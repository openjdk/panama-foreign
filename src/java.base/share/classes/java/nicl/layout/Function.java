package java.nicl.layout;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A function is made up of zero or more argument layouts and one return layout. A function descriptor
 * is used to model the signature of native functions. Note: a function is not a layout - it's merely
 * an aggregate for one or more layouts; as such it does not have a size.
 */
public final class Function {
    private final Optional<Layout> resLayout;
    private final Layout[] argLayouts;
    private boolean variadic;

    private Function(Optional<Layout> resLayout, boolean variadic, Layout... argLayouts) {
        this.resLayout = resLayout;
        this.variadic = variadic;
        this.argLayouts = argLayouts;
    }

    /**
     * Returns the return layout associated with this function.
     * @return the return layout.
     */
    public Optional<Layout> returnLayout() {
        return resLayout;
    }

    /**
     * Returns the argument layouts associated with this function.
     * @return the argument layouts.
     */
    public List<Layout> argumentLayouts() {
        return Arrays.asList(argLayouts);
    }

    /**
     * Does this function accept a variable-arity argument list?
     * @return true, if the function models a variadic function.
     */
    public boolean isVariadic() {
        return variadic;
    }

    /**
     * Create a function descriptor with given return and argument layouts.
     * @param varargs is this a variadic function
     * @param resLayout the return layout.
     * @param argLayouts the argument layouts.
     * @return the new function descriptor.
     */
    public static Function of(Layout resLayout, boolean varargs, Layout... argLayouts) {
        return new Function(Optional.of(resLayout), varargs, argLayouts);
    }

    /**
     * Create a void function descriptor with given argument layouts.
     * @param varargs is this a variadic function
     * @param argLayouts the argument layouts.
     * @return the new function descriptor.
     */
    public static Function ofVoid(boolean varargs, Layout... argLayouts) {
        return new Function(Optional.empty(), varargs, argLayouts);
    }

    @Override
    public String toString() {
        return String.format("(%s%s)%s",
                Stream.of(argLayouts)
                        .map(Object::toString)
                        .collect(Collectors.joining()),
                variadic ? "*" : "",
                resLayout.map(Object::toString).orElse("v"));
    }
}
