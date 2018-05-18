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
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Float128Vector extends FloatVector<Shapes.S128Bit> {
    static final Float128Species SPECIES = new Float128Species();

    static final Float128Vector ZERO = new Float128Vector();

    static final int LENGTH = SPECIES.length();

    private final float[] vec; // Don't access directly, use getElements() instead.

    private float[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Float128Vector() {
        vec = new float[SPECIES.length()];
    }

    Float128Vector(float[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Float128Vector uOp(FUnOp f) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Float128Vector(res);
    }

    @Override
    Float128Vector uOp(Mask<Float, Shapes.S128Bit> o, FUnOp f) {
        float[] vec = getElements();
        float[] res = new float[length()];
        boolean[] mbits = ((Float128Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Float128Vector(res);
    }

    // Binary operator

    @Override
    Float128Vector bOp(Vector<Float, Shapes.S128Bit> o, FBinOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((Float128Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Float128Vector(res);
    }

    @Override
    Float128Vector bOp(Vector<Float, Shapes.S128Bit> o1, Mask<Float, Shapes.S128Bit> o2, FBinOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((Float128Vector)o1).getElements();
        boolean[] mbits = ((Float128Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Float128Vector(res);
    }

    // Trinary operator

    @Override
    Float128Vector tOp(Vector<Float, Shapes.S128Bit> o1, Vector<Float, Shapes.S128Bit> o2, FTriOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((Float128Vector)o1).getElements();
        float[] vec3 = ((Float128Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Float128Vector(res);
    }

    @Override
    Float128Vector tOp(Vector<Float, Shapes.S128Bit> o1, Vector<Float, Shapes.S128Bit> o2, Mask<Float, Shapes.S128Bit> o3, FTriOp f) {
        float[] res = new float[length()];
        float[] vec1 = getElements();
        float[] vec2 = ((Float128Vector)o1).getElements();
        float[] vec3 = ((Float128Vector)o2).getElements();
        boolean[] mbits = ((Float128Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Float128Vector(res);
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
    public FloatVector<Shapes.S128Bit> add(float o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> add(float o, Mask<Float,Shapes.S128Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> sub(float o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> sub(float o, Mask<Float,Shapes.S128Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> mul(float o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> mul(float o, Mask<Float,Shapes.S128Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> min(float o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> max(float o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S128Bit> equal(float o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S128Bit> notEqual(float o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S128Bit> lessThan(float o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S128Bit> lessThanEq(float o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S128Bit> greaterThan(float o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Float, Shapes.S128Bit> greaterThanEq(float o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> blend(float o, Mask<Float,Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> div(float o) {
        return div(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> div(float o, Mask<Float,Shapes.S128Bit> m) {
        return div(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> atan2(float o) {
        return atan2(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> atan2(float o, Mask<Float,Shapes.S128Bit> m) {
        return atan2(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> pow(float o) {
        return pow(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> pow(float o, Mask<Float,Shapes.S128Bit> m) {
        return pow(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> fma(float o1, float o2) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> fma(float o1, float o2, Mask<Float,Shapes.S128Bit> m) {
        return fma(SPECIES.broadcast(o1), SPECIES.broadcast(o2), m);
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> hypot(float o) {
        return hypot(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public FloatVector<Shapes.S128Bit> hypot(float o, Mask<Float,Shapes.S128Bit> m) {
        return hypot(SPECIES.broadcast(o), m);
    }


    // Unary operations

    @Override
    @ForceInline
    public Float128Vector abs() {
        return (Float128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Float128Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float128Vector)v1).uOp((i, a) -> (float) Math.abs(a)));
    }

    @Override
    @ForceInline
    public Float128Vector neg() {
        return (Float128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NEG, Float128Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float128Vector)v1).uOp((i, a) -> (float) -a));
    }

    @Override
    @ForceInline
    public Float128Vector div(Vector<Float,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;
        return (Float128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Float128Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float128Vector)v1).bOp(v2, (i, a, b) -> (float)(a / b)));
    }

    @Override
    @ForceInline
    public Float128Vector sqrt() {
        return (Float128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_SQRT, Float128Vector.class, float.class, LENGTH,
            this,
            v1 -> ((Float128Vector)v1).uOp((i, a) -> (float) Math.sqrt((double) a)));
    }

    // Binary operations

    @Override
    @ForceInline
    public Float128Vector add(Vector<Float,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;
        return (Float128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Float128Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float128Vector)v1).bOp(v2, (i, a, b) -> (float)(a + b)));
    }

    @Override
    @ForceInline
    public Float128Vector sub(Vector<Float,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;
        return (Float128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Float128Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float128Vector)v1).bOp(v2, (i, a, b) -> (float)(a - b)));
    }

    @Override
    @ForceInline
    public Float128Vector mul(Vector<Float,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;
        return (Float128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Float128Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float128Vector)v1).bOp(v2, (i, a, b) -> (float)(a * b)));
    }

    @Override
    @ForceInline
    public Float128Vector min(Vector<Float,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;
        return (Float128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Float128Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float128Vector)v1).bOp(v2, (i, a, b) -> (float) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Float128Vector max(Vector<Float,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;
        return (Float128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Float128Vector.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> ((Float128Vector)v1).bOp(v2, (i, a, b) -> (float) ((a > b) ? a : b)));
        }

    @Override
    @ForceInline
    public Float128Vector add(Vector<Float,Shapes.S128Bit> v, Mask<Float, Shapes.S128Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (float)(a + b));
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Float128Vector sub(Vector<Float,Shapes.S128Bit> v, Mask<Float, Shapes.S128Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (float)(a - b));
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Float128Vector mul(Vector<Float,Shapes.S128Bit> v, Mask<Float, Shapes.S128Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (float)(a * b));
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Float128Vector div(Vector<Float,Shapes.S128Bit> v, Mask<Float, Shapes.S128Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (float)(a / b));
        return blend(div(v), m);
    }


    // Ternary operations

    @Override
    @ForceInline
    public Float128Vector fma(Vector<Float,Shapes.S128Bit> o1, Vector<Float,Shapes.S128Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Float128Vector v1 = (Float128Vector)o1;
        Float128Vector v2 = (Float128Vector)o2;
        return (Float128Vector) VectorIntrinsics.ternaryOp(
            VECTOR_OP_FMA, Float128Vector.class, float.class, LENGTH,
            this, v1, v2,
            (w1, w2, w3) -> w1.tOp(w2, w3, (i, a, b, c) -> Math.fma(a, b, c)));
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public float addAll() {
        int bits = (int) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_ADD, Float128Vector.class, float.class, LENGTH,
                                this,
                                v -> {
                                    float r = v.rOp((float) 0, (i, a, b) -> (float) (a + b));
                                    return (long)Float.floatToIntBits(r);
                                });
        return Float.intBitsToFloat(bits);
    }

    @Override
    @ForceInline
    public float subAll() {
        int bits = (int) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_SUB, Float128Vector.class, float.class, LENGTH,
                                this,
                                v -> {
                                    float r = v.rOp((float) 0, (i, a, b) -> (float) (a - b));
                                    return (long)Float.floatToIntBits(r);
                                });
        return Float.intBitsToFloat(bits);
    }

    @Override
    @ForceInline
    public float mulAll() {
        int bits = (int) VectorIntrinsics.reductionCoerced(
                                VECTOR_OP_MUL, Float128Vector.class, float.class, LENGTH,
                                this,
                                v -> {
                                    float r = v.rOp((float) 1, (i, a, b) -> (float) (a * b));
                                    return (long)Float.floatToIntBits(r);
                                });
        return Float.intBitsToFloat(bits);
    }


    @Override
    @ForceInline
    public float addAll(Mask<Float, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((float) 0), m).addAll();
    }

    @Override
    @ForceInline
    public float subAll(Mask<Float, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((float) 0), m).subAll();
    }

    @Override
    @ForceInline
    public float mulAll(Mask<Float, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast((float) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public float minAll(Mask<Float, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast(Float.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public float maxAll(Mask<Float, Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast(Float.MIN_VALUE), m).maxAll();
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(float[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Float128Vector.class, float.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((float[])arr)[idx + i] = a_));
    }

    @Override
    @ForceInline
    public void intoArray(float[] a, int ax, Mask<Float, Shapes.S128Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Float128Vector oldVal = SPECIES.fromArray(a, ax);
        Float128Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Float128Vector.class, float.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> {
                                   byte[] tarr = (byte[])arr;
                                   ByteBuffer bb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                   FloatBuffer fb = bb.asFloatBuffer();
                                   v.forEach((i, e) -> fb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix, Mask<Float, Shapes.S128Bit> m) {
        Float128Vector oldVal = SPECIES.fromByteArray(a, ix);
        Float128Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
            VectorIntrinsics.store(Float128Vector.class, float.class, LENGTH,
                                   bb.array(), ix, this,
                                   (arr, idx, v) -> {
                                       byte[] tarr = (byte[])arr;
                                       ByteBuffer lbb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                       FloatBuffer fb = lbb.asFloatBuffer();
                                       v.forEach((i, e) -> fb.put(e));
                                   });
        } else {
            super.intoByteBuffer(bb);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, Mask<Float, Shapes.S128Bit> m) {
        int idx = bb.position();
        Float128Vector oldVal = SPECIES.fromByteBuffer(bb, idx);
        Float128Vector newVal = oldVal.blend(this, m);
        newVal.intoByteBuffer(bb, idx);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
            VectorIntrinsics.store(Float128Vector.class, float.class, LENGTH,
                                   bb.array(), ax, this,
                                   (arr, idx, v) -> {
                                       byte[] tarr = (byte[])arr;
                                       ByteBuffer lbb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                       FloatBuffer fb = lbb.asFloatBuffer();
                                       v.forEach((i, e) -> fb.put(e));
                                   });
        } else {
            super.intoByteBuffer(bb, ix);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Float, Shapes.S128Bit> m) {
        Float128Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Float128Vector newVal = oldVal.blend(this, m);
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

        Float128Vector that = (Float128Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Float128Mask bTest(Vector<Float, Shapes.S128Bit> o, FBinTest f) {
        float[] vec1 = getElements();
        float[] vec2 = ((Float128Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Float128Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Float128Mask equal(Vector<Float, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;

        return (Float128Mask) VectorIntrinsics.compare(
            BT_eq, Float128Vector.class, Float128Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Float128Mask notEqual(Vector<Float, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;

        return (Float128Mask) VectorIntrinsics.compare(
            BT_ne, Float128Vector.class, Float128Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Float128Mask lessThan(Vector<Float, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;

        return (Float128Mask) VectorIntrinsics.compare(
            BT_lt, Float128Vector.class, Float128Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Float128Mask lessThanEq(Vector<Float, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;

        return (Float128Mask) VectorIntrinsics.compare(
            BT_le, Float128Vector.class, Float128Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Float128Mask greaterThan(Vector<Float, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;

        return (Float128Mask) VectorIntrinsics.compare(
            BT_gt, Float128Vector.class, Float128Mask.class, float.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Float128Mask greaterThanEq(Vector<Float, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Float128Vector v = (Float128Vector)o;

        return (Float128Mask) VectorIntrinsics.compare(
            BT_ge, Float128Vector.class, Float128Mask.class, float.class, LENGTH,
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
    void forEach(Mask<Float, Shapes.S128Bit> o, FUnCon f) {
        boolean[] mbits = ((Float128Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }

    Int128Vector toBits() {
        float[] vec = getElements();
        int[] res = new int[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.floatToIntBits(vec[i]);
        }
        return new Int128Vector(res);
    }


    @Override
    public Float128Vector rotateEL(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Float128Vector(res);
    }

    @Override
    public Float128Vector rotateER(int j) {
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
        return new Float128Vector(res);
    }

    @Override
    public Float128Vector shiftEL(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Float128Vector(res);
    }

    @Override
    public Float128Vector shiftER(int j) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Float128Vector(res);
    }

    @Override
    public Float128Vector shuffle(Vector<Float, Shapes.S128Bit> o, Shuffle<Float, Shapes.S128Bit> s) {
        Float128Vector v = (Float128Vector) o;
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
    public Float128Vector swizzle(Shuffle<Float, Shapes.S128Bit> s) {
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
    public Float128Vector blend(Vector<Float, Shapes.S128Bit> o1, Mask<Float, Shapes.S128Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Float128Vector v = (Float128Vector)o1;
        Float128Mask   m = (Float128Mask)o2;

        return (Float128Vector) VectorIntrinsics.blend(
            Float128Vector.class, Float128Mask.class, float.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public float get(int i) {
        float[] vec = getElements();
        return vec[i];
    }

    @Override
    public Float128Vector with(int i, float e) {
        float[] res = vec.clone();
        res[i] = e;
        return new Float128Vector(res);
    }

    // Mask

    static final class Float128Mask extends AbstractMask<Float, Shapes.S128Bit> {
        static final Float128Mask TRUE_MASK = new Float128Mask(true);
        static final Float128Mask FALSE_MASK = new Float128Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Float128Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Float128Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
        }

        public Float128Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Float128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Float128Mask(res);
        }

        @Override
        Float128Mask bOp(Mask<Float, Shapes.S128Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Float128Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Float128Mask(res);
        }

        @Override
        public Float128Species species() {
            return SPECIES;
        }

        @Override
        public Float128Vector toVector() {
            float[] res = new float[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (float) (bits[i] ? -1 : 0);
            }
            return new Float128Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S128Bit> rebracket(Species<Z, Shapes.S128Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.reinterpret(
                Float128Mask.class, float.class, LENGTH,
                species.elementType(), species.length(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        @Override
        @ForceInline
        public Float128Mask not() {
            return (Float128Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Float128Mask.class, int.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Float128Mask and(Mask<Float,Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            Float128Mask m = (Float128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Float128Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Float128Mask or(Mask<Float,Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            Float128Mask m = (Float128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Float128Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Float128Mask.class, int.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Float128Mask.class, int.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Float128Shuffle extends AbstractShuffle<Float, Shapes.S128Bit> {
        static final IntVector.IntSpecies<Shapes.S128Bit> INT_SPECIES = IntVector.species(Shapes.S_128_BIT);

        public Float128Shuffle(int[] reorder) {
            super(reorder);
        }

        public Float128Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Float128Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S128Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Float128Species species() {
        return SPECIES;
    }

    static final class Float128Species extends FloatSpecies<Shapes.S128Bit> {
        static final int BIT_SIZE = Shapes.S_128_BIT.bitSize();

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
        public Class<Float> elementType() {
            return float.class;
        }

        @Override
        @ForceInline
        public int elementSize() {
            return Float.SIZE;
        }

        @Override
        @ForceInline
        public Shapes.S128Bit shape() {
            return Shapes.S_128_BIT;
        }

        @Override
        Float128Vector op(FOp f) {
            float[] res = new float[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Float128Vector(res);
        }

        @Override
        Float128Vector op(Mask<Float, Shapes.S128Bit> o, FOp f) {
            float[] res = new float[length()];
            boolean[] mbits = ((Float128Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Float128Vector(res);
        }

        // Factories

        @Override
        public Float128Mask maskFromValues(boolean... bits) {
            return new Float128Mask(bits);
        }

        @Override
        public Float128Mask maskFromArray(boolean[] bits, int i) {
            return new Float128Mask(bits, i);
        }

        @Override
        public Float128Shuffle shuffleFromValues(int... ixs) {
            return new Float128Shuffle(ixs);
        }

        @Override
        public Float128Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Float128Shuffle(ixs, i);
        }

        @Override
        public Float128Shuffle shuffleFromVector(Vector<Integer, Shapes.S128Bit> v) {
            int[] a = ((IntVector<Shapes.S128Bit>) v).toArray();
            return new Float128Shuffle(a, 0);
        }

        @Override
        @ForceInline
        public Float128Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Float128Vector.class, float.class, LENGTH,
                                                     Float.floatToIntBits(0.0f),
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Float128Vector broadcast(float e) {
            return VectorIntrinsics.broadcastCoerced(
                Float128Vector.class, float.class, LENGTH,
                Float.floatToIntBits(e),
                ((long bits) -> SPECIES.op(i -> Float.intBitsToFloat((int)bits))));
        }

        @Override
        @ForceInline
        public Float128Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Float128Mask.class, int.class, LENGTH,
                                                     (int)-1,
                                                     (z -> Float128Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Float128Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Float128Mask.class, int.class, LENGTH,
                                                     0,
                                                     (z -> Float128Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Float128Vector scalars(float... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return (Float128Vector) VectorIntrinsics.load(Float128Vector.class, float.class, LENGTH,
                                                        es, ix,
                                                        (arr, idx) -> super.fromArray((float[]) arr, idx));
        }

        @Override
        @ForceInline
        public Float128Vector fromArray(float[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Float128Vector) VectorIntrinsics.load(Float128Vector.class, float.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((float[]) arr, idx));
        }

        @Override
        @ForceInline
        public Float128Vector fromArray(float[] a, int ax, Mask<Float, Shapes.S128Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }

        @Override
        @ForceInline
        public Float128Vector fromByteArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return (Float128Vector) VectorIntrinsics.load(Float128Vector.class, float.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
        }

        @Override
        @ForceInline
        public Float128Vector fromByteArray(byte[] a, int ix, Mask<Float, Shapes.S128Bit> m) {
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Float128Vector fromByteBuffer(ByteBuffer bb) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
                return (Float128Vector) VectorIntrinsics.load(Float128Vector.class, float.class, LENGTH,
                                                            bb.array(), ix,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Float128Vector)super.fromByteBuffer(bb);
            }
        }

        @Override
        @ForceInline
        public Float128Vector fromByteBuffer(ByteBuffer bb, Mask<Float, Shapes.S128Bit> m) {
            return zero().blend(fromByteBuffer(bb), m);
        }

        @Override
        @ForceInline
        public Float128Vector fromByteBuffer(ByteBuffer bb, int ix) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
                return (Float128Vector) VectorIntrinsics.load(Float128Vector.class, float.class, LENGTH,
                                                            bb.array(), ax,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Float128Vector)super.fromByteBuffer(bb, ix);
            }
        }

        @Override
        @ForceInline
        public Float128Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Float, Shapes.S128Bit> m) {
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Float128Vector castFromByte(ByteVector<S> o) {
            if (o.bitSize() == 64) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.cast(
                    Byte64Vector.class, byte.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.cast(
                    Byte128Vector.class, byte.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.cast(
                    Byte256Vector.class, byte.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.cast(
                    Byte512Vector.class, byte.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Float128Vector castFromShort(ShortVector<S> o) {
            if (o.bitSize() == 64) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.cast(
                    Short64Vector.class, short.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.cast(
                    Short128Vector.class, short.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.cast(
                    Short256Vector.class, short.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.cast(
                    Short512Vector.class, short.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Float128Vector castFromInt(IntVector<S> o) {
            if (o.bitSize() == 64) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.cast(
                    Int64Vector.class, int.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.cast(
                    Int128Vector.class, int.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.cast(
                    Int256Vector.class, int.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.cast(
                    Int512Vector.class, int.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Float128Vector castFromLong(LongVector<S> o) {
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.cast(
                    Long64Vector.class, long.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.cast(
                    Long128Vector.class, long.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.cast(
                    Long256Vector.class, long.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.cast(
                    Long512Vector.class, long.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Float128Vector castFromFloat(FloatVector<S> o) {
            if (o.bitSize() == 64) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.cast(
                    Float64Vector.class, float.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.cast(
                    Float128Vector.class, float.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.cast(
                    Float256Vector.class, float.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.cast(
                    Float512Vector.class, float.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Float128Vector castFromDouble(DoubleVector<S> o) {
            if (o.bitSize() == 64) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.cast(
                    Double64Vector.class, double.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.cast(
                    Double128Vector.class, double.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.cast(
                    Double256Vector.class, double.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.cast(
                    Double512Vector.class, double.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <E, S extends Shape> Float128Vector cast(Vector<E, S> o) {
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
        public <F> Float128Vector rebracket(Vector<F, Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte128Vector.class, byte.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short128Vector.class, short.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int128Vector.class, int.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long128Vector.class, long.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float128Vector.class, float.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double128Vector.class, double.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Float128Vector resize(Vector<Float, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float64Vector.class, float.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float128Vector.class, float.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float256Vector.class, float.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float512Vector.class, float.class, so.length(),
                    float.class, LENGTH, so,
                    (v, t) -> (Float128Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
