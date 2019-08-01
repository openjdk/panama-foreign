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

import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.*;
import jdk.internal.foreign.abi.x64.ArgumentClassImpl;
import jdk.internal.foreign.abi.x64.SharedUtils;

import jdk.incubator.foreign.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static jdk.internal.foreign.abi.x64.windows.Windowsx64ABI.VARARGS_ANNOTATION_NAME;
import static sun.security.action.GetBooleanAction.privilegedGetProperty;

public class CallingSequenceBuilderImpl extends CallingSequenceBuilder {

    private static final SharedUtils.StorageDebugHelper storageDbgHelper = new SharedUtils.StorageDebugHelper(
            new String[] { "rcx", "rdx", "r8", "r9" },
            new String[] { "rax" },
            new String[0],
            Windowsx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS,
            Windowsx64ABI.MAX_VECTOR_RETURN_REGISTERS
    );

    private static final boolean DEBUG =
            privilegedGetProperty("jdk.internal.foreign.abi.windows.x64.DEBUG");

    private static final int SSE_ARGUMENT_SIZE = 8;
    private static final int STACK_ARGUMENT_SLOT_SIZE = 8;

    CallingSequenceBuilderImpl(MemoryLayout layout) {
        this(layout, new StorageCalculator(false), new StorageCalculator(true));
    }

    private CallingSequenceBuilderImpl(MemoryLayout layout, StorageCalculator retCalculator, StorageCalculator argCalculator) {
        super(MemoryLayouts.WinABI.C_POINTER, layout, retCalculator::addBindings, argCalculator::addBindings);
    }

    @Override
    protected ArgumentInfo makeArgument(MemoryLayout layout, int pos, String name) {
        return new ArgumentInfo(layout, pos, name);
    }

    static class ArgumentInfo extends Argument {
        private final ArgumentClassImpl argumentClass;
        private final boolean isVarArg;
        
        ArgumentInfo(MemoryLayout layout, int argumentIndex, String debugName) {
            super(layout, argumentIndex, debugName);
            this.argumentClass = classifyType(layout, argumentIndex == -1);
            // null will result in false
            this.isVarArg = Boolean.parseBoolean((String) Utils.getAnnotation(layout, VARARGS_ANNOTATION_NAME));
        }

        @Override
        public boolean inMemory() {
            return argumentClass == ArgumentClassImpl.MEMORY;
        }
    }

    private static ArgumentClassImpl classifyValueType(ValueLayout type) {
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

        return clazz;
    }

    static boolean isRegisterAggregate(MemoryLayout type) {
        long size = type.byteSize();
        return size == 1
            || size == 2
            || size == 4
            || size == 8;
    }
    
    private static ArgumentClassImpl classifyStructType(GroupLayout type, boolean isReturn) {
        if(!isRegisterAggregate(type) && isReturn) {
            return ArgumentClassImpl.MEMORY; // used to signal super class
        }

        return ArgumentClassImpl.INTEGER;
    }

    private static ArgumentClassImpl classifyType(MemoryLayout type, boolean isReturn) {
        if (type instanceof ValueLayout) {
            return classifyValueType((ValueLayout) type);
        } else if (type instanceof SequenceLayout) {
            return ArgumentClassImpl.INTEGER;
        } else if (type instanceof GroupLayout) {
            return classifyStructType((GroupLayout) type, isReturn);
        } else {
            throw new IllegalArgumentException("Unhandled type " + type);
        }
    }

    private static void debugBinding(ArgumentBinding ab) {
        if (DEBUG) {
            System.out.println("Argument: " + ab.argument()
                    + " has the binding: (" + storageDbgHelper.getStorageName(ab.storage()) + ") "
                    + ab.storage() + " at offset: " + ab.offset());
        }
    }

    static class StorageCalculator {
        private final boolean forArguments;

        private int nRegs = 0;
        private long stackOffset = 0;

        StorageCalculator(boolean forArguments) {
            this.forArguments = forArguments;
        }

        void addBindings(Argument arg, BiConsumer<StorageClass, ArgumentBinding> bindingConsumer) {
            ArgumentInfo info = (ArgumentInfo) arg;
            if (nRegs >= Windowsx64ABI.MAX_REGISTER_ARGUMENTS) {
                assert forArguments : "no stack returns";
                // stack

                long alignment = Math.max(SharedUtils.alignment(info.layout(), true), STACK_ARGUMENT_SLOT_SIZE);
                stackOffset = Utils.alignUp(stackOffset, alignment);

                Storage storage = new Storage(
                        StorageClass.STACK_ARGUMENT_SLOT,
                        stackOffset / STACK_ARGUMENT_SLOT_SIZE,
                        STACK_ARGUMENT_SLOT_SIZE);
                ArgumentBinding binding = new ArgumentBinding(storage, info, 0);
                debugBinding(binding);
                bindingConsumer.accept(StorageClass.STACK_ARGUMENT_SLOT, binding);
                stackOffset += info.layout().byteSize();
            } else {
                // regs
                Storage storage;
                ArgumentBinding binding;
                switch (info.argumentClass) {
                    case INTEGER:
                    case POINTER:
                        storage = new Storage(
                                forArguments
                                    ? StorageClass.INTEGER_ARGUMENT_REGISTER
                                    : StorageClass.INTEGER_RETURN_REGISTER,
                                nRegs++,
                                SharedUtils.INTEGER_REGISTER_SIZE);
                        binding = new ArgumentBinding(storage, info, 0);
                        debugBinding(binding);
                        bindingConsumer.accept(storage.getStorageClass(), binding);
                        break;
                    case SSE:
                        storage = new Storage(
                                forArguments
                                    ? StorageClass.VECTOR_ARGUMENT_REGISTER
                                    : StorageClass.VECTOR_RETURN_REGISTER,
                                nRegs,
                                SSE_ARGUMENT_SIZE,
                                SharedUtils.VECTOR_REGISTER_SIZE);
                        binding = new ArgumentBinding(storage, info, 0);
                        debugBinding(binding);
                        bindingConsumer.accept(storage.getStorageClass(), binding);

                        if(info.isVarArg) {
                            Storage extraStorage = new Storage(
                                    StorageClass.INTEGER_ARGUMENT_REGISTER,
                                    nRegs,
                                    SharedUtils.INTEGER_REGISTER_SIZE);
                            ArgumentBinding extraBinding = new ArgumentBinding(extraStorage, info, 0);
                            debugBinding(extraBinding);
                            bindingConsumer.accept(extraStorage.getStorageClass(), extraBinding);
                        }
                        nRegs++;
                        break;
                    default:
                        throw new UnsupportedOperationException("Unhandled class " + info.argumentClass);
                }
            }
        }
    }
}
