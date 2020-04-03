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

package jdk.incubator.foreign;

import jdk.internal.foreign.BoundedAllocationScope;
import jdk.internal.foreign.UnboundedAllocationScope;

import java.lang.invoke.VarHandle;
import java.util.OptionalLong;

/**
 * This class provides a scope of given size, within which several allocations can be performed. An allocation scope is backed
 * by off-heap memory. Allocation scopes can be either <em>bounded</em> or <em>unbounded</em>, depending on whether the size
 * of the allocation scope is known statically. If an application knows before-hand how much memory it needs to allocate the values it needs,
 * using a <em>bounded</em> allocation scope will typically provide better performances than independently allocating the memory
 * for each value (e.g. using {@link MemorySegment#allocateNative(long)}), or using an <em>unbounded</em> allocation scope.
 * For this reason, using a bounded allocation scope is recommended in cases where programs might need to emulate native stack allocation.
 */
public abstract class NativeAllocationScope implements AutoCloseable {

    /**
     * If this allocation scope is bounded, returns the size, in bytes, of this allocation scope.
     * @return the size, in bytes, of this allocation scope (if available).
     */
    public abstract OptionalLong byteSize();

    /**
     * Returns the number of allocated bytes in this allocation scope.
     * @return the number of allocated bytes in this allocation scope.
     */
    public abstract long allocatedBytes();

    /**
     * Allocate a block of memory in this allocation scope with given layout and initialize it with given byte value.
     * The address returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * address must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return an address which points to the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this allocation scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a byte value.
     */
    public MemoryAddress allocate(MemoryLayout layout, byte value) {
        VarHandle handle = layout.varHandle(byte.class);
        MemoryAddress addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this allocation scope with given layout and initialize it with given short value.
     * The address returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * address must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return an address which points to the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this allocation scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a short value.
     */
    public MemoryAddress allocate(MemoryLayout layout, short value) {
        VarHandle handle = layout.varHandle(short.class);
        MemoryAddress addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this allocation scope with given layout and initialize it with given int value.
     * The address returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * address must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return an address which points to the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this allocation scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a int value.
     */
    public MemoryAddress allocate(MemoryLayout layout, int value) {
        VarHandle handle = layout.varHandle(int.class);
        MemoryAddress addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this allocation scope with given layout and initialize it with given float value.
     * The address returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * address must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return an address which points to the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this allocation scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a float value.
     */
    public MemoryAddress allocate(MemoryLayout layout, float value) {
        VarHandle handle = layout.varHandle(float.class);
        MemoryAddress addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this allocation scope with given layout and initialize it with given long value.
     * The address returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * address must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return an address which points to the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this allocation scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a long value.
     */
    public MemoryAddress allocate(MemoryLayout layout, long value) {
        VarHandle handle = layout.varHandle(long.class);
        MemoryAddress addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this allocation scope with given layout and initialize it with given double value.
     * The address returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * address must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return an address which points to the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this allocation scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a double value.
     */
    public MemoryAddress allocate(MemoryLayout layout, double value) {
        VarHandle handle = layout.varHandle(double.class);
        MemoryAddress addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this allocation scope with given layout and initialize it with given address value.
     * The address returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * address must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return an address which points to the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this allocation scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of an address value.
     */
    public MemoryAddress allocate(MemoryLayout layout, MemoryAddress value) {
        VarHandle handle = MemoryHandles.asAddressVarHandle(layout.varHandle(carrierForSize(layout.byteSize())));
        MemoryAddress addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    private static Class<?> carrierForSize(long size) {
        return switch ((int)size) {
            case 1 -> byte.class;
            case 2 -> short.class;
            case 4 -> int.class;
            case 8 -> long.class;
            default -> throw new IllegalArgumentException("Bad layout size: " + size);
        };
    }

    /**
     * Allocate a block of memory in this allocation scope with given layout. The address returned by this method is
     * associated with a segment which cannot be closed. Moreover, the returned address must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @return an address which points to the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this allocation scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     */
    public MemoryAddress allocate(MemoryLayout layout) {
        return allocate(layout.byteSize(), layout.byteAlignment());
    }

    /**
     * Allocate a block of memory in this allocation scope with given size. The address returned by this method is
     * associated with a segment which cannot be closed. Moreover, the returned address must be aligned to {@code size}.
     * @param bytesSize the size (in bytes) of the block of memory to be allocated.
     * @return an address which points to the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this allocation scope, that is, if
     * {@code limit() - size() < bytesSize}.
     */
    public MemoryAddress allocate(long bytesSize) {
        return allocate(bytesSize, bytesSize);
    }

    /**
     * Allocate a block of memory in this allocation scope with given size and alignment constraint.
     * The address returned by this method is associated with a segment which cannot be closed. Moreover,
     * the returned address must be aligned to {@code alignment}.
     * @param bytesSize the size (in bytes) of the block of memory to be allocated.
     * @param bytesAlignment the alignment (in bytes) of the block of memory to be allocated.
     * @return an address which points to the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this allocation scope, that is, if
     * {@code limit() - size() < bytesSize}.
     */
    public abstract MemoryAddress allocate(long bytesSize, long bytesAlignment);

    /**
     * Close this allocation scope; calling this method will render any address obtained through this allocation scope
     * unusable and might release any backing memory resources associated with this allocation scope.
     */
    @Override
    public abstract void close();

    /**
     * Creates a new bounded allocation scope, backed by off-heap memory.
     * @param size the size of the allocation scope.
     * @return a new bounded allocation scope, with given size (in bytes).
     */
    public static NativeAllocationScope boundedScope(long size) {
        return new BoundedAllocationScope(size);
    }

    /**
     * Creates a new unbounded allocation scope, backed by off-heap memory.
     * @return a new unbounded allocation scope.
     */
    public static NativeAllocationScope unboundedScope() {
        return new UnboundedAllocationScope();
    }
}
