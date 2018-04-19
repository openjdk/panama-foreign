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
final class Byte512Vector extends ByteVector<Shapes.S512Bit> {
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
    Byte512Vector uOp(Mask<Byte, Shapes.S512Bit> o, FUnOp f) {
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
    Byte512Vector bOp(Vector<Byte, Shapes.S512Bit> o, FBinOp f) {
        byte[] res = new byte[length()];
        byte[] vec1 = this.getElements();
        byte[] vec2 = ((Byte512Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Byte512Vector(res);
    }

    @Override
    Byte512Vector bOp(Vector<Byte, Shapes.S512Bit> o1, Mask<Byte, Shapes.S512Bit> o2, FBinOp f) {
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
    Byte512Vector tOp(Vector<Byte, Shapes.S512Bit> o1, Vector<Byte, Shapes.S512Bit> o2, FTriOp f) {
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
    Byte512Vector tOp(Vector<Byte, Shapes.S512Bit> o1, Vector<Byte, Shapes.S512Bit> o2, Mask<Byte, Shapes.S512Bit> o3, FTriOp f) {
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

    // Binary operations with scalars

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> add(byte o) {
        return add(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> add(byte o, Mask<Byte,Shapes.S512Bit> m) {
        return add(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> addSaturate(byte o) {
        return addSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> addSaturate(byte o, Mask<Byte,Shapes.S512Bit> m) {
        return addSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> sub(byte o) {
        return sub(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> sub(byte o, Mask<Byte,Shapes.S512Bit> m) {
        return sub(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> subSaturate(byte o) {
        return subSaturate(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> subSaturate(byte o, Mask<Byte,Shapes.S512Bit> m) {
        return subSaturate(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> mul(byte o) {
        return mul(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> mul(byte o, Mask<Byte,Shapes.S512Bit> m) {
        return mul(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> min(byte o) {
        return min(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> max(byte o) {
        return max(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S512Bit> equal(byte o) {
        return equal(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S512Bit> notEqual(byte o) {
        return notEqual(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S512Bit> lessThan(byte o) {
        return lessThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S512Bit> lessThanEq(byte o) {
        return lessThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S512Bit> greaterThan(byte o) {
        return greaterThan(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public Mask<Byte, Shapes.S512Bit> greaterThanEq(byte o) {
        return greaterThanEq(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> blend(byte o, Mask<Byte,Shapes.S512Bit> m) {
        return blend(SPECIES.broadcast(o), m);
    }


    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> and(byte o) {
        return and(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> and(byte o, Mask<Byte,Shapes.S512Bit> m) {
        return and(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> or(byte o) {
        return or(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> or(byte o, Mask<Byte,Shapes.S512Bit> m) {
        return or(SPECIES.broadcast(o), m);
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> xor(byte o) {
        return xor(SPECIES.broadcast(o));
    }

    @Override
    @ForceInline
    public ByteVector<Shapes.S512Bit> xor(byte o, Mask<Byte,Shapes.S512Bit> m) {
        return xor(SPECIES.broadcast(o), m);
    }


    // Unary operations



    @Override
    @ForceInline
    public Byte512Vector not() {
        return (Byte512Vector) VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Byte512Vector.class, byte.class, LENGTH,
            this,
            v1 -> ((Byte512Vector)v1).uOp((i, a) -> (byte) ~a));
    }
    // Binary operations

    @Override
    @ForceInline
    public Byte512Vector add(Vector<Byte,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return (Byte512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte512Vector)v1).bOp(v2, (i, a, b) -> (byte)(a + b)));
    }

    @Override
    @ForceInline
    public Byte512Vector sub(Vector<Byte,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return (Byte512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte512Vector)v1).bOp(v2, (i, a, b) -> (byte)(a - b)));
    }

    @Override
    @ForceInline
    public Byte512Vector mul(Vector<Byte,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return (Byte512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte512Vector)v1).bOp(v2, (i, a, b) -> (byte)(a * b)));
    }

    @Override
    @ForceInline
    public Byte512Vector min(Vector<Byte,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return (Byte512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte512Vector)v1).bOp(v2, (i, a, b) -> (byte) ((a < b) ? a : b)));
    }

    @Override
    @ForceInline
    public Byte512Vector max(Vector<Byte,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return (Byte512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte512Vector)v1).bOp(v2, (i, a, b) -> (byte) ((a > b) ? a : b)));
        }



    @Override
    @ForceInline
    public Byte512Vector and(Vector<Byte,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return (Byte512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte512Vector)v1).bOp(v2, (i, a, b) -> (byte)(a & b)));
    }

    @Override
    @ForceInline
    public Byte512Vector or(Vector<Byte,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return (Byte512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte512Vector)v1).bOp(v2, (i, a, b) -> (byte)(a | b)));
    }

    @Override
    @ForceInline
    public Byte512Vector xor(Vector<Byte,Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;
        return (Byte512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Byte512Vector.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> ((Byte512Vector)v1).bOp(v2, (i, a, b) -> (byte)(a ^ b)));
    }

    @Override
    @ForceInline
    public Byte512Vector and(Vector<Byte,Shapes.S512Bit> v, Mask<Byte, Shapes.S512Bit> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector or(Vector<Byte,Shapes.S512Bit> v, Mask<Byte, Shapes.S512Bit> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector xor(Vector<Byte,Shapes.S512Bit> v, Mask<Byte, Shapes.S512Bit> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Byte512Vector shiftL(int s) {
        return (Byte512Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Byte512Vector.class, byte.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (byte) (a << i)));
    }

    @Override
    @ForceInline
    public Byte512Vector shiftR(int s) {
        return (Byte512Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Byte512Vector.class, byte.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (byte) (a >>> i)));
    }

    @Override
    @ForceInline
    public Byte512Vector aShiftR(int s) {
        return (Byte512Vector) VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Byte512Vector.class, byte.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (byte) (a >> i)));
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

    // Memory operations

    @Override
    @ForceInline
    public void intoArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Byte512Vector.class, byte.class, LENGTH,
                               a, ix, this,
                               (arr, idx, v) -> v.forEach((i, a_) -> ((byte[])arr)[idx + i] = a_));
    }

    @Override
    @ForceInline
    public void intoArray(byte[] a, int ax, Mask<Byte, Shapes.S512Bit> m) {
        // TODO: use better default impl: forEach(m, (i, a_) -> a[ax + i] = a_);
        Byte512Vector oldVal = SPECIES.fromArray(a, ax);
        Byte512Vector newVal = oldVal.blend(this, m);
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

        Byte512Vector that = (Byte512Vector) o;
        return Arrays.equals(this.getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Byte512Mask bTest(Vector<Byte, Shapes.S512Bit> o, FBinTest f) {
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
    public Byte512Mask equal(Vector<Byte, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return (Byte512Mask) VectorIntrinsics.compare(
            BT_eq, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Byte512Mask notEqual(Vector<Byte, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return (Byte512Mask) VectorIntrinsics.compare(
            BT_ne, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Byte512Mask lessThan(Vector<Byte, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return (Byte512Mask) VectorIntrinsics.compare(
            BT_lt, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Byte512Mask lessThanEq(Vector<Byte, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return (Byte512Mask) VectorIntrinsics.compare(
            BT_le, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Byte512Mask greaterThan(Vector<Byte, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return (Byte512Mask) VectorIntrinsics.compare(
            BT_gt, Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Byte512Mask greaterThanEq(Vector<Byte, Shapes.S512Bit> o) {
        Objects.requireNonNull(o);
        Byte512Vector v = (Byte512Vector)o;

        return (Byte512Mask) VectorIntrinsics.compare(
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
    void forEach(Mask<Byte, Shapes.S512Bit> o, FUnCon f) {
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
    public Byte512Vector shuffle(Vector<Byte, Shapes.S512Bit> o, Shuffle<Byte, Shapes.S512Bit> s) {
        Byte512Vector v = (Byte512Vector) o;
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
    public Byte512Vector swizzle(Shuffle<Byte, Shapes.S512Bit> s) {
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
    @ForceInline
    public Byte512Vector blend(Vector<Byte, Shapes.S512Bit> o1, Mask<Byte, Shapes.S512Bit> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Byte512Vector v = (Byte512Vector)o1;
        Byte512Mask   m = (Byte512Mask)o2;

        return (Byte512Vector) VectorIntrinsics.blend(
            Byte512Vector.class, Byte512Mask.class, byte.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public byte get(int i) {
        byte[] vec = getElements();
        return vec[i];
    }

    @Override
    public Byte512Vector with(int i, byte e) {
        byte[] res = vec.clone();
        res[i] = e;
        return new Byte512Vector(res);
    }

    // Mask

    static final class Byte512Mask extends AbstractMask<Byte, Shapes.S512Bit> {
        static final Byte512Mask TRUE_MASK = new Byte512Mask(true);
        static final Byte512Mask FALSE_MASK = new Byte512Mask(false);

        // FIXME: was temporarily put here to simplify rematerialization support in the JVM
        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Byte512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Byte512Mask(boolean[] bits, int i) {
            this.bits = Arrays.copyOfRange(bits, i, i + species().length());
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
        Byte512Mask bOp(Mask<Byte, Shapes.S512Bit> o, MBinOp f) {
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
        public Byte512Vector toVector() {
            byte[] res = new byte[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = (byte) (bits[i] ? -1 : 0);
            }
            return new Byte512Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <Z> Mask<Z, Shapes.S512Bit> rebracket(Species<Z, Shapes.S512Bit> species) {
            Objects.requireNonNull(species);
            // TODO: check proper element type
            return VectorIntrinsics.reinterpret(
                Byte512Mask.class, byte.class, LENGTH,
                species.elementType(), species.length(), this,
                (m, t) -> m.reshape(species)
            );
        }

        // Unary operations

        //Mask<E, S> not();

        // Binary operations

        @Override
        @ForceInline
        public Byte512Mask and(Mask<Byte,Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            Byte512Mask m = (Byte512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Byte512Mask.class, byte.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Byte512Mask or(Mask<Byte,Shapes.S512Bit> o) {
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
                                         (m1, m2) -> super.anyTrue());
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(COND_carrySet, Byte512Mask.class, byte.class, LENGTH,
                                         this, species().maskAllTrue(),
                                         (m1, m2) -> super.allTrue());
        }
    }

    // Shuffle

    static final class Byte512Shuffle extends AbstractShuffle<Byte, Shapes.S512Bit> {
        static final IntVector.IntSpecies<Shapes.S512Bit> INT_SPECIES = IntVector.speciesInstance(Shapes.S_512_BIT);

        public Byte512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Byte512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        @Override
        public Byte512Species species() {
            return SPECIES;
        }

        @Override
        public IntVector.IntSpecies<Shapes.S512Bit> intSpecies() {
            return INT_SPECIES;
        }
    }

    // Species

    @Override
    public Byte512Species species() {
        return SPECIES;
    }

    static final class Byte512Species extends ByteSpecies<Shapes.S512Bit> {
        static final int BIT_SIZE = Shapes.S_512_BIT.bitSize();

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
        public Shapes.S512Bit shape() {
            return Shapes.S_512_BIT;
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
        Byte512Vector op(Mask<Byte, Shapes.S512Bit> o, FOp f) {
            byte[] res = new byte[length()];
            boolean[] mbits = ((Byte512Mask)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return new Byte512Vector(res);
        }

        // Factories

        @Override
        public Byte512Mask maskFromValues(boolean... bits) {
            return new Byte512Mask(bits);
        }

        @Override
        public Byte512Mask maskFromArray(boolean[] bits, int i) {
            return new Byte512Mask(bits, i);
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
        public Byte512Shuffle shuffleFromVector(Vector<Integer, Shapes.S512Bit> v) {
            int[] a = ((IntVector<Shapes.S512Bit>) v).toArray();
            return new Byte512Shuffle(a, 0);
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
        public Byte512Vector fromArray(byte[] a, int ix) {
            Objects.requireNonNull(a);
            ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
            return (Byte512Vector) VectorIntrinsics.load(Byte512Vector.class, byte.class, LENGTH,
                                                        a, ix,
                                                        (arr, idx) -> super.fromArray((byte[]) arr, idx));
        }

        @Override
        @ForceInline
        public Byte512Vector fromArray(byte[] a, int ax, Mask<Byte, Shapes.S512Bit> m) {
            return zero().blend(fromArray(a, ax), m); // TODO: use better default impl: op(m, i -> a[ax + i]);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F> Byte512Vector rebracket(Vector<F, Shapes.S512Bit> o) {
            Objects.requireNonNull(o);
            if (o.elementType() == byte.class) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte512Vector.class, byte.class, so.length(),
                    byte.class, LENGTH, so,
                    (v, t) -> (Byte512Vector)reshape(v)
                );
            } else if (o.elementType() == short.class) {
                Short512Vector so = (Short512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Short512Vector.class, short.class, so.length(),
                    byte.class, LENGTH, so,
                    (v, t) -> (Byte512Vector)reshape(v)
                );
            } else if (o.elementType() == int.class) {
                Int512Vector so = (Int512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Int512Vector.class, int.class, so.length(),
                    byte.class, LENGTH, so,
                    (v, t) -> (Byte512Vector)reshape(v)
                );
            } else if (o.elementType() == long.class) {
                Long512Vector so = (Long512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Long512Vector.class, long.class, so.length(),
                    byte.class, LENGTH, so,
                    (v, t) -> (Byte512Vector)reshape(v)
                );
            } else if (o.elementType() == float.class) {
                Float512Vector so = (Float512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Float512Vector.class, float.class, so.length(),
                    byte.class, LENGTH, so,
                    (v, t) -> (Byte512Vector)reshape(v)
                );
            } else if (o.elementType() == double.class) {
                Double512Vector so = (Double512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Double512Vector.class, double.class, so.length(),
                    byte.class, LENGTH, so,
                    (v, t) -> (Byte512Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <T extends Shape> Byte512Vector resize(Vector<Byte, T> o) {
            Objects.requireNonNull(o);
            if (o.bitSize() == 64) {
                Byte64Vector so = (Byte64Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte64Vector.class, byte.class, so.length(),
                    byte.class, LENGTH, so,
                    (v, t) -> (Byte512Vector)reshape(v)
                );
            } else if (o.bitSize() == 128) {
                Byte128Vector so = (Byte128Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte128Vector.class, byte.class, so.length(),
                    byte.class, LENGTH, so,
                    (v, t) -> (Byte512Vector)reshape(v)
                );
            } else if (o.bitSize() == 256) {
                Byte256Vector so = (Byte256Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte256Vector.class, byte.class, so.length(),
                    byte.class, LENGTH, so,
                    (v, t) -> (Byte512Vector)reshape(v)
                );
            } else if (o.bitSize() == 512) {
                Byte512Vector so = (Byte512Vector)o;
                return VectorIntrinsics.reinterpret(
                    Byte512Vector.class, byte.class, so.length(),
                    byte.class, LENGTH, so,
                    (v, t) -> (Byte512Vector)reshape(v)
                );
            } else {
                throw new InternalError("Unimplemented size");
            }
        }
    }
}
