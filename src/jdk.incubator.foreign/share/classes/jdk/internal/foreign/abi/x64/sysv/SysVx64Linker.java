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

import jdk.incubator.foreign.ForeignLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.internal.foreign.abi.UpcallStubs;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Optional;

import static jdk.incubator.foreign.CSupport.*;

/**
 * ABI implementation based on System V ABI AMD64 supplement v.0.99.6
 */
public class SysVx64Linker implements ForeignLinker {
    public static final int MAX_INTEGER_ARGUMENT_REGISTERS = 6;
    public static final int MAX_INTEGER_RETURN_REGISTERS = 2;
    public static final int MAX_VECTOR_ARGUMENT_REGISTERS = 8;
    public static final int MAX_VECTOR_RETURN_REGISTERS = 2;
    public static final int MAX_X87_RETURN_REGISTERS = 2;

    private static SysVx64Linker instance;

    static final long ADDRESS_SIZE = 64; // bits

    public static SysVx64Linker getInstance() {
        if (instance == null) {
            instance = new SysVx64Linker();
        }
        return instance;
    }

    @Override
    public MethodHandle downcallHandle(MemoryAddress symbol, MethodType type, FunctionDescriptor function) {
        return CallArranger.arrangeDowncall(symbol, type, function);
    }

    @Override
    public MemorySegment upcallStub(MethodHandle target, FunctionDescriptor function) {
        return UpcallStubs.upcallAddress(CallArranger.arrangeUpcall(target, target.type(), function));
    }

    @Override
    public String name() {
        return SysV.NAME;
    }

    static Optional<ArgumentClassImpl> argumentClassFor(MemoryLayout layout) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Optional<SysV.ArgumentClass> argClassOpt =
                (Optional<SysV.ArgumentClass>)(Optional)layout.attribute(SysV.CLASS_ATTRIBUTE_NAME);
        return argClassOpt.map(argClass -> switch (argClass) {
            case INTEGER -> ArgumentClassImpl.INTEGER;
            case SSE -> ArgumentClassImpl.SSE;
            case X87 -> ArgumentClassImpl.X87;
            case COMPLEX_87 -> ArgumentClassImpl.COMPLEX_X87;
            case POINTER -> ArgumentClassImpl.POINTER;
            default -> null;
        });
    }
}
