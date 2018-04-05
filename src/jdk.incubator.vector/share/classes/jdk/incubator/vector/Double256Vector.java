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
final class Double256Vector extends DoubleVector<Shapes.S256Bit> {
    static final Double256Species SPECIES = new Double256Species();

    static final Double256Vector ZERO = new Double256Vector();

    static final int LENGTH = SPECIES.length();

    private final double[] vec; // Don't access directly, use getElements() instead.

    private double[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Double256Vector() {
        vec = new double[SPECIES.length()];
    }

    Double256Vector(double[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Double256Vector uOp(FUnOp f) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Double256Vector(res);
    }

    @Override
    Double256Vector uOp(Mask<Double, Shapes.S256Bit> o, FUnOp f) {
        double[] vec = getElements();
        double[] res = new double[length()];
        boolean[] mbits = ((Double256Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Double256Vector(res);
    }

    // Binary operator

    @Override
    Double256Vector bOp(Vector<Double, Shapes.S256Bit> o, FBinOp f) {
        double[] res = new double[length()];
        double[] vec1 = this.getElements();
        double[] vec2 = ((Double256Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Double256Vector(res);
    }

    @Override
    Double256Vector bOp(Vector<Double, Shapes.S256Bit> o1, Mask<Double, Shapes.S256Bit> o2, FBinOp f) {
        double[] res = new double[length()];
        double[] vec1 = this.getElements();
        double[] vec2 = ((Double256Vector)o1).getElements();
        boolean[] mbits = ((Double256Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Double256Vector(res);
    }

    // Trinary operator

    @Override
    Double256Vector tOp(Vector<Double, Shapes.S256Bit> o1, Vector<Double, Shapes.S256Bit> o2, FTriOp f) {
        double[] res = new double[length()];
        double[] vec1 = this.getElements();
        double[] vec2 = ((Double256Vector)o1).getElements();
        double[] vec3 = ((Double256Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Double256Vector(res);
    }

    @Override
    Double256Vector tOp(Vector<Double, Shapes.S256Bit> o1, Vector<Double, Shapes.S256Bit> o2, Mask<Double, Shapes.S256Bit> o3, FTriOp f) {
        double[] res = new double[length()];
        double[] vec1 = getElements();
        double[] vec2 = ((Double256Vector)o1).getElements();
        double[] vec3 = ((Double256Vector)o2).getElements();
        boolean[] mbits = ((Double256Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Double256Vector(res);
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
    public DoubleVector<Shapes.S256Bit> add(double o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> add(double o, Mask<Double,Shapes.S256Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> addSaturate(double o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> addSaturate(double o, Mask<Double,Shapes.S256Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> sub(double o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> sub(double o, Mask<Double,Shapes.S256Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> subSaturate(double o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> subSaturate(double o, Mask<Double,Shapes.S256Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> mul(double o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> mul(double o, Mask<Double,Shapes.S256Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> min(double o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> max(double o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S256Bit> equal(double o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S256Bit> notEqual(double o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S256Bit> lessThan(double o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S256Bit> lessThanEq(double o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S256Bit> greaterThan(double o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Double, Shapes.S256Bit> greaterThanEq(double o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> blend(double o, Mask<Double,Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> div(double o) {
        return div(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> div(double o, Mask<Double,Shapes.S256Bit> m) {
        return div(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> atan2(double o) {
        return atan2(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> atan2(double o, Mask<Double,Shapes.S256Bit> m) {
        return atan2(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> pow(double o) {
        return pow(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> pow(double o, Mask<Double,Shapes.S256Bit> m) {
        return pow(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> fma(double o1, double o2) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> fma(double o1, double o2, Mask<Double,Shapes.S256Bit> m) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2), m);
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> hypot(double o) {
        return hypot(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public DoubleVector<Shapes.S256Bit> hypot(double o, Mask<Double,Shapes.S256Bit> m) {
        return hypot(SPECIES.broadcast(o), m);
    }



    // Unary operations

    @Override
    @ForceInline
    public Double256Vector abs() {
        return (Double256Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Double256Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double256Vector)v1).uOp((i, a) -> (double) Math.abs(a)));
    }

    @Override
    @ForceInline
    public Double256Vector neg() {
        return (Double256Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NEG, Double256Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double256Vector)v1).uOp((i, a) -> (double) -a));
    }

    @Override
    @ForceInline
    public Double256Vector div(Vector<Double,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Double256Vector v = (Double256Vector)o;
        return (Double256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Double256Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double256Vector)v1).bOp(v2, (i, a, b) -> (double)(a / b)));
    }

    @Override
    @ForceInline
    public Double256Vector sqrt() {
        return (Double256Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_SQRT, Double256Vector.class, double.class, LENGTH,
            this,
            v1 -> ((Double256Vector)v1).uOp((i, a) -> (double) Math.sqrt((double) a)));
    }

    // Binary operations

    @Override
    @ForceInline
    public Double256Vector add(Vector<Double,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Double256Vector v = (Double256Vector)o;
        return (Double256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Double256Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double256Vector)v1).bOp(v2, (i, a, b) -> (double)(a + b)));
    }

    @Override
    @ForceInline
    public Double256Vector sub(Vector<Double,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Double256Vector v = (Double256Vector)o;
        return (Double256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Double256Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double256Vector)v1).bOp(v2, (i, a, b) -> (double)(a - b)));
    }

    @Override
    @ForceInline
    public Double256Vector mul(Vector<Double,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Double256Vector v = (Double256Vector)o;
        return (Double256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Double256Vector.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> ((Double256Vector)v1).bOp(v2, (i, a, b) -> (double)(a * b)));
    }

    @Override
    @ForceInline
    public Double256Vector add(Vector<Double,Shapes.S256Bit> v, Mask<Double, Shapes.S256Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a + b));
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Double256Vector sub(Vector<Double,Shapes.S256Bit> v, Mask<Double, Shapes.S256Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a - b));
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Double256Vector mul(Vector<Double,Shapes.S256Bit> v, Mask<Double, Shapes.S256Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a * b));
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Double256Vector div(Vector<Double,Shapes.S256Bit> v, Mask<Double, Shapes.S256Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (double)(a / b));
        return blend(div(v), m);
    }


    // Ternary operations

    @Override
    @ForceInline
    public Double256Vector fma(Vector<Double,Shapes.S256Bit> o1, Vector<Double,Shapes.S256Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Double256Vector v1 = (Double256Vector)o1;
        Double256Vector v2 = (Double256Vector)o2;
        return (Double256Vector) VectorIntrinsics.ternaryOp(
            VECTOR_OP_FMA, Double256Vector.class, double.class, LENGTH,
            this, v1, v2,
            (w1, w2, w3) -> w1.tOp(w2, w3, (i, a, b, c) -> Math.fma(a, b, c)));
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public double addAll() {
        long bits = (long) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_ADD, Double256Vector.class, double.class, LENGTH,
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
                                VECTOR_OP_MUL, Double256Vector.class, double.class, LENGTH,
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
        VectorIntrinsics.store(Double256Vector.class, double.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((double[])arr)[idx + i] = a_));
    }

    @Override
    @ForceInline
    public void intoArray(double[] a, int ax, Mask<Double, Shapes.S256Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Double256Vector oldVal = SPECIES.fromArray(a, ax);
        Double256Vector newVal = oldVal.blend(this, m);
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

        Double256Vector that = (Double256Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Double256Mask bTest(Vector<Double, Shapes.S256Bit> o, FBinTest f) {
        double[] vec1 = getElements();
        double[] vec2 = ((Double256Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Double256Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Double256Mask equal(Vector<Double, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Double256Vector v = (Double256Vector)o;

        return (Double256Mask) VectorIntrinsics.compare(
            BT_eq, Double256Vector.class, Double256Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Double256Mask notEqual(Vector<Double, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Double256Vector v = (Double256Vector)o;

        return (Double256Mask) VectorIntrinsics.compare(
            BT_ne, Double256Vector.class, Double256Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Double256Mask lessThan(Vector<Double, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Double256Vector v = (Double256Vector)o;

        return (Double256Mask) VectorIntrinsics.compare(
            BT_lt, Double256Vector.class, Double256Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Double256Mask lessThanEq(Vector<Double, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Double256Vector v = (Double256Vector)o;

        return (Double256Mask) VectorIntrinsics.compare(
            BT_le, Double256Vector.class, Double256Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Double256Mask greaterThan(Vector<Double, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Double256Vector v = (Double256Vector)o;

        return (Double256Mask) VectorIntrinsics.compare(
            BT_gt, Double256Vector.class, Double256Mask.class, double.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Double256Mask greaterThanEq(Vector<Double, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Double256Vector v = (Double256Vector)o;

        return (Double256Mask) VectorIntrinsics.compare(
            BT_ge, Double256Vector.class, Double256Mask.class, double.class, LENGTH,
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
    void forEach(Mask<Double, Shapes.S256Bit> o, FUnCon f) {
        boolean[] mbits = ((Double256Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }

    Long256Vector toBits() {
        double[] vec = getElements();
        long[] res = new long[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Double.doubleToLongBits(vec[i]);
        }
        return new Long256Vector(res);
    }


    @Override
    public Double256Vector rotateEL(int j) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Double256Vector(res);
    }

    @Override
    public Double256Vector rotateER(int j) {
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
        return new Double256Vector(res);
    }

    @Override
    public Double256Vector shiftEL(int j) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Double256Vector(res);
    }

    @Override
    public Double256Vector shiftER(int j) {
        double[] vec = getElements();
        double[] res = new double[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Double256Vector(res);
    }

    @Override
    public Double256Vector shuffle(Vector<Double, Shapes.S256Bit> o, Shuffle<Double, Shapes.S256Bit> s) {
        Double256Vector v = (Double256Vector) o;
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
    public Double256Vector swizzle(Shuffle<Double, Shapes.S256Bit> s) {
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
    public Double256Vector blend(Vector<Double, Shapes.S256Bit> o1, Mask<Double, Shapes.S256Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Double256Vector v = (Double256Vector)o1;
        Double256Mask   m = (Double256Mask)o2;

        return (Double256Vector) VectorIntrinsics.blend(
            Double256Vector.class, Double256Mask.class, double.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    @Override
    @ForceInline
    @SuppressWarnings("unchecked")
    public <F> Vector<F, Shapes.S256Bit> rebracket(Species<F, Shapes.S256Bit> species) {
        Objects.requireNonNull(species);
        // TODO: check proper element type
        // TODO: update to pass the two species as an arguments and ideally
        // push down intrinsic call into species implementation
        return VectorIntrinsics.rebracket(
            Double256Vector.class, double.class, LENGTH,
            double.class, this,
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
    public Double256Vector with(int i, double e) {
        double[] res = vec.clone();
        res[i] = e;
        return new Double256Vector(res);
    }

    // Mask

    static final class Double256Mask extends AbstractMask<Double, Shapes.S256Bit> {
        static final Double256Mask TRUE_MASK = new Double256Mask(true);
        static final Double256Mask FALSE_MASK = new Double256Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Double256Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Double256Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
        }

        public Double256Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Double256Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Double256Mask(res);
        }

        @Override
        Double256Mask bOp(Mask<Double, Shapes.S256Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Double256Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Double256Mask(res);
        }

        @Override
        public Double256Species species() {
            return SPECIES;
        }

        @Override
        public Double256Vector toVector() {
            double[] res = new double[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (double) (bits[i] ? -1 : 0);
            }
            return new Double256Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S256Bit> rebracket(Species<Z, Shapes.S256Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.rebracket(
                Double256Mask.class, double.class, LENGTH,
                double.class, this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        //Mask<E, S> not();

        // Binary operations

        @Override
        @ForceInline
        public Double256Mask and(Mask<Double,Shapes.S256Bit> o) {
            Objects.requireNonNull(o);
            Double256Mask m = (Double256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Double256Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Double256Mask or(Mask<Double,Shapes.S256Bit> o) {
            Objects.requireNonNull(o);
            Double256Mask m = (Double256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Double256Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Double256Mask.class, long.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Double256Mask.class, long.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Double256Shuffle extends AbstractShuffle<Double, Shapes.S256Bit> {
        static final IntVector.IntSpecies<Shapes.S256Bit> INT_SPECIES = IntVector.speciesInstance(Shapes.S_256_BIT);

        public Double256Shuffle(int[] reorder) {
            super(reorder);
        }

        public Double256Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Double256Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S256Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Double256Species species() {
        return SPECIES;
    }

    static final class Double256Species extends DoubleSpecies<Shapes.S256Bit> {
        static final int BIT_SIZE = Shapes.S_256_BIT.bitSize();

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
        public Shapes.S256Bit shape() {
            return Shapes.S_256_BIT;
        }

        @Override
        Double256Vector op(FOp f) {
            double[] res = new double[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Double256Vector(res);
        }

        @Override
        Double256Vector op(Mask<Double, Shapes.S256Bit> o, FOp f) {
            double[] res = new double[length()];
            boolean[] mbits = ((Double256Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Double256Vector(res);
        }

        // Factories

        @Override
        public Double256Mask maskFromValues(boolean... bits) {
            return new Double256Mask(bits);
        }

        @Override
        public Double256Mask maskFromArray(boolean[] bits, int i) {
            return new Double256Mask(bits, i);
        }

        @Override
        public Double256Shuffle shuffleFromValues(int... ixs) {
            return new Double256Shuffle(ixs);
        }

        @Override
        public Double256Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Double256Shuffle(ixs, i);
        }

        @Override
        @ForceInline
        public Double256Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Double256Vector.class, double.class, LENGTH,
                                                     Double.doubleToLongBits(0.0f),
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Double256Vector broadcast(double e) {
            return VectorIntrinsics.broadcastCoerced(
                Double256Vector.class, double.class, LENGTH,
                Double.doubleToLongBits(e),
                ((long bits) -> SPECIES.op(i -> Double.longBitsToDouble((long)bits))));
        }

        @Override
        @ForceInline
        public Double256Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Double256Mask.class, long.class, LENGTH,
                                                     (long)-1,
                                                     (z -> Double256Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Double256Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Double256Mask.class, long.class, LENGTH,
                                                     0,
                                                     (z -> Double256Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Double256Vector fromArray(double[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Double256Vector) VectorIntrinsics.load(Double256Vector.class, double.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((double[]) arr, idx));
        }

        @Override
        @ForceInline
        public Double256Vector fromArray(double[] a, int ax, Mask<Double, Shapes.S256Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }
    }
}
