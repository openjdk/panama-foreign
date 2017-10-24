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

import javax.arrays.v2.A1Expr;
import javax.arrays.v2.Matrix;
import javax.arrays.v2.A2Expr;
import static javax.arrays.v2.A2Expr.COL_DIM;
import static javax.arrays.v2.A2Expr.NO_DIM;
import static javax.arrays.v2.A2Expr.ROW_DIM;
import javax.arrays.v2.Array;
import javax.arrays.v2.nodes.ArrayUnary;
import javax.arrays.v2.nodes.MatEltUnary;

/**
 * Elementwise matrix unary operators.
 *
 * @param <T> Result element type
 * @param <U> Operand 1 matrix element type
 */
public interface UnaryOp<T, U> extends AnyOp<T> {

    default A2Expr<T> apply(A2Expr<U> x) {
        return new MatEltUnary<>(this, x);
    }

    default A1Expr<T> apply(A1Expr<U> x) {
        return new ArrayUnary<>(this, x);
    }

    T apply(U x);

    /**
     * For tiles offset and sized as specified, performs target gets op operand1
     * in some probably-serial way.
     *
     * Suitable for specialization by subclasses.
     *
     * @param target
     * @param operand1
     * @param i_lo
     * @param i_hi
     * @param i_o1_off
     * @param j_lo
     * @param j_hi
     * @param j_o1_off
     */
    default void setOffsetSubMatrix(Matrix<T> target, Matrix<U> operand1,
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
                        target.set(i, j, apply(operand1.get(i, j)));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1.get(i + i_o1_off, j + j_o1_off)));
                    }
                }
            }
        } else { // Column Major
            if (i_o1_off == 0 && j_o1_off == 0) {
                // Common case -- everything is aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1.get(i, j)));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1.get(i + i_o1_off, j + j_o1_off)));
                    }
                }
            }
        }
    }

        /**
     * For tiles offset and sized as specified, performs target gets op operand1
     * in some probably-serial way.
     *
     * Suitable for specialization by subclasses.
     *
     * @param target
     * @param operand1
     * @param i_lo
     * @param i_hi
     * @param i_o1_off
     */
    default void setOffsetSubArray(Array<T> target, Array<U> operand1,
            long i_lo, long i_hi, long i_o1_off) {

        if (i_o1_off == 0) {
            // Common case -- everything is aligned.
            for (long i = i_lo; i < i_hi; i++) {
                target.set(i, apply(operand1.get(i)));
            }
        } else {
            // General case, not so aligned.
            for (long i = i_lo; i < i_hi; i++) {
                target.set(i, apply(operand1.get(i + i_o1_off)));
            }
        }

    }

}
