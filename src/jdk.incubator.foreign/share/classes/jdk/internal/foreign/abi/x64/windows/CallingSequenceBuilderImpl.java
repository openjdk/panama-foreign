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
import jdk.internal.foreign.abi.x64.ArgumentClass;
import jdk.internal.foreign.abi.x64.SharedUtils;

import jdk.incubator.foreign.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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

    public CallingSequenceBuilderImpl(MemoryLayout layout) {
        this(layout, new StorageCalculator(false), new StorageCalculator(true));
    }

    private CallingSequenceBuilderImpl(MemoryLayout layout, StorageCalculator retCalculator, StorageCalculator argCalculator) {
        super(layout,
                (a, c) -> retCalculator.addBindings(a, c, false),
                (a, c) -> argCalculator.addBindings(a, c, false),
                (a, c) -> argCalculator.addBindings(a, c, true));
    }

    @Override
    protected ArgumentInfo makeArgument(MemoryLayout layout, int pos, String name) {
        return new ArgumentInfo(layout, pos, name);
    }

    static class ArgumentInfo extends Argument {
        private final List<ArgumentClass> classes;
        
        ArgumentInfo(MemoryLayout layout, int argumentIndex, String debugName) {
            super(layout, argumentIndex, debugName);
            this.classes = classifyType(layout, argumentIndex == -1);
        }

        public int getRegs() {
            return (int)classes.stream()
                    .filter(this::isRegisterClass)
                    .count();
        }

        @Override
        public boolean inMemory() {
            return classes.stream().allMatch(this::isMemoryClass);
        }

        private boolean isMemoryClass(ArgumentClass cl) {
            return cl == ArgumentClass.MEMORY ||
                    cl == ArgumentClass.X87 ||
                    cl == ArgumentClass.X87UP;
        }

        private boolean isRegisterClass(ArgumentClass cl) {
            return cl == ArgumentClass.INTEGER ||
                    cl == ArgumentClass.SSE;
        }

        public List<ArgumentClass> getClasses() {
            return classes;
        }
    }

    static List<ArgumentClass> classifyValueType(ValueLayout type) {
        ArrayList<ArgumentClass> classes = new ArrayList<>();


        if (type.isIntegral()) {
            classes.add(ArgumentClass.INTEGER);
            // int128
            long left = (type.byteSize()) - 8;
            while (left > 0) {
                classes.add(ArgumentClass.INTEGER);
                left -= 8;
            }
            return classes;
        } else {
            if ((type.byteSize()) > 8) {
                classes.add(ArgumentClass.X87);
                classes.add(ArgumentClass.X87UP);
                return classes;
            } else {
                classes.add(ArgumentClass.SSE);
                return classes;
            }
        }
    }

    static boolean isRegisterAggregate(MemoryLayout type) {
        // FIXME handle bit size 1, 2, 4
        long size = type.byteSize();
        return size == 1
            || size == 2
            || size == 4
            || size == 8;
    }
    
    private static List<ArgumentClass> createMemoryClassArray(long n) {
        ArrayList<ArgumentClass> classes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            classes.add(ArgumentClass.MEMORY);
        }

        return classes;
    }

    private static List<ArgumentClass> classifyStructType(GroupLayout type, boolean isReturn) {
        ArrayList<ArgumentClass> classes = new ArrayList<>();
        
        if(isRegisterAggregate(type)) {
            classes.add(ArgumentClass.INTEGER);
        } else {
            if(isReturn) {
                return createMemoryClassArray(Utils.alignUp((type.byteSize()), 8));
            } else {
                classes.add(ArgumentClass.INTEGER);
            }
        }

        return classes;
    }

    private static List<ArgumentClass> classifyType(MemoryLayout type, boolean isReturn) {
        if (type instanceof ValueLayout) {
            return classifyValueType((ValueLayout) type);
        } else if (type instanceof AddressLayout) {
            ArrayList<ArgumentClass> classes = new ArrayList<>();
            classes.add(ArgumentClass.INTEGER);
            return classes;
        } else if (type instanceof SequenceLayout) {
            ArrayList<ArgumentClass> classes = new ArrayList<>();
            classes.add(ArgumentClass.INTEGER); // arrrays are always passed as pointers
            return classes;
        } else if (type instanceof GroupLayout) {
            return classifyStructType((GroupLayout) type, isReturn);
        } else {
            throw new IllegalArgumentException("Unhandled type " + type);
        }
    }

    static class StorageCalculator {
        private final boolean forArguments;

        private int nRegs = 0;
        private long stackOffset = 0;

        StorageCalculator(boolean forArguments) {
            this.forArguments = forArguments;
        }

        void addBindings(Argument arg, BiConsumer<StorageClass, ArgumentBinding> bindingConsumer, boolean forVarargs) {
            ArgumentInfo info = (ArgumentInfo)arg;
            if (info.inMemory() ||
                nRegs + info.getRegs() > (forArguments ? Windowsx64ABI.MAX_REGISTER_ARGUMENTS : Windowsx64ABI.MAX_REGISTER_RETURNS)) {
                // stack

                long alignment = Math.max(SharedUtils.alignment(info.layout(), true), 8);

                long newStackOffset = Utils.alignUp(stackOffset, alignment);
                stackOffset = newStackOffset;

                long tmpStackOffset = stackOffset;
                for (int i = 0; i < info.getClasses().size(); i++) {
                    Storage storage = new Storage(StorageClass.STACK_ARGUMENT_SLOT, tmpStackOffset / 8, 8);
                    bindingConsumer.accept(StorageClass.STACK_ARGUMENT_SLOT, new ArgumentBinding(storage, info, i * 8));

                    if (DEBUG) {
                        System.out.println("Argument " + info.name() + " will be passed on stack at offset " + tmpStackOffset);
                    }

                    tmpStackOffset += 8;
                }

                stackOffset += info.layout().byteSize();
            } else {
                // regs
                for (int i = 0; i < info.getClasses().size(); i++) {
                    Storage storage;

                    ArgumentClass c = info.getClasses().get(i);

                    switch (c) {
                    case INTEGER:
                        storage = new Storage(forArguments ? StorageClass.INTEGER_ARGUMENT_REGISTER : StorageClass.INTEGER_RETURN_REGISTER, nRegs++, SharedUtils.INTEGER_REGISTER_SIZE);
                        bindingConsumer.accept(storage.getStorageClass(), new ArgumentBinding(storage, info, i * 8));

                        if (DEBUG) {
                            System.out.println("Argument " + info.name() + " will be passed in register " +
                                    storageDbgHelper.getStorageName(storage));
                        }
                        break;

                    case SSE:
                        int width = 8;

                        for (int j = i + 1; j < info.getClasses().size(); j++) {
                            if (info.getClasses().get(j) == ArgumentClass.SSEUP) {
                                width += 8;
                            }
                        }

                        if (width > 64) {
                            throw new IllegalArgumentException((width * 8) + "-bit vector arguments not supported");
                        }

                        storage = new Storage(forArguments ? StorageClass.VECTOR_ARGUMENT_REGISTER : StorageClass.VECTOR_RETURN_REGISTER,
                                nRegs, width, SharedUtils.VECTOR_REGISTER_SIZE);
                        bindingConsumer.accept(storage.getStorageClass(), new ArgumentBinding(storage, info, i * 8));

                        if (DEBUG) {
                            System.out.println("Argument " + info.name() + " will be passed in register " +
                                    storageDbgHelper.getStorageName(storage));
                        }

                        if(width == 8 && storage.getStorageClass() == StorageClass.VECTOR_ARGUMENT_REGISTER && forVarargs) {
                            Storage extraStorage = new Storage(StorageClass.INTEGER_ARGUMENT_REGISTER, nRegs, SharedUtils.INTEGER_REGISTER_SIZE);
                            bindingConsumer.accept(extraStorage.getStorageClass(), new ArgumentBinding(extraStorage, info, i * 8));

                            if (DEBUG) {
                                System.out.println("Argument " + info.name() + " will be passed in register " +
                                        storageDbgHelper.getStorageName(extraStorage));
                            }
                        }

                        nRegs++;
                        break;

                    case SSEUP:
                        break;

                    default:
                        throw new UnsupportedOperationException("Unhandled class " + c);
                    }
                }
            }
        }
    }
}
