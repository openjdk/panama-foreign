/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.jextract.impl;

import jdk.incubator.foreign.*;

import jdk.internal.jextract.impl.ConstantBuilder.Constant;

import java.lang.invoke.MethodType;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FunctionalInterfaceBuilder extends ClassSourceBuilder {

    private static final String MEMBER_MODS = "static";

    private final MethodType fiType;
    private final FunctionDescriptor fiDesc;

    FunctionalInterfaceBuilder(JavaSourceBuilder enclosing, String className,
                               MethodType fiType, FunctionDescriptor fiDesc) {
        super(enclosing, Kind.INTERFACE, className);
        this.fiType = fiType;
        this.fiDesc = fiDesc;
    }

    @Override
    JavaSourceBuilder classEnd() {
        emitFunctionalInterfaceMethod();
        emitFunctionalFactories();
        emitFunctionalFactoryForPointer();
        return super.classEnd();
    }

    // private generation

    private void emitFunctionalInterfaceMethod() {
        incrAlign();
        indent();
        append(fiType.returnType().getName() + " apply(");
        String delim = "";
        for (int i = 0 ; i < fiType.parameterCount(); i++) {
            append(delim + fiType.parameterType(i).getName() + " x" + i);
            delim = ", ";
        }
        append(");\n");
        decrAlign();
    }

    private void emitFunctionalFactories() {
        emitWithConstantClass(constantBuilder -> {
            Constant functionDesc = constantBuilder.addFunctionDesc(className(), fiDesc);
            incrAlign();
            indent();
            append(MEMBER_MODS + " MemoryAddress allocate(" + className() + " fi) {\n");
            incrAlign();
            indent();
            append("return RuntimeHelper.upcallStub(" + className() + ".class, fi, " + functionDesc.accessExpression() + ", " +
                    "\"" + fiType.toMethodDescriptorString() + "\");\n");
            decrAlign();
            indent();
            append("}\n");
            indent();
            append(MEMBER_MODS + " MemoryAddress allocate(" + className() + " fi, ResourceScope scope) {\n");
            incrAlign();
            indent();
            append("return RuntimeHelper.upcallStub(" + className() + ".class, fi, " + functionDesc.accessExpression() + ", " +
                    "\"" + fiType.toMethodDescriptorString() + "\", scope);\n");
            decrAlign();
            indent();
            append("}\n");
            decrAlign();
        });
    }

    private void emitFunctionalFactoryForPointer() {
        emitWithConstantClass(constantBuilder -> {
            Constant mhConstant = constantBuilder.addMethodHandle(className(), className(), FunctionInfo.ofFunctionPointer(fiType, fiDesc), true);
            incrAlign();
            indent();
            append(MEMBER_MODS + " " + className() + " ofAddress(MemoryAddress addr) {\n");
            incrAlign();
            indent();
            append("return (");
            String delim = "";
            for (int i = 0 ; i < fiType.parameterCount(); i++) {
                append(delim + fiType.parameterType(i).getName() + " x" + i);
                delim = ", ";
            }
            append(") -> {\n");
            incrAlign();
            indent();
            append("try {\n");
            incrAlign();
            indent();
            if (!fiType.returnType().equals(void.class)) {
                append("return (" + fiType.returnType().getName() + ")");
            }
            append(mhConstant.accessExpression() + ".invokeExact((Addressable)addr");
            if (fiType.parameterCount() > 0) {
                String params = IntStream.range(0, fiType.parameterCount())
                        .mapToObj(i -> "x" + i)
                        .collect(Collectors.joining(", "));
                append(", " + params);
            }
            append(");\n");
            decrAlign();
            indent();
            append("} catch (Throwable ex$) {\n");
            incrAlign();
            indent();
            append("throw new AssertionError(\"should not reach here\", ex$);\n");
            decrAlign();
            indent();
            append("}\n");
            decrAlign();
            indent();
            append("};\n");
            decrAlign();
            indent();
            append("}\n");
            decrAlign();
        });
    }

    @Override
    protected void emitWithConstantClass(Consumer<ConstantBuilder> constantConsumer) {
        enclosing.emitWithConstantClass(constantConsumer);
    }
}
