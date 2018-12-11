/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.foreign.abi;

import java.foreign.Library;
import java.foreign.NativeMethodType;
import java.foreign.layout.Layout;
import java.lang.invoke.MethodHandle;
import java.util.Collection;
import jdk.internal.foreign.abi.x64.sysv.SysVx64ABI;

/**
 * This class models a system application binary interface (ABI).
 */
public interface SystemABI {
    /**
     * This enum represents the set of basic (C) types that should be supported
     * by the system ABI.
     */
    enum CType {
        Bool,
        Char,
        SignedChar,
        Short,
        Int,
        Long,
        LongLong,
        UnsignedChar,
        UnsignedShort,
        UnsignedInt,
        UnsignedLong,
        UnsignedLongLong,
        Pointer,
        Float,
        Double,
        LongDouble
    }

    /**
     * Query the ABI-specific layout of given basic type. Information such as size
     * and alignment can be obtained by inspecting the result layout (including its layout annotations).
     */
    Layout layoutOf(CType type);

    /**
     * Obtain a method handle which can be used to call a given native function,
     * given default calling covention.
     *
     * This is equivalent to:
     * downcallHandle(defaultCallingConvention(), addr, nmt, name)
     */
    default MethodHandle downcallHandle(Library.Symbol symbol, java.foreign.NativeMethodType nmt) {
        return downcallHandle(defaultCallingConvention(), symbol, nmt);
    }

    /**
     * Obtain a method handle which can be used to call a given native function,
     * given selected calling convention.
     */
    MethodHandle downcallHandle(CallingConvention cc, Library.Symbol symbol, java.foreign.NativeMethodType nmt);

    /**
     * Obtain the pointer to a native stub (using default calling convention) which
     * can be used to upcall into a given method handle.
     *
     * This is equivalent to:
     * upcallStub(defaultCallingConvention(), receiver, ret, args)
     */
    default Library.Symbol upcallStub(MethodHandle target, java.foreign.NativeMethodType nmt) {
        return upcallStub(defaultCallingConvention(), target, nmt);
    }

    /**
     * Obtain the pointer to a native stub (using selected calling convention) which
     * can be used to upcall into a given method handle.
     */
    Library.Symbol upcallStub(CallingConvention cc, MethodHandle target, NativeMethodType nmt);

    /**
     * Release the native stub.
     */
    void freeUpcallStub(Library.Symbol stub);

    /**
     * Query standard calling convention used by this platform ABI.
     */
    CallingConvention defaultCallingConvention();

    /**
     * Obtain given calling convention by name (if available).
     */
    CallingConvention namedCallingConvention(String name) throws IllegalArgumentException;

    /**
     * Query list of supported calling conventions.
     */
    Collection<CallingConvention> callingConventions();

    /**
     * A calling convention specifies how arguments and return types are communicated
     * from caller to callee.
     */
    interface CallingConvention {
        String name();
    }

    static SystemABI getInstance() {
        // FIXME: Either re-introduce system specific class like Host.java we had
        // or code up factory method based on system properties.
        return SysVx64ABI.getInstance();
    }
}
