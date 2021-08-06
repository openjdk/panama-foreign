/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

package jdk.incubator.foreign;

import jdk.internal.foreign.AbstractMemorySegmentImpl;
import jdk.internal.misc.ScopedMemoryAccess;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Hello
 */
public final class MemorySegments {

    static final ScopedMemoryAccess scopedMemoryAccess = ScopedMemoryAccess.getScopedMemoryAccess();
    private final static ByteOrder NON_NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ?
            ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    private final static ValueLayout JAVA_SHORT_NO = MemoryLayouts.JAVA_SHORT.withBitAlignment(8);
    private final static ValueLayout JAVA_SHORT_NNO = JAVA_SHORT_NO.withOrder(NON_NATIVE_ORDER);
    private final static ValueLayout JAVA_CHAR_NO = MemoryLayouts.JAVA_CHAR.withBitAlignment(8);
    private final static ValueLayout JAVA_CHAR_NNO = JAVA_CHAR_NO.withOrder(NON_NATIVE_ORDER);
    private final static ValueLayout JAVA_INT_NO = MemoryLayouts.JAVA_INT.withBitAlignment(8);
    private final static ValueLayout JAVA_INT_NNO = JAVA_INT_NO.withOrder(NON_NATIVE_ORDER);
    private final static ValueLayout JAVA_FLOAT_NO = MemoryLayouts.JAVA_FLOAT.withBitAlignment(8);
    private final static ValueLayout JAVA_FLOAT_NNO = JAVA_FLOAT_NO.withOrder(NON_NATIVE_ORDER);
    private final static ValueLayout JAVA_LONG_NO = MemoryLayouts.JAVA_LONG.withBitAlignment(8);
    private final static ValueLayout JAVA_LONG_NNO = JAVA_LONG_NO.withOrder(NON_NATIVE_ORDER);
    private final static ValueLayout JAVA_DOUBLE_NO = MemoryLayouts.JAVA_DOUBLE.withBitAlignment(8);
    private final static ValueLayout JAVA_DOUBLE_NNO = JAVA_DOUBLE_NO.withOrder(NON_NATIVE_ORDER);

    private MemorySegments() {
        // just the one!
    }

    private static final VarHandle byte_handle = MemoryHandles.varHandle(byte.class, ByteOrder.nativeOrder());
    private static final VarHandle bool_handle = MemoryHandles.varHandle(boolean.class, ByteOrder.nativeOrder());
    private static final VarHandle char_LE_handle = unalignedHandle(MemoryLayouts.BITS_16_LE, char.class);
    private static final VarHandle short_LE_handle = unalignedHandle(MemoryLayouts.BITS_16_LE, short.class);
    private static final VarHandle int_LE_handle = unalignedHandle(MemoryLayouts.BITS_32_LE, int.class);
    private static final VarHandle float_LE_handle = unalignedHandle(MemoryLayouts.BITS_32_LE, float.class);
    private static final VarHandle long_LE_handle = unalignedHandle(MemoryLayouts.BITS_64_LE, long.class);
    private static final VarHandle double_LE_handle = unalignedHandle(MemoryLayouts.BITS_64_LE, double.class);
    private static final VarHandle char_BE_handle = unalignedHandle(MemoryLayouts.BITS_16_BE, char.class);
    private static final VarHandle short_BE_handle = unalignedHandle(MemoryLayouts.BITS_16_BE, short.class);
    private static final VarHandle int_BE_handle = unalignedHandle(MemoryLayouts.BITS_32_BE, int.class);
    private static final VarHandle float_BE_handle = unalignedHandle(MemoryLayouts.BITS_32_BE, float.class);
    private static final VarHandle long_BE_handle = unalignedHandle(MemoryLayouts.BITS_64_BE, long.class);
    private static final VarHandle double_BE_handle = unalignedHandle(MemoryLayouts.BITS_64_BE, double.class);
    private static final VarHandle address_handle = unalignedHandle(MemoryLayouts.ADDRESS, MemoryAddress.class);

    static VarHandle unalignedHandle(ValueLayout elementLayout, Class<?> carrier) {
        return MemoryHandles.varHandle(carrier, 1, elementLayout.order());
    }

    /**
     * Reads a byte from given segment and offset.
     *
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @return a byte value read from {@code segment}.
     */
    @ForceInline
    public static byte getByte(MemorySegment segment, long offset) {
        Objects.requireNonNull(segment);
        return (byte)byte_handle.get(segment, offset);
    }

    /**
     * Writes a byte at given segment and offset.
     *
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param value the byte value to be written.
     */
    @ForceInline
    public static void setByte(MemorySegment segment, long offset, byte value) {
        Objects.requireNonNull(segment);
        byte_handle.set(segment, offset, value);
    }

    /**
     * Reads a boolean from given segment and offset.
     *
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @return a boolean value read from {@code segment}.
     */
    @ForceInline
    public static boolean getBoolean(MemorySegment segment, long offset) {
        Objects.requireNonNull(segment);
        return (boolean)bool_handle.get(segment, offset);
    }

    /**
     * Writes a boolean at given segment and offset.
     *
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param value the boolean value to be written.
     */
    @ForceInline
    public static void setBoolean(MemorySegment segment, long offset, boolean value) {
        Objects.requireNonNull(segment);
        bool_handle.set(segment, offset, value);
    }

    /**
     * Reads a char from given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    getCharAtOffset(segment, offset, ByteOrder.nativeOrder());
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @return a char value read from {@code segment}.
     */
    @ForceInline
    public static char getChar(MemorySegment segment, long offset) {
        return getChar(segment, offset, ByteOrder.nativeOrder());
    }

    /**
     * Writes a char at given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setCharAtOffset(segment, offset, ByteOrder.nativeOrder(), value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param value the char value to be written.
     */
    @ForceInline
    public static void setChar(MemorySegment segment, long offset, char value) {
        setChar(segment, offset, ByteOrder.nativeOrder(), value);
    }

    /**
     * Reads a short from given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    getShortAtOffset(segment, offset, ByteOrder.nativeOrder());
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @return a short value read from {@code segment}.
     */
    @ForceInline
    public static short getShort(MemorySegment segment, long offset) {
        return getShort(segment, offset, ByteOrder.nativeOrder());
    }

    /**
     * Writes a short at given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShortAtOffset(segment, offset, ByteOrder.nativeOrder(), value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param value the short value to be written.
     */
    @ForceInline
    public static void setShort(MemorySegment segment, long offset, short value) {
        setShort(segment, offset, ByteOrder.nativeOrder(), value);
    }

    /**
     * Reads an int from given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    getIntAtOffset(segment, offset, ByteOrder.nativeOrder());
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @return an int value read from {@code segment}.
     */
    @ForceInline
    public static int getInt(MemorySegment segment, long offset) {
        return getInt(segment, offset, ByteOrder.nativeOrder());
    }

    /**
     * Writes an int at given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setIntAtOffset(segment, offset, ByteOrder.nativeOrder(), value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param value the int value to be written.
     */
    @ForceInline
    public static void setInt(MemorySegment segment, long offset, int value) {
        setInt(segment, offset, ByteOrder.nativeOrder(), value);
    }

    /**
     * Reads a float from given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    getFloatAtOffset(segment, offset, ByteOrder.nativeOrder());
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @return a float value read from {@code segment}.
     */
    @ForceInline
    public static float getFloat(MemorySegment segment, long offset) {
        return getFloat(segment, offset, ByteOrder.nativeOrder());
    }

    /**
     * Writes a float at given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloatAtOffset(segment, offset, ByteOrder.nativeOrder(), value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param value the float value to be written.
     */
    @ForceInline
    public static void setFloat(MemorySegment segment, long offset, float value) {
        setFloat(segment, offset, ByteOrder.nativeOrder(), value);
    }

    /**
     * Reads a long from given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    getLongAtOffset(segment, offset, ByteOrder.nativeOrder());
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @return a long value read from {@code segment}.
     */
    @ForceInline
    public static long getLong(MemorySegment segment, long offset) {
        return getLong(segment, offset, ByteOrder.nativeOrder());
    }

    /**
     * Writes a long at given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLongAtOffset(segment, offset, ByteOrder.nativeOrder(), value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param value the long value to be written.
     */
    @ForceInline
    public static void setLong(MemorySegment segment, long offset, long value) {
        setLong(segment, offset, ByteOrder.nativeOrder(), value);
    }

    /**
     * Reads a double from given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    getDoubleAtOffset(segment, offset, ByteOrder.nativeOrder());
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @return a double value read from {@code segment}.
     */
    @ForceInline
    public static double getDouble(MemorySegment segment, long offset) {
        return getDouble(segment, offset, ByteOrder.nativeOrder());
    }

    /**
     * Writes a double at given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDoubleAtOffset(segment, offset, ByteOrder.nativeOrder(), value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param value the double value to be written.
     */
    @ForceInline
    public static void setDouble(MemorySegment segment, long offset, double value) {
        setDouble(segment, offset, ByteOrder.nativeOrder(), value);
    }

    /**
     * Reads a memory address from given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent (e.g. on a 64-bit platform) to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.asAddressHandle(MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder()));
    MemoryAddress value = (MemoryAddress)handle.get(segment, offset);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @return a memory address read from {@code segment}.
     */
    @ForceInline
    public static MemoryAddress getAddress(MemorySegment segment, long offset) {
        Objects.requireNonNull(segment);
        return (MemoryAddress)address_handle.get(segment, offset);
    }

    /**
     * Writes a memory address at given segment and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent (e.g. on a 64-bit platform) to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.asAddressHandle(MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder()));
    handle.set(segment, offset, value.address());
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param value the memory address to be written (expressed as an {@link Addressable} instance).
     */
    @ForceInline
    public static void setAddress(MemorySegment segment, long offset, Addressable value) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(value);
        address_handle.set(segment, offset, value.address());
    }

    /**
     * Reads a char from given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(char.class, 1, order);
    char value = (char)handle.get(segment, offset);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @return a char value read from {@code segment}.
     */
    @ForceInline
    public static char getChar(MemorySegment segment, long offset, ByteOrder order) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        return (char)((order == ByteOrder.BIG_ENDIAN) ? char_BE_handle : char_LE_handle).get(segment, offset);
    }

    /**
     * Writes a char at given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(char.class, 1, order);
    handle.set(segment, offset, value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @param value the char value to be written.
     */
    @ForceInline
    public static void setChar(MemorySegment segment, long offset, ByteOrder order, char value) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        ((order == ByteOrder.BIG_ENDIAN) ? char_BE_handle : char_LE_handle).set(segment, offset, value);
    }

    /**
     * Reads a short from given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(short.class, 1, order);
    short value = (short)handle.get(segment, offset);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @return a short value read from {@code segment}.
     */
    @ForceInline
    public static short getShort(MemorySegment segment, long offset, ByteOrder order) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        return (short)((order == ByteOrder.BIG_ENDIAN) ? short_BE_handle : short_LE_handle).get(segment, offset);
    }

    /**
     * Writes a short at given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(short.class, 1, order);
    handle.set(segment, offset, value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @param value the short value to be written.
     */
    @ForceInline
    public static void setShort(MemorySegment segment, long offset, ByteOrder order, short value) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        ((order == ByteOrder.BIG_ENDIAN) ? short_BE_handle : short_LE_handle).set(segment, offset, value);
    }

    /**
     * Reads an int from given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(int.class, 1, order);
    int value = (int)handle.get(segment, offset);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @return an int value read from {@code segment}.
     */
    @ForceInline
    public static int getInt(MemorySegment segment, long offset, ByteOrder order) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        return (int)((order == ByteOrder.BIG_ENDIAN) ? int_BE_handle : int_LE_handle).get(segment, offset);
    }

    /**
     * Writes an int at given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(int.class, 1, order);
    handle.set(segment, offset, value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @param value the int value to be written.
     */
    @ForceInline
    public static void setInt(MemorySegment segment, long offset, ByteOrder order, int value) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        ((order == ByteOrder.BIG_ENDIAN) ? int_BE_handle : int_LE_handle).set(segment, offset, value);
    }

    /**
     * Reads a float from given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(float.class, 1, order);
    float value = (float)handle.get(segment, offset);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @return a float value read from {@code segment}.
     */
    @ForceInline
    public static float getFloat(MemorySegment segment, long offset, ByteOrder order) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        return (float)((order == ByteOrder.BIG_ENDIAN) ? float_BE_handle : float_LE_handle).get(segment, offset);
    }

    /**
     * Writes a float at given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(float.class, 1, order);
    handle.set(segment, offset, value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @param value the float value to be written.
     */
    @ForceInline
    public static void setFloat(MemorySegment segment, long offset, ByteOrder order, float value) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        ((order == ByteOrder.BIG_ENDIAN) ? float_BE_handle : float_LE_handle).set(segment, offset, value);
    }

    /**
     * Reads a long from given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(long.class, 1, order);
    long value = (long)handle.get(segment, offset);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @return a long value read from {@code segment}.
     */
    @ForceInline
    public static long getLong(MemorySegment segment, long offset, ByteOrder order) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        return (long)((order == ByteOrder.BIG_ENDIAN) ? long_BE_handle : long_LE_handle).get(segment, offset);
    }

    /**
     * Writes a long at given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(long.class, 1, order);
    handle.set(segment, offset, value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @param value the long value to be written.
     */
    @ForceInline
    public static void setLong(MemorySegment segment, long offset, ByteOrder order, long value) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        ((order == ByteOrder.BIG_ENDIAN) ? long_BE_handle : long_LE_handle).set(segment, offset, value);
    }

    /**
     * Reads a double from given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(double.class, 1, order);
    double value = (double)handle.get(segment, offset);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @return a double value read from {@code segment}.
     */
    @ForceInline
    public static double getDouble(MemorySegment segment, long offset, ByteOrder order) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        return (double)((order == ByteOrder.BIG_ENDIAN) ? double_BE_handle : double_LE_handle).get(segment, offset);
    }

    /**
     * Writes a double at given segment and offset with given byte order.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.varHandle(double.class, 1, order);
    handle.set(segment, offset, value);
     * }</pre></blockquote>
     * @param segment the segment to be dereferenced.
     * @param offset offset in bytes (relative to {@code segment}). The final address of this read operation can be expressed as {@code segment.address().addOffset(offset)}.
     * @param order the specified byte order.
     * @param value the double value to be written.
     */
    @ForceInline
    public static void setDouble(MemorySegment segment, long offset, ByteOrder order, double value) {
        Objects.requireNonNull(segment);
        Objects.requireNonNull(order);
        ((order == ByteOrder.BIG_ENDIAN) ? double_BE_handle : double_LE_handle).set(segment, offset, value);
    }

    private static ValueLayout pick(ByteOrder order, ValueLayout nativeLayout, ValueLayout nonNativeLayout) {
        Objects.requireNonNull(order);
        return order == ByteOrder.nativeOrder() ?
                nativeLayout : nonNativeLayout;
    }

    /**
     * Copies a number of byte elements from a source byte array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source byte array.
     * @param srcIndex the starting index of the source byte array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of byte elements to be copied.
     */
    @ForceInline
    public static void copy(
            byte[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, MemoryLayouts.JAVA_BYTE, dstOffset, elementCount,
                Unsafe.ARRAY_BYTE_BASE_OFFSET, Unsafe.ARRAY_BYTE_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of byte elements from a source segment to a destination byte array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination byte array.
     * @param dstIndex the starting index of the destination byte array.
     * @param elementCount the number of byte elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            byte[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, MemoryLayouts.JAVA_BYTE, srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_BYTE_BASE_OFFSET, Unsafe.ARRAY_BYTE_INDEX_SCALE, dstArray.length);
    }

    /**
     * Copies a number of char elements from a source char array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source char array.
     * @param srcIndex the starting index of the source char array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of char elements to be copied.
     */
    @ForceInline
    public static void copy(
            char[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of char elements from a source char array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source char array.
     * @param srcIndex the starting index of the source char array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of char elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            char[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount, ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_CHAR_NO, JAVA_CHAR_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_CHAR_BASE_OFFSET, Unsafe.ARRAY_CHAR_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of char elements from a source segment to a destination char array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination char array.
     * @param dstIndex the starting index of the destination char array.
     * @param elementCount the number of char elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            char[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of char elements from a source segment to a destination char array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination char array.
     * @param dstIndex the starting index of the destination char array.
     * @param elementCount the number of char elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            char[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_CHAR_NO, JAVA_CHAR_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_CHAR_BASE_OFFSET, Unsafe.ARRAY_CHAR_INDEX_SCALE, dstArray.length);
    }

    /**
     * Copies a number of short elements from a source short array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source short array.
     * @param srcIndex the starting index of the source short array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of short elements to be copied.
     */
    @ForceInline
    public static void copy(
            short[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of short elements from a source short array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source short array.
     * @param srcIndex the starting index of the source short array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of short elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            short[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount,
            ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_SHORT_NO, JAVA_SHORT_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_SHORT_BASE_OFFSET, Unsafe.ARRAY_SHORT_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of short elements from a source segment to a destination short array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination short array.
     * @param dstIndex the starting index of the destination short array.
     * @param elementCount the number of short elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            short[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of short elements from a source segment to a destination short array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination short array.
     * @param dstIndex the starting index of the destination short array.
     * @param elementCount the number of short elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            short[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_SHORT_NO, JAVA_SHORT_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_SHORT_BASE_OFFSET, Unsafe.ARRAY_SHORT_INDEX_SCALE, dstArray.length);
    }

    /**
     * Copies a number of int elements from a source int array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source int array.
     * @param srcIndex the starting index of the source int array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of int elements to be copied.
     */
    @ForceInline
    public static void copy(
            int[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of int elements from a source int array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source int array.
     * @param srcIndex the starting index of the source int array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of int elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            int[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount,
            ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_INT_NO, JAVA_INT_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_INT_BASE_OFFSET, Unsafe.ARRAY_INT_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of int elements from a source segment to a destination int array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination int array.
     * @param dstIndex the starting index of the destination int array.
     * @param elementCount the number of int elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            int[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of int elements from a source segment to a destination int array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination int array.
     * @param dstIndex the starting index of the destination int array.
     * @param elementCount the number of int elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            int[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_INT_NO, JAVA_INT_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_INT_BASE_OFFSET, Unsafe.ARRAY_INT_INDEX_SCALE, dstArray.length);
    }

    /**
     * Copies a number of float elements from a source float array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source float array.
     * @param srcIndex the starting index of the source float array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of float elements to be copied.
     */
    @ForceInline
    public static void copy(
            float[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of float elements from a source float array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source float array.
     * @param srcIndex the starting index of the source float array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of float elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            float[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount,
            ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_FLOAT_NO, JAVA_FLOAT_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_FLOAT_BASE_OFFSET, Unsafe.ARRAY_FLOAT_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of float elements from a source segment to a destination float array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination float array.
     * @param dstIndex the starting index of the destination float array.
     * @param elementCount the number of float elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            float[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of float elements from a source segment to a destination float array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination float array.
     * @param dstIndex the starting index of the destination float array.
     * @param elementCount the number of float elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a float swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            float[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_FLOAT_NO, JAVA_FLOAT_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_FLOAT_BASE_OFFSET, Unsafe.ARRAY_FLOAT_INDEX_SCALE, dstArray.length);
    }

    /**
     * Copies a number of long elements from a source long array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source long array.
     * @param srcIndex the starting index of the source long array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of long elements to be copied.
     */
    @ForceInline
    public static void copy(
            long[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of long elements from a source long array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source long array.
     * @param srcIndex the starting index of the source long array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of long elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            long[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount,
            ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_LONG_NO, JAVA_LONG_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_LONG_BASE_OFFSET, Unsafe.ARRAY_LONG_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of long elements from a source segment to a destination long array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination long array.
     * @param dstIndex the starting index of the destination long array.
     * @param elementCount the number of long elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            long[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of long elements from a source segment to a destination long array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination long array.
     * @param dstIndex the starting index of the destination long array.
     * @param elementCount the number of long elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            long[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_LONG_NO, JAVA_LONG_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_LONG_BASE_OFFSET, Unsafe.ARRAY_LONG_INDEX_SCALE, dstArray.length);
    }

    /**
     * Copies a number of double elements from a source double array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes).
     * @param srcArray the source double array.
     * @param srcIndex the starting index of the source double array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of double elements to be copied.
     */
    @ForceInline
    public static void copy(
            double[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of double elements from a source double array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source double array.
     * @param srcIndex the starting index of the source double array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of double elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     */
    @ForceInline
    public static void copy(
            double[] srcArray, int srcIndex, MemorySegment dstSegment, long dstOffset, int elementCount,
            ByteOrder order) {
        copy(srcArray, srcIndex, dstSegment, pick(order, JAVA_DOUBLE_NO, JAVA_DOUBLE_NNO), dstOffset, elementCount,
                Unsafe.ARRAY_DOUBLE_BASE_OFFSET, Unsafe.ARRAY_DOUBLE_INDEX_SCALE, srcArray.length);
    }

    /**
     * Copies a number of double elements from a source segment to a destination double array,
     * starting at a given segment offset (expressed in bytes), and a given array index.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination double array.
     * @param dstIndex the starting index of the destination double array.
     * @param elementCount the number of double elements to be copied.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            double[] dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of double elements from a source segment to a destination double array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination double array.
     * @param dstIndex the starting index of the destination double array.
     * @param elementCount the number of double elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            double[] dstArray, int dstIndex, int elementCount,
            ByteOrder order) {
        copy(srcSegment, pick(order, JAVA_DOUBLE_NO, JAVA_DOUBLE_NNO), srcOffset, dstArray, dstIndex, elementCount,
                Unsafe.ARRAY_DOUBLE_BASE_OFFSET, Unsafe.ARRAY_DOUBLE_INDEX_SCALE, dstArray.length);
    }

    @ForceInline
    private static void copy(
            MemorySegment srcSegment, ValueLayout srcElementLayout, long srcOffset,
            Object dstArray, int dstIndex, int elementCount,
            int dstBase, int dstWidth, int dstLength) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(srcElementLayout);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffset, elementCount * dstWidth, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, dstLength);
        if (srcOffset % srcElementLayout.byteAlignment() != 0) {
            throw new IllegalArgumentException("Source offset incompatible with alignment constraints");
        }
        if (srcElementLayout.byteSize() != dstWidth) {
            throw new IllegalArgumentException("Array element size incompatible with segment element layout size");
        }
        if (srcElementLayout.order() == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, dstBase + (dstIndex * dstWidth), elementCount * dstWidth);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, dstBase + (dstIndex * dstWidth), elementCount * dstWidth, dstWidth);
        }
    }

    @ForceInline
    private static void copy(
            Object srcArray, int srcIndex,
            MemorySegment dstSegment, ValueLayout dstElementLayout, long dstOffset, int elementCount,
            int srcBase, int srcWidth, int srcLength) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(dstElementLayout);
        Objects.checkFromIndexSize(srcIndex, elementCount, srcLength);
        if (dstOffset % dstElementLayout.byteAlignment() != 0) {
            throw new IllegalArgumentException("Destination offset incompatible with alignment constraints");
        }
        if (dstElementLayout.byteSize() != srcWidth) {
            throw new IllegalArgumentException("Array element size incompatible with segment element layout size");
        }
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffset, elementCount * srcWidth, false);
        if (dstElementLayout.order() == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, srcBase + (srcIndex * srcWidth),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount * srcWidth);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, srcBase + (srcIndex * srcWidth),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount * srcWidth, srcWidth);
        }
    }
}
