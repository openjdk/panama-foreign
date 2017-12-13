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

final class GenericShuffle<E, S extends Vector.Shape<Vector<?, ?>>> implements Vector.Shuffle<E, S> {

    private final Vector.Species<E, S> s;
    private final int[] reorder;

    public GenericShuffle(Vector.Species<E, S> s, int[] reorder) {
        this.s = s;
        if (reorder.length != s.length())
            throw new ArrayIndexOutOfBoundsException("Reorder array length must match species length");
        this.reorder = reorder.clone();
    }

    @Override
    public int[] toArray() {
        int[] ar = new int[getSpecies().length()];
        System.arraycopy(reorder, 0, ar, 0, getSpecies().length());
        return ar;
    }

    @Override
    public Vector.Species<E, S> getSpecies() {
        return s;
    }

    @Override
    public Vector<Integer, S> toVector() {
        IntVector.IntSpecies<S> res = (IntVector.IntSpecies<S>) Vector.speciesInstance(Integer.class, s.shape());
        return res.fromArray(reorder, 0);
    }

    @Override
    public <E1> Vector<E1, S> toVector(Class<E1> type) {
        return toVector().cast(type);
    }

    @Override
    public int getElement(int i) {
        return reorder[i];
    }
}
