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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.spi.ToolProvider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

class JextractToolRunner {
    private static final ToolProvider JEXTRACT_TOOL = ToolProvider.findFirst("jextract")
            .orElseThrow(() ->
                    new RuntimeException("jextract tool not found")
            );

    private final Path inputDir;
    private final Path outputDir;

    protected JextractToolRunner() {
        this(null, null);
    }

    protected JextractToolRunner(Path input, Path output) {
        inputDir = (input != null) ? input :
            Paths.get(System.getProperty("test.src", "."));
        outputDir = (output != null) ? output :
            Paths.get(System.getProperty("test.classes", "."));
    }

    protected Path getInputFilePath(String fileName) {
        return inputDir.resolve(fileName).toAbsolutePath();
    }

    protected Path getOutputFilePath(String fileName) {
        return outputDir.resolve(fileName).toAbsolutePath();
    }

    protected static int checkJextract(String expected, String... options) {
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

    protected static void checkSuccess(String expected, String... options) {
        int result = checkJextract(expected, options);
        assertEquals(result, 0, "Sucess excepted, failed: " + result);
    }

    protected static void checkFailure(String expected, String... options) {
        int result = checkJextract(expected, options);
        assertNotEquals(result, 0, "Failure excepted, succeeded!");
    }

    protected static void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException ioExp) {
            System.err.println(ioExp);
        }
    }

    protected static Class<?> loadClass(String className, Path...paths) {
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

    protected static Field findField(Class<?> cls, String name) {
        try {
            return cls.getField(name);
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    protected static Method findMethod(Class<?> cls, String name, Class<?>... argTypes) {
        try {
            return cls.getMethod(name, argTypes);
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    protected static Method findStructFieldGet(Class<?> cls, String name) {
        return findMethod(cls, name + "$get");
    }

    protected static Method findGlobalVariableGet(Class<?> cls, String name) {
        return findMethod(cls, name + "$get");
    }

    protected static Method findFirstMethod(Class<?> cls, String name) {
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

    protected Field checkIntField(Class<?> cls, String name, int value) {
        Field field = findField(cls, name);
        assertNotNull(field);
        assertEquals(field.getType(), int.class);
        try {
            assertEquals((int)field.get(null), value);
        } catch (Exception exp) {
            System.err.println(exp);
            assertTrue(false, "should not reach here");
        }
        return field;
    }

    protected Class<?> findClass(Class<?>[] clz, String name) {
        for (Class<?> cls: clz) {
            if (cls.getSimpleName().equals(name)) {
                return cls;
            }
        }
        return null;
    }

    protected Method checkMethod(Class<?> cls, String name, Class<?> returnType, Class<?>... args) {
        try {
            Method m = cls.getDeclaredMethod(name, args);
            assertTrue(m.getReturnType() == returnType);
            return m;
        } catch (NoSuchMethodException nsme) {
            fail("Expect method " + name);
        }
        return null;
    }
}
