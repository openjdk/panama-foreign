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
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;


/**
 * A specialized {@link Vector} representing an ordered immutable sequence of
 * {@code double} values.
 *
 * @param <S> the type of shape of this vector
 */
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

    /**
     * Adds this vector to the result of broadcasting an input scalar.
     * <p>
     * This is a vector binary operation where the primitive addition operation
     * ({@code +}) is applied to lane elements.
     *
     * @param b the input scalar
     * @return the result of adding this vector to the broadcast of an input
     * scalar
     */
    public abstract DoubleVector<S> add(double b);

    @Override
    public DoubleVector<S> add(Vector<Double,S> o, Mask<Double, S> m) {
        return bOp(o, m, (i, a, b) -> (double) (a + b));
    }

    /**
     * Adds this vector to the result of broadcasting an input scalar,
     * selecting lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the primitive addition operation
     * ({@code +}) is applied to lane elements.
     *
     * @param b the input vector
     * @param m the mask controlling lane selection
     * @return the result of adding this vector to the broadcast of an input
     * scalar
     */
    public abstract DoubleVector<S> add(double b, Mask<Double, S> m);

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

    @Override
    public abstract DoubleVector<S> shuffle(Vector<Double,S> o, Shuffle<Double, S> m);

    @Override
    public abstract DoubleVector<S> swizzle(Shuffle<Double, S> m);

    @Override
    @ForceInline
    public <T extends Shape> DoubleVector<T> resize(Species<Double, T> species) {
        return (DoubleVector<T>) species.reshape(this);
    }

    @Override
    public abstract DoubleVector<S> rotateEL(int i);

    @Override
    public abstract DoubleVector<S> rotateER(int i);

    @Override
    public abstract DoubleVector<S> shiftEL(int i);

    @Override
    public abstract DoubleVector<S> shiftER(int i);

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

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     */
    public abstract double get(int i);

    /**
     * Replaces the lane element of this vector at lane index {@code i} with
     * value {@code e}.
     * <p>
     * This is a cross-lane operation.
     * @@@ specify as blend(species().broadcast(e), mask)
     *
     * @param i the lane index of the lane element to be replaced
     * @param e the value to be placed
     * @return the result of replacing the lane element of this vector at lane
     * index {@code i} with value {@code e}.
     */
    public abstract DoubleVector<S> with(int i, double e);

    // Type specific extractors

    /**
     * Returns an array containing the lane elements of this vector.
     * <p>
     * This method behaves as if it {@link #intoArray(double[], int)} stores}
     * this vector into an allocated array and returns the array as follows:
     * <pre>{@code
     *   double[] a = new double[this.length()];
     *   this.intoArray(a, 0);
     *   return a;
     * }</pre>
     *
     * @return an array containing the the lane elements of this vector
     */
    @ForceInline
    public double[] toArray() {
        double[] a = new double[species().length()];
        intoArray(a, 0);
        return a;
    }

    /**
     * Stores this vector into an array starting at offset.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * the lane element at index {@code N} is stored into the array at index
     * {@code i + N}.
     *
     * @param a the array
     * @param i the offset into the array
     * @throws IndexOutOfBoundsException if {@code i < 0}, or
     * {@code i > a.length - this.length()}
     */
    public void intoArray(double[] a, int i) {
        forEach((n, e) -> a[i + n] = e);
    }

    /**
     * Stores this vector into an array starting at offset and using a mask.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * if the mask lane at index {@code N} is set then the lane element at
     * index {@code N} is stored into the array index {@code i + N}.
     *
     * @param a the array
     * @param i the offset into the array
     * @param m the mask
     * @throws IndexOutOfBoundsException if {@code i < 0}, or
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set {@code i >= a.length - N}
     */
    public void intoArray(double[] a, int i, Mask<Double, S> m) {
        forEach(m, (n, e) -> a[i + n] = e);
    }

    /**
     * Stores this vector into an array using indexes obtained from an index
     * map.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index, the
     * lane element at index {@code N} is stored into the array at index
     * {@code i + indexMap[j + N]}.
     *
     * @param a the array
     * @param i the offset into the array, may be negative if relative
     * indexes in the index map compensate to produce a value within the
     * array bounds
     * @param indexMap the index map
     * @param j the offset into the index map
     * @throws IndexOutOfBoundsException if {@code j < 0}, or
     * {@code j > indexMap.length - this.length()},
     * or for any vector lane index {@code N} the result of
     * {@code i + indexMap[j + N]} is {@code < 0} or {@code >= a.length}
     */
    public void intoArray(double[] a, int i, int[] indexMap, int j) {
        forEach((n, e) -> a[i + indexMap[j + n]] = e);
    }

    /**
     * Stores this vector into an array using indexes obtained from an index
     * map and using a mask.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * if the mask lane at index {@code N} is set then the lane element at
     * index {@code N} is stored into the array at index
     * {@code i + indexMap[j + N]}.
     *
     * @param a the array
     * @param i the offset into the array, may be negative if relative
     * indexes in the index map compensate to produce a value within the
     * array bounds
     * @param m the mask
     * @param indexMap the index map
     * @param j the offset into the index map
     * @throws IndexOutOfBoundsException if {@code j < 0}, or
     * {@code j > indexMap.length - this.length()},
     * or for any vector lane index {@code N} where the mask at lane
     * {@code N} is set the result of {@code i + indexMap[j + N]} is
     * {@code < 0} or {@code >= a.length}
     */
    public void intoArray(double[] a, int i, Mask<Double, S> m, int[] indexMap, int j) {
        forEach(m, (n, e) -> a[i + indexMap[j + n]] = e);
    }

    // Species

    @Override
    public abstract DoubleSpecies<S> species();

    /**
     * A specialized factory for creating {@link DoubleVector} value of the same
     * shape, and a {@link Mask} and {@link Shuffle} values of the same shape
     * and {@code int} element type.
     *
     * @param <S> the type of shape of this species
     */
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

        /**
         * Returns a vector where all lane elements are set to the primitive
         * value {@code e}.
         *
         * @param e the value
         * @return a vector of vector where all lane elements are set to
         * the primitive value {@code e}
         */
        public DoubleVector<S> broadcast(double e) {
            return op(i -> e);
        }

        /**
         * Returns a vector where the first lane element is set to the primtive
         * value {@code e}, all other lane elements are set to the default
         * value.
         *
         * @param e the value
         * @return a vector where the first lane element is set to the primitive
         * value {@code e}
         */
        public DoubleVector<S> single(double e) {
            return op(i -> i == 0 ? e : (double) 0);
        }

        /**
         * Returns a vector where each lane element is set to a randomly
         * generated primitive value.
         * @@@ what are the properties of the random number generator?
         *
         * @return a vector where each lane elements is set to a randomly
         * generated primitive value
         */
        public DoubleVector<S> random() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            return op(i -> r.nextDouble());
        }

        /**
         * Returns a vector where each lane element is set to a given
         * primitive value.
         * <p>
         * For each vector lane, where {@code N} is the vector lane index, the
         * the primitive value at index {@code N} is placed into the resulting
         * vector at lane index {@code N}.
         *
         * @@@ What should happen if es.length < this.length() ? use the default
         * value or throw IndexOutOfBoundsException
         *
         * @param es the given primitive values
         * @return a vector where each lane element is set to a given primitive
         * value
         */
        public DoubleVector<S> scalars(double... es) {
            return op(i -> es[i]);
        }

        /**
         * Loads a vector from an array starting at offset.
         * <p>
         * For each vector lane, where {@code N} is the vector lane index, the
         * array element at index {@code i + N} is placed into the
         * resulting vector at lane index {@code N}.
         *
         * @param a the array
         * @param i the offset into the array
         * @return the vector loaded from an array
         * @throws IndexOutOfBoundsException if {@code i < 0}, or
         * {@code i > a.length - this.length()}
         */
        public DoubleVector<S> fromArray(double[] a, int i) {
            return op(n -> a[i + n]);
        }

        /**
         * Loads a vector from an array starting at offset and using a mask.
         * <p>
         * For each vector lane, where {@code N} is the vector lane index,
         * if the mask lane at index {@code N} is set then the array element at
         * index {@code i + N} is placed into the resulting vector at lane index
         * {@code N}, otherwise the default element value is placed into the
         * resulting vector at lane index {@code N}.
         *
         * @param a the array
         * @param i the offset into the array
         * @param m the mask
         * @return the vector loaded from an array
         * @throws IndexOutOfBoundsException if {@code i < 0}, or
         * for any vector lane index {@code N} where the mask at lane {@code N}
         * is set {@code i > a.length - N}
         */
        public DoubleVector<S> fromArray(double[] a, int i, Mask<Double, S> m) {
            return op(m, n -> a[i + n]);
        }

        /**
         * Loads a vector from an array using indexes obtained from an index
         * map.
         * <p>
         * For each vector lane, where {@code N} is the vector lane index, the
         * array element at index {@code i + indexMap[j + N]} is placed into the
         * resulting vector at lane index {@code N}.
         *
         * @param a the array
         * @param i the offset into the array, may be negative if relative
         * indexes in the index map compensate to produce a value within the
         * array bounds
         * @param indexMap the index map
         * @param j the offset into the index map
         * @return the vector loaded from an array
         * @throws IndexOutOfBoundsException if {@code j < 0}, or
         * {@code j > indexMap.length - this.length()},
         * or for any vector lane index {@code N} the result of
         * {@code i + indexMap[j + N]} is {@code < 0} or {@code >= a.length}
         */
        public DoubleVector<S> fromArray(double[] a, int i, int[] indexMap, int j) {
            return op(n -> a[i + indexMap[j + n]]);
        }

        /**
         * Loads a vector from an array using indexes obtained from an index
         * map and using a mask.
         * <p>
         * For each vector lane, where {@code N} is the vector lane index,
         * if the mask lane at index {@code N} is set then the array element at
         * index {@code i + indexMap[j + N]} is placed into the resulting vector
         * at lane index {@code N}.
         *
         * @param a the array
         * @param i the offset into the array, may be negative if relative
         * indexes in the index map compensate to produce a value within the
         * array bounds
         * @param indexMap the index map
         * @param j the offset into the index map
         * @return the vector loaded from an array
         * @throws IndexOutOfBoundsException if {@code j < 0}, or
         * {@code j > indexMap.length - this.length()},
         * or for any vector lane index {@code N} where the mask at lane
         * {@code N} is set the result of {@code i + indexMap[j + N]} is
         * {@code < 0} or {@code >= a.length}
         */
        public DoubleVector<S> fromArray(double[] a, int i, Mask<Double, S> m, int[] indexMap, int j) {
            return op(m, n -> a[i + indexMap[j + n]]);
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
        public <F, T extends Shape> DoubleVector<S> reshape(Vector<F, T> o) {
            int blen = Math.max(o.species().bitSize(), bitSize()) / Byte.SIZE;
            ByteBuffer bb = ByteBuffer.allocate(blen).order(ByteOrder.nativeOrder());
            o.intoByteBuffer(bb, 0);
            return fromByteBuffer(bb, 0);
        }

        @Override
        @ForceInline
        public <F> DoubleVector<S> rebracket(Vector<F, S> o) {
            return reshape(o);
        }

        @Override
        @ForceInline
        public <T extends Shape> DoubleVector<S> resize(Vector<Double, T> o) {
            return reshape(o);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> DoubleVector<S> cast(Vector<F, T> v) {
            // Allocate array of required size
            double[] a = new double[length()];

            Class<?> vtype = v.species().elementType();
            int limit = Math.min(v.species().length(), length());
            if (vtype == byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == int.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == double.class){
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

    /**
     * Finds the preferred species for an element type of {@code double}.
     * <p>
     * A preferred species is a species chosen by the platform that has a
     * shape of maximal bit size.  A preferred species for different element
     * types will have the same shape, and therefore vectors, masks, and
     * shuffles created from such species will be shape compatible.
     *
     * @return the preferred species for an element type of {@code double}
     */
    @SuppressWarnings("unchecked")
    public static DoubleSpecies<?> preferredSpeciesInstance() {
        return (DoubleSpecies<?>) Vector.preferredSpeciesInstance(double.class);
    }

    /**
     * Finds a species for an element type of {@code double} and shape.
     *
     * @param s the shape
     * @param <S> the type of shape
     * @return a species for an element type of {@code double} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    @SuppressWarnings("unchecked")
    public static <S extends Shape> DoubleSpecies<S> speciesInstance(S s) {
        Objects.requireNonNull(s);
        if (s == Shapes.S_64_BIT) {
            return (DoubleSpecies<S>) Double64Vector.SPECIES;
        } else if (s == Shapes.S_128_BIT) {
            return (DoubleSpecies<S>) Double128Vector.SPECIES;
        } else if (s == Shapes.S_256_BIT) {
            return (DoubleSpecies<S>) Double256Vector.SPECIES;
        } else if (s == Shapes.S_512_BIT) {
            return (DoubleSpecies<S>) Double512Vector.SPECIES;
        } else {
            throw new IllegalArgumentException("Bad shape: " + s);
        }
    }
}
