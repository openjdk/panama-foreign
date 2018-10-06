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
public final class IntVectorHelper {

    private IntVectorHelper() {}

    public interface BinaryOp {
        int apply(int i, int a, int b);
    }

    public interface UnaryOp {
        int apply(int i, int a);
    }

    public static <S extends Vector.Shape>
    IntVector<S> map(IntVector<S> va, IntVector<S> vb, BinaryOp op) {
        return va.bOp(vb, (i, a, b) -> op.apply(i, a, b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> map(IntVector<S> va, IntVector<S> vb, Vector.Mask<Integer,S> m, BinaryOp op) {
        return va.bOp(vb, m, (i, a, b) -> op.apply(i, a, b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> map(IntVector<S> va, UnaryOp op) {
        return va.uOp((i, a) -> op.apply(i, a));
    }

    public static <S extends Vector.Shape>
    IntVector<S> map(IntVector<S> va, Vector.Mask<Integer,S> m, UnaryOp op) {
        return va.uOp(m, (i, a) -> op.apply(i, a));
    }

    public static <S extends Vector.Shape>
    IntVector<S> div(IntVector<S> va, IntVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (int) (a / b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> div(IntVector<S> va, IntVector<S> vb, Vector.Mask<Integer, S> m) {
        return va.bOp(vb, m, (i, a, b) -> (int) (a / b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> mod(IntVector<S> va, IntVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (int) (a % b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> mod(IntVector<S> va, IntVector<S> vb, Vector.Mask<Integer, S> m) {
        return va.bOp(vb, m, (i, a, b) -> (int) (a % b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> addExact(IntVector<S> va, IntVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> Math.addExact(a, b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> addExact(IntVector<S> va, IntVector<S> vb, Vector.Mask<Integer,S> m) {
        return va.bOp(vb, m, (i, a, b) -> Math.addExact(a, b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> decrementExact(IntVector<S> va) {
        return va.uOp((i, a) -> Math.decrementExact(a));
    }

    public static <S extends Vector.Shape>
    IntVector<S> decrementExact(IntVector<S> va, Vector.Mask<Integer,S> m) {
        return va.uOp(m, (i, a) -> Math.decrementExact(a));
    }

    public static <S extends Vector.Shape>
    IntVector<S> incrementExact(IntVector<S> va) {
        return va.uOp((i, a) -> Math.incrementExact(a));
    }

    public static <S extends Vector.Shape>
    IntVector<S> incrementExact(IntVector<S> va, Vector.Mask<Integer,S> m) {
        return va.uOp(m, (i, a) -> Math.incrementExact(a));
    }

    public static <S extends Vector.Shape>
    IntVector<S> multiplyExact(IntVector<S> va, IntVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> Math.multiplyExact(a, b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> multiplyExact(IntVector<S> va, IntVector<S> vb, Vector.Mask<Integer, S> m) {
        return va.bOp(vb, m, (i, a, b) -> Math.multiplyExact(a, b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> negateExact(IntVector<S> va) {
        return va.uOp((i, a) -> Math.negateExact(a));
    }

    public static <S extends Vector.Shape>
    IntVector<S> negateExact(IntVector<S> va, Vector.Mask<Integer, S> m) {
        return va.uOp(m, (i, a) -> Math.negateExact(a));
    }

    public static <S extends Vector.Shape>
    IntVector<S> subtractExtract(IntVector<S> va, IntVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> Math.subtractExact(a, b));
    }

    public static <S extends Vector.Shape>
    IntVector<S> subtractExtract(IntVector<S> va, IntVector<S> vb, Vector.Mask<Integer,S> m) {
        return va.bOp(vb, m, (i, a, b) -> Math.subtractExact(a, b));
    }


}
