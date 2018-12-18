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

import org.testng.annotations.Test;

import java.foreign.layout.Layout;
import java.foreign.annotations.NativeStruct;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @modules jdk.jextract
 * @build JextractToolRunner
 * @run testng EmptyStructTest
 */
public class EmptyStructTest extends JextractToolRunner {
    @Test
    public void emptyStruct() {
        Path clzPath = getOutputFilePath("EmptyStructTest.jar");
        run("-o", clzPath.toString(),
                getInputFilePath("emptyStruct.h").toString()).checkSuccess();
        Class<?> cls = loadClass("emptyStruct", clzPath);
        Class<?>[] inners = cls.getDeclaredClasses();
        assertEquals(inners.length, 3);
        Class<?> emptyStruct = findClass(inners, "EmptyStruct");
        Class<?> nothing = findClass(inners, "Nothing");
        assertTrue(nothing.isAnnotation());
        NativeStruct ns = emptyStruct.getAnnotation(NativeStruct.class);
        assertNotNull(ns);
        Layout layout = Layout.of(ns.value());
        assertEquals(layout.bitsSize(), 0);
        deleteFile(clzPath);
    }
}
