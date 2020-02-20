/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019, Arm Limited. All rights reserved.
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
package jdk.internal.foreign.abi.aarch64;

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.abi.*;

import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.function.Function;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;
import static jdk.incubator.foreign.MemoryLayouts.AArch64ABI.*;

/**
 * ABI implementation based on ARM document "Procedure Call Standard for
 * the ARM 64-bit Architecture".
 */
public class AArch64ABI implements SystemABI {
    private static AArch64ABI instance;

    public static AArch64ABI getInstance() {
        if (instance == null) {
            instance = new AArch64ABI();
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
        return SystemABI.ABI_AARCH64;
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
            case POINTER -> Optional.of(C_POINTER);
            default -> Optional.empty();
        };
    }

    static ArgumentClassImpl argumentClassFor(Type type) {
        return switch (Objects.requireNonNull(type)) {
            case BOOL, UNSIGNED_CHAR, SIGNED_CHAR, CHAR, SHORT, UNSIGNED_SHORT,
                INT, UNSIGNED_INT, LONG, UNSIGNED_LONG, LONG_LONG, UNSIGNED_LONG_LONG ->
                    ArgumentClassImpl.INTEGER;
            case FLOAT, DOUBLE -> ArgumentClassImpl.VECTOR;
            case POINTER -> ArgumentClassImpl.POINTER;
            default -> null;
        };
    }
}
