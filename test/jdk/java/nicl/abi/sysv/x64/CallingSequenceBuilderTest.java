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
 * @modules java.base/jdk.internal.nicl.abi java.base/jdk.internal.nicl.types java.base/jdk.internal.nicl.abi.sysv.x64
 */

import jdk.internal.nicl.abi.CallingSequence;
import jdk.internal.nicl.abi.CallingSequenceBuilder;
import jdk.internal.nicl.abi.StorageClass;
import jdk.internal.nicl.abi.sysv.x64.CallingSequenceBuilderImpl;
import jdk.internal.nicl.abi.sysv.x64.Constants;
import jdk.internal.nicl.types.Types;

import java.nicl.layout.Group;
import java.nicl.layout.Layout;

public class CallingSequenceBuilderTest {
    public CallingSequenceBuilderTest() {
    }

    public void testInteger() {
        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        // Fill registers and spill over with 2 args on stack
        for (int i = 0; i < Constants.MAX_INTEGER_ARGUMENT_REGISTERS + 2; i++) {
            builder.addArgument(Types.INT64, "l" + i);
        }

        CallingSequence recipe = builder.build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(Constants.MAX_INTEGER_ARGUMENT_REGISTERS, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        for (int i = 0; i < Constants.MAX_INTEGER_ARGUMENT_REGISTERS; i++) {
            assertEquals(builder.getArguments().get(i), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).getMember());
            assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).getOffset());
        }

        for (int i = 0; i < 2; i++) {
            assertEquals(builder.getArguments().get(Constants.MAX_INTEGER_ARGUMENT_REGISTERS + i), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getMember());
            assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getOffset());
        }
    }

    public void testSse() {
        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        // Fill registers and spill over with 2 args on stack
        for (int i = 0; i < Constants.MAX_VECTOR_ARGUMENT_REGISTERS + 2; i++) {
            builder.addArgument(Types.FLOAT, "f" + i);
        }

        CallingSequence recipe = builder.build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(Constants.MAX_VECTOR_ARGUMENT_REGISTERS, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        for (int i = 0; i < Constants.MAX_VECTOR_ARGUMENT_REGISTERS; i++) {
            assertEquals(builder.getArguments().get(i), recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).getMember());
            assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).getOffset());
        }

        for (int i = 0; i < 2; i++) {
            assertEquals(builder.getArguments().get(Constants.MAX_VECTOR_ARGUMENT_REGISTERS + i), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getMember());
            assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getOffset());
        }
    }

     public void testMixed() {
        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        // Fill GP registers + 2 on stack
        for (int i = 0; i < Constants.MAX_INTEGER_ARGUMENT_REGISTERS + 2; i++) {
            builder.addArgument(Types.INT64, "l" + i);
        }

        // Fill SSE registers + 2 on stack
        for (int i = 0; i < Constants.MAX_VECTOR_ARGUMENT_REGISTERS + 2; i++) {
            builder.addArgument(Types.FLOAT, "f" + i);
        }

        CallingSequence recipe = builder.build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(Constants.MAX_INTEGER_ARGUMENT_REGISTERS, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(Constants.MAX_VECTOR_ARGUMENT_REGISTERS, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(4, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        int arg = 0;
        for (int i = 0; i < Constants.MAX_INTEGER_ARGUMENT_REGISTERS; i++, arg++) {
            assertEquals(builder.getArguments().get(arg), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).getMember());
            assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(i).getOffset());
        }

        for (int i = 0; i < 2; i++, arg++) {
            assertEquals(builder.getArguments().get(arg), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getMember());
            assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getOffset());
        }

        for (int i = 0; i < Constants.MAX_VECTOR_ARGUMENT_REGISTERS; i++, arg++) {
            assertEquals(builder.getArguments().get(arg), recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).getMember());
            assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(i).getOffset());
        }

        for (int i = 2; i < 4; i++, arg++) {
            assertEquals(builder.getArguments().get(arg), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(i).getMember());
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
        Group structparm = Group.struct(Types.INT32, Types.INT32, Types.DOUBLE);

        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        CallingSequence recipe = builder
                .addArgument(Types.INT32, "e")
                .addArgument(Types.INT32, "f")
                .addArgument(structparm, "s")
                .addArgument(Types.INT32, "g")
                .addArgument(Types.INT32, "h")
                .addArgument(Types.LONG_DOUBLE, "ld")
                .addArgument(Types.DOUBLE, "m")
                .addArgument(Types.DOUBLE, "n")
                .addArgument(Types.INT32, "i")
                .addArgument(Types.INT32, "j")
                .addArgument(Types.INT32, "k")
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(6, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(3, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(4, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // e
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());

        // f
        assertEquals(builder.getArguments().get(1), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getOffset());

        // s.a & s.b
        assertEquals(builder.getArguments().get(2), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(2).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(2).getOffset());

        // s.d
        assertEquals(builder.getArguments().get(2), recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getMember());
        assertEquals(8, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getOffset());

        // g
        assertEquals(builder.getArguments().get(3), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(3).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(3).getOffset());

        // h
        assertEquals(builder.getArguments().get(4), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(4).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(4).getOffset());

        // ld
        assertEquals(builder.getArguments().get(5), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getOffset());
        assertEquals(builder.getArguments().get(5), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getMember());
        assertEquals(8, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getOffset());

        // m
        assertEquals(builder.getArguments().get(6), recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).getOffset());

        // n
        assertEquals(builder.getArguments().get(7), recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(2).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(2).getOffset());

        // i
        assertEquals(builder.getArguments().get(8), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(5).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(5).getOffset());

        // j
        assertEquals(builder.getArguments().get(9), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getOffset());

        // k
        assertEquals(builder.getArguments().get(10), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(3).getMember());
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
        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        CallingSequence recipe = builder
                .addArgument(Types.INT, "a")
                .addArgument(Types.DOUBLE, "m")
                .addArgument(Types.INT, null)
                .addArgument(Types.LONG_DOUBLE, null)
                .addArgument(Types.DOUBLE, null)
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(2, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());


        // a
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());

        // m
        assertEquals(builder.getArguments().get(1), recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getOffset());

        // b
        assertEquals(builder.getArguments().get(2), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getOffset());

        // ld
        assertEquals(builder.getArguments().get(3), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getOffset());
        assertEquals(builder.getArguments().get(3), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getMember());
        assertEquals(8, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getOffset());

        // n
        assertEquals(builder.getArguments().get(4), recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(1).getMember());
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

        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        CallingSequence recipe = builder
                .addArgument(structparm, "s")
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(1, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember());
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

        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        CallingSequence recipe = builder
                .addArgument(structparm, "s")
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());

        // s.u1
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getMember());
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

        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        CallingSequence recipe = builder
                .addArgument(structparm, "s")
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(3, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getOffset());

        // s.u1
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getMember());
        assertEquals(8, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getOffset());

        // s.u2
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getMember());
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

        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        CallingSequence recipe = builder
                .addArgument(structparm, "s")
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(4, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(0).getOffset());

        // s.u1
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getMember());
        assertEquals(8, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(1).getOffset());

        // s.u2
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getMember());
        assertEquals(16, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(2).getOffset());

        // s.u3
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).get(3).getMember());
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
        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        CallingSequence recipe = builder
            .addArgument(Layout.of("u64:()v"), "f")
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(1, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.u0
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());
    }

    /**
     * void f(int64_t l0, float f0, __m256 m0);
     */
    public void testMixedArgs() {
        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        CallingSequence recipe = builder
                .addArgument(Types.INT64, "l0")
                .addArgument(Types.FLOAT, "f0")
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(1, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(1, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // l0
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());

        // f0
        assertEquals(builder.getArguments().get(1), recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).get(0).getMember());
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
        CallingSequenceBuilder builder = new CallingSequenceBuilder(CallingSequenceBuilderImpl.class);

        CallingSequence recipe = builder
                .addArgument(Layout.of("[i64i64]"), "s1")
                .build();

        assertEquals(false, recipe.returnsInMemory());
        assertEquals(2, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.VECTOR_ARGUMENT_REGISTER).size());
        assertEquals(0, recipe.getBindings(StorageClass.STACK_ARGUMENT_SLOT).size());

        // s.l0
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getMember());
        assertEquals(0, recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(0).getOffset());

        // s.l1
        assertEquals(builder.getArguments().get(0), recipe.getBindings(StorageClass.INTEGER_ARGUMENT_REGISTER).get(1).getMember());
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
        CallingSequenceBuilderTest t = new CallingSequenceBuilderTest();

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
