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
 */

import java.lang.invoke.MethodHandles;
import java.foreign.*;
import java.foreign.memory.*;
import java.foreign.annotations.*;

public class DuplicateStructs {
    @NativeHeader
    static interface duplicate {
        @NativeStruct("[i32(get=i$get)(set=i$set)](mystruct)")
        static interface MyStruct extends Struct<MyStruct> {
            @NativeGetter("i")
            int i$get();
            @NativeSetter("i")
            void i$set(int i);
        }

        @NativeStruct("[f32(get=f$get)(set=f$set)i32(get=i$get)(set=i$set)](mystruct)")
        static interface MyStruct2 extends Struct<MyStruct2> {
            @NativeGetter("f")
            float f$get();
            @NativeSetter("f")
            void f$set(float f);
            @NativeGetter("i")
            int i$get();
            @NativeSetter("i")
            void i$set(int i);
        }

        @NativeGetter("struct1")
        public abstract MyStruct struct1$get();
        @NativeSetter("struct1")
        public abstract void struct1$set(MyStruct arg);
        @NativeAddressof("struct1")
        public abstract Pointer<MyStruct> struct1$ptr();
    }

    public static void main(String[] args) {
        try {
            Libraries.bind(MethodHandles.lookup(), duplicate.class);
            throw new AssertionError("should not reach here");
        } catch (RuntimeException re) {
            System.err.println("Got as expected: " + re);
            re.printStackTrace();
        }
    }
}
