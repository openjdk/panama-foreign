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

import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.SharedUtils;
import jdk.internal.foreign.abi.UpcallStubs;
import jdk.internal.foreign.abi.aarch64.AArch64ABI;
import jdk.internal.foreign.abi.x64.sysv.SysVx64ABI;
import jdk.internal.foreign.abi.x64.windows.Windowsx64ABI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Optional;

/**
 * This class models a system application binary interface (ABI).
 *
 * Instances of this class can be obtained by calling {@link Foreign#getSystemABI()}
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
     * memory layout attribute key for abi native type
     */
    String NATIVE_TYPE = "abi/native-type";

    /**
     * Obtain a method handle which can be used to call a given native function.
     *
     * @param symbol downcall symbol.
     * @param type the method type.
     * @param function the function descriptor.
     * @return the downcall method handle.
     */
    MethodHandle downcallHandle(MemoryAddress symbol, MethodType type, FunctionDescriptor function);

    /**
     * Obtain the pointer to a native stub which
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

    enum Type {
        /**
         * The {@code _Bool} native type.
         */
        BOOL,

        /**
         * The {@code unsigned char} native type.
         */
        UNSIGNED_CHAR,

        /**
         * The {@code signed char} native type.
         */
        SIGNED_CHAR,

        /**
         * The {@code char} native type.
         */
        CHAR,

        /**
         * The {@code short} native type.
         */
        SHORT,

        /**
         * The {@code unsigned short} native type.
         */
        UNSIGNED_SHORT,

        /**
         * The {@code int} native type.
         */
        INT,

        /**
         * The {@code unsigned int} native type.
         */
        UNSIGNED_INT,

        /**
         * The {@code long} native type.
         */
        LONG,

        /**
         * The {@code unsigned long} native type.
         */
        UNSIGNED_LONG,

        /**
         * The {@code long long} native type.
         */
        LONG_LONG,

        /**
         * The {@code unsigned long long} native type.
         */
        UNSIGNED_LONG_LONG,

        /**
         * The {@code float} native type.
         */
        FLOAT,

        /**
         * The {@code double} native type.
         */
        DOUBLE,

        /**
         * The {@code long double} native type.
         */
        LONG_DOUBLE,

        /**
         * The {@code complex long double} native type.
         */
        COMPLEX_LONG_DOUBLE,

        /**
         * The {@code T*} native type.
         */
        POINTER;

        /**
         * Retrieve the ABI type attached to the given layout,
         * or throw an {@code IllegalArgumentException} if there is none
         *
         * @param ml the layout to retrieve the ABI type of
         * @return the retrieved ABI type
         * @throws IllegalArgumentException if the given layout does not have an ABI type attribute
         */
        public static Type fromLayout(MemoryLayout ml) throws IllegalArgumentException {
            return ml.attribute(NATIVE_TYPE)
                     .map(SystemABI.Type.class::cast)
                     .orElseThrow(() -> new IllegalArgumentException("No ABI attribute present"));
        }
    }

    /**
     * Returns memory layout for the given native type if supported by the platform ABI.
     * @param type the native type for which the layout is to be retrieved.
     * @return the layout (if any) associated with {@code type}
     */
    Optional<MemoryLayout> layoutFor(Type type);

    /**
     * Obtain an instance of the system ABI.
     * <p>
     * This method is <em>restricted</em>. Restricted method are unsafe, and, if used incorrectly, their use might crash
     * the JVM crash or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     * @return system ABI.
     * @throws IllegalAccessError if the runtime property {@code foreign.restricted} is not set to either
     * {@code permit}, {@code warn} or {@code debug} (the default value is set to {@code deny}).
     */
    static SystemABI getSystemABI() {
        Utils.checkRestrictedAccess("SystemABI.getSystemABI");
        return SharedUtils.getSystemABI();
    }
}
