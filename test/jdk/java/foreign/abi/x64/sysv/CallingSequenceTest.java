/*
 * Copyright (c) 2015, 2019, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @library ../..
 * @modules jdk.incubator.foreign/jdk.internal.foreign
 *          jdk.incubator.foreign/jdk.internal.foreign.abi
 *          jdk.incubator.foreign/jdk.internal.foreign.abi.x64
 *          jdk.incubator.foreign/jdk.internal.foreign.abi.x64.sysv
 *          java.base/sun.security.action
 * @run testng CallingSequenceTest
 */

import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemoryLayout;

import jdk.internal.foreign.abi.x64.sysv.CallingSequenceBuilderImpl;
import jdk.internal.foreign.abi.x64.sysv.SysVx64ABI;

import org.testng.annotations.Test;

import static jdk.internal.foreign.abi.StorageClass.*;
import static jdk.incubator.foreign.MemoryLayouts.SysV.*;

public class CallingSequenceTest extends CallingSequenceTestBase {

    @Test
    public void testInteger() {
        testInteger(CallingSequenceBuilderImpl::new,
                    SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS,
                    C_LONGLONG);
    }

    @Test
    public void testSse() {
        testVector(CallingSequenceBuilderImpl::new,
                   SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS,
                   C_FLOAT);
    }

    @Test
    public void testMixed() {
        new Verifier(new CallingSequenceBuilderImpl(null))
                .args(SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS,
                        C_LONGLONG,
                        binding(INTEGER_ARGUMENT_REGISTER, 0))
                .args(2, C_LONGLONG,
                        binding(STACK_ARGUMENT_SLOT, 0))
                .args(SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS,
                        C_FLOAT,
                        binding(VECTOR_ARGUMENT_REGISTER, 0))
                .args(2, C_FLOAT,
                        binding(STACK_ARGUMENT_SLOT, 0))
                .check(false);
    }

    /**
     * This is the example from the System V ABI AMD64 document
     *
     * struct structparm {
     *   int32_t a, int32_t b, double d;
     * } s;
     * int32_t e, f, g, h, i, j, k;
     * long double ld;
     * double m, n;
     *
     * void m(e, f, s, g, h, ld, m, n, i, j, k);
     *
     * m(s);
     */
    @Test
    public void testAbiExample() {
        new Verifier(new CallingSequenceBuilderImpl(null))
                .args(2, C_INT,
                        binding(INTEGER_ARGUMENT_REGISTER, 0))
                .arg(MemoryLayout.ofStruct(C_INT, C_INT, C_DOUBLE),
                        binding(INTEGER_ARGUMENT_REGISTER, 0), // s.a, s.b
                        binding(VECTOR_ARGUMENT_REGISTER, 8)) // s.d
                .args(2, C_INT,
                        binding(INTEGER_ARGUMENT_REGISTER, 0))
                .arg(C_LONGDOUBLE,
                        binding(STACK_ARGUMENT_SLOT, 0),
                        binding(STACK_ARGUMENT_SLOT, 8))
                .args(2, C_DOUBLE,
                        binding(VECTOR_ARGUMENT_REGISTER, 0))
                .arg(C_INT,
                        binding(INTEGER_ARGUMENT_REGISTER, 0))
                .args(2, C_INT,
                        binding(STACK_ARGUMENT_SLOT, 0))
                .check(false);
    }

    /**
     * This is a varargs example from the System V ABI AMD64 document
     *
     * int a, b;
     * long double ld;
     * double m, n;
     * __m256 u, y;
     *
     * extern void func (int a, double m, __m256 u, ...);
     *
     * func(a, m, u, b, ld, y, n);
     */
    @Test
    public void testAbiExampleVarargs() {
        new Verifier(new CallingSequenceBuilderImpl(null))
                .arg(C_INT, binding(INTEGER_ARGUMENT_REGISTER, 0))
                .arg(C_DOUBLE, binding(VECTOR_ARGUMENT_REGISTER, 0))
                .arg(C_INT, binding(INTEGER_ARGUMENT_REGISTER, 0))
                .arg(C_LONGDOUBLE,
                        binding(STACK_ARGUMENT_SLOT, 0),
                        binding(STACK_ARGUMENT_SLOT, 8))
                .arg(C_DOUBLE, binding(VECTOR_ARGUMENT_REGISTER, 0))
                .check(false);
    }


    /**
     * struct s {
     *   uint64_t u0;
     * } s;
     *
     * void m(struct s s);
     *
     * m(s);
     */
    @Test
    public void testStruct8() {
        new Verifier(new CallingSequenceBuilderImpl(null))
                .arg(MemoryLayout.ofStruct(C_ULONGLONG),
                        binding(INTEGER_ARGUMENT_REGISTER, 0))
                .check(false);
    }

    /**
     * struct s {
     *   uint64_t u0, u1;
     * } s;
     *
     * void m(struct s s);
     *
     * m(s);
     */
    @Test
    public void testStruct16() {
        new Verifier(new CallingSequenceBuilderImpl(null))
                .arg(MemoryLayout.ofStruct(C_ULONGLONG, C_ULONGLONG),
                        binding(INTEGER_ARGUMENT_REGISTER, 0),
                        binding(INTEGER_ARGUMENT_REGISTER, 8))
                .check(false);
    }

    /**
     * struct s {
     *   uint64_t u0, u1, u2;
     * } s;
     *
     * void m(struct s s);
     *
     * m(s);
     */
    @Test
    public void testStruct24() {
        new Verifier(new CallingSequenceBuilderImpl(null))
                .arg(MemoryLayout.ofStruct(C_ULONGLONG, C_ULONGLONG, C_ULONGLONG),
                        binding(STACK_ARGUMENT_SLOT, 0),
                        binding(STACK_ARGUMENT_SLOT, 8),
                        binding(STACK_ARGUMENT_SLOT, 16))
                .check(false);
    }

    /**
     * struct s {
     *   uint64_t u0, u1, u2, u3;
     * } s;
     *
     * void m(struct s s);
     *
     * m(s);
     */
    @Test
    public void testStruct32() {
        new Verifier(new CallingSequenceBuilderImpl(null))
                .arg(MemoryLayout.ofStruct(C_ULONGLONG, C_ULONGLONG, C_ULONGLONG, C_ULONGLONG),
                        binding(STACK_ARGUMENT_SLOT, 0),
                        binding(STACK_ARGUMENT_SLOT, 8),
                        binding(STACK_ARGUMENT_SLOT, 16),
                        binding(STACK_ARGUMENT_SLOT, 24))
                .check(false);
    }

    /**
     * typedef void (*f)(void);
     *
     * void m(f f);
     * void f_impl(void);
     *
     * m(f_impl);
     */
    @Test
    public void testFunctionType() {
        new Verifier(new CallingSequenceBuilderImpl(null))
                .arg(C_POINTER, binding(INTEGER_ARGUMENT_REGISTER, 0))
                .check(false);
    }

    /**
     * void f(int64_t l0, float f0, __m256 m0);
     */
    @Test
    public void testMixedArgs() {
        new Verifier(new CallingSequenceBuilderImpl(null))
                .arg(C_LONGLONG, binding(INTEGER_ARGUMENT_REGISTER, 0))
                .arg(C_FLOAT, binding(VECTOR_ARGUMENT_REGISTER, 0))
                .check(false);
    }

    /**
     * struct s {
     *    int64_t l0;
     *    int64_t l1;
     * };
     *
     * void f(struct s s1);
     */
    @Test
    public void testIntegerStruct() {
        new Verifier(new CallingSequenceBuilderImpl(null))
            .arg(MemoryLayout.ofStruct(C_LONGLONG, C_LONGLONG),
                        binding(INTEGER_ARGUMENT_REGISTER, 0),
                        binding(INTEGER_ARGUMENT_REGISTER, 8))
                .check(false);
    }

}
