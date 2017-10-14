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

package org.openjdk.tests.javax.arrays.tbd;

import javax.arrays.tbd.DComplex;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

public class DComplexTest {
    /**
     * Allows tests to be run as a simple command-line application,
     * assuming testng on classpath.
     *
     * @param args
     */
    public static void main(String[] args) {
        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] { DComplexTest.class });
        testng.addListener(tla);
        testng.run();
    }

    public DComplexTest() {
    }

    /**
     * Swaps parameters -- compatibility hack from Other Testing Framework.
     * Also does delta for DComplex "equality".
     *
     * @param expResult
     * @param result
     */
    static void myAssertEquals(DComplex expResult, DComplex result, double delta) {
        double diff = expResult.minus(result).dabs();
        if (diff > delta) {
            fail("expected:<" + expResult+ "> but was <" + result+ ">");
        }
    }

    /**
     * Swaps parameters -- compatibility hack from Other Testing Framework.
     * @param expResult
     * @param result
     */
    static void myAssertEquals(double expResult, double result, double delta) {
        double diff = Math.abs(expResult - result);
        if (diff > delta) {
            fail("expected:<" + expResult+ "> but was <" + result+ ">");
        }
    }

    /**
     * Swaps parameters -- compatibility hack from Other Testing Framework.
     * @param expResult
     * @param result
     */
    static void myAssertEquals(Object expResult, Object result) {
        assertEquals(result, expResult);
    }

    /**
     * Test of valueOf method, of class DComplex.
     */
    @Test
    public void testValueOf_double_double() {
        System.out.println("valueOf");
        double re = 1.0;
        double im = -1.0;
        DComplex result = DComplex.valueOf(re, im);
        myAssertEquals(result.re, 1.0, 0.0);
        myAssertEquals(result.im, -1.0, 0.0);
    }

    /**
     * Test of valueOf method, of class DComplex.
     */
    @Test
    public void testValueOf_double() {
        System.out.println("valueOf");
        double re = 1.0;
        DComplex result = DComplex.valueOf(re);
        myAssertEquals(result.re, 1.0, 0.0);
        myAssertEquals(result.im, 0.0, 0.0);
    }

    /**
     * Test of plus method, of class DComplex.
     */
    @Test
    public void testPlus() {
        System.out.println("plus");
        DComplex other = DComplex.valueOf(1, 2);
        DComplex instance = DComplex.valueOf(3, 4);
        DComplex expResult = DComplex.valueOf(4, 6);
        DComplex result = instance.plus(other);
        myAssertEquals(expResult, result);
    }

    /**
     * Test of minus method, of class DComplex.
     */
    @Test
    public void testMinus() {
        System.out.println("minus");
        DComplex other = DComplex.valueOf(1, 2);
        DComplex instance = DComplex.valueOf(-3, -4);
        DComplex expResult = DComplex.valueOf(-4, -6);
        DComplex result = instance.minus(other);
        myAssertEquals(expResult, result);
    }

    /**
     * Test of times method, of class DComplex.
     */
    @Test
    public void testTimes_DComplex() {
        System.out.println("times");
        DComplex other = DComplex.valueOf(1, 2);
        DComplex instance = DComplex.valueOf(3, 4);
        DComplex expResult = DComplex.valueOf(1*3-2*4, 2*3+1*4);
        DComplex result = instance.times(other);
        myAssertEquals(expResult, result);
    }

    /**
     * Test of times method, of class DComplex.
     */
    @Test
    public void testTimes_double() {
        System.out.println("times");
        double other = 2.0;
        DComplex instance = DComplex.valueOf(3, 4);
        DComplex expResult = DComplex.valueOf(6, 8);
        DComplex result = instance.times(other);
        myAssertEquals(expResult, result);
    }

    /**
     * Test of divided method, of class DComplex.
     */
    @Test
    public void testDivided() {
        System.out.println("divided");
        DComplex other = DComplex.valueOf(3, 4);
        DComplex instance = DComplex.valueOf(1*3-2*4, 2*3+1*4);
        DComplex expResult = DComplex.valueOf(1, 2);
        DComplex result = instance.divided(other);
        myAssertEquals(expResult, result);
    }

    /**
     * Test of conjugate method, of class DComplex.
     */
    @Test
    public void testConjugate() {
        System.out.println("conjugate");
        DComplex instance = DComplex.valueOf(3, 4);
        DComplex expResult = DComplex.valueOf(3, -4);
        DComplex result = instance.conjugate();
        myAssertEquals(expResult, result);
    }

    /**
     * Test of abs method, of class DComplex.
     */
    @Test
    public void testAbs() {
        System.out.println("abs");
        DComplex instance = DComplex.valueOf(3, 4);
        double expResult = 5.0;
        double result = instance.abs();
        myAssertEquals(expResult, result, 0.0);
    }

    /**
     * Test of dabs method, of class DComplex.
     */
    @Test
    public void testDabs() {
        System.out.println("dabs");
        DComplex instance = DComplex.valueOf(3, 4);
        double expResult = 5.0;
        double result = instance.dabs();
        myAssertEquals(expResult, result, 0.0);
    }

    /**
     * Test of angle method, of class DComplex.
     */
    @Test
    public void testAngle() {
        System.out.println("angle");
        DComplex instance = DComplex.valueOf(1,1);
        double expResult = Math.PI/4;
        double result = instance.angle();
        myAssertEquals(expResult, result, 0.0);
    }

    /**
     * Test of sqrt method, of class DComplex.
     */
    @Test
    public void testSqrt() {
        System.out.println("sqrt");
        DComplex instance = DComplex.valueOf(-4,0);
        DComplex expResult = DComplex.valueOf(0,2);
        DComplex result = instance.sqrt();
        myAssertEquals(expResult, result);
    }

    /**
     * Test of log method, of class DComplex.
     */
    @Test
    public void testLog() {
        System.out.println("log");
        DComplex instance = DComplex.valueOf(-Math.E,0);
        DComplex expResult = DComplex.valueOf(1,Math.PI);
        DComplex result = instance.log();
        myAssertEquals(expResult, result, 1E-12);
    }

    /**
     * Test of exp method, of class DComplex.
     */
    @Test
    public void testExp() {
        System.out.println("exp");
        DComplex instance = DComplex.valueOf(1,Math.PI);
        DComplex expResult = DComplex.valueOf(-Math.E,0);
        DComplex result = instance.exp();
        myAssertEquals(expResult, result, 1E-12);
    }

    /**
     * Test of pow method, of class DComplex.
     */
    @Test
    public void testPow_double() {
        System.out.println("pow");
        double p = 4.0;
        DComplex instance = DComplex.valueOf(1,1);
        DComplex expResult = DComplex.valueOf(-4,0);
        DComplex result = instance.pow(p);
        myAssertEquals(expResult, result, 1E-12);
    }

    /**
     * Test of pow method, of class DComplex.
     */
    @Test
    public void testPow_DComplexA() {
        System.out.println("powA");
        DComplex p = DComplex.valueOf(4,0);
        DComplex instance = DComplex.valueOf(1,1);
        DComplex expResult = DComplex.valueOf(-4,0);
        DComplex result = instance.pow(p);
        myAssertEquals(expResult, result, 1E-12);
    }

    @Test
    public void testPow_DComplexB() {
        System.out.println("powB");
        DComplex p = DComplex.valueOf(0,Math.PI);
        DComplex instance = DComplex.valueOf(Math.E, 0);
        DComplex expResult = DComplex.valueOf(-1,0);
        DComplex result = instance.pow(p);
        myAssertEquals(expResult, result, 1E-12);
    }

    /**
     * Test of toString method, of class DComplex.
     */
    @Test
    public void testToStringA() {
        System.out.println("toString++");
        DComplex instance = DComplex.valueOf(1,1);
        String expResult = "1.0+1.0i";
        String result = instance.toString();
        myAssertEquals(expResult, result);
     }

    @Test
    public void testToStringB() {
        System.out.println("toString+-");
        DComplex instance = DComplex.valueOf(1,-1);
        String expResult = "1.0-1.0i";
        String result = instance.toString();
        myAssertEquals(expResult, result);
     }

    @Test
    public void testToStringC() {
        System.out.println("toString-+");
        DComplex instance = DComplex.valueOf(-1,1);
        String expResult = "-1.0+1.0i";
        String result = instance.toString();
        myAssertEquals(expResult, result);
     }

    @Test
    public void testToStringD() {
        System.out.println("toString--");
        DComplex instance = DComplex.valueOf(-1,-1);
        String expResult = "-1.0-1.0i";
        String result = instance.toString();
        myAssertEquals(expResult, result);
     }

}
