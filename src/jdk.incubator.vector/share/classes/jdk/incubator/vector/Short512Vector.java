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
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Short512Vector extends ShortVector<Shapes.S512Bit> {
    static final Short512Species SPECIES = new Short512Species();

    static final Short512Vector ZERO = new Short512Vector();

    static final int LENGTH = SPECIES.length();

    private final short[] vec; // Don't access directly, use getElements() instead.

    private short[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Short512Vector() {
        vec = new short[SPECIES.length()];
    }

    Short512Vector(short[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Short512Vector uOp(FUnOp f) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector uOp(Mask<Short, Shapes.S512Bit> o, FUnOp f) {
        short[] vec = getElements();
        short[] res = new short[length()];
        boolean[] mbits = ((Short512Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Short512Vector(res);
    }

    // Binary operator

    @Override
    Short512Vector bOp(Vector<Short, Shapes.S512Bit> o, FBinOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short512Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector bOp(Vector<Short, Shapes.S512Bit> o1, Mask<Short, Shapes.S512Bit> o2, FBinOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short512Vector)o1).getElements();
        boolean[] mbits = ((Short512Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Short512Vector(res);
    }

    // Trinary operator

    @Override
    Short512Vector tOp(Vector<Short, Shapes.S512Bit> o1, Vector<Short, Shapes.S512Bit> o2, FTriOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short512Vector)o1).getElements();
        short[] vec3 = ((Short512Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector tOp(Vector<Short, Shapes.S512Bit> o1, Vector<Short, Shapes.S512Bit> o2, Mask<Short, Shapes.S512Bit> o3, FTriOp f) {
        short[] res = new short[length()];
        short[] vec1 = getElements();
        short[] vec2 = ((Short512Vector)o1).getElements();
        short[] vec3 = ((Short512Vector)o2).getElements();
        boolean[] mbits = ((Short512Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Short512Vector(res);
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
    public ShortVector<Shapes.S512Bit> add(short o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> add(short o, Mask<Short,Shapes.S512Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> sub(short o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> sub(short o, Mask<Short,Shapes.S512Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> mul(short o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> mul(short o, Mask<Short,Shapes.S512Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> min(short o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> max(short o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> equal(short o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> notEqual(short o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> lessThan(short o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> lessThanEq(short o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> greaterThan(short o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> greaterThanEq(short o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> blend(short o, Mask<Short,Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> and(short o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> and(short o, Mask<Short,Shapes.S512Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> or(short o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> or(short o, Mask<Short,Shapes.S512Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> xor(short o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> xor(short o, Mask<Short,Shapes.S512Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Short512Vector neg() {
        return SPECIES.zero().sub(this);
    }

    // Unary operations

    @ForceInline
    @Override
    public Short512Vector neg(Mask<Short, Shapes.S512Bit> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public Short512Vector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Short512Vector.class, short.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (short) Math.abs(a)));
    }

    @ForceInline
    @Override
    public Short512Vector abs(Mask<Short, Shapes.S512Bit> m) {
        return blend(abs(), m);
    }


    @Override
    @ForceInline
    public Short512Vector not() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Short512Vector.class, short.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (short) ~a));
    }

    @ForceInline
    @Override
    public Short512Vector not(Mask<Short, Shapes.S512Bit> m) {
        return blend(not(), m);
    }
    // Binary operations

    @Override
    @ForceInline
    public Short512Vector add(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a + b)));
    }

    @Override
    @ForceInline
    public Short512Vector add(Vector<Short,Shapes.S512Bit> v, Mask<Short, Shapes.S512Bit> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector sub(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a - b)));
    }

    @Override
    @ForceInline
    public Short512Vector sub(Vector<Short,Shapes.S512Bit> v, Mask<Short, Shapes.S512Bit> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector mul(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a * b)));
    }

    @Override
    @ForceInline
    public Short512Vector mul(Vector<Short,Shapes.S512Bit> v, Mask<Short, Shapes.S512Bit> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector min(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return (Short512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short512Vector)v1).bOp(v2, (i, a, b) -> (short) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Short512Vector max(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short) ((a > b) ? a : b)));
        }

    @Override
    @ForceInline
    public Short512Vector and(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a & b)));
    }

    @Override
    @ForceInline
    public Short512Vector or(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a | b)));
    }

    @Override
    @ForceInline
    public Short512Vector xor(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a ^ b)));
    }

    @Override
    @ForceInline
    public Short512Vector and(Vector<Short,Shapes.S512Bit> v, Mask<Short, Shapes.S512Bit> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector or(Vector<Short,Shapes.S512Bit> v, Mask<Short, Shapes.S512Bit> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector xor(Vector<Short,Shapes.S512Bit> v, Mask<Short, Shapes.S512Bit> m) {
        return blend(xor(v), m);
    }

    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public short addAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a + b)));
    }

    @Override
    @ForceInline
    public short andAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) -1, (i, a, b) -> (short) (a & b)));
    }

    @Override
    @ForceInline
    public short andAll(Mask<Short, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((short) -1), m).andAll();
    }

    @Override
    @ForceInline
    public short minAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MIN, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp(Short.MAX_VALUE , (i, a, b) -> (short) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public short maxAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MAX, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp(Short.MIN_VALUE , (i, a, b) -> (short) ((a > b) ? a : b)));
    }

    @Override
    @ForceInline
    public short mulAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 1, (i, a, b) -> (short) (a * b)));
    }

    @Override
    @ForceInline
    public short subAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_SUB, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a - b)));
    }

    @Override
    @ForceInline
    public short orAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a | b)));
    }

    @Override
    @ForceInline
    public short orAll(Mask<Short, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((short) 0), m).orAll();
    }

    @Override
    @ForceInline
    public short xorAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a ^ b)));
    }

    @Override
    @ForceInline
    public short xorAll(Mask<Short, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((short) 0), m).xorAll();
    }


    @Override
    @ForceInline
    public short addAll(Mask<Short, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((short) 0), m).addAll();
    }

    @Override
    @ForceInline
    public short subAll(Mask<Short, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((short) 0), m).subAll();
    }

    @Override
    @ForceInline
    public short mulAll(Mask<Short, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast((short) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public short minAll(Mask<Short, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(Short.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public short maxAll(Mask<Short, Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(Short.MIN_VALUE), m).maxAll();
    }

    @Override
    @ForceInline
    public Shuffle<Short, Shapes.S512Bit> toShuffle() {
        short[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return SPECIES.shuffleFromArray(sa, 0);
    }

    // Memory operations

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_SHORT_INDEX_SCALE);

    @Override
    @ForceInline
    public void intoArray(short[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Short512Vector.class, short.class, LENGTH,
                               a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_SHORT_BASE_OFFSET,
                               this,
                               a, ix,
                               (arr, idx, v) -> v.forEach((i, e) -> arr[idx + i] = e));
    }

    @Override
    @ForceInline
    public final void intoArray(short[] a, int ax, Mask<Short, Shapes.S512Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Short512Vector oldVal = SPECIES.fromArray(a, ax);
        Short512Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        // @@@ Endianess
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Short512Vector.class, short.class, LENGTH,
                               a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                               this,
                               a, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                   ShortBuffer tb = bbc.asShortBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public final void intoByteArray(byte[] a, int ix, Mask<Short, Shapes.S512Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Short512Vector oldVal = SPECIES.fromByteArray(a, ix);
        Short512Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        // @@@ Endianess
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        if (bb.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Short512Vector.class, short.class, LENGTH,
                               U.getObject(bb, BYTE_BUFFER_HB), ix + U.getLong(bb, BUFFER_ADDRESS),
                               this,
                               bb, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                   ShortBuffer tb = bbc.asShortBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Short, Shapes.S512Bit> m) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        Short512Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Short512Vector newVal = oldVal.blend(this, m);
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

        // @@@ Use equal op
        Short512Vector that = (Short512Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Short512Mask bTest(Vector<Short, Shapes.S512Bit> o, FBinTest f) {
        short[] vec1 = getElements();
        short[] vec2 = ((Short512Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Short512Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Short512Mask equal(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return VectorIntrinsics.compare(
            BT_eq, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Short512Mask notEqual(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return VectorIntrinsics.compare(
            BT_ne, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Short512Mask lessThan(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return VectorIntrinsics.compare(
            BT_lt, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Short512Mask lessThanEq(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return VectorIntrinsics.compare(
            BT_le, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Short512Mask greaterThan(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return (Short512Mask) VectorIntrinsics.compare(
            BT_gt, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Short512Mask greaterThanEq(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return VectorIntrinsics.compare(
            BT_ge, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
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
    void forEach(Mask<Short, Shapes.S512Bit> o, FUnCon f) {
        boolean[] mbits = ((Short512Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Short512Vector rotateEL(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector rotateER(int j) {
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
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector shiftEL(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector shiftER(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Short512Vector(res);
    }

    @Override
    @ForceInline
    public Short512Vector rearrange(Vector<Short, Shapes.S512Bit> v,
                                  Shuffle<Short, Shapes.S512Bit> s, Mask<Short, Shapes.S512Bit> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    public Short512Vector rearrange(Shuffle<Short, Shapes.S512Bit> s) {
        return uOp((i, a) -> {
            short[] vec = this.getElements();
            int ei = s.getElement(i);
            return vec[ei];
        });
    }

    @Override
    @ForceInline
    public Short512Vector blend(Vector<Short, Shapes.S512Bit> o1, Mask<Short, Shapes.S512Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Short512Vector v = (Short512Vector)o1;
        Short512Mask   m = (Short512Mask)o2;

        return VectorIntrinsics.blend(
            Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public short get(int i) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return (short) VectorIntrinsics.extract(
                                Short512Vector.class, short.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    short[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Short512Vector with(int i, short e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Short512Vector.class, short.class, LENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    short[] res = v.getElements().clone();
                                    res[ix] = (short)bits;
                                    return new Short512Vector(res);
                                });
    }

    // Mask

    static final class Short512Mask extends AbstractMask<Short, Shapes.S512Bit> {
        static final Short512Mask TRUE_MASK = new Short512Mask(true);
        static final Short512Mask FALSE_MASK = new Short512Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Short512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Short512Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Short512Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Short512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Short512Mask(res);
        }

        @Override
        Short512Mask bOp(Mask<Short, Shapes.S512Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Short512Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Short512Mask(res);
        }

        @Override
        public Short512Species species() {
            return SPECIES;
        }

        @Override
        public Short512Vector toVector() {
            short[] res = new short[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (short) (bits[i] ? -1 : 0);
            }
            return new Short512Vector(res);
        }

        // Unary operations

        @Override
        @ForceInline
        public Short512Mask not() {
            return (Short512Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Short512Mask.class, short.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Short512Mask and(Mask<Short,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Short512Mask m = (Short512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Short512Mask.class, short.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Short512Mask or(Mask<Short,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Short512Mask m = (Short512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Short512Mask.class, short.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Short512Mask.class, short.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Short512Mask.class, short.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Short512Shuffle extends AbstractShuffle<Short, Shapes.S512Bit> {
        Short512Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Short512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Short512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Short512Shuffle(IntUnaryOperator f) {
            super(f);
        }

        @Override
        public Short512Species species() {
            return SPECIES;
        }

        @Override
        public Short512Vector toVector() {
            short[] va = new short[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (short) getElement(i);
            }
            return species().fromArray(va, 0);
        }

        @Override
        public Short512Shuffle rearrange(Vector.Shuffle<Short, Shapes.S512Bit> o) {
            Short512Shuffle s = (Short512Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Short512Shuffle(r);
        }
    }

    // Species

    @Override
    public Short512Species species() {
        return SPECIES;
    }

    static final class Short512Species extends ShortSpecies<Shapes.S512Bit> {
        static final int BIT_SIZE = Shapes.S_512_BIT.bitSize();

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
        public Shapes.S512Bit shape() {
            return Shapes.S_512_BIT;
        }

        @Override
        Short512Vector op(FOp f) {
            short[] res = new short[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Short512Vector(res);
        }

        @Override
        Short512Vector op(Mask<Short, Shapes.S512Bit> o, FOp f) {
            short[] res = new short[length()];
            boolean[] mbits = ((Short512Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Short512Vector(res);
        }

        // Factories

        @Override
        public Short512Mask maskFromValues(boolean... bits) {
            return new Short512Mask(bits);
        }

        @Override
        public Short512Mask maskFromArray(boolean[] bits, int i) {
            return new Short512Mask(bits, i);
        }

        @Override
        public Short512Shuffle shuffle(IntUnaryOperator f) {
            return new Short512Shuffle(f);
        }

        @Override
        public Short512Shuffle shuffleIota() {
            return new Short512Shuffle(AbstractShuffle.IDENTITY);
        }

        @Override
        public Short512Shuffle shuffleFromValues(int... ixs) {
            return new Short512Shuffle(ixs);
        }

        @Override
        public Short512Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Short512Shuffle(ixs, i);
        }

        @Override
        @ForceInline
        public Short512Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Short512Vector.class, short.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Short512Vector broadcast(short e) {
            return VectorIntrinsics.broadcastCoerced(
                Short512Vector.class, short.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (short)bits)));
        }

        @Override
        @ForceInline
        public Short512Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Short512Mask.class, short.class, LENGTH,
                                                     (short)-1,
                                                     (z -> Short512Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Short512Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Short512Mask.class, short.class, LENGTH,
                                                     0,
                                                     (z -> Short512Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Short512Vector scalars(short... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return VectorIntrinsics.load(Short512Vector.class, short.class, LENGTH,
                                         es, Unsafe.ARRAY_SHORT_BASE_OFFSET,
                                         es, ix,
                                         (c, idx) -> op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Short512Vector fromArray(short[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return VectorIntrinsics.load(Short512Vector.class, short.class, LENGTH,
                                         a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_SHORT_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Short512Vector fromArray(short[] a, int ax, Mask<Short, Shapes.S512Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromArray(a, ax), m);
        }

        @Override
        @ForceInline
        public Short512Vector fromByteArray(byte[] a, int ix) {
            // @@@ Endianess
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Short512Vector.class, short.class, LENGTH,
                                         a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
                                             ShortBuffer tb = bbc.asShortBuffer();
                                             return op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Short512Vector fromByteArray(byte[] a, int ix, Mask<Short, Shapes.S512Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Short512Vector fromByteBuffer(ByteBuffer bb, int ix) {
            // @@@ Endianess
            if (bb.order() != ByteOrder.nativeOrder()) {
                throw new IllegalArgumentException();
            }
            ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Short512Vector.class, short.class, LENGTH,
                                         U.getObject(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + ix,
                                         bb, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                             ShortBuffer tb = bbc.asShortBuffer();
                                             return op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Short512Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Short, Shapes.S512Bit> m) {
            // @@@ This can result in out of bounds errors for unset mask lanes
            return zero().blend(fromByteBuffer(bb, ix), m);
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short512Vector castFromByte(ByteVector<S> o) {
            if (o.bitSize() == 64) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.cast(
                    Byte64Vector.class, byte.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.cast(
                    Byte128Vector.class, byte.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.cast(
                    Byte256Vector.class, byte.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.cast(
                    Byte512Vector.class, byte.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short512Vector castFromShort(ShortVector<S> o) {
            if (o.bitSize() == 64) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.cast(
                    Short64Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.cast(
                    Short128Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.cast(
                    Short256Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.cast(
                    Short512Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short512Vector castFromInt(IntVector<S> o) {
            if (o.bitSize() == 64) {
                Int64Vector so = (Int64Vector)o;
                return VectorIntrinsics.cast(
                    Int64Vector.class, int.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Int128Vector so = (Int128Vector)o;
                return VectorIntrinsics.cast(
                    Int128Vector.class, int.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.cast(
                    Int256Vector.class, int.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.cast(
                    Int512Vector.class, int.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short512Vector castFromLong(LongVector<S> o) {
            if (o.bitSize() == 64) {
                Long64Vector so = (Long64Vector)o;
                return VectorIntrinsics.cast(
                    Long64Vector.class, long.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.cast(
                    Long128Vector.class, long.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.cast(
                    Long256Vector.class, long.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.cast(
                    Long512Vector.class, long.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short512Vector castFromFloat(FloatVector<S> o) {
            if (o.bitSize() == 64) {
                Float64Vector so = (Float64Vector)o;
                return VectorIntrinsics.cast(
                    Float64Vector.class, float.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Float128Vector so = (Float128Vector)o;
                return VectorIntrinsics.cast(
                    Float128Vector.class, float.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.cast(
                    Float256Vector.class, float.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.cast(
                    Float512Vector.class, float.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @ForceInline
        @SuppressWarnings("unchecked")
        private <S extends Shape> Short512Vector castFromDouble(DoubleVector<S> o) {
            if (o.bitSize() == 64) {
                Double64Vector so = (Double64Vector)o;
                return VectorIntrinsics.cast(
                    Double64Vector.class, double.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 128) {
                Double128Vector so = (Double128Vector)o;
                return VectorIntrinsics.cast(
                    Double128Vector.class, double.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 256) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.cast(
                    Double256Vector.class, double.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else if (o.bitSize() == 512) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.cast(
                    Double512Vector.class, double.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)super.cast(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <E, S extends Shape> Short512Vector cast(Vector<E, S> o) {
            if (o.length() != LENGTH)
                throw new IllegalArgumentException("Vector length this species length differ");

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
        public <E, S extends Shape> Short512Mask cast(Mask<E, S> m) {
            if (m.length() != LENGTH)
                throw new IllegalArgumentException("Mask length this species length differ");
            return new Short512Mask(m.toArray());
        }

        @Override
        @ForceInline
        public <E, S extends Shape> Short512Shuffle cast(Shuffle<E, S> s) {
            if (s.length() != LENGTH)
                throw new IllegalArgumentException("Shuffle length this species length differ");
            return new Short512Shuffle(s.toArray());
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F> Short512Vector rebracket(Vector<F, Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte512Vector.class, byte.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short512Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int512Vector.class, int.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long512Vector.class, long.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float512Vector.class, float.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double512Vector.class, double.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented type");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Short512Vector resize(Vector<Short, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Short64Vector so = (Short64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short64Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Short128Vector so = (Short128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short128Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short256Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short512Vector.class, short.class, so.length(),
                    short.class, LENGTH, so,
                    (v, t) -> (Short512Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
