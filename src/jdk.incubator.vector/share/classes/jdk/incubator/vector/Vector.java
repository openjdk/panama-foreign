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

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

import java.nio.ByteBuffer;

/**
 * A {@code Vector} is designed for use in computations that can be transformed
 * by a runtime compiler, on supported hardware, to Single Instruction Multiple
 * Data (SIMD) computations leveraging vector hardware registers and vector
 * hardware instructions.  Such SIMD computations exploit data parallelism to
 * perform the same operation on multiple data points simultaneously in a
 * faster time it would ordinarily take to perform the same operation
 * sequentially on each data point.
 * <p>
 * A Vector represents an ordered immutable sequence of elements of the same
 * element type {@code e} that is one of the following primitive types
 * {@code byte}, {@code short}, {@code int}, {@code long}, {@code float}, or
 * {@code double}).  The type variable {@code E} corresponds to the boxed
 * element type, specifically the class that wraps a value of {@code e} in an
 * object (such the {@code Integer} class that wraps a value of {@code int}}.
 * A Vector has a {@link #shape() shape } {@code S}, extending type
 * {@link #Vector.Shape}, that governs the total {@link #bitSize() size} in bits
 * of the sequence of elements.
 * <p>
 * The number of elements in the sequence is referred to as the Vector
 * {@link #length() length}, and can be derived from the Vector bit size divided
 * by the element bit size.
 * The length also corresponds to the number of Vector lanes.  An element at
 * position {@code N} (from {@code 0}, inclusive, to length, exclusive) in the
 * sequence corresponds to an element at lane {@code N}.  Note: this arrangement
 * of Vector bit size, Vector length, element bit size, and element position has
 * no bearing on how a Vector instance and its sequence of elements may be
 * arranged in memory or represented as a value in a vector hardware register.
 * <p>
 * Vector declares a set of vector operations (methods) that are common to all
 * element types (such as addition).  Sub-classes of Vector with a concrete
 * boxed element type declare further operations that are specific to that
 * element type (such as access to element values in lanes, logical operations
 * on values of integral elements types, or transcendental operations on values
 * of floating point element types).
 * There are six sub-classes of Vector corresponding to the supported set
 * of element types, {@link ByteVector<S>}, {@link ShortVector<S>},
 * {@link IntVector<S>} {@link LongVector<S>}, {@link FloatVector<S>}, and
 * {@link DoubleVector<S>}.
 * <p>
 * Species...
 *
 * Masks...
 *
 * Shuffles...
 *
 *
 * <p>
 * Vector operations can be grouped into various categories and their behaviour
 * generally specified as follows:
 * <ul>
 * <li>
 * A vector unary operation (1-ary) operates on one input
 * vector to produce a result vector.  For each lane of the input vector the
 * lane element is operated on using the specified scalar unary operation and
 * the element result is placed into the vector result at the same lane.
 * The following pseudocode expresses the behaviour of this operation category,
 * where {@code e} is the element type and {@code EVector} corresponds to the
 * primitive Vector type:
 *
 * <pre>{@code
 * EVector<S> a = ...;
 * e[] ar = new e[a.length()];
 * for (int i = 0; i < a.length(); i++) {
 *     ar[i] = scalar_unary_op(a.getElement(i));
 * }
 * EVector<S> r = a.species().fromArray(ar, 0);
 * }</pre>
 *
 * Unless otherwise specified the input and result vectors will have the same
 * element type and shape.
 *
 * <li>
 * A vector binary operation (2-ary) operates on two input
 * vectors to produce a result vector.  For each lane of the two input vectors,
 * a and b say, the corresponding lane elements from a and b are operated on
 * using the specified scalar binary operation and the element result is placed
 * into the vector result at the same lane.
 * The following pseudocode expresses the behaviour of this operation category:
 *
 * <pre>{@code
 * EVector<S> a = ...;
 * EVector<S> b = ...;
 * e[] ar = new e[a.length()];
 * for (int i = 0; i < a.length(); i++) {
 *     ar[i] = scalar_binary_op(a.getElement(i), b.getElement(i));
 * }
 * EVector<S> r = a.species().fromArray(ar, 0);
 * }</pre>
 *
 * Unless otherwise specified the two input and result vectors will have the
 * same element type and shape.
 *
 * <li>
 * Generalizing from unary (1-ary) and binary (2-ary) operations, a vector n-ary
 * operation operates in n input vectors to produce a
 * result vector.  N lane elements from each input vector are operated on
 * using the specified n-ary scalar operation and the element result is placed
 * into the vector result at the same lane.
 * Unless otherwise specified the n input and result vectors will have the same
 * element type and shape.
 *
 * <li>
 * A vector reductive operation operates on all the lane
 * elements of an input vector.  An accumulation function is applied to all the
 * lane elements to produce a scalar result.
 * If the reductive operation is associative then the result may be accumulated
 * by operating on the lane elements in any order using a specified associative
 * scalar binary operation and identity value.  Otherwise, the reductive
 * operation specifies the behaviour of the accumulation function.
 * The following pseudocode expresses the behaviour of this operation category
 * if it is associative:
 * <pre>{@code
 * EVector<S> a = ...;
 * e r = <identity value>;
 * for (int i = 0; i < a.length(); i++) {
 *     r = assoc_scalar_binary_op(r, a.getElement(i));
 * }
 * }</pre>
 *
 * Unless otherwise specified the scalar result type and element type will be
 * the same.
 *
 * <li>
 * A vector binary test operation operates on two input vectors to produce a
 * result mask.  For each lane of the two input vectors, a and b say, the
 * the corresponding lane elements from a and b are operated on using the
 * specified scalar binary test operation and the boolean result is placed
 * into the mask at the same lane.
 * The following pseudocode expresses the behaviour of this operation category:
 * <pre>{@code
 * EVector<S> a = ...;
 * EVector<S> b = ...;
 * boolean[] ar = new boolean[a.length()];
 * for (int i = 0; i < a.length(); i++) {
 *     ar[i] = scalar_binary_test_op(a.getElement(i), b.getElement(i));
 * }
 * Mask<E, S> r = a.species().maskFromArray(ar, 0);
 * }</pre>
 *
 * Unless otherwise specified the two input vectors and result mask will have
 * the same element type and shape.
 *
 * <li>
 * A vector cross-lane operation...
 * </ul>
 *
 * If a vector operation does not fit into any of the above categories then
 * the operation explicitly specifies how it processes the lane elements of
 * input vectors, and where appropriate expresses the behaviour using
 * pseudocode.
 *
 * <p>
 * Many vector operations provide an additional mask accepting variant.  The
 * mask governs which lanes are selected to apply the scalar operation to lane
 * elements.
 * For certain operation categories the mask accepting variants can be specified
 * in generic terms.  If a lane of the mask is set then the scalar operation is
 * applied to corresponding lane elements, otherwise if a lane of a mask is not
 * set then a default scalar operation is applied and its result is placed into
 * the vector result at the same lane. The default operation is specified for
 * the following operation categories:
 * <ul>
 * <li>
 * For a vector n-ary operation the default operation is a function that returns
 * it's first argument.
 * <li>
 * For an associative vector reductive operation the default operation is a
 * function that returns the identity value.
 * <li>
 * For vector binary test operation the default operation is a function that
 * returns false.
 *</ul>
 * Otherwise, the mask accepting variant of the operation explicitly specifies
 * how it processes the lane elements of input vectors, and where appropriate
 * expresses the behaviour using pseudocode.
 *
 * <p>
 * For convenience many vector operations, of arity greater than one, provide
 * an additional scalar accepting variant.  This variant accepts compatible
 * scalar values instead of vectors for the second and subsequent arguments,
 * if any.
 * Unless otherwise specified the scalar variant behaves as if each scalar value
 * is transformed to a vector using the vector species broadcast operation, and
 * then the vector accepting vector operation is applied to the transformed
 * values.
 *
 * <p>
 * This is a value-based
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code Vector} may have unpredictable results and should be avoided.
 *
 * @param <E> the boxed element type of elements in this vector
 * @param <S> the type of shape of this vector
 */
public abstract class Vector<E, S extends Vector.Shape> {

    Vector() {}

    public abstract Species<E, S> species();

    public Class<E> elementType() { return species().elementType(); }

    public S shape() { return species().shape(); }

    public int length() { return species().length(); }

    public int bitSize() { return species().bitSize(); }

    //Arithmetic

    /**
     * Adds this vector to an input vector.
     * <p>
     * This is a vector binary operation where the primitive addition operation
     * ({@code +}) is applied to lane elements.
     *
     * @param b the input vector
     * @return the result of adding this vector to the input vector
     */
    public abstract Vector<E, S> add(Vector<E, S> b);

    /**
     * Adds this vector to an input vector, selecting lane elements
     * governed by a mask.
     * <p>
     * This is a vector binary operation where the primitive addition operation
     * ({@code +}) is applied to lane elements.
     *
     * @param b the input vector
     * @param m the mask governing lane selection
     * @return the result of adding this vector to the given vector
     */
    public abstract Vector<E, S> add(Vector<E, S> b, Mask<E, S> m);

    public abstract Vector<E, S> addSaturate(Vector<E, S> o);

    public abstract Vector<E, S> addSaturate(Vector<E, S> o, Mask<E, S> m);

    public abstract Vector<E, S> sub(Vector<E, S> o);

    public abstract Vector<E, S> sub(Vector<E, S> o, Mask<E, S> m);

    public abstract Vector<E, S> subSaturate(Vector<E, S> o);

    public abstract Vector<E, S> subSaturate(Vector<E, S> o, Mask<E, S> m);

    public abstract Vector<E, S> mul(Vector<E, S> o);

    public abstract Vector<E, S> mul(Vector<E, S> o, Mask<E, S> m);

    /**
     * Negates this vector.
     * <p>
     * This is a vector unary operation where the primitive negation operation
     * ({@code -})is applied to lane elements.
     *
     * @return the result of negating this vector
     */
    public abstract Vector<E, S> neg();

    public abstract Vector<E, S> neg(Mask<E, S> m);

    //Maths from java.math
    public abstract Vector<E, S> abs();

    public abstract Vector<E, S> abs(Mask<E, S> m);

    public abstract Vector<E, S> min(Vector<E, S> o);

    public abstract Vector<E, S> max(Vector<E, S> o);

    //TODO: Parity

    //Comparisons

    //TODO: N.B. Floating point NaN behaviors?
    //TODO: Check the JLS

    /**
     * Tests if this vector is equal to the given vector.
     * <p>
     * This is a vector binary test operation where the primitive equals
     * operation ({@code ==}) is applied to lane elements.
     *
     * @param b the given vector
     * @return the result mask of testing if this vector is equal to the given
     * vector
     */
    public abstract Mask<E, S> equal(Vector<E, S> b);

    public abstract Mask<E, S> notEqual(Vector<E, S> o);

    /**
     * Tests if this vector is less than the given vector.
     * <p>
     * This is a vector binary test operation where the primitive less than
     * operation ({@code <}) is applied to lane elements.
     *
     * @param b the given vector
     * @return the mask result of testing if this vector is less than the given
     * vector
     */
    public abstract Mask<E, S> lessThan(Vector<E, S> b);

    public abstract Mask<E, S> lessThanEq(Vector<E, S> o);

    public abstract Mask<E, S> greaterThan(Vector<E, S> o);

    public abstract Mask<E, S> greaterThanEq(Vector<E, S> o);

    //Elemental shifting
    public abstract Vector<E, S> rotateEL(int i); //Rotate elements left

    public abstract Vector<E, S> rotateER(int i); //Rotate elements right

    public abstract Vector<E, S> shiftEL(int i); //shift elements left

    public abstract Vector<E, S> shiftER(int i); //shift elements right

    //Blend, etc.
    public abstract Vector<E, S> blend(Vector<E, S> o, Mask<E, S> m);

    //Shuffles
    public abstract Vector<E, S> shuffle(Vector<E, S> o, Shuffle<E, S> s); //TODO: Example

    public abstract Vector<E, S> swizzle(Shuffle<E, S> s);


    // Conversions

    // Bitwise preserving

    @ForceInline
    public <F, T extends Shape> Vector<F, T> reshape(Species<F, T> species) {
        return species.reshape(this);
    }

    @ForceInline
    public <F> Vector<F, S> rebracket(Species<F, S> species) {
        return species.reshape(this);
    }

    public abstract <T extends Shape> Vector<E, T> resize(Species<E, T> species);

    // Cast

    @ForceInline
    public <F, T extends Shape> Vector<F, T> cast(Species<F, T> species) {
        return species.cast(this);
    }

    //Array stores

    public abstract void intoByteArray(byte[] bs, int ix);

    public abstract void intoByteArray(byte[] bs, int ix, Mask<E, S> m);

    public abstract void intoByteBuffer(ByteBuffer bb);

    public abstract void intoByteBuffer(ByteBuffer bb, Mask<E, S> m);

    public abstract void intoByteBuffer(ByteBuffer bb, int ix);

    public abstract void intoByteBuffer(ByteBuffer bb, int ix, Mask<E, S> m);


    public static abstract class Species<E, S extends Shape> {
        Species() {}

        public abstract Class<E> elementType();

        public abstract int elementSize();

        public abstract S shape();

        public int length() { return shape().length(this); }

        public int bitSize() { return shape().bitSize(); }

        // Factory

        public abstract Vector<E, S> zero();

        public abstract Vector<E, S> fromByteArray(byte[] bs, int ix);

        public abstract Vector<E, S> fromByteArray(byte[] bs, int ix, Mask<E, S> m);

        public abstract Vector<E, S> fromByteBuffer(ByteBuffer bb);

        public abstract Vector<E, S> fromByteBuffer(ByteBuffer bb, Mask<E, S> m);

        public abstract Vector<E, S> fromByteBuffer(ByteBuffer bb, int ix);

        public abstract Vector<E, S> fromByteBuffer(ByteBuffer bb, int ix, Mask<E, S> m);

        //Mask and shuffle constructions

        public abstract Mask<E, S> maskFromValues(boolean... bits);

        public abstract Mask<E, S> maskFromArray(boolean[] bits, int i);

        public abstract Mask<E, S> maskAllTrue();

        public abstract Mask<E, S> maskAllFalse();

        public abstract Shuffle<E, S> shuffleFromValues(int... ixs);

        public abstract Shuffle<E, S> shuffleFromArray(int[] ixs, int i);

        // Vector type/shape transformations

        // Reshape
        // Preserves bits, truncating if new shape is smaller in bit size than
        // old shape, or expanding (with zero bits) if new shape is larger in
        // bit size
        public abstract <F, T extends Shape> Vector<E, S> reshape(Vector<F, T> o);

        // Change type, not shape
        // No truncation or expansion of bits
        public abstract <F> Vector<E, S> rebracket(Vector<F, S> o);

        // Change shape, not type
        // Truncation or expansion of bits
        public abstract <T extends Shape> Vector<E, S> resize(Vector<E, T> o);

        // Cast
        // Elements will be converted as per JLS primitive conversion rules
        // If elementType == o.elementType then its equivalent to a resize
        public abstract <F, T extends Shape> Vector<E, S> cast(Vector<F, T> o);


        // Mask type/shape transformations

        public <F, T extends Shape> Mask<E, S> reshape(Mask<F, T> m) {
            return maskFromValues(m.toArray());
        }

        @ForceInline
        public <F> Mask<E, S> rebracket(Mask<F, S> m) {
            return reshape(m);
        }

        @ForceInline
        public <T extends Shape> Mask<E, S> resize(Mask<E, T> m) {
            return reshape(m);
        }


        // Shuffle type/shape transformations

        public <F, T extends Shape> Shuffle<E, S> reshape(Shuffle<F, T> m) {
            return shuffleFromValues(m.toArray());
        }

        @ForceInline
        public <F> Shuffle<E, S> rebracket(Shuffle<F, S> m) {
            return reshape(m);
        }

        @ForceInline
        public <T extends Shape> Shuffle<E, S> resize(Shuffle<E, T> m) {
            return reshape(m);
        }


        // Species/species transformations

        // Returns a species for a given element type and the length of this
        // species.
        // The length of the returned species will be equal to the length of
        // this species.
        //
        // Throws IAE if no shape exists for the element type and this species length,
//        public <F> Species<F, ?> toSpeciesWithSameNumberOfLanes(Class<F> c) {
//            // @@@ TODO implement and find better name
//            throw new UnsupportedOperationException();
//        }

    }

    public static abstract class Shape {
        Shape() {}

        public abstract int bitSize();  // usually 64, 128, 256, etc.

        public int length(Species<?, ?> s) { return bitSize() / s.elementSize(); }  // usually bitSize / sizeof(s.elementType)
    }

    public static abstract class Mask<E, S extends Shape> {
        Mask() {}

        public int length() { return species().length(); }

        public abstract long toLong();

        public abstract void intoArray(boolean[] bits, int i);

        public abstract boolean[] toArray();

        public abstract boolean anyTrue();

        public abstract boolean allTrue();

        public abstract int trueCount();

        // TODO: LZ count
        // numberOfLeadingZeros
        // numberOfTrailingZeros

        public abstract Mask<E, S> and(Mask<E, S> o);

        public abstract Mask<E, S> or(Mask<E, S> o);

        public abstract Mask<E, S> not();

        public abstract Species<E, S> species();

        public abstract Vector<E, S> toVector();

        public abstract boolean getElement(int i);

        @ForceInline
        public <F, T extends Shape> Mask<F, T> reshape(Species<F, T> species) {
            return species.reshape(this);
        }

        @ForceInline
        public <F> Mask<F, S> rebracket(Species<F, S> species) {
            return species.reshape(this);
        }

        @ForceInline
        public <T extends Shape> Mask<E, T> resize(Species<E, T> species) {
            return species.reshape(this);
        }
    }

    public static abstract class Shuffle<E, S extends Shape> {
        Shuffle() {}

        public int length() { return species().length(); }

        public abstract void intoArray(int[] ixs, int i);

        public abstract int[] toArray();

        public abstract Species<E, S> species();

        public abstract IntVector.IntSpecies<S> intSpecies();

        public abstract IntVector<S> toVector();

        public int getElement(int i) { return toArray()[i]; }

        @ForceInline
        public <F, T extends Shape> Shuffle<F, T> reshape(Species<F, T> species) {
            return species.reshape(this);
        }

        @ForceInline
        public <F> Shuffle<F, S> rebracket(Species<F, S> species) {
            return species.reshape(this);
        }

        @ForceInline
        public <T extends Shape> Shuffle<E, T> resize(Species<E, T> species) {
            return species.reshape(this);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> Vector.Species<E, ?> preferredSpeciesInstance(Class<E> c) {
        Unsafe u = Unsafe.getUnsafe();

        int vectorLength = u.getMaxVectorSize(boxToPrimitive(c));
        int vectorBitSize = bitSizeForVectorLength(c, vectorLength);
        Shape s = shapeForVectorBitSize(vectorBitSize);
        return speciesInstance(c, s);
    }

    private static Class<?> boxToPrimitive(Class<?> c) {
        if (c == Float.class) {
            return float.class;
        }
        else if (c == Double.class) {
            return double.class;
        }
        else if (c == Byte.class) {
            return byte.class;
        }
        else if (c == Short.class) {
            return short.class;
        }
        else if (c == Integer.class) {
            return int.class;
        }
        else if (c == Long.class) {
            return long.class;
        }
        else {
            throw new IllegalArgumentException("Bad vector type: " + c.getName());
        }
    }

    // @@@ public static method on Species?
    private static int bitSizeForVectorLength(Class<?> c, int elementSize) {
        if (c == Float.class) {
            return Float.SIZE * elementSize;
        }
        else if (c == Double.class) {
            return Double.SIZE * elementSize;
        }
        else if (c == Byte.class) {
            return Byte.SIZE * elementSize;
        }
        else if (c == Short.class) {
            return Short.SIZE * elementSize;
        }
        else if (c == Integer.class) {
            return Integer.SIZE * elementSize;
        }
        else if (c == Long.class) {
            return Long.SIZE * elementSize;
        }
        else {
            throw new IllegalArgumentException("Bad vector type: " + c.getName());
        }
    }

    // @@@ public static method on Shape?
    private static Shape shapeForVectorBitSize(int bitSize) {
        switch (bitSize) {
            case 64:
                return Shapes.S_64_BIT;
            case 128:
                return Shapes.S_128_BIT;
            case 256:
                return Shapes.S_256_BIT;
            case 512:
                return Shapes.S_512_BIT;
            default:
                throw new IllegalArgumentException("Bad vector bit size: " + bitSize);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E, S extends Shape> Vector.Species<E, S> speciesInstance(Class<E> c, S s) {
        Vector.Species<E, S> res = null;

        //Float
        if (c == Float.class) {
            if (s == Shapes.S_64_BIT)
                res = (Vector.Species<E, S>) Float64Vector.SPECIES;
            else if (s == Shapes.S_128_BIT)
                res = (Vector.Species<E, S>) Float128Vector.SPECIES;
            else if (s == Shapes.S_256_BIT)
                res = (Vector.Species<E, S>) Float256Vector.SPECIES;
            else if (s == Shapes.S_512_BIT)
                res = (Vector.Species<E, S>) Float512Vector.SPECIES;
            //Double
        }
        else if (c == Double.class) {
            if (s == Shapes.S_64_BIT)
                res = (Vector.Species<E, S>) Double64Vector.SPECIES;
            else if (s == Shapes.S_128_BIT)
                res = (Vector.Species<E, S>) Double128Vector.SPECIES;
            else if (s == Shapes.S_256_BIT)
                res = (Vector.Species<E, S>) Double256Vector.SPECIES;
            else if (s == Shapes.S_512_BIT)
                res = (Vector.Species<E, S>) Double512Vector.SPECIES;
            //Byte
        }
        else if (c == Byte.class) {
            if (s == Shapes.S_64_BIT)
                res = (Vector.Species<E, S>) Byte64Vector.SPECIES;
            else if (s == Shapes.S_128_BIT)
                res = (Vector.Species<E, S>) Byte128Vector.SPECIES;
            else if (s == Shapes.S_256_BIT)
                res = (Vector.Species<E, S>) Byte256Vector.SPECIES;
            else if (s == Shapes.S_512_BIT)
                res = (Vector.Species<E, S>) Byte512Vector.SPECIES;
            //Short
        }
        else if (c == Short.class) {
            if (s == Shapes.S_64_BIT)
                res = (Vector.Species<E, S>) Short64Vector.SPECIES;
            else if (s == Shapes.S_128_BIT)
                res = (Vector.Species<E, S>) Short128Vector.SPECIES;
            else if (s == Shapes.S_256_BIT)
                res = (Vector.Species<E, S>) Short256Vector.SPECIES;
            else if (s == Shapes.S_512_BIT)
                res = (Vector.Species<E, S>) Short512Vector.SPECIES;
            //Int
        }
        else if (c == Integer.class) {
            if (s == Shapes.S_64_BIT)
                res = (Vector.Species<E, S>) Int64Vector.SPECIES;
            else if (s == Shapes.S_128_BIT)
                res = (Vector.Species<E, S>) Int128Vector.SPECIES;
            else if (s == Shapes.S_256_BIT)
                res = (Vector.Species<E, S>) Int256Vector.SPECIES;
            else if (s == Shapes.S_512_BIT)
                res = (Vector.Species<E, S>) Int512Vector.SPECIES;
            //Long
        }
        else if (c == Long.class) {
            if (s == Shapes.S_64_BIT)
                res = (Vector.Species<E, S>) Long64Vector.SPECIES;
            else if (s == Shapes.S_128_BIT)
                res = (Vector.Species<E, S>) Long128Vector.SPECIES;
            else if (s == Shapes.S_256_BIT)
                res = (Vector.Species<E, S>) Long256Vector.SPECIES;
            else if (s == Shapes.S_512_BIT)
                res = (Vector.Species<E, S>) Long512Vector.SPECIES;
        }
        else {
            throw new IllegalArgumentException("Bad vector type: " + c.getName());
        }
        return res;
    }
}
