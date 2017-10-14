/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.vector;

import java.util.BitSet;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Rough sketch of interface for hardware vectors, version 0.01.
 * For best results, requires full polymorphism (any-generics) and full
 * unboxing (value types).
 * Uses an interface to hide implementation specifics.
 *
 * @author jrose
 */

/**
 *
 * @param <E> The element type
 * @param <S> The shape type
 */
public interface Vector<E, S extends Vector.Shape<Vector<?, S>>> {

    /**
     * Returns a byte-based species for a given compatible shape.
     *
     */
    static <S extends Vector.Shape<Vector<?, S>>> Vector.Species<Byte, S> byteSpecies(S shape) {
        return VectorBytes.species(shape);
    }


    Long2 toLong2();

    Long4 toLong4();

    Long8 toLong8();

    /**
     * @return the species associated with this vector to perform type queries
     * or create vectors
     */
    Species<E, S> species();

    /**
     * @return the shape associated with this species
     */
    default S shape() { return species().shape(); }

    /**
     * @return the class of the element
     */
    default Class<E> elementType() { return species().elementType(); }

    /**
     * @return the length of vectors created by this species
     */
    default int length() { return species().length(); }

    /**
     * @return the total bit size of vectors created by this species
     */
    default int bitSize() { return species().bitSize(); }


    // Scalar access

    /**
     * Get an element at a given index.
     * @param index the index.
     * @return the element at the given index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater
     * than or equal to length()
     */
    E getElement(int index);

    /**
     * Put a given element into the vector at a given index
     * @@@ Should Vector be immutable?
     *
     * @param index the index
     * @param x the element
     * @return this vector
     */
    Vector<E, S> putElement(int index, E x);

    // Bulk access

    /**
     * @return an array containing all of the elements in this vector.
     */
    default E[] toArray() {
        @SuppressWarnings("unchecked")
        E[] array = (E[]) java.lang.reflect.Array.newInstance(elementType(), length());
        for (int i = 0; i < array.length; i++) array[i] = getElement(i);
        return array;
    }

    /**
     * Assign the elements from this vector to corresponding elements in the
     * given array from the given offset (inclusive) up to the offset plus the
     * vector length (exclusive) or the array length (which ever is smaller).
     *
     */
    default void intoArray(E[] a, int o) {
        int l = Math.min(length(), a.length - o);
        for (int i = 0; i < l; i++) {
            a[o + i] = getElement(i);
        }
    }

    // putAll(E[]a, int o)

    // copyInto(Vector<E, S> v)

    // copy/clone()

    //    // shape shifting:
//
//    /**
//     * Return a vector with elements converted.
//     */
//    <E2, S2 extends Shape<Vector<?, S2>>>
//    Vector<E2, S2> reshape(Class<E2> type, S2 shape);
//
//    /**
//     * Return a vector with the same bitwise contents but different element type
//     * and shape.
//     */
//    <E2, S2 extends Shape<Vector<?, S2>>>
//    Vector<E2, S2> cast(Class<E2> type, S2 shape);
//
//    /**
//     * Return a vector of the same size but different elements and possibly
//     * different length.
//     */
//    default <E2> Vector<E2, S> rebracket(Class<E2> type) {
//        return cast(type, shape());
//    }
//
//    /**
//     * Return a vector with a different length, filled with the same elements
//     * and possibly zeroes.
//     */
//    default <S2 extends Shape<Vector<?, S2>>>
//    Vector<E, S2> resize(S2 shape) {
//        return reshape(elementType(), shape);
//    }
//

    // elemental operations:

    Vector<E, S> compareEqual(Vector<E, S> v);

    Mask<S> test(Predicate<E> op);

    Vector<E, S> map(UnaryOperator<E> op);

    Vector<E, S> mapWhere(Mask<S> mask, UnaryOperator<E> op);

    Vector<E, S> map(BinaryOperator<E> op, Vector<E, S> this2);

    Vector<E, S> mapWhere(Mask<S> mask, BinaryOperator<E> op, Vector<E, S> this2);

//    <T> Vector<T, S> map(Function<E, T> op);
//
//    <T> Vector<T, S> mapOrZero(Mask<S> mask, Function<E, T> op);
//
//    <F, T> Vector<T, S> map(BiFunction<E, F, T> op, Vector<F, S> this2);
//
//    <F> Mask<S> test(BiPredicate<E, F> op, Vector<F, S> this2);
//
//    <F> Vector<E, S> mapWhere(Mask<S> mask, BiFunction<E, F, E> op, Vector<F, S> this2);
//
//    <F, T> Vector<T, S> mapOrZero(Mask<S> mask, BiFunction<E, F, T> op, Vector<F, S> this2);
//

    // elemental operations, specialized:
    boolean isZeros();

    Vector<E, S> neg();

    Vector<E, S> xor(Vector<E, S> o);

    /**
     * Add elements of this vector to the correspond elements of a given vector
     * (of the same species) and return a vector whose corresponding elements
     * contain the results.
     *
     */
    Vector<E, S> add(Vector<E, S> o);

//    Vector<E, S> addWhere(Mask<S> mask, Vector<E, S> this2);
//    // add/sub/mul/div/and/or/xor/min/max/cmp, etc, etc, etc...
//
//    // elemental operations, specialized with broadcast:
//    Vector<E, S> addElement(E x2);
//
//    Vector<E, S> addElementWhere(Mask<S> mask, E x2);
//
//    // horizontal reductions:
//    E reduce(BinaryOperator<E> op);
//
//    E reduceWhere(Mask<S> mask, E id, BinaryOperator<E> op);

    <T> T reduce(Function<E, T> mapper, BinaryOperator<T> op);

//
//    // horizontal operations, specialized:

    Mask<S> toMask();


//    E sumAll();
//
//    E maxAll();
//
//    E orAll();
//    // sum/min/max/and/or/xor, etc, etc, etc...

    // permutations (and other reorderings):
    Vector<E, S> blend(Vector<E, S> b, Vector<E, S> mask);

    Vector<E, S> shuffle(Shuffle<S> perm);


    // convert own self to a permutation
    // What if values are out of bounds? mod by length() ?
    Shuffle<S> toShuffle();

 //
    /**
     * The vector species for a given element and shape type. Supports type
     * queries and the creation of vector instances.
     *
     * @param <E> the element type
     * @param <S> the shape type
     */
    interface Species<E, S extends Vector.Shape<Vector<?, S>>> {
        // Queries

        /**
         * @return the shape associated with this species
         */
        S shape();

        /**
         * @return the class of the element
         */
        Class<E> elementType();

        /**
         * @return the element size, in bits
         */
        int elementSize();

        /**
         * @return the length of vectors created by this species
         */
        default int length() { return shape().length(this); }

        /**
         * @return the total bit size of vectors created by this species
         */
        default int bitSize() { return shape().bitSize(); }


        // Factories

        default Vector<E, S> of(E... vs) {
            Vector<E, S> v = zeroVector();
            if (vs == null) return v;

            for (int i = 0; i < Math.min(vs.length, length()); i++) {
                v = v.putElement(i, vs[i]);
            }
            return v;
        }

        /**
         * @return a function that when applied to a value returns a
         * vector where the value is assigned to each element.
         */
        Function<E, Vector<E, S>> fromElementFactory();

        /**
         * Returns a vector where the given value is assigned to each element.
         *
         * @param v a value
         * @return returns a vector where the given value is assigned to each
         * element.
         */
        default Vector<E, S> fromElement(E v) {
            return fromElementFactory().apply(v);
        }

        /**
         * @return returns a zero vector where the default value is assigned to
         * each element
         */
        default Vector<E, S> zeroVector() {
            return fromElement(zeroElement());
        }

        /**
         * @return return the default value for the element type
         */
        default E zeroElement() {
            @SuppressWarnings("unchecked")
            E[] array = (E[]) java.lang.reflect.Array.newInstance(elementType(), 1);
            return array[0];
        }

        Vector<E, S> generate(IntFunction<? extends E> generator);

        /**
         * @return a function that when applied to an array of elements and
         * an offset returns a vector whose elements are assigned the same
         * values as in the array from the offset (inclusive) up to the offset
         * plus the vector length (exclusive) or the array length (which
         * ever is smaller).
         */
        BiFunction<E[], Integer, Vector<E, S>> fromArrayFactory();

        /**
         * Returns a vector whose elements are assigned the same values
         * as in the given array from the given offset (inclusive) up to the
         * offset plus the vector length (exclusive) or the array length (which
         * ever is smaller).
         *
         * @param array the array
         * @param o the offset
         * @return a vector whose elements are assigned the same values as in
         * the given array from the given offset.
         */
        default Vector<E, S> fromArray(E[] array, int o) {
            return fromArrayFactory().apply(array, o);
        }

        /*
        @@@ Should conversion from a mask be a factory method?
            Given that a mask is unlikely to be larger than 64 bits
            there is less need to get access to the mask internals
            and therefore there is less need for Mask to branch on
            the element type to select the write species to construct the
            vector

        @@@ pass values to be substituted for 0 and 1?
         */
//        Vector<E, S> fromMask(Mask<S> make);

        // @@@ Homage to APL

        // return null permutation for self
        Shuffle<S> iota();

        // arithmetic sequence ((B+i*S)%M)
        Shuffle<S> iota(int base, int step, int modulus);
    }

    /**
     * The underlying shape of a vector, independent of the element type
     * @param <V> the vector type
     */
    interface Shape<V extends Vector<?, ?> /*extends com.oracle.vector.Vector<?, Shape<V>>*/> {
        int bitSize();  // usually 64, 128, 256, etc.

        int length(Species<?, ?> s);  // usually bitSize / sizeof(s.elementType)
        //boolean isFixedSize();  // usually true
        //boolean ownMaskSize();  // non-zero only if this shape has a built-in mask
    }

    // associated mask type:
    interface Mask<S extends Shape<Vector<?, S>>> {
        int length();

        /**
         * Returns a packed bitmask for up to 64 elements.
         * Element {@code N} is tested as @{code (1<<N)}.
         */
        long toLong();

        BitSet toBitSet();

        boolean[] toArray();

        <E> Vector<E, S> toVector(Class<E> type);

        default boolean getElement(int i) { return i < 64 ? ((toLong() >>> i) & 1) != 0 : toArray()[i]; }
    }

    // associated permutation type:
    interface Shuffle<S extends Shape<Vector<?, S>>> {
        Long2 toLong2();

        Long4 toLong4();

        Long8 toLong8();

        int length();

        int[] toArray();

        Vector<Integer, S> toVector();

        <E> Vector<E, S> toVector(Class<E> type);

        default int getElement(int i) { return toArray()[i]; }
    }
}
