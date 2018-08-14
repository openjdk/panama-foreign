/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

/*
 * @test
 * @run testng FunctionAccessTest
 */

import java.lang.invoke.MethodHandles;
import java.foreign.Libraries;
import java.foreign.Library;
import java.foreign.Scope;
import java.foreign.annotations.NativeCallback;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Callback;
import java.foreign.memory.Resource;
import java.foreign.memory.Struct;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

@Test
public class FunctionAccessTest {

    @Test
    public void testFunctionField() {
        NativeToIntFunction funcOutsideScope = null;
        try (Scope s = Scope.newNativeScope()) {
            MyStruct m = s.allocateStruct(MyStruct.class);
            m.setFunction(() -> 42);
            NativeToIntFunction func = m.getFunction();
            assertTrue(func.resource().isPresent());
            Resource resource = func.resource().get();
            //do a roundtrip
            m.setFunction(func);
            System.gc();
            func = m.getFunction();

            //check resource is still there
            assertTrue(func.resource().isPresent());
            assertEquals(func.resource().get(), resource);
            funcOutsideScope = func;
            assertEquals(func.get(), 42);
            //check that calling get twice yields same object we started with
            m.setFunction(func);
            assertEquals(func.resource(), m.getFunction().resource());
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
        try {
            funcOutsideScope.get(); // should throw!
            fail("exception not thrown!");
        } catch (IllegalStateException ex) {
            PointerTest.assertEquals(ex.getMessage(), "Scope is not alive");
        }
    }

    @NativeStruct("[" +
            "   u64(get=getFunction)(set=setFunction):()i32" +
            "](MyStruct)")
    public interface MyStruct extends Struct<MyStruct> {
        NativeToIntFunction getFunction();
        void setFunction(NativeToIntFunction runnable);
    }

    @NativeCallback("()i32")
    public interface NativeToIntFunction extends Callback<NativeToIntFunction> {
        int get();
    }

    @Test
    public void testFunctionGlobal() {
        Library lib = Libraries.loadLibrary(MethodHandles.lookup(), "GlobalFunc");
        globalFunc gf = Libraries.bind(globalFunc.class, lib);
        assertEquals(gf.fp().f(8), 64);
        gf.setFp(x -> x / 2);
        System.gc();
        assertEquals(gf.fp().f(8), 4);
    }

    @NativeHeader(declarations = "fp=u64(get=fp)(set=setFp):(i32)i32")
    public interface globalFunc {
        Func fp();
        void setFp(Func func);

        @NativeCallback("(i32)i32")
        interface Func extends Callback<Func> {
            int f(int i);
        }
    }
}
