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
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.abi.SharedUtils;
import jdk.internal.foreign.abi.SharedUtils.SimpleVaArg;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;

import static jdk.incubator.foreign.CSupport.Win64.C_DOUBLE;
import static jdk.incubator.foreign.CSupport.Win64.C_INT;
import static jdk.incubator.foreign.CSupport.Win64.C_POINTER;
import static jdk.incubator.foreign.MemorySegment.CLOSE;
import static jdk.incubator.foreign.MemorySegment.READ;
import static jdk.incubator.foreign.MemorySegment.WRITE;

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
class WinVaList implements CSupport.VaList {
    public static final Class<?> CARRIER = MemoryAddress.class;
    private static final long VA_SLOT_SIZE_BYTES = 8;
    private static final VarHandle VH_address = MemoryHandles.asAddressVarHandle(C_POINTER.varHandle(long.class));

    private final MemorySegment segment;
    private final List<MemorySegment> slices;

    WinVaList(MemorySegment segment) {
        this(segment, new ArrayList<>());
    }

    WinVaList(MemorySegment segment, List<MemorySegment> slices) {
        this.segment = segment;
        this.slices = slices;
    }

    static Builder builder() {
        return new Builder();
    }

    MemorySegment getSegment() {
        return segment;
    }

    @Override
    public void close() {
        segment.close();
        slices.forEach(MemorySegment::close);
    }

    @Override
    public Reader reader(int num) {
        return new Reader(num);
    }

    @Override
    public boolean isAlive() {
        return segment.isAlive();
    }

    static class Builder implements CSupport.VaList.Builder {

        private final List<SimpleVaArg> args = new ArrayList<>();

        private Builder arg(Class<?> carrier, MemoryLayout layout, Object value) {
            SharedUtils.checkCompatibleType(carrier, layout, Windowsx64Linker.ADDRESS_SIZE);
            args.add(new SimpleVaArg(carrier, layout, value));
            return this;
        }

        @Override
        public Builder intArg(MemoryLayout layout, int value) {
            return arg(int.class, layout, value);
        }

        @Override
        public Builder longArg(MemoryLayout layout, long value) {
            return arg(long.class, layout, value);
        }

        @Override
        public Builder doubleArg(MemoryLayout layout, double value) {
            return arg(double.class, layout, value);
        }

        @Override
        public Builder memoryAddressArg(MemoryLayout layout, MemoryAddress value) {
            return arg(MemoryAddress.class, layout, value);
        }

        @Override
        public Builder memorySegmentArg(MemoryLayout layout, MemorySegment value) {
            return arg(MemorySegment.class, layout, value);
        }

        public WinVaList build() {
            MemorySegment ms = MemorySegment.allocateNative(VA_SLOT_SIZE_BYTES * args.size());
            List<MemorySegment> slices = new ArrayList<>();

            MemoryAddress addr = ms.baseAddress();
            for (SimpleVaArg arg : args) {
                if (arg.carrier == MemorySegment.class) {
                    MemorySegment msArg = ((MemorySegment) arg.value);
                    TypeClass typeClass = TypeClass.typeClassFor(arg.layout);
                    switch (typeClass) {
                        case STRUCT_REFERENCE -> {
                            MemorySegment copy = MemorySegment.allocateNative(arg.layout);
                            copy.copyFrom(msArg); // by-value
                            slices.add(copy);
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

            return new WinVaList(ms.withAccessModes(CLOSE | READ), slices);
        }
    }

    class Reader implements CSupport.VaList.Reader {
        private MemoryAddress ptr;

        public Reader(int num) {
            ptr = segment.asSlice(0, num * VA_SLOT_SIZE_BYTES).baseAddress();
        }

        @Override
        public int readInt(MemoryLayout layout) {
            return (int) read(int.class, layout);
        }

        @Override
        public long readLong(MemoryLayout layout) {
            return (long) read(long.class, layout);
        }

        @Override
        public double readDouble(MemoryLayout layout) {
            return (double) read(double.class, layout);
        }

        @Override
        public MemoryAddress readPointer(MemoryLayout layout) {
            return (MemoryAddress) read(MemoryAddress.class, layout);
        }

        @Override
        public MemorySegment readStructOrUnion(MemoryLayout layout) {
            return (MemorySegment) read(MemorySegment.class, layout);
        }

        private Object read(Class<?> carrier, MemoryLayout layout) {
            SharedUtils.checkCompatibleType(carrier, layout, Windowsx64Linker.ADDRESS_SIZE);
            Object res;
            if (carrier == MemorySegment.class) {
                TypeClass typeClass = TypeClass.typeClassFor(layout);
                switch (typeClass) {
                    case STRUCT_REFERENCE -> {
                        MemoryAddress structAddr = (MemoryAddress) VH_address.get(ptr);
                        MemorySegment struct = MemorySegment.ofNativeRestricted(structAddr, layout.byteSize(),
                                                                                segment.ownerThread(), null, null);
                        slices.add(struct);
                        res = struct.withAccessModes(WRITE | READ);
                    }
                    case STRUCT_REGISTER -> {
                        MemorySegment struct = MemorySegment.allocateNative(layout);
                        struct.copyFrom(segment.asSlice(ptr.segmentOffset(), layout.byteSize()));
                        slices.add(struct);
                        res = struct.withAccessModes(WRITE | READ);
                    }
                    default -> throw new IllegalStateException("Unexpected TypeClass: " + typeClass);
                }
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
    }
}
