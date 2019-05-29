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
 * @modules java.base/jdk.internal.foreign.abi java.base/jdk.internal.foreign.memory java.base/jdk.internal.foreign.abi.aarch64
 * @run testng CallingSequenceTest
 */

import java.foreign.NativeTypes;
import java.foreign.layout.Address;
import java.foreign.layout.Group;
import java.foreign.layout.Sequence;
import java.foreign.layout.Layout;
import java.foreign.memory.LayoutType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.StorageClass;
import jdk.internal.foreign.abi.aarch64.CallingSequenceBuilderImpl;
import jdk.internal.foreign.abi.aarch64.AArch64ABI;
import jdk.internal.foreign.memory.Types;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class CallingSequenceTest {

    @Test
    public void testInteger() {
        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);

        // Fill registers and spill over with 2 args on stack
        LayoutType<?> args[] =
            new LayoutType<?>[AArch64ABI.MAX_INTEGER_ARGUMENT_REGISTERS + 2];
        for (int i = 0; i < AArch64ABI.MAX_INTEGER_ARGUMENT_REGISTERS + 2; i++) {
            args[i] = NativeTypes.INT64;
        }

        Stream.of(args).map(LayoutType::layout).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var stackArgs = recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT);

        assertEquals(recipe.returnsInMemory(), false);
        assertEquals(intArgs.size(), AArch64ABI.MAX_INTEGER_ARGUMENT_REGISTERS);
        assertEquals(vecArgs.size(), 0);
        assertEquals(stackArgs.size(), 2);

        for (int i = 0; i < AArch64ABI.MAX_INTEGER_ARGUMENT_REGISTERS; i++) {
            assertEquals(args[i].layout(), intArgs.get(i).argument().layout());
            assertEquals(intArgs.get(i).offset(), 0);
        }

        for (int i = 0; i < 2; i++) {
            assertEquals(stackArgs.get(i).argument().layout(),
                                args[AArch64ABI.MAX_INTEGER_ARGUMENT_REGISTERS + i].layout());
            assertEquals(stackArgs.get(i).offset(), 0);
        }
    }

    @Test
    public void testTwoIntTwoFloat() {
        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);

        LayoutType<?> args[] = {
            NativeTypes.INT32,
            NativeTypes.INT32,
            NativeTypes.FLOAT,
            NativeTypes.FLOAT,
        };

        Stream.of(args).map(LayoutType::layout).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var stackArgs = recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, intArgs.size());
        assertEquals(2, vecArgs.size());
        assertEquals(0, stackArgs.size());

        assertEquals(args[0].layout(), intArgs.get(0).argument().layout());
        assertEquals(args[1].layout(), intArgs.get(1).argument().layout());
        assertEquals(args[2].layout(), vecArgs.get(0).argument().layout());
        assertEquals(args[3].layout(), vecArgs.get(1).argument().layout());
    }

    @Test
    public void testVectorArg() {
        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);

        LayoutType<?> args[] = {
            NativeTypes.INT32,
            NativeTypes.INT32,
            NativeTypes.FLOAT,
            NativeTypes.FLOAT,
        };

        Stream.of(args).map(LayoutType::layout).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var stackArgs = recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, intArgs.size());
        assertEquals(2, vecArgs.size());
        assertEquals(0, stackArgs.size());

        assertEquals(args[0].layout(), intArgs.get(0).argument().layout());
        assertEquals(args[1].layout(), intArgs.get(1).argument().layout());
        assertEquals(args[2].layout(), vecArgs.get(0).argument().layout());
        assertEquals(args[3].layout(), vecArgs.get(1).argument().layout());
    }

    @Test
    public void testStruct1() {
        // struct s { int32_t a, b; double c; };
        Layout[] args = {
            Group.struct(Types.INT32, Types.INT32, Types.DOUBLE)
        };

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);
        Stream.of(args).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var stackArgs = recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT);

        assertEquals(recipe.returnsInMemory(), false);
        assertEquals(intArgs.size(), 2);
        assertEquals(vecArgs.size(), 0);
        assertEquals(stackArgs.size(), 0);

        // s.a & s.b
        assertEquals(intArgs.get(0).argument().layout(), args[0]);
        assertEquals(intArgs.get(0).offset(), 0);

        // s.c --> note AArch64 passes this in an *integer* register
        assertEquals(intArgs.get(1).argument().layout(), args[0]);
        assertEquals(intArgs.get(1).offset(), 8);
    }

    @Test
    public void testStruct2() {
        // struct s { int32_t a, b; double c; int32_t d };
        Layout[] args = {
            Group.struct(Types.INT32, Types.INT32, Types.DOUBLE, Types.INT32)
        };

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);
        Stream.of(args).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var stackArgs = recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT);
        var indirectResult = recipe.bindings(StorageClass.INDIRECT_RESULT_REGISTER);

        assertEquals(recipe.returnsInMemory(), false);
        assertEquals(intArgs.size(), 1);
        assertEquals(vecArgs.size(), 0);
        assertEquals(stackArgs.size(), 0);
        assertEquals(indirectResult.size(), 0);

        assertEquals(intArgs.get(0).argument().layout(), args[0]);
        assertEquals(intArgs.get(0).offset(), 0);
    }

    @Test
    public void testStruct3() {
        // struct s { int32_t a, b; double c; int32_t d; };
        // struct t { int64_t a, b, c; };
        Layout[] args = {
            Group.struct(Types.INT32, Types.INT32, Types.DOUBLE, Types.INT32),
            Group.struct(Types.INT64, Types.INT64, Types.INT64),
            Types.INT32
        };

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);
        Stream.of(args).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var stackArgs = recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT);
        var indirectResult = recipe.bindings(StorageClass.INDIRECT_RESULT_REGISTER);

        assertEquals(recipe.returnsInMemory(), false);
        assertEquals(intArgs.size(), 3);
        assertEquals(vecArgs.size(), 0);
        assertEquals(stackArgs.size(), 0);
        assertEquals(indirectResult.size(), 0);

        assertEquals(intArgs.get(0).argument().layout(), args[0]);
        assertEquals(intArgs.get(0).offset(), 0);

        assertEquals(intArgs.get(1).argument().layout(), args[1]);
        assertEquals(intArgs.get(1).offset(), 0);

        assertEquals(intArgs.get(2).argument().layout(), args[2]);
        assertEquals(intArgs.get(2).offset(), 0);
    }

    @Test
    public void testArray1() {
        // struct s { int32_t a[2]; float b[2] };
        Layout[] args = {
            Group.struct(Sequence.of(2, Types.INT32),
                         Sequence.of(2, Types.FLOAT))
        };

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);
        Stream.of(args).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var stackArgs = recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT);

        assertEquals(recipe.returnsInMemory(), false);
        assertEquals(intArgs.size(), 2);
        assertEquals(vecArgs.size(), 0);
        assertEquals(stackArgs.size(), 0);

        // s.a[0] & s.a[1]
        assertEquals(intArgs.get(0).argument().layout(), args[0]);
        assertEquals(intArgs.get(0).offset(), 0);

        // s.b[0] & s.b[1]
        assertEquals(intArgs.get(1).argument().layout(), args[0]);
        assertEquals(intArgs.get(1).offset(), 8);
    }

    @Test
    public void testArray2() {
        // Composite types always passed in integer registers
        Layout[] args = {
            Sequence.of(4, Types.INT)
        };

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);
        Stream.of(args).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var stackArgs = recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT);

        assertEquals(recipe.returnsInMemory(), false);
        assertEquals(intArgs.size(), 2);
        assertEquals(vecArgs.size(), 0);
        assertEquals(stackArgs.size(), 0);

        // s.a[0] & s.a[1]
        assertEquals(intArgs.get(0).argument().layout(), args[0]);
        assertEquals(intArgs.get(0).offset(), 0);

        // s.a[2] & s.a[3]
        assertEquals(intArgs.get(1).argument().layout(), args[0]);
        assertEquals(intArgs.get(1).offset(), 8);
    }

    @Test
    public void testReturnStruct1() {
        // Pointer to temporary storage for result should be passed in
        // the indirect result register
        Layout resultLayout = Group.struct(Types.INT64, Types.INT64, Types.FLOAT);

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(resultLayout);
        CallingSequence recipe = sc.build();

        var intReturns = recipe.bindings(StorageClass.INTEGER_RETURN_REGISTER);
        var indirectResult = recipe.bindings(StorageClass.INDIRECT_RESULT_REGISTER);

        assertTrue(recipe.returnsInMemory());
        assertEquals(intReturns.size(), 1);  // XXX: why?
        assertEquals(indirectResult.size(), 1);

        assertEquals(indirectResult.get(0).argument().layout(),
                     Address.ofLayout(64, resultLayout));
        assertEquals(indirectResult.get(0).argument().argumentIndex(), -1);
    }

    @Test
    public void testReturnStruct2() {
        // If the size of the returned struct is <= 16 bytes then it
        // should be returned in integer registers
        Layout resultLayout = Group.struct(Types.INT64, Types.INT64);

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
        Layout structHFA = Group.struct(Types.FLOAT, Types.FLOAT);
        Layout[] args = { Types.FLOAT, Types.INT, structHFA };

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
        Layout[] args = { Sequence.of(5, Types.FLOAT) };

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);
        Stream.of(args).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var stackArgs = recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT);

        assertEquals(intArgs.size(), 1);
        assertEquals(vecArgs.size(), 0);
        assertEquals(stackArgs.size(), 0);

        assertEquals(intArgs.get(0).argument().layout(), args[0]);
        assertEquals(intArgs.get(0).offset(), 0);
    }

    @Test
    public void testStructHFA3() {
        Layout structFFF = Group.struct(Types.FLOAT, Types.FLOAT, Types.FLOAT);

        // The first two structs should be passed in vector register and
        // the third on the stack
        Layout[] args = { structFFF, structFFF, structFFF };

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);
        Stream.of(args).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        var intArgs = recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER);
        var vecArgs = recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER);
        var stackArgs = recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT);

        assertEquals(intArgs.size(), 0);
        assertEquals(vecArgs.size(), 6);  // 3x2 float members
        assertEquals(stackArgs.size(), 2); // 12 byte struct in 2x8 bytes slots

        for (int i = 0; i < 3; i++) {
            assertEquals(vecArgs.get(i).argument().layout(), structFFF);
            assertEquals(vecArgs.get(i).offset(), i * NativeTypes.FLOAT.bytesSize());
        }

        for (int i = 3; i < 6; i++) {
            assertEquals(vecArgs.get(i).argument().layout(), structFFF);
            assertEquals(vecArgs.get(i).offset(),
                         ((i - 3) * NativeTypes.FLOAT.bytesSize()));
        }

        for (int i = 0; i < 2; i++) {
            assertEquals(stackArgs.get(i).argument().layout(), structFFF);
            assertEquals(stackArgs.get(i).offset(), i * NativeTypes.UINT64.bytesSize());
        }
    }
}
