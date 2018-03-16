/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
package jdk.incubator.vector;

/**
 * Operations on vectors that are not intrinsic candidates
 */
@SuppressWarnings("cast")
public final class LongVectorHelper {

    private LongVectorHelper() {}

    public interface BinaryOp {
        long apply(int i, long a, long b);
    }

    public interface UnaryOp {
        long apply(int i, long a);
    }

    public static <S extends Vector.Shape>
    LongVector<S> map(LongVector<S> va, LongVector<S> vb, BinaryOp op) {
        return va.bOp(vb, (i, a, b) -> op.apply(i, a, b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> map(LongVector<S> va, LongVector<S> vb, Vector.Mask<Long,S> m, BinaryOp op) {
        return va.bOp(vb, m, (i, a, b) -> op.apply(i, a, b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> map(LongVector<S> va, UnaryOp op) {
        return va.uOp((i, a) -> op.apply(i, a));
    }

    public static <S extends Vector.Shape>
    LongVector<S> map(LongVector<S> va, Vector.Mask<Long,S> m, UnaryOp op) {
        return va.uOp(m, (i, a) -> op.apply(i, a));
    }

    public static <S extends Vector.Shape>
    LongVector<S> div(LongVector<S> va, LongVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (long) (a / b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> div(LongVector<S> va, LongVector<S> vb, Vector.Mask<Long, S> m) {
        return va.bOp(vb, m, (i, a, b) -> (long) (a / b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> mod(LongVector<S> va, LongVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (long) (a % b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> mod(LongVector<S> va, LongVector<S> vb, Vector.Mask<Long, S> m) {
        return va.bOp(vb, m, (i, a, b) -> (long) (a % b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> addExact(LongVector<S> va, LongVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> Math.addExact(a, b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> addExact(LongVector<S> va, LongVector<S> vb, Vector.Mask<Long,S> m) {
        return va.bOp(vb, m, (i, a, b) -> Math.addExact(a, b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> decrementExact(LongVector<S> va) {
        return va.uOp((i, a) -> Math.decrementExact(a));
    }

    public static <S extends Vector.Shape>
    LongVector<S> decrementExact(LongVector<S> va, Vector.Mask<Long,S> m) {
        return va.uOp(m, (i, a) -> Math.decrementExact(a));
    }

    public static <S extends Vector.Shape>
    LongVector<S> incrementExact(LongVector<S> va) {
        return va.uOp((i, a) -> Math.incrementExact(a));
    }

    public static <S extends Vector.Shape>
    LongVector<S> incrementExact(LongVector<S> va, Vector.Mask<Long,S> m) {
        return va.uOp(m, (i, a) -> Math.incrementExact(a));
    }

    public static <S extends Vector.Shape>
    LongVector<S> multiplyExact(LongVector<S> va, LongVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> Math.multiplyExact(a, b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> multiplyExact(LongVector<S> va, LongVector<S> vb, Vector.Mask<Long, S> m) {
        return va.bOp(vb, m, (i, a, b) -> Math.multiplyExact(a, b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> negateExact(LongVector<S> va) {
        return va.uOp((i, a) -> Math.negateExact(a));
    }

    public static <S extends Vector.Shape>
    LongVector<S> negateExact(LongVector<S> va, Vector.Mask<Long, S> m) {
        return va.uOp(m, (i, a) -> Math.negateExact(a));
    }

    public static <S extends Vector.Shape>
    LongVector<S> subtractExtract(LongVector<S> va, LongVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> Math.subtractExact(a, b));
    }

    public static <S extends Vector.Shape>
    LongVector<S> subtractExtract(LongVector<S> va, LongVector<S> vb, Vector.Mask<Long,S> m) {
        return va.bOp(vb, m, (i, a, b) -> Math.subtractExact(a, b));
    }
    /*
    // @@@ Shape specific
    // long,int-> long : S, S / 2 -> S
    // Long512Vector, Int256Vector -> Long512Vector

    public static <S extends Vector.Shape>
    LongVector<S> multiplyExact(LongVector<S> va, LongVector<S> vb) {
        throw new UnsupportedOperationException("multiplyExact not supported on Float");
    }

    public static <S extends Vector.Shape>
    LongVector<S> multiplyExact(LongVector<S> va, LongVector<S> vb, Vector.Mask<Long, S> m) {
        throw new UnsupportedOperationException("multiplyExact not supported on Float");
    }

    // @@@ Shape specific
    // long->int
    // Long512Vector -> Int256Vector

    public
    Vector<Integer, Shapes.S128Bit> toIntExact() {
        throw new UnsupportedOperationException("toIntExact not implemented.");
    }

    // Top 64 of 128 bits

    public static <S extends Vector.Shape>
    LongVector<S> multiplyHigh(LongVector<S> va, LongVector<S> vb) {
        throw new UnsupportedOperationException("multiplyHigh not supported on Float");
    }

    public static <S extends Vector.Shape>
    LongVector<S> multiplyHigh(LongVector<S> va, LongVector<S> vb, Vector.Mask<Long, S> m) {
        throw new UnsupportedOperationException("multiplyHigh not supported on Float");
    }
    */


}
