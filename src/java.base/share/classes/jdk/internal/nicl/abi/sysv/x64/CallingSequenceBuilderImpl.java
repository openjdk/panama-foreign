/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.nicl.abi.sysv.x64;

import jdk.internal.nicl.Argument;
import jdk.internal.nicl.Platform;
import jdk.internal.nicl.Util;
import jdk.internal.nicl.abi.AbstractCallingSequenceBuilderImpl;
import jdk.internal.nicl.abi.ArgumentBinding;
import jdk.internal.nicl.abi.CallingSequence;
import jdk.internal.nicl.abi.Storage;
import jdk.internal.nicl.abi.StorageClass;
import jdk.internal.nicl.abi.SystemABI;
import jdk.internal.nicl.types.Array;
import jdk.internal.nicl.types.BitFields;
import jdk.internal.nicl.types.Container;
import jdk.internal.nicl.types.Pointer;
import jdk.internal.nicl.types.Scalar;
import jdk.internal.nicl.types.Type;
import java.util.ArrayList;

public class CallingSequenceBuilderImpl extends AbstractCallingSequenceBuilderImpl {
    private static final boolean DEBUG = Boolean.getBoolean("jdk.internal.nicl.abi.sysv.x64.DEBUG");

    // The AVX 512 enlightened ABI says "eight eightbytes"
    private static final int MAX_AGGREGATE_REGS_SIZE = 8;

    public CallingSequenceBuilderImpl(Argument returned, ArrayList<Argument> arguments) {
        super(returned, arguments);
    }

    static class ArgumentInfo {
        private final ArrayList<ArgumentClass> classes;
        private final int nIntegerRegs, nVectorRegs;
        private final boolean inMemory;

        public ArgumentInfo(ArrayList<ArgumentClass> classes, int nIntegerRegs, int nVectorRegs) {
            this.classes = classes;

            this.nIntegerRegs = nIntegerRegs;
            this.nVectorRegs = nVectorRegs;

            this.inMemory = false;
        }

        public ArgumentInfo(int n) {
            this.classes = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                classes.add(ArgumentClass.MEMORY);
            }

            this.inMemory = true;
            this.nIntegerRegs = 0;
            this.nVectorRegs = 0;
        }

        public int getIntegerRegs() {
            return nIntegerRegs;
        }

        public int getVectorRegs() {
            return nVectorRegs;
        }

        public boolean inMemory() {
            return inMemory;
        }

        public ArrayList<ArgumentClass> getClasses() {
            return classes;
        }
    }

    private ArrayList<ArgumentClass> classifyScalarType(Scalar type, boolean named) {
        ArrayList<ArgumentClass> classes = new ArrayList<>();

        switch (type.typeCode()) {
            case 'e':
            case 'E':
                classes.add(ArgumentClass.X87);
                classes.add(ArgumentClass.X87UP);
                return classes;

            case 'i':
            case 'I':
                if (type.getSize() > 16) {
                    throw new UnsupportedOperationException("Type " + type + " not supported");
                }

                classes.add(ArgumentClass.INTEGER);
                // int128
                if (type.getSize() > 8) {
                    classes.add(ArgumentClass.INTEGER);
                }
                return classes;

            case 'o':
            case 'O':
            case 's':
            case 'S':
            case 'l':
            case 'L':
            case 'q':
            case 'Q':
            case 'B':
            case 'c':
                classes.add(ArgumentClass.INTEGER);
                return classes;

            case 'f':
            case 'F':
            case 'd':
            case 'D':
                classes.add(ArgumentClass.SSE);
                return classes;

            case 'v':
                // unnamed (varargs) __m256 and __m512 arguments are passed in MEMORY
                if (type.getSize() > 16 && !named) {
                    for (int i = 0; i < type.getSize(); i += 8) {
                        classes.add(ArgumentClass.MEMORY);
                    }
                    return classes;
                }

                if (type.getSize() > 64) {
                    throw new IllegalArgumentException((type.getSize() * 8) + "-bit vector types not supported");
                }

                classes.add(ArgumentClass.SSE);
                if (type.getSize() <= 8) {
                    return classes;
                }

                for (int i = 8; i < type.getSize(); i += 8) {
                    classes.add(ArgumentClass.SSEUP);
                }

                return classes;
            default:
                throw new IllegalArgumentException("Type " + type.typeCode() + " is not yet supported");
        }
    }

    private ArrayList<ArgumentClass> classifyArrayType(Array type, boolean named) {
        SystemABI abi = Platform.getInstance().getABI();

        long nWords = Util.alignUp(type.getSize(), 8) / 8;
        if (nWords > MAX_AGGREGATE_REGS_SIZE) {
            return createMemoryClassArray(nWords);
        }

        ArrayList<ArgumentClass> classes = new ArrayList<>();

        for (long i = 0; i < nWords; i++) {
            classes.add(ArgumentClass.NO_CLASS);
        }

        long offset = 0;
        final long count = type.getOccurrence();
        for (long idx = 0; idx < count; idx++) {
            Type t = type.getElementType();
            offset = abi.align(t, false, offset);
            ArrayList<ArgumentClass> subclasses = classifyType(t, named);
            if (subclasses.isEmpty()) {
                return classes;
            }

            for (int i = 0; i < subclasses.size(); i++) {
                int pos = (int)(offset / 8);
                ArgumentClass newClass = classes.get(i + pos).merge(subclasses.get(i));
                classes.set(i + pos, newClass);
            }

            offset += t.getSize();
        }

        for (int i = 0; i < classes.size(); i++) {
            ArgumentClass c = classes.get(i);

            if (c == ArgumentClass.MEMORY) {
                return createMemoryClassArray(classes.size());
            }

            if (c == ArgumentClass.X87UP) {
                if (i == 0) {
                    throw new IllegalArgumentException("Unexpected leading X87UP class");
                }

                if (classes.get(i - 1) != ArgumentClass.X87) {
                    return createMemoryClassArray(classes.size());
                }
            }
        }

        if (classes.size() > 2) {
            if (classes.get(0) != ArgumentClass.SSE) {
                return createMemoryClassArray(classes.size());
            }

            for (int i = 1; i < classes.size(); i++) {
                if (classes.get(i) != ArgumentClass.SSEUP) {
                    return createMemoryClassArray(classes.size());
                }
            }
        }

        return classes;
    }

    // TODO: handle zero length arrays
    // TODO: Handle nested structs (and primitives)
    private ArrayList<ArgumentClass> classifyStructType(Container type, boolean named) {
        if (type.isUnion()) {
            // TODO: how to deal with union?
            throw new UnsupportedOperationException("Union is not yet supported.");
        }

        SystemABI abi = Platform.getInstance().getABI();

        long nWords = Util.alignUp(type.getSize(), 8) / 8;
        if (nWords > MAX_AGGREGATE_REGS_SIZE) {
            return createMemoryClassArray(nWords);
        }

        ArrayList<ArgumentClass> classes = new ArrayList<>();

        for (long i = 0; i < nWords; i++) {
            classes.add(ArgumentClass.NO_CLASS);
        }

        // TODO: handle zero length arrays here

        long offset = 0;
        final int count = type.memberCount();
        for (int idx = 0; idx < count; idx++) {
            Type t = type.getMember(idx);
            offset = abi.align(t, false, offset);
            ArrayList<ArgumentClass> subclasses = classifyType(t, named);
            if (subclasses.isEmpty()) {
                return classes;
            }

            for (int i = 0; i < subclasses.size(); i++) {
                int pos = (int)(offset / 8);
                ArgumentClass newClass = classes.get(i + pos).merge(subclasses.get(i));
                classes.set(i + pos, newClass);
            }

            offset += t.getSize();
        }

        for (int i = 0; i < classes.size(); i++) {
            ArgumentClass c = classes.get(i);

            if (c == ArgumentClass.MEMORY) {
                return createMemoryClassArray(classes.size());
            }

            if (c == ArgumentClass.X87UP) {
                if (i == 0) {
                    throw new IllegalArgumentException("Unexpected leading X87UP class");
                }

                if (classes.get(i - 1) != ArgumentClass.X87) {
                    return createMemoryClassArray(classes.size());
                }
            }
        }

        if (classes.size() > 2) {
            if (classes.get(0) != ArgumentClass.SSE) {
                return createMemoryClassArray(classes.size());
            }

            for (int i = 1; i < classes.size(); i++) {
                if (classes.get(i) != ArgumentClass.SSEUP) {
                    return createMemoryClassArray(classes.size());
                }
            }
        }

        return classes;
    }

    private ArrayList<ArgumentClass> classifyType(Type type, boolean named) {
        if (type instanceof Scalar) {
            return classifyScalarType((Scalar) type, named);
        } else if (type instanceof Pointer) {
            ArrayList<ArgumentClass> classes = new ArrayList<>();
            classes.add(ArgumentClass.INTEGER);
            return classes;
        } else if (type instanceof Array) {
            return classifyArrayType((Array) type, named);
        } else if (type instanceof Container) {
            return classifyStructType((Container) type, named);
        } else if (type instanceof BitFields) {
            return classifyScalarType(((BitFields) type).getStorage(), named);
        } else {
            throw new IllegalArgumentException("Unhandled type " + type);
        }
    }

    private ArrayList<ArgumentClass> createMemoryClassArray(long n) {
        ArrayList<ArgumentClass> classes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            classes.add(ArgumentClass.MEMORY);
        }

        return classes;
    }

    private ArgumentInfo examineArgument(Type type, boolean named) {
        ArrayList<ArgumentClass> classes = classifyType(type, named);
        if (classes.isEmpty()) {
            return null;
        }

        int nIntegerRegs = 0;
        int nVectorRegs = 0;

        for (ArgumentClass c : classes) {
            switch (c) {
            case INTEGER:
                nIntegerRegs++;
                break;
            case SSE:
                nVectorRegs++;
                break;
            case X87:
            case X87UP:
                return new ArgumentInfo(classes.size());
            default:
                break;
            }
        }

        if (nIntegerRegs != 0 || nVectorRegs != 0) {
            return new ArgumentInfo(classes, nIntegerRegs, nVectorRegs);
        } else {
            return new ArgumentInfo(classes.size());
        }
    }

    class StorageCalculator {
        private final ArrayList<ArgumentBinding>[] bindings;
        private final boolean forArguments;

        private int nIntegerRegs = 0;
        private int nVectorRegs = 0;
        private long stackOffset = 0;

        StorageCalculator(ArrayList<ArgumentBinding>[] bindings, boolean forArguments) {
            this.bindings = bindings;
            this.forArguments = forArguments;
        }

        void addBindings(Argument arg, ArgumentInfo info) {
            if (info.inMemory() ||
                    nIntegerRegs + info.getIntegerRegs() > (forArguments ? Constants.MAX_INTEGER_ARGUMENT_REGISTERS : Constants.MAX_INTEGER_RETURN_REGISTERS) ||
                    nVectorRegs + info.getVectorRegs() > (forArguments ? Constants.MAX_VECTOR_ARGUMENT_REGISTERS : Constants.MAX_VECTOR_RETURN_REGISTERS)) {
                // stack

                long alignment = Math.max(Platform.getInstance().getABI().alignment(arg.getType(), true), 8);

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

                stackOffset += arg.getType().getSize();
            } else {
                // regs
                for (int i = 0; i < info.getClasses().size(); i++) {
                    Storage storage;

                    ArgumentClass c = info.getClasses().get(i);

                    switch (c) {
                    case INTEGER:
                        storage = new Storage(forArguments ? StorageClass.INTEGER_ARGUMENT_REGISTER : StorageClass.INTEGER_RETURN_REGISTER, nIntegerRegs++, Constants.INTEGER_REGISTER_SIZE);
                        bindings[storage.getStorageClass().ordinal()].add(new ArgumentBinding(storage, arg, i * 8));

                        if (DEBUG) {
                            System.out.println("Argument " + arg.getName() + " will be passed in register " + StorageNames.getStorageName(storage));
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

                        storage = new Storage(forArguments ? StorageClass.VECTOR_ARGUMENT_REGISTER : StorageClass.VECTOR_RETURN_REGISTER, nVectorRegs++, width);
                        bindings[storage.getStorageClass().ordinal()].add(new ArgumentBinding(storage, arg, i * 8));

                        if (DEBUG) {
                            System.out.println("Argument " + arg.getName() + " will be passed in register " + StorageNames.getStorageName(storage));
                        }
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

    private void addBindings(ArrayList<Argument> members, StorageCalculator calculator) {
        members.stream().forEach(arg -> calculator.addBindings(arg, examineArgument(arg.getType(), arg.isNamed())));
    }

    @Override
    public CallingSequence build() {
        ArrayList<Argument> returns = new ArrayList<>();
        ArrayList<Argument> args = this.arguments;
        boolean returnsInMemory = false;

        if (returned != null) {
            Type returnType = returned.getType();

            returnsInMemory = examineArgument(returnType, returned.isNamed()).inMemory();

            // In some cases the return is passed in as first implicit pointer argument, and a corresponding pointer type is returned
            if (returnsInMemory) {
                args = new ArrayList<>();

                Argument returnPointer = new Argument(-1, new Pointer(returned.getType()), returned.getName());
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

        addBindings(args, new StorageCalculator(bindings, true));
        addBindings(returns, new StorageCalculator(bindings, false));

        return new CallingSequence(bindings, returnsInMemory);
    }
}
