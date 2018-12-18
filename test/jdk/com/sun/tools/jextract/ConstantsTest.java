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

import org.testng.annotations.*;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;


/*
 * @test
 * @modules jdk.jextract
 * @build ConstantsTest
 * @run testng ConstantsTest
 */
public class ConstantsTest extends JextractToolRunner {

    Object[] constants;
    Path clzPath;
    Path dirPath;

    @BeforeTest
    public void setup() {
        clzPath = getOutputFilePath("ConstantsTest.c.jar");
        dirPath = getOutputFilePath("ConstantsTest.c.dir");
        run("-o", clzPath.toString(), "-d", dirPath.toString(),
                getInputFilePath("constants.h").toString()).checkSuccess();
        Class<?>[] cls = {
                loadClass("constants", clzPath),
                loadClass("constants", dirPath)
        };
        constants = new Object[cls.length];
        for (int i = 0 ; i < cls.length ; i++) {
            Class<?> cl = cls[i];
            constants[i] = Proxy.newProxyInstance(cl.getClassLoader(),
                    new Class<?>[]{ cl },
                    (proxy, method, args) -> MethodHandles.privateLookupIn(cl, MethodHandles.lookup())
                                .unreflectSpecial(method, cl)
                                .bindTo(proxy)
                                .invokeWithArguments(args));
        }
    }

    @AfterTest
    public void cleanup() {
        deleteFile(clzPath);
        deleteDir(dirPath);
    }


    @Test(dataProvider = "definedConstants")
    public void checkConstantsSignatures(String name, Class<?> type, Object value) {
        for (Object c : constants) {
            checkMethod(c.getClass(), name, type);
        }
    }

    @Test(dataProvider = "definedConstants")
    public void checkConstantsValues(String name, Class<?> type, Object value) throws ReflectiveOperationException {
        for (Object c : constants) {
            Object actual = c.getClass().getDeclaredMethod(name).invoke(c);
            assertEquals(actual, value);
        }
    }

    @Test(dataProvider = "missingConstants")
    public void checkMissingConstants(String name) {
        for (Object c : constants) {
            assertTrue(Stream.of(c.getClass().getDeclaredMethods())
                    .noneMatch(m -> m.getName().equals(name)));
        }
    }

    @DataProvider
    public static Object[][] definedConstants() {
        return new Object[][] {
                { "ZERO", int.class, 0 },
                { "ONE", int.class, 1 },
                { "TWO", int.class, 2 },
                { "THREE", int.class, 3 },
                { "FOUR", long.class, 4L },
                { "FIVE", long.class, 5L },
                { "SIX", int.class, 6 },
                { "STR", String.class, "Hello" },
                { "FLOAT_VALUE", float.class, 1.32f },
                { "DOUBLE_VALUE", double.class, 1.32 },
                { "QUOTE", String.class, "QUOTE" },
                { "CHAR_VALUE", char.class, 'h'},
                { "SUB", int.class, 7 }
        };
    }

    @DataProvider
    public static Object[][] missingConstants() {
        return new Object[][] {
                { "ID" },
                { "SUM" },
                { "BLOCK_BEGIN" },
                { "BLOCK_END" },
                { "INTEGER_MAX_VALUE" },
                { "CYCLIC_1" },
                { "CYCLIC_2" },
                { "SUP" },
                { "UNUSED" }
        };
    }
}
