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
import jdk.incubator.foreign.SystemABI;
import jdk.incubator.foreign.ValueLayout;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Type;
import jdk.internal.foreign.abi.SharedUtils;
import jdk.internal.foreign.abi.aarch64.AArch64ABI;
import jdk.internal.foreign.abi.x64.sysv.SysVx64ABI;
import jdk.internal.foreign.abi.x64.windows.Windowsx64ABI;

import java.util.Optional;

/**
 * General Layout utility functions
 */
public final class LayoutUtils {
    private static SystemABI abi = SharedUtils.getSystemABI();
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
            case UChar, Char_U:
                return C_UCHAR;
            case SChar, Char_S:
                return C_SCHAR;
            case Short:
                return C_SHORT;
            case UShort:
                return C_USHORT;
            case Int:
                return C_INT;
            case UInt:
                return C_UINT;
            case ULong:
                return C_ULONG;
            case Long:
                return C_LONG;
            case ULongLong:
                return C_ULONGLONG;
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
            case Complex:
                if (!abi.name().equals(SystemABI.ABI_SYSV)) {
                    throw new UnsupportedOperationException("unsupported: " + t.kind());
                }
                return SystemABI.SysV.C_COMPLEX_LONGDOUBLE;
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
        if (abi instanceof SysVx64ABI) {
            C_BOOL = SystemABI.SysV.C_BOOL;
            C_CHAR = SystemABI.SysV.C_CHAR;
            C_UCHAR = SystemABI.SysV.C_CHAR;
            C_SCHAR = SystemABI.SysV.C_CHAR;
            C_SHORT = SystemABI.SysV.C_SHORT;
            C_USHORT = SystemABI.SysV.C_SHORT;
            C_INT = SystemABI.SysV.C_INT;
            C_UINT = SystemABI.SysV.C_INT;
            C_LONG = SystemABI.SysV.C_LONG;
            C_ULONG = SystemABI.SysV.C_LONG;
            C_LONGLONG = SystemABI.SysV.C_LONGLONG;
            C_ULONGLONG = SystemABI.SysV.C_LONGLONG;
            C_FLOAT = SystemABI.SysV.C_FLOAT;
            C_DOUBLE = SystemABI.SysV.C_DOUBLE;
            C_LONGDOUBLE = SystemABI.SysV.C_LONGDOUBLE;
            C_POINTER = SystemABI.SysV.C_POINTER;
            INT8 = C_BOOL;
            INT16 = C_SHORT;
            INT32 = C_INT;
            INT64 = C_LONG;
        } else if (abi instanceof Windowsx64ABI) {
            C_BOOL = SystemABI.Win64.C_BOOL;
            C_CHAR = SystemABI.Win64.C_CHAR;
            C_UCHAR = SystemABI.Win64.C_CHAR;
            C_SCHAR = SystemABI.Win64.C_CHAR;
            C_SHORT = SystemABI.Win64.C_SHORT;
            C_USHORT = SystemABI.Win64.C_SHORT;
            C_INT = SystemABI.Win64.C_INT;
            C_UINT = SystemABI.Win64.C_INT;
            C_LONG = SystemABI.Win64.C_LONG;
            C_ULONG = SystemABI.Win64.C_LONG;
            C_LONGLONG = SystemABI.Win64.C_LONGLONG;
            C_ULONGLONG = SystemABI.Win64.C_LONGLONG;
            C_FLOAT = SystemABI.Win64.C_FLOAT;
            C_DOUBLE = SystemABI.Win64.C_DOUBLE;
            C_LONGDOUBLE = SystemABI.Win64.C_LONGDOUBLE;
            C_POINTER = SystemABI.Win64.C_POINTER;
            INT8 = C_BOOL;
            INT16 = C_SHORT;
            INT32 = C_INT;
            INT64 = C_LONGLONG;
        } else if (abi instanceof AArch64ABI) {
            C_BOOL = SystemABI.AArch64.C_BOOL;
            C_CHAR = SystemABI.AArch64.C_CHAR;
            C_UCHAR = SystemABI.AArch64.C_CHAR;
            C_SCHAR = SystemABI.AArch64.C_CHAR;
            C_SHORT = SystemABI.AArch64.C_SHORT;
            C_USHORT = SystemABI.AArch64.C_SHORT;
            C_INT = SystemABI.AArch64.C_INT;
            C_UINT = SystemABI.AArch64.C_INT;
            C_LONG = SystemABI.AArch64.C_LONG;
            C_ULONG = SystemABI.AArch64.C_LONG;
            C_LONGLONG = SystemABI.AArch64.C_LONGLONG;
            C_ULONGLONG = SystemABI.AArch64.C_LONGLONG;
            C_FLOAT = SystemABI.AArch64.C_FLOAT;
            C_DOUBLE = SystemABI.AArch64.C_DOUBLE;
            C_LONGDOUBLE = SystemABI.AArch64.C_LONGDOUBLE;
            C_POINTER = SystemABI.AArch64.C_POINTER;
            INT8 = C_BOOL;
            INT16 = C_SHORT;
            INT32 = C_INT;
            INT64 = C_LONG;
        } else {
            throw new ExceptionInInitializerError();
        }
    }
}
