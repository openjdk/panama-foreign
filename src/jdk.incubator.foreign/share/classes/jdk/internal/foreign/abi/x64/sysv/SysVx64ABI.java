/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi.x64.sysv;

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.*;
import jdk.internal.foreign.abi.x64.ArgumentClassImpl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.*;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

/**
 * ABI implementation based on System V ABI AMD64 supplement v.0.99.6
 */
public class SysVx64ABI implements SystemABI {
    public static final int MAX_INTEGER_ARGUMENT_REGISTERS = 6;
    public static final int MAX_INTEGER_RETURN_REGISTERS = 2;
    public static final int MAX_VECTOR_ARGUMENT_REGISTERS = 8;
    public static final int MAX_VECTOR_RETURN_REGISTERS = 2;
    public static final int MAX_X87_RETURN_REGISTERS = 2;

    private static final String fastPath = privilegedGetProperty("jdk.internal.foreign.NativeInvoker.FASTPATH");
    private static SysVx64ABI instance;

    public static SysVx64ABI getInstance() {
        if (instance == null) {
            instance = new SysVx64ABI();
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
        return new UniversalNativeInvoker(MemoryAddressImpl.addressof(symbol), callingSequence, type, function, adapter)
                .getBoundMethodHandle();
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
        CallingSequenceBuilder stdc = new CallingSequenceBuilderImpl(function.returnLayout().orElse(null));
        function.argumentLayouts().forEach(stdc::addArgument);
        return stdc.build();
    }

    UniversalAdapter adapter = new UniversalAdapter() {
        @Override
        public void unboxValue(Object o, Class<?> carrier, MemoryLayout layout, java.util.function.Function<ArgumentBinding, MemoryAddress> dstPtrFunc,
                               List<ArgumentBinding> bindings) {
            if (layout instanceof GroupLayout) {
                MemoryAddress src = ((MemorySegment)o).baseAddress();
                if (layout.bitSize() != 0) {
                    for (ArgumentBinding binding : bindings) {
                        MemoryAddress dst = dstPtrFunc.apply(binding);
                        MemoryAddressImpl srcPtr = (MemoryAddressImpl)src.offset(binding.offset());
                        MemoryAddress.copy(srcPtr, dst,
                                Math.min(binding.storage().getSize(), srcPtr.size() - srcPtr.offset()));
                    }
                }
            } else if (carrier == MemoryAddress.class) {
                assert bindings.size() <= 2;
                VarHandle vh = MemoryHandles.varHandle(long.class);
                MemoryAddress dst = dstPtrFunc.apply(bindings.get(0));
                vh.set(dst, MemoryAddressImpl.addressof((MemoryAddress)o));
            } else if (isX87(layout)) {
                //for now, we have to use an indirection (when we have value types we can fix this)
                assert bindings.size() <= 2;
                MemoryAddress.copy((MemoryAddress)o, dstPtrFunc.apply(bindings.get(0)), layout.byteSize());
            } else {
                assert bindings.size() == 1;
                VarHandle vh = MemoryHandles.varHandle(carrier);
                MemoryAddress dst = dstPtrFunc.apply(bindings.get(0));
                vh.set(dst, o);
            }
        }

        public Object boxValue(Class<?> carrier, MemoryLayout layout, java.util.function.Function<ArgumentBinding, MemoryAddress> srcPtrFunc,
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

                for (ArgumentBinding binding : bindings) {
                    MemoryAddressImpl dst = (MemoryAddressImpl)rtmp.offset(binding.offset());
                    MemoryAddress.copy(srcPtrFunc.apply(binding), dst,
                            Math.min(binding.storage().getSize(), dst.size() - dst.offset()));
                }

                return rtmp.segment();
            } else if (carrier == MemoryAddress.class) {
                assert bindings.size() <= 2;
                VarHandle vh = MemoryHandles.varHandle(long.class);
                return MemoryAddressImpl.ofNative((long)vh.get(srcPtrFunc.apply(bindings.get(0))));
            } else if (isX87(layout)) {
                //for now, we have to use an indirection (when we have value types we can fix this)
                assert bindings.size() <= 2;
                MemoryAddress dest = MemorySegment.ofNative(layout).baseAddress();
                MemoryAddress.copy(srcPtrFunc.apply(bindings.get(0)), dest, layout.byteSize());
                return dest;
            } else {
                assert bindings.size() == 1;
                VarHandle vh = MemoryHandles.varHandle(carrier);
                return vh.get(srcPtrFunc.apply(bindings.get(0)));
            }
        }
    };

    private boolean isX87(MemoryLayout layout) {
        return Utils.getAnnotation(layout, ArgumentClassImpl.ABI_CLASS) == ArgumentClassImpl.X87;
    }
}
