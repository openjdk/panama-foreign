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

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.Scope;
import java.foreign.layout.Function;
import java.foreign.memory.Array;
import java.foreign.memory.Callback;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jdk.internal.foreign.CallbackImplGenerator.SyntheticCallback;
import jdk.internal.foreign.abi.SystemABI;
import jdk.internal.foreign.memory.BoundedArray;
import jdk.internal.foreign.memory.BoundedPointer;
import jdk.internal.foreign.memory.CallbackImpl;
import jdk.internal.foreign.memory.MemoryBoundInfo;
import jdk.internal.misc.Unsafe;

public final class ScopeImpl implements Scope {

    // FIXME: Move this, make it dynamic and correct
    private final static long ALLOC_ALIGNMENT = 8;

    private final static Unsafe U = Unsafe.getUnsafe();
    // 64KB block
    private final static long UNIT_SIZE = 64 * 1024;

    public static ScopeImpl GLOBAL = new ScopeImpl(null, false);
    public static ScopeImpl UNCHECKED = new ScopeImpl(null, false);

    public static Scope makeLibraryScope() {
        return new ScopeImpl(GLOBAL, false);
    }

    public State state = State.ALIVE;
    private Set<Scope> descendants = new HashSet<>();
    private final boolean allowTerminalOps;

    private List<Library.Symbol> stubs = new ArrayList<>();

    // the address of allocated memory
    private long block;
    // the first offset of available memory
    private long free_offset;
    // the free offset when start transaction
    private long transaction_origin;
    // the scope owner
    private final ScopeImpl parent;

    //list of used blocks
    private final ArrayList<Long> used_blocks;

    public enum State {
        ALIVE(true),
        CLOSED(false),
        MERGED(false);

        boolean isActive;

        State(boolean isActive) {
            this.isActive = isActive;
        }
    }

    public ScopeImpl(ScopeImpl parent, boolean allowTerminalOps) {
        this.parent = parent;
        block = U.allocateMemory(UNIT_SIZE);
        free_offset = 0;
        transaction_origin = -1;
        used_blocks = new ArrayList<>();
        this.allowTerminalOps = allowTerminalOps;
    }

    public void checkAlive() {
        if (state == State.CLOSED) {
            throw new IllegalStateException("Scope is not alive");
        } else if (state == State.MERGED) {
            ((ScopeImpl) parent()).checkAlive();
        }
    }

    @Override
    public Scope parent() {
        return parent;
    }

    @Override
    public Scope fork() {
        Scope forkedScope = new ScopeImpl(this, true);
        descendants.add(forkedScope);
        return forkedScope;
    }

    private <T> BoundedPointer<T> allocateInternal(LayoutType<T> type, long count, int align) {
        // FIXME: when allocating structs align size up to 8 bytes to allow for raw reads/writes?
        long size = Util.alignUp(type.bytesSize(), align);
        if (size < 0) {
            throw new UnsupportedOperationException("Unknown size for type " + type);
        }

        size *= count;
        if (size > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException("allocate size to large");
        } else if (size == 0) {
            return BoundedPointer.nullPointer();
        }

        return new BoundedPointer<>(type, this, Pointer.AccessMode.READ_WRITE, allocateRegion(size));
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
        // FIXME: is this alignment needed?
        return allocate(LayoutType.ofStruct(clazz), 8).get();
    }

    MemoryBoundInfo allocateRegion(long size) {
        checkAllocate();
        if (size == 0) {
            return MemoryBoundInfo.NOTHING;
        }

        return MemoryBoundInfo.ofNative(allocate(size), size);
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
    public <T> Callback<T> allocateCallback(T funcIntfInstance) {
        @SuppressWarnings("unchecked")
        Class<T> carrier = (Class<T>)Util.findUniqueCallback(funcIntfInstance.getClass());
        if (carrier == null) {
            throw new IllegalArgumentException("Cannot infer callback type: " + funcIntfInstance.getClass().getName());
        }
        return allocateCallback(carrier, funcIntfInstance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Callback<T> allocateCallback(Class<T> funcIntfClass, T funcIntfInstance) {
        checkAllocate();
        Objects.nonNull(funcIntfClass);
        Objects.nonNull(funcIntfInstance);
        if (!Util.isCallback(funcIntfClass)) {
            throw new IllegalArgumentException("Not a callback class: " + funcIntfClass.getName());
        }
        try {
            Pointer<?> ptr;
            if (funcIntfInstance.getClass().isAnnotationPresent(SyntheticCallback.class)) {
                //stub already allocated, fetch its code pointer
                ptr = Util.getSyntheticCallbackAddress(funcIntfInstance);
                if (this != ptr.scope()) {
                    throw new IllegalArgumentException("Attempting to re-allocate callback from different scope");
                }
            } else {
                Method m = Util.findFunctionalInterfaceMethod(funcIntfClass);
                MethodHandle mh = Util.getCallbackMH(m).bindTo(funcIntfInstance);
                Function function = Util.getResolvedFunction(funcIntfClass, m);
                NativeMethodType nmt = Util.nativeMethodType(function, m);
                Library.Symbol stub = SystemABI.getInstance().upcallStub(mh, nmt);
                ptr = BoundedPointer.createNativeVoidPointer(this, stub.getAddress().addr());
                stubs.add(stub);
            }
            return new CallbackImpl<>(ptr, funcIntfClass);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void checkAllocate() {
        if (!state.isActive) {
            throw new IllegalStateException("Cannot allocate after close() or merge()");
        }
    }

    void checkTerminal() {
        if (!allowTerminalOps) {
            throw new IllegalStateException("Terminal operations close() or merge() not supported by this scope!");
        }
    }

    @Override
    public void merge() {
        checkTerminal();
        //copy callbacks
        parent.stubs.addAll(stubs);
        stubs.clear();
        //copy descendants
        parent.descendants.addAll(descendants);
        descendants.clear();
        //copy used and current blocks
        parent.used_blocks.addAll(used_blocks);
        parent.used_blocks.add(block);
        //change state
        state = State.MERGED;
    }

    @Override
    public void close() {
        checkTerminal();
        // Need to free stub first as the Pointer::addr will check scope is still alive
        stubs = null;
        state = State.CLOSED;
        for (Long addr: used_blocks) {
            U.freeMemory(addr);
        }
        used_blocks.clear();
        U.freeMemory(block);
        //close descendants
        for (Scope s : descendants) {
            s.close();
        }
        //remove from parent
        parent.descendants.remove(this);
    }

    public static void checkAncestor(Resource r1, Resource r2) {
        if (!isAncestor(r1.scope(), r2.scope())) {
            throw new RuntimeException("Access denied");
        }
    }

    private static boolean isAncestor(Scope s1, Scope s2) {
        if (s1 == ScopeImpl.UNCHECKED ||
                s2 == ScopeImpl.UNCHECKED ||
                s1 == s2) {
            return true;
        } else if (s2 == null) {
            return false;
        } else {
            return isAncestor(s1, s2.parent());
        }
    }
}
