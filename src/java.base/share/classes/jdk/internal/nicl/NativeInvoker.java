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

import java.lang.annotation.Retention;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nicl.*;
import java.nicl.layout.Function;
import java.nicl.layout.Layout;
import java.nicl.types.*;
import java.nicl.types.Pointer;
import java.util.ArrayList;
import java.util.stream.Stream;

import static sun.security.action.GetPropertyAction.privilegedGetProperty;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

class NativeInvoker {
    private static final boolean DEBUG = Boolean.parseBoolean(
        privilegedGetProperty("jdk.internal.nicl.NativeInvoker.DEBUG"));

    private static final LayoutType<Long> LAYOUT_TYPE_LONG = NativeTypes.INT64;

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
    private final Function function;

    NativeInvoker(Function function, MethodType methodType, Boolean isVarArgs, SymbolLookup lookup, String symbolName, String debugMethodString, java.lang.reflect.Type genericReturnType) throws NoSuchMethodException, IllegalAccessException {
        this(function, methodType, isVarArgs, lookupNativeFunction(lookup, symbolName), debugMethodString, genericReturnType);
    }

    private NativeInvoker(Function function, MethodType methodType, Boolean isVarArgs, MethodHandle targetMethodHandle, String debugMethodString, java.lang.reflect.Type genericReturnType) {
        this.function = function;
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

        if (c == Byte.class || c == Short.class || c == Character.class || c == Integer.class || c == Long.class) {
            return long.class;
        } else if (c == Float.class || c == Double.class) {
            return double.class;
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
        //Fixme: this should use the function field, not creating the layout via reflection!!!
        CallingSequence callingSequence = SystemABI.getInstance().arrangeCall(function);
        ShuffleRecipe shuffleRecipe = ShuffleRecipe.make(callingSequence);

        Struct<?> returnStruct = null;
        if (Util.isCStruct(methodType.returnType())) {
            // FIXME (STRUCT-LIFECYCLE):
            // Leak the allocated structs for now until the life cycle has been figured out
            Scope scope = Scope.newNativeScope();

            Class<?> c = methodType.returnType();
            @SuppressWarnings("unchecked")
            Struct<?> r = scope.allocateStruct((Class)c);

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
                        values[curValueArrayIndex] = returnStruct.ptr().addr();
                        curValueArrayIndex++;
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

    private Object processReturnValue(CallingSequence callingSequence, long[] returnValues, Struct<?> returnStruct) {
        if (methodType.returnType() == void.class) {
            return null;
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
        } else {
            Pointer<Long> ptr = Scope.newHeapScope().allocate(NativeTypes.INT64);
            ptr.set(returnValues[0]);
            try {
                return Util.unsafeCast(ptr, Util.makeType(genericReturnType, function.returnLayout().get())).type().getter().invoke(ptr);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private Pointer<Long> rawStructPointer(Struct<?> struct, long offset) {
        if ((offset % LAYOUT_TYPE_LONG.bytesSize()) != 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        return Util.unsafeCast(struct.ptr(), LAYOUT_TYPE_LONG).offset(offset / LAYOUT_TYPE_LONG.bytesSize());
    }

    public void rawStructWrite(Struct<?> struct, long offset, long value) {
        rawStructPointer(struct, offset).set(value);
    }

    private void copyStructReturnValue(Struct<?> structReturn, long structOffset, long[] returnValues, int curReturnValuesIndex, int n) {
        for (int i = 0; i < n; i++) {
            long offs = structOffset + i * 8;
            rawStructWrite(structReturn, offs, returnValues[curReturnValuesIndex]);
            curReturnValuesIndex++;
        }
    }

    private long[] getArgumentValues(Class<?> carrierType, ArgumentBinding binding, long n, Object arg) throws Throwable {
        if (Util.isCStruct(carrierType)) {
            long[] values = new long[(int)n];
            Struct<?> r = (Struct<?>)arg;

            Pointer<Long> src = Util.unsafeCast(Util.unsafeCast(r.ptr(), NativeTypes.UINT8).offset(binding.getOffset()), NativeTypes.UINT64);
            for (int i = 0; i < n; i++) {
                values[i] = src.offset(i).get();
            }
            return values;
        } else if (Util.isCallback(carrierType)) {
            return new long[] { UpcallHandler.make(carrierType, arg).getNativeEntryPoint().addr() };
        } else {
            Pointer<Long> ptr = Scope.newHeapScope().allocate(NativeTypes.INT64);
            Util.unsafeCast(ptr, Util.makeType(carrierType, function.argumentLayouts().get(binding.getMember().getArgumentIndex())))
                    .type().setter().invoke(ptr, arg);
            return new long[] { ptr.get() };
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

        //we need to infer layouts for all unnamed arguments
        Layout[] argLayouts = Stream.concat(function.argumentLayouts().stream(),
                Stream.of(unnamedArgs).map(Object::getClass).map(Util::variadicLayout))
                .toArray(Layout[]::new);

        Function varargFunc = function.returnLayout().isPresent() ?
                Function.of(function.returnLayout().get(), false, argLayouts) :
                Function.ofVoid(false, argLayouts);

        MethodType dynamicMethodType = getDynamicMethodType(methodType, unnamedArgs);

        NativeInvoker delegate = new NativeInvoker(varargFunc, dynamicMethodType, false, targetMethodHandle, debugMethodString, genericReturnType);
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
