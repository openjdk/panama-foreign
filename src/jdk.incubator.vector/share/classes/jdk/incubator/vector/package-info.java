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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * {@Incubating}
 * <p>
 * Classes to express vector computations that, given suitable hardware
 * and runtime ability, are accelerated using vector hardware instructions.
 * <p>
 * Vector computations consist of a sequence of operations on vectors.
 * A vector is a fixed sequence of scalar values; a scalar value is
 * a single unit of value such as an int, a long, a float and so on.
 * Operations on vectors typically perform the equivalent scalar operation on all
 * scalar values of the participating vectors, usually generating a vector result.
 * When run on a supporting platform, these operations can be
 * executed in parallel by the hardware.
 * This style of parallelism is called <em>Single Instruction Multiple Data</em> (SIMD)
 * parallelism.
 *
 * <p>The abstract class {@link jdk.incubator.vector.Vector} represents an ordered immutable sequence of
 * values of the same element type 'e' that is one of the following primitive types -
 * byte, short, int, long, float, or double. The type variable E corresponds to the
 * boxed element type, specifically the class that wraps a value of e in an object
 * (such as Integer class that wraps a value of int).
 *
 * <p>Vector declares a set of vector operations (methods) that are common to
 * all element types (such as addition). Subclasses of Vector corresponding to
 * a specific element type declare further operations that are specific to that element type
 * (such as access to element values in lanes, logical operations on values of integral
 * elements types, or transcendental operations on values of floating point element
 * types). There are six abstract subclasses of {@link jdk.incubator.vector.Vector} corresponding to the supported set of
 * element types: {@link jdk.incubator.vector.ByteVector}, {@link jdk.incubator.vector.ShortVector},
 * {@link jdk.incubator.vector.IntVector}, {@link jdk.incubator.vector.LongVector},
 * {@link jdk.incubator.vector.FloatVector}, and {@link jdk.incubator.vector.DoubleVector}.
 *
 * In addition to element type, vectors are parameterized by their <em>shape</em>,
 * which is their length.  The supported shapes are
 * represented by the enum {@link jdk.incubator.vector.VectorShape}.
 * The combination of element type and shape determines a <em>vector species</em>,
 * represented by {@link jdk.incubator.vector.VectorSpecies}.  The various typed
 * vector classes expose static constants corresponding to the supported species,
 * and static methods on these types generally take a species as a parameter.
 * For example,
 * {@link jdk.incubator.vector.FloatVector#fromArray(VectorSpecies, float[], int) FloatVector.fromArray()}
 * creates and returns a float vector of the specified species, with elements
 * loaded from the specified float array.
 *
 * <p>
 * The species instance for a specific combination of element type and shape
 * can be obtained by reading the appropriate static field, as follows:
 * <p>
 * {@code VectorSpecies<Float> s = FloatVector.SPECIES_256};
 * <p>
 *
 * Code that is agnostic to species can request the "preferred" species for a
 * given element type, where the optimal size is selected for the current platform:
 * <p>
 * {@code VectorSpecies<Float> s = FloatVector.SPECIES_PREFERRED};
 * <p>
 *
 * <p>
 * Here is an example of multiplying elements of two float arrays {@code a and b} using vector computation
 * and storing result in array {@code c}.
 * <pre>{@code
 * static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_512;
 *
 * void vectorMultiply(float[] a, float[] b, float[] c) {
 *   int i = 0;
 *   // It is assumed array arguments are of the same size
 *   for (; i < (a.length & ~(SPECIES.length() - 1));
 *            i += SPECIES.length()) {
 *         FloatVector va = FloatVector.fromArray(SPECIES, a, i);
 *         FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
 *         FloatVector vc = va.mul(vb)
 *         vc.intoArray(c, i);
 *   }
 *
 *   for (; i < a.length; i++) {
 *     c[i] = a[i] * b[i];
 *   }
 * }
 * }</pre>
 *
 * The scalar computation after the vector computation is required to process the tail of
 * elements, the length of which is smaller than the species length.
 *
 * The example above uses vectors hardcoded to a concrete shape (512-bit). Instead, we could use preferred
 * species as shown below, to make the code dynamically adapt to optimal shape for the platform on which it runs.
 *
 * <pre>{@code
 * static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
 * }</pre>
 *
 * <h2>Vector operations</h2>
 * We use the term <em>lanes</em> when defining operations on vectors. The number of lanes
 * in a vector is the number of scalar elements it holds. For example, a vector of
 * type {@code Float} and shape {@code VectorShape.S_256_BIT} has eight lanes.
 * Vector operations can be grouped into various categories and their behavior
 * generally specified as follows:
 * <ul>
 * <li>
 * A lane-wise unary operation operates on one input vector and produce a
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
 *     ar[i] = scalar_unary_op(a.get(i));
 * }
 * EVector r = EVector.fromArray(a.species(), ar, 0);
 * }</pre>
 *
 * Unless otherwise specified the input and result vectors will have the same
 * element type and shape.
 *
 * <li>
 * A lane-wise binary operation operates on two input
 * vectors to produce a result vector.
 * For each lane of the two input vectors,
 * a and b say, the corresponding lane elements from a and b are operated on
 * using the specified scalar binary operation and the element result is placed
 * into the vector result at the same lane.
 * The following pseudocode expresses the behavior of this operation category:
 *
 * <pre>{@code
 * EVector a = ...;
 * EVector b = ...;
 * e[] ar = new e[a.length()];
 * for (int i = 0; i < a.length(); i++) {
 *     ar[i] = scalar_binary_op(a.get(i), b.get(i));
 * }
 * EVector r = EVector.fromArray(a.species(), ar, 0);
 * }</pre>
 *
 * Unless otherwise specified the two input and result vectors will have the
 * same element type and shape.
 *
 * <li>
 * Generalizing from unary and binary operations, a lane-wise n-ary
 * operation operates on n input vectors to produce a
 * result vector.
 * N lane elements from each input vector are operated on
 * using the specified n-ary scalar operation and the element result is placed
 * into the vector result at the same lane.
 *
 * Unless otherwise specified the n input and result vectors will have the same
 * element type and shape.
 *
 * <li>
 * A vector reduction operation operates on all the lane
 * elements of an input vector, and applies an accumulation function to all the
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
 *     r = assoc_scalar_binary_op(r, a.get(i));
 * }
 * }</pre>
 *
 * Unless otherwise specified the scalar result type and element type will be
 * the same.
 *
 * <li>
 * A lane-wise binary test operation operates on two input vectors to produce a
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
 *     ar[i] = scalar_binary_test_op(a.get(i), b.get(i));
 * }
 * VectorMask<E> r = VectorMask.fromArray(a.species(), ar, 0);
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
 * <h2> Performance notes </h2>
 * This package depends on the runtime's ability to dynamically compile vector operations
 * into optimal vector hardware instructions. There is a default scalar implementation
 * for each operation which is used if the operation cannot be compiled to vector instructions.
 *
 * <p>There are certain things users need to pay attention to for generating optimal vector machine code:
 *
 * <ul>
 * <li>The shape of vectors used should be supported by the underlying platform. For example,
 * code written using {@code IntVector} of Shape S_512_BIT will not be compiled to vector
 * instructions on a platform which supports only 256 bit vectors. Instead, the default
 * scalar implementation will be used.
 * For this reason, it is recommended to use the preferred species as shown above to write
 * generically sized vector computations.
 * <li>Classes defined in this package should be treated as
 * <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a> classes.
 * Use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) will limit generation of
 * optimal vector instructions.
 * </ul>
 */
package jdk.incubator.vector;
