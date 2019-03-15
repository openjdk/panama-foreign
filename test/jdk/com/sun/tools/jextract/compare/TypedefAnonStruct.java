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
import java.foreign.memory.Pointer;

@NativeHeader(path="TypedefAnonStruct.h")
public interface TypedefAnonStruct {
    @NativeLocation(
      file="TypedefAnonStruct.h", line=24, column=9
    )
    @NativeStruct("[" +
            "i32(i)" +
            "i32(j)" +
            "](Point)")
    public static interface Point extends Struct<Point> {
        @NativeLocation(
          file="TypedefAnonStruct.h", line=25, column=9
        )
        @NativeGetter("i")
        public int i$get();
        @NativeSetter("i")
        public void i$set(int arg);
        @NativeAddressof("i")
        public Pointer<Integer> i$ptr();
        @NativeLocation(
          file="TypedefAnonStruct.h", line=25, column=12
        )
        @NativeGetter("j")
        public int j$get();
        @NativeSetter("j")
        public void j$set(int arg);
        @NativeAddressof("j")
        public Pointer<Integer> j$ptr();
    }

    @NativeLocation(
      file="TypedefAnonStruct.h", line=28, column=9
    )
    @NativeStruct("[" +
            "f32(x)" +
            "f32(y)" +
            "](FPoint)")
    public static interface FPoint extends Struct<FPoint> {
        @NativeLocation(
          file="TypedefAnonStruct.h", line=29, column=11
        )
        @NativeGetter("x")
        public float x$get();
        @NativeSetter("x")
        public void x$set(float arg);
        @NativeAddressof("x")
        public Pointer<Float> x$ptr();
        @NativeLocation(
          file="TypedefAnonStruct.h", line=29, column=14
        )
        @NativeGetter("y")
        public float y$get();
        @NativeSetter("y")
        public void y$set(float arg);
        @NativeAddressof("y")
        public Pointer<Float> y$ptr();
    }
}
