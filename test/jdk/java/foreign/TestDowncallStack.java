/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @modules jdk.incubator.foreign/jdk.internal.foreign
 * @build NativeTestHelper CallGeneratorHelper TestDowncallStack
 *
 * @run testng/othervm -XX:+IgnoreUnrecognizedVMOptions -XX:-VerifyDependencies
 *   --enable-native-access=ALL-UNNAMED -Dgenerator.sample.factor=17
 *   TestDowncallStack
 */

import jdk.incubator.foreign.Addressable;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SegmentAllocator;
import jdk.incubator.foreign.SymbolLookup;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;

public class TestDowncallStack extends CallGeneratorHelper {

    static CLinker abi = CLinker.systemCLinker();
    static {
        System.loadLibrary("TestDowncallStack");
    }

    static final SymbolLookup LOOKUP = SymbolLookup.loaderLookup();

    @Test(dataProvider="functions", dataProviderClass=CallGeneratorHelper.class)
    public void testDowncall(int count, String fName, Ret ret, List<ParamType> paramTypes, List<StructFieldType> fields) throws Throwable {
        List<Consumer<Object>> checks = new ArrayList<>();
        MemoryAddress addr = LOOKUP.lookup("s" + fName).get();
        FunctionDescriptor descriptor = function(ret, paramTypes, fields);
        Object[] args = makeArgs(paramTypes, fields, checks);
        try (NativeScope scope = new NativeScope()) {
            boolean needsScope = descriptor.returnLayout().map(l -> l instanceof GroupLayout).orElse(false);
            Object res = doCall(addr, scope, descriptor, args);
            if (ret == Ret.NON_VOID) {
                checks.forEach(c -> c.accept(res));
                if (needsScope) {
                    // check that return struct has indeed been allocated in the native scope
                    assertEquals(((MemorySegment) res).scope(), scope.scope());
                    assertEquals(scope.allocatedBytes(), descriptor.returnLayout().get().byteSize());
                } else {
                    // if here, there should be no allocation through the scope!
                    assertEquals(scope.allocatedBytes(), 0L);
                }
            } else {
                // if here, there should be no allocation through the scope!
                assertEquals(scope.allocatedBytes(), 0L);
            }
        }
    }

    Object doCall(Addressable addr, SegmentAllocator allocator, FunctionDescriptor descriptor, Object[] args) throws Throwable {
        MethodHandle mh = downcallHandle(abi, addr, allocator, descriptor);
        Object res = mh.invokeWithArguments(args);
        return res;
    }

    static FunctionDescriptor function(Ret ret, List<ParamType> params, List<StructFieldType> fields) {
        Stream<MemoryLayout> prefixLongs = Stream.generate(() -> (MemoryLayout) C_LONG_LONG).limit(8);
        Stream<MemoryLayout> prefixDoubles = Stream.generate(() -> (MemoryLayout)  C_DOUBLE).limit(8);
        Stream<MemoryLayout> prefix = Stream.concat(prefixLongs, prefixDoubles);
        List<MemoryLayout> pLayouts = params.stream().map(p -> p.layout(fields)).toList();
        MemoryLayout[] paramLayouts = Stream.concat(prefix, pLayouts.stream()).toArray(MemoryLayout[]::new);
        return ret == Ret.VOID ?
                FunctionDescriptor.ofVoid(paramLayouts) :
                FunctionDescriptor.of(pLayouts.get(0), paramLayouts);
    }

    static Object[] makeArgs(List<ParamType> params, List<StructFieldType> fields, List<Consumer<Object>> checks) throws ReflectiveOperationException {
        Object[] args = new Object[16 + params.size()];
        int argNum = 0;
        for (int i = 0; i < 8; i++) {
            args[argNum++] = makeArg(C_LONG_LONG, null, false);
        }
        for (int i = 0; i < 8; i++) {
            args[argNum++] = makeArg(C_DOUBLE, null, false);
        }
        for (int i = 0 ; i < params.size() ; i++) {
            args[argNum++] = makeArg(params.get(i).layout(fields), checks, i == 0);
        }
        return args;
    }
}
