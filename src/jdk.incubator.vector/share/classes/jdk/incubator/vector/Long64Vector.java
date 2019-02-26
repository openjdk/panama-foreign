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
    Long64Vector uOp(Mask<Long> o, FUnOp f) {
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
    Long64Vector bOp(Vector<Long> o1, Mask<Long> o2, FBinOp f) {
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
    Long64Vector tOp(Vector<Long> o1, Vector<Long> o2, Mask<Long> o3, FTriOp f) {
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
    public <F> Vector<F> cast(Species<F> s) {
        Objects.requireNonNull(s);
        if (s.length() != LENGTH)
            throw new IllegalArgumentException("Vector length this species length differ");

        return VectorIntrinsics.cast(
            Long64Vector.class,
            long.class, LENGTH,
            s.vectorType(),
            s.elementType(), LENGTH,
            this, s,
            (species, vector) -> vector.castDefault(species)
        );
    }

    @SuppressWarnings("unchecked")
    @ForceInline
    private <F> Vector<F> castDefault(Species<F> s) {
        int limit = s.length();

        Class<?> stype = s.elementType();
        if (stype == byte.class) {
            byte[] a = new byte[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (byte) this.get(i);
            }
            return (Vector) ((ByteVector.ByteSpecies)s).fromArray(a, 0);
        } else if (stype == short.class) {
            short[] a = new short[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (short) this.get(i);
            }
            return (Vector) ((ShortVector.ShortSpecies)s).fromArray(a, 0);
        } else if (stype == int.class) {
            int[] a = new int[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (int) this.get(i);
            }
            return (Vector) ((IntVector.IntSpecies)s).fromArray(a, 0);
        } else if (stype == long.class) {
            long[] a = new long[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (long) this.get(i);
            }
            return (Vector) ((LongVector.LongSpecies)s).fromArray(a, 0);
        } else if (stype == float.class) {
            float[] a = new float[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (float) this.get(i);
            }
            return (Vector) ((FloatVector.FloatSpecies)s).fromArray(a, 0);
        } else if (stype == double.class) {
            double[] a = new double[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (double) this.get(i);
            }
            return (Vector) ((DoubleVector.DoubleSpecies)s).fromArray(a, 0);
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }
    }

    @Override
    @ForceInline
    @SuppressWarnings("unchecked")
    public <F> Vector<F> reinterpret(Species<F> s) {
        Objects.requireNonNull(s);

        if(s.elementType().equals(long.class)) {
            return (Vector<F>) reshape((Species<Long>)s);
        }
        if(s.bitSize() == bitSize()) {
            return reinterpretType(s);
        }

        return defaultReinterpret(s);
    }

    @ForceInline
    private <F> Vector<F> reinterpretType(Species<F> s) {
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
    public LongVector reshape(Species<Long> s) {
        Objects.requireNonNull(s);
        if (s.bitSize() == 64 && (s instanceof Long64Vector.Long64Species)) {
            Long64Vector.Long64Species ts = (Long64Vector.Long64Species)s;
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Long64Vector.class,
                long.class, Long64Vector.LENGTH,
                this, ts,
                (species, vector) -> (LongVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 128 && (s instanceof Long128Vector.Long128Species)) {
            Long128Vector.Long128Species ts = (Long128Vector.Long128Species)s;
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Long128Vector.class,
                long.class, Long128Vector.LENGTH,
                this, ts,
                (species, vector) -> (LongVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 256 && (s instanceof Long256Vector.Long256Species)) {
            Long256Vector.Long256Species ts = (Long256Vector.Long256Species)s;
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Long256Vector.class,
                long.class, Long256Vector.LENGTH,
                this, ts,
                (species, vector) -> (LongVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 512 && (s instanceof Long512Vector.Long512Species)) {
            Long512Vector.Long512Species ts = (Long512Vector.Long512Species)s;
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                Long512Vector.class,
                long.class, Long512Vector.LENGTH,
                this, ts,
                (species, vector) -> (LongVector) vector.defaultReinterpret(species)
            );
        } else if ((s.bitSize() > 0) && (s.bitSize() <= 2048)
                && (s.bitSize() % 128 == 0) && (s instanceof LongMaxVector.LongMaxSpecies)) {
            LongMaxVector.LongMaxSpecies ts = (LongMaxVector.LongMaxSpecies)s;
            return VectorIntrinsics.reinterpret(
                Long64Vector.class,
                long.class, LENGTH,
                LongMaxVector.class,
                long.class, LongMaxVector.LENGTH,
                this, ts,
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
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector add(long o, Mask<Long> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector sub(long o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector sub(long o, Mask<Long> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector mul(long o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector mul(long o, Mask<Long> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector min(long o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector max(long o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long> equal(long o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long> notEqual(long o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long> lessThan(long o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long> lessThanEq(long o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long> greaterThan(long o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Long> greaterThanEq(long o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector blend(long o, Mask<Long> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public LongVector and(long o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector and(long o, Mask<Long> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector or(long o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector or(long o, Mask<Long> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public LongVector xor(long o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public LongVector xor(long o, Mask<Long> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Long64Vector neg() {
        return SPECIES.zero().sub(this);
    }

    // Unary operations

    @ForceInline
    @Override
    public Long64Vector neg(Mask<Long> m) {
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
    public Long64Vector abs(Mask<Long> m) {
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
    public Long64Vector not(Mask<Long> m) {
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
    public Long64Vector add(Vector<Long> v, Mask<Long> m) {
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
    public Long64Vector sub(Vector<Long> v, Mask<Long> m) {
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
    public Long64Vector mul(Vector<Long> v, Mask<Long> m) {
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
    public Long64Vector min(Vector<Long> v, Mask<Long> m) {
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
    public Long64Vector max(Vector<Long> v, Mask<Long> m) {
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
    public Long64Vector and(Vector<Long> v, Mask<Long> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector or(Vector<Long> v, Mask<Long> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Long64Vector xor(Vector<Long> v, Mask<Long> m) {
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
    public Long64Vector shiftL(int s, Mask<Long> m) {
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
    public Long64Vector shiftR(int s, Mask<Long> m) {
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
    public Long64Vector aShiftR(int s, Mask<Long> m) {
        return blend(aShiftR(s), m);
    }

    @Override
    @ForceInline
    public Long64Vector shiftL(Vector<Long> s) {
        Long64Vector shiftv = (Long64Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(species().broadcast(0x3f));
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
        shiftv = shiftv.and(species().broadcast(0x3f));
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
        shiftv = shiftv.and(species().broadcast(0x3f));
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
    public long andAll(Mask<Long> m) {
        return blend(SPECIES.broadcast((long) -1), m).andAll();
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
    public long orAll(Mask<Long> m) {
        return blend(SPECIES.broadcast((long) 0), m).orAll();
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
    public long xorAll(Mask<Long> m) {
        return blend(SPECIES.broadcast((long) 0), m).xorAll();
    }


    @Override
    @ForceInline
    public long addAll(Mask<Long> m) {
        return blend(SPECIES.broadcast((long) 0), m).addAll();
    }

    @Override
    @ForceInline
    public long subAll(Mask<Long> m) {
        return blend(SPECIES.broadcast((long) 0), m).subAll();
    }

    @Override
    @ForceInline
    public long mulAll(Mask<Long> m) {
        return blend(SPECIES.broadcast((long) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public long minAll(Mask<Long> m) {
        return blend(SPECIES.broadcast(Long.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public long maxAll(Mask<Long> m) {
        return blend(SPECIES.broadcast(Long.MIN_VALUE), m).maxAll();
    }

    @Override
    @ForceInline
    public Shuffle<Long> toShuffle() {
        long[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return SPECIES.shuffleFromArray(sa, 0);
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
    public final void intoArray(long[] a, int ax, Mask<Long> m) {
        Long64Vector oldVal = SPECIES.fromArray(a, ax);
        Long64Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }
    @Override
    @ForceInline
    public void intoArray(long[] a, int ix, int[] b, int iy) {
        this.intoArray(a, ix + b[iy]);
    }

     @Override
     @ForceInline
     public final void intoArray(long[] a, int ax, Mask<Long> m, int[] b, int iy) {
         // @@@ This can result in out of bounds errors for unset mask lanes
         Long64Vector oldVal = SPECIES.fromArray(a, ax, b, iy);
         Long64Vector newVal = oldVal.blend(this, m);
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
    public final void intoByteArray(byte[] a, int ix, Mask<Long> m) {
        Long64Vector oldVal = SPECIES.fromByteArray(a, ix);
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
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Long> m) {
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
    void forEach(Mask<Long> o, FUnCon f) {
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
                                  Shuffle<Long> s, Mask<Long> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    @ForceInline
    public Long64Vector rearrange(Shuffle<Long> o1) {
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
    public Long64Vector blend(Vector<Long> o1, Mask<Long> o2) {
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
        Long64Mask bOp(Mask<Long> o, MBinOp f) {
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
        @ForceInline
        public <F> Mask<F> cast(Species<F> s) {
            if (s.length() != LENGTH)
                throw new IllegalArgumentException("This mask's length and given species length differ");
            return s.maskFromArray(toArray(), 0);
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
        public Long64Mask and(Mask<Long> o) {
            Objects.requireNonNull(o);
            Long64Mask m = (Long64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Long64Mask.class, long.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Long64Mask or(Mask<Long> o) {
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
                                         (m, __) -> anyTrueHelper(m.getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Long64Mask.class, long.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m, __) -> allTrueHelper(m.getBits()));
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
        public Long64Species species() {
            return SPECIES;
        }

        @Override
        @ForceInline
        public <F> Shuffle<F> cast(Species<F> s) {
            if (s.length() != LENGTH)
                throw new IllegalArgumentException("This shuffle and the given species's length differ");
            return s.shuffleFromArray(toArray(), 0);
        }

        @Override
        public Long64Vector toVector() {
            long[] va = new long[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (long) getElement(i);
            }
            return species().fromArray(va, 0);
        }

        @Override
        public Long64Shuffle rearrange(Vector.Shuffle<Long> o) {
            Long64Shuffle s = (Long64Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Long64Shuffle(r);
        }
    }

    // Species

    @Override
    public Long64Species species() {
        return SPECIES;
    }

    static final class Long64Species extends LongSpecies {
        static final int BIT_SIZE = Shape.S_64_BIT.bitSize();

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
        @SuppressWarnings("unchecked")
        Class<?> vectorType() {
            return Long64Vector.class;
        }

        @Override
        @ForceInline
        public Shape shape() {
            return Shape.S_64_BIT;
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
        Long64Vector op(Mask<Long> o, FOp f) {
            long[] res = new long[length()];
            boolean[] mbits = ((Long64Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Long64Vector(res);
        }

        @Override
        Long64Mask opm(FOpm f) {
            boolean[] res = new boolean[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = (boolean)f.apply(i);
            }
            return new Long64Mask(res);
        }

        // Factories

        @Override
        public Long64Mask maskFromValues(boolean... bits) {
            return new Long64Mask(bits);
        }

        @Override
        public Long64Shuffle shuffle(IntUnaryOperator f) {
            return new Long64Shuffle(f);
        }

        @Override
        public Long64Shuffle shuffleIota() {
            return new Long64Shuffle(AbstractShuffle.IDENTITY);
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
            return VectorIntrinsics.load(Long64Vector.class, long.class, LENGTH,
                                         es, Unsafe.ARRAY_LONG_BASE_OFFSET,
                                         es, ix,
                                         (c, idx) -> SPECIES.op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Long64Mask maskFromArray(boolean[] bits, int ix) {
            Objects.requireNonNull(bits);
            ix = VectorIntrinsics.checkIndex(ix, bits.length, LENGTH);
            return VectorIntrinsics.load(Long64Mask.class, long.class, LENGTH,
                                         bits, (((long)ix) << BOOLEAN_ARRAY_SHIFT)+ Unsafe.ARRAY_BOOLEAN_BASE_OFFSET,
                                         bits, ix,
                                         (c, idx) -> SPECIES.opm(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Long64Vector fromArray(long[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return VectorIntrinsics.load(Long64Vector.class, long.class, LENGTH,
                                         a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_LONG_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> SPECIES.op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Long64Vector fromArray(long[] a, int ax, Mask<Long> m) {
            return zero().blend(fromArray(a, ax), m);
        }

        @Override
        @ForceInline
        public Long64Vector fromByteArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Long64Vector.class, long.class, LENGTH,
                                         a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                             LongBuffer tb = bbc.asLongBuffer();
                                             return SPECIES.op(i -> tb.get());
                                         });
        }
        @Override
        @ForceInline
        public Long64Vector fromArray(long[] a, int ix, int[] b, int iy) {
            return SPECIES.fromArray(a, ix + b[iy]);
       }

       @Override
       @ForceInline
       public Long64Vector fromArray(long[] a, int ax, Mask<Long> m, int[] indexMap, int j) {
           // @@@ This can result in out of bounds errors for unset mask lanes
           return zero().blend(fromArray(a, ax, indexMap, j), m);
       }


        @Override
        @ForceInline
        public Long64Vector fromByteArray(byte[] a, int ix, Mask<Long> m) {
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Long64Vector fromByteBuffer(ByteBuffer bb, int ix) {
            if (bb.order() != ByteOrder.nativeOrder()) {
                throw new IllegalArgumentException();
            }
            ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Long64Vector.class, long.class, LENGTH,
                                         U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + ix,
                                         bb, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                             LongBuffer tb = bbc.asLongBuffer();
                                             return SPECIES.op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Long64Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Long> m) {
            return zero().blend(fromByteBuffer(bb, ix), m);
        }
    }
}
