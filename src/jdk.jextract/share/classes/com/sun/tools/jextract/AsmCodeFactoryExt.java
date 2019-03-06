/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.jextract;

import java.foreign.Libraries;
import java.foreign.Scope;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

import com.sun.tools.jextract.parser.MacroParser;
import com.sun.tools.jextract.tree.Tree;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import com.sun.tools.jextract.tree.EnumTree;
import com.sun.tools.jextract.tree.FieldTree;
import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.MacroTree;
import com.sun.tools.jextract.tree.VarTree;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_FINAL;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_STATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ARETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_VARARGS;
import static jdk.internal.org.objectweb.asm.Opcodes.CHECKCAST;
import static jdk.internal.org.objectweb.asm.Opcodes.GETSTATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ILOAD;
import static jdk.internal.org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static jdk.internal.org.objectweb.asm.Opcodes.INVOKESTATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.IRETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.PUTSTATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.RETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.V1_8;

/**
 * This extended factory generates a class with only static methods and fields. A native
 * library interface instance (of the given header file) is kept as a static private field.
 * One static method is generated for every library interface method. Enum and macro constants
 * are mapped to static final fields. By importing the "static forwarder" class, the user code
 * looks more or less like C code. Libraries.bind and header interface usage is hidden.
 */
final class AsmCodeFactoryExt extends AsmCodeFactory {
    private final String headerClassNameDesc;
    private final ClassWriter cw;
    // suffix for static forwarder class name
    private static final String STATICS_CLASS_NAME_SUFFIX = "_h";
    // field name for the header interface instance.
    private static final String STATICS_LIBRARY_FIELD_NAME = "_theLibrary";

    private final List<Consumer<MethodVisitor>> constantInitializers = new ArrayList<>();
    private final List<EnumFactory> enumFactories = new ArrayList<>();

    AsmCodeFactoryExt(Context ctx, HeaderFile header) {
        super(ctx, header);
        log.print(Level.INFO, () -> "Instantiate StaticForwarderGenerator for " + header.path);
        this.headerClassNameDesc = "L" + headerClassName + ";";
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        this.cw.visit(V1_8, ACC_PUBLIC | ACC_FINAL, getClassName(),
            null, "java/lang/Object", null);
        scopeAccessor();
    }

    private class EnumFactory {
        private final EnumTree enumTree;
        private final String enumClassName;

        EnumFactory(EnumTree enumTree) {
            log.print(Level.INFO, () -> "Instantiate EnumFactory for " + enumTree.name());
            this.enumTree = enumTree;
            this.enumClassName = AsmCodeFactoryExt.this.getClassName() + "$" + enumTree.name();

        }

        String getEnumName() {
            return enumTree.name();
        }

        String getClassName() {
            return enumClassName;
        }

        byte[] getClassBytes() {
            return generate();
        }

        private byte[] generate() {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cw.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE, enumClassName,
                null, "java/lang/Object", null);
            cw.visitInnerClass(enumClassName, AsmCodeFactoryExt.this.getClassName(), enumTree.name(), ACC_PUBLIC | ACC_FINAL);
            enumTree.constants().forEach(constant -> {
                String name = constant.name();
                JType type = headerFile.dictionary().lookup(constant.type());
                Object constantValue = makeConstantValue(type, constant.enumConstant().get());
                FieldVisitor fv = cw.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, name, type.getDescriptor(),
                        type.getSignature(false), constantValue);
                fv.visitEnd();
            });

            cw.visitEnd();
            return cw.toByteArray();
        }
    }

    @Override
    public Boolean visitVar(VarTree varTree, JType jt) {
        if (super.visitVar(varTree, jt)) {
            String fieldName = varTree.name();
            assert !fieldName.isEmpty();

            emitStaticForwarder(fieldName + "$get",
                "()" + jt.getDescriptor(), "()" + jt.getSignature(false), false);
            jt.visitInner(cw);

            emitStaticForwarder(fieldName + "$set",
                "(" + jt.getDescriptor() + ")V",
                "(" + jt.getSignature(true) + ")V", false);
            JType ptrType = JType.GenericType.ofPointer(jt);
            emitStaticForwarder(fieldName + "$ptr",
                "()" + ptrType.getDescriptor(), "()" + ptrType.getSignature(false), false);
            ptrType.visitInner(cw);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean visitEnum(EnumTree enumTree, JType jt) {
        if (super.visitEnum(enumTree, jt)) {
            if (enumTree.name().isEmpty()) {
                enumTree.constants().forEach(constant -> addConstant(constant.name(),
                    headerFile.dictionary().lookup(constant.type()),
                    constant.enumConstant().get()));
            } else {
                EnumFactory ef = new EnumFactory(enumTree);
                enumFactories.add(ef);
                cw.visitInnerClass(ef.getClassName(), getClassName(), ef.getEnumName(),
                    ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean visitFunction(FunctionTree funcTree, JType jt) {
        if (super.visitFunction(funcTree, jt)) {
            assert (jt instanceof JType.Function);
            JType.Function fn = (JType.Function)jt;
            log.print(Level.FINE, () -> "Add method: " + fn.getSignature(false));
            emitStaticForwarder(funcTree.name(), fn.getDescriptor(), fn.getSignature(false), fn.isVarArgs);
            fn.visitInner(cw);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean visitMacro(MacroTree macroTree, JType jt) {
        if (super.visitMacro(macroTree, jt)) {
            String name = macroTree.name();
            MacroParser.Macro macro = macroTree.macro().get();
            log.print(Level.FINE, () -> "Adding macro " + name);
            addConstant(name, macro.type(), macro.value());
            return true;
        } else {
            return false;
        }
    }

    private void addConstant(String name, JType type, Object value) {
        Object constantValue = makeConstantValue(type, value);
        FieldVisitor fv = cw.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, name, type.getDescriptor(),
                type.getSignature(false), constantValue);
        fv.visitEnd();
        if (constantValue == null) {
            constantInitializers.add(mv -> {
                // load library interface (static) field
                String desc = type.getDescriptor();
                mv.visitFieldInsn(GETSTATIC, getClassName(),
                        STATICS_LIBRARY_FIELD_NAME, headerClassNameDesc);
                mv.visitMethodInsn(INVOKEINTERFACE, headerClassName, name, "()" + desc, true);
                mv.visitFieldInsn(PUTSTATIC, getClassName(), name, desc);
            });
        }
    }

    private Object makeConstantValue(JType type, Object value) {
        switch (type.getDescriptor()) {
            case "Z":
                return ((long)value) != 0;
            case "C":
                return (char)(long)value;
            case "B":
                return (byte)(long)value;
            case "S":
                return (short)(long)value;
            case "I":
                return (int)(long)value;
            case "F":
                return (float)(double)value;
            case "J": case "D":
                return value;
            default:
                return null;
        }
    }

    @Override
    public Map<String, byte[]> generateNativeHeader(List<Tree> decls) {
        Map<String, byte[]> results = new HashMap<>();
        results.putAll(super.generateNativeHeader(decls));
        staticsInitializer();
        results.put(getClassName(), getClassBytes());
        for (EnumFactory ef : enumFactories) {
            results.put(ef.getClassName(), ef.getClassBytes());
        }
        return Collections.unmodifiableMap(results);
    }

    // Internals only below this point

    private String getClassName() {
        return headerClassName + STATICS_CLASS_NAME_SUFFIX;
    }

    // return the generated static forwarder class bytes
    private byte[] getClassBytes() {
        cw.visitEnd();
        return cw.toByteArray();
    }

    // emit library interface static field and <clinit> initializer for that field
    private void staticsInitializer() {
        // library interface field
        FieldVisitor fv = cw.visitField(ACC_PRIVATE|ACC_STATIC|ACC_FINAL,
            STATICS_LIBRARY_FIELD_NAME, headerClassNameDesc, null, null);
        fv.visitEnd();

        // <clinit> to bind library interface field
        MethodVisitor mv = cw.visitMethod(ACC_STATIC,
            "<clinit>", "()V", null, null);
        mv.visitCode();

        // MethodHandles.lookup()
        Method lookupMethod = null;
        try {
            lookupMethod = MethodHandles.class.getMethod("lookup");
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException(nsme);
        }
        mv.visitMethodInsn(INVOKESTATIC,
            Type.getInternalName(MethodHandles.class), "lookup",
            Type.getMethodDescriptor(lookupMethod), false);

        // ldc library-interface-class
        mv.visitLdcInsn(Type.getObjectType(headerClassName));

        // Libraries.bind(lookup, class);
        Method bindMethod = null;
        try {
            bindMethod = Libraries.class.getMethod("bind", Lookup.class, Class.class);
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException(nsme);
        }
        mv.visitMethodInsn(INVOKESTATIC,
            Type.getInternalName(Libraries.class), "bind",
            Type.getMethodDescriptor(bindMethod), false);

        // store it in library interface field
        mv.visitTypeInsn(CHECKCAST, headerClassName);
        mv.visitFieldInsn(PUTSTATIC, getClassName(),
            STATICS_LIBRARY_FIELD_NAME, headerClassNameDesc);

        constantInitializers.forEach(init -> init.accept(mv));

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void scopeAccessor() {
        String scopeAccessorDesc = MethodType.methodType(Scope.class).descriptorString();
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "scope", scopeAccessorDesc, null, null);
        mv.visitCode();

        // load library interface (static) field
        mv.visitFieldInsn(GETSTATIC, getClassName(),
            STATICS_LIBRARY_FIELD_NAME, headerClassNameDesc);

        String libraryScopeDesc = MethodType.methodType(Scope.class, Object.class).descriptorString();
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Libraries.class), "libraryScope", libraryScopeDesc, false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1,1);
        mv.visitEnd();
    }

    // emit static forwarder method for a specific library interface method
    private void emitStaticForwarder(String name, String desc, String signature, boolean isVarArgs) {
        int accessFlags = ACC_PUBLIC | ACC_STATIC;
        if (isVarArgs) {
            accessFlags |= ACC_VARARGS;
        }

        MethodVisitor mv = cw.visitMethod(accessFlags, name, desc, signature, null);
        mv.visitCode();

        // load library interface (static) field
        mv.visitFieldInsn(GETSTATIC, getClassName(),
            STATICS_LIBRARY_FIELD_NAME, headerClassNameDesc);

        // forward the call to the interface
        Type[] argTypes = Type.getArgumentTypes(desc);
        Type retType = Type.getReturnType(desc);

        int loadIdx = 0;
        for (int i = 0; i < argTypes.length; i++) {
            mv.visitVarInsn(argTypes[i].getOpcode(ILOAD), loadIdx);
            loadIdx += argTypes[i].getSize();
        }
        mv.visitMethodInsn(INVOKEINTERFACE, headerClassName, name, desc, true);
        mv.visitInsn(retType.getOpcode(IRETURN));

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
