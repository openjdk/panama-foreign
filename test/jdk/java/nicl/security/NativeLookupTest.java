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
import java.lang.invoke.MethodType;
import java.nicl.Library;
import java.nicl.Libraries;

/**
 * @test
 * @run testng/othervm/policy=nativelookuptest.policy NativeLookupTest
 * @summary Tests for native method handle lookup method(s) security checks
 */
public class NativeLookupTest {
    @Test
    public void testNativeLookup() {
        checkSecurityException(() -> {
            try {
                Libraries.lookupNativeMethod(Libraries.getDefaultLibrary(), "foo",
                    MethodType.methodType(Void.class), false);
            } catch (IllegalAccessException | NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void testNativeLookupLibraryArray() {
        checkSecurityException(() -> {
            try {
                Libraries.lookupNativeMethod(
                    new Library[] { Libraries.getDefaultLibrary() }, "foo",
                    MethodType.methodType(Void.class), false);
            } catch (IllegalAccessException | NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
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
