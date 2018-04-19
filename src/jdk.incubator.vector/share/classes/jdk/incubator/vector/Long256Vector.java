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
    public LongVector<Shapes.S256Bit> addSaturate(long o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> addSaturate(long o, Mask<Long,Shapes.S256Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
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
    public LongVector<Shapes.S256Bit> subSaturate(long o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector<Shapes.S256Bit> subSaturate(long o, Mask<Long,Shapes.S256Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
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


    // Unary operations



    @Override
    @ForceInline
    public Long256Vector not() {
        return (Long256Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Long256Vector.class, long.class, LENGTH,
            this,
            v1 -> ((Long256Vector)v1).uOp((i, a) -> (long) ~a));
    }
    // Binary operations

    @Override
    @ForceInline
    public Long256Vector add(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return (Long256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long256Vector)v1).bOp(v2, (i, a, b) -> (long)(a + b)));
    }

    @Override
    @ForceInline
    public Long256Vector sub(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return (Long256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long256Vector)v1).bOp(v2, (i, a, b) -> (long)(a - b)));
    }

    @Override
    @ForceInline
    public Long256Vector mul(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return (Long256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long256Vector)v1).bOp(v2, (i, a, b) -> (long)(a * b)));
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
    public Long256Vector max(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return (Long256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long256Vector)v1).bOp(v2, (i, a, b) -> (long) ((a > b) ? a : b)));
        }



    @Override
    @ForceInline
    public Long256Vector and(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return (Long256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long256Vector)v1).bOp(v2, (i, a, b) -> (long)(a & b)));
    }

    @Override
    @ForceInline
    public Long256Vector or(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return (Long256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long256Vector)v1).bOp(v2, (i, a, b) -> (long)(a | b)));
    }

    @Override
    @ForceInline
    public Long256Vector xor(Vector<Long,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;
        return (Long256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Long256Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> ((Long256Vector)v1).bOp(v2, (i, a, b) -> (long)(a ^ b)));
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
        return (Long256Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Long256Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a << i)));
    }

    @Override
    @ForceInline
    public Long256Vector shiftR(int s) {
        return (Long256Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Long256Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a >>> i)));
    }

    @Override
    @ForceInline
    public Long256Vector aShiftR(int s) {
        return (Long256Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Long256Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a >> i)));
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
    public long mulAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 1, (i, a, b) -> (long) (a * b)));
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
    public long xorAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a ^ b)));
    }

    @Override
    @ForceInline
    public long subAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_SUB, Long256Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a - b)));
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(long[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Long256Vector.class, long.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((long[])arr)[idx + i] = a_));
    }

    @Override
    @ForceInline
    public void intoArray(long[] a, int ax, Mask<Long, Shapes.S256Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Long256Vector oldVal = SPECIES.fromArray(a, ax);
        Long256Vector newVal = oldVal.blend(this, m);
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

        Long256Vector that = (Long256Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
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

        return (Long256Mask) VectorIntrinsics.compare(
            BT_eq, Long256Vector.class, Long256Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Long256Mask notEqual(Vector<Long, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;

        return (Long256Mask) VectorIntrinsics.compare(
            BT_ne, Long256Vector.class, Long256Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Long256Mask lessThan(Vector<Long, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;

        return (Long256Mask) VectorIntrinsics.compare(
            BT_lt, Long256Vector.class, Long256Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Long256Mask lessThanEq(Vector<Long, Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Long256Vector v = (Long256Vector)o;

        return (Long256Mask) VectorIntrinsics.compare(
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

        return (Long256Mask) VectorIntrinsics.compare(
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
    public Long256Vector shuffle(Vector<Long, Shapes.S256Bit> o, Shuffle<Long, Shapes.S256Bit> s) {
        Long256Vector v = (Long256Vector) o;
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
    public Long256Vector swizzle(Shuffle<Long, Shapes.S256Bit> s) {
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
    public Long256Vector blend(Vector<Long, Shapes.S256Bit> o1, Mask<Long, Shapes.S256Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Long256Vector v = (Long256Vector)o1;
        Long256Mask   m = (Long256Mask)o2;

        return (Long256Vector) VectorIntrinsics.blend(
            Long256Vector.class, Long256Mask.class, long.class, LENGTH,
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
    public Long256Vector with(int i, long e) {
        long[] res = vec.clone();
        res[i] = e;
        return new Long256Vector(res);
    }

    // Mask

    static final class Long256Mask extends AbstractMask<Long, Shapes.S256Bit> {
        static final Long256Mask TRUE_MASK = new Long256Mask(true);
        static final Long256Mask FALSE_MASK = new Long256Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Long256Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Long256Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
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
                res[i] = (long) (bits[i] ? -1 : 0);
            }
            return new Long256Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S256Bit> rebracket(Species<Z, Shapes.S256Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.reinterpret(
                Long256Mask.class, long.class, LENGTH,
                species.elementType(), species.length(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        //Mask<E, S> not();

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
        static final IntVector.IntSpecies<Shapes.S256Bit> INT_SPECIES = IntVector.speciesInstance(Shapes.S_256_BIT);

        public Long256Shuffle(int[] reorder) {
            super(reorder);
        }

        public Long256Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Long256Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S256Bit> intSpecies() {
            return INT_SPECIES;
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
        public Long256Shuffle shuffleFromValues(int... ixs) {
            return new Long256Shuffle(ixs);
        }

        @Override
        public Long256Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Long256Shuffle(ixs, i);
        }

        @Override
        public Long256Shuffle shuffleFromVector(Vector<Integer, Shapes.S256Bit> v) {
            int[] a = ((IntVector<Shapes.S256Bit>) v).toArray();
            return new Long256Shuffle(a, 0);
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
        public Long256Vector fromArray(long[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Long256Vector) VectorIntrinsics.load(Long256Vector.class, long.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((long[]) arr, idx));
        }

        @Override
        @ForceInline
        public Long256Vector fromArray(long[] a, int ax, Mask<Long, Shapes.S256Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F> Long256Vector rebracket(Vector<F, Shapes.S256Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte256Vector.class, byte.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long256Vector)reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short256Vector so = (Short256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short256Vector.class, short.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long256Vector)reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int256Vector so = (Int256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int256Vector.class, int.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long256Vector)reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long256Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long256Vector)reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float256Vector so = (Float256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float256Vector.class, float.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long256Vector)reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double256Vector so = (Double256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double256Vector.class, double.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long256Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
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
                    Long64Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long256Vector)reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Long128Vector so = (Long128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long128Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long256Vector)reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Long256Vector so = (Long256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long256Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long256Vector)reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long512Vector.class, long.class, so.length(),
                    long.class, LENGTH, so,
                    (v, t) -> (Long256Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
