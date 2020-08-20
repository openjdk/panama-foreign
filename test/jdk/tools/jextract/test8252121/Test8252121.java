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

import java.util.stream.IntStream;
import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;
import static org.testng.Assert.assertEquals;
import static test.jextract.arrayparam.arrayparam_h.*;

/*
 * @test
 * @bug 8252121
 * @summary jextract generated code fails with ABI for typedefed array type parameters
 * @library ..
 * @modules jdk.incubator.jextract
 * @run driver JtregJextract -t test.jextract.arrayparam -l Arrayparam -- arrayparam.h
 * @run testng/othervm -Dforeign.restricted=permit Test8252121
 */
public class Test8252121 {
    @Test
    public void test() {
        try (NativeScope scope = NativeScope.unboundedScope()) {
            int[] array = { 3, 5, 89, 34, -33 };
            MemorySegment seg = scope.allocateArray(CSupport.C_INT, array);
            assertEquals(IntStream.of(array).sum(), sum(seg));
            assertEquals(IntStream.of(array).reduce(1, (a,b) -> a*b), mul(seg));
        }
    }
}
