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
package jdk.incubator.jextract.tool;

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryLayout;

import javax.tools.JavaFileObject;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.invoke.MethodType;

/**
 * Superclass for .java source generator classes.
 */
abstract class JavaSourceBuilder {
    static final String PUB_CLS_MODS = "public final ";
    static final String PUB_MODS = "public static ";
    protected final String className;
    protected final String pkgName;
    protected final ConstantHelper constantHelper;
    // buffer
    protected final StringBuffer sb;
    // current line alignment (number of 4-spaces)
    private int align;

    JavaSourceBuilder(String className, String pkgName, ConstantHelper constantHelper, int align) {
        this.className = className;
        this.pkgName = pkgName;
        this.constantHelper = constantHelper;
        this.align = align;
        this.sb = new StringBuffer();
    }

    JavaSourceBuilder(String className, String pkgName, ConstantHelper constantHelper) {
        this(className, pkgName, constantHelper, 0);
    }

    protected String getClassModifiers() {
        return PUB_CLS_MODS;
    }

    public void classBegin() {
        addPackagePrefix();
        addImportSection();

        indent();
        sb.append(getClassModifiers());
        sb.append("class ");
        sb.append(className);
        sb.append(" {\n\n");
        emitConstructor();
    }

    public void emitConstructor() {
        incrAlign();
        indent();
        sb.append("private ");
        sb.append(className);
        sb.append("() {}");
        sb.append('\n');
        decrAlign();
    }

    public void classEnd() {
        indent();
        sb.append("}\n\n");
    }

    public String getSource() {
        return sb.toString();
    }

    public void addContent(String src) {
        sb.append(src);
    }

    public JavaFileObject build() {
        String res = sb.toString();
        this.sb.delete(0, res.length());
        return Utils.fileFromString(pkgName, className, res);
    }

    public void addLayoutGetter(String javaName, MemoryLayout layout) {
        emitForwardGetter(constantHelper.addLayout(javaName, layout));
    }

    public void addVarHandleGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        emitForwardGetter(constantHelper.addVarHandle(javaName, nativeName, layout, type, parentLayout));
    }

    public void addMethodHandleGetter(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs) {
        emitForwardGetter(constantHelper.addMethodHandle(javaName, nativeName, mtype, desc, varargs));
    }

    public void addAddressGetter(String javaName, String nativeName, MemoryLayout layout, MemoryLayout parentLayout) {
        emitForwardGetter(constantHelper.addAddress(javaName, nativeName, layout));
    }

    public void addConstantGetter(String javaName, Class<?> type, Object value) {
        emitForwardGetter(constantHelper.addConstant(javaName, type, value));
    }

    public void addGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        sb.append(PUB_MODS + type.getName() + " " + javaName + "$get() {\n");
        incrAlign();
        indent();
        String vhParam = addressGetCallString(javaName, nativeName, layout);
        sb.append("return (" + type.getName() + ")"
                + varHandleGetCallString(javaName, nativeName, layout, type, null) + ".get(" + vhParam + ");\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
    }

    public void addSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        sb.append(PUB_MODS + "void " + javaName + "$set(" + type.getName() + " x) {\n");
        incrAlign();
        indent();
        String vhParam = addressGetCallString(javaName, nativeName, layout);
        sb.append(varHandleGetCallString(javaName, nativeName, layout, type, null) + ".set(" + vhParam + ", x);\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
    }

    // Utility

    protected void addPackagePrefix() {
        assert pkgName.indexOf('/') == -1 : "package name invalid: " + pkgName;
        sb.append("// Generated by jextract\n\n");
        if (!pkgName.isEmpty()) {
            sb.append("package ");
            sb.append(pkgName);
            sb.append(";\n\n");
        }
    }

    protected void addImportSection() {
        sb.append("import java.lang.invoke.MethodHandle;\n");
        sb.append("import java.lang.invoke.VarHandle;\n");
        sb.append("import jdk.incubator.foreign.*;\n");
        sb.append("import jdk.incubator.foreign.MemoryLayout.PathElement;\n");
        sb.append("import static ");
        sb.append(OutputFactory.C_LANG_CONSTANTS_HOLDER);
        sb.append(".*;\n");
    }

    protected void emitForwardGetter(DirectMethodHandleDesc desc) {
        incrAlign();
        indent();
        sb.append(PUB_MODS + displayName(desc.invocationType().returnType()) + " " + desc.methodName() + "() {\n");
        incrAlign();
        indent();
        sb.append("return " + getCallString(desc) + ";\n");
        decrAlign();
        indent();
        sb.append("}\n");
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

    protected void indent() {
        for (int i = 0; i < align; i++) {
            sb.append("    ");
        }
    }

    protected void incrAlign() {
        align++;
    }

    protected void decrAlign() {
        align--;
    }
}
