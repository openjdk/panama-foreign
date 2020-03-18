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
 * A mapped memory source describes an underlying memory region which is memory-mapped from a given path.
 * As such, it provides additional operations to allow for syncing the contents of the mapped memory source with
 * the ones of the underlying file backing the memory mapping (see {@link #force()} and {@link #force(MemorySegment)}).
 *
 * @apiNote In the future, if the Java language permits, {@link MemorySource}
 * may become a {@code sealed} interface, which would prohibit subclassing except by
 * explicitly permitted types.
 */
public interface MappedMemorySource extends MemorySource {
    /**
     * Forces any changes made to this memory source to be written to the
     * storage device containing the path associated with this memory source.
     *
     * <p> If the path associated with this memory source resides on a local storage
     * device then when this method returns it is guaranteed that all changes
     * made to this memory source since it was created, or since this method was last
     * invoked, will have been written to that device.
     *
     * <p> If the path associated to this memory source does not reside on a local device,
     * then no such guarantee is made.
     *
     * <p> If this the map mode associated with this memory source is not ({@link
     * java.nio.channels.FileChannel.MapMode#READ_WRITE}) then invoking this method may have no effect.
     * This method may or may not have an effect for implementation-specific mapping modes.</p>
     *
     * @throws IllegalStateException if the memory region backing this memory source has already been released (see
     * {@link #isReleased()}).
     */
    void force();

    /**
     * Forces any changes made to a segment derived from this memory source to be written to the
     * storage device containing the path associated with this memory source.
     *
     * <p> If the path associated with this memory source resides on a local storage
     * device then when this method returns it is guaranteed that all changes
     * made to this memory source since it was created, or since this method was last
     * invoked, will have been written to that device.
     *
     * <p> If the path associated to this memory source does not reside on a local device,
     * then no such guarantee is made.
     *
     * <p> If this the map modes associated with this memory source is not ({@link
     * java.nio.channels.FileChannel.MapMode#READ_WRITE}) then invoking this method may have no effect.
     * This method may or may not have an effect for implementation-specific mapping modes.</p>
     *
     * @param segment the memory segment whose contents are to be written back to storage.
     *
     * @throws IllegalStateException if the memory region backing this memory source has already been released (see
     * {@link #isReleased()}).
     */
    void force(MemorySegment segment);
}
