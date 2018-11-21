/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
import static test.jextract.utils.utils_h.*;

/*
 * @test
 * @library ..
 * @run driver JtregJextract -t test.jextract.utils -lUtils -- utils.h
 * @run testng StaticForwarderTest
 */
public class StaticForwarderTest {
    @Test
    public void test() {
        assertEquals(square(6), 36);
        assertEquals(R, 10);
        assertEquals(G, R + 1);
        assertEquals(B, G + 1);
        assertEquals(abc$get(), 53);
        assertEquals(square(abc$get()), 53*53);
        abc$set(25);
        assertEquals(abc$get(), 25);
        assertEquals(THE_ANSWER, 42);
        assertEquals(square(THE_ANSWER), 42*42);
    }
}
