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

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ValueLayout;
import jdk.incubator.foreign.SequenceLayout;

import javax.tools.JavaFileObject;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

// generates ConstantHelper as java source
class SourceConstantHelper implements ConstantHelper {
    private static final String PKG_STATIC_FINAL_MODS = "static final ";

    // set of names generates already
    private static final Map<String, DirectMethodHandleDesc> namesGenerated = new HashMap<>();

    private final ClassDesc CD_constantsHelper;
    
    private final ConstantClassBuilder builder; 
    
    static class ConstantClassBuilder extends JavaSourceBuilder {
        private final String baseClassName;
        private final String[] libraryNames;

        public ConstantClassBuilder(String className, String pkgName, String[] libraryNames, String baseClassName) {
            super(new StringSourceBuilder(), Kind.CLASS, className, pkgName, null);
            this.libraryNames = libraryNames;
            this.baseClassName = baseClassName;
        }

        @Override
        protected String getClassModifiers() {
            return "";
        }

        protected void classBegin() {
            super.classBegin();
            if (superClass() == null) { // only for the first one
                emitLibraries(libraryNames);
            }
        }

        @Override
        String superClass() {
            return baseClassName;
        }

        private void emitLibraries(String[] libraryNames) {
            builder.incrAlign();
            builder.indent();
            builder.append(PKG_STATIC_FINAL_MODS);
            builder.append("LibraryLookup[] LIBRARIES = RuntimeHelper.libraries(new String[] {\n");
            builder.incrAlign();
            for (String lib : libraryNames) {
                builder.indent();
                builder.append('\"');
                builder.append(quoteLibraryName(lib));
                builder.append("\",\n");
            }
            builder.decrAlign();
            builder.indent();
            builder.append("});\n\n");
            builder.decrAlign();
        }

        private static String quoteLibraryName(String lib) {
            return lib.replace("\\", "\\\\"); // double up slashes
        }

        JavaFileObject toJavaFileObject() {
            String pkgPrefix = pkgName.isEmpty() ? "" : pkgName.replaceAll("\\.", "/") + "/";
            return InMemoryJavaCompiler.jfoFromString(URI.create(pkgPrefix + className + ".java"), builder.build());
        }
    }

    private SourceConstantHelper(String packageName, String[] libraryNames, String className, String baseClassName, ClassDesc CD_constantsHelper) {
        this.builder = new ConstantClassBuilder(className, packageName, libraryNames, baseClassName);
        this.CD_constantsHelper = CD_constantsHelper;
    }

    public static ConstantHelper make(String packageName, String className, String[] libraryNames,
                                      String baseClassName) {
        ClassDesc CD_constantsHelper = ClassDesc.of(className);
        SourceConstantHelper helper = new SourceConstantHelper(packageName, libraryNames, className, baseClassName, CD_constantsHelper);
        helper.builder.classBegin();
        return helper;
    }
    
    StringSourceBuilder builder() {
        return builder.builder;
    }

    @Override
    public DirectMethodHandleDesc addLayout(String javaName, MemoryLayout layout) {
        String layoutName = javaName + "$LAYOUT";
        if (namesGenerated.containsKey(layoutName)) {
            return namesGenerated.get(layoutName);
        } else {
            String fieldName = emitLayoutField(javaName, layout);
            DirectMethodHandleDesc getter = emitGetter(layoutName, MemoryLayout.class, fieldName);
            namesGenerated.put(layoutName, getter);
            return getter;
        }
    }

    @Override
    public DirectMethodHandleDesc addFieldVarHandle(String javaName, String nativeName, MemoryLayout layout,
                                                    Class<?> type, String rootJavaName, MemoryLayout ignored,
                                                    List<String> prefixElementNames) {
        return addVarHandle(javaName, nativeName, layout, type, rootJavaName, prefixElementNames);
    }

    @Override
    public DirectMethodHandleDesc addGlobalVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        return addVarHandle(javaName, nativeName, layout, type, null, List.of());
    }

    private DirectMethodHandleDesc addVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type,
                                                String rootLayoutName, List<String> prefixElementNames) {
        String varHandleName = javaName + "$VH";

        if (namesGenerated.containsKey(varHandleName)) {
            return namesGenerated.get(varHandleName);
        } else {
            String fieldName = emitVarHandleField(javaName, nativeName, type, layout, rootLayoutName, prefixElementNames);
            DirectMethodHandleDesc getter = emitGetter(varHandleName, VarHandle.class, fieldName);
            namesGenerated.put(varHandleName, getter);
            return getter;
        }
    }

    @Override
    public DirectMethodHandleDesc addMethodHandle(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs) {
        String mhName = javaName + "$MH";
        if (namesGenerated.containsKey(mhName)) {
            return namesGenerated.get(mhName);
        } else {
            String fieldName = emitMethodHandleField(javaName, nativeName, mtype, desc, varargs);
            DirectMethodHandleDesc getter = emitGetter(mhName, MethodHandle.class, fieldName);
            namesGenerated.put(mhName, getter);
            return getter;
        }
    }

    @Override
    public DirectMethodHandleDesc addSegment(String javaName, String nativeName, MemoryLayout layout) {
        String segmentName = javaName + "$SEGMENT";
        if (namesGenerated.containsKey(segmentName)) {
            return namesGenerated.get(segmentName);
        } else {
            String fieldName = emitSegmentField(javaName, nativeName, layout);
            DirectMethodHandleDesc getter = emitGetter(segmentName, MemorySegment.class, fieldName);
            namesGenerated.put(segmentName, getter);
            return getter;
        }
    }

    @Override
    public DirectMethodHandleDesc addFunctionDesc(String javaName, FunctionDescriptor desc) {
        String funcDescName = javaName + "$FUNC";

        if (namesGenerated.containsKey(funcDescName)) {
            return namesGenerated.get(funcDescName);
        } else {
            String fieldName = emitFunctionDescField(javaName, desc);
            DirectMethodHandleDesc getter = emitGetter(funcDescName, FunctionDescriptor.class, fieldName);
            namesGenerated.put(funcDescName, getter);
            return getter;
        }
    }

    @Override
    public DirectMethodHandleDesc addConstant(String name, Class<?> type, Object value) {
        if (namesGenerated.containsKey(name)) {
            return namesGenerated.get(name);
        } else {
            String str;
            if (type == MemorySegment.class) {
                str = emitConstantSegment(name, value);
            } else if (type == MemoryAddress.class) {
                str = emitConstantAddress(name, value);
            } else {
                str = getConstantString(type, value);
            }
            DirectMethodHandleDesc getter = emitGetter(name, type, str);
            namesGenerated.put(name, getter);
            return getter;
        }
    }



    @Override
    public List<JavaFileObject> build() {
        classEnd();
        JavaFileObject result = builder.toJavaFileObject();
        return List.of(result);
    }

    protected JavaSourceBuilder classEnd() {
        builder().append("}\n");
        return null;
    }

    private DirectMethodHandleDesc getGetterDesc(String name, Class<?> type) {
        MethodType mt = methodType(type);
        return MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.STATIC,
                CD_constantsHelper, name, mt.describeConstable().orElseThrow()
        );
    }

    private DirectMethodHandleDesc emitGetter(String name, Class<?> type, String value) {
        builder().incrAlign();
        builder().indent();
        builder().append(PKG_STATIC_FINAL_MODS);
        builder().append(type.getName());
        builder().append(' ');
        builder().append(name);
        builder().append("() { return ");
        builder().append(value);
        builder().append("; }\n\n");
        builder().decrAlign();
        return getGetterDesc(name, type);
    }

    private String getMethodHandleFieldName(String javaName) {
        return javaName + "$MH_";
    }

    private String emitMethodHandleField(String javaName, String nativeName, MethodType mtype,
                                         FunctionDescriptor desc, boolean varargs) {
        addFunctionDesc(javaName, desc);
        builder().incrAlign();
        String fieldName = getMethodHandleFieldName(javaName);
        builder().indent();
        builder().append(PKG_STATIC_FINAL_MODS + "MethodHandle ");
        builder().append(fieldName + " = RuntimeHelper.downcallHandle(\n");
        builder().incrAlign();
        builder().indent();
        builder().append("LIBRARIES, \"" + nativeName + "\"");
        builder().append(",\n");
        builder().indent();
        builder().append("\"" + mtype.toMethodDescriptorString() + "\",\n");
        builder().indent();
        builder().append(getFunctionDescFieldName(javaName));
        builder().append(", ");
        // isVariadic
        builder().append(varargs);
        builder().append("\n");
        builder().decrAlign();
        builder().indent();
        builder().append(");\n");
        builder().decrAlign();
        return fieldName;
    }

    private String getVarHandleFieldName(String name) {
        return name + "$VH_";
    }

    private String emitVarHandleField(String javaName, String nativeName, Class<?> type, MemoryLayout layout,
                                      String rootLayoutName, List<String> prefixElementNames) {
        addLayout(javaName, layout);
        builder().incrAlign();
        String typeName = type.getName();
        boolean isAddr = typeName.contains("MemoryAddress");
        if (isAddr) {
            typeName = "long";
        }
        builder().indent();
        String fieldName = getVarHandleFieldName(javaName);
        builder().append(PKG_STATIC_FINAL_MODS + "VarHandle " + fieldName + " = ");
        if (isAddr) {
            builder().append("MemoryHandles.asAddressVarHandle(");
        }
        builder().append(getLayoutFieldName(rootLayoutName != null ? rootLayoutName : javaName));
        builder().append(".varHandle(" + typeName + ".class");
        if (rootLayoutName != null) {
            for (String prefixElementName : prefixElementNames) {
                builder().append(", MemoryLayout.PathElement.groupElement(\"" + prefixElementName + "\")");
            }
            builder().append(", MemoryLayout.PathElement.groupElement(\"" + nativeName + "\")");
        }
        builder().append(")");
        if (isAddr) {
            builder().append(")");
        }
        builder().append(";\n");
        builder().decrAlign();
        return fieldName;
    }

    private String getLayoutFieldName(String javaName) {
        return javaName + "$LAYOUT_";
    }

    private String emitLayoutField(String javaName, MemoryLayout layout) {
        String fieldName = getLayoutFieldName(javaName);
        builder().incrAlign();
        builder().indent();
        builder().append(PKG_STATIC_FINAL_MODS + "MemoryLayout " + fieldName + " = ");
        emitLayoutString(layout);
        builder().append(";\n");
        builder().decrAlign();
        return fieldName;
    }

    private void emitLayoutString(MemoryLayout l) {
        if (l instanceof ValueLayout) {
            builder().append(typeToLayoutName((ValueLayout) l));
        } else if (l instanceof SequenceLayout) {
            builder().append("MemoryLayout.ofSequence(");
            if (((SequenceLayout) l).elementCount().isPresent()) {
                builder().append(((SequenceLayout) l).elementCount().getAsLong() + ", ");
            }
            emitLayoutString(((SequenceLayout) l).elementLayout());
            builder().append(")");
        } else if (l instanceof GroupLayout) {
            if (((GroupLayout) l).isStruct()) {
                builder().append("MemoryLayout.ofStruct(\n");
            } else {
                builder().append("MemoryLayout.ofUnion(\n");
            }
            builder().incrAlign();
            String delim = "";
            for (MemoryLayout e : ((GroupLayout) l).memberLayouts()) {
                builder().append(delim);
                builder().indent();
                emitLayoutString(e);
                delim = ",\n";
            }
            builder().append("\n");
            builder().decrAlign();
            builder().indent();
            builder().append(")");
        } else {
            // padding
            builder().append("MemoryLayout.ofPaddingBits(" + l.bitSize() + ")");
        }
        if (l.name().isPresent()) {
            builder().append(".withName(\"" +  l.name().get() + "\")");
        }
    }

    private String getFunctionDescFieldName(String javaName) {
        return javaName + "$FUNC_";
    }

    private String emitFunctionDescField(String javaName, FunctionDescriptor desc) {
        builder().incrAlign();
        builder().indent();
        String fieldName = getFunctionDescFieldName(javaName);
        final boolean noArgs = desc.argumentLayouts().isEmpty();
        builder().append(PKG_STATIC_FINAL_MODS);
        builder().append("FunctionDescriptor ");
        builder().append(fieldName);
        builder().append(" = ");
        if (desc.returnLayout().isPresent()) {
            builder().append("FunctionDescriptor.of(");
            emitLayoutString(desc.returnLayout().get());
            if (!noArgs) {
                builder().append(",");
            }
        } else {
            builder().append("FunctionDescriptor.ofVoid(");
        }
        if (!noArgs) {
            builder().append("\n");
            builder().incrAlign();
            String delim = "";
            for (MemoryLayout e : desc.argumentLayouts()) {
                builder().append(delim);
                builder().indent();
                emitLayoutString(e);
                delim = ",\n";
            }
            builder().append("\n");
            builder().decrAlign();
            builder().indent();
        }
        builder().append(");\n");
        builder().decrAlign();
        return fieldName;
    }

    private String getConstantSegmentFieldName(String javaName) {
        return javaName + "$SEGMENT_CONSTANT_";
    }
    private String emitConstantSegment(String javaName, Object value) {
        builder().incrAlign();
        builder().indent();
        String fieldName = getConstantSegmentFieldName(javaName);
        builder().append(PKG_STATIC_FINAL_MODS);
        builder().append("MemorySegment ");
        builder().append(fieldName);
        builder().append(" = CLinker.toCString(\"");
        builder().append(Utils.quote(Objects.toString(value)));
        builder().append("\");\n");
        builder().decrAlign();
        return fieldName;
    }

    private String getConstantAddressFieldName(String javaName) {
        return javaName + "$ADDR_CONSTANT_";
    }
    private String emitConstantAddress(String javaName, Object value) {
        builder().incrAlign();
        builder().indent();
        String fieldName = getConstantAddressFieldName(javaName);
        builder().append(PKG_STATIC_FINAL_MODS);
        builder().append("MemoryAddress ");
        builder().append(fieldName);
        builder().append(" = MemoryAddress.ofLong(");
        builder().append(((Number)value).longValue());
        builder().append("L);\n");
        builder().decrAlign();
        return fieldName;
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

    private String emitSegmentField(String javaName, String nativeName, MemoryLayout layout) {
         addLayout(javaName, layout);
         builder().incrAlign();
         builder().indent();
         String fieldName = getSegmentFieldName(javaName);
         builder().append(PKG_STATIC_FINAL_MODS);
         builder().append("MemorySegment ");
         builder().append(fieldName);
         builder().append(" = ");
         builder().append("RuntimeHelper.lookupGlobalVariable(");
         builder().append("LIBRARIES, \"");
         builder().append(nativeName);
         builder().append("\", ");
         builder().append(getLayoutFieldName(javaName));
         builder().append(");\n");
         builder().decrAlign();
         return fieldName;
    }
}
