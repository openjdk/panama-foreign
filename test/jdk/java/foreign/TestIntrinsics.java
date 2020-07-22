/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

/*
 * @test
 * @modules jdk.incubator.foreign/jdk.incubator.foreign.unsafe
 *          jdk.incubator.foreign/jdk.internal.foreign
 *          jdk.incubator.foreign/jdk.internal.foreign.abi
 *          java.base/sun.security.action
 * @build NativeTestHelper
 * @run testng/othervm
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Dforeign.restricted=permit
 *   -Xbatch
 *   TestIntrinsics
 */

import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.ForeignLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import jdk.incubator.foreign.MemoryLayout;
import org.testng.annotations.*;

import static java.lang.invoke.MethodType.methodType;
import static jdk.incubator.foreign.CSupport.*;
import static org.testng.Assert.assertEquals;

public class TestIntrinsics extends NativeTestHelper {

    static final ForeignLinker abi = CSupport.getSystemLinker();
    static final LibraryLookup lookup = LibraryLookup.ofLibrary("Intrinsics");

    private static final MethodHandle MH_empty;
    private static final MethodHandle MH_identity_int;
    private static final MethodHandle MH_identity_char;
    private static final MethodHandle MH_identity_short;
    private static final MethodHandle MH_identity_long;
    private static final MethodHandle MH_identity_float;
    private static final MethodHandle MH_identity_double;
    private static final MethodHandle MH_identity_va;
    private static final MethodHandle MH_invoke_consumer;

    private static MethodHandle linkIndentity(String name, Class<?> carrier, MemoryLayout layout)
            throws NoSuchMethodException {
        LibraryLookup.Symbol ma = lookup.lookup(name);
        MethodType mt = methodType(carrier, carrier);
        FunctionDescriptor fd = FunctionDescriptor.of(layout, layout);
        return abi.downcallHandle(ma, mt, fd);
    }

    static {
        try {
            {
                LibraryLookup.Symbol ma = lookup.lookup("empty");
                MethodType mt = methodType(void.class);
                FunctionDescriptor fd = FunctionDescriptor.ofVoid();
                MH_empty = abi.downcallHandle(ma, mt, fd);
            }
            MH_identity_char = linkIndentity("identity_char", byte.class, C_CHAR);
            MH_identity_short = linkIndentity("identity_short", short.class, C_SHORT);
            MH_identity_int = linkIndentity("identity_int", int.class, C_INT);
            MH_identity_long = linkIndentity("identity_long", long.class, C_LONGLONG);
            MH_identity_float = linkIndentity("identity_float", float.class, C_FLOAT);
            MH_identity_double = linkIndentity("identity_double", double.class, C_DOUBLE);
            {
                LibraryLookup.Symbol ma = lookup.lookup("identity_va");
                MethodType mt = methodType(int.class, int.class, double.class, int.class, float.class, long.class);
                FunctionDescriptor fd = FunctionDescriptor.of(C_INT, C_INT, asVarArg(C_DOUBLE),
                        asVarArg(C_INT), asVarArg(C_FLOAT), asVarArg(C_LONGLONG));
                MH_identity_va = abi.downcallHandle(ma, mt, fd);
            }
            {
                LibraryLookup.Symbol ma = lookup.lookup("invoke_consumer");
                MethodType mt = methodType(void.class, int.class, double.class, long.class, float.class, byte.class,
                        short.class, char.class);
                FunctionDescriptor fd = FunctionDescriptor.ofVoid(C_INT, C_DOUBLE, C_LONGLONG, C_FLOAT, C_CHAR,
                        C_SHORT, C_SHORT);
                MH_invoke_consumer = abi.downcallHandle(ma, mt, fd);
            }
        } catch (ReflectiveOperationException e) {
            throw new BootstrapMethodError(e);
        }
    }

    @DataProvider
    public Object[][] handles() throws Throwable {
        return new Object[][] {
            { "invoke_empty" },
            { "invoke_identity_char" },
            { "invoke_identity_short" },
            { "invoke_identity_int" },
            { "invoke_identity_long" },
            { "invoke_identity_float" },
            { "invoke_identity_double" },
            { "invoke_identity_va" },
            { "invoke_consumer" },
        };
    }

    @Test(dataProvider = "handles")
    public void testIntrinsics(String methodName) throws Throwable {
        MethodHandle handle = MethodHandles.lookup().findStatic(TestIntrinsics.class, methodName, methodType(void.class));

        for (int i = 0; i < 20_000; i++) {
            handle.invokeExact();
        }
    }

    // where

    public static void invoke_empty() throws Throwable {
        MH_empty.invokeExact();
    }

    public static void invoke_identity_char() throws Throwable {
        byte x = (byte) MH_identity_char.invokeExact((byte) 10);
        assertEquals(x, (byte) 10);
    }

    public static void invoke_identity_short() throws Throwable {
        short x = (short) MH_identity_short.invokeExact((short) 10);
        assertEquals(x, (short) 10);
    }

    public static void invoke_identity_int() throws Throwable {
        int x = (int) MH_identity_int.invokeExact(10);
        assertEquals(x, 10);
    }

    public static void invoke_identity_long() throws Throwable {
        long x = (long) MH_identity_long.invokeExact(10L);
        assertEquals(x, 10L);
    }

    public static void invoke_identity_float() throws Throwable {
        float x = (float) MH_identity_float.invokeExact(10F);
        assertEquals(x, 10F);
    }

    public static void invoke_identity_double() throws Throwable {
        double x = (double) MH_identity_double.invokeExact(10D);
        assertEquals(x, 10D);
    }

    public static void invoke_identity_va() throws Throwable {
        int x = (int) MH_identity_va.invokeExact(1, 10D, 2, 3F, 4L);
        assertEquals(x, 1);
    }

    public static void invoke_consumer() throws Throwable {
        MH_invoke_consumer.invokeExact(1, 10D, 2L, 3F, (byte) 0, (short) 13, (char) 'a');
    }
}
