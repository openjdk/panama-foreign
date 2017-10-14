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
package javax.arrays.v2.nodes;

import java.util.concurrent.RecursiveAction;
import javax.arrays.v2.Matrix;
import javax.arrays.v2.A2Expr;
import javax.arrays.v2.SubMatrix;
import javax.arrays.v2.ops.ProductOp;

/**
 *
 * An AST node for the multiplication of two matrices.
 * Matrix multiplication is based on "sum" and "product",
 * where product maps (U,V) onto T and sum maps (T,T) onto T.
 *
 * @param <T> Element type of result matrix
 * @param <U> Element type of left (first) operand matrix
 * @param <V> Element type of right (second) operand matrix
 */
public class MatrixProduct<T,U,V> extends MatrixWithOp<T,U> {
    public final ProductOp<T,U,V> op;
    public final A2Expr<V> y;
    final int preferredMajor;

    public MatrixProduct(ProductOp<T,U,V> op, A2Expr<U> x, A2Expr<V> y) {
        super(x);
        this.op = op;
        this.y = y;
        if (x.length(COL_DIM) != y.length(ROW_DIM)) {
            throw new IllegalArgumentException();
        }
        preferredMajor = x.preferredMajorVote() - y.preferredMajorVote();
    }

    @Override
    public int preferredMajorVote() {
        return preferredMajor;
    }

    @Override
    public long length(int d) {
        if (d == COL_DIM) {
            return y.length(COL_DIM);
        } else {
            return x.length(d);
        }
    }

    @Override
    public void setInto(Matrix<T> target) {
        if (x.length(ROW_DIM) != target.length(ROW_DIM) || target.length(COL_DIM) != y.length(COL_DIM)) {
            throw new IllegalArgumentException();
        }
        A2Expr<T> source = simplify(target);
        source.evalInto(target);
    }

    @Override
    public A2Expr<T> simplify(Matrix<T> target, Simplifier simplifier) {
        return simplifier.simplifyRowColNode(target, this);
    }

    @Override
    public ProductOp<T,U,V>  op() {
        return op;
    }

    @Override
    public void evalInto(Matrix<T> target) {
        // Recursive subdivision of x,y and target,
        // eventually performing target = op(x,y).
        long n_rows = target.length(ROW_DIM);
        long n_cols = target.length(COL_DIM);

        long n_rows1 = x.length(ROW_DIM);
        long n_cols1 = x.length(COL_DIM);

        long n_rows2 = y.length(ROW_DIM);
        long n_cols2 = y.length(COL_DIM);

        if (n_rows != n_rows1 || n_cols1 != n_rows2 || n_cols2 != n_cols) {
            throw new IllegalArgumentException("Matrices must have conforming sizes");
        }
        setRecursive(target, x.getValue(), y.getValue(), A2Expr.productBlockSize());
    }


    /**
     * Performs this gets operand1 X operand2 with the row-column inner product
     * defined by times and plus, perhaps with recursive subdivision, perhaps
     * with fork/join parallelism. Recursive subevaluations are applied to
     * results of "subArray" to create an opportunity for specialization
     * (simplification) of array substructure.
     *
     * It may be profitable to override this method to obtain customized
     * behavior for special (e.g., distributed) array representations.
     *
     * @param target
     * @param operand1
     * @param operand2
     * @param blocksize
     */
    public void setRecursive(Matrix<T> target, Matrix<U> operand1, Matrix<V> operand2, int blocksize) {
        long n_rows = target.length(ROW_DIM);
        long n_cols = target.length(COL_DIM);
        if (n_rows <= blocksize) {
            if (n_cols <= blocksize) {
                // kernel iteration.
                setSubMatrix(target, operand1, operand2, 0, n_rows, 0, n_cols);
            } else {
                // subdivide columns
                long m0 = n_cols >> 1;
                long m1 = target.middle(COL_DIM);
                long m2 = operand1.middle(COL_DIM);
                long m3 = operand2.middle(COL_DIM);
                m0 = A2Expr.firstNotEqual(m0, m1, m3);

                ElementWiseSetProductAction<T,U,V> right = new ElementWiseSetProductAction<>(
                        this, target.getColumns(m0, n_cols), operand1,
                        operand2.getColumns(m0, n_cols), blocksize);
                right.fork();
                ElementWiseSetProductAction<T,U,V> left = new ElementWiseSetProductAction<>(
                        this, target.getColumns(0, m0), operand1,
                        operand2.getColumns(0, m0), blocksize);
                left.compute();
                right.join();
            }
        } else {
            long m0 = n_rows >> 1;
            long m1 = target.middle(ROW_DIM);
            long m2 = operand1.middle(ROW_DIM);
            long m3 = operand2.middle(ROW_DIM);
            m0 = A2Expr.firstNotEqual(m0, m1, m2);

            ElementWiseSetProductAction<T,U,V> right = new ElementWiseSetProductAction<>(
                    this, target.getRows(m0, n_rows),
                    operand1.getRows(m0, n_rows),
                    operand2, blocksize);
            right.fork();
            ElementWiseSetProductAction<T,U,V> left = new ElementWiseSetProductAction<>(
                    this, target.getRows(0, m0),
                    operand1.getRows(0, m0),
                    operand2, blocksize);
            left.compute();
            right.join();
        }
    }

    /**
     * Performs 'this gets opr1 X opr2' for equally-offset this, opr1, opr2
     * submatrices, multiplication defined by element operations times and plus.
     *
     * Does this by removing intervening submatrices, deriving additional
     * offsets, and delegating to a not-equally-offset overloading of getsLeaf.
     *
     * @param target
     * @param operand1
     * @param operand2
     * @param i_lo
     * @param i_hi
     * @param j_lo
     * @param j_hi
     */
    public void setSubMatrix(Matrix<T> target, Matrix<U> operand1, Matrix<V> operand2,
            long i_lo, long i_hi, long j_lo, long j_hi) {
        long o1_i_off = 0;
        long o2_i_off = 0;
        long o1_j_off = 0;
        long o2_j_off = 0;
        long o2_i_o1_j_count = operand1.length(COL_DIM);
        if (o2_i_o1_j_count != operand2.length(ROW_DIM))
            throw new IllegalArgumentException("Operand dimensions incompatible for matrix product, " +
                    operand1.length(COL_DIM) + " and " + operand2.length(ROW_DIM));
        if (target instanceof SubMatrix) {
            SubMatrix<T> sub_this = (SubMatrix<T>) target;
            long i_off = sub_this.beginRow();
            long j_off = sub_this.beginColumn();
            i_lo += i_off;
            i_hi += i_off;
            j_lo += j_off;
            j_hi += j_off;
            o1_i_off -= i_off;
            o2_j_off -= j_off;
            target = sub_this.parent();
        }
        if (operand1 instanceof SubMatrix) {
            SubMatrix<U> sub_1 = (SubMatrix<U>) operand1;
            o1_i_off += sub_1.beginRow();
            o1_j_off += sub_1.beginColumn();
            operand1 = sub_1.parent();
        }
        if (operand2 instanceof SubMatrix) {
            SubMatrix<V> sub_2 = (SubMatrix<V>) operand2;
            o2_i_off += sub_2.beginRow();
            o2_j_off += sub_2.beginColumn();
            operand2 = sub_2.parent();
        }
        op.setOffsetSubMatrix(target, operand1, operand2,
                i_lo, i_hi, o1_i_off, o1_j_off,
                j_lo, j_hi, o2_i_off, o2_j_off, o2_i_o1_j_count);
    }



    static public class ElementWiseSetProductAction<T,U,V> extends RecursiveAction {
        private static final long serialVersionUID = 0x768327c8ce93e4eeL;

        protected final Matrix<T> target;
        protected final Matrix<U> operand1;
        protected final Matrix<V> operand2;
        protected final MatrixProduct<T,U,V> node;
        protected final int blocksize;

        public ElementWiseSetProductAction(MatrixProduct<T,U,V> node, Matrix<T> target, Matrix<U> operand1, Matrix<V> operand2, int blocksize) {
            this.target = target;
            this.operand1 = operand1;
            this.operand2 = operand2;
            this.blocksize = blocksize;
            this.node = node;
        }

        @Override
        protected void compute() {
            node.setRecursive(target, operand1, operand2, blocksize);
        }
    }
}
