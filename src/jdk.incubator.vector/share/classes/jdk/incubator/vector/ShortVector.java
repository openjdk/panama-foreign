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
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import java.util.concurrent.ThreadLocalRandom;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;


/**
 * A specialized {@link Vector} representing an ordered immutable sequence of
 * {@code short} values.
 */
@SuppressWarnings("cast")
public abstract class ShortVector extends Vector<Short> {

    ShortVector() {}

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_SHORT_INDEX_SCALE);

    // Unary operator

    interface FUnOp {
        short apply(int i, short a);
    }

    abstract ShortVector uOp(FUnOp f);

    abstract ShortVector uOp(Mask<Short> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        short apply(int i, short a, short b);
    }

    abstract ShortVector bOp(Vector<Short> v, FBinOp f);

    abstract ShortVector bOp(Vector<Short> v, Mask<Short> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        short apply(int i, short a, short b, short c);
    }

    abstract ShortVector tOp(Vector<Short> v1, Vector<Short> v2, FTriOp f);

    abstract ShortVector tOp(Vector<Short> v1, Vector<Short> v2, Mask<Short> m, FTriOp f);

    // Reduction operator

    abstract short rOp(short v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, short a, short b);
    }

    abstract Mask<Short> bTest(Vector<Short> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, short a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Short> m, FUnCon f);

    // Static factories

    /**
     * Returns a vector where all lane elements are set to the default
     * primitive value.
     *
     * @return a zero vector
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static ShortVector zero(ShortSpecies species) {
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
     * {@link #fromByteBuffer(ShortSpecies, ByteBuffer, int, Mask) method} as follows:
     * <pre>{@code
     * return this.fromByteBuffer(ByteBuffer.wrap(a), i, this.maskAllTrue());
     * }</pre>
     *
     * @param a the byte array
     * @param ix the offset into the array
     * @return a vector loaded from a byte array
     * @throws IndexOutOfBoundsException if {@code i < 0} or
     * {@code i > a.length - (this.length() * this.elementSize() / Byte.SIZE)}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static ShortVector fromByteArray(ShortSpecies species, byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<ShortVector>) species.boxType(), short.class, species.length(),
                                     a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                     a, ix, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
                                         ShortBuffer tb = bbc.asShortBuffer();
                                         return ((ShortSpecies)s).op(i -> tb.get());
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
     * {@link #fromByteBuffer(ShortSpecies, ByteBuffer, int, Mask) method} as follows:
     * <pre>{@code
     * return this.fromByteBuffer(ByteBuffer.wrap(a), i, m);
     * }</pre>
     *
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
    public static ShortVector fromByteArray(ShortSpecies species, byte[] a, int ix, Mask<Short> m) {
        return zero(species).blend(fromByteArray(species, a, ix), m);
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
    @ForceInline
    @SuppressWarnings("unchecked")
    public static ShortVector fromArray(ShortSpecies species, short[] a, int i){
        Objects.requireNonNull(a);
        i = VectorIntrinsics.checkIndex(i, a.length, species.length());
        return VectorIntrinsics.load((Class<ShortVector>) species.boxType(), short.class, species.length(),
                                     a, (((long) i) << ARRAY_SHIFT) + Unsafe.ARRAY_SHORT_BASE_OFFSET,
                                     a, i, species,
                                     (c, idx, s) -> ((ShortSpecies)s).op(n -> c[idx + n]));
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
    @ForceInline
    public static ShortVector fromArray(ShortSpecies species, short[] a, int i, Mask<Short> m) {
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
    public static ShortVector fromArray(ShortSpecies species, short[] a, int i, int[] indexMap, int j) {
        return species.op(n -> a[i + indexMap[j + n]]);
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
    public static ShortVector fromArray(ShortSpecies species, short[] a, int i, Mask<Short> m, int[] indexMap, int j) {
        return species.op(m, n -> a[i + indexMap[j + n]]);
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
     * {@link #fromByteBuffer(ShortSpecies, ByteBuffer, int, Mask)} method} as follows:
     * <pre>{@code
     *   return this.fromByteBuffer(b, i, this.maskAllTrue())
     * }</pre>
     *
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
    public static ShortVector fromByteBuffer(ShortSpecies species, ByteBuffer bb, int ix) {
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        ix = VectorIntrinsics.checkIndex(ix, bb.limit(), species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<ShortVector>) species.boxType(), short.class, species.length(),
                                     U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + ix,
                                     bb, ix, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                         ShortBuffer tb = bbc.asShortBuffer();
                                         return ((ShortSpecies)s).op(i -> tb.get());
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
     * @param bb the byte buffer
     * @param ix the offset into the byte buffer
     * @return a vector loaded from a byte buffer
     * @throws IndexOutOfBoundsException if the offset is {@code < 0},
     * or {@code > b.limit()},
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set
     * {@code i >= b.limit() - (N * this.elementSize() / Byte.SIZE)}
     */
    @ForceInline
    public static ShortVector fromByteBuffer(ShortSpecies species, ByteBuffer bb, int ix, Mask<Short> m) {
        return zero(species).blend(fromByteBuffer(species, bb, ix), m);
    }

    @ForceInline
    public static Mask<Short> maskFromValues(ShortSpecies species, boolean... bits) {
        if (species.boxType() == ShortMaxVector.class)
            return new ShortMaxVector.ShortMaxMask(bits);
        switch (species.bitSize()) {
            case 64: return new Short64Vector.Short64Mask(bits);
            case 128: return new Short128Vector.Short128Mask(bits);
            case 256: return new Short256Vector.Short256Mask(bits);
            case 512: return new Short512Vector.Short512Mask(bits);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    // @@@ This is a bad implementation -- makes lambdas capturing -- fix this
    static Mask<Short> trueMask(ShortSpecies species) {
        if (species.boxType() == ShortMaxVector.class)
            return ShortMaxVector.ShortMaxMask.TRUE_MASK;
        switch (species.bitSize()) {
            case 64: return Short64Vector.Short64Mask.TRUE_MASK;
            case 128: return Short128Vector.Short128Mask.TRUE_MASK;
            case 256: return Short256Vector.Short256Mask.TRUE_MASK;
            case 512: return Short512Vector.Short512Mask.TRUE_MASK;
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    static Mask<Short> falseMask(ShortSpecies species) {
        if (species.boxType() == ShortMaxVector.class)
            return ShortMaxVector.ShortMaxMask.FALSE_MASK;
        switch (species.bitSize()) {
            case 64: return Short64Vector.Short64Mask.FALSE_MASK;
            case 128: return Short128Vector.Short128Mask.FALSE_MASK;
            case 256: return Short256Vector.Short256Mask.FALSE_MASK;
            case 512: return Short512Vector.Short512Mask.FALSE_MASK;
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    @ForceInline
    @SuppressWarnings("unchecked")
    public static Mask<Short> maskFromArray(ShortSpecies species, boolean[] bits, int ix) {
        Objects.requireNonNull(bits);
        ix = VectorIntrinsics.checkIndex(ix, bits.length, species.length());
        return VectorIntrinsics.load((Class<Mask<Short>>) species.maskType(), short.class, species.length(),
                                     bits, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_BOOLEAN_BASE_OFFSET,
                                     bits, ix, species,
                                     (c, idx, s) -> (Mask<Short>) ((ShortSpecies)s).opm(n -> c[idx + n]));
    }

    @ForceInline
    @SuppressWarnings("unchecked")
    public static Mask<Short> maskAllTrue(ShortSpecies species) {
        return VectorIntrinsics.broadcastCoerced((Class<Mask<Short>>) species.maskType(), short.class, species.length(),
                                                 (short)-1,  species,
                                                 ((z, s) -> trueMask((ShortSpecies)s)));
    }

    @ForceInline
    @SuppressWarnings("unchecked")
    public static Mask<Short> maskAllFalse(ShortSpecies species) {
        return VectorIntrinsics.broadcastCoerced((Class<Mask<Short>>) species.maskType(), short.class, species.length(),
                                                 0, species, 
                                                 ((z, s) -> falseMask((ShortSpecies)s)));
    }

    @ForceInline
    public static Shuffle<Short> shuffle(ShortSpecies species, IntUnaryOperator f) {
        if (species.boxType() == ShortMaxVector.class)
            return new ShortMaxVector.ShortMaxShuffle(f);
        switch (species.bitSize()) {
            case 64: return new Short64Vector.Short64Shuffle(f);
            case 128: return new Short128Vector.Short128Shuffle(f);
            case 256: return new Short256Vector.Short256Shuffle(f);
            case 512: return new Short512Vector.Short512Shuffle(f);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    @ForceInline
    public static Shuffle<Short> shuffleIota(ShortSpecies species) {
        if (species.boxType() == ShortMaxVector.class)
            return new ShortMaxVector.ShortMaxShuffle(AbstractShuffle.IDENTITY);
        switch (species.bitSize()) {
            case 64: return new Short64Vector.Short64Shuffle(AbstractShuffle.IDENTITY);
            case 128: return new Short128Vector.Short128Shuffle(AbstractShuffle.IDENTITY);
            case 256: return new Short256Vector.Short256Shuffle(AbstractShuffle.IDENTITY);
            case 512: return new Short512Vector.Short512Shuffle(AbstractShuffle.IDENTITY);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    @ForceInline
    public static Shuffle<Short> shuffleFromValues(ShortSpecies species, int... ixs) {
        if (species.boxType() == ShortMaxVector.class)
            return new ShortMaxVector.ShortMaxShuffle(ixs);
        switch (species.bitSize()) {
            case 64: return new Short64Vector.Short64Shuffle(ixs);
            case 128: return new Short128Vector.Short128Shuffle(ixs);
            case 256: return new Short256Vector.Short256Shuffle(ixs);
            case 512: return new Short512Vector.Short512Shuffle(ixs);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }

    @ForceInline
    public static Shuffle<Short> shuffleFromArray(ShortSpecies species, int[] ixs, int i) {
        if (species.boxType() == ShortMaxVector.class)
            return new ShortMaxVector.ShortMaxShuffle(ixs, i);
        switch (species.bitSize()) {
            case 64: return new Short64Vector.Short64Shuffle(ixs, i);
            case 128: return new Short128Vector.Short128Shuffle(ixs, i);
            case 256: return new Short256Vector.Short256Shuffle(ixs, i);
            case 512: return new Short512Vector.Short512Shuffle(ixs, i);
            default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
        }
    }


    // Ops

    @Override
    public abstract ShortVector add(Vector<Short> v);

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
    public abstract ShortVector add(short s);

    @Override
    public abstract ShortVector add(Vector<Short> v, Mask<Short> m);

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
    public abstract ShortVector add(short s, Mask<Short> m);

    @Override
    public abstract ShortVector sub(Vector<Short> v);

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
    public abstract ShortVector sub(short s);

    @Override
    public abstract ShortVector sub(Vector<Short> v, Mask<Short> m);

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
    public abstract ShortVector sub(short s, Mask<Short> m);

    @Override
    public abstract ShortVector mul(Vector<Short> v);

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
    public abstract ShortVector mul(short s);

    @Override
    public abstract ShortVector mul(Vector<Short> v, Mask<Short> m);

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
    public abstract ShortVector mul(short s, Mask<Short> m);

    @Override
    public abstract ShortVector neg();

    @Override
    public abstract ShortVector neg(Mask<Short> m);

    @Override
    public abstract ShortVector abs();

    @Override
    public abstract ShortVector abs(Mask<Short> m);

    @Override
    public abstract ShortVector min(Vector<Short> v);

    @Override
    public abstract ShortVector min(Vector<Short> v, Mask<Short> m);

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> Math.min(a, b)} is applied to lane elements.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract ShortVector min(short s);

    @Override
    public abstract ShortVector max(Vector<Short> v);

    @Override
    public abstract ShortVector max(Vector<Short> v, Mask<Short> m);

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> Math.max(a, b)} is applied to lane elements.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract ShortVector max(short s);

    @Override
    public abstract Mask<Short> equal(Vector<Short> v);

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
    public abstract Mask<Short> equal(short s);

    @Override
    public abstract Mask<Short> notEqual(Vector<Short> v);

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
    public abstract Mask<Short> notEqual(short s);

    @Override
    public abstract Mask<Short> lessThan(Vector<Short> v);

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
    public abstract Mask<Short> lessThan(short s);

    @Override
    public abstract Mask<Short> lessThanEq(Vector<Short> v);

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
    public abstract Mask<Short> lessThanEq(short s);

    @Override
    public abstract Mask<Short> greaterThan(Vector<Short> v);

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
    public abstract Mask<Short> greaterThan(short s);

    @Override
    public abstract Mask<Short> greaterThanEq(Vector<Short> v);

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
    public abstract Mask<Short> greaterThanEq(short s);

    @Override
    public abstract ShortVector blend(Vector<Short> v, Mask<Short> m);

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
    public abstract ShortVector blend(short s, Mask<Short> m);

    @Override
    public abstract ShortVector rearrange(Vector<Short> v,
                                                      Shuffle<Short> s, Mask<Short> m);

    @Override
    public abstract ShortVector rearrange(Shuffle<Short> m);

    @Override
    public abstract ShortVector reshape(Species<Short> s);

    @Override
    public abstract ShortVector rotateEL(int i);

    @Override
    public abstract ShortVector rotateER(int i);

    @Override
    public abstract ShortVector shiftEL(int i);

    @Override
    public abstract ShortVector shiftER(int i);



    /**
     * Bitwise ANDs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise AND
     * operation ({@code &}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise AND of this vector with the input vector
     */
    public abstract ShortVector and(Vector<Short> v);

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
    public abstract ShortVector and(short s);

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
    public abstract ShortVector and(Vector<Short> v, Mask<Short> m);

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
    public abstract ShortVector and(short s, Mask<Short> m);

    /**
     * Bitwise ORs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise OR
     * operation ({@code |}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise OR of this vector with the input vector
     */
    public abstract ShortVector or(Vector<Short> v);

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
    public abstract ShortVector or(short s);

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
    public abstract ShortVector or(Vector<Short> v, Mask<Short> m);

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
    public abstract ShortVector or(short s, Mask<Short> m);

    /**
     * Bitwise XORs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise XOR
     * operation ({@code ^}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise XOR of this vector with the input vector
     */
    public abstract ShortVector xor(Vector<Short> v);

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
    public abstract ShortVector xor(short s);

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
    public abstract ShortVector xor(Vector<Short> v, Mask<Short> m);

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
    public abstract ShortVector xor(short s, Mask<Short> m);

    /**
     * Bitwise NOTs this vector.
     * <p>
     * This is a vector unary operation where the primitive bitwise NOT
     * operation ({@code ~}) is applied to lane elements.
     *
     * @return the bitwise NOT of this vector
     */
    public abstract ShortVector not();

    /**
     * Bitwise NOTs this vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a vector unary operation where the primitive bitwise NOT
     * operation ({@code ~}) is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the bitwise NOT of this vector
     */
    public abstract ShortVector not(Mask<Short> m);

    /**
     * Logically left shifts this vector by the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive logical left shift
     * operation ({@code <<}) is applied to lane elements to left shift the
     * element by shift value as specified by the input scalar. Only the 4
     * lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator & with the mask value 0xF.
     * The shift distance actually used is therefore always in the range 0 to 15, inclusive.
     *
     * @param s the input scalar; the number of the bits to left shift
     * @return the result of logically left shifting left this vector by the
     * broadcast of an input scalar
     */
    public abstract ShortVector shiftL(int s);

    /**
     * Logically left shifts this vector by the broadcast of an input scalar,
     * selecting lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the primitive logical left shift
     * operation ({@code <<}) is applied to lane elements to left shift the
     * element by shift value as specified by the input scalar. Only the 4
     * lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator & with the mask value 0xF.
     * The shift distance actually used is therefore always in the range 0 to 15, inclusive.
     *
     * @param s the input scalar; the number of the bits to left shift
     * @param m the mask controlling lane selection
     * @return the result of logically left shifting left this vector by the
     * broadcast of an input scalar
     */
    public abstract ShortVector shiftL(int s, Mask<Short> m);


    // logical, or unsigned, shift right

     /**
     * Logically right shifts (or unsigned right shifts) this vector by the
     * broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive logical right shift
     * operation ({@code >>>}) is applied to lane elements to logically right shift the
     * element by shift value as specified by the input scalar. Only the 4
     * lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator & with the mask value 0xF.
     * The shift distance actually used is therefore always in the range 0 to 15, inclusive.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @return the result of logically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract ShortVector shiftR(int s);

     /**
     * Logically right shifts (or unsigned right shifts) this vector by the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * <p>
     * This is a vector binary operation where the primitive logical right shift
     * operation ({@code >>>}) is applied to lane elements to logically right shift the
     * element by shift value as specified by the input scalar. Only the 4
     * lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator & with the mask value 0xF.
     * The shift distance actually used is therefore always in the range 0 to 15, inclusive.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @return the result of logically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract ShortVector shiftR(int s, Mask<Short> m);


    /**
     * Arithmetically right shifts (or signed right shifts) this vector by the
     * broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive arithmetic right
     * shift operation ({@code >>}) is applied to lane elements  to arithmetically
     * right shift the element by shift value as specified by the input scalar.
     * Only the 4 lowest-order bits of shift value are used. It is as if the shift
     * value were subjected to a bitwise logical AND operator & with the mask value 0xF.
     * The shift distance actually used is therefore always in the range 0 to 15, inclusive.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @return the result of arithmetically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract ShortVector aShiftR(int s);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * <p>
     * This is a vector binary operation where the primitive arithmetic right
     * shift operation ({@code >>}) is applied to lane elements  to arithmetically
     * right shift the element by shift value as specified by the input scalar.
     * Only the 4 lowest-order bits of shift value are used. It is as if the shift
     * value were subjected to a bitwise logical AND operator & with the mask value 0xF.
     * The shift distance actually used is therefore always in the range 0 to 15, inclusive.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @param m the mask controlling lane selection
     * @return the result of arithmetically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract ShortVector aShiftR(int s, Mask<Short> m);


    @Override
    public abstract void intoByteArray(byte[] a, int ix);

    @Override
    public abstract void intoByteArray(byte[] a, int ix, Mask<Short> m);

    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix);

    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix, Mask<Short> m);


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
    public abstract short addAll();

    /**
     * Adds all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the addition
     * operation ({@code +}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @param m the mask controlling lane selection
     * @return the addition of all the lane elements of this vector
     */
    public abstract short addAll(Mask<Short> m);

    /**
     * Subtracts all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the subtraction
     * operation ({@code -}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the subtraction of all the lane elements of this vector
     */
    public abstract short subAll();

    /**
     * Subtracts all lane elements of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the subtraction
     * operation ({@code -}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @param m the mask controlling lane selection
     * @return the subtraction of all the lane elements of this vector
     */
    public abstract short subAll(Mask<Short> m);

    /**
     * Multiplies all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the
     * multiplication operation ({@code *}) is applied to lane elements,
     * and the identity value is {@code 1}.
     *
     * @return the multiplication of all the lane elements of this vector
     */
    public abstract short mulAll();

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
    public abstract short mulAll(Mask<Short> m);

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.min(a, b)} is applied to lane elements,
     * and the identity value is {@link Short#MAX_VALUE}.
     *
     * @return the minimum lane element of this vector
     */
    public abstract short minAll();

    /**
     * Returns the minimum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.min(a, b)} is applied to lane elements,
     * and the identity value is {@link Short#MAX_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the minimum lane element of this vector
     */
    public abstract short minAll(Mask<Short> m);

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.max(a, b)} is applied to lane elements,
     * and the identity value is {@link Short#MIN_VALUE}.
     *
     * @return the maximum lane element of this vector
     */
    public abstract short maxAll();

    /**
     * Returns the maximum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> Math.max(a, b)} is applied to lane elements,
     * and the identity value is {@link Short#MIN_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the maximum lane element of this vector
     */
    public abstract short maxAll(Mask<Short> m);

    /**
     * Logically ORs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical OR
     * operation ({@code |}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical OR all the lane elements of this vector
     */
    public abstract short orAll();

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
    public abstract short orAll(Mask<Short> m);

    /**
     * Logically ANDs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical AND
     * operation ({@code |}) is applied to lane elements,
     * and the identity value is {@code -1}.
     *
     * @return the logical AND all the lane elements of this vector
     */
    public abstract short andAll();

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
    public abstract short andAll(Mask<Short> m);

    /**
     * Logically XORs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical XOR
     * operation ({@code ^}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical XOR all the lane elements of this vector
     */
    public abstract short xorAll();

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
    public abstract short xorAll(Mask<Short> m);

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract short get(int i);

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
    public abstract ShortVector with(int i, short e);

    // Type specific extractors

    /**
     * Returns an array containing the lane elements of this vector.
     * <p>
     * This method behaves as if it {@link #intoArray(short[], int)} stores}
     * this vector into an allocated array and returns the array as follows:
     * <pre>{@code
     *   short[] a = new short[this.length()];
     *   this.intoArray(a, 0);
     *   return a;
     * }</pre>
     *
     * @return an array containing the the lane elements of this vector
     */
    @ForceInline
    public final short[] toArray() {
        short[] a = new short[species().length()];
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
    public abstract void intoArray(short[] a, int i);

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
    public abstract void intoArray(short[] a, int i, Mask<Short> m);

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
    public void intoArray(short[] a, int i, int[] indexMap, int j) {
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
    public void intoArray(short[] a, int i, Mask<Short> m, int[] indexMap, int j) {
        forEach(m, (n, e) -> a[i + indexMap[j + n]] = e);
    }
    // Species

    @Override
    public abstract ShortSpecies species();

    /**
     * A specialized factory for creating {@link ShortVector} value of the same
     * shape, and a {@link Mask} and {@link Shuffle} values of the same shape
     * and {@code int} element type.
     */
    public static abstract class ShortSpecies extends Vector.Species<Short> {
        interface FOp {
            short apply(int i);
        }

        abstract ShortVector op(FOp f);

        abstract ShortVector op(Mask<Short> m, FOp f);

        interface FOpm {
            boolean apply(int i);
        }

        abstract Mask<Short> opm(FOpm f);



        // Factories

        @Override
        public abstract ShortVector zero();

        /**
         * Returns a vector where all lane elements are set to the primitive
         * value {@code e}.
         *
         * @param e the value
         * @return a vector of vector where all lane elements are set to
         * the primitive value {@code e}
         */
        public abstract ShortVector broadcast(short e);

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
        public final ShortVector single(short e) {
            return zero().with(0, e);
        }

        /**
         * Returns a vector where each lane element is set to a randomly
         * generated primitive value.
         *
         * The semantics are equivalent to calling
         * {@link (short)ThreadLocalRandom#nextInt() }
         *
         * @return a vector where each lane elements is set to a randomly
         * generated primitive value
         */
        public ShortVector random() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            return op(i -> (short) r.nextInt());
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
        public abstract ShortVector scalars(short... es);
    }

    /**
     * Finds the preferred species for an element type of {@code short}.
     * <p>
     * A preferred species is a species chosen by the platform that has a
     * shape of maximal bit size.  A preferred species for different element
     * types will have the same shape, and therefore vectors, masks, and
     * shuffles created from such species will be shape compatible.
     *
     * @return the preferred species for an element type of {@code short}
     */
    @SuppressWarnings("unchecked")
    public static ShortSpecies preferredSpecies() {
        return (ShortSpecies) Species.ofPreferred(short.class);
    }

    /**
     * Finds a species for an element type of {@code short} and shape.
     *
     * @param s the shape
     * @return a species for an element type of {@code short} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    @SuppressWarnings("unchecked")
    public static ShortSpecies species(Vector.Shape s) {
        Objects.requireNonNull(s);
        switch (s) {
            case S_64_BIT: return Short64Vector.SPECIES;
            case S_128_BIT: return Short128Vector.SPECIES;
            case S_256_BIT: return Short256Vector.SPECIES;
            case S_512_BIT: return Short512Vector.SPECIES;
            case S_Max_BIT: return ShortMaxVector.SPECIES;
            default: throw new IllegalArgumentException("Bad shape: " + s);
        }
    }
}
