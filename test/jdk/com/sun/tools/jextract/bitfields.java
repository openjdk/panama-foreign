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

package com.acme;

import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;

@NativeHeader(path="bitfields.h")
public interface bitfields {

    @NativeStruct("[" +
                    "u64=[" +
                        "i2(get=x$get)(set=x$set)" +
                        "x6" +
                        "i15(get=y$get)(set=y$set)" +
                        "x9" +
                        "i20(get=z$get)(set=z$set)" +
                        "x12]" +
                    "u64=[" +
                        "i13(get=w$get)(set=w$set)" +
                        "x51]" +
                  "](bitfields1)")
    interface bitfields1 extends Struct<bitfields1> {
        long x$get();
        void x$set(long value);
        long y$get();
        void y$set(long value);
        int z$get();
        void z$set(int value);
        int w$get();
        void w$set(int value);
    }

    @NativeStruct("[" +
                    "u64=[" +
                        "u3(get=c$get)(set=c$set)" +
                        "u3(get=c2$get)(set=c2$set)" +
                        "x2" +
                        "u7(get=c3$get)(set=c3$set)" +
                        "i4(get=i$get)(set=i$set)" +
                        "i21(get=l$get)(set=l$set)" +
                        "x24]" +
                    "u64=[" +
                        "i42(get=ll$get)(set=ll$set)" +
                        "x22]" +
                  "](bitfields2)")
    interface bitfields2 extends Struct<bitfields2> {
        byte c$get();
        void c$set(byte value);
        byte c2$get();
        void c2$set(byte value);
        byte c3$get();
        void c3$set(byte value);
        int i$get();
        void i$set(int value);
        long l$get();
        void l$set(long value);
        long ll$get();
        void ll$set(long value);
    }

    @NativeStruct("[" +
                    "u32=[" +
                        "u4(get=c1$get)(set=c1$set)" +
                        "i20(get=i$get)(set=i$set)" +
                        "u8(get=c2$get)(set=c2$set)]" +
                    "u32=[" +
                        "i32(get=l1$get)(set=l1$set)]" +
                    "u32=[" +
                        "i32(get=l2$get)(set=l2$set)]" +
                  "](bitfields3)")
    interface bitfields3 extends Struct<bitfields3> {
        byte c1$get();
        void c1$set(byte value);
        int i$get();
        void i$set(int value);
        byte c2$get();
        void c2$set(byte value);
        int l1$get();
        void l1$set(int value);
        int l2$get();
        void l2$set(int value);
    }

    @NativeStruct("[" +
                    "i64(get=l$get)(set=l$set)(ptr=l$ptr)" +
                    "u64=[" +
                        "u4(get=c$get)(set=c$set)" +
                        "x60]" +
                  "](bitfields4)")
    interface bitfields4 extends Struct<bitfields4> {
        long l$get();
        void l$set(long value);
        Pointer<Long> l$ptr();
        byte c$get();
        void c$set(byte value);
    }

    @NativeStruct("[" +
                    "u64=[" +
                        "u7(get=c$get)(set=c$set)" +
                        "x57]" +
                    "u64=[" +
                        "i63(get=l$get)(set=l$set)" +
                        "x1]" +
                  "](bitfields5)")
    interface bitfields5 extends Struct<bitfields5> {
        byte c$get();
        void c$set(byte value);
        long l$get();
        void l$set(long value);
    }

    @NativeStruct("[" +
                    "u8=[" +
                        "u4(get=c1$get)(set=c1$set)]|" +
                    "i32=[" +
                        "i20(get=i$get)(set=i$set)]" +
                  "](bitfields6)")
    interface bitfields6 extends Struct<bitfields6> {
        byte c1$get();
        void c1$set(byte value);
        int i$get();
        void i$set(int value);
    }
}
