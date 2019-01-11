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

import jdk.internal.foreign.ScopeImpl;
import jdk.internal.foreign.memory.BoundedPointer;

import java.foreign.NativeMethodType;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Function;

import static sun.security.action.GetBooleanAction.privilegedGetProperty;

/**
 * This class implements native call invocation through a so called 'universal adapter'. A universal adapter takes
 * an array of longs together with a call 'recipe', which is used to move the arguments in the right places as
 * expected by the system ABI.
 */
public abstract class UniversalNativeInvoker {
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
    private final NativeMethodType nmt;
    private final CallingSequence callingSequence;
    private final long addr;
    private final String methodName;

    protected UniversalNativeInvoker(long addr, String methodName, CallingSequence callingSequence, NativeMethodType nmt) {
        this.addr = addr;
        this.methodName = methodName;
        this.callingSequence = callingSequence;
        this.nmt = nmt;
        this.shuffleRecipe = ShuffleRecipe.make(callingSequence);
    }

    public MethodHandle getBoundMethodHandle() {
        return INVOKE_MH.bindTo(this).asCollector(Object[].class, nmt.parameterCount())
                .asType(nmt.methodType());
    }

    Object invoke(Object[] args) throws Throwable {
        boolean isVoid = nmt.returnType() == NativeTypes.VOID;
        int nValues = shuffleRecipe.getNoofArgumentPulls();
        long[] values = new long[nValues];
        Pointer<Long> argsPtr = nValues > 0 ?
                BoundedPointer.fromLongArray(NativeTypes.UINT64, values) :
                Pointer.nullPointer();

        for (int i = 0 ; i < args.length ; i++) {
            Object arg = args[i];
            unboxValue(arg, nmt.parameterType(i), b -> argsPtr.offset(callingSequence.argumentStorageOffset(b)),
                    callingSequence.getArgumentBindings(i));
        }

        final Pointer<?> retPtr;
        long[] returnValues = new long[shuffleRecipe.getNoofReturnPulls()];
        if (callingSequence.returnsInMemory()) {
            // FIXME (STRUCT-LIFECYCLE):
            // Leak the allocated structs for now until the life cycle has been figured out
            Scope scope = Scope.newNativeScope();
            retPtr = ((ScopeImpl)scope).allocate(nmt.returnType(), 8);
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
            return boxValue(nmt.returnType(), b -> retPtr.offset(callingSequence.returnStorageOffset(b)), callingSequence.getReturnBindings());
        } else {
            return retPtr.get();
        }
    }

    public abstract void unboxValue(Object o, LayoutType<?> type, Function<ArgumentBinding,
                Pointer<?>> dstPtrFunc, List<ArgumentBinding> bindings) throws Throwable;

    public abstract Object boxValue(LayoutType<?> type, Function<ArgumentBinding,
            Pointer<?>> srcPtrFunc, List<ArgumentBinding> bindings) throws IllegalAccessException;

    //natives

    static native void invokeNative(long[] args, long[] rets, long[] recipe, long addr);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
