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
final class Byte256Vector extends ByteVector<Shapes.S256Bit> {
    static final Byte256Species SPECIES = new Byte256Species();

    static final Byte256Vector ZERO = new Byte256Vector();

    static final int LENGTH = SPECIES.length();

    private final byte[] vec; // Don't access directly, use getElements() instead.

    private byte[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Byte256Vector() {
        vec = new byte[SPECIES.length()];
    }

    Byte256Vector(byte[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Byte256Vector uOp(FUnOp f) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Byte256Vector(res);
    }

    @Override
    Byte256Vector uOp(Mask<Byte, Shapes.S256Bit> o, FUnOp f) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        boolean[] mbits = ((Byte256Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Byte256Vector(res);
    }

    // Binary operator

    @Override
    Byte256Vector bOp(Vector<Byte, Shapes.S256Bit> o, FBinOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = this.getElements();
        byte[] vec2 = ((Byte256Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Byte256Vector(res);
    }

    @Override
    Byte256Vector bOp(Vector<Byte, Shapes.S256Bit> o1, Mask<Byte, Shapes.S256Bit> o2, FBinOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = this.getElements();
        byte[] vec2 = ((Byte256Vector)o1).getElements();
        boolean[] mbits = ((Byte256Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Byte256Vector(res);
    }

    // Trinary operator

    @Override
    Byte256Vector tOp(Vector<Byte, Shapes.S256Bit> o1, Vector<Byte, Shapes.S256Bit> o2, FTriOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = this.getElements();
        byte[] vec2 = ((Byte256Vector)o1).getElements();
        byte[] vec3 = ((Byte256Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Byte256Vector(res);
    }

    @Override
    Byte256Vector tOp(Vector<Byte, Shapes.S256Bit> o1, Vector<Byte, Shapes.S256Bit> o2, Mask<Byte, Shapes.S256Bit> o3, FTriOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = getElements();
        byte[] vec2 = ((Byte256Vector)o1).getElements();
        byte[] vec3 = ((Byte256Vector)o2).getElements();
        boolean[] mbits = ((Byte256Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Byte256Vector(res);
    }

    @Override
    byte rOp(byte v, FBinOp f) {
        byte[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> add(byte o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> add(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> addSaturate(byte o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> addSaturate(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> sub(byte o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> sub(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> subSaturate(byte o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> subSaturate(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> mul(byte o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> mul(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> div(byte o) {
        return div(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> div(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return div(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> min(byte o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> max(byte o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S256Bit> equal(byte o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S256Bit> notEqual(byte o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S256Bit> lessThan(byte o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S256Bit> lessThanEq(byte o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S256Bit> greaterThan(byte o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S256Bit> greaterThanEq(byte o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> blend(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> and(byte o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> and(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> or(byte o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> or(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> xor(byte o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> xor(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> floorDiv(byte o) {
        return floorDiv(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> floorDiv(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return floorDiv(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> floorMod(byte o) {
        return floorMod(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S256Bit> floorMod(byte o, Mask<Byte,Shapes.S256Bit> m) {
        return floorMod(SPECIES.broadcast(o), m);
    }


    // Binary operations

    @Override
    @ForceInline
    public Byte256Vector add(Vector<Byte,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Byte256Vector v = (Byte256Vector)o;
        return (Byte256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Byte256Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte256Vector)v1).bOp(v2, (i, a, b) -> (byte)(a + b)));
    }

    @Override
    @ForceInline
    public Byte256Vector sub(Vector<Byte,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Byte256Vector v = (Byte256Vector)o;
        return (Byte256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Byte256Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte256Vector)v1).bOp(v2, (i, a, b) -> (byte)(a - b)));
    }

    @Override
    @ForceInline
    public Byte256Vector mul(Vector<Byte,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Byte256Vector v = (Byte256Vector)o;
        return (Byte256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Byte256Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte256Vector)v1).bOp(v2, (i, a, b) -> (byte)(a * b)));
    }


    @Override
    @ForceInline
    public Byte256Vector div(Vector<Byte,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Byte256Vector v = (Byte256Vector)o;
        return (Byte256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Byte256Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte256Vector)v1).bOp(v2, (i, a, b) -> (byte)(a / b)));
    }

    @Override
    @ForceInline
    public Byte256Vector and(Vector<Byte,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Byte256Vector v = (Byte256Vector)o;
        return (Byte256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Byte256Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte256Vector)v1).bOp(v2, (i, a, b) -> (byte)(a & b)));
    }

    @Override
    @ForceInline
    public Byte256Vector or(Vector<Byte,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Byte256Vector v = (Byte256Vector)o;
        return (Byte256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Byte256Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte256Vector)v1).bOp(v2, (i, a, b) -> (byte)(a | b)));
    }

    @Override
    @ForceInline
    public Byte256Vector xor(Vector<Byte,Shapes.S256Bit> o) {
        Objects.requireNonNull(o);
        Byte256Vector v = (Byte256Vector)o;
        return (Byte256Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Byte256Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte256Vector)v1).bOp(v2, (i, a, b) -> (byte)(a ^ b)));
    }

    // Type specific horizontal reductions


    // Memory operations

    @Override
    @ForceInline
    public void intoArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Byte256Vector.class, byte.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((byte[])arr)[idx + i] = a_));
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

        Byte256Vector that = (Byte256Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Byte256Mask bTest(Vector<Byte, Shapes.S256Bit> o, FBinTest f) {
        byte[] vec1 = getElements();
        byte[] vec2 = ((Byte256Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Byte256Mask(bits);
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
    void forEach(Mask<Byte, Shapes.S256Bit> o, FUnCon f) {
        boolean[] mbits = ((Byte256Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Byte256Vector rotateEL(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Byte256Vector(res);
    }

    @Override
    public Byte256Vector rotateER(int j) {
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
        return new Byte256Vector(res);
    }

    @Override
    public Byte256Vector shiftEL(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Byte256Vector(res);
    }

    @Override
    public Byte256Vector shiftER(int j) {
        byte[] vec = getElements();
        byte[] res = new byte[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Byte256Vector(res);
    }

    @Override
    public Byte256Vector shuffle(Vector<Byte, Shapes.S256Bit> o, Shuffle<Byte, Shapes.S256Bit> s) {
        Byte256Vector v = (Byte256Vector) o;
        return uOp((i, a) -> {
            byte[] vec = this.getElements();
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
    public Byte256Vector swizzle(Shuffle<Byte, Shapes.S256Bit> s) {
        return uOp((i, a) -> {
            byte[] vec = this.getElements();
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

        byte[] vec = getElements();
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
    public byte get(int i) {
        byte[] vec = getElements();
        return vec[i];
    }

    @Override
    public Byte256Vector with(int i, byte e) {
        byte[] res = vec.clone();
        res[i] = e;
        return new Byte256Vector(res);
    }

    // Mask

    static final class Byte256Mask extends AbstractMask<Byte, Shapes.S256Bit> {
        static final Byte256Mask TRUE_MASK = new Byte256Mask(true);
        static final Byte256Mask FALSE_MASK = new Byte256Mask(false);

        public Byte256Mask(boolean[] bits) {
            super(bits);
        }

        public Byte256Mask(boolean val) {
            super(val);
        }

        @Override
        Byte256Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Byte256Mask(res);
        }

        @Override
        Byte256Mask bOp(Mask<Byte, Shapes.S256Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Byte256Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Byte256Mask(res);
        }

        @Override
        public Byte256Species species() {
            return SPECIES;
        }

        @Override
        public Byte256Vector toVector() {
            byte[] res = new byte[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (byte) (bits[i] ? -1 : 0);
            }
            return new Byte256Vector(res);
        }

        // Unary operations

        //Mask<E, S> not();

        // Binary operations

        @Override
        @ForceInline
        public Byte256Mask and(Mask<Byte,Shapes.S256Bit> o) {
            Objects.requireNonNull(o);
            Byte256Mask m = (Byte256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Byte256Mask.class, byte.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Byte256Mask or(Mask<Byte,Shapes.S256Bit> o) {
            Objects.requireNonNull(o);
            Byte256Mask m = (Byte256Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Byte256Mask.class, byte.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Byte256Mask.class, byte.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Byte256Mask.class, byte.class, LENGTH,
                                         this, species().trueMask(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Byte256Shuffle extends AbstractShuffle<Byte, Shapes.S256Bit> {
        static final IntVector.IntSpecies<Shapes.S256Bit> INT_SPECIES = (IntVector.IntSpecies<Shapes.S256Bit>) Vector.speciesInstance(Integer.class, Shapes.S_256_BIT);

        public Byte256Shuffle(int[] reorder) {
            super(reorder);
        }

        @Override
        public Byte256Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S256Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Byte256Species species() {
        return SPECIES;
    }

    static final class Byte256Species extends ByteSpecies<Shapes.S256Bit> {
        static final int BIT_SIZE = Shapes.S_256_BIT.bitSize();

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
        public int bitSize() {
            return BIT_SIZE;
        }

        @Override
        public int length() {
            return LENGTH;
        }

        @Override
        public Class<Byte> elementType() {
            return Byte.class;
        }

        @Override
        public int elementSize() {
            return Byte.SIZE;
        }

        @Override
        public Shapes.S256Bit shape() {
            return Shapes.S_256_BIT;
        }

        @Override
        Byte256Vector op(FOp f) {
            byte[] res = new byte[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Byte256Vector(res);
        }

        @Override
        Byte256Vector op(Mask<Byte, Shapes.S256Bit> o, FOp f) {
            byte[] res = new byte[length()];
            boolean[] mbits = ((Byte256Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Byte256Vector(res);
        }

        // Factories

        @Override
        public Byte256Mask constantMask(boolean... bits) {
            return new Byte256Mask(bits.clone());
        }

        @Override
        public Byte256Shuffle constantShuffle(int... ixs) {
            return new Byte256Shuffle(ixs);
        }

        @Override
        @ForceInline
        public Byte256Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Byte256Vector.class, byte.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Byte256Vector broadcast(byte e) {
            return VectorIntrinsics.broadcastCoerced(
                Byte256Vector.class, byte.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (byte)bits)));
        }

        @HotSpotIntrinsicCandidate
        @Override
        @ForceInline
        public Byte256Mask trueMask() {
            return VectorIntrinsics.broadcastCoerced(Byte256Mask.class, byte.class, LENGTH,
                                                     (byte)-1,
                                                     (z -> Byte256Mask.TRUE_MASK));
        }

        @HotSpotIntrinsicCandidate
        @Override
        @ForceInline
        public Byte256Mask falseMask() {
            return VectorIntrinsics.broadcastCoerced(Byte256Mask.class, byte.class, LENGTH,
                                                     0,
                                                     (z -> Byte256Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Byte256Vector fromArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Byte256Vector) VectorIntrinsics.load(Byte256Vector.class, byte.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((byte[]) arr, idx));
        }
    }
}
