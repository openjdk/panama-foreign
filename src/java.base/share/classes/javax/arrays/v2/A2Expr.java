/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 *
 */

package javax.arrays.v2;

import javax.arrays.v2.nodes.Simplifier;
import javax.arrays.v2.nodes.MatrixTemporary;

/**
 *
 * @param <T>
 */
public interface A2Expr<T> {

    public final static class Parameters {

        static final int DEFAULT_ELEMENTWISE_BLOCKSIZE = 256;
        static final int DEFAULT_PRODUCT_BLOCKSIZE = 36;
        static final int DEFAULT_LOG_ARRAY_BLOCK_SIZE = 12;

        static int ELEMENTWISE_BLOCKSIZE = DEFAULT_ELEMENTWISE_BLOCKSIZE;
        static int PRODUCT_BLOCKSIZE = DEFAULT_PRODUCT_BLOCKSIZE;
        static int LOG_ARRAY_BLOCK_SIZE = DEFAULT_LOG_ARRAY_BLOCK_SIZE; // no way to change yet

        public static void setBlockSizes(int element_wise_blocksize, int product_blocksize) {
            ELEMENTWISE_BLOCKSIZE = element_wise_blocksize <= 0 ? DEFAULT_ELEMENTWISE_BLOCKSIZE : element_wise_blocksize;
            PRODUCT_BLOCKSIZE = product_blocksize <= 0 ? DEFAULT_PRODUCT_BLOCKSIZE : product_blocksize;
        }

        public static void defaultBlockSizes() {
            setBlockSizes(DEFAULT_ELEMENTWISE_BLOCKSIZE, DEFAULT_PRODUCT_BLOCKSIZE);
        }

        private Parameters() {
        }

    }

    public static int elementWiseBlockSize() {
            return Parameters.ELEMENTWISE_BLOCKSIZE;
    }

    public static int logArrayBlockSize() {
            return Parameters.LOG_ARRAY_BLOCK_SIZE;
    }

   public static int productBlockSize() {
            return Parameters.PRODUCT_BLOCKSIZE;
    }

   public static int arrayBlockSize() {
            return Parameters.ELEMENTWISE_BLOCKSIZE;
    }

   static long firstNotEqual(long m0, long m1, long m2, long m3, long m4, long m5) {
        // If any of these are not midpoint, take it.
        if (m1 != m0) {
            return m1;
        } else if (m2 != m0) {
            return m2;
        } else if (m3 != m0) {
            return m3;
        } else if (m4 != m0) {
            return m4;
        } else if (m5 != m0) {
            return m5;
        }
        return m0;
    }

   static long firstNotEqual(long m0, long m1, long m2, long m3, long m4) {
        // If any of these are not midpoint, take it.
        if (m1 != m0) {
            return m1;
        } else if (m2 != m0) {
            return m2;
        } else if (m3 != m0) {
            return m3;
        } else if (m4 != m0) {
            return m4;
        }
        return m0;
    }

   /**
     * Returns the value of the first parameter not equal to m0,
     * or m0 if all are equal.
     *
     * @param m0
     * @param m1
     * @param m2
     * @param m3
     * @return
     */
    static long firstNotEqual(long m0, long m1, long m2, long m3) {
        // If any of these are not midpoint, take it.
        if (m1 != m0) {
            return m1;
        } else if (m2 != m0) {
            return m2;
        } else if (m3 != m0) {
            return m3;
        }
        return m0;
    }

   /**
     * Returns the value of the first parameter not equal to m0,
     * or m0 if all are equal.
     *
     * @param m0
     * @param m1
     * @param m2
     * @return
     */
    static long firstNotEqual(long m0, long m1, long m2) {
        // If any of these are not midpoint, take it.
        if (m1 != m0) {
            return m1;
        } else if (m2 != m0) {
            return m2;
        }
        return m0;
    }

    /* Dimension encodings chosen to cancel when added and to have signs. */
    final static int NO_DIM = 0;
    final static int ROW_DIM = 1;
    final static int COL_DIM = -1;

    /**
     * Returns a multiple of ROW_DIM for row major, a multiple of COL_DIM for
     * column major, NO_DIM if no preference.  The "major" axis is the one to
     * hold constant for better locality (i.e., A[i,j] and A[i,j+1] are likely
     * adjacent if A is row major.
     *
     * Note that this is a "vote" -- in the sum RM + RM + RM + CM, the
     * overall expression has a vote for 2*RM.
     * @return
     */
    int preferredMajorVote();

    default int preferredMajorDim() {
        return canonicalizeMajorDim(preferredMajorVote());
    }

    static int canonicalizeMajorDim(int dim) {
        if (dim > 0) return ROW_DIM;
        else if (dim < 0) return COL_DIM;
        else return NO_DIM;
    }

    static int transposeDim(int dim) {
        return -dim;
    }

    Class<T> elementType();

    /**
     * Returns the size of the matrix's dimension d (d is ROW_DIM for rows or
     * COL_DIM for columns).
     *
     * @param d the dimension whose size is requested, ROW_DIM or COL_DIM.
     * @return the size of the d'th dimension.
     */
    long length(int d);

    /**
     * A matrix with internal structure may prefer a different "middle" from the
     * half-way point. The biased middle should lie within the two center
     * quartiles of the index space -- i.e, between size(d)/4 and 3*size(d)/4.
     *
     * @param d
     * @return
     */
    default long middle(int d) {
        return length(d) >> 1;
    }

    default long middle(int d, long beginInclusive, long endExclusive) {
        return (beginInclusive + endExclusive) >>> 1;
    }

    /**
     * Returns the total number of elements within this matrix, including
     * zeroes.
     *
     * @return the total number of elements in this matrix.
     */
    default long size() {
        return length(ROW_DIM) * length(COL_DIM);
    }

    /**
     * Returns the total number of "non-default" elements of this matrix.
     * Typically, default value is an additive identity (i.e., "zero").
     *
     * @return
     */
    default long sparseSize() {
        return size();
    }

    /**
     * Simplify with respect to target, then evaluate into target.
     * @param target
     */
    default void setInto(Matrix<T> target) {
        A2Expr<T> source = simplify(target);
        source.evalInto(target);
    }

    public static final Simplifier simplifier = new Simplifier();

    default A2Expr<T> simplify(Matrix<T> target) {
        return simplify(target, simplifier);
    }

    A2Expr<T> simplify(Matrix<T> target, Simplifier simplifier);

    void evalInto(Matrix<T> target);

    /**
     * Defined for all A2Expr's, but only valid to call
 for Matrix, MatrixTemporary, Transpose, and similar trivially
 computable values.
     *
     * @return
     */
    Matrix<T> getValue();

    // Override in MatrixTemporary, Matrix, Transpose
    default A2Expr<T> tempifyAsNecessary(Simplifier simplifier, int desiredMajorOrder) {
        // Here we must hold our nose and do the best possible job of allocating
        // the temporary.  We desperately DO NOT want to box primitives or near
        // primitives.
        desiredMajorOrder = canonicalizeMajorDim(desiredMajorOrder);
        Matrix<T> temp = simplifier.makeMatrix(this, desiredMajorOrder);
        A2Expr<T> x = simplify(temp, simplifier);
        MatrixTemporary<T> t = new MatrixTemporary<>(temp, x);
        return t;
    }


}
