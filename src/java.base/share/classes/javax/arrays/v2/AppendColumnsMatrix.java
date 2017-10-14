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
 * A matrix aliased with the concatenation of the columns of the two matrices
 * left and right.  Left and Right must have the same number of rows.
 *
 * @param <T>
 */
public class AppendColumnsMatrix<T> implements Matrix<T> {
    final Matrix<T> left;
    final Matrix<T> right;
    final long leftColumnSize;

    AppendColumnsMatrix(Matrix<T> left, Matrix<T> right) {
        if (left.length(Matrix.ROW_DIM) != right.length(Matrix.ROW_DIM)) {
            throw new IllegalArgumentException();
        }
        this.left = left;
        this.right = right;
        this.leftColumnSize = left.length(Matrix.COL_DIM);
    }

    @Override
    public T get(long i, long j) {
        if (j < leftColumnSize) {
            return left.get(i, j);
        } else {
            return right.get(i, j - leftColumnSize);
        }
    }

    @Override
    public void set(long i, long j, T x) {
        if (j < leftColumnSize) {
            left.set(i, j, x);
        } else {
            right.set(i, j - leftColumnSize, x);
        }
    }

    @Override
    public boolean cas(long i, long j, T expected, T replacement) {
       if (j < leftColumnSize) {
            return left.cas(i, j, expected, replacement);
        } else {
            return right.cas(i, j - leftColumnSize, expected, replacement);
        }
    }

    @Override
    public long length(int d) {
        if (d == Matrix.ROW_DIM) {
            return left.length(Matrix.ROW_DIM);
        } else {
            return leftColumnSize + right.length(d);
        }
    }

    @Override
    public long middle(int d) {
        if (d == Matrix.ROW_DIM) {
            return A2Expr.firstNotEqual(left.length(Matrix.ROW_DIM) >> 1, left.middle(Matrix.ROW_DIM), right.middle(Matrix.ROW_DIM));
        } else {
            long candidate = length(Matrix.COL_DIM) >> 1;
            long offset = Math.abs(candidate - leftColumnSize);
            // If within (1/4, 3/4), that is okay.
            if (offset << 1 < candidate) {
                return leftColumnSize;
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
        if (j_lo <= leftColumnSize && j_hi <= leftColumnSize) {
            return left.subMatrix(i_lo, i_hi, j_lo, j_hi);
        }
        if (j_lo >= leftColumnSize && j_hi >= leftColumnSize) {
            return right.subMatrix(i_lo, i_hi, j_lo - leftColumnSize, j_hi - leftColumnSize);
        }
        return new SubMatrix<>(this, i_lo, i_hi, j_lo, j_hi);
    }

    @Override
    public Array<T> getColumn(long i) {
        if (i < leftColumnSize) {
            return left.getColumn(i);
        }
        return right.getColumn(i - leftColumnSize);
    }

    @Override
    public int preferredMajorVote() {
        // Could weight by size, too.
        return (left.preferredMajorVote() + right.preferredMajorVote())/2;
    }

    @Override
    public Class<T> elementType() {
        return left.elementType();
    }
}
