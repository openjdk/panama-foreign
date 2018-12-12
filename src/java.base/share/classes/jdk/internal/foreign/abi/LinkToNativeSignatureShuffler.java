/*
 *  Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.foreign.Util;
import jdk.internal.foreign.memory.LayoutTypeImpl;

import java.foreign.layout.Function;
import java.foreign.memory.LayoutType;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class LinkToNativeSignatureShuffler extends DirectSignatureShuffler {
    public LinkToNativeSignatureShuffler(CallingSequence callingSequence, MethodType javaMethodType, IntFunction<LayoutType<?>> layoutTypeFactory, ShuffleDirection direction) {
        super(callingSequence, javaMethodType, layoutTypeFactory, direction);
    }

    @Override
    protected int[] forwardPermutations() {
        return IntStream.range(0, erasedMethodType.parameterCount()).toArray();
    }

    @Override
    MethodType nativeMethodType() {
        return erasedMethodType;
    }

    protected void processType(int sigPos, LayoutType<?> lt, List<ArgumentBinding> bindings, ShuffleDirection direction) {
        Class<?> carrier = (Class<?>) Util.unboxIfNeeded(((LayoutTypeImpl<?>)lt).carrier());
        if (carrier == float.class) {
            //floats should not be erased!
            updateNativeMethodType(sigPos, float.class);
        } else {
            super.processType(sigPos, lt, bindings, direction);
        }
    }

    static LinkToNativeSignatureShuffler javaToNativeShuffler(CallingSequence callingSequence, MethodType javaMethodType,
                                                        IntFunction<LayoutType<?>> layoutTypeFactory) {
        return new LinkToNativeSignatureShuffler(callingSequence, javaMethodType, layoutTypeFactory, ShuffleDirection.JAVA_TO_NATIVE);
    }

    static LinkToNativeSignatureShuffler nativeToJavaShuffler(CallingSequence callingSequence, MethodType javaMethodType,
                                                              IntFunction<LayoutType<?>> layoutTypeFactory) {
        return new LinkToNativeSignatureShuffler(callingSequence, javaMethodType, layoutTypeFactory, ShuffleDirection.NATIVE_TO_JAVA);
    }

    private static boolean accept(int arity, CallingSequence callingSequence, ShuffleDirection direction) {
        if (direction == ShuffleDirection.NATIVE_TO_JAVA) {
            //only support LL -> L for now
            return !callingSequence.returnsInMemory() &&
                    arity == 2 &&
                    callingSequence.getArgumentBindings(0).size() == 1 &&
                    callingSequence.getArgumentBindings(0).get(0).getStorage().getStorageClass() == StorageClass.INTEGER_ARGUMENT_REGISTER &&
                    callingSequence.getArgumentBindings(1).size() == 1 &&
                    callingSequence.getArgumentBindings(1).get(0).getStorage().getStorageClass() == StorageClass.INTEGER_ARGUMENT_REGISTER &&
                    callingSequence.getReturnBindings().size() == 1 &&
                    callingSequence.getReturnBindings().get(0).getStorage().getStorageClass() == StorageClass.INTEGER_RETURN_REGISTER;
        }
        for (int i = 0 ; i < arity ; i++) {
            List<ArgumentBinding> argumentBindings = callingSequence.getArgumentBindings(i);
            if (argumentBindings.size() != 1 ||
                    argumentBindings.get(0).getStorage().getStorageClass() == StorageClass.STACK_ARGUMENT_SLOT) {
                return false;
            }
        }

        List<ArgumentBinding> returnBindings = callingSequence.getReturnBindings();
        if (returnBindings.isEmpty()) {
            return true;
        } else {
            return !callingSequence.returnsInMemory() &&
                    returnBindings.size() == 1;
        }
    }

    public static boolean acceptDowncall(int arity, CallingSequence callingSequence) {
        return accept(arity, callingSequence, ShuffleDirection.JAVA_TO_NATIVE);
    }

    public static boolean acceptUpcall(int arity, CallingSequence callingSequence) {
        return accept(arity, callingSequence, ShuffleDirection.NATIVE_TO_JAVA);
    }
}
