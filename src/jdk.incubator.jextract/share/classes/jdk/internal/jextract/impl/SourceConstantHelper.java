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

import jdk.incubator.foreign.CSupport;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

// generates ConstantHelper as java source
class SourceConstantHelper implements ConstantHelper {
    private static final String PACKAGE_FINAL_MODS = "static final ";
    private static final String PRIVATE_FINAL_MODS = "private static final ";
    private static final String ABI_CLASS_ATTR;
    private static final int CONSTANTS_PER_CLASS = Integer.getInteger("jextract.constants.per.class", 1000);

    static {
        String abi = CSupport.getSystemLinker().name();
        ABI_CLASS_ATTR = switch (abi) {
            case CSupport.SysV.NAME -> CSupport.SysV.CLASS_ATTRIBUTE_NAME;
            case CSupport.Win64.NAME -> CSupport.Win64.CLASS_ATTRIBUTE_NAME;
            case CSupport.AArch64.NAME -> CSupport.AArch64.CLASS_ATTRIBUTE_NAME;
            default -> throw new UnsupportedOperationException("Unsupported Foreign Linker: " + abi);
        };
    }

    // set of names generates already
    private static final Map<String, DirectMethodHandleDesc> namesGenerated = new HashMap<>();
    // code buffer
    private StringBuilder sb = new StringBuilder();
    // current line alignment (number of 4-spaces)
    private int align;
    private final String pkgName;
    private final String headerClassName;
    private int constantCount;
    private int constantClassCount;
    private String constantClassName;
    private ClassDesc CD_constantsHelper;
    private final List<String> classes = new ArrayList<>();

    SourceConstantHelper(String parentClassName, String[] libraryNames) {
        int idx = parentClassName.lastIndexOf('.');
        this.pkgName = idx == -1? "" : parentClassName.substring(0, idx);
        this.headerClassName =  parentClassName.substring(idx + 1);
        this.constantClassName = getConstantClassName(headerClassName, constantClassCount);
        this.CD_constantsHelper = ClassDesc.of(pkgName.isEmpty() ? constantClassName : (pkgName + "." + constantClassName));
        classBegin(libraryNames, null, false);
    }

    private static String getConstantClassName(String className, int count) {
        return className + "$constants$" + count;
    }

    private void newConstantClass() {
        if (constantCount > CONSTANTS_PER_CLASS) {
            classEnd();
            constantClassCount++;
            String baseClassName = constantClassName;
            this.constantClassName = getConstantClassName(headerClassName, constantClassCount);
            this.CD_constantsHelper = ClassDesc.of(pkgName.isEmpty() ? constantClassName : (pkgName + "." + constantClassName));
            this.constantCount = 0;
            this.sb = new StringBuilder();
            classBegin(null, baseClassName, false);
        }
        constantCount++;
    }

    @Override
    public DirectMethodHandleDesc addLayout(String javaName, MemoryLayout layout) {
        newConstantClass();
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
    public DirectMethodHandleDesc addVarHandle(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        newConstantClass();
        String varHandleName = javaName + "$VH";

        if (namesGenerated.containsKey(varHandleName)) {
            return namesGenerated.get(varHandleName);
        } else {
            String fieldName = emitVarHandleField(javaName, type, layout);
            DirectMethodHandleDesc getter = emitGetter(varHandleName, VarHandle.class, fieldName);
            namesGenerated.put(varHandleName, getter);
            return getter;
        }
    }

    @Override
    public DirectMethodHandleDesc addMethodHandle(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs) {
        newConstantClass();
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
        newConstantClass();
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
        newConstantClass();
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
        newConstantClass();

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
    public List<JavaFileObject> getClasses() {
        classEnd();

        List<JavaFileObject> javaFileObjects = new ArrayList<>();
        int count = 0;
        for (String src : classes) {
            String name = getConstantClassName(headerClassName, count);
            javaFileObjects.add(newJavaFileObject(name, src));
            count++;
        }

        // generate overall header$constants subclass that inherits from
        // the individual "split" header$constants$N classes.
        this.sb = new StringBuilder();
        String baseClassName = constantClassName;
        this.constantClassName = headerClassName + "$constants";
        classBegin(null, baseClassName, true);
        classEnd();
        javaFileObjects.add(newJavaFileObject(constantClassName, sb.toString()));

        return javaFileObjects;
    }

    // Internals only below this point
    private void emitConstructor() {
        // emit private constructor to prevent construction objects
        incrAlign();
        indent();
        append(constantClassName);
        append("() {}\n");
        decrAlign();
    }

    private void classBegin(String[] libraryNames, String baseClassName, boolean leafClass) {
        addPackagePrefix(pkgName);
        addImportSection();
        append("public ");
        if (leafClass) {
            append("final ");
        }
        append("class ");
        append(constantClassName);
        if (baseClassName != null) {
            append(" extends ");
            append(baseClassName);
        }
        append(" {\n");
        emitConstructor();
        if (libraryNames != null) {
            emitLibraries(libraryNames);
        }
    }

    private void classEnd() {
        append("}\n");
        classes.add(sb.toString());
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
        append(JavaSourceBuilder.PUB_MODS);
        append(type.getName());
        append(' ');
        append(name);
        append("() { return ");
        append(value);
        append("; }\n\n");
        decrAlign();
        return getGetterDesc(name, type);
    }

    private void addPackagePrefix(String pkgName) {
        append("// Generated by jextract\n\n");
        if (!pkgName.isEmpty()) {
            append("package ");
            append(pkgName);
            append(";\n\n");
        }
    }

    private void addImportSection() {
        append("import java.lang.invoke.MethodHandle;\n");
        append("import java.lang.invoke.VarHandle;\n");
        append("import jdk.incubator.foreign.*;\n");
        append("import jdk.incubator.foreign.MemoryLayout.PathElement;\n");
        append("import static ");
        append(OutputFactory.C_LANG_CONSTANTS_HOLDER);
        append(".*;\n\n");
    }

    private void append(String s) {
        sb.append(s);
    }

    private void append(char c) {
        sb.append(c);
    }

    private void append(long l) {
        sb.append(l);
    }

    private void append(boolean b) {
        sb.append(b);
    }

    private void indent() {
        for (int i = 0; i < align; i++) {
            append("    ");
        }
    }

    private void incrAlign() {
        align++;
    }
    private void decrAlign() {
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
        append(PRIVATE_FINAL_MODS + "MethodHandle ");
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

    private String emitVarHandleField(String javaName, Class<?> type, MemoryLayout layout) {
        addLayout(javaName, layout);
        incrAlign();
        String typeName = type.getName();
        boolean isAddr = typeName.contains("MemoryAddress");
        if (isAddr) {
            typeName = "long";
        }
        indent();
        String fieldName = getVarHandleFieldName(javaName);
        append(PRIVATE_FINAL_MODS + "VarHandle " + fieldName + " = ");
        if (isAddr) {
            append("MemoryHandles.asAddressVarHandle(");
        }
        append(getLayoutFieldName(javaName));
        append(".varHandle(" + typeName + ".class)");
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
        append(PRIVATE_FINAL_MODS + "MemoryLayout " + fieldName + " = ");
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
            if (l == CSupport.SysV.C_COMPLEX_LONGDOUBLE) {
                append("C_COMPLEX_LONGDOUBLE");
            } else {
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
            }
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
        append(PRIVATE_FINAL_MODS);
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
        append(PRIVATE_FINAL_MODS);
        append("MemorySegment ");
        append(fieldName);
        append(" = CSupport.toCString(\"");
        append(Objects.toString(value));
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
        append(PRIVATE_FINAL_MODS);
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
        if (matchLayout(vl, CSupport.C_BOOL)) {
            return "C_BOOL";
        } else if (matchLayout(vl, CSupport.C_CHAR)) {
            return "C_CHAR";
        } else if (matchLayout(vl, CSupport.C_SHORT)) {
            return "C_SHORT";
        } else if (matchLayout(vl, CSupport.C_INT)) {
            return "C_INT";
        } else if (matchLayout(vl, CSupport.C_LONG)) {
            return "C_LONG";
        } else if (matchLayout(vl, CSupport.C_LONGLONG)) {
            return "C_LONGLONG";
        } else if (matchLayout(vl, CSupport.C_FLOAT)) {
            return "C_FLOAT";
        } else if (matchLayout(vl, CSupport.C_DOUBLE)) {
            return "C_DOUBLE";
        } else if (matchLayout(vl, CSupport.C_LONGDOUBLE)) {
            return "C_LONGDOUBLE";
        } else if (matchLayout(vl, CSupport.C_POINTER)) {
            return "C_POINTER";
        } else {
            throw new RuntimeException("should not reach here, problematic layout: " + vl);
        }
    }

    private static boolean matchLayout(ValueLayout a, ValueLayout b) {
        if (a == b) return true;
        return (a.bitSize() == b.bitSize() &&
                a.order() == b.order() &&
                a.bitAlignment() == b.bitAlignment() &&
                a.attribute(ABI_CLASS_ATTR).equals(b.attribute(ABI_CLASS_ATTR)));
    }

    private String getSegmentFieldName(String javaName) {
        return javaName + "$SEGMENT_";
    }

    private String emitSegmentField(String javaName, String nativeName, MemoryLayout layout) {
         addLayout(javaName, layout);
         incrAlign();
         indent();
         String fieldName = getSegmentFieldName(javaName);
         append(PRIVATE_FINAL_MODS);
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
        append(PACKAGE_FINAL_MODS);
        append("LibraryLookup[] LIBRARIES = RuntimeHelper.libraries(new String[] {\n");
        incrAlign();
        for (String lib : libraryNames) {
            indent();
            append('\"');
            append(lib);
            append("\",\n");
        }
        decrAlign();
        indent();
        append("});\n\n");
        decrAlign();
    }
}
