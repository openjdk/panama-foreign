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
 *          jdk.incubator.foreign/jdk.internal.foreign.abi.x64.windows
 * @build CallArrangerTestBase
 * @run testng TestWindowsCallArranger
 */

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.abi.Binding;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.x64.windows.CallArranger;
import org.testng.annotations.Test;

import java.lang.invoke.MethodType;

import static jdk.incubator.foreign.MemoryLayouts.WinABI.*;
import static jdk.incubator.foreign.MemoryLayouts.WinABI.asVarArg;
import static jdk.internal.foreign.abi.Binding.*;
import static jdk.internal.foreign.abi.Binding.copy;
import static jdk.internal.foreign.abi.x64.X86_64Architecture.*;
import static org.testng.Assert.*;

public class TestWindowsCallArranger extends CallArrangerTestBase {

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
    public void testIntegerRegs() {
        MethodType mt = MethodType.methodType(void.class, int.class, int.class, int.class, int.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false, C_INT, C_INT, C_INT, C_INT);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(rcx, int.class) },
            { move(rdx, int.class) },
            { move(r8, int.class) },
            { move(r9, int.class) }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @Test
    public void testDoubleRegs() {
        MethodType mt = MethodType.methodType(void.class, double.class, double.class, double.class, double.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(xmm0, double.class) },
            { move(xmm1, double.class) },
            { move(xmm2, double.class) },
            { move(xmm3, double.class) }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @Test
    public void testMixed() {
        MethodType mt = MethodType.methodType(void.class,
                long.class, long.class, float.class, float.class, long.class, long.class, float.class, float.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false,
                C_LONGLONG, C_LONGLONG, C_FLOAT, C_FLOAT, C_LONGLONG, C_LONGLONG, C_FLOAT, C_FLOAT);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(rcx, long.class) },
            { move(rdx, long.class) },
            { move(xmm2, float.class) },
            { move(xmm3, float.class) },
            { move(stackStorage(0), long.class) },
            { move(stackStorage(1), long.class) },
            { move(stackStorage(2), float.class) },
            { move(stackStorage(3), float.class) }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @Test
    public void testAbiExample() {
        MemoryLayout structLayout = MemoryLayout.ofStruct(C_INT, C_INT, C_DOUBLE);
        MethodType mt = MethodType.methodType(void.class,
                int.class, int.class, MemorySegment.class, int.class, int.class,
                double.class, double.class, double.class, int.class, int.class, int.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false,
                C_INT, C_INT, structLayout, C_INT, C_INT,
                C_DOUBLE, C_DOUBLE, C_DOUBLE, C_INT, C_INT, C_INT);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(rcx, int.class) },
            { move(rdx, int.class) },
            {
                copy(structLayout),
                baseAddress(),
                convertAddress(),
                move(r8, long.class)
            },
            { move(r9, int.class) },
            { move(stackStorage(0), int.class) },
            { move(stackStorage(1), double.class) },
            { move(stackStorage(2), double.class) },
            { move(stackStorage(3), double.class) },
            { move(stackStorage(4), int.class) },
            { move(stackStorage(5), int.class) },
            { move(stackStorage(6), int.class) }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @Test
    public void testAbiExampleVarargs() {
        MethodType mt = MethodType.methodType(void.class,
                int.class, double.class, int.class, double.class, double.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false,
                C_INT, C_DOUBLE, asVarArg(C_INT), asVarArg(C_DOUBLE), asVarArg(C_DOUBLE));
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            { move(rcx, int.class) },
            { move(xmm1, double.class) },
            { move(r8, int.class) },
            { dup(), move(r9, double.class), move(xmm3, double.class) },
            { move(stackStorage(0), double.class) },
        });

        checkReturnBindings(callingSequence, new Binding[]{});
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
    public void testStructRegister() {
        MemoryLayout struct = MemoryLayout.ofStruct(C_ULONGLONG);

        MethodType mt = MethodType.methodType(void.class, MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false, struct);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            { dereference(0, long.class), move(rcx, long.class) }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
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
    public void testStructReference() {
        MemoryLayout struct = MemoryLayout.ofStruct(C_ULONGLONG, C_ULONGLONG);

        MethodType mt = MethodType.methodType(void.class, MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(false, struct);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            {
                copy(struct),
                baseAddress(),
                convertAddress(),
                move(rcx, long.class)
            }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
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
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{
            { convertAddress(), move(rcx, long.class) }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }

    @Test
    public void testReturnRegisterStruct() {
        MemoryLayout struct = MemoryLayout.ofStruct(C_ULONGLONG);

        MethodType mt = MethodType.methodType(MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.of(struct, false);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertFalse(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), mt);
        assertEquals(callingSequence.functionDesc(), fd);

        checkArgumentBindings(callingSequence, new Binding[][]{});

        checkReturnBindings(callingSequence,
            new Binding[]{ allocate(struct),
                dup(),
                move(rax, long.class),
                dereference(0, long.class) });
    }

    @Test
    public void testIMR() {
        MemoryLayout struct = MemoryLayout.ofStruct(C_ULONGLONG, C_ULONGLONG);

        MethodType mt = MethodType.methodType(MemorySegment.class);
        FunctionDescriptor fd = FunctionDescriptor.of(struct, false);
        CallArranger.Bindings bindings = CallArranger.getBindings(mt, fd, false);

        assertTrue(bindings.isInMemoryReturn);
        CallingSequence callingSequence = bindings.callingSequence;
        assertEquals(callingSequence.methodType(), MethodType.methodType(void.class, MemoryAddress.class));
        assertEquals(callingSequence.functionDesc(), FunctionDescriptor.ofVoid(false, C_POINTER));

        checkArgumentBindings(callingSequence, new Binding[][]{
            { convertAddress(), move(rcx, long.class) }
        });

        checkReturnBindings(callingSequence, new Binding[]{});
    }
}
