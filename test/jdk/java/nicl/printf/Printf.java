/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nicl.Libraries;
import java.nicl.Scope;
import java.nicl.types.Pointer;

/**
 * @test
 * @run main Printf
 */
public class Printf {
    /**
     * Simple printf example using normal method invocation
     */
    public void testPrintf() {
        stdio i = Libraries.bind(MethodHandles.lookup(), stdio.class);

        // Create a scope to allocate things in
        Scope scope = Scope.newNativeScope();

        // Convert the Java string to a native one
        // Basically uses Unsafe to allocate memory and copy the bytes
        Pointer<Byte> fmt = scope.toCString("Hello, World!\n");

        // Call printf
        i.printf(fmt);

        // Make sure output is not stuck in buffer
        i.fflush(null);
    }


    /**
     * Simple printf example using method handle
     */
    public void testPrintfUsingMethodHandle() throws Throwable {
        stdio i = Libraries.bind(MethodHandles.lookup(), stdio.class);

        // Create a MH for the printf function
        MethodHandle printf = MethodHandles.publicLookup().findVirtual(stdio.class, "printf", MethodType.methodType(int.class, Pointer.class, Object[].class));

        // Create a scope to allocate things in
        Scope scope = Scope.newNativeScope();

        // Convert the Java string to a native one
        Pointer<Byte> fmt = scope.toCString("Hello, %d!\n");

        // Call printf
        printf.invoke(i, fmt, 4711);

        // Make sure output is not stuck in buffer
        i.fflush(null);
    }


    /**
     * printf with an integer arg
     */
    public void testPrintfWithIntegerArg() throws Throwable {
        stdio i = Libraries.bind(MethodHandles.lookup(), stdio.class);

        // Lookup a MH for the printf function
        MethodHandle printf = Util.lookup(Util.Function.PRINTF);

        // Create a scope to allocate things in
        Scope scope = Scope.newNativeScope();

        Pointer<Byte> fmt = scope.toCString("Hello, %d!\n");
        printf.invoke(i, fmt, 4711);

        // Make sure output is not stuck in buffer
        i.fflush(null);
    }

    /**
     * printf with a string argument
     */
    public void testPrintfWithStringArg() throws Throwable {
        stdio i = Libraries.bind(MethodHandles.lookup(), stdio.class);

        Scope scope = Scope.newNativeScope();

        // Lookup a MH for the printf function
        MethodHandle printf = Util.lookup(Util.Function.PRINTF);

        Pointer<Byte> fmt = scope.toCString("Hello, %s!\n");
        Pointer<Byte> arg = scope.toCString("World");

        printf.invoke(i, fmt, arg);

        // Make sure output is not stuck in buffer
        i.fflush(null);
    }

    public static void main(String[] args) throws Throwable {
        Printf h = new Printf();

        h.testPrintf();
        h.testPrintfUsingMethodHandle();
        h.testPrintfWithIntegerArg();
        h.testPrintfWithStringArg();
    }
}
