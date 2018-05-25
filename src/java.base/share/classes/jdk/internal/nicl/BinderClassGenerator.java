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
package jdk.internal.nicl;

import jdk.internal.misc.Unsafe;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nicl.layout.Layout;
import java.nicl.types.Pointer;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.stream.Stream;

import static jdk.internal.org.objectweb.asm.Opcodes.*;
import static jdk.internal.org.objectweb.asm.Opcodes.CHECKCAST;
import static jdk.internal.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static sun.security.action.GetPropertyAction.privilegedGetProperty;

abstract class BinderClassGenerator {

    private static final String DEBUG_DUMP_CLASSES_DIR_PROPERTY = "jdk.internal.nicl.ClassGenerator.DEBUG_DUMP_CLASSES_DIR";

    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.nicl.ClassGenerator.DEBUG"));

    private static final File DEBUG_DUMP_CLASSES_DIR;

    static {
        String path = privilegedGetProperty(DEBUG_DUMP_CLASSES_DIR_PROPERTY);
        if (path == null) {
            DEBUG_DUMP_CLASSES_DIR = null;
        } else {
            DEBUG_DUMP_CLASSES_DIR = new File(path);
        }
    }

    private static final Unsafe U = Unsafe.getUnsafe();

    // the host class used for defining the new (anonymous) class
    private final Class<?> hostClass;

    // interfaces the generated class implements
    final Class<?>[] interfaces;

    // name to use for the generated class
    final String implClassName;

    BinderClassGenerator(Class<?> hostClass, String implClassName, Class<?>[] interfaces) {
        this.hostClass = hostClass;
        this.implClassName = implClassName;
        this.interfaces = interfaces;
    }

    /**
     * Generate a class
     */
    public final Class<?> generate() {
        BinderClassWriter cw = new BinderClassWriter();

        if (DEBUG) {
            System.out.println("Generating header implementation class for " + implClassName + " using impl name " + implClassName);
        }

        String[] interfaceNames = (interfaces == null) ? null : Stream.of(interfaces).map(Type::getInternalName).toArray(String[]::new);
        cw.visit(52, ACC_PUBLIC | ACC_SUPER, implClassName, null, Type.getInternalName(Object.class), interfaceNames);

        generateConstructor(cw);

        generateMembers(cw);

        cw.visitEnd();

        byte[] classBytes = cw.toByteArray();
        if (DEBUG) {
            debugPrintClass(classBytes);
        }

        Class<?> implCls = defineClass(cw, classBytes);

        if (DEBUG) {
            System.out.println("Defined new class: " + implCls);
            for (Method m : implCls.getDeclaredMethods()) {
                System.out.println("  Method: " + m);
            }
        }

        return implCls;
    }

    //where
    private Class<?> defineClass(BinderClassWriter cw, byte[] classBytes) {
        try {
            Object[] patches = cw.resolvePatches(classBytes);
            Class<?> c = U.defineAnonymousClass(hostClass, classBytes, patches);
            if (DEBUG_DUMP_CLASSES_DIR != null) {
                debugWriteClassToFile(classBytes);
            }
            return c;
        } catch (VerifyError e) {
            debugPrintClass(classBytes);
            throw e;
        }
    }

    // code generation entry points (to be overridden by subclasses)

    protected abstract void generateConstructor(BinderClassWriter cw);

    protected abstract void generateMethodImplementation(BinderClassWriter cw, Method method);

    protected void generateMembers(BinderClassWriter cw) {
        for (Method m : interfaces[0].getMethods()) {
            try {
                LayoutResolver.instance().scanMethod(m);
                generateMethodImplementation(cw, m);
            } catch (Exception | Error e) {
                throw new RuntimeException("Failed to generate method " + m, e);
            }
        }
    }

    // shared code generation helpers

    int returnInsn(Class<?> cls) {
        if (cls.isPrimitive()) {
            switch (PrimitiveClassType.typeof(cls)) {
                case BOOLEAN: // fallthrough
                case BYTE: // fallthrough
                case SHORT: // fallthrough
                case CHAR: // fallthrough
                case INT:
                    return IRETURN;
                case LONG:
                    return LRETURN;
                case FLOAT:
                    return FRETURN;
                case DOUBLE:
                    return DRETURN;
                case VOID:
                    return RETURN;
                default:
                    throw new Error("Unsupported: " + cls.getName());
            }
        } else {
            return ARETURN;
        }
    }

    int loadInsn(Class<?> cls) {
        if (cls.isPrimitive()) {
            switch (PrimitiveClassType.typeof(cls)) {
                case BOOLEAN: // fallthrough
                case BYTE: // fallthrough
                case SHORT: // fallthrough
                case CHAR: // fallthrough
                case INT:
                    return ILOAD;
                case LONG:
                    return LLOAD;
                case FLOAT:
                    return FLOAD;
                case DOUBLE:
                    return DLOAD;
                default:
                    throw new Error("Unsupported: " + cls.getName());
            }
        } else {
            return ALOAD;
        }
    }

    // debug helpers

    private static void debugPrintClass(byte[] classFile) {
        ClassReader cr = new ClassReader(classFile);
        cr.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    }

    private void debugWriteClassToFile(byte[] classFile) {
        File file = new File(DEBUG_DUMP_CLASSES_DIR, implClassName + ".class");

        if (DEBUG) {
            System.err.println("Dumping class " + implClassName + " to " + file);
        }

        try {
            debugWriteDataToFile(classFile, file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write class " + implClassName + " to file " + file);
        }
    }

    private void debugWriteDataToFile(byte[] data, File file) {
        if (file.exists()) {
            file.delete();
        }
        if (file.exists()) {
            throw new RuntimeException("Failed to remove pre-existing file " + file);
        }

        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (!parent.exists()) {
            throw new RuntimeException("Failed to create " + parent);
        }
        if (!parent.isDirectory()) {
            throw new RuntimeException(parent + " is not a directory");
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write class " + implClassName + " to file " + file);
        }
    }

    enum AccessorKind {
        GET("get"),
        SET("set"),
        PTR("ptr");

        String annotationName;

        AccessorKind(String annotationName) {
            this.annotationName = annotationName;
        }

        java.lang.reflect.Type carrier(Method m) {
            switch (this) {
                case GET:
                    return m.getGenericReturnType();
                case SET:
                    return m.getGenericParameterTypes()[0];
                case PTR:
                    return ((ParameterizedType)m.getGenericReturnType()).getActualTypeArguments()[0];
                default:
                    throw new IllegalStateException("Unknown kind: " + this);
            }
        }

        MethodType getMethodType(Class<?> c) {
            switch (this) {
                case GET: return MethodType.methodType(c);
                case SET: return MethodType.methodType(void.class, c);
                case PTR: return MethodType.methodType(Pointer.class);
            }

            throw new IllegalArgumentException("Unhandled type: " + this);
        }

        static EnumMap<AccessorKind, String> from(Layout l) {
            EnumMap<AccessorKind, String> kinds = new EnumMap<>(AccessorKind.class);
            for (AccessorKind accessorKind : AccessorKind.values()) {
                String accessor = l.annotations().get(accessorKind.annotationName);
                if (accessor != null) {
                    kinds.put(accessorKind, accessor);
                }
            }
            return kinds;
        }
    }
}
