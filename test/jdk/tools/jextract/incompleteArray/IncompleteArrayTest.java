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

/*
 * @test
 * @library .. /test/lib
 * @modules jdk.incubator.jextract
 *
 * @run testng/othervm -Dforeign.restricted=permit IncompleteArrayTest
 */

import jdk.incubator.foreign.MemoryLayout;
import org.testng.annotations.Test;

import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_POINTER;
import static org.testng.Assert.*;

import java.nio.file.Path;

public class IncompleteArrayTest extends JextractToolRunner {

    @Test
    public void testIncompleteArray() {
        Path output = getOutputFilePath("incompleteArray_out");
        Path input = getInputFilePath("incompleteArray.h");
        run(
            "-t", "org.jextract",
            "-d", output,
            "--",
            input).checkSuccess();
        try (Loader loader = classLoader(output)) {
            Class<?> cls = loader.loadClass("org.jextract.Foo");
            assertNotNull(cls);

            MemoryLayout actualLayout = findLayout(cls);
            MemoryLayout expectedLayout = MemoryLayout.ofStruct(
                C_INT.withName("size"),
                MemoryLayout.ofPaddingBits(32),
                MemoryLayout.ofSequence(C_POINTER).withName("data")
            ).withName("Foo");
            assertEquals(actualLayout, expectedLayout);
        } finally {
            //deleteDir(output);
        }
    }

}
