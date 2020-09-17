/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

/*
 * @test
 * @run testng TestFunctionDescriptor
 */

import jdk.incubator.foreign.FunctionDescriptor;
import org.testng.annotations.Test;

import static jdk.incubator.foreign.CLinker.C_DOUBLE;
import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_LONGLONG;

public class TestFunctionDescriptor {

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullArgumentLayout() {
        FunctionDescriptor.ofVoid(C_INT, null, C_LONGLONG);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullReturnLayout() {
        FunctionDescriptor.of(null, C_INT, C_LONGLONG);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullArgumentLayoutsAppend() {
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(C_INT, C_LONGLONG);
        fd.appendArgumentLayouts(C_DOUBLE, null); // should throw
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullReturnLayoutChange() {
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(C_INT, C_LONGLONG);
        fd.changeReturnLayout(null); // should throw
    }

}
