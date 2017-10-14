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
package org.openjdk.tests.javax.arrays.v2.bench;
import java.util.function.BinaryOperator;
import javax.arrays.tbd.LongLongFunction;
import javax.arrays.v2.Matrix;
import static javax.arrays.v2.A2Expr.COL_DIM;
import static javax.arrays.v2.A2Expr.ROW_DIM;

public class Baseline {

    /**
     * Primitive double matrix multiplication, serial textbook algorithm.
     * a gets b times c
     * a is m x n b is m x p c is p x n
     *
     * @param a m by n target for product of multiplication
     * @param b m by p first factor
     * @param c p by n second factor
     */
    public static void mmBaseline(double[][] a, double[][] b, double[][] c) {
        int m = a.length;
        int n = a[0].length;
        int p = c.length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double x = 0;
                for (int k = 0; k < p; k++) {
                    x += b[i][k] * c[k][j];
                }
                a[i][j] = x;
            }
        }
    }

    /**
     * Common to several benchmarks, various block sizes to benchmark.
     */
    final static int[] block_sizes = {18, 32, 36, 72};

    /**
     * Return the sum ("op") of the trace of a with accumulator.
     *
     * @param <T>
     * @param a
     * @param plus_op
     * @param accumulator
     * @return
     */
    static public <T> T traceAccumulate(Matrix<T> a, BinaryOperator<T> plus_op, T accumulator) {
        T x = accumulator;
        long l = Math.min(a.length(ROW_DIM), a.length(COL_DIM));
        for (long i = 0; i < l; i++) {
            x = plus_op.apply(x, a.get(i,i));
        }
        return x;
    }

    /**
     * Return the sum ("op") of the trace of a with accumulator.
     *
     * @param a
     * @param accumulator
     * @return
     */
    static public double traceAccumulate(double[][] a, double accumulator) {
        double x = accumulator;
        int l = a.length;
        if (l > 0) l = Math.min(l, a[0].length); // assume uniform
        for (long i = 0; i < l; i++) {
            x = x + a[(int)i][(int)i];
        }
        return x;
    }

    /**
     * An initializer for 2-D uniform Java matrices that uses the same
     * initializer function as 2-D arrays2.0 matrices.
     *
     * @param a
     * @param filler
     */
    static public void set(double[][] a, LongLongFunction<Double> filler) {
        int m = a.length;
        int n = m > 0 ? a[0].length : 0;
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                a[i][j] = filler.apply(i,j);
    }

}
