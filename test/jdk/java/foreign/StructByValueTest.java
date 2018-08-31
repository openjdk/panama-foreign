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
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;

import org.testng.annotations.*;

import static org.testng.Assert.*;

@Test
public class StructByValueTest {

    @NativeHeader(declarations =
            "make=()$(tuple)" +
            "id=($(tuple))$(tuple)" +
            "big_make=()$(big_tuple)" +
            "big_id=($(big_tuple))$(big_tuple)"
    )
    interface structbyvalue {

        tuple make();
        tuple id(tuple t);

        big_tuple big_make();
        big_tuple big_id(big_tuple t);

        @NativeStruct("[i32(get=one) i32(get=two) i32(get=three) i32(get=four)](tuple)")
        interface tuple extends Struct<tuple> {
            int one();
            int two();
            int three();
            int four();
        }

        @NativeStruct("[i32(get=one) i32(get=two) i32(get=three) i32(get=four) i32(get=five)](big_tuple)")
        interface big_tuple extends Struct<big_tuple> {
            int one();
            int two();
            int three();
            int four();
            int five();
        }
    }

    static structbyvalue lib =
            Libraries.bind(structbyvalue.class,
                    Libraries.loadLibrary(MethodHandles.lookup(), "structbyvalue"));

    @Test
    public void testTuple() {
        structbyvalue.tuple t = lib.make();
        checkTuple(t, 1, 2, 3, 4);
        checkTuple(lib.id(t), 1, 2, 3, 4);
    }

    @Test
    public void testBigTuple() {
        structbyvalue.big_tuple bt = lib.big_make();
        checkBigTuple(bt, 1, 2, 3, 4, 5);
        checkBigTuple(lib.big_id(bt), 1, 2, 3, 4, 5);
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
}
