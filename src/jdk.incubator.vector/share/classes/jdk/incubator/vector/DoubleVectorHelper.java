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
public final class DoubleVectorHelper {

    private DoubleVectorHelper() {}

    public interface BinaryOp {
        double apply(int i, double a, double b);
    }

    public interface UnaryOp {
        double apply(int i, double a);
    }

    public static <S extends Vector.Shape>
    DoubleVector<S> map(DoubleVector<S> va, DoubleVector<S> vb, BinaryOp op) {
        return va.bOp(vb, (i, a, b) -> op.apply(i, a, b));
    }

    public static <S extends Vector.Shape>
    DoubleVector<S> map(DoubleVector<S> va, DoubleVector<S> vb, Vector.Mask<Double,S> m, BinaryOp op) {
        return va.bOp(vb, m, (i, a, b) -> op.apply(i, a, b));
    }

    public static <S extends Vector.Shape>
    DoubleVector<S> map(DoubleVector<S> va, UnaryOp op) {
        return va.uOp((i, a) -> op.apply(i, a));
    }

    public static <S extends Vector.Shape>
    DoubleVector<S> map(DoubleVector<S> va, Vector.Mask<Double,S> m, UnaryOp op) {
        return va.uOp(m, (i, a) -> op.apply(i, a));
    }



    public static <S extends Vector.Shape>
    DoubleVector<S> ceil(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.ceil((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> ceil(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.ceil((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> copySign(DoubleVector<S> va, DoubleVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (double) Math.copySign((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> copySign(DoubleVector<S> va, DoubleVector<S> vb, Vector.Mask<Double,S> m) {
        return va.bOp(vb, m, (i, a, b) -> (double) Math.copySign((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> floor(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.floor((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> floor(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.floor((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> getExponent(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.getExponent((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> getExponent(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.getExponent((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> IEEEremainder(DoubleVector<S> va, DoubleVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (double) Math.IEEEremainder((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> IEEEremainder(DoubleVector<S> va, DoubleVector<S> vb, Vector.Mask<Double,S> m) {
        return va.bOp(vb, m, (i, a, b) -> (double) Math.IEEEremainder((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> nextAfter(DoubleVector<S> va, DoubleVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (double) Math.nextAfter((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> nextAfter(DoubleVector<S> va, DoubleVector<S> vb, Vector.Mask<Double,S> m) {
        return va.bOp(vb, m, (i, a, b) -> (double) Math.nextAfter((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> nextDown(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.nextDown((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> nextDown(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.nextDown((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> nextUp(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.nextUp((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> nextUp(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.nextUp((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> rint(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.rint((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> rint(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.rint((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> round(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.round((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> round(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.round((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> scalb(Vector<Integer, S> o) {
        throw  new UnsupportedOperationException("Scalb not implemented yet.");
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> scalb(Vector<Integer, S> o, Vector.Mask<Double,S> m) {
        throw  new UnsupportedOperationException("Scalb not implemented yet.");
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> signum(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.signum((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> signum(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.signum((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> toDegrees(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.toDegrees((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> toDegrees(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.toDegrees((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> toRadians(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.toRadians((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> toRadians(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.toRadians((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> ulp(DoubleVector<S> va) {
        return va.uOp((i, a) -> (double) Math.ulp((double) a));
    }

    public static <S extends Vector.Shape> 
    DoubleVector<S> ulp(DoubleVector<S> va, Vector.Mask<Double,S> m) {
        return va.uOp(m, (i, a) -> (double) Math.ulp((double) a));
    }

}
