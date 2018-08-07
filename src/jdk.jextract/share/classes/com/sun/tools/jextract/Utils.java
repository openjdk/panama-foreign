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

import java.foreign.layout.Address;
import java.foreign.layout.Function;
import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.layout.Padding;
import java.foreign.layout.Sequence;
import java.foreign.layout.Unresolved;
import java.foreign.layout.Value;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.SourceVersion;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.SourceLocation;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;
import jdk.internal.foreign.memory.Types;

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
            // This happens when a typedef an anonymous struct, i.e., typedef struct {} type;
            Type t = cursor.type();
            nativeName = t.spelling();
            if (nativeName.contains("::")) {
                SourceLocation.Location loc = cursor.getSourceLocation().getFileLocation();
                return "anon$"
                        + loc.path().getFileName().toString().replaceAll("\\.", "_")
                        + "$" + loc.offset();
            }
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
            Layout l = getLayout(t.argType(i));
            args[i] = l instanceof Sequence? Address.ofLayout(64, ((Sequence)l).element()) : l;
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
        //symbolic reference
        return Unresolved.of()
                .withAnnotation(Layout.NAME, getIdentifier(t.canonicalType()));
    }

    public static Layout getRecordLayout(Type t, BiFunction<Cursor, Layout, Layout> fieldMapper) {
        return getRecordLayoutInternal(0, t, t, fieldMapper);
    }

    static Layout getRecordLayoutInternal(long offset, Type parent, Type t, BiFunction<Cursor, Layout, Layout> fieldMapper) {
        Cursor cu = t.getDeclarationCursor().getDefinition();
        if (cu.isInvalid()) {
            return getRecordReferenceLayout(t);
        }
        final boolean isUnion = cu.kind() == CursorKind.UnionDecl;
        Stream<Cursor> fieldTypes = cu.children()
                .filter(cx -> cx.isAnonymousStruct() || cx.kind() == CursorKind.FieldDecl);
        List<Layout> fieldLayouts = new ArrayList<>();
        int pendingBitfieldStart = -1;
        long actualSize = 0L;
        for (Cursor c : fieldTypes.collect(Collectors.toList())) {
            boolean isBitfield = c.isBitField();
            if (isBitfield && c.getBitFieldWidth() == 0) continue;
            long expectedOffset = offsetOf(parent, c);
            if (expectedOffset > offset) {
                if (isUnion) {
                    throw new IllegalStateException("No padding in union elements!");
                }
                fieldLayouts.add(Padding.of(expectedOffset - offset));
                actualSize += (expectedOffset - offset);
                offset = expectedOffset;
            }
            if (isBitfield && !isUnion && pendingBitfieldStart == -1) {
                pendingBitfieldStart = fieldLayouts.size();
            }
            if (!isBitfield && pendingBitfieldStart >= 0) {
                //emit/replace bitfields
                replaceBitfields(fieldLayouts, pendingBitfieldStart);
                pendingBitfieldStart = -1;
            }
            Layout fieldLayout = (c.isAnonymous()) ?
                    getRecordLayoutInternal(offset, parent, c.type(), fieldMapper) :
                    fieldLayout(isUnion, c, fieldMapper);
            fieldLayouts.add(fieldLayout);
            long size = fieldSize(isUnion, c);
            if (isUnion) {
                actualSize = Math.max(actualSize, size);
            } else {
                offset += size;
                actualSize += size;
            }
        }
        long expectedSize = t.size() * 8;
        if (actualSize < expectedSize) {
            fieldLayouts.add(Padding.of(expectedSize - actualSize));
        }
        if (pendingBitfieldStart >= 0) {
            //emit/replace bitfields
            replaceBitfields(fieldLayouts, pendingBitfieldStart);
        }
        Layout[] fields = fieldLayouts.toArray(new Layout[0]);
        Group g = isUnion ?
                Group.union(fields) : Group.struct(fields);
        return g.withAnnotation(Layout.NAME, getIdentifier(cu));
    }

    static Layout fieldLayout(boolean isUnion, Cursor c, BiFunction<Cursor, Layout, Layout> fieldMapper) {
        Layout layout = getLayout(c.type());
        if (c.isBitField()) {
            boolean isSigned = ((Value)layout).kind() == Value.Kind.INTEGRAL_SIGNED;
            Layout sublayout = isSigned ?
                    Value.ofSignedInt(c.getBitFieldWidth()) :
                    Value.ofUnsignedInt(c.getBitFieldWidth());
            sublayout = fieldMapper.apply(c, sublayout);
            return isUnion ?
                    bitfield((Value)layout, List.of(sublayout)) :
                    sublayout;
        } else {
            return fieldMapper.apply(c, layout);
        }
    }

    static long fieldSize(boolean isUnion, Cursor c) {
        if (!c.isBitField() || isUnion) {
            return c.type().size() * 8;
        } else {
            return c.getBitFieldWidth();
        }
    }

    static void replaceBitfields(List<Layout> layouts, int pendingBitfieldsStart) {
        long storageSize = storageSize(layouts);
        long offset = 0L;
        List<Layout> newFields = new ArrayList<>();
        List<Layout> pendingFields = new ArrayList<>();
        while (layouts.size() > pendingBitfieldsStart) {
            Layout l = layouts.remove(pendingBitfieldsStart);
            offset += l.bitsSize();
            pendingFields.add(l);
            if (!pendingFields.isEmpty() &&
                    offset == storageSize) {
                //emit new
                newFields.add(bitfield(Value.ofUnsignedInt(storageSize), pendingFields));
                pendingFields.clear();
                offset = 0L;
            } else if (offset > storageSize) {
                throw new IllegalStateException("Crossing storage unit boundaries");
            }
        }
        if (!pendingFields.isEmpty()) {
            throw new IllegalStateException("Partially used storage unit");
        }
        //add back new fields
        newFields.forEach(layouts::add);
    }

    static long storageSize(List<Layout> layouts) {
        long size = layouts.stream().mapToLong(Layout::bitsSize).sum();
        int[] sizes = { 64, 32, 16, 8 };
        for (int s : sizes) {
            if (size % s == 0) {
                return s;
            }
        }
        throw new IllegalStateException("Cannot infer storage size");
    }

    static Value bitfield(Value v, List<Layout> sublayouts) {
        return v.withContents(Group.struct(sublayouts.toArray(new Layout[0])));
    }

    static long offsetOf(Type parent, Cursor c) {
        if (c.kind() == CursorKind.FieldDecl) {
            return parent.getOffsetOf(c.spelling());
        } else {
            return c.children()
                    .mapToLong(child -> offsetOf(parent, child))
                    .findFirst().orElseThrow(IllegalStateException::new);
        }
    }
}
