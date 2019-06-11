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
import java.nio.IntBuffer;
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
 * {@code int} values.
 */
@SuppressWarnings("cast")
public abstract class IntVector extends Vector<Integer> {

    IntVector() {}

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_INT_INDEX_SCALE);

    // Unary operator

    interface FUnOp {
        int apply(int i, int a);
    }

    abstract IntVector uOp(FUnOp f);

    abstract IntVector uOp(VectorMask<Integer> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        int apply(int i, int a, int b);
    }

    abstract IntVector bOp(Vector<Integer> v, FBinOp f);

    abstract IntVector bOp(Vector<Integer> v, VectorMask<Integer> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        int apply(int i, int a, int b, int c);
    }

    abstract IntVector tOp(Vector<Integer> v1, Vector<Integer> v2, FTriOp f);

    abstract IntVector tOp(Vector<Integer> v1, Vector<Integer> v2, VectorMask<Integer> m, FTriOp f);

    // Reduction operator

    abstract int rOp(int v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, int a, int b);
    }

    abstract VectorMask<Integer> bTest(Vector<Integer> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, int a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(VectorMask<Integer> m, FUnCon f);

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
    public static IntVector zero(VectorSpecies<Integer> species) {
        return VectorIntrinsics.broadcastCoerced((Class<IntVector>) species.vectorType(), int.class, species.length(),
                                                 0, species,
                                                 ((bits, s) -> ((IntSpecies)s).op(i -> (int)bits)));
    }

    @ForceInline
    @SuppressWarnings("unchecked")
    static VectorShuffle<Integer> shuffleIotaHelper(VectorSpecies<Integer> species, int step) {
        switch (species.bitSize()) {
            case 64: return VectorIntrinsics.shuffleIota(int.class, Int64Vector.Int64Shuffle.class, species,
                                                        64 / Integer.SIZE, step,
                                                        (val, l) -> new Int64Vector.Int64Shuffle(i -> ((i + val) & (l-1))));
            case 128: return VectorIntrinsics.shuffleIota(int.class, Int128Vector.Int128Shuffle.class, species,
                                                        128/ Integer.SIZE, step,
                                                        (val, l) -> new Int128Vector.Int128Shuffle(i -> ((i + val) & (l-1))));
            case 256: return VectorIntrinsics.shuffleIota(int.class, Int256Vector.Int256Shuffle.class, species,
                                                        256/ Integer.SIZE, step,
                                                        (val, l) -> new Int256Vector.Int256Shuffle(i -> ((i + val) & (l-1))));
            case 512: return VectorIntrinsics.shuffleIota(int.class, Int512Vector.Int512Shuffle.class, species,
                                                        512 / Integer.SIZE, step,
                                                        (val, l) -> new Int512Vector.Int512Shuffle(i -> ((i + val) & (l-1))));
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
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
    public static IntVector fromByteArray(VectorSpecies<Integer> species, byte[] a, int offset) {
        Objects.requireNonNull(a);
        offset = VectorIntrinsics.checkIndex(offset, a.length, species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<IntVector>) species.vectorType(), int.class, species.length(),
                                     a, ((long) offset) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                     a, offset, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                         IntBuffer tb = bbc.asIntBuffer();
                                         return ((IntSpecies)s).op(i -> tb.get());
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
    public static IntVector fromByteArray(VectorSpecies<Integer> species, byte[] a, int offset, VectorMask<Integer> m) {
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
    public static IntVector fromArray(VectorSpecies<Integer> species, int[] a, int offset){
        Objects.requireNonNull(a);
        offset = VectorIntrinsics.checkIndex(offset, a.length, species.length());
        return VectorIntrinsics.load((Class<IntVector>) species.vectorType(), int.class, species.length(),
                                     a, (((long) offset) << ARRAY_SHIFT) + Unsafe.ARRAY_INT_BASE_OFFSET,
                                     a, offset, species,
                                     (c, idx, s) -> ((IntSpecies)s).op(n -> c[idx + n]));
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
    public static IntVector fromArray(VectorSpecies<Integer> species, int[] a, int offset, VectorMask<Integer> m) {
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
    public static IntVector fromArray(VectorSpecies<Integer> species, int[] a, int a_offset, int[] indexMap, int i_offset) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(indexMap);


        // Index vector: vix[0:n] = k -> a_offset + indexMap[i_offset + k]
        IntVector vix = IntVector.fromArray(IntVector.species(species.indexShape()), indexMap, i_offset).add(a_offset);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        return VectorIntrinsics.loadWithMap((Class<IntVector>) species.vectorType(), int.class, species.length(),
                                            IntVector.species(species.indexShape()).vectorType(), a, Unsafe.ARRAY_INT_BASE_OFFSET, vix,
                                            a, a_offset, indexMap, i_offset, species,
                                            (int[] c, int idx, int[] iMap, int idy, VectorSpecies<Integer> s) ->
                                                ((IntSpecies)s).op(n -> c[idx + iMap[idy+n]]));
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
    public static IntVector fromArray(VectorSpecies<Integer> species, int[] a, int a_offset, VectorMask<Integer> m, int[] indexMap, int i_offset) {
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
    public static IntVector fromByteBuffer(VectorSpecies<Integer> species, ByteBuffer bb, int offset) {
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        offset = VectorIntrinsics.checkIndex(offset, bb.limit(), species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<IntVector>) species.vectorType(), int.class, species.length(),
                                     U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + offset,
                                     bb, offset, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                         IntBuffer tb = bbc.asIntBuffer();
                                         return ((IntSpecies)s).op(i -> tb.get());
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
    public static IntVector fromByteBuffer(VectorSpecies<Integer> species, ByteBuffer bb, int offset, VectorMask<Integer> m) {
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
    public static IntVector broadcast(VectorSpecies<Integer> species, int e) {
        return VectorIntrinsics.broadcastCoerced(
            (Class<IntVector>) species.vectorType(), int.class, species.length(),
            e, species,
            ((bits, sp) -> ((IntSpecies)sp).op(i -> (int)bits)));
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
    public static IntVector scalars(VectorSpecies<Integer> species, int... es) {
        Objects.requireNonNull(es);
        int ix = VectorIntrinsics.checkIndex(0, es.length, species.length());
        return VectorIntrinsics.load((Class<IntVector>) species.vectorType(), int.class, species.length(),
                                     es, Unsafe.ARRAY_INT_BASE_OFFSET,
                                     es, ix, species,
                                     (c, idx, sp) -> ((IntSpecies)sp).op(n -> c[idx + n]));
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
    public static final IntVector single(VectorSpecies<Integer> species, int e) {
        return zero(species).with(0, e);
    }

    /**
     * Returns a vector where each lane element is set to a randomly
     * generated primitive value.
     *
     * The semantics are equivalent to calling
     * {@link ThreadLocalRandom#nextInt()}
     *
     * @param species species of the desired vector
     * @return a vector where each lane elements is set to a randomly
     * generated primitive value
     */
    public static IntVector random(VectorSpecies<Integer> species) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return ((IntSpecies)species).op(i -> r.nextInt());
    }

    // Ops

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector add(Vector<Integer> v);

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
    public abstract IntVector add(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector add(Vector<Integer> v, VectorMask<Integer> m);

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
    public abstract IntVector add(int s, VectorMask<Integer> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector sub(Vector<Integer> v);

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
    public abstract IntVector sub(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector sub(Vector<Integer> v, VectorMask<Integer> m);

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
    public abstract IntVector sub(int s, VectorMask<Integer> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector mul(Vector<Integer> v);

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
    public abstract IntVector mul(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector mul(Vector<Integer> v, VectorMask<Integer> m);

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
    public abstract IntVector mul(int s, VectorMask<Integer> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector neg();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector neg(VectorMask<Integer> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector abs();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector abs(VectorMask<Integer> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector min(Vector<Integer> v);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector min(Vector<Integer> v, VectorMask<Integer> m);

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to each lane.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract IntVector min(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector max(Vector<Integer> v);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector max(Vector<Integer> v, VectorMask<Integer> m);

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to each lane.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract IntVector max(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Integer> equal(Vector<Integer> v);

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
    public abstract VectorMask<Integer> equal(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Integer> notEqual(Vector<Integer> v);

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
    public abstract VectorMask<Integer> notEqual(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Integer> lessThan(Vector<Integer> v);

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
    public abstract VectorMask<Integer> lessThan(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Integer> lessThanEq(Vector<Integer> v);

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
    public abstract VectorMask<Integer> lessThanEq(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Integer> greaterThan(Vector<Integer> v);

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
    public abstract VectorMask<Integer> greaterThan(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Integer> greaterThanEq(Vector<Integer> v);

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
    public abstract VectorMask<Integer> greaterThanEq(int s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector blend(Vector<Integer> v, VectorMask<Integer> m);

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
    public abstract IntVector blend(int s, VectorMask<Integer> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector rearrange(Vector<Integer> v,
                                                      VectorShuffle<Integer> s, VectorMask<Integer> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector rearrange(VectorShuffle<Integer> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector reshape(VectorSpecies<Integer> s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector rotateLanesLeft(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector rotateLanesRight(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector shiftLanesLeft(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract IntVector shiftLanesRight(int i);



    /**
     * Bitwise ANDs this vector with an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise AND
     * operation ({@code &}) to each lane.
     *
     * @param v the input vector
     * @return the bitwise AND of this vector with the input vector
     */
    public abstract IntVector and(Vector<Integer> v);

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
    public abstract IntVector and(int s);

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
    public abstract IntVector and(Vector<Integer> v, VectorMask<Integer> m);

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
    public abstract IntVector and(int s, VectorMask<Integer> m);

    /**
     * Bitwise ORs this vector with an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise OR
     * operation ({@code |}) to each lane.
     *
     * @param v the input vector
     * @return the bitwise OR of this vector with the input vector
     */
    public abstract IntVector or(Vector<Integer> v);

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
    public abstract IntVector or(int s);

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
    public abstract IntVector or(Vector<Integer> v, VectorMask<Integer> m);

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
    public abstract IntVector or(int s, VectorMask<Integer> m);

    /**
     * Bitwise XORs this vector with an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise XOR
     * operation ({@code ^}) to each lane.
     *
     * @param v the input vector
     * @return the bitwise XOR of this vector with the input vector
     */
    public abstract IntVector xor(Vector<Integer> v);

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
    public abstract IntVector xor(int s);

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
    public abstract IntVector xor(Vector<Integer> v, VectorMask<Integer> m);

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
    public abstract IntVector xor(int s, VectorMask<Integer> m);

    /**
     * Bitwise NOTs this vector.
     * <p>
     * This is a lane-wise unary operation which applies the primitive bitwise NOT
     * operation ({@code ~}) to each lane.
     *
     * @return the bitwise NOT of this vector
     */
    public abstract IntVector not();

    /**
     * Bitwise NOTs this vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise unary operation which applies the primitive bitwise NOT
     * operation ({@code ~}) to each lane.
     *
     * @param m the mask controlling lane selection
     * @return the bitwise NOT of this vector
     */
    public abstract IntVector not(VectorMask<Integer> m);

    /**
     * Logically left shifts this vector by the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane to left shift the
     * element by shift value as specified by the input scalar.
     *
     * @param s the input scalar; the number of the bits to left shift
     * @return the result of logically left shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract IntVector shiftLeft(int s);

    /**
     * Logically left shifts this vector by the broadcast of an input scalar,
     * selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane to left shift the
     * element by shift value as specified by the input scalar.
     *
     * @param s the input scalar; the number of the bits to left shift
     * @param m the mask controlling lane selection
     * @return the result of logically left shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract IntVector shiftLeft(int s, VectorMask<Integer> m);

    /**
     * Logically left shifts this vector by an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     *
     * @param v the input vector
     * @return the result of logically left shifting this vector by the input
     * vector
     */
    public abstract IntVector shiftLeft(Vector<Integer> v);

    /**
     * Logically left shifts this vector by an input vector, selecting lane
     * elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of logically left shifting this vector by the input
     * vector
     */
    public IntVector shiftLeft(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(shiftLeft(v), m);
    }

    // logical, or unsigned, shift right

     /**
     * Logically right shifts (or unsigned right shifts) this vector by the
     * broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>>}) to each lane to logically right shift the
     * element by shift value as specified by the input scalar.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @return the result of logically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract IntVector shiftRight(int s);

     /**
     * Logically right shifts (or unsigned right shifts) this vector by the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>}) to each lane to logically right shift the
     * element by shift value as specified by the input scalar.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @param m the mask controlling lane selection
     * @return the result of logically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract IntVector shiftRight(int s, VectorMask<Integer> m);

    /**
     * Logically right shifts (or unsigned right shifts) this vector by an
     * input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>>}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     *
     * @param v the input vector
     * @return the result of logically right shifting this vector by the
     * input vector
     */
    public abstract IntVector shiftRight(Vector<Integer> v);

    /**
     * Logically right shifts (or unsigned right shifts) this vector by an
     * input vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>>}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of logically right shifting this vector by the
     * input vector
     */
    public IntVector shiftRight(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(shiftRight(v), m);
    }

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by the
     * broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane to arithmetically
     * right shift the element by shift value as specified by the input scalar.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @return the result of arithmetically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract IntVector shiftArithmeticRight(int s);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane to arithmetically
     * right shift the element by shift value as specified by the input scalar.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @param m the mask controlling lane selection
     * @return the result of arithmetically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract IntVector shiftArithmeticRight(int s, VectorMask<Integer> m);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by an
     * input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     *
     * @param v the input vector
     * @return the result of arithmetically right shifting this vector by the
     * input vector
     */
    public abstract IntVector shiftArithmeticRight(Vector<Integer> v);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by an
     * input vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of arithmetically right shifting this vector by the
     * input vector
     */
    public IntVector shiftArithmeticRight(Vector<Integer> v, VectorMask<Integer> m) {
        return blend(shiftArithmeticRight(v), m);
    }

    /**
     * Rotates left this vector by the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@link Integer#rotateLeft} to each lane and where
     * lane elements of this vector apply to the first argument, and lane
     * elements of the broadcast vector apply to the second argument (the
     * rotation distance).
     *
     * @param s the input scalar; the number of the bits to rotate left
     * @return the result of rotating left this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final IntVector rotateLeft(int s) {
        return shiftLeft(s).or(shiftRight(-s));
    }

    /**
     * Rotates left this vector by the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@link Integer#rotateLeft} to each lane and where
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
    public final IntVector rotateLeft(int s, VectorMask<Integer> m) {
        return shiftLeft(s, m).or(shiftRight(-s, m), m);
    }

    /**
     * Rotates right this vector by the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@link Integer#rotateRight} to each lane and where
     * lane elements of this vector apply to the first argument, and lane
     * elements of the broadcast vector apply to the second argument (the
     * rotation distance).
     *
     * @param s the input scalar; the number of the bits to rotate right
     * @return the result of rotating right this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final IntVector rotateRight(int s) {
        return shiftRight(s).or(shiftLeft(-s));
    }

    /**
     * Rotates right this vector by the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@link Integer#rotateRight} to each lane and where
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
    public final IntVector rotateRight(int s, VectorMask<Integer> m) {
        return shiftRight(s, m).or(shiftLeft(-s, m), m);
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
    public abstract void intoByteArray(byte[] a, int ix, VectorMask<Integer> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix, VectorMask<Integer> m);


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
    public abstract int addLanes();

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
    public abstract int addLanes(VectorMask<Integer> m);

    /**
     * Multiplies all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the
     * multiplication operation ({@code *}) to lane elements,
     * and the identity value is {@code 1}.
     *
     * @return the multiplication of all the lane elements of this vector
     */
    public abstract int mulLanes();

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
    public abstract int mulLanes(VectorMask<Integer> m);

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to lane elements,
     * and the identity value is
     * {@link Integer#MAX_VALUE}.
     *
     * @return the minimum lane element of this vector
     */
    public abstract int minLanes();

    /**
     * Returns the minimum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to lane elements,
     * and the identity value is
     * {@link Integer#MAX_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the minimum lane element of this vector
     */
    public abstract int minLanes(VectorMask<Integer> m);

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to lane elements,
     * and the identity value is
     * {@link Integer#MIN_VALUE}.
     *
     * @return the maximum lane element of this vector
     */
    public abstract int maxLanes();

    /**
     * Returns the maximum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to lane elements,
     * and the identity value is
     * {@link Integer#MIN_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the maximum lane element of this vector
     */
    public abstract int maxLanes(VectorMask<Integer> m);

    /**
     * Logically ORs all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical OR
     * operation ({@code |}) to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical OR all the lane elements of this vector
     */
    public abstract int orLanes();

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
    public abstract int orLanes(VectorMask<Integer> m);

    /**
     * Logically ANDs all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical AND
     * operation ({@code |}) to lane elements,
     * and the identity value is {@code -1}.
     *
     * @return the logical AND all the lane elements of this vector
     */
    public abstract int andLanes();

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
    public abstract int andLanes(VectorMask<Integer> m);

    /**
     * Logically XORs all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical XOR
     * operation ({@code ^}) to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical XOR all the lane elements of this vector
     */
    public abstract int xorLanes();

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
    public abstract int xorLanes(VectorMask<Integer> m);

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract int lane(int i);

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
    public abstract IntVector with(int i, int e);

    // Type specific extractors

    /**
     * Returns an array containing the lane elements of this vector.
     * <p>
     * This method behaves as if it {@link #intoArray(int[], int)} stores}
     * this vector into an allocated array and returns the array as follows:
     * <pre>{@code
     *   int[] a = new int[this.length()];
     *   this.intoArray(a, 0);
     *   return a;
     * }</pre>
     *
     * @return an array containing the the lane elements of this vector
     */
    @ForceInline
    public final int[] toArray() {
        int[] a = new int[species().length()];
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
    public abstract void intoArray(int[] a, int offset);

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
    public abstract void intoArray(int[] a, int offset, VectorMask<Integer> m);

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
    public abstract void intoArray(int[] a, int a_offset, int[] indexMap, int i_offset);

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
    public abstract void intoArray(int[] a, int a_offset, VectorMask<Integer> m, int[] indexMap, int i_offset);
    // Species

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorSpecies<Integer> species();

    /**
     * Class representing {@link IntVector}'s of the same {@link VectorShape VectorShape}.
     */
    static final class IntSpecies extends AbstractSpecies<Integer> {
        final Function<int[], IntVector> vectorFactory;

        private IntSpecies(VectorShape shape,
                          Class<?> vectorType,
                          Class<?> maskType,
                          Function<int[], IntVector> vectorFactory,
                          Function<boolean[], VectorMask<Integer>> maskFactory,
                          Function<IntUnaryOperator, VectorShuffle<Integer>> shuffleFromArrayFactory,
                          fShuffleFromArray<Integer> shuffleFromOpFactory) {
            super(shape, int.class, Integer.SIZE, vectorType, maskType, maskFactory,
                  shuffleFromArrayFactory, shuffleFromOpFactory);
            this.vectorFactory = vectorFactory;
        }

        interface FOp {
            int apply(int i);
        }

        IntVector op(FOp f) {
            int[] res = new int[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return vectorFactory.apply(res);
        }

        IntVector op(VectorMask<Integer> o, FOp f) {
            int[] res = new int[length()];
            boolean[] mbits = ((AbstractMask<Integer>)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return vectorFactory.apply(res);
        }
    }

    /**
     * Finds the preferred species for an element type of {@code int}.
     * <p>
     * A preferred species is a species chosen by the platform that has a
     * shape of maximal bit size.  A preferred species for different element
     * types will have the same shape, and therefore vectors, masks, and
     * shuffles created from such species will be shape compatible.
     *
     * @return the preferred species for an element type of {@code int}
     */
    private static IntSpecies preferredSpecies() {
        return (IntSpecies) VectorSpecies.ofPreferred(int.class);
    }

    /**
     * Finds a species for an element type of {@code int} and shape.
     *
     * @param s the shape
     * @return a species for an element type of {@code int} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    static IntSpecies species(VectorShape s) {
        Objects.requireNonNull(s);
        switch (s) {
            case S_64_BIT: return (IntSpecies) SPECIES_64;
            case S_128_BIT: return (IntSpecies) SPECIES_128;
            case S_256_BIT: return (IntSpecies) SPECIES_256;
            case S_512_BIT: return (IntSpecies) SPECIES_512;
            case S_Max_BIT: return (IntSpecies) SPECIES_MAX;
            default: throw new IllegalArgumentException("Bad shape: " + s);
        }
    }

    /** Species representing {@link IntVector}s of {@link VectorShape#S_64_BIT VectorShape.S_64_BIT}. */
    public static final VectorSpecies<Integer> SPECIES_64 = new IntSpecies(VectorShape.S_64_BIT, Int64Vector.class, Int64Vector.Int64Mask.class,
                                                                     Int64Vector::new, Int64Vector.Int64Mask::new,
                                                                     Int64Vector.Int64Shuffle::new, Int64Vector.Int64Shuffle::new);

    /** Species representing {@link IntVector}s of {@link VectorShape#S_128_BIT VectorShape.S_128_BIT}. */
    public static final VectorSpecies<Integer> SPECIES_128 = new IntSpecies(VectorShape.S_128_BIT, Int128Vector.class, Int128Vector.Int128Mask.class,
                                                                      Int128Vector::new, Int128Vector.Int128Mask::new,
                                                                      Int128Vector.Int128Shuffle::new, Int128Vector.Int128Shuffle::new);

    /** Species representing {@link IntVector}s of {@link VectorShape#S_256_BIT VectorShape.S_256_BIT}. */
    public static final VectorSpecies<Integer> SPECIES_256 = new IntSpecies(VectorShape.S_256_BIT, Int256Vector.class, Int256Vector.Int256Mask.class,
                                                                      Int256Vector::new, Int256Vector.Int256Mask::new,
                                                                      Int256Vector.Int256Shuffle::new, Int256Vector.Int256Shuffle::new);

    /** Species representing {@link IntVector}s of {@link VectorShape#S_512_BIT VectorShape.S_512_BIT}. */
    public static final VectorSpecies<Integer> SPECIES_512 = new IntSpecies(VectorShape.S_512_BIT, Int512Vector.class, Int512Vector.Int512Mask.class,
                                                                      Int512Vector::new, Int512Vector.Int512Mask::new,
                                                                      Int512Vector.Int512Shuffle::new, Int512Vector.Int512Shuffle::new);

    /** Species representing {@link IntVector}s of {@link VectorShape#S_Max_BIT VectorShape.S_Max_BIT}. */
    public static final VectorSpecies<Integer> SPECIES_MAX = new IntSpecies(VectorShape.S_Max_BIT, IntMaxVector.class, IntMaxVector.IntMaxMask.class,
                                                                      IntMaxVector::new, IntMaxVector.IntMaxMask::new,
                                                                      IntMaxVector.IntMaxShuffle::new, IntMaxVector.IntMaxShuffle::new);

    /**
     * Preferred species for {@link IntVector}s.
     * A preferred species is a species of maximal bit size for the platform.
     */
    public static final VectorSpecies<Integer> SPECIES_PREFERRED = (VectorSpecies<Integer>) preferredSpecies();
}
