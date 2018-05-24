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
 */

import java.lang.invoke.MethodHandles;
import java.nicl.*;
import java.nicl.metadata.*;
import java.nicl.metadata.Array;
import java.nicl.types.*;

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
        @NativeLocation(file="dummy", line=47, column=11, USR="c:@F@get_strings")
        @NativeType(layout="(u64:u64:u64:u8u64:i32)v", ctype="void (const char***, int*)")
        void get_strings(Pointer<Pointer<Pointer<Byte>>> p, Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11, USR="c:@F@get_strings2")
        @NativeType(layout="(u64:i32)u64:u64:u8", ctype="const char **(int *)")
        Pointer<Pointer<Byte>> get_strings2(Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11, USR="c:@F@get_structs")
        @NativeType(layout="(u64:u64:u64:[[3i32]u64:u8]u64:i32)v", ctype="void (const struct MyStruct ***, int *)" )
        void get_structs(Pointer<Pointer<Pointer<MyStruct>>> p, Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11, USR="c:@F@get_structs2")
        @NativeType(layout="(u64:i32)u64:u64:[[3i32]u64:u8]", ctype="const struct MyStruct **(int *)")
        Pointer<Pointer<MyStruct>> get_structs2(Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        @NativeStruct("[[3i32]u64:u8]")
        static interface MyStruct extends Struct<MyStruct> {
            @Offset(offset=0l)
            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@ia")
            @Array(elementType="int", elementSize=4l, length=3l)
            @NativeType(layout="[3i32]", ctype="int []")
            int[] ia$get();
            void ia$set(int[] i);

            @Offset(offset=128l)
            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@str")
            @NativeType(layout="u64:u8", ctype="const char*")
            Pointer<Byte> str$get();
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

        assertEquals(VERIFICATION_STRINGS.length, pi.get());

        for (int i = 0; i < pi.get(); i++) {
            Pointer<Byte> cstr = values.offset(i).get();
            String str = Pointer.toString(cstr);

            debug("str[" + i + "] = " + str);

            assertEquals(VERIFICATION_STRINGS[i], str);
        }
    }


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

    void testStrings2() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = NativeTypes.UINT32;

            Pointer<Integer> pi = scope.allocate(iType);

            Pointer<Pointer<Byte>> values = ptrs.get_strings2(pi);

            verifyStrings(values, pi);
        }
    }

    private void verifyStructs(Pointer<Pointer<pointers.MyStruct>> structs, Pointer<Integer> pi) {
        debug("structs: " + structs);
        debug("nstructs: " + pi.get());

        assertEquals(VERIFICATION_STRINGS.length, pi.get());

        int counter = 1;

        for (int i = 0; i < pi.get(); i++) {
            pointers.MyStruct s = structs.offset(i).get().get();
            String str = Pointer.toString(s.str$get());
            debug("str[" + i + "] = " + str);

            assertEquals(VERIFICATION_STRINGS[i], str);
            int[] ia = s.ia$get();

            assertEquals(3, ia.length);

            for (int j = 0; j < ia.length; j++) {
                assertEquals(counter++, ia[j]);
            }
        }
    }

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

    void testStructs2() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = NativeTypes.INT32;

            Pointer<Integer> pi = scope.allocate(iType);

            Pointer<Pointer<pointers.MyStruct>> pps = ptrs.get_structs2(pi);

            verifyStructs(pps, pi);
        }
    }

    public void test() {
        testStrings();
        testStrings2();
        testStructs();
        testStructs2();
    }

    static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new RuntimeException("expected: " + expected + " does not match actual: " + actual);
        }
    }

    public static void main(String[] args) {
        PointerTest pt = new PointerTest();
        pt.test();
    }
}
