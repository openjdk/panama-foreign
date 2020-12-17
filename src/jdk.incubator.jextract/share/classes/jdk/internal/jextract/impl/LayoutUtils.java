/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

package jdk.internal.jextract.impl;

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.jextract.Type.Primitive;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;

import java.util.Optional;
import java.util.function.Supplier;

import static jdk.incubator.foreign.CLinker.C_POINTER;

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

    public static String getName(Cursor cursor) {
        return cursor.spelling();
    }

    public static MemoryLayout getLayout(Type t) {
        Supplier<UnsupportedOperationException> unsupported = () ->
                new UnsupportedOperationException("unsupported: " + t.kind());
        switch(t.kind()) {
            case UChar, Char_U:
            case SChar, Char_S:
                return Primitive.Kind.Char.layout().orElseThrow(unsupported);
            case Short:
            case UShort:
                return Primitive.Kind.Short.layout().orElseThrow(unsupported);
            case Int:
            case UInt:
                return Primitive.Kind.Int.layout().orElseThrow(unsupported);
            case ULong:
            case Long:
                return Primitive.Kind.Long.layout().orElseThrow(unsupported);
            case ULongLong:
            case LongLong:
                return Primitive.Kind.LongLong.layout().orElseThrow(unsupported);
            case UInt128:
            case Int128:
                return Primitive.Kind.Int128.layout().orElseThrow(unsupported);
            case Enum:
                return valueLayoutForSize(t.size() * 8).layout().orElseThrow(unsupported);
            case Bool:
                return Primitive.Kind.Bool.layout().orElseThrow(unsupported);
            case Float:
                return Primitive.Kind.Float.layout().orElseThrow(unsupported);
            case Double:
                return Primitive.Kind.Double.layout().orElseThrow(unsupported);
            case LongDouble:
                return Primitive.Kind.LongDouble.layout().orElseThrow(unsupported);
            case Complex:
                throw new UnsupportedOperationException("unsupported: " + t.kind());
            case Record:
                return getRecordLayout(t);
            case Vector:
                return MemoryLayout.ofSequence(t.getNumberOfElements(), getLayout(t.getElementType()));
            case ConstantArray:
                return MemoryLayout.ofSequence(t.getNumberOfElements(), getLayout(t.getElementType()));
            case IncompleteArray:
                return MemoryLayout.ofSequence(getLayout(t.getElementType()));
            case Unexposed:
                Type canonical = t.canonicalType();
                if (canonical.equalType(t)) {
                    throw new TypeMaker.TypeException("Unknown type with same canonical type: " + t.spelling());
                }
                return getLayout(canonical);
            case Typedef:
            case Elaborated:
                return getLayout(t.canonicalType());
            case Pointer:
            case BlockPointer:
                return C_POINTER;
            default:
                throw new UnsupportedOperationException("unsupported: " + t.kind());
        }
    }

    public static Optional<MemoryLayout> getLayout(jdk.incubator.jextract.Type t) {
        try {
            return Optional.of(getLayoutInternal(t));
        } catch (Throwable ex) {
            return Optional.empty();
        }
    }

    public static MemoryLayout getLayoutInternal(jdk.incubator.jextract.Type t) {
        return t.accept(layoutMaker, null);
    }

    private static jdk.incubator.jextract.Type.Visitor<MemoryLayout, Void> layoutMaker = new jdk.incubator.jextract.Type.Visitor<>() {
        @Override
        public MemoryLayout visitPrimitive(jdk.incubator.jextract.Type.Primitive t, Void _ignored) {
            return t.kind().layout().orElseThrow(UnsupportedOperationException::new);
        }

        @Override
        public MemoryLayout visitDelegated(jdk.incubator.jextract.Type.Delegated t, Void _ignored) {
            if (t.kind() == jdk.incubator.jextract.Type.Delegated.Kind.POINTER) {
                return C_POINTER;
            } else {
                return t.type().accept(this, null);
            }
        }

        @Override
        public MemoryLayout visitFunction(jdk.incubator.jextract.Type.Function t, Void _ignored) {
            /*
             * // pointer to function declared as function like this
             *
             * typedef void CB(int);
             * void func(CB cb);
             */
            return C_POINTER;
        }

        @Override
        public MemoryLayout visitDeclared(jdk.incubator.jextract.Type.Declared t, Void _ignored) {
            return t.tree().layout().orElseThrow(UnsupportedOperationException::new);
        }

        @Override
        public MemoryLayout visitArray(jdk.incubator.jextract.Type.Array t, Void _ignored) {
            MemoryLayout elem = t.elementType().accept(this, null);
            if (t.elementCount().isPresent()) {
                return MemoryLayout.ofSequence(t.elementCount().getAsLong(), elem);
            } else {
                return MemoryLayout.ofSequence(elem);
            }
        }

        @Override
        public MemoryLayout visitType(jdk.incubator.jextract.Type t, Void _ignored) {
            throw new UnsupportedOperationException();
        }
    };

    static MemoryLayout getRecordLayout(Type type) {
        return RecordLayoutComputer.compute(0, type, type);
    }

    private static boolean isVoidType(jdk.incubator.jextract.Type type) {
        if (type instanceof jdk.incubator.jextract.Type.Primitive) {
            jdk.incubator.jextract.Type.Primitive pt = (jdk.incubator.jextract.Type.Primitive)type;
            return pt.kind() == jdk.incubator.jextract.Type.Primitive.Kind.Void;
        } else if (type instanceof jdk.incubator.jextract.Type.Delegated) {
            jdk.incubator.jextract.Type.Delegated dt = (jdk.incubator.jextract.Type.Delegated)type;
            return dt.kind() == jdk.incubator.jextract.Type.Delegated.Kind.TYPEDEF? isVoidType(dt.type()) : false;
        }
        return false;
    }

    public static Optional<FunctionDescriptor> getDescriptor(jdk.incubator.jextract.Type.Function t) {
        try {
            MemoryLayout[] args = t.argumentTypes().stream()
                    .map(LayoutUtils::getLayoutInternal)
                    .toArray(MemoryLayout[]::new);
            jdk.incubator.jextract.Type retType = t.returnType();
            if (isVoidType(retType)) {
                return Optional.of(FunctionDescriptor.ofVoid(args));
            } else {
                return Optional.of(FunctionDescriptor.of(getLayoutInternal(retType), args));
            }
        } catch (Throwable ex) {
            return Optional.empty();
        }
    }

    public static Primitive.Kind valueLayoutForSize(long size) {
        return switch ((int) size) {
            case 8 -> Primitive.Kind.Char;
            case 16 -> Primitive.Kind.Short;
            case 32 -> Primitive.Kind.Int;
            case 64 -> Primitive.Kind.LongLong;
            default -> throw new IllegalStateException("Cannot infer container layout");
        };
    }
}
