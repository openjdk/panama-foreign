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

import java.foreign.Libraries;
import java.foreign.Scope;
import java.foreign.annotations.*;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;

public class RegisterStructTest {
    @NativeHeader(libraries = { "RegisterStruct" })
    interface RegisterStruct {
        @NativeStruct(
          "[u8(c) x24 i32(i) f64(l)](RegStruct)"
        )
        interface RegStruct extends Struct<RegStruct> {
            @NativeGetter("c")
            byte c$get();
            @NativeSetter("c")
            void c$set(byte c);
            @NativeGetter("i")
            int i$get();
            @NativeSetter("i")
            void i$set(int i);
            @NativeGetter("l")
            double l$get();
            @NativeSetter("l")
            void l$set(double i);
        }

        @NativeFunction("(${RegStruct})u8")
        byte get_c(RegStruct ms);
        @NativeFunction("(${RegStruct})i32")
        int get_i(RegStruct ms);
        @NativeFunction("(${RegStruct})f64")
        double get_l(RegStruct ms);
    }

    public static void main(String[] args) {
        RegisterStruct l = Libraries.bind(MethodHandles.lookup(), RegisterStruct.class);
        try (Scope s = Scope.newNativeScope()) {
            RegisterStruct.RegStruct rs = s.allocateStruct(RegisterStruct.RegStruct.class);
            rs.c$set((byte)65);
            rs.i$set(100);
            rs.l$set((double) Integer.MAX_VALUE * 10d);
            checkEquals(rs.c$get(), l.get_c(rs));
            checkEquals(rs.i$get(), l.get_i(rs));
            checkEquals(rs.l$get(), l.get_l(rs));
        }
    }

    static void checkEquals(Object o1, Object o2) {
        if (!o1.equals(o2)) {
            throw new AssertionError("Mismatch - " + o1 + " != " + o2);
        }
    }
}
