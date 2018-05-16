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
    @NativeHeader
    static interface globvar {
        @C(file="dummy", line=1, column=1, USR="c:@F@init")
        @NativeType(layout="()V", ctype="dummy", size=1)
        @CallingConvention(value=1)
        public abstract void init();

        @C(file="dummy", line=1, column=1, USR="c:@global_boolean")
        @NativeType(layout="B", ctype="dummy", size=1, name="global_boolean")
        public abstract boolean global_boolean$get();
        public abstract void global_boolean$set(boolean arg);
        public abstract Reference<Boolean> global_boolean$ref();

        @C(file="dummy", line=1, column=1, USR="c:@global_i8")
        @NativeType(layout="c", ctype="dummy", size=1, name="global_i8")
        public abstract byte global_i8$get();
        public abstract void global_i8$set(byte arg);
        public abstract Reference<Byte> global_i8$ref();

        @C(file="dummy", line=1, column=1, USR="c:@global_i16")
        @NativeType(layout="s", ctype="dummy", size=2, name="global_i16")
        public abstract short global_i16$get();
        public abstract void global_i16$set(short arg);
        public abstract Reference<Short> global_i16$ref();

        @C(file="dummy", line=1, column=1, USR="c:@global_i32")
        @NativeType(layout="i", ctype="dummy", size=4, name="global_i32")
        public abstract int global_i32$get();
        public abstract void global_i32$set(int arg);
        public abstract Reference<Integer> global_i32$ref();

        @C(file="dummy", line=1, column=1, USR="c:@global_i64")
        @NativeType(layout="l", ctype="dummy", size=8, name="global_i64")
        public abstract long global_i64$get();
        public abstract void global_i64$set(long arg);
        public abstract Reference<Long> global_i64$ref();

        @C(file="dummy", line=1, column=1, USR="c:@global_f32")
        @NativeType(layout="f", ctype="dummy", size=4, name="global_f32")
        public abstract float global_f32$get();
        public abstract void global_f32$set(float arg);
        public abstract Reference<Float> global_f32$ref();

        @C(file="dummy", line=1, column=1, USR="c:@global_d64")
        @NativeType(layout="d", ctype="dummy", size=8, name="global_d64")
        public abstract double global_d64$get();
        public abstract void global_d64$set(double arg);
        public abstract Reference<Double> global_d64$ref();

        @NativeType(layout="[i]", ctype="dummy", size=4, isRecordType=true)
        @C(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        static interface MyStruct extends Reference<MyStruct> {
            @Offset(offset=0l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@i")
            @NativeType(layout="i", ctype="int", size=4l)
            int i$get();
            void i$set(int i);
        }

        @C(file="dummy", line=1, column=1, USR="c:@global_struct")
        @NativeType(layout="[i]", ctype="dummy", size=4, name="global_struct")
        public abstract MyStruct global_struct$get();
        public abstract void global_struct$set(MyStruct arg);
        public abstract Reference<MyStruct> global_struct$ref();
    }

    private final globvar i;
    {
        i = Libraries.bind(globvar.class, Libraries.loadLibrary(MethodHandles.lookup(), "GlobalVariable"));
        i.init();
    }

    public void testboolean() {
        // boolean
        assertTrue(i.global_boolean$get());
        assertTrue(i.global_boolean$ref().get());

        i.global_boolean$set(false);

        assertFalse(i.global_boolean$get());
        assertFalse(i.global_boolean$ref().get());
    }


    public void testi8() {
        // int8_t
        assertEquals(42, i.global_i8$get());
        assertEquals(42, i.global_i8$ref().get());

        i.global_i8$set((byte)47);

        assertEquals(47, i.global_i8$get());
        assertEquals(47, i.global_i8$ref().get());
    }

    public void testi16() {
        // int16_t
        assertEquals(42, i.global_i16$get());
        assertEquals(42, i.global_i16$ref().get());

        i.global_i16$set((short)47);

        assertEquals(47, i.global_i16$get());
        assertEquals(47, i.global_i16$ref().get());
    }

    public void testi32() {
        // int32_t
        assertEquals(42, i.global_i32$get());
        assertEquals(42, i.global_i32$ref().get());

        i.global_i32$set(47);

        assertEquals(47, i.global_i32$get());
        assertEquals(47, i.global_i32$ref().get());
    }

    public void testi64() {
        // int64_t
        assertEquals(42, i.global_i64$get());
        assertEquals(42, i.global_i64$ref().get());

        i.global_i64$set(47);

        assertEquals(47, i.global_i64$get());
        assertEquals(47, i.global_i64$ref().get());
    }

    public void testf32() {
        // float
        assertEquals(42f, i.global_f32$get());
        assertEquals(42f, i.global_f32$ref().get());

        i.global_f32$set(47f);

        assertEquals(47f, i.global_f32$get());
        assertEquals(47f, i.global_f32$ref().get());
    }

    public void testd64() {
        // double
        assertEquals(42.0, i.global_d64$get());
        assertEquals(42.0, i.global_d64$ref().get());

        i.global_d64$set(47.0);

        assertEquals(47.0, i.global_d64$get());
        assertEquals(47.0, i.global_d64$ref().get());
    }

    public void teststruct() {
        assertEquals(42, i.global_struct$get().i$get());
        assertEquals(42, i.global_struct$ref().get().i$get());

        try (Scope scope = Scope.newNativeScope()) {
            globvar.MyStruct s = scope.allocateStruct(LayoutType.create(globvar.MyStruct.class));

            s.i$set(47);

            i.global_struct$set(s);
        }

        assertEquals(47, i.global_struct$get().i$get());
        assertEquals(47, i.global_struct$ref().get().i$get());
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
