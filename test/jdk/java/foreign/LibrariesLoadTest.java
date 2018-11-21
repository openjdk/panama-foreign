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
 * @run testng LibrariesLoadTest
 */

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.foreign.Libraries;
import org.testng.annotations.*;
import static org.testng.Assert.*;

// Tests for Libraries.load and Libraries.loadLibrary APIs
@Test
public class LibrariesLoadTest {
    @Test
    public void testLoadWithAbsolutePath() {
        String libName = System.mapLibraryName("Hello");
        String[] paths = System.getProperty("java.library.path").split(File.pathSeparator);
        boolean foundLib = false;
        for (String p : paths) {
            File f = new File(p, libName);
            if (f.isFile()) {
                foundLib = true;
                Libraries.load(MethodHandles.lookup(), f.toString());
                break;
            }
        }
        assertTrue(foundLib, "Could not locate " + libName);
    }

    @Test
    public void testLoadWithSimpleName() {
        try {
            Libraries.load(MethodHandles.lookup(), System.mapLibraryName("Hello"));
            assertTrue(false, "should have thrown exception");
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("Got expected exception: " + ule);
        }
    }

    @Test
    public void testLoadLibraryWithPath() {
        try {
            Libraries.loadLibrary(MethodHandles.lookup(), File.separator + System.mapLibraryName("Hello"));
            assertTrue(false, "should have thrown exception");
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("Got expected exception: " + ule);
        }
    }

    @Test
    public void testLoadLibraryWithSimpleName() {
        Libraries.loadLibrary(MethodHandles.lookup(), "Hello");
    }
}
