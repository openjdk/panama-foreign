/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @modules jdk.incubator.foreign/jdk.incubator.foreign.unsafe
 *          jdk.incubator.foreign/jdk.internal.foreign
 *          jdk.incubator.foreign/jdk.internal.foreign.abi
 *          java.base/sun.security.action
 * @build NativeTestHelper CallGeneratorHelper TestDowncall
 *
 * @run testng/othervm -Djdk.internal.foreign.NativeInvoker.FASTPATH=none TestDowncall
 * @run testng/othervm TestDowncall
 */

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.SystemABI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.testng.annotations.*;
import static org.testng.Assert.*;

public class TestDowncall extends CallGeneratorHelper {

    static LibraryLookup lib = LibraryLookup.ofLibrary(MethodHandles.lookup(), "TestDowncall");
    static SystemABI abi = SystemABI.getInstance();


    @Test(dataProvider="functions", dataProviderClass=CallGeneratorHelper.class)
    public void testDowncall(String fName, Ret ret, List<ParamType> paramTypes, List<StructFieldType> fields) throws Throwable {
        List<Consumer<Object>> checks = new ArrayList<>();
        MemoryAddress addr = lib.lookup(fName);
        MethodHandle mh = abi.downcallHandle(addr, methodType(ret, paramTypes, fields), function(ret, paramTypes, fields));
        Object[] args = makeArgs(paramTypes, fields, checks);
        mh = mh.asSpreader(Object[].class, paramTypes.size());
        Object res = mh.invoke(args);
        if (ret == Ret.NON_VOID) {
            checks.forEach(c -> c.accept(res));
        }
        for (Object arg : args) {
            cleanup(arg);
        }
    }

    static MethodType methodType(Ret ret, List<ParamType> params, List<StructFieldType> fields) {
        MethodType mt = ret == Ret.VOID ?
                MethodType.methodType(void.class) : MethodType.methodType(paramCarrier(params.get(0).layout(fields)));
        for (ParamType p : params) {
            mt = mt.appendParameterTypes(paramCarrier(p.layout(fields)));
        }
        return mt;
    }

    static FunctionDescriptor function(Ret ret, List<ParamType> params, List<StructFieldType> fields) {
        MemoryLayout[] paramLayouts = params.stream().map(p -> p.layout(fields)).toArray(MemoryLayout[]::new);
        return ret == Ret.VOID ?
                FunctionDescriptor.ofVoid(paramLayouts) :
                FunctionDescriptor.of(paramLayouts[0], paramLayouts);
    }

    static Object[] makeArgs(List<ParamType> params, List<StructFieldType> fields, List<Consumer<Object>> checks) throws ReflectiveOperationException {
        Object[] args = new Object[params.size()];
        for (int i = 0 ; i < params.size() ; i++) {
            args[i] = makeArg(params.get(i).layout(fields), checks, i == 0);
        }
        return args;
    }
}
