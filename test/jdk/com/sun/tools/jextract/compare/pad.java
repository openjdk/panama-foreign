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

package com.acme;
 
import java.foreign.annotations.NativeAddressof;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeLocation;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Struct;

@NativeHeader(path="pad.h")
public interface pad {
    @NativeStruct(
            "[" +
                "u8(c1)" +
                "x56" +
                "[" +
                    "i64(l)|" +
                    "[" +
                        "u8(x1)" +
                        "x56" +
                        "f64(y1)" +
                    "](anon$pad_h$1123)|" +
                    "${anon$pad_h$1195}(p2)" +
                "](anon$pad_h$1086)" +
                "f32(f1)" +
                "x32" +
            "](PaddyStruct)")
    interface PaddyStruct extends Struct<PaddyStruct> {
        @NativeGetter("c1")
        public abstract byte c1$get();
        @NativeSetter("c1")
        public abstract void c1$set(byte c1);
        @NativeAddressof("c1")
        public abstract java.foreign.memory.Pointer<java.lang.Byte> c1$ptr();
        @NativeGetter("l")
        public abstract long l$get();
        @NativeSetter("l")
        public abstract void l$set(long l);
        @NativeAddressof("l")
        public abstract java.foreign.memory.Pointer<java.lang.Long> l$ptr();
        @NativeGetter("x1")
        public abstract byte x1$get();
        @NativeSetter("x1")
        public abstract void x1$set(byte x1);
        @NativeAddressof("x1")
        public abstract java.foreign.memory.Pointer<java.lang.Byte> x1$ptr();
        @NativeGetter("y1")
        public abstract double y1$get();
        @NativeSetter("y1")
        public abstract void y1$set(double y1);
        @NativeAddressof("y1")
        public abstract java.foreign.memory.Pointer<java.lang.Double> y1$ptr();
        @NativeGetter("p2")
        public abstract anon$pad_h$1195 p2$get();
        @NativeSetter("p2")
        public abstract void p2$set(anon$pad_h$1195 p2);
        @NativeAddressof("p2")
        public abstract java.foreign.memory.Pointer<anon$pad_h$1195> p2$ptr();
        @NativeGetter("f1")
        public abstract float f1$get();
        @NativeSetter("f1")
        public abstract void f1$set(float f1);
        @NativeAddressof("f1")
        public abstract java.foreign.memory.Pointer<java.lang.Float> f1$ptr();
    }

    @NativeStruct(
            "[" +
                "f64(x2)" +
                "f32(y2)" +
                "u8(z2)" +
                "x24" +
            "](anon$pad_h$1195)")
    interface anon$pad_h$1195 extends java.foreign.memory.Struct<anon$pad_h$1195> {
        @NativeGetter("x2")
        public abstract double x2$get();
        @NativeSetter("x2")
        public abstract void x2$set(double x2);
        @NativeAddressof("x2")
        public abstract java.foreign.memory.Pointer<java.lang.Double> x2$ptr();
        @NativeGetter("y2")
        public abstract float y2$get();
        @NativeSetter("y2")
        public abstract void y2$set(float y2);
        @NativeAddressof("y2")
        public abstract java.foreign.memory.Pointer<java.lang.Float> y2$ptr();
        @NativeGetter("z2")
        public abstract byte z2$get();
        @NativeSetter("z2")
        public abstract void z2$set(byte z2);
        @NativeAddressof("z2")
        public abstract java.foreign.memory.Pointer<java.lang.Byte> z2$ptr();
    }
}
