/*
 *  Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package javax.arrays.v2;

/**
 * A PermutedRowsMatrix is aliased with an existing matrix,
 * but with row addressing permuted according to a PermutationMatrix.
 *
 * Creation of a PermutedRowsMatrix marks the two input matrices
 * to be readonly (setReadOnly()) as a sanity-enhancing restriction.
 *
 * @param <T>
 */
public class PermutedRowsMatrix<T> implements Matrix<T> {

    private final Matrix<T> m;
    private final PermutationMatrix p;

    public PermutedRowsMatrix(PermutationMatrix pm, Matrix<T> m) {
        pm.setReadOnly();
        m.setReadOnly();
        this.p = pm;
        this.m = m;
    }

    @Override
    public T get(long i, long j) {
        return m.get(p.indexOfNonZeroColumnInRow(i), j);
    }

    @Override
    public void set(long i, long j, T x) {
         m.set(p.indexOfNonZeroColumnInRow(i), j, x);
    }

    @Override
    public boolean cas(long i, long j, T expected, T replacement) {
        return m.cas(p.indexOfNonZeroColumnInRow(i), j, expected, replacement);
    }

    @Override
    public long length(int d) {
        return m.length(d);
    }

   @Override
    public long size() {
        return m.size();
    }

    @Override
    public long sparseSize() {
        return m.sparseSize();
    }

    @Override
    public int preferredMajorVote() {
        int d = m.preferredMajorDim();
        // Logic: permuted rows delocalize columns.
        return d == COL_DIM ? NO_DIM : d;
    }

    @Override
    public Class<T> elementType() {
        return m.elementType();
    }
}
