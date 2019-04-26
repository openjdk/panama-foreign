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
 * {@code byte} values.
 */
@SuppressWarnings("cast")
public abstract class ByteVector extends Vector<Byte> {

    ByteVector() {}

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_BYTE_INDEX_SCALE);

    // Unary operator

    interface FUnOp {
        byte apply(int i, byte a);
    }

    abstract ByteVector uOp(FUnOp f);

    abstract ByteVector uOp(VectorMask<Byte> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        byte apply(int i, byte a, byte b);
    }

    abstract ByteVector bOp(Vector<Byte> v, FBinOp f);

    abstract ByteVector bOp(Vector<Byte> v, VectorMask<Byte> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        byte apply(int i, byte a, byte b, byte c);
    }

    abstract ByteVector tOp(Vector<Byte> v1, Vector<Byte> v2, FTriOp f);

    abstract ByteVector tOp(Vector<Byte> v1, Vector<Byte> v2, VectorMask<Byte> m, FTriOp f);

    // Reduction operator

    abstract byte rOp(byte v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, byte a, byte b);
    }

    abstract VectorMask<Byte> bTest(Vector<Byte> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, byte a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(VectorMask<Byte> m, FUnCon f);

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
    public static ByteVector zero(VectorSpecies<Byte> species) {
        return VectorIntrinsics.broadcastCoerced((Class<ByteVector>) species.vectorType(), byte.class, species.length(),
                                                 0, species,
                                                 ((bits, s) -> ((ByteSpecies)s).op(i -> (byte)bits)));
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
    public static ByteVector fromByteArray(VectorSpecies<Byte> species, byte[] a, int offset) {
        Objects.requireNonNull(a);
        offset = VectorIntrinsics.checkIndex(offset, a.length, species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<ByteVector>) species.vectorType(), byte.class, species.length(),
                                     a, ((long) offset) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                     a, offset, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
                                         ByteBuffer tb = bbc;
                                         return ((ByteSpecies)s).op(i -> tb.get());
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
    public static ByteVector fromByteArray(VectorSpecies<Byte> species, byte[] a, int offset, VectorMask<Byte> m) {
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
    public static ByteVector fromArray(VectorSpecies<Byte> species, byte[] a, int offset){
        Objects.requireNonNull(a);
        offset = VectorIntrinsics.checkIndex(offset, a.length, species.length());
        return VectorIntrinsics.load((Class<ByteVector>) species.vectorType(), byte.class, species.length(),
                                     a, (((long) offset) << ARRAY_SHIFT) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                     a, offset, species,
                                     (c, idx, s) -> ((ByteSpecies)s).op(n -> c[idx + n]));
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
    public static ByteVector fromArray(VectorSpecies<Byte> species, byte[] a, int offset, VectorMask<Byte> m) {
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
    public static ByteVector fromArray(VectorSpecies<Byte> species, byte[] a, int a_offset, int[] indexMap, int i_offset) {
        return ((ByteSpecies)species).op(n -> a[a_offset + indexMap[i_offset + n]]);
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
    public static ByteVector fromArray(VectorSpecies<Byte> species, byte[] a, int a_offset, VectorMask<Byte> m, int[] indexMap, int i_offset) {
        return ((ByteSpecies)species).op(m, n -> a[a_offset + indexMap[i_offset + n]]);
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
    public static ByteVector fromByteBuffer(VectorSpecies<Byte> species, ByteBuffer bb, int offset) {
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        offset = VectorIntrinsics.checkIndex(offset, bb.limit(), species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<ByteVector>) species.vectorType(), byte.class, species.length(),
                                     U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + offset,
                                     bb, offset, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                         ByteBuffer tb = bbc;
                                         return ((ByteSpecies)s).op(i -> tb.get());
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
    public static ByteVector fromByteBuffer(VectorSpecies<Byte> species, ByteBuffer bb, int offset, VectorMask<Byte> m) {
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
    public static ByteVector broadcast(VectorSpecies<Byte> species, byte e) {
        return VectorIntrinsics.broadcastCoerced(
            (Class<ByteVector>) species.vectorType(), byte.class, species.length(),
            e, species,
            ((bits, sp) -> ((ByteSpecies)sp).op(i -> (byte)bits)));
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
    public static ByteVector scalars(VectorSpecies<Byte> species, byte... es) {
        Objects.requireNonNull(es);
        int ix = VectorIntrinsics.checkIndex(0, es.length, species.length());
        return VectorIntrinsics.load((Class<ByteVector>) species.vectorType(), byte.class, species.length(),
                                     es, Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                     es, ix, species,
                                     (c, idx, sp) -> ((ByteSpecies)sp).op(n -> c[idx + n]));
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
    public static final ByteVector single(VectorSpecies<Byte> species, byte e) {
        return zero(species).with(0, e);
    }

    /**
     * Returns a vector where each lane element is set to a randomly
     * generated primitive value.
     *
     * The semantics are equivalent to calling
     * (byte){@link ThreadLocalRandom#nextInt()}
     *
     * @param species species of the desired vector
     * @return a vector where each lane elements is set to a randomly
     * generated primitive value
     */
    public static ByteVector random(VectorSpecies<Byte> species) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return ((ByteSpecies)species).op(i -> (byte) r.nextInt());
    }

    // Ops

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector add(Vector<Byte> v);

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
    public abstract ByteVector add(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector add(Vector<Byte> v, VectorMask<Byte> m);

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
    public abstract ByteVector add(byte s, VectorMask<Byte> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector sub(Vector<Byte> v);

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
    public abstract ByteVector sub(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector sub(Vector<Byte> v, VectorMask<Byte> m);

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
    public abstract ByteVector sub(byte s, VectorMask<Byte> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector mul(Vector<Byte> v);

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
    public abstract ByteVector mul(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector mul(Vector<Byte> v, VectorMask<Byte> m);

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
    public abstract ByteVector mul(byte s, VectorMask<Byte> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector neg();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector neg(VectorMask<Byte> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector abs();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector abs(VectorMask<Byte> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector min(Vector<Byte> v);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector min(Vector<Byte> v, VectorMask<Byte> m);

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to each lane.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract ByteVector min(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector max(Vector<Byte> v);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector max(Vector<Byte> v, VectorMask<Byte> m);

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to each lane.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract ByteVector max(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Byte> equal(Vector<Byte> v);

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
    public abstract VectorMask<Byte> equal(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Byte> notEqual(Vector<Byte> v);

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
    public abstract VectorMask<Byte> notEqual(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Byte> lessThan(Vector<Byte> v);

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
    public abstract VectorMask<Byte> lessThan(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Byte> lessThanEq(Vector<Byte> v);

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
    public abstract VectorMask<Byte> lessThanEq(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Byte> greaterThan(Vector<Byte> v);

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
    public abstract VectorMask<Byte> greaterThan(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Byte> greaterThanEq(Vector<Byte> v);

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
    public abstract VectorMask<Byte> greaterThanEq(byte s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector blend(Vector<Byte> v, VectorMask<Byte> m);

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
    public abstract ByteVector blend(byte s, VectorMask<Byte> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector rearrange(Vector<Byte> v,
                                                      VectorShuffle<Byte> s, VectorMask<Byte> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector rearrange(VectorShuffle<Byte> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector reshape(VectorSpecies<Byte> s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector rotateLanesLeft(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector rotateLanesRight(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector shiftLanesLeft(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ByteVector shiftLanesRight(int i);



    /**
     * Bitwise ANDs this vector with an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise AND
     * operation ({@code &}) to each lane.
     *
     * @param v the input vector
     * @return the bitwise AND of this vector with the input vector
     */
    public abstract ByteVector and(Vector<Byte> v);

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
    public abstract ByteVector and(byte s);

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
    public abstract ByteVector and(Vector<Byte> v, VectorMask<Byte> m);

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
    public abstract ByteVector and(byte s, VectorMask<Byte> m);

    /**
     * Bitwise ORs this vector with an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise OR
     * operation ({@code |}) to each lane.
     *
     * @param v the input vector
     * @return the bitwise OR of this vector with the input vector
     */
    public abstract ByteVector or(Vector<Byte> v);

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
    public abstract ByteVector or(byte s);

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
    public abstract ByteVector or(Vector<Byte> v, VectorMask<Byte> m);

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
    public abstract ByteVector or(byte s, VectorMask<Byte> m);

    /**
     * Bitwise XORs this vector with an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive bitwise XOR
     * operation ({@code ^}) to each lane.
     *
     * @param v the input vector
     * @return the bitwise XOR of this vector with the input vector
     */
    public abstract ByteVector xor(Vector<Byte> v);

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
    public abstract ByteVector xor(byte s);

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
    public abstract ByteVector xor(Vector<Byte> v, VectorMask<Byte> m);

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
    public abstract ByteVector xor(byte s, VectorMask<Byte> m);

    /**
     * Bitwise NOTs this vector.
     * <p>
     * This is a lane-wise unary operation which applies the primitive bitwise NOT
     * operation ({@code ~}) to each lane.
     *
     * @return the bitwise NOT of this vector
     */
    public abstract ByteVector not();

    /**
     * Bitwise NOTs this vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise unary operation which applies the primitive bitwise NOT
     * operation ({@code ~}) to each lane.
     *
     * @param m the mask controlling lane selection
     * @return the bitwise NOT of this vector
     */
    public abstract ByteVector not(VectorMask<Byte> m);

    /**
     * Logically left shifts this vector by the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane to left shift the
     * element by shift value as specified by the input scalar.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param s the input scalar; the number of the bits to left shift
     * @return the result of logically left shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract ByteVector shiftLeft(int s);

    /**
     * Logically left shifts this vector by the broadcast of an input scalar,
     * selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane to left shift the
     * element by shift value as specified by the input scalar.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param s the input scalar; the number of the bits to left shift
     * @param m the mask controlling lane selection
     * @return the result of logically left shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract ByteVector shiftLeft(int s, VectorMask<Byte> m);

    /**
     * Logically left shifts this vector by an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param v the input vector
     * @return the result of logically left shifting this vector by the input
     * vector
     */
    public abstract ByteVector shiftLeft(Vector<Byte> v);

    /**
     * Logically left shifts this vector by an input vector, selecting lane
     * elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical left shift
     * operation ({@code <<}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of logically left shifting this vector by the input
     * vector
     */
    public ByteVector shiftLeft(Vector<Byte> v, VectorMask<Byte> m) {
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
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @return the result of logically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract ByteVector shiftRight(int s);

     /**
     * Logically right shifts (or unsigned right shifts) this vector by the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>}) to each lane to logically right shift the
     * element by shift value as specified by the input scalar.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @param m the mask controlling lane selection
     * @return the result of logically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract ByteVector shiftRight(int s, VectorMask<Byte> m);

    /**
     * Logically right shifts (or unsigned right shifts) this vector by an
     * input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>>}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param v the input vector
     * @return the result of logically right shifting this vector by the
     * input vector
     */
    public abstract ByteVector shiftRight(Vector<Byte> v);

    /**
     * Logically right shifts (or unsigned right shifts) this vector by an
     * input vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive logical right shift
     * operation ({@code >>>}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift value
     * were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of logically right shifting this vector by the
     * input vector
     */
    public ByteVector shiftRight(Vector<Byte> v, VectorMask<Byte> m) {
        return blend(shiftRight(v), m);
    }

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by the
     * broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane to arithmetically
     * right shift the element by shift value as specified by the input scalar.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift
     * value were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @return the result of arithmetically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract ByteVector shiftArithmeticRight(int s);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane to arithmetically
     * right shift the element by shift value as specified by the input scalar.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift
     * value were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @param m the mask controlling lane selection
     * @return the result of arithmetically right shifting this vector by the
     * broadcast of an input scalar
     */
    public abstract ByteVector shiftArithmeticRight(int s, VectorMask<Byte> m);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by an
     * input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift
     * value were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param v the input vector
     * @return the result of arithmetically right shifting this vector by the
     * input vector
     */
    public abstract ByteVector shiftArithmeticRight(Vector<Byte> v);

    /**
     * Arithmetically right shifts (or signed right shifts) this vector by an
     * input vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive arithmetic right
     * shift operation ({@code >>}) to each lane. For each lane of this vector, the
     * shift value is the corresponding lane of input vector.
     * Only the 3 lowest-order bits of shift value are used. It is as if the shift
     * value were subjected to a bitwise logical AND operator ({@code &}) with the mask value 0x7.
     * The shift distance actually used is therefore always in the range 0 to 7, inclusive.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of arithmetically right shifting this vector by the
     * input vector
     */
    public ByteVector shiftArithmeticRight(Vector<Byte> v, VectorMask<Byte> m) {
        return blend(shiftArithmeticRight(v), m);
    }

    /**
     * Rotates left this vector by the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which produces the result of rotating left the two's
     * complement binary representation of each lane of first operand (this vector) by input scalar.
     * Rotation by any multiple of 8 is a no-op, so only the 3 lowest-order bits of input value are used.
     * It is as if the input value were subjected to a bitwise logical
     * AND operator ({@code &}) with the mask value 0x7.
     *
     * @param s the input scalar; the number of the bits to rotate left
     * @return the result of rotating left this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final ByteVector rotateLeft(int s) {
        return shiftLeft(s).or(shiftRight(-s));
    }

    /**
     * Rotates left this vector by the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which produces the result of rotating left the two's
     * complement binary representation of each lane of first operand (this vector) by input scalar.
     * Rotation by any multiple of 8 is a no-op, so only the 3 lowest-order bits of input value are used.
     * It is as if the input value were subjected to a bitwise logical
     * AND operator ({@code &}) with the mask value 0x7.
     *
     * @param s the input scalar; the number of the bits to rotate left
     * @param m the mask controlling lane selection
     * @return the result of rotating left this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final ByteVector rotateLeft(int s, VectorMask<Byte> m) {
        return shiftLeft(s, m).or(shiftRight(-s, m), m);
    }

    /**
     * Rotates right this vector by the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which produces the result of rotating right the two's
     * complement binary representation of each lane of first operand (this vector) by input scalar.
     * Rotation by any multiple of 8 is a no-op, so only the 3 lowest-order bits of input value are used.
     * It is as if the input value were subjected to a bitwise logical
     * AND operator ({@code &}) with the mask value 0x7.
     *
     * @param s the input scalar; the number of the bits to rotate right
     * @return the result of rotating right this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final ByteVector rotateRight(int s) {
        return shiftRight(s).or(shiftLeft(-s));
    }

    /**
     * Rotates right this vector by the broadcast of an input scalar, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which produces the result of rotating right the two's
     * complement binary representation of each lane of first operand (this vector) by input scalar.
     * Rotation by any multiple of 8 is a no-op, so only the 3 lowest-order bits of input value are used.
     * It is as if the input value were subjected to a bitwise logical
     * AND operator ({@code &}) with the mask value 0x7.
     *
     * @param s the input scalar; the number of the bits to rotate right
     * @param m the mask controlling lane selection
     * @return the result of rotating right this vector by the broadcast of an
     * input scalar
     */
    @ForceInline
    public final ByteVector rotateRight(int s, VectorMask<Byte> m) {
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
    public abstract void intoByteArray(byte[] a, int ix, VectorMask<Byte> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix, VectorMask<Byte> m);


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
    public abstract byte addLanes();

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
    public abstract byte addLanes(VectorMask<Byte> m);

    /**
     * Multiplies all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the
     * multiplication operation ({@code *}) to lane elements,
     * and the identity value is {@code 1}.
     *
     * @return the multiplication of all the lane elements of this vector
     */
    public abstract byte mulLanes();

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
    public abstract byte mulLanes(VectorMask<Byte> m);

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to lane elements,
     * and the identity value is
     * {@link Byte#MAX_VALUE}.
     *
     * @return the minimum lane element of this vector
     */
    public abstract byte minLanes();

    /**
     * Returns the minimum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to lane elements,
     * and the identity value is
     * {@link Byte#MAX_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the minimum lane element of this vector
     */
    public abstract byte minLanes(VectorMask<Byte> m);

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to lane elements,
     * and the identity value is
     * {@link Byte#MIN_VALUE}.
     *
     * @return the maximum lane element of this vector
     */
    public abstract byte maxLanes();

    /**
     * Returns the maximum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to lane elements,
     * and the identity value is
     * {@link Byte#MIN_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the maximum lane element of this vector
     */
    public abstract byte maxLanes(VectorMask<Byte> m);

    /**
     * Logically ORs all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical OR
     * operation ({@code |}) to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical OR all the lane elements of this vector
     */
    public abstract byte orLanes();

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
    public abstract byte orLanes(VectorMask<Byte> m);

    /**
     * Logically ANDs all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical AND
     * operation ({@code |}) to lane elements,
     * and the identity value is {@code -1}.
     *
     * @return the logical AND all the lane elements of this vector
     */
    public abstract byte andLanes();

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
    public abstract byte andLanes(VectorMask<Byte> m);

    /**
     * Logically XORs all lane elements of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the logical XOR
     * operation ({@code ^}) to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical XOR all the lane elements of this vector
     */
    public abstract byte xorLanes();

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
    public abstract byte xorLanes(VectorMask<Byte> m);

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract byte lane(int i);

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
    public abstract ByteVector with(int i, byte e);

    // Type specific extractors

    /**
     * Returns an array containing the lane elements of this vector.
     * <p>
     * This method behaves as if it {@link #intoArray(byte[], int)} stores}
     * this vector into an allocated array and returns the array as follows:
     * <pre>{@code
     *   byte[] a = new byte[this.length()];
     *   this.intoArray(a, 0);
     *   return a;
     * }</pre>
     *
     * @return an array containing the the lane elements of this vector
     */
    @ForceInline
    public final byte[] toArray() {
        byte[] a = new byte[species().length()];
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
    public abstract void intoArray(byte[] a, int offset);

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
    public abstract void intoArray(byte[] a, int offset, VectorMask<Byte> m);

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
    public void intoArray(byte[] a, int a_offset, int[] indexMap, int i_offset) {
        forEach((n, e) -> a[a_offset + indexMap[i_offset + n]] = e);
    }

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
    public void intoArray(byte[] a, int a_offset, VectorMask<Byte> m, int[] indexMap, int i_offset) {
        forEach(m, (n, e) -> a[a_offset + indexMap[i_offset + n]] = e);
    }
    // Species

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorSpecies<Byte> species();

    /**
     * Class representing {@link ByteVector}'s of the same {@link VectorShape VectorShape}.
     */
    static final class ByteSpecies extends AbstractSpecies<Byte> {
        final Function<byte[], ByteVector> vectorFactory;

        private ByteSpecies(VectorShape shape,
                          Class<?> vectorType,
                          Class<?> maskType,
                          Function<byte[], ByteVector> vectorFactory,
                          Function<boolean[], VectorMask<Byte>> maskFactory,
                          Function<IntUnaryOperator, VectorShuffle<Byte>> shuffleFromArrayFactory,
                          fShuffleFromArray<Byte> shuffleFromOpFactory) {
            super(shape, byte.class, Byte.SIZE, vectorType, maskType, maskFactory,
                  shuffleFromArrayFactory, shuffleFromOpFactory);
            this.vectorFactory = vectorFactory;
        }

        interface FOp {
            byte apply(int i);
        }

        ByteVector op(FOp f) {
            byte[] res = new byte[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return vectorFactory.apply(res);
        }

        ByteVector op(VectorMask<Byte> o, FOp f) {
            byte[] res = new byte[length()];
            boolean[] mbits = ((AbstractMask<Byte>)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return vectorFactory.apply(res);
        }
    }

    /**
     * Finds the preferred species for an element type of {@code byte}.
     * <p>
     * A preferred species is a species chosen by the platform that has a
     * shape of maximal bit size.  A preferred species for different element
     * types will have the same shape, and therefore vectors, masks, and
     * shuffles created from such species will be shape compatible.
     *
     * @return the preferred species for an element type of {@code byte}
     */
    private static ByteSpecies preferredSpecies() {
        return (ByteSpecies) VectorSpecies.ofPreferred(byte.class);
    }

    /**
     * Finds a species for an element type of {@code byte} and shape.
     *
     * @param s the shape
     * @return a species for an element type of {@code byte} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    static ByteSpecies species(VectorShape s) {
        Objects.requireNonNull(s);
        switch (s) {
            case S_64_BIT: return (ByteSpecies) SPECIES_64;
            case S_128_BIT: return (ByteSpecies) SPECIES_128;
            case S_256_BIT: return (ByteSpecies) SPECIES_256;
            case S_512_BIT: return (ByteSpecies) SPECIES_512;
            case S_Max_BIT: return (ByteSpecies) SPECIES_MAX;
            default: throw new IllegalArgumentException("Bad shape: " + s);
        }
    }

    /** Species representing {@link ByteVector}s of {@link VectorShape#S_64_BIT VectorShape.S_64_BIT}. */
    public static final VectorSpecies<Byte> SPECIES_64 = new ByteSpecies(VectorShape.S_64_BIT, Byte64Vector.class, Byte64Vector.Byte64Mask.class,
                                                                     Byte64Vector::new, Byte64Vector.Byte64Mask::new,
                                                                     Byte64Vector.Byte64Shuffle::new, Byte64Vector.Byte64Shuffle::new);

    /** Species representing {@link ByteVector}s of {@link VectorShape#S_128_BIT VectorShape.S_128_BIT}. */
    public static final VectorSpecies<Byte> SPECIES_128 = new ByteSpecies(VectorShape.S_128_BIT, Byte128Vector.class, Byte128Vector.Byte128Mask.class,
                                                                      Byte128Vector::new, Byte128Vector.Byte128Mask::new,
                                                                      Byte128Vector.Byte128Shuffle::new, Byte128Vector.Byte128Shuffle::new);

    /** Species representing {@link ByteVector}s of {@link VectorShape#S_256_BIT VectorShape.S_256_BIT}. */
    public static final VectorSpecies<Byte> SPECIES_256 = new ByteSpecies(VectorShape.S_256_BIT, Byte256Vector.class, Byte256Vector.Byte256Mask.class,
                                                                      Byte256Vector::new, Byte256Vector.Byte256Mask::new,
                                                                      Byte256Vector.Byte256Shuffle::new, Byte256Vector.Byte256Shuffle::new);

    /** Species representing {@link ByteVector}s of {@link VectorShape#S_512_BIT VectorShape.S_512_BIT}. */
    public static final VectorSpecies<Byte> SPECIES_512 = new ByteSpecies(VectorShape.S_512_BIT, Byte512Vector.class, Byte512Vector.Byte512Mask.class,
                                                                      Byte512Vector::new, Byte512Vector.Byte512Mask::new,
                                                                      Byte512Vector.Byte512Shuffle::new, Byte512Vector.Byte512Shuffle::new);

    /** Species representing {@link ByteVector}s of {@link VectorShape#S_Max_BIT VectorShape.S_Max_BIT}. */
    public static final VectorSpecies<Byte> SPECIES_MAX = new ByteSpecies(VectorShape.S_Max_BIT, ByteMaxVector.class, ByteMaxVector.ByteMaxMask.class,
                                                                      ByteMaxVector::new, ByteMaxVector.ByteMaxMask::new,
                                                                      ByteMaxVector.ByteMaxShuffle::new, ByteMaxVector.ByteMaxShuffle::new);

    /**
     * Preferred species for {@link ByteVector}s.
     * A preferred species is a species of maximal bit size for the platform.
     */
    public static final VectorSpecies<Byte> SPECIES_PREFERRED = (VectorSpecies<Byte>) preferredSpecies();
}
