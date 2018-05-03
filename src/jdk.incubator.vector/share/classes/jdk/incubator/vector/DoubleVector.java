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
import java.nio.DoubleBuffer;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;


/**
 * A specialized {@link Vector} representing an ordered immutable sequence of
 * {@code double} values.
 *
 * @param <S> the type of shape of this vector
 */
@SuppressWarnings("cast")
public abstract class DoubleVector<S extends Vector.Shape> extends Vector<Double,S> {

    DoubleVector() {}

    // Unary operator

    interface FUnOp {
        double apply(int i, double a);
    }

    abstract DoubleVector<S> uOp(FUnOp f);

    abstract DoubleVector<S> uOp(Mask<Double, S> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        double apply(int i, double a, double b);
    }

    abstract DoubleVector<S> bOp(Vector<Double,S> v, FBinOp f);

    abstract DoubleVector<S> bOp(Vector<Double,S> v, Mask<Double, S> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        double apply(int i, double a, double b, double c);
    }

    abstract DoubleVector<S> tOp(Vector<Double,S> v1, Vector<Double,S> v2, FTriOp f);

    abstract DoubleVector<S> tOp(Vector<Double,S> v1, Vector<Double,S> v2, Mask<Double, S> m, FTriOp f);

    // Reduction operator

    abstract double rOp(double v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, double a, double b);
    }

    abstract Mask<Double, S> bTest(Vector<Double,S> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, double a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Double, S> m, FUnCon f);

    //

    @Override
    public abstract DoubleVector<S> add(Vector<Double,S> v);

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
    public abstract DoubleVector<S> add(double s);

    @Override
    public DoubleVector<S> add(Vector<Double,S> v, Mask<Double, S> m) {
        return bOp(v, m, (i, a, b) -> (double) (a + b));
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
    public abstract DoubleVector<S> add(double s, Mask<Double, S> m);

    @Override
    public abstract DoubleVector<S> sub(Vector<Double,S> v);

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
    public abstract DoubleVector<S> sub(double s);

    @Override
    public DoubleVector<S> sub(Vector<Double,S> v, Mask<Double, S> m) {
        return bOp(v, m, (i, a, b) -> (double) (a - b));
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
    public abstract DoubleVector<S> sub(double s, Mask<Double, S> m);

    @Override
    public abstract DoubleVector<S> mul(Vector<Double,S> v);

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
    public abstract DoubleVector<S> mul(double s);

    @Override
    public DoubleVector<S> mul(Vector<Double,S> v, Mask<Double, S> m) {
        return bOp(v, m, (i, a, b) -> (double) (a * b));
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
    public abstract DoubleVector<S> mul(double s, Mask<Double, S> m);

    @Override
    public abstract DoubleVector<S> neg();

    @Override
    public DoubleVector<S> neg(Mask<Double, S> m) {
        return uOp(m, (i, a) -> (double) (-a));
    }

    @Override
    public abstract DoubleVector<S> abs();

    @Override
    public DoubleVector<S> abs(Mask<Double, S> m) {
        return uOp(m, (i, a) -> (double) Math.abs(a));
    }

    @Override
    public abstract DoubleVector<S> min(Vector<Double,S> v);

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> a < b ? a : b}  is applied to lane elements.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract DoubleVector<S> min(double s);

    @Override
    public abstract DoubleVector<S> max(Vector<Double,S> v);

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> a > b ? a : b}  is applied to lane elements.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract DoubleVector<S> max(double s);

    @Override
    public abstract Mask<Double, S> equal(Vector<Double,S> v);

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
    public abstract Mask<Double, S> equal(double s);

    @Override
    public abstract Mask<Double, S> notEqual(Vector<Double,S> v);

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
    public abstract Mask<Double, S> notEqual(double s);

    @Override
    public abstract Mask<Double, S> lessThan(Vector<Double,S> v);

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
    public abstract Mask<Double, S> lessThan(double s);

    @Override
    public abstract Mask<Double, S> lessThanEq(Vector<Double,S> v);

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
    public abstract Mask<Double, S> lessThanEq(double s);

    @Override
    public abstract Mask<Double, S> greaterThan(Vector<Double,S> v);

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
    public abstract Mask<Double, S> greaterThan(double s);

    @Override
    public abstract Mask<Double, S> greaterThanEq(Vector<Double,S> v);

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
    public abstract Mask<Double, S> greaterThanEq(double s);

    @Override
    public abstract DoubleVector<S> blend(Vector<Double,S> v, Mask<Double, S> m);

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
    public abstract DoubleVector<S> blend(double s, Mask<Double, S> m);

    @Override
    public abstract DoubleVector<S> shuffle(Vector<Double,S> v, Shuffle<Double, S> m);

    @Override
    public abstract DoubleVector<S> swizzle(Shuffle<Double, S> m);

    @Override
    @ForceInline
    public <T extends Shape> DoubleVector<T> resize(Species<Double, T> species) {
        return (DoubleVector<T>) species.resize(this);
    }

    @Override
    public abstract DoubleVector<S> rotateEL(int i);

    @Override
    public abstract DoubleVector<S> rotateER(int i);

    @Override
    public abstract DoubleVector<S> shiftEL(int i);

    @Override
    public abstract DoubleVector<S> shiftER(int i);

    /**
     * Divides this vector by an input vector.
     * <p>
     * This is a vector binary operation where the primitive division
     * operation ({@code /}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the result of dividing this vector by the input vector
     */
    public DoubleVector<S> div(Vector<Double,S> v) {
        return bOp(v, (i, a, b) -> (double) (a / b));
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
    public abstract DoubleVector<S> div(double s);

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
    public DoubleVector<S> div(Vector<Double,S> v, Mask<Double, S> m) {
        return bOp(v, m, (i, a, b) -> (double) (a / b));
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
    public abstract DoubleVector<S> div(double s, Mask<Double, S> m);

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
    public DoubleVector<S> sqrt() {
        return uOp((i, a) -> (double) Math.sqrt((double) a));
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
    public DoubleVector<S> sqrt(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.sqrt((double) a));
    }

    /**
     * Calculates the trigonometric tangent of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#tan} operation
     * is applied to lane elements.
     *
     * @return the tangent of this vector
     */
    public DoubleVector<S> tan() {
        return uOp((i, a) -> (double) Math.tan((double) a));
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
    public DoubleVector<S> tan(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.tan((double) a));
    }

    /**
     * Calculates the hyperbolic tangent of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#tanh} operation
     * is applied to lane elements.
     *
     * @return the hyperbolic tangent of this vector
     */
    public DoubleVector<S> tanh() {
        return uOp((i, a) -> (double) Math.tanh((double) a));
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
    public DoubleVector<S> tanh(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.tanh((double) a));
    }

    /**
     * Calculates the trigonometric sine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#sin} operation
     * is applied to lane elements.
     *
     * @return the sine of this vector
     */
    public DoubleVector<S> sin() {
        return uOp((i, a) -> (double) Math.sin((double) a));
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
    public DoubleVector<S> sin(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.sin((double) a));
    }

    /**
     * Calculates the hyperbolic sine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#sinh} operation
     * is applied to lane elements.
     *
     * @return the hyperbolic sine of this vector
     */
    public DoubleVector<S> sinh() {
        return uOp((i, a) -> (double) Math.sinh((double) a));
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
    public DoubleVector<S> sinh(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.sinh((double) a));
    }

    /**
     * Calculates the trigonometric cosine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#cos} operation
     * is applied to lane elements.
     *
     * @return the cosine of this vector
     */
    public DoubleVector<S> cos() {
        return uOp((i, a) -> (double) Math.cos((double) a));
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
    public DoubleVector<S> cos(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.cos((double) a));
    }

    /**
     * Calculates the hyperbolic cosine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#cosh} operation
     * is applied to lane elements.
     *
     * @return the hyperbolic cosine of this vector
     */
    public DoubleVector<S> cosh() {
        return uOp((i, a) -> (double) Math.cosh((double) a));
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
    public DoubleVector<S> cosh(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.cosh((double) a));
    }

    /**
     * Calculates the arc sine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#asin} operation
     * is applied to lane elements.
     *
     * @return the arc sine of this vector
     */
    public DoubleVector<S> asin() {
        return uOp((i, a) -> (double) Math.asin((double) a));
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
    public DoubleVector<S> asin(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.asin((double) a));
    }

    /**
     * Calculates the arc cosine of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#acos} operation
     * is applied to lane elements.
     *
     * @return the arc cosine of this vector
     */
    public DoubleVector<S> acos() {
        return uOp((i, a) -> (double) Math.acos((double) a));
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
    public DoubleVector<S> acos(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.acos((double) a));
    }

    /**
     * Calculates the arc tangent of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#atan} operation
     * is applied to lane elements.
     *
     * @return the arc tangent of this vector
     */
    public DoubleVector<S> atan() {
        return uOp((i, a) -> (double) Math.atan((double) a));
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
    public DoubleVector<S> atan(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.atan((double) a));
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
    public DoubleVector<S> atan2(Vector<Double,S> v) {
        return bOp(v, (i, a, b) -> (double) Math.atan2((double) a, (double) b));
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
    public abstract DoubleVector<S> atan2(double s);

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
    public DoubleVector<S> atan2(Vector<Double,S> v, Mask<Double,S> m) {
        return bOp(v, m, (i, a, b) -> (double) Math.atan2((double) a, (double) b));
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
    public abstract DoubleVector<S> atan2(double s, Mask<Double,S> m);

    /**
     * Calculates the cube root of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#cbrt} operation
     * is applied to lane elements.
     *
     * @return the cube root of this vector
     */
    public DoubleVector<S> cbrt() {
        return uOp((i, a) -> (double) Math.cbrt((double) a));
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
    public DoubleVector<S> cbrt(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.cbrt((double) a));
    }

    /**
     * Calculates the natural logarithm of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#log} operation
     * is applied to lane elements.
     *
     * @return the natural logarithm of this vector
     */
    public DoubleVector<S> log() {
        return uOp((i, a) -> (double) Math.log((double) a));
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
    public DoubleVector<S> log(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.log((double) a));
    }

    /**
     * Calculates the base 10 logarithm of this vector.
     * <p>
     * This is a vector unary operation where the {@link Math#log10} operation
     * is applied to lane elements.
     *
     * @return the base 10 logarithm of this vector
     */
    public DoubleVector<S> log10() {
        return uOp((i, a) -> (double) Math.log10((double) a));
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
    public DoubleVector<S> log10(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.log10((double) a));
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
    public DoubleVector<S> log1p() {
        return uOp((i, a) -> (double) Math.log1p((double) a));
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
    public DoubleVector<S> log1p(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.log1p((double) a));
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
    public DoubleVector<S> pow(Vector<Double,S> v) {
        return bOp(v, (i, a, b) -> (double) Math.pow((double) a, (double) b));
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
    public abstract DoubleVector<S> pow(double s);

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
    public DoubleVector<S> pow(Vector<Double,S> v, Mask<Double,S> m) {
        return bOp(v, m, (i, a, b) -> (double) Math.pow((double) a, (double) b));
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
    public abstract DoubleVector<S> pow(double s, Mask<Double,S> m);

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
    public DoubleVector<S> exp() {
        return uOp((i, a) -> (double) Math.exp((double) a));
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
    public DoubleVector<S> exp(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.exp((double) a));
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
    public DoubleVector<S> expm1() {
        return uOp((i, a) -> (double) Math.expm1((double) a));
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
    public DoubleVector<S> expm1(Mask<Double,S> m) {
        return uOp(m, (i, a) -> (double) Math.expm1((double) a));
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
    public DoubleVector<S> fma(Vector<Double,S> v1, Vector<Double,S> v2) {
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
    public abstract DoubleVector<S> fma(double s1, double s2);

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
    public DoubleVector<S> fma(Vector<Double,S> v1, Vector<Double,S> v2, Mask<Double,S> m) {
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
    public abstract DoubleVector<S> fma(double s1, double s2, Mask<Double,S> m);

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
    public DoubleVector<S> hypot(Vector<Double,S> v) {
        return bOp(v, (i, a, b) -> (double) Math.hypot((double) a, (double) b));
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
    public abstract DoubleVector<S> hypot(double s);

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
    public DoubleVector<S> hypot(Vector<Double,S> v, Mask<Double,S> m) {
        return bOp(v, m, (i, a, b) -> (double) Math.hypot((double) a, (double) b));
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
    public abstract DoubleVector<S> hypot(double s, Mask<Double,S> m);


    @Override
    public void intoByteArray(byte[] a, int ix) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
        DoubleBuffer fb = bb.asDoubleBuffer();
        forEach((i, e) -> fb.put(e));
    }

    @Override
    public void intoByteArray(byte[] a, int ix, Mask<Double, S> m) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
        DoubleBuffer fb = bb.asDoubleBuffer();
        forEach((i, e) -> {
            if (m.getElement(i))
                fb.put(e);
            else
                fb.position(fb.position() + 1);
        });
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb) {
        DoubleBuffer fb = bb.asDoubleBuffer();
        forEach((i, a) -> fb.put(a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, Mask<Double, S> m) {
        DoubleBuffer fb = bb.asDoubleBuffer();
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
        DoubleBuffer fb = bb.asDoubleBuffer();
        forEach((i, a) -> fb.put(i, a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Double, S> m) {
        bb = bb.duplicate().order(bb.order()).position(ix);
        DoubleBuffer fb = bb.asDoubleBuffer();
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
    public abstract double addAll();

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
    public abstract double addAll(Mask<Double, S> m);

    /**
     * Subtracts all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the subtraction
     * operation ({@code -}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the subtraction of all the lane elements of this vector
     */
    public abstract double subAll();

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
    public abstract double subAll(Mask<Double, S> m);

    /**
     * Multiplies all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the
     * multiplication operation ({@code *}) is applied to lane elements,
     * and the identity value is {@code 1}.
     *
     * @return the multiplication of all the lane elements of this vector
     */
    public double mulAll() {
        return rOp((double) 1, (i, a, b) -> (double) (a * b));
    }

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
    public abstract double mulAll(Mask<Double, S> m);

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> a > b ? b : a} is applied to lane elements,
     * and the identity value is {@link Double.MAX_VALUE}.
     *
     * @return the minimum lane element of this vector
     */
    public abstract double minAll();

    /**
     * Returns the minimum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> a > b ? b : a} is applied to lane elements,
     * and the identity value is {@link Double.MAX_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the minimum lane element of this vector
     */
    public abstract double minAll(Mask<Double, S> m);

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> a < b ? b : a} is applied to lane elements,
     * and the identity value is {@link Double.MIN_VALUE}.
     *
     * @return the maximum lane element of this vector
     */
    public abstract double maxAll();

    /**
     * Returns the maximum lane element of this vector, selecting lane elements
     * controlled by a mask.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> a < b ? b : a} is applied to lane elements,
     * and the identity value is {@link Double.MIN_VALUE}.
     *
     * @param m the mask controlling lane selection
     * @return the maximum lane element of this vector
     */
    public abstract double maxAll(Mask<Double, S> m);


    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract double get(int i);

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
    public abstract DoubleVector<S> with(int i, double e);

    // Type specific extractors

    /**
     * Returns an array containing the lane elements of this vector.
     * <p>
     * This method behaves as if it {@link #intoArray(double[], int)} stores}
     * this vector into an allocated array and returns the array as follows:
     * <pre>{@code
     *   double[] a = new double[this.length()];
     *   this.intoArray(a, 0);
     *   return a;
     * }</pre>
     *
     * @return an array containing the the lane elements of this vector
     */
    @ForceInline
    public double[] toArray() {
        double[] a = new double[species().length()];
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
    public void intoArray(double[] a, int i) {
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
    public void intoArray(double[] a, int i, Mask<Double, S> m) {
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
    public void intoArray(double[] a, int i, int[] indexMap, int j) {
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
    public void intoArray(double[] a, int i, Mask<Double, S> m, int[] indexMap, int j) {
        forEach(m, (n, e) -> a[i + indexMap[j + n]] = e);
    }

    // Species

    @Override
    public abstract DoubleSpecies<S> species();

    /**
     * A specialized factory for creating {@link DoubleVector} value of the same
     * shape, and a {@link Mask} and {@link Shuffle} values of the same shape
     * and {@code int} element type.
     *
     * @param <S> the type of shape of this species
     */
    public static abstract class DoubleSpecies<S extends Vector.Shape> extends Vector.Species<Double, S> {
        interface FOp {
            double apply(int i);
        }

        abstract DoubleVector<S> op(FOp f);

        abstract DoubleVector<S> op(Mask<Double, S> m, FOp f);

        // Factories

        @Override
        public abstract DoubleVector<S> zero();

        /**
         * Returns a vector where all lane elements are set to the primitive
         * value {@code e}.
         *
         * @param e the value
         * @return a vector of vector where all lane elements are set to
         * the primitive value {@code e}
         */
        public abstract DoubleVector<S> broadcast(double e);

        /**
         * Returns a vector where the first lane element is set to the primtive
         * value {@code e}, all other lane elements are set to the default
         * value.
         *
         * @param e the value
         * @return a vector where the first lane element is set to the primitive
         * value {@code e}
         */
        public DoubleVector<S> single(double e) {
            return op(i -> i == 0 ? e : (double) 0);
        }

        /**
         * Returns a vector where each lane element is set to a randomly
         * generated primitive value.
         * @@@ what are the properties of the random number generator?
         *
         * @return a vector where each lane elements is set to a randomly
         * generated primitive value
         */
        public DoubleVector<S> random() {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            return op(i -> r.nextDouble());
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
        public abstract DoubleVector<S> scalars(double... es);

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
        public DoubleVector<S> fromArray(double[] a, int i) {
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
        public DoubleVector<S> fromArray(double[] a, int i, Mask<Double, S> m) {
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
        public DoubleVector<S> fromArray(double[] a, int i, int[] indexMap, int j) {
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
        public DoubleVector<S> fromArray(double[] a, int i, Mask<Double, S> m, int[] indexMap, int j) {
            return op(m, n -> a[i + indexMap[j + n]]);
        }

        @Override
        public DoubleVector<S> fromByteArray(byte[] a, int ix) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
            DoubleBuffer fb = bb.asDoubleBuffer();
            return op(i -> fb.get());
        }

        @Override
        public DoubleVector<S> fromByteArray(byte[] a, int ix, Mask<Double, S> m) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
            DoubleBuffer fb = bb.asDoubleBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (double) 0;
                }
            });
        }

        @Override
        public DoubleVector<S> fromByteBuffer(ByteBuffer bb) {
            DoubleBuffer fb = bb.asDoubleBuffer();
            return op(i -> fb.get());
        }

        @Override
        public DoubleVector<S> fromByteBuffer(ByteBuffer bb, Mask<Double, S> m) {
            DoubleBuffer fb = bb.asDoubleBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (double) 0;
                }
            });
        }

        @Override
        public DoubleVector<S> fromByteBuffer(ByteBuffer bb, int ix) {
            bb = bb.duplicate().order(bb.order()).position(ix);
            DoubleBuffer fb = bb.asDoubleBuffer();
            return op(i -> fb.get(i));
        }

        @Override
        public DoubleVector<S> fromByteBuffer(ByteBuffer bb, int ix, Mask<Double, S> m) {
            bb = bb.duplicate().order(bb.order()).position(ix);
            DoubleBuffer fb = bb.asDoubleBuffer();
            return op(m, i -> fb.get(i));
        }

        @Override
        public <F, T extends Shape> DoubleVector<S> reshape(Vector<F, T> o) {
            int blen = Math.max(o.species().bitSize(), bitSize()) / Byte.SIZE;
            ByteBuffer bb = ByteBuffer.allocate(blen).order(ByteOrder.nativeOrder());
            o.intoByteBuffer(bb, 0);
            return fromByteBuffer(bb, 0);
        }

        @Override
        @ForceInline
        public abstract <F> DoubleVector<S> rebracket(Vector<F, S> o);

        @Override
        @ForceInline
        public abstract <T extends Shape> DoubleVector<S> resize(Vector<Double, T> o);

        @Override
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> DoubleVector<S> cast(Vector<F, T> v) {
            // Allocate array of required size
            double[] a = new double[length()];

            Class<?> vtype = v.species().elementType();
            int limit = Math.min(v.species().length(), length());
            if (vtype == byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == int.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else if (vtype == double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (double) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
        }

    }

    /**
     * Finds the preferred species for an element type of {@code double}.
     * <p>
     * A preferred species is a species chosen by the platform that has a
     * shape of maximal bit size.  A preferred species for different element
     * types will have the same shape, and therefore vectors, masks, and
     * shuffles created from such species will be shape compatible.
     *
     * @return the preferred species for an element type of {@code double}
     */
    @SuppressWarnings("unchecked")
    public static DoubleSpecies<?> preferredSpecies() {
        return (DoubleSpecies<?>) Vector.preferredSpecies(double.class);
    }

    /**
     * Finds a species for an element type of {@code double} and shape.
     *
     * @param s the shape
     * @param <S> the type of shape
     * @return a species for an element type of {@code double} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    @SuppressWarnings("unchecked")
    public static <S extends Shape> DoubleSpecies<S> species(S s) {
        Objects.requireNonNull(s);
        if (s == Shapes.S_64_BIT) {
            return (DoubleSpecies<S>) Double64Vector.SPECIES;
        } else if (s == Shapes.S_128_BIT) {
            return (DoubleSpecies<S>) Double128Vector.SPECIES;
        } else if (s == Shapes.S_256_BIT) {
            return (DoubleSpecies<S>) Double256Vector.SPECIES;
        } else if (s == Shapes.S_512_BIT) {
            return (DoubleSpecies<S>) Double512Vector.SPECIES;
        } else {
            throw new IllegalArgumentException("Bad shape: " + s);
        }
    }
}
