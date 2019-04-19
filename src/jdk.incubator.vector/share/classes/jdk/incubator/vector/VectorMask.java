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

import java.util.Objects;

/**
 * A {@code VectorMask} represents an ordered immutable sequence of {@code boolean}
 * values.  A VectorMask can be used with a mask accepting vector operation to
 * control the selection and operation of lane elements of input vectors.
 * <p>
 * The number of values in the sequence is referred to as the VectorMask
 * {@link #length() length}. The length also corresponds to the number of
 * VectorMask lanes.  The lane element at lane index {@code N} (from {@code 0},
 * inclusive, to length, exclusive) corresponds to the {@code N + 1}'th
 * value in the sequence.
 * A VectorMask and Vector of the same element type and shape have the same number
 * of lanes.
 * <p>
 * A lane is said to be <em>set</em> if the lane element is {@code true},
 * otherwise a lane is said to be <em>unset</em> if the lane element is
 * {@code false}.
 * <p>
 * VectorMask declares a limited set of unary, binary and reductive mask
 * operations.
 * <ul>
 * <li>
 * A mask unary operation (1-ary) operates on one input mask to produce a
 * result mask.
 * For each lane of the input mask the
 * lane element is operated on using the specified scalar unary operation and
 * the boolean result is placed into the mask result at the same lane.
 * The following pseudocode expresses the behaviour of this operation category:
 *
 * <pre>{@code
 * VectorMask<E> a = ...;
 * boolean[] ar = new boolean[a.length()];
 * for (int i = 0; i < a.length(); i++) {
 *     ar[i] = boolean_unary_op(a.isSet(i));
 * }
 * VectorMask<E> r = VectorMask.fromArray(ar, 0);
 * }</pre>
 *
 * <li>
 * A mask binary operation (2-ary) operates on two input
 * masks to produce a result mask.
 * For each lane of the two input masks,
 * a and b say, the corresponding lane elements from a and b are operated on
 * using the specified scalar binary operation and the boolean result is placed
 * into the mask result at the same lane.
 * The following pseudocode expresses the behaviour of this operation category:
 *
 * <pre>{@code
 * VectorMask<E> a = ...;
 * VectorMask<E> b = ...;
 * boolean[] ar = new boolean[a.length()];
 * for (int i = 0; i < a.length(); i++) {
 *     ar[i] = scalar_binary_op(a.isSet(i), b.isSet(i));
 * }
 * VectorMask<E> r = VectorMask.fromArray(ar, 0);
 * }</pre>
 *
 * </ul>
 * @param <E> the boxed element type of this mask
 */
public abstract class VectorMask<E> {
    VectorMask() {}

    /**
     * Returns the species of this mask.
     *
     * @return the species of this mask
     */
    public abstract VectorSpecies<E> species();

    /**
     * Returns the number of mask lanes (the length).
     *
     * @return the number of mask lanes
     */
    public int length() { return species().length(); }

    /**
     * Returns a mask where each lane is set or unset according to given
     * {@code boolean} values
     * <p>
     * For each mask lane, where {@code N} is the mask lane index,
     * if the given {@code boolean} value at index {@code N} is {@code true}
     * then the mask lane at index {@code N} is set, otherwise it is unset.
     *
     * @param species mask species
     * @param bits the given {@code boolean} values
     * @return a mask where each lane is set or unset according to the given {@code boolean} value
     * @throws IndexOutOfBoundsException if {@code bits.length < species.length()}
     */
    @ForceInline
    public static <E> VectorMask<E> fromValues(VectorSpecies<E> species, boolean... bits) {
        return fromArray(species, bits, 0);
    }

    /**
     * Loads a mask from a {@code boolean} array starting at an offset.
     * <p>
     * For each mask lane, where {@code N} is the mask lane index,
     * if the array element at index {@code ix + N} is {@code true} then the
     * mask lane at index {@code N} is set, otherwise it is unset.
     *
     * @param species mask species
     * @param bits the {@code boolean} array
     * @param ix the offset into the array
     * @return the mask loaded from a {@code boolean} array
     * @throws IndexOutOfBoundsException if {@code ix < 0}, or
     * {@code ix > bits.length - species.length()}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static <E> VectorMask<E> fromArray(VectorSpecies<E> species, boolean[] bits, int ix) {
        Objects.requireNonNull(bits);
        ix = VectorIntrinsics.checkIndex(ix, bits.length, species.length());
        return VectorIntrinsics.load((Class<VectorMask<E>>) species.maskType(), species.elementType(), species.length(),
                bits, (long) ix + Unsafe.ARRAY_BOOLEAN_BASE_OFFSET,
                bits, ix, species,
                (boolean[] c, int idx, VectorSpecies<E> s) -> ((AbstractSpecies<E>)s).opm(n -> c[idx + n]));
    }

    /**
     * Returns a mask where all lanes are set.
     *
     * @param species mask species
     * @return a mask where all lanes are set
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static <E> VectorMask<E> maskAllTrue(VectorSpecies<E> species) {
        return VectorIntrinsics.broadcastCoerced((Class<VectorMask<E>>) species.maskType(), species.elementType(), species.length(),
                -1,  species,
                ((z, s) -> AbstractMask.trueMask(s)));
    }

    /**
     * Returns a mask where all lanes are unset.
     *
     * @param species mask species
     * @return a mask where all lanes are unset
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static <E> VectorMask<E> maskAllFalse(VectorSpecies<E> species) {
        return VectorIntrinsics.broadcastCoerced((Class<VectorMask<E>>) species.maskType(), species.elementType(), species.length(),
                0, species,
                ((z, s) -> AbstractMask.falseMask(s)));
    }

    /**
     * Converts this mask to a mask of the given species shape of element type {@code F}.
     * <p>
     * For each mask lane, where {@code N} is the lane index, if the
     * mask lane at index {@code N} is set, then the mask lane at index
     * {@code N} of the resulting mask is set, otherwise that mask lane is
     * not set.
     *
     * @param s the species of the desired mask
     * @param <F> the boxed element type of the species
     * @return a mask converted by shape and element type
     * @throws IllegalArgumentException if this mask length and the species
     * length differ
     */
    public abstract <F> VectorMask<F> cast(VectorSpecies<F> s);

    /**
     * Returns the lane elements of this mask packed into a {@code long}
     * value for at most the first 64 lane elements.
     * <p>
     * The lane elements are packed in the order of least significant bit
     * to most significant bit.
     * For each mask lane where {@code N} is the mask lane index, if the
     * mask lane is set then the {@code N}'th bit is set to one in the
     * resulting {@code long} value, otherwise the {@code N}'th bit is set
     * to zero.
     *
     * @return the lane elements of this mask packed into a {@code long}
     * value.
     */
    public abstract long toLong();

    /**
     * Returns an {@code boolean} array containing the lane elements of this
     * mask.
     * <p>
     * This method behaves as if it {@link #intoArray(boolean[], int)} stores}
     * this mask into an allocated array and returns that array as
     * follows:
     * <pre>{@code
     * boolean[] a = new boolean[this.length()];
     * this.intoArray(a, 0);
     * return a;
     * }</pre>
     *
     * @return an array containing the the lane elements of this vector
     */
    public abstract boolean[] toArray();

    /**
     * Stores this mask into a {@code boolean} array starting at offset.
     * <p>
     * For each mask lane, where {@code N} is the mask lane index,
     * the lane element at index {@code N} is stored into the array at index
     * {@code i + N}.
     *
     * @param a the array
     * @param i the offset into the array
     * @throws IndexOutOfBoundsException if {@code i < 0}, or
     * {@code i > a.length - this.length()}
     */
    public abstract void intoArray(boolean[] a, int i);

    /**
     * Returns {@code true} if any of the mask lanes are set.
     *
     * @return {@code true} if any of the mask lanes are set, otherwise
     * {@code false}.
     */
    public abstract boolean anyTrue();

    /**
     * Returns {@code true} if all of the mask lanes are set.
     *
     * @return {@code true} if all of the mask lanes are set, otherwise
     * {@code false}.
     */
    public abstract boolean allTrue();

    /**
     * Returns the number of mask lanes that are set.
     *
     * @return the number of mask lanes that are set.
     */
    public abstract int trueCount();

    /**
     * Logically ands this mask with an input mask.
     * <p>
     * This is a mask binary operation where the logical and operation
     * ({@code &&} is applied to lane elements.
     *
     * @param o the input mask
     * @return the result of logically and'ing this mask with an input mask
     */
    public abstract VectorMask<E> and(VectorMask<E> o);

    /**
     * Logically ors this mask with an input mask.
     * <p>
     * This is a mask binary operation where the logical or operation
     * ({@code ||} is applied to lane elements.
     *
     * @param o the input mask
     * @return the result of logically or'ing this mask with an input mask
     */
    public abstract VectorMask<E> or(VectorMask<E> o);

    /**
     * Logically negates this mask.
     * <p>
     * This is a mask unary operation where the logical not operation
     * ({@code !} is applied to lane elements.
     *
     * @return the result of logically negating this mask.
     */
    public abstract VectorMask<E> not();

    /**
     * Returns a vector representation of this mask.
     * <p>
     * For each mask lane, where {@code N} is the mask lane index,
     * if the mask lane is set then an element value whose most significant
     * bit is set is placed into the resulting vector at lane index
     * {@code N}, otherwise the default element value is placed into the
     * resulting vector at lane index {@code N}.
     *
     * @return a vector representation of this mask.
     */
    public abstract Vector<E> toVector();

    /**
     * Tests if the lane at index {@code i} is set
     * @param i the lane index
     *
     * @return true if the lane at index {@code i} is set, otherwise false
     */
    public abstract boolean getElement(int i);

    /**
     * Tests if the lane at index {@code i} is set
     * @param i the lane index
     * @return true if the lane at index {@code i} is set, otherwise false
     * @see #getElement
     */
    public boolean isSet(int i) {
        return getElement(i);
    }
}
