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

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.SystemABI;
import jdk.incubator.foreign.SystemABI.Type;
import org.testng.annotations.Test;

import static jdk.incubator.foreign.SystemABI.NATIVE_TYPE;
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
            assertEquals(CPoint.x$get(addr), 42);
            assertEquals(CPoint.y$get(addr), -39);
        }
    }

    @Test
    public void testAllocate() {
        try (var seg = CPoint.allocate()) {
            var addr = seg.baseAddress();
            CPoint.x$set(addr, 56);
            CPoint.y$set(addr, 65);
            assertEquals(CPoint.x$get(addr), 56);
            assertEquals(CPoint.y$get(addr), 65);
        }
    }

    @Test
    public void testAllocateArray() {
        try (var seg = CPoint.allocateArray(3)) {
            var addr = seg.baseAddress();
            for (int i = 0; i < 3; i++) {
                CPoint.x$set(addr, i, 56 + i);
                CPoint.y$set(addr, i, 65 + i);
            }
            for (int i = 0; i < 3; i++) {
                assertEquals(CPoint.x$get(addr, i), 56 + i);
                assertEquals(CPoint.y$get(addr, i), 65 + i);
            }
        }
    }

    private static void checkFieldABIType(GroupLayout group, String fieldName, Type expected) {
        assertEquals(group.select(PathElement.groupElement(fieldName)).attribute(NATIVE_TYPE)
                                                                      .map(SystemABI.Type.class::cast)
                                                                      .orElseThrow(), expected);
    }

    @Test
    public void testFieldTypes() {
        GroupLayout g = (GroupLayout)CAllTypes.$LAYOUT();
        checkFieldABIType(g, "sc",  Type.SIGNED_CHAR);
        checkFieldABIType(g, "uc",  Type.UNSIGNED_CHAR);
        checkFieldABIType(g, "s",   Type.SHORT);
        checkFieldABIType(g, "us",  Type.UNSIGNED_SHORT);
        checkFieldABIType(g, "i",   Type.INT);
        checkFieldABIType(g, "ui",  Type.UNSIGNED_INT);
        checkFieldABIType(g, "l",   Type.LONG);
        checkFieldABIType(g, "ul",  Type.UNSIGNED_LONG);
        checkFieldABIType(g, "ll",  Type.LONG_LONG);
        checkFieldABIType(g, "ull", Type.UNSIGNED_LONG_LONG);
        checkFieldABIType(g, "f", Type.FLOAT);
        checkFieldABIType(g, "d", Type.DOUBLE);
        checkFieldABIType(g, "ld", Type.LONG_DOUBLE);
    }
}
