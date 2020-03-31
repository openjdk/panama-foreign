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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

package jdk.incubator.foreign;

import jdk.internal.foreign.InternalForeign;
import jdk.internal.foreign.MemorySegmentImpl;

import java.nio.charset.Charset;

/**
 * A class containing various methods relating to native interop.
 */
public interface Foreign {

    /**
     * Get an instance of the {@linkplain Foreign} interface.
     * <p>
     * Access to this method must be explicitly enabled by setting the {@code jdk.incubator.foreign.Foreign} system
     * property. The available values are {@code deny}, {@code warn}, {@code debug}, {@code permit}.
     * <ul>
     * <li>
     * If set to {@code deny} an {@linkplain IllegalAccessError} will be thrown.
     * <li>
     * If set to {@code warn} a warning message will be written to the standard error stream.
     * <li>
     * If set to {@code debug} a debug message and a stack trace will be printed to the standard output stream.
     * <li>
     * If set to {@code permit} no special action will be taken.
     * <li>
     * If set to any other value an {@linkplain IllegalAccessError} will be thrown.
     * </ul>
     * <p>
     * {@code deny} is the default value.
     *
     * @return an instance of {@linkplain Foreign}
     * @throws IllegalAccessError if {@code jdk.incubator.foreign.Foreign} is set to {@code deny}
     */
    static Foreign getInstance() throws IllegalAccessError {
        return InternalForeign.getInstance();
    }

    /**
     * Returns the absolute address represented by the given off-heap memory address as a {@code long}.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM crash or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     *
     * @param address the address to turn into a {@code long}
     * @return the address as a {@code long}
     * @throws IllegalAccessError if the permission jkd.incubator.foreign.restrictedMethods is set to 'deny'
     * @throws IllegalStateException if the given address is not an off-heap address
     */
    long asLong(MemoryAddress address) throws IllegalAccessError;

    /**
     * Returns a new native memory segment with given base address and size. The returned segment has its own temporal
     * bounds, and can therefore be closed; closing such a segment does <em>not</em> result in any resource being
     * deallocated.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM crash or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * <p>
     * This method allows for making an otherwise in-accessible memory region accessible. However, there
     * is no guarantee that this memory is safe to access, or that the given size for the new segment is not too large,
     * potentially resulting in out-of-bounds accesses. The developer is trusted to make the judgement that the use of the
     * returned memory segment is safe.
     *
     * @param base the desired base address
     * @param byteSize the desired size.
     * @return a new native memory segment with given base address and size.
     * @throws IllegalArgumentException if {@code base} does not encapsulate a native memory address.
     * @throws IllegalAccessError if the permission jkd.incubator.foreign.restrictedMethods is set to 'deny'
     */
    MemorySegment ofNativeUnchecked(MemoryAddress base, long byteSize) throws IllegalAccessError;

    /**
     * Returns a non-confined memory segment that has the same spatial and temporal bounds as the provided segment.
     * Since the returned segment can be effectively accessed by multiple threads in an unconstrained fashion,
     * this method should be used with care, as it might lead to JVM crashes - e.g. in the case where a thread {@code A}
     * closes a segment while another thread {@code B} is accessing it.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM crash or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * @param segment the segment from which an unconfined view is to be created.
     * @return a non-confined memory segment that has the same spatial and temporal bounds as the provided segment.
     */
    MemorySegment asUnconfined(MemorySegment segment);

    /**
     * Obtain an instance of the system ABI.
     * @return system ABI.
     */
    SystemABI getSystemABI();

    /**
     * Returns a new native memory segment holding contents of the given Java String
     * @param str the Java String
     * @return a new native memory segment
     */
    MemorySegment toCString(String str);

    /**
     * Returns a new native memory segment holding contents of the given Java String
     * @param str The Java String
     * @param charset The Charset to be used to encode the String
     * @return a new native memory segment
     */
    MemorySegment toCString(String str, Charset charset);

    /**
     * Returns a Java String from the contents of the given C '\0' terminated string
     * @param addr The address of the C string
     * @return a Java String
     */
    String toJavaString(MemoryAddress addr);
}
