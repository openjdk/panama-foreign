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

import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to generate header interface class in source form.
 * After aggregating various constituents of a .java source, build
 * method is called to get overall generated source string.
 */
class HeaderFileBuilder extends JavaSourceBuilder {

    private final String superclass;
    private final boolean isPublic;

    HeaderFileBuilder(ClassDesc desc, String superclass, boolean isPublic) {
        super(Kind.CLASS, desc);
        this.superclass = superclass;
        this.isPublic = isPublic;
    }

    @Override
    String superClass() {
        return superclass;
    }

    @Override
    protected String getClassModifiers() {
        return isPublic ? "public " : "";
    }

    @Override
    public void addVar(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        if (type.equals(MemorySegment.class)) {
            ConstantBuilder constantBuilder = new ConstantBuilder(this, Kind.CLASS, javaName + "_constants");
            constantBuilder.classBegin();
            String access = constantBuilder.addSegment(javaName, nativeName, layout);
            constantBuilder.classEnd();
            emitGetter(MemorySegment.class, javaName + "$SEGMENT", access, false, null);
        } else {
            ConstantBuilder constantBuilder = new ConstantBuilder(this, Kind.CLASS, javaName + "_constants");
            constantBuilder.classBegin();
            String layoutAccess = constantBuilder.addLayout(javaName, layout);
            String vhAccess = constantBuilder.addGlobalVarHandle(javaName, nativeName, layout, type);
            String segmentAccess = constantBuilder.addSegment(javaName, nativeName, layout);
            constantBuilder.classEnd();
            emitGetter(VarHandle.class, javaName + "$VH", vhAccess, false, null);
            emitGetter(MemoryLayout.class, javaName + "$LAYOUT", layoutAccess, false, null);
            emitGetter(MemorySegment.class, javaName + "$SEGMENT", segmentAccess, false, null);
            emitGlobalGetter(segmentAccess, vhAccess, javaName, nativeName, type);
            emitGlobalSetter(segmentAccess, vhAccess, javaName, nativeName, type);
        }
    }

    @Override
    public void addFunction(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs, List<String> paramNames) {
        ConstantBuilder constantBuilder = new ConstantBuilder(this, Kind.CLASS, javaName + "_constants");
        constantBuilder.classBegin();
        String access = constantBuilder.addMethodHandle(javaName, nativeName, mtype, desc, varargs);
        constantBuilder.classEnd();
        emitGetter(MethodHandle.class, javaName + "$MH", access,
                true, "unresolved symbol: " + nativeName);
        emitFunctionWrapper(access, javaName, nativeName, mtype, varargs, paramNames);
    }

    @Override
    public void addConstant(String javaName, Class<?> type, Object value) {
        if (type.equals(MemorySegment.class) || type.equals(MemoryAddress.class)) {
            ConstantBuilder constantBuilder = new ConstantBuilder(this, Kind.CLASS, javaName + "_constants");
            constantBuilder.classBegin();
            String mhDesc = constantBuilder.addConstantDesc(javaName, type, value);
            constantBuilder.classEnd();
            emitGetter(type, javaName, mhDesc, false, null);
        } else {
            emitGetter(type, javaName, getConstantString(type, value), false, null);
        }
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

    // private generation

    private void emitFunctionWrapper(String access, String javaName, String nativeName, MethodType mtype,
                                  boolean varargs, List<String> paramNames) {
        incrAlign();
        indent();
        append(PUB_MODS);
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
            append(delim + " " + pType.getSimpleName() + " " + pName);
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
        append("var mh$ = RuntimeHelper.requireNonNull(");
        append(access);
        append(", \"unresolved symbol: ");
        append(nativeName);
        append("\");\n");
        indent();
        append("try {\n");
        incrAlign();
        indent();
        if (!mtype.returnType().equals(void.class)) {
            append("return (" + mtype.returnType().getName() + ")");
        }
        append("mh$.invokeExact(" + String.join(", ", pExprs) + ");\n");
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
        append("}\n");
        decrAlign();
    }

    private void emitPrimitiveTypedef(Type.Primitive primType, String name) {
        Type.Primitive.Kind kind = primType.kind();
        if (primitiveKindSupported(kind) && !kind.layout().isEmpty()) {
            incrAlign();
            indent();
            append(PUB_MODS);
            append(" ValueLayout ");
            append(uniqueNestedClassName(name));
            append(" = ");
            append(TypeTranslator.typeToLayoutName(kind));
            append(";\n");
            decrAlign();
        }
    }

    private boolean primitiveKindSupported(Type.Primitive.Kind kind) {
        return switch(kind) {
            case Short, Int, Long, LongLong, Float, Double, Char -> true;
            default -> false;
        };
    }

    private String getConstantString(Class<?> type, Object value) {
        StringBuilder buf = new StringBuilder();
        if (type == float.class) {
            float f = ((Number)value).floatValue();
            if (Float.isFinite(f)) {
                buf.append(value);
                buf.append("f");
            } else {
                buf.append("Float.valueOf(\"");
                buf.append(value.toString());
                buf.append("\")");
            }
        } else if (type == long.class) {
            buf.append(value);
            buf.append("L");
        } else if (type == double.class) {
            double d = ((Number)value).doubleValue();
            if (Double.isFinite(d)) {
                buf.append(value);
                buf.append("d");
            } else {
                buf.append("Double.valueOf(\"");
                buf.append(value.toString());
                buf.append("\")");
            }
        } else {
            buf.append("(" + type.getName() + ")");
            buf.append(value + "L");
        }
        return buf.toString();
    }

    private void emitGlobalGetter(String vhParam, String vhStr, String javaName, String nativeName, Class<?> type) {
        incrAlign();
        indent();
        append(PUB_MODS + " " + type.getSimpleName() + " " + javaName + "$get() {\n");
        incrAlign();
        indent();
        append("return (" + type.getName() + ") ");
        append(vhStr);
        append(".get(RuntimeHelper.requireNonNull(");
        append(vhParam);
        append(", \"unresolved symbol: ");
        append(nativeName);
        append("\"));\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitGlobalSetter(String vhParam, String vhStr, String javaName, String nativeName, Class<?> type) {
        incrAlign();
        indent();
        append(PUB_MODS + "void " + javaName + "$set(" + " " + type.getSimpleName() + " x) {\n");
        incrAlign();
        indent();
        append(vhStr);
        append(".set(RuntimeHelper.requireNonNull(");
        append(vhParam);
        append(", \"unresolved symbol: ");
        append(nativeName);
        append("\"), x);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }
}
