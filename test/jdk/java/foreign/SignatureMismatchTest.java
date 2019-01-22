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
 * @run testng SignatureMismatchTest
 */

import java.foreign.Libraries;
import java.foreign.Scope;
import java.foreign.annotations.NativeFunction;
import java.foreign.annotations.NativeHeader;
import java.foreign.memory.Pointer;

import static java.lang.invoke.MethodHandles.lookup;

import org.testng.annotations.*;

import static org.testng.Assert.*;

@Test
public class SignatureMismatchTest {

    @NativeHeader
    interface Helper1 {
        @NativeFunction("()v")
        void exit(int code); // missing param
    }

    @NativeHeader
    interface Helper2 {
        @NativeFunction("(i32)i32")
        void exit(int code); // return mismatch
    }

    @NativeHeader
    interface Helper3 {
        @NativeFunction("(u64:u8*)i32")
        int printf(Pointer<Byte> message); // missing parameter for varargs
    }

    interface Func {
        @NativeFunction("(i32)v")
        int m(int i); //return mismatch
    }


    @Test(expectedExceptions = RuntimeException.class)
    void testAritiyMismatch() {
        Libraries.bind(lookup(), Helper1.class);
    }

    @Test(expectedExceptions = RuntimeException.class)
    void testReturnMismatch() {
        Libraries.bind(lookup(), Helper2.class);
    }

    @Test(expectedExceptions = RuntimeException.class)
    void testVaridadicMismatch() {
        Libraries.bind(lookup(), Helper3.class);
    }

    @Test(expectedExceptions = RuntimeException.class)
    void testCallbackMismatch() {
        try (Scope sc = Scope.newNativeScope()) {
            sc.allocateCallback(Func.class, i -> i);
        }
    }
}
