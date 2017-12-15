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

public abstract class DoubleVector<S extends Vector.Shape<Vector<?, S>>> implements Vector<Double, S> {

    private static final Double128Vector DOUBLE128 = new Double128Vector(Long2.ZERO);
    private static final Double256Vector DOUBLE256 = new Double256Vector(Long4.ZERO);


    public static Vector<Double, Shapes.S128Bit> double128FromArray(double[] ary, int offset) {
        return DOUBLE128.fromArray(ary, offset);
    }

    public static Vector<Double, Shapes.S256Bit> double256FromArray(double[] ary, int offset) {
        return DOUBLE256.fromArray(ary, offset);
    }

    public static Vector<Double, Shapes.S128Bit> double128FromArray(double[] ary) {
        return DOUBLE128.fromArray(ary, 0);
    }

    public abstract Vector<Double, S> fromArray(double[] ary, int offset);

    public Vector<Double, S> fromArray(double[] ary) {
        return fromArray(ary, 0);
    }

    public abstract void intoArray(double[] ary, int offset);

    public void intoArray(double[] ary) {
        intoArray(ary, 0);
    }

    public double[] toArray() {
        double[] res = new double[this.length()];
        intoArray(res);
        return res;
    }
}
