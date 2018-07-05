/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
 * @run main/othervm StructUpcall
 */

import java.lang.invoke.MethodHandles;
import java.nicl.Libraries;
import java.nicl.NativeTypes;
import java.nicl.Scope;
import java.nicl.metadata.NativeCallback;
import java.nicl.metadata.NativeHeader;
import java.nicl.metadata.NativeLocation;
import java.nicl.metadata.NativeStruct;
import java.nicl.types.Callback;
import java.nicl.types.Pointer;
import java.nicl.types.Struct;

public class StructUpcall {
    private static final boolean DEBUG = false;

    @NativeHeader(declarations = "struct_upcall=(u64:($(mystruct))v$(mystruct))v")
    public static interface Index {
        @NativeLocation(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        @NativeStruct("[" +
                      "  i32(get=field1$get)(set=field1$set)" +
                      "  i32(get=field2$get)(set=field2$set)" +
                      "  i32(get=field3$get)(set=field3$set)" +
                      "  u64(get=field4$get)(set=field4$set):v" +
                      "  u64(get=field5$get)(set=field5$set):v" +
                      "  u64(get=field6$get)(set=field6$set):v" +
                      "](mystruct)")
        static interface MyStruct extends Struct<MyStruct> {
            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field1")
            int field1$get();
            void field1$set(int i);

            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field2")
            int field2$get();
            void field2$set(int i);

            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field3")
            int field3$get();
            void field3$set(int i);

            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field4")
            Pointer<Void> field4$get();
            void field4$set(Pointer<?> p);

            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field5")
            Pointer<Void> field5$get();
            void field5$set(Pointer<?> p);

            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field6")
            Pointer<Void> field6$get();
            void field6$set(Pointer<?> p);
        }

        @NativeCallback("($(mystruct))v")
        @FunctionalInterface
        static interface MyStructVisitor extends Callback<MyStructVisitor> {
            @NativeLocation(file="dummy", line=47, column=11, USR="c:@F@slowsort")
            public void fn(MyStruct s);
        }

        @NativeLocation(file="dummy", line=47, column=11, USR="c:@F@struct_upcall")
        public abstract void struct_upcall(MyStructVisitor v, MyStruct s);
    }


    public static class MyStructVisitorImpl implements Index.MyStructVisitor {
        MyStructVisitorImpl() {
        }

        @Override
        public void fn(Index.MyStruct s) {
            if (DEBUG) {
                System.err.println("visit(" + s + ")");
                System.err.println("\ts.field1  = " + s.field1$get());
                System.err.println("\ts.field2 = " + s.field2$get());
                System.err.println("\ts.field3 = " + s.field3$get());
                System.err.println("\ts.field4 = " + s.field4$get().cast(NativeTypes.INT8).get());
                System.err.println("\ts.field5 = " + s.field5$get().cast(NativeTypes.INT8).get());
                System.err.println("\ts.field6 = " + s.field6$get().cast(NativeTypes.INT8).get());
            }

            assertEquals(47, s.field1$get());
            assertEquals(11, s.field2$get());
            assertEquals(93, s.field3$get());
            assertEquals(123, s.field4$get().cast(NativeTypes.UINT8).get());
            assertEquals(124, s.field5$get().cast(NativeTypes.UINT8).get());
            assertEquals(125, s.field6$get().cast(NativeTypes.UINT8).get());
        }
    }

    public void test() {
        Index i = Libraries.bind(Index.class, Libraries.loadLibrary(MethodHandles.lookup(), "Upcall"));

        try (Scope scope = Scope.newNativeScope()) {
            Index.MyStruct s = scope.allocateStruct(Index.MyStruct.class);

            Pointer<Byte> p1 = scope.allocate(NativeTypes.INT8);
            Pointer<Byte> p2 = scope.allocate(NativeTypes.INT8);
            Pointer<Byte> p3 = scope.allocate(NativeTypes.INT8);

            p1.set((byte)123);
            p2.set((byte)124);
            p3.set((byte)125);

            s.field1$set(47);
            s.field2$set(11);
            s.field3$set(93);
            s.field4$set(p1.cast(NativeTypes.VOID));
            s.field5$set(p2.cast(NativeTypes.VOID));
            s.field6$set(p3.cast(NativeTypes.VOID));

            assertEquals(47, s.field1$get());
            assertEquals(11, s.field2$get());
            assertEquals(93, s.field3$get());
            assertEquals(123, s.field4$get().cast(NativeTypes.INT8).get());
            assertEquals(124, s.field5$get().cast(NativeTypes.INT8).get());
            assertEquals(125, s.field6$get().cast(NativeTypes.INT8).get());

            Index.MyStructVisitor v = new MyStructVisitorImpl();

            i.struct_upcall(v, s);
        }

        if (DEBUG) {
            System.err.println("back in test()\n");
        }
    }

    static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new RuntimeException("expected: " + expected + " does not match actual: " + actual);
        }
    }

    public static void main(String[] args) {
        new StructUpcall().test();
    }
}
