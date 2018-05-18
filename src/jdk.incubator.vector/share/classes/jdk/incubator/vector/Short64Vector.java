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
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Short64Vector extends ShortVector<Shapes.S64Bit> {
    static final Short64Species SPECIES = new Short64Species();

    static final Short64Vector ZERO = new Short64Vector();

    static final int LENGTH = SPECIES.length();

    private final short[] vec; // Don't access directly, use getElements() instead.

    private short[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Short64Vector() {
        vec = new short[SPECIES.length()];
    }

    Short64Vector(short[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Short64Vector uOp(FUnOp f) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Short64Vector(res);
    }

    @Override
    Short64Vector uOp(Mask<Short, Shapes.S64Bit> o, FUnOp f) {
        short[] vec = getElements();
        short[] res = new short[length()];
        boolean[] mbits = ((Short64Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Short64Vector(res);
    }

    // Binary operator

    @Override
    Short64Vector bOp(Vector<Short, Shapes.S64Bit> o, FBinOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short64Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Short64Vector(res);
    }

    @Override
    Short64Vector bOp(Vector<Short, Shapes.S64Bit> o1, Mask<Short, Shapes.S64Bit> o2, FBinOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short64Vector)o1).getElements();
        boolean[] mbits = ((Short64Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Short64Vector(res);
    }

    // Trinary operator

    @Override
    Short64Vector tOp(Vector<Short, Shapes.S64Bit> o1, Vector<Short, Shapes.S64Bit> o2, FTriOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short64Vector)o1).getElements();
        short[] vec3 = ((Short64Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Short64Vector(res);
    }

    @Override
    Short64Vector tOp(Vector<Short, Shapes.S64Bit> o1, Vector<Short, Shapes.S64Bit> o2, Mask<Short, Shapes.S64Bit> o3, FTriOp f) {
        short[] res = new short[length()];
        short[] vec1 = getElements();
        short[] vec2 = ((Short64Vector)o1).getElements();
        short[] vec3 = ((Short64Vector)o2).getElements();
        boolean[] mbits = ((Short64Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Short64Vector(res);
    }

    @Override
    short rOp(short v, FBinOp f) {
        short[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> add(short o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> add(short o, Mask<Short,Shapes.S64Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> addSaturate(short o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> addSaturate(short o, Mask<Short,Shapes.S64Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> sub(short o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> sub(short o, Mask<Short,Shapes.S64Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> subSaturate(short o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> subSaturate(short o, Mask<Short,Shapes.S64Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> mul(short o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> mul(short o, Mask<Short,Shapes.S64Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> min(short o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> max(short o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S64Bit> equal(short o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S64Bit> notEqual(short o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S64Bit> lessThan(short o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S64Bit> lessThanEq(short o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S64Bit> greaterThan(short o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S64Bit> greaterThanEq(short o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> blend(short o, Mask<Short,Shapes.S64Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> and(short o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> and(short o, Mask<Short,Shapes.S64Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> or(short o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> or(short o, Mask<Short,Shapes.S64Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> xor(short o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S64Bit> xor(short o, Mask<Short,Shapes.S64Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Short64Vector neg() {
        return SPECIES.zero().sub(this);
    }

    // Unary operations

    @Override
    @ForceInline
    public Short64Vector abs() {
        return (Short64Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Short64Vector.class, short.class, LENGTH,
            this,
            v1 -> ((Short64Vector)v1).uOp((i, a) -> (short) Math.abs(a)));
    }


    @Override
    @ForceInline
    public Short64Vector not() {
        return (Short64Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Short64Vector.class, short.class, LENGTH,
            this,
            v1 -> ((Short64Vector)v1).uOp((i, a) -> (short) ~a));
    }
    // Binary operations

    @Override
    @ForceInline
    public Short64Vector add(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a + b)));
    }

    @Override
    @ForceInline
    public Short64Vector sub(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a - b)));
    }

    @Override
    @ForceInline
    public Short64Vector mul(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a * b)));
    }

    @Override
    @ForceInline
    public Short64Vector min(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Short64Vector max(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short) ((a > b) ? a : b)));
        }



    @Override
    @ForceInline
    public Short64Vector and(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a & b)));
    }

    @Override
    @ForceInline
    public Short64Vector or(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a | b)));
    }

    @Override
    @ForceInline
    public Short64Vector xor(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a ^ b)));
    }

    @Override
    @ForceInline
    public Short64Vector and(Vector<Short,Shapes.S64Bit> v, Mask<Short, Shapes.S64Bit> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Short64Vector or(Vector<Short,Shapes.S64Bit> v, Mask<Short, Shapes.S64Bit> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Short64Vector xor(Vector<Short,Shapes.S64Bit> v, Mask<Short, Shapes.S64Bit> m) {
        return blend(xor(v), m);
    }

    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public short addAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Short64Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a + b)));
    }

    @Override
    @ForceInline
    public short andAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Short64Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) -1, (i, a, b) -> (short) (a & b)));
    }

    @Override
    @ForceInline
    public short subAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_SUB, Short64Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a - b)));
    }

    @Override
    @ForceInline
    public short orAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Short64Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a | b)));
    }

    @Override
    @ForceInline
    public short xorAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Short64Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a ^ b)));
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(short[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Short64Vector.class, short.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((short[])arr)[idx + i] = a_));
    }

    @Override
    @ForceInline
    public void intoArray(short[] a, int ax, Mask<Short, Shapes.S64Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Short64Vector oldVal = SPECIES.fromArray(a, ax);
        Short64Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Short64Vector.class, short.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> {
                                   byte[] tarr = (byte[])arr;
                                   ByteBuffer bb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                   ShortBuffer fb = bb.asShortBuffer();
                                   v.forEach((i, e) -> fb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix, Mask<Short, Shapes.S64Bit> m) {
        Short64Vector oldVal = SPECIES.fromByteArray(a, ix);
        Short64Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
            VectorIntrinsics.store(Short64Vector.class, short.class, LENGTH,
                                   bb.array(), ix, this,
                                   (arr, idx, v) -> {
                                       byte[] tarr = (byte[])arr;
                                       ByteBuffer lbb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                       ShortBuffer fb = lbb.asShortBuffer();
                                       v.forEach((i, e) -> fb.put(e));
                                   });
        } else {
            super.intoByteBuffer(bb);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, Mask<Short, Shapes.S64Bit> m) {
        int idx = bb.position();
        Short64Vector oldVal = SPECIES.fromByteBuffer(bb, idx);
        Short64Vector newVal = oldVal.blend(this, m);
        newVal.intoByteBuffer(bb, idx);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
            VectorIntrinsics.store(Short64Vector.class, short.class, LENGTH,
                                   bb.array(), ax, this,
                                   (arr, idx, v) -> {
                                       byte[] tarr = (byte[])arr;
                                       ByteBuffer lbb = ByteBuffer.wrap(tarr, idx, tarr.length - idx).order(ByteOrder.nativeOrder());
                                       ShortBuffer fb = lbb.asShortBuffer();
                                       v.forEach((i, e) -> fb.put(e));
                                   });
        } else {
            super.intoByteBuffer(bb, ix);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Short, Shapes.S64Bit> m) {
        Short64Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Short64Vector newVal = oldVal.blend(this, m);
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

        Short64Vector that = (Short64Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Short64Mask bTest(Vector<Short, Shapes.S64Bit> o, FBinTest f) {
        short[] vec1 = getElements();
        short[] vec2 = ((Short64Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Short64Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Short64Mask equal(Vector<Short, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;

        return (Short64Mask) VectorIntrinsics.compare(
            BT_eq, Short64Vector.class, Short64Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Short64Mask notEqual(Vector<Short, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;

        return (Short64Mask) VectorIntrinsics.compare(
            BT_ne, Short64Vector.class, Short64Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Short64Mask lessThan(Vector<Short, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;

        return (Short64Mask) VectorIntrinsics.compare(
            BT_lt, Short64Vector.class, Short64Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Short64Mask lessThanEq(Vector<Short, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;

        return (Short64Mask) VectorIntrinsics.compare(
            BT_le, Short64Vector.class, Short64Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Short64Mask greaterThan(Vector<Short, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;

        return (Short64Mask) VectorIntrinsics.compare(
            BT_gt, Short64Vector.class, Short64Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Short64Mask greaterThanEq(Vector<Short, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;

        return (Short64Mask) VectorIntrinsics.compare(
            BT_ge, Short64Vector.class, Short64Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a >= b));
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        short[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Short, Shapes.S64Bit> o, FUnCon f) {
        boolean[] mbits = ((Short64Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Short64Vector rotateEL(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector rotateER(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector shiftEL(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector shiftER(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector shuffle(Vector<Short, Shapes.S64Bit> o, Shuffle<Short, Shapes.S64Bit> s) {
        Short64Vector v = (Short64Vector) o;
        return uOp((i, a) -> {
            short[] vec = this.getElements();
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
    public Short64Vector swizzle(Shuffle<Short, Shapes.S64Bit> s) {
        return uOp((i, a) -> {
            short[] vec = this.getElements();
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
    public Short64Vector blend(Vector<Short, Shapes.S64Bit> o1, Mask<Short, Shapes.S64Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Short64Vector v = (Short64Vector)o1;
        Short64Mask   m = (Short64Mask)o2;

        return (Short64Vector) VectorIntrinsics.blend(
            Short64Vector.class, Short64Mask.class, short.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public short get(int i) {
        short[] vec = getElements();
        return vec[i];
    }

    @Override
    public Short64Vector with(int i, short e) {
        short[] res = vec.clone();
        res[i] = e;
        return new Short64Vector(res);
    }

    // Mask

    static final class Short64Mask extends AbstractMask<Short, Shapes.S64Bit> {
        static final Short64Mask TRUE_MASK = new Short64Mask(true);
        static final Short64Mask FALSE_MASK = new Short64Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Short64Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Short64Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
        }

        public Short64Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Short64Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Short64Mask(res);
        }

        @Override
        Short64Mask bOp(Mask<Short, Shapes.S64Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Short64Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Short64Mask(res);
        }

        @Override
        public Short64Species species() {
            return SPECIES;
        }

        @Override
        public Short64Vector toVector() {
            short[] res = new short[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (short) (bits[i] ? -1 : 0);
            }
            return new Short64Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S64Bit> rebracket(Species<Z, Shapes.S64Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.reinterpret(
                Short64Mask.class, short.class, LENGTH,
                species.elementType(), species.length(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        @Override
        @ForceInline
        public Short64Mask not() {
            return (Short64Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Short64Mask.class, short.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Short64Mask and(Mask<Short,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Short64Mask m = (Short64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Short64Mask.class, short.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Short64Mask or(Mask<Short,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Short64Mask m = (Short64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Short64Mask.class, short.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Short64Mask.class, short.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Short64Mask.class, short.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Short64Shuffle extends AbstractShuffle<Short, Shapes.S64Bit> {
        static final IntVector.IntSpecies<Shapes.S64Bit> INT_SPECIES = IntVector.species(Shapes.S_64_BIT);

        public Short64Shuffle(int[] reorder) {
            super(reorder);
        }

        public Short64Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Short64Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S64Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Short64Species species() {
        return SPECIES;
    }

    static final class Short64Species extends ShortSpecies<Shapes.S64Bit> {
        static final int BIT_SIZE = Shapes.S_64_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Short.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(short.class.getSimpleName()).append("s x ");
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
        public Class<Short> elementType() {
            return short.class;
        }

        @Override
        @ForceInline
        public int elementSize() {
            return Short.SIZE;
        }

        @Override
        @ForceInline
        public Shapes.S64Bit shape() {
            return Shapes.S_64_BIT;
        }

        @Override
        Short64Vector op(FOp f) {
            short[] res = new short[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Short64Vector(res);
        }

        @Override
        Short64Vector op(Mask<Short, Shapes.S64Bit> o, FOp f) {
            short[] res = new short[length()];
            boolean[] mbits = ((Short64Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Short64Vector(res);
        }

        // Factories

        @Override
        public Short64Mask maskFromValues(boolean... bits) {
            return new Short64Mask(bits);
        }

        @Override
        public Short64Mask maskFromArray(boolean[] bits, int i) {
            return new Short64Mask(bits, i);
        }

        @Override
        public Short64Shuffle shuffleFromValues(int... ixs) {
            return new Short64Shuffle(ixs);
        }

        @Override
        public Short64Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Short64Shuffle(ixs, i);
        }

        @Override
        public Short64Shuffle shuffleFromVector(Vector<Integer, Shapes.S64Bit> v) {
            int[] a = ((IntVector<Shapes.S64Bit>) v).toArray();
            return new Short64Shuffle(a, 0);
        }

        @Override
        @ForceInline
        public Short64Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Short64Vector.class, short.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Short64Vector broadcast(short e) {
            return VectorIntrinsics.broadcastCoerced(
                Short64Vector.class, short.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (short)bits)));
        }

        @Override
        @ForceInline
        public Short64Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Short64Mask.class, short.class, LENGTH,
                                                     (short)-1,
                                                     (z -> Short64Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Short64Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Short64Mask.class, short.class, LENGTH,
                                                     0,
                                                     (z -> Short64Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Short64Vector scalars(short... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return (Short64Vector) VectorIntrinsics.load(Short64Vector.class, short.class, LENGTH,
                                                        es, ix,
                                                        (arr, idx) -> super.fromArray((short[]) arr, idx));
        }

        @Override
        @ForceInline
        public Short64Vector fromArray(short[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Short64Vector) VectorIntrinsics.load(Short64Vector.class, short.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((short[]) arr, idx));
        }

        @Override
        @ForceInline
        public Short64Vector fromArray(short[] a, int ax, Mask<Short, Shapes.S64Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }

        @Override
        @ForceInline
        public Short64Vector fromByteArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return (Short64Vector) VectorIntrinsics.load(Short64Vector.class, short.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
        }

        @Override
        @ForceInline
        public Short64Vector fromByteArray(byte[] a, int ix, Mask<Short, Shapes.S64Bit> m) {
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Short64Vector fromByteBuffer(ByteBuffer bb) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
                return (Short64Vector) VectorIntrinsics.load(Short64Vector.class, short.class, LENGTH,
                                                            bb.array(), ix,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Short64Vector)super.fromByteBuffer(bb);
            }
        }

        @Override
        @ForceInline
        public Short64Vector fromByteBuffer(ByteBuffer bb, Mask<Short, Shapes.S64Bit> m) {
            return zero().blend(fromByteBuffer(bb), m);
        }

        @Override
        @ForceInline
        public Short64Vector fromByteBuffer(ByteBuffer bb, int ix) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
                return (Short64Vector) VectorIntrinsics.load(Short64Vector.class, short.class, LENGTH,
                                                            bb.array(), ax,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Short64Vector)super.fromByteBuffer(bb, ix);
            }
        }

        @Override
        @ForceInline
        public Short64Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Short, Shapes.S64Bit> m) {
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short64Vector castFromByte(ByteVector<S> o) {
            if (o.bitSize() == 64) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.cast(
                    Byte64Vector.class, byte.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.cast(
                    Byte128Vector.class, byte.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.cast(
                    Byte256Vector.class, byte.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.cast(
                    Byte512Vector.class, byte.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short64Vector castFromShort(ShortVector<S> o) {
            if (o.bitSize() == 64) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.cast(
                    Short64Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.cast(
                    Short128Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.cast(
                    Short256Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.cast(
                    Short512Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short64Vector castFromInt(IntVector<S> o) {
            if (o.bitSize() == 64) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.cast(
                    Int64Vector.class, int.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.cast(
                    Int128Vector.class, int.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.cast(
                    Int256Vector.class, int.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.cast(
                    Int512Vector.class, int.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short64Vector castFromLong(LongVector<S> o) {
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.cast(
                    Long64Vector.class, long.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.cast(
                    Long128Vector.class, long.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.cast(
                    Long256Vector.class, long.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.cast(
                    Long512Vector.class, long.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short64Vector castFromFloat(FloatVector<S> o) {
            if (o.bitSize() == 64) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.cast(
                    Float64Vector.class, float.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.cast(
                    Float128Vector.class, float.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.cast(
                    Float256Vector.class, float.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.cast(
                    Float512Vector.class, float.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short64Vector castFromDouble(DoubleVector<S> o) {
            if (o.bitSize() == 64) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.cast(
                    Double64Vector.class, double.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.cast(
                    Double128Vector.class, double.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.cast(
                    Double256Vector.class, double.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.cast(
                    Double512Vector.class, double.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <E, S extends Shape> Short64Vector cast(Vector<E, S> o) {
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
        public <F> Short64Vector rebracket(Vector<F, Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte64Vector.class, byte.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short64Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int64Vector.class, int.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long64Vector.class, long.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float64Vector.class, float.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double64Vector.class, double.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Short64Vector resize(Vector<Short, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short64Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short128Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short256Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short512Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short64Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
