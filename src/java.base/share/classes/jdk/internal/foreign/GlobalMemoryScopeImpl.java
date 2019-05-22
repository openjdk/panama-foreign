/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.foreign;

import java.foreign.MemoryScope;
import java.foreign.MemorySegment;

public class GlobalMemoryScopeImpl extends AbstractMemoryScopeImpl {

    public GlobalMemoryScopeImpl(long charateristics) {
        super(charateristics);
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    void checkThread() {
        //do nothing
    }

    @Override
    public MemorySegment allocateRegion(long bytesSize, long alignmentBytes) throws IllegalArgumentException, RuntimeException, OutOfMemoryError {
        long alignedSize = bytesSize;

        if (alignmentBytes > MAX_ALIGN) {
            alignedSize = bytesSize + (alignmentBytes - 1);
        }

        long buf = U.allocateMemory(alignedSize);
        return MemorySegmentImpl.ofNative(this, alignUp(buf, alignmentBytes), bytesSize);
    }

    @Override
    public MemoryScope parent() {
        return null;
    }

    @Override
    public void close() {
        checkTerminal();
    }

    @Override
    public void merge() {
        checkTerminal();
    }

    public static final GlobalMemoryScopeImpl GLOBAL = new GlobalMemoryScopeImpl(MemoryScope.PINNED);
    public static final GlobalMemoryScopeImpl UNCHECKED = new GlobalMemoryScopeImpl(PINNED | MemoryScope.UNCHECKED);
}
