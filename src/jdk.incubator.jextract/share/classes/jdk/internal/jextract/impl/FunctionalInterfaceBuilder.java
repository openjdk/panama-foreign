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

public class FunctionalInterfaceBuilder extends JavaSourceBuilder {
    private final JavaSourceBuilder prev;
    private final String fiAnno;
    private final MethodType fiType;
    private final FunctionDescriptor fiDesc;

    FunctionalInterfaceBuilder(JavaSourceBuilder prev, String className, MethodType fiType, FunctionDescriptor fiDesc,
                               String pkgName, ConstantHelper constantHelper, AnnotationWriter annotationWriter, Type funcType) {
        super(className, pkgName, constantHelper, Kind.INTERFACE);
        this.fiType = fiType;
        this.fiDesc = fiDesc;
        this.prev = prev;
        this.fiAnno = annotationWriter.getCAnnotation(funcType);
    }

    JavaSourceBuilder prev() {
        return prev;
    }

    @Override
    void append(String s) {
        prev.append(s);
    }

    @Override
    void append(char c) {
        prev.append(c);
    }

    @Override
    void append(long l) {
        prev.append(l);
    }

    @Override
    void indent() {
        prev.indent();
    }

    @Override
    void incrAlign() {
        prev.incrAlign();
    }

    @Override
    void decrAlign() {
        prev.decrAlign();
    }

    @Override
    protected String getClassModifiers() {
        return PUB_MODS;
    }

    @Override
    protected void addPackagePrefix() {
        // nested class. containing class has necessary package declaration
    }

    @Override
    protected void addImportSection() {
        // nested class. containing class has necessary imports
    }

    @Override
    JavaSourceBuilder classEnd() {
        emitFunctionalInterfaceMethod();
        emitFunctionalFactories();
        return super.classEnd();
    }

    void emitFunctionalInterfaceMethod() {
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
        indent();
    }

    private void emitFunctionalFactories() {
        incrAlign();
        indent();
        append(PUB_MODS + " " + fiAnno + " MemorySegment allocate(" + className + " fi) {\n");
        incrAlign();
        indent();
        append("return RuntimeHelper.upcallStub(" + className + ".class, fi, " + functionGetCallString(className, fiDesc) + ", " +
                "\"" + fiType.toMethodDescriptorString() + "\");\n");
        decrAlign();
        indent();
        append("}\n");

        indent();
        append(PUB_MODS + " " + fiAnno + " MemorySegment allocate(" + className + " fi, NativeScope scope) {\n");
        incrAlign();
        indent();
        append("return scope.register(allocate(fi));\n");
        decrAlign();
        indent();
        append("}\n");
    }
}
