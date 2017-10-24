/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javax.arrays.v2;

public class DiagonalMatrix<T> implements Matrix<T> {
    private final Array<T> diagonal;
    private final T zero;

    /**
     *
     * @param diagonal
     * @param zero The zero value.  Must have SAME CLASS as elements stored on diagonal.
     */
    public DiagonalMatrix(Array<T> diagonal, T zero) {
        // TODO -- make a copy, or not?
        this.diagonal = diagonal;
        this.zero = zero;
    }

    @Override
    public T get(long i, long j) {
        if (i != j) return zero;
        return diagonal.get(i);
    }

    @Override
    public void set(long i, long j, T x) {
         if (i != j) throw new IllegalArgumentException("Diagonal matrix insists on remaining diagonal.");
         diagonal.set(i, x);
    }

    @Override
    public boolean cas(long i, long j, T expected, T replacement) {
         if (i != j) throw new IllegalArgumentException("Diagonal matrix insists on remaining diagonal.");
         return diagonal.cas(i, expected, replacement);
    }

    @Override
    public long length(int d) {
        return diagonal.length();
    }

    @Override
    public long sparseSize() {
        return diagonal.length();
    }

    @Override
    public Matrix<T> allocateSubMatrix(long i_lo, long i_hi, long j_lo, long j_hi) {
        return new DiagonalSubMatrix<>(this, i_lo, i_hi, j_lo, j_hi);
    }

    @Override
    public int preferredMajorVote() {
        // Could weight by size, too.
        return NO_DIM;
    }

    @Override
    @SuppressWarnings("unchecked")
    // TODO I think this is a lurking problem, type is really Class<? extends T>
    public Class<T> elementType() {
        return (Class<T>) zero.getClass();
    }

    public static class DiagonalSubMatrix<T> extends SubMatrix<T> {

        private final long sparseSize;

        public DiagonalSubMatrix(DiagonalMatrix<T> within, long i_lo, long i_hi, long j_lo, long j_hi) {
            super(within, i_lo, i_hi, j_lo, j_hi);
            long sparse_size;
            // Figure out how much of the diagonal intersects.
            if (i_lo >= j_lo) {
                // upper left corner is below or on diagonal
                // D...
                // CD..
                // ..D.
                if (i_lo <= j_hi) {
                    // upper right corner is above or on diagonal
                    // D...
                    // CD.C
                    // ..D.
                    if (i_hi <= j_hi) {
                        // lower right corner is above or on diagonal
                        // D...
                        // CD.C
                        // ..DC
                        sparse_size = i_hi - i_lo;
                    } else { // i_hi > j_hi
                        // lower right corner is below diagonal
                        // D...
                        // CD.C
                        // ..D.
                        // ...D
                        // ...C
                        sparse_size = j_hi - i_lo;
                    }
                } else { // i_lo > j_hi
                    // upper right corner is BELOW diagonal, so is upper left
                    // no intersection
                    sparse_size = 0;
                }
            } else { // i_lo < j_lo
                // upper left corner is above diagonal
                // C..
                // D..
                // .D.
                if (i_hi < j_lo) {
                    // lower left corner is above diagonal
                    // C..
                    // D..
                    // .D.
                    sparse_size = 0;
                } else { // i_hi >= j_lo
                    // lower left corner is below or on diagonal
                    // C..
                    // D..
                    // CD.
                    if (i_hi >= j_hi) {
                        // lower right corner is below or on diagonal
                        sparse_size = j_hi - j_lo;
                    } else { // i_lo < j_lo
                        // lower right corner is above diagonal
                        sparse_size = i_hi - j_lo;
                    }
                }
            }
            sparseSize = sparse_size;

        }

        @Override
        public long sparseSize() {
            return sparseSize;
        }
    }
}
