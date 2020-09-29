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
package jdk.internal.foreign;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.CLinker.CValueLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.ValueLayout;

import java.nio.ByteOrder;
import java.util.Map;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static jdk.incubator.foreign.MemoryLayouts.ADDRESS;

public class PlatformLayouts {
    public static <Z extends MemoryLayout> Z pick(Z sysv, Z win64, Z aarch64) {
        return switch (CABI.current()) {
            case SysV -> sysv;
            case Win64 -> win64;
            case AArch64 -> aarch64;
        };
    }

    public static MemoryLayout asVarArg(MemoryLayout ml) {
        if (CABI.current() == CABI.Win64) {
            return Win64.asVarArg(ml);
        }
        return ml;
    }

    private static CValueLayout ofChar(ByteOrder order, long bitSize) {
        return new CValueLayout(CValueLayout.Kind.CHAR, order, bitSize, bitSize, Map.of());
    }

    private static CValueLayout ofShort(ByteOrder order, long bitSize) {
        return new CValueLayout(CValueLayout.Kind.SHORT, order, bitSize, bitSize, Map.of());
    }

    private static CValueLayout ofInt(ByteOrder order, long bitSize) {
        return new CValueLayout(CValueLayout.Kind.INT, order, bitSize, bitSize, Map.of());
    }

    private static CValueLayout ofLong(ByteOrder order, long bitSize) {
        return new CValueLayout(CValueLayout.Kind.LONG, order, bitSize, bitSize, Map.of());
    }

    private static CValueLayout ofLongLong(ByteOrder order, long bitSize) {
        return new CValueLayout(CValueLayout.Kind.LONGLONG, order, bitSize, bitSize, Map.of());
    }

    private static CValueLayout ofFloat(ByteOrder order, long bitSize) {
        return new CValueLayout(CValueLayout.Kind.FLOAT, order, bitSize, bitSize, Map.of());
    }

    private static CValueLayout ofDouble(ByteOrder order, long bitSize) {
        return new CValueLayout(CValueLayout.Kind.DOUBLE, order, bitSize, bitSize, Map.of());
    }

    private static CValueLayout ofLongDouble(ByteOrder order, long bitSize) {
        return new CValueLayout(CValueLayout.Kind.LONGDOUBLE, order, bitSize, bitSize, Map.of());
    }

    /**
     * Creates a new CValueLayout with the {@code POINTER} kind
     *
     * @param order the byte order of the layout
     * @param bitSize the size, in bits, of the layout
     * @return the newly created CValueLayout
     */
    public static CValueLayout ofPointer(ByteOrder order, long bitSize) {
        return new CValueLayout(CValueLayout.Kind.POINTER, order, bitSize, bitSize, Map.of());
    }

    /**
     * This class defines layout constants modelling standard primitive types supported by the x64 SystemV ABI.
     */
    public static final class SysV {
        private SysV() {
            //just the one
        }

        /**
         * The {@code char} native type.
         */
        public static final CValueLayout C_CHAR = ofChar(LITTLE_ENDIAN, 8);

        /**
         * The {@code short} native type.
         */
        public static final CValueLayout C_SHORT = ofShort(LITTLE_ENDIAN, 16);

        /**
         * The {@code int} native type.
         */
        public static final CValueLayout C_INT = ofInt(LITTLE_ENDIAN, 32);

        /**
         * The {@code long} native type.
         */
        public static final CValueLayout C_LONG = ofLong(LITTLE_ENDIAN, 64);

        /**
         * The {@code long long} native type.
         */
        public static final CValueLayout C_LONGLONG = ofLongLong(LITTLE_ENDIAN, 64);

        /**
         * The {@code float} native type.
         */
        public static final CValueLayout C_FLOAT = ofFloat(LITTLE_ENDIAN, 32);

        /**
         * The {@code double} native type.
         */
        public static final CValueLayout C_DOUBLE = ofDouble(LITTLE_ENDIAN, 64);

        /**
         * The {@code long double} native type.
         */
        public static final CValueLayout C_LONGDOUBLE = ofLongDouble(LITTLE_ENDIAN, 128);

        /**
         * The {@code T*} native type.
         */
        public static final CValueLayout C_POINTER = ofPointer(LITTLE_ENDIAN, ADDRESS.bitSize());

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
         * The name of the layout attribute (see {@link MemoryLayout#attributes()} used to mark variadic parameters. The
         * attribute value must be a boolean.
         */
        public final static String VARARGS_ATTRIBUTE_NAME = "abi/windows/varargs";

        /**
         * The {@code char} native type.
         */
        public static final CValueLayout C_CHAR = ofChar(LITTLE_ENDIAN, 8);

        /**
         * The {@code short} native type.
         */
        public static final CValueLayout C_SHORT = ofShort(LITTLE_ENDIAN, 16);

        /**
         * The {@code int} native type.
         */
        public static final CValueLayout C_INT = ofInt(LITTLE_ENDIAN, 32);
        /**
         * The {@code long} native type.
         */
        public static final CValueLayout C_LONG = ofLong(LITTLE_ENDIAN, 32);

        /**
         * The {@code long long} native type.
         */
        public static final CValueLayout C_LONGLONG = ofLongLong(LITTLE_ENDIAN, 64);

        /**
         * The {@code float} native type.
         */
        public static final CValueLayout C_FLOAT = ofFloat(LITTLE_ENDIAN, 32);

        /**
         * The {@code double} native type.
         */
        public static final CValueLayout C_DOUBLE = ofDouble(LITTLE_ENDIAN, 64);

        /**
         * The {@code long double} native type.
         */
        public static final CValueLayout C_LONGDOUBLE = ofLongDouble(LITTLE_ENDIAN, 64);

        /**
         * The {@code T*} native type.
         */
        public static final CValueLayout C_POINTER = ofPointer(LITTLE_ENDIAN, ADDRESS.bitSize());

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
        public static MemoryLayout asVarArg(MemoryLayout layout) {
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
         * The {@code char} native type.
         */
        public static final CValueLayout C_CHAR = ofChar(LITTLE_ENDIAN, 8);

        /**
         * The {@code short} native type.
         */
        public static final CValueLayout C_SHORT = ofShort(LITTLE_ENDIAN, 16);

        /**
         * The {@code int} native type.
         */
        public static final CValueLayout C_INT = ofInt(LITTLE_ENDIAN, 32);

        /**
         * The {@code long} native type.
         */
        public static final CValueLayout C_LONG = ofLong(LITTLE_ENDIAN, 64);

        /**
         * The {@code long long} native type.
         */
        public static final CValueLayout C_LONGLONG = ofLongLong(LITTLE_ENDIAN, 64);

        /**
         * The {@code float} native type.
         */
        public static final CValueLayout C_FLOAT = ofFloat(LITTLE_ENDIAN, 32);

        /**
         * The {@code double} native type.
         */
        public static final CValueLayout C_DOUBLE = ofDouble(LITTLE_ENDIAN, 64);

        /**
         * The {@code long double} native type.
         */
        public static final CValueLayout C_LONGDOUBLE = ofLongDouble(LITTLE_ENDIAN, 128);

        /**
         * The {@code T*} native type.
         */
        public static final CValueLayout C_POINTER = ofPointer(LITTLE_ENDIAN, ADDRESS.bitSize());

        /**
         * The {@code va_list} native type, as it is passed to a function.
         */
        public static final MemoryLayout C_VA_LIST = AArch64.C_POINTER;
    }
}
