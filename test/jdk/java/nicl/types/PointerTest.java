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
        @C(file="dummy", line=47, column=11, USR="c:@F@get_strings")
        @CallingConvention(value=1)
        @NativeType(layout="(p:p:p:cp:i)V", ctype="void (const char***, int*)", size=1l)
        void get_strings(Pointer<Pointer<Pointer<Byte>>> p, Pointer<Integer> pcount);

        @C(file="dummy", line=47, column=11, USR="c:@F@get_strings2")
        @CallingConvention(value=1)
        @NativeType(layout="(p:i)p:p:c", ctype="const char **(int *)", size=1l)
        Pointer<Pointer<Byte>> get_strings2(Pointer<Integer> pcount);

        @C(file="dummy", line=47, column=11, USR="c:@F@get_structs")
        @CallingConvention(value=1)
        @NativeType(layout="(p:p:p:[3ip:c]p:i)V", ctype="void (const struct MyStruct ***, int *)", size=1l)
        void get_structs(Pointer<Pointer<Pointer<MyStruct>>> p, Pointer<Integer> pcount);

        @C(file="dummy", line=47, column=11, USR="c:@F@get_structs2")
        @CallingConvention(value=1)
        @NativeType(layout="(p:i)p", ctype="const struct MyStruct **(int *)", size=1l)
        Pointer<Pointer<MyStruct>> get_structs2(Pointer<Integer> pcount);

        @NativeType(layout="[3ip:c]", ctype="dummy", size=24, isRecordType=true)
        @C(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        static interface MyStruct extends Reference<MyStruct> {
            @Offset(offset=0l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@ia")
            @Array(elementType="int", elementSize=4l, length=3l)
            @NativeType(layout="iii", ctype="int []", size=12l)
            int[] ia$get();
            void ia$set(int[] i);

            @Offset(offset=128l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@str")
            @NativeType(layout="p:c", ctype="const char*", size=4l)
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
        debug("nvalues: " + pi.deref());

        assertEquals(VERIFICATION_STRINGS.length, pi.deref());

        for (int i = 0; i < pi.deref(); i++) {
            Pointer<Byte> cstr = values.offset(i).deref();
            String str = Pointer.toString(cstr);

            debug("str[" + i + "] = " + str);

            assertEquals(VERIFICATION_STRINGS[i], str);
        }
    }


    void testStrings() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = LayoutType.create(int.class);
            LayoutType<Pointer<Pointer<Byte>>> ppcType = LayoutType.create(byte.class).ptrType().ptrType();

            Pointer<Pointer<Pointer<Byte>>> pppc = scope.allocate(ppcType);
            Pointer<Integer> pi = scope.allocate(iType);

            ptrs.get_strings(pppc, pi);

            Pointer<Pointer<Byte>> values = pppc.deref();

            verifyStrings(values, pi);
        }
    }

    void testStrings2() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = LayoutType.create(int.class);

            Pointer<Integer> pi = scope.allocate(iType);

            Pointer<Pointer<Byte>> values = ptrs.get_strings2(pi);

            verifyStrings(values, pi);
        }
    }

    private void verifyStructs(Pointer<Pointer<pointers.MyStruct>> structs, Pointer<Integer> pi) {
        debug("structs: " + structs);
        debug("nstructs: " + pi.deref());

        assertEquals(VERIFICATION_STRINGS.length, pi.deref());

        int counter = 1;

        for (int i = 0; i < pi.deref(); i++) {
            pointers.MyStruct s = structs.offset(i).deref().deref();
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
            LayoutType<Integer> iType = LayoutType.create(int.class);
            LayoutType<Pointer<Pointer<pointers.MyStruct>>> ppsType = LayoutType.create(pointers.MyStruct.class).ptrType().ptrType();

            Pointer<Pointer<Pointer<pointers.MyStruct>>> ppps = scope.allocate(ppsType);
            Pointer<Integer> pi = scope.allocate(iType);

            ptrs.get_structs(ppps, pi);

            Pointer<Pointer<pointers.MyStruct>> pps = ppps.deref();

            verifyStructs(pps, pi);
        }
    }

    void testStructs2() {
        try (Scope scope = Scope.newNativeScope()) {
            LayoutType<Integer> iType = LayoutType.create(int.class);

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
