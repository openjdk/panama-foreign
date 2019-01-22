/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
import java.foreign.annotations.NativeFunction;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeLocation;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;

/**
 * This test is platform dependent, as the C type size may vary on platform.
 * Current value is based on x64 with __LP64__.
 */
@NativeHeader(path="simple.h", globals =
        {"i32(global)", "${anonymous}(basics)", "u64(unsigned_int):${_unsigned}"}
)
public interface simple {
    @NativeLocation(file="simple.h", line=26, column=5)
    @NativeGetter("global")
    public int global$get();
    @NativeSetter("global")
    public void global$set(int arg);
    @NativeAddressof("global")
    public Pointer<Integer> global$ptr();

    @NativeLocation(file="simple.h", line=32, column=8)
    @NativeStruct("[" +
            "u8(ch)" +
            "i8(sch)" +
            "i16(s)" +
            "i32(n)" +
            "i32(l)" +  // platform dependent
            "x32" +
            "i64(ll)" +
            "f32(f)" +
            "x32" +
            "f64(d)" +
            "f64(ld)" +  // platform dependent
            "](anonymous)")
    public static interface anonymous extends Struct<anonymous> {
        @NativeLocation(file="simple.h", line=33, column=10)
        @NativeGetter("ch")
        public byte ch$get();
        @NativeSetter("ch")
        public void ch$set(byte arg);
        @NativeAddressof("ch")
        public Pointer<Byte> ch$ptr();

        @NativeLocation(file="simple.h", line=34, column=17)
        @NativeGetter("sch")
        public byte sch$get();
        @NativeSetter("sch")
        public void sch$set(byte arg);
        @NativeAddressof("sch")
        public Pointer<Byte> sch$ptr();

        @NativeLocation(file="simple.h", line=35, column=11)
        @NativeGetter("s")
        public short s$get();
        @NativeSetter("s")
        public void s$set(short arg);
        @NativeAddressof("s")
        public Pointer<Short> s$ptr();

        @NativeLocation(file="simple.h", line=36, column=9)
        @NativeGetter("n")
        public int n$get();
        @NativeSetter("n")
        public void n$set(int arg);
        @NativeAddressof("n")
        public Pointer<Integer> n$ptr();

        @NativeLocation(file="simple.h", line=37, column=10)
        @NativeGetter("l")
        public int l$get();
        @NativeSetter("l")
        public void l$set(int arg);
        @NativeAddressof("l")
        public Pointer<Integer> l$ptr();

        @NativeLocation(file="simple.h", line=38, column=15)
        @NativeGetter("ll")
        public long ll$get();
        @NativeSetter("ll")
        public void ll$set(long arg);
        @NativeAddressof("ll")
        public Pointer<Long> ll$ptr();

        @NativeLocation(file="simple.h", line=39, column=11)
        @NativeGetter("f")
        public float f$get();
        @NativeSetter("f")
        public void f$set(float arg);
        @NativeAddressof("f")
        public Pointer<Float> f$ptr();

        @NativeLocation(file="simple.h", line=40, column=12)
        @NativeGetter("d")
        public double d$get();
        @NativeSetter("d")
        public void d$set(double arg);
        @NativeAddressof("d")
        public Pointer<Double> d$ptr();

        @NativeLocation(file="simple.h", line=41, column=17)
        @NativeGetter("ld")
        public double ld$get();
        @NativeSetter("ld")
        public void ld$set(double arg);
        @NativeAddressof("ld")
        public Pointer<Double> ld$ptr();
    }

    @NativeLocation(file="simple.h", line=42, column=3)
    @NativeGetter("basics")
    public anonymous basics$get();
    @NativeSetter("basics")
    public void basics$set(anonymous arg);
    @NativeAddressof("basics")
    public Pointer<anonymous> basics$ptr();

    @NativeLocation(file = "simple.h", line = 45, column = 8)
    @NativeStruct("[" +
            "u8(b)" +
            "u8(ch)" +
            "u16(s)" +
            "u32(n)" +
            "u32(l)" +  // platform dependent
            "x32" +
            "u64(ll)" +
            "](_unsigned)")
    public static interface _unsigned extends Struct<_unsigned> {
        @NativeLocation(file="simple.h", line=46, column=11)
        @NativeGetter("b")
        public boolean b$get();
        @NativeSetter("b")
        public void b$set(boolean arg);
        @NativeAddressof("b")
        public Pointer<Boolean> b$ptr();

        @NativeLocation(file="simple.h", line=47, column=19)
        @NativeGetter("ch")
        public byte ch$get();
        @NativeSetter("ch")
        public void ch$set(byte c);
        @NativeAddressof("ch")
        public Pointer<Byte> ch$ptr();

        @NativeLocation(file="simple.h", line=48, column=20)
        @NativeGetter("s")
        public short s$get();
        @NativeSetter("s")
        public void s$set(short s);
        @NativeAddressof("s")
        public Pointer<Short> s$ptr();

        @NativeLocation(file="simple.h", line=49, column=18)
        @NativeGetter("n")
        public int n$get();
        @NativeSetter("n")
        public void n$set(int i);
        @NativeAddressof("n")
        public Pointer<Integer> n$ptr();

        @NativeLocation(file="simple.h", line=50, column=19)
        @NativeGetter("l")
        public int l$get();
        @NativeSetter("l")
        public void l$set(int l);
        @NativeAddressof("l")
        public Pointer<Integer> l$ptr();

        @NativeLocation(file="simple.h", line=51, column=24)
        @NativeGetter("ll")
        public long ll$get();
        @NativeSetter("ll")
        public void ll$set(long l);
        @NativeAddressof("ll")
        public Pointer<Long> ll$ptr();
    }

    @NativeLocation(file="simple.h", line=52, column=4)
    @NativeGetter("unsigned_int")
    public Pointer<_unsigned> unsigned_int$get();
    @NativeSetter("unsigned_int")
    public void unsigned_int$set(Pointer<_unsigned> arg);
    @NativeAddressof("unsigned_int")
    public Pointer<Pointer<_unsigned>> unsigned_int$ptr();

    @NativeLocation(file = "simple.h", line = 54, column = 6)
    @NativeFunction("(${anonymous}u64:u8)v")
    public void func(anonymous s, Pointer<Byte> str);
}
