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
package jdk.internal.foreign.abi.x64.sysv;

import jdk.internal.foreign.abi.ArgumentBinding;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.UniversalNativeInvoker;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.util.List;
import java.util.function.Function;

class UniversalNativeInvokerImpl extends UniversalNativeInvoker {

    public UniversalNativeInvokerImpl(long addr, String methodName, CallingSequence callingSequence, NativeMethodType nmt) {
        super(addr, methodName, callingSequence, nmt);
    }

    public static UniversalNativeInvoker make(Library.Symbol symbol, CallingSequence callingSequence, NativeMethodType nmt) {
        try {
            return new UniversalNativeInvokerImpl(symbol.getAddress().addr(), symbol.getName(), callingSequence, nmt);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }
    }

    @Override
    public void unboxValue(Object o, LayoutType<?> type, Function<ArgumentBinding, Pointer<?>> dstPtrFunc,
                           List<ArgumentBinding> bindings) throws Throwable {
        SysVx64ABI.unboxValue(o, type, dstPtrFunc, bindings);
    }

    @Override
    public Object boxValue(LayoutType<?> type, Function<ArgumentBinding, Pointer<?>> srcPtrFunc,
                           List<ArgumentBinding> bindings) throws IllegalAccessException {
        return SysVx64ABI.boxValue(type, srcPtrFunc, bindings);
    }
}
