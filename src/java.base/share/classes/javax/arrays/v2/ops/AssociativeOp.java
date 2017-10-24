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
import javax.arrays.v2.nodes.ArrayAssociativeBinary;
import javax.arrays.v2.nodes.MatrixAssociativeBinary;
import javax.arrays.v2.nodes.MatrixAssociative3ary;
import javax.arrays.v2.nodes.MatrixAssociative4ary;

/**
 * Elementwise associative binary (and hence trinary and ternary) operators
 *
 * @param <T>
 */
public interface AssociativeOp<T> extends BinaryOp<T, T, T> {

    @Override
    T apply(T x, T y);

    @Override
    default A2Expr<T> apply(A2Expr<T> x, A2Expr<T> y) {
        return new MatrixAssociativeBinary<>(this, x, y);
    }

    default A2Expr<T> apply(A2Expr<T> x, A2Expr<T> y, A2Expr<T> z) {
        return new MatrixAssociative3ary<>(this, x, y, z);
    }

    default A2Expr<T> apply(A2Expr<T> w, A2Expr<T> x, A2Expr<T> y, A2Expr<T> z) {
        return new MatrixAssociative4ary<>(this, w, x, y, z);
    }

    default A1Expr<T> apply(A1Expr<T> x, A1Expr<T> y) {
        return new ArrayAssociativeBinary<>(this, x, y);
    }

    default T apply(T x, T y, T z) {
        return apply(apply(x, y), z);
    }

    default T apply(T w, T x, T y, T z) {
        return apply(apply(apply(w, x), y), z);
    }

    default void setOffsetSubMatrix(Matrix<T> target,
            Matrix<T> operand1, Matrix<T> operand2, Matrix<T> operand3,
            long i_lo, long i_hi, long i_o1_off, long i_o2_off, long i_o3_off,
            long j_lo, long j_hi, long j_o1_off, long j_o2_off, long j_o3_off) {

        // Need to take desired iteration direction into account.
        int targetOrder = target.preferredMajorDim();
        int operandOrder
                = A2Expr.canonicalizeMajorDim(operand1.preferredMajorDim()
                        + operand2.preferredMajorDim() + operand3.preferredMajorDim());

        if (targetOrder == ROW_DIM
                || targetOrder == NO_DIM && operandOrder != COL_DIM) {
            // Row Major

            if (i_o1_off == 0 && j_o1_off == 0 && i_o2_off == 0 &&
                    j_o2_off == 0 && i_o3_off == 0 && j_o3_off == 0) {
                // Common case -- everything is aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1.get(i, j), operand2.get(i, j), operand3.get(i, j)));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1.get(i + i_o1_off, j + j_o1_off),
                                operand2.get(i + i_o2_off, j + j_o2_off),
                                operand3.get(i + i_o3_off, j + j_o3_off)));
                    }
                }
            }
        } else { // Column major
            if (i_o1_off == 0 && j_o1_off == 0 && i_o2_off == 0 &&
                    j_o2_off == 0 && i_o3_off == 0 && j_o3_off == 0) {
                // Common case -- everything is aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1.get(i, j), operand2.get(i, j), operand3.get(i, j)));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1.get(i + i_o1_off, j + j_o1_off),
                                operand2.get(i + i_o2_off, j + j_o2_off), operand3.get(i + i_o3_off, j + j_o3_off)));
                    }
                }
            }
        }
    }

    default void setOffsetSubMatrix(Matrix<T> target,
            Matrix<T> operand1, Matrix<T> operand2, Matrix<T> operand3, Matrix<T> operand4,
            long i_lo, long i_hi, long i_o1_off, long i_o2_off, long i_o3_off, long i_o4_off,
            long j_lo, long j_hi, long j_o1_off, long j_o2_off, long j_o3_off, long j_o4_off) {

        // Need to take desired iteration direction into account.
        int targetOrder = target.preferredMajorDim();
        int operandOrder
                = A2Expr.canonicalizeMajorDim(operand1.preferredMajorDim() +
                        operand2.preferredMajorDim() +
                        operand3.preferredMajorDim() +
                        operand4.preferredMajorDim());

        if (targetOrder == ROW_DIM
                || targetOrder == NO_DIM && operandOrder != COL_DIM) {
            // Row Major

            if (i_o1_off == 0 && j_o1_off == 0 && i_o2_off == 0 &&
                    j_o2_off == 0 && i_o3_off == 0 && j_o3_off == 0 &&
                    i_o4_off == 0 && j_o4_off == 0) {
                // Common case -- everything is aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1.get(i, j),
                                               operand2.get(i, j),
                                               operand3.get(i, j),
                                               operand4.get(i, j)));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long i = i_lo; i < i_hi; i++) {
                    for (long j = j_lo; j < j_hi; j++) {
                        target.set(i, j, apply(operand1.get(i + i_o1_off, j + j_o1_off),
                                operand2.get(i + i_o2_off, j + j_o2_off),
                                operand3.get(i + i_o3_off, j + j_o3_off),
                                operand4.get(i + i_o4_off, j + j_o4_off)));
                    }
                }
            }
        } else { // Column major
            if (i_o1_off == 0 && j_o1_off == 0 && i_o2_off == 0 &&
                    j_o2_off == 0 && i_o3_off == 0 && j_o3_off == 0 &&
                    i_o4_off == 0 && j_o4_off == 0) {
                // Common case -- everything is aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1.get(i, j),
                                               operand2.get(i, j),
                                               operand3.get(i, j),
                                               operand4.get(i, j)));
                    }
                }
            } else {
                // General case, not so aligned.
                for (long j = j_lo; j < j_hi; j++) {
                    for (long i = i_lo; i < i_hi; i++) {
                        target.set(i, j, apply(operand1.get(i + i_o1_off, j + j_o1_off),
                                operand2.get(i + i_o2_off, j + j_o2_off),
                                operand3.get(i + i_o3_off, j + j_o3_off),
                                operand4.get(i + i_o4_off, j + j_o4_off)));
                    }
                }
            }
        }
    }
}
