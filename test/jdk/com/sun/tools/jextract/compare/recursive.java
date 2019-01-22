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
import java.foreign.annotations.NativeLocation;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;

/**
 * This test is platform dependent, as the C type size may vary on platform.
 * Current value is based on x64 with __LP64__.
 */
@NativeHeader(path="recursive.h")
public interface recursive {

    @NativeLocation(file="recursive.h", line=26, column=8)
    @NativeStruct("[u64(p):${Bar}](Foo)")
    public interface Foo extends Struct<Foo> {
        @NativeLocation(file="recursive.h", line=27, column=17)
        @NativeGetter("p")
        Pointer<Bar> p$get();
        @NativeSetter("p")
        void p$set(Pointer<Bar> value);
        @NativeAddressof("p")
        Pointer<Pointer<Bar>> p$ptr();
    }

    @NativeLocation(file = "recursive.h", line=30, column=8)
    @NativeStruct("[u64(q):${Foo}](Bar)")
    public interface Bar extends Struct<Bar> {
        @NativeLocation(file="recursive.h", line=31, column=17)
        @NativeGetter("q")
        Pointer<Foo> q$get();
        @NativeSetter("q")
        void q$set(Pointer<Foo> value);
        @NativeAddressof("q")
        Pointer<Pointer<Foo>> q$ptr();
    }
}
