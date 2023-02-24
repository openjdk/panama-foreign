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
        FunctionDescriptor upcallDesc = FunctionDescriptor.of(S, S);
        FunctionDescriptor downcallDesc = upcallDesc.insertArgumentLayouts(0, C_POINTER); // CB
        try (Arena arena = Arena.ofConfined()) {
            TestValue[] testArgs = genTestArgs(upcallDesc, arena);

            MethodHandle downcallHandle = downcallHandle("F", downcallDesc);
            Object[] args = new Object[downcallDesc.argumentLayouts().size() + 1]; // +1 for return allocator
            AtomicReference<Object[]> returnBox = new AtomicReference<>();
            int returnIdx = 0;
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

    static final StructLayout S = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(3, C_SHORT).withName("f0")
    ).withName("S");
}
