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
import java.nio.FloatBuffer;
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
 * {@code float} values.
 */
@SuppressWarnings("cast")
public abstract class FloatVector extends Vector<Float> {

    FloatVector() {}

    private static final int ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_FLOAT_INDEX_SCALE);

    // Unary operator

    interface FUnOp {
        float apply(int i, float a);
    }

    abstract FloatVector uOp(FUnOp f);

    abstract FloatVector uOp(VectorMask<Float> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        float apply(int i, float a, float b);
    }

    abstract FloatVector bOp(Vector<Float> v, FBinOp f);

    abstract FloatVector bOp(Vector<Float> v, VectorMask<Float> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        float apply(int i, float a, float b, float c);
    }

    abstract FloatVector tOp(Vector<Float> v1, Vector<Float> v2, FTriOp f);

    abstract FloatVector tOp(Vector<Float> v1, Vector<Float> v2, VectorMask<Float> m, FTriOp f);

    // Reduction operator

    abstract float rOp(float v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, float a, float b);
    }

    abstract VectorMask<Float> bTest(Vector<Float> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, float a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(VectorMask<Float> m, FUnCon f);

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
    public static FloatVector zero(VectorSpecies<Float> species) {
        return VectorIntrinsics.broadcastCoerced((Class<FloatVector>) species.boxType(), float.class, species.length(),
                                                 Float.floatToIntBits(0.0f), species,
                                                 ((bits, s) -> ((FloatSpecies)s).op(i -> Float.intBitsToFloat((int)bits))));
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
    public static FloatVector fromByteArray(VectorSpecies<Float> species, byte[] a, int offset) {
        Objects.requireNonNull(a);
        offset = VectorIntrinsics.checkIndex(offset, a.length, species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<FloatVector>) species.boxType(), float.class, species.length(),
                                     a, ((long) offset) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                                     a, offset, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = ByteBuffer.wrap(c, idx, a.length - idx).order(ByteOrder.nativeOrder());
                                         FloatBuffer tb = bbc.asFloatBuffer();
                                         return ((FloatSpecies)s).op(i -> tb.get());
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
    public static FloatVector fromByteArray(VectorSpecies<Float> species, byte[] a, int offset, VectorMask<Float> m) {
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
    public static FloatVector fromArray(VectorSpecies<Float> species, float[] a, int offset){
        Objects.requireNonNull(a);
        offset = VectorIntrinsics.checkIndex(offset, a.length, species.length());
        return VectorIntrinsics.load((Class<FloatVector>) species.boxType(), float.class, species.length(),
                                     a, (((long) offset) << ARRAY_SHIFT) + Unsafe.ARRAY_FLOAT_BASE_OFFSET,
                                     a, offset, species,
                                     (c, idx, s) -> ((FloatSpecies)s).op(n -> c[idx + n]));
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
    public static FloatVector fromArray(VectorSpecies<Float> species, float[] a, int offset, VectorMask<Float> m) {
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
    public static FloatVector fromArray(VectorSpecies<Float> species, float[] a, int a_offset, int[] indexMap, int i_offset) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(indexMap);


        // Index vector: vix[0:n] = k -> a_offset + indexMap[i_offset + k]
        IntVector vix = IntVector.fromArray(IntVector.species(species.indexShape()), indexMap, i_offset).add(a_offset);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        return VectorIntrinsics.loadWithMap((Class<FloatVector>) species.boxType(), float.class, species.length(),
                                            IntVector.species(species.indexShape()).boxType(), a, Unsafe.ARRAY_FLOAT_BASE_OFFSET, vix,
                                            a, a_offset, indexMap, i_offset, species,
                                            (float[] c, int idx, int[] iMap, int idy, VectorSpecies<Float> s) ->
                                                ((FloatSpecies)s).op(n -> c[idx + iMap[idy+n]]));
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
    public static FloatVector fromArray(VectorSpecies<Float> species, float[] a, int a_offset, VectorMask<Float> m, int[] indexMap, int i_offset) {
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
    public static FloatVector fromByteBuffer(VectorSpecies<Float> species, ByteBuffer bb, int offset) {
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        offset = VectorIntrinsics.checkIndex(offset, bb.limit(), species.bitSize() / Byte.SIZE);
        return VectorIntrinsics.load((Class<FloatVector>) species.boxType(), float.class, species.length(),
                                     U.getReference(bb, BYTE_BUFFER_HB), U.getLong(bb, BUFFER_ADDRESS) + offset,
                                     bb, offset, species,
                                     (c, idx, s) -> {
                                         ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                         FloatBuffer tb = bbc.asFloatBuffer();
                                         return ((FloatSpecies)s).op(i -> tb.get());
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
    public static FloatVector fromByteBuffer(VectorSpecies<Float> species, ByteBuffer bb, int offset, VectorMask<Float> m) {
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
    public static FloatVector broadcast(VectorSpecies<Float> species, float e) {
        return VectorIntrinsics.broadcastCoerced(
            (Class<FloatVector>) species.boxType(), float.class, species.length(),
            Float.floatToIntBits(e), species,
            ((bits, sp) -> ((FloatSpecies)sp).op(i -> Float.intBitsToFloat((int)bits))));
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
    public static FloatVector scalars(VectorSpecies<Float> species, float... es) {
        Objects.requireNonNull(es);
        int ix = VectorIntrinsics.checkIndex(0, es.length, species.length());
        return VectorIntrinsics.load((Class<FloatVector>) species.boxType(), float.class, species.length(),
                                     es, Unsafe.ARRAY_FLOAT_BASE_OFFSET,
                                     es, ix, species,
                                     (c, idx, sp) -> ((FloatSpecies)sp).op(n -> c[idx + n]));
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
    public static final FloatVector single(VectorSpecies<Float> species, float e) {
        return zero(species).with(0, e);
    }

    /**
     * Returns a vector where each lane element is set to a randomly
     * generated primitive value.
     *
     * The semantics are equivalent to calling
     * {@link ThreadLocalRandom#nextFloat()}
     *
     * @param species species of the desired vector
     * @return a vector where each lane elements is set to a randomly
     * generated primitive value
     */
    public static FloatVector random(VectorSpecies<Float> species) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return ((FloatSpecies)species).op(i -> r.nextFloat());
    }

    // Ops

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector add(Vector<Float> v);

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
    public abstract FloatVector add(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector add(Vector<Float> v, VectorMask<Float> m);

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
    public abstract FloatVector add(float s, VectorMask<Float> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector sub(Vector<Float> v);

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
    public abstract FloatVector sub(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector sub(Vector<Float> v, VectorMask<Float> m);

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
    public abstract FloatVector sub(float s, VectorMask<Float> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector mul(Vector<Float> v);

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
    public abstract FloatVector mul(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector mul(Vector<Float> v, VectorMask<Float> m);

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
    public abstract FloatVector mul(float s, VectorMask<Float> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector neg();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector neg(VectorMask<Float> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector abs();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector abs(VectorMask<Float> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector min(Vector<Float> v);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector min(Vector<Float> v, VectorMask<Float> m);

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to each lane.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract FloatVector min(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector max(Vector<Float> v);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector max(Vector<Float> v, VectorMask<Float> m);

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to each lane.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract FloatVector max(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Float> equal(Vector<Float> v);

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
    public abstract VectorMask<Float> equal(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Float> notEqual(Vector<Float> v);

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
    public abstract VectorMask<Float> notEqual(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Float> lessThan(Vector<Float> v);

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
    public abstract VectorMask<Float> lessThan(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Float> lessThanEq(Vector<Float> v);

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
    public abstract VectorMask<Float> lessThanEq(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Float> greaterThan(Vector<Float> v);

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
    public abstract VectorMask<Float> greaterThan(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorMask<Float> greaterThanEq(Vector<Float> v);

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
    public abstract VectorMask<Float> greaterThanEq(float s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector blend(Vector<Float> v, VectorMask<Float> m);

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
    public abstract FloatVector blend(float s, VectorMask<Float> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector rearrange(Vector<Float> v,
                                                      VectorShuffle<Float> s, VectorMask<Float> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector rearrange(VectorShuffle<Float> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector reshape(VectorSpecies<Float> s);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector rotateEL(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector rotateER(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector shiftEL(int i);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract FloatVector shiftER(int i);

    /**
     * Divides this vector by an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive division
     * operation ({@code /}) to each lane.
     *
     * @param v the input vector
     * @return the result of dividing this vector by the input vector
     */
    public abstract FloatVector div(Vector<Float> v);

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
    public abstract FloatVector div(float s);

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
    public abstract FloatVector div(Vector<Float> v, VectorMask<Float> m);

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
    public abstract FloatVector div(float s, VectorMask<Float> m);

    /**
     * Calculates the square root of this vector.
     * <p>
     * This is a lane-wise unary operation which applies the {@link Math#sqrt} operation
     * to each lane.
     *
     * @return the square root of this vector
     */
    public abstract FloatVector sqrt();

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
    public FloatVector sqrt(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.sqrt((double) a));
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
    public FloatVector tan() {
        return uOp((i, a) -> (float) Math.tan((double) a));
    }

    /**
     * Calculates the trigonometric tangent of this vector, selecting lane
     * elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#tan}
     *
     * @param m the mask controlling lane selection
     * @return the tangent of this vector
     */
    public FloatVector tan(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.tan((double) a));
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
    public FloatVector tanh() {
        return uOp((i, a) -> (float) Math.tanh((double) a));
    }

    /**
     * Calculates the hyperbolic tangent of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#tanh}
     *
     * @param m the mask controlling lane selection
     * @return the hyperbolic tangent of this vector
     */
    public FloatVector tanh(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.tanh((double) a));
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
    public FloatVector sin() {
        return uOp((i, a) -> (float) Math.sin((double) a));
    }

    /**
     * Calculates the trigonometric sine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#sin}
     *
     * @param m the mask controlling lane selection
     * @return the sine of this vector
     */
    public FloatVector sin(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.sin((double) a));
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
    public FloatVector sinh() {
        return uOp((i, a) -> (float) Math.sinh((double) a));
    }

    /**
     * Calculates the hyperbolic sine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#sinh}
     *
     * @param m the mask controlling lane selection
     * @return the hyperbolic sine of this vector
     */
    public FloatVector sinh(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.sinh((double) a));
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
    public FloatVector cos() {
        return uOp((i, a) -> (float) Math.cos((double) a));
    }

    /**
     * Calculates the trigonometric cosine of this vector, selecting lane
     * elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#cos}
     *
     * @param m the mask controlling lane selection
     * @return the cosine of this vector
     */
    public FloatVector cos(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.cos((double) a));
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
    public FloatVector cosh() {
        return uOp((i, a) -> (float) Math.cosh((double) a));
    }

    /**
     * Calculates the hyperbolic cosine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#cosh}
     *
     * @param m the mask controlling lane selection
     * @return the hyperbolic cosine of this vector
     */
    public FloatVector cosh(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.cosh((double) a));
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
    public FloatVector asin() {
        return uOp((i, a) -> (float) Math.asin((double) a));
    }

    /**
     * Calculates the arc sine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#asin}
     *
     * @param m the mask controlling lane selection
     * @return the arc sine of this vector
     */
    public FloatVector asin(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.asin((double) a));
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
    public FloatVector acos() {
        return uOp((i, a) -> (float) Math.acos((double) a));
    }

    /**
     * Calculates the arc cosine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#acos}
     *
     * @param m the mask controlling lane selection
     * @return the arc cosine of this vector
     */
    public FloatVector acos(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.acos((double) a));
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
    public FloatVector atan() {
        return uOp((i, a) -> (float) Math.atan((double) a));
    }

    /**
     * Calculates the arc tangent of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#atan}
     *
     * @param m the mask controlling lane selection
     * @return the arc tangent of this vector
     */
    public FloatVector atan(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.atan((double) a));
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
    public FloatVector atan2(Vector<Float> v) {
        return bOp(v, (i, a, b) -> (float) Math.atan2((double) a, (double) b));
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
    public abstract FloatVector atan2(float s);

    /**
     * Calculates the arc tangent of this vector divided by an input vector,
     * selecting lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#atan2}
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the arc tangent of this vector divided by the input vector
     */
    public FloatVector atan2(Vector<Float> v, VectorMask<Float> m) {
        return bOp(v, m, (i, a, b) -> (float) Math.atan2((double) a, (double) b));
    }

    /**
     * Calculates the arc tangent of this vector divided by the broadcast of an
     * an input scalar, selecting lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#atan2}
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the arc tangent of this vector over the input vector
     */
    public abstract FloatVector atan2(float s, VectorMask<Float> m);

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
    public FloatVector cbrt() {
        return uOp((i, a) -> (float) Math.cbrt((double) a));
    }

    /**
     * Calculates the cube root of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#cbrt}
     *
     * @param m the mask controlling lane selection
     * @return the cube root of this vector
     */
    public FloatVector cbrt(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.cbrt((double) a));
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
    public FloatVector log() {
        return uOp((i, a) -> (float) Math.log((double) a));
    }

    /**
     * Calculates the natural logarithm of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#log}
     *
     * @param m the mask controlling lane selection
     * @return the natural logarithm of this vector
     */
    public FloatVector log(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.log((double) a));
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
    public FloatVector log10() {
        return uOp((i, a) -> (float) Math.log10((double) a));
    }

    /**
     * Calculates the base 10 logarithm of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#log10}
     *
     * @param m the mask controlling lane selection
     * @return the base 10 logarithm of this vector
     */
    public FloatVector log10(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.log10((double) a));
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
    public FloatVector log1p() {
        return uOp((i, a) -> (float) Math.log1p((double) a));
    }

    /**
     * Calculates the natural logarithm of the sum of this vector and the
     * broadcast of {@code 1}, selecting lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#log1p}
     *
     * @param m the mask controlling lane selection
     * @return the natural logarithm of the sum of this vector and the broadcast
     * of {@code 1}
     */
    public FloatVector log1p(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.log1p((double) a));
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
    public FloatVector pow(Vector<Float> v) {
        return bOp(v, (i, a, b) -> (float) Math.pow((double) a, (double) b));
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
    public abstract FloatVector pow(float s);

    /**
     * Calculates this vector raised to the power of an input vector, selecting
     * lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#pow}
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return this vector raised to the power of an input vector
     */
    public FloatVector pow(Vector<Float> v, VectorMask<Float> m) {
        return bOp(v, m, (i, a, b) -> (float) Math.pow((double) a, (double) b));
    }

    /**
     * Calculates this vector raised to the power of the broadcast of an input
     * scalar, selecting lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#pow}
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return this vector raised to the power of the broadcast of an input
     * scalar.
     */
    public abstract FloatVector pow(float s, VectorMask<Float> m);

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
    public FloatVector exp() {
        return uOp((i, a) -> (float) Math.exp((double) a));
    }

    /**
     * Calculates the broadcast of Euler's number {@code e} raised to the power
     * of this vector, selecting lane elements controlled by a mask.
     * <p>
     * Semantics for rounding, monotonicity, and special cases are
     * described in {@link FloatVector#exp}
     *
     * @param m the mask controlling lane selection
     * @return the broadcast of Euler's number {@code e} raised to the power of
     * this vector
     */
    public FloatVector exp(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.exp((double) a));
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
    public FloatVector expm1() {
        return uOp((i, a) -> (float) Math.expm1((double) a));
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
     * described in {@link FloatVector#expm1}
     *
     * @param m the mask controlling lane selection
     * @return the broadcast of Euler's number {@code e} raised to the power of
     * this vector minus the broadcast of {@code -1}
     */
    public FloatVector expm1(VectorMask<Float> m) {
        return uOp(m, (i, a) -> (float) Math.expm1((double) a));
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
    public abstract FloatVector fma(Vector<Float> v1, Vector<Float> v2);

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
    public abstract FloatVector fma(float s1, float s2);

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
    public FloatVector fma(Vector<Float> v1, Vector<Float> v2, VectorMask<Float> m) {
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
    public abstract FloatVector fma(float s1, float s2, VectorMask<Float> m);

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
    public FloatVector hypot(Vector<Float> v) {
        return bOp(v, (i, a, b) -> (float) Math.hypot((double) a, (double) b));
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
    public abstract FloatVector hypot(float s);

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
     * described in {@link FloatVector#hypot}
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return square root of the sum of the squares of this vector and an input
     * vector
     */
    public FloatVector hypot(Vector<Float> v, VectorMask<Float> m) {
        return bOp(v, m, (i, a, b) -> (float) Math.hypot((double) a, (double) b));
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
     * described in {@link FloatVector#hypot}
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return square root of the sum of the squares of this vector and the
     * broadcast of an input scalar
     */
    public abstract FloatVector hypot(float s, VectorMask<Float> m);


    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteArray(byte[] a, int ix);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteArray(byte[] a, int ix, VectorMask<Float> m);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void intoByteBuffer(ByteBuffer bb, int ix, VectorMask<Float> m);


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
    public abstract float addAll();

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
    public abstract float addAll(VectorMask<Float> m);

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
    public abstract float mulAll();

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
    public abstract float mulAll(VectorMask<Float> m);

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to lane elements,
     * and the identity value is
     * {@link Float#POSITIVE_INFINITY}.
     *
     * @return the minimum lane element of this vector
     */
    public abstract float minAll();

    /**
     * Returns the minimum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.min(a, b)} to lane elements,
     * and the identity value is
     * {@link Float#POSITIVE_INFINITY}.
     *
     * @param m the mask controlling lane selection
     * @return the minimum lane element of this vector
     */
    public abstract float minAll(VectorMask<Float> m);

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to lane elements,
     * and the identity value is
     * {@link Float#NEGATIVE_INFINITY}.
     *
     * @return the maximum lane element of this vector
     */
    public abstract float maxAll();

    /**
     * Returns the maximum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which applies the operation
     * {@code (a, b) -> Math.max(a, b)} to lane elements,
     * and the identity value is
     * {@link Float#NEGATIVE_INFINITY}.
     *
     * @param m the mask controlling lane selection
     * @return the maximum lane element of this vector
     */
    public abstract float maxAll(VectorMask<Float> m);


    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract float lane(int i);

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
    public abstract FloatVector with(int i, float e);

    // Type specific extractors

    /**
     * Returns an array containing the lane elements of this vector.
     * <p>
     * This method behaves as if it {@link #intoArray(float[], int)} stores}
     * this vector into an allocated array and returns the array as follows:
     * <pre>{@code
     *   float[] a = new float[this.length()];
     *   this.intoArray(a, 0);
     *   return a;
     * }</pre>
     *
     * @return an array containing the the lane elements of this vector
     */
    @ForceInline
    public final float[] toArray() {
        float[] a = new float[species().length()];
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
    public abstract void intoArray(float[] a, int offset);

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
    public abstract void intoArray(float[] a, int offset, VectorMask<Float> m);

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
    public abstract void intoArray(float[] a, int a_offset, int[] indexMap, int i_offset);

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
    public abstract void intoArray(float[] a, int a_offset, VectorMask<Float> m, int[] indexMap, int i_offset);
    // Species

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract VectorSpecies<Float> species();

    /**
     * Class representing {@link FloatVector}'s of the same {@link VectorShape VectorShape}.
     */
    static final class FloatSpecies extends AbstractSpecies<Float> {
        final Function<float[], FloatVector> vectorFactory;

        private FloatSpecies(VectorShape shape,
                          Class<?> boxType,
                          Class<?> maskType,
                          Function<float[], FloatVector> vectorFactory,
                          Function<boolean[], VectorMask<Float>> maskFactory,
                          Function<IntUnaryOperator, VectorShuffle<Float>> shuffleFromArrayFactory,
                          fShuffleFromArray<Float> shuffleFromOpFactory) {
            super(shape, float.class, Float.SIZE, boxType, maskType, maskFactory,
                  shuffleFromArrayFactory, shuffleFromOpFactory);
            this.vectorFactory = vectorFactory;
        }

        interface FOp {
            float apply(int i);
        }

        FloatVector op(FOp f) {
            float[] res = new float[length()];
            for (int i = 0; i < length(); i++) {
                res[i] = f.apply(i);
            }
            return vectorFactory.apply(res);
        }

        FloatVector op(VectorMask<Float> o, FOp f) {
            float[] res = new float[length()];
            boolean[] mbits = ((AbstractMask<Float>)o).getBits();
            for (int i = 0; i < length(); i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return vectorFactory.apply(res);
        }
    }

    /**
     * Finds the preferred species for an element type of {@code float}.
     * <p>
     * A preferred species is a species chosen by the platform that has a
     * shape of maximal bit size.  A preferred species for different element
     * types will have the same shape, and therefore vectors, masks, and
     * shuffles created from such species will be shape compatible.
     *
     * @return the preferred species for an element type of {@code float}
     */
    private static FloatSpecies preferredSpecies() {
        return (FloatSpecies) VectorSpecies.ofPreferred(float.class);
    }

    /**
     * Finds a species for an element type of {@code float} and shape.
     *
     * @param s the shape
     * @return a species for an element type of {@code float} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    static FloatSpecies species(VectorShape s) {
        Objects.requireNonNull(s);
        switch (s) {
            case S_64_BIT: return (FloatSpecies) SPECIES_64;
            case S_128_BIT: return (FloatSpecies) SPECIES_128;
            case S_256_BIT: return (FloatSpecies) SPECIES_256;
            case S_512_BIT: return (FloatSpecies) SPECIES_512;
            case S_Max_BIT: return (FloatSpecies) SPECIES_MAX;
            default: throw new IllegalArgumentException("Bad shape: " + s);
        }
    }

    /** Species representing {@link FloatVector}s of {@link VectorShape#S_64_BIT VectorShape.S_64_BIT}. */
    public static final VectorSpecies<Float> SPECIES_64 = new FloatSpecies(VectorShape.S_64_BIT, Float64Vector.class, Float64Vector.Float64Mask.class,
                                                                     Float64Vector::new, Float64Vector.Float64Mask::new,
                                                                     Float64Vector.Float64Shuffle::new, Float64Vector.Float64Shuffle::new);

    /** Species representing {@link FloatVector}s of {@link VectorShape#S_128_BIT VectorShape.S_128_BIT}. */
    public static final VectorSpecies<Float> SPECIES_128 = new FloatSpecies(VectorShape.S_128_BIT, Float128Vector.class, Float128Vector.Float128Mask.class,
                                                                      Float128Vector::new, Float128Vector.Float128Mask::new,
                                                                      Float128Vector.Float128Shuffle::new, Float128Vector.Float128Shuffle::new);

    /** Species representing {@link FloatVector}s of {@link VectorShape#S_256_BIT VectorShape.S_256_BIT}. */
    public static final VectorSpecies<Float> SPECIES_256 = new FloatSpecies(VectorShape.S_256_BIT, Float256Vector.class, Float256Vector.Float256Mask.class,
                                                                      Float256Vector::new, Float256Vector.Float256Mask::new,
                                                                      Float256Vector.Float256Shuffle::new, Float256Vector.Float256Shuffle::new);

    /** Species representing {@link FloatVector}s of {@link VectorShape#S_512_BIT VectorShape.S_512_BIT}. */
    public static final VectorSpecies<Float> SPECIES_512 = new FloatSpecies(VectorShape.S_512_BIT, Float512Vector.class, Float512Vector.Float512Mask.class,
                                                                      Float512Vector::new, Float512Vector.Float512Mask::new,
                                                                      Float512Vector.Float512Shuffle::new, Float512Vector.Float512Shuffle::new);

    /** Species representing {@link FloatVector}s of {@link VectorShape#S_Max_BIT VectorShape.S_Max_BIT}. */
    public static final VectorSpecies<Float> SPECIES_MAX = new FloatSpecies(VectorShape.S_Max_BIT, FloatMaxVector.class, FloatMaxVector.FloatMaxMask.class,
                                                                      FloatMaxVector::new, FloatMaxVector.FloatMaxMask::new,
                                                                      FloatMaxVector.FloatMaxShuffle::new, FloatMaxVector.FloatMaxShuffle::new);

    /**
     * Preferred species for {@link FloatVector}s.
     * A preferred species is a species of maximal bit size for the platform.
     */
    public static final VectorSpecies<Float> SPECIES_PREFERRED = (VectorSpecies<Float>) preferredSpecies();
}
