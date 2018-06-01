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
import java.nicl.Libraries;
import java.nicl.Library;
import java.nicl.NativeTypes;
import java.nicl.Scope;
import java.nicl.metadata.NativeHeader;
import java.nicl.metadata.NativeLocation;
import java.nicl.metadata.NativeStruct;
import java.nicl.types.Array;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.nicl.types.Struct;
import java.util.Objects;

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

    @NativeHeader(declarations =
            "get_strings=(u64:u64:u64:u8u64:i32)v" +
            "get_strings2=(u64:i32)u64:u64:u8" +
            "get_structs=(u64:u64:u64:$(mystruct)u64:i32)v" +
            "get_structs2=(u64:i32)u64:u64:$(mystruct)"
    )
    static interface pointers {
        @NativeLocation(file="dummy", line=47, column=11, USR="c:@F@get_strings")
        void get_strings(Pointer<Pointer<Pointer<Byte>>> p, Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11, USR="c:@F@get_strings2")
        Pointer<Pointer<Byte>> get_strings2(Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11, USR="c:@F@get_structs")
        void get_structs(Pointer<Pointer<Pointer<MyStruct>>> p, Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11, USR="c:@F@get_structs2")
        Pointer<Pointer<MyStruct>> get_structs2(Pointer<Integer> pcount);

        @NativeLocation(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        @NativeStruct("[" +
                      "  [3i32](get=ia$get)(set=ia$set)" +
                      "  u32(pad)" +
                      "  u64(get=str$get)(set=str$set):u8" +
                      "](mystruct)")
        static interface MyStruct extends Struct<MyStruct> {
            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@ia")
            Array<Integer> ia$get();
            void ia$set(Array<Integer> i);

            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@str")
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
            int[] ia = s.ia$get().toArray(int[]::new);

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
                npe.printStackTrace(System.out);
            }

            values = ptrs.get_strings2(pi);
            Objects.requireNonNull(values);
            if (! values.isNull()) {
                throw new IllegalStateException("Expect to get back Pointer.nullPoitner()");
            }
            assertEquals(values, Pointer.nullPointer());
        }

    }

    public void test() {
        testStrings();
        testStrings2();
        testStructs();
        testStructs2();
        testNullPointer();
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
