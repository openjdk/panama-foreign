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

import java.foreign.NativeMethodType;
import java.foreign.layout.Function;
import java.foreign.memory.LayoutType;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class LinkToNativeSignatureShuffler extends DirectSignatureShuffler {
    public LinkToNativeSignatureShuffler(CallingSequence callingSequence, NativeMethodType nmt, ShuffleDirection direction) {
        super(callingSequence, nmt, direction);
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

    static LinkToNativeSignatureShuffler javaToNativeShuffler(CallingSequence callingSequence, NativeMethodType nmt) {
        return new LinkToNativeSignatureShuffler(callingSequence, nmt, ShuffleDirection.JAVA_TO_NATIVE);
    }

    static LinkToNativeSignatureShuffler nativeToJavaShuffler(CallingSequence callingSequence, NativeMethodType nmt) {
        return new LinkToNativeSignatureShuffler(callingSequence, nmt, ShuffleDirection.NATIVE_TO_JAVA);
    }

    private static boolean accept(List<ArgumentBinding> bindings) {
        if (bindings.size() != 1) {
            return false;
        }
        ArgumentBinding binding = bindings.get(0);
        switch (binding.getStorage().getStorageClass()) {
            case INTEGER_ARGUMENT_REGISTER:
            case VECTOR_ARGUMENT_REGISTER:
            case STACK_ARGUMENT_SLOT:
            case INTEGER_RETURN_REGISTER:
            case VECTOR_RETURN_REGISTER:
                return true;
            case X87_RETURN_REGISTER:
                return false;
            default:
                throw new InternalError();
        }
    }

    private static boolean accept(NativeMethodType nmt, CallingSequence callingSequence, ShuffleDirection direction) {
        int arity = nmt.parameterCount();
        if (direction == ShuffleDirection.NATIVE_TO_JAVA) {
            for (int i = 0; i < nmt.parameterCount(); i++) {
                List<ArgumentBinding> bindings = callingSequence.getArgumentBindings(i);
                if (!accept(bindings)) {
                    return false;
                }
            }
            List<ArgumentBinding> retBindings = callingSequence.getReturnBindings();
            return accept(retBindings);
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

    public static boolean acceptDowncall(NativeMethodType nmt, CallingSequence callingSequence) {
        return accept(nmt, callingSequence, ShuffleDirection.JAVA_TO_NATIVE);
    }

    public static boolean acceptUpcall(NativeMethodType nmt, CallingSequence callingSequence) {
        return accept(nmt, callingSequence, ShuffleDirection.NATIVE_TO_JAVA);
    }
}
