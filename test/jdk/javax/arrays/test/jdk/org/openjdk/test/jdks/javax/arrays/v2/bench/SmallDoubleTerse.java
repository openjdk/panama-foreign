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
import static javax.arrays.v2.A2Expr.Parameters.setBlockSizes;
import javax.arrays.v2.ColumnMatrix;
import javax.arrays.v2.RowMatrix;
import static javax.arrays.v2.ops.AnyOp.DTIMES_tersedef;
import static org.openjdk.tests.javax.arrays.v2.bench.Baseline.block_sizes;
import static org.openjdk.tests.javax.arrays.v2.bench.Baseline.set;

/**
 * Benchmarks a double matrix multiplication vs the double baseline
 * using the fully generic multiply with hand-customized storage.
 */

public class SmallDoubleTerse extends Baseline {

    public static void main(String[] args) {
        timingSmall();
    }

    public static void timingSmall() {
        int M = 500;
        int N = 500;
        int P = 500;

        double[][] a = new double[M][N];
        double[][] b = new double[M][P];
        double[][] c = new double[P][N];

        RowMatrix.D A = new RowMatrix.D(M,N);
        RowMatrix.D B = new RowMatrix.D(M,P);
        ColumnMatrix.D C = new ColumnMatrix.D(P,N);

        BinaryOperator<Double> plus = (x, y) -> x + y;

        LongLongFunction<Double> filler = (i,j) -> 1.0/(1 + Math.abs(i-j));

        B.set(filler);
        C.set(filler);
        set(b, filler);
        set(c, filler);
        long t;
        System.out.println("Matrix multiplication times double-textbook vs terse-definition-Double-generic, milliseconds");
        Double Sum = 0.0;
        double sum = 0.0;
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 4; j++) {

                setBlockSizes(64, block_sizes[j]);

                t = System.currentTimeMillis();
                Baseline.mmBaseline(a, b, c);
                long e_baseline = System.currentTimeMillis() - t;

                t = System.currentTimeMillis();
                A.set(DTIMES_tersedef.apply(B,C));
                long e_generic = System.currentTimeMillis() - t;

                sum = traceAccumulate(a, sum);
                Sum = traceAccumulate(A, plus, Sum);

                System.out.println("block = " + block_sizes[j] + " base = " + e_baseline + ", generic = " + e_generic);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ;
                }
            }
        }
        System.out.println("sum = " + sum + ", Sum = " + Sum);

    }
}
