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
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

/**
 * Interface supporting vectors of same element type, {@code E} and {@link VectorShape shape}.
 *
 * @param <E> the boxed element type of this species
 */
public interface VectorSpecies<E> {
    /**
     * Returns the primitive element type of vectors produced by this
     * species.
     *
     * @return the primitive element type
     */
    public abstract Class<E> elementType();

    /**
     * Returns the vector box type for this species
     *
     * @return the box type
     */
    abstract Class<?> boxType();

    /**
     * Returns the vector mask type for this species
     *
     * @return the box type
     */
    abstract Class<?> maskType();

    /**
     * Returns the element size, in bits, of vectors produced by this
     * species.
     *
     * @return the element size, in bits
     */
    public abstract int elementSize();

    /**
     * Returns the shape of masks, shuffles, and vectors produced by this
     * species.
     *
     * @return the primitive element type
     */
    public abstract VectorShape shape();

    /**
     * Returns the shape of the corresponding index species
     * @return the shape
     */
    @ForceInline
    public abstract VectorShape indexShape();

    /**
     * Returns the mask, shuffe, or vector lanes produced by this species.
     *
     * @return the the number of lanes
     */
    default public int length() { return shape().length(this); }

    /**
     * Returns the total vector size, in bits, of vectors produced by this
     * species.
     *
     * @return the total vector size, in bits
     */
    default public int bitSize() { return shape().bitSize(); }

    // Factory

    /**
     * Finds a species for an element type and shape.
     *
     * @param c the element type
     * @param s the shape
     * @param <E> the boxed element type
     * @return a species for an element type and shape
     * @throws IllegalArgumentException if no such species exists for the
     * element type and/or shape
     */
    @SuppressWarnings("unchecked")
    public static <E> VectorSpecies<E> of(Class<E> c, VectorShape s) {
        if (c == float.class) {
            return (VectorSpecies<E>) FloatVector.species(s);
        }
        else if (c == double.class) {
            return (VectorSpecies<E>) DoubleVector.species(s);
        }
        else if (c == byte.class) {
            return (VectorSpecies<E>) ByteVector.species(s);
        }
        else if (c == short.class) {
            return (VectorSpecies<E>) ShortVector.species(s);
        }
        else if (c == int.class) {
            return (VectorSpecies<E>) IntVector.species(s);
        }
        else if (c == long.class) {
            return (VectorSpecies<E>) LongVector.species(s);
        }
        else {
            throw new IllegalArgumentException("Bad vector element type: " + c.getName());
        }
    }

    /**
     * Finds a preferred species for an element type.
     * <p>
     * A preferred species is a species chosen by the platform that has a
     * shape of maximal bit size.  A preferred species for different element
     * types will have the same shape, and therefore vectors created from
     * such species will be shape compatible.
     *
     * @param c the element type
     * @param <E> the boxed element type
     * @return a preferred species for an element type
     * @throws IllegalArgumentException if no such species exists for the
     * element type
     */
    public static <E> VectorSpecies<E> ofPreferred(Class<E> c) {
        Unsafe u = Unsafe.getUnsafe();

        int vectorLength = u.getMaxVectorSize(c);
        int vectorBitSize = Vector.bitSizeForVectorLength(c, vectorLength);
        VectorShape s = VectorShape.forBitSize(vectorBitSize);
        return VectorSpecies.of(c, s);
    }
}

