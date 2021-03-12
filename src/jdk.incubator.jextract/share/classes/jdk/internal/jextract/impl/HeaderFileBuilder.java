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
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.jextract.Type;

import jdk.internal.jextract.impl.ConstantBuilder.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to generate header interface class in source form.
 * After aggregating various constituents of a .java source, build
 * method is called to get overall generated source string.
 */
abstract class HeaderFileBuilder extends ClassSourceBuilder {

    private static final String MEMBER_MODS = "public static";

    private final String superclass;

    HeaderFileBuilder(ToplevelBuilder enclosing, String name, String superclass) {
        super(enclosing, Kind.CLASS, name);
        this.superclass = superclass;
    }

    @Override
    String superClass() {
        return superclass;
    }

    @Override
    public void addVar(String javaName, String nativeName, VarInfo varInfo) {
        if (varInfo.carrier().equals(MemorySegment.class)) {
            emitWithConstantClass(constantBuilder -> {
                constantBuilder.addSegment(javaName, nativeName, varInfo.layout())
                        .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME, nativeName);
            });
        } else {
            emitWithConstantClass(constantBuilder -> {
                constantBuilder.addLayout(javaName, varInfo.layout())
                        .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME);
                Constant vhConstant = constantBuilder.addGlobalVarHandle(javaName, nativeName, varInfo)
                        .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME);
                Constant segmentConstant = constantBuilder.addSegment(javaName, nativeName, varInfo.layout())
                        .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME, nativeName);
                emitGlobalGetter(segmentConstant, vhConstant, javaName, nativeName, varInfo.carrier());
                emitGlobalSetter(segmentConstant, vhConstant, javaName, nativeName, varInfo.carrier());
                if (varInfo.fiName().isPresent()) {
                    emitFunctionalInterfaceGetter(varInfo.fiName().get(), javaName);
                }
            });
        }
    }

    @Override
    public void addFunction(String javaName, String nativeName, FunctionInfo functionInfo) {
        emitWithConstantClass(constantBuilder -> {
            Constant mhConstant = constantBuilder.addMethodHandle(javaName, nativeName, functionInfo, false)
                    .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME, nativeName);
            emitFunctionWrapper(mhConstant, javaName, nativeName, functionInfo);
        });
    }

    @Override
    public void addConstant(String javaName, Class<?> type, Object value) {
        if (type.equals(MemorySegment.class) || type.equals(MemoryAddress.class)) {
            emitWithConstantClass(constantBuilder -> {
                constantBuilder.addConstantDesc(javaName, type, value)
                        .emitGetter(this, MEMBER_MODS, Constant.JAVA_NAME);
            });
        } else {
            emitGetter(MEMBER_MODS, type, javaName, getConstantString(type, value));
        }
    }

    // private generation

    private void emitFunctionWrapper(Constant mhConstant, String javaName, String nativeName, FunctionInfo functionInfo) {
        incrAlign();
        indent();
        append(MEMBER_MODS + " ");
        append(functionInfo.methodType().returnType().getSimpleName() + " " + javaName + " (");
        String delim = "";
        List<String> pExprs = new ArrayList<>();
        List<String> paramNames = functionInfo.parameterNames().get();
        final int numParams = paramNames.size();
        for (int i = 0 ; i < numParams; i++) {
            String pName = paramNames.get(i);
            if (pName.isEmpty()) {
                pName = "x" + i;
            }
            if (functionInfo.methodType().parameterType(i).equals(MemoryAddress.class)) {
                pExprs.add(pName + ".address()");
            } else {
                pExprs.add(pName);
            }
            Class<?> pType = functionInfo.methodType().parameterType(i);
            if (pType.equals(MemoryAddress.class)) {
                pType = Addressable.class;
            }
            append(delim + " " + pType.getSimpleName() + " " + pName);
            delim = ", ";
        }
        if (functionInfo.isVarargs()) {
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
        append(mhConstant.accessExpression());
        append(", \"");
        append(nativeName);
        append("\");\n");
        indent();
        append("try {\n");
        incrAlign();
        indent();
        if (!functionInfo.methodType().returnType().equals(void.class)) {
            append("return (" + functionInfo.methodType().returnType().getName() + ")");
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

    private void emitFunctionalInterfaceGetter(String fiName, String javaName) {
        incrAlign();
        indent();
        append(MEMBER_MODS + " ");
        append(fiName + " " + javaName + " () {\n");
        incrAlign();
        indent();
        append("return " + fiName + ".ofAddressRestricted(" + javaName + "$get());\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    void emitPrimitiveTypedef(Type.Primitive primType, String name) {
        Type.Primitive.Kind kind = primType.kind();
        if (primitiveKindSupported(kind) && !kind.layout().isEmpty()) {
            incrAlign();
            indent();
            append(MEMBER_MODS);
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

    private void emitGlobalGetter(Constant segmentConstant, Constant vhConstant, String javaName, String nativeName, Class<?> type) {
        incrAlign();
        indent();
        append(MEMBER_MODS + " " + type.getSimpleName() + " " + javaName + "$get() {\n");
        incrAlign();
        indent();
        append("return (" + type.getName() + ") ");
        append(vhConstant.accessExpression());
        append(".get(RuntimeHelper.requireNonNull(");
        append(segmentConstant.accessExpression());
        append(", \"");
        append(nativeName);
        append("\"));\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    private void emitGlobalSetter(Constant segmentConstant, Constant vhConstant, String javaName, String nativeName, Class<?> type) {
        incrAlign();
        indent();
        append(MEMBER_MODS + " void " + javaName + "$set(" + " " + type.getSimpleName() + " x) {\n");
        incrAlign();
        indent();
        append(vhConstant.accessExpression());
        append(".set(RuntimeHelper.requireNonNull(");
        append(segmentConstant.accessExpression());
        append(", \"");
        append(nativeName);
        append("\"), x);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }
}
