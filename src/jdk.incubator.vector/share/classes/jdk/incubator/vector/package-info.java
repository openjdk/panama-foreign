/*
 * Copyright (c) 2017, 2019, Oracle and/or its affiliates. All rights reserved.
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
 * This package provides
 * classes to express vector computations that, given suitable hardware
 * and runtime ability, are accelerated using vector hardware instructions.
 *
 * <p> A {@linkplain Vector <em>vector</em>} is a
 *
 * <!-- The following paragraphs are shared verbatim
 *   -- between Vector.java and package-info.java -->
 * sequence of a fixed number of <em>lanes</em>,
 * all of some fixed
 * {@linkplain Vector#elementType() <em>element type</em>}
 * such as {@code byte}, {@code long}, or {@code float}.
 * Each lane contains an independent value of the element type.
 * Operations on vectors are typically
 * <a href="Vector.html#lane-wise"><em>lane-wise</em></a>,
 * distributing some scalar operation (such as
 * {@linkplain Vector#add(Vector) addition})
 * across the lanes of the participating vectors,
 *
 * usually generating a vector result whose lanes contain the various
 * scalar results.  When run on a supporting platform, lane-wise
 * operations can be executed in parallel by the hardware.  This style
 * of parallelism is called <em>Single Instruction Multiple Data</em>
 * (SIMD) parallelism.
 *
 * <p> In the SIMD style of programming, most of the operations within
 * a vector lane are unconditional, but the effect of conditional
 * execution may be achieved using
 * <a href="Vector.html#masking"><em>masked operations</em></a>
 * such as {@link Vector#blend(Vector,VectorMask) blend()},
 * under the control of an associated {@link VectorMask}.
 * Data motion other than strictly lane-wise flow is achieved using
 * <a href="Vector.html#cross-lane"><em>cross-lane</em></a>
 * operations, often under the control of an associated
 * {@link VectorShuffle}.
 * Lane data and/or whole vectors can be reformatted using various
 * kinds of lane-wise
 * {@linkplain Vector#convert(VectorOperators.Conversion,int) conversions},
 * and byte-wise reformatting
 * {@linkplain Vector#reinterpretShape(VectorSpecies,int) reinterpretations},
 * often under the control of a reflective {@link VectorSpecies}
 * object which selects an alternative vector format different
 * from that of the input vector.
 *
 * <!-- The preceding paragraphs are shared verbatim
 *   -- between Vector.java and package-info.java -->
 *
 * <p> In the abstract type {@link Vector Vector&lt;E&gt;}, the type
 * argument {@code E} specifies the box type corresponding to the
 * primitive element type.
 *
 * For example, {@code Integer} is the box for {@code int}, so {@code
 * Vector<Integer>} describes a vector where the lanes are of type
 * {@code int}.
 * 
 * The other element types, {@code byte}, {@code short}, {@code int},
 * {@code long}, {@code float}, or {@code double}, are specified by
 * their box types, all of which are named by capitalizing the
 * corresponding primitive type name.
 *
 * <p> {@code Vector<E>} declares a set of vector operations (methods)
 * that are common to all element types.  These common operations
 * include generic access to lane values, data selection and movement,
 * reformatting, and certain arithmetic operations (such as addition
 * or comparison) that are common to all primitive types.
 *
 * <p> Public subtypes of {@code Vector} correspond to specific
 * element types.  These declare further operations that are specific
 * to that element type, including unboxed access to lane values,
 * bitwise operations on values of integral element types, or
 * transcendental operations on values of floating point element
 * types.
 *
 * <p>This package contains a public subtype of {@link Vector}
 * corresponding to each supported element type:
 * {@link ByteVector}, {@link ShortVector},
 * {@link IntVector}, {@link LongVector},
 * {@link FloatVector}, and {@link DoubleVector}.
 *
 * <p>In addition to element type, every vector is assigned a definite <em>shape</em>,
 * which determines its overall format, including size in bits.  The supported shapes
 * are represented by the enumeration {@link VectorShape}.
 * The combination of element type and shape determines a <em>vector species</em>,
 * represented by {@link VectorSpecies}.  The various typed
 * vector classes expose static constants corresponding to the supported species,
 * and static methods on these types generally take a species as a parameter.
 * Unless otherwise documented, lane-wise vector operations require that all
 * inputs must be of the same species, and any vector result will also be
 * of the same species.
 *
 * <p>Vectors can be loaded to memory and stored back, with optional masking.
 * The shape of a vector determines how much memory it will occupy.  In the
 * absence of masking, the lanes are stored as a dense sequence of
 * back-to-back values in memory, the same as a dense (gap-free) series
 * of single scalar values.  The first lane value occupies the first
 * position in memory, and so on, up to the length of the vector.
 * Although memory order is not directly defined by Java, the memory order
 * of stored vector lanes always corresponds to increasing index values
 * in a Java array or a {@link java.nio.ByteBuffer}.
 * Within a vector, lane ordering is described as if the m
 * Byte order for lane storage is chosen such that the stored vector values
 * can be read or written as single primitive values, within the array
 * or buffer that holds the vector, producing the same values as the
 * lane-wise values within the vector.
 *
 * <p>For example,
 * {@link FloatVector#fromArray(VectorSpecies, float[], int)
 *        FloatVector.fromArray(fsp,fa,i)}
 * creates and returns a float vector of some particular species {@code fsp},
 * with elements loaded from some float array {@code fa}.
 * The first lane is loaded from {@code fa[i]} and the last lane
 * is initialized loaded from {@code fa[i+VL-1]}, where {@code VL}
 * is the length of the vector as derived from the species {@code fsp}.
 * Then, {@link FloatVector#add(Vector<Float>) FloatVector.add(fv2)}
 * will produce another float vector of that species {@code fsp},
 * but it must also be presented with a second float vector {@code fv2} of
 * the same species {@code fsp}.
 *
 * <p>
 * The species instance for a specific combination of element type and shape
 * can be obtained by reading the appropriate static field, as follows:
 * <pre>
 * {@code VectorSpecies<Float> s = FloatVector.SPECIES_256;}
 * </pre>
 *
 * Code that is agnostic to species can request the so-called
 * <em>preferred</em> species for a
 * given element type, where the optimal size is selected for the current platform:
 * <pre>
 * {@code VectorSpecies<Float> s = FloatVector.SPECIES_PREFERRED;}
 * </pre>
 *
 * <p>
 * Here is an example of multiplying elements of two float arrays
 * {@code a} and {@code b} using vector computation
 * and storing result in array {@code c}.
 * <pre>{@code
 * static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_512;
 *
 * void vectorMultiply(float[] a, float[] b, float[] c) {
 *   int i = 0;
 *   // It is assumed array arguments are of the same size
 *   for (; i < SPECIES.loopBound(a.length); i += SPECIES.length()) {
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

 * The scalar computation after the vector computation is required to
 * process a <em>tail</em> of <em>TLENGTH</em> array elements, where
 * <em>TLENGTH</em>{@code <= }<em>VLENGTH</em> for the vector species.
 
 * elements, the length of which is smaller than the species length. {@code VectorSpecies} also defines a
 * {@link VectorSpecies#loopBound(int) loopBound()} helper method which can be used in place of
 * {@code (a.length & ~(SPECIES.length() - 1))} in the above code to determine the terminating condition.
 *
 * The example above uses vectors hardcoded to a concrete shape (512-bit). Instead, we could use preferred
 * species as shown below, to make the code dynamically adapt to optimal shape for the platform on which it runs.
 *
 * <pre>{@code
 * static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
 * }</pre>
 *
 * <h1>Vector operations</h1>
 * We use the term <em>lanes</em> when defining operations on vectors. The number of lanes
 * in a vector is the number of scalar elements it holds. For example, a vector of
 * type {@code Float} and shape {@code VectorShape.S_256_BIT} has eight lanes.
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
 *     r = assoc_scalar_binary_op(r, a.lane(i));
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
 *     ar[i] = scalar_binary_test_op(a.lane(i), b.lane(i));
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
 * controlled by a {@link VectorShuffle}) or by blending (commonly controlled by a
 * {@link VectorMask}). Such an operation explicitly specifies how it rearranges lane
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
 * Many vector operations provide an additional {@link VectorMask mask}-accepting
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
 * its first argument, specifically the lane element of the first input vector.
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
 * <h1> Performance notes </h1>
 * This package depends on the runtime's ability to dynamically compile vector operations
 * into optimal vector hardware instructions. There is a default scalar implementation
 * for each operation which is used if the operation cannot be compiled to vector instructions.
 *
 * <p>There are certain things users need to pay attention to for generating optimal vector machine code:
 *
 * <ul>

 * <li> The shape of vectors used should be supported by the underlying
 * platform. For example, code written using {@link IntVector} of
 * {@link Shape} {@link Shape#S_512_BIT S_512_BIT} will not be
 * compiled to vector instructions on a platform which supports only
 * 256 bit vectors. Instead, the default scalar implementation will be
 * used.  For this reason, it is recommended to use the preferred
 * species as shown above to write generically sized vector
 * computations.
 *
 * <li> Most classes defined in this package should be treated as
 * <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a> classes.
 * This classification applies to {@link Vector} and its subtypes,
 * {@link VectorMask}, {@link VectorShuffle}, and {@link VectorSpecies}.
 *
 * With these types,
 *
 * <!-- The following paragraph is shared verbatim
 *   -- between Vector.java and package-info.java -->
 * identity-sensitive operations such as {@code ==} may yield
 * unpredictable results, or reduced performance.  Oddly enough,
 * {@link Vector#equals(Object) v.equals(w)} is likely to be faster
 * than {@code v==w}, since {@code equals} is <em>not</em> an identity
 * sensitive method.  It is also reasonable to use, on vectors, the
 * {@code toString} and {@code hashCode} methods of {@code Object}.
 *
 * Also, these objects can be stored in locals and parameters and as
 * {@code static final} constants, but storing them in other Java
 * fields or in array elements, while semantically valid, will may
 * incur performance risks.
 * <!-- The preceding paragraph is shared verbatim
 *   -- between Vector.java and package-info.java -->
 *
 * <li> Unless specified otherwise, any method arguments of reference
 * type must not be {@code null}, and any {@code null} argument will
 * elicit a {@code NullPointerException}.  This fact is not
 * individually documented for methods in this package.
 *
 * </ul>
 *
 * <p>
 * For every class in this package,
 * unless specified otherwise, any method arguments of reference
 * type must not be null, and any null argument will elicit a
 * {@code NullPointerException}.  This fact is not individually
 * documented for methods of this API.
 */
package jdk.incubator.vector;
