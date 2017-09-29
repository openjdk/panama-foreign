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
import java.nio.LongBuffer;

@SuppressWarnings("cast")
public abstract class LongVector<S extends Vector.Shape<Vector<?,?>>> implements Vector<Long,S> {

    LongVector() {}

    // Unary operator

    interface FUnOp {
        long apply(int i, long a);
    }

    abstract LongVector<S> uOp(FUnOp f);

    abstract LongVector<S> uOp(Mask<Long, S> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        long apply(int i, long a, long b);
    }

    abstract LongVector<S> bOp(Vector<Long,S> o, FBinOp f);

    abstract LongVector<S> bOp(Vector<Long,S> o, Mask<Long, S> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        long apply(int i, long a, long b, long c);
    }

    abstract LongVector<S> tOp(Vector<Long,S> o1, Vector<Long,S> o2, FTriOp f);

    abstract LongVector<S> tOp(Vector<Long,S> o1, Vector<Long,S> o2, Mask<Long, S> m, FTriOp f);

    // Reduction operator

    abstract long rOp(long v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, long a, long b);
    }

    abstract Mask<Long, S> bTest(Vector<Long,S> o, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, long a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Long, S> m, FUnCon f);

    //

    @Override
    public LongVector<S> add(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a + b));
    }

    @Override
    public LongVector<S> add(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a + b));
    }

    @Override
    public LongVector<S> addSaturate(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    @Override
    public LongVector<S> addSaturate(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    @Override
    public LongVector<S> sub(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a - b));
    }

    @Override
    public LongVector<S> sub(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a - b));
    }

    @Override
    public LongVector<S> subSaturate(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) ((a >= Long.MIN_VALUE || Long.MIN_VALUE + b > a) ? Long.MAX_VALUE : a - b));
    }

    @Override
    public LongVector<S> subSaturate(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) ((a >= Long.MIN_VALUE || Long.MIN_VALUE + b > a) ? Long.MAX_VALUE : a - b));
    }

    @Override
    public LongVector<S> mul(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a * b));
    }

    @Override
    public LongVector<S> mul(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a * b));
    }

    @Override
    public LongVector<S> div(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a / b));
    }

    @Override
    public LongVector<S> div(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a / b));
    }

    @Override
    public LongVector<S> neg() {
        return uOp((i, a) -> (long) (-a));
    }

    @Override
    public LongVector<S> neg(Mask<Long, S> m) {
        return uOp(m, (i, a) -> (long) (-a));
    }

    @Override
    public LongVector<S> abs() {
        return uOp((i, a) -> (long) Math.abs(a));
    }

    @Override
    public LongVector<S> abs(Mask<Long, S> m) {
        return uOp(m, (i, a) -> (long) Math.abs(a));
    }

    @Override
    public LongVector<S> min(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (a <= b) ? a : b);
    }

    @Override
    public LongVector<S> max(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (a >= b) ? a : b);
    }

    @Override
    public Mask<Long, S> equal(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a == b);
    }

    @Override
    public Mask<Long, S> notEqual(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a != b);
    }

    @Override
    public Mask<Long, S> lessThan(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a < b);
    }

    @Override
    public Mask<Long, S> lessThanEq(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a <= b);
    }

    @Override
    public Mask<Long, S> greaterThan(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a > b);
    }

    @Override
    public Mask<Long, S> greaterThanEq(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a >= b);
    }

    @Override
    public LongVector<S> blend(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, (i, a, b) -> m.getElement(i) ? a : b);
    }

    public LongVector<S> addExact(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> Math.addExact(a, b));
    }

    public LongVector<S> addExact(Vector<Long,S> o, Mask<Long,S> m) {
        return bOp(o, m, (i, a, b) -> Math.addExact(a, b));
    }

    public LongVector<S> decrementExact() {
        return uOp((i, a) -> Math.decrementExact(a));
    }

    public LongVector<S> decrementExact(Vector<Long,S> o, Mask<Long,S> m) {
        return uOp(m, (i, a) -> Math.decrementExact(a));
    }

    public LongVector<S> incrementExact(Vector<Long,S> o) {
        return uOp((i, a) -> Math.incrementExact(a));
    }

    public LongVector<S> incrementExact(Vector<Long,S> o, Mask<Long,S> m) {
        return uOp(m, (i, a) -> Math.incrementExact(a));
    }

    public LongVector<S> multiplyExact(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> Math.multiplyExact(a, b));
    }

    public LongVector<S> multiplyExact(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> Math.multiplyExact(a, b));
    }

    public LongVector<S> negateExact() {
        return uOp((i, a) -> Math.negateExact(a));
    }

    public LongVector<S> negateExact(Mask<Long, S> m) {
        return uOp(m, (i, a) -> Math.negateExact(a));
    }

    public LongVector<S> subtractExtract(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> Math.subtractExact(a, b));
    }

    public LongVector<S> subtractExtract(Vector<Long,S> o, Mask<Long,S> m) {
        return bOp(o, m, (i, a, b) -> Math.subtractExact(a, b));
    }
    /*
    // @@@ Shape specific
    // long,int-> long : S, S / 2 -> S
    // Long512Vector, Int256Vector -> Long512Vector

    public LongVector<S> multiplyExact(Vector<Long,S> o) {
        throw new UnsupportedOperationException("multiplyExact not supported on Float");
    }

    public LongVector<S> multiplyExact(Vector<Long,S> o, Mask<Long, S> m) {
        throw new UnsupportedOperationException("multiplyExact not supported on Float");
    }

    // @@@ Shape specific
    // long->int
    // Long512Vector -> Int256Vector

    public Vector<Integer, Shapes.S128Bit> toIntExact() {
        throw new UnsupportedOperationException("toIntExact not implemented.");
    }

    // Top 64 of 128 bits

    public LongVector<S> multiplyHigh(Vector<Long,S> o) {
        throw new UnsupportedOperationException("multiplyHigh not supported on Float");
    }

    public LongVector<S> multiplyHigh(Vector<Long,S> o, Mask<Long, S> m) {
        throw new UnsupportedOperationException("multiplyHigh not supported on Float");
    }
    */

    public LongVector<S> and(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a & b));
    }

    public LongVector<S> and(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a & b));
    }

    public LongVector<S> or(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a | b));
    }

    public LongVector<S> or(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a | b));
    }

    public LongVector<S> xor(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a ^ b));
    }

    public LongVector<S> xor(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a ^ b));
    }

    public LongVector<S> not() {
        return uOp((i, a) -> (long) (~a));
    }

    public LongVector<S> not(Mask<Long, S> m) {
        return uOp(m, (i, a) -> (long) (~a));
    }

    public LongVector<S> floorDiv(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a / b));
    }

    public LongVector<S> floorDiv(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a / b));
    }

    public LongVector<S> floorMod(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a % b));
    }

    public LongVector<S> floorMod(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a % b));
    }

    public LongVector<S> shiftL(int s) {
        return uOp((i, a) -> (long) (a << s));
    }

    public LongVector<S> shiftR(int s) {
        return uOp((i, a) -> (long) (a >> s));
    }

    public LongVector<S> aShiftR(int s) {
        return uOp((i, a) -> (long) (a >>> s));
    }

    public LongVector<S> rotateL(int j) {
        return uOp((i, a) -> (long) Long.rotateLeft(a, j));
    }

    public LongVector<S> rotateR(int j) {
        return uOp((i, a) -> (long) Long.rotateRight(a, j));
    }

    @Override
    public void intoByteArray(byte[] a, int ix) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb);
    }

    @Override
    public void intoByteArray(byte[] a, int ix, Mask<Long, S> m) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb, m);
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb) {
        LongBuffer fb = bb.asLongBuffer();
        forEach((i, a) -> fb.put(a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, Mask<Long, S> m) {
        LongBuffer fb = bb.asLongBuffer();
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
        LongBuffer fb = bb.asLongBuffer();
        forEach((i, a) -> fb.put(i, a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Long, S> m) {
        bb = bb.duplicate().position(ix);
        LongBuffer fb = bb.asLongBuffer();
        forEach(m, (i, a) -> fb.put(i, a));
    }


    // Type specific horizontal reductions

    public long sumAll() {
        return rOp((long) 0, (i, a, b) -> (long) (a + b));
    }

    public long subAll() {
        return rOp((long) 0, (i, a, b) -> (long) (a - b));
    }

    public long prodAll() {
        return rOp((long) 1, (i, a, b) -> (long) (a * b));
    }

    public long minAll() {
        return rOp(Long.MAX_VALUE, (i, a, b) -> a > b ? b : a);
    }

    public long maxAll() {
        return rOp(Long.MIN_VALUE, (i, a, b) -> a < b ? b : a);
    }

    public long orAll() {
        return rOp((long) 0, (i, a, b) -> (long) (a | b));
    }

    public long andAll() {
        return rOp((long) -1, (i, a, b) -> (long) (a & b));
    }

    public long xorAll() {
        return rOp((long) 0, (i, a, b) -> (long) (a ^ b));
    }

    // Type conversions

    @Override
    public <F> Vector<F,S> cast(Class<F> type) {
        return cast(type, shape());
    }

    // Type specific accessors

    public abstract long get(int i);

    public abstract LongVector<S> with(int i, long e);

    // Type specific extractors

    @HotSpotIntrinsicCandidate
    public void intoArray(long[] a, int ix) {
        forEach((i, a_) -> a[ix + i] = a_);
    }

    public void intoArray(long[] a, int ix, Mask<Long, S> m) {
        forEach(m, (i, a_) -> a[ix + i] = a_);
    }

    // Species

    @Override
    public abstract LongSpecies<S> species();

    public static abstract class LongSpecies<S extends Vector.Shape<Vector<?,?>>> implements Vector.Species<Long, S> {
        interface FOp {
            long apply(int i);
        }

        abstract LongVector<S> op(FOp f);

        abstract LongVector<S> op(Mask<Long, S> m, FOp f);

        // Factories

        @Override
        public LongVector<S> zero() {
            return op(i -> 0);
        }

        public LongVector<S> broadcast(long e) {
            return op(i -> e);
        }

        public LongVector<S> single(long e) {
            return op(i -> i == 0 ? e : (long) 0);
        }

        @HotSpotIntrinsicCandidate
        public LongVector<S> fromArray(long[] a, int ix) {
            return op(i -> a[ix + i]);
        }

        public LongVector<S> fromArray(long[] a, int ix, Mask<Long, S> m) {
            return op(m, i -> a[ix + i]);
        }

        @Override
        public LongVector<S> fromByteArray(byte[] a, int ix) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb);
        }

        @Override
        public LongVector<S> fromByteArray(byte[] a, int ix, Mask<Long, S> m) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb, m);
        }

        @Override
        public LongVector<S> fromByteBuffer(ByteBuffer bb) {
            LongBuffer fb = bb.asLongBuffer();
            return op(i -> fb.get());
        }

        @Override
        public LongVector<S> fromByteBuffer(ByteBuffer bb, Mask<Long, S> m) {
            LongBuffer fb = bb.asLongBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (long) 0;
                }
            });
        }

        @Override
        public LongVector<S> fromByteBuffer(ByteBuffer bb, int ix) {
            bb = bb.duplicate().position(ix);
            LongBuffer fb = bb.asLongBuffer();
            return op(i -> fb.get(i));
        }

        @Override
        public LongVector<S> fromByteBuffer(ByteBuffer bb, int ix, Mask<Long, S> m) {
            bb = bb.duplicate().position(ix);
            LongBuffer fb = bb.asLongBuffer();
            return op(m, i -> fb.get(i));
        }
    }
}
