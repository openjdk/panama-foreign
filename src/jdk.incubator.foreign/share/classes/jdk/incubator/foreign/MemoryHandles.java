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

import jdk.internal.access.JavaLangInvokeAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.Utils;
import sun.invoke.util.Wrapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * This class defines several factory methods for constructing and combining memory access var handles.
 * To obtain a memory access var handle, clients must start from one of the <em>leaf</em> methods
 * (see {@link MemoryHandles#varHandle(Class, ByteOrder)},
 * {@link MemoryHandles#varHandle(Class, long, ByteOrder)}). This determines the variable type
 * (all primitive types but {@code void} and {@code boolean} are supported), as well as the alignment constraint and the
 * byte order associated to a memory access var handle. The resulting memory access var handle can then be combined in various ways
 * to emulate different addressing modes. The var handles created by this class feature a <em>mandatory</em> coordinate type
 * (of type {@link MemoryAddress}), and zero or more {@code long} coordinate types, which can be used to emulate
 * multi-dimensional array indexing.
 * <p>
 * As an example, consider the memory layout expressed by a {@link SequenceLayout} instance constructed as follows:
 * <blockquote><pre>{@code
SequenceLayout seq = MemoryLayout.ofSequence(5,
    MemoryLayout.ofStruct(
        MemoryLayout.ofPaddingBits(32),
        MemoryLayout.ofValueBits(32, ByteOrder.BIG_ENDIAN).withName("value")
    ));
 * }</pre></blockquote>
 * To access the member layout named {@code value}, we can construct a memory access var handle as follows:
 * <blockquote><pre>{@code
VarHandle handle = MemoryHandles.varHandle(int.class, ByteOrder.BIG_ENDIAN); //(MemoryAddress) -> int
handle = MemoryHandles.withOffset(handle, 4); //(MemoryAddress) -> int
handle = MemoryHandles.withStride(handle, 8); //(MemoryAddress, long) -> int
 * }</pre></blockquote>
 *
 * <h2>Addressing mode</h2>
 *
 * The final memory location accessed by a memory access var handle can be computed as follows:
 *
 * <blockquote><pre>{@code
address = base + offset
 * }</pre></blockquote>
 *
 * where {@code base} denotes the address expressed by the {@link MemoryAddress} access coordinate, and {@code offset}
 * can be expressed in the following form:
 *
 * <blockquote><pre>{@code
offset = c_1 + c_2 + ... + c_m + (x_1 * s_1) + (x_2 * s_2) + ... + (x_n * s_n)
 * }</pre></blockquote>
 *
 * where {@code x_1}, {@code x_2}, ... {@code x_n} are <em>dynamic</em> values provided as optional {@code long}
 * access coordinates, whereas {@code c_1}, {@code c_2}, ... {@code c_m} and {@code s_0}, {@code s_1}, ... {@code s_n} are
 * <em>static</em> constants which are can be acquired through the {@link MemoryHandles#withOffset(VarHandle, long)}
 * and the {@link MemoryHandles#withStride(VarHandle, long)} combinators, respectively.
 *
 * <h2><a id="memaccess-mode"></a>Alignment and access modes</h2>
 *
 * A memory access var handle is associated with an access size {@code S} and an alignment constraint {@code B}
 * (both expressed in bytes). We say that a memory access operation is <em>fully aligned</em> if it occurs
 * at a memory address {@code A} which is compatible with both alignment constraints {@code S} and {@code B}.
 * If access is fully aligned then following access modes are supported and are
 * guaranteed to support atomic access:
 * <ul>
 * <li>read write access modes for all {@code T}, with the exception of
 *     access modes {@code get} and {@code set} for {@code long} and
 *     {@code double} on 32-bit platforms.
 * <li>atomic update access modes for {@code int}, {@code long},
 *     {@code float} or {@code double}.
 *     (Future major platform releases of the JDK may support additional
 *     types for certain currently unsupported access modes.)
 * <li>numeric atomic update access modes for {@code int} and {@code long}.
 *     (Future major platform releases of the JDK may support additional
 *     numeric types for certain currently unsupported access modes.)
 * <li>bitwise atomic update access modes for {@code int} and {@code long}.
 *     (Future major platform releases of the JDK may support additional
 *     numeric types for certain currently unsupported access modes.)
 * </ul>
 *
 * If {@code T} is {@code float} or {@code double} then atomic
 * update access modes compare values using their bitwise representation
 * (see {@link Float#floatToRawIntBits} and
 * {@link Double#doubleToRawLongBits}, respectively).
 * <p>
 * Alternatively, a memory access operation is <em>partially aligned</em> if it occurs at a memory address {@code A}
 * which is only compatible with the alignment constraint {@code B}; in such cases, access for anything other than the
 * {@code get} and {@code set} access modes will result in an {@code IllegalStateException}. If access is partially aligned,
 * atomic access is only guaranteed with respect to the largest power of two that divides the GCD of {@code A} and {@code S}.
 * <p>
 * Finally, in all other cases, we say that a memory access operation is <em>misaligned</em>; in such cases an
 * {@code IllegalStateException} is thrown, irrespective of the access mode being used.
 */
public final class MemoryHandles {

    private final static JavaLangInvokeAccess JLI = SharedSecrets.getJavaLangInvokeAccess();

    private MemoryHandles() {
        //sorry, just the one!
    }

    private static final MethodHandle LONG_TO_ADDRESS;
    private static final MethodHandle ADDRESS_TO_LONG;
    private static final MethodHandle ADD_OFFSET;
    private static final MethodHandle ADD_STRIDE;

    static {
        try {
            LONG_TO_ADDRESS = MethodHandles.lookup().findStatic(MemoryHandles.class, "longToAddress",
                    MethodType.methodType(MemoryAddress.class, long.class));
            ADDRESS_TO_LONG = MethodHandles.lookup().findStatic(MemoryHandles.class, "addressToLong",
                    MethodType.methodType(long.class, MemoryAddress.class));
            ADD_OFFSET = MethodHandles.lookup().findStatic(MemoryHandles.class, "addOffset",
                    MethodType.methodType(MemoryAddress.class, MemoryAddress.class, long.class));

            ADD_STRIDE = MethodHandles.lookup().findStatic(MemoryHandles.class, "addStride",
                    MethodType.methodType(MemoryAddress.class, MemoryAddress.class, long.class, long.class));
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Creates a memory access var handle with the given carrier type and byte order.
     *
     * The resulting memory access var handle features a single {@link MemoryAddress} access coordinate,
     * and its variable type is set by the given carrier type.
     *
     * The alignment constraint for the resulting memory access var handle is the same as the in memory size of the
     * carrier type, and the accessed offset is set at zero.
     *
     * @apiNote the resulting var handle features certain <a href="#memaccess-mode">access mode restrictions</a>,
     * which are common to all memory access var handles.
     *
     * @param carrier the carrier type. Valid carriers are {@code byte}, {@code short}, {@code char}, {@code int},
     * {@code float}, {@code long}, and {@code double}.
     * @param byteOrder the required byte order.
     * @return the new memory access var handle.
     * @throws IllegalArgumentException when an illegal carrier type is used
     */
    public static VarHandle varHandle(Class<?> carrier, ByteOrder byteOrder) {
        checkCarrier(carrier);
        return varHandle(carrier,
                carrierSize(carrier),
                byteOrder);
    }

    /**
     * Creates a memory access var handle with the given carrier type, alignment constraint, and byte order.
     *
     * The resulting memory access var handle features a single {@link MemoryAddress} access coordinate,
     * and its variable type is set by the given carrier type.
     *
     * The accessed offset is zero.
     *
     * @apiNote the resulting var handle features certain <a href="#memaccess-mode">access mode restrictions</a>,
     * which are common to all memory access var handles.
     *
     * @param carrier the carrier type. Valid carriers are {@code byte}, {@code short}, {@code char}, {@code int},
     * {@code float}, {@code long}, and {@code double}.
     * @param alignmentBytes the alignment constraint (in bytes). Must be a power of two.
     * @param byteOrder the required byte order.
     * @return the new memory access var handle.
     * @throws IllegalArgumentException if an illegal carrier type is used, or if {@code alignmentBytes} is not a power of two.
     */
    public static VarHandle varHandle(Class<?> carrier, long alignmentBytes, ByteOrder byteOrder) {
        checkCarrier(carrier);

        if (alignmentBytes <= 0
                || (alignmentBytes & (alignmentBytes - 1)) != 0) { // is power of 2?
            throw new IllegalArgumentException("Bad alignment: " + alignmentBytes);
        }

        return Utils.fixUpVarHandle(JLI.memoryAccessVarHandle(carrier, alignmentBytes - 1, byteOrder, 0, new long[]{}));
    }

    /**
     * Returns a var handle that adds a <em>fixed</em> offset to the incoming {@link MemoryAddress} coordinate
     * and then propagates such value to the target var handle. That is,
     * when the returned var handle receives a memory address coordinate pointing at a memory location at
     * offset <em>O</em>, a memory address coordinate pointing at a memory location at offset <em>O' + O</em>
     * is created, and then passed to the target var handle.
     *
     * The returned var handle will feature the same type and access coordinates as the target var handle.
     *
     * @param target the target memory access handle to access after the offset adjustment.
     * @param bytesOffset the offset, in bytes. Must be positive or zero.
     * @return the adapted var handle.
     * @throws IllegalArgumentException if the first access coordinate type is not of type {@link MemoryAddress}.
     */
    public static VarHandle withOffset(VarHandle target, long bytesOffset) {
        if (bytesOffset == 0) {
            return target; //nothing to do
        }

        checkAddressFirstCoordinate(target);

        if (JLI.isMemoryAccessVarHandle(target) &&
                (bytesOffset & JLI.memoryAddressAlignmentMask(target)) == 0) {
            //flatten
            return Utils.fixUpVarHandle(JLI.memoryAccessVarHandle(
                    JLI.memoryAddressCarrier(target),
                    JLI.memoryAddressAlignmentMask(target),
                    JLI.memoryAddressByteOrder(target),
                    JLI.memoryAddressOffset(target) + bytesOffset,
                    JLI.memoryAddressStrides(target)));
        } else {
            //slow path
            VarHandle res = MethodHandles.collectCoordinates(target, 0, ADD_OFFSET);
            return MethodHandles.insertCoordinates(res, 1, bytesOffset);
        }
    }

    /**
     * Returns a var handle which adds a <em>variable</em> offset to the incoming {@link MemoryAddress}
     * access coordinate value and then propagates such value to the target var handle.
     * That is, when the returned var handle receives a memory address coordinate pointing at a memory location at
     * offset <em>O</em>, a new memory address coordinate pointing at a memory location at offset <em>(S * X) + O</em>
     * is created, and then passed to the target var handle,
     * where <em>S</em> is a constant <em>stride</em>, whereas <em>X</em> is a dynamic value that will be
     * provided as an additional access coordinate (of type {@code long}).
     *
     * The returned var handle will feature the same type as the target var handle; an additional access coordinate
     * of type {@code long} will be added to the access coordinate types of the target var handle at the position
     * immediately following the leading access coordinate of type {@link MemoryAddress}.
     *
     * @param target the target memory access handle to access after the scale adjustment.
     * @param bytesStride the stride, in bytes, by which to multiply the coordinate value. Must be greater than zero.
     * @return the adapted var handle.
     * @throws IllegalArgumentException if the first access coordinate type is not of type {@link MemoryAddress}.
     */
    public static VarHandle withStride(VarHandle target, long bytesStride) {
        if (bytesStride == 0) {
            return MethodHandles.dropCoordinates(target, 1, long.class); // dummy coordinate
        }

        checkAddressFirstCoordinate(target);

        if (JLI.isMemoryAccessVarHandle(target) &&
                (bytesStride & JLI.memoryAddressAlignmentMask(target)) == 0) {
            //flatten
            long[] strides = JLI.memoryAddressStrides(target);
            long[] newStrides = new long[strides.length + 1];
            System.arraycopy(strides, 0, newStrides, 1, strides.length);
            newStrides[0] = bytesStride;

            return Utils.fixUpVarHandle(JLI.memoryAccessVarHandle(
                    JLI.memoryAddressCarrier(target),
                    JLI.memoryAddressAlignmentMask(target),
                    JLI.memoryAddressByteOrder(target),
                    JLI.memoryAddressOffset(target),
                    newStrides));
        } else {
            //slow path
            VarHandle res = MethodHandles.collectCoordinates(target, 0, ADD_STRIDE);
            return MethodHandles.insertCoordinates(res, 2, bytesStride);
        }
    }

    /**
     * Adapt an existing var handle into a new var handle whose carrier type is {@link MemoryAddress}.
     * That is, when calling {@link VarHandle#get(Object...)} on the returned var handle,
     * the read numeric value will be turned into a memory address (as if by calling {@link MemoryAddress#ofLong(long)});
     * similarly, when calling {@link VarHandle#set(Object...)}, the memory address to be set will be converted
     * into a numeric value, and then written into memory. The amount of bytes read (resp. written) from (resp. to)
     * memory depends on the carrier of the original memory access var handle.
     *
     * @param target the memory access var handle to be adapted
     * @return the adapted var handle.
     * @throws IllegalArgumentException if the carrier type of {@code varHandle} is either {@code boolean},
     * {@code float}, or {@code double}, or is not a primitive type.
     */
    public static VarHandle asAddressVarHandle(VarHandle target) {
        Class<?> carrier = target.varType();
        if (!carrier.isPrimitive() || carrier == boolean.class ||
                carrier == float.class || carrier == double.class) {
            throw new IllegalArgumentException("Unsupported carrier type: " + carrier.getName());
        }

        if (carrier != long.class) {
            // slow-path, we need to adapt
            return MethodHandles.filterValue(target,
                    MethodHandles.explicitCastArguments(ADDRESS_TO_LONG, MethodType.methodType(carrier, MemoryAddress.class)),
                    MethodHandles.explicitCastArguments(LONG_TO_ADDRESS, MethodType.methodType(MemoryAddress.class, carrier)));
        } else {
            // fast-path
            return MethodHandles.filterValue(target, ADDRESS_TO_LONG, LONG_TO_ADDRESS);
        }
    }

    private static void checkAddressFirstCoordinate(VarHandle handle) {
        if (handle.coordinateTypes().size() < 1 ||
                handle.coordinateTypes().get(0) != MemoryAddress.class) {
            throw new IllegalArgumentException("Expected var handle with leading coordinate of type MemoryAddress");
        }
    }

    private static void checkCarrier(Class<?> carrier) {
        if (!carrier.isPrimitive() || carrier == void.class || carrier == boolean.class) {
            throw new IllegalArgumentException("Illegal carrier: " + carrier.getSimpleName());
        }
    }

    private static long carrierSize(Class<?> carrier) {
        long bitsAlignment = Math.max(8, Wrapper.forPrimitiveType(carrier).bitWidth());
        return Utils.bitsToBytesOrThrow(bitsAlignment, IllegalStateException::new);
    }

    private static MemoryAddress longToAddress(long value) {
        return MemoryAddress.ofLong(value);
    }

    private static long addressToLong(MemoryAddress value) {
        return value.toRawLongValue();
    }

    private static MemoryAddress addOffset(MemoryAddress address, long offset) {
        return address.addOffset(offset);
    }

    private static MemoryAddress addStride(MemoryAddress address, long index, long stride) {
        return address.addOffset(index * stride);
    }
}
