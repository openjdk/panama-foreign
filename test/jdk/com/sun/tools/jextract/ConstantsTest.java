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

import java.foreign.Libraries;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.testng.Assert.assertTrue;


/*
 * @test
 * @modules jdk.jextract
 * @build ConstantsTest
 * @run testng ConstantsTest
 */
public class ConstantsTest extends JextractToolRunner {

    Object constants;
    Path clzPath;
    Path dirPath;
    Loader loader;

    @BeforeTest
    public void setup() {
        clzPath = getOutputFilePath("ConstantsTest.c.jar");
        dirPath = getOutputFilePath("ConstantsTest.c.dir");
        run("-o", clzPath.toString(), "-d", dirPath.toString(),
                getInputFilePath("constants.h").toString()).checkSuccess();
        loader = classLoader(clzPath);
        Class<?> cls = loader.loadClass("constants");
        constants = Libraries.bind(MethodHandles.lookup(), cls);
    }

    @AfterTest
    public void cleanup() {
        loader.close();
        deleteFile(clzPath);
        deleteDir(dirPath);
    }


    @Test(dataProvider = "definedConstants")
    public void checkConstantsSignatures(String name, Class<?> type, Object value) {
        checkMethod(constants.getClass(), name, type);
    }

    @Test(dataProvider = "definedConstants")
    public void checkConstantsValues(String name, Class<?> type, Predicate<Object> checker) throws ReflectiveOperationException {
        Object actual = constants.getClass().getDeclaredMethod(name).invoke(constants);
        assertTrue(checker.test(actual));
    }

    @Test(dataProvider = "missingConstants")
    public void checkMissingConstants(String name) {
        assertTrue(Stream.of(constants.getClass().getDeclaredMethods())
                .noneMatch(m -> m.getName().equals(name)));
    }

    @DataProvider
    public static Object[][] definedConstants() {
        return new Object[][] {
                { "ZERO", int.class, equalsTo(0) },
                { "ONE", int.class, equalsTo(1) },
                { "TWO", int.class, equalsTo(2) },
                { "THREE", int.class, equalsTo(3) },
                { "FOUR", long.class, equalsTo(4L) },
                { "FIVE", long.class, equalsTo(5L) },
                { "SIX", int.class, equalsTo(6) },
                { "STR", Pointer.class, pointerEqualsTo("Hello") },
                { "FLOAT_VALUE", float.class, equalsTo(1.32f) },
                { "DOUBLE_VALUE", double.class, equalsTo(1.32) },
                { "QUOTE", Pointer.class, pointerEqualsTo("QUOTE") },
                { "CHAR_VALUE", int.class, equalsTo(104) }, //integer char constants have type int
                { "MULTICHAR_VALUE", int.class, equalsTo(26728) },  //integer char constants have type int
                { "BOOL_VALUE", boolean.class, equalsTo(true) },
                { "ZERO_PTR", Pointer.class, pointerEqualsTo(0L) },
                { "F_PTR", Pointer.class, pointerEqualsTo(0xFFFF_FFFF_FFFF_FFFFL) }
        };
    }

    static Predicate<Object> equalsTo(Object that) {
        return o -> o.equals(that);
    }

    static Predicate<Pointer<Byte>> pointerEqualsTo(String that) {
        return p -> Pointer.toString(p).equals(that);
    }

    static Predicate<Pointer<Byte>> pointerEqualsTo(long addr) {
        return p -> {
            try {
                return p.addr() == addr;
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
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
