/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.vector;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public final class Shapes {

    public static final S64Bit SHAPE_64_BIT = new S64Bit();

    public static final S128Bit SHAPE_128_BIT = new S128Bit();

    public static final S256Bit SHAPE_256_BIT = new S256Bit();

    public static final S512Bit SHAPE_512_BIT = new S512Bit();

    private Shapes() { }

    public static final class S64Bit implements Vector.Shape<Vector<?, S64Bit>> {
        private S64Bit() {}

        @Override
        public int bitSize() {
            return 64;
        }

        @Override
        public int length(Vector.Species<?, ?> s) {
            return bitSize() / s.elementSize();
        }
    }

    public static final class S128Bit implements Vector.Shape<Vector<?, S128Bit>> {
        private S128Bit() {}

        @Override
        public int bitSize() {
            return 128;
        }

        @Override
        public int length(Vector.Species<?, ?> s) {
            return bitSize() / s.elementSize();
        }
    }

    public static final class S256Bit implements Vector.Shape<Vector<?, S256Bit>> {
        private S256Bit() {}

        @Override
        public int bitSize() {
            return 256;
        }

        @Override
        public int length(Vector.Species<?, ?> s) {
            return bitSize() / s.elementSize();
        }
    }

    public static final class S512Bit implements Vector.Shape<Vector<?, S512Bit>> {
        private S512Bit() {}

        @Override
        public int bitSize() {
            return 512;
        }

        @Override
        public int length(Vector.Species<?, ?> s) {
            return bitSize() / s.elementSize();
        }
    }

    public static final Unsafe UNSAFE;

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = (Unsafe) unsafeField.get(null);
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    static long pack(Byte[] v, int offset) {
        byte[] _v = new byte[v.length - offset];
        for (int i = 0; i < (v.length - offset); i++)
            _v[i] = v[i + offset];
        return pack(_v, 0);
    }

    static long pack(byte[] v, int offset) {
        // Little Endian decoding
        long r = UNSAFE.getLong(v, UNSAFE.ARRAY_BYTE_BASE_OFFSET + offset);
//        long r = 0;
//        r |= (long)(v[offset + 7] & 0xFF) << 56L;
//        r |= (long)(v[offset + 6] & 0xFF) << 48L;
//        r |= (long)(v[offset + 5] & 0xFF) << 40;
//        r |= (long)(v[offset + 4] & 0xFF) << 32;
//        r |= (long)(v[offset + 3] & 0xFF) << 24;
//        r |= (long)(v[offset + 2] & 0xFF) << 16;
//        r |= (long)(v[offset + 1] & 0xFF) << 8;
//        r |= (long)(v[offset + 0] & 0xFF);
        return r;
    }
}
