/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.foreign.layout;

import java.util.Map;
import java.util.Optional;
import jdk.internal.misc.Unsafe;

/**
 * A value layout. This layout is used to model basic native types, such as integral values and floating point numbers.
 * There are three kind of supported value layouts: integer signed, integer unsigned and floating point. Each
 * value layout also has a size and an endianness (which could be either big-endian or little-endian), as well as
 * an optional substructure layout (see {@link Value#contents()}).
 */
public class Value extends AbstractDescriptor<Value> implements Layout {

    /**
     * The value kind.
     */
    public enum Kind {
        /** Kind of signed integral. */
        INTEGRAL_UNSIGNED("u"),
        /** Kind of signed integral. */
        INTEGRAL_SIGNED("i"),
        /** Kind of floating-point value. */
        FLOATING_POINT("f");

        String tag;

        Kind(String tag) {
            this.tag = tag;
        }
    }

    /**
     * value endianness.
     */
    public enum Endianness {
        /** Least-significant-bit stored at lowest address. */
        LITTLE_ENDIAN,
        /** Most-significant-bit stored at lowest address. */
        BIG_ENDIAN;

        public static Endianness hostEndian() {
            return Unsafe.getUnsafe().isBigEndian() ? BIG_ENDIAN : LITTLE_ENDIAN;
        }
    }

    private final Kind kind;
    private final Optional<Endianness> endianness;
    private final long size;
    private final Optional<Group> contents;

    protected Value(Kind kind, Optional<Endianness> endianness, long size, Optional<Group> contents, Map<String, String> annotations) {
        super(annotations);
        this.kind = kind;
        this.endianness = endianness;
        this.size = size;
        this.contents = contents;
    }

    /**
     * Returns the value kind (e.g. integral vs. floating point).
     * @return the value kind.
     */
    public Kind kind() {
        return kind;
    }

    /**
     * Returns the value endianness.
     * @return the value endianness.
     */
    public Optional<Endianness> endianness() {
        return endianness;
    }

    /**
     * Get a Value layout with endianness if it's not specified already.
     * @return Value layout with new specified endianness.
     */
    public Value withEndianness(Endianness newEndian) {
        if (endianness.isEmpty()) {
            return new Value(kind, Optional.of(newEndian), size, contents, annotations());
        } else {
            return this;
        }
    }

    public boolean isSigned() {
        return kind != Kind.INTEGRAL_UNSIGNED;
    }

    /**
     * Returns true if the value endianness match native byte order
     * @return true if endianness match system architecture.
     */
    public boolean isNativeByteOrder() {
        return endianness.isEmpty() ? true : endianness.get() == Endianness.hostEndian();
    }

    @Override
    public long bitsSize() {
        return size;
    }

    @Override
    public boolean isPartial() {
        return contents.map(Layout::isPartial).orElse(false);
    }

    /**
     * Return optional group overlay associated with this value layout. This is useful to view a value
     * as an aggregate.
     * @return group overlay.
     */
    public Optional<Group> contents() {
        return contents;
    }

    public Value withContents(Group contents) {
        return new Value(kind, endianness, size, Optional.of(contents), annotations());
    }

    /**
     * Create a floating-point value of given size.
     * @param size the floating-polong size.
     * @return the new value layout.
     */
    public static Value ofFloatingPoint(long size) {
        return ofFloatingPoint(Optional.empty(), size);
    }

    /**
     * Create a floating-point value of given endianness and size.
     * @param endianness the floating-point endianness
     * @param size the floating-polong size.
     * @return the new value layout.
     */
    public static Value ofFloatingPoint(Endianness endianness, long size) {
        return ofFloatingPoint(Optional.of(endianness), size);
    }

    public static Value ofFloatingPoint(Optional<Endianness> endianness, long size) {
        return new Value(Kind.FLOATING_POINT, endianness, size, Optional.empty(), NO_ANNOS);
    }

    /**
     * Create an unsigned integral value of given size.
     * @param size the integral size.
     * @return the new value layout.
     */
    public static Value ofUnsignedInt(long size) {
        return ofUnsignedInt(Optional.empty(), size);
    }

    /**
     * Create an unsigned integral value of given endianness and size.
     * @param endianness the integral endianness
     * @param size the integral size.
     * @return the new value layout.
     */
    public static Value ofUnsignedInt(Endianness endianness, long size) {
        return ofUnsignedInt(Optional.of(endianness), size);
    }

    public static Value ofUnsignedInt(Optional<Endianness> endianness, long size) {
        return new Value(Kind.INTEGRAL_UNSIGNED, endianness, size, Optional.empty(), NO_ANNOS);
    }

    /**
     * Create an signed integral value of given size.
     * @param size the integral size.
     * @return the new value layout.
     */
    public static Value ofSignedInt(long size) {
        return ofSignedInt(Optional.empty(), size);
    }

    /**
     * Create an signed integral value of given endianness and size.
     * @param endianness the integral endianness
     * @param size the integral size.
     * @return the new value layout.
     */
    public static Value ofSignedInt(Endianness endianness, long size) {
        return ofSignedInt(Optional.of(endianness), size);
    }

    public static Value ofSignedInt(Optional<Endianness> endianness, long size) {
        return new Value(Kind.INTEGRAL_SIGNED, endianness, size, Optional.empty(), NO_ANNOS);
    }

    @Override
    public String toString() {
        String prefix = wrapWithAnnotations(String.format("%s%s%d",
                endianness.map(e -> e == Endianness.BIG_ENDIAN ? ">" : "<").orElse(""),
                kind.tag, size));
        return contents().map(g -> prefix + "=" + g).orElse(prefix);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof Value)) {
            return false;
        }
        Value v = (Value)other;
        return kind.equals(v.kind) && endianness.equals(v.endianness) &&
            size == v.size && contents.equals(v.contents);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ kind.hashCode() ^ endianness.hashCode() ^
            Long.hashCode(size) ^ contents.hashCode();
    }

    @Override
    Value withAnnotations(Map<String, String> annotations) {
        return new Value(kind, endianness, size, contents, annotations);
    }
}
