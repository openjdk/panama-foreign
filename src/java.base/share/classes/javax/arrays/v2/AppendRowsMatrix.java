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

public class AppendRowsMatrix<T> implements Matrix<T> {
    final Matrix<T> top;
    final Matrix<T> bottom;
    final long topRowSize;

    AppendRowsMatrix(Matrix<T> top, Matrix<T> bottom) {
        if (top.length(Matrix.COL_DIM) != bottom.length(Matrix.COL_DIM)) {
            throw new IllegalArgumentException( "" +
                    top.length(Matrix.COL_DIM) +
                    " cannot append to " + bottom.length(Matrix.COL_DIM));
        }
        this.top = top;
        this.bottom = bottom;
        this.topRowSize = top.length(Matrix.ROW_DIM);
    }

    @Override
    public T get(long i, long j) {
        if (i < topRowSize) {
            return top.get(i, j);
        } else {
            return bottom.get(i - topRowSize, j);
        }
    }

    @Override
    public void set(long i, long j, T x) {
        if (i < topRowSize) {
            top.set(i, j, x);
        } else {
            bottom.set(i - topRowSize, j, x);
        }
    }

    @Override
    public boolean cas(long i, long j, T expected, T replacement) {
        if (i < topRowSize) {
            return top.cas(i, j, expected, replacement);
        } else {
            return bottom.cas(i - topRowSize, j, expected, replacement);
        }
    }

    @Override
    public long length(int d) {
        if (d == Matrix.COL_DIM) {
            return top.length(Matrix.COL_DIM);
        } else {
            return topRowSize + bottom.length(d);
        }
    }

    @Override
    public long middle(int d) {
        if (d == Matrix.COL_DIM) {
            return A2Expr.firstNotEqual(top.length(Matrix.COL_DIM) >> 1, top.middle(Matrix.COL_DIM), bottom.middle(Matrix.COL_DIM));
        } else {
            long candidate = length(Matrix.ROW_DIM) >> 1;
            long offset = Math.abs(candidate - topRowSize);
            // If within (1/4, 3/4), that is okay.
            if (offset << 1 < candidate) {
                return topRowSize;
            }
            return candidate;
        }
    }

    @Override
    public Matrix<T> subMatrix(long i_lo, long i_hi, long j_lo, long j_hi) {
        if (!(0 <= i_lo && i_lo <= i_hi & i_hi <= length(Matrix.ROW_DIM) && 0 <= j_lo && j_lo <= j_hi & j_hi <= length(Matrix.COL_DIM))) {
            throw new IndexOutOfBoundsException();
        }
        if (0 == i_lo && i_hi == length(Matrix.ROW_DIM) && 0 == j_lo && j_lo == length(Matrix.COL_DIM)) {
            return this;
        }
        if (i_lo <= topRowSize && i_hi <= topRowSize) {
            return top.subMatrix(i_lo, i_hi, j_lo, j_hi);
        }
        if (i_lo >= topRowSize && i_hi >= topRowSize) {
            return top.subMatrix(i_lo - topRowSize, i_hi - topRowSize, j_lo, j_hi);
        }
        return new SubMatrix<>(this, i_lo, i_hi, j_lo, j_hi);
    }

    @Override
    public Array<T> getRow(long i) {
        if (i < topRowSize) {
            return top.getRow(i);
        }
        return bottom.getRow(i - topRowSize);
    }

    @Override
    public int preferredMajorVote() {
        // Could weight by size, too.
        return (top.preferredMajorVote() + bottom.preferredMajorVote())/2;
    }

    @Override
    public Class<T> elementType() {
        return top.elementType();
    }
}
