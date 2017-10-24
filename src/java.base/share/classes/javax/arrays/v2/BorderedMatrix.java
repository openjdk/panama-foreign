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
 *
 * @param <T>
 */
public abstract class BorderedMatrix<T> implements Matrix<T> {
    protected final int leftWidth;
    protected final int rightWidth;
    protected final int upperWidth;
    protected final int lowerWidth;

    protected final long rightLeastIndex;
    protected final long lowerLeastIndex;

    /**
     * Coordinate (0,0) of wrappedMatrix is located at (leftWidth, upperWidth)
     * of the borderedMatrix.
     */
    protected final Matrix<T> wrappedMatrix;

    @Override
    public long length(int dim) {
        if (dim == COL_DIM) {
            return lowerLeastIndex + lowerWidth;
        } else if (dim == ROW_DIM) {
            return rightLeastIndex + rightWidth;
        } else {
            throw new IllegalArgumentException("Expected COL_DIM or ROW_DIM");
        }
    }

    protected  abstract T getUpper(long i, long j);
    protected  abstract T getLower(long i, long j);
    protected  abstract T getLeft(long i, long j);
    protected  abstract T getRight(long i, long j);
    protected  abstract T getUpperLeft(long i, long j);
    protected  abstract T getUpperRight(long i, long j);
    protected  abstract T getLowerLeft(long i, long j);
    protected  abstract T getLowerRight(long i, long j);

    protected  T getCenter(long i, long j) {
        return wrappedMatrix.get(i - leftWidth, j - upperWidth);
    }

    @Override
    public T get(long i, long j) {
        if (i < upperWidth) {
            if (j < leftWidth) {
                return getUpperLeft(i,j);
            } else if (j > rightLeastIndex) {
                return getUpperRight(i,j);
            } else {
                return getUpper(i,j);
            }
        } else if (i > lowerLeastIndex) {
            if (j < leftWidth) {
                return getLowerLeft(i,j);
            } else if (j > rightLeastIndex) {
                return getLowerRight(i,j);
            } else {
                return getLower(i,j);
            }
        } else if (j < leftWidth) {
            return getLeft(i,j);
        } else if (j > rightLeastIndex) {
            return getRight(i,j);
        } else {
            return getCenter(i,j);
        }
    }

    BorderedMatrix(Matrix<T> center, int left, int right, int upper, int lower) {
        leftWidth = left;
        rightWidth = right;
        upperWidth = upper;
        lowerWidth = lower;
        wrappedMatrix = center;
        rightLeastIndex = leftWidth + center.length(COL_DIM);
        lowerLeastIndex = upperWidth + center.length(ROW_DIM);
    }
}
