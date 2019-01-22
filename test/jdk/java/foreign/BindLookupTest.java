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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.foreign.*;
import java.foreign.annotations.*;

/**
 * @test
 * @run testng/othervm BindLookupTest
 * @summary Tests for bind method(s) lookup checks
 */
public class BindLookupTest {
    @NativeHeader
    public static interface system {
        @NativeLocation(file="dummy", line=1, column=1)
        @NativeFunction("(i32)v")
        public abstract void exit(int i);
    }

    @NativeHeader
    public static interface bomb {
        @NativeFunction("()i32")
        public abstract int noSuchMethodTest();
    }

    @Test(dataProvider = "Lookups")
    public void testBind(Lookup lookup, Class<?> exceptionClass) {
        checkIllegalArgumentException(()-> {
            system i = Libraries.bind(lookup, system.class);
         }, exceptionClass);
    }

    @Test
    public void testBindWithLibrary() {
        checkIllegalArgumentException(()-> {
            system i = Libraries.bind(system.class, Libraries.getDefaultLibrary());
         }, null);
    }

    @Test
    public void testNotExistSymbol() {
        checkIllegalArgumentException(()-> {
            bomb i = Libraries.bind(bomb.class, Libraries.getDefaultLibrary());
        }, RuntimeException.class);
    }

    @DataProvider(name = "Lookups")
    public static Object[][] lookups() {
        return new Object[][] {
                { null, NullPointerException.class },
                { MethodHandles.lookup(), null },
                { MethodHandles.publicLookup(), IllegalArgumentException.class }};
    }

    private static void checkIllegalArgumentException(Runnable test, Class<?> exceptionClass) {
        try {
            test.run();
            Assert.assertNull(exceptionClass, "should not reach here");
        } catch (Throwable t) {
            Assert.assertEquals(t.getClass(), exceptionClass, "Got exception as expected");
            t.printStackTrace();
        }
    }
}
