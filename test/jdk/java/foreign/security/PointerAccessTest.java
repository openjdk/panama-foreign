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

import java.foreign.NativeTypes;
import java.foreign.annotations.NativeAddressof;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.foreign.Libraries;
import java.foreign.Scope;

import static org.testng.Assert.assertEquals;

/**
 * @test
 * @run testng/othervm/policy=scope.allow PointerAccessTest
 * @summary Tests for pointer access should work with SecurityManager
 */
public class PointerAccessTest {
    @NativeStruct("[" +
            "i32(x)" +
            "i32(y)" +
            "](Point)")
    public static interface Point extends Struct<Point> {
        @NativeGetter("x")
        public int x$get();
        @NativeSetter("x")
        public void x$set(int arg);
        @NativeAddressof("x")
        public Pointer<Integer> x$ptr();
        @NativeGetter("y")
        public int y$get();
        @NativeSetter("y")
        public void y$set(int arg);
        @NativeAddressof("y")
        public Pointer<Integer> y$ptr();
    }

    @Test
    public void testGlobalScope() {
        try (Scope s = Scope.globalScope().fork()) {
            Pointer<Integer> ptr = s.allocate(NativeTypes.UINT32);
            ptr.set(1234);
            assertEquals((int) ptr.get(), 1234);

            Point pt = s.allocateStruct(Point.class);
            pt.x$set(1024);
            pt.y$set(768);

            assertEquals(pt.x$get(), 1024);
            assertEquals(pt.y$get(), 768);
        }
    }
}
