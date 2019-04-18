/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 * @modules java.base/jdk.internal.foreign.abi java.base/jdk.internal.foreign.memory java.base/jdk.internal.foreign.abi.x64.sysv
 */

import java.foreign.NativeTypes;
import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.memory.LayoutType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.StorageClass;
import jdk.internal.foreign.abi.x64.sysv.CallingSequenceBuilderImpl;
import jdk.internal.foreign.abi.x64.sysv.SysVx64ABI;
import jdk.internal.foreign.memory.Types;

public class CallingSequenceTest {

    public void testInteger() {
        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);

        // Fill registers and spill over with 2 args on stack
        LayoutType<?> args[] = new LayoutType<?>[SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS + 2];
        for (int i = 0; i < SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS + 2; i++) {
            args[i] = NativeTypes.INT64;
        }

        Stream.of(args).map(LayoutType::layout).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        for (int i = 0; i < SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS; i++) {
            assertEquals(args[i].layout(), recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).argument().layout());
            assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).offset());
        }

        for (int i = 0; i < 2; i++) {
            assertEquals(args[SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS + i].layout(),
                    recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).argument().layout());
            assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).offset());
        }
    }

    public void testSse() {
        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);

        // Fill registers and spill over with 2 args on stack
        LayoutType<?> args[] = new LayoutType<?>[SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS + 2];
        for (int i = 0; i < SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS + 2; i++) {
            args[i] = NativeTypes.IEEE_FLOAT32;
        }

        Stream.of(args).map(LayoutType::layout).forEach(sc::addArgument);

        CallingSequence recipe = sc.build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        for (int i = 0; i < SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS; i++) {
            assertEquals(args[i].layout(), recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).argument().layout());
            assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).offset());
        }

        for (int i = 0; i < 2; i++) {
            assertEquals(args[SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS + i].layout(),
                    recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).argument().layout());
            assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).offset());
        }
    }

     public void testMixed() {
        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);

        // Fill GP registers + 2 on stack
        List<LayoutType<?>> args = new ArrayList<>();
        for (int i = 0; i < SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS + 2; i++) {
            args.add(NativeTypes.INT64);
        }

        // Fill SSE registers + 2 on stack
        for (int i = 0; i < SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS + 2; i++) {
            args.add(NativeTypes.IEEE_FLOAT32);
        }

        args.stream().map(LayoutType::layout).forEach(sc::addArgument);

        CallingSequence recipe = sc.build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(4, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        int arg = 0;
        for (int i = 0; i < SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS; i++, arg++) {
            assertEquals(args.get(arg).layout(), recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).argument().layout());
            assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).offset());
        }

        for (int i = 0; i < 2; i++, arg++) {
            assertEquals(args.get(arg).layout(), recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).argument().layout());
            assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).offset());
        }

        for (int i = 0; i < SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS; i++, arg++) {
            assertEquals(args.get(arg).layout(), recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).argument().layout());
            assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).offset());
        }

        for (int i = 2; i < 4; i++, arg++) {
            assertEquals(args.get(arg).layout(), recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).argument().layout());
            assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).offset());
        }
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
     * __m256 y;
     *
     * void m(e, f, s, g, h, ld, m, y, n, i, j, k);
     *
     * m(s);
     */
    public void testAbiExample() {
        Layout[] args = { Types.INT32, Types.INT32, Group.struct(Types.INT32, Types.INT32, Types.DOUBLE),
                Types.INT32, Types.INT32, Types.LONG_DOUBLE, Types.DOUBLE,
                Types.DOUBLE, Types.INT32, Types.INT32, Types.INT32 };

        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);
        Stream.of(args).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(6, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(3, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(4, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // e
        assertEquals(args[0], recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).offset());

        // f
        assertEquals(args[1], recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).offset());

        // s.a & s.b
        assertEquals(args[2], recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(2).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(2).offset());

        // s.d
        assertEquals(args[2], recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).argument().layout());
        assertEquals(8, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).offset());

        // g
        assertEquals(args[3], recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(3).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(3).offset());

        // h
        assertEquals(args[4], recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(4).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(4).offset());

        // ld
        assertEquals(args[5], recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).offset());
        assertEquals(args[5], recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).argument().layout());
        assertEquals(8, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).offset());

        // m
        assertEquals(args[6], recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).offset());

        // n
        assertEquals(args[7], recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(2).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(2).offset());

        // i
        assertEquals(args[8], recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(5).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(5).offset());

        // j
        assertEquals(args[9], recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).offset());

        // k
        assertEquals(args[10], recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(3).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(3).offset());
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
    public void testAbiExampleVarargs() {
        Layout[] args = {
                Types.INT,
                Types.DOUBLE,
                Types.INT,
                Types.LONG_DOUBLE,
                Types.DOUBLE };
        CallingSequenceBuilderImpl sc = new CallingSequenceBuilderImpl(null);
        Stream.of(args).forEach(sc::addArgument);
        CallingSequence recipe = sc.build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());


        // a
        assertEquals(args[0], recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).offset());

        // m
        assertEquals(args[1], recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).offset());

        // b
        assertEquals(args[2], recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).offset());

        // ld
        assertEquals(args[3], recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).offset());
        assertEquals(args[3], recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).argument().layout());
        assertEquals(8, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).offset());

        // n
        assertEquals(args[4], recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).offset());
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
    public void testStruct8() {
        Group structparm = Group.struct(Types.UNSIGNED.INT64);

        CallingSequence recipe = new CallingSequenceBuilderImpl(null)
                .addArgument(structparm)
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(1, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(structparm, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).offset());
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
    public void testStruct16() {
        Group structparm = Group.struct(Types.UNSIGNED.INT64, Types.UNSIGNED.INT64);

        CallingSequence recipe = new CallingSequenceBuilderImpl(null)
                .addArgument(structparm)
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(structparm, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).offset());

        // s.u1
        assertEquals(structparm, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).argument().layout());
        assertEquals(8, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).offset());
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
    public void testStruct24() {
        Group structparm = Group.struct(Types.UNSIGNED.INT64, Types.UNSIGNED.INT64, Types.UNSIGNED.INT64);

        CallingSequence recipe = new CallingSequenceBuilderImpl(null)
                .addArgument(structparm)
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(3, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(structparm, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).offset());

        // s.u1
        assertEquals(structparm, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).argument().layout());
        assertEquals(8, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).offset());

        // s.u2
        assertEquals(structparm, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).argument().layout());
        assertEquals(16, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).offset());
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
    public void testStruct32() {
        Layout structparm = Layout.of("[u64u64u64u64]");

        CallingSequence recipe = new CallingSequenceBuilderImpl(null)
                .addArgument(structparm)
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(4, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(structparm, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).offset());

        // s.u1
        assertEquals(structparm, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).argument().layout());
        assertEquals(8, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).offset());

        // s.u2
        assertEquals(structparm, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).argument().layout());
        assertEquals(16, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).offset());

        // s.u3
        assertEquals(structparm, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(3).argument().layout());
        assertEquals(24, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).get(3).offset());
    }

    /**
     * typedef void (*f)(void);
     *
     * void m(f f);
     * void f_impl(void);
     *
     * m(f_impl);
     */
    public void testFunctionType() {
        Layout arg = Layout.of("u64:()v");

        CallingSequence recipe = new CallingSequenceBuilderImpl(null)
                .addArgument(arg)
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(1, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(arg, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).offset());
    }

    /**
     * void f(int64_t l0, float f0, __m256 m0);
     */
    public void testMixedArgs() {
        CallingSequence recipe = new CallingSequenceBuilderImpl(null)
                .addArgument(Types.INT64)
                .addArgument(Types.FLOAT)
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(1, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(1, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // l0
        assertEquals(Types.INT64, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).offset());

        // f0
        assertEquals(Types.FLOAT, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).offset());
    }

    /**
     * struct s {
     *    int64_t l0;
     *    int64_t l1;
     * };
     *
     * void f(struct s s1);
     */
    public void testIntegerStruct() {
        Layout arg = Layout.of("[i64i64]");

        CallingSequence recipe = new CallingSequenceBuilderImpl(null)
                .addArgument(arg)
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.bindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.l0
        assertEquals(arg, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).argument().layout());
        assertEquals(0, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).offset());

        // s.l1
        assertEquals(arg, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).argument().layout());
        assertEquals(8, recipe.bindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).offset());
    }

    static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new RuntimeException("expected: " + expected + " does not match actual: " + actual);
        }
    }

    static void assertEquals(boolean expected, boolean actual) {
        if (expected != actual) {
            throw new RuntimeException("expected: " + expected + " does not match actual: " + actual);
        }
    }

    static void assertEquals(Object expected, Object actual) {
        if (expected != actual) {
            throw new RuntimeException("expected: " + expected + " does not match actual: " + actual);
        }
    }

    public static void main(String[] args) {
        CallingSequenceTest t = new CallingSequenceTest();

        t.testInteger();
        t.testSse();
        t.testMixed();
        t.testAbiExample();
        t.testAbiExampleVarargs();
        t.testStruct8();
        t.testStruct16();
        t.testStruct24();
        t.testStruct32();
        t.testFunctionType();
        t.testMixedArgs();
        t.testIntegerStruct();
    }
}
