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

package java.lang.invoke;

import jdk.internal.invoke.ABIDescriptor;
import jdk.internal.invoke.VMStorage;

import java.lang.ref.Cleaner;
import java.util.Objects;

/** TODO */
/*non-public*/ class NativeEntryPoint {
    final long addr;
    final ABIDescriptor abi;
    final VMStorage[] argMoves;
    final VMStorage[] returnMoves;
    final boolean needTransition;
    final MethodType methodType; // C2 sees erased version (byte -> int), so need this explicitly
    final String name;

    NativeEntryPoint(long addr, ABIDescriptor abi, VMStorage[] argMoves, VMStorage[] returnMoves,
                     boolean needTransition, MethodType methodType, String name) {
        this.addr = addr;
        this.abi = Objects.requireNonNull(abi);
        this.argMoves = Objects.requireNonNull(argMoves);
        this.returnMoves = Objects.requireNonNull(returnMoves);
        this.needTransition = needTransition;
        this.methodType = methodType;
        this.name = name;
    }

    static NativeEntryPoint make(long addr, ABIDescriptor abi,VMStorage[] argMoves, VMStorage[] returnMoves,
                                 boolean needTransition, MethodType methodType) {
        if (returnMoves.length > 1) {
            throw new IllegalArgumentException("Multiple register return not supported");
        }

        return new NativeEntryPoint(
            addr, abi, argMoves, returnMoves, needTransition, methodType, "native_call");
    }
}
