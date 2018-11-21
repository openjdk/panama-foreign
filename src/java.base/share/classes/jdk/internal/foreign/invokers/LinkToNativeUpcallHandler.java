/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.foreign.invokers;

import jdk.internal.foreign.Util;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.vm.annotation.Stable;

import java.foreign.NativeTypes;
import java.foreign.layout.Function;
import java.foreign.memory.LayoutType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * This class implements upcall invocation from native code through a set of specialized entry points. A specialized
 * entry point is a Java method which takes a number N of long arguments followed by a number M of double arguments;
 * possible return types for the entry point are either long, double or void.
 */
class LinkToNativeUpcallHandler extends UpcallHandler {
    @Stable
    private final MethodHandle mh;

    LinkToNativeUpcallHandler(CallingSequence callingSequence, Method fiMethod, Function function, Object receiver) {
        super(callingSequence, fiMethod, function, receiver, up -> {
            MethodType mt = ((LinkToNativeUpcallHandler)up).mh.type();
            return allocateLinkToNativeUpcallStub(up);
        });

        try {
            MethodHandle mh = MethodHandles.publicLookup().unreflect(fiMethod);
            Util.checkNoArrays(mh.type());
            mh = mh.bindTo(receiver);
            MethodType methodType = mh.type();
            LinkToNativeSignatureShuffler shuffler =
                    LinkToNativeSignatureShuffler.nativeToJavaShuffler(callingSequence, methodType, this::layoutFor);
            this.mh = shuffler.adapt(mh);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    private LayoutType<?> layoutFor(int pos) {
        return pos == -1 ?
                function.returnLayout().<LayoutType<?>>map(l -> Util.makeType(fiMethod.getGenericReturnType(), l)).orElse(NativeTypes.VOID) :
                Util.makeType(fiMethod.getGenericParameterTypes()[pos], function.argumentLayouts().get(pos));
    }

    /*** direct entry points ***/

    public static long invoke_J_JJ(LinkToNativeUpcallHandler handler, long arg0, long arg1) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1);
    }

    // natives

    static native long allocateLinkToNativeUpcallStub(UpcallHandler handler);

    private static native void registerNatives();
    static {
        DirectUpcallHandler.registerNatives();
    }
}
