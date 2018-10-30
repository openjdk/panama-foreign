/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.foreign.Libraries;
import java.foreign.Scope;
import java.foreign.memory.DoubleComplex;
import java.foreign.memory.FloatComplex;
import java.lang.invoke.MethodHandles;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.c99.mycomplex;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// FIXME: long double _Complex is not yet supported because long double is not
// yet supported. Fix this test to include long double _Complex when binder
// supports long double.

/*
 * @test
 * @bug 8213013
 * @summary jextract does not handle C99 _Complex type
 * @library ..
 * @run driver JtregJextract -t org.c99 -- mycomplex.h
 * @run testng ComplexTest
 */
public class ComplexTest {
    private static final double TOLERANCE = 0.0001;
    private static final float TOLERANCEF = 0.0001f;

    private mycomplex comlib;

    @BeforeTest
    public void init() {
        comlib = Libraries.bind(MethodHandles.lookup(), mycomplex.class);
    }

    @Test
    public void testDoubleComplex() {
        try (Scope s = Scope.newNativeScope()) {
            // check Euler's identity
            DoubleComplex dc = s.allocateStruct(DoubleComplex.class);
            dc.real$set(0.0);
            dc.imag$set(Math.PI);
            dc = comlib.cexp(dc);
            assertEquals(dc.real$get(), -1.0, TOLERANCE);
            assertEquals(dc.imag$get(), 0.0, TOLERANCE);

            // arg of i is pi/2
            dc.real$set(0.0);
            dc.imag$set(1.0);
            assertEquals(comlib.carg(dc), Math.PI/2.0, TOLERANCE);

            // abs of 1+i is sqrt(2)
            dc.real$set(1.0);
            dc.imag$set(1.0);
            assertEquals(comlib.cabs(dc), Math.sqrt(2.0), TOLERANCE);

            // conjugate of 1+i is 1-i
            dc = comlib.conj(dc);
            assertEquals(comlib.creal(dc), 1.0, TOLERANCE);
            assertEquals(comlib.cimag(dc), -1.0, TOLERANCE);
        }
    }

    @Test
    public void testFloatComplex() {
        try (Scope s = Scope.newNativeScope()) {
            // check Euler's identity
            FloatComplex fc = s.allocateStruct(FloatComplex.class);
            fc.real$set(0.0f);
            fc.imag$set((float)Math.PI);
            fc = comlib.cexpf(fc);
            assertEquals(fc.real$get(), -1.0f, TOLERANCEF);
            assertEquals(fc.imag$get(), 0.0f, TOLERANCEF);

            // arg of i is pi/2
            fc.real$set(0.0f);
            fc.imag$set(1.0f);
            assertEquals(comlib.cargf(fc), (float)Math.PI/2.0, TOLERANCEF);

            // abs of 1+i is sqrt(2)
            fc.real$set(1.0f);
            fc.imag$set(1.0f);
            assertEquals(comlib.cabsf(fc), (float)Math.sqrt(2.0), TOLERANCEF);

            // conjugate of 1+i is 1-i
            fc = comlib.conjf(fc);
            assertEquals(comlib.crealf(fc), 1.0f, TOLERANCEF);
            assertEquals(comlib.cimagf(fc), -1.0f, TOLERANCEF);
        }
    }
}
