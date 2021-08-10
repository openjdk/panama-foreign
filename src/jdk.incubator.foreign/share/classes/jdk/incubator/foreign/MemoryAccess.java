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
import jdk.internal.vm.annotation.DontInline;
import jdk.internal.vm.annotation.ForceInline;
import jdk.internal.vm.annotation.Stable;
import sun.invoke.util.Wrapper;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * This class defines convenient static methods which can be used to read/write Java values from/to memory segments.
 * <p>
 * Single values can be read and written using accessors like {@link #getInt(MemorySegment, long)}, and
 * {@link #setInt(MemorySegment, long, int)}. These accessors all accept the following parameters:
 * <ul>
 *     <li>the memory segment to be read (resp. written)</li>
 *     <li>the offset, in bytes, at which the read (resp. write) operation should occur</li>
 *     <li>the offset, in bytes, at which the read (resp. write) operation should occur</li>
 *     <li>an optional {@linkplain ByteOrder byte order}</li>
 * </ul>
 * <p>
 * Multiple values can be read and written in bulk from and to Java arrays, using accessors like {@link #copy(MemorySegment, long, Object, int, int)}.
 * These accessors all accept the following parameters (source parameters are laid out before destination parameters, similarly to
 * {@link System#arraycopy(Object, int, Object, int, int)}):
 <ul>
 *     <li>the memory segment to be read (resp. written)</li>
 *     <li>the offset, in bytes, at which elements should be read (resp. written) from (resp. to) the segment</li>
 *     <li>the Java array to be written (resp. read)</li>
 *     <li>the array index at which elements should be written (resp. read) to (resp. from) the array</li>
 *     <li>an optional {@linkplain ByteOrder byte order}</li>
 * </ul>
 * If the source (destination) segment is actually a view of the destination (source) array,
 * and if the copy region of the source overlaps with the copy region of the destination,
 * the copy of the overlapping region is performed as if the data in the overlapping region
 * were first copied into a temporary segment before being copied to the destination.
 * <p>
 * Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more {@code null}
 * elements to a method in this class causes a {@link NullPointerException} to be thrown. Moreover,
 * attempting to dereference a segment whose {@linkplain MemorySegment#scope() scope} has already been closed,
 * or from a thread other than the thread owning the scope causes an {@link IllegalStateException} to be thrown.
 * Finally, attempting to dereference a segment (of {@linkplain MemorySegment#address() base address} {@code B} and
 * {@linkplain MemorySegment#byteSize() size} {@code S}) at addresses that are {@code < B}, or {@code >= B + S},
 * causes an {@link IndexOutOfBoundsException} to be thrown; similarly, attempting to copy to/from an array
 * (of length {@code L}) at indices that are {@code < 0}, or {@code >= L} causes an {@link IndexOutOfBoundsException} to be thrown.</p>
 */
public final class MemoryAccess {

    private MemoryAccess() {
        // just the one!
    }

    static final Unsafe UNSAFE = Unsafe.getUnsafe();

    static final ScopedMemoryAccess scopedMemoryAccess = ScopedMemoryAccess.getScopedMemoryAccess();

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

    /**
     * Copies a number of elements from a source segment to a destination array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination array.
     * @param dstIndex the starting index of the destination array.
     * @param elementCount the number of array elements to be copied.
     * @throws  IllegalArgumentException if {@code dstArray} is not an array, or if it is an array but whose type is not supported.
     * Supported array types are {@code byte[]}, {@code char[]},{@code short[]},{@code int[]},{@code float[]},{@code long[]} and {@code double[]}.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            Object dstArray, int dstIndex, int elementCount) {
        copy(srcSegment, srcOffset, dstArray, dstIndex, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of elements from a source segment to a destination array,
     * starting at a given segment offset (expressed in bytes), and a given array index, using the given byte order.
     * @param srcSegment the source segment.
     * @param srcOffset the starting offset, in bytes, of the source segment.
     * @param dstArray the destination array.
     * @param dstIndex the starting index of the destination array.
     * @param elementCount the number of array elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     * @throws  IllegalArgumentException if {@code dstArray} is not an array, or if it is an array but whose type is not supported.
     * Supported array types are {@code byte[]}, {@code char[]},{@code short[]},{@code int[]},{@code float[]},{@code long[]} and {@code double[]}.
     */
    @ForceInline
    public static void copy(
            MemorySegment srcSegment, long srcOffset,
            Object dstArray, int dstIndex, int elementCount, ByteOrder order) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(order);
        long baseAndScale = getBaseAndScale(dstArray.getClass());
        int dstBase = (int)baseAndScale;
        int dstWidth = (int)(baseAndScale >> 32);
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)srcSegment;
        srcImpl.checkAccess(srcOffset, elementCount * dstWidth, true);
        Objects.checkFromIndexSize(dstIndex, elementCount, Array.getLength(dstArray));
        if (dstWidth == 1 || order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, dstBase + (dstIndex * dstWidth), elementCount * dstWidth);
        } else {
            scopedMemoryAccess.copySwapMemory(srcImpl.scope(), null,
                    srcImpl.unsafeGetBase(), srcImpl.unsafeGetOffset() + srcOffset,
                    dstArray, dstBase + (dstIndex * dstWidth), elementCount * dstWidth, dstWidth);
        }
    }

    /**
     * Copies a number of elements from a source array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source array.
     * @param srcIndex the starting index of the source array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of array elements to be copied.
     * @throws  IllegalArgumentException if {@code srcArray} is not an array, or if it is an array but whose type is not supported.
     * Supported array types are {@code byte[]}, {@code char[]},{@code short[]},{@code int[]},{@code float[]},{@code long[]} and {@code double[]}.
     */
    @ForceInline
    public static void copy(
            Object srcArray, int srcIndex,
            MemorySegment dstSegment, long dstOffset, int elementCount) {
        copy(srcArray, srcIndex, dstSegment, dstOffset, elementCount, ByteOrder.nativeOrder());
    }

    /**
     * Copies a number of elements from a source array to a destination segment,
     * starting at a given array index, and a given segment offset (expressed in bytes), using the given byte order.
     * @param srcArray the source array.
     * @param srcIndex the starting index of the source array.
     * @param dstSegment the destination segment.
     * @param dstOffset the starting offset, in bytes, of the destination segment.
     * @param elementCount the number of array elements to be copied.
     * @param order the byte order to be used for the copy operation. If the specified byte order is
     * different from the native order, a byte swap operation will be performed on each array element.
     * @throws  IllegalArgumentException if {@code srcArray} is not an array, or if it is an array but whose type is not supported.
     * Supported array types are {@code byte[]}, {@code char[]},{@code short[]},{@code int[]},{@code float[]},{@code long[]} and {@code double[]}.
     */
    @ForceInline
    public static void copy(
            Object srcArray, int srcIndex,
            MemorySegment dstSegment, long dstOffset, int elementCount, ByteOrder order) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(order);
        long baseAndScale = getBaseAndScale(srcArray.getClass());
        int srcBase = (int)baseAndScale;
        int srcWidth = (int)(baseAndScale >> 32);
        Objects.checkFromIndexSize(srcIndex, elementCount, Array.getLength(srcArray));
        AbstractMemorySegmentImpl destImpl = (AbstractMemorySegmentImpl)dstSegment;
        destImpl.checkAccess(dstOffset, elementCount * srcWidth, false);
        if (srcWidth == 1 || order == ByteOrder.nativeOrder()) {
            scopedMemoryAccess.copyMemory(null, destImpl.scope(),
                    srcArray, srcBase + (srcIndex * srcWidth),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount * srcWidth);
        } else {
            scopedMemoryAccess.copySwapMemory(null, destImpl.scope(),
                    srcArray, srcBase + (srcIndex * srcWidth),
                    destImpl.unsafeGetBase(), destImpl.unsafeGetOffset() + dstOffset, elementCount * srcWidth, srcWidth);
        }
    }

    static long getBaseAndScale(Class<?> arrayType) {
        if (arrayType.equals(byte[].class)) {
            return (long)Unsafe.ARRAY_BYTE_BASE_OFFSET | ((long)Unsafe.ARRAY_BYTE_INDEX_SCALE << 32);
        } else if (arrayType.equals(char[].class)) {
            return (long)Unsafe.ARRAY_CHAR_BASE_OFFSET | ((long)Unsafe.ARRAY_CHAR_INDEX_SCALE << 32);
        } else if (arrayType.equals(short[].class)) {
            return (long)Unsafe.ARRAY_SHORT_BASE_OFFSET | ((long)Unsafe.ARRAY_SHORT_INDEX_SCALE << 32);
        } else if (arrayType.equals(int[].class)) {
            return (long)Unsafe.ARRAY_INT_BASE_OFFSET | ((long)Unsafe.ARRAY_INT_INDEX_SCALE << 32);
        } else if (arrayType.equals(float[].class)) {
            return (long)Unsafe.ARRAY_FLOAT_BASE_OFFSET | ((long)Unsafe.ARRAY_FLOAT_INDEX_SCALE << 32);
        } else if (arrayType.equals(long[].class)) {
            return (long)Unsafe.ARRAY_LONG_BASE_OFFSET | ((long)Unsafe.ARRAY_LONG_INDEX_SCALE << 32);
        } else if (arrayType.equals(double[].class)) {
            return (long)Unsafe.ARRAY_DOUBLE_BASE_OFFSET | ((long)Unsafe.ARRAY_DOUBLE_INDEX_SCALE << 32);
        } else {
            throw new IllegalStateException();
        }
    }
}
