package jdk.incubator.foreign;

/**
 * Represents a type which is <em>addressable</em>. An addressable type is one which can projected down to
 * a memory address instance (see {@link #address()}). Examples of addressable types are {@link MemorySegment},
 * and {@link MemoryAddress}.
 *
 * @apiNote In the future, if the Java language permits, {@link Addressable}
 * may become a {@code sealed} interface, which would prohibit subclassing except by
 * explicitly permitted types, such as {@link MemorySegment} and {@link MemoryAddress}.
 *
 * @implSpec
 * Implementations of this interface <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
 */
public interface Addressable {
    /**
     * Map this object into a {@link MemoryAddress} instance.
     * @return the {@link MemoryAddress} instance associated with this object.
     */
    MemoryAddress address();
}
