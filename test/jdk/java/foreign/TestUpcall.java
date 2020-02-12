/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
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
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm -Djdk.internal.foreign.UpcallHandler.FASTPATH=none TestUpcall
 * @run testng/othervm TestUpcall
 */

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SystemABI;
import jdk.incubator.foreign.ValueLayout;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.insertArguments;
import static jdk.incubator.foreign.MemoryLayouts.C_POINTER;
import static org.testng.Assert.assertEquals;


public class TestUpcall extends CallGeneratorHelper {

    static LibraryLookup lib = LibraryLookup.ofLibrary(MethodHandles.lookup(), "TestUpcall");
    static SystemABI abi = SystemABI.getInstance();
    static final MemoryAddress dummyAddress;
    static final Cleaner cleaner = Cleaner.create();

    static MethodHandle DUMMY;
    static MethodHandle PASS_AND_SAVE;

    static {
        try {
            DUMMY = MethodHandles.lookup().findStatic(TestUpcall.class, "dummy", MethodType.methodType(void.class));
            PASS_AND_SAVE = MethodHandles.lookup().findStatic(TestUpcall.class, "passAndSave", MethodType.methodType(Object.class, Object[].class, AtomicReference.class));

            dummyAddress = abi.upcallStub(DUMMY, FunctionDescriptor.ofVoid());
            cleaner.register(dummyAddress, () -> abi.freeUpcallStub(dummyAddress));
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }


    @Test(dataProvider="functions", dataProviderClass=CallGeneratorHelper.class)
    public void testUpcalls(String fName, Ret ret, List<ParamType> paramTypes, List<StructFieldType> fields) throws Throwable {
        List<Consumer<Object>> returnChecks = new ArrayList<>();
        List<Consumer<Object[]>> argChecks = new ArrayList<>();
        MemoryAddress addr = lib.lookup(fName);
        MethodHandle mh = abi.downcallHandle(addr, methodType(ret, paramTypes, fields), function(ret, paramTypes, fields));
        Object[] args = makeArgs(ret, paramTypes, fields, returnChecks, argChecks);
        mh = mh.asSpreader(Object[].class, paramTypes.size() + 1);
        Object res = mh.invoke(args);
        argChecks.forEach(c -> c.accept(args));
        if (ret == Ret.NON_VOID) {
            returnChecks.forEach(c -> c.accept(res));
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
        mt = mt.appendParameterTypes(MemoryAddress.class); //the callback
        return mt;
    }

    static FunctionDescriptor function(Ret ret, List<ParamType> params, List<StructFieldType> fields) {
        List<MemoryLayout> paramLayouts = params.stream().map(p -> p.layout(fields)).collect(Collectors.toList());
        paramLayouts.add(C_POINTER); // the callback
        MemoryLayout[] layouts = paramLayouts.toArray(new MemoryLayout[0]);
        return ret == Ret.VOID ?
                FunctionDescriptor.ofVoid(layouts) :
                FunctionDescriptor.of(layouts[0], layouts);
    }

    static Object[] makeArgs(Ret ret, List<ParamType> params, List<StructFieldType> fields, List<Consumer<Object>> checks, List<Consumer<Object[]>> argChecks) throws ReflectiveOperationException {
        Object[] args = new Object[params.size() + 1];
        for (int i = 0 ; i < params.size() ; i++) {
            args[i] = makeArg(params.get(i).layout(fields), checks, i == 0);
        }
        args[params.size()] = makeCallback(ret, params, fields, checks, argChecks);
        return args;
    }

    @SuppressWarnings("unchecked")
    static MemoryAddress makeCallback(Ret ret, List<ParamType> params, List<StructFieldType> fields, List<Consumer<Object>> checks, List<Consumer<Object[]>> argChecks) {
        if (params.isEmpty()) {
            return dummyAddress;
        }

        AtomicReference<Object[]> box = new AtomicReference<>();
        MethodHandle mh = insertArguments(PASS_AND_SAVE, 1, box);
        mh = mh.asCollector(Object[].class, params.size());

        for (int i = 0; i < params.size(); i++) {
            ParamType pt = params.get(i);
            MemoryLayout layout = pt.layout(fields);
            Class<?> carrier = paramCarrier(layout);
            mh = mh.asType(mh.type().changeParameterType(i, carrier));

            final int finalI = i;
            if (carrier == MemorySegment.class) {
                argChecks.add(o -> assertStructEquals((MemorySegment) o[finalI], (MemorySegment) box.get()[finalI], layout));
            } else {
                argChecks.add(o -> assertEquals(o[finalI], box.get()[finalI]));
            }
        }

        ParamType firstParam = params.get(0);
        MemoryLayout firstlayout = firstParam.layout(fields);
        Class<?> firstCarrier = paramCarrier(firstlayout);

        if (firstCarrier == MemorySegment.class) {
            checks.add(o -> assertStructEquals((MemorySegment) o, (MemorySegment) box.get()[0], firstlayout));
        } else {
            checks.add(o -> assertEquals(o, box.get()[0]));
        }

        mh = mh.asType(mh.type().changeReturnType(ret == Ret.VOID ? void.class : firstCarrier));

        MemoryLayout[] paramLayouts = params.stream().map(p -> p.layout(fields)).toArray(MemoryLayout[]::new);
        FunctionDescriptor func = ret != Ret.VOID
                ? FunctionDescriptor.of(firstlayout, paramLayouts)
                : FunctionDescriptor.ofVoid(paramLayouts);
        MemoryAddress stub = abi.upcallStub(mh, func);
        cleaner.register(stub, () -> abi.freeUpcallStub(stub));
        return stub;
    }

    private static void assertStructEquals(MemorySegment s1, MemorySegment s2, MemoryLayout layout) {
        assertEquals(s1.byteSize(), s2.byteSize());
        GroupLayout g = (GroupLayout) layout;
        for (MemoryLayout field : g.memberLayouts()) {
            if (field instanceof ValueLayout) {
                VarHandle vh = g.varHandle(vhCarrier(field), MemoryLayout.PathElement.groupElement(field.name().orElseThrow()));
                assertEquals(vh.get(s1.baseAddress()), vh.get(s2.baseAddress()));
            }
        }
    }

    private static Class<?> vhCarrier(MemoryLayout layout) {
        if (layout instanceof ValueLayout) {
            if (isIntegral(layout)) {
                if (layout.bitSize() == 64) {
                    return long.class;
                }
                return int.class;
            } else if (layout.bitSize() == 32) {
                return float.class;
            }
            return double.class;
        } else {
            throw new IllegalStateException("Unexpected layout: " + layout);
        }
    }

    static Object passAndSave(Object[] o, AtomicReference<Object[]> ref) {
        ref.set(o);
        return o[0];
    }

    static void dummy() {
        //do nothing
    }
}
