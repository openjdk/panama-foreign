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
package jdk.internal.foreign.abi;

import jdk.incubator.foreign.MemoryLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The binding operators defined in the Binding class can be combined into argument and return value processing 'recipes'.
 *
 * The binding operators are interpreted using a stack-base interpreter. Operators can either consume operands from the
 * stack, or push them onto the stack.
 *
 * In the description of each binding we talk about 'boxing' and 'unboxing'.
 *  - Unboxing is the process of taking a Java value and decomposing it, and storing components into machine
 *    storage locations. As such, the binding interpreter stack starts with the Java value on it, and should end empty.
 *  - Boxing is the process of re-composing a Java value by pulling components from machine storage locations.
 *    If a MemorySegment is needed to store the result, one should be allocated using the ALLOCATE_BUFFER operator.
 *    The binding interpreter stack starts off empty, and ends with the value to be returned as the only value on it.
 * A binding operator can be interpreted differently based on whether we are boxing or unboxing a value. For example,
 * the CONVERT_ADDRESS operator 'unboxes' a MemoryAddress to a long, but 'boxes' a long to a MemoryAddress.
 *
 * Here are some examples of binding recipes derived from C declarations, and according to the Windows ABI (recipes are
 * ABI-specific). Note that each argument has it's own recipe, which is indicated by '[number]:' (though, the only
 * example that has multiple arguments is the one using varargs).
 *
 * --------------------
 *
 * void f(int i);
 *
 * Argument bindings:
 * 0: MOVE(rcx, int.class) // move an 'int' into the RCX register
 *
 * Return bindings:
 * none
 *
 * --------------------
 *
 * void f(int* i);
 *
 * Argument bindings:
 * 0: CONVERT_ADDRESS // the 'MemoryAddress' is converted into a 'long'
 *    MOVE(rcx, long.class) // the 'long' is moved into the RCX register
 *
 * Return bindings:
 * none
 *
 * --------------------
 *
 * int* f();
 *
 * Argument bindings:
 * none
 *
 * Return bindings:
 * 0: MOVE(rax, long) // load a 'long' from the RAX register
 *    CONVERT_ADDRESS // convert the 'long' into a 'MemoryAddress'
 *
 * --------------------
 *
 * typedef struct { // fits into single register
 *   int x;
 *   int y;
 * } MyStruct;
 *
 * void f(MyStruct ms);
 *
 * Argument bindings:
 * 0: DEREFERENCE(0, long.class) // From the struct's memory region, load a 'long' from offset '0'
 *    MOVE(rcx, long.class) // and copy that into the RCX regitster
 *
 * Return bindings:
 * none
 *
 * --------------------
 *
 * typedef struct { // does not fit into single register
 *   long long x;
 *   long long y;
 * } MyStruct;
 *
 * void f(MyStruct ms);
 *
 * For the Windows ABI:
 *
 * Argument bindings:
 * 0: COPY(16, 8) // copy the memory region containing the struct
 *    BASE_ADDRESS // take the base address of the copy
 *    CONVERT_ADDRESS // converts the base address to a 'long'
 *    MOVE(rcx, long.class) // moves the 'long' into the RCX register
 *
 * Return bindings:
 * none
 *
 * For the SysV ABI:
 *
 * Argument bindings:
 * 0: DUP // duplicates the MemoryRegion operand
 *    DEREFERENCE(0, long.class) // loads a 'long' from offset '0'
 *    MOVE(rdx, long.class) // moves the long into the RDX register
 *    DEREFERENCE(8, long.class) // loads a 'long' from offset '8'
 *    MOVE(rcx, long.class) // moves the long into the RCX register
 *
 * Return bindings:
 * none
 *
 * --------------------
 *
 * typedef struct { // fits into single register
 *   int x;
 *   int y;
 * } MyStruct;
 *
 * MyStruct f();
 *
 * Argument bindings:
 * none
 *
 * Return bindings:
 * 0: ALLOCATE(GroupLayout(C_INT, C_INT)) // allocate a buffer with the memory layout of the struct
 *    DUP // duplicate the allocated buffer
 *    MOVE(rax, long.class) // loads a 'long' from rax
 *    DEREFERENCE(0, long.class) // stores a 'long' at offset 0
 *
 * --------------------
 *
 * typedef struct { // does not fit into single register
 *   long long x;
 *   long long y;
 * } MyStruct;
 *
 * MyStruct f();
 *
 * !! uses synthetic argument, which is a pointer to a pre-allocated buffer
 *
 * Argument bindings:
 * 0: CONVERT_ADDRESS // unbox the MemoryAddress synthetic argument
 *    MOVE(rcx, long.class) // moves the 'long' into the RCX register
 *
 * Return bindings:
 * none
 *
 * --------------------
 *
 * void f(int dummy, ...); // varargs
 *
 * f(0, 10f); // passing a float
 *
 * Argument bindings:
 * 0: MOVE(rcx, int.class) // moves the 'int dummy' into the RCX register
 *
 * 1: DUP // duplicates the '10f' argument
 *    MOVE(rdx, float.class) // move one copy into the RDX register
 *    MOVE(xmm1, float.class) // moves the other copy into the xmm2 register
 *
 * Return bindings:
 * none
 *
 * --------------------
 */
public abstract class Binding {
    enum Tag {
        MOVE,
        DEREFERENCE,
        COPY_BUFFER,
        ALLOC_BUFFER,
        CONVERT_ADDRESS,
        BASE_ADDRESS,
        DUP
    }

    private final Tag tag;

    private Binding(Tag tag) {
        this.tag = tag;
    }

    public Tag tag() {
        return tag;
    }

    private static void checkType(Class<?> type) {
        if (!type.isPrimitive() || type == void.class || type == boolean.class)
            throw new IllegalArgumentException("Illegal type: " + type);
    }

    public static Move move(VMStorage storage, Class<?> type) {
        checkType(type);
        return new Move(storage, type);
    }

    public static Dereference dereference(long offset, Class<?> type) {
        checkType(type);
        if (offset < 0)
            throw new IllegalArgumentException("Negative offset: " + offset);
        return new Dereference(offset, type);
    }

    public static Copy copy(MemoryLayout layout) {
        return new Copy(layout.byteSize(), layout.byteAlignment());
    }

    public static Allocate allocate(MemoryLayout layout) {
        return new Allocate(layout.byteSize(), layout.byteAlignment());
    }

    public static ConvertAddress convertAddress() {
        return ConvertAddress.INSTANCE;
    }

    public static BaseAddress baseAddress() {
        return BaseAddress.INSTANCE;
    }

    public static Dup dup() {
        return Dup.INSTANCE;
    }


    public static Binding.Builder builder() {
        return new Binding.Builder();
    }

    /**
     * A builder helper class for generating lists of Bindings
     */
    public static class Builder {
        private final List<Binding> bindings = new ArrayList<>();

        public Binding.Builder move(VMStorage storage, Class<?> type) {
            bindings.add(Binding.move(storage, type));
            return this;
        }

        public Binding.Builder dereference(long offset, Class<?> type) {
            bindings.add(Binding.dereference(offset, type));
            return this;
        }

        public Binding.Builder copy(MemoryLayout layout) {
            bindings.add(Binding.copy(layout));
            return this;
        }

        public Binding.Builder allocate(MemoryLayout layout) {
            bindings.add(Binding.allocate(layout));
            return this;
        }

        public Binding.Builder convertAddress() {
            bindings.add(Binding.convertAddress());
            return this;
        }

        public Binding.Builder baseAddress() {
            bindings.add(Binding.baseAddress());
            return this;
        }

        public Binding.Builder dup() {
            bindings.add(Binding.dup());
            return this;
        }

        public List<Binding> build() {
            return new ArrayList<>(bindings);
        }
    }

    /**
     * MOVE([storage location], [type])
     *   When unboxing: pops a [type] from the operand stack, and moves it to [storage location]
     *   When boxing: loads a [type] from [storage location], and pushes it onto the operand stack
     * The [type] must be one of byte, short, char, int, long, float, or double
     */
    public static class Move extends Binding {
        private final VMStorage storage;
        private final Class<?> type;

        private Move(VMStorage storage, Class<?> type) {
            super(Tag.MOVE);
            this.storage = storage;
            this.type = type;
        }

        public VMStorage storage() {
            return storage;
        }

        public Class<?> type() {
            return type;
        }

        @Override
        public String toString() {
            return "Move{" +
                    "tag=" + tag() +
                    ", storage=" + storage +
                    ", type=" + type +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move move = (Move) o;
            return storage.equals(move.storage) &&
                    type.equals(move.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag(), storage, type);
        }
    }

    /**
     * DEREFERENCE([offset into memory region], [type])
     *   When unboxing: pops a MemorySegment from the operand stack,
     *     loads a [type] from [offset into memory region] from it, and pushes it onto the operand stack
     *   When boxing: pops a [type], and then a MemorySegment from the operand stack,
     *     and then stores [type] to [offset into memory region] of the MemorySegment
     * The [type] must be one of byte, short, char, int, long, float, or double
     */
    public static class Dereference extends Binding {
        private final long offset;
        private final Class<?> type;

        private Dereference(long offset, Class<?> type) {
            super(Tag.DEREFERENCE);
            this.offset = offset;
            this.type = type;
        }

        public long offset() {
            return offset;
        }

        public Class<?> type() {
            return type;
        }

        @Override
        public String toString() {
            return "Dereference{" +
                    "tag=" + tag() +
                    ", offset=" + offset +
                    ", type=" + type +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Dereference that = (Dereference) o;
            return offset == that.offset &&
                    type.equals(that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag(), offset, type);
        }
    }

    /**
     * COPY([size], [alignment])
     *   Creates a new MemorySegment with the given [size] and [alignment],
     *     and copies contents from a MemorySegment popped from the top of the operand stack into this new buffer,
     *     and pushes the new buffer onto the operand stack
     */
    public static class Copy extends Binding {
        private final long size;
        private final long alignment;

        private Copy(long size, long alignment) {
            super(Tag.COPY_BUFFER);
            this.size = size;
            this.alignment = alignment;
        }

        public long size() {
            return size;
        }

        public long alignment() {
            return alignment;
        }

        @Override
        public String toString() {
            return "Copy{" +
                    "tag=" + tag() +
                    ", size=" + size +
                    ", alignment=" + alignment +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Copy copy = (Copy) o;
            return size == copy.size &&
                    alignment == copy.alignment;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag(), size, alignment);
        }
    }

    /**
     * ALLOCATE([size], [alignment])
     *   Creates a new MemorySegment with the give [size] and [alignment], and pushes it onto the operand stack.
     */
    public static class Allocate extends Binding {
        private final long size;
        private final long alignment;

        private Allocate(long size, long alignment) {
            super(Tag.ALLOC_BUFFER);
            this.size = size;
            this.alignment = alignment;
        }

        public long size() {
            return size;
        }

        public long alignment() {
            return alignment;
        }

        @Override
        public String toString() {
            return "AllocateBuffer{" +
                    "tag=" + tag() +
                    "size=" + size +
                    ", alignment=" + alignment +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Allocate that = (Allocate) o;
            return size == that.size &&
                    alignment == that.alignment;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag(), size, alignment);
        }
    }

    /**
     * CONVERT_ADDRESS()
     *   When unboxing: pops a 'MemoryAddress' from the operand stack, converts it to a 'long',
     *     and pushes that onto the operand stack
     *   When boxing: pops a 'long' from the operand stack, converts it to a 'MemoryAddress',
     *     and pushes that onto the operand stack
     */
    public static class ConvertAddress extends Binding {
        private static final ConvertAddress INSTANCE = new ConvertAddress();
        private ConvertAddress() {
            super(Tag.CONVERT_ADDRESS);
        }

        @Override
        public String toString() {
            return "BoxAddress{" +
                    "tag=" + tag() +
                    "}";
        }

        @Override
        public int hashCode() {
            return tag().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }
    }

    /**
     * BASE_ADDRESS()
     *   Pops a MemorySegment from the operand stack, and takes the base address of the segment
     *   (the MemoryAddress that points to the start), and pushes that onto the operand stack
     */
    public static class BaseAddress extends Binding {
        private static final BaseAddress INSTANCE = new BaseAddress();
        private BaseAddress() {
            super(Tag.BASE_ADDRESS);
        }

        @Override
        public String toString() {
            return "BaseAddress{" +
                    "tag=" + tag() +
                    "}";
        }

        @Override
        public int hashCode() {
            return tag().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }
    }

    /**
     * DUP()
     *   Duplicates the value on the top of the operand stack (without popping it!),
     *   and pushes the duplicate onto the operand stack
     */
    public static class Dup extends Binding {
        private static final Dup INSTANCE = new Dup();
        private Dup() {
            super(Tag.DUP);
        }

        @Override
        public String toString() {
            return "Dup{" +
                    "tag=" + tag() +
                    "}";
        }

        @Override
        public int hashCode() {
            return tag().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }
    }
}
