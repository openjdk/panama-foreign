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
 * accompanied this code).Collecton
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A native method type represents the arguments and return native types (expressed as {@link LayoutType} objects)
 * accepted and returned by a native function, or the arguments and return type passed and expected by native function
 * caller.
 *
 * Native method types must be properly matched between a method handle and all its callers for the native function call to succeed.
 * @apiNote parameterTypes only include the explicit argument for varargs function, i.e, without the implicit trailing
 * array as we see in Java MethodType
 *
 * @see LayoutType
 * @see MethodType
 * @see Function
 */
public final class NativeMethodType {
    private final LayoutType<?> returnType;
    private final LayoutType<?>[] parameterTypes;
    private final boolean isVarArgs;
    private volatile MethodType methodType;
    private volatile Function function;

    private NativeMethodType(boolean isVarArgs, LayoutType<?> returnType, LayoutType<?>... parameterTypes) {
        this.parameterTypes = parameterTypes;
        this.isVarArgs = isVarArgs;
        this.returnType = (returnType == null) ? NativeTypes.VOID : returnType;
    }
    
    /**
     * Obtain a new native method type given native return/parameter types.
     * @param returnType the return native type.
     * @param parameterTypes the native parameter types.
     * @return the new native method type.
     */
    public static NativeMethodType of(LayoutType<?> returnType, LayoutType<?>... parameterTypes) {
        return of(false, returnType, parameterTypes);
    }

    /**
     * Obtain a new native method type given variable arity statis, native return/parameter types.
     * @param isVarargs true, if the native method type models a variable arity native function.
     * @param returnType the return native type.
     * @param parameterTypes the native parameter types.
     * @return the new native method type.
     */
    public static NativeMethodType of(boolean isVarargs, LayoutType<?> returnType, LayoutType<?>... parameterTypes) {
        Objects.requireNonNull(returnType);
        Objects.requireNonNull(parameterTypes);
        return new NativeMethodType(isVarargs, returnType, parameterTypes);
    }

    /**
     * Does this native method type describe a variable-arity native function?
     * @return true, if this native method type models a variable-arity native function.
     */
    public boolean isVarArgs() { return isVarArgs; }

    /**
     * Retrieves the return type of this native method type.
     * @return the return type.
     */
    public LayoutType<?> returnType() { return returnType; }

    /**
     * Retrieves the i-th parameter type of this native method type.
     * @return the parameter type at position {@code pos}.
     * @throws IndexOutOfBoundsException if {@code num} is not a valid index into {@link #parameterArray()}.
     */
    public LayoutType<?> parameterType(int pos) {
        return parameterTypes[pos];
    }

    /**
     * Retrieves the array of parameter types of this native method type.
     * @return the parameter type array.
     */
    public LayoutType<?>[] parameterArray() { return parameterTypes; }

    /**
     * Retrieves a list of parameter types of this native method type.
     * @return the parameter type array.
     */
    public List<LayoutType<?>> parameterList() { return Arrays.asList(parameterTypes); }

    /**
     * Retrieves the arity of this native method type.
     * @return the native method type arity (excluding trailing variable-arity parameters).
     */
    public int parameterCount() { return parameterTypes.length; }

    /**
     * Retrieves the {@link MethodType} instance associated with this native method type.
     * @return a {@link MethodType} instance.
     */
    public MethodType methodType() {
        if (methodType == null) {
            Class<?> r = returnType.carrier();
            int argsCount = parameterTypes.length;
            if (isVarArgs()) argsCount += 1;
            Class<?>[] a = new Class<?>[argsCount];
            for (int i = 0; i < parameterTypes.length; i++) {
                a[i] = parameterTypes[i].carrier();
            }
            if (isVarArgs()) {
                a[parameterTypes.length] = Object[].class;
            }
            methodType = MethodType.methodType(r, a);
        }
        return methodType;
    }

    /**
     * Retrieves the {@link Function} instance associated with this native method type.
     * @return a {@link Function} instance.
     */
    public Function function() {
        if (function == null) {
            Layout[] argsLayout = Arrays.stream(parameterTypes).map(LayoutType::layout).toArray(Layout[]::new);
            function = (returnType == NativeTypes.VOID) ?
                Function.ofVoid(isVarArgs(), argsLayout) :
                Function.of(returnType.layout(), isVarArgs(), argsLayout);
        }
        return function;
    }
}
