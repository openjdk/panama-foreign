/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.function.ToLongFunction;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.util.Objects.requireNonNull;

/**
 * {@code HexFormat} converts between bytes and chars and hex-encoded strings which may include
 * additional formatting markup such as prefixes, suffixes, and delimiters.
 * <p>
 * There are two factories of {@code HexFormat} with preset parameters {@link #of()} and
 * {@link #ofDelimiter(String) ofDelimiter(delimiter)}. For other parameter combinations
 * the {@code withXXX} methods return copies of {@code HexFormat} modified
 * {@link #withPrefix(String)}, {@link #withSuffix(String)}, {@link #withDelimiter(String)}
 * or choice of {@link #withUpperCase()} or {@link #withLowerCase()} parameters.
 * <p>
 * For primitive to hexadecimal string conversions the {@code toHexDigits}
 * methods include {@link #toHexDigits(byte)}, {@link #toHexDigits(int)}, and
 * {@link #toHexDigits(long)}, etc. The default is to use lowercase characters {@code "0-9","a-f"}.
 * For conversions producing uppercase hexadecimal the characters are {@code "0-9","A-F"}.
 * Only the {@link HexFormat#isUpperCase() HexFormat.isUpperCase()} parameter is
 * considered; the delimiter, prefix and suffix are not used.
 *
 * <p>
 * For hexadecimal string to primitive conversions the {@code fromHexDigits}
 * methods include {@link #fromHexDigits(CharSequence) fromHexDigits(string)},
 * {@link #fromHexDigitsToLong(CharSequence) fromHexDigitsToLong(string)}, and
 * {@link #fromHexDigit(int) fromHexDigit(int)} converts a single character or codepoint.
 * For conversions from hexadecimal characters the digits and uppercase and lowercase
 * characters in {@code "0-9", "a-f", and "A-F"} are converted to corresponding values
 * {@code 0-15}. The delimiter, prefix, suffix, and uppercase parameters are not used.
 *
 * <p>
 * For byte array to formatted hexadecimal string conversions
 * the {@code formatHex} methods include {@link #formatHex(byte[]) formatHex(byte[])}
 * and {@link #formatHex(Appendable, byte[]) formatHex(Appendable, byte[])}.
 * The formatted output is a string or is appended to an {@link Appendable} such as
 * {@link StringBuilder} or {@link java.io.PrintStream}.
 * Each byte value is formatted as the prefix, two hexadecimal characters from the
 * uppercase or lowercase digits, and the suffix.
 * A delimiter follows each formatted value, except the last.
 * For conversions producing uppercase hexadecimal strings use {@link #withUpperCase()}.
 *
 * <p>
 * For formatted hexadecimal string to byte array conversions the
 * {@code parseHex} methods include {@link #parseHex(CharSequence) parseHex(CharSequence)} and
 * {@link #parseHex(char[], int, int) parseHex(char[], offset, length)}.
 * Each byte value is parsed from the prefix, two case insensitive hexadecimal characters,
 * and the suffix. A delimiter follows each formatted value, except the last.
 *
 * @apiNote For example, an individual byte is converted to a string of hexadecimal digits using
 * {@link HexFormat#toHexDigits(int) toHexDigits(int)} and converted from a string to a
 * primitive value using {@link HexFormat#fromHexDigits(CharSequence) fromHexDigits(string)}.
 * <pre>{@code
 *     HexFormat hex = HexFormat.of();
 *     byte b = 127;
 *     String byteStr = hex.toHexDigits(b);
 *
 *     byte byteVal = (byte)hex.fromHexDigits(byteStr);
 *     assert(byteStr.equals("7f"));
 *     assert(b == byteVal);
 *
 *     // The hexadecimal digits are: "7f"
 * }</pre>
 * <p>
 * For a comma ({@code ", "}) separated format with a prefix ({@code "#"})
 * using lowercase hex digits the {@code HexFormat} is:
 * <pre>{@code
 *     HexFormat commaFormat = HexFormat.ofDelimiter(", ").withPrefix("#");
 *     byte[] bytes = {0, 1, 2, 3, 124, 125, 126, 127};
 *     String str = commaFormat.formatHex(bytes);
 *
 *     byte[] parsed = commaFormat.parseHex(str);
 *     assert(Arrays.equals(bytes, parsed));
 *
 *     // The formatted string is: "#00, #01, #02, #03, #7c, #7d, #7e, #7f"
 * }</pre>
 * <p>
 * For a fingerprint of byte values that uses the delimiter colon ({@code ":"})
 * and uppercase characters the {@code HexFormat} is:
 * <pre>{@code
 *     HexFormat formatFingerprint = HexFormat.ofDelimiter(":").withUpperCase();
 *     byte[] bytes = {0, 1, 2, 3, 124, 125, 126, 127};
 *     String str = formatFingerprint.formatHex(bytes);
 *     byte[] parsed = formatFingerprint.parseHex(str);
 *     assert(Arrays.equals(bytes, parsed));
 *
 *     // The formatted string is: "00:01:02:03:7C:7D:7E:7F"
 * }</pre>
 *
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code HexFormat} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 * <p>
 * This class is immutable and thread-safe.
 * <p>
 * Unless otherwise noted, passing a null argument to any method will cause a
 * {@link java.lang.NullPointerException NullPointerException} to be thrown.
 * @since 17
 */


public final class HexFormat {

    // Access to create strings from a byte array.
    private static final JavaLangAccess jla = SharedSecrets.getJavaLangAccess();

    private static final byte[] UPPERCASE_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
    };
    private static final byte[] LOWERCASE_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
    };
    // Analysis has shown that generating the whole array allows the JIT to generate
    // better code compared to a slimmed down array, such as one cutting off after 'f'
    private static final byte[] DIGITS = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    };
    /**
     * Format each byte of an array as a pair of hexadecimal digits.
     * The hexadecimal characters are from lowercase alpha digits.
     */
    private static final HexFormat HEX_FORMAT =
            new HexFormat("", "", "", LOWERCASE_DIGITS);

    private static final byte[] EMPTY_BYTES = {};

    private static final int DUMP_BYTES_PER_ROW = 16;

    private final String delimiter;
    private final String prefix;
    private final String suffix;
    private final byte[] digits;

    /**
     * Returns a HexFormat with a delimiter, prefix, suffix, and array of digits.
     *
     * @param delimiter a delimiter, non-null
     * @param prefix    a prefix, non-null
     * @param suffix    a suffix, non-null
     * @param digits    byte array of digits indexed by low nibble, non-null
     * @throws NullPointerException if any argument is null
     */
    private HexFormat(String delimiter, String prefix, String suffix, byte[] digits) {
        this.delimiter = Objects.requireNonNull(delimiter, "delimiter");
        this.prefix = Objects.requireNonNull(prefix, "prefix");
        this.suffix = Objects.requireNonNull(suffix, "suffix");
        this.digits = digits;
    }

    /**
     * Returns a hexadecimal formatter with no delimiter and lowercase characters.
     * The delimiter, prefix, and suffix are empty.
     * The methods {@link #withDelimiter(String) withDelimiter},
     * {@link #withUpperCase() withUpperCase}, {@link #withLowerCase() withLowerCase},
     * {@link #withPrefix(String) withPrefix}, and {@link #withSuffix(String) withSuffix}
     * return copies of formatters with new parameters.
     *
     * @return a hexadecimal formatter with no delimiter and lowercase characters
     */
    public static HexFormat of() {
        return HEX_FORMAT;
    }

    /**
     * Returns a hexadecimal formatter with the delimiter and lowercase characters.
     * The prefix and suffix are empty.
     * The methods {@link #withDelimiter(String) withDelimiter},
     * {@link #withUpperCase() withUpperCase}, {@link #withLowerCase() withLowerCase},
     * {@link #withPrefix(String) withPrefix}, and {@link #withSuffix(String) withSuffix}
     * return copies of formatters with new parameters.
     *
     * @param delimiter a delimiter, non-null, may be empty
     * @return a {@link HexFormat} with the delimiter and lowercase characters
     */
    public static HexFormat ofDelimiter(String delimiter) {
        return new HexFormat(delimiter, "", "", LOWERCASE_DIGITS);
    }

    /**
     * Returns a copy of this {@code HexFormat} with the delimiter.
     *
     * @param delimiter the delimiter, non-null, may be empty
     * @return a copy of this {@code HexFormat} with the delimiter
     */
    public HexFormat withDelimiter(String delimiter) {
        return new HexFormat(delimiter, this.prefix, this.suffix, this.digits);
    }

    /**
     * Returns a copy of this {@code HexFormat} with the prefix.
     *
     * @param prefix a prefix, non-null, may be empty
     * @return a copy of this {@code HexFormat} with the prefix
     */
    public HexFormat withPrefix(String prefix) {
        return new HexFormat(this.delimiter, prefix, this.suffix, this.digits);
    }

    /**
     * Returns a copy of this {@code HexFormat} with the suffix.
     *
     * @param suffix a suffix, non-null, may be empty
     * @return a copy of this {@code HexFormat} with the suffix
     */
    public HexFormat withSuffix(String suffix) {
        return new HexFormat(this.delimiter, this.prefix, suffix, this.digits);
    }

    /**
     * Returns a copy of this {@code HexFormat} to use uppercase hexadecimal characters.
     * The uppercase hexadecimal characters are {@code "0-9", "A-F"}.
     *
     * @return a copy of this {@code HexFormat} with uppercase hexadecimal characters
     */
    public HexFormat withUpperCase() {
        return new HexFormat(this.delimiter, this.prefix, this.suffix, UPPERCASE_DIGITS);
    }

    /**
     * Returns a copy of this {@code HexFormat} to use lowercase hexadecimal characters.
     * The lowercase hexadecimal characters are {@code "0-9", "a-f"}.
     *
     * @return a copy of this {@code HexFormat} with lowercase hexadecimal characters
     */
    public HexFormat withLowerCase() {
        return new HexFormat(this.delimiter, this.prefix, this.suffix, LOWERCASE_DIGITS);
    }

    /**
     * Returns the delimiter between hexadecimal values in formatted hexadecimal strings.
     *
     * @return the delimiter, non-null, may be empty {@code ""}
     */
    public String delimiter() {
        return delimiter;
    }

    /**
     * Returns the prefix used for each hexadecimal value in formatted hexadecimal strings.
     *
     * @return the prefix, non-null, may be empty {@code ""}
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Returns the suffix used for each hexadecimal value in formatted hexadecimal strings.
     *
     * @return the suffix, non-null, may be empty {@code ""}
     */
    public String suffix() {
        return suffix;
    }

    /**
     * Returns {@code true} if the hexadecimal digits are uppercase,
     * otherwise {@code false}.
     *
     * @return {@code true} if the hexadecimal digits are uppercase,
     * otherwise {@code false}
     */
    public boolean isUpperCase() {
        return Arrays.equals(digits, UPPERCASE_DIGITS);
    }

    /**
     * Returns a hexadecimal string formatted from a byte array.
     * Each byte value is formatted as the prefix, two hexadecimal characters
     * {@linkplain #isUpperCase selected from} uppercase or lowercase digits, and the suffix.
     * A delimiter follows each formatted value, except the last.
     * <p>
     * The behavior is equivalent to
     * {@link #formatHex(byte[], int, int) formatHex(bytes, 0, bytes.length))}.
     *
     * @param bytes a non-null array of bytes
     * @return a string hexadecimal formatting of the byte array
     */
    public String formatHex(byte[] bytes) {
        return formatHex(bytes, 0, bytes.length);
    }

    /**
     * Returns a hexadecimal string formatted from a byte array range.
     * Each byte value is formatted as the prefix, two hexadecimal characters
     * {@linkplain #isUpperCase selected from} uppercase or lowercase digits, and the suffix.
     * A delimiter follows each formatted value, except the last.
     *
     * @param bytes     a non-null array of bytes
     * @param fromIndex the initial index of the range, inclusive
     * @param toIndex   the final index of the range, exclusive
     * @return a string hexadecimal formatting each byte of the array range
     * @throws IndexOutOfBoundsException if the array range is out of bounds
     */
    public String formatHex(byte[] bytes, int fromIndex, int toIndex) {
        Objects.requireNonNull(bytes, "bytes");
        Objects.checkFromToIndex(fromIndex, toIndex, bytes.length);
        if (toIndex - fromIndex == 0) {
            return "";
        }
        // Format efficiently if possible
        String s = formatOptDelimiter(bytes, fromIndex, toIndex);
        if (s == null) {
            long stride = prefix.length() + 2L + suffix.length() + delimiter.length();
            int capacity = checkMaxArraySize((toIndex - fromIndex) * stride - delimiter.length());
            StringBuilder sb = new StringBuilder(capacity);
            formatHex(sb, bytes, fromIndex, toIndex);
            s = sb.toString();
        }
        return s;
    }

    /**
     * Appends formatted hexadecimal strings from a byte array to the {@link Appendable}.
     * Each byte value is formatted as the prefix, two hexadecimal characters
     * {@linkplain #isUpperCase selected from} uppercase or lowercase digits, and the suffix.
     * A delimiter follows each formatted value, except the last.
     * The formatted hexadecimal strings are appended in zero or more calls to the {@link Appendable} methods.
     *
     * @param <A>   The type of {@code Appendable}
     * @param out   an {@code Appendable}, non-null
     * @param bytes a byte array
     * @return the {@code Appendable}
     * @throws UncheckedIOException if an I/O exception occurs appending to the output
     */
    public <A extends Appendable> A formatHex(A out, byte[] bytes) {
        return formatHex(out, bytes, 0, bytes.length);
    }

    /**
     * Returns a hexadecimal string formatted from the provided {@linkplain MemorySegment segment}.
     * Each byte value is formatted as the prefix, two hexadecimal characters
     * {@linkplain #isUpperCase selected from} uppercase or lowercase digits, and the suffix.
     * A delimiter follows each formatted value, except the last.
     * <p>
     * The behavior is equivalent to
     * {@link #formatHex(Appendable, MemorySegment) formatHex(new StringBuilder(), segment).toString())}.
     * <p>
     * To view parts of a memory segment, use {@linkplain MemorySegment#asSlice(long) a slice}.
     *
     * @param segment a non-null memory segment
     * @return a string hexadecimal formatting of the byte array
     * @throws UncheckedIOException  if an I/O exception occurs appending to the output
     * @throws IllegalStateException if the {@linkplain MemorySegment#session() session} associated with this
     *                               segment is not {@linkplain MemorySession#isAlive() alive}.
     * @throws WrongThreadException  if this method is called from a thread other than the thread owning
     *                               the {@linkplain MemorySegment#session() session} associated with this segment.
     * @see #formatHex(Appendable, MemorySegment)
     */
    public String formatHex(MemorySegment segment) {
        return formatHex(new StringBuilder(), segment).toString();
    }

    /**
     * Appends formatted hexadecimal strings from the provided {@linkplain MemorySegment segment}
     * to the provided {@linkplain Appendable out}.
     * Each byte value is formatted as the prefix, two hexadecimal characters
     * {@linkplain #isUpperCase selected from} uppercase or lowercase digits, and the suffix.
     * A delimiter follows each formatted value, except the last.
     * The formatted hexadecimal strings are appended in zero or more calls to the {@link Appendable} methods.
     * <p>
     * To view parts of a memory segment, use {@linkplain MemorySegment#asSlice(long) a slice}.
     *
     * @param <A>     The type of {@code Appendable}
     * @param out     an {@code Appendable}, non-null
     * @param segment a memory segment
     * @return the {@code Appendable}
     * @throws UncheckedIOException  if an I/O exception occurs appending to the output
     * @throws IllegalStateException if the {@linkplain MemorySegment#session() session} associated with this
     *                               segment is not {@linkplain MemorySession#isAlive() alive}.
     * @throws WrongThreadException  if this method is called from a thread other than the thread owning
     *                               the {@linkplain MemorySegment#session() session} associated with this segment.
     */
    public <A extends Appendable> A formatHex(A out, MemorySegment segment) {
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(segment, "segment");

        long length = segment.byteSize();
        if (length > 0) {
            try {
                String between = suffix + delimiter + prefix;
                out.append(prefix);
                toHexDigits(out, segment.get(ValueLayout.JAVA_BYTE, 0));
                if (between.isEmpty()) {
                    for (long i = 1; i < length; i++) {
                        toHexDigits(out, segment.get(ValueLayout.JAVA_BYTE, i));
                    }
                } else {
                    for (long i = 1; i < length; i++) {
                        out.append(between);
                        toHexDigits(out, segment.get(ValueLayout.JAVA_BYTE, i));
                    }
                }
                out.append(suffix);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe.getMessage(), ioe);
            }
        }
        return out;
    }

    /**
     * Appends formatted hexadecimal strings from a byte array range to the {@link Appendable}.
     * Each byte value is formatted as the prefix, two hexadecimal characters
     * {@linkplain #isUpperCase selected from} uppercase or lowercase digits, and the suffix.
     * A delimiter follows each formatted value, except the last.
     * The formatted hexadecimal strings are appended in zero or more calls to the {@link Appendable} methods.
     *
     * @param <A>       The type of {@code Appendable}
     * @param out       an {@code Appendable}, non-null
     * @param bytes     a byte array, non-null
     * @param fromIndex the initial index of the range, inclusive
     * @param toIndex   the final index of the range, exclusive.
     * @return the {@code Appendable}
     * @throws IndexOutOfBoundsException if the array range is out of bounds
     * @throws UncheckedIOException      if an I/O exception occurs appending to the output
     */
    public <A extends Appendable> A formatHex(A out, byte[] bytes, int fromIndex, int toIndex) {
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(bytes, "bytes");
        Objects.checkFromToIndex(fromIndex, toIndex, bytes.length);

        int length = toIndex - fromIndex;
        if (length > 0) {
            try {
                String between = suffix + delimiter + prefix;
                out.append(prefix);
                toHexDigits(out, bytes[fromIndex]);
                if (between.isEmpty()) {
                    for (int i = 1; i < length; i++) {
                        toHexDigits(out, bytes[fromIndex + i]);
                    }
                } else {
                    for (int i = 1; i < length; i++) {
                        out.append(between);
                        toHexDigits(out, bytes[fromIndex + i]);
                    }
                }
                out.append(suffix);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe.getMessage(), ioe);
            }
        }
        return out;
    }

    /**
     * Returns a string formatting of the range of bytes optimized
     * for a single allocation.
     * Prefix and suffix must be empty and the delimiter
     * must be empty or a single byte character, otherwise null is returned.
     *
     * @param bytes     the bytes, non-null
     * @param fromIndex the initial index of the range, inclusive
     * @param toIndex   the final index of the range, exclusive.
     * @return a String formatted or null for non-single byte delimiter
     * or non-empty prefix or suffix
     */
    private String formatOptDelimiter(byte[] bytes, int fromIndex, int toIndex) {
        byte[] rep;
        if (!prefix.isEmpty() || !suffix.isEmpty()) {
            return null;
        }
        int length = toIndex - fromIndex;
        if (delimiter.isEmpty()) {
            // Allocate the byte array and fill in the hex pairs for each byte
            rep = new byte[checkMaxArraySize(length * 2L)];
            for (int i = 0; i < length; i++) {
                rep[i * 2] = (byte) toHighHexDigit(bytes[fromIndex + i]);
                rep[i * 2 + 1] = (byte) toLowHexDigit(bytes[fromIndex + i]);
            }
        } else if (delimiter.length() == 1 && delimiter.charAt(0) < 256) {
            // Allocate the byte array and fill in the characters for the first byte
            // Then insert the delimiter and hexadecimal characters for each of the remaining bytes
            char sep = delimiter.charAt(0);
            rep = new byte[checkMaxArraySize(length * 3L - 1L)];
            rep[0] = (byte) toHighHexDigit(bytes[fromIndex]);
            rep[1] = (byte) toLowHexDigit(bytes[fromIndex]);
            for (int i = 1; i < length; i++) {
                rep[i * 3 - 1] = (byte) sep;
                rep[i * 3] = (byte) toHighHexDigit(bytes[fromIndex + i]);
                rep[i * 3 + 1] = (byte) toLowHexDigit(bytes[fromIndex + i]);
            }
        } else {
            // Delimiter formatting not to a single byte
            return null;
        }
        try {
            // Return a new string using the bytes without making a copy
            return jla.newStringNoRepl(rep, StandardCharsets.ISO_8859_1);
        } catch (CharacterCodingException cce) {
            throw new AssertionError(cce);
        }
    }

    /**
     * Checked that the requested size for the result string is
     * less than or equal to the max array size.
     *
     * @param length the requested size of a byte array.
     * @return the length
     * @throws OutOfMemoryError if the size is larger than Integer.MAX_VALUE
     */
    private static int checkMaxArraySize(long length) {
        if (length > Integer.MAX_VALUE)
            throw new OutOfMemoryError("String size " + length +
                    " exceeds maximum " + Integer.MAX_VALUE);
        return (int) length;
    }

    /**
     * Returns a byte array containing hexadecimal values parsed from the string.
     * <p>
     * Each byte value is parsed from the prefix, two case insensitive hexadecimal characters,
     * and the suffix. A delimiter follows each formatted value, except the last.
     * The delimiters, prefixes, and suffixes strings must be present; they may be empty strings.
     * A valid string consists only of the above format.
     *
     * @param string a string containing the byte values with prefix, hexadecimal digits, suffix,
     *               and delimiters
     * @return a byte array with the values parsed from the string
     * @throws IllegalArgumentException if the prefix or suffix is not present for each byte value,
     *                                  the byte values are not hexadecimal characters, or if the delimiter is not present
     *                                  after all but the last byte value
     */
    public byte[] parseHex(CharSequence string) {
        return parseHex(string, 0, string.length());
    }

    /**
     * Returns a byte array containing hexadecimal values parsed from a range of the string.
     * <p>
     * Each byte value is parsed from the prefix, two case insensitive hexadecimal characters,
     * and the suffix. A delimiter follows each formatted value, except the last.
     * The delimiters, prefixes, and suffixes strings must be present; they may be empty strings.
     * A valid string consists only of the above format.
     *
     * @param string    a string range containing hexadecimal digits,
     *                  delimiters, prefix, and suffix.
     * @param fromIndex the initial index of the range, inclusive
     * @param toIndex   the final index of the range, exclusive.
     * @return a byte array with the values parsed from the string range
     * @throws IllegalArgumentException  if the prefix or suffix is not present for each byte value,
     *                                   the byte values are not hexadecimal characters, or if the delimiter is not present
     *                                   after all but the last byte value
     * @throws IndexOutOfBoundsException if the string range is out of bounds
     */
    public byte[] parseHex(CharSequence string, int fromIndex, int toIndex) {
        Objects.requireNonNull(string, "string");
        Objects.checkFromToIndex(fromIndex, toIndex, string.length());

        if (fromIndex != 0 || toIndex != string.length()) {
            string = string.subSequence(fromIndex, toIndex);
        }

        if (string.isEmpty())
            return EMPTY_BYTES;
        if (delimiter.isEmpty() && prefix.isEmpty() && suffix.isEmpty())
            return parseNoDelimiter(string);

        // avoid overflow for max length prefix or suffix
        long valueChars = prefix.length() + 2L + suffix.length();
        long stride = valueChars + delimiter.length();
        if ((string.length() - valueChars) % stride != 0)
            throw new IllegalArgumentException("extra or missing delimiters " +
                    "or values consisting of prefix, two hexadecimal digits, and suffix");

        checkLiteral(string, 0, prefix);
        checkLiteral(string, string.length() - suffix.length(), suffix);
        String between = suffix + delimiter + prefix;
        final int len = (int) ((string.length() - valueChars) / stride + 1L);
        byte[] bytes = new byte[len];
        int i, offset;
        for (i = 0, offset = prefix.length(); i < len - 1; i++, offset += 2 + between.length()) {
            bytes[i] = (byte) fromHexDigits(string, offset);
            checkLiteral(string, offset + 2, between);
        }
        bytes[i] = (byte) fromHexDigits(string, offset);

        return bytes;
    }

    /**
     * Returns a byte array containing hexadecimal values parsed from
     * a range of the character array.
     * <p>
     * Each byte value is parsed from the prefix, two case insensitive hexadecimal characters,
     * and the suffix. A delimiter follows each formatted value, except the last.
     * The delimiters, prefixes, and suffixes strings must be present; they may be empty strings.
     * A valid character array range consists only of the above format.
     *
     * @param chars     a character array range containing an even number of hexadecimal digits,
     *                  delimiters, prefix, and suffix.
     * @param fromIndex the initial index of the range, inclusive
     * @param toIndex   the final index of the range, exclusive.
     * @return a byte array with the values parsed from the character array range
     * @throws IllegalArgumentException  if the prefix or suffix is not present for each byte value,
     *                                   the byte values are not hexadecimal characters, or if the delimiter is not present
     *                                   after all but the last byte value
     * @throws IndexOutOfBoundsException if the character array range is out of bounds
     */
    public byte[] parseHex(char[] chars, int fromIndex, int toIndex) {
        Objects.requireNonNull(chars, "chars");
        Objects.checkFromToIndex(fromIndex, toIndex, chars.length);
        CharBuffer cb = CharBuffer.wrap(chars, fromIndex, toIndex - fromIndex);
        return parseHex(cb);
    }

    /**
     * Compare the literal and throw an exception if it does not match.
     * Pre-condition:  {@code index + literal.length() <= string.length()}.
     *
     * @param string  a CharSequence
     * @param index   the index of the literal in the CharSequence
     * @param literal the expected literal
     * @throws IllegalArgumentException if the literal is not present
     */
    private static void checkLiteral(CharSequence string, int index, String literal) {
        assert index <= string.length() - literal.length() : "pre-checked invariant error";
        if (literal.isEmpty() ||
                (literal.length() == 1 && literal.charAt(0) == string.charAt(index))) {
            return;
        }
        for (int i = 0; i < literal.length(); i++) {
            if (string.charAt(index + i) != literal.charAt(i)) {
                throw new IllegalArgumentException(escapeNL("found: \"" +
                        string.subSequence(index, index + literal.length()) +
                        "\", expected: \"" + literal + "\", index: " + index +
                        " ch: " + (int) string.charAt(index + i)));
            }
        }
    }

    /**
     * Expands new line characters to escaped newlines for display.
     *
     * @param string a string
     * @return a string with newline characters escaped
     */
    private static String escapeNL(String string) {
        return string.replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Returns the hexadecimal character for the low 4 bits of the value considering it to be a byte.
     * If the parameter {@link #isUpperCase()} is {@code true} the
     * character returned for values {@code 10-15} is uppercase {@code "A-F"},
     * otherwise the character returned is lowercase {@code "a-f"}.
     * The values in the range {@code 0-9} are returned as {@code "0-9"}.
     *
     * @param value a value, only the low 4 bits {@code 0-3} of the value are used
     * @return the hexadecimal character for the low 4 bits {@code 0-3} of the value
     */
    public char toLowHexDigit(int value) {
        return (char) digits[value & 0xf];
    }

    /**
     * Returns the hexadecimal character for the high 4 bits of the value considering it to be a byte.
     * If the parameter {@link #isUpperCase()} is {@code true} the
     * character returned for values {@code 10-15} is uppercase {@code "A-F"},
     * otherwise the character returned is lowercase {@code "a-f"}.
     * The values in the range {@code 0-9} are returned as {@code "0-9"}.
     *
     * @param value a value, only bits {@code 4-7} of the value are used
     * @return the hexadecimal character for the bits {@code 4-7} of the value
     */
    public char toHighHexDigit(int value) {
        return (char) digits[(value >> 4) & 0xf];
    }

    /**
     * Appends two hexadecimal characters for the byte value to the {@link Appendable}.
     * Each nibble (4 bits) from most significant to least significant of the value
     * is formatted as if by {@link #toLowHexDigit(int) toLowHexDigit(nibble)}.
     * The hexadecimal characters are appended in one or more calls to the
     * {@link Appendable} methods. The delimiter, prefix and suffix are not used.
     *
     * @param <A>   The type of {@code Appendable}
     * @param out   an {@code Appendable}, non-null
     * @param value a byte value
     * @return the {@code Appendable}
     * @throws UncheckedIOException if an I/O exception occurs appending to the output
     */
    public <A extends Appendable> A toHexDigits(A out, byte value) {
        Objects.requireNonNull(out, "out");
        try {
            out.append(toHighHexDigit(value));
            out.append(toLowHexDigit(value));
            return out;
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Returns the two hexadecimal characters for the {@code byte} value.
     * Each nibble (4 bits) from most significant to least significant of the value
     * is formatted as if by {@link #toLowHexDigit(int) toLowHexDigit(nibble)}.
     * The delimiter, prefix and suffix are not used.
     *
     * @param value a byte value
     * @return the two hexadecimal characters for the byte value
     */
    public String toHexDigits(byte value) {
        byte[] rep = new byte[2];
        rep[0] = (byte) toHighHexDigit(value);
        rep[1] = (byte) toLowHexDigit(value);
        try {
            return jla.newStringNoRepl(rep, StandardCharsets.ISO_8859_1);
        } catch (CharacterCodingException cce) {
            throw new AssertionError(cce);
        }
    }

    /**
     * Returns the four hexadecimal characters for the {@code char} value.
     * Each nibble (4 bits) from most significant to least significant of the value
     * is formatted as if by {@link #toLowHexDigit(int) toLowHexDigit(nibble)}.
     * The delimiter, prefix and suffix are not used.
     *
     * @param value a {@code char} value
     * @return the four hexadecimal characters for the {@code char} value
     */
    public String toHexDigits(char value) {
        return toHexDigits((short) value);
    }

    /**
     * Returns the four hexadecimal characters for the {@code short} value.
     * Each nibble (4 bits) from most significant to least significant of the value
     * is formatted as if by {@link #toLowHexDigit(int) toLowHexDigit(nibble)}.
     * The delimiter, prefix and suffix are not used.
     *
     * @param value a {@code short} value
     * @return the four hexadecimal characters for the {@code short} value
     */
    public String toHexDigits(short value) {
        byte[] rep = new byte[4];
        rep[0] = (byte) toHighHexDigit((byte) (value >> 8));
        rep[1] = (byte) toLowHexDigit((byte) (value >> 8));
        rep[2] = (byte) toHighHexDigit((byte) value);
        rep[3] = (byte) toLowHexDigit((byte) value);

        try {
            return jla.newStringNoRepl(rep, StandardCharsets.ISO_8859_1);
        } catch (CharacterCodingException cce) {
            throw new AssertionError(cce);
        }
    }

    /**
     * Returns the eight hexadecimal characters for the {@code int} value.
     * Each nibble (4 bits) from most significant to least significant of the value
     * is formatted as if by {@link #toLowHexDigit(int) toLowHexDigit(nibble)}.
     * The delimiter, prefix and suffix are not used.
     *
     * @param value an {@code int} value
     * @return the eight hexadecimal characters for the {@code int} value
     * @see Integer#toHexString
     */
    public String toHexDigits(int value) {
        byte[] rep = new byte[8];
        rep[0] = (byte) toHighHexDigit((byte) (value >> 24));
        rep[1] = (byte) toLowHexDigit((byte) (value >> 24));
        rep[2] = (byte) toHighHexDigit((byte) (value >> 16));
        rep[3] = (byte) toLowHexDigit((byte) (value >> 16));
        rep[4] = (byte) toHighHexDigit((byte) (value >> 8));
        rep[5] = (byte) toLowHexDigit((byte) (value >> 8));
        rep[6] = (byte) toHighHexDigit((byte) value);
        rep[7] = (byte) toLowHexDigit((byte) value);

        try {
            return jla.newStringNoRepl(rep, StandardCharsets.ISO_8859_1);
        } catch (CharacterCodingException cce) {
            throw new AssertionError(cce);
        }
    }

    /**
     * Returns the sixteen hexadecimal characters for the {@code long} value.
     * Each nibble (4 bits) from most significant to least significant of the value
     * is formatted as if by {@link #toLowHexDigit(int) toLowHexDigit(nibble)}.
     * The delimiter, prefix and suffix are not used.
     *
     * @param value a {@code long} value
     * @return the sixteen hexadecimal characters for the {@code long} value
     * @see Long#toHexString
     */
    public String toHexDigits(long value) {
        byte[] rep = new byte[16];
        rep[0] = (byte) toHighHexDigit((byte) (value >>> 56));
        rep[1] = (byte) toLowHexDigit((byte) (value >>> 56));
        rep[2] = (byte) toHighHexDigit((byte) (value >>> 48));
        rep[3] = (byte) toLowHexDigit((byte) (value >>> 48));
        rep[4] = (byte) toHighHexDigit((byte) (value >>> 40));
        rep[5] = (byte) toLowHexDigit((byte) (value >>> 40));
        rep[6] = (byte) toHighHexDigit((byte) (value >>> 32));
        rep[7] = (byte) toLowHexDigit((byte) (value >>> 32));
        rep[8] = (byte) toHighHexDigit((byte) (value >>> 24));
        rep[9] = (byte) toLowHexDigit((byte) (value >>> 24));
        rep[10] = (byte) toHighHexDigit((byte) (value >>> 16));
        rep[11] = (byte) toLowHexDigit((byte) (value >>> 16));
        rep[12] = (byte) toHighHexDigit((byte) (value >>> 8));
        rep[13] = (byte) toLowHexDigit((byte) (value >>> 8));
        rep[14] = (byte) toHighHexDigit((byte) value);
        rep[15] = (byte) toLowHexDigit((byte) value);

        try {
            return jla.newStringNoRepl(rep, StandardCharsets.ISO_8859_1);
        } catch (CharacterCodingException cce) {
            throw new AssertionError(cce);
        }
    }

    /**
     * Returns up to sixteen hexadecimal characters for the {@code long} value.
     * Each nibble (4 bits) from most significant to least significant of the value
     * is formatted as if by {@link #toLowHexDigit(int) toLowHexDigit(nibble)}.
     * The delimiter, prefix and suffix are not used.
     *
     * @param value  a {@code long} value
     * @param digits the number of hexadecimal digits to return, 0 to 16
     * @return the hexadecimal characters for the {@code long} value
     * @throws IllegalArgumentException if {@code digits} is negative or greater than 16
     */
    public String toHexDigits(long value, int digits) {
        if (digits < 0 || digits > 16)
            throw new IllegalArgumentException("number of digits: " + digits);
        if (digits == 0)
            return "";
        byte[] rep = new byte[digits];
        for (int i = rep.length - 1; i >= 0; i--) {
            rep[i] = (byte) toLowHexDigit((byte) (value));
            value = value >>> 4;
        }
        try {
            return jla.newStringNoRepl(rep, StandardCharsets.ISO_8859_1);
        } catch (CharacterCodingException cce) {
            throw new AssertionError(cce);
        }
    }

    /**
     * Returns a byte array containing the parsed hex digits.
     * A valid string consists only of an even number of hex digits.
     *
     * @param string a string containing an even number of only hex digits
     * @return a byte array
     * @throws IllegalArgumentException if the string length is not valid or
     *                                  the string contains non-hexadecimal characters
     */
    private static byte[] parseNoDelimiter(CharSequence string) {
        if ((string.length() & 1) != 0)
            throw new IllegalArgumentException("string length not even: " +
                    string.length());

        byte[] bytes = new byte[string.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) fromHexDigits(string, i * 2);
        }

        return bytes;
    }

    /**
     * Check the number of requested digits against a limit.
     *
     * @param fromIndex the initial index of the range, inclusive
     * @param toIndex   the final index of the range, exclusive.
     * @param limit     the maximum allowed
     * @return the length of the range
     */
    private static int checkDigitCount(int fromIndex, int toIndex, int limit) {
        int length = toIndex - fromIndex;
        if (length > limit)
            throw new IllegalArgumentException("string length greater than " +
                    limit + ": " + length);
        return length;
    }

    /**
     * Returns {@code true} if the character is a valid hexadecimal character or codepoint.
     * The valid hexadecimal characters are:
     * <ul>
     * <li>{@code '0' ('\u005Cu0030')} through {@code '9' ('\u005Cu0039')} inclusive,
     * <li>{@code 'A' ('\u005Cu0041')} through {@code 'F' ('\u005Cu0046')} inclusive, and
     * <li>{@code 'a' ('\u005Cu0061')} through {@code 'f' ('\u005Cu0066')} inclusive.
     * </ul>
     *
     * @param ch a codepoint
     * @return {@code true} if the character is valid a hexadecimal character,
     * otherwise {@code false}
     */
    public static boolean isHexDigit(int ch) {
        return ((ch >>> 8) == 0 && DIGITS[ch] >= 0);
    }

    /**
     * Returns the value for the hexadecimal character or codepoint.
     * The value is:
     * <ul>
     * <li>{@code (ch - '0')} for {@code '0'} through {@code '9'} inclusive,
     * <li>{@code (ch - 'A' + 10)} for {@code 'A'} through {@code 'F'} inclusive, and
     * <li>{@code (ch - 'a' + 10)} for {@code 'a'} through {@code 'f'} inclusive.
     * </ul>
     *
     * @param ch a character or codepoint
     * @return the value {@code 0-15}
     * @throws NumberFormatException if the codepoint is not a hexadecimal character
     */
    public static int fromHexDigit(int ch) {
        int value;
        if ((ch >>> 8) == 0 && (value = DIGITS[ch]) >= 0) {
            return value;
        }
        throw new NumberFormatException("not a hexadecimal digit: \"" + (char) ch + "\" = " + ch);
    }

    /**
     * Returns a value parsed from two hexadecimal characters in a string.
     * The characters in the range from {@code index} to {@code index + 1},
     * inclusive, must be valid hex digits according to {@link #fromHexDigit(int)}.
     *
     * @param string a CharSequence containing the characters
     * @param index  the index of the first character of the range
     * @return the value parsed from the string range
     * @throws NumberFormatException     if any of the characters in the range
     *                                   is not a hexadecimal character
     * @throws IndexOutOfBoundsException if the range is out of bounds
     *                                   for the {@code CharSequence}
     */
    private static int fromHexDigits(CharSequence string, int index) {
        int high = fromHexDigit(string.charAt(index));
        int low = fromHexDigit(string.charAt(index + 1));
        return (high << 4) | low;
    }

    /**
     * Returns the {@code int} value parsed from a string of up to eight hexadecimal characters.
     * The hexadecimal characters are parsed from most significant to least significant
     * using {@link #fromHexDigit(int)} to form an unsigned value.
     * The value is zero extended to 32 bits and is returned as an {@code int}.
     *
     * @param string to parse
     * @return the parsed int value
     * @apiNote {@link Integer#parseInt(String, int) Integer.parseInt(s, 16)} and
     * {@link Integer#parseUnsignedInt(String, int) Integer.parseUnsignedInt(s, 16)}
     * are similar but allow all Unicode hexadecimal digits defined by
     * {@link Character#digit(char, int) Character.digit(ch, 16)}.
     * {@code HexFormat} uses only hexadecimal characters
     * {@code "0-9"}, {@code "A-F"} and {@code "a-f"}.
     * Signed hexadecimal strings can be parsed with {@link Integer#parseInt(String, int)}.
     */
    public static int fromHexDigits(CharSequence string) {
        return fromHexDigits(string, 0, string.length());
    }

    /**
     * Returns the {@code int} value parsed from a string range of up to eight hexadecimal
     * characters.
     * The characters in the range {@code fromIndex} to {@code toIndex}, exclusive,
     * are parsed from most significant to least significant
     * using {@link #fromHexDigit(int)} to form an unsigned value.
     * The value is zero extended to 32 bits and is returned as an {@code int}.
     *
     * @param string    a CharSequence containing the characters
     * @param fromIndex the initial index of the range, inclusive
     * @param toIndex   the final index of the range, exclusive.
     * @return the value parsed from the string range
     * @throws IndexOutOfBoundsException if the range is out of bounds
     *                                   for the {@code CharSequence}
     * @throws IllegalArgumentException  if length of the range is greater than eight (8) or
     *                                   if any of the characters is not a hexadecimal character
     * @apiNote {@link Integer#parseInt(String, int) Integer.parseInt(s, 16)} and
     * {@link Integer#parseUnsignedInt(String, int) Integer.parseUnsignedInt(s, 16)}
     * are similar but allow all Unicode hexadecimal digits defined by
     * {@link Character#digit(char, int) Character.digit(ch, 16)}.
     * {@code HexFormat} uses only hexadecimal characters
     * {@code "0-9"}, {@code "A-F"} and {@code "a-f"}.
     * Signed hexadecimal strings can be parsed with {@link Integer#parseInt(String, int)}.
     */
    public static int fromHexDigits(CharSequence string, int fromIndex, int toIndex) {
        Objects.requireNonNull(string, "string");
        Objects.checkFromToIndex(fromIndex, toIndex, string.length());
        int length = checkDigitCount(fromIndex, toIndex, 8);
        int value = 0;
        for (int i = 0; i < length; i++) {
            value = (value << 4) + fromHexDigit(string.charAt(fromIndex + i));
        }
        return value;
    }

    /**
     * Returns the long value parsed from a string of up to sixteen hexadecimal characters.
     * The hexadecimal characters are parsed from most significant to least significant
     * using {@link #fromHexDigit(int)} to form an unsigned value.
     * The value is zero extended to 64 bits and is returned as a {@code long}.
     *
     * @param string to parse
     * @return the parsed long value
     * @apiNote {@link Long#parseLong(String, int) Long.parseLong(s, 16)} and
     * {@link Long#parseUnsignedLong(String, int) Long.parseUnsignedLong(s, 16)}
     * are similar but allow all Unicode hexadecimal digits defined by
     * {@link Character#digit(char, int) Character.digit(ch, 16)}.
     * {@code HexFormat} uses only hexadecimal characters
     * {@code "0-9"}, {@code "A-F"} and {@code "a-f"}.
     * Signed hexadecimal strings can be parsed with {@link Long#parseLong(String, int)}.
     */
    public static long fromHexDigitsToLong(CharSequence string) {
        return fromHexDigitsToLong(string, 0, string.length());
    }

    /**
     * Returns the long value parsed from a string range of up to sixteen hexadecimal
     * characters.
     * The characters in the range {@code fromIndex} to {@code toIndex}, exclusive,
     * are parsed from most significant to least significant
     * using {@link #fromHexDigit(int)} to form an unsigned value.
     * The value is zero extended to 64 bits and is returned as a {@code long}.
     *
     * @param string    a CharSequence containing the characters
     * @param fromIndex the initial index of the range, inclusive
     * @param toIndex   the final index of the range, exclusive.
     * @return the value parsed from the string range
     * @throws IndexOutOfBoundsException if the range is out of bounds
     *                                   for the {@code CharSequence}
     * @throws IllegalArgumentException  if the length of the range is greater than sixteen (16) or
     *                                   if any of the characters is not a hexadecimal character
     * @apiNote {@link Long#parseLong(String, int) Long.parseLong(s, 16)} and
     * {@link Long#parseUnsignedLong(String, int) Long.parseUnsignedLong(s, 16)}
     * are similar but allow all Unicode hexadecimal digits defined by
     * {@link Character#digit(char, int) Character.digit(ch, 16)}.
     * {@code HexFormat} uses only hexadecimal characters
     * {@code "0-9"}, {@code "A-F"} and {@code "a-f"}.
     * Signed hexadecimal strings can be parsed with {@link Long#parseLong(String, int)}.
     */
    public static long fromHexDigitsToLong(CharSequence string, int fromIndex, int toIndex) {
        Objects.requireNonNull(string, "string");
        Objects.checkFromToIndex(fromIndex, toIndex, string.length());
        int length = checkDigitCount(fromIndex, toIndex, 16);
        long value = 0L;
        for (int i = 0; i < length; i++) {
            value = (value << 4) + fromHexDigit(string.charAt(fromIndex + i));
        }
        return value;
    }

    /**
     * Returns {@code true} if the other object is a {@code HexFormat}
     * with the same parameters.
     *
     * @param o an object, may be null
     * @return {@code true} if the other object is a {@code HexFormat} and the parameters
     * uppercase, delimiter, prefix, and suffix are equal;
     * otherwise {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HexFormat otherHex = (HexFormat) o;
        return Arrays.equals(digits, otherHex.digits) &&
                delimiter.equals(otherHex.delimiter) &&
                prefix.equals(otherHex.prefix) &&
                suffix.equals(otherHex.suffix);
    }

    /**
     * Returns a hashcode for this {@code HexFormat}.
     *
     * @return a hashcode for this {@code HexFormat}
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(delimiter, prefix, suffix);
        result = 31 * result + Boolean.hashCode(Arrays.equals(digits, UPPERCASE_DIGITS));
        return result;
    }

    /**
     * Returns a description of the formatter parameters for uppercase,
     * delimiter, prefix, and suffix.
     *
     * @return a description of this {@code HexFormat}
     */
    @Override
    public String toString() {
        return escapeNL("uppercase: " + Arrays.equals(digits, UPPERCASE_DIGITS) +
                ", delimiter: \"" + delimiter +
                "\", prefix: \"" + prefix +
                "\", suffix: \"" + suffix + "\"");
    }

    /**
     * A class allowing various memory abstractions (such as MemorySegments and byte arrays) to be dumped into
     * various formats.
     *
     * @author Per Minborg
     * @since 20
     */
    public interface MemoryDumper {

        /**
         * Returns a Stream of string elements with values for the provided {@code bytes} array.
         * <p>
         * Each element in the stream depends on how this MemoryDumper was configured via its {@linkplain #builder()}.
         * <p>
         * As an example, an array created, initialized and used as follows
         * {@snippet lang = java:
         *   byte[] bytes = "The quick brown fox jumped over the lazy dog\nSecond line\t:here".getBytes(StandardCharsets.UTF_8);
         *
         *   MemoryDumper dumper = MemoryDumper.builder()
         *      .addIndexColumn()
         *      .addDataColumn()
         *      .withColumnPrefix("|")
         *      .withColumnSuffix("|")
         *      .addDataColumn(Builder.ColumnRenderer.ofAscii())
         *      .build();
         *
         *   dumper.dump(bytes)
         *      .forEach(System.out::println);
         *}
         * will be printed as:
         * {@snippet lang = text:
         * 0000000000000000 54 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20 |The quick brown |
         * 0000000000000010 66 6f 78 20 6a 75 6d 70 65 64 20 6f 76 65 72 20 |fox jumped over |
         * 0000000000000020 74 68 65 20 6c 61 7a 79 20 64 6f 67 0a 53 65 63 |the lazy dog.Sec|
         * 0000000000000030 6f 6e 64 20 6c 69 6e 65 09 3a 68 65 72 65 00    |ond line.:here._|
         *}
         *
         * @param bytes to dump
         * @return a Stream of string elements
         */
        Stream<String> dump(byte[] bytes);

        /**
         * Returns a Stream of string elements with values for the provided {@code bytes} array staring
         * at the provided {@code fromIndex} (inclusive) and ending at the provided {@code toIndex} (exclusive).
         * <p>
         * Each element in the stream depends on how this MemoryDumper was configured via its {@linkplain #builder()}.
         * <p>
         * As an example, an array created, initialized and used as follows
         * {@snippet lang = java:
         *   byte[] bytes = "The quick brown fox jumped over the lazy dog\nSecond line\t:here".getBytes(StandardCharsets.UTF_8);
         *
         *   MemoryDumper dumper = MemoryDumper.builder()
         *      .addIndexColumn()
         *      .addDataColumn()
         *      .withColumnPrefix("|")
         *      .withColumnSuffix("|")
         *      .addDataColumn(Builder.ColumnRenderer.ofAscii())
         *      .build();
         *
         *   dumper.dump(bytes, 0, 16)
         *      .forEach(System.out::println);
         *}
         * will be printed as:
         * {@snippet lang = text:
         * 0000000000000000 54 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20 |The quick brown |
         *}
         *
         * @param bytes     to dump
         * @param fromIndex the initial index of the range, inclusive
         * @param toIndex   the final index of the range, exclusive.
         * @return a Stream of string elements
         */
        Stream<String> dump(byte[] bytes, int fromIndex, int toIndex);

        /**
         * Returns a Stream of string elements with values for the provided {@code memory} segment.
         * <p>
         * Each element in the stream depends on how this MemoryDumper was configured via its {@linkplain #builder()}.
         * <p>
         * As an example, an array created, initialized and used as follows
         * {@snippet lang = java:
         * MemoryDumper dumper = MemoryDumper.builder()
         *            .addIndexColumn()
         *            .addDataColumn()
         *            .withColumnPrefix("|")
         *            .withColumnSuffix("|")
         *            .addDataColumn(Builder.ColumnRenderer.ofAscii())
         *            .build();
         *
         * try (MemorySession session = MemorySession.openConfined()) {
         *     MemorySegment segment = session.allocateUtf8String("The quick brown fox jumped over the lazy dog\nSecond line\t:here");
         *     dumper.dump(bytes)
         *         .forEach(System.out::println);
         * }
         *}
         * will be printed as:
         * {@snippet lang = text:
         * 0000000000000000 54 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20 |The quick brown |
         * 0000000000000010 66 6f 78 20 6a 75 6d 70 65 64 20 6f 76 65 72 20 |fox jumped over |
         * 0000000000000020 74 68 65 20 6c 61 7a 79 20 64 6f 67 0a 53 65 63 |the lazy dog.Sec|
         * 0000000000000030 6f 6e 64 20 6c 69 6e 65 09 3a 68 65 72 65 00    |ond line.:here._|
         *}
         *
         * @param segment to dump
         * @return a Stream of string elements
         */
        Stream<String> dump(MemorySegment segment);

        /**
         * Returns a Stream of string elements with values for the provided {@code buffer}.
         * <p>
         * Each element in the stream depends on how this MemoryDumper was configured via its {@linkplain #builder()}.
         * <p>
         * As an example, an array created, initialized and used as follows
         * {@snippet lang = java:
         * var buffer = ByteBuffer.wrap("The quick brown fox jumped over the lazy dog\nSecond line\t:here".getBytes(StandardCharsets.UTF_8));
         * MemoryDumper dumper = MemoryDumper.builder()
         *            .addIndexColumn()
         *            .addDataColumn()
         *            .withColumnPrefix("|")
         *            .withColumnSuffix("|")
         *            .addDataColumn(Builder.ColumnRenderer.ofAscii())
         *            .build();
         * dumper.dump(bytes)
         *     .forEach(System.out::println);
         *}
         * will be printed as:
         * {@snippet lang = text:
         * 0000000000000000 54 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20 |The quick brown |
         * 0000000000000010 66 6f 78 20 6a 75 6d 70 65 64 20 6f 76 65 72 20 |fox jumped over |
         * 0000000000000020 74 68 65 20 6c 61 7a 79 20 64 6f 67 0a 53 65 63 |the lazy dog.Sec|
         * 0000000000000030 6f 6e 64 20 6c 69 6e 65 09 3a 68 65 72 65 00    |ond line.:here._|
         *}
         *
         * @param buffer to dump
         * @return a Stream of string elements
         */
        Stream<String> dump(ByteBuffer buffer);

        /**
         * {@return a new builder that can be used to configure and create MemoryDumper instances.}
         */
        static Builder builder() {
            return new StandardMemoryDumperBuilder();
        }

        /**
         * {@return a standard MemoryDumper that will format memory segments in human readable form}
         * <p>
         * As an example, a memory segment, initialized and used as follows
         * {@snippet lang = java:
         * MemoryDumper dumper = MemoryDumper.standard();
         *
         * try (MemorySession session = MemorySession.openConfined()) {
         *     MemorySegment segment = session.allocateUtf8String("The quick brown fox jumped over the lazy dog\nSecond line\t:here");
         *     dumper.dump(bytes)
         *         .forEach(System.out::println);
         * }
         *}
         * will be printed as:
         * {@snippet lang = text:
         * 0000000000000000 54 68 65 20 71 75 69 63 6b 20 62 72 6f 77 6e 20 |The quick brown |
         * 0000000000000010 66 6f 78 20 6a 75 6d 70 65 64 20 6f 76 65 72 20 |fox jumped over |
         * 0000000000000020 74 68 65 20 6c 61 7a 79 20 64 6f 67 0a 53 65 63 |the lazy dog.Sec|
         * 0000000000000030 6f 6e 64 20 6c 69 6e 65 09 3a 68 65 72 65 00    |ond line.:here._|
         *}
         */
        static MemoryDumper standard() {
            return StandardMemoryDumper.STANDARD;
        }

        /**
         * A non-threadsafe builder for MemoryDumper instances.
         *
         * @since 20
         */
        interface Builder {

            /**
             * Configures the number of bytes per line rendered per element in the output Stream.
             * <p>
             * Unless configured otherwise, the default number of bytes per line is 16.
             *
             * @param bytesPerLine the number of bytes per line (non-negative)
             * @return this Builder
             * @throws IllegalArgumentException if the provided {@code bytesPerLine} is negative
             */
            Builder withBytesPerLine(int bytesPerLine);

            /**
             * Configures the column prefix for subsequent index and data columns.
             * <p>
             * Unless configured otherwise, the default column prefix is "" (an empty String).
             *
             * @param prefix the column prefix (non-null)
             * @return this Builder
             * @throws NullPointerException if the provided {@code prefix} is {@code null}
             */
            Builder withColumnPrefix(String prefix);

            /**
             * Configures the column suffix for subsequent index and data columns.
             * <p>
             * Unless configured otherwise, the default suffix is "" (an empty String).
             *
             * @param suffix the column suffix (non-null)
             * @return this Builder
             * @throws NullPointerException if the provided {@code suffix} is {@code null}
             */
            Builder withColumnSuffix(String suffix);

            /**
             * Configures the column delimiter for subsequent index and data columns.
             * <p>
             * The column delimiter will be inserted between columns (i.e. not after the last column).
             * Unless configured otherwise, the default suffix is " " (a single space).
             *
             * @param delimiter the column delimiter (non-null)
             * @return this Builder
             * @throws NullPointerException if the provided {@code delimiter} is {@code null}
             */
            Builder withColumnDelimiter(String delimiter);

            /**
             * Adds an index column with an 8-byte (64-bit) index with hexadecimal values formatted by
             * the default {@link HexFormat#of()} formatter.
             *
             * @return this Builder
             */
            Builder addIndexColumn();

            /**
             * Adds an index column with the provided {@code indexBytes} index with hexadecimal values formatted by
             * the default {@link HexFormat#of()} formatter.
             *
             * @param indexBytes the number of bytes to render in the index (in the range [1, 8])
             * @return this Builder
             * @throws IllegalArgumentException if the provided {@code bytes} is outside the range [1, 8]
             */
            Builder addIndexColumn(int indexBytes);

            /**
             * Adds an index column with the provided {@code indexBytes} index with hexadecimal values formatted by
             * the provided {@code formatter }.
             *
             * @param indexBytes the number of bytes to render in the index (in the range [1, 8])
             * @param formatter  the formatter to use for byte values in the index
             * @return this Builder
             * @throws IllegalArgumentException if the provided {@code bytes} is outside the range [1, 8]
             * @throws NullPointerException     if the provided {@code formatter} is {@code null}
             */
            Builder addIndexColumn(int indexBytes, HexFormat formatter);

            /**
             * Adds an index column with the provided {@code indexBytes} index with hexadecimal values formatted by
             * the provided {@code renderer }.
             * <p>
             * Indexes used by the provided {@code renderer} are counted as byte indexes with the index value itself
             * and runs from the provided {@code bytes - 1} to 0.
             *
             * @param indexBytes the number of bytes to render in the index (in the range [1, 8])
             * @param renderer   the renderer to use for byte values in the index
             * @return this Builder
             * @throws IllegalArgumentException if the provided {@code bytes} is outside the range [1, 8]
             * @throws NullPointerException     if the provided {@code renderer} is {@code null}
             */
            Builder addIndexColumn(int indexBytes, ColumnRenderer renderer);

            /**
             * Adds a data column with hexadecimal values formatted by
             * a {@code HexFormat.ofDelimiter(" ")} formatter.
             *
             * @return this Builder
             */
            Builder addDataColumn();

            /**
             * Adds a data column with hexadecimal values formatted by
             * the provided {@code formatter }.
             *
             * @param formatter the formatter to use for byte values in a row
             * @return this Builder
             * @throws NullPointerException if the provided {@code formatter} is {@code null}
             */
            Builder addDataColumn(HexFormat formatter);

            /**
             * Adds a data column with hexadecimal values formatted by
             * the provided {@code renderer }.
             *
             * @param renderer the renderer to use for byte values in a row
             * @return this Builder
             * @throws NullPointerException if the provided {@code renderer} is {@code null}
             */
            Builder addDataColumn(ColumnRenderer renderer);

            /**
             * {@return a new MemoryDumper instance with configurations made so far.}
             */
            MemoryDumper build();

            /**
             * Represents a function that accepts a long index and a byte value and produces a String.
             *
             * @since 20
             */
            @FunctionalInterface
            interface ColumnRenderer {

                /**
                 * {@return a rendering of the provided {@code bytes} with the provided {@code bytesPerLine} }
                 * <p>
                 * Note: On the last line, chars may be padded using the ' ' character.
                 *
                 * @param bytesPerLine the bytes per line
                 * @param bytes        the byte array to render
                 */
                String render(int bytesPerLine, byte[] bytes);

                /**
                 * {@return a ByteRenderer that renders bytes into human readable characters.}
                 */
                static ColumnRenderer ofAscii() {
                    return (bytePerLine, bytes) -> {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < bytePerLine; i++) {
                            if (i >= bytes.length) {
                                // Padding on the last line
                                sb.append(" ");
                            } else {
                                final byte val = bytes[i];
                                if (val >= 32 && val < 127) {
                                    sb.append((char) val);
                                } else {
                                    sb.append(".");
                                }
                            }
                        }
                        return sb.toString();
                    };
                }

                /**
                 * {@return a ByteReader that renders bytes as per the provided {@code formatter}}
                 *
                 * @param formatter to use when formatting values
                 */
                static ColumnRenderer of(HexFormat formatter) {
                    requireNonNull(formatter);
                    return new ByteRenderedFromHexFormat(formatter);
                }

            }

        }

    }

    private static final class ByteRenderedFromHexFormat implements MemoryDumper.Builder.ColumnRenderer {

        private final HexFormat formatter;

        public ByteRenderedFromHexFormat(HexFormat formatter) {
            this.formatter = formatter;
        }

        @Override
        public String render(int bytesPerLine, byte[] bytes) {
            final var sb = new StringBuilder();
            if (bytesPerLine == bytes.length) {
                formatter.formatHex(sb, bytes);
            } else {
                // We are on the last line which needs padding
                final var expanded = new byte[bytesPerLine];
                System.arraycopy(bytes, 0, expanded, 0, bytes.length);
                formatter.formatHex(sb, expanded);
                final int bytesToRemove = bytesPerLine - bytes.length;
                final int charsToRemove = bytesToRemove * (formatter.prefix().length()+formatter.suffix().length()+2) +
                                          (bytesToRemove -1) * formatter.delimiter().length();
                // Erase the excess part
                for (int i = 0; i < charsToRemove; i++) {
                    sb.setCharAt(sb.length() - i - 1, ' ');
                }
            }
            return sb.toString();
        }
    }

    private static final class StandardMemoryDumperBuilder implements MemoryDumper.Builder {

        private final List<Column> columns;
        private int bytesPerLine = 16;
        private String columnPrefix = "";
        private String columnSuffix = "";
        private String columnDelimiter = " ";

        StandardMemoryDumperBuilder() {
            columns = new ArrayList<>();
        }

        @Override
        public MemoryDumper.Builder withBytesPerLine(int bytes) {
            bytesPerLine = requirePositive(bytes);
            return this;
        }

        @Override
        public MemoryDumper.Builder withColumnPrefix(String prefix) {
            columnPrefix = requireNonNull(prefix);
            return this;
        }

        @Override
        public MemoryDumper.Builder withColumnSuffix(String suffix) {
            columnSuffix = requireNonNull(suffix);
            return this;
        }

        @Override
        public MemoryDumper.Builder withColumnDelimiter(String delimiter) {
            columnDelimiter = requireNonNull(delimiter);
            return this;
        }

        @Override
        public MemoryDumper.Builder addIndexColumn() {
            return addIndexColumn(8);
        }

        @Override
        public MemoryDumper.Builder addIndexColumn(int indexBytes) {
            requireBetweenClosed(1, 8, indexBytes);
            return addIndexColumn(indexBytes, HexFormat.of());
        }

        @Override
        public MemoryDumper.Builder addIndexColumn(int indexBytes, HexFormat formatter) {
            requireBetweenClosed(1, 8, indexBytes);
            requireNonNull(formatter);
            return addIndexColumn(indexBytes, ColumnRenderer.of(formatter));
        }

        @Override
        public MemoryDumper.Builder addIndexColumn(int indexBytes, ColumnRenderer renderer) {
            requireBetweenClosed(1, 8, indexBytes);
            requireNonNull(renderer);
            columns.add(new Column(ColumnType.INDEX, OptionalInt.of(indexBytes), columnPrefix, renderer, columnSuffix, columnDelimiter));
            return this;
        }

        @Override
        public MemoryDumper.Builder addDataColumn() {
            return addDataColumn(HexFormat.ofDelimiter(" "));
        }

        @Override
        public MemoryDumper.Builder addDataColumn(HexFormat formatter) {
            requireNonNull(formatter);
            return addDataColumn(ColumnRenderer.of(formatter));
        }

        @Override
        public MemoryDumper.Builder addDataColumn(ColumnRenderer renderer) {
            requireNonNull(renderer);
            columns.add(new Column(ColumnType.DATA, OptionalInt.empty(), columnPrefix, renderer, columnSuffix, columnDelimiter));
            return this;
        }

        @Override
        public MemoryDumper build() {
            return new StandardMemoryDumper(bytesPerLine, columns);
        }

        enum ColumnType {
            INDEX, DATA;
        }

        private record Column(ColumnType type,
                              OptionalInt indexByteLength,
                              String prefix,
                              ColumnRenderer renderer,
                              String suffix,
                              String delimiter) {
        }

        private int requirePositive(int value) {
            if (value < 1)
                throw new IllegalArgumentException("Value must be positive:" + value);
            return value;
        }

        // We assume from < to
        private int requireBetweenClosed(int from, int to, int value) {
            if (value < from || value > to)
                throw new IllegalArgumentException("Value " + value + " is not in the range [" + from + ", " + to + "]");
            return value;
        }

    }

    private static final class StandardMemoryDumper implements MemoryDumper {

        private static final MemoryDumper STANDARD = MemoryDumper.builder()
                .addIndexColumn()
                .addDataColumn()
                .withColumnPrefix("|")
                .withColumnSuffix("|")
                .addDataColumn(HexFormat.MemoryDumper.Builder.ColumnRenderer.ofAscii())
                .build();

        private final int bytesPerLine;
        private final List<StandardMemoryDumperBuilder.Column> columns;

        StandardMemoryDumper(int bytesPerLine,
                             List<StandardMemoryDumperBuilder.Column> columns) {
            this.bytesPerLine = bytesPerLine;
            this.columns = new ArrayList<>(columns);
        }

        @Override
        public Stream<String> dump(byte[] bytes) {
            return dump0(bytes, ba -> ba.length, (ba, index) -> ba[Math.toIntExact(index)]);
        }

        @Override
        public Stream<String> dump(byte[] bytes, int fromIndex, int toIndex) {
            if (fromIndex == 0) {
                if (toIndex>bytes.length)
                    throw new IllegalArgumentException("fromIndex " + fromIndex + " is greater than the array length " + bytes.length);
                return dump0(bytes, ba -> toIndex, (ba, index) -> ba[(int) index]);
            }
            return dump0(MemorySegment.ofArray(bytes).asSlice(fromIndex, toIndex - fromIndex),
                    MemorySegment::byteSize,
                    memorySegmentByteExtractor());
        }

        @Override
        public Stream<String> dump(MemorySegment segment) {
            return dump0(segment, MemorySegment::byteSize, memorySegmentByteExtractor());
        }

        @Override
        public Stream<String> dump(ByteBuffer buffer) {
            return dump0(buffer, ByteBuffer::remaining, (bb, index) -> bb.get(bb.position() + (int) (index)));
        }

        private <M> Stream<String> dump0(M memory,
                                         ToLongFunction<M> byteSizeExtractor,
                                         DumpState.ByteExtractor<M> byteExtractor) {
            requireNonNull(memory);

            final long lastIndex = byteSizeExtractor.applyAsLong(memory);
            final long lines = (lastIndex + bytesPerLine - 1) / bytesPerLine;
            final var state = new DumpState<>(memory, lastIndex, byteExtractor, bytesPerLine, columns);
            return LongStream.range(0, lines)
                    .mapToObj(state::lineMapper);
        }

        @Override
        public String toString() {
            return "StandardMemoryDumper{" +
                    "bytesPerLine=" + bytesPerLine +
                    ", columns=" + columns +
                    '}';
        }

        private static DumpState.ByteExtractor<MemorySegment> memorySegmentByteExtractor() {
            return (memory, index) -> memory.get(JAVA_BYTE, index);
        }

    }

    private static final class DumpState<M> {

        private final M memory;
        private final long lastIndex;
        private final ByteExtractor<M> byteExtractor;
        private final int bytesPerLine;
        private final List<StandardMemoryDumperBuilder.Column> columns;

        DumpState(M memory,
                  long lastIndex,
                  ByteExtractor<M> byteExtractor,
                  int bytesPerLine,
                  List<StandardMemoryDumperBuilder.Column> columns) {
            this.memory = memory;
            this.lastIndex = lastIndex;
            this.byteExtractor = byteExtractor;
            this.bytesPerLine = bytesPerLine;
            this.columns = columns;
        }


        /**
         * {@return a new complete line (either a full line or the last line) for the provided {@code line} number}.
         *
         * @param line the line to render (non-negative)
         * @return a new line or {@code null}
         */
        String lineMapper(long line) {

            // Todo: Investigate how to handle mapped sparse files

            final StringBuilder sb = new StringBuilder();
            int columnCount = 0;
            for (StandardMemoryDumperBuilder.Column c : columns) {
                if (columnCount > 0) {
                    sb.append(c.delimiter());
                }
                sb.append(c.prefix());
                if (c.type() == StandardMemoryDumperBuilder.ColumnType.INDEX) {
                    final int len = c.indexByteLength().orElseThrow();
                    sb.append(c.renderer().render(len, toByteArray(len, line * bytesPerLine)));
                }
                if (c.type() == StandardMemoryDumperBuilder.ColumnType.DATA) {
                    final int len;
                    final long overflow = (line + 1L) * bytesPerLine - lastIndex;
                    if (overflow > 0) {
                        // We are on the last line and the last line needs trimming
                        len = Math.toIntExact(bytesPerLine - overflow);
                    } else {
                        // We are on a line (last or not) and no trimming needs to be done
                        len = bytesPerLine;
                    }
                    final byte[] bytes = new byte[len];
                    for (int i = 0; i < len; i++) {
                        final long index = line * bytesPerLine + i;
                        bytes[i] = byteExtractor.extract(memory, index);
                    }
                    sb.append(c.renderer().render(bytesPerLine, bytes));
                }
                sb.append(c.suffix());
                columnCount++;
            }
            return sb.toString();
        }

        @FunctionalInterface
        interface ByteExtractor<M> {
            byte extract(M memory, long index);

            static ByteExtractor<MemorySegment> ofMemorySegment() {
                return (memory, index) -> memory.get(JAVA_BYTE, index);
            }

            static ByteExtractor<byte[]> ofByteArray() {
                return (memory, index) -> memory[Math.toIntExact(index)];
            }
        }
    }

    private static byte[] toByteArray(int length, long l) {
        byte[] result = new byte[length];
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            for (int i = (length - 1); i >= 0; i--) {
                result[i] = (byte) (l & 0xFF);
                l >>= 8;
            }
        } else {
            for (int i = 0; i < length; i++) {
                result[i] = (byte) (l & 0xFF);
                l >>= 8;
            }
        }
        return result;
    }

}
