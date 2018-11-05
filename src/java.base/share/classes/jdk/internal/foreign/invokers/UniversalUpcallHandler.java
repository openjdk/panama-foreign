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

package jdk.internal.foreign.invokers;

import jdk.internal.foreign.Util;
import jdk.internal.foreign.abi.ArgumentBinding;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.Storage;
import jdk.internal.foreign.abi.sysv.x64.Constants;
import jdk.internal.foreign.memory.BoundedMemoryRegion;
import jdk.internal.foreign.memory.BoundedPointer;
import jdk.internal.vm.annotation.Stable;

import java.foreign.NativeTypes;
import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

/**
 * This class implements upcall invocation from native code through a so called 'universal adapter'. A universal upcall adapter
 * takes an array of storage pointers, which describes the state of the CPU at the time of the upcall. This can be used
 * by the Java code to fetch the upcall arguments and to store the results to the desired location, as per system ABI.
 */
public class UniversalUpcallHandler extends UpcallHandler {

    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.foreign.UpcallHandler.DEBUG"));

    @Stable
    private final MethodHandle mh;

    private final LayoutType<?> returnLayout;
    private final LayoutType<?>[] argLayouts;

    public UniversalUpcallHandler(CallingSequence callingSequence, Method fiMethod, Function function, Object receiver) {
        super(callingSequence, fiMethod, function, receiver, UniversalUpcallHandler::allocateUpcallStub);

        try {
            MethodHandle mh = MethodHandles.publicLookup().unreflect(fiMethod);
            Util.checkNoArrays(mh.type());
            this.mh = mh.asSpreader(Object[].class, function.argumentLayouts().size());
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }

        if (function.returnLayout().isPresent()) {
            returnLayout = Util.makeType(fiMethod.getGenericReturnType(), function.returnLayout().get());
        } else {
            returnLayout = null;
        }
        List<Layout> args = function.argumentLayouts();
        argLayouts = new LayoutType<?>[args.size()];
        for (int i = 0 ; i < args.size() ; i++) {
            argLayouts[i] = Util.makeType(fiMethod.getGenericParameterTypes()[i], args.get(i));
        }
    }

    public static void invoke(UniversalUpcallHandler handler, long integers, long vectors, long stack, long integerReturn, long vectorReturn, long x87Return) {
        UpcallContext context = handler.new UpcallContext(integers, vectors, stack, integerReturn, vectorReturn, x87Return);
        handler.invoke(context);
    }

    class UpcallContext {

        private final Pointer<Long> integers;
        private final Pointer<Long> vectors;
        private final Pointer<Long> stack;
        private final Pointer<Long> integerReturns;
        private final Pointer<Long> vectorReturns;
        private final Pointer<Long> x87Returns;

        UpcallContext(long integers, long vectors, long stack, long integerReturn, long vectorReturn, long x87Returns) {
            this.integers = BoundedPointer.createNativePointer(NativeTypes.UINT64, integers);
            this.vectors = BoundedPointer.createNativePointer(NativeTypes.UINT64, vectors);
            this.stack = BoundedPointer.createNativePointer(NativeTypes.UINT64, stack);
            this.integerReturns = BoundedPointer.createNativePointer(NativeTypes.UINT64, integerReturn);
            this.vectorReturns = BoundedPointer.createNativePointer(NativeTypes.UINT64, vectorReturn);
            this.x87Returns = BoundedPointer.createNativePointer(NativeTypes.UINT64, x87Returns);
        }

        Pointer<Long> getPtr(ArgumentBinding binding) {
            Storage storage = binding.getStorage();
            switch (storage.getStorageClass()) {
            case INTEGER_ARGUMENT_REGISTER:
                return integers.offset(storage.getStorageIndex());
            case VECTOR_ARGUMENT_REGISTER:
                return vectors.offset(storage.getStorageIndex() * Constants.VECTOR_REGISTER_SIZE / 8);
            case STACK_ARGUMENT_SLOT:
                return stack.offset(storage.getStorageIndex());
            case INTEGER_RETURN_REGISTER:
                return integerReturns.offset(storage.getStorageIndex());
            case VECTOR_RETURN_REGISTER:
                return vectorReturns.offset(storage.getStorageIndex() * Constants.VECTOR_REGISTER_SIZE / 8);
            case X87_RETURN_REGISTER:
                return x87Returns.offset(storage.getStorageIndex() * Constants.X87_REGISTER_SIZE / 8);
            default:
                throw new Error("Unhandled storage: " + storage);
            }
        }

        @SuppressWarnings("unchecked")
        Pointer<Object> inMemoryPtr() {
            assert callingSequence.returnsInMemory();
            Pointer<Long> res = getPtr(callingSequence.getReturnBindings().get(0));
            long structAddr = res.get();
            long size = Util.alignUp(returnLayout.bytesSize(), 8);
            return new BoundedPointer<Object>((LayoutType)returnLayout, new BoundedMemoryRegion(structAddr, size));
        }
    }

    private void invoke(UpcallContext context) {
        try {
            // FIXME: Handle varargs upcalls here
            if (DEBUG) {
                System.err.println("=== UpcallHandler.invoke ===");
                System.err.println(callingSequence.asString());
            }

            Object[] args = new Object[argLayouts.length];
            for (int i = 0 ; i < argLayouts.length ; i++) {
                args[i] = UniversalNativeInvoker.boxValue(argLayouts[i], context::getPtr, callingSequence.getArgumentBindings(i));
            }

            Object o = mh.invoke(receiver, args);

            if (mh.type().returnType() != void.class) {
                if (!callingSequence.returnsInMemory()) {
                    UniversalNativeInvoker.unboxValue(o, returnLayout, context::getPtr,
                            callingSequence.getReturnBindings());
                } else {
                    context.inMemoryPtr().set(o);
                }
            }
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    static native long allocateUpcallStub(UpcallHandler handler);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
