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
import java.nio.LongBuffer;
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
 * {@code long} values.
 */
@SuppressWarnings("cast")
public abstract class LongVector extends Vector<Long> {

    LongVector() {}

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_LONG_INDEX_SCALE);

    // Unary operator

    interface FUnOp {
        long apply(int i, long a);
    }

    abstract LongVector uOp(FUnOp f);

    abstract LongVector uOp(VectorMask<Long> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        long apply(int i, long a, long b);
    }

    abstract LongVector bOp(Vector<Long> v, FBinOp f);

    abstract LongVector bOp(Vector<Long> v, VectorMask<Long> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        long apply(int i, long a, long b, long c);
    }

    abstract LongVector tOp(Vector<Long> v1, Vector<Long> v2, FTriOp f);

    abstract LongVector tOp(Vector<Long> v1, Vector<Long> v2, VectorMask<Long> m, FTriOp f);

    // Reduction operator

    abstract long rOp(long v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, long a, long b);
    }

    abstract VectorMask<Long> bTest(Vector<Long> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, long a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(VectorMask<Long> m, FUnCon f);

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
    public static LongVector zero(VectorSpecies<Long> species) {
        return VectorIntrinsics.broadcastCoerced((Class<LongVector>) species.boxType(), long.class, species.length(),
                                                 0, species,
                                                 ((bits, s) -> ((LongSpecies)s).op(i -> (long)bits)));
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
    public static LongVector fromByteArray(VectorSpecies<Long> species, byte[] a, int offset) {
        Objects.requireNonNull(a);
        offset = VectorIntrinsics.checkIndex(offset, a.length, species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<LongVector>) species.boxType(), long.class, species.length(),
                                     a, ((long) offset) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                     a, offset, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
                                         LongBuffer tb = bbc.asLongBuffer();
                                         return ((LongSpecies)s).op(i -> tb.get());
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
    public static LongVector fromByteArray(VectorSpecies<Long> species, byte[] a, int offset, VectorMask<Long> m) {
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
    public static LongVector fromArray(VectorSpecies<Long> species, long[] a, int offset){
        Objects.requireNonNull(a);
        offset = VectorIntrinsics.checkIndex(offset, a.length, species.length());
        return VectorIntrinsics.load((Class<LongVector>) species.boxType(), long.class, species.length(),
                                     a, (((long) offset) << ARRAY_SHIFT) + Unsafe.ARRAY_LONG_BASE_OFFSET,
                                     a, offset, species,
                                     (c, idx, s) -> ((LongSpecies)s).op(n -> c[idx + n]));
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
    public static LongVector fromArray(VectorSpecies<Long> species, long[] a, int offset, VectorMask<Long> m) {
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
    public static LongVector fromArray(VectorSpecies<Long> species, long[] a, int a_offset, int[] indexMap, int i_offset) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(indexMap);

        if (species.length() == 1) {
          return LongVector.fromArray(species, a, a_offset + indexMap[i_offset]);
        }

        // Index vector: vix[0:n] = k -> a_offset + indexMap[i_offset + k]
        IntVector vix = IntVector.fromArray(IntVector.species(species.indexShape()), indexMap, i_offset).add(a_offset);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        return VectorIntrinsics.loadWithMap((Class<LongVector>) species.boxType(), long.class, species.length(),
                                            IntVector.species(species.indexShape()).boxType(), a, Unsafe.ARRAY_LONG_BASE_OFFSET, vix,
                                            a, a_offset, indexMap, i_offset, species,
                                            (long[] c, int idx, int[] iMap, int idy, VectorSpecies<Long> s) ->
                                                ((LongSpecies)s).op(n -> c[idx + iMap[idy+n]]));
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
    public static LongVector fromArray(VectorSpecies<Long> species, long[] a, int a_offset, VectorMask<Long> m, int[] indexMap, int i_offset) {
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
    public static LongVector fromByteBuffer(VectorSpecies<Long> species, ByteBuffer bb, int offset) {
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        offset = VectorIntrinsics.checkIndex(offset, bb.limit(), species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<LongVector>) species.boxType(), long.class, species.length(),
                                     U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + offset,
                                     bb, offset, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                         LongBuffer tb = bbc.asLongBuffer();
                                         return ((LongSpecies)s).op(i -> tb.get());
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
    public static LongVector fromByteBuffer(VectorSpecies<Long> species, ByteBuffer bb, int offset, VectorMask<Long> m) {
        return zero(species).blend(fromByteBuffer(species, bb, offset), m);
    }

    /**
     * Returns a vector where all lane elements are set to the primitive
     * value {@code e}.
     *
     * @param species species of the desired vector
     * @param e the value
     * @return a vector of vector where all lane elements are set to
     * the primitive value {@code e}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static LongVector broadcast(VectorSpecies<Long> species, long e) {
        return VectorIntrinsics.broadcastCoerced(
            (Class<LongVector>) species.boxType(), long.class, species.length(),
            e, species,
            ((bits, sp) -> ((LongSpecies)sp).op(i -> (long)bits)));
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
    public static LongVector scalars(VectorSpecies<Long> species, long... es) {
        Objects.requireNonNull(es);
        int ix = VectorIntrinsics.checkIndex(0, es.length, species.length());
        return VectorIntrinsics.load((Class<LongVector>) species.boxType(), long.class, species.length(),
                                     es, Unsafe.ARRAY_LONG_BASE_OFFSET,
                                     es, ix, species,
                                     (c, idx, sp) -> ((LongSpecies)sp).op(n -> c[idx + n]));
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
    public static final LongVector single(VectorSpecies<Long> species, long e) {
        return zero(species).with(0, e);
    }

    /**
     * Returns a vector where each lane element is set to a randomly
     * generated primitive value.
     *
     * The semantics are equivalent to calling
     * {@link ThreadLocalRandom#nextLong()}
     *
     * @param species species of the desired vector
     * @return a vector where each lane elements is set to a randomly
     * generated primitive value
     */
    public static LongVector random(VectorSpecies<Long> species) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return ((LongSpecies)species).op(i -> r.nextLong());
    }

    // Ops

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector add(Vector<Long> v);

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
    public abstract LongVector add(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector add(Vector<Long> v, VectorMask<Long> m);

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
    public abstract LongVector add(long s, VectorMask<Long> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector sub(Vector<Long> v);

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
    public abstract LongVector sub(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector sub(Vector<Long> v, VectorMask<Long> m);

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
    public abstract LongVector sub(long s, VectorMask<Long> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector mul(Vector<Long> v);

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
    public abstract LongVector mul(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector mul(Vector<Long> v, VectorMask<Long> m);

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
    public abstract LongVector mul(long s, VectorMask<Long> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector neg();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector neg(VectorMask<Long> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector abs();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector abs(VectorMask<Long> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector min(Vector<Long> v);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector min(Vector<Long> v, VectorMask<Long> m);

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to each lane.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract LongVector min(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector max(Vector<Long> v);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector max(Vector<Long> v, VectorMask<Long> m);

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to each lane.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract LongVector max(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Long> equal(Vector<Long> v);

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
    public abstract VectorMask<Long> equal(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Long> notEqual(Vector<Long> v);

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
    public abstract VectorMask<Long> notEqual(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Long> lessThan(Vector<Long> v);

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
    public abstract VectorMask<Long> lessThan(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Long> lessThanEq(Vector<Long> v);

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
    public abstract VectorMask<Long> lessThanEq(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Long> greaterThan(Vector<Long> v);

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
    public abstract VectorMask<Long> greaterThan(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Long> greaterThanEq(Vector<Long> v);

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
    public abstract VectorMask<Long> greaterThanEq(long s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector blend(Vector<Long> v, VectorMask<Long> m);

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
    public abstract LongVector blend(long s, VectorMask<Long> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector rearrange(Vector<Long> v,
                                                      VectorShuffle<Long> s, VectorMask<Long> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector rearrange(VectorShuffle<Long> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector reshape(VectorSpecies<Long> s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector rotateEL(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector rotateER(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector shiftEL(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract LongVector shiftER(int i);



    /**
     * Bitwise ANDs this vector with an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise AND
     * operation ({@code &}) to each lane.
     *
     * @param v the input vector
     * @return the bitwise AND of this vector with the input vector
     */
    public abstract LongVector and(Vector<Long> v);

    /**
     * Bitwise ANDs this vector with the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise AND
     * operation ({@code &}) to each lane.
     *
     * @param s the input scalar
     * @return the bitwise AND of this vector with the broadcast of an input
     * scalar
     */
    public abstract LongVector and(long s);

    /**
     * Bitwise ANDs this vector with an input vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise AND
     * operation ({@code &}) to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the bitwise AND of this vector with the input vector
     */
    public abstract LongVector and(Vector<Long> v, VectorMask<Long> m);

    /**
     * Bitwise ANDs this vector with the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise AND
     * operation ({@code &}) to each lane.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the bitwise AND of this vector with the broadcast of an input
     * scalar
     */
    public abstract LongVector and(long s, VectorMask<Long> m);

    /**
     * Bitwise ORs this vector with an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise OR
     * operation ({@code |}) to each lane.
     *
     * @param v the input vector
     * @return the bitwise OR of this vector with the input vector
     */
    public abstract LongVector or(Vector<Long> v);

    /**
     * Bitwise ORs this vector with the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise OR
     * operation ({@code |}) to each lane.
     *
     * @param s the input scalar
     * @return the bitwise OR of this vector with the broadcast of an input
     * scalar
     */
    public abstract LongVector or(long s);

    /**
     * Bitwise ORs this vector with an input vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise OR
     * operation ({@code |}) to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the bitwise OR of this vector with the input vector
     */
    public abstract LongVector or(Vector<Long> v, VectorMask<Long> m);

    /**
     * Bitwise ORs this vector with the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise OR
     * operation ({@code |}) to each lane.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the bitwise OR of this vector with the broadcast of an input
     * scalar
     */
    public abstract LongVector or(long s, VectorMask<Long> m);

    /**
     * Bitwise XORs this vector with an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise XOR
     * operation ({@code ^}) to each lane.
     *
     * @param v the input vector
     * @return the bitwise XOR of this vector with the input vector
     */
    public abstract LongVector xor(Vector<Long> v);

    /**
     * Bitwise XORs this vector with the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise XOR
     * operation ({@code ^}) to each lane.
     *
     * @param s the input scalar
     * @return the bitwise XOR of this vector with the broadcast of an input
     * scalar
     */
    public abstract LongVector xor(long s);

    /**
     * Bitwise XORs this vector with an input vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise XOR
     * operation ({@code ^}) to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the bitwise XOR of this vector with the input vector
     */
    public abstract LongVector xor(Vector<Long> v, VectorMask<Long> m);

    /**
     * Bitwise XORs this vector with the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise XOR
     * operation ({@code ^}) to each lane.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the bitwise XOR of this vector with the broadcast of an input
     * scalar
     */
    public abstract LongVector xor(long s, VectorMask<Long> m);

    /**
     * Bitwise NOTs this vector.
     * <p>
     * This is a lane-wise unary operation which applies the primitive bitwise NOT
     * operation ({@code ~}) to each lane.
     *
     * @return the bitwise NOT of this vector
     */
    public abstract LongVector not();

    /**
     * Bitwise NOTs this vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise unary operation which applies the primitive bitwise NOT
     * operation ({@code ~}) to each lane.
     *
     * @param m the mask controlling lane selection
     * @return the bitwise NOT of this vector
     */
    public abstract LongVector not(VectorMask<Long> m);

    /**
     * Logically left shifts this vector by the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane.
     *
     * @param s the input scalar; the number of the bits to left shift
     * @return the result of logically left shifting left this vector by the
     * broadcast of an input scalar
     */
    public abstract LongVector shiftL(int s);

    /**
     * Logically left shifts this vector by the broadcast of an input scalar,
     * selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane.
     *
     * @param s the input scalar; the number of the bits to left shift
     * @param m the mask controlling lane selection
     * @return the result of logically left shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract LongVector shiftL(int s, VectorMask<Long> m);

    /**
     * Logically left shifts this vector by an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane.
     *
     * @param v the input vector
     * @return the result of logically left shifting this vector by the input
     * vector
     */
    public abstract LongVector shiftL(Vector<Long> v);

    /**
     * Logically left shifts this vector by an input vector, selecting lane
     * elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of logically left shifting this vector by the input
     * vector
     */
    public LongVector shiftL(Vector<Long> v, VectorMask<Long> m) {
        return bOp(v, m, (i, a, b) -> (long) (a << b));
    }

    // logical, or unsigned, shift right

    /**
     * Logically right shifts (or unsigned right shifts) this vector by the
     * broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>>}) to each lane.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @return the result of logically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract LongVector shiftR(int s);

    /**
     * Logically right shifts (or unsigned right shifts) this vector by the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>>}) to each lane.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @param m the mask controlling lane selection
     * @return the result of logically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract LongVector shiftR(int s, VectorMask<Long> m);

    /**
     * Logically right shifts (or unsigned right shifts) this vector by an
     * input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>>}) to each lane.
     *
     * @param v the input vector
     * @return the result of logically right shifting this vector by the
     * input vector
     */
    public abstract LongVector shiftR(Vector<Long> v);

    /**
     * Logically right shifts (or unsigned right shifts) this vector by an
     * input vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>>}) to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of logically right shifting this vector by the
     * input vector
     */
    public LongVector shiftR(Vector<Long> v, VectorMask<Long> m) {
        return bOp(v, m, (i, a, b) -> (long) (a >>> b));
    }

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by the
     * broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @return the result of arithmetically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract LongVector aShiftR(int s);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @param m the mask controlling lane selection
     * @return the result of arithmetically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract LongVector aShiftR(int s, VectorMask<Long> m);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by an
     * input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane.
     *
     * @param v the input vector
     * @return the result of arithmetically right shifting this vector by the
     * input vector
     */
    public abstract LongVector aShiftR(Vector<Long> v);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by an
     * input vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of arithmetically right shifting this vector by the
     * input vector
     */
    public LongVector aShiftR(Vector<Long> v, VectorMask<Long> m) {
        return bOp(v, m, (i, a, b) -> (long) (a >> b));
    }

    /**
     * Rotates left this vector by the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@link Long#rotateLeft} to each lane and where
     * lane elements of this vector apply to the first argument, and lane
     * elements of the broadcast vector apply to the second argument (the
     * rotation distance).
     *
     * @param s the input scalar; the number of the bits to rotate left
     * @return the result of rotating left this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final LongVector rotateL(int s) {
        return shiftL(s).or(shiftR(-s));
    }

    /**
     * Rotates left this vector by the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@link Long#rotateLeft} to each lane and where
     * lane elements of this vector apply to the first argument, and lane
     * elements of the broadcast vector apply to the second argument (the
     * rotation distance).
     *
     * @param s the input scalar; the number of the bits to rotate left
     * @param m the mask controlling lane selection
     * @return the result of rotating left this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final LongVector rotateL(int s, VectorMask<Long> m) {
        return shiftL(s, m).or(shiftR(-s, m), m);
    }

    /**
     * Rotates right this vector by the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@link Long#rotateRight} to each lane and where
     * lane elements of this vector apply to the first argument, and lane
     * elements of the broadcast vector apply to the second argument (the
     * rotation distance).
     *
     * @param s the input scalar; the number of the bits to rotate right
     * @return the result of rotating right this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final LongVector rotateR(int s) {
        return shiftR(s).or(shiftL(-s));
    }

    /**
     * Rotates right this vector by the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@link Long#rotateRight} to each lane and where
     * lane elements of this vector apply to the first argument, and lane
     * elements of the broadcast vector apply to the second argument (the
     * rotation distance).
     *
     * @param s the input scalar; the number of the bits to rotate right
     * @param m the mask controlling lane selection
     * @return the result of rotating right this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final LongVector rotateR(int s, VectorMask<Long> m) {
        return shiftR(s, m).or(shiftL(-s, m), m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteArray(byte[] a, int ix);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteArray(byte[] a, int ix, VectorMask<Long> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix, VectorMask<Long> m);


    // Type specific horizontal reductions
    /**
     * Adds all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the addition
     * operation ({@code +}) to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the addition of all the lane elements of this vector
     */
    public abstract long addAll();

    /**
     * Adds all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the addition
     * operation ({@code +}) to lane elements,
     * and the identity value is {@code 0}.
     *
     * @param m the mask controlling lane selection
     * @return the addition of the selected lane elements of this vector
     */
    public abstract long addAll(VectorMask<Long> m);

    /**
     * Multiplies all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the
     * multiplication operation ({@code *}) to lane elements,
     * and the identity value is {@code 1}.
     *
     * @return the multiplication of all the lane elements of this vector
     */
    public abstract long mulAll();

    /**
     * Multiplies all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the
     * multiplication operation ({@code *}) to lane elements,
     * and the identity value is {@code 1}.
     *
     * @param m the mask controlling lane selection
     * @return the multiplication of all the lane elements of this vector
     */
    public abstract long mulAll(VectorMask<Long> m);

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to lane elements,
     * and the identity value is
     * {@link Long#MAX_VALUE}.
     *
     * @return the minimum lane element of this vector
     */
    public abstract long minAll();

    /**
     * Returns the minimum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to lane elements,
     * and the identity value is
     * {@link Long#MAX_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the minimum lane element of this vector
     */
    public abstract long minAll(VectorMask<Long> m);

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to lane elements,
     * and the identity value is
     * {@link Long#MIN_VALUE}.
     *
     * @return the maximum lane element of this vector
     */
    public abstract long maxAll();

    /**
     * Returns the maximum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to lane elements,
     * and the identity value is
     * {@link Long#MIN_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the maximum lane element of this vector
     */
    public abstract long maxAll(VectorMask<Long> m);

    /**
     * Logically ORs all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical OR
     * operation ({@code |}) to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical OR all the lane elements of this vector
     */
    public abstract long orAll();

    /**
     * Logically ORs all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical OR
     * operation ({@code |}) to lane elements,
     * and the identity value is {@code 0}.
     *
     * @param m the mask controlling lane selection
     * @return the logical OR all the lane elements of this vector
     */
    public abstract long orAll(VectorMask<Long> m);

    /**
     * Logically ANDs all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical AND
     * operation ({@code |}) to lane elements,
     * and the identity value is {@code -1}.
     *
     * @return the logical AND all the lane elements of this vector
     */
    public abstract long andAll();

    /**
     * Logically ANDs all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical AND
     * operation ({@code |}) to lane elements,
     * and the identity value is {@code -1}.
     *
     * @param m the mask controlling lane selection
     * @return the logical AND all the lane elements of this vector
     */
    public abstract long andAll(VectorMask<Long> m);

    /**
     * Logically XORs all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical XOR
     * operation ({@code ^}) to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical XOR all the lane elements of this vector
     */
    public abstract long xorAll();

    /**
     * Logically XORs all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical XOR
     * operation ({@code ^}) to lane elements,
     * and the identity value is {@code 0}.
     *
     * @param m the mask controlling lane selection
     * @return the logical XOR all the lane elements of this vector
     */
    public abstract long xorAll(VectorMask<Long> m);

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract long lane(int i);

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
    public abstract LongVector with(int i, long e);

    // Type specific extractors

    /**
     * Returns an array containing the lane elements of this vector.
     * <p>
     * This method behaves as if it {@link #intoArray(long[], int)} stores}
     * this vector into an allocated array and returns the array as follows:
     * <pre>{@code
     *   long[] a = new long[this.length()];
     *   this.intoArray(a, 0);
     *   return a;
     * }</pre>
     *
     * @return an array containing the the lane elements of this vector
     */
    @ForceInline
    public final long[] toArray() {
        long[] a = new long[species().length()];
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
    public abstract void intoArray(long[] a, int offset);

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
    public abstract void intoArray(long[] a, int offset, VectorMask<Long> m);

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
    public abstract void intoArray(long[] a, int a_offset, int[] indexMap, int i_offset);

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
    public abstract void intoArray(long[] a, int a_offset, VectorMask<Long> m, int[] indexMap, int i_offset);
    // Species

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorSpecies<Long> species();

    /**
     * Class representing {@link LongVector}'s of the same {@link VectorShape VectorShape}.
     */
    static final class LongSpecies extends AbstractSpecies<Long> {
        final Function<long[], LongVector> vectorFactory;

        private LongSpecies(VectorShape shape,
                          Class<?> boxType,
                          Class<?> maskType,
                          Function<long[], LongVector> vectorFactory,
                          Function<boolean[], VectorMask<Long>> maskFactory,
                          Function<IntUnaryOperator, VectorShuffle<Long>> shuffleFromArrayFactory,
                          fShuffleFromArray<Long> shuffleFromOpFactory) {
            super(shape, long.class, Long.SIZE, boxType, maskType, maskFactory,
                  shuffleFromArrayFactory, shuffleFromOpFactory);
            this.vectorFactory = vectorFactory;
        }

        interface FOp {
            long apply(int i);
        }

        LongVector op(FOp f) {
            long[] res = new long[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return vectorFactory.apply(res);
        }

        LongVector op(VectorMask<Long> o, FOp f) {
            long[] res = new long[length()];
            boolean[] mbits = ((AbstractMask<Long>)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return vectorFactory.apply(res);
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
    private static LongSpecies preferredSpecies() {
        return (LongSpecies) VectorSpecies.ofPreferred(long.class);
    }

    /**
     * Finds a species for an element type of {@code long} and shape.
     *
     * @param s the shape
     * @return a species for an element type of {@code long} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    static LongSpecies species(VectorShape s) {
        Objects.requireNonNull(s);
        switch (s) {
            case S_64_BIT: return (LongSpecies) SPECIES_64;
            case S_128_BIT: return (LongSpecies) SPECIES_128;
            case S_256_BIT: return (LongSpecies) SPECIES_256;
            case S_512_BIT: return (LongSpecies) SPECIES_512;
            case S_Max_BIT: return (LongSpecies) SPECIES_MAX;
            default: throw new IllegalArgumentException("Bad shape: " + s);
        }
    }

    /** Species representing {@link LongVector}s of {@link VectorShape#S_64_BIT VectorShape.S_64_BIT}. */
    public static final VectorSpecies<Long> SPECIES_64 = new LongSpecies(VectorShape.S_64_BIT, Long64Vector.class, Long64Vector.Long64Mask.class,
                                                                     Long64Vector::new, Long64Vector.Long64Mask::new,
                                                                     Long64Vector.Long64Shuffle::new, Long64Vector.Long64Shuffle::new);

    /** Species representing {@link LongVector}s of {@link VectorShape#S_128_BIT VectorShape.S_128_BIT}. */
    public static final VectorSpecies<Long> SPECIES_128 = new LongSpecies(VectorShape.S_128_BIT, Long128Vector.class, Long128Vector.Long128Mask.class,
                                                                      Long128Vector::new, Long128Vector.Long128Mask::new,
                                                                      Long128Vector.Long128Shuffle::new, Long128Vector.Long128Shuffle::new);

    /** Species representing {@link LongVector}s of {@link VectorShape#S_256_BIT VectorShape.S_256_BIT}. */
    public static final VectorSpecies<Long> SPECIES_256 = new LongSpecies(VectorShape.S_256_BIT, Long256Vector.class, Long256Vector.Long256Mask.class,
                                                                      Long256Vector::new, Long256Vector.Long256Mask::new,
                                                                      Long256Vector.Long256Shuffle::new, Long256Vector.Long256Shuffle::new);

    /** Species representing {@link LongVector}s of {@link VectorShape#S_512_BIT VectorShape.S_512_BIT}. */
    public static final VectorSpecies<Long> SPECIES_512 = new LongSpecies(VectorShape.S_512_BIT, Long512Vector.class, Long512Vector.Long512Mask.class,
                                                                      Long512Vector::new, Long512Vector.Long512Mask::new,
                                                                      Long512Vector.Long512Shuffle::new, Long512Vector.Long512Shuffle::new);

    /** Species representing {@link LongVector}s of {@link VectorShape#S_Max_BIT VectorShape.S_Max_BIT}. */
    public static final VectorSpecies<Long> SPECIES_MAX = new LongSpecies(VectorShape.S_Max_BIT, LongMaxVector.class, LongMaxVector.LongMaxMask.class,
                                                                      LongMaxVector::new, LongMaxVector.LongMaxMask::new,
                                                                      LongMaxVector.LongMaxShuffle::new, LongMaxVector.LongMaxShuffle::new);

    /**
     * Preferred species for {@link LongVector}s.
     * A preferred species is a species of maximal bit size for the platform.
     */
    public static final VectorSpecies<Long> SPECIES_PREFERRED = (VectorSpecies<Long>) preferredSpecies();
}
