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

import jdk.internal.foreign.GlobalMemoryScopeImpl;

/**
 * A scope models a unit of resource lifecycle management. It provides primitives for memory allocation, as well
 * as a basic ownership model for allocating resources (e.g. pointers). Each scope has a parent scope (except the
 * global scope, which acts as root of the ownership model).
 * <p>
 *  A scope supports two terminal operation: first, a scope
 * can be closed (see {@link MemoryScope#close()}), which implies that all resources associated with that scope can be reclaimed; secondly,
 * a scope can be merged into the parent scope (see {@link MemoryScope#merge()}). After a terminal operation, a scope will no longer be available
 * for allocation. Some scopes can be pinned, in which case they do not support terminal operations.
 * <p>
 * Scope supports the {@link AutoCloseable} interface which enables non-pinned scopes to be used in conjunction
 * with the try-with-resources construct.
 * <p>
 * Since the global scope is the root of the scope ownership tree, this implies that resources allocated in that scope
 * must outlive resources allocated in any other scopes; for all practical purposes users must therefore assume that
 * resources allocated inside global scopes are never deallocated throughout the lifecycle of the application.
 */
public interface MemoryScope extends AutoCloseable {

    /**
     * Allocate region of memory with given {@code LayoutType}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
allocate(layout.bitsSize() / 8, layout.alignmentInBits() / 8).baseAddress();
     * }</pre></blockquote>
     *
     * @param layout the memory layout to be allocated.
     * @return the newly allocated memory region.
     * @throws IllegalArgumentException if the specified layout has illegal size or alignment constraints.
     * @throws RuntimeException if the specified size is too large for the system runtime.
     * @throws OutOfMemoryError if the allocation is refused by the system runtime.
     */
    default MemoryAddress allocate(Layout layout) throws IllegalArgumentException, RuntimeException, OutOfMemoryError {
        if (layout.bitsSize() % 8 != 0) {
            throw new IllegalArgumentException("Layout bits size must be a multiple of 8");
        } else if (layout.alignmentBits() % 8 != 0) {
            throw new IllegalArgumentException("Layout alignment bits must be a multiple of 8");
        }
        return allocate(layout.bitsSize() / 8, layout.alignmentBits() / 8).baseAddress();
    }

    /**
     * Allocate an unaligned memory segment with given size (expressed in bits).
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
allocate(bitsSize, 1).baseAddress();
     * }</pre></blockquote>
     *
     * @param bytesSize the size (expressed in bytes) of the memory segment to be allocated.
     * @return the newly allocated memory segment.
     * @throws IllegalArgumentException if specified size is &lt; 0.
     * @throws RuntimeException if the specified size is too large for the system runtime.
     * @throws OutOfMemoryError if the allocation is refused by the system runtime.
     */
    default MemorySegment allocate(long bytesSize) throws IllegalArgumentException, RuntimeException, OutOfMemoryError {
        return allocate(bytesSize, 1);
    }

    /**
     * Allocate a memory segment with given size (expressed in bits) and alignment constraints (also expressed in bits).
     * @param bytesSize the size (expressed in bits) of the memory segment to be allocated.
     * @param alignmentBytes the alignment constraints (expressed in bits) of the memory segment to be allocated.
     * @return the newly allocated memory segment.
     * @throws IllegalArgumentException if either specified size or alignment are &lt; 0, or if the alignment constraint
     * is not a power of 2.
     * @throws RuntimeException if the specified size is too large for the system runtime.
     * @throws OutOfMemoryError if the allocation is refused by the system runtime.
     */
    MemorySegment allocate(long bytesSize, long alignmentBytes) throws IllegalArgumentException, RuntimeException, OutOfMemoryError;

    /**
     * The parent of this scope.
     * @return the parent of this scope.
     */
    MemoryScope parent();

    /**
     * Is this scope alive - meaning that resources allocated within this scope have not been deallocated.
     * @return true, if this scope is alive.
     */
    boolean isAlive();

    /**
     * Closes this scope. All associated resources will be freed as a result of this operation.
     * Any existing resources (e.g. pointers) associated with this scope will no longer be accessible.
     *  As this is a terminal operation, this scope will no longer be available for further allocation.
     */
    @Override
    void close();

    /**
     * Copies all resources of this scope to the parent scope. As this is a terminal operation, this scope will no
     * longer be available for further allocation.
     */
    void merge();

    /**
     * Create a scope whose parent is the current scope.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
fork(0L);
     * }</pre></blockquote>
     * @return the new scope.
     */
    default MemoryScope fork() {
        return fork(0L);
    }

    /**
     * Create a scope whose parent is the current scope, with given charateristics.
     * @param charateristics bitmask of the scope properties.
     * @return the new scope.
     * @throws IllegalArgumentException if invalid charateristics values are provided.
     */
    MemoryScope fork(long charateristics) throws IllegalArgumentException;

    /**
     * Retrieves the global scope associated with this VM.
     * @return the global scope.
     * @implSpec the retured scope has the {@link #PINNED} characteristic set; as such, terminal operations are
     * not supported by the retured scope.
     */
    static MemoryScope globalScope() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.foreign.memory.MemoryScope", "globalScope"));
        }
        return GlobalMemoryScopeImpl.GLOBAL;
    }

    /**
     * Returns the set of characteristics associated with this scope. Values can be {@link MemoryScope#IMMUTABLE},
     * {@link MemoryScope#UNALIGNED}, {@link MemoryScope#UNCHECKED}, {@link MemoryScope#CONFINED}. Charateristics
     * are not passed from parent to children scopes during a {@link #fork()} operation.
     * @return a representation of the characteristics.
     */
    long characteristics();

    /**
     * Whether addresses generated by this scope are read-only. Can be set or unset during fork.
     * This allows to create a temporary writeable scope which can then be merged (see {@link MemoryScope#merge()} into
     * an immutable parent scope; conversely, it allows to create immutable children scopes of mutable parent scopes. Under
     * this scenario, an immutable children of a mutable parent cannot be merged into it (doing so will effectively make
     * all pointers of the immutable scope writeable again) - the only terminal operation supported in such a scenario
     * is {@link MemoryScope#close}.
     */
    long IMMUTABLE = 1;

    /**
     * Whether this scope rejects terminal operations (see {@link MemoryScope#close()}, {@link MemoryScope#merge()}.
     * This characteristic is not available for selection during a {@link #fork(long)} operation.
     */
    long PINNED = IMMUTABLE << 1;

    /**
     * Whether accesses at addresses generated by this scope can be unaligned. If unset, cannot be set during fork
     * (see {@link MemoryScope#fork()}.
     */
    long UNALIGNED = PINNED << 1;

    /**
     * Whether this scope ignores liveness checks. Can be useful in performance-sensitive context, where a user
     * might want to trade off safety for maximum performances.
     */
    long UNCHECKED = PINNED << 1;

    /**
     * Whether this scope allows access confined to given thread. Can be useful to restrict access to resources
     * managed by this scope to a single thread, to prevent memory races.
     */
    long CONFINED = UNCHECKED << 1;
}
