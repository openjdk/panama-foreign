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
import javax.arrays.v2.A1Expr;
import javax.arrays.v2.Matrix;
import javax.arrays.v2.A2Expr;
import static javax.arrays.v2.A2Expr.COL_DIM;
import static javax.arrays.v2.A2Expr.ROW_DIM;
import javax.arrays.v2.Array;
import javax.arrays.v2.SubArray;
import javax.arrays.v2.SubMatrix;
import javax.arrays.v2.ops.ProductOp;

/**
 * AST node for the product of a matrix with a vector (an array).
 *
 * @param <T> element type of the result matrix
 * @param <U> element type of the left (first) operand matrix
 * @param <V> element type of the right (second) operand matrix
 */
public class MatrixVectorProduct<T,U,V> implements A1Expr<T> {
    public final ProductOp<T,U,V> op;
    public final A2Expr<U> x;
    public final A1Expr<V> y;

    public MatrixVectorProduct(ProductOp<T,U,V> op, A2Expr<U> x, A1Expr<V> y) {
        this.x = x;
        this.op = op;
        this.y = y;
        if (x.length(COL_DIM) != y.length()) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public long length() {
        return y.length();
    }

    @Override
    public Class<T> elementType() {
        return op().resultType();
    }

    /**
     * GetValue is only intended for implementation by expressions with
     * O(1) computation cost; temporaries, transposes, and leaf values.
     *
     * @return
     */
    @Override
    public Array<T> getValue() {
        // Default behavior for all complex expressions except Temporary and Transpose
        throw new Error("Malformed tree, getValue applied to bare expression");
    }

    @Override
    public void setInto(Array<T> target) {
        if (x.length(ROW_DIM) != target.length()) {
            throw new IllegalArgumentException();
        }
        A1Expr<T> source = simplify(target);
        source.evalInto(target);
    }

    @Override
    public A1Expr<T> simplify(Array<T> target, Simplifier simplifier) {
        return simplifier.simplifyMatrixVectorProduct(target, this);
    }

    public ProductOp<T,U,V>  op() {
        return op;
    }

    @Override
    public void evalInto(Array<T> target) {
        // Recursive subdivision of x,y and target,
        // eventually performing target = op(x,y).
        long n_rows = target.length();

        long n_rows1 = x.length(ROW_DIM);
        long n_cols1 = x.length(COL_DIM);

        long n_rows2 = y.length();

        if (n_rows != n_rows1 || n_cols1 != n_rows2 ) {
            throw new IllegalArgumentException("Matrix and vector must have conforming sizes");
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
    public void setRecursive(Array<T> target, Matrix<U> operand1, Array<V> operand2, int blocksize) {
        long n_rows = target.length();
        if (n_rows <= blocksize) {
                // kernel iteration.
                setSubMatrix(target, operand1, operand2, 0, n_rows);
        } else {
            long m0 = n_rows >> 1;
            long m1 = target.middle();
            long m2 = operand1.middle(ROW_DIM);
            // long m3 = operand2.middle();
            m0 = A2Expr.firstNotEqual(m0, m1, m2);

            ElementWiseSetProductAction<T,U,V> right = new ElementWiseSetProductAction<>(
                    this, target.subArray(m0, n_rows),
                    operand1.getRows(m0, n_rows),
                    operand2, blocksize);
            right.fork();
            ElementWiseSetProductAction<T,U,V> left = new ElementWiseSetProductAction<>(
                    this, target.subArray(0, m0),
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
    public void setSubMatrix(Array<T> target, Matrix<U> operand1, Array<V> operand2,
            long i_lo, long i_hi) {
        long o1_i_off = 0;
        //long o2_i_off = 0;
        long o1_j_off = 0;
        long o2_off = 0;
        long K = operand1.length(COL_DIM);
        if (K != operand2.length()) {
            throw new IllegalArgumentException("Operand dimensions incompatible for matrix-vector product, "
                    + operand1.length(COL_DIM) + " and " + operand2.length());
        }

        if (target instanceof SubArray) {
            SubArray<T> sub_this = (SubArray<T>) target;
            long i_off = sub_this.begin();
            i_lo += i_off;
            i_hi += i_off;
            o1_i_off -= i_off;
            target = sub_this.parent();
        }
        if (operand1 instanceof SubMatrix) {
            SubMatrix<U> sub_1 = (SubMatrix<U>) operand1;
            o1_i_off += sub_1.beginRow();
            o1_j_off += sub_1.beginColumn();
            operand1 = sub_1.parent();
        }
        if (operand2 instanceof SubArray) {
            SubArray<V> sub_2 = (SubArray<V>) operand2;
            o2_off += sub_2.begin();
            operand2 = sub_2.parent();
        }
        op.setOffsetSubMatrix(target, operand1, operand2,
                i_lo, i_hi, o1_i_off, o1_j_off, o2_off, K);
    }



    static public class ElementWiseSetProductAction<T,U,V> extends RecursiveAction {
        private static final long serialVersionUID = 0x6b1614b0fe48522bL;

        protected final Array<T> target;
        protected final Matrix<U> operand1;
        protected final Array<V> operand2;
        protected final MatrixVectorProduct<T,U,V> node;
        protected final int blocksize;

        public ElementWiseSetProductAction(MatrixVectorProduct<T,U,V> node, Array<T> target, Matrix<U> operand1, Array<V> operand2, int blocksize) {
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
