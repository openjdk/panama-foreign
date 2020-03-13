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

import jdk.internal.foreign.Utils;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Models a memory source. Supported memory sources are: on-heap sources, native sources or mapped sources.
 */
public interface MemorySource {
    /**
     * Has the memory region associated with this memory source been released?
     * @return {@code true}, if the memory region associated with this memory source been released.
     */
    boolean isReleased();

    /**
     * Register this memory source against a {@link java.lang.ref.Cleaner}; this means that when all memory segments
     * based on this memory sources will become unreacheable, the memory source will be released.
     */
    void registerCleaner();

    /**
     * Obtains the size (in bytes) of the memory region backing this memory source.
     * @return the size (in bytes) of the memory region backing this memory source.
     */
    long byteSize();

}
