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
 * @run testng IncompleteArrayTest
 */

import org.testng.annotations.Test;

import java.foreign.Libraries;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.*;
import java.foreign.memory.Array;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;

import static org.testng.Assert.*;

public class IncompleteArrayTest {

    @NativeHeader
    interface IncompleteArray {
        @NativeFunction("(u64:[i32x32[0u64:v]])u64:u64:v")
        Pointer<Pointer<?>> GetArrayData(Pointer<Foo> fooPtr);

        @NativeFunction("([i32x32[0u64:v]])u64:v")
        Pointer<?> GetData(Foo fooPtr);

        @NativeStruct("[i32(length)x32[0u64:v](data)](Foo)")
        interface Foo extends Struct<Foo> {
            @NativeGetter("length")
            int length$get();
            @NativeSetter("length")
            void length$set(int var1);
            @NativeAddressof("length")
            Pointer<Integer> length$ptr();

            @NativeGetter("data")
            Array<Pointer<Void>> data$get();
            @NativeSetter("data")
            void data$set(Array<Pointer<Void>> var1);
            @NativeAddressof("data")
            Pointer<Array<Pointer<Void>>> data$ptr();
        }
    }

    private static final IncompleteArray lib = Libraries.bind(IncompleteArray.class,
            Libraries.loadLibrary(MethodHandles.lookup(), "IncompleteArray"));

    @Test
    public void incompleteArrayTest() {
        try(Scope scope = Scope.newNativeScope()) {
            Pointer<IncompleteArray.Foo> fooPtr = scope.allocate(LayoutType.ofStruct(IncompleteArray.Foo.class));
            Pointer<Pointer<?>> retP = lib.GetArrayData(fooPtr);
            assertTrue(retP.isNull());
            IncompleteArray.Foo foo = fooPtr.get();
            Pointer<?> ret = lib.GetData(foo);
            assertTrue(ret.isNull());

            foo.length$set(10);
            assertEquals(foo.length$get(), 10);

            Array<Pointer<Void>> arr = scope.allocateArray(NativeTypes.VOID.pointer(), 0);
            assertEquals(arr.length(), 0);
            foo.data$set(arr);
            Array<Pointer<Void>> arrRet = foo.data$get();
            assertEquals(arrRet.length(), 0);
        }
    }

}
