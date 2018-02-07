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
public final class FloatVectorHelper {

    private FloatVectorHelper() {}

    public interface BinaryOp {
        float apply(int i, float a, float b);
    }

    public interface UnaryOp {
        float apply(int i, float a);
    }

    public static <S extends Vector.Shape>
    FloatVector<S> map(FloatVector<S> va, FloatVector<S> vb, BinaryOp op) {
        return va.bOp(vb, (i, a, b) -> op.apply(i, a, b));
    }

    public static <S extends Vector.Shape>
    FloatVector<S> map(FloatVector<S> va, FloatVector<S> vb, Vector.Mask<Float,S> m, BinaryOp op) {
        return va.bOp(vb, m, (i, a, b) -> op.apply(i, a, b));
    }

    public static <S extends Vector.Shape>
    FloatVector<S> map(FloatVector<S> va, UnaryOp op) {
        return va.uOp((i, a) -> op.apply(i, a));
    }

    public static <S extends Vector.Shape>
    FloatVector<S> map(FloatVector<S> va, Vector.Mask<Float,S> m, UnaryOp op) {
        return va.uOp(m, (i, a) -> op.apply(i, a));
    }


    public static <S extends Vector.Shape>
    FloatVector<S> ceil(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.ceil((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> ceil(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.ceil((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> copySign(FloatVector<S> va, FloatVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (float) Math.copySign((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> copySign(FloatVector<S> va, FloatVector<S> vb, Vector.Mask<Float,S> m) {
        return va.bOp(vb, m, (i, a, b) -> (float) Math.copySign((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> floor(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.floor((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> floor(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.floor((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> getExponent(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.getExponent((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> getExponent(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.getExponent((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> IEEEremainder(FloatVector<S> va, FloatVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (float) Math.IEEEremainder((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> IEEEremainder(FloatVector<S> va, FloatVector<S> vb, Vector.Mask<Float,S> m) {
        return va.bOp(vb, m, (i, a, b) -> (float) Math.IEEEremainder((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> nextAfter(FloatVector<S> va, FloatVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (float) Math.nextAfter((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> nextAfter(FloatVector<S> va, FloatVector<S> vb, Vector.Mask<Float,S> m) {
        return va.bOp(vb, m, (i, a, b) -> (float) Math.nextAfter((double) a, (double) b));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> nextDown(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.nextDown((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> nextDown(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.nextDown((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> nextUp(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.nextUp((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> nextUp(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.nextUp((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> rint(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.rint((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> rint(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.rint((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> round(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.round((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> round(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.round((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> scalb(Vector<Integer, S> o) {
        throw  new UnsupportedOperationException("Scalb not implemented yet.");
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> scalb(Vector<Integer, S> o, Vector.Mask<Float,S> m) {
        throw  new UnsupportedOperationException("Scalb not implemented yet.");
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> signum(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.signum((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> signum(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.signum((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> toDegrees(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.toDegrees((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> toDegrees(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.toDegrees((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> toRadians(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.toRadians((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> toRadians(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.toRadians((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> ulp(FloatVector<S> va) {
        return va.uOp((i, a) -> (float) Math.ulp((double) a));
    }

    public static <S extends Vector.Shape> 
    FloatVector<S> ulp(FloatVector<S> va, Vector.Mask<Float,S> m) {
        return va.uOp(m, (i, a) -> (float) Math.ulp((double) a));
    }

}
