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
final class IntMaxVector extends IntVector {
    private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_MAX;

    static final IntMaxVector ZERO = new IntMaxVector();

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

    IntMaxVector() {
        vec = new int[SPECIES.length()];
    }

    IntMaxVector(int[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    IntMaxVector uOp(FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new IntMaxVector(res);
    }

    @Override
    IntMaxVector uOp(VectorMask<Integer> o, FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        boolean[] mbits = ((IntMaxMask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new IntMaxVector(res);
    }

    // Binary operator

    @Override
    IntMaxVector bOp(Vector<Integer> o, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((IntMaxVector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new IntMaxVector(res);
    }

    @Override
    IntMaxVector bOp(Vector<Integer> o1, VectorMask<Integer> o2, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((IntMaxVector)o1).getElements();
        boolean[] mbits = ((IntMaxMask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new IntMaxVector(res);
    }

    // Trinary operator

    @Override
    IntMaxVector tOp(Vector<Integer> o1, Vector<Integer> o2, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((IntMaxVector)o1).getElements();
        int[] vec3 = ((IntMaxVector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new IntMaxVector(res);
    }

    @Override
    IntMaxVector tOp(Vector<Integer> o1, Vector<Integer> o2, VectorMask<Integer> o3, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = getElements();
        int[] vec2 = ((IntMaxVector)o1).getElements();
        int[] vec3 = ((IntMaxVector)o2).getElements();
        boolean[] mbits = ((IntMaxMask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new IntMaxVector(res);
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
            IntMaxVector.class,
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
                IntMaxVector.class,
                int.class, LENGTH,
                ByteMaxVector.class,
                byte.class, ByteMaxVector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == short.class) {
            return VectorIntrinsics.reinterpret(
                IntMaxVector.class,
                int.class, LENGTH,
                ShortMaxVector.class,
                short.class, ShortMaxVector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == int.class) {
            return VectorIntrinsics.reinterpret(
                IntMaxVector.class,
                int.class, LENGTH,
                IntMaxVector.class,
                int.class, IntMaxVector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == long.class) {
            return VectorIntrinsics.reinterpret(
                IntMaxVector.class,
                int.class, LENGTH,
                LongMaxVector.class,
                long.class, LongMaxVector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == float.class) {
            return VectorIntrinsics.reinterpret(
                IntMaxVector.class,
                int.class, LENGTH,
                FloatMaxVector.class,
                float.class, FloatMaxVector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == double.class) {
            return VectorIntrinsics.reinterpret(
                IntMaxVector.class,
                int.class, LENGTH,
                DoubleMaxVector.class,
                double.class, DoubleMaxVector.LENGTH,
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
                IntMaxVector.class,
                int.class, LENGTH,
                Int64Vector.class,
                int.class, Int64Vector.LENGTH,
                this, s,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 128 && (s.boxType() == Int128Vector.class)) {
            return VectorIntrinsics.reinterpret(
                IntMaxVector.class,
                int.class, LENGTH,
                Int128Vector.class,
                int.class, Int128Vector.LENGTH,
                this, s,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 256 && (s.boxType() == Int256Vector.class)) {
            return VectorIntrinsics.reinterpret(
                IntMaxVector.class,
                int.class, LENGTH,
                Int256Vector.class,
                int.class, Int256Vector.LENGTH,
                this, s,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 512 && (s.boxType() == Int512Vector.class)) {
            return VectorIntrinsics.reinterpret(
                IntMaxVector.class,
                int.class, LENGTH,
                Int512Vector.class,
                int.class, Int512Vector.LENGTH,
                this, s,
                (species, vector) -> (IntVector) vector.defaultReinterpret(species)
            );
        } else if ((s.bitSize() > 0) && (s.bitSize() <= 2048)
                && (s.bitSize() % 128 == 0) && (s.boxType() == IntMaxVector.class)) {
            return VectorIntrinsics.reinterpret(
                IntMaxVector.class,
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
        return add((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector add(int o, VectorMask<Integer> m) {
        return add((IntMaxVector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntVector sub(int o) {
        return sub((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector sub(int o, VectorMask<Integer> m) {
        return sub((IntMaxVector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntVector mul(int o) {
        return mul((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector mul(int o, VectorMask<Integer> m) {
        return mul((IntMaxVector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntVector min(int o) {
        return min((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector max(int o) {
        return max((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> equal(int o) {
        return equal((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> notEqual(int o) {
        return notEqual((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> lessThan(int o) {
        return lessThan((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> lessThanEq(int o) {
        return lessThanEq((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> greaterThan(int o) {
        return greaterThan((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Integer> greaterThanEq(int o) {
        return greaterThanEq((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector blend(int o, VectorMask<Integer> m) {
        return blend((IntMaxVector)IntVector.broadcast(SPECIES, o), m);
    }


    @Override
    @ForceInline
    public IntVector and(int o) {
        return and((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector and(int o, VectorMask<Integer> m) {
        return and((IntMaxVector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntVector or(int o) {
        return or((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector or(int o, VectorMask<Integer> m) {
        return or((IntMaxVector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntVector xor(int o) {
        return xor((IntMaxVector)IntVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public IntVector xor(int o, VectorMask<Integer> m) {
        return xor((IntMaxVector)IntVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public IntMaxVector neg() {
        return (IntMaxVector)zero(SPECIES).sub(this);
    }

    // Unary operations

    @ForceInline
    @Override
    public IntMaxVector neg(VectorMask<Integer> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public IntMaxVector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, IntMaxVector.class, int.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (int) Math.abs(a)));
    }

    @ForceInline
    @Override
    public IntMaxVector abs(VectorMask<Integer> m) {
        return blend(abs(), m);
    }


    @Override
    @ForceInline
    public IntMaxVector not() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, IntMaxVector.class, int.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (int) ~a));
    }

    @ForceInline
    @Override
    public IntMaxVector not(VectorMask<Integer> m) {
        return blend(not(), m);
    }
    // Binary operations

    @Override
    @ForceInline
    public IntMaxVector add(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, IntMaxVector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a + b)));
    }

    @Override
    @ForceInline
    public IntMaxVector add(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public IntMaxVector sub(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, IntMaxVector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a - b)));
    }

    @Override
    @ForceInline
    public IntMaxVector sub(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public IntMaxVector mul(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, IntMaxVector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a * b)));
    }

    @Override
    @ForceInline
    public IntMaxVector mul(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public IntMaxVector min(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;
        return (IntMaxVector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, IntMaxVector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public IntMaxVector min(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(min(v), m);
    }

    @Override
    @ForceInline
    public IntMaxVector max(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, IntMaxVector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int) Math.max(a, b)));
        }

    @Override
    @ForceInline
    public IntMaxVector max(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(max(v), m);
    }

    @Override
    @ForceInline
    public IntMaxVector and(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, IntMaxVector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a & b)));
    }

    @Override
    @ForceInline
    public IntMaxVector or(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, IntMaxVector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a | b)));
    }

    @Override
    @ForceInline
    public IntMaxVector xor(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, IntMaxVector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (int)(a ^ b)));
    }

    @Override
    @ForceInline
    public IntMaxVector and(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public IntMaxVector or(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public IntMaxVector xor(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public IntMaxVector shiftL(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, IntMaxVector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a << i)));
    }

    @Override
    @ForceInline
    public IntMaxVector shiftL(int s, VectorMask<Integer> m) {
        return blend(shiftL(s), m);
    }

    @Override
    @ForceInline
    public IntMaxVector shiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, IntMaxVector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a >>> i)));
    }

    @Override
    @ForceInline
    public IntMaxVector shiftR(int s, VectorMask<Integer> m) {
        return blend(shiftR(s), m);
    }

    @Override
    @ForceInline
    public IntMaxVector aShiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, IntMaxVector.class, int.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (int) (a >> i)));
    }

    @Override
    @ForceInline
    public IntMaxVector aShiftR(int s, VectorMask<Integer> m) {
        return blend(aShiftR(s), m);
    }

    @Override
    @ForceInline
    public IntMaxVector shiftL(Vector<Integer> s) {
        IntMaxVector shiftv = (IntMaxVector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(IntVector.broadcast(SPECIES, 0x1f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_LSHIFT, IntMaxVector.class, int.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (int) (a << b)));
    }

    @Override
    @ForceInline
    public IntMaxVector shiftR(Vector<Integer> s) {
        IntMaxVector shiftv = (IntMaxVector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(IntVector.broadcast(SPECIES, 0x1f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_URSHIFT, IntMaxVector.class, int.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (int) (a >>> b)));
    }

    @Override
    @ForceInline
    public IntMaxVector aShiftR(Vector<Integer> s) {
        IntMaxVector shiftv = (IntMaxVector)s;
        // As per shift specification for Java, mask the shift count.
        shiftv = shiftv.and(IntVector.broadcast(SPECIES, 0x1f));
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_RSHIFT, IntMaxVector.class, int.class, LENGTH,
            this, shiftv,
            (v1, v2) -> v1.bOp(v2,(i,a, b) -> (int) (a >> b)));
    }
    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public int addAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, IntMaxVector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a + b)));
    }

    @Override
    @ForceInline
    public int andAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, IntMaxVector.class, int.class, LENGTH,
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
            VECTOR_OP_MIN, IntMaxVector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp(Integer.MAX_VALUE , (i, a, b) -> (int) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public int maxAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MAX, IntMaxVector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp(Integer.MIN_VALUE , (i, a, b) -> (int) Math.max(a, b)));
    }

    @Override
    @ForceInline
    public int mulAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, IntMaxVector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 1, (i, a, b) -> (int) (a * b)));
    }

    @Override
    @ForceInline
    public int orAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, IntMaxVector.class, int.class, LENGTH,
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
            VECTOR_OP_XOR, IntMaxVector.class, int.class, LENGTH,
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
        VectorIntrinsics.store(IntMaxVector.class, int.class, LENGTH,
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

        VectorIntrinsics.storeWithMap(IntMaxVector.class, int.class, LENGTH, vix.getClass(),
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
        VectorIntrinsics.store(IntMaxVector.class, int.class, LENGTH,
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
        IntMaxVector oldVal = (IntMaxVector) IntVector.fromByteArray(SPECIES, a, ix);
        IntMaxVector newVal = oldVal.blend(this, m);
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
        VectorIntrinsics.store(IntMaxVector.class, int.class, LENGTH,
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
        IntMaxVector oldVal = (IntMaxVector) IntVector.fromByteBuffer(SPECIES, bb, ix);
        IntMaxVector newVal = oldVal.blend(this, m);
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

        IntMaxVector that = (IntMaxVector) o;
        return this.equal(that).allTrue();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    IntMaxMask bTest(Vector<Integer> o, FBinTest f) {
        int[] vec1 = getElements();
        int[] vec2 = ((IntMaxVector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new IntMaxMask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public IntMaxMask equal(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;

        return VectorIntrinsics.compare(
            BT_eq, IntMaxVector.class, IntMaxMask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public IntMaxMask notEqual(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;

        return VectorIntrinsics.compare(
            BT_ne, IntMaxVector.class, IntMaxMask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public IntMaxMask lessThan(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;

        return VectorIntrinsics.compare(
            BT_lt, IntMaxVector.class, IntMaxMask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public IntMaxMask lessThanEq(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;

        return VectorIntrinsics.compare(
            BT_le, IntMaxVector.class, IntMaxMask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public IntMaxMask greaterThan(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;

        return (IntMaxMask) VectorIntrinsics.compare(
            BT_gt, IntMaxVector.class, IntMaxMask.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public IntMaxMask greaterThanEq(Vector<Integer> o) {
        Objects.requireNonNull(o);
        IntMaxVector v = (IntMaxVector)o;

        return VectorIntrinsics.compare(
            BT_ge, IntMaxVector.class, IntMaxMask.class, int.class, LENGTH,
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
        boolean[] mbits = ((IntMaxMask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }


    FloatMaxVector toFP() {
        int[] vec = getElements();
        float[] res = new float[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.intBitsToFloat(vec[i]);
        }
        return new FloatMaxVector(res);
    }

    @Override
    public IntMaxVector rotateEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new IntMaxVector(res);
    }

    @Override
    public IntMaxVector rotateER(int j) {
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
        return new IntMaxVector(res);
    }

    @Override
    public IntMaxVector shiftEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new IntMaxVector(res);
    }

    @Override
    public IntMaxVector shiftER(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new IntMaxVector(res);
    }

    @Override
    @ForceInline
    public IntMaxVector rearrange(Vector<Integer> v,
                                  VectorShuffle<Integer> s, VectorMask<Integer> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    @ForceInline
    public IntMaxVector rearrange(VectorShuffle<Integer> o1) {
        Objects.requireNonNull(o1);
        IntMaxShuffle s =  (IntMaxShuffle)o1;

        return VectorIntrinsics.rearrangeOp(
            IntMaxVector.class, IntMaxShuffle.class, int.class, LENGTH,
            this, s,
            (v1, s_) -> v1.uOp((i, a) -> {
                int ei = s_.lane(i);
                return v1.lane(ei);
            }));
    }

    @Override
    @ForceInline
    public IntMaxVector blend(Vector<Integer> o1, VectorMask<Integer> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        IntMaxVector v = (IntMaxVector)o1;
        IntMaxMask   m = (IntMaxMask)o2;

        return VectorIntrinsics.blend(
            IntMaxVector.class, IntMaxMask.class, int.class, LENGTH,
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
                                IntMaxVector.class, int.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    int[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public IntMaxVector with(int i, int e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                IntMaxVector.class, int.class, LENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    int[] res = v.getElements().clone();
                                    res[ix] = (int)bits;
                                    return new IntMaxVector(res);
                                });
    }

    // Mask

    static final class IntMaxMask extends AbstractMask<Integer> {
        static final IntMaxMask TRUE_MASK = new IntMaxMask(true);
        static final IntMaxMask FALSE_MASK = new IntMaxMask(false);

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public IntMaxMask(boolean[] bits) {
            this(bits, 0);
        }

        public IntMaxMask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public IntMaxMask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        IntMaxMask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new IntMaxMask(res);
        }

        @Override
        IntMaxMask bOp(VectorMask<Integer> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((IntMaxMask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new IntMaxMask(res);
        }

        @Override
        public VectorSpecies<Integer> species() {
            return SPECIES;
        }

        @Override
        public IntMaxVector toVector() {
            int[] res = new int[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (int) (bits[i] ? -1 : 0);
            }
            return new IntMaxVector(res);
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
                return (VectorMask <E>) new ByteMaxVector.ByteMaxMask(maskArray);
            } else if (stype == short.class) {
                return (VectorMask <E>) new ShortMaxVector.ShortMaxMask(maskArray);
            } else if (stype == int.class) {
                return (VectorMask <E>) new IntMaxVector.IntMaxMask(maskArray);
            } else if (stype == long.class) {
                return (VectorMask <E>) new LongMaxVector.LongMaxMask(maskArray);
            } else if (stype == float.class) {
                return (VectorMask <E>) new FloatMaxVector.FloatMaxMask(maskArray);
            } else if (stype == double.class) {
                return (VectorMask <E>) new DoubleMaxVector.DoubleMaxMask(maskArray);
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }
        }

        // Unary operations

        @Override
        @ForceInline
        public IntMaxMask not() {
            return (IntMaxMask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, IntMaxMask.class, int.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public IntMaxMask and(VectorMask<Integer> o) {
            Objects.requireNonNull(o);
            IntMaxMask m = (IntMaxMask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, IntMaxMask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public IntMaxMask or(VectorMask<Integer> o) {
            Objects.requireNonNull(o);
            IntMaxMask m = (IntMaxMask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, IntMaxMask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, IntMaxMask.class, int.class, LENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((IntMaxMask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, IntMaxMask.class, int.class, LENGTH,
                                         this, VectorMask.maskAllTrue(species()),
                                         (m, __) -> allTrueHelper(((IntMaxMask)m).getBits()));
        }
    }

    // Shuffle

    static final class IntMaxShuffle extends AbstractShuffle<Integer> {
        IntMaxShuffle(byte[] reorder) {
            super(reorder);
        }

        public IntMaxShuffle(int[] reorder) {
            super(reorder);
        }

        public IntMaxShuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public IntMaxShuffle(IntUnaryOperator f) {
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
                return (VectorShuffle<F>) new ByteMaxVector.ByteMaxShuffle(shuffleArray);
            } else if (stype == short.class) {
                return (VectorShuffle<F>) new ShortMaxVector.ShortMaxShuffle(shuffleArray);
            } else if (stype == int.class) {
                return (VectorShuffle<F>) new IntMaxVector.IntMaxShuffle(shuffleArray);
            } else if (stype == long.class) {
                return (VectorShuffle<F>) new LongMaxVector.LongMaxShuffle(shuffleArray);
            } else if (stype == float.class) {
                return (VectorShuffle<F>) new FloatMaxVector.FloatMaxShuffle(shuffleArray);
            } else if (stype == double.class) {
                return (VectorShuffle<F>) new DoubleMaxVector.DoubleMaxShuffle(shuffleArray);
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }
        }

        @Override
        public IntMaxShuffle rearrange(VectorShuffle<Integer> o) {
            IntMaxShuffle s = (IntMaxShuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new IntMaxShuffle(r);
        }
    }

    // VectorSpecies

    @Override
    public VectorSpecies<Integer> species() {
        return SPECIES;
    }
}
