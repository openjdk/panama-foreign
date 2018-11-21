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

import java.nio.ByteBuffer;

public class Type extends StructType {
    Type(ByteBuffer buf) {
        super(buf);
    }

    // Function Types
    public native boolean isVariadic();
    public native Type resultType();
    public native int numberOfArgs();
    public native Type argType(int idx);
    private native int getCallingConvention1();

    public CallingConvention getCallingConvention() {
        int v = getCallingConvention1();
        return CallingConvention.valueOf(v);
    }

    // Pointer type
    public native Type getPointeeType();

    // array/vector type
    public native Type getElementType();
    public native long getNumberOfElements();

    // Struct/RecordType
    public native long getOffsetOf(String fieldName);

    // Typedef
    public native Type canonicalType();

    public native String spelling();

    public native int kind1();

    public native long size();

    public TypeKind kind() {
        int v = kind1();
        // FIXME: assert(v == getData().getInt(0));
        return TypeKind.valueOf(v);
    }

    public native Cursor getDeclarationCursor();

    public native boolean equalType(Type other);

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Type)) {
            return false;
        }
        return equalType((Type)other);
    }

    @Override
    public int hashCode() {
        return spelling().hashCode();
    }
}
