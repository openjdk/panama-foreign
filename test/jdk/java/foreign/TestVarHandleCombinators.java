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
import java.foreign.MemoryAccessVarHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

import static org.testng.Assert.assertEquals;

public class TestVarHandleCombinators {

    @Test
    public void testElementAccess() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class);
        vh = MemoryAccessVarHandles.scaleAddress(vh, 1);

        byte[] arr = { 0, 0, -1, 0 };
        MemorySegment segment = MemorySegment.ofArray(arr);
        MemoryAddress addr = segment.baseAddress();

        assertEquals((byte) vh.get(addr, 2), (byte) -1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnalignedElement() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class, 4, ByteOrder.nativeOrder());
        MemoryAccessVarHandles.scaleAddress(vh, 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBadScaleElement() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(int.class);
        vh = MemoryAccessVarHandles.offsetAddress(vh, 4);
        MemoryAccessVarHandles.scaleAddress(vh, 4); //scale factor is too small - should be at least 8!
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlignNotPowerOf2() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class, 3, ByteOrder.nativeOrder());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlignNegative() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class, -1, ByteOrder.nativeOrder());
    }

    @Test
    public void testAlign() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class, 2, ByteOrder.nativeOrder());

        MemorySegment segment = MemorySegment.ofNative(1, 2);
        MemoryAddress address = segment.baseAddress();

        vh.set(address, (byte) 10); // fine, memory region is aligned
        assertEquals((byte) vh.get(address), (byte) 10);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAlignBadAccess() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class, 2, ByteOrder.nativeOrder());
        vh = MemoryAccessVarHandles.offsetAddress(vh, 1); // offset by 1 byte

        MemorySegment segment = MemorySegment.ofNative(2, 2);
        MemoryAddress address = segment.baseAddress();

        vh.set(address, (byte) 10); // should be bad align
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOffsetNegative() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class);
        MemoryAccessVarHandles.offsetAddress(vh, -1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnalignedOffset() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class, 4, ByteOrder.nativeOrder());
        MemoryAccessVarHandles.offsetAddress(vh, 2);
    }

    @Test
    public void testOffset() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(byte.class);
        vh = MemoryAccessVarHandles.offsetAddress(vh, 1);

        MemorySegment segment = MemorySegment.ofArray(new byte[2]);
        MemoryAddress address = segment.baseAddress();

        vh.set(address, (byte) 10);
        assertEquals((byte) vh.get(address), (byte) 10);
    }

    @Test
    public void testByteOrderLE() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(short.class, 2, ByteOrder.LITTLE_ENDIAN);
        byte[] arr = new byte[2];
        MemorySegment segment = MemorySegment.ofArray(arr);
        MemoryAddress address = segment.baseAddress();

        vh.set(address, (short) 0xFF);
        assertEquals(arr[0], (byte) 0xFF);
        assertEquals(arr[1], (byte) 0);
    }

    @Test
    public void testByteOrderBE() {
        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(short.class, 2, ByteOrder.BIG_ENDIAN);
        byte[] arr = new byte[2];
        MemorySegment segment = MemorySegment.ofArray(arr);
        MemoryAddress address = segment.baseAddress();

        vh.set(address, (short) 0xFF);
        assertEquals(arr[0], (byte) 0);
        assertEquals(arr[1], (byte) 0xFF);
    }

    @Test
    public void testNestedSequenceAccess() {
        int outer_size = 10;
        int inner_size = 5;

        //[10 : [5 : [x32 i32]]]

        VarHandle vh = MemoryAccessVarHandles.dereferenceVarHandle(int.class);
        vh = MemoryAccessVarHandles.offsetAddress(vh, 4);
        VarHandle inner_vh = MemoryAccessVarHandles.scaleAddress(vh, 8);
        VarHandle outer_vh = MemoryAccessVarHandles.scaleAddress(inner_vh, 5 * 8);
        int count = 0;
        try (MemorySegment segment = MemorySegment.ofNative(inner_size * outer_size * 8)) {
            for (long i = 0; i < outer_size; i++) {
                for (long j = 0; j < inner_size; j++) {
                    outer_vh.set(segment.baseAddress(), i, j, count);
                    assertEquals(
                            (int)inner_vh.get(segment.baseAddress().offset(i * inner_size * 8), j),
                            count);
                    count++;
                }
            }
        }
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
