/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
package jdk.internal.foreign.abi;

import java.foreign.Library;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import jdk.internal.foreign.ScopeImpl;
import jdk.internal.foreign.Util;
import jdk.internal.foreign.memory.BoundedPointer;
import jdk.internal.foreign.memory.LayoutTypeImpl;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

/**
 * This class implements native call invocation through a so called 'universal adapter'. A universal adapter takes
 * an array of longs together with a call 'recipe', which is used to move the arguments in the right places as
 * expected by the system ABI.
 */
public class UniversalNativeInvoker {
    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.foreign.NativeInvoker.DEBUG"));

    // Unbound MH for the invoke() method
    private static final MethodHandle INVOKE_MH;
    private static final MethodHandle INVOKE_NATIVE_MH;

    static {
        try {
            MethodType INVOKE_NATIVE_MT = MethodType.methodType(void.class, long[].class, long[].class, long[].class, long.class);
            INVOKE_NATIVE_MH = MethodHandles.lookup().findStatic(UniversalNativeInvoker.class, "invokeNative", INVOKE_NATIVE_MT);
            INVOKE_MH = MethodHandles.lookup().findVirtual(UniversalNativeInvoker.class, "invoke", MethodType.methodType(Object.class, Object[].class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final ShuffleRecipe shuffleRecipe;
    private final LayoutType<?> returnLayoutType;
    private final LayoutType<?>[] argLayoutTypes;
    private final CallingSequence callingSequence;
    private final long addr;
    private final String methodName;

    private UniversalNativeInvoker(long addr, String methodName, CallingSequence callingSequence, LayoutType<?> ret, LayoutType<?>... args) {
        this.addr = addr;
        this.methodName = methodName;
        this.callingSequence = callingSequence;
        this.returnLayoutType = ret;
        this.argLayoutTypes = args;
        this.shuffleRecipe = ShuffleRecipe.make(callingSequence);
    }

    public static MethodHandle make(Library.Symbol symbol, CallingSequence callingSequence, LayoutType<?> ret, LayoutType<?>... args) {
        try {

            UniversalNativeInvoker invoker = new UniversalNativeInvoker(
                    symbol.getAddress().addr(), symbol.getName(), callingSequence, ret, args);
            return INVOKE_MH.bindTo(invoker).asCollector(Object[].class, args.length)
                    .asType(Util.methodType(ret, args));
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }
    }

    Object invoke(Object[] args) throws Throwable {

        boolean isVoid = returnLayoutType == null || returnLayoutType.carrier() == void.class;
        int nValues = shuffleRecipe.getNoofArgumentPulls();
        long[] values = new long[nValues];
        Pointer<Long> argsPtr = nValues > 0 ?
                BoundedPointer.fromLongArray(NativeTypes.UINT64, values) :
                Pointer.nullPointer();

        for (int i = 0 ; i < args.length ; i++) {
            Object arg = args[i];
            unboxValue(arg, argLayoutTypes[i], b -> argsPtr.offset(callingSequence.argumentStorageOffset(b)),
                    callingSequence.getArgumentBindings(i));
        }

        final Pointer<?> retPtr;
        long[] returnValues = new long[shuffleRecipe.getNoofReturnPulls()];
        if (callingSequence.returnsInMemory()) {
            // FIXME (STRUCT-LIFECYCLE):
            // Leak the allocated structs for now until the life cycle has been figured out
            Scope scope = Scope.newNativeScope();
            retPtr = ((ScopeImpl)scope).allocate(returnLayoutType, 8);
            unboxValue(retPtr, NativeTypes.UINT64.pointer(), b -> argsPtr.offset(callingSequence.argumentStorageOffset(b)),
                    callingSequence.getReturnBindings());
        } else if (!isVoid && returnValues.length != 0) {
            retPtr = BoundedPointer.fromLongArray(NativeTypes.UINT64, returnValues);
        } else {
            retPtr = Pointer.nullPointer();
        }

        if (DEBUG) {
            System.err.println("Invoking method " + methodName + " with " + values.length + " argument values");
            for (int i = 0; i < values.length; i++) {
                System.err.println("value[" + i + "] = 0x" + Long.toHexString(values[i]));
            }
        }

        INVOKE_NATIVE_MH.invokeExact(values, returnValues, shuffleRecipe.getRecipe(), addr);

        if (DEBUG) {
            System.err.println("Returned from method " + methodName + " with " + returnValues.length + " return values");
            System.err.println("structPtr = 0x" + Long.toHexString(retPtr.addr()));
            for (int i = 0; i < returnValues.length; i++) {
                System.err.println("returnValues[" + i + "] = 0x" + Long.toHexString(returnValues[i]));
            }
        }

        if (isVoid) {
            return null;
        } else if (!callingSequence.returnsInMemory()) {
            return boxValue(returnLayoutType, b -> retPtr.offset(callingSequence.returnStorageOffset(b)), callingSequence.getReturnBindings());
        } else {
            return retPtr.get();
        }
    }

    // helper routines for marshalling/unmarshalling Java values to and from registers

    static void unboxValue(Object o, LayoutType<?> type, java.util.function.Function<ArgumentBinding, Pointer<?>> dstPtrFunc,
                           List<ArgumentBinding> bindings) throws Throwable {
        if (o instanceof Struct) {
            Struct<?> struct = (Struct<?>) o;
            if (struct.ptr().type().bytesSize() != 0) {
                Pointer<Long> src = Util.unsafeCast(struct.ptr(), NativeTypes.UINT64);
                for (ArgumentBinding binding : bindings) {
                    Pointer<?> dst = dstPtrFunc.apply(binding);
                    Pointer<Long> srcPtr = src.offset(binding.getOffset() / NativeTypes.UINT64.bytesSize());
                    Util.copy(srcPtr, dst, binding.getStorage().getSize());
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
                Util.copy(srcPtrFunc.apply(binding), dst, binding.getStorage().getSize());
            }

            return rtmp.get();
        } else {
            assert bindings.size() <= 2;
            return Util.unsafeCast(srcPtrFunc.apply(bindings.get(0)), type).get();
        }
    }

    //natives

    static native void invokeNative(long[] args, long[] rets, long[] recipe, long addr);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
