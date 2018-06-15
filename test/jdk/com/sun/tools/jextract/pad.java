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

import java.nicl.metadata.NativeHeader;
import java.nicl.metadata.NativeStruct;
import java.nicl.types.Struct;

@NativeHeader(path="pad.h")
public interface pad {
    @NativeStruct(
            "[" +
                "u8(get=c1$get)(set=c1$set)(ptr=c1$ptr)" +
                "x56" +
                "[" +
                    "i64(get=l$get)(set=l$set)(ptr=l$ptr)|" +
                    "[" +
                        "u8(get=x1$get)(set=x1$set)(ptr=x1$ptr)" +
                        "x56" +
                        "f64(get=y1$get)(set=y1$set)(ptr=y1$ptr)" +
                    "](anon$pad_h$1118)|" +
                    "$(anon$pad_h$1190)(get=p2$get)(set=p2$set)(ptr=p2$ptr)" +
                "](anon$pad_h$1086)" +
                "f32(get=f1$get)(set=f1$set)(ptr=f1$ptr)" +
                "x32" +
            "](PaddyStruct)")
    interface PaddyStruct extends Struct<PaddyStruct> {
        public abstract byte c1$get();
        public abstract void c1$set(byte c1);
        public abstract java.nicl.types.Pointer<java.lang.Byte> c1$ptr();
        public abstract long l$get();
        public abstract void l$set(long l);
        public abstract java.nicl.types.Pointer<java.lang.Long> l$ptr();
        public abstract byte x1$get();
        public abstract void x1$set(byte x1);
        public abstract java.nicl.types.Pointer<java.lang.Byte> x1$ptr();
        public abstract double y1$get();
        public abstract void y1$set(double y1);
        public abstract java.nicl.types.Pointer<java.lang.Double> y1$ptr();
        public abstract anon$pad_h$1190 p2$get();
        public abstract void p2$set(anon$pad_h$1190 p2);
        public abstract java.nicl.types.Pointer<anon$pad_h$1190> p2$ptr();
        public abstract float f1$get();
        public abstract void f1$set(float f1);
        public abstract java.nicl.types.Pointer<java.lang.Float> f1$ptr();
    }

    @NativeStruct(
            "[" +
                "f64(get=x2$get)(set=x2$set)(ptr=x2$ptr)" +
                "f32(get=y2$get)(set=y2$set)(ptr=y2$ptr)" +
                "u8(get=z2$get)(set=z2$set)(ptr=z2$ptr)" +
                "x24" +
            "](anon$pad_h$1190)")
    interface anon$pad_h$1190 extends java.nicl.types.Struct<anon$pad_h$1190> {
        public abstract double x2$get();
        public abstract void x2$set(double x2);
        public abstract java.nicl.types.Pointer<java.lang.Double> x2$ptr();
        public abstract float y2$get();
        public abstract void y2$set(float y2);
        public abstract java.nicl.types.Pointer<java.lang.Float> y2$ptr();
        public abstract byte z2$get();
        public abstract void z2$set(byte z2);
        public abstract java.nicl.types.Pointer<java.lang.Byte> z2$ptr();
    }
}
