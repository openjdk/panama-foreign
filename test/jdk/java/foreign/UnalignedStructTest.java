/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

/*
 * @test
 * @requires os.family != "windows"
 * @run testng UnalignedStructTest
 */

import org.testng.annotations.*;

import java.foreign.Libraries;
import java.foreign.Scope;
import java.foreign.annotations.NativeFunction;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;
import java.util.Random;
import java.util.stream.IntStream;

import static org.testng.Assert.*;

@Test
public class UnalignedStructTest {

    @NativeHeader
    interface unaligned {
        @NativeFunction("(${str} ${str})i16")
        short unaligned_sum_i1(Str str, Str str2);
        @NativeFunction("(${str} ${str})i16")
        short unaligned_sum_i2(Str str1, Str str2);

        @NativeStruct("[ f128 i16(i1) i16(i2) ](str)")
        interface Str extends Struct<Str> {
            @NativeGetter("i1")
            short i1$get();
            @NativeGetter("i2")
            short i2$get();
            @NativeSetter("i1")
            void i1$set(short i1);
            @NativeSetter("i2")
            void i2$set(short i2);
        }
    }

    static unaligned lib =
            Libraries.bind(unaligned.class,
                    Libraries.loadLibrary(MethodHandles.lookup(), "UnalignedStruct"));


    @Test(dataProvider = "pairs")
    public void testUnalignedCalls(short i1, short i2) {
        try (Scope sc = Scope.globalScope().fork()) {
            unaligned.Str str = sc.allocateStruct(unaligned.Str.class);
            str.i1$set(i1);
            str.i2$set(i2);
            //sanity check
            assertEquals(str.i1$get(), i1);
            assertEquals(str.i2$get(), i2);
            //real check
            assertEquals(lib.unaligned_sum_i1(str, str), i1 + i1);
            assertEquals(lib.unaligned_sum_i2(str, str), i2 + i2);
        }
    }

    static IntStream intStream() {
        //avoid overflow
        return new Random().ints(Byte.MIN_VALUE, Byte.MAX_VALUE)
                .limit(100);
    }

    @DataProvider
    public static Object[][] pairs() {
        int[] i1Arr = intStream().toArray();
        int[] i2Arr = intStream().toArray();
        Object[][] res = new Object[i1Arr.length][];
        for (int i = 0 ; i < i1Arr.length ; i++) {
            res[i] = new Object[] { (short)i1Arr[i], (short)i2Arr[i]};
        }
        return res;
    }
}
