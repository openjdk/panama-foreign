/*
 *  Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */
package jdk.internal.foreign.abi;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.Utils;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

public class BindingInterpreter {
    private static final VarHandle VH_BYTE = MemoryHandles.varHandle(byte.class, ByteOrder.nativeOrder());
    private static final VarHandle VH_CHAR = MemoryHandles.varHandle(char.class, ByteOrder.nativeOrder());
    private static final VarHandle VH_SHORT = MemoryHandles.varHandle(short.class, ByteOrder.nativeOrder());
    private static final VarHandle VH_INT = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());
    private static final VarHandle VH_LONG = MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder());
    private static final VarHandle VH_FLOAT = MemoryHandles.varHandle(float.class, ByteOrder.nativeOrder());
    private static final VarHandle VH_DOUBLE = MemoryHandles.varHandle(double.class, ByteOrder.nativeOrder());

    static void unbox(Object arg, List<Binding> bindings, Function<VMStorage,
            MemoryAddress> ptrFunction, List<? super MemorySegment> buffers) {
        Deque<Object> stack = new ArrayDeque<>();
        stack.push(arg);
        for (Binding b : bindings) {
            switch (b.tag()) {
                case MOVE -> {
                    Binding.Move binding = (Binding.Move) b;
                    MemoryAddress ptr = ptrFunction.apply(binding.storage());
                    writeOverSized(ptr, binding.type(), stack.pop());
                }
                case DEREFERENCE -> {
                    Binding.Dereference deref = (Binding.Dereference) b;
                    MemorySegment operand = (MemorySegment) stack.pop();
                    MemoryAddress baseAddress = operand.baseAddress();
                    MemoryAddress readAddress = baseAddress.addOffset(deref.offset());
                    stack.push(read(readAddress, deref.type()));
                }
                case COPY_BUFFER -> {
                    Binding.Copy binding = (Binding.Copy) b;
                    MemorySegment operand = (MemorySegment) stack.pop();
                    assert operand.byteSize() == binding.size() : "operand size mismatch";
                    MemorySegment copy = MemorySegment.allocateNative(binding.size(), binding.alignment());
                    MemoryAddress.copy(operand.baseAddress(), copy.baseAddress(), binding.size());
                    buffers.add(copy);
                    stack.push(copy);
                }
                case ALLOC_BUFFER ->
                    throw new UnsupportedOperationException();
                case CONVERT_ADDRESS ->
                    stack.push(MemoryAddressImpl.addressof((MemoryAddress) stack.pop()));
                case BASE_ADDRESS ->
                    stack.push(((MemorySegment) stack.pop()).baseAddress());
                case DUP ->
                    stack.push(stack.peekLast());
                default -> throw new IllegalArgumentException("Unsupported tag: " + b);
            }
        }
    }

    static Object box(List<Binding> bindings, Function<VMStorage, MemoryAddress> ptrFunction) {
        Deque<Object> stack = new ArrayDeque<>();
        for (Binding b : bindings) {
            switch (b.tag()) {
                case MOVE -> {
                    Binding.Move binding = (Binding.Move) b;
                    MemoryAddress ptr = ptrFunction.apply(binding.storage());
                    stack.push(read(ptr, binding.type()));
                }
                case DEREFERENCE -> {
                    Binding.Dereference binding = (Binding.Dereference) b;
                    Object value = stack.pop();
                    MemorySegment operand = (MemorySegment) stack.pop();
                    MemoryAddress baseAddress = operand.baseAddress();
                    MemoryAddress writeAddress = baseAddress.addOffset(binding.offset());
                    write(writeAddress, binding.type(), value);
                }
                case COPY_BUFFER -> {
                    Binding.Copy binding = (Binding.Copy) b;
                    MemoryAddress operand = (MemoryAddress) stack.pop();
                    operand = Utils.resizeNativeAddress(operand, binding.size());
                    MemorySegment copy = MemorySegment.allocateNative(binding.size(), binding.alignment());
                    MemoryAddress.copy(operand, copy.baseAddress(), binding.size());
                    stack.push(copy); // leaked
                }
                case ALLOC_BUFFER -> {
                    Binding.Allocate binding = (Binding.Allocate) b;
                    stack.push(MemorySegment.allocateNative(binding.size(), binding.alignment()));
                }
                case CONVERT_ADDRESS ->
                    stack.push(MemoryAddress.ofLong((long) stack.pop()));
                case BASE_ADDRESS ->
                    stack.push(((MemorySegment) stack.pop()).baseAddress());
                case DUP ->
                    stack.push(stack.peekLast());
                default -> throw new IllegalArgumentException("Unsupported tag: " + b);
            }
        }

       return stack.pop();
    }

    private static void writeOverSized(MemoryAddress ptr, Class<?> type, Object o) {
        // use VH_LONG for integers to zero out the whole register in the process
        if (type == long.class) {
            VH_LONG.set(ptr, (long) o);
        } else if (type == int.class) {
            VH_LONG.set(ptr, (long) (int) o);
        } else if (type == short.class) {
            VH_LONG.set(ptr, (long) (short) o);
        } else if (type == char.class) {
            VH_LONG.set(ptr, (long) (char) o);
        } else if (type == byte.class) {
            VH_LONG.set(ptr, (long) (byte) o);
        } else if (type == float.class) {
            VH_FLOAT.set(ptr, (float) o);
        } else if (type == double.class) {
            VH_DOUBLE.set(ptr, (double) o);
        } else {
            throw new IllegalArgumentException("Unsupported carrier: " + type);
        }
    }

    private static void write(MemoryAddress ptr, Class<?> type, Object o) {
        if (type == long.class) {
            VH_LONG.set(ptr, (long) o);
        } else if (type == int.class) {
            VH_INT.set(ptr, (int) o);
        } else if (type == short.class) {
            VH_SHORT.set(ptr, (short) o);
        } else if (type == char.class) {
            VH_CHAR.set(ptr, (char) o);
        } else if (type == byte.class) {
            VH_BYTE.set(ptr, (byte) o);
        } else if (type == float.class) {
            VH_FLOAT.set(ptr, (float) o);
        } else if (type == double.class) {
            VH_DOUBLE.set(ptr, (double) o);
        } else {
            throw new IllegalArgumentException("Unsupported carrier: " + type);
        }
    }

    private static Object read(MemoryAddress ptr, Class<?> type) {
        if (type == long.class) {
            return (long) VH_LONG.get(ptr);
        } else if (type == int.class) {
            return (int) VH_INT.get(ptr);
        } else if (type == short.class) {
            return (short) VH_SHORT.get(ptr);
        } else if (type == char.class) {
            return (char) VH_CHAR.get(ptr);
        } else if (type == byte.class) {
            return (byte) VH_BYTE.get(ptr);
        } else if (type == float.class) {
            return (float) VH_FLOAT.get(ptr);
        } else if (type == double.class) {
            return (double) VH_DOUBLE.get(ptr);
        } else {
            throw new IllegalArgumentException("Unsupported carrier: " + type);
        }
    }
}
