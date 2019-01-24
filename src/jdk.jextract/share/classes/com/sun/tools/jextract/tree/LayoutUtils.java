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

    static String getName(Cursor cursor) {
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

    static Layout getRecordReferenceLayout(Type t) {
        //symbolic reference
        return Unresolved.of(getName(t.canonicalType()));
    }

    static Layout getRecordLayout(Type type) {
        return RecordLayoutComputer.compute(0, type, type);
    }
}
