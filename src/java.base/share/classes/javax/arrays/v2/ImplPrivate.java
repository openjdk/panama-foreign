/*
 *  Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */
package javax.arrays.v2;

import java.lang.reflect.Field;
import jdk.internal.misc.Unsafe;

class ImplPrivate { // PACKAGE PROTECTION
    public static abstract class InsertWithEndianness {
        abstract int shiftDistance(int atByte);

        public final int insertShort(int atByte, int container, short value) {
            int shift = shiftDistance(atByte);
            int mask = 0xFFFF << shift;
            return (container & ~mask) | ((0xFFFF & value) << shift);
        }

        public final int insertByte(int atByte, int container, byte value) {
            int shift = shiftDistance(atByte);
            int mask = 0xFF << shift;
            return (container & ~mask) | ((0xFF & value) << shift);
        }
    }
    private final static class InsertWithBigEndianness extends InsertWithEndianness {
        // 0, 1, 2, 3 -> 24, 12, 8, 0
        @Override
        int shiftDistance(int atByte) {
            int shift = (3 - atByte) * 8;
            return shift;
        }
    }
    private final static  class InsertWithLittleEndianness extends InsertWithEndianness {
        // 0, 1, 2, 3 -> 0, 8, 16, 24
        @Override
        int shiftDistance(int atByte) {
            int shift = atByte * 8;
            return shift;
        }
    }
    @SuppressWarnings("restriction")
    static final Unsafe u = getUnsafe(); // PACKAGE PROTECTION
    static final InsertWithEndianness inserter = isBigEndian() ?
            new InsertWithBigEndianness() : new InsertWithLittleEndianness();

    @SuppressWarnings("restriction")
    private static Unsafe  getUnsafe() {
        Unsafe unsafe =
                (Unsafe) getField(jdk.internal.misc.Unsafe.class, null, "theUnsafe");
        return unsafe;
    }

    @SuppressWarnings("restriction")
    private static <T> Object getField(Class<T> cl, Object obj, String field_name) {
        try {
            Field field = cl.getDeclaredField(field_name);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | SecurityException |
                IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            throw new Error();
        }
    }

    @SuppressWarnings("restriction")
    private static boolean isBigEndian() {
        long addr = u.allocateMemory(8);
        u.putLong(addr, 0x1122334455667788L);
        boolean rc =  u.getByte(addr) == 0x11;
        u.freeMemory(addr);
        return rc;
    }

    public static final int ARRAY_BOOLEAN_BASE_OFFSET = Unsafe.ARRAY_BOOLEAN_BASE_OFFSET;
    public static final int ARRAY_BYTE_BASE_OFFSET = Unsafe.ARRAY_BYTE_BASE_OFFSET;
    public static final int ARRAY_CHAR_BASE_OFFSET = Unsafe.ARRAY_CHAR_BASE_OFFSET;
    public static final int ARRAY_SHORT_BASE_OFFSET = Unsafe.ARRAY_SHORT_BASE_OFFSET;
    public static final int ARRAY_INT_BASE_OFFSET = Unsafe.ARRAY_INT_BASE_OFFSET;
    public static final int ARRAY_LONG_BASE_OFFSET = Unsafe.ARRAY_LONG_BASE_OFFSET;
    public static final int ARRAY_FLOAT_BASE_OFFSET = Unsafe.ARRAY_FLOAT_BASE_OFFSET;
    public static final int ARRAY_DOUBLE_BASE_OFFSET = Unsafe.ARRAY_DOUBLE_BASE_OFFSET;
    public static final int ARRAY_OBJECT_BASE_OFFSET = Unsafe.ARRAY_OBJECT_BASE_OFFSET;

    public static final int ARRAY_BOOLEAN_INDEX_SCALE = Unsafe.ARRAY_BOOLEAN_INDEX_SCALE;
    public static final int ARRAY_BYTE_INDEX_SCALE = Unsafe.ARRAY_BYTE_INDEX_SCALE;
    public static final int ARRAY_CHAR_INDEX_SCALE = Unsafe.ARRAY_CHAR_INDEX_SCALE;
    public static final int ARRAY_SHORT_INDEX_SCALE = Unsafe.ARRAY_SHORT_INDEX_SCALE;
    public static final int ARRAY_INT_INDEX_SCALE = Unsafe.ARRAY_INT_INDEX_SCALE;
    public static final int ARRAY_LONG_INDEX_SCALE = Unsafe.ARRAY_LONG_INDEX_SCALE;
    public static final int ARRAY_FLOAT_INDEX_SCALE = Unsafe.ARRAY_FLOAT_INDEX_SCALE;
    public static final int ARRAY_DOUBLE_INDEX_SCALE = Unsafe.ARRAY_DOUBLE_INDEX_SCALE;
    public static final int ARRAY_OBJECT_INDEX_SCALE = Unsafe.ARRAY_OBJECT_INDEX_SCALE;

}
