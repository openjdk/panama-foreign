/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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

class HeaderImplGenerator extends BinderClassGenerator {

    // lookup helper to use for looking up symbols
    private final SymbolLookup lookup;

    HeaderImplGenerator(Class<?> hostClass, String implClassName, Class<?> c, SymbolLookup lookup) {
        super(hostClass, implClassName, new Class<?>[] { c });
        this.lookup = lookup;
    }

    enum AccessorMethodType {
        get, set, ref;

        MethodType getMethodType(Class<?> c) {
            switch (this) {
                case get: return MethodType.methodType(c);
                case set: return MethodType.methodType(void.class, c);
                case ref: return MethodType.methodType(Reference.class);
            }

            throw new IllegalArgumentException("Unhandled type: " + this);
        }
    }

    @Override
    protected void generateMethodImplementation(BinderClassWriter cw, Method method) {
        if (method.isAnnotationPresent(NativeType.class) && !Util.isFunction(method)) {
            generateGlobalVariableMethods(cw, method, getSymbolName(method, getGetterBaseName(method)));
        } else if (method.isAnnotationPresent(C.class) && method.isAnnotationPresent(CallingConvention.class)) {
            MethodType methodType = Util.methodTypeFor(method);
            NativeInvoker invoker;
            try {
                invoker = new NativeInvoker(methodType, method.isVarArgs(), lookup, getSymbolName(method), method.toString(), method.getGenericReturnType());
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            addMethodFromHandle(cw, method.getName(), methodType, method.isVarArgs(), invoker.getBoundMethodHandle());
        } else {
            super.generateMethodImplementation(cw, method);
        }
    }

    private void generateGlobalVariableMethods(BinderClassWriter cw, Method method, String symbolName) {
        Class<?> c = method.getReturnType();
        java.lang.reflect.Type type = method.getGenericReturnType();
        LayoutType<?> lt = Util.createLayoutType(type);

        int dollarIndex = method.getName().indexOf("$");
        String methodBaseName = method.getName().substring(0, dollarIndex);
        Pointer<?> p;

        try {
            p = lookup.lookup(symbolName).getAddress().cast(lt);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }

        for (AccessorMethodType t : AccessorMethodType.values()) {
            String methodName = methodBaseName + "$" + t.name();
            MethodHandle target;
            try {
                switch (t) {
                    case get:
                        target = MethodHandles.publicLookup().findVirtual(Reference.class, "get", MethodType.methodType(Object.class));
                        target = target.bindTo(p.lvalue()).asType(MethodType.methodType(c));
                        break;

                    case set:
                        target = MethodHandles.publicLookup().findVirtual(Reference.class, "set", MethodType.methodType(void.class, Object.class));
                        target = target.bindTo(p.lvalue()).asType(MethodType.methodType(void.class, c));
                        break;

                    case ref:
                        target = MethodHandles.constant(Reference.class, p.lvalue());
                        break;

                    default:
                        throw new InternalError("Unexpected access method type: " + t);
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            addMethodFromHandle(cw, methodName, t.getMethodType(c), false, target);
        }
    }

    private String getGetterBaseName(Method method) {
        String name = method.getName();

        if (!name.endsWith("$get")) {
            throw new IllegalArgumentException("Unexpected method name " + method.getName());
        }

        return name.substring(0, name.lastIndexOf("$"));
    }

    private String getSymbolName(Method method, String defaultName) {
        String name = method.getAnnotation(NativeType.class).name();
        if (NativeType.NO_NAME.equals(name)) {
            // FIXME: Make this an error (require name to be set)?
            return defaultName;
        } else {
            return name;
        }
    }

    private String getSymbolName(Method method) {
        return getSymbolName(method, method.getName());
    }

    // code generation helpers

    private void addMethodFromHandle(BinderClassWriter cw, String methodName, MethodType methodType, boolean isVarArgs, MethodHandle targetMethodHandle) {
        String descriptor = methodType.toMethodDescriptorString();

        int flags = ACC_PUBLIC;
        if (isVarArgs) {
            flags |= ACC_VARARGS;
        }

        MethodVisitor mv = cw.visitMethod(flags, methodName, descriptor, null, null);

        mv.visitCode();

        // push the method handle
        mv.visitLdcInsn(cw.makeConstantPoolPatch(targetMethodHandle));
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(MethodHandle.class));

        //copy arguments
        for (int i = 0, curSlot = 1; i < methodType.parameterCount(); i++) {
            Class<?> c = methodType.parameterType(i);
            mv.visitVarInsn(loadInsn(c), curSlot);
            curSlot += getSlotsForType(c);
        }

        //call MH
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invoke",
                targetMethodHandle.type().toMethodDescriptorString(), false);

        mv.visitInsn(returnInsn(methodType.returnType()));

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    //where
    private static int getSlotsForType(Class<?> c) {
        if (c == long.class || c == double.class) {
            return 2;
        }
        return 1;
    }
}
