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
package jdk.internal.foreign;

import java.foreign.NativeMethodType;
import java.foreign.layout.Function;
import java.foreign.memory.Pointer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import jdk.internal.foreign.abi.SystemABI;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_FINAL;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ALOAD;
import static jdk.internal.org.objectweb.asm.Opcodes.ARETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.CHECKCAST;
import static jdk.internal.org.objectweb.asm.Opcodes.GETFIELD;
import static jdk.internal.org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static jdk.internal.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static jdk.internal.org.objectweb.asm.Opcodes.POP2;
import static jdk.internal.org.objectweb.asm.Opcodes.PUTFIELD;
import static jdk.internal.org.objectweb.asm.Opcodes.RETURN;


class CallbackImplGenerator extends BinderClassGenerator {

    // name of pointer field
    private static final String POINTER_FIELD_NAME = "ptr";
    private static final String MH_FIELD_NAME = "mh";
    private static final MethodHandle PTR_ADDR;
    private static final MethodHandle INVOKER_FACTORY;
    private static final String STABLE_SIG = "Ljdk/internal/vm/annotation/Stable;";

    static {
        try {
            PTR_ADDR = MethodHandles.lookup().findVirtual(Pointer.class, "addr",
                           MethodType.methodType(long.class));
            INVOKER_FACTORY = MethodHandles.lookup().findStatic(CallbackImplGenerator.class, "makeInvoker",
                    MethodType.methodType(MethodHandle.class, Pointer.class, Function.class, Method.class));
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    CallbackImplGenerator(Class<?> hostClass, String implClassName, Class<?> c) {
        super(hostClass, implClassName, new Class<?>[] { c });
    }

    @Override
    protected void generateMembers(BinderClassWriter cw) {
        AnnotationVisitor av = cw.visitAnnotation(Type.getDescriptor(SyntheticCallback.class), true);
        av.visitEnd();
        generatePointerField(cw);
        generateMethodHandleField(cw);
        generatePointerGetter(cw);
        super.generateMembers(cw);
    }

    private void generatePointerField(BinderClassWriter cw) {
        cw.visitField(ACC_PRIVATE | ACC_FINAL, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class), null, null);
    }

    private void generateMethodHandleField(BinderClassWriter cw) {
        FieldVisitor fv = cw.visitField(ACC_PRIVATE | ACC_FINAL, MH_FIELD_NAME, Type.getDescriptor(MethodHandle.class), null, null);
        fv.visitAnnotation(STABLE_SIG, true);
        fv.visitEnd();
    }

    @Override
    protected void generateConstructor(BinderClassWriter cw) {
        /*
         * <init>(Pointer p) {
         *     super();
         *     this.p = p;
         * }
         */
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Pointer.class)), null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));

        mv.visitVarInsn(ALOAD, 0);

        Method method = Util.findFunctionalInterfaceMethod(interfaces[0]);
        Function function = Util.functionof(interfaces[0]);

        MethodHandle factory = MethodHandles.insertArguments(INVOKER_FACTORY, 1, function, method);
        mv.visitLdcInsn(cw.makeConstantPoolPatch(factory));
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(MethodHandle.class));

        mv.visitVarInsn(ALOAD, 1);

        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact",
                factory.type().toMethodDescriptorString(), false);

        mv.visitFieldInsn(PUTFIELD, implClassName, MH_FIELD_NAME, Type.getDescriptor(MethodHandle.class));

        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    @Override
    protected void generateMethodImplementation(BinderClassWriter cw, Method method) {
        MethodType methodType = Util.methodTypeFor(method);
        addMethodFromHandle(cw, method.getName(), methodType, method.isVarArgs(), mv -> loadReceiver(cw, mv));
    }

    private void generatePointerGetter(BinderClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, POINTER_FIELD_NAME, Type.getMethodDescriptor(Type.getType(Pointer.class)), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void loadReceiver(BinderClassWriter cw, MethodVisitor mv) {
        //1. check that pointer is still alive

        //load MH
        mv.visitLdcInsn(cw.makeConstantPoolPatch(PTR_ADDR));
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(MethodHandle.class));
        //load ptr arg
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        //call MH
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact",
                PTR_ADDR.type().toMethodDescriptorString(), false);
        mv.visitInsn(POP2);

        //2. load invoker mh
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, MH_FIELD_NAME, Type.getDescriptor(MethodHandle.class));
    }

    /* Method handle code helpers */
    static MethodHandle makeInvoker(Pointer<?> ptr, Function function, Method meth) {
        // Only native function pointer will get here
        // Java upcall stub will be detected by CallbackImpl
        return SystemABI.getInstance().downcallHandle(new SimpleSymbol(ptr, meth.getName()),
                NativeMethodType.of(function, meth));
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SyntheticCallback { }
}
