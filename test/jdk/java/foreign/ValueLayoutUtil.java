import jdk.internal.foreign.layout.ValueLayouts;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.Objects;

public final class ValueLayoutUtil {

    private ValueLayoutUtil() {
    }

    /**
     * Creates a value layout of given Java carrier and byte order. The type of resulting value layout is determined
     * by the carrier provided:
     * <ul>
     *     <li>{@link ValueLayout.OfBoolean}, for {@code boolean.class}</li>
     *     <li>{@link ValueLayout.OfByte}, for {@code byte.class}</li>
     *     <li>{@link ValueLayout.OfShort}, for {@code short.class}</li>
     *     <li>{@link ValueLayout.OfChar}, for {@code char.class}</li>
     *     <li>{@link ValueLayout.OfInt}, for {@code int.class}</li>
     *     <li>{@link ValueLayout.OfFloat}, for {@code float.class}</li>
     *     <li>{@link ValueLayout.OfLong}, for {@code long.class}</li>
     *     <li>{@link ValueLayout.OfDouble}, for {@code double.class}</li>
     *     <li>{@link ValueLayout.OfAddress}, for {@code MemorySegment.class}</li>
     * </ul>
     * @param carrier the value layout carrier.
     * @param order the value layout's byte order.
     * @return a value layout with the given Java carrier and byte-order.
     * @throws IllegalArgumentException if the carrier type is not supported.
     */
    static ValueLayout valueLayout(Class<?> carrier, ByteOrder order) {
        Objects.requireNonNull(carrier);
        Objects.requireNonNull(order);
        if (carrier == boolean.class) {
            return ValueLayouts.OfBooleanImpl.of(order);
        } else if (carrier == char.class) {
            return ValueLayouts.OfCharImpl.of(order);
        } else if (carrier == byte.class) {
            return ValueLayouts.OfByteImpl.of(order);
        } else if (carrier == short.class) {
            return ValueLayouts.OfShortImpl.of(order);
        } else if (carrier == int.class) {
            return ValueLayouts.OfIntImpl.of(order);
        } else if (carrier == float.class) {
            return ValueLayouts.OfFloatImpl.of(order);
        } else if (carrier == long.class) {
            return ValueLayouts.OfLongImpl.of(order);
        } else if (carrier == double.class) {
            return ValueLayouts.OfDoubleImpl.of(order);
        } else if (carrier == MemorySegment.class) {
            return ValueLayouts.OfAddressImpl.of(order);
        } else {
            throw new IllegalArgumentException("Unsupported carrier: " + carrier.getName());
        }
    }

}
