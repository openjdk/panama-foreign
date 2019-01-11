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
import java.foreign.NativeMethodType;
import java.foreign.NativeTypes;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import jdk.internal.foreign.Util;
import jdk.internal.foreign.abi.x64.SharedConstants;
import jdk.internal.foreign.memory.BoundedMemoryRegion;
import jdk.internal.foreign.memory.BoundedPointer;
import jdk.internal.vm.annotation.Stable;

import static sun.security.action.GetBooleanAction.privilegedGetProperty;

/**
 * This class implements upcall invocation from native code through a so called 'universal adapter'. A universal upcall adapter
 * takes an array of storage pointers, which describes the state of the CPU at the time of the upcall. This can be used
 * by the Java code to fetch the upcall arguments and to store the results to the desired location, as per system ABI.
 */
public abstract class UniversalUpcallHandler implements Library.Symbol {

    private static final boolean DEBUG =
        privilegedGetProperty("jdk.internal.foreign.UpcallHandler.DEBUG");

    @Stable
    private final MethodHandle mh;
    private final NativeMethodType nmt;
    private final CallingSequence callingSequence;
    private final Pointer<?> entryPoint;

    protected UniversalUpcallHandler(MethodHandle target, CallingSequence callingSequence, NativeMethodType nmt) {
        mh = target.asSpreader(Object[].class, nmt.parameterCount());
        this.nmt = nmt;
        this.callingSequence = callingSequence;
        this.entryPoint = BoundedPointer.createNativeVoidPointer(allocateUpcallStub());
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public Pointer<?> getAddress() {
        return entryPoint;
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
                return vectors.offset(storage.getStorageIndex() * SharedConstants.VECTOR_REGISTER_SIZE / 8);
            case STACK_ARGUMENT_SLOT:
                return stack.offset(storage.getStorageIndex());
            case INTEGER_RETURN_REGISTER:
                return integerReturns.offset(storage.getStorageIndex());
            case VECTOR_RETURN_REGISTER:
                return vectorReturns.offset(storage.getStorageIndex() * SharedConstants.VECTOR_REGISTER_SIZE / 8);
            case X87_RETURN_REGISTER:
                return x87Returns.offset(storage.getStorageIndex() * SharedConstants.X87_REGISTER_SIZE / 8);
            default:
                throw new Error("Unhandled storage: " + storage);
            }
        }

        @SuppressWarnings("unchecked")
        Pointer<Object> inMemoryPtr() {
            assert callingSequence.returnsInMemory();
            Pointer<Long> res = getPtr(callingSequence.getReturnBindings().get(0));
            long structAddr = res.get();
            long size = Util.alignUp(nmt.returnType().bytesSize(), 8);
            return new BoundedPointer<Object>((LayoutType)nmt.returnType(), BoundedMemoryRegion.of(structAddr, size));
        }

        void setReturnPtr(long ptr) {
            assert callingSequence.returnsInMemory();
            integerReturns.set(ptr);
        }

        public String asString() {
            StringBuilder result = new StringBuilder();
            result.append("UpcallContext:\n");
            if (callingSequence.returnsInMemory()) {
                result.append("In memory pointer:\n".indent(2));
                result.append(inMemoryPtr().toString().indent(4));
            }
            for (StorageClass cls : StorageClass.values()) {
                result.append((cls + "\n").indent(2));
                for (ArgumentBinding binding : callingSequence.getBindings(cls)) {
                    BoundedPointer<?> argPtr = (BoundedPointer<?>) getPtr(binding);
                    result.append(argPtr.dump((int) binding.getStorage().getSize()).indent(4));
                }
            }
            return result.toString();
        }
    }

    private void invoke(UpcallContext context) {
        try {
            // FIXME: Handle varargs upcalls here
            if (DEBUG) {
                System.err.println("=== UpcallHandler.invoke ===");
                System.err.println(callingSequence.asString());
                System.err.println(context.asString());
            }

            Object[] args = new Object[nmt.parameterCount()];
            for (int i = 0 ; i < nmt.parameterCount() ; i++) {
                args[i] = boxValue(nmt.parameterType(i), context::getPtr, callingSequence.getArgumentBindings(i));
            }

            if (DEBUG) {
                System.err.println("Java arguments:");
                System.err.println(Arrays.toString(args).indent(2));
            }

            Object o = mh.invoke(args);

            if (DEBUG) {
                System.err.println("Java return:");
                System.err.println(o.toString().indent(2));
            }

            if (mh.type().returnType() != void.class) {
                if (!callingSequence.returnsInMemory()) {
                    unboxValue(o, nmt.returnType(), context::getPtr,
                            callingSequence.getReturnBindings());
                } else {
                    Pointer<Object> inMemPtr = context.inMemoryPtr();
                    inMemPtr.set(o);
                    context.setReturnPtr(inMemPtr.addr()); // Write to RAX
                }
            }

            if (DEBUG) {
                System.err.println("Returning:");
                System.err.println(context.asString().indent(2));
            }
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    public abstract void unboxValue(Object o, LayoutType<?> type, Function<ArgumentBinding,
                Pointer<?>> dstPtrFunc, List<ArgumentBinding> bindings) throws Throwable;

    public abstract Object boxValue(LayoutType<?> type, Function<ArgumentBinding,
            Pointer<?>> srcPtrFunc, List<ArgumentBinding> bindings) throws IllegalAccessException;

    public native long allocateUpcallStub();

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
