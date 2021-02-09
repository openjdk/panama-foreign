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

    private String superclass;
    private boolean isPublic;

    HeaderFileBuilder(String pkgName, String clsName, String superclass, boolean isPublic) {
        super(new StringSourceBuilder(), Kind.CLASS, clsName, pkgName);
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
            emitForwardGetter(MemorySegment.class, javaName + "$SEGMENT", access, false, null);
        } else {
            ConstantBuilder constantBuilder = new ConstantBuilder(this, Kind.CLASS, javaName + "_constants");
            constantBuilder.classBegin();
            String layoutAccess = constantBuilder.addLayout(javaName, layout);
            String vhAccess = constantBuilder.addGlobalVarHandle(javaName, nativeName, layout, type);
            String segmentAccess = constantBuilder.addSegment(javaName, nativeName, layout);
            constantBuilder.classEnd();
            emitForwardGetter(VarHandle.class, javaName + "$VH", vhAccess, false, null);
            emitForwardGetter(MemoryLayout.class, javaName + "$LAYOUT", layoutAccess, false, null);
            emitForwardGetter(MemorySegment.class, javaName + "$SEGMENT", segmentAccess, false, null);
            addGetter(segmentAccess, vhAccess, javaName, nativeName, layout, type);
            addSetter(segmentAccess, vhAccess, javaName, nativeName, layout, type);
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
        ConstantBuilder constantBuilder = new ConstantBuilder(this, Kind.CLASS, javaName + "_constants");
        constantBuilder.classBegin();
        String access = constantBuilder.addMethodHandle(javaName, nativeName, mtype, desc, varargs);
        constantBuilder.classEnd();
        addMethodHandleGetter(MethodHandle.class, javaName + "$MH", access, nativeName);
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
        builder.append(access);
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

    private void addMethodHandleGetter(Class<?> type, String javaName, String access, String nativeName) {
        emitForwardGetter(type, javaName, access,
                true, "unresolved symbol: " + nativeName);
    }

    private void addConstantGetter(String javaName, Class<?> type, Object value) {
        if (type.equals(MemorySegment.class) || type.equals(MemoryAddress.class)) {
            ConstantBuilder constantBuilder = new ConstantBuilder(this, Kind.CLASS, javaName + "_constants");
            constantBuilder.classBegin();
            String mhDesc = constantBuilder.addConstantDesc(javaName, type, value);
            constantBuilder.classEnd();
            emitForwardGetter(type, javaName, mhDesc, false, null);
        } else {
            builder.incrAlign();
            builder.indent();
            builder.append("public static final ");
            builder.append(type.getName());
            builder.append(' ');
            builder.append(javaName);
            builder.append("() { return ");
            builder.append(getConstantString(type, value));
            builder.append("; }\n\n");
            builder.decrAlign();
        }
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

    private void addGetter(String vhParam, String vhStr, String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + " " + type.getSimpleName() + " " + javaName + "$get() {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return (" + type.getName() + ") ");
        builder.append(vhStr);
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

    private void addSetter(String vhParam, String vhStr, String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + "void " + javaName + "$set(" + " " + type.getSimpleName() + " x) {\n");
        builder.incrAlign();
        builder.indent();
        builder.append(vhStr);
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

    protected void emitForwardGetter(Class<?> type, String name, String access, boolean nullCheck, String errMsg) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + " " + type.getSimpleName() + " " +name + "() {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return ");
        if (nullCheck) {
            builder.append("RuntimeHelper.requireNonNull(");
        }
        builder.append(access);
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
}
