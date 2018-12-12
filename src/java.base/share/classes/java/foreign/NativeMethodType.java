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

package java.foreign;

import java.foreign.layout.Function;
import java.foreign.layout.Layout;
import java.foreign.memory.LayoutType;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import jdk.internal.foreign.Util;

/**
 * Describe a native function and corresponding Java method.
 * @apiNote argsType only include the explicit argument for varargs function, i.e, without the implicit trailing
 * array as we see in Java MethodType
 */
public class NativeMethodType {
    private final LayoutType<?> returnType;
    private final LayoutType<?>[] argsType;
    private final boolean isVarArgs;
    private MethodType methodType;

    public NativeMethodType(boolean isVarArgs, LayoutType<?> returnType, LayoutType<?>... argsType) {
        this.argsType = argsType;
        this.isVarArgs = isVarArgs;
        this.returnType = (returnType == null) ? NativeTypes.VOID : returnType;
    }

    /**
     * Get an instance of NativeMethodType
     * @param function Tha native function
     * @param method The Java method
     * @return
     */
    public static NativeMethodType of(Function function, Method method) {
        Util.checkCompatible(method, function);

        LayoutType<?> ret = function.returnLayout()
                .<LayoutType<?>>map(l -> Util.makeType(method.getGenericReturnType(), l))
                .orElse(NativeTypes.VOID);

        // Use function argument size and ignore last argument from method for vararg function
        LayoutType<?>[] args = new LayoutType<?>[function.argumentLayouts().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = Util.makeType(method.getGenericParameterTypes()[i], function.argumentLayouts().get(i));
        }

        return new NativeMethodType(function.isVariadic(), ret, args);
    }

    public boolean isVarArgs() { return isVarArgs; }
    public LayoutType<?> getReturnType() { return returnType; }
    public LayoutType<?>[] getArgsType() { return argsType; }

    public MethodType methodType() {
        if (methodType == null) {
            Class<?> r = returnType.carrier();
            int argsCount = argsType.length;
            if (isVarArgs()) argsCount += 1;
            Class<?>[] a = new Class<?>[argsCount];
            for (int i = 0; i < argsType.length; i++) {
                a[i] = argsType[i].carrier();
            }
            if (isVarArgs()) {
                a[argsType.length] = Object[].class;
            }
            methodType = MethodType.methodType(r, a);
        }
        return methodType;
    }

    public Function function() {
        Layout[] argsLayout = Arrays.stream(argsType).map(LayoutType::layout).toArray(Layout[]::new);
        if (returnType == NativeTypes.VOID) {
            return Function.ofVoid(isVarArgs(), argsLayout);
        } else {
            return Function.of(returnType.layout(), isVarArgs(), argsLayout);
        }
    }
}
