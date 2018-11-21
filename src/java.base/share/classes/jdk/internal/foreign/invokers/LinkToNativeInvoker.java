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

public class LinkToNativeInvoker extends NativeInvoker {
    @Stable
    private final MethodHandle boundMethodHandle;

    public MethodHandle getBoundMethodHandle() {
        return boundMethodHandle;
    }

    LinkToNativeInvoker(long addr, CallingSequence callingSequence, Function function, MethodType methodType, Method method) {
        super(addr, callingSequence, function, methodType, method);
        LinkToNativeSignatureShuffler shuffler =
                LinkToNativeSignatureShuffler.javaToNativeShuffler(callingSequence, methodType, this::layoutFor);
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().findNative(addr, method.getName(), shuffler.nativeMethodType());
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
        boundMethodHandle = shuffler.adapt(mh);
    }

    private LayoutType<?> layoutFor(int pos) {
        return pos == -1 ?
                function.returnLayout().<LayoutType<?>>map(l -> Util.makeType(method.getGenericReturnType(), l)).orElse(NativeTypes.VOID) :
                Util.makeType(method.getGenericParameterTypes()[pos], function.argumentLayouts().get(pos));
    }
}
