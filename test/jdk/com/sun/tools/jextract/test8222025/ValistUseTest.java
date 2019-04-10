/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.file.Path;
import java.io.IOException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/*
 * @test
 * @bug 8222025
 * @summary jextract generates reference to underfined type for va_list
 * @library ..
 * @requires os.family != "windows"
 * @run testng ValistUseTest
 */
public class ValistUseTest extends JextractToolRunner {
    @Test
    public void test() throws IOException {
        Path vaListUseJar = getOutputFilePath("test8222025.jar");
        deleteFile(vaListUseJar);
        Path va_list_use_H = getInputFilePath("va_list_use.h");
        run("-o", vaListUseJar.toString(),
            va_list_use_H.toString()).checkSuccess();
        try {
            Loader loader = classLoader(vaListUseJar);
            Class<?> vaListTag = loader.loadClass("clang_support.builtin$_h$__va_list_tag");
            assertTrue(vaListTag != null);
        } finally {
            deleteFile(vaListUseJar);
        }
     }
}
