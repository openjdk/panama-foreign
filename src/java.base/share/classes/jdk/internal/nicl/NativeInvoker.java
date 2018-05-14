/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.nicl;

import jdk.internal.nicl.abi.ArgumentBinding;
import jdk.internal.nicl.abi.CallingSequence;
import jdk.internal.nicl.abi.ShuffleRecipe;
import jdk.internal.nicl.abi.StorageClass;
import jdk.internal.nicl.abi.SystemABI;
import jdk.internal.nicl.abi.sysv.x64.Constants;
import jdk.internal.nicl.types.*;

import java.lang.annotation.Retention;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nicl.*;
import java.nicl.types.*;
import java.nicl.types.Pointer;
import java.util.ArrayList;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

class NativeInvoker {
    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.nicl.NativeInvoker.DEBUG"));

    private static final LayoutType<Long> LAYOUT_TYPE_LONG = LayoutTypeImpl.create(long.class);

    // Unbound MH for the invoke() method
    private static final MethodHandle BRIDGE_METHOD_HANDLE;
    private static final MethodHandle INVOKE_NATIVE_MH;

    @Retention(RUNTIME)
    private @interface InvokerMethod {
    }

    private static Method findInvokerMethod() {
        for (Method m : NativeInvoker.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(InvokerMethod.class)) {
                return m;
            }
        }

        throw new RuntimeException("Failed to locate invoke method");
    }

    static {
        try {
            MethodType INVOKE_NATIVE_MT = MethodType.methodType(void.class, long[].class, long[].class, long[].class, long.class);
            INVOKE_NATIVE_MH = MethodHandles.lookup().findStatic(NativeInvoker.class, "invokeNative", INVOKE_NATIVE_MT);
            BRIDGE_METHOD_HANDLE = MethodHandles.lookup().unreflect(findInvokerMethod());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }


    private static MethodHandle lookupNativeFunction(SymbolLookup lookup, String symbolName) throws NoSuchMethodException, IllegalAccessException {
        long addr = lookup.lookup(symbolName).getAddress().addr();
        return MethodHandles.insertArguments(INVOKE_NATIVE_MH, INVOKE_NATIVE_MH.type().parameterCount() - 1, addr);
    }

    private final MethodType methodType;
    private final boolean isVarArgs;
    private final String debugMethodString;
    private final java.lang.reflect.Type genericReturnType;
    private final MethodHandle targetMethodHandle;

    NativeInvoker(MethodType methodType, Boolean isVarArgs, SymbolLookup lookup, String symbolName, String debugMethodString, java.lang.reflect.Type genericReturnType) throws NoSuchMethodException, IllegalAccessException {
        this(methodType, isVarArgs, lookupNativeFunction(lookup, symbolName), debugMethodString, genericReturnType);
    }

    NativeInvoker(MethodType methodType, Boolean isVarArgs, SymbolLookup lookup, String symbolName) throws NoSuchMethodException, IllegalAccessException {
        this(methodType, isVarArgs, lookup, symbolName, "<unknown>", null);
    }

    private NativeInvoker(MethodType methodType, Boolean isVarArgs, MethodHandle targetMethodHandle, String debugMethodString, java.lang.reflect.Type genericReturnType) {
        this.methodType = methodType;
        this.isVarArgs = isVarArgs;
        this.targetMethodHandle = targetMethodHandle;
        this.debugMethodString = debugMethodString;
        this.genericReturnType = genericReturnType;
    }

    MethodHandle getBoundMethodHandle() {
        return BRIDGE_METHOD_HANDLE.bindTo(this).asCollector(Object[].class, methodType.parameterCount()).asType(methodType);
    }

    private Class<?> computeClass(Class<?> c) {
        if (c.isPrimitive()) {
            throw new IllegalArgumentException("Not expecting primitive type " + c.getName());
        }

        if (c == Integer.class) {
            return int.class;
        } else if (Pointer.class.isAssignableFrom(c)) {
            return Pointer.class;
        } else {
            throw new UnsupportedOperationException("Type unhandled: " + c.getName());
        }
    }

    private MethodType getDynamicMethodType(MethodType baseMethodType, Object[] unnamedArgs) {
        ArrayList<Class<?>> types = new ArrayList<>();

        Class<?>[] staticArgTypes = baseMethodType.parameterArray();

        // skip trailing Object[]
        for (int i = 0; i < staticArgTypes.length - 1; i++) {
            types.add(staticArgTypes[i]);
        }

        for (Object o : unnamedArgs) {
            types.add(computeClass(o.getClass()));
        }

        return MethodType.methodType(baseMethodType.returnType(), types.toArray(new Class<?>[0]));
    }

    private void dumpArgs(String debugMethodString, boolean isVarArgs, Object[] args) {
        System.err.println("invoking method " + debugMethodString);

        int nNamedArgs = args.length - (isVarArgs ? 1 : 0);
        for (int i = 0; i < nNamedArgs; i++) {
            System.err.println("    named arg: " + args[i]);
        }

        if (isVarArgs) {
            for (Object o : (Object[]) args[args.length - 1]) {
                System.err.println("  unnamed args: " + o);
            }
        }
    }


    @InvokerMethod
    private Object invoke(Object[] args) throws Throwable {
        if (DEBUG) {
            dumpArgs(debugMethodString, isVarArgs, args);
        }

        if (isVarArgs) {
            return invokeVarargs(args);
        } else {
            return invokeNormal(args);
        }
    }

    private Object invokeNormal(Object[] args) throws Throwable {
        CallingSequence callingSequence = SystemABI.getInstance().arrangeCall(Util.typeof(methodType));
        ShuffleRecipe shuffleRecipe = ShuffleRecipe.make(callingSequence);

        Reference<?> returnStruct = null;
        if (Util.isCStruct(methodType.returnType())) {
            // FIXME (STRUCT-LIFECYCLE):
            // Leak the allocated structs for now until the life cycle has been figured out
            Scope scope = new NativeScope();

            @SuppressWarnings("unchecked")
            Class<? extends Reference<?>> c = (Class<? extends Reference<?>>) methodType.returnType();
            LayoutType<? extends Reference<?>> lt = LayoutType.create(c);

            @SuppressWarnings("unchecked")
            Reference<?> r = scope.allocateStruct((LayoutType)lt);

            returnStruct = r;
        }

        int nValues = shuffleRecipe.getNoofArgumentPulls();
        long[] values = new long[nValues];
        if (nValues != 0) {
            int curValueArrayIndex = 0;
            for (StorageClass c : Constants.ARGUMENT_STORAGE_CLASSES) {
                for (ArgumentBinding binding : callingSequence.getBindings(c)) {
                    if (binding == null) {
                        // stack slot padding/alignment
                        continue;
                    }

                    if (c == StorageClass.INTEGER_ARGUMENT_REGISTER &&
                        callingSequence.returnsInMemory() &&
                        binding.getStorage().getStorageIndex() == 0) {
                        long n = binding.getStorage().getSize() / 8;
                        assert returnStruct != null;
                        long[] argValues = new long[] { Util.unpack(returnStruct.ptr()) };
                        System.arraycopy(argValues, 0, values, curValueArrayIndex, (int)n);
                        curValueArrayIndex += n;
                    } else {
                        long n = binding.getStorage().getSize() / 8;

                        int argIndex = binding.getMember().getArgumentIndex();

                        long[] argValues = getArgumentValues(binding.getMember().getCarrierType(methodType), binding, n, args[argIndex]);
                        if (n != argValues.length) {
                            throw new InternalError("carrierType: " + binding.getMember().getCarrierType(methodType) + " binding: " + binding + " n: " + n + " argValues.length: " + argValues.length);
                        }
                        System.arraycopy(argValues, 0, values, curValueArrayIndex, (int)n);
                        curValueArrayIndex += n;
                    }
                }
            }
        }

        if (DEBUG) {
            System.err.println("Invoking method " + debugMethodString + " with " + values.length + " argument values");
            for (int i = 0; i < values.length; i++) {
                System.err.println("value[" + i + "] = 0x" + Long.toHexString(values[i]));
            }
        }

        long[] returnValues = new long[shuffleRecipe.getNoofReturnPulls()];

        targetMethodHandle.invokeExact(values, returnValues, shuffleRecipe.getRecipe());

        if (DEBUG) {
            System.err.println("Returned from method " + debugMethodString + " with " + returnValues.length + " return values");
            for (int i = 0; i < returnValues.length; i++) {
                System.err.println("returnValues[" + i + "] = 0x" + Long.toHexString(returnValues[i]));
            }
        }

        return processReturnValue(callingSequence, returnValues, returnStruct);
    }

    private Object processReturnValue(CallingSequence callingSequence, long[] returnValues, Reference<?> returnStruct) {
        if (methodType.returnType() == void.class) {
            return null;
        } else if (methodType.returnType().isPrimitive()) {
            switch (PrimitiveClassType.typeof(methodType.returnType())) {
                case BYTE:
                    return (byte)returnValues[0];
                case BOOLEAN:
                    return returnValues[0] != 0;
                case SHORT:
                    return (short)returnValues[0];
                case CHAR:
                    return (char)returnValues[0];
                case INT:
                    return (int)returnValues[0];
                case LONG:
                    return returnValues[0];
                case FLOAT:
                    return Float.intBitsToFloat((int)returnValues[0]);
                case DOUBLE:
                    return Double.longBitsToDouble(returnValues[0]);
                case VOID:
                default:
                    throw new UnsupportedOperationException("NYI: " + methodType.returnType().getName());
            }
        } else if (Pointer.class.isAssignableFrom(methodType.returnType())) {
            return Util.createPtr(returnValues[0], Util.createLayoutType(genericReturnType).getInnerType());
        } else if (Util.isCStruct(methodType.returnType())) {
            if (!callingSequence.returnsInMemory()) {
                int curValueArrayIndex = 0;

                for (StorageClass c : Constants.RETURN_STORAGE_CLASSES) {
                    for (ArgumentBinding binding : callingSequence.getBindings(c)) {
                        assert binding != null;
                        assert returnStruct != null;

                        int n = (int)(binding.getStorage().getSize() / 8);
                        copyStructReturnValue(returnStruct, binding.getOffset(), returnValues, curValueArrayIndex, n);
                        curValueArrayIndex += n;
                    }
                }
            }

            return returnStruct;
        } else if (Util.isFunctionalInterface(methodType.returnType())) {
            // FIXME: NIY
            return null;
        } else {
            throw new UnsupportedOperationException("Unhandled type: " + methodType.returnType().getName());
        }
    }

    private Pointer<Long> rawStructPointer(Reference<?> struct, long offset) {
        if ((offset % LAYOUT_TYPE_LONG.getNativeTypeSize()) != 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        return struct.ptr().cast(LAYOUT_TYPE_LONG).offset(offset / LAYOUT_TYPE_LONG.getNativeTypeSize());
    }

    public void rawStructWrite(Reference<?> struct, long offset, long value) {
        rawStructPointer(struct, offset).lvalue().set(value);
    }

    private void copyStructReturnValue(Reference<?> structReturn, long structOffset, long[] returnValues, int curReturnValuesIndex, int n) {
        for (int i = 0; i < n; i++) {
            long offs = structOffset + i * 8;
            rawStructWrite(structReturn, offs, returnValues[curReturnValuesIndex]);
            curReturnValuesIndex++;
        }
    }

    private long[] getArgumentValues(Class<?> carrierType, ArgumentBinding binding, long n, Object arg) throws Throwable {
        if (carrierType.isPrimitive()) {
            switch (PrimitiveClassType.typeof(carrierType)) {
                case INT:
                    return new long[]{(Integer) arg};
                case BYTE:
                    return new long[]{(Byte) arg};
                case BOOLEAN:
                    return new long[]{(Boolean) arg ? 1 : 0};
                case SHORT:
                    return new long[]{(Short) arg};
                case CHAR:
                    return new long[]{(Character) arg};
                case LONG:
                    return new long[]{(Long) arg};
                case FLOAT:
                    return new long[]{Integer.toUnsignedLong(Float.floatToRawIntBits((Float) arg))};
                case DOUBLE:
                    return new long[]{Double.doubleToRawLongBits((Double) arg)};

                case VOID:
                default:
                    throw new IllegalArgumentException(carrierType.getName());
            }
        } else if (carrierType.isArray()) {
            throw new IllegalArgumentException("Array types NIY: " + carrierType);
        } else if (Pointer.class.isAssignableFrom(carrierType)) {
            return new long[] { Util.unpack((Pointer)arg) };
        } else if (Util.isCStruct(carrierType)) {
            long[] values = new long[(int)n];
            Reference<?> r = (Reference<?>)arg;

            Pointer<Long> src = r.ptr().cast(LayoutType.create(byte.class)).offset(binding.getOffset()).cast(LayoutType.create(long.class));

            for (int i = 0; i < n; i++) {
                values[i] = src.offset(i).lvalue().get();
            }
            return values;
        } else if (Util.isFunctionalInterface(carrierType)) {
            return new long[] { UpcallHandler.make(carrierType, arg).getNativeEntryPoint().addr() };
        } else {
            throw new UnsupportedOperationException("NYI: " + carrierType.getName());
        }
    }

    private Object invokeVarargs(Object[] args) throws Throwable {
        // one trailing Object[]
        int nNamedArgs = methodType.parameterCount() - 1;
        Object[] unnamedArgs = (Object[]) args[args.length - 1];

        // flatten argument list so that it can be passed to an asSpreader MH
        Object[] allArgs = new Object[nNamedArgs + unnamedArgs.length];
        System.arraycopy(args, 0, allArgs, 0, nNamedArgs);
        System.arraycopy(unnamedArgs, 0, allArgs, nNamedArgs, unnamedArgs.length);

        MethodType dynamicMethodType = getDynamicMethodType(methodType, unnamedArgs);

        NativeInvoker delegate = new NativeInvoker(dynamicMethodType, false, targetMethodHandle, debugMethodString, genericReturnType);
        return delegate.invoke(allArgs);
    }

    //natives

    static native void invokeNative(long[] args, long[] rets, long[] recipe, long addr);
    static native long allocateUpcallStub(int id);
    static native void freeUpcallStub(int id, long addr);
    static native long findNativeAddress(String name);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
