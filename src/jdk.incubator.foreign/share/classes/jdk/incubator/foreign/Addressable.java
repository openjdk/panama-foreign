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
 * Represents a type which is <em>addressable</em>. An addressable type is one which can be projected down to
 * a {@linkplain #address() memory address}. Examples of addressable types are {@link MemorySegment},
 * {@link MemoryAddress}, {@link VaList} and {@link CLinker.UpcallStub}.
 * <p>
 * An addressable instance is associated with a {@linkplain ResourceScope resource scope}; the resource scope determines the
 * lifecycle of the addressable instance, as well as whether the instance can be used from multiple threads. Some
 * addressable instances might not be associated with a lifecycle, in which case {@link #scope()} returns the
 * {@linkplain ResourceScope#globalScope() global scope}. Attempting to obtain a memory address
 * from an addressable instance whose backing scope has already been {@linkplain ResourceScope#isAlive() closed} always
 * results in an exception.
 * <p>
 * The {@link Addressable} type is used by the {@link CLinker C linker} to model the types of
 * {@link CLinker#downcallHandle(FunctionDescriptor) downcall handle} parameters that must be passed <em>by reference</em>
 * (e.g. memory addresses, va lists and upcall stubs).
 *
 * @implSpec
 * Implementations of this interface are <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
 */
public interface Addressable {

    /**
     * Returns the memory address associated with this addressable.
     * @return The memory address associated with this addressable.
     * @throws IllegalStateException if the scope associated with this addressable has been closed, or if access occurs from
     * a thread other than the thread owning that scope.
     */
    MemoryAddress address();

    /**
     * Obtains the resource scope associated with this addressable.
     * @return the resource scope associated with this addressable.
     */
    ResourceScope scope();
}
