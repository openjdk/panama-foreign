/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */
package com.oracle.vector;

public abstract class LongVector<S extends Vector.Shape<Vector<?, S>>> implements Vector<Long, S> {

    private static final Long128Vector LONG128 = new Long128Vector(Long2.ZERO);
    private static final Long256Vector LONG256 = new Long256Vector(Long4.ZERO);

    public static Vector<Long, Shapes.S128Bit> long128FromArray(long[] ary, int offset) {
        return LONG128.fromArray(ary, offset);
    }

    public static Vector<Long, Shapes.S256Bit> long256FromArray(long[] ary, int offset) {
        return LONG256.fromArray(ary, offset);
    }

    public static Vector<Long, Shapes.S128Bit> byte128FromArray(long[] ary) {
        return LONG128.fromArray(ary, 0);
    }

    public static Vector<Long, Shapes.S128Bit> zero128() {
        return LONG128;
    }

    public abstract Vector<Long, S> fromArray(long[] ary, int offset);

    public Vector<Long, S> fromArray(long[] ary) {
        return fromArray(ary, 0);
    }

    public abstract void intoArray(long[] ary, int offset);

    public void intoArray(long[] ary) {
        intoArray(ary, 0);
    }

    public long[] toArray() {
        long[] res = new long[this.length()];
        //int[] res = new int[8];
        intoArray(res);
        return res;
    }
}
