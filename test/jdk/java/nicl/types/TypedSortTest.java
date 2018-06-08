/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * @run testng TypedSortTest
 */

import java.lang.invoke.MethodHandles;
import java.nicl.Libraries;
import static java.nicl.NativeTypes.*;
import java.nicl.Scope;
import java.nicl.metadata.NativeCallback;
import java.nicl.metadata.NativeHeader;
import java.nicl.types.Array;
import java.nicl.types.Pointer;

import org.testng.annotations.*;
import static org.testng.Assert.assertEquals;

public class TypedSortTest {

    @NativeHeader(declarations = "qsort=(u64:[0i32]i32u64u64:(u64:i32u64:i32)i32)v")
    public interface StdLib {
        void qsort(Pointer<Integer> base, int nitems, long size, QsortComparator comp);

        @NativeCallback("(u64:i32u64:i32)i32")
        interface QsortComparator {
            int compare(Pointer<Integer> p1, Pointer<Integer> p2);
        }
    }

    StdLib stdLib = Libraries.bind(MethodHandles.lookup(), StdLib.class);

    int[] qsort(int[] array) {
        try (Scope s = Scope.newNativeScope()) {
            //allocate the array
            Array<Integer> c_arr = s.allocateArray(INT32, array);

            //call the function
            stdLib.qsort(c_arr.elementPointer(), array.length, INT32.bytesSize(),
                    (u1, u2) -> {
                            int i1 = u1.get();
                            int i2 = u2.get();
                            return i1 - i2;
                    });

            //get result
            return c_arr.toArray(int[]::new);
       }
    }

    @Test
    public void testQsort() {
        int[] input = new int[] { 3, 7, 9, 2, 4, 3 };
        assertEquals(qsort(input), new int[] { 2, 3, 3, 4, 7, 9 });
    }
}
