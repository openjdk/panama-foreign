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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import java.util.function.Function;
import java.util.concurrent.ThreadLocalRandom;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;


/**
 * A specialized {@link Vector} representing an ordered immutable sequence of
 * {@code double} values.
 */
@SuppressWarnings("cast")
public abstract class DoubleVector extends Vector<Double> {

    DoubleVector() {}

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_DOUBLE_INDEX_SCALE);

    // Unary operator

    interface FUnOp {
        double apply(int i, double a);
    }

    abstract DoubleVector uOp(FUnOp f);

    abstract DoubleVector uOp(VectorMask<Double> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        double apply(int i, double a, double b);
    }

    abstract DoubleVector bOp(Vector<Double> v, FBinOp f);

    abstract DoubleVector bOp(Vector<Double> v, VectorMask<Double> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        double apply(int i, double a, double b, double c);
    }

    abstract DoubleVector tOp(Vector<Double> v1, Vector<Double> v2, FTriOp f);

    abstract DoubleVector tOp(Vector<Double> v1, Vector<Double> v2, VectorMask<Double> m, FTriOp f);

    // Reduction operator

    abstract double rOp(double v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, double a, double b);
    }

    abstract VectorMask<Double> bTest(Vector<Double> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, double a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(VectorMask<Double> m, FUnCon f);

    // Static factories

    /**
     * Returns a vector where all lane elements are set to the default
     * primitive value.
     *
     * @param species species of desired vector
     * @return a zero vector of given species
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static DoubleVector zero(VectorSpecies<Double> species) {
        return VectorIntrinsics.broadcastCoerced((Class<DoubleVector>) species.vectorType(), double.class, species.length(),
                                                 Double.doubleToLongBits(0.0f), species,
                                                 ((bits, s) -> ((DoubleSpecies)s).op(i -> Double.longBitsToDouble((long)bits))));
    }

    /**
     * Loads a vector from a byte array starting at an offset.
     * <p>
     * Bytes are composed into primitive lane elements according to the
     * native byte order of the underlying platform
     * <p>
     * This method behaves as if it returns the result of calling the
     * byte buffer, offset, and mask accepting
     * {@link #fromByteBuffer(VectorSpecies, ByteBuffer, int, VectorMask) method} as follows:
     * <pre>{@code
     * return fromByteBuffer(species, ByteBuffer.wrap(a), offset, VectorMask.allTrue());
     * }</pre>
     *
     * @param species species of desired vector
     * @param a the byte array
     * @param offset the offset into the array
     * @return a vector loaded from a byte array
     * @throws IndexOutOfBoundsException if {@code i < 0} or
     * {@code offset > a.length - (species.length() * species.elementSize() / Byte.SIZE)}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static DoubleVector fromByteArray(VectorSpecies<Double> species, byte[] a, int offset) {
        Objects.requireNonNull(a);
        offset = VectorIntrinsics.checkIndex(offset, a.length, species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<DoubleVector>) species.vectorType(), double.class, species.length(),
                                     a, ((long) offset) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                     a, offset, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
                                         DoubleBuffer tb = bbc.asDoubleBuffer();
                                         return ((DoubleSpecies)s).op(i -> tb.get());
                                     });
    }

    /**
     * Loads a vector from a byte array starting at an offset and using a
     * mask.
     * <p>
     * Bytes are composed into primitive lane elements according to the
     * native byte order of the underlying platform.
     * <p>
     * This method behaves as if it returns the result of calling the
     * byte buffer, offset, and mask accepting
     * {@link #fromByteBuffer(VectorSpecies, ByteBuffer, int, VectorMask) method} as follows:
     * <pre>{@code
     * return fromByteBuffer(species, ByteBuffer.wrap(a), offset, m);
     * }</pre>
     *
     * @param species species of desired vector
     * @param a the byte array
     * @param offset the offset into the array
     * @param m the mask
     * @return a vector loaded from a byte array
     * @throws IndexOutOfBoundsException if {@code offset < 0} or
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set
     * {@code offset >= a.length - (N * species.elementSize() / Byte.SIZE)}
     */
    @ForceInline
    public static DoubleVector fromByteArray(VectorSpecies<Double> species, byte[] a, int offset, VectorMask<Double> m) {
        return zero(species).blend(fromByteArray(species, a, offset), m);
    }

    /**
     * Loads a vector from an array starting at offset.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index, the
     * array element at index {@code offset + N} is placed into the
     * resulting vector at lane index {@code N}.
     *
     * @param species species of desired vector
     * @param a the array
     * @param offset the offset into the array
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException if {@code offset < 0}, or
     * {@code offset > a.length - species.length()}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static DoubleVector fromArray(VectorSpecies<Double> species, double[] a, int offset){
        Objects.requireNonNull(a);
        offset = VectorIntrinsics.checkIndex(offset, a.length, species.length());
        return VectorIntrinsics.load((Class<DoubleVector>) species.vectorType(), double.class, species.length(),
                                     a, (((long) offset) << ARRAY_SHIFT) + Unsafe.ARRAY_DOUBLE_BASE_OFFSET,
                                     a, offset, species,
                                     (c, idx, s) -> ((DoubleSpecies)s).op(n -> c[idx + n]));
    }


    /**
     * Loads a vector from an array starting at offset and using a mask.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * if the mask lane at index {@code N} is set then the array element at
     * index {@code offset + N} is placed into the resulting vector at lane index
     * {@code N}, otherwise the default element value is placed into the
     * resulting vector at lane index {@code N}.
     *
     * @param species species of desired vector
     * @param a the array
     * @param offset the offset into the array
     * @param m the mask
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException if {@code offset < 0}, or
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set {@code offset > a.length - N}
     */
    @ForceInline
    public static DoubleVector fromArray(VectorSpecies<Double> species, double[] a, int offset, VectorMask<Double> m) {
        return zero(species).blend(fromArray(species, a, offset), m);
    }

    /**
     * Loads a vector from an array using indexes obtained from an index
     * map.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index, the
     * array element at index {@code a_offset + indexMap[i_offset + N]} is placed into the
     * resulting vector at lane index {@code N}.
     *
     * @param species species of desired vector
     * @param a the array
     * @param a_offset the offset into the array, may be negative if relative
     * indexes in the index map compensate to produce a value within the
     * array bounds
     * @param indexMap the index map
     * @param i_offset the offset into the index map
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException if {@code i_offset < 0}, or
     * {@code i_offset > indexMap.length - species.length()},
     * or for any vector lane index {@code N} the result of
     * {@code a_offset + indexMap[i_offset + N]} is {@code < 0} or {@code >= a.length}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static DoubleVector fromArray(VectorSpecies<Double> species, double[] a, int a_offset, int[] indexMap, int i_offset) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(indexMap);

        if (species.length() == 1) {
          return DoubleVector.fromArray(species, a, a_offset + indexMap[i_offset]);
        }

        // Index vector: vix[0:n] = k -> a_offset + indexMap[i_offset + k]
        IntVector vix = IntVector.fromArray(IntVector.species(species.indexShape()), indexMap, i_offset).add(a_offset);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        return VectorIntrinsics.loadWithMap((Class<DoubleVector>) species.vectorType(), double.class, species.length(),
                                            IntVector.species(species.indexShape()).vectorType(), a, Unsafe.ARRAY_DOUBLE_BASE_OFFSET, vix,
                                            a, a_offset, indexMap, i_offset, species,
                                            (double[] c, int idx, int[] iMap, int idy, VectorSpecies<Double> s) ->
                                                ((DoubleSpecies)s).op(n -> c[idx + iMap[idy+n]]));
        }

    /**
     * Loads a vector from an array using indexes obtained from an index
     * map and using a mask.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * if the mask lane at index {@code N} is set then the array element at
     * index {@code a_offset + indexMap[i_offset + N]} is placed into the resulting vector
     * at lane index {@code N}.
     *
     * @param species species of desired vector
     * @param a the array
     * @param a_offset the offset into the array, may be negative if relative
     * indexes in the index map compensate to produce a value within the
     * array bounds
     * @param m the mask
     * @param indexMap the index map
     * @param i_offset the offset into the index map
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException if {@code i_offset < 0}, or
     * {@code i_offset > indexMap.length - species.length()},
     * or for any vector lane index {@code N} where the mask at lane
     * {@code N} is set the result of {@code a_offset + indexMap[i_offset + N]} is
     * {@code < 0} or {@code >= a.length}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static DoubleVector fromArray(VectorSpecies<Double> species, double[] a, int a_offset, VectorMask<Double> m, int[] indexMap, int i_offset) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        return zero(species).blend(fromArray(species, a, a_offset, indexMap, i_offset), m);
    }


    /**
     * Loads a vector from a {@link ByteBuffer byte buffer} starting at an
     * offset into the byte buffer.
     * <p>
     * Bytes are composed into primitive lane elements according to the
     * native byte order of the underlying platform.
     * <p>
     * This method behaves as if it returns the result of calling the
     * byte buffer, offset, and mask accepting
     * {@link #fromByteBuffer(VectorSpecies, ByteBuffer, int, VectorMask)} method} as follows:
     * <pre>{@code
     *   return fromByteBuffer(b, offset, VectorMask.allTrue())
     * }</pre>
     *
     * @param species species of desired vector
     * @param bb the byte buffer
     * @param offset the offset into the byte buffer
     * @return a vector loaded from a byte buffer
     * @throws IndexOutOfBoundsException if the offset is {@code < 0},
     * or {@code > b.limit()},
     * or if there are fewer than
     * {@code species.length() * species.elementSize() / Byte.SIZE} bytes
     * remaining in the byte buffer from the given offset
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static DoubleVector fromByteBuffer(VectorSpecies<Double> species, ByteBuffer bb, int offset) {
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        offset = VectorIntrinsics.checkIndex(offset, bb.limit(), species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<DoubleVector>) species.vectorType(), double.class, species.length(),
                                     U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + offset,
                                     bb, offset, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                         DoubleBuffer tb = bbc.asDoubleBuffer();
                                         return ((DoubleSpecies)s).op(i -> tb.get());
                                     });
    }

    /**
     * Loads a vector from a {@link ByteBuffer byte buffer} starting at an
     * offset into the byte buffer and using a mask.
     * <p>
     * This method behaves as if the byte buffer is viewed as a primitive
     * {@link java.nio.Buffer buffer} for the primitive element type,
     * according to the native byte order of the underlying platform, and
     * the returned vector is loaded with a mask from a primitive array
     * obtained from the primitive buffer.
     * The following pseudocode expresses the behaviour, where
     * {@code EBuffer} is the primitive buffer type, {@code e} is the
     * primitive element type, and {@code ESpecies} is the primitive
     * species for {@code e}:
     * <pre>{@code
     * EBuffer eb = b.duplicate().
     *     order(ByteOrder.nativeOrder()).position(offset).
     *     asEBuffer();
     * e[] es = new e[species.length()];
     * for (int n = 0; n < t.length; n++) {
     *     if (m.isSet(n))
     *         es[n] = eb.get(n);
     * }
     * EVector r = EVector.fromArray(es, 0, m);
     * }</pre>
     *
     * @param species species of desired vector
     * @param bb the byte buffer
     * @param offset the offset into the byte buffer
     * @param m the mask
     * @return a vector loaded from a byte buffer
     * @throws IndexOutOfBoundsException if the offset is {@code < 0},
     * or {@code > b.limit()},
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set
     * {@code offset >= b.limit() - (N * species.elementSize() / Byte.SIZE)}
     */
    @ForceInline
    public static DoubleVector fromByteBuffer(VectorSpecies<Double> species, ByteBuffer bb, int offset, VectorMask<Double> m) {
        return zero(species).blend(fromByteBuffer(species, bb, offset), m);
    }

    /**
     * Returns a vector where all lane elements are set to the primitive
     * value {@code e}.
     *
     * @param species species of the desired vector
     * @param e the value to be broadcasted
     * @return a vector of vector where all lane elements are set to
     * the primitive value {@code e}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static DoubleVector broadcast(VectorSpecies<Double> species, double e) {
        return VectorIntrinsics.broadcastCoerced(
            (Class<DoubleVector>) species.vectorType(), double.class, species.length(),
            Double.doubleToLongBits(e), species,
            ((bits, sp) -> ((DoubleSpecies)sp).op(i -> Double.longBitsToDouble((long)bits))));
    }

    /**
     * Returns a vector where each lane element is set to given
     * primitive values.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index, the
     * the primitive value at index {@code N} is placed into the resulting
     * vector at lane index {@code N}.
     *
     * @param species species of the desired vector
     * @param es the given primitive values
     * @return a vector where each lane element is set to given primitive
     * values
     * @throws IndexOutOfBoundsException if {@code es.length < species.length()}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static DoubleVector scalars(VectorSpecies<Double> species, double... es) {
        Objects.requireNonNull(es);
        int ix = VectorIntrinsics.checkIndex(0, es.length, species.length());
        return VectorIntrinsics.load((Class<DoubleVector>) species.vectorType(), double.class, species.length(),
                                     es, Unsafe.ARRAY_DOUBLE_BASE_OFFSET,
                                     es, ix, species,
                                     (c, idx, sp) -> ((DoubleSpecies)sp).op(n -> c[idx + n]));
    }

    /**
     * Returns a vector where the first lane element is set to the primtive
     * value {@code e}, all other lane elements are set to the default
     * value.
     *
     * @param species species of the desired vector
     * @param e the value
     * @return a vector where the first lane element is set to the primitive
     * value {@code e}
     */
    @ForceInline
    public static final DoubleVector single(VectorSpecies<Double> species, double e) {
        return zero(species).with(0, e);
    }

    /**
     * Returns a vector where each lane element is set to a randomly
     * generated primitive value.
     *
     * The semantics are equivalent to calling
     * {@link ThreadLocalRandom#nextDouble()}
     *
     * @param species species of the desired vector
     * @return a vector where each lane elements is set to a randomly
     * generated primitive value
     */
    public static DoubleVector random(VectorSpecies<Double> species) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return ((DoubleSpecies)species).op(i -> r.nextDouble());
    }

    // Ops

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector add(Vector<Double> v);

    /**
     * Adds this vector to the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive addition operation
     * ({@code +}) to each lane.
     *
     * @param s the input scalar
     * @return the result of adding this vector to the broadcast of an input
     * scalar
     */
    public abstract DoubleVector add(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector add(Vector<Double> v, VectorMask<Double> m);

    /**
     * Adds this vector to broadcast of an input scalar,
     * selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive addition operation
     * ({@code +}) to each lane.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the result of adding this vector to the broadcast of an input
     * scalar
     */
    public abstract DoubleVector add(double s, VectorMask<Double> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector sub(Vector<Double> v);

    /**
     * Subtracts the broadcast of an input scalar from this vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive subtraction
     * operation ({@code -}) to each lane.
     *
     * @param s the input scalar
     * @return the result of subtracting the broadcast of an input
     * scalar from this vector
     */
    public abstract DoubleVector sub(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector sub(Vector<Double> v, VectorMask<Double> m);

    /**
     * Subtracts the broadcast of an input scalar from this vector, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive subtraction
     * operation ({@code -}) to each lane.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the result of subtracting the broadcast of an input
     * scalar from this vector
     */
    public abstract DoubleVector sub(double s, VectorMask<Double> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector mul(Vector<Double> v);

    /**
     * Multiplies this vector with the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive multiplication
     * operation ({@code *}) to each lane.
     *
     * @param s the input scalar
     * @return the result of multiplying this vector with the broadcast of an
     * input scalar
     */
    public abstract DoubleVector mul(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector mul(Vector<Double> v, VectorMask<Double> m);

    /**
     * Multiplies this vector with the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive multiplication
     * operation ({@code *}) to each lane.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the result of multiplying this vector with the broadcast of an
     * input scalar
     */
    public abstract DoubleVector mul(double s, VectorMask<Double> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector neg();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector neg(VectorMask<Double> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector abs();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector abs(VectorMask<Double> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector min(Vector<Double> v);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector min(Vector<Double> v, VectorMask<Double> m);

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to each lane.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract DoubleVector min(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector max(Vector<Double> v);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector max(Vector<Double> v, VectorMask<Double> m);

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to each lane.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract DoubleVector max(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Double> equal(Vector<Double> v);

    /**
     * Tests if this vector is equal to the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive equals
     * operation ({@code ==}) each lane.
     *
     * @param s the input scalar
     * @return the result mask of testing if this vector is equal to the
     * broadcast of an input scalar
     */
    public abstract VectorMask<Double> equal(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Double> notEqual(Vector<Double> v);

    /**
     * Tests if this vector is not equal to the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive not equals
     * operation ({@code !=}) to each lane.
     *
     * @param s the input scalar
     * @return the result mask of testing if this vector is not equal to the
     * broadcast of an input scalar
     */
    public abstract VectorMask<Double> notEqual(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Double> lessThan(Vector<Double> v);

    /**
     * Tests if this vector is less than the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive less than
     * operation ({@code <}) to each lane.
     *
     * @param s the input scalar
     * @return the mask result of testing if this vector is less than the
     * broadcast of an input scalar
     */
    public abstract VectorMask<Double> lessThan(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Double> lessThanEq(Vector<Double> v);

    /**
     * Tests if this vector is less or equal to the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive less than
     * or equal to operation ({@code <=}) to each lane.
     *
     * @param s the input scalar
     * @return the mask result of testing if this vector is less than or equal
     * to the broadcast of an input scalar
     */
    public abstract VectorMask<Double> lessThanEq(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Double> greaterThan(Vector<Double> v);

    /**
     * Tests if this vector is greater than the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive greater than
     * operation ({@code >}) to each lane.
     *
     * @param s the input scalar
     * @return the mask result of testing if this vector is greater than the
     * broadcast of an input scalar
     */
    public abstract VectorMask<Double> greaterThan(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Double> greaterThanEq(Vector<Double> v);

    /**
     * Tests if this vector is greater than or equal to the broadcast of an
     * input scalar.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive greater than
     * or equal to operation ({@code >=}) to each lane.
     *
     * @param s the input scalar
     * @return the mask result of testing if this vector is greater than or
     * equal to the broadcast of an input scalar
     */
    public abstract VectorMask<Double> greaterThanEq(double s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector blend(Vector<Double> v, VectorMask<Double> m);

    /**
     * Blends the lane elements of this vector with those of the broadcast of an
     * input scalar, selecting lanes controlled by a mask.
     * <p>
     * For each lane of the mask, at lane index {@code N}, if the mask lane
     * is set then the lane element at {@code N} from the input vector is
     * selected and placed into the resulting vector at {@code N},
     * otherwise the the lane element at {@code N} from this input vector is
     * selected and placed into the resulting vector at {@code N}.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the result of blending the lane elements of this vector with
     * those of the broadcast of an input scalar
     */
    public abstract DoubleVector blend(double s, VectorMask<Double> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector rearrange(Vector<Double> v,
                                                      VectorShuffle<Double> s, VectorMask<Double> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector rearrange(VectorShuffle<Double> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector reshape(VectorSpecies<Double> s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector rotateLanesLeft(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector rotateLanesRight(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector shiftLanesLeft(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract DoubleVector shiftLanesRight(int i);

    /**
     * Divides this vector by an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive division
     * operation ({@code /}) to each lane.
     *
     * @param v the input vector
     * @return the result of dividing this vector by the input vector
     */
    public abstract DoubleVector div(Vector<Double> v);

    /**
     * Divides this vector by the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive division
     * operation ({@code /}) to each lane.
     *
     * @param s the input scalar
     * @return the result of dividing this vector by the broadcast of an input
     * scalar
     */
    public abstract DoubleVector div(double s);

    /**
     * Divides this vector by an input vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive division
     * operation ({@code /}) to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of dividing this vector by the input vector
     */
    public abstract DoubleVector div(Vector<Double> v, VectorMask<Double> m);

    /**
     * Divides this vector by the broadcast of an input scalar, selecting lane
     * elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive division
     * operation ({@code /}) to each lane.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the result of dividing this vector by the broadcast of an input
     * scalar
     */
    public abstract DoubleVector div(double s, VectorMask<Double> m);

    /**
     * Calculates the square root of this vector.
     * <p>
     * This is a lane-wise unary operation which applies the {@link Math#sqrt} operation
     * to each lane.
     *
     * @return the square root of this vector
     */
    public abstract DoubleVector sqrt();

    /**
     * Calculates the square root of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a lane-wise unary operation which applies the {@link Math#sqrt} operation
     * to each lane.
     *
     * @param m the mask controlling lane selection
     * @return the square root of this vector
     */
    public DoubleVector sqrt(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.sqrt((double) a));
    }

    /**
     * Calculates the trigonometric tangent of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#tan} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#tan}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#tan}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the tangent of this vector
     */
    public DoubleVector tan() {
        return uOp((i, a) -> (double) Math.tan((double) a));
    }

    /**
     * Calculates the trigonometric tangent of this vector, selecting lane
     * elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#tan}
     *
     * @param m the mask controlling lane selection
     * @return the tangent of this vector
     */
    public DoubleVector tan(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.tan((double) a));
    }

    /**
     * Calculates the hyperbolic tangent of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#tanh} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#tanh}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#tanh}
     * specifications. The computed result will be within 2.5 ulps of the
     * exact result.
     *
     * @return the hyperbolic tangent of this vector
     */
    public DoubleVector tanh() {
        return uOp((i, a) -> (double) Math.tanh((double) a));
    }

    /**
     * Calculates the hyperbolic tangent of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#tanh}
     *
     * @param m the mask controlling lane selection
     * @return the hyperbolic tangent of this vector
     */
    public DoubleVector tanh(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.tanh((double) a));
    }

    /**
     * Calculates the trigonometric sine of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#sin} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#sin}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#sin}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the sine of this vector
     */
    public DoubleVector sin() {
        return uOp((i, a) -> (double) Math.sin((double) a));
    }

    /**
     * Calculates the trigonometric sine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#sin}
     *
     * @param m the mask controlling lane selection
     * @return the sine of this vector
     */
    public DoubleVector sin(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.sin((double) a));
    }

    /**
     * Calculates the hyperbolic sine of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#sinh} operation applied to each lane.
     * The implementation is not required to return same
     * results as  {@link Math#sinh}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#sinh}
     * specifications. The computed result will be within 2.5 ulps of the
     * exact result.
     *
     * @return the hyperbolic sine of this vector
     */
    public DoubleVector sinh() {
        return uOp((i, a) -> (double) Math.sinh((double) a));
    }

    /**
     * Calculates the hyperbolic sine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#sinh}
     *
     * @param m the mask controlling lane selection
     * @return the hyperbolic sine of this vector
     */
    public DoubleVector sinh(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.sinh((double) a));
    }

    /**
     * Calculates the trigonometric cosine of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#cos} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#cos}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#cos}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the cosine of this vector
     */
    public DoubleVector cos() {
        return uOp((i, a) -> (double) Math.cos((double) a));
    }

    /**
     * Calculates the trigonometric cosine of this vector, selecting lane
     * elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#cos}
     *
     * @param m the mask controlling lane selection
     * @return the cosine of this vector
     */
    public DoubleVector cos(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.cos((double) a));
    }

    /**
     * Calculates the hyperbolic cosine of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#cosh} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#cosh}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#cosh}
     * specifications. The computed result will be within 2.5 ulps of the
     * exact result.
     *
     * @return the hyperbolic cosine of this vector
     */
    public DoubleVector cosh() {
        return uOp((i, a) -> (double) Math.cosh((double) a));
    }

    /**
     * Calculates the hyperbolic cosine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#cosh}
     *
     * @param m the mask controlling lane selection
     * @return the hyperbolic cosine of this vector
     */
    public DoubleVector cosh(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.cosh((double) a));
    }

    /**
     * Calculates the arc sine of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#asin} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#asin}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#asin}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the arc sine of this vector
     */
    public DoubleVector asin() {
        return uOp((i, a) -> (double) Math.asin((double) a));
    }

    /**
     * Calculates the arc sine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#asin}
     *
     * @param m the mask controlling lane selection
     * @return the arc sine of this vector
     */
    public DoubleVector asin(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.asin((double) a));
    }

    /**
     * Calculates the arc cosine of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#acos} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#acos}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#acos}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the arc cosine of this vector
     */
    public DoubleVector acos() {
        return uOp((i, a) -> (double) Math.acos((double) a));
    }

    /**
     * Calculates the arc cosine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#acos}
     *
     * @param m the mask controlling lane selection
     * @return the arc cosine of this vector
     */
    public DoubleVector acos(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.acos((double) a));
    }

    /**
     * Calculates the arc tangent of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#atan} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#atan}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#atan}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the arc tangent of this vector
     */
    public DoubleVector atan() {
        return uOp((i, a) -> (double) Math.atan((double) a));
    }

    /**
     * Calculates the arc tangent of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#atan}
     *
     * @param m the mask controlling lane selection
     * @return the arc tangent of this vector
     */
    public DoubleVector atan(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.atan((double) a));
    }

    /**
     * Calculates the arc tangent of this vector divided by an input vector.
     * <p>
     * This is a lane-wise binary operation with same semantic definition as
     * {@link Math#atan2} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#atan2}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#atan2}
     * specifications. The computed result will be within 2 ulps of the
     * exact result.
     *
     * @param v the input vector
     * @return the arc tangent of this vector divided by the input vector
     */
    public DoubleVector atan2(Vector<Double> v) {
        return bOp(v, (i, a, b) -> (double) Math.atan2((double) a, (double) b));
    }

    /**
     * Calculates the arc tangent of this vector divided by the broadcast of an
     * an input scalar.
     * <p>
     * This is a lane-wise binary operation with same semantic definition as
     * {@link Math#atan2} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#atan2}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#atan2}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @param s the input scalar
     * @return the arc tangent of this vector over the input vector
     */
    public abstract DoubleVector atan2(double s);

    /**
     * Calculates the arc tangent of this vector divided by an input vector,
     * selecting lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#atan2}
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the arc tangent of this vector divided by the input vector
     */
    public DoubleVector atan2(Vector<Double> v, VectorMask<Double> m) {
        return bOp(v, m, (i, a, b) -> (double) Math.atan2((double) a, (double) b));
    }

    /**
     * Calculates the arc tangent of this vector divided by the broadcast of an
     * an input scalar, selecting lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#atan2}
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the arc tangent of this vector over the input vector
     */
    public abstract DoubleVector atan2(double s, VectorMask<Double> m);

    /**
     * Calculates the cube root of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#cbrt} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#cbrt}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#cbrt}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the cube root of this vector
     */
    public DoubleVector cbrt() {
        return uOp((i, a) -> (double) Math.cbrt((double) a));
    }

    /**
     * Calculates the cube root of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#cbrt}
     *
     * @param m the mask controlling lane selection
     * @return the cube root of this vector
     */
    public DoubleVector cbrt(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.cbrt((double) a));
    }

    /**
     * Calculates the natural logarithm of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#log} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#log}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#log}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the natural logarithm of this vector
     */
    public DoubleVector log() {
        return uOp((i, a) -> (double) Math.log((double) a));
    }

    /**
     * Calculates the natural logarithm of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#log}
     *
     * @param m the mask controlling lane selection
     * @return the natural logarithm of this vector
     */
    public DoubleVector log(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.log((double) a));
    }

    /**
     * Calculates the base 10 logarithm of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#log10} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#log10}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#log10}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the base 10 logarithm of this vector
     */
    public DoubleVector log10() {
        return uOp((i, a) -> (double) Math.log10((double) a));
    }

    /**
     * Calculates the base 10 logarithm of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#log10}
     *
     * @param m the mask controlling lane selection
     * @return the base 10 logarithm of this vector
     */
    public DoubleVector log10(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.log10((double) a));
    }

    /**
     * Calculates the natural logarithm of the sum of this vector and the
     * broadcast of {@code 1}.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#log1p} operation applied to each lane.
     * The implementation is not required to return same
     * results as  {@link Math#log1p}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#log1p}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the natural logarithm of the sum of this vector and the broadcast
     * of {@code 1}
     */
    public DoubleVector log1p() {
        return uOp((i, a) -> (double) Math.log1p((double) a));
    }

    /**
     * Calculates the natural logarithm of the sum of this vector and the
     * broadcast of {@code 1}, selecting lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#log1p}
     *
     * @param m the mask controlling lane selection
     * @return the natural logarithm of the sum of this vector and the broadcast
     * of {@code 1}
     */
    public DoubleVector log1p(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.log1p((double) a));
    }

    /**
     * Calculates this vector raised to the power of an input vector.
     * <p>
     * This is a lane-wise binary operation with same semantic definition as
     * {@link Math#pow} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#pow}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#pow}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @param v the input vector
     * @return this vector raised to the power of an input vector
     */
    public DoubleVector pow(Vector<Double> v) {
        return bOp(v, (i, a, b) -> (double) Math.pow((double) a, (double) b));
    }

    /**
     * Calculates this vector raised to the power of the broadcast of an input
     * scalar.
     * <p>
     * This is a lane-wise binary operation with same semantic definition as
     * {@link Math#pow} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#pow}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#pow}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @param s the input scalar
     * @return this vector raised to the power of the broadcast of an input
     * scalar.
     */
    public abstract DoubleVector pow(double s);

    /**
     * Calculates this vector raised to the power of an input vector, selecting
     * lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#pow}
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return this vector raised to the power of an input vector
     */
    public DoubleVector pow(Vector<Double> v, VectorMask<Double> m) {
        return bOp(v, m, (i, a, b) -> (double) Math.pow((double) a, (double) b));
    }

    /**
     * Calculates this vector raised to the power of the broadcast of an input
     * scalar, selecting lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#pow}
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return this vector raised to the power of the broadcast of an input
     * scalar.
     */
    public abstract DoubleVector pow(double s, VectorMask<Double> m);

    /**
     * Calculates the broadcast of Euler's number {@code e} raised to the power
     * of this vector.
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#exp} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#exp}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#exp}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the broadcast of Euler's number {@code e} raised to the power of
     * this vector
     */
    public DoubleVector exp() {
        return uOp((i, a) -> (double) Math.exp((double) a));
    }

    /**
     * Calculates the broadcast of Euler's number {@code e} raised to the power
     * of this vector, selecting lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#exp}
     *
     * @param m the mask controlling lane selection
     * @return the broadcast of Euler's number {@code e} raised to the power of
     * this vector
     */
    public DoubleVector exp(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.exp((double) a));
    }

    /**
     * Calculates the broadcast of Euler's number {@code e} raised to the power
     * of this vector minus the broadcast of {@code -1}.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.exp().sub(EVector.broadcast(this.species(), 1))
     * }</pre>
     * <p>
     * This is a lane-wise unary operation with same semantic definition as
     * {@link Math#expm1} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#expm1}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#expm1}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @return the broadcast of Euler's number {@code e} raised to the power of
     * this vector minus the broadcast of {@code -1}
     */
    public DoubleVector expm1() {
        return uOp((i, a) -> (double) Math.expm1((double) a));
    }

    /**
     * Calculates the broadcast of Euler's number {@code e} raised to the power
     * of this vector minus the broadcast of {@code -1}, selecting lane elements
     * controlled by a mask
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.exp(m).sub(EVector.broadcast(this.species(), 1), m)
     * }</pre>
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#expm1}
     *
     * @param m the mask controlling lane selection
     * @return the broadcast of Euler's number {@code e} raised to the power of
     * this vector minus the broadcast of {@code -1}
     */
    public DoubleVector expm1(VectorMask<Double> m) {
        return uOp(m, (i, a) -> (double) Math.expm1((double) a));
    }

    /**
     * Calculates the product of this vector and a first input vector summed
     * with a second input vector.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(v1).add(v2)
     * }</pre>
     * <p>
     * This is a lane-wise ternary operation which applies the {@link Math#fma} operation
     * to each lane.
     *
     * @param v1 the first input vector
     * @param v2 the second input vector
     * @return the product of this vector and the first input vector summed with
     * the second input vector
     */
    public abstract DoubleVector fma(Vector<Double> v1, Vector<Double> v2);

    /**
     * Calculates the product of this vector and the broadcast of a first input
     * scalar summed with the broadcast of a second input scalar.
     * More specifically as if the following:
     * <pre>{@code
     *   this.fma(EVector.broadcast(this.species(), s1), EVector.broadcast(this.species(), s2))
     * }</pre>
     * <p>
     * This is a lane-wise ternary operation which applies the {@link Math#fma} operation
     * to each lane.
     *
     * @param s1 the first input scalar
     * @param s2 the second input scalar
     * @return the product of this vector and the broadcast of a first input
     * scalar summed with the broadcast of a second input scalar
     */
    public abstract DoubleVector fma(double s1, double s2);

    /**
     * Calculates the product of this vector and a first input vector summed
     * with a second input vector, selecting lane elements controlled by a mask.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(v1, m).add(v2, m)
     * }</pre>
     * <p>
     * This is a lane-wise ternary operation which applies the {@link Math#fma} operation
     * to each lane.
     *
     * @param v1 the first input vector
     * @param v2 the second input vector
     * @param m the mask controlling lane selection
     * @return the product of this vector and the first input vector summed with
     * the second input vector
     */
    public DoubleVector fma(Vector<Double> v1, Vector<Double> v2, VectorMask<Double> m) {
        return tOp(v1, v2, m, (i, a, b, c) -> Math.fma(a, b, c));
    }

    /**
     * Calculates the product of this vector and the broadcast of a first input
     * scalar summed with the broadcast of a second input scalar, selecting lane
     * elements controlled by a mask
     * More specifically as if the following:
     * <pre>{@code
     *   this.fma(EVector.broadcast(this.species(), s1), EVector.broadcast(this.species(), s2), m)
     * }</pre>
     * <p>
     * This is a lane-wise ternary operation which applies the {@link Math#fma} operation
     * to each lane.
     *
     * @param s1 the first input scalar
     * @param s2 the second input scalar
     * @param m the mask controlling lane selection
     * @return the product of this vector and the broadcast of a first input
     * scalar summed with the broadcast of a second input scalar
     */
    public abstract DoubleVector fma(double s1, double s2, VectorMask<Double> m);

    /**
     * Calculates square root of the sum of the squares of this vector and an
     * input vector.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(this).add(v.mul(v)).sqrt()
     * }</pre>
     * <p>
     * This is a lane-wise binary operation with same semantic definition as
     * {@link Math#hypot} operation applied to each lane.
     * The implementation is not required to return same
     * results as {@link Math#hypot}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#hypot}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @param v the input vector
     * @return square root of the sum of the squares of this vector and an input
     * vector
     */
    public DoubleVector hypot(Vector<Double> v) {
        return bOp(v, (i, a, b) -> (double) Math.hypot((double) a, (double) b));
    }

    /**
     * Calculates square root of the sum of the squares of this vector and the
     * broadcast of an input scalar.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(this).add(EVector.broadcast(this.species(), s * s)).sqrt()
     * }</pre>
     * <p>
     * This is a lane-wise binary operation with same semantic definition as
     * {@link Math#hypot} operation applied to each.
     * The implementation is not required to return same
     * results as {@link Math#hypot}, but adheres to rounding, monotonicity,
     * and special case semantics as defined in the {@link Math#hypot}
     * specifications. The computed result will be within 1 ulp of the
     * exact result.
     *
     * @param s the input scalar
     * @return square root of the sum of the squares of this vector and the
     * broadcast of an input scalar
     */
    public abstract DoubleVector hypot(double s);

    /**
     * Calculates square root of the sum of the squares of this vector and an
     * input vector, selecting lane elements controlled by a mask.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(this, m).add(v.mul(v), m).sqrt(m)
     * }</pre>
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#hypot}
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return square root of the sum of the squares of this vector and an input
     * vector
     */
    public DoubleVector hypot(Vector<Double> v, VectorMask<Double> m) {
        return bOp(v, m, (i, a, b) -> (double) Math.hypot((double) a, (double) b));
    }

    /**
     * Calculates square root of the sum of the squares of this vector and the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(this, m).add(EVector.broadcast(this.species(), s * s), m).sqrt(m)
     * }</pre>
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link DoubleVector#hypot}
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return square root of the sum of the squares of this vector and the
     * broadcast of an input scalar
     */
    public abstract DoubleVector hypot(double s, VectorMask<Double> m);


    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteArray(byte[] a, int ix);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteArray(byte[] a, int ix, VectorMask<Double> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix, VectorMask<Double> m);


    // Type specific horizontal reductions
    /**
     * Adds all lane elements of this vector.
     * <p>
     * This is a cross-lane reduction operation which applies the addition
     * operation ({@code +}) to lane elements,
     * and the identity value is {@code 0.0}.
     *
     * <p>The value of a floating-point sum is a function both of the input values as well
     * as the order of addition operations. The order of addition operations of this method
     * is intentionally not defined to allow for JVM to generate optimal machine
     * code for the underlying platform at runtime. If the platform supports a vector
     * instruction to add all values in the vector, or if there is some other efficient machine
     * code sequence, then the JVM has the option of generating this machine code. Otherwise,
     * the default implementation of adding vectors sequentially from left to right is used.
     * For this reason, the output of this method may vary for the same input values.
     *
     * @return the addition of all the lane elements of this vector
     */
    public abstract double addLanes();

    /**
     * Adds all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a cross-lane reduction operation which applies the addition
     * operation ({@code +}) to lane elements,
     * and the identity value is {@code 0.0}.
     *
     * <p>The value of a floating-point sum is a function both of the input values as well
     * as the order of addition operations. The order of addition operations of this method
     * is intentionally not defined to allow for JVM to generate optimal machine
     * code for the underlying platform at runtime. If the platform supports a vector
     * instruction to add all values in the vector, or if there is some other efficient machine
     * code sequence, then the JVM has the option of generating this machine code. Otherwise,
     * the default implementation of adding vectors sequentially from left to right is used.
     * For this reason, the output of this method may vary on the same input values.
     *
     * @param m the mask controlling lane selection
     * @return the addition of the selected lane elements of this vector
     */
    public abstract double addLanes(VectorMask<Double> m);

    /**
     * Multiplies all lane elements of this vector.
     * <p>
     * This is a cross-lane reduction operation which applies the
     * multiplication operation ({@code *}) to lane elements,
     * and the identity value is {@code 1.0}.
     *
     * <p>The order of multiplication operations of this method
     * is intentionally not defined to allow for JVM to generate optimal machine
     * code for the underlying platform at runtime. If the platform supports a vector
     * instruction to multiply all values in the vector, or if there is some other efficient machine
     * code sequence, then the JVM has the option of generating this machine code. Otherwise,
     * the default implementation of multiplying vectors sequentially from left to right is used.
     * For this reason, the output of this method may vary on the same input values.
     *
     * @return the multiplication of all the lane elements of this vector
     */
    public abstract double mulLanes();

    /**
     * Multiplies all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a cross-lane reduction operation which applies the
     * multiplication operation ({@code *}) to lane elements,
     * and the identity value is {@code 1.0}.
     *
     * <p>The order of multiplication operations of this method
     * is intentionally not defined to allow for JVM to generate optimal machine
     * code for the underlying platform at runtime. If the platform supports a vector
     * instruction to multiply all values in the vector, or if there is some other efficient machine
     * code sequence, then the JVM has the option of generating this machine code. Otherwise,
     * the default implementation of multiplying vectors sequentially from left to right is used.
     * For this reason, the output of this method may vary on the same input values.
     *
     * @param m the mask controlling lane selection
     * @return the multiplication of all the lane elements of this vector
     */
    public abstract double mulLanes(VectorMask<Double> m);

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to lane elements,
     * and the identity value is
     * {@link Double#POSITIVE_INFINITY}.
     *
     * @return the minimum lane element of this vector
     */
    public abstract double minLanes();

    /**
     * Returns the minimum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to lane elements,
     * and the identity value is
     * {@link Double#POSITIVE_INFINITY}.
     *
     * @param m the mask controlling lane selection
     * @return the minimum lane element of this vector
     */
    public abstract double minLanes(VectorMask<Double> m);

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to lane elements,
     * and the identity value is
     * {@link Double#NEGATIVE_INFINITY}.
     *
     * @return the maximum lane element of this vector
     */
    public abstract double maxLanes();

    /**
     * Returns the maximum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to lane elements,
     * and the identity value is
     * {@link Double#NEGATIVE_INFINITY}.
     *
     * @param m the mask controlling lane selection
     * @return the maximum lane element of this vector
     */
    public abstract double maxLanes(VectorMask<Double> m);


    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract double lane(int i);

    /**
     * Replaces the lane element of this vector at lane index {@code i} with
     * value {@code e}.
     * <p>
     * This is a cross-lane operation and behaves as if it returns the result
     * of blending this vector with an input vector that is the result of
     * broadcasting {@code e} and a mask that has only one lane set at lane
     * index {@code i}.
     *
     * @param i the lane index of the lane element to be replaced
     * @param e the value to be placed
     * @return the result of replacing the lane element of this vector at lane
     * index {@code i} with value {@code e}.
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract DoubleVector with(int i, double e);

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
    public final double[] toArray() {
        double[] a = new double[species().length()];
        intoArray(a, 0);
        return a;
    }

    /**
     * Stores this vector into an array starting at offset.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * the lane element at index {@code N} is stored into the array at index
     * {@code offset + N}.
     *
     * @param a the array
     * @param offset the offset into the array
     * @throws IndexOutOfBoundsException if {@code offset < 0}, or
     * {@code offset > a.length - this.length()}
     */
    public abstract void intoArray(double[] a, int offset);

    /**
     * Stores this vector into an array starting at offset and using a mask.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * if the mask lane at index {@code N} is set then the lane element at
     * index {@code N} is stored into the array index {@code offset + N}.
     *
     * @param a the array
     * @param offset the offset into the array
     * @param m the mask
     * @throws IndexOutOfBoundsException if {@code offset < 0}, or
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set {@code offset >= a.length - N}
     */
    public abstract void intoArray(double[] a, int offset, VectorMask<Double> m);

    /**
     * Stores this vector into an array using indexes obtained from an index
     * map.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index, the
     * lane element at index {@code N} is stored into the array at index
     * {@code a_offset + indexMap[i_offset + N]}.
     *
     * @param a the array
     * @param a_offset the offset into the array, may be negative if relative
     * indexes in the index map compensate to produce a value within the
     * array bounds
     * @param indexMap the index map
     * @param i_offset the offset into the index map
     * @throws IndexOutOfBoundsException if {@code i_offset < 0}, or
     * {@code i_offset > indexMap.length - this.length()},
     * or for any vector lane index {@code N} the result of
     * {@code a_offset + indexMap[i_offset + N]} is {@code < 0} or {@code >= a.length}
     */
    public abstract void intoArray(double[] a, int a_offset, int[] indexMap, int i_offset);

    /**
     * Stores this vector into an array using indexes obtained from an index
     * map and using a mask.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * if the mask lane at index {@code N} is set then the lane element at
     * index {@code N} is stored into the array at index
     * {@code a_offset + indexMap[i_offset + N]}.
     *
     * @param a the array
     * @param a_offset the offset into the array, may be negative if relative
     * indexes in the index map compensate to produce a value within the
     * array bounds
     * @param m the mask
     * @param indexMap the index map
     * @param i_offset the offset into the index map
     * @throws IndexOutOfBoundsException if {@code j < 0}, or
     * {@code i_offset > indexMap.length - this.length()},
     * or for any vector lane index {@code N} where the mask at lane
     * {@code N} is set the result of {@code a_offset + indexMap[i_offset + N]} is
     * {@code < 0} or {@code >= a.length}
     */
    public abstract void intoArray(double[] a, int a_offset, VectorMask<Double> m, int[] indexMap, int i_offset);
    // Species

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorSpecies<Double> species();

    /**
     * Class representing {@link DoubleVector}'s of the same {@link VectorShape VectorShape}.
     */
    static final class DoubleSpecies extends AbstractSpecies<Double> {
        final Function<double[], DoubleVector> vectorFactory;

        private DoubleSpecies(VectorShape shape,
                          Class<?> vectorType,
                          Class<?> maskType,
                          Function<double[], DoubleVector> vectorFactory,
                          Function<boolean[], VectorMask<Double>> maskFactory,
                          Function<IntUnaryOperator, VectorShuffle<Double>> shuffleFromArrayFactory,
                          fShuffleFromArray<Double> shuffleFromOpFactory) {
            super(shape, double.class, Double.SIZE, vectorType, maskType, maskFactory,
                  shuffleFromArrayFactory, shuffleFromOpFactory);
            this.vectorFactory = vectorFactory;
        }

        interface FOp {
            double apply(int i);
        }

        DoubleVector op(FOp f) {
            double[] res = new double[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return vectorFactory.apply(res);
        }

        DoubleVector op(VectorMask<Double> o, FOp f) {
            double[] res = new double[length()];
            boolean[] mbits = ((AbstractMask<Double>)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return vectorFactory.apply(res);
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
    private static DoubleSpecies preferredSpecies() {
        return (DoubleSpecies) VectorSpecies.ofPreferred(double.class);
    }

    /**
     * Finds a species for an element type of {@code double} and shape.
     *
     * @param s the shape
     * @return a species for an element type of {@code double} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    static DoubleSpecies species(VectorShape s) {
        Objects.requireNonNull(s);
        switch (s) {
            case S_64_BIT: return (DoubleSpecies) SPECIES_64;
            case S_128_BIT: return (DoubleSpecies) SPECIES_128;
            case S_256_BIT: return (DoubleSpecies) SPECIES_256;
            case S_512_BIT: return (DoubleSpecies) SPECIES_512;
            case S_Max_BIT: return (DoubleSpecies) SPECIES_MAX;
            default: throw new IllegalArgumentException("Bad shape: " + s);
        }
    }

    /** Species representing {@link DoubleVector}s of {@link VectorShape#S_64_BIT VectorShape.S_64_BIT}. */
    public static final VectorSpecies<Double> SPECIES_64 = new DoubleSpecies(VectorShape.S_64_BIT, Double64Vector.class, Double64Vector.Double64Mask.class,
                                                                     Double64Vector::new, Double64Vector.Double64Mask::new,
                                                                     Double64Vector.Double64Shuffle::new, Double64Vector.Double64Shuffle::new);

    /** Species representing {@link DoubleVector}s of {@link VectorShape#S_128_BIT VectorShape.S_128_BIT}. */
    public static final VectorSpecies<Double> SPECIES_128 = new DoubleSpecies(VectorShape.S_128_BIT, Double128Vector.class, Double128Vector.Double128Mask.class,
                                                                      Double128Vector::new, Double128Vector.Double128Mask::new,
                                                                      Double128Vector.Double128Shuffle::new, Double128Vector.Double128Shuffle::new);

    /** Species representing {@link DoubleVector}s of {@link VectorShape#S_256_BIT VectorShape.S_256_BIT}. */
    public static final VectorSpecies<Double> SPECIES_256 = new DoubleSpecies(VectorShape.S_256_BIT, Double256Vector.class, Double256Vector.Double256Mask.class,
                                                                      Double256Vector::new, Double256Vector.Double256Mask::new,
                                                                      Double256Vector.Double256Shuffle::new, Double256Vector.Double256Shuffle::new);

    /** Species representing {@link DoubleVector}s of {@link VectorShape#S_512_BIT VectorShape.S_512_BIT}. */
    public static final VectorSpecies<Double> SPECIES_512 = new DoubleSpecies(VectorShape.S_512_BIT, Double512Vector.class, Double512Vector.Double512Mask.class,
                                                                      Double512Vector::new, Double512Vector.Double512Mask::new,
                                                                      Double512Vector.Double512Shuffle::new, Double512Vector.Double512Shuffle::new);

    /** Species representing {@link DoubleVector}s of {@link VectorShape#S_Max_BIT VectorShape.S_Max_BIT}. */
    public static final VectorSpecies<Double> SPECIES_MAX = new DoubleSpecies(VectorShape.S_Max_BIT, DoubleMaxVector.class, DoubleMaxVector.DoubleMaxMask.class,
                                                                      DoubleMaxVector::new, DoubleMaxVector.DoubleMaxMask::new,
                                                                      DoubleMaxVector.DoubleMaxShuffle::new, DoubleMaxVector.DoubleMaxShuffle::new);

    /**
     * Preferred species for {@link DoubleVector}s.
     * A preferred species is a species of maximal bit size for the platform.
     */
    public static final VectorSpecies<Double> SPECIES_PREFERRED = (VectorSpecies<Double>) preferredSpecies();
}
