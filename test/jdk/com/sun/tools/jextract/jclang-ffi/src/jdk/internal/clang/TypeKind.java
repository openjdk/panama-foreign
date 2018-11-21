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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public enum TypeKind {

    Invalid(0),
    Unexposed(1),
    Void(2),
    Bool(3),
    Char_U(4),
    UChar(5),
    Char16(6),
    Char32(7),
    UShort(8),
    UInt(9),
    ULong(10),
    ULongLong(11),
    UInt128(12),
    Char_S(13),
    SChar(14),
    WChar(15),
    Short(16),
    Int(17),
    Long(18),
    LongLong(19),
    Int128(20),
    Float(21),
    Double(22),
    LongDouble(23),
    NullPtr(24),
    Overload(25),
    Dependent(26),
    ObjCId(27),
    ObjCClass(28),
    ObjCSel(29),
    Float128(30),
    Half(31),
    Complex(100),
    Pointer(101),
    BlockPointer(102),
    LValueReference(103),
    RValueReference(104),
    Record(105),
    Enum(106),
    Typedef(107),
    ObjCInterface(108),
    ObjCObjectPointer(109),
    FunctionNoProto(110),
    FunctionProto(111),
    ConstantArray(112),
    Vector(113),
    IncompleteArray(114),
    VariableArray(115),
    DependentSizedArray(116),
    MemberPointer(117),
    Auto(118),
    Elaborated(119);

    private final int value;

    TypeKind(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    private final static Map<Integer, TypeKind> lookup;

    static {
        lookup = new HashMap<>();
        for (TypeKind e: TypeKind.values()) {
            lookup.put(e.value(), e);
        }
    }

    public final static TypeKind valueOf(int value) {
        TypeKind x = lookup.get(value);
        if (null == x) {
            throw new NoSuchElementException("kind = " + value);
        }
        return x;
    }
}
