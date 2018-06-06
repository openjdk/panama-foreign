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
import java.nicl.*;
import java.nicl.metadata.*;
import java.nicl.types.*;

public class PaddedStructTest {
    @NativeHeader(libraries = { "PaddedStruct" }, declarations="func=($(MyStruct))$(MyStruct)")
    static interface PaddedStruct {
        @NativeStruct(
          "[u8(get=c$get)(set=c$set)(ptr=c$ptr)x24i32(get=i$get)(set=i$set)(ptr=i$ptr)](MyStruct)"
        )
        public interface MyStruct extends Struct<MyStruct> {
            byte c$get();
            void c$set(byte c);
            Pointer<Byte> c$ptr();
            int i$get();
            void i$set(int i);
            Pointer<Integer> i$ptr();
        }

        MyStruct func(MyStruct ms);
    }

    public static void main(String[] args) {
        PaddedStruct ps = Libraries.bind(MethodHandles.lookup(), PaddedStruct.class);
        try (Scope s = Scope.newNativeScope()) {
            PaddedStruct.MyStruct ms = s.allocateStruct(PaddedStruct.MyStruct.class);
            ms.c$set((byte)65);
            ms.i$set(100);
            ms = ps.func(ms);
            if (ms.c$get() != 66) {
                throw new RuntimeException("MyStruct.c is expected to be 66");
            }
            if (ms.i$get() != 142) {
                throw new RuntimeException("MyStruct.i is expected to be 142");
            }
        }
    }
}
