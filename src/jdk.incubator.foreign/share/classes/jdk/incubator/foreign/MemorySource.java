/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

package jdk.incubator.foreign;

/**
 * A memory source describes an underlying memory region with specific size and kind. Supported memory sources are:
 * on-heap sources, native sources or mapped sources. Memory sources cannot be allocated or released explicitly - instead,
 * clients need to operate on them using a memory segment (see {@link MemorySegment}). In other words, a memory segment
 * can be thought of as a <em>view</em> over a given memory source.
 *
 * <h2><a id = "releasing-sources">Releasing memory sources</a></h2>
 *
 * When <em>all</em> memory segments associated with a given memory source have been closed explicitly
 * (see {@link MemorySegment#close()}), or, alternatively, when all said segments are deemed <em>unreacheable</em> <em>and</em>
 * the memory source has been registered against a cleaner (see {@link MemorySource#registerCleaner()}), the memory source
 * is <em>released</em>; this has different meanings depending on the kind of memory source being considered:
 * <ul>
 *     <li>releasing a native memory source results in <em>freeing</em> the native memory associated with it</li>
 *     <li>releasing a mapped memory source results in the backing memory-mapped file to be unmapped</li>
 *     <li>releasing a heap memory source does not have any side-effect; since heap memory sources might keep
 *     strong references to the original heap-based object, it is the responsibility of clients to ensure that
 *     all segments referring to the released heap source are discarded in a timely manner, so as not to prevent garbage
 *     collection to reclaim the underlying objects.</li>
 * </ul>
 *
 * @apiNote In the future, if the Java language permits, {@link MemorySource}
 * may become a {@code sealed} interface, which would prohibit subclassing except by
 * explicitly permitted types.
 */
public interface MemorySource {
    /**
     * Has this memory source been released?
     * @return {@code true}, if this memory source been released.
     */
    boolean isReleased();

    /**
     * Registers this memory source against a {@link java.lang.ref.Cleaner}; this means that when all memory segments
     * backed by this memory sources become unreacheable, this memory source will be automatically released.
     */
    void registerCleaner();

    /**
     * Obtains the size (in bytes) of this memory source.
     * @return the size (in bytes) of this memory source.
     */
    long byteSize();

    /**
     * Is this a memory source backed by off-heap memory?
     * @return true, if this is either a native or mapped memory source.
     */
    boolean isNative();

    /**
     * Return the raw native address from a {@link MemoryAddress} instance associated with this memory source.
     * @param address the {@link MemoryAddress} instance whose raw native address is to be retrieved.
     * @return the raw native address associated with {@code address}.
     * @throws UnsupportedOperationException if {@link #isNative()} returns {@code false}.
     * @throws IllegalArgumentException if {@code address} is not associated with this memory source.
     */
    long address(MemoryAddress address);

    /**
     * Return the base object associated with this heap memory source.
     * @return the base object associated with this heap memory source.
     * @throws UnsupportedOperationException if {@link #isNative()} returns {@code true}.
     */
    Object base();
}
