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

/*
 * @test
 * @run testng LongDoubleBinderTest
 */

import org.testng.annotations.Test;

import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.util.function.Consumer;

import static org.testng.Assert.assertEquals;

// Make sure at least the binder part works on other ABIs
public class LongDoubleBinderTest {

    private static void withLongDoublePointer(Consumer<Pointer<Double>> action) {
        LayoutType<Double> ld = NativeTypes.LittleEndian.SysVABI.LONGDOUBLE;
        try (Scope scope = Scope.globalScope().fork()) {
            Pointer<Double> ptr = scope.allocate(ld);
            action.accept(ptr);
        }
    }

    @Test
    public void testNormal() {
        withLongDoublePointer(ptr -> {
            ptr.set(10D);
            assertEquals(10D, ptr.get());
        });
    }

    @Test
    public void testReadOnly() {
        withLongDoublePointer(ptr -> {
            ptr.set(10D);
            ptr = ptr.asReadOnly();
            assertEquals(10D, ptr.get());
        });
    }

    @Test
    public void testWriteOnly() {
        withLongDoublePointer(ptr -> {
            Pointer<Double> ptr2 = ptr.asWriteOnly();
            ptr2.set(10D);
            assertEquals(10D, ptr.get());
        });
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testIllegalRead() {
        withLongDoublePointer(ptr -> {
            ptr = ptr.asWriteOnly();
            ptr.get(); // should throw
        });
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testIllegalWrite() {
        withLongDoublePointer(ptr -> {
            ptr = ptr.asReadOnly();
            ptr.set(10D); // should throw
        });
    }

}
