/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

/*
 * @test
 * @modules java.base/sun.nio.ch
 *          jdk.incubator.foreign/jdk.internal.foreign
 *          jdk.incubator.foreign/jdk.internal.foreign.abi
 *          jdk.incubator.foreign/jdk.internal.foreign.abi.aarch64
 * @build CallArrangerTestBase
 * @run testng TestAarch64CallArranger
 */

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.abi.Binding;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.aarch64.CallArranger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.invoke.MethodType;

import static jdk.incubator.foreign.MemoryLayouts.AArch64ABI.*;
import static jdk.internal.foreign.abi.Binding.*;
import static jdk.internal.foreign.abi.aarch64.AArch64Architecture.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestAarch64CallArranger extends CallArrangerTestBase {

    @Test
    public void testEmpty() {
        MethodType mt = MethodType.methodType(void.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{});

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @Test
    public void testInteger() {
        MethodType mt = MethodType.methodType(void.class,
                int.class, int.class, int.class, int.class,
                int.class, int.class, int.class, int.class,
                int.class, int.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false,
                C_INT, C_INT, C_INT, C_INT,
                C_INT, C_INT, C_INT, C_INT,
                C_INT, C_INT);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(r0, int.class) },
            { move(r1, int.class) },
            { move(r2, int.class) },
            { move(r3, int.class) },
            { move(r4, int.class) },
            { move(r5, int.class) },
            { move(r6, int.class) },
            { move(r7, int.class) },
            { move(stackStorage(0), int.class) },
            { move(stackStorage(1), int.class) },
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @Test
    public void testTwoIntTwoFloat() {
      MethodType mt = MethodType.methodType(void.class,
                int.class, int.class, float.class, float.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false,
                C_INT, C_INT, C_FLOAT, C_FLOAT);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(r0, int.class) },
            { move(r1, int.class) },
            { move(v0, float.class) },
            { move(v1, float.class) },
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @Test(dataProvider = "structs")
    public void testStruct(MemoryLayout struct, Binding[] expectedBindings) {
        MethodType mt = MethodType.methodType(void.class, MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false, struct);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            expectedBindings
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @DataProvider
    public static Object[][] structs() {
        MemoryLayout struct2 = MemoryLayout.ofStruct(C_INT, C_INT, C_DOUBLE, C_INT);
        return new Object[][]{
            // struct s { int32_t a, b; double c; };
            { MemoryLayout.ofStruct(C_INT, C_INT, C_DOUBLE), new Binding[] {
                dup(),
                    // s.a & s.b
                    dereference(0, long.class), move(r0, long.class),
                    // s.c --> note AArch64 passes this in an *integer* register
                    dereference(8, long.class), move(r1, long.class),
            }},
            // struct s { int32_t a, b; double c; int32_t d };
            { struct2, new Binding[] {
                copy(struct2),
                baseAddress(),
                convertAddress(),
                move(r0, long.class)
            }},
            // struct s { int32_t a[2]; float b[2] };
            { MemoryLayout.ofStruct(C_INT, C_INT, C_FLOAT, C_FLOAT), new Binding[] {
                dup(),
                    // s.a[0] & s.a[1]
                    dereference(0, long.class), move(r0, long.class),
                    // s.b[0] & s.b[1]
                    dereference(8, long.class), move(r1, long.class),
            }},
        };
    }

    @Test
    public void testMultipleStructs() {
        MemoryLayout struct1 = MemoryLayout.ofStruct(C_INT, C_INT, C_DOUBLE, C_INT);
        MemoryLayout struct2 = MemoryLayout.ofStruct(C_LONG, C_LONG, C_LONG);

        MethodType mt = MethodType.methodType(void.class, MemorySegment.class, MemorySegment.class, int.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false, struct1, struct2, C_INT);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            {
                copy(struct1),
                baseAddress(),
                convertAddress(),
                move(r0, long.class)
            },
            {
                copy(struct2),
                baseAddress(),
                convertAddress(),
                move(r1, long.class)
            },
            { move(r2, int.class) }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @Test
    public void testReturnStruct1() {
        MemoryLayout struct = MemoryLayout.ofStruct(C_LONG, C_LONG, C_FLOAT);

        MethodType mt = MethodType.methodType(MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.of(struct, false);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertTrue(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), MethodType.methodType(void.class, MemoryAddress.class));
        assertEquals(callingSequence.functionDesc(), FunctionDescriptor.ofVoid(false, C_POINTER));

        checkArgumentBindings(callingSequence, new Binding[][]{
            {
                convertAddress(),
                move(r8, long.class)
            }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @Test
    public void testReturnStruct2() {
        MemoryLayout struct = MemoryLayout.ofStruct(C_LONG, C_LONG);

        MethodType mt = MethodType.methodType(MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.of(struct, false);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{});

        checkReturnBindings(callingSequence, new Binding[]{
            allocate(struct),
            dup(),
            move(r0, long.class),
            dereference(0, long.class),
            dup(),
            move(r1, long.class),
            dereference(8, long.class),
        });
    }

    @Test
    public void testStructHFA1() {
        MemoryLayout hfa = MemoryLayout.ofStruct(C_FLOAT, C_FLOAT);

        MethodType mt = MethodType.methodType(MemorySegment.class, float.class, int.class, MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.of(hfa, false, C_FLOAT, C_INT, hfa);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(v0, float.class) },
            { move(r0, int.class) },
            {
                dup(),
                dereference(0, int.class),
                move(v1, int.class),
                dereference(4, int.class),
                move(v2, int.class)
            }
        });

        checkReturnBindings(callingSequence, new Binding[]{
            allocate(hfa),
            dup(),
            move(v0, int.class),
            dereference(0, int.class),
            dup(),
            move(v1, int.class),
            dereference(4, int.class),
        });
    }

    @Test
    public void testStructHFA3() {
        MemoryLayout struct = MemoryLayout.ofStruct(C_FLOAT, C_FLOAT, C_FLOAT);

        MethodType mt = MethodType.methodType(void.class, MemorySegment.class, MemorySegment.class, MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false, struct, struct, struct);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            {
                dup(),
                dereference(0, int.class),
                move(v0, int.class),
                dup(),
                dereference(4, int.class),
                move(v1, int.class),
                dereference(8, int.class),
                move(v2, int.class)
            },
            {
                dup(),
                dereference(0, int.class),
                move(v3, int.class),
                dup(),
                dereference(4, int.class),
                move(v4, int.class),
                dereference(8, int.class),
                move(v5, int.class)
            },
            {
                dup(),
                dereference(0, long.class),
                move(stackStorage(0), long.class),
                dereference(8, long.class),
                move(stackStorage(1), long.class),
            }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }
}
