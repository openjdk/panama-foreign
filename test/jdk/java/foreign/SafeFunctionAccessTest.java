/*
 *  Copyright (c) 2021, 2022, Oracle and/or its affiliates. All rights reserved.
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
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @run testng/othervm --enable-native-access=ALL-UNNAMED SafeFunctionAccessTest
 */

import java.lang.foreign.Addressable;
import java.lang.foreign.CLinker;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.NativeSymbol;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ResourceScope;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import java.lang.foreign.VaList;
import org.testng.annotations.*;

import static org.testng.Assert.*;

public class SafeFunctionAccessTest extends NativeTestHelper {
    static {
        System.loadLibrary("SafeAccess");
    }

    static MemoryLayout POINT = MemoryLayout.structLayout(
            C_INT, C_INT
    );

    @Test(expectedExceptions = IllegalStateException.class)
    public void testClosedStruct() throws Throwable {
        MemorySegment segment;
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            segment = MemorySegment.allocateNative(POINT, scope);
        }
        assertFalse(segment.scope().isAlive());
        MethodHandle handle = CLinker.systemCLinker().downcallHandle(
                findNativeOrThrow(SafeFunctionAccessTest.class,"struct_func"),
                FunctionDescriptor.ofVoid(POINT));

        handle.invokeExact(segment);
    }

    @Test
    public void testClosedStructAddr_6() throws Throwable {
        MethodHandle handle = CLinker.systemCLinker().downcallHandle(
                findNativeOrThrow(SafeFunctionAccessTest.class, "addr_func_6"),
                FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER));
        for (int i = 0 ; i < 6 ; i++) {
            MemorySegment[] segments = new MemorySegment[]{
                    MemorySegment.allocateNative(POINT, ResourceScope.newImplicitScope()),
                    MemorySegment.allocateNative(POINT, ResourceScope.newImplicitScope()),
                    MemorySegment.allocateNative(POINT, ResourceScope.newImplicitScope()),
                    MemorySegment.allocateNative(POINT, ResourceScope.newImplicitScope()),
                    MemorySegment.allocateNative(POINT, ResourceScope.newImplicitScope()),
                    MemorySegment.allocateNative(POINT, ResourceScope.newImplicitScope())
            };
            // check liveness
            segments[i].scope().close();
            for (int j = 0 ; j < 6 ; j++) {
                if (i == j) {
                    assertFalse(segments[j].scope().isAlive());
                } else {
                    assertTrue(segments[j].scope().isAlive());
                }
            }
            try {
                handle.invokeWithArguments(segments);
                fail();
            } catch (IllegalStateException ex) {
                assertTrue(ex.getMessage().contains("Already closed"));
            }
            for (int j = 0 ; j < 6 ; j++) {
                if (i != j) {
                    segments[j].scope().close(); // should succeed!
                }
            }
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testClosedVaList() throws Throwable {
        VaList list;
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            list = VaList.make(b -> b.addVarg(C_INT, 42), scope);
        }
        assertFalse(list.scope().isAlive());
        MethodHandle handle = CLinker.systemCLinker().downcallHandle(
                findNativeOrThrow(SafeFunctionAccessTest.class, "addr_func"),
                FunctionDescriptor.ofVoid(C_POINTER));

        handle.invokeExact((Addressable)list);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testClosedUpcall() throws Throwable {
        NativeSymbol upcall;
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            MethodHandle dummy = MethodHandles.lookup().findStatic(SafeFunctionAccessTest.class, "dummy", MethodType.methodType(void.class));
            upcall = CLinker.systemCLinker().upcallStub(dummy, FunctionDescriptor.ofVoid(), scope);
        }
        assertFalse(upcall.scope().isAlive());
        MethodHandle handle = CLinker.systemCLinker().downcallHandle(
                findNativeOrThrow(SafeFunctionAccessTest.class, "addr_func"),
                FunctionDescriptor.ofVoid(C_POINTER));

        handle.invokeExact((Addressable)upcall);
    }

    static void dummy() { }

    @Test
    public void testClosedVaListCallback() throws Throwable {
        MethodHandle handle = CLinker.systemCLinker().downcallHandle(
                findNativeOrThrow(SafeFunctionAccessTest.class, "addr_func_cb"),
                FunctionDescriptor.ofVoid(C_POINTER, C_POINTER));

        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            VaList list = VaList.make(b -> b.addVarg(C_INT, 42), scope);
            handle.invoke(list, scopeChecker(scope));
        }
    }

    @Test
    public void testClosedStructCallback() throws Throwable {
        MethodHandle handle = CLinker.systemCLinker().downcallHandle(
                findNativeOrThrow(SafeFunctionAccessTest.class, "addr_func_cb"),
                FunctionDescriptor.ofVoid(C_POINTER, C_POINTER));

        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            MemorySegment segment = MemorySegment.allocateNative(POINT, scope);
            handle.invoke(segment, scopeChecker(scope));
        }
    }

    @Test
    public void testClosedUpcallCallback() throws Throwable {
        MethodHandle handle = CLinker.systemCLinker().downcallHandle(
                findNativeOrThrow(SafeFunctionAccessTest.class, "addr_func_cb"),
                FunctionDescriptor.ofVoid(C_POINTER, C_POINTER));

        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            MethodHandle dummy = MethodHandles.lookup().findStatic(SafeFunctionAccessTest.class, "dummy", MethodType.methodType(void.class));
            NativeSymbol upcall = CLinker.systemCLinker().upcallStub(dummy, FunctionDescriptor.ofVoid(), scope);
            handle.invoke(upcall, scopeChecker(scope));
        }
    }

    NativeSymbol scopeChecker(ResourceScope scope) {
        try {
            MethodHandle handle = MethodHandles.lookup().findStatic(SafeFunctionAccessTest.class, "checkScope",
                    MethodType.methodType(void.class, ResourceScope.class));
            handle = handle.bindTo(scope);
            return CLinker.systemCLinker().upcallStub(handle, FunctionDescriptor.ofVoid(), ResourceScope.newImplicitScope());
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    static void checkScope(ResourceScope scope) {
        try {
            scope.close();
            fail("Scope closed unexpectedly!");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("kept alive")); //if acquired, fine
        }
    }
}
