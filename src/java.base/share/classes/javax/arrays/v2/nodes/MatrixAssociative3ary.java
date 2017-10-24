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

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import javax.arrays.v2.Matrix;
import javax.arrays.v2.A2Expr;
import static javax.arrays.v2.A2Expr.COL_DIM;
import static javax.arrays.v2.A2Expr.ROW_DIM;
import javax.arrays.v2.SubMatrix;
import javax.arrays.v2.ops.AssociativeOp;

/**
 * A trinary AST node with an associative operator.  These are used to reduce
 * the number of temporaries created in expression evaluation.
 *
 * @param <T> The element type of the result and three operands.
 */
public class MatrixAssociative3ary<T> extends MatrixAssociative<T> {
    public final A2Expr<T> y;
    public final A2Expr<T> z;

    public MatrixAssociative3ary(AssociativeOp<T> op, A2Expr<T> x, A2Expr<T> y, A2Expr<T> z) {
        super(op, x, x.preferredMajorVote() + y.preferredMajorVote() + z.preferredMajorVote());
        this.y = y;
        this.z = z;
        if (x.length(ROW_DIM) != y.length(ROW_DIM) || x.length(ROW_DIM) != z.length(ROW_DIM) || x.length(COL_DIM) != y.length(COL_DIM) || x.length(COL_DIM) != z.length(COL_DIM)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "(" + op + " " + x + " " + y + " " + z + ")";
    }

    @Override
    public A2Expr<T> simplify(Matrix<T> target, Simplifier simplifier) {
        return simplifier.simplifyEltWise3(target, this);
    }

    @Override
    public void evalInto(Matrix<T> target) {
        long n_rows = target.length(ROW_DIM);
        long n_cols = target.length(COL_DIM);
        if (x.length(ROW_DIM) != n_rows || y.length(ROW_DIM) != n_rows|| z.length(ROW_DIM) != n_rows
                || x.length(COL_DIM) != n_cols || y.length(COL_DIM) != n_cols || z.length(COL_DIM) != n_cols) {
            throw new IllegalArgumentException("Matrices must have conforming sizes");
        }
        setRecursive(target, x.getValue(), y.getValue(), z.getValue(), A2Expr.elementWiseBlockSize());
    }

    /**
     * Performs this gets operand1 op operand2 op operand3, perhaps with recursive
     * subdivision, perhaps with fork/join parallelism. Recursive subevaluations
     * are applied to results of "subArray" to create an opportunity for
     * specialization (simplification) of array substructure.
     *
     * It may be profitable to override this method to obtain customized
     * behavior for special (e.g., distributed) array representations.
     *
     * @param operand1
     * @param op
     * @param operand2
     * @param operand3
     * @param blocksize
     */
     void setRecursive(Matrix<T> target, Matrix<T> operand1, Matrix<T> operand2, Matrix<T> operand3, int blocksize) {
        long n_rows = target.length(ROW_DIM);
        long n_cols = target.length(COL_DIM);
        if (n_rows <= blocksize) {
            if (n_cols <= blocksize) {
                // kernel iteration.
                setSubMatrix(target, operand1, operand2, operand3, 0, n_rows, 0, n_cols);
            } else {
                // subdivide columns
                long m0 = n_cols >> 1;
                long m1 = target.middle(COL_DIM);
                long m2 = operand1.middle(COL_DIM);
                long m3 = operand2.middle(COL_DIM);
                long m4 = operand3.middle(COL_DIM);
                m0 = A2Expr.firstNotEqual(m0, m1, m2, m3, m4);

                EltWise3Action<T> right = new EltWise3Action<>(
                        this, target.getColumns(m0, n_cols),
                        operand1.getColumns(m0, n_cols),
                        operand2.getColumns(m0, n_cols),
                        operand3.getColumns(m0, n_cols), blocksize);
                EltWise3Action<T> left = new EltWise3Action<>(
                        this, target.getColumns(0, m0),
                        operand1.getColumns(0, m0),
                        operand2.getColumns(0, m0),
                        operand3.getColumns(0, m0), blocksize);
                ForkJoinTask.invokeAll(left, right);
            }
        } else {
            // subdivide rows
            long m0 = n_rows >> 1;
            long m1 = target.middle(ROW_DIM);
            long m2 = operand1.middle(ROW_DIM);
            long m3 = operand2.middle(ROW_DIM);
            long m4 = operand3.middle(ROW_DIM);
            m0 = A2Expr.firstNotEqual(m0, m1, m2, m3, m4);

            EltWise3Action<T> right = new EltWise3Action<>(
                    this, target.getRows(m0, n_rows),
                    operand1.getRows(m0, n_rows),
                    operand2.getRows(m0, n_rows),
                    operand3.getRows(m0, n_rows), blocksize);
            EltWise3Action<T> left = new EltWise3Action<>(
                    this, target.getRows(0, m0),
                    operand1.getRows(0, m0),
                    operand2.getRows(0, m0),
                    operand3.getRows(0, m0), blocksize);
            ForkJoinTask.invokeAll(left, right);
        }
    }

    /**
     * Performs 'this gets opr1 op opr2' for equally-offset this, opr1, opr2
     * submatrices. Does this by removing intervening submatrices, deriving
     * additional offsets, and delegating to a not-equally-offset overloading of
     * getsLeaf.
     *
     * @param operand1
     * @param op
     * @param operand2
     * @param operand3
     * @param i_lo
     * @param i_hi
     * @param j_lo
     * @param j_hi
     */
     void setSubMatrix(Matrix<T> target, Matrix<T> operand1, Matrix<T> operand2, Matrix<T> operand3,
            long i_lo, long i_hi,
            long j_lo, long j_hi) {
        long o1_i_off = 0;
        long o2_i_off = 0;
        long o3_i_off = 0;
        long o1_j_off = 0;
        long o2_j_off = 0;
        long o3_j_off = 0;
        if (target instanceof SubMatrix) {
            SubMatrix<T> sub_this = (SubMatrix<T>) target;
            long i_off = sub_this.beginRow();
            long j_off = sub_this.beginColumn();
            i_lo += i_off;
            j_lo += j_off;
            i_hi += i_off;
            j_hi += j_off;
            o1_i_off -= i_off;
            o2_i_off -= i_off;
            o3_i_off -= i_off;
            o1_j_off -= j_off;
            o2_j_off -= j_off;
            o3_j_off -= j_off;
            target = sub_this.parent();
        }
        if (operand1 instanceof SubMatrix) {
            SubMatrix<T> sub_1 = (SubMatrix<T>) operand1;
            o1_i_off += sub_1.beginRow();
            o1_j_off += sub_1.beginColumn();
            operand1 = sub_1.parent();
        }
        if (operand2 instanceof SubMatrix) {
            SubMatrix<T> sub_2 = (SubMatrix<T>) operand2;
            o2_i_off += sub_2.beginRow();
            o2_j_off += sub_2.beginColumn();
            operand2 = sub_2.parent();
        }
        if (operand3 instanceof SubMatrix) {
            SubMatrix<T> sub_3 = (SubMatrix<T>) operand3;
            o3_i_off += sub_3.beginRow();
            o3_j_off += sub_3.beginColumn();
            operand3 = sub_3.parent();
        }
        op.setOffsetSubMatrix(target, operand1, operand2, operand3,
                i_lo, i_hi, o1_i_off, o2_i_off, o3_i_off,
                j_lo, j_hi, o1_j_off, o2_j_off, o3_j_off);
    }


    static public class EltWise3Action<T> extends RecursiveAction {
        private static final long serialVersionUID = 0x3dbe29db8e627a58L;

        protected final Matrix<T> target;
        protected final Matrix<T> operand1;
        protected final Matrix<T> operand2;
        protected final Matrix<T> operand3;
        protected final MatrixAssociative3ary<T> node;
        protected final int blocksize;

        public EltWise3Action(MatrixAssociative3ary<T> node, Matrix<T> target,
                Matrix<T> operand1, Matrix<T> operand2, Matrix<T> operand3, int blocksize) {
            this.target = target;
            this.operand1 = operand1;
            this.operand2 = operand2;
            this.operand3 = operand3;
            this.node = node;
            this.blocksize = blocksize;
        }

        @Override
        protected void compute() {
            node.setRecursive(target, operand1, operand2, operand3, blocksize);
        }
    }
}
