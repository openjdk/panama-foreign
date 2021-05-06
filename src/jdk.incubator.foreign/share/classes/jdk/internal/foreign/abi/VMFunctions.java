/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static jdk.incubator.foreign.CLinker.C_LONG_LONG;
import static jdk.incubator.foreign.CLinker.C_POINTER;

/**
 * This class is used to setup downcall method handles which refer to commonly used functions within the JVM.
 * A memory segment is allocated, with enough room to contain as many pointers as the number of constants
 * defined in {@link FunctionName}. This segment is then filled by the JVM with function pointers which target
 * the desired functions.
 */
class VMFunctions {

    /**
     * The order of these constants has to match that in which the VM will fill the {@code vmFunctions} pointer array.
     */
    enum FunctionName {
        MALLOC,
        FREE;

        MemoryAddress get() {
            return MemoryAccess.getAddressAtIndex(vmFunctions, ordinal());
        }
    }

    private static final CLinker linker = SharedUtils.getSystemLinker();
    private static final MemorySegment vmFunctions;

    static {
        vmFunctions = MemorySegment.allocateNative(
                MemoryLayouts.ADDRESS.byteSize() * FunctionName.values().length,
                ResourceScope.newImplicitScope());
        initVMFunctions(vmFunctions.address().toRawLongValue());
    }

    static final MethodHandle MH_MALLOC = linker.downcallHandle(FunctionName.MALLOC.get(),
            MethodType.methodType(MemoryAddress.class, long.class),
            FunctionDescriptor.of(C_POINTER, C_LONG_LONG));

    static final MethodHandle MH_FREE = linker.downcallHandle(FunctionName.FREE.get(),
            MethodType.methodType(void.class, MemoryAddress.class),
            FunctionDescriptor.ofVoid(C_POINTER));

    static native void initVMFunctions(long address);
}
