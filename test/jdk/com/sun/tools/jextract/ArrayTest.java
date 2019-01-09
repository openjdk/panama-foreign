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

import java.lang.reflect.Method;
import java.foreign.annotations.NativeStruct;
import java.nio.file.Path;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/*
 * @test
 * @modules jdk.jextract
 * @build JextractToolRunner
 * @run testng ArrayTest
 */
public class ArrayTest extends JextractToolRunner {
    @Test
    public void arrayInStruct() {
        Path clzPath = getOutputFilePath("tmp.jar");
        deleteFile(clzPath);
        run("-o", clzPath.toString(),
                getInputFilePath("arrayTest.h").toString()).checkSuccess();
        try(Loader loader = classLoader(clzPath)) {
            Class<?> cls = loader.loadClass("arrayTest");

            Class<?>[] inners = cls.getDeclaredClasses();
            // FIXME: should really be two without duplicate callback
            assertEquals(inners.length, 3);

            Class<?> struct = findClass(inners, "EndWithArray");
            NativeStruct ns = struct.getAnnotation(NativeStruct.class);
            assertNotNull(ns);

            Method m = findMethod(cls, "construct", int.class, int.class, java.foreign.memory.Pointer.class);
            assertNotNull(m);
        } finally {
            deleteFile(clzPath);
        }
    }
}
