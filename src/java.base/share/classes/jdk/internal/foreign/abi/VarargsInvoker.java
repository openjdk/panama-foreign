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

package jdk.internal.foreign.abi;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import jdk.internal.foreign.Util;

public class VarargsInvoker {

    private static final MethodHandle INVOKE_MH;
    final NativeMethodType nativeMethodType;
    final Library.Symbol symbol;

    private VarargsInvoker(Library.Symbol symbol, NativeMethodType nativeMethodType) {
        this.symbol = symbol;
        this.nativeMethodType = nativeMethodType;
    }

    static {
        try {
            INVOKE_MH = MethodHandles.lookup().findVirtual(VarargsInvoker.class, "invoke", MethodType.methodType(Object.class, Object[].class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle make(Library.Symbol symbol, NativeMethodType nativeMethodType) {
        VarargsInvoker invoker = new VarargsInvoker(symbol, nativeMethodType);
        MethodType methodType = nativeMethodType.methodType();
        return INVOKE_MH.bindTo(invoker).asCollector(Object[].class, methodType.parameterCount())
                .asType(methodType);
    }

    private Object invoke(Object[] args) throws Throwable {
        // one trailing Object[]
        int nNamedArgs = nativeMethodType.getArgsType().length;
        assert(args.length == nNamedArgs + 1);
        // The last argument is the array of vararg collector
        Object[] unnamedArgs = (Object[]) args[args.length - 1];

        LayoutType<?> retLayoutType = nativeMethodType.getReturnType();
        LayoutType<?>[] argLayoutTypes = new LayoutType<?>[nNamedArgs + unnamedArgs.length];
        System.arraycopy(nativeMethodType.getArgsType(), 0, argLayoutTypes, 0, nNamedArgs);
        int pos = nNamedArgs;
        for (Object o: unnamedArgs) {
            Class<?> type = o.getClass();
            argLayoutTypes[pos++] = Util.makeType(computeClass(type), Util.variadicLayout(type));
        }
        MethodHandle delegate = SystemABI.getInstance().downcallHandle(symbol,
                new NativeMethodType(false, retLayoutType, argLayoutTypes));

        // flatten argument list so that it can be passed to an asSpreader MH
        Object[] allArgs = new Object[nNamedArgs + unnamedArgs.length];
        System.arraycopy(args, 0, allArgs, 0, nNamedArgs);
        System.arraycopy(unnamedArgs, 0, allArgs, nNamedArgs, unnamedArgs.length);

        return delegate.invokeWithArguments(allArgs);
    }

    private Class<?> computeClass(Class<?> c) {
        if (c.isPrimitive()) {
            throw new IllegalArgumentException("Not expecting primitive type " + c.getName());
        }

        if (c == Byte.class || c == Short.class || c == Character.class || c == Integer.class || c == Long.class) {
            return long.class;
        } else if (c == Float.class || c == Double.class) {
            return double.class;
        } else if (Pointer.class.isAssignableFrom(c)) {
            return Pointer.class;
        } else {
            throw new UnsupportedOperationException("Type unhandled: " + c.getName());
        }
    }
}
