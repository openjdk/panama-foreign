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

import java.foreign.MemoryAddress;
import java.foreign.MemorySegment;
import java.foreign.MemoryScope;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class ConfinedMemoryScopeImpl extends AbstractMemoryScopeImpl {

    private Set<MemoryScope> descendants = new HashSet<>();

    // FIXME: Move this, make it dynamic and correct
    private final static long ALLOC_ALIGNMENT = 8;
    // 64KB block
    private final static long UNIT_SIZE = 64 * 1024;

    public State state = ALIVE;

    // the address of allocated memory
    private long block;
    // the first offset of available memory
    private long free_offset;
    // the free offset when start transaction
    private long transaction_origin;
    // the scope owner
    private final AbstractMemoryScopeImpl parent;
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

    public ConfinedMemoryScopeImpl(AbstractMemoryScopeImpl parent, long charateristics) {
        super(charateristics);
        this.parent = parent;
        block = U.allocateMemory(UNIT_SIZE);
        free_offset = 0;
        transaction_origin = -1;
        used_blocks = new ArrayList<>();
        this.thread = Thread.currentThread();
    }

    @Override
    public MemoryScope fork(long characteristics) {
        MemoryScope res = super.fork(characteristics);
        if (parent instanceof ConfinedMemoryScopeImpl) {
            ((ConfinedMemoryScopeImpl) parent).descendants.add(this);
        }
        return res;
    }

    @Override
    public boolean isAlive() {
        if ((characteristics & MemoryScope.UNCHECKED) != 0) {
            return true;
        } else if (state == ALIVE) {
            return true;
        } else if (state == CLOSED) {
            return false;
        } else if (state == MERGED) {
            return parent.isAlive();
        } else {
            throw new IllegalStateException("Invalid scope state");
        }
    }

    @Override
    public MemoryScope parent() {
        return parent;
    }

    private void rollbackAllocation() {
        if (transaction_origin < 0) {
            return;
        }

        free_offset = transaction_origin;
        transaction_origin = -1;
    }

    @Override
    MemorySegment allocateRegion(long size, long align) {
        checkAllocate();
        long prev_size = size;
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
                return MemorySegmentImpl.ofNative(this, alignUp(newBuf, align), prev_size);
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

        return MemorySegmentImpl.ofNative(this, alignUp(rv, align), prev_size);
    }

    public void checkAllocate() {
        if (!state.isActive()) {
            throw new IllegalStateException("Cannot allocate after close() or merge()");
        }
    }

    @Override
    void checkThread() {
        if (thread != Thread.currentThread()) {
            throw new IllegalStateException("Attempt to access scope outside owning thread");
        }
    }

    @Override
    public void merge() {
        checkTerminal();
        //copy descendants
        if (parent instanceof ConfinedMemoryScopeImpl) {
            ((ConfinedMemoryScopeImpl) parent).descendants.addAll(descendants);
            descendants.clear();
        }
        //copy used and current blocks
        if (parent instanceof ConfinedMemoryScopeImpl) {
            ((ConfinedMemoryScopeImpl)parent).used_blocks.addAll(used_blocks);
            ((ConfinedMemoryScopeImpl)parent).used_blocks.add(block);
        }
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
        if (parent instanceof ConfinedMemoryScopeImpl) {
            //remove from parent
            ((ConfinedMemoryScopeImpl) parent).descendants.remove(this);
        }
    }

    public static void checkAncestor(MemoryAddress a1, MemoryAddress a2) {
        if (!isAncestor(a1.segment().scope(), a2.segment().scope())) {
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
}
