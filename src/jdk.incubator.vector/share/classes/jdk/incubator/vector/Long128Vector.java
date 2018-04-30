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
final class Long128Vector extends LongVector<Shapes.S128Bit> {
    static final Long128Species SPECIES = new Long128Species();

    static final Long128Vector ZERO = new Long128Vector();

    static final int LENGTH = SPECIES.length();

    private final long[] vec; // Don't access directly, use getElements() instead.

    private long[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Long128Vector() {
        vec = new long[SPECIES.length()];
    }

    Long128Vector(long[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Long128Vector uOp(FUnOp f) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Long128Vector(res);
    }

    @Override
    Long128Vector uOp(Mask<Long, Shapes.S128Bit> o, FUnOp f) {
        long[] vec = getElements();
        long[] res = new long[length()];
        boolean[] mbits = ((Long128Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Long128Vector(res);
    }

    // Binary operator

    @Override
    Long128Vector bOp(Vector<Long, Shapes.S128Bit> o, FBinOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((Long128Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Long128Vector(res);
    }

    @Override
    Long128Vector bOp(Vector<Long, Shapes.S128Bit> o1, Mask<Long, Shapes.S128Bit> o2, FBinOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((Long128Vector)o1).getElements();
        boolean[] mbits = ((Long128Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Long128Vector(res);
    }

    // Trinary operator

    @Override
    Long128Vector tOp(Vector<Long, Shapes.S128Bit> o1, Vector<Long, Shapes.S128Bit> o2, FTriOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((Long128Vector)o1).getElements();
        long[] vec3 = ((Long128Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Long128Vector(res);
    }

    @Override
    Long128Vector tOp(Vector<Long, Shapes.S128Bit> o1, Vector<Long, Shapes.S128Bit> o2, Mask<Long, Shapes.S128Bit> o3, FTriOp f) {
        long[] res = new long[length()];
        long[] vec1 = getElements();
        long[] vec2 = ((Long128Vector)o1).getElements();
        long[] vec3 = ((Long128Vector)o2).getElements();
        boolean[] mbits = ((Long128Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Long128Vector(res);
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
    public LongVector<Shapes.S128Bit> add(long o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> add(long o, Mask<Long,Shapes.S128Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> addSaturate(long o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> addSaturate(long o, Mask<Long,Shapes.S128Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> sub(long o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> sub(long o, Mask<Long,Shapes.S128Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> subSaturate(long o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> subSaturate(long o, Mask<Long,Shapes.S128Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> mul(long o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> mul(long o, Mask<Long,Shapes.S128Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> min(long o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> max(long o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S128Bit> equal(long o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S128Bit> notEqual(long o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S128Bit> lessThan(long o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S128Bit> lessThanEq(long o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S128Bit> greaterThan(long o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S128Bit> greaterThanEq(long o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> blend(long o, Mask<Long,Shapes.S128Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> and(long o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> and(long o, Mask<Long,Shapes.S128Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> or(long o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> or(long o, Mask<Long,Shapes.S128Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> xor(long o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S128Bit> xor(long o, Mask<Long,Shapes.S128Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }


    // Unary operations

    @Override
    @ForceInline
    public Long128Vector abs() {
        return (Long128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Long128Vector.class, long.class, LENGTH,
            this,
            v1 -> ((Long128Vector)v1).uOp((i, a) -> (long) Math.abs(a)));
    }



    @Override
    @ForceInline
    public Long128Vector not() {
        return (Long128Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Long128Vector.class, long.class, LENGTH,
            this,
            v1 -> ((Long128Vector)v1).uOp((i, a) -> (long) ~a));
    }
    // Binary operations

    @Override
    @ForceInline
    public Long128Vector add(Vector<Long,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2, (i, a, b) -> (long)(a + b)));
    }

    @Override
    @ForceInline
    public Long128Vector sub(Vector<Long,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2, (i, a, b) -> (long)(a - b)));
    }

    @Override
    @ForceInline
    public Long128Vector mul(Vector<Long,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2, (i, a, b) -> (long)(a * b)));
    }

    @Override
    @ForceInline
    public Long128Vector min(Vector<Long,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2, (i, a, b) -> (long) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Long128Vector max(Vector<Long,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2, (i, a, b) -> (long) ((a > b) ? a : b)));
        }



    @Override
    @ForceInline
    public Long128Vector and(Vector<Long,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2, (i, a, b) -> (long)(a & b)));
    }

    @Override
    @ForceInline
    public Long128Vector or(Vector<Long,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2, (i, a, b) -> (long)(a | b)));
    }

    @Override
    @ForceInline
    public Long128Vector xor(Vector<Long,Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2, (i, a, b) -> (long)(a ^ b)));
    }

    @Override
    @ForceInline
    public Long128Vector and(Vector<Long,Shapes.S128Bit> v, Mask<Long, Shapes.S128Bit> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Long128Vector or(Vector<Long,Shapes.S128Bit> v, Mask<Long, Shapes.S128Bit> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Long128Vector xor(Vector<Long,Shapes.S128Bit> v, Mask<Long, Shapes.S128Bit> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Long128Vector shiftL(int s) {
        return (Long128Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Long128Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a << i)));
    }

    @Override
    @ForceInline
    public Long128Vector shiftR(int s) {
        return (Long128Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Long128Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a >>> i)));
    }

    @Override
    @ForceInline
    public Long128Vector aShiftR(int s) {
        return (Long128Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Long128Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a >> i)));
    }

    @Override
    @ForceInline
    public Long128Vector shiftL(Vector<Long,Shapes.S128Bit> s) {
        Long128Vector v = (Long128Vector)s;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_LSHIFT, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2,(i,a, b) -> (long) (a << b)));
    }

    @Override
    @ForceInline
    public Long128Vector shiftR(Vector<Long,Shapes.S128Bit> s) {
        Long128Vector v = (Long128Vector)s;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_URSHIFT, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2,(i,a, b) -> (long) (a >>> b)));
    }

    @Override
    @ForceInline
    public Long128Vector ashiftR(Vector<Long,Shapes.S128Bit> s) {
        Long128Vector v = (Long128Vector)s;
        return (Long128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_RSHIFT, Long128Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long128Vector)v1).bOp(v2,(i,a, b) -> (long) (a >> b)));
    }
    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public long addAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Long128Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a + b)));
    }

    @Override
    @ForceInline
    public long andAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Long128Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) -1, (i, a, b) -> (long) (a & b)));
    }

    @Override
    @ForceInline
    public long subAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_SUB, Long128Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a - b)));
    }

    @Override
    @ForceInline
    public long mulAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Long128Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 1, (i, a, b) -> (long) (a * b)));
    }

    @Override
    @ForceInline
    public long orAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Long128Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a | b)));
    }

    @Override
    @ForceInline
    public long xorAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Long128Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a ^ b)));
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(long[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Long128Vector.class, long.class, LENGTH,
                               a, ix, this,
                               (arr, idx) -> super.intoArray((long[]) arr, idx));
    }

    @Override
    @ForceInline
    public void intoArray(long[] a, int ax, Mask<Long, Shapes.S128Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Long128Vector oldVal = SPECIES.fromArray(a, ax);
        Long128Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Long128Vector.class, long.class, LENGTH,
                               a, ix, this,
                               (arr, idx) -> super.intoByteArray((byte[]) arr, idx));
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix, Mask<Long, Shapes.S128Bit> m) {
        Long128Vector oldVal = SPECIES.fromByteArray(a, ix);
        Long128Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
            VectorIntrinsics.store(Long128Vector.class, long.class, LENGTH,
                                   bb.array(), ix, this,
                                   (arr, idx) -> super.intoByteArray((byte[]) arr, idx));
        } else {
            super.intoByteBuffer(bb);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, Mask<Long, Shapes.S128Bit> m) {
        int idx = bb.position();
        Long128Vector oldVal = SPECIES.fromByteBuffer(bb, idx);
        Long128Vector newVal = oldVal.blend(this, m);
        newVal.intoByteBuffer(bb, idx);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
            int num_bytes = bitSize() / Byte.SIZE;
            int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
            VectorIntrinsics.store(Long128Vector.class, long.class, LENGTH,
                                   bb.array(), ax, this,
                                   (arr, idx) -> super.intoByteArray((byte[]) arr, idx));
        } else {
            super.intoByteBuffer(bb, ix);
        }
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Long, Shapes.S128Bit> m) {
        Long128Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Long128Vector newVal = oldVal.blend(this, m);
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

        Long128Vector that = (Long128Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Long128Mask bTest(Vector<Long, Shapes.S128Bit> o, FBinTest f) {
        long[] vec1 = getElements();
        long[] vec2 = ((Long128Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Long128Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Long128Mask equal(Vector<Long, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;

        return (Long128Mask) VectorIntrinsics.compare(
            BT_eq, Long128Vector.class, Long128Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Long128Mask notEqual(Vector<Long, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;

        return (Long128Mask) VectorIntrinsics.compare(
            BT_ne, Long128Vector.class, Long128Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Long128Mask lessThan(Vector<Long, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;

        return (Long128Mask) VectorIntrinsics.compare(
            BT_lt, Long128Vector.class, Long128Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Long128Mask lessThanEq(Vector<Long, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;

        return (Long128Mask) VectorIntrinsics.compare(
            BT_le, Long128Vector.class, Long128Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Long128Mask greaterThan(Vector<Long, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;

        return (Long128Mask) VectorIntrinsics.compare(
            BT_gt, Long128Vector.class, Long128Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Long128Mask greaterThanEq(Vector<Long, Shapes.S128Bit> o) {
        Objects.requireNonNull(o);
        Long128Vector v = (Long128Vector)o;

        return (Long128Mask) VectorIntrinsics.compare(
            BT_ge, Long128Vector.class, Long128Mask.class, long.class, LENGTH,
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
    void forEach(Mask<Long, Shapes.S128Bit> o, FUnCon f) {
        boolean[] mbits = ((Long128Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }


    Double128Vector toFP() {
        long[] vec = getElements();
        double[] res = new double[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Double.longBitsToDouble(vec[i]);
        }
        return new Double128Vector(res);
    }

    @Override
    public Long128Vector rotateEL(int j) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Long128Vector(res);
    }

    @Override
    public Long128Vector rotateER(int j) {
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
        return new Long128Vector(res);
    }

    @Override
    public Long128Vector shiftEL(int j) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Long128Vector(res);
    }

    @Override
    public Long128Vector shiftER(int j) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Long128Vector(res);
    }

    @Override
    public Long128Vector shuffle(Vector<Long, Shapes.S128Bit> o, Shuffle<Long, Shapes.S128Bit> s) {
        Long128Vector v = (Long128Vector) o;
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
    public Long128Vector swizzle(Shuffle<Long, Shapes.S128Bit> s) {
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
    public Long128Vector blend(Vector<Long, Shapes.S128Bit> o1, Mask<Long, Shapes.S128Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Long128Vector v = (Long128Vector)o1;
        Long128Mask   m = (Long128Mask)o2;

        return (Long128Vector) VectorIntrinsics.blend(
            Long128Vector.class, Long128Mask.class, long.class, LENGTH,
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
    public Long128Vector with(int i, long e) {
        long[] res = vec.clone();
        res[i] = e;
        return new Long128Vector(res);
    }

    // Mask

    static final class Long128Mask extends AbstractMask<Long, Shapes.S128Bit> {
        static final Long128Mask TRUE_MASK = new Long128Mask(true);
        static final Long128Mask FALSE_MASK = new Long128Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Long128Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Long128Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
        }

        public Long128Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Long128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Long128Mask(res);
        }

        @Override
        Long128Mask bOp(Mask<Long, Shapes.S128Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Long128Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Long128Mask(res);
        }

        @Override
        public Long128Species species() {
            return SPECIES;
        }

        @Override
        public Long128Vector toVector() {
            long[] res = new long[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (long) (bits[i] ? -1 : 0);
            }
            return new Long128Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S128Bit> rebracket(Species<Z, Shapes.S128Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.reinterpret(
                Long128Mask.class, long.class, LENGTH,
                species.elementType(), species.length(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        @Override
        @ForceInline
        public Long128Mask not() {
            return (Long128Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Long128Mask.class, long.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Long128Mask and(Mask<Long,Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            Long128Mask m = (Long128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Long128Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Long128Mask or(Mask<Long,Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            Long128Mask m = (Long128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Long128Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Long128Mask.class, long.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Long128Mask.class, long.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Long128Shuffle extends AbstractShuffle<Long, Shapes.S128Bit> {
        static final IntVector.IntSpecies<Shapes.S128Bit> INT_SPECIES = IntVector.speciesInstance(Shapes.S_128_BIT);

        public Long128Shuffle(int[] reorder) {
            super(reorder);
        }

        public Long128Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Long128Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S128Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Long128Species species() {
        return SPECIES;
    }

    static final class Long128Species extends LongSpecies<Shapes.S128Bit> {
        static final int BIT_SIZE = Shapes.S_128_BIT.bitSize();

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
        public Shapes.S128Bit shape() {
            return Shapes.S_128_BIT;
        }

        @Override
        Long128Vector op(FOp f) {
            long[] res = new long[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Long128Vector(res);
        }

        @Override
        Long128Vector op(Mask<Long, Shapes.S128Bit> o, FOp f) {
            long[] res = new long[length()];
            boolean[] mbits = ((Long128Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Long128Vector(res);
        }

        // Factories

        @Override
        public Long128Mask maskFromValues(boolean... bits) {
            return new Long128Mask(bits);
        }

        @Override
        public Long128Mask maskFromArray(boolean[] bits, int i) {
            return new Long128Mask(bits, i);
        }

        @Override
        public Long128Shuffle shuffleFromValues(int... ixs) {
            return new Long128Shuffle(ixs);
        }

        @Override
        public Long128Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Long128Shuffle(ixs, i);
        }

        @Override
        public Long128Shuffle shuffleFromVector(Vector<Integer, Shapes.S128Bit> v) {
            int[] a = ((IntVector<Shapes.S128Bit>) v).toArray();
            return new Long128Shuffle(a, 0);
        }

        @Override
        @ForceInline
        public Long128Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Long128Vector.class, long.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Long128Vector broadcast(long e) {
            return VectorIntrinsics.broadcastCoerced(
                Long128Vector.class, long.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (long)bits)));
        }

        @Override
        @ForceInline
        public Long128Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Long128Mask.class, long.class, LENGTH,
                                                     (long)-1,
                                                     (z -> Long128Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Long128Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Long128Mask.class, long.class, LENGTH,
                                                     0,
                                                     (z -> Long128Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Long128Vector scalars(long... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return (Long128Vector) VectorIntrinsics.load(Long128Vector.class, long.class, LENGTH,
                                                        es, ix,
                                                        (arr, idx) -> super.fromArray((long[]) arr, idx));
        }

        @Override
        @ForceInline
        public Long128Vector fromArray(long[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Long128Vector) VectorIntrinsics.load(Long128Vector.class, long.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((long[]) arr, idx));
        }

        @Override
        @ForceInline
        public Long128Vector fromArray(long[] a, int ax, Mask<Long, Shapes.S128Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }

        @Override
        @ForceInline
        public Long128Vector fromByteArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return (Long128Vector) VectorIntrinsics.load(Long128Vector.class, long.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
        }

        @Override
        @ForceInline
        public Long128Vector fromByteArray(byte[] a, int ix, Mask<Long, Shapes.S128Bit> m) {
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Long128Vector fromByteBuffer(ByteBuffer bb) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ix = VectorIntrinsics.checkIndex(bb.position(), bb.limit(), num_bytes);
                return (Long128Vector) VectorIntrinsics.load(Long128Vector.class, long.class, LENGTH,
                                                            bb.array(), ix,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Long128Vector)super.fromByteBuffer(bb);
            }
        }

        @Override
        @ForceInline
        public Long128Vector fromByteBuffer(ByteBuffer bb, Mask<Long, Shapes.S128Bit> m) {
            return zero().blend(fromByteBuffer(bb), m);
        }

        @Override
        @ForceInline
        public Long128Vector fromByteBuffer(ByteBuffer bb, int ix) {
            if (bb.hasArray() && !bb.isReadOnly() && bb.order() == ByteOrder.nativeOrder()) {
                int num_bytes = bitSize() / Byte.SIZE;
                int ax = VectorIntrinsics.checkIndex(ix, bb.limit(), num_bytes);
                return (Long128Vector) VectorIntrinsics.load(Long128Vector.class, long.class, LENGTH,
                                                            bb.array(), ax,
                                                            (arr, idx) -> super.fromByteArray((byte[]) arr, idx));
            } else {
                return (Long128Vector)super.fromByteBuffer(bb, ix);
            }
        }

        @Override
        @ForceInline
        public Long128Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Long, Shapes.S128Bit> m) {
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long128Vector castFromByte(ByteVector<S> o) {
            if (o.bitSize() == 64) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.cast(
                    Byte64Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.cast(
                    Byte128Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.cast(
                    Byte256Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.cast(
                    Byte512Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long128Vector castFromShort(ShortVector<S> o) {
            if (o.bitSize() == 64) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.cast(
                    Short64Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.cast(
                    Short128Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.cast(
                    Short256Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.cast(
                    Short512Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long128Vector castFromInt(IntVector<S> o) {
            if (o.bitSize() == 64) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.cast(
                    Int64Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.cast(
                    Int128Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.cast(
                    Int256Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.cast(
                    Int512Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long128Vector castFromLong(LongVector<S> o) {
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.cast(
                    Long64Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.cast(
                    Long128Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.cast(
                    Long256Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.cast(
                    Long512Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long128Vector castFromFloat(FloatVector<S> o) {
            if (o.bitSize() == 64) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.cast(
                    Float64Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.cast(
                    Float128Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.cast(
                    Float256Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.cast(
                    Float512Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Long128Vector castFromDouble(DoubleVector<S> o) {
            if (o.bitSize() == 64) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.cast(
                    Double64Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.cast(
                    Double128Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.cast(
                    Double256Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.cast(
                    Double512Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <E, S extends Shape> Long128Vector cast(Vector<E, S> o) {
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
        public <F> Long128Vector rebracket(Vector<F, Shapes.S128Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte128Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short128Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int128Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long128Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float128Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double128Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Long128Vector resize(Vector<Long, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long64Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long128Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long256Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long512Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long128Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
