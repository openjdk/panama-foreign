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
 */

import java.nicl.NativeLibrary;
import java.nicl.metadata.C;
import java.nicl.metadata.CallingConvention;
import java.nicl.metadata.Header;
import java.nicl.metadata.NativeType;

public class Long4Upcall {
    private static final boolean DEBUG = false;

    @Header(path="dummy")
    public static interface upcall {
        @FunctionalInterface
        static interface cb {
            @C(file="dummy", line=47, column=11, USR="c:@F@slowsort")
            @NativeType(layout="(=256v=256v)=256v", ctype="__m256i (__m256i,__m256i)", size=4l)
            @CallingConvention(value=1)
            public Long4 fn(Long4 l1, Long4 l2);
        }

        @C(file="dummy", line=47, column=11, USR="c:@F@long4_upcall")
        @NativeType(layout="(p:(=256v=256v)=256v=256v=256v)=256v", ctype="__m256i (long4_upcall_cb, __m256i, __m256i)", name="long4_upcall", size=1)
        @CallingConvention(value=1)
        public abstract Long4 long4_upcall(cb cb, Long4 l1, Long4 l2);
    }

    public static class cbImpl implements upcall.cb {
        @Override
        public Long4 fn(Long4 l1, Long4 l2) {
            if (DEBUG) {
                System.err.println("fn(" + l1 + ", " + l2 + ")");
            }

            assertEquals(1, l1.extract(0));
            assertEquals(2, l1.extract(1));
            assertEquals(3, l1.extract(2));
            assertEquals(4, l1.extract(3));

            assertEquals(1, l2.extract(0));
            assertEquals(1, l2.extract(1));
            assertEquals(1, l2.extract(2));
            assertEquals(1, l2.extract(3));

            return Long4.make(
                    l1.extract(0) + l2.extract(0),
                    l1.extract(1) + l2.extract(1),
                    l1.extract(2) + l2.extract(2),
                    l1.extract(3) + l2.extract(3));
        }
    }

    public void test() {
        upcall i = NativeLibrary.bindRaw(upcall.class, NativeLibrary.loadLibrary("Upcall"));
        upcall.cb v = new cbImpl();

        Long4 l = i.long4_upcall(v, Long4.make(1, 2, 3, 4), Long4.make(1, 1, 1, 1));

        assertEquals(2, l.extract(0));
        assertEquals(3, l.extract(1));
        assertEquals(4, l.extract(2));
        assertEquals(5, l.extract(3));

        if (DEBUG) {
            System.err.println("back in test()");
            System.err.println("call returned: " + l);
        }
    }

    private static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new RuntimeException("actual: " + actual + " does not match expected: " + expected);
        }
    }

    public static void main(String[] args) {
        new Long4Upcall().test();
    }
}
