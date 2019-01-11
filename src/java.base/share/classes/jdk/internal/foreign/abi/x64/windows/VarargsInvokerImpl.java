/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package jdk.internal.foreign.abi.x64.windows;

import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.VarargsInvoker;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

class VarargsInvokerImpl extends VarargsInvoker {

    private VarargsInvokerImpl(Library.Symbol symbol, NativeMethodType nativeMethodType) {
        super(symbol, nativeMethodType);
    }

    public static MethodHandle make(Library.Symbol symbol, NativeMethodType nativeMethodType) {
        VarargsInvokerImpl invoker = new VarargsInvokerImpl(symbol, nativeMethodType);
        MethodType methodType = nativeMethodType.methodType();
        return INVOKE_MH.bindTo(invoker).asCollector(Object[].class, methodType.parameterCount())
                .asType(methodType);
    }

    @Override
    protected MethodHandle specialize(NativeMethodType newNativeMethodType) {
        CallingSequence cs = Windowsx64ABI.arrangeCall(newNativeMethodType, nativeMethodType.parameterCount());
        return UniversalNativeInvokerImpl.make(symbol, cs, newNativeMethodType).getBoundMethodHandle();
    }

}

