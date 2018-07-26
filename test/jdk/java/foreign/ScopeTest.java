/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
import java.foreign.Scope;
import java.foreign.memory.Array;
import java.foreign.memory.Pointer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

/*
 * @test
 * @run testng ScopeTest
 */
public class ScopeTest {
    private <X> void assertEmptyArray(Array<X> ar, X value) {
        assertEquals(ar.length(), 0);
        assertTrue(ar.elementPointer().isNull());
        try {
            ar.get(0);
            fail("Expect exception");
        } catch (IllegalStateException ex) {
            // ignore
        }

        try {
            ar.set(0, value);
            fail("Expect exception");
        } catch (IllegalStateException ex) {
            // ignore
        }
    }

    @Test
    public void testNullAllocation() {
        try (Scope scope = Scope.newNativeScope()) {
            Pointer<Void> ptr = scope.allocate(NativeTypes.VOID);
            // Do we want an exception or a NULL pointer?
            // I think an exception make more sense
            assertTrue(ptr.isNull());

            assertEmptyArray(scope.allocateArray(NativeTypes.INT8, 0), (byte) 0xAB);
            assertEmptyArray(scope.allocateArray(NativeTypes.INT8, null), (byte) 0xCD);
        }
    }

    @Test
    public void testArrayAllocation() {
        try (Scope scope = Scope.newNativeScope()) {
            Pointer<Byte> ptr = scope.allocate(NativeTypes.INT8);
            assertFalse(ptr.isNull());
            ptr.set((byte) 0xEE);
            assertEquals((byte) ptr.get(), (byte) 0xEE);

            byte[] ar = new byte[] { 0x01, 0x23, 0x45, 0x67,
                    (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF };
            Array<Byte> nar = scope.allocateArray(NativeTypes.INT8, ar);
            for (int i = 0; i < ar.length; i++) {
                assertEquals((int) nar.get(i), ar[i]);
            }
        }
    }

    @Test
    public void testArrayCast() {
        byte[] ar = new byte[Integer.BYTES * 4];
        int[] data = new int[] { 0xDEAFBEEF, 0xBABEFACE, 0xBEEFCAFE, 0xDEAFFACE };
        ByteBuffer bb = ByteBuffer.wrap(ar);
        IntBuffer ib = bb.order(ByteOrder.nativeOrder()).asIntBuffer();
        for (int i : data) {
            ib.put(i);
        }

        try (Scope scope = Scope.newNativeScope()) {
            Array<Byte> nar = scope.allocateArray(NativeTypes.INT8, ar);

            Pointer<Integer> pInt = nar.elementPointer().cast(NativeTypes.VOID).cast(NativeTypes.INT);
            int limit = ar.length / Integer.BYTES;
            assertEquals(limit, 4);

            Array<Integer> arInt = nar.cast(NativeTypes.INT);
            // Cast doesn't recalculate the size :(
            // This can be really bad for upcast like this
            assertEquals(arInt.length(), nar.length());

            arInt = nar.cast(NativeTypes.INT, limit);
            assertEquals(arInt.length(), limit);

            for (int i = 0; i < limit; i++) {
                assertEquals((int) pInt.offset(i).get(), data[i]);
                assertEquals((int) arInt.get(i), data[i]);
            }
        }
    }

    @Test
    public void testAsByteBuffer() {
        int[] data = new int[] { 0xDEAFBEEF, 0xBABEFACE, 0xBEEFCAFE, 0xDEAFFACE };

        try (Scope scope = Scope.newNativeScope()) {
            Array<Integer> nar = scope.allocateArray(NativeTypes.INT, data);

            assertEquals(nar.length(), data.length);
            int byteCounts = Integer.BYTES * data.length;
            assertEquals(nar.elementPointer().bytesSize(), byteCounts);

            Pointer<Byte> ptr = nar.elementPointer().cast(NativeTypes.VOID).cast(NativeTypes.INT8);
            IntBuffer ibNative = Pointer.asDirectByteBuffer(ptr, byteCounts)
                    // got to make sure it's native order
                    .order(ByteOrder.nativeOrder()).asIntBuffer();

            for (int i = 0; i < data.length; i++) {
                assertEquals(ibNative.get(), data[i]);
            }
        } catch (IllegalAccessException iae) {
            fail("Not expecting IAE");
        }
    }
}
