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
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.SystemABI;
import jdk.incubator.foreign.ValueLayout;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;
import jdk.internal.clang.TypeKind;
import jdk.internal.foreign.abi.aarch64.AArch64ABI;
import jdk.internal.foreign.abi.x64.sysv.SysVx64ABI;
import jdk.internal.foreign.abi.x64.windows.Windowsx64ABI;

import java.util.Optional;

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
        switch(t.kind()) {
            case Char_S:
            case Char_U:
            case UChar:
            case SChar:
                return C_SCHAR;
            case UShort:
            case Short:
                return C_SHORT;
            case Int:
            case UInt:
                return C_INT;
            case ULong:
            case Long:
                return C_LONG;
            case ULongLong:
            case LongLong:
                return C_LONGLONG;
            case UInt128:
            case Int128:
                throw new UnsupportedOperationException();
            case Enum:
                return valueLayoutForSize(t.size() * 8);
            case Bool:
                return C_BOOL;
            case Float:
                return C_FLOAT;
            case Double:
                return C_DOUBLE;
            case LongDouble:
                return C_LONGDOUBLE;
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
                    throw new IllegalStateException("Unknown type with same canonical type: " + t.spelling());
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
            return t.layout().orElseThrow(UnsupportedOperationException::new);
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
            throw new UnsupportedOperationException();
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

    public static Optional<FunctionDescriptor> getDescriptor(jdk.incubator.jextract.Type.Function t) {
        try {
            MemoryLayout[] args = t.argumentTypes().stream()
                    .map(LayoutUtils::getLayoutInternal)
                    .toArray(MemoryLayout[]::new);
            if (t.returnType() instanceof jdk.incubator.jextract.Type.Primitive &&
                    ((jdk.incubator.jextract.Type.Primitive) t.returnType()).kind() == jdk.incubator.jextract.Type.Primitive.Kind.Void) {
                return Optional.of(FunctionDescriptor.ofVoid(args));
            } else {
                return Optional.of(FunctionDescriptor.of(getLayoutInternal(t.returnType()), args));
            }
        } catch (Throwable ex) {
            return Optional.empty();
        }
    }

    public static ValueLayout valueLayoutForSize(long size) {
        switch ((int)size) {
            case 8: return INT8;
            case 16: return INT16;
            case 32: return INT32;
            case 64: return INT64;
            default:
                throw new IllegalStateException("Cannot infer container layout");
        }
    }
    
    // platform-dependent layouts

    public static final ValueLayout C_BOOL;
    public static final ValueLayout C_CHAR;
    public static final ValueLayout C_UCHAR;
    public static final ValueLayout C_SCHAR;
    public static final ValueLayout C_SHORT;
    public static final ValueLayout C_USHORT;
    public static final ValueLayout C_INT;
    public static final ValueLayout C_UINT;
    public static final ValueLayout C_LONG;
    public static final ValueLayout C_ULONG;
    public static final ValueLayout C_LONGLONG;
    public static final ValueLayout C_ULONGLONG;
    public static final ValueLayout C_FLOAT;
    public static final ValueLayout C_DOUBLE;
    public static final ValueLayout C_LONGDOUBLE;
    public static final ValueLayout C_POINTER;

    public static final ValueLayout INT8;
    public static final ValueLayout INT16;
    public static final ValueLayout INT32;
    public static final ValueLayout INT64;

    static {
        SystemABI abi = SystemABI.getInstance();
        if (abi instanceof SysVx64ABI) {
            C_BOOL = MemoryLayouts.SysV.C_BOOL;
            C_CHAR = MemoryLayouts.SysV.C_CHAR;
            C_UCHAR = MemoryLayouts.SysV.C_UCHAR;
            C_SCHAR = MemoryLayouts.SysV.C_SCHAR;
            C_SHORT = MemoryLayouts.SysV.C_SHORT;
            C_USHORT = MemoryLayouts.SysV.C_USHORT;
            C_INT = MemoryLayouts.SysV.C_INT;
            C_UINT = MemoryLayouts.SysV.C_UINT;
            C_LONG = MemoryLayouts.SysV.C_LONG;
            C_ULONG = MemoryLayouts.SysV.C_ULONG;
            C_LONGLONG = MemoryLayouts.SysV.C_LONGLONG;
            C_ULONGLONG = MemoryLayouts.SysV.C_ULONGLONG;
            C_FLOAT = MemoryLayouts.SysV.C_FLOAT;
            C_DOUBLE = MemoryLayouts.SysV.C_DOUBLE;
            C_LONGDOUBLE = MemoryLayouts.SysV.C_LONGDOUBLE;
            C_POINTER = MemoryLayouts.SysV.C_POINTER;
            INT8 = C_BOOL;
            INT16 = C_SHORT;
            INT32 = C_INT;
            INT64 = C_LONG;
        } else if (abi instanceof Windowsx64ABI) {
            C_BOOL = MemoryLayouts.WinABI.C_BOOL;
            C_CHAR = MemoryLayouts.WinABI.C_CHAR;
            C_UCHAR = MemoryLayouts.WinABI.C_UCHAR;
            C_SCHAR = MemoryLayouts.WinABI.C_SCHAR;
            C_SHORT = MemoryLayouts.WinABI.C_SHORT;
            C_USHORT = MemoryLayouts.WinABI.C_USHORT;
            C_INT = MemoryLayouts.WinABI.C_INT;
            C_UINT = MemoryLayouts.WinABI.C_UINT;
            C_LONG = MemoryLayouts.WinABI.C_LONG;
            C_ULONG = MemoryLayouts.WinABI.C_ULONG;
            C_LONGLONG = MemoryLayouts.WinABI.C_LONGLONG;
            C_ULONGLONG = MemoryLayouts.WinABI.C_ULONGLONG;
            C_FLOAT = MemoryLayouts.WinABI.C_FLOAT;
            C_DOUBLE = MemoryLayouts.WinABI.C_DOUBLE;
            C_LONGDOUBLE = C_DOUBLE;
            C_POINTER = MemoryLayouts.WinABI.C_POINTER;
            INT8 = C_BOOL;
            INT16 = C_SHORT;
            INT32 = C_INT;
            INT64 = C_LONGLONG;
        } else if (abi instanceof AArch64ABI) {
            C_BOOL = MemoryLayouts.AArch64ABI.C_BOOL;
            C_CHAR = MemoryLayouts.AArch64ABI.C_CHAR;
            C_UCHAR = MemoryLayouts.AArch64ABI.C_UCHAR;
            C_SCHAR = MemoryLayouts.AArch64ABI.C_SCHAR;
            C_SHORT = MemoryLayouts.AArch64ABI.C_SHORT;
            C_USHORT = MemoryLayouts.AArch64ABI.C_USHORT;
            C_INT = MemoryLayouts.AArch64ABI.C_INT;
            C_UINT = MemoryLayouts.AArch64ABI.C_UINT;
            C_LONG = MemoryLayouts.AArch64ABI.C_LONG;
            C_ULONG = MemoryLayouts.AArch64ABI.C_ULONG;
            C_LONGLONG = MemoryLayouts.AArch64ABI.C_LONGLONG;
            C_ULONGLONG = MemoryLayouts.AArch64ABI.C_ULONGLONG;
            C_FLOAT = MemoryLayouts.AArch64ABI.C_FLOAT;
            C_DOUBLE = MemoryLayouts.AArch64ABI.C_DOUBLE;
            C_LONGDOUBLE = C_DOUBLE;
            C_POINTER = MemoryLayouts.AArch64ABI.C_POINTER;
            INT8 = C_BOOL;
            INT16 = C_SHORT;
            INT32 = C_INT;
            INT64 = C_LONG;
        } else {
            throw new ExceptionInInitializerError();
        }
    }
}
