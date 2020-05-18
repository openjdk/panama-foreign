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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * This class can be used to link native functions as a {@link MethodHandle}, or to link Java
 * methods as a native function pointer (modelled as a {@link MemorySegment}).
 *
 * Instances of this interface can be obtained for instance by calling {@link CSupport#getSystemLinker()}
 */
public interface ForeignLinker {
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
     * Allocates a native stub segment which contains executable code to upcall into a given method handle.
     * As such, the base address of the returned stub segment can be passed to other foreign functions
     * (as a function pointer). The returned segment is <em>not</em> thread-confined, and it only features
     * the {@link MemorySegment#CLOSE} access mode. When the returned segment is closed,
     * the corresponding native stub will be deallocated.
     *
     * @param target the target method handle.
     * @param function the function descriptor.
     * @return the native stub segment.
     */
    MemorySegment upcallStub(MethodHandle target, FunctionDescriptor function);

    /**
     * Returns the name of this linker.
     *
     * @return the name
     */
    String name();
}
