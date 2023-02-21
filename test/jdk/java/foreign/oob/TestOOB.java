/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/*
 * @test
 * @enablePreview
 * @library ../
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64" | os.arch == "riscv64"
 * @modules java.base/jdk.internal.foreign
 * @run testng/othervm --enable-native-access=ALL-UNNAMED TestOOB
 */

import org.testng.annotations.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.invoke.MethodHandle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class TestOOB extends NativeTestHelper {
    static {
        System.loadLibrary("OOB");
    }

    @Test
    public void testOOB() throws Throwable {
        FunctionDescriptor upcallDesc = FunctionDescriptor.of(
            S5, C_LONG_LONG, C_POINTER, S3, C_SHORT, C_POINTER, C_CHAR, S1, C_POINTER,
            S4, C_CHAR, C_LONG_LONG, C_CHAR, C_LONG_LONG, C_CHAR, C_SHORT, S5,
            C_LONG_LONG, S6, C_FLOAT, C_FLOAT, U2, C_DOUBLE, C_LONG_LONG, C_DOUBLE, C_FLOAT);
        FunctionDescriptor downcallDesc = upcallDesc.insertArgumentLayouts(0, C_POINTER); // CB
        try (Arena arena = Arena.ofConfined()) {
            TestValue[] testArgs = genTestArgs(upcallDesc, arena);

            MethodHandle downcallHandle = downcallHandle("F85", downcallDesc);
            Object[] args = new Object[downcallDesc.argumentLayouts().size() + 1]; // +1 for return allocator
            AtomicReference<Object[]> returnBox = new AtomicReference<>();
            int returnIdx = 15;
            int argIdx = 0;
            args[argIdx++] = arena;
            args[argIdx++] = makeArgSaverCB(upcallDesc, arena, returnBox, returnIdx);
            for (TestValue testArg : testArgs) {
                args[argIdx++] = testArg.value();
            }

            MemorySegment returned = (MemorySegment) downcallHandle.invokeWithArguments(args);

            testArgs[returnIdx].check().accept(returned);

            Object[] capturedArgs = returnBox.get();
            for (int i = 0; i < testArgs.length; i++) {
                testArgs[i].check().accept(capturedArgs[i]);
            }
        }
    }

    static final StructLayout S1 = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(3, C_FLOAT).withName("f0")
    ).withName("S1");
    static final UnionLayout U2 = MemoryLayout.unionLayout(
        C_INT.withName("f0"),
        MemoryLayout.sequenceLayout(4, C_POINTER).withName("f1"),
        C_POINTER.withName("f2")
    ).withName("U2");
    static final UnionLayout U1 = MemoryLayout.unionLayout(
        C_CHAR.withName("f0")
    ).withName("U1");
    static final StructLayout S2 = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(3, MemoryLayout.sequenceLayout(3, C_CHAR)).withName("f0")
    ).withName("S2");
    static final StructLayout S3 = MemoryLayout.structLayout(
        S2,
        MemoryLayout.paddingLayout(56),
        C_LONG_LONG.withName("f1"),
        U1,
        MemoryLayout.paddingLayout(8),
        MemoryLayout.sequenceLayout(2, C_SHORT).withName("f3"),
        MemoryLayout.paddingLayout(16)
    ).withName("S3");
    static final StructLayout S4 = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(3, C_SHORT).withName("f0")
    ).withName("S4");
    static final StructLayout S5 = MemoryLayout.structLayout(
        C_INT.withName("f0"),
        C_FLOAT.withName("f1"),
        C_SHORT.withName("f2"),
        C_SHORT.withName("f3")
    ).withName("S5");
    static final StructLayout S6 = MemoryLayout.structLayout(
        C_DOUBLE.withName("f0"),
        C_INT.withName("f1"),
        C_FLOAT.withName("f2"),
        C_LONG_LONG.withName("f3")
    ).withName("S6");

}
