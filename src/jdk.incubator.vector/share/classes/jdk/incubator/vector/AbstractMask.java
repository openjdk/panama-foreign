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

import java.util.Arrays;

abstract class AbstractMask<E> extends VectorMask<E> {

    /*package-private*/
    abstract boolean[] getBits();

    // Unary operator

    interface MUnOp {
        boolean apply(int i, boolean a);
    }

    abstract AbstractMask<E> uOp(MUnOp f);

    // Binary operator

    interface MBinOp {
        boolean apply(int i, boolean a, boolean b);
    }

    abstract AbstractMask<E> bOp(VectorMask<E> o, MBinOp f);

    @Override
    public String toString() {
        return Arrays.toString(getBits());
    }

    @Override
    public boolean lane(int i) {
        return getBits()[i];
    }

    @Override
    public long toLong() {
        long res = 0;
        long set = 1;
        boolean[] bits = getBits();
        for (int i = 0; i < species().length(); i++) {
            res = bits[i] ? res | set : res;
            set = set << 1;
        }
        return res;
    }

    @Override
    public void intoArray(boolean[] bits, int i) {
        System.arraycopy(getBits(), 0, bits, i, species().length());
    }

    @Override
    public boolean[] toArray() {
        return getBits().clone();
    }

    @Override
    public int trueCount() {
        int c = 0;
        for (boolean i : getBits()) {
            if (i) c++;
        }
        return c;
    }

    @Override
    public AbstractMask<E> and(VectorMask<E> o) {
        return bOp(o, (i, a, b) -> a && b);
    }

    @Override
    public AbstractMask<E> or(VectorMask<E> o) {
        return bOp(o, (i, a, b) -> a || b);
    }

    @Override
    public AbstractMask<E> not() {
        return uOp((i, a) -> !a);
    }

    /*package-private*/
    static boolean anyTrueHelper(boolean[] bits) {
        for (boolean i : bits) {
            if (i) return true;
        }
        return false;
    }

    /*package-private*/
    static boolean allTrueHelper(boolean[] bits) {
        for (boolean i : bits) {
            if (!i) return false;
        }
        return true;
    }

    // @@@ This is a bad implementation -- makes lambdas capturing -- fix this
    @SuppressWarnings("unchecked")
    static <E> VectorMask<E> trueMask(VectorSpecies<E> species) {
        Class<?> eType = species.elementType();

        if (eType == byte.class) {
            if (species.vectorType() == ByteMaxVector.class)
                return (VectorMask<E>) ByteMaxVector.ByteMaxMask.TRUE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Byte64Vector.Byte64Mask.TRUE_MASK;
                case 128: return (VectorMask<E>) Byte128Vector.Byte128Mask.TRUE_MASK;
                case 256: return (VectorMask<E>) Byte256Vector.Byte256Mask.TRUE_MASK;
                case 512: return (VectorMask<E>) Byte512Vector.Byte512Mask.TRUE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else if (eType == short.class) {
            if (species.vectorType() == ShortMaxVector.class)
                return (VectorMask<E>) ShortMaxVector.ShortMaxMask.TRUE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Short64Vector.Short64Mask.TRUE_MASK;
                case 128: return (VectorMask<E>) Short128Vector.Short128Mask.TRUE_MASK;
                case 256: return (VectorMask<E>) Short256Vector.Short256Mask.TRUE_MASK;
                case 512: return (VectorMask<E>) Short512Vector.Short512Mask.TRUE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else if (eType == int.class) {
            if (species.vectorType() == IntMaxVector.class)
                return (VectorMask<E>) IntMaxVector.IntMaxMask.TRUE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Int64Vector.Int64Mask.TRUE_MASK;
                case 128: return (VectorMask<E>) Int128Vector.Int128Mask.TRUE_MASK;
                case 256: return (VectorMask<E>) Int256Vector.Int256Mask.TRUE_MASK;
                case 512: return (VectorMask<E>) Int512Vector.Int512Mask.TRUE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else if (eType == long.class) {
            if (species.vectorType() == LongMaxVector.class)
                return (VectorMask<E>) LongMaxVector.LongMaxMask.TRUE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Long64Vector.Long64Mask.TRUE_MASK;
                case 128: return (VectorMask<E>) Long128Vector.Long128Mask.TRUE_MASK;
                case 256: return (VectorMask<E>) Long256Vector.Long256Mask.TRUE_MASK;
                case 512: return (VectorMask<E>) Long512Vector.Long512Mask.TRUE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else if (eType == float.class) {
            if (species.vectorType() == FloatMaxVector.class)
                return (VectorMask<E>) FloatMaxVector.FloatMaxMask.TRUE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Float64Vector.Float64Mask.TRUE_MASK;
                case 128: return (VectorMask<E>) Float128Vector.Float128Mask.TRUE_MASK;
                case 256: return (VectorMask<E>) Float256Vector.Float256Mask.TRUE_MASK;
                case 512: return (VectorMask<E>) Float512Vector.Float512Mask.TRUE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else if (eType == double.class) {
            if (species.vectorType() == DoubleMaxVector.class)
                return (VectorMask<E>) DoubleMaxVector.DoubleMaxMask.TRUE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Double64Vector.Double64Mask.TRUE_MASK;
                case 128: return (VectorMask<E>) Double128Vector.Double128Mask.TRUE_MASK;
                case 256: return (VectorMask<E>) Double256Vector.Double256Mask.TRUE_MASK;
                case 512: return (VectorMask<E>) Double512Vector.Double512Mask.TRUE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else {
            throw new IllegalArgumentException("Bad element type of species");
        }
    }

    // @@@ This is a bad implementation -- makes lambdas capturing -- fix this
    @SuppressWarnings("unchecked")
    static <E> VectorMask<E> falseMask(VectorSpecies<E> species) {
        Class<?> eType = species.elementType();

        if (eType == byte.class) {
            if (species.vectorType() == ByteMaxVector.class)
                return (VectorMask<E>) ByteMaxVector.ByteMaxMask.FALSE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Byte64Vector.Byte64Mask.FALSE_MASK;
                case 128: return (VectorMask<E>) Byte128Vector.Byte128Mask.FALSE_MASK;
                case 256: return (VectorMask<E>) Byte256Vector.Byte256Mask.FALSE_MASK;
                case 512: return (VectorMask<E>) Byte512Vector.Byte512Mask.FALSE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else if (eType == short.class) {
            if (species.vectorType() == ShortMaxVector.class)
                return (VectorMask<E>) ShortMaxVector.ShortMaxMask.FALSE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Short64Vector.Short64Mask.FALSE_MASK;
                case 128: return (VectorMask<E>) Short128Vector.Short128Mask.FALSE_MASK;
                case 256: return (VectorMask<E>) Short256Vector.Short256Mask.FALSE_MASK;
                case 512: return (VectorMask<E>) Short512Vector.Short512Mask.FALSE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else if (eType == int.class) {
            if (species.vectorType() == IntMaxVector.class)
                return (VectorMask<E>) IntMaxVector.IntMaxMask.FALSE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Int64Vector.Int64Mask.FALSE_MASK;
                case 128: return (VectorMask<E>) Int128Vector.Int128Mask.FALSE_MASK;
                case 256: return (VectorMask<E>) Int256Vector.Int256Mask.FALSE_MASK;
                case 512: return (VectorMask<E>) Int512Vector.Int512Mask.FALSE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else if (eType == long.class) {
            if (species.vectorType() == LongMaxVector.class)
                return (VectorMask<E>) LongMaxVector.LongMaxMask.FALSE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Long64Vector.Long64Mask.FALSE_MASK;
                case 128: return (VectorMask<E>) Long128Vector.Long128Mask.FALSE_MASK;
                case 256: return (VectorMask<E>) Long256Vector.Long256Mask.FALSE_MASK;
                case 512: return (VectorMask<E>) Long512Vector.Long512Mask.FALSE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else if (eType == float.class) {
            if (species.vectorType() == FloatMaxVector.class)
                return (VectorMask<E>) FloatMaxVector.FloatMaxMask.FALSE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Float64Vector.Float64Mask.FALSE_MASK;
                case 128: return (VectorMask<E>) Float128Vector.Float128Mask.FALSE_MASK;
                case 256: return (VectorMask<E>) Float256Vector.Float256Mask.FALSE_MASK;
                case 512: return (VectorMask<E>) Float512Vector.Float512Mask.FALSE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else if (eType == double.class) {
            if (species.vectorType() == DoubleMaxVector.class)
                return (VectorMask<E>) DoubleMaxVector.DoubleMaxMask.FALSE_MASK;
            switch (species.bitSize()) {
                case 64: return (VectorMask<E>) Double64Vector.Double64Mask.FALSE_MASK;
                case 128: return (VectorMask<E>) Double128Vector.Double128Mask.FALSE_MASK;
                case 256: return (VectorMask<E>) Double256Vector.Double256Mask.FALSE_MASK;
                case 512: return (VectorMask<E>) Double512Vector.Double512Mask.FALSE_MASK;
                default: throw new IllegalArgumentException(Integer.toString(species.bitSize()));
            }
        } else {
            throw new IllegalArgumentException("Bad element type of species");
        }
    }
}
