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
package org.openjdk.tests.javax.arrays.v2;

import javax.arrays.v2.Matrix;
import javax.arrays.v2.A2Expr;
import javax.arrays.v2.Array;
import javax.arrays.v2.DoubleArray;
import javax.arrays.v2.RowMatrix;
import javax.arrays.v2.Scalar;
import javax.arrays.v2.nodes.MatrixAssociative3ary;
import javax.arrays.v2.nodes.MatrixAssociative4ary;
import static javax.arrays.v2.ops.AnyOp.DMINUS;
import static javax.arrays.v2.ops.AnyOp.DPLUS;
import static javax.arrays.v2.ops.AnyOp.DTIMES;
import static javax.arrays.v2.ops.AnyOp.DTRANSPOSE;
import static javax.arrays.v2.ops.AnyOp.DuMINUS;
import static org.testng.Assert.*;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

/**
 *
 */
public class A2OpEval {

    public static void assertEquals(double found, double expected) {
        // Filter -0 into 0, yes, that is an issue in places.
        org.testng.Assert.assertEquals(expected, 0 + found);
    }

    public static void main(String[] args) {
        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[]{A2OpEval.class});
        testng.addListener(tla);
        testng.run();
    }

    public A2OpEval() {
    }

    @Test
    public void eltWise1MinusSmall() {
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        a.set((i, j) -> 100.0 * i + j);
        assertEquals(a.get(2, 1), 201.0);
        b.set(DuMINUS.apply(a));
        assertEquals(b.get(2, 1), -201.0);

    }

    @Test
    public void transposeSmall() {
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(2, 3);
        a.set((i, j) -> 100.0 * i + j);
        assertEquals(a.get(2, 1), 201.0);
        b.set(DTRANSPOSE.apply(a));
        assertEquals(b.get(1, 2), 201.0);
    }

    @Test
    public void transposeMinusSmall() {
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(2, 3);
        a.set((i, j) -> 100.0 * i + j);
        assertEquals(a.get(2, 1), 201.0);
        b.set(DTRANSPOSE.apply(DuMINUS.apply(a)));
        assertEquals(b.get(1, 2), -201.0);
        A2Expr<Double> e = DTRANSPOSE.apply(DuMINUS.apply(a));
        e = e.simplify(b);
        System.out.println(e);
    }

    @Test
    public void minusTransposeSmall() {
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(2, 3);
        a.set((i, j) -> 100.0 * i + j);
        assertEquals(a.get(2, 1), 201.0);
        b.set(DuMINUS.apply(DTRANSPOSE.apply(a)));
        assertEquals(b.get(1, 2), -201.0);
        A2Expr<Double> e = DuMINUS.apply(DTRANSPOSE.apply(a));
        e = e.simplify(b);
        System.out.println(e);
    }

    @Test
    public void minusTTTSmall() {
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(2, 3);
        a.set((i, j) -> 100.0 * i + j);
        assertEquals(a.get(2, 1), 201.0);
        b.set(DuMINUS.apply(DTRANSPOSE.apply(DTRANSPOSE.apply(DTRANSPOSE.apply(a)))));
        assertEquals(b.get(1, 2), -201.0);
        A2Expr<Double> e = DuMINUS.apply(DTRANSPOSE.apply(DTRANSPOSE.apply(DTRANSPOSE.apply(a))));
        e = e.simplify(b);
        System.out.println(e);
    }

    @Test
    public void eltWise1MinusLarge() {
        A2Expr.Parameters.setBlockSizes(16, 16);
        Matrix<Double> a = new RowMatrix.D(301, 300);
        Matrix<Double> b = new RowMatrix.D(301, 300);
        a.set((i, j) -> 1000.0 * i + j);
        assertEquals(a.get(300, 299), 300299.0);
        b.set(DuMINUS.apply(a));
        assertEquals(b.get(300, 299), -300299.0);
        assertEquals(b.get(1, 2), -1002.0);
        assertEquals(b.get(1, 299), -1299.0);
        assertEquals(b.get(300, 1), -300001.0);
        assertEquals(b.get(1, 0), -1000.0);
        assertEquals(b.get(0, 299), -299.0);
        assertEquals(b.get(300, 0), -300000.0);
    }

    @Test
    public void assoc2PlusSmall() {
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        a.set((i, j) -> 100.0 * i + j);
        b.set((i, j) -> 1000.0 * i + 10 * j);
        assertEquals(a.get(2, 1), 201.0);
        assertEquals(b.get(2, 1), 2010.0);
        c.set(DPLUS.apply(a, b));
        assertEquals(c.get(2, 1), 2211.0);
    }

    @Test
    public void assoc2PlusLarge() {
        A2Expr.Parameters.setBlockSizes(16, 16);
        Matrix<Double> a = new RowMatrix.D(301, 300);
        Matrix<Double> b = new RowMatrix.D(301, 300);
        Matrix<Double> c = new RowMatrix.D(301, 300);
        a.set((i, j) -> 1000.0 * i + j);
        assertEquals(a.get(300, 299), 300299.0);
        b.set((i, j) -> 1000.0 * i + 2 * j);
        c.set(DPLUS.apply(a, b));
        assertEquals(c.get(300, 299), 600897.0);
        assertEquals(c.get(1, 2), 2006.0);
        assertEquals(c.get(1, 299), 2897.0);
        assertEquals(c.get(300, 1), 600003.0);
        assertEquals(c.get(1, 0), 2000.0);
        assertEquals(c.get(0, 299), 897.0);
        assertEquals(c.get(300, 0), 600000.0);
    }

    @Test
    public void binaryMinusLarge() {
        A2Expr.Parameters.setBlockSizes(16, 16);
        Matrix<Double> a = new RowMatrix.D(301, 300);
        Matrix<Double> b = new RowMatrix.D(301, 300);
        Matrix<Double> c = new RowMatrix.D(301, 300);
        a.set((i, j) -> 2000.0 * i + 2 * j);
        b.set((i, j) -> 1000.0 * i + j);
        c.set(DMINUS.apply(a, b));
        assertEquals(c.get(300, 299), 300299.0);
        assertEquals(c.get(1, 2), 1002.0);
        assertEquals(c.get(1, 299), 1299.0);
        assertEquals(c.get(300, 1), 300001.0);
        assertEquals(c.get(1, 0), 1000.0);
        assertEquals(c.get(0, 299), 299.0);
        assertEquals(c.get(300, 0), 300000.0);
    }

    @Test
    public void assoc3PlusLarge() {
        A2Expr.Parameters.setBlockSizes(16, 16);
        Matrix<Double> a = new RowMatrix.D(301, 300);
        Matrix<Double> b = new RowMatrix.D(301, 300);
        Matrix<Double> c = new RowMatrix.D(301, 300);
        Matrix<Double> d = new RowMatrix.D(301, 300);

        a.set((i, j) -> 2 * (1000.0 * i + j));
        b.set((i, j) -> 4 * (1000.0 * i + j));
        c.set((i, j) -> -5 * (1000.0 * i + j));

        A2Expr<Double> e = DPLUS.apply(a, DPLUS.apply(b, c));
        e = e.simplify(d);
        A2Expr<Double> _e = e.simplify(d);
        org.testng.Assert.assertEquals(e, _e, "Default simplify should be idempotent");
        assertTrue(e instanceof MatrixAssociative3ary, "Expected EltWise3");

        d.set(e);
        assertEquals(d.get(300, 299), 300299.0);
        assertEquals(d.get(1, 2), 1002.0);
        assertEquals(d.get(1, 299), 1299.0);
        assertEquals(d.get(300, 1), 300001.0);
        assertEquals(d.get(1, 0), 1000.0);
        assertEquals(d.get(0, 299), 299.0);
        assertEquals(d.get(300, 0), 300000.0);
    }

    @Test
    public void assoc4PlusLarge() {
        // Force a few levels of fork/join
        A2Expr.Parameters.setBlockSizes(16, 16);
        Matrix<Double> a = new RowMatrix.D(301, 300);
        Matrix<Double> b = new RowMatrix.D(301, 300);
        Matrix<Double> c = new RowMatrix.D(301, 300);
        Matrix<Double> d = new RowMatrix.D(301, 300);
        Matrix<Double> e = new RowMatrix.D(301, 300);

        // Initialize inputs
        a.set((i, j) -> 2 * (1000.0 * i + j));
        b.set((i, j) -> 4 * (1000.0 * i + j));
        c.set((i, j) -> -3 * (1000.0 * i + j));
        d.set((i, j) -> -2 * (1000.0 * i + j));

        // Form expression, verify that it simplifies to a 4-operand plus
        A2Expr<Double> f = DPLUS.apply(DPLUS.apply(a, b), DPLUS.apply(c, d));
        f = f.simplify(e);
        A2Expr<Double> _f = f.simplify(e);
        org.testng.Assert.assertEquals(f, _f, "Default simplify should be idempotent");
        assertTrue(f instanceof MatrixAssociative4ary, "Expected EltWise4");

        // Evaluate
        e.set(f);

        // Check
        assertEquals(e.get(300, 299), 300299.0);
        assertEquals(e.get(1, 2), 1002.0);
        assertEquals(e.get(1, 299), 1299.0);
        assertEquals(e.get(300, 1), 300001.0);
        assertEquals(e.get(1, 0), 1000.0);
        assertEquals(e.get(0, 299), 299.0);
        assertEquals(e.get(300, 0), 300000.0);
    }

    @Test
    public void scalarRightPlusLarge() {
        A2Expr.Parameters.setBlockSizes(16, 16);
        Matrix<Double> a = new RowMatrix.D(301, 300);
        Double b = 1.0;
        Matrix<Double> c = new RowMatrix.D(301, 300);
        a.set((i, j) -> 1000.0 * i + j);
        assertEquals(a.get(300, 299), 300299.0);
        c.set(DPLUS.apply(a, b));

        assertEquals(c.get(300, 299), 300300.0);
        assertEquals(c.get(1, 2), 1003.0);
        assertEquals(c.get(1, 299), 1300.0);
        assertEquals(c.get(300, 1), 300002.0);
        assertEquals(c.get(1, 0), 1001.0);
        assertEquals(c.get(0, 299), 300.0);
        assertEquals(c.get(300, 0), 300001.0);
    }

    @Test
    public void scalarLeftPlusLarge() {
        A2Expr.Parameters.setBlockSizes(16, 16);
        Matrix<Double> a = new RowMatrix.D(301, 300);
        Double b = 1.0;
        Matrix<Double> c = new RowMatrix.D(301, 300);
        a.set((i, j) -> 1000.0 * i + j);
        assertEquals(a.get(300, 299), 300299.0);
        c.set(DPLUS.apply(b, a));

        assertEquals(c.get(300, 299), 300300.0);
        assertEquals(c.get(1, 2), 1003.0);
        assertEquals(c.get(1, 299), 1300.0);
        assertEquals(c.get(300, 1), 300002.0);
        assertEquals(c.get(1, 0), 1001.0);
        assertEquals(c.get(0, 299), 300.0);
        assertEquals(c.get(300, 0), 300001.0);
    }

    @Test
    public void productSmall() {
        Matrix<Double> a = new RowMatrix.D(2, 2);
        Matrix<Double> b = new RowMatrix.D(2, 2);
        Matrix<Double> c = new RowMatrix.D(2, 2);
        a.set((i, j) -> i == j ? 2.0 : 0.0); // 2s on the diagonal
        b.set((i, j) -> (double) j - i); // [0 1; -1 0]
        c.set(DTIMES.apply(b, a));
        assertEquals(c.get(0, 0), 0.0);
        assertEquals(c.get(0, 1), 2.0);
        assertEquals(c.get(1, 0), -2.0);
        assertEquals(c.get(1, 1), 0.0);

        c.set(DTIMES.apply(a, b));
        assertEquals(c.get(0, 0), 0.0);
        assertEquals(c.get(0, 1), 2.0);
        assertEquals(c.get(1, 0), -2.0);
        assertEquals(c.get(1, 1), 0.0);

        c.set(DTIMES.apply(a, a));
        assertEquals(c.get(0, 0), 4.0);
        assertEquals(c.get(0, 1), 0.0);
        assertEquals(c.get(1, 0), 0.0);
        assertEquals(c.get(1, 1), 4.0);

        c.set(DTIMES.apply(b, b));
        assertEquals(c.get(0, 0), -1.0);
        assertEquals(c.get(0, 1), 0.0);
        assertEquals(c.get(1, 0), 0.0);
        assertEquals(c.get(1, 1), -1.0);
    }

    @Test
    public void productLarge() {
        A2Expr.Parameters.setBlockSizes(16, 16);
        Matrix<Double> a = new RowMatrix.D(200, 200);
        Matrix<Double> b = new RowMatrix.D(200, 200);
        Matrix<Double> c = new RowMatrix.D(200, 200);
        a.set((i, j) -> i == j ? 2.0 : 0.0); // 2s on the diagonal
        b.set((i, j) -> (double) j - i); // [0 1; -1 0]
        c.set(DTIMES.apply(a, b));
        assertEquals(c.get(0, 0), 0.0);
        assertEquals(c.get(0, 1), 2.0);
        assertEquals(c.get(1, 0), -2.0);
        assertEquals(c.get(1, 1), 0.0);
        assertEquals(c.get(0, 0), 0.0);
        assertEquals(c.get(0, 100), 200.0);
        assertEquals(c.get(100, 0), -200.0);
        assertEquals(c.get(199, 198), -2.0);
        assertEquals(c.get(198, 199), 2.0);
        c.set(DTIMES.apply(b, a));
        assertEquals(c.get(0, 0), 0.0);
        assertEquals(c.get(0, 1), 2.0);
        assertEquals(c.get(1, 0), -2.0);
        assertEquals(c.get(1, 1), 0.0);
        assertEquals(c.get(0, 0), 0.0);
        assertEquals(c.get(0, 100), 200.0);
        assertEquals(c.get(100, 0), -200.0);
        assertEquals(c.get(199, 198), -2.0);
        assertEquals(c.get(198, 199), 2.0);
    }

    @Test
    public void productLarge2() {
        // Modify this to do submatrices, non-uniform interior
        A2Expr.Parameters.setBlockSizes(16, 16);
        Matrix<Double> a = new RowMatrix.D(202, 203);
        Matrix<Double> b = new RowMatrix.D(203, 202);
        Matrix<Double> c = new RowMatrix.D(204, 205);

        a.set((i, j) -> 9999.0);
        b.set((i, j) -> 9999.0);

        test200SquareMultiply(a.subMatrix(1, 201, 1, 201), b.subMatrix(2, 202, 1, 201), c.subMatrix(2, 202, 3, 203));
    }

    void test200SquareMultiply(Matrix<Double> a, Matrix<Double> b, Matrix<Double> c) {
        a.set((i, j) -> i == j ? (double) i : 0.0); // 2s on the diagonal
        b.set((i, j) -> (double) j - i); // [0 1; -1 0]

        System.out.println("A corner");
        printULcorner(a);
        System.out.println("B corner");
        printULcorner(b);

        c.set(DTIMES.apply(a, b));
        System.out.println("C corner");
        printULcorner(c);

        assertEquals(c.get(0, 0), 0.0);
        assertEquals(c.get(0, 1), 0.0);
        assertEquals(c.get(1, 0), -1.0);
        assertEquals(c.get(1, 1), 0.0);
        assertEquals(c.get(0, 0), 0.0);
        assertEquals(c.get(0, 100), 0.0);
        assertEquals(c.get(100, 0), -10000.0);
        assertEquals(c.get(199, 198), -199.0);
        assertEquals(c.get(198, 199), 198.0);

        c.set(DTIMES.apply(b, a));
        assertEquals(c.get(0, 0), 0.0);
        assertEquals(c.get(0, 1), 1.0);
        assertEquals(c.get(1, 0), 0.0);
        assertEquals(c.get(1, 1), 0.0);
        assertEquals(c.get(0, 0), 0.0);
        assertEquals(c.get(0, 100), 10000.0);
        assertEquals(c.get(100, 0), 0.0);
        assertEquals(c.get(199, 198), -198.0);
        assertEquals(c.get(198, 199), 199.0);
    }

    void printULcorner(Matrix<Double> a) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                System.out.print(" " + a.get(i, j));
            }
            System.out.println();
        }
    }

    @Test
    public void arrayUnary() {
        Array<Double> a = DoubleArray.make(1000000);
        Array<Double> b = DoubleArray.make(1000000);
        a.set((i) -> (double) i);
        b.set(DuMINUS.apply(a));
        assertEquals(b.get(0), 0);
        assertEquals(b.get(1), -1);
        assertEquals(b.get(999999), -999999);
    }

    @Test
    public void arrayBinary() {
        Array<Double> a = DoubleArray.make(1000000);
        Array<Double> b = DoubleArray.make(1000000);
        Array<Double> c = DoubleArray.make(1000000);
        a.set((i) -> (double) i);
        b.set((i) -> (double) -2 * i);
        c.set(DPLUS.apply(a, b));
        assertEquals(c.get(0), 0);
        assertEquals(c.get(1), -1);
        assertEquals(c.get(999999), -999999);
    }

    @Test
    public void arrayBinaryNA() {
        Array<Double> a = DoubleArray.make(1000000);
        Array<Double> b = DoubleArray.make(1000000);
        Array<Double> c = DoubleArray.make(1000000);
        a.set((i) -> (double) i);
        b.set((i) -> (double) 2 * i);
        c.set(DMINUS.apply(a, b));
        assertEquals(c.get(0), 0);
        assertEquals(c.get(1), -1);
        assertEquals(c.get(999999), -999999);
    }

    @Test
    public void arrayBinaryWithTemps() {
        Array<Double> a = DoubleArray.make(1000000);
        Array<Double> b = DoubleArray.make(1000000);
        Array<Double> c = DoubleArray.make(1000000);
        Array<Double> d = DoubleArray.make(1000000);
        Array<Double> e = DoubleArray.make(1000000);
        a.set((i) -> (double) 7 * i);
        b.set((i) -> (double) 4 * i); // 7 - 4 = 3
        c.set((i) -> (double) 5 * i);
        d.set((i) -> (double) 9 * i); // 5 - 9 = -4
        e.set(DPLUS.apply(DMINUS.apply(a, b), DMINUS.apply(c, d)));
        assertEquals(e.get(0), 0);
        assertEquals(e.get(1), -1);
        assertEquals(e.get(999999), -999999);
    }

    @Test
    public void MVProduct() {
        A2Expr.Parameters.setBlockSizes(16, 16);
        Matrix<Double> a = new RowMatrix.D(55, 100);
        Array<Double> b = DoubleArray.make(100);
        Array<Double> c = DoubleArray.make(100);

        a.set((i, j) -> 99999.0);
        b.set(i -> 88888.0);
        c.set(i -> 1000.0 + i);

        // aa 50 x 60
        Matrix<Double> aa = a.subMatrix(1, 51, 10, 70);
        Array<Double> bb = b.subArray(5, 65);
        Array<Double> cc = c.subArray(10, 60);

        aa.set((i, j) -> i == j ? 1.0 : 0.0);
        bb.set(i -> (double) i);

        assertEquals(bb.get(3), 3.0);

        cc.set(DTIMES.apply(aa, bb));

        assertEquals(cc.get(0), 0.0);
        assertEquals(cc.get(1), 1.0);
        assertEquals(cc.get(49), 49.0);
        assertEquals(c.get(9), 1009.0);
        assertEquals(c.get(60), 1060.0);
    }

    @Test
    public void AVProductSmall() {
        A2Expr.Parameters.setBlockSizes(8, 8);
        Array<Double> a = DoubleArray.make(100);
        Array<Double> b = DoubleArray.make(100);
        Scalar<Double> c = new Scalar<>();

        a.set(i -> (double) i - 1);
        b.set(i -> 2.0 * i);

        int N = 30;

        Array<Double> aa = a.subArray(2, 4); // 1-30
        Array<Double> bb = b.subArray(1, 3); // 2-60 by 2

        c.set(DTIMES.apply(aa, bb));

        assertEquals(c.get(), 2 + 8);

    }

    @Test
    public void AVProductSimple() {
        A2Expr.Parameters.setBlockSizes(8, 8);
        Array<Double> a = DoubleArray.make(100);
        Array<Double> b = DoubleArray.make(100);
        Scalar<Double> c = new Scalar<>();

        a.set(i -> 1.0);
        b.set(i -> 1.0);

        int N = 30;

        Array<Double> aa = a.subArray(2, 2 + N); // 1-30
        Array<Double> bb = b.subArray(1, 1 + N); // 2-60 by 2

        c.set(DTIMES.apply(aa, bb));

        assertEquals(c.get(), 30.0);

    }

    @Test
    public void AVProduct() {
        A2Expr.Parameters.setBlockSizes(8, 8);
        Array<Double> a = DoubleArray.make(100);
        Array<Double> b = DoubleArray.make(100);
        Scalar<Double> c = new Scalar<>();

        a.set(i -> i - 1.0);
        b.set(i -> 2.0 * i);

        int N = 30;

        Array<Double> aa = a.subArray(2, 2 + N); // 1-29
        Array<Double> bb = b.subArray(1, 1 + N); // 2-58 by 2

        c.set(DTIMES.apply(aa, bb));

        // expect 2 times sum of first 30 natural numbers
        assertEquals(c.get(), N * (N + 1) * (N + N + 1) / 3.0);
    }

}
