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
import jdk.internal.foreign.memory.Types;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.layout.Value;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.Collection;
import java.util.List;

/**
 * ABI implementation based on Windows ABI AMD64 supplement v.0.99.6
 */
public class Windowsx64ABI implements SystemABI {

    private static Windowsx64ABI instance;

    public static Windowsx64ABI getInstance() {
        if (instance == null) {
            instance = new Windowsx64ABI();
        }
        return instance;
    }

    static CallingSequence arrangeCall(NativeMethodType nmt) {
        return arrangeCall(nmt, Integer.MAX_VALUE);
    }

    static CallingSequence arrangeCall(NativeMethodType nmt, int varArgsStart) {
        Function f = nmt.function();
        CallingSequenceBuilderImpl builder = new CallingSequenceBuilderImpl(
                f.returnLayout().map(x -> new Argument(-1, x, "__retval")).orElse(null));
        for (int i = 0; i < f.argumentLayouts().size(); i++) {
            Layout type = f.argumentLayouts().get(i);
            builder.addArgument(type, i >= varArgsStart, "arg" + i);
        }
        if (f.isVariadic()) {
            builder.addArgument(Types.POINTER, false, null);
        }

        return builder.build();
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
            case Long:
            case Int:
                return Value.ofSignedInt(32);
            case UnsignedInt:
            case UnsignedLong:
                return Value.ofUnsignedInt(32);
            case LongLong:
                return Value.ofSignedInt(64);
            case Pointer:
            case UnsignedLongLong:
                return Value.ofUnsignedInt(64);
            case Float:
                return Value.ofFloatingPoint(32);
            case Double:
            case LongDouble:
                return Value.ofFloatingPoint(64);
            default:
                throw new IllegalArgumentException("Unknown layout " + type);

        }
    }

    @Override
    public MethodHandle downcallHandle(CallingConvention cc, Library.Symbol symbol, NativeMethodType nmt) {
        Util.checkNoArrays(nmt.methodType());
        if (nmt.isVarArgs()) {
            return VarargsInvokerImpl.make(symbol, nmt);
        }

        return UniversalNativeInvokerImpl.make(symbol, arrangeCall(nmt), nmt).getBoundMethodHandle();
    }

    @Override
    public Library.Symbol upcallStub(CallingConvention cc, MethodHandle target, NativeMethodType nmt) {
        Util.checkNoArrays(nmt.methodType());
        if (!target.type().equals(nmt.methodType())) {
            throw new WrongMethodTypeException("Native method type has wrong type: " + nmt.methodType());
        }

        return UpcallStubs.registerUpcallStub(new UniversalUpcallHandlerImpl(target, arrangeCall(nmt), nmt));
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

    static void unboxValue(Object o, LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> dstPtrFunc,
                           List<ArgumentBinding> bindings) throws Throwable {
        if (o instanceof Struct) {
            assert (bindings.size() == 1); // always for structs on windows

            Pointer<?> structPtr = ((Struct<?>) o).ptr();
            LayoutType<?> structType = structPtr.type();
            Pointer<Long> src = Util.unsafeCast(structPtr, NativeTypes.UINT64);
            ArgumentBinding binding = bindings.get(0);

            if (CallingSequenceBuilderImpl.isRegisterAggregate(binding.getMember().getType())) { // pass value
                Util.copy(src, dstPtrFunc.apply(binding), structType.bytesSize());
            } else { // pass a pointer
                /*
                 * Leak memory for now
                 */
                Scope scope = Scope.newNativeScope();
                Pointer<?> copy = scope.allocate(structType);
                Util.copy(src, copy, structType.bytesSize());

                Pointer<?> dst = dstPtrFunc.apply(binding);
                Util.unsafeCast(dst, NativeTypes.UINT64).type().setter().invoke(dst, copy.addr());
            }
        } else {
            assert bindings.size() <= 2;
            Pointer<?> dst = Util.unsafeCast(dstPtrFunc.apply(bindings.get(0)), type);
            dst.type().setter().invoke(dst, o);
        }
    }

    static Object boxValue(LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> srcPtrFunc,
                           List<ArgumentBinding> bindings) throws IllegalAccessException {
        assert (bindings.size() == 1); // always on windows
        ArgumentBinding binding = bindings.get(0);
        Class<?> carrier = ((LayoutTypeImpl<?>) type).carrier();
        if (Util.isCStruct(carrier)) {

            /*
             * Leak memory for now
             */
            Scope scope = Scope.newNativeScope();

            @SuppressWarnings({"rawtypes", "unchecked"})
            Pointer<?> rtmp = ((ScopeImpl) scope).allocate(type, 8);

            if (CallingSequenceBuilderImpl.isRegisterAggregate(type.layout())) {
                Pointer<Long> dst = Util.unsafeCast(rtmp, NativeTypes.UINT64).offset(binding.getOffset() / NativeTypes.UINT64.bytesSize());
                Util.copy(srcPtrFunc.apply(binding), dst, binding.getStorage().getSize());
            } else {
                Pointer<?> local = Util.unsafeCast(srcPtrFunc.apply(binding), type.pointer()).get();
                // need defensive copy since Structs don't have value semantics on the Java side.
                Util.copy(local, rtmp, type.bytesSize());
            }

            return rtmp.get();
        }
        return Util.unsafeCast(srcPtrFunc.apply(binding), type).get();
    }

}