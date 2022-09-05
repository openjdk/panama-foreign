/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.SkipException;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/*
 * @test
 * @enablePreview
 * @summary Check HexFormat formatting and parsing
 * @run testng/othervm HexFormatTest
 */

@Test
public class HexFormatTest {
    static final Class<NullPointerException> NPE = NullPointerException.class;

    @DataProvider(name = "HexFormattersParsers")
    Object[][] hexFormattersParsers() {
        return new Object[][]{
                {"", "", "", true,
                        HexFormat.of().withUpperCase()},
                {", ", "#", "L", false,
                        HexFormat.ofDelimiter(", ").withPrefix("#").withSuffix("L")},
                {"", "", "", false,
                        HexFormat.of().withPrefix("").withSuffix("")},
                {".", "", "", false,
                        HexFormat.ofDelimiter(".").withPrefix("").withSuffix("")},
                {", ", "0x", "", true,
                        HexFormat.ofDelimiter(", ").withUpperCase().withPrefix("0x")},
                {"\u0202", "\u0203", "\u0204", false,
                        HexFormat.ofDelimiter("\u0202").withPrefix("\u0203").withSuffix("\u0204")},
                {"\u0202", "", "", false,
                        HexFormat.ofDelimiter("\u0202")},

        };
    }

    @DataProvider(name = "HexStringsThrowing")
    Object[][] HexStringsThrowing() {
        return new Object[][]{
                {"0", ":", "", ""},         // wrong string length
                {"01:", ":", "", ""},       // wrong string length
                {"01:0", ":", "", ""},      // wrong string length
                {"0", ",", "", ""},         // wrong length and separator
                {"01:", ",", "", ""},       // wrong length and separator
                {"01:0", ",", "", ""},      // wrong length and separator
                {"01:00", ",", "", ""},     // wrong separator
                {"00]", ",", "[", "]"},     // missing prefix
                {"[00", ",", "[", "]"},     // missing suffix
                {"]", ",", "[", "]"},       // missing prefix
                {"[", ",", "[", "]"},       // missing suffix
                {"00", ",", "abc", ""},     // Prefix longer than string
                {"01", ",", "", "def"},     // Suffix longer than string
                {"abc00,", ",", "abc", ""},     // Prefix and delim but not another value
                {"01def,", ",", "", "def"},     // Suffix and delim but not another value
        };
    }

    @DataProvider(name = "BadBytesThrowing")
    Object[][] badBytesThrowing() {
        return new Object[][]{
                {new byte[1], 0, 2},        // bad toIndex
                {new byte[1], 1, 2},        // bad fromIndex + toIndex
                {new byte[1], -1, 2},       // bad fromIndex
                {new byte[1], -1, 1},       // bad fromIndex
                {new byte[1], 0, -1},       // bad toIndex
                {new byte[1], 1, -1},       // bad toIndex
        };
    }

    @DataProvider(name = "BadParseHexThrowing")
    Object[][] badParseHexThrowing() {
        return new Object[][]{
                {"a", 0, 2, IndexOutOfBoundsException.class},        // bad toIndex
                {"b", 1, 2, IndexOutOfBoundsException.class},        // bad toIndex
                {"a", -1, 2, IndexOutOfBoundsException.class},       // bad fromIndex
                {"b", -1, 1, IndexOutOfBoundsException.class},       // bad fromIndex
                {"a", 0, -1, IndexOutOfBoundsException.class},       // bad toIndex
                {"b", 1, -1, IndexOutOfBoundsException.class},       // bad fromIndex + toIndex
                {"76543210", 0, 7, IllegalArgumentException.class},  // odd number of digits
                {"zz00", 0, 4, IllegalArgumentException.class},      // non-hex digits
                {"00zz", 0, 4, IllegalArgumentException.class},      // non-hex digits
        };
    }

    @DataProvider(name = "BadFromHexDigitsThrowing")
    Object[][] badHexDigitsThrowing() {
        return new Object[][]{
                {"a", 0, 2, IndexOutOfBoundsException.class},        // bad toIndex
                {"b", 1, 2, IndexOutOfBoundsException.class},        // bad fromIndex + toIndex
                {"a", -1, 2, IndexOutOfBoundsException.class},       // bad toIndex
                {"b", -1, 1, IndexOutOfBoundsException.class},       // bad fromIndex + toIndex
                {"a", 0, -1, IndexOutOfBoundsException.class},       // bad toIndex
                {"b", 1, -1, IndexOutOfBoundsException.class},       // bad fromIndex + toIndex
        };
    }

    static byte[] genBytes(int origin, int len) {
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++)
            bytes[i] = (byte) (origin + i);
        return bytes;
    }

    @Test
    static void testToHex() {
        HexFormat hex = HexFormat.of();
        for (int i = 0; i < 32; i++) {
            char c = hex.toLowHexDigit((byte)i);
            String expected = Integer.toHexString(i & 0xf);
            assertEquals(c, expected.charAt(0), "toHex formatting");
        }
    }

    @Test
    static void testToHexDigits() {
        HexFormat hex = HexFormat.of();
        for (int i = 0; i < 256; i++) {
            String actual = hex.toHexDigits((byte)i);
            int expected = HexFormat.fromHexDigits(actual);
            assertEquals(expected, i, "fromHexDigits");
            assertEquals(actual.charAt(0), hex.toHighHexDigit((byte)i),
                    "first char mismatch");
            assertEquals(actual.charAt(1), hex.toLowHexDigit((byte)i),
                    "second char mismatch");
        }
    }

    @Test
    static void testIsHexDigit() {
        for (int i = 0; i < 0x3ff; i++) {
            boolean actual = HexFormat.isHexDigit(i);
            boolean expected = Character.digit(i, 16) >= 0;
            assertEquals(actual, expected, "isHexDigit: " + i);
        }
    }

    @Test
    static void testFromHexDigit() {
        String chars = "0123456789ABCDEF0123456789abcdef";
        for (int i = 0; i < chars.length(); i++) {
            int v = HexFormat.fromHexDigit(chars.charAt(i));
            assertEquals(v, i & 0xf, "fromHex decode");
        }
    }

    @Test
    static void testFromHexInvalid() {
        for (int i = 0; i < 65536; i++) {
            char ch = (char)i;
            if (ch > 0xff || Character.digit(ch, 16) < 0) {
                assertFalse(HexFormat.isHexDigit(ch), "isHexDigit incorrect for '" + ch + "'  = " + i);
                expectThrows(NumberFormatException.class,
                        () -> HexFormat.fromHexDigit(ch));

            }
        }
    }

    @Test
    static void testAppendHexByteWithStringBuilder() {
        HexFormat hex = HexFormat.of();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            sb.setLength(0);
            StringBuilder sb1 = hex.toHexDigits(sb, (byte)i);
            assertSame(sb1, sb, "toHexDigits returned different StringBuilder");
            assertEquals(sb.length(), 2, "wrong length after append: " + i);
            assertEquals(sb.charAt(0), hex.toHighHexDigit((byte)i), "MSB converted wrong");
            assertEquals(sb.charAt(1), hex.toLowHexDigit((byte)i), "LSB converted wrong");

            assertEquals(HexFormat.fromHexDigits(sb), i, "hex.format(sb, byte) wrong");
        }
    }

    @Test
    static void testAppendHexByteWithCharBuffer() {
        HexFormat hex = HexFormat.of();
        CharBuffer cb = CharBuffer.allocate(256);
        for (int i = 1; i <= 128; i++) {
            CharBuffer cb1 = hex.toHexDigits(cb, (byte)i);
            assertTrue(cb1 == cb);
            assertEquals(cb.position(), i * 2);
        }
        assertEquals(cb.remaining(), 0);
    }

    @Test
    static void testAppendHexByteWithCharArrayWriter() {
        HexFormat hex = HexFormat.of();
        CharArrayWriter caw = new CharArrayWriter();
        for (int i = 1; i <= 128; i++) {
            CharArrayWriter caw1 = hex.toHexDigits(caw, (byte)i);
            assertTrue(caw1 == caw);
            assertEquals(caw.size(), i * 2);
        }
    }

    @Test
    static void testFromHexPairInvalid() {
        HexFormat hex = HexFormat.of();

        // An assortment of invalid characters
        String chars = "-0--0-";
        for (int i = 0; i < chars.length(); i += 2) {
            final int ndx = i;
            Throwable ex = expectThrows(NumberFormatException.class,
                    () -> HexFormat.fromHexDigits(chars.subSequence(ndx, ndx+2)));
            System.out.println(ex);
        }
    }

    @Test(dataProvider = "HexStringsThrowing")
    static void testToBytesThrowing(String value, String sep, String prefix, String suffix) {
        HexFormat hex = HexFormat.ofDelimiter(sep).withPrefix(prefix).withSuffix(suffix);
        Throwable ex = expectThrows(IllegalArgumentException.class,
                () -> {
                    byte[] v = hex.parseHex(value);
                    System.out.println("str: " + value + ", actual: " + v + ", bytes: " +
                                    Arrays.toString(v));
                });
        System.out.println("ex: " + ex);
    }

    @Test
    static void testFactoryNPE() {
        assertThrows(NPE, () -> HexFormat.ofDelimiter(null));
        assertThrows(NPE, () -> HexFormat.of().withDelimiter(null));
        assertThrows(NPE, () -> HexFormat.of().withPrefix(null));
        assertThrows(NPE, () -> HexFormat.of().withSuffix(null));
    }

    @Test
    static void testFormatHexNPE() {
        assertThrows(NPE, () -> HexFormat.of().formatHex((byte[])null));
        assertThrows(NPE, () -> HexFormat.of().formatHex((MemorySegment)null));
        assertThrows(NPE, () -> HexFormat.of().formatHex(null, 0, 1));
        assertThrows(NPE, () -> HexFormat.of().formatHex(null, (byte[])null));
        assertThrows(NPE, () -> HexFormat.of().formatHex(null, (MemorySegment) null));
        assertThrows(NPE,  () -> HexFormat.of().formatHex(null, null, 0, 0));
        StringBuilder sb = new StringBuilder();
        assertThrows(NPE, () -> HexFormat.of().formatHex(sb, (byte[])null));
        assertThrows(NPE, () -> HexFormat.of().formatHex(sb, (MemorySegment)null));
        assertThrows(NPE, () -> HexFormat.of().formatHex(sb, null, 0, 1));
    }

    @Test
    static void testParseHexNPE() {
        assertThrows(NPE, () -> HexFormat.of().parseHex(null));
        assertThrows(NPE, () -> HexFormat.of().parseHex((String)null, 0, 0));
        assertThrows(NPE, () -> HexFormat.of().parseHex((char[])null, 0, 0));
    }

    @Test
    static void testFromHexNPE() {
        assertThrows(NPE, () -> HexFormat.fromHexDigits(null));
        assertThrows(NPE, () -> HexFormat.fromHexDigits(null, 0, 0));
        assertThrows(NPE, () -> HexFormat.fromHexDigitsToLong(null));
        assertThrows(NPE, () -> HexFormat.fromHexDigitsToLong(null, 0, 0));
    }

    @Test
    static void testToHexDigitsNPE() {
        assertThrows(NPE, () -> HexFormat.of().toHexDigits(null, (byte)0));
    }

    @Test(dataProvider = "BadParseHexThrowing")
    static void badParseHex(String string, int offset, int length,
                            Class<? extends Throwable> exClass) {
        assertThrows(exClass,
                () -> HexFormat.of().parseHex(string, offset, length));
        char[] chars = string.toCharArray();
        assertThrows(exClass,
                () -> HexFormat.of().parseHex(chars, offset, length));
    }

    @Test(dataProvider = "BadFromHexDigitsThrowing")
    static void badFromHexDigits(String string, int fromIndex, int toIndex,
                           Class<? extends Throwable> exClass) {
        assertThrows(exClass,
                () -> HexFormat.fromHexDigits(string, fromIndex, toIndex));
        assertThrows(exClass,
                () -> HexFormat.fromHexDigitsToLong(string, fromIndex, toIndex));
    }

    // Verify IAE for strings that are too long for the target primitive type
    // or the number of requested digits is too large.
    @Test
    static void wrongNumberDigits() {
        assertThrows(IllegalArgumentException.class,
                () -> HexFormat.fromHexDigits("9876543210"));
        assertThrows(IllegalArgumentException.class,
                () -> HexFormat.fromHexDigits("9876543210", 0, 9));
        assertThrows(IllegalArgumentException.class,
                () -> HexFormat.fromHexDigitsToLong("98765432109876543210"));
        assertThrows(IllegalArgumentException.class,
                () -> HexFormat.fromHexDigitsToLong("98765432109876543210", 0, 17));
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatter(String delimiter, String prefix, String suffix,
                                   boolean uppercase,
                                   HexFormat hex) {
        byte[] expected = genBytes('A', 15);
        String res = hex.formatHex(expected);
        testFormatter(expected, res, delimiter, prefix, suffix, uppercase);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatterMemorySegment(String delimiter, String prefix, String suffix,
                              boolean uppercase,
                              HexFormat hex) {
        byte[] expected = genBytes('A', 15);
        var segment = MemorySegment.ofArray(expected);
        String res = hex.formatHex(segment);
        testFormatter(expected, res, delimiter, prefix, suffix, uppercase);
    }

    static void testFormatter(byte[] expected, String res,
                              String delimiter, String prefix, String suffix,
                              boolean uppercase) {
        assertTrue(res.startsWith(prefix), "Prefix not found");
        assertTrue(res.endsWith(suffix), "Suffix not found");
        int expectedLen = expected.length * (2 + prefix.length() +
                delimiter.length() + suffix.length()) - delimiter.length();
        assertEquals(res.length(), expectedLen, "String length");

        if (expected.length > 1) {
            // check prefix and suffix is present for each hex pair
            for (int i = 0; i < expected.length; i++) {
                int valueChars = prefix.length() + 2 + suffix.length();
                int offset = i * (valueChars + delimiter.length());
                String value = res.substring(offset, offset + valueChars);
                assertTrue(value.startsWith(prefix), "wrong prefix");
                assertTrue(value.endsWith(suffix), "wrong suffix");

                // Check case of digits
                String cc = value.substring(prefix.length(), prefix.length() + 2);
                assertEquals(cc,
                        (uppercase) ? cc.toUpperCase(Locale.ROOT) : cc.toLowerCase(Locale.ROOT),
                        "Case mismatch");
                if (i < expected.length - 1 && !delimiter.isEmpty()) {
                    // Check the delimiter is present for each pair except the last
                    assertEquals(res.substring(offset + valueChars,
                            offset + valueChars + delimiter.length()), delimiter);
                }
            }
        }
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatHexString(String unused1, String unused2, String unused3,
                                   boolean unused4, HexFormat hex) {
        byte[] expected = genBytes('A', 15);
        String s = hex.formatHex(expected);
        System.out.println("    formatted: " + s);

        byte[] actual = hex.parseHex(s);
        System.out.println("    parsed as: " + Arrays.toString(actual));
        int mismatch = Arrays.mismatch(expected, actual);
        assertEquals(actual, expected, "format/parse cycle failed, mismatch: " + mismatch);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testParseHexStringRange(String delimiter, String prefix, String suffix,
                                   boolean unused4, HexFormat hex) {
        byte[] expected = genBytes('A', 15);
        String s = hex.formatHex(expected);

        // Parse values 2, 3, 4 from the generated string
        int low = 2;
        int high = 5;
        int stride = prefix.length() + 2 + suffix.length() + delimiter.length();
        System.out.println("    formatted subrange: " +
                s.substring(low * stride, high * stride - delimiter.length()));
        byte[] actual = hex.parseHex(s, low * stride,
                high * stride - delimiter.length());
        System.out.println("    parsed as: " + Arrays.toString(actual));

        assertEquals(actual.length, (high - low), "array length");
        int mismatch = Arrays.mismatch(expected, low, high, actual, 0, high - low);
        assertEquals(mismatch, -1, "format/parse cycle failed, mismatch: " + mismatch);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testParseHexEmptyString(String delimiter, String prefix, String suffix,
                                        boolean unused4, HexFormat hex) {
        byte[] actual = hex.parseHex("");
        assertEquals(actual.length, 0, "empty string parse");
        actual = hex.parseHex("abc", 0, 0);
        assertEquals(actual.length, 0, "empty string range parse");
        actual = hex.parseHex(new char[1], 0, 0);
        assertEquals(actual.length, 0, "empty char array subrange empty parse");
    }

        @Test(dataProvider="HexFormattersParsers")
    static void testFormatHexRangeString(String unused1, String unused2, String unused3,
                                   boolean unused4, HexFormat hex) {
        byte[] expected = genBytes('A', 15);
        int low = 1;
        int high = expected.length - 2;
        String s = hex.formatHex(expected, low, high);
        System.out.println("    formatted: " + s);

        byte[] actual = hex.parseHex(s);
        System.out.println("    parsed as: " + Arrays.toString(actual));
        int mismatch = Arrays.mismatch(expected, low, high, actual, 0, high - low);
        assertEquals(mismatch, -1, "format/parse cycle failed, mismatch: " + mismatch);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatHexAppendable(String unused1, String unused2, String unused3,
                                     boolean unused4, HexFormat hex) {
        byte[] expected = genBytes('A', 15);
        StringBuilder sb = new StringBuilder();
        StringBuilder s = hex.formatHex(sb, expected);
        testFormatHexAppendable(expected, sb, s, hex);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatHexAppendableMemorySegment(String unused1, String unused2, String unused3,
                                     boolean unused4, HexFormat hex) {
        byte[] expected = genBytes('A', 15);
        var segment = MemorySegment.ofArray(expected);
        StringBuilder sb = new StringBuilder();
        StringBuilder s = hex.formatHex(sb, segment);
        testFormatHexAppendable(expected, sb, s, hex);
    }

    static void testFormatHexAppendable(byte[] expected, StringBuilder sb, StringBuilder s, HexFormat hex) {
        assertEquals(s, sb, "formatHex returned unknown StringBuilder");
        System.out.println("    formatted: " + s);

        byte[] actual = hex.parseHex(s.toString());
        System.out.println("    parsed as: " + Arrays.toString(actual));
        int mismatch = Arrays.mismatch(expected, actual);
        assertEquals(actual, expected, "format/parse cycle failed, mismatch: " + mismatch);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatHexRangeAppendable(String unused1, String unused2, String unused3,
                                     boolean unused4, HexFormat hex) {
        byte[] expected = genBytes('A', 15);
        int low = 1;
        int high = expected.length - 2;
        StringBuilder sb = new StringBuilder();
        StringBuilder s = hex.formatHex(sb, expected, low, high);
        assertEquals(s, sb, "formatHex returned unknown StringBuilder");
        System.out.println("    formatted: " + s);

        byte[] actual = hex.parseHex(s.toString());
        System.out.println("    parsed as: " + Arrays.toString(actual));
        byte[] sub = Arrays.copyOfRange(expected, low, high);
        System.out.println("actual: " + Arrays.toString(actual));
        System.out.println("sub   : " + Arrays.toString(sub));
        int mismatch = Arrays.mismatch(expected, low, high, actual, 0, high - low);

        assertEquals(actual, sub, "format/parse cycle failed, mismatch: " + mismatch);
        assertEquals(mismatch, -1, "format/parse cycle failed, mismatch: " + mismatch);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatHexCharArray(String unused1, String unused2, String unused3,
                                     boolean unused4, HexFormat hex) {
        byte[] expected = genBytes('A', 15);
        String s = hex.formatHex(expected);
        System.out.println("    formatted: " + s);

        char[] chars = s.toCharArray();
        byte[] actual = hex.parseHex(chars, 0, chars.length);
        System.out.println("    parsed as: " + Arrays.toString(actual));
        int mismatch = Arrays.mismatch(expected, actual);
        assertEquals(actual, expected, "format/parse cycle failed, mismatch: " + mismatch);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatHexCharArrayIndexed(String delimiter, String prefix, String suffix,
                                              boolean unused4, HexFormat hex) {
        byte[] expected = genBytes('A', 15);
        String s = hex.formatHex(expected);
        System.out.println("    formatted: " + s);


        // Parse values 2, 3, 4 from the generated string
        int low = 2;
        int high = 5;
        int stride = prefix.length() + 2 + suffix.length() + delimiter.length();
        System.out.println("    formatted subrange: " +
                s.substring(low * stride, high * stride - delimiter.length()));
        char[] chars = s.toCharArray();
        byte[] actual = hex.parseHex(chars, low * stride,
                high * stride - delimiter.length());
        System.out.println("    parsed as: " + Arrays.toString(actual));

        assertEquals(actual.length, (high - low), "array length");
        int mismatch = Arrays.mismatch(expected, low, high, actual, 0, high - low);
        assertEquals(mismatch, -1, "format/parse cycle failed, mismatch: " + mismatch);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatterToString(String delimiter, String prefix, String suffix,
                                    boolean uppercase,
                                    HexFormat hex) {
        String actual = String.format(
                "uppercase: %s, delimiter: \"%s\", prefix: \"%s\", suffix: \"%s\"",
                uppercase, escapeNL(delimiter), escapeNL(prefix), escapeNL(suffix));
        System.out.println("    hex: " + actual);
        assertEquals(actual, hex.toString(), "Formatter toString mismatch");
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatterParameterMethods(String delimiter, String prefix, String suffix,
                                    boolean uppercase,
                                    HexFormat hex) {
        assertEquals(hex.delimiter(), delimiter);
        assertEquals(hex.prefix(), prefix);
        assertEquals(hex.suffix(), suffix);
        assertEquals(hex.isUpperCase(), uppercase);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testFormatterTestEquals(String delimiter, String prefix, String suffix,
                                    boolean uppercase,
                                    HexFormat expected) {
        HexFormat actual = HexFormat.of()
                .withDelimiter(delimiter)
                .withPrefix(prefix)
                .withSuffix(suffix);
        actual = uppercase ? actual.withUpperCase() : actual.withLowerCase();

        assertEquals(actual.delimiter(), delimiter, "delimiter");
        assertEquals(actual.prefix(), prefix, "prefix");
        assertEquals(actual.suffix(), suffix, "suffix");
        assertEquals(actual.isUpperCase(), uppercase, "uppercase");
        assertTrue(actual.equals(expected), "equals method");
        assertEquals(actual.hashCode(), expected.hashCode(), "hashCode");

        assertTrue(actual.equals(actual));   // equals self
        assertFalse(actual.equals(null));    // never equals null
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testZeroLength(String delimiter, String prefix, String suffix, boolean uppercase,
                                HexFormat hex) {
        // Test formatting of zero length byte arrays, should produce no output
        StringBuilder sb = new StringBuilder();
        assertEquals(hex.formatHex(new byte[0]), "", "Zero length");
        assertEquals(hex.formatHex(new byte[0], 0, 0), "", "Zero length");

        hex.formatHex(sb, new byte[0]);
        assertEquals(sb.length(), 0, "length should not change");
        hex.formatHex(sb, new byte[0], 0, 0);
        assertEquals(sb.length(), 0, "length should not change");

    }

    @Test(dataProvider="HexFormattersParsers")
    static void testZeroLengthMemorySegment(String delimiter, String prefix, String suffix, boolean uppercase,
                                HexFormat hex) {
        // Test formatting of zero length byte arrays, should produce no output
        var segment = MemorySegment.ofArray(new byte[0]);
        StringBuilder sb = new StringBuilder();
        assertEquals(hex.formatHex(segment), "", "Zero length");

        hex.formatHex(sb, segment);
        assertEquals(sb.length(), 0, "length should not change");
    }
    private static String escapeNL(String string) {
        return string.replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    @Test
    static void testfromHexDigitsToInt() {
        HexFormat hex = HexFormat.of();

        String allHex = "76543210";
        final int orig = 0x76543210;
        for (int digits = 0; digits <= 8; digits++) {
            String s = hex.toHexDigits(orig, digits);
            long actual = HexFormat.fromHexDigits(s, 0, digits);
            System.out.printf("    digits: %2d, formatted: \"%s\", parsed as: 0x%08x%n",
                    digits, s, actual);
            assertEquals(s, allHex.substring(8 - digits, 8));
            long expected = (digits < 8) ? orig & ~(0xffffffff << (4 * digits)) : orig;
            assertEquals(actual, expected);
        }
    }

    @Test
    static void testfromHexDigitsToLong() {
        HexFormat hex = HexFormat.of();

        String allHex = "fedcba9876543210";
        final long orig = 0xfedcba9876543210L;
        for (int digits = 0; digits <= 16; digits++) {
            String s = hex.toHexDigits(orig, digits);
            long actual = HexFormat.fromHexDigitsToLong(s, 0, digits);
            System.out.printf("    digits: %2d, formatted: \"%s\", parsed as: 0x%016xL%n",
                    digits, s, actual);
            assertEquals(s, allHex.substring(16 - digits, 16));
            long expected = (digits < 16) ? orig & ~(0xffffffffffffffffL << (4 * digits)) : orig;
            assertEquals(actual, expected);
        }
    }

    @Test
    static void testToHexDigitsLong() {
        HexFormat hex = HexFormat.of();

        String allHex = "fedcba9876543210";
        final long expected = 0xfedcba9876543210L;
        String s = hex.toHexDigits(expected);
        long actual = HexFormat.fromHexDigitsToLong(s);
        System.out.printf("    formatted: \"%s\", parsed as: 0x%016xL%n", s, actual);
        assertEquals(s, allHex);
        assertEquals(actual, expected);
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testIOException(String delimiter, String prefix, String suffix, boolean uppercase,
                               HexFormat hex) {
        Appendable throwingAppendable = new ThrowingAppendable();
        assertThrows(UncheckedIOException.class,
                () -> hex.formatHex(throwingAppendable, new byte[1]));
        assertThrows(UncheckedIOException.class,
                () -> hex.formatHex(throwingAppendable, MemorySegment.ofArray(new byte[1])));
        assertThrows(UncheckedIOException.class,
                () -> hex.formatHex(throwingAppendable, new byte[1], 0, 1));
        assertThrows(UncheckedIOException.class,
                () -> hex.toHexDigits(throwingAppendable, (byte)1));
    }

    @Test(dataProvider="HexFormattersParsers")
    static void testOOME(String delimiter, String prefix, String suffix, boolean uppercase,
                         HexFormat hex) {
        // compute the size of byte array that will exceed the buffer
        long valueChars = prefix.length() + 2 + suffix.length();
        long stride = valueChars + delimiter.length();
        long max = Integer.MAX_VALUE & 0xFFFFFFFFL;
        long len = max / stride;
        long remainder = max - ((len - 1) * stride);
        if (remainder > valueChars) {
            len++;
            remainder -= valueChars;
        }
        try {
            byte[] bytes = new byte[(int) len];
            Throwable ex = expectThrows(OutOfMemoryError.class,
                    () -> hex.formatHex(bytes));
            System.out.println("ex: " + ex);
        } catch (OutOfMemoryError oome) {
            System.out.printf("OOME: total mem: %08x, free mem: %08x, max mem: %08x%n",
                    Runtime.getRuntime().totalMemory(),
                    Runtime.getRuntime().freeMemory(),
                    Runtime.getRuntime().maxMemory());
            throw new SkipException("Insufficient Memory to test OOME");
        }

    }

    /**
     * Example code from the HexFormat javadoc.
     * Showing simple usage of the API using "assert" to express the correct results
     * when shown in the javadoc.
     * The additional TestNG asserts verify the correctness of the same code.
     */
    @Test
    private static void samples() {
        {
            // Primitive formatting and parsing.
            HexFormat hex = HexFormat.of();

            byte b = 127;
            String byteStr = hex.toHexDigits(b);
            System.out.println("    " + byteStr);

            byte byteVal = (byte) HexFormat.fromHexDigits(byteStr);
            assert(byteStr.equals("7f"));
            assert(b == byteVal);
            assertTrue(byteStr.equals("7f"));
            assertTrue(b == byteVal);


            char c = 'A';
            String charStr = hex.toHexDigits(c);
            System.out.println("    " + charStr);
            int charVal = HexFormat.fromHexDigits(charStr);
            assert(c == charVal);
            assertTrue(c == charVal);

            int i = 12345;
            String intStr = hex.toHexDigits(i);
            System.out.println("    " + intStr);
            int intVal = HexFormat.fromHexDigits(intStr);
            assert(i == intVal);
            assertTrue(i == intVal);

            long l = Long.MAX_VALUE;
            String longStr = hex.toHexDigits(l, 16);
            long longVal = HexFormat.fromHexDigitsToLong(longStr, 0, 16);
            System.out.println("    " + longStr + ", " + longVal);
            assert(l == longVal);
            assertTrue(l == longVal);
        }

        {
            // RFC 4752 Fingerprint
            HexFormat formatFingerprint = HexFormat.ofDelimiter(":").withUpperCase();
            byte[] bytes = {0, 1, 2, 3, 124, 125, 126, 127};
            String str = formatFingerprint.formatHex(bytes);
            System.out.println("    Formatted: " + str);

            byte[] parsed = formatFingerprint.parseHex(str);
            System.out.println("    Parsed: " + Arrays.toString(parsed));
            assert(Arrays.equals(bytes, parsed));
            assertTrue(Arrays.equals(bytes, parsed));
        }

        {
            // Comma separated formatting
            HexFormat commaFormat = HexFormat.ofDelimiter(",");
            byte[] bytes = {0, 1, 2, 3, 124, 125, 126, 127};
            String str = commaFormat.formatHex(bytes);
            System.out.println("    Formatted: " + str);

            byte[] parsed = commaFormat.parseHex(str);
            System.out.println("    Parsed: " + Arrays.toString(parsed));
            assert(Arrays.equals(bytes, parsed));
            assertTrue(Arrays.equals(bytes, parsed));
        }
        {
            // Text formatting
            HexFormat commaFormat = HexFormat.ofDelimiter(", ").withPrefix("#");
            byte[] bytes = {0, 1, 2, 3, 124, 125, 126, 127};
            String str = commaFormat.formatHex(bytes);
            System.out.println("    Formatted: " + str);

            byte[] parsed = commaFormat.parseHex(str);
            System.out.println("    Parsed:    " + Arrays.toString(parsed));
            assert(Arrays.equals(bytes, parsed));
            assertTrue(Arrays.equals(bytes, parsed));
        }
    }


    private static final int HEX_SEGMENT_SIZE = 64 + 4;
    private static final String THE_QUICK = "The quick brown fox jumped over the lazy dog\nSecond line\t:here";

    private static final byte[] THE_QUICK_ARRAY = THE_QUICK.getBytes(StandardCharsets.UTF_8);
    private static final String EXPECTED_HEX = platformLineSeparated("""
            0000000000000000 54 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20 |The quick brown |
            0000000000000010 66 6f 78 20 6a 75 6d 70 65 64 20 6f 76 65 72 20 |fox jumped over |
            0000000000000020 74 68 65 20 6c 61 7a 79 20 64 6f 67 0a 53 65 63 |the lazy dog.Sec|
            0000000000000030 6f 6e 64 20 6c 69 6e 65 09 3a 68 65 72 65 00 00 |ond line.:here..|
            0000000000000040 00 00 00 00                                     |....            |""");

    private static final String EXPECTED_HEX_SPECIAL = platformLineSeparated("""
            0000000000000000  54 68 65 20 71 75 69 63  6b 20 62 72 6f 77 6e 20  |The quick brown |
            0000000000000010  66 6f 78 20 6a 75 6d 70  65 64 20 6f 76 65 72 20  |fox jumped over |
            0000000000000020  74 68 65 20 6c 61 7a 79  20 64 6f 67 0a 53 65 63  |the lazy dog.Sec|
            0000000000000030  6f 6e 64 20 6c 69 6e 65  09 3a 68 65 72 65 00 00  |ond line.:here..|
            0000000000000040  00 00 00 00                                       |....            |""");

    private static final String EXPECT_ADDRESS = "0x" + "00".repeat((int) ValueLayout.ADDRESS.byteSize());

    @Test
    public void testDump64BitIndexToConsole() {
        System.out.println("***");
        try (var session = MemorySession.openConfined()) {
            var segment = session.allocateUtf8String(THE_QUICK);
            HexFormat.MemoryDumper dumper = HexFormat.MemoryDumper.builder()
                    .addIndexColumn()
                    .build();

            System.out.println(dumper);

            dumper.dump(segment)
                    .forEach(System.out::println);
        }
    }

    @Test
    public void testDump64BitIndex() {

        var expect = """
                0000000000000000
                0000000000000010
                0000000000000020
                0000000000000030
                0000000000000040""";

        var actual = testWithFreshMemorySegment(HEX_SEGMENT_SIZE, segment -> {
            segment.setUtf8String(0, THE_QUICK);
            return HexFormat.MemoryDumper.builder()
                    .addIndexColumn()
                    .build()
                    .dump(segment)
                    .collect(joining(System.lineSeparator()));
        });
        assertEquals(actual, expect);
    }

    @Test
    public void testDump32BitIndex() {

        var expect = """
                00000000
                00000010
                00000020
                00000030
                00000040""";

        var actual = testWithFreshMemorySegment(HEX_SEGMENT_SIZE, segment -> {
            segment.setUtf8String(0, THE_QUICK);
            return HexFormat.MemoryDumper.builder()
                    .addIndexColumn(Integer.BYTES)
                    .build()
                    .dump(segment)
                    .collect(joining(System.lineSeparator()));
        });
        assertEquals(actual, expect);
    }

    @Test
    public void testDump8BitIndex() {

        var expect = """
                00
                10
                20
                30
                40""";

        var actual = testWithFreshMemorySegment(HEX_SEGMENT_SIZE, segment -> {
            segment.setUtf8String(0, THE_QUICK);
            return HexFormat.MemoryDumper.builder()
                    .addIndexColumn(1)
                    .build()
                    .dump(segment)
                    .collect(joining(System.lineSeparator()));
        });
        assertEquals(actual, expect);
    }

    @Test
    public void testHexStreamOneByte() {

        var expect = platformLineSeparated("0000000000000000 41 00                                           |A.              |");

        var actual = testWithFreshMemorySegment(2, segment -> {
            segment.setUtf8String(0, "A");
            return HexFormat.MemoryDumper.builder()
                    .addIndexColumn()
                    .addDataColumn()
                    .withColumnPrefix("|")
                    .withColumnSuffix("|")
                    .addDataColumn(HexFormat.MemoryDumper.Builder.ColumnRenderer.ofAscii())
                    .build()
                    .dump(segment)
                    .collect(joining(System.lineSeparator()));
        });
        assertEquals(actual, expect);
    }

    @Test
    public void testHexStreamOneByteWithStrangeFormatting() {

        var expect = platformLineSeparated("0x00--,0x00--,0x00--,0x00--,0x00--,0x00--,0x00--,0x00-- 0x41--,0x00--,                                                                                                  |A.              |");

        var actual = testWithFreshMemorySegment(2, segment -> {
            segment.setUtf8String(0, "A");
            var formatter = HexFormat.ofDelimiter(",").withPrefix("0x").withSuffix("--");
            return HexFormat.MemoryDumper.builder()
                    .addIndexColumn(8, formatter)
                    .addDataColumn(formatter)
                    .withColumnPrefix("|")
                    .withColumnSuffix("|")
                    .addDataColumn(HexFormat.MemoryDumper.Builder.ColumnRenderer.ofAscii())
                    .build()
                    .dump(segment)
                    .collect(joining(System.lineSeparator()));
        });
        assertEquals(actual, expect);
    }

    @Test
    public void testHexStream() {

        var actual = testWithFreshMemorySegment(HEX_SEGMENT_SIZE, segment -> {
            segment.setUtf8String(0, THE_QUICK);
            return HexFormat.MemoryDumper.builder()
                    .addIndexColumn()
                    .addDataColumn()
                    .withColumnPrefix("|")
                    .withColumnSuffix("|")
                    .addDataColumn(HexFormat.MemoryDumper.Builder.ColumnRenderer.ofAscii())
                    .build()
                    .dump(segment)
                    .collect(joining(System.lineSeparator()));
        });
        assertEquals(actual, EXPECTED_HEX);
    }

    @Test
    public void testEmptyMemoryDump() {
        var actual = testWithFreshMemorySegment(0, segment ->
                HexFormat.MemoryDumper.builder()
                        .addIndexColumn()
                        .build()
                        .dump(segment)
                        .collect(joining())
        );
        assertEquals(actual, "");
    }

    @Test
    public void testEmptyDumper() {
        var actual = testWithFreshMemorySegment(10, segment ->
                HexFormat.MemoryDumper.builder()
                        .build()
                        .dump(segment)
                        .collect(joining())
        );
        assertEquals(actual, "");
    }

    @Test
    public void test256HexDump() {
        var expect = platformLineSeparated("""
                0000000000000000 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
                0000000000000010 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f |................|
                0000000000000020 20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f | !"#$%&'()*+,-./|
                0000000000000030 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f |0123456789:;<=>?|
                0000000000000040 40 41 42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f |@ABCDEFGHIJKLMNO|
                0000000000000050 50 51 52 53 54 55 56 57 58 59 5a 5b 5c 5d 5e 5f |PQRSTUVWXYZ[\\]^_|
                0000000000000060 60 61 62 63 64 65 66 67 68 69 6a 6b 6c 6d 6e 6f |`abcdefghijklmno|
                0000000000000070 70 71 72 73 74 75 76 77 78 79 7a 7b 7c 7d 7e 7f |pqrstuvwxyz{|}~.|
                0000000000000080 80 81 82 83 84 85 86 87 88 89 8a 8b 8c 8d 8e 8f |................|
                0000000000000090 90 91 92 93 94 95 96 97 98 99 9a 9b 9c 9d 9e 9f |................|
                00000000000000a0 a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 aa ab ac ad ae af |................|
                00000000000000b0 b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 ba bb bc bd be bf |................|
                00000000000000c0 c0 c1 c2 c3 c4 c5 c6 c7 c8 c9 ca cb cc cd ce cf |................|
                00000000000000d0 d0 d1 d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df |................|
                00000000000000e0 e0 e1 e2 e3 e4 e5 e6 e7 e8 e9 ea eb ec ed ee ef |................|
                00000000000000f0 f0 f1 f2 f3 f4 f5 f6 f7 f8 f9 fa fb fc fd fe ff |................|""");

        try (var session = MemorySession.openConfined()) {
            var segment = session.allocate(256);
            for (int i = 0; i < segment.byteSize(); i++) {
                segment.set(ValueLayout.JAVA_BYTE, i, (byte) i);
            }
            var actual = HexFormat.MemoryDumper.builder()
                    .addIndexColumn()
                    .addDataColumn()
                    .withColumnPrefix("|")
                    .withColumnSuffix("|")
                    .addDataColumn(HexFormat.MemoryDumper.Builder.ColumnRenderer.ofAscii())
                    .build()
                    .dump(segment)
                    .collect(joining(System.lineSeparator()));
            assertEquals(actual, expect);
        }
    }

    @Test
    public void test4kHexDump() {
        try (var session = MemorySession.openConfined()) {
            var segment = session.allocate(2048);
            for (int i = 0; i < segment.byteSize(); i++) {
                segment.set(ValueLayout.JAVA_BYTE, i, (byte) i);
            }
            var stat = HexFormat.MemoryDumper.builder()
                    .addIndexColumn()
                    .addDataColumn()
                    .withColumnPrefix("|")
                    .withColumnSuffix("|")
                    .addDataColumn(HexFormat.MemoryDumper.Builder.ColumnRenderer.ofAscii())
                    .build()
                    .dump(segment)
                    .mapToInt(String::length)
                    .summaryStatistics();

            // Every row is of equal length
            assertEquals(stat.getMax(), stat.getMin());

        }
    }

    @Test
    public void testStandardDump() {
        var actual = testWithFreshMemorySegment(HEX_SEGMENT_SIZE, segment -> {
            segment.setUtf8String(0, THE_QUICK);
            return HexFormat.MemoryDumper.standard()
                    .dump(segment)
                    .collect(joining(System.lineSeparator()));
        });
        assertEquals(actual, EXPECTED_HEX);
    }

    @Test
    public void testStandardDumpWithArray() {
        var expect = """
                0000000000000000 54 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20 |The quick brown |
                0000000000000010 66 6f 78 20 6a 75 6d 70 65 64 20 6f 76 65 72 20 |fox jumped over |
                0000000000000020 74 68 65 20 6c 61 7a 79 20 64 6f 67 0a 53 65 63 |the lazy dog.Sec|
                0000000000000030 6f 6e 64 20 6c 69 6e 65 09 3a 68 65 72 65       |ond line.:here  |""";
        var actual = HexFormat.MemoryDumper.standard()
                    .dump(THE_QUICK_ARRAY)
                    .collect(joining(System.lineSeparator()));

        assertEquals(actual, expect);
    }

    @Test
    public void testStandardDumpWithArrayFromToEndpoints() {
        var expect = """
                0000000000000000 54 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20 |The quick brown |
                0000000000000010 66 6f 78 20 6a 75 6d 70 65 64 20 6f 76 65 72 20 |fox jumped over |
                0000000000000020 74 68 65 20 6c 61 7a 79 20 64 6f 67 0a 53 65 63 |the lazy dog.Sec|
                0000000000000030 6f 6e 64 20 6c 69 6e 65 09 3a 68 65 72 65       |ond line.:here  |""";
        var actual = HexFormat.MemoryDumper.standard()
                .dump(THE_QUICK_ARRAY, 0, THE_QUICK_ARRAY.length)
                .collect(joining(System.lineSeparator()));

        assertEquals(actual, expect);
    }

    @Test
    public void testStandardDumpWithArrayFrom0To13() {
        var expect = """
                0000000000000000 54 68 65 20 71 75 69 63 6b 20 62 72 6f          |The quick bro   |""";
        var actual = HexFormat.MemoryDumper.standard()
                .dump(THE_QUICK_ARRAY, 0, 13)
                .collect(joining(System.lineSeparator()));

        assertEquals(actual, expect);
    }

    @Test
    public void testStandardDumpWithArrayFrom2To9() {
        var expect = """
                0000000000000000 65 20 71 75 69 63 6b                            |e quick         |""";
        var actual = HexFormat.MemoryDumper.standard()
                .dump(THE_QUICK_ARRAY, 2, 9)
                .collect(joining(System.lineSeparator()));

        assertEquals(actual, expect);
    }

    @Test
    public void testStandardDumpByteBuffer() {
        var expect = """
                0000000000000000 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20 66 |he quick brown f|
                0000000000000010 6f 78 20 6a 75 6d 70 65 64 20 6f 76 65 72 20 74 |ox jumped over t|
                0000000000000020 68 65 20 6c 61 7a 79 20 64 6f 67 0a 53 65 63 6f |he lazy dog.Seco|
                0000000000000030 6e 64 20 6c 69 6e 65 09 3a 68 65 72 65          |nd line.:here   |""";
        var buffer = ByteBuffer.wrap(THE_QUICK_ARRAY);
        // Consume a byte
        buffer.get();
        var actual = HexFormat.MemoryDumper.standard()
                .dump(buffer)
                .collect(joining(System.lineSeparator()));

        assertEquals(actual, expect);
    }

    public void testSeveralColumns() {
        try (var session = MemorySession.openConfined()) {
            var segment = session.allocateUtf8String(THE_QUICK);
            var expect = """
+0000000000000000+ -00000000- /54 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20/ *54 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20* 0000
+0000000000000010+ -00000010- /66 6f 78 20 6a 75 6d 70 65 64 20 6f 76 65 72 20/ *66 6f 78 20 6a 75 6d 70 65 64 20 6f 76 65 72 20* 0010
+0000000000000020+ -00000020- /74 68 65 20 6c 61 7a 79 20 64 6f 67 0a 53 65 63/ *74 68 65 20 6c 61 7a 79 20 64 6f 67 0a 53 65 63* 0020
+0000000000000030+ -00000030- /6f 6e 64 20 6c 69 6e 65 09 3a 68 65 72 65 00   / *6f 6e 64 20 6c 69 6e 65 09 3a 68 65 72 65 00   * 0030""";
            var actual = HexFormat.MemoryDumper.builder()
                    .withColumnPrefix("+")
                    .withColumnSuffix("+")
                    .addIndexColumn()
                    .withColumnPrefix("-")
                    .withColumnSuffix("-")
                    .addIndexColumn(4)
                    .withColumnPrefix("/")
                    .withColumnSuffix("/")
                    .addDataColumn()
                    .withColumnPrefix("*")
                    .withColumnSuffix("*")
                    .addDataColumn()
                    .withColumnPrefix("")
                    .withColumnSuffix("")
                    .addIndexColumn(2)
                    .build()
                    .dump(segment)
                    .collect(joining(System.lineSeparator()));
            assertEquals(actual, expect);
        }
    }

    private static String platformLineSeparated(String s) {
        return s.lines()
                .collect(joining(System.lineSeparator()));
    }

    private static <T> T testWithFreshMemorySegment(long size,
                                                    Function<MemorySegment, T> mapper) {
        try (final MemorySession session = MemorySession.openConfined()) {
            var segment = session.allocate(size);
            return mapper.apply(segment);
        }
    }

    /**
     * A test implementation of Appendable that throws IOException on all methods.
     */
    static class ThrowingAppendable implements Appendable {
        @Override
        public Appendable append(CharSequence csq) throws IOException {
            throw new IOException(".append(CharSequence) always throws");
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            throw new IOException(".append(CharSequence, start, end) always throws");
        }

        @Override
        public Appendable append(char c) throws IOException {
            throw new IOException(".append(char) always throws");
        }
    }
}
