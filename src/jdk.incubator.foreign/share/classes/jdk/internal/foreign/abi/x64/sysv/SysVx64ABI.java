/*
 * Copyright (c) 2016, 2020, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.*;
import jdk.internal.foreign.abi.x64.ArgumentClassImpl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.*;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;
import static jdk.incubator.foreign.MemoryLayouts.SysV.*;

/**
 * ABI implementation based on System V ABI AMD64 supplement v.0.99.6
 */
public class SysVx64ABI implements SystemABI {
    public static final int MAX_INTEGER_ARGUMENT_REGISTERS = 6;
    public static final int MAX_INTEGER_RETURN_REGISTERS = 2;
    public static final int MAX_VECTOR_ARGUMENT_REGISTERS = 8;
    public static final int MAX_VECTOR_RETURN_REGISTERS = 2;
    public static final int MAX_X87_RETURN_REGISTERS = 2;

    private static final String fastPath = privilegedGetProperty("jdk.internal.foreign.NativeInvoker.FASTPATH");
    private static SysVx64ABI instance;

    public static SysVx64ABI getInstance() {
        if (instance == null) {
            instance = new SysVx64ABI();
        }
        return instance;
    }

    @Override
    public MethodHandle downcallHandle(MemoryAddress symbol, MethodType type, FunctionDescriptor function) {
        return CallArranger.arrangeDowncall(MemoryAddressImpl.addressof(symbol), type, function);
    }

    @Override
    public MemoryAddress upcallStub(MethodHandle target, FunctionDescriptor function) {
        return UpcallStubs.upcallAddress(CallArranger.arrangeUpcall(target, target.type(), function));
    }

    @Override
    public String name() {
        return SystemABI.ABI_SYSV;
    }

    @Override
    public Optional<MemoryLayout> layoutFor(Type type) {
        return switch (Objects.requireNonNull(type)) {
            case BOOL -> Optional.of(C_BOOL);
            case UNSIGNED_CHAR -> Optional.of(C_UCHAR);
            case SIGNED_CHAR -> Optional.of(C_SCHAR);
            case CHAR -> Optional.of(C_CHAR);
            case SHORT -> Optional.of(C_SHORT);
            case UNSIGNED_SHORT -> Optional.of(C_USHORT);
            case INT -> Optional.of(C_INT);
            case UNSIGNED_INT -> Optional.of(C_UINT);
            case LONG -> Optional.of(C_LONG);
            case UNSIGNED_LONG -> Optional.of(C_ULONG);
            case LONG_LONG -> Optional.of(C_LONGLONG);
            case UNSIGNED_LONG_LONG -> Optional.of(C_ULONGLONG);
            case FLOAT -> Optional.of(C_FLOAT);
            case DOUBLE -> Optional.of(C_DOUBLE);
            case LONG_DOUBLE -> Optional.of(C_LONGDOUBLE);
            case COMPLEX_LONG_DOUBLE -> Optional.of(C_COMPLEX_LONGDOUBLE);
            case POINTER -> Optional.of(C_POINTER);
            default -> Optional.empty();
        };
    }

    static ArgumentClassImpl argumentClassFor(Type type) {
        return switch (Objects.requireNonNull(type)) {
            case BOOL, UNSIGNED_CHAR, SIGNED_CHAR, CHAR, SHORT, UNSIGNED_SHORT,
                INT, UNSIGNED_INT, LONG, UNSIGNED_LONG, LONG_LONG, UNSIGNED_LONG_LONG ->
                    ArgumentClassImpl.INTEGER;
            case FLOAT, DOUBLE -> ArgumentClassImpl.SSE;
            case LONG_DOUBLE -> ArgumentClassImpl.X87;
            case COMPLEX_LONG_DOUBLE -> ArgumentClassImpl.COMPLEX_X87;
            case POINTER -> ArgumentClassImpl.POINTER;
            default -> null;
        };
    }
}
