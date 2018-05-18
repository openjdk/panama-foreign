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

import jdk.internal.vm.annotation.ForceInline;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;


/**
 * A specialized {@link Vector} representing an ordered immutable sequence of
 * {@code float} values.
 *
 * @param <S> the type of shape of this vector
 */
@SuppressWarnings("cast")
public abstract class FloatVector<S extends Vector.Shape> extends Vector<Float,S> {

    FloatVector() {}

    // Unary operator

    interface FUnOp {
        float apply(int i, float a);
    }

    abstract FloatVector<S> uOp(FUnOp f);

    abstract FloatVector<S> uOp(Mask<Float, S> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        float apply(int i, float a, float b);
    }

    abstract FloatVector<S> bOp(Vector<Float,S> v, FBinOp f);

    abstract FloatVector<S> bOp(Vector<Float,S> v, Mask<Float, S> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        float apply(int i, float a, float b, float c);
    }

    abstract FloatVector<S> tOp(Vector<Float,S> v1, Vector<Float,S> v2, FTriOp f);

    abstract FloatVector<S> tOp(Vector<Float,S> v1, Vector<Float,S> v2, Mask<Float, S> m, FTriOp f);

    // Reduction operator

    abstract float rOp(float v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, float a, float b);
    }

    abstract Mask<Float, S> bTest(Vector<Float,S> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, float a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Float, S> m, FUnCon f);

    //

    @Override
    public abstract FloatVector<S> add(Vector<Float,S> v);

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
    public abstract FloatVector<S> add(float s);

    @Override
    public FloatVector<S> add(Vector<Float,S> v, Mask<Float, S> m) {
        return bOp(v, m, (i, a, b) -> (float) (a + b));
    }

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
    public abstract FloatVector<S> add(float s, Mask<Float, S> m);

    @Override
    public abstract FloatVector<S> sub(Vector<Float,S> v);

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
    public abstract FloatVector<S> sub(float s);

    @Override
    public FloatVector<S> sub(Vector<Float,S> v, Mask<Float, S> m) {
        return bOp(v, m, (i, a, b) -> (float) (a - b));
    }

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
    public abstract FloatVector<S> sub(float s, Mask<Float, S> m);

    @Override
    public abstract FloatVector<S> mul(Vector<Float,S> v);

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
    public abstract FloatVector<S> mul(float s);

    @Override
    public FloatVector<S> mul(Vector<Float,S> v, Mask<Float, S> m) {
        return bOp(v, m, (i, a, b) -> (float) (a * b));
    }

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
    public abstract FloatVector<S> mul(float s, Mask<Float, S> m);

    @Override
    public abstract FloatVector<S> neg();

    @Override
    public FloatVector<S> neg(Mask<Float, S> m) {
        return uOp(m, (i, a) -> (float) (-a));
    }

    @Override
    public abstract FloatVector<S> abs();

    @Override
    public FloatVector<S> abs(Mask<Float, S> m) {
        return uOp(m, (i, a) -> (float) Math.abs(a));
    }

    @Override
    public abstract FloatVector<S> min(Vector<Float,S> v);

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> a < b ? a : b}  is applied to lane elements.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract FloatVector<S> min(float s);

    @Override
    public abstract FloatVector<S> max(Vector<Float,S> v);

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> a > b ? a : b}  is applied to lane elements.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract FloatVector<S> max(float s);

    @Override
    public abstract Mask<Float, S> equal(Vector<Float,S> v);

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
    public abstract Mask<Float, S> equal(float s);

    @Override
    public abstract Mask<Float, S> notEqual(Vector<Float,S> v);

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
    public abstract Mask<Float, S> notEqual(float s);

    @Override
    public abstract Mask<Float, S> lessThan(Vector<Float,S> v);

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
    public abstract Mask<Float, S> lessThan(float s);

    @Override
    public abstract Mask<Float, S> lessThanEq(Vector<Float,S> v);

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
    public abstract Mask<Float, S> lessThanEq(float s);

    @Override
    public abstract Mask<Float, S> greaterThan(Vector<Float,S> v);

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
    public abstract Mask<Float, S> greaterThan(float s);

    @Override
    public abstract Mask<Float, S> greaterThanEq(Vector<Float,S> v);

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
    public abstract Mask<Float, S> greaterThanEq(float s);

    @Override
    public abstract FloatVector<S> blend(Vector<Float,S> v, Mask<Float, S> m);

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
    public abstract FloatVector<S> blend(float s, Mask<Float, S> m);

    @Override
    public abstract FloatVector<S> shuffle(Vector<Float,S> v, Shuffle<Float, S> m);

    @Override
    public abstract FloatVector<S> swizzle(Shuffle<Float, S> m);

    @Override
    @ForceInline
    public <T extends Shape> FloatVector<T> resize(Species<Float, T> species) {
        return (FloatVector<T>) species.resize(this);
    }

    @Override
    public abstract FloatVector<S> rotateEL(int i);

    @Override
    public abstract FloatVector<S> rotateER(int i);

    @Override
    public abstract FloatVector<S> shiftEL(int i);

    @Override
    public abstract FloatVector<S> shiftER(int i);

    /**
     * Divides this vector by an input vector.
     * <p>
     * This is a vector binary operation where the primitive division
     * operation ({@code /}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the result of dividing this vector by the input vector
     */
    public FloatVector<S> div(Vector<Float,S> v) {
        return bOp(v, (i, a, b) -> (float) (a / b));
    }

    /**
     * Divides this vector by the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the primitive division
     * operation ({@code /}) is applied to lane elements.
     *
     * @param v the input scalar
     * @return the result of dividing this vector by the broadcast of an input
     * scalar
     */
    public abstract FloatVector<S> div(float s);

    /**
     * Divides this vector by an input vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector binary operation where the primitive division
     * operation ({@code /}) is applied to lane elements.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the result of dividing this vector by the input vector
     */
    public FloatVector<S> div(Vector<Float,S> v, Mask<Float, S> m) {
        return bOp(v, m, (i, a, b) -> (float) (a / b));
    }

    /**
     * Divides this vector by the broadcast of an input scalar, selecting lane
     * elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the primitive division
     * operation ({@code /}) is applied to lane elements.
     *
     * @param v the input scalar
     * @param m the mask controlling lane selection
     * @return the result of dividing this vector by the broadcast of an input
     * scalar
     */
    public abstract FloatVector<S> div(float s, Mask<Float, S> m);

// @@@ Many methods are refer to Math or StrictMath functions that only accept
//     double values, what should be the behaviour for lane elements of float
//     vectors? down and then upcast? Or will some numeric algorithms differ?
//     The answers might also depend if strict definitions are required
//     to ensure portability.
//     Leveraging the existing defintions in Math/StrictMath is very convenient
//     but its unclear if it is t

    /**
     * Calculates the square root of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#sqrt} operation
     * is applied to lane elements.
     *
     * @return the square root of this vector
     */
    public FloatVector<S> sqrt() {
        return uOp((i, a) -> (float) Math.sqrt((double) a));
    }

    /**
     * Calculates the square root of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#sqrt} operation
     * ({@code -}) is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the square root of this vector
     */
    public FloatVector<S> sqrt(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.sqrt((double) a));
    }

    /**
     * Calculates the trigonometric tangent of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#tan} operation
     * is applied to lane elements.
     *
     * @return the tangent of this vector
     */
    public FloatVector<S> tan() {
        return uOp((i, a) -> (float) Math.tan((double) a));
    }

    /**
     * Calculates the trigonometric tangent of this vector, selecting lane
     * elements controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#tan} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the tangent of this vector
     */
    public FloatVector<S> tan(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.tan((double) a));
    }

    /**
     * Calculates the hyperbolic tangent of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#tanh} operation
     * is applied to lane elements.
     *
     * @return the hyperbolic tangent of this vector
     */
    public FloatVector<S> tanh() {
        return uOp((i, a) -> (float) Math.tanh((double) a));
    }

    /**
     * Calculates the hyperbolic tangent of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#tanh} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the hyperbolic tangent of this vector
     */
    public FloatVector<S> tanh(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.tanh((double) a));
    }

    /**
     * Calculates the trigonometric sine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#sin} operation
     * is applied to lane elements.
     *
     * @return the sine of this vector
     */
    public FloatVector<S> sin() {
        return uOp((i, a) -> (float) Math.sin((double) a));
    }

    /**
     * Calculates the trigonometric sine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#sin} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the sine of this vector
     */
    public FloatVector<S> sin(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.sin((double) a));
    }

    /**
     * Calculates the hyperbolic sine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#sinh} operation
     * is applied to lane elements.
     *
     * @return the hyperbolic sine of this vector
     */
    public FloatVector<S> sinh() {
        return uOp((i, a) -> (float) Math.sinh((double) a));
    }

    /**
     * Calculates the hyperbolic sine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#sinh} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the hyperbolic sine of this vector
     */
    public FloatVector<S> sinh(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.sinh((double) a));
    }

    /**
     * Calculates the trigonometric cosine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#cos} operation
     * is applied to lane elements.
     *
     * @return the cosine of this vector
     */
    public FloatVector<S> cos() {
        return uOp((i, a) -> (float) Math.cos((double) a));
    }

    /**
     * Calculates the trigonometric cosine of this vector, selecting lane
     * elements controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#cos} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the cosine of this vector
     */
    public FloatVector<S> cos(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.cos((double) a));
    }

    /**
     * Calculates the hyperbolic cosine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#cosh} operation
     * is applied to lane elements.
     *
     * @return the hyperbolic cosine of this vector
     */
    public FloatVector<S> cosh() {
        return uOp((i, a) -> (float) Math.cosh((double) a));
    }

    /**
     * Calculates the hyperbolic cosine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#cosh} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the hyperbolic cosine of this vector
     */
    public FloatVector<S> cosh(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.cosh((double) a));
    }

    /**
     * Calculates the arc sine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#asin} operation
     * is applied to lane elements.
     *
     * @return the arc sine of this vector
     */
    public FloatVector<S> asin() {
        return uOp((i, a) -> (float) Math.asin((double) a));
    }

    /**
     * Calculates the arc sine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#asin} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the arc sine of this vector
     */
    public FloatVector<S> asin(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.asin((double) a));
    }

    /**
     * Calculates the arc cosine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#acos} operation
     * is applied to lane elements.
     *
     * @return the arc cosine of this vector
     */
    public FloatVector<S> acos() {
        return uOp((i, a) -> (float) Math.acos((double) a));
    }

    /**
     * Calculates the arc cosine of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#acos} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the arc cosine of this vector
     */
    public FloatVector<S> acos(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.acos((double) a));
    }

    /**
     * Calculates the arc tangent of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#atan} operation
     * is applied to lane elements.
     *
     * @return the arc tangent of this vector
     */
    public FloatVector<S> atan() {
        return uOp((i, a) -> (float) Math.atan((double) a));
    }

    /**
     * Calculates the arc tangent of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#atan} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the arc tangent of this vector
     */
    public FloatVector<S> atan(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.atan((double) a));
    }

    /**
     * Calculates the arc tangent of this vector divided by an input vector.
     * <p>
     * This is a vector binary operation where the {@link Math#atan2} operation
     * is applied to lane elements.
     *
     * @param v the input vector
     * @return the arc tangent of this vector divided by the input vector
     */
    public FloatVector<S> atan2(Vector<Float,S> v) {
        return bOp(v, (i, a, b) -> (float) Math.atan2((double) a, (double) b));
    }

    /**
     * Calculates the arc tangent of this vector divided by the broadcast of an
     * an input scalar.
     * <p>
     * This is a vector binary operation where the {@link Math#atan2} operation
     * is applied to lane elements.
     *
     * @param s the input scalar
     * @return the arc tangent of this vector over the input vector
     */
    public abstract FloatVector<S> atan2(float s);

    /**
     * Calculates the arc tangent of this vector divided by an input vector,
     * selecting lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the {@link Math#atan2} operation
     * is applied to lane elements.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return the arc tangent of this vector divided by the input vector
     */
    public FloatVector<S> atan2(Vector<Float,S> v, Mask<Float,S> m) {
        return bOp(v, m, (i, a, b) -> (float) Math.atan2((double) a, (double) b));
    }

    /**
     * Calculates the arc tangent of this vector divided by the broadcast of an
     * an input scalar, selecting lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the {@link Math#atan2} operation
     * is applied to lane elements.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return the arc tangent of this vector over the input vector
     */
    public abstract FloatVector<S> atan2(float s, Mask<Float,S> m);

    /**
     * Calculates the cube root of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#cbrt} operation
     * is applied to lane elements.
     *
     * @return the cube root of this vector
     */
    public FloatVector<S> cbrt() {
        return uOp((i, a) -> (float) Math.cbrt((double) a));
    }

    /**
     * Calculates the cube root of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#cbrt} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the cube root of this vector
     */
    public FloatVector<S> cbrt(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.cbrt((double) a));
    }

    /**
     * Calculates the natural logarithm of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#log} operation
     * is applied to lane elements.
     *
     * @return the natural logarithm of this vector
     */
    public FloatVector<S> log() {
        return uOp((i, a) -> (float) Math.log((double) a));
    }

    /**
     * Calculates the natural logarithm of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#log} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the natural logarithm of this vector
     */
    public FloatVector<S> log(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.log((double) a));
    }

    /**
     * Calculates the base 10 logarithm of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#log10} operation
     * is applied to lane elements.
     *
     * @return the base 10 logarithm of this vector
     */
    public FloatVector<S> log10() {
        return uOp((i, a) -> (float) Math.log10((double) a));
    }

    /**
     * Calculates the base 10 logarithm of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#log10} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the base 10 logarithm of this vector
     */
    public FloatVector<S> log10(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.log10((double) a));
    }

    /**
     * Calculates the natural logarithm of the sum of this vector and the
     * broadcast of {@code 1}.
     * <p>
     * This is a vector unary operation where the {@link Math#log1p} operation
     * is applied to lane elements.
     *
     * @return the natural logarithm of the sum of this vector and the broadcast
     * of {@code 1}
     */
    public FloatVector<S> log1p() {
        return uOp((i, a) -> (float) Math.log1p((double) a));
    }

    /**
     * Calculates the natural logarithm of the sum of this vector and the
     * broadcast of {@code 1}, selecting lane elements controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#log1p} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the natural logarithm of the sum of this vector and the broadcast
     * of {@code 1}
     */
    public FloatVector<S> log1p(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.log1p((double) a));
    }

    /**
     * Calculates this vector raised to the power of an input vector.
     * <p>
     * This is a vector binary operation where the {@link Math#pow} operation
     * is applied to lane elements.
     *
     * @param v the input vector
     * @return this vector raised to the power of an input vector
     */
    public FloatVector<S> pow(Vector<Float,S> v) {
        return bOp(v, (i, a, b) -> (float) Math.pow((double) a, (double) b));
    }

    /**
     * Calculates this vector raised to the power of the broadcast of an input
     * scalar.
     * <p>
     * This is a vector binary operation where the {@link Math#pow} operation
     * is applied to lane elements.
     *
     * @param s the input scalar
     * @return this vector raised to the power of the broadcast of an input
     * scalar.
     */
    public abstract FloatVector<S> pow(float s);

    /**
     * Calculates this vector raised to the power of an input vector, selecting
     * lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the {@link Math#pow} operation
     * is applied to lane elements.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return this vector raised to the power of an input vector
     */
    public FloatVector<S> pow(Vector<Float,S> v, Mask<Float,S> m) {
        return bOp(v, m, (i, a, b) -> (float) Math.pow((double) a, (double) b));
    }

    /**
     * Calculates this vector raised to the power of the broadcast of an input
     * scalar, selecting lane elements controlled by a mask.
     * <p>
     * This is a vector binary operation where the {@link Math#pow} operation
     * is applied to lane elements.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return this vector raised to the power of the broadcast of an input
     * scalar.
     */
    public abstract FloatVector<S> pow(float s, Mask<Float,S> m);

    /**
     * Calculates the broadcast of Euler's number {@code e} raised to the power
     * of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#exp} operation
     * is applied to lane elements.
     *
     * @return the broadcast of Euler's number {@code e} raised to the power of
     * this vector
     */
    public FloatVector<S> exp() {
        return uOp((i, a) -> (float) Math.exp((double) a));
    }

    /**
     * Calculates the broadcast of Euler's number {@code e} raised to the power
     * of this vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a vector unary operation where the {@link Math#exp} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the broadcast of Euler's number {@code e} raised to the power of
     * this vector
     */
    public FloatVector<S> exp(Mask<Float,S> m) {
        return uOp(m, (i, a) -> (float) Math.exp((double) a));
    }

    /**
     * Calculates the broadcast of Euler's number {@code e} raised to the power
     * of this vector minus the broadcast of {@code -1}.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.exp().sub(this.species().broadcast(1))
     * }</pre>
     * <p>
     * This is a vector unary operation where the {@link Math#expm1} operation
     * is applied to lane elements.
     *
     * @return the broadcast of Euler's number {@code e} raised to the power of
     * this vector minus the broadcast of {@code -1}
     */
    public FloatVector<S> expm1() {
        return uOp((i, a) -> (float) Math.expm1((double) a));
    }

    /**
     * Calculates the broadcast of Euler's number {@code e} raised to the power
     * of this vector minus the broadcast of {@code -1}, selecting lane elements
     * controlled by a mask
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.exp(m).sub(this.species().broadcast(1), m)
     * }</pre>
     * <p>
     * This is a vector unary operation where the {@link Math#expm1} operation
     * is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the broadcast of Euler's number {@code e} raised to the power of
     * this vector minus the broadcast of {@code -1}
     */
    public FloatVector<S> expm1(Mask<Float,S> m) {
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
     * This is a vector ternary operation where the {@link Math#fma} operation
     * is applied to lane elements.
     *
     * @param v1 the first input vector
     * @param v2 the second input vector
     * @return the product of this vector and the first input vector summed with
     * the second input vector
     */
    public FloatVector<S> fma(Vector<Float,S> v1, Vector<Float,S> v2) {
        return tOp(v1, v2, (i, a, b, c) -> Math.fma(a, b, c));
    }

    /**
     * Calculates the product of this vector and the broadcast of a first input
     * scalar summed with the broadcast of a second input scalar.
     * More specifically as if the following:
     * <pre>{@code
     *   this.fma(this.species().broadcast(s1), this.species().broadcast(s2))
     * }</pre>
     * <p>
     * This is a vector ternary operation where the {@link Math#fma} operation
     * is applied to lane elements.
     *
     * @param s1 the first input scalar
     * @param s2 the second input scalar
     * @return the product of this vector and the broadcast of a first input
     * scalar summed with the broadcast of a second input scalar
     */
    public abstract FloatVector<S> fma(float s1, float s2);

    /**
     * Calculates the product of this vector and a first input vector summed
     * with a second input vector, selecting lane elements controlled by a mask.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(v1, m).add(v2, m)
     * }</pre>
     * <p>
     * This is a vector ternary operation where the {@link Math#fma} operation
     * is applied to lane elements.
     *
     * @param v1 the first input vector
     * @param v2 the second input vector
     * @param m the mask controlling lane selection
     * @return the product of this vector and the first input vector summed with
     * the second input vector
     */
    public FloatVector<S> fma(Vector<Float,S> v1, Vector<Float,S> v2, Mask<Float,S> m) {
        return tOp(v1, v2, m, (i, a, b, c) -> Math.fma(a, b, c));
    }

    /**
     * Calculates the product of this vector and the broadcast of a first input
     * scalar summed with the broadcast of a second input scalar, selecting lane
     * elements controlled by a mask
     * More specifically as if the following:
     * <pre>{@code
     *   this.fma(this.species().broadcast(s1), this.species().broadcast(s2), m)
     * }</pre>
     * <p>
     * This is a vector ternary operation where the {@link Math#fma} operation
     * is applied to lane elements.
     *
     * @param s1 the first input scalar
     * @param s2 the second input scalar
     * @param m the mask controlling lane selection
     * @return the product of this vector and the broadcast of a first input
     * scalar summed with the broadcast of a second input scalar
     */
    public abstract FloatVector<S> fma(float s1, float s2, Mask<Float,S> m);

// Computes the square root of the sum of the squares of x and y

    /**
     * Calculates square root of the sum of the squares of this vector and an
     * input vector.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(this).add(v.mul(v)).sqrt()
     * }</pre>
     * <p>
     * This is a vector binary operation where the {@link Math#hypot} operation
     * is applied to lane elements.
     *
     * @param v the input vector
     * @return square root of the sum of the squares of this vector and an input
     * vector
     */
    public FloatVector<S> hypot(Vector<Float,S> v) {
        return bOp(v, (i, a, b) -> (float) Math.hypot((double) a, (double) b));
    }

    /**
     * Calculates square root of the sum of the squares of this vector and the
     * broadcast of an input scalar.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(this).add(this.species().broadcast(v * v)).sqrt()
     * }</pre>
     * <p>
     * This is a vector binary operation where the {@link Math#hypot} operation
     * is applied to lane elements.
     *
     * @param s the input scalar
     * @return square root of the sum of the squares of this vector and the
     * broadcast of an input scalar
     */
    public abstract FloatVector<S> hypot(float s);

    /**
     * Calculates square root of the sum of the squares of this vector and an
     * input vector, selecting lane elements controlled by a mask.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(this, m).add(v.mul(v), m).sqrt(m)
     * }</pre>
     * <p>
     * This is a vector binary operation where the {@link Math#hypot} operation
     * is applied to lane elements.
     *
     * @param v the input vector
     * @param m the mask controlling lane selection
     * @return square root of the sum of the squares of this vector and an input
     * vector
     */
    public FloatVector<S> hypot(Vector<Float,S> v, Mask<Float,S> m) {
        return bOp(v, m, (i, a, b) -> (float) Math.hypot((double) a, (double) b));
    }

    /**
     * Calculates square root of the sum of the squares of this vector and the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * More specifically as if the following (ignoring any differences in
     * numerical accuracy):
     * <pre>{@code
     *   this.mul(this, m).add(this.species().broadcast(v * v), m).sqrt(m)
     * }</pre>
     * <p>
     * This is a vector binary operation where the {@link Math#hypot} operation
     * is applied to lane elements.
     *
     * @param s the input scalar
     * @param m the mask controlling lane selection
     * @return square root of the sum of the squares of this vector and the
     * broadcast of an input scalar
     */
    public abstract FloatVector<S> hypot(float s, Mask<Float,S> m);


    @Override
    public void intoByteArray(byte[] a, int ix) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        forEach((i, e) -> fb.put(e));
    }

    @Override
    public void intoByteArray(byte[] a, int ix, Mask<Float, S> m) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        forEach((i, e) -> {
            if (m.getElement(i))
                fb.put(e);
            else
                fb.position(fb.position() + 1);
        });
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb) {
        FloatBuffer fb = bb.asFloatBuffer();
        forEach((i, a) -> fb.put(a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, Mask<Float, S> m) {
        FloatBuffer fb = bb.asFloatBuffer();
        forEach((i, a) -> {
            if (m.getElement(i))
                fb.put(a);
            else
                fb.position(fb.position() + 1);
        });
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        bb = bb.duplicate().order(bb.order()).position(ix);
        FloatBuffer fb = bb.asFloatBuffer();
        forEach((i, a) -> fb.put(i, a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Float, S> m) {
        bb = bb.duplicate().order(bb.order()).position(ix);
        FloatBuffer fb = bb.asFloatBuffer();
        forEach(m, (i, a) -> fb.put(i, a));
    }


    // Type specific horizontal reductions

// @@@ For floating point vectors order matters for reproducibility
//     with equivalent sequential reduction. Some order needs to be specified
//     by default. If that default is sequential encounter order then there
//     could be a "go faster" option that is unspecified, essentially giving
//     implementation flexibility at the expense of reproducibility and/or
//     accuracy.
// @@@ Mask versions?

    /**
     * Adds all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the addition
     * operation ({@code +}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the addition of all the lane elements of this vector
     */
    public abstract float addAll();

    /**
     * Subtracts all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the subtraction
     * operation ({@code -}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the subtraction of all the lane elements of this vector
     */
    public abstract float subAll();

    /**
     * Multiplies all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the
     * multiplication operation ({@code *}) is applied to lane elements,
     * and the identity value is {@code 1}.
     *
     * @return the multiplication of all the lane elements of this vector
     */
    public float mulAll() {
        return rOp((float) 1, (i, a, b) -> (float) (a * b));
    }

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> a > b ? b : a} is applied to lane elements,
     * and the identity value is {@link Float.MAX_VALUE}.
     *
     * @return the minimum lane element of this vector
     */
    public float minAll() {
        return rOp(Float.MAX_VALUE, (i, a, b) -> a > b ? b : a);
    }

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> a < b ? b : a} is applied to lane elements,
     * and the identity value is {@link Float.MIN_VALUE}.
     *
     * @return the maximum lane element of this vector
     */
    public float maxAll() {
        return rOp(Float.MIN_VALUE, (i, a, b) -> a < b ? b : a);
    }


    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     */
    public abstract float get(int i);

    /**
     * Replaces the lane element of this vector at lane index {@code i} with
     * value {@code e}.
     * <p>
     * This is a cross-lane operation and behaves it returns the result of
     * blending this vector with an input vector that is the result of
     * broadcasting {@code e} and a mask that has only one lane set at lane
     * index {@code i}.
     *
     * @param i the lane index of the lane element to be replaced
     * @param e the value to be placed
     * @return the result of replacing the lane element of this vector at lane
     * index {@code i} with value {@code e}.
     */
    public abstract FloatVector<S> with(int i, float e);

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
    public float[] toArray() {
        float[] a = new float[species().length()];
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
    public void intoArray(float[] a, int i) {
        forEach((n, e) -> a[i + n] = e);
    }

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
    public void intoArray(float[] a, int i, Mask<Float, S> m) {
        forEach(m, (n, e) -> a[i + n] = e);
    }

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
    public void intoArray(float[] a, int i, int[] indexMap, int j) {
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
    public void intoArray(float[] a, int i, Mask<Float, S> m, int[] indexMap, int j) {
        forEach(m, (n, e) -> a[i + indexMap[j + n]] = e);
    }

    // Species

    @Override
    public abstract FloatSpecies<S> species();

    /**
     * A specialized factory for creating {@link FloatVector} value of the same
     * shape, and a {@link Mask} and {@link Shuffle} values of the same shape
     * and {@code int} element type.
     *
     * @param <S> the type of shape of this species
     */
    public static abstract class FloatSpecies<S extends Vector.Shape> extends Vector.Species<Float, S> {
        interface FOp {
            float apply(int i);
        }

        abstract FloatVector<S> op(FOp f);

        abstract FloatVector<S> op(Mask<Float, S> m, FOp f);

        // Factories

        @Override
        public abstract FloatVector<S> zero();

        /**
         * Returns a vector where all lane elements are set to the primitive
         * value {@code e}.
         *
         * @param e the value
         * @return a vector of vector where all lane elements are set to
         * the primitive value {@code e}
         */
        public abstract FloatVector<S> broadcast(float e);

        /**
         * Returns a vector where the first lane element is set to the primtive
         * value {@code e}, all other lane elements are set to the default
         * value.
         *
         * @param e the value
         * @return a vector where the first lane element is set to the primitive
         * value {@code e}
         */
        public FloatVector<S> single(float e) {
            return op(i -> i == 0 ? e : (float) 0);
        }

        /**
         * Returns a vector where each lane element is set to a randomly
         * generated primitive value.
         * @@@ what are the properties of the random number generator?
         *
         * @return a vector where each lane elements is set to a randomly
         * generated primitive value
         */
        public FloatVector<S> random() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            return op(i -> r.nextFloat());
        }

        /**
         * Returns a vector where each lane element is set to a given
         * primitive value.
         * <p>
         * For each vector lane, where {@code N} is the vector lane index, the
         * the primitive value at index {@code N} is placed into the resulting
         * vector at lane index {@code N}.
         *
         * @@@ What should happen if es.length < this.length() ? use the default
         * value or throw IndexOutOfBoundsException
         *
         * @param es the given primitive values
         * @return a vector where each lane element is set to a given primitive
         * value
         */
        public abstract FloatVector<S> scalars(float... es);

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
        public FloatVector<S> fromArray(float[] a, int i) {
            return op(n -> a[i + n]);
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
        public FloatVector<S> fromArray(float[] a, int i, Mask<Float, S> m) {
            return op(m, n -> a[i + n]);
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
        public FloatVector<S> fromArray(float[] a, int i, int[] indexMap, int j) {
            return op(n -> a[i + indexMap[j + n]]);
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
        public FloatVector<S> fromArray(float[] a, int i, Mask<Float, S> m, int[] indexMap, int j) {
            return op(m, n -> a[i + indexMap[j + n]]);
        }

        @Override
        public FloatVector<S> fromByteArray(byte[] a, int ix) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
            FloatBuffer fb = bb.asFloatBuffer();
            return op(i -> fb.get());
        }

        @Override
        public FloatVector<S> fromByteArray(byte[] a, int ix, Mask<Float, S> m) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
            FloatBuffer fb = bb.asFloatBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (float) 0;
                }
            });
        }

        @Override
        public FloatVector<S> fromByteBuffer(ByteBuffer bb) {
            FloatBuffer fb = bb.asFloatBuffer();
            return op(i -> fb.get());
        }

        @Override
        public FloatVector<S> fromByteBuffer(ByteBuffer bb, Mask<Float, S> m) {
            FloatBuffer fb = bb.asFloatBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (float) 0;
                }
            });
        }

        @Override
        public FloatVector<S> fromByteBuffer(ByteBuffer bb, int ix) {
            bb = bb.duplicate().order(bb.order()).position(ix);
            FloatBuffer fb = bb.asFloatBuffer();
            return op(i -> fb.get(i));
        }

        @Override
        public FloatVector<S> fromByteBuffer(ByteBuffer bb, int ix, Mask<Float, S> m) {
            bb = bb.duplicate().order(bb.order()).position(ix);
            FloatBuffer fb = bb.asFloatBuffer();
            return op(m, i -> fb.get(i));
        }

        @Override
        public <F, T extends Shape> FloatVector<S> reshape(Vector<F, T> o) {
            int blen = Math.max(o.species().bitSize(), bitSize()) / Byte.SIZE;
            ByteBuffer bb = ByteBuffer.allocate(blen).order(ByteOrder.nativeOrder());
            o.intoByteBuffer(bb, 0);
            return fromByteBuffer(bb, 0);
        }

        @Override
        @ForceInline
        public abstract <F> FloatVector<S> rebracket(Vector<F, S> o);

        @Override
        @ForceInline
        public abstract <T extends Shape> FloatVector<S> resize(Vector<Float, T> o);

        @Override
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> FloatVector<S> cast(Vector<F, T> v) {
            // Allocate array of required size
            float[] a = new float[length()];

            Class<?> vtype = v.species().elementType();
            int limit = Math.min(v.species().length(), length());
            if (vtype == byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == int.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else if (vtype == double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (float) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
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
    @SuppressWarnings("unchecked")
    public static FloatSpecies<?> preferredSpecies() {
        return (FloatSpecies<?>) Vector.preferredSpecies(float.class);
    }

    /**
     * Finds a species for an element type of {@code float} and shape.
     *
     * @param s the shape
     * @param <S> the type of shape
     * @return a species for an element type of {@code float} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    @SuppressWarnings("unchecked")
    public static <S extends Shape> FloatSpecies<S> species(S s) {
        Objects.requireNonNull(s);
        if (s == Shapes.S_64_BIT) {
            return (FloatSpecies<S>) Float64Vector.SPECIES;
        } else if (s == Shapes.S_128_BIT) {
            return (FloatSpecies<S>) Float128Vector.SPECIES;
        } else if (s == Shapes.S_256_BIT) {
            return (FloatSpecies<S>) Float256Vector.SPECIES;
        } else if (s == Shapes.S_512_BIT) {
            return (FloatSpecies<S>) Float512Vector.SPECIES;
        } else {
            throw new IllegalArgumentException("Bad shape: " + s);
        }
    }
}
