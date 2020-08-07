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
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.jextract.Declaration;

import javax.tools.JavaFileObject;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.invoke.MethodType;
import java.util.HashSet;
import java.util.Set;

/**
 * Superclass for .java source generator classes.
 */
abstract class JavaSourceBuilder {
    static final String PUB_CLS_MODS = "public final ";
    static final String PUB_MODS = "public static ";
    protected final String className;
    protected final String pkgName;
    protected final ConstantHelper constantHelper;

    Set<String> nestedClassNames = new HashSet<>();
    int nestedClassNameCount = 0;

    JavaSourceBuilder(String className, String pkgName, ConstantHelper constantHelper, int align) {
        this.className = className;
        this.pkgName = pkgName;
        this.constantHelper = constantHelper;
    }

    abstract JavaSourceBuilder prev();

    abstract void append(String s);

    abstract void append(char c);

    abstract void append(long l);

    abstract void indent();

    abstract void incrAlign();

    abstract void decrAlign();

    JavaSourceBuilder(String className, String pkgName, ConstantHelper constantHelper) {
        this(className, pkgName, constantHelper, 0);
    }

    protected String getClassModifiers() {
        return PUB_CLS_MODS;
    }

    void classBegin() {
        addPackagePrefix();
        addImportSection();

        indent();
        append(getClassModifiers());
        append("class ");
        append(className);
        append(" {\n\n");
        emitConstructor();
    }

    void emitConstructor() {
        incrAlign();
        indent();
        append("private ");
        append(className);
        append("() {}");
        append('\n');
        decrAlign();
    }

    JavaSourceBuilder classEnd() {
        indent();
        append("}\n\n");
        return prev();
    }

    void addLayoutGetter(String javaName, MemoryLayout layout) {
        emitForwardGetter(constantHelper.addLayout(javaName, layout));
    }

    void addVarHandleGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        emitForwardGetter(constantHelper.addVarHandle(javaName, nativeName, layout, type, parentLayout));
    }

    void addMethodHandleGetter(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs) {
        emitForwardGetter(constantHelper.addMethodHandle(javaName, nativeName, mtype, desc, varargs));
    }

    void addAddressGetter(String javaName, String nativeName, MemoryLayout layout, MemoryLayout parentLayout) {
        emitForwardGetter(constantHelper.addAddress(javaName, nativeName, layout));
    }

    void addConstantGetter(String javaName, Class<?> type, Object value) {
        emitForwardGetter(constantHelper.addConstant(javaName, type, value));
    }

    void addGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        append(PUB_MODS + type.getName() + " " + javaName + "$get() {\n");
        incrAlign();
        indent();
        String vhParam = addressGetCallString(javaName, nativeName, layout);
        append("return (" + type.getName() + ")"
                + varHandleGetCallString(javaName, nativeName, layout, type, null) + ".get(" + vhParam + ");\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    void addSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        append(PUB_MODS + "void " + javaName + "$set(" + type.getName() + " x) {\n");
        incrAlign();
        indent();
        String vhParam = addressGetCallString(javaName, nativeName, layout);
        append(varHandleGetCallString(javaName, nativeName, layout, type, null) + ".set(" + vhParam + ", x);\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
    }

    // Utility

    protected void addPackagePrefix() {
        assert pkgName.indexOf('/') == -1 : "package name invalid: " + pkgName;
        append("// Generated by jextract\n\n");
        if (!pkgName.isEmpty()) {
            append("package ");
            append(pkgName);
            append(";\n\n");
        }
    }

    protected void addImportSection() {
        append("import java.lang.invoke.MethodHandle;\n");
        append("import java.lang.invoke.VarHandle;\n");
        append("import jdk.incubator.foreign.*;\n");
        append("import jdk.incubator.foreign.MemoryLayout.PathElement;\n");
        append("import static ");
        append(OutputFactory.C_LANG_CONSTANTS_HOLDER);
        append(".*;\n");
    }

    protected void emitForwardGetter(DirectMethodHandleDesc desc) {
        incrAlign();
        indent();
        append(PUB_MODS + displayName(desc.invocationType().returnType()) + " " + desc.methodName() + "() {\n");
        incrAlign();
        indent();
        append("return " + getCallString(desc) + ";\n");
        decrAlign();
        indent();
        append("}\n");
        decrAlign();
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

    protected String varHandleGetCallString(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        return getCallString(constantHelper.addVarHandle(javaName, nativeName, layout, type, parentLayout));
    }

    protected String addressGetCallString(String javaName, String nativeName, MemoryLayout layout) {
        return getCallString(constantHelper.addAddress(javaName, nativeName, layout));
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
}
