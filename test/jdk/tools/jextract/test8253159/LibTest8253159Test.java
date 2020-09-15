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
import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import test.jextract.test8253159.RuntimeHelper;
import static test.jextract.test8253159.test8253159_h.*;

/*
 * @test id=classes
 * @bug 8253159
 * @summary RuntimeHelper.asArrayRestricted should create noncloseable segment
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -l Test8253159 -t test.jextract.test8253159 -- test8253159.h
 * @run testng/othervm -Dforeign.restricted=permit LibTest8253159Test
 */
/*
 * @test id=sources
 * @bug 8253159
 * @summary RuntimeHelper.asArrayRestricted should create noncloseable segment
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextractSources -l Test8253159 -t test.jextract.test8253159 -- test8253159.h
 * @run testng/othervm -Dforeign.restricted=permit LibTest8253159Test
 */
public class LibTest8253159Test {
    @Test
    public void test() {
        MemoryAddress addr = get_array();
        MemorySegment seg = RuntimeHelper.asArrayRestricted(addr, CSupport.C_INT, 6);
        int[] actual = seg.toIntArray();
        int[] expected = new int[] { 2, 3, 5, 7, 11, 13};
        assertEquals(actual.length, expected.length);
        for (int i = 0; i < actual.length; i++) {
            assertEquals(actual[i], expected[i]);
        }
        boolean caughtException = false;
        try {
            seg.close();
        } catch (UnsupportedOperationException uoe) {
            System.err.println(uoe);
            caughtException = true;
        }
        assertTrue(caughtException);
    }
}
