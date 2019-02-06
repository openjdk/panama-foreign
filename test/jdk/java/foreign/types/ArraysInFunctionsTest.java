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
 * @run testng ArraysInFunctionsTest
 */

import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.Scope;
import java.foreign.annotations.NativeFunction;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Array;
import java.foreign.memory.Callback;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ArraysInFunctionsTest {

    Library lib = Libraries.loadLibrary(MethodHandles.lookup(), "ArraysFunc");

    @NativeHeader
    interface Func {
        @NativeFunction("([2 $(foo)])v")
        void m(Array<Foo> foos);

        @NativeStruct("[ i32(get=i) ](foo)")
        interface Foo extends Struct<Foo> {
            int i();
        }
    }

    @NativeHeader
    interface Callb {
        @NativeFunction("(u64:([2 $(foo)])v)v")
        void g(F f);

        @NativeStruct("[ i32(get=i) ](foo)")
        interface Foo extends Struct<Foo> {
            int i();
        }

        interface F extends Callback<F> {
            @NativeFunction("([2 $(foo)])v")
            void m(Array<Foo> foos);
        }

        @NativeStruct("[ u64(f):([2 $(foo)])v ](callbstr)")
        interface CallbStr extends Struct<CallbStr> {
            @NativeGetter("f")
            F f();
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testFunc() {
        Libraries.bind(Func.class, lib);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCallback() {
        Libraries.bind(Callb.class, lib);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCallbStr() {
        try (Scope s = Scope.newNativeScope()) {
            s.allocateStruct(Callb.CallbStr.class);
        }
    }
}
