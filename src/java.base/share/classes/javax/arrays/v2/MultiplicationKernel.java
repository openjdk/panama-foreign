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

package javax.arrays.v2;

import javax.arrays.tbd.DComplex;
import static javax.arrays.v2.Matrix.COL_DIM;
import java.util.function.BinaryOperator;

/**
 * A multiplication kernel is an optional implementation of the leaf case
 * of a subdivided matrix multiplication operation.
 *
 * The apply method takes 9 parameters identifying the target and two operands,
 * the submatrix of the target (corners (i_lo, j_lo) and (i_hi, j_hi)) and the
 * offset of i within the first operand and the offset of j within the second
 * operand.
 *
 * This particular set of kernels is suited for arrays that are implemented
 * within a single address space; the calculation sweeps across all the columns
 * of operand1 and all the rows of operand2, so for very large matrices locality
 * improvements are certainly possible.
 *
 * @param <T>
 */

abstract public class MultiplicationKernel<T> {

    abstract public boolean isApplicable(Matrix<T> target, Matrix<T> operand1, Matrix<T> operand2);

    abstract public void apply(Matrix<T> target, Matrix<T> operand1, Matrix<T> operand2,
            long i_lo, long i_hi, long i_o1_off,
            long j_lo, long j_hi, long j_o2_off);

    /**
     * A non-specific kernel.
     * This might serve as a template for bytecode specialization.
     * It may be the case that simply copying these bytecodes into a new
     * class (that would generate a new method) would allow specialized
     * dispatch in the kernel.
     *
     * The inner loop is 3x3 blocked for operand reuse.
     *
     * @param <T>
     */
    public final static class Any<T> extends MultiplicationKernel<T> {

        private final BinaryOperator<T> times;
        private final BinaryOperator<T> plus;

        public Any(BinaryOperator<T> times,
                BinaryOperator<T> plus) {
            this.times = times;
            this.plus = plus;
        }

        @Override
        public boolean isApplicable(Matrix<T> target, Matrix<T> operand1, Matrix<T> operand2) {
            return true;
        }

        @Override
        public void apply(Matrix<T> target, Matrix<T> operand1, Matrix<T> operand2, long i_lo, long i_hi, long i_o1_off, long j_lo, long j_hi, long j_o2_off) {
            long K = operand1.length(COL_DIM);

            // General case, not so aligned.
            long i;
            for (i = i_lo; i + 2 < i_hi; i += 3) {
                long j;
                long ii = i + i_o1_off;
                for (j = j_lo; j + 2 < j_hi; j += 3) {
                    long jj = j + j_o2_off;
                    T o1_0 = operand1.get(ii, 0);
                    T o2_0 = operand2.get(0, jj);
                    T o1_1 = operand1.get(ii + 1, 0);
                    T o2_1 = operand2.get(0, jj + 1);
                    T o1_2 = operand1.get(ii + 2, 0);
                    T o2_2 = operand2.get(0, jj + 2);
                    T a00 = times.apply(o1_0, o2_0);
                    T a01 = times.apply(o1_0, o2_1);
                    T a02 = times.apply(o1_0, o2_2);
                    T a10 = times.apply(o1_1, o2_0);
                    T a11 = times.apply(o1_1, o2_1);
                    T a12 = times.apply(o1_1, o2_2);
                    T a20 = times.apply(o1_2, o2_0);
                    T a21 = times.apply(o1_2, o2_1);
                    T a22 = times.apply(o1_2, o2_2);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        o2_1 = operand2.get(k, jj + 1);
                        o1_2 = operand1.get(ii + 2, k);
                        o2_2 = operand2.get(k, jj + 2);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                        a01 = plus.apply(a01, times.apply(o1_0, o2_1));
                        a02 = plus.apply(a02, times.apply(o1_0, o2_2));
                        a10 = plus.apply(a10, times.apply(o1_1, o2_0));
                        a11 = plus.apply(a11, times.apply(o1_1, o2_1));
                        a12 = plus.apply(a12, times.apply(o1_1, o2_2));
                        a20 = plus.apply(a20, times.apply(o1_2, o2_0));
                        a21 = plus.apply(a21, times.apply(o1_2, o2_1));
                        a22 = plus.apply(a22, times.apply(o1_2, o2_2));
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
                for (; j < j_hi; j++) {
                    long jj = j + j_o2_off;
                    T o1_0 = operand1.get(ii, 0);
                    T o2_0 = operand2.get(0, jj);
                    T o1_1 = operand1.get(ii + 1, 0);
                    T o1_2 = operand1.get(ii + 2, 0);
                    T a00 = times.apply(o1_0, o2_0);
                    T a10 = times.apply(o1_1, o2_0);
                    T a20 = times.apply(o1_2, o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        o1_2 = operand1.get(ii + 2, k);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                        a10 = plus.apply(a10, times.apply(o1_1, o2_0));
                        a20 = plus.apply(a20, times.apply(o1_2, o2_0));
                    }
                    target.set(i, j, a00);
                    target.set(i + 1, j, a10);
                    target.set(i + 2, j, a20);
                }

            }
            for (; i < i_hi; i++) {
                long ii = i + i_o1_off;
                for (long j = j_lo; j < j_hi; j++) {
                    long jj = j + j_o2_off;
                    T o1_0 = operand1.get(ii, 0);
                    T o2_0 = operand2.get(0, jj);
                    T a00 = times.apply(o1_0, o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                    }
                    target.set(i, j, a00);
                }
            }

        }
    }

    /**
     * A kernel specialized for multiplication of matrices of "Double";
     * all the arithmetic and accumulation is performed in primitive double,
     * which can provide substantial performance improvements even though
     * times and plus are BinaryOperators, not primitive operations.
     *
     * The inner loop is 2x2 blocked for operand reuse.
     */

    public final static class D extends MultiplicationKernel<Double> {

        private final BinaryOperator<Double> times;
        private final BinaryOperator<Double> plus;

        public D(BinaryOperator<Double> times,
                BinaryOperator<Double> plus) {
            this.times = times;
            this.plus = plus;
        }

        @Override
        public boolean isApplicable(Matrix<Double> target, Matrix<Double> operand1, Matrix<Double> operand2) {
            return target instanceof RowMatrix.D && operand1 instanceof RowMatrix.D && operand2 instanceof RowMatrix.D;
        }

        @Override
        public void apply(Matrix<Double> target, Matrix<Double> operand1, Matrix<Double> operand2, long i_lo, long i_hi, long i_o1_off, long j_lo, long j_hi, long j_o2_off) {
            long K = operand1.length(COL_DIM);

            // General case, not so aligned.
            long i;
            for (i = i_lo; i + 1 < i_hi; i += 2) {
                long j;
                long ii = i + i_o1_off;
                for (j = j_lo; j + 1 < j_hi; j += 2) {
                    long jj = j + j_o2_off;
                    double o1_0 = operand1.get(ii, 0);
                    double o2_0 = operand2.get(0, jj);
                    double o1_1 = operand1.get(ii + 1, 0);
                    double o2_1 = operand2.get(0, jj + 1);
                    double a00 = times.apply(o1_0, o2_0);
                    double a01 = times.apply(o1_0, o2_1);
                    double a10 = times.apply(o1_1, o2_0);
                    double a11 = times.apply(o1_1, o2_1);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        o2_1 = operand2.get(k, jj + 1);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                        a01 = plus.apply(a01, times.apply(o1_0, o2_1));
                        a10 = plus.apply(a10, times.apply(o1_1, o2_0));
                        a11 = plus.apply(a11, times.apply(o1_1, o2_1));
                    }
                    target.set(i, j, a00);
                    target.set(i, j + 1, a01);
                    target.set(i + 1, j, a10);
                    target.set(i + 1, j + 1, a11);
                }
                if (j < j_hi) {
                    long jj = j + j_o2_off;
                    double o1_0 = operand1.get(ii, 0);
                    double o2_0 = operand2.get(0, jj);
                    double o1_1 = operand1.get(ii + 1, 0);
                    double a00 = times.apply(o1_0, o2_0);
                    double a10 = times.apply(o1_1, o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                        a10 = plus.apply(a10, times.apply(o1_1, o2_0));
                    }
                    target.set(i, j, a00);
                    target.set(i + 1, j, a10);
                }

            }
            if (i < i_hi) {
                long ii = i + i_o1_off;
                for (long j = j_lo; j < j_hi; j++) {
                    long jj = j + j_o2_off;
                    double o1_0 = operand1.get(ii, 0);
                    double o2_0 = operand2.get(0, jj);
                    double a00 = times.apply(o1_0, o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                    }
                    target.set(i, j, a00);
                }
            }
        }
    }

   /**
     * A kernel specialized for multiplication of matrices of "Double";
     * all the arithmetic and accumulation is performed in primitive double,
     * further specialized to use the standard operators * and + to implement
     * matrix multiplication.
     *
     * The inner loop is 2x2 blocked for operand reuse.
     */
    public final static class Dstd extends MultiplicationKernel<Double> {

        public static final Dstd kernel = new Dstd();

        private Dstd() {
        }

        @Override
        public boolean isApplicable(Matrix<Double> target, Matrix<Double> operand1, Matrix<Double> operand2) {
            return target instanceof RowMatrix.D && operand1 instanceof RowMatrix.D && operand2 instanceof RowMatrix.D;
        }

        @Override
        public void apply(Matrix<Double> target, Matrix<Double> operand1, Matrix<Double> operand2, long i_lo, long i_hi, long i_o1_off, long j_lo, long j_hi, long j_o2_off) {
            long K = operand1.length(COL_DIM);

            // General case, not so aligned.
            long i;
            for (i = i_lo; i + 1 < i_hi; i += 2) {
                long j;
                long ii = i + i_o1_off;
                for (j = j_lo; j + 1 < j_hi; j += 2) {
                    long jj = j + j_o2_off;
                    double o1_0 = operand1.get(ii, 0);
                    double o2_0 = operand2.get(0, jj);
                    double o1_1 = operand1.get(ii + 1, 0);
                    double o2_1 = operand2.get(0, jj + 1);
                    double a00 = (o1_0 * o2_0);
                    double a01 = (o1_0 * o2_1);
                    double a10 = (o1_1 * o2_0);
                    double a11 = (o1_1 * o2_1);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        o2_1 = operand2.get(k, jj + 1);
                        a00 += ((o1_0 * o2_0));
                        a01 += ((o1_0 * o2_1));
                        a10 += ((o1_1 * o2_0));
                        a11 += ((o1_1 * o2_1));
                    }
                    target.set(i, j, a00);
                    target.set(i, j + 1, a01);
                    target.set(i + 1, j, a10);
                    target.set(i + 1, j + 1, a11);
                }
                if (j < j_hi) {
                    long jj = j + j_o2_off;
                    double o1_0 = operand1.get(ii, 0);
                    double o2_0 = operand2.get(0, jj);
                    double o1_1 = operand1.get(ii + 1, 0);
                    double a00 = (o1_0 * o2_0);
                    double a10 = (o1_1 * o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        a00 += ((o1_0 * o2_0));
                        a10 += ((o1_1 * o2_0));
                    }
                    target.set(i, j, a00);
                    target.set(i + 1, j, a10);
                }

            }
            if (i < i_hi) {
                long ii = i + i_o1_off;
                for (long j = j_lo; j < j_hi; j++) {
                    long jj = j + j_o2_off;
                    double o1_0 = operand1.get(ii, 0);
                    double o2_0 = operand2.get(0, jj);
                    double a00 = (o1_0 * o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        a00 += ((o1_0 * o2_0));
                    }
                    target.set(i, j, a00);
                }
            }
        }
    }

    /**
     * A kernel specialized for Long, using primitive long for the inner
     * loop accumulator, but user-supplied times and plus.
     *
     * The inner loop is 2x2 blocked for operand reuse.
     */
    public static class L extends MultiplicationKernel<Long> {

        private final BinaryOperator<Long> times;
        private final BinaryOperator<Long> plus;

        public L(BinaryOperator<Long> times,
                BinaryOperator<Long> plus) {
            this.times = times;
            this.plus = plus;
        }

        @Override
        public boolean isApplicable(Matrix<Long> target, Matrix<Long> operand1, Matrix<Long> operand2) {
            return target instanceof RowMatrix.L && operand1 instanceof RowMatrix.L && operand2 instanceof RowMatrix.L;
        }

        @Override
        public void apply(Matrix<Long> target, Matrix<Long> operand1, Matrix<Long> operand2,
                long i_lo, long i_hi, long i_o1_off,
                long j_lo, long j_hi, long j_o2_off) {
            long K = operand1.length(COL_DIM);

            // General case, not so aligned.
            long i;
            for (i = i_lo; i + 1 < i_hi; i += 2) {
                long j;
                long ii = i + i_o1_off;
                for (j = j_lo; j + 1 < j_hi; j += 2) {
                    long jj = j + j_o2_off;
                    long o1_0 = operand1.get(ii, 0);
                    long o2_0 = operand2.get(0, jj);
                    long o1_1 = operand1.get(ii + 1, 0);
                    long o2_1 = operand2.get(0, jj + 1);
                    long a00 = times.apply(o1_0, o2_0);
                    long a01 = times.apply(o1_0, o2_1);
                    long a10 = times.apply(o1_1, o2_0);
                    long a11 = times.apply(o1_1, o2_1);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        o2_1 = operand2.get(k, jj + 1);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                        a01 = plus.apply(a01, times.apply(o1_0, o2_1));
                        a10 = plus.apply(a10, times.apply(o1_1, o2_0));
                        a11 = plus.apply(a11, times.apply(o1_1, o2_1));
                    }
                    target.set(i, j, a00);
                    target.set(i, j + 1, a01);
                    target.set(i + 1, j, a10);
                    target.set(i + 1, j + 1, a11);
                }
                if (j < j_hi) {
                    long jj = j + j_o2_off;
                    long o1_0 = operand1.get(ii, 0);
                    long o2_0 = operand2.get(0, jj);
                    long o1_1 = operand1.get(ii + 1, 0);
                    long a00 = times.apply(o1_0, o2_0);
                    long a10 = times.apply(o1_1, o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                        a10 = plus.apply(a10, times.apply(o1_1, o2_0));
                    }
                    target.set(i, j, a00);
                    target.set(i + 1, j, a10);
                }

            }
            if (i < i_hi) {
                long ii = i + i_o1_off;
                for (long j = j_lo; j < j_hi; j++) {
                    long jj = j + j_o2_off;
                    long o1_0 = operand1.get(ii, 0);
                    long o2_0 = operand2.get(0, jj);
                    long a00 = times.apply(o1_0, o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                    }
                    target.set(i, j, a00);
                }
            }
        }
    }

    /**
     * A kernel pre-specialized for DoubleComplex, with 2x2 inner loop
     * blocking for operand reuse.  Times and plus are user-supplied.
     */
    public final static class DX2 extends MultiplicationKernel<DComplex> {

        private final BinaryOperator<DComplex> times;
        private final BinaryOperator<DComplex> plus;

        public DX2(BinaryOperator<DComplex> times,
                BinaryOperator<DComplex> plus) {
            this.times = times;
            this.plus = plus;
        }

        @Override
        public boolean isApplicable(Matrix<DComplex> target, Matrix<DComplex> operand1, Matrix<DComplex> operand2) {
            return target instanceof RowMatrix.DX && operand1 instanceof RowMatrix.DX && operand2 instanceof RowMatrix.DX;
        }

        @Override
        public void apply(Matrix<DComplex> _target, Matrix<DComplex> _operand1, Matrix<DComplex> _operand2, long i_lo, long i_hi, long i_o1_off, long j_lo, long j_hi, long j_o2_off) {
            long K = _operand1.length(COL_DIM);

            RowMatrix.DX target = (RowMatrix.DX) _target;
            RowMatrix.DX operand1 = (RowMatrix.DX) _operand1;
            RowMatrix.DX operand2 = (RowMatrix.DX) _operand2;

            // General case, not so aligned.
            long i;
            for (i = i_lo; i + 1 < i_hi; i += 2) {
                long j;
                long ii = i + i_o1_off;
                for (j = j_lo; j + 1 < j_hi; j += 2) {
                    long jj = j + j_o2_off;
                    DComplex o1_0 = operand1.get(ii, 0);
                    DComplex o2_0 = operand2.get(0, jj);
                    DComplex o1_1 = operand1.get(ii + 1, 0);
                    DComplex o2_1 = operand2.get(0, jj + 1);
                    DComplex a00 = times.apply(o1_0, o2_0);
                    DComplex a01 = times.apply(o1_0, o2_1);
                    DComplex a10 = times.apply(o1_1, o2_0);
                    DComplex a11 = times.apply(o1_1, o2_1);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        o2_1 = operand2.get(k, jj + 1);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                        a01 = plus.apply(a01, times.apply(o1_0, o2_1));
                        a10 = plus.apply(a10, times.apply(o1_1, o2_0));
                        a11 = plus.apply(a11, times.apply(o1_1, o2_1));
                    }
                    target.set(i, j, a00);
                    target.set(i, j + 1, a01);
                    target.set(i + 1, j, a10);
                    target.set(i + 1, j + 1, a11);
                }
                if (j < j_hi) {
                    long jj = j + j_o2_off;
                    DComplex o1_0 = operand1.get(ii, 0);
                    DComplex o2_0 = operand2.get(0, jj);
                    DComplex o1_1 = operand1.get(ii + 1, 0);
                    DComplex a00 = times.apply(o1_0, o2_0);
                    DComplex a10 = times.apply(o1_1, o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                        a10 = plus.apply(a10, times.apply(o1_1, o2_0));
                    }
                    target.set(i, j, a00);
                    target.set(i + 1, j, a10);
                }

            }
            if (i < i_hi) {
                long ii = i + i_o1_off;
                for (long j = j_lo; j < j_hi; j++) {
                    long jj = j + j_o2_off;
                    DComplex o1_0 = operand1.get(ii, 0);
                    DComplex o2_0 = operand2.get(0, jj);
                    DComplex a00 = times.apply(o1_0, o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                    }
                    target.set(i, j, a00);
                }
            }
        }
    }

    /**
     * A kernel pre-specialized for DoubleComplex, with 3x3 inner loop
     * blocking for operand reuse.  Times and plus are user-supplied.
     */
    public final static class DX3 extends MultiplicationKernel<DComplex> {

        private final BinaryOperator<DComplex> times;
        private final BinaryOperator<DComplex> plus;

        public DX3(BinaryOperator<DComplex> times,
                BinaryOperator<DComplex> plus) {
            this.times = times;
            this.plus = plus;
        }

        @Override
        public boolean isApplicable(Matrix<DComplex> target, Matrix<DComplex> operand1, Matrix<DComplex> operand2) {
            return target instanceof RowMatrix.DX && operand1 instanceof RowMatrix.DX && operand2 instanceof RowMatrix.DX;
        }

        @Override
        public void apply(Matrix<DComplex> target, Matrix<DComplex> operand1, Matrix<DComplex> operand2, long i_lo, long i_hi, long i_o1_off, long j_lo, long j_hi, long j_o2_off) {
            long K = operand1.length(COL_DIM);

            // General case, not so aligned.
            long i;
            for (i = i_lo; i + 2 < i_hi; i += 3) {
                long j;
                long ii = i + i_o1_off;
                for (j = j_lo; j + 2 < j_hi; j += 3) {
                    long jj = j + j_o2_off;
                    DComplex o1_0 = operand1.get(ii, 0);
                    DComplex o2_0 = operand2.get(0, jj);
                    DComplex o1_1 = operand1.get(ii + 1, 0);
                    DComplex o2_1 = operand2.get(0, jj + 1);
                    DComplex o1_2 = operand1.get(ii + 2, 0);
                    DComplex o2_2 = operand2.get(0, jj + 2);
                    DComplex a00 = times.apply(o1_0, o2_0);
                    DComplex a01 = times.apply(o1_0, o2_1);
                    DComplex a02 = times.apply(o1_0, o2_2);
                    DComplex a10 = times.apply(o1_1, o2_0);
                    DComplex a11 = times.apply(o1_1, o2_1);
                    DComplex a12 = times.apply(o1_1, o2_2);
                    DComplex a20 = times.apply(o1_2, o2_0);
                    DComplex a21 = times.apply(o1_2, o2_1);
                    DComplex a22 = times.apply(o1_2, o2_2);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        o2_1 = operand2.get(k, jj + 1);
                        o1_2 = operand1.get(ii + 2, k);
                        o2_2 = operand2.get(k, jj + 2);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                        a01 = plus.apply(a01, times.apply(o1_0, o2_1));
                        a02 = plus.apply(a02, times.apply(o1_0, o2_2));
                        a10 = plus.apply(a10, times.apply(o1_1, o2_0));
                        a11 = plus.apply(a11, times.apply(o1_1, o2_1));
                        a12 = plus.apply(a12, times.apply(o1_1, o2_2));
                        a20 = plus.apply(a20, times.apply(o1_2, o2_0));
                        a21 = plus.apply(a21, times.apply(o1_2, o2_1));
                        a22 = plus.apply(a22, times.apply(o1_2, o2_2));
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
                for (; j < j_hi; j++) {
                    long jj = j + j_o2_off;
                    DComplex o1_0 = operand1.get(ii, 0);
                    DComplex o2_0 = operand2.get(0, jj);
                    DComplex o1_1 = operand1.get(ii + 1, 0);
                    DComplex o1_2 = operand1.get(ii + 2, 0);
                    DComplex a00 = times.apply(o1_0, o2_0);
                    DComplex a10 = times.apply(o1_1, o2_0);
                    DComplex a20 = times.apply(o1_2, o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        o1_1 = operand1.get(ii + 1, k);
                        o1_2 = operand1.get(ii + 2, k);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                        a10 = plus.apply(a10, times.apply(o1_1, o2_0));
                        a20 = plus.apply(a20, times.apply(o1_2, o2_0));
                    }
                    target.set(i, j, a00);
                    target.set(i + 1, j, a10);
                    target.set(i + 2, j, a20);
                }

            }
            for (; i < i_hi; i++) {
                long ii = i + i_o1_off;
                for (long j = j_lo; j < j_hi; j++) {
                    long jj = j + j_o2_off;
                    DComplex o1_0 = operand1.get(ii, 0);
                    DComplex o2_0 = operand2.get(0, jj);
                    DComplex a00 = times.apply(o1_0, o2_0);
                    for (long k = 1; k < K; k++) {
                        o1_0 = operand1.get(ii, k);
                        o2_0 = operand2.get(k, jj);
                        a00 = plus.apply(a00, times.apply(o1_0, o2_0));
                    }
                    target.set(i, j, a00);
                }
            }
        }
    }

}
