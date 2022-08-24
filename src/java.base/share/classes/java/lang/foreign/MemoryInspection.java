package java.lang.foreign;

import jdk.internal.foreign.MemorySegmentRenderUtil;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.util.Objects.requireNonNull;
import static jdk.internal.foreign.MemorySegmentRenderUtil.*;

/**
 * Class that support inspection of various memory abstractions such as
 * MemorySegment, ByteBuffer and byte arrays.
 * <p>
 * The methods in this class are not thread safe.
 */
public final class MemoryInspection {

    // Suppresses default constructor, ensuring non-instantiability.
    private MemoryInspection() {
    }

    /**
     * Returns a Stream of human-readable, lines with hexadecimal values for the provided
     * {@code memory} abstraction. The memory abstraction can be of arbitrary type.
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
     * rendered for each line, there will be N = ({@code adapter.length(memory)} + 15) / 16 elements in the returned stream.
     * <p>
     * As a consequence of the above, this method renders to a format similar to the *nix command "hexdump -C".
     * <p>
     * As an example, a memory created, initialized and used as follows
     * {@snippet lang = java:
     *   MemorySegment memory = memorySession.allocate(64 + 4);
     *   memory.setUtf8String(0, "The quick brown fox jumped over the lazy dog\nSecond line\t:here");
     *   MemoryInspection.hexDump(memory, Adapter.ofMemorySegment())
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
     * Use a {@linkplain MemorySegment#asSlice(long, long) slice} or similar memory slicing capabilities to
     * inspect a specific region of a memory.
     * <p>
     * This method can be used to directly dump the contents of various other memory abstractions such as
     * {@linkplain ByteBuffer ByteBuffers} and byte arrays:
     * {@snippet lang = java:
     *   MemoryInspection.hexDump(byteArray, Adapter.ofByteArray());
     *   MemoryInspection.hexDump(byteBuffer, Adapter.ofByteBuffer());
     *}
     *
     * @param memory  the memory abstraction to inspect.
     * @param adapter to apply to the provided memory to determine the size and content of the memory abstraction.
     * @param <M>     the memory abstraction type.
     * @return a Stream of human-readable, lines with hexadecimal values
     * @throws RuntimeException depending on the provided extractors whose exceptions will be relayed to the
     *                          call site.
     */
    public static <M> Stream<String> hexDump(M memory,
                                             Adapter<M> adapter) {
        requireNonNull(memory);
        requireNonNull(adapter);
        return MemorySegmentRenderUtil.hexDump(memory, adapter);
    }

    /**
     * Returns a human-readable view of the provided {@code memory} abstraction by optionally
     * (if not a {@link MemorySession} and {@link Adapter#ofMemorySegment()} already) copying the contents
     * to a fresh MemorySegment and then view the resulting MemorySegment through the provided {@code layout}.
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
     * This method is intended to view memory abstractions through small and medium-sized memory layouts.
     *
     * @param memory   to be viewed
     * @param adapter  to apply to the provided memory to determine the size and content of the memory abstraction.
     * @param layout   to use as a layout when viewing the memory segment
     * @param renderer to apply when rendering value layouts
     * @param <M>      the memory abstraction type.
     * @return a view of the memory abstraction viewed through the memory layout
     * @throws OutOfMemoryError if the view exceeds the array size VM limit
     */
    public static <M> String toString(M memory,
                                      Adapter<M> adapter,
                                      MemoryLayout layout,
                                      ValueLayoutRenderer renderer) {
        requireNonNull(memory);
        requireNonNull(adapter);
        requireNonNull(layout);
        requireNonNull(renderer);

        if (memory instanceof MemorySegment segment && adapter == MEMORY_SEGMENT_MEMORY_ADAPTER) {
            return MemorySegmentRenderUtil.toString(segment, layout, renderer);
        }
        long length = adapter.length(memory);
        try (var session = MemorySession.openConfined()) {
            var segment = session.allocate(length, Long.SIZE);
            for (long i = 0; i < length; i++) {
                segment.set(JAVA_BYTE, i, adapter.get(memory, i));
            }
            return MemorySegmentRenderUtil.toString(segment, layout, renderer);
        }
    }

    /**
     * General memory adapter for rendering any memory abstraction.
     *
     * @param <M> the type of memory abstraction (e.g. ByteBuffer, MemorySegment or byte array)
     */
    public interface Adapter<M> {

        /**
         * {@return a byte from the provided {@code  memory} at the provided {@code offset}}.
         *
         * @param memory the memory to read from
         * @param offset the offset in memory to read from
         * @throws RuntimeException if the provided offset is out of bounds or, depending on the memory
         *                          abstraction, for other reasons. The type of exception depends on the underlying
         *                          memory.
         */
        byte get(M memory, long offset);

        /**
         * {@return the length of this memory abstraction}
         *
         * @param memory the memory to read from
         */
        long length(M memory);

        /**
         * {@return a {@code MemoryAdapter<MemorySegment> } that reads byte values from a {@link MemorySegment}}
         */
        static Adapter<MemorySegment> ofMemorySegment() {
            return MEMORY_SEGMENT_MEMORY_ADAPTER;
        }

        /**
         * {@return a {@code MemoryAdapter<ByteBuffer> } that reads byte values from a {@link ByteBuffer}}
         */
        static Adapter<ByteBuffer> ofByteBuffer() {
            return BYTE_BUFFER_MEMORY_ADAPTER;
        }

        /**
         * {@return a {@code MemoryAdapter<byte[]> } that reads byte values from a byte array}
         */
        static Adapter<byte[]> ofByteArray() {
            return BYTE_ARRAY_MEMORY_ADAPTER;
        }

    }

    /**
     * An interface that can be used to specify custom rendering of value
     * layouts via the {@link MemorySegment#toString(MemoryLayout, ValueLayoutRenderer)} method.
     * <p>
     * The render methods take two parameters:
     * <ul>
     *     <li>layout: This can be used to select different formatting for different paths</li>
     *     <li>value: The actual value</li>
     * </ul>
     * <p>
     * The {@linkplain ValueLayoutRenderer#standard() standard() } value layout renderer is path
     * agnostic and will thus render all layouts of the same type the same way.
     *
     * @see MemorySegment#toString(MemoryLayout, ValueLayoutRenderer)
     */
    public interface ValueLayoutRenderer {
        /**
         * Renders the provided {@code layout} and {@code value} to a String.
         *
         * @param layout the layout to render
         * @param value  the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfBoolean layout, boolean value) {
            requireNonNull(layout);
            return Boolean.toString(value);
        }

        /**
         * Renders the provided {@code layout} and {@code value} to a String.
         *
         * @param layout the layout to render
         * @param value  the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfByte layout, byte value) {
            requireNonNull(layout);
            return Byte.toString(value);
        }

        /**
         * Renders the provided {@code layout} and {@code value} to a String.
         *
         * @param layout the layout to render
         * @param value  the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfChar layout, char value) {
            requireNonNull(layout);
            return Character.toString(value);
        }

        /**
         * Renders the provided {@code layout} and {@code value} to a String.
         *
         * @param layout the layout to render
         * @param value  the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfShort layout, short value) {
            requireNonNull(layout);
            return Short.toString(value);
        }

        /**
         * Renders the provided {@code layout} and {@code value} to a String.
         *
         * @param layout the layout to render
         * @param value  the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfInt layout, int value) {
            requireNonNull(layout);
            return Integer.toString(value);
        }

        /**
         * Renders the provided {@code layout} and {@code value} to a String.
         *
         * @param layout the layout to render
         * @param value  the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfLong layout, long value) {
            requireNonNull(layout);
            return Long.toString(value);
        }

        /**
         * Renders the provided {@code layout} and {@code value} to a String.
         *
         * @param layout the layout to render
         * @param value  the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfFloat layout, float value) {
            requireNonNull(layout);
            return Float.toString(value);
        }

        /**
         * Renders the provided {@code layout} and {@code value} to a String.
         *
         * @param layout the layout to render
         * @param value  the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfDouble layout, double value) {
            requireNonNull(layout);
            return Double.toString(value);
        }

        /**
         * Renders the provided {@code layout} and {@code value} to a String.
         *
         * @param layout the layout to render
         * @param value  the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfAddress layout, MemorySegment value) {
            requireNonNull(layout);
            return String.format("0x%0" + (ValueLayout.ADDRESS.byteSize() * 2) + "X", value.address());
        }

        /**
         * {@return a standard value layout renderer that will render numeric values into decimal form and where
         * other value types are rendered to a reasonable "natural" form}
         * <p>
         * More specifically, values types are rendered as follows:
         * <ul>
         *     <li>Numeric values are rendered in decimal form (e.g 1 or 1.2).</li>
         *     <li>Boolean values are rendered as {@code true} or {@code false}.</li>
         *     <li>Character values are rendered as {@code char}.</li>
         *     <li>Address values are rendered in hexadecimal form e.g. {@code 0x0000000000000000} (on 64-bit platforms) or
         *     {@code 0x00000000} (on 32-bit platforms)</li>
         * </ul>
         */
        static ValueLayoutRenderer standard() {
            return STANDARD_VALUE_LAYOUT_RENDERER;
        }

    }
}
