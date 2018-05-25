/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
import java.nicl.types.*;
import java.nicl.metadata.*;

public class GlobalVariable {
    @NativeHeader(declarations =
            "init=()v" +
            "global_boolean=u8(get=global_boolean$get)(set=global_boolean$set)(ptr=global_boolean$ptr)" +
            "global_i8=i8(get=global_i8$get)(set=global_i8$set)(ptr=global_i8$ptr)" +
            "global_i16=i16(get=global_i16$get)(set=global_i16$set)(ptr=global_i16$ptr)" +
            "global_i32=i32(get=global_i32$get)(set=global_i32$set)(ptr=global_i32$ptr)" +
            "global_i64=i64(get=global_i64$get)(set=global_i64$set)(ptr=global_i64$ptr)" +
            "global_f32=f32(get=global_f32$get)(set=global_f32$set)(ptr=global_f32$ptr)" +
            "global_d64=f64(get=global_d64$get)(set=global_d64$set)(ptr=global_d64$ptr)" +
            "global_struct=$(mystruct)(get=global_struct$get)(set=global_struct$set)(ptr=global_struct$ptr)"
    )
    static interface globvar {
        @NativeLocation(file="dummy", line=1, column=1, USR="c:@F@init")
        public abstract void init();

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@global_boolean")
        public abstract boolean global_boolean$get();
        public abstract void global_boolean$set(boolean arg);
        public abstract Pointer<Boolean> global_boolean$ptr();

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@global_i8")
        public abstract byte global_i8$get();
        public abstract void global_i8$set(byte arg);
        public abstract Pointer<Byte> global_i8$ptr();

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@global_i16")
        public abstract short global_i16$get();
        public abstract void global_i16$set(short arg);
        public abstract Pointer<Short> global_i16$ptr();

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@global_i32")
        public abstract int global_i32$get();
        public abstract void global_i32$set(int arg);
        public abstract Pointer<Integer> global_i32$ptr();

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@global_i64")
        public abstract long global_i64$get();
        public abstract void global_i64$set(long arg);
        public abstract Pointer<Long> global_i64$ptr();

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@global_f32")
        public abstract float global_f32$get();
        public abstract void global_f32$set(float arg);
        public abstract Pointer<Float> global_f32$ptr();

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@global_d64")
        public abstract double global_d64$get();
        public abstract void global_d64$set(double arg);
        public abstract Pointer<Double> global_d64$ptr();

        @NativeLocation(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        @NativeStruct("[i32(get=i$get)(set=i$set)](mystruct)")
        static interface MyStruct extends Struct<MyStruct> {
            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@i")
            int i$get();
            void i$set(int i);
        }

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@global_struct")
        public abstract MyStruct global_struct$get();
        public abstract void global_struct$set(MyStruct arg);
        public abstract Pointer<MyStruct> global_struct$ptr();
    }

    private final globvar i;
    {
        i = Libraries.bind(globvar.class, Libraries.loadLibrary(MethodHandles.lookup(), "GlobalVariable"));
        i.init();
    }

    public void testboolean() {
        // boolean
        assertTrue(i.global_boolean$get());
        assertTrue(i.global_boolean$ptr().get());

        i.global_boolean$set(false);

        assertFalse(i.global_boolean$get());
        assertFalse(i.global_boolean$ptr().get());
    }


    public void testi8() {
        // int8_t
        assertEquals(42, i.global_i8$get());
        assertEquals(42, i.global_i8$ptr().get());

        i.global_i8$set((byte)47);

        assertEquals(47, i.global_i8$get());
        assertEquals(47, i.global_i8$ptr().get());
    }

    public void testi16() {
        // int16_t
        assertEquals(42, i.global_i16$get());
        assertEquals(42, i.global_i16$ptr().get());

        i.global_i16$set((short)47);

        assertEquals(47, i.global_i16$get());
        assertEquals(47, i.global_i16$ptr().get());
    }

    public void testi32() {
        // int32_t
        assertEquals(42, i.global_i32$get());
        assertEquals(42, i.global_i32$ptr().get());

        i.global_i32$set(47);

        assertEquals(47, i.global_i32$get());
        assertEquals(47, i.global_i32$ptr().get());
    }

    public void testi64() {
        // int64_t
        assertEquals(42, i.global_i64$get());
        assertEquals(42, i.global_i64$ptr().get());

        i.global_i64$set(47);

        assertEquals(47, i.global_i64$get());
        assertEquals(47, i.global_i64$ptr().get());
    }

    public void testf32() {
        // float
        assertEquals(42f, i.global_f32$get());
        assertEquals(42f, i.global_f32$ptr().get());

        i.global_f32$set(47f);

        assertEquals(47f, i.global_f32$get());
        assertEquals(47f, i.global_f32$ptr().get());
    }

    public void testd64() {
        // double
        assertEquals(42.0, i.global_d64$get());
        assertEquals(42.0, i.global_d64$ptr().get());

        i.global_d64$set(47.0);

        assertEquals(47.0, i.global_d64$get());
        assertEquals(47.0, i.global_d64$ptr().get());
    }

    public void teststruct() {
        assertEquals(42, i.global_struct$get().i$get());
        assertEquals(42, i.global_struct$ptr().get().i$get());

        try (Scope scope = Scope.newNativeScope()) {
            globvar.MyStruct s = scope.allocateStruct(globvar.MyStruct.class);

            s.i$set(47);

            i.global_struct$set(s);
        }

        assertEquals(47, i.global_struct$get().i$get());
        assertEquals(47, i.global_struct$ptr().get().i$get());
    }


    static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new RuntimeException("expected: " + expected + " does not match actual: " + actual);
        }
    }

    static void assertEquals(float expected, float actual) {
        if (expected != actual) {
            throw new RuntimeException("expected: " + expected + " does not match actual: " + actual);
        }
    }

    static void assertEquals(double expected, double actual) {
        if (expected != actual) {
            throw new RuntimeException("expected: " + expected + " does not match actual: " + actual);
        }
    }

    static void assertTrue(boolean actual) {
        if (!actual) {
            throw new RuntimeException("expected: true does not match actual: " + actual);
        }
    }

    static void assertFalse(boolean actual) {
        if (actual) {
            throw new RuntimeException("expected: false does not match actual: " + actual);
        }
    }

    public static void main(String[] args) {
        GlobalVariable t = new GlobalVariable();

        t.testboolean();
        t.testi8();
        t.testi16();
        t.testi32();
        t.testi64();
        t.testf32();
        t.testd64();
        t.teststruct();
    }
}
