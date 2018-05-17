/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

package java.nicl;

import java.nicl.layout.Value;
import java.nicl.types.LayoutType;

/**
 * This class defines constants modelling standard primitive types in the C programming language.
 */
public final class NativeTypes {

    /**
     * The {@code void} type.
     */
    public static LayoutType<?> VOID = LayoutType.ofVoid(Value.ofUnsignedInt(0));

    /**
     * The {@code int8_t} type.
     */
    public static LayoutType<Byte> INT8 = LayoutType.ofByte(Value.ofSignedInt(8));

    /**
     * The {@code uint8_t} type.
     */
    public static LayoutType<Byte> UINT8 = LayoutType.ofByte(Value.ofUnsignedInt(8));

    /**
     * The {@code int16_t} type.
     */
    public static LayoutType<Short> INT16 = LayoutType.ofShort(Value.ofSignedInt(16));

    /**
     * The {@code uint16_t} type.
     */
    public static LayoutType<Short> UINT16 = LayoutType.ofShort(Value.ofUnsignedInt(16));

    /**
     * The {@code int32_t} type.
     */
    public static LayoutType<Integer> INT32 = LayoutType.ofInt(Value.ofSignedInt(32));

    /**
     * The {@code uint32_t} type.
     */
    public static LayoutType<Integer> UINT32 = LayoutType.ofInt(Value.ofUnsignedInt(32));

    /**
     * The {@code int64_t} type.
     */
    public static LayoutType<Long> INT64 = LayoutType.ofLong(Value.ofSignedInt(64));

    /**
     * The {@code uint64_t} type.
     */
    public static LayoutType<Long> UINT64 = LayoutType.ofLong(Value.ofUnsignedInt(64));

    /**
     * The {@code float} type.
     */
    public static LayoutType<Float> FLOAT = LayoutType.ofFloat(Value.ofFloatingPoint(32));

    /**
     * The {@code double} type.
     */
    public static LayoutType<Double> DOUBLE = LayoutType.ofDouble(Value.ofFloatingPoint(64));
}
