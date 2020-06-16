/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.foreign.AbstractMemorySegmentImpl;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.NativeMemorySegmentImpl;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.SharedUtils;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A set of utilities for working with libraries using the C language/ABI
 */
public class CSupport {
    /**
     * Obtain a linker that uses the de facto C ABI of the current system to do it's linking.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * @return a linker for this system.
     * @throws IllegalAccessError if the runtime property {@code foreign.restricted} is not set to either
     * {@code permit}, {@code warn} or {@code debug} (the default value is set to {@code deny}).
     */
    public static ForeignLinker getSystemLinker() {
        Utils.checkRestrictedAccess("CSupport.getSystemLinker");
        return SharedUtils.getSystemLinker();
    }

    /**
     * An interface that models a C {@code va_list}.
     *
     * Per the C specification (see C standard 6.5.2.2 Function calls - item 6),
     * arguments to variadic calls are erased by way of 'default argument promotions',
     * which erases integral types by way of integer promotion (see C standard 6.3.1.1 - item 2),
     * and which erases all {@code float} arguments to {@code double}.
     *
     * As such, this interface only supports reading {@code int}, {@code double},
     * and any other type that fits into a {@code long}.
     */
    public interface VaList extends AutoCloseable {

        /**
         * Reads a value into an {@code int}
         *
         * @param layout the layout of the value
         * @return the value read as an {@code int}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        int vargAsInt(MemoryLayout layout);

        /**
         * Reads a value into a {@code long}
         *
         * @param layout the layout of the value
         * @return the value read as an {@code long}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        long vargAsLong(MemoryLayout layout);

        /**
         * Reads a value into a {@code double}
         *
         * @param layout the layout of the value
         * @return the value read as an {@code double}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        double vargAsDouble(MemoryLayout layout);

        /**
         * Reads a value into a {@code MemoryAddress}
         *
         * @param layout the layout of the value
         * @return the value read as an {@code MemoryAddress}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        MemoryAddress vargAsAddress(MemoryLayout layout);

        /**
         * Reads a value into a {@code MemorySegment}
         *
         * @param layout the layout of the value
         * @return the value read as an {@code MemorySegment}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        MemorySegment vargAsSegment(MemoryLayout layout);

        /**
         * Skips a number of va arguments with the given memory layouts.
         *
         * @param layouts the layout of the value
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        void skip(MemoryLayout... layouts);

        /**
         * A predicate used to check if the memory associated with the C {@code va_list} modelled
         * by this instance is still valid; or, in other words, if {@code close()} has been called on this
         * instance.
         *
         * @return true, if the memory associated with the C {@code va_list} modelled by this instance is still valid
         * @see #close()
         */
        boolean isAlive();

        /**
         * Releases the underlying C {@code va_list} modelled by this instance. As a result, subsequent attempts to call
         * operations on this instance (e.g. {@link #copy()} will fail with an exception.
         *
         * @see #isAlive()
         */
        void close();

        /**
         * Copies this C {@code va_list}.
         *
         * @return a copy of this C {@code va_list}.
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        VaList copy();

        /**
         * Returns the memory address of the C {@code va_list} associated with this instance.
         *
         * @return the memory address of the C {@code va_list} associated with this instance.
         */
        MemoryAddress address();

        /**
         * Constructs a new {@code VaList} instance out of a memory address pointing to an existing C {@code va_list}.
         *
         * @param address a memory address pointing to an existing C {@code va_list}.
         * @return a new {@code VaList} instance backed by the C {@code va_list} at {@code address}.
         */
        static VaList ofAddress(MemoryAddress address) {
            return SharedUtils.newVaListOfAddress(address);
        }

        /**
         * Constructs a new {@code VaList} using a builder (see {@link Builder}).
         *
         * Note that when there are no arguments added to the created va list,
         * this method will return the same as {@linkplain #empty()}.
         *
         * @param actions a consumer for a builder (see {@link Builder}) which can be used to specify the contents
         *                of the underlying C {@code va_list}.
         * @return a new {@code VaList} instance backed by a fresh C {@code va_list}.
         */
        static VaList make(Consumer<VaList.Builder> actions) {
            return SharedUtils.newVaList(actions);
        }

        /**
         * Returns an empty C {@code va_list} constant.
         *
         * The returned {@code VaList} can not be closed.
         *
         * @return a {@code VaList} modelling an empty C {@code va_list}.
         */
        static VaList empty() {
            return SharedUtils.emptyVaList();
        }

        /**
         * A builder interface used to construct a C {@code va_list}.
         */
        interface Builder {

            /**
             * Adds a native value represented as an {@code int} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as an {@code int}.
             * @return this builder.
             */
            Builder vargFromInt(MemoryLayout layout, int value);

            /**
             * Adds a native value represented as a {@code long} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code long}.
             * @return this builder.
             */
            Builder vargFromLong(MemoryLayout layout, long value);

            /**
             * Adds a native value represented as a {@code double} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code double}.
             * @return this builder.
             */
            Builder vargFromDouble(MemoryLayout layout, double value);

            /**
             * Adds a native value represented as a {@code MemoryAddress} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code MemoryAddress}.
             * @return this builder.
             */
            Builder vargFromAddress(MemoryLayout layout, MemoryAddress value);

            /**
             * Adds a native value represented as a {@code MemorySegment} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code MemorySegment}.
             * @return this builder.
             */
            Builder vargFromSegment(MemoryLayout layout, MemorySegment value);
        }
    }

    /**
     * The {@code _Bool} native type.
     */
    public static final ValueLayout C_BOOL = Utils.pick(SysV.C_BOOL, Win64.C_BOOL, AArch64.C_BOOL);
    /**
     * The {@code char} native type.
     */
    public static final ValueLayout C_CHAR = Utils.pick(SysV.C_CHAR, Win64.C_CHAR, AArch64.C_CHAR);
    /**
     * The {@code short} native type.
     */
    public static final ValueLayout C_SHORT = Utils.pick(SysV.C_SHORT, Win64.C_SHORT, AArch64.C_SHORT);
    /**
     * The {@code int} native type.
     */
    public static final ValueLayout C_INT = Utils.pick(SysV.C_INT, Win64.C_INT, AArch64.C_INT);
    /**
     * The {@code long} native type.
     */
    public static final ValueLayout C_LONG = Utils.pick(SysV.C_LONG, Win64.C_LONG, AArch64.C_LONG);
    /**
     * The {@code long long} native type.
     */
    public static final ValueLayout C_LONGLONG = Utils.pick(SysV.C_LONGLONG, Win64.C_LONGLONG, AArch64.C_LONGLONG);
    /**
     * The {@code float} native type.
     */
    public static final ValueLayout C_FLOAT = Utils.pick(SysV.C_FLOAT, Win64.C_FLOAT, AArch64.C_FLOAT);
    /**
     * The {@code double} native type.
     */
    public static final ValueLayout C_DOUBLE = Utils.pick(SysV.C_DOUBLE, Win64.C_DOUBLE, AArch64.C_DOUBLE);
    /**
     * The {@code long double} native type.
     */
    public static final ValueLayout C_LONGDOUBLE = Utils.pick(SysV.C_LONGDOUBLE, Win64.C_LONGDOUBLE, AArch64.C_LONGDOUBLE);
    /**
     * The {@code T*} native type.
     */
    public static final ValueLayout C_POINTER = Utils.pick(SysV.C_POINTER, Win64.C_POINTER, AArch64.C_POINTER);

    /**
     * The {@code va_list} native type.
     */
    public static final MemoryLayout C_VA_LIST = Utils.pick(SysV.C_VA_LIST, Win64.C_VA_LIST, null);

    /**
     * This class defines layout constants modelling standard primitive types supported by the x64 SystemV ABI.
     */
    public static final class SysV {
        private SysV() {
            //just the one
        }

        /**
         * The name of the SysV linker ({@see ForeignLinker#name})
         */
        public static final String NAME = "SysV";

        public final static String CLASS_ATTRIBUTE_NAME = "abi/sysv/class";

        public enum ArgumentClass {
            INTEGER,
            SSE,
            X87,
            COMPLEX_87,
            POINTER;
        }

        /**
         * The {@code _Bool} native type.
         */
        public static final ValueLayout C_BOOL = MemoryLayouts.BITS_8_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code char} native type.
         */
        public static final ValueLayout C_CHAR = MemoryLayouts.BITS_8_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code short} native type.
         */
        public static final ValueLayout C_SHORT = MemoryLayouts.BITS_16_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code float} native type.
         */
        public static final ValueLayout C_FLOAT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.SSE);

        /**
         * The {@code double} native type.
         */
        public static final ValueLayout C_DOUBLE = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.SSE);

        /**
         * The {@code long double} native type.
         */
        public static final ValueLayout C_LONGDOUBLE = MemoryLayout.ofValueBits(128, ByteOrder.LITTLE_ENDIAN)
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.X87);

        /**
         * The {@code complex long double} native type.
         */
        public static final GroupLayout C_COMPLEX_LONGDOUBLE = MemoryLayout.ofStruct(C_LONGDOUBLE, C_LONGDOUBLE)
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.COMPLEX_87);

        /**
         * The {@code T*} native type.
         */
        public static final ValueLayout C_POINTER = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.POINTER);

        /**
         * The {@code va_list} native type, as it is passed to a function.
         */
        public static final MemoryLayout C_VA_LIST = SysV.C_POINTER;
    }

    /**
     * This class defines layout constants modelling standard primitive types supported by the x64 Windows ABI.
     */
    public static final class Win64 {

        private Win64() {
            //just the one
        }

        /**
         * The name of the Windows linker ({@see ForeignLinker#name})
         */
        public final static String NAME = "Windows";

        public final static String VARARGS_ATTRIBUTE_NAME = "abi/windows/varargs";

        public final static String CLASS_ATTRIBUTE_NAME = "abi/windows/class";

        public enum ArgumentClass {
            INTEGER,
            FLOAT,
            POINTER;
        }

        /**
         * The {@code _Bool} native type.
         */
        public static final ValueLayout C_BOOL = MemoryLayouts.BITS_8_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code char} native type.
         */
        public static final ValueLayout C_CHAR = MemoryLayouts.BITS_8_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code short} native type.
         */
        public static final ValueLayout C_SHORT = MemoryLayouts.BITS_16_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code float} native type.
         */
        public static final ValueLayout C_FLOAT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.FLOAT);

        /**
         * The {@code double} native type.
         */
        public static final ValueLayout C_DOUBLE = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.FLOAT);

        /**
         * The {@code long double} native type.
         */
        public static final ValueLayout C_LONGDOUBLE = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.FLOAT);

        /**
         * The {@code T*} native type.
         */
        public static final ValueLayout C_POINTER = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.POINTER);

        /**
         * The {@code va_list} native type, as it is passed to a function.
         */
        public static final MemoryLayout C_VA_LIST = Win64.C_POINTER;

        public static ValueLayout asVarArg(ValueLayout l) {
            return l.withAttribute(VARARGS_ATTRIBUTE_NAME, "true");
        }
    }

    /**
     * This class defines layout constants modelling standard primitive types supported by the AArch64 ABI.
     */
    public static final class AArch64 {

        private AArch64() {
            //just the one
        }

        /**
         * The name of the AArch64 linker ({@see ForeignLinker#name})
         */
        public final static String NAME = "AArch64";

        public static final String CLASS_ATTRIBUTE_NAME = "abi/aarch64/class";

        public enum ArgumentClass {
            INTEGER,
            VECTOR,
            POINTER;
        }

        /**
         * The {@code _Bool} native type.
         */
        public static final ValueLayout C_BOOL = MemoryLayouts.BITS_8_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code char} native type.
         */
        public static final ValueLayout C_CHAR = MemoryLayouts.BITS_8_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code short} native type.
         */
        public static final ValueLayout C_SHORT = MemoryLayouts.BITS_16_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code float} native type.
         */
        public static final ValueLayout C_FLOAT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.VECTOR);

        /**
         * The {@code double} native type.
         */
        public static final ValueLayout C_DOUBLE = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.VECTOR);

        /**
         * The {@code long double} native type.
         */
        public static final ValueLayout C_LONGDOUBLE = MemoryLayout.ofValueBits(128, ByteOrder.LITTLE_ENDIAN)
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.VECTOR);

        /**
         * The {@code T*} native type.
         */
        public static final ValueLayout C_POINTER = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.POINTER);
    }

    private final static VarHandle byteArrHandle =
            MemoryLayout.ofSequence(C_CHAR).varHandle(byte.class, MemoryLayout.PathElement.sequenceElement());

    /**
     * Convert a Java string into a null-terminated C string, using the
     * platform's default charset, storing the result into a new native memory segment.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement byte array.  The
     * {@link java.nio.charset.CharsetEncoder} class should be used when more
     * control over the encoding process is required.
     *
     * @param str the Java string to be converted into a C string.
     * @return a new native memory segment containing the converted C string.
     * @throws NullPointerException if either {@code str == null}.
     */
    public static MemorySegment toCString(String str) {
        Objects.requireNonNull(str);
        return toCString(str.getBytes());
    }

    /**
     * Convert a Java string into a null-terminated C string, using the given {@linkplain java.nio.charset.Charset charset},
     * storing the result into a new native memory segment.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement byte array.  The
     * {@link java.nio.charset.CharsetEncoder} class should be used when more
     * control over the encoding process is required.
     *
     * @param str the Java string to be converted into a C string.
     * @param charset The {@linkplain java.nio.charset.Charset} to be used to compute the contents of the C string.
     * @return a new native memory segment containing the converted C string.
     * @throws NullPointerException if either {@code str == null} or {@code charset == null}.
     */
    public static MemorySegment toCString(String str, Charset charset) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(charset);
        return toCString(str.getBytes(charset));
    }

    /**
     * Convert a Java string into a null-terminated C string, using the platform's default charset,
     * storing the result into a native memory segment allocated using the provided scope.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement byte array.  The
     * {@link java.nio.charset.CharsetEncoder} class should be used when more
     * control over the encoding process is required.
     *
     * @param str the Java string to be converted into a C string.
     * @param scope the scope to be used for the native segment allocation.
     * @return a new native memory segment containing the converted C string.
     * @throws NullPointerException if either {@code str == null} or {@code scope == null}.
     */
    public static MemoryAddress toCString(String str, NativeScope scope) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(scope);
        return toCString(str.getBytes(), scope);
    }

    /**
     * Convert a Java string into a null-terminated C string, using the given {@linkplain java.nio.charset.Charset charset},
     * storing the result into a new native memory segment native memory segment allocated using the provided scope.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement byte array.  The
     * {@link java.nio.charset.CharsetEncoder} class should be used when more
     * control over the encoding process is required.
     *
     * @param str the Java string to be converted into a C string.
     * @param charset The {@linkplain java.nio.charset.Charset} to be used to compute the contents of the C string.
     * @param scope the scope to be used for the native segment allocation.
     * @return a new native memory segment containing the converted C string.
     * @throws NullPointerException if either {@code str == null}, {@code charset == null} or {@code scope == null}.
     */
    public static MemoryAddress toCString(String str, Charset charset, NativeScope scope) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(scope);
        return toCString(str.getBytes(charset), scope);
    }

    /**
     * Convert a null-terminated C string stored at given address into a Java string, using the platform's default charset.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string.  The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * @param addr the address at which the string is stored.
     * @return a Java string with the contents of the null-terminated C string at given address.
     * @throws NullPointerException if {@code addr == null}
     * @throws IllegalArgumentException if the size of the native string is greater than {@code Integer.MAX_VALUE}.
     */
    public static String toJavaStringRestricted(MemoryAddress addr) {
        Utils.checkRestrictedAccess("CSupport.toJavaStringRestricted");
        return toJavaStringInternal(addr.rebase(AbstractMemorySegmentImpl.EVERYTHING), Charset.defaultCharset());
    }

    /**
     * Convert a null-terminated C string stored at given address into a Java string, using the given {@linkplain java.nio.charset.Charset charset}.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string.  The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * @param addr the address at which the string is stored.
     * @param charset The {@linkplain java.nio.charset.Charset} to be used to compute the contents of the Java string.
     * @return a Java string with the contents of the null-terminated C string at given address.
     * @throws NullPointerException if {@code addr == null}
     * @throws IllegalArgumentException if the size of the native string is greater than {@code Integer.MAX_VALUE}.
     */
    public static String toJavaStringRestricted(MemoryAddress addr, Charset charset) {
        Utils.checkRestrictedAccess("CSupport.toJavaStringRestricted");
        return toJavaStringInternal(addr.rebase(AbstractMemorySegmentImpl.EVERYTHING), charset);
    }

    /**
     * Convert a null-terminated C string stored at given address into a Java string, using the platform's default charset.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string.  The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     * @param addr the address at which the string is stored.
     * @return a Java string with the contents of the null-terminated C string at given address.
     * @throws NullPointerException if {@code addr == null}
     * @throws IllegalArgumentException if the size of the native string is greater than {@code Integer.MAX_VALUE}.
     * @throws IllegalStateException if the size of the native string is greater than the size of the segment
     * associated with {@code addr}, or if {@code addr} is associated with a segment that is </em>not alive<em>.
     */
    public static String toJavaString(MemoryAddress addr) {
        return toJavaStringInternal(addr, Charset.defaultCharset());
    }

    /**
     * Convert a null-terminated C string stored at given address into a Java string, using the given {@linkplain java.nio.charset.Charset charset}.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string.  The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     * @param addr the address at which the string is stored.
     * @param charset The {@linkplain java.nio.charset.Charset} to be used to compute the contents of the Java string.
     * @return a Java string with the contents of the null-terminated C string at given address.
     * @throws NullPointerException if {@code addr == null}
     * @throws IllegalArgumentException if the size of the native string is greater than {@code Integer.MAX_VALUE}.
     * @throws IllegalStateException if the size of the native string is greater than the size of the segment
     * associated with {@code addr}, or if {@code addr} is associated with a segment that is </em>not alive<em>.
     */
    public static String toJavaString(MemoryAddress addr, Charset charset) {
        return toJavaStringInternal(addr, charset);
    }

    private static String toJavaStringInternal(MemoryAddress addr, Charset charset) {
        int len = strlen(addr);
        byte[] bytes = new byte[len];
        MemorySegment.ofArray(bytes)
                .copyFrom(NativeMemorySegmentImpl.makeNativeSegmentUnchecked(addr, len, null, null, null));
        return new String(bytes, charset);
    }

    private static int strlen(MemoryAddress address) {
        // iterate until overflow (String can only hold a byte[], whose length can be expressed as an int)
        for (int offset = 0; offset >= 0; offset++) {
            byte curr = (byte) byteArrHandle.get(address, (long) offset);
            if (curr == 0) {
                return offset;
            }
        }
        throw new IllegalArgumentException("String too large");
    }

    private static void copy(MemoryAddress addr, byte[] bytes) {
        var heapSegment = MemorySegment.ofArray(bytes);
        addr.segment().copyFrom(heapSegment);
        byteArrHandle.set(addr, (long)bytes.length, (byte)0);
    }

    private static MemorySegment toCString(byte[] bytes) {
        MemorySegment segment = MemorySegment.allocateNative(bytes.length + 1, 1L);
        MemoryAddress addr = segment.baseAddress();
        copy(addr, bytes);
        return segment;
    }

    private static MemoryAddress toCString(byte[] bytes, NativeScope scope) {
        MemoryAddress addr = scope.allocate(bytes.length + 1, 1L);
        copy(addr, bytes);
        return addr;
    }
}
