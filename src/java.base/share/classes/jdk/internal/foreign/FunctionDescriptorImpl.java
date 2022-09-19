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

import jdk.internal.foreign.abi.LinkerOptions;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @implSpec This class and its subclasses are immutable, thread-safe and <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
 */
public sealed class FunctionDescriptorImpl implements FunctionDescriptor {

    private final MemoryLayout resLayout; // Nullable
    private final List<MemoryLayout> argLayouts;
    private final Map<Class<?>, Linker.Option> options;

    private FunctionDescriptorImpl(MemoryLayout resLayout, List<MemoryLayout> argLayouts,
                                   Map<Class<?>, Linker.Option> options) {
        this.resLayout = resLayout;
        this.argLayouts = List.copyOf(argLayouts);
        this.options = options; // copied by accessor
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

    @Override
    public FunctionDescriptorImpl asVariadic(MemoryLayout... variadicLayouts) {
        return new VariadicFunctionDescriptor(this, variadicLayouts);
    }

    @Override
    public FunctionDescriptorImpl addOptions(Linker.Option... options) {
        return new FunctionDescriptorImpl(resLayout, argLayouts, addOptions(this.options, options));
    }

    private static Map<Class<?>, Linker.Option> addOptions(Map<Class<?>, Linker.Option> oldOptions,
                                                           Linker.Option... optionsToAdd) {
        Map<Class<?>, Linker.Option> newOptions = new HashMap<>(oldOptions);

        for (Linker.Option option : optionsToAdd) {
            if (newOptions.containsKey(option.getClass())) {
                throw new IllegalArgumentException("Option already present: " + option);
            }
            newOptions.put(option.getClass(), option);
        }
        return newOptions;
    }

    @Override
    public <T extends Linker.Option> T getOption(Class<T> optionType) {
        return optionType.cast(options.get(optionType));
    }

    /**
     * The index of the first variadic argument layout (where defined).
     *
     * @return The index of the first variadic argument layout, or {@code -1} if this is not a
     * {@linkplain #asVariadic(MemoryLayout...) variadic} layout.
     */
    public int firstVariadicArgumentIndex() {
        LinkerOptions.FirstVariadicArg firstVariadicArg = getOption(LinkerOptions.FirstVariadicArg.class);
        return firstVariadicArg == null ? -1 : firstVariadicArg.index();
    }

    @Override
    public List<Linker.Option> options() {
        return List.copyOf(options.values());
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
        return new FunctionDescriptorImpl(resLayout, newLayouts, options);
    }

    /**
     * Returns a function descriptor with the given memory layout as the new return layout.
     *
     * @param newReturn the new return layout.
     * @return the new function descriptor.
     */
    public FunctionDescriptorImpl changeReturnLayout(MemoryLayout newReturn) {
        requireNonNull(newReturn);
        return new FunctionDescriptorImpl(newReturn, argLayouts, options);
    }

    /**
     * Returns a function descriptor with the return layout dropped. This is useful to model functions
     * which return no values.
     *
     * @return the new function descriptor.
     */
    public FunctionDescriptorImpl dropReturnLayout() {
        return new FunctionDescriptorImpl(null, argLayouts, options);
    }

    private static Class<?> carrierTypeFor(MemoryLayout layout) {
        if (layout instanceof ValueLayout valueLayout) {
            return valueLayout.carrier();
        } else if (layout instanceof GroupLayout) {
            return MemorySegment.class;
        } else {
            throw new IllegalArgumentException("Unsupported layout: " + layout);
        }
    }

    @Override
    public MethodType toMethodType() {
        Class<?> returnValue = resLayout != null ? carrierTypeFor(resLayout) : void.class;
        Class<?>[] argCarriers = new Class<?>[argLayouts.size()];
        for (int i = 0; i < argCarriers.length; i++) {
            argCarriers[i] = carrierTypeFor(argLayouts.get(i));
        }
        return MethodType.methodType(returnValue, argCarriers);
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
     *     <li>the two function descriptors have the same linker options</li>
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
                Objects.equals(options, f.options());
    }

    /**
     * {@return the hash code value for this function descriptor}
     */
    @Override
    public final int hashCode() {
        return Objects.hash(argLayouts, resLayout, options);
    }

    private static final Map<Class<?>, Linker.Option> NO_OPTIONS = Map.of();

    public static FunctionDescriptor of(MemoryLayout resLayout, List<MemoryLayout> argLayouts) {
        return new FunctionDescriptorImpl(resLayout, argLayouts, NO_OPTIONS);
    }

    public static FunctionDescriptor ofVoid(List<MemoryLayout> argLayouts) {
        return new FunctionDescriptorImpl(null, argLayouts, NO_OPTIONS);
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
                            .toList(),
                    descriptor.options);
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
