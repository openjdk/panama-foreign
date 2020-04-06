/*
 *  Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
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
import jdk.internal.foreign.abi.UpcallStubs;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.ByteOrder;

/**
 * This class models a system application binary interface (ABI).
 *
 * Instances of this class can be obtained by calling {@link Foreign#getSystemABI()}
 */
public interface SystemABI {

    /**
     * Obtain a method handle which can be used to call a given native function.
     *
     * @param symbol downcall symbol.
     * @param type the method type.
     * @param function the function descriptor.
     * @return the downcall method handle.
     */
    MethodHandle downcallHandle(MemoryAddress symbol, MethodType type, FunctionDescriptor function);

    /**
     * Obtain the pointer to a native stub which
     * can be used to upcall into a given method handle.
     *
     * @param target the target method handle.
     * @param function the function descriptor.
     * @return the upcall symbol.
     */
    MemoryAddress upcallStub(MethodHandle target, FunctionDescriptor function);

    /**
     * Frees an upcall stub given it's memory address.
     *
     * @param address the memory address of the upcall stub, returned from
     *                {@link SystemABI#upcallStub(MethodHandle, FunctionDescriptor)}.
     * @throws IllegalArgumentException if the given address is not a valid upcall stub address.
     */
    default void freeUpcallStub(MemoryAddress address) {
        UpcallStubs.freeUpcallStub(address);
    }

    /**
     * Returns the name of this ABI.
     *
     * @return the name
     */
    String name();

    /**
     * The {@code _Bool} native type.
     */
     ValueLayout C_BOOL = Utils.pick(SysV.C_BOOL, Win64.C_BOOL, AArch64.C_BOOL);

    /**
     * The {@code unsigned char} native type.
     */
     ValueLayout C_UCHAR = Utils.pick(SysV.C_UCHAR, Win64.C_UCHAR, AArch64.C_UCHAR);

    /**
     * The {@code signed char} native type.
     */
     ValueLayout C_SCHAR = Utils.pick(SysV.C_SCHAR, Win64.C_SCHAR, AArch64.C_SCHAR) ;

    /**
     * The {@code char} native type.
     */
     ValueLayout C_CHAR = Utils.pick(SysV.C_CHAR, Win64.C_CHAR, AArch64.C_CHAR);

    /**
     * The {@code short} native type.
     */
     ValueLayout C_SHORT = Utils.pick(SysV.C_SHORT, Win64.C_SHORT, AArch64.C_SHORT);

    /**
     * The {@code unsigned short} native type.
     */
     ValueLayout C_USHORT = Utils.pick(SysV.C_USHORT, Win64.C_USHORT, AArch64.C_USHORT);

    /**
     * The {@code int} native type.
     */
     ValueLayout C_INT = Utils.pick(SysV.C_INT, Win64.C_INT, AArch64.C_INT);

    /**
     * The {@code unsigned int} native type.
     */
     ValueLayout C_UINT = Utils.pick(SysV.C_UINT, Win64.C_UINT, AArch64.C_UINT);

    /**
     * The {@code long} native type.
     */
     ValueLayout C_LONG = Utils.pick(SysV.C_LONG, Win64.C_LONG, AArch64.C_LONG);

    /**
     * The {@code unsigned long} native type.
     */
     ValueLayout C_ULONG = Utils.pick(SysV.C_ULONG, Win64.C_ULONG, AArch64.C_ULONG);

    /**
     * The {@code long long} native type.
     */
     ValueLayout C_LONGLONG = Utils.pick(SysV.C_LONGLONG, Win64.C_LONGLONG, AArch64.C_LONGLONG);

    /**
     * The {@code unsigned long long} native type.
     */
     ValueLayout C_ULONGLONG = Utils.pick(SysV.C_ULONGLONG, Win64.C_ULONGLONG, AArch64.C_ULONGLONG);

    /**
     * The {@code float} native type.
     */
     ValueLayout C_FLOAT = Utils.pick(SysV.C_FLOAT, Win64.C_FLOAT, AArch64.C_FLOAT);

    /**
     * The {@code double} native type.
     */
     ValueLayout C_DOUBLE = Utils.pick(SysV.C_DOUBLE, Win64.C_DOUBLE, AArch64.C_DOUBLE);

    /**
     * The {@code long double} native type.
     */
    ValueLayout C_LONGDOUBLE = Utils.pick(SysV.C_LONGDOUBLE, Win64.C_LONGDOUBLE, AArch64.C_LONGDOUBLE);

    /**
     * The {@code T*} native type.
     */
     ValueLayout C_POINTER = Utils.pick(SysV.C_POINTER, Win64.C_POINTER, AArch64.C_POINTER);

    /**
     * This class defines layout constants modelling standard primitive types supported by the x64 SystemV ABI.
     */
    final class SysV {
        private SysV() {
            //just the one
        }

        /**
         * The name of the SysV ABI
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
         * The {@code unsigned char} native type.
         */
        public static final ValueLayout C_UCHAR = MemoryLayouts.BITS_8_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);


        /**
         * The {@code signed char} native type.
         */
        public static final ValueLayout C_SCHAR = MemoryLayouts.BITS_8_LE
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
         * The {@code unsigned short} native type.
         */
        public static final ValueLayout C_USHORT = MemoryLayouts.BITS_16_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code unsigned int} native type.
         */
        public static final ValueLayout C_UINT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code unsigned long} native type.
         */
        public static final ValueLayout C_ULONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);


        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code unsigned long long} native type.
         */
        public static final ValueLayout C_ULONGLONG = MemoryLayouts.BITS_64_LE
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
    }

    /**
     * This class defines layout constants modelling standard primitive types supported by the x64 Windows ABI.
     */
    final class Win64 {

        private Win64() {
            //just the one
        }

        /**
         * The name of the Windows ABI
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
         * The {@code unsigned char} native type.
         */
        public static final ValueLayout C_UCHAR = MemoryLayouts.BITS_8_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code signed char} native type.
         */
        public static final ValueLayout C_SCHAR = MemoryLayouts.BITS_8_LE
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
         * The {@code unsigned short} native type.
         */
        public static final ValueLayout C_USHORT = MemoryLayouts.BITS_16_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code unsigned int} native type.
         */
        public static final ValueLayout C_UINT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code unsigned long} native type.
         */
        public static final ValueLayout C_ULONG = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code unsigned long long} native type.
         */
        public static final ValueLayout C_ULONGLONG = MemoryLayouts.BITS_64_LE
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

        public static ValueLayout asVarArg(ValueLayout l) {
            return l.withAttribute(VARARGS_ATTRIBUTE_NAME, "true");
        }
    }

    /**
     * This class defines layout constants modelling standard primitive types supported by the AArch64 ABI.
     */
    final class AArch64 {

        private AArch64() {
            //just the one
        }

        /**
         * The name of the AArch64 ABI
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
         * The {@code unsigned char} native type.
         */
        public static final ValueLayout C_UCHAR = MemoryLayouts.BITS_8_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code signed char} native type.
         */
        public static final ValueLayout C_SCHAR = MemoryLayouts.BITS_8_LE
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
         * The {@code unsigned short} native type.
         */
        public static final ValueLayout C_USHORT = MemoryLayouts.BITS_16_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code unsigned int} native type.
         */
        public static final ValueLayout C_UINT = MemoryLayouts.BITS_32_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code unsigned long} native type.
         */
        public static final ValueLayout C_ULONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAttribute(CLASS_ATTRIBUTE_NAME, ArgumentClass.INTEGER);

        /**
         * The {@code unsigned long long} native type.
         */
        public static final ValueLayout C_ULONGLONG = MemoryLayouts.BITS_64_LE
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
