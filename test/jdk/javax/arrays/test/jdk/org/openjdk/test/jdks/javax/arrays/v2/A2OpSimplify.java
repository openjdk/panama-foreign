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
import static javax.arrays.v2.A2Expr.COL_DIM;
import static javax.arrays.v2.A2Expr.ROW_DIM;
import javax.arrays.v2.nodes.*;
import static javax.arrays.v2.ops.AnyOp.*;
import javax.arrays.v2.RowMatrix;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.testng.TestNG;
import org.testng.TestListenerAdapter;

/**
 *
 */
public class A2OpSimplify {

    public static void main(String[] args) {
        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] { A2OpSimplify.class });
        testng.addListener(tla);
        testng.run();
    }

    public A2OpSimplify() {
    }

    @Test
    public void conformsOkay() {
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(b, c);
        System.out.println("x = " + x);
        assertTrue(x instanceof MatrixAssociativeBinary, "Expected EltWise2");
    }

    @Test
    public void simplifyTrivial() {
        System.out.println("\nTrivial");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(b, c);
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatrixAssociativeBinary, "Expected EltWise2");
    }

    @Test
    public void simplifyScalarL() {
        System.out.println("\nScalarL");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(1.0, c);
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatScalarLeft, "Expected ScalarLeft");
    }

    @Test
    public void simplifyScalarR() {
        System.out.println("\nScalarR");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(b, 1.0);
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatScalarRight, "Expected ScalarRight");
    }

    @Test
    public void simplifyBinBin2Tri() {
        System.out.println("\nBinBin2Tri");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        Matrix<Double> d = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(b, DPLUS.apply(c,d));
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatrixAssociative3ary, "Expected EltWise3");
    }

    @Test
    public void simplifyBinBinBin2Tern() {
        System.out.println("\nBinBinBin2Tern");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        Matrix<Double> d = new RowMatrix.D(3, 2);
        Matrix<Double> e = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(DPLUS.apply(b,c), DPLUS.apply(d,e));
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatrixAssociative4ary, "Expected EltWise4");
    }

    @Test
    public void simplifyScalarOfBinBinBin2Tern() {
        System.out.println("\nScalarOfBinBinBin2Tern");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        Matrix<Double> d = new RowMatrix.D(3, 2);
        Matrix<Double> e = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(1.0, DPLUS.apply(DPLUS.apply(b,c), DPLUS.apply(d,e)));
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatScalarLeft, "Expected ScalarLeft");
        A2Expr<Double> y = ((MatScalarLeft)s).x;
        assertTrue(y instanceof MatrixTemporary, "Expected Temporary");
        A2Expr<Double> z = ((MatrixTemporary) y).x;
        assertTrue(z instanceof MatrixAssociative4ary, "Expected EltWise4");
    }

    @Test
    public void simplifyBinBinBinScalar2Tri() {
        System.out.println("\nBinBinBinScalar2Tri");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        Matrix<Double> d = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(DPLUS.apply(b,c), DPLUS.apply(d,1.0));
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatrixAssociative3ary, "Expected EltWise3");
    }

    @Test
    public void simplifyBinBinNaBin2Tri() {
        System.out.println("\nBinBinNaBin2Tri");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        Matrix<Double> d = new RowMatrix.D(3, 2);
        Matrix<Double> e = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(DMINUS.apply(b,c), DPLUS.apply(d,e));
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatrixAssociative3ary, "Expected EltWise3");
    }

    @Test
    public void simplifyWrappedBinBinBinNa2Tri() {
        System.out.println("\nBinBinBinNa2Tri");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        Matrix<Double> d = new RowMatrix.D(3, 2);
        Matrix<Double> e = new RowMatrix.D(3, 2);
        A2Expr<Double> x = D_WRAPPED_SIMPLIFIER.apply(DPLUS.apply(DPLUS.apply(b,c), DMINUS.apply(d,e)));
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatrixAssociative3ary, "Expected EltWise3");
    }

    @Test
    public void simplifyBinBinNa2Bin() {
        System.out.println("\nBinBinNa2Bin");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        Matrix<Double> d = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(b, DMINUS.apply(c,d));
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatrixAssociativeBinary, "Expected EltWise2");
    }

    @Test
    public void simplifyBinNaBin2Bin() {
        System.out.println("\nBinNaBin2Bin");
        Matrix<Double> a = new RowMatrix.D(3, 2);
        Matrix<Double> b = new RowMatrix.D(3, 2);
        Matrix<Double> c = new RowMatrix.D(3, 2);
        Matrix<Double> d = new RowMatrix.D(3, 2);
        A2Expr<Double> x = DPLUS.apply(DMINUS.apply(b,c), d);
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatrixAssociativeBinary, "Expected EltWise2");
    }

    @Test
    public void simplifyProdOfPlusPlus() {
        System.out.println("\nProdOfPlusPlus");
        Matrix<Double> a = new RowMatrix.D(3, 3);
        Matrix<Double> b = new RowMatrix.D(3, 3);
        Matrix<Double> c = new RowMatrix.D(3, 3);
        Matrix<Double> d = new RowMatrix.D(3, 3);
        Matrix<Double> e = new RowMatrix.D(3, 3);
        A2Expr<Double> x = DTIMES.apply(DPLUS.apply(b,c), DPLUS.apply(d,e));
        System.out.println("x = " + x);
        A2Expr<Double> s = x.simplify(a);
        System.out.println("s = " + s);
        assertTrue(s instanceof MatrixProduct, "Expected ProductNode");
        A2Expr<Double> sx = ((MatrixProduct)s).x;
        A2Expr<Double> sy = ((MatrixProduct)s).y;
        assertEquals(sx.preferredMajorDim(), ROW_DIM);
        assertEquals(sy.preferredMajorDim(), COL_DIM);

    }


}
