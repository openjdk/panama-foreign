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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Arrays;

import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.MemorySegmentImpl;
import jdk.internal.foreign.Utils;
import jdk.internal.vm.annotation.Stable;

import static sun.security.action.GetBooleanAction.privilegedGetProperty;

/**
 * This class implements upcall invocation from native code through a so called 'universal adapter'. A universal upcall adapter
 * takes an array of storage pointers, which describes the state of the CPU at the time of the upcall. This can be used
 * by the Java code to fetch the upcall arguments and to store the results to the desired location, as per system ABI.
 */
public class UniversalUpcallHandler implements UpcallHandler {

    private static final boolean DEBUG =
        privilegedGetProperty("jdk.internal.foreign.UpcallHandler.DEBUG");

    private static final VarHandle PTR_HANDLE =
            MemoryLayout.ofValueBits(64, ByteOrder.nativeOrder()).varHandle(long.class);

    @Stable
    private final MethodHandle mh;
    private final MethodType type;
    private final FunctionDescriptor function;
    private final CallingSequence callingSequence;
    private final long entryPoint;
    private final UniversalAdapter adapter;

    public UniversalUpcallHandler(MethodHandle target, CallingSequence callingSequence, MethodType type, FunctionDescriptor function, UniversalAdapter adapter) {
        mh = target.asSpreader(Object[].class, type.parameterCount());
        this.type = type;
        this.adapter = adapter;
        this.function = function;
        this.callingSequence = callingSequence;
        this.entryPoint = allocateUpcallStub();
    }

    @Override
    public long entryPoint() {
        return entryPoint;
    }

    public static void invoke(UniversalUpcallHandler handler, long integers, long vectors,
                              long stack, long integerReturn, long vectorReturn,
                              long x87Return, long indirectResult) {
        UpcallContext context = handler.new UpcallContext(integers, vectors, stack, integerReturn,
                                                          vectorReturn, x87Return, indirectResult);
        handler.invoke(context);
    }

    class UpcallContext {

        private final MemoryAddress integers;
        private final MemoryAddress vectors;
        private final MemoryAddress stack;
        private final MemoryAddress integerReturns;
        private final MemoryAddress vectorReturns;
        private final MemoryAddress x87Returns;
        private final MemoryAddress indirectResult;

        UpcallContext(long integers, long vectors, long stack, long integerReturn, long vectorReturn, long x87Returns, long indirectResults) {
            this.integers = MemoryAddressImpl.ofNative(integers);
            this.vectors = MemoryAddressImpl.ofNative(vectors);
            this.stack = MemoryAddressImpl.ofNative(stack);
            this.integerReturns = MemoryAddressImpl.ofNative(integerReturn);
            this.vectorReturns = MemoryAddressImpl.ofNative(vectorReturn);
            this.x87Returns = MemoryAddressImpl.ofNative(x87Returns);
            this.indirectResult = MemoryAddressImpl.ofNative(indirectResults);
        }

        MemoryAddress getPtr(ArgumentBinding binding) {
            Storage storage = binding.storage();
            switch (storage.getStorageClass()) {
            case INTEGER_ARGUMENT_REGISTER:
                return integers.offset(storage.getStorageIndex() * 8);
            case VECTOR_ARGUMENT_REGISTER:
                return vectors.offset(storage.getStorageIndex() * storage.getMaxSize());
            case STACK_ARGUMENT_SLOT:
                return stack.offset(storage.getStorageIndex() * 8);
            case INTEGER_RETURN_REGISTER:
                return integerReturns.offset(storage.getStorageIndex() * 8);
            case VECTOR_RETURN_REGISTER:
                return vectorReturns.offset(storage.getStorageIndex() * storage.getMaxSize());
            case X87_RETURN_REGISTER:
                return x87Returns.offset(storage.getStorageIndex() * storage.getMaxSize());
            case INDIRECT_RESULT_REGISTER:
                return indirectResult.offset(storage.getStorageIndex() * 8);
            default:
                throw new Error("Unhandled storage: " + storage);
            }
        }

        MemoryAddress inMemoryPtr() {
            assert callingSequence.returnsInMemory();
            MemoryAddress res = getPtr(callingSequence.returnInMemoryBinding());
            long addr = (long)PTR_HANDLE.get(res);
            long size = Utils.alignUp(function.returnLayout().get().byteSize(), 8);
            return MemorySegmentImpl.EVERYTHING.slice(addr, size).baseAddress();
        }

        void setReturnPtr(long ptr) {
            assert callingSequence.returnsInMemory();
            PTR_HANDLE.set(integerReturns, ptr);
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

            Object[] args = new Object[type.parameterCount()];
            for (int i = 0 ; i < type.parameterCount() ; i++) {
                args[i] = adapter.boxValue(type.parameterType(i), function.argumentLayouts().get(i),
                        context::getPtr, callingSequence.argumentBindings(i));
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
                    adapter.unboxValue(o, type.returnType(), function.returnLayout().get(),
                            context::getPtr,
                            callingSequence.returnBindings());
                } else {
                    MemoryAddress inMemPtr = context.inMemoryPtr();
                    MemoryAddress.copy(((MemorySegment)o).baseAddress(), inMemPtr, function.returnLayout().get().byteSize());
                    context.setReturnPtr(MemoryAddressImpl.addressof(inMemPtr)); // Write to RAX
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

    public native long allocateUpcallStub();

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
