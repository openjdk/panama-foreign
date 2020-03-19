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

import org.testng.annotations.Test;
import java.nio.file.Path;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.SystemABI.Type;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @modules jdk.incubator.jextract
 * @build JextractToolRunner
 * @run testng/othervm -Djdk.incubator.foreign.Foreign=permit UniondeclTest
 */
public class UniondeclTest extends JextractToolRunner {
    @Test
    public void unionDecl() {
        Path uniondeclOutput = getOutputFilePath("uniondecl.h");
        Path uniondeclH = getInputFilePath("uniondecl.h");
        run("-d", uniondeclOutput.toString(), uniondeclH.toString()).checkSuccess();
        try(Loader loader = classLoader(uniondeclOutput)) {
            Class<?> cls = loader.loadClass("uniondecl_h");
            // check a method for "void func(IntOrFloat*)"
            assertNotNull(findMethod(cls, "func", MemoryAddress.class));
            // check Point layout
            GroupLayout intOrFloatLayout = (GroupLayout)findLayout(cls, "IntOrFloat");
            assertNotNull(intOrFloatLayout);
            assertTrue(intOrFloatLayout.isUnion());
            checkFieldABIType(intOrFloatLayout, "i",  Type.INT);
            checkFieldABIType(intOrFloatLayout, "f",  Type.FLOAT);
        } finally {
            deleteDir(uniondeclOutput);
        }
    }
}
