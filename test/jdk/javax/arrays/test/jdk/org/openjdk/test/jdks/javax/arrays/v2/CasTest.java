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

import static javax.arrays.v2.A2Expr.COL_DIM;
import static javax.arrays.v2.A2Expr.ROW_DIM;
import javax.arrays.v2.ColumnMatrix;
import javax.arrays.v2.Matrix;
import javax.arrays.v2.RowMatrix;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class CasTest {

    public CasTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void i() {
        Matrix<Integer> a = RowMatrix.I.make(4,5);
        Matrix<Integer> b = ColumnMatrix.I.make(4,5);
        testI(a);
        testI(b);
        Matrix<Integer> c = a.subMatrix(1,4,3,5);
        testI(c);
        Matrix<Integer> d = b.subMatrix(0,3,2,4);
        testI(d);
        Matrix<Integer> e = c.appendColumns(d);
        testI(e);
        Matrix<Integer> f = c.appendRows(d);
        testI(f);
    }

    @Test
    public void s() {
        Matrix<Short> a = RowMatrix.S.make(4,5);
        Matrix<Short> b = ColumnMatrix.S.make(4,5);
        testS(a);
        testS(b);
    }

   @Test
    public void f() {
        Matrix<Float> a = RowMatrix.F.make(4,5);
        Matrix<Float> b = ColumnMatrix.F.make(4,5);
        testF(a);
        testF(b);
    }

   @Test
    public void d() {
        Matrix<Double> a = RowMatrix.D.make(4,5);
        Matrix<Double> b = ColumnMatrix.D.make(4,5);
        testD(a);
        testD(b);
    }

    @Test
    public void b() {
        Matrix<Byte> a = RowMatrix.B.make(4,5);
        Matrix<Byte> b = ColumnMatrix.B.make(4,5);
        testB(a);
        testB(b);
    }

    void testI(Matrix<Integer> a) {
        a.set((i,j) -> (int) (10*i + j));

        for (int i = 0; i < a.length(ROW_DIM); i++)
            for (int j = 0; j < a.length(COL_DIM); j++) {
                assertEquals(a.cas(i,j,10*i+j,100 + 10*i+j), true);
                assertEquals((int) a.get(i,j), 100 + 10*i+j);
            }
    }

    void testS(Matrix<Short> a) {
        a.set((i,j) -> (short) (10*i + j));
        for (int i = 0; i < a.length(ROW_DIM); i++)
            for (int j = 0; j < a.length(COL_DIM); j++) {
                assertEquals(a.cas(i,j,(short)(10*i+j),(short)(100 + 10*i+j)), true);
                assertEquals((int) a.get(i,j), 100 + 10*i+j);
            }
    }

    void testF(Matrix<Float> a) {
        a.set((i,j) -> (float) (10*i + j));
        for (int i = 0; i < a.length(ROW_DIM); i++)
            for (int j = 0; j < a.length(COL_DIM); j++) {
                assertEquals(a.cas(i,j,(float)(10*i+j),(float)(100 + 10*i+j)), true);
                assertEquals((float) a.get(i,j), (float) 100 + 10*i+j);
            }
    }

    void testD(Matrix<Double> a) {
        a.set((i,j) -> (double) (10*i + j));
        for (int i = 0; i < a.length(ROW_DIM); i++)
            for (int j = 0; j < a.length(COL_DIM); j++) {
                assertEquals(a.cas(i,j,(double)(10*i+j),(double)(100 + 10*i+j)), true);
                assertEquals((double) a.get(i,j), (double) 100 + 10*i+j);
            }
    }

    void testB(Matrix<Byte> a) {
        a.set((i,j) -> (byte) (10*i + j));
        for (int i = 0; i < a.length(ROW_DIM); i++)
            for (int j = 0; j < a.length(COL_DIM); j++) {
                assertEquals(a.cas(i,j,(byte)(10*i+j),(byte)(100 + 10*i+j)), true);
                assertEquals((byte) a.get(i,j), (byte)(100 + 10*i+j));
            }
    }


}
