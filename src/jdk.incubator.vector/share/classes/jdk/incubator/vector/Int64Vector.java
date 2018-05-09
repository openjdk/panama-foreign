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
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Int64Vector extends IntVector<Shapes.S64Bit> {
    static final Int64Species SPECIES = new Int64Species();

    static final Int64Vector ZERO = new Int64Vector();

    static final int LENGTH = SPECIES.length();

    private final int[] vec; // Don't access directly, use getElements() instead.

    private int[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Int64Vector() {
        vec = new int[SPECIES.length()];
    }

    Int64Vector(int[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Int64Vector uOp(FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Int64Vector(res);
    }

    @Override
    Int64Vector uOp(Mask<Integer, Shapes.S64Bit> o, FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        boolean[] mbits = ((Int64Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Int64Vector(res);
    }

    // Binary operator

    @Override
    Int64Vector bOp(Vector<Integer, Shapes.S64Bit> o, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int64Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int64Vector(res);
    }

    @Override
    Int64Vector bOp(Vector<Integer, Shapes.S64Bit> o1, Mask<Integer, Shapes.S64Bit> o2, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int64Vector)o1).getElements();
        boolean[] mbits = ((Int64Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Int64Vector(res);
    }

    // Trinary operator

    @Override
    Int64Vector tOp(Vector<Integer, Shapes.S64Bit> o1, Vector<Integer, Shapes.S64Bit> o2, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int64Vector)o1).getElements();
        int[] vec3 = ((Int64Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Int64Vector(res);
    }

    @Override
    Int64Vector tOp(Vector<Integer, Shapes.S64Bit> o1, Vector<Integer, Shapes.S64Bit> o2, Mask<Integer, Shapes.S64Bit> o3, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = getElements();
        int[] vec2 = ((Int64Vector)o1).getElements();
        int[] vec3 = ((Int64Vector)o2).getElements();
        boolean[] mbits = ((Int64Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Int64Vector(res);
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
    public IntVector<Shapes.S64Bit> add(int o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> add(int o, Mask<Integer,Shapes.S64Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> addSaturate(int o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> addSaturate(int o, Mask<Integer,Shapes.S64Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> sub(int o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> sub(int o, Mask<Integer,Shapes.S64Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> subSaturate(int o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> subSaturate(int o, Mask<Integer,Shapes.S64Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> mul(int o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> mul(int o, Mask<Integer,Shapes.S64Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> min(int o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> max(int o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> equal(int o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> notEqual(int o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> lessThan(int o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> lessThanEq(int o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> greaterThan(int o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> greaterThanEq(int o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> blend(int o, Mask<Integer,Shapes.S64Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> and(int o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> and(int o, Mask<Integer,Shapes.S64Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> or(int o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> or(int o, Mask<Integer,Shapes.S64Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> xor(int o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> xor(int o, Mask<Integer,Shapes.S64Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Int64Vector neg() {
        return SPECIES.zero().sub(this);
    }

    // Unary operations

    @Override
    @ForceInline
    public Int64Vector abs() {
        return (Int64Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Int64Vector.class, int.class, LENGTH,
            this,
            v1 -> ((Int64Vector)v1).uOp((i, a) -> (int) Math.abs(a)));
    }


    @Override
    @ForceInline
    public Int64Vector not() {
        return (Int64Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Int64Vector.class, int.class, LENGTH,
            this,
            v1 -> ((Int64Vector)v1).uOp((i, a) -> (int) ~a));
    }
    // Binary operations

    @Override
    @ForceInline
    public Int64Vector add(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a + b)));
    }

    @Override
    @ForceInline
    public Int64Vector sub(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a - b)));
    }

    @Override
    @ForceInline
    public Int64Vector mul(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a * b)));
    }

    @Override
    @ForceInline
    public Int64Vector min(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Int64Vector max(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int) ((a > b) ? a : b)));
        }

    @Override
    @ForceInline
    public Int64Vector add(Vector<Integer,Shapes.S64Bit> v, Mask<Integer, Shapes.S64Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (int)(a + b));
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Int64Vector sub(Vector<Integer,Shapes.S64Bit> v, Mask<Integer, Shapes.S64Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (int)(a - b));
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Int64Vector mul(Vector<Integer,Shapes.S64Bit> v, Mask<Integer, Shapes.S64Bit> m) {
        // TODO: use better default impl: bOp(o, m, (i, a, b) -> (int)(a * b));
        return blend(mul(v), m);
    }


    @Override
    @ForceInline
    public Int64Vector and(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a & b)));
    }

    @Override
    @ForceInline
    public Int64Vector or(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a | b)));
    }

    @Override
    @ForceInline
    public Int64Vector xor(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a ^ b)));
    }

    @Override
    @ForceInline
    public Int64Vector and(Vector<Integer,Shapes.S64Bit> v, Mask<Integer, Shapes.S64Bit> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Int64Vector or(Vector<Integer,Shapes.S64Bit> v, Mask<Integer, Shapes.S64Bit> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Int64Vector xor(Vector<Integer,Shapes.S64Bit> v, Mask<Integer, Shapes.S64Bit> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Int64Vector shiftL(int s) {
        return (Int64Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Int64Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a << i)));
    }

    @Override
    @ForceInline
    public Int64Vector shiftR(int s) {
        return (Int64Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Int64Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a >>> i)));
    }

    @Override
    @ForceInline
    public Int64Vector aShiftR(int s) {
        return (Int64Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Int64Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a >> i)));
    }

    @Override
    @ForceInline
    public Int64Vector shiftL(Vector<Integer,Shapes.S64Bit> s) {
        Int64Vector v = (Int64Vector)s;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_LSHIFT, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2,(i,a, b) -> (int) (a << b)));
    }

    @Override
    @ForceInline
    public Int64Vector shiftR(Vector<Integer,Shapes.S64Bit> s) {
        Int64Vector v = (Int64Vector)s;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_URSHIFT, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2,(i,a, b) -> (int) (a >>> b)));
    }

    @Override
    @ForceInline
    public Int64Vector ashiftR(Vector<Integer,Shapes.S64Bit> s) {
        Int64Vector v = (Int64Vector)s;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_RSHIFT, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2,(i,a, b) -> (int) (a >> b)));
    }
    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public int addAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Int64Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a + b)));
    }

    @Override
    @ForceInline
    public int andAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Int64Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) -1, (i, a, b) -> (int) (a & b)));
    }

    @Override
    @ForceInline
    public int subAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_SUB, Int64Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a - b)));
    }

    @Override
    @ForceInline
    public int mulAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Int64Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 1, (i, a, b) -> (int) (a * b)));
    }

    @Override
    @ForceInline
    public int orAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Int64Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a | b)));
    }

    @Override
    @ForceInline
    public int xorAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Int64Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a ^ b)));
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(int[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Int64Vector.class, int.class, LENGTH,
                               a, ix, this,
                               (arr, idx) -> super.intoArray((int[]) arr, idx));
    }

    @Override
    @ForceInline
    public void intoArray(int[] a, int ax, Mask<Integer, Shapes.S64Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Int64Vector oldVal = SPECIES.fromArray(a, ax);
        Int64Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Int64Vector.class, int.class, LENGTH,
                               a, ix, this,
                               (arr, idx) -> super.intoByteArray((byte[]) arr, idx));
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix, Mask<Integer, Shapes.S64Bit> m) {
        Int64Vector oldVal = SPECIES.fromByteArray(a, ix);
        Int64Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
            VectorIntrinsics.store(Int64Vector.class, int.class, LENGTH,
                                   bb.array(), ix, this,
                                   (arr, idx) -> super.intoByteArray((byte[]) arr, idx));
        } else {
            super.intoByteBuffer(bb);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, Mask<Integer, Shapes.S64Bit> m) {
        int idx = bb.position();
        Int64Vector oldVal = SPECIES.fromByteBuffer(bb, idx);
        Int64Vector newVal = oldVal.blend(this, m);
        newVal.intoByteBuffer(bb, idx);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
            VectorIntrinsics.store(Int64Vector.class, int.class, LENGTH,
                                   bb.array(), ax, this,
                                   (arr, idx) -> super.intoByteArray((byte[]) arr, idx));
        } else {
            super.intoByteBuffer(bb, ix);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Integer, Shapes.S64Bit> m) {
        Int64Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Int64Vector newVal = oldVal.blend(this, m);
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

        Int64Vector that = (Int64Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Int64Mask bTest(Vector<Integer, Shapes.S64Bit> o, FBinTest f) {
        int[] vec1 = getElements();
        int[] vec2 = ((Int64Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int64Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Int64Mask equal(Vector<Integer, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;

        return (Int64Mask) VectorIntrinsics.compare(
            BT_eq, Int64Vector.class, Int64Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Int64Mask notEqual(Vector<Integer, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;

        return (Int64Mask) VectorIntrinsics.compare(
            BT_ne, Int64Vector.class, Int64Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Int64Mask lessThan(Vector<Integer, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;

        return (Int64Mask) VectorIntrinsics.compare(
            BT_lt, Int64Vector.class, Int64Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Int64Mask lessThanEq(Vector<Integer, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;

        return (Int64Mask) VectorIntrinsics.compare(
            BT_le, Int64Vector.class, Int64Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Int64Mask greaterThan(Vector<Integer, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;

        return (Int64Mask) VectorIntrinsics.compare(
            BT_gt, Int64Vector.class, Int64Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Int64Mask greaterThanEq(Vector<Integer, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;

        return (Int64Mask) VectorIntrinsics.compare(
            BT_ge, Int64Vector.class, Int64Mask.class, int.class, LENGTH,
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
    void forEach(Mask<Integer, Shapes.S64Bit> o, FUnCon f) {
        boolean[] mbits = ((Int64Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }


    Float64Vector toFP() {
        int[] vec = getElements();
        float[] res = new float[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.intBitsToFloat(vec[i]);
        }
        return new Float64Vector(res);
    }

    @Override
    public Int64Vector rotateEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Int64Vector(res);
    }

    @Override
    public Int64Vector rotateER(int j) {
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
        return new Int64Vector(res);
    }

    @Override
    public Int64Vector shiftEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Int64Vector(res);
    }

    @Override
    public Int64Vector shiftER(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Int64Vector(res);
    }

    @Override
    public Int64Vector shuffle(Vector<Integer, Shapes.S64Bit> o, Shuffle<Integer, Shapes.S64Bit> s) {
        Int64Vector v = (Int64Vector) o;
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
    public Int64Vector swizzle(Shuffle<Integer, Shapes.S64Bit> s) {
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
    public Int64Vector blend(Vector<Integer, Shapes.S64Bit> o1, Mask<Integer, Shapes.S64Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Int64Vector v = (Int64Vector)o1;
        Int64Mask   m = (Int64Mask)o2;

        return (Int64Vector) VectorIntrinsics.blend(
            Int64Vector.class, Int64Mask.class, int.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public int get(int i) {
        int[] vec = getElements();
        return vec[i];
    }

    @Override
    public Int64Vector with(int i, int e) {
        int[] res = vec.clone();
        res[i] = e;
        return new Int64Vector(res);
    }

    // Mask

    static final class Int64Mask extends AbstractMask<Integer, Shapes.S64Bit> {
        static final Int64Mask TRUE_MASK = new Int64Mask(true);
        static final Int64Mask FALSE_MASK = new Int64Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Int64Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Int64Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
        }

        public Int64Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Int64Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Int64Mask(res);
        }

        @Override
        Int64Mask bOp(Mask<Integer, Shapes.S64Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Int64Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Int64Mask(res);
        }

        @Override
        public Int64Species species() {
            return SPECIES;
        }

        @Override
        public Int64Vector toVector() {
            int[] res = new int[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (int) (bits[i] ? -1 : 0);
            }
            return new Int64Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S64Bit> rebracket(Species<Z, Shapes.S64Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.reinterpret(
                Int64Mask.class, int.class, LENGTH,
                species.elementType(), species.length(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        @Override
        @ForceInline
        public Int64Mask not() {
            return (Int64Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Int64Mask.class, int.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Int64Mask and(Mask<Integer,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Int64Mask m = (Int64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Int64Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Int64Mask or(Mask<Integer,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Int64Mask m = (Int64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Int64Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Int64Mask.class, int.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Int64Mask.class, int.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Int64Shuffle extends AbstractShuffle<Integer, Shapes.S64Bit> {
        static final IntVector.IntSpecies<Shapes.S64Bit> INT_SPECIES = IntVector.speciesInstance(Shapes.S_64_BIT);

        public Int64Shuffle(int[] reorder) {
            super(reorder);
        }

        public Int64Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Int64Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S64Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Int64Species species() {
        return SPECIES;
    }

    static final class Int64Species extends IntSpecies<Shapes.S64Bit> {
        static final int BIT_SIZE = Shapes.S_64_BIT.bitSize();

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
        public Shapes.S64Bit shape() {
            return Shapes.S_64_BIT;
        }

        @Override
        Int64Vector op(FOp f) {
            int[] res = new int[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Int64Vector(res);
        }

        @Override
        Int64Vector op(Mask<Integer, Shapes.S64Bit> o, FOp f) {
            int[] res = new int[length()];
            boolean[] mbits = ((Int64Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Int64Vector(res);
        }

        // Factories

        @Override
        public Int64Mask maskFromValues(boolean... bits) {
            return new Int64Mask(bits);
        }

        @Override
        public Int64Mask maskFromArray(boolean[] bits, int i) {
            return new Int64Mask(bits, i);
        }

        @Override
        public Int64Shuffle shuffleFromValues(int... ixs) {
            return new Int64Shuffle(ixs);
        }

        @Override
        public Int64Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Int64Shuffle(ixs, i);
        }

        @Override
        public Int64Shuffle shuffleFromVector(Vector<Integer, Shapes.S64Bit> v) {
            int[] a = ((IntVector<Shapes.S64Bit>) v).toArray();
            return new Int64Shuffle(a, 0);
        }

        @Override
        @ForceInline
        public Int64Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Int64Vector.class, int.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Int64Vector broadcast(int e) {
            return VectorIntrinsics.broadcastCoerced(
                Int64Vector.class, int.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (int)bits)));
        }

        @Override
        @ForceInline
        public Int64Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Int64Mask.class, int.class, LENGTH,
                                                     (int)-1,
                                                     (z -> Int64Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Int64Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Int64Mask.class, int.class, LENGTH,
                                                     0,
                                                     (z -> Int64Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Int64Vector scalars(int... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return (Int64Vector) VectorIntrinsics.load(Int64Vector.class, int.class, LENGTH,
                                                        es, ix,
                                                        (arr, idx) -> super.fromArray((int[]) arr, idx));
        }

        @Override
        @ForceInline
        public Int64Vector fromArray(int[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Int64Vector) VectorIntrinsics.load(Int64Vector.class, int.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((int[]) arr, idx));
        }

        @Override
        @ForceInline
        public Int64Vector fromArray(int[] a, int ax, Mask<Integer, Shapes.S64Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }

        @Override
        @ForceInline
        public Int64Vector fromByteArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return (Int64Vector) VectorIntrinsics.load(Int64Vector.class, int.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
        }

        @Override
        @ForceInline
        public Int64Vector fromByteArray(byte[] a, int ix, Mask<Integer, Shapes.S64Bit> m) {
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Int64Vector fromByteBuffer(ByteBuffer bb) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
                return (Int64Vector) VectorIntrinsics.load(Int64Vector.class, int.class, LENGTH,
                                                            bb.array(), ix,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Int64Vector)super.fromByteBuffer(bb);
            }
        }

        @Override
        @ForceInline
        public Int64Vector fromByteBuffer(ByteBuffer bb, Mask<Integer, Shapes.S64Bit> m) {
            return zero().blend(fromByteBuffer(bb), m);
        }

        @Override
        @ForceInline
        public Int64Vector fromByteBuffer(ByteBuffer bb, int ix) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
                return (Int64Vector) VectorIntrinsics.load(Int64Vector.class, int.class, LENGTH,
                                                            bb.array(), ax,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Int64Vector)super.fromByteBuffer(bb, ix);
            }
        }

        @Override
        @ForceInline
        public Int64Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Integer, Shapes.S64Bit> m) {
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int64Vector castFromByte(ByteVector<S> o) {
            if (o.bitSize() == 64) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.cast(
                    Byte64Vector.class, byte.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.cast(
                    Byte128Vector.class, byte.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.cast(
                    Byte256Vector.class, byte.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.cast(
                    Byte512Vector.class, byte.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int64Vector castFromShort(ShortVector<S> o) {
            if (o.bitSize() == 64) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.cast(
                    Short64Vector.class, short.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.cast(
                    Short128Vector.class, short.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.cast(
                    Short256Vector.class, short.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.cast(
                    Short512Vector.class, short.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int64Vector castFromInt(IntVector<S> o) {
            if (o.bitSize() == 64) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.cast(
                    Int64Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.cast(
                    Int128Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.cast(
                    Int256Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.cast(
                    Int512Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int64Vector castFromLong(LongVector<S> o) {
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.cast(
                    Long64Vector.class, long.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.cast(
                    Long128Vector.class, long.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.cast(
                    Long256Vector.class, long.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.cast(
                    Long512Vector.class, long.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int64Vector castFromFloat(FloatVector<S> o) {
            if (o.bitSize() == 64) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.cast(
                    Float64Vector.class, float.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.cast(
                    Float128Vector.class, float.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.cast(
                    Float256Vector.class, float.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.cast(
                    Float512Vector.class, float.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Int64Vector castFromDouble(DoubleVector<S> o) {
            if (o.bitSize() == 64) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.cast(
                    Double64Vector.class, double.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.cast(
                    Double128Vector.class, double.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.cast(
                    Double256Vector.class, double.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.cast(
                    Double512Vector.class, double.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <E, S extends Shape> Int64Vector cast(Vector<E, S> o) {
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
        public <F> Int64Vector rebracket(Vector<F, Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte64Vector.class, byte.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short64Vector.class, short.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int64Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long64Vector.class, long.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float64Vector.class, float.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double64Vector.class, double.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Int64Vector resize(Vector<Integer, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int64Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int128Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int256Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int512Vector.class, int.class, so.length(),
                    int.class, LENGTH, so,
                    (v, t) -> (Int64Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
