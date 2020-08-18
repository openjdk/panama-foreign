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
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import org.testng.annotations.Test;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertNotNull;

/*
 * @test
 * @library /test/lib
 * @modules jdk.incubator.jextract
 * @build JextractToolRunner
 * @bug 8251943
 * @summary jextract should not generate MemorySegment typed fields for variables, struct fields if layout size info is not available
 * @run testng/othervm -Dforeign.restricted=permit Test8251943
 */
public class Test8251943 extends JextractToolRunner {

    @Test
    public void test() {
        Path outputPath = getOutputFilePath("output");
        Path headerFile = getInputFilePath("test8251943.h");
        run("-d", outputPath.toString(), headerFile.toString()).checkSuccess();
        try(Loader loader = classLoader(outputPath)) {
            Class<?> headerClass = loader.loadClass("test8251943_h");
            assertNull(findMethod(headerClass, "tzname$SEGMENT"));

            Class<?> fooClass = loader.loadClass("test8251943_h$Foo");
            assertNotNull(findMethod(fooClass, "bar$get", MemorySegment.class));
            assertNull(findMethod(fooClass, "names$get", MemorySegment.class));
        } finally {
            deleteDir(outputPath);
        }
    }
}
