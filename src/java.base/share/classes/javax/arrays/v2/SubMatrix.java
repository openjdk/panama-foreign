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

import javax.arrays.tbd.LongLongFunction;

public class SubMatrix<T> implements Matrix<T> {
    public final Matrix<T> within;
    public final long rLo;
    public final long rHi;
    public final long cLo;
    public final long cHi;

    SubMatrix(Matrix<T> within, long i_lo, long i_hi, long j_lo, long j_hi) {
        if (i_lo < 0 || j_lo < 0 || i_hi > within.length(Matrix.ROW_DIM) || j_hi > within.length(Matrix.COL_DIM)) {
            throw new IndexOutOfBoundsException();
        }
        this.within = within;
        rLo = i_lo;
        rHi = i_hi;
        cLo = j_lo;
        cHi = j_hi;
    }

    @Override
    public T get(long i, long j) {
        if (i < 0 || j < 0) {
            throw new IndexOutOfBoundsException();
        }
        long w_i = i + rLo;
        long w_j = j + cLo;
        if (w_i >= rHi || w_j >= cHi) {
            throw new IndexOutOfBoundsException();
        }
        return within.get(w_i, w_j);
    }

    @Override
    public void set(long i, long j, T x) {
        if (i < 0 || j < 0) {
            throw new IndexOutOfBoundsException();
        }
        long w_i = i + rLo;
        long w_j = j + cLo;
        if (w_i >= rHi || w_j >= cHi) {
            throw new IndexOutOfBoundsException();
        }
        within.set(w_i, w_j, x);
    }

    @Override
    public boolean cas(long i, long j, T expected, T replacement) {
        if (i < 0 || j < 0) {
            throw new IndexOutOfBoundsException();
        }
        long w_i = i + rLo;
        long w_j = j + cLo;
        if (w_i >= rHi || w_j >= cHi) {
            throw new IndexOutOfBoundsException();
        }
        return within.cas(w_i, w_j, expected, replacement);
    }

    @Override
    public long length(int d) {
        if (d == Matrix.ROW_DIM) {
            return rHi - rLo;
        } else if (d == Matrix.COL_DIM) {
            return cHi - cLo;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Matrix<T> subMatrix(long i_lo, long i_hi, long j_lo, long j_hi) {
        if (!(0 <= i_lo && i_lo <= i_hi & i_hi <= length(Matrix.ROW_DIM) && 0 <= j_lo && j_lo <= j_hi & j_hi <= length(Matrix.COL_DIM))) {
            throw new IndexOutOfBoundsException();
        }
        if (0 == i_lo && i_hi == length(Matrix.ROW_DIM) && 0 == j_lo && j_lo == length(Matrix.COL_DIM)) {
            return this;
        }
        return within.subMatrix(rLo + i_lo, rLo + i_hi, cLo + j_lo, cLo + j_hi);
    }

    @Override
    public Matrix<T> appendRows(Matrix<T> a) {
        if (a instanceof SubMatrix) {
            SubMatrix<T> sma = (SubMatrix<T>) a;
            // If same parent and geometry aligns
            if (sma.parent() == parent() && endRow() == sma.beginRow() && beginColumn() == sma.beginColumn() && endColumn() == sma.endColumn()) {
                // get a different submatrix from the parent.
                return parent().subMatrix(beginRow(), sma.endRow(), beginColumn(), endColumn());
            }
        }
        return new AppendRowsMatrix<>(this, a);
    }

    @Override
    public Matrix<T> appendColumns(Matrix<T> a) {
        if (a instanceof SubMatrix) {
            SubMatrix<T> sma = (SubMatrix<T>) a;
            // If same parent and geometry aligns
            if (sma.parent() == parent() && endColumn() == sma.beginColumn() && beginRow() == sma.beginRow() && endRow() == sma.endRow()) {
                // get a different submatrix from the parent.
                return parent().subMatrix(beginRow(), endRow(), beginColumn(), sma.endColumn());
            }
        }
        return new AppendColumnsMatrix<>(this, a);
    }

    public Matrix<T> parent() {
        return within;
    }

    public long end(int d) {
        return d == Matrix.ROW_DIM ? rHi : cHi;
    }

    public long begin(int d) {
        return d == Matrix.ROW_DIM ? rLo : cLo;
    }

    public long endRow() {
        return rHi;
    }

    public long beginRow() {
        return rLo;
    }

    public long endColumn() {
        return cHi;
    }

    public long beginColumn() {
        return cLo;
    }

    @Override
    public void setSubMatrix(LongLongFunction<T> filler,
            long i_lo, long i_hi,
            long j_lo, long j_hi, long delta_i, long delta_j) {
        Matrix<T> target = parent();
        long i_off = beginRow();
        long j_off = beginColumn();
        i_lo += i_off;
        j_lo += j_off;
        i_hi += i_off;
        j_hi += j_off;
        delta_i -= i_off;
        delta_j -= j_off;

        target.setSubMatrix(filler, i_lo, i_hi, j_lo, j_hi, delta_i, delta_j);
    }

    // Additional refactoring did not go so smoothly because of operands and their offsets.

    @Override
    public int preferredMajorVote() {
        return parent().preferredMajorVote();
    }

    @Override
    public Class<T> elementType() {
        return within.elementType();
    }


}
