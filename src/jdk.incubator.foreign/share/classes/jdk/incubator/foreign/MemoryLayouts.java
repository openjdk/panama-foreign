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

import jdk.internal.foreign.abi.x64.windows.Windowsx64ABI;

import java.nio.ByteOrder;

import static jdk.incubator.foreign.SystemABI.ABI_AARCH64;
import static jdk.incubator.foreign.SystemABI.ABI_SYSV;
import static jdk.incubator.foreign.SystemABI.ABI_WINDOWS;

/**
 * This class defines useful layout constants. Some of the constants defined in this class are explicit in both
 * size and byte order (see {@link #BITS_64_BE}), and can therefore be used to explicitly and unambiguously specify the
 * contents of a memory segment. Other constants make implicit byte order assumptions (see
 * {@link #JAVA_INT}); as such, these constants make it easy to work with other serialization-centric APIs,
 * such as {@link java.nio.ByteBuffer}.
 */
public final class MemoryLayouts {

    private MemoryLayouts() {
        //just the one, please
    }

    /**
     * A value layout constant with size of one byte, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_8_LE = MemoryLayout.ofValueBits(8, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of two bytes, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_16_LE = MemoryLayout.ofValueBits(16, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of four bytes, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_32_LE = MemoryLayout.ofValueBits(32, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of eight bytes, and byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     */
    public static final ValueLayout BITS_64_LE = MemoryLayout.ofValueBits(64, ByteOrder.LITTLE_ENDIAN);

    /**
     * A value layout constant with size of one byte, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_8_BE = MemoryLayout.ofValueBits(8, ByteOrder.BIG_ENDIAN);

    /**
     * A value layout constant with size of two bytes, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_16_BE = MemoryLayout.ofValueBits(16, ByteOrder.BIG_ENDIAN);

    /**
     * A value layout constant with size of four bytes, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_32_BE = MemoryLayout.ofValueBits(32, ByteOrder.BIG_ENDIAN);

    /**
     * A value layout constant with size of eight bytes, and byte order set to {@link ByteOrder#BIG_ENDIAN}.
     */
    public static final ValueLayout BITS_64_BE = MemoryLayout.ofValueBits(64, ByteOrder.BIG_ENDIAN);

    /**
     * A padding layout constant with size of one byte.
     */
    public static final MemoryLayout PAD_8 = MemoryLayout.ofPaddingBits(8);

    /**
     * A padding layout constant with size of two bytes.
     */
    public static final MemoryLayout PAD_16 = MemoryLayout.ofPaddingBits(16);

    /**
     * A padding layout constant with size of four bytes.
     */
    public static final MemoryLayout PAD_32 = MemoryLayout.ofPaddingBits(32);

    /**
     * A padding layout constant with size of eight bytes.
     */
    public static final MemoryLayout PAD_64 = MemoryLayout.ofPaddingBits(64);

    /**
     * A value layout constant whose size is the same as that of a Java {@code byte}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_BYTE = MemoryLayout.ofValueBits(8, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code char}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_CHAR = MemoryLayout.ofValueBits(16, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code short}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_SHORT = MemoryLayout.ofValueBits(16, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code int}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_INT = MemoryLayout.ofValueBits(32, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code long}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_LONG = MemoryLayout.ofValueBits(64, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code float}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_FLOAT = MemoryLayout.ofValueBits(32, ByteOrder.nativeOrder());

    /**
     * A value layout constant whose size is the same as that of a Java {@code double}, and byte order set to {@link ByteOrder#nativeOrder()}.
     */
    public static final ValueLayout JAVA_DOUBLE = MemoryLayout.ofValueBits(64, ByteOrder.nativeOrder());

    /**
     * The {@code _Bool} native type.
     */
    public static final ValueLayout C_BOOL;

    /**
     * The {@code unsigned char} native type.
     */
    public static final ValueLayout C_UCHAR;

    /**
     * The {@code signed char} native type.
     */
    public static final ValueLayout C_SCHAR ;

    /**
     * The {@code char} native type.
     */
    public static final ValueLayout C_CHAR;

    /**
     * The {@code short} native type.
     */
    public static final ValueLayout C_SHORT;

    /**
     * The {@code unsigned short} native type.
     */
    public static final ValueLayout C_USHORT;

    /**
     * The {@code int} native type.
     */
    public static final ValueLayout C_INT;

    /**
     * The {@code unsigned int} native type.
     */
    public static final ValueLayout C_UINT;

    /**
     * The {@code long} native type.
     */
    public static final ValueLayout C_LONG;

    /**
     * The {@code unsigned long} native type.
     */
    public static final ValueLayout C_ULONG;

    /**
     * The {@code long long} native type.
     */
    public static final ValueLayout C_LONGLONG;

    /**
     * The {@code unsigned long long} native type.
     */
    public static final ValueLayout C_ULONGLONG;

    /**
     * The {@code float} native type.
     */
    public static final ValueLayout C_FLOAT;

    /**
     * The {@code double} native type.
     */
    public static final ValueLayout C_DOUBLE;

    /**
     * The {@code T*} native type.
     */
    public static final ValueLayout C_POINTER;

    static {
        SystemABI abi = SystemABI.getInstance();
        switch (abi.name()) {
            case ABI_SYSV -> {
                C_BOOL = SysV.C_BOOL;
                C_UCHAR = SysV.C_UCHAR;
                C_SCHAR = SysV.C_SCHAR;
                C_CHAR = SysV.C_CHAR;
                C_SHORT = SysV.C_SHORT;
                C_USHORT = SysV.C_USHORT;
                C_INT = SysV.C_INT;
                C_UINT = SysV.C_UINT;
                C_LONG = SysV.C_LONG;
                C_ULONG = SysV.C_ULONG;
                C_LONGLONG = SysV.C_LONGLONG;
                C_ULONGLONG = SysV.C_ULONGLONG;
                C_FLOAT = SysV.C_FLOAT;
                C_DOUBLE = SysV.C_DOUBLE;
                C_POINTER = SysV.C_POINTER;
            }
            case ABI_WINDOWS -> {
                C_BOOL = WinABI.C_BOOL;
                C_UCHAR = WinABI.C_UCHAR;
                C_SCHAR = WinABI.C_SCHAR;
                C_CHAR = WinABI.C_CHAR;
                C_SHORT = WinABI.C_SHORT;
                C_USHORT = WinABI.C_USHORT;
                C_INT = WinABI.C_INT;
                C_UINT = WinABI.C_UINT;
                C_LONG = WinABI.C_LONG;
                C_ULONG = WinABI.C_ULONG;
                C_LONGLONG = WinABI.C_LONGLONG;
                C_ULONGLONG = WinABI.C_ULONGLONG;
                C_FLOAT = WinABI.C_FLOAT;
                C_DOUBLE = WinABI.C_DOUBLE;
                C_POINTER = WinABI.C_POINTER;
            }
            case ABI_AARCH64 -> {
                C_BOOL = AArch64ABI.C_BOOL;
                C_UCHAR = AArch64ABI.C_UCHAR;
                C_SCHAR = AArch64ABI.C_SCHAR;
                C_CHAR = AArch64ABI.C_CHAR;
                C_SHORT = AArch64ABI.C_SHORT;
                C_USHORT = AArch64ABI.C_USHORT;
                C_INT = AArch64ABI.C_INT;
                C_UINT = AArch64ABI.C_UINT;
                C_LONG = AArch64ABI.C_LONG;
                C_ULONG = AArch64ABI.C_ULONG;
                C_LONGLONG = AArch64ABI.C_LONGLONG;
                C_ULONGLONG = AArch64ABI.C_ULONGLONG;
                C_FLOAT = AArch64ABI.C_FLOAT;
                C_DOUBLE = AArch64ABI.C_DOUBLE;
                C_POINTER = AArch64ABI.C_POINTER;
            }
            default -> throw new IllegalStateException("Unsupported ABI: " + abi.name());
        }
    }

    /**
     * This class defines layout constants modelling standard primitive types supported by the x64 SystemV ABI.
     */
    public static final class SysV {
        private SysV() {
            //just the one
        }

        /**
         * The {@code _Bool} native type.
         */
        public static final ValueLayout C_BOOL = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.BOOL);


        /**
         * The {@code unsigned char} native type.
         */
        public static final ValueLayout C_UCHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_CHAR);


        /**
         * The {@code signed char} native type.
         */
        public static final ValueLayout C_SCHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.SIGNED_CHAR);


        /**
         * The {@code char} native type.
         */
        public static final ValueLayout C_CHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.CHAR);

        /**
         * The {@code short} native type.
         */
        public static final ValueLayout C_SHORT = MemoryLayouts.BITS_16_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.SHORT);

        /**
         * The {@code unsigned short} native type.
         */
        public static final ValueLayout C_USHORT = MemoryLayouts.BITS_16_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_SHORT);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.INT);

        /**
         * The {@code unsigned int} native type.
         */
        public static final ValueLayout C_UINT = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_INT);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.LONG);

        /**
         * The {@code unsigned long} native type.
         */
        public static final ValueLayout C_ULONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_LONG);


        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.LONG_LONG);

        /**
         * The {@code unsigned long long} native type.
         */
        public static final ValueLayout C_ULONGLONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_LONG_LONG);

        /**
         * The {@code float} native type.
         */
        public static final ValueLayout C_FLOAT = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.FLOAT);

        /**
         * The {@code double} native type.
         */
        public static final ValueLayout C_DOUBLE = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.DOUBLE);

        /**
         * The {@code long double} native type.
         */
        public static final ValueLayout C_LONGDOUBLE = MemoryLayout.ofValueBits(128, ByteOrder.LITTLE_ENDIAN)
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.LONG_DOUBLE);

        /**
         * The {@code complex long double} native type.
         */
        public static final GroupLayout C_COMPLEX_LONGDOUBLE = MemoryLayout.ofStruct(C_LONGDOUBLE, C_LONGDOUBLE)
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.COMPLEX_LONG_DOUBLE);

        /**
         * The {@code T*} native type.
         */
        public static final ValueLayout C_POINTER = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.POINTER);
    }

    /**
     * This class defines layout constants modelling standard primitive types supported by the x64 Windows ABI.
     */
    public static final class WinABI {
        /**
         * The {@code _Bool} native type.
         */
        public static final ValueLayout C_BOOL = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.BOOL);
        
        /**
         * The {@code unsigned char} native type.
         */
        public static final ValueLayout C_UCHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_CHAR);

        /**
         * The {@code signed char} native type.
         */
        public static final ValueLayout C_SCHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.SIGNED_CHAR);

        /**
         * The {@code char} native type.
         */
        public static final ValueLayout C_CHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.CHAR);

        /**
         * The {@code short} native type.
         */
        public static final ValueLayout C_SHORT = MemoryLayouts.BITS_16_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.SHORT);

        /**
         * The {@code unsigned short} native type.
         */
        public static final ValueLayout C_USHORT = MemoryLayouts.BITS_16_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_SHORT);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.INT);

        /**
         * The {@code unsigned int} native type.
         */
        public static final ValueLayout C_UINT = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_INT);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.LONG);

        /**
         * The {@code unsigned long} native type.
         */
        public static final ValueLayout C_ULONG = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_LONG);

        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.LONG_LONG);

        /**
         * The {@code unsigned long long} native type.
         */
        public static final ValueLayout C_ULONGLONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_LONG_LONG);

        /**
         * The {@code float} native type.
         */
        public static final ValueLayout C_FLOAT = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.FLOAT);

        /**
         * The {@code double} native type.
         */
        public static final ValueLayout C_DOUBLE = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.DOUBLE);

        /**
         * The {@code T*} native type.
         */
        public static final ValueLayout C_POINTER = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.POINTER);

        public static ValueLayout asVarArg(ValueLayout l) {
           return l.withAnnotation(Windowsx64ABI.VARARGS_ANNOTATION_NAME, "true");
        }
    }

    /**
     * This class defines layout constants modelling standard primitive types supported by the AArch64 ABI.
     */
    public static final class AArch64ABI {
        /**
         * The {@code _Bool} native type.
         */
        public static final ValueLayout C_BOOL = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.BOOL);

        /**
         * The {@code unsigned char} native type.
         */
        public static final ValueLayout C_UCHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_CHAR);

        /**
         * The {@code signed char} native type.
         */
        public static final ValueLayout C_SCHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.SIGNED_CHAR);

        /**
         * The {@code char} native type.
         */
        public static final ValueLayout C_CHAR = MemoryLayouts.BITS_8_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.CHAR);

        /**
         * The {@code short} native type.
         */
        public static final ValueLayout C_SHORT = MemoryLayouts.BITS_16_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.SHORT);

        /**
         * The {@code unsigned short} native type.
         */
        public static final ValueLayout C_USHORT = MemoryLayouts.BITS_16_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_SHORT);

        /**
         * The {@code int} native type.
         */
        public static final ValueLayout C_INT = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.INT);

        /**
         * The {@code unsigned int} native type.
         */
        public static final ValueLayout C_UINT = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_INT);

        /**
         * The {@code long} native type.
         */
        public static final ValueLayout C_LONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.LONG);

        /**
         * The {@code unsigned long} native type.
         */
        public static final ValueLayout C_ULONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_LONG);

        /**
         * The {@code long long} native type.
         */
        public static final ValueLayout C_LONGLONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.LONG_LONG);

        /**
         * The {@code unsigned long long} native type.
         */
        public static final ValueLayout C_ULONGLONG = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.UNSIGNED_LONG_LONG);

        /**
         * The {@code float} native type.
         */
        public static final ValueLayout C_FLOAT = MemoryLayouts.BITS_32_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.FLOAT);

        /**
         * The {@code double} native type.
         */
        public static final ValueLayout C_DOUBLE = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.DOUBLE);

        /**
         * The {@code T*} native type.
         */
        public static final ValueLayout C_POINTER = MemoryLayouts.BITS_64_LE
                .withAnnotation(AbstractLayout.NATIVE_TYPE, SystemABI.Type.POINTER);
    }
}
