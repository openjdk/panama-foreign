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
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Int512Vector extends IntVector<Shapes.S512Bit> {
    static final Int512Species SPECIES = new Int512Species();

    static final Int512Vector ZERO = new Int512Vector();

    static final int LENGTH = SPECIES.length();

    private final int[] vec; // Don't access directly, use getElements() instead.

    private int[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Int512Vector() {
        vec = new int[SPECIES.length()];
    }

    Int512Vector(int[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Int512Vector uOp(FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Int512Vector(res);
    }

    @Override
    Int512Vector uOp(Mask<Integer, Shapes.S512Bit> o, FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        boolean[] mbits = ((Int512Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Int512Vector(res);
    }

    // Binary operator

    @Override
    Int512Vector bOp(Vector<Integer, Shapes.S512Bit> o, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int512Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int512Vector(res);
    }

    @Override
    Int512Vector bOp(Vector<Integer, Shapes.S512Bit> o1, Mask<Integer, Shapes.S512Bit> o2, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int512Vector)o1).getElements();
        boolean[] mbits = ((Int512Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Int512Vector(res);
    }

    // Trinary operator

    @Override
    Int512Vector tOp(Vector<Integer, Shapes.S512Bit> o1, Vector<Integer, Shapes.S512Bit> o2, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int512Vector)o1).getElements();
        int[] vec3 = ((Int512Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Int512Vector(res);
    }

    @Override
    Int512Vector tOp(Vector<Integer, Shapes.S512Bit> o1, Vector<Integer, Shapes.S512Bit> o2, Mask<Integer, Shapes.S512Bit> o3, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = getElements();
        int[] vec2 = ((Int512Vector)o1).getElements();
        int[] vec3 = ((Int512Vector)o2).getElements();
        boolean[] mbits = ((Int512Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Int512Vector(res);
    }

    @Override
    int rOp(int v, FBinOp f) {
        int[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> add(int o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> add(int o, Mask<Integer,Shapes.S512Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> sub(int o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> sub(int o, Mask<Integer,Shapes.S512Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> mul(int o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> mul(int o, Mask<Integer,Shapes.S512Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> min(int o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> max(int o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S512Bit> equal(int o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S512Bit> notEqual(int o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S512Bit> lessThan(int o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S512Bit> lessThanEq(int o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S512Bit> greaterThan(int o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S512Bit> greaterThanEq(int o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> blend(int o, Mask<Integer,Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> and(int o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> and(int o, Mask<Integer,Shapes.S512Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> or(int o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> or(int o, Mask<Integer,Shapes.S512Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> xor(int o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S512Bit> xor(int o, Mask<Integer,Shapes.S512Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Int512Vector neg() {
        return SPECIES.zero().sub(this);
    }

    // Unary operations

    @Override
    @ForceInline
    public Int512Vector abs() {
        return (Int512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Int512Vector.class, int.class, LENGTH,
            this,
            v1 -> ((Int512Vector)v1).uOp((i, a) -> (int) Math.abs(a)));
    }


    @Override
    @ForceInline
    public Int512Vector not() {
        return (Int512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Int512Vector.class, int.class, LENGTH,
            this,
            v1 -> ((Int512Vector)v1).uOp((i, a) -> (int) ~a));
    }
    // Binary operations

    @Override
    @ForceInline
    public Int512Vector add(Vector<Integer,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2, (i, a, b) -> (int)(a + b)));
    }

    @Override
    @ForceInline
    public Int512Vector sub(Vector<Integer,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2, (i, a, b) -> (int)(a - b)));
    }

    @Override
    @ForceInline
    public Int512Vector mul(Vector<Integer,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2, (i, a, b) -> (int)(a * b)));
    }

    @Override
    @ForceInline
    public Int512Vector min(Vector<Integer,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2, (i, a, b) -> (int) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Int512Vector max(Vector<Integer,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2, (i, a, b) -> (int) ((a > b) ? a : b)));
        }

    @Override
    @ForceInline
    public Int512Vector add(Vector<Integer,Shapes.S512Bit> v, Mask<Integer, Shapes.S512Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (int)(a + b));
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Int512Vector sub(Vector<Integer,Shapes.S512Bit> v, Mask<Integer, Shapes.S512Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (int)(a - b));
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Int512Vector mul(Vector<Integer,Shapes.S512Bit> v, Mask<Integer, Shapes.S512Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (int)(a * b));
        return blend(mul(v), m);
    }


    @Override
    @ForceInline
    public Int512Vector and(Vector<Integer,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2, (i, a, b) -> (int)(a & b)));
    }

    @Override
    @ForceInline
    public Int512Vector or(Vector<Integer,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2, (i, a, b) -> (int)(a | b)));
    }

    @Override
    @ForceInline
    public Int512Vector xor(Vector<Integer,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2, (i, a, b) -> (int)(a ^ b)));
    }

    @Override
    @ForceInline
    public Int512Vector and(Vector<Integer,Shapes.S512Bit> v, Mask<Integer, Shapes.S512Bit> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Int512Vector or(Vector<Integer,Shapes.S512Bit> v, Mask<Integer, Shapes.S512Bit> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Int512Vector xor(Vector<Integer,Shapes.S512Bit> v, Mask<Integer, Shapes.S512Bit> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Int512Vector shiftL(int s) {
        return (Int512Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Int512Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a << i)));
    }

    @Override
    @ForceInline
    public Int512Vector shiftR(int s) {
        return (Int512Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Int512Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a >>> i)));
    }

    @Override
    @ForceInline
    public Int512Vector aShiftR(int s) {
        return (Int512Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Int512Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a >> i)));
    }

    @Override
    @ForceInline
    public Int512Vector shiftL(Vector<Integer,Shapes.S512Bit> s) {
        Int512Vector v = (Int512Vector)s;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_LSHIFT, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2,(i,a, b) -> (int) (a << b)));
    }

    @Override
    @ForceInline
    public Int512Vector shiftR(Vector<Integer,Shapes.S512Bit> s) {
        Int512Vector v = (Int512Vector)s;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_URSHIFT, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2,(i,a, b) -> (int) (a >>> b)));
    }

    @Override
    @ForceInline
    public Int512Vector ashiftR(Vector<Integer,Shapes.S512Bit> s) {
        Int512Vector v = (Int512Vector)s;
        return (Int512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_RSHIFT, Int512Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int512Vector)v1).bOp(v2,(i,a, b) -> (int) (a >> b)));
    }
    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public int addAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Int512Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a + b)));
    }

    @Override
    @ForceInline
    public int andAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Int512Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) -1, (i, a, b) -> (int) (a & b)));
    }

    @Override
    @ForceInline
    public int andAll(Mask<Integer, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((int) -1), m).andAll();
    }

    @Override
    @ForceInline
    public int minAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MIN, Int512Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp(Integer.MAX_VALUE , (i, a, b) -> (int) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public int maxAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MAX, Int512Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp(Integer.MIN_VALUE , (i, a, b) -> (int) ((a > b) ? a : b)));
    }

    @Override
    @ForceInline
    public int mulAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Int512Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 1, (i, a, b) -> (int) (a * b)));
    }

    @Override
    @ForceInline
    public int subAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_SUB, Int512Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a - b)));
    }

    @Override
    @ForceInline
    public int orAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Int512Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a | b)));
    }

    @Override
    @ForceInline
    public int orAll(Mask<Integer, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((int) 0), m).orAll();
    }

    @Override
    @ForceInline
    public int xorAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Int512Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a ^ b)));
    }

    @Override
    @ForceInline
    public int xorAll(Mask<Integer, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((int) 0), m).xorAll();
    }


    @Override
    @ForceInline
    public int addAll(Mask<Integer, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((int) 0), m).addAll();
    }

    @Override
    @ForceInline
    public int subAll(Mask<Integer, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((int) 0), m).subAll();
    }

    @Override
    @ForceInline
    public int mulAll(Mask<Integer, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((int) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public int minAll(Mask<Integer, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(Integer.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public int maxAll(Mask<Integer, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(Integer.MIN_VALUE), m).maxAll();
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(int[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Int512Vector.class, int.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((int[])arr)[idx + i] = a_));
    }

    @Override
    @ForceInline
    public void intoArray(int[] a, int ax, Mask<Integer, Shapes.S512Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Int512Vector oldVal = SPECIES.fromArray(a, ax);
        Int512Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Int512Vector.class, int.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> {
                                   byte[] tarr = (byte[])arr;
                                   ByteBuffer bb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                   IntBuffer fb = bb.asIntBuffer();
                                   v.forEach((i, e) -> fb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix, Mask<Integer, Shapes.S512Bit> m) {
        Int512Vector oldVal = SPECIES.fromByteArray(a, ix);
        Int512Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
            VectorIntrinsics.store(Int512Vector.class, int.class, LENGTH,
                                   bb.array(), ax, this,
                                   (arr, idx, v) -> {
                                       byte[] tarr = (byte[])arr;
                                       ByteBuffer lbb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                       IntBuffer fb = lbb.asIntBuffer();
                                       v.forEach((i, e) -> fb.put(e));
                                   });
        } else {
            super.intoByteBuffer(bb, ix);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Integer, Shapes.S512Bit> m) {
        Int512Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Int512Vector newVal = oldVal.blend(this, m);
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

        Int512Vector that = (Int512Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Int512Mask bTest(Vector<Integer, Shapes.S512Bit> o, FBinTest f) {
        int[] vec1 = getElements();
        int[] vec2 = ((Int512Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int512Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Int512Mask equal(Vector<Integer, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;

        return (Int512Mask) VectorIntrinsics.compare(
            BT_eq, Int512Vector.class, Int512Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Int512Mask notEqual(Vector<Integer, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;

        return (Int512Mask) VectorIntrinsics.compare(
            BT_ne, Int512Vector.class, Int512Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Int512Mask lessThan(Vector<Integer, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;

        return (Int512Mask) VectorIntrinsics.compare(
            BT_lt, Int512Vector.class, Int512Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Int512Mask lessThanEq(Vector<Integer, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;

        return (Int512Mask) VectorIntrinsics.compare(
            BT_le, Int512Vector.class, Int512Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Int512Mask greaterThan(Vector<Integer, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;

        return (Int512Mask) VectorIntrinsics.compare(
            BT_gt, Int512Vector.class, Int512Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Int512Mask greaterThanEq(Vector<Integer, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Int512Vector v = (Int512Vector)o;

        return (Int512Mask) VectorIntrinsics.compare(
            BT_ge, Int512Vector.class, Int512Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a >= b));
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        int[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Integer, Shapes.S512Bit> o, FUnCon f) {
        boolean[] mbits = ((Int512Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }


    Float512Vector toFP() {
        int[] vec = getElements();
        float[] res = new float[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.intBitsToFloat(vec[i]);
        }
        return new Float512Vector(res);
    }

    @Override
    public Int512Vector rotateEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Int512Vector(res);
    }

    @Override
    public Int512Vector rotateER(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Int512Vector(res);
    }

    @Override
    public Int512Vector shiftEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Int512Vector(res);
    }

    @Override
    public Int512Vector shiftER(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Int512Vector(res);
    }

    @Override
    public Int512Vector shuffle(Vector<Integer, Shapes.S512Bit> o, Shuffle<Integer, Shapes.S512Bit> s) {
        Int512Vector v = (Int512Vector) o;
        return uOp((i, a) -> {
            int[] vec = this.getElements();
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
    public Int512Vector swizzle(Shuffle<Integer, Shapes.S512Bit> s) {
        return uOp((i, a) -> {
            int[] vec = this.getElements();
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
    public Int512Vector blend(Vector<Integer, Shapes.S512Bit> o1, Mask<Integer, Shapes.S512Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Int512Vector v = (Int512Vector)o1;
        Int512Mask   m = (Int512Mask)o2;

        return (Int512Vector) VectorIntrinsics.blend(
            Int512Vector.class, Int512Mask.class, int.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public int get(int i) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return (int) VectorIntrinsics.extract(
                                Int512Vector.class, int.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    int[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Int512Vector with(int i, int e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Int512Vector.class, int.class, LENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    int[] res = v.getElements().clone();
                                    res[ix] = (int)bits;
                                    return new Int512Vector(res);
                                });
    }

    // Mask

    static final class Int512Mask extends AbstractMask<Integer, Shapes.S512Bit> {
        static final Int512Mask TRUE_MASK = new Int512Mask(true);
        static final Int512Mask FALSE_MASK = new Int512Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Int512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Int512Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
        }

        public Int512Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Int512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Int512Mask(res);
        }

        @Override
        Int512Mask bOp(Mask<Integer, Shapes.S512Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Int512Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Int512Mask(res);
        }

        @Override
        public Int512Species species() {
            return SPECIES;
        }

        @Override
        public Int512Vector toVector() {
            int[] res = new int[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (int) (bits[i] ? -1 : 0);
            }
            return new Int512Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S512Bit> rebracket(Species<Z, Shapes.S512Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.reinterpret(
                Int512Mask.class, int.class, LENGTH,
                species.elementType(), species.length(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        @Override
        @ForceInline
        public Int512Mask not() {
            return (Int512Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Int512Mask.class, int.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Int512Mask and(Mask<Integer,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Int512Mask m = (Int512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Int512Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Int512Mask or(Mask<Integer,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Int512Mask m = (Int512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Int512Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Int512Mask.class, int.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Int512Mask.class, int.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Int512Shuffle extends AbstractShuffle<Integer, Shapes.S512Bit> {
        static final IntVector.IntSpecies<Shapes.S512Bit> INT_SPECIES = IntVector.species(Shapes.S_512_BIT);

        public Int512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Int512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Int512Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S512Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Int512Species species() {
        return SPECIES;
    }

    static final class Int512Species extends IntSpecies<Shapes.S512Bit> {
        static final int BIT_SIZE = Shapes.S_512_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Integer.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(int.class.getSimpleName()).append("s x ");
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
        public Class<Integer> elementType() {
            return int.class;
        }

        @Override
        @ForceInline
        public int elementSize() {
            return Integer.SIZE;
        }

        @Override
        @ForceInline
        public Shapes.S512Bit shape() {
            return Shapes.S_512_BIT;
        }

        @Override
        Int512Vector op(FOp f) {
            int[] res = new int[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Int512Vector(res);
        }

        @Override
        Int512Vector op(Mask<Integer, Shapes.S512Bit> o, FOp f) {
            int[] res = new int[length()];
            boolean[] mbits = ((Int512Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Int512Vector(res);
        }

        // Factories

        @Override
        public Int512Mask maskFromValues(boolean... bits) {
            return new Int512Mask(bits);
        }

        @Override
        public Int512Mask maskFromArray(boolean[] bits, int i) {
            return new Int512Mask(bits, i);
        }

        @Override
        public Int512Shuffle shuffleFromValues(int... ixs) {
            return new Int512Shuffle(ixs);
        }

        @Override
        public Int512Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Int512Shuffle(ixs, i);
        }

        @Override
        public Int512Shuffle shuffleFromVector(Vector<Integer, Shapes.S512Bit> v) {
            int[] a = ((IntVector<Shapes.S512Bit>) v).toArray();
            return new Int512Shuffle(a, 0);
        }

        @Override
        @ForceInline
        public Int512Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Int512Vector.class, int.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Int512Vector broadcast(int e) {
            return VectorIntrinsics.broadcastCoerced(
                Int512Vector.class, int.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (int)bits)));
        }

        @Override
        @ForceInline
        public Int512Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Int512Mask.class, int.class, LENGTH,
                                                     (int)-1,
                                                     (z -> Int512Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Int512Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Int512Mask.class, int.class, LENGTH,
                                                     0,
                                                     (z -> Int512Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Int512Vector scalars(int... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return (Int512Vector) VectorIntrinsics.load(Int512Vector.class, int.class, LENGTH,
                                                        es, ix,
                                                        (arr, idx) -> super.fromArray((int[]) arr, idx));
        }

        @Override
        @ForceInline
        public Int512Vector fromArray(int[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Int512Vector) VectorIntrinsics.load(Int512Vector.class, int.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((int[]) arr, idx));
        }

        @Override
        @ForceInline
        public Int512Vector fromArray(int[] a, int ax, Mask<Integer, Shapes.S512Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }

        @Override
        @ForceInline
        public Int512Vector fromByteArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return (Int512Vector) VectorIntrinsics.load(Int512Vector.class, int.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
        }

        @Override
        @ForceInline
        public Int512Vector fromByteArray(byte[] a, int ix, Mask<Integer, Shapes.S512Bit> m) {
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Int512Vector fromByteBuffer(ByteBuffer bb, int ix) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
                return (Int512Vector) VectorIntrinsics.load(Int512Vector.class, int.class, LENGTH,
                                                            bb.array(), ax,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Int512Vector)super.fromByteBuffer(bb, ix);
            }
        }

        @Override
        @ForceInline
        public Int512Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Integer, Shapes.S512Bit> m) {
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int512Vector castFromByte(ByteVector<S> o) {
            if (o.bitSize() == 64) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.cast(
                    Byte64Vector.class, byte.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.cast(
                    Byte128Vector.class, byte.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.cast(
                    Byte256Vector.class, byte.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.cast(
                    Byte512Vector.class, byte.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int512Vector castFromShort(ShortVector<S> o) {
            if (o.bitSize() == 64) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.cast(
                    Short64Vector.class, short.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.cast(
                    Short128Vector.class, short.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.cast(
                    Short256Vector.class, short.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.cast(
                    Short512Vector.class, short.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int512Vector castFromInt(IntVector<S> o) {
            if (o.bitSize() == 64) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.cast(
                    Int64Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.cast(
                    Int128Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.cast(
                    Int256Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.cast(
                    Int512Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int512Vector castFromLong(LongVector<S> o) {
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.cast(
                    Long64Vector.class, long.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.cast(
                    Long128Vector.class, long.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.cast(
                    Long256Vector.class, long.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.cast(
                    Long512Vector.class, long.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int512Vector castFromFloat(FloatVector<S> o) {
            if (o.bitSize() == 64) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.cast(
                    Float64Vector.class, float.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.cast(
                    Float128Vector.class, float.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.cast(
                    Float256Vector.class, float.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.cast(
                    Float512Vector.class, float.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int512Vector castFromDouble(DoubleVector<S> o) {
            if (o.bitSize() == 64) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.cast(
                    Double64Vector.class, double.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.cast(
                    Double128Vector.class, double.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.cast(
                    Double256Vector.class, double.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.cast(
                    Double512Vector.class, double.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <E, S extends Shape> Int512Vector cast(Vector<E, S> o) {
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
        public <F> Int512Vector rebracket(Vector<F, Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte512Vector.class, byte.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short512Vector.class, short.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int512Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long512Vector.class, long.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float512Vector.class, float.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double512Vector.class, double.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Int512Vector resize(Vector<Integer, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int64Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int128Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int256Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int512Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int512Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
