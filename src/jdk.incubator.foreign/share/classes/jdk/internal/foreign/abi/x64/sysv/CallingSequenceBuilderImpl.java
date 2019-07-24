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
package jdk.internal.foreign.abi.x64.sysv;

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
import jdk.internal.foreign.abi.x64.ArgumentClassImpl;
import jdk.internal.foreign.abi.x64.SharedUtils;

import static sun.security.action.GetBooleanAction.privilegedGetProperty;

public class CallingSequenceBuilderImpl extends CallingSequenceBuilder {

    private static final SharedUtils.StorageDebugHelper storageDbgHelper = new SharedUtils.StorageDebugHelper(
            new String[] { "rdi", "rsi", "rdx", "rcx", "r8", "r9" },
            new String[] { "rax", "rdx" },
            new String[] { "st0", "st1" },
            SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS,
            SysVx64ABI.MAX_VECTOR_RETURN_REGISTERS
    );

    private static final boolean DEBUG =
        privilegedGetProperty("jdk.internal.foreign.abi.x64.sysv.DEBUG");

    // The AVX 512 enlightened ABI says "eight eightbytes"
    // Although AMD64 0.99.6 states 4 eightbytes
    private static final int MAX_AGGREGATE_REGS_SIZE = 8;

    private static final ArrayList<ArgumentClassImpl> COMPLEX_X87_CLASSES;

    static {
        COMPLEX_X87_CLASSES = new ArrayList<>();
        COMPLEX_X87_CLASSES.add(ArgumentClassImpl.X87);
        COMPLEX_X87_CLASSES.add(ArgumentClassImpl.X87UP);
        COMPLEX_X87_CLASSES.add(ArgumentClassImpl.X87);
        COMPLEX_X87_CLASSES.add(ArgumentClassImpl.X87UP);
    }

    public CallingSequenceBuilderImpl(MemoryLayout layout) {
        this(layout, new StorageCalculator(false), new StorageCalculator(true));
    }

    private CallingSequenceBuilderImpl(MemoryLayout layout, StorageCalculator retCalculator, StorageCalculator argCalculator) {
        super(MemoryLayouts.SysV.C_POINTER, layout, retCalculator::addBindings, argCalculator::addBindings, argCalculator::addBindings);
    }

    @Override
    protected ArgumentInfo makeArgument(MemoryLayout layout, int pos, String name) {
        return new ArgumentInfo(layout, pos, name);
    }

    static class ArgumentInfo extends Argument {
        private final List<ArgumentClassImpl> classes;

        public ArgumentInfo(MemoryLayout layout, int argumentIndex, String debugName) {
            super(layout, argumentIndex, debugName);
            this.classes = classifyType(layout);
        }

        public int getIntegerRegs() {
            return (int)classes.stream()
                    .filter(cl -> cl == ArgumentClassImpl.INTEGER)
                    .count();
        }

        public int getVectorRegs() {
            return (int)classes.stream()
                    .filter(cl -> cl == ArgumentClassImpl.SSE)
                    .count();
        }

        @Override
        public boolean inMemory() {
            return classes.stream().allMatch(this::isMemoryClass);
        }

        private boolean isMemoryClass(ArgumentClassImpl cl) {
            return cl == ArgumentClassImpl.MEMORY ||
                    (argumentIndex() != -1 &&
                            (cl == ArgumentClassImpl.X87 || cl == ArgumentClassImpl.X87UP));
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
        } else if (clazz == ArgumentClassImpl.X87) {
            classes.add(ArgumentClassImpl.X87UP);
        }

        return classes;
    }

    private static List<ArgumentClassImpl> classifyArrayType(SequenceLayout type) {
        long nWords = Utils.alignUp((type.byteSize()), 8) / 8;
        if (nWords > MAX_AGGREGATE_REGS_SIZE) {
            return createMemoryClassArray(nWords);
        }

        ArrayList<ArgumentClassImpl> classes = new ArrayList<>();

        for (long i = 0; i < nWords; i++) {
            classes.add(ArgumentClassImpl.NO_CLASS);
        }

        long offset = 0;
        final long count = type.elementsCount().getAsLong();
        for (long idx = 0; idx < count; idx++) {
            MemoryLayout t = type.elementLayout();
            offset = SharedUtils.align(t, false, offset);
            List<ArgumentClassImpl> subclasses = classifyType(t);
            if (subclasses.isEmpty()) {
                return classes;
            }

            for (int i = 0; i < subclasses.size(); i++) {
                int pos = (int)(offset / 8);
                ArgumentClassImpl newClass = classes.get(i + pos).merge(subclasses.get(i));
                classes.set(i + pos, newClass);
            }

            offset += t.byteSize();
        }

        for (int i = 0; i < classes.size(); i++) {
            ArgumentClassImpl c = classes.get(i);

            if (c == ArgumentClassImpl.MEMORY) {
                return createMemoryClassArray(classes.size());
            }

            if (c == ArgumentClassImpl.X87UP) {
                if (i == 0) {
                    throw new IllegalArgumentException("Unexpected leading X87UP class");
                }

                if (classes.get(i - 1) != ArgumentClassImpl.X87) {
                    return createMemoryClassArray(classes.size());
                }
            }
        }

        if (classes.size() > 2) {
            if (classes.get(0) != ArgumentClassImpl.SSE) {
                return createMemoryClassArray(classes.size());
            }

            for (int i = 1; i < classes.size(); i++) {
                if (classes.get(i) != ArgumentClassImpl.SSEUP) {
                    return createMemoryClassArray(classes.size());
                }
            }
        }

        return classes;
    }

    // TODO: handle zero length arrays
    // TODO: Handle nested structs (and primitives)
    private static List<ArgumentClassImpl> classifyStructType(GroupLayout type) {
        ArgumentClassImpl clazz = (ArgumentClassImpl)Utils.getAnnotation(type, ArgumentClassImpl.ABI_CLASS);
        if (clazz == ArgumentClassImpl.COMPLEX_X87) {
            return COMPLEX_X87_CLASSES;
        }

        long nWords = Utils.alignUp((type.byteSize()), 8) / 8;
        if (nWords > MAX_AGGREGATE_REGS_SIZE) {
            return createMemoryClassArray(nWords);
        }

        ArrayList<ArgumentClassImpl> classes = new ArrayList<>();

        for (long i = 0; i < nWords; i++) {
            classes.add(ArgumentClassImpl.NO_CLASS);
        }

        long offset = 0;
        final int count = type.memberLayouts().size();
        for (int idx = 0; idx < count; idx++) {
            MemoryLayout t = type.memberLayouts().get(idx);
            if (Utils.isPadding(t)) {
                continue;
            }
            // ignore zero-length array for now
            // TODO: handle zero length arrays here
            if (t instanceof SequenceLayout) {
                if (((SequenceLayout) t).elementsCount().getAsLong() == 0) {
                    continue;
                }
            }
            offset = SharedUtils.align(t, false, offset);
            List<ArgumentClassImpl> subclasses = classifyType(t);
            if (subclasses.isEmpty()) {
                return classes;
            }

            for (int i = 0; i < subclasses.size(); i++) {
                int pos = (int)(offset / 8);
                ArgumentClassImpl newClass = classes.get(i + pos).merge(subclasses.get(i));
                classes.set(i + pos, newClass);
            }

            // TODO: validate union strategy is sound
            if (type.isStruct()) {
                offset += t.byteSize();
            }
        }

        for (int i = 0; i < classes.size(); i++) {
            ArgumentClassImpl c = classes.get(i);

            if (c == ArgumentClassImpl.MEMORY) {
                return createMemoryClassArray(classes.size());
            }

            if (c == ArgumentClassImpl.X87UP) {
                if (i == 0) {
                    throw new IllegalArgumentException("Unexpected leading X87UP class");
                }

                if (classes.get(i - 1) != ArgumentClassImpl.X87) {
                    return createMemoryClassArray(classes.size());
                }
            }
        }

        if (classes.size() > 2) {
            if (classes.get(0) != ArgumentClassImpl.SSE) {
                return createMemoryClassArray(classes.size());
            }

            for (int i = 1; i < classes.size(); i++) {
                if (classes.get(i) != ArgumentClassImpl.SSEUP) {
                    return createMemoryClassArray(classes.size());
                }
            }
        }

        return classes;
    }

    private static List<ArgumentClassImpl> classifyType(MemoryLayout type) {
        try {
            if (type instanceof ValueLayout) {
                return classifyValueType((ValueLayout) type);
            } else if (type instanceof SequenceLayout) {
                return classifyArrayType((SequenceLayout) type);
            } else if (type instanceof GroupLayout) {
                return classifyStructType((GroupLayout) type);
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
        private int nX87Regs = 0;
        private long stackOffset = 0;

        StorageCalculator(boolean forArguments) {
            this.forArguments = forArguments;
        }

        public void addBindings(Argument arg, BiConsumer<StorageClass, ArgumentBinding> bindingConsumer) {
            ArgumentInfo info = (ArgumentInfo)arg;
            if (info.inMemory() ||
                    nIntegerRegs + info.getIntegerRegs() > (forArguments ? SysVx64ABI.MAX_INTEGER_ARGUMENT_REGISTERS : SysVx64ABI.MAX_INTEGER_RETURN_REGISTERS) ||
                    nVectorRegs + info.getVectorRegs() > (forArguments ? SysVx64ABI.MAX_VECTOR_ARGUMENT_REGISTERS : SysVx64ABI.MAX_VECTOR_RETURN_REGISTERS)) {
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

                    ArgumentClassImpl c = info.getClasses().get(i);

                    switch (c) {
                    case INTEGER:
                        storage = new Storage(forArguments ? StorageClass.INTEGER_ARGUMENT_REGISTER : StorageClass.INTEGER_RETURN_REGISTER, nIntegerRegs++, SharedUtils.INTEGER_REGISTER_SIZE);
                        bindingConsumer.accept(storage.getStorageClass(), new ArgumentBinding(storage, info, i * 8));

                        if (DEBUG) {
                            System.out.println("Argument " + info.name() + " will be passed in register " +
                                    storageDbgHelper.getStorageName(storage));
                        }
                        break;

                    case SSE: {
                        int width = 8;

                        for (int j = i + 1; j < info.getClasses().size(); j++) {
                            if (info.getClasses().get(j) == ArgumentClassImpl.SSEUP) {
                                width += 8;
                            }
                        }

                        if (width > 64) {
                            throw new IllegalArgumentException((width * 8) + "-bit vector arguments not supported");
                        }

                        storage = new Storage(forArguments ? StorageClass.VECTOR_ARGUMENT_REGISTER : StorageClass.VECTOR_RETURN_REGISTER,
                                nVectorRegs++, width, SharedUtils.VECTOR_REGISTER_SIZE);

                        bindingConsumer.accept(storage.getStorageClass(), new ArgumentBinding(storage, info, i * 8));

                        if (DEBUG) {
                            System.out.println("Argument " + info.name() + " will be passed in register " +
                                    storageDbgHelper.getStorageName(storage));
                        }
                        break;
                    }

                    case SSEUP:
                        break;

                    case X87: {
                        int width = 8;

                        if (i < info.getClasses().size() && info.getClasses().get(i + 1) == ArgumentClassImpl.X87UP) {
                            width += 8;
                        }

                        assert !forArguments;

                        storage = new Storage(StorageClass.X87_RETURN_REGISTER, nX87Regs++, width, SharedUtils.X87_REGISTER_SIZE);
                        bindingConsumer.accept(storage.getStorageClass(), new ArgumentBinding(storage, info, i * 8));

                        if (DEBUG) {
                            System.out.println("Argument " + info.name() + " will be passed in register " +
                                    storageDbgHelper.getStorageName(storage));
                        }
                        break;
                    }

                    case X87UP:
                        break;

                    default:
                        throw new UnsupportedOperationException("Unhandled class " + c);
                    }
                }
            }
        }
    }
}
