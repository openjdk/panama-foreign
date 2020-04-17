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
import jdk.incubator.foreign.MemorySegment;

import javax.tools.JavaFileObject;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to generate header interface class in source form.
 * After aggregating various constituents of a .java source, build
 * method is called to get overall generated source string.
 */
class JavaSourceBuilder {

    private static final String PUB_CLS_MODS = "public final ";
    private static final String PUB_MODS = "public static final ";

    private final String pkgName;
    private final String[] libraryNames;
    // buffer
    protected StringBuffer sb;
    // current line alignment (number of 4-spaces)
    protected int align;

    private String className = null;
    private ConstantHelper constantHelper;

    JavaSourceBuilder(int align, String pkgName, String[] libraryNames) {
        this.align = align;
        this.libraryNames = libraryNames;
        this.sb = new StringBuffer();
        this.pkgName = pkgName;
    }

    JavaSourceBuilder(String pkgName, String[] libraryNames) {
        this(0, pkgName, libraryNames);
    }

    public void classBegin(String name) {
        className = name;
        String qualName = pkgName.isEmpty() ? name : pkgName + "." + name;
        constantHelper = new ConstantHelper(qualName,
                ClassDesc.of(pkgName, "RuntimeHelper"), ClassDesc.of(pkgName, "Cstring"), libraryNames);

        addPackagePrefix();
        addImportSection();

        indent();
        sb.append(PUB_CLS_MODS + "class ");
        sb.append(name);
        sb.append(" {\n\n");
    }

    public void classEnd() {
        indent();
        sb.append("}\n\n");
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

    public void addAddressGetter(String javaName, String nativeName) {
        emitForwardGetter(constantHelper.addAddress(javaName, nativeName));
    }

    public void addConstantGetter(String javaName, Class<?> type, Object value) {
        emitForwardGetter(constantHelper.addConstant(javaName, type, value));
    }

    public void addFunctionalFactory(String className, MethodType mtype, FunctionDescriptor fDesc) {
        incrAlign();
        indent();
        sb.append(PUB_MODS + "MemoryAddress " + className + "$make(" + className + " fi) {\n");
        incrAlign();
        indent();
        sb.append("return RuntimeHelper.upcallStub(" + className + ".class, fi, " + functionGetCallString(className, fDesc) + ", " +
                "\"" + mtype.toMethodDescriptorString() + "\");\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
    }

    public void addStaticFunctionWrapper(String javaName, String nativeName, MethodType mtype, FunctionDescriptor desc, boolean varargs, List<String> paramNames) {
        incrAlign();
        indent();
        sb.append(PUB_MODS + mtype.returnType().getName() + " " + javaName + " (");
        String delim = "";
        List<String> pNames = new ArrayList<>();
        final int numParams = paramNames.size();
        for (int i = 0 ; i < numParams; i++) {
            String pName = paramNames.get(i);
            if (pName.isEmpty()) {
                pName = "x" + i;
            }
            pNames.add(pName);
            sb.append(delim + mtype.parameterType(i).getName() + " " + pName);
            delim = ", ";
        }
        if (varargs) {
            String lastArg = "x" + numParams;
            if (numParams > 0) {
                sb.append(", ");
            }
            sb.append("Object... " + lastArg);
            pNames.add(lastArg);
        }
        sb.append(") {\n");
        incrAlign();
        indent();
        sb.append("try {\n");
        incrAlign();
        indent();
        if (!mtype.returnType().equals(void.class)) {
            sb.append("return (" + mtype.returnType().getName() + ")");
        }
        sb.append(methodHandleGetCallString(javaName, nativeName, mtype, desc, varargs) + ".invokeExact(" + String.join(", ", pNames) + ");\n");
        decrAlign();
        indent();
        sb.append("} catch (Throwable ex) {\n");
        incrAlign();
        indent();
        sb.append("throw new AssertionError(ex);\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
    }

    public void addFunctionalInterface(String name, MethodType mtype) {
        incrAlign();
        indent();
        sb.append("public interface " + name + " {\n");
        incrAlign();
        indent();
        sb.append(mtype.returnType().getName() + " apply(");
        String delim = "";
        for (int i = 0 ; i < mtype.parameterCount() ; i++) {
            sb.append(delim + mtype.parameterType(i).getName() + " x" + i);
            delim = ", ";
        }
        sb.append(");\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
        indent();
    }

    public void addGetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        String param = parentLayout != null ? (MemorySegment.class.getName() + " seg") : "";
        sb.append(PUB_MODS + type.getName() + " " + javaName + "$get(" + param + ") {\n");
        incrAlign();
        indent();
        String vhParam = parentLayout != null ?
                "seg.baseAddress()" : addressGetCallString(javaName, nativeName);
        sb.append("return (" + type.getName() + ")"
                + varHandleGetCallString(javaName, nativeName, layout, type, parentLayout) + ".get(" + vhParam + ");\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
    }

    public void addSetter(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        incrAlign();
        indent();
        String param = parentLayout != null ? (MemorySegment.class.getName() + " seg, ") : "";
        sb.append(PUB_MODS + "void " + javaName + "$set(" + param + type.getName() + " x) {\n");
        incrAlign();
        indent();
        String vhParam = parentLayout != null ?
                "seg.baseAddress()" : addressGetCallString(javaName, nativeName);
        sb.append(varHandleGetCallString(javaName, nativeName, layout, type, parentLayout) + ".set(" + vhParam + ", x);\n");
        decrAlign();
        indent();
        sb.append("}\n");
        decrAlign();
    }

    public List<JavaFileObject> build() {
        String res = sb.toString();
        this.sb = null;
        List<JavaFileObject> outputs = new ArrayList<>(constantHelper.getClasses());
        outputs.add(Utils.fileFromString(pkgName, className, res));
        return outputs;
    }

    // Utility

    private void addPackagePrefix() {
        assert pkgName.indexOf('/') == -1 : "package name invalid: " + pkgName;
        sb.append("// Generated by jextract\n\n");
        if (!pkgName.isEmpty()) {
            sb.append("package ");
            sb.append(pkgName);
            sb.append(";\n\n");
        }
    }

    private void addImportSection() {
        sb.append("import java.lang.invoke.MethodHandle;\n");
        sb.append("import java.lang.invoke.VarHandle;\n");
        sb.append("import jdk.incubator.foreign.*;\n");
        sb.append("import jdk.incubator.foreign.MemoryLayout.PathElement;\n");
        sb.append("import static ");
        sb.append(OutputFactory.C_LANG_CONSTANTS_HOLDER);
        sb.append(".*;\n");
    }

    private void emitForwardGetter(DirectMethodHandleDesc desc) {
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

    private String getCallString(DirectMethodHandleDesc desc) {
        return desc.owner().displayName() + "." + desc.methodName() + "()";
    }

    private String displayName(ClassDesc returnType) {
        return returnType.displayName(); // TODO shorten based on imports
    }

    private String functionGetCallString(String javaName, FunctionDescriptor fDesc) {
        return getCallString(constantHelper.addFunctionDesc(javaName, fDesc));
    }

    private String methodHandleGetCallString(String javaName, String nativeName, MethodType mt, FunctionDescriptor fDesc, boolean varargs) {
        return getCallString(constantHelper.addMethodHandle(javaName, nativeName, mt, fDesc, varargs));
    }

    private String varHandleGetCallString(String javaName, String nativeName, MemoryLayout layout, Class<?> type, MemoryLayout parentLayout) {
        return getCallString(constantHelper.addVarHandle(javaName, nativeName, layout, type, parentLayout));
    }

    private String addressGetCallString(String javaName, String nativeName) {
        return getCallString(constantHelper.addAddress(javaName, nativeName));
    }

    private void indent() {
        for (int i = 0; i < align; i++) {
            sb.append("    ");
        }
    }

    private void incrAlign() {
        align++;
    }

    private void decrAlign() {
        align--;
    }

}
