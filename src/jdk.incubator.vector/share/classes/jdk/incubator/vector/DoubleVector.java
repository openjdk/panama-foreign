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
import java.nio.DoubleBuffer;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("cast")
public abstract class DoubleVector<S extends Vector.Shape<Vector<?,?>>> implements Vector<Double,S> {

    DoubleVector() {}

    // Unary operator

    interface FUnOp {
        double apply(int i, double a);
    }

    abstract DoubleVector<S> uOp(FUnOp f);

    abstract DoubleVector<S> uOp(Mask<Double, S> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        double apply(int i, double a, double b);
    }

    abstract DoubleVector<S> bOp(Vector<Double,S> o, FBinOp f);

    abstract DoubleVector<S> bOp(Vector<Double,S> o, Mask<Double, S> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        double apply(int i, double a, double b, double c);
    }

    abstract DoubleVector<S> tOp(Vector<Double,S> o1, Vector<Double,S> o2, FTriOp f);

    abstract DoubleVector<S> tOp(Vector<Double,S> o1, Vector<Double,S> o2, Mask<Double, S> m, FTriOp f);

    // Reduction operator

    abstract double rOp(double v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, double a, double b);
    }

    abstract Mask<Double, S> bTest(Vector<Double,S> o, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, double a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Double, S> m, FUnCon f);

    //

    @HotSpotIntrinsicCandidate
    @Override
    public DoubleVector<S> add(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) (a + b));
    }

    @HotSpotIntrinsicCandidate
    @Override
    public DoubleVector<S> add(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) (a + b));
    }

    @Override
    public DoubleVector<S> addSaturate(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    @Override
    public DoubleVector<S> addSaturate(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    @HotSpotIntrinsicCandidate
    @Override
    public DoubleVector<S> sub(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) (a - b));
    }

    @HotSpotIntrinsicCandidate
    @Override
    public DoubleVector<S> sub(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) (a - b));
    }

    @Override
    public DoubleVector<S> subSaturate(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) ((a >= Double.MIN_VALUE || Double.MIN_VALUE + b > a) ? Double.MAX_VALUE : a - b));
    }

    @Override
    public DoubleVector<S> subSaturate(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) ((a >= Double.MIN_VALUE || Double.MIN_VALUE + b > a) ? Double.MAX_VALUE : a - b));
    }

    @HotSpotIntrinsicCandidate
    @Override
    public DoubleVector<S> mul(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) (a * b));
    }

    @HotSpotIntrinsicCandidate
    @Override
    public DoubleVector<S> mul(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) (a * b));
    }

    @HotSpotIntrinsicCandidate
    @Override
    public DoubleVector<S> div(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) (a / b));
    }

    @HotSpotIntrinsicCandidate
    @Override
    public DoubleVector<S> div(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) (a / b));
    }

    @Override
    public DoubleVector<S> neg() {
        return uOp((i, a) -> (double) (-a));
    }

    @Override
    public DoubleVector<S> neg(Mask<Double, S> m) {
        return uOp(m, (i, a) -> (double) (-a));
    }

    @Override
    public DoubleVector<S> abs() {
        return uOp((i, a) -> (double) Math.abs(a));
    }

    @Override
    public DoubleVector<S> abs(Mask<Double, S> m) {
        return uOp(m, (i, a) -> (double) Math.abs(a));
    }

    @Override
    public DoubleVector<S> min(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (a <= b) ? a : b);
    }

    @Override
    public DoubleVector<S> max(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (a >= b) ? a : b);
    }

    @Override
    public Mask<Double, S> equal(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a == b);
    }

    @Override
    public Mask<Double, S> notEqual(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a != b);
    }

    @Override
    public Mask<Double, S> lessThan(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a < b);
    }

    @Override
    public Mask<Double, S> lessThanEq(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a <= b);
    }

    @Override
    public Mask<Double, S> greaterThan(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a > b);
    }

    @Override
    public Mask<Double, S> greaterThanEq(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a >= b);
    }

    @HotSpotIntrinsicCandidate
    @Override
    public DoubleVector<S> blend(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, (i, a, b) -> m.getElement(i) ? b : a);
    }

    public DoubleVector<S> sqrt() {
        return uOp((i, a) -> (double) Math.sqrt((double) a));
    }

    public DoubleVector<S> sqrt(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.sqrt((double) a));
    }

    public DoubleVector<S> tan() {
        return uOp((i, a) -> (double) Math.tan((double) a));
    }

    public DoubleVector<S> tan(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.tan((double) a));
    }

    public DoubleVector<S> tanh() {
        return uOp((i, a) -> (double) Math.tanh((double) a));
    }

    public DoubleVector<S> tanh(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.tanh((double) a));
    }

    public DoubleVector<S> toDegrees() {
        return uOp((i, a) -> (double) Math.toDegrees((double) a));
    }

    public DoubleVector<S> toDegrees(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.toDegrees((double) a));
    }

    public DoubleVector<S> toRadians() {
        return uOp((i, a) -> (double) Math.toRadians((double) a));
    }

    public DoubleVector<S> toRadians(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.toRadians((double) a));
    }

    public DoubleVector<S> ulp() {
        return uOp((i, a) -> (double) Math.ulp((double) a));
    }

    public DoubleVector<S> ulp(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.ulp((double) a));
    }

    public DoubleVector<S> sin() {
        return uOp((i, a) -> (double) Math.sin((double) a));
    }

    public DoubleVector<S> sin(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.sin((double) a));
    }

    public DoubleVector<S> sinh() {
        return uOp((i, a) -> (double) Math.sinh((double) a));
    }

    public DoubleVector<S> sinh(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.sinh((double) a));
    }

    public DoubleVector<S> cos() {
        return uOp((i, a) -> (double) Math.cos((double) a));
    }

    public DoubleVector<S> cos(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.cos((double) a));
    }

    public DoubleVector<S> cosh() {
        return uOp((i, a) -> (double) Math.cosh((double) a));
    }

    public DoubleVector<S> cosh(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.cosh((double) a));
    }

    public DoubleVector<S> asin() {
        return uOp((i, a) -> (double) Math.asin((double) a));
    }

    public DoubleVector<S> asin(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.asin((double) a));
    }

    public DoubleVector<S> acos() {
        return uOp((i, a) -> (double) Math.acos((double) a));
    }

    public DoubleVector<S> acos(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.acos((double) a));
    }

    public DoubleVector<S> atan() {
        return uOp((i, a) -> (double) Math.atan((double) a));
    }

    public DoubleVector<S> atan(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.atan((double) a));
    }

    public DoubleVector<S> atan2(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) Math.atan2((double) a, (double) b));
    }

    public DoubleVector<S> atan2(Vector<Double,S> o, Mask<Double,S> m) {
        return bOp(o, m, (i, a, b) -> (double) Math.atan2((double) a, (double) b));
    }

    public DoubleVector<S> cbrt() {
        return uOp((i, a) -> (double) Math.cbrt((double) a));
    }

    public DoubleVector<S> cbrt(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.cbrt((double) a));
    }

    public DoubleVector<S> ceil() {
        return uOp((i, a) -> (double) Math.ceil((double) a));
    }

    public DoubleVector<S> ceil(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.ceil((double) a));
    }

    public DoubleVector<S> copySign(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) Math.copySign((double) a, (double) b));
    }

    public DoubleVector<S> copySign(Vector<Double,S> o, Mask<Double,S> m) {
        return bOp(o, m, (i, a, b) -> (double) Math.copySign((double) a, (double) b));
    }

    public DoubleVector<S> log() {
        return uOp((i, a) -> (double) Math.log((double) a));
    }

    public DoubleVector<S> log(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.log((double) a));
    }

    public DoubleVector<S> log10() {
        return uOp((i, a) -> (double) Math.log10((double) a));
    }

    public DoubleVector<S> log10(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.log10((double) a));
    }

    public DoubleVector<S> log1p() {
        return uOp((i, a) -> (double) Math.log1p((double) a));
    }

    public DoubleVector<S> log1p(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.log1p((double) a));
    }

    public DoubleVector<S> nextAfter(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) Math.nextAfter((double) a, (double) b));
    }

    public DoubleVector<S> nextAfter(Vector<Double,S> o, Mask<Double,S> m) {
        return bOp(o, m, (i, a, b) -> (double) Math.nextAfter((double) a, (double) b));
    }

    public DoubleVector<S> nextDown() {
        return uOp((i, a) -> (double) Math.nextDown((double) a));
    }

    public DoubleVector<S> nextDown(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.nextDown((double) a));
    }

    public DoubleVector<S> nextUp() {
        return uOp((i, a) -> (double) Math.nextUp((double) a));
    }

    public DoubleVector<S> nextUp(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.nextUp((double) a));
    }

    public DoubleVector<S> pow(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) Math.pow((double) a, (double) b));
    }

    public DoubleVector<S> pow(Vector<Double,S> o, Mask<Double,S> m) {
        return bOp(o, m, (i, a, b) -> (double) Math.pow((double) a, (double) b));
    }

    public DoubleVector<S> random() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return uOp((i, a) -> r.nextDouble());
    }

    public DoubleVector<S> rint() {
        return uOp((i, a) -> (double) Math.rint((double) a));
    }

    public DoubleVector<S> rint(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.rint((double) a));
    }

    public DoubleVector<S> round() {
        return uOp((i, a) -> (double) Math.round((double) a));
    }

    public DoubleVector<S> round(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.round((double) a));
    }

    public DoubleVector<S> scalb(Vector<Integer, S> o) {
        throw  new UnsupportedOperationException("Scalb not implemented yet.");
    }

    public DoubleVector<S> scalb(Vector<Integer, S> o, Mask<Double,S> m) {
        throw  new UnsupportedOperationException("Scalb not implemented yet.");
    }

    public DoubleVector<S> signum() {
        return uOp((i, a) -> (double) Math.signum((double) a));
    }

    public DoubleVector<S> signum(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.signum((double) a));
    }

    public DoubleVector<S> exp() {
        return uOp((i, a) -> (double) Math.exp((double) a));
    }

    public DoubleVector<S> exp(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.exp((double) a));
    }

    public DoubleVector<S> expm1() {
        return uOp((i, a) -> (double) Math.expm1((double) a));
    }

    public DoubleVector<S> expm1(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.expm1((double) a));
    }

    public DoubleVector<S> floor() {
        return uOp((i, a) -> (double) Math.floor((double) a));
    }

    public DoubleVector<S> floor(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.floor((double) a));
    }

    public DoubleVector<S> fma(Vector<Double,S> o1, Vector<Double,S> o2) {
        return tOp(o1, o2, (i, a, b, c) -> Math.fma(a, b, c));
    }

    public DoubleVector<S> fma(Vector<Double,S> o1, Vector<Double,S> o2, Mask<Double,S> m) {
        return tOp(o1, o2, m, (i, a, b, c) -> Math.fma(a, b, c));
    }

    public DoubleVector<S> getExponent() {
        return uOp((i, a) -> (double) Math.getExponent((double) a));
    }

    public DoubleVector<S> getExponent(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.getExponent((double) a));
    }

    public DoubleVector<S> hypot(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) Math.hypot((double) a, (double) b));
    }

    public DoubleVector<S> hypot(Vector<Double,S> o, Mask<Double,S> m) {
        return bOp(o, m, (i, a, b) -> (double) Math.hypot((double) a, (double) b));
    }

    public DoubleVector<S> IEEEremainder(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) Math.IEEEremainder((double) a, (double) b));
    }

    public DoubleVector<S> IEEEremainder(Vector<Double,S> o, Mask<Double,S> m) {
        return bOp(o, m, (i, a, b) -> (double) Math.IEEEremainder((double) a, (double) b));
    }


    @Override
    public void intoByteArray(byte[] a, int ix) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb);
    }

    @Override
    public void intoByteArray(byte[] a, int ix, Mask<Double, S> m) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb, m);
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb) {
        DoubleBuffer fb = bb.asDoubleBuffer();
        forEach((i, a) -> fb.put(a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, Mask<Double, S> m) {
        DoubleBuffer fb = bb.asDoubleBuffer();
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
        DoubleBuffer fb = bb.asDoubleBuffer();
        forEach((i, a) -> fb.put(i, a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Double, S> m) {
        bb = bb.duplicate().position(ix);
        DoubleBuffer fb = bb.asDoubleBuffer();
        forEach(m, (i, a) -> fb.put(i, a));
    }


    // Type specific horizontal reductions

    @HotSpotIntrinsicCandidate
    public double addAll() {
        return rOp((double) 0, (i, a, b) -> (double) (a + b));
    }

    public double subAll() {
        return rOp((double) 0, (i, a, b) -> (double) (a - b));
    }

    public double mulAll() {
        return rOp((double) 1, (i, a, b) -> (double) (a * b));
    }

    public double minAll() {
        return rOp(Double.MAX_VALUE, (i, a, b) -> a > b ? b : a);
    }

    public double maxAll() {
        return rOp(Double.MIN_VALUE, (i, a, b) -> a < b ? b : a);
    }


    // Type conversions

    @Override
    public <F> Vector<F,S> cast(Class<F> type) {
        return cast(type, shape());
    }

    // Type specific accessors

    public abstract double get(int i);

    public abstract DoubleVector<S> with(int i, double e);

    // Type specific extractors

    @HotSpotIntrinsicCandidate
    public void intoArray(double[] a, int ax) {
        forEach((i, a_) -> a[ax + i] = a_);
    }

    @HotSpotIntrinsicCandidate
    public void intoArray(double[] a, int ax, Mask<Double, S> m) {
        forEach(m, (i, a_) -> a[ax + i] = a_);
    }

    public void intoArray(double[] a, int ax, int[] indexMap, int mx) {
        forEach((i, a_) -> a[ax + indexMap[mx + i]] = a_);
    }

    public void intoArray(double[] a, int ax, Mask<Double, S> m, int[] indexMap, int mx) {
        forEach(m, (i, a_) -> a[ax + indexMap[mx + i]] = a_);
    }

    // Species

    @Override
    public abstract DoubleSpecies<S> species();

    public static abstract class DoubleSpecies<S extends Vector.Shape<Vector<?,?>>> implements Vector.Species<Double, S> {
        interface FOp {
            double apply(int i);
        }

        abstract DoubleVector<S> op(FOp f);

        abstract DoubleVector<S> op(Mask<Double, S> m, FOp f);

        // Factories

        @HotSpotIntrinsicCandidate
        @Override
        public DoubleVector<S> zero() {
            return op(i -> 0);
        }

        @HotSpotIntrinsicCandidate
        public DoubleVector<S> broadcast(double e) {
            return op(i -> e);
        }

        public DoubleVector<S> single(double e) {
            return op(i -> i == 0 ? e : (double) 0);
        }

        public DoubleVector<S> scalars(double... es) {
            return op(i -> es[i]);
        }

        @HotSpotIntrinsicCandidate
        public DoubleVector<S> fromArray(double[] a, int ax) {
            return op(i -> a[ax + i]);
        }

        @HotSpotIntrinsicCandidate
        public DoubleVector<S> fromArray(double[] a, int ax, Mask<Double, S> m) {
            return op(m, i -> a[ax + i]);
        }

        public DoubleVector<S> fromArray(double[] a, int ax, int[] indexMap, int mx) {
            return op(i -> a[ax + indexMap[mx + i]]);
        }

        public DoubleVector<S> fromArray(double[] a, int ax, Mask<Double, S> m, int[] indexMap, int mx) {
            return op(m, i -> a[ax + indexMap[mx + i]]);
        }

        @Override
        public DoubleVector<S> fromByteArray(byte[] a, int ix) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb);
        }

        @Override
        public DoubleVector<S> fromByteArray(byte[] a, int ix, Mask<Double, S> m) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb, m);
        }

        @Override
        public DoubleVector<S> fromByteBuffer(ByteBuffer bb) {
            DoubleBuffer fb = bb.asDoubleBuffer();
            return op(i -> fb.get());
        }

        @Override
        public DoubleVector<S> fromByteBuffer(ByteBuffer bb, Mask<Double, S> m) {
            DoubleBuffer fb = bb.asDoubleBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (double) 0;
                }
            });
        }

        @Override
        public DoubleVector<S> fromByteBuffer(ByteBuffer bb, int ix) {
            bb = bb.duplicate().position(ix);
            DoubleBuffer fb = bb.asDoubleBuffer();
            return op(i -> fb.get(i));
        }

        @Override
        public DoubleVector<S> fromByteBuffer(ByteBuffer bb, int ix, Mask<Double, S> m) {
            bb = bb.duplicate().position(ix);
            DoubleBuffer fb = bb.asDoubleBuffer();
            return op(m, i -> fb.get(i));
        }
    }
}
