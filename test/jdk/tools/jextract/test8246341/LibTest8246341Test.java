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

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import jdk.incubator.foreign.NativeAllocationScope;
import org.testng.annotations.Test;
import test.jextract.test8246341.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static test.jextract.test8246341.test8246341_h.*;

/*
 * @test
 * @library ..
 * @modules jdk.incubator.jextract
 * @bug 8246341
 * @summary jextract should generate simple allocation, access API for C primitive types
 * @run driver JtregJextract -l Test8246341 -t test.jextract.test8246341 -- test8246341.h
 * @run testng/othervm -Dforeign.restricted=permit LibTest8246341Test
 */
public class LibTest8246341Test {
    @Test
    public void test() {
        boolean[] callbackCalled = new boolean[1];
        try (var callback = func$callback.allocate((argc, argv) -> {
            callbackCalled[0] = true;
            var addr = Cpointer.asArray(argv, argc);
            assertEquals(argc, 4);
            assertEquals(Cstring.toJavaString(Cpointer.get(addr, 0)), "java");
            assertEquals(Cstring.toJavaString(Cpointer.get(addr, 1)), "python");
            assertEquals(Cstring.toJavaString(Cpointer.get(addr, 2)), "javascript");
            assertEquals(Cstring.toJavaString(Cpointer.get(addr, 3)), "c++");
        })) {
            func(callback.baseAddress());
        }
        assertTrue(callbackCalled[0]);
    }
}
