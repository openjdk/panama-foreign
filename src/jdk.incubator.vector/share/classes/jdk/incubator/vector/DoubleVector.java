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

import jdk.internal.vm.annotation.ForceInline;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("cast")
public abstract class DoubleVector<S extends Vector.Shape> extends Vector<Double,S> {

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

    @Override
    public DoubleVector<S> add(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) (a + b));
    }

    public abstract DoubleVector<S> add(double o);

    @Override
    public DoubleVector<S> add(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) (a + b));
    }

    public abstract DoubleVector<S> add(double o, Mask<Double, S> m);

    @Override
    public DoubleVector<S> addSaturate(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract DoubleVector<S> addSaturate(double o);

    @Override
    public DoubleVector<S> addSaturate(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract DoubleVector<S> addSaturate(double o, Mask<Double, S> m);

    @Override
    public DoubleVector<S> sub(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) (a - b));
    }

    public abstract DoubleVector<S> sub(double o);

    @Override
    public DoubleVector<S> sub(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) (a - b));
    }

    public abstract DoubleVector<S> sub(double o, Mask<Double, S> m);

    @Override
    public DoubleVector<S> subSaturate(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) ((a >= Double.MIN_VALUE || Double.MIN_VALUE + b > a) ? Double.MAX_VALUE : a - b));
    }

    public abstract DoubleVector<S> subSaturate(double o);

    @Override
    public DoubleVector<S> subSaturate(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) ((a >= Double.MIN_VALUE || Double.MIN_VALUE + b > a) ? Double.MAX_VALUE : a - b));
    }

    public abstract DoubleVector<S> subSaturate(double o, Mask<Double, S> m);

    @Override
    public DoubleVector<S> mul(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) (a * b));
    }

    public abstract DoubleVector<S> mul(double o);

    @Override
    public DoubleVector<S> mul(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) (a * b));
    }

    public abstract DoubleVector<S> mul(double o, Mask<Double, S> m);

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

    public abstract DoubleVector<S> min(double o);

    @Override
    public DoubleVector<S> max(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (a >= b) ? a : b);
    }

    public abstract DoubleVector<S> max(double o);

    @Override
    public Mask<Double, S> equal(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a == b);
    }

    public abstract Mask<Double, S> equal(double o);

    @Override
    public Mask<Double, S> notEqual(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a != b);
    }

    public abstract Mask<Double, S> notEqual(double o);

    @Override
    public Mask<Double, S> lessThan(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a < b);
    }

    public abstract Mask<Double, S> lessThan(double o);

    @Override
    public Mask<Double, S> lessThanEq(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a <= b);
    }

    public abstract Mask<Double, S> lessThanEq(double o);

    @Override
    public Mask<Double, S> greaterThan(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a > b);
    }

    public abstract Mask<Double, S> greaterThan(double o);

    @Override
    public Mask<Double, S> greaterThanEq(Vector<Double,S> o) {
        return bTest(o, (i, a, b) -> a >= b);
    }

    public abstract Mask<Double, S> greaterThanEq(double o);

    @Override
    public DoubleVector<S> blend(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, (i, a, b) -> m.getElement(i) ? b : a);
    }

    public abstract DoubleVector<S> blend(double o, Mask<Double, S> m);

    public DoubleVector<S> div(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) (a / b));
    }

    public abstract DoubleVector<S> div(double o);

    public DoubleVector<S> div(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) (a / b));
    }

    public abstract DoubleVector<S> div(double o, Mask<Double, S> m);

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

    public abstract DoubleVector<S> atan2(double o);

    public DoubleVector<S> atan2(Vector<Double,S> o, Mask<Double,S> m) {
        return bOp(o, m, (i, a, b) -> (double) Math.atan2((double) a, (double) b));
    }

    public abstract DoubleVector<S> atan2(double o, Mask<Double,S> m);

    public DoubleVector<S> cbrt() {
        return uOp((i, a) -> (double) Math.cbrt((double) a));
    }

    public DoubleVector<S> cbrt(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.cbrt((double) a));
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

    public DoubleVector<S> pow(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) Math.pow((double) a, (double) b));
    }

    public abstract DoubleVector<S> pow(double o);

    public DoubleVector<S> pow(Vector<Double,S> o, Mask<Double,S> m) {
        return bOp(o, m, (i, a, b) -> (double) Math.pow((double) a, (double) b));
    }

    public abstract DoubleVector<S> pow(double o, Mask<Double,S> m);

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

    public DoubleVector<S> fma(Vector<Double,S> o1, Vector<Double,S> o2) {
        return tOp(o1, o2, (i, a, b, c) -> Math.fma(a, b, c));
    }

    public abstract DoubleVector<S> fma(double o1, double o2);

    public DoubleVector<S> fma(Vector<Double,S> o1, Vector<Double,S> o2, Mask<Double,S> m) {
        return tOp(o1, o2, m, (i, a, b, c) -> Math.fma(a, b, c));
    }

    public abstract DoubleVector<S> fma(double o1, double o2, Mask<Double,S> m);

    public DoubleVector<S> hypot(Vector<Double,S> o) {
        return bOp(o, (i, a, b) -> (double) Math.hypot((double) a, (double) b));
    }

    public abstract DoubleVector<S> hypot(double o);

    public DoubleVector<S> hypot(Vector<Double,S> o, Mask<Double,S> m) {
        return bOp(o, m, (i, a, b) -> (double) Math.hypot((double) a, (double) b));
    }

    public abstract DoubleVector<S> hypot(double o, Mask<Double,S> m);


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


    // Type specific accessors

    public abstract double get(int i);

    public abstract DoubleVector<S> with(int i, double e);

    // Type specific extractors

    @ForceInline
    public double[] toArray() {
        double[] a = new double[species().length()];
        intoArray(a, 0);
        return a;
    }

    public void intoArray(double[] a, int ax) {
        forEach((i, a_) -> a[ax + i] = a_);
    }

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

    public static abstract class DoubleSpecies<S extends Vector.Shape> extends Vector.Species<Double, S> {
        interface FOp {
            double apply(int i);
        }

        abstract DoubleVector<S> op(FOp f);

        abstract DoubleVector<S> op(Mask<Double, S> m, FOp f);

        // Factories

        @Override
        public DoubleVector<S> zero() {
            return op(i -> 0);
        }

        public DoubleVector<S> broadcast(double e) {
            return op(i -> e);
        }

        public DoubleVector<S> single(double e) {
            return op(i -> i == 0 ? e : (double) 0);
        }

        public DoubleVector<S> random() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            return op(i -> r.nextDouble());
        }

        public DoubleVector<S> scalars(double... es) {
            return op(i -> es[i]);
        }

        public DoubleVector<S> fromArray(double[] a, int ax) {
            return op(i -> a[ax + i]);
        }

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

        @Override
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> DoubleVector<S> cast(Vector<F, T> v) {
            // Allocate array of required size
            double[] a = new double[length()];

            Class<?> vtype = v.species().elementType();
            int limit = Math.min(v.species().length(), length());
            if (vtype == Byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == Short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == Integer.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == Long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == Float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == Double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
        }

    }
}
