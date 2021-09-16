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

import jdk.internal.foreign.Utils;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import jdk.internal.vm.annotation.Stable;
import sun.invoke.util.Wrapper;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

import static java.lang.constant.ConstantDescs.BSM_GET_STATIC_FINAL;
import static java.lang.constant.ConstantDescs.BSM_INVOKE;

/**
 * A value layout. A value layout is used to model the memory layout associated with values of basic data types, such as <em>integral</em> types
 * (either signed or unsigned) and <em>floating-point</em> types. Each value layout has a size, a {@linkplain ByteOrder byte order})
 * and a <em>carrier</em>, that is, the Java type that should be used when {@linkplain MemorySegment#get(OfInt, long) accessing}
 * a memory region using the value layout.
 * <p>
 * This class defines useful value layout constants for Java primitive types and addresses.
 * The layout constants in this class make implicit alignment and byte-ordering assumption: all layout
 * constants in this class are byte-aligned, and their byte order is set to the {@linkplain ByteOrder#nativeOrder() platform default},
 * thus making it easy to work with other APIs, such as arrays and {@link java.nio.ByteBuffer}.
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; programmers should treat instances that are
 * {@linkplain #equals(Object) equal} as interchangeable and should not
 * use instances for synchronization, or unpredictable behavior may
 * occur. For example, in a future release, synchronization may fail.
 * The {@code equals} method should be used for comparisons.
 *
 * <p> Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more {@code null}
 * elements to a method in this class causes a {@link NullPointerException NullPointerException} to be thrown. </p>
 *
 * @implSpec
 * This class is immutable and thread-safe.
 */
public sealed class ValueLayout extends AbstractLayout implements MemoryLayout {

    private final Class<?> carrier;
    private final ByteOrder order;

    ValueLayout(Class<?> carrier, ByteOrder order, long size) {
        this(carrier, order, size, size, Optional.empty());
    }

    ValueLayout(Class<?> carrier, ByteOrder order, long size, long alignment, Optional<String> name) {
        super(OptionalLong.of(size), alignment, name);
        this.carrier = carrier;
        this.order = order;
        checkCarrierSize(carrier, size);
    }

    /**
     * Returns the value's byte order.
     *
     * @return the value's  byte order.
     */
    public ByteOrder order() {
        return order;
    }

    /**
     * Returns a new value layout with given byte order.
     *
     * @param order the desired byte order.
     * @return a new value layout with given byte order.
     */
    public ValueLayout withOrder(ByteOrder order) {
        return new ValueLayout(carrier, Objects.requireNonNull(order), bitSize(), alignment, name());
    }

    @Override
    public String toString() {
        return decorateLayoutString(String.format("%s%d",
                order == ByteOrder.BIG_ENDIAN ? "B" : "b",
                bitSize()));
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
        return carrier.equals(v.carrier) &&
            order.equals(v.order) &&
            bitSize() == v.bitSize() &&
            alignment == v.alignment;
    }

    /**
     * Returns the carrier associated with this value layout.
     * @return the carrier associated with this value layout.
     */
    public Class<?> carrier() {
        return carrier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), order, bitSize(), alignment);
    }

    @Override
    ValueLayout dup(long alignment, Optional<String> name) {
        return new ValueLayout(carrier, order, bitSize(), alignment, name());
    }

    @Override
    public Optional<DynamicConstantDesc<ValueLayout>> describeConstable() {
        ClassDesc THIS_DESC = getClass().describeConstable().get();
        DynamicConstantDesc<ValueLayout> desc = DynamicConstantDesc.ofNamed(BSM_GET_STATIC_FINAL, specializedConstantName(), THIS_DESC, CD_VALUE_LAYOUT);
        if (order != ByteOrder.nativeOrder()) {
            MethodHandleDesc MH_WITH_ORDER = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL, THIS_DESC, "withOrder",
                    MethodTypeDesc.of(THIS_DESC, CD_BYTEORDER));

            desc = DynamicConstantDesc.ofNamed(BSM_INVOKE, "withOrder", desc.constantType(), MH_WITH_ORDER,
                    desc, order == ByteOrder.BIG_ENDIAN ? BIG_ENDIAN : LITTLE_ENDIAN);
        }
        return Optional.of(decorateLayoutConstant(desc));
    }

    String specializedConstantName() {
        throw new IllegalStateException();
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
    public ValueLayout withBitAlignment(long alignmentBits) {
        return (ValueLayout)super.withBitAlignment(alignmentBits);
    }

    static void checkCarrierSize(Class<?> carrier, long size) {
        if (!isValidCarrier(carrier)) {
            throw new IllegalArgumentException("Invalid carrier: " + carrier.getName());
        }
        if (carrier == void.class) return;
        if (carrier == MemoryAddress.class && size != (Unsafe.ADDRESS_SIZE * 8)) {
            throw new IllegalArgumentException("Address size mismatch: " + (Unsafe.ADDRESS_SIZE * 8) + " != " + size);
        }
        if (carrier.isPrimitive() && Wrapper.forPrimitiveType(carrier).bitWidth() != size &&
                carrier != boolean.class && size != 8) {
            throw new IllegalArgumentException("Carrier size mismatch: " + carrier.getName() + " != " + size);
        }
    }

    static boolean isValidCarrier(Class<?> carrier) {
        return carrier == void.class
                || carrier == boolean.class
                || carrier == byte.class
                || carrier == short.class
                || carrier == char.class
                || carrier == int.class
                || carrier == long.class
                || carrier == float.class
                || carrier == double.class
                || carrier == MemoryAddress.class;
    }

    @Stable
    private VarHandle handle;

    @ForceInline
    VarHandle accessHandle() {
        if (handle == null) {
            handle = Utils.makeMemoryAccessVarHandle(carrier, false, byteAlignment() - 1, order());
        }
        return handle;
    }

    /**
     * A value layout whose carrier is {@code boolean.class}.
     */
    public static final class OfBoolean extends ValueLayout {
        OfBoolean(ByteOrder order, long size) {
            super(boolean.class, order, size);
        }

        OfBoolean(ByteOrder order, long size, long alignment, Optional<String> name) {
            super(boolean.class, order, size, alignment, name);
        }

        @Override
        OfBoolean dup(long alignment, Optional<String> name) {
            return new OfBoolean(order(), bitSize(), alignment, name);
        }

        @Override
        public OfBoolean withName(String name) {
            return (OfBoolean)super.withName(name);
        }

        @Override
        public OfBoolean withBitAlignment(long alignmentBits) {
            return (OfBoolean)super.withBitAlignment(alignmentBits);
        }

        @Override
        public OfBoolean withOrder(ByteOrder order) {
            Objects.requireNonNull(order);
            return new OfBoolean(order, bitSize(), alignment, name());
        }

        @Override
        String specializedConstantName() {
            return "JAVA_BOOLEAN";
        }
    }

    /**
     * A value layout whose carrier is {@code byte.class}.
     */
    public static final class OfByte extends ValueLayout {
        OfByte(ByteOrder order, long size) {
            super(byte.class, order, size);
        }

        OfByte(ByteOrder order, long size, long alignment, Optional<String> name) {
            super(byte.class, order, size, alignment, name);
        }

        @Override
        OfByte dup(long alignment, Optional<String> name) {
            return new OfByte(order(), bitSize(), alignment, name);
        }

        @Override
        public OfByte withName(String name) {
            return (OfByte)super.withName(name);
        }

        @Override
        public OfByte withBitAlignment(long alignmentBits) {
            return (OfByte)super.withBitAlignment(alignmentBits);
        }

        @Override
        public OfByte withOrder(ByteOrder order) {
            Objects.requireNonNull(order);
            return new OfByte(order, bitSize(), alignment, name());
        }

        @Override
        String specializedConstantName() {
            return "JAVA_BYTE";
        }
    }

    /**
     * A value layout whose carrier is {@code char.class}.
     */
    public static final class OfChar extends ValueLayout {
        OfChar(ByteOrder order, long size) {
            super(char.class, order, size);
        }

        OfChar(ByteOrder order, long size, long alignment, Optional<String> name) {
            super(char.class, order, size, alignment, name);
        }

        @Override
        OfChar dup(long alignment, Optional<String> name) {
            return new OfChar(order(), bitSize(), alignment, name);
        }

        @Override
        public OfChar withName(String name) {
            return (OfChar)super.withName(name);
        }

        @Override
        public OfChar withBitAlignment(long alignmentBits) {
            return (OfChar)super.withBitAlignment(alignmentBits);
        }

        @Override
        public OfChar withOrder(ByteOrder order) {
            Objects.requireNonNull(order);
            return new OfChar(order, bitSize(), alignment, name());
        }

        @Override
        String specializedConstantName() {
            return "JAVA_CHAR";
        }
    }

    /**
     * A value layout whose carrier is {@code short.class}.
     */
    public static final class OfShort extends ValueLayout {
        OfShort(ByteOrder order, long size) {
            super(short.class, order, size);
        }

        OfShort(ByteOrder order, long size, long alignment, Optional<String> name) {
            super(short.class, order, size, alignment, name);
        }

        @Override
        OfShort dup(long alignment, Optional<String> name) {
            return new OfShort(order(), bitSize(), alignment, name);
        }

        @Override
        public OfShort withName(String name) {
            return (OfShort)super.withName(name);
        }

        @Override
        public OfShort withBitAlignment(long alignmentBits) {
            return (OfShort)super.withBitAlignment(alignmentBits);
        }

        @Override
        public OfShort withOrder(ByteOrder order) {
            Objects.requireNonNull(order);
            return new OfShort(order, bitSize(), alignment, name());
        }

        @Override
        String specializedConstantName() {
            return "JAVA_SHORT";
        }
    }

    /**
     * A value layout whose carrier is {@code int.class}.
     */
    public static final class OfInt extends ValueLayout {
        OfInt(ByteOrder order, long size) {
            super(int.class, order, size);
        }

        OfInt(ByteOrder order, long size, long alignment, Optional<String> name) {
            super(int.class, order, size, alignment, name);
        }

        @Override
        OfInt dup(long alignment, Optional<String> name) {
            return new OfInt(order(), bitSize(), alignment, name);
        }

        @Override
        public OfInt withName(String name) {
            return (OfInt)super.withName(name);
        }

        @Override
        public OfInt withBitAlignment(long alignmentBits) {
            return (OfInt)super.withBitAlignment(alignmentBits);
        }

        @Override
        public OfInt withOrder(ByteOrder order) {
            Objects.requireNonNull(order);
            return new OfInt(order, bitSize(), alignment, name());
        }

        @Override
        String specializedConstantName() {
            return "JAVA_INT";
        }
    }

    /**
     * A value layout whose carrier is {@code float.class}.
     */
    public static final class OfFloat extends ValueLayout {
        OfFloat(ByteOrder order, long size) {
            super(float.class, order, size);
        }

        OfFloat(ByteOrder order, long size, long alignment, Optional<String> name) {
            super(float.class, order, size, alignment, name);
        }

        @Override
        OfFloat dup(long alignment, Optional<String> name) {
            return new OfFloat(order(), bitSize(), alignment, name);
        }

        @Override
        public OfFloat withName(String name) {
            return (OfFloat)super.withName(name);
        }

        @Override
        public OfFloat withBitAlignment(long alignmentBits) {
            return (OfFloat)super.withBitAlignment(alignmentBits);
        }

        @Override
        public OfFloat withOrder(ByteOrder order) {
            Objects.requireNonNull(order);
            return new OfFloat(order, bitSize(), alignment, name());
        }

        @Override
        String specializedConstantName() {
            return "JAVA_FLOAT";
        }
    }

    /**
     * A value layout whose carrier is {@code long.class}.
     */
    public static final class OfLong extends ValueLayout {
        OfLong(ByteOrder order, long size) {
            super(long.class, order, size);
        }

        OfLong(ByteOrder order, long size, long alignment, Optional<String> name) {
            super(long.class, order, size, alignment, name);
        }

        @Override
        OfLong dup(long alignment, Optional<String> name) {
            return new OfLong(order(), bitSize(), alignment, name);
        }

        @Override
        public OfLong withName(String name) {
            return (OfLong)super.withName(name);
        }

        @Override
        public OfLong withBitAlignment(long alignmentBits) {
            return (OfLong)super.withBitAlignment(alignmentBits);
        }

        @Override
        public OfLong withOrder(ByteOrder order) {
            Objects.requireNonNull(order);
            return new OfLong(order, bitSize(), alignment, name());
        }

        @Override
        String specializedConstantName() {
            return "JAVA_LONG";
        }
    }

    /**
     * A value layout whose carrier is {@code double.class}.
     */
    public static final class OfDouble extends ValueLayout {
        OfDouble(ByteOrder order, long size) {
            super(double.class, order, size);
        }

        OfDouble(ByteOrder order, long size, long alignment, Optional<String> name) {
            super(double.class, order, size, alignment, name);
        }

        @Override
        OfDouble dup(long alignment, Optional<String> name) {
            return new OfDouble(order(), bitSize(), alignment, name);
        }

        @Override
        public OfDouble withName(String name) {
            return (OfDouble)super.withName(name);
        }

        @Override
        public OfDouble withBitAlignment(long alignmentBits) {
            return (OfDouble)super.withBitAlignment(alignmentBits);
        }

        @Override
        public OfDouble withOrder(ByteOrder order) {
            Objects.requireNonNull(order);
            return new OfDouble(order, bitSize(), alignment, name());
        }

        @Override
        String specializedConstantName() {
            return "JAVA_DOUBLE";
        }
    }

    /**
     * A value layout whose carrier is {@code MemoryAddress.class}.
     */
    public static final class OfAddress extends ValueLayout {
        OfAddress(ByteOrder order, long size) {
            super(MemoryAddress.class, order, size);
        }

        OfAddress(ByteOrder order, long size, long alignment, Optional<String> name) {
            super(MemoryAddress.class, order, size, alignment, name);
        }

        @Override
        OfAddress dup(long alignment, Optional<String> name) {
            return new OfAddress(order(), bitSize(), alignment, name);
        }

        @Override
        public OfAddress withName(String name) {
            return (OfAddress)super.withName(name);
        }

        @Override
        public OfAddress withBitAlignment(long alignmentBits) {
            return (OfAddress)super.withBitAlignment(alignmentBits);
        }

        @Override
        public OfAddress withOrder(ByteOrder order) {
            Objects.requireNonNull(order);
            return new OfAddress(order, bitSize(), alignment, name());
        }

        @Override
        String specializedConstantName() {
            return "ADDRESS";
        }
    }

    /**
     * A value layout constant whose size is the same as that of a machine address (e.g. {@code size_t}),
     * bit-alignment set to 8, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final OfAddress ADDRESS = new OfAddress(ByteOrder.nativeOrder(), Unsafe.ADDRESS_SIZE * 8).withBitAlignment(8);

    /**
     * A value layout constant whose size is the same as that of a Java {@code byte},
     * bit-alignment set to 8, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final OfByte JAVA_BYTE = new OfByte(ByteOrder.nativeOrder(), 8).withBitAlignment(8);

    /**
     * A value layout constant whose size is the same as that of a Java {@code boolean},
     * bit-alignment set to 8, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final OfBoolean JAVA_BOOLEAN = new OfBoolean(ByteOrder.nativeOrder(), 8).withBitAlignment(8);

    /**
     * A value layout constant whose size is the same as that of a Java {@code char},
     * bit-alignment set to 8, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final OfChar JAVA_CHAR = new OfChar(ByteOrder.nativeOrder(), 16).withBitAlignment(8);

    /**
     * A value layout constant whose size is the same as that of a Java {@code short},
     * bit-alignment set to 8, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final OfShort JAVA_SHORT = new OfShort(ByteOrder.nativeOrder(), 16).withBitAlignment(8);

    /**
     * A value layout constant whose size is the same as that of a Java {@code int},
     * bit-alignment set to 8, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final OfInt JAVA_INT = new OfInt(ByteOrder.nativeOrder(), 32).withBitAlignment(8);

    /**
     * A value layout constant whose size is the same as that of a Java {@code long},
     * bit-alignment set to 8, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final OfLong JAVA_LONG = new OfLong(ByteOrder.nativeOrder(), 64)
            .withBitAlignment(8);

    /**
     * A value layout constant whose size is the same as that of a Java {@code float},
     * bit-alignment set to 8, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final OfFloat JAVA_FLOAT = new OfFloat(ByteOrder.nativeOrder(), 32).withBitAlignment(8);

    /**
     * A value layout constant whose size is the same as that of a Java {@code double},
     * bit-alignment set to 8, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final OfDouble JAVA_DOUBLE = new OfDouble(ByteOrder.nativeOrder(), 64).withBitAlignment(8);
}
