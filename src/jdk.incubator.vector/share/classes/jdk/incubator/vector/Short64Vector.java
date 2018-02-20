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
final class Short64Vector extends ShortVector<Shapes.S64Bit> {
    static final Short64Species SPECIES = new Short64Species();

    static final Short64Vector ZERO = new Short64Vector();

    static final int LENGTH = SPECIES.length();

    private final short[] vec; // Don't access directly, use getElements() instead.

    private short[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Short64Vector() {
        vec = new short[SPECIES.length()];
    }

    Short64Vector(short[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Short64Vector uOp(FUnOp f) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Short64Vector(res);
    }

    @Override
    Short64Vector uOp(Mask<Short, Shapes.S64Bit> o, FUnOp f) {
        short[] vec = getElements();
        short[] res = new short[length()];
        boolean[] mbits = ((Short64Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Short64Vector(res);
    }

    // Binary operator

    @Override
    Short64Vector bOp(Vector<Short, Shapes.S64Bit> o, FBinOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short64Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Short64Vector(res);
    }

    @Override
    Short64Vector bOp(Vector<Short, Shapes.S64Bit> o1, Mask<Short, Shapes.S64Bit> o2, FBinOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short64Vector)o1).getElements();
        boolean[] mbits = ((Short64Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Short64Vector(res);
    }

    // Trinary operator

    @Override
    Short64Vector tOp(Vector<Short, Shapes.S64Bit> o1, Vector<Short, Shapes.S64Bit> o2, FTriOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short64Vector)o1).getElements();
        short[] vec3 = ((Short64Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Short64Vector(res);
    }

    @Override
    Short64Vector tOp(Vector<Short, Shapes.S64Bit> o1, Vector<Short, Shapes.S64Bit> o2, Mask<Short, Shapes.S64Bit> o3, FTriOp f) {
        short[] res = new short[length()];
        short[] vec1 = getElements();
        short[] vec2 = ((Short64Vector)o1).getElements();
        short[] vec3 = ((Short64Vector)o2).getElements();
        boolean[] mbits = ((Short64Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Short64Vector(res);
    }

    @Override
    short rOp(short v, FBinOp f) {
        short[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations

    @Override
    @ForceInline
    public Short64Vector add(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a + b)));
    }

    @Override
    @ForceInline
    public Short64Vector sub(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a - b)));
    }

    @Override
    @ForceInline
    public Short64Vector mul(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a * b)));
    }


    @Override
    @ForceInline
    public Short64Vector div(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_DIV, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a / b)));
    }

    @Override
    @ForceInline
    public Short64Vector and(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a & b)));
    }

    @Override
    @ForceInline
    public Short64Vector or(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a | b)));
    }

    @Override
    @ForceInline
    public Short64Vector xor(Vector<Short,Shapes.S64Bit> o) {
        Objects.requireNonNull(o);
        Short64Vector v = (Short64Vector)o;
        return (Short64Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Short64Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short64Vector)v1).bOp(v2, (i, a, b) -> (short)(a ^ b)));
    }

    // Type specific horizontal reductions


    // Memory operations

    @Override
    @ForceInline
    public void intoArray(short[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Short64Vector.class, short.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((short[])arr)[idx + i] = a_));
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

        Short64Vector that = (Short64Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Short64Mask bTest(Vector<Short, Shapes.S64Bit> o, FBinTest f) {
        short[] vec1 = getElements();
        short[] vec2 = ((Short64Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Short64Mask(bits);
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        short[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(Mask<Short, Shapes.S64Bit> o, FUnCon f) {
        boolean[] mbits = ((Short64Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Short64Vector rotateEL(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector rotateER(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector shiftEL(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector shiftER(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Short64Vector(res);
    }

    @Override
    public Short64Vector shuffle(Vector<Short, Shapes.S64Bit> o, Shuffle<Short, Shapes.S64Bit> s) {
        Short64Vector v = (Short64Vector) o;
        return uOp((i, a) -> {
            short[] vec = this.getElements();
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
    public Short64Vector swizzle(Shuffle<Short, Shapes.S64Bit> s) {
        return uOp((i, a) -> {
            short[] vec = this.getElements();
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

        short[] vec = getElements();
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
    public short get(int i) {
        short[] vec = getElements();
        return vec[i];
    }

    @Override
    public Short64Vector with(int i, short e) {
        short[] res = vec.clone();
        res[i] = e;
        return new Short64Vector(res);
    }

    // Mask

    static final class Short64Mask extends AbstractMask<Short, Shapes.S64Bit> {
        static final Short64Mask TRUE_MASK = new Short64Mask(true);
        static final Short64Mask FALSE_MASK = new Short64Mask(false);

        public Short64Mask(boolean[] bits) {
            super(bits);
        }

        public Short64Mask(boolean val) {
            super(val);
        }

        @Override
        Short64Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Short64Mask(res);
        }

        @Override
        Short64Mask bOp(Mask<Short, Shapes.S64Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Short64Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Short64Mask(res);
        }

        @Override
        public Short64Species species() {
            return SPECIES;
        }

        @Override
        public Short64Vector toVector() {
            short[] res = new short[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (short) (bits[i] ? -1 : 0);
            }
            return new Short64Vector(res);
        }

        // Unary operations

        //Mask<E, S> not();

        // Binary operations

        @Override
        @ForceInline
        public Short64Mask and(Mask<Short,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Short64Mask m = (Short64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Short64Mask.class, short.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Short64Mask or(Mask<Short,Shapes.S64Bit> o) {
            Objects.requireNonNull(o);
            Short64Mask m = (Short64Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Short64Mask.class, short.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Short64Mask.class, short.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Short64Mask.class, short.class, LENGTH,
                                         this, species().trueMask(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Species

    @Override
    public Short64Species species() {
        return SPECIES;
    }

    static final class Short64Species extends ShortSpecies<Shapes.S64Bit> {
        static final int BIT_SIZE = Shapes.S_64_BIT.bitSize();

        static final int LENGTH = BIT_SIZE / Short.SIZE;

        @Override
        public String toString() {
           StringBuilder sb = new StringBuilder("Shape[");
           sb.append(bitSize()).append(" bits, ");
           sb.append(length()).append(" ").append(short.class.getSimpleName()).append("s x ");
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
        public Class<Short> elementType() {
            return Short.class;
        }

        @Override
        public int elementSize() {
            return Short.SIZE;
        }

        @Override
        public Shapes.S64Bit shape() {
            return Shapes.S_64_BIT;
        }

        @Override
        Short64Vector op(FOp f) {
            short[] res = new short[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Short64Vector(res);
        }

        @Override
        Short64Vector op(Mask<Short, Shapes.S64Bit> o, FOp f) {
            short[] res = new short[length()];
            boolean[] mbits = ((Short64Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Short64Vector(res);
        }

        // Factories

        @Override
        public Short64Mask constantMask(boolean... bits) {
            return new Short64Mask(bits.clone());
        }


        @Override
        @ForceInline
        public Short64Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Short64Vector.class, short.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Short64Vector broadcast(short e) {
            return VectorIntrinsics.broadcastCoerced(
                Short64Vector.class, short.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (short)bits)));
        }

        @Override
        @ForceInline
        public Short64Mask trueMask() {
            return VectorIntrinsics.broadcastCoerced(Short64Mask.class, short.class, LENGTH,
                                                     (short)-1,
                                                     (z -> Short64Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Short64Mask falseMask() {
            return VectorIntrinsics.broadcastCoerced(Short64Mask.class, short.class, LENGTH,
                                                     0,
                                                     (z -> Short64Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Short64Vector fromArray(short[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Short64Vector) VectorIntrinsics.load(Short64Vector.class, short.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((short[]) arr, idx));
        }
    }
}
