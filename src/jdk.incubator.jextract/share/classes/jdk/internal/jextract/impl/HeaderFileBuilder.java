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
    protected final StringBuffer sb;

    // current line alignment (number of 4-spaces)
    private int align;
    private String superclass;

    HeaderFileBuilder(String headerfileName, String pkgName, String superclass, ConstantHelper.ConstantHelperFactory constantHelperFactory, AnnotationWriter annotationWriter) {
        super(Kind.CLASS, Utils.javaSafeIdentifier(headerfileName.replace(".h", "_h"), true), pkgName,
                constantHelperFactory.make(Utils.javaSafeIdentifier(headerfileName.replace(".h", "_h"), true)), annotationWriter);
        this.superclass = superclass;
        this.sb = new StringBuffer();
    }

    @Override
    String superClass() {
        return superclass;
    }

    @Override
    protected String getClassModifiers() {
        return "";
    }

    @Override
    JavaSourceBuilder prev() {
        return null;
    }

    @Override
    void append(String s) {
        sb.append(s);
    }

    @Override
    void append(char c) {
        sb.append(c);
    }

    @Override
    void append(long l) {
        sb.append(l);
    }

    @Override
    void indent() {
        for (int i = 0; i < align; i++) {
            append("    ");
        }
    }

    @Override
    void incrAlign() {
        align++;
    }

    @Override
    void decrAlign() {
        align--;
    }

    void addStaticFunctionWrapper(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc,
                                  boolean varargs, List<String> paramNames, List<String> annos, String returnAnno) {
        incrAlign();
        indent();
        append(PUB_MODS);
        if (mtype.returnType() != void.class) {
            append(returnAnno);
            append(' ');
        }
        append(mtype.returnType().getSimpleName() + " " + javaName + " (");
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
            append(delim + annos.get(i) + " " + pType.getSimpleName() + " " + pName);
            delim = ", ";
        }
        if (varargs) {
            String lastArg = "x" + numParams;
            if (numParams > 0) {
                append(", ");
            }
            append("Object... " + lastArg);
            pExprs.add(lastArg);
        }
        append(") {\n");
        incrAlign();
        indent();
        append("try {\n");
        incrAlign();
        indent();
        if (!mtype.returnType().equals(void.class)) {
            append("return (" + mtype.returnType().getName() + ")");
        }
        append(methodHandleGetCallString(javaName, nativeName, mtype, desc, varargs) + ".invokeExact(" + String.join(", ", pExprs) + ");\n");
        decrAlign();
        indent();
        append("} catch (Throwable ex) {\n");
        incrAlign();
        indent();
        append("throw new AssertionError(ex);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    void emitPrimitiveTypedef(Type.Primitive primType, String name, String anno) {
        Type.Primitive.Kind kind = primType.kind();
        if (primitiveKindSupported(kind) && !kind.layout().isEmpty()) {
            incrAlign();
            indent();
            append(PUB_MODS);
            append(anno + " ValueLayout ");
            append(uniqueNestedClassName(name));
            append(" = ");
            append(TypeTranslator.typeToLayoutName(kind));
            append(";\n");
            decrAlign();
        }
    }

    private boolean primitiveKindSupported(Type.Primitive.Kind kind) {
        return switch(kind) {
            case Short, Int, Long, LongLong, Float, Double, LongDouble, Char -> true;
            default -> false;
        };
    }

    List<JavaFileObject> build() {
        classEnd();
        String res = sb.toString();
        this.sb.delete(0, res.length());
        return List.of(Utils.fileFromString(pkgName, className, res), constantHelper.build());
    }
}
