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
 * @build TestUpcall
 *
 * @run testng/othervm -Djdk.internal.foreign.UpcallHandler.FASTPATH=none TestUpcall
 * @run testng/othervm TestUpcall
 */

import org.testng.annotations.*;

import java.foreign.Libraries;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeSetter;
import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.layout.Padding;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.testng.Assert.*;

public class TestUpcall extends JextractToolRunner {

    final static int MAX_CODE = 20;
    final List<Runnable> cleanups = new ArrayList<>();

    public static class UpcallTest {

        private final Class<?> headerCls;
        private final Object lib;
    
        public UpcallTest(Class<?> headerCls, Object lib) {
            this.headerCls = headerCls;
            this.lib = lib;
        }
    
        @Test(dataProvider = "getArgs")
        public void testUpCall(String mName, @NoInjection Method m)  throws ReflectiveOperationException {
            System.err.print("Calling " + mName + "...");
            try(Scope scope = Scope.newNativeScope()) {
                List<Consumer<Object>> checks = new ArrayList<>();
                Object res = m.invoke(lib, makeArgs(scope, m, checks));
                if (m.getReturnType() != void.class) {
                    checks.forEach(c -> c.accept(res));
                }
            }
            System.err.println("...done");
        }
    
        @DataProvider
        public Object[][] getArgs() {
            return Stream.of(headerCls.getDeclaredMethods())
                    .map(m -> new Object[]{ m.getName(), m })
                    .toArray(Object[][]::new);
        }
    
    }

    @Factory
    public Object[] getTests() throws ReflectiveOperationException {
        List<UpcallTest> res = new ArrayList<>();
        for (int i = 0 ; i < MAX_CODE ; i++) {
            Path clzPath = getOutputFilePath("libTestUpcall" + i + ".jar");
            run("-o", clzPath.toString(),
                    "--exclude-symbols", filterFor(i),
                    getInputFilePath("libTestUpcall.h").toString()).checkSuccess();
            Loader loader = classLoader(clzPath);
            Class<?> headerCls = loader.loadClass("libTestUpcall");
            Object lib = Libraries.bind(headerCls, Libraries.loadLibrary(MethodHandles.lookup(), "TestUpcall"));
            res.add(new UpcallTest(headerCls, lib));
            cleanups.add(() -> {
               loader.close();
               deleteFile(clzPath);
            });
        }
        if(res.isEmpty())
            throw new RuntimeException("Could not generate any tests");
        return res.toArray();
    }

    @AfterSuite
    public void after() {
        cleanups.forEach(Runnable::run);
    }

    static Object[] makeArgs(Scope sc, Method m, List<Consumer<Object>> checks) throws ReflectiveOperationException {
        Class<?>[] params = m.getParameterTypes();
        Object[] args = new Object[params.length];
        for (int i = 0 ; i < params.length - 1 ; i++) {
            args[i] = makeArg(sc, params[i], checks, i == 0);
        }
        args[params.length - 1] = makeCallback(sc, m);
        return args;
    }

    @SuppressWarnings("unchecked")
    static Object makeArg(Scope sc, Class<?> carrier, List<Consumer<Object>> checks, boolean check) throws ReflectiveOperationException {
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

    static void initStruct(Scope sc, Struct<?> str, List<Consumer<Object>> checks, boolean check) throws ReflectiveOperationException {
        Group g = (Group)str.ptr().type().layout();
        for (Layout l : g.elements()) {
            if (l instanceof Padding) continue;
            Method getter = findAccessor(str.getClass(), l.name().get(), NativeGetter.class, NativeGetter::value);
            Class<?> carrier = getter.getReturnType();
            Method setter = findAccessor(str.getClass(), l.name().get(), NativeSetter.class, NativeSetter::value);
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

    static <A extends Annotation> Method findAccessor(Class<?> clazz, String name, Class<A> anno, Function<A, String> nameFunc) {
        return Stream.of(clazz.getInterfaces()[0].getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(anno))
                .filter(m -> nameFunc.apply(m.getAnnotation(anno)).equals(name))
                .findFirst().get();
    }

    @SuppressWarnings("unchecked")
    static Object makeCallback(Scope sc, Method m) {
        ParameterizedType callbackParam = ((ParameterizedType)m.getGenericParameterTypes()[m.getParameterCount() - 1]);
        Class<?> callbackType = (Class<?>)callbackParam.getActualTypeArguments()[0];
        Object cb = sc.allocateCallback((Class)callbackType, allocateCallbackInstance(callbackType));
        return cb;
        //throw new UnsupportedOperationException("Hello " + callbackType);
    }

    static Object allocateCallbackInstance(Class<?> carrier) {
        return Proxy.newProxyInstance(carrier.getClassLoader(), new Class<?>[] { carrier },
                (proxy, method, args) -> args.length > 0 ? args[0] : null);
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
