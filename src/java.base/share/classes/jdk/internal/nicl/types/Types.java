/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.nicl.types;

import java.nicl.layout.Address;
import java.nicl.layout.Layout;
import java.nicl.layout.Value;

public class Types {
    public final static Layout BYTE = Value.ofSignedInt(8);
    public final static Layout SHORT = Value.ofSignedInt(16);
    public final static Layout INT = Value.ofSignedInt(32);
    public final static Layout LONG = Value.ofSignedInt(64);
    public final static Layout LONG_LONG = Value.ofSignedInt(64);
    public final static Layout FLOAT = Value.ofFloatingPoint(32);
    public final static Layout DOUBLE = Value.ofFloatingPoint(64);
    public final static Layout LONG_DOUBLE = Value.ofFloatingPoint(128);
    public final static Layout CHAR = Value.ofSignedInt(8);
    public final static Layout BOOLEAN = Value.ofUnsignedInt(8);
    public final static Layout POINTER = Address.ofVoid(64);
    public final static Layout INT8 = Value.ofSignedInt(8);
    public final static Layout INT16 = Value.ofSignedInt(16);
    public final static Layout INT32 = Value.ofSignedInt(32);
    public final static Layout INT64 = Value.ofSignedInt(64);
    public final static Layout INT128 = Value.ofSignedInt(128);
    public final static Layout VOID = Value.ofUnsignedInt(0);

    public static class UNSIGNED {
        public final static Layout BYTE = Value.ofUnsignedInt(8);
        public final static Layout SHORT = Value.ofUnsignedInt(16);
        public final static Layout INT = Value.ofUnsignedInt(32);
        public final static Layout LONG = Value.ofUnsignedInt(64);
        public final static Layout LONG_LONG = Value.ofUnsignedInt(64);
        public final static Layout INT8 = Value.ofUnsignedInt(8);
        public final static Layout INT16 = Value.ofUnsignedInt(16);
        public final static Layout INT32 = Value.ofUnsignedInt(32);
        public final static Layout INT64 = Value.ofUnsignedInt(64);
        public final static Layout INT128 = Value.ofUnsignedInt(128);
    }

    public static class BE {
        public final static Layout SHORT = Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 16);
        public final static Layout INT = Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 32);
        public final static Layout LONG = Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 64);
        public final static Layout LONG_LONG = Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 64);
        public final static Layout FLOAT = Value.ofFloatingPoint(Value.Endianness.BIG_ENDIAN, 32);
        public final static Layout DOUBLE = Value.ofFloatingPoint(Value.Endianness.BIG_ENDIAN, 64);
        public final static Layout LONG_DOUBLE = Value.ofFloatingPoint(Value.Endianness.BIG_ENDIAN, 128);
        public final static Layout INT16 = Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 16);
        public final static Layout INT32 = Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 32);
        public final static Layout INT64 = Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 64);
        public final static Layout INT128 = Value.ofSignedInt(Value.Endianness.BIG_ENDIAN, 128);

        public static class UNSIGNED {
            public final static Layout SHORT = Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 16);
            public final static Layout INT = Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 32);
            public final static Layout LONG = Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 64);
            public final static Layout LONG_LONG = Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 64);
            public final static Layout INT16 = Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 16);
            public final static Layout INT32 = Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 32);
            public final static Layout INT64 = Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 64);
            public final static Layout INT128 = Value.ofUnsignedInt(Value.Endianness.BIG_ENDIAN, 128);
        }
    }

    public static class LE {
        public final static Layout SHORT = Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 16);
        public final static Layout INT = Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 32);
        public final static Layout LONG = Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 64);
        public final static Layout LONG_LONG = Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 64);
        public final static Layout FLOAT = Value.ofFloatingPoint(Value.Endianness.LITTLE_ENDIAN, 32);
        public final static Layout DOUBLE = Value.ofFloatingPoint(Value.Endianness.LITTLE_ENDIAN, 64);
        public final static Layout LONG_DOUBLE = Value.ofFloatingPoint(Value.Endianness.LITTLE_ENDIAN, 128);
        public final static Layout INT16 = Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 16);
        public final static Layout INT32 = Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 32);
        public final static Layout INT64 = Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 64);
        public final static Layout INT128 = Value.ofSignedInt(Value.Endianness.LITTLE_ENDIAN, 128);

        public static class UNSIGNED {
            public final static Layout SHORT = Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 16);
            public final static Layout INT = Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 32);
            public final static Layout LONG = Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 64);
            public final static Layout LONG_LONG = Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 64);
            public final static Layout INT16 = Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 16);
            public final static Layout INT32 = Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 32);
            public final static Layout INT64 = Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 64);
            public final static Layout INT128 = Value.ofUnsignedInt(Value.Endianness.LITTLE_ENDIAN, 128);
        }
    }
}
