/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.ValueLayout;

import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class ConstantBuilder extends NestedClassBuilder {

    // set of names generates already
    private final Map<String, String> namesGenerated = new HashMap<>();

    public ConstantBuilder(JavaSourceBuilder enclosing, JavaSourceBuilder.Kind kind, String className) {
        super(enclosing, kind, className);
    }

    String memberMods() {
        return kind == Kind.CLASS ?
                "static final " : "";
    }

    // public API

    public String addLayout(String javaName, MemoryLayout layout) {
        return emitIfAbsent(javaName, ConstantKind.LAYOUT,
                () -> emitLayoutField(javaName, layout));
    }

    public String addFieldVarHandle(String javaName, String nativeName, MemoryLayout layout,
                                    Class<?> type, String rootJavaName, List<String> prefixElementNames) {
        return addVarHandle(javaName, nativeName, layout, type, rootJavaName, prefixElementNames);
    }

    public String addGlobalVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        return addVarHandle(javaName, nativeName, layout, type, null, List.of());
    }

    private String addVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type,
                                String rootLayoutName, List<String> prefixElementNames) {
        return emitIfAbsent(javaName, ConstantKind.VAR_HANDLE,
                () -> emitVarHandleField(javaName, nativeName, type, layout, rootLayoutName, prefixElementNames));
    }

    public String addMethodHandle(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs) {
        return emitIfAbsent(javaName, ConstantKind.METHOD_HANDLE,
                () -> emitMethodHandleField(javaName, nativeName, mtype, desc, varargs));
    }

    public String addSegment(String javaName, String nativeName, MemoryLayout layout) {
        return emitIfAbsent(javaName, ConstantKind.SEGMENT,
                () -> emitSegmentField(javaName, nativeName, layout));
    }

    public String addFunctionDesc(String javaName, FunctionDescriptor desc) {
        return emitIfAbsent(javaName, ConstantKind.FUNCTION_DESCRIPTOR,
                () -> emitFunctionDescField(javaName, desc));
    }

    public String addConstantDesc(String javaName, Class<?> type, Object value) {
        return emitIfAbsent(javaName, ConstantKind.CONSTANT,
                () -> emitConstant(javaName, type, value));
    }

    // private generators

    enum ConstantKind {
        LAYOUT("LAYOUT_"),
        METHOD_HANDLE("MH_"),
        VAR_HANDLE("VH_"),
        FUNCTION_DESCRIPTOR("FUNC_"),
        ADDRESS("ADDR_"),
        SEGMENT("SEGMENT_"),
        CONSTANT("");

        final String nameSuffix;

        ConstantKind(String nameSuffix) {
            this.nameSuffix = nameSuffix;
        }

        String fieldName(String javaName) {
            return javaName + "$" + nameSuffix;
        }
    }

    public String emitIfAbsent(String name, ConstantKind kind, Supplier<String> constantFactory) {
        String lookupName = kind.fieldName(name);
        String access = namesGenerated.get(lookupName);
        if (access == null) {
            String fieldName = constantFactory.get();
            access = className() + "." + fieldName;
            namesGenerated.put(fieldName, access);
        }
        return access;
    }

    private String emitMethodHandleField(String javaName, String nativeName, MethodType mtype,
                                         FunctionDescriptor desc, boolean varargs) {
        String functionDescAccess = addFunctionDesc(javaName, desc);
        incrAlign();
        String fieldName = ConstantKind.METHOD_HANDLE.fieldName(javaName);
        indent();
        append(memberMods() + "MethodHandle ");
        append(fieldName + " = RuntimeHelper.downcallHandle(\n");
        incrAlign();
        indent();
        append("LIBRARIES, \"" + nativeName + "\"");
        append(",\n");
        indent();
        append("\"" + mtype.toMethodDescriptorString() + "\",\n");
        indent();
        append(functionDescAccess);
        append(", ");
        // isVariadic
        append(varargs);
        append("\n");
        decrAlign();
        indent();
        append(");\n");
        decrAlign();
        return fieldName;
    }

    private String emitVarHandleField(String javaName, String nativeName, Class<?> type, MemoryLayout layout,
                                      String rootLayoutName, List<String> prefixElementNames) {
        String layoutAccess = rootLayoutName != null ?
                ConstantKind.LAYOUT.fieldName(rootLayoutName) :
                addLayout(javaName, layout);
        incrAlign();
        String typeName = type.getName();
        boolean isAddr = typeName.contains("MemoryAddress");
        if (isAddr) {
            typeName = "long";
        }
        indent();
        String fieldName = ConstantKind.VAR_HANDLE.fieldName(javaName);
        append(memberMods() + "VarHandle " + fieldName + " = ");
        if (isAddr) {
            append("MemoryHandles.asAddressVarHandle(");
        }
        append(layoutAccess);
        append(".varHandle(" + typeName + ".class");
        if (rootLayoutName != null) {
            for (String prefixElementName : prefixElementNames) {
                append(", MemoryLayout.PathElement.groupElement(\"" + prefixElementName + "\")");
            }
            append(", MemoryLayout.PathElement.groupElement(\"" + nativeName + "\")");
        }
        append(")");
        if (isAddr) {
            append(")");
        }
        append(";\n");
        decrAlign();
        return fieldName;
    }

    private String emitLayoutField(String javaName, MemoryLayout layout) {
        String fieldName = ConstantKind.LAYOUT.fieldName(javaName);
        incrAlign();
        indent();
        append(memberMods() + "MemoryLayout " + fieldName + " = ");
        emitLayoutString(layout);
        append(";\n");
        decrAlign();
        return fieldName;
    }

    private void emitLayoutString(MemoryLayout l) {
        if (l instanceof ValueLayout) {
            append(typeToLayoutName((ValueLayout) l));
        } else if (l instanceof SequenceLayout) {
            append("MemoryLayout.ofSequence(");
            if (((SequenceLayout) l).elementCount().isPresent()) {
                append(((SequenceLayout) l).elementCount().getAsLong() + ", ");
            }
            emitLayoutString(((SequenceLayout) l).elementLayout());
            append(")");
        } else if (l instanceof GroupLayout) {
            if (((GroupLayout) l).isStruct()) {
                append("MemoryLayout.ofStruct(\n");
            } else {
                append("MemoryLayout.ofUnion(\n");
            }
            incrAlign();
            String delim = "";
            for (MemoryLayout e : ((GroupLayout) l).memberLayouts()) {
                append(delim);
                indent();
                emitLayoutString(e);
                delim = ",\n";
            }
            append("\n");
            decrAlign();
            indent();
            append(")");
        } else {
            // padding
            append("MemoryLayout.ofPaddingBits(" + l.bitSize() + ")");
        }
        if (l.name().isPresent()) {
            append(".withName(\"" +  l.name().get() + "\")");
        }
    }

    private String emitFunctionDescField(String javaName, FunctionDescriptor desc) {
        incrAlign();
        indent();
        String fieldName = ConstantKind.FUNCTION_DESCRIPTOR.fieldName(javaName);
        final boolean noArgs = desc.argumentLayouts().isEmpty();
        append(memberMods());
        append("FunctionDescriptor ");
        append(fieldName);
        append(" = ");
        if (desc.returnLayout().isPresent()) {
            append("FunctionDescriptor.of(");
            emitLayoutString(desc.returnLayout().get());
            if (!noArgs) {
                append(",");
            }
        } else {
            append("FunctionDescriptor.ofVoid(");
        }
        if (!noArgs) {
            append("\n");
            incrAlign();
            String delim = "";
            for (MemoryLayout e : desc.argumentLayouts()) {
                append(delim);
                indent();
                emitLayoutString(e);
                delim = ",\n";
            }
            append("\n");
            decrAlign();
            indent();
        }
        append(");\n");
        decrAlign();
        return fieldName;
    }

    private String emitConstantSegment(String javaName, Object value) {
        incrAlign();
        indent();
        String fieldName = ConstantKind.CONSTANT.fieldName(javaName);
        append(memberMods());
        append("MemorySegment ");
        append(fieldName);
        append(" = CLinker.toCString(\"");
        append(Utils.quote(Objects.toString(value)));
        append("\");\n");
        decrAlign();
        return fieldName;
    }

    private String emitConstant(String javaName, Class<?> type, Object value) {
        String str;
        if (type == MemorySegment.class) {
            str = emitConstantSegment(javaName, value);
        } else if (type == MemoryAddress.class) {
            str = emitConstantAddress(javaName, value);
        } else {
            throw new UnsupportedOperationException();
        }
        return str;
    }

    private String emitConstantAddress(String javaName, Object value) {
        incrAlign();
        indent();
        String fieldName = ConstantKind.ADDRESS.fieldName(javaName);
        append(memberMods());
        append("MemoryAddress ");
        append(fieldName);
        append(" = MemoryAddress.ofLong(");
        append(((Number)value).longValue());
        append("L);\n");
        decrAlign();
        return fieldName;
    }

    private static String typeToLayoutName(ValueLayout vl) {
        if (UnsupportedLayouts.isUnsupported(vl)) {
            return "MemoryLayout.ofPaddingBits(" + vl.bitSize() + ")";
        }

        CLinker.TypeKind kind = (CLinker.TypeKind)vl.attribute(CLinker.TypeKind.ATTR_NAME).orElseThrow(
                () -> new IllegalStateException("Unexpected value layout: could not determine ABI class"));
        return switch (kind) {
            case CHAR -> "C_CHAR";
            case SHORT -> "C_SHORT";
            case INT -> "C_INT";
            case LONG -> "C_LONG";
            case LONG_LONG -> "C_LONG_LONG";
            case FLOAT -> "C_FLOAT";
            case DOUBLE -> "C_DOUBLE";
            case POINTER -> "C_POINTER";
        };
    }

    private String emitSegmentField(String javaName, String nativeName, MemoryLayout layout) {
        String fld = addLayout(javaName, layout);
        incrAlign();
        indent();
        String fieldName = ConstantKind.SEGMENT.fieldName(javaName);
        append(memberMods());
        append("MemorySegment ");
        append(fieldName);
        append(" = ");
        append("RuntimeHelper.lookupGlobalVariable(");
        append("LIBRARIES, \"");
        append(nativeName);
        append("\", ");
        append(fld);
        append(");\n");
        decrAlign();
        return fieldName;
    }
}
