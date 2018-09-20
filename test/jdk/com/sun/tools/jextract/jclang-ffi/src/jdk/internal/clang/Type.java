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

package jdk.internal.clang;

import clang.Index.CXType;
import java.foreign.Scope;
import java.foreign.memory.Pointer;

public class Type {
    private final CXType type;
    Type(CXType type) {
        this.type = type;
    }

    // Function Types
    public boolean isVariadic() {
        return LibClang.lib.clang_isFunctionTypeVariadic(type) != 0;
    }
    public Type resultType() {
        return new Type(LibClang.lib.clang_getResultType(type));
    }
    public int numberOfArgs() {
        return LibClang.lib.clang_getNumArgTypes(type);
    }
    public Type argType(int idx) {
        return new Type(LibClang.lib.clang_getArgType(type, idx));
    }
    private int getCallingConvention1() {
        return LibClang.lib.clang_getFunctionTypeCallingConv(type);
    }

    public CallingConvention getCallingConvention() {
        int v = getCallingConvention1();
        return CallingConvention.valueOf(v);
    }

    // Pointer type
    public Type getPointeeType() {
        return new Type(LibClang.lib.clang_getPointeeType(type));
    }

    // array/vector type
    public Type getElementType() {
        return new Type(LibClang.lib.clang_getElementType(type));
    }
    public long getNumberOfElements() {
        return LibClang.lib.clang_getNumElements(type);
    }

    // Struct/RecordType
    public long getOffsetOf(String fieldName) {
        try (Scope s = Scope.newNativeScope()) {
            Pointer<Byte> cfname = s.allocateCString(fieldName);
            return LibClang.lib.clang_Type_getOffsetOf(type, cfname);
        }
    }

    // Typedef
    public Type canonicalType() {
        return new Type(LibClang.lib.clang_getCanonicalType(type));
    }

    public String spelling() {
        return LibClang.CXStrToString(LibClang.lib.clang_getTypeSpelling(type));
    }

    public int kind1() {
        return type.kind$get();
    }

    public long size() {
        return LibClang.lib.clang_Type_getSizeOf(type);
    }

    public TypeKind kind() {
        int v = kind1();
        return TypeKind.valueOf(v);
    }

    public Cursor getDeclarationCursor() {
        return new Cursor(LibClang.lib.clang_getTypeDeclaration(type));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Type)) {
            return false;
        }
        return LibClang.lib.clang_equalTypes(type, ((Type)other).type) != 0;
    }

    @Override
    public int hashCode() {
        return spelling().hashCode();
    }
}
