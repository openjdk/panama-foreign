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
import jdk.internal.HotSpotIntrinsicCandidate;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Int64Vector extends IntVector<Shapes.S64Bit> {
    static final Int64Species SPECIES = new Int64Species();

    static final Int64Vector ZERO = new Int64Vector();

    static final int LENGTH = SPECIES.length();

    private final int[] vec; // Don't access directly, use getElements() instead.

    private int[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Int64Vector() {
        vec = new int[SPECIES.length()];
    }

    Int64Vector(int[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Int64Vector uOp(FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Int64Vector(res);
    }

    @Override
    Int64Vector uOp(Mask<Integer, Shapes.S64Bit> o, FUnOp f) {
        int[] vec = getElements();
        int[] res = new int[length()];
        boolean[] mbits = ((Int64Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Int64Vector(res);
    }

    // Binary operator

    @Override
    Int64Vector bOp(Vector<Integer, Shapes.S64Bit> o, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int64Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int64Vector(res);
    }

    @Override
    Int64Vector bOp(Vector<Integer, Shapes.S64Bit> o1, Mask<Integer, Shapes.S64Bit> o2, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int64Vector)o1).getElements();
        boolean[] mbits = ((Int64Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Int64Vector(res);
    }

    // Trinary operator

    @Override
    Int64Vector tOp(Vector<Integer, Shapes.S64Bit> o1, Vector<Integer, Shapes.S64Bit> o2, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int64Vector)o1).getElements();
        int[] vec3 = ((Int64Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Int64Vector(res);
    }

    @Override
    Int64Vector tOp(Vector<Integer, Shapes.S64Bit> o1, Vector<Integer, Shapes.S64Bit> o2, Mask<Integer, Shapes.S64Bit> o3, FTriOp f) {
        int[] res = new int[length()];
        int[] vec1 = getElements();
        int[] vec2 = ((Int64Vector)o1).getElements();
        int[] vec3 = ((Int64Vector)o2).getElements();
        boolean[] mbits = ((Int64Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Int64Vector(res);
    }

    @Override
    int rOp(int v, FBinOp f) {
        int[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> add(int o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> add(int o, Mask<Integer,Shapes.S64Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> addSaturate(int o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> addSaturate(int o, Mask<Integer,Shapes.S64Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> sub(int o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> sub(int o, Mask<Integer,Shapes.S64Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> subSaturate(int o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> subSaturate(int o, Mask<Integer,Shapes.S64Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> mul(int o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> mul(int o, Mask<Integer,Shapes.S64Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> div(int o) {
        return div(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> div(int o, Mask<Integer,Shapes.S64Bit> m) {
        return div(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> min(int o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> max(int o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> equal(int o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> notEqual(int o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> lessThan(int o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> lessThanEq(int o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> greaterThan(int o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Integer, Shapes.S64Bit> greaterThanEq(int o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> blend(int o, Mask<Integer,Shapes.S64Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> and(int o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> and(int o, Mask<Integer,Shapes.S64Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> or(int o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> or(int o, Mask<Integer,Shapes.S64Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> xor(int o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> xor(int o, Mask<Integer,Shapes.S64Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> floorDiv(int o) {
        return floorDiv(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> floorDiv(int o, Mask<Integer,Shapes.S64Bit> m) {
        return floorDiv(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> floorMod(int o) {
        return floorMod(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public IntVector<Shapes.S64Bit> floorMod(int o, Mask<Integer,Shapes.S64Bit> m) {
        return floorMod(SPECIES.broadcast(o), m);
    }


    // Binary operations

    @Override
    @ForceInline
    public Int64Vector add(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a + b)));
    }

    @Override
    @ForceInline
    public Int64Vector sub(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a - b)));
    }

    @Override
    @ForceInline
    public Int64Vector mul(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a * b)));
    }


    @Override
    @ForceInline
    public Int64Vector div(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a / b)));
    }

    @Override
    @ForceInline
    public Int64Vector and(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a & b)));
    }

    @Override
    @ForceInline
    public Int64Vector or(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a | b)));
    }

    @Override
    @ForceInline
    public Int64Vector xor(Vector<Integer,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Int64Vector v = (Int64Vector)o;
        return (Int64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Int64Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int64Vector)v1).bOp(v2, (i, a, b) -> (int)(a ^ b)));
    }

    // Type specific horizontal reductions

    @Override
    @ForceInline
    public int addAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Int64Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 0, (i, a, b) -> (int) (a + b)));
    }

    @Override
    @ForceInline
    public int mulAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Int64Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 1, (i, a, b) -> (int) (a * b)));
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(int[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Int64Vector.class, int.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((int[])arr)[idx + i] = a_));
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

        Int64Vector that = (Int64Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Int64Mask bTest(Vector<Integer, Shapes.S64Bit> o, FBinTest f) {
        int[] vec1 = getElements();
        int[] vec2 = ((Int64Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int64Mask(bits);
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
    void forEach(Mask<Integer, Shapes.S64Bit> o, FUnCon f) {
        boolean[] mbits = ((Int64Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }


    Float64Vector toFP() {
        int[] vec = getElements();
        float[] res = new float[this.species().length()];
        for(int i = 0; i < this.species().length(); i++){
            res[i] = Float.intBitsToFloat(vec[i]);
        }
        return new Float64Vector(res);
    }

    @Override
    public Int64Vector rotateEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Int64Vector(res);
    }

    @Override
    public Int64Vector rotateER(int j) {
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
        return new Int64Vector(res);
    }

    @Override
    public Int64Vector shiftEL(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Int64Vector(res);
    }

    @Override
    public Int64Vector shiftER(int j) {
        int[] vec = getElements();
        int[] res = new int[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Int64Vector(res);
    }

    @Override
    public Int64Vector shuffle(Vector<Integer, Shapes.S64Bit> o, Shuffle<Integer, Shapes.S64Bit> s) {
        Int64Vector v = (Int64Vector) o;
        return uOp((i, a) -> {
            int[] vec = this.getElements();
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
    public Int64Vector swizzle(Shuffle<Integer, Shapes.S64Bit> s) {
        return uOp((i, a) -> {
            int[] vec = this.getElements();
            int e = s.getElement(i);
            if(e >= 0 && e < length()) {
                return vec[e];
            } else {
                throw new ArrayIndexOutOfBoundsException("Bad reordering for shuffle");
            }
        });
    }

    @Override
    public <F, Z extends Shape> Vector<F, Z> cast(Class<F> type, Z shape) {
        Vector.Species<F,Z> species = Vector.speciesInstance(type, shape);

        // Whichever is larger
        int blen = Math.max(species.bitSize(), bitSize()) / Byte.SIZE;
        ByteBuffer bb = ByteBuffer.allocate(blen);

        int limit = Math.min(species.length(), length());

        int[] vec = getElements();
        if (type == Byte.class) {
            for (int i = 0; i < limit; i++){
                bb.put(i, (byte) vec[i]);
            }
        } else if (type == Short.class) {
            for (int i = 0; i < limit; i++){
                bb.asShortBuffer().put(i, (short) vec[i]);
            }
        } else if (type == Integer.class) {
            for (int i = 0; i < limit; i++){
                bb.asIntBuffer().put(i, (int) vec[i]);
            }
        } else if (type == Long.class){
            for (int i = 0; i < limit; i++){
                bb.asLongBuffer().put(i, (long) vec[i]);
            }
        } else if (type == Float.class){
            for (int i = 0; i < limit; i++){
                bb.asFloatBuffer().put(i, (float) vec[i]);
            }
        } else if (type == Double.class){
            for (int i = 0; i < limit; i++){
                bb.asDoubleBuffer().put(i, (double) vec[i]);
            }
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }

        return species.fromByteBuffer(bb);
    }

    // Accessors

    @Override
    public int get(int i) {
        int[] vec = getElements();
        return vec[i];
    }

    @Override
    public Int64Vector with(int i, int e) {
        int[] res = vec.clone();
        res[i] = e;
        return new Int64Vector(res);
    }

    // Mask

    static final class Int64Mask extends AbstractMask<Integer, Shapes.S64Bit> {
        static final Int64Mask TRUE_MASK = new Int64Mask(true);
        static final Int64Mask FALSE_MASK = new Int64Mask(false);

        public Int64Mask(boolean[] bits) {
            super(bits);
        }

        public Int64Mask(boolean val) {
            super(val);
        }

        @Override
        Int64Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Int64Mask(res);
        }

        @Override
        Int64Mask bOp(Mask<Integer, Shapes.S64Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Int64Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Int64Mask(res);
        }

        @Override
        public Int64Species species() {
            return SPECIES;
        }

        @Override
        public Int64Vector toVector() {
            int[] res = new int[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (int) (bits[i] ? -1 : 0);
            }
            return new Int64Vector(res);
        }

        // Unary operations

        //Mask<E, S> not();

        // Binary operations

        @Override
        @ForceInline
        public Int64Mask and(Mask<Integer,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Int64Mask m = (Int64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Int64Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Int64Mask or(Mask<Integer,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Int64Mask m = (Int64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Int64Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Int64Mask.class, int.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Int64Mask.class, int.class, LENGTH,
                                         this, species().trueMask(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Int64Shuffle extends AbstractShuffle<Integer, Shapes.S64Bit> {
        static final IntVector.IntSpecies<Shapes.S64Bit> INT_SPECIES = (IntVector.IntSpecies<Shapes.S64Bit>) Vector.speciesInstance(Integer.class, Shapes.S_64_BIT);

        public Int64Shuffle(int[] reorder) {
            super(reorder);
        }

        @Override
        public Int64Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S64Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Int64Species species() {
        return SPECIES;
    }

    static final class Int64Species extends IntSpecies<Shapes.S64Bit> {
        static final int BIT_SIZE = Shapes.S_64_BIT.bitSize();

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
        public int bitSize() {
            return BIT_SIZE;
        }

        @Override
        public int length() {
            return LENGTH;
        }

        @Override
        public Class<Integer> elementType() {
            return Integer.class;
        }

        @Override
        public int elementSize() {
            return Integer.SIZE;
        }

        @Override
        public Shapes.S64Bit shape() {
            return Shapes.S_64_BIT;
        }

        @Override
        Int64Vector op(FOp f) {
            int[] res = new int[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Int64Vector(res);
        }

        @Override
        Int64Vector op(Mask<Integer, Shapes.S64Bit> o, FOp f) {
            int[] res = new int[length()];
            boolean[] mbits = ((Int64Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Int64Vector(res);
        }

        // Factories

        @Override
        public Int64Mask constantMask(boolean... bits) {
            return new Int64Mask(bits.clone());
        }

        @Override
        public Int64Shuffle constantShuffle(int... ixs) {
            return new Int64Shuffle(ixs);
        }

        @Override
        @ForceInline
        public Int64Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Int64Vector.class, int.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Int64Vector broadcast(int e) {
            return VectorIntrinsics.broadcastCoerced(
                Int64Vector.class, int.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (int)bits)));
        }

        @HotSpotIntrinsicCandidate
        @Override
        @ForceInline
        public Int64Mask trueMask() {
            return VectorIntrinsics.broadcastCoerced(Int64Mask.class, int.class, LENGTH,
                                                     (int)-1,
                                                     (z -> Int64Mask.TRUE_MASK));
        }

        @HotSpotIntrinsicCandidate
        @Override
        @ForceInline
        public Int64Mask falseMask() {
            return VectorIntrinsics.broadcastCoerced(Int64Mask.class, int.class, LENGTH,
                                                     0,
                                                     (z -> Int64Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Int64Vector fromArray(int[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Int64Vector) VectorIntrinsics.load(Int64Vector.class, int.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((int[]) arr, idx));
        }
    }
}
