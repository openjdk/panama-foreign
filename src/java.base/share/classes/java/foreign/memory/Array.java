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

package java.foreign.memory;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import jdk.internal.foreign.Util;
import jdk.internal.foreign.memory.BoundedArray;
import jdk.internal.foreign.memory.BoundedPointer;

/**
 * This interface models a native array. An array is composed by a base pointer and a size.
 * @param <X> the carrier type associated with the array element.
 */
public interface Array<X> {

    /**
     * Obtain the array size.
     * @return the array size.
     */
    long length();

    /**
     * Obtain the base pointer associated with this array.
     * @return the base pointer.
     */
    Pointer<X> elementPointer();

    /**
     * Retrieves the {@code LayoutType} associated with the element type of this array. This is effectively the same as:
     * <p>
     *     <code>
     *         array.elementPointer().type();
     *     </code>
     * </p>
     *
     * @return the array element type's {@code LayoutType}.
     */
    default LayoutType<X> elementType() {
        return elementPointer().type();
    }

    /**
     * Returns a stream of pointers starting at the start of this array and incrementing the pointer by 1 until
     * the end of this array.  This is effectively the same as:
     * <p>
     *     <code>
     *         array.elementPointer().iterate(elementPointer().offset(length()))
     *     </code>
     * </p>
     * 
     * @return a stream limited by the size of this array.
     * @throws IllegalArgumentException if the size of the element layout of this array is zero.
     * @see Pointer#iterate(Pointer)
     */
    default Stream<Pointer<X>> iterate() throws IllegalArgumentException {
        return elementPointer().iterate(elementPointer().offset(length()));
    }

    /**
     * Returns the length, in bytes, of the memory region covered by this
     * array. This is the same as
     * {@code length() * type().bytesSize()}
     *
     * @return the length of the memory region covered by this array
     */
    default long bytesSize() {
        return length() * elementType().bytesSize();
    }

    /**
     * Cast the array to given {@code LayoutType}.
     * @param <Y> the target array type.
     * @param type the new {@code LayoutType} associated with the array.
     * @return a new array with desired type info.
     */
    default <Y> Array<Y> cast(LayoutType<Y> type) { return cast(type, length()); }

    /**
     * Cast the array to given {@code LayoutType} and size.
     * @param <Y> the target array type.
     * @param type the new {@code LayoutType} associated with the array.
     * @param size the new size associated with the array.
     * @return a new array with desired type and size info.
     */
    <Y> Array<Y> cast(LayoutType<Y> type, long size);

    /**
     * Load the value at given position in this array. This is roughly equivalent to:
     * <p>
     *     <code>
     *         basePointer().offset(index).get();
     *     </code>
     * </p>
     * @param index element position
     * @return the array element value.
     */
    @SuppressWarnings("unchecked")
    default X get(long index) {
        try {
            return (X)elementType().getter().invoke(elementPointer().offset(index));
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Stores the value at given position in this array. This is roughly equivalent to:
     * <p>
     *     <code>
     *         basePointer().offset(index).set(x);
     *     </code>
      </p>
     * @param index element position
     * @param x the value to be stored.
     */
    default void set(long index, X x) {
        try {
            elementType().setter().invoke(elementPointer().offset(index), x);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Collect element values into a Java array.
     * @param arrFactory the factory for the Java array.
     * @param <Z> the Java array type.
     * @return a new instance of a Java array.
     * @throws IllegalArgumentException if the array created by the provided factory is not compatible with the required type.
     * @throws IndexOutOfBoundsException if this native array has a size that exceeds the maximum Java arrays size.
     */
    default <Z> Z toArray(IntFunction<Z> arrFactory) {
        if (length() > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException("Native array is too big");
        }
        int size = (int)length();
        Z arr = arrFactory.apply((int)length());
        BoundedArray.copyTo(this, arr, size);
        return arr;
    }

    /**
     * Copy contents of source array into destination array.
     * @param src source array.
     * @param dst destination array.
     * @param <Z> the array carrier type.
     * @throws IllegalArgumentException if the two arrays have different layouts.
     */
    static <Z> void assign(Array<Z> src, Array<Z> dst) {
        Pointer.copy(src.ptr(), dst.ptr());
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * A subsequence of array components are copied from the source
     * array referenced by {@code src} to the destination array
     * referenced by {@code dest}. The number of components copied is
     * equal to the {@code length} argument. The components at
     * positions {@code srcPos} through
     * {@code srcPos+length-1} in the source array are copied into
     * positions {@code destPos} through
     * {@code destPos+length-1}, respectively, of the destination
     * array.
     *
     * @param      src      the source array.
     * @param      srcPos   starting position in the source array.
     * @param      dest     the destination array.
     * @param      destPos  starting position in the destination data.
     * @param      length   the number of array elements to be copied.
     * @param      <Z>      the carrier type
     * @throws     IndexOutOfBoundsException  if copying would cause
     *             access of data outside array bounds.
     * @throws     NullPointerException if either {@code src} or
     *             {@code dest} is {@code null}.
     */
    static <Z> void copy(Array<Z> src, long srcPos, Array<Z> dest, long destPos, long length) {
        Objects.requireNonNull(src);
        Objects.requireNonNull(dest);

        if (length == 0) {
            return;
        }

        Array.assign(src.slice(srcPos, length), dest.slice(destPos, length));
    }

    /**
     * Return a subsequence of elements as an {@code Array}. The returned array
     * is a coherent view of those selected elements, any change in either is
     * actually changed in the other.
     *
     * @param from The starting position of the subsequence
     * @param count The number of elements in the subsequence
     * @throws IndexOutOfBoundsException if the range cause access outside of the array
     * @return The Array of the selected elements
     */
    default Array<X> slice(long from, long count) {
        if (from < 0 || count < 0 || (from + count) > length()) {
            throw new IndexOutOfBoundsException();
        }
        return elementPointer().offset(from).withSize(count);
    }

    /**
     * Return a {@code Pointer} to the array. This is different to the {@code
     * elementPointer} because the {@code Layout} of the returned pointer is
     * for the whole array instead of a single element.
     *
     * @return The Pointer to the Array
     */
    default Pointer<Array<X>> ptr() {
        return Util.unsafeCast(elementPointer(), elementType().array(length()));
    }

    /**
     * Return an {@code Array} represents the heap-allocated primitive array.
     * The returned {@code Array} or derived {@code Pointer} is good to use in
     * Java code but may cause UnsupportedOperationException when used in
     * argument to native call.
     *
     * @param elementType The layout of the element type
     * @param javaArray The heap-allocated primitive array
     * @param <Z> The carrier type of the element
     * @throws IllegalAccessException The specified array is of primitive type
     *            or the elementType doesn't not match the primitive type
     * @throws SecurityException if access is not permitted based on
     *            the current security policy.
     * @throws UnsupportedOperationException if the Array is used
     * @return The Array represent the heap-allocated primitive array
     */
    static <Z> Array<Z> ofPrimitiveArray(LayoutType<Z> elementType, Object javaArray) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.foreign.memory.fromHeap"));
        }

        Class<?> elemCarrier = javaArray.getClass().getComponentType();
        if (!elemCarrier.isPrimitive()) {
            throw new IllegalArgumentException("Not primitive type");
        }
        if (elementType.carrier() != elemCarrier ||
                elementType.layout().bitsSize() != Util.sizeof(elemCarrier)) {
            throw new IllegalArgumentException("Type does not match");
        }

        return BoundedPointer.fromArray(elementType, javaArray)
                .withSize(java.lang.reflect.Array.getLength(javaArray));
    }
}
