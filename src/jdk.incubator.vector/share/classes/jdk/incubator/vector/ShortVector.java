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
import java.nio.ShortBuffer;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("cast")
public abstract class ShortVector<S extends Vector.Shape> implements Vector<Short,S> {

    ShortVector() {}

    // Unary operator

    interface FUnOp {
        short apply(int i, short a);
    }

    abstract ShortVector<S> uOp(FUnOp f);

    abstract ShortVector<S> uOp(Mask<Short, S> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        short apply(int i, short a, short b);
    }

    abstract ShortVector<S> bOp(Vector<Short,S> o, FBinOp f);

    abstract ShortVector<S> bOp(Vector<Short,S> o, Mask<Short, S> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        short apply(int i, short a, short b, short c);
    }

    abstract ShortVector<S> tOp(Vector<Short,S> o1, Vector<Short,S> o2, FTriOp f);

    abstract ShortVector<S> tOp(Vector<Short,S> o1, Vector<Short,S> o2, Mask<Short, S> m, FTriOp f);

    // Reduction operator

    abstract short rOp(short v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, short a, short b);
    }

    abstract Mask<Short, S> bTest(Vector<Short,S> o, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, short a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Short, S> m, FUnCon f);

    //

    @Override
    public ShortVector<S> add(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a + b));
    }

    public abstract ShortVector<S> add(short o);

    @Override
    public ShortVector<S> add(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a + b));
    }

    public abstract ShortVector<S> add(short o, Mask<Short, S> m);

    @Override
    public ShortVector<S> addSaturate(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract ShortVector<S> addSaturate(short o);

    @Override
    public ShortVector<S> addSaturate(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract ShortVector<S> addSaturate(short o, Mask<Short, S> m);

    @Override
    public ShortVector<S> sub(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a - b));
    }

    public abstract ShortVector<S> sub(short o);

    @Override
    public ShortVector<S> sub(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a - b));
    }

    public abstract ShortVector<S> sub(short o, Mask<Short, S> m);

    @Override
    public ShortVector<S> subSaturate(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) ((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b));
    }

    public abstract ShortVector<S> subSaturate(short o);

    @Override
    public ShortVector<S> subSaturate(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) ((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b));
    }

    public abstract ShortVector<S> subSaturate(short o, Mask<Short, S> m);

    @Override
    public ShortVector<S> mul(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a * b));
    }

    public abstract ShortVector<S> mul(short o);

    @Override
    public ShortVector<S> mul(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a * b));
    }

    public abstract ShortVector<S> mul(short o, Mask<Short, S> m);

    @Override
    public ShortVector<S> div(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a / b));
    }

    public abstract ShortVector<S> div(short o);

    @Override
    public ShortVector<S> div(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a / b));
    }

    public abstract ShortVector<S> div(short o, Mask<Short, S> m);

    @Override
    public ShortVector<S> neg() {
        return uOp((i, a) -> (short) (-a));
    }

    @Override
    public ShortVector<S> neg(Mask<Short, S> m) {
        return uOp(m, (i, a) -> (short) (-a));
    }

    @Override
    public ShortVector<S> abs() {
        return uOp((i, a) -> (short) Math.abs(a));
    }

    @Override
    public ShortVector<S> abs(Mask<Short, S> m) {
        return uOp(m, (i, a) -> (short) Math.abs(a));
    }

    @Override
    public ShortVector<S> min(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (a <= b) ? a : b);
    }

    public abstract ShortVector<S> min(short o);

    @Override
    public ShortVector<S> max(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (a >= b) ? a : b);
    }

    public abstract ShortVector<S> max(short o);

    @Override
    public Mask<Short, S> equal(Vector<Short,S> o) {
        return bTest(o, (i, a, b) -> a == b);
    }

    public abstract Mask<Short, S> equal(short o);

    @Override
    public Mask<Short, S> notEqual(Vector<Short,S> o) {
        return bTest(o, (i, a, b) -> a != b);
    }

    public abstract Mask<Short, S> notEqual(short o);

    @Override
    public Mask<Short, S> lessThan(Vector<Short,S> o) {
        return bTest(o, (i, a, b) -> a < b);
    }

    public abstract Mask<Short, S> lessThan(short o);

    @Override
    public Mask<Short, S> lessThanEq(Vector<Short,S> o) {
        return bTest(o, (i, a, b) -> a <= b);
    }

    public abstract Mask<Short, S> lessThanEq(short o);

    @Override
    public Mask<Short, S> greaterThan(Vector<Short,S> o) {
        return bTest(o, (i, a, b) -> a > b);
    }

    public abstract Mask<Short, S> greaterThan(short o);

    @Override
    public Mask<Short, S> greaterThanEq(Vector<Short,S> o) {
        return bTest(o, (i, a, b) -> a >= b);
    }

    public abstract Mask<Short, S> greaterThanEq(short o);

    @HotSpotIntrinsicCandidate
    @Override
    public ShortVector<S> blend(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, (i, a, b) -> m.getElement(i) ? b : a);
    }

    public abstract ShortVector<S> blend(short o, Mask<Short, S> m);


    public ShortVector<S> and(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a & b));
    }

    public abstract ShortVector<S> and(short o);

    public ShortVector<S> and(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a & b));
    }

    public abstract ShortVector<S> and(short o, Mask<Short, S> m);

    public ShortVector<S> or(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a | b));
    }

    public abstract ShortVector<S> or(short o);

    public ShortVector<S> or(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a | b));
    }

    public abstract ShortVector<S> or(short o, Mask<Short, S> m);

    public ShortVector<S> xor(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a ^ b));
    }

    public abstract ShortVector<S> xor(short o);

    public ShortVector<S> xor(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a ^ b));
    }

    public abstract ShortVector<S> xor(short o, Mask<Short, S> m);

    public ShortVector<S> not() {
        return uOp((i, a) -> (short) (~a));
    }

    public ShortVector<S> not(Mask<Short, S> m) {
        return uOp(m, (i, a) -> (short) (~a));
    }

    public ShortVector<S> floorDiv(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a / b));
    }

    public abstract ShortVector<S> floorDiv(short o);

    public ShortVector<S> floorDiv(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a / b));
    }

    public abstract ShortVector<S> floorDiv(short o, Mask<Short, S> m);

    public ShortVector<S> floorMod(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a % b));
    }

    public abstract ShortVector<S> floorMod(short o);

    public ShortVector<S> floorMod(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a % b));
    }

    public abstract ShortVector<S> floorMod(short o, Mask<Short, S> m);

    // logical shift left
    public ShortVector<S> shiftL(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a << b));
    }

    public ShortVector<S> shiftL(int s) {
        return uOp((i, a) -> (short) (a << s));
    }

    public ShortVector<S> shiftL(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a << b));
    }

    public ShortVector<S> shiftL(int s, Mask<Short, S> m) {
        return uOp(m, (i, a) -> (short) (a << s));
    }

    // logical, or unsigned, shift right
    public ShortVector<S> shiftR(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a >>> b));
    }

    public ShortVector<S> shiftR(int s) {
        return uOp((i, a) -> (short) (a >>> s));
    }

    public ShortVector<S> shiftR(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a >>> b));
    }

    public ShortVector<S> shiftR(int s, Mask<Short, S> m) {
        return uOp(m, (i, a) -> (short) (a >>> s));
    }

    // arithmetic, or signed, shift right
    public ShortVector<S> ashiftR(Vector<Short,S> o) {
        return bOp(o, (i, a, b) -> (short) (a >> b));
    }

    public ShortVector<S> aShiftR(int s) {
        return uOp((i, a) -> (short) (a >> s));
    }

    public ShortVector<S> ashiftR(Vector<Short,S> o, Mask<Short, S> m) {
        return bOp(o, m, (i, a, b) -> (short) (a >> b));
    }

    public ShortVector<S> aShiftR(int s, Mask<Short, S> m) {
        return uOp(m, (i, a) -> (short) (a >> s));
    }


    public ShortVector<S> rotateL(int j) {
        return uOp((i, a) -> (short) Integer.rotateLeft(a, j));
    }

    public ShortVector<S> rotateR(int j) {
        return uOp((i, a) -> (short) Integer.rotateRight(a, j));
    }

    @Override
    public void intoByteArray(byte[] a, int ix) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb);
    }

    @Override
    public void intoByteArray(byte[] a, int ix, Mask<Short, S> m) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb, m);
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb) {
        ShortBuffer fb = bb.asShortBuffer();
        forEach((i, a) -> fb.put(a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, Mask<Short, S> m) {
        ShortBuffer fb = bb.asShortBuffer();
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
        ShortBuffer fb = bb.asShortBuffer();
        forEach((i, a) -> fb.put(i, a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Short, S> m) {
        bb = bb.duplicate().position(ix);
        ShortBuffer fb = bb.asShortBuffer();
        forEach(m, (i, a) -> fb.put(i, a));
    }


    // Type specific horizontal reductions

    public short addAll() {
        return rOp((short) 0, (i, a, b) -> (short) (a + b));
    }

    public short subAll() {
        return rOp((short) 0, (i, a, b) -> (short) (a - b));
    }

    public short mulAll() {
        return rOp((short) 1, (i, a, b) -> (short) (a * b));
    }

    public short minAll() {
        return rOp(Short.MAX_VALUE, (i, a, b) -> a > b ? b : a);
    }

    public short maxAll() {
        return rOp(Short.MIN_VALUE, (i, a, b) -> a < b ? b : a);
    }

    public short orAll() {
        return rOp((short) 0, (i, a, b) -> (short) (a | b));
    }

    public short andAll() {
        return rOp((short) -1, (i, a, b) -> (short) (a & b));
    }

    public short xorAll() {
        return rOp((short) 0, (i, a, b) -> (short) (a ^ b));
    }

    // Type conversions

    @Override
    public <F> Vector<F,S> cast(Class<F> type) {
        return cast(type, shape());
    }

    // Type specific accessors

    public abstract short get(int i);

    public abstract ShortVector<S> with(int i, short e);

    // Type specific extractors

    @HotSpotIntrinsicCandidate
    public void intoArray(short[] a, int ax) {
        forEach((i, a_) -> a[ax + i] = a_);
    }

    @HotSpotIntrinsicCandidate
    public void intoArray(short[] a, int ax, Mask<Short, S> m) {
        forEach(m, (i, a_) -> a[ax + i] = a_);
    }

    public void intoArray(short[] a, int ax, int[] indexMap, int mx) {
        forEach((i, a_) -> a[ax + indexMap[mx + i]] = a_);
    }

    public void intoArray(short[] a, int ax, Mask<Short, S> m, int[] indexMap, int mx) {
        forEach(m, (i, a_) -> a[ax + indexMap[mx + i]] = a_);
    }

    // Species

    @Override
    public abstract ShortSpecies<S> species();

    public static abstract class ShortSpecies<S extends Vector.Shape> implements Vector.Species<Short, S> {
        interface FOp {
            short apply(int i);
        }

        abstract ShortVector<S> op(FOp f);

        abstract ShortVector<S> op(Mask<Short, S> m, FOp f);

        // Factories

        @Override
        public ShortVector<S> zero() {
            return op(i -> 0);
        }

        public ShortVector<S> broadcast(short e) {
            return op(i -> e);
        }

        public ShortVector<S> single(short e) {
            return op(i -> i == 0 ? e : (short) 0);
        }

        public ShortVector<S> random() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            return op(i -> (short) r.nextInt());
        }

        public ShortVector<S> scalars(short... es) {
            return op(i -> es[i]);
        }

        @HotSpotIntrinsicCandidate
        public ShortVector<S> fromArray(short[] a, int ax) {
            return op(i -> a[ax + i]);
        }

        @HotSpotIntrinsicCandidate
        public ShortVector<S> fromArray(short[] a, int ax, Mask<Short, S> m) {
            return op(m, i -> a[ax + i]);
        }

        public ShortVector<S> fromArray(short[] a, int ax, int[] indexMap, int mx) {
            return op(i -> a[ax + indexMap[mx + i]]);
        }

        public ShortVector<S> fromArray(short[] a, int ax, Mask<Short, S> m, int[] indexMap, int mx) {
            return op(m, i -> a[ax + indexMap[mx + i]]);
        }

        @Override
        public ShortVector<S> fromByteArray(byte[] a, int ix) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb);
        }

        @Override
        public ShortVector<S> fromByteArray(byte[] a, int ix, Mask<Short, S> m) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb, m);
        }

        @Override
        public ShortVector<S> fromByteBuffer(ByteBuffer bb) {
            ShortBuffer fb = bb.asShortBuffer();
            return op(i -> fb.get());
        }

        @Override
        public ShortVector<S> fromByteBuffer(ByteBuffer bb, Mask<Short, S> m) {
            ShortBuffer fb = bb.asShortBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (short) 0;
                }
            });
        }

        @Override
        public ShortVector<S> fromByteBuffer(ByteBuffer bb, int ix) {
            bb = bb.duplicate().position(ix);
            ShortBuffer fb = bb.asShortBuffer();
            return op(i -> fb.get(i));
        }

        @Override
        public ShortVector<S> fromByteBuffer(ByteBuffer bb, int ix, Mask<Short, S> m) {
            bb = bb.duplicate().position(ix);
            ShortBuffer fb = bb.asShortBuffer();
            return op(m, i -> fb.get(i));
        }
    }
}
