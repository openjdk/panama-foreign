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

abstract class AbstractMask<E, S extends Vector.Shape<Vector<?, ?>>> implements Vector.Mask<E, S> {
    final boolean[] bits;

    AbstractMask(boolean[] bits) {
        if (bits.length != species().length())
            throw new ArrayIndexOutOfBoundsException("Boolean array must be the same length as the masked vector");

        this.bits = bits.clone();
    }

    AbstractMask(boolean val) {
        this.bits = new boolean[species().length()];
        for (int i = 0; i < this.bits.length; i++) {
            this.bits[i] = val;
        }
    }

    // Unary operator

    interface MUnOp {
        boolean apply(int i, boolean a);
    }

    abstract AbstractMask<E, S> uOp(MUnOp f);

    // Binary operator

    interface MBinOp {
        boolean apply(int i, boolean a, boolean b);
    }

    abstract AbstractMask<E, S> bOp(Vector.Mask<E, S> o, MBinOp f);

    @Override
    public String toString() {
        return Arrays.toString(bits);
    }

    @Override
    public boolean getElement(int i) {
        return bits[i];
    }

    @Override
    public long toLong() {
        long res = 0;
        long set = 1;
        for (int i = 0; i < species().length(); i++) {
            res = bits[i] ? res | set : res;
            set = set << 1;
        }
        return res;
    }

    @Override
    public boolean[] toArray() {
        return bits.clone();
    }

    @Override
    public boolean anyTrue() {
        for (boolean i : bits) {
            if (i) return true;
        }
        return false;
    }

    @Override
    public boolean allTrue() {
        for (boolean i : bits) {
            if (!i) return false;
        }
        return true;
    }

    @Override
    public int trueCount() {
        int c = 0;
        for (boolean i : bits) {
            if (i) c++;
        }
        return c;
    }

    @Override
    public AbstractMask<E, S> and(Vector.Mask<E, S> o) {
        return bOp(o, (i, a, b) -> a && b);
    }

    @Override
    public AbstractMask<E, S> or(Vector.Mask<E, S> o) {
        return bOp(o, (i, a, b) -> a || b);
    }

    @Override
    public AbstractMask<E, S> not() {
        return uOp((i, a) -> !a);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Z> Vector<Z, S> toVector(Class<Z> e) {
        Vector.Species<Z, S> species = Vector.speciesInstance(e, species().shape());
        if (e == Float.class) {
            float[] ar = new float[species().length()];
            for (int i = 0; i < species().length(); i++) {
                ar[i] = bits[i] ? Float.intBitsToFloat(-1) : Float.intBitsToFloat(0);
            }
            return (Vector<Z, S>) ((FloatVector.FloatSpecies<S>) species).fromArray(ar, 0);
        }
        else if (e == Double.class) {
            double[] ar = new double[species().length()];
            for (int i = 0; i < species().length(); i++) {
                ar[i] = bits[i] ? Double.longBitsToDouble(-1) : Double.longBitsToDouble(0);
            }
            return (Vector<Z, S>) ((DoubleVector.DoubleSpecies<S>) species).fromArray(ar, 0);
        }
        else if (e == Integer.class) {
            int[] ar = new int[species().length()];
            for (int i = 0; i < species().length(); i++) {
                ar[i] = bits[i] ? -1 : 0;
            }
            return (Vector<Z, S>) ((IntVector.IntSpecies<S>) species).fromArray(ar, 0);
        }
        else if (e == Long.class) {
            long[] ar = new long[species().length()];
            for (int i = 0; i < species().length(); i++) {
                ar[i] = bits[i] ? -1 : 0;
            }
            return (Vector<Z, S>) ((LongVector.LongSpecies<S>) species).fromArray(ar, 0);
        }
        else if (e == Short.class) {
            short[] ar = new short[species().length()];
            for (int i = 0; i < species().length(); i++) {
                ar[i] = (short) (bits[i] ? -1 : 0);
            }
            return (Vector<Z, S>) ((ShortVector.ShortSpecies<S>) species).fromArray(ar, 0);
        }
        else if (e == Byte.class) {
            byte[] ar = new byte[species().length()];
            for (int i = 0; i < species().length(); i++) {
                ar[i] = (byte) (bits[i] ? -1 : 0);
            }
            return (Vector<Z, S>) ((ByteVector.ByteSpecies<S>) species).fromArray(ar, 0);
        }
        else {
            throw new UnsupportedOperationException("Invalid vector element specified for Mask.");
        }
    }

    @Override
    public <F, Z extends Vector.Shape<Vector<?, ?>>>
    Vector.Mask<F, Z> reshape(Class<F> type, Z shape) {
        Vector.Species<F, Z> species = Vector.speciesInstance(type, shape);
        return species.constantMask(Arrays.copyOf(bits, species.length()));
    }
}
