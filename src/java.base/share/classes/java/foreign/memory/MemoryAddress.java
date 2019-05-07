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

package java.foreign.memory;

import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.MemoryBoundInfo;
import jdk.internal.foreign.MemoryScopeImpl;

import java.nio.ByteBuffer;

/**
 * This interface encapsulate a memory address with certain spatial and temporal bounds.
 */
public interface MemoryAddress {
    /**
     * Creates a new memory address with given offset from current one.
     * @param l specified offset at which new address should be created.
     * @return a new memory address with given offset from current one.
     */
    MemoryAddress offset(long l);

    /**
     * Creates a new memory address whose base address is the same as this address
     * and whose new limit is at the offset specified by the given argument.
     * @param newSize The new address limit.
     * @return a new address with updated base/limit addresses.
     */
    MemoryAddress narrow(long newSize);

    /**
     * The scope associated with this address.
     * @return The scope associated with this address.
     */
    MemoryScope scope();

    /**
     * Wraps the this address in a direct {@link ByteBuffer}
     *
     * @param bytes the size of the buffer in bytes
     * @return the created {@link ByteBuffer}
     * @throws IllegalAccessException if bytes is larger than the region covered by this address
     */
    ByteBuffer asDirectByteBuffer(int bytes) throws IllegalAccessException;

    @Override
    boolean equals(Object that);

    @Override
    int hashCode();

    @Override
    String toString();

    /**
     * Perform bulk copy from source address to target address.
     * @param src the source address.
     * @param dst the target address.
     * @param bytes the number of bytes to be copied.
     */
    static void copy(MemoryAddress src, MemoryAddress dst, long bytes) {
        MemoryAddressImpl.copy((MemoryAddressImpl)src, (MemoryAddressImpl)dst, bytes);
    }

    /**
     * Returns a memory address that models the memory region associated with the given byte
     * buffer. The region starts relative to the buffer's position (inclusive)
     * and ends relative to the buffer's limit (exclusive).
     * <p>
     * The address keeps a reference to the buffer to ensure the buffer is kept
     * live for the life-time of the address.
     * <p>
     *
     * @param bb the byte buffer
     * @return the created address
     */
    static MemoryAddress ofByteBuffer(ByteBuffer bb) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.foreign.Pointer.fromByteBuffer"));
        }
        return new MemoryAddressImpl(MemoryScopeImpl.UNCHECKED,
                MemoryBoundInfo.ofByteBuffer(bb));
    }

    /**
     * Return the null address.
     * @return the null address.
     */
    static MemoryAddress ofNull() {
        return MemoryAddressImpl.NULL_ADDR;
    }
}
