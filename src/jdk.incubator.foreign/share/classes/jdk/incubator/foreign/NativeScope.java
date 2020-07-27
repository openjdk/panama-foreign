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

import jdk.internal.foreign.AbstractMemorySegmentImpl;
import jdk.internal.foreign.AbstractNativeScope;
import jdk.internal.foreign.Utils;
import jdk.internal.misc.Unsafe;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.util.OptionalLong;
import java.util.function.Function;

/**
 * This class provides a scope of given size, within which several allocations can be performed. An native scope is backed
 * by off-heap memory. Native scopes can be either <em>bounded</em> or <em>unbounded</em>, depending on whether the size
 * of the native scope is known statically. If an application knows before-hand how much memory it needs to allocate the values it needs,
 * using a <em>bounded</em> native scope will typically provide better performances than independently allocating the memory
 * for each value (e.g. using {@link MemorySegment#allocateNative(long)}), or using an <em>unbounded</em> native scope.
 * For this reason, using a bounded native scope is recommended in cases where programs might need to emulate native stack allocation.
 * <p>
 * Allocation scopes are thread-confined (see {@link #ownerThread()}; as such, the resulting {@link MemorySegment} instances
 * returned by the native scope will be backed by memory segments confined by the same owner thread as the native scope.
 * <p>
 * To allow for more usability, it is possible for an native scope to reclaim ownership of an existing memory segments
 * (see {@link #register(MemorySegment)}). This might be useful to allow one or more segments which were independently
 * created to share the same life-cycle as a given native scope - which in turns enables client to group all memory
 * allocation and usage under a single <em>try-with-resources block</em>.
 */
public abstract class NativeScope implements AutoCloseable {

    /**
     * If this native scope is bounded, returns the size, in bytes, of this native scope.
     * @return the size, in bytes, of this native scope (if available).
     */
    public abstract OptionalLong byteSize();

    /**
     * The thread owning this native scope.
     * @return the thread owning this native scope.
     */
    public abstract Thread ownerThread();

    /**
     * Returns the number of allocated bytes in this native scope.
     * @return the number of allocated bytes in this native scope.
     */
    public abstract long allocatedBytes();

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given byte value.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a byte value.
     */
    public MemorySegment allocate(MemoryLayout layout, byte value) {
        VarHandle handle = layout.varHandle(byte.class);
        MemorySegment addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given short value.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a short value.
     */
    public MemorySegment allocate(MemoryLayout layout, short value) {
        VarHandle handle = layout.varHandle(short.class);
        MemorySegment addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given int value.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a int value.
     */
    public MemorySegment allocate(MemoryLayout layout, int value) {
        VarHandle handle = layout.varHandle(int.class);
        MemorySegment addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given float value.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a float value.
     */
    public MemorySegment allocate(MemoryLayout layout, float value) {
        VarHandle handle = layout.varHandle(float.class);
        MemorySegment addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given long value.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a long value.
     */
    public MemorySegment allocate(MemoryLayout layout, long value) {
        VarHandle handle = layout.varHandle(long.class);
        MemorySegment addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given double value.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @param value the value to be set on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     * @throws IllegalArgumentException if {@code layout.byteSize()} does not conform to the size of a double value.
     */
    public MemorySegment allocate(MemoryLayout layout, double value) {
        VarHandle handle = layout.varHandle(double.class);
        MemorySegment addr = allocate(layout);
        handle.set(addr, value);
        return addr;
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given byte array.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param elementLayout the element layout of the array to be allocated.
     * @param array the array to be copied on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < (elementLayout.byteSize() * array.length)}.
     * @throws IllegalArgumentException if {@code elementLayout.byteSize()} does not conform to the size of a byte value.
     */
    public MemorySegment allocateArray(ValueLayout elementLayout, byte[] array) {
        return copyArrayWithSwapIfNeeded(array, elementLayout, MemorySegment::ofArray);
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given short array.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param elementLayout the element layout of the array to be allocated.
     * @param array the array to be copied on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < (elementLayout.byteSize() * array.length)}.
     * @throws IllegalArgumentException if {@code elementLayout.byteSize()} does not conform to the size of a short value.
     */
    public MemorySegment allocateArray(ValueLayout elementLayout, short[] array) {
        return copyArrayWithSwapIfNeeded(array, elementLayout, MemorySegment::ofArray);
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given char array.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param elementLayout the element layout of the array to be allocated.
     * @param array the array to be copied on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < (elementLayout.byteSize() * array.length)}.
     * @throws IllegalArgumentException if {@code elementLayout.byteSize()} does not conform to the size of a char value.
     */
    public MemorySegment allocateArray(ValueLayout elementLayout, char[] array) {
        return copyArrayWithSwapIfNeeded(array, elementLayout, MemorySegment::ofArray);
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given int array.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param elementLayout the element layout of the array to be allocated.
     * @param array the array to be copied on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < (elementLayout.byteSize() * array.length)}.
     * @throws IllegalArgumentException if {@code elementLayout.byteSize()} does not conform to the size of a int value.
     */
    public MemorySegment allocateArray(ValueLayout elementLayout, int[] array) {
        return copyArrayWithSwapIfNeeded(array, elementLayout, MemorySegment::ofArray);
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given float array.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param elementLayout the element layout of the array to be allocated.
     * @param array the array to be copied on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < (elementLayout.byteSize() * array.length)}.
     * @throws IllegalArgumentException if {@code elementLayout.byteSize()} does not conform to the size of a float value.
     */
    public MemorySegment allocateArray(ValueLayout elementLayout, float[] array) {
        return copyArrayWithSwapIfNeeded(array, elementLayout, MemorySegment::ofArray);
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given long array.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param elementLayout the element layout of the array to be allocated.
     * @param array the array to be copied on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < (elementLayout.byteSize() * array.length)}.
     * @throws IllegalArgumentException if {@code elementLayout.byteSize()} does not conform to the size of a long value.
     */
    public MemorySegment allocateArray(ValueLayout elementLayout, long[] array) {
        return copyArrayWithSwapIfNeeded(array, elementLayout, MemorySegment::ofArray);
    }

    /**
     * Allocate a block of memory in this native scope with given layout and initialize it with given double array.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover, the returned
     * segment must conform to the layout alignment constraints.
     * @param elementLayout the element layout of the array to be allocated.
     * @param array the array to be copied on the newly allocated memory block.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < (elementLayout.byteSize() * array.length)}.
     * @throws IllegalArgumentException if {@code elementLayout.byteSize()} does not conform to the size of a double value.
     */
    public MemorySegment allocateArray(ValueLayout elementLayout, double[] array) {
        return copyArrayWithSwapIfNeeded(array, elementLayout, MemorySegment::ofArray);
    }

    private <Z> MemorySegment copyArrayWithSwapIfNeeded(Z array, ValueLayout elementLayout,
                                                        Function<Z, MemorySegment> heapSegmentFactory) {
        Utils.checkPrimitiveCarrierCompat(array.getClass().componentType(), elementLayout);
        MemorySegment addr = allocate(MemoryLayout.ofSequence(Array.getLength(array), elementLayout));
        if (elementLayout.byteSize() == 1 || (elementLayout.order() == ByteOrder.nativeOrder())) {
            addr.copyFrom(heapSegmentFactory.apply(array));
        } else {
            ((AbstractMemorySegmentImpl)addr).copyFromSwap(heapSegmentFactory.apply(array), elementLayout.byteSize());
        }
        return addr;
    }

    /**
     * Allocate a block of memory in this native scope with given layout. The segment returned by this method is
     * associated with a segment which cannot be closed. Moreover, the returned segment must conform to the layout alignment constraints.
     * @param layout the layout of the block of memory to be allocated.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < layout.byteSize()}.
     */
    public MemorySegment allocate(MemoryLayout layout) {
        return allocate(layout.byteSize(), layout.byteAlignment());
    }

    /**
     * Allocate a block of memory corresponding to an array with given element layout and size.
     * The segment returned by this method is associated with a segment which cannot be closed.
     * Moreover, the returned segment must conform to the layout alignment constraints. This is equivalent to the
     * following code:
     * <pre>{@code
    allocate(MemoryLayout.ofSequence(size, elementLayout));
     * }</pre>
     * @param elementLayout the array element layout.
     * @param size the array element count.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < (elementLayout.byteSize() * size)}.
     */
    public MemorySegment allocateArray(MemoryLayout elementLayout, long size) {
        return allocate(MemoryLayout.ofSequence(size, elementLayout));
    }

    /**
     * Allocate a block of memory in this native scope with given size. The segment returned by this method is
     * associated with a segment which cannot be closed. Moreover, the returned segment must be aligned to {@code size}.
     * @param bytesSize the size (in bytes) of the block of memory to be allocated.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < bytesSize}.
     */
    public MemorySegment allocate(long bytesSize) {
        return allocate(bytesSize, bytesSize);
    }

    /**
     * Allocate a block of memory in this native scope with given size and alignment constraint.
     * The segment returned by this method is associated with a segment which cannot be closed. Moreover,
     * the returned segment must be aligned to {@code alignment}.
     * @param bytesSize the size (in bytes) of the block of memory to be allocated.
     * @param bytesAlignment the alignment (in bytes) of the block of memory to be allocated.
     * @return a segment for the newly allocated memory block.
     * @throws OutOfMemoryError if there is not enough space left in this native scope, that is, if
     * {@code limit() - size() < bytesSize}.
     */
    public abstract MemorySegment allocate(long bytesSize, long bytesAlignment);

    /**
     * Register a segment on this scope, which will then reclaim ownership of said segment.
     * The input segment must be closeable - that is, it must feature the {@link MemorySegment#CLOSE} access mode.
     * As a side-effect, the input segment will be marked as <em>not alive</em>, and a new segment will be returned.
     * <p>
     * The returned segment will feature only {@link MemorySegment#READ} and
     * {@link MemorySegment#WRITE} access modes (assuming these were available in the original segment). As such
     * the resulting segment cannot be closed directly using {@link MemorySegment#close()} - but it will be closed
     * indirectly when this native scope is closed.
     * @param segment the segment which will be registered on this native scope.
     * @return a new, non closeable memory segment, backed by the same underlying region as {@code segment},
     * but whose life-cycle is tied to that of this native scope.
     * @throws IllegalStateException if {@code segment} is not <em>alive</em> (see {@link MemorySegment#isAlive()}).
     * @throws NullPointerException if {@code segment == null}
     * @throws IllegalArgumentException if {@code segment} is not confined and {@code segment.ownerThread() != this.ownerThread()},
     * or if {@code segment} does not feature the {@link MemorySegment#CLOSE} access mode.
     */
    public abstract MemorySegment register(MemorySegment segment);

    /**
     * Close this native scope; calling this method will render any segment obtained through this native scope
     * unusable and might release any backing memory resources associated with this native scope.
     */
    @Override
    public abstract void close();

    /**
     * Creates a new bounded native scope, backed by off-heap memory.
     * @param size the size of the native scope.
     * @return a new bounded native scope, with given size (in bytes).
     */
    public static NativeScope boundedScope(long size) {
        return new AbstractNativeScope.BoundedNativeScope(size);
    }

    /**
     * Creates a new unbounded native scope, backed by off-heap memory.
     * @return a new unbounded native scope.
     */
    public static NativeScope unboundedScope() {
        return new AbstractNativeScope.UnboundedNativeScope();
    }
}
