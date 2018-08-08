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

import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.Array;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import test.arrayparam.funcArrayParam;
import test.arrayparam.funcArrayParam.FPPtrFieldStruct;

import static org.testng.Assert.assertEquals;

/*
 * @test
 * @bug 8209016
 * @library ..
 * @run driver JtregJextract -t test.arrayparam -- funcArrayParam.h
 * @run testng FuncArrayParamTest
 */
public class FuncArrayParamTest {
    private funcArrayParam fap;

    @BeforeTest
    public void init() {
        Library lib = Libraries.loadLibrary(MethodHandles.lookup(), "FuncArrayParam");
        fap = Libraries.bind(funcArrayParam.class, lib);
    }

    private static int[] jarr = new int[] { 34, 66, 23, 53, 345 };

    @Test
    public void testFuncArrayParam() {
        try (Scope scope = Scope.newNativeScope()) {
            Array<Integer> carr = scope.allocateArray(NativeTypes.INT32, jarr.length);
            for (int i = 0; i < jarr.length; i++) {
                carr.set(i, jarr[i]);
            }
            int sum = Arrays.stream(jarr).sum();
            assertEquals(fap.f(carr.elementPointer(), jarr.length), sum);
            assertEquals(fap.g(carr.elementPointer(), jarr.length), sum);
            assertEquals(fap.k(carr.elementPointer(), jarr.length), sum);

            assertEquals(fap.map_sum(carr.elementPointer(), jarr.length, (arr, idx, val)->2*val), 2*sum);

            FPPtrFieldStruct s = scope.allocateStruct(FPPtrFieldStruct.class);
            s.map$set((arr, idx, val) -> -val);
            assertEquals(fap.map_sum2(carr.elementPointer(), jarr.length, s), -sum);
        }
    }
}
