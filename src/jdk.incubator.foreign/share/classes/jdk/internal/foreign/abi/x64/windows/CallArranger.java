/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi.x64.windows;

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.ValueLayout;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.CallingSequenceBuilder;
import jdk.internal.foreign.abi.UpcallHandler;
import jdk.internal.foreign.abi.ABIDescriptor;
import jdk.internal.foreign.abi.Binding;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.ProgrammableInvoker;
import jdk.internal.foreign.abi.ProgrammableUpcallHandler;
import jdk.internal.foreign.abi.VMStorage;
import jdk.internal.foreign.abi.x64.X86_64Architecture;
import jdk.internal.foreign.abi.x64.ArgumentClassImpl;
import jdk.internal.foreign.abi.SharedUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.lang.invoke.MethodHandles.collectArguments;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.permuteArguments;
import static java.lang.invoke.MethodType.methodType;
import static jdk.internal.foreign.abi.x64.X86_64Architecture.*;
import static jdk.internal.foreign.abi.x64.windows.Windowsx64ABI.VARARGS_ANNOTATION_NAME;

/**
 * For the Windowx x64 C ABI specifically, this class uses the ProgrammableInvoker API, namely CallingSequenceBuilder2
 * to translate a C FunctionDescriptor into a CallingSequence2, which can then be turned into a MethodHandle.
 *
 * This includes taking care of synthetic arguments like pointers to return buffers for 'in-memory' returns.
 */
public class CallArranger {
    private static final int SSE_ARGUMENT_SIZE = 8;
    private static final int STACK_SLOT_SIZE = 8;
    private static final MethodHandle MH_ALLOC_BUFFER;
    private static final MethodHandle MH_BASEADDRESS;
    private static final MethodHandle MH_BUFFER_COPY;

    private static final ABIDescriptor CWindows = X86_64Architecture.abiFor(
        new VMStorage[] { rcx, rdx, r8, r9 },
        new VMStorage[] { xmm0, xmm1, xmm2, xmm3 },
        new VMStorage[] { rax },
        new VMStorage[] { xmm0 },
        0,
        new VMStorage[] { rax, r10, r11 },
        new VMStorage[] { xmm4, xmm5 },
        16,
        32
    );

    static {
        try {
            var lookup = MethodHandles.lookup();
            MH_ALLOC_BUFFER = lookup.findStatic(MemorySegment.class, "allocateNative",
                    methodType(MemorySegment.class, MemoryLayout.class));
            MH_BASEADDRESS = lookup.findVirtual(MemorySegment.class, "baseAddress",
                    methodType(MemoryAddress.class));
            MH_BUFFER_COPY = lookup.findStatic(CallArranger.class, "bufferCopy",
                    methodType(void.class, MemoryAddress.class, MemorySegment.class));
        } catch (ReflectiveOperationException e) {
            throw new BootstrapMethodError(e);
        }
    }

    public static MethodHandle arrangeDowncall(long addr, MethodType mt, FunctionDescriptor cDesc) {
        assert mt.parameterCount() == cDesc.argumentLayouts().size() : "arity must match!";
        assert (mt.returnType() != void.class) == cDesc.returnLayout().isPresent() : "return type presence must match!";

        CallingSequenceBuilder csb = new CallingSequenceBuilder();

        UnboxBindingCalculator argCalc = new UnboxBindingCalculator(true);
        BoxBindingCalculator retCalc = new BoxBindingCalculator(false);

        boolean returnInMemory = isInMemoryReturn(cDesc.returnLayout());
        if (returnInMemory) {
            csb.addArgument(MemoryAddress.class, MemoryLayouts.WinABI.C_POINTER,
                    argCalc.getBindings(MemoryAddress.class, MemoryLayouts.WinABI.C_POINTER));
        } else if (cDesc.returnLayout().isPresent()) {
            csb.setReturnBindings(mt.returnType(), cDesc.returnLayout().get(),
                    retCalc.getBindings(mt.returnType(), cDesc.returnLayout().get()));
        }

        for (int i = 0; i < mt.parameterCount(); i++) {
            MemoryLayout layout = cDesc.argumentLayouts().get(i);
            csb.addArgument(mt.parameterType(i), layout, argCalc.getBindings(mt.parameterType(i), layout));
        }

        CallingSequence cs = csb.build();
        MethodHandle rawHandle = new ProgrammableInvoker(CWindows, addr, cs).getBoundMethodHandle();

        if (returnInMemory) {
            assert rawHandle.type().returnType() == void.class : "return expected to be void for in memory returns";
            assert rawHandle.type().parameterType(0) == MemoryAddress.class : "MemoryAddress expected as first param";

            MethodHandle ret = identity(MemorySegment.class); // (MemorySegment) MemorySegment
            rawHandle = collectArguments(ret, 1, rawHandle); // (MemorySegment, MemoryAddress ...) MemorySegment
            rawHandle = collectArguments(rawHandle, 1, MH_BASEADDRESS); // (MemorySegment, MemorySegment ...) MemorySegment
            MethodType oldType = rawHandle.type(); // (MemorySegment, MemorySegment, ...) MemorySegment
            MethodType newType = oldType.dropParameterTypes(0, 1); // (MemorySegment, ...) MemorySegment
            int[] reorder = IntStream.concat(IntStream.of(0), IntStream.range(0, oldType.parameterCount() - 1)).toArray(); // [0, 0, 1, 2, 3, ...]
            rawHandle = permuteArguments(rawHandle, newType, reorder); // (MemorySegment, ...) MemoryAddress
            rawHandle = collectArguments(rawHandle, 0, insertArguments(MH_ALLOC_BUFFER, 0, cDesc.returnLayout().get())); // (...) MemoryAddress
        }

        return rawHandle;
    }

    public static UpcallHandler arrangeUpcall(MethodHandle target, MethodType mt, FunctionDescriptor cDesc) {
        assert mt.parameterCount() == cDesc.argumentLayouts().size() : "arity must match!";
        assert (mt.returnType() != void.class) == cDesc.returnLayout().isPresent() : "return type presence must match!";

        CallingSequenceBuilder csb = new CallingSequenceBuilder();

        BoxBindingCalculator argCalc = new BoxBindingCalculator(true);
        UnboxBindingCalculator retCalc = new UnboxBindingCalculator(false);

        boolean returnInMemory = isInMemoryReturn(cDesc.returnLayout());
        if (returnInMemory) {
            csb.addArgument(MemoryAddress.class, MemoryLayouts.WinABI.C_POINTER,
                    argCalc.getBindings(MemoryAddress.class, MemoryLayouts.WinABI.C_POINTER));
            csb.setReturnBindings(MemoryAddress.class, MemoryLayouts.WinABI.C_POINTER,
                    retCalc.getBindings(MemoryAddress.class, MemoryLayouts.WinABI.C_POINTER));
        } else if (cDesc.returnLayout().isPresent()) {
            csb.setReturnBindings(mt.returnType(), cDesc.returnLayout().get(),
                    retCalc.getBindings(mt.returnType(), cDesc.returnLayout().get()));
        }

        for (int i = 0; i < mt.parameterCount(); i++) {
            MemoryLayout layout = cDesc.argumentLayouts().get(i);
            csb.addArgument(mt.parameterType(i), layout, argCalc.getBindings(mt.parameterType(i), layout));
        }

        if (returnInMemory) {
            assert target.type().returnType() == MemorySegment.class : "Must return MemorySegment for IMR";

            target = collectArguments(MH_BUFFER_COPY, 1, target); // erase return type
            int[] reorder = IntStream.range(-1, target.type().parameterCount()).toArray();
            reorder[0] = 0; // [0, 0, 1, 2, 3 ...]
            target = collectArguments(identity(MemoryAddress.class), 1, target); // (MemoryAddress, MemoryAddress, ...) MemoryAddress
            target = permuteArguments(target, target.type().dropParameterTypes(0, 1), reorder); // (MemoryAddress, ...) MemoryAddress
        }

        CallingSequence cs = csb.build();
        return new ProgrammableUpcallHandler(CWindows, target, cs);
    }

    private static void bufferCopy(MemoryAddress dest, MemorySegment buffer) {
        MemoryAddress.copy(buffer.baseAddress(), Utils.resizeNativeAddress(dest, buffer.byteSize()), buffer.byteSize());
    }

    private static boolean isInMemoryReturn(Optional<MemoryLayout> returnLayout) {
        return returnLayout
                .filter(GroupLayout.class::isInstance)
                .filter(g -> !isRegisterAggregate(g))
                .isPresent();
    }

    private enum TypeClass {
        STRUCT_REGISTER,
        STRUCT_REFERENCE,
        POINTER,
        INTEGER,
        FLOAT,
        VARARG_FLOAT
    }

    private static TypeClass classifyValueType(ValueLayout type) {
        ArgumentClassImpl clazz = (ArgumentClassImpl)Utils.getAnnotation(type, ArgumentClassImpl.ABI_CLASS);
        if (clazz == null) {
            //padding not allowed here
            throw new IllegalStateException("Unexpected value layout: could not determine ABI class");
        }

        // No 128 bit integers in the Windows C ABI. There are __m128(i|d) intrinsic types but they act just
        // like a struct when passing as an argument (passed by pointer).
        // https://docs.microsoft.com/en-us/cpp/cpp/m128?view=vs-2019

        // x87 is ignored on Windows:
        // "The x87 register stack is unused, and may be used by the callee,
        // but must be considered volatile across function calls."
        // https://docs.microsoft.com/en-us/cpp/build/x64-calling-convention?view=vs-2019

        if (clazz == ArgumentClassImpl.INTEGER) {
            return TypeClass.INTEGER;
        } else if(clazz == ArgumentClassImpl.POINTER) {
            return TypeClass.POINTER;
        } else if (clazz == ArgumentClassImpl.SSE) {
            if (Boolean.parseBoolean((String) Utils.getAnnotation(type, VARARGS_ANNOTATION_NAME))) {
                return TypeClass.VARARG_FLOAT;
            }
            return TypeClass.FLOAT;
        }
        throw new IllegalArgumentException("Unknown ABI class: " + clazz);
    }

    private static boolean isRegisterAggregate(MemoryLayout type) {
        long size = type.byteSize();
        return size == 1
            || size == 2
            || size == 4
            || size == 8;
    }

    private static TypeClass classifyStructType(MemoryLayout layout) {
        if (isRegisterAggregate(layout)) {
            return TypeClass.STRUCT_REGISTER;
        }
        return TypeClass.STRUCT_REFERENCE;
    }

    private static TypeClass classifyType(MemoryLayout type) {
        if (type instanceof ValueLayout) {
            return classifyValueType((ValueLayout) type);
        } else if (type instanceof  GroupLayout) {
            return classifyStructType(type);
        } else if (type instanceof SequenceLayout) {
            return TypeClass.INTEGER;
        } else {
            throw new IllegalArgumentException("Unhandled type " + type);
        }
    }

    static class StorageCalculator {
        private final boolean forArguments;

        private int nRegs = 0;
        private long stackOffset = 0;

        public StorageCalculator(boolean forArguments) {
            this.forArguments = forArguments;
        }

        VMStorage nextStorage(int type, MemoryLayout layout) {
            if (nRegs >= Windowsx64ABI.MAX_REGISTER_ARGUMENTS) {
                assert forArguments : "no stack returns";
                // stack
                long alignment = Math.max(SharedUtils.alignment(layout, true), STACK_SLOT_SIZE);
                stackOffset = Utils.alignUp(stackOffset, alignment);

                VMStorage storage = X86_64Architecture.stackStorage((int) (stackOffset / STACK_SLOT_SIZE));
                stackOffset += layout.byteSize();
                return storage;
            }
            return (forArguments
                    ? CWindows.inputStorage
                    : CWindows.outputStorage)
                 [type][nRegs++];
        }

        public VMStorage extraVarargsStorage() {
            assert forArguments;
            return CWindows.inputStorage[StorageClasses.INTEGER][nRegs - 1];
        }
    }

    static class UnboxBindingCalculator {
        private final StorageCalculator storageCalculator;

        UnboxBindingCalculator(boolean forArguments) {
            this.storageCalculator = new StorageCalculator(forArguments);
        }

        List<Binding> getBindings(Class<?> carrier, MemoryLayout layout) {
            TypeClass argumentClass = classifyType(layout);
            List<Binding> bindings = new ArrayList<>();
            switch (argumentClass) {
                case STRUCT_REGISTER: {
                    assert carrier == MemorySegment.class;
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.add(new Binding.Dereference(storage, 0, layout.byteSize()));
                    break;
                }
                case STRUCT_REFERENCE: {
                    assert carrier == MemorySegment.class;
                    bindings.add(new Binding.Copy(layout.byteSize(), layout.byteAlignment()));
                    bindings.add(new Binding.BaseAddress());
                    bindings.add(new Binding.BoxAddress());
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.add(new Binding.Move(storage, long.class));
                    break;
                }
                case POINTER: {
                    bindings.add(new Binding.BoxAddress());
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.add(new Binding.Move(storage, long.class));
                    break;
                }
                case INTEGER: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.add(new Binding.Move(storage, carrier));
                    break;
                }
                case FLOAT: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.VECTOR, layout);
                    bindings.add(new Binding.Move(storage, carrier));
                    break;
                }
                case VARARG_FLOAT: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.VECTOR, layout);
                    bindings.add(new Binding.Move(storage, carrier));

                    if (!INSTANCE.isStackType(storage.type())) { // need extra for register arg
                        VMStorage extraStorage = storageCalculator.extraVarargsStorage();
                        bindings.add(new Binding.Move(extraStorage, carrier));
                    }
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unhandled class " + argumentClass);
            }
            return bindings;
        }
    }

    static class BoxBindingCalculator {
        private final StorageCalculator storageCalculator;

        BoxBindingCalculator(boolean forArguments) {
            this.storageCalculator = new StorageCalculator(forArguments);
        }

        @SuppressWarnings("fallthrough")
        List<Binding> getBindings(Class<?> carrier, MemoryLayout layout) {
            TypeClass argumentClass = classifyType(layout);
            List<Binding> bindings = new ArrayList<>();
            switch (argumentClass) {
                case STRUCT_REGISTER: {
                    assert carrier == MemorySegment.class;
                    bindings.add(new Binding.AllocateBuffer(layout));
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.add(new Binding.Dereference(storage, 0, layout.byteSize()));
                    break;
                }
                case STRUCT_REFERENCE: {
                    assert carrier == MemorySegment.class;
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.add(new Binding.Move(storage, long.class));
                    bindings.add(new Binding.BoxAddress());
                    // ASSERT SCOPE OF BOXED ADDRESS HERE
                    // caveat. buffer should instead go out of scope after call
                    bindings.add(new Binding.Copy(layout.byteSize(), layout.byteAlignment()));
                    break;
                }
                case POINTER: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.add(new Binding.Move(storage, long.class));
                    bindings.add(new Binding.BoxAddress());
                    break;
                }
                case INTEGER: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.add(new Binding.Move(storage, carrier));
                    break;
                }
                case FLOAT: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.VECTOR, layout);
                    bindings.add(new Binding.Move(storage, carrier));
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unhandled class " + argumentClass);
            }
            return bindings;
        }
    }
}
