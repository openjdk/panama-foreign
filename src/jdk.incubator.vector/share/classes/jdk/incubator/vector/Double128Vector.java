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
import java.util.Arrays;
import java.util.Objects;
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
    public DoubleVector<Shapes.S128Bit> addSaturate(double o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> addSaturate(double o, Mask<Double,Shapes.S128Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
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
    public DoubleVector<Shapes.S128Bit> subSaturate(double o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S128Bit> subSaturate(double o, Mask<Double,Shapes.S128Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
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

    @Override
    @ForceInline
    public Double128Vector abs() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.abs(a)));
    }

    @Override
    @ForceInline
    public Double128Vector neg() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NEG, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) -a));
    }

    @Override
    @ForceInline
    public Double128Vector div(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(a / b)));
    }

    @Override
    @ForceInline
    public Double128Vector sqrt() {
        return (Double128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_SQRT, Double128Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double128Vector)v1).uOp((i, a) -> (double) Math.sqrt((double) a)));
    }

    // Binary operations

    @Override
    @ForceInline
    public Double128Vector add(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(a + b)));
    }

    @Override
    @ForceInline
    public Double128Vector sub(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(a - b)));
    }

    @Override
    @ForceInline
    public Double128Vector mul(Vector<Double,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;
        return (Double128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Double128Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double128Vector)v1).bOp(v2, (i, a, b) -> (double)(a * b)));
    }

    @Override
    @ForceInline
    public Double128Vector add(Vector<Double,Shapes.S128Bit> v, Mask<Double, Shapes.S128Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a + b));
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Double128Vector sub(Vector<Double,Shapes.S128Bit> v, Mask<Double, Shapes.S128Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a - b));
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Double128Vector mul(Vector<Double,Shapes.S128Bit> v, Mask<Double, Shapes.S128Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a * b));
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Double128Vector div(Vector<Double,Shapes.S128Bit> v, Mask<Double, Shapes.S128Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a / b));
        return blend(div(v), m);
    }


    // Ternary operations

    @Override
    @ForceInline
    public Double128Vector fma(Vector<Double,Shapes.S128Bit> o1, Vector<Double,Shapes.S128Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Double128Vector v1 = (Double128Vector)o1;
        Double128Vector v2 = (Double128Vector)o2;
        return (Double128Vector) VectorIntrinsics.ternaryOp(
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

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(double[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Double128Vector.class, double.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((double[])arr)[idx + i] = a_));
    }

    @Override
    @ForceInline
    public void intoArray(double[] a, int ax, Mask<Double, Shapes.S128Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Double128Vector oldVal = SPECIES.fromArray(a, ax);
        Double128Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
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

        return (Double128Mask) VectorIntrinsics.compare(
            BT_eq, Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Double128Mask notEqual(Vector<Double, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;

        return (Double128Mask) VectorIntrinsics.compare(
            BT_ne, Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Double128Mask lessThan(Vector<Double, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;

        return (Double128Mask) VectorIntrinsics.compare(
            BT_lt, Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Double128Mask lessThanEq(Vector<Double, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Double128Vector v = (Double128Vector)o;

        return (Double128Mask) VectorIntrinsics.compare(
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

        return (Double128Mask) VectorIntrinsics.compare(
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
    public Double128Vector shuffle(Vector<Double, Shapes.S128Bit> o, Shuffle<Double, Shapes.S128Bit> s) {
        Double128Vector v = (Double128Vector) o;
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
    public Double128Vector swizzle(Shuffle<Double, Shapes.S128Bit> s) {
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
    public Double128Vector blend(Vector<Double, Shapes.S128Bit> o1, Mask<Double, Shapes.S128Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Double128Vector v = (Double128Vector)o1;
        Double128Mask   m = (Double128Mask)o2;

        return (Double128Vector) VectorIntrinsics.blend(
            Double128Vector.class, Double128Mask.class, double.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    @Override
    @ForceInline
    @SuppressWarnings("unchecked")
    public <F> Vector<F, Shapes.S128Bit> rebracket(Species<F, Shapes.S128Bit> species) {
        Objects.requireNonNull(species);
        // TODO: check proper element type
        // TODO: update to pass the two species as an arguments and ideally
        // push down intrinsic call into species implementation
        return VectorIntrinsics.rebracket(
            Double128Vector.class, double.class, LENGTH,
            species.elementType(), this,
            (v, t) -> species.reshape(v)
        );
    }

    // Accessors

    @Override
    public double get(int i) {
        double[] vec = getElements();
        return vec[i];
    }

    @Override
    public Double128Vector with(int i, double e) {
        double[] res = vec.clone();
        res[i] = e;
        return new Double128Vector(res);
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

        public Double128Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
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
                res[i] = (double) (bits[i] ? -1 : 0);
            }
            return new Double128Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S128Bit> rebracket(Species<Z, Shapes.S128Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.rebracket(
                Double128Mask.class, double.class, LENGTH,
                species.elementType(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        //Mask<E, S> not();

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
        static final IntVector.IntSpecies<Shapes.S128Bit> INT_SPECIES = IntVector.speciesInstance(Shapes.S_128_BIT);

        public Double128Shuffle(int[] reorder) {
            super(reorder);
        }

        public Double128Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Double128Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S128Bit> intSpecies() {
            return INT_SPECIES;
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
        public int bitSize() {
            return BIT_SIZE;
        }

        @Override
        public int length() {
            return LENGTH;
        }

        @Override
        public Class<Double> elementType() {
            return double.class;
        }

        @Override
        public int elementSize() {
            return Double.SIZE;
        }

        @Override
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
        public Double128Shuffle shuffleFromValues(int... ixs) {
            return new Double128Shuffle(ixs);
        }

        @Override
        public Double128Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Double128Shuffle(ixs, i);
        }

        @Override
        public Double128Shuffle shuffleFromVector(Vector<Integer, Shapes.S128Bit> v) {
            int[] a = ((IntVector<Shapes.S128Bit>) v).toArray();
            return new Double128Shuffle(a, 0);
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
        public Double128Vector fromArray(double[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Double128Vector) VectorIntrinsics.load(Double128Vector.class, double.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((double[]) arr, idx));
        }

        @Override
        @ForceInline
        public Double128Vector fromArray(double[] a, int ax, Mask<Double, Shapes.S128Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }
    }
}
