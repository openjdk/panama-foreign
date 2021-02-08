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
import jdk.incubator.jextract.Type;

import java.lang.invoke.MethodType;

public class FunctionalInterfaceBuilder extends NestedClassBuilder {
    private final MethodType fiType;
    private final FunctionDescriptor fiDesc;

    FunctionalInterfaceBuilder(JavaSourceBuilder enclosing, String className, MethodType fiType,
                               FunctionDescriptor fiDesc, Type funcType) {
        super(enclosing, Kind.INTERFACE, className);
        this.fiType = fiType;
        this.fiDesc = fiDesc;
    }

    @Override
    JavaSourceBuilder classEnd() {
        emitFunctionalInterfaceMethod();
        emitFunctionalFactories();
        return super.classEnd();
    }

    void emitFunctionalInterfaceMethod() {
        builder.incrAlign();
        builder.indent();
        builder.append(fiType.returnType().getName() + " apply(");
        String delim = "";
        for (int i = 0 ; i < fiType.parameterCount(); i++) {
            builder.append(delim + fiType.parameterType(i).getName() + " x" + i);
            delim = ", ";
        }
        builder.append(");\n");
        builder.decrAlign();
    }

    private void emitFunctionalFactories() {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + " MemorySegment allocate(" + className + " fi) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return RuntimeHelper.upcallStub(" + className + ".class, fi, " + functionGetCallString(className, fiDesc) + ", " +
                "\"" + fiType.toMethodDescriptorString() + "\");\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.indent();
        builder.append(PUB_MODS + " MemorySegment allocate(" + className + " fi, NativeScope scope) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return allocate(fi).handoff(scope);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private String functionGetCallString(String javaName, FunctionDescriptor fDesc) {
        return getCallString(constantHelper.addFunctionDesc(javaName, fDesc));
    }
}
