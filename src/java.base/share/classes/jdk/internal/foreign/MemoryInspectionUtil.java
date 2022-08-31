/*
 *  Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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
 *
 */

package jdk.internal.foreign;

import java.lang.foreign.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.util.Objects.requireNonNull;

/**
 * Internal class to support inspection of memory abstractions like MemorySegments into various formats.
 */
public final class MemoryInspectionUtil {

    public static final MemoryInspection.ValueLayoutRenderer STANDARD_VALUE_LAYOUT_RENDERER = new StandardValueLayoutRenderer();

    private MemoryInspectionUtil() {
    }

    /**
     * Returns a human-readable view of the provided {@code segment} viewed through
     * the provided {@code layout}.
     * <p>
     * Lines are separated with the system-dependent line separator {@link System#lineSeparator() }.
     * Otherwise, the exact format of the returned view is unspecified and should not
     * be acted upon programmatically.
     * <p>
     * As an example, a MemorySegment viewed though the following memory layout
     * {@snippet lang = java:
     * var layout = MemoryLayout.structLayout(
     *         ValueLayout.JAVA_INT.withName("x"),
     *         ValueLayout.JAVA_INT.withName("y")
     * ).withName("Point");
     *}
     * might be rendered to something like this:
     * {@snippet lang = text:
     * Point {
     *   x=1,
     *   y=2
     * }
     *}
     * <p>
     * This method is intended to view memory segments through small and medium-sized memory layouts
     * and is, in all cases, restricted by the inherent String capacity limit.
     *
     * @param segment to be viewed
     * @param layout  to use as a layout when viewing the memory segment
     * @return a view of the memory segment viewed through the memory layout
     * @throws OutOfMemoryError if the view exceeds the array size VM limit
     */
    private static String toString(MemorySegment segment,
                                  MemoryLayout layout,
                                  MemoryInspection.ValueLayoutRenderer renderer) {
        requireNonNull(segment);
        requireNonNull(layout);
        requireNonNull(renderer);

        final var sb = new StringBuilder();
        final Consumer<String> action = line -> {
            if (!sb.isEmpty()) {
                sb.append(System.lineSeparator());
            }
            sb.append(line);
        };
        toString0(segment, layout, renderer, action, new ViewState(), "");
        return sb.toString();
    }

    public static Stream<String> inspect(MemorySegment segment,
                                         MemoryLayout layout,
                                         MemoryInspection.ValueLayoutRenderer renderer) {
        requireNonNull(segment);
        requireNonNull(layout);
        requireNonNull(renderer);

        final var builder = Stream.<String>builder();
        toString0(segment, layout, renderer, builder::add, new ViewState(), "");
        return builder.build();
    }

    private static void toString0(MemorySegment segment,
                                 MemoryLayout layout,
                                 MemoryInspection.ValueLayoutRenderer renderer,
                                 Consumer<String> action,
                                 ViewState state,
                                 String suffix) {

        // TODO: Replace with "patterns in switch statement" once this becomes available.

        if (layout instanceof ValueLayout.OfBoolean ofBoolean) {
            action.accept(renderValueLayout(state, ofBoolean, renderer.render(ofBoolean, segment.get(ofBoolean, state.indexAndAdd(ofBoolean))), suffix));
            return;
        }
        if (layout instanceof ValueLayout.OfByte ofByte) {
            action.accept(renderValueLayout(state, ofByte, renderer.render(ofByte, segment.get(ofByte, state.indexAndAdd(ofByte))), suffix));
            return;
        }
        if (layout instanceof ValueLayout.OfShort ofShort) {
            action.accept(renderValueLayout(state, ofShort, renderer.render(ofShort, segment.get(ofShort, state.indexAndAdd(ofShort))), suffix));
            return;
        }
        if (layout instanceof ValueLayout.OfInt ofInt) {
            action.accept(renderValueLayout(state, ofInt, renderer.render(ofInt, segment.get(ofInt, state.indexAndAdd(ofInt))), suffix));
            return;
        }
        if (layout instanceof ValueLayout.OfLong ofLong) {
            action.accept(renderValueLayout(state, ofLong, renderer.render(ofLong, segment.get(ofLong, state.indexAndAdd(ofLong))), suffix));
            return;
        }
        if (layout instanceof ValueLayout.OfFloat ofFloat) {
            action.accept(renderValueLayout(state, ofFloat, renderer.render(ofFloat, segment.get(ofFloat, state.indexAndAdd(ofFloat))), suffix));
            return;
        }
        if (layout instanceof ValueLayout.OfDouble ofDouble) {
            action.accept(renderValueLayout(state, ofDouble, renderer.render(ofDouble, segment.get(ofDouble, state.indexAndAdd(ofDouble))), suffix));
            return;
        }
        if (layout instanceof ValueLayout.OfChar ofChar) {
            action.accept(renderValueLayout(state, ofChar, renderer.render(ofChar, segment.get(ofChar, state.indexAndAdd(ofChar))), suffix));
            return;
        }
        if (layout instanceof ValueLayout.OfAddress ofAddress) {
            action.accept(renderValueLayout(state, ofAddress, renderer.render(ofAddress, segment.get(ofAddress, state.indexAndAdd(ofAddress))), suffix));
            return;
        }
        // PaddingLayout is package private.
        if (layout instanceof PaddingLayout paddingLayout) {
            action.accept(state.indentSpaces() + paddingLayout.bitSize() + " padding bits");
            state.indexAndAdd(paddingLayout);
            return;
        }
        if (layout instanceof GroupLayout groupLayout) {

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
                    // We record the max index used for any union member so we can leave off from there
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
            return;
        }
        if (layout instanceof SequenceLayout sequenceLayout) {
            action.accept(indentedLabel(state, sequenceLayout) + " [");
            state.incrementIndent();
            final long elementCount = sequenceLayout.elementCount();
            for (long i = 0; i < elementCount; i++) {
                toString0(segment, sequenceLayout.elementLayout(), renderer, action, state, (i != (elementCount - 1L)) ? "," : "");
            }
            state.decrementIndent();
            action.accept(state.indentSpaces() + "]" + suffix);
            return;
        }
        action.accept(state.indentSpaces() + "Unknown layout: " + layout + " at index " + state.index());
        state.indexAndAdd(layout);
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

    private static final class StandardValueLayoutRenderer implements MemoryInspection.ValueLayoutRenderer {
        @Override
        public String toString() {
            return singletonToString(StandardValueLayoutRenderer.class);
        }
    }

    private static String singletonToString(Class<?> implementingClass) {
        return "The " + implementingClass.getName() + " singleton";
    }

}
