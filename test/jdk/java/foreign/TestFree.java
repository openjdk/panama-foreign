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
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

/*
 * @test
 * @bug 8248421
 * @summary CSupport should have a way to free memory allocated outside Java
 * @run testng/othervm -Dforeign.restricted=permit TestFree
 */

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ForeignLinker;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import static jdk.incubator.foreign.CSupport.*;
import static org.testng.Assert.assertEquals;

public class TestFree {
    private static MemorySegment asArrayRestricted(MemoryAddress addr, MemoryLayout layout, int numElements) {
        return MemorySegment.ofNativeRestricted(addr, numElements * layout.byteSize(),
               Thread.currentThread(), null, null);
    }

    public void test() throws Throwable {
        LibraryLookup lib = LibraryLookup.ofDefault();
        ForeignLinker abi = getSystemLinker();
        MethodHandle malloc = getSystemLinker().downcallHandle(lib.lookup("malloc"),
                    MethodType.methodType(void.class, MemoryAddress.class),
                    FunctionDescriptor.of(C_POINTER, C_INT));
        String str = "hello world";
        MemoryAddress addr = (MemoryAddress)malloc.invokeExact(str.length() + 1);
        MemorySegment seg = asArrayRestricted(addr, C_CHAR, str.length() + 1);
        seg.copyFrom(MemorySegment.ofArray(str.getBytes()));
        MemoryAccess.setByteAtOffset(seg, str.length(), (byte)0);
        assertEquals(str, toJavaString(seg));
        freeMemoryRestricted(addr);
    }
}
