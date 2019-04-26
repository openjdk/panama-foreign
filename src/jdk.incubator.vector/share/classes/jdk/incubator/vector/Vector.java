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
import jdk.internal.vm.annotation.Stable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

import jdk.incubator.vector.*;

/**
 * A {@code Vector} is designed for use in computations that can be transformed
 * by a runtime compiler, on supported hardware, to Single Instruction Multiple
 * Data (SIMD) computations leveraging vector hardware registers and vector
 * hardware instructions.  Such SIMD computations exploit data parallelism to
 * perform the same operation on multiple data points simultaneously in
 * less time than it would ordinarily take to perform the same operation
 * sequentially on each data point.
 * <p>
 * A Vector represents an ordered immutable sequence of values of the same
 * element type {@code e} that is one of the following primitive types
 * {@code byte}, {@code short}, {@code int}, {@code long}, {@code float}, or
 * {@code double}).  The type variable {@code E} corresponds to the boxed
 * element type, specifically the class that wraps a value of {@code e} in an
 * object (such the {@code Integer} class that wraps a value of {@code int}}.
 * A Vector has a {@link #shape() shape} {@code S}, extending type {@link VectorShape},
 * that governs the total {@link #bitSize() size} in bits of the sequence of values.
 * The combination of element type and shape determines a <em>vector species</em>,
 * represented by {@link jdk.incubator.vector.VectorSpecies}.
 * <p>
 * The number of values in the sequence is referred to as the Vector
 * {@link #length() length}.  The length also corresponds to the number of
 * Vector lanes.  The lane element at lane index {@code N} (from {@code 0},
 * inclusive, to length, exclusive) corresponds to the {@code N + 1}'th value in
 * the sequence.
 * Note: this arrangement
 * of Vector bit size, Vector length, element bit size, and lane element index
 * has no bearing on how a Vector instance and its sequence of elements may be
 * arranged in memory or represented as a value in a vector hardware register.
 * <p>
 * Vector declares a set of vector operations (methods) that are common to all
 * element types (such as addition).  Sub-classes of Vector with a concrete
 * boxed element type declare further operations that are specific to that
 * element type (such as access to element values in lanes, logical operations
 * on values of integral elements types, or transcendental operations on values
 * of floating point element types).
 * There are six abstract sub-classes of Vector corresponding to the supported set
 * of element types, {@link ByteVector}, {@link ShortVector},
 * {@link IntVector} {@link LongVector}, {@link FloatVector}, and
 * {@link DoubleVector}. Along with type-specific operations these classes
 * support creation of vector values (instances of Vector).
 * They expose static constants corresponding to the supported species,
 * and static methods on these types generally take a species as a parameter.
 * For example,
 * {@link jdk.incubator.vector.FloatVector#fromArray(VectorSpecies, float[], int) FloatVector.fromArray()}
 * creates and returns a float vector of the specified species, with elements
 * loaded from the specified float array.
 * <p>
 * It is recommended that Species instances be held in {@code static final}
 * fields for optimal creation and usage of Vector values by the runtime compiler.
 * <p>
 * Vector operations can be grouped into various categories and their behavior
 * generally specified as follows:
 * <ul>
 * <li>
 * A lane-wise unary operation operates on one input vector and produces a
 * result vector.
 * For each lane of the input vector the
 * lane element is operated on using the specified scalar unary operation and
 * the element result is placed into the vector result at the same lane.
 * The following pseudocode expresses the behavior of this operation category,
 * where {@code e} is the element type and {@code EVector} corresponds to the
 * primitive Vector type:
 *
 * <pre>{@code
 * EVector a = ...;
 * e[] ar = new e[a.length()];
 * for (int i = 0; i < a.length(); i++) {
 *     ar[i] = scalar_unary_op(a.lane(i));
 * }
 * EVector r = EVector.fromArray(a.species(), ar, 0);
 * }</pre>
 *
 * Unless otherwise specified the input and result vectors will have the same
 * element type and shape.
 *
 * <li>
 * A lane-wise binary operation operates on two input
 * vectors and produces a result vector.
 * For each lane of the two input vectors a and b,
 * the corresponding lane elements from a and b are operated on
 * using the specified scalar binary operation and the element result is placed
 * into the vector result at the same lane.
 * The following pseudocode expresses the behavior of this operation category:
 *
 * <pre>{@code
 * EVector a = ...;
 * EVector b = ...;
 * e[] ar = new e[a.length()];
 * for (int i = 0; i < a.length(); i++) {
 *     ar[i] = scalar_binary_op(a.lane(i), b.lane(i));
 * }
 * EVector r = EVector.fromArray(a.species(), ar, 0);
 * }</pre>
 *
 * Unless otherwise specified the two input and result vectors will have the
 * same element type and shape.
 *
 * <li>
 * Generalizing from unary and binary operations, a lane-wise n-ary
 * operation operates on n input vectors and produces a result vector.
 * N lane elements from each input vector are operated on
 * using the specified n-ary scalar operation and the element result is placed
 * into the vector result at the same lane.
 * Unless otherwise specified the n input and result vectors will have the same
 * element type and shape.
 *
 * <li>
 * A cross-lane vector reduction operation operates on all the lane
 * elements of an input vector.
 * An accumulation function is applied to all the
 * lane elements to produce a scalar result.
 * If the reduction operation is associative then the result may be accumulated
 * by operating on the lane elements in any order using a specified associative
 * scalar binary operation and identity value.  Otherwise, the reduction
 * operation specifies the behavior of the accumulation function.
 * The following pseudocode expresses the behavior of this operation category
 * if it is associative:
 * <pre>{@code
 * EVector a = ...;
 * e r = <identity value>;
 * for (int i = 0; i < a.length(); i++) {
 *     r = assoc_scalar_binary_op(r, a.lane(i));
 * }
 * }</pre>
 *
 * Unless otherwise specified the scalar result type and element type will be
 * the same.
 *
 * <li>
 * A lane-wise binary test operation operates on two input vectors and produces a
 * result mask.  For each lane of the two input vectors, a and b say, the
 * the corresponding lane elements from a and b are operated on using the
 * specified scalar binary test operation and the boolean result is placed
 * into the mask at the same lane.
 * The following pseudocode expresses the behavior of this operation category:
 * <pre>{@code
 * EVector a = ...;
 * EVector b = ...;
 * boolean[] ar = new boolean[a.length()];
 * for (int i = 0; i < a.length(); i++) {
 *     ar[i] = scalar_binary_test_op(a.lane(i), b.lane(i));
 * }
 * VectorMask r = VectorMask.fromArray(a.species(), ar, 0);
 * }</pre>
 *
 * Unless otherwise specified the two input vectors and result mask will have
 * the same element type and shape.
 *
 * <li>
 * The prior categories of operation can be said to operate within the vector
 * lanes, where lane access is uniformly applied to all vectors, specifically
 * the scalar operation is applied to elements taken from input vectors at the
 * same lane, and if appropriate applied to the result vector at the same lane.
 * A further category of operation is a cross-lane vector operation where lane
 * access is defined by the arguments to the operation.  Cross-lane operations
 * generally rearrange lane elements, for example by permutation (commonly
 * controlled by a {@link jdk.incubator.vector.VectorShuffle}) or by blending (commonly controlled by a
 * {@link jdk.incubator.vector.VectorMask}). Such an operation explicitly specifies how it rearranges lane
 * elements.
 * </ul>
 *
 * <p>
 * If a vector operation does not belong to one of the above categories then
 * the operation explicitly specifies how it processes the lane elements of
 * input vectors, and where appropriate expresses the behavior using
 * pseudocode.
 *
 * <p>
 * Many vector operations provide an additional {@link VectorMask mask} accepting
 * variant.
 * The mask controls which lanes are selected for application of the scalar
 * operation.  Masks are a key component for the support of control flow in
 * vector computations.
 * <p>
 * Many vector operations provide an additional {@link jdk.incubator.vector.VectorMask mask}-accepting
 * variant.
 * The mask controls which lanes are selected for application of the scalar
 * operation.  Masks are a key component for the support of control flow in
 * vector computations.
 * <p>
 * For certain operation categories the mask accepting variants can be specified
 * in generic terms.  If a lane of the mask is set then the scalar operation is
 * applied to corresponding lane elements, otherwise if a lane of a mask is not
 * set then a default scalar operation is applied and its result is placed into
 * the vector result at the same lane. The default operation is specified as follows:
 * <ul>
 * <li>
 * For a lane-wise n-ary operation the default operation is a function that returns
 * it's first argument, specifically the lane element of the first input vector.
 * <li>
 * For an associative vector reduction operation the default operation is a
 * function that returns the identity value.
 * <li>
 * For lane-wise binary test operation the default operation is a function that
 * returns false.
 * </ul>
 * Otherwise, the mask accepting variant of the operation explicitly specifies
 * how it processes the lane elements of input vectors, and where appropriate
 * expresses the behavior using pseudocode.
 *
 * <p>
 * For convenience, many vector operations of arity greater than one provide
 * an additional scalar-accepting variant (such as adding a constant scalar
 * value to all lanes of a vector).  This variant accepts compatible
 * scalar values instead of vectors for the second and subsequent input vectors,
 * if any.
 * Unless otherwise specified the scalar variant behaves as if each scalar value
 * is transformed to a vector using the appropriate vector {@code broadcast} operation, and
 * then the vector accepting vector operation is applied using the transformed
 * values.
 *
 * <p>
 * This is a value-based
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code Vector} may have unpredictable results and should be avoided.
 *
 * @param <E> the boxed element type of elements in this vector
 */
public abstract class Vector<E> {

    Vector() {}

    /**
     * Returns the species of this vector.
     *
     * @return the species of this vector
     */
    public abstract VectorSpecies<E> species();

    /**
     * Returns the primitive element type of this vector.
     *
     * @return the primitive element type of this vector
     */
    public Class<E> elementType() { return species().elementType(); }

    /**
     * Returns the element size, in bits, of this vector.
     *
     * @return the element size, in bits, of this vector
     */
    public int elementSize() { return species().elementSize(); }

    /**
     * Returns the shape of this vector.
     *
     * @return the shape of this vector
     */
    public VectorShape shape() { return species().shape(); }

    /**
     * Returns the number of vector lanes (the length).
     *
     * @return the number of vector lanes
     */
    public int length() { return species().length(); }

    /**
     * Returns the total size, in bits, of this vector.
     *
     * @return the total size, in bits, of this vector
     */
    public int bitSize() { return species().bitSize(); }

    //Arithmetic

    /**
     * Adds this vector to an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive addition operation
     * ({@code +}) to each lane.
     *
     * @param v the input vector
     * @return the result of adding this vector to the input vector
     */
    public abstract Vector<E> add(Vector<E> v);

    /**
     * Adds this vector to an input vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive addition operation
     * ({@code +}) to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of adding this vector to the given vector
     */
    public abstract Vector<E> add(Vector<E> v, VectorMask<E> m);

    /**
     * Subtracts an input vector from this vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive subtraction
     * operation ({@code -}) to each lane.
     *
     * @param v the input vector
     * @return the result of subtracting the input vector from this vector
     */
    public abstract Vector<E> sub(Vector<E> v);

    /**
     * Subtracts an input vector from this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive subtraction
     * operation ({@code -}) to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of subtracting the input vector from this vector
     */
    public abstract Vector<E> sub(Vector<E> v, VectorMask<E> m);

    /**
     * Multiplies this vector with an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the primitive multiplication
     * operation ({@code *}) to each lane.
     *
     * @param v the input vector
     * @return the result of multiplying this vector with the input vector
     */
    public abstract Vector<E> mul(Vector<E> v);

    /**
     * Multiplies this vector with an input vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the primitive multiplication
     * operation ({@code *}) to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of multiplying this vector with the input vector
     */
    public abstract Vector<E> mul(Vector<E> v, VectorMask<E> m);

    /**
     * Negates this vector.
     * <p>
     * This is a lane-wise unary operation which applies the primitive negation operation
     * ({@code -}) to each lane.
     *
     * @return the negation this vector
     */
    public abstract Vector<E> neg();

    /**
     * Negates this vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise unary operation which applies the primitive negation operation
     * ({@code -}) to each lane.
     *
     * @param m the mask controlling lane selection
     * @return the negation this vector
     */
    public abstract Vector<E> neg(VectorMask<E> m);

    // Maths from java.math

    /**
     * Returns the modulus of this vector.
     * <p>
     * This is a lane-wise unary operation which applies the operation
     * {@code (a) -> (a < 0) ? -a : a} to each lane.
     *
     * @return the modulus this vector
     */
    public abstract Vector<E> abs();

    /**
     * Returns the modulus of this vector, selecting lane elements controlled by
     * a mask.
     * <p>
     * This is a lane-wise unary operation which applies the operation
     * {@code (a) -> (a < 0) ? -a : a} to each lane.
     *
     * @param m the mask controlling lane selection
     * @return the modulus this vector
     */
    public abstract Vector<E> abs(VectorMask<E> m);

    /**
     * Returns the minimum of this vector and an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> a < b ? a : b}  to each lane.
     *
     * @param v the input vector
     * @return the minimum of this vector and the input vector
     */
    public abstract Vector<E> min(Vector<E> v);

    /**
     * Returns the minimum of this vector and an input vector,
     * selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> a < b ? a : b}  to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the minimum of this vector and the input vector
     */
    public abstract Vector<E> min(Vector<E> v, VectorMask<E> m);

    /**
     * Returns the maximum of this vector and an input vector.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> a > b ? a : b}  to each lane.
     *
     * @param v the input vector
     * @return the maximum of this vector and the input vector
     */
    public abstract Vector<E> max(Vector<E> v);

    /**
     * Returns the maximum of this vector and an input vector,
     * selecting lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies the operation
     * {@code (a, b) -> a > b ? a : b}  to each lane.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the maximum of this vector and the input vector
     */
    public abstract Vector<E> max(Vector<E> v, VectorMask<E> m);

    // Comparisons

    /**
     * Tests if this vector is equal to an input vector.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive equals
     * operation ({@code ==}) to each lane.
     *
     * @param v the input vector
     * @return the result mask of testing if this vector is equal to the input
     * vector
     */
    public abstract VectorMask<E> equal(Vector<E> v);

    /**
     * Tests if this vector is not equal to an input vector.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive not equals
     * operation ({@code !=}) to each lane.
     *
     * @param v the input vector
     * @return the result mask of testing if this vector is not equal to the
     * input vector
     */
    public abstract VectorMask<E> notEqual(Vector<E> v);

    /**
     * Tests if this vector is less than an input vector.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive less than
     * operation ({@code <}) to each lane.
     *
     * @param v the input vector
     * @return the mask result of testing if this vector is less than the input
     * vector
     */
    public abstract VectorMask<E> lessThan(Vector<E> v);

    /**
     * Tests if this vector is less or equal to an input vector.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive less than
     * or equal to operation ({@code <=}) to each lane.
     *
     * @param v the input vector
     * @return the mask result of testing if this vector is less than or equal
     * to the input vector
     */
    public abstract VectorMask<E> lessThanEq(Vector<E> v);

    /**
     * Tests if this vector is greater than an input vector.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive greater than
     * operation ({@code >}) to each lane.
     *
     * @param v the input vector
     * @return the mask result of testing if this vector is greater than the
     * input vector
     */
    public abstract VectorMask<E> greaterThan(Vector<E> v);

    /**
     * Tests if this vector is greater than or equal to an input vector.
     * <p>
     * This is a lane-wise binary test operation which applies the primitive greater than
     * or equal to operation ({@code >=}) to each lane.
     *
     * @param v the input vector
     * @return the mask result of testing if this vector is greater than or
     * equal to the given vector
     */
    public abstract VectorMask<E> greaterThanEq(Vector<E> v);

    // Elemental shifting

    /**
     * Rotates left the lane elements of this vector by the given number of
     * lanes, {@code i}, modulus the vector length.
     * <p>
     * This is a cross-lane operation that permutes the lane elements of this
     * vector.
     * For each lane of the input vector, at lane index {@code N}, the lane
     * element is placed into the result vector at lane index
     * {@code (N + i) % length()}.
     *
     * @param i the number of lanes to rotate left
     * @return the result of rotating left lane elements of this vector by the
     * given number of lanes
     */
    public abstract Vector<E> rotateLanesLeft(int i);

    /**
     * Rotates right the lane elements of this vector by the given number of
     * lanes, {@code i}, modulus the vector length.
     * <p>
     * This is a cross-lane operation that permutes the lane elements of this
     * vector.
     * For each lane of the input vector, at lane index {@code N}, the lane
     * element is placed into the result vector at lane index
     * {@code (N + length() - (i % length())) % length()}
     *
     * @param i the number of lanes to rotate right
     * @return the result of rotating right lane elements of this vector by the
     * given number of lanes
     */
    public abstract Vector<E> rotateLanesRight(int i);

    /**
     * Shift left the lane elements of this vector by the given number of
     * lanes, {@code i}, modulus the vector length.
     * <p>
     * This is a cross-lane operation that permutes the lane elements of this
     * vector and behaves as if rotating left the lane elements by {@code i},
     * and then the zero value is placed into the result vector at lane indexes
     * less than {@code i % length()}.
     *
     * @param i the number of lanes to shift left
     * @return the result of shifting left lane elements of this vector by the
     * given number of lanes
     * @throws IllegalArgumentException if {@code i} is {@code < 0}.
     */
    public abstract Vector<E> shiftLanesLeft(int i);

    /**
     * Shift right the lane elements of this vector by the given number of
     * lanes, {@code i}, modulus the vector length.
     * <p>
     * This is a cross-lane operation that permutes the lane elements of this
     * vector and behaves as if rotating right the lane elements by {@code i},
     * and then the zero value is placed into the result vector at lane indexes
     * greater or equal to {@code length() - (i % length())}.
     *
     * @param i the number of lanes to shift right
     * @return the result of shifting right lane elements of this vector by the
     * given number of lanes
     * @throws IllegalArgumentException if {@code i} is {@code < 0}.
     */
    public abstract Vector<E> shiftLanesRight(int i);

    /**
     * Blends the lane elements of this vector with those of an input vector,
     * selecting lanes controlled by a mask.
     * <p>
     * For each lane of the mask, at lane index {@code N}, if the mask lane
     * is set then the lane element at {@code N} from the input vector is
     * selected and placed into the resulting vector at {@code N},
     * otherwise the lane element at {@code N} from this vector is
     * selected and placed into the resulting vector at {@code N}.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of blending the lane elements of this vector with
     * those of an input vector
     */
    public abstract Vector<E> blend(Vector<E> v, VectorMask<E> m);

    /**
     * Rearranges the lane elements of this vector selecting lane indexes
     * controlled by a shuffle.
     * <p>
     * This is a cross-lane operation that rearranges the lane elements of this
     * vector.
     * For each lane of the shuffle, at lane index {@code N} with lane
     * element {@code I}, the lane element at {@code I} from this vector is
     * selected and placed into the resulting vector at {@code N}.
     *
     * @param s the shuffle controlling lane index selection
     * @return the rearrangement of the lane elements of this vector
     */
    public abstract Vector<E> rearrange(VectorShuffle<E> s);

    /**
     * Rearranges the lane elements of this vector and those of an input vector,
     * selecting lane indexes controlled by shuffles and a mask.
     * <p>
     * This is a cross-lane operation that rearranges the lane elements of this
     * vector and the input vector.  This method behaves as if it rearranges
     * each vector with the corresponding shuffle and then blends the two
     * results with the mask:
     * <pre>{@code
     * return this.rearrange(s1).blend(v.rearrange(s2), m);
     * }</pre>
     *
     * @param v the input vector
     * @param s the shuffle controlling lane index selection of the input vector
     * if corresponding mask lanes are set, otherwise controlling lane
     * index selection of this vector
     * @param m the mask controlling shuffled lane selection
     * @return the rearrangement of lane elements of this vector and
     * those of an input vector
     */
    public abstract Vector<E> rearrange(Vector<E> v,
                                           VectorShuffle<E> s, VectorMask<E> m);



    // Conversions

    /**
     * Converts this vector into a shuffle, creating a shuffle from vector
     * lane elements cast to {@code int} then logically AND'ed with the
     * shuffle length minus one.
     * <p>
     * This methods behaves as if it returns the result of creating a shuffle
     * given an array of the vector lane elements, as follows:
     * <pre>{@code
     * $type$[] a = this.toArray();
     * int[] sa = new int[a.length];
     * for (int i = 0; i < a.length; i++) {
     *     sa[i] = (int) a[i];
     * }
     * return this.species().shuffleFromValues(sa);
     * }</pre>
     *
     * @return a shuffle representation of this vector
     */
    public abstract VectorShuffle<E> toShuffle();

    // Bitwise preserving

    /**
     * Transforms this vector to a vector of the given species of element type {@code F}.
     * <p>
     * The underlying bits of this vector are copied to the resulting
     * vector without modification, but those bits, before copying, may be
     * truncated if the this vector's bit size is greater than desired vector's bit
     * size, or appended to with zero bits if this vector's bit size is less
     * than desired vector's bit size.
     * <p>
     * The method behaves as if this vector is stored into a byte buffer
     * and then the desired vector is loaded from the byte buffer using
     * native byte ordering. The implication is that ByteBuffer reads bytes
     * and then composes them based on the byte ordering so the result
     * depends on this composition.
     * <p>
     * For example, on a system with ByteOrder.LITTLE_ENDIAN, loading from
     * byte array with values {0,1,2,3} and reshaping to int, leads to bytes
     * being composed in order 0x3 0x2 0x1 0x0 which is decimal value 50462976.
     * On a system with ByteOrder.BIG_ENDIAN, the value is instead 66051 because
     * bytes are composed in order 0x0 0x1 0x2 0x3.
     * <p>
     * The following pseudocode expresses the behavior:
     * <pre>{@code
     * int bufferLen = Math.max(this.bitSize(), s.bitSize()) / Byte.SIZE;
     * ByteBuffer bb = ByteBuffer.allocate(bufferLen).order(ByteOrder.nativeOrder());
     * this.intoByteBuffer(bb, 0);
     * return $type$Vector.fromByteBuffer(s, bb, 0);
     * }</pre>
     *
     * @param s species of desired vector
     * @param <F> the boxed element type of the species
     * @return a vector transformed, by shape and element type, from this vector
     * @see Vector#reshape(VectorSpecies)
     * @see Vector#cast(VectorSpecies)
     */
    public abstract <F> Vector<F> reinterpret(VectorSpecies<F> s);

    @ForceInline
    @SuppressWarnings("unchecked")
    <F> Vector<F> defaultReinterpret(VectorSpecies<F> s) {
        int blen = Math.max(s.bitSize(), this.species().bitSize()) / Byte.SIZE;
        ByteBuffer bb = ByteBuffer.allocate(blen).order(ByteOrder.nativeOrder());
        this.intoByteBuffer(bb, 0);

        Class<?> stype = s.elementType();
        if (stype == byte.class) {
           return (Vector) ByteVector.fromByteBuffer((ByteVector.ByteSpecies)s, bb, 0);
        } else if (stype == short.class) {
           return (Vector) ShortVector.fromByteBuffer((ShortVector.ShortSpecies)s, bb, 0);
        } else if (stype == int.class) {
           return (Vector) IntVector.fromByteBuffer((IntVector.IntSpecies)s, bb, 0);
        } else if (stype == long.class) {
           return (Vector) LongVector.fromByteBuffer((LongVector.LongSpecies)s, bb, 0);
        } else if (stype == float.class) {
           return (Vector) FloatVector.fromByteBuffer((FloatVector.FloatSpecies)s, bb, 0);
        } else if (stype == double.class) {
           return (Vector) DoubleVector.fromByteBuffer((DoubleVector.DoubleSpecies)s, bb, 0);
        } else {
            throw new UnsupportedOperationException("Bad lane type for reinterpret.");
        }
    }

    /**
     * Transforms this vector to a vector of same element type but different shape identified by species.
     * <p>
     * The lane elements of this vector are copied without
     * modification to the resulting vector, but those lane elements, before
     * copying, may be truncated if this vector's length is greater than the desired
     * vector's length, or appended to with default element values if this
     * vector's length is less than desired vector's length.
     * <p>
     * The method behaves as if this vector is stored into a byte array
     * and then the returned vector is loaded from the byte array.
     * The following pseudocode expresses the behavior:
     * <pre>{@code
     * int alen = Math.max(this.bitSize(), s.bitSize()) / Byte.SIZE;
     * byte[] a = new byte[alen];
     * this.intoByteArray(a, 0);
     * return $type$Vector.fromByteArray(s, a, 0);
     * }</pre>
     *
     * @param s species of the desired vector
     * @return a vector transformed, by shape, from this vector
     * @see Vector#reinterpret(VectorSpecies)
     * @see Vector#cast(VectorSpecies)
     */
    public abstract Vector<E> reshape(VectorSpecies<E> s);

    // Cast

    /**
     * Converts this vector to a vector of the given species element type {@code F}.
     * <p>
     * For each vector lane up to the length of this vector or
     * desired vector, which ever is the minimum, and where {@code N} is the
     * vector lane index, the element at index {@code N} of primitive type
     * {@code E} is converted, according to primitive conversion rules
     * specified by the Java Language Specification, to a value of primitive
     * type {@code F} and placed into the resulting vector at lane index
     * {@code N}. If desired vector's length is greater than this
     * vector's length then the default primitive value is placed into
     * subsequent lanes of the resulting vector.
     *
     * @param s species of the desired vector
     * @param <F> the boxed element type of the species
     * @return a vector converted by shape and element type from this vector
     * @see Vector#reshape(VectorSpecies)
     * @see Vector#reinterpret(VectorSpecies)
     */
    public abstract <F> Vector<F> cast(VectorSpecies<F> s);

    //Array stores

    /**
     * Stores this vector into a byte array starting at an offset.
     * <p>
     * Bytes are extracted from primitive lane elements according to the
     * native byte order of the underlying platform.
     * <p>
     * This method behaves as it calls the
     * byte buffer, offset, and mask accepting
     * {@link #intoByteBuffer(ByteBuffer, int, VectorMask) method} as follows:
     * <pre>{@code
     * return this.intoByteBuffer(ByteBuffer.wrap(a), i, this.maskAllTrue());
     * }</pre>
     *
     * @param a the byte array
     * @param i the offset into the array
     * @return a vector loaded from a byte array
     * @throws IndexOutOfBoundsException if {@code i < 0} or
     * {@code i > a.length - (this.length() * this.elementSize() / Byte.SIZE)}
     */
    public abstract void intoByteArray(byte[] a, int i);

    /**
     * Stores this vector into a byte array starting at an offset and using a mask.
     * <p>
     * Bytes are extracted from primitive lane elements according to the
     * native byte order of the underlying platform.
     * <p>
     * This method behaves as it calls the
     * byte buffer, offset, and mask accepting
     * {@link #intoByteBuffer(ByteBuffer, int, VectorMask) method} as follows:
     * <pre>{@code
     * return this.intoByteBuffer(ByteBuffer.wrap(a), i, m);
     * }</pre>
     *
     * @param a the byte array
     * @param i the offset into the array
     * @param m the mask controlling lane selection
     * @throws IndexOutOfBoundsException if the offset is {@code < 0},
     * or {@code > a.length},
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set
     * {@code i >= a.length - (N * this.elementSize() / Byte.SIZE)}
     */
    public abstract void intoByteArray(byte[] a, int i, VectorMask<E> m);

    /**
     * Stores this vector into a {@link ByteBuffer byte buffer} starting at an
     * offset into the byte buffer.
     * <p>
     * Bytes are extracted from primitive lane elements according to the
     * native byte order of the underlying platform.
     * <p>
     * This method behaves as if it calls the byte buffer, offset, and mask
     * accepting
     * {@link #intoByteBuffer(ByteBuffer, int, VectorMask)} method} as follows:
     * <pre>{@code
     *   this.intoByteBuffer(b, i, this.maskAllTrue())
     * }</pre>
     *
     * @param b the byte buffer
     * @param i the offset into the byte buffer
     * @throws IndexOutOfBoundsException if the offset is {@code < 0},
     * or {@code > b.limit()},
     * or if there are fewer than
     * {@code this.length() * this.elementSize() / Byte.SIZE} bytes
     * remaining in the byte buffer from the given offset
     */
    public abstract void intoByteBuffer(ByteBuffer b, int i);

    /**
     * Stores this vector into a {@link ByteBuffer byte buffer} starting at an
     * offset into the byte buffer and using a mask.
     * <p>
     * This method behaves as if the byte buffer is viewed as a primitive
     * {@link java.nio.Buffer buffer} for the primitive element type,
     * according to the native byte order of the underlying platform, and
     * the lane elements of this vector are put into the buffer if the
     * corresponding mask lane is set.
     * The following pseudocode expresses the behavior, where
     * {@coce EBuffer} is the primitive buffer type, {@code e} is the
     * primitive element type, and {@code EVector} is the primitive
     * vector type for this vector:
     * <pre>{@code
     * EBuffer eb = b.duplicate().
     *     order(ByteOrder.nativeOrder()).position(i).
     *     asEBuffer();
     * e[] es = ((EVector)this).toArray();
     * for (int n = 0; n < t.length; n++) {
     *     if (m.isSet(n)) {
     *         eb.put(n, es[n]);
     *     }
     * }
     * }</pre>
     *
     * @param b the byte buffer
     * @param i the offset into the byte buffer
     * @param m the mask
     * @throws IndexOutOfBoundsException if the offset is {@code < 0},
     * or {@code > b.limit()},
     * for any vector lane index {@code N} where the mask at lane {@code N}
     * is set
     * {@code i >= b.limit() - (N * this.elementSize() / Byte.SIZE)} bytes
     */
    public abstract void intoByteBuffer(ByteBuffer b, int i, VectorMask<E> m);

    /**
     * Find bit size based on element type and number of elements.
     *
     * @param c the element type
     * @param numElem number of lanes in the vector
     * @return size in bits for vector
     */
    public static int bitSizeForVectorLength(Class<?> c, int numElem) {
        if (c == float.class) {
            return Float.SIZE * numElem;
        }
        else if (c == double.class) {
            return Double.SIZE * numElem;
        }
        else if (c == byte.class) {
            return Byte.SIZE * numElem;
        }
        else if (c == short.class) {
            return Short.SIZE * numElem;
        }
        else if (c == int.class) {
            return Integer.SIZE * numElem;
        }
        else if (c == long.class) {
            return Long.SIZE * numElem;
        }
        else {
            throw new IllegalArgumentException("Bad vector type: " + c.getName());
        }
    }

    /**
     * Returns a mask of same species as {@code this} vector and where each lane is set or unset according to given
     * {@code boolean} values.
     * <p>
     * This method behaves as if it returns the result of calling the static {@link VectorMask#fromValues(VectorSpecies, boolean...) fromValues()}
     * method in VectorMask as follows:
     * <pre> {@code
     *     return VectorMask.fromValues(this.species(), bits);
     * } </pre>
     *
     * @param bits the given {@code boolean} values
     * @return a mask where each lane is set or unset according to the given {@code boolean} value
     * @throws IndexOutOfBoundsException if {@code bits.length < this.species().length()}
     * @see VectorMask#fromValues(VectorSpecies, boolean...)
     */
    @ForceInline
    public final VectorMask<E> maskFromValues(boolean... bits) {
        return VectorMask.fromValues(this.species(), bits);
    }

    /**
     * Loads a mask of same species as {@code this} vector from a {@code boolean} array starting at an offset.
     * <p>
     * This method behaves as if it returns the result of calling the static {@link VectorMask#fromArray(VectorSpecies, boolean[], int) fromArray()}
     * method in VectorMask as follows:
     * <pre> {@code
     *     return VectorMask.fromArray(this.species(), bits, offset);
     * } </pre>
     *
     * @param bits the {@code boolean} array
     * @param offset the offset into the array
     * @return the mask loaded from a {@code boolean} array
     * @throws IndexOutOfBoundsException if {@code offset < 0}, or
     * {@code offset > bits.length - species.length()}
     * @see VectorMask#fromArray(VectorSpecies, boolean[], int)
     */
    @ForceInline
    public final VectorMask<E> maskFromArray(boolean[] bits, int offset) {
        return VectorMask.fromArray(this.species(), bits, offset);
    }

    /**
     * Returns a mask of same species as {@code this} vector and where all lanes are set.
     *
     * @return a mask where all lanes are set
     * @see VectorMask#maskAllTrue(VectorSpecies)
     */
    @ForceInline
    public final VectorMask<E> maskAllTrue() {
        return VectorMask.maskAllTrue(this.species());
    }

    /**
     * Returns a mask of same species as {@code this} vector and where all lanes are unset.
     *
     * @return a mask where all lanes are unset
     * @see VectorMask#maskAllFalse(VectorSpecies)
     */
    @ForceInline
    public final VectorMask<E> maskAllFalse() {
        return VectorMask.maskAllFalse(this.species());
    }

    /**
     * Returns a shuffle of same species as {@code this} vector and where each lane element is set to a given
     * {@code int} value logically AND'ed by the species length minus one.
     * <p>
     * This method behaves as if it returns the result of calling the static {@link VectorShuffle#fromValues(VectorSpecies, int...) fromValues()}
     * method in VectorShuffle as follows:
     * <pre> {@code
     *     return VectorShuffle.fromValues(this.species(), ixs);
     * } </pre>
     *
     * @param ixs the given {@code int} values
     * @return a shuffle where each lane element is set to a given
     * {@code int} value
     * @throws IndexOutOfBoundsException if the number of int values is
     * {@code < this.species().length()}
     * @see AbstractShuffle#fromValues(VectorSpecies, int...)
     */
    @ForceInline
    public final VectorShuffle<E> shuffleFromValues(int... ixs) {
        return VectorShuffle.fromValues(this.species(), ixs);
    }

    /**
     * Loads a shuffle of same species as {@code this} vector from an {@code int} array starting at an offset.
     * <p>
     * This method behaves as if it returns the result of calling the static {@link VectorShuffle#fromArray(VectorSpecies, int[], int) fromArray()}
     * method in VectorShuffle as follows:
     * <pre> {@code
     *     return VectorShuffle.fromArray(this.species(), ixs, offset);
     * } </pre>
     *
     * @param ixs the {@code int} array
     * @param offset the offset into the array
     * @return a shuffle loaded from the {@code int} array
     * @throws IndexOutOfBoundsException if {@code offset < 0}, or
     * {@code offset > ixs.length - this.species().length()}
     * @see AbstractShuffle#fromArray(VectorSpecies, int[], int)
     */
    @ForceInline
    public final VectorShuffle<E> shuffleFromArray(int[] ixs, int offset) {
        return VectorShuffle.fromArray(this.species(), ixs, offset);
    }

    /**
     * Returns a shuffle of same species as {@code this} vector of mapped indexes where each lane element is
     * the result of applying a mapping function to the corresponding lane
     * index.
     * <p>
     * This method behaves as if it returns the result of calling the static {@link VectorShuffle#shuffle(VectorSpecies, IntUnaryOperator) shuffle()}
     * method in VectorShuffle as follows:
     * <pre> {@code
     *     return AbstractShuffle.shuffle(this.species(), f);
     * } </pre>
     *
     * @param f the lane index mapping function
     * @return a shuffle of mapped indexes
     * @see AbstractShuffle#shuffle(VectorSpecies, IntUnaryOperator)
     */
    @ForceInline
    public final VectorShuffle<E> shuffle(IntUnaryOperator f) {
        return AbstractShuffle.shuffle(this.species(), f);
    }

    /**
     * Returns a shuffle of same species as {@code this} vector and where each lane element is the value of its
     * corresponding lane index.
     * <p>
     * This method behaves as if it returns the result of calling the static {@link VectorShuffle#shuffleIota(VectorSpecies) shuffleIota()}
     * method in VectorShuffle as follows:
     * <pre> {@code
     *     return VectorShuffle.shuffleIota(this.species());
     * } </pre>
     *
     * @return a shuffle of lane indexes
     * @see AbstractShuffle#shuffleIota(VectorSpecies)
     */
    @ForceInline
    public final VectorShuffle<E> shuffleIota() {
        return VectorShuffle.shuffleIota(this.species());
    }

    /**
     * Returns a shuffle of same species as {@code this} vector and with lane elements set to sequential {@code int}
     * values starting from {@code start}.
     * <p>
     * This method behaves as if it returns the result of calling the static {@link VectorShuffle#shuffleIota(VectorSpecies, int) shuffleIota()}
     * method in VectorShuffle as follows:
     * <pre> {@code
     *     return VectorShuffle.shuffleIota(this.species(), start);
     * } </pre>
     *
     * @param start starting value of sequence
     * @return a shuffle of lane indexes
     * @see AbstractShuffle#shuffleIota(VectorSpecies, int)
     */
    @ForceInline
    public final VectorShuffle<E> shuffleIota(int start) {
        return VectorShuffle.shuffleIota(this.species(), start);
    }

    /**
     * Returns a shuffle of same species as {@code this} vector and with lane elements set to sequential {@code int}
     * values starting from {@code start} and looping around species length.
     * <p>
     * This method behaves as if it returns the result of calling the static {@link VectorShuffle#shuffleOffset(VectorSpecies, int) shuffleOffset()}
     * method in VectorShuffle as follows:
     * <pre> {@code
     *     return VectorShuffle.shuffleOffset(this.species(), start);
     * } </pre>
     *
     * @param start starting value of sequence
     * @return a shuffle of lane indexes
     * @see AbstractShuffle#shuffleOffset(VectorSpecies, int)
     */
    @ForceInline
    public final VectorShuffle<E> shuffleOffset(int start) {
        return VectorShuffle.shuffleOffset(this.species(), start);
    }
}
