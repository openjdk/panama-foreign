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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Internal class to support rendering of MemorySegments into various formats.
 */
public final class MemorySegmentRenderUtil {

    private static final byte[] UPPERCASE_HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static final int HEX_STREAM_BYTES_PER_ROW = 1 << 4; // Should be a power of 2
    private static final int HEX_LINE_LENGTH_EXCLUDING_CHARS = Long.BYTES * 2 + HEX_STREAM_BYTES_PER_ROW * 3 + 4;

    private MemorySegmentRenderUtil() {
    }

    /**
     * Returns a Stream of human-readable, lines with hexadecimal values for the provided {@code memorySegment}.
     * <p>
     * The exact format of the stream elements is unspecified and should not
     * be acted upon programmatically. Loosely speaking, this method renders
     * a format similar to the *nix command "hexdump -C".
     * <p>
     * As an example, a MemorySegment created and initialized as follows
     * {@snippet lang = java:
     * MemorySegment memorySegment = memorySession.allocate(64 + 4);
     * memorySegment.setUtf8String(0, "The quick brown fox jumped over the lazy dog\nSecond line\t:here");
     * hexStream(memorySegment)
     *     .forEach(System.out::println);
     *}
     * might print to something like this:
     * {@snippet lang = text:
     * 0000000000000000  54 68 65 20 71 75 69 63  6B 20 62 72 6F 77 6E 20  |The quick brown |
     * 0000000000000010  66 6F 78 20 6A 75 6D 70  65 64 20 6F 76 65 72 20  |fox jumped over |
     * 0000000000000020  74 68 65 20 6C 61 7A 79  20 64 6F 67 0A 53 65 63  |the lazy dog.Sec|
     * 0000000000000030  6F 6E 64 20 6C 69 6E 65  09 3A 68 65 72 65 00 00  |ond line.:here..|
     * 0000000000000040  00 00 00 00                                       |....|
     *}
     * <p>
     * Use a {@linkplain MemorySegment#asSlice(long, long) slice} to inspect a specific region
     * of a memory segment.
     *
     * @param memorySegment the memory segment to generate hexadecimal values from
     * @return a Stream of human-readable, lines with hexadecimal values
     */
    public static Stream<String> hexStream(MemorySegment memorySegment) {
        requireNonNull(memorySegment);
        // Todo: Investigate how to handle mapped sparse files

        final var state = new HexStreamState();
        return LongStream.range(0, memorySegment.byteSize())
                .mapToObj(index -> {
                    if (state.isEmpty()) {
                        // We are on a new line: Append the index
                        state.appendIndex(index);
                    }
                    if (index % (HEX_STREAM_BYTES_PER_ROW >>> 1) == 0) {
                        // We are either at the beginning or halfway through: add an extra space for readability
                        state.appendSpace();
                    }
                    // Append the actual memory value
                    state.appendValue(memorySegment.get(ValueLayout.JAVA_BYTE, index));
                    final long nextCnt = index + 1;
                    if (nextCnt % HEX_STREAM_BYTES_PER_ROW == 0 || nextCnt == memorySegment.byteSize()) {
                        // We have a complete line (eiter a full line or the last line)
                        return state.renderLineToStringAndReset();
                    } else {
                        // For this count, there was no line break so pass null and filter it away later
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }

    /**
     * Returns a human-readable view of the provided {@code memorySegment} viewed through
     * the provided {@code memoryLayout}.
     * <p>
     * Lines are separated with the system-dependent line separator {@link System#lineSeparator() }.
     * Otherwise, the exact format of the returned view is unspecified and should not
     * be acted upon programmatically.
     * <p>
     * As an example, a MemorySegment viewed though the following memory layout
     * {@snippet lang = java:
     * var memoryLayout = MemoryLayout.structLayout(
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
     * @param memorySegment to be viewed
     * @param memoryLayout  to use as a memoryLayout when viewing the memory segment
     * @return a view of the memory segment viewed through the memory layout
     * @throws OutOfMemoryError if the view exceeds the array size VM limit
     */
    public static String viewThrough(MemorySegment memorySegment,
                                     MemoryLayout memoryLayout) {
        requireNonNull(memorySegment);
        requireNonNull(memoryLayout);

        final var sb = new StringBuilder();
        final Consumer<CharSequence> action = line -> {
            if (!sb.isEmpty()) {
                sb.append(System.lineSeparator());
            }
            sb.append(line);
        };
        renderView(memorySegment, memoryLayout, action, new ViewState(), "");
        return sb.toString();
    }

    public static void renderView(MemorySegment memorySegment,
                                  MemoryLayout memoryLayout,
                                  Consumer<? super CharSequence> action,
                                  ViewState state,
                                  String suffix) {

        // TODO: Replace with "patterns in switch statement" once this becomes available.

        if (memoryLayout instanceof ValueLayout.OfByte ofByte) {
            action.accept(renderValueLayout(state, ofByte, Byte.toString(memorySegment.get(ofByte, state.indexAndAdd(ofByte))), suffix));
            return;
        }
        if (memoryLayout instanceof ValueLayout.OfShort ofShort) {
            action.accept(renderValueLayout(state, ofShort, Short.toString(memorySegment.get(ofShort, state.indexAndAdd(ofShort))), suffix));
            return;
        }
        if (memoryLayout instanceof ValueLayout.OfInt ofInt) {
            action.accept(renderValueLayout(state, ofInt, Integer.toString(memorySegment.get(ofInt, state.indexAndAdd(ofInt))), suffix));
            return;
        }
        if (memoryLayout instanceof ValueLayout.OfLong ofLong) {
            action.accept(renderValueLayout(state, ofLong, Long.toString(memorySegment.get(ofLong, state.indexAndAdd(ofLong))), suffix));
            return;
        }
        if (memoryLayout instanceof ValueLayout.OfFloat ofFloat) {
            action.accept(renderValueLayout(state, ofFloat, Float.toString(memorySegment.get(ofFloat, state.indexAndAdd(ofFloat))), suffix));
            return;
        }
        if (memoryLayout instanceof ValueLayout.OfDouble ofDouble) {
            action.accept(renderValueLayout(state, ofDouble, Double.toString(memorySegment.get(ofDouble, state.indexAndAdd(ofDouble))), suffix));
            return;
        }
        if (memoryLayout instanceof ValueLayout.OfChar ofChar) {
            action.accept(renderValueLayout(state, ofChar, Character.toString(memorySegment.get(ofChar, state.indexAndAdd(ofChar))), suffix));
            return;
        }
        if (memoryLayout instanceof ValueLayout.OfBoolean ofBoolean) {
            action.accept(renderValueLayout(state, ofBoolean, Boolean.toString(memorySegment.get(ofBoolean, state.indexAndAdd(ofBoolean))), suffix));
            return;
        }
        if (memoryLayout instanceof ValueLayout.OfAddress ofAddress) {
            action.accept(renderValueLayout(state, ofAddress, memorySegment.get(ofAddress, state.indexAndAdd(ofAddress)).toString(), suffix));
            return;
        }
        // PaddingLayout is package private.
        if ("java.lang.foreign.PaddingLayout".equals(memoryLayout.getClass().getName())) {
            action.accept(state.indentSpaces() + memoryLayout.bitSize() + " padding bits");
            state.indexAndAdd(memoryLayout);
            return;
        }
        if (memoryLayout instanceof GroupLayout groupLayout) {

            // Strictly, we should provide all permutations of nested unions.

            final var separator = groupLayout.isStruct()
                    ? ","  // Struct separator
                    : "|"; // Union separator

            action.accept(indentedLabel(state, groupLayout) + " {");
            state.incrementIndent();
            final var members = groupLayout.memberLayouts();
            final long initialIndex = state.index();
            long maxIndex = initialIndex;
            for (int i = 0; i < members.size(); i++) {
                if (groupLayout.isUnion()) {
                    // If it is a union, we need to reset the index for each member
                    state.index(initialIndex);
                    // We record the max index used for any union member so we can leave off from there
                    maxIndex = Math.max(maxIndex, state.index());
                }
                renderView(memorySegment, members.get(i), action, state, (i != (members.size() - 1)) ? separator : "");
                if (groupLayout.isUnion()) {
                    // This is the best we can do.
                    state.index(maxIndex);
                }
            }
            state.decrementIndent();
            action.accept(state.indentSpaces() + "}" + suffix);
            return;
        }
        if (memoryLayout instanceof SequenceLayout sequenceLayout) {
            action.accept(indentedLabel(state, sequenceLayout) + " [");
            state.incrementIndent();
            final long elementCount = sequenceLayout.elementCount();
            for (long i = 0; i < elementCount; i++) {
                renderView(memorySegment, sequenceLayout.elementLayout(), action, state, (i != (elementCount - 1L)) ? "," : "");
            }
            state.decrementIndent();
            action.accept(state.indentSpaces() + "]" + suffix);
            return;
        }
        action.accept("Unknown memoryLayout: " + memoryLayout);
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

    static final class HexStreamState {
        private final StringBuilder line = new StringBuilder();
        private final StringBuilder chars = new StringBuilder();

        boolean isEmpty() {
            return line.isEmpty();
        }

        void appendIndex(long index) {
            appendHexTo(line, index);
            appendSpace();
        }

        void appendValue(byte val) {
            appendHexTo(line, val);
            chars.append(viewByteAsAscii(val));
            appendSpace();
        }

        String renderLineToStringAndReset() {
            while (line.length() < HEX_LINE_LENGTH_EXCLUDING_CHARS) {
                // Pad if necessary
                appendSpace();
            }
            line.append('|').append(chars).append('|');

            final String result = line.toString();
            line.setLength(0);
            chars.setLength(0);
            return result;
        }

        void appendSpace() {
            line.append(' ');
        }
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

    static void appendHexTo(StringBuilder sb,
                            long value) {
        long reversed = Long.reverseBytes(value);
        for (int i = 0; i < Long.BYTES; i++) {
            appendHexTo(sb, (byte) (reversed & 0xff));
            reversed >>>= Byte.SIZE;
        }
    }

    static void appendHexTo(StringBuilder sb,
                            byte value) {
        sb.append((char) UPPERCASE_HEX_DIGITS[(value >>> 4)]);
        sb.append((char) UPPERCASE_HEX_DIGITS[(value & (byte) 0x0f)]);
    }

    static char viewByteAsAscii(byte b) {
        final int value = Byte.toUnsignedInt(b);
        return (value >= 32 && value < 127)
                ? (char) value
                : '.';
    }

}
