/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.NativeScope;
import org.testng.annotations.Test;
import test.jextract.test8246341.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static test.jextract.test8246341.test8246341_h.*;
import static jdk.incubator.foreign.CSupport.*;

/*
 * @test
 * @library ..
 * @modules jdk.incubator.jextract
 * @modules jdk.incubator.foreign
 * @bug 8246341
 * @summary jextract should generate Cpointer utilities class
 * @run driver JtregJextract -l Test8246341 -t test.jextract.test8246341 -- test8246341.h
 * @run testng/othervm -Dforeign.restricted=permit LibTest8246341Test
 */
public class LibTest8246341Test {
    private static MemoryAddress getPointerAt(MemoryAddress addr, int element) {
        return MemoryAccess.getAddress(addr, element*C_POINTER.byteSize());
    }

    public static MemoryAddress allocatePointer(MemoryAddress value, NativeScope scope) {
        var addr = scope.allocate(C_POINTER);
        var handle = C_POINTER.varHandle(long.class);
        handle.set(addr, value.toRawLongValue());
        return addr;
    }

    @Test
    public void testPointerArray() {
        boolean[] callbackCalled = new boolean[1];
        try (var callback = func$callback.allocate((argc, argv) -> {
            callbackCalled[0] = true;
            var addr = RuntimeHelper.asArrayRestricted(argv, C_POINTER, argc);
            assertEquals(argc, 4);
            assertEquals(toJavaStringRestricted(getPointerAt(addr, 0)), "java");
            assertEquals(toJavaStringRestricted(getPointerAt(addr, 1)), "python");
            assertEquals(toJavaStringRestricted(getPointerAt(addr, 2)), "javascript");
            assertEquals(toJavaStringRestricted(getPointerAt(addr, 3)), "c++");
        })) {
            func(callback.baseAddress());
        }
        assertTrue(callbackCalled[0]);
    }

    @Test
    public void testPointerAllocate() {
        try (var scope = NativeScope.boundedScope(C_POINTER.byteSize())) {
            var addr = allocatePointer(MemoryAddress.NULL, scope);
            fillin(addr);
            assertEquals(toJavaStringRestricted(getPointerAt(addr, 0)), "hello world");
        }
    }
}
