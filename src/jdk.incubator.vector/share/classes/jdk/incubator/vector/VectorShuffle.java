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
import java.util.function.IntUnaryOperator;

/**
 * A {@code VectorShuffle} represents an ordered immutable sequence of
 * {@code int} values.  A VectorShuffle can be used with a shuffle accepting
 * vector operation to control the rearrangement of lane elements of input
 * vectors
 * <p>
 * The number of values in the sequence is referred to as the shuffle
 * {@link #length() length}.  The length also corresponds to the number of
 * shuffle lanes.  The lane element at lane index {@code N} (from {@code 0},
 * inclusive, to length, exclusive) corresponds to the {@code N + 1}'th
 * value in the sequence.
 * A VectorShuffle and Vector of the same element type and shape have the same
 * number of lanes.
 * <p>
 * A VectorShuffle describes how a lane element of a vector may cross lanes from
 * its lane index, {@code i} say, to another lane index whose value is the
 * shuffle's lane element at lane index {@code i}.  VectorShuffle lane elements
 * will be in the range of {@code 0} (inclusive) to the shuffle length
 * (exclusive), and therefore cannot induce out of bounds errors when
 * used with vectors operations and vectors of the same length.
 *
 * @param <E> the boxed element type of this mask
 */
public abstract class VectorShuffle<E> {
    VectorShuffle() {}

    /**
     * Returns the species of this shuffle.
     *
     * @return the species of this shuffle
     */
    public abstract VectorSpecies<E> species();

    /**
     * Returns the number of shuffle lanes (the length).
     *
     * @return the number of shuffle lanes
     */
    public int length() { return species().length(); }

    /**
     * Converts this shuffle to a shuffle of the given species of element type {@code F}.
     * <p>
     * For each shuffle lane, where {@code N} is the lane index, the
     * shuffle element at index {@code N} is placed, unmodified, into the
     * resulting shuffle at index {@code N}.
     *
     * @param species species of desired shuffle
     * @param <F> the boxed element type of the species
     * @return a shuffle converted by shape and element type
     * @throws IllegalArgumentException if this shuffle length and the
     * species length differ
     */
    public abstract <F> VectorShuffle<F> cast(VectorSpecies<F> species);

    /**
     * Returns a shuffle of mapped indexes where each lane element is
     * the result of applying a mapping function to the corresponding lane
     * index.
     * <p>
     * Care should be taken to ensure VectorShuffle values produced from this
     * method are consumed as constants to ensure optimal generation of
     * code.  For example, values held in static final fields or values
     * held in loop constant local variables.
     * <p>
     * This method behaves as if a shuffle is created from an array of
     * mapped indexes as follows:
     * <pre>{@code
     *   int[] a = new int[species.length()];
     *   for (int i = 0; i < a.length; i++) {
     *       a[i] = f.applyAsInt(i);
     *   }
     *   return VectorShuffle.fromValues(a);
     * }</pre>
     *
     * @param species shuffle species
     * @param f the lane index mapping function
     * @return a shuffle of mapped indexes
     */
    @ForceInline
    public static <E> VectorShuffle<E> shuffle(VectorSpecies<E> species, IntUnaryOperator f) {
        return ((AbstractSpecies<E>) species).shuffleFromOpFactory.apply(f);
    }

    /**
     * Returns a shuffle where each lane element is the value of its
     * corresponding lane index.
     * <p>
     * This method behaves as if a shuffle is created from an identity
     * index mapping function as follows:
     * <pre>{@code
     *   return VectorShuffle.shuffle(i -> i);
     * }</pre>
     *
     * @param species shuffle species
     * @return a shuffle of lane indexes
     */
    @ForceInline
    public static <E> VectorShuffle<E> shuffleIota(VectorSpecies<E> species) {
        return ((AbstractSpecies<E>) species).shuffleFromOpFactory.apply(AbstractShuffle.IDENTITY);
    }

    /**
     * Returns a shuffle where each lane element is set to a given
     * {@code int} value logically AND'ed by the species length minus one.
     * <p>
     * For each shuffle lane, where {@code N} is the shuffle lane index, the
     * the {@code int} value at index {@code N} logically AND'ed by
     * {@code species.length() - 1} is placed into the resulting shuffle at
     * lane index {@code N}.
     *
     * @param species shuffle species
     * @param ixs the given {@code int} values
     * @return a shuffle where each lane element is set to a given
     * {@code int} value
     * @throws IndexOutOfBoundsException if the number of int values is
     * {@code < species.length()}
     */
    @ForceInline
    public static <E> VectorShuffle<E> fromValues(VectorSpecies<E> species, int... ixs) {
        return ((AbstractSpecies<E>) species).shuffleFromArrayFactory.apply(ixs, 0);
    }

    /**
     * Loads a shuffle from an {@code int} array starting at an offset.
     * <p>
     * For each shuffle lane, where {@code N} is the shuffle lane index, the
     * array element at index {@code i + N} logically AND'ed by
     * {@code species.length() - 1} is placed into the resulting shuffle at lane
     * index {@code N}.
     *
     * @param species shuffle species
     * @param ixs the {@code int} array
     * @param i the offset into the array
     * @return a shuffle loaded from the {@code int} array
     * @throws IndexOutOfBoundsException if {@code i < 0}, or
     * {@code i > a.length - species.length()}
     */
    @ForceInline
    public static <E> VectorShuffle<E> fromArray(VectorSpecies<E> species, int[] ixs, int i) {
        return ((AbstractSpecies<E>) species).shuffleFromArrayFactory.apply(ixs, i);
    }

    /**
     * Returns an {@code int} array containing the lane elements of this
     * shuffle.
     * <p>
     * This method behaves as if it {@link #intoArray(int[], int)} stores}
     * this shuffle into an allocated array and returns that array as
     * follows:
     * <pre>{@code
     *   int[] a = new int[this.length()];
     *   VectorShuffle.intoArray(a, 0);
     *   return a;
     * }</pre>
     *
     * @return an array containing the the lane elements of this vector
     */
    public abstract int[] toArray();

    /**
     * Stores this shuffle into an {@code int} array starting at offset.
     * <p>
     * For each shuffle lane, where {@code N} is the shuffle lane index,
     * the lane element at index {@code N} is stored into the array at index
     * {@code i + N}.
     *
     * @param a the array
     * @param i the offset into the array
     * @throws IndexOutOfBoundsException if {@code i < 0}, or
     * {@code i > a.length - this.length()}
     */
    public abstract void intoArray(int[] a, int i);

    /**
     * Converts this shuffle into a vector, creating a vector from shuffle
     * lane elements (int values) cast to the vector element type.
     * <p>
     * This method behaves as if it returns the result of creating a
     * vector given an {@code int} array obtained from this shuffle's
     * lane elements, as follows:
     * <pre>{@code
     *   int[] sa = this.toArray();
     *   $type$[] va = new $type$[a.length];
     *   for (int i = 0; i < a.length; i++) {
     *       va[i] = ($type$) sa[i];
     *   }
     *   return IntVector.fromArray(va, 0);
     * }</pre>
     *
     * @return a vector representation of this shuffle
     */
    public abstract Vector<E> toVector();

    /**
     * Gets the {@code int} lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the {@code int} lane element at lane index {@code i}
     */
    public int getElement(int i) { return toArray()[i]; }

    /**
     * Rearranges the lane elements of this shuffle selecting lane indexes
     * controlled by another shuffle.
     * <p>
     * For each lane of the specified shuffle, at lane index {@code N} with lane
     * element {@code I}, the lane element at {@code I} from this shuffle is
     * selected and placed into the resulting shuffle at {@code N}.
     *
     * @param s the shuffle controlling lane index selection
     * @return the rearrangement of the lane elements of this shuffle
     */
    public abstract VectorShuffle<E> rearrange(VectorShuffle<E> s);
}
