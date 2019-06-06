/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
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

package java.foreign;

import java.foreign.layout.Value;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;

/**
 * This class defines constants modelling standard types.
 */
public final class NativeTypes {

    private static final boolean isHostLE;
    private static final boolean isWindows;
    private static final boolean isAArch64;
    private static final boolean isX86;

    static {
        String arch = System.getProperty("os.arch");
        isX86 = arch.equals("amd64") || arch.equals("x86_64");
        isAArch64 = arch.equals("aarch64");
        isHostLE = isAArch64 || isX86;
        String os = System.getProperty("os.name");
        isWindows = os.startsWith("Windows");
    }

    private static <T> LayoutType<T> pick(LayoutType<T> x86Windows, LayoutType<T> x86SysV,
                                          LayoutType<T> aarch64) {
        if (isX86) {
            return isWindows ? x86Windows : x86SysV;
        } else if (isAArch64) {
            return aarch64;
        } else {
            throw new UnsupportedOperationException("Unsupported platform");
        }
    }

    /**
     * An 8-bit signed integral type, platform endian format.
     */
    public static final LayoutType<Byte> INT8 = isHostLE ?
            LittleEndian.INT8 : BigEndian.INT8;

    /**
     * An 8-bit unsigned integral type, platform endian format.
     */
    public static final LayoutType<Byte> UINT8 = isHostLE ?
            LittleEndian.UINT8 : BigEndian.UINT8;

    /**
     * A 16-bit signed integral type, platform endian format.
     */
    public static final LayoutType<Short> INT16 = isHostLE ?
            LittleEndian.INT16 : BigEndian.INT16;

    /**
     * A 16-bit unsigned integral type, platform endian format.
     */
    public static final LayoutType<Short> UINT16 = isHostLE ?
            LittleEndian.UINT16 : BigEndian.UINT16;

    /**
     * An 32-bit signed integral type, platform endian format.
     */
    public static final LayoutType<Integer> INT32 = isHostLE ?
            LittleEndian.INT32 : BigEndian.INT32;

    /**
     * A 32-bit unsigned integral type, platform endian format.
     */
    public static final LayoutType<Integer> UINT32 = isHostLE ?
            LittleEndian.UINT32 : BigEndian.UINT32;

    /**
     * A 64-bit signed integral type, platform endian format.
     */
    public static final LayoutType<Long> INT64 = isHostLE ?
            LittleEndian.INT64 : BigEndian.INT64;

    /**
     * A 64-bit unsigned integral type, platform endian format.
     */
    public static final LayoutType<Long> UINT64 = isHostLE ?
            LittleEndian.UINT64 : BigEndian.UINT64;

    /**
     * A single precision floating point type, according to the IEEE 754 standard,
     * platform endian format.
     */
    public static final LayoutType<Float> IEEE_FLOAT32 = isHostLE ?
            LittleEndian.IEEE_FLOAT32 : BigEndian.IEEE_FLOAT32;

    /**
     * A double precision floating point type, according to the IEEE 754 standard,
     * platform endian format.
     */
    public static final LayoutType<Double> IEEE_FLOAT64 = isHostLE ?
            LittleEndian.IEEE_FLOAT64 : BigEndian.IEEE_FLOAT64;

    /**
     * An extended precision floating point type, according to the IEEE 754 standard,
     * platform endian format.
     */
    public static final LayoutType<Double> IEEE_FLOAT80 = isHostLE ?
            LittleEndian.IEEE_FLOAT80 : BigEndian.IEEE_FLOAT80;

    /**
     * The {@code void} type.
     */
    public static final LayoutType<Void> VOID = LayoutType.ofVoid(Value.ofUnsignedInt(0));

    /**
     * The {@code _Bool} native type, according to system ABI.
     */
    public static final LayoutType<Byte> BOOL =
            pick(LittleEndian.WinABI.BOOL, LittleEndian.SysVABI.BOOL,
                 LittleEndian.AArch64ABI.BOOL);

    /**
     * The {@code unsigned char} native type, according to system ABI.
     */
    public static final LayoutType<Byte> UCHAR =
            pick(LittleEndian.WinABI.UCHAR, LittleEndian.SysVABI.UCHAR,
                 LittleEndian.AArch64ABI.UCHAR);

    /**
     * The {@code signed char} native type, according to system ABI.
     */
    public static final LayoutType<Byte> SCHAR =
            pick(LittleEndian.WinABI.SCHAR, LittleEndian.SysVABI.SCHAR,
                 LittleEndian.AArch64ABI.SCHAR);

    /**
     * The {@code char} native type, according to system ABI.
     */
    public static final LayoutType<Byte> CHAR =
            pick(LittleEndian.WinABI.CHAR, LittleEndian.SysVABI.CHAR,
                 LittleEndian.AArch64ABI.CHAR);

    /**
     * The {@code short} native type, according to system ABI.
     */
    public static final LayoutType<Short> SHORT =
            pick(LittleEndian.WinABI.SHORT, LittleEndian.SysVABI.SHORT,
                 LittleEndian.AArch64ABI.SHORT);

    /**
     * The {@code unsigned short} native type, according to system ABI.
     */
    public static final LayoutType<Short> USHORT =
            pick(LittleEndian.WinABI.USHORT, LittleEndian.SysVABI.USHORT,
                 LittleEndian.AArch64ABI.USHORT);

    /**
     * The {@code int} native type, according to system ABI.
     */
    public static final LayoutType<Integer> INT =
            pick(LittleEndian.WinABI.INT, LittleEndian.SysVABI.INT,
                 LittleEndian.AArch64ABI.INT);

    /**
     * The {@code unsigned int} native type, according to system ABI.
     */
    public static final LayoutType<Integer> UINT =
            pick(LittleEndian.WinABI.UINT, LittleEndian.SysVABI.UINT,
                 LittleEndian.AArch64ABI.UINT);

    /**
     * The {@code long} native type, according to system ABI.
     */
    public static final LayoutType<Long> LONG =
            pick(LittleEndian.WinABI.LONG, LittleEndian.SysVABI.LONG,
                 LittleEndian.WinABI.LONG);

    /**
     * The {@code unsigned long} native type, according to system ABI.
     */
    public static final LayoutType<Long> ULONG =
            pick(LittleEndian.WinABI.ULONG, LittleEndian.SysVABI.ULONG,
                 LittleEndian.AArch64ABI.ULONG);

    /**
     * The {@code long long} native type, according to system ABI.
     */
    public static final LayoutType<Long> LONGLONG =
            pick(LittleEndian.WinABI.LONGLONG, LittleEndian.SysVABI.LONGLONG,
                 LittleEndian.AArch64ABI.LONGLONG);

    /**
     * The {@code unsigned long long} native type, according to system ABI.
     */
    public static final LayoutType<Long> ULONGLONG =
            pick(LittleEndian.WinABI.ULONGLONG, LittleEndian.SysVABI.ULONGLONG,
                 LittleEndian.AArch64ABI.ULONGLONG);

    /**
     * The {@code float} native type, according to system ABI.
     */
    public static final LayoutType<Float> FLOAT =
            pick(LittleEndian.WinABI.FLOAT, LittleEndian.SysVABI.FLOAT,
                 LittleEndian.AArch64ABI.FLOAT);

    /**
     * The {@code double} native type, according to system ABI.
     */
    public static final LayoutType<Double> DOUBLE =
            pick(LittleEndian.WinABI.DOUBLE, LittleEndian.SysVABI.DOUBLE,
                 LittleEndian.AArch64ABI.DOUBLE);

    /**
     * The {@code T*} native type, according to system ABI.
     */
    public static final LayoutType<Pointer<Void>> POINTER =
            pick(LittleEndian.WinABI.POINTER, LittleEndian.SysVABI.POINTER,
                 LittleEndian.AArch64ABI.POINTER);

    /**
     * This class defines constants modelling standard primitive types in big endian format.
     */
    public final static class BigEndian {

        /**
         * An 8-bit signed integral type, big endian format.
         */
        public static final LayoutType<Byte> INT8 = LayoutType.ofByte(
                Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 8));

        /**
         * An 8-bit unsigned integral type, big endian format.
         */
        public static final LayoutType<Byte> UINT8 = LayoutType.ofByte(
                Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 8));

        /**
         * A 16-bit signed integral type, big endian format.
         */
        public static final LayoutType<Short> INT16 = LayoutType.ofShort(
                Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 16));

        /**
         * A 16-bit unsigned integral type, big endian format.
         */
        public static final LayoutType<Short> UINT16 = LayoutType.ofShort(
                Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 16));

        /**
         * A 32-bit signed integral type, big endian format.
         */
        public static final LayoutType<Integer> INT32 = LayoutType.ofInt(
                Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 32));

        /**
         * A 32-bit unsigned integral type, big endian format.
         */
        public static final LayoutType<Integer> UINT32 = LayoutType.ofInt(
                Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 32));

        /**
         * A 64-bit signed integral type, big endian format.
         */
        public static final LayoutType<Long> INT64 = LayoutType.ofLong(
                Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 64));

        /**
         * A 64-bit unsigned integral type, big endian format.
         */
        public static final LayoutType<Long> UINT64 = LayoutType.ofLong(
                Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 64));

        /**
         * A single precision floating point type, according to the IEEE 754 standard,
         * big endian format.
         */
        public static final LayoutType<Float> IEEE_FLOAT32 = LayoutType.ofFloat(
                Value.ofFloatingPoint(Value.Endianness.BIG_ENDIAN, 32));

        /**
         * A double precision floating point type, according to the IEEE 754 standard,
         * big endian format.
         */
        public static final LayoutType<Double> IEEE_FLOAT64 = LayoutType.ofDouble(
                Value.ofFloatingPoint(Value.Endianness.BIG_ENDIAN, 64));

        /**
         * An extended precision floating point type, according to the IEEE 754 standard,
         * big endian format.
         */
        public static final LayoutType<Double> IEEE_FLOAT80 = LayoutType.ofDouble(
                Value.ofFloatingPoint(Value.Endianness.BIG_ENDIAN, 80));
    }

    /**
     * This class defines constants modelling standard primitive types in little endian format.
     */
    public final static class LittleEndian {

        /**
         * An 8-bit signed integral type, little endian format.
         */
        public static final LayoutType<Byte> INT8 = LayoutType.ofByte(
                Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 8));

        /**
         * An 8-bit unsigned integral type, little endian format.
         */
        public static final LayoutType<Byte> UINT8 = LayoutType.ofByte(
                Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 8));

        /**
         * A 16-bit signed integral type, little endian format.
         */
        public static final LayoutType<Short> INT16 = LayoutType.ofShort(
                Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 16));

        /**
         * A 16-bit unsigned integral type, little endian format.
         */
        public static final LayoutType<Short> UINT16 = LayoutType.ofShort(
                Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 16));

        /**
         * A 32-bit signed integral type, little endian format.
         */
        public static final LayoutType<Integer> INT32 = LayoutType.ofInt(
                Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 32));

        /**
         * A 32-bit unsigned integral type, little endian format.
         */
        public static final LayoutType<Integer> UINT32 = LayoutType.ofInt(
                Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 32));

        /**
         * A 64-bit signed integral type, little endian format.
         */
        public static final LayoutType<Long> INT64 = LayoutType.ofLong(
                Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 64));

        /**
         * A 64-bit unsigned integral type, little endian format.
         */
        public static final LayoutType<Long> UINT64 = LayoutType.ofLong(
                Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 64));

        /**
         * A single precision floating point type, according to the IEEE 754 standard,
         * little endian format.
         */
        public static final LayoutType<Float> IEEE_FLOAT32 = LayoutType.ofFloat(
                Value.ofFloatingPoint(Value.Endianness.LITTLE_ENDIAN, 32));

        /**
         * A double precision floating point type, according to the IEEE 754 standard,
         * little endian format.
         */
        public static final LayoutType<Double> IEEE_FLOAT64 = LayoutType.ofDouble(
                Value.ofFloatingPoint(Value.Endianness.LITTLE_ENDIAN, 64));

        /**
         * An extended precision floating point type, according to the IEEE 754 standard,
         * little endian format.
         */
        public static final LayoutType<Double> IEEE_FLOAT80 = LayoutType.ofDouble(
                Value.ofFloatingPoint(Value.Endianness.LITTLE_ENDIAN, 80));

        /**
         * This class defines constants modelling standard primitive types supported by the x64 SystemV ABI.
         */
        public static final class SysVABI {

            /**
             * The {@code _Bool} native type.
             */
            public static final LayoutType<Byte> BOOL = UINT8;

            /**
             * The {@code unsigned char} native type.
             */
            public static final LayoutType<Byte> UCHAR = UINT8;

            /**
             * The {@code signed char} native type.
             */
            public static final LayoutType<Byte> SCHAR = INT8;

            /**
             * The {@code char} native type.
             */
            public static final LayoutType<Byte> CHAR = SCHAR;

            /**
             * The {@code short} native type.
             */
            public static final LayoutType<Short> SHORT = INT16;

            /**
             * The {@code unsigned short} native type.
             */
            public static final LayoutType<Short> USHORT = UINT16;

            /**
             * The {@code int} native type.
             */
            public static final LayoutType<Integer> INT = INT32;

            /**
             * The {@code unsigned int} native type.
             */
            public static final LayoutType<Integer> UINT = UINT32;

            /**
             * The {@code long} native type.
             */
            public static final LayoutType<Long> LONG = INT64;

            /**
             * The {@code unsigned long} native type.
             */
            public static final LayoutType<Long> ULONG = UINT64;

            /**
             * The {@code long long} native type.
             */
            public static final LayoutType<Long> LONGLONG = INT64;

            /**
             * The {@code unsigned long long} native type.
             */
            public static final LayoutType<Long> ULONGLONG = UINT64;

            /**
             * The {@code float} native type.
             */
            public static final LayoutType<Float> FLOAT = IEEE_FLOAT32;

            /**
             * The {@code double} native type.
             */
            public static final LayoutType<Double> DOUBLE = IEEE_FLOAT64;

            /**
             * The {@code long double} native type.
             */
            public static final LayoutType<Double> LONGDOUBLE = IEEE_FLOAT80;

            /**
             * The {@code T*} native type.
             */
            public static final LayoutType<Pointer<Void>> POINTER =
                    NativeTypes.VOID.pointer(Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 64));
        }

        /**
         * This class defines constants modelling standard primitive types supported by the x64 Windows ABI.
         */
        public static final class WinABI {

            /**
             * The {@code _Bool} native type.
             */
            public static final LayoutType<Byte> BOOL = UINT8;

            /**
             * The {@code unsigned char} native type.
             */
            public static final LayoutType<Byte> UCHAR = UINT8;

            /**
             * The {@code signed char} native type.
             */
            public static final LayoutType<Byte> SCHAR = INT8;

            /**
             * The {@code char} native type.
             */
            public static final LayoutType<Byte> CHAR = SCHAR;

            /**
             * The {@code short} native type.
             */
            public static final LayoutType<Short> SHORT = INT16;

            /**
             * The {@code unsigned short} native type.
             */
            public static final LayoutType<Short> USHORT = UINT16;

            /**
             * The {@code int} native type.
             */
            public static final LayoutType<Integer> INT = INT32;

            /**
             * The {@code unsigned int} native type.
             */
            public static final LayoutType<Integer> UINT = UINT32;

            /**
             * The {@code long} native type.
             */
            public static final LayoutType<Long> LONG = LayoutType.ofLong(INT32.layout());

            /**
             * The {@code unsigned long} native type.
             */
            public static final LayoutType<Long> ULONG = LayoutType.ofLong(UINT32.layout());

            /**
             * The {@code long long} native type.
             */
            public static final LayoutType<Long> LONGLONG = INT64;

            /**
             * The {@code unsigned long long} native type.
             */
            public static final LayoutType<Long> ULONGLONG = UINT64;

            /**
             * The {@code float} native type.
             */
            public static final LayoutType<Float> FLOAT = IEEE_FLOAT32;

            /**
             * The {@code double} native type.
             */
            public static final LayoutType<Double> DOUBLE = IEEE_FLOAT64;

            /**
             * The {@code T*} native type.
             */
            public static final LayoutType<Pointer<Void>> POINTER =
                    NativeTypes.VOID.pointer(Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 64));
        }

        /**
         * This class defines constants modelling standard primitive types supported by the AArch64 ABI.
         */
        public static final class AArch64ABI {

            /**
             * The {@code _Bool} native type.
             */
            public static final LayoutType<Byte> BOOL = UINT8;

            /**
             * The {@code unsigned char} native type.
             */
            public static final LayoutType<Byte> UCHAR = UINT8;

            /**
             * The {@code signed char} native type.
             */
            public static final LayoutType<Byte> SCHAR = INT8;

            /**
             * The {@code char} native type.
             */
            public static final LayoutType<Byte> CHAR = UCHAR;

            /**
             * The {@code short} native type.
             */
            public static final LayoutType<Short> SHORT = INT16;

            /**
             * The {@code unsigned short} native type.
             */
            public static final LayoutType<Short> USHORT = UINT16;

            /**
             * The {@code int} native type.
             */
            public static final LayoutType<Integer> INT = INT32;

            /**
             * The {@code unsigned int} native type.
             */
            public static final LayoutType<Integer> UINT = UINT32;

            /**
             * The {@code long} native type.
             */
            public static final LayoutType<Long> LONG = INT64;

            /**
             * The {@code unsigned long} native type.
             */
            public static final LayoutType<Long> ULONG = UINT64;

            /**
             * The {@code long long} native type.
             */
            public static final LayoutType<Long> LONGLONG = INT64;

            /**
             * The {@code unsigned long long} native type.
             */
            public static final LayoutType<Long> ULONGLONG = UINT64;

            /**
             * The {@code float} native type.
             */
            public static final LayoutType<Float> FLOAT = IEEE_FLOAT32;

            /**
             * The {@code double} native type.
             */
            public static final LayoutType<Double> DOUBLE = IEEE_FLOAT64;

            /**
             * The {@code long double} native type.
             */
            public static final LayoutType<Double> LONGDOUBLE = LayoutType.ofDouble(
                    Value.ofFloatingPoint(Value.Endianness.LITTLE_ENDIAN, 128));

            /**
             * The {@code T*} native type.
             */
            public static final LayoutType<Pointer<Void>> POINTER =
                    NativeTypes.VOID.pointer(Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 64));
        }
    }
}
