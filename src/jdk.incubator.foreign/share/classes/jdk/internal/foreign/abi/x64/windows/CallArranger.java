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
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;

import static jdk.internal.foreign.abi.x64.X86_64Architecture.*;
import static jdk.internal.foreign.abi.x64.windows.Windowsx64ABI.VARARGS_ANNOTATION_NAME;

/**
 * For the Windowx x64 C ABI specifically, this class uses the ProgrammableInvoker API, namely CallingSequenceBuilder2
 * to translate a C FunctionDescriptor into a CallingSequence2, which can then be turned into a MethodHandle.
 *
 * This includes taking care of synthetic arguments like pointers to return buffers for 'in-memory' returns.
 */
public class CallArranger {
    private static final int STACK_SLOT_SIZE = 8;

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

    // record
    public static class Bindings {
        public final CallingSequence callingSequence;
        public final boolean isInMemoryReturn;

        Bindings(CallingSequence callingSequence, boolean isInMemoryReturn) {
            this.callingSequence = callingSequence;
            this.isInMemoryReturn = isInMemoryReturn;
        }
    }

    public static Bindings getBindings(MethodType mt, FunctionDescriptor cDesc, boolean forUpcall) {
        SharedUtils.checkFunctionTypes(mt, cDesc);

        class CallingSequenceBuilderHelper {
            final CallingSequenceBuilder csb = new CallingSequenceBuilder(forUpcall);
            final BindingCalculator argCalc =
                forUpcall ? new BoxBindingCalculator(true) : new UnboxBindingCalculator(true);
            final BindingCalculator retCalc =
                forUpcall ? new UnboxBindingCalculator(false) : new BoxBindingCalculator(false);

            void addArgumentBindings(Class<?> carrier, MemoryLayout layout) {
                csb.addArgumentBindings(carrier, layout, argCalc.getBindings(carrier, layout));
            }

            void setReturnBindings(Class<?> carrier, MemoryLayout layout) {
                csb.setReturnBindings(carrier, layout, retCalc.getBindings(carrier, layout));
            }
        }
        var csb = new CallingSequenceBuilderHelper();

        boolean returnInMemory = isInMemoryReturn(cDesc.returnLayout());
        if (returnInMemory) {
            Class<?> carrier = MemoryAddress.class;
            MemoryLayout layout = MemoryLayouts.WinABI.C_POINTER;
            csb.addArgumentBindings(carrier, layout);
            if (forUpcall) {
                csb.setReturnBindings(carrier, layout);
            }
        } else if (cDesc.returnLayout().isPresent()) {
            csb.setReturnBindings(mt.returnType(), cDesc.returnLayout().get());
        }

        for (int i = 0; i < mt.parameterCount(); i++) {
            csb.addArgumentBindings(mt.parameterType(i), cDesc.argumentLayouts().get(i));
        }

        return new Bindings(csb.csb.build(), returnInMemory);
    }

    public static MethodHandle arrangeDowncall(long addr, MethodType mt, FunctionDescriptor cDesc) {
        Bindings bindings = getBindings(mt, cDesc, false);

        MethodHandle handle = new ProgrammableInvoker(CWindows, addr, bindings.callingSequence).getBoundMethodHandle();

        if (bindings.isInMemoryReturn) {
            handle = SharedUtils.adaptDowncallForIMR(handle, cDesc);
        }

        return handle;
    }

    public static UpcallHandler arrangeUpcall(MethodHandle target, MethodType mt, FunctionDescriptor cDesc) {
        Bindings bindings = getBindings(mt, cDesc, true);

        if (bindings.isInMemoryReturn) {
            target = SharedUtils.adaptUpcallForIMR(target);
        }

        return new ProgrammableUpcallHandler(CWindows, target, bindings.callingSequence);
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
        var optAbiType = type.abiType();
        //padding not allowed here
        ArgumentClassImpl clazz = optAbiType.map(Windowsx64ABI::argumentClassFor).
            orElseThrow(()->new IllegalStateException("Unexpected value layout: could not determine ABI class"));
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

    private interface BindingCalculator {
        List<Binding> getBindings(Class<?> carrier, MemoryLayout layout);
    }

    static class UnboxBindingCalculator implements BindingCalculator {
        private final StorageCalculator storageCalculator;

        UnboxBindingCalculator(boolean forArguments) {
            this.storageCalculator = new StorageCalculator(forArguments);
        }

        @Override
        public List<Binding> getBindings(Class<?> carrier, MemoryLayout layout) {
            TypeClass argumentClass = classifyType(layout);
            Binding.Builder bindings = Binding.builder();
            switch (argumentClass) {
                case STRUCT_REGISTER: {
                    assert carrier == MemorySegment.class;
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    Class<?> type = SharedUtils.primitiveCarrierForSize(layout.byteSize());
                    bindings.dereference(0, type)
                            .move(storage, type);
                    break;
                }
                case STRUCT_REFERENCE: {
                    assert carrier == MemorySegment.class;
                    bindings.copy(layout)
                            .baseAddress()
                            .convertAddress();
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.move(storage, long.class);
                    break;
                }
                case POINTER: {
                    bindings.convertAddress();
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.move(storage, long.class);
                    break;
                }
                case INTEGER: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.move(storage, carrier);
                    break;
                }
                case FLOAT: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.VECTOR, layout);
                    bindings.move(storage, carrier);
                    break;
                }
                case VARARG_FLOAT: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.VECTOR, layout);
                    if (!INSTANCE.isStackType(storage.type())) { // need extra for register arg
                        VMStorage extraStorage = storageCalculator.extraVarargsStorage();
                        bindings.dup()
                                .move(extraStorage, carrier);
                    }

                    bindings.move(storage, carrier);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unhandled class " + argumentClass);
            }
            return bindings.build();
        }
    }

    static class BoxBindingCalculator implements BindingCalculator {
        private final StorageCalculator storageCalculator;

        BoxBindingCalculator(boolean forArguments) {
            this.storageCalculator = new StorageCalculator(forArguments);
        }

        @SuppressWarnings("fallthrough")
        @Override
        public List<Binding> getBindings(Class<?> carrier, MemoryLayout layout) {
            TypeClass argumentClass = classifyType(layout);
            Binding.Builder bindings = Binding.builder();
            switch (argumentClass) {
                case STRUCT_REGISTER: {
                    assert carrier == MemorySegment.class;
                    bindings.allocate(layout)
                            .dup();
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    Class<?> type = SharedUtils.primitiveCarrierForSize(layout.byteSize());
                    bindings.move(storage, type)
                            .dereference(0, type);
                    break;
                }
                case STRUCT_REFERENCE: {
                    assert carrier == MemorySegment.class;
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.move(storage, long.class)
                            .convertAddress();
                    // ASSERT SCOPE OF BOXED ADDRESS HERE
                    // caveat. buffer should instead go out of scope after call
                    bindings.copy(layout);
                    break;
                }
                case POINTER: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.move(storage, long.class)
                            .convertAddress();
                    break;
                }
                case INTEGER: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.INTEGER, layout);
                    bindings.move(storage, carrier);
                    break;
                }
                case FLOAT: {
                    VMStorage storage = storageCalculator.nextStorage(StorageClasses.VECTOR, layout);
                    bindings.move(storage, carrier);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unhandled class " + argumentClass);
            }
            return bindings.build();
        }
    }
}
