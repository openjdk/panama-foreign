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

import jdk.internal.HotSpotIntrinsicCandidate;

import java.nio.ByteBuffer;

@SuppressWarnings("cast")
public abstract class ByteVector<S extends Vector.Shape<Vector<?,?>>> implements Vector<Byte,S> {

    ByteVector() {}

    // Unary operator

    interface FUnOp {
        byte apply(int i, byte a);
    }

    abstract ByteVector<S> uOp(FUnOp f);

    abstract ByteVector<S> uOp(Mask<Byte, S> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        byte apply(int i, byte a, byte b);
    }

    abstract ByteVector<S> bOp(Vector<Byte,S> o, FBinOp f);

    abstract ByteVector<S> bOp(Vector<Byte,S> o, Mask<Byte, S> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        byte apply(int i, byte a, byte b, byte c);
    }

    abstract ByteVector<S> tOp(Vector<Byte,S> o1, Vector<Byte,S> o2, FTriOp f);

    abstract ByteVector<S> tOp(Vector<Byte,S> o1, Vector<Byte,S> o2, Mask<Byte, S> m, FTriOp f);

    // Reduction operator

    abstract byte rOp(byte v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, byte a, byte b);
    }

    abstract Mask<Byte, S> bTest(Vector<Byte,S> o, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, byte a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Byte, S> m, FUnCon f);

    //

    @Override
    public ByteVector<S> add(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a + b));
    }

    @Override
    public ByteVector<S> add(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a + b));
    }

    @Override
    public ByteVector<S> addSaturate(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    @Override
    public ByteVector<S> addSaturate(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    @Override
    public ByteVector<S> sub(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a - b));
    }

    @Override
    public ByteVector<S> sub(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a - b));
    }

    @Override
    public ByteVector<S> subSaturate(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) ((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b));
    }

    @Override
    public ByteVector<S> subSaturate(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) ((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b));
    }

    @Override
    public ByteVector<S> mul(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a * b));
    }

    @Override
    public ByteVector<S> mul(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a * b));
    }

    @Override
    public ByteVector<S> div(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a / b));
    }

    @Override
    public ByteVector<S> div(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a / b));
    }

    @Override
    public ByteVector<S> neg() {
        return uOp((i, a) -> (byte) (-a));
    }

    @Override
    public ByteVector<S> neg(Mask<Byte, S> m) {
        return uOp(m, (i, a) -> (byte) (-a));
    }

    @Override
    public ByteVector<S> abs() {
        return uOp((i, a) -> (byte) Math.abs(a));
    }

    @Override
    public ByteVector<S> abs(Mask<Byte, S> m) {
        return uOp(m, (i, a) -> (byte) Math.abs(a));
    }

    @Override
    public ByteVector<S> min(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (a <= b) ? a : b);
    }

    @Override
    public ByteVector<S> max(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (a >= b) ? a : b);
    }

    @Override
    public Mask<Byte, S> equal(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a == b);
    }

    @Override
    public Mask<Byte, S> notEqual(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a != b);
    }

    @Override
    public Mask<Byte, S> lessThan(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a < b);
    }

    @Override
    public Mask<Byte, S> lessThanEq(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a <= b);
    }

    @Override
    public Mask<Byte, S> greaterThan(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a > b);
    }

    @Override
    public Mask<Byte, S> greaterThanEq(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a >= b);
    }

    @Override
    public ByteVector<S> blend(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, (i, a, b) -> m.getElement(i) ? a : b);
    }


    public ByteVector<S> and(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a & b));
    }

    public ByteVector<S> and(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a & b));
    }

    public ByteVector<S> or(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a | b));
    }

    public ByteVector<S> or(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a | b));
    }

    public ByteVector<S> xor(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a ^ b));
    }

    public ByteVector<S> xor(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a ^ b));
    }

    public ByteVector<S> not() {
        return uOp((i, a) -> (byte) (~a));
    }

    public ByteVector<S> not(Mask<Byte, S> m) {
        return uOp(m, (i, a) -> (byte) (~a));
    }

    public ByteVector<S> floorDiv(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a / b));
    }

    public ByteVector<S> floorDiv(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a / b));
    }

    public ByteVector<S> floorMod(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a % b));
    }

    public ByteVector<S> floorMod(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a % b));
    }

    public ByteVector<S> shiftL(int s) {
        return uOp((i, a) -> (byte) (a << s));
    }

    public ByteVector<S> shiftR(int s) {
        return uOp((i, a) -> (byte) (a >> s));
    }

    public ByteVector<S> aShiftR(int s) {
        return uOp((i, a) -> (byte) (a >>> s));
    }

    public ByteVector<S> rotateL(int j) {
        return uOp((i, a) -> (byte) Integer.rotateLeft(a, j));
    }

    public ByteVector<S> rotateR(int j) {
        return uOp((i, a) -> (byte) Integer.rotateRight(a, j));
    }

    @Override
    public void intoByteArray(byte[] a, int ix) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb);
    }

    @Override
    public void intoByteArray(byte[] a, int ix, Mask<Byte, S> m) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb, m);
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb) {
        ByteBuffer fb = bb;
        forEach((i, a) -> fb.put(a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, Mask<Byte, S> m) {
        ByteBuffer fb = bb;
        forEach((i, a) -> {
            if (m.getElement(i))
                fb.put(a);
            else
                fb.position(fb.position() + 1);
        });
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        bb = bb.duplicate().position(ix);
        ByteBuffer fb = bb;
        forEach((i, a) -> fb.put(i, a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Byte, S> m) {
        bb = bb.duplicate().position(ix);
        ByteBuffer fb = bb;
        forEach(m, (i, a) -> fb.put(i, a));
    }


    // Type specific horizontal reductions

    public byte sumAll() {
        return rOp((byte) 0, (i, a, b) -> (byte) (a + b));
    }

    public byte subAll() {
        return rOp((byte) 0, (i, a, b) -> (byte) (a - b));
    }

    public byte prodAll() {
        return rOp((byte) 1, (i, a, b) -> (byte) (a * b));
    }

    public byte minAll() {
        return rOp(Byte.MAX_VALUE, (i, a, b) -> a > b ? b : a);
    }

    public byte maxAll() {
        return rOp(Byte.MIN_VALUE, (i, a, b) -> a < b ? b : a);
    }

    public byte orAll() {
        return rOp((byte) 0, (i, a, b) -> (byte) (a | b));
    }

    public byte andAll() {
        return rOp((byte) -1, (i, a, b) -> (byte) (a & b));
    }

    public byte xorAll() {
        return rOp((byte) 0, (i, a, b) -> (byte) (a ^ b));
    }

    // Type conversions

    @Override
    public <F> Vector<F,S> cast(Class<F> type) {
        return cast(type, shape());
    }

    // Type specific accessors

    public abstract byte get(int i);

    public abstract ByteVector<S> with(int i, byte e);

    // Type specific extractors

    @HotSpotIntrinsicCandidate
    public void intoArray(byte[] a, int ix) {
        forEach((i, a_) -> a[ix + i] = a_);
    }

    public void intoArray(byte[] a, int ix, Mask<Byte, S> m) {
        forEach(m, (i, a_) -> a[ix + i] = a_);
    }

    // Species

    @Override
    public abstract ByteSpecies<S> species();

    public static abstract class ByteSpecies<S extends Vector.Shape<Vector<?,?>>> implements Vector.Species<Byte, S> {
        interface FOp {
            byte apply(int i);
        }

        abstract ByteVector<S> op(FOp f);

        abstract ByteVector<S> op(Mask<Byte, S> m, FOp f);

        // Factories

        @Override
        public ByteVector<S> zero() {
            return op(i -> 0);
        }

        public ByteVector<S> broadcast(byte e) {
            return op(i -> e);
        }

        public ByteVector<S> single(byte e) {
            return op(i -> i == 0 ? e : (byte) 0);
        }

        @HotSpotIntrinsicCandidate
        public ByteVector<S> fromArray(byte[] a, int ix) {
            return op(i -> a[ix + i]);
        }

        public ByteVector<S> fromArray(byte[] a, int ix, Mask<Byte, S> m) {
            return op(m, i -> a[ix + i]);
        }

        @Override
        public ByteVector<S> fromByteArray(byte[] a, int ix) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb);
        }

        @Override
        public ByteVector<S> fromByteArray(byte[] a, int ix, Mask<Byte, S> m) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb, m);
        }

        @Override
        public ByteVector<S> fromByteBuffer(ByteBuffer bb) {
            ByteBuffer fb = bb;
            return op(i -> fb.get());
        }

        @Override
        public ByteVector<S> fromByteBuffer(ByteBuffer bb, Mask<Byte, S> m) {
            ByteBuffer fb = bb;
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (byte) 0;
                }
            });
        }

        @Override
        public ByteVector<S> fromByteBuffer(ByteBuffer bb, int ix) {
            bb = bb.duplicate().position(ix);
            ByteBuffer fb = bb;
            return op(i -> fb.get(i));
        }

        @Override
        public ByteVector<S> fromByteBuffer(ByteBuffer bb, int ix, Mask<Byte, S> m) {
            bb = bb.duplicate().position(ix);
            ByteBuffer fb = bb;
            return op(m, i -> fb.get(i));
        }
    }
}
