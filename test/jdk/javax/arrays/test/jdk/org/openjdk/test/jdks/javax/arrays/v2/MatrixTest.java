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

import javax.arrays.tbd.Complex;
import javax.arrays.tbd.DComplex;
import javax.arrays.v2.Matrix;
import javax.arrays.v2.RowMatrix;
import javax.arrays.v2.ColumnMatrix;
import static org.testng.Assert.*;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

/**
 * A suite of tests for row and column major matrices, across all the
 * different types for which those two layouts have been specialized.
 */
public class MatrixTest {

    @Test(enabled=false)
    public static void main(String[] args) {
        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] { MatrixTest.class });
        testng.addListener(tla);
        testng.run();
    }
    
    public MatrixTest() {
    }

    @Test
    public void testRowAllocFillGetSetGet() {
        Matrix<String> a = new RowMatrix<>(String.class, 3, 2);
        tailString(a);
    }
    @Test
    public void testColumnAllocFillGetSetGet() {
        Matrix<String> a = new ColumnMatrix<>(String.class, 3, 2);
        tailString(a);
    }

    void tailString(Matrix<String> a) {
        a.set((i,j) -> "("+i+","+j+")");
        assertEquals(a.get(2,1), "(2,1)");
        a.set(2,1,"cat");
        assertEquals(a.get(2,1), "cat");
    }

    @Test
    public void testDRowAllocFillGetSetGet() {
        Matrix<Double> a = new RowMatrix.D(3, 2);
        tailD(a);
    }

    @Test
    public void testDColumnAllocFillGetSetGet() {
        Matrix<Double> a = new ColumnMatrix.D(3, 2);
        tailD(a);
    }

    void tailD(Matrix<Double> a) {
        a.set((i,j) -> 100.0*i + j);
        assertEquals(a.get(2,1), 201.0);
        a.set(2,1,666.0);
        assertEquals(a.get(2,1), 666.0);
    }

    @Test
    public void testFRowAllocFillGetSetGet() {
        Matrix<Float> a = new RowMatrix.F(3, 2);
        tailF(a);
    }

    @Test
    public void testFColumnAllocFillGetSetGet() {
        Matrix<Float> a = new ColumnMatrix.F(3, 2);
        tailF(a);
    }

    void tailF(Matrix<Float> a) {
        a.set((i,j) -> (float)(100.0*i + j));
        assertEquals(a.get(2,1), 201.0F);
        a.set(2,1,666.0F);
        assertEquals(a.get(2,1), 666.0F);
    }

    @Test
    public void testLRowAllocFillGetSetGet() {
        Matrix<Long> a = new RowMatrix.L(3, 2);
        tailL(a);
    }

    @Test
    public void testLColumnAllocFillGetSetGet() {
        Matrix<Long> a = new ColumnMatrix.L(3, 2);
        tailL(a);
    }

    void tailL(Matrix<Long> a) {
        a.set((i,j) -> 100*i + j);
        assertEquals((long)a.get(2,1), 201L);
        a.set(2,1,666L);
        assertEquals((long)a.get(2,1), 666L);
    }

    @Test
    public void testIRowAllocFillGetSetGet() {
        Matrix<Integer> a = new RowMatrix.I(3, 2);
        tailI(a);
    }

    @Test
    public void testIColumnAllocFillGetSetGet() {
        Matrix<Integer> a = new ColumnMatrix.I(3, 2);
        tailI(a);
    }

    void tailI(Matrix<Integer> a) {
        a.set((i,j) -> (int)(100*i + j));
        assertEquals((int)a.get(2,1), 201);
        a.set(2,1,666);
        assertEquals((int)a.get(2,1), 666);
    }

    @Test
    public void testSRowAllocFillGetSetGet() {
        Matrix<Short> a = new RowMatrix.S(3, 2);
        tailS(a);
    }

    @Test
    public void testSColumnAllocFillGetSetGet() {
        Matrix<Short> a = new ColumnMatrix.S(3, 2);
        tailS(a);
    }

    void tailS(Matrix<Short> a) {
        a.set((i,j) -> (short)(100*i + j));
        assertEquals((short)a.get(2,1), (short)201);
        a.set(2,1,(short)666);
        assertEquals((short)a.get(2,1), (short)666);
    }

    @Test
    public void testBRowAllocFillGetSetGet() {
        Matrix<Byte> a = new RowMatrix.B(3, 2);
        tailB(a);
    }

    @Test
    public void testBColumnAllocFillGetSetGet() {
        Matrix<Byte> a = new ColumnMatrix.B(3, 2);
        tailB(a);
    }

    void tailB(Matrix<Byte> a) {
        a.set((i,j) -> (byte)(10*i + j));
        assertEquals((byte)a.get(2,1), (byte)21);
        a.set(2,1,(byte)66);
        assertEquals((byte)a.get(2,1), (byte)66);
    }

    @Test
    public void testCXinLRowAllocFillGetSetGet() {
        Matrix<Complex> a = new RowMatrix.CXinL(3, 2);
        tailCX(a);
    }

    @Test
    public void testCXinLColumnAllocFillGetSetGet() {
        Matrix<Complex> a = new ColumnMatrix.CXinL(3, 2);
        tailCX(a);
    }

    @Test
    public void testCXinFFRowAllocFillGetSetGet() {
        Matrix<Complex> a = new RowMatrix.CXinFF(3, 2);
        tailCX(a);
    }

    @Test
    public void testCXinFFColumnAllocFillGetSetGet() {
        Matrix<Complex> a = new ColumnMatrix.CXinFF(3, 2);
        tailCX(a);
    }

    void tailCX(Matrix<Complex> a) {
        a.set((i,j) -> Complex.valueOf(i,j));
        assertEquals(a.get(2,1), Complex.valueOf(2,1));
        a.set(2,1,Complex.valueOf(6,6));
        assertEquals(a.get(2,1), Complex.valueOf(6,6));
    }

    @Test
    public void testDXRowAllocFillGetSetGet() {
        Matrix<DComplex> a = new RowMatrix.DX(3, 2);
        tailDX(a);
    }

    @Test
    public void testDXColumnAllocFillGetSetGet() {
        Matrix<DComplex> a = new ColumnMatrix.DX(3, 2);
        tailDX(a);
    }

    void tailDX(Matrix<DComplex> a) {
        a.set((i,j) -> DComplex.valueOf(i,j));
        assertEquals(a.get(2,1), DComplex.valueOf(2,1));
        a.set(2,1,DComplex.valueOf(6,6));
        assertEquals(a.get(2,1), DComplex.valueOf(6,6));
    }
}
