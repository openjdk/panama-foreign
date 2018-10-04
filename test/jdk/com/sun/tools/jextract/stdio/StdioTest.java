/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
import java.nio.file.Paths;
import java.util.spi.ToolProvider;
import org.testng.annotations.Test;

/*
 * @test
 * @run testng StdioTest
 */
public class StdioTest {
    private static final ToolProvider JEXTRACT = ToolProvider.findFirst("jextract")
            .orElseThrow(() ->
                    new RuntimeException("jextract tool not found")
            );

    @Test
    public void stdioJextractTest() throws Exception {
        Path outputDir = Paths.get(System.getProperty("test.classes", "."),  "stdio");
        outputDir.toFile().mkdirs();
        int result = JEXTRACT.run(System.out, System.err, new String[] {
            "-d", outputDir.toString(), "/usr/include/stdio.h" });
        if (result != 0) {
            throw new RuntimeException(JEXTRACT.name() + " returns non-zero value");
        }
    }
}
