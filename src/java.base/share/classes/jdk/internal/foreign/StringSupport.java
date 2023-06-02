package jdk.internal.foreign;

import jdk.internal.vm.annotation.ForceInline;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/**
 * Miscellaneous functions to read and write strings, in various charsets.
 */
public class StringSupport {

    @ForceInline
    public static String read(MemorySegment segment, long offset, Charset charset) {
        return switch (CharsetKind.of(charset)) {
            case SINGLE_BYTE -> readFast_byte(segment, offset, charset);
            case DOUBLE_BYTE -> readFast_short(segment, offset, charset);
            default -> throw new UnsupportedOperationException("Unsupported charset: " + charset);
        };
    }

    @ForceInline
    public static void write(MemorySegment segment, long offset, Charset charset, String string) {
        switch (CharsetKind.of(charset)) {
            case SINGLE_BYTE -> writeFast_byte(segment, offset, charset, string);
            case DOUBLE_BYTE -> writeFast_short(segment, offset, charset, string);
            default -> throw new UnsupportedOperationException("Unsupported charset: " + charset);
        }
    }

    @ForceInline
    private static String readFast_byte(MemorySegment segment, long offset, Charset charset) {
        long len = strlen_byte(segment, offset);
        byte[] bytes = new byte[(int)len];
        MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, offset, bytes, 0, (int)len);
        return new String(bytes, charset);
    }

    @ForceInline
    private static void writeFast_byte(MemorySegment segment, long offset, Charset charset, String string) {
        byte[] bytes = string.getBytes(charset);
        MemorySegment.copy(bytes, 0, segment, ValueLayout.JAVA_BYTE, offset, bytes.length);
        segment.set(ValueLayout.JAVA_BYTE, offset + bytes.length, (byte)0);
    }

    @ForceInline
    private static String readFast_short(MemorySegment segment, long offset, Charset charset) {
        long len = strlen_short(segment, offset);
        byte[] bytes = new byte[(int)len];
        MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, offset, bytes, 0, (int)len);
        return new String(bytes, charset);
    }

    @ForceInline
    private static void writeFast_short(MemorySegment segment, long offset, Charset charset, String string) {
        byte[] bytes = string.getBytes(charset);
        MemorySegment.copy(bytes, 0, segment, JAVA_BYTE, offset, bytes.length);
        segment.set(JAVA_SHORT, offset + bytes.length, (short)0);
    }

    @ForceInline
    private static int strlen_byte(MemorySegment segment, long start) {
        // iterate until overflow (String can only hold a byte[], whose length can be expressed as an int)
        for (int offset = 0; offset >= 0; offset++) {
            byte curr = segment.get(JAVA_BYTE, start + offset);
            if (curr == 0) {
                return offset;
            }
        }
        throw new IllegalArgumentException("String too large");
    }

    @ForceInline
    private static int strlen_short(MemorySegment segment, long start) {
        // iterate until overflow (String can only hold a byte[], whose length can be expressed as an int)
        for (int offset = 0; offset >= 0; offset += 2) {
            short curr = segment.get(JAVA_SHORT, start + offset);
            if (curr == 0) {
                return offset;
            }
        }
        throw new IllegalArgumentException("String too large");
    }

    public enum CharsetKind {
        SINGLE_BYTE(1),
        DOUBLE_BYTE(2);

        final int terminatorCharSize;

        CharsetKind(int terminatorCharSize) {
            this.terminatorCharSize = terminatorCharSize;
        }

        public int getTerminatorCharSize() {
            return terminatorCharSize;
        }

        public static CharsetKind of(Charset charset) {
            if (charset == StandardCharsets.UTF_8 || charset == StandardCharsets.ISO_8859_1 || charset == StandardCharsets.US_ASCII) {
                return CharsetKind.SINGLE_BYTE;
            } else if (charset == StandardCharsets.UTF_16LE || charset == StandardCharsets.UTF_16BE || charset == StandardCharsets.UTF_16) {
                return CharsetKind.DOUBLE_BYTE;
            } else {
                throw new UnsupportedOperationException("Unsupported charset: " + charset);
            }
        }
    }
}
