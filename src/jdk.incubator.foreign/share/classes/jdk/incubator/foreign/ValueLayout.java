/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.incubator.foreign;

import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * A value layout. A value layout is used to model the memory layout associated with values of basic data types, such as <em>integral</em> types
 * (either signed or unsigned) and <em>floating-point</em> types. Each value layout has a size and a byte order (see {@link ByteOrder}).
 * Where it's not explicitly provided, a value layout's byte order is assumed to be compatible with the platform byte order (see {@link ByteOrder#nativeOrder()}).
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code ValueLayout} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 */
public class ValueLayout extends AbstractLayout implements MemoryLayout {

    /**
     * The value kind.
     */
    enum Kind {
        /** Kind of unsigned integral value. */
        INTEGRAL_UNSIGNED("u", MH_UNSIGNED),
        /** Signed integral value. */
        INTEGRAL_SIGNED("i", MH_SIGNED),
        /** Kind of floating-point value. */
        FLOATING_POINT("f", MH_FLOAT);

        String tag;
        MethodHandleDesc mhDesc;

        Kind(String tag, MethodHandleDesc mhDesc) {
            this.tag = tag;
            this.mhDesc = mhDesc;
        }
    }

    final Kind kind;
    private final ByteOrder order;
    private final long size;

    ValueLayout(Kind kind, ByteOrder order, long size, OptionalLong alignment, Optional<String> name) {
        super(alignment, name);
        this.kind = kind;
        this.order = order;
        this.size = size;
    }

    /**
     * Returns the value's byte order.
     * @return the value's  byte order.
     */
    public ByteOrder order() {
        return order;
    }

    /**
     * Is this layout associated with a signed integral value?
     * @return true, this layout associated with a signed integral value.
     */
    public boolean isSigned() {
        return kind == Kind.INTEGRAL_SIGNED;
    }

    /**
     * Is this layout associated with an integral value?
     * @return true, this layout associated with an integral value (either signed or unsigned).
     * @see ValueLayout#isSigned()
     */
    public boolean isIntegral() {
        return kind != Kind.FLOATING_POINT;
    }

    @Override
    public long bitSize() {
        return size;
    }

    @Override
    long naturalAlignmentBits() {
        return size;
    }

    @Override
    public String toString() {
        return decorateLayoutString(String.format("%s%d",
                order == ByteOrder.BIG_ENDIAN ?
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
        return kind.equals(v.kind) && order.equals(v.order) &&
            size == v.size;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ kind.hashCode() ^ order.hashCode() ^
            Long.hashCode(size);
    }

    @Override
    ValueLayout dup(OptionalLong alignment, Optional<String> name) {
        return new ValueLayout(kind, order, size, alignment, name);
    }

    @Override
    public Optional<DynamicConstantDesc<ValueLayout>> describeConstable() {
        return Optional.of(DynamicConstantDesc.ofNamed(ConstantDescs.BSM_INVOKE, "value",
                CD_VALUE_LAYOUT, kind.mhDesc, order == ByteOrder.BIG_ENDIAN ? BIG_ENDIAN : LITTLE_ENDIAN, size));
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
    public ValueLayout withBitAlignment(long alignmentBits) throws IllegalArgumentException {
        return (ValueLayout)super.withBitAlignment(alignmentBits);
    }
}
