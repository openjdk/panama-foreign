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
import jdk.internal.foreign.abi.SystemABI;

import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.memory.Pointer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.stream.Stream;

class VarargsInvoker extends NativeInvoker {

    private static final MethodHandle INVOKE_MH;

    public VarargsInvoker(long addr, Function function, MethodType methodType, Method method) {
        super(addr, null, function, methodType, method);
    }

    static {
        try {
            INVOKE_MH = MethodHandles.lookup().findVirtual(VarargsInvoker.class, "invoke", MethodType.methodType(Object.class, Object[].class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MethodHandle getBoundMethodHandle() {
        return INVOKE_MH.bindTo(this).asCollector(Object[].class, methodType.parameterCount())
                .asType(methodType);
    }

    private Object invoke(Object[] args) throws Throwable {
        // one trailing Object[]
        int nNamedArgs = methodType.parameterCount() - 1;
        Object[] unnamedArgs = (Object[]) args[args.length - 1];

        // flatten argument list so that it can be passed to an asSpreader MH
        Object[] allArgs = new Object[nNamedArgs + unnamedArgs.length];
        System.arraycopy(args, 0, allArgs, 0, nNamedArgs);
        System.arraycopy(unnamedArgs, 0, allArgs, nNamedArgs, unnamedArgs.length);

        //we need to infer layouts for all unnamed arguments
        Layout[] argLayouts = Stream.concat(function.argumentLayouts().stream(),
                Stream.of(unnamedArgs).map(Object::getClass).map(Util::variadicLayout))
                .toArray(Layout[]::new);

        Function varargFunc = function.returnLayout().isPresent() ?
                Function.of(function.returnLayout().get(), false, argLayouts) :
                Function.ofVoid(false, argLayouts);

        MethodType dynamicMethodType = getDynamicMethodType(methodType, unnamedArgs);

        UniversalNativeInvoker delegate = new UniversalNativeInvoker(addr, SystemABI.getInstance().arrangeCall(varargFunc),
                varargFunc, dynamicMethodType,
                method, dynamicMethodType.parameterArray());
        return delegate.invoke(allArgs);
    }

    private MethodType getDynamicMethodType(MethodType baseMethodType, Object[] unnamedArgs) {
        ArrayList<Class<?>> types = new ArrayList<>();

        Class<?>[] staticArgTypes = baseMethodType.parameterArray();

        // skip trailing Object[]
        for (int i = 0; i < staticArgTypes.length - 1; i++) {
            types.add(staticArgTypes[i]);
        }

        for (Object o : unnamedArgs) {
            types.add(computeClass(o.getClass()));
        }

        return MethodType.methodType(baseMethodType.returnType(), types.toArray(new Class<?>[0]));
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
