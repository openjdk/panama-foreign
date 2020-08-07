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

import java.nio.file.Path;
import org.testng.annotations.Test;
import static org.testng.Assert.assertNotNull;

/*
 * @test
 * @modules jdk.incubator.jextract
 * @library /test/lib
 * @build JextractToolRunner
 * @bug 8244512
 * @summary jextract throws NPE for a nested struct declaration
 * @run testng/othervm -Dforeign.restricted=permit Test8244512
 */
public class Test8244512 extends JextractToolRunner {
    @Test
    public void testNestedStructs() {
        Path nestedOutput = getOutputFilePath("nestedgen");
        Path nestedH = getInputFilePath("nested.h");
        run("-d", nestedOutput.toString(), nestedH.toString()).checkSuccess();
        try(Loader loader = classLoader(nestedOutput)) {
            checkClass(loader, "Foo");
            checkClass(loader, "Foo$Bar");
            checkClass(loader, "U");
            checkClass(loader, "U$Point");
            checkClass(loader, "MyStruct");
            checkClass(loader, "MyStruct$MyStruct_Z");
            checkClass(loader, "MyStruct$k");
            checkClass(loader, "MyUnion");
            checkClass(loader, "MyUnion$MyUnion_Z");
            checkClass(loader, "MyUnion$k");
            checkClass(loader, "X");
            checkClass(loader, "X2");
        } finally {
            deleteDir(nestedOutput);
        }
    }

    private static void checkClass(Loader loader, String name) {
        assertNotNull(loader.loadClass("nested_h$" + name));
    }
}
