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

import javax.arrays.tbd.Complex;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

public class ComplexTest {
    /**
     * Allows tests to be run as a simple command-line application,
     * assuming testng on classpath.
     *
     * @param args
     */
    public static void main(String[] args) {
        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] { ComplexTest.class });
        testng.addListener(tla);
        testng.run();
    }

    public ComplexTest() {
    }

    /**
     * Swaps parameters -- compatibility hack from Other Testing Framework.
     * Also does delta for Complex "equality".
     *
     * @param expResult
     * @param result
     */
    static void myAssertEquals(Complex expResult, Complex result, double delta) {
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
     * Test of valueOf method, of class Complex.
     */
    @Test
    public void testValueOf_float_float() {
        System.out.println("valueOf");
        float re = 1.0F;
        float im = -1.0F;
        Complex result = Complex.valueOf(re, im);
        myAssertEquals(result.re, 1.0F, 0.0);
        myAssertEquals(result.im, -1.0F, 0.0);
    }

    /**
     * Test of valueOf method, of class Complex.
     */
    @Test
    public void testValueOf_double_double() {
        System.out.println("valueOf");
        double re = 1.0;
        double im = -1.0;
        Complex result = Complex.valueOf(re, im);
        myAssertEquals(result.re, 1.0F, 0.0);
        myAssertEquals(result.im, -1.0F, 0.0);
    }

    /**
     * Test of valueOf method, of class Complex.
     */
    @Test
    public void testValueOf_float() {
        System.out.println("valueOf");
        float re = 1.0F;
        Complex result = Complex.valueOf(re);
        myAssertEquals(result.re, 1.0F, 0.0);
        myAssertEquals(result.im, 0.0F, 0.0);
    }

    /**
     * Test of valueOf method, of class Complex.
     */
    @Test
    public void testValueOf_double() {
        System.out.println("valueOf");
        double re = 1.0;
        Complex result = Complex.valueOf(re);
        myAssertEquals(result.re, 1.0F, 0.0);
        myAssertEquals(result.im, 0.0F, 0.0);
    }

    /**
     * Test of plus method, of class Complex.
     */
    @Test
    public void testPlus() {
        System.out.println("plus");
        Complex other = Complex.valueOf(1, 2);
        Complex instance = Complex.valueOf(3, 4);
        Complex expResult = Complex.valueOf(4, 6);
        Complex result = instance.plus(other);
        myAssertEquals(expResult, result);
    }

    /**
     * Test of minus method, of class Complex.
     */
    @Test
    public void testMinus() {
        System.out.println("minus");
        Complex other = Complex.valueOf(1, 2);
        Complex instance = Complex.valueOf(-3, -4);
        Complex expResult = Complex.valueOf(-4, -6);
        Complex result = instance.minus(other);
        myAssertEquals(expResult, result);
    }

    /**
     * Test of times method, of class Complex.
     */
    @Test
    public void testTimes_Complex() {
        System.out.println("times");
        Complex other = Complex.valueOf(1, 2);
        Complex instance = Complex.valueOf(3, 4);
        Complex expResult = Complex.valueOf(1*3-2*4, 2*3+1*4);
        Complex result = instance.times(other);
        myAssertEquals(expResult, result);
    }

    /**
     * Test of times method, of class Complex.
     */
    @Test
    public void testTimes_double() {
        System.out.println("times");
        double other = 2.0;
        Complex instance = Complex.valueOf(3, 4);
        Complex expResult = Complex.valueOf(6, 8);
        Complex result = instance.times(other);
        myAssertEquals(expResult, result);
    }

    /**
     * Test of divided method, of class Complex.
     */
    @Test
    public void testDivided() {
        System.out.println("divided");
        Complex other = Complex.valueOf(3, 4);
        Complex instance = Complex.valueOf(1*3-2*4, 2*3+1*4);
        Complex expResult = Complex.valueOf(1, 2);
        Complex result = instance.divided(other);
        myAssertEquals(expResult, result);
    }

    /**
     * Test of conjugate method, of class Complex.
     */
    @Test
    public void testConjugate() {
        System.out.println("conjugate");
        Complex instance = Complex.valueOf(3, 4);
        Complex expResult = Complex.valueOf(3, -4);
        Complex result = instance.conjugate();
        myAssertEquals(expResult, result);
    }

    /**
     * Test of abs method, of class Complex.
     */
    @Test
    public void testAbs() {
        System.out.println("abs");
        Complex instance = Complex.valueOf(3, 4);
        float expResult = 5.0F;
        float result = instance.abs();
        myAssertEquals(expResult, result, 0.0);
    }

    /**
     * Test of dabs method, of class Complex.
     */
    @Test
    public void testDabs() {
        System.out.println("dabs");
        Complex instance = Complex.valueOf(3, 4);
        double expResult = 5.0F;
        double result = instance.dabs();
        myAssertEquals(expResult, result, 0.0);
    }

    /**
     * Test of angle method, of class Complex.
     */
    @Test
    public void testAngle() {
        System.out.println("angle");
        Complex instance = Complex.valueOf(1,1);
        float expResult = (float) Math.PI/4;
        float result = instance.angle();
        myAssertEquals(expResult, result, 0.0);
    }

    /**
     * Test of sqrt method, of class Complex.
     */
    @Test
    public void testSqrt() {
        System.out.println("sqrt");
        Complex instance = Complex.valueOf(-4,0);
        Complex expResult = Complex.valueOf(0,2);
        Complex result = instance.sqrt();
        myAssertEquals(expResult, result);
    }

    /**
     * Test of log method, of class Complex.
     */
    @Test
    public void testLog() {
        System.out.println("log");
        Complex instance = Complex.valueOf(-Math.E,0);
        Complex expResult = Complex.valueOf(1,Math.PI);
        Complex result = instance.log();
        myAssertEquals(expResult, result, 1E-6);
    }

    /**
     * Test of exp method, of class Complex.
     */
    @Test
    public void testExp() {
        System.out.println("exp");
        Complex instance = Complex.valueOf(1,Math.PI);
        Complex expResult = Complex.valueOf(-Math.E,0);
        Complex result = instance.exp();
        myAssertEquals(expResult, result, 1E-6);
    }

    /**
     * Test of pow method, of class Complex.
     */
    @Test
    public void testPow_double() {
        System.out.println("pow");
        double p = 4.0;
        Complex instance = Complex.valueOf(1,1);
        Complex expResult = Complex.valueOf(-4,0);
        Complex result = instance.pow(p);
        myAssertEquals(expResult, result, 1E-6);
    }

    /**
     * Test of pow method, of class Complex.
     */
    @Test
    public void testPow_ComplexA() {
        System.out.println("powA");
        Complex p = Complex.valueOf(4,0);
        Complex instance = Complex.valueOf(1,1);
        Complex expResult = Complex.valueOf(-4,0);
        Complex result = instance.pow(p);
        myAssertEquals(expResult, result, 1E-6);
    }

    @Test
    public void testPow_ComplexB() {
        System.out.println("powB");
        Complex p = Complex.valueOf(0,Math.PI);
        Complex instance = Complex.valueOf(Math.E, 0);
        Complex expResult = Complex.valueOf(-1,0);
        Complex result = instance.pow(p);
        myAssertEquals(expResult, result, 1E-6);
    }

    /**
     * Test of toString method, of class Complex.
     */
    @Test
    public void testToStringA() {
        System.out.println("toString++");
        Complex instance = Complex.valueOf(1,1);
        String expResult = "1.0+1.0i";
        String result = instance.toString();
        myAssertEquals(expResult, result);
     }

    @Test
    public void testToStringB() {
        System.out.println("toString+-");
        Complex instance = Complex.valueOf(1,-1);
        String expResult = "1.0-1.0i";
        String result = instance.toString();
        myAssertEquals(expResult, result);
     }

    @Test
    public void testToStringC() {
        System.out.println("toString-+");
        Complex instance = Complex.valueOf(-1,1);
        String expResult = "-1.0+1.0i";
        String result = instance.toString();
        myAssertEquals(expResult, result);
     }

    @Test
    public void testToStringD() {
        System.out.println("toString--");
        Complex instance = Complex.valueOf(-1,-1);
        String expResult = "-1.0-1.0i";
        String result = instance.toString();
        myAssertEquals(expResult, result);
     }

}
