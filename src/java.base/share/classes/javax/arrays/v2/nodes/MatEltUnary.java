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
import static javax.arrays.v2.A2Expr.firstNotEqual;
import javax.arrays.v2.SubMatrix;
import javax.arrays.v2.ops.UnaryOp;

/**
 * The class of all elementwise unary AST nodes.
 *
 * @param <T> element type of result matrix
 * @param <U> element type of operand matrix
 */
public class MatEltUnary<T, U> extends MatrixElementwise<T, U> {
    final UnaryOp<T, U> op;

    public MatEltUnary(UnaryOp<T, U> op, A2Expr<U> x) {
        super(x, x.preferredMajorVote());
        this.op = op;
    }

    @Override
    public UnaryOp<T, U> op() {
        return op;
    }

    @Override
    public String toString() {
        return "(" + op + " " + x + ")";
    }

    @Override
    public A2Expr<T> simplify(Matrix<T> target, Simplifier simplifier) {
        return simplifier.simplifyEltWise1(target, this);
    }

    @Override
    public void evalInto(Matrix<T> target) {
        setRecursive(target, x.getValue(), A2Expr.elementWiseBlockSize());
    }

    /**
     * Assigns (op operand1) into target, perhaps with recursive subdivision,
     * perhaps with fork/join parallelism. Recursive subevaluations
     * are applied to results of "subArray" to create an opportunity for
     * specialization (simplification) of array substructure.
     *
     * It may be profitable to override this method to obtain customized
     * behavior for special (e.g., distributed) array representations.
     *
     * @param operand1
     * @param op
     * @param blocksize
     */
    void setRecursive(Matrix<T> target, Matrix<U> operand1, int blocksize) {
        long n_rows = target.length(ROW_DIM);
        long n_cols = target.length(COL_DIM);
        if (n_rows <= blocksize) {
            if (n_cols <= blocksize) {
                // kernel iteration.
                setSubMatrix(target, operand1, 0, n_rows, 0, n_cols);
            } else {
                // subdivide columns
                long m0 = n_cols >> 1;
                long m1 = target.middle(COL_DIM);
                long m2 = operand1.middle(COL_DIM);
                m0 = firstNotEqual(m0, m1, m2);

                EltWise1Action<T,U> right = new EltWise1Action<>(
                        target.getColumns(m0, n_cols),
                        operand1.getColumns(m0, n_cols), this,
                        blocksize);
                right.fork();
                EltWise1Action<T,U> left = new EltWise1Action<>(
                        target.getColumns(0, m0),
                        operand1.getColumns(0, m0), this,
                        blocksize);
                left.compute();
                right.join();
            }
        } else {
            long m0 = n_rows >> 1;
            long m1 = target.middle(ROW_DIM);
            long m2 = operand1.middle(ROW_DIM);
            m0 = firstNotEqual(m0, m1, m2);

            EltWise1Action<T,U> right = new EltWise1Action<>(
                    target.getRows(m0, n_rows),
                    operand1.getRows(m0, n_rows), this,
                    blocksize);
            right.fork();
            EltWise1Action<T,U> left = new EltWise1Action<>(
                    target.getRows(0, m0),
                    operand1.getRows(0, m0), this,
                    blocksize);
            left.compute();
            right.join();
        }
    }

    /**
     * Performs 'this gets op opr1' for equally-offset this, opr1
     * submatrices. Does this by removing intervening submatrices, deriving
     * additional offsets, and delegating to a not-equally-offset overloading of
     * getsLeaf.
     *
     * @param operand1
     * @param op
     * @param i_lo
     * @param i_hi
     * @param j_lo
     * @param j_hi
     */
    void setSubMatrix(Matrix<T> target, Matrix<U> operand1,
            long i_lo, long i_hi,
            long j_lo, long j_hi) {
        long o1_i_off = 0;
        long o1_j_off = 0;
        if (target instanceof SubMatrix) {
            SubMatrix<T> sub_this = (SubMatrix<T>) target;
            long i_off = sub_this.beginRow();
            long j_off = sub_this.beginColumn();
            i_lo += i_off;
            j_lo += j_off;
            i_hi += i_off;
            j_hi += j_off;
            o1_i_off -= i_off;
            o1_j_off -= j_off;
            target = sub_this.parent();
        }
        if (operand1 instanceof SubMatrix) {
            SubMatrix<U> sub_1 = (SubMatrix<U>) operand1;
            o1_i_off += sub_1.beginRow();
            o1_j_off += sub_1.beginColumn();
            operand1 = sub_1.parent();
        }
        op.setOffsetSubMatrix(target, operand1,
                i_lo, i_hi, o1_i_off,
                j_lo, j_hi, o1_j_off);
    }

    static public class EltWise1Action<T,U> extends RecursiveAction {
        private static final long serialVersionUID = 0x20743fd3e3b0f69L;

        protected final Matrix<T> target;
        protected final Matrix<U> operand1;
        protected final MatEltUnary<T,U> node;
        protected final int blocksize;

        public EltWise1Action(Matrix<T> target,
                Matrix<U> operand1, MatEltUnary<T,U> node, int blocksize) {
            this.target = target;
            this.operand1 = operand1;
            this.node = node;
            this.blocksize = blocksize;
        }

        @Override
        protected void compute() {
            node.setRecursive(target, operand1, blocksize);
        }
    }
}
