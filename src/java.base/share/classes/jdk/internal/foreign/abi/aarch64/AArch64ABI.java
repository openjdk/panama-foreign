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

import jdk.internal.foreign.ScopeImpl;
import jdk.internal.foreign.Util;
import jdk.internal.foreign.abi.*;
import jdk.internal.foreign.memory.LayoutTypeImpl;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.layout.Value;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.function.Function;
import java.util.Collection;
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
    public MethodHandle downcallHandle(CallingConvention cc, Library.Symbol symbol, NativeMethodType nmt) {
        if (nmt.isVarArgs()) {
            return VarargsInvoker.make(symbol, nmt, CallingSequenceBuilderImpl::new, adapter);
        }

        CallingSequence callingSequence = arrangeCall(nmt);
        if (fastPath == null || !fastPath.equals("none")) {
            if (DirectSignatureShuffler.acceptDowncall(nmt, callingSequence)) {
                return DirectNativeInvoker.make(symbol, callingSequence, nmt);
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for: %s", symbol.getName()));
            }
        }
        try {
            return new UniversalNativeInvoker(symbol, callingSequence, nmt,
                    adapter).getBoundMethodHandle();
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Library.Symbol upcallStub(CallingConvention cc, MethodHandle target, NativeMethodType nmt) {
        if (!target.type().equals(nmt.methodType())) {
            throw new WrongMethodTypeException("Native method type has wrong type: " + nmt.methodType());
        }
        CallingSequence callingSequence = arrangeCall(nmt);
        if (fastPath == null || !fastPath.equals("none")) {
            if (DirectSignatureShuffler.acceptUpcall(nmt, callingSequence)) {
                return UpcallStubs.registerUpcallStub(new DirectUpcallHandler(target, callingSequence, nmt));
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for function type %s", nmt.function()));
            }
        }
        return UpcallStubs.registerUpcallStub(new UniversalUpcallHandler(target, callingSequence, nmt,
                adapter));
    }

    CallingSequence arrangeCall(NativeMethodType nmt) {
        CallingSequenceBuilder builder = new CallingSequenceBuilderImpl(
            nmt.function().returnLayout().orElse(null));
        nmt.function().argumentLayouts().forEach(builder::addArgument);
        return builder.build();
    }

    @Override
    public CallingConvention defaultCallingConvention() {
        return null;
    }

    @Override
    public CallingConvention namedCallingConvention(String name) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Collection<CallingConvention> callingConventions() {
        return null;
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
        public void unboxValue(Object o, LayoutType<?> type,
                               Function<ArgumentBinding, Pointer<?>> dstPtrFunc,
                               List<ArgumentBinding> bindings) throws Throwable {
            if (o instanceof Struct) {
                Struct<?> struct = (Struct<?>) o;

                if (struct.ptr().type().bytesSize() == 0) {
                    return;    // Ignore empty structs
                }

                Group layout = (Group)type.layout();

                if (storageIsVectorRegister(bindings.get(0).storage())) {
                    // The struct is an HFA where each member is passed
                    // in a separate vector register

                    Pointer<Byte> src = Util.unsafeCast(struct.ptr(), NativeTypes.UINT8);

                    for (ArgumentBinding binding : bindings) {
                        Pointer.copy(src.offset(binding.offset()),
                                     dstPtrFunc.apply(binding),
                                     layout.elements().get(0).bitsSize() / 8);
                    }
                } else if (CallingSequenceBuilderImpl.isRegisterAggregate(layout) ||
                           CallingSequenceBuilderImpl.isHomogeneousFloatAggregate(layout)) {
                    // Struct is passed in integer registers or on the stack

                    Pointer<Long> src = Util.unsafeCast(struct.ptr(), NativeTypes.UINT64);

                    for (ArgumentBinding binding : bindings) {
                        Pointer<?> dst = dstPtrFunc.apply(binding);
                        Pointer<Long> srcPtr =
                            src.offset(binding.offset() / NativeTypes.UINT64.bytesSize());
                        Pointer.copy(srcPtr, dst, binding.storage().getSize());
                    }
                }
                else {
                    // Struct is passed by pointer
                    assert bindings.size() == 1;

                    Pointer<?> structPtr = struct.ptr();
                    LayoutType<?> structType = structPtr.type();

                    Pointer<Long> src = Util.unsafeCast(structPtr, NativeTypes.UINT64);

                    /*
                     * Leak memory for now
                     */
                    Scope scope = Scope.globalScope().fork();
                    Pointer<?> copy = scope.allocate(structType);
                    Pointer.copy(src, copy, structType.bytesSize());

                    Pointer<?> dst = dstPtrFunc.apply(bindings.get(0));
                    Util.unsafeCast(dst, NativeTypes.UINT64)
                        .type().setter().invoke(dst, copy.addr());
                }
            } else {
                assert bindings.size() <= 2;
                Pointer<?> dst = Util.unsafeCast(dstPtrFunc.apply(bindings.get(0)), type);
                dst.type().setter().invoke(dst, o);
            }
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public Object boxValue(LayoutType<?> type,
                               Function<ArgumentBinding, Pointer<?>> srcPtrFunc,
                               List<ArgumentBinding> bindings) throws IllegalAccessException {
            Class<?> carrier = ((LayoutTypeImpl<?>)type).carrier();
            if (Util.isCStruct(carrier)) {
                /*
                 * Leak memory for now
                 */
                Scope scope = Scope.globalScope().fork();

                if (type.bytesSize() == 0) {
                    // Empty struct
                    return scope.allocateStruct((Class)carrier);
                }

                Pointer<?> rtmp = ((ScopeImpl)scope).allocate(type, 8);

                Group layout = (Group)type.layout();

                if (storageIsVectorRegister(bindings.get(0).storage())) {
                    // The struct is an HFA where each member is passed
                    // in a separate vector register

                    Pointer<Byte> dst = Util.unsafeCast(rtmp, NativeTypes.UINT8);

                    for (ArgumentBinding binding : bindings) {
                        Pointer.copy(srcPtrFunc.apply(binding),
                                     dst.offset(binding.offset()),
                                     layout.elements().get(0).bitsSize() / 8);
                    }
                } else if (CallingSequenceBuilderImpl.isRegisterAggregate(layout) ||
                           CallingSequenceBuilderImpl.isHomogeneousFloatAggregate(layout)) {
                    // Struct is passed in integer registers or on the stack

                    for (ArgumentBinding binding : bindings) {
                        Pointer<Long> dst = Util.unsafeCast(rtmp, NativeTypes.UINT64)
                            .offset(binding.offset() / NativeTypes.UINT64.bytesSize());
                        Pointer.copy(srcPtrFunc.apply(binding), dst,
                                     binding.storage().getSize());
                    }
                } else {
                    // Struct is passed by pointer
                    assert bindings.size() == 1;

                    Pointer<?> local = Util.unsafeCast(srcPtrFunc.apply(bindings.get(0)),
                                                       type.pointer()).get();
                    Pointer.copy(local, rtmp, type.bytesSize());
                }

                return rtmp.get();
            } else {
                assert bindings.size() <= 2;
                return Util.unsafeCast(srcPtrFunc.apply(bindings.get(0)), type).get();
            }
        }
    };
}
