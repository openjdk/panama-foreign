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
import jdk.internal.vm.annotation.Stable;

/**
 * A {@code VectorShape} governs the total size, in bits, of a
 * {@link Vector}, {@link VectorMask}, or {@link VectorShuffle}.  The shape in
 * combination with the element type together govern the number of lanes.
 */
public enum VectorShape {
    /** Shape of length 64 bits */
    S_64_BIT(64),
    /** Shape of length 128 bits */
    S_128_BIT(128),
    /** Shape of length 256 bits */
    S_256_BIT(256),
    /** Shape of length 512 bits */
    S_512_BIT(512),
    /** Shape of maximum length supported on the platform */
    S_Max_BIT(Unsafe.getUnsafe().getMaxVectorSize(byte.class) * 8);

    @Stable
    final int bitSize;

    VectorShape(int bitSize) {
        this.bitSize = bitSize;
    }

    /**
     * Returns the size, in bits, of this shape.
     *
     * @return the size, in bits, of this shape.
     */
    public int bitSize() {
        return bitSize;
    }

    /**
     * Return the number of lanes of a vector of this shape and whose element
     * type is of the provided species
     *
     * @param s the species describing the element type
     * @return the number of lanes
     */
    int length(VectorSpecies<?> s) {
        return bitSize() / s.elementSize();
    }

    /**
     * Finds appropriate shape depending on bitsize.
     *
     * @param bitSize the size in bits
     * @return the shape corresponding to bitsize
     * @see #bitSize
     */
    public static VectorShape forBitSize(int bitSize) {
        switch (bitSize) {
            case 64:
                return VectorShape.S_64_BIT;
            case 128:
                return VectorShape.S_128_BIT;
            case 256:
                return VectorShape.S_256_BIT;
            case 512:
                return VectorShape.S_512_BIT;
            default:
                if ((bitSize > 0) && (bitSize <= 2048) && (bitSize % 128 == 0)) {
                    return VectorShape.S_Max_BIT;
                } else {
                    throw new IllegalArgumentException("Bad vector bit size: " + bitSize);
                }
        }
    }
}

