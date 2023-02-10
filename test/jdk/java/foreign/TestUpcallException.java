/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates. All rights reserved.
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
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64" | os.arch == "riscv64"
 * @library /test/lib
 * @build TestUpcallException
 *
 * @run testng/othervm/native
 *   --enable-native-access=ALL-UNNAMED
 *   TestUpcallException
 */

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class TestUpcallException extends UpcallTestHelper {

    @Test(dataProvider = "exceptionCases")
    public void testException(Class<?> target, boolean useSpec) throws InterruptedException, IOException {
        runInNewProcess(target, useSpec)
                .assertStdErrContains("Testing upcall exceptions");
    }

    @DataProvider
    public static Object[][] exceptionCases() {
        return new Object[][]{
            { VoidUpcallRunner.class,    false },
            { NonVoidUpcallRunner.class, false },
            { VoidUpcallRunner.class,    true  },
            { NonVoidUpcallRunner.class, true  }
        };
    }

    public static class VoidUpcallRunner extends ExceptionRunnerBase {
        public static void main(String[] args) throws Throwable {
            try (Arena arena = Arena.openConfined()) {
                MemorySegment stub = Linker.nativeLinker().upcallStub(VOID_TARGET, FunctionDescriptor.ofVoid(), arena.scope());
                downcallVoid.invoke(stub); // should call Shutdown.exit(1);
            }
        }
    }

    public static class NonVoidUpcallRunner extends ExceptionRunnerBase {
        public static void main(String[] args) throws Throwable {
            try (Arena arena = Arena.openConfined()) {
                MemorySegment stub = Linker.nativeLinker().upcallStub(INT_TARGET, FunctionDescriptor.of(C_INT, C_INT), arena.scope());
                downcallNonVoid.invoke(42, stub); // should call Shutdown.exit(1);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test(dataProvider = "uncaughtHandlerCases")
    public void testUncaughtExceptionHandlerOption(Class<?> target) throws InterruptedException, IOException {
        runInNewProcess(target, true)
                .assertStdOutContains("From uncaught exception handler");
    }

    @DataProvider
    public static Object[][] uncaughtHandlerCases() {
        return new Object[][]{
            { UncaughtHandlerOptionRunner.class },
            { UncaughtHandlerThreadRunner.class }
        };
    }

    public static class UncaughtHandlerOptionRunner extends VoidUpcallRunner {
        public static void main(String[] args) throws Throwable {
            try (Arena arena = Arena.openConfined()) {
                MemorySegment stub = Linker.nativeLinker().upcallStub(VOID_TARGET, FunctionDescriptor.ofVoid(),
                        arena.scope(), Linker.Option.uncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER));
                downcallVoid.invoke(stub);
            }
        }
    }

    public static class UncaughtHandlerThreadRunner extends VoidUpcallRunner {
        public static void main(String[] args) throws Throwable {
            Thread.currentThread().setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
            try (Arena arena = Arena.openConfined()) {
                MemorySegment stub = Linker.nativeLinker().upcallStub(VOID_TARGET, FunctionDescriptor.ofVoid(), arena.scope());
                downcallVoid.invoke(stub);
            }
        }
    }

    // where

    private static class ExceptionRunnerBase {
        static final MethodHandle downcallVoid;
        static final MethodHandle downcallNonVoid;
        static final MethodHandle VOID_TARGET;
        static final MethodHandle INT_TARGET;

        static final Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER
                = (thread, throwable) -> System.out.println("From uncaught exception handler");

        static {
                System.loadLibrary("TestUpcall");
            downcallVoid = Linker.nativeLinker().downcallHandle(
                findNativeOrThrow("f0_V__"),
                    FunctionDescriptor.ofVoid(C_POINTER)
            );
            downcallNonVoid = Linker.nativeLinker().downcallHandle(
                    findNativeOrThrow("f10_I_I_"),
                    FunctionDescriptor.of(C_INT, C_INT, C_POINTER)
            );
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                VOID_TARGET = lookup.findStatic(ExceptionRunnerBase.class, "throwException",
                        MethodType.methodType(void.class));
                INT_TARGET = lookup.findStatic(ExceptionRunnerBase.class, "throwException",
                        MethodType.methodType(int.class, int.class));
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        public static void throwException() {
            throw new RuntimeException("Testing upcall exceptions");
        }

        public static int throwException(int x) {
            throw new RuntimeException("Testing upcall exceptions");
        }
    }
}
