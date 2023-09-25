/*
 *  Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.foreign.AddressLayout;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.UnionLayout;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Internal class to support inspection MemorySegments into various formats.
 */
final class MemoryInspectionUtil {

    static final BiFunction<ValueLayout, Object, String> STANDARD_VALUE_LAYOUT_RENDERER = new StandardValueLayoutRenderer();

    // Suppresses default constructor, ensuring non-instantiability.
    private MemoryInspectionUtil() {
    }

    static Stream<String> inspect(MemorySegment segment,
                                  MemoryLayout layout,
                                  BiFunction<ValueLayout, Object, String> renderer) {
        requireNonNull(segment);
        requireNonNull(layout);
        requireNonNull(renderer);

        final var builder = Stream.<String>builder();
        toString0(segment, layout, renderer, builder::add, new ViewState(), "");
        return builder.build();
    }

    private static void toString0(MemorySegment segment,
                                  MemoryLayout layout,
                                  BiFunction<ValueLayout, Object, String> renderer,
                                  Consumer<String> action,
                                  ViewState state,
                                  String suffix) {

        switch (layout) {
            case ValueLayout.OfBoolean ofBoolean ->
                    action.accept(renderValueLayout(state, ofBoolean, renderer.apply(ofBoolean, segment.get(ofBoolean, state.indexAndAdd(ofBoolean))), suffix));
            case ValueLayout.OfByte ofByte ->
                    action.accept(renderValueLayout(state, ofByte, renderer.apply(ofByte, segment.get(ofByte, state.indexAndAdd(ofByte))), suffix));
            case ValueLayout.OfShort ofShort ->
                    action.accept(renderValueLayout(state, ofShort, renderer.apply(ofShort, segment.get(ofShort, state.indexAndAdd(ofShort))), suffix));
            case ValueLayout.OfChar ofChar ->
                    action.accept(renderValueLayout(state, ofChar, renderer.apply(ofChar, segment.get(ofChar, state.indexAndAdd(ofChar))), suffix));
            case ValueLayout.OfInt ofInt ->
                    action.accept(renderValueLayout(state, ofInt, renderer.apply(ofInt, segment.get(ofInt, state.indexAndAdd(ofInt))), suffix));
            case ValueLayout.OfLong ofLong ->
                    action.accept(renderValueLayout(state, ofLong, renderer.apply(ofLong, segment.get(ofLong, state.indexAndAdd(ofLong))), suffix));
            case ValueLayout.OfFloat ofFloat ->
                    action.accept(renderValueLayout(state, ofFloat, renderer.apply(ofFloat, segment.get(ofFloat, state.indexAndAdd(ofFloat))), suffix));
            case ValueLayout.OfDouble ofDouble ->
                    action.accept(renderValueLayout(state, ofDouble, renderer.apply(ofDouble, segment.get(ofDouble, state.indexAndAdd(ofDouble))), suffix));
            case AddressLayout addressLayout ->
                    action.accept(renderValueLayout(state, addressLayout, renderer.apply(addressLayout, segment.get(addressLayout, state.indexAndAdd(addressLayout))), suffix));
            case PaddingLayout paddingLayout -> {
                action.accept(state.indentSpaces() + paddingLayout.byteSize() + " padding bytes");
                state.indexAndAdd(paddingLayout);
            }
            case GroupLayout groupLayout -> {
                /* Strictly, we should provide all permutations of unions.
                 * So, if we have a union U =  (A|B),(C|D) then we should present:
                 * (A,C), (A,D), (B,C) and (B,D)
                 */

                final var separator = groupLayout instanceof StructLayout
                        ? ","  // Struct separator
                        : "|"; // Union separator

                action.accept(indentedLabel(state, groupLayout) + " {");
                state.incrementIndent();
                final var members = groupLayout.memberLayouts();
                final long initialIndex = state.index();
                long maxIndex = initialIndex;
                for (int i = 0; i < members.size(); i++) {
                    if (groupLayout instanceof UnionLayout) {
                        // If it is a union, we need to reset the index for each member
                        state.index(initialIndex);
                        // We record the max index used for any union member so that we can leave off from there
                        maxIndex = Math.max(maxIndex, state.index());
                    }
                    toString0(segment, members.get(i), renderer, action, state, (i != (members.size() - 1)) ? separator : "");
                    if (groupLayout instanceof UnionLayout) {
                        // This is the best we can do.
                        state.index(maxIndex);
                    }
                }
                state.decrementIndent();
                action.accept(state.indentSpaces() + "}" + suffix);
            }
            case SequenceLayout sequenceLayout -> {
                action.accept(indentedLabel(state, sequenceLayout) + " [");
                state.incrementIndent();
                final long elementCount = sequenceLayout.elementCount();
                for (long i = 0; i < elementCount; i++) {
                    toString0(segment, sequenceLayout.elementLayout(), renderer, action, state, (i != (elementCount - 1L)) ? "," : "");
                }
                state.decrementIndent();
                action.accept(state.indentSpaces() + "]" + suffix);
            }
        }
    }

    static String renderValueLayout(ViewState state,
                                    ValueLayout layout,
                                    String value,
                                    String suffix) {
        return indentedLabel(state, layout) + "=" + value + suffix;
    }

    static String indentedLabel(ViewState state,
                                MemoryLayout layout) {
        return state.indentSpaces() + layout.name()
                .orElseGet(layout::toString);
    }

    static final class ViewState {

        private static final int SPACES_PER_INDENT = 4;

        // Holding a non-static indents allows simple thread-safe use
        private final StringBuilder indents = new StringBuilder();

        private int indent;
        private long index;

        void incrementIndent() {
            indent++;
        }

        void decrementIndent() {
            indent--;
        }

        String indentSpaces() {
            final int spaces = indent * SPACES_PER_INDENT;
            while (indents.length() < spaces) {
                // Expand as needed
                indents.append(" ");
            }
            return indents.substring(0, spaces);
        }

        long index() {
            return index;
        }

        void index(long index) {
            this.index = index;
        }

        long indexAndAdd(long delta) {
            final long val = index;
            index += delta;
            return val;
        }

        long indexAndAdd(MemoryLayout layout) {
            return indexAndAdd(layout.byteSize());
        }
    }

    private static final class StandardValueLayoutRenderer implements BiFunction<ValueLayout, Object, String> {

        @Override
        public String apply(ValueLayout layout, Object o) {
            requireNonNull(layout);
            requireNonNull(o);

            return switch (layout) {
                case ValueLayout.OfBoolean __ when o instanceof Boolean b -> Boolean.toString(b);
                case ValueLayout.OfByte __ when o instanceof Byte b -> Byte.toString(b);
                case ValueLayout.OfShort __ when o instanceof Short s -> Short.toString(s);
                case ValueLayout.OfChar __ when o instanceof Character c -> Character.toString(c);
                case ValueLayout.OfInt __ when o instanceof Integer i -> Integer.toString(i);
                case ValueLayout.OfLong __ when o instanceof Long l -> Long.toString(l);
                case ValueLayout.OfFloat __ when o instanceof Float f -> Float.toString(f);
                case ValueLayout.OfDouble __ when o instanceof Double d -> Double.toString(d);
                case AddressLayout __ when o instanceof MemorySegment m ->
                        String.format("0x%0" + (ValueLayout.ADDRESS.byteSize() * 2) + "X", m.address());
                default ->
                        throw new UnsupportedOperationException("layout " + layout + " for " + o.getClass().getName() + " not supported");
            };
        }

        @Override
        public String toString() {
            return singletonToString(StandardValueLayoutRenderer.class);
        }
    }

    private static String singletonToString(Class<?> implementingClass) {
        return "The " + implementingClass.getName() + " singleton";
    }

}
