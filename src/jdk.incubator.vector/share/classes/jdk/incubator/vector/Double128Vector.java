/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Double128Vector extends DoubleVector<Shapes.S128Bit> {
    static final Double128Species SPECIES = new Double128Species();

    static final Double128Vector ZERO = new Double128Vector();

    static final int LENGTH = SPECIES.length();

    private final double[] vec; // Don't access directly, use getElements() instead.

    private double[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Double128Vector() {
        vec = new double[SPECIES.length()];
    }

    Double128Vector(double[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Double128Vector uOp(FUnOp f) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Double128Vector(res);
    }

    @Override
    Double128Vector uOp(Mask<Double, Shapes.S128Bit> o, FUnOp f) {
        double[] vec = getElements();
        double[] res = new double[length()];
        boolean[] mbits = ((Double128Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Double128Vector(res);
    }

    // Binary operator

    @Override
    Double128Vector bOp(Vector<Double, Shapes.S128Bit> o, FBinOp f) {
        double[] res = new double[length()];
        double[] vec1 = this.getElements();
        double[] vec2 = ((Double128Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Double128Vector(res);
    }

    @Override
    Double128Vector bOp(Vector<Double, Shapes.S128Bit> o1, Mask<Double, Shapes.S128Bit> o2, FBinOp f) {
        double[] res = new double[length()];
        double[] vec1 = this.getElements();
        double[] vec2 = ((Double128Vector)o1).getElements();
        boolean[] mbits = ((Double128Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Double128Vector(res);
    }

    // Trinary operator

    @Override
    Double128Vector tOp(Vector<Double, Shapes.S128Bit> o1, Vector<Double, Shapes.S128Bit> o2, FTriOp f) {
        double[] res = new double[length()];
        double[] vec1 = this.getElements();
        double[] vec2 = ((Double128Vector)o1).getElements();
        double[] vec3 = ((Double128Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Double128Vector(res);
    }

    @Override
    Double128Vector tOp(Vector<Double, Shapes.S128Bit> o1, Vector<Double, Shapes.S128Bit> o2, Mask<Double, Shapes.S128Bit> o3, FTriOp f) {
        double[] res = new double[length()];
        double[] vec1 = getElements();
        double[] vec2 = ((Double128Vector)o1).getElements();
        double[] vec3 = ((Double128Vector)o2).getElements();
        boolean[] mbits = ((Double128Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Double128Vector(res);
    }

    @Override
    double rOp(double v, FBinOp f) {
        double[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> add(double o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> add(double o, Mask<Double,Shapes.S128Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> sub(double o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> sub(double o, Mask<Double,Shapes.S128Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> mul(double o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> mul(double o, Mask<Double,Shapes.S128Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> min(double o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> max(double o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S128Bit> equal(double o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S128Bit> notEqual(double o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S128Bit> lessThan(double o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S128Bit> lessThanEq(double o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S128Bit> greaterThan(double o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S128Bit> greaterThanEq(double o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> blend(double o, Mask<Double,Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> div(double o) {
        return div(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> div(double o, Mask<Double,Shapes.S128Bit> m) {
        return div(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Double128Vector div(Vector<Double,Shapes.S128Bit> v, Mask<Double, Shapes.S128Bit> m) {
        return blend(div(v), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> atan2(double o) {
        return atan2(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> atan2(double o, Mask<Double,Shapes.S128Bit> m) {
        return atan2(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> pow(double o) {
        return pow(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> pow(double o, Mask<Double,Shapes.S128Bit> m) {
        return pow(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> fma(double o1, double o2) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> fma(double o1, double o2, Mask<Double,Shapes.S128Bit> m) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> hypot(double o) {
        return hypot(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> hypot(double o, Mask<Double,Shapes.S128Bit> m) {
        return hypot(SPECIES.broadcast(o), m);
    }


    // Unary operations

    @ForceInline
    @Override
    public Double128Vector neg(Mask<Double, Shapes.S128Bit> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public Double128Vector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (double) Math.abs(a)));
    }

    @ForceInline
    @Override
    public Double128Vector abs(Mask<Double, Shapes.S128Bit> m) {
        return blend(abs(), m);
    }

    @Override
    @ForceInline
    public Double128Vector neg() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NEG, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (double) -a));
    }

    @Override
    @ForceInline
    public Double128Vector div(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (double)(a / b)));
    }

    @Override
    @ForceInline
    public Double128Vector sqrt() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_SQRT, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (double) Math.sqrt((double) a)));
    }

    @Override
    @ForceInline
    public Double128Vector exp() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_EXP, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.exp((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector log1p() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_LOG1P, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.log1p((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector log() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_LOG, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.log((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector log10() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_LOG10, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.log10((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector expm1() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_EXPM1, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.expm1((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector cbrt() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_CBRT, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.cbrt((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector sin() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_SIN, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.sin((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector cos() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_COS, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.cos((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector tan() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_TAN, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.tan((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector asin() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ASIN, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.asin((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector acos() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ACOS, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.acos((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector atan() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ATAN, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.atan((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector sinh() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_SINH, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.sinh((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector cosh() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_COSH, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.cosh((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector tanh() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_TANH, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.tanh((double) a)));
    }
    
    @Override
    @ForceInline
    public Double128Vector pow(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_POW, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(Math.pow(a,b))));
    }
    
    @Override
    @ForceInline
    public Double128Vector hypot(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_HYPOT, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(Math.hypot(a,b))));
    }
    
    @Override
    @ForceInline
    public Double128Vector atan2(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ATAN2, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(Math.atan2(a,b))));
    }
    

    // Binary operations

    @Override
    @ForceInline
    public Double128Vector add(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (double)(a + b)));
    }

    @Override
    @ForceInline
    public Double128Vector add(Vector<Double,Shapes.S128Bit> v, Mask<Double, Shapes.S128Bit> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Double128Vector sub(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (double)(a - b)));
    }

    @Override
    @ForceInline
    public Double128Vector sub(Vector<Double,Shapes.S128Bit> v, Mask<Double, Shapes.S128Bit> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Double128Vector mul(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (double)(a * b)));
    }

    @Override
    @ForceInline
    public Double128Vector mul(Vector<Double,Shapes.S128Bit> v, Mask<Double, Shapes.S128Bit> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Double128Vector min(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Double128Vector max(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (double) ((a > b) ? a : b)));
        }


    // Ternary operations

    @Override
    @ForceInline
    public Double128Vector fma(Vector<Double,Shapes.S128Bit> o1, Vector<Double,Shapes.S128Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Double128Vector v1 = (Double128Vector)o1;
        Double128Vector v2 = (Double128Vector)o2;
        return VectorIntrinsics.ternaryOp(
            VECTOR_OP_FMA, Double128Vector.class, double.class, LENGTH,
            this, v1, v2,
            (w1, w2, w3) -> w1.tOp(w2, w3, (i, a, b, c) -> Math.fma(a, b, c)));
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public double addAll() {
        long bits = (long) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_ADD, Double128Vector.class, double.class, LENGTH,
                                this,
                                v -> {
                                    double r = v.rOp((double) 0, (i, a, b) -> (double) (a + b));
                                    return (long)Double.doubleToLongBits(r);
                                });
        return Double.longBitsToDouble(bits);
    }

    @Override
    @ForceInline
    public double subAll() {
        long bits = (long) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_SUB, Double128Vector.class, double.class, LENGTH,
                                this,
                                v -> {
                                    double r = v.rOp((double) 0, (i, a, b) -> (double) (a - b));
                                    return (long)Double.doubleToLongBits(r);
                                });
        return Double.longBitsToDouble(bits);
    }

    @Override
    @ForceInline
    public double mulAll() {
        long bits = (long) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_MUL, Double128Vector.class, double.class, LENGTH,
                                this,
                                v -> {
                                    double r = v.rOp((double) 1, (i, a, b) -> (double) (a * b));
                                    return (long)Double.doubleToLongBits(r);
                                });
        return Double.longBitsToDouble(bits);
    }

    @Override
    @ForceInline
    public double minAll() {
        long bits = (long) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_MIN, Double128Vector.class, double.class, LENGTH,
                                this,
                                v -> {
                                    double r = v.rOp(Double.MAX_VALUE , (i, a, b) -> (double) ((a < b) ? a : b));
                                    return (long)Double.doubleToLongBits(r);
                                });
        return Double.longBitsToDouble(bits);
    }

    @Override
    @ForceInline
    public double maxAll() {
        long bits = (long) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_MAX, Double128Vector.class, double.class, LENGTH,
                                this,
                                v -> {
                                    double r = v.rOp(Double.MIN_VALUE , (i, a, b) -> (double) ((a > b) ? a : b));
                                    return (long)Double.doubleToLongBits(r);
                                });
        return Double.longBitsToDouble(bits);
    }


    @Override
    @ForceInline
    public double addAll(Mask<Double, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((double) 0), m).addAll();
    }

    @Override
    @ForceInline
    public double subAll(Mask<Double, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((double) 0), m).subAll();
    }

    @Override
    @ForceInline
    public double mulAll(Mask<Double, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((double) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public double minAll(Mask<Double, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast(Double.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public double maxAll(Mask<Double, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast(Double.MIN_VALUE), m).maxAll();
    }

    @Override
    @ForceInline
    public Shuffle<Double, Shapes.S128Bit> toShuffle() {
        double[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return SPECIES.shuffleFromArray(sa, 0);
    }

    // Memory operations

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_DOUBLE_INDEX_SCALE);

    @Override
    @ForceInline
    public void intoArray(double[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Double128Vector.class, double.class, LENGTH,
                               a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_DOUBLE_BASE_OFFSET,
                               this,
                               a, ix,
                               (arr, idx, v) -> v.forEach((i, e) -> arr[idx + i] = e));
    }

    @Override
    @ForceInline
    public final void intoArray(double[] a, int ax, Mask<Double, Shapes.S128Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Double128Vector oldVal = SPECIES.fromArray(a, ax);
        Double128Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        // @@@ Endianess
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Double128Vector.class, double.class, LENGTH,
                               a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                               this,
                               a, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                   DoubleBuffer tb = bbc.asDoubleBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public final void intoByteArray(byte[] a, int ix, Mask<Double, Shapes.S128Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Double128Vector oldVal = SPECIES.fromByteArray(a, ix);
        Double128Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        // @@@ Endianess
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        if (bb.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Double128Vector.class, double.class, LENGTH,
                               U.getObject(bb, BYTE_BUFFER_HB), ix + U.getLong(bb, BUFFER_ADDRESS),
                               this,
                               bb, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                   DoubleBuffer tb = bbc.asDoubleBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Double, Shapes.S128Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Double128Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Double128Vector newVal = oldVal.blend(this, m);
        newVal.intoByteBuffer(bb, ix);
    }

    //

    @Override
    public String toString() {
        return Arrays.toString(getElements());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        // @@@ Use equal op
        Double128Vector that = (Double128Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Double128Mask bTest(Vector<Double, Shapes.S128Bit> o, FBinTest f) {
        double[] vec1 = getElements();
        double[] vec2 = ((Double128Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Double128Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Double128Mask equal(Vector<Double, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;

        return VectorIntrinsics.compare(
            BT_eq, Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Double128Mask notEqual(Vector<Double, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;

        return VectorIntrinsics.compare(
            BT_ne, Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Double128Mask lessThan(Vector<Double, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;

        return VectorIntrinsics.compare(
            BT_lt, Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Double128Mask lessThanEq(Vector<Double, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;

        return VectorIntrinsics.compare(
            BT_le, Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Double128Mask greaterThan(Vector<Double, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;

        return (Double128Mask) VectorIntrinsics.compare(
            BT_gt, Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Double128Mask greaterThanEq(Vector<Double, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;

        return VectorIntrinsics.compare(
            BT_ge, Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a >= b));
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        double[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Double, Shapes.S128Bit> o, FUnCon f) {
        boolean[] mbits = ((Double128Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }

    Long128Vector toBits() {
        double[] vec = getElements();
        long[] res = new long[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Double.doubleToLongBits(vec[i]);
        }
        return new Long128Vector(res);
    }


    @Override
    public Double128Vector rotateEL(int j) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Double128Vector(res);
    }

    @Override
    public Double128Vector rotateER(int j) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Double128Vector(res);
    }

    @Override
    public Double128Vector shiftEL(int j) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Double128Vector(res);
    }

    @Override
    public Double128Vector shiftER(int j) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Double128Vector(res);
    }

    @Override
    @ForceInline
    public Double128Vector rearrange(Vector<Double, Shapes.S128Bit> v,
                                  Shuffle<Double, Shapes.S128Bit> s, Mask<Double, Shapes.S128Bit> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    public Double128Vector rearrange(Shuffle<Double, Shapes.S128Bit> s) {
        return uOp((i, a) -> {
            double[] vec = this.getElements();
            int ei = s.getElement(i);
            return vec[ei];
        });
    }

    @Override
    @ForceInline
    public Double128Vector blend(Vector<Double, Shapes.S128Bit> o1, Mask<Double, Shapes.S128Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Double128Vector v = (Double128Vector)o1;
        Double128Mask   m = (Double128Mask)o2;

        return VectorIntrinsics.blend(
            Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public double get(int i) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        long bits = (long) VectorIntrinsics.extract(
                                Double128Vector.class, double.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    double[] vecarr = vec.getElements();
                                    return (long)Double.doubleToLongBits(vecarr[ix]);
                                });
        return Double.longBitsToDouble(bits);
    }

    @Override
    public Double128Vector with(int i, double e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Double128Vector.class, double.class, LENGTH,
                                this, i, (long)Double.doubleToLongBits(e),
                                (v, ix, bits) -> {
                                    double[] res = v.getElements().clone();
                                    res[ix] = Double.longBitsToDouble((long)bits);
                                    return new Double128Vector(res);
                                });
    }

    // Mask

    static final class Double128Mask extends AbstractMask<Double, Shapes.S128Bit> {
        static final Double128Mask TRUE_MASK = new Double128Mask(true);
        static final Double128Mask FALSE_MASK = new Double128Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Double128Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Double128Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Double128Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Double128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Double128Mask(res);
        }

        @Override
        Double128Mask bOp(Mask<Double, Shapes.S128Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Double128Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Double128Mask(res);
        }

        @Override
        public Double128Species species() {
            return SPECIES;
        }

        @Override
        public Double128Vector toVector() {
            double[] res = new double[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (double) (bits[i] ? -1 : 0);
            }
            return new Double128Vector(res);
        }

        // Unary operations

        @Override
        @ForceInline
        public Double128Mask not() {
            return (Double128Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Double128Mask.class, long.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Double128Mask and(Mask<Double,Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            Double128Mask m = (Double128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Double128Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Double128Mask or(Mask<Double,Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            Double128Mask m = (Double128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Double128Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Double128Mask.class, long.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Double128Mask.class, long.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Double128Shuffle extends AbstractShuffle<Double, Shapes.S128Bit> {
        Double128Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Double128Shuffle(int[] reorder) {
            super(reorder);
        }

        public Double128Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Double128Shuffle(IntUnaryOperator f) {
            super(f);
        }

        @Override
        public Double128Species species() {
            return SPECIES;
        }

        @Override
        public Double128Vector toVector() {
            double[] va = new double[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (double) getElement(i);
            }
            return species().fromArray(va, 0);
        }

        @Override
        public Double128Shuffle rearrange(Vector.Shuffle<Double, Shapes.S128Bit> o) {
            Double128Shuffle s = (Double128Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Double128Shuffle(r);
        }
    }

    // Species

    @Override
    public Double128Species species() {
        return SPECIES;
    }

    static final class Double128Species extends DoubleSpecies<Shapes.S128Bit> {
        static final int BIT_SIZE = Shapes.S_128_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Double.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(double.class.getSimpleName()).append("s x ");
           sb.append(elementSize()).append(" bits");
           sb.append("]");
           return sb.toString();
        }

        @Override
        @ForceInline
        public int bitSize() {
            return BIT_SIZE;
        }

        @Override
        @ForceInline
        public int length() {
            return LENGTH;
        }

        @Override
        @ForceInline
        public Class<Double> elementType() {
            return double.class;
        }

        @Override
        @ForceInline
        public int elementSize() {
            return Double.SIZE;
        }

        @Override
        @ForceInline
        public Shapes.S128Bit shape() {
            return Shapes.S_128_BIT;
        }

        @Override
        Double128Vector op(FOp f) {
            double[] res = new double[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Double128Vector(res);
        }

        @Override
        Double128Vector op(Mask<Double, Shapes.S128Bit> o, FOp f) {
            double[] res = new double[length()];
            boolean[] mbits = ((Double128Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Double128Vector(res);
        }

        // Factories

        @Override
        public Double128Mask maskFromValues(boolean... bits) {
            return new Double128Mask(bits);
        }

        @Override
        public Double128Mask maskFromArray(boolean[] bits, int i) {
            return new Double128Mask(bits, i);
        }

        @Override
        public Double128Shuffle shuffle(IntUnaryOperator f) {
            return new Double128Shuffle(f);
        }

        @Override
        public Double128Shuffle shuffleIota() {
            return new Double128Shuffle(AbstractShuffle.IDENTITY);
        }

        @Override
        public Double128Shuffle shuffleFromValues(int... ixs) {
            return new Double128Shuffle(ixs);
        }

        @Override
        public Double128Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Double128Shuffle(ixs, i);
        }

        @Override
        @ForceInline
        public Double128Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Double128Vector.class, double.class, LENGTH,
                                                     Double.doubleToLongBits(0.0f),
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Double128Vector broadcast(double e) {
            return VectorIntrinsics.broadcastCoerced(
                Double128Vector.class, double.class, LENGTH,
                Double.doubleToLongBits(e),
                ((long bits) -> SPECIES.op(i -> Double.longBitsToDouble((long)bits))));
        }

        @Override
        @ForceInline
        public Double128Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Double128Mask.class, long.class, LENGTH,
                                                     (long)-1,
                                                     (z -> Double128Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Double128Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Double128Mask.class, long.class, LENGTH,
                                                     0,
                                                     (z -> Double128Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Double128Vector scalars(double... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return VectorIntrinsics.load(Double128Vector.class, double.class, LENGTH,
                                         es, Unsafe.ARRAY_DOUBLE_BASE_OFFSET,
                                         es, ix,
                                         (c, idx) -> op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Double128Vector fromArray(double[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return VectorIntrinsics.load(Double128Vector.class, double.class, LENGTH,
                                         a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_DOUBLE_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Double128Vector fromArray(double[] a, int ax, Mask<Double, Shapes.S128Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromArray(a, ax), m);
        }

        @Override
        @ForceInline
        public Double128Vector fromByteArray(byte[] a, int ix) {
            // @@@ Endianess
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Double128Vector.class, double.class, LENGTH,
                                         a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
                                             DoubleBuffer tb = bbc.asDoubleBuffer();
                                             return op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Double128Vector fromByteArray(byte[] a, int ix, Mask<Double, Shapes.S128Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Double128Vector fromByteBuffer(ByteBuffer bb, int ix) {
            // @@@ Endianess
            if (bb.order() != ByteOrder.nativeOrder()) {
                throw new IllegalArgumentException();
            }
            ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Double128Vector.class, double.class, LENGTH,
                                         U.getObject(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + ix,
                                         bb, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                             DoubleBuffer tb = bbc.asDoubleBuffer();
                                             return op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Double128Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Double, Shapes.S128Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> Double128Vector cast(Vector<F, T> o) {
            if (o.length() != LENGTH)
                throw new IllegalArgumentException("Vector length this species length differ");

            return VectorIntrinsics.cast(
                o.getClass(),
                o.elementType(), LENGTH,
                double.class, LENGTH,
                o, this,
                (s, v) -> s.castDefault(v)
            );
        }

        @SuppressWarnings("unchecked")
        @ForceInline
        private <F, T extends Shape> Double128Vector castDefault(Vector<F, T> v) {
            // Allocate array of required size
            int limit = length();
            double[] a = new double[limit];

            Class<?> vtype = v.species().elementType();
            if (vtype == byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == int.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
        }

        @Override
        @ForceInline
        public <E, S extends Shape> Double128Mask cast(Mask<E, S> m) {
            if (m.length() != LENGTH)
                throw new IllegalArgumentException("Mask length this species length differ");
            return new Double128Mask(m.toArray());
        }

        @Override
        @ForceInline
        public <E, S extends Shape> Double128Shuffle cast(Shuffle<E, S> s) {
            if (s.length() != LENGTH)
                throw new IllegalArgumentException("Shuffle length this species length differ");
            return new Double128Shuffle(s.toArray());
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F> Double128Vector rebracket(Vector<F, Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte128Vector.class,
                    byte.class, so.length(),
                    double.class, LENGTH,
                    so, this,
                    (s, v) -> (Double128Vector) s.reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short128Vector.class,
                    short.class, so.length(),
                    double.class, LENGTH,
                    so, this,
                    (s, v) -> (Double128Vector) s.reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int128Vector.class,
                    int.class, so.length(),
                    double.class, LENGTH,
                    so, this,
                    (s, v) -> (Double128Vector) s.reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long128Vector.class,
                    long.class, so.length(),
                    double.class, LENGTH,
                    so, this,
                    (s, v) -> (Double128Vector) s.reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float128Vector.class,
                    float.class, so.length(),
                    double.class, LENGTH,
                    so, this,
                    (s, v) -> (Double128Vector) s.reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double128Vector.class,
                    double.class, so.length(),
                    double.class, LENGTH,
                    so, this,
                    (s, v) -> (Double128Vector) s.reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Double128Vector resize(Vector<Double, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double64Vector.class,
                    double.class, so.length(),
                    double.class, LENGTH,
                    so, this,
                    (s, v) -> (Double128Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double128Vector.class,
                    double.class, so.length(),
                    double.class, LENGTH,
                    so, this,
                    (s, v) -> (Double128Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double256Vector.class,
                    double.class, so.length(),
                    double.class, LENGTH,
                    so, this,
                    (s, v) -> (Double128Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double512Vector.class,
                    double.class, so.length(),
                    double.class, LENGTH,
                    so, this,
                    (s, v) -> (Double128Vector) s.reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
