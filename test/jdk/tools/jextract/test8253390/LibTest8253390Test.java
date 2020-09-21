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
import static jdk.incubator.foreign.CLinker.toJavaString;
import static test.jextract.test8253390.test8253390_h.*;

/*
 * @test id=classes
 * @library ..
 * @modules jdk.incubator.jextract
 * @bug 8253390
 * @summary jextract should quote string literals
 * @run driver JtregJextract -t test.jextract.test8253390 -- test8253390.h
 * @run testng/othervm -Dforeign.restricted=permit LibTest8253390Test
 */
/*
 * @test id=sources
 * @library ..
 * @modules jdk.incubator.jextract
 * @bug 8253390
 * @summary jextract should quote string literals
 * @run driver JtregJextractSources -t test.jextract.test8253390 -- test8253390.h
 * @run testng/othervm -Dforeign.restricted=permit LibTest8253390Test
 */
public class LibTest8253390Test {
    @Test
    public void testSquare() {
        assertEquals(toJavaString(GREETING()), "hello\nworld");
        assertEquals(toJavaString(GREETING2()), "hello\tworld");
    }
}
