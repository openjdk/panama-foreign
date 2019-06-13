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
 * @run testng TestVarHandleCombinators
 */

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.foreign.MemoryAddress;
import java.foreign.MemorySegment;
import java.lang.invoke.MemoryAccessVarHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

import static org.testng.Assert.assertEquals;

public class TestVarHandleCombinators {

    @Test
    public void testElementAccess() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class);
        vh = MemoryAccessVarHandles.elementHandle(vh, 1);

        byte[] arr = { 0, 0, -1, 0 };
        MemorySegment segment = MemorySegment.ofArray(arr);
        MemoryAddress addr = segment.baseAddress();

        assertEquals((byte) vh.get(addr, 2), (byte) -1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlignNotPowerOf2() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class);
        MemoryAccessVarHandles.alignAccess(vh, 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlignNegative() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class);
        MemoryAccessVarHandles.alignAccess(vh, -1);
    }

    @Test
    public void testAlign() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class);
        vh = MemoryAccessVarHandles.alignAccess(vh, 2);

        MemorySegment segment = MemorySegment.ofNative(1, 2);
        MemoryAddress address = segment.baseAddress();

        vh.set(address, (byte) 10); // fine, memory region is aligned
        assertEquals((byte) vh.get(address), (byte) 10);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testAlignBadAccess() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class);
        vh = MemoryAccessVarHandles.alignAccess(vh, 2);
        vh = MemoryAccessVarHandles.offsetHandle(vh, 1); // offset by 1 byte

        MemorySegment segment = MemorySegment.ofNative(2, 2);
        MemoryAddress address = segment.baseAddress();

        vh.set(address, (byte) 10); // should be bad align
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOffsetNegative() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class);
        MemoryAccessVarHandles.offsetHandle(vh, -1);
    }

    @Test
    public void testOffset() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class);
        vh = MemoryAccessVarHandles.offsetHandle(vh, 1);

        MemorySegment segment = MemorySegment.ofArray(new byte[2]);
        MemoryAddress address = segment.baseAddress();

        vh.set(address, (byte) 10);
        assertEquals((byte) vh.get(address), (byte) 10);
    }

    @Test
    public void testByteOrderLE() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(short.class);
        byte[] arr = new byte[2];
        MemorySegment segment = MemorySegment.ofArray(arr);
        MemoryAddress address = segment.baseAddress();

        vh = MemoryAccessVarHandles.byteOrder(vh, ByteOrder.LITTLE_ENDIAN);

        vh.set(address, (short) 0xFF);
        assertEquals(arr[0], (byte) 0xFF);
        assertEquals(arr[1], (byte) 0);
    }

    @Test
    public void testByteOrderBE() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(short.class);
        byte[] arr = new byte[2];
        MemorySegment segment = MemorySegment.ofArray(arr);
        MemoryAddress address = segment.baseAddress();

        vh = MemoryAccessVarHandles.byteOrder(vh, ByteOrder.BIG_ENDIAN);

        vh.set(address, (short) 0xFF);
        assertEquals(arr[0], (byte) 0);
        assertEquals(arr[1], (byte) 0xFF);
    }

    @Test(dataProvider = "badCarriers", expectedExceptions = IllegalArgumentException.class)
    public void testBadCarrier(Class<?> carrier) {
        MemoryAccessVarHandles.dereferenceVarHandle(carrier);
    }

    @DataProvider(name = "badCarriers")
    public Object[][] createBadCarriers() {
        return new Object[][] {
                { void.class },
                { boolean.class },
                { Object.class },
                { int[].class },
                { MemoryAddress.class }
        };
    }

}
