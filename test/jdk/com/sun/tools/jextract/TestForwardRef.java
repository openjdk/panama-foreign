/*
 *  Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

/*
 * @test
 * @library ..
 * @modules jdk.jextract
 * @build TestForwardRef
 *
 * @run testng/othervm TestForwardRef
 */

import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.testng.Assert.*;

public class TestForwardRef extends JextractToolRunner {

    @Test
    public void testForwardRefSameFile() {
        Path forwardRefJar = getOutputFilePath("forwardRef.jar");
        deleteFile(forwardRefJar);
        Path forwardRefH = getInputFilePath("forwardRef.h");
        //no duplicate warnings should be generated here!
        run("-o", forwardRefJar.toString(), forwardRefH.toString())
                .checkMatchesOutput("^$")
                .checkSuccess();
        try (Loader loader = classLoader(forwardRefJar)) {
            Class<?> cls = loader.loadClass("forwardRef");
            Method getterS = findGlobalVariableGet(cls, "s"); //check that we can load the globals's type correctly
            Class<?> structS = getterS.getReturnType();
            assertEquals(structS.getDeclaredClasses().length, 0); //check no spurious decl in forward ref
            assertNotNull(findStructFieldGet(structS, "p"));
        } finally {
            deleteFile(forwardRefJar);
        }
    }
}
