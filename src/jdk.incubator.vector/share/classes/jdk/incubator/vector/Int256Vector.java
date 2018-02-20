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
final class Int256Vector extends IntVector<Shapes.S256Bit> {
    static final Int256Species SPECIES = new Int256Species();

    static final Int256Vector ZERO = new Int256Vector();

    static final int LENGTH = SPECIES.length();

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
    Int256Vector uOp(Mask<Integer, Shapes.S256Bit> o, FUnOp f) {
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
    Int256Vector bOp(Vector<Integer, Shapes.S256Bit> o, FBinOp f) {
        int[] res = new int[length()];
        int[] vec1 = this.getElements();
        int[] vec2 = ((Int256Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int256Vector(res);
    }

    @Override
    Int256Vector bOp(Vector<Integer, Shapes.S256Bit> o1, Mask<Integer, Shapes.S256Bit> o2, FBinOp f) {
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
    Int256Vector tOp(Vector<Integer, Shapes.S256Bit> o1, Vector<Integer, Shapes.S256Bit> o2, FTriOp f) {
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
    Int256Vector tOp(Vector<Integer, Shapes.S256Bit> o1, Vector<Integer, Shapes.S256Bit> o2, Mask<Integer, Shapes.S256Bit> o3, FTriOp f) {
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

    // Binary operations

    @Override
    @ForceInline
    public Int256Vector add(Vector<Integer,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return (Int256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int256Vector)v1).bOp(v2, (i, a, b) -> (int)(a + b)));
    }

    @Override
    @ForceInline
    public Int256Vector sub(Vector<Integer,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return (Int256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int256Vector)v1).bOp(v2, (i, a, b) -> (int)(a - b)));
    }

    @Override
    @ForceInline
    public Int256Vector mul(Vector<Integer,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return (Int256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int256Vector)v1).bOp(v2, (i, a, b) -> (int)(a * b)));
    }


    @Override
    @ForceInline
    public Int256Vector div(Vector<Integer,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return (Int256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int256Vector)v1).bOp(v2, (i, a, b) -> (int)(a / b)));
    }

    @Override
    @ForceInline
    public Int256Vector and(Vector<Integer,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return (Int256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int256Vector)v1).bOp(v2, (i, a, b) -> (int)(a & b)));
    }

    @Override
    @ForceInline
    public Int256Vector or(Vector<Integer,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return (Int256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int256Vector)v1).bOp(v2, (i, a, b) -> (int)(a | b)));
    }

    @Override
    @ForceInline
    public Int256Vector xor(Vector<Integer,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Int256Vector v = (Int256Vector)o;
        return (Int256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Int256Vector.class, int.class, LENGTH,
            this, v,
            (v1, v2) -> ((Int256Vector)v1).bOp(v2, (i, a, b) -> (int)(a ^ b)));
    }

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
    public int mulAll() {
        return (int) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Int256Vector.class, int.class, LENGTH,
            this,
            v -> (long) v.rOp((int) 1, (i, a, b) -> (int) (a * b)));
    }

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(int[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Int256Vector.class, int.class, LENGTH,
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

        Int256Vector that = (Int256Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Int256Mask bTest(Vector<Integer, Shapes.S256Bit> o, FBinTest f) {
        int[] vec1 = getElements();
        int[] vec2 = ((Int256Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Int256Mask(bits);
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
    void forEach(Mask<Integer, Shapes.S256Bit> o, FUnCon f) {
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
            res[j + i % length()] = vec[i];
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
    public Int256Vector shuffle(Vector<Integer, Shapes.S256Bit> o, Shuffle<Integer, Shapes.S256Bit> s) {
        Int256Vector v = (Int256Vector) o;
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
    public Int256Vector swizzle(Shuffle<Integer, Shapes.S256Bit> s) {
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
    public Int256Vector with(int i, int e) {
        int[] res = vec.clone();
        res[i] = e;
        return new Int256Vector(res);
    }

    // Mask

    static final class Int256Mask extends AbstractMask<Integer, Shapes.S256Bit> {
        static final Int256Mask TRUE_MASK = new Int256Mask(true);
        static final Int256Mask FALSE_MASK = new Int256Mask(false);

        public Int256Mask(boolean[] bits) {
            super(bits);
        }

        public Int256Mask(boolean val) {
            super(val);
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
        Int256Mask bOp(Mask<Integer, Shapes.S256Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Int256Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Int256Mask(res);
        }

        @Override
        public Int256Species species() {
            return SPECIES;
        }

        @Override
        public Int256Vector toVector() {
            int[] res = new int[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (int) (bits[i] ? -1 : 0);
            }
            return new Int256Vector(res);
        }

        // Unary operations

        //Mask<E, S> not();

        // Binary operations

        @Override
        @ForceInline
        public Int256Mask and(Mask<Integer,Shapes.S256Bit> o) {
            Objects.requireNonNull(o);
            Int256Mask m = (Int256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Int256Mask.class, int.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Int256Mask or(Mask<Integer,Shapes.S256Bit> o) {
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
            return VectorIntrinsics.test(COND_notZero, Int256Mask.class, int.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Int256Mask.class, int.class, LENGTH,
                                         this, species().trueMask(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Species

    @Override
    public Int256Species species() {
        return SPECIES;
    }

    static final class Int256Species extends IntSpecies<Shapes.S256Bit> {
        static final int BIT_SIZE = Shapes.S_256_BIT.bitSize();

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
        public Shapes.S256Bit shape() {
            return Shapes.S_256_BIT;
        }

        @Override
        Int256Vector op(FOp f) {
            int[] res = new int[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Int256Vector(res);
        }

        @Override
        Int256Vector op(Mask<Integer, Shapes.S256Bit> o, FOp f) {
            int[] res = new int[length()];
            boolean[] mbits = ((Int256Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Int256Vector(res);
        }

        // Factories

        @Override
        public Int256Mask constantMask(boolean... bits) {
            return new Int256Mask(bits.clone());
        }


        @Override
        @ForceInline
        public Int256Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Int256Vector.class, int.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Int256Vector broadcast(int e) {
            return VectorIntrinsics.broadcastCoerced(
                Int256Vector.class, int.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (int)bits)));
        }

        @HotSpotIntrinsicCandidate
        @Override
        @ForceInline
        public Int256Mask trueMask() {
            return VectorIntrinsics.broadcastCoerced(Int256Mask.class, int.class, LENGTH,
                                                     (int)-1,
                                                     (z -> Int256Mask.TRUE_MASK));
        }

        @HotSpotIntrinsicCandidate
        @Override
        @ForceInline
        public Int256Mask falseMask() {
            return VectorIntrinsics.broadcastCoerced(Int256Mask.class, int.class, LENGTH,
                                                     0,
                                                     (z -> Int256Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Int256Vector fromArray(int[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Int256Vector) VectorIntrinsics.load(Int256Vector.class, int.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((int[]) arr, idx));
        }
    }
}
