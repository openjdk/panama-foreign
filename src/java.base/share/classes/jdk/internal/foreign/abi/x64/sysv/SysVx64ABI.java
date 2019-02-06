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

import jdk.internal.foreign.ScopeImpl;
import jdk.internal.foreign.Util;
import jdk.internal.foreign.abi.*;
import jdk.internal.foreign.memory.LayoutTypeImpl;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.layout.*;
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
    private static final String fastPath = privilegedGetProperty("jdk.internal.foreign.NativeInvoker.FASTPATH");
    private static SysVx64ABI instance;

    public static SysVx64ABI getInstance() {
        if (instance == null) {
            instance = new SysVx64ABI();
        }
        return instance;
    }

    @Override
    public Layout layoutOf(CType type) {
        switch (type) {
            case Char:
            case SignedChar:
                return Value.ofSignedInt(8);
            case Bool:
            case UnsignedChar:
                return Value.ofUnsignedInt(8);
            case Short:
                return Value.ofSignedInt(16);
            case UnsignedShort:
                return Value.ofUnsignedInt(16);
            case Int:
                return Value.ofSignedInt(32);
            case UnsignedInt:
                return Value.ofUnsignedInt(32);
            case Long:
            case LongLong:
                return Value.ofSignedInt(64);
            case UnsignedLong:
            case UnsignedLongLong:
                return Value.ofUnsignedInt(64);
            case Float:
                return Value.ofFloatingPoint(32);
            case Double:
                return Value.ofFloatingPoint(64);
            case LongDouble:
                return Value.ofFloatingPoint(128);
            case Pointer:
                return Value.ofUnsignedInt(64);
            default:
                throw new IllegalArgumentException("Unknown layout " + type);
        }
    }

    @Override
    public MethodHandle downcallHandle(CallingConvention cc, Library.Symbol symbol, NativeMethodType nmt) {
        if (nmt.isVarArgs()) {
            return VarargsInvokerImpl.make(symbol, nmt);
        }

        StandardCall sc = new StandardCall();
        CallingSequence callingSequence = sc.arrangeCall(nmt);

        if (fastPath == null || !fastPath.equals("none")) {
            if (LinkToNativeSignatureShuffler.acceptDowncall(nmt, callingSequence)) {
                return LinkToNativeInvoker.make(symbol, callingSequence, nmt);
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for: %s", symbol.getName()));
            }
        }
        return UniversalNativeInvokerImpl.make(symbol, callingSequence, nmt).getBoundMethodHandle();
    }

    @Override
    public Library.Symbol upcallStub(CallingConvention cc, MethodHandle target, NativeMethodType nmt) {
        if (!target.type().equals(nmt.methodType())) {
            throw new WrongMethodTypeException("Native method type has wrong type: " + nmt.methodType());
        }
        StandardCall sc = new StandardCall();
        CallingSequence callingSequence = sc.arrangeCall(nmt);
        if (fastPath == null || !fastPath.equals("none")) {
            if (LinkToNativeSignatureShuffler.acceptUpcall(nmt, callingSequence)) {
                return UpcallStubs.registerUpcallStub(new LinkToNativeUpcallHandler(target, callingSequence, nmt));
            } else if (fastPath != null && fastPath.equals("direct")) {
                throw new IllegalStateException(
                        String.format("No fast path for function type %s", nmt.function()));
            }
        }
        return UpcallStubs.registerUpcallStub(new UniversalUpcallHandlerImpl(target, callingSequence, nmt));
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

    static void unboxValue(Object o, LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> dstPtrFunc,
                           List<ArgumentBinding> bindings) throws Throwable {
        if (o instanceof Struct) {
            Struct<?> struct = (Struct<?>) o;
            if (struct.ptr().type().bytesSize() != 0) {
                Pointer<Long> src = Util.unsafeCast(struct.ptr(), NativeTypes.UINT64);
                for (ArgumentBinding binding : bindings) {
                    Pointer<?> dst = dstPtrFunc.apply(binding);
                    Pointer<Long> srcPtr = src.offset(binding.getOffset() / NativeTypes.UINT64.bytesSize());
                    Pointer.copy(srcPtr, dst, binding.getStorage().getSize());
                }
            }
        } else {
            assert bindings.size() <= 2;
            Pointer<?> dst = Util.unsafeCast(dstPtrFunc.apply(bindings.get(0)), type);
            dst.type().setter().invoke(dst, o);
        }
    }

    @SuppressWarnings("unchecked")
    static Object boxValue(LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> srcPtrFunc,
                           List<ArgumentBinding> bindings) throws IllegalAccessException {
        Class<?> carrier = ((LayoutTypeImpl<?>)type).carrier();
        if (Util.isCStruct(carrier)) {
            /*
             * Leak memory for now
             */
            Scope scope = Scope.newNativeScope();

            if (type.bytesSize() == 0) {
                //empty struct!
                return scope.allocateStruct((Class)carrier);
            }

            @SuppressWarnings({"rawtypes", "unchecked"})
            Pointer<?> rtmp = ((ScopeImpl)scope).allocate(type, 8);

            for (ArgumentBinding binding : bindings) {
                Pointer<Long> dst = Util.unsafeCast(rtmp, NativeTypes.UINT64).offset(binding.getOffset() / NativeTypes.UINT64.bytesSize());
                Pointer.copy(srcPtrFunc.apply(binding), dst, binding.getStorage().getSize());
            }

            return rtmp.get();
        } else {
            assert bindings.size() <= 2;
            return Util.unsafeCast(srcPtrFunc.apply(bindings.get(0)), type).get();
        }
    }
}

