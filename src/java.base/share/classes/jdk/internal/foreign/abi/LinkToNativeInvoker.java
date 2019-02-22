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

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.Scope;
import java.foreign.memory.Callback;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.stream.IntStream;

import static java.lang.invoke.MethodHandles.collectArguments;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.permuteArguments;
import static java.lang.invoke.MethodType.methodType;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;

public class LinkToNativeInvoker {

    private static final MethodHandle ALLOC_LEAKY;
    private static final MethodHandle MH_Pointer_get;

    static {
        Lookup lookup = MethodHandles.lookup();
        try {
            ALLOC_LEAKY = lookup.findStatic(LinkToNativeInvoker.class, "allocLeaky", methodType(Pointer.class, LayoutType.class));
            MH_Pointer_get = lookup.findVirtual(Pointer.class, "get", methodType(Object.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle make(Library.Symbol symbol, CallingSequence callingSequence, NativeMethodType nmt) {
        LinkToNativeSignatureShuffler shuffler =
                LinkToNativeSignatureShuffler.javaToNativeShuffler(callingSequence, nmt);
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().findNative(symbol, shuffler.nativeMethodType());
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
        mh =  shuffler.adapt(mh);

        if(callingSequence.returnsInMemory()) {
            MethodType internalMethodType = nmt.methodType().insertParameterTypes(0, Pointer.class);
            MethodHandle ret = MH_Pointer_get.asType(methodType(nmt.methodType().returnType(), Pointer.class));
            mh = collectArguments(ret, 1, mh);
            int[] reorder = IntStream.range(-1, internalMethodType.parameterCount()).toArray();
            reorder[0] = 0;
            mh = permuteArguments(mh, internalMethodType, reorder);
            mh = collectArguments(mh, 0, insertArguments(ALLOC_LEAKY, 0, nmt.returnType()));
        }

        return mh;
    }


    private static <T> Pointer<T> allocLeaky(LayoutType<T> type) {
        return Scope.newNativeScope().allocate(type);
    }
}
