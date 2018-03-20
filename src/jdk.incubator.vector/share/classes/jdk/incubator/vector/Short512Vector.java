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
final class Short512Vector extends ShortVector<Shapes.S512Bit> {
    static final Short512Species SPECIES = new Short512Species();

    static final Short512Vector ZERO = new Short512Vector();

    static final int LENGTH = SPECIES.length();

    private final short[] vec; // Don't access directly, use getElements() instead.

    private short[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Short512Vector() {
        vec = new short[SPECIES.length()];
    }

    Short512Vector(short[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Short512Vector uOp(FUnOp f) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector uOp(Mask<Short, Shapes.S512Bit> o, FUnOp f) {
        short[] vec = getElements();
        short[] res = new short[length()];
        boolean[] mbits = ((Short512Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Short512Vector(res);
    }

    // Binary operator

    @Override
    Short512Vector bOp(Vector<Short, Shapes.S512Bit> o, FBinOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short512Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector bOp(Vector<Short, Shapes.S512Bit> o1, Mask<Short, Shapes.S512Bit> o2, FBinOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short512Vector)o1).getElements();
        boolean[] mbits = ((Short512Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Short512Vector(res);
    }

    // Trinary operator

    @Override
    Short512Vector tOp(Vector<Short, Shapes.S512Bit> o1, Vector<Short, Shapes.S512Bit> o2, FTriOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short512Vector)o1).getElements();
        short[] vec3 = ((Short512Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector tOp(Vector<Short, Shapes.S512Bit> o1, Vector<Short, Shapes.S512Bit> o2, Mask<Short, Shapes.S512Bit> o3, FTriOp f) {
        short[] res = new short[length()];
        short[] vec1 = getElements();
        short[] vec2 = ((Short512Vector)o1).getElements();
        short[] vec3 = ((Short512Vector)o2).getElements();
        boolean[] mbits = ((Short512Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Short512Vector(res);
    }

    @Override
    short rOp(short v, FBinOp f) {
        short[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> add(short o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> add(short o, Mask<Short,Shapes.S512Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> addSaturate(short o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> addSaturate(short o, Mask<Short,Shapes.S512Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> sub(short o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> sub(short o, Mask<Short,Shapes.S512Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> subSaturate(short o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> subSaturate(short o, Mask<Short,Shapes.S512Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> mul(short o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> mul(short o, Mask<Short,Shapes.S512Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> min(short o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> max(short o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> equal(short o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> notEqual(short o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> lessThan(short o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> lessThanEq(short o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> greaterThan(short o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Short, Shapes.S512Bit> greaterThanEq(short o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> blend(short o, Mask<Short,Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> and(short o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> and(short o, Mask<Short,Shapes.S512Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> or(short o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> or(short o, Mask<Short,Shapes.S512Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> xor(short o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ShortVector<Shapes.S512Bit> xor(short o, Mask<Short,Shapes.S512Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }


    // Unary operations



    @Override
    @ForceInline
    public Short512Vector not() {
        return (Short512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Short512Vector.class, short.class, LENGTH,
            this,
            v1 -> ((Short512Vector)v1).uOp((i, a) -> (short) ~a));
    }
    // Binary operations

    @Override
    @ForceInline
    public Short512Vector add(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return (Short512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short512Vector)v1).bOp(v2, (i, a, b) -> (short)(a + b)));
    }

    @Override
    @ForceInline
    public Short512Vector sub(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return (Short512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short512Vector)v1).bOp(v2, (i, a, b) -> (short)(a - b)));
    }

    @Override
    @ForceInline
    public Short512Vector mul(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return (Short512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short512Vector)v1).bOp(v2, (i, a, b) -> (short)(a * b)));
    }



    @Override
    @ForceInline
    public Short512Vector and(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return (Short512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short512Vector)v1).bOp(v2, (i, a, b) -> (short)(a & b)));
    }

    @Override
    @ForceInline
    public Short512Vector or(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return (Short512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short512Vector)v1).bOp(v2, (i, a, b) -> (short)(a | b)));
    }

    @Override
    @ForceInline
    public Short512Vector xor(Vector<Short,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return (Short512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> ((Short512Vector)v1).bOp(v2, (i, a, b) -> (short)(a ^ b)));
    }

    @Override
    @ForceInline
    public Short512Vector and(Vector<Short,Shapes.S512Bit> v, Mask<Short, Shapes.S512Bit> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector or(Vector<Short,Shapes.S512Bit> v, Mask<Short, Shapes.S512Bit> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector xor(Vector<Short,Shapes.S512Bit> v, Mask<Short, Shapes.S512Bit> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector shiftL(int s) {
        return (Short512Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Short512Vector.class, short.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (short) (a << i)));
    }

    @Override
    @ForceInline
    public Short512Vector shiftR(int s) {
        return (Short512Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Short512Vector.class, short.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (short) (a >>> i)));
    }

    @Override
    @ForceInline
    public Short512Vector aShiftR(int s) {
        return (Short512Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Short512Vector.class, short.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (short) (a >> i)));
    }

    // Ternary operations


    // Type specific horizontal reductions


    // Memory operations

    @Override
    @ForceInline
    public void intoArray(short[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Short512Vector.class, short.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((short[])arr)[idx + i] = a_));
    }

    @Override
    @ForceInline
    public void intoArray(short[] a, int ax, Mask<Short, Shapes.S512Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Short512Vector oldVal = SPECIES.fromArray(a, ax);
        Short512Vector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
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

        Short512Vector that = (Short512Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Short512Mask bTest(Vector<Short, Shapes.S512Bit> o, FBinTest f) {
        short[] vec1 = getElements();
        short[] vec2 = ((Short512Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Short512Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Short512Mask equal(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return (Short512Mask) VectorIntrinsics.compare(
            BT_eq, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Short512Mask notEqual(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return (Short512Mask) VectorIntrinsics.compare(
            BT_ne, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Short512Mask lessThan(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return (Short512Mask) VectorIntrinsics.compare(
            BT_lt, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Short512Mask lessThanEq(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return (Short512Mask) VectorIntrinsics.compare(
            BT_le, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Short512Mask greaterThan(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return (Short512Mask) VectorIntrinsics.compare(
            BT_gt, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Short512Mask greaterThanEq(Vector<Short, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return (Short512Mask) VectorIntrinsics.compare(
            BT_ge, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a >= b));
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
    void forEach(Mask<Short, Shapes.S512Bit> o, FUnCon f) {
        boolean[] mbits = ((Short512Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Short512Vector rotateEL(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            res[j + i % length()] = vec[i];
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector rotateER(int j) {
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
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector shiftEL(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector shiftER(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector shuffle(Vector<Short, Shapes.S512Bit> o, Shuffle<Short, Shapes.S512Bit> s) {
        Short512Vector v = (Short512Vector) o;
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
    public Short512Vector swizzle(Shuffle<Short, Shapes.S512Bit> s) {
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
    @ForceInline
    public Short512Vector blend(Vector<Short, Shapes.S512Bit> o1, Mask<Short, Shapes.S512Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Short512Vector v = (Short512Vector)o1;
        Short512Mask   m = (Short512Mask)o2;

        return (Short512Vector) VectorIntrinsics.blend(
            Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    @Override
    @ForceInline
    @SuppressWarnings("unchecked")
    public <F> Vector<F, Shapes.S512Bit> rebracket(Species<F, Shapes.S512Bit> species) {
        Objects.requireNonNull(species);
        // TODO: check proper element type
        // TODO: update to pass the two species as an arguments and ideally
        // push down intrinsic call into species implementation
        return VectorIntrinsics.rebracket(
            Short512Vector.class, short.class, LENGTH,
            species.elementType(), this,
            (v, t) -> species.reshape(v)
        );
    }

    // Accessors

    @Override
    public short get(int i) {
        short[] vec = getElements();
        return vec[i];
    }

    @Override
    public Short512Vector with(int i, short e) {
        short[] res = vec.clone();
        res[i] = e;
        return new Short512Vector(res);
    }

    // Mask

    static final class Short512Mask extends AbstractMask<Short, Shapes.S512Bit> {
        static final Short512Mask TRUE_MASK = new Short512Mask(true);
        static final Short512Mask FALSE_MASK = new Short512Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Short512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Short512Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
        }

        public Short512Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Short512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Short512Mask(res);
        }

        @Override
        Short512Mask bOp(Mask<Short, Shapes.S512Bit> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Short512Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Short512Mask(res);
        }

        @Override
        public Short512Species species() {
            return SPECIES;
        }

        @Override
        public Short512Vector toVector() {
            short[] res = new short[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (short) (bits[i] ? -1 : 0);
            }
            return new Short512Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S512Bit> rebracket(Species<Z, Shapes.S512Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.rebracket(
                Short512Mask.class, short.class, LENGTH,
                species.elementType(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        //Mask<E, S> not();

        // Binary operations

        @Override
        @ForceInline
        public Short512Mask and(Mask<Short,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Short512Mask m = (Short512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Short512Mask.class, short.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Short512Mask or(Mask<Short,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Short512Mask m = (Short512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Short512Mask.class, short.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(COND_notZero, Short512Mask.class, short.class, LENGTH,
                                         this, this,
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Short512Mask.class, short.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Short512Shuffle extends AbstractShuffle<Short, Shapes.S512Bit> {
        static final IntVector.IntSpecies<Shapes.S512Bit> INT_SPECIES = (IntVector.IntSpecies<Shapes.S512Bit>) Vector.speciesInstance(Integer.class, Shapes.S_512_BIT);

        public Short512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Short512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Short512Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S512Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Short512Species species() {
        return SPECIES;
    }

    static final class Short512Species extends ShortSpecies<Shapes.S512Bit> {
        static final int BIT_SIZE = Shapes.S_512_BIT.bitSize();

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
        public Shapes.S512Bit shape() {
            return Shapes.S_512_BIT;
        }

        @Override
        Short512Vector op(FOp f) {
            short[] res = new short[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return new Short512Vector(res);
        }

        @Override
        Short512Vector op(Mask<Short, Shapes.S512Bit> o, FOp f) {
            short[] res = new short[length()];
            boolean[] mbits = ((Short512Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Short512Vector(res);
        }

        // Factories

        @Override
        public Short512Mask maskFromValues(boolean... bits) {
            return new Short512Mask(bits);
        }

        @Override
        public Short512Mask maskFromArray(boolean[] bits, int i) {
            return new Short512Mask(bits, i);
        }

        @Override
        public Short512Shuffle shuffleFromValues(int... ixs) {
            return new Short512Shuffle(ixs);
        }

        @Override
        public Short512Shuffle shuffleFromArray(int[] ixs, int i) {
            return new Short512Shuffle(ixs, i);
        }

        @Override
        @ForceInline
        public Short512Vector zero() {
            return VectorIntrinsics.broadcastCoerced(Short512Vector.class, short.class, LENGTH,
                                                     0,
                                                     (z -> ZERO));
        }

        @Override
        @ForceInline
        public Short512Vector broadcast(short e) {
            return VectorIntrinsics.broadcastCoerced(
                Short512Vector.class, short.class, LENGTH,
                e,
                ((long bits) -> SPECIES.op(i -> (short)bits)));
        }

        @Override
        @ForceInline
        public Short512Mask maskAllTrue() {
            return VectorIntrinsics.broadcastCoerced(Short512Mask.class, short.class, LENGTH,
                                                     (short)-1,
                                                     (z -> Short512Mask.TRUE_MASK));
        }

        @Override
        @ForceInline
        public Short512Mask maskAllFalse() {
            return VectorIntrinsics.broadcastCoerced(Short512Mask.class, short.class, LENGTH,
                                                     0,
                                                     (z -> Short512Mask.FALSE_MASK));
        }

        @Override
        @ForceInline
        public Short512Vector fromArray(short[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Short512Vector) VectorIntrinsics.load(Short512Vector.class, short.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((short[]) arr, idx));
        }

        @Override
        @ForceInline
        public Short512Vector fromArray(short[] a, int ax, Mask<Short, Shapes.S512Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }
    }
}
