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

import jdk.internal.foreign.NativeMemorySegmentImpl;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.SharedUtils;

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
     * <p>
     * A va list is a stateful cursor used to iterate over a set of variadic arguments.
     * <p>
     * Per the C specification (see C standard 6.5.2.2 Function calls - item 6),
     * arguments to variadic calls are erased by way of 'default argument promotions',
     * which erases integral types by way of integer promotion (see C standard 6.3.1.1 - item 2),
     * and which erases all {@code float} arguments to {@code double}.
     * <p>
     * As such, this interface only supports reading {@code int}, {@code double},
     * and any other type that fits into a {@code long}.
     */
    public interface VaList extends Addressable, AutoCloseable {

        /**
         * Reads the next value as an {@code int} and advances this va list's position.
         *
         * @param layout the layout of the value
         * @return the value read as an {@code int}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code int}
         */
        int vargAsInt(MemoryLayout layout);

        /**
         * Reads the next value as a {@code long} and advances this va list's position.
         *
         * @param layout the layout of the value
         * @return the value read as an {@code long}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code long}
         */
        long vargAsLong(MemoryLayout layout);

        /**
         * Reads the next value as a {@code double} and advances this va list's position.
         *
         * @param layout the layout of the value
         * @return the value read as an {@code double}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code double}
         */
        double vargAsDouble(MemoryLayout layout);

        /**
         * Reads the next value as a {@code MemoryAddress} and advances this va list's position.
         *
         * @param layout the layout of the value
         * @return the value read as an {@code MemoryAddress}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code MemoryAddress}
         */
        MemoryAddress vargAsAddress(MemoryLayout layout);

        /**
         * Reads the next value as a {@code MemorySegment}, and advances this va list's position.
         * <p>
         * The memory segment returned by this method will be allocated using
         * {@link MemorySegment#allocateNative(long, long)}, and will have to be closed separately.
         *
         * @param layout the layout of the value
         * @return the value read as an {@code MemorySegment}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code MemorySegment}
         */
        MemorySegment vargAsSegment(MemoryLayout layout);

        /**
         * Reads the next value as a {@code MemorySegment}, and advances this va list's position.
         * <p>
         * The memory segment returned by this method will be allocated using the given {@code NativeScope}.
         *
         * @param layout the layout of the value
         * @param scope the scope to allocate the segment in
         * @return the value read as an {@code MemorySegment}
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         * @throws IllegalArgumentException if the given memory layout is not compatible with {@code MemorySegment}
         */
        MemorySegment vargAsSegment(MemoryLayout layout, NativeScope scope);

        /**
         * Skips a number of elements with the given memory layouts, and advances this va list's position.
         *
         * @param layouts the layout of the value
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        void skip(MemoryLayout... layouts);

        /**
         * A predicate used to check if the memory associated with the C {@code va_list} modelled
         * by this instance is still valid to use.
         *
         * @return true, if the memory associated with the C {@code va_list} modelled by this instance is still valid
         * @see #close()
         */
        boolean isAlive();

        /**
         * Releases the underlying C {@code va_list} modelled by this instance, and any native memory that is attached
         * to this va list that holds its elements (see {@link VaList#make(Consumer)}).
         * <p>
         * After calling this method, {@link #isAlive()} will return {@code false} and further attempts to read values
         * from this va list will result in an exception.
         *
         * @see #isAlive()
         */
        void close();

        /**
         * Copies this C {@code va_list} at its current position. Copying is useful to traverse the va list's elements
         * starting from the current position, without affecting the state of the original va list, essentially
         * allowing the elements to be traversed multiple times.
         * <p>
         * If this method needs to allocate native memory for the copy, it will use
         * {@link MemorySegment#allocateNative(long, long)} to do so. {@link #close()} will have to be called on the
         * returned va list instance to release the allocated memory.
         * <p>
         * This method only copies the va list cursor itself and not the memory that may be attached to the
         * va list which holds its elements. That means that if this va list was created with the
         * {@link #make(Consumer)} method, closing this va list will also release the native memory that holds its
         * elements, making the copy unusable.
         *
         * @return a copy of this C {@code va_list}.
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        VaList copy();

        /**
         * Copies this C {@code va_list} at its current position. Copying is useful to traverse the va list's elements
         * starting from the current position, without affecting the state of the original va list, essentially
         * allowing the elements to be traversed multiple times.
         * <p>
         * If this method needs to allocate native memory for the copy, it will use
         * the given {@code NativeScope} to do so.
         * <p>
         * This method only copies the va list cursor itself and not the memory that may be attached to the
         * va list which holds its elements. That means that if this va list was created with the
         * {@link #make(Consumer)} method, closing this va list will also release the native memory that holds its
         * elements, making the copy unusable.
         *
         * @param scope the scope to allocate the copy in
         * @return a copy of this C {@code va_list}.
         * @throws IllegalStateException if the C {@code va_list} associated with this instance is no longer valid
         * (see {@link #close()}).
         */
        VaList copy(NativeScope scope);

        /**
         * Returns the memory address of the C {@code va_list} associated with this instance.
         *
         * @return the memory address of the C {@code va_list} associated with this instance.
         */
        @Override
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
         * <p>
         * If this method needs to allocate native memory for the va list, it will use
         * {@link MemorySegment#allocateNative(long, long)} to do so.
         * <p>
         * This method will allocate native memory to hold the elements in the va list. This memory
         * will be 'attached' to the returned va list instance, and will be released when {@link VaList#close()}
         * is called.
         * <p>
         * Note that when there are no elements added to the created va list,
         * this method will return the same as {@linkplain #empty()}.
         *
         * @param actions a consumer for a builder (see {@link Builder}) which can be used to specify the elements
         *                of the underlying C {@code va_list}.
         * @return a new {@code VaList} instance backed by a fresh C {@code va_list}.
         */
        static VaList make(Consumer<VaList.Builder> actions) {
            return SharedUtils.newVaList(actions, MemorySegment::allocateNative);
        }

        /**
         * Constructs a new {@code VaList} using a builder (see {@link Builder}).
         * <p>
         * If this method needs to allocate native memory for the va list, it will use
         * the given {@code NativeScope} to do so.
         * <p>
         * This method will allocate native memory to hold the elements in the va list. This memory
         * will be managed by the given {@code NativeScope}, and will be released when the scope is closed.
         * <p>
         * Note that when there are no elements added to the created va list,
         * this method will return the same as {@linkplain #empty()}.
         *
         * @param actions a consumer for a builder (see {@link Builder}) which can be used to specify the elements
         *                of the underlying C {@code va_list}.
         * @param scope the scope to be used for the valist allocation.
         * @return a new {@code VaList} instance backed by a fresh C {@code va_list}.
         */
        static VaList make(Consumer<VaList.Builder> actions, NativeScope scope) {
            return SharedUtils.newVaList(actions, SharedUtils.Allocator.ofScope(scope));
        }

        /**
         * Returns an empty C {@code va_list} constant.
         * <p>
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
             * @throws IllegalArgumentException if the given memory layout is not compatible with {@code int}
             */
            Builder vargFromInt(MemoryLayout layout, int value);

            /**
             * Adds a native value represented as a {@code long} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code long}.
             * @return this builder.
             * @throws IllegalArgumentException if the given memory layout is not compatible with {@code long}
             */
            Builder vargFromLong(MemoryLayout layout, long value);

            /**
             * Adds a native value represented as a {@code double} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code double}.
             * @return this builder.
             * @throws IllegalArgumentException if the given memory layout is not compatible with {@code double}
             */
            Builder vargFromDouble(MemoryLayout layout, double value);

            /**
             * Adds a native value represented as a {@code MemoryAddress} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code MemoryAddress}.
             * @return this builder.
             * @throws IllegalArgumentException if the given memory layout is not compatible with {@code MemoryAddress}
             */
            Builder vargFromAddress(MemoryLayout layout, MemoryAddress value);

            /**
             * Adds a native value represented as a {@code MemorySegment} to the C {@code va_list} being constructed.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code MemorySegment}.
             * @return this builder.
             * @throws IllegalArgumentException if the given memory layout is not compatible with {@code MemorySegment}
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
    public static final MemoryLayout C_VA_LIST = Utils.pick(SysV.C_VA_LIST, Win64.C_VA_LIST, AArch64.C_VA_LIST);

    /**
     * This class defines layout constants modelling standard primitive types supported by the x64 SystemV ABI.
     */
    public static final class SysV {
        private SysV() {
            //just the one
        }

        /**
         * The name of the SysV linker
         * @see ForeignLinker#name
         */
        public static final String NAME = "SysV";

        /**
         * The name of the layout attribute (see {@link MemoryLayout#attributes()} used for ABI classification. The
         * attribute value must be an enum constant from {@link ArgumentClass}.
         */
        public final static String CLASS_ATTRIBUTE_NAME = "abi/sysv/class";

        /**
         * Constants used for ABI classification. They are referred to by the layout attribute {@link #CLASS_ATTRIBUTE_NAME}.
         */
        public enum ArgumentClass {
            /** Classification constant for integral values */
            INTEGER,
            /** Classification constant for floating point values */
            SSE,
            /** Classification constant for x87 floating point values */
            X87,
            /** Classification constant for {@code complex long double} values */
            COMPLEX_87,
            /** Classification constant for machine pointer values */
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
         * The name of the Windows linker
         * @see ForeignLinker#name
         */
        public final static String NAME = "Windows";

        /**
         * The name of the layout attribute (see {@link MemoryLayout#attributes()} used to mark variadic parameters. The
         * attribute value must be a boolean.
         */
        public final static String VARARGS_ATTRIBUTE_NAME = "abi/windows/varargs";

        /**
         * The name of the layout attribute (see {@link MemoryLayout#attributes()} used for ABI classification. The
         * attribute value must be an enum constant from {@link ArgumentClass}.
         */
        public final static String CLASS_ATTRIBUTE_NAME = "abi/windows/class";

        /**
         * Constants used for ABI classification. They are referred to by the layout attribute {@link #CLASS_ATTRIBUTE_NAME}.
         */
        public enum ArgumentClass {
            /** Classification constant for integral values */
            INTEGER,
            /** Classification constant for floating point values */
            FLOAT,
            /** Classification constant for machine pointer values */
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

        /**
         * Return a new memory layout which describes a variadic parameter to be passed to a function.
         * @param layout the original parameter layout.
         * @return a layout which is the same as {@code layout}, except for the extra attribute {@link #VARARGS_ATTRIBUTE_NAME},
         * which is set to {@code true}.
         */
        public static ValueLayout asVarArg(ValueLayout layout) {
            return layout.withAttribute(VARARGS_ATTRIBUTE_NAME, true);
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
         * The name of the AArch64 linker
         * @see ForeignLinker#name
         */
        public final static String NAME = "AArch64";

        /**
         * The name of the layout attribute (see {@link MemoryLayout#attributes()} used for ABI classification. The
         * attribute value must be an enum constant from {@link ArgumentClass}.
         */
        public static final String CLASS_ATTRIBUTE_NAME = "abi/aarch64/class";

        /**
         * Constants used for ABI classification. They are referred to by the layout attribute {@link #CLASS_ATTRIBUTE_NAME}.
         */
        public enum ArgumentClass {
            /** Classification constant for machine integral values */
            INTEGER,
            /** Classification constant for machine floating point values */
            VECTOR,
            /** Classification constant for machine pointer values */
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

        /**
         * The {@code va_list} native type, as it is passed to a function.
         */
        public static final MemoryLayout C_VA_LIST = AArch64.C_POINTER;
    }

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
    public static MemorySegment toCString(String str, NativeScope scope) {
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
    public static MemorySegment toCString(String str, Charset charset, NativeScope scope) {
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
        return SharedUtils.toJavaStringInternal(NativeMemorySegmentImpl.EVERYTHING, addr.toRawLongValue(), Charset.defaultCharset());
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
        return SharedUtils.toJavaStringInternal(NativeMemorySegmentImpl.EVERYTHING, addr.toRawLongValue(), charset);
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
     * associated with {@code addr}, or if {@code addr} is associated with a segment that is <em>not alive</em>.
     */
    public static String toJavaString(MemorySegment addr) {
        return SharedUtils.toJavaStringInternal(addr, 0L, Charset.defaultCharset());
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
     * associated with {@code addr}, or if {@code addr} is associated with a segment that is <em>not alive</em>.
     */
    public static String toJavaString(MemorySegment addr, Charset charset) {
        return SharedUtils.toJavaStringInternal(addr, 0L, charset);
    }

    private static void copy(MemorySegment addr, byte[] bytes) {
        var heapSegment = MemorySegment.ofArray(bytes);
        addr.copyFrom(heapSegment);
        MemoryAccess.setByteAtOffset(addr, bytes.length, (byte)0);
    }

    private static MemorySegment toCString(byte[] bytes) {
        MemorySegment segment = MemorySegment.allocateNative(bytes.length + 1, 1L);
        copy(segment, bytes);
        return segment;
    }

    private static MemorySegment toCString(byte[] bytes, NativeScope scope) {
        MemorySegment addr = scope.allocate(bytes.length + 1, 1L);
        copy(addr, bytes);
        return addr;
    }
}
