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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.nicl.layout.Function;
import java.nicl.metadata.NativeHeader;
import java.nio.file.Path;
import java.util.Map;
import jdk.internal.nicl.types.DescriptorParser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @modules jdk.jextract java.base/jdk.internal.nicl.types
 * @build StructTest
 * @run testng StructTest
 */
public class StructTest extends JextractToolRunner {
    private static final String[] ExpectedIfs = {
            "TypedefNamedAsIs",
            "TypedefNamedDifferent",
            "TypedefAnonymous",
            "Plain",
            "IncompleteArray"
    };

    private static final String[] ExpectedFIs = {
            "FunctionPointer",
            "FI1",
            // FIXME: FunctionPointer should only be generated once wo anonymous FI2
            "FI2"
    };

    private static final String[] ExpectedTypeAnnotations = {
            "UndefinedStruct",
            // "UndefineStructForPointer",
            "UndefinedStructPointer",
            // "Opaque",
            "TypedefNamedDifferent_t"
    };

    private static final String[] ExpectedAnonymousRecord = {
            "TypedefAnonymous__anonymous_struct",
    };

    private static final int NumberOfInnerClasses = ExpectedIfs.length +
            ExpectedFIs.length + ExpectedTypeAnnotations.length +
            ExpectedAnonymousRecord.length;

    private void verifyPlain(Class<?> plain) {
        assertNotNull(plain);
        checkMethod(plain, "x$get", int.class);
        checkMethod(plain, "x$set", void.class, int.class);
        checkMethod(plain, "x$ptr", java.nicl.types.Pointer.class);
        checkMethod(plain, "y$get", int.class);
        checkMethod(plain, "y$set", void.class, int.class);
        checkMethod(plain, "y$ptr", java.nicl.types.Pointer.class);
    }

    private void verifyTypedefNamedAsIs(Class<?> asIs) {
        assertNotNull(asIs);
    }

    private void verifyExpectedAnnotations(Class<?>[] declared) {
        for (String name : ExpectedTypeAnnotations) {
            Class<?> cls = findClass(declared, name);
            assertNotNull(cls);
            assertTrue(cls.isAnnotation());
        }
    }

    private void assertVoidPointer(ParameterizedType pvoid, boolean isWildcard) {
        assertEquals(pvoid.getRawType(), java.nicl.types.Pointer.class);
        Type[] tas = pvoid.getActualTypeArguments();
        assertEquals(tas.length, 1);
        if (isWildcard) {
            WildcardType wt = (WildcardType) tas[0];
            assertEquals(wt.getLowerBounds().length, 0);
            assertEquals(wt.getUpperBounds().length, 1);
            assertEquals(wt.getUpperBounds()[0], Object.class);
        } else {
            assertEquals(tas[0], Void.class);
        }
    }

    private void verifyIncompleteArray(Class<?> incomplete) {
        assertNotNull(incomplete);
        Method f = findMethod(incomplete, "ptr$get");
        ParameterizedType pVoid = (ParameterizedType) f.getGenericReturnType();
        assertVoidPointer(pVoid, false);
        f = findMethod(incomplete, "ptr$set", java.nicl.types.Pointer.class);
        // Setter for void* should be wildcard
        ParameterizedType pWildcard = (ParameterizedType) f.getGenericParameterTypes()[0];
        assertVoidPointer(pWildcard, true);

        f = findMethod(incomplete, "junk$get");
        ParameterizedType ppVoid = (ParameterizedType) f.getGenericReturnType();
        assertEquals(ppVoid.getActualTypeArguments()[0], pVoid);
        f = findMethod(incomplete, "junk$set", java.nicl.types.Pointer.class);
        assertEquals(f.getGenericParameterTypes()[0], ppVoid);
    }

    private void verifyFunctionWithVoidPointer(Class<?> cls) {
        Method m = findMethod(cls, "FunctionWithVoidPointer",
                java.nicl.types.Pointer.class,
                java.nicl.types.Pointer.class);
        assertNotNull(m);

        ParameterizedType pVoid = (ParameterizedType) m.getGenericReturnType();
        assertVoidPointer(pVoid, false);

        Type[] args = m.getGenericParameterTypes();
        assertEquals(args.length, 2);

        ParameterizedType pWildcard = (ParameterizedType) args[0];
        assertVoidPointer(pWildcard, true);

        ParameterizedType ppVoid = (ParameterizedType) args[1];
        assertEquals(ppVoid.getActualTypeArguments()[0], pVoid);
    }

    private void verifyFunctionPointer(Class<?> cls) {
        Method m = findMethod(cls, "fn",
                java.nicl.types.Pointer.class,
                java.nicl.types.Pointer.class);
        assertNotNull(m);

        ParameterizedType pVoid = (ParameterizedType) m.getGenericReturnType();
        assertVoidPointer(pVoid, false);

        Type[] args = m.getGenericParameterTypes();
        assertEquals(args.length, 2);

        ParameterizedType pWildcard = (ParameterizedType) args[0];
        assertVoidPointer(pWildcard, true);

        ParameterizedType ppVoid = (ParameterizedType) args[1];
        assertEquals(ppVoid.getActualTypeArguments()[0], pVoid);
    }

    private void verifyUndefinedStructFunctions(Class<?> header) {
        NativeHeader nh = header.getAnnotation(NativeHeader.class);
        Map<String, Object> map = DescriptorParser.parseHeaderDeclarations(nh.declarations());

        Method m = findMethod(header, "getParent", java.nicl.types.Pointer.class);
        assertNotNull(m);
        ParameterizedType pVoid = (ParameterizedType) m.getGenericReturnType();
        assertVoidPointer(pVoid, false);
        Type[] args = m.getGenericParameterTypes();
        assertEquals(args.length, 1);
        ParameterizedType pWildcard = (ParameterizedType) args[0];
        assertVoidPointer(pWildcard, true);

        Function fn = (Function) map.get("getParent");
        fn = (Function) map.get("getSibling");
        fn = (Function) map.get("getFirstChild");
        assertEquals(fn.toString(), "(u64:i8)u64:i8");
    }

    private void verifyClass(Class<?> cls) {
        Class<?>[] clz = cls.getDeclaredClasses();
        assertEquals(clz.length, NumberOfInnerClasses);
        verifyPlain(findClass(clz, "Plain"));
        verifyTypedefNamedAsIs(findClass(clz, "TypedefNamedAsIs"));
        verifyExpectedAnnotations(clz);
        verifyFunctionWithVoidPointer(cls);
        verifyFunctionPointer(findClass(clz,"FunctionPointer"));
        verifyIncompleteArray(findClass(clz, "IncompleteArray"));
        checkMethod(cls, "voidArguments", void.class);
        verifyUndefinedStructFunctions(cls);
    }

    private void verifyAsCpp(Class<?> cls) {
        verifyClass(cls);
        checkMethod(cls, "emptyArguments", void.class);
    }

    private void verifyAsC(Class<?> cls) {
        verifyClass(cls);
        checkMethod(cls, "emptyArguments", void.class, Object[].class);
    }

    @Test
    public void testCppMode() {
        Path clzPath = getOutputFilePath("StructTest.cpp.jar");
        checkSuccess(null, "-C", "-x", "-C", "c++",
                "-o", clzPath.toString(),
                getInputFilePath("struct.h").toString());
        Class<?> cls = loadClass("struct", clzPath);
        verifyAsCpp(cls);
        deleteFile(clzPath);
    }

    @Test
    public void testCMode() {
        Path clzPath = getOutputFilePath("StructTest.c.jar");
        checkSuccess(null,"-o", clzPath.toString(),
                getInputFilePath("struct.h").toString());
        Class<?> cls = loadClass("struct", clzPath);
        verifyAsC(cls);
        deleteFile(clzPath);
    }
}
