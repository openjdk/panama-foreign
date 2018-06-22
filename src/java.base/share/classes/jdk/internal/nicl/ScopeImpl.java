/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.nicl;

import jdk.internal.misc.Unsafe;
import jdk.internal.nicl.types.BoundedArray;
import jdk.internal.nicl.types.BoundedMemoryRegion;
import jdk.internal.nicl.types.BoundedPointer;

import java.nicl.Scope;
import java.nicl.layout.Layout;
import java.nicl.types.Array;
import java.nicl.types.LayoutType;
import java.nicl.types.Pointer;
import java.nicl.types.Struct;
import java.util.ArrayList;

public abstract class ScopeImpl implements Scope {

    private boolean isAlive = true;

    @Override
    public void checkAlive() {
        if (!isAlive) {
            throw new IllegalStateException("Scope is not alive");
        }
    }

    abstract BoundedMemoryRegion allocateRegion(long size);

    private <T> BoundedPointer<T> allocateInternal(LayoutType<T> type, long count, int align) {
        // FIXME: when allocating structs align size up to 8 bytes to allow for raw reads/writes?
        long size = Util.alignUp(type.bytesSize(), align);
        if (size < 0) {
            throw new UnsupportedOperationException("Unknown size for type " + type);
        }

        size *= count;
        if (size > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("allocate size to large");
        }

        return new BoundedPointer<>(type, allocateRegion(size));
    }

    @Override
    public <X> Array<X> allocateArray(LayoutType<X> elementType, long size) {
        return new BoundedArray<>(allocateInternal(elementType, size, 1), size);
    }

    @Override
    public <T> Pointer<T> allocate(LayoutType<T> type) {
        return allocate(type, 1);
    }

    public <T> Pointer<T> allocate(LayoutType<T> type, int align) {
        return allocateInternal(type, 1, align);
    }

    @Override
    public <T extends Struct<T>> T allocateStruct(Class<T> clazz) {
        LayoutType<T> type = LayoutType.ofStruct(clazz);
        long size = Util.alignUp(type.bytesSize(), 8);
        BoundedPointer<T> p = new BoundedPointer<>(type, allocateRegion(size), 0);
        return p.get();
    }

    @Override
    public void close() {
        isAlive = false;
    }

    public static class NativeScope extends ScopeImpl {

        // FIXME: Move this, make it dynamic and correct
        private final static long ALLOC_ALIGNMENT = 8;

        private final static Unsafe U = Unsafe.getUnsafe();
        // 64KB block
        private final static long UNIT_SIZE = 64 * 1024;

        // the address of allocated memory
        private long block;
        // the first offset of available memory
        private long free_offset;
        // the free offset when start transaction
        private long transaction_origin;

        //list of used blocks
        private final ArrayList<Long> used_blocks;

        public NativeScope() {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(new RuntimePermission("java.nicl.NativeScope", "create"));
            }
            block = U.allocateMemory(UNIT_SIZE);
            free_offset = 0;
            transaction_origin = -1;
            used_blocks = new ArrayList<>();
        }

        BoundedMemoryRegion allocateRegion(long size) {
            return new BoundedMemoryRegion(allocate(size), size, this);
        }

        private void rollbackAllocation() {
            if (transaction_origin < 0) {
                return;
            }

            free_offset = transaction_origin;
            transaction_origin = -1;
        }

        private long allocate(long size) {
            if (size <= 0) {
                rollbackAllocation();
                throw new IllegalArgumentException();
            }

            long boundary = free_offset + size;

            if (boundary > UNIT_SIZE) {
                try {
                    long newBuf;
                    if (size >= (UNIT_SIZE >> 1)) {
                        // Need more than half block, just allocate for it
                        newBuf = U.allocateMemory(size);
                        used_blocks.add(newBuf);
                    } else {
                        // less than half block left, start a new block
                        // shrink current block
                        newBuf = U.reallocateMemory(block, free_offset);
                        // We want to revisit strategy if shrink is need a copy
                        assert newBuf == block;
                        used_blocks.add(block);
                        // create a new block
                        newBuf = block = U.allocateMemory(UNIT_SIZE);
                        free_offset = Util.alignUp(size, ALLOC_ALIGNMENT);
                    }
                    // new buffer allocated, commit partial transaction for simplification
                    transaction_origin = -1;
                    return newBuf;
                } catch (OutOfMemoryError ome) {
                    rollbackAllocation();
                    throw ome;
                }
            }

            long rv = block + free_offset;
            free_offset += Util.alignUp(size, ALLOC_ALIGNMENT);

            if ((rv % ALLOC_ALIGNMENT) != 0) {
                throw new RuntimeException("Invalid alignment: 0x" + Long.toHexString(rv));
            }

            return rv;
        }

        @Override
        public void close() {
            super.close();
            for (Long addr: used_blocks) {
                U.freeMemory(addr);
            }
            used_blocks.clear();
            U.freeMemory(block);
        }
    }

    public static class HeapScope extends ScopeImpl {

        public HeapScope() {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(new RuntimePermission("java.nicl.HeapScope", "create"));
            }
        }

        BoundedMemoryRegion allocateRegion(long size) {
            long allocSize = Util.alignUp(size, 8);

            long nElems = allocSize / 8;
            if (nElems > Integer.MAX_VALUE) {
                throw new UnsupportedOperationException("allocate size to large");
            }

            long[] arr = new long[(int)nElems];
            return new BoundedMemoryRegion(arr, Unsafe.ARRAY_LONG_BASE_OFFSET, allocSize, BoundedMemoryRegion.MODE_RW, this);
        }
    }
}
