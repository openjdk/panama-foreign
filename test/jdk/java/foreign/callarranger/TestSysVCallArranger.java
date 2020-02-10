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
 *          jdk.incubator.foreign/jdk.internal.foreign.abi.x64
 *          jdk.incubator.foreign/jdk.internal.foreign.abi.x64.sysv
 * @build CallArrangerTestBase
 * @run testng TestSysVCallArranger
 */

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.abi.Binding;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.x64.sysv.CallArranger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.invoke.MethodType;

import static jdk.incubator.foreign.MemoryLayouts.SysV.*;
import static jdk.incubator.foreign.MemoryLayouts.WinABI.C_POINTER;
import static jdk.internal.foreign.abi.Binding.*;
import static jdk.internal.foreign.abi.x64.X86_64Architecture.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestSysVCallArranger extends CallArrangerTestBase {

    @Test
    public void testEmpty() {
        MethodType mt = MethodType.methodType(void.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt.appendParameterTypes(long.class));
        assertEquals(callingSequence.functionDesc(), descAddArgument(fd, C_LONG));

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(rax, long.class) }
        });

        checkReturnBindings(callingSequence, new Binding[]{});

        assertEquals(bindings.nVectorArgs, 0);
    }

    @Test
    public void testIntegerRegs() {
        MethodType mt = MethodType.methodType(void.class,
                int.class, int.class, int.class, int.class, int.class, int.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false,
                C_INT, C_INT, C_INT, C_INT, C_INT, C_INT);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt.appendParameterTypes(long.class));
        assertEquals(callingSequence.functionDesc(), descAddArgument(fd, C_LONG));

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(rdi, int.class) },
            { move(rsi, int.class) },
            { move(rdx, int.class) },
            { move(rcx, int.class) },
            { move(r8, int.class) },
            { move(r9, int.class) },
            { move(rax, long.class) },
        });

        checkReturnBindings(callingSequence, new Binding[]{});

        assertEquals(bindings.nVectorArgs, 0);
    }

    @Test
    public void testDoubleRegs() {
        MethodType mt = MethodType.methodType(void.class,
                double.class, double.class, double.class, double.class,
                double.class, double.class, double.class, double.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false,
                C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE,
                C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt.appendParameterTypes(long.class));
        assertEquals(callingSequence.functionDesc(), descAddArgument(fd, C_LONG));

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(xmm0, double.class) },
            { move(xmm1, double.class) },
            { move(xmm2, double.class) },
            { move(xmm3, double.class) },
            { move(xmm4, double.class) },
            { move(xmm5, double.class) },
            { move(xmm6, double.class) },
            { move(xmm7, double.class) },
            { move(rax, long.class) },
        });

        checkReturnBindings(callingSequence, new Binding[]{});

        assertEquals(bindings.nVectorArgs, 8);
    }

    @Test
    public void testMixed() {
        MethodType mt = MethodType.methodType(void.class,
                long.class, long.class, long.class, long.class, long.class, long.class, long.class, long.class,
                float.class, float.class, float.class, float.class,
                float.class, float.class, float.class, float.class, float.class, float.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false,
                C_LONG, C_LONG, C_LONG, C_LONG, C_LONG, C_LONG, C_LONG, C_LONG,
                C_FLOAT, C_FLOAT, C_FLOAT, C_FLOAT,
                C_FLOAT, C_FLOAT, C_FLOAT, C_FLOAT, C_FLOAT, C_FLOAT);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt.appendParameterTypes(long.class));
        assertEquals(callingSequence.functionDesc(), descAddArgument(fd, C_LONG));

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(rdi, long.class) },
            { move(rsi, long.class) },
            { move(rdx, long.class) },
            { move(rcx, long.class) },
            { move(r8, long.class) },
            { move(r9, long.class) },
            { move(stackStorage(0), long.class) },
            { move(stackStorage(1), long.class) },
            { move(xmm0, float.class) },
            { move(xmm1, float.class) },
            { move(xmm2, float.class) },
            { move(xmm3, float.class) },
            { move(xmm4, float.class) },
            { move(xmm5, float.class) },
            { move(xmm6, float.class) },
            { move(xmm7, float.class) },
            { move(stackStorage(2), float.class) },
            { move(stackStorage(3), float.class) },
            { move(rax, long.class) },
        });

        checkReturnBindings(callingSequence, new Binding[]{});

        assertEquals(bindings.nVectorArgs, 8);
    }

    /**
     * This is the example from the System V ABI AMD64 document
     *
     * struct structparm {
     *   int32_t a, int32_t b, double d;
     * } s;
     * int32_t e, f, g, h, i, j, k;
     * double m, n;
     *
     * void m(e, f, s, g, h, m, n, i, j, k);
     *
     * m(s);
     */
    @Test
    public void testAbiExample() {
        MemoryLayout struct = MemoryLayout.ofStruct(C_INT, C_INT, C_DOUBLE);

        MethodType mt = MethodType.methodType(void.class,
                int.class, int.class, MemorySegment.class, int.class, int.class,
                double.class, double.class, int.class, int.class, int.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false,
                C_INT, C_INT, struct, C_INT, C_INT, C_DOUBLE, C_DOUBLE, C_INT, C_INT, C_INT);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt.appendParameterTypes(long.class));
        assertEquals(callingSequence.functionDesc(), descAddArgument(fd, C_LONG));

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(rdi, int.class) },
            { move(rsi, int.class) },
            {
                dup(),
                dereference(0, long.class), move(rdx, long.class),
                dereference(8, long.class), move(xmm0, long.class)
            },
            { move(rcx, int.class) },
            { move(r8, int.class) },
            { move(xmm1, double.class) },
            { move(xmm2, double.class) },
            { move(r9, int.class) },
            { move(stackStorage(0), int.class) },
            { move(stackStorage(1), int.class) },
            { move(rax, long.class) },
        });

        checkReturnBindings(callingSequence, new Binding[]{});

        assertEquals(bindings.nVectorArgs, 3);
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
    public void testMemoryAddress() {
        MethodType mt = MethodType.methodType(void.class, MemoryAddress.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false, C_POINTER);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt.appendParameterTypes(long.class));
        assertEquals(callingSequence.functionDesc(), descAddArgument(fd, C_LONG));

        checkArgumentBindings(callingSequence, new Binding[][]{
            { convertAddress(), move(rdi, long.class) },
            { move(rax, long.class) },
        });

        checkReturnBindings(callingSequence, new Binding[]{});

        assertEquals(bindings.nVectorArgs, 0);
    }

    @Test(dataProvider = "structs")
    public void testStruct(MemoryLayout struct, Binding[] expectedBindings) {
        MethodType mt = MethodType.methodType(void.class, MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false, struct);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt.appendParameterTypes(long.class));
        assertEquals(callingSequence.functionDesc(), descAddArgument(fd, C_LONG));

        checkArgumentBindings(callingSequence, new Binding[][]{
            expectedBindings,
            { move(rax, long.class) },
        });

        checkReturnBindings(callingSequence, new Binding[]{});

        assertEquals(bindings.nVectorArgs, 0);
    }


    @DataProvider
    public static Object[][] structs() {
        return new Object[][]{
            { MemoryLayout.ofStruct(C_ULONG), new Binding[]{
                    dereference(0, long.class), move(rdi, long.class)
                }
            },
            { MemoryLayout.ofStruct(C_ULONG, C_ULONG), new Binding[]{
                    dup(),
                    dereference(0, long.class), move(rdi, long.class),
                    dereference(8, long.class), move(rsi, long.class)
                }
            },
            { MemoryLayout.ofStruct(C_ULONG, C_ULONG, C_ULONG), new Binding[]{
                    dup(),
                    dereference(0, long.class), move(stackStorage(0), long.class),
                    dup(),
                    dereference(8, long.class), move(stackStorage(1), long.class),
                    dereference(16, long.class), move(stackStorage(2), long.class)
                }
            },
            { MemoryLayout.ofStruct(C_ULONG, C_ULONG, C_ULONG, C_ULONG), new Binding[]{
                    dup(),
                    dereference(0, long.class), move(stackStorage(0), long.class),
                    dup(),
                    dereference(8, long.class), move(stackStorage(1), long.class),
                    dup(),
                    dereference(16, long.class), move(stackStorage(2), long.class),
                    dereference(24, long.class), move(stackStorage(3), long.class)
                }
            },
        };
    }

    @Test
    public void testReturnRegisterStruct() {
        MemoryLayout struct = MemoryLayout.ofStruct(C_ULONG, C_ULONG);

        MethodType mt = MethodType.methodType(MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.of(struct, false);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt.appendParameterTypes(long.class));
        assertEquals(callingSequence.functionDesc(), descAddArgument(fd, C_LONG));

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(rax, long.class) }
        });

        checkReturnBindings(callingSequence, new Binding[] {
            allocate(struct),
            dup(),
            move(rax, long.class),
            dereference(0, long.class),
            dup(),
            move(rdx, long.class),
            dereference(8, long.class)
        });

        assertEquals(bindings.nVectorArgs, 0);
    }

    @Test
    public void testIMR() {
        MemoryLayout struct = MemoryLayout.ofStruct(C_ULONG, C_ULONG, C_ULONG);

        MethodType mt = MethodType.methodType(MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.of(struct, false);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertTrue(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), MethodType.methodType(void.class, MemoryAddress.class, long.class));
        assertEquals(callingSequence.functionDesc(), FunctionDescriptor.ofVoid(false, C_POINTER, C_LONG));

        checkArgumentBindings(callingSequence, new Binding[][]{
            { convertAddress(), move(rdi, long.class) },
            { move(rax, long.class) }
        });

        checkReturnBindings(callingSequence, new Binding[] {});

        assertEquals(bindings.nVectorArgs, 0);
    }

}
