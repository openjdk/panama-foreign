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
 * @run main/othervm DoubleUpcall
 */

import java.lang.invoke.MethodHandles;
import java.nicl.Libraries;
import java.nicl.metadata.C;
import java.nicl.metadata.CallingConvention;
import java.nicl.metadata.Header;
import java.nicl.metadata.NativeType;

public class DoubleUpcall {
    private static final boolean DEBUG = false;

    @Header(path="dummy")
    public static interface upcall {
        @FunctionalInterface
        static interface cb {
            @C(file="dummy", line=47, column=11, USR="c:@F@slowsort")
            @NativeType(layout="(dd)d", ctype="double (double,double)", size=4l)
            @CallingConvention(value=1)
            public double fn(double d1, double d2);
        }

        @C(file="dummy", line=47, column=11, USR="c:@F@double_upcall")
        @NativeType(layout="(p:(DD)DDD)D", ctype="double (double_upcall_cb, double, double)", name="double_upcall", size=1)
        @CallingConvention(value=1)
        public abstract double double_upcall(cb cb, double d1, double d2);
    }

    public static class cbImpl implements upcall.cb {
        @Override
        public double fn(double d1, double d2) {
            if (DEBUG) {
                System.err.println("fn(" + d1 + ", " + d2 + ")");
            }

            assertEquals(1.23, d1, 0.1);
            assertEquals(1.11, d2, 0.1);

            return d1 + d2;
        }
    }

    public void test() {
        upcall i = Libraries.bindRaw(upcall.class, Libraries.loadLibrary(MethodHandles.lookup(), "Upcall"));
        upcall.cb v = new cbImpl();

        double d = i.double_upcall(v, 1.23, 1.11);

        if (DEBUG) {
            System.err.println("back in test()");
            System.err.println("call returned: " + d);
        }

        assertEquals(2.34, d, 0.1);
    }

    private static void assertEquals(double expected, double actual, double maxDiff) {
        if (Math.abs(expected - actual) > maxDiff) {
            throw new RuntimeException("actual: " + actual + " does not match expected: " + expected);
        }
    }

    public static void main(String[] args) {
        new DoubleUpcall().test();
    }
}
