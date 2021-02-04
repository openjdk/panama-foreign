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
import jdk.incubator.jextract.Type;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.invoke.MethodType;
import java.util.HashSet;
import java.util.Set;

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

    static final String PUB_CLS_MODS = "public final ";
    static final String PUB_MODS = "public static ";
    protected final StringSourceBuilder builder;
    private final Kind kind;
    protected final String className;
    protected final String pkgName;
    protected final ConstantHelper constantHelper;

    Set<String> nestedClassNames = new HashSet<>();
    int nestedClassNameCount = 0;

    JavaSourceBuilder(StringSourceBuilder builder, Kind kind, String className, String pkgName, ConstantHelper constantHelper) {
        this.builder = builder;
        this.kind = kind;
        this.className = className;
        this.pkgName = pkgName;
        this.constantHelper = constantHelper;
    }

    String superClass() {
        return null;
    }

    Type type() {
        return null;
    }

    protected String getClassModifiers() {
        return PUB_CLS_MODS;
    }

    void classBegin() {
        addPackagePrefix();
        addImportSection();

        builder.indent();
        builder.append(getClassModifiers());
        builder.append(kind.kindName + " " + className);
        if (superClass() != null) {
            builder.append(" extends ");
            builder.append(superClass());
        }
        builder.append(" {\n\n");
        if (kind != Kind.INTERFACE) {
            emitConstructor();
        }
    }

    void emitConstructor() {
        builder.incrAlign();
        builder.indent();
        builder.append("/* package-private */ ");
        builder.append(className);
        builder.append("() {}");
        builder.append('\n');
        builder.decrAlign();
    }

    JavaSourceBuilder classEnd() {
        builder.indent();
        builder.append("}\n\n");
        return this;
    }

    void addLayoutGetter(String javaName, MemoryLayout layout) {
        emitForwardGetter(constantHelper.addLayout(javaName, layout));
    }

    void addVarHandleGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        emitForwardGetter(constantHelper.addGlobalVarHandle(javaName, nativeName, layout, type));
    }

    void addMethodHandleGetter(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs) {
        emitForwardGetter(constantHelper.addMethodHandle(javaName, nativeName, mtype, desc, varargs),
            true, "unresolved symbol: " + nativeName);
    }

    void addSegmentGetter(String javaName, String nativeName, MemoryLayout layout) {
        emitForwardGetter(constantHelper.addSegment(javaName, nativeName, layout),
            true, "unresolved symbol: " + nativeName);
    }

    void addConstantGetter(String javaName, Class<?> type, Object value) {
        emitForwardGetter(constantHelper.addConstant(javaName, type, value));
    }

    void addGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + " " + type.getSimpleName() + " " + javaName + "$get() {\n");
        builder.incrAlign();
        builder.indent();
        String vhParam = addressGetCallString(javaName, nativeName, layout);
        builder.append("return (" + type.getName() + ") ");
        builder.append(globalVarHandleGetCallString(javaName, nativeName, layout, type));
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

    void addSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + "void " + javaName + "$set(" + " " + type.getSimpleName() + " x) {\n");
        builder.incrAlign();
        builder.indent();
        String vhParam = addressGetCallString(javaName, nativeName, layout);
        builder.append(globalVarHandleGetCallString(javaName, nativeName, layout, type));
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

    protected void addPackagePrefix() {
        assert pkgName.indexOf('/') == -1 : "package name invalid: " + pkgName;
        builder.append("// Generated by jextract\n\n");
        if (!pkgName.isEmpty()) {
            builder.append("package ");
            builder.append(pkgName);
            builder.append(";\n\n");
        }
    }

    protected void addImportSection() {
        builder.append("import java.lang.invoke.MethodHandle;\n");
        builder.append("import java.lang.invoke.VarHandle;\n");
        builder.append("import java.util.Objects;\n");
        builder.append("import jdk.incubator.foreign.*;\n");
        builder.append("import jdk.incubator.foreign.MemoryLayout.PathElement;\n");
        builder.append("import static ");
        builder.append(OutputFactory.C_LANG_CONSTANTS_HOLDER);
        builder.append(".*;\n");
    }

    protected void emitForwardGetter(DirectMethodHandleDesc desc) {
        emitForwardGetter(desc, false, "");
    }

    protected void emitForwardGetter(DirectMethodHandleDesc desc, boolean nullCheck, String errMsg) {
        builder.incrAlign();
        builder.indent();
        builder.append(PUB_MODS + " " + displayName(desc.invocationType().returnType()) + " " + desc.methodName() + "() {\n");
        builder.incrAlign();
        builder.indent();
        builder.append("return ");
        if (nullCheck) {
            builder.append("RuntimeHelper.requireNonNull(");
        }
        builder.append(getCallString(desc));
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

    protected String getCallString(DirectMethodHandleDesc desc) {
        return desc.owner().displayName() + "." + desc.methodName() + "()";
    }

    protected String displayName(ClassDesc returnType) {
        return returnType.displayName(); // TODO shorten based on imports
    }

    protected String functionGetCallString(String javaName, FunctionDescriptor fDesc) {
        return getCallString(constantHelper.addFunctionDesc(javaName, fDesc));
    }

    protected String methodHandleGetCallString(String javaName, String nativeName, MethodType mt, FunctionDescriptor fDesc, boolean varargs) {
        return getCallString(constantHelper.addMethodHandle(javaName, nativeName, mt, fDesc, varargs));
    }

    private String globalVarHandleGetCallString(String javaName, String nativeName, MemoryLayout layout, Class<?> type) {
        return getCallString(constantHelper.addGlobalVarHandle(javaName, nativeName, layout, type));
    }

    protected String addressGetCallString(String javaName, String nativeName, MemoryLayout layout) {
        return getCallString(constantHelper.addSegment(javaName, nativeName, layout));
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
    String uniqueNestedClassName(String name) {
        name = Utils.javaSafeIdentifier(name);
        return nestedClassNames.add(name.toLowerCase()) ? name : (name + "$" + nestedClassNameCount++);
    }

    StructBuilder newStructBuilder(String name, GroupLayout parentLayout, Type type) {
        return new StructBuilder(this, name, parentLayout, type);
    }
}
