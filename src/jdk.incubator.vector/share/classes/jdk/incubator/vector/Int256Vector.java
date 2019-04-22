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
final class Int256Vector extends IntVector {
    private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_256;

    static final Int256Vector ZERO = new Int256Vector();

    static final int LENGTH = SPECIES.length();

    // Index vector species
    private static final IntVector.IntSpecies INDEX_SPECIES;

    static {
        int bitSize = Vector.bitSizeForVectorLength(int.class, LENGTH);
        INDEX_SPECIES = (IntVector.IntSpecies) IntVector.species(VectorShape.forBitSize(bitSize));
    }

    private final int[] vec; // Don't access directly, use getElements() instead.

    private int[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Int256Vector() {
        vec = new int[SPECIES.length()];
    }

    Int256Vector(int[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Int256Vector uOp(FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Int256Vector(res);
    }

    @Override
    Int256Vector uOp(VectorMask<Integer> o, FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        boolean[] mbits = ((Int256Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Int256Vector(res);
    }

    // Binary operator

    @Override
    Int256Vector bOp(Vector<Integer> o, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int256Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int256Vector(res);
    }

    @Override
    Int256Vector bOp(Vector<Integer> o1, VectorMask<Integer> o2, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int256Vector)o1).getElements();
        boolean[] mbits = ((Int256Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Int256Vector(res);
    }

    // Trinary operator

    @Override
    Int256Vector tOp(Vector<Integer> o1, Vector<Integer> o2, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int256Vector)o1).getElements();
        int[] vec3 = ((Int256Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Int256Vector(res);
    }

    @Override
    Int256Vector tOp(Vector<Integer> o1, Vector<Integer> o2, VectorMask<Integer> o3, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = getElements();
        int[] vec2 = ((Int256Vector)o1).getElements();
        int[] vec3 = ((Int256Vector)o2).getElements();
        boolean[] mbits = ((Int256Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Int256Vector(res);
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
    public <F> Vector<F> cast(VectorSpecies<F> s) {
        Objects.requireNonNull(s);
        if (s.length() != LENGTH)
            throw new IllegalArgumentException("Vector length this species length differ");

        return VectorIntrinsics.cast(
            Int256Vector.class,
            int.class, LENGTH,
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
                a[i] = (byte) this.lane(i);
            }
            return (Vector) ByteVector.fromArray((VectorSpecies<Byte>) s, a, 0);
        } else if (stype == short.class) {
            short[] a = new short[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (short) this.lane(i);
            }
            return (Vector) ShortVector.fromArray((VectorSpecies<Short>) s, a, 0);
        } else if (stype == int.class) {
            int[] a = new int[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (int) this.lane(i);
            }
            return (Vector) IntVector.fromArray((VectorSpecies<Integer>) s, a, 0);
        } else if (stype == long.class) {
            long[] a = new long[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (long) this.lane(i);
            }
            return (Vector) LongVector.fromArray((VectorSpecies<Long>) s, a, 0);
        } else if (stype == float.class) {
            float[] a = new float[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (float) this.lane(i);
            }
            return (Vector) FloatVector.fromArray((VectorSpecies<Float>) s, a, 0);
        } else if (stype == double.class) {
            double[] a = new double[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (double) this.lane(i);
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

        if(s.elementType().equals(int.class)) {
            return (Vector<F>) reshape((VectorSpecies<Integer>)s);
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
                Int256Vector.class,
                int.class, LENGTH,
                Byte256Vector.class,
                byte.class, Byte256Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == short.class) {
            return VectorIntrinsics.reinterpret(
                Int256Vector.class,
                int.class, LENGTH,
                Short256Vector.class,
                short.class, Short256Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == int.class) {
            return VectorIntrinsics.reinterpret(
                Int256Vector.class,
                int.class, LENGTH,
                Int256Vector.class,
                int.class, Int256Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == long.class) {
            return VectorIntrinsics.reinterpret(
                Int256Vector.class,
                int.class, LENGTH,
                Long256Vector.class,
                long.class, Long256Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == float.class) {
            return VectorIntrinsics.reinterpret(
                Int256Vector.class,
                int.class, LENGTH,
                Float256Vector.class,
                float.class, Float256Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == double.class) {
            return VectorIntrinsics.reinterpret(
                Int256Vector.class,
                int.class, LENGTH,
                Double256Vector.class,
                double.class, Double256Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }
    }

    @Override
    @ForceInline
    public IntVector reshape(VectorSpecies<Integer> s) {
        Objects.requireNonNull(s);
        if (s.bitSize() == 64 && (s.boxType() == Int64Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Int256Vector.class,
                int.class, LENGTH,
                Int64Vector.class,
                int.class, Int64Vector.LENGTH,
                this, s,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 128 && (s.boxType() == Int128Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Int256Vector.class,
                int.class, LENGTH,
                Int128Vector.class,
                int.class, Int128Vector.LENGTH,
                this, s,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 256 && (s.boxType() == Int256Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Int256Vector.class,
                int.class, LENGTH,
                Int256Vector.class,
                int.class, Int256Vector.LENGTH,
                this, s,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 512 && (s.boxType() == Int512Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Int256Vector.class,
                int.class, LENGTH,
                Int512Vector.class,
                int.class, Int512Vector.LENGTH,
                this, s,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if ((s.bitSize() > 0) && (s.bitSize() <= 2048)
                && (s.bitSize() % 128 == 0) && (s.boxType() == IntMaxVector.class)) {
            return VectorIntrinsics.reinterpret(
                Int256Vector.class,
                int.class, LENGTH,
                IntMaxVector.class,
                int.class, IntMaxVector.LENGTH,
                this, s,
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
        return add((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector add(int o, VectorMask<Integer> m) {
        return add((Int256Vector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntVector sub(int o) {
        return sub((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector sub(int o, VectorMask<Integer> m) {
        return sub((Int256Vector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntVector mul(int o) {
        return mul((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector mul(int o, VectorMask<Integer> m) {
        return mul((Int256Vector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntVector min(int o) {
        return min((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector max(int o) {
        return max((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> equal(int o) {
        return equal((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> notEqual(int o) {
        return notEqual((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> lessThan(int o) {
        return lessThan((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> lessThanEq(int o) {
        return lessThanEq((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> greaterThan(int o) {
        return greaterThan((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> greaterThanEq(int o) {
        return greaterThanEq((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector blend(int o, VectorMask<Integer> m) {
        return blend((Int256Vector)IntVector.broadcast(SPECIES, o), m);
    }


    @Override
    @ForceInline
    public IntVector and(int o) {
        return and((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector and(int o, VectorMask<Integer> m) {
        return and((Int256Vector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntVector or(int o) {
        return or((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector or(int o, VectorMask<Integer> m) {
        return or((Int256Vector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntVector xor(int o) {
        return xor((Int256Vector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector xor(int o, VectorMask<Integer> m) {
        return xor((Int256Vector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public Int256Vector neg() {
        return (Int256Vector)zero(SPECIES).sub(this);
    }

    // Unary operations

    @ForceInline
    @Override
    public Int256Vector neg(VectorMask<Integer> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public Int256Vector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Int256Vector.class, int.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (int) Math.abs(a)));
    }

    @ForceInline
    @Override
    public Int256Vector abs(VectorMask<Integer> m) {
        return blend(abs(), m);
    }


    @Override
    @ForceInline
    public Int256Vector not() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Int256Vector.class, int.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (int) ~a));
    }

    @ForceInline
    @Override
    public Int256Vector not(VectorMask<Integer> m) {
        return blend(not(), m);
    }
    // Binary operations

    @Override
    @ForceInline
    public Int256Vector add(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a + b)));
    }

    @Override
    @ForceInline
    public Int256Vector add(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Int256Vector sub(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a - b)));
    }

    @Override
    @ForceInline
    public Int256Vector sub(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Int256Vector mul(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a * b)));
    }

    @Override
    @ForceInline
    public Int256Vector mul(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Int256Vector min(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return (Int256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public Int256Vector min(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(min(v), m);
    }

    @Override
    @ForceInline
    public Int256Vector max(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int) Math.max(a, b)));
        }

    @Override
    @ForceInline
    public Int256Vector max(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(max(v), m);
    }

    @Override
    @ForceInline
    public Int256Vector and(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a & b)));
    }

    @Override
    @ForceInline
    public Int256Vector or(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a | b)));
    }

    @Override
    @ForceInline
    public Int256Vector xor(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a ^ b)));
    }

    @Override
    @ForceInline
    public Int256Vector and(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Int256Vector or(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Int256Vector xor(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Int256Vector shiftL(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Int256Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a << i)));
    }

    @Override
    @ForceInline
    public Int256Vector shiftL(int s, VectorMask<Integer> m) {
        return blend(shiftL(s), m);
    }

    @Override
    @ForceInline
    public Int256Vector shiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Int256Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a >>> i)));
    }

    @Override
    @ForceInline
    public Int256Vector shiftR(int s, VectorMask<Integer> m) {
        return blend(shiftR(s), m);
    }

    @Override
    @ForceInline
    public Int256Vector aShiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Int256Vector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a >> i)));
    }

    @Override
    @ForceInline
    public Int256Vector aShiftR(int s, VectorMask<Integer> m) {
        return blend(aShiftR(s), m);
    }

    @Override
    @ForceInline
    public Int256Vector shiftL(Vector<Integer> s) {
        Int256Vector shiftv = (Int256Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(IntVector.broadcast(SPECIES, 0x1f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_LSHIFT, Int256Vector.class, int.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (int) (a << b)));
    }

    @Override
    @ForceInline
    public Int256Vector shiftR(Vector<Integer> s) {
        Int256Vector shiftv = (Int256Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(IntVector.broadcast(SPECIES, 0x1f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_URSHIFT, Int256Vector.class, int.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (int) (a >>> b)));
    }

    @Override
    @ForceInline
    public Int256Vector aShiftR(Vector<Integer> s) {
        Int256Vector shiftv = (Int256Vector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(IntVector.broadcast(SPECIES, 0x1f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_RSHIFT, Int256Vector.class, int.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (int) (a >> b)));
    }
    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public int addAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Int256Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a + b)));
    }

    @Override
    @ForceInline
    public int andAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Int256Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) -1, (i, a, b) -> (int) (a & b)));
    }

    @Override
    @ForceInline
    public int andAll(VectorMask<Integer> m) {
        return IntVector.broadcast(SPECIES, (int) -1).blend(this, m).andAll();
    }

    @Override
    @ForceInline
    public int minAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MIN, Int256Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp(Integer.MAX_VALUE , (i, a, b) -> (int) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public int maxAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MAX, Int256Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp(Integer.MIN_VALUE , (i, a, b) -> (int) Math.max(a, b)));
    }

    @Override
    @ForceInline
    public int mulAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Int256Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 1, (i, a, b) -> (int) (a * b)));
    }

    @Override
    @ForceInline
    public int orAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Int256Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a | b)));
    }

    @Override
    @ForceInline
    public int orAll(VectorMask<Integer> m) {
        return IntVector.broadcast(SPECIES, (int) 0).blend(this, m).orAll();
    }

    @Override
    @ForceInline
    public int xorAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Int256Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a ^ b)));
    }

    @Override
    @ForceInline
    public int xorAll(VectorMask<Integer> m) {
        return IntVector.broadcast(SPECIES, (int) 0).blend(this, m).xorAll();
    }


    @Override
    @ForceInline
    public int addAll(VectorMask<Integer> m) {
        return IntVector.broadcast(SPECIES, (int) 0).blend(this, m).addAll();
    }


    @Override
    @ForceInline
    public int mulAll(VectorMask<Integer> m) {
        return IntVector.broadcast(SPECIES, (int) 1).blend(this, m).mulAll();
    }

    @Override
    @ForceInline
    public int minAll(VectorMask<Integer> m) {
        return IntVector.broadcast(SPECIES, Integer.MAX_VALUE).blend(this, m).minAll();
    }

    @Override
    @ForceInline
    public int maxAll(VectorMask<Integer> m) {
        return IntVector.broadcast(SPECIES, Integer.MIN_VALUE).blend(this, m).maxAll();
    }

    @Override
    @ForceInline
    public VectorShuffle<Integer> toShuffle() {
        int[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return VectorShuffle.fromArray(SPECIES, sa, 0);
    }

    // Memory operations

    private static final int ARRAY_SHIFT         = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_INT_INDEX_SCALE);
    private static final int BOOLEAN_ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_BOOLEAN_INDEX_SCALE);

    @Override
    @ForceInline
    public void intoArray(int[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Int256Vector.class, int.class, LENGTH,
                               a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_INT_BASE_OFFSET,
                               this,
                               a, ix,
                               (arr, idx, v) -> v.forEach((i, e) -> arr[idx + i] = e));
    }

    @Override
    @ForceInline
    public final void intoArray(int[] a, int ax, VectorMask<Integer> m) {
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
        IntVector vix = IntVector.fromArray(INDEX_SPECIES, b, iy).add(ix);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        VectorIntrinsics.storeWithMap(Int256Vector.class, int.class, LENGTH, Int256Vector.class,
                               a, Unsafe.ARRAY_INT_BASE_OFFSET, vix,
                               this,
                               a, ix, b, iy,
                               (arr, idx, v, indexMap, idy) -> v.forEach((i, e) -> arr[idx+indexMap[idy+i]] = e));
    }

     @Override
     @ForceInline
     public final void intoArray(int[] a, int ax, VectorMask<Integer> m, int[] b, int iy) {
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
        VectorIntrinsics.store(Int256Vector.class, int.class, LENGTH,
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
    public final void intoByteArray(byte[] a, int ix, VectorMask<Integer> m) {
        Int256Vector oldVal = (Int256Vector) IntVector.fromByteArray(SPECIES, a, ix);
        Int256Vector newVal = oldVal.blend(this, m);
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
        VectorIntrinsics.store(Int256Vector.class, int.class, LENGTH,
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
    public void intoByteBuffer(ByteBuffer bb, int ix, VectorMask<Integer> m) {
        Int256Vector oldVal = (Int256Vector) IntVector.fromByteBuffer(SPECIES, bb, ix);
        Int256Vector newVal = oldVal.blend(this, m);
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

        Int256Vector that = (Int256Vector) o;
        return this.equal(that).allTrue();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Int256Mask bTest(Vector<Integer> o, FBinTest f) {
        int[] vec1 = getElements();
        int[] vec2 = ((Int256Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int256Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Int256Mask equal(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;

        return VectorIntrinsics.compare(
            BT_eq, Int256Vector.class, Int256Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Int256Mask notEqual(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;

        return VectorIntrinsics.compare(
            BT_ne, Int256Vector.class, Int256Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Int256Mask lessThan(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;

        return VectorIntrinsics.compare(
            BT_lt, Int256Vector.class, Int256Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Int256Mask lessThanEq(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;

        return VectorIntrinsics.compare(
            BT_le, Int256Vector.class, Int256Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Int256Mask greaterThan(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;

        return (Int256Mask) VectorIntrinsics.compare(
            BT_gt, Int256Vector.class, Int256Mask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Int256Mask greaterThanEq(Vector<Integer> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;

        return VectorIntrinsics.compare(
            BT_ge, Int256Vector.class, Int256Mask.class, int.class, LENGTH,
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
    void forEach(VectorMask<Integer> o, FUnCon f) {
        boolean[] mbits = ((Int256Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }


    Float256Vector toFP() {
        int[] vec = getElements();
        float[] res = new float[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.intBitsToFloat(vec[i]);
        }
        return new Float256Vector(res);
    }

    @Override
    public Int256Vector rotateEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Int256Vector(res);
    }

    @Override
    public Int256Vector rotateER(int j) {
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
        return new Int256Vector(res);
    }

    @Override
    public Int256Vector shiftEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Int256Vector(res);
    }

    @Override
    public Int256Vector shiftER(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Int256Vector(res);
    }

    @Override
    @ForceInline
    public Int256Vector rearrange(Vector<Integer> v,
                                  VectorShuffle<Integer> s, VectorMask<Integer> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    @ForceInline
    public Int256Vector rearrange(VectorShuffle<Integer> o1) {
        Objects.requireNonNull(o1);
        Int256Shuffle s =  (Int256Shuffle)o1;

        return VectorIntrinsics.rearrangeOp(
            Int256Vector.class, Int256Shuffle.class, int.class, LENGTH,
            this, s,
            (v1, s_) -> v1.uOp((i, a) -> {
                int ei = s_.lane(i);
                return v1.lane(ei);
            }));
    }

    @Override
    @ForceInline
    public Int256Vector blend(Vector<Integer> o1, VectorMask<Integer> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Int256Vector v = (Int256Vector)o1;
        Int256Mask   m = (Int256Mask)o2;

        return VectorIntrinsics.blend(
            Int256Vector.class, Int256Mask.class, int.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.lane(i) ? b : a));
    }

    // Accessors

    @Override
    public int lane(int i) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return (int) VectorIntrinsics.extract(
                                Int256Vector.class, int.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    int[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Int256Vector with(int i, int e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Int256Vector.class, int.class, LENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    int[] res = v.getElements().clone();
                                    res[ix] = (int)bits;
                                    return new Int256Vector(res);
                                });
    }

    // Mask

    static final class Int256Mask extends AbstractMask<Integer> {
        static final Int256Mask TRUE_MASK = new Int256Mask(true);
        static final Int256Mask FALSE_MASK = new Int256Mask(false);

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Int256Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Int256Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Int256Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Int256Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Int256Mask(res);
        }

        @Override
        Int256Mask bOp(VectorMask<Integer> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Int256Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Int256Mask(res);
        }

        @Override
        public VectorSpecies<Integer> species() {
            return SPECIES;
        }

        @Override
        public Int256Vector toVector() {
            int[] res = new int[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (int) (bits[i] ? -1 : 0);
            }
            return new Int256Vector(res);
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
                return (VectorMask <E>) new Byte256Vector.Byte256Mask(maskArray);
            } else if (stype == short.class) {
                return (VectorMask <E>) new Short256Vector.Short256Mask(maskArray);
            } else if (stype == int.class) {
                return (VectorMask <E>) new Int256Vector.Int256Mask(maskArray);
            } else if (stype == long.class) {
                return (VectorMask <E>) new Long256Vector.Long256Mask(maskArray);
            } else if (stype == float.class) {
                return (VectorMask <E>) new Float256Vector.Float256Mask(maskArray);
            } else if (stype == double.class) {
                return (VectorMask <E>) new Double256Vector.Double256Mask(maskArray);
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }
        }

        // Unary operations

        @Override
        @ForceInline
        public Int256Mask not() {
            return (Int256Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Int256Mask.class, int.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Int256Mask and(VectorMask<Integer> o) {
            Objects.requireNonNull(o);
            Int256Mask m = (Int256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Int256Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Int256Mask or(VectorMask<Integer> o) {
            Objects.requireNonNull(o);
            Int256Mask m = (Int256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Int256Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Int256Mask.class, int.class, LENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Int256Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Int256Mask.class, int.class, LENGTH,
                                         this, VectorMask.maskAllTrue(species()),
                                         (m, __) -> allTrueHelper(((Int256Mask)m).getBits()));
        }
    }

    // Shuffle

    static final class Int256Shuffle extends AbstractShuffle<Integer> {
        Int256Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Int256Shuffle(int[] reorder) {
            super(reorder);
        }

        public Int256Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Int256Shuffle(IntUnaryOperator f) {
            super(f);
        }

        @Override
        public VectorSpecies<Integer> species() {
            return SPECIES;
        }

        @Override
        public IntVector toVector() {
            int[] va = new int[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (int) lane(i);
            }
            return IntVector.fromArray(SPECIES, va, 0);
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
                return (VectorShuffle<F>) new Byte256Vector.Byte256Shuffle(shuffleArray);
            } else if (stype == short.class) {
                return (VectorShuffle<F>) new Short256Vector.Short256Shuffle(shuffleArray);
            } else if (stype == int.class) {
                return (VectorShuffle<F>) new Int256Vector.Int256Shuffle(shuffleArray);
            } else if (stype == long.class) {
                return (VectorShuffle<F>) new Long256Vector.Long256Shuffle(shuffleArray);
            } else if (stype == float.class) {
                return (VectorShuffle<F>) new Float256Vector.Float256Shuffle(shuffleArray);
            } else if (stype == double.class) {
                return (VectorShuffle<F>) new Double256Vector.Double256Shuffle(shuffleArray);
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }
        }

        @Override
        public Int256Shuffle rearrange(VectorShuffle<Integer> o) {
            Int256Shuffle s = (Int256Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Int256Shuffle(r);
        }
    }

    // VectorSpecies

    @Override
    public VectorSpecies<Integer> species() {
        return SPECIES;
    }
}
