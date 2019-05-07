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

import jdk.internal.foreign.ScopeImpl;
import jdk.internal.foreign.Util;
import jdk.internal.foreign.abi.*;
import jdk.internal.foreign.memory.LayoutTypeImpl;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.layout.Function;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.Collection;
import java.util.List;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

/**
 * ABI implementation based on Windows ABI AMD64 supplement v.0.99.6
 */
public class Windowsx64ABI implements SystemABI {

    private static final String fastPath = privilegedGetProperty("jdk.internal.foreign.NativeInvoker.FASTPATH");

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

    static CallingSequence arrangeCall(NativeMethodType nmt) {
        Function f = nmt.function();
        CallingSequenceBuilder builder = new CallingSequenceBuilderImpl(
                f.returnLayout().orElse(null));
        f.argumentLayouts().forEach(builder::addArgument);
        return builder.build();
    }

    @Override
    public MethodHandle downcallHandle(CallingConvention cc, Library.Symbol symbol, NativeMethodType nmt) {
        Util.checkNoArrays(nmt.methodType());
        if (nmt.isVarArgs()) {
            return VarargsInvoker.make(symbol, nmt, CallingSequenceBuilderImpl::new, adapter);
        }

        CallingSequence callingSequence = arrangeCall(nmt);

        if (fastPath == null || !fastPath.equals("none")) {
            if (LinkToNativeSignatureShuffler.acceptDowncall(nmt, callingSequence)
                    && nmt.function().argumentLayouts().stream().allMatch(CallingSequenceBuilderImpl::isRegisterAggregate)) {
                // TODO allow struct-by-pointer passing (with copy!)
                return LinkToNativeInvoker.make(symbol, callingSequence, nmt);
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for: %s", symbol.getName()));
            }
        }

        try {
            return new UniversalNativeInvoker(symbol, callingSequence, nmt, adapter).getBoundMethodHandle();
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Library.Symbol upcallStub(CallingConvention cc, MethodHandle target, NativeMethodType nmt) {
        Util.checkNoArrays(nmt.methodType());
        if (!target.type().equals(nmt.methodType())) {
            throw new WrongMethodTypeException("Native method type has wrong type: " + nmt.methodType());
        }

        CallingSequence callingSequence = arrangeCall(nmt);

        if (fastPath == null || !fastPath.equals("none")) {
            if (LinkToNativeSignatureShuffler.acceptUpcall(nmt, callingSequence)
                    && nmt.function().argumentLayouts().stream().allMatch(CallingSequenceBuilderImpl::isRegisterAggregate)) {
                // TODO allow struct-by-pointer passing
                return UpcallStubs.registerUpcallStub(new LinkToNativeUpcallHandler(target, callingSequence, nmt));
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for function type %s", nmt.function()));
            }
        }
        return UpcallStubs.registerUpcallStub(new UniversalUpcallHandler(target, callingSequence, nmt, adapter));
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
        @Override
        public void unboxValue(Object o, LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> dstPtrFunc,
                           List<ArgumentBinding> bindings) throws Throwable {
            if (o instanceof Struct) {
                assert (bindings.size() == 1); // always for structs on windows

                Pointer<?> structPtr = ((Struct<?>) o).ptr();
                LayoutType<?> structType = structPtr.type();
                Pointer<Long> src = Util.unsafeCast(structPtr, NativeTypes.UINT64);
                ArgumentBinding binding = bindings.get(0);

                if (CallingSequenceBuilderImpl.isRegisterAggregate(binding.argument().layout())) { // pass value
                    Pointer.copy(src, dstPtrFunc.apply(binding), structType.bytesSize());
                } else { // pass a pointer
                    /*
                     * Leak memory for now
                     */
                    Scope scope = Scope.globalScope().fork();
                    Pointer<?> copy = scope.allocate(structType);
                    Pointer.copy(src, copy, structType.bytesSize());

                    Pointer<?> dst = dstPtrFunc.apply(binding);
                    Util.unsafeCast(dst, NativeTypes.UINT64).type().setter().invoke(dst, copy.addr());
                }
            } else {
                assert bindings.size() <= 2;
                Pointer<?> dst = Util.unsafeCast(dstPtrFunc.apply(bindings.get(0)), type);
                dst.type().setter().invoke(dst, o);
            }
        }

        @Override
        public Object boxValue(LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> srcPtrFunc,
                               List<ArgumentBinding> bindings) throws IllegalAccessException {
            assert (bindings.size() == 1); // always on windows
            ArgumentBinding binding = bindings.get(0);
            Class<?> carrier = ((LayoutTypeImpl<?>) type).carrier();
            if (Util.isCStruct(carrier)) {

                /*
                 * Leak memory for now
                 */
                Scope scope = Scope.globalScope().fork();

                @SuppressWarnings({"rawtypes", "unchecked"})
                Pointer<?> rtmp = ((ScopeImpl) scope).allocate(type, 8);

                if (CallingSequenceBuilderImpl.isRegisterAggregate(type.layout())) {
                    Pointer<Long> dst = Util.unsafeCast(rtmp, NativeTypes.UINT64).offset(binding.offset() / NativeTypes.UINT64.bytesSize());
                    Pointer.copy(srcPtrFunc.apply(binding), dst, binding.storage().getSize());
                } else {
                    Pointer<?> local = Util.unsafeCast(srcPtrFunc.apply(binding), type.pointer()).get();
                    // need defensive copy since Structs don't have value semantics on the Java side.
                    Pointer.copy(local, rtmp, type.bytesSize());
                }

                return rtmp.get();
            }
            return Util.unsafeCast(srcPtrFunc.apply(binding), type).get();
        }
    };
}