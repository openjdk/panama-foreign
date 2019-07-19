/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.MemoryAddressImpl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

import static sun.security.action.GetBooleanAction.privilegedGetProperty;

/**
 * This class implements native call invocation through a so called 'universal adapter'. A universal adapter takes
 * an array of longs together with a call 'recipe', which is used to move the arguments in the right places as
 * expected by the system ABI.
 */
public class UniversalNativeInvoker {
    private static final boolean DEBUG =
        privilegedGetProperty("jdk.internal.foreign.NativeInvoker.DEBUG");

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
    private final MethodType type;
    private final FunctionDescriptor function;
    private final CallingSequence callingSequence;
    private final long addr;
    private final UniversalAdapter adapter;

    public UniversalNativeInvoker(long addr, CallingSequence callingSequence, MethodType type, FunctionDescriptor function, UniversalAdapter adapter) {
        this.addr = addr;
        this.callingSequence = callingSequence;
        this.type = type;
        this.function = function;
        this.adapter = adapter;
        this.shuffleRecipe = ShuffleRecipe.make(callingSequence);
    }

    public MethodHandle getBoundMethodHandle() {
        return INVOKE_MH.bindTo(this).asCollector(Object[].class, type.parameterCount())
                .asType(type);
    }

    Object invoke(Object[] args) throws Throwable {
        boolean isVoid = !function.returnLayout().isPresent();
        int nValues = shuffleRecipe.getNoofArgumentPulls();
        long[] values = new long[nValues];
        MemoryAddress argsPtr = nValues > 0 ?
                MemorySegment.ofArray(values).baseAddress() :
                MemoryAddressImpl.ofNull();

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            adapter.unboxValue(arg, type.parameterType(i), function.argumentLayouts().get(i),
                    b -> shuffleRecipe.offset(argsPtr, b),
                    callingSequence.argumentBindings(i));
        }

        final MemoryAddress retPtr;
        long[] returnValues = new long[shuffleRecipe.getNoofReturnPulls()];
        if (callingSequence.returnsInMemory()) {
            // FIXME (STRUCT-LIFECYCLE):
            // Leak the allocated structs for now until the life cycle has been figured out
            retPtr = MemorySegment.ofNative(function.returnLayout().get().withBitAlignment(64)).baseAddress();
            adapter.unboxValue(retPtr, MemoryAddress.class, MemoryLayout.ofAddress(64), b -> shuffleRecipe.offset(argsPtr, b),
                    List.of(callingSequence.returnInMemoryBinding()));
        } else if (!isVoid && returnValues.length != 0) {
            retPtr = MemorySegment.ofArray(returnValues).baseAddress();
        } else {
            retPtr = MemoryAddressImpl.ofNull();
        }

        if (DEBUG) {
            System.err.println("Invoking method with " + values.length + " argument values");
            for (int i = 0; i < values.length; i++) {
                System.err.println("value[" + i + "] = 0x" + Long.toHexString(values[i]));
            }
        }

        INVOKE_NATIVE_MH.invokeExact(values, returnValues, shuffleRecipe.getRecipe(), addr);

        if (DEBUG) {
            System.err.println("Returned from method with " + returnValues.length + " return values");
            System.err.println("structPtr = 0x" + Long.toHexString(MemoryAddressImpl.addressof(retPtr)));
            for (int i = 0; i < returnValues.length; i++) {
                System.err.println("returnValues[" + i + "] = 0x" + Long.toHexString(returnValues[i]));
            }
        }

        if (isVoid) {
            return null;
        } else if (!callingSequence.returnsInMemory()) {
            return adapter.boxValue(type.returnType(), function.returnLayout().get(),
                    b -> shuffleRecipe.offset(retPtr, b), callingSequence.returnBindings());
        } else {
            return retPtr.segment();
        }
    }

    //natives

    static native void invokeNative(long[] args, long[] rets, long[] recipe, long addr);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
