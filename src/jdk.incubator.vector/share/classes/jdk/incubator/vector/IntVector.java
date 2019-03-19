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

    abstract IntVector uOp(Mask<Integer> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        int apply(int i, int a, int b);
    }

    abstract IntVector bOp(Vector<Integer> v, FBinOp f);

    abstract IntVector bOp(Vector<Integer> v, Mask<Integer> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        int apply(int i, int a, int b, int c);
    }

    abstract IntVector tOp(Vector<Integer> v1, Vector<Integer> v2, FTriOp f);

    abstract IntVector tOp(Vector<Integer> v1, Vector<Integer> v2, Mask<Integer> m, FTriOp f);

    // Reduction operator

    abstract int rOp(int v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, int a, int b);
    }

    abstract Mask<Integer> bTest(Vector<Integer> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, int a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Integer> m, FUnCon f);

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
    public static IntVector zero(IntSpecies species) {
        return species.zero();
    }

    /**
     * Loads a vector from a byte array starting at an offset.
     * <p>
     * Bytes are composed into primitive lane elements according to the
     * native byte order of the underlying platform
     * <p>
     * This method behaves as if it returns the result of calling the
     * byte buffer, offset, and mask accepting
     * {@link #fromByteBuffer(IntSpecies, ByteBuffer, int, Mask) method} as follows:
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
    public static IntVector fromByteArray(IntSpecies species, byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<IntVector>) species.boxType(), int.class, species.length(),
                                     a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                     a, ix, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
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
     * {@link #fromByteBuffer(IntSpecies, ByteBuffer, int, Mask) method} as follows:
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
    public static IntVector fromByteArray(IntSpecies species, byte[] a, int ix, Mask<Integer> m) {
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
    public static IntVector fromArray(IntSpecies species, int[] a, int i){
        Objects.requireNonNull(a);
        i = VectorIntrinsics.checkIndex(i, a.length, species.length());
        return VectorIntrinsics.load((Class<IntVector>) species.boxType(), int.class, species.length(),
                                     a, (((long) i) << ARRAY_SHIFT) + Unsafe.ARRAY_INT_BASE_OFFSET,
                                     a, i, species,
                                     (c, idx, s) -> ((IntSpecies)s).op(n -> c[idx + n]));
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
    public static IntVector fromArray(IntSpecies species, int[] a, int i, Mask<Integer> m) {
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
    public static IntVector fromArray(IntSpecies species, int[] a, int i, int[] indexMap, int j) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(indexMap);


        // Index vector: vix[0:n] = k -> i + indexMap[j + i]
        IntVector vix = IntVector.fromArray(species.indexSpecies(), indexMap, j).add(i);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        return VectorIntrinsics.loadWithMap((Class<IntVector>) species.boxType(), int.class, species.length(),
                                            species.indexSpecies().vectorType(), a, Unsafe.ARRAY_INT_BASE_OFFSET, vix,
                                            a, i, indexMap, j, species,
                                           (c, idx, iMap, idy, s) -> ((IntSpecies)s).op(n -> c[idx + iMap[idy+n]]));
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
    public static IntVector fromArray(IntSpecies species, int[] a, int i, Mask<Integer> m, int[] indexMap, int j) {
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
     * {@link #fromByteBuffer(IntSpecies, ByteBuffer, int, Mask)} method} as follows:
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
    public static IntVector fromByteBuffer(IntSpecies species, ByteBuffer bb, int ix) {
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        ix = VectorIntrinsics.checkIndex(ix, bb.limit(), species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<IntVector>) species.boxType(), int.class, species.length(),
                                     U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + ix,
                                     bb, ix, species,
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
    public static IntVector fromByteBuffer(IntSpecies species, ByteBuffer bb, int ix, Mask<Integer> m) {
        return zero(species).blend(fromByteBuffer(species, bb, ix), m);
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
    public static Mask<Integer> maskFromValues(IntSpecies species, boolean... bits) {
        if (species.boxType() == IntMaxVector.class)
            return new IntMaxVector.IntMaxMask(bits);
        switch (species.bitSize()) {
            case 64: return new Int64Vector.Int64Mask(bits);
            case 128: return new Int128Vector.Int128Mask(bits);
            case 256: return new Int256Vector.Int256Mask(bits);
            case 512: return new Int512Vector.Int512Mask(bits);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    // @@@ This is a bad implementation -- makes lambdas capturing -- fix this
    static Mask<Integer> trueMask(IntSpecies species) {
        if (species.boxType() == IntMaxVector.class)
            return IntMaxVector.IntMaxMask.TRUE_MASK;
        switch (species.bitSize()) {
            case 64: return Int64Vector.Int64Mask.TRUE_MASK;
            case 128: return Int128Vector.Int128Mask.TRUE_MASK;
            case 256: return Int256Vector.Int256Mask.TRUE_MASK;
            case 512: return Int512Vector.Int512Mask.TRUE_MASK;
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    static Mask<Integer> falseMask(IntSpecies species) {
        if (species.boxType() == IntMaxVector.class)
            return IntMaxVector.IntMaxMask.FALSE_MASK;
        switch (species.bitSize()) {
            case 64: return Int64Vector.Int64Mask.FALSE_MASK;
            case 128: return Int128Vector.Int128Mask.FALSE_MASK;
            case 256: return Int256Vector.Int256Mask.FALSE_MASK;
            case 512: return Int512Vector.Int512Mask.FALSE_MASK;
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
    public static Mask<Integer> maskFromArray(IntSpecies species, boolean[] bits, int ix) {
        Objects.requireNonNull(bits);
        ix = VectorIntrinsics.checkIndex(ix, bits.length, species.length());
        return VectorIntrinsics.load((Class<Mask<Integer>>) species.maskType(), int.class, species.length(),
                                     bits, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_BOOLEAN_BASE_OFFSET,
                                     bits, ix, species,
                                     (c, idx, s) -> (Mask<Integer>) ((IntSpecies)s).opm(n -> c[idx + n]));
    }

    /**
     * Returns a mask where all lanes are set.
     *
     * @param species mask species
     * @return a mask where all lanes are set
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static Mask<Integer> maskAllTrue(IntSpecies species) {
        return VectorIntrinsics.broadcastCoerced((Class<Mask<Integer>>) species.maskType(), int.class, species.length(),
                                                 (int)-1,  species,
                                                 ((z, s) -> trueMask((IntSpecies)s)));
    }

    /**
     * Returns a mask where all lanes are unset.
     *
     * @param species mask species
     * @return a mask where all lanes are unset
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static Mask<Integer> maskAllFalse(IntSpecies species) {
        return VectorIntrinsics.broadcastCoerced((Class<Mask<Integer>>) species.maskType(), int.class, species.length(),
                                                 0, species, 
                                                 ((z, s) -> falseMask((IntSpecies)s)));
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
    public static Shuffle<Integer> shuffle(IntSpecies species, IntUnaryOperator f) {
        if (species.boxType() == IntMaxVector.class)
            return new IntMaxVector.IntMaxShuffle(f);
        switch (species.bitSize()) {
            case 64: return new Int64Vector.Int64Shuffle(f);
            case 128: return new Int128Vector.Int128Shuffle(f);
            case 256: return new Int256Vector.Int256Shuffle(f);
            case 512: return new Int512Vector.Int512Shuffle(f);
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
    public static Shuffle<Integer> shuffleIota(IntSpecies species) {
        if (species.boxType() == IntMaxVector.class)
            return new IntMaxVector.IntMaxShuffle(AbstractShuffle.IDENTITY);
        switch (species.bitSize()) {
            case 64: return new Int64Vector.Int64Shuffle(AbstractShuffle.IDENTITY);
            case 128: return new Int128Vector.Int128Shuffle(AbstractShuffle.IDENTITY);
            case 256: return new Int256Vector.Int256Shuffle(AbstractShuffle.IDENTITY);
            case 512: return new Int512Vector.Int512Shuffle(AbstractShuffle.IDENTITY);
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
    public static Shuffle<Integer> shuffleFromValues(IntSpecies species, int... ixs) {
        if (species.boxType() == IntMaxVector.class)
            return new IntMaxVector.IntMaxShuffle(ixs);
        switch (species.bitSize()) {
            case 64: return new Int64Vector.Int64Shuffle(ixs);
            case 128: return new Int128Vector.Int128Shuffle(ixs);
            case 256: return new Int256Vector.Int256Shuffle(ixs);
            case 512: return new Int512Vector.Int512Shuffle(ixs);
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
    public static Shuffle<Integer> shuffleFromArray(IntSpecies species, int[] ixs, int i) {
        if (species.boxType() == IntMaxVector.class)
            return new IntMaxVector.IntMaxShuffle(ixs, i);
        switch (species.bitSize()) {
            case 64: return new Int64Vector.Int64Shuffle(ixs, i);
            case 128: return new Int128Vector.Int128Shuffle(ixs, i);
            case 256: return new Int256Vector.Int256Shuffle(ixs, i);
            case 512: return new Int512Vector.Int512Shuffle(ixs, i);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }


    // Ops

    @Override
    public abstract IntVector add(Vector<Integer> v);

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
    public abstract IntVector add(int s);

    @Override
    public abstract IntVector add(Vector<Integer> v, Mask<Integer> m);

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
    public abstract IntVector add(int s, Mask<Integer> m);

    @Override
    public abstract IntVector sub(Vector<Integer> v);

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
    public abstract IntVector sub(int s);

    @Override
    public abstract IntVector sub(Vector<Integer> v, Mask<Integer> m);

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
    public abstract IntVector sub(int s, Mask<Integer> m);

    @Override
    public abstract IntVector mul(Vector<Integer> v);

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
    public abstract IntVector mul(int s);

    @Override
    public abstract IntVector mul(Vector<Integer> v, Mask<Integer> m);

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
    public abstract IntVector mul(int s, Mask<Integer> m);

    @Override
    public abstract IntVector neg();

    @Override
    public abstract IntVector neg(Mask<Integer> m);

    @Override
    public abstract IntVector abs();

    @Override
    public abstract IntVector abs(Mask<Integer> m);

    @Override
    public abstract IntVector min(Vector<Integer> v);

    @Override
    public abstract IntVector min(Vector<Integer> v, Mask<Integer> m);

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> Math.min(a, b)} is applied to lane elements.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract IntVector min(int s);

    @Override
    public abstract IntVector max(Vector<Integer> v);

    @Override
    public abstract IntVector max(Vector<Integer> v, Mask<Integer> m);

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> Math.max(a, b)} is applied to lane elements.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract IntVector max(int s);

    @Override
    public abstract Mask<Integer> equal(Vector<Integer> v);

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
    public abstract Mask<Integer> equal(int s);

    @Override
    public abstract Mask<Integer> notEqual(Vector<Integer> v);

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
    public abstract Mask<Integer> notEqual(int s);

    @Override
    public abstract Mask<Integer> lessThan(Vector<Integer> v);

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
    public abstract Mask<Integer> lessThan(int s);

    @Override
    public abstract Mask<Integer> lessThanEq(Vector<Integer> v);

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
    public abstract Mask<Integer> lessThanEq(int s);

    @Override
    public abstract Mask<Integer> greaterThan(Vector<Integer> v);

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
    public abstract Mask<Integer> greaterThan(int s);

    @Override
    public abstract Mask<Integer> greaterThanEq(Vector<Integer> v);

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
    public abstract Mask<Integer> greaterThanEq(int s);

    @Override
    public abstract IntVector blend(Vector<Integer> v, Mask<Integer> m);

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
    public abstract IntVector blend(int s, Mask<Integer> m);

    @Override
    public abstract IntVector rearrange(Vector<Integer> v,
                                                      Shuffle<Integer> s, Mask<Integer> m);

    @Override
    public abstract IntVector rearrange(Shuffle<Integer> m);

    @Override
    public abstract IntVector reshape(Species<Integer> s);

    @Override
    public abstract IntVector rotateEL(int i);

    @Override
    public abstract IntVector rotateER(int i);

    @Override
    public abstract IntVector shiftEL(int i);

    @Override
    public abstract IntVector shiftER(int i);



    /**
     * Bitwise ANDs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise AND
     * operation ({@code &}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise AND of this vector with the input vector
     */
    public abstract IntVector and(Vector<Integer> v);

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
    public abstract IntVector and(int s);

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
    public abstract IntVector and(Vector<Integer> v, Mask<Integer> m);

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
    public abstract IntVector and(int s, Mask<Integer> m);

    /**
     * Bitwise ORs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise OR
     * operation ({@code |}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise OR of this vector with the input vector
     */
    public abstract IntVector or(Vector<Integer> v);

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
    public abstract IntVector or(int s);

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
    public abstract IntVector or(Vector<Integer> v, Mask<Integer> m);

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
    public abstract IntVector or(int s, Mask<Integer> m);

    /**
     * Bitwise XORs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise XOR
     * operation ({@code ^}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise XOR of this vector with the input vector
     */
    public abstract IntVector xor(Vector<Integer> v);

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
    public abstract IntVector xor(int s);

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
    public abstract IntVector xor(Vector<Integer> v, Mask<Integer> m);

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
    public abstract IntVector xor(int s, Mask<Integer> m);

    /**
     * Bitwise NOTs this vector.
     * <p>
     * This is a vector unary operation where the primitive bitwise NOT
     * operation ({@code ~}) is applied to lane elements.
     *
     * @return the bitwise NOT of this vector
     */
    public abstract IntVector not();

    /**
     * Bitwise NOTs this vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a vector unary operation where the primitive bitwise NOT
     * operation ({@code ~}) is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the bitwise NOT of this vector
     */
    public abstract IntVector not(Mask<Integer> m);

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
    public abstract IntVector shiftL(int s);

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
    public abstract IntVector shiftL(int s, Mask<Integer> m);

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
    public abstract IntVector shiftL(Vector<Integer> v);

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
    public IntVector shiftL(Vector<Integer> v, Mask<Integer> m) {
        return bOp(v, m, (i, a, b) -> (int) (a << b));
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
    public abstract IntVector shiftR(int s);

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
    public abstract IntVector shiftR(int s, Mask<Integer> m);

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
    public abstract IntVector shiftR(Vector<Integer> v);

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
    public IntVector shiftR(Vector<Integer> v, Mask<Integer> m) {
        return bOp(v, m, (i, a, b) -> (int) (a >>> b));
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
    public abstract IntVector aShiftR(int s);

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
    public abstract IntVector aShiftR(int s, Mask<Integer> m);

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
    public abstract IntVector aShiftR(Vector<Integer> v);

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
    public IntVector aShiftR(Vector<Integer> v, Mask<Integer> m) {
        return bOp(v, m, (i, a, b) -> (int) (a >> b));
    }

    /**
     * Rotates left this vector by the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@link Integer#rotateLeft} is applied to lane elements and where
     * lane elements of this vector apply to the first argument, and lane
     * elements of the broadcast vector apply to the second argument (the
     * rotation distance).
     *
     * @param s the input scalar; the number of the bits to rotate left
     * @return the result of rotating left this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final IntVector rotateL(int s) {
        return shiftL(s).or(shiftR(-s));
    }

    /**
     * Rotates left this vector by the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the operation
     * {@link Integer#rotateLeft} is applied to lane elements and where
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
    public final IntVector rotateL(int s, Mask<Integer> m) {
        return shiftL(s, m).or(shiftR(-s, m), m);
    }

    /**
     * Rotates right this vector by the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@link Integer#rotateRight} is applied to lane elements and where
     * lane elements of this vector apply to the first argument, and lane
     * elements of the broadcast vector apply to the second argument (the
     * rotation distance).
     *
     * @param s the input scalar; the number of the bits to rotate right
     * @return the result of rotating right this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final IntVector rotateR(int s) {
        return shiftR(s).or(shiftL(-s));
    }

    /**
     * Rotates right this vector by the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the operation
     * {@link Integer#rotateRight} is applied to lane elements and where
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
    public final IntVector rotateR(int s, Mask<Integer> m) {
        return shiftR(s, m).or(shiftL(-s, m), m);
    }

    @Override
    public abstract void intoByteArray(byte[] a, int ix);

    @Override
    public abstract void intoByteArray(byte[] a, int ix, Mask<Integer> m);

    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix);

    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix, Mask<Integer> m);


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
    public abstract int addAll();

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
    public abstract int addAll(Mask<Integer> m);

    /**
     * Multiplies all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the
     * multiplication operation ({@code *}) is applied to lane elements,
     * and the identity value is {@code 1}.
     *
     * @return the multiplication of all the lane elements of this vector
     */
    public abstract int mulAll();

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
    public abstract int mulAll(Mask<Integer> m);

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.min(a, b)} is applied to lane elements,
     * and the identity value is
     * {@link Integer#MAX_VALUE}.
     *
     * @return the minimum lane element of this vector
     */
    public abstract int minAll();

    /**
     * Returns the minimum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.min(a, b)} is applied to lane elements,
     * and the identity value is
     * {@link Integer#MAX_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the minimum lane element of this vector
     */
    public abstract int minAll(Mask<Integer> m);

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.max(a, b)} is applied to lane elements,
     * and the identity value is
     * {@link Integer#MIN_VALUE}.
     *
     * @return the maximum lane element of this vector
     */
    public abstract int maxAll();

    /**
     * Returns the maximum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.max(a, b)} is applied to lane elements,
     * and the identity value is
     * {@link Integer#MIN_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the maximum lane element of this vector
     */
    public abstract int maxAll(Mask<Integer> m);

    /**
     * Logically ORs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical OR
     * operation ({@code |}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical OR all the lane elements of this vector
     */
    public abstract int orAll();

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
    public abstract int orAll(Mask<Integer> m);

    /**
     * Logically ANDs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical AND
     * operation ({@code |}) is applied to lane elements,
     * and the identity value is {@code -1}.
     *
     * @return the logical AND all the lane elements of this vector
     */
    public abstract int andAll();

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
    public abstract int andAll(Mask<Integer> m);

    /**
     * Logically XORs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical XOR
     * operation ({@code ^}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical XOR all the lane elements of this vector
     */
    public abstract int xorAll();

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
    public abstract int xorAll(Mask<Integer> m);

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract int get(int i);

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
     * {@code i + N}.
     *
     * @param a the array
     * @param i the offset into the array
     * @throws IndexOutOfBoundsException if {@code i < 0}, or
     * {@code i > a.length - this.length()}
     */
    public abstract void intoArray(int[] a, int i);

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
    public abstract void intoArray(int[] a, int i, Mask<Integer> m);

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
    public abstract void intoArray(int[] a, int i, int[] indexMap, int j);

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
    public abstract void intoArray(int[] a, int i, Mask<Integer> m, int[] indexMap, int j);
    // Species

    @Override
    public abstract IntSpecies species();

    /**
     * Class representing {@link IntVector}'s of the same {@link Vector.Shape Shape}.
     */
    public static abstract class IntSpecies extends Vector.Species<Integer> {
        interface FOp {
            int apply(int i);
        }

        abstract IntVector op(FOp f);

        abstract IntVector op(Mask<Integer> m, FOp f);

        interface FOpm {
            boolean apply(int i);
        }

        abstract Mask<Integer> opm(FOpm f);

        abstract IntVector.IntSpecies indexSpecies();


        // Factories

        @Override
        public abstract IntVector zero();

        /**
         * Returns a vector where all lane elements are set to the primitive
         * value {@code e}.
         *
         * @param e the value
         * @return a vector of vector where all lane elements are set to
         * the primitive value {@code e}
         */
        public abstract IntVector broadcast(int e);

        /**
         * Returns a vector where the first lane element is set to the primtive
         * value {@code e}, all other lane elements are set to the default
         * value.
         *
         * @param e the value
         * @return a vector where the first lane element is set to the primitive
         * value {@code e}
         */
        @ForceInline
        public final IntVector single(int e) {
            return zero().with(0, e);
        }

        /**
         * Returns a vector where each lane element is set to a randomly
         * generated primitive value.
         *
         * The semantics are equivalent to calling
         * {@code (int)ThreadLocalRandom#nextInt()}.
         *
         * @return a vector where each lane elements is set to a randomly
         * generated primitive value
         */
        public IntVector random() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            return op(i -> r.nextInt());
        }

        /**
         * Returns a vector where each lane element is set to a given
         * primitive value.
         * <p>
         * For each vector lane, where {@code N} is the vector lane index, the
         * the primitive value at index {@code N} is placed into the resulting
         * vector at lane index {@code N}.
         *
         * @param es the given primitive values
         * @return a vector where each lane element is set to a given primitive
         * value
         * @throws IndexOutOfBoundsException if {@code es.length < this.length()}
         */
        public abstract IntVector scalars(int... es);
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
    @SuppressWarnings("unchecked")
    public static IntSpecies preferredSpecies() {
        return (IntSpecies) Species.ofPreferred(int.class);
    }

    /**
     * Finds a species for an element type of {@code int} and shape.
     *
     * @param s the shape
     * @return a species for an element type of {@code int} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    @SuppressWarnings("unchecked")
    public static IntSpecies species(Vector.Shape s) {
        Objects.requireNonNull(s);
        switch (s) {
            case S_64_BIT: return Int64Vector.SPECIES;
            case S_128_BIT: return Int128Vector.SPECIES;
            case S_256_BIT: return Int256Vector.SPECIES;
            case S_512_BIT: return Int512Vector.SPECIES;
            case S_Max_BIT: return IntMaxVector.SPECIES;
            default: throw new IllegalArgumentException("Bad shape: " + s);
        }
    }
}
