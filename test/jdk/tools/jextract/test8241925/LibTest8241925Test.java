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

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.NativeScope;
import org.testng.annotations.Test;
import test.jextract.test8241925.*;
import static org.testng.Assert.assertEquals;
import static test.jextract.test8241925.test8241925_h.*;
import static jdk.incubator.foreign.CLinker.*;

/*
 * @test
 * @library ..
 * @modules jdk.incubator.jextract
 * @bug 8241925
 * @summary jextract should generate simple allocation, access API for C primitive types
 * @run driver JtregJextract -l Test8241925 -t test.jextract.test8241925 -- test8241925.h
 * @run testng/othervm -Dforeign.restricted=permit LibTest8241925Test
 */
public class LibTest8241925Test {
    @Test
    public void test() {
        try (var scope = NativeScope.unboundedScope()) {
            var addr = scope.allocate(C_INT, 12);
            assertEquals(MemoryAccess.getInt(addr), 12);
            square(addr);
            assertEquals(MemoryAccess.getInt(addr), 144);

            addr = scope.allocate(C_DOUBLE, 12.0);
            assertEquals(MemoryAccess.getDouble(addr), 12.0, 0.1);
            square_fp(addr);
            assertEquals(MemoryAccess.getDouble(addr), 144.0, 0.1);

            int[] intArray = { 34, 67, 78, 8 };
            addr = scope.allocateArray(C_INT, intArray);
            int sum = sum(addr, intArray.length);
            assertEquals(sum, IntStream.of(intArray).sum());
            int[] convertedArray = addr.toIntArray();
            assertEquals(convertedArray, intArray);

            double[] dblArray = { 34.5, 67.56, 78.2, 8.45 };
            addr = scope.allocateArray(C_DOUBLE, dblArray);
            double sumd = sum_fp(addr, dblArray.length);
            assertEquals(sumd, DoubleStream.of(dblArray).sum(), 0.1);
            double[] convertedDblArray = addr.toDoubleArray();
            for (int i = 0; i < dblArray.length; i++) {
                assertEquals(dblArray[i], convertedDblArray[i], 0.1);
            }

            assertEquals(toJavaStringRestricted(name()), "java");

            var dest = scope.allocateArray(C_CHAR, 12);
            dest.copyFrom(toCString("hello "));
            var src = toCString("world", scope);
            assertEquals(toJavaStringRestricted(concatenate(dest, src)), "hello world");
        }
    }
}
