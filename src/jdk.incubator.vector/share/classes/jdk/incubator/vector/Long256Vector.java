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
import java.nio.LongBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Long256Vector extends LongVector<Shapes.S256Bit> {
    static final Long256Species SPECIES = new Long256Species();

    static final Long256Vector ZERO = new Long256Vector();

    static final int LENGTH = SPECIES.length();

    private final long[] vec; // Don't access directly, use getElements() instead.

    private long[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Long256Vector() {
        vec = new long[SPECIES.length()];
    }

    Long256Vector(long[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Long256Vector uOp(FUnOp f) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Long256Vector(res);
    }

    @Override
    Long256Vector uOp(Mask<Long, Shapes.S256Bit> o, FUnOp f) {
        long[] vec = getElements();
        long[] res = new long[length()];
        boolean[] mbits = ((Long256Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Long256Vector(res);
    }

    // Binary operator

    @Override
    Long256Vector bOp(Vector<Long, Shapes.S256Bit> o, FBinOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((Long256Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Long256Vector(res);
    }

    @Override
    Long256Vector bOp(Vector<Long, Shapes.S256Bit> o1, Mask<Long, Shapes.S256Bit> o2, FBinOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((Long256Vector)o1).getElements();
        boolean[] mbits = ((Long256Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Long256Vector(res);
    }

    // Trinary operator

    @Override
    Long256Vector tOp(Vector<Long, Shapes.S256Bit> o1, Vector<Long, Shapes.S256Bit> o2, FTriOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((Long256Vector)o1).getElements();
        long[] vec3 = ((Long256Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Long256Vector(res);
    }

    @Override
    Long256Vector tOp(Vector<Long, Shapes.S256Bit> o1, Vector<Long, Shapes.S256Bit> o2, Mask<Long, Shapes.S256Bit> o3, FTriOp f) {
        long[] res = new long[length()];
        long[] vec1 = getElements();
        long[] vec2 = ((Long256Vector)o1).getElements();
        long[] vec3 = ((Long256Vector)o2).getElements();
        boolean[] mbits = ((Long256Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Long256Vector(res);
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
    public LongVector<Shapes.S256Bit> add(long o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> add(long o, Mask<Long,Shapes.S256Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> sub(long o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> sub(long o, Mask<Long,Shapes.S256Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> mul(long o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> mul(long o, Mask<Long,Shapes.S256Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> min(long o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> max(long o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S256Bit> equal(long o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S256Bit> notEqual(long o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S256Bit> lessThan(long o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S256Bit> lessThanEq(long o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S256Bit> greaterThan(long o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long, Shapes.S256Bit> greaterThanEq(long o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> blend(long o, Mask<Long,Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> and(long o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> and(long o, Mask<Long,Shapes.S256Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> or(long o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> or(long o, Mask<Long,Shapes.S256Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> xor(long o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> xor(long o, Mask<Long,Shapes.S256Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Long256Vector neg() {
        return SPECIES.zero().sub(this);
    }

    // Unary operations

    @ForceInline
    @Override
    public Long256Vector neg(Mask<Long, Shapes.S256Bit> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public Long256Vector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Long256Vector.class, long.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (long) Math.abs(a)));
    }

    @ForceInline
    @Override
    public Long256Vector abs(Mask<Long, Shapes.S256Bit> m) {
        return blend(abs(), m);
    }


    @Override
    @ForceInline
    public Long256Vector not() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Long256Vector.class, long.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (long) ~a));
    }

    @ForceInline
    @Override
    public Long256Vector not(Mask<Long, Shapes.S256Bit> m) {
        return blend(not(), m);
    }
    // Binary operations

    @Override
    @ForceInline
    public Long256Vector add(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a + b)));
    }

    @Override
    @ForceInline
    public Long256Vector add(Vector<Long,Shapes.S256Bit> v, Mask<Long, Shapes.S256Bit> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Long256Vector sub(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a - b)));
    }

    @Override
    @ForceInline
    public Long256Vector sub(Vector<Long,Shapes.S256Bit> v, Mask<Long, Shapes.S256Bit> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Long256Vector mul(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a * b)));
    }

    @Override
    @ForceInline
    public Long256Vector mul(Vector<Long,Shapes.S256Bit> v, Mask<Long, Shapes.S256Bit> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Long256Vector min(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return (Long256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long256Vector)v1).bOp(v2, (i, a, b) -> (long) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Long256Vector min(Vector<Long,Shapes.S256Bit> v, Mask<Long, Shapes.S256Bit> m) {
        return blend(min(v), m);
    }

    @Override
    @ForceInline
    public Long256Vector max(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long) ((a > b) ? a : b)));
        }

    @Override
    @ForceInline
    public Long256Vector max(Vector<Long,Shapes.S256Bit> v, Mask<Long, Shapes.S256Bit> m) {
        return blend(max(v), m);
    }

    @Override
    @ForceInline
    public Long256Vector and(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a & b)));
    }

    @Override
    @ForceInline
    public Long256Vector or(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a | b)));
    }

    @Override
    @ForceInline
    public Long256Vector xor(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a ^ b)));
    }

    @Override
    @ForceInline
    public Long256Vector and(Vector<Long,Shapes.S256Bit> v, Mask<Long, Shapes.S256Bit> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Long256Vector or(Vector<Long,Shapes.S256Bit> v, Mask<Long, Shapes.S256Bit> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Long256Vector xor(Vector<Long,Shapes.S256Bit> v, Mask<Long, Shapes.S256Bit> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Long256Vector shiftL(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Long256Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a << i)));
    }

    @Override
    @ForceInline
    public Long256Vector shiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Long256Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a >>> i)));
    }

    @Override
    @ForceInline
    public Long256Vector aShiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Long256Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a >> i)));
    }

    @Override
    @ForceInline
    public Long256Vector shiftL(Vector<Long,Shapes.S256Bit> s) {
        Long256Vector shiftv = (Long256Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(species().broadcast(0x3f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_LSHIFT, Long256Vector.class, long.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (long) (a << b)));
    }

    @Override
    @ForceInline
    public Long256Vector shiftR(Vector<Long,Shapes.S256Bit> s) {
        Long256Vector shiftv = (Long256Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(species().broadcast(0x3f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_URSHIFT, Long256Vector.class, long.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (long) (a >>> b)));
    }

    @Override
    @ForceInline
    public Long256Vector aShiftR(Vector<Long,Shapes.S256Bit> s) {
        Long256Vector shiftv = (Long256Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(species().broadcast(0x3f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_RSHIFT, Long256Vector.class, long.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (long) (a >> b)));
    }
    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public long addAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a + b)));
    }

    @Override
    @ForceInline
    public long andAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) -1, (i, a, b) -> (long) (a & b)));
    }

    @Override
    @ForceInline
    public long andAll(Mask<Long, Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast((long) -1), m).andAll();
    }

    @Override
    @ForceInline
    public long minAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MIN, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp(Long.MAX_VALUE , (i, a, b) -> (long) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public long maxAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MAX, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp(Long.MIN_VALUE , (i, a, b) -> (long) ((a > b) ? a : b)));
    }

    @Override
    @ForceInline
    public long mulAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 1, (i, a, b) -> (long) (a * b)));
    }

    @Override
    @ForceInline
    public long subAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_SUB, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a - b)));
    }

    @Override
    @ForceInline
    public long orAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a | b)));
    }

    @Override
    @ForceInline
    public long orAll(Mask<Long, Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast((long) 0), m).orAll();
    }

    @Override
    @ForceInline
    public long xorAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a ^ b)));
    }

    @Override
    @ForceInline
    public long xorAll(Mask<Long, Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast((long) 0), m).xorAll();
    }


    @Override
    @ForceInline
    public long addAll(Mask<Long, Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast((long) 0), m).addAll();
    }

    @Override
    @ForceInline
    public long subAll(Mask<Long, Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast((long) 0), m).subAll();
    }

    @Override
    @ForceInline
    public long mulAll(Mask<Long, Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast((long) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public long minAll(Mask<Long, Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast(Long.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public long maxAll(Mask<Long, Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast(Long.MIN_VALUE), m).maxAll();
    }

    @Override
    @ForceInline
    public Shuffle<Long, Shapes.S256Bit> toShuffle() {
        long[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return SPECIES.shuffleFromArray(sa, 0);
    }

    // Memory operations

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_LONG_INDEX_SCALE);

    @Override
    @ForceInline
    public void intoArray(long[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Long256Vector.class, long.class, LENGTH,
                               a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_LONG_BASE_OFFSET,
                               this,
                               a, ix,
                               (arr, idx, v) -> v.forEach((i, e) -> arr[idx + i] = e));
    }

    @Override
    @ForceInline
    public final void intoArray(long[] a, int ax, Mask<Long, Shapes.S256Bit> m) {
        Long256Vector oldVal = SPECIES.fromArray(a, ax);
        Long256Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Long256Vector.class, long.class, LENGTH,
                               a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                               this,
                               a, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                   LongBuffer tb = bbc.asLongBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public final void intoByteArray(byte[] a, int ix, Mask<Long, Shapes.S256Bit> m) {
        Long256Vector oldVal = SPECIES.fromByteArray(a, ix);
        Long256Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        if (bb.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Long256Vector.class, long.class, LENGTH,
                               U.getReference(bb, BYTE_BUFFER_HB), ix + U.getLong(bb, BUFFER_ADDRESS),
                               this,
                               bb, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                   LongBuffer tb = bbc.asLongBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Long, Shapes.S256Bit> m) {
        Long256Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Long256Vector newVal = oldVal.blend(this, m);
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

        Long256Vector that = (Long256Vector) o;
        return this.equal(that).allTrue();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Long256Mask bTest(Vector<Long, Shapes.S256Bit> o, FBinTest f) {
        long[] vec1 = getElements();
        long[] vec2 = ((Long256Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Long256Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Long256Mask equal(Vector<Long, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;

        return VectorIntrinsics.compare(
            BT_eq, Long256Vector.class, Long256Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Long256Mask notEqual(Vector<Long, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;

        return VectorIntrinsics.compare(
            BT_ne, Long256Vector.class, Long256Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Long256Mask lessThan(Vector<Long, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;

        return VectorIntrinsics.compare(
            BT_lt, Long256Vector.class, Long256Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Long256Mask lessThanEq(Vector<Long, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;

        return VectorIntrinsics.compare(
            BT_le, Long256Vector.class, Long256Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Long256Mask greaterThan(Vector<Long, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;

        return (Long256Mask) VectorIntrinsics.compare(
            BT_gt, Long256Vector.class, Long256Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Long256Mask greaterThanEq(Vector<Long, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;

        return VectorIntrinsics.compare(
            BT_ge, Long256Vector.class, Long256Mask.class, long.class, LENGTH,
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
    void forEach(Mask<Long, Shapes.S256Bit> o, FUnCon f) {
        boolean[] mbits = ((Long256Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }


    Double256Vector toFP() {
        long[] vec = getElements();
        double[] res = new double[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Double.longBitsToDouble(vec[i]);
        }
        return new Double256Vector(res);
    }

    @Override
    public Long256Vector rotateEL(int j) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Long256Vector(res);
    }

    @Override
    public Long256Vector rotateER(int j) {
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
        return new Long256Vector(res);
    }

    @Override
    public Long256Vector shiftEL(int j) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Long256Vector(res);
    }

    @Override
    public Long256Vector shiftER(int j) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Long256Vector(res);
    }

    @Override
    @ForceInline
    public Long256Vector rearrange(Vector<Long, Shapes.S256Bit> v,
                                  Shuffle<Long, Shapes.S256Bit> s, Mask<Long, Shapes.S256Bit> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    @ForceInline
    public Long256Vector rearrange(Shuffle<Long, Shapes.S256Bit> o1) {
    Objects.requireNonNull(o1);
    Long256Shuffle s =  (Long256Shuffle)o1;

        return VectorIntrinsics.rearrangeOp(
            Long256Vector.class, Long256Shuffle.class, long.class, LENGTH,
            this, s,
            (v1, s_) -> v1.uOp((i, a) -> {
            long[] vec = this.getElements();
            int ei = s_.getElement(i);
            return vec[ei];
        }));
    }

    @Override
    @ForceInline
    public Long256Vector blend(Vector<Long, Shapes.S256Bit> o1, Mask<Long, Shapes.S256Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Long256Vector v = (Long256Vector)o1;
        Long256Mask   m = (Long256Mask)o2;

        return VectorIntrinsics.blend(
            Long256Vector.class, Long256Mask.class, long.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public long get(int i) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return (long) VectorIntrinsics.extract(
                                Long256Vector.class, long.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    long[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Long256Vector with(int i, long e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Long256Vector.class, long.class, LENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    long[] res = v.getElements().clone();
                                    res[ix] = (long)bits;
                                    return new Long256Vector(res);
                                });
    }

    // Mask

    static final class Long256Mask extends AbstractMask<Long, Shapes.S256Bit> {
        static final Long256Mask TRUE_MASK = new Long256Mask(true);
        static final Long256Mask FALSE_MASK = new Long256Mask(false);

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Long256Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Long256Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Long256Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Long256Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Long256Mask(res);
        }

        @Override
        Long256Mask bOp(Mask<Long, Shapes.S256Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Long256Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Long256Mask(res);
        }

        @Override
        public Long256Species species() {
            return SPECIES;
        }

        @Override
        public Long256Vector toVector() {
            long[] res = new long[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (long) (bits[i] ? -1 : 0);
            }
            return new Long256Vector(res);
        }

        // Unary operations

        @Override
        @ForceInline
        public Long256Mask not() {
            return (Long256Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Long256Mask.class, long.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Long256Mask and(Mask<Long,Shapes.S256Bit> o) {
            Objects.requireNonNull(o);
            Long256Mask m = (Long256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Long256Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Long256Mask or(Mask<Long,Shapes.S256Bit> o) {
            Objects.requireNonNull(o);
            Long256Mask m = (Long256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Long256Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Long256Mask.class, long.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Long256Mask.class, long.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Long256Shuffle extends AbstractShuffle<Long, Shapes.S256Bit> {
        Long256Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Long256Shuffle(int[] reorder) {
            super(reorder);
        }

        public Long256Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Long256Shuffle(IntUnaryOperator f) {
            super(f);
        }

        @Override
        public Long256Species species() {
            return SPECIES;
        }

        @Override
        public Long256Vector toVector() {
            long[] va = new long[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (long) getElement(i);
            }
            return species().fromArray(va, 0);
        }

        @Override
        public Long256Shuffle rearrange(Vector.Shuffle<Long, Shapes.S256Bit> o) {
            Long256Shuffle s = (Long256Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Long256Shuffle(r);
        }
    }

    // Species

    @Override
    public Long256Species species() {
        return SPECIES;
    }

    static final class Long256Species extends LongSpecies<Shapes.S256Bit> {
        static final int BIT_SIZE = Shapes.S_256_BIT.bitSize();

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
        public Shapes.S256Bit shape() {
            return Shapes.S_256_BIT;
        }

        @Override
        Long256Vector op(FOp f) {
            long[] res = new long[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Long256Vector(res);
        }

        @Override
        Long256Vector op(Mask<Long, Shapes.S256Bit> o, FOp f) {
            long[] res = new long[length()];
            boolean[] mbits = ((Long256Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Long256Vector(res);
        }

        // Factories

        @Override
        public Long256Mask maskFromValues(boolean... bits) {
            return new Long256Mask(bits);
        }

        @Override
        public Long256Mask maskFromArray(boolean[] bits, int i) {
            return new Long256Mask(bits, i);
        }

        @Override
        public Long256Shuffle shuffle(IntUnaryOperator f) {
            return new Long256Shuffle(f);
        }

        @Override
        public Long256Shuffle shuffleIota() {
            return new Long256Shuffle(AbstractShuffle.IDENTITY);
        }

        @Override
        public Long256Shuffle shuffleFromValues(int... ixs) {
            return new Long256Shuffle(ixs);
        }

        @Override
        public Long256Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Long256Shuffle(ixs, i);
        }

        @Override
        @ForceInline
        public Long256Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Long256Vector.class, long.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Long256Vector broadcast(long e) {
            return VectorIntrinsics.broadcastCoerced(
                Long256Vector.class, long.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (long)bits)));
        }

        @Override
        @ForceInline
        public Long256Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Long256Mask.class, long.class, LENGTH,
                                                     (long)-1,
                                                     (z -> Long256Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Long256Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Long256Mask.class, long.class, LENGTH,
                                                     0,
                                                     (z -> Long256Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Long256Vector scalars(long... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return VectorIntrinsics.load(Long256Vector.class, long.class, LENGTH,
                                         es, Unsafe.ARRAY_LONG_BASE_OFFSET,
                                         es, ix,
                                         (c, idx) -> op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Long256Vector fromArray(long[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return VectorIntrinsics.load(Long256Vector.class, long.class, LENGTH,
                                         a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_LONG_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Long256Vector fromArray(long[] a, int ax, Mask<Long, Shapes.S256Bit> m) {
            return zero().blend(fromArray(a, ax), m);
        }

        @Override
        @ForceInline
        public Long256Vector fromByteArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Long256Vector.class, long.class, LENGTH,
                                         a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
                                             LongBuffer tb = bbc.asLongBuffer();
                                             return op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Long256Vector fromByteArray(byte[] a, int ix, Mask<Long, Shapes.S256Bit> m) {
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Long256Vector fromByteBuffer(ByteBuffer bb, int ix) {
            if (bb.order() != ByteOrder.nativeOrder()) {
                throw new IllegalArgumentException();
            }
            ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Long256Vector.class, long.class, LENGTH,
                                         U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + ix,
                                         bb, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                             LongBuffer tb = bbc.asLongBuffer();
                                             return op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Long256Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Long, Shapes.S256Bit> m) {
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> Long256Vector cast(Vector<F, T> o) {
            if (o.length() != LENGTH)
                throw new IllegalArgumentException("Vector length this species length differ");

            return VectorIntrinsics.cast(
                o.getClass(),
                o.elementType(), LENGTH,
                Long256Vector.class,
                long.class, LENGTH,
                o, this,
                (s, v) -> s.castDefault(v)
            );
        }

        @SuppressWarnings("unchecked")
        @ForceInline
        private <F, T extends Shape> Long256Vector castDefault(Vector<F, T> v) {
            // Allocate array of required size
            int limit = length();
            long[] a = new long[limit];

            Class<?> vtype = v.species().elementType();
            if (vtype == byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else if (vtype == short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else if (vtype == int.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else if (vtype == long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else if (vtype == float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else if (vtype == double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
        }

        @Override
        @ForceInline
        public <E, S extends Shape> Long256Mask cast(Mask<E, S> m) {
            if (m.length() != LENGTH)
                throw new IllegalArgumentException("Mask length this species length differ");
            return new Long256Mask(m.toArray());
        }

        @Override
        @ForceInline
        public <E, S extends Shape> Long256Shuffle cast(Shuffle<E, S> s) {
            if (s.length() != LENGTH)
                throw new IllegalArgumentException("Shuffle length this species length differ");
            return new Long256Shuffle(s.toArray());
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F> Long256Vector rebracket(Vector<F, Shapes.S256Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte256Vector.class,
                    byte.class, so.length(),
                    Long256Vector.class,
                    long.class, LENGTH,
                    so, this,
                    (s, v) -> (Long256Vector) s.reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short256Vector.class,
                    short.class, so.length(),
                    Long256Vector.class,
                    long.class, LENGTH,
                    so, this,
                    (s, v) -> (Long256Vector) s.reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int256Vector.class,
                    int.class, so.length(),
                    Long256Vector.class,
                    long.class, LENGTH,
                    so, this,
                    (s, v) -> (Long256Vector) s.reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long256Vector.class,
                    long.class, so.length(),
                    Long256Vector.class,
                    long.class, LENGTH,
                    so, this,
                    (s, v) -> (Long256Vector) s.reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float256Vector.class,
                    float.class, so.length(),
                    Long256Vector.class,
                    long.class, LENGTH,
                    so, this,
                    (s, v) -> (Long256Vector) s.reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double256Vector.class,
                    double.class, so.length(),
                    Long256Vector.class,
                    long.class, LENGTH,
                    so, this,
                    (s, v) -> (Long256Vector) s.reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Long256Vector resize(Vector<Long, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long64Vector.class,
                    long.class, so.length(),
                    Long256Vector.class,
                    long.class, LENGTH,
                    so, this,
                    (s, v) -> (Long256Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long128Vector.class,
                    long.class, so.length(),
                    Long256Vector.class,
                    long.class, LENGTH,
                    so, this,
                    (s, v) -> (Long256Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long256Vector.class,
                    long.class, so.length(),
                    Long256Vector.class,
                    long.class, LENGTH,
                    so, this,
                    (s, v) -> (Long256Vector) s.reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long512Vector.class,
                    long.class, so.length(),
                    Long256Vector.class,
                    long.class, LENGTH,
                    so, this,
                    (s, v) -> (Long256Vector) s.reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
