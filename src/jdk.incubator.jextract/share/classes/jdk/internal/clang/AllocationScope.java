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

package jdk.internal.clang;

import jdk.incubator.foreign.MemorySegment;

import java.util.LinkedHashSet;
import java.util.Set;

class AllocationScope implements AutoCloseable {
    private final Set<MemorySegment> segments = new LinkedHashSet<>();

    MemorySegment track(MemorySegment segment) {
        if (!segments.add(segment)) {
            throw new IllegalArgumentException("Attempt to track a memory segment twice");
        }
        return segment;
    }

    void forget(MemorySegment segment) {
        if (!segments.remove(segment)) {
            throw new IllegalArgumentException("Attempt to forget a memory segment not tracked by this scope");
        }
    }

    @Override
    public void close() {
        var currentThread = Thread.currentThread();
        segments.stream()
                .filter(ms -> ms.ownerThread() == currentThread)
                .forEach(MemorySegment::close);
    }
}
