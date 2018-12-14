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
import jdk.internal.foreign.abi.x64.sysv.Constants;
import jdk.internal.foreign.abi.x64.sysv.StandardCall;
import jdk.internal.foreign.memory.Types;

public class StandardCallTest {
    public StandardCallTest() {
    }

    public void testInteger() {
        StandardCall sc = new StandardCall();

        // Fill registers and spill over with 2 args on stack
        LayoutType<?> args[] = new LayoutType<?>[Constants.MAX_INTEGER_ARGUMENT_REGISTERS + 2];
        for (int i = 0; i < Constants.MAX_INTEGER_ARGUMENT_REGISTERS + 2; i++) {
            args[i] = NativeTypes.INT64;
        }

        CallingSequence recipe = sc.arrangeCall(null,
                Stream.of(args).map(LayoutType::layout).toArray(Layout[]::new));

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(Constants.MAX_INTEGER_ARGUMENT_REGISTERS, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        for (int i = 0; i < Constants.MAX_INTEGER_ARGUMENT_REGISTERS; i++) {
            assertEquals(args[i].layout(), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).getMember().getType());
            assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).getOffset());
        }

        for (int i = 0; i < 2; i++) {
            assertEquals(args[Constants.MAX_INTEGER_ARGUMENT_REGISTERS + i].layout(),
                    recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getMember().getType());
            assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getOffset());
        }
    }

    public void testSse() {
        StandardCall sc = new StandardCall();

        // Fill registers and spill over with 2 args on stack
        LayoutType<?> args[] = new LayoutType<?>[Constants.MAX_VECTOR_ARGUMENT_REGISTERS + 2];
        for (int i = 0; i < Constants.MAX_VECTOR_ARGUMENT_REGISTERS + 2; i++) {
            args[i] = NativeTypes.FLOAT;
        }

        CallingSequence recipe = sc.arrangeCall(null,
                Stream.of(args).map(LayoutType::layout).toArray(Layout[]::new));

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(Constants.MAX_VECTOR_ARGUMENT_REGISTERS, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        for (int i = 0; i < Constants.MAX_VECTOR_ARGUMENT_REGISTERS; i++) {
            assertEquals(args[i].layout(), recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).getMember().getType());
            assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).getOffset());
        }

        for (int i = 0; i < 2; i++) {
            assertEquals(args[Constants.MAX_VECTOR_ARGUMENT_REGISTERS + i].layout(),
                    recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getMember().getType());
            assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getOffset());
        }
    }

     public void testMixed() {
        StandardCall sc = new StandardCall();

        // Fill GP registers + 2 on stack
        List<LayoutType<?>> args = new ArrayList<>();
        for (int i = 0; i < Constants.MAX_INTEGER_ARGUMENT_REGISTERS + 2; i++) {
            args.add(NativeTypes.INT64);
        }

        // Fill SSE registers + 2 on stack
        for (int i = 0; i < Constants.MAX_VECTOR_ARGUMENT_REGISTERS + 2; i++) {
            args.add(NativeTypes.FLOAT);
        }

        CallingSequence recipe = sc.arrangeCall(null,
                args.stream().map(LayoutType::layout).toArray(Layout[]::new));

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(Constants.MAX_INTEGER_ARGUMENT_REGISTERS, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(Constants.MAX_VECTOR_ARGUMENT_REGISTERS, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(4, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        int arg = 0;
        for (int i = 0; i < Constants.MAX_INTEGER_ARGUMENT_REGISTERS; i++, arg++) {
            assertEquals(args.get(arg).layout(), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).getMember().getType());
            assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).getOffset());
        }

        for (int i = 0; i < 2; i++, arg++) {
            assertEquals(args.get(arg).layout(), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getMember().getType());
            assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getOffset());
        }

        for (int i = 0; i < Constants.MAX_VECTOR_ARGUMENT_REGISTERS; i++, arg++) {
            assertEquals(args.get(arg).layout(), recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).getMember().getType());
            assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).getOffset());
        }

        for (int i = 2; i < 4; i++, arg++) {
            assertEquals(args.get(arg).layout(), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getMember().getType());
            assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getOffset());
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

        StandardCall sc = new StandardCall();
        CallingSequence recipe = sc.arrangeCall(null, args);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(6, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(3, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(4, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // e
        assertEquals(args[0], recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());

        // f
        assertEquals(args[1], recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getOffset());

        // s.a & s.b
        assertEquals(args[2], recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(2).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(2).getOffset());

        // s.d
        assertEquals(args[2], recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getMember().getType());
        assertEquals(8, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getOffset());

        // g
        assertEquals(args[3], recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(3).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(3).getOffset());

        // h
        assertEquals(args[4], recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(4).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(4).getOffset());

        // ld
        assertEquals(args[5], recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getOffset());
        assertEquals(args[5], recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getMember().getType());
        assertEquals(8, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getOffset());

        // m
        assertEquals(args[6], recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).getOffset());

        // n
        assertEquals(args[7], recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(2).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(2).getOffset());

        // i
        assertEquals(args[8], recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(5).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(5).getOffset());

        // j
        assertEquals(args[9], recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getOffset());

        // k
        assertEquals(args[10], recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(3).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(3).getOffset());
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
        StandardCall sc = new StandardCall();
        CallingSequence recipe = sc.arrangeCall(null, args);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());


        // a
        assertEquals(args[0], recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());

        // m
        assertEquals(args[1], recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getOffset());

        // b
        assertEquals(args[2], recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getOffset());

        // ld
        assertEquals(args[3], recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getOffset());
        assertEquals(args[3], recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getMember().getType());
        assertEquals(8, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getOffset());

        // n
        assertEquals(args[4], recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).getOffset());
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

        StandardCall sc = new StandardCall();
        CallingSequence recipe = sc.arrangeCall(null, structparm);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(1, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(structparm, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());
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

        StandardCall sc = new StandardCall();
        CallingSequence recipe = sc.arrangeCall(null, structparm);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(structparm, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());

        // s.u1
        assertEquals(structparm, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getMember().getType());
        assertEquals(8, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getOffset());
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

        StandardCall sc = new StandardCall();
        CallingSequence recipe = sc.arrangeCall(null, structparm);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(3, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(structparm, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getOffset());

        // s.u1
        assertEquals(structparm, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getMember().getType());
        assertEquals(8, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getOffset());

        // s.u2
        assertEquals(structparm, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getMember().getType());
        assertEquals(16, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getOffset());
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

        StandardCall sc = new StandardCall();
        CallingSequence recipe = sc.arrangeCall(null, structparm);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(4, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(structparm, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getOffset());

        // s.u1
        assertEquals(structparm, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getMember().getType());
        assertEquals(8, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getOffset());

        // s.u2
        assertEquals(structparm, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getMember().getType());
        assertEquals(16, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getOffset());

        // s.u3
        assertEquals(structparm, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(3).getMember().getType());
        assertEquals(24, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(3).getOffset());
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
        CallingSequence recipe = new StandardCall().arrangeCall(null, arg);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(1, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(arg, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());
    }

    /**
     * void f(int64_t l0, float f0, __m256 m0);
     */
    public void testMixedArgs() {
        CallingSequence recipe = new StandardCall().arrangeCall(null,
                Types.INT64, Types.FLOAT);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(1, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(1, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // l0
        assertEquals(Types.INT64, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());

        // f0
        assertEquals(Types.FLOAT, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getOffset());
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

        CallingSequence recipe = new StandardCall().arrangeCall(null, arg);

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.l0
        assertEquals(arg, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember().getType());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());

        // s.l1
        assertEquals(arg, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getMember().getType());
        assertEquals(8, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getOffset());
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
        StandardCallTest t = new StandardCallTest();

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
