/*
 *  Copyright (c) 2020, 2021, Oracle and/or its affiliates. All rights reserved.
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
 * @requires os.arch=="amd64" | os.arch=="x86_64" | os.arch=="aarch64"
 * @run testng/othervm
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   --enable-native-access=ALL-UNNAMED
 *   -Xbatch
 *   TestIntrinsics
 */

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.SymbolLookup;
import org.testng.annotations.*;

import static java.lang.invoke.MethodType.methodType;
import static org.testng.Assert.assertEquals;

public class TestIntrinsics extends NativeTestHelper {

    static final CLinker abi = CLinker.systemCLinker();
    static {
        System.loadLibrary("Intrinsics");
    }

    static final SymbolLookup LOOKUP = SymbolLookup.loaderLookup();

    private interface RunnableX {
        void run() throws Throwable;
    }

    @Test(dataProvider = "tests")
    public void testIntrinsics(String name, RunnableX test) throws Throwable {
        for (int i = 0; i < 20_000; i++) {
            test.run();
        }
    }

    @DataProvider
    public Object[][] tests() {
        List<Object[]> testsList = new ArrayList<>();

        interface AddTest {
            void add(String name, MethodHandle target, Object expectedResult, Object... args);
        }

        AddTest tests = (name, mh, expectedResult, args) -> testsList.add(new Object[] { name, (RunnableX) () -> {
            Object actual = mh.invokeWithArguments(args);
            assertEquals(actual, expectedResult);
        }});

        interface AddIdentity {
            void add(String name, Class<?> carrier, MemoryLayout layout, Object arg);
        }

        AddIdentity addIdentity = (name, carrier, layout, arg) -> {
            MemoryAddress ma = LOOKUP.lookup(name).get();
            FunctionDescriptor fd = FunctionDescriptor.of(layout, layout);

            tests.add(name, abi.downcallHandle(ma, fd), arg, arg);
            tests.add(name + "_virtual", abi.downcallHandle(fd), arg, ma, arg);
        };

        { // empty
            MemoryAddress ma = LOOKUP.lookup("empty").get();
            FunctionDescriptor fd = FunctionDescriptor.ofVoid();
            tests.add("empty", abi.downcallHandle(ma, fd), null);
        }

        addIdentity.add("identity_bool",   boolean.class, C_BOOL,   true);
        addIdentity.add("identity_char",   byte.class,    C_CHAR,   (byte) 10);
        addIdentity.add("identity_short",  short.class,   C_SHORT, (short) 10);
        addIdentity.add("identity_int",    int.class,     C_INT,           10);
        addIdentity.add("identity_long",   long.class,    C_LONG_LONG,     10L);
        addIdentity.add("identity_float",  float.class,   C_FLOAT,         10F);
        addIdentity.add("identity_double", double.class,  C_DOUBLE,        10D);

        { // identity_va
            MemoryAddress ma = LOOKUP.lookup("identity_va").get();
            FunctionDescriptor fd = FunctionDescriptor.of(C_INT, C_INT).asVariadic(C_DOUBLE, C_INT, C_FLOAT, C_LONG_LONG);
            tests.add("varargs", abi.downcallHandle(ma, fd), 1, 1, 10D, 2, 3F, 4L);
        }

        { // high_arity
            FunctionDescriptor baseFD = FunctionDescriptor.ofVoid(
                    C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG, C_LONG_LONG,
                    C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE, C_DOUBLE,
                    C_INT, C_DOUBLE, C_LONG_LONG, C_FLOAT, C_CHAR, C_SHORT, C_SHORT);
            Object[] args = {
                0, 0, 0, 0, 0, 0, 0, 0, // saturating registers
                0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D, // saturating registers
                1, 10D, 2L, 3F, (byte) 0, (short) 13, 'a'};
            for (int checkedArg = 16; checkedArg < args.length; checkedArg++) {
                String name = "invoke_high_arity" + (checkedArg - 16);
                MemoryAddress ma = LOOKUP.lookup(name).get();
                FunctionDescriptor fd = baseFD.withReturnLayout(baseFD.argumentLayouts().get(checkedArg));
                Object expected = args[checkedArg];
                tests.add(name, abi.downcallHandle(ma, fd), expected, args);
            }
        }

        return testsList.toArray(Object[][]::new);
    }
}
