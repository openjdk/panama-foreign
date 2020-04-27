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
 * @modules jdk.incubator.foreign/jdk.internal.foreign
 * @run testng/othervm -Dforeign.restricted=permit TestLibraryLookup
 */

import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.internal.foreign.LibrariesHelper;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class TestLibraryLookup {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidLookupName() {
        LibraryLookup.ofLibrary("NonExistent");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidLookupPath() {
        LibraryLookup.ofPath("/foo/bar/NonExistent");
    }

    @Test
    public void testSimpleLookup() throws Throwable {
        MemoryAddress symbol = null;
        LibraryLookup lookup = LibraryLookup.ofLibrary("LookupTest");
        symbol = lookup.lookup("f");
        assertTrue(symbol.segment().isAlive());
        assertEquals(LibrariesHelper.numLoadedLibraries(), 1);
        lookup = null;
        symbol = null;
        for (int i = 0 ; i < 1000 ; i++) {
            System.gc();
            Object o = new Object[1000];
            Thread.sleep(1);
        }
        waitUnload();
    }

    @Test
    public void testMultiLookup() throws Throwable {
        List<MemoryAddress> symbols = new ArrayList<>();
        List<LibraryLookup> lookups = new ArrayList<>();
        for (int i = 0 ; i < 5 ; i++) {
            LibraryLookup lookup = LibraryLookup.ofLibrary("LookupTest");
            MemoryAddress symbol = lookup.lookup("f");
            assertTrue(symbol.segment().isAlive());
            lookups.add(lookup);
            symbols.add(symbol);
            assertEquals(LibrariesHelper.numLoadedLibraries(), 1);
        }
        lookups = null;
        symbols = null;
        waitUnload();
    }

    private static void waitUnload() throws InterruptedException {
        while (LibrariesHelper.numLoadedLibraries() != 0) {
            System.gc();
            Object o = new Object[1000];
            Thread.sleep(1);
        }
    }
}
