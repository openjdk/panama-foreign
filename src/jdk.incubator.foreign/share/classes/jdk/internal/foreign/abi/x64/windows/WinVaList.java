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
package jdk.internal.foreign.abi.x64.windows;

import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.CSupport.VaList;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.abi.SharedUtils;
import jdk.internal.foreign.abi.SharedUtils.SimpleVaArg;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;

import static jdk.incubator.foreign.CSupport.Win64.C_POINTER;
import static jdk.incubator.foreign.MemorySegment.CLOSE;
import static jdk.incubator.foreign.MemorySegment.READ;

// see vadefs.h (VC header)
//
// in short
// -> va_list is just a pointer to a buffer with 64 bit entries.
// -> non-power-of-two-sized, or larger than 64 bit types passed by reference.
// -> other types passed in 64 bit slots by normal function calling convention.
//
// X64 va_arg impl:
//
//    typedef char* va_list;
//
//    #define __crt_va_arg(ap, t)                                               \
//        ((sizeof(t) > sizeof(__int64) || (sizeof(t) & (sizeof(t) - 1)) != 0) \
//            ? **(t**)((ap += sizeof(__int64)) - sizeof(__int64))             \
//            :  *(t* )((ap += sizeof(__int64)) - sizeof(__int64)))
//
class WinVaList implements VaList {
    public static final Class<?> CARRIER = MemoryAddress.class;
    private static final long VA_SLOT_SIZE_BYTES = 8;
    private static final VarHandle VH_address = MemoryHandles.asAddressVarHandle(C_POINTER.varHandle(long.class));

    private static final VaList EMPTY = new SharedUtils.EmptyVaList(MemoryAddress.NULL);

    private final MemorySegment segment;
    private MemoryAddress ptr;
    private final List<MemorySegment> copies;

    WinVaList(MemorySegment segment) {
        this(segment, new ArrayList<>());
    }

    WinVaList(MemorySegment segment, List<MemorySegment> copies) {
        this.segment = segment;
        this.ptr = segment.baseAddress();
        this.copies = copies;
    }

    public static final VaList empty() {
        return EMPTY;
    }

    @Override
    public int vargAsInt(MemoryLayout layout) {
        return (int) read(int.class, layout);
    }

    @Override
    public long vargAsLong(MemoryLayout layout) {
        return (long) read(long.class, layout);
    }

    @Override
    public double vargAsDouble(MemoryLayout layout) {
        return (double) read(double.class, layout);
    }

    @Override
    public MemoryAddress vargAsAddress(MemoryLayout layout) {
        return (MemoryAddress) read(MemoryAddress.class, layout);
    }

    @Override
    public MemorySegment vargAsSegment(MemoryLayout layout) {
        return (MemorySegment) read(MemorySegment.class, layout);
    }

    private Object read(Class<?> carrier, MemoryLayout layout) {
        SharedUtils.checkCompatibleType(carrier, layout, Windowsx64Linker.ADDRESS_SIZE);
        Object res;
        if (carrier == MemorySegment.class) {
            TypeClass typeClass = TypeClass.typeClassFor(layout);
            res = switch (typeClass) {
                case STRUCT_REFERENCE -> {
                    MemoryAddress structAddr = (MemoryAddress) VH_address.get(ptr);
                    try (MemorySegment struct = MemorySegment.ofNativeRestricted(structAddr, layout.byteSize(),
                                                                            segment.ownerThread(), null, null)) {
                        MemorySegment seg = MemorySegment.allocateNative(layout.byteSize());
                        seg.copyFrom(struct);
                        yield seg;
                    }
                }
                case STRUCT_REGISTER -> {
                    MemorySegment struct = MemorySegment.allocateNative(layout);
                    struct.copyFrom(segment.asSlice(ptr.segmentOffset(), layout.byteSize()));
                    yield struct;
                }
                default -> throw new IllegalStateException("Unexpected TypeClass: " + typeClass);
            };
        } else {
            VarHandle reader = SharedUtils.vhPrimitiveOrAddress(carrier, layout);
            res = reader.get(ptr);
        }
        ptr = ptr.addOffset(VA_SLOT_SIZE_BYTES);
        return res;
    }

    @Override
    public void skip(MemoryLayout... layouts) {
        ptr = ptr.addOffset(layouts.length * VA_SLOT_SIZE_BYTES);
    }

    static WinVaList ofAddress(MemoryAddress addr) {
        return new WinVaList(MemorySegment.ofNativeRestricted(addr, Long.MAX_VALUE, Thread.currentThread(), null, null));
    }

    static Builder builder() {
        return new Builder();
    }

    @Override
    public void close() {
        segment.close();
        copies.forEach(MemorySegment::close);
    }

    @Override
    public VaList copy() {
        return WinVaList.ofAddress(ptr);
    }

    @Override
    public MemoryAddress address() {
        return ptr;
    }

    @Override
    public boolean isAlive() {
        return segment.isAlive();
    }

    static class Builder implements VaList.Builder {

        private final List<SimpleVaArg> args = new ArrayList<>();

        private Builder arg(Class<?> carrier, MemoryLayout layout, Object value) {
            SharedUtils.checkCompatibleType(carrier, layout, Windowsx64Linker.ADDRESS_SIZE);
            args.add(new SimpleVaArg(carrier, layout, value));
            return this;
        }

        @Override
        public Builder vargFromInt(MemoryLayout layout, int value) {
            return arg(int.class, layout, value);
        }

        @Override
        public Builder vargFromLong(MemoryLayout layout, long value) {
            return arg(long.class, layout, value);
        }

        @Override
        public Builder vargFromDouble(MemoryLayout layout, double value) {
            return arg(double.class, layout, value);
        }

        @Override
        public Builder vargFromAddress(MemoryLayout layout, MemoryAddress value) {
            return arg(MemoryAddress.class, layout, value);
        }

        @Override
        public Builder vargFromSegment(MemoryLayout layout, MemorySegment value) {
            return arg(MemorySegment.class, layout, value);
        }

        public VaList build() {
            if (args.isEmpty()) {
                return EMPTY;
            }
            MemorySegment ms = MemorySegment.allocateNative(VA_SLOT_SIZE_BYTES * args.size());
            List<MemorySegment> copies = new ArrayList<>();

            MemoryAddress addr = ms.baseAddress();
            for (SimpleVaArg arg : args) {
                if (arg.carrier == MemorySegment.class) {
                    MemorySegment msArg = ((MemorySegment) arg.value);
                    TypeClass typeClass = TypeClass.typeClassFor(arg.layout);
                    switch (typeClass) {
                        case STRUCT_REFERENCE -> {
                            MemorySegment copy = MemorySegment.allocateNative(arg.layout);
                            copy.copyFrom(msArg); // by-value
                            copies.add(copy);
                            VH_address.set(addr, copy.baseAddress());
                        }
                        case STRUCT_REGISTER -> {
                            MemorySegment slice = ms.asSlice(addr.segmentOffset(), VA_SLOT_SIZE_BYTES);
                            slice.copyFrom(msArg);
                        }
                        default -> throw new IllegalStateException("Unexpected TypeClass: " + typeClass);
                    }
                } else {
                    VarHandle writer = arg.varHandle();
                    writer.set(addr, arg.value);
                }
                addr = addr.addOffset(VA_SLOT_SIZE_BYTES);
            }

            return new WinVaList(ms.withAccessModes(CLOSE | READ), copies);
        }
    }
}
