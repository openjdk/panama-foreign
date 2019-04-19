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
import jdk.internal.vm.annotation.Stable;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

abstract class AbstractSpecies<E> implements VectorSpecies<E> {
    @FunctionalInterface
    interface fShuffleFromArray<E> {
        VectorShuffle<E> apply(int[] reorder, int idx);
    }

    final Function<boolean[], VectorMask<E>> maskFactory;
    final Function<IntUnaryOperator, VectorShuffle<E>> shuffleFromOpFactory;
    final fShuffleFromArray<E> shuffleFromArrayFactory;

    @Stable
    protected final VectorShape shape;
    @Stable
    protected final Class<E> elementType;
    @Stable
    protected final int elementSize;
    @Stable
    protected final Class<?> boxType;
    @Stable
    protected final Class<?> maskType;
    @Stable
    protected final VectorShape indexShape;

    AbstractSpecies(VectorShape shape, Class<E> elementType, int elementSize,
                    Class<?> boxType, Class<?> maskType, Function<boolean[], VectorMask<E>> maskFactory,
                    Function<IntUnaryOperator, VectorShuffle<E>> shuffleFromOpFactory,
                    fShuffleFromArray<E> shuffleFromArrayFactory) {

        this.maskFactory = maskFactory;
        this.shuffleFromArrayFactory = shuffleFromArrayFactory;
        this.shuffleFromOpFactory = shuffleFromOpFactory;

        this.shape = shape;
        this.elementType = elementType;
        this.elementSize = elementSize;
        this.boxType = boxType;
        this.maskType = maskType;

        if (boxType == Long64Vector.class || boxType == Double64Vector.class) {
            indexShape = VectorShape.S_64_BIT;
        }
        else {
            int bitSize = Vector.bitSizeForVectorLength(int.class, shape.bitSize() / elementSize);
            indexShape = VectorShape.forBitSize(bitSize);
        }
    }

    @Override
    @ForceInline
    public int bitSize() {
        return shape.bitSize();
    }

    @Override
    @ForceInline
    public int length() {
        return shape.bitSize() / elementSize;
    }

    @Override
    @ForceInline
    public Class<E> elementType() {
        return elementType;
    }

    @Override
    @ForceInline
    public Class<?> boxType() {
        return boxType;
    }

    @Override
    @ForceInline
    public Class<?> maskType() {
        return maskType;
    }

    @Override
    @ForceInline
    public int elementSize() {
        return elementSize;
    }

    @Override
    @ForceInline
    public VectorShape shape() {
        return shape;
    }

    @Override
    @ForceInline
    public VectorShape indexShape() { return indexShape; }

    @Override
    public String toString() {
        return new StringBuilder("Shape[")
                .append(bitSize()).append(" bits, ")
                .append(length()).append(" ").append(elementType.getSimpleName()).append("s x ")
                .append(elementSize()).append(" bits")
                .append("]")
                .toString();
    }

    interface FOpm {
        boolean apply(int i);
    }

    VectorMask<E> opm(AbstractSpecies.FOpm f) {
        boolean[] res = new boolean[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i);
        }
        return maskFactory.apply(res);
    }
}
