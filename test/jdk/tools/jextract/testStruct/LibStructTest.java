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

import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static test.jextract.struct.struct_h.*;

/*
 * @test
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -l Struct -t test.jextract.struct -- struct.h
 * @run testng/othervm -Dforeign.restricted=permit LibStructTest
 */
public class LibStructTest {
    @Test
    public void testMakePoint() {
        try (var seg = makePoint(42, -39)) {
            var addr = seg.baseAddress();
            assertEquals(Point.x$get(addr), 42);
            assertEquals(Point.y$get(addr), -39);
        }
    }

    @Test
    public void testAllocate() {
        try (var seg = Point.allocate()) {
            var addr = seg.baseAddress();
            Point.x$set(addr, 56);
            Point.y$set(addr, 65);
            assertEquals(Point.x$get(addr), 56);
            assertEquals(Point.y$get(addr), 65);
        }
    }

    @Test
    public void testAllocateArray() {
        try (var seg = Point.allocateArray(3)) {
            var addr = seg.baseAddress();
            for (int i = 0; i < 3; i++) {
                Point.x$set(addr, i, 56 + i);
                Point.y$set(addr, i, 65 + i);
            }
            for (int i = 0; i < 3; i++) {
                assertEquals(Point.x$get(addr, i), 56 + i);
                assertEquals(Point.y$get(addr, i), 65 + i);
            }
        }
    }

    private static void checkField(GroupLayout group, String fieldName, MemoryLayout expected) {
        assertEquals(group.select(PathElement.groupElement(fieldName)), expected.withName(fieldName));
    }

    @Test
    public void testFieldTypes() {
        GroupLayout g = (GroupLayout)AllTypes.$LAYOUT();
        checkField(g, "sc", CSupport.C_CHAR);
        checkField(g, "uc", CSupport.C_CHAR);
        checkField(g, "s",  CSupport.C_SHORT);
        checkField(g, "us", CSupport.C_SHORT);
        checkField(g, "i",  CSupport.C_INT);
        checkField(g, "ui", CSupport.C_INT);
        checkField(g, "l",  CSupport.C_LONG);
        checkField(g, "ul", CSupport.C_LONG);
        checkField(g, "ll", CSupport.C_LONGLONG);
        checkField(g, "ull",CSupport.C_LONGLONG);
        checkField(g, "f",  CSupport.C_FLOAT);
        checkField(g, "d",  CSupport.C_DOUBLE);
        checkField(g, "ld", CSupport.C_LONGDOUBLE);
    }
}
