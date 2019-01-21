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

/*
 * @test
 * @run testng PointerTest
 */

import org.testng.annotations.Test;

import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.Pointer;
import java.foreign.memory.Pointer.AccessMode;
import java.security.AccessControlException;

import static org.testng.Assert.assertEquals;

public class PointerTest {

    @Test
    public void testReadOnly() {
        try(Scope scope = Scope.newNativeScope()) {
            Pointer<Integer> p = scope.allocate(NativeTypes.INT32);
            p.set(10);
            assertEquals(p.isAccessibleFor(AccessMode.READ_WRITE), true);
            assertEquals((int) p.get(), 10);

            Pointer<Integer> p2 = p.asReadOnly();
            assertEquals(p2.isAccessibleFor(AccessMode.READ), true);
            assertEquals(p2.isAccessibleFor(AccessMode.WRITE), false);
            assertEquals((int) p2.get(), 10);
        }
    }

    @Test(expectedExceptions = AccessControlException.class)
    public void testIllegalReadOnly() {
        try(Scope scope = Scope.newNativeScope()) {
            Pointer<Integer> p = scope.allocate(NativeTypes.INT32);
            p = p.asWriteOnly();
            p.asReadOnly(); // throws
        }
    }

    @Test
    public void testWriteOnly() {
        try(Scope scope = Scope.newNativeScope()) {
            Pointer<Integer> p = scope.allocate(NativeTypes.INT32);
            p.set(10);
            assertEquals(p.isAccessibleFor(AccessMode.READ_WRITE), true);
            assertEquals((int) p.get(), 10);

            Pointer<Integer> p2 = p.asWriteOnly();
            assertEquals(p2.isAccessibleFor(AccessMode.READ), false);
            assertEquals(p2.isAccessibleFor(AccessMode.WRITE), true);
            p2.set(15); // should work
        }
    }

    @Test(expectedExceptions = AccessControlException.class)
    public void testIllegalWriteOnly() {
        try(Scope scope = Scope.newNativeScope()) {
            Pointer<Integer> p = scope.allocate(NativeTypes.INT32);
            p = p.asReadOnly();
            p.asWriteOnly(); // throws
        }
    }

}
