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

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * This class defines useful layout constants. Some of the constants defined in this class are explicit in both
 * size and byte order (see {@link #BITS_64_BE}), and can therefore be used to explicitly and unambiguously specify the
 * contents of a memory segment. Other constants make implicit byte order assumptions (see
 * {@link #JAVA_INT}); as such, these constants make it easy to work with other serialization-centric APIs,
 * such as {@link java.nio.ByteBuffer}.
 */
public final class MemoryLayouts {

    private MemoryLayouts() {
        //just the one, please
    }

    /**
     * A value layout constant with size of one byte, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_8_LE = MemoryLayout.ofValueBits(8, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of two bytes, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_16_LE = MemoryLayout.ofValueBits(16, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of four bytes, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_32_LE = MemoryLayout.ofValueBits(32, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of eight bytes, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_64_LE = MemoryLayout.ofValueBits(64, ByteOrder.LITTLE_ENDIAN);

    private static final VarHandle byte_LE_handle = indexedHandle(BITS_8_LE, byte.class);
    private static final VarHandle char_LE_handle = indexedHandle(BITS_16_LE, char.class);
    private static final VarHandle short_LE_handle = indexedHandle(BITS_16_LE, short.class);
    private static final VarHandle int_LE_handle = indexedHandle(BITS_32_LE, int.class);
    private static final VarHandle float_LE_handle = indexedHandle(BITS_32_LE, float.class);
    private static final VarHandle long_LE_handle = indexedHandle(BITS_64_LE, long.class);
    private static final VarHandle double_LE_handle = indexedHandle(BITS_64_LE, double.class);

    /**
     * Read a byte from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_8_LE.withBitAlignment(8).varHandle(byte.class), 1L);
    byte value = (byte)handle.get(addr, offset);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a byte value read from {@code addr} at the offset specified by {@code index}.
     */
    public static byte getByte_LE(MemoryAddress addr, long offset) {
        return (byte)byte_LE_handle.get(addr, offset);
    }

    /**
     * Writes a byte at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_8_LE.withBitAlignment(8).varHandle(byte.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the byte value to be written.
     */
    public static void setByte_LE(MemoryAddress addr, long offset, byte value) {
        byte_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a char from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_LE.withBitAlignment(8).varHandle(char.class), 1L);
    char value = (char)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a char value read from {@code addr} at the offset specified by {@code index}.
     */
    public static char getChar_LE(MemoryAddress addr, long offset) {
        return (char)char_LE_handle.get(addr, offset);
    }

    /**
     * Writes a char at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_LE.withBitAlignment(8).varHandle(char.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the char value to be written.
     */
    public static void setChar_LE(MemoryAddress addr, long offset, char value) {
        char_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a short from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_LE.withBitAlignment(8).varHandle(short.class), 1L);
    short value = (short)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a short value read from {@code addr} at the offset specified by {@code index}.
     */
    public static short getShort_LE(MemoryAddress addr, long offset) {
        return (short)short_LE_handle.get(addr, offset);
    }

    /**
     * Writes a short at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_LE.withBitAlignment(8).varHandle(short.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the short value to be written.
     */
    public static void setShort_LE(MemoryAddress addr, long offset, short value) {
        short_LE_handle.set(addr, offset, value);
    }

    /**
     * Read an int from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_LE.withBitAlignment(8).varHandle(int.class), 1L);
    int value = (int)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return an int value read from {@code addr} at the offset specified by {@code index}.
     */
    public static int getInt_LE(MemoryAddress addr, long offset) {
        return (int)int_LE_handle.get(addr, offset);
    }

    /**
     * Writes an int at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_LE.withBitAlignment(8).varHandle(int.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the int value to be written.
     */
    public static void setInt_LE(MemoryAddress addr, long offset, int value) {
        int_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a float from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_LE.withBitAlignment(8).varHandle(float.class), 1L);
    float value = (float)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a float value read from {@code addr} at the offset specified by {@code index}.
     */
    public static float getFloat_LE(MemoryAddress addr, long offset) {
        return (float)float_LE_handle.get(addr, offset);
    }

    /**
     * Writes a float at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_LE.withBitAlignment(8).varHandle(float.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the float value to be written.
     */
    public static void setFloat_LE(MemoryAddress addr, long offset, float value) {
        float_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a long from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_LE.withBitAlignment(8).varHandle(long.class), 1L);
    long value = (long)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a long value read from {@code addr} at the offset specified by {@code index}.
     */
    public static long getLong_LE(MemoryAddress addr, long offset) {
        return (long)long_LE_handle.get(addr, offset);
    }

    /**
     * Writes a long at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_LE.withBitAlignment(8).varHandle(long.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the long value to be written.
     */
    public static void setLong_LE(MemoryAddress addr, long offset, long value) {
        long_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a double from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_LE.withBitAlignment(8).varHandle(double.class), 1L);
    double value = (double)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a double value read from {@code addr} at the offset specified by {@code index}.
     */
    public static double getDouble_LE(MemoryAddress addr, long offset) {
        return (double)double_LE_handle.get(addr, offset);
    }

    /**
     * Writes a double at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_LE.withBitAlignment(8).varHandle(double.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the double value to be written.
     */
    public static void setDouble_LE(MemoryAddress addr, long offset, double value) {
        double_LE_handle.set(addr, offset, value);
    }

    /**
     * A value layout constant with size of one byte, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_8_BE = MemoryLayout.ofValueBits(8, ByteOrder.BIG_ENDIAN);

    /**
     * A value layout constant with size of two bytes, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_16_BE = MemoryLayout.ofValueBits(16, ByteOrder.BIG_ENDIAN);

    /**
     * A value layout constant with size of four bytes, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_32_BE = MemoryLayout.ofValueBits(32, ByteOrder.BIG_ENDIAN);

    /**
     * A value layout constant with size of eight bytes, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_64_BE = MemoryLayout.ofValueBits(64, ByteOrder.BIG_ENDIAN);

    private static final VarHandle byte_BE_handle = indexedHandle(BITS_8_BE, byte.class);
    private static final VarHandle char_BE_handle = indexedHandle(BITS_16_BE, char.class);
    private static final VarHandle short_BE_handle = indexedHandle(BITS_16_BE, short.class);
    private static final VarHandle int_BE_handle = indexedHandle(BITS_32_BE, int.class);
    private static final VarHandle float_BE_handle = indexedHandle(BITS_32_BE, float.class);
    private static final VarHandle long_BE_handle = indexedHandle(BITS_64_BE, long.class);
    private static final VarHandle double_BE_handle = indexedHandle(BITS_64_BE, double.class);

    /**
     * Read a byte from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_8_BE.withBitAlignment(8).varHandle(byte.class), 1L);
    byte value = (byte)handle.get(addr, offset);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a byte value read from {@code addr} at the offset specified by {@code index}.
     */
    public static byte getByte_BE(MemoryAddress addr, long offset) {
        return (byte)byte_BE_handle.get(addr, offset);
    }

    /**
     * Writes a byte at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_8_BE.withBitAlignment(8).varHandle(byte.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the byte value to be written.
     */
    public static void setByte_BE(MemoryAddress addr, long offset, byte value) {
        byte_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a char from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_BE.withBitAlignment(8).varHandle(char.class), 1L);
    char value = (char)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a char value read from {@code addr} at the offset specified by {@code index}.
     */
    public static char getChar_BE(MemoryAddress addr, long offset) {
        return (char)char_BE_handle.get(addr, offset);
    }

    /**
     * Writes a char at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_BE.withBitAlignment(8).varHandle(char.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the char value to be written.
     */
    public static void setChar_BE(MemoryAddress addr, long offset, char value) {
        char_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a short from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_BE.withBitAlignment(8).varHandle(short.class), 1L);
    short value = (short)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a short value read from {@code addr} at the offset specified by {@code index}.
     */
    public static short getShort_BE(MemoryAddress addr, long offset) {
        return (short)short_BE_handle.get(addr, offset);
    }

    /**
     * Writes a short at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_BE.withBitAlignment(8).varHandle(short.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the short value to be written.
     */
    public static void setShort_BE(MemoryAddress addr, long offset, short value) {
        short_BE_handle.set(addr, offset, value);
    }

    /**
     * Read an int from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_BE.withBitAlignment(8).varHandle(int.class), 1L);
    int value = (int)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return an int value read from {@code addr} at the offset specified by {@code index}.
     */
    public static int getInt_BE(MemoryAddress addr, long offset) {
        return (int)int_BE_handle.get(addr, offset);
    }

    /**
     * Writes an int at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_BE.withBitAlignment(8).varHandle(int.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the int value to be written.
     */
    public static void setInt_BE(MemoryAddress addr, long offset, int value) {
        int_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a float from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_BE.withBitAlignment(8).varHandle(float.class), 1L);
    float value = (float)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a float value read from {@code addr} at the offset specified by {@code index}.
     */
    public static float getFloat_BE(MemoryAddress addr, long offset) {
        return (float)float_BE_handle.get(addr, offset);
    }

    /**
     * Writes a float at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_BE.withBitAlignment(8).varHandle(float.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the float value to be written.
     */
    public static void setFloat_BE(MemoryAddress addr, long offset, float value) {
        float_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a long from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_BE.withBitAlignment(8).varHandle(long.class), 1L);
    long value = (long)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a long value read from {@code addr} at the offset specified by {@code index}.
     */
    public static long getLong_BE(MemoryAddress addr, long offset) {
        return (long)long_BE_handle.get(addr, offset);
    }

    /**
     * Writes a long at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_BE.withBitAlignment(8).varHandle(long.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the long value to be written.
     */
    public static void setLong_BE(MemoryAddress addr, long offset, long value) {
        long_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a double from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_BE.withBitAlignment(8).varHandle(double.class), 1L);
    double value = (double)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a double value read from {@code addr} at the offset specified by {@code index}.
     */
    public static double getDouble_BE(MemoryAddress addr, long offset) {
        return (double)double_BE_handle.get(addr, offset);
    }

    /**
     * Writes a double at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_BE.withBitAlignment(8).varHandle(double.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the double value to be written.
     */
    public static void setDouble_BE(MemoryAddress addr, long offset, double value) {
        double_BE_handle.set(addr, offset, value);
    }

    /**
     * A padding layout constant with size of one byte.
     */
    public static final MemoryLayout PAD_8 = MemoryLayout.ofPaddingBits(8);

    /**
     * A padding layout constant with size of two bytes.
     */
    public static final MemoryLayout PAD_16 = MemoryLayout.ofPaddingBits(16);

    /**
     * A padding layout constant with size of four bytes.
     */
    public static final MemoryLayout PAD_32 = MemoryLayout.ofPaddingBits(32);

    /**
     * A padding layout constant with size of eight bytes.
     */
    public static final MemoryLayout PAD_64 = MemoryLayout.ofPaddingBits(64);

    /**
     * A value layout constant whose size is the same as that of a Java {@code byte}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_BYTE = MemoryLayout.ofValueBits(8, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code char}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_CHAR = MemoryLayout.ofValueBits(16, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code short}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_SHORT = MemoryLayout.ofValueBits(16, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code int}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_INT = MemoryLayout.ofValueBits(32, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code long}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_LONG = MemoryLayout.ofValueBits(64, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code float}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_FLOAT = MemoryLayout.ofValueBits(32, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code double}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_DOUBLE = MemoryLayout.ofValueBits(64, ByteOrder.nativeOrder());

    /**
     * Read a byte from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_BYTE.withBitAlignment(8).varHandle(byte.class), 1L);
    byte value = (byte)handle.get(addr, offset);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a byte value read from {@code addr} at the offset specified by {@code index}.
     */
    public static byte getByte(MemoryAddress addr, long offset) {
        return (byte)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? byte_BE_handle : byte_LE_handle).get(addr, offset);
    }

    /**
     * Writes a byte at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_BYTE.withBitAlignment(8).varHandle(byte.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the byte value to be written.
     */
    public static void setByte(MemoryAddress addr, long offset, byte value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? byte_BE_handle : byte_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a char from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_CHAR.withBitAlignment(8).varHandle(char.class), 1L);
    char value = (char)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a char value read from {@code addr} at the offset specified by {@code index}.
     */
    public static char getChar(MemoryAddress addr, long offset) {
        return (char)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? char_BE_handle : char_LE_handle).get(addr, offset);
    }

    /**
     * Writes a char at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_CHAR.withBitAlignment(8).varHandle(char.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the char value to be written.
     */
    public static void setChar(MemoryAddress addr, long offset, char value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? char_BE_handle : char_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a short from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_SHORT.withBitAlignment(8).varHandle(short.class), 1L);
    short value = (short)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a short value read from {@code addr} at the offset specified by {@code index}.
     */
    public static short getShort(MemoryAddress addr, long offset) {
        return (short)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? short_BE_handle : short_LE_handle).get(addr, offset);
    }

    /**
     * Writes a short at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_SHORT.withBitAlignment(8).varHandle(short.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the short value to be written.
     */
    public static void setShort(MemoryAddress addr, long offset, short value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? short_BE_handle : short_LE_handle).set(addr, offset, value);
    }

    /**
     * Read an int from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_INT.withBitAlignment(8).varHandle(int.class), 1L);
    int value = (int)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return an int value read from {@code addr} at the offset specified by {@code index}.
     */
    public static int getInt(MemoryAddress addr, long offset) {
        return (int)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? int_BE_handle : int_LE_handle).get(addr, offset);
    }

    /**
     * Writes an int at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_INT.withBitAlignment(8).varHandle(int.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the int value to be written.
     */
    public static void setInt(MemoryAddress addr, long offset, int value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? int_BE_handle : int_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a float from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_FLOAT.withBitAlignment(8).varHandle(float.class), 1L);
    float value = (float)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a float value read from {@code addr} at the offset specified by {@code index}.
     */
    public static float getFloat(MemoryAddress addr, long offset) {
        return (float)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? float_BE_handle : float_LE_handle).get(addr, offset);
    }

    /**
     * Writes a float at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_FLOAT.withBitAlignment(8).varHandle(float.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the float value to be written.
     */
    public static void setFloat(MemoryAddress addr, long offset, float value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? float_BE_handle : float_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a long from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_LONG.withBitAlignment(8).varHandle(long.class), 1L);
    long value = (long)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a long value read from {@code addr} at the offset specified by {@code index}.
     */
    public static long getLong(MemoryAddress addr, long offset) {
        return (long)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? long_BE_handle : long_LE_handle).get(addr, offset);
    }

    /**
     * Writes a long at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_LONG.withBitAlignment(8).varHandle(long.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the long value to be written.
     */
    public static void setLong(MemoryAddress addr, long offset, long value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? long_BE_handle : long_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a double from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_DOUBLE.withBitAlignment(8).varHandle(double.class), 1L);
    double value = (double)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a double value read from {@code addr} at the offset specified by {@code index}.
     */
    public static double getDouble(MemoryAddress addr, long offset) {
        return (double)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? double_BE_handle : double_LE_handle).get(addr, offset);
    }

    /**
     * Writes a double at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_DOUBLE.withBitAlignment(8).varHandle(double.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the double value to be written.
     */
    public static void setDouble(MemoryAddress addr, long offset, double value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? double_BE_handle : double_LE_handle).set(addr, offset, value);
    }

    private static VarHandle indexedHandle(MemoryLayout elementLayout, Class<?> carrier) {
        return MemoryHandles.withStride(elementLayout.withBitAlignment(8).varHandle(carrier), 1L);
    }
}
