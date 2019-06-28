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

package java.foreign;

import jdk.internal.foreign.MemoryAddressImpl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A memory address encodes an offset within a given {@link MemorySegment}. Memory addresses are typically obtained
 * using the {@link MemorySegment#baseAddress()} method; such addresses can then be adjusted as required,
 * using {@link MemoryAddress#offset(long)}.
 * <p>
 * A memory address is typically used as the first argument in a memory access var handle call, to perform some operation
 * on the underlying memory backing a given memory segment. Since a memory address is always associated with a memory segment,
 * such access operations are always subject to spatial and temporal checks as enforced by the address' owning memory region.
 * <p>
 * To allow for interoperability with existing code, a byte buffer view can be obtained from a memory address
 * (see {@link MemoryAddress#asByteBuffer(int)}). This can be useful, for instance, for those clients that want to keep
 * using the {@link ByteBuffer} API, but need to operate on large memory segments. Byte buffers obtained in such a way support
 * the same spatial and temporal access restrictions associated to the memory address from which they originated.
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code MemoryAddress} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 */
public interface MemoryAddress {
    /**
     * Creates a new memory address with given offset (in bytes) from current one.
     * @param l specified offset (in bytes), relative to this address, which should be used to create the new address.
     * @return a new memory address with given offset from current one.
     */
    MemoryAddress offset(long l);

    /**
     * The memory segment this address belongs to.
     * @return The memory segment this address belongs to.
     */
    MemorySegment segment();

    /**
     * Wraps this address in a {@link ByteBuffer}. Some of the properties of the returned buffer are linked to
     * the properties of this address. For instance, if this address is associated with an <em>immutable</em> segment
     * (see {@link MemorySegment#asReadOnly()}, then the resulting buffer is <em>read-only</em> (see {@link ByteBuffer#isReadOnly()}.
     * Additionally, if this address belongs to a native memory segment, the resulting buffer is <em>direct</em> (see
     * {@link ByteBuffer#isDirect()}).
     * <p>
     * The life-cycle of the returned buffer will be tied to that of this address. That means that if the memory segment
     * associated with this address is closed (see {@link MemorySegment#close()}, accessing the returned
     * buffer will throw an {@link IllegalStateException}.
     * <p>
     * The resulting buffer's byte order is {@link java.nio.ByteOrder#BIG_ENDIAN}; this can be changed using
     * {@link ByteBuffer#order(ByteOrder)}.
     *
     * @param bytes the size of the buffer in bytes.
     * @return the created {@link ByteBuffer}.
     * @throws IllegalArgumentException if bytes is larger than the segment covered by this address.
     * @throws UnsupportedOperationException if this address cannot be mapped onto a {@link ByteBuffer} instance,
     * e.g. because it models an heap-based address that is not based on a {@code byte[]}).
     * @throws IllegalStateException if the scope associated with this address' segment has been closed.
     */
    ByteBuffer asByteBuffer(int bytes) throws IllegalArgumentException, UnsupportedOperationException, IllegalStateException;

    /**
     * Compares the specified object with this address for equality. Returns {@code true} if and only if the specified
     * object is also a address, and it is equal to this address.
     *
     * @param that the object to be compared for equality with this address.
     * @return {@code true} if the specified object is equal to this address.
     */
    @Override
    boolean equals(Object that);

    /**
     * Returns the hash code value for this address.
     * @return the hash code value for this address.
     */
    @Override
    int hashCode();

    /**
     * Perform bulk copy from source address to target address.
     * @param src the source address.
     * @param dst the target address.
     * @param bytes the number of bytes to be copied.
     * @throws IllegalArgumentException if {@code bytes} is &lt; 0, or if it is greater than the size of the segments
     * associated with either {@code src} or {@code dst}.
     * @throws IllegalStateException if either the source address or the target address belong to memory segments
     * which have been already closed.
     */
    static void copy(MemoryAddress src, MemoryAddress dst, long bytes) throws IllegalStateException, IllegalArgumentException {
        MemoryAddressImpl.copy((MemoryAddressImpl)src, (MemoryAddressImpl)dst, bytes);
    }

}
