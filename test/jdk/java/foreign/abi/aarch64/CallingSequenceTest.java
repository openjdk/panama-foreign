/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019, Arm Limited. All rights reserved.
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

/**
 * @test
 * @library ..
 * @modules jdk.incubator.foreign/jdk.internal.foreign
 *          jdk.incubator.foreign/jdk.internal.foreign.abi
 *          jdk.incubator.foreign/jdk.internal.foreign.abi.aarch64
 * @run testng CallingSequenceTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemoryLayout;

import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.StorageClass;
import jdk.internal.foreign.abi.aarch64.CallingSequenceBuilderImpl;
import jdk.internal.foreign.abi.aarch64.AArch64ABI;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import static jdk.incubator.foreign.MemoryLayouts.AArch64ABI.*;
import static jdk.internal.foreign.abi.StorageClass.*;

public class CallingSequenceTest extends CallingSequenceTestBase {

    @Test
    public void testInteger() {
        testInteger(CallingSequenceBuilderImpl::new,
                    AArch64ABI.MAX_INTEGER_ARGUMENT_REGISTERS,
                    C_LONGLONG);
    }

    @Test
    public void testVector() {
        testVector(CallingSequenceBuilderImpl::new,
                   AArch64ABI.MAX_VECTOR_ARGUMENT_REGISTERS,
                   C_FLOAT);
    }

    @Test
    public void testTwoIntTwoFloat() {
        new Verifier(new CallingSequenceBuilderImpl(null))
            .args(2, C_INT, binding(INTEGER_ARGUMENT_REGISTER, 0))
            .args(2, C_FLOAT, binding(VECTOR_ARGUMENT_REGISTER, 0))
            .check(false);
    }

    @Test
    public void testStruct1() {
        // struct s { int32_t a, b; double c; };
        // Passed in two integer registers
        new Verifier(new CallingSequenceBuilderImpl(null))
            .arg(MemoryLayout.ofStruct(C_INT, C_INT, C_DOUBLE),
                 binding(INTEGER_ARGUMENT_REGISTER, 0),
                 binding(INTEGER_ARGUMENT_REGISTER, 8))
            .check(false);
    }

    @Test
    public void testStruct2() {
        // struct s { int32_t a, b; double c; int32_t d };
        // Passed by pointer
        new Verifier(new CallingSequenceBuilderImpl(null))
            .arg(MemoryLayout.ofStruct(C_INT, C_INT, C_DOUBLE, C_INT),
                 binding(INTEGER_ARGUMENT_REGISTER, 0))
            .check(false);
    }

    @Test
    public void testStruct3() {
        // struct s { int32_t a, b; double c; int32_t d; };
        // struct t { int64_t a, b, c; };
        new Verifier(new CallingSequenceBuilderImpl(null))
            .arg(MemoryLayout.ofStruct(C_INT, C_INT, C_DOUBLE, C_INT),
                 binding(INTEGER_ARGUMENT_REGISTER, 0))
            .arg(MemoryLayout.ofStruct(C_LONGLONG, C_LONGLONG, C_LONGLONG),
                 binding(INTEGER_ARGUMENT_REGISTER, 0))
            .arg(C_INT, binding(INTEGER_ARGUMENT_REGISTER, 0))
            .check(false);
    }

    @Test
    public void testArray1() {
        // struct s { int32_t a[2]; float b[2] };
        new Verifier(new CallingSequenceBuilderImpl(null))
            .arg(MemoryLayout.ofStruct(MemoryLayout.ofSequence(2, C_INT),
                                       MemoryLayout.ofSequence(2, C_FLOAT)),
                 binding(INTEGER_ARGUMENT_REGISTER, 0),
                 binding(INTEGER_ARGUMENT_REGISTER, 8))
            .check(false);
    }

    @Test
    public void testArray2() {
        // Array of integers passed in integer registers
        new Verifier(new CallingSequenceBuilderImpl(null))
            .arg(MemoryLayout.ofSequence(4, C_INT),
                 binding(INTEGER_ARGUMENT_REGISTER, 0),
                 binding(INTEGER_ARGUMENT_REGISTER, 8))
            .check(false);
    }

    @Test
    public void testReturnStruct1() {
        // Pointer to temporary storage for result should be passed in
        // the indirect result register
        MemoryLayout resultLayout =
            MemoryLayout.ofStruct(C_LONGLONG, C_LONGLONG, C_FLOAT);

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(resultLayout);
        CallingSequence recipe = sc.build();

        var intReturns = recipe.bindings(StorageClass.INTEGER_RETURN_REGISTER);
        var indirectResult = recipe.bindings(StorageClass.INDIRECT_RESULT_REGISTER);

        assertTrue(recipe.returnsInMemory());
        assertEquals(intReturns.size(), 1);  // XXX: why?
        assertEquals(indirectResult.size(), 1);

        assertEquals(indirectResult.get(0).argument().layout(), C_POINTER);
        assertEquals(indirectResult.get(0).argument().argumentIndex(), -1);
    }

    @Test
    public void testReturnStruct2() {
        // If the size of the returned struct is <= 16 bytes then it
        // should be returned in integer registers
        MemoryLayout resultLayout =
            MemoryLayout.ofStruct(C_LONGLONG, C_LONGLONG);

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(resultLayout);
        CallingSequence recipe = sc.build();

        var intReturns = recipe.bindings(StorageClass.INTEGER_RETURN_REGISTER);
        var indirectResult = recipe.bindings(StorageClass.INDIRECT_RESULT_REGISTER);

        assertFalse(recipe.returnsInMemory());
        assertEquals(intReturns.size(), 2);
        assertEquals(indirectResult.size(), 0);

        assertEquals(intReturns.get(0).argument().layout(), resultLayout);
        assertEquals(intReturns.get(0).offset(), 0);

        assertEquals(intReturns.get(1).argument().layout(), resultLayout);
        assertEquals(intReturns.get(1).offset(), 8);
    }

    @Test
    public void testStructHFA1() {
        MemoryLayout structHFA = MemoryLayout.ofStruct(C_FLOAT, C_FLOAT);
        MemoryLayout[] args = { C_FLOAT, C_INT, structHFA };

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(structHFA);
        Stream.of(args).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var intReturns = recipe.bindings(StorageClass.INTEGER_RETURN_REGISTER);
        var vecReturns = recipe.bindings(StorageClass.VECTOR_RETURN_REGISTER);
        var indirectResult = recipe.bindings(StorageClass.INDIRECT_RESULT_REGISTER);

        assertFalse(recipe.returnsInMemory());
        assertEquals(intArgs.size(), 1);
        assertEquals(vecArgs.size(), 3);
        assertEquals(intReturns.size(), 0);
        assertEquals(vecReturns.size(), 2);
        assertEquals(indirectResult.size(), 0);

        assertEquals(intArgs.get(0).argument().layout(), args[1]);
        assertEquals(intArgs.get(0).offset(), 0);

        assertEquals(vecArgs.get(0).argument().layout(), args[0]);
        assertEquals(vecArgs.get(0).offset(), 0);

        assertEquals(vecArgs.get(1).argument().layout(), args[2]);
        assertEquals(vecArgs.get(1).offset(), 0);

        assertEquals(vecArgs.get(2).argument().layout(), args[2]);
        assertEquals(vecArgs.get(2).offset(), 4);

        assertEquals(vecReturns.get(0).argument().layout(), structHFA);
        assertEquals(vecReturns.get(0).offset(), 0);

        assertEquals(vecReturns.get(1).argument().layout(), structHFA);
        assertEquals(vecReturns.get(1).offset(), 4);
    }

    @Test
    public void testStructHFA2() {
        // A composite type with more than four elements is not an HFA
        new Verifier(new CallingSequenceBuilderImpl(null))
            .arg(MemoryLayout.ofSequence(5, C_FLOAT),
                 binding(INTEGER_ARGUMENT_REGISTER, 0))
            .check(false);
    }

    @Test
    public void testStructHFA3() {
        MemoryLayout structFFF = MemoryLayout.ofStruct(C_FLOAT, C_FLOAT, C_FLOAT);

        // The first two structs should be passed in vector register and
        // the third on the stack
        new Verifier(new CallingSequenceBuilderImpl(null))
            .arg(structFFF,
                 binding(VECTOR_ARGUMENT_REGISTER, 0),
                 binding(VECTOR_ARGUMENT_REGISTER, 4),
                 binding(VECTOR_ARGUMENT_REGISTER, 8))
            .arg(structFFF,
                 binding(VECTOR_ARGUMENT_REGISTER, 0),
                 binding(VECTOR_ARGUMENT_REGISTER, 4),
                 binding(VECTOR_ARGUMENT_REGISTER, 8))
            .arg(structFFF,
                 binding(STACK_ARGUMENT_SLOT, 0),
                 binding(STACK_ARGUMENT_SLOT, 8))
            .check(false);
    }
}
