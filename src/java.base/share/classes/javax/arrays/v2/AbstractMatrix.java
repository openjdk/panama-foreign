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
 * Handy base class for other concrete implementations.
 * @param <T>
 */
abstract public class AbstractMatrix<T> implements Matrix<T> {

    protected final long size;
    protected final long nRows;
    protected final long nCols;
    protected final Class<T> eltClass;

    protected AbstractMatrix(Class<T> eltClass, long nRows, long nCols) {
        this.size = nRows * nCols;
        this.nRows = nRows;
        this.nCols = nCols;
        this.eltClass = eltClass;
    }

    @Override
    public long length(int d) {
        return d == ROW_DIM ? nRows : nCols;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public Class<T> elementType() {
        return eltClass;
    }

    public static void checkValidIndex(long i, int l) throws ArrayIndexOutOfBoundsException {
        if (i > l || i < 0)
            throw new ArrayIndexOutOfBoundsException();
    }


    /**
     * Common methods for all the flattened-storage row-major matrices.
     *
     * @param <T>
     */
    abstract public static class RowMajor<T> extends AbstractMatrix<T> {

        protected RowMajor(Class<T> eltClass, long nRows, long nCols) {
            super(eltClass, nRows, nCols);
        }

        @Override
        public int preferredMajorVote() {
            return ROW_DIM;
        }
    }

    /**
     * Common methods for all the flattened-storage column-major matrices.
     *
     * @param <T>
     */
    abstract public static class ColumnMajor<T> extends AbstractMatrix<T> {

        protected ColumnMajor(Class<T> eltClass, long nRows, long nCols) {
            super(eltClass, nRows, nCols);
        }

        @Override
        public int preferredMajorVote() {
            return COL_DIM;
        }
    }
}
