/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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
 * @enablePreview
 * @library ../
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @run testng/othervm --enable-native-access=ALL-UNNAMED TestNormalize
 */

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BOOLEAN;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;
import static org.testng.Assert.assertEquals;

// test normalization of smaller than int primitive types
public class TestNormalize extends NativeTestHelper {

    private static final Linker LINKER = Linker.nativeLinker();
    private static final MethodHandle MH_BOOLEAN;
    private static final MethodHandle MH_BYTE;
    private static final MethodHandle MH_SHORT;
    private static final MethodHandle MH_CHAR;

    static {
        System.loadLibrary("Normalize");

        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MH_BOOLEAN = lookup.findStatic(TestNormalize.class, "identity", MethodType.methodType(boolean.class, boolean.class));
            MH_BYTE = lookup.findStatic(TestNormalize.class, "identity", MethodType.methodType(byte.class, byte.class));
            MH_SHORT = lookup.findStatic(TestNormalize.class, "identity", MethodType.methodType(short.class, short.class));
            MH_CHAR = lookup.findStatic(TestNormalize.class, "identity", MethodType.methodType(char.class, char.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Test(dataProvider = "cases")
    public void testNormalize(String targetName, ValueLayout layout, Object testValue, MethodHandle upcallTarget) throws Throwable {
        FunctionDescriptor upcallDesc = FunctionDescriptor.of(layout, layout);
        FunctionDescriptor downcallDesc = upcallDesc.insertArgumentLayouts(0, ADDRESS); // upcall stub

        MemorySegment target = findNativeOrThrow(targetName);
        MethodHandle downcallHandle = LINKER.downcallHandle(target, downcallDesc);
        Object result;
        try (MemorySession session = MemorySession.openConfined()) {
            MemorySegment upcallStub = LINKER.upcallStub(upcallTarget, upcallDesc, session);

            result = downcallHandle.invoke(upcallStub, testValue);
        }
        assertEquals(result, testValue);
    }

    // use explicit functions to avoid LambdaForm erasure
    public static boolean identity(boolean b) {
        return b;
    }
    public static byte identity(byte b) {
        return b;
    }
    public static char identity(char c) {
        return c;
    }
    public static short identity(short s) {
        return s;
    }

    @DataProvider
    public static Object[][] cases() {
        return new Object[][] {
                { "test_char", JAVA_BOOLEAN, true, MH_BOOLEAN },
                { "test_char", JAVA_BYTE, (byte) 42, MH_BYTE },
                { "test_short", JAVA_SHORT, (short) 42, MH_SHORT },
                { "test_short", JAVA_CHAR, 'a', MH_CHAR }
        };
    }
}
