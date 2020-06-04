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
package jdk.internal.foreign.abi.x64.windows;

import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.ValueLayout;

import static jdk.incubator.foreign.CSupport.Win64.VARARGS_ATTRIBUTE_NAME;

enum TypeClass {
    STRUCT_REGISTER,
    STRUCT_REFERENCE,
    POINTER,
    INTEGER,
    FLOAT,
    VARARG_FLOAT;


    private static TypeClass classifyValueType(ValueLayout type) {
        CSupport.Win64.ArgumentClass clazz = Windowsx64Linker.argumentClassFor(type);
        if (clazz == null) {
            //padding not allowed here
            throw new IllegalStateException("Unexpected value layout: could not determine ABI class");
        }

        // No 128 bit integers in the Windows C ABI. There are __m128(i|d) intrinsic types but they act just
        // like a struct when passing as an argument (passed by pointer).
        // https://docs.microsoft.com/en-us/cpp/cpp/m128?view=vs-2019

        // x87 is ignored on Windows:
        // "The x87 register stack is unused, and may be used by the callee,
        // but must be considered volatile across function calls."
        // https://docs.microsoft.com/en-us/cpp/build/x64-calling-convention?view=vs-2019

        if (clazz == CSupport.Win64.ArgumentClass.INTEGER) {
            return INTEGER;
        } else if(clazz == CSupport.Win64.ArgumentClass.POINTER) {
            return POINTER;
        } else if (clazz == CSupport.Win64.ArgumentClass.FLOAT) {
            if (type.attribute(VARARGS_ATTRIBUTE_NAME)
                    .map(String.class::cast)
                    .map(Boolean::parseBoolean).orElse(false)) {
                return VARARG_FLOAT;
            }
            return FLOAT;
        }
        throw new IllegalArgumentException("Unknown ABI class: " + clazz);
    }

    static boolean isRegisterAggregate(MemoryLayout type) {
        long size = type.byteSize();
        return size == 1
            || size == 2
            || size == 4
            || size == 8;
    }

    private static TypeClass classifyStructType(MemoryLayout layout) {
        if (isRegisterAggregate(layout)) {
            return STRUCT_REGISTER;
        }
        return STRUCT_REFERENCE;
    }

    static TypeClass typeClassFor(MemoryLayout type) {
        if (type instanceof ValueLayout) {
            return classifyValueType((ValueLayout) type);
        } else if (type instanceof GroupLayout) {
            return classifyStructType(type);
        } else if (type instanceof SequenceLayout) {
            return INTEGER;
        } else {
            throw new IllegalArgumentException("Unhandled type " + type);
        }
    }
}
