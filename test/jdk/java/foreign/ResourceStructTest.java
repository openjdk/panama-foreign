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

import org.testng.annotations.Test;

import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.NativeAddressof;
import java.foreign.annotations.NativeCallback;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Array;
import java.foreign.memory.Callback;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;

import static org.testng.Assert.*;

/*
 * @test
 * @run testng ResourceStructTest
 */

public class ResourceStructTest {

    @NativeStruct("[" +
            "i32(x)" +
            "i32(y)" +
            "](nested)")
    public interface nested extends Struct<nested> {
        @NativeGetter("x")
        int x$get();
        @NativeSetter("x")
        void x$set(int var1);
        @NativeAddressof("x")
        Pointer<Integer> x$ptr();

        @NativeGetter("y")
        int y$get();
        @NativeSetter("y")
        void y$set(int var1);
        @NativeAddressof("y")
        Pointer<Integer> y$ptr();
    }

    @NativeStruct("[" +
            "u64(cb):(i32)v" +
            "u64(xp):i32" +
            "[3i32](arr)" +
            "${nested}(n)" +
            "x32" +
            "](resources)")
    public interface resources extends Struct<resources> {
        @NativeGetter("cb")
        Callback<FI1> cb$get();
        @NativeSetter("cb")
        void cb$set(Callback<FI1> var1);
        @NativeAddressof("cb")
        Pointer<Callback<FI1>> cb$ptr();

        @NativeGetter("xp")
        Pointer<Integer> xp$get();
        @NativeSetter("xp")
        void xp$set(Pointer<Integer> var1);
        @NativeAddressof("xp")
        Pointer<Pointer<Integer>> xp$ptr();

        @NativeGetter("arr")
        Array<Integer> arr$get();
        @NativeSetter("arr")
        void arr$set(Array<Integer> var1);
        @NativeAddressof("arr")
        Pointer<Array<Integer>> arr$ptr();

        @NativeGetter("n")
        nested n$get();
        @NativeSetter("n")
        void n$set(nested var1);
        @NativeAddressof("n")
        Pointer<nested> n$ptr();
    }

    @FunctionalInterface
    @NativeCallback("()v")
    public interface FI1 {
        void fn();
    }

    @Test
    public void testPointerInferScope() {
        try(Scope scope = Scope.globalScope().fork()) {
            resources recs = scope.allocateStruct(resources.class);
            recs.xp$set(scope.allocate(NativeTypes.INT32));
            Pointer<Integer> p = recs.xp$get();
            assertEquals(p.scope(), scope);
        }
    }

    @Test
    public void testCallbackInferScope() {
        try(Scope scope = Scope.globalScope().fork()) {
            resources recs = scope.allocateStruct(resources.class);
            recs.cb$set(scope.allocateCallback(() -> {}));
            Callback<FI1> cb = recs.cb$get();
            assertEquals(cb.entryPoint().scope(), scope);
        }
    }

    @Test
    public void testArrayInferScope() {
        try(Scope scope = Scope.globalScope().fork()) {
            resources recs = scope.allocateStruct(resources.class);
            recs.arr$set(scope.allocateArray(NativeTypes.INT32, 3));
            Array<Integer> arr = recs.arr$get();
            assertEquals(arr.elementPointer().scope(), scope);
        }
    }

    @Test
    public void testStructInferScope() {
        try(Scope scope = Scope.globalScope().fork()) {
            resources recs = scope.allocateStruct(resources.class);
            recs.n$set(scope.allocateStruct(nested.class));
            nested str = recs.n$get();
            assertEquals(str.ptr().scope(), scope);
        }
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Access denied.*")
    public void testPointerSetInChildScope() {
        try(Scope scope = Scope.globalScope().fork()) {
            try(Scope childScope = scope.fork()) {
                resources recs = scope.allocateStruct(resources.class);
                recs.xp$set(childScope.allocate(NativeTypes.INT32)); // should throw
            }
        }
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Access denied.*")
    public void testCallbackSetInChildScope() {
        try(Scope scope = Scope.globalScope().fork()) {
            try(Scope childScope = scope.fork()) {
                resources recs = scope.allocateStruct(resources.class);
                recs.cb$set(childScope.allocateCallback(() -> {})); // should throw
            }
        }
    }

    @Test
    public void testArraySetInChildScope() {
        try(Scope scope = Scope.globalScope().fork()) {
            try(Scope childScope = scope.fork()) {
                resources recs = scope.allocateStruct(resources.class);
                recs.arr$set(childScope.allocateArray(NativeTypes.INT32, 3)); // should not throw
            }
        }
    }

    @Test
    public void testStructSetInChildScope() {
        try(Scope scope = Scope.globalScope().fork()) {
            try(Scope childScope = scope.fork()) {
                resources recs = scope.allocateStruct(resources.class);
                recs.n$set(childScope.allocateStruct(nested.class)); // should not throw
            }
        }
    }
}
