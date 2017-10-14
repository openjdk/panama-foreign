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

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

// Generate a method which captures all of its incoming arguments and calls (invokes) a method handle
class GenericMethodImplGenerator extends MethodGenerator {
    private final MethodType methodType;
    private final MethodHandle targetMethodHandle;
    private final boolean isVarArgs;
    private final boolean prefixThis;

    GenericMethodImplGenerator(ClassGeneratorContext ctxt, String methodName, MethodType methodType, boolean isVarArgs, MethodHandle targetMethodHandle, boolean prefixThis) {
        super(ctxt, methodName);

        this.methodType = methodType;
        this.isVarArgs = isVarArgs;
        this.targetMethodHandle = targetMethodHandle;
        this.prefixThis = prefixThis;
    }

    GenericMethodImplGenerator(ClassGeneratorContext ctxt, String methodName, MethodType methodType, boolean isVarArgs, MethodHandle targetMethodHandle) {
        this(ctxt, methodName, methodType, isVarArgs, targetMethodHandle, false);
    }

    @Override
    public void generate() {
        ClassWriter cw = ctxt.getClassWriter();

        String descriptor = methodType.toMethodDescriptorString();

        int flags = ACC_PUBLIC;
        if (isVarArgs) {
            flags |= ACC_VARARGS;
        }

        MethodVisitor mv = cw.visitMethod(flags, methodName, descriptor, null, null);

        mv.visitCode();

        generateMethodBody(mv, targetMethodHandle.type(), createMethodHandleField(targetMethodHandle));

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private String createMethodHandleField(MethodHandle mh) {
        FieldGenerator fg = new FieldGenerator(methodName + "$targetMH$" + ctxt.getFieldsBuilder().generateUniqueId(), true, mh) {
            @Override
            public void generate(ClassWriter cw) {
                cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getName(), Type.getDescriptor(MethodHandle.class), null, null);
            }
        };
        ctxt.getFieldsBuilder().add(fg);

        return fg.getName();
    }

    private void copyArguments(MethodVisitor mv) {
        if (prefixThis) {
            mv.visitVarInsn(ALOAD, 0);
        }

        for (int i = 0, curSlot = 1; i < methodType.parameterCount(); i++) {
            Class<?> c = methodType.parameterType(i);
            mv.visitVarInsn(Util.loadInsn(c), curSlot);
            curSlot += Util.getSlotsForType(c);
        }
    }

    private void generateMethodBody(MethodVisitor mv, MethodType mt, String methodHandleFieldName) {
        // push the method handle
        mv.visitFieldInsn(GETSTATIC, ctxt.getClassName(), methodHandleFieldName, Type.getDescriptor(MethodHandle.class));

        copyArguments(mv);

        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invoke", mt.toMethodDescriptorString(), false);

        mv.visitInsn(Util.returnInsn(methodType.returnType()));
    }
}
