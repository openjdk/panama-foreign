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

import java.nicl.Scope;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @test
 * @run testng TestPointer
 * @summary Functional tests for Pointer
 */
public class TestPointer {
    @Test
    public void testAsDirectByteBuffer() {
        final int size = 1000;
        String[] data = { "Panama", "Amber", "Valhalla", "Graal", "Metropolis"};

        try (Scope scope = Scope.newNativeScope()) {
            Pointer<Byte> nativeBuffer = scope.allocateArray(
                    LayoutType.create(byte.class), size);
            ByteBuffer javaBuffer = Pointer.asDirectByteBuffer(nativeBuffer, size).order(ByteOrder.nativeOrder());

            assertTrue(javaBuffer.isDirect());

            javaBuffer.putInt(data.length);

            long offset = Integer.BYTES + Long.BYTES * data.length;
            for (int i = 0; i < data.length; i++) {
                javaBuffer.putLong(offset);
                offset += data[i].getBytes().length;
            }

            for (int i = 0; i < data.length; i++) {
                javaBuffer.put(data[i].getBytes());
            }
            javaBuffer.put((byte) 0);

            assertEquals(javaBuffer.position(), offset + 1);
            assertEquals((int) nativeBuffer.cast(LayoutType.create(int.class)).deref(), data.length);
            Pointer<Long> offsetTable = nativeBuffer.offset(Integer.BYTES).cast(LayoutType.create(long.class));
            for (int i = 0; i < data.length; i++) {
                long offStr = offsetTable.offset(i).deref();
                // Pointer.toString() expect null terminated string
                String str = Pointer.toString(nativeBuffer.offset(offStr));
                assertTrue(str.startsWith(data[i]));
            }

        } catch (IllegalAccessException iae) {
            Assert.fail("Not expecting IAE");
        }
    }
}
