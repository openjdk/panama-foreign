/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
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
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @run testng/othervm --enable-native-access=ALL-UNNAMED -Dgenerator.sample.factor=17 TestVarArgs
 */

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.PaddingLayout;
import java.lang.foreign.ValueLayout;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.foreign.MemoryLayout.PathElement.*;
import static org.testng.Assert.*;

public class TestVarArgs extends CallGeneratorHelper {

    static final VarHandle VH_IntArray = C_INT.arrayElementVarHandle();
    static final MethodHandle MH_CHECK;

    static final Linker LINKER = Linker.nativeLinker();
    static {
        System.loadLibrary("VarArgs");
        try {
            MH_CHECK = MethodHandles.lookup().findStatic(TestVarArgs.class, "check",
                    MethodType.methodType(void.class, int.class, MemorySegment.class, List.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static final MemorySegment VARARGS_ADDR = findNativeOrThrow("varargs");
    static final MemorySegment SUM_HFA_FLOATS_ADDR = findNativeOrThrow("sum_struct_hfa_floats");
    static final MemorySegment SUM_HFA_DOUBLES_ADDR = findNativeOrThrow("sum_struct_hfa_doubles");
    static final MemorySegment SUM_SPILLED_STRUCT_INTS_ADDR = findNativeOrThrow("sum_spilled_struct_ints");
    static final MemorySegment SUM_SPILLED_HFA_FLOATS_ADDR = findNativeOrThrow("sum_spilled_struct_hfa_floats");
    static final MemorySegment SUM_SPILLED_HFA_DOUBLES_ADDR = findNativeOrThrow("sum_spilled_struct_hfa_doubles");

    @Test(dataProvider = "functions")
    public void testVarArgs(int count, String fName, Ret ret, // ignore this stuff
                            List<ParamType> paramTypes, List<StructFieldType> fields) throws Throwable {
        List<Arg> args = makeArgs(paramTypes, fields);

        try (Arena arena = Arena.openConfined()) {
            MethodHandle checker = MethodHandles.insertArguments(MH_CHECK, 2, args);
            MemorySegment writeBack = LINKER.upcallStub(checker, FunctionDescriptor.ofVoid(C_INT, C_POINTER), arena.session());
            MemorySegment callInfo = MemorySegment.allocateNative(CallInfo.LAYOUT, arena.session());;
            MemorySegment argIDs = MemorySegment.allocateNative(MemoryLayout.sequenceLayout(args.size(), C_INT), arena.session());;

            MemorySegment callInfoPtr = callInfo;

            CallInfo.writeback(callInfo, writeBack);
            CallInfo.argIDs(callInfo, argIDs);

            for (int i = 0; i < args.size(); i++) {
                VH_IntArray.set(argIDs, (long) i, args.get(i).id.ordinal());
            }

            List<MemoryLayout> argLayouts = new ArrayList<>();
            argLayouts.add(C_POINTER); // call info
            argLayouts.add(C_INT); // size

            FunctionDescriptor baseDesc = FunctionDescriptor.ofVoid(argLayouts.toArray(MemoryLayout[]::new));
            Linker.Option varargIndex = Linker.Option.firstVariadicArg(baseDesc.argumentLayouts().size());
            FunctionDescriptor desc = baseDesc.appendArgumentLayouts(args.stream().map(a -> a.layout).toArray(MemoryLayout[]::new));

            MethodHandle downcallHandle = LINKER.downcallHandle(VARARGS_ADDR, desc, varargIndex);

            List<Object> argValues = new ArrayList<>();
            argValues.add(callInfoPtr); // call info
            argValues.add(args.size());  // size
            args.forEach(a -> argValues.add(a.value));

            downcallHandle.invokeWithArguments(argValues);

            // args checked by upcall
        }
    }

    @DataProvider(name = "variadicStructDescriptions")
    public static Object[][] variadicStructDescriptions() {
        return new Object[][] {
            new Object[] { StructFieldType.FLOAT, 0, SUM_HFA_FLOATS_ADDR },
            new Object[] { StructFieldType.FLOAT, 6, SUM_SPILLED_HFA_FLOATS_ADDR },
            new Object[] { StructFieldType.DOUBLE, 0, SUM_HFA_DOUBLES_ADDR },
            new Object[] { StructFieldType.DOUBLE, 6, SUM_SPILLED_HFA_DOUBLES_ADDR },
            new Object[] { StructFieldType.INT, 6, SUM_SPILLED_STRUCT_INTS_ADDR },
        };
    }

    @Test(dataProvider = "variadicStructDescriptions")
    public void testSumVariadicHfa(StructFieldType structFieldType, int extraIntArgs, MemorySegment foreignFunctionSymbol) throws Throwable {
        assertTrue(structFieldType == StructFieldType.INT ||
            structFieldType == StructFieldType.FLOAT ||
            structFieldType == StructFieldType.DOUBLE);

        int maxFields = structFieldType == StructFieldType.DOUBLE ? 2 : 4;

        for (int num_fields = 1; num_fields <= maxFields; num_fields++) {
            List<StructFieldType> fields = new ArrayList<StructFieldType>();
            for (int i=0; i < num_fields; i++) {
                fields.add(structFieldType);
            }

            try (Arena arena = Arena.openConfined()) {
                GroupLayout groupLayout = (GroupLayout)ParamType.STRUCT.layout(fields);
                MemorySegment structMemorySegment = MemorySegment.allocateNative(groupLayout, arena.session());

                int expectedSumOfFieldsAsInt = 0;
                float expectedSumOfFieldsAsFloat = 0;
                double expectedSumOfFieldsAsDouble = 0;

                int fieldId = 1;
                for (MemoryLayout memberLayout : groupLayout.memberLayouts()) {
                    if (memberLayout instanceof PaddingLayout) continue;

                    assertTrue(memberLayout instanceof ValueLayout);
                    assertTrue(!isPointer(memberLayout));
                    if (isIntegral(memberLayout)) {
                        assertTrue(structFieldType == StructFieldType.INT);
                    }

                    VarHandle accessor = groupLayout.varHandle(MemoryLayout.PathElement.groupElement(memberLayout.name().get()));

                    switch (structFieldType) {
                        case FLOAT: {
                            float fieldValueAsFloat = fieldId * 42.0f;
                            expectedSumOfFieldsAsFloat += fieldValueAsFloat;
                            accessor.set(structMemorySegment, fieldValueAsFloat);
                            break;
                        }
                        case DOUBLE: {
                            double fieldValueAsDouble = fieldId * 51.75;
                            expectedSumOfFieldsAsDouble += fieldValueAsDouble;
                            accessor.set(structMemorySegment, fieldValueAsDouble);
                            break;
                        }
                        case INT: {
                            int fieldValueAsInt = fieldId * 2022;
                            expectedSumOfFieldsAsInt += fieldValueAsInt;
                            accessor.set(structMemorySegment, fieldValueAsInt);
                            break;
                        }
                    }

                    fieldId++;
                }

                List<MemoryLayout> argLayouts = new ArrayList<>();
                argLayouts.add(C_INT); // number of fields
                for (int i=0; i < extraIntArgs; i++) {
                    argLayouts.add(C_INT);
                }

                MemoryLayout resLayout;

                switch (structFieldType) {
                    case FLOAT: resLayout = C_FLOAT; break;
                    case DOUBLE: resLayout = C_DOUBLE; break;
                    case INT: resLayout = C_INT; break;
                    default: throw new UnsupportedOperationException("Unhandled field type " + structFieldType);
                }

                FunctionDescriptor baseDesc = FunctionDescriptor.of(resLayout, argLayouts.toArray(MemoryLayout[]::new));
                Linker.Option varargIndex = Linker.Option.firstVariadicArg(baseDesc.argumentLayouts().size());
                FunctionDescriptor desc = baseDesc.appendArgumentLayouts(groupLayout);

                MethodHandle downcallHandle = LINKER.downcallHandle(foreignFunctionSymbol, desc, varargIndex);

                List<Object> argValues = new ArrayList<>();
                argValues.add(num_fields);

                for (int i=0; i < extraIntArgs; i++) {
                    argValues.add(i+1);
                }

                argValues.add(structMemorySegment);

                Object result = downcallHandle.invokeWithArguments(argValues);

                switch (structFieldType) {
                    case FLOAT: {
                        assertEquals((float)result, expectedSumOfFieldsAsFloat);
                        break;
                    }
                    case DOUBLE: {
                        assertEquals((double)result, expectedSumOfFieldsAsDouble);
                        break;
                    }
                    case INT: {
                        assertEquals((int)result, expectedSumOfFieldsAsInt);
                        break;
                    }
                }
            }
        }
    }

    private static List<Arg> makeArgs(List<ParamType> paramTypes, List<StructFieldType> fields) throws ReflectiveOperationException {
        List<Arg> args = new ArrayList<>();
        for (ParamType pType : paramTypes) {
            MemoryLayout layout = pType.layout(fields);
            List<Consumer<Object>> checks = new ArrayList<>();
            Object arg = makeArg(layout, checks, true);
            Arg.NativeType type = Arg.NativeType.of(pType.type(fields));
            args.add(pType == ParamType.STRUCT
                ? Arg.structArg(type, layout, arg, checks)
                : Arg.primitiveArg(type, layout, arg, checks));
        }
        return args;
    }

    private static void check(int index, MemorySegment ptr, List<Arg> args) {
        Arg varArg = args.get(index);
        MemoryLayout layout = varArg.layout;
        MethodHandle getter = varArg.getter;
        List<Consumer<Object>> checks = varArg.checks;
        try (Arena arena = Arena.openConfined()) {
            MemorySegment seg = MemorySegment.ofAddress(ptr.address(), layout.byteSize(), arena.session());
            Object obj = getter.invoke(seg);
            checks.forEach(check -> check.accept(obj));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static class CallInfo {
        static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
                C_POINTER.withName("writeback"), // writeback
                C_POINTER.withName("argIDs")); // arg ids

        static final VarHandle VH_writeback = LAYOUT.varHandle(groupElement("writeback"));
        static final VarHandle VH_argIDs = LAYOUT.varHandle(groupElement("argIDs"));

        static void writeback(MemorySegment seg, MemorySegment addr) {
            VH_writeback.set(seg, addr);
        }
        static void argIDs(MemorySegment seg, MemorySegment addr) {
            VH_argIDs.set(seg, addr);
        }
    }

    private static final class Arg {
        final NativeType id;
        final MemoryLayout layout;
        final Object value;
        final MethodHandle getter;
        final List<Consumer<Object>> checks;

        private Arg(NativeType id, MemoryLayout layout, Object value, MethodHandle getter, List<Consumer<Object>> checks) {
            this.id = id;
            this.layout = layout;
            this.value = value;
            this.getter = getter;
            this.checks = checks;
        }

        private static Arg primitiveArg(NativeType id, MemoryLayout layout, Object value, List<Consumer<Object>> checks) {
            return new Arg(id, layout, value, layout.varHandle().toMethodHandle(VarHandle.AccessMode.GET), checks);
        }

        private static Arg structArg(NativeType id, MemoryLayout layout, Object value, List<Consumer<Object>> checks) {
            return new Arg(id, layout, value, MethodHandles.identity(MemorySegment.class), checks);
        }

        enum NativeType {
            INT,
            FLOAT,
            DOUBLE,
            POINTER,
            S_I,
            S_F,
            S_D,
            S_P,
            S_II,
            S_IF,
            S_ID,
            S_IP,
            S_FI,
            S_FF,
            S_FD,
            S_FP,
            S_DI,
            S_DF,
            S_DD,
            S_DP,
            S_PI,
            S_PF,
            S_PD,
            S_PP,
            S_III,
            S_IIF,
            S_IID,
            S_IIP,
            S_IFI,
            S_IFF,
            S_IFD,
            S_IFP,
            S_IDI,
            S_IDF,
            S_IDD,
            S_IDP,
            S_IPI,
            S_IPF,
            S_IPD,
            S_IPP,
            S_FII,
            S_FIF,
            S_FID,
            S_FIP,
            S_FFI,
            S_FFF,
            S_FFD,
            S_FFP,
            S_FDI,
            S_FDF,
            S_FDD,
            S_FDP,
            S_FPI,
            S_FPF,
            S_FPD,
            S_FPP,
            S_DII,
            S_DIF,
            S_DID,
            S_DIP,
            S_DFI,
            S_DFF,
            S_DFD,
            S_DFP,
            S_DDI,
            S_DDF,
            S_DDD,
            S_DDP,
            S_DPI,
            S_DPF,
            S_DPD,
            S_DPP,
            S_PII,
            S_PIF,
            S_PID,
            S_PIP,
            S_PFI,
            S_PFF,
            S_PFD,
            S_PFP,
            S_PDI,
            S_PDF,
            S_PDD,
            S_PDP,
            S_PPI,
            S_PPF,
            S_PPD,
            S_PPP,
            ;

            public static NativeType of(String type) {
                return NativeType.valueOf(switch (type) {
                    case "int" -> "INT";
                    case "float" -> "FLOAT";
                    case "double" -> "DOUBLE";
                    case "void*" -> "POINTER";
                    default -> type.substring("struct ".length());
                });
            }
        }
    }

}
