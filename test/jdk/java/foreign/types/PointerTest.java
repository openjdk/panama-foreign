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
 * @modules java.base/jdk.internal.misc
 * 
 * @run testng PointerTest
 */

import java.foreign.annotations.*;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.Array;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.util.Objects;

import org.testng.annotations.*;

import static org.testng.Assert.*;

@Test
public class PointerTest {
    private static final boolean DEBUG = Boolean.getBoolean("PointerTest.DEBUG");

    private static final Library lib;
    private static final pointers ptrs;

    private static final String[] VERIFICATION_STRINGS = {
        "String A",
        "String B",
        "String C"
    };

    static {
        lib = Libraries.loadLibrary(MethodHandles.lookup(), "Pointers");
        ptrs = Libraries.bind(pointers.class, lib);
    }

    @NativeHeader
    static interface pointers {
        @NativeLocation(file="dummy", line=47, column=11)
        @NativeFunction("(u64:u64:u64:u8u64:i32)v")
        void get_strings(Pointer<Pointer<Pointer<Byte>>> p, Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11)
        @NativeFunction("(u64:i32)u64:u64:u8")
        Pointer<Pointer<Byte>> get_strings2(Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=71, column=0)
        @NativeFunction("(u64:i32)u64:v")
        Pointer<Void> get_stringsAsVoidPtr(Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=75, column=0)
        @NativeFunction("(u64:i32)u64:i8")
        Pointer<Void> get_stringsAsOpaquePtr(Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11)
        @NativeFunction("(u64:u64:u64:${mystruct}u64:i32)v")
        void get_structs(Pointer<Pointer<Pointer<MyStruct>>> p, Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11)
        @NativeFunction("(u64:i32)u64:u64:${mystruct}")
        Pointer<Pointer<MyStruct>> get_structs2(Pointer<Integer> pcount);

        @NativeFunction("()u64:u8")
        Pointer<?> get_negative();

        @NativeFunction("()u64:i32")
        Pointer<Integer> get_overflow_pointer();

        @NativeFunction("()u64:i8")
        Pointer<Byte> get_1_byte_pointer();

        Void notExist(Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11)
        @NativeStruct("[" +
                      "  [3i32](ia)" +
                      "  u32(pad)" +
                      "  u64(str):u8" +
                      "](mystruct)")
        static interface MyStruct extends Struct<MyStruct> {
            @NativeLocation(file="dummy", line=47, column=11)
            @NativeGetter("ia")
            Array<Integer> ia$get();
            @NativeSetter("ia")
            void ia$set(Array<Integer> i);

            @NativeLocation(file="dummy", line=47, column=11)
            @NativeGetter("str")
            Pointer<Byte> str$get();
            @NativeSetter("str")
            void str$set(Pointer<Byte> str);
        }
    }

    private static void debug(String str) {
        if (DEBUG) {
            System.err.println(str);
        }
    }

    private void verifyStrings(Pointer<Pointer<Byte>> values, Pointer<Integer> pi) {
        debug("values: " + values);
        debug("nvalues: " + pi.get());

        assertEquals(VERIFICATION_STRINGS.length, (int) pi.get());

        for (int i = 0; i < pi.get(); i++) {
            Pointer<Byte> cstr = values.offset(i).get();
            String str = Pointer.toString(cstr);

            debug("str[" + i + "] = " + str);

            assertEquals(VERIFICATION_STRINGS[i], str);
        }
    }

    @Test
    void testStrings() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = NativeTypes.INT32;
            LayoutType<Pointer<Pointer<Byte>>> ppcType = NativeTypes.UINT8.pointer().pointer();

            Pointer<Pointer<Pointer<Byte>>> pppc = scope.allocate(ppcType);
            Pointer<Integer> pi = scope.allocate(iType);

            ptrs.get_strings(pppc, pi);

            Pointer<Pointer<Byte>> values = pppc.get();

            verifyStrings(values, pi);
        }
    }

    @Test
    void testStrings2() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = NativeTypes.UINT32;

            Pointer<Integer> pi = scope.allocate(iType);

            Pointer<Pointer<Byte>> values = ptrs.get_strings2(pi);

            verifyStrings(values, pi);
        }
    }

    @Test
    void testStringsAsVoidPtr() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = NativeTypes.UINT32;
            Pointer<Integer> pi = scope.allocate(iType);
            Pointer<Void> values = ptrs.get_stringsAsVoidPtr(pi);
            verifyStrings(values.cast(NativeTypes.INT8.pointer()), pi);
        }
    }

    @Test
    void testStringsAsOpaquePtr() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = NativeTypes.UINT32;
            Pointer<Integer> pi = scope.allocate(iType);
            Pointer<Void> values = ptrs.get_stringsAsOpaquePtr(pi);
            verifyStrings(values.cast(NativeTypes.INT8.pointer()), pi);
        }
    }
    private void verifyStructs(Pointer<Pointer<pointers.MyStruct>> structs, Pointer<Integer> pi) {
        debug("structs: " + structs);
        debug("nstructs: " + pi.get());

        assertEquals(VERIFICATION_STRINGS.length, (int) pi.get());

        int counter = 1;

        for (int i = 0; i < pi.get(); i++) {
            pointers.MyStruct s = structs.offset(i).get().get();
            String str = Pointer.toString(s.str$get());
            debug("str[" + i + "] = " + str);

            assertEquals(VERIFICATION_STRINGS[i], str);
            int[] ia = s.ia$get().toArray(int[]::new);

            assertEquals(3, ia.length);

            for (int j = 0; j < ia.length; j++) {
                assertEquals(counter++, ia[j]);
            }
        }
    }

    @Test
    void testStructs() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = NativeTypes.INT32;
            LayoutType<Pointer<Pointer<pointers.MyStruct>>> ppsType = LayoutType.ofStruct(pointers.MyStruct.class).pointer().pointer();

            Pointer<Pointer<Pointer<pointers.MyStruct>>> ppps = scope.allocate(ppsType);
            Pointer<Integer> pi = scope.allocate(iType);

            ptrs.get_structs(ppps, pi);

            Pointer<Pointer<pointers.MyStruct>> pps = ppps.get();

            verifyStructs(pps, pi);
        }
    }

    @Test
    void testStructs2() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = NativeTypes.INT32;

            Pointer<Integer> pi = scope.allocate(iType);

            Pointer<Pointer<pointers.MyStruct>> pps = ptrs.get_structs2(pi);

            verifyStructs(pps, pi);
        }
    }

    @Test
    void testNullPointer() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = NativeTypes.UINT32;

            Pointer<Integer> pi = Pointer.nullPointer();
            Pointer<Pointer<Byte>> values;

            try {
                values = ptrs.get_strings2(null);
                throw new IllegalStateException("null should not be allowed to pass as Pointer object and should not cause crash");
            } catch (NullPointerException npe) {
                // expected
            }

            values = ptrs.get_strings2(pi);
            Objects.requireNonNull(values);
            if (! values.isNull()) {
                throw new IllegalStateException("Expect to get back Pointer.nullPoitner()");
            }
            assertEquals(values, Pointer.nullPointer());
        }
    }

    @Test(expectedExceptions = AbstractMethodError.class)
    void testNotExistWontCrash() {
        ptrs.notExist(Pointer.nullPointer());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testMemoryRegionRange() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        Pointer<Byte> ptr = Pointer.fromByteBuffer(bb);
        Pointer<Long> ptr2 = ptr.cast(NativeTypes.VOID).cast(NativeTypes.UINT64);
        long l = ptr2.get();
    }

    @Test
    public void testNegative() throws IllegalAccessException {
        Pointer<?> ptr = ptrs.get_negative();
        assertEquals(-1L, ptr.addr());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testAutomaticLengthRegionNotBigEnough() {
        Pointer<Integer> ptr = ptrs.get_overflow_pointer();
        int i = ptr.get();
    }

    @Test
    public void test1ByteRegionAtMaxHeapOffset() throws IllegalAccessException {
        Pointer<?> ptr = ptrs.get_1_byte_pointer(); // should not throw
        assertEquals(-1L, ptr.addr());
    }

}
