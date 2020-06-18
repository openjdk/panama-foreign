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
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.Utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class BindingInterpreter {

    static void unbox(Object arg, List<Binding> bindings, StoreFunc storeFunc, List<? super MemorySegment> buffers) {
        Deque<Object> stack = new ArrayDeque<>();
        stack.push(arg);
        for (Binding b : bindings) {
            switch (b.tag()) {
                case MOVE -> {
                    Binding.Move binding = (Binding.Move) b;
                    storeFunc.store(binding.storage(), binding.type(), stack.pop());
                }
                case DEREFERENCE -> {
                    Binding.Dereference deref = (Binding.Dereference) b;
                    MemorySegment operand = (MemorySegment) stack.pop();
                    MemoryAddress baseAddress = operand.baseAddress();
                    MemoryAddress readAddress = baseAddress.addOffset(deref.offset());
                    stack.push(SharedUtils.read(readAddress, deref.type()));
                }
                case COPY_BUFFER -> {
                    Binding.Copy binding = (Binding.Copy) b;
                    MemorySegment operand = (MemorySegment) stack.pop();
                    assert operand.byteSize() == binding.size() : "operand size mismatch";
                    MemorySegment copy = MemorySegment.allocateNative(binding.size(), binding.alignment());
                    copy.copyFrom(operand.asSlice(0, binding.size()));
                    buffers.add(copy);
                    stack.push(copy);
                }
                case ALLOC_BUFFER ->
                    throw new UnsupportedOperationException();
                case CONVERT_ADDRESS ->
                    stack.push(((MemoryAddress) stack.pop()).toRawLongValue());
                case BASE_ADDRESS ->
                    stack.push(((MemorySegment) stack.pop()).baseAddress());
                case DUP ->
                    stack.push(stack.peekLast());
                default -> throw new IllegalArgumentException("Unsupported tag: " + b);
            }
        }
    }

    static Object box(List<Binding> bindings, LoadFunc loadFunc) {
        Deque<Object> stack = new ArrayDeque<>();
        for (Binding b : bindings) {
            switch (b.tag()) {
                case MOVE -> {
                    Binding.Move binding = (Binding.Move) b;
                    stack.push(loadFunc.load(binding.storage(), binding.type()));
                }
                case DEREFERENCE -> {
                    Binding.Dereference binding = (Binding.Dereference) b;
                    Object value = stack.pop();
                    MemorySegment operand = (MemorySegment) stack.pop();
                    MemoryAddress baseAddress = operand.baseAddress();
                    MemoryAddress writeAddress = baseAddress.addOffset(binding.offset());
                    SharedUtils.write(writeAddress, binding.type(), value);
                }
                case COPY_BUFFER -> {
                    Binding.Copy binding = (Binding.Copy) b;
                    MemoryAddress operand = (MemoryAddress) stack.pop();
                    operand = MemoryAddressImpl.ofLongUnchecked(operand.toRawLongValue(), binding.size());
                    MemorySegment copy = MemorySegment.allocateNative(binding.size(), binding.alignment());
                    copy.copyFrom(operand.segment().asSlice(0, binding.size()));
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

    interface StoreFunc {
        void store(VMStorage storage, Class<?> type, Object o);
    }

    interface LoadFunc {
        Object load(VMStorage storage, Class<?> type);
    }
}
