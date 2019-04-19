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
final class Long64Vector extends LongVector {
    private static final VectorSpecies<Long> SPECIES = LongVector.SPECIES_64;

    static final Long64Vector ZERO = new Long64Vector();

    static final int LENGTH = SPECIES.length();

    // Index vector species
    private static final IntVector.IntSpecies INDEX_SPECIES;

    static {
        INDEX_SPECIES = (IntVector.IntSpecies) IntVector.species(VectorShape.S_64_BIT);
    }

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
    Long64Vector uOp(VectorMask<Long> o, FUnOp f) {
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
    Long64Vector bOp(Vector<Long> o, FBinOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((Long64Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Long64Vector(res);
    }

    @Override
    Long64Vector bOp(Vector<Long> o1, VectorMask<Long> o2, FBinOp f) {
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
    Long64Vector tOp(Vector<Long> o1, Vector<Long> o2, FTriOp f) {
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
    Long64Vector tOp(Vector<Long> o1, Vector<Long> o2, VectorMask<Long> o3, FTriOp f) {
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

    @Override
    @ForceInline
    public <F> Vector<F> cast(VectorSpecies<F> s) {
        Objects.requireNonNull(s);
        if (s.length() != LENGTH)
            throw new IllegalArgumentException("Vector length this species length differ");

        return VectorIntrinsics.cast(
            Long64Vector.class,
            long.class, LENGTH,
            s.boxType(),
            s.elementType(), LENGTH,
            this, s,
            (species, vector) -> vector.castDefault(species)
        );
    }

    @SuppressWarnings("unchecked")
    @ForceInline
    private <F> Vector<F> castDefault(VectorSpecies<F> s) {
        int limit = s.length();

        Class<?> stype = s.elementType();
        if (stype == byte.class) {
            byte[] a = new byte[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (byte) this.get(i);
            }
            return (Vector) ByteVector.fromArray((VectorSpecies<Byte>) s, a, 0);
        } else if (stype == short.class) {
            short[] a = new short[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (short) this.get(i);
            }
            return (Vector) ShortVector.fromArray((VectorSpecies<Short>) s, a, 0);
        } else if (stype == int.class) {
            int[] a = new int[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (int) this.get(i);
            }
            return (Vector) IntVector.fromArray((VectorSpecies<Integer>) s, a, 0);
        } else if (stype == long.class) {
            long[] a = new long[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (long) this.get(i);
            }
            return (Vector) LongVector.fromArray((VectorSpecies<Long>) s, a, 0);
        } else if (stype == float.class) {
            float[] a = new float[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (float) this.get(i);
            }
            return (Vector) FloatVector.fromArray((VectorSpecies<Float>) s, a, 0);
        } else if (stype == double.class) {
            double[] a = new double[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (double) this.get(i);
            }
            return (Vector) DoubleVector.fromArray((VectorSpecies<Double>) s, a, 0);
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }
    }

    @Override
    @ForceInline
    @SuppressWarnings("unchecked")
    public <F> Vector<F> reinterpret(VectorSpecies<F> s) {
        Objects.requireNonNull(s);

        if(s.elementType().equals(long.class)) {
            return (Vector<F>) reshape((VectorSpecies<Long>)s);
        }
        if(s.bitSize() == bitSize()) {
            return reinterpretType(s);
        }

        return defaultReinterpret(s);
    }

    @ForceInline
    private <F> Vector<F> reinterpretType(VectorSpecies<F> s) {
        Objects.requireNonNull(s);

        Class<?> stype = s.elementType();
        if (stype == byte.class) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Byte64Vector.class,
                byte.class, Byte64Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == short.class) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Short64Vector.class,
                short.class, Short64Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == int.class) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Int64Vector.class,
                int.class, Int64Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == long.class) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Long64Vector.class,
                long.class, Long64Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == float.class) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Float64Vector.class,
                float.class, Float64Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == double.class) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Double64Vector.class,
                double.class, Double64Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }
    }

    @Override
    @ForceInline
    public LongVector reshape(VectorSpecies<Long> s) {
        Objects.requireNonNull(s);
        if (s.bitSize() == 64 && (s.boxType() == Long64Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Long64Vector.class,
                long.class, Long64Vector.LENGTH,
                this, s,
                (species, vector) -> (LongVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 128 && (s.boxType() == Long128Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Long128Vector.class,
                long.class, Long128Vector.LENGTH,
                this, s,
                (species, vector) -> (LongVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 256 && (s.boxType() == Long256Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Long256Vector.class,
                long.class, Long256Vector.LENGTH,
                this, s,
                (species, vector) -> (LongVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 512 && (s.boxType() == Long512Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Long512Vector.class,
                long.class, Long512Vector.LENGTH,
                this, s,
                (species, vector) -> (LongVector) vector.defaultReinterpret(species)
            );
        } else if ((s.bitSize() > 0) && (s.bitSize() <= 2048)
                && (s.bitSize() % 128 == 0) && (s.boxType() == LongMaxVector.class)) {
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                LongMaxVector.class,
                long.class, LongMaxVector.LENGTH,
                this, s,
                (species, vector) -> (LongVector) vector.defaultReinterpret(species)
            );
        } else {
            throw new InternalError("Unimplemented size");
        }
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public LongVector add(long o) {
        return add((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public LongVector add(long o, VectorMask<Long> m) {
        return add((Long64Vector)LongVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public LongVector sub(long o) {
        return sub((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public LongVector sub(long o, VectorMask<Long> m) {
        return sub((Long64Vector)LongVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public LongVector mul(long o) {
        return mul((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public LongVector mul(long o, VectorMask<Long> m) {
        return mul((Long64Vector)LongVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public LongVector min(long o) {
        return min((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public LongVector max(long o) {
        return max((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Long> equal(long o) {
        return equal((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Long> notEqual(long o) {
        return notEqual((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Long> lessThan(long o) {
        return lessThan((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Long> lessThanEq(long o) {
        return lessThanEq((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Long> greaterThan(long o) {
        return greaterThan((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Long> greaterThanEq(long o) {
        return greaterThanEq((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public LongVector blend(long o, VectorMask<Long> m) {
        return blend((Long64Vector)LongVector.broadcast(SPECIES, o), m);
    }


    @Override
    @ForceInline
    public LongVector and(long o) {
        return and((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public LongVector and(long o, VectorMask<Long> m) {
        return and((Long64Vector)LongVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public LongVector or(long o) {
        return or((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public LongVector or(long o, VectorMask<Long> m) {
        return or((Long64Vector)LongVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public LongVector xor(long o) {
        return xor((Long64Vector)LongVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public LongVector xor(long o, VectorMask<Long> m) {
        return xor((Long64Vector)LongVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public Long64Vector neg() {
        return (Long64Vector)zero(SPECIES).sub(this);
    }

    // Unary operations

    @ForceInline
    @Override
    public Long64Vector neg(VectorMask<Long> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public Long64Vector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Long64Vector.class, long.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (long) Math.abs(a)));
    }

    @ForceInline
    @Override
    public Long64Vector abs(VectorMask<Long> m) {
        return blend(abs(), m);
    }


    @Override
    @ForceInline
    public Long64Vector not() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Long64Vector.class, long.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (long) ~a));
    }

    @ForceInline
    @Override
    public Long64Vector not(VectorMask<Long> m) {
        return blend(not(), m);
    }
    // Binary operations

    @Override
    @ForceInline
    public Long64Vector add(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a + b)));
    }

    @Override
    @ForceInline
    public Long64Vector add(Vector<Long> v, VectorMask<Long> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector sub(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a - b)));
    }

    @Override
    @ForceInline
    public Long64Vector sub(Vector<Long> v, VectorMask<Long> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector mul(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a * b)));
    }

    @Override
    @ForceInline
    public Long64Vector mul(Vector<Long> v, VectorMask<Long> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector min(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return (Long64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public Long64Vector min(Vector<Long> v, VectorMask<Long> m) {
        return blend(min(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector max(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long) Math.max(a, b)));
        }

    @Override
    @ForceInline
    public Long64Vector max(Vector<Long> v, VectorMask<Long> m) {
        return blend(max(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector and(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a & b)));
    }

    @Override
    @ForceInline
    public Long64Vector or(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a | b)));
    }

    @Override
    @ForceInline
    public Long64Vector xor(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Long64Vector.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (long)(a ^ b)));
    }

    @Override
    @ForceInline
    public Long64Vector and(Vector<Long> v, VectorMask<Long> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector or(Vector<Long> v, VectorMask<Long> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector xor(Vector<Long> v, VectorMask<Long> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector shiftL(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Long64Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a << i)));
    }

    @Override
    @ForceInline
    public Long64Vector shiftL(int s, VectorMask<Long> m) {
        return blend(shiftL(s), m);
    }

    @Override
    @ForceInline
    public Long64Vector shiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Long64Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a >>> i)));
    }

    @Override
    @ForceInline
    public Long64Vector shiftR(int s, VectorMask<Long> m) {
        return blend(shiftR(s), m);
    }

    @Override
    @ForceInline
    public Long64Vector aShiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Long64Vector.class, long.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (long) (a >> i)));
    }

    @Override
    @ForceInline
    public Long64Vector aShiftR(int s, VectorMask<Long> m) {
        return blend(aShiftR(s), m);
    }

    @Override
    @ForceInline
    public Long64Vector shiftL(Vector<Long> s) {
        Long64Vector shiftv = (Long64Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(LongVector.broadcast(SPECIES, 0x3f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_LSHIFT, Long64Vector.class, long.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (long) (a << b)));
    }

    @Override
    @ForceInline
    public Long64Vector shiftR(Vector<Long> s) {
        Long64Vector shiftv = (Long64Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(LongVector.broadcast(SPECIES, 0x3f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_URSHIFT, Long64Vector.class, long.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (long) (a >>> b)));
    }

    @Override
    @ForceInline
    public Long64Vector aShiftR(Vector<Long> s) {
        Long64Vector shiftv = (Long64Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(LongVector.broadcast(SPECIES, 0x3f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_RSHIFT, Long64Vector.class, long.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (long) (a >> b)));
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
    public long andAll(VectorMask<Long> m) {
        return blend((Long64Vector)LongVector.broadcast(SPECIES, (long) -1), m).andAll();
    }

    @Override
    @ForceInline
    public long minAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MIN, Long64Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp(Long.MAX_VALUE , (i, a, b) -> (long) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public long maxAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MAX, Long64Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp(Long.MIN_VALUE , (i, a, b) -> (long) Math.max(a, b)));
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
    public long orAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Long64Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a | b)));
    }

    @Override
    @ForceInline
    public long orAll(VectorMask<Long> m) {
        return blend((Long64Vector)LongVector.broadcast(SPECIES, (long) 0), m).orAll();
    }

    @Override
    @ForceInline
    public long xorAll() {
        return (long) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Long64Vector.class, long.class, LENGTH,
            this,
            v -> (long) v.rOp((long) 0, (i, a, b) -> (long) (a ^ b)));
    }

    @Override
    @ForceInline
    public long xorAll(VectorMask<Long> m) {
        return blend((Long64Vector)LongVector.broadcast(SPECIES, (long) 0), m).xorAll();
    }


    @Override
    @ForceInline
    public long addAll(VectorMask<Long> m) {
        return blend((Long64Vector)LongVector.broadcast(SPECIES, (long) 0), m).addAll();
    }


    @Override
    @ForceInline
    public long mulAll(VectorMask<Long> m) {
        return blend((Long64Vector)LongVector.broadcast(SPECIES, (long) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public long minAll(VectorMask<Long> m) {
        return blend((Long64Vector)LongVector.broadcast(SPECIES, Long.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public long maxAll(VectorMask<Long> m) {
        return blend((Long64Vector)LongVector.broadcast(SPECIES, Long.MIN_VALUE), m).maxAll();
    }

    @Override
    @ForceInline
    public VectorShuffle<Long> toShuffle() {
        long[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return VectorShuffle.fromArray(SPECIES, sa, 0);
    }

    // Memory operations

    private static final int ARRAY_SHIFT         = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_LONG_INDEX_SCALE);
    private static final int BOOLEAN_ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_BOOLEAN_INDEX_SCALE);

    @Override
    @ForceInline
    public void intoArray(long[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Long64Vector.class, long.class, LENGTH,
                               a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_LONG_BASE_OFFSET,
                               this,
                               a, ix,
                               (arr, idx, v) -> v.forEach((i, e) -> arr[idx + i] = e));
    }

    @Override
    @ForceInline
    public final void intoArray(long[] a, int ax, VectorMask<Long> m) {
        LongVector oldVal = LongVector.fromArray(SPECIES, a, ax);
        LongVector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }
    @Override
    @ForceInline
    public void intoArray(long[] a, int ix, int[] b, int iy) {
        this.intoArray(a, ix + b[iy]);
    }

     @Override
     @ForceInline
     public final void intoArray(long[] a, int ax, VectorMask<Long> m, int[] b, int iy) {
         // @@@ This can result in out of bounds errors for unset mask lanes
         LongVector oldVal = LongVector.fromArray(SPECIES, a, ax, b, iy);
         LongVector newVal = oldVal.blend(this, m);
         newVal.intoArray(a, ax, b, iy);
     }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Long64Vector.class, long.class, LENGTH,
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
    public final void intoByteArray(byte[] a, int ix, VectorMask<Long> m) {
        Long64Vector oldVal = (Long64Vector) LongVector.fromByteArray(SPECIES, a, ix);
        Long64Vector newVal = oldVal.blend(this, m);
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
        VectorIntrinsics.store(Long64Vector.class, long.class, LENGTH,
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
    public void intoByteBuffer(ByteBuffer bb, int ix, VectorMask<Long> m) {
        Long64Vector oldVal = (Long64Vector) LongVector.fromByteBuffer(SPECIES, bb, ix);
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
        return this.equal(that).allTrue();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Long64Mask bTest(Vector<Long> o, FBinTest f) {
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
    public Long64Mask equal(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return VectorIntrinsics.compare(
            BT_eq, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Long64Mask notEqual(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return VectorIntrinsics.compare(
            BT_ne, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Long64Mask lessThan(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return VectorIntrinsics.compare(
            BT_lt, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Long64Mask lessThanEq(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return VectorIntrinsics.compare(
            BT_le, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Long64Mask greaterThan(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return (Long64Mask) VectorIntrinsics.compare(
            BT_gt, Long64Vector.class, Long64Mask.class, long.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Long64Mask greaterThanEq(Vector<Long> o) {
        Objects.requireNonNull(o);
        Long64Vector v = (Long64Vector)o;

        return VectorIntrinsics.compare(
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
    void forEach(VectorMask<Long> o, FUnCon f) {
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
    @ForceInline
    public Long64Vector rearrange(Vector<Long> v,
                                  VectorShuffle<Long> s, VectorMask<Long> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    @ForceInline
    public Long64Vector rearrange(VectorShuffle<Long> o1) {
        Objects.requireNonNull(o1);
        Long64Shuffle s =  (Long64Shuffle)o1;

        return VectorIntrinsics.rearrangeOp(
            Long64Vector.class, Long64Shuffle.class, long.class, LENGTH,
            this, s,
            (v1, s_) -> v1.uOp((i, a) -> {
                int ei = s_.getElement(i);
                return v1.get(ei);
            }));
    }

    @Override
    @ForceInline
    public Long64Vector blend(Vector<Long> o1, VectorMask<Long> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Long64Vector v = (Long64Vector)o1;
        Long64Mask   m = (Long64Mask)o2;

        return VectorIntrinsics.blend(
            Long64Vector.class, Long64Mask.class, long.class, LENGTH,
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
                                Long64Vector.class, long.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    long[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Long64Vector with(int i, long e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Long64Vector.class, long.class, LENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    long[] res = v.getElements().clone();
                                    res[ix] = (long)bits;
                                    return new Long64Vector(res);
                                });
    }

    // Mask

    static final class Long64Mask extends AbstractMask<Long> {
        static final Long64Mask TRUE_MASK = new Long64Mask(true);
        static final Long64Mask FALSE_MASK = new Long64Mask(false);

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Long64Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Long64Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
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
        Long64Mask bOp(VectorMask<Long> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Long64Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Long64Mask(res);
        }

        @Override
        public VectorSpecies<Long> species() {
            return SPECIES;
        }

        @Override
        public Long64Vector toVector() {
            long[] res = new long[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (long) (bits[i] ? -1 : 0);
            }
            return new Long64Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <E> VectorMask<E> cast(VectorSpecies<E> species) {
            if (length() != species.length())
                throw new IllegalArgumentException("VectorMask length and species length differ");
            Class<?> stype = species.elementType();
            boolean [] maskArray = toArray();
            if (stype == byte.class) {
                return (VectorMask <E>) new Byte64Vector.Byte64Mask(maskArray);
            } else if (stype == short.class) {
                return (VectorMask <E>) new Short64Vector.Short64Mask(maskArray);
            } else if (stype == int.class) {
                return (VectorMask <E>) new Int64Vector.Int64Mask(maskArray);
            } else if (stype == long.class) {
                return (VectorMask <E>) new Long64Vector.Long64Mask(maskArray);
            } else if (stype == float.class) {
                return (VectorMask <E>) new Float64Vector.Float64Mask(maskArray);
            } else if (stype == double.class) {
                return (VectorMask <E>) new Double64Vector.Double64Mask(maskArray);
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }
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
        public Long64Mask and(VectorMask<Long> o) {
            Objects.requireNonNull(o);
            Long64Mask m = (Long64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Long64Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Long64Mask or(VectorMask<Long> o) {
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
            return VectorIntrinsics.test(BT_ne, Long64Mask.class, long.class, LENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Long64Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Long64Mask.class, long.class, LENGTH,
                                         this, VectorMask.maskAllTrue(species()),
                                         (m, __) -> allTrueHelper(((Long64Mask)m).getBits()));
        }
    }

    // Shuffle

    static final class Long64Shuffle extends AbstractShuffle<Long> {
        Long64Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Long64Shuffle(int[] reorder) {
            super(reorder);
        }

        public Long64Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Long64Shuffle(IntUnaryOperator f) {
            super(f);
        }

        @Override
        public VectorSpecies<Long> species() {
            return SPECIES;
        }

        @Override
        public LongVector toVector() {
            long[] va = new long[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (long) getElement(i);
            }
            return LongVector.fromArray(SPECIES, va, 0);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F> VectorShuffle<F> cast(VectorSpecies<F> species) {
            if (length() != species.length())
                throw new IllegalArgumentException("Shuffle length and species length differ");
            Class<?> stype = species.elementType();
            int [] shuffleArray = toArray();
            if (stype == byte.class) {
                return (VectorShuffle<F>) new Byte64Vector.Byte64Shuffle(shuffleArray);
            } else if (stype == short.class) {
                return (VectorShuffle<F>) new Short64Vector.Short64Shuffle(shuffleArray);
            } else if (stype == int.class) {
                return (VectorShuffle<F>) new Int64Vector.Int64Shuffle(shuffleArray);
            } else if (stype == long.class) {
                return (VectorShuffle<F>) new Long64Vector.Long64Shuffle(shuffleArray);
            } else if (stype == float.class) {
                return (VectorShuffle<F>) new Float64Vector.Float64Shuffle(shuffleArray);
            } else if (stype == double.class) {
                return (VectorShuffle<F>) new Double64Vector.Double64Shuffle(shuffleArray);
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }
        }

        @Override
        public Long64Shuffle rearrange(VectorShuffle<Long> o) {
            Long64Shuffle s = (Long64Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Long64Shuffle(r);
        }
    }

    // VectorSpecies

    @Override
    public VectorSpecies<Long> species() {
        return SPECIES;
    }
}
