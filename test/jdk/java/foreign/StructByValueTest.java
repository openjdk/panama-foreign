/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 * @run testng StructByValueTest
 */
import java.foreign.Libraries;
import java.foreign.annotations.NativeFunction;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;

import org.testng.annotations.*;

import static org.testng.Assert.*;

@Test
public class StructByValueTest {

    @NativeHeader
    interface structbyvalue {

        @NativeFunction("()${small_tuple}")
        small_tuple small_make();
        @NativeFunction("(${small_tuple})${small_tuple}")
        small_tuple small_id(small_tuple t);
        @NativeFunction("(${small_tuple})${small_tuple}")
        small_tuple small_zero(small_tuple t);

        @NativeFunction("()${tuple}")
        tuple make();
        @NativeFunction("(${tuple})${tuple}")
        tuple id(tuple t);
        @NativeFunction("(${tuple})${tuple}")
        tuple zero(tuple t);

        @NativeFunction("()${big_tuple}")
        big_tuple big_make();
        @NativeFunction("(${big_tuple})${big_tuple}")
        big_tuple big_id(big_tuple t);
        @NativeFunction("(${big_tuple})${big_tuple}")
        big_tuple big_zero(big_tuple t);

        @NativeStruct("[i32(one) i32(two)](small_tuple)")
        interface small_tuple extends Struct<small_tuple> {
            @NativeGetter("one")
            int one();
            @NativeGetter("two")
            int two();
        }

        @NativeStruct("[i32(one) i32(two) i32(three) i32(four)](tuple)")
        interface tuple extends Struct<tuple> {
            @NativeGetter("one")
            int one();
            @NativeGetter("two")
            int two();
            @NativeGetter("three")
            int three();
            @NativeGetter("four")
            int four();
        }

        @NativeStruct("[i32(one) i32(two) i32(three) i32(four) i32(five)](big_tuple)")
        interface big_tuple extends Struct<big_tuple> {
            @NativeGetter("one")
            int one();
            @NativeGetter("two")
            int two();
            @NativeGetter("three")
            int three();
            @NativeGetter("four")
            int four();
            @NativeGetter("five")
            int five();
        }
    }

    static structbyvalue lib =
            Libraries.bind(structbyvalue.class,
                    Libraries.loadLibrary(MethodHandles.lookup(), "structbyvalue"));

    public void testSmallTuple() {
        structbyvalue.small_tuple t = lib.small_make();
        checkSmallTuple(t, 1, 2);
        checkSmallTuple(lib.small_id(t), 1, 2);
        checkSmallTuple(lib.small_zero(t), 0, 0);
        checkSmallTuple(t, 1, 2);
    }

    @Test
    public void testTuple() {
        structbyvalue.tuple t = lib.make();
        checkTuple(t, 1, 2, 3, 4);
        checkTuple(lib.id(t), 1, 2, 3, 4);
        checkTuple(lib.zero(t), 0, 0, 0, 0);
        checkTuple(t, 1, 2, 3, 4);
    }

    @Test
    public void testBigTuple() {
        structbyvalue.big_tuple bt = lib.big_make();
        checkBigTuple(bt, 1, 2, 3, 4, 5);
        checkBigTuple(lib.big_id(bt), 1, 2, 3, 4, 5);
        checkBigTuple(lib.big_zero(bt), 0, 0, 0, 0, 0);
        checkBigTuple(bt, 1, 2, 3, 4, 5);
    }

    static void checkTuple(structbyvalue.tuple t, int one, int two, int three, int four) {
        assertEquals(t.one(), one);
        assertEquals(t.two(), two);
        assertEquals(t.three(), three);
        assertEquals(t.four(), four);
    }

    static void checkBigTuple(structbyvalue.big_tuple t, int one, int two, int three, int four, int five) {
        assertEquals(t.one(), one);
        assertEquals(t.two(), two);
        assertEquals(t.three(), three);
        assertEquals(t.four(), four);
        assertEquals(t.five(), five);
    }

    static void checkSmallTuple(structbyvalue.small_tuple t, int one, int two) {
        assertEquals(t.one(), one);
        assertEquals(t.two(), two);
    }
}
