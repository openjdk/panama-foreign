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

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import javax.arrays.v2.Matrix;
import javax.arrays.v2.A2Expr;
import javax.arrays.v2.ColumnMatrix;
import javax.arrays.v2.RowMatrix;
import javax.arrays.v2.nodes.MatrixAggregate;
import javax.arrays.v2.nodes.Simplifier;
import javax.arrays.v2.nodes.MatTranspose;
import javax.arrays.v2.nodes.WrappedSimplifier;

/**
 *
 * @param <T>
 */
public interface AnyOp<T> {

    public Class<T> resultType();

    /**
     * A helper function for concisely generating matrix/array associative operators.
     * There appears to be a minor performance penalty with current compilers
     * (hotspot and graal)
     *
     * @param <T> The element type of the operands and result of the associative operator.
     * @param f The binary operator, usually a lambda.
     * @param name The name of the operator, for toString/debugging purposes.
     * @param type The Java class of the element type.
     * @return The associative operator
     */
    static <T> AssociativeOp<T> associative(BinaryOperator<T> f, final String name, final Class<T> type) {
        return new AssociativeOp<T>() {
            @Override
            public T apply(T x, T y) {
                return f.apply(x, y);
            }

            @Override
            public String toString() {
                return name;
            }

            @Override
            public Class<T> resultType() {
                return type;
            }

        };
    }

    /**
     *
     * A helper function for concisely generating matrix/array product operators.
     * There appears to be a minor performance penalty with current compilers
     * (hotspot and graal)
     *
     * @param <T> The element type of the operands and result of the product operator.
     * @param _times The element multiplication operation for an inner product.
     * @param _plus The element addition operation for an inner product.
     * @param name The name of the operator, for toString/debugging purposes.
     * @param type The Java class of the element type.
     * @return The product operator
     */
    static <T> ProductOp<T, T, T> productOp(final BinaryOperator<T> _times,
            final BinaryOperator<T> _plus,
            final String name,
            final Class<T> type) {
        return new ProductOp<T, T, T>() {

            @Override
            public T times(T x, T y) {
                return _times.apply(x, y);
            }

            @Override
            public T plus(T x, T y) {
                return _plus.apply(x, y);
            }

            @Override
            public String toString() {
                return name;
            }

            @Override
            public Class<T> resultType() {
                return type;
            }

        };
    }

    /**
     * Associative elementwise addition of arrays and matrices of Double.
     */
    static public final AssociativeOp<Double> DPLUS
            = associative((x, y) -> x + y, "+", Double.class);

    // Benchmarking experiment -- does it work better if we override default methods?
    static public final AssociativeOp<Double> DPLUSX = new AssociativeOp<Double>() {
        @Override
        public Double apply(Double x, Double y) {
            return x + y;
        }

        @Override
        public Double apply(Double x, Double y, Double z) {
            return x + y;
        }

        @Override
        public Double apply(Double w, Double x, Double y, Double z) {
            return w + x + y + z;
        }

        @Override
        public String toString() {
            return "+";
        }

        @Override
        public Class<Double> resultType() {
            return Double.class;
        }
    };

    /**
     *  Elementwise subtraction of arrays and matrices of Double.
     */
    static public final BinaryOp<Double, Double, Double> DMINUS
            = new BinaryOp<Double, Double, Double>() {
                @Override
                public Double apply(Double x, Double y) {
                    return x - y;
                }

                @Override
                public String toString() {
                    return "-";
                }

                @Override
                public Class<Double> resultType() {
                    return Double.class;
                }
            };

    /**
     *  Elementwise unary negation of arrays and matrices of Double.
     */
    static public final UnaryOp<Double, Double> DuMINUS
            = new UnaryOp<Double, Double>() {
                @Override
                public Double apply(Double x) {
                    return -x;
                }

                @Override
                public String toString() {
                    return "-";
                }

                @Override
                public Class<Double> resultType() {
                    return Double.class;
                }
            };

    /**
     *  Elementwise unary negation of arrays and matrices of Byte.
     */
    static public final UnaryOp<Byte, Byte> BuMINUS
            = new UnaryOp<Byte, Byte>() {
                @Override
                public Byte apply(Byte x) {
                    return (byte) -x;
                }

                @Override
                public String toString() {
                    return "-";
                }

                @Override
                public Class<Byte> resultType() {
                    return Byte.class;
                }
            };

   /**
     * Associative elementwise multiplication of arrays and matrices of Double.
     */
    static public final AssociativeOp<Double> DTIMES_elt = new AssociativeOp<Double>() {
        @Override
        public Double apply(Double x, Double y) {
            return x * y;
        }

        @Override
        public String toString() {
            return "*";
        }

        @Override
        public Class<Double> resultType() {
            return Double.class;
        }
    };

   /**
     * Product multiplication (inner product of rows and columns)
     * of arrays and matrices of Double.
     */
    static public final ProductOp<Double, Double, Double> DTIMES = new ProductOp<Double, Double, Double>() {
        @Override
        public Double times(Double x, Double y) {
            return x * y;
        }

        @Override
        public Double plus(Double x, Double y) {
            return x + y;
        }

        @Override
        public String toString() {
            return "*";
        }

        @Override
        public Class<Double> resultType() {
            return Double.class;
        }
    };


   /**
     * Product multiplication (inner product of rows and columns)
     * of arrays and matrices of Double, using the shorthand definition
     * to check the performance cost, if any.
     */
    static public final ProductOp<Double, Double, Double> DTIMES_tersedef
            = productOp((x, y) -> x * y, (x, y) -> x + y, "*", Double.class);

   /**
     * Product multiplication (inner product of rows and columns)
     * of arrays and matrices of Double.
     *
     * For benchmarking tests, this uses unboxed double for all accumulators within the
     * leaf kernel operation but does not modify times and plus.
     */
    static public final ProductOp<Double, Double, Double> DTIMES_kernel1 = new ProductOp<Double, Double, Double>() {
        @Override
        public Double times(Double x, Double y) {
            return x * y;
        }

        @Override
        public Double plus(Double x, Double y) {
            return x + y;
        }

        @Override
        public String toString() {
            return "*";
        }

        @Override
        public Class<Double> resultType() {
            return Double.class;
        }

        @Override
        public void setOffsetSubMatrix(Matrix<Double> target, Matrix<Double> operand1, Matrix<Double> operand2,
                long i_lo, long i_hi, long i_o1_off, long j_o1_off,
                long j_lo, long j_hi, long i_o2_off, long j_o2_off, long K) {

            // General case, not so aligned.
            long i;
            for (i = i_lo; i + 2 < i_hi; i += 3) {
                long j;
                long ii = i + i_o1_off;
                for (j = j_lo; j + 2 < j_hi; j += 3) {
                    long jj = j + j_o2_off;
                    double o1_0 = operand1.get(ii, 0);
                    double o2_0 = operand2.get(0, jj);
                    double o1_1 = operand1.get(ii + 1, 0);
                    double o2_1 = operand2.get(0, jj + 1);
                    double o1_2 = operand1.get(ii + 2, 0);
                    double o2_2 = operand2.get(0, jj + 2);
                    double a00 = times(o1_0, o2_0);
                    double a01 = times(o1_0, o2_1);
                    double a02 = times(o1_0, o2_2);
                    double a10 = times(o1_1, o2_0);
                    double a11 = times(o1_1, o2_1);
                    double a12 = times(o1_1, o2_2);
                    double a20 = times(o1_2, o2_0);
                    double a21 = times(o1_2, o2_1);
                    double a22 = times(o1_2, o2_2);
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
                for (; j < j_hi; j++) {
                    long jj = j + j_o2_off;
                    double o1_0 = operand1.get(ii, 0);
                    double o2_0 = operand2.get(0, jj);
                    double o1_1 = operand1.get(ii + 1, 0);
                    double o1_2 = operand1.get(ii + 2, 0);
                    double a00 = times(o1_0, o2_0);
                    double a10 = times(o1_1, o2_0);
                    double a20 = times(o1_2, o2_0);
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
            for (; i < i_hi; i++) {
                long ii = i + i_o1_off;
                for (long j = j_lo; j < j_hi; j++) {
                    long jj = j + j_o2_off;
                    double o1_0 = operand1.get(ii, 0);
                    double o2_0 = operand2.get(0, jj);
                    double a00 = times(o1_0, o2_0);
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
    };

   /**
     * Product multiplication (inner product of rows and columns)
     * of arrays and matrices of Double.
     *
     * For benchmarking tests, this tests for the use of a particular pair
     * of favored operand types (RowMatrix.D and ColumnMatrix.D) and in that case
     * completely inlines everything; this represents best-case from inlining
     * and specialization.
     */
    static public final ProductOp<Double, Double, Double> DTIMES_kernel2 = new ProductOp<Double, Double, Double>() {
        @Override
        public Double times(Double x, Double y) {
            return x * y;
        }

        @Override
        public Double plus(Double x, Double y) {
            return x + y;
        }

        @Override
        public String toString() {
            return "*";
        }

        @Override
        public Class<Double> resultType() {
            return Double.class;
        }

        @Override
        public void setOffsetSubMatrix(Matrix<Double> target, Matrix<Double> operand1, Matrix<Double> operand2,
                long i_lo, long i_hi, long i_o1_off, long j_o1_off,
                long j_lo, long j_hi, long i_o2_off, long j_o2_off, long K) {

            if (operand1 instanceof RowMatrix.D
                    && operand2 instanceof ColumnMatrix.D) {

                double[][] a1 = ((RowMatrix.D) operand1).blocks;
                double[][] a2 = ((ColumnMatrix.D) operand2).blocks;

                // General case, not so aligned.
                long i;
                for (i = i_lo; i + 2 < i_hi; i += 3) {
                    long j;
                    int ii = (int) (i + i_o1_off);
                    for (j = j_lo; j + 2 < j_hi; j += 3) {
                        int jj = (int) (j + j_o2_off);
                        double o1_0 = a1[ii][0];
                        double o2_0 = a2[jj][0];
                        double o1_1 = a1[ii + 1][0];
                        double o2_1 = a2[jj + 1][0];
                        double o1_2 = a1[ii + 2][0];
                        double o2_2 = a2[jj + 2][0];
                        double a00 = o2_0 * o1_0;
                        double a01 = o2_1 * o1_0;
                        double a02 = o2_2 * o1_0;
                        double a10 = o2_0 * o1_1;
                        double a11 = o2_1 * o1_1;
                        double a12 = o2_2 * o1_1;
                        double a20 = o2_0 * o1_2;
                        double a21 = o2_1 * o1_2;
                        double a22 = o2_2 * o1_2;
                        for (int k = 1; k < K; k++) {
                            int k1 = k + (int) j_o1_off;
                            int k2 = k + (int) i_o2_off;
                            o1_0 = a1[ii][k1];
                            o2_0 = a2[jj][k2];
                            o1_1 = a1[ii + 1][k1];
                            o2_1 = a2[jj + 1][k2];
                            o1_2 = a1[ii + 2][k1];
                            o2_2 = a2[jj + 2][k2];
                            a00 = a00 + o2_0 * o1_0;
                            a01 = a01 + o2_1 * o1_0;
                            a02 = a02 + o2_2 * o1_0;
                            a10 = a10 + o2_0 * o1_1;
                            a11 = a11 + o2_1 * o1_1;
                            a12 = a12 + o2_2 * o1_1;
                            a20 = a20 + o2_0 * o1_2;
                            a21 = a21 + o2_1 * o1_2;
                            a22 = a22 + o2_2 * o1_2;
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
                        int jj = (int) (j + j_o2_off);
                        double o1_0 = a1[ii][0];
                        double o2_0 = a2[jj][0];
                        double o1_1 = a1[ii + 1][0];
                        double o1_2 = a1[ii + 2][0];
                        double a00 = o2_0 * o1_0;
                        double a10 = o2_0 * o1_1;
                        double a20 = o2_0 * o1_2;
                        for (int k = 1; k < K; k++) {
                            int k1 = k + (int) j_o1_off;
                            int k2 = k + (int) i_o2_off;
                            o1_0 = a1[ii][k1];
                            o2_0 = a2[jj][k2];
                            o1_1 = a1[ii + 1][k1];
                            o1_2 = a1[ii + 2][k1];
                            a00 = a00 + o2_0 * o1_0;
                            a10 = a10 + o2_0 * o1_1;
                            a20 = a20 + o2_0 * o1_2;
                        }
                        target.set(i, j, a00);
                        target.set(i + 1, j, a10);
                        target.set(i + 2, j, a20);
                    }
                }
                for (; i < i_hi; i++) {
                    int ii = (int) (i + i_o1_off);
                    for (long j = j_lo; j < j_hi; j++) {
                        int jj = (int) (j + j_o2_off);
                        double o1_0 = a1[ii][0];
                        double o2_0 = a2[jj][0];
                        double a00 = o2_0 * o1_0;
                        for (int k = 1; k < K; k++) {
                            int k1 = k + (int) j_o1_off;
                            int k2 = k + (int) i_o2_off;
                            o1_0 = a1[ii][k1];
                            o2_0 = a2[jj][k2];
                            a00 = a00 + o2_0 * o1_0;
                        }
                        target.set(i, j, a00);
                    }
                }
            } else {
                System.out.println("Oopsie-daisy, not feeding the kernel the right inputs");
                DTIMES_kernel1.setOffsetSubMatrix(target, operand1, operand2,
                        i_lo, i_hi, i_o1_off, j_o1_off, j_lo, j_hi, i_o2_off, j_o2_off, K);
            }
        }
    };

   /**
     * Product multiplication (inner product of rows and columns)
     * of arrays and matrices of Double.
     *
     * For benchmarking tests, this tests for the use of a particular pair
     * of favored operand types (RowMatrix.D and ColumnMatrix.D) but does not
     * include heroic inlining; will the compiler be able to make use of the
     * gratuitously checked type information?  The intermediate accumulators
     * are unboxed.
     */
    static public final ProductOp<Double, Double, Double> DTIMES_kernel3 = new ProductOp<Double, Double, Double>() {
        @Override
        public Double times(Double x, Double y) {
            return x * y;
        }

        @Override
        public Double plus(Double x, Double y) {
            return x + y;
        }

        @Override
        public String toString() {
            return "*";
        }

        @Override
        public Class<Double> resultType() {
            return Double.class;
        }

        @Override
        public void setOffsetSubMatrix(Matrix<Double> target, Matrix<Double> _operand1, Matrix<Double> _operand2,
                long i_lo, long i_hi, long i_o1_off, long j_o1_off,
                long j_lo, long j_hi, long i_o2_off, long j_o2_off, long K) {

            // General case, not so aligned.
            if (_operand1 instanceof RowMatrix.D
                    && _operand2 instanceof ColumnMatrix.D) {
                RowMatrix.D operand1 = (RowMatrix.D) _operand1;
                ColumnMatrix.D operand2 = (ColumnMatrix.D) _operand2;
                long i;
                for (i = i_lo; i + 2 < i_hi; i += 3) {
                    long j;
                    long ii = i + i_o1_off;
                    for (j = j_lo; j + 2 < j_hi; j += 3) {
                        long jj = j + j_o2_off;
                        double o1_0 = operand1.get(ii, 0);
                        double o2_0 = operand2.get(0, jj);
                        double o1_1 = operand1.get(ii + 1, 0);
                        double o2_1 = operand2.get(0, jj + 1);
                        double o1_2 = operand1.get(ii + 2, 0);
                        double o2_2 = operand2.get(0, jj + 2);
                        double a00 = times(o1_0, o2_0);
                        double a01 = times(o1_0, o2_1);
                        double a02 = times(o1_0, o2_2);
                        double a10 = times(o1_1, o2_0);
                        double a11 = times(o1_1, o2_1);
                        double a12 = times(o1_1, o2_2);
                        double a20 = times(o1_2, o2_0);
                        double a21 = times(o1_2, o2_1);
                        double a22 = times(o1_2, o2_2);
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
                    for (; j < j_hi; j++) {
                        long jj = j + j_o2_off;
                        double o1_0 = operand1.get(ii, 0);
                        double o2_0 = operand2.get(0, jj);
                        double o1_1 = operand1.get(ii + 1, 0);
                        double o1_2 = operand1.get(ii + 2, 0);
                        double a00 = times(o1_0, o2_0);
                        double a10 = times(o1_1, o2_0);
                        double a20 = times(o1_2, o2_0);
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
                for (; i < i_hi; i++) {
                    long ii = i + i_o1_off;
                    for (long j = j_lo; j < j_hi; j++) {
                        long jj = j + j_o2_off;
                        double o1_0 = operand1.get(ii, 0);
                        double o2_0 = operand2.get(0, jj);
                        double a00 = times(o1_0, o2_0);
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
            } else {
                System.out.println("Oopsie-daisy, not feeding the kernel the right inputs");
                DTIMES_kernel1.setOffsetSubMatrix(target, _operand1, _operand2,
                        i_lo, i_hi, i_o1_off, j_o1_off, j_lo, j_hi, i_o2_off, j_o2_off, K);
            }
        }
    };

    /**
     * Double unary identity.
     */
    static public final UnaryOp<Double, Double> DID = new UnaryOp<Double, Double>() {
        @Override
        public Double apply(Double x) {
            return x;
        }

        @Override
        public String toString() {
            return "=";
        }

        @Override
        public Class<Double> resultType() {
            return Double.class;
        }
    };

    static public <T> AggregateOp<T> aggregateOp(final UnaryOperator<A2Expr<T>> f, final String name, final Class<T> type) {
        return new AggregateOp<T>() {
            @Override
            public A2Expr<T> apply(A2Expr<T> x) {
                return f.apply(x);
            }

            @Override
            public Class<T> resultType() {
                return type;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    /**
     * Transposed view of a matrix of Double.
     */
    static public final AggregateOp<Double> DTRANSPOSE
            = aggregateOp((x) -> new MatTranspose<>(x), "TRANSPOSE", Double.class);

    /**
     * Tests the ability to inject a custom simplifier with a single operator
     * at the top of an expression tree.
     */
    static public final AggregateOp<Double> D_WRAPPED_SIMPLIFIER
            = aggregateOp((x) -> new MatrixAggregate<Double>(x) {
                @Override
                public A2Expr<Double> simplify(Matrix<Double> target, Simplifier simplifier) {
                    return x.simplify(target, new WrappedSimplifier());
                }
            },
            "WRAPPER", Double.class);
}
