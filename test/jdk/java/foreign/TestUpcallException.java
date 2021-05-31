/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @library /test/lib
 * @modules jdk.incubator.foreign/jdk.internal.foreign
 * @build ThrowingUpcall TestUpcallException
 *
 * @run testng/othervm/native
 *   --enable-native-access=ALL-UNNAMED
 *   TestUpcallException
 */

import jdk.test.lib.Utils;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import static jdk.incubator.foreign.CLinker.ERR_UNCAUGHT_EXCEPTION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class TestUpcallException {

    @Test
    public void testExceptionInterpreted() throws InterruptedException, IOException {
        boolean useSpec = false;
        run(useSpec);
    }

    @Test
    public void testExceptionSpecialized() throws IOException, InterruptedException {
        boolean useSpec = true;
        run(useSpec);
    }

    private void run(boolean useSpec) throws IOException, InterruptedException {
        Process process = new ProcessBuilder()
            .command(
                Paths.get(Utils.TEST_JDK)
                     .resolve("bin")
                     .resolve("java")
                     .toAbsolutePath()
                     .toString(),
                "--add-modules", "jdk.incubator.foreign",
                "--enable-native-access=ALL-UNNAMED",
                "-Djava.library.path=" + System.getProperty("java.library.path"),
                "-Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=" + useSpec,
                "-cp", Utils.TEST_CLASS_PATH,
                "ThrowingUpcall")
            .start();

        int result = process.waitFor();
        assertEquals(result, ERR_UNCAUGHT_EXCEPTION);
        assertOutputContains(process.getErrorStream(), "Testing upcall exceptions");
    }

    private static void assertOutputContains(InputStream stream, String str) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(str)) {
                    return;
                }
            }
        }
        fail("Did not find '" + str + "' in stream");
    }
}
