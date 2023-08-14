/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
package org.openjdk.bench.java.lang.foreign;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.foreign.ValueLayout.*;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsPrepend = {"--add-modules=jdk.incubator.vector"}, jvmArgsAppend = {"--enable-native-access=ALL-UNNAMED", "--enable-preview"})
public class InternalStrLen {

    private MemorySegment singleByteSegment;
    private MemorySegment singleByteSegmentMisaligned;
    private MemorySegment doubleByteSegment;
    private MemorySegment quadByteSegment;

    @Param({"1", "4", "16", "251", "1024"})
    // @Param({"4", "251"})
    int size;

    // Todo: The segment might not be aligned to the element size? E.g. a short might be byte aligned

    @Setup
    public void setup() {
        singleByteSegment = Arena.ofAuto().allocate((size + 1L) * Byte.BYTES);
        singleByteSegmentMisaligned = Arena.ofAuto().allocate((size + 1L) * Byte.BYTES);
        doubleByteSegment = Arena.ofAuto().allocate((size + 1L) * Short.BYTES);
        quadByteSegment = Arena.ofAuto().allocate((size + 1L) * Integer.BYTES);
        Stream.of(singleByteSegment, doubleByteSegment, quadByteSegment)
                .forEach(s -> IntStream.range(0, (int) s.byteSize() - 1)
                        .forEach(i -> s.set(
                                ValueLayout.JAVA_BYTE,
                                i,
                                (byte) ThreadLocalRandom.current().nextInt(1, 254)
                        )));
        singleByteSegment.set(ValueLayout.JAVA_BYTE, singleByteSegment.byteSize() - Byte.BYTES, (byte) 0);
        doubleByteSegment.set(ValueLayout.JAVA_SHORT, doubleByteSegment.byteSize() - Short.BYTES, (short) 0);
        quadByteSegment.set(ValueLayout.JAVA_INT, quadByteSegment.byteSize() - Integer.BYTES, 0);
        singleByteSegmentMisaligned = Arena.ofAuto().allocate(singleByteSegment.byteSize() + 1).
                asSlice(1);
        MemorySegment.copy(singleByteSegment, 0, singleByteSegmentMisaligned, 0, singleByteSegment.byteSize());
    }

    @Benchmark
    public int legacySingleByte() {
        return legacy_strlen_byte(singleByteSegment, 0);
    }

    @Benchmark
    public int legacySingleByteMisaligned() {
        return legacy_strlen_byte(singleByteSegmentMisaligned, 0);
    }

    @Benchmark
    public int legacyDoubleByte() {
        return legacy_strlen_short(doubleByteSegment, 0);
    }

    @Benchmark
    public int legacyQuadByte() {
        return legacy_strlen_int(quadByteSegment, 0);
    }

    //

    @Benchmark
    public int newSingleByte() {
        return chunked_strlen_byte(singleByteSegment, 0);
    }

    @Benchmark
    public int newSingleByteMisaligned() {
        return chunked_strlen_byte(singleByteSegmentMisaligned, 0);
    }

    @Benchmark
    public int newDoubleByte() {
        return chunked_strlen_short(doubleByteSegment, 0);
    }

    @Benchmark
    public int newQuadByte() {
        return strlen_int(quadByteSegment, 0);
    }

    // New methods

    /**
     * {@return the shortest distance beginning at the provided {@code start}
     *  to the encountering of a zero byte in the provided {@code segment}}
     * <p>
     * The method divides the region of interest into three distinct regions:
     * <ul>
     *     <li>head (un-aligned access handling on a byte-by-byte basis) (if any)</li>
     *     <li>body (long aligned access handling eight bytes at a time) (if any)</li>
     *     <li>tail (un-aligned access handling on a byte-by-byte basis) (if any)</li>
     * </ul>
     * <p>
     * The body is using a heuristic method to determine if a long word
     * contains a zero byte. The method might have false positives but
     * never false negatives.
     * <p>
     * This method is inspired by the `glibc/string/strlen.c` implementation
     *
     * @param segment to examine
     * @param start   from where examination shall begin
     * @throws IllegalArgumentException if the examined region contains no zero bytes
     *                                  within a length that can be accepted by a String
     */
    private static int chunked_strlen_byte(MemorySegment segment, long start) {

        // Handle the first unaligned "head" bytes separately
        int headCount = (int) remainsToAlignment(segment.address() + start, Long.BYTES);

        int offset = 0;
        for (; offset < headCount; offset++) {
            byte curr = segment.get(JAVA_BYTE, start + offset);
            if (curr == 0) {
                return offset;
            }
        }

        // We are now on a long-aligned boundary so this is the "body"
        int bodyCount = bodyCount(segment.byteSize() - start - headCount);

        for (; offset < bodyCount; offset += Long.BYTES) {
            // We know we are `long` aligned so, we can save on alignment checking here
            long curr = segment.get(JAVA_LONG_UNALIGNED, start + offset);
            // Is this a candidate?
            if (mightContainZeroByte(curr)) {
                for (int j = 0; j < 8; j++) {
                    if (segment.get(JAVA_BYTE, start + offset + j) == 0) {
                        return offset + j;
                    }
                }
            }
        }

        // Handle the "tail"
        return requireWithinArraySize((long) offset + strlen_byte(segment, start + offset));
    }

    /* Bits 63 and N * 8 (N = 1..7) of this number are zero.  Call these bits
       the "holes".  Note that there is a hole just to the left of
       each byte, with an extra at the end:

       bits:  01111110 11111110 11111110 11111110 11111110 11111110 11111110 11111111
       bytes: AAAAAAAA BBBBBBBB CCCCCCCC DDDDDDDD EEEEEEEE FFFFFFFF GGGGGGGG HHHHHHHH

       The 1-bits make sure that carries propagate to the next 0-bit.
       The 0-bits provide holes for carries to fall into.
    */
    private static final long HIMAGIC_FOR_BYTES = 0x8080_8080_8080_8080L;
    private static final long LOMAGIC_FOR_BYTES = 0x0101_0101_0101_0101L;

    static boolean mightContainZeroByte(long l) {
        return ((l - LOMAGIC_FOR_BYTES) & (~l) & HIMAGIC_FOR_BYTES) != 0;
    }

    private static final long HIMAGIC_FOR_SHORTS = 0x8000_8000_8000_8000L;
    private static final long LOMAGIC_FOR_SHORTS = 0x0001_0001_0001_0001L;

    static boolean mightContainZeroShort(long l) {
        return ((l - LOMAGIC_FOR_SHORTS) & (~l) & HIMAGIC_FOR_SHORTS) != 0;
    }

    static int requireWithinArraySize(long size) {
        if (size > SOFT_MAX_ARRAY_LENGTH) {
            throw newIaeStringTooLarge();
        }
        return (int) size;
    }

    static int bodyCount(long remaining) {
        return (int) Math.min(
                // Make sure we do not wrap around
                Integer.MAX_VALUE - Long.BYTES,
                // Remaining bytes to consider
                remaining)
                & -Long.BYTES; // Mask 0xFFFFFFF8
    }

    private static int strlen_byte(MemorySegment segment, long start) {
        for (int offset = 0; offset < SOFT_MAX_ARRAY_LENGTH; offset += 1) {
            byte curr = segment.get(JAVA_BYTE, start + offset);
            if (curr == 0) {
                return offset;
            }
        }
        throw newIaeStringTooLarge();
    }

    /**
     * {@return the shortest distance beginning at the provided {@code start}
     *  to the encountering of a zero short in the provided {@code segment}}
     * <p>
     * Note: The inspected region must be short aligned.
     *
     * @see #chunked_strlen_byte(MemorySegment, long) for more information
     *
     * @param segment to examine
     * @param start   from where examination shall begin
     * @throws IllegalArgumentException if the examined region contains no zero shorts
     *                                  within a length that can be accepted by a String
     */
    private static int chunked_strlen_short(MemorySegment segment, long start) {

        // Handle the first unaligned "head" bytes separately
        int headCount = (int)remainsToAlignment(segment.address() + start, Long.BYTES);

        int offset = 0;
        for (; offset < headCount; offset += Short.BYTES) {
            short curr = segment.get(JAVA_SHORT, start + offset);
            if (curr == 0) {
                return offset;
            }
        }

        // We are now on a long-aligned boundary so this is the "body"
        int bodyCount = bodyCount(segment.byteSize() - start - headCount);

        for (; offset < bodyCount; offset += Long.BYTES) {
            // We know we are `long` aligned so, we can save on alignment checking here
            long curr = segment.get(JAVA_LONG_UNALIGNED, start + offset);
            // Is this a candidate?
            if (mightContainZeroShort(curr)) {
                for (int j = 0; j < Long.BYTES; j += Short.BYTES) {
                    if (segment.get(JAVA_SHORT_UNALIGNED, start + offset + j) == 0) {
                        return offset + j;
                    }
                }
            }
        }

        // Handle the "tail"
        return requireWithinArraySize((long) offset + strlen_short(segment, start + offset));
    }

    private static int strlen_short(MemorySegment segment, long start) {
        // Do an initial read using aligned semantics.
        // If this succeeds, we know that all other subsequent reads will be aligned
        if (segment.get(JAVA_SHORT, start) == (short)0) {
            return 0;
        }
        for (int offset = Short.BYTES; offset < SOFT_MAX_ARRAY_LENGTH; offset += Short.BYTES) {
            short curr = segment.get(JAVA_SHORT_UNALIGNED, start + offset);
            if (curr == (short)0) {
                return offset;
            }
        }
        throw newIaeStringTooLarge();
    }

    // The gain of using `long` wide operations for `int` is lower than for the two other `byte` and `short` variants
    private static int strlen_int(MemorySegment segment, long start) {
        // Do an initial read using aligned semantics.
        // If this succeeds, we know that all other subsequent reads will be aligned
        if (segment.get(JAVA_INT, start) == 0) {
            return 0;
        }
        for (int offset = Integer.BYTES; offset < SOFT_MAX_ARRAY_LENGTH; offset += Integer.BYTES) {
            // We are guaranteed to be aligned here so, we can use unaligned access.
            int curr = segment.get(JAVA_INT_UNALIGNED, start + offset);
            if (curr == 0) {
                return offset;
            }
        }
        throw newIaeStringTooLarge();
    }

    private static IllegalArgumentException newIaeStringTooLarge() {
        return new IllegalArgumentException("String too large");
    }

    public static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    // These are the legacy methods

    private static int legacy_strlen_byte(MemorySegment segment, long start) {
        // iterate until overflow (String can only hold a byte[], whose length can be expressed as an int)
        for (int offset = 0; offset >= 0; offset++) {
            byte curr = segment.get(JAVA_BYTE, start + offset);
            if (curr == 0) {
                return offset;
            }
        }
        throw new IllegalArgumentException("String too large");
    }

    private static int legacy_strlen_short(MemorySegment segment, long start) {
        // iterate until overflow (String can only hold a byte[], whose length can be expressed as an int)
        for (int offset = 0; offset >= 0; offset += 2) {
            short curr = segment.get(JAVA_SHORT, start + offset);
            if (curr == 0) {
                return offset;
            }
        }
        throw new IllegalArgumentException("String too large");
    }

    private static int legacy_strlen_int(MemorySegment segment, long start) {
        // iterate until overflow (String can only hold a byte[], whose length can be expressed as an int)
        for (int offset = 0; offset >= 0; offset += 4) {
            int curr = segment.get(JAVA_INT, start + offset);
            if (curr == 0) {
                return offset;
            }
        }
        throw new IllegalArgumentException("String too large");
    }


    public static long alignUp(long addr, long alignment) {
        return ((addr - 1) | (alignment - 1)) + 1;
    }

    public static long remainsToAlignment(long addr, long alignment) {
        return alignUp(addr, alignment) - addr;
    }

}

