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
import java.nio.LongBuffer;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("cast")
public abstract class LongVector<S extends Vector.Shape> extends Vector<Long,S> {

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
    public abstract LongVector<S> add(long b);

    @Override
    public LongVector<S> add(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a + b));
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
    public abstract LongVector<S> add(long b, Mask<Long, S> m);

    @Override
    public LongVector<S> addSaturate(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract LongVector<S> addSaturate(long o);

    @Override
    public LongVector<S> addSaturate(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract LongVector<S> addSaturate(long o, Mask<Long, S> m);

    @Override
    public LongVector<S> sub(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a - b));
    }

    public abstract LongVector<S> sub(long o);

    @Override
    public LongVector<S> sub(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a - b));
    }

    public abstract LongVector<S> sub(long o, Mask<Long, S> m);

    @Override
    public LongVector<S> subSaturate(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) ((a >= Long.MIN_VALUE || Long.MIN_VALUE + b > a) ? Long.MAX_VALUE : a - b));
    }

    public abstract LongVector<S> subSaturate(long o);

    @Override
    public LongVector<S> subSaturate(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) ((a >= Long.MIN_VALUE || Long.MIN_VALUE + b > a) ? Long.MAX_VALUE : a - b));
    }

    public abstract LongVector<S> subSaturate(long o, Mask<Long, S> m);

    @Override
    public LongVector<S> mul(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a * b));
    }

    public abstract LongVector<S> mul(long o);

    @Override
    public LongVector<S> mul(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a * b));
    }

    public abstract LongVector<S> mul(long o, Mask<Long, S> m);

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

    public abstract LongVector<S> min(long o);

    @Override
    public LongVector<S> max(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (a >= b) ? a : b);
    }

    public abstract LongVector<S> max(long o);

    @Override
    public Mask<Long, S> equal(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a == b);
    }

    public abstract Mask<Long, S> equal(long o);

    @Override
    public Mask<Long, S> notEqual(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a != b);
    }

    public abstract Mask<Long, S> notEqual(long o);

    @Override
    public Mask<Long, S> lessThan(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a < b);
    }

    public abstract Mask<Long, S> lessThan(long o);

    @Override
    public Mask<Long, S> lessThanEq(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a <= b);
    }

    public abstract Mask<Long, S> lessThanEq(long o);

    @Override
    public Mask<Long, S> greaterThan(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a > b);
    }

    public abstract Mask<Long, S> greaterThan(long o);

    @Override
    public Mask<Long, S> greaterThanEq(Vector<Long,S> o) {
        return bTest(o, (i, a, b) -> a >= b);
    }

    public abstract Mask<Long, S> greaterThanEq(long o);

    @Override
    public LongVector<S> blend(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, (i, a, b) -> m.getElement(i) ? b : a);
    }

    public abstract LongVector<S> blend(long o, Mask<Long, S> m);

    @Override
    public abstract LongVector<S> shuffle(Vector<Long,S> o, Shuffle<Long, S> m);

    @Override
    public abstract LongVector<S> swizzle(Shuffle<Long, S> m);

    @Override
    @ForceInline
    public <T extends Shape> LongVector<T> resize(Species<Long, T> species) {
        return (LongVector<T>) species.reshape(this);
    }

    @Override
    public abstract LongVector<S> rotateEL(int i);

    @Override
    public abstract LongVector<S> rotateER(int i);

    @Override
    public abstract LongVector<S> shiftEL(int i);

    @Override
    public abstract LongVector<S> shiftER(int i);


    public LongVector<S> and(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a & b));
    }

    public abstract LongVector<S> and(long o);

    public LongVector<S> and(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a & b));
    }

    public abstract LongVector<S> and(long o, Mask<Long, S> m);

    public LongVector<S> or(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a | b));
    }

    public abstract LongVector<S> or(long o);

    public LongVector<S> or(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a | b));
    }

    public abstract LongVector<S> or(long o, Mask<Long, S> m);

    public LongVector<S> xor(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a ^ b));
    }

    public abstract LongVector<S> xor(long o);

    public LongVector<S> xor(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a ^ b));
    }

    public abstract LongVector<S> xor(long o, Mask<Long, S> m);

    public LongVector<S> not() {
        return uOp((i, a) -> (long) (~a));
    }

    public LongVector<S> not(Mask<Long, S> m) {
        return uOp(m, (i, a) -> (long) (~a));
    }

    // logical shift left
    public LongVector<S> shiftL(int s) {
        return uOp((i, a) -> (long) (a << s));
    }

    public LongVector<S> shiftL(int s, Mask<Long, S> m) {
        return uOp(m, (i, a) -> (long) (a << s));
    }

    public LongVector<S> shiftL(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a << b));
    }

    public LongVector<S> shiftL(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a << b));
    }

    // logical, or unsigned, shift right
    public LongVector<S> shiftR(int s) {
        return uOp((i, a) -> (long) (a >>> s));
    }

    public LongVector<S> shiftR(int s, Mask<Long, S> m) {
        return uOp(m, (i, a) -> (long) (a >>> s));
    }

    public LongVector<S> shiftR(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a >>> b));
    }

    public LongVector<S> shiftR(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a >>> b));
    }

    // arithmetic, or signed, shift right
    public LongVector<S> aShiftR(int s) {
        return uOp((i, a) -> (long) (a >> s));
    }

    public LongVector<S> aShiftR(int s, Mask<Long, S> m) {
        return uOp(m, (i, a) -> (long) (a >> s));
    }

    public LongVector<S> ashiftR(Vector<Long,S> o) {
        return bOp(o, (i, a, b) -> (long) (a >> b));
    }

    public LongVector<S> ashiftR(Vector<Long,S> o, Mask<Long, S> m) {
        return bOp(o, m, (i, a, b) -> (long) (a >> b));
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

    /**
     * Sums all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the addition
     * operation ({@code +}) is applied to lane elements, and the identity value
     * is {@code 0}.
     *
     * @return the sum of all the lane elements of this vector
     */
    public long addAll() {
        return rOp((long) 0, (i, a, b) -> (long) (a + b));
    }

    public long subAll() {
        return rOp((long) 0, (i, a, b) -> (long) (a - b));
    }

    public long mulAll() {
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

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     */
    public abstract long get(int i);

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
    public abstract LongVector<S> with(int i, long e);

    // Type specific extractors

    @ForceInline
    public long[] toArray() {
        long[] a = new long[species().length()];
        intoArray(a, 0);
        return a;
    }

    public void intoArray(long[] a, int ax) {
        forEach((i, a_) -> a[ax + i] = a_);
    }

    public void intoArray(long[] a, int ax, Mask<Long, S> m) {
        forEach(m, (i, a_) -> a[ax + i] = a_);
    }

    public void intoArray(long[] a, int ax, int[] indexMap, int mx) {
        forEach((i, a_) -> a[ax + indexMap[mx + i]] = a_);
    }

    public void intoArray(long[] a, int ax, Mask<Long, S> m, int[] indexMap, int mx) {
        forEach(m, (i, a_) -> a[ax + indexMap[mx + i]] = a_);
    }

    // Species

    @Override
    public abstract LongSpecies<S> species();

    public static abstract class LongSpecies<S extends Vector.Shape> extends Vector.Species<Long, S> {
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

        public LongVector<S> random() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            return op(i -> r.nextLong());
        }

        public LongVector<S> scalars(long... es) {
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
         * @throws IndexOutOfBoundsException if {@code i < 0} or
         * {@code i < a.length - this.length()}
         */
        public LongVector<S> fromArray(long[] a, int i) {
            return op(n -> a[i + n]);
        }

        /**
         * Loads a vector from an array starting at offset.
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
         * @throws IndexOutOfBoundsException if {@code i < 0} or
         * {@code i < a.length - this.length()}
         */
        public LongVector<S> fromArray(long[] a, int i, Mask<Long, S> m) {
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
         * {@code j < indexMap.length - this.length()}, or for any vector
         * lane index {@code N} the result of {@code i + indexMap[j + N]} is
         * {@code < 0} or {@code >= a.length}
         */
        public LongVector<S> fromArray(long[] a, int i, int[] indexMap, int j) {
            return op(n -> a[i + indexMap[j + n]]);
        }

        /**
         * Loads a vector from an array using indexes obtained from an index
         * map.
         * <p>
         * For each vector lane, where {@code N} is the vector lane index,
         * if the mask lane at index {@code N} is set then the array element at
         * index {@code i + indexMap[j + N]} is placed into the resulting vector
         * at lane index {@code N}, otherwise the default element value is
         * placed into the resulting vector at lane index {@code N}.
         *
         * @param a the array
         * @param i the offset into the array, may be negative if relative
         * indexes in the index map compensate to produce a value within the
         * array bounds
         * @param m the mask
         * @param indexMap the index map
         * @param j the offset into the index map
         * @return the vector loaded from an array
         * @throws IndexOutOfBoundsException if {@code j < 0}, or
         * {@code j < indexMap.length - this.length()}, or for any vector
         * lane index {@code N} the result of {@code i + indexMap[j + N]} is
         * {@code < 0} or {@code >= a.length}
         */
        public LongVector<S> fromArray(long[] a, int i, Mask<Long, S> m, int[] indexMap, int j) {
            return op(m, n -> a[i + indexMap[j + n]]);
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

        @Override
        public <F, T extends Shape> LongVector<S> reshape(Vector<F, T> o) {
            int blen = Math.max(o.species().bitSize(), bitSize()) / Byte.SIZE;
            ByteBuffer bb = ByteBuffer.allocate(blen).order(ByteOrder.nativeOrder());
            o.intoByteBuffer(bb, 0);
            return fromByteBuffer(bb, 0);
        }

        @Override
        @ForceInline
        public <F> LongVector<S> rebracket(Vector<F, S> o) {
            return reshape(o);
        }

        @Override
        @ForceInline
        public <T extends Shape> LongVector<S> resize(Vector<Long, T> o) {
            return reshape(o);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> LongVector<S> cast(Vector<F, T> v) {
            // Allocate array of required size
            long[] a = new long[length()];

            Class<?> vtype = v.species().elementType();
            int limit = Math.min(v.species().length(), length());
            if (vtype == byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else if (vtype == short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else if (vtype == int.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else if (vtype == long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else if (vtype == float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else if (vtype == double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (long) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
        }

    }

    /**
     * Finds the preferred species for an element type of {@code long}.
     * <p>
     * A preferred species is a species chosen by the platform that has a
     * shape of maximal bit size.  A preferred species for different element
     * types will have the same shape, and therefore vectors, masks, and
     * shuffles created from such species will be shape compatible.
     *
     * @return the preferred species for an element type of {@code long}
     */
    @SuppressWarnings("unchecked")
    public static LongSpecies<?> preferredSpeciesInstance() {
        return (LongSpecies<?>) Vector.preferredSpeciesInstance(long.class);
    }

    /**
     * Finds a species for an element type of {@code long} and shape.
     *
     * @param s the shape
     * @param <S> the type of shape
     * @return a species for an element type of {@code long} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    @SuppressWarnings("unchecked")
    public static <S extends Shape> LongSpecies<S> speciesInstance(S s) {
        Objects.requireNonNull(s);
        if (s == Shapes.S_64_BIT) {
            return (LongSpecies<S>) Long64Vector.SPECIES;
        } else if (s == Shapes.S_128_BIT) {
            return (LongSpecies<S>) Long128Vector.SPECIES;
        } else if (s == Shapes.S_256_BIT) {
            return (LongSpecies<S>) Long256Vector.SPECIES;
        } else if (s == Shapes.S_512_BIT) {
            return (LongSpecies<S>) Long512Vector.SPECIES;
        } else {
            throw new IllegalArgumentException("Bad shape: " + s);
        }
    }
}
