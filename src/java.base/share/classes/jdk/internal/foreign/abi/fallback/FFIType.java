/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi.fallback;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.SegmentScope;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

final class FFIType {
    private static final long SIZE_BYTES = LibFallback.FFITypeSizeof();

    private static MemorySegment makeStructType(List<MemoryLayout> elements, FFIABI abi, SegmentScope scope) {
        MemorySegment elementsSeg = MemorySegment.allocateNative((elements.size() + 1) * ADDRESS.byteSize(), scope);
        int i = 0;
        for (; i < elements.size(); i++) {
            MemoryLayout elementLayout = elements.get(i);
            MemorySegment elementType = toFFIType(scope, inferCarrier(elementLayout), elementLayout, abi);
            elementsSeg.setAtIndex(ADDRESS, i, elementType);
        }
        // elements array is null-terminated
        elementsSeg.setAtIndex(ADDRESS, i, MemorySegment.NULL);

        MemorySegment ffiType = MemorySegment.allocateNative(SIZE_BYTES, scope);
        LibFallback.FFITypeSetType(ffiType.address(), LibFallback.STRUCT_TAG);
        LibFallback.FFITypeSetElements(ffiType.address(), elementsSeg.address());

        return ffiType;
    }

    private static final Map<Class<?>, MemorySegment> CARRIER_TO_TYPE = Map.of(
        boolean.class, LibFallback.UINT8_TYPE,
        byte.class, LibFallback.SINT8_TYPE,
        short.class, LibFallback.SINT16_TYPE,
        char.class, LibFallback.UINT16_TYPE,
        int.class, LibFallback.SINT32_TYPE,
        long.class, LibFallback.SINT64_TYPE,
        float.class, LibFallback.FLOAT_TYPE,
        double.class, LibFallback.DOUBLE_TYPE,
        MemorySegment.class, LibFallback.POINTER_TYPE
    );

    static MemorySegment toFFIType(SegmentScope cifScope, Class<?> type, MemoryLayout layout, FFIABI abi) {
        if (layout instanceof GroupLayout grpl) {
            assert type == MemorySegment.class;
            if (grpl instanceof StructLayout) {
                // libffi doesn't want our padding
                List<MemoryLayout> filteredLayouts = grpl.memberLayouts().stream()
                        .filter(Predicate.not(PaddingLayout.class::isInstance))
                        .toList();
                MemorySegment structType = makeStructType(filteredLayouts, abi, cifScope);
                verifyStructType(grpl, filteredLayouts, structType, abi);
                return structType;
            }
            assert grpl instanceof UnionLayout;
            throw new UnsupportedOperationException("No unions (TODO)");
        } else if (layout instanceof SequenceLayout sl) {
            List<MemoryLayout> elements = Collections.nCopies(Math.toIntExact(sl.elementCount()), sl.elementLayout());
            return makeStructType(elements, abi, cifScope);
        }
        return Objects.requireNonNull(CARRIER_TO_TYPE.get(type));
    }

    // verify layout against what libffi set
    private static void verifyStructType(GroupLayout grpl, List<MemoryLayout> filteredLayouts, MemorySegment structType,
                                         FFIABI abi) {
        try (Arena verifyArena = Arena.openConfined()) {
            MemorySegment offsetsOut = verifyArena.allocate(ADDRESS.byteSize() * filteredLayouts.size());
            LibFallback.getStructOffsets(structType, offsetsOut, abi);
            for (int i = 0; i < filteredLayouts.size(); i++) {
                MemoryLayout element = filteredLayouts.get(i);
                final int finalI = i;
                element.name().ifPresent(name -> {
                    long layoutOffset = grpl.byteOffset(MemoryLayout.PathElement.groupElement(name));
                    long ffiOffset = switch ((int) ADDRESS.bitSize()) {
                        case 64 -> offsetsOut.getAtIndex(JAVA_LONG, finalI);
                        case 32 -> offsetsOut.getAtIndex(JAVA_INT, finalI);
                        default -> throw new IllegalStateException("Address size not supported: " + ADDRESS.byteSize());
                    };
                    if (ffiOffset != layoutOffset) {
                        throw new IllegalArgumentException("Invalid group layout." +
                                " Offset of '" + name + "': " + layoutOffset + " != " + ffiOffset);
                    }
                });
            }
        }
    }

    private static Class<?> inferCarrier(MemoryLayout element) {
        if (element instanceof ValueLayout vl) {
            return vl.carrier();
        } else if (element instanceof GroupLayout) {
            return MemorySegment.class;
        } else if (element instanceof SequenceLayout) {
            return null; // field of struct
        }
        throw new IllegalArgumentException("Can not infer carrier for: " + element);
    }
}
