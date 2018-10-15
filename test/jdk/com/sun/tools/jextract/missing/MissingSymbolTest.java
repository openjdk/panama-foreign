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
import java.lang.reflect.Method;
import org.testng.annotations.Test;
import test.jextract.missing.missing;

import static org.testng.Assert.assertTrue;
import static test.jextract.missing.missing.*;

/*
 * @test
 * @library ..
 * @run driver JtregJextract -l Missing -L $(test.nativepath) -t test.jextract.missing -- missing.h
 * @run testng MissingSymbolTest
 */
public class MissingSymbolTest {
    @Test
    public void testBind() {
        // make sure that we can bind even though "cube" method is missing!
        Library lib = Libraries.loadLibrary(MethodHandles.lookup(), "Missing");
        missing libMissing = Libraries.bind(missing.class, lib);

        // make sure cube method is not present
        boolean noCubeMethod = false;
        try {
            Method cube = missing.class.getMethod("cube", int.class);
        } catch (NoSuchMethodException nsme) {
            System.err.println(nsme);
            noCubeMethod = true;
        }
        assertTrue(noCubeMethod);
    }
}
