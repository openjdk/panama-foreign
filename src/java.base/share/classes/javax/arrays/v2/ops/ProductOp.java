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
import javax.arrays.v2.Array;
import javax.arrays.v2.nodes.ArrayVectorProduct;
import javax.arrays.v2.nodes.MatrixProduct;
import javax.arrays.v2.nodes.MatrixVectorProduct;

public interface ProductOp<T,U,V> extends AnyOp<T> {

    public abstract T times(U x, V y);
    public abstract T plus(T x, T y);

    default A2Expr<T> apply(A2Expr<U> x, A2Expr<V> y) {
        return new MatrixProduct<>(this, x, y);
    }

    default A1Expr<T> apply(A2Expr<U> x, A1Expr<V> y) {
        return new MatrixVectorProduct<>(this, x, y);
    }

    default A0Expr<T> apply(A1Expr<U> x, A1Expr<V> y) {
        return new ArrayVectorProduct<>(this, x, y);
    }

    // TODO need to add hook for a "kernel".

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
     * @param j_o1_off
     * @param j_lo
     * @param j_hi
     * @param i_o2_off
     * @param j_o2_off
     * @param K
     */
    default void setOffsetSubMatrix(Matrix<T> target, Matrix<U> operand1, Matrix<V> operand2,
            long i_lo, long i_hi, long i_o1_off, long j_o1_off,
            long j_lo, long j_hi, long i_o2_off, long j_o2_off, long K) {

        // General case, not so aligned.
        long i;
        for (i = i_lo; i + 2 < i_hi; i += 3) {
            long j;
            long ii = i + i_o1_off;
            for (j = j_lo; j + 2 < j_hi; j += 3) {
                long jj = j + j_o2_off;
                U o1_0 = operand1.get(ii, j_o1_off);
                V o2_0 = operand2.get(i_o2_off, jj);
                U o1_1 = operand1.get(ii + 1, j_o1_off);
                V o2_1 = operand2.get(i_o2_off, jj + 1);
                U o1_2 = operand1.get(ii + 2, j_o1_off);
                V o2_2 = operand2.get(i_o2_off, jj + 2);
                T a00 = times(o1_0, o2_0);
                T a01 = times(o1_0, o2_1);
                T a02 = times(o1_0, o2_2);
                T a10 = times(o1_1, o2_0);
                T a11 = times(o1_1, o2_1);
                T a12 = times(o1_1, o2_2);
                T a20 = times(o1_2, o2_0);
                T a21 = times(o1_2, o2_1);
                T a22 = times(o1_2, o2_2);
                for (long k = 1; k < K; k++) {
                    long k1 = k + j_o1_off;
                    long k2 = k + i_o2_off;
                    o1_0 = operand1.get(ii, k1);
                    o2_0 = operand2.get(k2, jj);
                    o1_1 = operand1.get(ii + 1, k1);
                    o2_1 = operand2.get(k2, jj + 1);
                    o1_2 = operand1.get(ii + 2, k1);
                    o2_2 = operand2.get(k2, jj + 2);
                    a00 = plus(a00, times(o1_0, o2_0));
                    a01 = plus(a01, times(o1_0, o2_1));
                    a02 = plus(a02, times(o1_0, o2_2));
                    a10 = plus(a10, times(o1_1, o2_0));
                    a11 = plus(a11, times(o1_1, o2_1));
                    a12 = plus(a12, times(o1_1, o2_2));
                    a20 = plus(a20, times(o1_2, o2_0));
                    a21 = plus(a21, times(o1_2, o2_1));
                    a22 = plus(a22, times(o1_2, o2_2));
                }
                target.set(i, j, a00);
                target.set(i, j + 1, a01);
                target.set(i, j + 2, a02);
                target.set(i + 1, j, a10);
                target.set(i + 1, j + 1, a11);
                target.set(i + 1, j + 2, a12);
                target.set(i + 2, j, a20);
                target.set(i + 2, j + 1, a21);
                target.set(i + 2, j + 2, a22);
            }
            for (;j < j_hi;j++) {
                long jj = j + j_o2_off;
                U o1_0 = operand1.get(ii, j_o1_off);
                V o2_0 = operand2.get(i_o2_off, jj);
                U o1_1 = operand1.get(ii + 1, j_o1_off);
                U o1_2 = operand1.get(ii + 2, j_o1_off);
                T a00 = times(o1_0, o2_0);
                T a10 = times(o1_1, o2_0);
                T a20 = times(o1_2, o2_0);
                for (long k = 1; k < K; k++) {
                    long k1 = k + j_o1_off;
                    long k2 = k + i_o2_off;
                    o1_0 = operand1.get(ii, k1);
                    o2_0 = operand2.get(k2, jj);
                    o1_1 = operand1.get(ii + 1, k1);
                    o1_2 = operand1.get(ii + 2, k1);
                    a00 = plus(a00, times(o1_0, o2_0));
                    a10 = plus(a10, times(o1_1, o2_0));
                    a20 = plus(a20, times(o1_2, o2_0));
                }
                target.set(i, j, a00);
                target.set(i + 1, j, a10);
                target.set(i + 2, j, a20);
            }
        }
        for (;i < i_hi;i++) {
            long ii = i + i_o1_off;
            for (long j = j_lo; j < j_hi; j++) {
                long jj = j + j_o2_off;
                U o1_0 = operand1.get(ii, j_o1_off);
                V o2_0 = operand2.get(i_o2_off, jj);
                T a00 = times(o1_0, o2_0);
                for (long k = 1; k < K; k++) {
                    long k1 = k + j_o1_off;
                    long k2 = k + i_o2_off;
                    o1_0 = operand1.get(ii, k1);
                    o2_0 = operand2.get(k2, jj);
                    a00 = plus(a00, times(o1_0, o2_0));
                }
                target.set(i, j, a00);
            }
        }
    }

    /**
     *
     * @param target
     * @param operand1
     * @param operand2
     * @param i_lo
     * @param i_hi
     * @param i_o1_off
     * @param j_o1_off
     * @param o2_off
     * @param K
     */
    default void setOffsetSubMatrix(Array<T> target, Matrix<U> operand1, Array<V> operand2,
            long i_lo, long i_hi, long i_o1_off, long j_o1_off, long o2_off, long K) {

        // General case, not so aligned.
        long i;
        for (i = i_lo; i + 3 < i_hi; i += 4) {
            long ii = i + i_o1_off;
            U o1_0 = operand1.get(ii, j_o1_off);
            V o2_0 = operand2.get(o2_off);
            U o1_1 = operand1.get(ii + 1, j_o1_off);
            U o1_2 = operand1.get(ii + 2, j_o1_off);
            U o1_3 = operand1.get(ii + 3, j_o1_off);
            T a00 = times(o1_0, o2_0);
            T a10 = times(o1_1, o2_0);
            T a20 = times(o1_2, o2_0);
            T a30 = times(o1_3, o2_0);
            for (long k = 1; k < K; k++) {
                long k1 = k + j_o1_off;
                long k2 = k + o2_off;
                o1_0 = operand1.get(ii, k1);
                o2_0 = operand2.get(k2);
                o1_1 = operand1.get(ii + 1, k1);
                o1_2 = operand1.get(ii + 2, k1);
                o1_3 = operand1.get(ii + 3, k1);
                a00 = plus(a00, times(o1_0, o2_0));
                a10 = plus(a10, times(o1_1, o2_0));
                a20 = plus(a20, times(o1_2, o2_0));
                a30 = plus(a30, times(o1_3, o2_0));
            }
            target.set(i, a00);
            target.set(i + 1, a10);
            target.set(i + 2, a20);
            target.set(i + 3, a30);
        }
        for (; i < i_hi; i++) {
            long ii = i + i_o1_off;
            U o1_0 = operand1.get(ii, j_o1_off);
            V o2_0 = operand2.get(o2_off);
            T a00 = times(o1_0, o2_0);
            for (long k = 1; k < K; k++) {
                long k1 = k + j_o1_off;
                long k2 = k + o2_off;
                o1_0 = operand1.get(ii, k1);
                o2_0 = operand2.get(k2);
                a00 = plus(a00, times(o1_0, o2_0));
            }
            target.set(i, a00);
        }
    }

    default T setOffsetSubArray(Array<U> operand1, Array<V> operand2,
            long i_lo, long i_hi, long o2_off) {

            U o1 = operand1.get(i_lo);
            V o2 = operand2.get(i_lo + o2_off);
            T a = times(o1, o2);
            for (long i = i_lo+1; i < i_hi; i++) {
                o1 = operand1.get(i);
                o2 = operand2.get(i + o2_off);
                a = plus(a, times(o1, o2));
            }
            return a;

    }
}
