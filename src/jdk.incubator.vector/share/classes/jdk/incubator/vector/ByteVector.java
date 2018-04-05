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
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("cast")
public abstract class ByteVector<S extends Vector.Shape> extends Vector<Byte,S> {

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
    public abstract ByteVector<S> add(byte b);

    @Override
    public ByteVector<S> add(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a + b));
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
    public abstract ByteVector<S> add(byte b, Mask<Byte, S> m);

    @Override
    public ByteVector<S> addSaturate(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract ByteVector<S> addSaturate(byte o);

    @Override
    public ByteVector<S> addSaturate(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract ByteVector<S> addSaturate(byte o, Mask<Byte, S> m);

    @Override
    public ByteVector<S> sub(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a - b));
    }

    public abstract ByteVector<S> sub(byte o);

    @Override
    public ByteVector<S> sub(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a - b));
    }

    public abstract ByteVector<S> sub(byte o, Mask<Byte, S> m);

    @Override
    public ByteVector<S> subSaturate(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) ((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b));
    }

    public abstract ByteVector<S> subSaturate(byte o);

    @Override
    public ByteVector<S> subSaturate(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) ((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b));
    }

    public abstract ByteVector<S> subSaturate(byte o, Mask<Byte, S> m);

    @Override
    public ByteVector<S> mul(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a * b));
    }

    public abstract ByteVector<S> mul(byte o);

    @Override
    public ByteVector<S> mul(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a * b));
    }

    public abstract ByteVector<S> mul(byte o, Mask<Byte, S> m);

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

    public abstract ByteVector<S> min(byte o);

    @Override
    public ByteVector<S> max(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (a >= b) ? a : b);
    }

    public abstract ByteVector<S> max(byte o);

    @Override
    public Mask<Byte, S> equal(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a == b);
    }

    public abstract Mask<Byte, S> equal(byte o);

    @Override
    public Mask<Byte, S> notEqual(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a != b);
    }

    public abstract Mask<Byte, S> notEqual(byte o);

    @Override
    public Mask<Byte, S> lessThan(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a < b);
    }

    public abstract Mask<Byte, S> lessThan(byte o);

    @Override
    public Mask<Byte, S> lessThanEq(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a <= b);
    }

    public abstract Mask<Byte, S> lessThanEq(byte o);

    @Override
    public Mask<Byte, S> greaterThan(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a > b);
    }

    public abstract Mask<Byte, S> greaterThan(byte o);

    @Override
    public Mask<Byte, S> greaterThanEq(Vector<Byte,S> o) {
        return bTest(o, (i, a, b) -> a >= b);
    }

    public abstract Mask<Byte, S> greaterThanEq(byte o);

    @Override
    public ByteVector<S> blend(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, (i, a, b) -> m.getElement(i) ? b : a);
    }

    public abstract ByteVector<S> blend(byte o, Mask<Byte, S> m);

    @Override
    public abstract ByteVector<S> shuffle(Vector<Byte,S> o, Shuffle<Byte, S> m);

    @Override
    public abstract ByteVector<S> swizzle(Shuffle<Byte, S> m);

    @Override
    @ForceInline
    public <T extends Shape> ByteVector<T> resize(Species<Byte, T> species) {
        return (ByteVector<T>) species.reshape(this);
    }

    @Override
    public abstract ByteVector<S> rotateEL(int i);

    @Override
    public abstract ByteVector<S> rotateER(int i);

    @Override
    public abstract ByteVector<S> shiftEL(int i);

    @Override
    public abstract ByteVector<S> shiftER(int i);


    public ByteVector<S> and(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a & b));
    }

    public abstract ByteVector<S> and(byte o);

    public ByteVector<S> and(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a & b));
    }

    public abstract ByteVector<S> and(byte o, Mask<Byte, S> m);

    public ByteVector<S> or(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a | b));
    }

    public abstract ByteVector<S> or(byte o);

    public ByteVector<S> or(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a | b));
    }

    public abstract ByteVector<S> or(byte o, Mask<Byte, S> m);

    public ByteVector<S> xor(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a ^ b));
    }

    public abstract ByteVector<S> xor(byte o);

    public ByteVector<S> xor(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a ^ b));
    }

    public abstract ByteVector<S> xor(byte o, Mask<Byte, S> m);

    public ByteVector<S> not() {
        return uOp((i, a) -> (byte) (~a));
    }

    public ByteVector<S> not(Mask<Byte, S> m) {
        return uOp(m, (i, a) -> (byte) (~a));
    }

    // logical shift left
    public ByteVector<S> shiftL(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a << b));
    }

    public ByteVector<S> shiftL(int s) {
        return uOp((i, a) -> (byte) (a << s));
    }

    public ByteVector<S> shiftL(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a << b));
    }

    public ByteVector<S> shiftL(int s, Mask<Byte, S> m) {
        return uOp(m, (i, a) -> (byte) (a << s));
    }

    // logical, or unsigned, shift right
    public ByteVector<S> shiftR(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a >>> b));
    }

    public ByteVector<S> shiftR(int s) {
        return uOp((i, a) -> (byte) (a >>> s));
    }

    public ByteVector<S> shiftR(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a >>> b));
    }

    public ByteVector<S> shiftR(int s, Mask<Byte, S> m) {
        return uOp(m, (i, a) -> (byte) (a >>> s));
    }

    // arithmetic, or signed, shift right
    public ByteVector<S> ashiftR(Vector<Byte,S> o) {
        return bOp(o, (i, a, b) -> (byte) (a >> b));
    }

    public ByteVector<S> aShiftR(int s) {
        return uOp((i, a) -> (byte) (a >> s));
    }

    public ByteVector<S> ashiftR(Vector<Byte,S> o, Mask<Byte, S> m) {
        return bOp(o, m, (i, a, b) -> (byte) (a >> b));
    }

    public ByteVector<S> aShiftR(int s, Mask<Byte, S> m) {
        return uOp(m, (i, a) -> (byte) (a >> s));
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

    /**
     * Sums all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the addition
     * operation ({@code +}) is applied to lane elements, and the identity value
     * is {@code 0}.
     *
     * @return the sum of all the lane elements of this vector
     */
    public byte addAll() {
        return rOp((byte) 0, (i, a, b) -> (byte) (a + b));
    }

    public byte subAll() {
        return rOp((byte) 0, (i, a, b) -> (byte) (a - b));
    }

    public byte mulAll() {
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

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     */
    public abstract byte get(int i);

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
    public abstract ByteVector<S> with(int i, byte e);

    // Type specific extractors

    @ForceInline
    public byte[] toArray() {
        byte[] a = new byte[species().length()];
        intoArray(a, 0);
        return a;
    }

    public void intoArray(byte[] a, int ax) {
        forEach((i, a_) -> a[ax + i] = a_);
    }

    public void intoArray(byte[] a, int ax, Mask<Byte, S> m) {
        forEach(m, (i, a_) -> a[ax + i] = a_);
    }

    public void intoArray(byte[] a, int ax, int[] indexMap, int mx) {
        forEach((i, a_) -> a[ax + indexMap[mx + i]] = a_);
    }

    public void intoArray(byte[] a, int ax, Mask<Byte, S> m, int[] indexMap, int mx) {
        forEach(m, (i, a_) -> a[ax + indexMap[mx + i]] = a_);
    }

    // Species

    @Override
    public abstract ByteSpecies<S> species();

    public static abstract class ByteSpecies<S extends Vector.Shape> extends Vector.Species<Byte, S> {
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

        public ByteVector<S> random() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            return op(i -> (byte) r.nextInt());
        }

        public ByteVector<S> scalars(byte... es) {
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
        public ByteVector<S> fromArray(byte[] a, int i) {
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
        public ByteVector<S> fromArray(byte[] a, int i, Mask<Byte, S> m) {
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
        public ByteVector<S> fromArray(byte[] a, int i, int[] indexMap, int j) {
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
        public ByteVector<S> fromArray(byte[] a, int i, Mask<Byte, S> m, int[] indexMap, int j) {
            return op(m, n -> a[i + indexMap[j + n]]);
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

        @Override
        public <F, T extends Shape> ByteVector<S> reshape(Vector<F, T> o) {
            int blen = Math.max(o.species().bitSize(), bitSize()) / Byte.SIZE;
            ByteBuffer bb = ByteBuffer.allocate(blen).order(ByteOrder.nativeOrder());
            o.intoByteBuffer(bb, 0);
            return fromByteBuffer(bb, 0);
        }

        @Override
        @ForceInline
        public <F> ByteVector<S> rebracket(Vector<F, S> o) {
            return reshape(o);
        }

        @Override
        @ForceInline
        public <T extends Shape> ByteVector<S> resize(Vector<Byte, T> o) {
            return reshape(o);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> ByteVector<S> cast(Vector<F, T> v) {
            // Allocate array of required size
            byte[] a = new byte[length()];

            Class<?> vtype = v.species().elementType();
            int limit = Math.min(v.species().length(), length());
            if (vtype == Byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else if (vtype == Short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else if (vtype == Integer.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else if (vtype == Long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else if (vtype == Float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else if (vtype == Double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (byte) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
        }

    }
}
