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

/**
 * A memory address encodes an offset within a given {@link MemorySegment}, which defines the set of . As such, access to
 * the memory location represented by a memory address is subject to spatial and temporal checks as enforced by the
 * address' owning memory region.
 */
public interface MemoryAddress {
    /**
     * Creates a new memory address with given offset from current one.
     * @param l specified offset at which new address should be created.
     * @return a new memory address with given offset from current one.
     */
    MemoryAddress offset(long l);

    /**
     * The memory segment owning this address.
     * @return The memory segment owning this address.
     */
    MemorySegment segment();

    /**
     * Wraps the this address in a direct {@link ByteBuffer}
     *
     * @param bytes the size of the buffer in bytes
     * @return the created {@link ByteBuffer}
     * @throws IllegalAccessException if bytes is larger than the segment covered by this address
     */
    ByteBuffer asDirectByteBuffer(int bytes) throws IllegalAccessException;

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
     */
    static void copy(MemoryAddress src, MemoryAddress dst, long bytes) {
        MemoryAddressImpl.copy((MemoryAddressImpl)src, (MemoryAddressImpl)dst, bytes);
    }

}
