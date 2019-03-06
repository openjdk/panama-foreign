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
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.testng.annotations.Test;
import test.jextract.enums.enums;

import static org.testng.Assert.assertEquals;
import static test.jextract.enums.enums_h.*;

/*
 * @test
 * @bug 8210935 8218763
 * @summary C enum constants should be mapped to interface methods instead of static final int constants
 * @library ..
 * @run driver JtregJextract -t test.jextract.enums -l libEnums -- enums.h
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

        assertEquals(libEnums.i_value1_func(), libEnums.I_VALUE1());
        assertEquals(libEnums.i_value2_func(), libEnums.I_VALUE2());
        assertEquals(libEnums.l_value1_func(), libEnums.L_VALUE1());
        assertEquals(libEnums.l_value2_func(), libEnums.L_VALUE2());
    }

    @Test
    public void testSwitchOnEnum() {
        for (int c: List.of(0xFF0000, 0x00FF00, 0x0000FF)) {
            switch (c) {
                case Color.R:
                    assertEquals(c, libEnums.R());
                    break;
                case Color.G:
                    assertEquals(c, libEnums.G());
                    break;
                case Color.B:
                    assertEquals(c, libEnums.B());
                    break;
            }
        }
    }
}
