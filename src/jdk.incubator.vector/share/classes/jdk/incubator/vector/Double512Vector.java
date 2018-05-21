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
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Double512Vector extends DoubleVector<Shapes.S512Bit> {
    static final Double512Species SPECIES = new Double512Species();

    static final Double512Vector ZERO = new Double512Vector();

    static final int LENGTH = SPECIES.length();

    private final double[] vec; // Don't access directly, use getElements() instead.

    private double[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Double512Vector() {
        vec = new double[SPECIES.length()];
    }

    Double512Vector(double[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Double512Vector uOp(FUnOp f) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Double512Vector(res);
    }

    @Override
    Double512Vector uOp(Mask<Double, Shapes.S512Bit> o, FUnOp f) {
        double[] vec = getElements();
        double[] res = new double[length()];
        boolean[] mbits = ((Double512Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Double512Vector(res);
    }

    // Binary operator

    @Override
    Double512Vector bOp(Vector<Double, Shapes.S512Bit> o, FBinOp f) {
        double[] res = new double[length()];
        double[] vec1 = this.getElements();
        double[] vec2 = ((Double512Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Double512Vector(res);
    }

    @Override
    Double512Vector bOp(Vector<Double, Shapes.S512Bit> o1, Mask<Double, Shapes.S512Bit> o2, FBinOp f) {
        double[] res = new double[length()];
        double[] vec1 = this.getElements();
        double[] vec2 = ((Double512Vector)o1).getElements();
        boolean[] mbits = ((Double512Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Double512Vector(res);
    }

    // Trinary operator

    @Override
    Double512Vector tOp(Vector<Double, Shapes.S512Bit> o1, Vector<Double, Shapes.S512Bit> o2, FTriOp f) {
        double[] res = new double[length()];
        double[] vec1 = this.getElements();
        double[] vec2 = ((Double512Vector)o1).getElements();
        double[] vec3 = ((Double512Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Double512Vector(res);
    }

    @Override
    Double512Vector tOp(Vector<Double, Shapes.S512Bit> o1, Vector<Double, Shapes.S512Bit> o2, Mask<Double, Shapes.S512Bit> o3, FTriOp f) {
        double[] res = new double[length()];
        double[] vec1 = getElements();
        double[] vec2 = ((Double512Vector)o1).getElements();
        double[] vec3 = ((Double512Vector)o2).getElements();
        boolean[] mbits = ((Double512Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Double512Vector(res);
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
    public DoubleVector<Shapes.S512Bit> add(double o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> add(double o, Mask<Double,Shapes.S512Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> sub(double o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> sub(double o, Mask<Double,Shapes.S512Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> mul(double o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> mul(double o, Mask<Double,Shapes.S512Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> min(double o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> max(double o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S512Bit> equal(double o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S512Bit> notEqual(double o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S512Bit> lessThan(double o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S512Bit> lessThanEq(double o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S512Bit> greaterThan(double o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S512Bit> greaterThanEq(double o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> blend(double o, Mask<Double,Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> div(double o) {
        return div(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> div(double o, Mask<Double,Shapes.S512Bit> m) {
        return div(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> atan2(double o) {
        return atan2(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> atan2(double o, Mask<Double,Shapes.S512Bit> m) {
        return atan2(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> pow(double o) {
        return pow(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> pow(double o, Mask<Double,Shapes.S512Bit> m) {
        return pow(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> fma(double o1, double o2) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> fma(double o1, double o2, Mask<Double,Shapes.S512Bit> m) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> hypot(double o) {
        return hypot(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S512Bit> hypot(double o, Mask<Double,Shapes.S512Bit> m) {
        return hypot(SPECIES.broadcast(o), m);
    }


    // Unary operations

    @Override
    @ForceInline
    public Double512Vector abs() {
        return (Double512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Double512Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double512Vector)v1).uOp((i, a) -> (double) Math.abs(a)));
    }

    @Override
    @ForceInline
    public Double512Vector neg() {
        return (Double512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NEG, Double512Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double512Vector)v1).uOp((i, a) -> (double) -a));
    }

    @Override
    @ForceInline
    public Double512Vector div(Vector<Double,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;
        return (Double512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Double512Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double512Vector)v1).bOp(v2, (i, a, b) -> (double)(a / b)));
    }

    @Override
    @ForceInline
    public Double512Vector sqrt() {
        return (Double512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_SQRT, Double512Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double512Vector)v1).uOp((i, a) -> (double) Math.sqrt((double) a)));
    }

    // Binary operations

    @Override
    @ForceInline
    public Double512Vector add(Vector<Double,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;
        return (Double512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Double512Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double512Vector)v1).bOp(v2, (i, a, b) -> (double)(a + b)));
    }

    @Override
    @ForceInline
    public Double512Vector sub(Vector<Double,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;
        return (Double512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Double512Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double512Vector)v1).bOp(v2, (i, a, b) -> (double)(a - b)));
    }

    @Override
    @ForceInline
    public Double512Vector mul(Vector<Double,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;
        return (Double512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Double512Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double512Vector)v1).bOp(v2, (i, a, b) -> (double)(a * b)));
    }

    @Override
    @ForceInline
    public Double512Vector min(Vector<Double,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;
        return (Double512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Double512Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double512Vector)v1).bOp(v2, (i, a, b) -> (double) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Double512Vector max(Vector<Double,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;
        return (Double512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Double512Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double512Vector)v1).bOp(v2, (i, a, b) -> (double) ((a > b) ? a : b)));
        }

    @Override
    @ForceInline
    public Double512Vector add(Vector<Double,Shapes.S512Bit> v, Mask<Double, Shapes.S512Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a + b));
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Double512Vector sub(Vector<Double,Shapes.S512Bit> v, Mask<Double, Shapes.S512Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a - b));
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Double512Vector mul(Vector<Double,Shapes.S512Bit> v, Mask<Double, Shapes.S512Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a * b));
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Double512Vector div(Vector<Double,Shapes.S512Bit> v, Mask<Double, Shapes.S512Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a / b));
        return blend(div(v), m);
    }


    // Ternary operations

    @Override
    @ForceInline
    public Double512Vector fma(Vector<Double,Shapes.S512Bit> o1, Vector<Double,Shapes.S512Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Double512Vector v1 = (Double512Vector)o1;
        Double512Vector v2 = (Double512Vector)o2;
        return (Double512Vector) VectorIntrinsics.ternaryOp(
            VECTOR_OP_FMA, Double512Vector.class, double.class, LENGTH,
            this, v1, v2,
            (w1, w2, w3) -> w1.tOp(w2, w3, (i, a, b, c) -> Math.fma(a, b, c)));
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public double addAll() {
        long bits = (long) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_ADD, Double512Vector.class, double.class, LENGTH,
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
                                VECTOR_OP_SUB, Double512Vector.class, double.class, LENGTH,
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
                                VECTOR_OP_MUL, Double512Vector.class, double.class, LENGTH,
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
                                VECTOR_OP_MIN, Double512Vector.class, double.class, LENGTH,
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
                                VECTOR_OP_MAX, Double512Vector.class, double.class, LENGTH,
                                this,
                                v -> {
                                    double r = v.rOp(Double.MIN_VALUE , (i, a, b) -> (double) ((a > b) ? a : b));
                                    return (long)Double.doubleToLongBits(r);
                                });
        return Double.longBitsToDouble(bits);
    }


    @Override
    @ForceInline
    public double addAll(Mask<Double, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((double) 0), m).addAll();
    }

    @Override
    @ForceInline
    public double subAll(Mask<Double, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((double) 0), m).subAll();
    }

    @Override
    @ForceInline
    public double mulAll(Mask<Double, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((double) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public double minAll(Mask<Double, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(Double.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public double maxAll(Mask<Double, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(Double.MIN_VALUE), m).maxAll();
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(double[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Double512Vector.class, double.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((double[])arr)[idx + i] = a_));
    }

    @Override
    @ForceInline
    public void intoArray(double[] a, int ax, Mask<Double, Shapes.S512Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Double512Vector oldVal = SPECIES.fromArray(a, ax);
        Double512Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Double512Vector.class, double.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> {
                                   byte[] tarr = (byte[])arr;
                                   ByteBuffer bb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                   DoubleBuffer fb = bb.asDoubleBuffer();
                                   v.forEach((i, e) -> fb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix, Mask<Double, Shapes.S512Bit> m) {
        Double512Vector oldVal = SPECIES.fromByteArray(a, ix);
        Double512Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
            VectorIntrinsics.store(Double512Vector.class, double.class, LENGTH,
                                   bb.array(), ix, this,
                                   (arr, idx, v) -> {
                                       byte[] tarr = (byte[])arr;
                                       ByteBuffer lbb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                       DoubleBuffer fb = lbb.asDoubleBuffer();
                                       v.forEach((i, e) -> fb.put(e));
                                   });
        } else {
            super.intoByteBuffer(bb);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, Mask<Double, Shapes.S512Bit> m) {
        int idx = bb.position();
        Double512Vector oldVal = SPECIES.fromByteBuffer(bb, idx);
        Double512Vector newVal = oldVal.blend(this, m);
        newVal.intoByteBuffer(bb, idx);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
            VectorIntrinsics.store(Double512Vector.class, double.class, LENGTH,
                                   bb.array(), ax, this,
                                   (arr, idx, v) -> {
                                       byte[] tarr = (byte[])arr;
                                       ByteBuffer lbb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                       DoubleBuffer fb = lbb.asDoubleBuffer();
                                       v.forEach((i, e) -> fb.put(e));
                                   });
        } else {
            super.intoByteBuffer(bb, ix);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Double, Shapes.S512Bit> m) {
        Double512Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Double512Vector newVal = oldVal.blend(this, m);
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

        Double512Vector that = (Double512Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Double512Mask bTest(Vector<Double, Shapes.S512Bit> o, FBinTest f) {
        double[] vec1 = getElements();
        double[] vec2 = ((Double512Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Double512Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Double512Mask equal(Vector<Double, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;

        return (Double512Mask) VectorIntrinsics.compare(
            BT_eq, Double512Vector.class, Double512Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Double512Mask notEqual(Vector<Double, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;

        return (Double512Mask) VectorIntrinsics.compare(
            BT_ne, Double512Vector.class, Double512Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Double512Mask lessThan(Vector<Double, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;

        return (Double512Mask) VectorIntrinsics.compare(
            BT_lt, Double512Vector.class, Double512Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Double512Mask lessThanEq(Vector<Double, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;

        return (Double512Mask) VectorIntrinsics.compare(
            BT_le, Double512Vector.class, Double512Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Double512Mask greaterThan(Vector<Double, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;

        return (Double512Mask) VectorIntrinsics.compare(
            BT_gt, Double512Vector.class, Double512Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Double512Mask greaterThanEq(Vector<Double, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Double512Vector v = (Double512Vector)o;

        return (Double512Mask) VectorIntrinsics.compare(
            BT_ge, Double512Vector.class, Double512Mask.class, double.class, LENGTH,
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
    void forEach(Mask<Double, Shapes.S512Bit> o, FUnCon f) {
        boolean[] mbits = ((Double512Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }

    Long512Vector toBits() {
        double[] vec = getElements();
        long[] res = new long[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Double.doubleToLongBits(vec[i]);
        }
        return new Long512Vector(res);
    }


    @Override
    public Double512Vector rotateEL(int j) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Double512Vector(res);
    }

    @Override
    public Double512Vector rotateER(int j) {
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
        return new Double512Vector(res);
    }

    @Override
    public Double512Vector shiftEL(int j) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Double512Vector(res);
    }

    @Override
    public Double512Vector shiftER(int j) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Double512Vector(res);
    }

    @Override
    public Double512Vector shuffle(Vector<Double, Shapes.S512Bit> o, Shuffle<Double, Shapes.S512Bit> s) {
        Double512Vector v = (Double512Vector) o;
        return uOp((i, a) -> {
            double[] vec = this.getElements();
            int e = s.getElement(i);
            if(e >= 0 && e < length()) {
                //from this
                return vec[e];
            } else if(e < length() * 2) {
                //from o
                return v.getElements()[e - length()];
            } else {
                throw new ArrayIndexOutOfBoundsException("Bad reordering for shuffle");
            }
        });
    }

    @Override
    public Double512Vector swizzle(Shuffle<Double, Shapes.S512Bit> s) {
        return uOp((i, a) -> {
            double[] vec = this.getElements();
            int e = s.getElement(i);
            if(e >= 0 && e < length()) {
                return vec[e];
            } else {
                throw new ArrayIndexOutOfBoundsException("Bad reordering for shuffle");
            }
        });
    }

    @Override
    @ForceInline
    public Double512Vector blend(Vector<Double, Shapes.S512Bit> o1, Mask<Double, Shapes.S512Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Double512Vector v = (Double512Vector)o1;
        Double512Mask   m = (Double512Mask)o2;

        return (Double512Vector) VectorIntrinsics.blend(
            Double512Vector.class, Double512Mask.class, double.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public double get(int i) {
        double[] vec = getElements();
        return vec[i];
    }

    @Override
    public Double512Vector with(int i, double e) {
        double[] res = vec.clone();
        res[i] = e;
        return new Double512Vector(res);
    }

    // Mask

    static final class Double512Mask extends AbstractMask<Double, Shapes.S512Bit> {
        static final Double512Mask TRUE_MASK = new Double512Mask(true);
        static final Double512Mask FALSE_MASK = new Double512Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Double512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Double512Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
        }

        public Double512Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Double512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Double512Mask(res);
        }

        @Override
        Double512Mask bOp(Mask<Double, Shapes.S512Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Double512Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Double512Mask(res);
        }

        @Override
        public Double512Species species() {
            return SPECIES;
        }

        @Override
        public Double512Vector toVector() {
            double[] res = new double[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (double) (bits[i] ? -1 : 0);
            }
            return new Double512Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S512Bit> rebracket(Species<Z, Shapes.S512Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.reinterpret(
                Double512Mask.class, double.class, LENGTH,
                species.elementType(), species.length(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        @Override
        @ForceInline
        public Double512Mask not() {
            return (Double512Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Double512Mask.class, long.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Double512Mask and(Mask<Double,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Double512Mask m = (Double512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Double512Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Double512Mask or(Mask<Double,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Double512Mask m = (Double512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Double512Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Double512Mask.class, long.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Double512Mask.class, long.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Double512Shuffle extends AbstractShuffle<Double, Shapes.S512Bit> {
        static final IntVector.IntSpecies<Shapes.S512Bit> INT_SPECIES = IntVector.species(Shapes.S_512_BIT);

        public Double512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Double512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Double512Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S512Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Double512Species species() {
        return SPECIES;
    }

    static final class Double512Species extends DoubleSpecies<Shapes.S512Bit> {
        static final int BIT_SIZE = Shapes.S_512_BIT.bitSize();

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
        public Shapes.S512Bit shape() {
            return Shapes.S_512_BIT;
        }

        @Override
        Double512Vector op(FOp f) {
            double[] res = new double[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Double512Vector(res);
        }

        @Override
        Double512Vector op(Mask<Double, Shapes.S512Bit> o, FOp f) {
            double[] res = new double[length()];
            boolean[] mbits = ((Double512Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Double512Vector(res);
        }

        // Factories

        @Override
        public Double512Mask maskFromValues(boolean... bits) {
            return new Double512Mask(bits);
        }

        @Override
        public Double512Mask maskFromArray(boolean[] bits, int i) {
            return new Double512Mask(bits, i);
        }

        @Override
        public Double512Shuffle shuffleFromValues(int... ixs) {
            return new Double512Shuffle(ixs);
        }

        @Override
        public Double512Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Double512Shuffle(ixs, i);
        }

        @Override
        public Double512Shuffle shuffleFromVector(Vector<Integer, Shapes.S512Bit> v) {
            int[] a = ((IntVector<Shapes.S512Bit>) v).toArray();
            return new Double512Shuffle(a, 0);
        }

        @Override
        @ForceInline
        public Double512Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Double512Vector.class, double.class, LENGTH,
                                                     Double.doubleToLongBits(0.0f),
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Double512Vector broadcast(double e) {
            return VectorIntrinsics.broadcastCoerced(
                Double512Vector.class, double.class, LENGTH,
                Double.doubleToLongBits(e),
                ((long bits) -> SPECIES.op(i -> Double.longBitsToDouble((long)bits))));
        }

        @Override
        @ForceInline
        public Double512Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Double512Mask.class, long.class, LENGTH,
                                                     (long)-1,
                                                     (z -> Double512Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Double512Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Double512Mask.class, long.class, LENGTH,
                                                     0,
                                                     (z -> Double512Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Double512Vector scalars(double... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return (Double512Vector) VectorIntrinsics.load(Double512Vector.class, double.class, LENGTH,
                                                        es, ix,
                                                        (arr, idx) -> super.fromArray((double[]) arr, idx));
        }

        @Override
        @ForceInline
        public Double512Vector fromArray(double[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Double512Vector) VectorIntrinsics.load(Double512Vector.class, double.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((double[]) arr, idx));
        }

        @Override
        @ForceInline
        public Double512Vector fromArray(double[] a, int ax, Mask<Double, Shapes.S512Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }

        @Override
        @ForceInline
        public Double512Vector fromByteArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return (Double512Vector) VectorIntrinsics.load(Double512Vector.class, double.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
        }

        @Override
        @ForceInline
        public Double512Vector fromByteArray(byte[] a, int ix, Mask<Double, Shapes.S512Bit> m) {
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Double512Vector fromByteBuffer(ByteBuffer bb) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
                return (Double512Vector) VectorIntrinsics.load(Double512Vector.class, double.class, LENGTH,
                                                            bb.array(), ix,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Double512Vector)super.fromByteBuffer(bb);
            }
        }

        @Override
        @ForceInline
        public Double512Vector fromByteBuffer(ByteBuffer bb, Mask<Double, Shapes.S512Bit> m) {
            return zero().blend(fromByteBuffer(bb), m);
        }

        @Override
        @ForceInline
        public Double512Vector fromByteBuffer(ByteBuffer bb, int ix) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
                return (Double512Vector) VectorIntrinsics.load(Double512Vector.class, double.class, LENGTH,
                                                            bb.array(), ax,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Double512Vector)super.fromByteBuffer(bb, ix);
            }
        }

        @Override
        @ForceInline
        public Double512Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Double, Shapes.S512Bit> m) {
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Double512Vector castFromByte(ByteVector<S> o) {
            if (o.bitSize() == 64) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.cast(
                    Byte64Vector.class, byte.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.cast(
                    Byte128Vector.class, byte.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.cast(
                    Byte256Vector.class, byte.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.cast(
                    Byte512Vector.class, byte.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Double512Vector castFromShort(ShortVector<S> o) {
            if (o.bitSize() == 64) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.cast(
                    Short64Vector.class, short.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.cast(
                    Short128Vector.class, short.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.cast(
                    Short256Vector.class, short.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.cast(
                    Short512Vector.class, short.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Double512Vector castFromInt(IntVector<S> o) {
            if (o.bitSize() == 64) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.cast(
                    Int64Vector.class, int.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.cast(
                    Int128Vector.class, int.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.cast(
                    Int256Vector.class, int.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.cast(
                    Int512Vector.class, int.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Double512Vector castFromLong(LongVector<S> o) {
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.cast(
                    Long64Vector.class, long.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.cast(
                    Long128Vector.class, long.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.cast(
                    Long256Vector.class, long.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.cast(
                    Long512Vector.class, long.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Double512Vector castFromFloat(FloatVector<S> o) {
            if (o.bitSize() == 64) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.cast(
                    Float64Vector.class, float.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.cast(
                    Float128Vector.class, float.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.cast(
                    Float256Vector.class, float.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.cast(
                    Float512Vector.class, float.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Double512Vector castFromDouble(DoubleVector<S> o) {
            if (o.bitSize() == 64) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.cast(
                    Double64Vector.class, double.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.cast(
                    Double128Vector.class, double.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.cast(
                    Double256Vector.class, double.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.cast(
                    Double512Vector.class, double.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <E, S extends Shape> Double512Vector cast(Vector<E, S> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                ByteVector<S> so = (ByteVector<S>)o;
                return castFromByte(so);
            } else if (o.elementType() == short.class) {
                ShortVector<S> so = (ShortVector<S>)o;
                return castFromShort(so);
            } else if (o.elementType() == int.class) {
                IntVector<S> so = (IntVector<S>)o;
                return castFromInt(so);
            } else if (o.elementType() == long.class) {
                LongVector<S> so = (LongVector<S>)o;
                return castFromLong(so);
            } else if (o.elementType() == float.class) {
                FloatVector<S> so = (FloatVector<S>)o;
                return castFromFloat(so);
            } else if (o.elementType() == double.class) {
                DoubleVector<S> so = (DoubleVector<S>)o;
                return castFromDouble(so);
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F> Double512Vector rebracket(Vector<F, Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte512Vector.class, byte.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short512Vector.class, short.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int512Vector.class, int.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long512Vector.class, long.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float512Vector.class, float.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double512Vector.class, double.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Double512Vector resize(Vector<Double, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double64Vector.class, double.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double128Vector.class, double.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double256Vector.class, double.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double512Vector.class, double.class, so.length(),
                    double.class, LENGTH, so,
                    (v, t) -> (Double512Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
