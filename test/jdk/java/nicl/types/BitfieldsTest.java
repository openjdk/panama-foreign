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

import java.nicl.Scope;
import java.nicl.metadata.NativeStruct;
import java.nicl.types.Struct;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class BitfieldsTest {

    @NativeStruct("[u32(set=setBf)=[" +
            "       u4(get=getFirst)(set=setFirst)" +
            "       u18(get=getSecond)(set=setSecond)" +
            "       u10(get=getThird)(set=setThird)" +
            "       ]]")
    interface MyStruct extends Struct<MyStruct> {
        void setBf(int bf);

        byte getFirst();
        void setFirst(byte first);

        int getSecond();
        void setSecond(int second);

        short getThird();
        void setThird(short third);
    }

    @Test
    public void testBitfieldAccess() {
        try (Scope s = Scope.newNativeScope()) {
            MyStruct m = s.allocateStruct(MyStruct.class);
            m.setBf(0b0000000011_000000000000000010_0001);
            assertEquals(m.getFirst(), 1);
            assertEquals(m.getSecond(), 2);
            assertEquals(m.getThird(), 3);

            m.setFirst((byte)3);
            m.setSecond(42);
            m.setThird((short)60);
            assertEquals(m.getFirst(), 3);
            assertEquals(m.getSecond(), 42);
            assertEquals(m.getThird(), 60);
        }
    }
}
