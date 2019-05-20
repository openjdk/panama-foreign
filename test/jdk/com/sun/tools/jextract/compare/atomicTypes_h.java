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
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeLocation;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NativeHeader(path="atomicTypes.h")
public interface atomicTypes_h {

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface uchar_t {

    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface atomic_int_t {

    }

    @NativeStruct("[u8(auc)x24u32(vui)i32(aai)](SomeTypes)")
    public static interface SomeTypes extends Struct<SomeTypes> {

        @NativeGetter("auc")
        public byte auc$get();

        @NativeSetter("auc")
        public void auc$set(byte value);

        @NativeAddressof("auc")
        public Pointer<Byte> auc$ptr();

        @NativeGetter("vui")
        public int vui$get();

        @NativeSetter("vui")
        public void vui$set(int value);

        @NativeAddressof("vui")
        public Pointer<Integer> vui$ptr();

        @NativeGetter("aai")
        public int aai$get();

        @NativeSetter("aai")
        public void aai$set(int value);

        @NativeAddressof("aai")
        public Pointer<Integer> aai$ptr();

    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface atomic_some_types_t {

    }
}


