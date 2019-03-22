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

/*
 * @test
 * @library ..
 * @modules jdk.jextract
 *
 * @run testng FilterTest
 */

import org.testng.annotations.*;

import java.nio.file.Path;

import static org.testng.Assert.*;

public class FilterTest extends JextractToolRunner {

    private Path jar;
    private Loader loader;

    @BeforeClass
    public void beforeClass() {
        jar = getInputFilePath("out.jar");
        deleteFile(jar);
        Path header = getInputFilePath("filter.h");
        Path curDir = getInputFilePath("");

        run("-L", curDir.toString(),
                "-l", "Filter",
                "-o", jar.toString(),
                "--exclude-symbols", "foo5",
                "--include-headers", ".*filter\\.h",
                "--include-headers", ".*filterDep3\\.h",
                header.toString()).checkSuccess();

        loader = classLoader(jar);
    }

    @AfterClass
    public void afterClass() {
        loader.close();
        deleteFile(jar);
    }

    @Test
    public void testMainHeaderGen() {
        Class<?> filter = loader.loadClass("filter");
        assertNotNull(filter);
    }

    @Test
    public void testDroppedHeader() {
        Class<?> filterDep = loader.loadClass("filterDep1");
        assertNull(filterDep);
    }

    @Test
    public void testIncludedDependency() {
        Class<?> filterDep = loader.loadClass("filterDep2");
        assertNotNull(filterDep);
        Class<?> widget = loader.loadClass("filterDep2$Widget1");
        assertNotNull(widget);

        Class<?> junk = loader.loadClass("filterDep2$Junk1");
        assertNull(junk);
    }

    @Test
    public void testManullyIncludedHeader() {
        Class<?> filterDep = loader.loadClass("filterDep3");
        assertNotNull(filterDep);
        Class<?> baz = loader.loadClass("filterDep3$Baz");
        assertNotNull(baz);
    }

    @Test
    public void testArrayElementChase() {
        Class<?> filterDep = loader.loadClass("filterDep4");
        assertNotNull(filterDep);
        Class<?> widget = loader.loadClass("filterDep4$Widget2");
        assertNotNull(widget);

        Class<?> junk = loader.loadClass("filterDep4$Junk2");
        assertNull(junk);
    }

    @Test
    public void testPointeeTypeChase() {
        Class<?> filterDep = loader.loadClass("filterDep5");
        assertNotNull(filterDep);
        Class<?> widget = loader.loadClass("filterDep5$Widget3");
        assertNotNull(widget);

        Class<?> junk = loader.loadClass("filterDep5$Junk3");
        assertNull(junk);
    }

    @Test
    public void testIndirectTypedef() {
        Class<?> filterDep = loader.loadClass("filterDep6");
        assertNotNull(filterDep);
        Class<?> _Widget4 = loader.loadClass("filterDep6$_Widget4");
        assertNotNull(_Widget4);
        Class<?> intermediateWidget = loader.loadClass("filterDep6$IntermediateWidget");
        assertNull(intermediateWidget);
        Class<?> widget = loader.loadClass("filterDep6$Widget4");
        assertNotNull(widget);

        Class<?> junk = loader.loadClass("filterDep6$Junk4");
        assertNull(junk);
    }

    @Test
    public void testNonFunctionDep() {
        Class<?> filterDep = loader.loadClass("filterDep7");
        assertNotNull(filterDep);
        Class<?> widget = loader.loadClass("filterDep7$Widget6");
        assertNotNull(widget);

        Class<?> junk = loader.loadClass("filterDep6$Junk5");
        assertNull(junk);
    }

    @Test
    public void testPatternFilter() {
        // pre-condition
        Class<?> filter = loader.loadClass("filter");
        assertNull(findFirstMethod(filter, "foo6"));

        Class<?> filterDep = loader.loadClass("filterDep8");
        assertNull(filterDep);
        Class<?> junk = loader.loadClass("filterDep8$Junk6");
        assertNull(junk);
    }

    @Test
    public void testLibraryFilter() {
        // pre-condition
        Class<?> filter = loader.loadClass("filter");
        assertNull(findFirstMethod(filter, "foo7"));

        Class<?> filterDep = loader.loadClass("filterDep9");
        assertNull(filterDep);
        Class<?> junk = loader.loadClass("filterDep9$Junk7");
        assertNull(junk);
    }
}
