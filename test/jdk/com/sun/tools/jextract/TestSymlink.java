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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/*
 * @test
 * @requires os.family != "windows"
 * @bug 8224013
 * @modules jdk.jextract
 * @build JextractToolRunner
 * @run testng/othervm -Duser.language=en TestSymlink
 */
public class TestSymlink extends JextractToolRunner {

    // Not running on Windows, since creating symlink requires Admin access.

    @Test
    public void testTargetDir() throws IOException {
        Path src = getOutputFilePath("gensrc");
        Path realTarget = getOutputFilePath("realGenSrc");
        Files.createDirectory(realTarget);
        Files.createSymbolicLink(src, realTarget);
        run("--src-dump-dir", src.toString(), "-t", "com.acme",
                getInputFilePath("simple.h").toString()).checkSuccess();
        try {
            assertTrue(Files.isRegularFile(src.resolve("com").resolve("acme").resolve(staticForwarderName("simple.h") + ".java")));
        } finally {
            deleteFile(src);
            deleteDir(realTarget);
        }
    }
}
