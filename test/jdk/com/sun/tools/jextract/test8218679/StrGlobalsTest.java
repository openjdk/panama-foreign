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

import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandles;
import org.testng.annotations.Test;
import test.jextract.strglobals.strglobals;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/*
 * @test
 * @bug 8218679
 * @summary binder throws NPE when binding sqlite3 interface
 * @library ..
 * @run driver JtregJextract -t test.jextract.strglobals -- strglobals.h
 * @run testng StrGlobalsTest
 */
public class StrGlobalsTest {
    static final strglobals libGlobals;

    static {
        Library lib = Libraries.loadLibrary(MethodHandles.lookup(), "Strglobals");
        libGlobals = Libraries.bind(strglobals.class, lib);
    }

    @Test
    public void test() {
        assertEquals(Pointer.toString(libGlobals.str1$get()), "hello");
        assertEquals(Pointer.toString(libGlobals.str2$get()), "world");
    }
}
