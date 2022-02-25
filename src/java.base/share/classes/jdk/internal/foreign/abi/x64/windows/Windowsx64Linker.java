/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.NativeSymbol;
import java.lang.foreign.MemorySession;
import java.lang.foreign.VaList;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import jdk.internal.foreign.abi.AbstractLinker;

/**
 * ABI implementation based on Windows ABI AMD64 supplement v.0.99.6
 */
public final class Windowsx64Linker extends AbstractLinker {

    public static final int MAX_INTEGER_ARGUMENT_REGISTERS = 4;
    public static final int MAX_INTEGER_RETURN_REGISTERS = 1;
    public static final int MAX_VECTOR_ARGUMENT_REGISTERS = 4;
    public static final int MAX_VECTOR_RETURN_REGISTERS = 1;
    public static final int MAX_REGISTER_ARGUMENTS = 4;
    public static final int MAX_REGISTER_RETURNS = 1;

    private static Windowsx64Linker instance;

    public static Windowsx64Linker getInstance() {
        if (instance == null) {
            instance = new Windowsx64Linker();
        }
        return instance;
    }

    @Override
    protected MethodHandle arrangeDowncall(MethodType inferredMethodType, FunctionDescriptor function) {
        return CallArranger.arrangeDowncall(inferredMethodType, function);
    }

    @Override
    protected NativeSymbol arrangeUpcall(MethodHandle target, MethodType targetType, FunctionDescriptor function, MemorySession session) {
        return CallArranger.arrangeUpcall(target, targetType, function, session);
    }

    public static VaList newVaList(Consumer<VaList.Builder> actions, MemorySession session) {
        WinVaList.Builder builder = WinVaList.builder(session);
        actions.accept(builder);
        return builder.build();
    }

    public static VaList newVaListOfAddress(MemoryAddress ma, MemorySession session) {
        return WinVaList.ofAddress(ma, session);
    }

    public static VaList emptyVaList() {
        return WinVaList.empty();
    }
}

