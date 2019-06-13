/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package java.lang.invoke;

import sun.invoke.util.Wrapper;

import java.nio.ByteOrder;
import java.util.Arrays;

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

    /**
     * Creates a memory access {@link VarHandle} with the given carrier type.
     *
     * The resulting {@link VarHandle} has a single {@link java.foreign.MemoryAddress} as a parameter
     * and returns the carrier type.
     *
     * The alignment constraint for the resulting {@link VarHandle} is the same as the in memory size of the
     * carrier type. The used byte order is the machine native byte order. The access offset is zero,
     * and there are no access coordinates.
     *
     * @param carrier the carrier type. Valid carriers are {@code byte}, {@code short}, {@code char}, {@code int},
     * {@code float}, {@code long}, and {@code double}.
     * @return the new {@link VarHandle}
     * @throws IllegalArgumentException when an illegal carrier type is used
     */
    public static VarHandle dereferenceVarHandle(Class<?> carrier) throws IllegalArgumentException {
        if (!carrier.isPrimitive() || carrier == void.class || carrier == boolean.class) {
            throw new IllegalArgumentException("Illegal carrier: " + carrier.getSimpleName());
        }

        return VarHandles.makeMemoryAddressViewHandle(carrier, Wrapper.forPrimitiveType(carrier).bitWidth() / 8,
                ByteOrder.nativeOrder(), 0, new long[]{});
    }

    private static VarHandleMemoryAddressBase checkMemAccessHandle(VarHandle handle) {
        if (!(handle instanceof VarHandleMemoryAddressBase)) {
            throw new IllegalArgumentException("Not a memory access varhandle: " + handle);
        }

        return (VarHandleMemoryAddressBase) handle;
    }

    /**
     * Creates a memory access {@link VarHandle} with a fixed offset added to the access offset.
     *
     * @param handle the base handle to adapt
     * @param offset the offset, in bytes. Must be positive or zero.
     * @return the new {@link VarHandle}
     * @throws IllegalArgumentException when a {@link VarHandle} is passed that is not a memory access {@link VarHandle}
     * or when a negative offset is passed.
     */
    public static VarHandle offsetHandle(VarHandle handle, long offset) throws IllegalArgumentException {
        VarHandleMemoryAddressBase baseHandle = checkMemAccessHandle(handle);

        if (offset < 0) {
            throw new IllegalArgumentException("Illegal offset: " + offset);
        }

        return VarHandles.makeMemoryAddressViewHandle(
                baseHandle.carrier(),
                baseHandle.alignment + 1, // saved alignment is pre-computed
                baseHandle.be ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN,
                baseHandle.offset + offset,
                baseHandle.strides());
    }

    /**
     * Creates a memory access {@link VarHandle} with a variable offset added to the access offset.
     *
     * The variable offset is given as an additional {@code long} coordinate argument when calling an
     * accessor method on the resulting {@link VarHandle}. This coordinate is multiplied with the given
     * fixed scale factor to compute the effective offset.
     *
     * @param handle the base handle to adapt
     * @param scale the scale factor by which to multiply the coordinate value. Must be greater than zero.
     * @return the new {@link VarHandle}
     * @throws IllegalArgumentException when a {@link VarHandle} is passed that is not a memory access {@link VarHandle}
     * or when a negative scale or zero is passed.
     */
    public static VarHandle elementHandle(VarHandle handle, long scale) throws IllegalArgumentException {
        VarHandleMemoryAddressBase baseHandle = checkMemAccessHandle(handle);

        if (scale <= 0) {
            throw new IllegalArgumentException("Scale factor must be positive: " + scale);
        }

        long[] strides = baseHandle.strides();
        long[] newStrides = Arrays.copyOf(strides, strides.length + 1);
        newStrides[strides.length] = scale;

        return VarHandles.makeMemoryAddressViewHandle(
                baseHandle.carrier(),
                baseHandle.alignment + 1, // saved alignment is pre-computed
                baseHandle.be ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN,
                baseHandle.offset,
                newStrides);
    }

    /**
     * Creates a memory access {@link VarHandle} which enforces the given alignment upon access.
     *
     * @param handle the base handle to adapt
     * @param align the required alignment, in bytes. Must be a power of 2.
     * @return the new {@link VarHandle}
     * @throws IllegalArgumentException when a {@link VarHandle} is passed that is not a memory access {@link VarHandle}
     * or when an invalid alignment is passed.
     */
    public static VarHandle alignAccess(VarHandle handle, long align) throws IllegalArgumentException {
        VarHandleMemoryAddressBase baseHandle = checkMemAccessHandle(handle);

        if (align <= 0
                || (align & (align - 1)) != 0) { // is power of 2?
            throw new IllegalArgumentException("Bad alignment: " + align);
        }

        return VarHandles.makeMemoryAddressViewHandle(
                baseHandle.carrier(),
                align,
                baseHandle.be ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN,
                baseHandle.offset,
                baseHandle.strides());
    }

    /**
     * Creates a memory access {@link VarHandle} which uses the given byte order for its accesses.
     *
     * @param handle the base handle to adapt
     * @param order the desired byte order.
     * @return the new {@link VarHandle}
     * @throws IllegalArgumentException when a {@link VarHandle} is passed that is not a memory access {@link VarHandle}
     */
    public static VarHandle byteOrder(VarHandle handle, ByteOrder order) throws IllegalArgumentException {
        VarHandleMemoryAddressBase baseHandle = checkMemAccessHandle(handle);

        return VarHandles.makeMemoryAddressViewHandle(
                baseHandle.carrier(),
                baseHandle.alignment + 1, // saved alignment is pre-computed
                order,
                baseHandle.offset,
                baseHandle.strides());
    }

}
