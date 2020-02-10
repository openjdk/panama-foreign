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

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import sun.security.action.GetPropertyAction;

import java.lang.invoke.MethodType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class CallingSequenceBuilder {
    private static final boolean VERIFY_BINDINGS = Boolean.parseBoolean(
            GetPropertyAction.privilegedGetProperty("jdk.incubator.foreign.VERIFY_BINDINGS", "true"));

    private final boolean forUpcall;
    private final List<List<Binding>> inputBindings = new ArrayList<>();
    private List<Binding> ouputBindings = List.of();

    private MethodType mt = MethodType.methodType(void.class);
    private FunctionDescriptor desc = FunctionDescriptor.ofVoid(false);

    public CallingSequenceBuilder(boolean forUpcall) {
        this.forUpcall = forUpcall;
    }

    public final CallingSequenceBuilder addArgumentBindings(Class<?> carrier, MemoryLayout layout,
                                                            List<Binding> bindings) {
        verifyBindings(true, carrier, bindings);
        inputBindings.add(bindings);
        mt = mt.appendParameterTypes(carrier);
        descAddArgument(layout);
        return this;
    }

    private void descAddArgument(MemoryLayout layout) {
        boolean isVoid = desc.returnLayout().isEmpty();
        var args = new ArrayList<>(desc.argumentLayouts());
        args.add(layout);
        var argsArray = args.toArray(MemoryLayout[]::new);
        if (isVoid) {
            desc = FunctionDescriptor.ofVoid(false, argsArray);
        } else {
            desc = FunctionDescriptor.of(desc.returnLayout().get(), false, argsArray);
        }
    }

    public CallingSequenceBuilder setReturnBindings(Class<?> carrier, MemoryLayout layout,
                                                    List<Binding> bindings) {
        verifyBindings(false, carrier, bindings);
        this.ouputBindings = bindings;
        mt = mt.changeReturnType(carrier);
        desc = FunctionDescriptor.of(layout, false, desc.argumentLayouts().toArray(MemoryLayout[]::new));
        return this;
    }

    public CallingSequence build() {
        return new CallingSequence(mt, desc, inputBindings, ouputBindings);
    }

    private void verifyBindings(boolean forArguments, Class<?> carrier, List<Binding> bindings) {
        if (VERIFY_BINDINGS) {
            if (forUpcall == forArguments) {
                verifyBoxBindings(carrier, bindings);
            } else {
                verifyUnboxBindings(carrier, bindings);
            }
        }
    }

    private static void checkType(Class<?> actualType, Class<?> expectedType) {
        if (expectedType != actualType) {
            throw new IllegalArgumentException(
                    String.format("Invalid operand type: %s. %s expected", actualType, expectedType));
        }
    }

    private static void verifyUnboxBindings(Class<?> inType, List<Binding> bindings) {
        Deque<Class<?>> stack = new ArrayDeque<>();
        stack.push(inType);

        for (Binding b : bindings) {
            switch (b.tag()) {
                case MOVE -> {
                    Class<?> actualType = stack.pop();
                    Class<?> expectedType = ((Binding.Move) b).type();
                    checkType(actualType, expectedType);
                }
                case DEREFERENCE -> {
                    Class<?> actualType = stack.pop();
                    checkType(actualType, MemorySegment.class);
                    Class<?> newType = ((Binding.Dereference) b).type();
                    stack.push(newType);
                }
                case BASE_ADDRESS -> {
                    Class<?> actualType = stack.pop();
                    checkType(actualType, MemorySegment.class);
                    stack.push(MemoryAddress.class);
                }
                case CONVERT_ADDRESS -> {
                    Class<?> actualType = stack.pop();
                    checkType(actualType, MemoryAddress.class);
                    stack.push(long.class);
                }
                case ALLOC_BUFFER ->
                    throw new UnsupportedOperationException();
                case COPY_BUFFER -> {
                    Class<?> actualType = stack.pop();
                    checkType(actualType, MemorySegment.class);
                    stack.push(MemorySegment.class);
                }
                case DUP ->
                    stack.push(stack.peekLast());
                default -> throw new IllegalArgumentException("Unknown binding: " + b);
            }
        }

        if (!stack.isEmpty()) {
            throw new IllegalArgumentException("Stack must be empty after recipe");
        }
    }

    private static void verifyBoxBindings(Class<?> outType, List<Binding> bindings) {
        Deque<Class<?>> stack = new ArrayDeque<>();

        for (Binding b : bindings) {
            switch (b.tag()) {
                case MOVE -> {
                    Class<?> newType = ((Binding.Move) b).type();
                    stack.push(newType);
                }
                case DEREFERENCE -> {
                    Class<?> storeType = stack.pop();
                    checkType(storeType, ((Binding.Dereference) b).type());
                    Class<?> segmentType = stack.pop();
                    checkType(segmentType, MemorySegment.class);
                }
                case CONVERT_ADDRESS -> {
                    Class<?> actualType = stack.pop();
                    checkType(actualType, long.class);
                    stack.push(MemoryAddress.class);
                }
                case BASE_ADDRESS -> {
                    Class<?> actualType = stack.pop();
                    checkType(actualType, MemorySegment.class);
                    stack.push(MemoryAddress.class);
                }
                case ALLOC_BUFFER -> {
                    stack.push(MemorySegment.class);
                }
                case COPY_BUFFER -> {
                    Class<?> actualType = stack.pop();
                    checkType(actualType, MemoryAddress.class);
                    stack.push(MemorySegment.class);
                }
                case DUP ->
                    stack.push(stack.peekLast());
                default -> throw new IllegalArgumentException("Unknown binding: " + b);
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Stack must contain exactly 1 value");
        }

        Class<?> actualReturnType = stack.pop();
        checkType(actualReturnType, outType);
    }
}
