/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.foreign.layout;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A function is made up of zero or more argument layouts and one return layout. A function descriptor
 * is used to model the signature of native functions. Note: a function is not a layout - it's merely
 * an aggregate for one or more layouts; as such it does not have a size.
 */
public final class Function extends AbstractDescriptor<Function> {
    private final Optional<Layout> resLayout;
    private final Layout[] argLayouts;
    private final boolean variadic;

    private Function(Map<String, String> annotations, Optional<Layout> resLayout, boolean variadic, Layout... argLayouts) {
        super(annotations);
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
     * Does this function contain unresolved layouts?
     * @return true if this function contains (possibly nested) unresolved layouts.
     */
    public boolean isPartial() {
        return Stream.concat(resLayout.stream(), Stream.of(argLayouts))
                .anyMatch(Layout::isPartial);
    }

    /**
     * Create a function descriptor with given return and argument layouts.
     * @param varargs is this a variadic function
     * @param resLayout the return layout.
     * @param argLayouts the argument layouts.
     * @return the new function descriptor.
     */
    public static Function of(Layout resLayout, boolean varargs, Layout... argLayouts) {
        return new Function(NO_ANNOS, Optional.of(resLayout), varargs, argLayouts);
    }

    /**
     * Create a void function descriptor with given argument layouts.
     * @param varargs is this a variadic function
     * @param argLayouts the argument layouts.
     * @return the new function descriptor.
     */
    public static Function ofVoid(boolean varargs, Layout... argLayouts) {
        return new Function(NO_ANNOS, Optional.empty(), varargs, argLayouts);
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Function)) {
            return false;
        }
        Function f = (Function)other;
        return resLayout.equals(f.resLayout) && Arrays.equals(argLayouts, f.argLayouts) &&
            variadic == f.variadic;
    }

    @Override
    public int hashCode() {
        return resLayout.hashCode() ^ Arrays.hashCode(argLayouts) ^ Boolean.hashCode(variadic);
    }

    @Override
    Function withAnnotations(Map<String, String> annotations) {
        return new Function(annotations, resLayout, variadic, argLayouts);
    }
}
