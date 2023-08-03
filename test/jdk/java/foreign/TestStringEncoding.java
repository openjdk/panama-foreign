/*
 * Copyright (c) 2021, 2023, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.testng.annotations.*;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static org.testng.Assert.*;

/*
 * @test
 * @run testng TestStringEncoding
 */

public class TestStringEncoding {

    @Test(dataProvider = "strings")
    public void testStrings(String testString) {
        for (Charset charset : Charset.availableCharsets().values()) {
            if (isStandard(charset)) {
                try (Arena arena = Arena.ofConfined()) {
                    MemorySegment text = arena.allocateFrom(testString, charset);

                    int terminatorSize = "\0".getBytes(charset).length;
                    if (charset == StandardCharsets.UTF_16) {
                        terminatorSize -= 2; // drop BOM
                    }
                    // Note that the JDK's UTF_32 encoder doesn't add a BOM.
                    // This is legal under the Unicode standard, and means the byte order is BE.
                    // See: https://unicode.org/faq/utf_bom.html#gen7

                    int expectedByteLength =
                            testString.getBytes(charset).length +
                            terminatorSize;

                    assertEquals(text.byteSize(), expectedByteLength);

                    String roundTrip = text.getString(0, charset);
                    if (charset.newEncoder().canEncode(testString)) {
                        assertEquals(roundTrip, testString);
                    }
                }
            } else {
                assertThrows(UnsupportedOperationException.class, () -> Arena.global().allocateFrom(testString, charset));
            }
        }
    }

    @Test(dataProvider = "strings")
    public void unboundedSegment(String testString) {
        testModifyingSegment(testString,
                standardCharsets(),
                s -> s.reinterpret(Long.MAX_VALUE),
                UnaryOperator.identity());
    }

    @Test(dataProvider = "strings")
    public void unalignedSegmentSingleByte(String testString) {
        testModifyingSegment(testString,
                singleByteCharsets(),
                s -> s.byteSize() > 1 ? s.asSlice(1) : s,
                s -> s.length() > 0 ? s.substring(1) : s);
    }

    @Test(dataProvider = "strings")
    public void expandedSegment(String testString) {
        try (var arena = Arena.ofConfined()) {
            for (int i = 0; i < Long.BYTES; i++) {
                int extra = i;
                testModifyingSegment(testString,
                        // Single byte charsets
                        standardCharsets(),
                        s -> {
                            var s2 = arena.allocate(s.byteSize() + extra);
                            MemorySegment.copy(s, 0, s2, 0, s.byteSize());
                            return s2;
                        },
                        UnaryOperator.identity());
            }
        }
    }

    public void testModifyingSegment(String testString,
                                     List<Charset> charsets,
                                     UnaryOperator<MemorySegment> segmentMapper,
                                     UnaryOperator<String> stringMapper) {
        for (var charset: charsets) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment text = arena.allocateFrom(testString, charset);
                text = segmentMapper.apply(text);
                String roundTrip = text.getString(0, charset);
                String expected = stringMapper.apply(testString);
                if (charset.newEncoder().canEncode(testString)) {
                    assertEquals(roundTrip, expected);
                }
            }
        }
    }

    @Test()
    public void testPeculiarContentSingleByte() {
        Random random = new Random(42);
        for (int len = 7; len < 71; len++) {
            try (var arena = Arena.ofConfined()) {
                var segment = arena.allocate(len, 4);
                var arr = new byte[len];
                random.nextBytes(arr);
                segment.copyFrom(MemorySegment.ofArray(arr));
                int terminatorIndex = random.nextInt(len);
                segment.set(ValueLayout.JAVA_BYTE, terminatorIndex, (byte) 0);
                for (Charset charset : singleByteCharsets()) {
                    var s = segment.getString(0, charset);
                    var ref = referenceImpl(segment, 0, charset);
                    assertEquals(s, ref);
                }
            }
        }
    }


    public void testOffset() {
        // Use offset in
    }

    @Test()
    public void testJumboSegment() {

        try (var arena = Arena.ofConfined()) {
            var segment = arena.allocate((long) Integer.MAX_VALUE + 100);
        }

    }

    void nativeSegFromNativeCall() {

    }

    @DataProvider
    public static Object[][] strings() {
        return new Object[][] {
            { "testing" },
            { "" },
            { "X" },
            { "12345" },
            { "yen \u00A5" },
            { "snowman \u26C4" },
            { "rainbow \uD83C\uDF08" },
            {"0"},
            {"01"},
            {"012"},
            {"0123"},
            {"01234"},
            {"012345"},
            {"0123456"},
            {"01234567"},
            {"012345678"},
            {"0123456789"}
        };
    }

    boolean isStandard(Charset charset) {
        for (Field standardCharset : StandardCharsets.class.getDeclaredFields()) {
            try {
                if (standardCharset.get(null) == charset) {
                    return true;
                }
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
        return false;
    }

    List<Charset> standardCharsets() {
        return Charset.availableCharsets().values().stream()
                .filter(this::isStandard)
                .toList();
    }

    List<Charset> singleByteCharsets() {
        return Arrays.asList(StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1, StandardCharsets.US_ASCII);
    }


    static String referenceImpl(MemorySegment segment, long offset, Charset charset) {
        long len = strlen_byte(segment, offset);
        byte[] bytes = new byte[(int)len];
        MemorySegment.copy(segment, JAVA_BYTE, offset, bytes, 0, (int)len);
        return new String(bytes, charset);
    }


    // Reference implementation
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

}
