/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.jextract.jnr;

import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;


public class LibFactory {
    final String pkgName;
    final StructFactory structFactory;
    final FileWriter fout;

    private LibFactory(String pkg, FileWriter fout, StructFactory helper) {
        this.pkgName = pkg;
        this.fout = fout;
        this.structFactory = helper;
    }

    String getPointerType(Type pointee) throws IOException {
        System.out.println("Pointee type is " + pointee.kind() + " " + pointee.spelling());
        switch(pointee.kind()) {
            case Char_S:
            case SChar:
            case UChar:
            case Char_U:
            case Char16:
            case WChar:
                return "String";
            case Record:
            case Unexposed:
                return structFactory.getUnexposedTypeClassName(pointee);
            case Typedef:
                return getPointerType(pointee.canonicalType());
            default:
                return "Pointer";
        }
    }

    String getTypeName(Type type) throws IOException {
        switch(type.kind()) {
            case Void:
                return "void";
            case Typedef:
                return getTypeName(type.canonicalType());
            case Bool:
                return "boolean";
            case UChar:
            case Char_U:
                return "byte";
            case SChar:
            case Char_S:
                return "byte";
            case UShort:
            case Short:
                return "short";
            case Char16:
            case WChar:
                return "char";
            case UInt:
            case Int:
            case Char32:
                return "int";
            case ULong:
            case Long:
                return "long";
            case ULongLong:
            case LongLong:
                return "@LongLong long";
            case Float:
                return "float";
            case Double:
                return "double";
            case NullPtr:
                return "Pointer";
            case Pointer:
                return getPointerType(type.getPointeeType());
            case ConstantArray:
                return getTypeName(type.getElementType()) + "[]";
            case Unexposed:
                return structFactory.getUnexposedTypeClassName(type);
            case Record:
                return structFactory.getUnexposedTypeClassName(type);
            default:
                throw new IllegalArgumentException("Unsupport type: " + type.kind());
        }
    }

    public String addArgument(Cursor c) {
        try {
            Type t = c.type();
            return getTypeName(t) + " " + c.spelling();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public LibFactory addFunction(Cursor c) throws IOException{
        Type t = c.type();
        assert(t.kind() == TypeKind.FunctionProto);
        String methodName = c.spelling();
        fout.append("    public ");
        fout.append(getTypeName(t.resultType()));
        fout.append(" ");
        fout.append(methodName);
        fout.append("(");
        // arguments
        int args = t.numberOfArgs();
        for (int i = 0; i < args; i++) {
            Cursor argCx = c.getArgument(i);
            if (i != 0) {
                fout.append(", ");
            }
            fout.append(getTypeName(argCx.type()));
            fout.append(" ");
            fout.append(argCx.spelling());
        }
        if (t.isVariadic()) {
            if (args != 0) {
                fout.append(", ");
            }
            fout.append("Object... args");
        }
        fout.append(");\n");
        // continue chaining
        return this;
    }

    public void build() throws IOException {
        // close interface definition
        fout.append("}\n");
        fout.close();
    }

    public static LibFactory create(String pkgName, String intffName, StructFactory helper) throws IOException {
        FileWriter f = new FileWriter(intffName + ".java");
        // package name
        if (! pkgName.isEmpty()) {
            f.append("package ");
            f.append(pkgName);
            f.append(";\n\n");
        }
        // import statements
        f.append("\n");
        // class
        f.append("public interface ");
        f.append(intffName);
        f.append(" {\n");

        return new LibFactory(pkgName, f, helper);
    }
}
