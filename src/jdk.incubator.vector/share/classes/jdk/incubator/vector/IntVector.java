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
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;


/**
 * A specialized {@link Vector} representing an ordered immutable sequence of
 * {@code int} values.
 *
 * @param <S> the type of shape of this vector
 */
@SuppressWarnings("cast")
public abstract class IntVector<S extends Vector.Shape> extends Vector<Integer,S> {

    IntVector() {}

    // Unary operator

    interface FUnOp {
        int apply(int i, int a);
    }

    abstract IntVector<S> uOp(FUnOp f);

    abstract IntVector<S> uOp(Mask<Integer, S> m, FUnOp f);

    // Binary operator

    interface FBinOp {
        int apply(int i, int a, int b);
    }

    abstract IntVector<S> bOp(Vector<Integer,S> v, FBinOp f);

    abstract IntVector<S> bOp(Vector<Integer,S> v, Mask<Integer, S> m, FBinOp f);

    // Trinary operator

    interface FTriOp {
        int apply(int i, int a, int b, int c);
    }

    abstract IntVector<S> tOp(Vector<Integer,S> v1, Vector<Integer,S> v2, FTriOp f);

    abstract IntVector<S> tOp(Vector<Integer,S> v1, Vector<Integer,S> v2, Mask<Integer, S> m, FTriOp f);

    // Reduction operator

    abstract int rOp(int v, FBinOp f);

    // Binary test

    interface FBinTest {
        boolean apply(int i, int a, int b);
    }

    abstract Mask<Integer, S> bTest(Vector<Integer,S> v, FBinTest f);

    // Foreach

    interface FUnCon {
        void apply(int i, int a);
    }

    abstract void forEach(FUnCon f);

    abstract void forEach(Mask<Integer, S> m, FUnCon f);

    //

    @Override
    public IntVector<S> add(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) (a + b));
    }

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
    public abstract IntVector<S> add(int s);

    @Override
    public IntVector<S> add(Vector<Integer,S> v, Mask<Integer, S> m) {
        return bOp(v, m, (i, a, b) -> (int) (a + b));
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
    public abstract IntVector<S> add(int s, Mask<Integer, S> m);

    @Override
    public IntVector<S> addSaturate(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract IntVector<S> addSaturate(int s);

    @Override
    public IntVector<S> addSaturate(Vector<Integer,S> v, Mask<Integer, S> m) {
        return bOp(v, m, (i, a, b) -> (int) ((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b));
    }

    public abstract IntVector<S> addSaturate(int s, Mask<Integer, S> m);

    @Override
    public IntVector<S> sub(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) (a - b));
    }

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
    public abstract IntVector<S> sub(int s);

    @Override
    public IntVector<S> sub(Vector<Integer,S> v, Mask<Integer, S> m) {
        return bOp(v, m, (i, a, b) -> (int) (a - b));
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
    public abstract IntVector<S> sub(int s, Mask<Integer, S> m);

    @Override
    public IntVector<S> subSaturate(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) ((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b));
    }

    public abstract IntVector<S> subSaturate(int s);

    @Override
    public IntVector<S> subSaturate(Vector<Integer,S> v, Mask<Integer, S> m) {
        return bOp(v, m, (i, a, b) -> (int) ((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b));
    }

    public abstract IntVector<S> subSaturate(int s, Mask<Integer, S> m);

    @Override
    public IntVector<S> mul(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) (a * b));
    }

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
    public abstract IntVector<S> mul(int s);

    @Override
    public IntVector<S> mul(Vector<Integer,S> v, Mask<Integer, S> m) {
        return bOp(v, m, (i, a, b) -> (int) (a * b));
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
    public abstract IntVector<S> mul(int s, Mask<Integer, S> m);

    @Override
    public IntVector<S> neg() {
        return uOp((i, a) -> (int) (-a));
    }

    @Override
    public IntVector<S> neg(Mask<Integer, S> m) {
        return uOp(m, (i, a) -> (int) (-a));
    }

    @Override
    public IntVector<S> abs() {
        return uOp((i, a) -> (int) Math.abs(a));
    }

    @Override
    public IntVector<S> abs(Mask<Integer, S> m) {
        return uOp(m, (i, a) -> (int) Math.abs(a));
    }

    @Override
    public IntVector<S> min(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (a < b) ? a : b);
    }

    /**
     * Returns the minimum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> a < b ? a : b}  is applied to lane elements.
     *
     * @param s the input scalar
     * @return the minimum of this vector and the broadcast of an input scalar
     */
    public abstract IntVector<S> min(int s);

    @Override
    public IntVector<S> max(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (a > b) ? a : b);
    }

    /**
     * Returns the maximum of this vector and the broadcast of an input scalar.
     * <p>
     * This is a vector binary operation where the operation
     * {@code (a, b) -> a > b ? a : b}  is applied to lane elements.
     *
     * @param s the input scalar
     * @return the maximum of this vector and the broadcast of an input scalar
     */
    public abstract IntVector<S> max(int s);

    @Override
    public Mask<Integer, S> equal(Vector<Integer,S> v) {
        return bTest(v, (i, a, b) -> a == b);
    }

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
    public abstract Mask<Integer, S> equal(int s);

    @Override
    public Mask<Integer, S> notEqual(Vector<Integer,S> v) {
        return bTest(v, (i, a, b) -> a != b);
    }

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
    public abstract Mask<Integer, S> notEqual(int s);

    @Override
    public Mask<Integer, S> lessThan(Vector<Integer,S> v) {
        return bTest(v, (i, a, b) -> a < b);
    }

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
    public abstract Mask<Integer, S> lessThan(int s);

    @Override
    public Mask<Integer, S> lessThanEq(Vector<Integer,S> v) {
        return bTest(v, (i, a, b) -> a <= b);
    }

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
    public abstract Mask<Integer, S> lessThanEq(int s);

    @Override
    public Mask<Integer, S> greaterThan(Vector<Integer,S> v) {
        return bTest(v, (i, a, b) -> a > b);
    }

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
    public abstract Mask<Integer, S> greaterThan(int s);

    @Override
    public Mask<Integer, S> greaterThanEq(Vector<Integer,S> v) {
        return bTest(v, (i, a, b) -> a >= b);
    }

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
    public abstract Mask<Integer, S> greaterThanEq(int s);

    @Override
    public IntVector<S> blend(Vector<Integer,S> v, Mask<Integer, S> m) {
        return bOp(v, (i, a, b) -> m.getElement(i) ? b : a);
    }

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
    public abstract IntVector<S> blend(int s, Mask<Integer, S> m);

    @Override
    public abstract IntVector<S> shuffle(Vector<Integer,S> v, Shuffle<Integer, S> m);

    @Override
    public abstract IntVector<S> swizzle(Shuffle<Integer, S> m);

    @Override
    @ForceInline
    public <T extends Shape> IntVector<T> resize(Species<Integer, T> species) {
        return (IntVector<T>) species.resize(this);
    }

    @Override
    public abstract IntVector<S> rotateEL(int i);

    @Override
    public abstract IntVector<S> rotateER(int i);

    @Override
    public abstract IntVector<S> shiftEL(int i);

    @Override
    public abstract IntVector<S> shiftER(int i);



    /**
     * Bitwise ANDs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise AND
     * operation ({@code &}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise AND of this vector with the input vector
     */
    public IntVector<S> and(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) (a & b));
    }

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
    public abstract IntVector<S> and(int s);

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
    public IntVector<S> and(Vector<Integer,S> v, Mask<Integer, S> m) {
        return bOp(v, m, (i, a, b) -> (int) (a & b));
    }

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
    public abstract IntVector<S> and(int s, Mask<Integer, S> m);

    /**
     * Bitwise ORs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise OR
     * operation ({@code |}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise OR of this vector with the input vector
     */
    public IntVector<S> or(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) (a | b));
    }

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
    public abstract IntVector<S> or(int s);

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
    public IntVector<S> or(Vector<Integer,S> v, Mask<Integer, S> m) {
        return bOp(v, m, (i, a, b) -> (int) (a | b));
    }

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
    public abstract IntVector<S> or(int s, Mask<Integer, S> m);

    /**
     * Bitwise XORs this vector with an input vector.
     * <p>
     * This is a vector binary operation where the primitive bitwise XOR
     * operation ({@code ^}) is applied to lane elements.
     *
     * @param v the input vector
     * @return the bitwise XOR of this vector with the input vector
     */
    public IntVector<S> xor(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) (a ^ b));
    }

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
    public abstract IntVector<S> xor(int s);

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
    public IntVector<S> xor(Vector<Integer,S> v, Mask<Integer, S> m) {
        return bOp(v, m, (i, a, b) -> (int) (a ^ b));
    }

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
    public abstract IntVector<S> xor(int s, Mask<Integer, S> m);

    /**
     * Bitwise NOTs this vector.
     * <p>
     * This is a vector unary operation where the primitive bitwise NOT
     * operation ({@code ~}) is applied to lane elements.
     *
     * @return the bitwise NOT of this vector
     */
    public IntVector<S> not() {
        return uOp((i, a) -> (int) (~a));
    }

    /**
     * Bitwise NOTs this vector, selecting lane elements controlled by a mask.
     * <p>
     * This is a vector unary operation where the primitive bitwise NOT
     * operation ({@code ~}) is applied to lane elements.
     *
     * @param m the mask controlling lane selection
     * @return the bitwise NOT of this vector
     */
    public IntVector<S> not(Mask<Integer, S> m) {
        return uOp(m, (i, a) -> (int) (~a));
    }

/*
@@@ Check the shift operations against the JLS definition and vector
    instructions.

    How to define byte and short? in Java the values are promoted
    to int values.

    For int values (promoted or otherwise) the low 5 bits of s are used.
    For long values the low 6 bits of s are used.
 */

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
    public IntVector<S> shiftL(int s) {
        return uOp((i, a) -> (int) (a << s));
    }

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
    public IntVector<S> shiftL(int s, Mask<Integer, S> m) {
        return uOp(m, (i, a) -> (int) (a << s));
    }

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
    public IntVector<S> shiftL(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) (a << b));
    }

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
    public IntVector<S> shiftL(Vector<Integer,S> v, Mask<Integer, S> m) {
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
    public IntVector<S> shiftR(int s) {
        return uOp((i, a) -> (int) (a >>> s));
    }

    /**
     * Logically right shifts (or unsigned right shifts) this vector by the
     * broadcast of an input scalar, selecting lane elements controlled by a
     * mask.
     * <p>
     * This is a vector binary operation where the primitive logical right shift
     * operation ({@code >>>}) is applied to lane elements.
     *
     * @param s the input scalar; the number of the bits to right shift
     * @return the result of logically right shifting this vector by the
     * broadcast of an input scalar
     */
    public IntVector<S> shiftR(int s, Mask<Integer, S> m) {
        return uOp(m, (i, a) -> (int) (a >>> s));
    }

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
    public IntVector<S> shiftR(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) (a >>> b));
    }

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
    public IntVector<S> shiftR(Vector<Integer,S> v, Mask<Integer, S> m) {
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
    public IntVector<S> aShiftR(int s) {
        return uOp((i, a) -> (int) (a >> s));
    }

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
    public IntVector<S> aShiftR(int s, Mask<Integer, S> m) {
        return uOp(m, (i, a) -> (int) (a >> s));
    }

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
    public IntVector<S> ashiftR(Vector<Integer,S> v) {
        return bOp(v, (i, a, b) -> (int) (a >> b));
    }

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
    public IntVector<S> ashiftR(Vector<Integer,S> v, Mask<Integer, S> m) {
        return bOp(v, m, (i, a, b) -> (int) (a >> b));
    }

// @@@ Should the rotateL/R operations be on int and long only?
//     Currently short and byte promote to int, bits rotated, then downcast,
//     thus bits will be truncated and not correctly rotated
// @@@ Mask versions?

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
    public IntVector<S> rotateL(int s) {
        return uOp((i, a) -> (int) Integer.rotateLeft(a, s));
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
    public IntVector<S> rotateR(int s) {
        return uOp((i, a) -> (int) Integer.rotateRight(a, s));
    }

    @Override
    public void intoByteArray(byte[] a, int ix) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
        IntBuffer fb = bb.asIntBuffer();
        forEach((i, e) -> fb.put(e));
    }

    @Override
    public void intoByteArray(byte[] a, int ix, Mask<Integer, S> m) {
        ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
        IntBuffer fb = bb.asIntBuffer();
        forEach((i, e) -> {
            if (m.getElement(i))
                fb.put(e);
            else
                fb.position(fb.position() + 1);
        });
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb) {
        IntBuffer fb = bb.asIntBuffer();
        forEach((i, a) -> fb.put(a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, Mask<Integer, S> m) {
        IntBuffer fb = bb.asIntBuffer();
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
        IntBuffer fb = bb.asIntBuffer();
        forEach((i, a) -> fb.put(i, a));
    }

    @Override
    public void intoByteBuffer(ByteBuffer bb, int ix, Mask<Integer, S> m) {
        bb = bb.duplicate().order(bb.order()).position(ix);
        IntBuffer fb = bb.asIntBuffer();
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
    public int addAll() {
        return rOp((int) 0, (i, a, b) -> (int) (a + b));
    }

    /**
     * Subtracts all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the subtraction
     * operation ({@code -}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the subtraction of all the lane elements of this vector
     */
    public int subAll() {
        return rOp((int) 0, (i, a, b) -> (int) (a - b));
    }

    /**
     * Multiplies all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the
     * multiplication operation ({@code *}) is applied to lane elements,
     * and the identity value is {@code 1}.
     *
     * @return the multiplication of all the lane elements of this vector
     */
    public int mulAll() {
        return rOp((int) 1, (i, a, b) -> (int) (a * b));
    }

    /**
     * Returns the minimum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> a > b ? b : a} is applied to lane elements,
     * and the identity value is {@link Integer.MAX_VALUE}.
     *
     * @return the minimum lane element of this vector
     */
    public int minAll() {
        return rOp(Integer.MAX_VALUE, (i, a, b) -> a > b ? b : a);
    }

    /**
     * Returns the maximum lane element of this vector.
     * <p>
     * This is an associative vector reduction operation where the operation
     * {@code (a, b) -> a < b ? b : a} is applied to lane elements,
     * and the identity value is {@link Integer.MIN_VALUE}.
     *
     * @return the maximum lane element of this vector
     */
    public int maxAll() {
        return rOp(Integer.MIN_VALUE, (i, a, b) -> a < b ? b : a);
    }

    /**
     * Logically ORs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical OR
     * operation ({@code |}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical OR all the lane elements of this vector
     */
    public int orAll() {
        return rOp((int) 0, (i, a, b) -> (int) (a | b));
    }

    /**
     * Logically ANDs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical AND
     * operation ({@code |}) is applied to lane elements,
     * and the identity value is {@code -1}.
     *
     * @return the logical AND all the lane elements of this vector
     */
    public int andAll() {
        return rOp((int) -1, (i, a, b) -> (int) (a & b));
    }

    /**
     * Logically XORs all lane elements of this vector.
     * <p>
     * This is an associative vector reduction operation where the logical XOR
     * operation ({@code ^}) is applied to lane elements,
     * and the identity value is {@code 0}.
     *
     * @return the logical XOR all the lane elements of this vector
     */
    public int xorAll() {
        return rOp((int) 0, (i, a, b) -> (int) (a ^ b));
    }

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     */
    public abstract int get(int i);

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
    public abstract IntVector<S> with(int i, int e);

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
    public int[] toArray() {
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
    public void intoArray(int[] a, int i) {
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
    public void intoArray(int[] a, int i, Mask<Integer, S> m) {
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
    public void intoArray(int[] a, int i, int[] indexMap, int j) {
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
    public void intoArray(int[] a, int i, Mask<Integer, S> m, int[] indexMap, int j) {
        forEach(m, (n, e) -> a[i + indexMap[j + n]] = e);
    }

    // Species

    @Override
    public abstract IntSpecies<S> species();

    /**
     * A specialized factory for creating {@link IntVector} value of the same
     * shape, and a {@link Mask} and {@link Shuffle} values of the same shape
     * and {@code int} element type.
     *
     * @param <S> the type of shape of this species
     */
    public static abstract class IntSpecies<S extends Vector.Shape> extends Vector.Species<Integer, S> {
        interface FOp {
            int apply(int i);
        }

        abstract IntVector<S> op(FOp f);

        abstract IntVector<S> op(Mask<Integer, S> m, FOp f);

        // Factories

        @Override
        public IntVector<S> zero() {
            return op(i -> 0);
        }

        /**
         * Returns a vector where all lane elements are set to the primitive
         * value {@code e}.
         *
         * @param e the value
         * @return a vector of vector where all lane elements are set to
         * the primitive value {@code e}
         */
        public IntVector<S> broadcast(int e) {
            return op(i -> e);
        }

        /**
         * Returns a vector where the first lane element is set to the primtive
         * value {@code e}, all other lane elements are set to the default
         * value.
         *
         * @param e the value
         * @return a vector where the first lane element is set to the primitive
         * value {@code e}
         */
        public IntVector<S> single(int e) {
            return op(i -> i == 0 ? e : (int) 0);
        }

        /**
         * Returns a vector where each lane element is set to a randomly
         * generated primitive value.
         * @@@ what are the properties of the random number generator?
         *
         * @return a vector where each lane elements is set to a randomly
         * generated primitive value
         */
        public IntVector<S> random() {
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
         * @@@ What should happen if es.length < this.length() ? use the default
         * value or throw IndexOutOfBoundsException
         *
         * @param es the given primitive values
         * @return a vector where each lane element is set to a given primitive
         * value
         */
        public IntVector<S> scalars(int... es) {
            return op(i -> es[i]);
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
        public IntVector<S> fromArray(int[] a, int i) {
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
        public IntVector<S> fromArray(int[] a, int i, Mask<Integer, S> m) {
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
        public IntVector<S> fromArray(int[] a, int i, int[] indexMap, int j) {
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
        public IntVector<S> fromArray(int[] a, int i, Mask<Integer, S> m, int[] indexMap, int j) {
            return op(m, n -> a[i + indexMap[j + n]]);
        }

        @Override
        public IntVector<S> fromByteArray(byte[] a, int ix) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
            IntBuffer fb = bb.asIntBuffer();
            return op(i -> fb.get());
        }

        @Override
        public IntVector<S> fromByteArray(byte[] a, int ix, Mask<Integer, S> m) {
            ByteBuffer bb = ByteBuffer.wrap(a, ix, a.length - ix).order(ByteOrder.nativeOrder());
            IntBuffer fb = bb.asIntBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (int) 0;
                }
            });
        }

        @Override
        public IntVector<S> fromByteBuffer(ByteBuffer bb) {
            IntBuffer fb = bb.asIntBuffer();
            return op(i -> fb.get());
        }

        @Override
        public IntVector<S> fromByteBuffer(ByteBuffer bb, Mask<Integer, S> m) {
            IntBuffer fb = bb.asIntBuffer();
            return op(i -> {
                if(m.getElement(i))
                    return fb.get();
                else {
                    fb.position(fb.position() + 1);
                    return (int) 0;
                }
            });
        }

        @Override
        public IntVector<S> fromByteBuffer(ByteBuffer bb, int ix) {
            bb = bb.duplicate().order(bb.order()).position(ix);
            IntBuffer fb = bb.asIntBuffer();
            return op(i -> fb.get(i));
        }

        @Override
        public IntVector<S> fromByteBuffer(ByteBuffer bb, int ix, Mask<Integer, S> m) {
            bb = bb.duplicate().order(bb.order()).position(ix);
            IntBuffer fb = bb.asIntBuffer();
            return op(m, i -> fb.get(i));
        }

        @Override
        public <F, T extends Shape> IntVector<S> reshape(Vector<F, T> o) {
            int blen = Math.max(o.species().bitSize(), bitSize()) / Byte.SIZE;
            ByteBuffer bb = ByteBuffer.allocate(blen).order(ByteOrder.nativeOrder());
            o.intoByteBuffer(bb, 0);
            return fromByteBuffer(bb, 0);
        }

        @Override
        @ForceInline
        public <F> IntVector<S> rebracket(Vector<F, S> o) {
            return reshape(o);
        }

        @Override
        @ForceInline
        public <T extends Shape> IntVector<S> resize(Vector<Integer, T> o) {
            return reshape(o);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <F, T extends Shape> IntVector<S> cast(Vector<F, T> v) {
            // Allocate array of required size
            int[] a = new int[length()];

            Class<?> vtype = v.species().elementType();
            int limit = Math.min(v.species().length(), length());
            if (vtype == byte.class) {
                ByteVector<T> tv = (ByteVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (int) tv.get(i);
                }
            } else if (vtype == short.class) {
                ShortVector<T> tv = (ShortVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (int) tv.get(i);
                }
            } else if (vtype == int.class) {
                IntVector<T> tv = (IntVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (int) tv.get(i);
                }
            } else if (vtype == long.class){
                LongVector<T> tv = (LongVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (int) tv.get(i);
                }
            } else if (vtype == float.class){
                FloatVector<T> tv = (FloatVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (int) tv.get(i);
                }
            } else if (vtype == double.class){
                DoubleVector<T> tv = (DoubleVector<T>)v;
                for (int i = 0; i < limit; i++) {
                    a[i] = (int) tv.get(i);
                }
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }

            return scalars(a);
        }

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
    public static IntSpecies<?> preferredSpecies() {
        return (IntSpecies<?>) Vector.preferredSpecies(int.class);
    }

    /**
     * Finds a species for an element type of {@code int} and shape.
     *
     * @param s the shape
     * @param <S> the type of shape
     * @return a species for an element type of {@code int} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    @SuppressWarnings("unchecked")
    public static <S extends Shape> IntSpecies<S> species(S s) {
        Objects.requireNonNull(s);
        if (s == Shapes.S_64_BIT) {
            return (IntSpecies<S>) Int64Vector.SPECIES;
        } else if (s == Shapes.S_128_BIT) {
            return (IntSpecies<S>) Int128Vector.SPECIES;
        } else if (s == Shapes.S_256_BIT) {
            return (IntSpecies<S>) Int256Vector.SPECIES;
        } else if (s == Shapes.S_512_BIT) {
            return (IntSpecies<S>) Int512Vector.SPECIES;
        } else {
            throw new IllegalArgumentException("Bad shape: " + s);
        }
    }
}
