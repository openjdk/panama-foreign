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

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;
import jdk.internal.foreign.MemoryAddressImpl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.invoke.MethodHandles.collectArguments;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.permuteArguments;
import static java.lang.invoke.MethodType.methodType;

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
 *    MOVE(rcx, long.class) // and copy that into the RCX register
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
    private static final MethodHandle MH_UNBOX_ADDRESS;
    private static final MethodHandle MH_BOX_ADDRESS;
    private static final MethodHandle MH_BASE_ADDRESS;
    private static final MethodHandle MH_COPY_BUFFER;
    private static final MethodHandle MH_ALLOCATE_BUFFER;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MH_UNBOX_ADDRESS = lookup.findVirtual(MemoryAddress.class, "toRawLongValue",
                    methodType(long.class));
            MH_BOX_ADDRESS = lookup.findStatic(MemoryAddress.class, "ofLong",
                    methodType(MemoryAddress.class, long.class));
            MH_BASE_ADDRESS = lookup.findVirtual(MemorySegment.class, "address",
                    methodType(MemoryAddress.class));
            MH_COPY_BUFFER = lookup.findStatic(Binding.class, "copyBuffer",
                    methodType(MemorySegment.class, MemorySegment.class, long.class, long.class, NativeScope.class));
            MH_ALLOCATE_BUFFER = lookup.findStatic(MemorySegment.class, "allocateNative",
                    methodType(MemorySegment.class, long.class, long.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

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

    public abstract void verifyUnbox(Deque<Class<?>> stack);
    public abstract void verifyBox(Deque<Class<?>> stack);

    public abstract void unbox(Deque<Object> stack, BindingInterpreter.StoreFunc storeFunc, NativeScope scope);
    public abstract void box(Deque<Object> stack, BindingInterpreter.LoadFunc loadFunc);

    public abstract MethodHandle specializeUnbox(MethodHandle specializedHandle, int insertPos);
    public abstract MethodHandle specializeBox(MethodHandle returnFilter);

    private static MethodHandle mergeArguments(MethodHandle mh, int sourceIndex, int destIndex) {
        MethodType oldType = mh.type();
        Class<?> sourceType = oldType.parameterType(sourceIndex);
        Class<?> destType = oldType.parameterType(destIndex);
        if (sourceType != destType) {
            // TODO meet?
            throw new IllegalArgumentException("Parameter types differ: " + sourceType + " != " + destType);
        }
        MethodType newType = oldType.dropParameterTypes(destIndex, destIndex + 1);
        int[] reorder = new int[oldType.parameterCount()];
        assert destIndex > sourceIndex;
        for (int i = 0, index = 0; i < reorder.length; i++) {
            if (i != destIndex) {
                reorder[i] = index++;
            } else {
                reorder[i] = sourceIndex;
            }
        }
        return permuteArguments(mh, newType, reorder);
    }

    private static void checkType(Class<?> type) {
        if (!type.isPrimitive() || type == void.class || type == boolean.class)
            throw new IllegalArgumentException("Illegal type: " + type);
    }

    private static MemorySegment copyBuffer(MemorySegment operand, long size, long alignment, NativeScope allocator) {
        MemorySegment copy = allocator.allocate(size, alignment);
        copy.copyFrom(operand.asSlice(0, size));
        return copy;
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

        @Override
        public void verifyUnbox(Deque<Class<?>> stack) {
            Class<?> actualType = stack.pop();
            Class<?> expectedType = type;
            SharedUtils.checkType(actualType, expectedType);
        }

        @Override
        public void verifyBox(Deque<Class<?>> stack) {
            stack.push(type);
        }

        @Override
        public void unbox(Deque<Object> stack, BindingInterpreter.StoreFunc storeFunc, NativeScope scope) {
            storeFunc.store(storage, type, stack.pop());
        }

        @Override
        public void box(Deque<Object> stack, BindingInterpreter.LoadFunc loadFunc) {
            stack.push(loadFunc.load(storage, type));
        }

        @Override
        public MethodHandle specializeUnbox(MethodHandle specializedHandle, int insertPos) {
            return specializedHandle; // no-op
        }

        @Override
        public MethodHandle specializeBox(MethodHandle returnFilter) {
            return returnFilter; // no-op
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

        @Override
        public void verifyUnbox(Deque<Class<?>> stack) {
            Class<?> actualType = stack.pop();
            SharedUtils.checkType(actualType, MemorySegment.class);
            Class<?> newType = type;
            stack.push(newType);
        }

        @Override
        public void verifyBox(Deque<Class<?>> stack) {
            Class<?> storeType = stack.pop();
            SharedUtils.checkType(storeType, type);
            Class<?> segmentType = stack.pop();
            SharedUtils.checkType(segmentType, MemorySegment.class);
        }

        @Override
        public void unbox(Deque<Object> stack, BindingInterpreter.StoreFunc storeFunc, NativeScope scope) {
            MemorySegment operand = (MemorySegment) stack.pop();
            MemorySegment readAddress = operand.asSlice(offset);
            stack.push(SharedUtils.read(readAddress, type));
        }

        @Override
        public void box(Deque<Object> stack, BindingInterpreter.LoadFunc loadFunc) {
            Object value = stack.pop();
            MemorySegment operand = (MemorySegment) stack.pop();
            MemorySegment writeAddress = operand.asSlice(offset);
            SharedUtils.write(writeAddress, type, value);
        }

        private VarHandle varHandle() {
            return MemoryHandles.insertCoordinates(MemoryHandles.varHandle(type, ByteOrder.nativeOrder()), 1, offset);
        }

        @Override
        public MethodHandle specializeUnbox(MethodHandle specializedHandle, int insertPos) {
            MethodHandle filter = varHandle()
                    .toMethodHandle(VarHandle.AccessMode.GET)
                    .asType(methodType(type, MemorySegment.class));
            return filterArguments(specializedHandle, insertPos, filter);
        }

        @Override
        public MethodHandle specializeBox(MethodHandle returnFilter) {
            MethodHandle setter = varHandle().toMethodHandle(VarHandle.AccessMode.SET);
            setter = setter.asType(methodType(void.class, MemorySegment.class, type));
            return collectArguments(returnFilter, returnFilter.type().parameterCount(), setter);
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

        @Override
        public void verifyUnbox(Deque<Class<?>> stack) {
            Class<?> actualType = stack.pop();
            SharedUtils.checkType(actualType, MemorySegment.class);
            stack.push(MemorySegment.class);
        }

        @Override
        public void verifyBox(Deque<Class<?>> stack) {
            Class<?> actualType = stack.pop();
            SharedUtils.checkType(actualType, MemoryAddress.class);
            stack.push(MemorySegment.class);
        }

        @Override
        public void unbox(Deque<Object> stack, BindingInterpreter.StoreFunc storeFunc, NativeScope scope) {
            MemorySegment operand = (MemorySegment) stack.pop();
            MemorySegment copy = scope.allocate(size, alignment);
            copy.copyFrom(operand.asSlice(0, size));
            stack.push(copy);
        }

        @Override
        public void box(Deque<Object> stack, BindingInterpreter.LoadFunc loadFunc) {
            MemoryAddress operand = (MemoryAddress) stack.pop();
            MemorySegment segment = MemoryAddressImpl.ofLongUnchecked(operand.toRawLongValue(), size);
            MemorySegment copy = MemorySegment.allocateNative(size, alignment);
            copy.copyFrom(segment);
            stack.push(copy); // leaked
        }

        @Override
        public MethodHandle specializeUnbox(MethodHandle specializedHandle, int insertPos) {
            MethodHandle filter = insertArguments(MH_COPY_BUFFER, 1, size, alignment);
            specializedHandle = collectArguments(specializedHandle, insertPos, filter);
            return mergeArguments(specializedHandle, 0, insertPos + 1);
        }

        @Override
        public MethodHandle specializeBox(MethodHandle returnFilter) {
            throw new UnsupportedOperationException();
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

        @Override
        public void verifyUnbox(Deque<Class<?>> stack) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void verifyBox(Deque<Class<?>> stack) {
            stack.push(MemorySegment.class);
        }

        @Override
        public void unbox(Deque<Object> stack, BindingInterpreter.StoreFunc storeFunc, NativeScope scope) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void box(Deque<Object> stack, BindingInterpreter.LoadFunc loadFunc) {
            stack.push(MemorySegment.allocateNative(size, alignment));
        }

        @Override
        public MethodHandle specializeUnbox(MethodHandle specializedHandle, int insertPos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MethodHandle specializeBox(MethodHandle returnFilter) {
            return collectArguments(returnFilter, 0, insertArguments(MH_ALLOCATE_BUFFER, 0, size, alignment));
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

        @Override
        public void verifyUnbox(Deque<Class<?>> stack) {
            Class<?> actualType = stack.pop();
            SharedUtils.checkType(actualType, MemoryAddress.class);
            stack.push(long.class);
        }

        @Override
        public void verifyBox(Deque<Class<?>> stack) {
            Class<?> actualType = stack.pop();
            SharedUtils.checkType(actualType, long.class);
            stack.push(MemoryAddress.class);
        }

        @Override
        public void unbox(Deque<Object> stack, BindingInterpreter.StoreFunc storeFunc, NativeScope scope) {
            stack.push(((MemoryAddress)stack.pop()).toRawLongValue());
        }

        @Override
        public void box(Deque<Object> stack, BindingInterpreter.LoadFunc loadFunc) {
            stack.push(MemoryAddress.ofLong((long) stack.pop()));
        }

        @Override
        public MethodHandle specializeUnbox(MethodHandle specializedHandle, int insertPos) {
            return filterArguments(specializedHandle, insertPos, MH_UNBOX_ADDRESS);
        }

        @Override
        public MethodHandle specializeBox(MethodHandle returnFilter) {
            return filterArguments(returnFilter, 0, MH_BOX_ADDRESS);
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

        @Override
        public void verifyUnbox(Deque<Class<?>> stack) {
            Class<?> actualType = stack.pop();
            SharedUtils.checkType(actualType, MemorySegment.class);
            stack.push(MemoryAddress.class);
        }

        @Override
        public void verifyBox(Deque<Class<?>> stack) {
            Class<?> actualType = stack.pop();
            SharedUtils.checkType(actualType, MemorySegment.class);
            stack.push(MemoryAddress.class);
        }

        @Override
        public void unbox(Deque<Object> stack, BindingInterpreter.StoreFunc storeFunc, NativeScope scope) {
            stack.push(((MemorySegment) stack.pop()).address());
        }

        @Override
        public void box(Deque<Object> stack, BindingInterpreter.LoadFunc loadFunc) {
            stack.push(((MemorySegment) stack.pop()).address());
        }

        @Override
        public MethodHandle specializeUnbox(MethodHandle specializedHandle, int insertPos) {
            return filterArguments(specializedHandle, insertPos, MH_BASE_ADDRESS);
        }

        @Override
        public MethodHandle specializeBox(MethodHandle returnFilter) {
            throw new UnsupportedOperationException();
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

        @Override
        public void verifyUnbox(Deque<Class<?>> stack) {
            stack.push(stack.peekLast());
        }

        @Override
        public void verifyBox(Deque<Class<?>> stack) {
            stack.push(stack.peekLast());
        }

        @Override
        public void unbox(Deque<Object> stack, BindingInterpreter.StoreFunc storeFunc, NativeScope scope) {
            stack.push(stack.peekLast());
        }

        @Override
        public void box(Deque<Object> stack, BindingInterpreter.LoadFunc loadFunc) {
            stack.push(stack.peekLast());
        }

        /*
         * Fixes up Y-shaped data graphs (produced by DEREFERENCE):
         *
         * 1. DUP()
         * 2. DEREFERENCE(0, int.class)
         * 3. MOVE  (ignored)
         * 4. DEREFERENCE(4, int.class)
         * 5. MOVE  (ignored)
         *
         * (specialized in reverse!)
         *
         * 5. (int, int) -> void                       insertPos = 1
         * 4. (MemorySegment, int) -> void             insertPos = 1
         * 3. (MemorySegment, int) -> void             insertPos = 0
         * 2. (MemorySegment, MemorySegment) -> void   insertPos = 0
         * 1. (MemorySegment) -> void                  insertPos = 0
         *
         */
        @Override
        public MethodHandle specializeUnbox(MethodHandle specializedHandle, int insertPos) {
            return mergeArguments(specializedHandle, insertPos, insertPos + 1);
        }

        /*
         * Fixes up Y-shaped data graphs (produced by DEREFERENCE):
         *
         * 1. ALLOCATE_BUFFER(4, 4)
         * 2. DUP
         * 3. MOVE  (ignored)
         * 4. DEREFERNCE(0, int.class)
         *
         * (specialized in reverse!)
         *
         * input: (MemorySegment) -> MemorySegment (identity function of high-level return)
         * 4. (MemorySegment, MemorySegment, int) -> MemorySegment
         * 3. (MemorySegment, MemorySegment, int) -> MemorySegment
         * 2. (MemorySegment, int) -> MemorySegment
         * 1. (int) -> MemorySegment
         *
         */
        @Override
        public MethodHandle specializeBox(MethodHandle returnFilter) {
            // assumes shape like: (MS, ..., MS, T) R
            return mergeArguments(returnFilter, 0, returnFilter.type().parameterCount() - 2);
        }
    }
}
