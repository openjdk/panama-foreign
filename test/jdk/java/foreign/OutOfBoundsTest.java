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
 * @run testng OutOfBoundsTest
 */

import org.testng.annotations.Test;

import java.foreign.Libraries;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.NativeAddressof;
import java.foreign.annotations.NativeFunction;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;

public class OutOfBoundsTest {

    @NativeHeader
    public interface OutOfBounds {
        @NativeFunction("(u64:u8)v")
        void ptr(Pointer<Byte> ptr);
    }

    @NativeStruct("[u64(ptr):u8](ptrStruct)")
    public interface ptrStruct extends Struct<ptrStruct> {
        @NativeGetter("ptr")
        Pointer<Byte> ptr$get();
        @NativeSetter("ptr")
        void ptr$set(Pointer<Byte> var1);
        @NativeAddressof("ptr")
        Pointer<Pointer<Byte>> ptr$ptr();
    }

    private static final OutOfBounds lib = Libraries.bind(OutOfBounds.class, Libraries.loadLibrary(MethodHandles.lookup(), "OutOfBounds"));

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testPointerOOB() {
        try(Scope scope = Scope.newNativeScope()) {
            Pointer<Byte> p = scope.allocate(NativeTypes.INT8);
            p = p.offset(1); // oob
            lib.ptr(p); // should throw
        }
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testStructPointerOOB() {
        try(Scope scope = Scope.newNativeScope()) {
            Pointer<Byte> p = scope.allocate(NativeTypes.INT8);
            p = p.offset(1); // oob
            ptrStruct s = scope.allocateStruct(ptrStruct.class);
            s.ptr$set(p); // should throw
        }
    }
}
