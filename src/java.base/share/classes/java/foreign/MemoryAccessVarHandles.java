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
package java.foreign;

import jdk.internal.access.JavaLangInvokeAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.foreign.Utils;
import sun.invoke.util.Wrapper;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * A class with several factory methods for constructing memory access VarHandles.
 *
 * The VarHandles created through this class access memory at the address represented by
 * a given {@link java.foreign.MemoryAddress} argument, plus an offset (which may also be zero).
 *
 * The offset is computed upon access by multiplying the set of given access coordinates by a set of
 * scale factors, and adding a fixed offset to the result.
 */
public class MemoryAccessVarHandles {

    private static JavaLangInvokeAccess JLI = SharedSecrets.getJavaLangInvokeAccess();

    /**
     * Creates a memory access var handle with the given carrier type.
     *
     * The resulting memory access var handle has a single {@link java.foreign.MemoryAddress} as a parameter
     * and returns the carrier type.
     *
     * The alignment constraint for the resulting memory access var handle is the same as the in memory size of the
     * carrier type. The used byte order is the machine native byte order. The access offset is zero,
     * and there are no access coordinates.
     *
     * @param carrier the carrier type. Valid carriers are {@code byte}, {@code short}, {@code char}, {@code int},
     * {@code float}, {@code long}, and {@code double}.
     * @return the new memory access var handle
     * @throws IllegalArgumentException when an illegal carrier type is used
     */
    public static VarHandle dereferenceHandle(Class<?> carrier) throws IllegalArgumentException {
        return dereferenceHandle(carrier,
                carrierSize(carrier),
                ByteOrder.nativeOrder());
    }

    /**
     * Creates a memory access var handle with the given carrier type, alignment constraints and byte order.
     *
     * The resulting memory access var handle has a single {@link java.foreign.MemoryAddress} as a parameter
     * and returns the carrier type.
     *
     * The access offset is zero, and there are no access coordinates.
     *
     * @param carrier the carrier type. Valid carriers are {@code byte}, {@code short}, {@code char}, {@code int},
     * {@code float}, {@code long}, and {@code double}.
     * @param align the alignment constraints (in bytes). Must be a power of two.
     * @param byteOrder the required byte order.
     * @return the new memory access var handle
     * @throws IllegalArgumentException when an illegal carrier type is used
     */
    public static VarHandle dereferenceHandle(Class<?> carrier, long align, ByteOrder byteOrder) throws IllegalArgumentException {
        if (!carrier.isPrimitive() || carrier == void.class || carrier == boolean.class) {
            throw new IllegalArgumentException("Illegal carrier: " + carrier.getSimpleName());
        }

        if (align <= 0
                || (align & (align - 1)) != 0) { // is power of 2?
            throw new IllegalArgumentException("Bad alignment: " + align);
        }

        return JLI.memoryAddressViewVarHandle(carrier, align, byteOrder, 0, new long[]{});
    }

    /**
     * Creates a memory access var handle with a fixed offset added to the access offset.
     *
     * @param handle the base handle to adapt
     * @param offset the offset, in bytes. Must be positive or zero.
     * @return the new memory access var handle
     * @throws IllegalArgumentException when a memory access var handle is passed that is not a memory access var handle,
     * if the offset is negative, or incompatible with the alignment constraints.
     * or when a negative offset is passed.
     */
    public static VarHandle offsetHandle(VarHandle handle, long offset) throws IllegalArgumentException {
        if (offset < 0) {
            throw new IllegalArgumentException("Illegal offset: " + offset);
        }

        long align = JLI.memoryAddressAlignment(handle);

        if (offset % align != 0) {
            throw new IllegalArgumentException("Offset " + offset + " does not conform to alignment " + align);
        }

        return JLI.memoryAddressViewVarHandle(
                JLI.memoryAddressCarrier(handle),
                align,
                JLI.memoryAddressByteOrder(handle),
                JLI.memoryAddressOffset(handle) + offset,
                JLI.memoryAddressStrides(handle));
    }

    /**
     * Creates a memory access var handle with a variable offset added to the access offset.
     *
     * The variable offset is given as an additional {@code long} coordinate argument when calling an
     * accessor method on the resulting memory access var handle. This coordinate is multiplied with the given
     * fixed scale factor to compute the effective offset.
     *
     * @param handle the base handle to adapt
     * @param scale the scale factor by which to multiply the coordinate value. Must be greater than zero.
     * @return the new memory access var handle
     * @throws IllegalArgumentException when a memory access var handle is passed that is not a memory access var handle,
     * if the scale factor is negative, or incompatible with the alignment constraints, or the current memory access var handle offset.
     * or when a negative scale or zero is passed.
     */
    public static VarHandle elementHandle(VarHandle handle, long scale) throws IllegalArgumentException {
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale factor must be positive: " + scale);
        }

        long align = JLI.memoryAddressAlignment(handle);

        if (scale % align != 0) {
            throw new IllegalArgumentException("Scale factor " + scale + " does not conform to alignment " + align);
        }

        long offset = JLI.memoryAddressOffset(handle);
        Class<?> carrier = JLI.memoryAddressCarrier(handle);

        if (scale < offset + carrierSize(carrier)) {
            throw new IllegalArgumentException("Scale factor " + scale + " is too small");
        }

        long[] strides = JLI.memoryAddressStrides(handle);
        long[] newStrides = new long[strides.length + 1];
        System.arraycopy(strides, 0, newStrides, 1, strides.length);
        newStrides[0] = scale;

        return JLI.memoryAddressViewVarHandle(
                JLI.memoryAddressCarrier(handle),
                align,
                JLI.memoryAddressByteOrder(handle),
                offset,
                newStrides);
    }

    private static long carrierSize(Class<?> carrier) {
        long bitsAlignment = Math.max(8, Wrapper.forPrimitiveType(carrier).bitWidth());
        return Utils.bitsToBytesOrThrow(bitsAlignment, IllegalStateException::new);
    }
}
