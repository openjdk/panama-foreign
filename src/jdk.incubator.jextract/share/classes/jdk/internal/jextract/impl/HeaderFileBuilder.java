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

import jdk.incubator.foreign.Addressable;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.jextract.Type;

import javax.tools.JavaFileObject;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to generate header interface class in source form.
 * After aggregating various constituents of a .java source, build
 * method is called to get overall generated source string.
 */
class HeaderFileBuilder extends JavaSourceBuilder {

    private String superclass;

    HeaderFileBuilder(String clsName, String pkgName, String superclass, ConstantHelper constantHelper) {
        super(new StringSourceBuilder(), Kind.CLASS, clsName, pkgName, constantHelper);
        this.superclass = superclass;
    }

    @Override
    String superClass() {
        return superclass;
    }

    @Override
    protected String getClassModifiers() {
        return "";
    }

    void addStaticFunctionWrapper(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc,
                                  boolean varargs, List<String> paramNames) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS);
        builder.append(mtype.returnType().getSimpleName() + " " + javaName + " (");
        String delim = "";
        List<String> pExprs = new ArrayList<>();
        final int numParams = paramNames.size();
        for (int i = 0 ; i < numParams; i++) {
            String pName = paramNames.get(i);
            if (pName.isEmpty()) {
                pName = "x" + i;
            }
            if (mtype.parameterType(i).equals(MemoryAddress.class)) {
                pExprs.add(pName + ".address()");
            } else {
                pExprs.add(pName);
            }
            Class<?> pType = mtype.parameterType(i);
            if (pType.equals(MemoryAddress.class)) {
                pType = Addressable.class;
            }
            builder.append(delim + " " + pType.getSimpleName() + " " + pName);
            delim = ", ";
        }
        if (varargs) {
            String lastArg = "x" + numParams;
            if (numParams > 0) {
                builder.append(", ");
            }
            builder.append("Object... " + lastArg);
            pExprs.add(lastArg);
        }
        builder.append(") {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("var mh$ = RuntimeHelper.requireNonNull(");
        builder.append(methodHandleGetCallString(javaName, nativeName, mtype, desc, varargs));
        builder.append(", \"unresolved symbol: ");
        builder.append(nativeName);
        builder.append("\");\n");
        builder.indent();
        builder.append("try {\n");
        builder.incrAlign();
        builder.indent();
        if (!mtype.returnType().equals(void.class)) {
            builder.append("return (" + mtype.returnType().getName() + ")");
        }
        builder.append("mh$.invokeExact(" + String.join(", ", pExprs) + ");\n");
        builder.decrAlign();
        builder.indent();
        builder.append("} catch (Throwable ex$) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("throw new AssertionError(\"should not reach here\", ex$);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    void emitPrimitiveTypedef(Type.Primitive primType, String name) {
        Type.Primitive.Kind kind = primType.kind();
        if (primitiveKindSupported(kind) && !kind.layout().isEmpty()) {
            builder.incrAlign();
            builder.indent();
            builder.append(PUB_MODS);
            builder.append(" ValueLayout ");
            builder.append(uniqueNestedClassName(name));
            builder.append(" = ");
            builder.append(TypeTranslator.typeToLayoutName(kind));
            builder.append(";\n");
            builder.decrAlign();
        }
    }

    private boolean primitiveKindSupported(Type.Primitive.Kind kind) {
        return switch(kind) {
            case Short, Int, Long, LongLong, Float, Double, Char -> true;
            default -> false;
        };
    }

    List<JavaFileObject> build() {
        classEnd();
        String res = builder.build();
        return List.of(Utils.fileFromString(pkgName, className, res));
    }
}
