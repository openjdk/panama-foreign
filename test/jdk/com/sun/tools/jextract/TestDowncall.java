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

/*
 * @test
 * @modules jdk.jextract
 * @build TestDowncall
 *
 * @run testng/othervm -Djdk.internal.foreign.NativeInvoker.FASTPATH=none TestDowncall
 * @run testng/othervm TestDowncall
 */

import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.foreign.Libraries;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.layout.Padding;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TestDowncall extends JextractToolRunner {

    final static int MAX_CODE = 20;

    @Test(dataProvider = "filters")
    public void testDowncall(String filter) throws ReflectiveOperationException {
        Path clzPath = getOutputFilePath("libTestDowncall.jar");
        checkSuccess(null,"-o", clzPath.toString(),
                "--exclude-symbols", filter,
                getInputFilePath("libTestDowncall.h").toString());
        Class<?> headerCls = loadClass("libTestDowncall", clzPath);
        Object lib = Libraries.bind(headerCls, Libraries.loadLibrary(MethodHandles.lookup(), "TestDowncall"));
        for (Method m : headerCls.getDeclaredMethods()) {
            try (Scope sc = Scope.newNativeScope()) {
                List<Consumer<Object>> checks = new ArrayList<>();
                Object res = m.invoke(lib, makeArgs(sc, m, checks));
                if (m.getReturnType() != void.class) {
                    checks.forEach(c -> c.accept(res));
                }
            }
        }
    }

    Object[] makeArgs(Scope sc, Method m, List<Consumer<Object>> checks) throws ReflectiveOperationException {
        Class<?>[] params = m.getParameterTypes();
        Object[] args = new Object[params.length];
        for (int i = 0 ; i < params.length ; i++) {
            args[i] = makeArg(sc, params[i], checks, i == 0);
        }
        return args;
    }

    @SuppressWarnings("unchecked")
    Object makeArg(Scope sc, Class<?> carrier, List<Consumer<Object>> checks, boolean check) throws ReflectiveOperationException {
        if (Struct.class.isAssignableFrom(carrier)) {
            Struct<?> str = sc.allocateStruct((Class)carrier);
            initStruct(sc, str, checks, check);
            return str;
        } else if (carrier == int.class) {
            if (check) {
                checks.add(o -> assertEquals(o, 42));
            }
            return 42;
        } else if (carrier == float.class) {
            if (check) {
                checks.add(o -> assertEquals(o, 12f));
            }
            return 12f;
        } else if (carrier == double.class) {
            if (check) {
                checks.add(o -> assertEquals(o, 24d));
            }
            return 24d;
        } else if (carrier == Pointer.class) {
            Pointer<?> p = sc.allocate(NativeTypes.INT32);
            if (check) {
                checks.add(o -> {
                    try {
                        assertEquals(((Pointer<?>)o).addr(), p.addr());
                    } catch (Throwable ex) {
                        throw new IllegalStateException(ex);
                    }
                });
            }
            return p;
        } else {
            throw new IllegalStateException("Unexpected carrier: " + carrier);
        }
    }

    void initStruct(Scope sc, Struct<?> str, List<Consumer<Object>> checks, boolean check) throws ReflectiveOperationException {
        Group g = (Group)str.ptr().type().layout();
        for (Layout l : g.elements()) {
            if (l instanceof Padding) continue;
            Method getter = str.getClass().getDeclaredMethod(l.annotations().get("get"));
            Class<?> carrier = getter.getReturnType();
            Method setter = str.getClass().getDeclaredMethod(l.annotations().get("set"), carrier);
            List<Consumer<Object>> fieldsCheck = new ArrayList<>();
            Object value = makeArg(sc, carrier, fieldsCheck, check);
            //set value
            setter.invoke(str, value);
            //add check
            if (check) {
                assertTrue(fieldsCheck.size() == 1);
                checks.add(o -> {
                    try {
                        fieldsCheck.get(0).accept(getter.invoke(o));
                    } catch (Throwable ex) {
                        throw new IllegalStateException(ex);
                    }
                });
            }
        }
    }

    @DataProvider(name = "filters")
    public static Object[][] filters() {
        Object[][] res = new Object[MAX_CODE][];
        for (int i = 0 ; i < MAX_CODE ; i++) {
            res[i] = new Object[] { filterFor(i) };
        }
        return res;
    }

    static String filterFor(int k) {
        List<String> patterns = new ArrayList<>();
        for (int i = 0 ; i < MAX_CODE ; i++) {
            if (i != k) {
                patterns.add("f" + i + "_");
            }
        }
        return String.format("(%s).*", String.join("|", patterns));
    }
}
