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
package javax.arrays.v2.ops;

import javax.arrays.v2.A0Expr;
import javax.arrays.v2.A1Expr;
import javax.arrays.v2.Matrix;
import javax.arrays.v2.A2Expr;
import static javax.arrays.v2.A2Expr.COL_DIM;
import static javax.arrays.v2.A2Expr.NO_DIM;
import static javax.arrays.v2.A2Expr.ROW_DIM;
import javax.arrays.v2.Array;
import javax.arrays.v2.Geometry;
import javax.arrays.v2.Scalar;
import javax.arrays.v2.nodes.ArrayBinary;
import javax.arrays.v2.nodes.ArrScalarLeft;
import javax.arrays.v2.nodes.MatEltBinary;
import javax.arrays.v2.nodes.MatScalarLeft;
import javax.arrays.v2.nodes.MatScalarRight;

/**
 * Elementwise  binary operators, not associative.
 *
 * @param <T> Result type of this operator
 * @param <U> Operand1 type of this operator
 * @param <V> Operand2 type of this operator.
 */
public interface BinaryOp<T, U, V> extends AnyOp<T> {

    T apply(U x, V y);

    /**
     * Combine matrices x and y with this operator to produce
     * a non-associative binary node.
     *
     * @param x
     * @param y
     * @return
     */
    default A2Expr<T> apply(A2Expr<U> x, A2Expr<V> y) {
        return new MatEltBinary<>(this, x, y);
    }

    /**
     * Combine matrices x and y with this operator to produce
     * a scalar-right node.
     *
     * @param x
     * @param y
     * @return
     */
    default A2Expr<T> apply(A2Expr<U> x, V y) {
        return new MatScalarRight<>(this, x, y);
    }

    /**
     * Combine matrices x and y with this operator to produce
     * a scalar-left node.
     *
     * @param x
     * @param y
     * @return
     */
    default A2Expr<T> apply(U x, A2Expr<V> y) {
        return new MatScalarLeft<>(this, x, y);
    }

    default A1Expr<T> apply(U x, A1Expr<V> y) {
        return apply(new Scalar<>(x), y);
    }

    default A1Expr<T> apply(A0Expr<U> x, A1Expr<V> y) {
        return new ArrScalarLeft<>(this, x, y);
    }

    default A1Expr<T> apply(A1Expr<U> x, A1Expr<V> y) {
        return new ArrayBinary<>(this, x, y);
    }


    // Is this the right place for this?  I think we
    // need to generalize "major order", perhaps to include
    // things like "distributed", "sparse", "small" -- this
    // is an abstraction of all the stuff we want to know
    // in order to make an evaluation run well.
    // It might make sense to think of this as being like a dynamic
    // type, except that getting it wrong should only make things go
    // slowly, not wrong.  TODO not used yet.
    default Geometry apply(Geometry x, Geometry y) {
        return new Geometry(x.desiredMajorOrder + y.desiredMajorOrder);
    }

    /**
     * For tiles offset and sized as specified, performs this gets operand1 op
     * operand2 in some probably-serial way.
     *
     * Suitable for specialization by subclasses.
     *
     * @param target
     * @param operand1
     * @param operand2
     * @param i_lo
     * @param i_hi
     * @param i_o1_off
     * @param i_o2_off
     * @param j_lo
     * @param j_hi
     * @param j_o1_off
     * @param j_o2_off
     */
    default void setOffsetSubMatrix(Matrix<T> target, Matrix<U> operand1, Matrix<V> operand2,
            long i_lo, long i_hi, long i_o1_off, long i_o2_off,
            long j_lo, long j_hi, long j_o1_off, long j_o2_off) {

        // Need to take desired iteration direction into account.
        int targetOrder = target.preferredMajorDim();
        int operandOrder = A2Expr.canonicalizeMajorDim(operand1.preferredMajorDim() + operand2.preferredMajorDim());

        if (targetOrder == ROW_DIM
                || targetOrder == NO_DIM && operandOrder != COL_DIM) {
            // Row Major
            if (i_o1_off == 0 && j_o1_off == 0 && i_o2_off == 0 && j_o2_off == 0) {
                // Common case -- everything is aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1.get(i, j), operand2.get(i, j)));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1.get(i + i_o1_off, j + j_o1_off), operand2.get(i + i_o2_off, j + j_o2_off)));
                    }
                }
            }
        } else { // Column major
            if (i_o1_off == 0 && j_o1_off == 0 && i_o2_off == 0 && j_o2_off == 0) {
                // Common case -- everything is aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1.get(i, j), operand2.get(i, j)));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1.get(i + i_o1_off, j + j_o1_off), operand2.get(i + i_o2_off, j + j_o2_off)));
                    }
                }
            }
        }
    }

    /**
     *
     * Scalar right fork/join leaf.
     *
     * @param target
     * @param operand1 matrix left operand
     * @param operand2 scalar right operand
     * @param i_lo
     * @param i_hi
     * @param i_o1_off
     * @param j_lo
     * @param j_hi
     * @param j_o1_off
     */
    default void setOffsetSubMatrix(Matrix<T> target, Matrix<U> operand1, V operand2,
            long i_lo, long i_hi, long i_o1_off,
            long j_lo, long j_hi, long j_o1_off) {

        // Need to take desired iteration direction into account.
        int targetOrder = target.preferredMajorDim();
        int operandOrder = operand1.preferredMajorDim();

        if (targetOrder == ROW_DIM
                || targetOrder == NO_DIM && operandOrder != COL_DIM) {
            // Row Major

            if (i_o1_off == 0 && j_o1_off == 0) {
                // Common case -- everything is aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1.get(i, j), operand2));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1.get(i + i_o1_off, j + j_o1_off), operand2));
                    }
                }
            }
        } else { // Column major
            if (i_o1_off == 0 && j_o1_off == 0) {
                // Common case -- everything is aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1.get(i, j), operand2));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1.get(i + i_o1_off, j + j_o1_off), operand2));
                    }
                }
            }
        }
    }

    /**
     *
     * Scalar left fork/join leaf.
     *
     * @param target
     * @param operand1 scalar left operand
     * @param operand2 matrix right operand
     * @param i_lo
     * @param i_hi
     * @param i_o2_off
     * @param j_lo
     * @param j_hi
     * @param j_o2_off
     */
    default void setOffsetSubMatrix(Matrix<T> target, U operand1, Matrix<V> operand2,
            long i_lo, long i_hi, long i_o2_off,
            long j_lo, long j_hi, long j_o2_off) {

        // Need to take desired iteration direction into account.
        int targetOrder = target.preferredMajorDim();
        int operandOrder = operand2.preferredMajorDim();

        if (targetOrder == ROW_DIM
                || targetOrder == NO_DIM && operandOrder != COL_DIM) {
            // Row Major
            if (i_o2_off == 0 && j_o2_off == 0) {
                // Common case -- everything is aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1, operand2.get(i, j)));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1, operand2.get(i + i_o2_off, j + j_o2_off)));
                    }
                }
            }
        } else { // Column major
            if (i_o2_off == 0 && j_o2_off == 0) {
                // Common case -- everything is aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1, operand2.get(i, j)));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1, operand2.get(i + i_o2_off, j + j_o2_off)));
                    }
                }
            }
        }
    }



   /**
     * For tiles offset and sized as specified, performs this gets operand1 op
     * operand2 in some probably-serial way.
     *
     * Suitable for specialization by subclasses.
     *
     * @param target
     * @param operand1
     * @param operand2
     * @param i_lo
     * @param i_hi
     * @param i_o1_off
     * @param i_o2_off
     */
    default void setOffsetSubArray(Array<T> target, Array<U> operand1, Array<V> operand2,
            long i_lo, long i_hi, long i_o1_off, long i_o2_off) {
            // Row Major
            if (i_o1_off == 0 && i_o2_off == 0) {
                // Common case -- everything is aligned.
                for (long i = i_lo; i < i_hi; i++) {
                        target.set(i,apply(operand1.get(i), operand2.get(i)));
                }
            } else {
                // General case, not so aligned.
                for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, apply(operand1.get(i + i_o1_off), operand2.get(i + i_o2_off)));
                }
            }
    }

   /**
     * Right scalar.
     * For tiles offset and sized as specified, performs this gets operand1 op
     * operand2 in some probably-serial way.
     *
     * Suitable for specialization by subclasses.
     *
     * @param target
     * @param operand1
     * @param operand2
     * @param i_lo
     * @param i_hi
     * @param i_o1_off
     */
    default void setOffsetSubArray(Array<T> target, Array<U> operand1, V operand2,
            long i_lo, long i_hi, long i_o1_off) {
            // Row Major
            if (i_o1_off == 0) {
                // Common case -- everything is aligned.
                for (long i = i_lo; i < i_hi; i++) {
                        target.set(i,apply(operand1.get(i), operand2));
                }
            } else {
                // General case, not so aligned.
                for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, apply(operand1.get(i + i_o1_off), operand2));
                }
            }
    }

   /**
     * Left scalar.
     * For tiles offset and sized as specified, performs this gets operand1 op
     * operand2 in some probably-serial way.
     *
     * Suitable for specialization by subclasses.
     *
     * @param target
     * @param operand1
     * @param operand2
     * @param i_lo
     * @param i_hi
     * @param i_o2_off
     */
    default void setOffsetSubArray(Array<T> target, U operand1, Array<V> operand2,
            long i_lo, long i_hi, long i_o2_off) {
            // Row Major
            if (i_o2_off == 0) {
                // Common case -- everything is aligned.
                for (long i = i_lo; i < i_hi; i++) {
                        target.set(i,apply(operand1, operand2.get(i)));
                }
            } else {
                // General case, not so aligned.
                for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, apply(operand1, operand2.get(i + i_o2_off)));
                }
            }
    }

}
