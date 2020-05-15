/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import org.testng.annotations.Test;
import test.jextract.test8245003.*;
import static org.testng.Assert.assertEquals;
import static test.jextract.test8245003.test8245003_h.*;

/*
 * @test
 * @bug 8245003
 * @summary jextract does not generate accessor for MemorySegement typed values
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -l Test8245003 -t test.jextract.test8245003 -- test8245003.h
 * @run testng/othervm -Dforeign.restricted=permit Test8245003
 */
public class Test8245003 {
    @Test
    public void testStructAccessor() {
        var addr = special_pt$ADDR();
        assertEquals(addr.segment().byteSize(), CPoint.sizeof());
        assertEquals(CPoint.x$get(addr), 56);
        assertEquals(CPoint.y$get(addr), 75);

        addr = special_pt3d$ADDR();
        assertEquals(addr.segment().byteSize(), CPoint3D.sizeof());
        assertEquals(CPoint3D.z$get(addr), 35);
        var pointAddr = CPoint3D.p$addr(addr);
        assertEquals(pointAddr.segment().byteSize(), CPoint.sizeof());
        assertEquals(CPoint.x$get(pointAddr), 43);
        assertEquals(CPoint.y$get(pointAddr), 45);
    }

    @Test
    public void testArrayAccessor() {
        var addr = iarr$ADDR();
        assertEquals(addr.segment().byteSize(), Cint.sizeof()*5);
        int[] arr = Cint.toJavaArray(addr.segment());
        assertEquals(arr.length, 5);
        assertEquals(arr[0], 2);
        assertEquals(arr[1], -2);
        assertEquals(arr[2], 42);
        assertEquals(arr[3], -42);
        assertEquals(arr[4], 345);

        addr = foo$ADDR();
        assertEquals(addr.segment().byteSize(), CFoo.sizeof());
        assertEquals(CFoo.count$get(addr), 37);
        var greeting = CFoo.greeting$addr(addr);
        byte[] barr = Cchar.toJavaArray(greeting.segment());
        assertEquals(new String(barr), "hello");
    }
}
