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
 * @bug 8218769
 * @test
 * @run testng UnionTest
 */

import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.NativeAddressof;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test
public class UnionTest {
    private static byte[] DATA = { 0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0 };
    private static final long LEL = 0xF0DEBC9A78563412L;
    private static final int[] LEI = { 0x78563412, 0xF0DEBC9A };
    private static final short[] LES =  { 0x3412, 0x7856, (short) 0xBC9A, (short) 0xF0DE };

    @NativeStruct("[u16(s)|u32(i)|u64(l)]")
    interface SimpleUnion extends Struct<SimpleUnion> {
        @NativeGetter("s")
        short s$get();
        @NativeSetter("s")
        void s$set(short s);
        @NativeAddressof("s")
        Pointer<Short> s$ptr();

        @NativeGetter("i")
        int i$get();
        @NativeSetter("i")
        void i$set(int s);
        @NativeAddressof("i")
        Pointer<Integer> i$ptr();

        @NativeGetter("l")
        long l$get();
        @NativeSetter("l")
        void l$set(long l);
        @NativeAddressof("l")
        Pointer<Long> l$ptr();
    }

    @Test
    public void VerifyLayout() throws IllegalAccessException {
        try (Scope s = Scope.newNativeScope()) {
            SimpleUnion union = s.allocateStruct(SimpleUnion.class);
            assertEquals(union.s$ptr().addr(), union.ptr().addr());
            assertEquals(union.i$ptr().addr(), union.ptr().addr());
            assertEquals(union.l$ptr().addr(), union.ptr().addr());

            Pointer<Byte> data = union.ptr().cast(NativeTypes.VOID).cast(NativeTypes.UINT8);
            for (int i = 0; i < DATA.length; i++) {
                data.offset(i).set(DATA[i]);
            }

            assertEquals(union.s$get(), LES[0]);
            assertEquals(union.i$get(), LEI[0]);
            assertEquals(union.l$get(), LEL);
        }
    }
}