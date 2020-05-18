/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
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

import jdk.incubator.foreign.ForeignLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.ValueLayout;
import jdk.internal.foreign.MemoryAddressImpl;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.aarch64.AArch64Linker;
import jdk.internal.foreign.abi.x64.sysv.SysVx64Linker;
import jdk.internal.foreign.abi.x64.windows.Windowsx64Linker;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.invoke.MethodHandles.collectArguments;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.permuteArguments;
import static java.lang.invoke.MethodType.methodType;

public class SharedUtils {

    private static final MethodHandle MH_ALLOC_BUFFER;
    private static final MethodHandle MH_BASEADDRESS;
    private static final MethodHandle MH_BUFFER_COPY;

    static {
        try {
            var lookup = MethodHandles.lookup();
            MH_ALLOC_BUFFER = lookup.findStatic(SharedUtils.class, "allocateNative",
                    methodType(MemorySegment.class, MemoryLayout.class));
            MH_BASEADDRESS = lookup.findVirtual(MemorySegment.class, "baseAddress",
                    methodType(MemoryAddress.class));
            MH_BUFFER_COPY = lookup.findStatic(SharedUtils.class, "bufferCopy",
                    methodType(MemoryAddress.class, MemoryAddress.class, MemorySegment.class));
        } catch (ReflectiveOperationException e) {
            throw new BootstrapMethodError(e);
        }
    }

    // workaround for https://bugs.openjdk.java.net/browse/JDK-8239083
    private static MemorySegment allocateNative(MemoryLayout layout) {
        return MemorySegment.allocateNative(layout);
    }

    /**
     * Align the specified type from a given address
     * @return The address the data should be at based on alignment requirement
     */
    public static long align(MemoryLayout t, boolean isVar, long addr) {
        return alignUp(addr, alignment(t, isVar));
    }

    public static long alignUp(long addr, long alignment) {
        return ((addr - 1) | (alignment - 1)) + 1;
    }

    /**
     * The alignment requirement for a given type
     * @param isVar indicate if the type is a standalone variable. This change how
     * array is aligned. for example.
     */
    public static long alignment(MemoryLayout t, boolean isVar) {
        if (t instanceof ValueLayout) {
            return alignmentOfScalar((ValueLayout) t);
        } else if (t instanceof SequenceLayout) {
            // when array is used alone
            return alignmentOfArray((SequenceLayout) t, isVar);
        } else if (t instanceof GroupLayout) {
            return alignmentOfContainer((GroupLayout) t);
        } else if (t.isPadding()) {
            return 1;
        } else {
            throw new IllegalArgumentException("Invalid type: " + t);
        }
    }

    private static long alignmentOfScalar(ValueLayout st) {
        return st.byteSize();
    }

    private static long alignmentOfArray(SequenceLayout ar, boolean isVar) {
        if (ar.elementCount().orElseThrow() == 0) {
            // VLA or incomplete
            return 16;
        } else if ((ar.byteSize()) >= 16 && isVar) {
            return 16;
        } else {
            // align as element type
            MemoryLayout elementType = ar.elementLayout();
            return alignment(elementType, false);
        }
    }

    private static long alignmentOfContainer(GroupLayout ct) {
        // Most strict member
        return ct.memberLayouts().stream().mapToLong(t -> alignment(t, false)).max().orElse(1);
    }

    /**
     * Takes a MethodHandle that takes an input buffer as a first argument (a MemoryAddress), and returns nothing,
     * and adapts it to return a MemorySegment, by allocating a MemorySegment for the input
     * buffer, calling the target MethodHandle, and then returning the allocated MemorySegment.
     *
     * This allows viewing a MethodHandle that makes use of in memory return (IMR) as a MethodHandle that just returns
     * a MemorySegment without requiring a pre-allocated buffer as an explicit input.
     *
     * @param handle the target handle to adapt
     * @param cDesc the function descriptor of the native function (with actual return layout)
     * @return the adapted handle
     */
    public static MethodHandle adaptDowncallForIMR(MethodHandle handle, FunctionDescriptor cDesc) {
        if (handle.type().returnType() != void.class)
            throw new IllegalArgumentException("return expected to be void for in memory returns");
        if (handle.type().parameterType(0) != MemoryAddress.class)
            throw new IllegalArgumentException("MemoryAddress expected as first param");
        if (cDesc.returnLayout().isEmpty())
            throw new IllegalArgumentException("Return layout needed: " + cDesc);

        MethodHandle ret = identity(MemorySegment.class); // (MemorySegment) MemorySegment
        handle = collectArguments(ret, 1, handle); // (MemorySegment, MemoryAddress ...) MemorySegment
        handle = collectArguments(handle, 1, MH_BASEADDRESS); // (MemorySegment, MemorySegment ...) MemorySegment
        MethodType oldType = handle.type(); // (MemorySegment, MemorySegment, ...) MemorySegment
        MethodType newType = oldType.dropParameterTypes(0, 1); // (MemorySegment, ...) MemorySegment
        int[] reorder = IntStream.range(-1, newType.parameterCount()).toArray();
        reorder[0] = 0; // [0, 0, 1, 2, 3, ...]
        handle = permuteArguments(handle, newType, reorder); // (MemorySegment, ...) MemoryAddress
        handle = collectArguments(handle, 0, insertArguments(MH_ALLOC_BUFFER, 0, cDesc.returnLayout().get())); // (...) MemoryAddress

        return handle;
    }

    /**
     * Takes a MethodHandle that returns a MemorySegment, and adapts it to take an input buffer as a first argument
     * (a MemoryAddress), and upon invocation, copies the contents of the returned MemorySegment into the input buffer
     * passed as the first argument.
     *
     * @param target the target handle to adapt
     * @return the adapted handle
     */
    public static MethodHandle adaptUpcallForIMR(MethodHandle target) {
        if (target.type().returnType() != MemorySegment.class)
            throw new IllegalArgumentException("Must return MemorySegment for IMR");

        target = collectArguments(MH_BUFFER_COPY, 1, target); // (MemoryAddress, ...) MemoryAddress

        return target;
    }

    private static MemoryAddress bufferCopy(MemoryAddress dest, MemorySegment buffer) {
        MemoryAddressImpl.ofLongUnchecked(dest.toRawLongValue(), buffer.byteSize())
                .segment().copyFrom(buffer);
        return dest;
    }

    private static void checkCompatibleType(Class<?> carrier, MemoryLayout layout, long addressSize) {
        if (carrier.isPrimitive()) {
            Utils.checkPrimitiveCarrierCompat(carrier, layout);
        } else if (carrier == MemoryAddress.class) {
            Utils.checkLayoutType(layout, ValueLayout.class);
            if (layout.bitSize() != addressSize)
                throw new IllegalArgumentException("Address size mismatch: " + addressSize + " != " + layout.bitSize());
        } else if(carrier == MemorySegment.class) {
           Utils.checkLayoutType(layout, GroupLayout.class);
        } else {
            throw new IllegalArgumentException("Unsupported carrier: " + carrier);
        }
    }

    public static void checkFunctionTypes(MethodType mt, FunctionDescriptor cDesc, long addressSize) {
        if (mt.returnType() == void.class != cDesc.returnLayout().isEmpty())
            throw new IllegalArgumentException("Return type mismatch: " + mt + " != " + cDesc);
        List<MemoryLayout> argLayouts = cDesc.argumentLayouts();
        if (mt.parameterCount() != argLayouts.size())
            throw new IllegalArgumentException("Arity mismatch: " + mt + " != " + cDesc);

        int paramCount = mt.parameterCount();
        for (int i = 0; i < paramCount; i++) {
            checkCompatibleType(mt.parameterType(i), argLayouts.get(i), addressSize);
        }
        cDesc.returnLayout().ifPresent(rl -> checkCompatibleType(mt.returnType(), rl, addressSize));
    }

    public static Class<?> primitiveCarrierForSize(long size) {
        if (size == 1) {
            return byte.class;
        } else if(size == 2) {
            return short.class;
        } else if (size <= 4) {
            return int.class;
        } else if (size <= 8) {
            return long.class;
        }

        throw new IllegalArgumentException("Size too large: " + size);
    }

    public static ForeignLinker getSystemLinker() {
        String arch = System.getProperty("os.arch");
        String os = System.getProperty("os.name");
        if (arch.equals("amd64") || arch.equals("x86_64")) {
            if (os.startsWith("Windows")) {
                return Windowsx64Linker.getInstance();
            } else {
                return SysVx64Linker.getInstance();
            }
        } else if (arch.equals("aarch64")) {
            return AArch64Linker.getInstance();
        }
        throw new UnsupportedOperationException("Unsupported os or arch: " + os + ", " + arch);
    }
}
