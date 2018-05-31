/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles;
import java.nicl.*;
import java.nicl.metadata.*;

/**
 * @test
 * @run testng/othervm/policy=bindtest.policy BindTest
 * @summary Tests for bind method(s) security checks
 */
public class BindTest {
    @NativeHeader(declarations = "getpid=()i32")
    static interface system {
        @NativeLocation(file="dummy", line=1, column=1, USR="c:@F@getpid")
        public abstract int getpid();
    }

    @Test
    public void testBind() {
        checkSecurityException(()-> {
            system i = Libraries.bind(MethodHandles.lookup(), system.class);
         });
    }

    @Test
    public void testBindWithLibrary() {
        checkSecurityException(()-> {
            system i = Libraries.bind(system.class, Libraries.getDefaultLibrary());
         });
    }

    private static void checkSecurityException(Runnable test) {
        try {
            test.run();
            Assert.fail("should not reach here");
        } catch (SecurityException se) {
            Assert.assertTrue(true, "Got security exception as expected");
            se.printStackTrace();
        }
    }
}
