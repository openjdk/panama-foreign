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
import jdk.internal.foreign.abi.StorageClass;
import jdk.internal.foreign.abi.SystemABI;

import java.foreign.layout.Function;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;

/**
 * This class encapsulates a given native invocation call strategy.
 */
public abstract class NativeInvoker {

    private static final String fastPath = privilegedGetProperty("jdk.internal.foreign.NativeInvoker.FASTPATH");

    final long addr;
    final Method method;
    final Function function;
    final MethodType methodType;
    final CallingSequence callingSequence;

    NativeInvoker(long addr, CallingSequence callingSequence, Function function, MethodType methodType, Method method) {
        this.addr = addr;
        this.function = function;
        this.methodType = Util.checkNoArrays(methodType);
        this.method = method;
        this.callingSequence = callingSequence;
    }

    public abstract MethodHandle getBoundMethodHandle();

    public static NativeInvoker of(long addr, Function function, MethodType methodType, Method method) {
        Util.checkCompatible(method, function);
        if (method.isVarArgs()) {
            return new VarargsInvoker(addr, function, methodType, method);
        } else {
            CallingSequence callingSequence = SystemABI.getInstance().arrangeCall(function);
            if (fastPath == null || !fastPath.equals("none")) {
                if (DirectSignatureShuffler.acceptDowncall(function, callingSequence)) {
                    return new DirectNativeInvoker(addr, callingSequence, function, methodType, method);
                } else if (fastPath != null && fastPath.equals("direct")) {
                    throw new IllegalStateException(
                            String.format("No fast path for: %s = %s", method.getName(), function));
                }
            }
            return new UniversalNativeInvoker(addr, callingSequence, function, methodType, method);
        }
    }
}
