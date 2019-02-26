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
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Byte512Vector extends ByteVector {
    static final Byte512Species SPECIES = new Byte512Species();

    static final Byte512Vector ZERO = new Byte512Vector();

    static final int LENGTH = SPECIES.length();

    private final byte[] vec; // Don't access directly, use getElements() instead.

    private byte[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Byte512Vector() {
        vec = new byte[SPECIES.length()];
    }

    Byte512Vector(byte[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Byte512Vector uOp(FUnOp f) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Byte512Vector(res);
    }

    @Override
    Byte512Vector uOp(Mask<Byte> o, FUnOp f) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        boolean[] mbits = ((Byte512Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Byte512Vector(res);
    }

    // Binary operator

    @Override
    Byte512Vector bOp(Vector<Byte> o, FBinOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = this.getElements();
        byte[] vec2 = ((Byte512Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Byte512Vector(res);
    }

    @Override
    Byte512Vector bOp(Vector<Byte> o1, Mask<Byte> o2, FBinOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = this.getElements();
        byte[] vec2 = ((Byte512Vector)o1).getElements();
        boolean[] mbits = ((Byte512Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Byte512Vector(res);
    }

    // Trinary operator

    @Override
    Byte512Vector tOp(Vector<Byte> o1, Vector<Byte> o2, FTriOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = this.getElements();
        byte[] vec2 = ((Byte512Vector)o1).getElements();
        byte[] vec3 = ((Byte512Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Byte512Vector(res);
    }

    @Override
    Byte512Vector tOp(Vector<Byte> o1, Vector<Byte> o2, Mask<Byte> o3, FTriOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = getElements();
        byte[] vec2 = ((Byte512Vector)o1).getElements();
        byte[] vec3 = ((Byte512Vector)o2).getElements();
        boolean[] mbits = ((Byte512Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Byte512Vector(res);
    }

    @Override
    byte rOp(byte v, FBinOp f) {
        byte[] vec = getElements();
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
            Byte512Vector.class,
            byte.class, LENGTH,
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

        if(s.elementType().equals(byte.class)) {
            return (Vector<F>) reshape((Species<Byte>)s);
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
                Byte512Vector.class,
                byte.class, LENGTH,
                Byte512Vector.class,
                byte.class, Byte512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == short.class) {
            return VectorIntrinsics.reinterpret(
                Byte512Vector.class,
                byte.class, LENGTH,
                Short512Vector.class,
                short.class, Short512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == int.class) {
            return VectorIntrinsics.reinterpret(
                Byte512Vector.class,
                byte.class, LENGTH,
                Int512Vector.class,
                int.class, Int512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == long.class) {
            return VectorIntrinsics.reinterpret(
                Byte512Vector.class,
                byte.class, LENGTH,
                Long512Vector.class,
                long.class, Long512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == float.class) {
            return VectorIntrinsics.reinterpret(
                Byte512Vector.class,
                byte.class, LENGTH,
                Float512Vector.class,
                float.class, Float512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == double.class) {
            return VectorIntrinsics.reinterpret(
                Byte512Vector.class,
                byte.class, LENGTH,
                Double512Vector.class,
                double.class, Double512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }
    }

    @Override
    @ForceInline
    public ByteVector reshape(Species<Byte> s) {
        Objects.requireNonNull(s);
        if (s.bitSize() == 64 && (s instanceof Byte64Vector.Byte64Species)) {
            Byte64Vector.Byte64Species ts = (Byte64Vector.Byte64Species)s;
            return VectorIntrinsics.reinterpret(
                Byte512Vector.class,
                byte.class, LENGTH,
                Byte64Vector.class,
                byte.class, Byte64Vector.LENGTH,
                this, ts,
                (species, vector) -> (ByteVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 128 && (s instanceof Byte128Vector.Byte128Species)) {
            Byte128Vector.Byte128Species ts = (Byte128Vector.Byte128Species)s;
            return VectorIntrinsics.reinterpret(
                Byte512Vector.class,
                byte.class, LENGTH,
                Byte128Vector.class,
                byte.class, Byte128Vector.LENGTH,
                this, ts,
                (species, vector) -> (ByteVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 256 && (s instanceof Byte256Vector.Byte256Species)) {
            Byte256Vector.Byte256Species ts = (Byte256Vector.Byte256Species)s;
            return VectorIntrinsics.reinterpret(
                Byte512Vector.class,
                byte.class, LENGTH,
                Byte256Vector.class,
                byte.class, Byte256Vector.LENGTH,
                this, ts,
                (species, vector) -> (ByteVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 512 && (s instanceof Byte512Vector.Byte512Species)) {
            Byte512Vector.Byte512Species ts = (Byte512Vector.Byte512Species)s;
            return VectorIntrinsics.reinterpret(
                Byte512Vector.class,
                byte.class, LENGTH,
                Byte512Vector.class,
                byte.class, Byte512Vector.LENGTH,
                this, ts,
                (species, vector) -> (ByteVector) vector.defaultReinterpret(species)
            );
        } else if ((s.bitSize() > 0) && (s.bitSize() <= 2048)
                && (s.bitSize() % 128 == 0) && (s instanceof ByteMaxVector.ByteMaxSpecies)) {
            ByteMaxVector.ByteMaxSpecies ts = (ByteMaxVector.ByteMaxSpecies)s;
            return VectorIntrinsics.reinterpret(
                Byte512Vector.class,
                byte.class, LENGTH,
                ByteMaxVector.class,
                byte.class, ByteMaxVector.LENGTH,
                this, ts,
                (species, vector) -> (ByteVector) vector.defaultReinterpret(species)
            );
        } else {
            throw new InternalError("Unimplemented size");
        }
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public ByteVector add(byte o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector add(byte o, Mask<Byte> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector sub(byte o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector sub(byte o, Mask<Byte> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector mul(byte o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector mul(byte o, Mask<Byte> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector min(byte o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector max(byte o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte> equal(byte o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte> notEqual(byte o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte> lessThan(byte o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte> lessThanEq(byte o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte> greaterThan(byte o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte> greaterThanEq(byte o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector blend(byte o, Mask<Byte> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public ByteVector and(byte o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector and(byte o, Mask<Byte> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector or(byte o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector or(byte o, Mask<Byte> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector xor(byte o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector xor(byte o, Mask<Byte> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Byte512Vector neg() {
        return SPECIES.zero().sub(this);
    }

    // Unary operations

    @ForceInline
    @Override
    public Byte512Vector neg(Mask<Byte> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public Byte512Vector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Byte512Vector.class, byte.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (byte) Math.abs(a)));
    }

    @ForceInline
    @Override
    public Byte512Vector abs(Mask<Byte> m) {
        return blend(abs(), m);
    }


    @Override
    @ForceInline
    public Byte512Vector not() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Byte512Vector.class, byte.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (byte) ~a));
    }

    @ForceInline
    @Override
    public Byte512Vector not(Mask<Byte> m) {
        return blend(not(), m);
    }
    // Binary operations

    @Override
    @ForceInline
    public Byte512Vector add(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a + b)));
    }

    @Override
    @ForceInline
    public Byte512Vector add(Vector<Byte> v, Mask<Byte> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector sub(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a - b)));
    }

    @Override
    @ForceInline
    public Byte512Vector sub(Vector<Byte> v, Mask<Byte> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector mul(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a * b)));
    }

    @Override
    @ForceInline
    public Byte512Vector mul(Vector<Byte> v, Mask<Byte> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector min(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return (Byte512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public Byte512Vector min(Vector<Byte> v, Mask<Byte> m) {
        return blend(min(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector max(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte) Math.max(a, b)));
        }

    @Override
    @ForceInline
    public Byte512Vector max(Vector<Byte> v, Mask<Byte> m) {
        return blend(max(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector and(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a & b)));
    }

    @Override
    @ForceInline
    public Byte512Vector or(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a | b)));
    }

    @Override
    @ForceInline
    public Byte512Vector xor(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (byte)(a ^ b)));
    }

    @Override
    @ForceInline
    public Byte512Vector and(Vector<Byte> v, Mask<Byte> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector or(Vector<Byte> v, Mask<Byte> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector xor(Vector<Byte> v, Mask<Byte> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector shiftL(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Byte512Vector.class, byte.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (byte) (a << (i & 7))));
    }

    @Override
    @ForceInline
    public Byte512Vector shiftL(int s, Mask<Byte> m) {
        return blend(shiftL(s), m);
    }

    @Override
    @ForceInline
    public Byte512Vector shiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Byte512Vector.class, byte.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (byte) (a >>> (i & 7))));
    }

    @Override
    @ForceInline
    public Byte512Vector shiftR(int s, Mask<Byte> m) {
        return blend(shiftR(s), m);
    }

    @Override
    @ForceInline
    public Byte512Vector aShiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Byte512Vector.class, byte.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (byte) (a >> (i & 7))));
    }

    @Override
    @ForceInline
    public Byte512Vector aShiftR(int s, Mask<Byte> m) {
        return blend(aShiftR(s), m);
    }
    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public byte addAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Byte512Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) 0, (i, a, b) -> (byte) (a + b)));
    }

    @Override
    @ForceInline
    public byte andAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Byte512Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) -1, (i, a, b) -> (byte) (a & b)));
    }

    @Override
    @ForceInline
    public byte andAll(Mask<Byte> m) {
        return blend(SPECIES.broadcast((byte) -1), m).andAll();
    }

    @Override
    @ForceInline
    public byte minAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MIN, Byte512Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp(Byte.MAX_VALUE , (i, a, b) -> (byte) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public byte maxAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MAX, Byte512Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp(Byte.MIN_VALUE , (i, a, b) -> (byte) Math.max(a, b)));
    }

    @Override
    @ForceInline
    public byte mulAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Byte512Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) 1, (i, a, b) -> (byte) (a * b)));
    }

    @Override
    @ForceInline
    public byte subAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_SUB, Byte512Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) 0, (i, a, b) -> (byte) (a - b)));
    }

    @Override
    @ForceInline
    public byte orAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Byte512Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) 0, (i, a, b) -> (byte) (a | b)));
    }

    @Override
    @ForceInline
    public byte orAll(Mask<Byte> m) {
        return blend(SPECIES.broadcast((byte) 0), m).orAll();
    }

    @Override
    @ForceInline
    public byte xorAll() {
        return (byte) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Byte512Vector.class, byte.class, LENGTH,
            this,
            v -> (long) v.rOp((byte) 0, (i, a, b) -> (byte) (a ^ b)));
    }

    @Override
    @ForceInline
    public byte xorAll(Mask<Byte> m) {
        return blend(SPECIES.broadcast((byte) 0), m).xorAll();
    }


    @Override
    @ForceInline
    public byte addAll(Mask<Byte> m) {
        return blend(SPECIES.broadcast((byte) 0), m).addAll();
    }

    @Override
    @ForceInline
    public byte subAll(Mask<Byte> m) {
        return blend(SPECIES.broadcast((byte) 0), m).subAll();
    }

    @Override
    @ForceInline
    public byte mulAll(Mask<Byte> m) {
        return blend(SPECIES.broadcast((byte) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public byte minAll(Mask<Byte> m) {
        return blend(SPECIES.broadcast(Byte.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public byte maxAll(Mask<Byte> m) {
        return blend(SPECIES.broadcast(Byte.MIN_VALUE), m).maxAll();
    }

    @Override
    @ForceInline
    public Shuffle<Byte> toShuffle() {
        byte[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return SPECIES.shuffleFromArray(sa, 0);
    }

    // Memory operations

    private static final int ARRAY_SHIFT         = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_BYTE_INDEX_SCALE);
    private static final int BOOLEAN_ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_BOOLEAN_INDEX_SCALE);

    @Override
    @ForceInline
    public void intoArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Byte512Vector.class, byte.class, LENGTH,
                               a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                               this,
                               a, ix,
                               (arr, idx, v) -> v.forEach((i, e) -> arr[idx + i] = e));
    }

    @Override
    @ForceInline
    public final void intoArray(byte[] a, int ax, Mask<Byte> m) {
        Byte512Vector oldVal = SPECIES.fromArray(a, ax);
        Byte512Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Byte512Vector.class, byte.class, LENGTH,
                               a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                               this,
                               a, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                   ByteBuffer tb = bbc;
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public final void intoByteArray(byte[] a, int ix, Mask<Byte> m) {
        Byte512Vector oldVal = SPECIES.fromByteArray(a, ix);
        Byte512Vector newVal = oldVal.blend(this, m);
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
        VectorIntrinsics.store(Byte512Vector.class, byte.class, LENGTH,
                               U.getReference(bb, BYTE_BUFFER_HB), ix + U.getLong(bb, BUFFER_ADDRESS),
                               this,
                               bb, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                   ByteBuffer tb = bbc;
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Byte> m) {
        Byte512Vector oldVal = SPECIES.fromByteBuffer(bb, ix);
        Byte512Vector newVal = oldVal.blend(this, m);
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

        Byte512Vector that = (Byte512Vector) o;
        return this.equal(that).allTrue();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Byte512Mask bTest(Vector<Byte> o, FBinTest f) {
        byte[] vec1 = getElements();
        byte[] vec2 = ((Byte512Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Byte512Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Byte512Mask equal(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return VectorIntrinsics.compare(
            BT_eq, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Byte512Mask notEqual(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return VectorIntrinsics.compare(
            BT_ne, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Byte512Mask lessThan(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return VectorIntrinsics.compare(
            BT_lt, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Byte512Mask lessThanEq(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return VectorIntrinsics.compare(
            BT_le, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Byte512Mask greaterThan(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return (Byte512Mask) VectorIntrinsics.compare(
            BT_gt, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Byte512Mask greaterThanEq(Vector<Byte> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return VectorIntrinsics.compare(
            BT_ge, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a >= b));
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        byte[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Byte> o, FUnCon f) {
        boolean[] mbits = ((Byte512Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Byte512Vector rotateEL(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Byte512Vector(res);
    }

    @Override
    public Byte512Vector rotateER(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Byte512Vector(res);
    }

    @Override
    public Byte512Vector shiftEL(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Byte512Vector(res);
    }

    @Override
    public Byte512Vector shiftER(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Byte512Vector(res);
    }

    @Override
    @ForceInline
    public Byte512Vector rearrange(Vector<Byte> v,
                                  Shuffle<Byte> s, Mask<Byte> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    @ForceInline
    public Byte512Vector rearrange(Shuffle<Byte> o1) {
        Objects.requireNonNull(o1);
        Byte512Shuffle s =  (Byte512Shuffle)o1;

        return VectorIntrinsics.rearrangeOp(
            Byte512Vector.class, Byte512Shuffle.class, byte.class, LENGTH,
            this, s,
            (v1, s_) -> v1.uOp((i, a) -> {
                int ei = s_.getElement(i);
                return v1.get(ei);
            }));
    }

    @Override
    @ForceInline
    public Byte512Vector blend(Vector<Byte> o1, Mask<Byte> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Byte512Vector v = (Byte512Vector)o1;
        Byte512Mask   m = (Byte512Mask)o2;

        return VectorIntrinsics.blend(
            Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public byte get(int i) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return (byte) VectorIntrinsics.extract(
                                Byte512Vector.class, byte.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    byte[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Byte512Vector with(int i, byte e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Byte512Vector.class, byte.class, LENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    byte[] res = v.getElements().clone();
                                    res[ix] = (byte)bits;
                                    return new Byte512Vector(res);
                                });
    }

    // Mask

    static final class Byte512Mask extends AbstractMask<Byte> {
        static final Byte512Mask TRUE_MASK = new Byte512Mask(true);
        static final Byte512Mask FALSE_MASK = new Byte512Mask(false);

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Byte512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Byte512Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Byte512Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Byte512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Byte512Mask(res);
        }

        @Override
        Byte512Mask bOp(Mask<Byte> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Byte512Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Byte512Mask(res);
        }

        @Override
        public Byte512Species species() {
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
        public Byte512Vector toVector() {
            byte[] res = new byte[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (byte) (bits[i] ? -1 : 0);
            }
            return new Byte512Vector(res);
        }

        // Unary operations

        @Override
        @ForceInline
        public Byte512Mask not() {
            return (Byte512Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Byte512Mask.class, byte.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Byte512Mask and(Mask<Byte> o) {
            Objects.requireNonNull(o);
            Byte512Mask m = (Byte512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Byte512Mask.class, byte.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Byte512Mask or(Mask<Byte> o) {
            Objects.requireNonNull(o);
            Byte512Mask m = (Byte512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Byte512Mask.class, byte.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Byte512Mask.class, byte.class, LENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(m.getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Byte512Mask.class, byte.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m, __) -> allTrueHelper(m.getBits()));
        }
    }

    // Shuffle

    static final class Byte512Shuffle extends AbstractShuffle<Byte> {
        Byte512Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Byte512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Byte512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Byte512Shuffle(IntUnaryOperator f) {
            super(f);
        }

        @Override
        public Byte512Species species() {
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
        public Byte512Vector toVector() {
            byte[] va = new byte[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (byte) getElement(i);
            }
            return species().fromArray(va, 0);
        }

        @Override
        public Byte512Shuffle rearrange(Vector.Shuffle<Byte> o) {
            Byte512Shuffle s = (Byte512Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Byte512Shuffle(r);
        }
    }

    // Species

    @Override
    public Byte512Species species() {
        return SPECIES;
    }

    static final class Byte512Species extends ByteSpecies {
        static final int BIT_SIZE = Shape.S_512_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Byte.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(byte.class.getSimpleName()).append("s x ");
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
        public Class<Byte> elementType() {
            return byte.class;
        }

        @Override
        @ForceInline
        public int elementSize() {
            return Byte.SIZE;
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        Class<?> vectorType() {
            return Byte512Vector.class;
        }

        @Override
        @ForceInline
        public Shape shape() {
            return Shape.S_512_BIT;
        }

        @Override
        Byte512Vector op(FOp f) {
            byte[] res = new byte[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Byte512Vector(res);
        }

        @Override
        Byte512Vector op(Mask<Byte> o, FOp f) {
            byte[] res = new byte[length()];
            boolean[] mbits = ((Byte512Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Byte512Vector(res);
        }

        @Override
        Byte512Mask opm(FOpm f) {
            boolean[] res = new boolean[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = (boolean)f.apply(i);
            }
            return new Byte512Mask(res);
        }

        // Factories

        @Override
        public Byte512Mask maskFromValues(boolean... bits) {
            return new Byte512Mask(bits);
        }

        @Override
        public Byte512Shuffle shuffle(IntUnaryOperator f) {
            return new Byte512Shuffle(f);
        }

        @Override
        public Byte512Shuffle shuffleIota() {
            return new Byte512Shuffle(AbstractShuffle.IDENTITY);
        }

        @Override
        public Byte512Shuffle shuffleFromValues(int... ixs) {
            return new Byte512Shuffle(ixs);
        }

        @Override
        public Byte512Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Byte512Shuffle(ixs, i);
        }

        @Override
        @ForceInline
        public Byte512Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Byte512Vector.class, byte.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Byte512Vector broadcast(byte e) {
            return VectorIntrinsics.broadcastCoerced(
                Byte512Vector.class, byte.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (byte)bits)));
        }

        @Override
        @ForceInline
        public Byte512Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Byte512Mask.class, byte.class, LENGTH,
                                                     (byte)-1,
                                                     (z -> Byte512Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Byte512Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Byte512Mask.class, byte.class, LENGTH,
                                                     0,
                                                     (z -> Byte512Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Byte512Vector scalars(byte... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return VectorIntrinsics.load(Byte512Vector.class, byte.class, LENGTH,
                                         es, Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         es, ix,
                                         (c, idx) -> SPECIES.op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Byte512Mask maskFromArray(boolean[] bits, int ix) {
            Objects.requireNonNull(bits);
            ix = VectorIntrinsics.checkIndex(ix, bits.length, LENGTH);
            return VectorIntrinsics.load(Byte512Mask.class, byte.class, LENGTH,
                                         bits, (((long)ix) << BOOLEAN_ARRAY_SHIFT)+ Unsafe.ARRAY_BOOLEAN_BASE_OFFSET,
                                         bits, ix,
                                         (c, idx) -> SPECIES.opm(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Byte512Vector fromArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return VectorIntrinsics.load(Byte512Vector.class, byte.class, LENGTH,
                                         a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> SPECIES.op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public Byte512Vector fromArray(byte[] a, int ax, Mask<Byte> m) {
            return zero().blend(fromArray(a, ax), m);
        }

        @Override
        @ForceInline
        public Byte512Vector fromByteArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Byte512Vector.class, byte.class, LENGTH,
                                         a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                         a, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                             ByteBuffer tb = bbc;
                                             return SPECIES.op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Byte512Vector fromByteArray(byte[] a, int ix, Mask<Byte> m) {
            return zero().blend(fromByteArray(a, ix), m);
        }

        @Override
        @ForceInline
        public Byte512Vector fromByteBuffer(ByteBuffer bb, int ix) {
            if (bb.order() != ByteOrder.nativeOrder()) {
                throw new IllegalArgumentException();
            }
            ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
            return VectorIntrinsics.load(Byte512Vector.class, byte.class, LENGTH,
                                         U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + ix,
                                         bb, ix,
                                         (c, idx) -> {
                                             ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                             ByteBuffer tb = bbc;
                                             return SPECIES.op(i -> tb.get());
                                         });
        }

        @Override
        @ForceInline
        public Byte512Vector fromByteBuffer(ByteBuffer bb, int ix, Mask<Byte> m) {
            return zero().blend(fromByteBuffer(bb, ix), m);
        }
    }
}
