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

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeAllocationScope;
import jdk.internal.access.JavaLangInvokeAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.invoke.MethodHandles.collectArguments;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.empty;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.permuteArguments;
import static java.lang.invoke.MethodHandles.tryFinally;
import static java.lang.invoke.MethodType.methodType;
import static sun.security.action.GetBooleanAction.privilegedGetProperty;

/**
 * This class implements native call invocation through a so called 'universal adapter'. A universal adapter takes
 * an array of longs together with a call 'recipe', which is used to move the arguments in the right places as
 * expected by the system ABI.
 */
public class ProgrammableInvoker {
    private static final boolean DEBUG =
        privilegedGetProperty("jdk.internal.foreign.ProgrammableInvoker.DEBUG");
    private static final boolean NO_SPEC =
        privilegedGetProperty("jdk.internal.foreign.ProgrammableInvoker.NO_SPEC");

    private static final VarHandle VH_LONG = MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder());

    private static final MethodHandle MH_INVOKE_MOVES;
    private static final MethodHandle MH_INVOKE_INTERP_BINDINGS;

    private static final MethodHandle MH_UNBOX_ADDRESS;
    private static final MethodHandle MH_BOX_ADDRESS;
    private static final MethodHandle MH_BASE_ADDRESS;
    private static final MethodHandle MH_COPY_BUFFER;
    private static final MethodHandle MH_MAKE_ALLOCATOR;
    private static final MethodHandle MH_CLOSE_ALLOCATOR;
    private static final MethodHandle MH_ALLOCATE_BUFFER;

    private static final Map<ABIDescriptor, Long> adapterStubs = new ConcurrentHashMap<>();

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MH_INVOKE_MOVES = lookup.findVirtual(ProgrammableInvoker.class, "invokeMoves",
                    methodType(Object.class, Object[].class, Binding.Move[].class, Binding.Move[].class));
            MH_INVOKE_INTERP_BINDINGS = lookup.findVirtual(ProgrammableInvoker.class, "invokeInterpBindings",
                    methodType(Object.class, Object[].class, MethodHandle.class, Map.class, Map.class));
            MH_UNBOX_ADDRESS = lookup.findStatic(ProgrammableInvoker.class, "toRawLongValue",
                    methodType(long.class, MemoryAddress.class));
            MH_BOX_ADDRESS = lookup.findStatic(ProgrammableInvoker.class, "ofLong",
                    methodType(MemoryAddress.class, long.class));
            MH_BASE_ADDRESS = lookup.findVirtual(MemorySegment.class, "baseAddress",
                    methodType(MemoryAddress.class));
            MH_COPY_BUFFER = lookup.findStatic(ProgrammableInvoker.class, "copyBuffer",
                    methodType(MemorySegment.class, MemorySegment.class, long.class, long.class, NativeAllocationScope.class));
            MH_MAKE_ALLOCATOR = lookup.findStatic(NativeAllocationScope.class, "boundedScope",
                    methodType(NativeAllocationScope.class, long.class));
            MH_CLOSE_ALLOCATOR = lookup.findVirtual(NativeAllocationScope.class, "close",
                    methodType(void.class));
            MH_ALLOCATE_BUFFER = lookup.findStatic(MemorySegment.class, "allocateNative",
                    methodType(MemorySegment.class, long.class, long.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final ABIDescriptor abi;
    private final BufferLayout layout;
    private final long stackArgsBytes;

    private final CallingSequence callingSequence;

    private final MemoryAddress addr;
    private final long stubAddress;

    private final long bufferCopySize;

    public ProgrammableInvoker(ABIDescriptor abi, MemoryAddress addr, CallingSequence callingSequence) {
        this.abi = abi;
        this.layout = BufferLayout.of(abi);
        this.stubAddress = adapterStubs.computeIfAbsent(abi, key -> generateAdapter(key, layout));

        this.addr = addr;
        this.callingSequence = callingSequence;

        this.stackArgsBytes = callingSequence.argMoveBindings()
                .map(Binding.Move::storage)
                .filter(s -> abi.arch.isStackType(s.type()))
                .count()
                * abi.arch.typeSize(abi.arch.stackType());

        this.bufferCopySize = bufferCopySize(callingSequence);
    }

    private static long bufferCopySize(CallingSequence callingSequence) {
        // FIXME: > 16 bytes alignment might need extra space since the
        // starting address of the allocator might be un-aligned.
        long size = 0;
        for (int i = 0; i < callingSequence.argumentCount(); i++) {
            List<Binding> bindings = callingSequence.argumentBindings(i);
            for (Binding b : bindings) {
                if (b instanceof Binding.Copy) {
                    Binding.Copy c = (Binding.Copy) b;
                    size = Utils.alignUp(size, c.alignment());
                    size += c.size();
                }
            }
        }
        return size;
    }

    public MethodHandle getBoundMethodHandle() {
        Binding.Move[] argMoves = callingSequence.argMoveBindings().toArray(Binding.Move[]::new);
        Class<?>[] argMoveTypes = Arrays.stream(argMoves).map(Binding.Move::type).toArray(Class<?>[]::new);

        Binding.Move[] retMoves = callingSequence.retMoveBindings().toArray(Binding.Move[]::new);
        Class<?> returnType = retMoves.length == 0
                ? void.class
                : retMoves.length == 1
                    ? retMoves[0].type()
                    : Object[].class;

        MethodType intrinsicType = methodType(returnType, argMoveTypes);

        MethodHandle handle = insertArguments(MH_INVOKE_MOVES.bindTo(this), 1, argMoves, retMoves)
                                            .asCollector(Object[].class, intrinsicType.parameterCount())
                                            .asType(intrinsicType);

        if (NO_SPEC || retMoves.length > 1) {
            Map<VMStorage, Integer> argIndexMap = indexMap(argMoves);
            Map<VMStorage, Integer> retIndexMap = indexMap(retMoves);

            handle = insertArguments(MH_INVOKE_INTERP_BINDINGS.bindTo(this), 1, handle, argIndexMap, retIndexMap);
            handle = handle.asCollector(Object[].class, callingSequence.methodType().parameterCount())
                                             .asType(callingSequence.methodType());
         } else {
             handle = specialize(handle);
         }

        return handle;
    }

    private MethodHandle specialize(MethodHandle intrinsicHandle) {
        MethodType type = callingSequence.methodType();
        MethodType intrinsicType = intrinsicHandle.type();

        int insertPos = -1;
        if (bufferCopySize > 0) {
            intrinsicHandle = dropArguments(intrinsicHandle, 0, NativeAllocationScope.class);
            insertPos++;
        }
        for (int i = 0; i < type.parameterCount(); i++) {
            List<Binding> bindings = callingSequence.argumentBindings(i);
            insertPos += bindings.stream().filter(Binding.Move.class::isInstance).count() + 1;
            // We interpret the bindings in reverse since we have to construct a MethodHandle from the bottom up
            for (int j = bindings.size() - 1; j >= 0; j--) {
                Binding binding = bindings.get(j);
                switch (binding.tag()) {
                    case MOVE -> insertPos--; // handled by fallback
                    case DUP ->
                        intrinsicHandle = mergeArguments(intrinsicHandle, insertPos, insertPos + 1);
                    case CONVERT_ADDRESS ->
                        intrinsicHandle = filterArguments(intrinsicHandle, insertPos, MH_UNBOX_ADDRESS);
                    case BASE_ADDRESS ->
                        intrinsicHandle = filterArguments(intrinsicHandle, insertPos, MH_BASE_ADDRESS);
                    case DEREFERENCE -> {
                        Binding.Dereference deref = (Binding.Dereference) binding;
                        MethodHandle filter = filterArguments(
                            deref.varHandle()
                            .toMethodHandle(VarHandle.AccessMode.GET)
                            .asType(methodType(deref.type(), MemoryAddress.class)), 0, MH_BASE_ADDRESS);
                        intrinsicHandle = filterArguments(intrinsicHandle, insertPos, filter);
                    }
                    case COPY_BUFFER -> {
                        Binding.Copy copy = (Binding.Copy) binding;
                        MethodHandle filter = insertArguments(MH_COPY_BUFFER, 1, copy.size(), copy.alignment());
                        intrinsicHandle = collectArguments(intrinsicHandle, insertPos, filter);
                        intrinsicHandle = mergeArguments(intrinsicHandle, 0, insertPos + 1);
                    }
                    default -> throw new IllegalArgumentException("Illegal tag: " + binding.tag());
                }
            }
        }

        if (type.returnType() != void.class) {
            MethodHandle returnFilter = identity(type.returnType());
            List<Binding> bindings = callingSequence.returnBindings();
            for (int j = bindings.size() - 1; j >= 0; j--) {
                Binding binding = bindings.get(j);
                switch (binding.tag()) {
                    case MOVE -> { /* handled by fallback */ }
                    case CONVERT_ADDRESS ->
                        returnFilter = filterArguments(returnFilter, 0, MH_BOX_ADDRESS);
                    case DEREFERENCE -> {
                        Binding.Dereference deref = (Binding.Dereference) binding;
                        MethodHandle setter = deref.varHandle().toMethodHandle(VarHandle.AccessMode.SET);
                        setter = filterArguments(
                            setter.asType(methodType(void.class, MemoryAddress.class, deref.type())),
                            0, MH_BASE_ADDRESS);
                        returnFilter = collectArguments(returnFilter, returnFilter.type().parameterCount(), setter);
                    }
                    case DUP ->
                        // FIXME assumes shape like: (MS, ..., MS, T) R, is that good enough?
                        returnFilter = mergeArguments(returnFilter, 0, returnFilter.type().parameterCount() - 2);
                    case ALLOC_BUFFER -> {
                        Binding.Allocate alloc = (Binding.Allocate) binding;
                        returnFilter = collectArguments(returnFilter, 0,
                                insertArguments(MH_ALLOCATE_BUFFER, 0, alloc.size(), alloc.alignment()));
                    }
                    default ->
                        throw new IllegalArgumentException("Illegal tag: " + binding.tag());
                }
            }

            intrinsicHandle = MethodHandles.filterReturnValue(intrinsicHandle, returnFilter);
        }

        if (bufferCopySize > 0) {
            MethodHandle closer = intrinsicType.returnType() == void.class
                  // (Throwable, NativeAllocationScope) -> void
                ? collectArguments(empty(methodType(void.class, Throwable.class)), 1, MH_CLOSE_ALLOCATOR)
                  // (Throwable, V, NativeAllocationScope) -> V
                : collectArguments(dropArguments(identity(intrinsicHandle.type().returnType()), 0, Throwable.class),
                                   2, MH_CLOSE_ALLOCATOR);
            intrinsicHandle = tryFinally(intrinsicHandle, closer);
            intrinsicHandle = collectArguments(intrinsicHandle, 0, insertArguments(MH_MAKE_ALLOCATOR, 0, bufferCopySize));
        }
        return intrinsicHandle;
    }

    private static MethodHandle mergeArguments(MethodHandle mh, int sourceIndex, int destIndex) {
        MethodType oldType = mh.type();
        Class<?> sourceType = oldType.parameterType(sourceIndex);
        Class<?> destType = oldType.parameterType(destIndex);
        if (sourceType != destType) {
            // TODO meet?
            throw new IllegalArgumentException("Parameter types differ: " + sourceType + " != " + destType);
        }
        MethodType newType = oldType.dropParameterTypes(destIndex, destIndex + 1);
        int[] reorder = new int[oldType.parameterCount()];
        assert destIndex > sourceIndex;
        for (int i = 0, index = 0; i < reorder.length; i++) {
            if (i != destIndex) {
                reorder[i] = index++;
            } else {
                reorder[i] = sourceIndex;
            }
        }
        return permuteArguments(mh, newType, reorder);
    }

    private static MemorySegment copyBuffer(MemorySegment operand, long size, long alignment,
                                    NativeAllocationScope allocator) {
        assert operand.byteSize() == size : "operand size mismatch";
        MemorySegment copy = allocator.allocate(size, alignment).segment();
        copy.copyFrom(operand.asSlice(0, size));
        return copy;
    }

    private static long toRawLongValue(MemoryAddress address) {
        return address.toRawLongValue(); // Workaround for JDK-8239083
    }

    private static MemoryAddress ofLong(long address) {
        return MemoryAddress.ofLong(address); // Workaround for JDK-8239083
    }

    private Map<VMStorage, Integer> indexMap(Binding.Move[] moves) {
        return IntStream.range(0, moves.length)
                        .boxed()
                        .collect(Collectors.toMap(i -> moves[i].storage(), i -> i));
    }

    /**
     * Does a native invocation by moving primitive values from the arg array into an intermediate buffer
     * and calling the assembly stub that forwards arguments from the buffer to the target function
     *
     * @param args an array of primitive values to be copied in to the buffer
     * @param argBindings Binding.Move values describing how arguments should be copied
     * @param returnBindings Binding.Move values describing how return values should be copied
     * @return null, a single primitive value, or an Object[] of primitive values
     */
    Object invokeMoves(Object[] args, Binding.Move[] argBindings, Binding.Move[] returnBindings) {
        MemorySegment stackArgsSeg = null;
        try (MemorySegment argBuffer = MemorySegment.allocateNative(layout.size, 64)) {
            MemoryAddress argsPtr = argBuffer.baseAddress();
            MemoryAddress stackArgs;
            if (stackArgsBytes > 0) {
                stackArgsSeg = MemorySegment.allocateNative(stackArgsBytes, 8);
                stackArgs = stackArgsSeg.baseAddress();
            } else {
                stackArgs = MemoryAddressImpl.NULL;
            }

            VH_LONG.set(argsPtr.addOffset(layout.arguments_next_pc), addr.toRawLongValue());
            VH_LONG.set(argsPtr.addOffset(layout.stack_args_bytes), stackArgsBytes);
            VH_LONG.set(argsPtr.addOffset(layout.stack_args), stackArgs.toRawLongValue());

            for (int i = 0; i < argBindings.length; i++) {
                Binding.Move binding = argBindings[i];
                VMStorage storage = binding.storage();
                MemoryAddress ptr = abi.arch.isStackType(storage.type())
                    ? stackArgs.addOffset(storage.index() * abi.arch.typeSize(abi.arch.stackType()))
                    : argsPtr.addOffset(layout.argOffset(storage));
                SharedUtils.writeOverSized(ptr, binding.type(), args[i]);
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

            if (returnBindings.length == 0) {
                return null;
            } else if (returnBindings.length == 1) {
                Binding.Move move = returnBindings[0];
                VMStorage storage = move.storage();
                return SharedUtils.read(argsPtr.addOffset(layout.retOffset(storage)), move.type());
            } else { // length > 1
                Object[] returns = new Object[returnBindings.length];
                for (int i = 0; i < returnBindings.length; i++) {
                    Binding.Move move = returnBindings[i];
                    VMStorage storage = move.storage();
                    returns[i] = SharedUtils.read(argsPtr.addOffset(layout.retOffset(storage)), move.type());
                }
                return returns;
            }
        } finally {
            if (stackArgsSeg != null) {
                stackArgsSeg.close();
            }
        }
    }

    Object invokeInterpBindings(Object[] args, MethodHandle leaf,
                                Map<VMStorage, Integer> argIndexMap,
                                Map<VMStorage, Integer> retIndexMap) throws Throwable {
        List<MemorySegment> tempBuffers = new ArrayList<>();
        try {
            // do argument processing, get Object[] as result
            Object[] moves = new Object[leaf.type().parameterCount()];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                BindingInterpreter.unbox(arg, callingSequence.argumentBindings(i),
                        (storage, type, value) -> {
                            moves[argIndexMap.get(storage)] = value;
                        }, tempBuffers);
            }

            // call leaf
            Object o = leaf.invokeWithArguments(moves);

            // return value processing
            if (o == null) {
                return null;
            } else if (o instanceof Object[]) {
                Object[] oArr = (Object[]) o;
                return BindingInterpreter.box(callingSequence.returnBindings(),
                        (storage, type) -> oArr[retIndexMap.get(storage)]);
            } else {
                return BindingInterpreter.box(callingSequence.returnBindings(), (storage, type) -> o);
            }
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

