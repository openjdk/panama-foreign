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

import jdk.incubator.foreign.ResourceScope;
import org.testng.annotations.Test;

import jdk.incubator.foreign.MemorySegment;

import static org.testng.Assert.assertEquals;
import static test.jextract.vsprintf.vsprintf_h.*;
import static jdk.incubator.foreign.CLinker.*;

/*
 * @test id=classes
 * @bug 8252016
 * @summary jextract should handle va_list
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -t test.jextract.vsprintf -l VSPrintf -- vsprintf.h
 * @run testng/othervm -Dforeign.restricted=permit Test8252016
 */
/*
 * @test id=sources
 * @bug 8252016
 * @summary jextract should handle va_list
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextractSources -t test.jextract.vsprintf -l VSPrintf -- vsprintf.h
 * @run testng/othervm -Dforeign.restricted=permit Test8252016
 */
public class Test8252016 {
    @Test
    public void testsVsprintf() {
        try (ResourceScope scope = ResourceScope.ofConfined()) {
            MemorySegment s = MemorySegment.allocateNative(1024, scope);
            VaList vaList = VaList.make(b -> {
                b.vargFromInt(C_INT, 12);
                b.vargFromDouble(C_DOUBLE, 5.5d);
                b.vargFromLong(C_LONG_LONG, -200L);
                b.vargFromLong(C_LONG_LONG, Long.MAX_VALUE);
            }, scope);
            my_vsprintf(s, toCString("%hhd %.2f %lld %lld"), vaList);
            String str = toJavaString(s);
            assertEquals(str, "12 5.50 -200 " + Long.MAX_VALUE);
       }
    }
}
