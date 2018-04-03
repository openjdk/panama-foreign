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
final class Float64Vector extends FloatVector<Shapes.S64Bit> {
    static final Float64Species SPECIES = new Float64Species();

    static final Float64Vector ZERO = new Float64Vector();

    static final int LENGTH = SPECIES.length();

    private final float[] vec; // Don't access directly, use getElements() instead.

    private float[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Float64Vector() {
        vec = new float[SPECIES.length()];
    }

    Float64Vector(float[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Float64Vector uOp(FUnOp f) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Float64Vector(res);
    }

    @Override
    Float64Vector uOp(Mask<Float, Shapes.S64Bit> o, FUnOp f) {
        float[] vec = getElements();
        float[] res = new float[length()];
        boolean[] mbits = ((Float64Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Float64Vector(res);
    }

    // Binary operator

    @Override
    Float64Vector bOp(Vector<Float, Shapes.S64Bit> o, FBinOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((Float64Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Float64Vector(res);
    }

    @Override
    Float64Vector bOp(Vector<Float, Shapes.S64Bit> o1, Mask<Float, Shapes.S64Bit> o2, FBinOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((Float64Vector)o1).getElements();
        boolean[] mbits = ((Float64Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Float64Vector(res);
    }

    // Trinary operator

    @Override
    Float64Vector tOp(Vector<Float, Shapes.S64Bit> o1, Vector<Float, Shapes.S64Bit> o2, FTriOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((Float64Vector)o1).getElements();
        float[] vec3 = ((Float64Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Float64Vector(res);
    }

    @Override
    Float64Vector tOp(Vector<Float, Shapes.S64Bit> o1, Vector<Float, Shapes.S64Bit> o2, Mask<Float, Shapes.S64Bit> o3, FTriOp f) {
        float[] res = new float[length()];
        float[] vec1 = getElements();
        float[] vec2 = ((Float64Vector)o1).getElements();
        float[] vec3 = ((Float64Vector)o2).getElements();
        boolean[] mbits = ((Float64Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Float64Vector(res);
    }

    @Override
    float rOp(float v, FBinOp f) {
        float[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> add(float o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> add(float o, Mask<Float,Shapes.S64Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> addSaturate(float o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> addSaturate(float o, Mask<Float,Shapes.S64Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> sub(float o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> sub(float o, Mask<Float,Shapes.S64Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> subSaturate(float o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> subSaturate(float o, Mask<Float,Shapes.S64Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> mul(float o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> mul(float o, Mask<Float,Shapes.S64Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> min(float o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> max(float o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S64Bit> equal(float o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S64Bit> notEqual(float o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S64Bit> lessThan(float o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S64Bit> lessThanEq(float o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S64Bit> greaterThan(float o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S64Bit> greaterThanEq(float o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> blend(float o, Mask<Float,Shapes.S64Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> div(float o) {
        return div(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> div(float o, Mask<Float,Shapes.S64Bit> m) {
        return div(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> atan2(float o) {
        return atan2(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> atan2(float o, Mask<Float,Shapes.S64Bit> m) {
        return atan2(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> pow(float o) {
        return pow(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> pow(float o, Mask<Float,Shapes.S64Bit> m) {
        return pow(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> fma(float o1, float o2) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> fma(float o1, float o2, Mask<Float,Shapes.S64Bit> m) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> hypot(float o) {
        return hypot(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S64Bit> hypot(float o, Mask<Float,Shapes.S64Bit> m) {
        return hypot(SPECIES.broadcast(o), m);
    }



    // Unary operations

    @Override
    @ForceInline
    public Float64Vector abs() {
        return (Float64Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Float64Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float64Vector)v1).uOp((i, a) -> (float) Math.abs(a)));
    }

    @Override
    @ForceInline
    public Float64Vector neg() {
        return (Float64Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NEG, Float64Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float64Vector)v1).uOp((i, a) -> (float) -a));
    }

    @Override
    @ForceInline
    public Float64Vector div(Vector<Float,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Float64Vector v = (Float64Vector)o;
        return (Float64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Float64Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float64Vector)v1).bOp(v2, (i, a, b) -> (float)(a / b)));
    }

    @Override
    @ForceInline
    public Float64Vector sqrt() {
        return (Float64Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_SQRT, Float64Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float64Vector)v1).uOp((i, a) -> (float) Math.sqrt((double) a)));
    }

    // Binary operations

    @Override
    @ForceInline
    public Float64Vector add(Vector<Float,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Float64Vector v = (Float64Vector)o;
        return (Float64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Float64Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float64Vector)v1).bOp(v2, (i, a, b) -> (float)(a + b)));
    }

    @Override
    @ForceInline
    public Float64Vector sub(Vector<Float,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Float64Vector v = (Float64Vector)o;
        return (Float64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Float64Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float64Vector)v1).bOp(v2, (i, a, b) -> (float)(a - b)));
    }

    @Override
    @ForceInline
    public Float64Vector mul(Vector<Float,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Float64Vector v = (Float64Vector)o;
        return (Float64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Float64Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float64Vector)v1).bOp(v2, (i, a, b) -> (float)(a * b)));
    }

    @Override
    @ForceInline
    public Float64Vector add(Vector<Float,Shapes.S64Bit> v, Mask<Float, Shapes.S64Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (float)(a + b));
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Float64Vector sub(Vector<Float,Shapes.S64Bit> v, Mask<Float, Shapes.S64Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (float)(a - b));
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Float64Vector mul(Vector<Float,Shapes.S64Bit> v, Mask<Float, Shapes.S64Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (float)(a * b));
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Float64Vector div(Vector<Float,Shapes.S64Bit> v, Mask<Float, Shapes.S64Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (float)(a / b));
        return blend(div(v), m);
    }


    // Ternary operations

    @Override
    @ForceInline
    public Float64Vector fma(Vector<Float,Shapes.S64Bit> o1, Vector<Float,Shapes.S64Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Float64Vector v1 = (Float64Vector)o1;
        Float64Vector v2 = (Float64Vector)o2;
        return (Float64Vector) VectorIntrinsics.ternaryOp(
            VECTOR_OP_FMA, Float64Vector.class, float.class, LENGTH,
            this, v1, v2,
            (w1, w2, w3) -> w1.tOp(w2, w3, (i, a, b, c) -> Math.fma(a, b, c)));
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public float addAll() {
        int bits = (int) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_ADD, Float64Vector.class, float.class, LENGTH,
                                this,
                                v -> {
                                    float r = v.rOp((float) 0, (i, a, b) -> (float) (a + b));
                                    return (long)Float.floatToIntBits(r);
                                });
        return Float.intBitsToFloat(bits);
    }

    @Override
    @ForceInline
    public float mulAll() {
        int bits = (int) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_MUL, Float64Vector.class, float.class, LENGTH,
                                this,
                                v -> {
                                    float r = v.rOp((float) 1, (i, a, b) -> (float) (a * b));
                                    return (long)Float.floatToIntBits(r);
                                });
        return Float.intBitsToFloat(bits);
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(float[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Float64Vector.class, float.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((float[])arr)[idx + i] = a_));
    }

    @Override
    @ForceInline
    public void intoArray(float[] a, int ax, Mask<Float, Shapes.S64Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Float64Vector oldVal = SPECIES.fromArray(a, ax);
        Float64Vector newVal = oldVal.blend(this, m);
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

        Float64Vector that = (Float64Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Float64Mask bTest(Vector<Float, Shapes.S64Bit> o, FBinTest f) {
        float[] vec1 = getElements();
        float[] vec2 = ((Float64Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Float64Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Float64Mask equal(Vector<Float, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Float64Vector v = (Float64Vector)o;

        return (Float64Mask) VectorIntrinsics.compare(
            BT_eq, Float64Vector.class, Float64Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Float64Mask notEqual(Vector<Float, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Float64Vector v = (Float64Vector)o;

        return (Float64Mask) VectorIntrinsics.compare(
            BT_ne, Float64Vector.class, Float64Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Float64Mask lessThan(Vector<Float, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Float64Vector v = (Float64Vector)o;

        return (Float64Mask) VectorIntrinsics.compare(
            BT_lt, Float64Vector.class, Float64Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Float64Mask lessThanEq(Vector<Float, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Float64Vector v = (Float64Vector)o;

        return (Float64Mask) VectorIntrinsics.compare(
            BT_le, Float64Vector.class, Float64Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Float64Mask greaterThan(Vector<Float, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Float64Vector v = (Float64Vector)o;

        return (Float64Mask) VectorIntrinsics.compare(
            BT_gt, Float64Vector.class, Float64Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Float64Mask greaterThanEq(Vector<Float, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Float64Vector v = (Float64Vector)o;

        return (Float64Mask) VectorIntrinsics.compare(
            BT_ge, Float64Vector.class, Float64Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a >= b));
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        float[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Float, Shapes.S64Bit> o, FUnCon f) {
        boolean[] mbits = ((Float64Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }

    Int64Vector toBits() {
        float[] vec = getElements();
        int[] res = new int[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.floatToIntBits(vec[i]);
        }
        return new Int64Vector(res);
    }


    @Override
    public Float64Vector rotateEL(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Float64Vector(res);
    }

    @Override
    public Float64Vector rotateER(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Float64Vector(res);
    }

    @Override
    public Float64Vector shiftEL(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Float64Vector(res);
    }

    @Override
    public Float64Vector shiftER(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Float64Vector(res);
    }

    @Override
    public Float64Vector shuffle(Vector<Float, Shapes.S64Bit> o, Shuffle<Float, Shapes.S64Bit> s) {
        Float64Vector v = (Float64Vector) o;
        return uOp((i, a) -> {
            float[] vec = this.getElements();
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
    public Float64Vector swizzle(Shuffle<Float, Shapes.S64Bit> s) {
        return uOp((i, a) -> {
            float[] vec = this.getElements();
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
    public Float64Vector blend(Vector<Float, Shapes.S64Bit> o1, Mask<Float, Shapes.S64Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Float64Vector v = (Float64Vector)o1;
        Float64Mask   m = (Float64Mask)o2;

        return (Float64Vector) VectorIntrinsics.blend(
            Float64Vector.class, Float64Mask.class, float.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    @Override
    @ForceInline
    @SuppressWarnings("unchecked")
    public <F> Vector<F, Shapes.S64Bit> rebracket(Species<F, Shapes.S64Bit> species) {
        Objects.requireNonNull(species);
        // TODO: check proper element type
        // TODO: update to pass the two species as an arguments and ideally
        // push down intrinsic call into species implementation
        return VectorIntrinsics.rebracket(
            Float64Vector.class, float.class, LENGTH,
            species.elementType(), this,
            (v, t) -> species.reshape(v)
        );
    }

    // Accessors

    @Override
    public float get(int i) {
        float[] vec = getElements();
        return vec[i];
    }

    @Override
    public Float64Vector with(int i, float e) {
        float[] res = vec.clone();
        res[i] = e;
        return new Float64Vector(res);
    }

    // Mask

    static final class Float64Mask extends AbstractMask<Float, Shapes.S64Bit> {
        static final Float64Mask TRUE_MASK = new Float64Mask(true);
        static final Float64Mask FALSE_MASK = new Float64Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Float64Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Float64Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
        }

        public Float64Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Float64Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Float64Mask(res);
        }

        @Override
        Float64Mask bOp(Mask<Float, Shapes.S64Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Float64Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Float64Mask(res);
        }

        @Override
        public Float64Species species() {
            return SPECIES;
        }

        @Override
        public Float64Vector toVector() {
            float[] res = new float[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (float) (bits[i] ? -1 : 0);
            }
            return new Float64Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S64Bit> rebracket(Species<Z, Shapes.S64Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.rebracket(
                Float64Mask.class, float.class, LENGTH,
                species.elementType(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        //Mask<E, S> not();

        // Binary operations

        @Override
        @ForceInline
        public Float64Mask and(Mask<Float,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Float64Mask m = (Float64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Float64Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Float64Mask or(Mask<Float,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Float64Mask m = (Float64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Float64Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Float64Mask.class, int.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Float64Mask.class, int.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Float64Shuffle extends AbstractShuffle<Float, Shapes.S64Bit> {
        static final IntVector.IntSpecies<Shapes.S64Bit> INT_SPECIES = (IntVector.IntSpecies<Shapes.S64Bit>) Vector.speciesInstance(Integer.class, Shapes.S_64_BIT);

        public Float64Shuffle(int[] reorder) {
            super(reorder);
        }

        public Float64Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Float64Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S64Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Float64Species species() {
        return SPECIES;
    }

    static final class Float64Species extends FloatSpecies<Shapes.S64Bit> {
        static final int BIT_SIZE = Shapes.S_64_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Float.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(float.class.getSimpleName()).append("s x ");
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
        public Class<Float> elementType() {
            return Float.class;
        }

        @Override
        public int elementSize() {
            return Float.SIZE;
        }

        @Override
        public Shapes.S64Bit shape() {
            return Shapes.S_64_BIT;
        }

        @Override
        Float64Vector op(FOp f) {
            float[] res = new float[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Float64Vector(res);
        }

        @Override
        Float64Vector op(Mask<Float, Shapes.S64Bit> o, FOp f) {
            float[] res = new float[length()];
            boolean[] mbits = ((Float64Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Float64Vector(res);
        }

        // Factories

        @Override
        public Float64Mask maskFromValues(boolean... bits) {
            return new Float64Mask(bits);
        }

        @Override
        public Float64Mask maskFromArray(boolean[] bits, int i) {
            return new Float64Mask(bits, i);
        }

        @Override
        public Float64Shuffle shuffleFromValues(int... ixs) {
            return new Float64Shuffle(ixs);
        }

        @Override
        public Float64Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Float64Shuffle(ixs, i);
        }

        @Override
        @ForceInline
        public Float64Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Float64Vector.class, float.class, LENGTH,
                                                     Float.floatToIntBits(0.0f),
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Float64Vector broadcast(float e) {
            return VectorIntrinsics.broadcastCoerced(
                Float64Vector.class, float.class, LENGTH,
                Float.floatToIntBits(e),
                ((long bits) -> SPECIES.op(i -> Float.intBitsToFloat((int)bits))));
        }

        @Override
        @ForceInline
        public Float64Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Float64Mask.class, int.class, LENGTH,
                                                     (int)-1,
                                                     (z -> Float64Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Float64Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Float64Mask.class, int.class, LENGTH,
                                                     0,
                                                     (z -> Float64Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Float64Vector fromArray(float[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Float64Vector) VectorIntrinsics.load(Float64Vector.class, float.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((float[]) arr, idx));
        }

        @Override
        @ForceInline
        public Float64Vector fromArray(float[] a, int ax, Mask<Float, Shapes.S64Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }
    }
}
