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
import java.nio.FloatBuffer;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("cast")
public abstract class FloatVector<S extends Vector.Shape> implements Vector<Float,S> {

    FloatVector() {}

    // Unary operator

    interface FUnOp {
        float apply(int i, float a);
    }

    abstract FloatVector<S> uOp(FUnOp f);

    abstract FloatVector<S> uOp(Mask<Float, S> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        float apply(int i, float a, float b);
    }

    abstract FloatVector<S> bOp(Vector<Float,S> o, FBinOp f);

    abstract FloatVector<S> bOp(Vector<Float,S> o, Mask<Float, S> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        float apply(int i, float a, float b, float c);
    }

    abstract FloatVector<S> tOp(Vector<Float,S> o1, Vector<Float,S> o2, FTriOp f);

    abstract FloatVector<S> tOp(Vector<Float,S> o1, Vector<Float,S> o2, Mask<Float, S> m, FTriOp f);

    // Reduction operator

    abstract float rOp(float v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, float a, float b);
    }

    abstract Mask<Float, S> bTest(Vector<Float,S> o, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, float a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Float, S> m, FUnCon f);

    //

    @Override
    public FloatVector<S> add(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (float) (a + b));
    }

    public abstract FloatVector<S> add(float o);

    @Override
    public FloatVector<S> add(Vector<Float,S> o, Mask<Float, S> m) {
        return bOp(o, m, (i, a, b) -> (float) (a + b));
    }

    public abstract FloatVector<S> add(float o, Mask<Float, S> m);

    @Override
    public FloatVector<S> addSaturate(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (float) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract FloatVector<S> addSaturate(float o);

    @Override
    public FloatVector<S> addSaturate(Vector<Float,S> o, Mask<Float, S> m) {
        return bOp(o, m, (i, a, b) -> (float) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract FloatVector<S> addSaturate(float o, Mask<Float, S> m);

    @Override
    public FloatVector<S> sub(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (float) (a - b));
    }

    public abstract FloatVector<S> sub(float o);

    @Override
    public FloatVector<S> sub(Vector<Float,S> o, Mask<Float, S> m) {
        return bOp(o, m, (i, a, b) -> (float) (a - b));
    }

    public abstract FloatVector<S> sub(float o, Mask<Float, S> m);

    @Override
    public FloatVector<S> subSaturate(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (float) ((a >= Float.MIN_VALUE || Float.MIN_VALUE + b > a) ? Float.MAX_VALUE : a - b));
    }

    public abstract FloatVector<S> subSaturate(float o);

    @Override
    public FloatVector<S> subSaturate(Vector<Float,S> o, Mask<Float, S> m) {
        return bOp(o, m, (i, a, b) -> (float) ((a >= Float.MIN_VALUE || Float.MIN_VALUE + b > a) ? Float.MAX_VALUE : a - b));
    }

    public abstract FloatVector<S> subSaturate(float o, Mask<Float, S> m);

    @Override
    public FloatVector<S> mul(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (float) (a * b));
    }

    public abstract FloatVector<S> mul(float o);

    @Override
    public FloatVector<S> mul(Vector<Float,S> o, Mask<Float, S> m) {
        return bOp(o, m, (i, a, b) -> (float) (a * b));
    }

    public abstract FloatVector<S> mul(float o, Mask<Float, S> m);

    @Override
    public FloatVector<S> neg() {
        return uOp((i, a) -> (float) (-a));
    }

    @Override
    public FloatVector<S> neg(Mask<Float, S> m) {
        return uOp(m, (i, a) -> (float) (-a));
    }

    @Override
    public FloatVector<S> abs() {
        return uOp((i, a) -> (float) Math.abs(a));
    }

    @Override
    public FloatVector<S> abs(Mask<Float, S> m) {
        return uOp(m, (i, a) -> (float) Math.abs(a));
    }

    @Override
    public FloatVector<S> min(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (a <= b) ? a : b);
    }

    public abstract FloatVector<S> min(float o);

    @Override
    public FloatVector<S> max(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (a >= b) ? a : b);
    }

    public abstract FloatVector<S> max(float o);

    @Override
    public Mask<Float, S> equal(Vector<Float,S> o) {
        return bTest(o, (i, a, b) -> a == b);
    }

    public abstract Mask<Float, S> equal(float o);

    @Override
    public Mask<Float, S> notEqual(Vector<Float,S> o) {
        return bTest(o, (i, a, b) -> a != b);
    }

    public abstract Mask<Float, S> notEqual(float o);

    @Override
    public Mask<Float, S> lessThan(Vector<Float,S> o) {
        return bTest(o, (i, a, b) -> a < b);
    }

    public abstract Mask<Float, S> lessThan(float o);

    @Override
    public Mask<Float, S> lessThanEq(Vector<Float,S> o) {
        return bTest(o, (i, a, b) -> a <= b);
    }

    public abstract Mask<Float, S> lessThanEq(float o);

    @Override
    public Mask<Float, S> greaterThan(Vector<Float,S> o) {
        return bTest(o, (i, a, b) -> a > b);
    }

    public abstract Mask<Float, S> greaterThan(float o);

    @Override
    public Mask<Float, S> greaterThanEq(Vector<Float,S> o) {
        return bTest(o, (i, a, b) -> a >= b);
    }

    public abstract Mask<Float, S> greaterThanEq(float o);

    @Override
    public FloatVector<S> blend(Vector<Float,S> o, Mask<Float, S> m) {
        return bOp(o, (i, a, b) -> m.getElement(i) ? b : a);
    }

    public abstract FloatVector<S> blend(float o, Mask<Float, S> m);

    public FloatVector<S> div(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (float) (a / b));
    }

    public abstract FloatVector<S> div(float o);

    public FloatVector<S> div(Vector<Float,S> o, Mask<Float, S> m) {
        return bOp(o, m, (i, a, b) -> (float) (a / b));
    }

    public abstract FloatVector<S> div(float o, Mask<Float, S> m);

    public FloatVector<S> sqrt() {
        return uOp((i, a) -> (float) Math.sqrt((double) a));
    }

    public FloatVector<S> sqrt(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.sqrt((double) a));
    }

    public FloatVector<S> tan() {
        return uOp((i, a) -> (float) Math.tan((double) a));
    }

    public FloatVector<S> tan(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.tan((double) a));
    }

    public FloatVector<S> tanh() {
        return uOp((i, a) -> (float) Math.tanh((double) a));
    }

    public FloatVector<S> tanh(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.tanh((double) a));
    }

    public FloatVector<S> sin() {
        return uOp((i, a) -> (float) Math.sin((double) a));
    }

    public FloatVector<S> sin(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.sin((double) a));
    }

    public FloatVector<S> sinh() {
        return uOp((i, a) -> (float) Math.sinh((double) a));
    }

    public FloatVector<S> sinh(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.sinh((double) a));
    }

    public FloatVector<S> cos() {
        return uOp((i, a) -> (float) Math.cos((double) a));
    }

    public FloatVector<S> cos(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.cos((double) a));
    }

    public FloatVector<S> cosh() {
        return uOp((i, a) -> (float) Math.cosh((double) a));
    }

    public FloatVector<S> cosh(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.cosh((double) a));
    }

    public FloatVector<S> asin() {
        return uOp((i, a) -> (float) Math.asin((double) a));
    }

    public FloatVector<S> asin(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.asin((double) a));
    }

    public FloatVector<S> acos() {
        return uOp((i, a) -> (float) Math.acos((double) a));
    }

    public FloatVector<S> acos(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.acos((double) a));
    }

    public FloatVector<S> atan() {
        return uOp((i, a) -> (float) Math.atan((double) a));
    }

    public FloatVector<S> atan(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.atan((double) a));
    }

    public FloatVector<S> atan2(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (float) Math.atan2((double) a, (double) b));
    }

    public abstract FloatVector<S> atan2(float o);

    public FloatVector<S> atan2(Vector<Float,S> o, Mask<Float,S> m) {
        return bOp(o, m, (i, a, b) -> (float) Math.atan2((double) a, (double) b));
    }

    public abstract FloatVector<S> atan2(float o, Mask<Float,S> m);

    public FloatVector<S> cbrt() {
        return uOp((i, a) -> (float) Math.cbrt((double) a));
    }

    public FloatVector<S> cbrt(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.cbrt((double) a));
    }

    public FloatVector<S> log() {
        return uOp((i, a) -> (float) Math.log((double) a));
    }

    public FloatVector<S> log(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.log((double) a));
    }

    public FloatVector<S> log10() {
        return uOp((i, a) -> (float) Math.log10((double) a));
    }

    public FloatVector<S> log10(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.log10((double) a));
    }

    public FloatVector<S> log1p() {
        return uOp((i, a) -> (float) Math.log1p((double) a));
    }

    public FloatVector<S> log1p(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.log1p((double) a));
    }

    public FloatVector<S> pow(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (float) Math.pow((double) a, (double) b));
    }

    public abstract FloatVector<S> pow(float o);

    public FloatVector<S> pow(Vector<Float,S> o, Mask<Float,S> m) {
        return bOp(o, m, (i, a, b) -> (float) Math.pow((double) a, (double) b));
    }

    public abstract FloatVector<S> pow(float o, Mask<Float,S> m);

    public FloatVector<S> exp() {
        return uOp((i, a) -> (float) Math.exp((double) a));
    }

    public FloatVector<S> exp(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.exp((double) a));
    }

    public FloatVector<S> expm1() {
        return uOp((i, a) -> (float) Math.expm1((double) a));
    }

    public FloatVector<S> expm1(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.expm1((double) a));
    }

    public FloatVector<S> fma(Vector<Float,S> o1, Vector<Float,S> o2) {
        return tOp(o1, o2, (i, a, b, c) -> Math.fma(a, b, c));
    }

    public abstract FloatVector<S> fma(float o1, float o2);

    public FloatVector<S> fma(Vector<Float,S> o1, Vector<Float,S> o2, Mask<Float,S> m) {
        return tOp(o1, o2, m, (i, a, b, c) -> Math.fma(a, b, c));
    }

    public abstract FloatVector<S> fma(float o1, float o2, Mask<Float,S> m);

    public FloatVector<S> hypot(Vector<Float,S> o) {
        return bOp(o, (i, a, b) -> (float) Math.hypot((double) a, (double) b));
    }

    public abstract FloatVector<S> hypot(float o);

    public FloatVector<S> hypot(Vector<Float,S> o, Mask<Float,S> m) {
        return bOp(o, m, (i, a, b) -> (float) Math.hypot((double) a, (double) b));
    }

    public abstract FloatVector<S> hypot(float o, Mask<Float,S> m);


    @Override
    public void intoByteArray(byte[] a, int ix) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb);
    }

    @Override
    public void intoByteArray(byte[] a, int ix, Mask<Float, S> m) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
        intoByteBuffer(bb, m);
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb) {
        FloatBuffer fb = bb.asFloatBuffer();
        forEach((i, a) -> fb.put(a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, Mask<Float, S> m) {
        FloatBuffer fb = bb.asFloatBuffer();
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
        FloatBuffer fb = bb.asFloatBuffer();
        forEach((i, a) -> fb.put(i, a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Float, S> m) {
        bb = bb.duplicate().position(ix);
        FloatBuffer fb = bb.asFloatBuffer();
        forEach(m, (i, a) -> fb.put(i, a));
    }


    // Type specific horizontal reductions

    public float addAll() {
        return rOp((float) 0, (i, a, b) -> (float) (a + b));
    }

    public float subAll() {
        return rOp((float) 0, (i, a, b) -> (float) (a - b));
    }

    public float mulAll() {
        return rOp((float) 1, (i, a, b) -> (float) (a * b));
    }

    public float minAll() {
        return rOp(Float.MAX_VALUE, (i, a, b) -> a > b ? b : a);
    }

    public float maxAll() {
        return rOp(Float.MIN_VALUE, (i, a, b) -> a < b ? b : a);
    }


    // Type specific accessors

    public abstract float get(int i);

    public abstract FloatVector<S> with(int i, float e);

    // Type specific extractors

    @ForceInline
    public float[] toArray() {
        float[] a = new float[species().length()];
        intoArray(a, 0);
        return a;
    }

    public void intoArray(float[] a, int ax) {
        forEach((i, a_) -> a[ax + i] = a_);
    }

    public void intoArray(float[] a, int ax, Mask<Float, S> m) {
        forEach(m, (i, a_) -> a[ax + i] = a_);
    }

    public void intoArray(float[] a, int ax, int[] indexMap, int mx) {
        forEach((i, a_) -> a[ax + indexMap[mx + i]] = a_);
    }

    public void intoArray(float[] a, int ax, Mask<Float, S> m, int[] indexMap, int mx) {
        forEach(m, (i, a_) -> a[ax + indexMap[mx + i]] = a_);
    }

    // Species

    @Override
    public abstract FloatSpecies<S> species();

    public static abstract class FloatSpecies<S extends Vector.Shape> implements Vector.Species<Float, S> {
        interface FOp {
            float apply(int i);
        }

        abstract FloatVector<S> op(FOp f);

        abstract FloatVector<S> op(Mask<Float, S> m, FOp f);

        // Factories

        @Override
        public FloatVector<S> zero() {
            return op(i -> 0);
        }

        public FloatVector<S> broadcast(float e) {
            return op(i -> e);
        }

        public FloatVector<S> single(float e) {
            return op(i -> i == 0 ? e : (float) 0);
        }

        public FloatVector<S> random() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            return op(i -> r.nextFloat());
        }

        public FloatVector<S> scalars(float... es) {
            return op(i -> es[i]);
        }

        public FloatVector<S> fromArray(float[] a, int ax) {
            return op(i -> a[ax + i]);
        }

        public FloatVector<S> fromArray(float[] a, int ax, Mask<Float, S> m) {
            return op(m, i -> a[ax + i]);
        }

        public FloatVector<S> fromArray(float[] a, int ax, int[] indexMap, int mx) {
            return op(i -> a[ax + indexMap[mx + i]]);
        }

        public FloatVector<S> fromArray(float[] a, int ax, Mask<Float, S> m, int[] indexMap, int mx) {
            return op(m, i -> a[ax + indexMap[mx + i]]);
        }

        @Override
        public FloatVector<S> fromByteArray(byte[] a, int ix) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb);
        }

        @Override
        public FloatVector<S> fromByteArray(byte[] a, int ix, Mask<Float, S> m) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix);
            return fromByteBuffer(bb, m);
        }

        @Override
        public FloatVector<S> fromByteBuffer(ByteBuffer bb) {
            FloatBuffer fb = bb.asFloatBuffer();
            return op(i -> fb.get());
        }

        @Override
        public FloatVector<S> fromByteBuffer(ByteBuffer bb, Mask<Float, S> m) {
            FloatBuffer fb = bb.asFloatBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (float) 0;
                }
            });
        }

        @Override
        public FloatVector<S> fromByteBuffer(ByteBuffer bb, int ix) {
            bb = bb.duplicate().position(ix);
            FloatBuffer fb = bb.asFloatBuffer();
            return op(i -> fb.get(i));
        }

        @Override
        public FloatVector<S> fromByteBuffer(ByteBuffer bb, int ix, Mask<Float, S> m) {
            bb = bb.duplicate().position(ix);
            FloatBuffer fb = bb.asFloatBuffer();
            return op(m, i -> fb.get(i));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> FloatVector<S> cast(Vector<F, T> v) {
            // Allocate array of required size
            float[] a = new float[length()];

            Class<?> vtype = v.species().elementType();
            int limit = Math.min(v.species().length(), length());
            if (vtype == Byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == Short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == Integer.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == Long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == Float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == Double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
        }

    }
}
