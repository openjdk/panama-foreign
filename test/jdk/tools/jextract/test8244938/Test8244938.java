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
import static org.testng.Assert.assertEquals;
import static test.jextract.test8244938.test8244938_h.*;

/*
 * @test
 * @bug 8244938
 * @summary Crash in foreign ABI CallArranger class when a test native function returns a nested struct
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -l Test8244938 -t test.jextract.test8244938 -- test8244938.h
 * @run testng/othervm -Dforeign.restricted=permit Test8244938
 */
public class Test8244938 {
    @Test
    public void testNestedStructReturn() {
         var seg = func();
         assertEquals(seg.byteSize(), Point.sizeof());
         var addr = seg.address();
         assertEquals(Point.k$get(addr), 44);
         var point2dAddr = Point.point2d$addr(addr);
         assertEquals(Point2D.i$get(point2dAddr), 567);
         assertEquals(Point2D.j$get(point2dAddr), 33);
    }
}
