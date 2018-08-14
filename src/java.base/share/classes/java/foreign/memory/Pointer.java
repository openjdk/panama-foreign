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
import jdk.internal.foreign.Util;
import jdk.internal.foreign.memory.BoundedMemoryRegion;
import jdk.internal.foreign.memory.BoundedPointer;
import jdk.internal.misc.SharedSecrets;

/**
 * This interface models a native pointer.
 * @param <X> the carrier type associated with the pointee.
 */
public interface Pointer<X> extends Resource {

    /**
     * Obtains the {@code NULL} pointer.
     * @param <Z> the null pointer type.
     * @return the {@code NULL} pointer.
     */
    @SuppressWarnings("unchecked")
    static <Z extends Pointer<?>> Z nullPointer() {
        return (Z)new BoundedPointer<>(NativeTypes.VOID, BoundedMemoryRegion.NOTHING, 0, 0);
    }

    /**
     * Move the array by a given offset.
     * @param nElements offset (expressed in number of elements).
     * @return a new array array to the new address.
     * @throws IllegalArgumentException if offset if zero
     * @throws IndexOutOfBoundsException if offset exceeds the boundaries of the memory region pointed to by this array.
     */
    Pointer<X> offset(long nElements) throws IllegalArgumentException, IndexOutOfBoundsException;

    /**
     * Obtain a stream of pointers from current array. The n-th array in the stream is obtained by moving
     * the (n-1)-th array off one element. The size of the stream depends on the layout from which this array
     * was obtained. For example, element pointers obtained through {@link Array#elementPointer()} will return a stream
     * whose number of elements is the same as that in the underlying array layout.
     * @return a stream of pointers; the first pointer points at the first element of the underlying array.
     */
    Stream<Pointer<X>> elements();

    /**
     * Retrieves the {@code LayoutType} associated with the memory region pointed to by this pointer.
     * @return the pointer's {@code LayoutType}.
     */
    LayoutType<X> type();

    /**
     * Is this pointer NULL?
     * @return true if pointer is NULL.
     */
    boolean isNull();

    /**
     * Is the memory accessible from the array for the given mode
     *
     * @param mode the mode
     * @return true if accessible, otherwise false
     */
    boolean isAccessibleFor(int mode);

    /**
     * Returns the underlying memory address associated with this array.
     * @return memory address.
     * @throws IllegalAccessException if the memory address cannot be safely obtained.
     */
    long addr() throws IllegalAccessException;

    /**
     * Returns the length, in bytes, of the memory region covered by this
     * pointer. The number of elements covered is the same as
     * {@code bytesSize() / (layout().size() / 8)}
     *
     * @return the length of the memory region covered
     * @see #elementSize
     */
    long bytesSize();

    /**
     * Returns the number of elements covered by this pointer.
     *
     * @return returns the number of elements
     * @see #bytesSize
     */
    default long elementSize() {
        return bytesSize() / (type().layout().bitsSize() / 8);
    }

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
     * Returns a pointer to the memory region covered by the given direct byte
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
     * @param bb the direct byte buffer
     * @return a pointer
     */
    static Pointer<Byte> fromByteBuffer(ByteBuffer bb) {
        // For a direct ByteBuffer base == null and address is absolute
        Object base = Util.getBufferBase(bb);
        long address = Util.getBufferAddress(bb);
        int pos = bb.position();
        int limit = bb.limit();
        BoundedMemoryRegion mr = new BoundedMemoryRegion(base, address + pos, limit - pos, bb.isReadOnly() ? 1 : 3) {
            // Keep a reference to the buffer so it is kept alive while the
            // region is alive
            final Object ref = bb;

            // @@@ For heap ByteBuffer the addr() will throw an exception
            //     need to adapt a pointer and memory region be more cognizant
            //     of the double addressing mode
            //     the direct address for a heap buffer needs to behave
            //     differently see JNI GetPrimitiveArrayCritical for clues on
            //     behaviour.

            // @@@ Same trick can be performed to create a pointer to a
            //     primitive array
        };
        return new BoundedPointer<>(NativeTypes.UINT8, mr);
    }

    static ByteBuffer asDirectByteBuffer(Pointer<?> buf, int bytes) throws IllegalAccessException {
        if (bytes > buf.bytesSize()) {
            throw new IllegalAccessException();
        }
        return SharedSecrets.getJavaNioAccess()
                .newDirectByteBuffer(buf.addr(), bytes, null);
    }

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
