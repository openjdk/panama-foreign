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
import java.foreign.Scope;
import java.lang.invoke.MethodHandles;
import org.testng.annotations.Test;
import test.jextract.t8235406_h;
import test.jextract.t8235406_h.Brush00;

import static org.testng.Assert.assertEquals;
import static test.jextract.t8235406_lib.Color.*;

/*
 * @test
 * @bug 8235406
 * @summary Test identifier with number are extracted correctly
 * @library ..
 * @run driver JtregJextract -t test.jextract -l libT8235406 -- t8235406.h
 * @run testng T8235406
 */
public class T8235406 {
    static final t8235406_h libTest;

    static {
        Library lib = Libraries.loadLibrary(MethodHandles.lookup(), "T8235406");
        libTest = Libraries.bind(t8235406_h.class, lib);
    }

    @Test
    public void test() {
        libTest.setBrush(0, R01, G01);
        libTest.setBrush(1, G01, B01);
        libTest.setBrush(2, B01, R01);

        try (Scope s = Scope.globalScope().fork()) {
            Brush00 brush = s.allocateStruct(Brush00.class);
            libTest.getBrush00(brush.ptr());
            assertEquals(brush.foreground03$get(), R01);
            assertEquals(brush.background03$get(), G01);
            libTest.getBrush01(brush.ptr());
            assertEquals(brush.foreground03$get(), G01);
            assertEquals(brush.background03$get(), B01);
            libTest.getBrush02(brush.ptr());
            assertEquals(brush.foreground03$get(), B01);
            assertEquals(brush.background03$get(), R01);
        }
    }
}
