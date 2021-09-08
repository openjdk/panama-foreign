/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8273382
 * @summary jextract should generete alias layout for pointer typedefs
 * @run testng/othervm --enable-native-access=jdk.incubator.jextract Test8273382
 */
public class Test8273382 extends JextractToolRunner {
    @Test
    public void testPointerTypedefs() {
        Path test8273382Output = getOutputFilePath("test8273382gen");
        Path test8273382H = getInputFilePath("test8273382.h");
        run("-d", test8273382Output.toString(), test8273382H.toString()).checkSuccess();
        try(Loader loader = classLoader(test8273382Output)) {
            Class<?> headerCls = loader.loadClass("test8273382_h");
            assertNotNull(findField(headerCls, "int_ptr_t"));
            assertNotNull(findField(headerCls, "point_ptr_t"));
        } finally {
            deleteDir(test8273382Output);
        }
    }
}
