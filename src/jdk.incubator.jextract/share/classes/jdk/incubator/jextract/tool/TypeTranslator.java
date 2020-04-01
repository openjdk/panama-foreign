/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package jdk.incubator.jextract.tool;

import jdk.incubator.foreign.SystemABI;
import jdk.incubator.jextract.Type;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;

import java.lang.invoke.MethodType;

public class TypeTranslator implements Type.Visitor<Class<?>, Void> {
    @Override
    public Class<?> visitPrimitive(Type.Primitive t, Void aVoid) {
        if (t.layout().isEmpty()) {
            return void.class;
        } else {
            return layoutToClass(isFloatingPoint(t), t.layout().orElseThrow(UnsupportedOperationException::new));
        }
    }

    private boolean isFloatingPoint(Type.Primitive t) {
        switch (t.kind()) {
            case Float:
            case Float128:
            case HalfFloat:
            case Double:
            case LongDouble:
                return true;
            default:
                return false;
        }
    }

    static String typeToLayoutName(SystemABI.Type type) {
        return switch (type) {
            case BOOL -> "C_BOOL";
            case SIGNED_CHAR -> "C_SCHAR";
            case UNSIGNED_CHAR -> "C_UCHAR";
            case CHAR -> "C_CHAR";
            case SHORT -> "C_SHORT";
            case UNSIGNED_SHORT -> "C_USHORT";
            case INT -> "C_INT";
            case UNSIGNED_INT -> "C_UINT";
            case LONG -> "C_LONG";
            case UNSIGNED_LONG -> "C_ULONG";
            case LONG_LONG -> "C_LONGLONG";
            case UNSIGNED_LONG_LONG -> "C_ULONGLONG";
            case FLOAT -> "C_FLOAT";
            case DOUBLE -> "C_DOUBLE";
            case LONG_DOUBLE -> "C_LONGDOUBLE";
            case POINTER -> "C_POINTER";
            default -> {
                throw new RuntimeException("should not reach here: " + type);
            }
        };
    }

    static Class<?> layoutToClass(boolean fp, MemoryLayout layout) {
        switch ((int)layout.bitSize()) {
            case 8: return byte.class;
            case 16: return short.class;
            case 32: return !fp ? int.class : float.class;
            case 64:
            case 128: return !fp ? long.class : double.class;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Class<?> visitDelegated(Type.Delegated t, Void aVoid) {
        return t.kind() == Type.Delegated.Kind.POINTER ?
                MemoryAddress.class :
                t.type().accept(this, null);
    }

    @Override
    public Class<?> visitFunction(Type.Function t, Void aVoid) {
        return MemoryAddress.class; // function pointer
    }

    @Override
    public Class<?> visitDeclared(Type.Declared t, Void aVoid) {
        switch (t.tree().kind()) {
            case UNION:
            case STRUCT:
                return MemorySegment.class;
            case ENUM:
                return layoutToClass(false, t.tree().layout().orElseThrow(UnsupportedOperationException::new));
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Class<?> visitArray(Type.Array t, Void aVoid) {
        if (t.kind() == Type.Array.Kind.VECTOR) {
            throw new UnsupportedOperationException();
        } else {
            return MemorySegment.class;
        }
    }

    @Override
    public Class<?> visitType(Type t, Void aVoid) {
        throw new UnsupportedOperationException();
    }

    Class<?> getJavaType(Type t) {
        return t.accept(this, null);
    }

    MethodType getMethodType(Type.Function type) {
        MethodType mtype = MethodType.methodType(getJavaType(type.returnType()));
        for (Type arg : type.argumentTypes()) {
            mtype = mtype.appendParameterTypes(getJavaType(arg));
        }
        if (type.varargs()) {
            mtype = mtype.appendParameterTypes(Object[].class);
        }
        return mtype;
    }
}
