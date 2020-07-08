package jdk.incubator.foreign;

import jdk.internal.access.foreign.MemoryAddressProxy;
import jdk.internal.vm.annotation.ForceInline;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * This class defines ready-made static accessors which can be used to dereference memory segments in many ways.
 * <p>
 * The most primitive accessors (see {@link #getIntAtOffset(MemoryAddress, long)} (MemoryAddress, long)}) take a <em>base</em> address and an offset (expressed in bytes).
 * The final address at which the dereference will occur will be computed by offsetting the base address by
 * the specified offset, as if by calling {@link MemoryAddress#addOffset(long)} on the specified base address.
 * <p>
 * In cases where no offset is required, overloads are provided (see {@link #getInt(MemoryAddress)}) so that
 * clients can omit the offset coordinate.
 * <p>
 * To help dereferencing in array-like use cases (e.g. where the layout of a given memory segment is a sequence
 * layout of given size an element count), higher-level overloads are also provided (see {@link #getIntAtIndex(MemoryAddress, long)}),
 * which take an <em>base</em> address and a <em>logical</em> element index. The formula to obtain the byte offset {@code O} from an
 * index {@code I} is given by {@code O = I * S} where {@code S} is the size (expressed in bytes) of the element to
 * be dereferenced.
 */
public final class MemoryAccess {

    private MemoryAccess() {
        // just the one
    }

    private static final VarHandle byte_LE_handle = indexedHandle(MemoryLayouts.BITS_8_LE, byte.class);
    private static final VarHandle char_LE_handle = indexedHandle(MemoryLayouts.BITS_16_LE, char.class);
    private static final VarHandle short_LE_handle = indexedHandle(MemoryLayouts.BITS_16_LE, short.class);
    private static final VarHandle int_LE_handle = indexedHandle(MemoryLayouts.BITS_32_LE, int.class);
    private static final VarHandle float_LE_handle = indexedHandle(MemoryLayouts.BITS_32_LE, float.class);
    private static final VarHandle long_LE_handle = indexedHandle(MemoryLayouts.BITS_64_LE, long.class);
    private static final VarHandle double_LE_handle = indexedHandle(MemoryLayouts.BITS_64_LE, double.class);
    private static final VarHandle byte_BE_handle = indexedHandle(MemoryLayouts.BITS_8_BE, byte.class);
    private static final VarHandle char_BE_handle = indexedHandle(MemoryLayouts.BITS_16_BE, char.class);
    private static final VarHandle short_BE_handle = indexedHandle(MemoryLayouts.BITS_16_BE, short.class);
    private static final VarHandle int_BE_handle = indexedHandle(MemoryLayouts.BITS_32_BE, int.class);
    private static final VarHandle float_BE_handle = indexedHandle(MemoryLayouts.BITS_32_BE, float.class);
    private static final VarHandle long_BE_handle = indexedHandle(MemoryLayouts.BITS_64_BE, long.class);
    private static final VarHandle double_BE_handle = indexedHandle(MemoryLayouts.BITS_64_BE, double.class);
    private static final VarHandle address_handle = MemoryHandles.asAddressVarHandle((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? long_BE_handle : long_LE_handle);

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
     * @return a byte value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static byte getByteAtOffset_LE(MemoryAddress addr, long offset) {
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
    public static void setByteAtOffset_LE(MemoryAddress addr, long offset, byte value) {
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
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static char getCharAtOffset_LE(MemoryAddress addr, long offset) {
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
    public static void setCharAtOffset_LE(MemoryAddress addr, long offset, char value) {
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
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static short getShortAtOffset_LE(MemoryAddress addr, long offset) {
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
    public static void setShortAtOffset_LE(MemoryAddress addr, long offset, short value) {
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
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static int getIntAtOffset_LE(MemoryAddress addr, long offset) {
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
    public static void setIntAtOffset_LE(MemoryAddress addr, long offset, int value) {
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
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static float getFloatAtOffset_LE(MemoryAddress addr, long offset) {
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
    public static void setFloatAtOffset_LE(MemoryAddress addr, long offset, float value) {
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
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static long getLongAtOffset_LE(MemoryAddress addr, long offset) {
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
    public static void setLongAtOffset_LE(MemoryAddress addr, long offset, long value) {
        long_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a double from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_LE.withBitAlignment(8).varHandle(double.class), 1L);
    double value = (double)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static double getDoubleAtOffset_LE(MemoryAddress addr, long offset) {
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
    public static void setDoubleAtOffset_LE(MemoryAddress addr, long offset, double value) {
        double_LE_handle.set(addr, offset, value);
    }

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
     * @return a byte value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static byte getByteAtOffset_BE(MemoryAddress addr, long offset) {
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
    public static void setByteAtOffset_BE(MemoryAddress addr, long offset, byte value) {
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
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static char getCharAtOffset_BE(MemoryAddress addr, long offset) {
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
    public static void setCharAtOffset_BE(MemoryAddress addr, long offset, char value) {
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
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static short getShortAtOffset_BE(MemoryAddress addr, long offset) {
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
    public static void setShortAtOffset_BE(MemoryAddress addr, long offset, short value) {
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
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static int getIntAtOffset_BE(MemoryAddress addr, long offset) {
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
    public static void setIntAtOffset_BE(MemoryAddress addr, long offset, int value) {
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
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static float getFloatAtOffset_BE(MemoryAddress addr, long offset) {
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
    public static void setFloatAtOffset_BE(MemoryAddress addr, long offset, float value) {
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
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static long getLongAtOffset_BE(MemoryAddress addr, long offset) {
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
    public static void setLongAtOffset_BE(MemoryAddress addr, long offset, long value) {
        long_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a double from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_BE.withBitAlignment(8).varHandle(double.class), 1L);
    double value = (double)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static double getDoubleAtOffset_BE(MemoryAddress addr, long offset) {
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
    public static void setDoubleAtOffset_BE(MemoryAddress addr, long offset, double value) {
        double_BE_handle.set(addr, offset, value);
    }

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
     * @return a byte value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static byte getByteAtOffset(MemoryAddress addr, long offset) {
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
    public static void setByteAtOffset(MemoryAddress addr, long offset, byte value) {
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
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static char getCharAtOffset(MemoryAddress addr, long offset) {
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
    public static void setCharAtOffset(MemoryAddress addr, long offset, char value) {
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
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static short getShortAtOffset(MemoryAddress addr, long offset) {
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
    public static void setShortAtOffset(MemoryAddress addr, long offset, short value) {
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
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static int getIntAtOffset(MemoryAddress addr, long offset) {
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
    public static void setIntAtOffset(MemoryAddress addr, long offset, int value) {
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
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static float getFloatAtOffset(MemoryAddress addr, long offset) {
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
    public static void setFloatAtOffset(MemoryAddress addr, long offset, float value) {
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
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static long getLongAtOffset(MemoryAddress addr, long offset) {
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
    public static void setLongAtOffset(MemoryAddress addr, long offset, long value) {
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
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static double getDoubleAtOffset(MemoryAddress addr, long offset) {
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
    public static void setDoubleAtOffset(MemoryAddress addr, long offset, double value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? double_BE_handle : double_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a memory address from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.asAddressHandle(MemoryHandles.withStride(JAVA_LONG.withBitAlignment(8).varHandle(long.class), 1L));
    MemoryAddress value = (MemoryAddress)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a memory address read from {@code addr} at the offset specified by {@code offset}.
     */
    public static MemoryAddress getAddressAtOffset(MemoryAddress addr, long offset) {
        return (MemoryAddress)address_handle.get(addr, offset);
    }

    /**
     * Writes a memory address at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.asAddressHandle(MemoryHandles.withStride(JAVA_LONG.withBitAlignment(8).varHandle(long.class), 1L));
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the memory address to be written.
     */
    public static void setAddressAtOffset(MemoryAddress addr, long offset, MemoryAddress value) {
        address_handle.set(addr, offset, value);
    }

    private static VarHandle indexedHandle(MemoryLayout elementLayout, Class<?> carrier) {
        return MemoryHandles.withStride(elementLayout.withBitAlignment(8).varHandle(carrier), 1L);
    }

    /**
     * Read a byte from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByteAtOffset_LE(addr, 0L);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @return a byte value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static byte getByte_LE(MemoryAddress addr) {
        return getByteAtOffset_LE(addr, 0L);
    }

    /**
     * Writes a byte at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByteAtOffset_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the byte value to be written.
     */
    public static void setByte_LE(MemoryAddress addr, byte value) {
        setByteAtOffset_LE(addr, 0L, value);
    }

    /**
     * Read a char from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getCharAtOffset_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static char getChar_LE(MemoryAddress addr) {
        return getCharAtOffset_LE(addr, 0L);
    }

    /**
     * Writes a char at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setCharAtOffset_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the char value to be written.
     */
    public static void setChar_LE(MemoryAddress addr, char value) {
        setCharAtOffset_LE(addr, 0L, value);
    }

    /**
     * Read a short from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShortAtOffset_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static short getShort_LE(MemoryAddress addr) {
        return getShortAtOffset_LE(addr, 0L);
    }

    /**
     * Writes a short at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShortAtOffset_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the short value to be written.
     */
    public static void setShort_LE(MemoryAddress addr, short value) {
        setShortAtOffset_LE(addr, 0L, value);
    }

    /**
     * Read an int from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getIntAtOffset_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static int getInt_LE(MemoryAddress addr) {
        return getIntAtOffset_LE(addr, 0L);
    }

    /**
     * Writes an int at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setIntAtOffset_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the int value to be written.
     */
    public static void setInt_LE(MemoryAddress addr, int value) {
        setIntAtOffset_LE(addr, 0L, value);
    }

    /**
     * Read a float from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloatAtOffset_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static float getFloat_LE(MemoryAddress addr) {
        return getFloatAtOffset_LE(addr, 0L);
    }

    /**
     * Writes a float at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloatAtOffset_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the float value to be written.
     */
    public static void setFloat_LE(MemoryAddress addr, float value) {
        setFloatAtOffset_LE(addr, 0L, value);
    }

    /**
     * Read a long from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    long value = getLongAtOffset_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static long getLong_LE(MemoryAddress addr) {
        return getLongAtOffset_LE(addr, 0L);
    }

    /**
     * Writes a long at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLongAtOffset_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the long value to be written.
     */
    public static void setLong_LE(MemoryAddress addr, long value) {
        setLongAtOffset_LE(addr, 0L, value);
    }

    /**
     * Read a double from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    double value = getDoubleAtOffset_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static double getDouble_LE(MemoryAddress addr) {
        return getDoubleAtOffset_LE(addr, 0L);
    }

    /**
     * Writes a double at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDoubleAtOffset_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the double value to be written.
     */
    public static void setDouble_LE(MemoryAddress addr, double value) {
        setDoubleAtOffset_LE(addr, 0L, value);
    }

    /**
     * Read a byte from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByteAtOffset_BE(addr, 0L);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @return a byte value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static byte getByte_BE(MemoryAddress addr) {
        return getByteAtOffset_BE(addr, 0L);
    }

    /**
     * Writes a byte at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByteAtOffset_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the byte value to be written.
     */
    public static void setByte_BE(MemoryAddress addr, byte value) {
        setByteAtOffset_BE(addr, 0L, value);
    }

    /**
     * Read a char from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getCharAtOffset_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static char getChar_BE(MemoryAddress addr) {
        return getCharAtOffset_BE(addr, 0L);
    }

    /**
     * Writes a char at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setCharAtOffset_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the char value to be written.
     */
    public static void setChar_BE(MemoryAddress addr, char value) {
        setCharAtOffset_BE(addr, 0L, value);
    }

    /**
     * Read a short from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShortAtOffset_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static short getShort_BE(MemoryAddress addr) {
        return getShortAtOffset_BE(addr, 0L);
    }

    /**
     * Writes a short at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShortAtOffset_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the short value to be written.
     */
    public static void setShort_BE(MemoryAddress addr, short value) {
        setShortAtOffset_BE(addr, 0L, value);
    }

    /**
     * Read an int from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getIntAtOffset_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static int getInt_BE(MemoryAddress addr) {
        return getIntAtOffset_BE(addr, 0L);
    }

    /**
     * Writes an int at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setIntAtOffset_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the int value to be written.
     */
    public static void setInt_BE(MemoryAddress addr, int value) {
        setIntAtOffset_BE(addr, 0L, value);
    }

    /**
     * Read a float from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloatAtOffset_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static float getFloat_BE(MemoryAddress addr) {
        return getFloatAtOffset_BE(addr, 0L);
    }

    /**
     * Writes a float at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloatAtOffset_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the float value to be written.
     */
    public static void setFloat_BE(MemoryAddress addr, float value) {
        setFloatAtOffset_BE(addr, 0L, value);
    }

    /**
     * Read a long from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    long value = getLongAtOffset_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static long getLong_BE(MemoryAddress addr) {
        return getLongAtOffset_BE(addr, 0L);
    }

    /**
     * Writes a long at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLongAtOffset_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the long value to be written.
     */
    public static void setLong_BE(MemoryAddress addr, long value) {
        setLongAtOffset_BE(addr, 0L, value);
    }

    /**
     * Read a double from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    double value = getDoubleAtOffset_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static double getDouble_BE(MemoryAddress addr) {
        return getDoubleAtOffset_BE(addr, 0L);
    }

    /**
     * Writes a double at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDoubleAtOffset_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the double value to be written.
     */
    public static void setDouble_BE(MemoryAddress addr, double value) {
        setDoubleAtOffset_BE(addr, 0L, value);
    }

    /**
     * Read a byte from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByteAtOffset(addr, 0L);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @return a byte value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static byte getByte(MemoryAddress addr) {
        return getByteAtOffset(addr, 0L);
    }

    /**
     * Writes a byte at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByteAtOffset(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the byte value to be written.
     */
    public static void setByte(MemoryAddress addr, byte value) {
        setByteAtOffset(addr, 0L, value);
    }

    /**
     * Read a char from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getCharAtOffset(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static char getChar(MemoryAddress addr) {
        return getCharAtOffset(addr, 0L);
    }

    /**
     * Writes a char at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setCharAtOffset(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the char value to be written.
     */
    public static void setChar(MemoryAddress addr, char value) {
        setCharAtOffset(addr, 0L, value);
    }

    /**
     * Read a short from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShortAtOffset(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static short getShort(MemoryAddress addr) {
        return getShortAtOffset(addr, 0L);
    }

    /**
     * Writes a short at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShortAtOffset(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the short value to be written.
     */
    public static void setShort(MemoryAddress addr, short value) {
        setShortAtOffset(addr, 0L, value);
    }

    /**
     * Read an int from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getIntAtOffset(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static int getInt(MemoryAddress addr) {
        return getIntAtOffset(addr, 0L);
    }

    /**
     * Writes an int at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setIntAtOffset(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the int value to be written.
     */
    public static void setInt(MemoryAddress addr, int value) {
        setIntAtOffset(addr, 0L, value);
    }

    /**
     * Read a float from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloatAtOffset(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static float getFloat(MemoryAddress addr) {
        return getFloatAtOffset(addr, 0L);
    }

    /**
     * Writes a float at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloatAtOffset(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the float value to be written.
     */
    public static void setFloat(MemoryAddress addr, float value) {
        setFloatAtOffset(addr, 0L, value);
    }

    /**
     * Read a long from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    long value = getLongAtOffset(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static long getLong(MemoryAddress addr) {
        return getLongAtOffset(addr, 0L);
    }

    /**
     * Writes a long at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLongAtOffset(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the long value to be written.
     */
    public static void setLong(MemoryAddress addr, long value) {
        setLongAtOffset(addr, 0L, value);
    }

    /**
     * Read a double from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    double value = getDoubleAtOffset(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static double getDouble(MemoryAddress addr) {
        return getDoubleAtOffset(addr, 0L);
    }

    /**
     * Writes a double at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDoubleAtOffset(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the double value to be written.
     */
    public static void setDouble(MemoryAddress addr, double value) {
        setDoubleAtOffset(addr, 0L, value);
    }

    /**
     * Read a memory address from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    MemoryAddress value = getAddressAtOffset(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a memory address read from {@code addr} at the offset specified by {@code offset}.
     */
    public static MemoryAddress getAddress(MemoryAddress addr) {
        return getAddressAtOffset(addr, 0L);
    }

    /**
     * Writes a memory address at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setAddressAtOffset(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the memory address to be written.
     */
    public static void setAddress(MemoryAddress addr, MemoryAddress value) {
        setAddressAtOffset(addr, 0L, value);
    }

    /**
     * Read a byte from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByteAtOffset_LE(addr, index);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @return a byte value read from {@code addr} at the element index specified by {@code index}.
     */
    public static byte getByteAtIndex_LE(MemoryAddress addr, long index) {
        return getByteAtOffset_LE(addr, index);
    }

    /**
     * Writes a byte at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByteAtOffset_LE(addr, index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @param value the byte value to be written.
     */
    public static void setByteAtIndex_LE(MemoryAddress addr, long index, byte value) {
        setByteAtOffset_LE(addr, index, value);
    }

    /**
     * Read a char from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getCharAtOffset_LE(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a char value read from {@code addr} at the element index specified by {@code index}.
     */
    public static char getCharAtIndex_LE(MemoryAddress addr, long index) {
        return getCharAtOffset_LE(addr, scale(addr, index, 2));
    }

    /**
     * Writes a char at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setCharAtOffset_LE(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the char value to be written.
     */
    public static void setCharAtIndex_LE(MemoryAddress addr, long index, char value) {
        setCharAtOffset_LE(addr, scale(addr, index, 2), value);
    }

    /**
     * Read a short from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShortAtOffset_LE(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a short value read from {@code addr} at the element index specified by {@code index}.
     */
    public static short getShortAtIndex_LE(MemoryAddress addr, long index) {
        return getShortAtOffset_LE(addr, scale(addr, index, 2));
    }

    /**
     * Writes a short at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShortAtOffset_LE(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the short value to be written.
     */
    public static void setShortAtIndex_LE(MemoryAddress addr, long index, short value) {
        setShortAtOffset_LE(addr, scale(addr, index, 2), value);
    }

    /**
     * Read an int from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getIntAtOffset_LE(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return an int value read from {@code addr} at the element index specified by {@code index}.
     */
    public static int getIntAtIndex_LE(MemoryAddress addr, long index) {
        return getIntAtOffset_LE(addr, scale(addr, index, 4));
    }

    /**
     * Writes an int at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setIntAtOffset_LE(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the int value to be written.
     */
    public static void setIntAtIndex_LE(MemoryAddress addr, long index, int value) {
        setIntAtOffset_LE(addr, scale(addr, index, 4), value);
    }

    /**
     * Read a float from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloatAtOffset_LE(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return a float value read from {@code addr} at the element index specified by {@code index}.
     */
    public static float getFloatAtIndex_LE(MemoryAddress addr, long index) {
        return getFloatAtOffset_LE(addr, scale(addr, index, 4));
    }

    /**
     * Writes a float at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloatAtOffset_LE(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the float value to be written.
     */
    public static void setFloatAtIndex_LE(MemoryAddress addr, long index, float value) {
        setFloatAtOffset_LE(addr, scale(addr, index, 4), value);
    }

    /**
     * Read a long from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getLongAtOffset_LE(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a long value read from {@code addr} at the element index specified by {@code index}.
     */
    public static long getLongAtIndex_LE(MemoryAddress addr, long index) {
        return getLongAtOffset_LE(addr, scale(addr, index, 8));
    }

    /**
     * Writes a long at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLongAtOffset_LE(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the long value to be written.
     */
    public static void setLongAtIndex_LE(MemoryAddress addr, long index, long value) {
        setLongAtOffset_LE(addr, scale(addr, index, 8), value);
    }

    /**
     * Read a double from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getDoubleAtOffset_LE(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a double value read from {@code addr} at the element index specified by {@code index}.
     */
    public static double getDoubleAtIndex_LE(MemoryAddress addr, long index) {
        return getDoubleAtOffset_LE(addr, scale(addr, index, 8));
    }

    /**
     * Writes a double at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDoubleAtOffset_LE(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the double value to be written.
     */
    public static void setDoubleAtIndex_LE(MemoryAddress addr, long index, double value) {
        setDoubleAtOffset_LE(addr, scale(addr, index, 8), value);
    }

    /**
     * Read a byte from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByteAtOffset_BE(addr, index);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @return a byte value read from {@code addr} at the element index specified by {@code index}.
     */
    public static byte getByteAtIndex_BE(MemoryAddress addr, long index) {
        return getByteAtOffset_BE(addr, index);
    }

    /**
     * Writes a byte at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByteAtOffset_BE(addr, index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @param value the byte value to be written.
     */
    public static void setByteAtIndex_BE(MemoryAddress addr, long index, byte value) {
        setByteAtOffset_BE(addr, index, value);
    }

    /**
     * Read a char from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getCharAtOffset_BE(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a char value read from {@code addr} at the element index specified by {@code index}.
     */
    public static char getCharAtIndex_BE(MemoryAddress addr, long index) {
        return getCharAtOffset_BE(addr, scale(addr, index, 2));
    }

    /**
     * Writes a char at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setCharAtOffset_BE(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the char value to be written.
     */
    public static void setCharAtIndex_BE(MemoryAddress addr, long index, char value) {
        setCharAtOffset_BE(addr, scale(addr, index, 2), value);
    }

    /**
     * Read a short from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShortAtOffset_BE(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a short value read from {@code addr} at the element index specified by {@code index}.
     */
    public static short getShortAtIndex_BE(MemoryAddress addr, long index) {
        return getShortAtOffset_BE(addr, scale(addr, index, 2));
    }

    /**
     * Writes a short at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShortAtOffset_BE(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the short value to be written.
     */
    public static void setShortAtIndex_BE(MemoryAddress addr, long index, short value) {
        setShortAtOffset_BE(addr, scale(addr, index, 2), value);
    }

    /**
     * Read an int from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getIntAtOffset_BE(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return an int value read from {@code addr} at the element index specified by {@code index}.
     */
    public static int getIntAtIndex_BE(MemoryAddress addr, long index) {
        return getIntAtOffset_BE(addr, scale(addr, index, 4));
    }

    /**
     * Writes an int at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setIntAtOffset_BE(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the int value to be written.
     */
    public static void setIntAtIndex_BE(MemoryAddress addr, long index, int value) {
        setIntAtOffset_BE(addr, scale(addr, index, 4), value);
    }

    /**
     * Read a float from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloatAtOffset_BE(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return a float value read from {@code addr} at the element index specified by {@code index}.
     */
    public static float getFloatAtIndex_BE(MemoryAddress addr, long index) {
        return getFloatAtOffset_BE(addr, scale(addr, index, 4));
    }

    /**
     * Writes a float at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloatAtOffset_BE(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the float value to be written.
     */
    public static void setFloatAtIndex_BE(MemoryAddress addr, long index, float value) {
        setFloatAtOffset_BE(addr, scale(addr, index, 4), value);
    }

    /**
     * Read a long from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getLongAtOffset_BE(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a long value read from {@code addr} at the element index specified by {@code index}.
     */
    public static long getLongAtIndex_BE(MemoryAddress addr, long index) {
        return getLongAtOffset_BE(addr, scale(addr, index, 8));
    }

    /**
     * Writes a long at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLongAtOffset_BE(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the long value to be written.
     */
    public static void setLongAtIndex_BE(MemoryAddress addr, long index, long value) {
        setLongAtOffset_BE(addr, scale(addr, index, 8), value);
    }

    /**
     * Read a double from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getDoubleAtOffset_BE(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a double value read from {@code addr} at the element index specified by {@code index}.
     */
    public static double getDoubleAtIndex_BE(MemoryAddress addr, long index) {
        return getDoubleAtOffset_BE(addr, scale(addr, index, 8));
    }

    /**
     * Writes a double at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDoubleAtOffset_BE(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the double value to be written.
     */
    public static void setDoubleAtIndex_BE(MemoryAddress addr, long index, double value) {
        setDoubleAtOffset_BE(addr, scale(addr, index, 8), value);
    }

    /**
     * Read a byte from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByteAtOffset(addr, index);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @return a byte value read from {@code addr} at the element index specified by {@code index}.
     */
    public static byte getByteAtIndex(MemoryAddress addr, long index) {
        return getByteAtOffset(addr, index);
    }

    /**
     * Writes a byte at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByteAtOffset(addr, index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @param value the byte value to be written.
     */
    public static void setByteAtIndex(MemoryAddress addr, long index, byte value) {
        setByteAtOffset(addr, index, value);
    }

    /**
     * Read a char from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getCharAtOffset(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a char value read from {@code addr} at the element index specified by {@code index}.
     */
    public static char getCharAtIndex(MemoryAddress addr, long index) {
        return getCharAtOffset(addr, scale(addr, index, 2));
    }

    /**
     * Writes a char at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setCharAtOffset(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the char value to be written.
     */
    public static void setCharAtIndex(MemoryAddress addr, long index, char value) {
        setCharAtOffset(addr, scale(addr, index, 2), value);
    }

    /**
     * Read a short from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShortAtOffset(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a short value read from {@code addr} at the element index specified by {@code index}.
     */
    public static short getShortAtIndex(MemoryAddress addr, long index) {
        return getShortAtOffset(addr, scale(addr, index, 2));
    }

    /**
     * Writes a short at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShortAtOffset(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the short value to be written.
     */
    public static void setShortAtIndex(MemoryAddress addr, long index, short value) {
        setShortAtOffset(addr, scale(addr, index, 2), value);
    }

    /**
     * Read an int from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getIntAtOffset(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return an int value read from {@code addr} at the element index specified by {@code index}.
     */
    public static int getIntAtIndex(MemoryAddress addr, long index) {
        return getIntAtOffset(addr, scale(addr, index, 4));
    }

    /**
     * Writes an int at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setIntAtOffset(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the int value to be written.
     */
    public static void setIntAtIndex(MemoryAddress addr, long index, int value) {
        setIntAtOffset(addr, scale(addr, index, 4), value);
    }

    /**
     * Read a float from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloatAtOffset(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return a float value read from {@code addr} at the element index specified by {@code index}.
     */
    public static float getFloatAtIndex(MemoryAddress addr, long index) {
        return getFloatAtOffset(addr, scale(addr, index, 4));
    }

    /**
     * Writes a float at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloatAtOffset(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the float value to be written.
     */
    public static void setFloatAtIndex(MemoryAddress addr, long index, float value) {
        setFloatAtOffset(addr, scale(addr, index, 4), value);
    }

    /**
     * Read a long from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getLongAtOffset(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a long value read from {@code addr} at the element index specified by {@code index}.
     */
    public static long getLongAtIndex(MemoryAddress addr, long index) {
        return getLongAtOffset(addr, scale(addr, index, 8));
    }

    /**
     * Writes a long at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLongAtOffset(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the long value to be written.
     */
    public static void setLongAtIndex(MemoryAddress addr, long index, long value) {
        setLongAtOffset(addr, scale(addr, index, 8), value);
    }

    /**
     * Read a double from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getDoubleAtOffset(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a double value read from {@code addr} at the element index specified by {@code index}.
     */
    public static double getDoubleAtIndex(MemoryAddress addr, long index) {
        return getDoubleAtOffset(addr, scale(addr, index, 8));
    }

    /**
     * Writes a double at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDoubleAtOffset(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the double value to be written.
     */
    public static void setDoubleAtIndex(MemoryAddress addr, long index, double value) {
        setDoubleAtOffset(addr, scale(addr, index, 8), value);
    }

    /**
     * Read a memory address from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getAddressAtOffset(addr, index * 8);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a memory address read from {@code addr} at the element index specified by {@code index}.
     */
    public static MemoryAddress getAddressAtIndex(MemoryAddress addr, long index) {
        return getAddressAtOffset(addr, scale(addr, index, 8));
    }

    /**
     * Writes a memory address at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setAddressAtOffset(addr, index * 8, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the memory address to be written.
     */
    public static void setAddressAtIndex(MemoryAddress addr, long index, MemoryAddress value) {
        setAddressAtOffset(addr, scale(addr, index, 8), value);
    }

    @ForceInline
    private static long scale(MemoryAddress address, long index, int size) {
        return MemoryAddressProxy.multiplyOffsets(index, size, (MemoryAddressProxy)address);
    }
}
