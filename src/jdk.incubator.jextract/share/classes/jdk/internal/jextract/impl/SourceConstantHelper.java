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
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ValueLayout;
import jdk.incubator.foreign.SequenceLayout;
import jdk.internal.jextract.impl.LayoutUtils.CanonicalABIType;

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
import static jdk.internal.jextract.impl.LayoutUtils.CANONICAL_FIELD;

// generates ConstantHelper as java source
class SourceConstantHelper extends JavaSourceBuilder implements ConstantHelper {
    private static final String PKG_STATIC_FINAL_MODS = "static final ";

    // set of names generates already
    private static final Map<String, DirectMethodHandleDesc> namesGenerated = new HashMap<>();
    // code buffer
    private StringBuilder sb = new StringBuilder();
    // current line alignment (number of 4-spaces)
    private int align;
    private final ClassDesc CD_constantsHelper;
    private final String[] libraryNames;
    private final String baseClassName;

    private SourceConstantHelper(String packageName, String[] libraryNames, String className, String baseClassName, ClassDesc CD_constantsHelper) {
        super(Kind.CLASS, className, packageName, null, null);
        this.CD_constantsHelper = CD_constantsHelper;
        this.libraryNames = libraryNames;
        this.baseClassName = baseClassName;
    }

    @Override
    String superClass() {
        return baseClassName;
    }

    @Override
    JavaSourceBuilder prev() {
        return null;
    }

    public static ConstantHelper make(String packageName, String className, String[] libraryNames,
                                      String baseClassName) {
        ClassDesc CD_constantsHelper = ClassDesc.of(className);
        SourceConstantHelper helper = new SourceConstantHelper(packageName, libraryNames, className, baseClassName, CD_constantsHelper);
        helper.classBegin();
        return helper;
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
    public DirectMethodHandleDesc addFieldVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type, String parentJavaName, MemoryLayout ignored) {
        return addVarHandle(javaName, nativeName, layout, type, parentJavaName);
    }

    @Override
    public DirectMethodHandleDesc addGlobalVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        return addVarHandle(javaName, nativeName, layout, type, null);
    }

    private DirectMethodHandleDesc addVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type, String parentJavaFieldName) {
        String varHandleName = javaName + "$VH";

        if (namesGenerated.containsKey(varHandleName)) {
            return namesGenerated.get(varHandleName);
        } else {
            String fieldName = emitVarHandleField(javaName, nativeName, type, layout, parentJavaFieldName);
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

    private JavaFileObject newJavaFileObject(String className, String src) {
        String pkgPrefix = pkgName.isEmpty() ? "" : pkgName.replaceAll("\\.", "/") + "/";
        return InMemoryJavaCompiler.jfoFromString(URI.create(pkgPrefix + className + ".java"), src);
    }

    @Override
    public List<JavaFileObject> build() {
        classEnd();
        JavaFileObject result = newJavaFileObject(className, sb.toString());
        sb = null;
        return List.of(result);
    }

    protected void classBegin() {
        super.classBegin();
        if (superClass() == null) { // only for the first one
            emitLibraries(libraryNames);
        }
    }

    protected JavaSourceBuilder classEnd() {
        append("}\n");
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
        incrAlign();
        indent();
        append(PKG_STATIC_FINAL_MODS);
        append(type.getName());
        append(' ');
        append(name);
        append("() { return ");
        append(value);
        append("; }\n\n");
        decrAlign();
        return getGetterDesc(name, type);
    }

    @Override
    protected void addImportSection() {
        append("import java.lang.invoke.MethodHandle;\n");
        append("import java.lang.invoke.VarHandle;\n");
        append("import jdk.incubator.foreign.*;\n");
        append("import jdk.incubator.foreign.MemoryLayout.PathElement;\n");
        append("import static ");
        append(OutputFactory.C_LANG_CONSTANTS_HOLDER);
        append(".*;\n\n");
    }

    protected void append(String s) {
        sb.append(s);
    }

    protected void append(char c) {
        sb.append(c);
    }

    protected void append(long l) {
        sb.append(l);
    }

    protected void append(boolean b) {
        sb.append(b);
    }

    protected void indent() {
        for (int i = 0; i < align; i++) {
            append("    ");
        }
    }

    protected void incrAlign() {
        align++;
    }
    protected void decrAlign() {
        align--;
    }

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

    private String emitVarHandleField(String javaName, String nativeName, Class<?> type, MemoryLayout layout, String parentJavaName) {
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
        append(getLayoutFieldName(parentJavaName != null ? parentJavaName : javaName));
        append(".varHandle(" + typeName + ".class");
        if (parentJavaName != null) {
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
        return vl.attribute(CANONICAL_FIELD)
            .map(CanonicalABIType.class::cast)
            .map(CanonicalABIType::name)
            .orElseThrow(() -> new RuntimeException("should not reach here, problematic layout: " + vl));
    }

    private String getSegmentFieldName(String javaName) {
        return javaName + "$SEGMENT_";
    }

    private String emitSegmentField(String javaName, String nativeName, MemoryLayout layout) {
         addLayout(javaName, layout);
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
         append(getLayoutFieldName(javaName));
         append(");\n");
         decrAlign();
         return fieldName;
    }

    private void emitLibraries(String[] libraryNames) {
        incrAlign();
        indent();
        append(PKG_STATIC_FINAL_MODS);
        append("LibraryLookup[] LIBRARIES = RuntimeHelper.libraries(new String[] {\n");
        incrAlign();
        for (String lib : libraryNames) {
            indent();
            append('\"');
            append(quoteLibraryName(lib));
            append("\",\n");
        }
        decrAlign();
        indent();
        append("});\n\n");
        decrAlign();
    }

    private static String quoteLibraryName(String lib) {
        return lib.replace("\\", "\\\\"); // double up slashes
    }
}
