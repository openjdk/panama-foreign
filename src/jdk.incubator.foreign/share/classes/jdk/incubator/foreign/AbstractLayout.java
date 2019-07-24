/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package jdk.incubator.foreign;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

abstract class AbstractLayout implements MemoryLayout {
    private final OptionalLong alignment;
    private final Map<String, Constable> annotations;

    public AbstractLayout(OptionalLong alignment, Map<String, Constable> annotations) {
        this.alignment = alignment;
        this.annotations = Collections.unmodifiableMap(annotations);
    }

    Map<String, Constable> annotations() {
        return annotations;
    }

    OptionalLong optAlignment() {
        return alignment;
    }

    Optional<String> optName() {
        return Optional.ofNullable((String)annotations.get(NAME));
    }

    @Override
    public AbstractLayout withName(String name) {
        return withAnnotation(NAME, name);
    }

    @SuppressWarnings("unchecked")
    public <Z extends AbstractLayout> Z withAnnotation(String name, Constable value) {
        Map<String, Constable> new_annos = new HashMap<>(annotations);
        new_annos.put(name, value);
        return (Z)dup(alignment, new_annos);
    }

    @Override
    public final Optional<String> name() {
        return optName();
    }

    abstract AbstractLayout dup(OptionalLong alignment, Map<String, Constable> name);

    abstract long naturalAlignmentBits();

    @Override
    public AbstractLayout withBitAlignment(long alignmentBits) throws IllegalArgumentException {
        checkAlignment(alignmentBits);
        return dup(OptionalLong.of(alignmentBits), annotations);
    }

    void checkAlignment(long alignmentBitCount) {
        if (alignmentBitCount <= 0 || //alignment must be positive
                ((alignmentBitCount & (alignmentBitCount - 1)) != 0L) || //alignment must be a power of two
                (alignmentBitCount < 8)) { //alignment must be greater than 8
            throw new IllegalArgumentException("Invalid alignment: " + alignmentBitCount);
        }
    }

    static void checkSize(long size) {
        checkSize(size, false);
    }

    static void checkSize(long size, boolean includeZero) {
        if (size < 0 || (!includeZero && size == 0)) {
            throw new IllegalArgumentException("Invalid size for layout: " + size);
        }
    }

    @Override
    public final long bitAlignment() {
        return alignment.orElse(naturalAlignmentBits());
    }

    String decorateLayoutString(String s) {
        if (optName().isPresent()) {
            s = String.format("%s(%s)", s, optName().get());
        }
        if (alignment.isPresent()) {
            s = alignment.getAsLong() + "%" + s;
        }
        return s;
    }

    @Override
    public int hashCode() {
        return optName().hashCode() << alignment.orElse(0L);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AbstractLayout)) {
            return false;
        }

        return Objects.equals(alignment, ((AbstractLayout)other).alignment) &&
                Objects.equals(annotations, ((AbstractLayout)other).annotations);
    }

    static final String NAME = "name";

    /*** Helper constants for implementing Layout::describeConstable ***/

    static final ClassDesc CD_LAYOUT = MemoryLayout.class.describeConstable().get();

    static final ClassDesc CD_VALUE_LAYOUT = ValueLayout.class.describeConstable().get();

    static final ClassDesc CD_SEQUENCE_LAYOUT = SequenceLayout.class.describeConstable().get();

    static final ClassDesc CD_GROUP_LAYOUT = GroupLayout.class.describeConstable().get();

    static final ClassDesc CD_BYTEORDER = ByteOrder.class.describeConstable().get();

    static final ClassDesc CD_FUNCTION_DESC = FunctionDescriptor.class.describeConstable().get();

    static final ConstantDesc BIG_ENDIAN = DynamicConstantDesc.ofNamed(ConstantDescs.BSM_GET_STATIC_FINAL, "BIG_ENDIAN", CD_BYTEORDER, CD_BYTEORDER);

    static final ConstantDesc LITTLE_ENDIAN = DynamicConstantDesc.ofNamed(ConstantDescs.BSM_GET_STATIC_FINAL, "LITTLE_ENDIAN", CD_BYTEORDER, CD_BYTEORDER);

    static final ConstantDesc TRUE = DynamicConstantDesc.ofNamed(ConstantDescs.BSM_GET_STATIC_FINAL, "TRUE", ConstantDescs.CD_Boolean, ConstantDescs.CD_Boolean);

    static final ConstantDesc FALSE = DynamicConstantDesc.ofNamed(ConstantDescs.BSM_GET_STATIC_FINAL, "FALSE", ConstantDescs.CD_Boolean, ConstantDescs.CD_Boolean);

    static final MethodHandleDesc MH_PADDING = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_STATIC, CD_LAYOUT, "ofPaddingBits",
                MethodTypeDesc.of(CD_LAYOUT, ConstantDescs.CD_long));

    static final MethodHandleDesc MH_VALUE = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_STATIC, CD_LAYOUT, "ofValueBits",
                MethodTypeDesc.of(CD_VALUE_LAYOUT, ConstantDescs.CD_long, CD_BYTEORDER));

    static final MethodHandleDesc MH_SIZED_SEQUENCE = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_STATIC, CD_LAYOUT, "ofSequence",
                MethodTypeDesc.of(CD_SEQUENCE_LAYOUT, ConstantDescs.CD_long, CD_LAYOUT));

    static final MethodHandleDesc MH_UNSIZED_SEQUENCE = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_STATIC, CD_LAYOUT, "ofSequence",
                MethodTypeDesc.of(CD_SEQUENCE_LAYOUT, CD_LAYOUT));

    static final MethodHandleDesc MH_STRUCT = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_STATIC, CD_LAYOUT, "ofStruct",
                MethodTypeDesc.of(CD_GROUP_LAYOUT, CD_LAYOUT.arrayType()));

    static final MethodHandleDesc MH_UNION = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_STATIC, CD_LAYOUT, "ofUnion",
                MethodTypeDesc.of(CD_GROUP_LAYOUT, CD_LAYOUT.arrayType()));

    static final MethodHandleDesc MH_VOID_FUNCTION = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_STATIC, CD_FUNCTION_DESC, "ofVoid",
                MethodTypeDesc.of(CD_FUNCTION_DESC, ConstantDescs.CD_boolean, CD_LAYOUT.arrayType()));

    static final MethodHandleDesc MH_FUNCTION = MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.INTERFACE_STATIC, CD_FUNCTION_DESC, "of",
                MethodTypeDesc.of(CD_FUNCTION_DESC, CD_LAYOUT, ConstantDescs.CD_boolean, CD_LAYOUT.arrayType()));
}
