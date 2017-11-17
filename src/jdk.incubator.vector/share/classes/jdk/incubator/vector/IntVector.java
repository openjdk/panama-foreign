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
import java.nio.IntBuffer;

@SuppressWarnings("cast")
public abstract class IntVector<S extends Vector.Shape<Vector<?,?>>> implements Vector<Integer,S> {

    IntVector() {}

    // Unary operator

    interface FUnOp {
        int apply(int i, int a);
    }

    abstract IntVector<S> uOp(FUnOp f);

    abstract IntVector<S> uOp(Mask<Integer, S> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        int apply(int i, int a, int b);
    }

    abstract IntVector<S> bOp(Vector<Integer,S> o, FBinOp f);

    abstract IntVector<S> bOp(Vector<Integer,S> o, Mask<Integer, S> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        int apply(int i, int a, int b, int c);
    }

    abstract IntVector<S> tOp(Vector<Integer,S> o1, Vector<Integer,S> o2, FTriOp f);

    abstract IntVector<S> tOp(Vector<Integer,S> o1, Vector<Integer,S> o2, Mask<Integer, S> m, FTriOp f);

    // Reduction operator

    abstract int rOp(int v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, int a, int b);
    }

    abstract Mask<Integer, S> bTest(Vector<Integer,S> o, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, int a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Integer, S> m, FUnCon f);

    //

    @HotSpotIntrinsicCandidate
    @Override
    public IntVector<S> add(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) (a + b));
    }

    @Override
    public IntVector<S> add(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) (a + b));
    }

    @Override
    public IntVector<S> addSaturate(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    @Override
    public IntVector<S> addSaturate(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    @HotSpotIntrinsicCandidate
    @Override
    public IntVector<S> sub(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) (a - b));
    }

    @Override
    public IntVector<S> sub(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) (a - b));
    }

    @Override
    public IntVector<S> subSaturate(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) ((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b));
    }

    @Override
    public IntVector<S> subSaturate(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) ((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b));
    }

    @HotSpotIntrinsicCandidate
    @Override
    public IntVector<S> mul(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) (a * b));
    }

    @Override
    public IntVector<S> mul(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) (a * b));
    }

    @HotSpotIntrinsicCandidate
    @Override
    public IntVector<S> div(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) (a / b));
    }

    @Override
    public IntVector<S> div(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) (a / b));
    }

    @Override
    public IntVector<S> neg() {
        return uOp((i, a) -> (int) (-a));
    }

    @Override
    public IntVector<S> neg(Mask<Integer, S> m) {
        return uOp(m, (i, a) -> (int) (-a));
    }

    @Override
    public IntVector<S> abs() {
        return uOp((i, a) -> (int) Math.abs(a));
    }

    @Override
    public IntVector<S> abs(Mask<Integer, S> m) {
        return uOp(m, (i, a) -> (int) Math.abs(a));
    }

    @Override
    public IntVector<S> min(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (a <= b) ? a : b);
    }

    @Override
    public IntVector<S> max(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (a >= b) ? a : b);
    }

    @HotSpotIntrinsicCandidate
    @Override
    public Mask<Integer, S> equal(Vector<Integer,S> o) {
        return bTest(o, (i, a, b) -> a == b);
    }

    @Override
    public Mask<Integer, S> notEqual(Vector<Integer,S> o) {
        return bTest(o, (i, a, b) -> a != b);
    }

    @HotSpotIntrinsicCandidate
    @Override
    public Mask<Integer, S> lessThan(Vector<Integer,S> o) {
        return bTest(o, (i, a, b) -> a < b);
    }

    @Override
    public Mask<Integer, S> lessThanEq(Vector<Integer,S> o) {
        return bTest(o, (i, a, b) -> a <= b);
    }

    @Override
    public Mask<Integer, S> greaterThan(Vector<Integer,S> o) {
        return bTest(o, (i, a, b) -> a > b);
    }

    @Override
    public Mask<Integer, S> greaterThanEq(Vector<Integer,S> o) {
        return bTest(o, (i, a, b) -> a >= b);
    }

    @HotSpotIntrinsicCandidate
    @Override
    public IntVector<S> blend(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, (i, a, b) -> m.getElement(i) ? a : b);
    }

    public IntVector<S> addExact(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> Math.addExact(a, b));
    }

    public IntVector<S> addExact(Vector<Integer,S> o, Mask<Integer,S> m) {
        return bOp(o, m, (i, a, b) -> Math.addExact(a, b));
    }

    public IntVector<S> decrementExact() {
        return uOp((i, a) -> Math.decrementExact(a));
    }

    public IntVector<S> decrementExact(Vector<Integer,S> o, Mask<Integer,S> m) {
        return uOp(m, (i, a) -> Math.decrementExact(a));
    }

    public IntVector<S> incrementExact(Vector<Integer,S> o) {
        return uOp((i, a) -> Math.incrementExact(a));
    }

    public IntVector<S> incrementExact(Vector<Integer,S> o, Mask<Integer,S> m) {
        return uOp(m, (i, a) -> Math.incrementExact(a));
    }

    public IntVector<S> multiplyExact(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> Math.multiplyExact(a, b));
    }

    public IntVector<S> multiplyExact(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> Math.multiplyExact(a, b));
    }

    public IntVector<S> negateExact() {
        return uOp((i, a) -> Math.negateExact(a));
    }

    public IntVector<S> negateExact(Mask<Integer, S> m) {
        return uOp(m, (i, a) -> Math.negateExact(a));
    }

    public IntVector<S> subtractExtract(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> Math.subtractExact(a, b));
    }

    public IntVector<S> subtractExtract(Vector<Integer,S> o, Mask<Integer,S> m) {
        return bOp(o, m, (i, a, b) -> Math.subtractExact(a, b));
    }
    // @@@ Shape specific
    // Int256Vector -> Long512Vector
    /*

    public IntVector<S> multiplyFull(Vector<Integer,S> o) {
        throw new UnsupportedOperationException("multiplyFull not supported on Float");
    }

    public IntVector<S> multiplyFull(Vector<Integer,S> o, Mask<Integer, S> m) {
        throw new UnsupportedOperationException("multiplyFull not supported on Float");
    }
    */

    public IntVector<S> and(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) (a & b));
    }

    public IntVector<S> and(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) (a & b));
    }

    public IntVector<S> or(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) (a | b));
    }

    public IntVector<S> or(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) (a | b));
    }

    public IntVector<S> xor(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) (a ^ b));
    }

    public IntVector<S> xor(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) (a ^ b));
    }

    public IntVector<S> not() {
        return uOp((i, a) -> (int) (~a));
    }

    public IntVector<S> not(Mask<Integer, S> m) {
        return uOp(m, (i, a) -> (int) (~a));
    }

    public IntVector<S> floorDiv(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) (a / b));
    }

    public IntVector<S> floorDiv(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) (a / b));
    }

    public IntVector<S> floorMod(Vector<Integer,S> o) {
        return bOp(o, (i, a, b) -> (int) (a % b));
    }

    public IntVector<S> floorMod(Vector<Integer,S> o, Mask<Integer, S> m) {
        return bOp(o, m, (i, a, b) -> (int) (a % b));
    }

    public IntVector<S> shiftL(int s) {
        return uOp((i, a) -> (int) (a << s));
    }

    public IntVector<S> shiftR(int s) {
        return uOp((i, a) -> (int) (a >> s));
    }

    public IntVector<S> aShiftR(int s) {
        return uOp((i, a) -> (int) (a >>> s));
    }

    public IntVector<S> rotateL(int j) {
        return uOp((i, a) -> (int) Integer.rotateLeft(a, j));
    }

    public IntVector<S> rotateR(int j) {
        return uOp((i, a) -> (int) Integer.rotateRight(a, j));
    }

    @Override
    public void intoByteArray(byte[] a, int ix) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb);
    }

    @Override
    public void intoByteArray(byte[] a, int ix, Mask<Integer, S> m) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb, m);
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb) {
        IntBuffer fb = bb.asIntBuffer();
        forEach((i, a) -> fb.put(a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, Mask<Integer, S> m) {
        IntBuffer fb = bb.asIntBuffer();
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
        IntBuffer fb = bb.asIntBuffer();
        forEach((i, a) -> fb.put(i, a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Integer, S> m) {
        bb = bb.duplicate().position(ix);
        IntBuffer fb = bb.asIntBuffer();
        forEach(m, (i, a) -> fb.put(i, a));
    }


    // Type specific horizontal reductions

    @HotSpotIntrinsicCandidate
    public int addAll() {
        return rOp((int) 0, (i, a, b) -> (int) (a + b));
    }

    public int subAll() {
        return rOp((int) 0, (i, a, b) -> (int) (a - b));
    }

    public int mulAll() {
        return rOp((int) 1, (i, a, b) -> (int) (a * b));
    }

    public int minAll() {
        return rOp(Integer.MAX_VALUE, (i, a, b) -> a > b ? b : a);
    }

    public int maxAll() {
        return rOp(Integer.MIN_VALUE, (i, a, b) -> a < b ? b : a);
    }

    public int orAll() {
        return rOp((int) 0, (i, a, b) -> (int) (a | b));
    }

    public int andAll() {
        return rOp((int) -1, (i, a, b) -> (int) (a & b));
    }

    public int xorAll() {
        return rOp((int) 0, (i, a, b) -> (int) (a ^ b));
    }

    // Type conversions

    @Override
    public <F> Vector<F,S> cast(Class<F> type) {
        return cast(type, shape());
    }

    // Type specific accessors

    public abstract int get(int i);

    public abstract IntVector<S> with(int i, int e);

    // Type specific extractors

    @HotSpotIntrinsicCandidate
    public void intoArray(int[] a, int ix) {
        forEach((i, a_) -> a[ix + i] = a_);
    }

    public void intoArray(int[] a, int ix, Mask<Integer, S> m) {
        forEach(m, (i, a_) -> a[ix + i] = a_);
    }

    // Species

    @Override
    public abstract IntSpecies<S> species();

    public static abstract class IntSpecies<S extends Vector.Shape<Vector<?,?>>> implements Vector.Species<Integer, S> {
        interface FOp {
            int apply(int i);
        }

        abstract IntVector<S> op(FOp f);

        abstract IntVector<S> op(Mask<Integer, S> m, FOp f);

        // Factories

        @HotSpotIntrinsicCandidate
        @Override
        public IntVector<S> zero() {
            return op(i -> 0);
        }

        @HotSpotIntrinsicCandidate
        public IntVector<S> broadcast(int e) {
            return op(i -> e);
        }

        public IntVector<S> single(int e) {
            return op(i -> i == 0 ? e : (int) 0);
        }

        @HotSpotIntrinsicCandidate
        public IntVector<S> fromArray(int[] a, int ix) {
            return op(i -> a[ix + i]);
        }

        public IntVector<S> fromArray(int[] a, int ix, Mask<Integer, S> m) {
            return op(m, i -> a[ix + i]);
        }

        @Override
        public IntVector<S> fromByteArray(byte[] a, int ix) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb);
        }

        @Override
        public IntVector<S> fromByteArray(byte[] a, int ix, Mask<Integer, S> m) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb, m);
        }

        @Override
        public IntVector<S> fromByteBuffer(ByteBuffer bb) {
            IntBuffer fb = bb.asIntBuffer();
            return op(i -> fb.get());
        }

        @Override
        public IntVector<S> fromByteBuffer(ByteBuffer bb, Mask<Integer, S> m) {
            IntBuffer fb = bb.asIntBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (int) 0;
                }
            });
        }

        @Override
        public IntVector<S> fromByteBuffer(ByteBuffer bb, int ix) {
            bb = bb.duplicate().position(ix);
            IntBuffer fb = bb.asIntBuffer();
            return op(i -> fb.get(i));
        }

        @Override
        public IntVector<S> fromByteBuffer(ByteBuffer bb, int ix, Mask<Integer, S> m) {
            bb = bb.duplicate().position(ix);
            IntBuffer fb = bb.asIntBuffer();
            return op(m, i -> fb.get(i));
        }
    }
}
