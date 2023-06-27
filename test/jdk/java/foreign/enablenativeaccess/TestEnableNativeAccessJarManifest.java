/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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

/**
 * @test
 * @summary Basic test for Enable-Native-Access attribute in the
 *          manifest of a main application JAR
 * @library /test/lib
 * @requires jdk.foreign.linker != "UNSUPPORTED"
 * @requires !vm.musl
 *
 * @enablePreview
 * @build TestEnableNativeAccess
 *        org.openjdk.foreigntest.PanamaMainUnnamedModule
 * @run testng TestEnableNativeAccessJarManifest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;
import jdk.test.lib.util.JarUtils;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

public class TestEnableNativeAccessJarManifest extends TestEnableNativeAccessBase {

    static record Attribute(String name, String value) {}

    /**
     * Runs the test to execute the given test action. The VM is run with the
     * given VM options and the output checked to see that it matches the
     * expected result.
     */
    OutputAnalyzer run(String action, Result expectedResult, Attribute... attributes) throws Exception {
        Manifest man = new Manifest();
        Attributes attrs = man.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, UNNAMED);

        for (Attribute attrib : attributes) {
            attrs.put(new Attributes.Name(attrib.name()), attrib.value());
        }

        // create the JAR file with Test1 and Test2
        Path jarfile = Paths.get(action + ".jar");
        Files.deleteIfExists(jarfile);

        Path classes = Paths.get(System.getProperty("test.classes", ""));
        JarUtils.createJarFile(jarfile, man, classes, Paths.get(UNNAMED.replace('.', '/') + ".class"));

        // java -jar test.jar
        OutputAnalyzer outputAnalyzer = ProcessTools.executeTestJava(
                    "--enable-preview",
                    "-Djava.library.path=" + System.getProperty("java.library.path"),
                    "-jar", jarfile.toString())
                .outputTo(System.out)
                .errorTo(System.out);
        checkResult(expectedResult, outputAnalyzer);
        return outputAnalyzer;
    }

    @Test
    public void testSucceed() throws Exception {

    }

    @DataProvider
    public Object[][] succeedCases() {
        return new Object[][] {
            { "panama_no_unnamed_module_native_access", successWithWarning("ALL-UNNAMED") },
            { "panama_unnamed_module_native_access_false", successWithWarning("ALL-UNNAMED"), new Attribute[] {new Attribute("Enable-Native-Access", "false")} },
            { "panama_unnamed_module_native_access_asdf", successWithWarning("ALL-UNNAMED"), new Attribute[] {new Attribute("Enable-Native-Access", "asdf")} },
            { "panama_unnamed_module_native_access_true", successNoWarning(), new Attribute[] {new Attribute("Enable-Native-Access", "true")} },
            { "panama_unnamed_module_native_access_True", successNoWarning(), new Attribute[] {new Attribute("Enable-Native-Access", "True")} },
        };
    }
}
