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
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.jextract.Declaration;
import jdk.incubator.jextract.Type;

import javax.tools.JavaFileObject;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodType;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Superclass for .java source generator classes.
 */
abstract class JavaSourceBuilder {

    enum Kind {
        CLASS("class"),
        INTERFACE("interface");

        final String kindName;

        Kind(String kindName) {
            this.kindName = kindName;
        }
    }

    final Kind kind;
    final ClassDesc desc;

    Set<String> nestedClassNames = new HashSet<>();
    int nestedClassNameCount = 0;

    // code buffer
    private StringBuilder sb = new StringBuilder();
    // current line alignment (number of 4-spaces)
    private int align;

    JavaSourceBuilder(int align, Kind kind, ClassDesc desc) {
        this.align = align;
        this.kind = kind;
        this.desc = desc;
    }

    JavaSourceBuilder(Kind kind, ClassDesc desc) {
        this(0, kind, desc);
    }

    String className() {
        return desc.displayName();
    }

    String fullName() {
        return className();
    }

    final String packageName() {
        return desc.packageName();
    }

    String superClass() {
        return null;
    }

    String mods() {
        return "public ";
    }

    void classBegin() {
        emitPackagePrefix();
        emitImportSection();

        indent();
        append(mods());
        append(kind.kindName + " " + className());
        if (superClass() != null) {
            append(" extends ");
            append(superClass());
        }
        append(" {\n\n");
    }

    JavaSourceBuilder classEnd() {
        if (constantBuilder != null) {
            constantBuilder.classEnd();
        }
        indent();
        append("}\n\n");
        return this;
    }

    // public API (used by OutputFactory)

    public static record FunctionInfo(
            MethodType methodType,
            FunctionDescriptor descriptor,
            boolean isVarargs,
            Optional<List<String>> parameterNames) {

        static FunctionInfo ofFunction(MethodType methodType, FunctionDescriptor functionDescriptor, boolean isVarargs, List<String> parameterNames) {
            return new FunctionInfo(methodType, functionDescriptor, isVarargs, Optional.of(parameterNames));
        }

        static FunctionInfo ofFunctionPointer(MethodType methodType, FunctionDescriptor functionDescriptor) {
            return new FunctionInfo(methodType, functionDescriptor, false, Optional.empty());
        }
    }

    public static record VarInfo(
            Class<?> carrier,
            MemoryLayout layout,
            Optional<String> fiName) {

        static VarInfo ofVar(Class<?> carrier, MemoryLayout layout) {
            return new VarInfo(carrier, layout, Optional.empty());
        }

        static VarInfo ofFunctionalPointerVar(Class<?> carrier, MemoryLayout layout, String fiName) {
            return new VarInfo(carrier, layout, Optional.ofNullable(fiName));
        }
    }

    public void addVar(String javaName, String nativeName, VarInfo varInfo) {
        throw new UnsupportedOperationException();
    }

    public void addFunction(String javaName, String nativeName, FunctionInfo functionInfo) {
        throw new UnsupportedOperationException();
    }

    public void addConstant(String javaName, Class<?> type, Object value) {
        throw new UnsupportedOperationException();
    }

    public void addTypedef(String name, String superClass, Type type) {
        throw new UnsupportedOperationException();
    }

    public StructBuilder addStruct(String name, Declaration parent, GroupLayout layout, Type type) {
        return new StructBuilder(this, name.isEmpty() ? parent.name() : name, layout, type);
    }

    public String addFunctionalInterface(String name, FunctionInfo functionInfo) {
        FunctionalInterfaceBuilder builder = new FunctionalInterfaceBuilder(this, name,
                functionInfo.methodType(), functionInfo.descriptor());
        builder.classBegin();
        builder.classEnd();
        return builder.fullName();
    }

    public List<JavaFileObject> toFiles() {
        classEnd();
        String res = build();
        return List.of(Utils.fileFromString(packageName(), className(), res));
    }

    // Internal generation helpers (used by other builders)

    int align() {
        return align;
    }

    void append(String s) {
        sb.append(s);
    }

    void append(char c) {
        sb.append(c);
    }

    void append(boolean b) {
        sb.append(b);
    }

    void append(long l) {
        sb.append(l);
    }

    void indent() {
        for (int i = 0; i < align; i++) {
            append("    ");
        }
    }

    void incrAlign() {
        align++;
    }

    void decrAlign() {
        align--;
    }

    String build() {
        String s = sb.toString();
        sb = null;
        return s;
    }

    /*
     * We may have case-insensitive name collision! A C program may have
     * defined structs/unions/typedefs with the names FooS, fooS, FoOs, fOOs.
     * Because we map structs/unions/typedefs to nested classes of header classes,
     * such a case-insensitive name collision is problematic. This is because in
     * a case-insensitive file system javac will overwrite classes for
     * Header$CFooS, Header$CfooS, Header$CFoOs and so on! We solve this by
     * generating unique case-insensitive names for nested classes.
     */
    final String uniqueNestedClassName(String name) {
        name = Utils.javaSafeIdentifier(name);
        return nestedClassNames.add(name.toLowerCase()) ? name : (name + "$" + nestedClassNameCount++);
    }

    protected void emitPackagePrefix() {
        assert packageName().indexOf('/') == -1 : "package name invalid: " + packageName();
        append("// Generated by jextract\n\n");
        if (!packageName().isEmpty()) {
            append("package ");
            append(packageName());
            append(";\n\n");
        }
    }

    protected void emitImportSection() {
        append("import java.lang.invoke.MethodHandle;\n");
        append("import java.lang.invoke.VarHandle;\n");
        append("import java.util.Objects;\n");
        append("import jdk.incubator.foreign.*;\n");
        append("import jdk.incubator.foreign.MemoryLayout.PathElement;\n");
        append("import static ");
        append(OutputFactory.C_LANG_CONSTANTS_HOLDER);
        append(".*;\n");
    }

    protected void emitGetter(String mods, Class<?> type, String name, String access, boolean nullCheck, String symbolName) {
        incrAlign();
        indent();
        append(mods + " " + type.getSimpleName() + " " +name + "() {\n");
        incrAlign();
        indent();
        append("return ");
        if (nullCheck) {
            append("RuntimeHelper.requireNonNull(");
        }
        append(access);
        if (nullCheck) {
            append(",\"");
            append(symbolName);
            append("\")");
        }
        append(";\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    protected void emitGetter(String mods, Class<?> type, String name, String access) {
        emitGetter(mods, type, name, access, false, null);
    }

    int constant_counter = 0;
    int constant_class_index = 0;

    static final int CONSTANTS_PER_CLASS = Integer.getInteger("jextract.constants.per.class", 5);
    ConstantBuilder constantBuilder;

    protected void emitWithConstantClass(String javaName, Consumer<ConstantBuilder> constantConsumer) {
        if (constant_counter > CONSTANTS_PER_CLASS || constantBuilder == null) {
            if (constantBuilder != null) {
                constantBuilder.classEnd();
            }
            constant_counter = 0;
            constantBuilder = new ConstantBuilder(this, Kind.CLASS, "constants$" + constant_class_index++);
            constantBuilder.classBegin();
        }
        constantConsumer.accept(constantBuilder);
        constant_counter++;
    }
}
