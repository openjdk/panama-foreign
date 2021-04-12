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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jdk.incubator.foreign.Addressable;
import jdk.incubator.foreign.MemoryAddress;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @modules jdk.incubator.jextract
 * @library /test/lib
 * @build JextractToolRunner
 * @bug 8240181
 * @run testng/othervm --enable-native-access=jdk.incubator.jextract -Duser.language=en --add-modules jdk.incubator.jextract JextractToolProviderTest
 */
public class JextractToolProviderTest extends JextractToolRunner {
    @Test
    public void testHelp() {
        run().checkFailure(OPTION_ERROR); // no options
        run("--help").checkSuccess();
        run("-h").checkSuccess();
        run("-?").checkSuccess();
    }

    // error for non-existent header file
    @Test
    public void testNonExistentHeader() {
        run(getInputFilePath("non_existent.h").toString())
            .checkFailure(INPUT_ERROR)
            .checkContainsOutput("cannot read header file");
    }

    // error for header including non_existent.h file
    @Test
    public void testNonExistentIncluder() {
        run(getInputFilePath("non_existent_includer.h").toString())
            .checkFailure(CLANG_ERROR)
            .checkContainsOutput("file not found");
    }

    @Test
    public void testDirectoryAsHeader() {
        run(getInputFilePath("directory.h").toString())
            .checkFailure(INPUT_ERROR)
            .checkContainsOutput("not a file");
    }

    // error for header with parser errors
    @Test
    public void testHeaderWithDeclarationErrors() {
        run(getInputFilePath("illegal_decls.h").toString())
            .checkFailure(CLANG_ERROR)
            .checkContainsOutput("cannot combine with previous 'short' declaration specifier");
    }

    @Test
    public void testOutputClass() {
        Path helloOutput = getOutputFilePath("hellogen");
        Path helloH = getInputFilePath("hello.h");
        run("-d", helloOutput.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloOutput)) {
            Class<?> cls = loader.loadClass("hello_h");
            // check a method for "void func(int)"
            assertNotNull(findMethod(cls, "func", int.class));
            // check a method for "int printf(MemoryAddress, Object[])"
            assertNotNull(findMethod(cls, "printf", Addressable.class, Object[].class));
        } finally {
            deleteDir(helloOutput);
        }
    }

    private void testTargetPackage(String targetPkgOption) {
        Path helloOutput = getOutputFilePath("hellogen");
        Path helloH = getInputFilePath("hello.h");
        run(targetPkgOption, "com.acme", "-d",
            helloOutput.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloOutput)) {
            Class<?> cls = loader.loadClass("com.acme.hello_h");
            // check a method for "void func(int)"
            assertNotNull(findMethod(cls, "func", int.class));
            // check a method for "int printf(MemoryAddress, Object[])"
            assertNotNull(findMethod(cls, "printf", Addressable.class, Object[].class));
        } finally {
            deleteDir(helloOutput);
        }
    }

    @Test
    public void testTargetPackageOption() {
        testTargetPackage("-t");
    }

    @Test
    public void testTargetPackageLongOption() {
        testTargetPackage("--target-package");
    }

    @Test
    public void testHeaderClassName() {
        Path helloOutput = getOutputFilePath("hellogen");
        Path helloH = getInputFilePath("hello.h");
        run("--header-class-name", "MyHello", "-t", "com.acme", "-d",
            helloOutput.toString(), helloH.toString()).checkSuccess();
        try(Loader loader = classLoader(helloOutput)) {
            Class<?> cls = loader.loadClass("com.acme.MyHello");
            // check a method for "void func(int)"
            assertNotNull(findMethod(cls, "func", int.class));
            // check a method for "int printf(MemoryAddress, Object[])"
            assertNotNull(findMethod(cls, "printf", Addressable.class, Object[].class));
        } finally {
            deleteDir(helloOutput);
        }
    }
}
