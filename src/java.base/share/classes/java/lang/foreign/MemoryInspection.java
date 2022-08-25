package java.lang.foreign;

import jdk.internal.foreign.MemoryInspectionUtil;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static jdk.internal.foreign.MemoryInspectionUtil.*;

/**
 * Class that supports inspection of various memory abstractions such as memory of type
 * MemorySegment, ByteBuffer and byte arrays.
 * <p>
 * The methods in this class are not thread safe.
 */
public final class MemoryInspection {

    // Suppresses default constructor, ensuring non-instantiability.
    private MemoryInspection() {
    }

    /**
     * Returns a human-readable view of the provided {@linkplain MemorySegment memory} viewed
     * through the provided {@code layout} using the provided {@linkplain ValueLayoutRenderer renderer}.
     * <p>
     * The exact format of the returned view is unspecified and should not
     * be acted upon programmatically.
     * <p>
     * As an example, a MemorySegment viewed though the following memory layout
     * {@snippet lang = java:
     * var layout = MemoryLayout.structLayout(
     *         ValueLayout.JAVA_INT.withName("x"),
     *         ValueLayout.JAVA_INT.withName("y")
     * ).withName("Point");
     *
     * MemoryInspection.inspect(segment, layout, ValueLayoutRenderer.standard())
     *     .forEach(System.out::println);
     *
     *}
     * might be rendered to something like this:
     * {@snippet lang = text:
     * Point {
     *   x=1,
     *   y=2
     * }
     *}
     * <p>
     * This method is intended to view memory segments through small and medium-sized memory layouts.
     *
     * @param segment  to be viewed
     * @param layout   to use as a layout when viewing the memory segment
     * @param renderer to apply when rendering value layouts
     * @return a view of the memory abstraction viewed through the memory layout
     */
    public static Stream<String> inspect(MemorySegment segment,
                                         MemoryLayout layout,
                                         ValueLayoutRenderer renderer) {
        requireNonNull(segment);
        requireNonNull(layout);
        requireNonNull(renderer);
        return MemoryInspectionUtil.inspect(segment, layout, renderer);
    }

    /**
     * An interface that can be used to specify custom rendering of value
     * layouts via the {@link MemoryInspection#inspect(MemorySegment, MemoryLayout, ValueLayoutRenderer)} method.
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
     * @see MemoryInspection#inspect(MemorySegment, MemoryLayout, ValueLayoutRenderer)
     */
    public interface ValueLayoutRenderer {
        /**
         * Renders the provided {@code booleanLayout} and {@code value} to a String.
         *
         * @param booleanLayout the layout to render
         * @param value         the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfBoolean booleanLayout, boolean value) {
            requireNonNull(booleanLayout);
            return Boolean.toString(value);
        }

        /**
         * Renders the provided {@code booleanLayout} and {@code value} to a String.
         *
         * @param booleanLayout the layout to render
         * @param value         the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfByte booleanLayout, byte value) {
            requireNonNull(booleanLayout);
            return Byte.toString(value);
        }

        /**
         * Renders the provided {@code charLayout} and {@code value} to a String.
         *
         * @param charLayout the layout to render
         * @param value      the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfChar charLayout, char value) {
            requireNonNull(charLayout);
            return Character.toString(value);
        }

        /**
         * Renders the provided {@code sortLayout} and {@code value} to a String.
         *
         * @param sortLayout the layout to render
         * @param value      the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfShort sortLayout, short value) {
            requireNonNull(sortLayout);
            return Short.toString(value);
        }

        /**
         * Renders the provided {@code intLayout} and {@code value} to a String.
         *
         * @param intLayout the layout to render
         * @param value     the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfInt intLayout, int value) {
            requireNonNull(intLayout);
            return Integer.toString(value);
        }

        /**
         * Renders the provided {@code longLayout} and {@code value} to a String.
         *
         * @param longLayout the layout to render
         * @param value      the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfLong longLayout, long value) {
            requireNonNull(longLayout);
            return Long.toString(value);
        }

        /**
         * Renders the provided {@code floatLayout} and {@code value} to a String.
         *
         * @param floatLayout the layout to render
         * @param value       the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfFloat floatLayout, float value) {
            requireNonNull(floatLayout);
            return Float.toString(value);
        }

        /**
         * Renders the provided {@code doubleLayout} and {@code value} to a String.
         *
         * @param doubleLayout the layout to render
         * @param value        the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfDouble doubleLayout, double value) {
            requireNonNull(doubleLayout);
            return Double.toString(value);
        }

        /**
         * Renders the provided {@code addressLayout} and {@code value} to a String.
         *
         * @param addressLayout the layout to render
         * @param value         the value to render
         * @return rendered String
         */
        default String render(ValueLayout.OfAddress addressLayout, MemorySegment value) {
            requireNonNull(addressLayout);
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
