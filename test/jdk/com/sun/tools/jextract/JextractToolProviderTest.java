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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.spi.ToolProvider;

/*
 * @test
 * @modules jdk.jextract
 * @build JextractToolProviderTest
 * @run main JextractToolProviderTest
 */
public class JextractToolProviderTest {
    private static final ToolProvider JEXTRACT_TOOL = ToolProvider.findFirst("jextract")
        .orElseThrow(() ->
            new RuntimeException("jextract tool not found")
        );

    private static String testSrcDir = System.getProperty("test.src", ".");
    private static String testClassesDir = System.getProperty("test.classes", ".");

    private static String getFilePath(String dir, String fileName) {
        return Paths.get(dir, fileName).toAbsolutePath().toString();
    }

    private static String getInputFilePath(String fileName) {
        return getFilePath(testSrcDir, fileName);
    }

    private static String getOutputFilePath(String fileName) {
        return getFilePath(testClassesDir, fileName);
    }

    private static int checkJextract(String expected, String... options) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);

        int result = JEXTRACT_TOOL.run(pw, pw, options);
        String output = writer.toString();
        System.err.println(output);
        if (expected != null) {
            if (!output.contains(expected)) {
                throw new AssertionError("Output does not contain " + expected);
            }
        }

        return result;
    }

    private static void checkSuccess(String expected, String... options) {
        int result = checkJextract(null, options);
        if (result != 0) {
            throw new AssertionError("Sucess excepted, failed: " + result);
        }
    }

    private static void checkFailure(String expected, String... options) {
        int result = checkJextract(expected, options);
        if (result == 0) {
            throw new AssertionError("Failure excepted, succeeded!");
        }
    }

    public static void main(String[] args) throws Exception {
        checkFailure(null); // no options
        checkSuccess(null, "--help");
        checkSuccess(null, "-h");
        checkSuccess(null, "-?");

        // error for non-existent header file
        checkFailure("Cannot open header file", "--dry-run", getInputFilePath("non_existent.h"));

        // only dry-run, don't produce any output
        String simpleJar = getOutputFilePath("simple.jar");
        checkSuccess(null, "--dry-run", getInputFilePath("simple.h"));
        if (Files.isRegularFile(Paths.get(simpleJar))) {
            throw new AssertionError(simpleJar + "output file should not have been produced");
        }
        // simple output file check
        checkSuccess(null, "-o", simpleJar, getInputFilePath("simple.h"));
        if (!Files.isRegularFile(Paths.get(simpleJar))) {
            throw new AssertionError(simpleJar + "output file not produced");
        }
    }
}
