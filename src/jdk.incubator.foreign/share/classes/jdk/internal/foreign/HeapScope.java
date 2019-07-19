/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.misc.Unsafe;

import jdk.incubator.foreign.MemorySegment;

public class HeapScope extends MemorySegmentImpl.Scope {

    private Object base;

    private HeapScope(Object base) {
        this.base = base;
    }

    @Override
    public Object base() {
        return base;
    }

    @Override
    public void close() {
        super.close();
        base = null;
    }

    public static MemorySegment ofArray(Object array) {
        int size = java.lang.reflect.Array.getLength(array);
        long base = baseOffset(array.getClass());
        long scale = indexScale(array.getClass());
        return new MemorySegmentImpl(base, size * scale, 0, new HeapScope(array));
    }
    
    static long baseOffset(Class<?> cl) {
        cl = cl.componentType();
        if (cl == byte.class) {
            return Unsafe.ARRAY_BYTE_BASE_OFFSET;
        } else if (cl == char.class) {
            return Unsafe.ARRAY_CHAR_BASE_OFFSET;
        } else if (cl == short.class) {
            return Unsafe.ARRAY_SHORT_BASE_OFFSET;
        } else if (cl == int.class) {
            return Unsafe.ARRAY_INT_BASE_OFFSET;
        } else if (cl == float.class) {
            return Unsafe.ARRAY_FLOAT_BASE_OFFSET;
        } else if (cl == long.class) {
            return Unsafe.ARRAY_LONG_BASE_OFFSET;
        } else if (cl == double.class) {
            return Unsafe.ARRAY_DOUBLE_BASE_OFFSET;
        } else {
            throw new IllegalStateException("bad type" + cl);
        }
    }
    
    static long indexScale(Class<?> cl) {
        cl = cl.componentType();
        if (cl == byte.class) {
            return Unsafe.ARRAY_BYTE_INDEX_SCALE;
        } else if (cl == char.class) {
            return Unsafe.ARRAY_CHAR_INDEX_SCALE;
        } else if (cl == short.class) {
            return Unsafe.ARRAY_SHORT_INDEX_SCALE;
        } else if (cl == int.class) {
            return Unsafe.ARRAY_INT_INDEX_SCALE;
        } else if (cl == float.class) {
            return Unsafe.ARRAY_FLOAT_INDEX_SCALE;
        } else if (cl == long.class) {
            return Unsafe.ARRAY_LONG_INDEX_SCALE;
        } else if (cl == double.class) {
            return Unsafe.ARRAY_DOUBLE_INDEX_SCALE;
        } else {
            throw new IllegalStateException("bad type" + cl);
        }
    }
}
