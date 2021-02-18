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

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.jextract.Type;

import jdk.internal.jextract.impl.ConstantBuilder.Constant;

import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodType;
import java.util.List;

/**
 * A helper class to generate header interface class in source form.
 * After aggregating various constituents of a .java source, build
 * method is called to get overall generated source string.
 */
abstract class HeaderFileBuilder extends JavaSourceBuilder {

    private static final String MEMBER_MODS = "public static";

    private final String superclass;

    HeaderFileBuilder(ClassDesc desc, String superclass) {
        super(Kind.CLASS, desc);
        this.superclass = superclass;
    }

    @Override
    String superClass() {
        return superclass;
    }

    @Override
    public void addVar(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        if (type.equals(MemorySegment.class)) {
            emitWithConstantClass(javaName, constantBuilder -> {
                constantBuilder.addSegment(javaName, nativeName, layout)
                        .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME, nativeName);
            });
        } else {
            emitWithConstantClass(javaName, constantBuilder -> {
                constantBuilder.addLayout(javaName, layout)
                        .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME);
                Constant vhConstant = constantBuilder.addGlobalVarHandle(javaName, nativeName, layout, type)
                        .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME);
                Constant segmentConstant = constantBuilder.addSegment(javaName, nativeName, layout)
                        .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME, nativeName);
                emitGlobalGetter(segmentConstant, vhConstant, javaName, nativeName, type);
                emitGlobalSetter(segmentConstant, vhConstant, javaName, nativeName, type);
            });
        }
    }

    @Override
    public void addFunction(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs, List<String> paramNames) {
        emitWithConstantClass(javaName, constantBuilder -> {
            constantBuilder.addMethodHandle(javaName, nativeName, mtype, desc, false, varargs)
                    .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME, nativeName)
                    .emitFunction(this, MEMBER_MODS, Constant.JAVA_NAME, paramNames, nativeName);
        });
    }

    @Override
    public void addVirtualFunction(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc) {
        emitWithConstantClass(javaName, constantBuilder -> {
            constantBuilder.addMethodHandle(javaName, nativeName, mtype, desc, true, false)
                    .emitGetter(this, MEMBER_MODS, Constant.QUALIFIED_NAME)
                    .emitFunction(this, MEMBER_MODS, Constant.JAVA_NAME, null);
        });
    }

    @Override
    public void addConstant(String javaName, Class<?> type, Object value) {
        if (type.equals(MemorySegment.class) || type.equals(MemoryAddress.class)) {
            emitWithConstantClass(javaName, constantBuilder -> {
                constantBuilder.addConstantDesc(javaName, type, value)
                        .emitGetter(this, MEMBER_MODS, Constant.JAVA_NAME);
            });
        } else {
            emitGetter(MEMBER_MODS, type, javaName, getConstantString(type, value));
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

    private void emitPrimitiveTypedef(Type.Primitive primType, String name) {
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
