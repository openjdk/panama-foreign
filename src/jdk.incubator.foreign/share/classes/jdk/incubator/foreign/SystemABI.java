/*
 *  Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */
package jdk.incubator.foreign;

import jdk.internal.foreign.abi.UpcallStubs;
import jdk.internal.foreign.abi.aarch64.AArch64ABI;
import jdk.internal.foreign.abi.x64.sysv.SysVx64ABI;
import jdk.internal.foreign.abi.x64.windows.Windowsx64ABI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * This class models a system application binary interface (ABI).
 */
public interface SystemABI {

    /**
     * The name of the SysV ABI
     */
    String ABI_SYSV = "SysV";

    /**
     * The name of the Windows ABI
     */
    String ABI_WINDOWS = "Windows";

    /**
     * The name of the AArch64 ABI
     */
    String ABI_AARCH64 = "AArch64";

    /**
     * Obtain a method handle which can be used to call a given native function,
     * given default calling covention.
     *
     * @param symbol downcall symbol.
     * @param type the method type.
     * @param function the function descriptor.
     * @return the downcall method handle.
     */
    MethodHandle downcallHandle(MemoryAddress symbol, MethodType type, FunctionDescriptor function);

    /**
     * Obtain the pointer to a native stub (using default calling convention) which
     * can be used to upcall into a given method handle.
     *
     * @param target the target method handle.
     * @param function the function descriptor.
     * @return the upcall symbol.
     */
    MemoryAddress upcallStub(MethodHandle target, FunctionDescriptor function);

    /**
     * Frees an upcall stub given it's memory address.
     *
     * @param address the memory address of the upcall stub, returned from
     *                {@link SystemABI#upcallStub(MethodHandle, FunctionDescriptor)}.
     * @throws IllegalArgumentException if the given address is not a valid upcall stub address.
     */
    default void freeUpcallStub(MemoryAddress address) {
        UpcallStubs.freeUpcallStub(address);
    }

    /**
     * Returns the name of this ABI.
     *
     * @return the name
     */
    String name();

    /**
     * Obtain an instance of the system ABI.
     * @return system ABI.
     */
    static SystemABI getInstance() {
        String arch = System.getProperty("os.arch");
        String os = System.getProperty("os.name");
        if (arch.equals("amd64") || arch.equals("x86_64")) {
            if (os.startsWith("Windows")) {
                return Windowsx64ABI.getInstance();
            } else {
                return SysVx64ABI.getInstance();
            }
        } else if (arch.equals("aarch64")) {
            return AArch64ABI.getInstance();
        }
        throw new UnsupportedOperationException("Unsupported os or arch: " + os + ", " + arch);
    }
}
