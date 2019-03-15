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

import java.foreign.annotations.NativeAddressof;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;

@NativeHeader(path="bitfields.h")
public interface bitfields {

    @NativeStruct("[" +
                    "u64=[" +
                        "i2(x)" +
                        "x6" +
                        "i15(y)" +
                        "x9" +
                        "i20(z)" +
                        "x12]" +
                    "u64=[" +
                        "i13(w)" +
                        "x51]" +
                  "](bitfields1)")
    interface bitfields1 extends Struct<bitfields1> {
        @NativeGetter("x")
        long x$get();
        @NativeSetter("x")
        void x$set(long value);
        @NativeGetter("y")
        long y$get();
        @NativeSetter("y")
        void y$set(long value);
        @NativeGetter("z")
        int z$get();
        @NativeSetter("z")
        void z$set(int value);
        @NativeGetter("w")
        int w$get();
        @NativeSetter("w")
        void w$set(int value);
    }

    @NativeStruct("[" +
                    "u64=[" +
                        "u3(c)" +
                        "u3(c2)" +
                        "x2" +
                        "u7(c3)" +
                        "i4(i)" +
                        "i21(l)" +
                        "x24]" +
                    "u64=[" +
                        "i42(ll)" +
                        "x22]" +
                  "](bitfields2)")
    interface bitfields2 extends Struct<bitfields2> {
        @NativeGetter("c")
        byte c$get();
        @NativeSetter("c")
        void c$set(byte value);
        @NativeGetter("c2")
        byte c2$get();
        @NativeSetter("c2")
        void c2$set(byte value);
        @NativeGetter("c3")
        byte c3$get();
        @NativeSetter("c3")
        void c3$set(byte value);
        @NativeGetter("i")
        int i$get();
        @NativeSetter("i")
        void i$set(int value);
        @NativeGetter("l")
        long l$get();
        @NativeSetter("l")
        void l$set(long value);
        @NativeGetter("ll")
        long ll$get();
        @NativeSetter("ll")
        void ll$set(long value);
    }

    @NativeStruct("[" +
                    "u32=[" +
                        "u4(c1)" +
                        "i20(i)" +
                        "u8(c2)]" +
                    "u32=[" +
                        "i32(l1)]" +
                    "u32=[" +
                        "i32(l2)]" +
                  "](bitfields3)")
    interface bitfields3 extends Struct<bitfields3> {
        @NativeGetter("c1")
        byte c1$get();
        @NativeSetter("c1")
        void c1$set(byte value);
        @NativeGetter("i")
        int i$get();
        @NativeSetter("i")
        void i$set(int value);
        @NativeGetter("c2")
        byte c2$get();
        @NativeSetter("c2")
        void c2$set(byte value);
        @NativeGetter("l1")
        int l1$get();
        @NativeSetter("l1")
        void l1$set(int value);
        @NativeGetter("l2")
        int l2$get();
        @NativeSetter("l2")
        void l2$set(int value);
    }

    @NativeStruct("[" +
                    "i64(l)" +
                    "u64=[" +
                        "u4(c)" +
                        "x60]" +
                  "](bitfields4)")
    interface bitfields4 extends Struct<bitfields4> {
        @NativeGetter("l")
        long l$get();
        @NativeSetter("l")
        void l$set(long value);
        @NativeAddressof("l")
        Pointer<Long> l$ptr();
        @NativeGetter("c")
        byte c$get();
        @NativeSetter("c")
        void c$set(byte value);
    }

    @NativeStruct("[" +
                    "u64=[" +
                        "u7(c)" +
                        "x57]" +
                    "u64=[" +
                        "i63(l)" +
                        "x1]" +
                  "](bitfields5)")
    interface bitfields5 extends Struct<bitfields5> {
        @NativeGetter("c")
        byte c$get();
        @NativeSetter("c")
        void c$set(byte value);
        @NativeGetter("l")
        long l$get();
        @NativeSetter("l")
        void l$set(long value);
    }

    @NativeStruct("[" +
                    "u8=[" +
                        "u4(c1)]|" +
                    "i32=[" +
                        "i20(i)]" +
                  "](bitfields6)")
    interface bitfields6 extends Struct<bitfields6> {
        @NativeGetter("c1")
        byte c1$get();
        @NativeSetter("c1")
        void c1$set(byte value);
        @NativeGetter("i")
        int i$get();
        @NativeSetter("i")
        void i$set(int value);
    }

    @NativeStruct("[" +
                     "u32(x)" +
                     "u32=[u15(a)u17(pad)]" +
                   "](bitfields7)")
    public interface bitfields7 extends Struct<bitfields7> {
         @NativeGetter("x")
         int x$get();
         @NativeSetter("x")
         void x$set(int value);
         @NativeAddressof("x")
         Pointer<Integer> x$ptr();
         @NativeGetter("a")
         int a$get();
         @NativeSetter("a")
         void a$set(int value);
         @NativeGetter("pad")
         int pad$get();
         @NativeSetter("pad")
         void pad$set(int value);
    }

    @NativeStruct("[" +
                  "i32(i)" +
                  "i32(j)" +
                  "](Point)")
    public interface Point extends Struct<Point> {
         @NativeGetter("i")
         int i$get();
         @NativeSetter("i")
         void i$set(int value);
         @NativeAddressof("i")
         Pointer<Integer> i$ptr();
         @NativeGetter("j")
         int j$get();
         @NativeSetter("j")
         void j$set(int value);
         @NativeAddressof("j")
         Pointer<Integer> j$ptr();
    }

    @NativeStruct("[" +
                      "${Point}(p)" +
                      "u32=[i12(x)i2(y)x18]" +
                   "](bitfields8)")
    public interface bitfields8 extends Struct<bitfields8> {
         @NativeGetter("p")
         Point p$get();
         @NativeSetter("p")
         void p$set(Point value);
         Pointer<Point> p$ptr();
         @NativeGetter("x")
         int x$get();
         @NativeSetter("x")
         void x$set(int value);
         @NativeGetter("y")
         int y$get();
         @NativeSetter("y")
         void y$set(int value);
    }

    @NativeStruct("[" +
                "u32=[u1(x)x7u8(y)x16]" +
                "i32(z)" +
            "](bitfields9)")
    public interface bitfields9 extends Struct<bitfields9> {
        @NativeGetter("x")
        int x$get();
        @NativeSetter("x")
        void x$set(int var1);

        @NativeGetter("y")
        int y$get();
        @NativeSetter("y")
        void y$set(int var1);

        @NativeGetter("z")
        int z$get();
        @NativeSetter("z")
        void z$set(int var1);
        @NativeAddressof("z")
        Pointer<Integer> z$ptr();
    }

    @NativeStruct("[u32=[u1(x)]|x64](bitfields10)")
    public interface bitfields10 extends Struct<bitfields.bitfields10> {
        @NativeGetter("x")
        int x$get();

        @NativeSetter("x")
        void x$set(int var1);
    }

}
