/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
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
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.MemoryAddressImpl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static sun.security.action.GetBooleanAction.privilegedGetProperty;

/**
 * This class implements native call invocation through a so called 'universal adapter'. A universal adapter takes
 * an array of longs together with a call 'recipe', which is used to move the arguments in the right places as
 * expected by the system ABI.
 */
public class ProgrammableInvoker {
    private static final boolean DEBUG =
        privilegedGetProperty("jdk.internal.foreign.ProgrammableInvoker.DEBUG");

    private static final VarHandle VH_LONG = MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder());

    // Unbound MH for the invoke() method
    private static final MethodHandle INVOKE_MH;

    private static final Map<ABIDescriptor, Long> adapterStubs = new ConcurrentHashMap<>();

    static {
        try {
            INVOKE_MH = MethodHandles.lookup().findVirtual(ProgrammableInvoker.class, "invoke", MethodType.methodType(Object.class, Object[].class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final ABIDescriptor abi;
    private final BufferLayout layout;
    private final long stackArgsBytes;

    private final MethodType type;
    private final FunctionDescriptor function;
    private final CallingSequence callingSequence;

    private final MemoryAddress addr;
    private final long stubAddress;

    public ProgrammableInvoker(ABIDescriptor abi, MemoryAddress addr, CallingSequence callingSequence) {
        this.abi = abi;
        this.layout = BufferLayout.of(abi);
        this.stubAddress = adapterStubs.computeIfAbsent(abi, key -> generateAdapter(key, layout));

        this.addr = addr;
        this.callingSequence = callingSequence;
        this.type = callingSequence.methodType();
        this.function = callingSequence.functionDesc();

        this.stackArgsBytes = callingSequence.moveBindings()
                .map(Binding.Move::storage)
                .filter(s -> abi.arch.isStackType(s.type()))
                .count()
                * abi.arch.typeSize(abi.arch.stackType());
    }

    public MethodHandle getBoundMethodHandle() {
        return INVOKE_MH.bindTo(this).asCollector(Object[].class, type.parameterCount()).asType(type);
    }

    Object invoke(Object[] args) {
        List<MemorySegment> tempBuffers = new ArrayList<>();
        try (MemorySegment argBuffer = MemorySegment.allocateNative(layout.size, 64)) {
            MemoryAddress argsPtr = argBuffer.baseAddress();
            MemoryAddress stackArgs;
            if (stackArgsBytes > 0) {
                MemorySegment stackArgsSeg = MemorySegment.allocateNative(stackArgsBytes, 8);
                tempBuffers.add(stackArgsSeg);
                stackArgs = stackArgsSeg.baseAddress();
            } else {
                stackArgs = MemoryAddressImpl.NULL;
            }

            VH_LONG.set(argsPtr.addOffset(layout.arguments_next_pc), addr.toRawLongValue());
            VH_LONG.set(argsPtr.addOffset(layout.stack_args_bytes), stackArgsBytes);
            VH_LONG.set(argsPtr.addOffset(layout.stack_args), stackArgs.toRawLongValue());

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                jdk.internal.foreign.abi.BindingInterpreter.unbox(arg, callingSequence.argumentBindings(i),
                        s -> {
                            if (abi.arch.isStackType(s.type())) {
                                return stackArgs.addOffset(s.index() * abi.arch.typeSize(abi.arch.stackType()));
                            }
                            return argsPtr.addOffset(layout.argOffset(s));
                        }, tempBuffers);
            }

            if (DEBUG) {
                System.err.println("Buffer state before:");
                layout.dump(abi.arch, argsPtr, System.err);
            }

            invokeNative(stubAddress, argsPtr.toRawLongValue());

            if (DEBUG) {
                System.err.println("Buffer state after:");
                layout.dump(abi.arch, argsPtr, System.err);
            }

            return function.returnLayout().isEmpty()
                    ? null
                    : jdk.internal.foreign.abi.BindingInterpreter.box(callingSequence.returnBindings(),
                    s -> argsPtr.addOffset(layout.retOffset(s))); // buffers are leaked
        } finally {
            tempBuffers.forEach(MemorySegment::close);
        }
    }

    //natives

    static native void invokeNative(long adapterStub, long buff);
    static native long generateAdapter(ABIDescriptor abi, BufferLayout layout);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}

