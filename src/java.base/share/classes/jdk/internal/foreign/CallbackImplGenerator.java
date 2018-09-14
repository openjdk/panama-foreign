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

import jdk.internal.foreign.memory.BoundedPointer;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.foreign.Scope;
import java.foreign.layout.Function;
import java.foreign.memory.Pointer;

import static jdk.internal.org.objectweb.asm.Opcodes.*;


class CallbackImplGenerator extends BinderClassGenerator {

    // name of pointer field
    private static final String POINTER_FIELD_NAME = "ptr";
    private static final MethodHandle PTR_CHECKER;
    private static final MethodHandle ADR_IS_STUB;
    private static final MethodHandle ADR_GET_CALLBACK_OBJ;

    static {
        try {
            PTR_CHECKER = MethodHandles.lookup().findStatic(CallbackImplGenerator.class, "checkPointer",
                    MethodType.methodType(long.class, Pointer.class));
            ADR_IS_STUB = MethodHandles.lookup().findStatic(CallbackImplGenerator.class, "isNativeStub",
                    MethodType.methodType(boolean.class, long.class));
            ADR_GET_CALLBACK_OBJ = MethodHandles.lookup().findStatic(CallbackImplGenerator.class, "getCallbackObject",
                    MethodType.methodType(Object.class, long.class));
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
        generatePointerGetter(cw);
        super.generateMembers(cw);
    }

    private void generatePointerField(BinderClassWriter cw) {
        cw.visitField(ACC_PRIVATE | ACC_FINAL, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class), null, null);
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

        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    @Override
    protected void generateMethodImplementation(BinderClassWriter cw, Method method) {
        generateFunctionMethod(cw, method);
    }

    void generateFunctionMethod(BinderClassWriter cw, Method method) {
        Function function = Util.functionof(interfaces[0]);
        try {
            MethodHandle javaInvoker = MethodHandles.publicLookup().unreflect(method);
            MethodHandle pointerFilter = ADR_GET_CALLBACK_OBJ.asType(ADR_GET_CALLBACK_OBJ.type().changeReturnType(interfaces[0]));
            NativeInvoker nativeInvoker = new NativeInvoker(layoutResolver.resolve(function), method);
            MethodHandle callback_invoker = MethodHandles.guardWithTest(ADR_IS_STUB,
                    MethodHandles.filterArguments(javaInvoker, 0, pointerFilter),
                    nativeInvoker.getBoundMethodHandle());
            addMethodFromHandle(cw, method.getName(), nativeInvoker.type(), method.isVarArgs(), callback_invoker, mv -> getPtrAddress(cw, mv));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
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

    void getPtrAddress(BinderClassWriter cw, MethodVisitor mv) {
        //load MH
        mv.visitLdcInsn(cw.makeConstantPoolPatch(PTR_CHECKER));
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(MethodHandle.class));
        //load ptr arg
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        //call MH
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact",
                PTR_CHECKER.type().toMethodDescriptorString(), false);
    }

    /* Method handle code helpers */

    static boolean isNativeStub(long addr) {
        try {
            return NativeInvoker.getUpcallHandler(addr) != null;
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    static Object getCallbackObject(long addr) {
        try {
            return NativeInvoker.getUpcallHandler(addr).getCallbackObject();
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    static long checkPointer(Pointer<?> ptr) throws Throwable {
        Scope s = ptr.scope();
        s.checkAlive();
        return ptr.addr();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SyntheticCallback { }
}
