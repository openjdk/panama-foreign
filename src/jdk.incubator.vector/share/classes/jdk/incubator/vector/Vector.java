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
 * hardware instructions.  Such computations exploit data parallelism to perform
 * the same operation on multiple data points simultaneously in a faster time it
 * would ordinarily take to perform the same operation sequentially on each data
 * point.
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
 * Only Vectors of the same element type and shape can be operated on together.
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
 * Vector declares a set of operations (methods) that are common to all
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
 * <p>
 * Operations...
 * unary operations
 * binary operations
 * reductive operations
 * cross-lane operations
 * <p>
 * Masks...
 * <p>
 * Shuffles
 *
 * <p>This is a value-based
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
    public abstract Vector<E, S> add(Vector<E, S> o);

    public abstract Vector<E, S> add(Vector<E, S> o, Mask<E, S> m);

    public abstract Vector<E, S> addSaturate(Vector<E, S> o);

    public abstract Vector<E, S> addSaturate(Vector<E, S> o, Mask<E, S> m);

    public abstract Vector<E, S> sub(Vector<E, S> o);

    public abstract Vector<E, S> sub(Vector<E, S> o, Mask<E, S> m);

    public abstract Vector<E, S> subSaturate(Vector<E, S> o);

    public abstract Vector<E, S> subSaturate(Vector<E, S> o, Mask<E, S> m);

    public abstract Vector<E, S> mul(Vector<E, S> o);

    public abstract Vector<E, S> mul(Vector<E, S> o, Mask<E, S> m);

    public abstract Vector<E, S> neg();

    public abstract Vector<E, S> neg(Mask<E, S> m);

    //Maths from java.math
    public abstract Vector<E, S> abs();

    public abstract Vector<E, S> abs(Mask<E, S> m);

    public abstract Vector<E, S> min(Vector<E, S> o);

    public abstract Vector<E, S> max(Vector<E, S> o);

    //TODO: Parity

    //Comparisons
    //For now these are projected into the same element type.  False is the element 0.  True is otherwise.
    //TODO: N.B. Floating point NaN behaviors?
    //TODO: Check the JLS
    public abstract Mask<E, S> equal(Vector<E, S> o);

    public abstract Mask<E, S> notEqual(Vector<E, S> o);

    public abstract Mask<E, S> lessThan(Vector<E, S> o);

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
