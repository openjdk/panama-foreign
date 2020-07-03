package jdk.incubator.foreign;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * This class defines ready-made static accessors which can be used to dereference memory segments in many ways.
 * <p>
 * The most primitive accessors (see {@link #getInt(MemoryAddress, long)}) take a <em>base</em> address and an offset (expressed in bytes).
 * The final address at which the dereference will occur will be computed by offsetting the base address by
 * the specified offset, as if by calling {@link MemoryAddress#addOffset(long)} on the specified base address.
 * <p>
 * In cases where no offset is required, overloads are provided (see {@link #getInt(MemoryAddress)}) so that
 * clients can omit the offset coordinate.
 * <p>
 * To help dereferencing in array-like use cases (e.g. where the layout of a given memory segment is a sequence
 * layout of given size an element count), higher-level overloads are also provided (see {@link #getIntElement(MemoryAddress, long)}),
 * which take an <em>base</em> address and a <em>logical</em> element index. The formula to obtain the byte offset {@code O} from an
 * index {@code I} is given by {@code O = I * S} where {@code s} is the size (expressed in bytes) of the element to
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
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
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
    VarHandle handle = MemoryHandles.withStride(BITS_64_LE.withBitAlignment(8).varHandle(double.class), 1L);
    double value = (double)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
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
    VarHandle handle = MemoryHandles.withStride(BITS_64_BE.withBitAlignment(8).varHandle(double.class), 1L);
    double value = (double)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
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
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
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
    public static MemoryAddress getAddress(MemoryAddress addr, long offset) {
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
    public static void setAddress(MemoryAddress addr, long offset, MemoryAddress value) {
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
    byte value = getByte_LE(addr, 0L);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @return a byte value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static byte getByte_LE(MemoryAddress addr) {
        return getByte_LE(addr, 0L);
    }

    /**
     * Writes a byte at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByte_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the byte value to be written.
     */
    public static void setByte_LE(MemoryAddress addr, byte value) {
        setByte_LE(addr, 0L, value);
    }

    /**
     * Read a char from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getChar_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static char getChar_LE(MemoryAddress addr) {
        return getChar_LE(addr, 0L);
    }

    /**
     * Writes a char at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setChar_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the char value to be written.
     */
    public static void setChar_LE(MemoryAddress addr, char value) {
        setChar_LE(addr, 0L, value);
    }

    /**
     * Read a short from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShort_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static short getShort_LE(MemoryAddress addr) {
        return getShort_LE(addr, 0L);
    }

    /**
     * Writes a short at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShort_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the short value to be written.
     */
    public static void setShort_LE(MemoryAddress addr, short value) {
        setShort_LE(addr, 0L, value);
    }

    /**
     * Read an int from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getInt_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static int getInt_LE(MemoryAddress addr) {
        return getInt_LE(addr, 0L);
    }

    /**
     * Writes an int at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setInt_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the int value to be written.
     */
    public static void setInt_LE(MemoryAddress addr, int value) {
        setInt_LE(addr, 0L, value);
    }

    /**
     * Read a float from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloat_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static float getFloat_LE(MemoryAddress addr) {
        return getFloat_LE(addr, 0L);
    }

    /**
     * Writes a float at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloat_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the float value to be written.
     */
    public static void setFloat_LE(MemoryAddress addr, float value) {
        setFloat_LE(addr, 0L, value);
    }

    /**
     * Read a long from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    long value = getLong_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static long getLong_LE(MemoryAddress addr) {
        return getLong_LE(addr, 0L);
    }

    /**
     * Writes a long at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLong_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the long value to be written.
     */
    public static void setLong_LE(MemoryAddress addr, long value) {
        setLong_LE(addr, 0L, value);
    }

    /**
     * Read a double from given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    double value = getDouble_LE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static double getDouble_LE(MemoryAddress addr) {
        return getDouble_LE(addr, 0L);
    }

    /**
     * Writes a double at given address, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDouble_LE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the double value to be written.
     */
    public static void setDouble_LE(MemoryAddress addr, double value) {
        setDouble_LE(addr, 0L, value);
    }

    /**
     * Read a byte from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByte_BE(addr, 0L);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @return a byte value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static byte getByte_BE(MemoryAddress addr) {
        return getByte_BE(addr, 0L);
    }

    /**
     * Writes a byte at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByte_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the byte value to be written.
     */
    public static void setByte_BE(MemoryAddress addr, byte value) {
        setByte_BE(addr, 0L, value);
    }

    /**
     * Read a char from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getChar_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static char getChar_BE(MemoryAddress addr) {
        return getChar_BE(addr, 0L);
    }

    /**
     * Writes a char at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setChar_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the char value to be written.
     */
    public static void setChar_BE(MemoryAddress addr, char value) {
        setChar_BE(addr, 0L, value);
    }

    /**
     * Read a short from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShort_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static short getShort_BE(MemoryAddress addr) {
        return getShort_BE(addr, 0L);
    }

    /**
     * Writes a short at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShort_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the short value to be written.
     */
    public static void setShort_BE(MemoryAddress addr, short value) {
        setShort_BE(addr, 0L, value);
    }

    /**
     * Read an int from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getInt_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static int getInt_BE(MemoryAddress addr) {
        return getInt_BE(addr, 0L);
    }

    /**
     * Writes an int at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setInt_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the int value to be written.
     */
    public static void setInt_BE(MemoryAddress addr, int value) {
        setInt_BE(addr, 0L, value);
    }

    /**
     * Read a float from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloat_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static float getFloat_BE(MemoryAddress addr) {
        return getFloat_BE(addr, 0L);
    }

    /**
     * Writes a float at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloat_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the float value to be written.
     */
    public static void setFloat_BE(MemoryAddress addr, float value) {
        setFloat_BE(addr, 0L, value);
    }

    /**
     * Read a long from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    long value = getLong_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static long getLong_BE(MemoryAddress addr) {
        return getLong_BE(addr, 0L);
    }

    /**
     * Writes a long at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLong_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the long value to be written.
     */
    public static void setLong_BE(MemoryAddress addr, long value) {
        setLong_BE(addr, 0L, value);
    }

    /**
     * Read a double from given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    double value = getDouble_BE(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static double getDouble_BE(MemoryAddress addr) {
        return getDouble_BE(addr, 0L);
    }

    /**
     * Writes a double at given address, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDouble_BE(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the double value to be written.
     */
    public static void setDouble_BE(MemoryAddress addr, double value) {
        setDouble_BE(addr, 0L, value);
    }

    /**
     * Read a byte from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByte(addr, 0L);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @return a byte value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static byte getByte(MemoryAddress addr) {
        return getByte(addr, 0L);
    }

    /**
     * Writes a byte at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByte(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the byte value to be written.
     */
    public static void setByte(MemoryAddress addr, byte value) {
        setByte(addr, 0L, value);
    }

    /**
     * Read a char from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getChar(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a char value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static char getChar(MemoryAddress addr) {
        return getChar(addr, 0L);
    }

    /**
     * Writes a char at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setChar(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the char value to be written.
     */
    public static void setChar(MemoryAddress addr, char value) {
        setChar(addr, 0L, value);
    }

    /**
     * Read a short from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShort(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a short value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static short getShort(MemoryAddress addr) {
        return getShort(addr, 0L);
    }

    /**
     * Writes a short at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShort(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the short value to be written.
     */
    public static void setShort(MemoryAddress addr, short value) {
        setShort(addr, 0L, value);
    }

    /**
     * Read an int from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getInt(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return an int value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static int getInt(MemoryAddress addr) {
        return getInt(addr, 0L);
    }

    /**
     * Writes an int at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setInt(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the int value to be written.
     */
    public static void setInt(MemoryAddress addr, int value) {
        setInt(addr, 0L, value);
    }

    /**
     * Read a float from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloat(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a float value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static float getFloat(MemoryAddress addr) {
        return getFloat(addr, 0L);
    }

    /**
     * Writes a float at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloat(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the float value to be written.
     */
    public static void setFloat(MemoryAddress addr, float value) {
        setFloat(addr, 0L, value);
    }

    /**
     * Read a long from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    long value = getLong(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a long value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static long getLong(MemoryAddress addr) {
        return getLong(addr, 0L);
    }

    /**
     * Writes a long at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLong(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the long value to be written.
     */
    public static void setLong(MemoryAddress addr, long value) {
        setLong(addr, 0L, value);
    }

    /**
     * Read a double from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    double value = getDouble(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a double value read from {@code addr} at the offset specified by {@code offset}.
     */
    public static double getDouble(MemoryAddress addr) {
        return getDouble(addr, 0L);
    }

    /**
     * Writes a double at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDouble(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the double value to be written.
     */
    public static void setDouble(MemoryAddress addr, double value) {
        setDouble(addr, 0L, value);
    }

    /**
     * Read a memory address from given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    MemoryAddress value = getAddress(addr, 0L);
     * }</pre></blockquote>
     * @param addr base address.
     * @return a memory address read from {@code addr} at the offset specified by {@code offset}.
     */
    public static MemoryAddress getAddress(MemoryAddress addr) {
        return getAddress(addr, 0L);
    }

    /**
     * Writes a memory address at given address, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setAddress(addr, 0L, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param value the memory address to be written.
     */
    public static void setAddress(MemoryAddress addr, MemoryAddress value) {
        setAddress(addr, 0L, value);
    }

    /**
     * Read a byte from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByte_LE(addr, index);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @return a byte value read from {@code addr} at the element index specified by {@code index}.
     */
    public static byte getByteElement_LE(MemoryAddress addr, long index) {
        return getByte_LE(addr, index);
    }

    /**
     * Writes a byte at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByte_LE(addr, index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @param value the byte value to be written.
     */
    public static void setByteElement_LE(MemoryAddress addr, long index, byte value) {
        setByte_LE(addr, index, value);
    }

    /**
     * Read a char from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getChar_LE(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a char value read from {@code addr} at the element index specified by {@code index}.
     */
    public static char getCharElement_LE(MemoryAddress addr, long index) {
        return getChar_LE(addr, 2 * index);
    }

    /**
     * Writes a char at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setChar_LE(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the char value to be written.
     */
    public static void setCharElement_LE(MemoryAddress addr, long index, char value) {
        setChar_LE(addr, 2 * index, value);
    }

    /**
     * Read a short from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShort_LE(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a short value read from {@code addr} at the element index specified by {@code index}.
     */
    public static short getShortElement_LE(MemoryAddress addr, long index) {
        return getShort_LE(addr, 2 * index);
    }

    /**
     * Writes a short at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShort_LE(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the short value to be written.
     */
    public static void setShortElement_LE(MemoryAddress addr, long index, short value) {
        setShort_LE(addr, 2 * index, value);
    }

    /**
     * Read an int from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getInt_LE(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return an int value read from {@code addr} at the element index specified by {@code index}.
     */
    public static int getIntElement_LE(MemoryAddress addr, long index) {
        return getInt_LE(addr, 4 * index);
    }

    /**
     * Writes an int at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setInt_LE(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the int value to be written.
     */
    public static void setIntElement_LE(MemoryAddress addr, long index, int value) {
        setInt_LE(addr, 4 * index, value);
    }

    /**
     * Read a float from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloat_LE(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return a float value read from {@code addr} at the element index specified by {@code index}.
     */
    public static float getFloatElement_LE(MemoryAddress addr, long index) {
        return getFloat_LE(addr, 4 * index);
    }

    /**
     * Writes a float at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloat_LE(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the float value to be written.
     */
    public static void setFloatElement_LE(MemoryAddress addr, long index, float value) {
        setFloat_LE(addr, 4 * index, value);
    }

    /**
     * Read a long from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getLong_LE(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a long value read from {@code addr} at the element index specified by {@code index}.
     */
    public static long getLongElement_LE(MemoryAddress addr, long index) {
        return getLong_LE(addr, 8 * index);
    }

    /**
     * Writes a long at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLong_LE(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the long value to be written.
     */
    public static void setLongElement_LE(MemoryAddress addr, long index, long value) {
        setLong_LE(addr, 8 * index, value);
    }

    /**
     * Read a double from given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getDouble_LE(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a double value read from {@code addr} at the element index specified by {@code index}.
     */
    public static double getDoubleElement_LE(MemoryAddress addr, long index) {
        return getDouble_LE(addr, 8 * index);
    }

    /**
     * Writes a double at given address and element index, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDouble_LE(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the double value to be written.
     */
    public static void setDoubleElement_LE(MemoryAddress addr, long index, double value) {
        setDouble_LE(addr, 8 * index, value);
    }

    /**
     * Read a byte from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByte_BE(addr, index);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @return a byte value read from {@code addr} at the element index specified by {@code index}.
     */
    public static byte getByteElement_BE(MemoryAddress addr, long index) {
        return getByte_BE(addr, index);
    }

    /**
     * Writes a byte at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByte_BE(addr, index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @param value the byte value to be written.
     */
    public static void setByteElement_BE(MemoryAddress addr, long index, byte value) {
        setByte_BE(addr, index, value);
    }

    /**
     * Read a char from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getChar_BE(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a char value read from {@code addr} at the element index specified by {@code index}.
     */
    public static char getCharElement_BE(MemoryAddress addr, long index) {
        return getChar_BE(addr, 2 * index);
    }

    /**
     * Writes a char at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setChar_BE(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the char value to be written.
     */
    public static void setCharElement_BE(MemoryAddress addr, long index, char value) {
        setChar_BE(addr, 2 * index, value);
    }

    /**
     * Read a short from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShort_BE(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a short value read from {@code addr} at the element index specified by {@code index}.
     */
    public static short getShortElement_BE(MemoryAddress addr, long index) {
        return getShort_BE(addr, 2 * index);
    }

    /**
     * Writes a short at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShort_BE(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the short value to be written.
     */
    public static void setShortElement_BE(MemoryAddress addr, long index, short value) {
        setShort_BE(addr, 2 * index, value);
    }

    /**
     * Read an int from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getInt_BE(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return an int value read from {@code addr} at the element index specified by {@code index}.
     */
    public static int getIntElement_BE(MemoryAddress addr, long index) {
        return getInt_BE(addr, 4 * index);
    }

    /**
     * Writes an int at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setInt_BE(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the int value to be written.
     */
    public static void setIntElement_BE(MemoryAddress addr, long index, int value) {
        setInt_BE(addr, 4 * index, value);
    }

    /**
     * Read a float from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloat_BE(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return a float value read from {@code addr} at the element index specified by {@code index}.
     */
    public static float getFloatElement_BE(MemoryAddress addr, long index) {
        return getFloat_BE(addr, 4 * index);
    }

    /**
     * Writes a float at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloat_BE(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the float value to be written.
     */
    public static void setFloatElement_BE(MemoryAddress addr, long index, float value) {
        setFloat_BE(addr, 4 * index, value);
    }

    /**
     * Read a long from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getLong_BE(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a long value read from {@code addr} at the element index specified by {@code index}.
     */
    public static long getLongElement_BE(MemoryAddress addr, long index) {
        return getLong_BE(addr, 8 * index);
    }

    /**
     * Writes a long at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLong_BE(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the long value to be written.
     */
    public static void setLongElement_BE(MemoryAddress addr, long index, long value) {
        setLong_BE(addr, 8 * index, value);
    }

    /**
     * Read a double from given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getDouble_BE(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a double value read from {@code addr} at the element index specified by {@code index}.
     */
    public static double getDoubleElement_BE(MemoryAddress addr, long index) {
        return getDouble_BE(addr, 8 * index);
    }

    /**
     * Writes a double at given address and element index, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDouble_BE(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the double value to be written.
     */
    public static void setDoubleElement_BE(MemoryAddress addr, long index, double value) {
        setDouble_BE(addr, 8 * index, value);
    }

    /**
     * Read a byte from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    byte value = getByte(addr, index);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @return a byte value read from {@code addr} at the element index specified by {@code index}.
     */
    public static byte getByteElement(MemoryAddress addr, long index) {
        return getByte(addr, index);
    }

    /**
     * Writes a byte at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setByte(addr, index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index)}.
     * @param value the byte value to be written.
     */
    public static void setByteElement(MemoryAddress addr, long index, byte value) {
        setByte(addr, index, value);
    }

    /**
     * Read a char from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    char value = getChar(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a char value read from {@code addr} at the element index specified by {@code index}.
     */
    public static char getCharElement(MemoryAddress addr, long index) {
        return getChar(addr, 2 * index);
    }

    /**
     * Writes a char at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setChar(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the char value to be written.
     */
    public static void setCharElement(MemoryAddress addr, long index, char value) {
        setChar(addr, 2 * index, value);
    }

    /**
     * Read a short from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    short value = getShort(addr, 2 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @return a short value read from {@code addr} at the element index specified by {@code index}.
     */
    public static short getShortElement(MemoryAddress addr, long index) {
        return getShort(addr, 2 * index);
    }

    /**
     * Writes a short at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setShort(addr, 2 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 2)}.
     * @param value the short value to be written.
     */
    public static void setShortElement(MemoryAddress addr, long index, short value) {
        setShort(addr, 2 * index, value);
    }

    /**
     * Read an int from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    int value = getInt(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return an int value read from {@code addr} at the element index specified by {@code index}.
     */
    public static int getIntElement(MemoryAddress addr, long index) {
        return getInt(addr, 4 * index);
    }

    /**
     * Writes an int at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setInt(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the int value to be written.
     */
    public static void setIntElement(MemoryAddress addr, long index, int value) {
        setInt(addr, 4 * index, value);
    }

    /**
     * Read a float from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    float value = getFloat(addr, 4 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @return a float value read from {@code addr} at the element index specified by {@code index}.
     */
    public static float getFloatElement(MemoryAddress addr, long index) {
        return getFloat(addr, 4 * index);
    }

    /**
     * Writes a float at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setFloat(addr, 4 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 4)}.
     * @param value the float value to be written.
     */
    public static void setFloatElement(MemoryAddress addr, long index, float value) {
        setFloat(addr, 4 * index, value);
    }

    /**
     * Read a long from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getLong(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a long value read from {@code addr} at the element index specified by {@code index}.
     */
    public static long getLongElement(MemoryAddress addr, long index) {
        return getLong(addr, 8 * index);
    }

    /**
     * Writes a long at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setLong(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the long value to be written.
     */
    public static void setLongElement(MemoryAddress addr, long index, long value) {
        setLong(addr, 8 * index, value);
    }

    /**
     * Read a double from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getDouble(addr, 8 * index);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a double value read from {@code addr} at the element index specified by {@code index}.
     */
    public static double getDoubleElement(MemoryAddress addr, long index) {
        return getDouble(addr, 8 * index);
    }

    /**
     * Writes a double at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setDouble(addr, 8 * index, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the double value to be written.
     */
    public static void setDoubleElement(MemoryAddress addr, long index, double value) {
        setDouble(addr, 8 * index, value);
    }

    /**
     * Read a memory address from given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    return getAddress(addr, index * 8);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @return a memory address read from {@code addr} at the element index specified by {@code index}.
     */
    public static MemoryAddress getAddressElement(MemoryAddress addr, long index) {
        return getAddress(addr, index * 8);
    }

    /**
     * Writes a memory address at given address and element index, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    setAddress(addr, index * 8, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param index element index (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(index * 8)}.
     * @param value the memory address to be written.
     */
    public static void setAddressElement(MemoryAddress addr, long index, MemoryAddress value) {
        setAddress(addr, index * 8, value);
    }
}
