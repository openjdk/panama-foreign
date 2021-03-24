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
import jdk.incubator.foreign.ResourceScope;
import org.testng.annotations.Test;
import test.jextract.test8246341.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static test.jextract.test8246341.test8246341_h.*;
import static jdk.incubator.foreign.CLinker.*;

/*
 * @test id=classes
 * @bug 8246341
 * @summary jextract should generate Cpointer utilities class
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -l Test8246341 -t test.jextract.test8246341 -- test8246341.h
 * @run testng/othervm -Dforeign.restricted=permit LibTest8246341Test
 */
/*
 * @test id=sources
 * @bug 8246341
 * @summary jextract should generate Cpointer utilities class
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextractSources -l Test8246341 -t test.jextract.test8246341 -- test8246341.h
 * @run testng/othervm -Dforeign.restricted=permit LibTest8246341Test
 */
public class LibTest8246341Test {
    @Test
    public void testPointerArray() {
        boolean[] callbackCalled = new boolean[1];
        try (ResourceScope scope = ResourceScope.ofConfined()) {
            var callback = func$callback.allocate((argc, argv) -> {
                callbackCalled[0] = true;
                var addr = argv.asSegmentRestricted(C_POINTER.byteSize() * argc, scope);
                assertEquals(argc, 4);
                assertEquals(toJavaStringRestricted(MemoryAccess.getAddressAtIndex(addr, 0)), "java");
                assertEquals(toJavaStringRestricted(MemoryAccess.getAddressAtIndex(addr, 1)), "python");
                assertEquals(toJavaStringRestricted(MemoryAccess.getAddressAtIndex(addr, 2)), "javascript");
                assertEquals(toJavaStringRestricted(MemoryAccess.getAddressAtIndex(addr, 3)), "c++");
            }, scope);
            func(callback);
        }
        assertTrue(callbackCalled[0]);
    }

    @Test
    public void testPointerAllocate() {
        try (var scope = NativeScope.boundedScope(C_POINTER.byteSize())) {
            var addr = scope.allocate(C_POINTER);
            MemoryAccess.setAddress(addr, MemoryAddress.NULL);
            fillin(addr);
            assertEquals(toJavaStringRestricted(MemoryAccess.getAddress(addr)), "hello world");
        }
    }
}
