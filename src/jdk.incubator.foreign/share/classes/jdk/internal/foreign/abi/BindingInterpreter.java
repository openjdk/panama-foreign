/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.function.Function;

class BindingInterpreter {
    private static final VarHandle VH_LONG = MemoryHandles.varHandle(long.class);
    private static final VarHandle VH_FLOAT = MemoryHandles.varHandle(float.class);
    private static final VarHandle VH_DOUBLE = MemoryHandles.varHandle(double.class);

    static void unbox(Object arg, List<Binding> bindings, Function<VMStorage,
            MemoryAddress> ptrFunction, List<? super MemorySegment> buffers) {
        Object currentValue = arg;
        for (Binding b : bindings) {
            switch (b.tag()) {
                case Binding.MOVE_TAG: {
                    Binding.Move binding = (Binding.Move) b;
                    MemoryAddress ptr = ptrFunction.apply(binding.storage());
                    // use VH_LONG for integers to zero out the whole register in the process
                    if (binding.type() == long.class) {
                        VH_LONG.set(ptr, (long) currentValue);
                    } else if (binding.type() == int.class) {
                        VH_LONG.set(ptr, (long) (int) currentValue);
                    } else if (binding.type() == short.class) {
                        VH_LONG.set(ptr, (long) (short) currentValue);
                    } else if (binding.type() == char.class) {
                        VH_LONG.set(ptr, (long) (char) currentValue);
                    } else if (binding.type() == byte.class) {
                        VH_LONG.set(ptr, (long) (byte) currentValue);
                    } else if (binding.type() == float.class) {
                        VH_FLOAT.set(ptr, (float) currentValue);
                    } else if (binding.type() == double.class) {
                        VH_DOUBLE.set(ptr, (double) currentValue);
                    } else {
                        throw new IllegalArgumentException("Unsupported carrier: " + binding.type());
                    }
                } break;
                case Binding.BOX_ADDRESS_TAG: {
                    currentValue = MemoryAddressImpl.addressof((MemoryAddress) currentValue);
                } break;
                case Binding.BASE_ADDRESS_TAG: {
                    currentValue = ((MemorySegment) currentValue).baseAddress();
                } break;
                case Binding.DEREFERENCE_TAG: {
                    Binding.Dereference binding = (Binding.Dereference) b;
                    MemoryAddress ptr = ptrFunction.apply(binding.storage());
                    MemorySegment operand = (MemorySegment) currentValue;
                    MemoryAddress baseAddress = operand.baseAddress();
                    MemoryAddress.copy(baseAddress.offset(binding.offset()), ptr, binding.size());
                } break;
                case Binding.COPY_BUFFER_TAG: {
                    Binding.Copy binding = (Binding.Copy) b;
                    MemorySegment operand = (MemorySegment) currentValue;
                    assert operand.byteSize() == binding.size() : "operand size mismatch";
                    MemorySegment copy = MemorySegment.ofNative(binding.size(), binding.alignment());
                    MemoryAddress.copy(operand.baseAddress(), copy.baseAddress(), binding.size());
                    buffers.add(copy);
                    currentValue = copy;
                } break;
                default: throw new IllegalArgumentException("Unsupported tag: " + b);
            }
        }
    }

    static Object box(List<Binding> bindings, Function<VMStorage, MemoryAddress> ptrFunction) {
        Object currentValue = null;
        for (Binding b : bindings) {
            switch (b.tag()) {
                case Binding.MOVE_TAG: {
                    Binding.Move binding = (Binding.Move) b;
                    MemoryAddress ptr = ptrFunction.apply(binding.storage());
                    if (binding.type() == long.class) {
                        currentValue = (long) VH_LONG.get(ptr);
                    } else if (binding.type() == int.class) {
                        currentValue = (int) (long) VH_LONG.get(ptr);
                    } else if (binding.type() == short.class) {
                        currentValue = (short) (long) VH_LONG.get(ptr);
                    } else if (binding.type() == char.class) {
                        currentValue = (char) (long) VH_LONG.get(ptr);
                    } else if (binding.type() == byte.class) {
                        currentValue = (byte) (long) VH_LONG.get(ptr);
                    } else if (binding.type() == float.class) {
                        currentValue = (float) VH_FLOAT.get(ptr);
                    } else if (binding.type() == double.class) {
                        currentValue = (double) VH_DOUBLE.get(ptr);
                    } else {
                        throw new IllegalArgumentException("Unsupported type: " + binding.type());
                    }
                } break;
                case Binding.DEREFERENCE_TAG: {
                    Binding.Dereference binding = (Binding.Dereference) b;
                    MemoryAddress ptr = ptrFunction.apply(binding.storage());
                    MemorySegment operand = (MemorySegment) currentValue;
                    MemoryAddress baseAddress = operand.baseAddress();
                    MemoryAddress.copy(ptr, baseAddress.offset(binding.offset()), binding.size());
                } break;
                case Binding.COPY_BUFFER_TAG: {
                    Binding.Copy binding = (Binding.Copy) b;
                    MemoryAddress operand = (MemoryAddress) currentValue;
                    MemorySegment copy = MemorySegment.ofNative(binding.size(), binding.alignment());
                    MemoryAddress.copy(operand, copy.baseAddress(), binding.size());
                    currentValue = copy; // leaked
                } break;
                case Binding.ALLOC_BUFFER_TAG: {
                    Binding.AllocateBuffer binding = (Binding.AllocateBuffer) b;
                    currentValue = MemorySegment.ofNative(binding.size(), binding.alignment());
                } break;
                case Binding.BOX_ADDRESS_TAG: {
                    currentValue = MemoryAddressImpl.ofNative((long) currentValue);
                } break;
                case Binding.BASE_ADDRESS_TAG: {
                    currentValue = ((MemorySegment) currentValue).baseAddress();
                } break;
                default: throw new IllegalArgumentException("Unsupported tag: " + b);
            }
        }

       return currentValue;
    }
}
