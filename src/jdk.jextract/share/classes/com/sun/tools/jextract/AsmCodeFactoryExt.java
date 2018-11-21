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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import com.sun.tools.jextract.tree.EnumTree;
import com.sun.tools.jextract.tree.FieldTree;
import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.MacroTree;
import com.sun.tools.jextract.tree.VarTree;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_FINAL;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_STATIC;
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

    AsmCodeFactoryExt(Context ctx, HeaderFile header) {
        super(ctx, header);
        logger.info(() -> "Instantiate StaticForwarderGenerator for " + header.path);
        this.headerClassNameDesc = "L" + headerClassName + ";";
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        this.cw.visit(V1_8, ACC_PUBLIC | ACC_FINAL, getClassName(),
            null, "java/lang/Object", null);
        staticsInitializer();
    }

    @Override
    public Boolean visitVar(VarTree varTree, JType jt) {
        if (super.visitVar(varTree, jt)) {
            String fieldName = varTree.name();
            assert !fieldName.isEmpty();

            emitStaticForwarder(fieldName + "$get",
                "()" + jt.getDescriptor(), "()" + jt.getSignature());

            emitStaticForwarder(fieldName + "$set",
                "(" + jt.getDescriptor() + ")V",
                "(" + JType.getPointerVoidAsWildcard(jt) + ")V");
            JType ptrType = new PointerType(jt);
            emitStaticForwarder(fieldName + "$ptr",
                "()" + ptrType.getDescriptor(), "()" + ptrType.getSignature());

            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean visitEnum(EnumTree enumTree, JType jt) {
        if (super.visitEnum(enumTree, jt)) {
            enumTree.constants().forEach(constant -> addEnumConstant(constant));
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
            String uniqueName = funcTree.name() + "." + fn.getDescriptor();
            logger.fine(() -> "Add method: " + fn.getSignature());
            emitStaticForwarder(funcTree.name(), fn.getDescriptor(), fn.getSignature());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean visitMacro(MacroTree macroTree, JType jt) {
        if (super.visitMacro(macroTree, jt)) {
            String name = macroTree.name();
            Object value = macroTree.value().get();
            logger.fine(() -> "Adding macro " + name);
            Class<?> macroType = Utils.unboxIfNeeded(value.getClass());
            String sig = Type.getType(macroType).getDescriptor();
            FieldVisitor fv = cw.visitField(ACC_PUBLIC | ACC_STATIC, name, sig, null, value);
            fv.visitEnd();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected synchronized void produce() {
        super.produce();
        types.put(getSimpleClassName(), getClassBytes());
    }

    // Internals only below this point
    // not fully qualified
    private String getSimpleClassName() {
        return headerFile.clsName + STATICS_CLASS_NAME_SUFFIX;
    }

    private String getClassName() {
        return headerClassName + STATICS_CLASS_NAME_SUFFIX;
    }

    // return the generated static forwarder class bytes
    private byte[] getClassBytes() {
        cw.visitEnd();
        return cw.toByteArray();
    }

    // map each C enum constant as a static final field of the static forwarder class
    private void addEnumConstant(FieldTree fieldTree) {
        assert (fieldTree.isEnumConstant());
        String name = fieldTree.name();
        String desc = headerFile.globalLookup(fieldTree.type()).getDescriptor();
        if (desc.length() != 1) {
            throw new AssertionError("expected single char descriptor: " + desc);
        }
        FieldVisitor fv = null;
        switch (desc.charAt(0)) {
            case 'J':
                long lvalue = fieldTree.enumConstant().get();
                fv = cw.visitField(ACC_PUBLIC | ACC_STATIC, name, desc, null, lvalue);
                break;
            case 'I':
                int ivalue = fieldTree.enumConstant().get().intValue();
                fv = cw.visitField(ACC_PUBLIC | ACC_STATIC, name, desc, null, ivalue);
                break;
            default:
                throw new AssertionError("should not reach here");
        }
        fv.visitEnd();
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

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    // emit static forwarder method for a specific library interface method
    private void emitStaticForwarder(String name, String desc, String signature) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC,
            name, desc, signature, null);
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
