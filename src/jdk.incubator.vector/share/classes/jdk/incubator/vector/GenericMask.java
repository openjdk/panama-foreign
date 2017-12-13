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

final class GenericMask<E, S extends Vector.Shape<Vector<?, ?>>> implements Vector.Mask<E, S> {

    private final Vector.Species<E, S> s;
    private final boolean[] bits;

    public GenericMask(Vector.Species<E, S> s, boolean[] bits) {
        this.s = s;

        if (bits.length != species().length())
            throw new ArrayIndexOutOfBoundsException("Boolean array must be the same length as the masked vector");

        this.bits = bits.clone();
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
        boolean res = true;
        for (boolean i : bits) {
            res = res && i;
        }
        return res;
    }

    @Override
    public Vector.Species<E, S> species() {
        return s;
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
            throw new UnsupportedOperationException("Invalid vector element specified for GenericMask.");
        }
    }

    @Override
    public <F, Z extends Vector.Shape<Vector<?, ?>>>
    GenericMask<F, Z> reshape(Class<F> type, Z shape) {
        Vector.Species<F, Z> species = Vector.speciesInstance(type, shape);
        return new GenericMask<>(species, Arrays.copyOf(bits, species.length()));
    }

    @Override
    public boolean getElement(int i) {
        return bits[i];
    }
}
