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

/*
 * @test
 * @library ..
 * @modules jdk.jextract
 * @build RpathTest
 *
 * @run testng/othervm RpathTest
 */

import org.testng.annotations.*;

import java.foreign.annotations.NativeHeader;
import java.nio.file.Path;

import static org.testng.Assert.*;

public class RpathTest extends JextractToolRunner {

    @Test
    public void testExplicit() throws ReflectiveOperationException {
        Path clzPath = getOutputFilePath("libTestRpath.jar");
        run("-o", clzPath.toString(),
                "-l", "b", "-rpath", "foo/bar",
                getInputFilePath("foo.h").toString()).checkSuccess();
        try(Loader loader = classLoader(clzPath)) {
            for (String name : new String[] { "foo", "bar"}) {
                Class<?> headerCls = loader.loadClass(name);
                NativeHeader nativeHeader = headerCls.getAnnotation(NativeHeader.class);
                assertNotNull(nativeHeader);
                assertTrue(nativeHeader.libraryPaths().length == 1);
                assertEquals(nativeHeader.libraryPaths()[0], "foo/bar");
            }
        } finally {
            deleteFile(clzPath);
        }
    }

    @Test
    public void testAuto() throws ReflectiveOperationException {
        Path clzPath = getOutputFilePath("libTestRpath.jar");
        run("-o", clzPath.toString(),
                "-l", "b", "-L", "foo/bar", "-infer-rpath",
                getInputFilePath("foo.h").toString()).checkSuccess();
        try(Loader loader = classLoader(clzPath)) {
            for (String name : new String[] { "foo", "bar"}) {
                Class<?> headerCls = loader.loadClass(name);
                NativeHeader nativeHeader = headerCls.getAnnotation(NativeHeader.class);
                assertNotNull(nativeHeader);
                assertTrue(nativeHeader.libraryPaths().length == 1);
                assertEquals(nativeHeader.libraryPaths()[0], "foo/bar");
            }
        } finally {
            deleteFile(clzPath);
        }
    }
}
