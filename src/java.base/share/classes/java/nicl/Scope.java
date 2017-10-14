/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
package java.nicl;

import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.nicl.types.Reference;
import java.nicl.types.Resource;

public interface Scope extends AutoCloseable {
    void checkAlive();

    @Deprecated
    void startAllocation();

    @Deprecated
    void endAllocation();

    <T> Pointer<T> allocate(LayoutType<T> type);

    /**
     * Allocate storage for a number of element of specified type.
     * @param type The LayoutType of the element
     * @param count The numbder of elements to be allocated
     * @return A pointer to the allocated storage points to the first element.
     */
    <T> Pointer<T> allocateArray(LayoutType<T> type, long count);

    @Deprecated
    default <T> Pointer<T> allocate(LayoutType<T> type, long count) {
        return allocateArray(type, count);
    }

    // FIXME: When .ptr().deref works as expected for structs (returns
    // an actual struct instance instead of a ReferenceImpl instance)
    // this can be removed
    <T extends Reference<T>> T allocateStruct(LayoutType<T> type);

    void free(Resource ptr);

    void handoff(Resource ptr);

    @Override
    void close();
}
