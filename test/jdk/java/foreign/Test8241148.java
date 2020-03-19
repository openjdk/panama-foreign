/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
import java.lang.invoke.MethodType;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.SystemABI;
import jdk.incubator.foreign.unsafe.ForeignUnsafe;

import org.testng.annotations.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static jdk.incubator.foreign.MemoryLayouts.*;
import static org.testng.Assert.*;

/*
 * @test
 * @bug 8241148
 * @summary need a way to create MemorySegment with contents from java String and a way to read a C char* as java String
 * @modules jdk.incubator.foreign/jdk.incubator.foreign.unsafe
 *          jdk.incubator.foreign/jdk.internal.foreign
 *          jdk.incubator.foreign/jdk.internal.foreign.abi
 * @run testng Test8241148
 */
@Test
public class Test8241148 {
    private final static MethodHandle getenv;
    private final static MethodHandle strlen;

    static {
        try {
            SystemABI abi = SystemABI.getInstance();
            LibraryLookup lookup = LibraryLookup.ofDefault();

            getenv = abi.downcallHandle(lookup.lookup("getenv"),
                    MethodType.methodType(MemoryAddress.class, MemoryAddress.class),
                    FunctionDescriptor.of(C_POINTER, C_POINTER));

            strlen = abi.downcallHandle(lookup.lookup("strlen"),
                    MethodType.methodType(int.class, MemoryAddress.class),
                    FunctionDescriptor.of(C_INT, C_POINTER));
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Test
    public void test() throws Throwable {
        try (var seg = ForeignUnsafe.toCString("java")) {
            assertEquals((int) strlen.invoke(seg.baseAddress()), 4);
        }
        try (var pathSeg = ForeignUnsafe.toCString("PATH")) {
            var path = (MemoryAddress) getenv.invoke(pathSeg.baseAddress());
            if (!path.equals(NULL)) {
                int len = (int) strlen.invoke(path);
                var pathStr = ForeignUnsafe.toJavaString(path);
                assertEquals(pathStr.length(), len);
                System.out.println("PATH = " + pathStr);
                assertEquals(pathStr, System.getenv("PATH"));
            }
        }
    }
}
