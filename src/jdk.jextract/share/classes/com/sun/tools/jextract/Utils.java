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
import jdk.internal.clang.TypeKind;
import jdk.internal.nicl.types.Types;
import jdk.internal.nicl.types.Types.UNSIGNED;

import javax.lang.model.SourceVersion;
import java.nicl.layout.Address;
import java.nicl.layout.Function;
import java.nicl.layout.Group;
import java.nicl.layout.Group.Kind;
import java.nicl.layout.Layout;
import java.nicl.layout.Padding;
import java.nicl.layout.Sequence;
import java.nicl.layout.Unresolved;
import java.nicl.layout.Value;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static Layout getLayout(Cursor clang_cursor) {
        return getLayout(clang_cursor.type());
    }

    public static Function getFunction(Cursor clang_cursor) {
        return getFunction(clang_cursor.type());
    }

    public static boolean isFunction(Type clang_type) {
        switch (clang_type.kind()) {
            case Unexposed:
            case Typedef:
            case Elaborated:
                return isFunction(clang_type.canonicalType());
            case FunctionProto:
            case FunctionNoProto:
                return true;
            default:
                return false;
        }
    }

    public static Function getFunction(Type t) {
        switch (t.kind()) {
            case Unexposed:
            case Typedef:
            case Elaborated:
                return parseFunctionInternal(t.canonicalType());
            case FunctionProto:
            case FunctionNoProto:
                return parseFunctionInternal(t);
            default:
                throw new IllegalArgumentException(
                        "Unsupported type kind: " + t.kind());
        }
    }

    private static Function parseFunctionInternal(Type t) {
        final int argSize = t.numberOfArgs();
        Layout[] args = new Layout[argSize];
        for (int i = 0; i < argSize; i++) {
            args[i] = getLayout(t.argType(i));
        }
        if (t.resultType().kind() == TypeKind.Void) {
            return Function.ofVoid(t.isVariadic(), args);
        } else {
            return Function.of(getLayout(t.resultType()), t.isVariadic(), args);
        }
    }

    public static Layout getLayout(Type t) {
        switch(t.kind()) {
            case Bool:
                return Types.BOOLEAN;
            case Int:
                return Types.INT;
            case UInt:
                return Types.UNSIGNED.INT;
            case Int128:
                return Types.INT128;
            case UInt128:
                return Types.UNSIGNED.INT128;
            case Short:
                return Types.SHORT;
            case UShort:
                return Types.UNSIGNED.SHORT;
            case Long:
                return Types.LONG;
            case ULong:
                return Types.UNSIGNED.LONG;
            case LongLong:
                return Types.LONG_LONG;
            case ULongLong:
                return Types.UNSIGNED.LONG_LONG;
            case SChar:
                return Types.BYTE;
            case Char_S:
            case Char_U:
            case UChar:
                return Types.UNSIGNED.BYTE;
            case Float:
                return Types.FLOAT;
            case Double:
                return Types.DOUBLE;
            case LongDouble:
                return Types.LONG_DOUBLE;
            case Record:
                return getRecordReferenceLayout(t);
            case Enum:
                return Types.INT;
            case ConstantArray:
                return Sequence.of(t.getNumberOfElements(), getLayout(t.getElementType()));
            case IncompleteArray:
                return Sequence.of(0L, getLayout(t.getElementType()));
            case Unexposed:
            case Typedef:
            case Elaborated:
                return getLayout(t.canonicalType());
            case Pointer:
            case BlockPointer:
                return parsePointerInternal(t.getPointeeType());
            default:
                throw new IllegalArgumentException(
                        "Unsupported type kind: " + t.kind());
        }
    }

    private static Address parsePointerInternal(Type pointeeType) {
        switch (pointeeType.kind()) {
            case Unexposed:
            case Typedef:
            case Elaborated:
                return parsePointerInternal(pointeeType.canonicalType());
            case FunctionProto:
            case FunctionNoProto:
                return Address.ofFunction(64, parseFunctionInternal(pointeeType));
            case Void:
                return Address.ofVoid(64);
            default:
                return Address.ofLayout(64, getLayout(pointeeType));
        }
    }

    private static Layout getRecordReferenceLayout(Type t) {
        Cursor cu = t.getDeclarationCursor().getDefinition();
        if (cu.isInvalid()) {
            // Have no idea what's inside - likely a pointer to undefined struct
            return t.size() < 0 ?
                    Types.CHAR :
                    Sequence.of(t.size(), Types.CHAR);
        } else {
            //symbolic reference
            return Unresolved.of()
                    .withAnnotation(Layout.NAME, t.canonicalType().getDeclarationCursor().spelling());
        }
    }

    public static Group getRecordLayout(Type t, java.util.function.BiFunction<String, Layout, Layout> fieldMapper) {
        Cursor cu = t.getDeclarationCursor().getDefinition();
        assert !cu.isInvalid();
        final boolean isUnion = cu.kind() == CursorKind.UnionDecl;
        Stream<Cursor> fieldTypes = cu.stream()
                .filter(cx -> cx.kind() == CursorKind.FieldDecl);
        long offset = 0L;
        List<Layout> fieldLayouts = new ArrayList<>();
        for (Cursor c : fieldTypes.collect(Collectors.toList())) {
            String fieldName = c.spelling();
            long fieldOffset = t.getOffsetOf(c.spelling());
            if (fieldOffset != offset) {
                //add padding
                fieldLayouts.add(Padding.of(fieldOffset - offset));
                offset = fieldOffset;
            }
            Layout fieldLayout = c.isAnonymousStruct() ?
                    getRecordLayout(c.type(), fieldMapper) :
                    getLayout(c.type());
            fieldLayouts.add(fieldMapper.apply(fieldName, fieldLayout));
            if (!isUnion) {
                offset += c.type().size() * 8;
            }
        }
        long size = t.size() * 8;
        if (offset != size) {
            //add final padding
            fieldLayouts.add(Padding.of(size - offset));
        }
        Layout[] fields = fieldLayouts.toArray(new Layout[0]);
        Group g = isUnion ?
                Group.union(fields) : Group.struct(fields);
        return g.withAnnotation(Layout.NAME, cu.spelling());
    }
}
