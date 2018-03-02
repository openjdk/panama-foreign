/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.nicl;

import jdk.internal.misc.Unsafe;
import jdk.internal.nicl.Util;
import jdk.internal.nicl.types.BoundedMemoryRegion;
import jdk.internal.nicl.types.BoundedPointer;

import java.nicl.Scope;
import java.nicl.types.*;

public class HeapScope implements Scope {
    private boolean isAlive = true;

    public HeapScope() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("java.nicl.HeapScope", "create"));
        }
    }

    @Override
    public void checkAlive() {
        if (!isAlive) {
            throw new IllegalStateException("Scope is not alive");
        }
    }

    @Deprecated
    @Override
    public void startAllocation() {
        // FIXME:
        throw new UnsupportedOperationException("NIY");
    }

    @Deprecated
    @Override
    public void endAllocation() {
        // FIXME:
        throw new UnsupportedOperationException("NIY");
    }

    private BoundedMemoryRegion allocateRegion(long size) {
        long allocSize = Util.alignUp(size, 8);

        long nElems = allocSize / 8;
        if (nElems > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("allocate size to large");
        }

        long[] arr = new long[(int)nElems];
        return new BoundedMemoryRegion(arr, Unsafe.ARRAY_LONG_BASE_OFFSET, allocSize, BoundedMemoryRegion.MODE_RW, this);
    }

    @Override
    public <T> Pointer<T> allocateArray(LayoutType<T> type, long count) {
        // Sanity check for now, can be removed/loosened if needed
        long size = type.getNativeTypeSize();
        if (size < 0) {
            throw new UnsupportedOperationException("Unknown size for type " + type);
        }

        size *= count;
        if (size > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("allocate size to large");
        }

        return new BoundedPointer<>(type, allocateRegion(size));
    }

    public <T> Pointer<T> allocate(LayoutType<T> type) {
        return allocateArray(type, 1);
    }

    @Override
    public <T extends Reference<T>> T allocateStruct(LayoutType<T> type) {
        long size = Util.alignUp(Util.sizeof(type.getCarrierType()), 8);
        BoundedPointer<T> p = new BoundedPointer<>(type, allocateRegion(size), 0);
        return p.deref();
    }

    @Override
    public void free(Resource resource) {
        // FIXME: Implement me
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void handoff(Resource resource) {
        // FIXME: Implement me
        throw new UnsupportedOperationException("NIY");
    }

    @Override
    public void close() {
        isAlive = false;
    }
}
