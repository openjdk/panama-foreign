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
import jdk.incubator.foreign.SystemABI.Type;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static test.jextract.struct.struct_h.*;

/*
 * @test
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -l Struct -t test.jextract.struct -- struct.h
 * @run testng LibStructTest
 */
public class LibStructTest {
    @Test
    public void testMakePoint() {
        try (var seg = makePoint(42, -39)) {
            assertEquals(Point$x$get(seg), 42);
            assertEquals(Point$y$get(seg), -39);
        }
    }

    @Test
    public void testFieldTypes() {
         GroupLayout g = (GroupLayout)AllTypes$LAYOUT;
         int fieldCount = 0;
         for (var ml : g.memberLayouts()) {
             var type = ml.abiType().orElse(null);
             if (type == null) {
                 // ignore paddings
                 continue;
             }
             switch (ml.name().get()) {
                 case "sc":
                     assertEquals(type, Type.SIGNED_CHAR);
                     break;
                 case "uc":
                     assertEquals(type, Type.UNSIGNED_CHAR);
                     break;
                 case "s":
                     assertEquals(type, Type.SHORT);
                     break;
                 case "us":
                     assertEquals(type, Type.UNSIGNED_SHORT);
                     break;
                 case "i":
                     assertEquals(type, Type.INT);
                     break;
                 case "ui":
                     assertEquals(type, Type.UNSIGNED_INT);
                     break;
                 case "l":
                     assertEquals(type, Type.LONG);
                     break;
                 case "ul":
                     assertEquals(type, Type.UNSIGNED_LONG);
                     break;
                 case "ll":
                     assertEquals(type, Type.LONG_LONG);
                     break;
                 case "ull":
                     assertEquals(type, Type.UNSIGNED_LONG_LONG);
                     break;
             }
             fieldCount++;
         }
         assertEquals(fieldCount, 10);
    }
}
