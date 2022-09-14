/*
 *  Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */
package jdk.internal.foreign;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @implSpec This class and its subclasses are immutable, thread-safe and <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
 */
public sealed class FunctionDescriptorImpl implements FunctionDescriptor {

    private final MemoryLayout resLayout; // Nullable
    private final List<MemoryLayout> argLayouts;

    private FunctionDescriptorImpl(MemoryLayout resLayout, List<MemoryLayout> argLayouts) {
        this.resLayout = resLayout;
        this.argLayouts = List.copyOf(argLayouts);
    }

    /**
     * {@return the return layout (if any) associated with this function descriptor}
     */
    public final Optional<MemoryLayout> returnLayout() {
        return Optional.ofNullable(resLayout);
    }

    /**
     * {@return the argument layouts associated with this function descriptor (as an immutable list)}.
     */
    public final List<MemoryLayout> argumentLayouts() {
        return argLayouts;
    }

    /**
     * Creates a specialized variadic function descriptor, by appending given variadic layouts to this
     * function descriptor argument layouts. The resulting function descriptor can report the position
     * of the {@linkplain #firstVariadicArgumentIndex() first variadic argument}, and cannot be altered
     * in any way: for instance, calling {@link #changeReturnLayout(MemoryLayout)} on the resulting descriptor
     * will throw an {@link UnsupportedOperationException}.
     *
     * @param variadicLayouts the variadic argument layouts to be appended to this descriptor argument layouts.
     * @return a variadic function descriptor, or this descriptor if {@code variadicLayouts.length == 0}.
     */
    public final FunctionDescriptorImpl asVariadic(MemoryLayout... variadicLayouts) {
        // Null checks are implicit in the constructor of VariadicFunction
        return variadicLayouts.length == 0 ? this : new VariadicFunctionDescriptor(this, variadicLayouts);
    }

    /**
     * The index of the first variadic argument layout (where defined).
     *
     * @return The index of the first variadic argument layout, or {@code -1} if this is not a
     * {@linkplain #asVariadic(MemoryLayout...) variadic} layout.
     */
    public int firstVariadicArgumentIndex() {
        return -1;
    }

    /**
     * Returns a function descriptor with the given argument layouts appended to the argument layout array
     * of this function descriptor.
     *
     * @param addedLayouts the argument layouts to append.
     * @return the new function descriptor.
     */
    public final FunctionDescriptorImpl appendArgumentLayouts(MemoryLayout... addedLayouts) {
        return insertArgumentLayouts(argLayouts.size(), addedLayouts);
    }

    /**
     * Returns a function descriptor with the given argument layouts inserted at the given index, into the argument
     * layout array of this function descriptor.
     *
     * @param index        the index at which to insert the arguments
     * @param addedLayouts the argument layouts to insert at given index.
     * @return the new function descriptor.
     * @throws IllegalArgumentException if {@code index < 0 || index > argumentLayouts().size()}.
     */
    public FunctionDescriptorImpl insertArgumentLayouts(int index, MemoryLayout... addedLayouts) {
        if (index < 0 || index > argLayouts.size())
            throw new IllegalArgumentException("Index out of bounds: " + index);
        List<MemoryLayout> added = List.of(addedLayouts); // null check on array and its elements
        List<MemoryLayout> newLayouts = new ArrayList<>(argLayouts.size() + addedLayouts.length);
        newLayouts.addAll(argLayouts.subList(0, index));
        newLayouts.addAll(added);
        newLayouts.addAll(argLayouts.subList(index, argLayouts.size()));
        return new FunctionDescriptorImpl(resLayout, newLayouts);
    }

    /**
     * Returns a function descriptor with the given memory layout as the new return layout.
     *
     * @param newReturn the new return layout.
     * @return the new function descriptor.
     */
    public FunctionDescriptorImpl changeReturnLayout(MemoryLayout newReturn) {
        Objects.requireNonNull(newReturn);
        return new FunctionDescriptorImpl(newReturn, argLayouts);
    }

    /**
     * Returns a function descriptor with the return layout dropped. This is useful to model functions
     * which return no values.
     *
     * @return the new function descriptor.
     */
    public FunctionDescriptorImpl dropReturnLayout() {
        return new FunctionDescriptorImpl(null, argLayouts);
    }

    /**
     * {@return the string representation of this function descriptor}
     */
    @Override
    public final String toString() {
        return String.format("(%s)%s",
                IntStream.range(0, argLayouts.size())
                        .mapToObj(i -> (i == firstVariadicArgumentIndex() ?
                                "..." : "") + argLayouts.get(i))
                        .collect(Collectors.joining()),
                returnLayout()
                        .map(Object::toString)
                        .orElse("v"));
    }

    /**
     * Compares the specified object with this function descriptor for equality. Returns {@code true} if and only if the specified
     * object is also a function descriptor, and all the following conditions are met:
     * <ul>
     *     <li>the two function descriptors have equals return layouts (see {@link MemoryLayout#equals(Object)}), or both have no return layout;</li>
     *     <li>the two function descriptors have argument layouts that are pair-wise {@linkplain MemoryLayout#equals(Object) equal}; and</li>
     *     <li>the two function descriptors have the same leading {@linkplain #firstVariadicArgumentIndex() variadic argument index}</li>
     * </ul>
     *
     * @param other the object to be compared for equality with this function descriptor.
     * @return {@code true} if the specified object is equal to this function descriptor.
     */
    @Override
    public final boolean equals(Object other) {
        return other instanceof FunctionDescriptorImpl f &&
                Objects.equals(resLayout, f.resLayout) &&
                Objects.equals(argLayouts, f.argLayouts) &&
                firstVariadicArgumentIndex() == f.firstVariadicArgumentIndex();
    }

    /**
     * {@return the hash code value for this function descriptor}
     */
    @Override
    public final int hashCode() {
        return Objects.hash(argLayouts, resLayout, firstVariadicArgumentIndex());
    }

    public static FunctionDescriptor of(MemoryLayout resLayout, List<MemoryLayout> argLayouts) {
        return new FunctionDescriptorImpl(resLayout, argLayouts);
    }

    public static FunctionDescriptor ofVoid(List<MemoryLayout> argLayouts) {
        return new FunctionDescriptorImpl(null, argLayouts);
    }

    static final class VariadicFunctionDescriptor extends FunctionDescriptorImpl {

        private final int firstVariadicIndex;

        /**
         * Constructor.
         *
         * @param descriptor the original functional descriptor
         * @param argLayouts the memory layouts to apply
         * @throws NullPointerException if any of the provided parameters or array elements are {@code null}
         */
        VariadicFunctionDescriptor(FunctionDescriptorImpl descriptor, MemoryLayout... argLayouts) {
            super(descriptor.returnLayout().orElse(null),
                    Stream.concat(descriptor.argumentLayouts().stream(), Arrays.stream(argLayouts)
                            .map(Objects::requireNonNull))
                            .toList());
            this.firstVariadicIndex = descriptor.argumentLayouts().size();
        }

        @Override
        public int firstVariadicArgumentIndex() {
            return firstVariadicIndex;
        }

        @Override
        public FunctionDescriptorImpl insertArgumentLayouts(int index, MemoryLayout... addedLayouts) {
            throw newUnsupportedOperationException();
        }

        @Override
        public FunctionDescriptorImpl changeReturnLayout(MemoryLayout newReturn) {
            throw newUnsupportedOperationException();
        }

        @Override
        public FunctionDescriptorImpl dropReturnLayout() {
            throw newUnsupportedOperationException();
        }

        private UnsupportedOperationException newUnsupportedOperationException() {
            return new UnsupportedOperationException("Method not supported by " + VariadicFunctionDescriptor.class.getSimpleName());
        }
    }
}
