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

import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandles;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import test.jextract.enums.enums;

/*
 * @test
 * @bug 8210935
 * @summary C enum constants should be mapped to interface methods instead of static final int constants
 * @library ..
 * @run driver JtregJextract -t test.jextract.enums -- enums.h
 * @run testng LibEnumsTest
 */
public class LibEnumsTest {
    static final enums libEnums;

    static {
        Library lib = Libraries.loadLibrary(MethodHandles.lookup(), "Enums");
        libEnums = Libraries.bind(enums.class, lib);
    }

    @Test
    public void testEnumConstants() {
        assertEquals(libEnums.R(), 0xFF0000);
        assertEquals(libEnums.G(), 0x00FF00);
        assertEquals(libEnums.B(), 0x0000FF);

        assertEquals(libEnums.R() | libEnums.G(), libEnums.red_green());
        assertEquals(libEnums.G() | libEnums.B(), libEnums.green_blue());
        assertEquals(libEnums.R() | libEnums.G() | libEnums.B(), libEnums.red_green_blue());

        assertEquals(libEnums.iminfunc(), libEnums.I_MIN());
        assertEquals(libEnums.imaxfunc(), libEnums.I_MAX());
        assertEquals(libEnums.lminfunc(), libEnums.L_MIN());
        assertEquals(libEnums.lmaxfunc(), libEnums.L_MAX());
    }
}
