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
import java.nicl.Scope;
import java.nicl.metadata.C;
import java.nicl.metadata.CallingConvention;
import java.nicl.metadata.Header;
import java.nicl.metadata.NativeType;
import java.nicl.metadata.Offset;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.nicl.types.Reference;

public class StructUpcall {
    private static final boolean DEBUG = false;

    @Header(path="dummy")
    public static interface Index {
        @NativeType(layout="[iiippp]", ctype="dummy", size=40, isRecordType=true)
        @C(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        static interface MyStruct extends Reference<MyStruct> {
            @Offset(offset=0l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field1")
            @NativeType(layout="i", ctype="enum MyStructField1", size=4l)
            int field1$get();
            void field1$set(int i);

            @Offset(offset=32l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field2")
            @NativeType(layout="i", ctype="int", size=4l)
            int field2$get();
            void field2$set(int i);

            @Offset(offset=64l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field3")
            @NativeType(layout="i", ctype="int", size=4l)
            int field3$get();
            void field3$set(int i);

            @Offset(offset=128l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field4")
            @NativeType(layout="p", ctype="const void *", size=8l)
            Pointer<Void> field4$get();
            void field4$set(Pointer<Void> p);

            @Offset(offset=192l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field5")
            @NativeType(layout="p", ctype="const void *", size=8l)
            Pointer<Void> field5$get();
            void field5$set(Pointer<Void> p);

            @Offset(offset=256l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@FI@field6")
            @NativeType(layout="p", ctype="const void *", size=8l)
            Pointer<Void> field6$get();
            void field6$set(Pointer<Void> p);
        }

        @FunctionalInterface
        static interface MyStructVisitor {
            @C(file="dummy", line=47, column=11, USR="c:@F@slowsort")
            @NativeType(layout="([iiippp])V", ctype="void (dummy)", size=4l)
            @CallingConvention(value=1)
            public void fn(MyStruct s);
        }

        @C(file="dummy", line=47, column=11, USR="c:@F@struct_upcall")
        @NativeType(layout="(p:([iiip:Vp:Vp:V])V[iiip:Vp:Vp:V])V", ctype="void (struct_upcall_cb, struct MyStruct)", name="struct_upcall", size=1)
        @CallingConvention(value=1)
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
                System.err.println("\ts.field4 = " + s.field4$get().cast(LayoutType.create(byte.class)).lvalue().get());
                System.err.println("\ts.field5 = " + s.field5$get().cast(LayoutType.create(byte.class)).lvalue().get());
                System.err.println("\ts.field6 = " + s.field6$get().cast(LayoutType.create(byte.class)).lvalue().get());
            }

            assertEquals(47, s.field1$get());
            assertEquals(11, s.field2$get());
            assertEquals(93, s.field3$get());
            assertEquals(123, s.field4$get().cast(LayoutType.create(byte.class)).lvalue().get());
            assertEquals(124, s.field5$get().cast(LayoutType.create(byte.class)).lvalue().get());
            assertEquals(125, s.field6$get().cast(LayoutType.create(byte.class)).lvalue().get());
        }
    }

    public void test() {
        Index i = Libraries.bindRaw(Index.class, Libraries.loadLibrary(MethodHandles.lookup(), "Upcall"));

        try (Scope scope = Scope.newNativeScope()) {
            Reference<Index.MyStruct> s = scope.allocateStruct(LayoutType.create(Index.MyStruct.class));

            Pointer<Byte> p1 = scope.allocate(LayoutType.create(byte.class));
            Pointer<Byte> p2 = scope.allocate(LayoutType.create(byte.class));
            Pointer<Byte> p3 = scope.allocate(LayoutType.create(byte.class));

            p1.lvalue().set((byte)123);
            p2.lvalue().set((byte)124);
            p3.lvalue().set((byte)125);

            s.get().field1$set(47);
            s.get().field2$set(11);
            s.get().field3$set(93);
            s.get().field4$set(p1.cast(LayoutType.create(void.class)));
            s.get().field5$set(p2.cast(LayoutType.create(void.class)));
            s.get().field6$set(p3.cast(LayoutType.create(void.class)));

            assertEquals(47, s.get().field1$get());
            assertEquals(11, s.get().field2$get());
            assertEquals(93, s.get().field3$get());
            assertEquals(123, s.get().field4$get().cast(LayoutType.create(byte.class)).lvalue().get());
            assertEquals(124, s.get().field5$get().cast(LayoutType.create(byte.class)).lvalue().get());
            assertEquals(125, s.get().field6$get().cast(LayoutType.create(byte.class)).lvalue().get());

            Index.MyStructVisitor v = new MyStructVisitorImpl();

            i.struct_upcall(v, s.get());
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
