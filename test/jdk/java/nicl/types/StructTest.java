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

import java.nicl.*;
import java.nicl.metadata.*;
import java.nicl.metadata.Array;
import java.nicl.types.*;

public class StructTest {
    public static final boolean DEBUG = Boolean.getBoolean("StructTest.DEBUG");

    public static final long TOTAL_SIZE = 16;
    public static final long A_OFFSET  = 0;
    public static final long A_LENGTH  = 4;
    public static final long M_OFFSET  = 256;
    public static final long MA_OFFSET = 512;
    public static final int MA_LENGTH = 2;

    public static long alignUp(long n, long alignment) {
        return (n + alignment - 1) & ~(alignment - 1);
    }

    @NativeLocation(file="dummy", line=47, column=11, USR="c:@S@MyStruct")
    @NativeStruct("[[4i32]]")
    @NativeType(ctype="struct MyStruct")
    static interface MyStruct extends Struct<MyStruct> {
        @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@MyStruct@field1")
        @NativeType(layout="[4i32]", ctype="off_t")
        @Array(elementType="int", elementSize=4l, length=4l)
        @Offset(offset=0l)
        int[] a$get();
        void a$set(int[] a);
    }

    public int buildInt(long baseValue) {
        int tmp = 0;

        for (int i = 0; i < 4; i++) {
            tmp |= baseValue++ << (i * 8);
        }

        return tmp;
    }


    public long buildLong(long baseValue) {
        long tmp = 0;

        for (int i = 0; i < 8; i++) {
            tmp |= baseValue++ << (i * 8);
        }

        return tmp;
    }

    public void testIntArray(MyStruct s, Pointer<Byte> p) {
        {
            long expected = A_OFFSET / 8;

            int[] ia = s.a$get();
            assertEquals(A_LENGTH, ia.length);

            for (int i = 0; i < ia.length; i++, expected += 4) {
                if (DEBUG) {
                    System.err.println("ia[" + i + "] = 0x" + Integer.toHexString(ia[i]));
                }
                try {
                    assertEquals(buildInt(expected), ia[i]);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to verify ia[" + i + "]", e);
                }
            }
        }

        {
            int counter = 0x80;

            int[] ia = new int[4];
            for (int i = 0; i < ia.length; i++, counter += 4) {
                ia[i] = buildInt(counter);
            }
            s.a$set(ia);
        }

        {
            int expected = 0x80;

            int[] ia = s.a$get();
            assertEquals(A_LENGTH, ia.length);

            for (int i = 0; i < ia.length; i++, expected += 4) {
                int val = ia[i];
                if (DEBUG) {
                    System.err.println("ia[" + i + "] = 0x" + Integer.toHexString(val));
                }
                try {
                    assertEquals(buildInt(expected), val);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to verify ia[" + i + "]", e);
                }
            }
        }
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new RuntimeException("actual: 0x" + Long.toHexString(actual) + " does not match expected: 0x" + Long.toHexString(expected));
        }
    }

    private static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new RuntimeException("actual: 0x" + Long.toHexString(actual) + " does not match expected: 0x" + Long.toHexString(expected));
        }
    }

    public void test() {
        try (Scope scope = Scope.newNativeScope()) {
            MyStruct s = scope.allocateStruct(MyStruct.class);
            long size = TOTAL_SIZE;
            Pointer<Byte> p = scope.allocate(NativeTypes.UINT8, size);

            for (int i = 0; i < size; i++) {
                p.offset(i).set((byte)i);
            }
            Pointer.copy(p, s.ptr(), size);

            testIntArray(s, p);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException("Unexpected exception " + iae);
        }
    }

    public static void main(String[] args) {
        StructTest t = new StructTest();
        t.test();
    }
}
