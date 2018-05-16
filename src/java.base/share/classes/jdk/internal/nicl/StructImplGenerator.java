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

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nicl.metadata.*;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.nicl.types.Reference;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

class StructImplGenerator extends BinderClassGenerator {

    // name of pointer field, only used for record types
    private static final String POINTER_FIELD_NAME = "ptr";

    // support method handles
    private static final MethodHandle BUILD_REF_MH;
    private static final MethodHandle PTR_COPY_TO_ARRAY_INT_MH;
    private static final MethodHandle PTR_COPY_TO_ARRAY_OBJECT_MH;
    private static final MethodHandle PTR_COPY_FROM_ARRAY_INT_MH;
    private static final MethodHandle PTR_COPY_FROM_ARRAY_OBJECT_MH;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            BUILD_REF_MH = lookup.findStatic(RuntimeSupport.class, "buildRef", MethodType.methodType(Reference.class, Pointer.class, long.class, LayoutType.class));
            PTR_COPY_TO_ARRAY_INT_MH = lookup.findStatic(RuntimeSupport.class, "copyToArray", MethodType.methodType(void.class, Pointer.class, long.class, int[].class, int.class));
            PTR_COPY_TO_ARRAY_OBJECT_MH = lookup.findStatic(RuntimeSupport.class, "copyToArray", MethodType.methodType(void.class, Pointer.class, long.class, Object[].class, int.class, Class.class));
            PTR_COPY_FROM_ARRAY_INT_MH = lookup.findStatic(RuntimeSupport.class, "copyFromArray", MethodType.methodType(void.class, int[].class, Pointer.class, long.class, int.class));
            PTR_COPY_FROM_ARRAY_OBJECT_MH = lookup.findStatic(RuntimeSupport.class, "copyFromArray", MethodType.methodType(void.class, Object[].class, Pointer.class, long.class, int.class, Class.class));
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    StructImplGenerator(Class<?> hostClass, String implClassName, Class<?> c) {
        super(hostClass, implClassName, new Class<?>[] { c });
    }

    @Override
    protected void generateMembers(BinderClassWriter cw) {
        generatePointerField(cw);
        generateConstructor(cw);
        generatePointerGetter(cw);
        generateRefHelper(cw);
        generateReferenceImpl(cw);
        super.generateMembers(cw);
    }

    private void generatePointerField(BinderClassWriter cw) {
        cw.visitField(ACC_PRIVATE | ACC_FINAL, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class), null, null);
    }

    private void generateConstructor(BinderClassWriter cw) {
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


    private void generatePointerGetter(BinderClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "ptr", Type.getMethodDescriptor(Type.getType(Pointer.class)), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateRefHelper(BinderClassWriter cw) {
        /*
         * private <T> Reference<T> ref(long offset, LayoutType<T> t) {
         *     MethodHandle buildRef = ldc <buildRef>
         *     return buildRef.invokeExact(p, offset, t);
         * }
         */
        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "ref", Type.getMethodDescriptor(Type.getType(Reference.class), Type.LONG_TYPE, Type.getType(LayoutType.class)), null, null);
        mv.visitCode();
        mv.visitLdcInsn(cw.makeConstantPoolPatch(BUILD_REF_MH));
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(MethodHandle.class));
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        mv.visitVarInsn(LLOAD, 1);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact", Type.getMethodDescriptor(Type.getType(Reference.class), Type.getType(Pointer.class), Type.LONG_TYPE, Type.getType(LayoutType.class)), false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateReferenceImpl(BinderClassWriter cw) {
        // Reference<T>.get()
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get", Type.getMethodDescriptor(Type.getType(Object.class)), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Reference<T>.set()
        // FIXME: Copy here?
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "set", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, implClassName);
            mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
            mv.visitFieldInsn(PUTFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    @Override
    protected void generateMethodImplementation(BinderClassWriter cw, Method method) {
        if (method.isAnnotationPresent(Offset.class)) {
            if (!method.isAnnotationPresent(C.class) || !method.isAnnotationPresent(NativeType.class)) {
                throw new IllegalArgumentException("Unexpectedly found an @Offset annotated method without a @NativeType annotation");
            }

            long off = method.getAnnotation(Offset.class).offset();
            if (off < 0 || off % 8 != 0) {
                throw new Error("NYI: Sub-byte offsets (" + off + ") in struct type: " + interfaces[0].getCanonicalName());
            }
            off = off / 8;

            generateFieldAccessors(cw, method, off);
        } else if (method.getDeclaringClass() == Reference.class) {
            // ignore - the corresponding methods are generated as part of setting up the record type
        } else {
            super.generateMethodImplementation(cw, method);
        }
    }

    private void generateFieldAccessors(BinderClassWriter cw, Method method, long offset) {
        Class<?> javaType = method.getReturnType();

        try {
            if (javaType.isArray()) {
                generateArrayFieldAccessors(cw, method, javaType, offset);
            } else {
                generateNormalFieldAccessors(cw, method, javaType, offset);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create accessors for " + method, e);
        }
    }

    private void generateArrayFieldAccessors(BinderClassWriter cw, Method method, Class<?> javaType, long offset) {
        Array ar = method.getAnnotation(Array.class);
        if (null == ar) {
            throw new IllegalStateException("Array return type should have Array annotation");
        }
        final long length = ar.length();
        if (length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Array size is too large");
        }

        generateArrayGetter(cw, method, javaType, offset, (int) length);
        generateArraySetter(cw, method, javaType, offset, (int) length);
    }

    private void generateArrayGetter(BinderClassWriter cw, Method method, Class<?> javaType, long offset, int length) {
        Class<?> componentType = javaType.getComponentType();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(Type.getType(method.getReturnType())), null, null);
        mv.visitCode();

        allocArray(mv, componentType, length);
        mv.visitVarInsn(ASTORE, 1);

        //load receiver MH
        mv.visitLdcInsn(cw.makeConstantPoolPatch(componentType.isPrimitive() ?
                        PTR_COPY_TO_ARRAY_INT_MH : PTR_COPY_TO_ARRAY_OBJECT_MH));
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(MethodHandle.class));


        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        mv.visitLdcInsn(offset);

        mv.visitVarInsn(ALOAD, 1);

        mv.visitLdcInsn(length);

        if (componentType.isPrimitive()) {
            switch (PrimitiveClassType.typeof(componentType)) {
                case INT:
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Pointer.class), Type.LONG_TYPE, Type.getType(int[].class), Type.INT_TYPE), false);
                    break;

                // FIXME: Add other primitives here
                default:
                    throw new UnsupportedOperationException("Unhandled component type: " + componentType);
            }
        } else {
            mv.visitLdcInsn(Type.getType(componentType));
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Pointer.class), Type.LONG_TYPE, Type.getType(Object[].class), Type.INT_TYPE, Type.getType(Class.class)), false);
        }

        mv.visitVarInsn(ALOAD, 1);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateArraySetter(BinderClassWriter cw, Method method, Class<?> javaType, long offset, int length) {
        Class<?> componentType = javaType.getComponentType();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName().replace("$get", "$set"), Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(method.getReturnType())), null, null);
        mv.visitCode();

        //load receiver MH
        mv.visitLdcInsn(cw.makeConstantPoolPatch(componentType.isPrimitive() ?
                        PTR_COPY_FROM_ARRAY_INT_MH : PTR_COPY_FROM_ARRAY_OBJECT_MH));
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(MethodHandle.class));

        mv.visitVarInsn(ALOAD, 1);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        mv.visitLdcInsn(offset);

        mv.visitLdcInsn(length);

        if (componentType.isPrimitive()) {
            switch (PrimitiveClassType.typeof(componentType)) {
                case INT:
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(int[].class), Type.getType(Pointer.class), Type.LONG_TYPE, Type.INT_TYPE), false);
                    break;

                // FIXME: Add other primitives here
                default:
                    throw new UnsupportedOperationException("Unhandled component type: " + componentType);
            }
        } else {
            mv.visitLdcInsn(Type.getType(componentType));
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object[].class), Type.getType(Pointer.class), Type.LONG_TYPE, Type.INT_TYPE, Type.getType(Class.class)), false);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void allocArray(MethodVisitor mv, Class<?> componentType, int length) {
        mv.visitLdcInsn(length);

        if (componentType.isPrimitive()) {
            switch (PrimitiveClassType.typeof(componentType)) {
                case INT:
                    mv.visitIntInsn(NEWARRAY, T_INT);
                    break;

                // FIXME: Add other primitives here
                default:
                    throw new IllegalArgumentException("Unhandled type: " + componentType);
            }
        } else {
            mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(componentType));
        }
    }

    private void generateNormalFieldAccessors(BinderClassWriter cw, Method method, Class<?> javaType, long offset) {
        LayoutType<?> lt = Util.createLayoutType(method.getGenericReturnType());

        // Getter
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(Type.getType(javaType)), null, null);
            mv.visitCode();
            generateGetter(cw, mv, offset, javaType, lt);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Setter
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName().replace("$get", "$set"),
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(javaType)), null, null);
            mv.visitCode();
            generateSetter(cw, mv, offset, javaType, lt);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        // Reference
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName().replace("$get", "$ref"),
                    Type.getMethodDescriptor(Type.getType(Reference.class)), null, null);
            mv.visitCode();
            pushRef(cw, mv, offset, lt);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private void generateGetter(BinderClassWriter cw, MethodVisitor mv, long off, Class<?> javaType, LayoutType<?> lt) {
        /*
         * return this.ref(<offset>, this.<layoutTypeField>).get();
         */
        pushRef(cw, mv, off, lt);
        mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Reference.class), "get", Type.getMethodDescriptor(Type.getType(Object.class)), true);
        if (javaType.isPrimitive()) {
            unbox(mv, javaType);
        } else {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaType));
        }
        mv.visitInsn(returnInsn(javaType));
    }

    private void generateSetter(BinderClassWriter cw, MethodVisitor mv, long off, Class<?> javaType, LayoutType<?> lt) {
        /*
         * this.ref(<offset>, this.<layoutTypeField>).set(<value>);
         */
        pushRef(cw, mv, off, lt);
        mv.visitVarInsn(loadInsn(javaType), 1);
        if (javaType.isPrimitive()) {
            box(mv, javaType);
        }
        mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Reference.class), "set", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), true);
        mv.visitInsn(RETURN);
    }

    private void pushRef(BinderClassWriter cw, MethodVisitor mv, long off, LayoutType<?> layoutType) {
        /*
         * ref(<offset>, this.<layoutTypeField>)
         */
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(off);
        mv.visitLdcInsn(cw.makeConstantPoolPatch(layoutType));
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(LayoutType.class));
        mv.visitMethodInsn(INVOKEVIRTUAL, implClassName, "ref", Type.getMethodDescriptor(Type.getType(Reference.class), Type.LONG_TYPE, Type.getType(LayoutType.class)), false);
    }
}
