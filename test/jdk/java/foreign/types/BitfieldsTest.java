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

/*
 * @test
 * @run testng BitfieldsTest
 */

import java.foreign.Scope;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Struct;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class BitfieldsTest {

    interface StructBase extends Struct<StructBase> {
        @NativeSetter("bf")
        void setBf(long bf);

        @NativeGetter("first")
        byte getFirst();
        @NativeSetter("first")
        void setFirst(byte first);

        @NativeGetter("second")
        int getSecond();
        @NativeSetter("second")
        void setSecond(int second);

        @NativeGetter("third")
        short getThird();
        @NativeSetter("third")
        void setThird(short third);
    }

    @NativeStruct("[u16(bf)=[" +
            "       u2(first)" +
            "       u9(second)" +
            "       u5(third)" +
            "       ]]")
    interface MyStruct16 extends StructBase { }

    @NativeStruct("[u32(bf)=[" +
            "       u4(first)" +
            "       u18(second)" +
            "       u10(third)" +
            "       ]]")
    interface MyStruct32 extends StructBase { }

    @NativeStruct("[u64(bf)=[" +
            "       u8(first)" +
            "       u36(second)" +
            "       u20(third)" +
            "       ]]")
    interface MyStruct64 extends StructBase { }

    @Test(dataProvider="bitfields")
    public void testBitfieldAccess(Class structClass, long initMask) {
        try (Scope s = Scope.newNativeScope()) {
            @SuppressWarnings("unchecked")
            StructBase m = (StructBase)s.allocateStruct(structClass);
            m.setBf(initMask);
            assertEquals(m.getFirst(), 1);
            assertEquals(m.getSecond(), 2);
            assertEquals(m.getThird(), 3);

            m.setFirst((byte)3);
            m.setSecond(60);
            m.setThird((short)22);
            assertEquals(m.getFirst(), 3);
            assertEquals(m.getSecond(), 60);
            assertEquals(m.getThird(), 22);
        }
    }

    @DataProvider(name="bitfields")
    Object[][] bitfields() {
        return new Object[][]{
                {MyStruct16.class, 0b00011_000000010_01L},
                {MyStruct32.class, 0b0000000011_000000000000000010_0001L},
                {MyStruct64.class, 0b00000000000000000011_000000000000000000000000000000000010_00000001L}
        };
    }
}
