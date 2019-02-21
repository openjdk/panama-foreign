/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.spi.ToolProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/*
 * @test
 * @bug 8219442
 * @summary jextract does not generate InnerClasses attribute properly
 * @library ..
 * @run testng InnerClassTest
 */
public class InnerClassTest extends JextractToolRunner {
    private static final ToolProvider JAVAP_TOOL = ToolProvider.findFirst("javap")
        .orElseThrow(() ->
            new RuntimeException("javap tool not found")
        );

    @Test
    public void test() throws IOException {
        Path testJar = getOutputFilePath("test8219442.jar");
        deleteFile(testJar);
        Path testH = getInputFilePath("test.h");
        run("-o", testJar.toString(),
            "-t", "test8219442", testH.toString()).checkSuccess();
        try {
            String text = getInnerClasses(testJar.toString(), "test8219442.test");
            assertTrue(text.indexOf("// Point=class test8219442/point$Point of class test8219442/point") != -1);

            text = getInnerClasses(testJar.toString(), "test8219442.point.Point3D");
            assertTrue(text.indexOf("// Point=class test8219442/point$Point of class test8219442/point") != -1);
            assertTrue(text.indexOf("// Point3D=class test8219442/point$Point3D of class test8219442/point") != -1);
        } finally {
            deleteFile(testJar);
        }
     }

     // return InnerClasses: section from javap output of the given class
     private String getInnerClasses(String jarFile, String className) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
            int ec = JAVAP_TOOL.run(ps, ps, "-verbose", "--class-path", jarFile, className);
            assertTrue(ec == 0);
            String javapOut = baos.toString();
            System.out.println(javapOut); // for test debugging
            return javapOut.substring(javapOut.lastIndexOf("InnerClasses:"));
        }
    }
}
