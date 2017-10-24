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
import javax.arrays.v2.ops.BinaryOp;

/**
 * A binary AST node with a elementwise scalar-matrix operator.
 *
 * @param <T> Element type of result matrix
 * @param <U> Type of scalar left (first) operand
 * @param <V> Element type of matrix-typed right (second) operand
 */
public class MatScalarLeft<T, U, V> extends MatrixElementwise<T, V> {
    public final U a;
    public final BinaryOp<T, U, V> op;

    public MatScalarLeft(BinaryOp<T, U, V> op, U a, A2Expr<V> x) {
        super(x);
        this.a = a;
        this.op = op;
    }

    @Override
    public BinaryOp<T, U, V> op() {
        return op;
    }

    @Override
    public String toString() {
        return "(" + op + " " + a + " " + x + ")";
    }

    @Override
    public A2Expr<T> simplify(Matrix<T> target, Simplifier simplifier) {
        return simplifier.simplifyScalarLeft(target, this);
    }

    @Override
    public void evalInto(Matrix<T> target) {
        long n_rows = target.length(ROW_DIM);
        long n_cols = target.length(COL_DIM);
        if (x.length(ROW_DIM) != n_rows || x.length(COL_DIM) != n_cols ) {
            throw new IllegalArgumentException("Matrices must have conforming sizes");
        }
        setRecursive(target, a, x.getValue(), A2Expr.elementWiseBlockSize());
    }

      /**
     * Performs this gets operand1 op operand2, perhaps with recursive
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
     * @param blocksize
     */
     void setRecursive(Matrix<T> target, U operand1, Matrix<V> operand2, int blocksize) {
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
                long m2 = operand2.middle(COL_DIM);
                m0 = A2Expr.firstNotEqual(m0, m1, m2);

                ScalarLeftAction<T,U,V> right = new ScalarLeftAction<>(
                        this, target.getColumns(m0, n_cols),
                        operand1,
                        operand2.getColumns(m0, n_cols), blocksize);
                ScalarLeftAction<T,U,V> left = new ScalarLeftAction<>(
                        this, target.getColumns(0, m0),
                        operand1,
                        operand2.getColumns(0, m0), blocksize);
                ForkJoinTask.invokeAll(left, right);
            }
        } else {
            // subdivide rows
            long m0 = n_rows >> 1;
            long m1 = target.middle(ROW_DIM);
            long m2 = operand2.middle(ROW_DIM);
            m0 = A2Expr.firstNotEqual(m0, m1, m2);

            ScalarLeftAction<T,U,V> right = new ScalarLeftAction<>(
                    this, target.getRows(m0, n_rows),
                    operand1,
                    operand2.getRows(m0, n_rows), blocksize);
            ScalarLeftAction<T,U,V> left = new ScalarLeftAction<>(
                    this, target.getRows(0, m0),
                    operand1,
                    operand2.getRows(0, m0), blocksize);
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
     * @param i_lo
     * @param i_hi
     * @param j_lo
     * @param j_hi
     */
     void setSubMatrix(Matrix<T> target, U operand1, Matrix<V> operand2,
            long i_lo, long i_hi,
            long j_lo, long j_hi) {
        long o2_i_off = 0;
        long o2_j_off = 0;
        if (target instanceof SubMatrix) {
            SubMatrix<T> sub_this = (SubMatrix<T>) target;
            long i_off = sub_this.beginRow();
            long j_off = sub_this.beginColumn();
            i_lo += i_off;
            j_lo += j_off;
            i_hi += i_off;
            j_hi += j_off;
            o2_i_off -= i_off;
            o2_j_off -= j_off;
            target = sub_this.parent();
        }
        if (operand2 instanceof SubMatrix) {
            SubMatrix<V> sub_2 = (SubMatrix<V>) operand2;
            o2_i_off += sub_2.beginRow();
            o2_j_off += sub_2.beginColumn();
            operand2 = sub_2.parent();
        }
        op.setOffsetSubMatrix(target, operand1, operand2,
                i_lo, i_hi, o2_i_off,
                j_lo, j_hi, o2_j_off);
    }


    static public class ScalarLeftAction<T,U,V> extends RecursiveAction {
        private static final long serialVersionUID = 0x5d61f22effdf89eL;

        protected final Matrix<T> target;
        protected final U operand1;
        protected final Matrix<V> operand2;
        protected final MatScalarLeft<T,U,V> node;
        protected final int blocksize;

        public ScalarLeftAction(MatScalarLeft<T,U,V> node, Matrix<T> target,
                U operand1, Matrix<V> operand2, int blocksize) {
            this.target = target;
            this.operand1 = operand1;
            this.operand2 = operand2;
            this.node = node;
            this.blocksize = blocksize;
        }

        @Override
        protected void compute() {
            node.setRecursive(target, operand1, operand2, blocksize);
        }
    }
}
