/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019, Arm Limited. All rights reserved.
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
package jdk.internal.foreign.abi.aarch64;

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.Argument;
import jdk.internal.foreign.abi.ArgumentBinding;
import jdk.internal.foreign.abi.CallingSequenceBuilder;
import jdk.internal.foreign.abi.Storage;
import jdk.internal.foreign.abi.StorageClass;

import static sun.security.action.GetBooleanAction.privilegedGetProperty;

public class CallingSequenceBuilderImpl extends CallingSequenceBuilder {

    private static final SharedUtils.StorageDebugHelper storageDbgHelper =
        new SharedUtils.StorageDebugHelper(
            AArch64ABI.MAX_INTEGER_ARGUMENT_REGISTERS,
            AArch64ABI.MAX_INTEGER_RETURN_REGISTERS,
            AArch64ABI.MAX_VECTOR_ARGUMENT_REGISTERS,
            AArch64ABI.MAX_VECTOR_RETURN_REGISTERS);

    private static final boolean DEBUG =
        privilegedGetProperty("jdk.internal.foreign.abi.aarch64.DEBUG");

    private static final int MAX_AGGREGATE_REGS_SIZE = 2;

    public CallingSequenceBuilderImpl(MemoryLayout layout) {
        this(layout, new StorageCalculator(false), new StorageCalculator(true));
    }

    private CallingSequenceBuilderImpl(MemoryLayout layout, StorageCalculator retCalculator,
                                       StorageCalculator argCalculator) {
        super(MemoryLayouts.AArch64ABI.C_POINTER, layout, retCalculator::addBindings, argCalculator::addBindings,
              argCalculator::addBindings);
    }

    @Override
    protected ArgumentInfo makeArgument(MemoryLayout layout, int pos, String name) {
        return new ArgumentInfo(layout, pos, name);
    }

    static class ArgumentInfo extends Argument {
        private final List<ArgumentClassImpl> classes;

        public ArgumentInfo(MemoryLayout layout, int argumentIndex, String debugName) {
            super(layout, argumentIndex, debugName);
            this.classes = classifyType(layout, argumentIndex == -1);
        }

        public int getIntegerRegs() {
            return (int)classes.stream()
                    .filter(cl -> cl == ArgumentClassImpl.INTEGER)
                    .count();
        }

        public int getVectorRegs() {
            return (int)classes.stream()
                    .filter(cl -> cl == ArgumentClassImpl.VECTOR || cl == ArgumentClassImpl.HFA)
                    .count();
        }

        @Override
        public boolean inMemory() {
            return classes.stream().allMatch(this::isMemoryClass);
        }

        private boolean isMemoryClass(ArgumentClassImpl cl) {
            return cl == ArgumentClassImpl.MEMORY;
        }

        public List<ArgumentClassImpl> getClasses() {
            return classes;
        }
    }

    private static List<ArgumentClassImpl> classifyValueType(ValueLayout type) {
        ArrayList<ArgumentClassImpl> classes = new ArrayList<>();

        ArgumentClassImpl clazz = (ArgumentClassImpl)Utils.getAnnotation(type, ArgumentClassImpl.ABI_CLASS);
        if (clazz == null) {
            //padding not allowed here
            throw new IllegalStateException("Unexpected value layout: could not determine ABI class");
        }
        if (clazz == ArgumentClassImpl.POINTER) {
            clazz = ArgumentClassImpl.INTEGER;
        }
        classes.add(clazz);
        if (clazz == ArgumentClassImpl.INTEGER) {
            // int128
            long left = (type.byteSize()) - 8;
            while (left > 0) {
                classes.add(ArgumentClassImpl.INTEGER);
                left -= 8;
            }
            return classes;
        }

        return classes;
    }

    static boolean isRegisterAggregate(MemoryLayout type) {
        return type.bitSize() <= MAX_AGGREGATE_REGS_SIZE * 64;
    }

    static boolean isHomogeneousFloatAggregate(MemoryLayout type) {
        if (!(type instanceof GroupLayout))
            return false;

        GroupLayout groupLayout = (GroupLayout)type;

        final int numElements = groupLayout.memberLayouts().size();
        if (numElements > 4 || numElements == 0)
            return false;

        MemoryLayout baseType = groupLayout.memberLayouts().get(0);

        if (!(baseType instanceof ValueLayout))
            return false;

        ArgumentClassImpl baseArgClass =
            (ArgumentClassImpl)Utils.getAnnotation(baseType, ArgumentClassImpl.ABI_CLASS);
        if (baseArgClass != ArgumentClassImpl.VECTOR)
           return false;

        for (MemoryLayout elem : groupLayout.memberLayouts()) {
            ArgumentClassImpl argClass =
                    (ArgumentClassImpl)Utils.getAnnotation(elem, ArgumentClassImpl.ABI_CLASS);
            if (!(elem instanceof ValueLayout) ||
                    elem.bitSize() != baseType.bitSize() ||
                    elem.bitAlignment() != baseType.bitAlignment() ||
                    baseArgClass != argClass) {
                return false;
            }
        }

        return true;
    }

    private static long countWords(MemoryLayout type) {
        return Utils.alignUp((type.byteSize()), 8) / 8;
    }

    private static List<ArgumentClassImpl> classifyCompositeType(MemoryLayout type,
                                                                 boolean isReturn) {
        ArrayList<ArgumentClassImpl> classes = new ArrayList<>();
        long nWords = countWords(type);

        if (isHomogeneousFloatAggregate(type)) {
            for (MemoryLayout elem : ((GroupLayout)type).memberLayouts()) {
                classes.add(ArgumentClassImpl.HFA);
            }
        } else if (isRegisterAggregate(type)) {
            for (long i = 0; i < nWords; i++) {
                classes.add(ArgumentClassImpl.INTEGER);
            }
        } else {
            if (isReturn) {
                return createMemoryClassArray(nWords);
            } else {
                // Pass a pointer to a copy of the struct
                classes.add(ArgumentClassImpl.INTEGER);
            }
        }

        return classes;
    }

    private static List<ArgumentClassImpl> classifyType(MemoryLayout type, boolean isReturn) {
        try {
            if (type instanceof ValueLayout) {
                return classifyValueType((ValueLayout) type);
            } else if (type instanceof SequenceLayout) {
                return classifyCompositeType((SequenceLayout) type, isReturn);
            } else if (type instanceof GroupLayout) {
                return classifyCompositeType((GroupLayout) type, isReturn);
            } else {
                throw new IllegalArgumentException("Unhandled type " + type);
            }
        } catch (UnsupportedOperationException e) {
            System.err.println("Failed to classify layout: " + type);
            throw e;
        }
    }

    private static List<ArgumentClassImpl> createMemoryClassArray(long n) {
        ArrayList<ArgumentClassImpl> classes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            classes.add(ArgumentClassImpl.MEMORY);
        }

        return classes;
    }

    static class StorageCalculator {
        private final boolean forArguments;

        private int nIntegerRegs = 0;
        private int nVectorRegs = 0;
        private long stackOffset = 0;

        StorageCalculator(boolean forArguments) {
            this.forArguments = forArguments;
        }

        public void addBindings(Argument arg, BiConsumer<StorageClass, ArgumentBinding> bindingConsumer) {
            ArgumentInfo info = (ArgumentInfo)arg;
            if (forArguments && arg.argumentIndex() == -1) {
                // Special case for a larger than 16 byte struct passed by
                // ValueLayout. A pointer to the temporary storage for the result must
                // be passed in the "indirect result register" (r8). This is the
                // only time an argument binding will have index -1.

                assert info.getClasses().size() == 1;
                assert info.getClasses().get(0) == ArgumentClassImpl.INTEGER;

                Storage storage = new Storage(StorageClass.INDIRECT_RESULT_REGISTER, 0,
                                              SharedUtils.INTEGER_REGISTER_SIZE);
                bindingConsumer.accept(storage.getStorageClass(),
                                       new ArgumentBinding(storage, info, 0));

                if (DEBUG) {
                    System.out.println("Argument " + info.name() +
                                       " will be passed in indirect result register");
                }
            } else if (info.inMemory() ||
                    nIntegerRegs + info.getIntegerRegs() > (forArguments ? AArch64ABI.MAX_INTEGER_ARGUMENT_REGISTERS : AArch64ABI.MAX_INTEGER_RETURN_REGISTERS) ||
                    nVectorRegs + info.getVectorRegs() > (forArguments ? AArch64ABI.MAX_VECTOR_ARGUMENT_REGISTERS : AArch64ABI.MAX_VECTOR_RETURN_REGISTERS)) {
                // stack

                long alignment = Math.max(SharedUtils.alignment(info.layout(), true), 8);

                long newStackOffset = Utils.alignUp(stackOffset, alignment);
                stackOffset = newStackOffset;

                List<ArgumentClassImpl> classes = info.getClasses();

                if (!classes.isEmpty() && classes.get(0) == ArgumentClassImpl.HFA) {
                    // This argument was supposed to be passed with each
                    // element in a separate vector register but there aren't
                    // enough free registers so it must be pased on the stack
                    // instead.
                    classes = createMemoryClassArray(countWords(info.layout()));

                    // No more arguments can be assigned to vector registers:
                    // see 5.4.2 "Parameter Passing Rules" stage C.3.
                    nVectorRegs = AArch64ABI.MAX_VECTOR_ARGUMENT_REGISTERS;
                }

                long tmpStackOffset = stackOffset;
                for (int i = 0; i < classes.size(); i++) {
                    Storage storage = new Storage(StorageClass.STACK_ARGUMENT_SLOT, tmpStackOffset / 8, 8);
                    bindingConsumer.accept(StorageClass.STACK_ARGUMENT_SLOT,
                                           new ArgumentBinding(storage, info, i * 8));

                    if (DEBUG) {
                        System.out.println("Argument " + info.name() +
                                           " will be passed on stack at offset " +
                                           tmpStackOffset);
                    }

                    tmpStackOffset += 8;
                }

                stackOffset += info.layout().byteSize();
            } else {
                // Pass in registers
                long offset = 0;
                for (ArgumentClassImpl c : info.getClasses()) {
                    Storage storage;
                    switch (c) {
                    case INTEGER:
                        storage = new Storage(forArguments ? StorageClass.INTEGER_ARGUMENT_REGISTER : StorageClass.INTEGER_RETURN_REGISTER,
                                              nIntegerRegs++,
                                              SharedUtils.INTEGER_REGISTER_SIZE);
                        bindingConsumer.accept(storage.getStorageClass(),
                                               new ArgumentBinding(storage, info, offset));
                        offset += 8;
                        break;

                    case HFA:
                    case VECTOR:
                        storage = new Storage(forArguments ? StorageClass.VECTOR_ARGUMENT_REGISTER : StorageClass.VECTOR_RETURN_REGISTER,
                                              nVectorRegs++,
                                              SharedUtils.VECTOR_REGISTER_SIZE);
                        bindingConsumer.accept(storage.getStorageClass(),
                                               new ArgumentBinding(storage, info, offset));

                        if (c == ArgumentClassImpl.HFA) {
                            // All members in the composite type are passed in
                            // separate vector registers and must be the same
                            // size
                            offset += ((GroupLayout)info.layout()).memberLayouts()
                                .get(0).byteSize();
                        } else {
                            offset += 8;
                        }
                        break;

                    default:
                        throw new UnsupportedOperationException("Unhandled class " + c);
                    }

                    if (DEBUG) {
                        System.out.println("Argument " + info.name() +
                                           " will be passed in register " +
                                           storageDbgHelper.getStorageName(storage));
                    }
                }
            }
        }
    }
}
