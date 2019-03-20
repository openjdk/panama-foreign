/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
package com.sun.tools.jextract;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.sun.tools.jextract.tree.FunctionTree;
import com.sun.tools.jextract.tree.VarTree;

/**
 * A helper class to generate static forwarder header class in source form.
 * After aggregating various constituents of a .java source, build method
 * is called to get overall generated source string.
 */
final class JavaSourceBuilderExt extends JavaSourceBuilder {
    JavaSourceBuilderExt(int align) {
        super(align);
    }

    JavaSourceBuilderExt() {
        super();
    }

    protected void addImportSection() {
        sb.append("import java.foreign.*;\n");
        sb.append("import java.foreign.memory.*;\n");
        sb.append("import java.lang.invoke.*;\n\n");
    }

    protected void classBegin(String name, boolean isStatic) {
        classBegin(name, isStatic, null);
    }

    protected void classBegin(String name, boolean isStatic, String superType) {
        check();
        indent();
        sb.append("public ");
        if (isStatic) {
            sb.append("static ");
        }
        sb.append("class ");
        sb.append(name);
        if (superType != null) {
            sb.append(" extends ");
            sb.append(superType);
        }
        sb.append(" {\n\n");
    }

    protected void classEnd() {
        interfaceEnd();
    }

    // implement LibraryScope scope(LibraryInterfaceObject) method
    protected void emitScopeAccessor(String libraryField) {
        check();
        incrAlign();
        indent();
        sb.append("public static Scope scope() {\n");
        incrAlign();
        indent();
        sb.append("return Libraries.libraryScope(");
        sb.append(libraryField);
        sb.append(");\n");
        decrAlign();
        indent();
        sb.append("}\n\n");
        decrAlign();
    }

    // add private static field for the underlying header interface (bound) object
    protected void addLibraryField(String className, String libraryField) {
        check();
        incrAlign();
        indent();
        sb.append("private static final ");
        sb.append(className);
        sb.append(' ');
        sb.append(libraryField);
        sb.append(" = Libraries.bind(MethodHandles.lookup(), ");
        sb.append(className);
        sb.append(".class);\n\n");
        decrAlign();
    }

    // add a static final field for enum constant or a C macro
    protected void addField(String name, JType type, Object value, String libraryField) {
        check();
        incrAlign();
        indent();
        sb.append("public static final ");
        sb.append(type.getSourceSignature(false));
        sb.append(' ');
        sb.append(name);
        sb.append(" = ");
        if (value != null) {
            boolean isChar = value instanceof Character;
            if (isChar) {
                sb.append('\'');
            }
            sb.append(value);
            if (isChar) {
                sb.append('\'');
            }
            if (value instanceof Long) {
                sb.append('L');
            } else if (value instanceof Double) {
                sb.append('D');
            }
        } else {
            sb.append(libraryField);
            sb.append('.');
            sb.append(name);
            sb.append("()");
        }
        sb.append(";\n\n");
        decrAlign();
    }

    // emit static forwarder methods for a variable getter, setter and pointer getter
    protected void emitStaticForwarder(VarTree varTree, JType type, String libraryField) {
        check();
        incrAlign();

        String fieldName = varTree.name();

        // getter
        indent();
        sb.append("public static ");
        sb.append(type.getSourceSignature(false));
        sb.append(' ');
        sb.append(fieldName);
        sb.append("$get() {\n");
        incrAlign();
        indent();
        sb.append("return ");
        sb.append(libraryField);
        sb.append('.');
        sb.append(fieldName);
        sb.append("$get();\n");
        decrAlign();
        indent();
        sb.append("}\n\n");

        // setter
        indent();
        sb.append("public static void ");
        sb.append(fieldName);
        sb.append("$set(");
        sb.append(type.getSourceSignature(true));
        sb.append(" value) {\n");
        incrAlign();
        indent();
        sb.append(libraryField);
        sb.append('.');
        sb.append(fieldName);
        sb.append("$set(value);\n");
        decrAlign();
        indent();
        sb.append("}\n\n");

        // ptr getter
        JType ptrType = JType.GenericType.ofPointer(type);
        indent();
        sb.append("public static ");
        sb.append(ptrType.getSourceSignature(false));
        sb.append(' ');
        sb.append(fieldName);
        sb.append("$ptr() {\n");
        incrAlign();
        indent();
        sb.append("return ");
        sb.append(libraryField);
        sb.append('.');
        sb.append(fieldName);
        sb.append("$ptr();\n");
        decrAlign();
        indent();
        sb.append("}\n\n");

        decrAlign();
    }

    // emit static forwarder method of a function
    protected void emitStaticForwarder(FunctionTree funcTree, JType.Function fn, String libraryField) {
        check();
        incrAlign();
        indent();
        sb.append("public static ");
        sb.append(fn.returnType.getSourceSignature(false));
        sb.append(' ');
        sb.append(funcTree.name());
        sb.append('(');
        final int arg_cnt = funcTree.numParams();
        final List<String> argNames = new ArrayList<>();
        for (int i = 0; i < fn.args.length; i++) {
            sb.append(fn.args[i].getSourceSignature(false));
            sb.append(' ');
            String pname = funcTree.paramName(i);
            pname = pname == null || pname.isEmpty()? ("$arg" + i) : pname;
            argNames.add(pname);
            sb.append(pname);
            if (fn.isVarArgs || i != arg_cnt - 1) {
                sb.append(", ");
            }
        }
        if (fn.isVarArgs) {
            sb.append("Object... ");
            String pname = funcTree.paramName(arg_cnt - 1);
            pname = pname == null || pname.isEmpty()? "$args" : pname;
            argNames.add(pname);
            sb.append(pname);
        }

        sb.append(") {\n");
        incrAlign();
        indent();
        if (fn.returnType != JType.Void) {
            sb.append("return ");
        }
        sb.append(libraryField);
        sb.append('.');
        sb.append(funcTree.name());
        sb.append('(');
        int size = argNames.size();
        int j = 0;
        for (String an : argNames) {
            sb.append(an);
            if (j != size - 1) sb.append(", ");
            j++;
        }
        sb.append(");\n");
        decrAlign();
        indent();
        sb.append("}\n\n");
        decrAlign();
    }
}
