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

package com.sun.tools.jextract;

import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.Type;

import javax.lang.model.SourceVersion;
import java.util.HashSet;
import java.util.Set;

/**
 * General utility functions
 */
public class Utils {
    public static String toJavaIdentifier(String str) {
        final int size = str.length();
        StringBuilder sb = new StringBuilder(size);
        if (! Character.isJavaIdentifierStart(str.charAt(0))) {
            sb.append('_');
        }
        for (int i = 0; i < size; i++) {
            char ch = str.charAt(i);
            if (Character.isJavaIdentifierPart(ch)) {
                sb.append(ch);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    public static String toClassName(String cname) {
        StringBuilder sb = new StringBuilder(cname.length());
        cname = toJavaIdentifier(cname);
        sb.append(cname);
        if (SourceVersion.isKeyword(cname)) {
            sb.append("$");
        }
        return sb.toString();
    }

    public static String toInternalName(String pkg, String name, String... nested) {
        if ((pkg == null || pkg.isEmpty()) && nested == null) {
            return name;
        }

        StringBuilder sb = new StringBuilder();
        if (pkg != null && ! pkg.isEmpty()) {
            sb.append(pkg.replace('.', '/'));
            if (sb.charAt(sb.length() - 1) != '/') {
                sb.append('/');
            }
        }
        sb.append(name);
        for (String n: nested) {
            sb.append('$');
            sb.append(n);
        }
        return sb.toString();
    }

    public static String getIdentifier(Type type) {
        Cursor c = type.getDeclarationCursor();
        if (c.isInvalid()) {
            return type.spelling();
        }
        return getIdentifier(c);
    }

    public static String getIdentifier(Cursor cursor) {
        // Use cursor name instead of type name, this way we don't have struct
        // or enum prefix
        String nativeName = cursor.spelling();
        if (nativeName.isEmpty()) {
            // This happens when a typedef an anonymouns struct, i.e., typedef struct {} type;
            Type t = cursor.type();
            nativeName = t.spelling();
        }

        return nativeName;
    }

    public static String ClassToDescriptor(Class<?> cls) {
        if (cls.isArray()) {
            return cls.getName();
        }
        if (cls.isPrimitive()) {
            switch (cls.getTypeName()) {
                case "int":
                    return "I";
                case "long":
                    return "J";
                case "byte":
                    return "B";
                case "char":
                    return "C";
                case "float":
                    return "F";
                case "double":
                    return "D";
                case "short":
                    return "S";
                case "boolean":
                    return "Z";
                case "void":
                    return "V";
            }
        }
        // assuming reference
        return "L" + cls.getName() + ";";
    }

    public static String DescriptorToBinaryName(String descriptor) {
        final char[] ar = descriptor.trim().toCharArray();
        switch (ar[0]) {
            case '(':
                throw new IllegalArgumentException("Method descriptor is not allowed");
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'Z':
                return "boolean";
        }

        StringBuilder sb = new StringBuilder();
        if (ar[0] == 'L') {
            for (int i = 1; i < ar.length && ar[i] != ';'; i++) {
                if (ar[i] == '/') {
                    sb.append('.');
                }
                if (!Character.isJavaIdentifierPart(ar[i])) {
                    throw new IllegalArgumentException("Malformed descriptor");
                }
                sb.append(ar[i]);
            }
            return sb.toString();
        }

        if (ar[0] == '[') {
            int depth = 1;
            while (ar[depth] == '[') depth++;
            sb.append(DescriptorToBinaryName(descriptor.substring(depth)));
            for (int i = 0; i < depth; i++) {
                sb.append("[]");
            }
            return sb.toString();
        }

        throw new IllegalArgumentException("Malformed descriptor");
    }

    private static class DescriptorBuilder {
        final StringBuilder sb = new StringBuilder();
        private Set<String> seenRecords = new HashSet<>();

        public DescriptorBuilder append(Type t) {
            switch(t.kind()) {
                case Bool:
                    sb.append('B');
                    break;
                case Int:
                    sb.append('i');
                    break;
                case UInt:
                    sb.append('I');
                    break;
                case Int128:
                    sb.append("=128i");
                    break;
                case UInt128:
                    sb.append("=128I");
                    break;
                case Short:
                    sb.append('s');
                    break;
                case UShort:
                    sb.append('S');
                    break;
                case Long:
                    sb.append('l');
                    break;
                case ULong:
                    sb.append('L');
                    break;
                case LongLong:
                    sb.append('q');
                    break;
                case ULongLong:
                    sb.append('Q');
                    break;
                case Char_S:
                case Char_U:
                    sb.append('c');
                    break;
                case SChar:
                    sb.append('o');
                    break;
                case UChar:
                    sb.append('O');
                    break;
                case Float:
                    sb.append('F');
                    break;
                case Double:
                    sb.append('D');
                    break;
                case LongDouble:
                    sb.append('E');
                    break;
                case Void:
                    sb.append('V');
                    break;
                case Vector:
                    sb.append('=');
                    sb.append(t.size() * 8);
                    sb.append('v');
                    break;
                case Record:
                    sb.append('[');
                    doRecord(t);
                    sb.append(']');
                    break;
                case Enum:
                    sb.append('i');
                    break;
                case ConstantArray:
                    sb.append(t.getNumberOfElements());
                    append(t.getElementType());
                    break;
                case IncompleteArray:
                    sb.append('*');
                    append(t.getElementType());
                    break;
                case FunctionProto:
                case FunctionNoProto:
                    doFunction(t);
                    break;
                case Unexposed:
                case Typedef:
                case Elaborated:
                    append(t.canonicalType());
                    break;
                case Pointer:
                case BlockPointer:
                    sb.append("p:");
                    int prevPos = sb.length() - 1;
                    try {
                        append(t.getPointeeType());
                    } catch (RecursiveRecordDeclarationError err) {
                        //undo changes
                        sb.delete(prevPos, sb.length());
                    }
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unsupported type kind: " + t.kind());
            }
            return this;
        }

        private void doFunction(Type t) {
            sb.append('(');
            final int args = t.numberOfArgs();
            for (int i = 0; i < args; i++) {
                append(t.argType(i));
            }
            if (t.isVariadic()) {
                sb.append("*)");
            } else {
                sb.append(')');
            }
            append(t.resultType());
        }

        private void doRecord(Type t) {
            Cursor cu = t.getDeclarationCursor().getDefinition();
            if (cu.isInvalid()) {
                // Have no idea what's inside, fill with char[]
                if (t.size() < 0) {
                    sb.append('*');
                } else {
                    sb.append(t.size());
                }
                sb.append('c');
            } else {
                if (!seenRecords.add(t.spelling())) {
                    throw new RecursiveRecordDeclarationError();
                } else {
                    try {
                        final boolean isUnion = cu.kind() == CursorKind.UnionDecl;
                        cu.stream()
                                .filter(cx -> cx.kind() == CursorKind.FieldDecl)
                                .forEachOrdered(cx -> {
                                    append(cx.type());
                                    if (isUnion) {
                                        sb.append('|');
                                    }
                                });
                        if (sb.charAt(sb.length() - 1) == '|') {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                    } finally {
                        seenRecords.remove(t.spelling());
                    }
                }
            }
        }

        public String build() {
            return sb.toString();
        }
    }

    static class RecursiveRecordDeclarationError extends Error {
        private static final long serialVersionUID = 0L;
    }

    public static String getLayout(Type clang_type) {
        return new DescriptorBuilder().append(clang_type).build();
    }

    public static String getLayout(Cursor clang_cursor) {
        return getLayout(clang_cursor.type());
    }
}
