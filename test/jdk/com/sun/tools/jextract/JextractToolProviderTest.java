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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nicl.metadata.Header;
import java.nicl.metadata.LibraryDependencies;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.spi.ToolProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

/*
 * @test
 * @modules jdk.jextract
 * @build JextractToolProviderTest
 * @run testng/othervm -Duser.language=en JextractToolProviderTest
 */
public class JextractToolProviderTest {
    private static final ToolProvider JEXTRACT_TOOL = ToolProvider.findFirst("jextract")
        .orElseThrow(() ->
            new RuntimeException("jextract tool not found")
        );

    private static String testSrcDir = System.getProperty("test.src", ".");
    private static String testClassesDir = System.getProperty("test.classes", ".");

    private static Path getFilePath(String dir, String fileName) {
        return Paths.get(dir, fileName).toAbsolutePath();
    }

    private static Path getInputFilePath(String fileName) {
        return getFilePath(testSrcDir, fileName);
    }

    private static Path getOutputFilePath(String fileName) {
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
        assertEquals(result, 0, "Sucess excepted, failed: " + result);
    }

    private static void checkFailure(String expected, String... options) {
        int result = checkJextract(expected, options);
        assertNotEquals(result, 0, "Failure excepted, succeeded!");
    }

    private static void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException ioExp) {
            System.err.println(ioExp);
        }
    }

    private static Class<?> loadClass(String className, Path...paths) {
        try {
            URL[] urls = new URL[paths.length];
            for (int i = 0; i < paths.length; i++) {
                urls[i] = paths[i].toUri().toURL();
            }
            URLClassLoader ucl = new URLClassLoader(urls, null);
            return Class.forName(className, false, ucl);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Class<?> cls, String name) {
        try {
            return cls.getField(name);
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    private static Method findMethod(Class<?> cls, String name, Class<?>... argTypes) {
        try {
            return cls.getMethod(name, argTypes);
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    private static Method findFirstMethod(Class<?> cls, String name) {
        try {
            for (Method m : cls.getMethods()) {
                if (name.equals(m.getName())) {
                    return m;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    @Test
    public void testHelp() {
        checkFailure(null); // no options
        checkSuccess(null, "--help");
        checkSuccess(null, "-h");
        checkSuccess(null, "-?");
    }

    // error for non-existent header file
    @Test
    public void testNonExistentHeader() {
        checkFailure("Cannot open header file", "--dry-run",
            getInputFilePath("non_existent.h").toString());
    }

    @Test
    public void testDryRun() {
        // only dry-run, don't produce any output
        Path simpleJar = getOutputFilePath("simple.jar");
        deleteFile(simpleJar);
        checkSuccess(null, "--dry-run", getInputFilePath("simple.h").toString());
        try {
            assertFalse(Files.isRegularFile(simpleJar));
        } finally {
            deleteFile(simpleJar);
        }
    }

    @Test
    public void testOutputFileOption() {
        // simple output file check
        Path simpleJar = getOutputFilePath("simple.jar");
        deleteFile(simpleJar);
        checkSuccess(null, "-o", simpleJar.toString(),
            getInputFilePath("simple.h").toString());
        try {
            assertTrue(Files.isRegularFile(simpleJar));
        } finally {
            deleteFile(simpleJar);
        }
    }

    @Test
    public void testOutputClass() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
	Path helloH = getInputFilePath("hello.h");
        checkSuccess(null, "-o", helloJar.toString(), helloH.toString());
        try {
            Class<?> cls = loadClass("hello", helloJar);
            // check header annotation
            Header header = cls.getAnnotation(Header.class);
            assertNotNull(header);
            assertEquals(header.path(), helloH.toString());

            // check a method for "void func()"
            assertNotNull(findMethod(cls, "func", Object[].class));
        } finally {
            deleteFile(helloJar);
        }
    }

    private void testTargetPackage(String targetPkgOption) {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
	Path helloH = getInputFilePath("hello.h");
        checkSuccess(null, targetPkgOption, "com.acme", "-o", helloJar.toString(), helloH.toString());
        try {
            Class<?> cls = loadClass("com.acme.hello", helloJar);
            // check header annotation
            Header header = cls.getAnnotation(Header.class);
            assertNotNull(header);
            assertEquals(header.path(), helloH.toString());

            // check a method for "void func()"
            assertNotNull(findMethod(cls, "func", Object[].class));
        } finally {
            deleteFile(helloJar);
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

    private void testPackageMapping(String pkgMapOption) {
        Path worldJar = getOutputFilePath("world.jar");
        deleteFile(worldJar);
        Path mytypesJar = getOutputFilePath("mytypes.jar");
        deleteFile(mytypesJar);

	Path worldH = getInputFilePath("world.h");
	Path include = getInputFilePath("include");
        // generate jar for mytypes.h
        checkSuccess(null, "-t", "com.acme", "-o", mytypesJar.toString(),
            include.resolve("mytypes.h").toString());
        // world.h include mytypes.h, use appropriate package for stuff from mytypes.h
        checkSuccess(null, "-I", include.toString(), pkgMapOption, include.toString() + "=com.acme",
            "-o", worldJar.toString(), worldH.toString());
        try {
            Class<?> cls = loadClass("world", worldJar, mytypesJar);
            Method m = findFirstMethod(cls, "distance");
            Class<?>[] params = m.getParameterTypes();
            assertEquals(params[0].getName(), "com.acme.mytypes$Point");
        } finally {
            deleteFile(worldJar);
            deleteFile(mytypesJar);
        }
    }

    @Test
    public void testPackageDirMappingOption() {
        testPackageMapping("-m");
    }

    @Test
    public void testPackageDirMappingLongOption() {
        testPackageMapping("--package-map");
    }

    @Test
    public void test_option_L_without_l() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
	Path helloH = getInputFilePath("hello.h");
        Path linkDir = getInputFilePath("libs");
        String warning = "WARNING: -L option specified without any -l option";
        checkSuccess(warning, "-L", linkDir.toString(), "-o", helloJar.toString(), helloH.toString());
    }

    @Test
    public void test_option_rpath_without_l() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
	Path helloH = getInputFilePath("hello.h");
        Path rpathDir = getInputFilePath("libs");
        String warning = "WARNING: -rpath option specified without any -l option";
        try {
            checkSuccess(warning, "-rpath", rpathDir.toString(), "-o", helloJar.toString(), helloH.toString());
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void test_option_l() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
	Path helloH = getInputFilePath("hello.h");
        checkSuccess(null, "-l", "hello", "-o", helloJar.toString(), helloH.toString());
        try {
            Class<?> cls = loadClass("hello", helloJar);
            // check LibraryDependencies annotation capture -l value
            LibraryDependencies libDeps = cls.getAnnotation(LibraryDependencies.class);
            assertNotNull(libDeps);
            assertEquals(libDeps.names().length, 1);
            assertEquals(libDeps.names()[0], "hello");
            // no library paths (rpath) set
            assertEquals(libDeps.paths().length, 0);
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void test_option_l_and_rpath() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
	Path helloH = getInputFilePath("hello.h");
        Path rpathDir = getInputFilePath("libs");
        checkSuccess(null, "-l", "hello", "-rpath", rpathDir.toString(),
             "-o", helloJar.toString(), helloH.toString());
        try {
            Class<?> cls = loadClass("hello", helloJar);
            // check LibraryDependencies annotation captures -l and -rpath values
            LibraryDependencies libDeps = cls.getAnnotation(LibraryDependencies.class);
            assertNotNull(libDeps);
            assertEquals(libDeps.names().length, 1);
            assertEquals(libDeps.names()[0], "hello");
            assertEquals(libDeps.paths().length, 1);
            assertEquals(libDeps.paths()[0], rpathDir.toString());
        } finally {
            deleteFile(helloJar);
        }
    }

    @Test
    public void testUnionDeclaration() {
        Path uniondeclJar = getOutputFilePath("uniondecl.jar");
        deleteFile(uniondeclJar);
	Path uniondeclH = getInputFilePath("uniondecl.h");
        try {
            checkSuccess(null, "-o", uniondeclJar.toString(), uniondeclH.toString());
            Class<?> unionCls = loadClass("uniondecl", uniondeclJar);
            assertNotNull(unionCls);
            boolean found = Arrays.stream(unionCls.getClasses()).
                map(Class::getSimpleName).
                filter(n -> n.equals("IntOrFloat")).
                findFirst().isPresent();
            assertTrue(found, "uniondecl.IntOrFloat not found");
        } finally {
            deleteFile(uniondeclJar);
        }
    }

    private void checkIntField(Class<?> cls, String name, int value) {
        Field field = findField(cls, name);
        assertNotNull(field);
        assertEquals(field.getType(), int.class);
        try {
            assertEquals((int)field.get(null), value);
        } catch (Exception exp) {
            System.err.println(exp);
            assertTrue(false, "should not reach here");
        }
    }

    @Test
    public void testAnonymousEnum() {
        Path anonenumJar = getOutputFilePath("anonenum.jar");
        deleteFile(anonenumJar);
	Path anonenumH = getInputFilePath("anonenum.h");
        try {
            checkSuccess(null, "-o", anonenumJar.toString(), anonenumH.toString());
            Class<?> anonenumCls = loadClass("anonenum", anonenumJar);
            assertNotNull(anonenumCls);
            // the nested type for anonymous enum has name starting with "enum__anonymous_at"
            // followed by full path name of header file + line + column numbers. Any non-ident
            // char replaced by "_". But we test only the start pattern here.
            Optional<Class<?>> optEnumCls = Arrays.stream(anonenumCls.getClasses()).
                filter(c -> c.getSimpleName().startsWith("enum__anonymous_at")).
                findFirst();
            assertTrue(optEnumCls.isPresent());
            Class<?> enumCls = optEnumCls.get();
            checkIntField(enumCls, "RED", 0xff0000);
            checkIntField(enumCls, "GREEN", 0x00ff00);
            checkIntField(enumCls, "BLUE", 0x0000ff);
        } finally {
            deleteFile(anonenumJar);
        }
    }

    @Test
    public void testExcludeSymbols() {
        Path helloJar = getOutputFilePath("hello.jar");
        deleteFile(helloJar);
        Path helloH = getInputFilePath("hello.h");
        checkSuccess(null, "-o", helloJar.toString(), helloH.toString());
        try {
            Class<?> cls = loadClass("hello", helloJar);
            // check a method for "void func()"
            assertNotNull(findMethod(cls, "func", Object[].class));
            // check a method for "void junk()"
            assertNotNull(findMethod(cls, "junk", Object[].class));
        } finally {
            deleteFile(helloJar);
        }

        // try with --exclude-symbols" this time.
        checkSuccess(null, "--exclude-symbols", "junk", "-o", helloJar.toString(), helloH.toString());
        try {
            Class<?> cls = loadClass("hello", helloJar);
            // check a method for "void func()"
            assertNotNull(findMethod(cls, "func", Object[].class));
            // check a method for "void junk()"
            assertNull(findMethod(cls, "junk", Object[].class));
        } finally {
            deleteFile(helloJar);
        }
    }
}
