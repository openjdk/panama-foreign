/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.internal.foreign.invoke.abi.aarch64;

import java.foreign.layout.Address;
import java.foreign.layout.Group;
import java.foreign.layout.Layout;
import java.foreign.layout.Sequence;
import java.foreign.layout.Value;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import jdk.internal.foreign.invoke.Util;
import jdk.internal.foreign.invoke.abi.Argument;
import jdk.internal.foreign.invoke.abi.ArgumentBinding;
import jdk.internal.foreign.invoke.abi.CallingSequenceBuilder;
import jdk.internal.foreign.invoke.abi.Storage;
import jdk.internal.foreign.invoke.abi.StorageClass;

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

    public CallingSequenceBuilderImpl(Layout layout) {
        this(layout, new StorageCalculator(false), new StorageCalculator(true));
    }

    private CallingSequenceBuilderImpl(Layout layout, StorageCalculator retCalculator,
                                       StorageCalculator argCalculator) {
        super(layout, retCalculator::addBindings, argCalculator::addBindings,
              argCalculator::addBindings);
    }

    @Override
    protected ArgumentInfo makeArgument(Layout layout, int pos, String name) {
        return new ArgumentInfo(layout, pos, name);
    }

    static class ArgumentInfo extends jdk.internal.foreign.invoke.abi.Argument {
        private final List<ArgumentClass> classes;

        public ArgumentInfo(Layout layout, int argumentIndex, String debugName) {
            super(layout, argumentIndex, debugName);
            this.classes = classifyType(layout, argumentIndex == -1);
        }

        public int getIntegerRegs() {
            return (int)classes.stream()
                    .filter(cl -> cl == ArgumentClass.INTEGER)
                    .count();
        }

        public int getVectorRegs() {
            return (int)classes.stream()
                    .filter(cl -> cl == ArgumentClass.VECTOR || cl == ArgumentClass.HFA)
                    .count();
        }

        @Override
        public boolean inMemory() {
            return classes.stream().allMatch(this::isMemoryClass);
        }

        private boolean isMemoryClass(ArgumentClass cl) {
            return cl == ArgumentClass.MEMORY;
        }

        public List<ArgumentClass> getClasses() {
            return classes;
        }
    }

    private static List<ArgumentClass> classifyValueType(Value type) {
        ArrayList<ArgumentClass> classes = new ArrayList<>();

        switch (type.kind()) {
            case INTEGRAL_SIGNED: case INTEGRAL_UNSIGNED:
                classes.add(ArgumentClass.INTEGER);
                // int128
                long left = (type.bitsSize() / 8) - 8;
                while (left > 0) {
                    classes.add(ArgumentClass.INTEGER);
                    left -= 8;
                }
                return classes;
            case FLOATING_POINT:
                classes.add(ArgumentClass.VECTOR);
                return classes;
            default:
                throw new IllegalArgumentException("Type " + type + " is not yet supported");
        }
    }

    static boolean isRegisterAggregate(Layout type) {
        return type.bitsSize() <= MAX_AGGREGATE_REGS_SIZE * 64;
    }

    static boolean isHomogeneousFloatAggregate(Layout type) {
        if (!(type instanceof Group))
            return false;

        Group group = (Group)type;

        final int numElements = group.elements().size();
        if (numElements > 4 || numElements == 0)
            return false;

        Layout baseType = group.elements().get(0).stripAnnotations();

        if (!(baseType instanceof Value))
            return false;

        if (((Value)baseType).kind() != Value.Kind.FLOATING_POINT)
            return false;

        for (Layout elem : group.elements()) {
            if (!elem.stripAnnotations().equals(baseType))
                return false;
        }

        return true;
    }

    private static long countWords(Layout type) {
        return Util.alignUp((type.bitsSize() / 8), 8) / 8;
    }

    private static List<ArgumentClass> classifyCompositeType(Layout type,
                                                             boolean isReturn) {
        ArrayList<ArgumentClass> classes = new ArrayList<>();
        long nWords = countWords(type);

        if (isHomogeneousFloatAggregate(type)) {
            for (Layout elem : ((Group)type).elements()) {
                classes.add(ArgumentClass.HFA);
            }
        } else if (isRegisterAggregate(type)) {
            for (long i = 0; i < nWords; i++) {
                classes.add(ArgumentClass.INTEGER);
            }
        } else {
            if (isReturn) {
                return createMemoryClassArray(nWords);
            } else {
                // Pass a pointer to a copy of the struct
                classes.add(ArgumentClass.INTEGER);
            }
        }

        return classes;
    }

    private static List<ArgumentClass> classifyType(Layout type, boolean isReturn) {
        try {
            if (type instanceof Value) {
                return classifyValueType((Value) type);
            } else if (type instanceof Address) {
                ArrayList<ArgumentClass> classes = new ArrayList<>();
                classes.add(ArgumentClass.INTEGER);
                return classes;
            } else if (type instanceof Sequence) {
                return classifyCompositeType((Sequence) type, isReturn);
            } else if (type instanceof Group) {
                return classifyCompositeType((Group) type, isReturn);
            } else {
                throw new IllegalArgumentException("Unhandled type " + type);
            }
        } catch (UnsupportedOperationException e) {
            System.err.println("Failed to classify layout: " + type);
            throw e;
        }
    }

    private static List<ArgumentClass> createMemoryClassArray(long n) {
        ArrayList<ArgumentClass> classes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            classes.add(ArgumentClass.MEMORY);
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
                // value. A pointer to the temporary storage for the result must
                // be passed in the "indirect result register" (r8). This is the
                // only time an argument binding will have index -1.

                assert info.getClasses().size() == 1;
                assert info.getClasses().get(0) == ArgumentClass.INTEGER;

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

                long newStackOffset = Util.alignUp(stackOffset, alignment);
                stackOffset = newStackOffset;

                List<ArgumentClass> classes = info.getClasses();

                if (!classes.isEmpty() && classes.get(0) == ArgumentClass.HFA) {
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

                stackOffset += info.layout().bitsSize() / 8;
            } else {
                // Pass in registers
                long offset = 0;
                for (ArgumentClass c : info.getClasses()) {
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

                        if (c == ArgumentClass.HFA) {
                            // All members in the composite type are passed in
                            // separate vector registers and must be the same
                            // size
                            offset += ((Group)info.layout()).elements()
                                .get(0).bitsSize() / 8;
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
