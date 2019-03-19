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
import java.nio.IntBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Int128Vector extends IntVector {
    static final Int128Species SPECIES = new Int128Species();

    static final Int128Vector ZERO = new Int128Vector();

    static final int LENGTH = SPECIES.length();

    // Index vector species
    private static final IntVector.IntSpecies INDEX_SPEC;
    static {
        int bitSize = Vector.bitSizeForVectorLength(int.class, LENGTH);
        Vector.Shape shape = Shape.forBitSize(bitSize);
        INDEX_SPEC = (IntVector.IntSpecies) Species.of(int.class, shape);
    }
    private final int[] vec; // Don't access directly, use getElements() instead.

    private int[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Int128Vector() {
        vec = new int[SPECIES.length()];
    }

    Int128Vector(int[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Int128Vector uOp(FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Int128Vector(res);
    }

    @Override
    Int128Vector uOp(Mask<Integer> o, FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        boolean[] mbits = ((Int128Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Int128Vector(res);
    }

    // Binary operator

    @Override
    Int128Vector bOp(Vector<Integer> o, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int128Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int128Vector(res);
    }

    @Override
    Int128Vector bOp(Vector<Integer> o1, Mask<Integer> o2, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int128Vector)o1).getElements();
        boolean[] mbits = ((Int128Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Int128Vector(res);
    }

    // Trinary operator

    @Override
    Int128Vector tOp(Vector<Integer> o1, Vector<Integer> o2, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int128Vector)o1).getElements();
        int[] vec3 = ((Int128Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Int128Vector(res);
    }

    @Override
    Int128Vector tOp(Vector<Integer> o1, Vector<Integer> o2, Mask<Integer> o3, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = getElements();
        int[] vec2 = ((Int128Vector)o1).getElements();
        int[] vec3 = ((Int128Vector)o2).getElements();
        boolean[] mbits = ((Int128Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Int128Vector(res);
    }

    @Override
    int rOp(int v, FBinOp f) {
        int[] vec = getElements();
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
            Int128Vector.class,
            int.class, LENGTH,
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
            return (Vector) ByteVector.fromArray((ByteVector.ByteSpecies) s, a, 0);
        } else if (stype == short.class) {
            short[] a = new short[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (short) this.get(i);
            }
            return (Vector) ShortVector.fromArray((ShortVector.ShortSpecies) s, a, 0);
        } else if (stype == int.class) {
            int[] a = new int[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (int) this.get(i);
            }
            return (Vector) IntVector.fromArray((IntVector.IntSpecies) s, a, 0);
        } else if (stype == long.class) {
            long[] a = new long[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (long) this.get(i);
            }
            return (Vector) LongVector.fromArray((LongVector.LongSpecies) s, a, 0);
        } else if (stype == float.class) {
            float[] a = new float[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (float) this.get(i);
            }
            return (Vector) FloatVector.fromArray((FloatVector.FloatSpecies) s, a, 0);
        } else if (stype == double.class) {
            double[] a = new double[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (double) this.get(i);
            }
            return (Vector) DoubleVector.fromArray((DoubleVector.DoubleSpecies) s, a, 0);
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }
    }

    @Override
    @ForceInline
    @SuppressWarnings("unchecked")
    public <F> Vector<F> reinterpret(Species<F> s) {
        Objects.requireNonNull(s);

        if(s.elementType().equals(int.class)) {
            return (Vector<F>) reshape((Species<Integer>)s);
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
                Int128Vector.class,
                int.class, LENGTH,
                Byte128Vector.class,
                byte.class, Byte128Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == short.class) {
            return VectorIntrinsics.reinterpret(
                Int128Vector.class,
                int.class, LENGTH,
                Short128Vector.class,
                short.class, Short128Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == int.class) {
            return VectorIntrinsics.reinterpret(
                Int128Vector.class,
                int.class, LENGTH,
                Int128Vector.class,
                int.class, Int128Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == long.class) {
            return VectorIntrinsics.reinterpret(
                Int128Vector.class,
                int.class, LENGTH,
                Long128Vector.class,
                long.class, Long128Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == float.class) {
            return VectorIntrinsics.reinterpret(
                Int128Vector.class,
                int.class, LENGTH,
                Float128Vector.class,
                float.class, Float128Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == double.class) {
            return VectorIntrinsics.reinterpret(
                Int128Vector.class,
                int.class, LENGTH,
                Double128Vector.class,
                double.class, Double128Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }
    }

    @Override
    @ForceInline
    public IntVector reshape(Species<Integer> s) {
        Objects.requireNonNull(s);
        if (s.bitSize() == 64 && (s instanceof Int64Vector.Int64Species)) {
            Int64Vector.Int64Species ts = (Int64Vector.Int64Species)s;
            return VectorIntrinsics.reinterpret(
                Int128Vector.class,
                int.class, LENGTH,
                Int64Vector.class,
                int.class, Int64Vector.LENGTH,
                this, ts,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 128 && (s instanceof Int128Vector.Int128Species)) {
            Int128Vector.Int128Species ts = (Int128Vector.Int128Species)s;
            return VectorIntrinsics.reinterpret(
                Int128Vector.class,
                int.class, LENGTH,
                Int128Vector.class,
                int.class, Int128Vector.LENGTH,
                this, ts,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 256 && (s instanceof Int256Vector.Int256Species)) {
            Int256Vector.Int256Species ts = (Int256Vector.Int256Species)s;
            return VectorIntrinsics.reinterpret(
                Int128Vector.class,
                int.class, LENGTH,
                Int256Vector.class,
                int.class, Int256Vector.LENGTH,
                this, ts,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 512 && (s instanceof Int512Vector.Int512Species)) {
            Int512Vector.Int512Species ts = (Int512Vector.Int512Species)s;
            return VectorIntrinsics.reinterpret(
                Int128Vector.class,
                int.class, LENGTH,
                Int512Vector.class,
                int.class, Int512Vector.LENGTH,
                this, ts,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if ((s.bitSize() > 0) && (s.bitSize() <= 2048)
                && (s.bitSize() % 128 == 0) && (s instanceof IntMaxVector.IntMaxSpecies)) {
            IntMaxVector.IntMaxSpecies ts = (IntMaxVector.IntMaxSpecies)s;
            return VectorIntrinsics.reinterpret(
                Int128Vector.class,
                int.class, LENGTH,
                IntMaxVector.class,
                int.class, IntMaxVector.LENGTH,
                this, ts,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else {
            throw new InternalError("Unimplemented size");
        }
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public IntVector add(int o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector add(int o, Mask<Integer> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector sub(int o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector sub(int o, Mask<Integer> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector mul(int o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector mul(int o, Mask<Integer> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector min(int o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector max(int o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer> equal(int o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer> notEqual(int o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer> lessThan(int o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer> lessThanEq(int o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer> greaterThan(int o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer> greaterThanEq(int o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector blend(int o, Mask<Integer> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public IntVector and(int o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector and(int o, Mask<Integer> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector or(int o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector or(int o, Mask<Integer> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector xor(int o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector xor(int o, Mask<Integer> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public Int128Vector neg() {
        return (Int128Vector)zero(SPECIES).sub(this);
    }

    // Unary operations

    @ForceInline
    @Override
    public Int128Vector neg(Mask<Integer> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public Int128Vector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Int128Vector.class, int.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (int) Math.abs(a)));
    }

    @ForceInline
    @Override
    public Int128Vector abs(Mask<Integer> m) {
        return blend(abs(), m);
    }


    @Override
    @ForceInline
    public Int128Vector not() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Int128Vector.class, int.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (int) ~a));
    }

    @ForceInline
    @Override
    public Int128Vector not(Mask<Integer> m) {
        return blend(not(), m);
    }
    // Binary operations

    @Override
    @ForceInline
    public Int128Vector add(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Int128Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a + b)));
    }

    @Override
    @ForceInline
    public Int128Vector add(Vector<Integer> v, Mask<Integer> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Int128Vector sub(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Int128Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a - b)));
    }

    @Override
    @ForceInline
    public Int128Vector sub(Vector<Integer> v, Mask<Integer> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Int128Vector mul(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Int128Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a * b)));
    }

    @Override
    @ForceInline
    public Int128Vector mul(Vector<Integer> v, Mask<Integer> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Int128Vector min(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;
        return (Int128Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Int128Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public Int128Vector min(Vector<Integer> v, Mask<Integer> m) {
        return blend(min(v), m);
    }

    @Override
    @ForceInline
    public Int128Vector max(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Int128Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int) Math.max(a, b)));
        }

    @Override
    @ForceInline
    public Int128Vector max(Vector<Integer> v, Mask<Integer> m) {
        return blend(max(v), m);
    }

    @Override
    @ForceInline
    public Int128Vector and(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Int128Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a & b)));
    }

    @Override
    @ForceInline
    public Int128Vector or(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Int128Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a | b)));
    }

    @Override
    @ForceInline
    public Int128Vector xor(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Int128Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a ^ b)));
    }

    @Override
    @ForceInline
    public Int128Vector and(Vector<Integer> v, Mask<Integer> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Int128Vector or(Vector<Integer> v, Mask<Integer> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Int128Vector xor(Vector<Integer> v, Mask<Integer> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Int128Vector shiftL(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Int128Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a << i)));
    }

    @Override
    @ForceInline
    public Int128Vector shiftL(int s, Mask<Integer> m) {
        return blend(shiftL(s), m);
    }

    @Override
    @ForceInline
    public Int128Vector shiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Int128Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a >>> i)));
    }

    @Override
    @ForceInline
    public Int128Vector shiftR(int s, Mask<Integer> m) {
        return blend(shiftR(s), m);
    }

    @Override
    @ForceInline
    public Int128Vector aShiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Int128Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a >> i)));
    }

    @Override
    @ForceInline
    public Int128Vector aShiftR(int s, Mask<Integer> m) {
        return blend(aShiftR(s), m);
    }

    @Override
    @ForceInline
    public Int128Vector shiftL(Vector<Integer> s) {
        Int128Vector shiftv = (Int128Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(species().broadcast(0x1f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_LSHIFT, Int128Vector.class, int.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (int) (a << b)));
    }

    @Override
    @ForceInline
    public Int128Vector shiftR(Vector<Integer> s) {
        Int128Vector shiftv = (Int128Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(species().broadcast(0x1f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_URSHIFT, Int128Vector.class, int.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (int) (a >>> b)));
    }

    @Override
    @ForceInline
    public Int128Vector aShiftR(Vector<Integer> s) {
        Int128Vector shiftv = (Int128Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(species().broadcast(0x1f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_RSHIFT, Int128Vector.class, int.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (int) (a >> b)));
    }
    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public int addAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Int128Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a + b)));
    }

    @Override
    @ForceInline
    public int andAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Int128Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) -1, (i, a, b) -> (int) (a & b)));
    }

    @Override
    @ForceInline
    public int andAll(Mask<Integer> m) {
        return blend(SPECIES.broadcast((int) -1), m).andAll();
    }

    @Override
    @ForceInline
    public int minAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MIN, Int128Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp(Integer.MAX_VALUE , (i, a, b) -> (int) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public int maxAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MAX, Int128Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp(Integer.MIN_VALUE , (i, a, b) -> (int) Math.max(a, b)));
    }

    @Override
    @ForceInline
    public int mulAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Int128Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 1, (i, a, b) -> (int) (a * b)));
    }

    @Override
    @ForceInline
    public int orAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Int128Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a | b)));
    }

    @Override
    @ForceInline
    public int orAll(Mask<Integer> m) {
        return blend(SPECIES.broadcast((int) 0), m).orAll();
    }

    @Override
    @ForceInline
    public int xorAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Int128Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a ^ b)));
    }

    @Override
    @ForceInline
    public int xorAll(Mask<Integer> m) {
        return blend(SPECIES.broadcast((int) 0), m).xorAll();
    }


    @Override
    @ForceInline
    public int addAll(Mask<Integer> m) {
        return blend(SPECIES.broadcast((int) 0), m).addAll();
    }

    @Override
    @ForceInline
    public int mulAll(Mask<Integer> m) {
        return blend(SPECIES.broadcast((int) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public int minAll(Mask<Integer> m) {
        return blend(SPECIES.broadcast(Integer.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public int maxAll(Mask<Integer> m) {
        return blend(SPECIES.broadcast(Integer.MIN_VALUE), m).maxAll();
    }

    @Override
    @ForceInline
    public Shuffle<Integer> toShuffle() {
        int[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return IntVector.shuffleFromArray(SPECIES, sa, 0);
    }

    // Memory operations

    private static final int ARRAY_SHIFT         = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_INT_INDEX_SCALE);
    private static final int BOOLEAN_ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_BOOLEAN_INDEX_SCALE);

    @Override
    @ForceInline
    public void intoArray(int[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Int128Vector.class, int.class, LENGTH,
                               a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_INT_BASE_OFFSET,
                               this,
                               a, ix,
                               (arr, idx, v) -> v.forEach((i, e) -> arr[idx + i] = e));
    }

    @Override
    @ForceInline
    public final void intoArray(int[] a, int ax, Mask<Integer> m) {
        IntVector oldVal = IntVector.fromArray(SPECIES, a, ax);
        IntVector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }
    @Override
    @ForceInline
    public void intoArray(int[] a, int ix, int[] b, int iy) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        // Index vector: vix[0:n] = i -> ix + indexMap[iy + i]
        IntVector vix = IntVector.fromArray(INDEX_SPEC, b, iy).add(ix);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        VectorIntrinsics.storeWithMap(Int128Vector.class, int.class, LENGTH, Int128Vector.class,
                               a, Unsafe.ARRAY_INT_BASE_OFFSET, vix,
                               this,
                               a, ix, b, iy,
                               (arr, idx, v, indexMap, idy) -> v.forEach((i, e) -> arr[idx+indexMap[idy+i]] = e));
    }

     @Override
     @ForceInline
     public final void intoArray(int[] a, int ax, Mask<Integer> m, int[] b, int iy) {
         // @@@ This can result in out of bounds errors for unset mask lanes
         IntVector oldVal = IntVector.fromArray(SPECIES, a, ax, b, iy);
         IntVector newVal = oldVal.blend(this, m);
         newVal.intoArray(a, ax, b, iy);
     }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Int128Vector.class, int.class, LENGTH,
                               a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                               this,
                               a, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                   IntBuffer tb = bbc.asIntBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public final void intoByteArray(byte[] a, int ix, Mask<Integer> m) {
        Int128Vector oldVal = (Int128Vector) IntVector.fromByteArray(SPECIES, a, ix);
        Int128Vector newVal = oldVal.blend(this, m);
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
        VectorIntrinsics.store(Int128Vector.class, int.class, LENGTH,
                               U.getReference(bb, BYTE_BUFFER_HB), ix + U.getLong(bb, BUFFER_ADDRESS),
                               this,
                               bb, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                   IntBuffer tb = bbc.asIntBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Integer> m) {
        Int128Vector oldVal = (Int128Vector) IntVector.fromByteBuffer(SPECIES, bb, ix);
        Int128Vector newVal = oldVal.blend(this, m);
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

        Int128Vector that = (Int128Vector) o;
        return this.equal(that).allTrue();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Int128Mask bTest(Vector<Integer> o, FBinTest f) {
        int[] vec1 = getElements();
        int[] vec2 = ((Int128Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int128Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Int128Mask equal(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;

        return VectorIntrinsics.compare(
            BT_eq, Int128Vector.class, Int128Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Int128Mask notEqual(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;

        return VectorIntrinsics.compare(
            BT_ne, Int128Vector.class, Int128Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Int128Mask lessThan(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;

        return VectorIntrinsics.compare(
            BT_lt, Int128Vector.class, Int128Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Int128Mask lessThanEq(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;

        return VectorIntrinsics.compare(
            BT_le, Int128Vector.class, Int128Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Int128Mask greaterThan(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;

        return (Int128Mask) VectorIntrinsics.compare(
            BT_gt, Int128Vector.class, Int128Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Int128Mask greaterThanEq(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int128Vector v = (Int128Vector)o;

        return VectorIntrinsics.compare(
            BT_ge, Int128Vector.class, Int128Mask.class, int.class, LENGTH,
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
    void forEach(Mask<Integer> o, FUnCon f) {
        boolean[] mbits = ((Int128Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }


    Float128Vector toFP() {
        int[] vec = getElements();
        float[] res = new float[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.intBitsToFloat(vec[i]);
        }
        return new Float128Vector(res);
    }

    @Override
    public Int128Vector rotateEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Int128Vector(res);
    }

    @Override
    public Int128Vector rotateER(int j) {
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
        return new Int128Vector(res);
    }

    @Override
    public Int128Vector shiftEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Int128Vector(res);
    }

    @Override
    public Int128Vector shiftER(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Int128Vector(res);
    }

    @Override
    @ForceInline
    public Int128Vector rearrange(Vector<Integer> v,
                                  Shuffle<Integer> s, Mask<Integer> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    @ForceInline
    public Int128Vector rearrange(Shuffle<Integer> o1) {
        Objects.requireNonNull(o1);
        Int128Shuffle s =  (Int128Shuffle)o1;

        return VectorIntrinsics.rearrangeOp(
            Int128Vector.class, Int128Shuffle.class, int.class, LENGTH,
            this, s,
            (v1, s_) -> v1.uOp((i, a) -> {
                int ei = s_.getElement(i);
                return v1.get(ei);
            }));
    }

    @Override
    @ForceInline
    public Int128Vector blend(Vector<Integer> o1, Mask<Integer> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Int128Vector v = (Int128Vector)o1;
        Int128Mask   m = (Int128Mask)o2;

        return VectorIntrinsics.blend(
            Int128Vector.class, Int128Mask.class, int.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public int get(int i) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return (int) VectorIntrinsics.extract(
                                Int128Vector.class, int.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    int[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Int128Vector with(int i, int e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Int128Vector.class, int.class, LENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    int[] res = v.getElements().clone();
                                    res[ix] = (int)bits;
                                    return new Int128Vector(res);
                                });
    }

    // Mask

    static final class Int128Mask extends AbstractMask<Integer> {
        static final Int128Mask TRUE_MASK = new Int128Mask(true);
        static final Int128Mask FALSE_MASK = new Int128Mask(false);

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Int128Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Int128Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Int128Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Int128Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Int128Mask(res);
        }

        @Override
        Int128Mask bOp(Mask<Integer> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Int128Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Int128Mask(res);
        }

        @Override
        public Int128Species species() {
            return SPECIES;
        }

        @Override
        public Int128Vector toVector() {
            int[] res = new int[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (int) (bits[i] ? -1 : 0);
            }
            return new Int128Vector(res);
        }

        // Unary operations

        @Override
        @ForceInline
        public Int128Mask not() {
            return (Int128Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Int128Mask.class, int.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Int128Mask and(Mask<Integer> o) {
            Objects.requireNonNull(o);
            Int128Mask m = (Int128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Int128Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Int128Mask or(Mask<Integer> o) {
            Objects.requireNonNull(o);
            Int128Mask m = (Int128Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Int128Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Int128Mask.class, int.class, LENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Int128Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Int128Mask.class, int.class, LENGTH,
                                         this, IntVector.maskAllTrue(species()),
                                         (m, __) -> allTrueHelper(((Int128Mask)m).getBits()));
        }
    }

    // Shuffle

    static final class Int128Shuffle extends AbstractShuffle<Integer> {
        Int128Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Int128Shuffle(int[] reorder) {
            super(reorder);
        }

        public Int128Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Int128Shuffle(IntUnaryOperator f) {
            super(f);
        }

        @Override
        public Int128Species species() {
            return SPECIES;
        }

        @Override
        public IntVector toVector() {
            int[] va = new int[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (int) getElement(i);
            }
            return IntVector.fromArray(SPECIES, va, 0);
        }

        @Override
        public Int128Shuffle rearrange(Vector.Shuffle<Integer> o) {
            Int128Shuffle s = (Int128Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Int128Shuffle(r);
        }
    }

    // Species

    @Override
    public Int128Species species() {
        return SPECIES;
    }

    static final class Int128Species extends IntSpecies {
        static final int BIT_SIZE = Shape.S_128_BIT.bitSize();

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
        public Class<?> boxType() {
            return Int128Vector.class;
        }

        @Override
        @ForceInline
        public Class<?> maskType() {
            return Int128Mask.class;
        }

        @Override
        @ForceInline
        public int elementSize() {
            return Integer.SIZE;
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        Class<?> vectorType() {
            return Int128Vector.class;
        }

        @Override
        @ForceInline
        public Shape shape() {
            return Shape.S_128_BIT;
        }

       @Override
       IntVector.IntSpecies indexSpecies() {
          return INDEX_SPEC;
       }

        @Override
        Int128Vector op(FOp f) {
            int[] res = new int[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Int128Vector(res);
        }

        @Override
        Int128Vector op(Mask<Integer> o, FOp f) {
            int[] res = new int[length()];
            boolean[] mbits = ((Int128Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Int128Vector(res);
        }

        @Override
        Int128Mask opm(FOpm f) {
            boolean[] res = new boolean[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = (boolean)f.apply(i);
            }
            return new Int128Mask(res);
        }

        // Factories

        @Override
        @ForceInline
        public Int128Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Int128Vector.class, int.class, LENGTH,
                                                     0, SPECIES,
                                                     ((bits, s) -> ((Int128Species)s).op(i -> (int)bits)));
        }

        @Override
        @ForceInline
        public Int128Vector broadcast(int e) {
            return VectorIntrinsics.broadcastCoerced(
                Int128Vector.class, int.class, LENGTH,
                e, SPECIES,
                ((bits, s) -> ((Int128Species)s).op(i -> (int)bits)));
        }

        @Override
        @ForceInline
        public Int128Vector scalars(int... es) {
            Objects.requireNonNull(es);
            int ix = VectorIntrinsics.checkIndex(0, es.length, LENGTH);
            return VectorIntrinsics.load(Int128Vector.class, int.class, LENGTH,
                                         es, Unsafe.ARRAY_INT_BASE_OFFSET,
                                         es, ix, SPECIES,
                                         (c, idx, s) -> ((Int128Species)s).op(n -> c[idx + n]));
        }

        @Override
        @ForceInline
        public <E> Int128Mask cast(Mask<E> m) {
            if (m.length() != LENGTH)
                throw new IllegalArgumentException("Mask length this species length differ");
            return new Int128Mask(m.toArray());
        }

        @Override
        @ForceInline
        public <E> Int128Shuffle cast(Shuffle<E> s) {
            if (s.length() != LENGTH)
                throw new IllegalArgumentException("Shuffle length this species length differ");
            return new Int128Shuffle(s.toArray());
        }
    }
}
