/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
import java.foreign.Scope;
import java.foreign.memory.Callback;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.function.IntConsumer;

import org.testng.annotations.*;
import static org.testng.Assert.*;

/*
 * @test
 * @bug 8217164
 * @library . ..
 * @modules jdk.jextract
 * @run testng/othervm -Duser.language=en SimpleFuncParamTest
 */

public class SimpleFuncParamTest extends JextractToolRunner {

    private static final MethodHandle MH_testCB;

    static {
        try  {
            MH_testCB = MethodHandles.lookup().findVirtual(SimpleFuncParamTest.class, "testCB", MethodType.methodType(void.class, int.class));
        } catch(ReflectiveOperationException e) {
            throw new BootstrapMethodError(e);
        }
    }

    private boolean wasCalled;

    @BeforeTest
    public void before() {
        wasCalled = false;
    }

    @Test
    public void testFuncParam() throws Throwable {
        Path jarPath = getOutputFilePath("FuncParam.jar");
        deleteFile(jarPath);
        Path hPath = getInputFilePath("funcParam.h");
        run("-o", jarPath.toString(), hPath.toString()).checkSuccess();
        try(Loader loader = classLoader(jarPath)) {
            Class<?> cbClass = loader.loadClass("funcParam$FI1");
            Class<?> funcClass = loader.loadClass("funcParam");
            try (Scope scope = Scope.newNativeScope()) {
                Lookup l = MethodHandles.lookup();
                Callback<?> cb = scope.allocateCallback((Class) cbClass, MethodHandleProxies.asInterfaceInstance(cbClass, MH_testCB.bindTo(this)));
                MethodHandle target = l.findVirtual(funcClass, "f", MethodType.methodType(void.class, Callback.class));
                Object lib = Libraries.bind(funcClass, Libraries.loadLibrary(l, "FuncParam"));
                target.invoke(lib, cb);
                assertTrue(wasCalled);
            }
        } finally {
            deleteFile(jarPath);
        }

    }

    private void testCB(int i) {
        wasCalled = true;
        assertEquals(i, 10);
    }

}
