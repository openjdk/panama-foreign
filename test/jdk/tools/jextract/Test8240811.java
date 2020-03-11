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

import java.nio.file.Path;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.SystemABI.Type;
import org.testng.annotations.Test;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @modules jdk.incubator.jextract
 * @build JextractToolRunner
 * @bug 8240811
 * @summary jextract generates non-compilable code for name collision between a struct and a global variable
 * @run testng Test8240811
 */
public class Test8240811 extends JextractToolRunner {
    @Test
    public void testNameCollision() {
        Path nameCollisionOutput = getOutputFilePath("name_collision_gen");
        Path nameCollisionH = getInputFilePath("name_collision.h");
        run("-d", nameCollisionOutput.toString(), nameCollisionH.toString()).checkSuccess();
        try(Loader loader = classLoader(nameCollisionOutput)) {
            Class<?> cls = loader.loadClass("name_collision_h");
            assertNotNull(cls);

            // check foo layout
            MemoryLayout fooLayout = findLayout(cls, "foo");
            assertNotNull(fooLayout);
            assertTrue(((GroupLayout)fooLayout).isStruct());
            checkFieldABIType(fooLayout, "x",  Type.INT);
            checkFieldABIType(fooLayout, "y",  Type.INT);
            checkFieldABIType(fooLayout, "z",  Type.INT);

            MemoryLayout fooVarLayout = findLayout(cls, "var$foo");
            assertNotNull(fooVarLayout);

            // check foo2 layout
            MemoryLayout foo2Layout = findLayout(cls, "foo2");
            assertNotNull(foo2Layout);
            assertTrue(((GroupLayout)foo2Layout).isUnion());
            checkFieldABIType(foo2Layout, "i",  Type.INT);
            checkFieldABIType(foo2Layout, "l",  Type.LONG);

            MemoryLayout foo2VarLayout = findLayout(cls, "var$foo2");
            assertNotNull(foo2VarLayout);

            MemoryLayout barVarLayout = findLayout(cls, "bar");
            assertNotNull(barVarLayout);

            // check bar layout
            MemoryLayout barLayout = findLayout(cls, "struct$bar");
            assertNotNull(barLayout);
            assertTrue(((GroupLayout)barLayout).isStruct());
            checkFieldABIType(barLayout, "f1",  Type.FLOAT);
            checkFieldABIType(barLayout, "f2",  Type.FLOAT);

            MemoryLayout bar2VarLayout = findLayout(cls, "bar2");
            assertNotNull(bar2VarLayout);

            // check bar layout
            MemoryLayout bar2Layout = findLayout(cls, "union$bar2");
            assertNotNull(bar2Layout);
            assertTrue(((GroupLayout)bar2Layout).isUnion());
            checkFieldABIType(bar2Layout, "f",  Type.FLOAT);
            checkFieldABIType(bar2Layout, "d",  Type.DOUBLE);
        } finally {
            deleteDir(nameCollisionOutput);
        }
    }
}
