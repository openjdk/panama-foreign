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
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandles;
import org.testng.annotations.Test;
import test.jextract.struct.struct;

import static org.testng.Assert.fail;
import static test.jextract.struct.struct.*;

/*
 * @test
 * @library ..
 * @run driver JtregJextract -t test.jextract.struct -- struct.h
 * @run testng LibStructTest
 */
public class LibStructTest {
    static final struct libStruct;

    static {
        Library lib = Libraries.loadLibrary(MethodHandles.lookup(), "Struct");
        libStruct = Libraries.bind(struct.class, lib);
    }

    @Test
    public void testDerefUndefined() {
        Pointer<Void> ptr = libStruct.allocateUndefinedStruct();
        try {
            ptr.get();
            fail("Should not be able to dereference a Pointer to undefined struct");
        } catch (IllegalStateException ex) {
            // ignore expected
        }
    }
}
