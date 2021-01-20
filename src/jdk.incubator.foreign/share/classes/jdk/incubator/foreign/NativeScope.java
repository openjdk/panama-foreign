/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

package jdk.incubator.foreign;

import jdk.internal.foreign.AbstractNativeScope;

import java.util.OptionalLong;

/**
 * A native scope is an abstraction which provides shared temporal bounds for one or more allocations, backed
 * by off-heap memory. Native scopes can be either <em>bounded</em> or <em>unbounded</em>, depending on whether the size
 * of the native scope is known statically. If an application knows before-hand how much memory it needs to allocate,
 * then using a <em>bounded</em> native scope will typically provide better performance than independently allocating the memory
 * for each value (e.g. using {@link MemorySegment#allocateNative(long)}), or using an <em>unbounded</em> native scope.
 * For this reason, using a bounded native scope is recommended in cases where programs might need to emulate native stack allocation.
 * <p>
 * Allocation scopes are thread-confined (see {@link #ownerThread()}; as such, the resulting {@link MemorySegment} instances
 * returned by the native scope will be backed by memory segments confined by the same owner thread as the native scope's
 * owner thread.
 * <p> Unless otherwise specified, passing a {@code null} argument, or an array argument containing one or more {@code null}
 * elements to a method in this class causes a {@link NullPointerException NullPointerException} to be thrown. </p>
 *
 * @apiNote In the future, if the Java language permits, {@link NativeScope}
 * may become a {@code sealed} interface, which would prohibit subclassing except by
 * explicitly permitted types.
 */
public interface NativeScope extends ResourceScope, NativeAllocator {

    /**
     * If this native scope is bounded, returns the size, in bytes, of this native scope.
     * @return the size, in bytes, of this native scope (if available).
     */
    OptionalLong byteSize();

    /**
     * Returns the number of allocated bytes in this native scope.
     * @return the number of allocated bytes in this native scope.
     */
    long allocatedBytes();

    /**
     * Close this native scope; calling this method will render any segment obtained through this native scope
     * unusable and might release any backing memory resources associated with this native scope.
     */
    @Override
    void close();

    /**
     * Creates a new bounded native scope, backed by off-heap memory.
     * @param size the size of the native scope.
     * @return a new bounded native scope, with given size (in bytes).
     */
    static NativeScope boundedScope(long size) {
        return new AbstractNativeScope.BoundedNativeScope(size);
    }

    /**
     * Creates a new unbounded native scope, backed by off-heap memory.
     * @return a new unbounded native scope.
     */
    static NativeScope unboundedScope() {
        return new AbstractNativeScope.UnboundedNativeScope();
    }
}
