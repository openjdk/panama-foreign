/*
 *  Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
 */

package jdk.internal.foreign;

import jdk.internal.foreign.abi.SharedUtils;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.*;

/**
 * Miscellaneous functions to read and write strings, in various charsets.
 */
public class StringSupport {
    public static String read(MemorySegment segment, long offset, Charset charset) {
        return switch (CharsetKind.of(charset)) {
            case SINGLE_BYTE -> readFast_byte(segment, offset, charset);
            case DOUBLE_BYTE -> readFast_short(segment, offset, charset);
            case QUAD_BYTE -> readFast_int(segment, offset, charset);
            default -> throw new UnsupportedOperationException("Unsupported charset: " + charset);
        };
    }

    public static void write(MemorySegment segment, long offset, Charset charset, String string) {
        switch (CharsetKind.of(charset)) {
            case SINGLE_BYTE -> writeFast_byte(segment, offset, charset, string);
            case DOUBLE_BYTE -> writeFast_short(segment, offset, charset, string);
            case QUAD_BYTE -> writeFast_int(segment, offset, charset, string);
            default -> throw new UnsupportedOperationException("Unsupported charset: " + charset);
        }
    }
    private static String readFast_byte(MemorySegment segment, long offset, Charset charset) {
        long len = segment.byteSize() < Long.MAX_VALUE
                // We can only read in chunks if the segment is bound
                ? chunked_strlen_byte(segment, offset)
                : strlen_byte(segment, offset);
        byte[] bytes = new byte[(int)len];
        MemorySegment.copy(segment, JAVA_BYTE, offset, bytes, 0, (int)len);
        return new String(bytes, charset);
    }

    private static void writeFast_byte(MemorySegment segment, long offset, Charset charset, String string) {
        byte[] bytes = string.getBytes(charset);
        MemorySegment.copy(bytes, 0, segment, JAVA_BYTE, offset, bytes.length);
        segment.set(JAVA_BYTE, offset + bytes.length, (byte)0);
    }

    private static String readFast_short(MemorySegment segment, long offset, Charset charset) {
        long len = strlen_short(segment, offset);
        byte[] bytes = new byte[(int)len];
        MemorySegment.copy(segment, JAVA_BYTE, offset, bytes, 0, (int)len);
        return new String(bytes, charset);
    }

    private static void writeFast_short(MemorySegment segment, long offset, Charset charset, String string) {
        byte[] bytes = string.getBytes(charset);
        MemorySegment.copy(bytes, 0, segment, JAVA_BYTE, offset, bytes.length);
        segment.set(JAVA_SHORT, offset + bytes.length, (short)0);
    }

    private static String readFast_int(MemorySegment segment, long offset, Charset charset) {
        long len = strlen_int(segment, offset);
        byte[] bytes = new byte[(int)len];
        MemorySegment.copy(segment, JAVA_BYTE, offset, bytes, 0, (int)len);
        return new String(bytes, charset);
    }

    private static void writeFast_int(MemorySegment segment, long offset, Charset charset, String string) {
        byte[] bytes = string.getBytes(charset);
        MemorySegment.copy(bytes, 0, segment, JAVA_BYTE, offset, bytes.length);
        segment.set(JAVA_INT, offset + bytes.length, 0);
    }

    // Create an array handle for which the index parameter is always zero
    private static final VarHandle LONG_HANDLE =
            MethodHandles.insertCoordinates(MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.nativeOrder()), 1, 0);

    // This method is inspired by the `glibc/string/strlen.c` implementation
    private static int chunked_strlen_byte(MemorySegment segment, long start) {

        long startAddress = segment.address() + start;
        int headCount = (int)(SharedUtils.alignUp(startAddress, Long.BYTES) - startAddress);

        int offset = 0;
        for (; offset < headCount; offset++) {
            byte curr = segment.get(JAVA_BYTE, start + offset);
            if (curr == 0) {
                return offset;
            }
        }

        // We are now on a long-aligned boundary
        int bodyCount = (int)Math.min(
                // Make sure we do not wrap around
                Integer.MAX_VALUE - Long.BYTES,
                // Remaining bytes to consider
                (segment.byteSize() - start - headCount)  // segment.byteSize() might be Long.MAX_VALUE
        ) & -Long.BYTES;

        for (; offset < bodyCount; offset += Long.BYTES) {
            // We know we are `long` aligned so, we can save on alignment checking here
            long curr = segment.get(JAVA_LONG_UNALIGNED, start + offset);
            // Is this a candidate?
            if (mightContainZeroByte(curr)) {
                byte[] arr = new byte[Long.BYTES];
                // Check the actual content
                LONG_HANDLE.set(arr, curr);
                for (int j = 0; j < 8; j++) {
                    if (arr[j] == 0) {
                        return offset + j;
                    }
                }
            }
        }

        // Handle the tail
        return offset + strlen_byte(segment, start + offset);
    }

    /* Bits 63 and N * 8 (N = 1..7) of this number are zero.  Call these bits
       the "holes".  Note that there is a hole just to the left of
       each byte, with an extra at the end:

       bits:  01111110 11111110 11111110 11111110 11111110 11111110 11111110 11111111
       bytes: AAAAAAAA BBBBBBBB CCCCCCCC DDDDDDDD EEEEEEEE FFFFFFFF GGGGGGGG HHHHHHHH

       The 1-bits make sure that carries propagate to the next 0-bit.
       The 0-bits provide holes for carries to fall into.
    */
    private static final long HI_MAGIC = 0x8080_8080_8080_8080L;
    private static final long LO_MAGIC = 0x0101_0101_0101_0101L;

    static boolean mightContainZeroByte(long l) {
        return ((l - LO_MAGIC) & (~l) & HI_MAGIC) != 0;
    }

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

    private static int strlen_int(MemorySegment segment, long start) {
        // iterate until overflow (String can only hold a byte[], whose length can be expressed as an int)
        for (int offset = 0; offset >= 0; offset += 4) {
            int curr = segment.get(JAVA_INT, start + offset);
            if (curr == 0) {
                return offset;
            }
        }
        throw new IllegalArgumentException("String too large");
    }

    public enum CharsetKind {
        SINGLE_BYTE(1),
        DOUBLE_BYTE(2),
        QUAD_BYTE(4);

        final int terminatorCharSize;

        CharsetKind(int terminatorCharSize) {
            this.terminatorCharSize = terminatorCharSize;
        }

        public int terminatorCharSize() {
            return terminatorCharSize;
        }

        public static CharsetKind of(Charset charset) {
            if (charset == StandardCharsets.UTF_8 || charset == StandardCharsets.ISO_8859_1 || charset == StandardCharsets.US_ASCII) {
                return CharsetKind.SINGLE_BYTE;
            } else if (charset == StandardCharsets.UTF_16LE || charset == StandardCharsets.UTF_16BE || charset == StandardCharsets.UTF_16) {
                return CharsetKind.DOUBLE_BYTE;
            } else if (charset == StandardCharsets.UTF_32LE || charset == StandardCharsets.UTF_32BE || charset == StandardCharsets.UTF_32) {
                return CharsetKind.QUAD_BYTE;
            } else {
                throw new UnsupportedOperationException("Unsupported charset: " + charset);
            }
        }
    }
}
