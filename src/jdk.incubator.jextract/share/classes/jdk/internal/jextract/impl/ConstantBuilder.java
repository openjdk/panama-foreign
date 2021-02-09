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

public class ConstantBuilder extends NestedClassBuilder {

    private static final String PKG_STATIC_FINAL_MODS = "static final ";

    // set of names generates already
    private final Map<String, String> namesGenerated = new HashMap<>();

    public ConstantBuilder(JavaSourceBuilder enclosing, JavaSourceBuilder.Kind kind, String className) {
        super(enclosing, kind, className);
    }

    // public API

    public String addLayout(String javaName, MemoryLayout layout) {
        String layoutName = javaName + "$LAYOUT";
        if (namesGenerated.containsKey(layoutName)) {
            return namesGenerated.get(layoutName);
        } else {
            String fieldName = emitLayoutField(javaName, layout);
            String access = className + "." + fieldName;
            namesGenerated.put(layoutName, access);
            return access;
        }
    }

    public String addFieldVarHandle(String javaName, String nativeName, MemoryLayout layout,
                                    Class<?> type, String rootJavaName, MemoryLayout ignored,
                                    List<String> prefixElementNames) {
        return addVarHandle(javaName, nativeName, layout, type, rootJavaName, prefixElementNames);
    }

    public String addGlobalVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        return addVarHandle(javaName, nativeName, layout, type, null, List.of());
    }

    private String addVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type,
                                String rootLayoutName, List<String> prefixElementNames) {
        String varHandleName = javaName + "$VH";

        if (namesGenerated.containsKey(varHandleName)) {
            return namesGenerated.get(varHandleName);
        } else {
            String fieldName = emitVarHandleField(javaName, nativeName, type, layout, rootLayoutName, prefixElementNames);
            String access = className + "." + fieldName;
            namesGenerated.put(varHandleName, access);
            return access;
        }
    }

    public String addMethodHandle(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs) {
        String mhName = javaName + "$MH";
        if (namesGenerated.containsKey(mhName)) {
            return namesGenerated.get(mhName);
        } else {
            String fieldName = emitMethodHandleField(javaName, nativeName, mtype, desc, varargs);
            String access = className + "." + fieldName;
            namesGenerated.put(mhName, access);
            return access;
        }
    }

    public String addSegment(String javaName, String nativeName, MemoryLayout layout) {
        String segmentName = javaName + "$SEGMENT";
        if (namesGenerated.containsKey(segmentName)) {
            return namesGenerated.get(segmentName);
        } else {
            String fieldName = emitSegmentFieldF(javaName, nativeName, layout);
            String access = className + "." + fieldName;
            namesGenerated.put(segmentName, access);
            return access;
        }
    }

    public String addFunctionDesc(String javaName, FunctionDescriptor desc) {
        String funcDescName = javaName + "$FUNC";

        if (namesGenerated.containsKey(funcDescName)) {
            return namesGenerated.get(funcDescName);
        } else {
            String fieldName = emitFunctionDescField(javaName, desc);
            String access = className + "." + fieldName;
            namesGenerated.put(funcDescName, access);
            return access;
        }
    }

    public String addConstantDesc(String name, Class<?> type, Object value) {
        if (namesGenerated.containsKey(name)) {
            return namesGenerated.get(name);
        } else {
            String str;
            if (type == MemorySegment.class) {
                str = emitConstantSegment(name, value);
            } else if (type == MemoryAddress.class) {
                str = emitConstantAddress(name, value);
            } else {
                throw new UnsupportedOperationException();
            }
            String access = className + "." + str;
            namesGenerated.put(name, access);
            return access;
        }
    }

    // private generators

    private String getMethodHandleFieldName(String javaName) {
        return javaName + "$MH_";
    }

    private String emitMethodHandleField(String javaName, String nativeName, MethodType mtype,
                                         FunctionDescriptor desc, boolean varargs) {
        addFunctionDesc(javaName, desc);
        incrAlign();
        String fieldName = getMethodHandleFieldName(javaName);
        indent();
        append(PKG_STATIC_FINAL_MODS + "MethodHandle ");
        append(fieldName + " = RuntimeHelper.downcallHandle(\n");
        incrAlign();
        indent();
        append("LIBRARIES, \"" + nativeName + "\"");
        append(",\n");
        indent();
        append("\"" + mtype.toMethodDescriptorString() + "\",\n");
        indent();
        append(getFunctionDescFieldName(javaName));
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

    private String getVarHandleFieldName(String name) {
        return name + "$VH_";
    }

    private String emitVarHandleField(String javaName, String nativeName, Class<?> type, MemoryLayout layout,
                                      String rootLayoutName, List<String> prefixElementNames) {
        addLayout(javaName, layout);
        incrAlign();
        String typeName = type.getName();
        boolean isAddr = typeName.contains("MemoryAddress");
        if (isAddr) {
            typeName = "long";
        }
        indent();
        String fieldName = getVarHandleFieldName(javaName);
        append(PKG_STATIC_FINAL_MODS + "VarHandle " + fieldName + " = ");
        if (isAddr) {
            append("MemoryHandles.asAddressVarHandle(");
        }
        append(getLayoutFieldName(rootLayoutName != null ? rootLayoutName : javaName));
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

    private String getLayoutFieldName(String javaName) {
        return javaName + "$LAYOUT_";
    }

    private String emitLayoutField(String javaName, MemoryLayout layout) {
        String fieldName = getLayoutFieldName(javaName);
        incrAlign();
        indent();
        append(PKG_STATIC_FINAL_MODS + "MemoryLayout " + fieldName + " = ");
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

    private String getFunctionDescFieldName(String javaName) {
        return javaName + "$FUNC_";
    }

    private String emitFunctionDescField(String javaName, FunctionDescriptor desc) {
        incrAlign();
        indent();
        String fieldName = getFunctionDescFieldName(javaName);
        final boolean noArgs = desc.argumentLayouts().isEmpty();
        append(PKG_STATIC_FINAL_MODS);
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

    private String getConstantSegmentFieldName(String javaName) {
        return javaName + "$SEGMENT_CONSTANT_";
    }
    private String emitConstantSegment(String javaName, Object value) {
        incrAlign();
        indent();
        String fieldName = getConstantSegmentFieldName(javaName);
        append(PKG_STATIC_FINAL_MODS);
        append("MemorySegment ");
        append(fieldName);
        append(" = CLinker.toCString(\"");
        append(Utils.quote(Objects.toString(value)));
        append("\");\n");
        decrAlign();
        return fieldName;
    }

    private String getConstantAddressFieldName(String javaName) {
        return javaName + "$ADDR_CONSTANT_";
    }
    private String emitConstantAddress(String javaName, Object value) {
        incrAlign();
        indent();
        String fieldName = getConstantAddressFieldName(javaName);
        append(PKG_STATIC_FINAL_MODS);
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

    private String getSegmentFieldName(String javaName) {
        return javaName + "$SEGMENT_";
    }

    private String emitSegmentFieldF(String javaName, String nativeName, MemoryLayout layout) {
        String fld = addLayout(javaName, layout);
        incrAlign();
        indent();
        String fieldName = getSegmentFieldName(javaName);
        append(PKG_STATIC_FINAL_MODS);
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
