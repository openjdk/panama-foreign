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

import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.SharedUtils;

import java.nio.ByteOrder;
import java.util.function.Consumer;

/**
 * A set of utilities for working with libraries using the C language/ABI
 */
public class CSupport {
    /**
     * Obtain a linker that uses the de facto C ABI of the current system to do it's linking.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM crash or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * @return a linker for this system.
     * @throws IllegalAccessError if the runtime property {@code foreign.restricted} is not set to either
     * {@code permit}, {@code warn} or {@code debug} (the default value is set to {@code deny}).
     */
    public static ForeignLinker getSystemLinker() {
        Utils.checkRestrictedAccess("CSupport.getSystemLinker");
        return SharedUtils.getSystemLinker();
    }

    public static VaList newVaList(Consumer<VaList.Builder> actions) {
        return SharedUtils.newVaList(actions);
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
         */
        int vargAsInt(MemoryLayout layout);

        /**
         * Reads a value into a {@code long}
         *
         * @param layout the layout of the value
         * @return the value read as an {@code long}
         */
        long vargAsLong(MemoryLayout layout);

        /**
         * Reads a value into a {@code double}
         *
         * @param layout the layout of the value
         * @return the value read as an {@code double}
         */
        double vargAsDouble(MemoryLayout layout);

        /**
         * Reads a value into a {@code MemoryAddress}
         *
         * @param layout the layout of the value
         * @return the value read as an {@code MemoryAddress}
         */
        MemoryAddress vargAsAddress(MemoryLayout layout);

        /**
         * Reads a value into a {@code MemorySegment}
         *
         * @param layout the layout of the value
         * @return the value read as an {@code MemorySegment}
         */
        MemorySegment vargAsSegment(MemoryLayout layout);

        /**
         * Skips a number of va arguments with the given memory layouts.
         *
         * @param layouts the layout of the value
         */
        void skip(MemoryLayout...layouts);

        /**
         * A predicate used to check if this va list is alive,
         * or in other words; if {@code close()} has been called on this
         * va list.
         *
         * @return true if this va list is still alive.
         * @see #close()
         */
        boolean isAlive();

        /**
         * Closes this va list, releasing any resources it was using.
         *
         * @see #isAlive()
         */
        void close();

        /**
         * Copies this va list.
         *
         * @return a copy of this va list.
         */
        VaList copy();

        /**
         * Returns the underlying memory address of this va list.
         *
         * @return the address
         */
        MemoryAddress toAddress();

        /**
         * Constructs a {@code VaList} out of the memory address of a va_list.
         *
         * @param ma the memory address
         * @return the new {@code VaList}.
         */
        static VaList ofAddress(MemoryAddress ma) {
            return SharedUtils.newVaListOfAddress(ma);
        }

        /**
         * A builder interface used to construct a va list.
         */
        interface Builder {

            /**
             * Adds a native value represented as an {@code int} to the va list.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as an {@code int}.
             * @return this builder.
             */
            Builder vargFromInt(MemoryLayout layout, int value);

            /**
             * Adds a native value represented as a {@code long} to the va list.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code long}.
             * @return this builder.
             */
            Builder vargFromLong(MemoryLayout layout, long value);

            /**
             * Adds a native value represented as a {@code double} to the va list.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code double}.
             * @return this builder.
             */
            Builder vargFromDouble(MemoryLayout layout, double value);

            /**
             * Adds a native value represented as a {@code MemoryAddress} to the va list.
             *
             * @param layout the native layout of the value.
             * @param value the value, represented as a {@code MemoryAddress}.
             * @return this builder.
             */
            Builder vargFromAddress(MemoryLayout layout, MemoryAddress value);

            /**
             * Adds a native value represented as a {@code MemorySegment} to the va list.
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
}
