/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 * @requires (os.family != "windows") & (os.arch == "amd64" | os.arch == "x86_64")
 */

import java.nicl.NativeLibrary;
import java.nicl.metadata.C;
import java.nicl.metadata.CallingConvention;
import java.nicl.metadata.Header;
import java.nicl.metadata.NativeType;

public class Vector {
    @Header(path="dummy")
    static interface vector {
        @C(file="dummy", line=47, column=11, USR="c:@F@foo")
        @NativeType(layout="(=256v)i", ctype="int (__m256i)", name="foo", size=1)
        @CallingConvention(value=1)
        int foo(Long4 v);

        @C(file="dummy", line=47, column=11, USR="c:@F@reverse")
        @NativeType(layout="(=256v)=256v", ctype="__m256i (__m256i)", name="reverse", size=1)
        @CallingConvention(value=1)
        Long4 reverse(Long4 v);
    }

    public void testSimple() {
        vector i = NativeLibrary.bindRaw(vector.class, NativeLibrary.loadLibrary("Vector"));

        Long4 arg = Long4.make(1, 2, 3, 4);
        assertEquals(4711, i.foo(arg));

        Long4 ret = i.reverse(arg);

        assertEquals(4, ret.extract(0));
        assertEquals(3, ret.extract(1));
        assertEquals(2, ret.extract(2));
        assertEquals(1, ret.extract(3));
    }

    public static void main(String[] args) {
        new Vector().testSimple();
    }

    static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new RuntimeException("expected: " + expected + " does not match actual: " + actual);
        }
    }
}
