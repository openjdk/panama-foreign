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

    abstract LongVector uOp(Mask<Long> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        long apply(int i, long a, long b);
    }

    abstract LongVector bOp(Vector<Long> v, FBinOp f);

    abstract LongVector bOp(Vector<Long> v, Mask<Long> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        long apply(int i, long a, long b, long c);
    }

    abstract LongVector tOp(Vector<Long> v1, Vector<Long> v2, FTriOp f);

    abstract LongVector tOp(Vector<Long> v1, Vector<Long> v2, Mask<Long> m, FTriOp f);

    // Reduction operator

    abstract long rOp(long v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, long a, long b);
    }

    abstract Mask<Long> bTest(Vector<Long> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, long a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Long> m, FUnCon f);

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
    public static LongVector zero(Species<Long> species) {
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
     * {@link #fromByteBuffer(Species<Long>, ByteBuffer, int, Mask) method} as follows:
     * <pre>{@code
     * return this.fromByteBuffer(ByteBuffer.wrap(a), i, this.maskAllTrue());
     * }</pre>
     *
     * @param species species of desired vector
     * @param a the byte array
     * @param ix the offset into the array
     * @return a vector loaded from a byte array
     * @throws IndexOutOfBoundsException if {@code i < 0} or
     * {@code i > a.length - (this.length() * this.elementSize() / Byte.SIZE)}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static LongVector fromByteArray(Species<Long> species, byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<LongVector>) species.boxType(), long.class, species.length(),
                                     a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                     a, ix, species,
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
     * {@link #fromByteBuffer(Species<Long>, ByteBuffer, int, Mask) method} as follows:
     * <pre>{@code
     * return this.fromByteBuffer(ByteBuffer.wrap(a), i, m);
     * }</pre>
     *
     * @param species species of desired vector
     * @param a the byte array
     * @param ix the offset into the array
     * @param m the mask
     * @return a vector loaded from a byte array
     * @throws IndexOutOfBoundsException if {@code i < 0} or
     * {@code i > a.length - (this.length() * this.elementSize() / Byte.SIZE)}
     * @throws IndexOutOfBoundsException if the offset is {@code < 0},
     * or {@code > a.length},
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set
     * {@code i >= a.length - (N * this.elementSize() / Byte.SIZE)}
     */
    @ForceInline
    public static LongVector fromByteArray(Species<Long> species, byte[] a, int ix, Mask<Long> m) {
        return zero(species).blend(fromByteArray(species, a, ix), m);
    }

    /**
     * Loads a vector from an array starting at offset.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index, the
     * array element at index {@code i + N} is placed into the
     * resulting vector at lane index {@code N}.
     *
     * @param species species of desired vector
     * @param a the array
     * @param i the offset into the array
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException if {@code i < 0}, or
     * {@code i > a.length - this.length()}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static LongVector fromArray(Species<Long> species, long[] a, int i){
        Objects.requireNonNull(a);
        i = VectorIntrinsics.checkIndex(i, a.length, species.length());
        return VectorIntrinsics.load((Class<LongVector>) species.boxType(), long.class, species.length(),
                                     a, (((long) i) << ARRAY_SHIFT) + Unsafe.ARRAY_LONG_BASE_OFFSET,
                                     a, i, species,
                                     (c, idx, s) -> ((LongSpecies)s).op(n -> c[idx + n]));
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
     * @param species species of desired vector
     * @param a the array
     * @param i the offset into the array
     * @param m the mask
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException if {@code i < 0}, or
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set {@code i > a.length - N}
     */
    @ForceInline
    public static LongVector fromArray(Species<Long> species, long[] a, int i, Mask<Long> m) {
        return zero(species).blend(fromArray(species, a, i), m);
    }

    /**
     * Loads a vector from an array using indexes obtained from an index
     * map.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index, the
     * array element at index {@code i + indexMap[j + N]} is placed into the
     * resulting vector at lane index {@code N}.
     *
     * @param species species of desired vector
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
    @ForceInline
    @SuppressWarnings("unchecked")
    public static LongVector fromArray(Species<Long> species, long[] a, int i, int[] indexMap, int j) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(indexMap);

        if (species.length() == 1) {
          return LongVector.fromArray(species, a, i + indexMap[j]);
        }

        // Index vector: vix[0:n] = k -> i + indexMap[j + k]
        IntVector vix = IntVector.fromArray(IntVector.species(species.indexShape()), indexMap, j).add(i);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        return VectorIntrinsics.loadWithMap((Class<LongVector>) species.boxType(), long.class, species.length(),
                                            IntVector.species(species.indexShape()).boxType(), a, Unsafe.ARRAY_LONG_BASE_OFFSET, vix,
                                            a, i, indexMap, j, species,
                                            (long[] c, int idx, int[] iMap, int idy, Species<Long> s) ->
                                                ((LongSpecies)s).op(n -> c[idx + iMap[idy+n]]));
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
     * @param species species of desired vector
     * @param a the array
     * @param i the offset into the array, may be negative if relative
     * indexes in the index map compensate to produce a value within the
     * array bounds
     * @param m the mask
     * @param indexMap the index map
     * @param j the offset into the index map
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException if {@code j < 0}, or
     * {@code j > indexMap.length - this.length()},
     * or for any vector lane index {@code N} where the mask at lane
     * {@code N} is set the result of {@code i + indexMap[j + N]} is
     * {@code < 0} or {@code >= a.length}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static LongVector fromArray(Species<Long> species, long[] a, int i, Mask<Long> m, int[] indexMap, int j) {
        // @@@ This can result in out of bounds errors for unset mask lanes
        return zero(species).blend(fromArray(species, a, i, indexMap, j), m);
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
     * {@link #fromByteBuffer(Species<Long>, ByteBuffer, int, Mask)} method} as follows:
     * <pre>{@code
     *   return this.fromByteBuffer(b, i, this.maskAllTrue())
     * }</pre>
     *
     * @param species species of desired vector
     * @param bb the byte buffer
     * @param ix the offset into the byte buffer
     * @return a vector loaded from a byte buffer
     * @throws IndexOutOfBoundsException if the offset is {@code < 0},
     * or {@code > b.limit()},
     * or if there are fewer than
     * {@code this.length() * this.elementSize() / Byte.SIZE} bytes
     * remaining in the byte buffer from the given offset
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static LongVector fromByteBuffer(Species<Long> species, ByteBuffer bb, int ix) {
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        ix = VectorIntrinsics.checkIndex(ix, bb.limit(), species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<LongVector>) species.boxType(), long.class, species.length(),
                                     U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + ix,
                                     bb, ix, species,
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
     * {@coce EBuffer} is the primitive buffer type, {@code e} is the
     * primitive element type, and {@code ESpecies<S>} is the primitive
     * species for {@code e}:
     * <pre>{@code
     * EBuffer eb = b.duplicate().
     *     order(ByteOrder.nativeOrder()).position(i).
     *     asEBuffer();
     * e[] es = new e[this.length()];
     * for (int n = 0; n < t.length; n++) {
     *     if (m.isSet(n))
     *         es[n] = eb.get(n);
     * }
     * Vector<E> r = ((ESpecies<S>)this).fromArray(es, 0, m);
     * }</pre>
     *
     * @param species species of desired vector
     * @param bb the byte buffer
     * @param ix the offset into the byte buffer
     * @param m the mask
     * @return a vector loaded from a byte buffer
     * @throws IndexOutOfBoundsException if the offset is {@code < 0},
     * or {@code > b.limit()},
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set
     * {@code i >= b.limit() - (N * this.elementSize() / Byte.SIZE)}
     */
    @ForceInline
    public static LongVector fromByteBuffer(Species<Long> species, ByteBuffer bb, int ix, Mask<Long> m) {
        return zero(species).blend(fromByteBuffer(species, bb, ix), m);
    }

    /**
     * Returns a vector where all lane elements are set to the primitive
     * value {@code e}.
     *
     * @param s species of the desired vector
     * @param e the value
     * @return a vector of vector where all lane elements are set to
     * the primitive value {@code e}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static LongVector broadcast(Species<Long> s, long e) {
        return VectorIntrinsics.broadcastCoerced(
            (Class<LongVector>) s.boxType(), long.class, s.length(),
            e, s,
            ((bits, sp) -> ((LongSpecies)sp).op(i -> (long)bits)));
    }

    /**
     * Returns a vector where each lane element is set to a given
     * primitive value.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index, the
     * the primitive value at index {@code N} is placed into the resulting
     * vector at lane index {@code N}.
     *
     * @param s species of the desired vector
     * @param es the given primitive values
     * @return a vector where each lane element is set to a given primitive
     * value
     * @throws IndexOutOfBoundsException if {@code es.length < this.length()}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static LongVector scalars(Species<Long> s, long... es) {
        Objects.requireNonNull(es);
        int ix = VectorIntrinsics.checkIndex(0, es.length, s.length());
        return VectorIntrinsics.load((Class<LongVector>) s.boxType(), long.class, s.length(),
                                     es, Unsafe.ARRAY_LONG_BASE_OFFSET,
                                     es, ix, s,
                                     (c, idx, sp) -> ((LongSpecies)sp).op(n -> c[idx + n]));
    }

    /**
     * Returns a vector where the first lane element is set to the primtive
     * value {@code e}, all other lane elements are set to the default
     * value.
     *
     * @param s species of the desired vector
     * @param e the value
     * @return a vector where the first lane element is set to the primitive
     * value {@code e}
     */
    @ForceInline
    public static final LongVector single(Species<Long> s, long e) {
        return zero(s).with(0, e);
    }

    /**
     * Returns a vector where each lane element is set to a randomly
     * generated primitive value.
     *
     * The semantics are equivalent to calling
     * {@link ThreadLocalRandom#nextLong()}
     *
     * @param s species of the desired vector
     * @return a vector where each lane elements is set to a randomly
     * generated primitive value
     */
    public static LongVector random(Species<Long> s) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return ((LongSpecies)s).op(i -> r.nextLong());
    }

    /**
     * Returns a mask where each lane is set or unset according to given
     * {@code boolean} values
     * <p>
     * For each mask lane, where {@code N} is the mask lane index,
     * if the given {@code boolean} value at index {@code N} is {@code true}
     * then the mask lane at index {@code N} is set, otherwise it is unset.
     *
     * @param species mask species
     * @param bits the given {@code boolean} values
     * @return a mask where each lane is set or unset according to the given {@code boolean} value
     * @throws IndexOutOfBoundsException if {@code bits.length < species.length()}
     */
    @ForceInline
    public static Mask<Long> maskFromValues(Species<Long> species, boolean... bits) {
        if (species.boxType() == LongMaxVector.class)
            return new LongMaxVector.LongMaxMask(bits);
        switch (species.bitSize()) {
            case 64: return new Long64Vector.Long64Mask(bits);
            case 128: return new Long128Vector.Long128Mask(bits);
            case 256: return new Long256Vector.Long256Mask(bits);
            case 512: return new Long512Vector.Long512Mask(bits);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    // @@@ This is a bad implementation -- makes lambdas capturing -- fix this
    static Mask<Long> trueMask(Species<Long> species) {
        if (species.boxType() == LongMaxVector.class)
            return LongMaxVector.LongMaxMask.TRUE_MASK;
        switch (species.bitSize()) {
            case 64: return Long64Vector.Long64Mask.TRUE_MASK;
            case 128: return Long128Vector.Long128Mask.TRUE_MASK;
            case 256: return Long256Vector.Long256Mask.TRUE_MASK;
            case 512: return Long512Vector.Long512Mask.TRUE_MASK;
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    static Mask<Long> falseMask(Species<Long> species) {
        if (species.boxType() == LongMaxVector.class)
            return LongMaxVector.LongMaxMask.FALSE_MASK;
        switch (species.bitSize()) {
            case 64: return Long64Vector.Long64Mask.FALSE_MASK;
            case 128: return Long128Vector.Long128Mask.FALSE_MASK;
            case 256: return Long256Vector.Long256Mask.FALSE_MASK;
            case 512: return Long512Vector.Long512Mask.FALSE_MASK;
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    /**
     * Loads a mask from a {@code boolean} array starting at an offset.
     * <p>
     * For each mask lane, where {@code N} is the mask lane index,
     * if the array element at index {@code ix + N} is {@code true} then the
     * mask lane at index {@code N} is set, otherwise it is unset.
     *
     * @param species mask species
     * @param bits the {@code boolean} array
     * @param ix the offset into the array
     * @return the mask loaded from a {@code boolean} array
     * @throws IndexOutOfBoundsException if {@code ix < 0}, or
     * {@code ix > bits.length - species.length()}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static Mask<Long> maskFromArray(Species<Long> species, boolean[] bits, int ix) {
        Objects.requireNonNull(bits);
        ix = VectorIntrinsics.checkIndex(ix, bits.length, species.length());
        return VectorIntrinsics.load((Class<Mask<Long>>) species.maskType(), long.class, species.length(),
                                     bits, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_BOOLEAN_BASE_OFFSET,
                                     bits, ix, species,
                                     (c, idx, s) -> (Mask<Long>) ((LongSpecies)s).opm(n -> c[idx + n]));
    }

    /**
     * Returns a mask where all lanes are set.
     *
     * @param species mask species
     * @return a mask where all lanes are set
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static Mask<Long> maskAllTrue(Species<Long> species) {
        return VectorIntrinsics.broadcastCoerced((Class<Mask<Long>>) species.maskType(), long.class, species.length(),
                                                 (long)-1,  species,
                                                 ((z, s) -> trueMask(s)));
    }

    /**
     * Returns a mask where all lanes are unset.
     *
     * @param species mask species
     * @return a mask where all lanes are unset
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static Mask<Long> maskAllFalse(Species<Long> species) {
        return VectorIntrinsics.broadcastCoerced((Class<Mask<Long>>) species.maskType(), long.class, species.length(),
                                                 0, species, 
                                                 ((z, s) -> falseMask(s)));
    }

    /**
     * Returns a shuffle of mapped indexes where each lane element is
     * the result of applying a mapping function to the corresponding lane
     * index.
     * <p>
     * Care should be taken to ensure Shuffle values produced from this
     * method are consumed as constants to ensure optimal generation of
     * code.  For example, values held in static final fields or values
     * held in loop constant local variables.
     * <p>
     * This method behaves as if a shuffle is created from an array of
     * mapped indexes as follows:
     * <pre>{@code
     *   int[] a = new int[species.length()];
     *   for (int i = 0; i < a.length; i++) {
     *       a[i] = f.applyAsInt(i);
     *   }
     *   return this.shuffleFromValues(a);
     * }</pre>
     *
     * @param species shuffle species
     * @param f the lane index mapping function
     * @return a shuffle of mapped indexes
     */
    @ForceInline
    public static Shuffle<Long> shuffle(Species<Long> species, IntUnaryOperator f) {
        if (species.boxType() == LongMaxVector.class)
            return new LongMaxVector.LongMaxShuffle(f);
        switch (species.bitSize()) {
            case 64: return new Long64Vector.Long64Shuffle(f);
            case 128: return new Long128Vector.Long128Shuffle(f);
            case 256: return new Long256Vector.Long256Shuffle(f);
            case 512: return new Long512Vector.Long512Shuffle(f);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    /**
     * Returns a shuffle where each lane element is the value of its
     * corresponding lane index.
     * <p>
     * This method behaves as if a shuffle is created from an identity
     * index mapping function as follows:
     * <pre>{@code
     *   return this.shuffle(i -> i);
     * }</pre>
     *
     * @param species shuffle species
     * @return a shuffle of lane indexes
     */
    @ForceInline
    public static Shuffle<Long> shuffleIota(Species<Long> species) {
        if (species.boxType() == LongMaxVector.class)
            return new LongMaxVector.LongMaxShuffle(AbstractShuffle.IDENTITY);
        switch (species.bitSize()) {
            case 64: return new Long64Vector.Long64Shuffle(AbstractShuffle.IDENTITY);
            case 128: return new Long128Vector.Long128Shuffle(AbstractShuffle.IDENTITY);
            case 256: return new Long256Vector.Long256Shuffle(AbstractShuffle.IDENTITY);
            case 512: return new Long512Vector.Long512Shuffle(AbstractShuffle.IDENTITY);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    /**
     * Returns a shuffle where each lane element is set to a given
     * {@code int} value logically AND'ed by the species length minus one.
     * <p>
     * For each shuffle lane, where {@code N} is the shuffle lane index, the
     * the {@code int} value at index {@code N} logically AND'ed by
     * {@code species.length() - 1} is placed into the resulting shuffle at
     * lane index {@code N}.
     *
     * @param species shuffle species
     * @param ixs the given {@code int} values
     * @return a shuffle where each lane element is set to a given
     * {@code int} value
     * @throws IndexOutOfBoundsException if the number of int values is
     * {@code < species.length()}
     */
    @ForceInline
    public static Shuffle<Long> shuffleFromValues(Species<Long> species, int... ixs) {
        if (species.boxType() == LongMaxVector.class)
            return new LongMaxVector.LongMaxShuffle(ixs);
        switch (species.bitSize()) {
            case 64: return new Long64Vector.Long64Shuffle(ixs);
            case 128: return new Long128Vector.Long128Shuffle(ixs);
            case 256: return new Long256Vector.Long256Shuffle(ixs);
            case 512: return new Long512Vector.Long512Shuffle(ixs);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    /**
     * Loads a shuffle from an {@code int} array starting at an offset.
     * <p>
     * For each shuffle lane, where {@code N} is the shuffle lane index, the
     * array element at index {@code i + N} logically AND'ed by
     * {@code species.length() - 1} is placed into the resulting shuffle at lane
     * index {@code N}.
     *
     * @param species shuffle species
     * @param ixs the {@code int} array
     * @param i the offset into the array
     * @return a shuffle loaded from the {@code int} array
     * @throws IndexOutOfBoundsException if {@code i < 0}, or
     * {@code i > a.length - species.length()}
     */
    @ForceInline
    public static Shuffle<Long> shuffleFromArray(Species<Long> species, int[] ixs, int i) {
        if (species.boxType() == LongMaxVector.class)
            return new LongMaxVector.LongMaxShuffle(ixs, i);
        switch (species.bitSize()) {
            case 64: return new Long64Vector.Long64Shuffle(ixs, i);
            case 128: return new Long128Vector.Long128Shuffle(ixs, i);
            case 256: return new Long256Vector.Long256Shuffle(ixs, i);
            case 512: return new Long512Vector.Long512Shuffle(ixs, i);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    // Ops

    @Override
    public abstract LongVector add(Vector<Long> v);

    /**
     * Adds this vector to the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive addition operation
     * ({@code +}) is applied to lane elements.
     *
     * @param s the input scalar
     * @return the result of adding this vector to the broadcast of an input
     * scalar
     */
    public abstract LongVector add(long s);

    @Override
    public abstract LongVector add(Vector<Long> v, Mask<Long> m);

    /**
     * Adds this vector to broadcast of an input scalar,
     * selecting lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the primitive addition operation
     * ({@code +}) is applied to lane elements.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the result of adding this vector to the broadcast of an input
     * scalar
     */
    public abstract LongVector add(long s, Mask<Long> m);

    @Override
    public abstract LongVector sub(Vector<Long> v);

    /**
     * Subtracts the broadcast of an input scalar from this vector.
     * <p>
     * This is a vector binary operation where the primitive subtraction
     * operation ({@code -}) is applied to lane elements.
     *
     * @param s the input scalar
     * @return the result of subtracting the broadcast of an input
     * scalar from this vector
     */
    public abstract LongVector sub(long s);

    @Override
    public abstract LongVector sub(Vector<Long> v, Mask<Long> m);

    /**
     * Subtracts the broadcast of an input scalar from this vector, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the primitive subtraction
     * operation ({@code -}) is applied to lane elements.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the result of subtracting the broadcast of an input
     * scalar from this vector
     */
    public abstract LongVector sub(long s, Mask<Long> m);

    @Override
    public abstract LongVector mul(Vector<Long> v);

    /**
     * Multiplies this vector with the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive multiplication
     * operation ({@code *}) is applied to lane elements.
     *
     * @param s the input scalar
     * @return the result of multiplying this vector with the broadcast of an
     * input scalar
     */
    public abstract LongVector mul(long s);

    @Override
    public abstract LongVector mul(Vector<Long> v, Mask<Long> m);

    /**
     * Multiplies this vector with the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the primitive multiplication
     * operation ({@code *}) is applied to lane elements.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the result of multiplying this vector with the broadcast of an
     * input scalar
     */
    public abstract LongVector mul(long s, Mask<Long> m);

    @Override
    public abstract LongVector neg();

    @Override
    public abstract LongVector neg(Mask<Long> m);

    @Override
    public abstract LongVector abs();

    @Override
    public abstract LongVector abs(Mask<Long> m);

    @Override
    public abstract LongVector min(Vector<Long> v);

    @Override
    public abstract LongVector min(Vector<Long> v, Mask<Long> m);

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> Math.min(a, b)} is applied to lane elements.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract LongVector min(long s);

    @Override
    public abstract LongVector max(Vector<Long> v);

    @Override
    public abstract LongVector max(Vector<Long> v, Mask<Long> m);

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> Math.max(a, b)} is applied to lane elements.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract LongVector max(long s);

    @Override
    public abstract Mask<Long> equal(Vector<Long> v);

    /**
     * Tests if this vector is equal to the broadcast of an input scalar.
     * <p>
     * This is a vector binary test operation where the primitive equals
     * operation ({@code ==}) is applied to lane elements.
     *
     * @param s the input scalar
     * @return the result mask of testing if this vector is equal to the
     * broadcast of an input scalar
     */
    public abstract Mask<Long> equal(long s);

    @Override
    public abstract Mask<Long> notEqual(Vector<Long> v);

    /**
     * Tests if this vector is not equal to the broadcast of an input scalar.
     * <p>
     * This is a vector binary test operation where the primitive not equals
     * operation ({@code !=}) is applied to lane elements.
     *
     * @param s the input scalar
     * @return the result mask of testing if this vector is not equal to the
     * broadcast of an input scalar
     */
    public abstract Mask<Long> notEqual(long s);

    @Override
    public abstract Mask<Long> lessThan(Vector<Long> v);

    /**
     * Tests if this vector is less than the broadcast of an input scalar.
     * <p>
     * This is a vector binary test operation where the primitive less than
     * operation ({@code <}) is applied to lane elements.
     *
     * @param s the input scalar
     * @return the mask result of testing if this vector is less than the
     * broadcast of an input scalar
     */
    public abstract Mask<Long> lessThan(long s);

    @Override
    public abstract Mask<Long> lessThanEq(Vector<Long> v);

    /**
     * Tests if this vector is less or equal to the broadcast of an input scalar.
     * <p>
     * This is a vector binary test operation where the primitive less than
     * or equal to operation ({@code <=}) is applied to lane elements.
     *
     * @param s the input scalar
     * @return the mask result of testing if this vector is less than or equal
     * to the broadcast of an input scalar
     */
    public abstract Mask<Long> lessThanEq(long s);

    @Override
    public abstract Mask<Long> greaterThan(Vector<Long> v);

    /**
     * Tests if this vector is greater than the broadcast of an input scalar.
     * <p>
     * This is a vector binary test operation where the primitive greater than
     * operation ({@code >}) is applied to lane elements.
     *
     * @param s the input scalar
     * @return the mask result of testing if this vector is greater than the
     * broadcast of an input scalar
     */
    public abstract Mask<Long> greaterThan(long s);

    @Override
    public abstract Mask<Long> greaterThanEq(Vector<Long> v);

    /**
     * Tests if this vector is greater than or equal to the broadcast of an
     * input scalar.
     * <p>
     * This is a vector binary test operation where the primitive greater than
     * or equal to operation ({@code >=}) is applied to lane elements.
     *
     * @param s the input scalar
     * @return the mask result of testing if this vector is greater than or
     * equal to the broadcast of an input scalar
     */
    public abstract Mask<Long> greaterThanEq(long s);

    @Override
    public abstract LongVector blend(Vector<Long> v, Mask<Long> m);

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
    public abstract LongVector blend(long s, Mask<Long> m);

    @Override
    public abstract LongVector rearrange(Vector<Long> v,
                                                      Shuffle<Long> s, Mask<Long> m);

    @Override
    public abstract LongVector rearrange(Shuffle<Long> m);

    @Override
    public abstract LongVector reshape(Species<Long> s);

    @Override
    public abstract LongVector rotateEL(int i);

    @Override
    public abstract LongVector rotateER(int i);

    @Override
    public abstract LongVector shiftEL(int i);

    @Override
    public abstract LongVector shiftER(int i);



    /**
     * Bitwise ANDs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise AND
     * operation ({@code &}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise AND of this vector with the input vector
     */
    public abstract LongVector and(Vector<Long> v);

    /**
     * Bitwise ANDs this vector with the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive bitwise AND
     * operation ({@code &}) is applied to lane elements.
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
     * This is a vector binary operation where the primitive bitwise AND
     * operation ({@code &}) is applied to lane elements.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the bitwise AND of this vector with the input vector
     */
    public abstract LongVector and(Vector<Long> v, Mask<Long> m);

    /**
     * Bitwise ANDs this vector with the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the primitive bitwise AND
     * operation ({@code &}) is applied to lane elements.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the bitwise AND of this vector with the broadcast of an input
     * scalar
     */
    public abstract LongVector and(long s, Mask<Long> m);

    /**
     * Bitwise ORs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise OR
     * operation ({@code |}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise OR of this vector with the input vector
     */
    public abstract LongVector or(Vector<Long> v);

    /**
     * Bitwise ORs this vector with the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive bitwise OR
     * operation ({@code |}) is applied to lane elements.
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
     * This is a vector binary operation where the primitive bitwise OR
     * operation ({@code |}) is applied to lane elements.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the bitwise OR of this vector with the input vector
     */
    public abstract LongVector or(Vector<Long> v, Mask<Long> m);

    /**
     * Bitwise ORs this vector with the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the primitive bitwise OR
     * operation ({@code |}) is applied to lane elements.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the bitwise OR of this vector with the broadcast of an input
     * scalar
     */
    public abstract LongVector or(long s, Mask<Long> m);

    /**
     * Bitwise XORs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise XOR
     * operation ({@code ^}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise XOR of this vector with the input vector
     */
    public abstract LongVector xor(Vector<Long> v);

    /**
     * Bitwise XORs this vector with the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive bitwise XOR
     * operation ({@code ^}) is applied to lane elements.
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
     * This is a vector binary operation where the primitive bitwise XOR
     * operation ({@code ^}) is applied to lane elements.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the bitwise XOR of this vector with the input vector
     */
    public abstract LongVector xor(Vector<Long> v, Mask<Long> m);

    /**
     * Bitwise XORs this vector with the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the primitive bitwise XOR
     * operation ({@code ^}) is applied to lane elements.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the bitwise XOR of this vector with the broadcast of an input
     * scalar
     */
    public abstract LongVector xor(long s, Mask<Long> m);

    /**
     * Bitwise NOTs this vector.
     * <p>
     * This is a vector unary operation where the primitive bitwise NOT
     * operation ({@code ~}) is applied to lane elements.
     *
     * @return the bitwise NOT of this vector
     */
    public abstract LongVector not();

    /**
     * Bitwise NOTs this vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a vector unary operation where the primitive bitwise NOT
     * operation ({@code ~}) is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the bitwise NOT of this vector
     */
    public abstract LongVector not(Mask<Long> m);

    /**
     * Logically left shifts this vector by the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive logical left shift
     * operation ({@code <<}) is applied to lane elements.
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
     * This is a vector binary operation where the primitive logical left shift
     * operation ({@code <<}) is applied to lane elements.
     *
     * @param s the input scalar; the number of the bits to left shift
     * @param m the mask controlling lane selection
     * @return the result of logically left shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract LongVector shiftL(int s, Mask<Long> m);

    /**
     * Logically left shifts this vector by an input vector.
     * <p>
     * This is a vector binary operation where the primitive logical left shift
     * operation ({@code <<}) is applied to lane elements.
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
     * This is a vector binary operation where the primitive logical left shift
     * operation ({@code <<}) is applied to lane elements.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of logically left shifting this vector by the input
     * vector
     */
    public LongVector shiftL(Vector<Long> v, Mask<Long> m) {
        return bOp(v, m, (i, a, b) -> (long) (a << b));
    }

    // logical, or unsigned, shift right

    /**
     * Logically right shifts (or unsigned right shifts) this vector by the
     * broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive logical right shift
     * operation ({@code >>>}) is applied to lane elements.
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
     * This is a vector binary operation where the primitive logical right shift
     * operation ({@code >>>}) is applied to lane elements.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @param m the mask controlling lane selection
     * @return the result of logically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract LongVector shiftR(int s, Mask<Long> m);

    /**
     * Logically right shifts (or unsigned right shifts) this vector by an
     * input vector.
     * <p>
     * This is a vector binary operation where the primitive logical right shift
     * operation ({@code >>>}) is applied to lane elements.
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
     * This is a vector binary operation where the primitive logical right shift
     * operation ({@code >>>}) is applied to lane elements.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of logically right shifting this vector by the
     * input vector
     */
    public LongVector shiftR(Vector<Long> v, Mask<Long> m) {
        return bOp(v, m, (i, a, b) -> (long) (a >>> b));
    }

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by the
     * broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive arithmetic right
     * shift operation ({@code >>}) is applied to lane elements.
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
     * This is a vector binary operation where the primitive arithmetic right
     * shift operation ({@code >>}) is applied to lane elements.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @param m the mask controlling lane selection
     * @return the result of arithmetically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract LongVector aShiftR(int s, Mask<Long> m);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by an
     * input vector.
     * <p>
     * This is a vector binary operation where the primitive arithmetic right
     * shift operation ({@code >>}) is applied to lane elements.
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
     * This is a vector binary operation where the primitive arithmetic right
     * shift operation ({@code >>}) is applied to lane elements.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of arithmetically right shifting this vector by the
     * input vector
     */
    public LongVector aShiftR(Vector<Long> v, Mask<Long> m) {
        return bOp(v, m, (i, a, b) -> (long) (a >> b));
    }

    /**
     * Rotates left this vector by the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@link Long#rotateLeft} is applied to lane elements and where
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
     * This is a vector binary operation where the operation
     * {@link Long#rotateLeft} is applied to lane elements and where
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
    public final LongVector rotateL(int s, Mask<Long> m) {
        return shiftL(s, m).or(shiftR(-s, m), m);
    }

    /**
     * Rotates right this vector by the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@link Long#rotateRight} is applied to lane elements and where
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
     * This is a vector binary operation where the operation
     * {@link Long#rotateRight} is applied to lane elements and where
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
    public final LongVector rotateR(int s, Mask<Long> m) {
        return shiftR(s, m).or(shiftL(-s, m), m);
    }

    @Override
    public abstract void intoByteArray(byte[] a, int ix);

    @Override
    public abstract void intoByteArray(byte[] a, int ix, Mask<Long> m);

    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix);

    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix, Mask<Long> m);


    // Type specific horizontal reductions
    /**
     * Adds all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the addition
     * operation ({@code +}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the addition of all the lane elements of this vector
     */
    public abstract long addAll();

    /**
     * Adds all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the addition
     * operation ({@code +}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @param m the mask controlling lane selection
     * @return the addition of the selected lane elements of this vector
     */
    public abstract long addAll(Mask<Long> m);

    /**
     * Multiplies all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the
     * multiplication operation ({@code *}) is applied to lane elements,
     * and the identity value is {@code 1}.
     *
     * @return the multiplication of all the lane elements of this vector
     */
    public abstract long mulAll();

    /**
     * Multiplies all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the
     * multiplication operation ({@code *}) is applied to lane elements,
     * and the identity value is {@code 1}.
     *
     * @param m the mask controlling lane selection
     * @return the multiplication of all the lane elements of this vector
     */
    public abstract long mulAll(Mask<Long> m);

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.min(a, b)} is applied to lane elements,
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
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.min(a, b)} is applied to lane elements,
     * and the identity value is
     * {@link Long#MAX_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the minimum lane element of this vector
     */
    public abstract long minAll(Mask<Long> m);

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.max(a, b)} is applied to lane elements,
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
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.max(a, b)} is applied to lane elements,
     * and the identity value is
     * {@link Long#MIN_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the maximum lane element of this vector
     */
    public abstract long maxAll(Mask<Long> m);

    /**
     * Logically ORs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical OR
     * operation ({@code |}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical OR all the lane elements of this vector
     */
    public abstract long orAll();

    /**
     * Logically ORs all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the logical OR
     * operation ({@code |}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @param m the mask controlling lane selection
     * @return the logical OR all the lane elements of this vector
     */
    public abstract long orAll(Mask<Long> m);

    /**
     * Logically ANDs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical AND
     * operation ({@code |}) is applied to lane elements,
     * and the identity value is {@code -1}.
     *
     * @return the logical AND all the lane elements of this vector
     */
    public abstract long andAll();

    /**
     * Logically ANDs all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the logical AND
     * operation ({@code |}) is applied to lane elements,
     * and the identity value is {@code -1}.
     *
     * @param m the mask controlling lane selection
     * @return the logical AND all the lane elements of this vector
     */
    public abstract long andAll(Mask<Long> m);

    /**
     * Logically XORs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical XOR
     * operation ({@code ^}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical XOR all the lane elements of this vector
     */
    public abstract long xorAll();

    /**
     * Logically XORs all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the logical XOR
     * operation ({@code ^}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @param m the mask controlling lane selection
     * @return the logical XOR all the lane elements of this vector
     */
    public abstract long xorAll(Mask<Long> m);

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract long get(int i);

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
     * {@code i + N}.
     *
     * @param a the array
     * @param i the offset into the array
     * @throws IndexOutOfBoundsException if {@code i < 0}, or
     * {@code i > a.length - this.length()}
     */
    public abstract void intoArray(long[] a, int i);

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
    public abstract void intoArray(long[] a, int i, Mask<Long> m);

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
    public abstract void intoArray(long[] a, int i, int[] indexMap, int j);

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
    public abstract void intoArray(long[] a, int i, Mask<Long> m, int[] indexMap, int j);
    // Species

    @Override
    public abstract Species<Long> species();

    /**
     * Class representing {@link LongVector}'s of the same {@link Vector.Shape Shape}.
     */
    static final class LongSpecies extends Vector.AbstractSpecies<Long> {
        final Function<long[], LongVector> vectorFactory;
        final Function<boolean[], Vector.Mask<Long>> maskFactory;

        private LongSpecies(Vector.Shape shape,
                          Class<?> boxType,
                          Class<?> maskType,
                          Function<long[], LongVector> vectorFactory,
                          Function<boolean[], Vector.Mask<Long>> maskFactory) {
            super(shape, long.class, Long.SIZE, boxType, maskType);
            this.vectorFactory = vectorFactory;
            this.maskFactory = maskFactory;
        }

        interface FOp {
            long apply(int i);
        }

        interface FOpm {
            boolean apply(int i);
        }

        LongVector op(FOp f) {
            long[] res = new long[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return vectorFactory.apply(res);
        }

        LongVector op(Vector.Mask<Long> o, FOp f) {
            long[] res = new long[length()];
            boolean[] mbits = ((AbstractMask<Long>)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return vectorFactory.apply(res);
        }

        Vector.Mask<Long> opm(IntVector.IntSpecies.FOpm f) {
            boolean[] res = new boolean[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = (boolean)f.apply(i);
            }
            return maskFactory.apply(res);
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
        return (LongSpecies) Species.ofPreferred(long.class);
    }

    /**
     * Finds a species for an element type of {@code long} and shape.
     *
     * @param s the shape
     * @return a species for an element type of {@code long} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    static LongSpecies species(Vector.Shape s) {
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

    /** Species representing {@link LongVector}s of {@link Vector.Shape#S_64_BIT Shape.S_64_BIT}. */
    public static final Species<Long> SPECIES_64 = new LongSpecies(Shape.S_64_BIT, Long64Vector.class, Long64Vector.Long64Mask.class,
                                                                     Long64Vector::new, Long64Vector.Long64Mask::new);

    /** Species representing {@link LongVector}s of {@link Vector.Shape#S_128_BIT Shape.S_128_BIT}. */
    public static final Species<Long> SPECIES_128 = new LongSpecies(Shape.S_128_BIT, Long128Vector.class, Long128Vector.Long128Mask.class,
                                                                      Long128Vector::new, Long128Vector.Long128Mask::new);

    /** Species representing {@link LongVector}s of {@link Vector.Shape#S_256_BIT Shape.S_256_BIT}. */
    public static final Species<Long> SPECIES_256 = new LongSpecies(Shape.S_256_BIT, Long256Vector.class, Long256Vector.Long256Mask.class,
                                                                      Long256Vector::new, Long256Vector.Long256Mask::new);

    /** Species representing {@link LongVector}s of {@link Vector.Shape#S_512_BIT Shape.S_512_BIT}. */
    public static final Species<Long> SPECIES_512 = new LongSpecies(Shape.S_512_BIT, Long512Vector.class, Long512Vector.Long512Mask.class,
                                                                      Long512Vector::new, Long512Vector.Long512Mask::new);

    /** Species representing {@link LongVector}s of {@link Vector.Shape#S_Max_BIT Shape.S_Max_BIT}. */
    public static final Species<Long> SPECIES_MAX = new LongSpecies(Shape.S_Max_BIT, LongMaxVector.class, LongMaxVector.LongMaxMask.class,
                                                                      LongMaxVector::new, LongMaxVector.LongMaxMask::new);

    /**
     * Preferred species for {@link LongVector}s.
     * A preferred species is a species of maximal bit size for the platform.
     */
    public static final Species<Long> SPECIES_PREFERRED = (Species<Long>) preferredSpecies();
}
