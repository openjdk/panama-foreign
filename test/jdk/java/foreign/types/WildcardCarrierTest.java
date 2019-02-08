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
 * @run testng WildcardCarrierTest
 */

import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.memory.Array;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandles;

import org.testng.annotations.*;
import static org.testng.Assert.*;

public class WildcardCarrierTest {

    @SuppressWarnings("rawtypes")
    interface RawLib {
        Array arr_int();
        Pointer ptr_ptr_int();
        Array arr_ptr_int();
    }

    @NativeHeader(globals = {"[5 i32](arr_int)", "u64(ptr_ptr_int):u64:i32", "[5 u64:i32](arr_ptr_int)"})
    interface LibNoWild extends RawLib {
        @NativeGetter("arr_int")
        Array<Integer> arr_int();
        @NativeGetter("ptr_ptr_int")
        Pointer<Pointer<Integer>> ptr_ptr_int();
        @NativeGetter("arr_ptr_int")
        Array<Pointer<Integer>> arr_ptr_int();
    }

    @NativeHeader(globals = {"[5 i32](arr_int)", "u64(ptr_ptr_int):u64:i32", "[5 u64:i32](arr_ptr_int)"})
    interface LibExtWild extends RawLib {
        @NativeGetter("arr_int")
        Array<? extends Integer> arr_int();
        @NativeGetter("ptr_ptr_int")
        Pointer<? extends Pointer<? extends Integer>> ptr_ptr_int();
        @NativeGetter("arr_ptr_int")
        Array<? extends Pointer<? extends Integer>> arr_ptr_int();
    }
    
    @NativeHeader(globals = {"[5 i32](arr_int)", "u64(ptr_ptr_int):u64:i32", "[5 u64:i32](arr_ptr_int)"})
    interface LibSupWild extends RawLib {
        @NativeGetter("arr_int")
        Array<? super Integer> arr_int();
        @NativeGetter("ptr_ptr_int")
        Pointer<? super Pointer<? super Integer>> ptr_ptr_int();
        @NativeGetter("arr_ptr_int")
        Array<? super Pointer<? super Integer>> arr_ptr_int();
    }

    @NativeHeader(globals = {"[5 i32](arr_int)", "u64(ptr_ptr_int):u64:i32", "[5 u64:i32](arr_ptr_int)"})
    interface LibMixWild extends RawLib {
        @NativeGetter("arr_int")
        Array<? super Integer> arr_int();
        @NativeGetter("ptr_ptr_int")
        Pointer<? extends Pointer<? super Integer>> ptr_ptr_int();
        @NativeGetter("arr_ptr_int")
        Array<? super Pointer<? extends Integer>> arr_ptr_int();
    }

    @Test(dataProvider = "wildLibs")
    public void testWild(Class<? extends RawLib> wildLib) {
        Library lib = Libraries.loadLibrary(MethodHandles.lookup(), "Wild");
        RawLib rlib = Libraries.bind(wildLib, lib);
        testRaw(rlib);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void testRaw(RawLib rt) {
        Array arr_int = rt.arr_int();
        Pointer ptr_ptr_int = rt.ptr_ptr_int();
        Array arr_ptr_int = rt.arr_ptr_int();
        for (int i = 0 ; i < arr_int.length() ; i++) {
            Integer expected = (Integer)arr_int.get(i);
            assertEquals(expected, ((Pointer)ptr_ptr_int.offset(i).get()).get());
            assertEquals(expected, ((Pointer)arr_ptr_int.get(i)).get());
        }
    }
    
    @DataProvider
    public static Object[][] wildLibs() {
        Object[][] res = {
                { LibNoWild.class },
                { LibExtWild.class },
                { LibSupWild.class },
                { LibMixWild.class },
        };
        return res;
    }
}
