/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.foreign.abi;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeSymbol;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.ValueLayout;
import jdk.internal.foreign.MemoryAddressImpl;
import sun.security.action.GetPropertyAction;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.exactInvoker;
import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static jdk.internal.foreign.abi.SharedUtils.mergeArguments;
import static sun.security.action.GetBooleanAction.privilegedGetProperty;

/**
 * This class implements upcall invocation from native code through a so called 'universal adapter'. A universal upcall adapter
 * takes an array of storage pointers, which describes the state of the CPU at the time of the upcall. This can be used
 * by the Java code to fetch the upcall arguments and to store the results to the desired location, as per system ABI.
 */
public class ProgrammableUpcallHandler {
    private static final boolean DEBUG =
        privilegedGetProperty("jdk.internal.foreign.ProgrammableUpcallHandler.DEBUG");
    private static final boolean USE_SPEC = Boolean.parseBoolean(
        GetPropertyAction.privilegedGetProperty("jdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC", "true"));
    private static final boolean USE_INTRINSICS = Boolean.parseBoolean(
        GetPropertyAction.privilegedGetProperty("jdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS", "true"));

    private static final VarHandle VH_LONG = ValueLayout.JAVA_LONG.varHandle();

    private static final MethodHandle MH_invokeMoves;
    private static final MethodHandle MH_invokeInterpBindings;

    static {
        try {
            MethodHandles.Lookup lookup = lookup();
            MH_invokeMoves = lookup.findStatic(ProgrammableUpcallHandler.class, "invokeMoves",
                    methodType(void.class, MemoryAddress.class, MethodHandle.class,
                               Binding.VMLoad[].class, Binding.VMStore[].class, ABIDescriptor.class, BufferLayout.class));
            MH_invokeInterpBindings = lookup.findStatic(ProgrammableUpcallHandler.class, "invokeInterpBindings",
                    methodType(Object.class, Object[].class, InvocationData.class));
        } catch (ReflectiveOperationException e) {
            throw new InternalError(e);
        }
    }

    public static NativeSymbol make(ABIDescriptor abi, MethodHandle target, CallingSequence callingSequence, ResourceScope scope) {
        Binding.VMLoad[] argMoves = argMoveBindings(callingSequence);
        Binding.VMStore[] retMoves = retMoveBindings(callingSequence);

        boolean isSimple = !(retMoves.length > 1);

        Class<?> llReturn = retMoves.length == 1 ? retMoves[0].type() : void.class;
        Class<?>[] llParams = Arrays.stream(argMoves).map(Binding.Move::type).toArray(Class<?>[]::new);
        MethodType llType = MethodType.methodType(llReturn, llParams);

        MethodHandle doBindings;
        if (/* USE_SPEC && isSimple */ false) {
            doBindings = specializedBindingHandle(target, callingSequence, llReturn);
            assert doBindings.type() == llType;
        } else {
            Map<VMStorage, Integer> argIndices = SharedUtils.indexMap(argMoves);
            Map<VMStorage, Integer> retIndices = SharedUtils.indexMap(retMoves);
            int spreaderCount = callingSequence.methodType().parameterCount();
            if (callingSequence.isImr()) {
                spreaderCount--; // imr segment is dropped from the argument list
            }
            target = target.asSpreader(Object[].class, spreaderCount);
            InvocationData invData = new InvocationData(target, argIndices, retIndices, callingSequence, retMoves, abi);
            doBindings = insertArguments(MH_invokeInterpBindings, 1, invData);
            doBindings = doBindings.asCollector(Object[].class, llType.parameterCount());
            doBindings = doBindings.asType(llType);
        }

        long entryPoint;
        if (/* USE_INTRINSICS && isSimple && supportsOptimizedUpcalls() */ true) {
            checkPrimitive(doBindings.type());
            doBindings = insertArguments(exactInvoker(doBindings.type()), 0, doBindings);
            VMStorage[] args = Arrays.stream(argMoves).map(Binding.Move::storage).toArray(VMStorage[]::new);
            VMStorage[] rets = Arrays.stream(retMoves).map(Binding.Move::storage).toArray(VMStorage[]::new);
            CallRegs conv = new CallRegs(args, rets);
            entryPoint = allocateOptimizedUpcallStub(doBindings, abi, conv, callingSequence.isImr(), callingSequence.imrSize());
        } else {
            BufferLayout layout = BufferLayout.of(abi);
            MethodHandle doBindingsErased = doBindings.asSpreader(Object[].class, doBindings.type().parameterCount());
            MethodHandle invokeMoves = insertArguments(MH_invokeMoves, 1, doBindingsErased, argMoves, retMoves, abi, layout);
            entryPoint = allocateUpcallStub(invokeMoves, abi, layout);
        }
        return UpcallStubs.makeUpcall(entryPoint, scope);
    }

    private static void checkPrimitive(MethodType type) {
        if (!type.returnType().isPrimitive()
                || type.parameterList().stream().anyMatch(p -> !p.isPrimitive()))
            throw new IllegalArgumentException("MethodHandle type must be primitive: " + type);
    }

    private static Stream<Binding.VMLoad> argMoveBindingsStream(CallingSequence callingSequence) {
        return callingSequence.argumentBindings()
                .filter(Binding.VMLoad.class::isInstance)
                .map(Binding.VMLoad.class::cast);
    }

    private static Binding.VMLoad[] argMoveBindings(CallingSequence callingSequence) {
        return argMoveBindingsStream(callingSequence)
                .toArray(Binding.VMLoad[]::new);
    }

    private static Binding.VMStore[] retMoveBindings(CallingSequence callingSequence) {
        return callingSequence.returnBindings().stream()
                .filter(Binding.VMStore.class::isInstance)
                .map(Binding.VMStore.class::cast)
                .toArray(Binding.VMStore[]::new);
    }

    private static MethodHandle specializedBindingHandle(MethodHandle target, CallingSequence callingSequence,
                                                         Class<?> llReturn) {
        MethodType highLevelType = callingSequence.methodType();

        MethodHandle specializedHandle = target; // initial

        int argAllocatorPos = 0;
        int argInsertPos = 1;
        specializedHandle = dropArguments(specializedHandle, argAllocatorPos, Binding.Context.class);
        for (int i = 0; i < highLevelType.parameterCount(); i++) {
            MethodHandle filter = identity(highLevelType.parameterType(i));
            int filterAllocatorPos = 0;
            int filterInsertPos = 1; // +1 for allocator
            filter = dropArguments(filter, filterAllocatorPos, Binding.Context.class);

            List<Binding> bindings = callingSequence.argumentBindings(i);
            for (int j = bindings.size() - 1; j >= 0; j--) {
                Binding binding = bindings.get(j);
                filter = binding.specialize(filter, filterInsertPos, filterAllocatorPos);
            }
            specializedHandle = MethodHandles.collectArguments(specializedHandle, argInsertPos, filter);
            specializedHandle = mergeArguments(specializedHandle, argAllocatorPos, argInsertPos + filterAllocatorPos);
            argInsertPos += filter.type().parameterCount() - 1; // -1 for allocator
        }

        if (llReturn != void.class) {
            int retAllocatorPos = -1; // assumed not needed
            int retInsertPos = 0;
            MethodHandle filter = identity(llReturn);
            List<Binding> bindings = callingSequence.returnBindings();
            for (int j = bindings.size() - 1; j >= 0; j--) {
                Binding binding = bindings.get(j);
                filter = binding.specialize(filter, retInsertPos, retAllocatorPos);
            }
            specializedHandle = filterReturnValue(specializedHandle, filter);
        }

        specializedHandle = SharedUtils.wrapWithAllocator(specializedHandle, argAllocatorPos, callingSequence.allocationSize(), true);

        return specializedHandle;
    }

    public static void invoke(MethodHandle mh, long address) throws Throwable {
        mh.invokeExact(MemoryAddress.ofLong(address));
    }

    private static void invokeMoves(MemoryAddress buffer, MethodHandle leaf,
                                    Binding.VMLoad[] argBindings, Binding.VMStore[] returnBindings,
                                    ABIDescriptor abi, BufferLayout layout) throws Throwable {
        MemorySegment bufferBase = MemoryAddressImpl.ofLongUnchecked(buffer.toRawLongValue(), layout.size);

        if (DEBUG) {
            System.err.println("Buffer state before:");
            layout.dump(abi.arch, bufferBase, System.err);
        }

        MemorySegment stackArgsBase = MemoryAddressImpl.ofLongUnchecked((long)VH_LONG.get(bufferBase.asSlice(layout.stack_args)));
        Object[] moves = new Object[argBindings.length];
        for (int i = 0; i < moves.length; i++) {
            Binding.VMLoad binding = argBindings[i];
            VMStorage storage = binding.storage();
            MemorySegment ptr = abi.arch.isStackType(storage.type())
                ? stackArgsBase.asSlice(storage.index() * abi.arch.typeSize(abi.arch.stackType()))
                : bufferBase.asSlice(layout.argOffset(storage));
            moves[i] = SharedUtils.read(ptr, binding.type());
        }

        // invokeInterpBindings, and then actual target
        Object o = leaf.invoke(moves);

        if (o == null) {
            // nop
        } else if (o instanceof Object[] returns) {
            for (int i = 0; i < returnBindings.length; i++) {
                Binding.VMStore binding = returnBindings[i];
                VMStorage storage = binding.storage();
                MemorySegment ptr = bufferBase.asSlice(layout.retOffset(storage));
                SharedUtils.writeOverSized(ptr, binding.type(), returns[i]);
            }
        } else { // single Object
            Binding.VMStore binding = returnBindings[0];
            VMStorage storage = binding.storage();
            MemorySegment ptr = bufferBase.asSlice(layout.retOffset(storage));
            SharedUtils.writeOverSized(ptr, binding.type(), o);
        }

        if (DEBUG) {
            System.err.println("Buffer state after:");
            layout.dump(abi.arch, bufferBase, System.err);
        }
    }

    private record InvocationData(MethodHandle leaf,
                                  Map<VMStorage, Integer> argIndexMap,
                                  Map<VMStorage, Integer> retIndexMap,
                                  CallingSequence callingSequence,
                                  Binding.VMStore[] retMoves,
                                  ABIDescriptor abi) {}

    private static Object invokeInterpBindings(Object[] lowLevelArgs, InvocationData invData) throws Throwable {
        Binding.Context allocator = invData.callingSequence.allocationSize() != 0
                ? Binding.Context.ofBoundedAllocator(invData.callingSequence.allocationSize())
                : Binding.Context.ofScope();
        try (allocator) {
            /// Invoke interpreter, got array of high-level arguments back
            Object[] highLevelArgs = new Object[invData.callingSequence.methodType().parameterCount()];
            for (int i = 0; i < highLevelArgs.length; i++) {
                highLevelArgs[i] = BindingInterpreter.box(invData.callingSequence.argumentBindings(i),
                        (storage, type) -> lowLevelArgs[invData.argIndexMap.get(storage)], allocator);
            }

            MemorySegment imrSegment = null;
            if (invData.callingSequence.isImr()) {
                // this one is for us
                imrSegment = (MemorySegment) highLevelArgs[0];
                Object[] newArgs = new Object[highLevelArgs.length - 1];
                System.arraycopy(highLevelArgs, 1, newArgs, 0, newArgs.length);
                highLevelArgs = newArgs;
            }

            if (DEBUG) {
                System.err.println("Java arguments:");
                System.err.println(Arrays.toString(highLevelArgs).indent(2));
            }

            // invoke our target
            Object o = invData.leaf.invoke(highLevelArgs);

            if (DEBUG) {
                System.err.println("Java return:");
                System.err.println(Objects.toString(o).indent(2));
            }

            Object[] returnValues = new Object[invData.retIndexMap.size()];
            if (invData.leaf.type().returnType() != void.class) {
                BindingInterpreter.unbox(o, invData.callingSequence.returnBindings(),
                        (storage, type, value) -> returnValues[invData.retIndexMap.get(storage)] = value, null);
            }

            if (returnValues.length == 0) {
                return null;
            } else if (returnValues.length == 1) {
                return returnValues[0];
            } else {
                assert invData.callingSequence.isImr();

                Binding.VMStore[] retMoves = invData.callingSequence.returnBindings().stream()
                        .filter(Binding.VMStore.class::isInstance)
                        .map(Binding.VMStore.class::cast)
                        .toArray(Binding.VMStore[]::new);

                assert returnValues.length == retMoves.length;
                int imrWriteOffset = 0;
                for (int i = 0; i < retMoves.length; i++) {
                    Binding.VMStore store = retMoves[i];
                    Object value = returnValues[i];
                    SharedUtils.writeOverSized(imrSegment.asSlice(imrWriteOffset), store.type(), value);
                    imrWriteOffset += invData.abi.arch.typeSize(store.storage().type());
                }
                return null;
            }
        } catch(Throwable t) {
            SharedUtils.handleUncaughtException(t);
            return null;
        }
    }

    // used for transporting data into native code
    private static record CallRegs(VMStorage[] argRegs, VMStorage[] retRegs) {}

    static native long allocateOptimizedUpcallStub(MethodHandle mh, ABIDescriptor abi, CallRegs conv, boolean isImr, long imrSize);
    static native long allocateUpcallStub(MethodHandle mh, ABIDescriptor abi, BufferLayout layout);
    static native boolean supportsOptimizedUpcalls();

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
