/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.MemorySegment;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/*
 * @test
 * @run testng TestRestricted
 */
public class TestRestricted {

    @Test(expectedExceptions = IllegalAccessException.class)
    public void testReflection() throws Throwable {
        Method method = MemorySegment.class.getDeclaredMethod("ofNativeRestricted");
        method.invoke(null);
    }

    @Test(expectedExceptions = IllegalAccessException.class)
    public void testLookup() throws Throwable {
        MethodHandles.lookup().findStatic(MemorySegment.class, "ofNativeRestricted", MethodType.methodType(MemorySegment.class));
    }

    @Test(expectedExceptions = IllegalAccessException.class)
    public void testDirectAccess() throws Throwable {
        MemorySegment.ofNativeRestricted();
    }
}
