/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.nicl.types;

import java.util.Arrays;
import java.util.stream.Stream;

public class Function implements Type {
    final Type[] arguments;
    final Type returnType;
    final boolean isVarArg;

    public Function(Type[] args, Type returnType, boolean isVarArg) {
        this.arguments = args;
        this.returnType = returnType;
        this.isVarArg = isVarArg;
    }

    public boolean isVarArg() {
        return isVarArg;
    }

    public Stream<Type> arguments() {
        return Stream.of(arguments);
    }

    public int argumentCount() {
        return arguments.length;
    }

    public Type getArgumentType(int index) {
        return arguments[index];
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    public long getSize() {
        throw new UnsupportedOperationException("Function type has no size");
    }

    @Override
    public int hashCode() {
        return (returnType.hashCode() * 31 + Arrays.hashCode(arguments)) << 1 + (isVarArg ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof jdk.internal.nicl.types.Function)) {
            return false;
        }
        jdk.internal.nicl.types.Function other = (jdk.internal.nicl.types.Function) o;
        if (!other.returnType.equals(returnType)) {
            return false;
        }
        if (other.isVarArg != isVarArg) {
            return false;
        }
        if (other.arguments.length != arguments.length) {
            return false;
        }
        for (int i = 0; i < arguments.length; i++) {
            if (!arguments[i].equals(other.arguments[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('(');
        for (Type arg : arguments) {
            sb.append(arg.toString());
        }
        if (isVarArg) {
            sb.append('*');
        }
        sb.append(')');
        sb.append(returnType.toString());
        return sb.toString();
    }
}
