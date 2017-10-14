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

import static javax.arrays.v2.A2Expr.transposeDim;

/**
 *
 */
public class TransposedViewMatrix<T> implements Matrix<T> {

    private final Matrix<T> m;

    public TransposedViewMatrix(Matrix<T> m) {
        this.m = m;
    }

    @Override
    public T get(long i, long j) {
        return m.get(j,i);
    }

    @Override
    public void set(long i, long j, T x) {
        m.set(j,i,x);
    }

    @Override
    public boolean cas(long i, long j, T expected, T replacement) {
        return m.cas(j,i,expected, replacement);
    }

    @Override
    public int preferredMajorVote() {
        return transposeDim(m.preferredMajorVote());
    }

    @Override
    public Class<T> elementType() {
        return m.elementType();
    }

    @Override
    public long length(int d) {
        return m.length(transposeDim(d));
    }
}
