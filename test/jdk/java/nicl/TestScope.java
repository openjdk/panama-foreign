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

import org.testng.annotations.Test;

import java.nicl.Scope;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @test
 * @run testng TestScope
 * @summary Functional test for Scope
 */
public class TestScope {
    final static LayoutType<Byte> BYTE = LayoutType.create(byte.class);

    @Test
    public void testZeroLength() {
        try (Scope scope = Scope.newNativeScope()) {
            Pointer<Byte> cstr = scope.toCString("");
            assertEquals((byte) cstr.deref(), (byte) 0);

            Pointer<Byte> ar = scope.toNativeBuffer(new byte[0]);
            assertTrue(ar.isNull());

            ar = scope.allocateArray(BYTE, 0);
            assertTrue(ar.isNull());

            ar = scope.toNativeArray(BYTE, new String[0], s -> (byte) s.length());
            assertTrue(ar.isNull());

            ar = scope.toNativeArray(BYTE, Collections.emptyList(), o -> (byte) o.hashCode());
            assertTrue(ar.isNull());
        }
    }

    @Test
    public void testToNativeArray() {
        String[] data = { "Uno", "Dos", "Tres" };
        try (Scope scope = Scope.newNativeScope()) {
            Pointer<Pointer<Byte>> ar1 = scope.toNativeArray(
                    LayoutType.create(byte.class).ptrType(), data,
                    scope::toCString);

            Pointer<Pointer<Byte>> ar2 = scope.toNativeArray(
                    LayoutType.create(byte.class).ptrType(), List.of(data),
                    scope::toCString);

            Pointer<Pointer<Byte>> ar3 = scope.toCStrArray(data);

            for (int i = 0; i < data.length; i++) {
                assertEquals(Pointer.toString(ar1.offset(i).deref()), data[i]);
                assertEquals(Pointer.toString(ar2.offset(i).deref()), data[i]);
                assertEquals(Pointer.toString(ar3.offset(i).deref()), data[i]);
            }

            Pointer<Integer> sizes = scope.toNativeArray(LayoutType.create(int.class), data,
                    String::length);
            for (int i = 0; i < data.length; i++) {
                assertEquals((int) sizes.offset(i).deref(), data[i].length());
            }
        }
    }
}
