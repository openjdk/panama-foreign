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

package jdk.internal.foreign;

import java.foreign.Layout;
import java.foreign.MemoryAddress;
import java.foreign.MemoryScope;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jdk.internal.misc.Unsafe;

public final class MemoryScopeImpl implements MemoryScope {

    // FIXME: Move this, make it dynamic and correct
    private final static long ALLOC_ALIGNMENT = 8;

    private final static Unsafe U = Unsafe.getUnsafe();
    // 64KB block
    private final static long UNIT_SIZE = 64 * 1024;

    // The maximum alignment supported by malloc - typically 16 on 64-bit platforms.
    private final static long MAX_ALIGN = 16;

    public State state = ALIVE;
    private Set<MemoryScope> descendants = new HashSet<>();

    // the address of allocated memory
    private long block;
    // the first offset of available memory
    private long free_offset;
    // the free offset when start transaction
    private long transaction_origin;
    // the scope owner
    private final MemoryScopeImpl parent;
    // the scope charateristics
    private final long charateristics;
    // the scope thread (if confined)
    private final Thread thread;

    //list of used blocks
    private final ArrayList<Long> used_blocks;

    static class State {
        boolean isActive() { return false; }
    }

    final static State ALIVE = new State() {
        boolean isActive() { return true; }
    };

    final static State CLOSED = new State();
    final static State MERGED = new State();

    public static final MemoryScopeImpl GLOBAL = new MemoryScopeImpl(null, PINNED);
    public static final MemoryScopeImpl UNCHECKED = new MemoryScopeImpl(null, PINNED | MemoryScope.UNCHECKED);
    
    public MemoryScopeImpl(MemoryScopeImpl parent, long charateristics) {
        this.parent = parent;
        block = U.allocateMemory(UNIT_SIZE);
        free_offset = 0;
        transaction_origin = -1;
        used_blocks = new ArrayList<>();
        this.charateristics = charateristics;
        this.thread = (charateristics & CONFINED) != 0 ?
            Thread.currentThread() : null;
    }

    public void checkAlive() {
        if (thread != null && thread != Thread.currentThread()) {
            throw new IllegalStateException("Attempt to access scope outside confinement thread!");
        } else if ((charateristics & MemoryScope.UNCHECKED) != 0 || state == ALIVE) {
            return;
        } else if (state == CLOSED) {
            throw new IllegalStateException("Scope is not alive");
        } else if (state == MERGED) {
            ((MemoryScopeImpl) parent()).checkAlive();
        }
    }

    @Override
    public Optional<Thread> confinementThread() {
        return Optional.ofNullable(thread);
    }

    @Override
    public MemoryScope parent() {
        return parent;
    }

    @Override
    public MemoryScope fork() {
        MemoryScope forkedScope = new MemoryScopeImpl(this, 0L);
        descendants.add(forkedScope);
        return forkedScope;
    }

    @Override
    public MemoryScope fork(long charateristics) {
        return new MemoryScopeImpl(this, charateristics);
    }

    @Override
    public long characteristics() {
        return 0;
    }

    @Override
    public MemoryAddress allocate(Layout layout) {
        return allocate(layout, layout.alignmentBits() / 8);
    }

    public MemoryAddress allocate(Layout layout, long align) {
        // FIXME: when allocating structs align size up to 8 bytes to allow for raw reads/writes?
        long size = layout.bitsSize() / 8;
        if (size < 0) {
            throw new UnsupportedOperationException("Unknown size for layout: " + layout);
        }

        if (size > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("allocate size to large");
        }

        return new MemoryAddressImpl(this, allocateRegion(size, align));
    }

    MemoryBoundInfo allocateRegion(long size, long align) {
        checkAllocate();
        if (size == 0) {
            return MemoryBoundInfo.NOTHING;
        }

        return MemoryBoundInfo.ofNative(allocate(size, align), size);
    }

    private void rollbackAllocation() {
        if (transaction_origin < 0) {
            return;
        }

        free_offset = transaction_origin;
        transaction_origin = -1;
    }

    private long allocate(long size, long align) {
        if (align > MAX_ALIGN) {
            size += align - 1;
        }

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
                    free_offset = alignUp(size, ALLOC_ALIGNMENT);
                }
                // new buffer allocated, commit partial transaction for simplification
                transaction_origin = -1;
                return alignUp(newBuf, align);
            } catch (OutOfMemoryError ome) {
                rollbackAllocation();
                throw ome;
            }
        }

        long rv = block + free_offset;
        free_offset += alignUp(size, ALLOC_ALIGNMENT);

        if ((rv % ALLOC_ALIGNMENT) != 0) {
            throw new RuntimeException("Invalid alignment: 0x" + Long.toHexString(rv));
        }

        return alignUp(rv, align);
    }

    public void checkAllocate() {
        if (!state.isActive()) {
            throw new IllegalStateException("Cannot allocate after close() or merge()");
        }
    }

    void checkTerminal() {
        if (thread != null && thread != Thread.currentThread()) {
            throw new IllegalStateException("Attempt to access scope outside confinement thread!");
        } else if ((charateristics & PINNED) != 0) {
            throw new IllegalStateException("Terminal operations close() or merge() not supported by this scope!");
        }
    }

    @Override
    public void merge() {
        checkTerminal();
        //copy descendants
        parent.descendants.addAll(descendants);
        descendants.clear();
        //copy used and current blocks
        parent.used_blocks.addAll(used_blocks);
        parent.used_blocks.add(block);
        //change state
        state = MERGED;
    }

    @Override
    public void close() {
        checkTerminal();
        state = CLOSED;
        for (Long addr: used_blocks) {
            U.freeMemory(addr);
        }
        used_blocks.clear();
        U.freeMemory(block);
        //close descendants
        for (MemoryScope s : descendants) {
            s.close();
        }
        //remove from parent
        parent.descendants.remove(this);
    }

    public static void checkAncestor(MemoryAddress a1, MemoryAddress a2) {
        if (!isAncestor(a1.scope(), a2.scope())) {
            throw new RuntimeException("Access denied");
        }
    }

    private static boolean isAncestor(MemoryScope s1, MemoryScope s2) {
        if ((s1.characteristics() & MemoryScope.UNCHECKED) != 0 ||
                (s2.characteristics() & MemoryScope.UNCHECKED) != 0 ||
                s1 == s2) {
            return true;
        } else if (s2 == null) {
            return false;
        } else {
            return isAncestor(s1, s2.parent());
        }
    }

    public static long alignUp(long n, long alignment) {
        return (n + alignment - 1) & ~(alignment - 1);
    }
}
