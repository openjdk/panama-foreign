/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019, Arm Limited. All rights reserved.
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
package jdk.internal.foreign.abi.aarch64;

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.abi.*;

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.function.Function;
import java.util.List;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

/**
 * ABI implementation based on ARM document "Procedure Call Standard for
 * the ARM 64-bit Architecture".
 */
public class AArch64ABI implements SystemABI {
    public static final int MAX_INTEGER_ARGUMENT_REGISTERS = 8;  // r0..r7
    public static final int MAX_INTEGER_RETURN_REGISTERS = 8;    // r0..r7
    public static final int MAX_VECTOR_ARGUMENT_REGISTERS = 8;   // v0..v7 float/SIMD
    public static final int MAX_VECTOR_RETURN_REGISTERS = 8;     // v0..v7 float/SIMD

    private static final String fastPath = privilegedGetProperty("jdk.internal.foreign.NativeInvoker.FASTPATH");
    private static AArch64ABI instance;

    public static AArch64ABI getInstance() {
        if (instance == null) {
            instance = new AArch64ABI();
        }
        return instance;
    }

    @Override
    public MethodHandle downcallHandle(MemoryAddress symbol, MethodType type, FunctionDescriptor function) {
        if (function.isVariadic()) {
            throw new IllegalArgumentException("Variadic function: " + function);
        }

        CallingSequence callingSequence = arrangeCall(function);
        if (fastPath == null || !fastPath.equals("none")) {
            if (DirectSignatureShuffler.acceptDowncall(type, callingSequence)) {
                return DirectNativeInvoker.make(symbol, callingSequence, type);
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for: %s", type.descriptorString()));
            }
        }
        return new UniversalNativeInvoker(MemoryAddressImpl.addressof(symbol),
                callingSequence, type, function, adapter).getBoundMethodHandle();
    }

    @Override
    public MemoryAddress upcallStub(MethodHandle target, FunctionDescriptor function) {
        if (function.isVariadic()) {
            throw new IllegalArgumentException("Variadic function: " + function);
        }
        MethodType type = target.type();
        CallingSequence callingSequence = arrangeCall(function);
        if (fastPath == null || !fastPath.equals("none")) {
            if (DirectSignatureShuffler.acceptUpcall(type, callingSequence)) {
                return UpcallStubs.upcallAddress(new DirectUpcallHandler(target, callingSequence, type));
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for function type %s", function));
            }
        }
        return UpcallStubs.upcallAddress(new UniversalUpcallHandler(target, callingSequence, type, function,
                adapter));
    }

    CallingSequence arrangeCall(FunctionDescriptor function) {
        CallingSequenceBuilder builder = new CallingSequenceBuilderImpl(
            function.returnLayout().orElse(null));
        function.argumentLayouts().forEach(builder::addArgument);
        return builder.build();
    }

    UniversalAdapter adapter = new UniversalAdapter() {

        private boolean storageIsVectorRegister(Storage storage) {
            switch (storage.getStorageClass()) {
            case VECTOR_ARGUMENT_REGISTER:
            case VECTOR_RETURN_REGISTER:
                return true;
            default:
                return false;
            }
        }

        private boolean storageIsIntegerRegister(Storage storage) {
            switch (storage.getStorageClass()) {
            case INTEGER_ARGUMENT_REGISTER:
            case INTEGER_RETURN_REGISTER:
                return true;
            default:
                return false;
            }
        }

        @Override
        public void unboxValue(Object o, Class<?> carrier, MemoryLayout layout,
                               Function<ArgumentBinding, MemoryAddress> dstPtrFunc,
                               List<ArgumentBinding> bindings) {
            if (layout instanceof GroupLayout) {
                MemoryAddress src = ((MemorySegment)o).baseAddress();

                if (layout.bitSize() == 0) {
                    return;    // Ignore empty structs
                }

                if (storageIsVectorRegister(bindings.get(0).storage())) {
                    // The struct is an HFA where each member is passed
                    // in a separate vector register

                    for (ArgumentBinding binding : bindings) {
                        MemoryAddress.copy(src.offset(binding.offset()),
                                     dstPtrFunc.apply(binding),
                                ((GroupLayout)layout).memberLayouts().get(0).byteSize());
                    }
                } else if (CallingSequenceBuilderImpl.isRegisterAggregate(layout) ||
                           CallingSequenceBuilderImpl.isHomogeneousFloatAggregate(layout)) {
                    // Struct is passed in integer registers or on the stack

                    for (ArgumentBinding binding : bindings) {
                        MemoryAddress dst = dstPtrFunc.apply(binding);
                        MemoryAddressImpl srcPtr = (MemoryAddressImpl)src.offset(binding.offset());
                        MemoryAddress.copy(srcPtr, dst,
                                Math.min(binding.storage().getSize(), srcPtr.size() - srcPtr.offset()));
                    }
                }
                else {
                    // Struct is passed by pointer
                    assert bindings.size() == 1;

                    /*
                     * Leak memory for now
                     */
                    MemoryAddress copy = MemorySegment.ofNative(layout).baseAddress();
                    MemoryAddress.copy(src, copy, layout.byteSize());

                    MemoryAddress dst = dstPtrFunc.apply(bindings.get(0));
                    VarHandle vh = MemoryHandles.varHandle(long.class);
                    vh.set(dst, MemoryAddressImpl.addressof(copy));
                }
            } else if (carrier == MemoryAddress.class) {
                assert bindings.size() <= 2;
                VarHandle vh = MemoryHandles.varHandle(long.class);
                MemoryAddress dst = dstPtrFunc.apply(bindings.get(0));
                vh.set(dst, MemoryAddressImpl.addressof((MemoryAddress)o));
            } else {
                assert bindings.size() == 1;
                VarHandle vh = MemoryHandles.varHandle(carrier);
                MemoryAddress dst = dstPtrFunc.apply(bindings.get(0));
                vh.set(dst, o);
            }
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public Object boxValue(Class<?> carrier, MemoryLayout layout,
                               Function<ArgumentBinding, MemoryAddress> srcPtrFunc,
                               List<ArgumentBinding> bindings) {
            if (layout instanceof GroupLayout) {
                /*
                 * Leak memory for now
                 */
                if (layout.bitSize() == 0) {
                    //empty struct!
                    return MemoryAddressImpl.ofNull();
                }

                MemoryAddress rtmp = MemorySegment.ofNative(layout).baseAddress();

                if (storageIsVectorRegister(bindings.get(0).storage())) {
                    // The struct is an HFA where each member is passed
                    // in a separate vector register

                    for (ArgumentBinding binding : bindings) {
                        MemoryAddress.copy(srcPtrFunc.apply(binding),
                                     rtmp.offset(binding.offset()),
                                ((GroupLayout)layout).memberLayouts().get(0).byteSize());
                    }
                } else if (CallingSequenceBuilderImpl.isRegisterAggregate(layout) ||
                           CallingSequenceBuilderImpl.isHomogeneousFloatAggregate(layout)) {
                    // Struct is passed in integer registers or on the stack

                    for (ArgumentBinding binding : bindings) {
                        MemoryAddress dst = rtmp.offset(binding.offset());
                        long remainBytes = layout.byteSize() - binding.offset();
                        MemoryAddress.copy(srcPtrFunc.apply(binding), dst,
                                Math.min(binding.storage().getSize(), remainBytes));
                    }
                } else {
                    // Struct is passed by pointer
                    assert bindings.size() == 1;

                    MemoryAddress local = srcPtrFunc.apply(bindings.get(0));
                    VarHandle longHandle = MemoryHandles.varHandle(long.class);
                    long indirectAddr = (long)longHandle.get(local);
                    MemoryAddress.copy(MemoryAddressImpl.ofNative(indirectAddr), rtmp, layout.byteSize());
                }

                return rtmp.segment();
            } else if (carrier == MemoryAddress.class) {
                assert bindings.size() <= 2;
                VarHandle vh = MemoryHandles.varHandle(long.class);
                return MemoryAddressImpl.ofNative((long)vh.get(srcPtrFunc.apply(bindings.get(0))));
            } else {
                assert bindings.size() <= 2;
                VarHandle vh = MemoryHandles.varHandle(carrier);
                return vh.get(srcPtrFunc.apply(bindings.get(0)));
            }
        }
    };
}
