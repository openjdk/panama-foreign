/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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
package java.foreign.memory;

import java.foreign.NativeTypes;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.stream.Stream;
import java.util.function.Predicate;
import jdk.internal.foreign.Util;
import jdk.internal.foreign.memory.BoundedMemoryRegion;
import jdk.internal.foreign.memory.BoundedPointer;

/**
 * This interface models a native pointer.
 * @param <X> the carrier type associated with the pointee.
 */
public interface Pointer<X> extends Resource {

    /**
     * Obtains the {@code NULL} pointer.
     * @param <Z> the carrier type of the pointer.
     * @return the {@code NULL} pointer.
     */
    static <Z> Pointer<Z> nullPointer() {
        return BoundedPointer.nullPointer();
    }

    /**
     * Add a given offset to this pointer.
     * @param nElements offset (expressed in number of elements).
     * @return a new pointer with the added offset.
     * @throws IllegalArgumentException if the size of the layout of this pointer is zero.
     * @throws IndexOutOfBoundsException if offset exceeds the boundaries of the memory region of this pointer.
     */
    Pointer<X> offset(long nElements) throws IllegalArgumentException, IndexOutOfBoundsException;

    /**
     * Returns a stream of pointers starting at this pointer and incrementing the pointer by 1 until
     * the {@code hasNext} predicate returns {@code false}. This is effectively the same as:
     * <p>
     *     <code>
     *         Stream.iterate(pointer, hasNext, p -> p.offset(1))
     *     </code>
     * </p>
     * 
     * @param hasNext a predicate which should return {@code true} as long as the stream should continue
     * @return a stream limited by the {@code hasNext} predicate.
     * @throws IllegalArgumentException if the size of the layout of this pointer is zero.
     * @throws IndexOutOfBoundsException if offset exceeds the boundaries of the memory region of this pointer.
     */
    default Stream<Pointer<X>> iterate(Predicate<? super Pointer<X>> hasNext) throws IllegalArgumentException, IndexOutOfBoundsException {
        return Stream.iterate(this, hasNext, p -> p.offset(1));
    }

    /**
     * Returns a stream of pointers starting at this pointer and incrementing the pointer by 1 until
     * the pointer is equal to {@code end}. This is effectively the same as:
     * <p>
     *     <code>
     *         Stream.iterate(pointer, p -> !p.equals(end), p -> p.offset(1))
     *     </code>
     * </p>
     *
     * @param end a pointer which is used as the end-point of the iteration
     * @return a stream from this pointer until {@code end}
     * @throws IllegalArgumentException if the size of the layout of this pointer is zero.
     * @throws IndexOutOfBoundsException if offset exceeds the boundaries of the memory region of this pointer.
     */
    default Stream<Pointer<X>> iterate(Pointer<X> end) throws IllegalArgumentException, IndexOutOfBoundsException {
        return Stream.iterate(this, p -> !p.equals(end), p -> p.offset(1));
    }

    /**
     * Retrieves the {@link LayoutType} associated with this pointer.
     * @return the pointer's {@link LayoutType}.
     */
    LayoutType<X> type();

    /**
     * Checks if this pointer is {@code NULL}.
     * @return {@code true} if pointer is {@code NULL}.
     */
    boolean isNull();

    /**
     * Is the memory this pointer points to accessible for the given mode.
     * 
     * @param mode the access mode
     * @return {@code true} if accessible, otherwise {@code false}
     */
    boolean isAccessibleFor(int mode);

    /**
     * Returns the underlying memory address associated with this pointer.
     * @return the memory address.
     * @throws IllegalAccessException if the memory address is not a native address.
     */
    long addr() throws IllegalAccessException;

    /**
     * Construct an array out of an element pointer, with given size.
     * @param size the size of the resulting array.
     * @return an array.
     */
    Array<X> withSize(long size);

    /**
     * Cast the pointer to given {@code LayoutType}.
     * @param <Y> the target pointer type.
     * @param type the new {@code LayoutType} associated with the pointer.
     * @return a new pointer with desired type info.
     */
    <Y> Pointer<Y> cast(LayoutType<Y> type);

    /**
     * Load the value associated with this pointer.
     * @return the pointer's value.
     */
    @SuppressWarnings("unchecked")
    default X get() {
        try {
            return (X)type().getter().invoke(this);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Stores the value associated with this pointer.
     * @param x the value to be stored.
     */
    default void set(X x) {
        try {
            type().setter().invoke(this, x);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Returns a pointer to the memory region covered by the given byte
     * buffer. The region starts relative to the buffer's position (inclusive)
     * and ends relative to the buffer's limit (exclusive).
     * <p>
     * The pointer keeps a reference to the buffer to ensure the buffer is kept
     * live for the life-time of the pointer.
     * <p>
     * For a direct ByteBuffer the address is accessible via {@link #addr()},
     * where as for a heap ByteBuffer this method throws an
     * {@code IllegalStateException}.
     *
     * @param bb the byte buffer
     * @return the created pointer
     */
    static Pointer<Byte> fromByteBuffer(ByteBuffer bb) {
        return new BoundedPointer<>(NativeTypes.UINT8, BoundedMemoryRegion.ofByteBuffer(bb));
    }

    /**
     * Wraps the this pointer in a direct {@link ByteBuffer}
     * 
     * @param bytes the size of the buffer in bytes
     * @return the created {@link ByteBuffer}
     * @throws IllegalAccessException if bytes is larger than the region covered by this pointer
     */
    ByteBuffer asDirectByteBuffer(int bytes) throws IllegalAccessException;

    static void copy(Pointer<?> src, Pointer<?> dst, long bytes) throws IllegalAccessException {
        Util.copy(src, dst, bytes);
    }

    static String toString(Pointer<Byte> cstr) {
        if (cstr == null || cstr.isNull()) {
            return null;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte b;
        for (int i = 0; (b = cstr.offset(i).get()) != 0; i++) {
            os.write(b);
        }
        return os.toString();
    }
}
