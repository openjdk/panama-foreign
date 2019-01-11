/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.foreign.Util;
import jdk.internal.foreign.abi.*;
import jdk.internal.foreign.abi.x64.CallingSequenceBuilder;
import jdk.internal.foreign.abi.x64.ArgumentClass;
import jdk.internal.foreign.abi.x64.SharedConstants;

import java.foreign.layout.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static sun.security.action.GetBooleanAction.privilegedGetProperty;

class CallingSequenceBuilderImpl extends CallingSequenceBuilder {
    private static final String[] INTEGER_ARGUMENT_REGISTER_NAMES = { "rcx", "rdx", "r8", "r9" };
    private static final String[] INTEGER_RETURN_REGISTERS_NAMES = { "rax" };
    private static final String[] X87_RETURN_REGISTERS_NAMES = { }; // TODO find out if Windows ABI actually uses these

    private static final boolean DEBUG =
            privilegedGetProperty("jdk.internal.foreign.abi.windows.x64.DEBUG");

    private int curArgIndex = 0;

    private final Argument returned;
    private final List<Argument> arguments = new ArrayList<>();;
    private final Set<Argument> varargs = new HashSet<>();

    public CallingSequenceBuilderImpl(Argument returned) {
        super(INTEGER_ARGUMENT_REGISTER_NAMES, INTEGER_RETURN_REGISTERS_NAMES, X87_RETURN_REGISTERS_NAMES,
                Constants.MAX_VECTOR_ARGUMENT_REGISTERS, Constants.MAX_VECTOR_RETURN_REGISTERS);
        this.returned = returned;
    }

    public void addArgument(Layout type, boolean isVarArg, String name) {
        Argument arg = new Argument(curArgIndex++, type, name);
        arguments.add(arg);
        if(isVarArg) {
            varargs.add(arg);
        }
    }

    static class ArgumentInfo {
        private final List<ArgumentClass> classes;
        private final int nRegs;
        private final boolean inMemory;

        public ArgumentInfo(List<ArgumentClass> classes, int nRegs) {
            this.classes = classes;

            this.inMemory = false;
            this.nRegs = nRegs;
        }

        public ArgumentInfo(int n) {
            this.classes = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                classes.add(ArgumentClass.MEMORY);
            }

            this.inMemory = true;
            this.nRegs = 0;
        }

        public int getRegs() {
            return nRegs;
        }

        public boolean inMemory() {
            return inMemory;
        }

        public List<ArgumentClass> getClasses() {
            return classes;
        }
    }

    static List<ArgumentClass> classifyValueType(Value type) {
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
                if ((type.bitsSize() / 8) > 8) {
                    classes.add(ArgumentClass.X87);
                    classes.add(ArgumentClass.X87UP);
                    return classes;
                } else {
                    classes.add(ArgumentClass.SSE);
                    return classes;
                }
            default:
                throw new IllegalArgumentException("Type " + type + " is not yet supported");
        }
    }

    static boolean isRegisterAggregate(Layout type) {
        // FIXME handle bit size 1, 2, 4
        long size = type.bitsSize() / 8;
        return size == 1
            || size == 2
            || size == 4
            || size == 8;
    }
    
    private List<ArgumentClass> createMemoryClassArray(long n) {
        ArrayList<ArgumentClass> classes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            classes.add(ArgumentClass.MEMORY);
        }

        return classes;
    }

    private List<ArgumentClass> classifyStructType(Group type, boolean isReturn) {
        ArrayList<ArgumentClass> classes = new ArrayList<>();
        
        if(isRegisterAggregate(type)) {
            classes.add(ArgumentClass.INTEGER);
        } else {
            if(isReturn) {
                return createMemoryClassArray(Util.alignUp((type.bitsSize() / 8), 8));
            } else {
                classes.add(ArgumentClass.INTEGER);
            }
        }

        return classes;
    }

    private List<ArgumentClass> classifyType(Layout type, boolean isReturn) {
        if (type instanceof Value) {
            return classifyValueType((Value) type);
        } else if (type instanceof Address) {
            ArrayList<ArgumentClass> classes = new ArrayList<>();
            classes.add(ArgumentClass.INTEGER);
            return classes;
        } else if (type instanceof Sequence) {
            ArrayList<ArgumentClass> classes = new ArrayList<>();
            classes.add(ArgumentClass.INTEGER); // arrrays are always passed as pointers
            return classes;
        } else if (type instanceof Group) {
            return classifyStructType((Group) type, isReturn);
        } else {
            throw new IllegalArgumentException("Unhandled type " + type);
        }
    }

    private ArgumentInfo examineArgument(Layout type, boolean isReturn) {
        List<ArgumentClass> classes = classifyType(type, isReturn);
        if (classes.isEmpty()) {
            return null;
        }

        int nRegs = 0;

        for (ArgumentClass c : classes) {
            switch (c) {
            case INTEGER:
            case SSE:
                nRegs++;
                break;
            case X87:
            case X87UP:
                return new ArgumentInfo(classes.size());
            default:
                break;
            }
        }

        if (nRegs != 0) {
            return new ArgumentInfo(classes, nRegs);
        } else {
            return new ArgumentInfo(classes.size());
        }
    }

    class StorageCalculator {
        private final ArrayList<ArgumentBinding>[] bindings;
        private final boolean forArguments;

        private int nRegs = 0;
        private long stackOffset = 0;

        StorageCalculator(ArrayList<ArgumentBinding>[] bindings, boolean forArguments) {
            this.bindings = bindings;
            this.forArguments = forArguments;
        }

        void addBindings(Argument arg, ArgumentInfo info) {
            if (info.inMemory() ||
                nRegs + info.getRegs() > (forArguments ? Constants.MAX_REGISTER_ARGUMENTS : Constants.MAX_REGISTER_RETURNS)) {
                // stack

                long alignment = Math.max(alignment(arg.getType(), true), 8);

                long newStackOffset = Util.alignUp(stackOffset, alignment);

                // fill holes on stack with nulls
                for (int i = 0; i < (newStackOffset - stackOffset) / 8; i++) {
                    bindings[StorageClass.STACK_ARGUMENT_SLOT.ordinal()].add(null);
                }
                stackOffset = newStackOffset;

                long tmpStackOffset = stackOffset;
                for (int i = 0; i < info.getClasses().size(); i++) {
                    Storage storage = new Storage(StorageClass.STACK_ARGUMENT_SLOT, tmpStackOffset / 8, 8);
                    bindings[StorageClass.STACK_ARGUMENT_SLOT.ordinal()].add(new ArgumentBinding(storage, arg, i * 8));

                    if (DEBUG) {
                        System.out.println("Argument " + arg.getName() + " will be passed on stack at offset " + tmpStackOffset);
                    }

                    tmpStackOffset += 8;
                }

                stackOffset += arg.getType().bitsSize() / 8;
            } else {
                // regs
                for (int i = 0; i < info.getClasses().size(); i++) {
                    Storage storage;

                    ArgumentClass c = info.getClasses().get(i);

                    switch (c) {
                    case INTEGER:
                        storage = new Storage(forArguments ? StorageClass.INTEGER_ARGUMENT_REGISTER : StorageClass.INTEGER_RETURN_REGISTER, nRegs++, SharedConstants.INTEGER_REGISTER_SIZE);
                        bindings[storage.getStorageClass().ordinal()].add(new ArgumentBinding(storage, arg, i * 8));

                        if (DEBUG) {
                            System.out.println("Argument " + arg.getName() + " will be passed in register " + getStorageName(storage));
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

                        storage = new Storage(forArguments ? StorageClass.VECTOR_ARGUMENT_REGISTER : StorageClass.VECTOR_RETURN_REGISTER, nRegs, width);
                        bindings[storage.getStorageClass().ordinal()].add(new ArgumentBinding(storage, arg, i * 8));

                        if (DEBUG) {
                            System.out.println("Argument " + arg.getName() + " will be passed in register " + getStorageName(storage));
                        }

                        if(width == 8 && storage.getStorageClass() == StorageClass.VECTOR_ARGUMENT_REGISTER && varargs.contains(arg)) {
                            Storage extraStorage = new Storage(StorageClass.INTEGER_ARGUMENT_REGISTER, nRegs, SharedConstants.INTEGER_REGISTER_SIZE);
                            bindings[StorageClass.INTEGER_ARGUMENT_REGISTER.ordinal()].add(new ArgumentBinding(extraStorage, arg, i * 8));

                            if (DEBUG) {
                                System.out.println("Argument " + arg.getName() + " will be passed in register " + getStorageName(extraStorage));
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

    private void addBindings(List<Argument> members, StorageCalculator calculator, boolean isReturn) {
        members.stream().forEach(arg -> calculator.addBindings(arg, examineArgument(arg.getType(), isReturn)));
    }

    public CallingSequence build() {
        List<Argument> returns = new ArrayList<>();
        List<Argument> args = this.arguments;
        boolean returnsInMemory = false;

        if (returned != null) {
            Layout returnType = returned.getType();

            returnsInMemory = examineArgument(returnType, true).inMemory();

            // In some cases the return is passed in as first implicit pointer argument, and a corresponding pointer type is returned
            if (returnsInMemory) {
                args = new ArrayList<>();

                Argument returnPointer = new Argument(-1, Address.ofLayout(64, returned.getType()), returned.getName());
                args.add(returnPointer);
                args.addAll(this.arguments);

                returns.add(returnPointer);
            } else {
                returns.add(returned);
            }
        }

        @SuppressWarnings("unchecked")
        ArrayList<ArgumentBinding>[] bindings = (ArrayList<ArgumentBinding>[]) new ArrayList<?>[StorageClass.values().length];

        for (int i = 0; i < StorageClass.values().length; i++) {
            bindings[i] = new ArrayList<>();
        }

        addBindings(args, new StorageCalculator(bindings, true), false);
        addBindings(returns, new StorageCalculator(bindings, false), true);

        return new CallingSequence(this.arguments.size(), bindings, returnsInMemory);
    }
}
