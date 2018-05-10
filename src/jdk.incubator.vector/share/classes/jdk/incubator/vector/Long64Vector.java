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
final class Long64Vector extends LongVector<Shapes.S64Bit> {
    static final Long64Species SPECIES = new Long64Species();

    static final Long64Vector ZERO = new Long64Vector();

    static final int LENGTH = SPECIES.length();

    private final long[] vec; // Don't access directly, use getElements() instead.

    private long[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Long64Vector() {
        vec = new long[SPECIES.length()];
    }

    Long64Vector(long[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Long64Vector uOp(FUnOp f) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Long64Vector(res);
    }

    @Override
    Long64Vector uOp(Mask<Long, Shapes.S64Bit> o, FUnOp f) {
        long[] vec = getElements();
        long[] res = new long[length()];
        boolean[] mbits = ((Long64Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Long64Vector(res);
    }

    // Binary operator

    @Override
    Long64Vector bOp(Vector<Long, Shapes.S64Bit> o, FBinOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((Long64Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Long64Vector(res);
    }

    @Override
    Long64Vector bOp(Vector<Long, Shapes.S64Bit> o1, Mask<Long, Shapes.S64Bit> o2, FBinOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((Long64Vector)o1).getElements();
        boolean[] mbits = ((Long64Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Long64Vector(res);
    }

    // Trinary operator

    @Override
    Long64Vector tOp(Vector<Long, Shapes.S64Bit> o1, Vector<Long, Shapes.S64Bit> o2, FTriOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((Long64Vector)o1).getElements();
        long[] vec3 = ((Long64Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Long64Vector(res);
    }

    @Override
    Long64Vector tOp(Vector<Long, Shapes.S64Bit> o1, Vector<Long, Shapes.S64Bit> o2, Mask<Long, Shapes.S64Bit> o3, FTriOp f) {
        long[] res = new long[length()];
        long[] vec1 = getElements();
        long[] vec2 = ((Long64Vector)o1).getElements();
        long[] vec3 = ((Long64Vector)o2).getElements();
        boolean[] mbits = ((Long64Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Long64Vector(res);
    }

    @Override
    long rOp(long v, FBinOp f) {
        long[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> add(long o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> add(long o, Mask<Long,Shapes.S64Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> addSaturate(long o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> addSaturate(long o, Mask<Long,Shapes.S64Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> sub(long o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> sub(long o, Mask<Long,Shapes.S64Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> subSaturate(long o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> subSaturate(long o, Mask<Long,Shapes.S64Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> mul(long o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> mul(long o, Mask<Long,Shapes.S64Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> min(long o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> max(long o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S64Bit> equal(long o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S64Bit> notEqual(long o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S64Bit> lessThan(long o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S64Bit> lessThanEq(long o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S64Bit> greaterThan(long o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S64Bit> greaterThanEq(long o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> blend(long o, Mask<Long,Shapes.S64Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> and(long o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> and(long o, Mask<Long,Shapes.S64Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> or(long o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> or(long o, Mask<Long,Shapes.S64Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> xor(long o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S64Bit> xor(long o, Mask<Long,Shapes.S64Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Long64Vector neg() {
        return SPECIES.zero().sub(this);
    }

    // Unary operations

    @Override
    @ForceInline
    public Long64Vector abs() {
        return (Long64Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Long64Vector.class, long.class, LENGTH,
            this,
            v1 -> ((Long64Vector)v1).uOp((i, a) -> (long) Math.abs(a)));
    }


    @Override
    @ForceInline
    public Long64Vector not() {
        return (Long64Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Long64Vector.class, long.class, LENGTH,
            this,
            v1 -> ((Long64Vector)v1).uOp((i, a) -> (long) ~a));
    }
    // Binary operations

    @Override
    @ForceInline
    public Long64Vector add(Vector<Long,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2, (i, a, b) -> (long)(a + b)));
    }

    @Override
    @ForceInline
    public Long64Vector sub(Vector<Long,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2, (i, a, b) -> (long)(a - b)));
    }

    @Override
    @ForceInline
    public Long64Vector mul(Vector<Long,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2, (i, a, b) -> (long)(a * b)));
    }

    @Override
    @ForceInline
    public Long64Vector min(Vector<Long,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2, (i, a, b) -> (long) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Long64Vector max(Vector<Long,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2, (i, a, b) -> (long) ((a > b) ? a : b)));
        }



    @Override
    @ForceInline
    public Long64Vector and(Vector<Long,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2, (i, a, b) -> (long)(a & b)));
    }

    @Override
    @ForceInline
    public Long64Vector or(Vector<Long,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2, (i, a, b) -> (long)(a | b)));
    }

    @Override
    @ForceInline
    public Long64Vector xor(Vector<Long,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2, (i, a, b) -> (long)(a ^ b)));
    }

    @Override
    @ForceInline
    public Long64Vector and(Vector<Long,Shapes.S64Bit> v, Mask<Long, Shapes.S64Bit> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector or(Vector<Long,Shapes.S64Bit> v, Mask<Long, Shapes.S64Bit> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector xor(Vector<Long,Shapes.S64Bit> v, Mask<Long, Shapes.S64Bit> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector shiftL(int s) {
        return (Long64Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Long64Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a << i)));
    }

    @Override
    @ForceInline
    public Long64Vector shiftR(int s) {
        return (Long64Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Long64Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a >>> i)));
    }

    @Override
    @ForceInline
    public Long64Vector aShiftR(int s) {
        return (Long64Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Long64Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a >> i)));
    }

    @Override
    @ForceInline
    public Long64Vector shiftL(Vector<Long,Shapes.S64Bit> s) {
        Long64Vector v = (Long64Vector)s;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_LSHIFT, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2,(i,a, b) -> (long) (a << b)));
    }

    @Override
    @ForceInline
    public Long64Vector shiftR(Vector<Long,Shapes.S64Bit> s) {
        Long64Vector v = (Long64Vector)s;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_URSHIFT, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2,(i,a, b) -> (long) (a >>> b)));
    }

    @Override
    @ForceInline
    public Long64Vector ashiftR(Vector<Long,Shapes.S64Bit> s) {
        Long64Vector v = (Long64Vector)s;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_RSHIFT, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long64Vector)v1).bOp(v2,(i,a, b) -> (long) (a >> b)));
    }
    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public long addAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Long64Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a + b)));
    }

    @Override
    @ForceInline
    public long andAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Long64Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) -1, (i, a, b) -> (long) (a & b)));
    }

    @Override
    @ForceInline
    public long mulAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Long64Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 1, (i, a, b) -> (long) (a * b)));
    }

    @Override
    @ForceInline
    public long subAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_SUB, Long64Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a - b)));
    }

    @Override
    @ForceInline
    public long orAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Long64Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a | b)));
    }

    @Override
    @ForceInline
    public long xorAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Long64Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a ^ b)));
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(long[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Long64Vector.class, long.class, LENGTH,
                               a, ix, this,
                               (arr, idx) -> super.intoArray((long[]) arr, idx));
    }

    @Override
    @ForceInline
    public void intoArray(long[] a, int ax, Mask<Long, Shapes.S64Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Long64Vector oldVal = SPECIES.fromArray(a, ax);
        Long64Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Long64Vector.class, long.class, LENGTH,
                               a, ix, this,
                               (arr, idx) -> super.intoByteArray((byte[]) arr, idx));
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix, Mask<Long, Shapes.S64Bit> m) {
        Long64Vector oldVal = SPECIES.fromByteArray(a, ix);
        Long64Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
            VectorIntrinsics.store(Long64Vector.class, long.class, LENGTH,
                                   bb.array(), ix, this,
                                   (arr, idx) -> super.intoByteArray((byte[]) arr, idx));
        } else {
            super.intoByteBuffer(bb);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, Mask<Long, Shapes.S64Bit> m) {
        int idx = bb.position();
        Long64Vector oldVal = SPECIES.fromByteBuffer(bb, idx);
        Long64Vector newVal = oldVal.blend(this, m);
        newVal.intoByteBuffer(bb, idx);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
            VectorIntrinsics.store(Long64Vector.class, long.class, LENGTH,
                                   bb.array(), ax, this,
                                   (arr, idx) -> super.intoByteArray((byte[]) arr, idx));
        } else {
            super.intoByteBuffer(bb, ix);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Long, Shapes.S64Bit> m) {
        Long64Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Long64Vector newVal = oldVal.blend(this, m);
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

        Long64Vector that = (Long64Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Long64Mask bTest(Vector<Long, Shapes.S64Bit> o, FBinTest f) {
        long[] vec1 = getElements();
        long[] vec2 = ((Long64Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Long64Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Long64Mask equal(Vector<Long, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return (Long64Mask) VectorIntrinsics.compare(
            BT_eq, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Long64Mask notEqual(Vector<Long, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return (Long64Mask) VectorIntrinsics.compare(
            BT_ne, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Long64Mask lessThan(Vector<Long, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return (Long64Mask) VectorIntrinsics.compare(
            BT_lt, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Long64Mask lessThanEq(Vector<Long, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return (Long64Mask) VectorIntrinsics.compare(
            BT_le, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Long64Mask greaterThan(Vector<Long, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return (Long64Mask) VectorIntrinsics.compare(
            BT_gt, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Long64Mask greaterThanEq(Vector<Long, Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return (Long64Mask) VectorIntrinsics.compare(
            BT_ge, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a >= b));
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        long[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Long, Shapes.S64Bit> o, FUnCon f) {
        boolean[] mbits = ((Long64Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }


    Double64Vector toFP() {
        long[] vec = getElements();
        double[] res = new double[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Double.longBitsToDouble(vec[i]);
        }
        return new Double64Vector(res);
    }

    @Override
    public Long64Vector rotateEL(int j) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Long64Vector(res);
    }

    @Override
    public Long64Vector rotateER(int j) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Long64Vector(res);
    }

    @Override
    public Long64Vector shiftEL(int j) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Long64Vector(res);
    }

    @Override
    public Long64Vector shiftER(int j) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Long64Vector(res);
    }

    @Override
    public Long64Vector shuffle(Vector<Long, Shapes.S64Bit> o, Shuffle<Long, Shapes.S64Bit> s) {
        Long64Vector v = (Long64Vector) o;
        return uOp((i, a) -> {
            long[] vec = this.getElements();
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
    public Long64Vector swizzle(Shuffle<Long, Shapes.S64Bit> s) {
        return uOp((i, a) -> {
            long[] vec = this.getElements();
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
    public Long64Vector blend(Vector<Long, Shapes.S64Bit> o1, Mask<Long, Shapes.S64Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Long64Vector v = (Long64Vector)o1;
        Long64Mask   m = (Long64Mask)o2;

        return (Long64Vector) VectorIntrinsics.blend(
            Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public long get(int i) {
        long[] vec = getElements();
        return vec[i];
    }

    @Override
    public Long64Vector with(int i, long e) {
        long[] res = vec.clone();
        res[i] = e;
        return new Long64Vector(res);
    }

    // Mask

    static final class Long64Mask extends AbstractMask<Long, Shapes.S64Bit> {
        static final Long64Mask TRUE_MASK = new Long64Mask(true);
        static final Long64Mask FALSE_MASK = new Long64Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Long64Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Long64Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
        }

        public Long64Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Long64Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Long64Mask(res);
        }

        @Override
        Long64Mask bOp(Mask<Long, Shapes.S64Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Long64Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Long64Mask(res);
        }

        @Override
        public Long64Species species() {
            return SPECIES;
        }

        @Override
        public Long64Vector toVector() {
            long[] res = new long[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (long) (bits[i] ? -1 : 0);
            }
            return new Long64Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S64Bit> rebracket(Species<Z, Shapes.S64Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.reinterpret(
                Long64Mask.class, long.class, LENGTH,
                species.elementType(), species.length(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        @Override
        @ForceInline
        public Long64Mask not() {
            return (Long64Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Long64Mask.class, long.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Long64Mask and(Mask<Long,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Long64Mask m = (Long64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Long64Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Long64Mask or(Mask<Long,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Long64Mask m = (Long64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Long64Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Long64Mask.class, long.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Long64Mask.class, long.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Long64Shuffle extends AbstractShuffle<Long, Shapes.S64Bit> {
        static final IntVector.IntSpecies<Shapes.S64Bit> INT_SPECIES = IntVector.speciesInstance(Shapes.S_64_BIT);

        public Long64Shuffle(int[] reorder) {
            super(reorder);
        }

        public Long64Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Long64Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S64Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Long64Species species() {
        return SPECIES;
    }

    static final class Long64Species extends LongSpecies<Shapes.S64Bit> {
        static final int BIT_SIZE = Shapes.S_64_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Long.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(long.class.getSimpleName()).append("s x ");
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
        public Class<Long> elementType() {
            return long.class;
        }

        @Override
        @ForceInline
        public int elementSize() {
            return Long.SIZE;
        }

        @Override
        @ForceInline
        public Shapes.S64Bit shape() {
            return Shapes.S_64_BIT;
        }

        @Override
        Long64Vector op(FOp f) {
            long[] res = new long[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Long64Vector(res);
        }

        @Override
        Long64Vector op(Mask<Long, Shapes.S64Bit> o, FOp f) {
            long[] res = new long[length()];
            boolean[] mbits = ((Long64Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Long64Vector(res);
        }

        // Factories

        @Override
        public Long64Mask maskFromValues(boolean... bits) {
            return new Long64Mask(bits);
        }

        @Override
        public Long64Mask maskFromArray(boolean[] bits, int i) {
            return new Long64Mask(bits, i);
        }

        @Override
        public Long64Shuffle shuffleFromValues(int... ixs) {
            return new Long64Shuffle(ixs);
        }

        @Override
        public Long64Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Long64Shuffle(ixs, i);
        }

        @Override
        public Long64Shuffle shuffleFromVector(Vector<Integer, Shapes.S64Bit> v) {
            int[] a = ((IntVector<Shapes.S64Bit>) v).toArray();
            return new Long64Shuffle(a, 0);
        }

        @Override
        @ForceInline
        public Long64Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Long64Vector.class, long.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Long64Vector broadcast(long e) {
            return VectorIntrinsics.broadcastCoerced(
                Long64Vector.class, long.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (long)bits)));
        }

        @Override
        @ForceInline
        public Long64Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Long64Mask.class, long.class, LENGTH,
                                                     (long)-1,
                                                     (z -> Long64Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Long64Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Long64Mask.class, long.class, LENGTH,
                                                     0,
                                                     (z -> Long64Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Long64Vector scalars(long... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return (Long64Vector) VectorIntrinsics.load(Long64Vector.class, long.class, LENGTH,
                                                        es, ix,
                                                        (arr, idx) -> super.fromArray((long[]) arr, idx));
        }

        @Override
        @ForceInline
        public Long64Vector fromArray(long[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Long64Vector) VectorIntrinsics.load(Long64Vector.class, long.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((long[]) arr, idx));
        }

        @Override
        @ForceInline
        public Long64Vector fromArray(long[] a, int ax, Mask<Long, Shapes.S64Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }

        @Override
        @ForceInline
        public Long64Vector fromByteArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return (Long64Vector) VectorIntrinsics.load(Long64Vector.class, long.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
        }

        @Override
        @ForceInline
        public Long64Vector fromByteArray(byte[] a, int ix, Mask<Long, Shapes.S64Bit> m) {
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Long64Vector fromByteBuffer(ByteBuffer bb) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
                return (Long64Vector) VectorIntrinsics.load(Long64Vector.class, long.class, LENGTH,
                                                            bb.array(), ix,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Long64Vector)super.fromByteBuffer(bb);
            }
        }

        @Override
        @ForceInline
        public Long64Vector fromByteBuffer(ByteBuffer bb, Mask<Long, Shapes.S64Bit> m) {
            return zero().blend(fromByteBuffer(bb), m);
        }

        @Override
        @ForceInline
        public Long64Vector fromByteBuffer(ByteBuffer bb, int ix) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
                return (Long64Vector) VectorIntrinsics.load(Long64Vector.class, long.class, LENGTH,
                                                            bb.array(), ax,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Long64Vector)super.fromByteBuffer(bb, ix);
            }
        }

        @Override
        @ForceInline
        public Long64Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Long, Shapes.S64Bit> m) {
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long64Vector castFromByte(ByteVector<S> o) {
            if (o.bitSize() == 64) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.cast(
                    Byte64Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.cast(
                    Byte128Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.cast(
                    Byte256Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.cast(
                    Byte512Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long64Vector castFromShort(ShortVector<S> o) {
            if (o.bitSize() == 64) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.cast(
                    Short64Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.cast(
                    Short128Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.cast(
                    Short256Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.cast(
                    Short512Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long64Vector castFromInt(IntVector<S> o) {
            if (o.bitSize() == 64) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.cast(
                    Int64Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.cast(
                    Int128Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.cast(
                    Int256Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.cast(
                    Int512Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long64Vector castFromLong(LongVector<S> o) {
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.cast(
                    Long64Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.cast(
                    Long128Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.cast(
                    Long256Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.cast(
                    Long512Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long64Vector castFromFloat(FloatVector<S> o) {
            if (o.bitSize() == 64) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.cast(
                    Float64Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.cast(
                    Float128Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.cast(
                    Float256Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.cast(
                    Float512Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long64Vector castFromDouble(DoubleVector<S> o) {
            if (o.bitSize() == 64) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.cast(
                    Double64Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.cast(
                    Double128Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.cast(
                    Double256Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.cast(
                    Double512Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <E, S extends Shape> Long64Vector cast(Vector<E, S> o) {
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
        public <F> Long64Vector rebracket(Vector<F, Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte64Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short64Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int64Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long64Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float64Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double64Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Long64Vector resize(Vector<Long, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long64Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long128Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long256Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long512Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long64Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
