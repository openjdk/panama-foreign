/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi.x64.windows;

import jdk.incubator.foreign.AddressLayout;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.abi.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.List;

/**
 * ABI implementation based on Windows ABI AMD64 supplement v.0.99.6
 */
public class Windowsx64ABI implements SystemABI {

    public static final int MAX_INTEGER_ARGUMENT_REGISTERS = 4;
    public static final int MAX_INTEGER_RETURN_REGISTERS = 1;
    public static final int MAX_VECTOR_ARGUMENT_REGISTERS = 4;
    public static final int MAX_VECTOR_RETURN_REGISTERS = 1;
    public static final int MAX_REGISTER_ARGUMENTS = 4;
    public static final int MAX_REGISTER_RETURNS = 1;

    private static Windowsx64ABI instance;

    public static Windowsx64ABI getInstance() {
        if (instance == null) {
            instance = new Windowsx64ABI();
        }
        return instance;
    }

    static CallingSequence arrangeCall(FunctionDescriptor f) {
        CallingSequenceBuilder builder = new CallingSequenceBuilderImpl(
                f.returnLayout().orElse(null));
        f.argumentLayouts().forEach(builder::addArgument);
        return builder.build();
    }

    @Override
    public MethodHandle downcallHandle(MemoryAddress symbol, MethodType type, FunctionDescriptor function) {
        if (function.isVariadic()) {
            throw new IllegalArgumentException("Variadic function: " + function);
        }

        return new UniversalNativeInvoker(MemoryAddressImpl.addressof(symbol),
                arrangeCall(function), type, function, adapter).getBoundMethodHandle();
    }

    @Override
    public MemoryAddress upcallStub(MethodHandle target, FunctionDescriptor function) {
        if (function.isVariadic()) {
            throw new IllegalArgumentException("Variadic function: " + function);
        }

        return UpcallStubs.upcallAddress(new UniversalUpcallHandler(target, arrangeCall(function), target.type(), function, adapter));
    }

    UniversalAdapter adapter = new UniversalAdapter() {
        @Override
        public void unboxValue(Object o, Class<?> carrier, MemoryLayout layout, java.util.function.Function<ArgumentBinding, MemoryAddress> dstPtrFunc,
                               List<ArgumentBinding> bindings) throws Throwable {
            if (layout instanceof GroupLayout) {
                assert (bindings.size() == 1); // always for structs on windows

                MemoryAddress structPtr = ((MemorySegment)o).baseAddress();
                GroupLayout g = (GroupLayout)layout;
                ArgumentBinding binding = bindings.get(0);

                if (CallingSequenceBuilderImpl.isRegisterAggregate(binding.argument().layout())) { // pass value
                    MemoryAddress.copy(structPtr, dstPtrFunc.apply(binding), g.byteSize());
                } else { // pass a pointer
                    /*
                     * Leak memory for now
                     */
                    MemoryAddress copy = MemorySegment.ofNative(g).baseAddress();
                    MemoryAddress.copy(structPtr, copy, g.byteSize());

                    MemoryAddress dst = dstPtrFunc.apply(binding);
                    VarHandle longHandle = MemoryHandles.varHandle(long.class);
                    longHandle.set(dst, MemoryAddressImpl.addressof(copy));
                }
            } else if (layout instanceof AddressLayout) {
                assert bindings.size() <= 2;
                VarHandle vh = MemoryHandles.varHandle(long.class);
                MemoryAddress dst = dstPtrFunc.apply(bindings.get(0));
                vh.set(dst, MemoryAddressImpl.addressof((MemoryAddress)o));
            } else {
                assert bindings.size() <= 2;
                MemoryAddress dst = dstPtrFunc.apply(bindings.get(0));
                VarHandle primHandle = MemoryHandles.varHandle(carrier);
                primHandle.set(dst, o);
            }
        }

        @Override
        public Object boxValue(Class<?> carrier, MemoryLayout layout, java.util.function.Function<ArgumentBinding, MemoryAddress> srcPtrFunc,
                               List<ArgumentBinding> bindings) {
            assert (bindings.size() == 1); // always on windows
            ArgumentBinding binding = bindings.get(0);
            if (layout instanceof GroupLayout) {

                /*
                 * Leak memory for now
                 */
                MemoryAddress rtmp = MemorySegment.ofNative(layout).baseAddress();

                if (CallingSequenceBuilderImpl.isRegisterAggregate(layout)) {
                    MemoryAddress dst = rtmp.offset(binding.offset());
                    MemoryAddress.copy(srcPtrFunc.apply(binding), dst, layout.byteSize());
                } else {
                    VarHandle longHandle = MemoryHandles.varHandle(long.class);
                    long addr = (long)longHandle.get(srcPtrFunc.apply(binding));
                    MemoryAddress local = MemoryAddressImpl.ofNative(addr);
                    // need defensive copy since Structs don't have value semantics on the Java side.
                    MemoryAddress.copy(local, rtmp, layout.byteSize());
                }

                return rtmp.segment();
            } else if (layout instanceof AddressLayout) {
                assert bindings.size() <= 2;
                VarHandle vh = MemoryHandles.varHandle(long.class);
                return MemoryAddressImpl.ofNative((long)vh.get(srcPtrFunc.apply(bindings.get(0))));
            } else {
                assert bindings.size() <= 2;
                VarHandle vh = MemoryHandles.varHandle(carrier);
                return vh.get(srcPtrFunc.apply(binding));
            }
        }
    };
}
