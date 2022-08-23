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

    private static final String ADDRESS_FORMATTING = "0x%0" + (ValueLayout.ADDRESS.byteSize() * 2) + "X";

    public static final MemorySegment.ValueLayoutRenderer STANDARD_VALUE_LAYOUT_RENDERER = new StandardValueLayoutRenderer();

    private MemorySegmentRenderUtil() {
    }

    /**
     * Returns a Stream of human-readable, lines with hexadecimal values for this memory segment.
     * <p>
     * Each element in the stream comprises the following characters:
     * <ol>
     *     <li>an initial 64-bit offset (e.g. "0000000000000010").</li>
     *     <li>a sequence of two spaces (i.e. "  ").</li>
     *     <li>a sequence of at most eight bytes (e.g. "66 6F 78 20 6A 75 6D 70") where
     *     each byte is separated by a space.</li>
     *     <li>a sequence of two spaces (i.e. "  ").</li>
     *     <li>a sequence of at most eight bytes (e.g. "65 64 20 6F 76 65 72 20") where
     *     each byte separated by a space.</li>
     *     <li>a sequence of N spaces (i.e. "  ") such that the intermediate line is aligned to 68 characters</li>
     *     <li>a "|" separator.</li>
     *     <li>a sequence of at most 16 printable Ascii characters (values outside [32, 127] will be printed as ".").</li>
     *     <li>a "|" separator.</li>
     * </ol>
     * All the values above are given in hexadecimal form with leading zeros. As there are at most 16 bytes
     * rendered for each line, there will be N = ({@link MemorySegment#byteSize()} + 15) / 16 elements in the returned stream.
     * <p>
     * As a consequence of the above, this method renders to a format similar to the *nix command "hexdump -C".
     * <p>
     * As an example, a memory segment created, initialized and used as follows
     * {@snippet lang = java:
     *   MemorySegment segment = memorySession.allocate(64 + 4);
     *   segment.setUtf8String(0, "The quick brown fox jumped over the lazy dog\nSecond line\t:here");
     *   segment.hexDump()
     *       .forEach(System.out::println);
     *}
     * will be printed as:
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
     * <p>
     * This method can be used to dump the contents of various other memory containers such as
     * {@linkplain ByteBuffer ByteBuffers} and byte arrays by means of first wrapping the container
     * into a MemorySegment:
     * {@snippet lang = java:
     *   MemorySegment.ofArray(byteArray).hexDump();
     *   MemorySegment.ofBuffer(byteBuffer).hexDump();
     *}
     *
     * @param segment to inspect
     * @return a Stream of human-readable, lines with hexadecimal values
     */
    public static Stream<String> hexDump(MemorySegment segment) {
        requireNonNull(segment);
        // Todo: Investigate how to handle mapped sparse files

        final var state = new HexStreamState();
        return LongStream.range(0, segment.byteSize())
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
                    state.appendValue(segment.get(ValueLayout.JAVA_BYTE, index));
                    final long nextCnt = index + 1;
                    if (nextCnt % HEX_STREAM_BYTES_PER_ROW == 0 || nextCnt == segment.byteSize()) {
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
    public static String toString(MemorySegment segment,
                                  MemoryLayout layout,
                                  MemorySegment.ValueLayoutRenderer renderer) {
        requireNonNull(segment);
        requireNonNull(layout);
        requireNonNull(renderer);

        final var sb = new StringBuilder();
        final Consumer<CharSequence> action = line -> {
            if (!sb.isEmpty()) {
                sb.append(System.lineSeparator());
            }
            sb.append(line);
        };
        toString0(segment, layout, renderer, action, new ViewState(), "");
        return sb.toString();
    }

    public static void toString0(MemorySegment segment,
                                 MemoryLayout layout,
                                 MemorySegment.ValueLayoutRenderer renderer,
                                 Consumer<? super CharSequence> action,
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
        if ("java.lang.foreign.PaddingLayout".equals(layout.getClass().getName())) {
            action.accept(state.indentSpaces() + layout.bitSize() + " padding bits");
            state.indexAndAdd(layout);
            return;
        }
        if (layout instanceof GroupLayout groupLayout) {

            /* Strictly, we should provide all permutations of unions.
             * So, if we have a union U =  (A|B),(C|D) then we should present:
             * (A,C), (A,D), (B,C) and (B,D)
             */

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
                toString0(segment, members.get(i), renderer, action, state, (i != (members.size() - 1)) ? separator : "");
                if (groupLayout.isUnion()) {
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
        sb.append((char) UPPERCASE_HEX_DIGITS[(value >>> 4) & 0x0f]);
        sb.append((char) UPPERCASE_HEX_DIGITS[(value & 0x0f)]);
    }

    static char viewByteAsAscii(byte b) {
        final int value = Byte.toUnsignedInt(b);
        return (value >= 32 && value < 127)
                ? (char) value
                : '.';
    }

    private static final class StandardValueLayoutRenderer implements MemorySegment.ValueLayoutRenderer {
        @Override
        public String toString() {
            return StandardValueLayoutRenderer.class.getSimpleName();
        }
    }

}
