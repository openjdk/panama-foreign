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

import jdk.incubator.foreign.MemorySegment;

import static org.testng.Assert.assertEquals;
import static test.jextract.printf.printf_h.*;
import static jdk.incubator.foreign.CSupport.*;

/*
 * @test
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -t test.jextract.printf -l Printf -- printf.h
 * @run testng/othervm -Dforeign.restricted=permit Test8244959
 */
public class Test8244959 {
    @Test
    public void testsPrintf() {
        try (MemorySegment s = MemorySegment.allocateNative(1024)) {
            my_sprintf(s,
                toCString("%hhd %c %.2f %.2f %lld %lld %d %hd %d %d %lld %c"), 12,
                (byte) 1, 'b', -1.25f, 5.5d, -200L, Long.MAX_VALUE, (byte) -2, (short) 2, 3, (short) -4, 5L, 'a');
            String str = toJavaString(s);
            assertEquals(str, "1 b -1.25 5.50 -200 " + Long.MAX_VALUE + " -2 2 3 -4 5 a");
        }
    }
}
