/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.jextract.tree;

import java.foreign.layout.Address;
import java.foreign.layout.Function;
import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.layout.Padding;
import java.foreign.layout.Sequence;
import java.foreign.layout.Unresolved;
import java.foreign.layout.Value;
import java.foreign.memory.DoubleComplex;
import java.foreign.memory.FloatComplex;
import java.foreign.memory.LayoutType;
import java.foreign.memory.LongDoubleComplex;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.SourceLocation;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;

/**
 * General Layout utility functions
 */
public final class LayoutUtils {
    private LayoutUtils() {}

    public static String getName(Type type) {
        Cursor c = type.getDeclarationCursor();
        if (c.isInvalid()) {
            return type.spelling();
        }
        return getName(c);
    }

    public static String getName(Tree tree) {
        String name = tree.name();
        return name.isEmpty()? getName(tree.cursor()) : name;
    }

    private static String getName(Cursor cursor) {
        // Use cursor name instead of type name, this way we don't have struct
        // or enum prefix
        String nativeName = cursor.spelling();
        if (nativeName.isEmpty()) {
            Type t = cursor.type();
            nativeName = t.spelling();
            if (nativeName.contains("::") || nativeName.contains(" ")) {
                SourceLocation.Location loc = cursor.getSourceLocation().getFileLocation();
                return "anon$"
                        + loc.path().getFileName().toString().replaceAll("\\.", "_")
                        + "$" + loc.offset();
            }
        }

        return nativeName;
    }

    private static boolean isFunction(Type clang_type) {
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
        assert isFunction(t) : "not a function type";
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
            case SChar:
            case Short:
            case Int:
            case Long:
            case LongLong:
            case Int128:
            case Enum:
                return Value.ofSignedInt(t.size() * 8);
            case Bool:
            case UInt:
            case UInt128:
            case UShort:
            case ULong:
            case ULongLong:
            case Char_S:
            case Char_U:
            case UChar:
                return Value.ofUnsignedInt(t.size() * 8);
            case Float:
            case Double:
            case LongDouble:
                return Value.ofFloatingPoint(t.size() * 8);
            case Record:
                return getRecordReferenceLayout(t);
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
            case FunctionProto:
                return Address.ofFunction(64, parseFunctionInternal(t));
            case Complex:
                TypeKind ek = t.getElementType().kind();
                if (ek == TypeKind.Float) {
                    return LayoutType.ofStruct(FloatComplex.class).layout();
                } else if (ek == TypeKind.Double) {
                    return LayoutType.ofStruct(DoubleComplex.class).layout();
                } else if (ek == TypeKind.LongDouble) {
                    return LayoutType.ofStruct(LongDoubleComplex.class).layout();
                } else {
                    throw new IllegalArgumentException(
                        "Unsupported _Complex kind: " + ek);
                }
            default:
                throw new IllegalArgumentException(
                        "Unsupported type kind: " + t.kind()  + ", for type: " + t.spelling());
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
                .withAnnotation(Layout.NAME, getName(t.canonicalType()));
    }

    static Layout getRecordLayout(Type t, BiFunction<Cursor, Layout, Layout> fieldMapper) {
        return getRecordLayoutInternal(0, t, t, fieldMapper);
    }

    private static Layout getRecordLayoutInternal(long offset, Type parent, Type t, BiFunction<Cursor, Layout, Layout> fieldMapper) {
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
            Layout fieldLayout = (c.isAnonymousStruct()) ?
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
        return g.withAnnotation(Layout.NAME, getName(cu));
    }

    private static Layout fieldLayout(boolean isUnion, Cursor c, BiFunction<Cursor, Layout, Layout> fieldMapper) {
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

    private static long fieldSize(boolean isUnion, Cursor c) {
        if (!c.isBitField() || isUnion) {
            return c.type().size() * 8;
        } else {
            return c.getBitFieldWidth();
        }
    }

    private static void replaceBitfields(List<Layout> layouts, int pendingBitfieldsStart) {
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

    private static long storageSize(List<Layout> layouts) {
        long size = layouts.stream().mapToLong(Layout::bitsSize).sum();
        int[] sizes = { 64, 32, 16, 8 };
        for (int s : sizes) {
            if (size % s == 0) {
                return s;
            }
        }
        throw new IllegalStateException("Cannot infer storage size");
    }

    private static Value bitfield(Value v, List<Layout> sublayouts) {
        return v.withContents(Group.struct(sublayouts.toArray(new Layout[0])));
    }

    private static long offsetOf(Type parent, Cursor c) {
        if (c.kind() == CursorKind.FieldDecl) {
            return parent.getOffsetOf(c.spelling());
        } else {
            return c.children()
                    .mapToLong(child -> offsetOf(parent, child))
                    .findFirst().orElseThrow(IllegalStateException::new);
        }
    }
}
