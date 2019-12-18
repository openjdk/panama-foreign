/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.invoke.abi.x64.sysv;

import jdk.internal.foreign.invoke.ScopeImpl;
import jdk.internal.foreign.invoke.Util;
import jdk.internal.foreign.invoke.abi.ArgumentBinding;
import jdk.internal.foreign.invoke.abi.CallingSequence;
import jdk.internal.foreign.invoke.abi.CallingSequenceBuilder;
import jdk.internal.foreign.invoke.abi.DirectNativeInvoker;
import jdk.internal.foreign.invoke.abi.DirectSignatureShuffler;
import jdk.internal.foreign.invoke.abi.DirectUpcallHandler;
import jdk.internal.foreign.invoke.abi.SystemABI;
import jdk.internal.foreign.invoke.abi.UniversalAdapter;
import jdk.internal.foreign.invoke.abi.UniversalNativeInvoker;
import jdk.internal.foreign.invoke.abi.UniversalUpcallHandler;
import jdk.internal.foreign.invoke.abi.UpcallStubs;
import jdk.internal.foreign.invoke.abi.VarargsInvoker;
import jdk.internal.foreign.invoke.memory.LayoutTypeImpl;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
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
        return new UniversalNativeInvoker(symbol, callingSequence, nmt, adapter).getBoundMethodHandle();
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
        CallingSequenceBuilder stdc = new CallingSequenceBuilderImpl(nmt.function().returnLayout().orElse(null));
        nmt.function().argumentLayouts().forEach(stdc::addArgument);
        return stdc.build();
    }

    private static final Map<String, CallingConvention> SysVCallingConventions = new HashMap<>();
    private static final CallingConvention CV_C = () -> "C";

    static {
        SysVCallingConventions.put("C", CV_C);
    }

    @Override
    public CallingConvention defaultCallingConvention() {
        return CV_C;
    }

    @Override
    public CallingConvention namedCallingConvention(String name) throws IllegalArgumentException {
        CallingConvention cv = SysVCallingConventions.get(name);
        if (null == cv) {
            throw new IllegalArgumentException("Unknown calling convention " + name);
        }
        return cv;
    }

    @Override
    public Collection<CallingConvention> callingConventions() {
        return Collections.unmodifiableCollection(SysVCallingConventions.values());
    }

    UniversalAdapter adapter = new UniversalAdapter() {
        @Override
        public void unboxValue(Object o, LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> dstPtrFunc,
                           List<ArgumentBinding> bindings) throws Throwable {
            if (o instanceof Struct) {
                Struct<?> struct = (Struct<?>) o;
                if (struct.ptr().type().bytesSize() != 0) {
                    Pointer<Long> src = Util.unsafeCast(struct.ptr(), NativeTypes.UINT64);
                    for (ArgumentBinding binding : bindings) {
                        Pointer<?> dst = dstPtrFunc.apply(binding);
                        Pointer<Long> srcPtr = src.offset(binding.offset() / NativeTypes.UINT64.bytesSize());
                        Pointer.copy(srcPtr, dst, binding.storage().getSize());
                    }
                }
            } else {
                assert bindings.size() <= 2;
                Pointer<?> dst = Util.unsafeCast(dstPtrFunc.apply(bindings.get(0)), type);
                dst.type().setter().invoke(dst, o);
            }
        }

        @SuppressWarnings("unchecked")
        public Object boxValue(LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> srcPtrFunc,
                               List<ArgumentBinding> bindings) {
            Class<?> carrier = ((LayoutTypeImpl<?>)type).carrier();
            if (Util.isCStruct(carrier)) {
                /*
                 * Leak memory for now
                 */
                Scope scope = Scope.globalScope().fork();

                if (type.bytesSize() == 0) {
                    //empty struct!
                    return scope.allocateStruct((Class)carrier);
                }

                @SuppressWarnings({"rawtypes", "unchecked"})
                Pointer<?> rtmp = ((ScopeImpl)scope).allocate(type, 8);

                for (ArgumentBinding binding : bindings) {
                    Pointer<Long> dst = Util.unsafeCast(rtmp, NativeTypes.UINT64).offset(binding.offset() / NativeTypes.UINT64.bytesSize());
                    Pointer.copy(srcPtrFunc.apply(binding), dst, binding.storage().getSize());
                }

                return rtmp.get();
            } else {
                assert bindings.size() <= 2;
                return Util.unsafeCast(srcPtrFunc.apply(bindings.get(0)), type).get();
            }
        }
    };
}

