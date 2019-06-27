/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package java.foreign;

import jdk.internal.foreign.LayoutPathImpl;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * A value layout. This layout is used to model basic native types, such as integral values and floating point numbers.
 * There are three kind of supported value layouts: integer signed, integer unsigned and floating point. Each
 * value layout also has a size and an endianness (which could be either big-endian or little-endian).
 */
public class ValueLayout extends AbstractLayout implements Layout {

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

    private final Kind kind;
    private final ByteOrder endianness;
    private final long size;

    ValueLayout(Kind kind, ByteOrder endianness, long size, OptionalLong alignment, Optional<String> name) {
        super(alignment, name);
        this.kind = kind;
        this.endianness = endianness;
        this.size = size;
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
    public ByteOrder endianness() {
        return endianness;
    }

    /**
     * Is this value a signed layout?
     * @return true, if this value layout is signed.
     */
    public boolean isSigned() {
        return kind != Kind.INTEGRAL_UNSIGNED;
    }

    /**
     * Returns true if the value endianness match native byte order
     * @return true if endianness match system architecture.
     */
    public boolean isNativeByteOrder() {
        return endianness == ByteOrder.nativeOrder();
    }

    @Override
    public long bitsSize() {
        return size;
    }

    @Override
    long naturalAlignmentBits() {
        return size;
    }

    /**
     * Create a floating-point value of given size.
     * @param size the floating-polong size.
     * @return the new value layout.
     * @throws IllegalArgumentException if size is &le; 0.
     */
    public static ValueLayout ofFloatingPoint(long size) throws IllegalArgumentException  {
        checkSize(size);
        return ofFloatingPoint(ByteOrder.nativeOrder(), size);
    }

    /**
     * Create a floating-point value of given endianness and size.
     * @param endianness the floating-point endianness
     * @param size the floating-point size.
     * @return the new value layout.
     * @throws IllegalArgumentException if size is &le; 0.
     */
    public static ValueLayout ofFloatingPoint(ByteOrder endianness, long size) throws IllegalArgumentException {
        checkSize(size);
        return new ValueLayout(Kind.FLOATING_POINT, endianness, size, OptionalLong.empty(), Optional.empty());
    }

    /**
     * Create an unsigned integral value of given size.
     * @param size the integral size.
     * @return the new value layout.
     * @throws IllegalArgumentException if size is &le; 0.
     */
    public static ValueLayout ofUnsignedInt(long size) throws IllegalArgumentException {
        checkSize(size);
        return ofUnsignedInt(ByteOrder.nativeOrder(), size);
    }

    /**
     * Create an unsigned integral value of given endianness and size.
     * @param endianness the integral endianness
     * @param size the integral size.
     * @return the new value layout.
     * @throws IllegalArgumentException if size is &le; 0.
     */
    public static ValueLayout ofUnsignedInt(ByteOrder endianness, long size) throws IllegalArgumentException {
        checkSize(size);
        return new ValueLayout(Kind.INTEGRAL_UNSIGNED, endianness, size, OptionalLong.empty(), Optional.empty());
    }

    /**
     * Create an signed integral value of given size.
     * @param size the integral size.
     * @return the new value layout.
     * @throws IllegalArgumentException if size is &le; 0.
     */
    public static ValueLayout ofSignedInt(long size) throws IllegalArgumentException  {
        checkSize(size);
        return ofSignedInt(ByteOrder.nativeOrder(), size);
    }

    /**
     * Create an signed integral value of given endianness and size.
     * @param endianness the integral endianness
     * @param size the integral size.
     * @return the new value layout.
     * @throws IllegalArgumentException if size is &le; 0.
     */
    public static ValueLayout ofSignedInt(ByteOrder endianness, long size) throws IllegalArgumentException  {
        checkSize(size);
        return new ValueLayout(Kind.INTEGRAL_SIGNED, endianness, size, OptionalLong.empty(), Optional.empty());
    }

    @Override
    public String toString() {
        return decorateLayoutString(String.format("%s%d",
                endianness == ByteOrder.BIG_ENDIAN ?
                        kind.tag.toUpperCase() : kind.tag,
                size));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof ValueLayout)) {
            return false;
        }
        ValueLayout v = (ValueLayout)other;
        return kind.equals(v.kind) && endianness.equals(v.endianness) &&
            size == v.size;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ kind.hashCode() ^ endianness.hashCode() ^
            Long.hashCode(size);
    }

    @Override
    ValueLayout dup(OptionalLong alignment, Optional<String> name) {
        return new ValueLayout(kind, endianness, size, alignment, name);
    }

    /**
     * A var handle that can be used to dereference this value layout.
     * @param carrier the var handle carrier type.
     * @return a var handle which can be used to dereference this value layout.
     * @throws IllegalArgumentException if the carrier does not represent a primitive type, if the carrier is {@code void},
     * {@code boolean}, or if the size of the carrier type does not match that of the layout targeted by this layout path.
     */
    public VarHandle dereferenceHandle(Class<?> carrier) throws IllegalArgumentException {
        return LayoutPathImpl.rootPath(this).dereferenceHandle(carrier);
    }

    //hack: the declarations below are to make javadoc happy; we could have used generics in AbstractLayout
    //but that causes issues with javadoc, see JDK-8224052

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueLayout withName(String name) {
        return (ValueLayout)super.withName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueLayout alignTo(long alignmentBits) throws IllegalArgumentException {
        return (ValueLayout)super.alignTo(alignmentBits);
    }
}
