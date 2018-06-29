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

import jdk.internal.nicl.LayoutPaths.LayoutPath;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nicl.layout.Group;
import java.nicl.layout.Layout;
import java.nicl.layout.Value;
import java.nicl.metadata.*;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

class StructImplGenerator extends BinderClassGenerator {

    // name of pointer field, only used for record types
    private static final String POINTER_FIELD_NAME = "ptr";

    private Group layout;

    StructImplGenerator(Class<?> hostClass, String implClassName, Class<?> c) {
        super(hostClass, implClassName, new Class<?>[] { c });
        layout = (Group)layoutResolver.resolve(Layout.of(c.getAnnotation(NativeStruct.class).value()));
    }

    @Override
    protected void generateMembers(BinderClassWriter cw) {
        layoutResolver.scanType(interfaces[0]);
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


    private void generatePointerGetter(BinderClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "ptr", Type.getMethodDescriptor(Type.getType(Pointer.class)), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    @Override
    protected void generateMethodImplementation(BinderClassWriter cw, Method method) {
        for (AccessorKind accessorKind : AccessorKind.values()) {
            LayoutPath path = findAccessor(method, accessorKind);
            if (path != null) {
                generateFieldAccessor(cw, method, accessorKind, path);
            }
        }
    }

    private void generateFieldAccessor(BinderClassWriter cw, Method method, AccessorKind accessorKind, LayoutPath path) {
        java.lang.reflect.Type javaType = accessorKind.carrier(method);
        Layout l = path.layout();
        LayoutType<?> lt = Util.makeType(javaType, l);
        Class<?> erasedType = Util.erasure(javaType);

        switch (accessorKind) {
            case GET:
            case SET: {
                //add accessor method
                MethodType accType = accessorKind.getMethodType(erasedType);
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(),
                        accType.toMethodDescriptorString(), null, null);
                mv.visitCode();
                MethodHandle accessor = accessorKind == AccessorKind.GET ?
                            RuntimeSupport.getterHandle(lt, path) :
                            RuntimeSupport.setterHandle(lt, path);

                mv.visitLdcInsn(cw.makeConstantPoolPatch(accessor));
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(MethodHandle.class));
                //load arguments
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));
                if (accessorKind == AccessorKind.SET) {
                    mv.visitVarInsn(loadInsn(erasedType), 1);
                }
                //call MH
                mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact",
                        accessor.type().toMethodDescriptorString(), false);
                //handle return
                if (accessorKind == AccessorKind.GET) {
                    mv.visitInsn(returnInsn(erasedType));
                } else {
                    mv.visitInsn(RETURN);
                }
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                break;
            }
            case PTR: {
                if (path.enclosing.layout() instanceof Value) {
                    throw new IllegalStateException("Cannot generate pointer accessor for bitfield!");
                }
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(),
                        Type.getMethodDescriptor(Type.getType(Pointer.class)), null, null);
                mv.visitCode();

                MethodHandle caster = RuntimeSupport.casterHandle(path, lt);

                mv.visitLdcInsn(cw.makeConstantPoolPatch(caster));
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(MethodHandle.class));

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, implClassName, POINTER_FIELD_NAME, Type.getDescriptor(Pointer.class));

                //call MH
                mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invokeExact",
                        caster.type().toMethodDescriptorString(), false);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                break;
            }
        }
    }

    private LayoutPath findAccessor(Method method, AccessorKind kind) {
        return LayoutPaths.lookup(layout, l -> method.getName().equals(AccessorKind.from(l).get(kind)))
                .findAny().orElse(null);
    }
}
