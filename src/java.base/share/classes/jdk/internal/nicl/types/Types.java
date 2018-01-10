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

public class Types {
    public final static Type BYTE = new Scalar('o');
    public final static Type SHORT = new Scalar('s');
    public final static Type INT = new Scalar('i');
    public final static Type LONG = new Scalar('l');
    public final static Type LONG_LONG = new Scalar('q');
    public final static Type FLOAT = new Scalar('f');
    public final static Type DOUBLE = new Scalar('d');
    public final static Type LONG_DOUBLE = new Scalar('e');
    public final static Type VOID = new Scalar('V');
    public final static Type CHAR = new Scalar('c');
    public final static Type BOOLEAN = new Scalar('B');
    public final static Type POINTER = new Pointer(null);
    public final static Type INT8 = new Scalar('i', Type.Endianness.NATIVE, 8);
    public final static Type INT16 = new Scalar('i', Type.Endianness.NATIVE, 16);
    public final static Type INT32 = new Scalar('i', Type.Endianness.NATIVE, 32);
    public final static Type INT64 = new Scalar('i', Type.Endianness.NATIVE, 64);
    public final static Type M64 = new Scalar('v', Type.Endianness.NATIVE, 64);
    public final static Type M128 = new Scalar('v', Type.Endianness.NATIVE, 128);
    public final static Type M256 = new Scalar('v', Type.Endianness.NATIVE, 256);
    public final static Type M512 = new Scalar('v', Type.Endianness.NATIVE, 512);

    public static class UNSIGNED {
        public final static Type BYTE = new Scalar('O');
        public final static Type SHORT = new Scalar('S');
        public final static Type INT = new Scalar('I');
        public final static Type LONG = new Scalar('L');
        public final static Type LONG_LONG = new Scalar('Q');
        public final static Type FLOAT = new Scalar('F');
        public final static Type DOUBLE = new Scalar('D');
        public final static Type LONG_DOUBLE = new Scalar('E');
        public final static Type INT8 = new Scalar('I', Type.Endianness.NATIVE, 8);
        public final static Type INT16 = new Scalar('I', Type.Endianness.NATIVE, 16);
        public final static Type INT32 = new Scalar('I', Type.Endianness.NATIVE, 32);
        public final static Type INT64 = new Scalar('I', Type.Endianness.NATIVE, 64);
    }

    public static class BE {
        public final static Type SHORT = new Scalar('s', Type.Endianness.BIG);
        public final static Type INT = new Scalar('i', Type.Endianness.BIG);
        public final static Type LONG = new Scalar('l', Type.Endianness.BIG);
        public final static Type LONG_LONG = new Scalar('q', Type.Endianness.BIG);
        public final static Type FLOAT = new Scalar('f', Type.Endianness.BIG);
        public final static Type DOUBLE = new Scalar('d', Type.Endianness.BIG);
        public final static Type LONG_DOUBLE = new Scalar('e', Type.Endianness.BIG);
        public final static Type INT16 = new Scalar('i', Type.Endianness.BIG, 16);
        public final static Type INT32 = new Scalar('i', Type.Endianness.BIG, 32);
        public final static Type INT64 = new Scalar('i', Type.Endianness.BIG, 64);

        public static class UNSIGNED {
            public final static Type SHORT = new Scalar('S', Type.Endianness.BIG);
            public final static Type INT = new Scalar('I', Type.Endianness.BIG);
            public final static Type LONG = new Scalar('L', Type.Endianness.BIG);
            public final static Type LONG_LONG = new Scalar('Q', Type.Endianness.BIG);
            public final static Type FLOAT = new Scalar('F', Type.Endianness.BIG);
            public final static Type DOUBLE = new Scalar('D', Type.Endianness.BIG);
            public final static Type LONG_DOUBLE = new Scalar('E', Type.Endianness.BIG);
            public final static Type INT16 = new Scalar('I', Type.Endianness.BIG, 16);
            public final static Type INT32 = new Scalar('I', Type.Endianness.BIG, 32);
            public final static Type INT64 = new Scalar('I', Type.Endianness.BIG, 64);
        }
    }

    public static class LE {
        public final static Type SHORT = new Scalar('s', Type.Endianness.LITTLE);
        public final static Type INT = new Scalar('i', Type.Endianness.LITTLE);
        public final static Type LONG = new Scalar('l', Type.Endianness.LITTLE);
        public final static Type LONG_LONG = new Scalar('q', Type.Endianness.LITTLE);
        public final static Type FLOAT = new Scalar('f', Type.Endianness.LITTLE);
        public final static Type DOUBLE = new Scalar('d', Type.Endianness.LITTLE);
        public final static Type LONG_DOUBLE = new Scalar('e', Type.Endianness.LITTLE);
        public final static Type INT16 = new Scalar('i', Type.Endianness.LITTLE, 16);
        public final static Type INT32 = new Scalar('i', Type.Endianness.LITTLE, 32);
        public final static Type INT64 = new Scalar('i', Type.Endianness.LITTLE, 64);

        public static class UNSIGNED {
            public final static Type SHORT = new Scalar('S', Type.Endianness.LITTLE);
            public final static Type INT = new Scalar('I', Type.Endianness.LITTLE);
            public final static Type LONG = new Scalar('L', Type.Endianness.LITTLE);
            public final static Type LONG_LONG = new Scalar('Q', Type.Endianness.LITTLE);
            public final static Type FLOAT = new Scalar('F', Type.Endianness.LITTLE);
            public final static Type DOUBLE = new Scalar('D', Type.Endianness.LITTLE);
            public final static Type LONG_DOUBLE = new Scalar('E', Type.Endianness.LITTLE);
            public final static Type INT16 = new Scalar('I', Type.Endianness.LITTLE, 16);
            public final static Type INT32 = new Scalar('I', Type.Endianness.LITTLE, 32);
            public final static Type INT64 = new Scalar('I', Type.Endianness.LITTLE, 64);
        }
    }
}
