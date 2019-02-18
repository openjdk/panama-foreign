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

import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Struct;

public class Nested {
    @NativeStruct("[ ${A2}(next) ](A1)")
    interface A1 extends Struct<A1> {
        @NativeGetter("next")
        A2 next$get();
        @NativeSetter("next")
        void next$set(A2 b);
    }

    @NativeStruct("[ ${A3}(next) ](A2)")
    interface A2 extends Struct<A2> {
        @NativeGetter("next")
        A3 next$get();
        @NativeSetter("next")
        void next$set(A3 b);
    }

    @NativeStruct("[ ${A4}(next) ](A3)")
    interface A3 extends Struct<A3> {
        @NativeGetter("next")
        A4 next$get();
        @NativeSetter("next")
        void next$set(A4 b);
    }

    @NativeStruct("[ i32(i) ](A4)")
    interface A4 extends Struct<A4> {
        @NativeGetter("i")
        int i$get();
        @NativeSetter("i")
        void i$set(int i);
    }
}
