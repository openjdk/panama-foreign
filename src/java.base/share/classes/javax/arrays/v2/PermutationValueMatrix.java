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

public final class PermutationValueMatrix<T> implements Matrix<T> {

    private final PermutationMatrix pm;
    private final T zero;
    private final T one;

    public PermutationValueMatrix(PermutationMatrix pm, T zero, T one) {
        this.zero = zero;
        this.one = one;
        this.pm = pm;
        pm.setReadOnly();
    }

    @Override
    public T get(long i, long j) {
        return pm.get(i, j) ? one : zero;
    }

    @Override
    public void set(long i, long j, T x) {
        throw new UnsupportedOperationException("Cannot assign into a permutation matrix");
    }

    @Override
    public boolean cas(long i, long j, T expected, T replacement) {
        throw new UnsupportedOperationException("Cannot assign into a permutation matrix");
    }



    @Override
    public long length(int d) {
        return pm.length(d);
    }
    @Override
    public long size() {
        return pm.size();
    }
    @Override
    public long sparseSize() {
        return pm.size();
    }

    @Override
    public int preferredMajorVote() {
        return NO_DIM;
    }

    @Override
    @SuppressWarnings("unchecked")
    // TODO I think this is a lurking problem, type is really Class<? extends T>
    public Class<T> elementType() {
        return (Class<T>) zero.getClass();
    }


}
