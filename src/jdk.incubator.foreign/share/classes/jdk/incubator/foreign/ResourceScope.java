/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.foreign.MemoryScope;

import java.lang.ref.Cleaner;

public interface ResourceScope {
    /**
     * Is this segment alive?
     * @return true, if the segment is alive.
     * @see MemorySegment#close()
     */
    boolean isAlive();

    /**
     * The thread owning this segment.
     * @return the thread owning this segment.
     */
    Thread ownerThread();

    /**
     * Closes this memory segment. This is a <em>terminal operation</em>; as a side-effect, if this operation completes
     * without exceptions, this segment will be marked as <em>not alive</em>, and subsequent operations on this segment
     * will fail with {@link IllegalStateException}.
     * <p>
     * Depending on the kind of memory segment being closed, calling this method further triggers deallocation of all the resources
     * associated with the memory segment.
     *
     * @apiNote This operation is not idempotent; that is, closing an already closed segment <em>always</em> results in an
     * exception being thrown. This reflects a deliberate design choice: segment state transitions should be
     * manifest in the client code; a failure in any of these transitions reveals a bug in the underlying application
     * logic. This is especially useful when reasoning about the lifecycle of dependent segment views (see {@link #asSlice(MemoryAddress)},
     * where closing one segment might side-effect multiple segments. In such cases it might in fact not be obvious, looking
     * at the code, as to whether a given segment is alive or not.
     *
     * @throws IllegalStateException if this segment is not <em>alive</em>, or if access occurs from a thread other than the
     * thread owning this segment, or if this segment is shared and the segment is concurrently accessed while this method is
     * called.
     * @throws UnsupportedOperationException if this segment does not support the {@link #CLOSE} access mode.
     */
    void close();

    static ResourceScope ofShared() {
        return MemoryScope.createShared(null, null);
    }
    static ResourceScope ofConfined() {
        return MemoryScope.createConfined(null, null);
    }

    static ResourceScope ofShared(Cleaner cleaner) {
        return MemoryScope.createShared(null, null);
    }
    static ResourceScope ofConfined(Cleaner cleaner) {
        return MemoryScope.createConfined(null, null);
    }
}
