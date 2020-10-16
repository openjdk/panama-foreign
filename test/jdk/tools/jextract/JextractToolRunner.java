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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;
import java.util.spi.ToolProvider;

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.jextract.Type;
import jdk.test.lib.util.FileUtils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class JextractToolRunner {
    private static String safeFileName(String filename) {
        int ext = filename.lastIndexOf('.');
        return ext != -1 ? filename.substring(0, ext) : filename;
    }

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

    protected static class JextractResult {
        private int exitCode;
        private String output;

        JextractResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        protected JextractResult checkSuccess() {
            assertEquals(exitCode, 0, "Sucess expected, failed: " + exitCode);
            return this;
        }

        protected JextractResult checkFailure() {
            assertNotEquals(exitCode, 0, "Failure expected, succeeded!");
            return this;
        }

        protected JextractResult checkContainsOutput(String expected) {
            Objects.requireNonNull(expected);
            assertTrue(output.contains(expected), "Output does not contain string: " + expected);
            return this;
        }

        protected JextractResult checkMatchesOutput(String regex) {
            Objects.requireNonNull(regex);
            assertTrue(output.trim().matches(regex), "Output does not match regex: " + regex);
            return this;
        }
    }

    protected static JextractResult run(Object... options) {
        return run(Arrays.stream(options).map(Objects::toString).toArray(String[]::new));
    }

    protected static JextractResult run(String... options) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        String[] args = new String[options.length + 1];
        int result = JEXTRACT_TOOL.run(pw, pw, options);
        String output = writer.toString();
        System.err.println(output);
        return new JextractResult(result, output);
    }

    protected static void deleteFile(Path path) {
        try {
            FileUtils.deleteFileIfExistsWithRetry(path);
        } catch (IOException ioExp) {
            throw new RuntimeException(ioExp);
        }
    }

    protected static void deleteDir(Path path) {
        try {
            FileUtils.deleteFileTreeWithRetry(path);
        } catch (IOException ioExp) {
            throw new RuntimeException(ioExp);
        }
    }

    protected static Loader classLoader(Path... paths) {
        try {
            URL[] urls = new URL[paths.length];
            for (int i = 0; i < paths.length; i++) {
                urls[i] = paths[i].toUri().toURL();
            }
            URLClassLoader ucl = new URLClassLoader(urls,
                    JextractToolRunner.class.getClassLoader());
            return new Loader(ucl);
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

    protected Method checkIntGetter(Class<?> cls, String name, int value) {
        Method method = findMethod(cls, name);
        assertNotNull(method);
        assertEquals(method.getReturnType(), int.class);
        try {
            assertEquals((int)method.invoke(null), value);
        } catch (Exception exp) {
            System.err.println(exp);
            assertTrue(false, "should not reach here");
        }
        return method;
    }

    protected static Method findMethod(Class<?> cls, String name, Class<?>... argTypes) {
        try {
            return cls.getMethod(name, argTypes);
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
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

    protected static Class<?> findNestedClass(Class<?> clz, String name) {
        return findClass(clz.getClasses(), name);
    }

    protected static Class<?> findClass(Class<?>[] clz, String name) {
        for (Class<?> cls: clz) {
            if (cls.getSimpleName().equals(name)) {
                return cls;
            }
        }
        return null;
    }

    protected Method checkMethod(Class<?> cls, String name, MethodType type) {
        return checkMethod(cls, name, type.returnType(), type.parameterArray());
    }

    protected Method checkMethod(Class<?> cls, String name, Class<?> returnType, Class<?>... args) {
        Method m = findMethod(cls, name, args);
        assertNotNull(m);
        assertEquals(m.getReturnType(), returnType);
        assertEquals(m.getParameterTypes(), args);
        return m;
    }

    protected MemoryLayout findLayout(Class<?> cls, String name) {
        Method method = findMethod(cls, name + "$LAYOUT");
        assertNotNull(method);
        assertEquals(method.getReturnType(), MemoryLayout.class);
        try {
            return (MemoryLayout)method.invoke(null);
        } catch (Exception exp) {
            System.err.println(exp);
            assertTrue(false, "should not reach here");
        }
        return null;
    }

    protected MemoryLayout findLayout(Class<?> cls) {
        return findLayout(cls, "");
    }

    protected static void checkField(MemoryLayout group, String fieldName, MemoryLayout expected) {
        assertEquals(group.select(PathElement.groupElement(fieldName)), expected.withName(fieldName));
    }

    protected static class Loader implements AutoCloseable {

        private final URLClassLoader loader;

        public Loader(URLClassLoader loader) {
            this.loader = loader;
        }

        public Class<?> loadClass(String className) {
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException e) {
                // return null so caller can check if class loading
                // was successful with assertNotNull/assertNull
                return null;
            }
        }

        @Override
        public void close() {
            try {
                loader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
