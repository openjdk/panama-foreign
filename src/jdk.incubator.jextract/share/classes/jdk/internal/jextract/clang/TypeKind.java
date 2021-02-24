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
package jdk.internal.jextract.clang;

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
    Float16(32),
    ShortAccum(33),
    Accum(34),
    LongAccum(35),
    UShortAccum(36),
    UAccum(37),
    ULongAccum(38),
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
    Elaborated(119),
    Pipe(120),
    OCLImage1dRO(121),
    OCLImage1dArrayRO(122),
    OCLImage1dBufferRO(123),
    OCLImage2dRO(124),
    OCLImage2dArrayRO(125),
    OCLImage2dDepthRO(126),
    OCLImage2dArrayDepthRO(127),
    OCLImage2dMSAARO(128),
    OCLImage2dArrayMSAARO(129),
    OCLImage2dMSAADepthRO(130),
    OCLImage2dArrayMSAADepthRO(131),
    OCLImage3dRO(132),
    OCLImage1dWO(133),
    OCLImage1dArrayWO(134),
    OCLImage1dBufferWO(135),
    OCLImage2dWO(136),
    OCLImage2dArrayWO(137),
    OCLImage2dDepthWO(138),
    OCLImage2dArrayDepthWO(139),
    OCLImage2dMSAAWO(140),
    OCLImage2dArrayMSAAWO(141),
    OCLImage2dMSAADepthWO(142),
    OCLImage2dArrayMSAADepthWO(143),
    OCLImage3dWO(144),
    OCLImage1dRW(145),
    OCLImage1dArrayRW(146),
    OCLImage1dBufferRW(147),
    OCLImage2dRW(148),
    OCLImage2dArrayRW(149),
    OCLImage2dDepthRW(150),
    OCLImage2dArrayDepthRW(151),
    OCLImage2dMSAARW(152),
    OCLImage2dArrayMSAARW(153),
    OCLImage2dMSAADepthRW(154),
    OCLImage2dArrayMSAADepthRW(155),
    OCLImage3dRW(156),
    OCLSampler(157),
    OCLEvent(158),
    OCLQueue(159),
    OCLReserveID(160),
    ObjCObject(161),
    ObjCTypeParam(162),
    Attributed(163),
    OCLIntelSubgroupAVCMcePayload(164),
    OCLIntelSubgroupAVCImePayload(165),
    OCLIntelSubgroupAVCRefPayload(166),
    OCLIntelSubgroupAVCSicPayload(167),
    OCLIntelSubgroupAVCMceResult(168),
    OCLIntelSubgroupAVCImeResult(169),
    OCLIntelSubgroupAVCRefResult(170),
    OCLIntelSubgroupAVCSicResult(171),
    OCLIntelSubgroupAVCImeResultSingleRefStreamout(172),
    OCLIntelSubgroupAVCImeResultDualRefStreamout(173),
    OCLIntelSubgroupAVCImeSingleRefStreamin(174),
    OCLIntelSubgroupAVCImeDualRefStreamin(175),
    ExtVector(176),
    Atomic(177);

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
