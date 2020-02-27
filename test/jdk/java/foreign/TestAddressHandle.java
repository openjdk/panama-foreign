/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 * @run testng TestAddressHandle
 */

import java.lang.invoke.*;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;

import org.testng.annotations.*;
import static org.testng.Assert.*;

public class TestAddressHandle {
    @Test(dataProvider = "addressHandles")
    public void testAddressHandle(VarHandle addrHandle) {
        VarHandle longHandle = MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder());
        try (MemorySegment segment = MemorySegment.allocateNative(8)) {
            longHandle.set(segment.baseAddress(), 42L);
            MemoryAddress address = (MemoryAddress)addrHandle.get(segment.baseAddress());
            assertEquals(address.offset(), 42L);
            try {
                longHandle.get(address); // check OOB
                fail();
            } catch (UnsupportedOperationException ex) {
                assertTrue(true);
            }
            addrHandle.set(segment.baseAddress(), address.addOffset(1));
            long result = (long)longHandle.get(segment.baseAddress());
            assertEquals(43L, result);
        }
    }

    @Test(dataProvider = "addressHandles")
    public void testNull(VarHandle addrHandle) {
        VarHandle longHandle = MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder());
        try (MemorySegment segment = MemorySegment.allocateNative(8)) {
            longHandle.set(segment.baseAddress(), 0L);
            MemoryAddress address = (MemoryAddress)addrHandle.get(segment.baseAddress());
            assertTrue(address == MemoryAddress.NULL);
        }
    }

    @DataProvider(name = "addressHandles")
    static Object[][] addressHandles() {
        return new Object[][] {
            { MemoryHandles.varHandle(MemoryAddress.class, ByteOrder.nativeOrder()) },
            { MemoryHandles.withOffset(MemoryHandles.varHandle(MemoryAddress.class, ByteOrder.nativeOrder()), 0) },
            { MemoryLayouts.JAVA_LONG.varHandle(MemoryAddress.class) }
        };
    }
}
