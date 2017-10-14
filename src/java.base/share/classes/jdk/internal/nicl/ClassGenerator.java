/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_SUPER;

abstract class ClassGenerator implements ImplGenerator {
    private static final String DEBUG_DUMP_CLASSES_DIR_PROPERTY = "jdk.internal.nicl.ClassGenerator.DEBUG_DUMP_CLASSES_DIR";
    private static final String DEBUG_USE_ANON_CLASSES_PROPERTY = "jdk.internal.nicl.ClassGenerator.DEBUG_USE_ANON_CLASSES";

    private static final boolean DEBUG = Boolean.getBoolean("jdk.internal.nicl.ClassGenerator.DEBUG");

    private static final File DEBUG_DUMP_CLASSES_DIR;
    private static final boolean DEBUG_USE_ANON_CLASSES;

    static {
        String path = System.getProperty(DEBUG_DUMP_CLASSES_DIR_PROPERTY);
        if (path == null) {
            DEBUG_DUMP_CLASSES_DIR = null;
            DEBUG_USE_ANON_CLASSES = Boolean.valueOf(System.getProperty(DEBUG_USE_ANON_CLASSES_PROPERTY, "true"));
        } else {
            // Turn off the use of anonymous classes to enable debugging
            DEBUG_USE_ANON_CLASSES = false;
            DEBUG_DUMP_CLASSES_DIR = new File(path);
        }
    }

    private static final Unsafe U = Unsafe.getUnsafe();

    // the host class used for defining the new (anonymous) class
    private final Class<?> hostClass;

    // interfaces the generated class implements
    private final Class<?>[] interfaces;

    // name to use for the generated class
    final String implClassName;

    ClassGenerator(Class<?> hostClass, String implClassName, Class<?>[] interfaces) {
        this.hostClass = hostClass;
        this.implClassName = implClassName;
        this.interfaces = interfaces;
    }

    abstract void generate(ClassGeneratorContext ctxt);

    static class GeneratedClass {
        private final byte[] classFile;
        private final Object[] patches;
        private final FieldsBuilder fields;

        GeneratedClass(ClassGeneratorContext ctxt) {
            this.classFile = ctxt.getClassWriter().toByteArray();
            this.fields = ctxt.getFieldsBuilder();
            this.patches = ctxt.resolvePatches(classFile);
        }

        byte[] getClassFile() {
            return classFile;
        }

        Object[] getPatches() {
            return patches;
        }

        private void initClass(Class<?> c) {
            fields.initStaticFields(c);
        }
    }

    private void defineFields(ClassGeneratorContext ctxt) {
        ctxt.getFieldsBuilder().generateFields(ctxt.getClassWriter());
    }

    private GeneratedClass weaveClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        ClassGeneratorContext ctxt = new ClassGeneratorContext(cw, implClassName);

        if (DEBUG) {
            System.out.println("Generating header implementation class for " + implClassName + " using impl name " + implClassName);
        }

        String[] interfaceNames = (interfaces == null) ? null : Stream.of(interfaces).map(Type::getInternalName).toArray(String[]::new);
        cw.visit(52, ACC_PUBLIC | ACC_SUPER, implClassName, null, Type.getInternalName(Object.class), interfaceNames);

        generate(ctxt);
        defineFields(ctxt);

        cw.visitEnd();

        return new GeneratedClass(ctxt);
    }

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

    private Class<?> defineClass(GeneratedClass gc) {
        try {
            Object[] patches = gc.getPatches();

            if (DEBUG_USE_ANON_CLASSES) {
                // default (non-debug) case
                return U.defineAnonymousClass(hostClass, gc.getClassFile(), patches);
            } else {
                // Debug mode - define regular (non-anonymous) class and potentially dump it to disk

                if (patches != null) {
                    throw new RuntimeException("Constant pool patches can only be used with anonymous classes");
                }

                byte[] data = gc.getClassFile();

                Class<?> c = U.defineClass(implClassName, data, 0, data.length, hostClass.getClassLoader(), null);

                if (DEBUG_DUMP_CLASSES_DIR != null) {
                    debugWriteClassToFile(data);
                }

                return c;
            }
        } catch (VerifyError e) {
            debugPrintClass(gc.getClassFile());
            throw e;
        }
    }

    /**
     * Generate a class
     */
    @Override
    public Class<?> generate() {
        GeneratedClass gc = weaveClass();

        if (DEBUG) {
            debugPrintClass(gc.getClassFile());
        }

        Class<?> implCls = defineClass(gc);

        if (DEBUG) {
            System.out.println("Defined new class: " + implCls);
            for (Method m : implCls.getDeclaredMethods()) {
                System.out.println("  Method: " + m);
            }
        }

        gc.initClass(implCls);

        return implCls;
    }
}
