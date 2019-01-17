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

import java.foreign.NativeTypes;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeStruct;
import java.foreign.layout.Descriptor;
import java.foreign.layout.Function;
import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import jdk.internal.foreign.LayoutResolver;
import jdk.internal.foreign.memory.DescriptorParser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @modules jdk.jextract java.base/jdk.internal.foreign.memory java.base/jdk.internal.foreign
 * @library ..
 * @build JextractToolRunner
 * @run testng StructTest
 */
public class StructTest extends JextractToolRunner {
    private static final String[] ExpectedIfs = {
            "TypedefNamedAsIs",
            "TypedefNamedDifferent",
            "TypedefAnonymous",
            "Plain",
            "IncompleteArray",
            "UndefinedStruct",
            "UndefinedStructForPointer",
            "Opaque"
    };

    private static final String[] ExpectedFIs = {
            "FunctionPointer",
            "FI1",
            // FIXME: FunctionPointer should only be generated once wo anonymous FI2
            "FI2"
    };

    private static final String[] ExpectedTypeAnnotations = {
            "UndefinedStructPointer",
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
        checkMethod(plain, "x$ptr", java.foreign.memory.Pointer.class);
        checkMethod(plain, "y$get", int.class);
        checkMethod(plain, "y$set", void.class, int.class);
        checkMethod(plain, "y$ptr", java.foreign.memory.Pointer.class);
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
        assertEquals(pvoid.getRawType(), java.foreign.memory.Pointer.class);
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

    private void assertPointerType(ParameterizedType ptr, Class<?> pointee) {
        assertEquals(ptr.getRawType(), java.foreign.memory.Pointer.class);
        Type[] tas = ptr.getActualTypeArguments();
        assertEquals(tas.length, 1);
        assertEquals(tas[0], pointee);
    }

    private void verifyIncompleteArray(Class<?> incomplete) {
        assertNotNull(incomplete);
        Method f = findMethod(incomplete, "ptr$get");
        ParameterizedType pVoid = (ParameterizedType) f.getGenericReturnType();
        assertVoidPointer(pVoid, false);
        f = findMethod(incomplete, "ptr$set", java.foreign.memory.Pointer.class);
        // Setter for void* should be wildcard
        ParameterizedType pWildcard = (ParameterizedType) f.getGenericParameterTypes()[0];
        assertVoidPointer(pWildcard, true);

        f = findMethod(incomplete, "junk$get");
        ParameterizedType ppVoid = (ParameterizedType) f.getGenericReturnType();
        assertEquals(ppVoid.getActualTypeArguments()[0], pVoid);
        f = findMethod(incomplete, "junk$set", java.foreign.memory.Pointer.class);

        ppVoid = (ParameterizedType) f.getGenericParameterTypes()[0];
        assertTrue(ppVoid.getActualTypeArguments()[0] instanceof WildcardType);
        assertEquals(((WildcardType)ppVoid.getActualTypeArguments()[0]).getUpperBounds()[0], pWildcard);
    }

    private void verifyFunctionWithVoidPointer(Class<?> cls) {
        Method m = findMethod(cls, "FunctionWithVoidPointer",
                java.foreign.memory.Pointer.class,
                java.foreign.memory.Pointer.class);
        assertNotNull(m);

        ParameterizedType pVoid = (ParameterizedType) m.getGenericReturnType();
        assertVoidPointer(pVoid, false);

        Type[] args = m.getGenericParameterTypes();
        assertEquals(args.length, 2);

        ParameterizedType pWildcard = (ParameterizedType) args[0];
        assertVoidPointer(pWildcard, true);

        ParameterizedType ppVoid = (ParameterizedType) args[1];
        assertTrue(ppVoid.getActualTypeArguments()[0] instanceof WildcardType);
        assertEquals(((WildcardType)ppVoid.getActualTypeArguments()[0]).getUpperBounds()[0], pWildcard);
    }

    private void verifyFunctionPointer(Class<?> cls) {
        Method m = findMethod(cls, "fn",
                java.foreign.memory.Pointer.class,
                java.foreign.memory.Pointer.class);
        assertNotNull(m);

        ParameterizedType pVoid = (ParameterizedType) m.getGenericReturnType();
        assertVoidPointer(pVoid, false);

        Type[] args = m.getGenericParameterTypes();
        assertEquals(args.length, 2);

        ParameterizedType pWildcard = (ParameterizedType) args[0];
        assertVoidPointer(pWildcard, true);

        ParameterizedType ppVoid = (ParameterizedType) args[1];
        assertTrue(ppVoid.getActualTypeArguments()[0] instanceof WildcardType);
        assertEquals(((WildcardType)ppVoid.getActualTypeArguments()[0]).getUpperBounds()[0], pWildcard);
    }

    private void verifyUndefinedStructFunctions(Class<?> header) {
        final String POINTEE = "UndefinedStructForPointer";
        NativeHeader nh = header.getAnnotation(NativeHeader.class);
        Map<String, Descriptor> map = DescriptorParser.parseHeaderDeclarations(nh.declarations());

        Method m = findMethod(header, "getParent", java.foreign.memory.Pointer.class);
        assertNotNull(m);
        ParameterizedType pReturn = (ParameterizedType) m.getGenericReturnType();
        Class<?> undefined = findClass(header.getDeclaredClasses(), POINTEE);
        assertNotNull(undefined);
        assertPointerType(pReturn, undefined);
        Type[] args = m.getGenericParameterTypes();
        assertEquals(args.length, 1);
        ParameterizedType pArg = (ParameterizedType) args[0];
        assertPointerType(pArg, undefined);

        String expected = "(u64:$(" + POINTEE + "))u64:$(" + POINTEE + ")";
        Function fn = (Function) map.get("getParent");
        assertEquals(fn.toString(), expected);
        fn = (Function) map.get("getSibling");
        assertEquals(fn.toString(), expected);
        fn = (Function) map.get("getFirstChild");
        assertEquals(fn.toString(), expected);
    }

    private void verifyTypedefAnonymous(Class<?> cls) {
        NativeStruct ns = cls.getAnnotation(NativeStruct.class);

        Layout layout = Layout.of(ns.value());
        assertTrue(layout.isPartial());
        System.out.println("Unresolved layout = " + layout.toString());

        LayoutResolver resolver = LayoutResolver.get(cls);
        layout = resolver.resolve(layout);
        assertFalse(layout.isPartial());

        assertTrue(layout instanceof Group);
        Group g = (Group) layout;
        System.out.println("Resolved layout   = " + g.toString());
        assertEquals(g.bitsSize(), 8 * 4 * NativeTypes.INT.bytesSize());
    }

    private void verifyGetAnonymous(Class<?> header) {
        NativeHeader nh = header.getAnnotation(NativeHeader.class);
        Map<String, Descriptor> map = DescriptorParser.parseHeaderDeclarations(nh.declarations());
        Method m = findFirstMethod(header, "getAnonymous");
        assertNotNull(m);

        LayoutResolver resolver = LayoutResolver.get(header);
        Function fn = (Function) map.get("getAnonymous");
        assertTrue(fn.isPartial());
        System.out.println("Function unresolved  = " + fn.toString());
        fn = resolver.resolve(fn);
        System.out.println("Function resolved as = " + fn.toString());
        assertFalse(fn.isPartial());

        List<Layout> args = fn.argumentLayouts();
        Group g = (Group) args.get(0);
        assertFalse(g.isPartial());
        assertEquals(g.bitsSize(), NativeTypes.VOID.pointer().bytesSize() * 8);
    }

    private void verifyClass(Class<?> cls) {
        Class<?>[] clz = cls.getDeclaredClasses();
        assertEquals(clz.length, NumberOfInnerClasses);
        verifyPlain(findClass(clz, "Plain"));
        verifyTypedefNamedAsIs(findClass(clz, "TypedefNamedAsIs"));
        verifyExpectedAnnotations(clz);
        verifyFunctionWithVoidPointer(cls);
        verifyFunctionPointer(findClass(clz,"FI2")); //Todo: is this what jextract needs to emit?
        verifyIncompleteArray(findClass(clz, "IncompleteArray"));
        verifyTypedefAnonymous(findClass(clz, "TypedefAnonymous"));
        checkMethod(cls, "voidArguments", void.class);
        verifyUndefinedStructFunctions(cls);
        verifyGetAnonymous(cls);
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
        run("-C", "-x", "-C", "c++",
                "-o", clzPath.toString(),
                getInputFilePath("struct.h").toString()).checkSuccess();
        try(Loader loader = classLoader(clzPath)) {
            Class<?> cls = loader.loadClass("struct");
            verifyAsCpp(cls);
        } finally {
            deleteFile(clzPath);
        }
    }

    @Test
    public void testCMode() {
        Path clzPath = getOutputFilePath("StructTest.c.jar");
        run("-o", clzPath.toString(),
                getInputFilePath("struct.h").toString()).checkSuccess();
        try(Loader loader = classLoader(clzPath)) {
            Class<?> cls = loader.loadClass("struct");
            verifyAsC(cls);
        } finally {
            deleteFile(clzPath);
        }
    }
}
