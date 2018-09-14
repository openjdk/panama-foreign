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
package jdk.internal.foreign;

import jdk.internal.foreign.memory.BoundedMemoryRegion;
import jdk.internal.foreign.memory.BoundedPointer;
import jdk.internal.foreign.memory.DescriptorParser;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import java.foreign.Scope;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.annotations.*;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.util.HashMap;
import java.util.Map;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

class HeaderImplGenerator extends BinderClassGenerator {

    // lookup helper to use for looking up symbols
    private final SymbolLookup lookup;

    // dictionary from method name to member info
    final Map<String, MemberInfo<?>> nameToInfo = new HashMap<>();

    // global scope for this library
    private final Scope libScope;

    HeaderImplGenerator(Class<?> hostClass, String implClassName, Class<?> c, SymbolLookup lookup, Scope libScope) {
        super(hostClass, implClassName, new Class<?>[] { c });
        this.lookup = lookup;
        this.libScope = libScope;
    }

    abstract class MemberInfo<D> {
        String symbolName;
        D descriptor;

        MemberInfo(String symbolName, D descriptor) {
            this.symbolName = symbolName;
            this.descriptor = descriptor;
        }
    }

    class GlobalVarInfo extends MemberInfo<Layout> {

        AccessorKind accessorKind;

        GlobalVarInfo(String symbolName, Layout layout, AccessorKind accessorKind) {
            super(symbolName, layout);
            this.accessorKind = accessorKind;
        }
    }

    class FunctionInfo extends MemberInfo<Function> {
        public FunctionInfo(String symbolName, Function descriptor) {
            super(symbolName, descriptor);
        }
    }

    @Override
    protected void generateMembers(BinderClassWriter cw) {
        Class<?> headerClass = interfaces[0];
        String declarations = headerClass.getAnnotation(NativeHeader.class).declarations();
        for (Map.Entry<String, Object> declEntry : DescriptorParser.parseHeaderDeclarations(declarations).entrySet()) {
            if (declEntry.getValue() instanceof Layout) {
                Layout l = (Layout)declEntry.getValue();
                for (Map.Entry<AccessorKind, String> accessorEntry : AccessorKind.from(l).entrySet()) {
                    nameToInfo.put(accessorEntry.getValue(), new GlobalVarInfo(declEntry.getKey(), l, accessorEntry.getKey()));
                }
            } else {
                Function f = (Function)declEntry.getValue();
                nameToInfo.put(declEntry.getKey(), new FunctionInfo(declEntry.getKey(), f));
            }
        }
        super.generateMembers(cw);
    }

    @Override
    protected void generateConstructor(BinderClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1,1);
        mv.visitEnd();
    }

    @Override
    protected void generateMethodImplementation(BinderClassWriter cw, Method method) {
        MemberInfo<?> memberInfo = nameToInfo.get(method.getName());
        if (memberInfo instanceof FunctionInfo) {
            generateFunctionMethod(cw, method, (FunctionInfo)memberInfo);
        } else if (memberInfo instanceof GlobalVarInfo) {
            generateGlobalVariableMethod(cw, method, (GlobalVarInfo)memberInfo);
        }
    }

    void generateFunctionMethod(BinderClassWriter cw, Method method, FunctionInfo info) {
        Function function = info.descriptor;
        try {
            NativeInvoker invoker = new NativeInvoker(layoutResolver.resolve(function), method);
            long addr = lookup.lookup(info.symbolName).getAddress().addr();
            addMethodFromHandle(cw, method.getName(), invoker.type(), method.isVarArgs(), invoker.getBoundMethodHandle(),
                mv -> mv.visitLdcInsn(addr));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void generateGlobalVariableMethod(BinderClassWriter cw, Method method, GlobalVarInfo info) {
        java.lang.reflect.Type type = info.accessorKind.carrier(method);
        Class<?> c = Util.erasure(type);
        Layout l = info.descriptor;
        LayoutType<?> lt = Util.makeType(type, l);

        String methodName = method.getName();
        Pointer<?> p;

        try {
            p = BoundedPointer.createNativeVoidPointer(libScope,
                    lookup.lookup(info.symbolName).getAddress().addr(), BoundedMemoryRegion.MODE_RW).
                    cast(lt).limit(1);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }

        MethodHandle target;
        AccessorKind kind = info.accessorKind;
        switch (kind) {
            case GET:
                target = lt.getter();
                target = target.bindTo(p).asType(MethodType.methodType(c));
                break;

            case SET:
                target = lt.setter();
                target = target.bindTo(p).asType(MethodType.methodType(void.class, c));
                break;

            case PTR:
                target = MethodHandles.constant(Pointer.class, p);
                break;

            default:
                throw new InternalError("Unexpected access method type: " + kind);
        }

        addMethodFromHandle(cw, methodName, kind.getMethodType(c), false, target, mv -> {});
    }
}
