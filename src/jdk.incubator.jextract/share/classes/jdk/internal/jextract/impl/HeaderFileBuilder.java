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
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.jextract.Type;

import javax.tools.JavaFileObject;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
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

    List<JavaFileObject> build() {
        classEnd();
        String res = builder.build();
        return List.of(Utils.fileFromString(pkgName, className, res));
    }

    @Override
    public void addVar(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        if (type.equals(MemorySegment.class)) {
            addSegmentGetter(javaName, nativeName, layout);
        } else {
            addLayoutGetter(javaName, layout);
            addVarHandleGetter(javaName, nativeName, layout, type);
            addSegmentGetter(javaName, nativeName, layout);
            addGetter(javaName, nativeName, layout, type);
            addSetter(javaName, nativeName, layout, type);
        }
    }

    @Override
    public void addFunction(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs, List<String> paramNames) {
        addStaticFunctionWrapper(javaName, nativeName, mtype, desc, varargs, paramNames);
    }

    @Override
    public void addConstant(String javaName, Class<?> type, Object value) {
        addConstantGetter(javaName, type, value);
    }

    @Override
    public void addTypedef(String name, String superClass, Type type) {
        if (type instanceof Type.Primitive) {
            // primitive
            emitPrimitiveTypedef((Type.Primitive)type, name);
        } else {
            TypedefBuilder builder = new TypedefBuilder(this, name, superClass);
            builder.classBegin();
            builder.classEnd();
        }
    }

    @Override
    public StructBuilder addStruct(String name, GroupLayout parentLayout, Type type) {
        return new StructBuilder(this, name, parentLayout, type);
    }

    @Override
    public void addFunctionalInterface(String name, MethodType mtype, FunctionDescriptor desc, Type type) {
        FunctionalInterfaceBuilder builder = new FunctionalInterfaceBuilder(this, name, mtype, desc, type);
        builder.classBegin();
        builder.classEnd();
    }

    private void addStaticFunctionWrapper(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc,
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

    private void emitPrimitiveTypedef(Type.Primitive primType, String name) {
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

    private void addLayoutGetter(String javaName, MemoryLayout layout) {
        emitForwardGetter(constantHelper.addLayout(javaName, layout));
    }

    private void addVarHandleGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        emitForwardGetter(constantHelper.addGlobalVarHandle(javaName, nativeName, layout, type));
    }

    private void addMethodHandleGetter(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs) {
        emitForwardGetter(constantHelper.addMethodHandle(javaName, nativeName, mtype, desc, varargs),
                true, "unresolved symbol: " + nativeName);
    }

    private void addSegmentGetter(String javaName, String nativeName, MemoryLayout layout) {
        emitForwardGetter(constantHelper.addSegment(javaName, nativeName, layout),
                true, "unresolved symbol: " + nativeName);
    }

    private void addConstantGetter(String javaName, Class<?> type, Object value) {
        emitForwardGetter(constantHelper.addConstant(javaName, type, value));
    }

    private void addGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + " " + type.getSimpleName() + " " + javaName + "$get() {\n");
        builder.incrAlign();
        builder.indent();
        String vhParam = addressGetCallString(javaName, nativeName, layout);
        builder.append("return (" + type.getName() + ") ");
        builder.append(globalVarHandleGetCallString(javaName, nativeName, layout, type));
        builder.append(".get(RuntimeHelper.requireNonNull(");
        builder.append(vhParam);
        builder.append(", \"unresolved symbol: ");
        builder.append(nativeName);
        builder.append("\"));\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private void addSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + "void " + javaName + "$set(" + " " + type.getSimpleName() + " x) {\n");
        builder.incrAlign();
        builder.indent();
        String vhParam = addressGetCallString(javaName, nativeName, layout);
        builder.append(globalVarHandleGetCallString(javaName, nativeName, layout, type));
        builder.append(".set(RuntimeHelper.requireNonNull(");
        builder.append(vhParam);
        builder.append(", \"unresolved symbol: ");
        builder.append(nativeName);
        builder.append("\"), x);\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    // Utility

    protected void emitForwardGetter(DirectMethodHandleDesc desc) {
        emitForwardGetter(desc, false, "");
    }

    protected void emitForwardGetter(DirectMethodHandleDesc desc, boolean nullCheck, String errMsg) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + " " + displayName(desc.invocationType().returnType()) + " " + desc.methodName() + "() {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return ");
        if (nullCheck) {
            builder.append("RuntimeHelper.requireNonNull(");
        }
        builder.append(getCallString(desc));
        if (nullCheck) {
            builder.append(",\"");
            builder.append(errMsg);
            builder.append("\")");
        }
        builder.append(";\n");
        builder.decrAlign();
        builder.indent();
        builder.append("}\n");
        builder.decrAlign();
    }

    private String methodHandleGetCallString(String javaName, String nativeName, MethodType mt, FunctionDescriptor fDesc, boolean varargs) {
        return getCallString(constantHelper.addMethodHandle(javaName, nativeName, mt, fDesc, varargs));
    }

    private String globalVarHandleGetCallString(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        return getCallString(constantHelper.addGlobalVarHandle(javaName, nativeName, layout, type));
    }

    private String addressGetCallString(String javaName, String nativeName, MemoryLayout layout) {
        return getCallString(constantHelper.addSegment(javaName, nativeName, layout));
    }
}
